import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory, type Router } from 'vue-router'
import AppSidebar from '../AppSidebar.vue'

/*
 * 用真实路由而不是 stub：选中态取决于当前路径，stub 掉 RouterLink 就测不到
 * 「任何时刻只有一项亮起」这条 —— 而这正是概览项（to: '/'）在嵌套路由下会踩的坑。
 * 路由结构与 `src/router/index.ts` 保持一致，改那边记得同步这里。
 */
function makeRouter(): Router {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/',
        component: { template: '<div />' },
        children: [
          { path: '', name: 'dashboard', component: { template: '<div />' } },
          { path: 'projects', name: 'projects', component: { template: '<div />' } },
          { path: 'projects/:id', name: 'project-detail', component: { template: '<div />' } },
        ],
      },
    ],
  })
}

async function mountAt(path: string) {
  const router = makeRouter()
  await router.push(path)
  await router.isReady()
  return mount(AppSidebar, { global: { plugins: [router] } })
}

const labelsOf = (wrapper: Awaited<ReturnType<typeof mountAt>>, selector: string) =>
  wrapper.findAll(selector).map((el) => el.text().trim())

describe('AppSidebar', () => {
  it('renders brand and nav items', async () => {
    const wrapper = await mountAt('/')
    expect(wrapper.text()).toContain('Sucremi')
    expect(wrapper.text()).toContain('我的开发工作台')
    expect(wrapper.text()).toContain('首页概览')
    expect(wrapper.text()).toContain('项目管理')
  })

  it('七个条目全部按设计稿渲染，顺序一致', async () => {
    const wrapper = await mountAt('/')
    expect(labelsOf(wrapper, '.nav-label')).toEqual([
      '首页概览',
      '项目管理',
      '开发记录',
      '知识沉淀',
      '魔法蕾米',
      '资料文库',
      '系统设置',
    ])
  })

  it('只有已实现路由的两项可跳转，其余是不可点的占位项', async () => {
    const wrapper = await mountAt('/')
    const items = wrapper.findAll('.nav-item')
    expect(items).toHaveLength(7)

    const linked = items.filter((el) => el.element.tagName === 'A')
    expect(linked.map((el) => el.text().trim())).toEqual(['首页概览', '项目管理'])

    const inert = items.filter((el) => el.element.tagName === 'SPAN')
    expect(inert).toHaveLength(5)
    for (const el of inert) {
      expect(el.attributes('aria-disabled')).toBe('true')
      expect(el.classes()).toContain('nav-item-static')
    }
  })

  it('每条导航项都带图标轮廓与右侧标记槽', async () => {
    const wrapper = await mountAt('/')
    for (const item of wrapper.findAll('.nav-item')) {
      expect(item.find('.nav-icon-wrap .nav-glyph').exists()).toBe(true)
      expect(item.find('.nav-chevron').exists()).toBe(true)
      expect(item.find('.nav-dot').exists()).toBe(true)
    }
  })

  /*
   * 回归点：概览项指向 '/'，在嵌套路由下 RouterLink 认为它对任何子路由都「命中」，
   * 于是 /projects 上会同时点亮「首页概览」和「项目管理」，出现两个选中胶囊。
   */
  it('任何路径下都只有一项处于选中态', async () => {
    for (const path of ['/', '/projects', '/projects/42']) {
      const wrapper = await mountAt(path)
      const active = wrapper.findAll('.nav-item.active')
      expect(active, `路径 ${path} 的选中项数量`).toHaveLength(1)
    }
  })

  it('选中项与当前分区一致，子页面归属父分区', async () => {
    expect(labelsOf(await mountAt('/'), '.nav-item.active')).toEqual(['首页概览'])
    expect(labelsOf(await mountAt('/projects'), '.nav-item.active')).toEqual(['项目管理'])
    expect(labelsOf(await mountAt('/projects/42'), '.nav-item.active')).toEqual(['项目管理'])
  })

  it('点击可跳转项会通知父级收起抽屉', async () => {
    const wrapper = await mountAt('/')
    const items = wrapper.findAll('.nav-item')
    await items[1]!.trigger('click')
    expect(wrapper.emitted('navigate')).toHaveLength(1)

    // 占位项不可点，不该发事件
    await items[6]!.trigger('click')
    expect(wrapper.emitted('navigate')).toHaveLength(1)
  })
})
