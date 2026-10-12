import { describe, it, expect } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import ProjectFormDialog from '../ProjectFormDialog.vue'
import type { ProjectForm } from '@/types/project'

type Wrapper = ReturnType<typeof mount>

function mountDialog(props: Record<string, unknown> = {}) {
  return mount(ProjectFormDialog, {
    props: { modelValue: false, mode: 'create', ...props },
    global: { plugins: [ElementPlus] },
  })
}

/** el-dialog 懒渲染：先以关闭状态挂载，再打开，模拟真实使用路径。 */
async function open(wrapper: Wrapper) {
  await wrapper.setProps({ modelValue: true })
  await flushPromises()
}

function footerButton(wrapper: Wrapper, text: string) {
  const button = wrapper.findAll('.el-dialog__footer button').find((b) => b.text().includes(text))
  expect(button, `未找到「${text}」按钮`).toBeDefined()
  return button!
}

describe('ProjectFormDialog', () => {
  it('prefills fields in edit mode', async () => {
    const initial: Partial<ProjectForm> = {
      name: '旧名称',
      summary: '旧简介',
      status: 'DONE',
      progress: 80,
    }
    const wrapper = mountDialog({ mode: 'edit', initial })
    await open(wrapper)

    expect(wrapper.find('input').element.value).toBe('旧名称')
    expect(wrapper.find('textarea').element.value).toBe('旧简介')
  })

  it('does not emit submit when the name is blank', async () => {
    const wrapper = mountDialog()
    await open(wrapper)

    await footerButton(wrapper, '保存').trigger('click')
    await flushPromises()

    expect(wrapper.emitted('submit')).toBeUndefined()
  })

  it('emits the full form payload when valid', async () => {
    const wrapper = mountDialog({
      mode: 'edit',
      initial: { name: '项目A', status: 'PAUSED', progress: 30 },
    })
    await open(wrapper)

    await wrapper.find('input').setValue('项目B')
    await footerButton(wrapper, '保存').trigger('click')
    await flushPromises()

    expect(wrapper.emitted('submit')).toEqual([
      [{ name: '项目B', summary: '', goal: '', status: 'PAUSED', progress: 30 }],
    ])
  })

  it('emits update:modelValue false when cancelled', async () => {
    const wrapper = mountDialog()
    await open(wrapper)

    await footerButton(wrapper, '取消').trigger('click')
    await flushPromises()

    expect(wrapper.emitted('update:modelValue')).toEqual([[false]])
    expect(wrapper.emitted('submit')).toBeUndefined()
  })
})
