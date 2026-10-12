import { apiGet, type ApiResponse } from './http'
import type { DashboardSummary, ProjectFlowSummary } from '@/types/dashboard'

export function getDashboardSummary(): Promise<ApiResponse<DashboardSummary>> {
  return apiGet<DashboardSummary>('/api/v1/dashboard/summary')
}

export function getProjectFlow(): Promise<ApiResponse<ProjectFlowSummary>> {
  return apiGet<ProjectFlowSummary>('/api/v1/dashboard/project-flow')
}
