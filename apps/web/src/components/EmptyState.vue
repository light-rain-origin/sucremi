<script setup lang="ts">
withDefaults(defineProps<{ text?: string }>(), { text: '暂无数据' })
</script>

<template>
  <div class="empty-state">
    <div class="empty-icon">🗂️</div>
    <p class="empty-text">{{ text }}</p>
    <slot />
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 16px;
  color: var(--sc-text-secondary);
}
.empty-icon {
  font-size: 40px;
  opacity: 0.7;
}
.empty-text {
  margin: 0;
}
</style>
