import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'
import DashboardView from '@/views/DashboardView.vue'
import ProjectsView from '@/views/ProjectsView.vue'
import ProjectDetailView from '@/views/ProjectDetailView.vue'
import EmptyModuleView from '@/views/EmptyModuleView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', name: 'dashboard', component: DashboardView, meta: { title: '概览' } },
        { path: 'projects', name: 'projects', component: ProjectsView, meta: { title: '项目' } },
        {
          path: 'development-records',
          name: 'development-records',
          component: EmptyModuleView,
          meta: { title: '开发记录' },
        },
        {
          path: 'knowledge',
          name: 'knowledge',
          component: EmptyModuleView,
          meta: { title: '知识沉淀' },
        },
        { path: 'remi', name: 'remi', component: EmptyModuleView, meta: { title: '魔法蕾米' } },
        {
          path: 'library',
          name: 'library',
          component: EmptyModuleView,
          meta: { title: '资料文库' },
        },
        {
          path: 'settings',
          name: 'settings',
          component: EmptyModuleView,
          meta: { title: '系统设置' },
        },
        {
          path: 'projects/:id',
          name: 'project-detail',
          component: ProjectDetailView,
          meta: { title: '项目详情' },
        },
      ],
    },
  ],
})

export default router
