import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import ProjectCard from '../ProjectCard.vue'
import type { Project } from '@/types/project'

const project: Project = {
  id: 7,
  name: '烘焙小程序V2',
  summary: '做一个能下单的小程序',
  goal: '上线并推广',
  status: 'ACTIVE',
  progress: 55,
  archived: false,
  createdAt: '2026-10-12T11:34:37',
  updatedAt: '2026-10-12T11:34:37',
}

function render(p: Project = project) {
  return mount(ProjectCard, {
    props: { project: p },
    global: { plugins: [ElementPlus] },
  })
}

describe('ProjectCard', () => {
  it('shows name, summary, status and progress', () => {
    const text = render().text()
    expect(text).toContain('烘焙小程序V2')
    expect(text).toContain('做一个能下单的小程序')
    expect(text).toContain('进行中')
    expect(text).toContain('55%')
  })

  it('falls back when summary is empty', () => {
    expect(render({ ...project, summary: null }).text()).toContain('暂无简介')
  })

  it('emits open with the project id on click', async () => {
    const wrapper = render()
    await wrapper.trigger('click')
    expect(wrapper.emitted('open')).toEqual([[7]])
  })
})
