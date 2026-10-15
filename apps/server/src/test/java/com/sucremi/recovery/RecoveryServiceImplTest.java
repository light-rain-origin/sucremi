package com.sucremi.recovery;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sucremi.common.exception.ResourceNotFoundException;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.Project;
import com.sucremi.project.ProjectMapper;
import com.sucremi.recovery.dto.CreateActionRequest;
import com.sucremi.recovery.dto.CreateCheckpointRequest;
import com.sucremi.recovery.dto.RecoveryResponse;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * 恢复服务单元测试，覆盖数据归属、状态更新、排序及空检查点语义。
 */
@ExtendWith(MockitoExtension.class)
class RecoveryServiceImplTest {
    @Mock ProjectMapper projectMapper;
    @Mock ProjectCheckpointMapper checkpointMapper;
    @Mock ProjectActionMapper actionMapper;

    private final CurrentUserResolver currentUser = () -> 7L;
    private RecoveryServiceImpl service;

    /** 初始化纯 Mockito 测试所需的 MyBatis-Plus 实体元数据。 */
    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Project.class);
        TableInfoHelper.initTableInfo(assistant, ProjectCheckpoint.class);
        TableInfoHelper.initTableInfo(assistant, ProjectAction.class);
    }

    /** 为每个测试创建新的服务实例。 */
    @BeforeEach
    void setUp() {
        service = new RecoveryServiceImpl(projectMapper, checkpointMapper, actionMapper, currentUser);
    }

    /** 检查点创建时应注入可信用户和项目归属，并清理摘要首尾空白。 */
    @Test
    void checkpointCreationAssignsCurrentUserAndProject() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(checkpointMapper.insert(any(ProjectCheckpoint.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, ProjectCheckpoint.class).setId(11L);
            return 1;
        });
        ArgumentCaptor<ProjectCheckpoint> captor = ArgumentCaptor.forClass(ProjectCheckpoint.class);

        var response = service.createCheckpoint(3L, new CreateCheckpointRequest("  当前进展  ", "联调", null));

        verify(checkpointMapper).insert(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertEquals(3L, captor.getValue().getProjectId());
        assertEquals("当前进展", captor.getValue().getSummary());
        assertEquals(11L, response.id());
    }

    /** 检查点列表查询必须同时按用户和项目过滤。 */
    @Test
    void checkpointListIsScopedByUserAndProject() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        ArgumentCaptor<Wrapper<ProjectCheckpoint>> captor = ArgumentCaptor.forClass(Wrapper.class);
        when(checkpointMapper.selectList(captor.capture())).thenReturn(List.of());

        service.listCheckpoints(3L);

        String sql = captor.getValue().getSqlSegment();
        assertTrue(sql.contains("user_id"), sql);
        assertTrue(sql.contains("project_id"), sql);
    }

    /** 检查点删除必须同时匹配用户、项目和检查点 ID。 */
    @Test
    void checkpointDeletionIsScopedByUserAndProject() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(checkpointMapper.delete(any())).thenReturn(1);
        ArgumentCaptor<Wrapper<ProjectCheckpoint>> captor = ArgumentCaptor.forClass(Wrapper.class);

        service.deleteCheckpoint(3L, 11L);

        verify(checkpointMapper).delete(captor.capture());
        String sql = captor.getValue().getSqlSegment();
        assertTrue(sql.contains("user_id"), sql);
        assertTrue(sql.contains("project_id"), sql);
        assertTrue(sql.contains("id"), sql);
    }

    /** 删除不存在或不属于当前用户的检查点应返回 404。 */
    @Test
    void missingOwnedCheckpointCannotBeDeleted() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(checkpointMapper.delete(any())).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteCheckpoint(3L, 99L));
    }

    /** 不属于当前用户的项目不可创建检查点。 */
    @Test
    void projectOwnedByAnotherUserIsNotAccessible() {
        when(projectMapper.selectOne(any())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class,
                () -> service.createCheckpoint(3L, new CreateCheckpointRequest("进度", null, null)));
    }

    /** 新行动应继承当前用户和项目，并从待办状态开始。 */
    @Test
    void actionCreationAssignsOwnerAndStartsTodo() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(actionMapper.insert(any(ProjectAction.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, ProjectAction.class).setId(21L);
            return 1;
        });
        ArgumentCaptor<ProjectAction> captor = ArgumentCaptor.forClass(ProjectAction.class);

        var response = service.createAction(3L, new CreateActionRequest("补接口", null));

        verify(actionMapper).insert(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertEquals(3L, captor.getValue().getProjectId());
        assertEquals("TODO", response.status());
    }

    /** 完成行动时应保存完成状态、完成时间和用户范围条件。 */
    @Test
    void completingActionPersistsDoneStatusWithOwnerScopedUpdate() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(actionMapper.selectOne(any())).thenReturn(action(21L, 3L, 7L));
        when(actionMapper.update(isNull(), any())).thenReturn(1);
        ArgumentCaptor<Wrapper<ProjectAction>> update = ArgumentCaptor.forClass(Wrapper.class);

        var response = service.updateActionStatus(3L, 21L, "DONE");

        verify(actionMapper).update(isNull(), update.capture());
        String where = update.getValue().getSqlSegment();
        assertTrue(where.contains("user_id"), where);
        assertTrue(where.contains("project_id"), where);
        assertTrue(where.contains("id"), where);
        assertEquals("DONE", response.status());
        assertTrue(response.completedAt() != null);
    }

    /** 不能更新其他用户或其他项目的行动。 */
    @Test
    void actionFromAnotherOwnerIsNotUpdated() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(actionMapper.selectOne(any())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateActionStatus(3L, 21L, "DONE"));
    }

    /** 行动删除必须在当前用户和项目范围内执行。 */
    @Test
    void actionDeletionIsScopedByUserAndProject() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(actionMapper.delete(any())).thenReturn(1);
        ArgumentCaptor<Wrapper<ProjectAction>> captor = ArgumentCaptor.forClass(Wrapper.class);

        service.deleteAction(3L, 21L);

        verify(actionMapper).delete(captor.capture());
        String sql = captor.getValue().getSqlSegment();
        assertTrue(sql.contains("user_id"), sql);
        assertTrue(sql.contains("project_id"), sql);
        assertTrue(sql.contains("id"), sql);
    }

    /** 删除不存在或不属于当前用户的行动应返回 404。 */
    @Test
    void missingOwnedActionCannotBeDeleted() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(actionMapper.delete(any())).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteAction(3L, 99L));
    }

    /** 重排完整行动集合时，每次更新都必须受用户和项目范围约束。 */
    @Test
    void reorderRequiresWholeProjectActionSetAndUpdatesWithOwnerScope() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        ProjectAction first = action(21L, 3L, 7L);
        ProjectAction second = action(22L, 3L, 7L);
        when(actionMapper.selectList(any())).thenReturn(List.of(first, second));
        when(actionMapper.update(isNull(), any())).thenReturn(1);
        ArgumentCaptor<Wrapper<ProjectAction>> updates = ArgumentCaptor.forClass(Wrapper.class);

        service.reorderActions(3L, List.of(22L, 21L));

        verify(actionMapper, times(2)).update(isNull(), updates.capture());
        for (Wrapper<ProjectAction> update : updates.getAllValues()) {
            String where = update.getSqlSegment();
            assertTrue(where.contains("user_id"), where);
            assertTrue(where.contains("project_id"), where);
            assertTrue(where.contains("id"), where);
        }
    }

    /** 重排请求包含不属于项目的行动时应整体拒绝。 */
    @Test
    void reorderRejectsActionIdsOutsideOwnedProject() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(actionMapper.selectList(any())).thenReturn(List.of(action(21L, 3L, 7L)));

        org.junit.jupiter.api.Assertions.assertThrows(com.sucremi.common.exception.BusinessException.class,
                () -> service.reorderActions(3L, List.of(21L, 999L)));
    }

    /** 没有检查点时返回空摘要，不拼造恢复内容。 */
    @Test
    void recoveryWithoutCheckpointDoesNotInventSummary() {
        when(projectMapper.selectOne(any())).thenReturn(project(3L, 7L));
        when(checkpointMapper.selectOne(any())).thenReturn(null);
        when(actionMapper.selectList(any())).thenReturn(List.of());

        RecoveryResponse response = service.recovery(3L);

        assertNull(response.latestCheckpoint());
        assertNull(response.recoverySummary());
        assertTrue(response.actions().isEmpty());
    }

    /** 构造测试用项目实体。 */
    private Project project(long id, long userId) {
        Project project = new Project();
        project.setId(id);
        project.setUserId(userId);
        project.setName("项目");
        project.setStatus("ACTIVE");
        project.setProgress(0);
        return project;
    }

    /** 构造测试用行动实体。 */
    private ProjectAction action(long id, long projectId, long userId) {
        ProjectAction action = new ProjectAction();
        action.setId(id);
        action.setProjectId(projectId);
        action.setUserId(userId);
        action.setTitle("下一步");
        action.setStatus("TODO");
        action.setSortOrder(0);
        return action;
    }
}
