import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import ProgressBar from '../ProgressBar.vue'

function render(value: number) {
  return mount(ProgressBar, { props: { value }, global: { plugins: [ElementPlus] } })
}

describe('ProgressBar', () => {
  it('renders the percentage text', () => {
    expect(render(40).find('.el-progress').exists()).toBe(true)
    expect(render(40).text()).toContain('40%')
  })

  it('renders 0% without breaking', () => {
    expect(render(0).text()).toContain('0%')
  })

  it('renders 100% as complete', () => {
    expect(render(100).text()).toContain('100%')
  })
})
