import { describe, expect, it, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import ProjectActivityTimeline from '../ProjectActivityTimeline.vue'

vi.mock('@/api/activity', () => ({
  getProjectActivities: vi.fn<typeof import('@/api/activity').getProjectActivities>(),
  getRecentActivities: vi.fn<typeof import('@/api/activity').getRecentActivities>(),
}))

import { getProjectActivities, getRecentActivities } from '@/api/activity'

const mockedProjectActivities = vi.mocked(getProjectActivities)
const mockedRecentActivities = vi.mocked(getRecentActivities)

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
    const wrapper = mount(ProjectActivityTimeline, { props: { projectId: 4 } })
    await flushPromises()

    expect(mockedProjectActivities).toHaveBeenCalledWith(4, { page: 1, size: 10, type: '' })
    expect(wrapper.text()).toContain('蕾米工作台')
    expect(wrapper.text()).toContain('保存了检查点')
  })

  it('shows a useful empty state and loads recent activities in compact mode', async () => {
    mockedRecentActivities.mockResolvedValue({ code: 0, message: 'OK', data: [], requestId: 'r', timestamp: 1 })
    const wrapper = mount(ProjectActivityTimeline, { props: { compact: true } })
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
    const wrapper = mount(ProjectActivityTimeline, { props: { projectId: 4 } })
    await flushPromises()

    expect(wrapper.text()).toContain('采纳了决策「采用 Pinia」')
  })

  it('surfaces API errors with a retry action', async () => {
    mockedProjectActivities.mockResolvedValue({
      code: 50000, message: '活动暂时不可用', data: null as never, requestId: 'r', timestamp: 1,
    })
    const wrapper = mount(ProjectActivityTimeline, { props: { projectId: 4 } })
    await flushPromises()

    expect(wrapper.text()).toContain('活动暂时不可用')
    expect(wrapper.find('button').text()).toBe('重试')
  })
})
