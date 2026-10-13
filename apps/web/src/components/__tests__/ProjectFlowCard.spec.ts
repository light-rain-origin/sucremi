import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProjectFlowCard from '../ProjectFlowCard.vue'
import type { Project } from '@/types/project'
import type { ProjectFlowSummary } from '@/types/dashboard'

const projects: Project[] = [
  {
    id: 1,
    name: 'Sucremi 工作台',
    summary: null,
    goal: null,
    status: 'ACTIVE',
    progress: 40,
    archived: false,
    createdAt: '2026-10-01T10:00:00',
    updatedAt: '2026-10-12T10:00:00',
  },
  {
    id: 2,
    name: '灵感收集器',
    summary: null,
    goal: null,
    status: 'PAUSED',
    progress: 20,
    archived: false,
    createdAt: '2026-09-01T10:00:00',
    updatedAt: '2026-10-10T10:00:00',
  },
  {
    id: 3,
    name: '个人主页',
    summary: null,
    goal: null,
    status: 'DONE',
    progress: 100,
    archived: false,
    createdAt: '2026-08-01T10:00:00',
    updatedAt: '2026-10-08T10:00:00',
  },
]

const flow: ProjectFlowSummary = {
  unfinishedCount: 2,
  ongoingCount: 1,
  completedCount: 1,
  projects,
}

function render(props: { flow?: ProjectFlowSummary | null; loading?: boolean; error?: string | null } = {}) {
  return mount(ProjectFlowCard, {
    props: {
      flow: props.flow === undefined ? flow : props.flow,
      loading: props.loading ?? false,
      error: props.error ?? null,
    },
  })
}

describe('ProjectFlowCard', () => {
  it('renders Chinese labels and actual project names, dates and progress', () => {
    const wrapper = render()

    expect(wrapper.text()).toContain('未完成2')
    expect(wrapper.text()).toContain('进行中1')
    expect(wrapper.text()).toContain('已完成1')
    expect(wrapper.text()).toContain('Sucremi 工作台')
    expect(wrapper.text()).toContain('40%')
    expect(wrapper.text()).not.toContain('Remaining')
    expect(wrapper.text()).not.toContain('Tasks Done')
  })

  it('filters unfinished projects and shows their average progress', async () => {
    const wrapper = render()

    await wrapper.get('[role="tab"]:nth-child(1)').trigger('click')

    expect(wrapper.text()).toContain('Sucremi 工作台')
    expect(wrapper.text()).toContain('灵感收集器')
    expect(wrapper.text()).not.toContain('个人主页')
    expect(wrapper.find('[role="img"]').attributes('aria-label')).toContain('平均进度 30%')
  })

  it('emits the selected project id and the view-all action', async () => {
    const wrapper = render()

    await wrapper.get('.project-pill').trigger('click')
    await wrapper.get('.view-all').trigger('click')

    expect(wrapper.emitted('open')).toEqual([[1]])
    expect(wrapper.emitted('viewAll')).toHaveLength(1)
  })

  it('shows loading, error and empty states', () => {
    expect(render({ flow: null, loading: true }).text()).toContain('正在加载项目进度')
    expect(render({ flow: null, error: '加载失败' }).text()).toContain('加载失败')
    expect(render({ flow: { ...flow, ongoingCount: 0, projects: [projects[1]!] } }).text()).toContain('进行中暂无项目')
  })
})
