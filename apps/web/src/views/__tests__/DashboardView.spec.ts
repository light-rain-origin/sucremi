import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import ElementPlus from 'element-plus'
import DashboardView from '../DashboardView.vue'
import type { DashboardSummary } from '@/types/dashboard'

vi.mock('@/api/dashboard', () => ({
  getDashboardSummary: vi.fn<typeof import('@/api/dashboard').getDashboardSummary>(),
  getProjectFlow: vi.fn<typeof import('@/api/dashboard').getProjectFlow>(),
}))
vi.mock('@/api/project', () => ({
  listProjects: vi.fn<typeof import('@/api/project').listProjects>(),
  createProject: vi.fn<typeof import('@/api/project').createProject>(),
  archiveProject: vi.fn<typeof import('@/api/project').archiveProject>(),
  unarchiveProject: vi.fn<typeof import('@/api/project').unarchiveProject>(),
}))
vi.mock('@/api/recovery', () => ({
  getProjectRecovery: vi.fn<typeof import('@/api/recovery').getProjectRecovery>(),
}))

import { getDashboardSummary, getProjectFlow } from '@/api/dashboard'
import { getProjectRecovery } from '@/api/recovery'

const mocked = vi.mocked(getDashboardSummary)
const mockedProjectFlow = vi.mocked(getProjectFlow)
const mockedRecovery = vi.mocked(getProjectRecovery)

function render() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/projects', component: { template: '<div />' } },
      { path: '/projects/:id', component: { template: '<div />' } },
      { path: '/projects/:id/recovery', component: { template: '<div />' } },
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
    mockedProjectFlow.mockReset()
    mockedRecovery.mockReset()
    mockedRecovery.mockImplementation(async (projectId) => ({
      code: 0,
      message: 'OK',
      data: {
        project: summary.recentProjects.find((project) => project.id === Number(projectId)) ?? summary.recentProjects[0]!,
        latestCheckpoint: {
          id: 12,
          projectId: Number(projectId),
          summary: '恢复卡片测试进度',
          currentFocus: '完善页面',
          blockers: null,
          createdAt: '2026-10-14T12:00:00',
        },
        actions: [],
        recoverySummary: '恢复卡片测试进度 当前关注：完善页面',
      },
      requestId: 'recovery',
      timestamp: 1,
    }))
    mockedProjectFlow.mockResolvedValue({
      code: 0,
      message: 'OK',
      data: {
        unfinishedCount: 2,
        ongoingCount: 2,
        completedCount: 0,
        projects: summary.recentProjects,
      },
      requestId: 'flow',
      timestamp: 1,
    })
  })

  it('renders the five stat cards with backend counts', async () => {
    mocked.mockResolvedValue({ code: 0, message: 'OK', data: summary, requestId: 'r', timestamp: 1 })
    const wrapper = render()
    await flushPromises()

    const text = wrapper.text()
    for (const label of ['全部项目', '进行中', '已暂停', '已完成', '已归档']) {
      expect(text).toContain(label)
    }
    expect(text).toContain('早上好，今天也慢慢来！')
    expect(text).toContain('4')
    expect(text).toContain('3')
    expect(text).toContain('最近项目')
    expect(text).toContain('状态与整体进度')
    expect(text).toContain('继续工作')
    expect(text).toContain('继续这个项目')
    expect(text).toContain('最近更新')
    expect(text).toContain('恢复卡片测试进度')
    expect(text).toContain('20%')
  })

  it('shows the empty state with a create entry when there is no project', async () => {
    mockedProjectFlow.mockResolvedValue({
      code: 0,
      message: 'OK',
      data: { unfinishedCount: 0, ongoingCount: 0, completedCount: 0, projects: [] },
      requestId: 'flow',
      timestamp: 1,
    })
    mocked.mockResolvedValue({
      code: 0,
      message: 'OK',
      data: { ...summary, totalProjects: 0, activeProjects: 0, pausedProjects: 0, doneProjects: 0, archivedProjects: 0, recentProjects: [] },
      requestId: 'r',
      timestamp: 1,
    })
    const wrapper = render()
    await flushPromises()

    expect(wrapper.text()).toContain('先创建一个项目，记录下每次努力的起点')
  })

  it('surfaces a project flow API error in the bottom recovery card', async () => {
    mockedProjectFlow.mockResolvedValue({
      code: 50000,
      message: '项目进度读取失败',
      data: null as unknown as import('@/types/dashboard').ProjectFlowSummary,
      requestId: 'r',
      timestamp: 1,
    })
    mocked.mockResolvedValue({ code: 0, message: 'OK', data: summary, requestId: 'r', timestamp: 1 })
    const wrapper = render()
    await flushPromises()

    expect(wrapper.text()).toContain('项目进度读取失败')
  })
})
