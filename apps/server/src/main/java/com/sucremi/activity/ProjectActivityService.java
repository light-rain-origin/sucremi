package com.sucremi.activity;

import com.sucremi.common.api.PageResult;
import com.sucremi.activity.dto.ProjectActivityResponse;

import java.util.List;

/** 项目活动记录与查询服务。 */
public interface ProjectActivityService {
    /** 在当前业务事务内记录一条轻量事件。 */
    void record(long userId, long projectId, ProjectActivityType type, String metadata);

    /** 查询当前用户指定项目的分页活动。 */
    PageResult<ProjectActivityResponse> list(long projectId, int page, int size, ProjectActivityType type);

    /** 查询当前用户跨项目的最近活动。 */
    List<ProjectActivityResponse> recent(int size);
}
