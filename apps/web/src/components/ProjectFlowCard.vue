<script setup lang="ts">
import { computed, ref, useId } from 'vue'
import type { Project, ProjectStatus } from '@/types/project'
import type { ProjectFlowSummary } from '@/types/dashboard'
import { formatDateTime } from '@/utils/datetime'

type ProjectTab = 'unfinished' | 'ongoing' | 'completed'

const props = defineProps<{
  flow: ProjectFlowSummary | null
  loading: boolean
  error: string | null
}>()

const emit = defineEmits<{ open: [id: number]; viewAll: [] }>()

/** 画布高度只够放三行，多出来的项目靠底部「查看全部项目」承接。 */
const VISIBLE_ROWS = 3

/** 配色池大小，与 tokens 里的 --sc-flow-tone-1..4 一一对应。 */
const ACTIVE_TONE_SLOTS = 4

/** 状态保留色：已暂停恒为橙、已完成恒为紫。它们不参与随机分配，
    所以这两个颜色在任何一屏、任何皮肤下都仍然只表示状态。 */
const RESERVED_PILL_TONES: Partial<Record<ProjectStatus, string>> = {
  PAUSED: 'tone-paused',
  DONE: 'tone-done',
}

const selectedTab = ref<ProjectTab>('ongoing')
const markerPrefix = useId().replace(/:/g, '')
const tabs: { id: ProjectTab; label: string }[] = [
  { id: 'unfinished', label: '未完成' },
  { id: 'ongoing', label: '进行中' },
  { id: 'completed', label: '已完成' },
]

const counts = computed<Record<ProjectTab, number>>(() => ({
  unfinished: props.flow?.unfinishedCount ?? 0,
  ongoing: props.flow?.ongoingCount ?? 0,
  completed: props.flow?.completedCount ?? 0,
}))

const selectedProjects = computed(() => {
  const projects = props.flow?.projects ?? []
  if (selectedTab.value === 'completed') {
    return projects.filter((project) => project.status === 'DONE')
  }
  const matched =
    selectedTab.value === 'unfinished'
      ? projects.filter((project) => project.status !== 'DONE')
      : projects.filter((project) => project.status === 'ACTIVE')
  // 未完成 / 进行中只画进度最高的三条；sort 是稳定的，进度相同时保留
  // 后端给的「最近更新在前」顺序。已完成不排序 —— 后端已按更新时间排好。
  return matched.sort((a, b) => b.progress - a.progress)
})

/** 由 id 定的伪随机槽位：同一个项目每次渲染都落在同一个颜色上。
    换成 Math.random() 的话，切一次页签颜色就重排一遍，颜色就不再是任何东西的线索。 */
function toneSlot(id: number) {
  return Math.abs(Math.imul(id, 2654435761)) % ACTIVE_TONE_SLOTS
}

/** 给这一屏的项目分配胶囊配色：进行中的从配色池里取，撞色就往后顺延一个空槽，
    尽量让同屏出现的颜色互不相同；已暂停 / 已完成用状态保留色。 */
function assignPillTones(projects: Project[]): string[] {
  const taken = new Set<number>()
  return projects.map((project) => {
    const reserved = RESERVED_PILL_TONES[project.status]
    if (reserved) return reserved
    let slot = toneSlot(project.id)
    while (taken.has(slot)) slot = (slot + 1) % ACTIVE_TONE_SLOTS
    taken.add(slot)
    return `tone-${slot + 1}`
  })
}

const averageProgress = computed(() => {
  if (!selectedProjects.value.length) return 0
  const total = selectedProjects.value.reduce((sum, project) => sum + project.progress, 0)
  return Math.round(total / selectedProjects.value.length)
})
const selectedLabel = computed(() => tabs.find((tab) => tab.id === selectedTab.value)?.label ?? '项目')

const displayProjects = computed(() => {
  const projects = selectedProjects.value.slice(0, VISIBLE_ROWS)
  const tones = assignPillTones(projects)
  const gap = 55
  const rowHeight = 42
  const totalHeight = projects.length * rowHeight + Math.max(0, projects.length - 1) * (gap - rowHeight)
  const start = (180 - totalHeight) / 2

  return projects.map((project, index) => ({
    project,
    tone: tones[index],
    top: start + index * gap,
    center: start + index * gap + rowHeight / 2,
  }))
})

