package com.sucremi.recovery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建开发检查点的请求。
 *
 * @param summary 必填的开发状态摘要，最多 10000 个字符
 * @param currentFocus 可选的当前关注事项，最多 255 个字符
 * @param blockers 可选的阻塞事项，最多 10000 个字符
 */
public record CreateCheckpointRequest(
        @NotBlank @Size(max = 10000) String summary,
        @Size(max = 255) String currentFocus,
        @Size(max = 10000) String blockers
) {
}
