package com.sucremi.recovery.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新下一步行动状态的请求。
 *
 * @param status 新状态，仅支持 TODO 和 DONE
 */
public record UpdateActionStatusRequest(@NotBlank String status) {
}
