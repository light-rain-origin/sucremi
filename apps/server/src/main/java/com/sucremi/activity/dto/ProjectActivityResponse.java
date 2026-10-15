package com.sucremi.activity.dto;

import java.time.LocalDateTime;

/**
 * 项目活动查询响应。
 *
 * @param id 活动 ID
 * @param projectId 项目 ID
 * @param projectName 当前项目名称
 * @param eventType 稳定事件类型
 * @param metadataVersion 元数据版本
 * @param metadata 轻量事件元数据 JSON 字符串
 * @param createdAt 事件时间
 */
public record ProjectActivityResponse(long id, long projectId, String projectName, String eventType,
                                      int metadataVersion, String metadata, LocalDateTime createdAt) {
}
