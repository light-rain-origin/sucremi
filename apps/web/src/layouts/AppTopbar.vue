<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppIcon from '@/components/AppIcon.vue'
import SkinSwitcher from '@/components/SkinSwitcher.vue'

defineEmits<{ toggle: [] }>()

const route = useRoute()
const title = computed(() => (route.path === '/' ? '首页概览' : (route.meta.title as string) || 'Sucremi'))
</script>

<template>
  <header class="app-top-navigation">
    <div class="navigation-heading">
      <button class="menu-toggle" aria-label="菜单" @click="$emit('toggle')">
        <AppIcon name="menu" :size="20" />
      </button>
      <h1 class="page-title">{{ title }}</h1>
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
          <path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
          <path d="M9.5 21h5" />
        </svg>
        <i aria-hidden="true" />
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
  min-height: 66px;
  margin: 25px 21px 0;
  padding: 0 20px 0 28px;
  border: 1px solid rgba(238, 227, 244, 0.88);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 5px 18px rgba(105, 78, 128, 0.045);
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
  overflow: hidden;
  margin: 0;
  color: #40344f;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-display {
  display: flex;
  min-width: 0;
  height: 38px;
  align-items: center;
  gap: 10px;
  padding: 0 10px 0 13px;
  border: 1px solid #eee3f4;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: rgba(140, 126, 159, 0.8);
}

.search-icon {
  width: 17px;
  height: 17px;
  flex: 0 0 auto;
  stroke: #5a42c5;
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
  border: 1px solid #eee3f4;
  border-radius: 10px;
  color: #9689aa;
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
  color: #62598a;
  cursor: pointer;
}

.icon-action:hover {
  background: rgba(243, 234, 255, 0.8);
}

.icon-action svg {
  width: 19px;
  height: 19px;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 1.7;
}

.notification-action i {
  position: absolute;
  top: 3px;
  right: 3px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef86ba;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.9);
}

.profile-display {
  display: flex;
  align-items: center;
  gap: 9px;
  color: #40344f;
  white-space: nowrap;
}

.profile-avatar {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 1px solid rgba(182, 153, 238, 0.55);
  border-radius: 50%;
  background: linear-gradient(145deg, #ffe7f4, #e9e1ff);
  color: #8058ce;
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
  stroke: #80769a;
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
    min-height: 60px;
    margin: 25px 12px 0;
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
