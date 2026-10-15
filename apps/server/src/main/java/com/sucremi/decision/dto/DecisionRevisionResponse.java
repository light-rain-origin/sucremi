package com.sucremi.decision.dto;

import com.sucremi.decision.DecisionStatus;

import java.time.LocalDateTime;
import java.util.List;

/** 决策历史版本响应，字段为当时完整的业务快照。 */
public record DecisionRevisionResponse(long id, long projectId, long decisionId, int revisionNumber,
        String title, String context, List<String> options, String decision, String rationale,
        String consequences, String revisitCondition, DecisionStatus status, Long supersededById,
        int version, String changeSummary, LocalDateTime createdAt) {
}
