package com.sucremi.dashboard;

import com.sucremi.common.api.ApiResponse;
import com.sucremi.activity.dto.ProjectActivityResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页概览接口。
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummary> summary() {
        return ApiResponse.ok(dashboardService.summary());
    }

    @GetMapping("/project-flow")
    public ApiResponse<ProjectFlowSummary> projectFlow() {
        return ApiResponse.ok(dashboardService.projectFlow());
    }

    /** 返回当前用户的近期项目活动。 */
    @GetMapping("/recent-activities")
    public ApiResponse<List<ProjectActivityResponse>> recentActivities(
            @RequestParam(defaultValue = "5") int size) {
        return ApiResponse.ok(dashboardService.recentActivities(size));
    }
}
