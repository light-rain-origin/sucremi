package com.sucremi.recovery;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sucremi.common.exception.BusinessException;
import com.sucremi.common.exception.ResourceNotFoundException;
import com.sucremi.activity.ProjectActivityService;
import com.sucremi.activity.ProjectActivityType;
import com.sucremi.decision.DecisionService;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.Project;
import com.sucremi.project.ProjectMapper;
import com.sucremi.project.dto.ProjectResponse;
import com.sucremi.recovery.dto.ActionResponse;
import com.sucremi.recovery.dto.CheckpointResponse;
import com.sucremi.recovery.dto.CreateActionRequest;
import com.sucremi.recovery.dto.CreateCheckpointRequest;
import com.sucremi.recovery.dto.RecoveryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

/**
 * 上下文恢复业务实现，负责项目归属校验、检查点与行动维护及恢复摘要聚合。
 *
 * <p>所有持久化查询和更新均附带当前用户 ID；跨用户资源统一按不存在处理。</p>
 */
@Service
public class RecoveryServiceImpl implements RecoveryService {
    /** 尚未完成的行动状态。 */
    private static final String TODO = "TODO";
    /** 已完成的行动状态。 */
    private static final String DONE = "DONE";

    private final ProjectMapper projectMapper;
    private final ProjectCheckpointMapper checkpointMapper;
    private final ProjectActionMapper actionMapper;
    private final CurrentUserResolver currentUser;
    private final ProjectActivityService activityService;
    private final DecisionService decisionService;

    /** 创建恢复服务并注入项目、检查点、行动数据访问及当前身份解析器。 */
    public RecoveryServiceImpl(ProjectMapper projectMapper, ProjectCheckpointMapper checkpointMapper,
                               ProjectActionMapper actionMapper, CurrentUserResolver currentUser,
                               ProjectActivityService activityService, DecisionService decisionService) {
        this.projectMapper = projectMapper;
        this.checkpointMapper = checkpointMapper;
        this.actionMapper = actionMapper;
        this.currentUser = currentUser;
        this.activityService = activityService;
        this.decisionService = decisionService;
    }

