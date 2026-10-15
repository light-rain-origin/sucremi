package com.sucremi.project;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sucremi.common.api.PageResult;
import com.sucremi.common.exception.BusinessException;
import com.sucremi.common.exception.ResourceNotFoundException;
import com.sucremi.activity.ProjectActivityService;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.dto.ProjectCreateRequest;
import com.sucremi.project.dto.ProjectQuery;
import com.sucremi.project.dto.ProjectResponse;
import com.sucremi.project.dto.ProjectUpdateRequest;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProjectServiceImpl 单元测试（Mockito，不依赖数据库）。
 * 重点验证：所有权注入、跨用户/不存在时返回 404、状态校验、分页映射。
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    ProjectMapper mapper;

    @Mock
    ProjectActivityService activityService;

    // 固定当前用户 = 7
    final CurrentUserResolver currentUser = () -> 7L;

    ProjectServiceImpl service;

    /**
     * 纯 Mockito 环境下没有 MyBatis 上下文，lambda 列引用需要先初始化实体元数据，
     * 否则 {@code getSqlSegment()} 会抛 “can not find lambda cache”。
     */
    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), Project.class);
    }

    @BeforeEach
    void setUp() {
        service = new ProjectServiceImpl(mapper, currentUser, activityService);
    }

    private Project project(long id, long userId) {
        Project p = new Project();
        p.setId(id);
        p.setUserId(userId);
        p.setName("P");
        p.setStatus("ACTIVE");
        p.setProgress(0);
        return p;
    }

    @Test
    void createInjectsCurrentUserAsOwner() {
        when(mapper.insert(any(Project.class))).thenAnswer(inv -> {
            inv.getArgument(0, Project.class).setId(10L);
            return 1;
        });
        when(mapper.selectOne(any())).thenReturn(project(10L, 7L));

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        ProjectResponse resp = service.create(new ProjectCreateRequest("P", null, null, null, null));

        verify(mapper).insert(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertEquals("ACTIVE", captor.getValue().getStatus());
        assertEquals(0, captor.getValue().getProgress());
        assertEquals(10L, resp.id());
    }

    @Test
    void createRejectsInvalidStatus() {
        assertThrows(BusinessException.class,
                () -> service.create(new ProjectCreateRequest("P", null, null, "NOPE", null)));
    }

    @Test
    void getByIdThrows404WhenNotOwned() {
        when(mapper.selectOne(any())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void updateThrows404WhenNotOwned() {
        when(mapper.selectOne(any())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class,
                () -> service.update(99L, new ProjectUpdateRequest("N", null, null, "ACTIVE", 0)));
    }

    @Test
    void archiveThrows404WhenNotOwned() {
        when(mapper.selectOne(any())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.archive(99L));
    }

    @Test
    void unarchiveThrows404WhenNotOwned() {
        when(mapper.selectOne(any())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.unarchive(99L));
    }

    @Test
    void archiveSucceedsWhenOwned() {
        Project archived = project(1L, 7L);
        archived.setArchivedAt(java.time.LocalDateTime.now());
        when(mapper.selectOne(any())).thenReturn(project(1L, 7L), archived);
        when(mapper.update(isNull(), any())).thenReturn(1);

        service.archive(1L);
        service.unarchive(1L);

        verify(mapper, times(2)).update(isNull(), any());
        verify(activityService, times(2)).record(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("{}"));
    }

    /** 项目状态未变化时不新增时间线事件。 */
    @Test
    void unchangedProjectStatusDoesNotRecordActivity() {
        when(mapper.selectOne(any())).thenReturn(project(1L, 7L), project(1L, 7L));
        when(mapper.update(isNull(), any())).thenReturn(1);

        service.update(1L, new ProjectUpdateRequest("P", null, null, "ACTIVE", 0));

        verify(activityService, org.mockito.Mockito.never()).record(any(Long.class), any(Long.class), any(), any());
    }

    @Test
    void listMapsPageResult() {
        Page<Project> page = new Page<>(1, 20);
        page.setRecords(List.of(project(1L, 7L)));
        page.setTotal(1);
        when(mapper.selectPage(any(), any())).thenReturn(page);

        PageResult<ProjectResponse> result = service.list(new ProjectQuery());
        assertEquals(1, result.total());
        assertEquals(1, result.list().size());
        assertEquals(1L, result.list().get(0).id());
    }

    /** 分页参数来自前端，必须夹取到 [1, 100]，避免一次拉全表。 */
    @Test
    @SuppressWarnings("unchecked")
    void listClampsPageAndSize() {
        Page<Project> page = new Page<>(1, 20);
        page.setRecords(List.of());
        page.setTotal(0);
        ArgumentCaptor<Page<Project>> captor = ArgumentCaptor.forClass(Page.class);
        when(mapper.selectPage(captor.capture(), any())).thenReturn(page);

        ProjectQuery query = new ProjectQuery();
        query.setPage(0);
        query.setSize(1000);
        service.list(query);

        assertEquals(1, captor.getValue().getCurrent());
        assertEquals(100, captor.getValue().getSize());
    }

    @Test
    void listRejectsInvalidStatusFilter() {
        ProjectQuery query = new ProjectQuery();
        query.setStatus("NOPE");
        assertThrows(BusinessException.class, () -> service.list(query));
    }

    /** 列表查询必须带上 user_id 条件（越权读不出别人的项目），并按 archived 切换归档/未归档。 */
    @Test
    @SuppressWarnings("unchecked")
    void listScopesQueryToCurrentUserAndArchivedFlag() {
        Page<Project> page = new Page<>(1, 20);
        page.setRecords(List.of());
        page.setTotal(0);
        ArgumentCaptor<Wrapper<Project>> captor = ArgumentCaptor.forClass(Wrapper.class);
        when(mapper.selectPage(any(), captor.capture())).thenReturn(page);

        ProjectQuery query = new ProjectQuery();
        query.setArchived(true);
        service.list(query);

        String sql = captor.getValue().getSqlSegment();
        assertTrue(sql.contains("user_id"), "列表查询缺少 user_id 过滤: " + sql);
        assertTrue(sql.contains("archived_at"), "归档列表缺少 archived_at 过滤: " + sql);
    }
}
