package com.sucremi.decision;

import com.sucremi.common.api.ApiResponse;
import com.sucremi.common.api.PageResult;
import com.sucremi.decision.dto.DecisionRequest;
import com.sucremi.decision.dto.DecisionResponse;
import com.sucremi.decision.dto.DecisionRevisionResponse;
import com.sucremi.decision.dto.SupersedeDecisionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 项目决策记录 REST 接口；用户归属与业务校验由服务层执行。 */
@Validated
@RestController
@RequestMapping("/api/v1/projects/{projectId}/decisions")
public class DecisionController {
    private final DecisionService decisionService;

    /** 创建决策接口控制器。 */
    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    /** 分页筛选项目决策。 */
    @GetMapping
    public ApiResponse<PageResult<DecisionResponse>> list(@PathVariable long projectId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) DecisionStatus status,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        if (!"updatedAt".equals(sortBy) && !"createdAt".equals(sortBy)) sortBy = "updatedAt";
        if (!"asc".equalsIgnoreCase(direction) && !"desc".equalsIgnoreCase(direction)) direction = "desc";
        return ApiResponse.ok(decisionService.list(projectId, page, size, status, sortBy, direction));
    }

    /** 创建项目决策。 */
    @PostMapping
    public ApiResponse<DecisionResponse> create(@PathVariable long projectId,
            @Valid @RequestBody DecisionRequest request) {
        return ApiResponse.ok(decisionService.create(projectId, request));
    }

    /** 读取项目决策详情。 */
    @GetMapping("/{decisionId}")
    public ApiResponse<DecisionResponse> get(@PathVariable long projectId, @PathVariable long decisionId) {
        return ApiResponse.ok(decisionService.get(projectId, decisionId));
    }

    /** 更新项目决策，并检查请求中的乐观锁版本。 */
    @PatchMapping("/{decisionId}")
    public ApiResponse<DecisionResponse> update(@PathVariable long projectId, @PathVariable long decisionId,
            @Valid @RequestBody DecisionRequest request) {
        return ApiResponse.ok(decisionService.update(projectId, decisionId, request));
    }

    /** 分页读取决策历史版本。 */
    @GetMapping("/{decisionId}/revisions")
    public ApiResponse<PageResult<DecisionRevisionResponse>> revisions(@PathVariable long projectId,
            @PathVariable long decisionId, @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(decisionService.revisions(projectId, decisionId, page, size));
    }

    /** 原子创建替代决策，并结束旧决策的有效状态。 */
    @PostMapping("/{decisionId}/supersede")
    public ApiResponse<DecisionResponse> supersede(@PathVariable long projectId, @PathVariable long decisionId,
            @Valid @RequestBody SupersedeDecisionRequest request) {
        return ApiResponse.ok(decisionService.supersede(projectId, decisionId, request));
    }

    /** 软删除提议中的误创建决策。 */
    @DeleteMapping("/{decisionId}")
    public ApiResponse<Void> delete(@PathVariable long projectId, @PathVariable long decisionId) {
        decisionService.delete(projectId, decisionId);
        return ApiResponse.ok();
    }
}