    /** 校验项目归属后创建新的检查点快照。 */
    @Override
    @Transactional
    public CheckpointResponse createCheckpoint(long projectId, CreateCheckpointRequest request) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        ProjectCheckpoint checkpoint = new ProjectCheckpoint();
        checkpoint.setUserId(userId);
        checkpoint.setProjectId(projectId);
        checkpoint.setSummary(request.summary().trim());
        checkpoint.setCurrentFocus(trimToNull(request.currentFocus()));
        checkpoint.setBlockers(trimToNull(request.blockers()));
        checkpoint.setCreatedAt(LocalDateTime.now());
        checkpointMapper.insert(checkpoint);
        activityService.record(userId, projectId, ProjectActivityType.CHECKPOINT_CREATED,
                "{\"checkpointId\":" + checkpoint.getId() + "}");
        return toResponse(checkpoint);
    }

    /** 查询当前用户在指定项目下的检查点历史。 */
    @Override
    public List<CheckpointResponse> listCheckpoints(long projectId) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        return checkpointMapper.selectList(Wrappers.<ProjectCheckpoint>lambdaQuery()
                        .eq(ProjectCheckpoint::getUserId, userId)
                        .eq(ProjectCheckpoint::getProjectId, projectId)
                        .orderByDesc(ProjectCheckpoint::getCreatedAt)
                        .orderByDesc(ProjectCheckpoint::getId))
                .stream().map(this::toResponse).toList();
    }

    /** 在用户和项目范围内删除检查点，不存在时按 404 处理。 */
    @Override
    @Transactional
    public void deleteCheckpoint(long projectId, long checkpointId) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        int rows = checkpointMapper.delete(Wrappers.<ProjectCheckpoint>lambdaQuery()
                .eq(ProjectCheckpoint::getId, checkpointId)
                .eq(ProjectCheckpoint::getUserId, userId)
                .eq(ProjectCheckpoint::getProjectId, projectId));
        if (rows == 0) throw new ResourceNotFoundException("检查点不存在");
    }

    /** 读取当前用户在指定项目下最新的检查点。 */
    @Override
    public CheckpointResponse latestCheckpoint(long projectId) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        ProjectCheckpoint checkpoint = checkpointMapper.selectOne(Wrappers.<ProjectCheckpoint>lambdaQuery()
                .eq(ProjectCheckpoint::getUserId, userId)
                .eq(ProjectCheckpoint::getProjectId, projectId)
                .orderByDesc(ProjectCheckpoint::getCreatedAt)
                .orderByDesc(ProjectCheckpoint::getId)
                .last("LIMIT 1"));
        if (checkpoint == null) {
            throw new ResourceNotFoundException("检查点不存在");
        }
        return toResponse(checkpoint);
    }

    /** 创建待办行动，并将其顺序放在已有行动之后。 */
    @Override
    @Transactional
    public ActionResponse createAction(long projectId, CreateActionRequest request) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        ProjectAction lastOrdered = actionMapper.selectOne(Wrappers.<ProjectAction>lambdaQuery()
                .select(ProjectAction::getSortOrder)
                .eq(ProjectAction::getUserId, userId)
                .eq(ProjectAction::getProjectId, projectId)
                .orderByDesc(ProjectAction::getSortOrder)
                .last("LIMIT 1"));
        Integer maxOrder = lastOrdered == null ? null : lastOrdered.getSortOrder();
        ProjectAction action = new ProjectAction();
        action.setUserId(userId);
        action.setProjectId(projectId);
        action.setTitle(request.title().trim());
        action.setDetails(trimToNull(request.details()));
        action.setStatus(TODO);
        action.setSortOrder(maxOrder == null ? 0 : Math.addExact(maxOrder, 1));
        action.setCreatedAt(LocalDateTime.now());
        action.setUpdatedAt(action.getCreatedAt());
        actionMapper.insert(action);
        activityService.record(userId, projectId, ProjectActivityType.ACTION_CREATED,
                "{\"actionId\":" + action.getId() + "}");
        return toResponse(action);
    }

    /** 按排序值查询当前用户在指定项目下的行动。 */
    @Override
    public List<ActionResponse> listActions(long projectId) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        return actionMapper.selectList(Wrappers.<ProjectAction>lambdaQuery()
                        .eq(ProjectAction::getUserId, userId)
                        .eq(ProjectAction::getProjectId, projectId)
                        .orderByAsc(ProjectAction::getSortOrder)
                        .orderByAsc(ProjectAction::getId))
                .stream().map(this::toResponse).toList();
    }

    /** 在用户和项目范围内删除行动，不存在时按 404 处理。 */
    @Override
    @Transactional
    public void deleteAction(long projectId, long actionId) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        int rows = actionMapper.delete(Wrappers.<ProjectAction>lambdaQuery()
                .eq(ProjectAction::getId, actionId)
                .eq(ProjectAction::getUserId, userId)
                .eq(ProjectAction::getProjectId, projectId));
        if (rows == 0) throw new ResourceNotFoundException("行动不存在");
    }

    /** 在用户与项目范围内更新行动状态和完成时间。 */
    @Override
    @Transactional
    public ActionResponse updateActionStatus(long projectId, long actionId, String status) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        if (!TODO.equals(status) && !DONE.equals(status)) {
            throw new BusinessException(40000, "非法的行动状态");
        }
        ProjectAction action = requireOwnedAction(projectId, actionId, userId);
        if (status.equals(action.getStatus())) return toResponse(action);
        LocalDateTime now = LocalDateTime.now();
        int rows = actionMapper.update(null, Wrappers.<ProjectAction>lambdaUpdate()
                .eq(ProjectAction::getId, actionId)
                .eq(ProjectAction::getProjectId, projectId)
                .eq(ProjectAction::getUserId, userId)
                .set(ProjectAction::getStatus, status)
                .set(ProjectAction::getCompletedAt, DONE.equals(status) ? now : null)
                .set(ProjectAction::getUpdatedAt, now));
        if (rows == 0) throw new ResourceNotFoundException("行动不存在");
        action.setStatus(status);
        action.setCompletedAt(DONE.equals(status) ? now : null);
        action.setUpdatedAt(now);
        activityService.record(userId, projectId,
                DONE.equals(status) ? ProjectActivityType.ACTION_COMPLETED : ProjectActivityType.ACTION_REOPENED,
                "{\"actionId\":" + actionId + "}");
        return toResponse(action);
    }

    /** 验证行动 ID 集合完整且无重复后，事务性地更新其顺序。 */
    @Override
    @Transactional
    public List<ActionResponse> reorderActions(long projectId, List<Long> actionIds) {
        long userId = currentUser.currentUserId();
        requireOwnedProject(projectId, userId);
        if (actionIds == null || new HashSet<>(actionIds).size() != actionIds.size()
                || actionIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException(40000, "行动排序列表无效");
        }
        List<ProjectAction> actions = actionMapper.selectList(Wrappers.<ProjectAction>lambdaQuery()
                .eq(ProjectAction::getUserId, userId)
                .eq(ProjectAction::getProjectId, projectId));
        if (actions.size() != actionIds.size()
                || !new HashSet<>(actions.stream().map(ProjectAction::getId).toList()).equals(new HashSet<>(actionIds))) {
            throw new BusinessException(40000, "排序列表必须包含该项目的全部行动");
        }
        for (int i = 0; i < actionIds.size(); i++) {
            int rows = actionMapper.update(null, Wrappers.<ProjectAction>lambdaUpdate()
                    .eq(ProjectAction::getId, actionIds.get(i))
                    .eq(ProjectAction::getProjectId, projectId)
                    .eq(ProjectAction::getUserId, userId)
                    .set(ProjectAction::getSortOrder, i)
                    .set(ProjectAction::getUpdatedAt, LocalDateTime.now()));
            if (rows == 0) throw new ResourceNotFoundException("行动不存在");
        }
        return listActions(projectId);
    }

    /** 聚合最新检查点与行动；不存在检查点时不生成恢复摘要。 */
    @Override
    public RecoveryResponse recovery(long projectId) {
        long userId = currentUser.currentUserId();
        Project project = requireOwnedProject(projectId, userId);
        ProjectCheckpoint checkpoint = checkpointMapper.selectOne(Wrappers.<ProjectCheckpoint>lambdaQuery()
                .eq(ProjectCheckpoint::getUserId, userId)
                .eq(ProjectCheckpoint::getProjectId, projectId)
                .orderByDesc(ProjectCheckpoint::getCreatedAt)
                .orderByDesc(ProjectCheckpoint::getId)
                .last("LIMIT 1"));
        List<ActionResponse> actions = listActions(projectId);
        String summary = checkpoint == null ? null : buildSummary(checkpoint, actions);
        return new RecoveryResponse(toProjectResponse(project), checkpoint == null ? null : toResponse(checkpoint),
                actions, summary, decisionService.keyDecisions(projectId));
    }

    /** 返回归属指定用户的项目；不属于该用户时抛出 404。 */
    private Project requireOwnedProject(long projectId, long userId) {
        Project project = projectMapper.selectOne(Wrappers.<Project>lambdaQuery()
                .eq(Project::getId, projectId)
                .eq(Project::getUserId, userId));
        if (project == null) throw new ResourceNotFoundException("项目不存在");
        return project;
    }

    /** 返回归属指定用户和项目的行动；其他情况抛出 404。 */
    private ProjectAction requireOwnedAction(long projectId, long actionId, long userId) {
        ProjectAction action = actionMapper.selectOne(Wrappers.<ProjectAction>lambdaQuery()
                .eq(ProjectAction::getId, actionId)
                .eq(ProjectAction::getProjectId, projectId)
                .eq(ProjectAction::getUserId, userId));
        if (action == null) throw new ResourceNotFoundException("行动不存在");
        return action;
    }

    /** 仅使用已保存的检查点字段和首条待办生成确定性摘要。 */
    private String buildSummary(ProjectCheckpoint checkpoint, List<ActionResponse> actions) {
        StringBuilder result = new StringBuilder(checkpoint.getSummary());
        if (checkpoint.getCurrentFocus() != null && !checkpoint.getCurrentFocus().isBlank()) {
            result.append(" 当前关注：").append(checkpoint.getCurrentFocus());
        }
        if (checkpoint.getBlockers() != null && !checkpoint.getBlockers().isBlank()) {
            result.append(" 阻塞事项：").append(checkpoint.getBlockers());
        }
        actions.stream().filter(action -> TODO.equals(action.status())).findFirst()
                .ifPresent(action -> result.append(" 下一步：").append(action.title()));
        return result.toString();
    }

    /** 将项目实体映射为 API 响应。 */
    private ProjectResponse toProjectResponse(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getSummary(), project.getGoal(),
                project.getStatus(), project.getProgress(), project.getArchivedAt() != null,
                project.getCreatedAt(), project.getUpdatedAt());
    }

    /** 将检查点实体映射为 API 响应。 */
    private CheckpointResponse toResponse(ProjectCheckpoint checkpoint) {
        return new CheckpointResponse(checkpoint.getId(), checkpoint.getProjectId(), checkpoint.getSummary(),
                checkpoint.getCurrentFocus(), checkpoint.getBlockers(), checkpoint.getCreatedAt());
    }

    /** 将行动实体映射为 API 响应。 */
    private ActionResponse toResponse(ProjectAction action) {
        return new ActionResponse(action.getId(), action.getProjectId(), action.getTitle(), action.getDetails(),
                action.getStatus(), action.getSortOrder() == null ? 0 : action.getSortOrder(), action.getCreatedAt(),
                action.getUpdatedAt(), action.getCompletedAt());
    }

    /** 去除字符串两端空白，并将空内容统一为 {@code null}。 */
    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
