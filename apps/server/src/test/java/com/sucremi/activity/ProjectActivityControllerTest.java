package com.sucremi.activity;

import com.sucremi.activity.dto.ProjectActivityResponse;
import com.sucremi.common.api.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 项目活动查询接口测试，验证分页响应和事件类型筛选参数。 */
@WebMvcTest(ProjectActivityController.class)
class ProjectActivityControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ProjectActivityService activityService;

    /** 项目活动分页接口应将类型筛选传递给服务并返回分页字段。 */
    @Test
    void listsPagedActivitiesWithTypeFilter() throws Exception {
        when(activityService.list(4L, 2, 10, ProjectActivityType.ACTION_COMPLETED))
                .thenReturn(new PageResult<>(List.of(new ProjectActivityResponse(
                        9L, 4L, "项目", "ACTION_COMPLETED", 1, "{}", null)), 11, 2, 10));

        mockMvc.perform(get("/api/v1/projects/4/activities?page=2&size=10&type=ACTION_COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(11))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.list[0].eventType").value("ACTION_COMPLETED"));
    }
}
