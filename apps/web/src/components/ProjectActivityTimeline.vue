<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { getProjectActivities, getRecentActivities } from '@/api/activity'
import type { ProjectActivity, ProjectActivityType } from '@/types/activity'
import { formatDateTime } from '@/utils/datetime'

const props = withDefaults(defineProps<{ projectId?: number; compact?: boolean }>(), { compact: false })
const events = ref<ProjectActivity[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const page = ref(1)
const size = computed(() => props.compact ? 5 : 10)
const total = ref(0)
const filter = ref<ProjectActivityType | ''>('')
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

const eventLabels: Record<ProjectActivityType, string> = {
  PROJECT_CREATED: '创建了项目',
  PROJECT_STATUS_CHANGED: '更新了项目状态',
  PROJECT_ARCHIVED: '归档了项目',
  PROJECT_UNARCHIVED: '取消归档项目',
  CHECKPOINT_CREATED: '保存了检查点',
  ACTION_CREATED: '添加了下一步行动',
  ACTION_COMPLETED: '完成了下一步行动',
  ACTION_REOPENED: '重新打开了下一步行动',
}

const filterOptions: Array<{ label: string; value: ProjectActivityType | '' }> = [
  { label: '全部活动', value: '' },
  { label: '项目创建', value: 'PROJECT_CREATED' },
  { label: '项目状态', value: 'PROJECT_STATUS_CHANGED' },
  { label: '归档操作', value: 'PROJECT_ARCHIVED' },
  { label: '取消归档', value: 'PROJECT_UNARCHIVED' },
  { label: '检查点', value: 'CHECKPOINT_CREATED' },
  { label: '行动', value: 'ACTION_CREATED' },
  { label: '行动完成', value: 'ACTION_COMPLETED' },
  { label: '重新打开行动', value: 'ACTION_REOPENED' },
]

function eventLabel(event: ProjectActivity) {
  if (event.eventType === 'PROJECT_STATUS_CHANGED') {
    try {
      const metadata = JSON.parse(event.metadata) as { from?: string; to?: string }
      const statusLabels: Record<string, string> = { ACTIVE: '进行中', PAUSED: '已暂停', DONE: '已完成' }
      if (metadata.from && metadata.to) {
        return `状态从${statusLabels[metadata.from] ?? metadata.from}调整为${statusLabels[metadata.to] ?? metadata.to}`
      }
    } catch {
      // 元数据不可解析时显示通用事件说明。
    }
  }
  return eventLabels[event.eventType] ?? '更新了项目'
}

async function load() {
  loading.value = true
  error.value = null
  try {
    if (props.projectId !== undefined) {
      const response = await getProjectActivities(props.projectId, { page: page.value, size: size.value, type: filter.value })
      if (response.code !== 0) throw new Error(response.message || '活动读取失败')
      events.value = response.data.list
      total.value = response.data.total
    } else {
      const response = await getRecentActivities(5)
      if (response.code !== 0) throw new Error(response.message || '活动读取失败')
      events.value = response.data
      total.value = events.value.length
    }
  } catch (reason) {
    error.value = (reason as Error).message || '活动读取失败'
  } finally {
    loading.value = false
  }
}

function changeFilter() {
  page.value = 1
  load()
}

function changePage(nextPage: number) {
  page.value = Math.min(Math.max(nextPage, 1), pageCount.value)
  load()
}

watch(() => props.projectId, load)
onMounted(load)
</script>

<template>
  <section class="activity-timeline" :class="{ compact }" :aria-busy="loading">
    <header v-if="!compact" class="timeline-toolbar">
      <p>按时间记录项目中的重要变化。</p>
      <label class="filter-label">
        <span>筛选</span>
        <select v-model="filter" aria-label="按活动类型筛选" @change="changeFilter">
          <option v-for="option in filterOptions" :key="option.label" :value="option.value">{{ option.label }}</option>
        </select>
      </label>
    </header>

    <div v-if="loading" class="timeline-state" role="status">正在加载活动…</div>
    <div v-else-if="error" class="timeline-state error-state" role="alert">
      <span>{{ error }}</span><button type="button" @click="load">重试</button>
    </div>
    <div v-else-if="events.length === 0" class="timeline-state empty-state">
      <span class="empty-star" aria-hidden="true">✦</span>
      <strong>还没有活动记录</strong>
      <span>项目有进展时，重要变化会自动留在这里。</span>
    </div>
    <ol v-else class="timeline-list">
      <li v-for="event in events" :key="event.id" class="timeline-item">
        <span class="timeline-dot" aria-hidden="true"></span>
        <RouterLink v-if="compact" class="event-copy event-link" :to="`/projects/${event.projectId}`">
          <strong class="event-project">{{ event.projectName }}</strong>
          <span class="event-label">{{ eventLabel(event) }}</span>
          <time :datetime="event.createdAt">{{ formatDateTime(event.createdAt) }}</time>
        </RouterLink>
        <div v-else class="event-copy">
          <strong class="event-project">{{ event.projectName }}</strong>
          <span class="event-label">{{ eventLabel(event) }}</span>
          <time :datetime="event.createdAt">{{ formatDateTime(event.createdAt) }}</time>
        </div>
      </li>
    </ol>
    <nav v-if="!compact && total > size" class="timeline-pagination" aria-label="活动分页">
      <button type="button" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
      <span>{{ page }} / {{ pageCount }}</span>
      <button type="button" :disabled="page >= pageCount" @click="changePage(page + 1)">下一页</button>
    </nav>
  </section>
</template>

<style scoped>
.activity-timeline { min-width: 0; color: var(--sc-text); }
.timeline-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 14px; margin: 0 0 18px; }
.timeline-toolbar p { margin: 0; color: var(--sc-text-secondary); font-size: var(--sc-font-size-md); }
.filter-label { display: flex; align-items: center; gap: 8px; color: var(--sc-text-secondary); font-size: var(--sc-font-size-sm); }
.filter-label select { padding: 7px 28px 7px 10px; border: 1px solid var(--sc-border-soft); border-radius: var(--sc-radius-md); background: var(--sc-card-surface); color: var(--sc-text); font: inherit; }
.timeline-list { position: relative; display: grid; gap: 0; margin: 0; padding: 4px 0 4px 9px; list-style: none; }
.timeline-list::before { position: absolute; top: 11px; bottom: 12px; left: 13px; width: 1px; background: color-mix(in srgb, var(--sc-brand-alt) 28%, var(--sc-border-soft)); content: ''; }
.timeline-item { position: relative; display: flex; gap: 14px; min-width: 0; padding: 0 0 20px; }
.timeline-item:last-child { padding-bottom: 0; }
.timeline-dot { z-index: 1; flex: 0 0 9px; width: 9px; height: 9px; margin: 5px 0 0; border: 2px solid var(--sc-card-surface); border-radius: 50%; background: var(--sc-brand-alt); box-shadow: 0 0 0 1px color-mix(in srgb, var(--sc-brand-alt) 50%, transparent); }
.event-copy { display: flex; min-width: 0; flex: 1; flex-wrap: wrap; align-items: baseline; gap: 4px 8px; }
.event-project { flex-basis: 100%; overflow: hidden; color: var(--sc-text); font-size: var(--sc-font-size-md); text-overflow: ellipsis; white-space: nowrap; }
.event-label { color: var(--sc-text-secondary); font-size: var(--sc-font-size-md); }
.event-copy time { color: var(--sc-text-placeholder); font-size: var(--sc-font-size-sm); }
.event-link { color: inherit; text-decoration: none; }
.event-link:hover .event-project, .event-link:hover .event-label { color: var(--sc-brand-alt-strong); }
.timeline-state { display: flex; min-height: 120px; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: var(--sc-text-secondary); text-align: center; font-size: var(--sc-font-size-md); }
.empty-star { color: var(--sc-brand-alt); font-size: 22px; }
.empty-state strong { color: var(--sc-text); font-weight: 600; }
.timeline-state button, .timeline-pagination button { padding: 6px 11px; border: 1px solid var(--sc-border-strong); border-radius: var(--sc-radius-pill); background: var(--sc-panel); color: var(--sc-brand-alt-strong); font: inherit; cursor: pointer; }
.error-state { color: var(--sc-error-strong); }
.timeline-pagination { display: flex; align-items: center; justify-content: flex-end; gap: 12px; margin-top: 14px; color: var(--sc-text-secondary); font-size: var(--sc-font-size-sm); }
.timeline-pagination button:disabled { opacity: .45; cursor: default; }
.compact .timeline-list { padding-top: 11px; }
.compact .timeline-item { gap: 11px; padding-bottom: 15px; }
.compact .event-copy { flex-direction: column; align-items: flex-start; gap: 2px; }
.compact .event-project { font-size: 10px; font-weight: 600; }
.compact .event-label { overflow: hidden; max-width: 100%; color: var(--sc-text); text-overflow: ellipsis; white-space: nowrap; font-size: var(--sc-font-size-sm); }
.compact .event-copy time { font-size: 10px; }
.compact .timeline-state { min-height: 180px; padding: 10px; }
@media (max-width: 600px) { .timeline-toolbar { align-items: flex-start; flex-direction: column; } }
</style>
