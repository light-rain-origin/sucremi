import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
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
    expect(wrapper.find('clipPath').exists()).toBe(true)
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

  it('keeps the clip geometry mounted while the full circle takes over', async () => {
    const frameCallbacks: FrameRequestCallback[] = []
    vi.spyOn(window, 'requestAnimationFrame').mockImplementation((callback) => {
      frameCallbacks.push(callback)
      return frameCallbacks.length
    })
    vi.spyOn(window, 'cancelAnimationFrame').mockImplementation(() => undefined)
    vi.spyOn(performance, 'now').mockReturnValue(100)

    const wrapper = mount(DashboardProjectProgress, {
      props: {
        summary: {
          totalProjects: 1,
          activeProjects: 1,
          pausedProjects: 0,
          doneProjects: 0,
          archivedProjects: 0,
          recentProjects: [],
        },
      },
    })

    const firstFrame = frameCallbacks[frameCallbacks.length - 1]
    expect(firstFrame).toBeDefined()
    expect(wrapper.find('clipPath circle').attributes('r')).toBe('0')
    firstFrame?.(1060)
    await nextTick()

    const clipBeforeFinish = wrapper.find('clipPath path').attributes('d')
    expect(wrapper.find('clipPath circle').attributes('r')).toBe('100')

    const lastFrame = frameCallbacks[frameCallbacks.length - 1]
    lastFrame?.(1100)
    await nextTick()

    expect(wrapper.find('clipPath path').exists()).toBe(true)
    expect(wrapper.find('clipPath path').attributes('d')).toBe(clipBeforeFinish)
    expect(wrapper.find('clipPath circle').attributes('r')).toBe('100')
    expect(wrapper.findAll('.donut-segment')).toHaveLength(1)

    wrapper.unmount()
    vi.restoreAllMocks()
  })
})
