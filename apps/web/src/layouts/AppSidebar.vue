<script setup lang="ts">
import type { IconName } from '@/components/icons'
import AppIcon from '@/components/AppIcon.vue'

defineEmits<{ navigate: [] }>()

const items: { to: string; label: string; exact: boolean; icon: IconName }[] = [
  { to: '/', label: '概览', exact: true, icon: 'home' },
  { to: '/projects', label: '项目', exact: false, icon: 'folder' },
]
</script>

<template>
  <aside class="sidebar">
    <div class="brand">
      <!-- 品牌标记：粉紫渐变瓷贴 + 白色字母 + 小星星，替代原先的纯色方块 -->
      <span class="brand-mark">
        <svg class="brand-tile" viewBox="0 0 34 34" aria-hidden="true" focusable="false">
          <defs>
            <linearGradient id="scBrandTile" x1="2" y1="1" x2="32" y2="33" gradientUnits="userSpaceOnUse">
              <stop stop-color="#F7A8CC" />
              <stop offset="1" stop-color="#9B86F8" />
            </linearGradient>
          </defs>
          <rect x="0" y="0" width="34" height="34" rx="11" fill="url(#scBrandTile)" />
          <path
            d="M25.2 5.4l.85 2.35 2.35.85-2.35.85-.85 2.35-.85-2.35L22 8.6l2.35-.85z"
            fill="#fff"
            opacity=".92"
          />
        </svg>
        <span class="brand-letter" aria-hidden="true">S</span>
      </span>
      <span class="brand-text">
        <span class="brand-name">Sucremi</span>
        <span class="brand-sub">开发工作台</span>
      </span>
    </div>

    <nav class="nav">
      <RouterLink
        v-for="it in items"
        :key="it.to"
        :to="it.to"
        class="nav-item"
        active-class="active"
        :exact-active-class="it.exact ? 'active' : ''"
        @click="$emit('navigate')"
      >
        <AppIcon class="nav-icon" :name="it.icon" :size="18" />
        <span class="nav-label">{{ it.label }}</span>
        <span class="nav-accent" aria-hidden="true" />
      </RouterLink>
    </nav>

    <div class="sidebar-foot">
      <AppIcon name="sparkle" :size="15" />
      <span>私人工作台</span>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sc-sidebar-w);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 18px 14px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.88) 0%, var(--sc-panel) 100%);
  border-right: 1px solid var(--sc-border-soft);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 6px 22px;
}

.brand-mark {
  position: relative;
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  filter: drop-shadow(0 4px 10px rgba(199, 150, 210, 0.35));
}

.brand-tile {
  display: block;
  width: 34px;
  height: 34px;
}

.brand-letter {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 17px;
  font-weight: 800;
  line-height: 1;
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--sc-text);
  line-height: 1.2;
}

.brand-sub {
  font-size: 11px;
  color: var(--sc-text-placeholder);
  letter-spacing: 0.06em;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: var(--sc-radius-pill);
  color: var(--sc-text-secondary);
  font-weight: 500;
  transition:
    background 0.18s ease,
    color 0.18s ease;
}

.nav-item:hover {
  background: var(--sc-bg-muted);
  color: var(--sc-text);
}

.nav-icon {
  color: currentColor;
  opacity: 0.85;
}

.nav-label {
  flex: 1;
  min-width: 0;
}

/* 选中态：淡粉→淡紫轻渐变 + 右侧细小强调标记 */
.nav-item.active {
  background: linear-gradient(100deg, var(--sc-pink-soft) 0%, var(--sc-purple-soft) 100%);
  color: var(--sc-pink-strong);
  font-weight: 600;
}

.nav-item.active .nav-icon {
  opacity: 1;
}

.nav-accent {
  width: 14px;
  height: 3px;
  border-radius: var(--sc-radius-pill);
  background: var(--sc-gradient-brand);
  opacity: 0;
  transition: opacity 0.18s ease;
}

.nav-item.active .nav-accent {
  opacity: 1;
}

.sidebar-foot {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: var(--sc-radius-lg);
  background: var(--sc-gradient-soft);
  border: 1px solid var(--sc-border-soft);
  color: var(--sc-purple-strong);
  font-size: 12px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    z-index: 30;
    transform: translateX(-100%);
    transition: transform 0.2s ease;
    box-shadow: var(--sc-shadow-lg);
    background: var(--sc-bg-elevated);
  }
  .sidebar.open {
    transform: translateX(0);
  }
}
</style>
