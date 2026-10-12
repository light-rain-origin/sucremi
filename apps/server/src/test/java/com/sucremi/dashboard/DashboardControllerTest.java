package com.sucremi.dashboard;

import com.sucremi.project.dto.ProjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DashboardController Web 层切片测试（@WebMvcTest + mock 的 Service，不依赖数据库）。
 * 覆盖：路由、响应结构字段名（前端 types/dashboard.ts 依赖这些字段）。
 */
@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DashboardService dashboardService;

    @Test
    void summaryReturnsCountsAndRecentProjects() throws Exception {
        ProjectResponse recent = new ProjectResponse(9L, "最近项目", "s", null, "ACTIVE", 30, false, null, null);
        when(dashboardService.summary())
                .thenReturn(new DashboardSummary(3, 1, 1, 1, 2, List.of(recent)));

        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalProjects").value(3))
                .andExpect(jsonPath("$.data.activeProjects").value(1))
                .andExpect(jsonPath("$.data.pausedProjects").value(1))
                .andExpect(jsonPath("$.data.doneProjects").value(1))
                .andExpect(jsonPath("$.data.archivedProjects").value(2))
                .andExpect(jsonPath("$.data.recentProjects[0].id").value(9))
                .andExpect(jsonPath("$.data.recentProjects[0].name").value("最近项目"));
    }

    @Test
    void summaryWithoutProjectsReturnsEmptyRecent() throws Exception {
        when(dashboardService.summary()).thenReturn(new DashboardSummary(0, 0, 0, 0, 0, List.of()));

        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalProjects").value(0))
                .andExpect(jsonPath("$.data.recentProjects").isArray())
                .andExpect(jsonPath("$.data.recentProjects").isEmpty());
    }
}
