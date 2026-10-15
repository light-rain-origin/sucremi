<script setup lang="ts">
import { computed } from 'vue'
import type { DashboardSummary } from '@/types/dashboard'

const props = defineProps<{ summary: DashboardSummary | null }>()

const cards = computed(() => [
  { label: '全部项目', count: props.summary?.totalProjects ?? 0, note: '🌟一步一步积累', icon: 'folder', tone: 'rose' },
  { label: '进行中', count: props.summary?.activeProjects ?? 0, note: '继续保持节奏', icon: 'play', tone: 'active' },
  { label: '已暂停', count: props.summary?.pausedProjects ?? 0, note: '随时可以再出发', icon: 'pause', tone: 'paused' },
  { label: '已完成', count: props.summary?.doneProjects ?? 0, note: '完成后记得庆祝', icon: 'check', tone: 'done' },
  { label: '已归档', count: props.summary?.archivedProjects ?? 0, note: '把阶段成果收藏好', icon: 'archive', tone: 'archived' },
])
</script>

<template>
  <section class="dashboard-stats" aria-label="项目统计">
    <article v-for="card in cards" :key="card.label" class="stat-card">
      <span class="stat-icon" :class="`tone-${card.tone}`" aria-hidden="true">
        <svg v-if="card.icon === 'folder'" viewBox="0 0 24 24"><path d="M3.5 6.5h6l2 2h9v9.8a1.7 1.7 0 0 1-1.7 1.7H5.2a1.7 1.7 0 0 1-1.7-1.7z"/><path d="M3.5 9h17"/></svg>
        <svg v-else-if="card.icon === 'play'" viewBox="0 0 24 24"><path d="m8 5.5 11 6.5-11 6.5z"/></svg>
        <svg v-else-if="card.icon === 'pause'" viewBox="0 0 24 24"><path d="M8 5.5v13M16 5.5v13"/></svg>
        <svg v-else-if="card.icon === 'check'" viewBox="0 0 24 24"><path d="m5 12.5 4.5 4.5L19 7.5"/></svg>
        <svg v-else viewBox="0 0 24 24"><rect x="4" y="7" width="16" height="13" rx="2"/><path d="M3 7V5a1 1 0 0 1 1-1h16a1 1 0 0 1 1 1v2M9 11h6"/></svg>
      </span>
      <div class="stat-copy">
        <strong>{{ card.count }}</strong>
        <span>{{ card.label }}</span>
      </div>
      <p>{{ card.note }}</p>
    </article>
  </section>
</template>

<style scoped>
.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 20px;
}

.stat-card {
  position: relative;
  display: grid;
  min-width: 0;
  min-height: 140px;
  grid-template-columns: 50px 1fr;
  grid-template-rows: 1fr auto;
  align-items: center;
  column-gap: 16px;
  padding: 20px 24px 14px;
  border: 1px solid var(--sc-border-soft);
  border-radius: 25px;
  background: var(--sc-card-surface);
  box-shadow: var(--sc-shadow-sm);
}

.stat-icon {
  display: grid;
  width: 50px;
  height: 50px;
  place-items: center;
  border-radius: 50%;
}

.stat-icon svg {
  width: 25px;
  height: 25px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.tone-rose { color: #e985bb; background: color-mix(in srgb, currentColor 16%, var(--sc-card-surface)); }
.tone-active { color: var(--sc-status-active); background: color-mix(in srgb, currentColor 16%, var(--sc-card-surface)); }
.tone-paused { color: var(--sc-status-paused); background: color-mix(in srgb, currentColor 16%, var(--sc-card-surface)); }
.tone-done { color: var(--sc-status-done); background: color-mix(in srgb, currentColor 16%, var(--sc-card-surface)); }
.tone-archived { color: var(--sc-status-archived); background: color-mix(in srgb, currentColor 16%, var(--sc-card-surface)); }

.stat-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.stat-copy strong {
  color: var(--sc-text);
  font-size: 28px;
  font-weight: 600;
  line-height: 1.2;
}

.stat-copy span {
  overflow: hidden;
  color: var(--sc-text-secondary);
  font-size: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stat-card p {
  grid-column: 1 / -1;
  margin: 4px 0 0;
  color: var(--sc-text-secondary);
  font-size: 13px;
  line-height: 1.5;
}

@media (max-width: 1000px) {
  .dashboard-stats { grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
}

@media (max-width: 600px) {
  .dashboard-stats { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
  .stat-card { min-height: 124px; padding: 16px; column-gap: 11px; }
  .stat-icon { width: 42px; height: 42px; }
  .stat-copy strong { font-size: 24px; }
  .stat-copy span { font-size: 15px; }
  .stat-card p { font-size: 12px; }
}
</style>
