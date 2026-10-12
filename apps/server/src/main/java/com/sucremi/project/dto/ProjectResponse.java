package com.sucremi.project.dto;

import java.time.LocalDateTime;

/**
 * 项目响应。
 */
public record ProjectResponse(
        Long id,
        String name,
        String summary,
        String goal,
        String status,
        Integer progress,
        boolean archived,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
