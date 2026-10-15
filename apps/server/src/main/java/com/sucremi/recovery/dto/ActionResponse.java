package com.sucremi.recovery.dto;

import java.time.LocalDateTime;

/**
 * 项目行动 API 响应。
 *
 * @param id 行动 ID
 * @param projectId 所属项目 ID
 * @param title 行动标题
 * @param details 行动说明
 * @param status 行动状态，取值为 TODO 或 DONE
 * @param sortOrder 项目内显示顺序
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @param completedAt 完成时间；未完成时为空
 */
public record ActionResponse(Long id, Long projectId, String title, String details, String status,
                             int sortOrder, LocalDateTime createdAt, LocalDateTime updatedAt,
                             LocalDateTime completedAt) {
}
