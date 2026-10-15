package com.sucremi.recovery.dto;

import com.sucremi.project.dto.ProjectResponse;

import java.util.List;

/**
 * 项目上下文恢复聚合响应。
 *
 * @param project 项目基本信息
 * @param latestCheckpoint 最新检查点；从未保存时为空
 * @param actions 项目行动列表
 * @param recoverySummary 基于检查点和行动生成的规则式摘要；无检查点时为空
 */
public record RecoveryResponse(ProjectResponse project, CheckpointResponse latestCheckpoint,
                               List<ActionResponse> actions, String recoverySummary) {
}
