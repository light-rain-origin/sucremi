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
/* 页面底色：粉 → 淡紫的对角渐变 + 两处柔光，构成 NapCat 式的整体氛围 */
.app-shell {
  position: relative;
  display: flex;
  min-height: 100vh;
  background:
    radial-gradient(1100px 520px at 4% -10%, rgba(245, 168, 203, 0.32), transparent 62%),
    radial-gradient(900px 500px at 100% 2%, rgba(155, 134, 248, 0.22), transparent 60%),
    var(--sc-gradient-page);
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
  padding: 4px 28px 48px;
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
    background: rgba(78, 60, 86, 0.32);
    z-index: 20;
  }
  .content-inner {
    padding: 4px 16px 32px;
  }
}
</style>
