import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import EmptyState from '../EmptyState.vue'

describe('EmptyState', () => {
  it('shows default text', () => {
    expect(mount(EmptyState).text()).toContain('暂无数据')
  })

  it('shows custom text', () => {
    expect(mount(EmptyState, { props: { text: '还没有项目' } }).text()).toContain('还没有项目')
  })
})