const connectorPaths = computed(() => displayProjects.value.map(({ project, center }) => ({
  id: project.id,
  path: `M286 ${center} C338 ${center} 304 90 365 90`,
  status: project.status,
})))

const moreLabel = computed(() => {
  const total = selectedProjects.value.length
  if (total <= VISIBLE_ROWS) return `共 ${total} 个项目`
  return selectedTab.value === 'completed'
    ? `显示最近 ${VISIBLE_ROWS} 个，共 ${total} 个项目`
    : `显示进度最高的 ${VISIBLE_ROWS} 个，共 ${total} 个项目`
})

/** 连线只跟状态走（进行中的连线统一用品牌色），不跟随胶囊的随机配色。 */
function lineClass(status: ProjectStatus) {
  return `tone-${status.toLowerCase()}`
}
</script>

<template>
  <section class="project-flow-card" aria-label="项目进度">
    <header class="card-header">
      <div class="heading">
        <h2>项目</h2>
        <span class="subheading">状态与整体进度</span>
      </div>
      <div class="tabs" role="tablist" aria-label="项目状态筛选">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          type="button"
          role="tab"
          class="tab"
          :class="{ selected: selectedTab === tab.id }"
          :aria-selected="selectedTab === tab.id"
          @click="selectedTab = tab.id"
        >
          <span>{{ tab.label }}</span>
          <span class="tab-count">{{ counts[tab.id] }}</span>
        </button>
      </div>
    </header>

    <div class="flow-canvas" :class="{ 'is-empty': !displayProjects.length }">
      <template v-if="displayProjects.length">
        <svg class="connectors" viewBox="0 0 620 180" preserveAspectRatio="none" aria-hidden="true">
          <defs>
            <marker :id="`${markerPrefix}-arrow-primary`" markerWidth="7" markerHeight="7" refX="6" refY="3.5" orient="auto">
              <path d="M0 0L7 3.5L0 7z" fill="var(--sc-brand-strong)" />
            </marker>
            <marker :id="`${markerPrefix}-arrow-down`" markerWidth="7" markerHeight="7" refX="3.5" refY="6" orient="auto">
              <path d="M0 0L7 0L3.5 7z" fill="var(--sc-brand-strong)" />
            </marker>
          </defs>
          <path
            v-for="path in connectorPaths"
            :key="path.id"
            class="connector-line"
            :class="lineClass(path.status)"
            :d="path.path"
          />
          <path
            class="connector-line connector-summary"
            d="M365 90H431"
            :marker-end="`url(#${markerPrefix}-arrow-primary)`"
          />
          <path
            class="connector-line connector-summary"
            d="M493 109V130"
            :marker-end="`url(#${markerPrefix}-arrow-down)`"
          />
          <circle cx="365" cy="90" r="3.5" fill="var(--sc-brand-alt)" />
        </svg>

        <button
          v-for="row in displayProjects"
          :key="row.project.id"
          type="button"
          class="project-pill"
          :class="row.tone"
          :style="{ '--row-top': `${row.top}px` }"
          :aria-label="`${row.project.name}，更新于 ${formatDateTime(row.project.updatedAt)}，进度 ${row.project.progress}%`"
          @click="emit('open', row.project.id)"
        >
          <span class="project-name">{{ row.project.name }}</span>
          <span class="project-date">更新于 {{ formatDateTime(row.project.updatedAt) }}</span>
          <span class="project-percent">{{ row.project.progress }}%</span>
          <i class="connector-dot" aria-hidden="true" />
        </button>

        <div class="summary-node">平均进度</div>

        <div
          class="completion-pill"
          role="img"
          :aria-label="`当前筛选项目平均进度 ${averageProgress}%`"
        >
          <span class="completion-fill" :style="{ width: `${averageProgress * 0.72}%` }" />
          <span class="completion-orb" :style="{ left: `calc(${averageProgress * 0.72}% - 5px)` }" />
          <strong>{{ averageProgress }}%</strong>
        </div>
      </template>

      <div v-else-if="loading" class="empty-state">正在加载项目进度…</div>
      <div v-else-if="error" class="empty-state error-state">{{ error }}</div>
      <div v-else class="empty-state">{{ selectedLabel }}暂无项目</div>
    </div>

    <footer class="card-footer">
      <div class="legend" aria-label="项目状态图例">
        <span><i class="legend-swatch ongoing" />进行中</span>
        <span><i class="legend-swatch paused" />已暂停</span>
        <span><i class="legend-swatch completed" />已完成</span>
      </div>
      <span class="foot-note">{{ moreLabel }}</span>
      <button class="view-all" type="button" @click="emit('viewAll')">查看全部项目 <span aria-hidden="true">→</span></button>
    </footer>
  </section>
