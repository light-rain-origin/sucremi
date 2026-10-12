package com.sucremi.project;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sucremi.common.api.PageResult;
import com.sucremi.common.exception.BusinessException;
import com.sucremi.common.exception.ResourceNotFoundException;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.dto.ProjectCreateRequest;
import com.sucremi.project.dto.ProjectQuery;
import com.sucremi.project.dto.ProjectResponse;
import com.sucremi.project.dto.ProjectUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
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

    // 固定当前用户 = 7
    final CurrentUserResolver currentUser = () -> 7L;

    ProjectServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProjectServiceImpl(mapper, currentUser);
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
        when(mapper.update(isNull(), any())).thenReturn(0);
        assertThrows(ResourceNotFoundException.class,
                () -> service.update(99L, new ProjectUpdateRequest("N", null, null, "ACTIVE", 0)));
    }

    @Test
    void archiveThrows404WhenNotOwned() {
        when(mapper.update(isNull(), any())).thenReturn(0);
        assertThrows(ResourceNotFoundException.class, () -> service.archive(99L));
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
}
