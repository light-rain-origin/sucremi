package com.sucremi.decision;

/** 项目决策生命周期状态；枚举名称会持久化到数据库。 */
public enum DecisionStatus {
    /** 尚未正式确定。 */
    PROPOSED,
    /** 当前有效的已采纳决策。 */
    ACCEPTED,
    /** 已由后续决策替代。 */
    SUPERSEDED
}
