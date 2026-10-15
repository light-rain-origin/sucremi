package com.sucremi.dashboard;

import com.sucremi.common.api.PageResult;
import com.sucremi.activity.ProjectActivityService;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.Project;
import com.sucremi.project.ProjectMapper;
import com.sucremi.project.ProjectService;
import com.sucremi.project.ProjectStatus;
import com.sucremi.project.dto.ProjectResponse;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DashboardServiceImpl 单元测试（Mockito，不依赖数据库）。
 * 验证统计计数按序装配，并复用 ProjectService 取最近项目。
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), Project.class);
    }

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectService projectService;

    @Mock
    ProjectActivityService activityService;

    final CurrentUserResolver currentUser = () -> 7L;

    DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DashboardServiceImpl(projectMapper, projectService, currentUser, activityService);
    }

    @Test
    void summaryAssemblesCountsAndRecent() {
        // selectCount 调用顺序：total, active, paused, done, archived
        when(projectMapper.selectCount(any())).thenReturn(10L, 6L, 2L, 2L, 3L);
        ProjectResponse p = new ProjectResponse(1L, "P", null, null, "ACTIVE", 0, false, null, null);
        when(projectService.list(any())).thenReturn(new PageResult<>(List.of(p), 10, 1, 5));

        DashboardSummary s = service.summary();

        assertEquals(10, s.totalProjects());
        assertEquals(6, s.activeProjects());
        assertEquals(2, s.pausedProjects());
        assertEquals(2, s.doneProjects());
        assertEquals(3, s.archivedProjects());
        assertEquals(1, s.recentProjects().size());
        assertEquals(1L, s.recentProjects().get(0).id());
    }

    @Test
    @SuppressWarnings("unchecked")
    void projectFlowReturnsCurrentUsersUnarchivedProjectsAndCounts() {
        Project active = project(1, ProjectStatus.ACTIVE, 40);
        Project paused = project(2, ProjectStatus.PAUSED, 25);
        Project done = project(3, ProjectStatus.DONE, 100);
        when(projectMapper.selectList(any())).thenReturn(List.of(active, paused, done));
        ArgumentCaptor<LambdaQueryWrapper<Project>> query = ArgumentCaptor.forClass(LambdaQueryWrapper.class);

        ProjectFlowSummary result = service.projectFlow();

        verify(projectMapper).selectList(query.capture());
        assertEquals(2, result.unfinishedCount());
        assertEquals(1, result.ongoingCount());
        assertEquals(1, result.completedCount());
        assertEquals(3, result.projects().size());
        assertEquals(25, result.projects().get(1).progress());
        assertTrue(query.getValue().getSqlSegment().contains("user_id"));
        assertTrue(query.getValue().getSqlSegment().contains("archived_at"));
        assertEquals(List.of(7L), List.copyOf(query.getValue().getParamNameValuePairs().values()));
    }

    private Project project(long id, ProjectStatus status, int progress) {
        Project project = new Project();
        project.setId(id);
        project.setUserId(7L);
        project.setName("项目 " + id);
        project.setStatus(status.name());
        project.setProgress(progress);
        return project;
    }
}
