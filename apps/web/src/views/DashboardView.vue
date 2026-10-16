<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/stores/dashboard'
import DashboardBanner from '@/components/DashboardBanner.vue'
import DashboardStatsCards from '@/components/DashboardStatsCards.vue'
import DashboardProjectProgress from '@/components/DashboardProjectProgress.vue'
import ProjectFlowCard from '@/components/ProjectFlowCard.vue'
import ContinueWorkCard from '@/components/ContinueWorkCard.vue'
import ProjectActivityTimeline from '@/components/ProjectActivityTimeline.vue'
import { getProjectRecovery } from '@/api/recovery'
import type { ProjectAction } from '@/types/recovery'
import type { Project } from '@/types/project'

const router = useRouter()
const dashboard = useDashboardStore()
const { summary, projectFlow, projectFlowLoading, projectFlowError } = storeToRefs(dashboard)

interface DashboardTodo {
  action: ProjectAction
  project: Project
}

const todos = ref<DashboardTodo[]>([])
const todosLoading = ref(false)
const todosError = ref<string | null>(null)
const projectIds = computed(() => (projectFlow.value?.projects ?? []).map((project) => project.id).join(','))
let todoRequestSequence = 0

async function loadTodos() {
  const sequence = ++todoRequestSequence
  todos.value = []
  todosError.value = null

  if (projectFlowLoading.value || !projectFlow.value) {
    todosLoading.value = projectFlowLoading.value
    return
  }
  if (projectFlowError.value) {
    todosLoading.value = false
    todosError.value = projectFlowError.value
    return
  }

  const projects = projectFlow.value.projects
  if (!projects.length) {
    todosLoading.value = false
    return
  }

  todosLoading.value = true
  try {
    const recoveries = await Promise.all(projects.map(async (project) => {
      const response = await getProjectRecovery(project.id)
      if (response.code !== 0) throw new Error(response.message || '待办事项读取失败')
      return { project, actions: response.data.actions }
    }))
    if (sequence !== todoRequestSequence) return

    todos.value = recoveries
      .flatMap(({ project, actions }) => actions
        .filter((action) => action.status === 'TODO')
        .map((action) => ({ action, project })))
      .sort((a, b) =>
        new Date(b.action.createdAt).getTime() - new Date(a.action.createdAt).getTime() ||
        b.action.id - a.action.id,
      )
      .slice(0, 5)
  } catch (reason) {
    if (sequence === todoRequestSequence) todosError.value = (reason as Error).message || '待办事项读取失败'
  } finally {
    if (sequence === todoRequestSequence) todosLoading.value = false
  }
}

watch([projectIds, projectFlowLoading, projectFlowError], loadTodos, { immediate: true })

onMounted(() => {
  dashboard.fetchProjectFlow()
  dashboard.fetchSummary()
})

function openDetail(id: number) {
  router.push(`/projects/${id}`)
}
</script>

