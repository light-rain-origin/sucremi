<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Project, ProjectStatus } from '@/types/project'
import { formatDateTime } from '@/utils/datetime'

const props = defineProps<{
  projects: Project[]
  activeCount: number
  pausedCount: number
  doneCount: number
}>()

const emit = defineEmits<{ open: [id: number] }>()

const tabs: { status: ProjectStatus; label: string }[] = [
  { status: 'ACTIVE', label: '进行中' },
  { status: 'PAUSED', label: '已暂停' },
  { status: 'DONE', label: '已完成' },
]

// 只筛选接口返回的最近项目；状态数量仍展示服务端统计的完整数量。
const selected = ref<ProjectStatus>('ACTIVE')
const visibleProjects = computed(() => props.projects.filter((project) => project.status === selected.value))
const counts = computed<Record<ProjectStatus, number>>(() => ({
  ACTIVE: props.activeCount,
  PAUSED: props.pausedCount,
  DONE: props.doneCount,
}))
</script>

<template>
  <el-card shadow="never" class="overview-card">
    <template #header>
      <div class="card-head">
        <div class="heading">
          <span class="eyebrow">PROJECTS</span>
          <h2>项目进度</h2>
        </div>
        <nav class="tabs" aria-label="项目状态">
          <button
            v-for="tab in tabs"
            :key="tab.status"
            class="tab"
            :class="{ active: selected === tab.status }"
            type="button"
            :aria-pressed="selected === tab.status"
            @click="selected = tab.status"
          >
            <span>{{ tab.label }}</span>
            <span class="tab-count">{{ counts[tab.status] }}</span>
          </button>
        </nav>
      </div>
    </template>

    <div v-if="visibleProjects.length" class="project-list">
      <button
        v-for="project in visibleProjects"
        :key="project.id"
        type="button"
        class="project-row"
        :class="`status-${project.status.toLowerCase()}`"
        @click="emit('open', project.id)"
      >
        <span class="project-main">
          <span class="project-name">{{ project.name }}</span>
          <span class="project-date">更新于 {{ formatDateTime(project.updatedAt) }}</span>
        </span>
        <span
          class="progress-track"
          role="progressbar"
          :aria-label="`${project.name}进度`"
          :aria-valuenow="project.progress"
          aria-valuemin="0"
          aria-valuemax="100"
        >
          <span class="progress-fill" :style="{ width: `${project.progress}%` }" />
        </span>
        <span class="progress-value">{{ project.progress }}%</span>
        <span class="row-arrow" aria-hidden="true">↗</span>
      </button>
    </div>
    <div v-else class="empty-state">
      <span class="empty-dot" aria-hidden="true" />
      <span>{{ counts[selected] ? '最近更新的项目中暂无此状态' : '这个状态下还没有项目' }}</span>
    </div>

    <template #footer>
      <div class="card-foot">
        <span class="legend"><i class="legend-dot" />项目进度</span>
        <RouterLink to="/projects" class="all-projects">查看全部项目 <span aria-hidden="true">→</span></RouterLink>
      </div>
    </template>
  </el-card>
</template>

<style scoped>
.overview-card {
  overflow: hidden;
}

.card-head,
.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.heading h2 {
  margin: 2px 0 0;
  color: var(--sc-text);
  font-size: var(--sc-font-size-xl);
  font-weight: 700;
}

.eyebrow {
  color: var(--sc-text-placeholder);
  font-size: var(--sc-font-size-xs);
  font-weight: 700;
  letter-spacing: 0.12em;
}

.tabs {
  display: flex;
  align-items: center;
  gap: 18px;
}

.tab {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 0 10px;
  border: 0;
  background: transparent;
  color: var(--sc-text-secondary);
  font: inherit;
  font-size: var(--sc-font-size-md);
  cursor: pointer;
}

.tab.active {
  color: var(--sc-brand-alt-strong);
  font-weight: 600;
}

.tab.active::after {
  position: absolute;
  right: 0;
  bottom: 2px;
  left: 0;
  height: 2px;
  border-radius: var(--sc-radius-pill);
  background: var(--sc-gradient-brand);
  content: '';
}

.tab-count {
  color: var(--sc-text-placeholder);
  font-size: var(--sc-font-size-xs);
  font-variant-numeric: tabular-nums;
}

.project-list {
  display: grid;
  gap: 10px;
}

.project-row {
  --row-color: var(--sc-brand);
  --row-soft: var(--sc-brand-soft);
  display: grid;
  grid-template-columns: minmax(150px, 1fr) minmax(90px, 0.8fr) 42px 20px;
  align-items: center;
  gap: 14px;
  width: 100%;
  min-height: 62px;
  padding: 10px 14px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-lg);
  background: linear-gradient(100deg, var(--row-soft), var(--sc-panel-strong) 58%);
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
}

.project-row:hover {
  transform: translateY(-1px);
  border-color: var(--row-color);
  box-shadow: var(--sc-shadow-sm);
}

.status-paused {
  --row-color: var(--sc-warning);
  --row-soft: var(--sc-stat-amber-soft);
}

.status-done {
  --row-color: var(--sc-success);
  --row-soft: var(--sc-stat-mint-soft);
}

.project-main {
  display: grid;
  min-width: 0;
  gap: 2px;
}

.project-name {
  overflow: hidden;
  color: var(--sc-text);
  font-size: var(--sc-font-size-lg);
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-date {
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-xs);
}

.progress-track {
  overflow: hidden;
  height: 7px;
  border-radius: var(--sc-radius-pill);
  background: var(--sc-border-soft);
}

.progress-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--sc-gradient-brand);
}

.status-paused .progress-fill {
  background: linear-gradient(90deg, #f0c778, var(--sc-warning));
}

.status-done .progress-fill {
  background: linear-gradient(90deg, #8acda0, var(--sc-success));
}

.progress-value {
  color: var(--sc-text);
  font-size: var(--sc-font-size-sm);
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  text-align: right;
}

.row-arrow {
  color: var(--sc-text-placeholder);
  font-size: var(--sc-font-size-lg);
  text-align: right;
  transition: color 0.18s ease, transform 0.18s ease;
}

.project-row:hover .row-arrow {
  transform: translate(2px, -2px);
  color: var(--row-color);
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  min-height: 150px;
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-md);
}

.empty-dot,
.legend-dot {
  width: 8px;
  height: 8px;
  flex: 0 0 auto;
  border-radius: 50%;
  background: var(--sc-brand);
}

.card-foot {
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-sm);
}

.legend {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.all-projects {
  color: var(--sc-brand-alt-strong);
  font-weight: 600;
}

.all-projects:hover {
  color: var(--sc-brand-strong);
}

@media (max-width: 640px) {
  .card-head {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .tabs {
    width: 100%;
    justify-content: space-between;
    gap: 10px;
  }

  .project-row {
    grid-template-columns: minmax(0, 1fr) 38px 16px;
    gap: 10px;
  }

  .progress-track {
    grid-column: 1 / 2;
    grid-row: 2;
    width: 100%;
  }

  .project-main {
    grid-column: 1 / 2;
    grid-row: 1;
  }

  .progress-value {
    grid-column: 2;
    grid-row: 1 / 3;
  }

  .row-arrow {
    grid-column: 3;
    grid-row: 1 / 3;
  }
}

@media (prefers-reduced-motion: reduce) {
  .project-row,
  .row-arrow {
    transition: none;
  }
}
</style>
