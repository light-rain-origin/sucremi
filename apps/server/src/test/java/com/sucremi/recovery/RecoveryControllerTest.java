package com.sucremi.recovery;

import com.sucremi.recovery.dto.CheckpointResponse;
import com.sucremi.recovery.dto.RecoveryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 恢复接口 Web 层切片测试，验证响应封装和项目级路由。
 */
@WebMvcTest(RecoveryController.class)
class RecoveryControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean RecoveryService recoveryService;

    /** 无检查点时，聚合响应明确返回空摘要和空行动列表。 */
    @Test
    void recoveryEndpointReturnsExplicitEmptyCheckpointState() throws Exception {
        when(recoveryService.recovery(4L)).thenReturn(new RecoveryResponse(null, null, List.of(), null));

        mockMvc.perform(get("/api/v1/projects/4/recovery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.latestCheckpoint").value(nullValue()))
                .andExpect(jsonPath("$.data.recoverySummary").value(nullValue()))
                .andExpect(jsonPath("$.data.actions").isEmpty());
    }

    /** 最新检查点读取接口应返回指定项目的检查点数据。 */
    @Test
    void latestCheckpointEndpointUsesProjectScopedRoute() throws Exception {
        when(recoveryService.latestCheckpoint(4L))
                .thenReturn(new CheckpointResponse(9L, 4L, "进展", null, null, null));

        mockMvc.perform(get("/api/v1/projects/4/checkpoints/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.projectId").value(4));
    }

    /** 删除检查点接口应委托服务层并返回统一成功响应。 */
    @Test
    void deleteCheckpointEndpointUsesProjectAndCheckpointIds() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/4/checkpoints/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(recoveryService).deleteCheckpoint(4L, 9L);
    }

    /** 删除行动接口应委托服务层并返回统一成功响应。 */
    @Test
    void deleteActionEndpointUsesProjectAndActionIds() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/4/actions/21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(recoveryService).deleteAction(4L, 21L);
    }
}
