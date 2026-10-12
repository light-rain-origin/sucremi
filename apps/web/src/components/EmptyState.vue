<script setup lang="ts">
import AppIcon from './AppIcon.vue'

withDefaults(defineProps<{ text?: string }>(), { text: '暂无数据' })
</script>

<template>
  <div class="empty-state">
    <span class="empty-icon"><AppIcon name="inbox" :size="26" /></span>
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
  gap: 12px;
  padding: 44px 16px;
  color: var(--sc-text-secondary);
}

.empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: var(--sc-gradient-soft);
  border: 1px solid var(--sc-border-soft);
  color: var(--sc-brand-alt);
}

.empty-text {
  margin: 0;
}
</style>
