<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppIcon from '@/components/AppIcon.vue'
import SkinSwitcher from '@/components/SkinSwitcher.vue'

defineEmits<{ toggle: [] }>()

const route = useRoute()
const title = computed(() => {
  if (route.path === '/') return '首页概览'
  if (route.path === '/projects') return '项目管理'
  return (route.meta.title as string) || 'Sucremi'
})
const titleTransition = ref('page-title-up')
watch(() => route.path, (path) => {
  titleTransition.value = path === '/' ? 'page-title-down' : 'page-title-up'
})
type ColorMode = 'light' | 'dark'
const COLOR_MODE_KEY = 'sucremi.color-mode'

function readColorMode(): ColorMode {
  try {
    return window.localStorage.getItem(COLOR_MODE_KEY) === 'dark' ? 'dark' : 'light'
  } catch {
    return 'light'
  }
}

const colorMode = ref<ColorMode>(readColorMode())

function applyColorMode() {
  document.documentElement.dataset.colorMode = colorMode.value
  document.documentElement.style.colorScheme = colorMode.value
}

applyColorMode()

function toggleColorMode() {
  colorMode.value = colorMode.value === 'light' ? 'dark' : 'light'
  applyColorMode()
  try {
    window.localStorage.setItem(COLOR_MODE_KEY, colorMode.value)
  } catch {
    // 本次会话仍可切换；存储不可用时刷新后回到浅色。
  }
}
</script>

<template>
  <header class="app-top-navigation">
    <div class="navigation-heading">
      <button class="menu-toggle" aria-label="菜单" @click="$emit('toggle')">
        <AppIcon name="menu" :size="20" />
      </button>
      <h1 class="page-title">
        <Transition :name="titleTransition">
          <span :key="title" class="page-title-text">{{ title }}</span>
        </Transition>
      </h1>
    </div>

    <div class="search-display" role="search" aria-label="搜索项目、任务、记录、资料、待办">
      <svg class="search-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <circle cx="10.8" cy="10.8" r="6.6" />
        <path d="m16 16 4.2 4.2" />
      </svg>
      <span class="search-placeholder">搜索项目、任务、记录、资料、待办...</span>
      <span class="search-shortcut">Ctrl + K</span>
    </div>

    <div class="navigation-actions">
      <button class="icon-action notification-action" type="button" aria-label="通知">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 3a6 6 0 0 0-6 6c0 4.7-1.55 6.45-2.73 7.74A1.3 1.3 0 0 0 4.23 19h15.54a1.3 1.3 0 0 0 .96-2.26C19.55 15.45 18 13.7 18 9a6 6 0 0 0-6-6Z" />
          <path d="M9.6 20a2.55 2.55 0 0 0 4.8 0Z" />
        </svg>
        <i aria-hidden="true" />
      </button>
      <button
        class="icon-action color-mode-action"
        type="button"
        :aria-label="colorMode === 'light' ? '切换到深色模式' : '切换到浅色模式'"
        :title="colorMode === 'light' ? '深色模式' : '浅色模式'"
        @click="toggleColorMode"
      >
        <svg v-if="colorMode === 'light'" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <circle cx="12" cy="12" r="4.1" />
          <path d="M11 1.8h2v3.4h-2zM11 18.8h2v3.4h-2zM18.8 11h3.4v2h-3.4zM1.8 11h3.4v2H1.8zM17.66 4.93l1.41 1.41-2.4 2.4-1.41-1.41zM7.34 15.25l1.41 1.41-2.4 2.4-1.41-1.41zM17.66 19.07l-2.4-2.4 1.41-1.41 2.4 2.4zM7.34 8.75l-2.4-2.4 1.41-1.41 2.4 2.4z" />
        </svg>
        <svg v-else viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M20.4 15.3A8.8 8.8 0 0 1 8.7 3.6a8.9 8.9 0 1 0 11.7 11.7Z" />
        </svg>
      </button>
      <SkinSwitcher />
      <div class="profile-display" aria-label="Sucremi 用户">
        <span class="profile-avatar" aria-hidden="true">S</span>
        <span class="profile-name">Sucremi</span>
        <svg class="profile-chevron" viewBox="0 0 12 12" fill="none" aria-hidden="true">
          <path d="m3 4.5 3 3 3-3" />
        </svg>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-top-navigation {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: minmax(170px, 1fr) minmax(280px, 360px) minmax(220px, 1fr);
  align-items: center;
  height: 60px;
  min-height: 60px;
  margin: 20px 21px 0;
  padding: 0 20px 0 28px;
  border: 1px solid var(--sc-border-soft);
  border-radius: 18px;
  background: var(--sc-panel);
  box-shadow: var(--sc-shadow-sm);
  backdrop-filter: blur(18px);
}

