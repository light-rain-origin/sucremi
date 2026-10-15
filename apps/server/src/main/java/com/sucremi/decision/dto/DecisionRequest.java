package com.sucremi.decision.dto;

import com.sucremi.decision.DecisionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 创建或编辑决策的结构化字段。 */
public record DecisionRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 20000) String context,
        @Size(max = 30) List<@NotBlank @Size(max = 1000) String> options,
        @Size(max = 20000) String decision,
        @Size(max = 20000) String rationale,
        @Size(max = 20000) String consequences,
        @Size(max = 20000) String revisitCondition,
        @NotNull DecisionStatus status,
        @Size(max = 500) String changeSummary,
        @Min(1) Integer expectedVersion
) {
}
