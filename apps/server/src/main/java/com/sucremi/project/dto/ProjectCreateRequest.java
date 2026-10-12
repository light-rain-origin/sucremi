package com.sucremi.project.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建项目请求。不接收 user_id —— 归属由服务端按当前身份注入。
 */
public record ProjectCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String summary,
        String goal,
        @Size(max = 20) String status,
        @Min(0) @Max(100) Integer progress
) {
}
