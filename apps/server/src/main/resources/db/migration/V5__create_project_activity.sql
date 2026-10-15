-- 项目活动时间线；仅保存事件类型和版本化的轻量元数据，不复制检查点或资料正文。
CREATE TABLE project_activity (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '活动主键',
    user_id           BIGINT UNSIGNED NOT NULL COMMENT '所属用户 → app_user.id',
    project_id        BIGINT UNSIGNED NOT NULL COMMENT '所属项目 → project.id',
    event_type        VARCHAR(40)     NOT NULL COMMENT '稳定活动类型枚举',
    metadata_version  SMALLINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '元数据结构版本',
    metadata          JSON            NOT NULL COMMENT '轻量事件元数据',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    PRIMARY KEY (id),
    KEY idx_activity_project_latest (user_id, project_id, created_at, id),
    KEY idx_activity_user_latest (user_id, created_at, id),
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_activity_project FOREIGN KEY (project_id) REFERENCES project (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT '项目活动时间线';
