import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import ProjectOverviewCard from '../ProjectOverviewCard.vue'
import type { Project } from '@/types/project'

const projects: Project[] = [
  {
    id: 1,
    name: 'Remi 工作台',
    summary: null,
    goal: null,
    status: 'ACTIVE',
    progress: 42,
    archived: false,
    createdAt: '2026-10-01T10:00:00',
    updatedAt: '2026-10-12T10:00:00',
  },
  {
    id: 2,
    name: '旧项目',
    summary: null,
    goal: null,
    status: 'DONE',
    progress: 100,
    archived: false,
    createdAt: '2026-09-01T10:00:00',
    updatedAt: '2026-10-10T10:00:00',
  },
]

function render() {
  return mount(ProjectOverviewCard, {
    props: { projects, activeCount: 1, pausedCount: 2, doneCount: 1 },
    global: {
      plugins: [ElementPlus],
      stubs: { RouterLink: { template: '<a><slot /></a>' } },
    },
  })
}

describe('ProjectOverviewCard', () => {
  it('shows project progress with server counts and filters by status', async () => {
    const wrapper = render()

    expect(wrapper.text()).toContain('Remi 工作台')
    expect(wrapper.text()).toContain('42%')
    expect(wrapper.text()).toContain('进行中1')
    expect(wrapper.text()).not.toContain('旧项目')

    await wrapper.get('button.tab:nth-child(3)').trigger('click')

    expect(wrapper.text()).toContain('旧项目')
    expect(wrapper.text()).toContain('100%')
    expect(wrapper.text()).not.toContain('Remi 工作台')
  })

  it('emits the selected project id when a project row is clicked', async () => {
    const wrapper = render()

    await wrapper.get('.project-row').trigger('click')

    expect(wrapper.emitted('open')).toEqual([[1]])
  })

  it('explains when a status has projects outside the recent five', async () => {
    const wrapper = render()

    await wrapper.get('button.tab:nth-child(2)').trigger('click')

    expect(wrapper.text()).toContain('最近更新的项目中暂无此状态')
  })
})
