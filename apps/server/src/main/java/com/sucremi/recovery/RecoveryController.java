package com.sucremi.recovery;

import com.sucremi.common.api.ApiResponse;
import com.sucremi.recovery.dto.ActionResponse;
import com.sucremi.recovery.dto.CheckpointResponse;
import com.sucremi.recovery.dto.CreateActionRequest;
import com.sucremi.recovery.dto.CreateCheckpointRequest;
import com.sucremi.recovery.dto.RecoveryResponse;
import com.sucremi.recovery.dto.ReorderActionsRequest;
import com.sucremi.recovery.dto.UpdateActionStatusRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目上下文恢复 REST 接口。
 *
 * <p>接口统一挂载在项目资源下，用户身份与数据归属由服务层处理。</p>
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class RecoveryController {
    private final RecoveryService recoveryService;

    public RecoveryController(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    /**
     * 保存一条新的开发检查点。
     *
     * @param projectId 项目 ID
     * @param request 检查点内容
     * @return 新建的检查点响应
     */
    @PostMapping("/checkpoints")
    public ApiResponse<CheckpointResponse> createCheckpoint(@PathVariable long projectId,
            @Valid @RequestBody CreateCheckpointRequest request) {
        return ApiResponse.ok(recoveryService.createCheckpoint(projectId, request));
    }

    /**
     * 按时间倒序读取项目的检查点历史。
     *
     * @param projectId 项目 ID
     * @return 检查点列表响应
     */
    @GetMapping("/checkpoints")
    public ApiResponse<List<CheckpointResponse>> checkpoints(@PathVariable long projectId) {
        return ApiResponse.ok(recoveryService.listCheckpoints(projectId));
    }

    /** 删除项目下的一条检查点记录。
     *
     * @param projectId 项目 ID
     * @param checkpointId 检查点 ID
     * @return 空成功响应
     */
    @DeleteMapping("/checkpoints/{checkpointId}")
    public ApiResponse<Void> deleteCheckpoint(@PathVariable long projectId,
            @PathVariable long checkpointId) {
        recoveryService.deleteCheckpoint(projectId, checkpointId);
        return ApiResponse.ok();
    }

    /**
     * 读取项目最新的一条检查点。
     *
     * @param projectId 项目 ID
     * @return 最新检查点响应
     */
    @GetMapping("/checkpoints/latest")
    public ApiResponse<CheckpointResponse> latestCheckpoint(@PathVariable long projectId) {
        return ApiResponse.ok(recoveryService.latestCheckpoint(projectId));
    }

    /**
     * 按项目内顺序读取下一步行动。
     *
     * @param projectId 项目 ID
     * @return 行动列表响应
     */
    @GetMapping("/actions")
    public ApiResponse<List<ActionResponse>> actions(@PathVariable long projectId) {
        return ApiResponse.ok(recoveryService.listActions(projectId));
    }

    /** 删除项目下的一条下一步行动。
     *
     * @param projectId 项目 ID
     * @param actionId 行动 ID
     * @return 空成功响应
     */
    @DeleteMapping("/actions/{actionId}")
    public ApiResponse<Void> deleteAction(@PathVariable long projectId, @PathVariable long actionId) {
        recoveryService.deleteAction(projectId, actionId);
        return ApiResponse.ok();
    }

    /**
     * 为项目创建一条待办行动。
     *
     * @param projectId 项目 ID
     * @param request 行动内容
     * @return 新建行动响应
     */
    @PostMapping("/actions")
    public ApiResponse<ActionResponse> createAction(@PathVariable long projectId,
            @Valid @RequestBody CreateActionRequest request) {
        return ApiResponse.ok(recoveryService.createAction(projectId, request));
    }

    /**
     * 更新行动状态，并记录或清除完成时间。
     *
     * @param projectId 项目 ID
     * @param actionId 行动 ID
     * @param request 新状态
     * @return 更新后的行动响应
     */
    @PatchMapping("/actions/{actionId}/status")
    public ApiResponse<ActionResponse> updateActionStatus(@PathVariable long projectId,
            @PathVariable long actionId, @Valid @RequestBody UpdateActionStatusRequest request) {
        return ApiResponse.ok(recoveryService.updateActionStatus(projectId, actionId, request.status()));
    }

    /**
     * 使用完整行动 ID 列表保存项目内的显示顺序。
     *
     * @param projectId 项目 ID
     * @param request 新的行动顺序
     * @return 排序后的行动列表响应
     */
    @PutMapping("/actions/order")
    public ApiResponse<List<ActionResponse>> reorderActions(@PathVariable long projectId,
            @Valid @RequestBody ReorderActionsRequest request) {
        return ApiResponse.ok(recoveryService.reorderActions(projectId, request.actionIds()));
    }

    /**
     * 聚合项目、最新检查点与行动，返回规则式恢复摘要。
     *
     * @param projectId 项目 ID
     * @return 上下文恢复聚合响应
     */
    @GetMapping("/recovery")
    public ApiResponse<RecoveryResponse> recovery(@PathVariable long projectId) {
        return ApiResponse.ok(recoveryService.recovery(projectId));
    }
}
