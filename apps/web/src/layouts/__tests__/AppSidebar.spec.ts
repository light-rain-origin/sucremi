import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AppSidebar from '../AppSidebar.vue'

describe('AppSidebar', () => {
  it('renders brand and nav items', () => {
    const wrapper = mount(AppSidebar, {
      global: { stubs: { RouterLink: { template: '<a><slot /></a>' } } },
    })
    expect(wrapper.text()).toContain('Sucremi')
    expect(wrapper.text()).toContain('概览')
    expect(wrapper.text()).toContain('项目')
  })
})
