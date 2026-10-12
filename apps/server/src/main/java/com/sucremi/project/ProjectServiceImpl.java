package com.sucremi.project;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sucremi.common.api.PageResult;
import com.sucremi.common.exception.BusinessException;
import com.sucremi.common.exception.ResourceNotFoundException;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.dto.ProjectCreateRequest;
import com.sucremi.project.dto.ProjectQuery;
import com.sucremi.project.dto.ProjectResponse;
import com.sucremi.project.dto.ProjectUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目服务实现。所有读写都以 {@link CurrentUserResolver} 解析出的当前用户为范围：
 * 查询按 user_id 过滤，写操作在 WHERE 中带 user_id，命中 0 行即视为不存在（404）。
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper mapper;
    private final CurrentUserResolver currentUser;

    public ProjectServiceImpl(ProjectMapper mapper, CurrentUserResolver currentUser) {
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        validateStatus(request.status());
        Project p = new Project();
        p.setUserId(currentUser.currentUserId());
        p.setName(request.name());
        p.setSummary(request.summary());
        p.setGoal(request.goal());
        p.setStatus(request.status() == null ? ProjectStatus.ACTIVE.name() : request.status());
        p.setProgress(request.progress() == null ? 0 : request.progress());
        mapper.insert(p);
        return toResponse(requireOwned(p.getId()));
    }

    @Override
    public ProjectResponse getById(long id) {
        return toResponse(requireOwned(id));
    }

    @Override
    @Transactional
    public ProjectResponse update(long id, ProjectUpdateRequest request) {
        validateStatus(request.status());
        int rows = mapper.update(null, Wrappers.<Project>lambdaUpdate()
                .eq(Project::getId, id)
                .eq(Project::getUserId, currentUser.currentUserId())
                .set(Project::getName, request.name())
                .set(Project::getSummary, request.summary())
                .set(Project::getGoal, request.goal())
                .set(Project::getStatus, request.status())
                .set(Project::getProgress, request.progress()));
        if (rows == 0) {
            throw new ResourceNotFoundException("项目不存在");
        }
        return toResponse(requireOwned(id));
    }

    @Override
    public PageResult<ProjectResponse> list(ProjectQuery query) {
        LambdaQueryWrapper<Project> w = Wrappers.<Project>lambdaQuery()
                .eq(Project::getUserId, currentUser.currentUserId());
        if (query.isArchived()) {
            w.isNotNull(Project::getArchivedAt);
        } else {
            w.isNull(Project::getArchivedAt);
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            validateStatus(query.getStatus());
            w.eq(Project::getStatus, query.getStatus());
        }
        if (query.getQ() != null && !query.getQ().isBlank()) {
            String kw = query.getQ().trim();
            w.and(x -> x.like(Project::getName, kw).or().like(Project::getSummary, kw));
        }
        applySort(w, query.getSort());

        Page<Project> page = new Page<>(Math.max(query.getPage(), 1), clampSize(query.getSize()));
        Page<Project> result = mapper.selectPage(page, w);
        List<ProjectResponse> list = result.getRecords().stream().map(this::toResponse).toList();
        return new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional
    public void archive(long id) {
        setArchivedAt(id, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void unarchive(long id) {
        setArchivedAt(id, null);
    }

    private void setArchivedAt(long id, LocalDateTime value) {
        int rows = mapper.update(null, Wrappers.<Project>lambdaUpdate()
                .eq(Project::getId, id)
                .eq(Project::getUserId, currentUser.currentUserId())
                .set(Project::getArchivedAt, value));
        if (rows == 0) {
            throw new ResourceNotFoundException("项目不存在");
        }
    }

    private Project requireOwned(long id) {
        Project p = mapper.selectOne(Wrappers.<Project>lambdaQuery()
                .eq(Project::getId, id)
                .eq(Project::getUserId, currentUser.currentUserId()));
        if (p == null) {
            throw new ResourceNotFoundException("项目不存在");
        }
        return p;
    }

    private void validateStatus(String status) {
        if (status != null && !ProjectStatus.isValid(status)) {
            throw new BusinessException(40000, "非法的项目状态: " + status);
        }
    }

    private int clampSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private void applySort(LambdaQueryWrapper<Project> w, String sort) {
        String field = "updatedAt";
        boolean asc = false;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            field = parts[0].trim();
            asc = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim());
        }
        switch (field) {
            case "createdAt" -> w.orderBy(true, asc, Project::getCreatedAt);
            case "name" -> w.orderBy(true, asc, Project::getName);
            case "progress" -> w.orderBy(true, asc, Project::getProgress);
            case "updatedAt" -> w.orderBy(true, asc, Project::getUpdatedAt);
            default -> w.orderByDesc(Project::getUpdatedAt);
        }
    }

    private ProjectResponse toResponse(Project p) {
        return new ProjectResponse(p.getId(), p.getName(), p.getSummary(), p.getGoal(),
                p.getStatus(), p.getProgress(), p.getArchivedAt() != null,
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
