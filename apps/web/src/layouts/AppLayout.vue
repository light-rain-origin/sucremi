<script setup lang="ts">
import { ref } from 'vue'
import AppSidebar from './AppSidebar.vue'
import AppTopbar from './AppTopbar.vue'

// 移动端侧栏开合（桌面端由 CSS 常驻显示）
const sidebarOpen = ref(false)
</script>

<template>
  <div class="app-shell">
    <div class="app-background-glow" aria-hidden="true">
      <svg viewBox="1870 2877.779 598 400" xmlns="http://www.w3.org/2000/svg" focusable="false">
        <g opacity="0.5">
          <defs>
            <filter id="unified-background-glow" x="-50%" y="-50%" width="200%" height="200%">
              <feGaussianBlur stdDeviation="26" />
            </filter>
          </defs>
          <g class="fills">
            <ellipse
              class="background-glow-fill"
              cx="2169"
              cy="3077.779"
              rx="299"
              ry="200"
              filter="url(#unified-background-glow)"
            />
          </g>
        </g>
      </svg>
    </div>
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
  isolation: isolate;
  display: flex;
  min-height: 100vh;
  background: var(--sc-backdrop);
}

.app-background-glow {
  position: fixed;
  z-index: 0;
  top: -54px;
  right: -146px;
  width: 598px;
  height: 400px;
  -webkit-mask-image: radial-gradient(ellipse at center, #000 34%, rgba(0, 0, 0, 0.78) 52%, transparent 82%);
  mask-image: radial-gradient(ellipse at center, #000 34%, rgba(0, 0, 0, 0.78) 52%, transparent 82%);
  pointer-events: none;
}

.app-background-glow svg {
  display: block;
  width: 100%;
  height: 100%;
}

.background-glow-fill {
  fill: var(--sc-background-glow-color);
  fill-opacity: var(--sc-background-glow-opacity);
}

:deep(.sidebar) {
  z-index: 1;
}

.main-col {
  position: relative;
  z-index: 1;
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
