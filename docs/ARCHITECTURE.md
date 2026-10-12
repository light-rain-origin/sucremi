# 架构（ARCHITECTURE）

## 总体

单仓库（monorepo），前后端分目录、**独立构建**：

```
apps/web      前端（Vue 3）
apps/server   后端（Spring Boot，模块化单体）
```

后端采用**模块化单体**，不引入微服务。业务按职责分模块，避免巨型 Controller / Service。

## 前端（apps/web）

- Vue 3 + TypeScript + Vite 构建。
- 状态：Pinia；路由：Vue Router；UI：Element Plus。
- Markdown 编辑：Vditor（阶段 0 仅完成依赖评估与安装，未集成完整编辑器）。
- 测试：Vitest（jsdom 环境）。
- 接口调用统一封装在 `src/api/`，约定后端统一响应结构。
- 开发代理：`/api/*` → `http://localhost:8080`（见 `vite.config.ts`），避免跨域。

## 后端（apps/server）

- Java 25 + Spring Boot 4.1.1 + Maven（通过 `mvnw` 包装器）。
- 持久层：MyBatis-Plus（`mybatis-plus-spring-boot4-starter`）+ MySQL 8.4。
- 迁移：Flyway，所有结构变更以迁移脚本落地。
- 测试：JUnit 5；测试用内存 H2（`test` profile）以摆脱对本地 MySQL 的依赖。

### 包结构（按职责）

```
com.sucremi
├─ ServerApplication           启动类
├─ common.api.ApiResponse      统一响应结构
├─ common.exception            BusinessException + 全局异常处理
└─ health.HealthController     健康检查
```

后续业务模块以同样方式按职责新增独立包（如 `project`、`note` 等）。

## API 约定

- 统一前缀 `/api/v1`。
- 统一响应结构 `ApiResponse{ code, message, data, timestamp }`，`code == 0` 为成功。
- 全局异常处理统一返回上述结构，**不暴露堆栈或内部细节**。

## 用户数据隔离（预留设计）

虽然 V1 为单用户，但从一开始就为多用户隔离预留空间：

- 存在 `app_user` 表作为用户归属锚点。
- 未来所有业务表统一携带 `user_id`，外键关联 `app_user.id`。
- 查询在服务层按当前用户过滤。

如此可在不重构既有结构的前提下平滑支持多用户。

## 配置与密钥

- 密钥一律从环境变量读取，`application.yaml` 以 `${ENV:default}` 引用。
- `.env` 本地忽略，仓库只保留 `.env.example` 占位示例。
