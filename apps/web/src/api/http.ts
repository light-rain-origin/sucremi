/**
 * 极简 HTTP 客户端。约定后端统一响应结构 ApiResponse。
 * 开发环境下通过 Vite 代理访问后端（见 vite.config.ts）。
 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export async function apiGet<T>(path: string): Promise<ApiResponse<T>> {
  const res = await fetch(path, { headers: { Accept: 'application/json' } })
  return (await res.json()) as ApiResponse<T>
}
