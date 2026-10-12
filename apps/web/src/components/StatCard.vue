<script setup lang="ts">
import type { IconName } from './icons'
import AppIcon from './AppIcon.vue'

/**
 * 概览统计卡。
 * `tone` 只影响图标底色与数字颜色，用来区分五类统计，不改变含义。
 */
type StatTone = 'purple' | 'pink' | 'amber' | 'mint' | 'slate'

withDefaults(defineProps<{ label: string; value: number | string; icon: IconName; tone?: StatTone }>(), {
  tone: 'purple',
})
</script>

<template>
  <div class="stat-card" :class="`tone-${tone}`">
    <span class="stat-icon"><AppIcon :name="icon" :size="18" /></span>
    <div class="stat-value">{{ value }}</div>
    <div class="stat-label">{{ label }}</div>
  </div>
</template>

<style scoped>
.stat-card {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 18px;
  border-radius: var(--sc-radius-xl);
  border: 1px solid var(--sc-border-soft);
  background: var(--sc-panel);
  box-shadow: var(--sc-shadow-sm);
  transition:
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.stat-card:hover {
  box-shadow: var(--sc-shadow-md);
  border-color: var(--sc-border);
}

.stat-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  margin-bottom: 10px;
  border-radius: var(--sc-radius-md);
  background: var(--tone-soft);
  color: var(--tone-strong);
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.15;
  color: var(--tone-strong);
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 13px;
  color: var(--sc-text-secondary);
}

.tone-purple {
  --tone-soft: rgba(155, 134, 248, 0.14);
  --tone-strong: var(--sc-purple-strong);
}

.tone-pink {
  --tone-soft: rgba(245, 168, 203, 0.2);
  --tone-strong: var(--sc-pink-strong);
}

.tone-amber {
  --tone-soft: rgba(224, 168, 96, 0.18);
  --tone-strong: var(--sc-warning-strong);
}

.tone-mint {
  --tone-soft: rgba(95, 185, 139, 0.16);
  --tone-strong: var(--sc-success-strong);
}

.tone-slate {
  --tone-soft: rgba(140, 129, 152, 0.14);
  --tone-strong: #6b6277;
}
</style>
