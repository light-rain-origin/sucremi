-- 项目决策记录及不可变历史快照。
CREATE TABLE project_decision (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '决策主键',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '所属用户 → app_user.id',
    project_id          BIGINT UNSIGNED NOT NULL COMMENT '所属项目 → project.id',
    title               VARCHAR(200)    NOT NULL COMMENT '决策标题',
    context             TEXT            NOT NULL COMMENT '背景与问题',
    options_json        JSON                     DEFAULT NULL COMMENT '备选方案数组',
    decision            TEXT                     DEFAULT NULL COMMENT '最终选择',
    rationale           TEXT                     DEFAULT NULL COMMENT '选择理由',
    consequences        TEXT                     DEFAULT NULL COMMENT '预期影响与取舍',
    revisit_condition  TEXT                     DEFAULT NULL COMMENT '重新评估条件',
    status              VARCHAR(20)     NOT NULL DEFAULT 'PROPOSED' COMMENT '决策状态',
    superseded_by_id    BIGINT UNSIGNED          DEFAULT NULL COMMENT '替代本决策的新决策 ID',
    version             INT UNSIGNED    NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at          DATETIME                 DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (id),
    KEY idx_decision_project_status_updated (user_id, project_id, status, updated_at, id),
    KEY idx_decision_project_created (user_id, project_id, created_at, id),
    KEY idx_decision_superseded_by (superseded_by_id),
    CONSTRAINT fk_decision_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_decision_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_decision_superseded_by FOREIGN KEY (superseded_by_id) REFERENCES project_decision (id),
    CONSTRAINT ck_decision_status CHECK (status IN ('PROPOSED', 'ACCEPTED', 'SUPERSEDED')),
    CONSTRAINT ck_decision_version CHECK (version >= 1)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT '项目决策记录';

CREATE TABLE project_decision_revision (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '决策历史主键',
    user_id           BIGINT UNSIGNED NOT NULL COMMENT '所属用户 → app_user.id',
    project_id        BIGINT UNSIGNED NOT NULL COMMENT '所属项目 → project.id',
    decision_id       BIGINT UNSIGNED NOT NULL COMMENT '所属决策 → project_decision.id',
    revision_number   INT UNSIGNED    NOT NULL COMMENT '决策版本号，从 1 开始',
    snapshot_json     JSON            NOT NULL COMMENT '完整决策快照',
    change_summary    VARCHAR(500)             DEFAULT NULL COMMENT '变更说明',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '版本时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_decision_revision_number (decision_id, revision_number),
    KEY idx_decision_revision_owner (user_id, project_id, decision_id, revision_number),
    CONSTRAINT fk_decision_revision_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_decision_revision_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_decision_revision_decision FOREIGN KEY (decision_id) REFERENCES project_decision (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT '项目决策历史版本';
