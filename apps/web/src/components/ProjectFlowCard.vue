<script setup lang="ts">
import { computed, ref } from 'vue'
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

/** 最近更新稿中的连接线坐标，以 606px × 180px 画布为基准。 */
const LINE_START_X = 292.95
const JUNCTION = { x: 331.34, y: 90 }
const MIDDLE_END_X = 373.77
const SUMMARY_START_X = 373.236
const SUMMARY_END_X = 454.051

/** 连线复用最近更新稿的弯曲走向，顶 / 底项目汇入同一点，中间项目沿水平线继续流动。 */
const connectorPaths = computed(() =>
  displayProjects.value.map(({ project, center, tone }) => {
    const delta = center - JUNCTION.y
    const direction = Math.sign(delta)
    const scale = Math.min(1, Math.abs(delta) / 59)

    if (direction === 0) {
      return {
        id: project.id,
        tone,
        path: `M${LINE_START_X} 90 C329.78 90 336.94 90 ${MIDDLE_END_X} 90`,
        arrow: null,
      }
    }

    const bend = direction * scale
    const arrow = {
      d: `M${LINE_START_X + 15.175} ${center - 27.194 * bend}` +
        `L${LINE_START_X + 18.835} ${center - 33.976 * bend}` +
        `L${LINE_START_X + 22.131} ${center - 26.95 * bend}Z`,
    }

    return {
      id: project.id,
      tone,
      path:
        `M${LINE_START_X} ${center}` +
        `C324.543 ${center} 301.518 ${center - 46.288 * bend} 320.626 ${center - 56.865 * bend}` +
        `C323.09 ${JUNCTION.y + 0.77 * direction} 327.267 ${JUNCTION.y} ${JUNCTION.x} ${JUNCTION.y}`,
      arrow,
    }
  }),
)

const connectorSummaryStart = computed(() =>
  connectorPaths.value.some((path) => path.arrow === null) ? SUMMARY_START_X : JUNCTION.x,
)

const moreLabel = computed(() => {
  const total = selectedProjects.value.length
  if (total <= VISIBLE_ROWS) return `共 ${total} 个项目`
  return selectedTab.value === 'completed'
    ? `显示最近 ${VISIBLE_ROWS} 个，共 ${total} 个项目`
    : `显示进度最高的 ${VISIBLE_ROWS} 个，共 ${total} 个项目`
})
</script>

<template>
  <section class="project-flow-card" aria-label="最近更新">
    <header class="card-header">
      <div class="heading">
        <h2>最近更新</h2>
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
        <svg class="connectors" viewBox="0 0 606.11 180" preserveAspectRatio="none" aria-hidden="true">
          <template v-for="path in connectorPaths" :key="path.id">
            <path class="connector-line" :class="path.tone" :d="path.path" />
            <path
              v-if="path.arrow"
              class="connector-arrow"
              :class="path.tone"
              :d="path.arrow.d"
            />
          </template>
          <!-- 汇合后的品牌色主线，接到平均进度节点下方。 -->
          <path class="connector-line connector-summary" :d="`M${connectorSummaryStart} 90H${SUMMARY_END_X}`" />
          <path
            class="connector-line connector-summary"
            d="M485.906 106V146"
          />
          <path class="junction-arrow" d="M328.176 86.534L335.111 90L328.176 93.466Z" />
          <path class="connector-arrow connector-summary-arrow" d="M371.351 86.534L378.286 90L371.351 93.466Z" />
          <path class="connector-arrow" d="M485.967 123.86L482.467 117L489.467 117Z" />
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

        <!-- 平均进度：与项目列表 / 概览卡同一套进度条语言（浅色轨道 + 品牌渐变填充），
             数字单独占右端一格。填充宽度与光球位置都在 CSS 里由 --completion-ratio
             推导，这里只给一个 0..1 的比例 —— 0% 到 100% 就是完整量程。 -->
        <div
          class="completion-pill"
          role="progressbar"
          :aria-label="`${selectedLabel}项目平均进度`"
          :aria-valuenow="averageProgress"
          aria-valuemin="0"
          aria-valuemax="100"
          :style="{ '--completion-ratio': averageProgress / 100 }"
        >
          <span class="completion-fill" />
          <span class="completion-orb" />
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
  width: min(100%, 666.72px);
  height: 315px;
  padding: 14px 30px 18px;
  overflow: hidden;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-xl);
  background: var(--sc-card-surface);
  box-shadow: var(--sc-shadow-md);
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
  height: 182px;
  flex: 0 0 182px;
  border: 1.2px solid var(--sc-flow-grid-line);
  border-radius: 15px;
  background-color: var(--sc-card-surface);
  background-image: repeating-linear-gradient(
    to bottom,
    transparent 0,
    transparent calc(36.2px - 1.2px),
    var(--sc-flow-grid-line) calc(36.2px - 1.2px),
    var(--sc-flow-grid-line) 36.2px
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
  stroke: var(--tone, var(--sc-brand));
  stroke-width: 1.8;
  vector-effect: non-scaling-stroke;
}

