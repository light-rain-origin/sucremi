import { apiDelete, apiGet, apiPatch, apiPost, type ApiResponse } from './http'
import type { DecisionPayload, DecisionRevision, ProjectDecision } from '@/types/decision'
import type { PageResult } from '@/types/common'
export type { PageResult } from '@/types/common'
export type SupersedePayload = Omit<DecisionPayload, 'status' | 'expectedVersion'> & { expectedVersion: number }

function base(projectId: number | string) {
  return `/api/v1/projects/${projectId}/decisions`
}

export function listDecisions(projectId: number | string, params: {
  page?: number; size?: number; status?: string; sortBy?: 'createdAt' | 'updatedAt'; direction?: 'asc' | 'desc'
} = {}): Promise<ApiResponse<PageResult<ProjectDecision>>> {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => { if (value) query.set(key, String(value)) })
  return apiGet<PageResult<ProjectDecision>>(`${base(projectId)}${query.size ? `?${query}` : ''}`)
}

export function getDecision(projectId: number | string, decisionId: number | string): Promise<ApiResponse<ProjectDecision>> {
  return apiGet<ProjectDecision>(`${base(projectId)}/${decisionId}`)
}

export function createDecision(projectId: number | string, payload: DecisionPayload): Promise<ApiResponse<ProjectDecision>> {
  return apiPost<ProjectDecision>(base(projectId), payload)
}

export function updateDecision(projectId: number | string, decisionId: number | string, payload: DecisionPayload): Promise<ApiResponse<ProjectDecision>> {
  return apiPatch<ProjectDecision>(`${base(projectId)}/${decisionId}`, payload)
}

export function listDecisionRevisions(projectId: number | string, decisionId: number | string, page = 1, size = 20): Promise<ApiResponse<PageResult<DecisionRevision>>> {
  return apiGet<PageResult<DecisionRevision>>(`${base(projectId)}/${decisionId}/revisions?page=${page}&size=${size}`)
}

export function supersedeDecision(projectId: number | string, decisionId: number | string, payload: SupersedePayload): Promise<ApiResponse<ProjectDecision>> {
  return apiPost<ProjectDecision>(`${base(projectId)}/${decisionId}/supersede`, payload)
}

export function deleteDecision(projectId: number | string, decisionId: number | string): Promise<ApiResponse<void>> {
  return apiDelete<void>(`${base(projectId)}/${decisionId}`)
}
