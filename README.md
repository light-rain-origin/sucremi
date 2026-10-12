# Sucremi

个人开发工作台。解决项目中断后忘记进度、设计原因、资料位置和下一步行动的问题。

第一版服务单个用户，优先建设私人工作台，公开网站后续开发。

> 当前处于 **阶段 0（工程初始化）**：仅完成工程骨架、技术约定、文档与本地启动验证，尚未实现业务功能。

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
├─ docs/        项目文档
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

> 本阶段不使用 Docker。

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

后端通过环境变量读取数据库配置。可在 shell 中 `export`，或在 IDE 运行配置中设置
`SUCREMI_DB_URL` / `SUCREMI_DB_USERNAME` / `SUCREMI_DB_PASSWORD`。

### 3. 启动后端

```bash
cd apps/server
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

首次启动时 Flyway 会自动执行数据库迁移。后端默认监听 `http://localhost:8080`，
健康检查：`http://localhost:8080/api/v1/health`。

### 4. 启动前端

```bash
cd apps/web
npm install        # 首次
npm run dev
```

前端默认 `http://localhost:5173`，`/api/*` 请求通过 Vite 代理转发到后端 8080。
打开首页点击“检查后端健康状态”可验证前后端连通。

## 停止

- 前端 / 后端：在各自终端按 `Ctrl+C`。

## 测试

```bash
# 后端（使用内存 H2，无需本地 MySQL）
cd apps/server && ./mvnw test

# 前端
cd apps/web && npm run test:unit -- --run
```

## 文档

- [产品说明](docs/PRODUCT_SPEC.md)
- [架构](docs/ARCHITECTURE.md)
- [数据库](docs/DATABASE.md)
- [API](docs/API.md)
- [开发指南](docs/DEVELOPMENT.md)
- [路线图](docs/ROADMAP.md)

## 安全约定

- 禁止提交真实密码、API Key 等秘密；`.env` 已被忽略，`.env.example` 仅含占位值。
- MySQL 默认仅供本地开发访问。
- 对外统一异常响应，不暴露堆栈或敏感信息。
