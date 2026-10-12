# 数据库（DATABASE）

## 环境

- MySQL **8.4 LTS**，本地开发使用，默认仅本地访问。
- 字符集 `utf8mb4` / 排序规则 `utf8mb4_unicode_ci`。
- 库名：`sucremi`；开发账号：`sucremi`（见 `scripts/sql/init_database.sql`）。

## 迁移管理（Flyway）

- 所有结构变更**必须**通过 Flyway 迁移，禁止手工改表。
- 迁移脚本位置：`apps/server/src/main/resources/db/migration/`。
- 命名规范：`V<版本号>__<描述>.sql`，例如 `V1__init_baseline.sql`、`V2__add_project.sql`。
- 后端启动时自动执行未应用的迁移（`spring.flyway.enabled=true`，`baseline-on-migrate=true`）。
- 测试环境关闭 Flyway，使用内存 H2，不影响本地库。

## 初始化步骤

```bash
# 1. 创建库与账号（首次，需 MySQL 8.4）
mysql -u root -p < scripts/sql/init_database.sql

# 2. 配置 .env 中的数据库密码（与上面一致）

# 3. 启动后端，Flyway 自动建表
cd apps/server && ./mvnw spring-boot:run
```

## 当前表结构

### `app_user` —— 工作台用户（用户数据隔离锚点）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT UNSIGNED, PK, AUTO_INCREMENT | 用户主键 |
| username | VARCHAR(64), UNIQUE, NOT NULL | 登录名/唯一标识 |
| display_name | VARCHAR(128), NULL | 展示名称 |
| created_at | DATETIME, 默认当前时间 | 创建时间 |
| updated_at | DATETIME, 更新时自动刷新 | 更新时间 |

> V1 为单用户，`app_user` 仅建立隔离所需的最小结构，不含认证/权限字段。

## 用户数据隔离约定

后续业务表统一携带 `user_id BIGINT UNSIGNED NOT NULL` 并外键关联 `app_user.id`，
服务层按当前用户过滤数据。此约定从第一张业务表起执行，避免日后重构。
