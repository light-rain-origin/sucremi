import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import RecoveryView from '../RecoveryView.vue'
import { ElMessageBox } from 'element-plus'
import type { ProjectRecovery } from '@/types/recovery'

vi.mock('@/api/recovery', () => ({
  getProjectRecovery: vi.fn<typeof import('@/api/recovery').getProjectRecovery>(),
  listCheckpoints: vi.fn<typeof import('@/api/recovery').listCheckpoints>(),
  createCheckpoint: vi.fn<typeof import('@/api/recovery').createCheckpoint>(),
  createAction: vi.fn<typeof import('@/api/recovery').createAction>(),
  deleteCheckpoint: vi.fn<typeof import('@/api/recovery').deleteCheckpoint>(),
  deleteAction: vi.fn<typeof import('@/api/recovery').deleteAction>(),
  updateActionStatus: vi.fn<typeof import('@/api/recovery').updateActionStatus>(),
  reorderActions: vi.fn<typeof import('@/api/recovery').reorderActions>(),
}))

import {
  createAction,
  createCheckpoint,
  deleteAction,
  deleteCheckpoint,
  getProjectRecovery,
  listCheckpoints,
  reorderActions,
  updateActionStatus,
} from '@/api/recovery'

const mockedRecovery = vi.mocked(getProjectRecovery)
const mockedCreateCheckpoint = vi.mocked(createCheckpoint)
const mockedCreateAction = vi.mocked(createAction)
const mockedDeleteAction = vi.mocked(deleteAction)
const mockedDeleteCheckpoint = vi.mocked(deleteCheckpoint)
const mockedListCheckpoints = vi.mocked(listCheckpoints)
const mockedUpdateActionStatus = vi.mocked(updateActionStatus)
const mockedReorderActions = vi.mocked(reorderActions)

const sampleProject = {
  id: 8,
  name: '蕾米工作台',
  summary: null,
  goal: null,
  status: 'ACTIVE' as const,
  progress: 36,
  archived: false,
  createdAt: '2026-10-12T09:00:00',
  updatedAt: '2026-10-14T12:30:00',
}

const action = {
  id: 21,
  projectId: 8,
  title: '补充异常处理',
  details: '覆盖网络超时情况',
  status: 'TODO' as const,
  sortOrder: 0,
  createdAt: '2026-10-14T12:01:00',
  updatedAt: '2026-10-14T12:01:00',
  completedAt: null,
}

function recovery(overrides: Partial<ProjectRecovery> = {}): ProjectRecovery {
  return {
    project: sampleProject,
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

async function render(initial: ProjectRecovery = recovery()) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/projects', component: { template: '<div />' } },
      { path: '/projects/:id', component: { template: '<div />' } },
      { path: '/projects/:id/recovery', component: RecoveryView },
    ],
  })
  mockedRecovery.mockResolvedValue(ok(initial))
  mockedListCheckpoints.mockResolvedValue({
    code: 0,
    message: 'OK',
    data: initial.latestCheckpoint ? [initial.latestCheckpoint] : [],
    requestId: 'h',
    timestamp: 1,
  })
  await router.push('/projects/8/recovery')
  await router.isReady()
  const wrapper = mount(RecoveryView, { global: { plugins: [router] } })
  await flushPromises()
  return wrapper
}

