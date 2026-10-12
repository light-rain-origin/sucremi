-- Sucremi 初始基线迁移。
--
-- 设计说明：第一版服务单个用户，但所有业务数据从一开始就以 app_user 作为
-- 归属锚点，为“用户数据隔离”预留空间 —— 后续业务表统一携带 user_id 外键关联
-- 到 app_user.id，从而天然支持多用户隔离，而无需重构既有结构。
-- 本阶段不实现认证/权限，app_user 仅建立隔离所需的最小结构。

CREATE TABLE app_user (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    username     VARCHAR(64)     NOT NULL COMMENT '登录名/唯一标识',
    display_name VARCHAR(128)             DEFAULT NULL COMMENT '展示名称',
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT '工作台用户（用户数据隔离锚点）';
