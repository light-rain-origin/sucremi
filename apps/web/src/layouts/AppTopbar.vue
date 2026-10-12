<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

defineEmits<{ toggle: [] }>()

const route = useRoute()
const title = computed(() => (route.meta.title as string) || 'Sucremi')
</script>

<template>
  <header class="topbar">
    <button class="menu-toggle" aria-label="菜单" @click="$emit('toggle')">☰</button>
    <h1 class="page-title">{{ title }}</h1>
    <div class="topbar-right">
      <!-- 预留：用户菜单 / 操作入口（接入认证后填充） -->
    </div>
  </header>
</template>

<style scoped>
.topbar {
  height: var(--sc-topbar-h);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 24px;
  background: var(--sc-bg-elevated);
  border-bottom: 1px solid var(--sc-border);
}

.page-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--sc-text);
}

.topbar-right {
  margin-left: auto;
}

.menu-toggle {
  display: none;
  border: none;
  background: transparent;
  font-size: 20px;
  cursor: pointer;
  color: var(--sc-text-secondary);
  padding: 4px 8px;
  border-radius: var(--sc-radius-sm);
}

.menu-toggle:hover {
  background: var(--sc-bg-muted);
}

@media (max-width: 768px) {
  .topbar {
    padding: 0 16px;
  }
  .menu-toggle {
    display: inline-flex;
  }
}
</style>
