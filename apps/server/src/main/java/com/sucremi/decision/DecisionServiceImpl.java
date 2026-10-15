package com.sucremi.decision;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.sucremi.activity.ProjectActivityService;
import com.sucremi.activity.ProjectActivityType;
import com.sucremi.common.api.PageResult;
import com.sucremi.common.exception.BusinessException;
import com.sucremi.common.exception.ResourceConflictException;
import com.sucremi.common.exception.ResourceNotFoundException;
import com.sucremi.decision.dto.DecisionRequest;
import com.sucremi.decision.dto.DecisionResponse;
import com.sucremi.decision.dto.DecisionRevisionResponse;
import com.sucremi.decision.dto.KeyDecisionResponse;
import com.sucremi.decision.dto.SupersedeDecisionRequest;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.Project;
import com.sucremi.project.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 决策服务实现；持久化读写始终限定当前用户及所属项目。 */
@Service
public class DecisionServiceImpl implements DecisionService {
    private final ProjectMapper projectMapper;
    private final ProjectDecisionMapper decisionMapper;
    private final ProjectDecisionRevisionMapper revisionMapper;
    private final CurrentUserResolver currentUser;
    private final ProjectActivityService activityService;
    private final ObjectMapper objectMapper;

    /** 创建决策服务。 */
    public DecisionServiceImpl(ProjectMapper projectMapper, ProjectDecisionMapper decisionMapper,
            ProjectDecisionRevisionMapper revisionMapper, CurrentUserResolver currentUser,
            ProjectActivityService activityService, ObjectMapper objectMapper) {
        this.projectMapper = projectMapper;
        this.decisionMapper = decisionMapper;
        this.revisionMapper = revisionMapper;
        this.currentUser = currentUser;
        this.activityService = activityService;
        this.objectMapper = objectMapper;
    }

    /** 按项目分页读取决策，默认已采纳优先、最近更新优先。 */
    @Override
    public PageResult<DecisionResponse> list(long projectId, int page, int size, DecisionStatus status,
                                             String sortBy, String direction) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        boolean ascending = "asc".equalsIgnoreCase(direction);
        String field = "createdAt".equals(sortBy) ? "createdAt" : "updatedAt";
        Page<ProjectDecision> result = decisionMapper.selectPage(new Page<>(Math.max(page, 1), clamp(size)),
                Wrappers.<ProjectDecision>lambdaQuery().eq(ProjectDecision::getUserId, userId)
                        .eq(ProjectDecision::getProjectId, projectId).isNull(ProjectDecision::getDeletedAt)
                        .eq(status != null, ProjectDecision::getStatus, status == null ? null : status.name())
                        .orderByAsc(ProjectDecision::getStatus)
                        .orderBy(true, ascending, "createdAt".equals(field) ? ProjectDecision::getCreatedAt
                                : ProjectDecision::getUpdatedAt)
                        .orderByDesc(ProjectDecision::getId));
        List<DecisionResponse> rows = result.getRecords().stream().map(this::toResponse).toList();
        return new PageResult<>(rows, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /** 创建决策，并在同一事务内保存初始快照与活动。 */
    @Override
    @Transactional
    public DecisionResponse create(long projectId, DecisionRequest request) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        if (request.expectedVersion() != null) throw new BusinessException(40000, "创建决策不应提交版本号");
        validateAccepted(request.status(), request.decision(), request.rationale());
        ProjectDecision item = new ProjectDecision();
        item.setUserId(userId);
        item.setProjectId(projectId);
        apply(item, request);
        item.setStatus(request.status().name());
        item.setVersion(1);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(item.getCreatedAt());
        decisionMapper.insert(item);
        insertRevision(item, null);
        record(userId, projectId, ProjectActivityType.DECISION_CREATED, item, null);
        return toResponse(item);
    }

    /** 读取当前用户项目内未删除的决策详情。 */
    @Override
    public DecisionResponse get(long projectId, long decisionId) {
        return toResponse(requireOwnedDecision(projectId, decisionId, currentUser.currentUserId()));
    }

