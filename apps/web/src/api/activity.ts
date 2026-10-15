import { apiGet, type ApiResponse } from './http'
import type { ProjectActivity, ProjectActivityPage, ProjectActivityType } from '@/types/activity'

export function getProjectActivities(
  projectId: number | string,
  params: { page?: number; size?: number; type?: ProjectActivityType | '' } = {},
): Promise<ApiResponse<ProjectActivityPage>> {
  const query = new URLSearchParams()
  if (params.page !== undefined) query.set('page', String(params.page))
  if (params.size !== undefined) query.set('size', String(params.size))
  if (params.type) query.set('type', params.type)
  const suffix = query.size ? `?${query.toString()}` : ''
  return apiGet<ProjectActivityPage>(`/api/v1/projects/${projectId}/activities${suffix}`)
}

export function getRecentActivities(size = 5): Promise<ApiResponse<ProjectActivity[]>> {
  return apiGet<ProjectActivity[]>(`/api/v1/dashboard/recent-activities?size=${size}`)
}