describe('RecoveryView', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    mockedRecovery.mockReset()
    mockedCreateCheckpoint.mockReset()
    mockedCreateAction.mockReset()
    mockedDeleteAction.mockReset()
    mockedDeleteCheckpoint.mockReset()
    mockedListCheckpoints.mockReset()
    mockedUpdateActionStatus.mockReset()
    mockedReorderActions.mockReset()
    mockedListCheckpoints.mockResolvedValue({
      code: 0,
      message: 'OK',
      data: [],
      requestId: 'h',
      timestamp: 1,
    })
  })

  it('shows an honest empty checkpoint state and a form to save one', async () => {
    const wrapper = await render()

    expect(wrapper.text()).toContain('还没有保存过检查点')
    expect(wrapper.text()).toContain('保存当前检查点')
    expect(wrapper.text()).toContain('还没有下一步行动')
    expect(wrapper.text()).not.toContain('上次停在这里')
  })

  it('provides stable anchors for activity links to checkpoints and actions', async () => {
    const checkpoint = {
      id: 32,
      projectId: 8,
      summary: '完成上下文回归',
      currentFocus: '活动跳转',
      blockers: null,
      createdAt: '2026-10-15T12:00:00',
    }
    const wrapper = await render(recovery({ latestCheckpoint: checkpoint, actions: [action] }))

    expect(wrapper.find('#checkpoint-32').exists()).toBe(true)
    expect(wrapper.find('#action-21').exists()).toBe(true)
  })

  it('shows current key decisions and a link to the decision detail', async () => {
    const wrapper = await render(recovery({ keyDecisions: [{ id: 31, title: '采用 Pinia', decision: 'Pinia', rationaleSummary: '更适配 Vue' }] }))
    expect(wrapper.text()).toContain('关键决策')
    expect(wrapper.text()).toContain('更适配 Vue')
    expect(wrapper.find('a[href="/projects/8/decisions/31"]').exists()).toBe(true)
  })

  it('renders the latest checkpoint and persisted actions', async () => {
    const wrapper = await render(
      recovery({
        latestCheckpoint: {
          id: 9,
          projectId: 8,
          summary: '登录流程已接通',
          currentFocus: '补充错误状态',
          blockers: '等待接口联调',
          createdAt: '2026-10-14T12:00:00',
        },
        actions: [action],
        recoverySummary: '登录流程已接通 当前关注：补充错误状态 阻塞事项：等待接口联调 下一步：补充异常处理',
      }),
    )

    expect(wrapper.text()).toContain('蕾米工作台')
    expect(wrapper.text()).toContain('登录流程已接通')
    expect(wrapper.text()).toContain('等待接口联调')
    expect(wrapper.text()).toContain('补充异常处理')
  })

  it('persists a checkpoint and reloads the latest recovery data', async () => {
    const updated = recovery({
      latestCheckpoint: {
        id: 31,
        projectId: 8,
        summary: '完成了核心流程',
        currentFocus: '准备测试',
        blockers: null,
        createdAt: '2026-10-15T10:00:00',
      },
      recoverySummary: '完成了核心流程 当前关注：准备测试',
    })
    const wrapper = await render()
    mockedCreateCheckpoint.mockResolvedValue({
      code: 0,
      message: 'OK',
      data: updated.latestCheckpoint!,
      requestId: 'c',
      timestamp: 2,
    })
    mockedRecovery.mockResolvedValue(ok(updated))

    await wrapper.get('textarea').setValue('完成了核心流程')
    await wrapper.get('input[placeholder="现在正在处理什么？"]').setValue('准备测试')
    await wrapper.get('.checkpoint-form').trigger('submit')
    await flushPromises()

    expect(mockedCreateCheckpoint).toHaveBeenCalledWith(8, {
      summary: '完成了核心流程',
      currentFocus: '准备测试',
    })
    expect(wrapper.text()).toContain('完成了核心流程')
    expect(mockedRecovery).toHaveBeenCalledTimes(2)
  })

  it('persists action completion and displays the completed state', async () => {
    const wrapper = await render(recovery({ actions: [action] }))
    mockedUpdateActionStatus.mockResolvedValue({
      code: 0,
      message: 'OK',
      data: { ...action, status: 'DONE', completedAt: '2026-10-15T10:00:00' },
      requestId: 'u',
      timestamp: 2,
    })

    await wrapper.get('button[aria-label="完成：补充异常处理"]').trigger('click')
    await flushPromises()

    expect(mockedUpdateActionStatus).toHaveBeenCalledWith(8, 21, 'DONE')
    expect(wrapper.text()).toContain('已完成')
  })

  it('saves action ordering through the API', async () => {
    const second = { ...action, id: 22, title: '编写测试', sortOrder: 1 }
    const wrapper = await render(recovery({ actions: [action, second] }))
    mockedReorderActions.mockResolvedValue({
      code: 0,
      message: 'OK',
      data: [{ ...second, sortOrder: 0 }, { ...action, sortOrder: 1 }],
      requestId: 'o',
      timestamp: 3,
    })

    await wrapper.get('button[aria-label="下移：补充异常处理"]').trigger('click')
    await flushPromises()

    expect(mockedReorderActions).toHaveBeenCalledWith(8, [22, 21])
  })

  it('deletes an action after confirmation and updates the list', async () => {
    vi.spyOn(ElMessageBox, 'confirm').mockResolvedValue({ value: '', action: 'confirm' } as never)
    mockedDeleteAction.mockResolvedValue({ code: 0, message: 'OK', data: undefined, requestId: 'd', timestamp: 4 })
    const wrapper = await render(recovery({ actions: [action] }))

    await wrapper.get('button[aria-label="删除行动：补充异常处理"]').trigger('click')
    await flushPromises()

    expect(mockedDeleteAction).toHaveBeenCalledWith(8, 21)
    expect(wrapper.text()).toContain('还没有下一步行动')
  })

  it('deletes the latest checkpoint and returns to the empty state', async () => {
    vi.spyOn(ElMessageBox, 'confirm').mockResolvedValue({ value: '', action: 'confirm' } as never)
    const savedCheckpoint = {
      id: 9,
      projectId: 8,
      summary: '旧的进度记录',
      currentFocus: null,
      blockers: null,
      createdAt: '2026-10-14T12:00:00',
    }
    mockedDeleteCheckpoint.mockResolvedValue({ code: 0, message: 'OK', data: undefined, requestId: 'd', timestamp: 4 })
    const wrapper = await render(recovery({ latestCheckpoint: savedCheckpoint, recoverySummary: '旧的进度记录' }))
    mockedListCheckpoints.mockResolvedValue({ code: 0, message: 'OK', data: [], requestId: 'h', timestamp: 5 })
    mockedRecovery.mockResolvedValue(ok(recovery()))

    await wrapper.get('button[aria-label="删除最新检查点"]').trigger('click')
    await flushPromises()

    expect(mockedDeleteCheckpoint).toHaveBeenCalledWith(8, 9)
    expect(wrapper.text()).toContain('还没有保存过检查点')
  })

  it('shows a loading state while recovery data is pending', async () => {
    let finishRequest!: (value: ReturnType<typeof ok>) => void
    mockedRecovery.mockReturnValue(new Promise((resolve) => (finishRequest = resolve)))
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/projects/:id/recovery', component: RecoveryView }],
    })
    await router.push('/projects/8/recovery')
    const wrapper = mount(RecoveryView, { global: { plugins: [router] } })
    expect(wrapper.get('[role="status"]').text()).toContain('正在找回项目进度')

    finishRequest(ok(recovery()))
    await flushPromises()
    expect(wrapper.text()).toContain('还没有保存过检查点')
  })

  it('shows API failure and retries successfully', async () => {
    mockedRecovery.mockRejectedValueOnce(new Error('恢复接口不可用'))
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/projects/:id/recovery', component: RecoveryView }],
    })
    await router.push('/projects/8/recovery')
    const wrapper = mount(RecoveryView, { global: { plugins: [router] } })
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toContain('恢复接口不可用')
    mockedRecovery.mockResolvedValueOnce(ok(recovery()))
    await wrapper.get('.secondary-button').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('还没有保存过检查点')
  })
})