    /** 使用乐观锁编辑决策；无实际变化时不增加版本也不记录事件。 */
    @Override
    @Transactional
    public DecisionResponse update(long projectId, long decisionId, DecisionRequest request) {
        long userId = currentUser.currentUserId();
        ProjectDecision item = requireOwnedDecision(projectId, decisionId, userId);
        checkVersion(item, request.expectedVersion());
        DecisionStatus oldStatus = DecisionStatus.valueOf(item.getStatus());
        if (oldStatus == DecisionStatus.SUPERSEDED) throw new BusinessException(40000, "已被替代的决策不可编辑");
        DecisionStatus newStatus = request.status();
        if (oldStatus == DecisionStatus.ACCEPTED && newStatus != DecisionStatus.ACCEPTED) {
            throw new BusinessException(40000, "已采纳决策只能通过替代操作结束");
        }
        validateAccepted(newStatus, request.decision(), request.rationale());
        ProjectDecision before = copy(item);
        apply(item, request);
        item.setStatus(newStatus.name());
        if (sameContent(before, item)) return toResponse(before);
        LocalDateTime now = LocalDateTime.now();
        int nextVersion = item.getVersion() + 1;
        int rows = decisionMapper.update(null, Wrappers.<ProjectDecision>lambdaUpdate()
                .eq(ProjectDecision::getId, decisionId).eq(ProjectDecision::getUserId, userId)
                .eq(ProjectDecision::getProjectId, projectId).eq(ProjectDecision::getVersion, request.expectedVersion())
                .isNull(ProjectDecision::getDeletedAt)
                .set(ProjectDecision::getTitle, item.getTitle()).set(ProjectDecision::getContext, item.getContext())
                .set(ProjectDecision::getOptionsJson, item.getOptionsJson()).set(ProjectDecision::getDecision, item.getDecision())
                .set(ProjectDecision::getRationale, item.getRationale()).set(ProjectDecision::getConsequences, item.getConsequences())
                .set(ProjectDecision::getRevisitCondition, item.getRevisitCondition()).set(ProjectDecision::getStatus, item.getStatus())
                .set(ProjectDecision::getVersion, nextVersion).set(ProjectDecision::getUpdatedAt, now));
        if (rows == 0) throw new ResourceConflictException("决策已被其他操作更新，请刷新后重试");
        item.setVersion(nextVersion);
        item.setUpdatedAt(now);
        insertRevision(item, request.changeSummary());
        ProjectActivityType type = oldStatus != newStatus && newStatus == DecisionStatus.ACCEPTED
                ? ProjectActivityType.DECISION_ACCEPTED : ProjectActivityType.DECISION_UPDATED;
        record(userId, projectId, type, item, null);
        return toResponse(item);
    }

