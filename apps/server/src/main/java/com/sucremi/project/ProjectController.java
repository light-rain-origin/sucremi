package com.sucremi.project;

import com.sucremi.common.api.ApiResponse;
import com.sucremi.common.api.PageResult;
import com.sucremi.project.dto.ProjectCreateRequest;
import com.sucremi.project.dto.ProjectQuery;
import com.sucremi.project.dto.ProjectResponse;
import com.sucremi.project.dto.ProjectUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目 REST 接口。统一前缀 /api/v1/projects。
 *
 * <p>身份与所有权在 {@link ProjectService} 层处理：Controller 只负责参数校验与响应包装。
 * 校验失败由全局异常处理返回 400；资源不存在/越权返回 404。</p>
 */
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<PageResult<ProjectResponse>> list(ProjectQuery query) {
        return ApiResponse.ok(projectService.list(query));
    }

    @PostMapping
    public ApiResponse<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest request) {
        return ApiResponse.ok(projectService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> get(@PathVariable long id) {
        return ApiResponse.ok(projectService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectResponse> update(@PathVariable long id,
                                               @Valid @RequestBody ProjectUpdateRequest request) {
        return ApiResponse.ok(projectService.update(id, request));
    }

    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archive(@PathVariable long id) {
        projectService.archive(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/unarchive")
    public ApiResponse<Void> unarchive(@PathVariable long id) {
        projectService.unarchive(id);
        return ApiResponse.ok();
    }
}
