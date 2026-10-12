package com.sucremi.project;

import com.sucremi.common.api.PageResult;
import com.sucremi.common.exception.ResourceNotFoundException;
import com.sucremi.project.dto.ProjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProjectController Web 层切片测试（@WebMvcTest + mock 的 Service，不依赖数据库）。
 * 覆盖：路由、参数校验(400)、不存在(404)、响应结构。
 */
@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProjectService projectService;

    private ProjectResponse sample() {
        return new ProjectResponse(1L, "P", "s", "g", "ACTIVE", 0, false, null, null);
    }

    @Test
    void createValidReturnsOk() throws Exception {
        when(projectService.create(any())).thenReturn(sample());
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"P\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void createBlankNameReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void getMissingReturns404() throws Exception {
        when(projectService.getById(99L)).thenThrow(new ResourceNotFoundException("项目不存在"));
        mockMvc.perform(get("/api/v1/projects/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40400));
    }

    @Test
    void listReturnsPage() throws Exception {
        when(projectService.list(any())).thenReturn(new PageResult<>(List.of(sample()), 1, 1, 20));
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(1));
    }

    @Test
    void updateInvalidReturns400() throws Exception {
        mockMvc.perform(put("/api/v1/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void archiveReturnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/projects/1/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(projectService).archive(eq(1L));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }
}
