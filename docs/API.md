# API

## 约定

- 基础前缀：`/api/v1`
- 内容类型：`application/json`
- 统一响应结构：

```json
{
  "code": 0,
  "message": "OK",
  "data": { },
  "requestId": "f1e2d3c4-....",
  "timestamp": 1760000000000
}
```

| 字段 | 说明 |
|------|------|
| code | 业务状态码，`0` 为成功，非 0 为业务/系统错误 |
| message | 可读提示信息 |
| data | 业务数据，错误时为 `null` |
| requestId | 本次请求的追踪 ID，与响应头 `X-Request-Id` 一致，便于日志关联 |
| timestamp | 服务端生成响应的毫秒时间戳 |

> `requestId` 由后端 `RequestIdFilter` 为每个请求生成（或沿用调用方传入的 `X-Request-Id`），
> 同时写入响应头 `X-Request-Id` 与日志 MDC（`%X{requestId}`）。

## 错误码约定

| 范围 | 含义 |
|------|------|
| 0 | 成功 |
| 40000 | 参数校验失败 |
| 40100 | 未认证（无可信用户身份） |
| 50000 | 服务器内部错误（不含堆栈/细节） |

业务错误通过抛出 `BusinessException(code, message)` 返回，由全局异常处理统一包装。
后续业务模块可在各自范围内约定更细的业务码。

## 接口

### GET /api/v1/health

健康检查。用于前端代理连通性验证与基础存活探测，不依赖数据库。

**响应示例**

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "status": "UP",
    "app": "sucremi-server",
    "time": "2026-10-11T13:38:34.842Z"
  },
  "requestId": "f1e2d3c4-....",
  "timestamp": 1760160000000
}
```
