package com.sucremi.recovery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建下一步行动的请求。
 *
 * @param title 必填行动标题，最多 200 个字符
 * @param details 可选行动说明，最多 1000 个字符
 */
public record CreateActionRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String details
) {
}
