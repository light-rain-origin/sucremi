<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/stores/dashboard'
import DashboardBanner from '@/components/DashboardBanner.vue'
import DashboardStatsCards from '@/components/DashboardStatsCards.vue'
import DashboardProjectProgress from '@/components/DashboardProjectProgress.vue'
import ProjectFlowCard from '@/components/ProjectFlowCard.vue'
import ContinueWorkCard from '@/components/ContinueWorkCard.vue'
import ProjectActivityTimeline from '@/components/ProjectActivityTimeline.vue'

const router = useRouter()
const dashboard = useDashboardStore()
const { summary, projectFlow, projectFlowLoading, projectFlowError } = storeToRefs(dashboard)

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
      <div class="dashboard-primary-group">
        <ProjectFlowCard
          :flow="projectFlow"
          :loading="projectFlowLoading"
          :error="projectFlowError"
          @open="openDetail"
          @view-all="router.push('/projects')"
        />
        <DashboardProjectProgress :summary="summary" />
        <div class="continue-work-slot">
          <ContinueWorkCard
            :projects="projectFlow?.projects ?? []"
            :projects-loading="projectFlowLoading || !projectFlow"
            :projects-error="projectFlowError"
            @retry-projects="dashboard.fetchProjectFlow()"
          />
        </div>
      </div>
      <aside class="dashboard-third-column" aria-label="待办与近期活动">
        <section class="dashboard-mini-panel todo-panel" aria-labelledby="dashboard-todo-title">
          <header class="dashboard-mini-header">
            <span class="dashboard-mini-icon todo-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24"><path d="M8 5.5h11M8 12h11M8 18.5h7"/><path d="m3.5 5.5 1.2 1.2 2-2.4M3.5 12l1.2 1.2 2-2.4M3.5 18.5l1.2 1.2 2-2.4"/></svg>
            </span>
            <h2 id="dashboard-todo-title">我的待办</h2>
          </header>
          <div class="dashboard-mini-empty">
            <span class="empty-mark" aria-hidden="true">✓</span>
            <strong>暂无待办事项</strong>
            <p>记下下一步，回来时更容易接上进度。</p>
          </div>
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
      </aside>
    </div>
  </section>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.dashboard-lower-row {
  display: grid;
  width: 100%;
  grid-template-columns: minmax(0, 1106.72px) minmax(0, 1fr);
  align-items: start;
  column-gap: 20px;
  row-gap: 18px;
}

.dashboard-primary-group {
  display: grid;
  min-width: 0;
  grid-template-columns: minmax(0, 666.72px) 420px;
  column-gap: 20px;
  row-gap: 18px;
}

.continue-work-slot {
  min-width: 0;
  grid-column: 1 / -1;
}

.dashboard-third-column {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 18px;
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
  min-height: 260px;
}

.activity-panel {
  min-height: 315px;
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
  min-height: 0;
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

@media (max-width: 1784px) {
  .dashboard-lower-row {
    width: 100%;
    grid-template-columns: minmax(0, 1fr);
  }

  .dashboard-primary-group {
    grid-template-columns: minmax(0, 1fr) 370px;
  }

  .dashboard-third-column {
    width: min(100%, 420px);
    grid-column: 1;
    justify-self: end;
  }
}

@media (max-width: 900px) {
  .dashboard-lower-row {
    grid-template-columns: minmax(0, 1fr);
  }

  .dashboard-primary-group {
    grid-template-columns: minmax(0, 1fr);
  }

  .continue-work-slot {
    grid-column: auto;
  }

  .dashboard-third-column {
    width: 100%;
    grid-column: auto;
    justify-self: stretch;
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
