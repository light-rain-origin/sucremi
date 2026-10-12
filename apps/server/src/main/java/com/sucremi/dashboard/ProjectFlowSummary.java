package com.sucremi.dashboard;

import com.sucremi.project.dto.ProjectResponse;

import java.util.List;

/** 当前用户未归档项目的状态汇总与项目进度列表。 */
public record ProjectFlowSummary(
        long unfinishedCount,
        long ongoingCount,
        long completedCount,
        List<ProjectResponse> projects
) {
}
