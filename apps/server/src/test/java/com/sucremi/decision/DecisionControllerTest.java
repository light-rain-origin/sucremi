package com.sucremi.decision;

import com.sucremi.common.api.PageResult;
import com.sucremi.decision.dto.DecisionResponse;
import com.sucremi.decision.dto.DecisionRevisionResponse;
import com.sucremi.common.exception.ResourceConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 决策接口切片测试，覆盖项目级路由、分页数据与历史版本响应。 */
@WebMvcTest(DecisionController.class)
class DecisionControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean DecisionService decisionService;

    /** 决策列表返回统一分页结构。 */
    @Test
    void listsProjectDecisions() throws Exception {
        DecisionResponse decision = new DecisionResponse(8L, 4L, "采用 Pinia", "状态共享问题", List.of("Vuex"),
                "Pinia", "更贴合 Vue 生态", null, null, DecisionStatus.ACCEPTED, null, 1, null, null);
        when(decisionService.list(4L, 1, 20, null, "updatedAt", "desc"))
                .thenReturn(new PageResult<>(List.of(decision), 1, 1, 20));

        mockMvc.perform(get("/api/v1/projects/4/decisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].status").value("ACCEPTED"));
    }

    /** 历史版本接口按决策资源路径返回分页快照。 */
    @Test
    void listsDecisionRevisions() throws Exception {
        DecisionRevisionResponse revision = new DecisionRevisionResponse(9L, 4L, 8L, 1, "采用 Pinia", "状态共享问题",
                List.of("Vuex"), "Pinia", "更贴合 Vue 生态", null, null, DecisionStatus.ACCEPTED, null, 1, null, null);
        when(decisionService.revisions(4L, 8L, 1, 20)).thenReturn(new PageResult<>(List.of(revision), 1, 1, 20));

        mockMvc.perform(get("/api/v1/projects/4/decisions/8/revisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].revisionNumber").value(1));
    }

    /** 乐观锁冲突通过统一响应返回 HTTP 409。 */
    @Test
    void returnsConflictForStaleDecisionVersion() throws Exception {
        when(decisionService.update(org.mockito.ArgumentMatchers.eq(4L), org.mockito.ArgumentMatchers.eq(8L),
                org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ResourceConflictException("决策已被其他操作更新，请刷新后重试"));

        mockMvc.perform(patch("/api/v1/projects/4/decisions/8").contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"采用 Pinia","context":"共享状态","options":[],"status":"PROPOSED","expectedVersion":1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40900));
    }
}