.navigation-heading,
.navigation-actions {
  display: flex;
  min-width: 0;
  align-items: center;
}

.navigation-heading {
  gap: 12px;
}

.page-title {
  position: relative;
  width: 5em;
  height: 1.2em;
  overflow: hidden;
  margin: 0;
  color: var(--sc-page-title-color);
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.04em;
  line-height: 1.2;
}

.page-title-text {
  position: absolute;
  inset: 0 auto auto 0;
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.page-title-up-enter-active,
.page-title-up-leave-active,
.page-title-down-enter-active,
.page-title-down-leave-active {
  transition: transform 220ms cubic-bezier(0.22, 0.61, 0.36, 1);
}

.page-title-up-enter-from {
  transform: translateY(120%);
}

.page-title-up-leave-to {
  transform: translateY(-120%);
}

.page-title-down-enter-from {
  transform: translateY(-120%);
}

.page-title-down-leave-to {
  transform: translateY(120%);
}

@media (prefers-reduced-motion: reduce) {
  .page-title-up-enter-active,
  .page-title-up-leave-active,
  .page-title-down-enter-active,
  .page-title-down-leave-active {
    transition-duration: 1ms;
  }
}

.search-display {
  display: flex;
  min-width: 0;
  height: 38px;
  align-items: center;
  gap: 10px;
  padding: 0 10px 0 13px;
  border: 1px solid var(--sc-border-soft);
  border-radius: 999px;
  background: var(--sc-panel-strong);
  color: var(--sc-text-placeholder);
}

.search-icon {
  width: 17px;
  height: 17px;
  flex: 0 0 auto;
  stroke: #a99ac0;
  stroke-linecap: round;
  stroke-width: 2.4;
}

.search-placeholder {
  overflow: hidden;
  flex: 1;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-shortcut {
  flex: 0 0 auto;
  padding: 3px 7px;
  border: 1px solid var(--sc-border-soft);
  border-radius: 10px;
  color: var(--sc-text-secondary);
  font-size: 11px;
  line-height: 1.2;
}

.navigation-actions {
  justify-content: flex-end;
  gap: 18px;
}

.icon-action {
  position: relative;
  display: grid;
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  place-items: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  border: 1px solid rgba(155, 134, 248, 0.8);
  background: #f3eaff;
  color: #9b86f8;
  cursor: pointer;
}

.icon-action:hover {
  background: var(--sc-brand-alt-soft);
}

.icon-action svg {
  width: 19px;
  height: 19px;
  fill: currentColor;
  stroke: none;
}

.notification-action i {
  position: absolute;
  top: 3px;
  right: 3px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef86ba;
  box-shadow: 0 0 0 2px var(--sc-panel-strong);
}

.color-mode-action {
  border-color: rgba(155, 134, 248, 0.8);
  background: #f3eaff;
  color: #9b86f8;
}

.color-mode-action:hover {
  background: #e9ddff;
}

.profile-display {
  display: flex;
  align-items: center;
  gap: 9px;
  color: var(--sc-text);
  white-space: nowrap;
}

.profile-avatar {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 1px solid var(--sc-border-strong);
  border-radius: 50%;
  background: var(--sc-gradient-soft);
  color: var(--sc-brand-alt-strong);
  font-size: 16px;
  font-weight: 700;
}

.profile-name {
  font-size: 14px;
}

.profile-chevron {
  width: 12px;
  height: 12px;
  margin-left: 2px;
  stroke: var(--sc-text-secondary);
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 1.4;
}

.menu-toggle {
  display: none;
  align-items: center;
  padding: 6px 8px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-sm);
  background: var(--sc-panel-strong);
  color: var(--sc-text-secondary);
  cursor: pointer;
}

.menu-toggle:hover {
  color: var(--sc-brand-alt-strong);
}

@media (max-width: 1024px) {
  .app-top-navigation {
    grid-template-columns: minmax(150px, 1fr) minmax(240px, 320px) minmax(175px, 1fr);
    margin-right: 16px;
    margin-left: 16px;
    padding-right: 14px;
    padding-left: 20px;
  }

  .navigation-actions {
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .app-top-navigation {
    grid-template-columns: minmax(0, 1fr) auto;
    height: 60px;
    min-height: 60px;
    margin: 20px 12px 0;
    padding: 0 12px;
    border-radius: 16px;
  }

  .menu-toggle {
    display: inline-flex;
  }

  .search-display {
    display: none;
  }

  .navigation-actions {
    gap: 10px;
  }

  .notification-action,
  .profile-display {
    display: none;
  }
}
</style>
