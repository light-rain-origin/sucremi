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

/** 构造测试项目：只写关心的字段，其余给中性默认值。 */
function project(id: number, name: string, status: Project['status'], progress: number): Project {
  return {
    id,
    name,
    summary: null,
    goal: null,
    status,
    progress,
    archived: false,
    createdAt: '2026-09-01T10:00:00',
    updatedAt: '2026-10-12T10:00:00',
  }
}

const mixed: Project[] = [
  project(11, '低进度', 'ACTIVE', 10),
  project(12, '高进度', 'ACTIVE', 90),
  project(13, '暂停中', 'PAUSED', 50),
  project(14, '中进度', 'ACTIVE', 70),
]
const mixedFlow: ProjectFlowSummary = {
  unfinishedCount: 4,
  ongoingCount: 3,
  completedCount: 0,
  projects: mixed,
}

function names(wrapper: ReturnType<typeof render>) {
  return wrapper.findAll('.project-name').map((el) => el.text())
}
function tones(wrapper: ReturnType<typeof render>) {
  return wrapper.findAll('.project-pill').map((el) => el.classes().find((c) => c.startsWith('tone-')) ?? '')
}

describe('ProjectFlowCard · 取数与配色', () => {
  it('未完成只画进度最高的三条，并按进度降序', async () => {
    const wrapper = render({ flow: mixedFlow })

    await wrapper.get('[role="tab"]:nth-child(1)').trigger('click')

    expect(names(wrapper)).toEqual(['高进度', '中进度', '暂停中'])
    expect(wrapper.text()).not.toContain('低进度')
    expect(wrapper.text()).toContain('显示进度最高的 3 个，共 4 个项目')
  })

  it('进行中同样按进度降序，且不足三条时不显示「显示前三条」的提示', async () => {
    const wrapper = render({ flow: mixedFlow })

    expect(names(wrapper)).toEqual(['高进度', '中进度', '低进度'])
    expect(wrapper.text()).toContain('共 3 个项目')
  })

  it('已暂停恒用暂停色，进行中的项目不会被分到暂停色或完成色', async () => {
    const wrapper = render({ flow: mixedFlow })

    await wrapper.get('[role="tab"]:nth-child(1)').trigger('click')

    const rows = wrapper.findAll('.project-pill').map((el) => ({
      name: el.get('.project-name').text(),
      tone: el.classes().find((c) => c.startsWith('tone-')) ?? '',
    }))
    expect(rows.find((row) => row.name === '暂停中')?.tone).toBe('tone-paused')
    for (const row of rows.filter((r) => r.name !== '暂停中')) {
      expect(row.tone).not.toBe('tone-paused')
      expect(row.tone).not.toBe('tone-done')
    }
  })

  it('已完成仍用状态保留色', async () => {
    const wrapper = render()

    await wrapper.get('[role="tab"]:nth-child(3)').trigger('click')

    expect(tones(wrapper)).toEqual(['tone-done'])
  })

  it('同屏的进行中项目颜色互不相同，且同一项目每次渲染颜色不变', async () => {
    const first = render({ flow: mixedFlow })
    const second = render({ flow: mixedFlow })

    const colors = tones(first)
    expect(new Set(colors).size).toBe(colors.length)
    expect(colors.every((tone) => /^tone-[1-4]$/.test(tone))).toBe(true)
    expect(colors).toEqual(tones(second))
  })
})
