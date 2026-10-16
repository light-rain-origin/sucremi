<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, useId, watch } from 'vue'
import type { DashboardSummary } from '@/types/dashboard'

const props = defineProps<{ summary: DashboardSummary | null }>()
const chartId = useId()
const animationProgress = ref(0)
let animationFrame = 0

const total = computed(() =>
  (props.summary?.totalProjects ?? 0) + (props.summary?.archivedProjects ?? 0),
)

const statusItems = computed(() => [
  { label: '进行中', count: props.summary?.activeProjects ?? 0, tone: 'active', color: 'var(--sc-status-active)' },
  { label: '已暂停', count: props.summary?.pausedProjects ?? 0, tone: 'paused', color: 'var(--sc-status-paused)' },
  { label: '已完成', count: props.summary?.doneProjects ?? 0, tone: 'done', color: 'var(--sc-status-done)' },
  { label: '已归档', count: props.summary?.archivedProjects ?? 0, tone: 'archived', color: 'var(--sc-status-archived)' },
])

function point(radius: number, angle: number) {
  const radians = (angle * Math.PI) / 180
  return {
    x: 75 + radius * Math.cos(radians),
    y: 75 + radius * Math.sin(radians),
  }
}

function roundedSegmentPath(startAngle: number, endAngle: number) {
  const outerRadius = 75
  const innerRadius = 55
  const cornerRadius = Math.min(4, (endAngle - startAngle) * Math.PI / 180 * innerRadius / 2)
  const outerInset = (cornerRadius / outerRadius) * 180 / Math.PI
  const innerInset = (cornerRadius / innerRadius) * 180 / Math.PI
  const outerStart = point(outerRadius, startAngle + outerInset)
  const outerEnd = point(outerRadius, endAngle - outerInset)
  const innerEnd = point(innerRadius, endAngle - innerInset)
  const innerStart = point(innerRadius, startAngle + innerInset)
  const outerArc = endAngle - startAngle - outerInset * 2
  const innerArc = endAngle - startAngle - innerInset * 2
  const largeOuterArc = outerArc > 180 ? 1 : 0
  const largeInnerArc = innerArc > 180 ? 1 : 0

  const format = (value: number) => Number(value.toFixed(3))
  const coordinates = (value: { x: number; y: number }) => `${format(value.x)} ${format(value.y)}`

  return [
    `M ${coordinates(outerStart)}`,
    `A ${outerRadius} ${outerRadius} 0 ${largeOuterArc} 1 ${coordinates(outerEnd)}`,
    `Q ${coordinates(point(outerRadius, endAngle))} ${coordinates(point(outerRadius - cornerRadius, endAngle))}`,
    `L ${coordinates(point(innerRadius + cornerRadius, endAngle))}`,
    `Q ${coordinates(point(innerRadius, endAngle))} ${coordinates(innerEnd)}`,
    `A ${innerRadius} ${innerRadius} 0 ${largeInnerArc} 0 ${coordinates(innerStart)}`,
    `Q ${coordinates(point(innerRadius, startAngle))} ${coordinates(point(innerRadius + cornerRadius, startAngle))}`,
    `L ${coordinates(point(outerRadius - cornerRadius, startAngle))}`,
    `Q ${coordinates(point(outerRadius, startAngle))} ${coordinates(outerStart)}`,
    'Z',
  ].join(' ')
}

const donutSegments = computed(() => {
  if (!total.value) return []

  const visibleItems = statusItems.value.filter((item) => item.count > 0)
  const gapAngle = 5
  let cursor = 0

  return visibleItems.map((item) => {
    const segmentAngle = (item.count / total.value) * 360
    const segmentGap = Math.min(gapAngle, segmentAngle * 0.2)
    const startAngle = cursor + segmentGap / 2 - 90
    const endAngle = cursor + segmentAngle - segmentGap / 2 - 90
    cursor += segmentAngle
    return {
      ...item,
      startAngle,
      endAngle,
    }
  })
})

const displayedSegments = computed(() => donutSegments.value.map((segment) => ({
  ...segment,
  path: roundedSegmentPath(
    -90 + (segment.startAngle + 90) * animationProgress.value,
    -90 + (segment.endAngle + 90) * animationProgress.value,
  ),
})))

const revealClipId = `${chartId}-expansion`
const clipHandoffProgress = 0.9995
const clipComplete = computed(() => animationProgress.value >= clipHandoffProgress)
const revealClipPath = computed(() => {
  // Preserve the arc at the end; the mounted circle supplies the full clip.
  const progress = Math.min(animationProgress.value, clipHandoffProgress)
  if (progress <= 0) return 'M 75 75 L 75 0 Z'

  const endAngle = -90 + progress * 360
  const end = point(75, endAngle)
  const format = (value: number) => Number(value.toFixed(3))
  return `M 75 75 L 75 0 A 75 75 0 ${progress > 0.5 ? 1 : 0} 1 ${format(end.x)} ${format(end.y)} Z`
})

function easeInOutCubic(progress: number) {
  return progress < 0.5
    ? 4 * progress * progress * progress
    : 1 - Math.pow(-2 * progress + 2, 3) / 2
}

