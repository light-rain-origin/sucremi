import { apiGet, apiPatch, apiPost, apiPut, type ApiResponse } from './http'
import type {
  CreateActionPayload,
  CreateCheckpointPayload,
  ProjectAction,
  ProjectCheckpoint,
  ProjectRecovery,
} from '@/types/recovery'

/** 获取项目的恢复摘要、最新检查点及下一步行动。 */
export function getProjectRecovery(projectId: number | string): Promise<ApiResponse<ProjectRecovery>> {
  return apiGet<ProjectRecovery>(`/api/v1/projects/${projectId}/recovery`)
}

/** 创建一条项目检查点。 */
export function createCheckpoint(
  projectId: number | string,
  payload: CreateCheckpointPayload,
): Promise<ApiResponse<ProjectCheckpoint>> {
  return apiPost<ProjectCheckpoint>(`/api/v1/projects/${projectId}/checkpoints`, payload)
}

/** 查询项目的检查点历史。 */
export function listCheckpoints(projectId: number | string): Promise<ApiResponse<ProjectCheckpoint[]>> {
  return apiGet<ProjectCheckpoint[]>(`/api/v1/projects/${projectId}/checkpoints`)
}

/** 读取项目最新检查点。 */
export function getLatestCheckpoint(projectId: number | string): Promise<ApiResponse<ProjectCheckpoint>> {
  return apiGet<ProjectCheckpoint>(`/api/v1/projects/${projectId}/checkpoints/latest`)
}

/** 创建项目下一步行动。 */
export function createAction(
  projectId: number | string,
  payload: CreateActionPayload,
): Promise<ApiResponse<ProjectAction>> {
  return apiPost<ProjectAction>(`/api/v1/projects/${projectId}/actions`, payload)
}

/** 修改行动完成状态。 */
export function updateActionStatus(
  projectId: number | string,
  actionId: number | string,
  status: ProjectAction['status'],
): Promise<ApiResponse<ProjectAction>> {
  return apiPatch<ProjectAction>(`/api/v1/projects/${projectId}/actions/${actionId}/status`, { status })
}

/** 保存项目内全部行动的新顺序。 */
export function reorderActions(
  projectId: number | string,
  actionIds: number[],
): Promise<ApiResponse<ProjectAction[]>> {
  return apiPut<ProjectAction[]>(`/api/v1/projects/${projectId}/actions/order`, { actionIds })
}
