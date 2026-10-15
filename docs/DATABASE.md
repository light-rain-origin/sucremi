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

### `project_checkpoint` —— 项目开发检查点（V4）

保存用户手动记录的开发状态快照，包含摘要、当前关注点、阻塞事项和创建时间。通过 `user_id`、
`project_id` 归属到用户和项目，并以 `(user_id, project_id, created_at, id)` 索引支持读取最新记录。

### `project_action` —— 项目下一步行动（V4）

保存用户维护的下一步行动，包含标题、说明、TODO/DONE 状态、项目内排序和完成时间。通过
`user_id`、`project_id` 限定归属，并以 `(user_id, project_id, sort_order, id)` 索引支持列表排序。
两表结构定义在 `V4__create_context_recovery.sql`，由 Flyway 自动迁移；不得手工建表。

### `project_activity` —— 项目活动时间线（V5）

记录项目创建、状态与归档变化、检查点创建、行动创建及行动状态变化。每条记录带 `user_id` 和
`project_id`，使用 `(user_id, project_id, created_at, id)` 支持项目内倒序分页，使用
`(user_id, created_at, id)` 支持首页最近活动。`event_type` 为稳定枚举，`metadata_version` 标记
元数据结构版本；`metadata` 仅保存轻量 ID 或状态信息，不重复保存检查点、行动说明等正文。V5 不回填历史事件。

### `project_decision` 与 `project_decision_revision` —— 项目决策（V6）

`project_decision` 保存当前决策状态，包括用户与项目归属、背景、备选方案 JSON、选择与理由、状态、替代关系、
乐观锁版本和软删除时间。状态为 `PROPOSED`、`ACCEPTED`、`SUPERSEDED`；普通读取排除软删除记录，服务层在
所有读写中附加当前用户和项目条件。

`project_decision_revision` 保存完整 JSON 快照。版本号在决策内唯一，创建时从 1 开始；实质性变更和快照在同一
事务提交，历史版本没有修改接口。替代关系经当前用户与项目校验，同一事务内完成新决策创建、旧决策更新、双方
快照和活动记录。V6 不回填历史决策或活动。

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
