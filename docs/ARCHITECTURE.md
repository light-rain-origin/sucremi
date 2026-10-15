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
- Markdown 编辑：Vditor（已安装依赖，尚未集成编辑器 —— 留给资料 / 笔记等后续阶段）。
- 测试：Vitest（jsdom 环境）。
- 接口调用统一封装在 `src/api/`，约定后端统一响应结构。
- 目录：页面 `src/views/`、通用组件 `src/components/`、状态 `src/stores/`、类型 `src/types/`、
  工具 `src/utils/`、路由 `src/router/`。
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
├─ common.api                  ApiResponse / PageResult 统一响应结构
├─ common.exception            BusinessException / ResourceNotFoundException /
│                              UnauthenticatedException + 全局异常处理
├─ common.web                  RequestContext + X-Request-Id 过滤器（MDC 可日志追踪）
├─ config                      MyBatis-Plus 等框架配置
├─ identity                    当前用户解析（开发期固定身份 / 失败关闭兜底）
├─ health.HealthController     健康检查
├─ project                     项目模块（controller / service / mapper / dto）
├─ decision                    项目决策与版本快照模块
├─ recovery                    上下文恢复聚合（含决策摘要）
├─ activity                    项目活动事件
└─ dashboard                   概览统计
```

业务模块沿用同样方式按职责新增独立包（如 `note`、`task` 等），模块内含各自的
controller / service / mapper / dto，不建立巨型 Controller / Service。

## API 约定

- 统一前缀 `/api/v1`。
- 统一响应结构 `ApiResponse{ code, message, data, timestamp }`，`code == 0` 为成功。
- 全局异常处理统一返回上述结构，**不暴露堆栈或内部细节**。

## 用户数据隔离

虽然 V1 为单用户，但结构从一开始就按多用户隔离落地，而不是留待日后重构：

- `app_user` 表作为用户归属锚点。
- 业务表统一携带 `user_id`，外键关联 `app_user.id`（首张业务表 `project` 已如此）。
- 身份由 `identity` 包的 `CurrentUserResolver` 提供，业务代码不感知具体认证方式：
  `local`/`dev`/`test` profile 下注入开发期固定身份，其余情况由兜底实现直接拒绝
  （**失败关闭**，避免无认证状态被误部署到公网）。
- 查询在 SQL 层按当前用户过滤；写操作按 `id + user_id` 更新，命中 0 行即返回 404 ——
  **不区分「不存在」与「别人的记录」**，避免泄露资源存在性。

身份与数据归属背景见 `docs/decisions/0001-identity-and-data-ownership.md`；决策版本与替代规则见
`docs/decisions/0002-project-decision-history.md`。

决策模块分别维护当前状态和不可变版本快照。决策变更、快照、替代状态与活动事件在同一业务事务中完成；
上下文恢复只读取最多三条当前有效决策。`version` 用于乐观锁，历史快照使用后状态并以决策内版本号唯一。

## 配置与密钥

- 密钥一律从环境变量读取，`application.yaml` 以 `${ENV:default}` 引用。
- `.env` 本地忽略，仓库只保留 `.env.example` 占位示例。
