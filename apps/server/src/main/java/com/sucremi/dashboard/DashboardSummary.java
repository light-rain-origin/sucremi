package com.sucremi.dashboard;

import com.sucremi.project.dto.ProjectResponse;

import java.util.List;

/**
 * 首页概览数据：统计计数 + 最近更新的项目。
 * 计数均为当前用户、未归档项目（archivedProjects 除外）。
 */
public record DashboardSummary(
        long totalProjects,
        long activeProjects,
        long pausedProjects,
        long doneProjects,
        long archivedProjects,
        List<ProjectResponse> recentProjects
) {
}
