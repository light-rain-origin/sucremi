<script setup lang="ts">
defineProps<{ label: string; value: number | string }>()
</script>

<template>
  <div class="stat-card">
    <div class="stat-value">{{ value }}</div>
    <div class="stat-label">{{ label }}</div>
  </div>
</template>

<style scoped>
.stat-card {
  background: var(--sc-bg-elevated);
  border: 1px solid var(--sc-border);
  border-radius: var(--sc-radius-lg);
  padding: 18px 20px;
  box-shadow: var(--sc-shadow-sm);
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--sc-primary-active);
  line-height: 1.2;
}
.stat-label {
  margin-top: 4px;
  color: var(--sc-text-secondary);
  font-size: 13px;
}
</style>