    /** 分页读取不可变的历史快照。 */
    @Override
    public PageResult<DecisionRevisionResponse> revisions(long projectId, long decisionId, int page, int size) {
        long userId = currentUser.currentUserId();
        requireOwnedDecision(projectId, decisionId, userId);
        Page<ProjectDecisionRevision> result = revisionMapper.selectPage(new Page<>(Math.max(page, 1), clamp(size)),
                Wrappers.<ProjectDecisionRevision>lambdaQuery().eq(ProjectDecisionRevision::getUserId, userId)
                        .eq(ProjectDecisionRevision::getProjectId, projectId)
                        .eq(ProjectDecisionRevision::getDecisionId, decisionId)
                        .orderByDesc(ProjectDecisionRevision::getRevisionNumber));
        List<DecisionRevisionResponse> rows = result.getRecords().stream().map(this::toRevisionResponse).toList();
        return new PageResult<>(rows, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /** 原子创建新采纳决策并将旧决策标记为已替代。 */
    @Override
    @Transactional
    public DecisionResponse supersede(long projectId, long decisionId, SupersedeDecisionRequest request) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        ProjectDecision old = requireOwnedDecision(projectId, decisionId, userId);
        checkVersion(old, request.expectedVersion());
        if (DecisionStatus.valueOf(old.getStatus()) == DecisionStatus.SUPERSEDED) {
            throw new BusinessException(40000, "该决策已被替代");
        }
        ProjectDecision replacement = new ProjectDecision();
        replacement.setUserId(userId);
        replacement.setProjectId(projectId);
        replacement.setTitle(request.title().trim());
        replacement.setContext(request.context().trim());
        replacement.setOptionsJson(writeJson(request.options() == null ? List.of() : request.options()));
        replacement.setDecision(request.decision().trim());
        replacement.setRationale(request.rationale().trim());
        replacement.setConsequences(trimToNull(request.consequences()));
        replacement.setRevisitCondition(trimToNull(request.revisitCondition()));
        replacement.setStatus(DecisionStatus.ACCEPTED.name());
        replacement.setVersion(1);
        replacement.setCreatedAt(LocalDateTime.now());
        replacement.setUpdatedAt(replacement.getCreatedAt());
        decisionMapper.insert(replacement);
        insertRevision(replacement, "替代决策创建");

        old.setStatus(DecisionStatus.SUPERSEDED.name());
        old.setSupersededById(replacement.getId());
        old.setVersion(old.getVersion() + 1);
        old.setUpdatedAt(LocalDateTime.now());
        int rows = decisionMapper.update(null, Wrappers.<ProjectDecision>lambdaUpdate()
                .eq(ProjectDecision::getId, decisionId).eq(ProjectDecision::getUserId, userId)
                .eq(ProjectDecision::getProjectId, projectId).eq(ProjectDecision::getVersion, request.expectedVersion())
                .isNull(ProjectDecision::getDeletedAt).set(ProjectDecision::getStatus, old.getStatus())
                .set(ProjectDecision::getSupersededById, replacement.getId()).set(ProjectDecision::getVersion, old.getVersion())
                .set(ProjectDecision::getUpdatedAt, old.getUpdatedAt()));
        if (rows == 0) throw new ResourceConflictException("决策已被其他操作更新，请刷新后重试");
        insertRevision(old, request.changeSummary());
        record(userId, projectId, ProjectActivityType.DECISION_SUPERSEDED, old, replacement.getId());
        record(userId, projectId, ProjectActivityType.DECISION_ACCEPTED, replacement, decisionId);
        return toResponse(replacement);
    }

    /** 将提议中的误创建记录软删除，并保留删除后的快照。 */
    @Override
    @Transactional
    public void delete(long projectId, long decisionId) {
        long userId = currentUser.currentUserId();
        ProjectDecision item = requireOwnedDecision(projectId, decisionId, userId);
        if (DecisionStatus.valueOf(item.getStatus()) != DecisionStatus.PROPOSED) {
            throw new BusinessException(40000, "仅提议中的误创建决策可以删除");
        }
        LocalDateTime now = LocalDateTime.now();
        item.setDeletedAt(now);
        item.setVersion(item.getVersion() + 1);
        item.setUpdatedAt(now);
        int rows = decisionMapper.update(null, Wrappers.<ProjectDecision>lambdaUpdate()
                .eq(ProjectDecision::getId, decisionId).eq(ProjectDecision::getUserId, userId)
                .eq(ProjectDecision::getProjectId, projectId).eq(ProjectDecision::getVersion, item.getVersion() - 1)
                .isNull(ProjectDecision::getDeletedAt).set(ProjectDecision::getDeletedAt, now)
                .set(ProjectDecision::getVersion, item.getVersion()).set(ProjectDecision::getUpdatedAt, now));
        if (rows == 0) throw new ResourceConflictException("决策已被其他操作更新，请刷新后重试");
        insertRevision(item, "软删除误创建决策");
        record(userId, projectId, ProjectActivityType.DECISION_DELETED, item, null);
    }

    /** 获取最多三条最近更新的有效决策，用于上下文恢复。 */
    @Override
    public List<KeyDecisionResponse> keyDecisions(long projectId) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        return decisionMapper.selectList(Wrappers.<ProjectDecision>lambdaQuery()
                        .eq(ProjectDecision::getUserId, userId).eq(ProjectDecision::getProjectId, projectId)
                        .eq(ProjectDecision::getStatus, DecisionStatus.ACCEPTED.name())
                        .isNull(ProjectDecision::getDeletedAt).orderByDesc(ProjectDecision::getUpdatedAt)
                        .orderByDesc(ProjectDecision::getId).last("LIMIT 3"))
                .stream().map(item -> new KeyDecisionResponse(item.getId(), item.getTitle(), item.getDecision(),
                        truncate(item.getRationale(), 180))).toList();
    }

