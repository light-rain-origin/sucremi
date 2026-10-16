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
    expect(wrapper.findAll('.donut-reveal-mask')).toHaveLength(1)
    expect(wrapper.find('.donut-reveal-mask').attributes('d')).toBe(
      'M 75 10 A 65 65 0 0 1 75 140 A 65 65 0 0 1 75 10',
    )
    expect(wrapper.findAll('.donut-segment').every((segment) => segment.attributes('mask')?.startsWith('url(#'))).toBe(true)
  })

  it('keeps the empty track and zero percentages when there are no projects', () => {
    const wrapper = mount(DashboardProjectProgress, { props: { summary: null } })

    expect(wrapper.find('.donut-track').exists()).toBe(true)
    expect(wrapper.findAll('path.donut-segment')).toHaveLength(0)
    expect(wrapper.find('.donut-center').text()).toContain('0 个')
    expect(wrapper.findAll('.progress-legend strong').every((item) => item.text() === '0%')).toBe(true)
  })
})