/* 线上的箭头跟着自己那根线的颜色走 */
.connector-arrow {
  fill: var(--tone, var(--sc-brand));
}

/* 合并点的箭头：与主线同色系，读作「三根线汇成一根，继续往右」 */
.junction-arrow {
  fill: #a8cd6e;
}

.connector-summary-arrow {
  fill: var(--sc-brand-strong);
}

.project-pill {
  position: absolute;
  top: var(--row-top);
  left: 10.1px;
  display: flex;
  width: 46.67%;
  height: 42px;
  flex-direction: column;
  justify-content: center;
  padding: 0 48px 0 12.1px;
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: var(--sc-radius-pill);
  /* 底色来自 tone-* 声明的 --tone（见下），与自己的连线、线上箭头同色 */
  background: var(--tone);
  color: #fff;
  text-align: left;
  box-shadow: var(--sc-shadow-sm);
  cursor: pointer;
}

/* 胶囊配色。tone-1..4 是「进行中」的配色池，由脚本按项目 id 分配（见 assignPillTones），
   同屏内不重复；tone-paused / tone-done 是状态保留色，不参与分配。
   这些颜色不承载业务含义，只用来把同屏的项目区分开。

   一个 class 同时挂在三个地方：胶囊取 background、连线取 stroke、线上箭头取 fill。
   所以 tone-* 只声明 --tone 这个色值，具体属性各自由相应元素去读 —— 颜色只写一遍，
   同一个 class 挂到胶囊上还是挂到 path 上都不会互相干扰。合并之后的主线不带 tone 类，
   落回 --sc-brand，于是「各自的流」和「汇合后的流」在颜色上是分开的。 */
.tone-1 {
  --tone: var(--sc-flow-tone-1);
}

.tone-2 {
  --tone: var(--sc-flow-tone-2);
}

.tone-3 {
  --tone: var(--sc-flow-tone-3);
}

.tone-4 {
  --tone: var(--sc-flow-tone-4);
}

.tone-paused {
  --tone: var(--sc-flow-tone-paused);
}

.tone-done {
  --tone: var(--sc-flow-tone-done);
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
  background: var(--sc-flow-summary-bg);
  color: var(--sc-flow-summary-text);
  font-size: var(--sc-font-size-md);
  font-weight: 600;
  white-space: nowrap;
}

/* 平均进度：浅色轨道 + 品牌渐变填充，和项目列表 / 概览卡的进度条是同一套语言；
   内部再叠一颗光球与一道拖尾表示「现在到这里」。
   轨道从 --sc-border-strong 换成 --sc-border-soft：深一档那个压在近白的卡片底上
   读成一道旧旧的灰紫，浅一档更干净，也和项目列表的轨道是同一个值。 */
