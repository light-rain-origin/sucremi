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

  it('每个项目的连线跟随自己胶囊的颜色，合并后的主线不跟', () => {
    const wrapper = render({ flow: mixedFlow })

    const lines = wrapper
      .findAll('path.connector-line')
      .filter((el) => !el.classes().includes('connector-summary'))
      .map((el) => el.classes().find((c) => c.startsWith('tone-')))

    expect(lines).toEqual(tones(wrapper))
    expect(wrapper.findAll('path.connector-summary').every((el) => !el.classes().some((c) => c.startsWith('tone-')))).toBe(true)
  })

  it('连线是 S 形：两端切线水平，中段斜切', () => {
    const wrapper = render({ flow: mixedFlow })

    const lines = wrapper
      .findAll('path.connector-line')
      .filter((el) => !el.classes().includes('connector-summary'))

    // 三行的中心是 35 / 90 / 145。第一个控制点与起点同高、第二个与合并点同高，
    // 两端切线因此水平；控制点各占 36px，中段被挤到约 52° —— 这就是 S 的弧度。
    expect(lines.map((el) => el.attributes('d'))).toEqual([
      'M286 35 C322 35 329 90 365 90',
      'M286 90 C322 90 329 90 365 90',
      'M286 145 C322 145 329 90 365 90',
    ])
  })

  it('每根线在中点各带一个箭头，与合并处的箭头共用一个三角形', () => {
    const wrapper = render({ flow: mixedFlow })
    const TRIANGLE = 'M-7 -3.5L0 0L-7 3.5Z'

    const arrows = wrapper.findAll('.connector-arrow')
    expect(arrows).toHaveLength(tones(wrapper).length)
    expect(arrows.map((el) => el.classes().find((c) => c.startsWith('tone-')))).toEqual(tones(wrapper))
    // 中点在 x = 325.5（与跨度无关），角度跟着那一行的落差走：上行 +52°、中间行水平、下行 −52°
    expect(arrows.map((el) => el.attributes('transform'))).toEqual([
      'translate(325.5 62.5) rotate(52)',
      'translate(325.5 90) rotate(0)',
      'translate(325.5 117.5) rotate(-52)',
    ])

    // 同一个 d + 只做平移 ⇒ 与合并处的箭头必然同尺寸
    expect(arrows.every((el) => el.attributes('d') === TRIANGLE)).toBe(true)
    const junction = wrapper.get('.junction-arrow')
    expect(junction.attributes('d')).toBe(TRIANGLE)
    expect(junction.attributes('transform')).toBe('translate(365 90)')
    expect(wrapper.find('.connectors circle').exists()).toBe(false)
  })

  it('合并后的干线自身不带箭头，箭头只留在合并处', () => {
    const wrapper = render({ flow: mixedFlow })

    const summary = wrapper.findAll('path.connector-summary')
    // 干线从合并点（365, 90）＝ 箭头尖端起步，末端不再有第二个箭头
    const trunk = summary.find((el) => el.attributes('d') === 'M365 90H431')!
    expect(trunk.attributes('marker-end')).toBeUndefined()
    // 指向「平均进度」的那根下行箭头保留
    const down = summary.find((el) => el.attributes('d') === 'M493 109V130')!
    expect(down.attributes('marker-end')).toContain('arrow-down')
  })
})
