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
  "timestamp": 1760000000000
}
```

| 字段 | 说明 |
|------|------|
| code | 业务状态码，`0` 为成功，非 0 为业务/系统错误 |
| message | 可读提示信息 |
| data | 业务数据，错误时为 `null` |
| timestamp | 服务端生成响应的毫秒时间戳 |

## 错误码约定

| 范围 | 含义 |
|------|------|
| 0 | 成功 |
| 40000 | 参数校验失败 |
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
  "timestamp": 1760160000000
}
```
