package com.sucremi.recovery.dto;

import java.time.LocalDateTime;

/**
 * 项目检查点 API 响应。
 *
 * @param id 检查点 ID
 * @param projectId 所属项目 ID
 * @param summary 保存的开发状态摘要
 * @param currentFocus 保存时的当前关注事项
 * @param blockers 保存时的阻塞事项
 * @param createdAt 检查点保存时间
 */
public record CheckpointResponse(Long id, Long projectId, String summary, String currentFocus,
                                 String blockers, LocalDateTime createdAt) {
}
