import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import ContinueWorkCard from '../ContinueWorkCard.vue'
import type { Project } from '@/types/project'
import type { ProjectRecovery } from '@/types/recovery'

vi.mock('@/api/recovery', () => ({
  getProjectRecovery: vi.fn<typeof import('@/api/recovery').getProjectRecovery>(),
}))

import { getProjectRecovery } from '@/api/recovery'

const mockedRecovery = vi.mocked(getProjectRecovery)

const project: Project = {
  id: 8,
  name: '蕾米工作台',
  summary: null,
  goal: null,
  status: 'ACTIVE',
  progress: 36,
  archived: false,
  createdAt: '2026-10-12T09:00:00',
  updatedAt: '2026-10-14T12:30:00',
}

function recovery(overrides: Partial<ProjectRecovery> = {}): ProjectRecovery {
  return {
    project,
    latestCheckpoint: null,
    actions: [],
    recoverySummary: null,
    keyDecisions: [],
    ...overrides,
  }
}

function ok(data: ProjectRecovery) {
  return { code: 0, message: 'OK', data, requestId: 'r', timestamp: 1 }
}

function render(props: { projects?: Project[]; projectsLoading?: boolean; projectsError?: string | null } = {}) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/projects', component: { template: '<div />' } },
      { path: '/projects/:id/recovery', component: { template: '<div />' } },
    ],
  })
  const wrapper = mount(ContinueWorkCard, {
    props: {
      projects: props.projects ?? [project],
      projectsLoading: props.projectsLoading ?? false,
      projectsError: props.projectsError ?? null,
    },
    global: { plugins: [router] },
  })
  return { wrapper, router }
}

describe('ContinueWorkCard', () => {
  beforeEach(() => mockedRecovery.mockReset())

  it('shows loading while the project list is loading', () => {
    const { wrapper } = render({ projectsLoading: true })

    expect(wrapper.get('[role="status"]').text()).toContain('正在整理项目进度')
    expect(mockedRecovery).not.toHaveBeenCalled()
  })

  it('guides the user to create a project when none exist', async () => {
    const { wrapper } = render({ projects: [] })
    await flushPromises()

    expect(wrapper.text()).toContain('先创建一个项目')
    expect(mockedRecovery).not.toHaveBeenCalled()
  })

  it('guides the user to save a checkpoint when a project has none', async () => {
    mockedRecovery.mockResolvedValue(ok(recovery()))
    const { wrapper } = render()
    await flushPromises()

    expect(wrapper.text()).toContain('还没有保存检查点')
    expect(wrapper.get('a.continue-link').attributes('href')).toBe('/projects/8/recovery')
    expect(wrapper.text()).not.toContain('undefined')
  })

  it('shows the latest saved summary and next action', async () => {
    mockedRecovery.mockResolvedValue(
      ok(
        recovery({
          latestCheckpoint: {
            id: 3,
            projectId: 8,
            summary: '接口联调已完成',
            currentFocus: '完善错误提示',
            blockers: null,
            createdAt: '2026-10-14T12:00:00',
          },
          actions: [
            {
              id: 4,
              projectId: 8,
              title: '补充异常处理',
              details: null,
              status: 'TODO',
              sortOrder: 0,
              createdAt: '2026-10-14T12:01:00',
              updatedAt: '2026-10-14T12:01:00',
              completedAt: null,
            },
          ],
          recoverySummary: '接口联调已完成 当前关注：完善错误提示 下一步：补充异常处理',
        }),
      ),
    )
    const { wrapper } = render()
    await flushPromises()

    expect(wrapper.text()).toContain('接口联调已完成')
    expect(wrapper.text()).toContain('补充异常处理')
    expect(wrapper.get('a.continue-link').text()).toContain('继续这个项目')
  })

  it('selects the project with the newest checkpoint', async () => {
    const olderProject = { ...project, id: 7, name: '旧项目' }
    mockedRecovery.mockImplementation(async (id) =>
      ok(
        recovery({
          project: Number(id) === 8 ? project : olderProject,
          latestCheckpoint: {
            id: Number(id),
            projectId: Number(id),
            summary: '进度',
            currentFocus: null,
            blockers: null,
            createdAt: Number(id) === 8 ? '2026-10-14T10:00:00' : '2026-10-14T11:00:00',
          },
          recoverySummary: Number(id) === 8 ? '新项目摘要' : '旧项目摘要',
        }),
      ),
    )
    const { wrapper } = render({ projects: [project, olderProject] })
    await flushPromises()

    expect(wrapper.text()).toContain('旧项目摘要')
    expect(wrapper.get('a.continue-link').attributes('href')).toBe('/projects/7/recovery')
  })

  it('shows API errors and retries the recovery request', async () => {
    mockedRecovery.mockRejectedValueOnce(new Error('网络不可用')).mockResolvedValueOnce(ok(recovery()))
    const { wrapper } = render()
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toContain('网络不可用')
    await wrapper.get('button').trigger('click')
    await flushPromises()

    expect(mockedRecovery).toHaveBeenCalledTimes(2)
    expect(wrapper.text()).toContain('还没有保存检查点')
  })

  it('offers retry for project list failures', async () => {
    const { wrapper } = render({ projectsError: '项目读取失败' })
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toContain('项目读取失败')
    await wrapper.get('button').trigger('click')
    expect(wrapper.emitted('retryProjects')).toHaveLength(1)
  })
})