function startExpansionAnimation() {
  cancelAnimationFrame(animationFrame)
  if (window.matchMedia?.('(prefers-reduced-motion: reduce)').matches) {
    animationProgress.value = 1
    return
  }

  const duration = 1000
  const startedAt = performance.now()
  animationProgress.value = 0

  const step = (now: number) => {
    const elapsed = Math.min((now - startedAt) / duration, 1)
    animationProgress.value = easeInOutCubic(elapsed)
    if (elapsed < 1) animationFrame = requestAnimationFrame(step)
  }

  animationFrame = requestAnimationFrame(step)
}

onMounted(() => {
  if (donutSegments.value.length) startExpansionAnimation()
})

watch(() => donutSegments.value.length > 0, (hasSegments) => {
  if (hasSegments) startExpansionAnimation()
})

onBeforeUnmount(() => cancelAnimationFrame(animationFrame))

function percent(count: number) {
  return total.value ? Math.round((count / total.value) * 100) : 0
}
</script>

<template>
  <section class="dashboard-side-card" aria-labelledby="project-progress-title">
    <header class="progress-header">
      <div class="heading-copy">
        <h2 id="project-progress-title">项目进度</h2>
        <span>项目状态分布</span>
      </div>
    </header>

    <div class="progress-content">
      <div
        class="progress-donut"
        role="img"
        :aria-label="`共 ${total} 个项目，进行中 ${summary?.activeProjects ?? 0} 个`"
      >
        <svg
          :key="total > 0 ? 'loaded' : 'empty'"
          class="donut-svg"
          viewBox="0 0 150 150"
          aria-hidden="true"
        >
          <circle class="donut-track" cx="75" cy="75" r="65" />
          <defs>
            <clipPath :id="revealClipId" clipPathUnits="userSpaceOnUse">
              <path :d="revealClipPath" />
              <circle cx="75" cy="75" :r="clipComplete ? 100 : 0" />
            </clipPath>
          </defs>
          <g :clip-path="`url(#${revealClipId})`">
            <path
              v-for="segment in displayedSegments"
              :key="segment.label"
              class="donut-segment"
              :class="`tone-${segment.tone}`"
              :d="segment.path"
            />
          </g>
        </svg>
        <div class="donut-center">
          <span>进行中</span>
          <strong>{{ summary?.activeProjects ?? 0 }} 个</strong>
        </div>
      </div>

      <ul class="progress-legend" aria-label="各状态占比">
        <li v-for="item in statusItems" :key="item.label">
          <span class="legend-dot" :style="{ background: item.color }" aria-hidden="true" />
          <span class="legend-label">{{ item.label }}</span>
          <strong>{{ percent(item.count) }}%</strong>
        </li>
      </ul>
    </div>

    <footer class="progress-footer">每次推进一点，都是进展 <span aria-hidden="true">✦</span></footer>
  </section>
</template>

<style scoped>
.dashboard-side-card {
  display: flex;
  width: 100%;
  min-width: 0;
  height: 315px;
  box-sizing: border-box;
  flex: 1 1 auto;
  container-type: inline-size;
  flex-direction: column;
  padding: 19px 18px 14px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-xl);
  background: var(--sc-card-surface);
  box-shadow: var(--sc-shadow-md);
  color: var(--sc-text);
}

.progress-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.heading-copy h2 {
  margin: 0;
  color: var(--sc-text);
  font-size: var(--sc-font-size-3xl);
  font-weight: 700;
  line-height: 1.35;
}

.heading-copy span {
  display: block;
  margin-top: 1px;
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-xs);
}

.progress-content {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding-inline: 0;
  min-height: 0;
}

.progress-donut {
  position: relative;
  display: grid;
  width: min(150px, 48%);
  aspect-ratio: 1;
  height: auto;
  flex: 0 1 150px;
  min-width: 120px;
  place-items: center;
  border-radius: 50%;
}

.donut-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: visible;
}

.donut-track,
.donut-segment {
  fill: none;
}

.donut-track {
  stroke: var(--sc-border-soft);
}

.donut-segment {
  fill: var(--segment-color);
}

.donut-segment.tone-active { --segment-color: var(--sc-status-active); }
.donut-segment.tone-paused { --segment-color: var(--sc-status-paused); }
.donut-segment.tone-done { --segment-color: var(--sc-status-done); }
.donut-segment.tone-archived { --segment-color: var(--sc-status-archived); }

.donut-center {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
}

.donut-center span {
  color: var(--sc-text-secondary);
  font-size: clamp(12px, 4.1cqi, var(--sc-font-size-md));
}

.donut-center strong {
  color: var(--sc-text);
  font-size: clamp(20px, 7.5cqi, 24px);
  font-weight: 700;
  line-height: 1.25;
}

.progress-legend {
  display: flex;
  min-width: 0;
  flex: 0 1 auto;
  flex-direction: column;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.progress-legend li {
  display: grid;
  grid-template-columns: 11px auto auto;
  align-items: center;
  column-gap: 7px;
  color: var(--sc-text-secondary);
  font-size: clamp(12px, 4.4cqi, var(--sc-font-size-lg));
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-legend strong {
  color: var(--sc-text);
  font-size: clamp(12px, 4.4cqi, var(--sc-font-size-lg));
  font-weight: 500;
}

.progress-footer {
  padding-top: 11px;
  border-top: 1px solid var(--sc-border-soft);
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-xs);
  text-align: center;
}

.progress-footer span {
  color: var(--sc-brand);
}

@media (max-width: 900px) {
  .dashboard-side-card {
    height: auto;
    min-height: 315px;
  }
}

</style>