.completion-pill {
  /* 右端留给数字的一格，以及真正用来画进度的宽度 —— 填充与光球共用它，
     两者因此永远同步。以前这里是 `进度 * 0.72` 的魔数：为了让填充避开数字，
     整条刻度被压到 72%，还说不清「72% 是哪来的」。现在留白是一个显式的值，
     量程本身是完整的。 */
  --completion-zone: 46px;
  --completion-area: calc(100% - var(--completion-zone));
  /* 光球直径，以及球心离填充前沿的偏移：半个球身再加 1px 余量，于是整颗球
     都落在填充里面，只有辉光会洒到轨道上（辉光本来就该洒出来）。 */
  --completion-orb-size: 8px;
  --completion-orb-inset: calc(var(--completion-orb-size) / 2 + 1px);
  position: absolute;
  top: 132px;
  left: 68%;
  width: 150px;
  height: 38px;
  overflow: hidden;
  border-radius: var(--sc-radius-pill);
  background: var(--sc-border-soft);
  color: var(--sc-text);
}

/* 填充宽度 =（轨道宽 − 数字格）× 比例，比例由模板写在 --completion-ratio 上。
   两端都收成圆弧，前端跟着光球走。 */
.completion-fill {
  position: absolute;
  inset: 0 auto 0 0;
  width: calc(var(--completion-area) * var(--completion-ratio, 0));
  border-radius: var(--sc-radius-pill);
  background: var(--sc-gradient-brand);
  transition: width 0.25s ease;
}

/* 光球后面拖出的尾迹：贴中线的一道细楔形，根部只有 8px 高 —— 正好等于光球
   直径，根部右缘落在球心上（right 与光球的 inset 同值），于是楔形的两个尖角
   正好落在球的轮廓上被球盖住，不会露出硬边；向左收成尖并淡出，读起来是
   光球甩出来的拖尾。clip-path 做收尖，渐变做淡出；两者叠加才有走向感。 */
.completion-fill::after {
  position: absolute;
  inset: 15px var(--completion-orb-inset) 15px 14px;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.04) 0%,
    rgba(255, 255, 255, 0.4) 100%
  );
  clip-path: polygon(0 50%, 100% 0, 100% 100%);
  content: '';
}

/* 当前位置的光球。球心停在填充前沿往左 --completion-orb-inset 处，于是整颗球
   （含右缘）都落在填充内部 —— 球一进填充，就不再需要描边去和轨道区分了，
   白球压在粉紫渐变上本来就亮。原先那圈 --sc-brand-strong 描边是黑球时代的
   东西，现在只会让球显得硬、像贴上去的塑料片；换成一层白色辉光才像「光」。
   位置那个下限是给极低进度兜底的：填充宽不到 2 个 inset（8 + 2 = 10px，约 10%）
   时容不下球，与其让球心跟着前沿往左、被胶囊圆角切掉半个，不如让它停在刚进
   轨道一点点 —— 这时球会比填充探出几个像素，但它没有描边，探出去的部分只是
   落在轨道上的辉光，读起来仍是「刚从起点出发」，不是残缺的指示器。 */
.completion-orb {
  position: absolute;
  top: 50%;
  left: max(
    var(--completion-orb-inset),
    calc(var(--completion-area) * var(--completion-ratio, 0) - var(--completion-orb-inset))
  );
  width: var(--completion-orb-size);
  height: var(--completion-orb-size);
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: var(--sc-brand-on);
  box-shadow: var(--sc-flow-orb-glow);
  /* 与填充同一条 transition：光球是坐在填充前端的，只动宽度的话它会先跳到
     终点、填充再慢慢追上来，两者会脱开 0.25s */
  transition: left 0.25s ease;
}

/* 数字格：占住右端固定宽度，让填充与光球都停在它左边。
   试过给这一格加浅色面板把「刻度区 / 读数区」分开，但面板比轨道更接近卡片底色，
   胶囊右端的圆角会跟着消失、整条看起来像被切掉一截，所以留白保持与轨道同色 ——
   读比例时以标签为准。 */
.completion-pill strong {
  position: absolute;
  top: 0;
  right: 0;
  display: grid;
  width: var(--completion-zone);
  height: 100%;
  place-items: center;
  font-size: var(--sc-font-size-md);
  font-weight: 650;
  font-variant-numeric: tabular-nums;
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
    height: auto;
    min-height: 315px;
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
  .completion-fill,
  .completion-orb {
    transition: none;
  }
}
</style>
