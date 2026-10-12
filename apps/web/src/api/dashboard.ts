import { apiGet, type ApiResponse } from './http'
import type { DashboardSummary } from '@/types/dashboard'

export function getDashboardSummary(): Promise<ApiResponse<DashboardSummary>> {
  return apiGet<DashboardSummary>('/api/v1/dashboard/summary')
}
