package com.sucremi.dashboard;

import com.sucremi.common.api.PageResult;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.ProjectMapper;
import com.sucremi.project.ProjectService;
import com.sucremi.project.dto.ProjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * DashboardServiceImpl 单元测试（Mockito，不依赖数据库）。
 * 验证统计计数按序装配，并复用 ProjectService 取最近项目。
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectService projectService;

    final CurrentUserResolver currentUser = () -> 7L;

    DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DashboardServiceImpl(projectMapper, projectService, currentUser);
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
}
