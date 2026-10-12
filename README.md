# Sucremi

个人开发工作台。解决项目中断后忘记进度、设计原因、资料位置和下一步行动的问题。

第一版服务单个用户，优先建设私人工作台，公开网站后续开发。

> 当前进度：**P0（MVP）已完成** —— 概览 / 项目列表 / 项目详情三个页面可用，数据落在本地
> MySQL；身份采用开发期固定用户 + 失败关闭兜底（见
> [决策记录](docs/decisions/0001-identity-and-data-ownership.md)）。
> 后续按任务书推进，未规划的能力尚未开工。

## 当前功能

- **概览**：项目计数（全部 / 进行中 / 已暂停 / 已完成 / 已归档）、最近更新项目、新建项目入口
- **项目列表**：按状态与归档过滤、分页、卡片视图、新建项目
- **项目详情**：编辑、进度展示、归档 / 取消归档
- **后端接口**（统一 `/api/v1` 前缀）：`GET /health`、`GET|POST /projects`、
  `GET|PUT /projects/{id}`、`POST /projects/{id}/archive|unarchive`、`GET /dashboard/summary`

后续阶段规划见 [路线图](docs/ROADMAP.md)。

## 技术栈

- 前端：Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus + Vditor + Vitest
- 后端：Java 25 + Spring Boot 4.1.1 + Maven + MyBatis-Plus + MySQL 8.4 LTS + Flyway + JUnit 5
- 结构：单仓库（monorepo），前后端分目录、独立构建。

## 目录结构

```
sucremi/
├─ apps/
│  ├─ web/      前端（Vue 3）
│  └─ server/   后端（Spring Boot）
├─ docs/        项目文档（含 decisions/ 决策记录）
├─ scripts/     本地脚本（含数据库初始化 SQL）
├─ .env.example 环境变量示例（占位值）
└─ package.json monorepo 根（便捷脚本）
```

## 环境要求（本机实测版本）

| 工具 | 要求 | 本机实测 |
|------|------|----------|
| Java | 25 | 25.0.2 LTS ✅ |
| Maven | 3.9+ | 通过 `mvnw` 包装器（分发 3.10.0）✅ |
| Node.js | ≥ 24.12 | 24.21.0 ✅ |
| npm | — | 11.19.0 ✅ |
| Git | — | 2.55.0 ✅ |
| MySQL | 8.4 LTS | 需本机手动安装（见下）⚠️ |

> 本项目不使用 Docker，MySQL 仅本地访问。

## 本地启动

### 1. 准备数据库（首次）

安装 MySQL 8.4 LTS 后，创建开发库与账号：

```bash
mysql -u root -p < scripts/sql/init_database.sql
```

脚本中的占位密码请替换为你自己的本地密码。

### 2. 配置环境变量

复制 `.env.example` 为 `.env`，填入与上一步一致的数据库密码：

```bash
cp .env.example .env
```

后端通过环境变量读取数据库配置。**注意 Spring Boot 不会自动加载 `.env` 文件**——它只是变量
清单参考；实际需在 shell 中 `export`，或在 IDE 运行配置里设置
`SUCREMI_DB_URL` / `SUCREMI_DB_USERNAME` / `SUCREMI_DB_PASSWORD`。

### 3. 启动后端

```bash
cd apps/server
# Windows PowerShell
$env:SUCREMI_DB_PASSWORD="你的本地密码"; .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
# Git Bash / macOS / Linux
SUCREMI_DB_PASSWORD=你的本地密码 ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

`local` profile **必须带**：它启用开发期固定身份（`dev-user-id`）。缺少它后端仍能启动、
健康检查也正常，但所有需要用户身份的业务接口都会返回 401 —— 这是刻意的失败关闭设计，
防止无认证状态被误用于公开部署。启动成功时日志会有一行
`开发期固定身份已启用 (dev-user-id=…)` 警告。

首次启动时 Flyway 会自动执行数据库迁移。后端默认监听 `http://localhost:8080`，
健康检查：`http://localhost:8080/api/v1/health`。

### 4. 启动前端

```bash
cd apps/web
npm install        # 首次
npm run dev
```

前端默认 `http://localhost:5173`，`/api/*` 请求通过 Vite 代理转发到后端 8080。
打开即为概览页。

## 停止

- 前端 / 后端：在各自终端按 `Ctrl+C`。

## 测试

```bash
# 后端（使用内存 H2，无需本地 MySQL）
cd apps/server && ./mvnw test

# 前端
cd apps/web && npm run test:unit -- --run
```

覆盖范围见 [开发指南](docs/DEVELOPMENT.md) 的「P0 测试矩阵」。MyBatis-Plus 生成的 SQL 与
Flyway 迁移只在真实 MySQL 上验证（测试 profile 不建表）。

## 文档

- [产品说明](docs/PRODUCT_SPEC.md)
- [架构](docs/ARCHITECTURE.md)
- [数据库](docs/DATABASE.md)
- [API](docs/API.md)
- [开发指南](docs/DEVELOPMENT.md)
- [路线图](docs/ROADMAP.md)
- [决策记录：身份与数据归属](docs/decisions/0001-identity-and-data-ownership.md)

## 安全约定

- 禁止提交真实密码、API Key 等秘密；`.env` 已被忽略，`.env.example` 仅含占位值。
- MySQL 默认仅供本地开发访问。
- 对外统一异常响应，不暴露堆栈或敏感信息。
- 业务数据按 `user_id` 隔离，跨用户访问一律返回 404，不泄露资源是否存在。
