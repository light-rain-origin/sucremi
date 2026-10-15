# 开发指南（DEVELOPMENT）

## 本机实测环境

| 工具 | 要求 | 本机实测（2026-10-11） |
|------|------|------------------------|
| Java | 25 | 25.0.2 LTS ✅ |
| Maven | 3.9+ | `mvnw` 包装器，分发 Maven 3.10.0 ✅（系统未单独安装 mvn） |
| Node.js | ≥ 24.12 | 24.21.0 ✅ |
| npm | — | 11.19.0 ✅ |
| Git | — | 2.55.0 ✅ |
| MySQL | 8.4 LTS | 需本机手动安装 ⚠️ |
| Docker | — | 未安装，本阶段不使用 |

## 首次准备

```bash
# 1. 安装 MySQL 8.4 LTS，然后创建库与账号
mysql -u root -p < scripts/sql/init_database.sql

# 2. 配置环境变量
cp .env.example .env   # 填入与上一步一致的数据库密码

# 3. 前端安装依赖
cd apps/web && npm install
```

## 启动

```bash
# 后端（首次启动时 Flyway 自动迁移）
# 需要：① 数据库密码(环境变量 SUCREMI_DB_PASSWORD) ② local profile(启用开发期固定身份)
cd apps/server
SUCREMI_DB_PASSWORD=你的本地密码 ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# 或在 IDE 运行配置里设置：环境变量 SUCREMI_DB_PASSWORD，Active profiles = local

# 前端
cd apps/web && npm run dev
```

> 未加 `local` profile 时，后端仍可启动、健康检查正常，但需要用户身份的业务接口会返回
> 401（失败关闭设计，防止无认证状态被误用于公开部署，详见
> docs/decisions/0001-identity-and-data-ownership.md）。

- 后端：`http://localhost:8080`，健康检查 `GET /api/v1/health`
- 前端：`http://localhost:5173`，`/api/*` 经 Vite 代理转发到后端

## 停止

各自终端 `Ctrl+C`。

## 测试

```bash
# 后端：使用内存 H2（test profile，关闭 Flyway），无需本地 MySQL
cd apps/server && ./mvnw test

# 前端
cd apps/web && npm run test:unit -- --run
```

### P0 测试矩阵（当前覆盖）

| 层 | 用例 | 覆盖内容 |
|----|------|----------|
| 后端 · 启动 | `ServerApplicationTests` | 上下文可加载 |
| 后端 · 接口 | `HealthApiTests` | `/health` 响应结构、`X-Request-Id` 透传 |
| 后端 · 接口 | `ProjectControllerTest` | 路由、参数校验 400、不存在 404、非法 JSON 400 |
| 后端 · 接口 | `DashboardControllerTest` | `/dashboard/summary` 字段结构（前端类型依赖） |
| 后端 · 恢复接口 | `RecoveryControllerTest` | 检查点、行动、恢复聚合接口及参数校验 |
| 后端 · 恢复服务 | `RecoveryServiceImplTest` | 用户隔离、项目归属、最新检查点、行动状态与排序 |
| 后端 · 服务 | `ProjectServiceImplTest` | 所有权注入、跨用户/不存在 404、状态白名单、分页夹取、列表带 `user_id` 过滤 |
| 后端 · 服务 | `DashboardServiceImplTest` | 计数装配、复用项目服务取最近项目 |
| 后端 · 领域 | `ProjectStatusTest` / `IdentityResolverTest` | 状态白名单、开发身份与拒绝式兜底 |
| 前端 · 组件 | `StatusTag` / `ProgressBar` / `ProjectCard` / `EmptyState` / `ProjectFormDialog` | 展示映射、进度渲染、卡片事件、必填校验拦截 |
| 前端 · 布局 | `AppSidebar` | 导航渲染 |
| 前端 · 状态 | `stores/project` / `stores/dashboard` | 列表/创建/归档成功与失败路径、错误信息透出 |
| 前端 · 页面 | `ProjectsView` / `DashboardView` | 列表与空态、过滤器防抖与重置页码、翻页、统计卡与最近项目 |
| 前端 · 恢复 | `ContinueWorkCard` / `RecoveryView` | 首页继续工作卡片、检查点与行动交互及空态/错误态 |
| 前端 · 工具 | `datetime` | 空值、正常格式化、非法输入回退 |

未纳入单元测试的部分：MyBatis-Plus 生成的 SQL 与 Flyway 迁移只在**真实 MySQL** 上验证
（`./mvnw spring-boot:run` + 手工/脚本冒烟），测试 profile 不建表。

> 前端 `vitest.config.ts` 里 `server.deps.inline: ['element-plus']` 是必需的：若把它外部化交给
> Node 直接加载，`async-validator` 的默认导出互操作会出问题，导致 `el-form` 校验静默失效
> （表单级 `validate()` 对空必填字段也返回通过）。

## 构建

```bash
cd apps/server && ./mvnw clean package     # 产出 target/*.jar
cd apps/web && npm run build               # 产出 dist/
```

## 环境变量

后端通过环境变量读取数据库配置（见 `application.yaml`）：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| SUCREMI_DB_URL | `jdbc:mysql://localhost:3306/sucremi?...` | JDBC 连接串 |
| SUCREMI_DB_USERNAME | `sucremi` | 数据库用户 |
| SUCREMI_DB_PASSWORD | （空） | 数据库密码，本地必须设置 |
| SUCREMI_SERVER_PORT | `8080` | 后端端口 |

可在 shell `export`，或在 IDE 运行配置中设置。注意 Spring Boot 不会自动加载 `.env` 文件，
`.env` 主要作为变量清单参考；如需自动加载可在 IDE 使用 EnvFile 类插件。

## 约定

- 每完成一步执行相应构建或测试，不以“生成成功”代替验证。
- 数据库变更一律新增 Flyway 迁移脚本，不手工改表。
- 不提交秘密；提交前检查工作区无 `.env` 等敏感文件。

## 已知问题

### npm audit 的 braces 高危误报（不处理）

`cd apps/web && npm audit` 会报 4 个 high，指向 `braces`（经
`@vue/eslint-config-typescript → fast-glob → micromatch → braces` 引入）。

实情：当前已安装 `braces@3.0.3`，即该漏洞（CVE-2024-4068）的**修复版**，也是现存最新版，
整条链都是最新。这是 npm audit 的陈旧公告误报，**没有可做的前向修复**。

**不要执行 `npm audit fix --force`**：它会把 `@vue/eslint-config-typescript` 从 14.9.0
降级到 14.0.1，并与 eslint 10 / eslint-plugin-vue 10 产生 peer 依赖冲突，破坏 lint，
且根本动不到已是最新的 braces。

该依赖仅在 lint 期（devDependency）使用，不进前端/后端运行时产物，触发条件是超深嵌套的
glob 模式，而 lint 配置无不可信输入，实际风险≈0。待上游 `fast-glob`/`micromatch` 发布
新版后，用 `npm update` 自然消除即可。
