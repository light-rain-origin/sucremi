-- Sucremi 本地数据库初始化脚本（MySQL 8.4 LTS）。
-- 以 root 身份执行一次，创建开发库与开发账号。
-- 表结构不在此创建 —— 由后端启动时 Flyway 自动迁移（见 apps/server/.../db/migration）。
--
-- 用法示例：
--   mysql -u root -p < scripts/sql/init_database.sql
--
-- 注意：请把下面的占位密码替换为你自己的本地密码，并与 .env 中的 SUCREMI_DB_PASSWORD 保持一致。

CREATE DATABASE IF NOT EXISTS sucremi
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'sucremi'@'localhost' IDENTIFIED BY 'change-me-local-password';
GRANT ALL PRIVILEGES ON sucremi.* TO 'sucremi'@'localhost';
FLUSH PRIVILEGES;
