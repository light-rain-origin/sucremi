import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useDashboardStore } from '../dashboard'
import type { DashboardSummary } from '@/types/dashboard'

vi.mock('@/api/dashboard', () => ({
  getDashboardSummary: vi.fn<typeof import('@/api/dashboard').getDashboardSummary>(),
}))

import { getDashboardSummary } from '@/api/dashboard'

const mocked = vi.mocked(getDashboardSummary)

const sample: DashboardSummary = {
  totalProjects: 2,
  activeProjects: 1,
  pausedProjects: 1,
  doneProjects: 0,
  archivedProjects: 0,
  recentProjects: [],
}

describe('useDashboardStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mocked.mockReset()
  })

  it('stores summary on success', async () => {
    mocked.mockResolvedValue({ code: 0, message: 'OK', data: sample, requestId: 'r', timestamp: 1 })
    const store = useDashboardStore()

    await store.fetchSummary()

    expect(store.summary).toEqual(sample)
    expect(store.error).toBeNull()
    expect(store.loading).toBe(false)
  })

  it('records message when code is not 0', async () => {
    mocked.mockResolvedValue({
      code: 50000,
      message: '服务器开小差了',
      data: null as unknown as DashboardSummary,
      requestId: 'r',
      timestamp: 1,
    })
    const store = useDashboardStore()

    await store.fetchSummary()

    expect(store.error).toBe('服务器开小差了')
    expect(store.summary).toBeNull()
  })

  it('records error and clears loading when the request rejects', async () => {
    mocked.mockRejectedValue(new Error('网络异常'))
    const store = useDashboardStore()

    await store.fetchSummary()

    expect(store.error).toBe('网络异常')
    expect(store.loading).toBe(false)
  })
})
