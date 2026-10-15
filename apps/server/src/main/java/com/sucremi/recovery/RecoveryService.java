package com.sucremi.recovery;

import com.sucremi.recovery.dto.ActionResponse;
import com.sucremi.recovery.dto.CheckpointResponse;
import com.sucremi.recovery.dto.CreateActionRequest;
import com.sucremi.recovery.dto.CreateCheckpointRequest;
import com.sucremi.recovery.dto.RecoveryResponse;

import java.util.List;

/**
 * 项目上下文恢复业务服务。
 *
 * <p>所有操作都从当前身份解析用户 ID，并校验项目及行动的归属。</p>
 */
public interface RecoveryService {
    /**
     * 创建检查点；检查点内容作为新的历史快照保存。
     *
     * @param projectId 项目 ID
     * @param request 检查点内容
     * @return 新建的检查点
     */
    CheckpointResponse createCheckpoint(long projectId, CreateCheckpointRequest request);

    /**
     * 查询项目全部检查点，最新记录排在前面。
     *
     * @param projectId 项目 ID
     * @return 按创建时间倒序排列的检查点
     */
    List<CheckpointResponse> listCheckpoints(long projectId);

    /**
     * 查询项目最新检查点；无记录或项目不可访问时返回 404。
     *
     * @param projectId 项目 ID
     * @return 最新检查点
     */
    CheckpointResponse latestCheckpoint(long projectId);

    /**
     * 创建项目行动，新行动默认处于待办状态并排在末尾。
     *
     * @param projectId 项目 ID
     * @param request 行动内容
     * @return 新建的行动
     */
    ActionResponse createAction(long projectId, CreateActionRequest request);

    /**
     * 按保存顺序查询项目行动。
     *
     * @param projectId 项目 ID
     * @return 排序后的行动列表
     */
    List<ActionResponse> listActions(long projectId);

    /**
     * 更新行动状态；仅允许 TODO 和 DONE。
     *
     * @param projectId 项目 ID
     * @param actionId 行动 ID
     * @param status 新状态
     * @return 更新后的行动
     */
    ActionResponse updateActionStatus(long projectId, long actionId, String status);

    /**
     * 按完整的行动 ID 顺序重排项目行动。
     *
     * @param projectId 项目 ID
     * @param actionIds 按目标顺序排列的全部行动 ID
     * @return 更新顺序后的行动列表
     */
    List<ActionResponse> reorderActions(long projectId, List<Long> actionIds);

    /**
     * 聚合项目恢复信息；无检查点时恢复摘要为空。
     *
     * @param projectId 项目 ID
     * @return 项目、检查点、行动和规则式摘要
     */
    RecoveryResponse recovery(long projectId);
}
