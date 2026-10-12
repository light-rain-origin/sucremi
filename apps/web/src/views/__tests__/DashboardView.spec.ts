import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import ElementPlus from 'element-plus'
import DashboardView from '../DashboardView.vue'
import type { DashboardSummary } from '@/types/dashboard'

vi.mock('@/api/dashboard', () => ({
  getDashboardSummary: vi.fn<typeof import('@/api/dashboard').getDashboardSummary>(),
}))
vi.mock('@/api/project', () => ({
  listProjects: vi.fn<typeof import('@/api/project').listProjects>(),
  createProject: vi.fn<typeof import('@/api/project').createProject>(),
  archiveProject: vi.fn<typeof import('@/api/project').archiveProject>(),
  unarchiveProject: vi.fn<typeof import('@/api/project').unarchiveProject>(),
}))

import { getDashboardSummary } from '@/api/dashboard'

const mocked = vi.mocked(getDashboardSummary)

function render() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/projects', component: { template: '<div />' } },
      { path: '/projects/:id', component: { template: '<div />' } },
    ],
  })
  return mount(DashboardView, { global: { plugins: [ElementPlus, createPinia(), router] } })
}

const summary: DashboardSummary = {
  totalProjects: 4,
  activeProjects: 2,
  pausedProjects: 1,
  doneProjects: 1,
  archivedProjects: 3,
  recentProjects: [
    {
      id: 5,
      name: '最近项目',
      summary: 's',
      goal: null,
      status: 'ACTIVE',
      progress: 20,
      archived: false,
      createdAt: '2026-10-12T11:34:37',
      updatedAt: '2026-10-12T11:34:37',
    },
  ],
}

describe('DashboardView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mocked.mockReset()
  })

  it('renders the five stat cards with backend counts', async () => {
    mocked.mockResolvedValue({ code: 0, message: 'OK', data: summary, requestId: 'r', timestamp: 1 })
    const wrapper = render()
    await flushPromises()

    const text = wrapper.text()
    for (const label of ['全部项目', '进行中', '已暂停', '已完成', '已归档']) {
      expect(text).toContain(label)
    }
    expect(text).toContain('4')
    expect(text).toContain('3')
    expect(text).toContain('最近项目')
  })

  it('shows the empty state with a create entry when there is no project', async () => {
    mocked.mockResolvedValue({
      code: 0,
      message: 'OK',
      data: { ...summary, totalProjects: 0, activeProjects: 0, pausedProjects: 0, doneProjects: 0, archivedProjects: 0, recentProjects: [] },
      requestId: 'r',
      timestamp: 1,
    })
    const wrapper = render()
    await flushPromises()

    expect(wrapper.text()).toContain('还没有项目，创建第一个吧')
  })

  it('surfaces a backend error message', async () => {
    mocked.mockResolvedValue({
      code: 50000,
      message: '服务器开小差了',
      data: null as unknown as DashboardSummary,
      requestId: 'r',
      timestamp: 1,
    })
    const wrapper = render()
    await flushPromises()

    expect(wrapper.text()).toContain('服务器开小差了')
  })
})
