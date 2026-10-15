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

### 项目 Project

所有接口在当前用户范围内操作；跨用户或不存在的项目一律返回 404。请求体不接收 `user_id`。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/projects` | 列表。query：`q`(名称/简介模糊)、`status`、`archived`(默认 false)、`page`(默认 1)、`size`(默认 20，上限 100)、`sort`(默认 `updatedAt,desc`；字段白名单：updatedAt/createdAt/name/progress) |
| POST | `/api/v1/projects` | 创建。body：`name`(必填,≤100)、`summary`(≤500)、`goal`、`status`(默认 ACTIVE)、`progress`(0-100,默认 0) |
| GET | `/api/v1/projects/{id}` | 详情 |
| PUT | `/api/v1/projects/{id}` | 编辑。body：`name`(必填)、`summary`、`goal`、`status`(必填)、`progress`(必填,0-100) |
| POST | `/api/v1/projects/{id}/archive` | 归档(软) |
| POST | `/api/v1/projects/{id}/unarchive` | 取消归档 |

`data` 为单个 `ProjectResponse`（含 `id/name/summary/goal/status/progress/archived/createdAt/updatedAt`），
列表接口 `data` 为 `PageResult`（`list/total/page/size`）。错误：参数校验 400/`40000`，不存在 404/`40400`。

### 首页概览 Dashboard

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/dashboard/summary` | 当前用户的统计 + 最近项目 |
| GET | `/api/v1/dashboard/project-flow` | 当前用户未归档项目的状态计数与进度列表 |
| GET | `/api/v1/dashboard/recent-activities` | 当前用户最近 5 条项目活动；`size` 可调小，最多返回 5 条 |

`data`：`{ totalProjects, activeProjects, pausedProjects, doneProjects, archivedProjects, recentProjects[] }`。
计数均限当前用户；前四项为未归档项目，`archivedProjects` 为已归档数；`recentProjects` 为最近更新的前 5 个未归档项目（`ProjectResponse` 列表）。

`/dashboard/project-flow` 的 `data`：`{ unfinishedCount, ongoingCount, completedCount, projects[] }`。
仅返回当前用户的未归档项目，按更新时间倒序；`unfinishedCount` 包含进行中与已暂停项目，
`ongoingCount` 只统计进行中项目，`completedCount` 统计已完成项目；`projects` 为对应的 `ProjectResponse` 列表。

`/dashboard/recent-activities` 返回活动数组，元素包含 `id/projectId/projectName/eventType/metadataVersion/metadata/createdAt`。

### 项目上下文恢复

所有恢复接口均位于项目资源下，仅操作当前用户拥有的项目；跨用户或不存在的项目统一返回 404。
检查点和行动由用户在项目恢复页手动保存，恢复摘要只根据最新检查点及行动规则式聚合，不会生成未保存的进度。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/projects/{projectId}/checkpoints` | 保存检查点；body：`summary`（必填，≤10000）、`currentFocus`（可选，≤255）、`blockers`（可选，≤10000） |
| GET | `/api/v1/projects/{projectId}/checkpoints` | 按时间倒序读取检查点历史 |
| DELETE | `/api/v1/projects/{projectId}/checkpoints/{checkpointId}` | 删除指定检查点；仅当前用户可删除所属项目中的记录 |
| GET | `/api/v1/projects/{projectId}/checkpoints/latest` | 读取最新检查点；无记录时 `data` 为空 |
| POST | `/api/v1/projects/{projectId}/actions` | 新建行动；body：`title`（必填，≤200）、`details`（可选，≤1000） |
| GET | `/api/v1/projects/{projectId}/actions` | 按 `sortOrder` 读取行动 |
| DELETE | `/api/v1/projects/{projectId}/actions/{actionId}` | 删除指定行动；仅当前用户可删除所属项目中的记录 |
| PATCH | `/api/v1/projects/{projectId}/actions/{actionId}/status` | 更新状态；body：`{"status":"TODO"}` 或 `{"status":"DONE"}` |
| PUT | `/api/v1/projects/{projectId}/actions/order` | 更新顺序；body：`{"actionIds":[1,2]}`，须包含该项目全部行动 ID 且不可重复 |
| GET | `/api/v1/projects/{projectId}/recovery` | 获取项目、最新检查点、行动及规则式恢复摘要 |

检查点响应字段为 `id/projectId/summary/currentFocus/blockers/createdAt`；行动响应字段为
`id/projectId/title/details/status/sortOrder/createdAt/updatedAt/completedAt`。聚合接口返回
`{ project, latestCheckpoint, actions, recoverySummary }`；没有检查点时 `latestCheckpoint` 与
`recoverySummary` 为空，行动列表仍可独立使用。

### 项目活动时间线

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/projects/{projectId}/activities` | 项目活动分页查询；参数：`page`(默认 1)、`size`(默认 20，上限 100)、`type`(可选稳定事件类型) |

响应 `data` 为 `PageResult`，活动包含 `id/projectId/projectName/eventType/metadataVersion/metadata/createdAt`；`metadata` 是 JSON 字符串。
事件类型包括 `PROJECT_CREATED`、`PROJECT_STATUS_CHANGED`、`PROJECT_ARCHIVED`、`PROJECT_UNARCHIVED`、
`CHECKPOINT_CREATED`、`ACTION_CREATED`、`ACTION_COMPLETED`、`ACTION_REOPENED`。元数据首版为版本 1，
仅保存关联记录 ID 或状态变化，不复制检查点、行动说明等正文；历史数据不回填。