    private void apply(ProjectDecision item, DecisionRequest request) {
        item.setTitle(request.title().trim());
        item.setContext(request.context().trim());
        item.setOptionsJson(writeJson(request.options() == null ? List.of() : request.options()));
        item.setDecision(trimToNull(request.decision()));
        item.setRationale(trimToNull(request.rationale()));
        item.setConsequences(trimToNull(request.consequences()));
        item.setRevisitCondition(trimToNull(request.revisitCondition()));
    }

    private void validateAccepted(DecisionStatus status, String decision, String rationale) {
        if (status == DecisionStatus.ACCEPTED
                && (decision == null || decision.isBlank() || rationale == null || rationale.isBlank())) {
            throw new BusinessException(40000, "采纳决策时必须填写最终选择和选择理由");
        }
    }

    private void checkVersion(ProjectDecision item, Integer expectedVersion) {
        if (expectedVersion == null) throw new BusinessException(40000, "缺少决策版本号");
        if (!expectedVersion.equals(item.getVersion())) {
            throw new ResourceConflictException("决策已被其他操作更新，请刷新后重试");
        }
    }

    private ProjectDecision requireOwnedDecision(long projectId, long decisionId, long userId) {
        requireOwnedProject(projectId, userId);
        ProjectDecision item = decisionMapper.selectOne(Wrappers.<ProjectDecision>lambdaQuery()
                .eq(ProjectDecision::getId, decisionId).eq(ProjectDecision::getProjectId, projectId)
                .eq(ProjectDecision::getUserId, userId).isNull(ProjectDecision::getDeletedAt));
        if (item == null) throw new ResourceNotFoundException("决策不存在");
        return item;
    }

    private Project requireOwnedProject(long projectId, long userId) {
        Project project = projectMapper.selectOne(Wrappers.<Project>lambdaQuery()
                .eq(Project::getId, projectId).eq(Project::getUserId, userId));
        if (project == null) throw new ResourceNotFoundException("项目不存在");
        return project;
    }

    private void insertRevision(ProjectDecision item, String summary) {
        ProjectDecisionRevision revision = new ProjectDecisionRevision();
        revision.setUserId(item.getUserId());
        revision.setProjectId(item.getProjectId());
        revision.setDecisionId(item.getId());
        revision.setRevisionNumber(item.getVersion());
        revision.setSnapshotJson(snapshot(item));
        revision.setChangeSummary(trimToNull(summary));
        revision.setCreatedAt(LocalDateTime.now());
        revisionMapper.insert(revision);
    }

    private String snapshot(ProjectDecision item) {
        return writeJson(new DecisionSnapshot(item.getId(), item.getProjectId(), item.getTitle(), item.getContext(),
                readOptions(item.getOptionsJson()), item.getDecision(), item.getRationale(), item.getConsequences(),
                item.getRevisitCondition(), DecisionStatus.valueOf(item.getStatus()), item.getSupersededById(),
                item.getVersion(), item.getCreatedAt(), item.getUpdatedAt(), item.getDeletedAt()));
    }

    private void record(long userId, long projectId, ProjectActivityType type, ProjectDecision item, Long relatedId) {
        String metadata = writeJson(new ActivityMetadata(item.getId(), truncate(item.getTitle(), 120), relatedId));
        activityService.record(userId, projectId, type, metadata);
    }

