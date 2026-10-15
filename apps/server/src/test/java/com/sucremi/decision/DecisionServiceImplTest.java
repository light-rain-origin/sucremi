package com.sucremi.decision;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import tools.jackson.databind.ObjectMapper;
import com.sucremi.activity.ProjectActivityService;
import com.sucremi.activity.ProjectActivityType;
import com.sucremi.common.exception.BusinessException;
import com.sucremi.common.exception.ResourceNotFoundException;
import com.sucremi.common.exception.ResourceConflictException;
import com.sucremi.decision.dto.DecisionRequest;
import com.sucremi.decision.dto.SupersedeDecisionRequest;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.Project;
import com.sucremi.project.ProjectMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 决策服务测试，覆盖归属校验、采纳字段校验及创建快照与事件。 */
@ExtendWith(MockitoExtension.class)
class DecisionServiceImplTest {
    @Mock ProjectMapper projectMapper;
    @Mock ProjectDecisionMapper decisionMapper;
    @Mock ProjectDecisionRevisionMapper revisionMapper;
    @Mock ProjectActivityService activityService;
    private final CurrentUserResolver currentUser = () -> 7L;
    private DecisionServiceImpl service;

    /** 初始化 MyBatis-Plus 测试实体元数据。 */
    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Project.class);
        TableInfoHelper.initTableInfo(assistant, ProjectDecision.class);
        TableInfoHelper.initTableInfo(assistant, ProjectDecisionRevision.class);
    }

    /** 每个测试使用独立服务实例。 */
    @BeforeEach
    void setUp() {
        service = new DecisionServiceImpl(projectMapper, decisionMapper, revisionMapper, currentUser,
                activityService, new ObjectMapper());
    }

    /** 创建提议中的决策会保存版本一，并将可信用户写入记录与活动。 */
    @Test
    void createsProposalWithInitialRevisionAndActivity() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(decisionMapper.insert(any(ProjectDecision.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, ProjectDecision.class).setId(11L);
            return 1;
        });
        ArgumentCaptor<ProjectDecisionRevision> revisions = ArgumentCaptor.forClass(ProjectDecisionRevision.class);

        var response = service.create(3L, new DecisionRequest("采用 Pinia", "需要共享状态", List.of("Vuex"),
                null, null, null, null, DecisionStatus.PROPOSED, null, null));

        verify(revisionMapper).insert(revisions.capture());
        assertEquals(11L, response.id());
        assertEquals(1, revisions.getValue().getRevisionNumber());
        assertEquals(7L, revisions.getValue().getUserId());
        verify(activityService).record(7L, 3L, ProjectActivityType.DECISION_CREATED,
                "{\"decisionId\":11,\"title\":\"采用 Pinia\",\"relatedDecisionId\":null}");
    }

    /** 非当前用户项目下禁止创建决策。 */
    @Test
    void rejectsProjectOutsideCurrentUserScope() {
        when(projectMapper.selectOne(any())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.create(3L,
                new DecisionRequest("标题", "背景", List.of(), null, null, null, null, DecisionStatus.PROPOSED, null, null)));
        verify(decisionMapper, never()).insert(any(ProjectDecision.class));
    }

    /** 创建已采纳决策时必须提供最终选择和理由。 */
    @Test
    void acceptedDecisionRequiresChoiceAndRationale() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        assertThrows(BusinessException.class, () -> service.create(3L,
                new DecisionRequest("标题", "背景", List.of(), null, "理由", null, null, DecisionStatus.ACCEPTED, null, null)));
        verify(decisionMapper, never()).insert(any(ProjectDecision.class));
    }

    /** 实质性编辑增加快照版本并写入更新事件。 */
    @Test
    void updateCreatesNextSnapshotVersion() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(decisionMapper.selectOne(any())).thenReturn(decision(11L, 3L, 7L, DecisionStatus.PROPOSED, 1));
        when(decisionMapper.update(isNull(), any())).thenReturn(1);
        ArgumentCaptor<ProjectDecisionRevision> revisions = ArgumentCaptor.forClass(ProjectDecisionRevision.class);

        var response = service.update(3L, 11L, new DecisionRequest("采用 Pinia", "需要共享状态", List.of("Vuex"),
                null, null, null, null, DecisionStatus.PROPOSED, "补充背景", 1));

        verify(revisionMapper).insert(revisions.capture());
        assertEquals(2, response.version());
        assertEquals(2, revisions.getValue().getRevisionNumber());
        org.junit.jupiter.api.Assertions.assertTrue(revisions.getValue().getSnapshotJson().contains("采用 Pinia"));
        verify(activityService).record(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.eq(ProjectActivityType.DECISION_UPDATED), any());
    }

    /** 过期版本在写入前返回冲突，不写快照或活动。 */
    @Test
    void staleVersionDoesNotWriteRevisionOrActivity() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(decisionMapper.selectOne(any())).thenReturn(decision(11L, 3L, 7L, DecisionStatus.PROPOSED, 2));

        assertThrows(ResourceConflictException.class, () -> service.update(3L, 11L,
                new DecisionRequest("标题", "背景", List.of(), null, null, null, null,
                        DecisionStatus.PROPOSED, null, 1)));

        verify(decisionMapper, never()).update(isNull(), any());
        verify(revisionMapper, never()).insert(any(ProjectDecisionRevision.class));
        verify(activityService, never()).record(any(Long.class), any(Long.class), any(), any());
    }

    /** 替代会创建已采纳新版本、更新旧记录并为两条决策保存历史和活动。 */
    @Test
    void supersedeCreatesLinkedAcceptedDecisionAtomically() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(decisionMapper.selectOne(any())).thenReturn(decision(11L, 3L, 7L, DecisionStatus.ACCEPTED, 2));
        when(decisionMapper.insert(any(ProjectDecision.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, ProjectDecision.class).setId(12L);
            return 1;
        });
        when(decisionMapper.update(isNull(), any())).thenReturn(1);
        ArgumentCaptor<ProjectDecisionRevision> revisions = ArgumentCaptor.forClass(ProjectDecisionRevision.class);

        var response = service.supersede(3L, 11L, new SupersedeDecisionRequest(2, "改用 PostgreSQL", "查询需求变化",
                List.of("MySQL", "PostgreSQL"), "PostgreSQL", "支持目标查询", null, null, "数据查询扩展"));

        verify(revisionMapper, times(2)).insert(revisions.capture());
        assertEquals(12L, response.id());
        assertEquals(DecisionStatus.ACCEPTED, response.status());
        assertEquals(2, revisions.getAllValues().size());
        assertTrue(revisions.getAllValues().stream().anyMatch(r -> r.getSnapshotJson().contains("SUPERSEDED")));
        assertTrue(revisions.getAllValues().stream().anyMatch(r -> r.getSnapshotJson().contains("ACCEPTED")));
        verify(activityService).record(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.eq(ProjectActivityType.DECISION_SUPERSEDED), any());
        verify(activityService).record(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.eq(ProjectActivityType.DECISION_ACCEPTED), any());
    }

    private Project project(long id, long userId) {
        Project project = new Project();
        project.setId(id);
        project.setUserId(userId);
        project.setName("测试项目");
        return project;
    }

    private ProjectDecision decision(long id, long projectId, long userId, DecisionStatus status, int version) {
        ProjectDecision item = new ProjectDecision();
        item.setId(id); item.setProjectId(projectId); item.setUserId(userId); item.setTitle("原决策");
        item.setContext("原背景"); item.setOptionsJson("[]"); item.setStatus(status.name()); item.setVersion(version);
        return item;
    }
}
