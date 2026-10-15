package com.sucremi.decision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 创建替代决策并指定旧决策预期版本的请求。 */
public record SupersedeDecisionRequest(
        @NotNull @Min(1) Integer expectedVersion,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 20000) String context,
        @Size(max = 30) List<@NotBlank @Size(max = 1000) String> options,
        @NotBlank @Size(max = 20000) String decision,
        @NotBlank @Size(max = 20000) String rationale,
        @Size(max = 20000) String consequences,
        @Size(max = 20000) String revisitCondition,
        @Size(max = 500) String changeSummary
) {
}
