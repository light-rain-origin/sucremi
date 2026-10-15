import { describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import DecisionList from '../DecisionList.vue'

vi.mock('@/api/decision', () => ({
  listDecisions: vi.fn<typeof import('@/api/decision').listDecisions>(),
  createDecision: vi.fn<typeof import('@/api/decision').createDecision>(),
  updateDecision: vi.fn<typeof import('@/api/decision').updateDecision>(),
}))
import { listDecisions } from '@/api/decision'

const decision = {
  id: 3, projectId: 8, title: '使用 Pinia', context: '需要共享页面状态', options: ['Vuex', 'Pinia'],
  decision: 'Pinia', rationale: '轻量且适配 Vue', consequences: null, revisitCondition: null,
  status: 'ACCEPTED' as const, supersededById: null, version: 1, createdAt: '2026-10-15T10:00:00', updatedAt: '2026-10-15T10:00:00',
}

async function render(data: object) {
  vi.mocked(listDecisions).mockResolvedValue({ code: 0, message: 'OK', data, requestId: 'r', timestamp: 1 } as never)
  const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/projects/:id/decisions/:decisionId', component: { template: '<div />' } }] })
  const wrapper = mount(DecisionList, { props: { projectId: 8 }, global: { plugins: [router], stubs: { ElButton: true, ElDialog: true, ElPagination: true } } })
  await flushPromises()
  return wrapper
}

describe('DecisionList', () => {
  it('renders a real accepted decision with its details link', async () => {
    const wrapper = await render({ list: [decision], total: 1, page: 1, size: 6 })
    expect(wrapper.text()).toContain('使用 Pinia')
    expect(wrapper.text()).toContain('已采纳')
    expect(wrapper.find('a[href="/projects/8/decisions/3"]').exists()).toBe(true)
  })

  it('shows a clear empty state when the project has no decisions', async () => {
    const wrapper = await render({ list: [], total: 0, page: 1, size: 6 })
    expect(wrapper.text()).toContain('还没有决策记录')
  })
})