</template>

<style scoped>
.project-flow-card {
  width: min(100%, 668px);
  padding: 20px 28px 18px;
  overflow: hidden;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-xl);
  background: var(--sc-gradient-soft);
  box-shadow: var(--sc-shadow-sm);
  color: var(--sc-text);
  font-family: var(--sc-font);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 12px;
}

.heading h2 {
  margin: 0;
  color: var(--sc-text);
  font-size: var(--sc-font-size-3xl);
  font-weight: 700;
}

.subheading {
  color: var(--sc-text-placeholder);
  font-size: var(--sc-font-size-xs);
}

.tabs {
  display: flex;
  align-items: center;
  gap: 24px;
}

.tab {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0 9px;
  border: 0;
  background: none;
  color: var(--sc-text-secondary);
  font: inherit;
  font-size: var(--sc-font-size-md);
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.18s ease;
}

.tab:hover,
.tab.selected {
  color: var(--sc-brand-alt-strong);
}

.tab.selected {
  font-weight: 650;
}

.tab.selected::after {
  position: absolute;
  right: 0;
  bottom: 1px;
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

.flow-canvas {
  position: relative;
  height: 180px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-md);
  background-color: var(--sc-panel-faint);
  background-image: repeating-linear-gradient(
    to bottom,
    transparent 0,
    transparent 33px,
    var(--sc-border-soft) 34px,
    transparent 35px
  );
}

.connectors {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: visible;
}

.connector-line {
  fill: none;
  stroke: var(--sc-brand);
  stroke-width: 1.8;
  vector-effect: non-scaling-stroke;
}

.tone-active.connector-line,
.connector-line.tone-active {
  stroke: var(--sc-brand-strong);
}

.connector-line.tone-paused {
  stroke: var(--sc-warning-strong);
}

.connector-line.tone-done {
  stroke: var(--sc-purple-strong);
}

.project-pill {
  position: absolute;
  top: var(--row-top);
  left: 0;
  display: flex;
  width: 47%;
  height: 42px;
  flex-direction: column;
  justify-content: center;
  padding: 0 48px 0 22px;
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: var(--sc-radius-pill);
  color: #fff;
  text-align: left;
  box-shadow: var(--sc-shadow-sm);
  cursor: pointer;
}

/* 胶囊配色。tone-1..4 是「进行中」的配色池，由脚本按项目 id 分配（见 assignPillTones），
   同屏内不重复；tone-paused / tone-done 是状态保留色，不参与分配。
   这些颜色不承载业务含义，只用来把同屏的项目区分开。 */
.tone-1 {
  background: var(--sc-flow-tone-1);
}

.tone-2 {
  background: var(--sc-flow-tone-2);
}

.tone-3 {
  background: var(--sc-flow-tone-3);
}

.tone-4 {
  background: var(--sc-flow-tone-4);
}

.tone-paused {
  background: var(--sc-flow-tone-paused);
}

.tone-done {
  background: var(--sc-flow-tone-done);
}

