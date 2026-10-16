import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import ElementPlus from 'element-plus'
import ProjectsView from '../ProjectsView.vue'
import { useProjectStore } from '@/stores/project'
import type { Project } from '@/types/project'

vi.mock('@/api/project', () => ({
  listProjects: vi.fn<typeof import('@/api/project').listProjects>(),
  createProject: vi.fn<typeof import('@/api/project').createProject>(),
  archiveProject: vi.fn<typeof import('@/api/project').archiveProject>(),
  unarchiveProject: vi.fn<typeof import('@/api/project').unarchiveProject>(),
}))

import { listProjects } from '@/api/project'

const mocked = vi.mocked(listProjects)

const project: Project = {
  id: 1,
  name: '项目A',
  summary: '简介A',
  goal: null,
  status: 'ACTIVE',
  progress: 10,
  archived: false,
  createdAt: '2026-10-12T11:34:37',
  updatedAt: '2026-10-12T11:34:37',
}

const okPage = (list: Project[]) => ({
  code: 0,
  message: 'OK',
  data: { list, total: list.length, page: 1, size: 12 },
  requestId: 'r',
  timestamp: 1,
})

/** 先建 pinia 再取 store、再挂载，保证测试与组件用的是同一个 store 实例。 */
async function render(path = '/projects') {
  const pinia = createPinia()
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/projects', component: { template: '<div />' } },
      { path: '/projects/:id', component: { template: '<div />' } },
    ],
  })
  await router.push(path)
  await router.isReady()
  const store = useProjectStore(pinia)
  const wrapper = mount(ProjectsView, { global: { plugins: [ElementPlus, pinia, router] } })
  return { wrapper, store, router }
}

/** 过滤器变更走 250ms 防抖。 */
const settleDebounce = () => new Promise((resolve) => setTimeout(resolve, 300))

/** 最近一次列表请求的参数。 */
const lastParams = () => mocked.mock.calls[mocked.mock.calls.length - 1]![0]

describe('ProjectsView', () => {
  beforeEach(() => {
    mocked.mockReset()
  })

  it('loads the list on mount and renders cards', async () => {
    mocked.mockResolvedValue(okPage([project]))
    const { wrapper } = await render()
    await flushPromises()

    expect(mocked).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('项目A')
  })

  it('shows the empty state when the current filter has no project', async () => {
    mocked.mockResolvedValue(okPage([]))
    const { wrapper } = await render()
    await flushPromises()

    expect(wrapper.text()).toContain('还没有项目，创建第一个吧')
  })

  it('resets to page 1 and refetches when a filter changes', async () => {
    mocked.mockResolvedValue(okPage([project]))
    const { store } = await render()
    await flushPromises()
    store.query.page = 3
    await flushPromises()
    const callsBefore = mocked.mock.calls.length

    store.query.status = 'PAUSED'
    await settleDebounce()
    await flushPromises()

    expect(store.query.page).toBe(1)
    expect(mocked.mock.calls.length).toBe(callsBefore + 1)
    expect(lastParams()).toMatchObject({ status: 'PAUSED', page: 1 })
  })

  it('passes the archived flag through to the API', async () => {
    mocked.mockResolvedValue(okPage([]))
    const { wrapper, store, router } = await render()
    await flushPromises()

    store.query.archived = true
    await settleDebounce()
    await flushPromises()

    expect(lastParams()).toMatchObject({ archived: true })
    expect(wrapper.text()).toContain('没有已归档的项目')
    await flushPromises()
    expect(router.currentRoute.value.query.archived).toBe('true')
  })

  it('refetches immediately when the page changes', async () => {
    mocked.mockResolvedValue({
      ...okPage([project]),
      data: { list: [project], total: 40, page: 1, size: 12 },
    })
    const { wrapper, store } = await render()
    await flushPromises()
    const callsBefore = mocked.mock.calls.length
    expect(callsBefore).toBe(1)

    wrapper.findComponent({ name: 'ElPagination' }).vm.$emit('current-change', 2)
    await flushPromises()

    expect(store.query.page).toBe(2)
    expect(mocked.mock.calls.length).toBe(callsBefore + 1)
    expect(lastParams()).toMatchObject({ page: 2 })
  })

  it('restores a status filter from the URL and keeps it after refresh navigation', async () => {
    mocked.mockResolvedValue(okPage([project]))
    const { store, router } = await render('/projects?status=PAUSED')
    await flushPromises()

    expect(store.query.status).toBe('PAUSED')
    expect(lastParams()).toMatchObject({ status: 'PAUSED', archived: false })

    store.query.status = 'DONE'
    await settleDebounce()
    await flushPromises()
    expect(router.currentRoute.value.query.status).toBe('DONE')
  })
})
