package com.sucremi.project.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 编辑项目请求（整体更新可编辑字段）。不接收 user_id / id。
 */
public record ProjectUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String summary,
        String goal,
        @NotBlank @Size(max = 20) String status,
        @NotNull @Min(0) @Max(100) Integer progress
) {
}
