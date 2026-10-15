package com.sucremi.activity;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sucremi.identity.CurrentUserResolver;
import com.sucremi.project.Project;
import com.sucremi.project.ProjectMapper;
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
import static org.mockito.Mockito.when;

/** 项目活动服务测试，覆盖分页、筛选、用户归属和事件持久化。 */
@ExtendWith(MockitoExtension.class)
class ProjectActivityServiceImplTest {
    @Mock ProjectActivityMapper activityMapper;
    @Mock ProjectMapper projectMapper;
    private final CurrentUserResolver currentUser = () -> 7L;
    private ProjectActivityServiceImpl service;

    /** 初始化 Mockito 环境所需的 MyBatis 实体元数据。 */
    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Project.class);
        TableInfoHelper.initTableInfo(assistant, ProjectActivity.class);
    }

    /** 为每个测试创建服务实例。 */
    @BeforeEach
    void setUp() {
        service = new ProjectActivityServiceImpl(activityMapper, projectMapper, currentUser);
    }

    /** 查询必须在 SQL 条件中包含用户、项目和可选类型，并对页长进行限制。 */
    @Test
    @SuppressWarnings("unchecked")
    void listScopesAndClampsPagination() {
        Project project = new Project();
        project.setId(4L);
        project.setUserId(7L);
        project.setName("时间线项目");
        when(projectMapper.selectOne(any())).thenReturn(project);
        Page<ProjectActivity> page = new Page<>(1, 100);
        page.setRecords(List.of());
        page.setTotal(0);
        ArgumentCaptor<Page<ProjectActivity>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<Wrapper<ProjectActivity>> queryCaptor = ArgumentCaptor.forClass(Wrapper.class);
        when(activityMapper.selectPage(pageCaptor.capture(), queryCaptor.capture())).thenReturn(page);

        var result = service.list(4L, 0, 1000, ProjectActivityType.ACTION_COMPLETED);

        assertEquals(1, pageCaptor.getValue().getCurrent());
        assertEquals(100, pageCaptor.getValue().getSize());
        assertEquals(0, result.total());
        String sql = queryCaptor.getValue().getSqlSegment();
        assertTrue(sql.contains("user_id"), sql);
        assertTrue(sql.contains("project_id"), sql);
        assertTrue(sql.contains("event_type"), sql);
    }

    /** 不属于当前用户的项目按不存在处理。 */
    @Test
    void listRejectsForeignProject() {
        when(projectMapper.selectOne(any())).thenReturn(null);
        assertThrows(com.sucremi.common.exception.ResourceNotFoundException.class,
                () -> service.list(4L, 1, 20, null));
    }

    /** 事件持久化时强制使用调用方可信身份，并写入版本化元数据。 */
    @Test
    void recordSetsOwnerAndMetadataVersion() {
        org.mockito.ArgumentCaptor<ProjectActivity> activity = org.mockito.ArgumentCaptor.forClass(ProjectActivity.class);
        org.mockito.Mockito.when(activityMapper.insert(any(ProjectActivity.class))).thenReturn(1);

        service.record(7L, 4L, ProjectActivityType.CHECKPOINT_CREATED, "{\"checkpointId\":9}");

        org.mockito.Mockito.verify(activityMapper).insert(activity.capture());
        assertEquals(7L, activity.getValue().getUserId());
        assertEquals(4L, activity.getValue().getProjectId());
        assertEquals(1, activity.getValue().getMetadataVersion());
        assertEquals("{\"checkpointId\":9}", activity.getValue().getMetadata());
    }

    /** 首页最近活动只读取当前用户的活动，并校验关联项目仍属于该用户。 */
    @Test
    @SuppressWarnings("unchecked")
    void recentActivitiesAreScopedToCurrentUser() {
        ProjectActivity item = new ProjectActivity();
        item.setId(2L);
        item.setUserId(7L);
        item.setProjectId(4L);
        item.setEventType(ProjectActivityType.CHECKPOINT_CREATED.name());
        item.setMetadataVersion(1);
        item.setMetadata("{}");
        when(activityMapper.selectList(any())).thenReturn(List.of(item));
        Project project = new Project();
        project.setId(4L);
        project.setUserId(7L);
        project.setName("项目");
        when(projectMapper.selectOne(any())).thenReturn(project);
        ArgumentCaptor<Wrapper<ProjectActivity>> query = ArgumentCaptor.forClass(Wrapper.class);

        var result = service.recent(5);

        org.mockito.Mockito.verify(activityMapper).selectList(query.capture());
        assertTrue(query.getValue().getSqlSegment().contains("user_id"));
        assertEquals(1, result.size());
        assertEquals("项目", result.get(0).projectName());
    }
}
