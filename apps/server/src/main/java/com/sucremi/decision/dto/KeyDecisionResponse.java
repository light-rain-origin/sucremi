package com.sucremi.decision.dto;

/** 上下文恢复展示的轻量关键决策。 */
public record KeyDecisionResponse(long id, String title, String decision, String rationaleSummary) {
}