<template>
  <section class="dashboard" aria-label="首页概览">
    <DashboardBanner />
    <DashboardStatsCards :summary="summary" />
    <div class="dashboard-lower-row">
      <ProjectFlowCard
        :flow="projectFlow"
        :loading="projectFlowLoading"
        :error="projectFlowError"
        @open="openDetail"
        @view-all="router.push('/projects')"
      />

      <div class="progress-slot">
        <DashboardProjectProgress :summary="summary" />
      </div>

      <div class="dashboard-right-stack">
        <div class="continue-work-slot">
          <ContinueWorkCard
            :projects="projectFlow?.projects ?? []"
            :projects-loading="projectFlowLoading || !projectFlow"
            :projects-error="projectFlowError"
            @retry-projects="dashboard.fetchProjectFlow()"
          />
        </div>

        <section class="dashboard-mini-panel todo-panel" aria-labelledby="dashboard-todo-title" :aria-busy="todosLoading">
          <header class="dashboard-mini-header">
            <span class="dashboard-mini-icon todo-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24"><path d="M8 5.5h11M8 12h11M8 18.5h7"/><path d="m3.5 5.5 1.2 1.2 2-2.4M3.5 12l1.2 1.2 2-2.4M3.5 18.5l1.2 1.2 2-2.4"/></svg>
            </span>
            <h2 id="dashboard-todo-title">我的待办</h2>
            <span v-if="todos.length" class="todo-count">{{ todos.length }} 项</span>
          </header>

          <div v-if="todosLoading" class="todo-state" role="status">正在整理待办事项…</div>
          <div v-else-if="todosError" class="todo-state todo-error" role="alert">
            <span>{{ todosError }}</span>
            <button type="button" @click="loadTodos">重试</button>
          </div>
          <div v-else-if="todos.length === 0" class="dashboard-mini-empty">
            <span class="empty-mark" aria-hidden="true">✓</span>
            <strong>暂无待办事项</strong>
            <p>记下下一步，回来时更容易接上进度。</p>
          </div>
          <ul v-else class="todo-list">
            <li v-for="todo in todos" :key="`${todo.project.id}-${todo.action.id}`" class="todo-item">
              <span class="todo-check" aria-hidden="true"></span>
              <RouterLink :to="`/projects/${todo.project.id}/recovery`" class="todo-link">
                <span class="todo-title">{{ todo.action.title }}</span>
                <span class="todo-project">{{ todo.project.name }}</span>
              </RouterLink>
              <span class="todo-arrow" aria-hidden="true">›</span>
            </li>
          </ul>
        </section>

        <section class="dashboard-mini-panel activity-panel" aria-labelledby="dashboard-activity-title">
          <header class="dashboard-mini-header">
            <span class="dashboard-mini-icon activity-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24"><path d="M4 19.5h16M6.5 16V9.5M12 16V5.5M17.5 16v-4"/><path d="m5.5 7 5-3 5 2 3-2"/></svg>
            </span>
            <h2 id="dashboard-activity-title">近期活动</h2>
          </header>
          <ProjectActivityTimeline compact />
        </section>
      </div>
    </div>
  </section>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 18px;
  container-type: inline-size;
}

.dashboard-lower-row {
  display: grid;
  width: 100%;
  grid-template-columns: minmax(0, 666.72px) minmax(320px, 420px) minmax(320px, 1fr);
  grid-template-areas: 'recent progress right-stack';
  grid-template-rows: auto;
  align-items: start;
  column-gap: 20px;
  row-gap: 18px;
}

.dashboard-lower-row :deep(.project-flow-card) {
  grid-area: recent;
}

.continue-work-slot {
  min-width: 0;
  container-type: inline-size;
}

.continue-work-slot :deep(.continue-card) {
  min-height: 220px;
  height: auto;
  box-sizing: border-box;
}

.dashboard-right-stack {
  display: flex;
  min-width: 0;
  min-height: 0;
  grid-area: right-stack;
  flex-direction: column;
  gap: 18px;
}

.progress-slot {
  grid-area: progress;
  min-width: 0;
}

.progress-slot :deep(.dashboard-side-card) {
  height: 315px;
  min-height: 315px;
  box-sizing: border-box;
}

.dashboard-mini-panel {
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 20px 18px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-xl);
  background: var(--sc-card-surface);
  box-shadow: var(--sc-shadow-md);
}

.todo-panel {
  box-sizing: border-box;
  container-type: inline-size;
}

.activity-panel {
  min-height: 315px;
  flex: 1 1 auto;
}

.dashboard-mini-header {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 9px;
}

.dashboard-mini-header h2 {
  overflow: hidden;
  margin: 0;
  color: var(--sc-text);
  font-size: 15px;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-count {
  margin-left: auto;
  padding: 4px 9px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--sc-brand-alt) 12%, var(--sc-card-surface));
  color: var(--sc-brand-alt-strong);
  font-size: 11px;
  font-weight: 600;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 184px;
  overflow-y: auto;
  margin: 14px 0 0;
  padding: 0;
  list-style: none;
  overscroll-behavior: contain;
  scrollbar-color: var(--sc-border-strong) transparent;
  scrollbar-width: thin;
}

