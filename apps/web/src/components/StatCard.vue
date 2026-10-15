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
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  grid-template-rows: auto auto;
  align-items: center;
  column-gap: 13px;
  row-gap: 2px;
  min-height: 94px;
  padding: 15px 17px;
  border-radius: var(--sc-radius-xl);
  border: 1px solid var(--sc-border-soft);
  background: linear-gradient(125deg, var(--tone-soft), var(--sc-panel-strong) 66%);
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
  display: grid;
  grid-row: 1 / 3;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 17px;
  background: var(--tone-soft);
  color: var(--tone-strong);
}

.stat-value {
  align-self: end;
  color: var(--sc-text);
  font-size: 26px;
  font-weight: 700;
  line-height: 1.15;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  align-self: start;
  font-size: var(--sc-font-size-md);
  color: var(--sc-text-secondary);
}

/* 五档色调只是色相差异，取值全部来自皮肤的角色 token */
.tone-purple {
  --tone-soft: var(--sc-stat-purple-soft);
  --tone-strong: var(--sc-stat-purple-strong);
}

.tone-pink {
  --tone-soft: var(--sc-stat-pink-soft);
  --tone-strong: var(--sc-stat-pink-strong);
}

.tone-amber {
  --tone-soft: var(--sc-stat-amber-soft);
  --tone-strong: var(--sc-stat-amber-strong);
}

.tone-mint {
  --tone-soft: var(--sc-stat-mint-soft);
  --tone-strong: var(--sc-stat-mint-strong);
}

.tone-slate {
  --tone-soft: var(--sc-stat-slate-soft);
  --tone-strong: var(--sc-stat-slate-strong);
}
</style>
