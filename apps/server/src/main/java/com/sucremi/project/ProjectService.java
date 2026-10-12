package com.sucremi.project;

import com.sucremi.common.api.PageResult;
import com.sucremi.project.dto.ProjectCreateRequest;
import com.sucremi.project.dto.ProjectQuery;
import com.sucremi.project.dto.ProjectResponse;
import com.sucremi.project.dto.ProjectUpdateRequest;

/**
 * 项目业务服务。所有操作都在当前用户范围内执行；跨用户访问按不存在处理（404）。
 */
public interface ProjectService {

    ProjectResponse create(ProjectCreateRequest request);

    ProjectResponse getById(long id);

    ProjectResponse update(long id, ProjectUpdateRequest request);

    PageResult<ProjectResponse> list(ProjectQuery query);

    void archive(long id);

    void unarchive(long id);
}
