import { apiGet, apiPost, apiPut, type ApiResponse } from './http'
import type { Project, ProjectForm } from '@/types/project'
import type { PageResult } from '@/types/common'

export type { PageResult } from '@/types/common'

export interface ProjectListParams {
  q?: string
  status?: string
  archived?: boolean
  page?: number
  size?: number
  sort?: string
}

export function listProjects(params: ProjectListParams): Promise<ApiResponse<PageResult<Project>>> {
  const qs = new URLSearchParams()
  if (params.q) qs.set('q', params.q)
  if (params.status) qs.set('status', params.status)
  if (params.archived != null) qs.set('archived', String(params.archived))
  if (params.page != null) qs.set('page', String(params.page))
  if (params.size != null) qs.set('size', String(params.size))
  if (params.sort) qs.set('sort', params.sort)
  return apiGet<PageResult<Project>>(`/api/v1/projects?${qs.toString()}`)
}

export function getProject(id: number | string): Promise<ApiResponse<Project>> {
  return apiGet<Project>(`/api/v1/projects/${id}`)
}

export function createProject(form: ProjectForm): Promise<ApiResponse<Project>> {
  return apiPost<Project>('/api/v1/projects', form)
}

export function updateProject(id: number | string, form: ProjectForm): Promise<ApiResponse<Project>> {
  return apiPut<Project>(`/api/v1/projects/${id}`, form)
}

export function archiveProject(id: number | string): Promise<ApiResponse<void>> {
  return apiPost<void>(`/api/v1/projects/${id}/archive`)
}

export function unarchiveProject(id: number | string): Promise<ApiResponse<void>> {
  return apiPost<void>(`/api/v1/projects/${id}/unarchive`)
}
