<script setup lang="ts">
import { ref } from 'vue'
import AppSidebar from './AppSidebar.vue'
import AppTopbar from './AppTopbar.vue'

// 移动端侧栏开合（桌面端由 CSS 常驻显示）
const sidebarOpen = ref(false)
</script>

<template>
  <div class="app-shell">
    <div v-if="sidebarOpen" class="sidebar-backdrop" @click="sidebarOpen = false" />
    <AppSidebar :class="{ open: sidebarOpen }" @navigate="sidebarOpen = false" />
    <div class="main-col">
      <AppTopbar @toggle="sidebarOpen = !sidebarOpen" />
      <main class="content">
        <div class="content-inner">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 页面氛围（底色 + 柔光 / 圆点）整条由 --sc-backdrop 提供，皮肤可自由替换多层叠加 */
.app-shell {
  position: relative;
  display: flex;
  min-height: 100vh;
  background: var(--sc-backdrop);
}

.main-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  overflow-y: auto;
}

.content-inner {
  max-width: var(--sc-content-max);
  margin: 0 auto;
  padding: 4px var(--sc-content-pad) 48px;
}

.sidebar-backdrop {
  display: none;
}

/* 移动端：侧栏改为浮层 */
@media (max-width: 768px) {
  .sidebar-backdrop {
    display: block;
    position: fixed;
    inset: 0;
    background: var(--sc-overlay);
    z-index: 20;
  }
  .content-inner {
    padding: 4px 16px 32px;
  }
}
</style>
