# CLAUDE.md

本文件为在此仓库工作的 Claude Code 提供指引。请在修改代码前阅读。

## 项目定位

Sucremi 是**个人开发工作台**，解决项目中断后忘记进度、设计原因、资料位置和下一步
行动的问题。第一版服务单个用户，优先私人工作台，公开网站后续开发。

当前处于**阶段 0（工程初始化）**：只做骨架、约定、文档与本地启动验证，**不实现业务功能**。
阶段 0 完成后应停下，等待下一阶段任务，不要擅自开发业务模块。

## 技术栈

- 前端（`apps/web`）：Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus + Vditor + Vitest
- 后端（`apps/server`）：Java 25 + Spring Boot 4.1.1 + Maven + MyBatis-Plus + MySQL 8.4 LTS + Flyway + JUnit 5
- 单仓库，前后端分目录、独立构建。本阶段不使用 Docker。

## 目录职责

- `apps/web/src/api/` —— 后端接口封装（统一 ApiResponse 结构）
- `apps/web/src/views/` —— 页面；`src/router/` 路由；`src/stores/` Pinia 状态
- `apps/server/.../common/api/` —— 统一响应 `ApiResponse`
- `apps/server/.../common/exception/` —— 全局异常处理
- `apps/server/.../health/` —— 健康检查（按职责分模块的示例）
- `apps/server/src/main/resources/db/migration/` —— Flyway 迁移脚本
- `docs/` —— 产品 / 架构 / 数据库 / API / 开发 / 路线图文档
- `scripts/` —— 本地脚本（数据库初始化等）

## 架构约束

- 模块化单体，**不引入微服务**。
- 业务按职责分模块，不建立巨型 Controller / Service。
- API 统一使用 `/api/v1` 前缀。
- 统一响应结构；统一异常处理，**绝不向客户端暴露堆栈或敏感信息**。
- 数据库结构变更**必须**通过 Flyway 迁移，不手工改表。
- 数据库表与接口须预留**用户数据隔离**空间：业务表统一携带 `user_id`，关联 `app_user.id`。
- 不为“预留扩展”而引入尚无实际需求的基础设施。
- 不实现尚未规划的 AI、多 Agent、公开博客或复杂权限功能。

## 测试要求

- 每完成一步执行相应构建或测试，**不要仅凭生成成功就宣称完成**。
- 后端测试使用内存 H2（`test` profile，关闭 Flyway），无需本地 MySQL 即可运行：
  `cd apps/server && ./mvnw test`
- 前端：`cd apps/web && npm run test:unit -- --run`
- 新增功能 / 修复缺陷应同时补测试。

## 数据安全

- 所有秘密从环境变量或本地忽略的配置文件读取；`application.yaml` 以 `${ENV:default}` 引用。
- 禁止提交真实密码、API Key；`.env` 已忽略，`.env.example` 仅占位。
- MySQL 默认仅本地访问。

## 禁止事项

- 不擅自执行破坏性操作（删库、drop 表、强制重置等）。
- 未经确认不提交 Git、不推送远程、不部署公网。
- 修改前先检查已有文件，避免覆盖用户已有工作；目录非空先分析再提出兼容方案。
- 依赖下载失败 / 环境缺失 / 版本冲突时如实报告，不得伪造成功。

## 常用命令

```bash
# 后端：构建+测试 / 运行
cd apps/server && ./mvnw test
cd apps/server && ./mvnw spring-boot:run

# 前端：开发 / 测试 / 构建 / 代码检查
cd apps/web && npm run dev
cd apps/web && npm run test:unit -- --run
cd apps/web && npm run build
cd apps/web && npm run lint

# 数据库初始化（首次，需本地 MySQL 8.4）
mysql -u root -p < scripts/sql/init_database.sql
```
