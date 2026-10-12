import { apiGet, type ApiResponse } from './http'

export interface HealthInfo {
  status: string
  app: string
  time: string
}

export function fetchHealth(): Promise<ApiResponse<HealthInfo>> {
  return apiGet<HealthInfo>('/api/v1/health')
}
