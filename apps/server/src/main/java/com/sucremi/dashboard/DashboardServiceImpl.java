package com.sucremi.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.Project;
import com.sucremi.project.ProjectMapper;
import com.sucremi.project.ProjectService;
import com.sucremi.project.ProjectStatus;
import com.sucremi.project.dto.ProjectQuery;
import com.sucremi.project.dto.ProjectResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

/**
 * 首页概览实现。统计与最近项目均限定在当前用户范围内。
 * 最近项目复用 {@link ProjectService#list} 以保持过滤与映射一致。
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_SIZE = 5;

    private final ProjectMapper projectMapper;
    private final ProjectService projectService;
    private final CurrentUserResolver currentUser;

    public DashboardServiceImpl(ProjectMapper projectMapper, ProjectService projectService,
                                CurrentUserResolver currentUser) {
        this.projectMapper = projectMapper;
        this.projectService = projectService;
        this.currentUser = currentUser;
    }

    @Override
    public DashboardSummary summary() {
        long uid = currentUser.currentUserId();
        long total = count(uid, w -> w.isNull(Project::getArchivedAt));
        long active = count(uid, w -> w.isNull(Project::getArchivedAt).eq(Project::getStatus, ProjectStatus.ACTIVE.name()));
        long paused = count(uid, w -> w.isNull(Project::getArchivedAt).eq(Project::getStatus, ProjectStatus.PAUSED.name()));
        long done = count(uid, w -> w.isNull(Project::getArchivedAt).eq(Project::getStatus, ProjectStatus.DONE.name()));
        long archived = count(uid, w -> w.isNotNull(Project::getArchivedAt));

        ProjectQuery query = new ProjectQuery();
        query.setPage(1);
        query.setSize(RECENT_SIZE);
        query.setArchived(false);
        query.setSort("updatedAt,desc");
        List<ProjectResponse> recent = projectService.list(query).list();

        return new DashboardSummary(total, active, paused, done, archived, recent);
    }

    private long count(long uid, Consumer<LambdaQueryWrapper<Project>> extra) {
        LambdaQueryWrapper<Project> w = Wrappers.<Project>lambdaQuery().eq(Project::getUserId, uid);
        extra.accept(w);
        Long c = projectMapper.selectCount(w);
        return c == null ? 0L : c;
    }
}
