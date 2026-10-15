package com.sucremi.decision.dto;

import com.sucremi.decision.DecisionStatus;

import java.time.LocalDateTime;
import java.util.List;

/** 决策详情与列表响应。 */
public record DecisionResponse(long id, long projectId, String title, String context,
                               List<String> options, String decision, String rationale,
                               String consequences, String revisitCondition, DecisionStatus status,
                               Long supersededById, int version, LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
}
