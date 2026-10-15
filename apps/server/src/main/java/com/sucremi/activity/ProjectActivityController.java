package com.sucremi.activity;

import com.sucremi.activity.dto.ProjectActivityResponse;
import com.sucremi.common.api.ApiResponse;
import com.sucremi.common.api.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 项目活动时间线查询接口。 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/activities")
public class ProjectActivityController {
    private final ProjectActivityService activityService;

    /** 创建项目活动接口。 */
    public ProjectActivityController(ProjectActivityService activityService) {
        this.activityService = activityService;
    }

    /** 分页读取项目活动，可按事件类型筛选。 */
    @GetMapping
    public ApiResponse<PageResult<ProjectActivityResponse>> list(@PathVariable long projectId,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ProjectActivityType type) {
        return ApiResponse.ok(activityService.list(projectId, page, size, type));
    }
}