.todo-item {
  display: flex;
  height: 56px;
  min-width: 0;
  flex: 0 0 56px;
  align-items: center;
  gap: 10px;
  padding: 10px 11px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-lg);
  background: color-mix(in srgb, var(--sc-panel) 78%, transparent);
  box-sizing: border-box;
}

.todo-check {
  width: 14px;
  height: 14px;
  flex: 0 0 14px;
  border: 1.5px solid var(--sc-brand-alt);
  border-radius: 50%;
}

.todo-link {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 3px;
}

.todo-title,
.todo-project {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-title {
  color: var(--sc-text);
  font-size: 13px;
  font-weight: 550;
}

.todo-project {
  color: var(--sc-text-secondary);
  font-size: 11px;
}

.todo-arrow {
  color: var(--sc-text-placeholder);
  font-size: 20px;
  line-height: 1;
}

.todo-state {
  display: flex;
  min-height: 130px;
  flex: 1;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--sc-text-secondary);
  font-size: 12px;
  text-align: center;
}

.todo-error {
  flex-direction: column;
  color: var(--sc-error-strong);
}

.todo-error button {
  padding: 6px 12px;
  border: 1px solid var(--sc-border-strong);
  border-radius: var(--sc-radius-pill);
  background: var(--sc-panel-strong);
  color: var(--sc-brand-alt-strong);
  font: inherit;
  cursor: pointer;
}

.dashboard-mini-icon {
  display: grid;
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
  place-items: center;
  border-radius: 11px;
}

.dashboard-mini-icon svg {
  width: 17px;
  height: 17px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.7;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.todo-icon {
  color: #9b86f8;
  background: color-mix(in srgb, currentColor 14%, var(--sc-card-surface));
}

.activity-icon {
  color: #e985bb;
  background: color-mix(in srgb, currentColor 14%, var(--sc-card-surface));
}

.activity-panel :deep(.activity-timeline) {
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1;
}

.activity-panel :deep(.timeline-list) {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: space-evenly;
}

.activity-panel :deep(.timeline-state) {
  flex: 1;
}

.dashboard-mini-empty {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 6px 0;
  text-align: center;
}

.dashboard-mini-empty .empty-mark {
  display: grid;
  width: 38px;
  height: 38px;
  margin-bottom: 10px;
  place-items: center;
  border-radius: 50%;
  background: var(--sc-bg-muted);
  color: var(--sc-brand-alt);
  font-size: 18px;
  font-weight: 700;
}

.dashboard-mini-empty strong {
  color: var(--sc-text);
  font-size: 13px;
  font-weight: 600;
}

.dashboard-mini-empty p {
  max-width: 180px;
  margin: 6px 0 0;
  color: var(--sc-text-secondary);
  font-size: 11px;
  line-height: 1.65;
}

@container (max-width: 1346px) {
  .dashboard-lower-row {
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
    grid-template-rows: auto auto;
    grid-template-areas:
      'recent progress'
      'right-stack right-stack';
  }

  .dashboard-lower-row :deep(.project-flow-card) {
    width: 100%;
  }

}

@container (max-width: 820px) {
  .dashboard-lower-row {
    grid-template-columns: minmax(0, 1fr);
    grid-template-rows: auto;
    grid-template-areas:
      'recent'
      'progress'
      'right-stack';
  }

  .dashboard-lower-row :deep(.project-flow-card) {
    width: 100%;
  }

  .progress-slot :deep(.dashboard-side-card) {
    height: auto;
  }
}

@container (max-width: 460px) {
  .continue-work-slot :deep(.saved-at) {
    display: none;
  }
}

@media (max-width: 560px) {
  .dashboard-mini-panel {
    padding: 18px 16px;
  }

  .activity-panel {
    min-height: 0;
  }
}
</style>
