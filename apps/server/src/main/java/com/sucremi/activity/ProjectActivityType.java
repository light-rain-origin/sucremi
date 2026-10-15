package com.sucremi.activity;

/**
 * 项目活动的稳定事件类型。
 *
 * <p>枚举名称会持久化到数据库；新增类型时保持已有值不变。</p>
 */
public enum ProjectActivityType {
    PROJECT_CREATED,
    PROJECT_STATUS_CHANGED,
    PROJECT_ARCHIVED,
    PROJECT_UNARCHIVED,
    CHECKPOINT_CREATED,
    ACTION_CREATED,
    ACTION_COMPLETED,
    ACTION_REOPENED
}