.project-name {
  overflow: hidden;
  font-size: var(--sc-font-size-md);
  font-weight: 650;
  line-height: 1.15;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-date {
  overflow: hidden;
  font-size: var(--sc-font-size-xs);
  line-height: 1.15;
  opacity: 0.94;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-percent {
  position: absolute;
  right: 17px;
  top: 50%;
  transform: translateY(-50%);
  font-size: var(--sc-font-size-xs);
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.connector-dot {
  position: absolute;
  top: 50%;
  right: -5px;
  width: 9px;
  height: 9px;
  transform: translateY(-50%);
  border-radius: 50%;
  background: inherit;
  filter: brightness(0.94);
}

.summary-node {
  position: absolute;
  top: 71px;
  left: 70%;
  display: grid;
  width: 122px;
  height: 38px;
  place-items: center;
  border: 2px solid var(--sc-brand-strong);
  border-radius: var(--sc-radius-pill);
  background: var(--sc-text);
  color: #fff;
  font-size: var(--sc-font-size-md);
  font-weight: 600;
  white-space: nowrap;
}

.completion-pill {
  position: absolute;
  top: 132px;
  left: 68%;
  width: 150px;
  height: 38px;
  overflow: hidden;
  border-radius: var(--sc-radius-pill);
  background: var(--sc-text);
  color: #fff;
}

.completion-fill {
  position: absolute;
  inset: 0 auto 0 0;
  max-width: 72%;
  border-radius: inherit;
  background: var(--sc-gradient-brand);
  transition: width 0.25s ease;
}

.completion-fill::after {
  position: absolute;
  inset: 5px 5px 5px 14px;
  border-radius: inherit;
  background: rgba(255, 255, 255, 0.24);
  content: '';
}

.completion-orb {
  position: absolute;
  top: 50%;
  width: 10px;
  height: 10px;
  transform: translateY(-50%);
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.7);
}

.completion-pill strong {
  position: absolute;
  top: 0;
  right: 0;
  display: grid;
  width: 42px;
  height: 100%;
  place-items: center;
  font-size: var(--sc-font-size-xs);
  font-weight: 650;
}

.empty-state {
  display: grid;
  height: 100%;
  place-items: center;
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-md);
}

.error-state {
  color: var(--sc-error-strong);
}

.card-footer,
.legend,
.legend > span {
  display: flex;
  align-items: center;
}

.card-footer {
  gap: 14px;
  padding-top: 14px;
}

.legend {
  gap: 20px;
}

.legend > span {
  gap: 7px;
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-sm);
  white-space: nowrap;
}

.legend-swatch {
  width: 16px;
  height: 7px;
  border-radius: var(--sc-radius-pill);
}

/* 进行中是多色，图例用一条跨整套配色池的渐变表示，而不是单色 */
.legend-swatch.ongoing {
  background: linear-gradient(
    90deg,
    var(--sc-flow-tone-1),
    var(--sc-flow-tone-2),
    var(--sc-flow-tone-3),
    var(--sc-flow-tone-4)
  );
}

.legend-swatch.paused {
  background: var(--sc-flow-tone-paused);
}

.legend-swatch.completed {
  background: var(--sc-flow-tone-done);
}

.foot-note {
  margin-left: auto;
  color: var(--sc-text-placeholder);
  font-size: var(--sc-font-size-xs);
  white-space: nowrap;
}

.view-all {
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--sc-brand-alt-strong);
  font: inherit;
  font-size: var(--sc-font-size-sm);
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.view-all:hover {
  color: var(--sc-brand-strong);
}

@media (max-width: 560px) {
  .project-flow-card {
    padding: 18px 16px 16px;
  }

  .card-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .tabs {
    width: 100%;
    justify-content: space-between;
    gap: 12px;
  }

  .project-pill {
    width: 56%;
    padding-right: 40px;
    padding-left: 14px;
  }

  .project-percent {
    right: 12px;
  }

  .summary-node {
    left: 66%;
    width: 31%;
    font-size: var(--sc-font-size-xs);
  }

  .completion-pill {
    left: 63%;
    width: 34%;
  }

  .card-footer {
    flex-wrap: wrap;
    justify-content: space-between;
    gap: 10px;
  }

  .legend {
    width: 100%;
    justify-content: space-between;
    gap: 8px;
  }

  .legend > span {
    gap: 5px;
    font-size: var(--sc-font-size-xs);
  }

  .foot-note {
    margin-left: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .tab,
  .completion-fill {
    transition: none;
  }
}
</style>