    private boolean sameContent(ProjectDecision a, ProjectDecision b) {
        return java.util.Objects.equals(a.getTitle(), b.getTitle())
                && java.util.Objects.equals(a.getContext(), b.getContext())
                && java.util.Objects.equals(a.getOptionsJson(), b.getOptionsJson())
                && java.util.Objects.equals(a.getDecision(), b.getDecision())
                && java.util.Objects.equals(a.getRationale(), b.getRationale())
                && java.util.Objects.equals(a.getConsequences(), b.getConsequences())
                && java.util.Objects.equals(a.getRevisitCondition(), b.getRevisitCondition())
                && java.util.Objects.equals(a.getStatus(), b.getStatus());
    }

    private ProjectDecision copy(ProjectDecision source) {
        ProjectDecision target = new ProjectDecision();
        target.setId(source.getId()); target.setUserId(source.getUserId()); target.setProjectId(source.getProjectId());
        target.setTitle(source.getTitle()); target.setContext(source.getContext()); target.setOptionsJson(source.getOptionsJson());
        target.setDecision(source.getDecision()); target.setRationale(source.getRationale()); target.setConsequences(source.getConsequences());
        target.setRevisitCondition(source.getRevisitCondition()); target.setStatus(source.getStatus());
        target.setSupersededById(source.getSupersededById()); target.setVersion(source.getVersion());
        target.setCreatedAt(source.getCreatedAt()); target.setUpdatedAt(source.getUpdatedAt()); target.setDeletedAt(source.getDeletedAt());
        return target;
    }

    private DecisionResponse toResponse(ProjectDecision item) {
        return new DecisionResponse(item.getId(), item.getProjectId(), item.getTitle(), item.getContext(),
                readOptions(item.getOptionsJson()), item.getDecision(), item.getRationale(), item.getConsequences(),
                item.getRevisitCondition(), DecisionStatus.valueOf(item.getStatus()), item.getSupersededById(),
                item.getVersion(), item.getCreatedAt(), item.getUpdatedAt());
    }

    private DecisionRevisionResponse toRevisionResponse(ProjectDecisionRevision revision) {
        try {
            JsonNode node = objectMapper.readTree(revision.getSnapshotJson());
            return new DecisionRevisionResponse(revision.getId(), revision.getProjectId(), revision.getDecisionId(), revision.getRevisionNumber(),
                    text(node, "title"), text(node, "context"), stringList(node.get("options")), text(node, "decision"),
                    text(node, "rationale"), text(node, "consequences"), text(node, "revisitCondition"),
                    DecisionStatus.valueOf(text(node, "status")), node.path("supersededById").isNull()
                    ? null : node.path("supersededById").asLong(), node.path("version").asInt(), revision.getChangeSummary(),
                    revision.getCreatedAt());
        } catch (JacksonException ex) {
            throw new IllegalStateException("决策历史快照格式无效", ex);
        }
    }

    private List<String> readOptions(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)); }
        catch (JacksonException ex) { throw new IllegalStateException("决策备选方案格式无效", ex); }
    }

    private List<String> stringList(JsonNode node) {
        if (node == null || !node.isArray()) return List.of();
        java.util.ArrayList<String> result = new java.util.ArrayList<>();
        node.forEach(value -> result.add(value.asText()));
        return List.copyOf(result);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String writeJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JacksonException ex) { throw new IllegalStateException("无法序列化决策数据", ex); }
    }

    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String truncate(String value, int limit) { return value == null || value.length() <= limit ? value : value.substring(0, limit); }
    private int clamp(int size) { return Math.min(Math.max(size, 1), 100); }

    private record DecisionSnapshot(long id, long projectId, String title, String context, List<String> options,
            String decision, String rationale, String consequences, String revisitCondition, DecisionStatus status,
            Long supersededById, int version, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) { }
    private record ActivityMetadata(long decisionId, String title, Long relatedDecisionId) { }
}
