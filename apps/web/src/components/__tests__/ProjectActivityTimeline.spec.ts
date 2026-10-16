import { describe, expect, it, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import ProjectActivityTimeline from '../ProjectActivityTimeline.vue'

vi.mock('@/api/activity', () => ({
  getProjectActivities: vi.fn<typeof import('@/api/activity').getProjectActivities>(),
  getRecentActivities: vi.fn<typeof import('@/api/activity').getRecentActivities>(),
}))

import { getProjectActivities, getRecentActivities } from '@/api/activity'

const mockedProjectActivities = vi.mocked(getProjectActivities)
const mockedRecentActivities = vi.mocked(getRecentActivities)

function render(props: { projectId?: number; compact?: boolean }) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/projects/:id', component: { template: '<div />' } },
      { path: '/projects/:id/recovery', component: { template: '<div />' } },
      { path: '/projects/:id/decisions/:decisionId', component: { template: '<div />' } },
    ],
  })
  return mount(ProjectActivityTimeline, { props, global: { plugins: [router] } })
}

const event = {
  id: 1,
  projectId: 4,
  projectName: '蕾米工作台',
  eventType: 'CHECKPOINT_CREATED' as const,
  metadataVersion: 1,
  metadata: '{"checkpointId":8}',
  createdAt: '2026-10-15T10:00:00',
}

describe('ProjectActivityTimeline', () => {
  beforeEach(() => {
    mockedProjectActivities.mockReset()
    mockedRecentActivities.mockReset()
  })

  it('loads and displays a project activity page', async () => {
    mockedProjectActivities.mockResolvedValue({
      code: 0, message: 'OK', data: { list: [event], total: 1, page: 1, size: 10 }, requestId: 'r', timestamp: 1,
    })
    const wrapper = render({ projectId: 4 })
    await flushPromises()

    expect(mockedProjectActivities).toHaveBeenCalledWith(4, { page: 1, size: 10, type: '' })
    expect(wrapper.text()).toContain('蕾米工作台')
    expect(wrapper.text()).toContain('保存了检查点')
  })

  it('shows a useful empty state and loads recent activities in compact mode', async () => {
    mockedRecentActivities.mockResolvedValue({ code: 0, message: 'OK', data: [], requestId: 'r', timestamp: 1 })
    const wrapper = render({ compact: true })
    await flushPromises()

    expect(mockedRecentActivities).toHaveBeenCalledWith(5)
    expect(wrapper.text()).toContain('还没有活动记录')
  })

  it('shows the short decision title from versioned activity metadata', async () => {
    mockedProjectActivities.mockResolvedValue({
      code: 0,
      message: 'OK',
      data: {
        list: [{ ...event, eventType: 'DECISION_ACCEPTED' as const, metadata: '{"decisionId":9,"title":"采用 Pinia"}' }],
        total: 1,
        page: 1,
        size: 10,
      },
      requestId: 'r',
      timestamp: 1,
    })
    const wrapper = render({ projectId: 4 })
    await flushPromises()

    expect(wrapper.text()).toContain('采纳了决策「采用 Pinia」')
  })

  it('surfaces API errors with a retry action', async () => {
    mockedProjectActivities.mockResolvedValue({
      code: 50000, message: '活动暂时不可用', data: null as never, requestId: 'r', timestamp: 1,
    })
    const wrapper = render({ projectId: 4 })
    await flushPromises()

    expect(wrapper.text()).toContain('活动暂时不可用')
    expect(wrapper.find('button').text()).toBe('重试')
  })

  it('links checkpoint and action events to their recovery records', async () => {
    mockedProjectActivities.mockResolvedValue({
      code: 0,
      message: 'OK',
      data: {
        list: [
          event,
          { ...event, id: 2, eventType: 'ACTION_COMPLETED', metadata: '{"actionId":19}' },
        ],
        total: 2,
        page: 1,
        size: 10,
      },
      requestId: 'r',
      timestamp: 1,
    })
    const wrapper = render({ projectId: 4 })
    await flushPromises()

    expect(wrapper.findAll('a').map((link) => link.attributes('href'))).toEqual([
      '/projects/4/recovery#checkpoint-8',
      '/projects/4/recovery#action-19',
    ])
  })

  it('opens a decision when its id exists and falls back for deleted decisions', async () => {
    mockedRecentActivities.mockResolvedValue({
      code: 0,
      message: 'OK',
      data: [
        { ...event, eventType: 'DECISION_ACCEPTED', metadata: '{"decisionId":29,"title":"采用 Pinia"}' },
        { ...event, id: 2, eventType: 'DECISION_DELETED', metadata: '{"decisionId":30,"title":"旧决策"}' },
      ],
      requestId: 'r',
      timestamp: 1,
    })
    const wrapper = render({ compact: true })
    await flushPromises()

    expect(wrapper.findAll('a').map((link) => link.attributes('href'))).toEqual([
      '/projects/4/decisions/29',
      '/projects/4#decision-records',
    ])
  })
})
