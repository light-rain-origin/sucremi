package com.sucremi.recovery.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 更新项目行动顺序的请求。
 *
 * @param actionIds 按新顺序排列的该项目全部行动 ID，不得重复或遗漏
 */
public record ReorderActionsRequest(@NotNull List<Long> actionIds) {
}
