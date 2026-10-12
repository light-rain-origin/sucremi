-- 项目表：工作台核心业务实体。
-- 归属：每个项目属于一个用户（user_id → app_user.id），是用户数据隔离的落地起点；
--       后续 task/checkpoint 等子表同样携带 user_id 并关联本表。
-- 性质：可变业务数据（非历史快照）。归档采用软归档（archived_at），不物理删除。

CREATE TABLE project (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '项目主键',
    user_id     BIGINT UNSIGNED  NOT NULL COMMENT '所属用户 → app_user.id',
    name        VARCHAR(100)     NOT NULL COMMENT '项目名称',
    summary     VARCHAR(500)              DEFAULT NULL COMMENT '简介',
    goal        TEXT                      DEFAULT NULL COMMENT '目标',
    status      VARCHAR(20)      NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/PAUSED/DONE',
    progress    TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '进度百分比 0-100',
    archived_at DATETIME                  DEFAULT NULL COMMENT '归档时间；NULL=未归档（软归档）',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_project_user (user_id, archived_at, updated_at),
    CONSTRAINT fk_project_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT ck_project_progress CHECK (progress <= 100)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT '项目';
