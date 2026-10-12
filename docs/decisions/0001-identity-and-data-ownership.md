# ADR 0001：身份模型与数据所有权

状态：已采纳（阶段 1） · 日期：2026-10-12

## 背景

Sucremi V1 先服务单用户。本阶段**只做身份模型与数据所有权，暂不实现认证**
（无登录/注册/密码/JWT/OAuth）。但数据与代码必须能平滑对接后续：JWT、
第三方关联登录（QQ/微信/GitHub）、Spring Security。

## 决策

### 1. app_user 作为稳定身份锚点
- `app_user.id`（BIGINT UNSIGNED）是所有业务数据归属的稳定主键，永不复用、不随用户名变化。
- **app_user 保持“纯身份”**：不放密码、不放任何单一第三方的 openid 等字段——
  这样一个用户将来可关联多个登录方式而无需改表。

### 2. CurrentUserResolver 抽象
- 业务层通过接口 `CurrentUserResolver.currentUserId()` 获取当前用户，**不依赖**任何具体认证方式。
- 阶段 1 实现：`DevUserResolver`（返回配置的固定本地用户 id）。
- 将来认证就绪：实现一个基于 Spring Security `SecurityContext` 的解析器注册为 Bean，
  业务层零改动即可切换。

### 3. 开发期固定身份 + 失败关闭
- 仅在 profile `local/dev/test` 下注册 `DevUserResolver`（固定 `app_user.id=1`，由 V2 迁移 seed）。
- 其它情况（未指定 profile / prod）由 `DenyingUserResolver` 兜底：需要身份的请求一律 401。
- **防误用于公网**：公开部署若未接入真实认证，业务接口默认不可用（失败关闭）。
  启用开发身份是显式动作（加 local profile），不会在生产悄悄生效。

### 4. 数据所有权
- 所有业务表直接携带 `user_id`（含子表），服务层统一按当前用户过滤；跨用户访问返回 404。
- **绝不**依据前端传入的 user_id 判定身份；DTO 不接收 user_id，由服务端注入。

## 未来认证接入（规划，不在本阶段实现）

- **Spring Security + JWT**：请求携带 JWT → 过滤器认证 → 填充 SecurityContext →
  `SecurityContextUserResolver` 实现 `CurrentUserResolver`。
- **第三方关联登录（QQ/微信/GitHub）**：新增 `user_identity` 关联表
  `(provider, provider_user_id) → app_user_id`，一个 app_user 可关联多个 provider；
  首次第三方登录时创建或关联到 app_user。app_user 本身不变。
- 切换时：注册真实解析器 Bean，`DenyingUserResolver`（`@ConditionalOnMissingBean`）自动让位；
  业务层与现有表结构无需重构。

## 重要区分

“**表已具备 user_id 字段**” ≠ “**系统已具备可靠的用户认证**”。本阶段只达成前者；
在接入真实认证前，严禁以开发身份对公网提供服务。
