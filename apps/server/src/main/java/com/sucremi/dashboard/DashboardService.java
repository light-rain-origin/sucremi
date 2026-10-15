package com.sucremi.dashboard;

import com.sucremi.activity.dto.ProjectActivityResponse;

import java.util.List;

/**
 * 首页概览服务。
 */
public interface DashboardService {

    DashboardSummary summary();

    ProjectFlowSummary projectFlow();

    List<ProjectActivityResponse> recentActivities(int size);
}
