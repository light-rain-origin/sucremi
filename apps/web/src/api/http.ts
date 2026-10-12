/**
 * 极简 HTTP 客户端。约定后端统一响应结构 ApiResponse。
 * 开发环境下通过 Vite 代理访问后端（见 vite.config.ts）。
 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  requestId: string
  timestamp: number
}

async function request<T>(path: string, init: RequestInit): Promise<ApiResponse<T>> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (init.body != null) {
    headers['Content-Type'] = 'application/json'
  }
  const res = await fetch(path, { headers, ...init })
  return (await res.json()) as ApiResponse<T>
}

export function apiGet<T>(path: string): Promise<ApiResponse<T>> {
  return request<T>(path, { method: 'GET' })
}

export function apiPost<T>(path: string, body?: unknown): Promise<ApiResponse<T>> {
  return request<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) })
}

export function apiPut<T>(path: string, body?: unknown): Promise<ApiResponse<T>> {
  return request<T>(path, { method: 'PUT', body: body === undefined ? undefined : JSON.stringify(body) })
}
