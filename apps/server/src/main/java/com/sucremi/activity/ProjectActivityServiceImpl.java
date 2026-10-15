package com.sucremi.activity;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sucremi.activity.dto.ProjectActivityResponse;
import com.sucremi.common.api.PageResult;
import com.sucremi.common.exception.ResourceNotFoundException;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.Project;
import com.sucremi.project.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 项目活动服务实现，所有查询都限制在当前用户数据范围内。
 */
@Service
public class ProjectActivityServiceImpl implements ProjectActivityService {
    private final ProjectActivityMapper activityMapper;
    private final ProjectMapper projectMapper;
    private final CurrentUserResolver currentUser;

    /** 创建项目活动服务。 */
    public ProjectActivityServiceImpl(ProjectActivityMapper activityMapper, ProjectMapper projectMapper,
                                      CurrentUserResolver currentUser) {
        this.activityMapper = activityMapper;
        this.projectMapper = projectMapper;
        this.currentUser = currentUser;
    }

    /** 在调用方事务中保存活动；不单独开启事务以保证业务与活动原子提交。 */
    @Override
    public void record(long userId, long projectId, ProjectActivityType type, String metadata) {
        ProjectActivity activity = new ProjectActivity();
        activity.setUserId(userId);
        activity.setProjectId(projectId);
        activity.setEventType(type.name());
        activity.setMetadataVersion(ProjectActivity.currentMetadataVersion());
        activity.setMetadata(metadata == null ? "{}" : metadata);
        activityMapper.insert(activity);
    }

    /** 按时间倒序分页查询活动，并验证项目归属。 */
    @Override
    public PageResult<ProjectActivityResponse> list(long projectId, int page, int size, ProjectActivityType type) {
        long userId = currentUser.currentUserId();
        Project project = ownedProject(projectId, userId);
        Page<ProjectActivity> result = activityMapper.selectPage(new Page<>(Math.max(page, 1), clamp(size)),
                Wrappers.<ProjectActivity>lambdaQuery().eq(ProjectActivity::getUserId, userId)
                        .eq(ProjectActivity::getProjectId, projectId)
                        .eq(type != null, ProjectActivity::getEventType, type == null ? null : type.name())
                        .orderByDesc(ProjectActivity::getCreatedAt).orderByDesc(ProjectActivity::getId));
        List<ProjectActivityResponse> rows = result.getRecords().stream()
                .map(activity -> response(activity, project.getName())).toList();
        return new PageResult<>(rows, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /** 返回当前用户最近活动；活动与项目均按当前用户范围读取。 */
    @Override
    public List<ProjectActivityResponse> recent(int size) {
        long userId = currentUser.currentUserId();
        List<ProjectActivity> activities = activityMapper.selectList(Wrappers.<ProjectActivity>lambdaQuery()
                .eq(ProjectActivity::getUserId, userId)
                .orderByDesc(ProjectActivity::getCreatedAt).orderByDesc(ProjectActivity::getId)
                .last("LIMIT " + clamp(size)));
        return activities.stream().map(activity -> {
            Project project = projectMapper.selectOne(Wrappers.<Project>lambdaQuery()
                    .eq(Project::getId, activity.getProjectId()).eq(Project::getUserId, userId));
            if (project == null) throw new ResourceNotFoundException("项目不存在");
            return response(activity, project.getName());
        }).toList();
    }

    private Project ownedProject(long projectId, long userId) {
        Project project = projectMapper.selectOne(Wrappers.<Project>lambdaQuery()
                .eq(Project::getId, projectId).eq(Project::getUserId, userId));
        if (project == null) throw new ResourceNotFoundException("项目不存在");
        return project;
    }

    private ProjectActivityResponse response(ProjectActivity activity, String projectName) {
        return new ProjectActivityResponse(activity.getId(), activity.getProjectId(), projectName,
                activity.getEventType(), activity.getMetadataVersion(), activity.getMetadata(), activity.getCreatedAt());
    }

    private int clamp(int size) {
        return Math.min(Math.max(size, 1), 100);
    }
}
