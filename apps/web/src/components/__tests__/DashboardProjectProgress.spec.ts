import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardProjectProgress from '../DashboardProjectProgress.vue'

describe('DashboardProjectProgress', () => {
  it('draws one SVG segment for every project status with a count', () => {
    const wrapper = mount(DashboardProjectProgress, {
      props: {
        summary: {
          totalProjects: 5,
          activeProjects: 2,
          pausedProjects: 1,
          doneProjects: 1,
          archivedProjects: 1,
          recentProjects: [],
        },
      },
    })

    expect(wrapper.find('.donut-svg').exists()).toBe(true)
    expect(wrapper.findAll('path.donut-segment')).toHaveLength(4)
    expect(wrapper.find('.donut-center').text()).toContain('2 个')
    expect(wrapper.find('.progress-donut').attributes('aria-label')).toContain('共 6 个项目')
    expect(wrapper.find('.tone-active').exists()).toBe(true)
    expect(wrapper.find('.tone-paused').exists()).toBe(true)
    expect(wrapper.find('.tone-done').exists()).toBe(true)
    expect(wrapper.find('.tone-archived').exists()).toBe(true)
    expect(wrapper.find('.tone-active').attributes('d')).toContain('Q')
    expect(wrapper.findAll('clipPath path')).toHaveLength(1)
    expect(wrapper.find('clipPath path').attributes('d')).toContain('A 75 75')
    expect(wrapper.find('g[clip-path]').attributes('clip-path')).toMatch(/^url\(#.+\)$/)
  })

  it('keeps the empty track and zero percentages when there are no projects', () => {
    const wrapper = mount(DashboardProjectProgress, { props: { summary: null } })

    expect(wrapper.find('.donut-track').exists()).toBe(true)
    expect(wrapper.findAll('path.donut-segment')).toHaveLength(0)
    expect(wrapper.find('.donut-center').text()).toContain('0 个')
    expect(wrapper.findAll('.progress-legend strong').every((item) => item.text() === '0%')).toBe(true)
  })

  it('restarts the entrance animation when dashboard data arrives after an empty render', async () => {
    const wrapper = mount(DashboardProjectProgress, { props: { summary: null } })
    const emptySvg = wrapper.find('.donut-svg').element

    await wrapper.setProps({
      summary: {
        totalProjects: 1,
        activeProjects: 1,
        pausedProjects: 0,
        doneProjects: 0,
        archivedProjects: 0,
        recentProjects: [],
      },
    })

    expect(wrapper.find('.donut-svg').element).not.toBe(emptySvg)
    expect(wrapper.findAll('.donut-segment')).toHaveLength(1)
  })
})
