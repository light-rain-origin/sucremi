-- 用户项目上下文恢复：检查点快照与下一步行动。
CREATE TABLE project_checkpoint (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '检查点主键',
    user_id      BIGINT UNSIGNED NOT NULL COMMENT '所属用户 → app_user.id',
    project_id   BIGINT UNSIGNED NOT NULL COMMENT '所属项目 → project.id',
    summary      TEXT            NOT NULL COMMENT '本次开发状态摘要',
    current_focus VARCHAR(255)            DEFAULT NULL COMMENT '当前关注事项',
    blockers     TEXT                     DEFAULT NULL COMMENT '阻塞事项',
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '保存时间',
    PRIMARY KEY (id),
    KEY idx_checkpoint_project_latest (user_id, project_id, created_at, id),
    CONSTRAINT fk_checkpoint_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_checkpoint_project FOREIGN KEY (project_id) REFERENCES project (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT '项目开发检查点';

CREATE TABLE project_action (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '行动主键',
    user_id      BIGINT UNSIGNED NOT NULL COMMENT '所属用户 → app_user.id',
    project_id   BIGINT UNSIGNED NOT NULL COMMENT '所属项目 → project.id',
    title        VARCHAR(200)    NOT NULL COMMENT '下一步行动',
    details      VARCHAR(1000)           DEFAULT NULL COMMENT '行动说明',
    status       VARCHAR(20)     NOT NULL DEFAULT 'TODO' COMMENT '状态：TODO/DONE',
    sort_order   INT             NOT NULL DEFAULT 0 COMMENT '展示顺序，值越小越靠前',
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    completed_at DATETIME                 DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (id),
    KEY idx_action_project_order (user_id, project_id, sort_order, id),
    CONSTRAINT fk_action_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_action_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT ck_action_status CHECK (status IN ('TODO', 'DONE'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT '项目下一步行动';
