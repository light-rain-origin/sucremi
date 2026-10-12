<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppIcon from '@/components/AppIcon.vue'
import SkinSwitcher from '@/components/SkinSwitcher.vue'

defineEmits<{ toggle: [] }>()

const route = useRoute()
const title = computed(() => (route.meta.title as string) || 'Sucremi')
</script>

<template>
  <header class="topbar">
    <button class="menu-toggle" aria-label="菜单" @click="$emit('toggle')">
      <AppIcon name="menu" :size="20" />
    </button>
    <!-- 标题前的渐变短竖条：页头的品牌视觉标记，不承载语义 -->
    <span class="title-mark" aria-hidden="true" />
    <h1 class="page-title">{{ title }}</h1>
    <div class="topbar-right">
      <SkinSwitcher />
      <!-- 预留：用户菜单等操作入口（接入认证后填充） -->
    </div>
  </header>
</template>

<style scoped>
/* 页头不再做独立白色卡片，直接浮在页面渐变上，视觉更轻 */
.topbar {
  height: var(--sc-topbar-h);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 28px;
}

.title-mark {
  width: 4px;
  height: 18px;
  border-radius: var(--sc-radius-pill);
  background: var(--sc-gradient-brand);
}

.page-title {
  margin: 0;
  font-size: var(--sc-font-size-2xl);
  font-weight: 600;
  color: var(--sc-text);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.menu-toggle {
  display: none;
  align-items: center;
  border: none;
  background: var(--sc-panel-strong);
  color: var(--sc-text-secondary);
  cursor: pointer;
  padding: 6px 8px;
  border-radius: var(--sc-radius-sm);
  border: 1px solid var(--sc-border-soft);
}

.menu-toggle:hover {
  color: var(--sc-brand-alt-strong);
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
