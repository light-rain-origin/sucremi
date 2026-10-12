import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import StatusTag from '../StatusTag.vue'

describe('StatusTag', () => {
  it('maps ACTIVE to 进行中', () => {
    const wrapper = mount(StatusTag, {
      props: { status: 'ACTIVE' },
      global: { plugins: [ElementPlus] },
    })
    expect(wrapper.text()).toContain('进行中')
  })

  it('maps DONE to 完成', () => {
    const wrapper = mount(StatusTag, {
      props: { status: 'DONE' },
      global: { plugins: [ElementPlus] },
    })
    expect(wrapper.text()).toContain('完成')
  })
})
