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
      <!-- 品牌标记：渐变瓷贴 + 白色字母 + 小星星。颜色全部走品牌 token，随皮肤变化 -->
      <span class="brand-mark">
        <svg class="brand-tile" viewBox="0 0 34 34" aria-hidden="true" focusable="false">
          <defs>
            <linearGradient id="scBrandTile" x1="2" y1="1" x2="32" y2="33" gradientUnits="userSpaceOnUse">
              <stop class="brand-stop-from" />
              <stop class="brand-stop-to" offset="1" />
            </linearGradient>
          </defs>
          <rect x="0" y="0" width="34" height="34" rx="11" fill="url(#scBrandTile)" />
          <path
            class="brand-sparkle"
            d="M25.2 5.4l.85 2.35 2.35.85-2.35.85-.85 2.35-.85-2.35L22 8.6l2.35-.85z"
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

    <!-- 角色插画装饰位：高度与图片都来自皮肤 token，默认高度 0 → 不占位、不留空洞 -->
    <div class="sidebar-art" aria-hidden="true" />

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
  background: var(--sc-sidebar-bg);
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
  filter: var(--sc-brand-glow);
}

.brand-tile {
  display: block;
  width: 34px;
  height: 34px;
}

/* SVG 的渐变端点用 CSS 上色：表现属性里写 var() 不生效，类选择器可以 */
.brand-stop-from {
  stop-color: var(--sc-brand-tile-from);
}

.brand-stop-to {
  stop-color: var(--sc-brand-tile-to);
}

.brand-sparkle {
  fill: var(--sc-brand-on);
}

.brand-letter {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: var(--sc-brand-on);
  font-size: var(--sc-font-size-2xl);
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
  font-size: var(--sc-font-size-2xl);
  font-weight: 700;
  color: var(--sc-text);
  line-height: 1.2;
}

.brand-sub {
  font-size: var(--sc-font-size-xs);
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

/* 选中态：品牌色轻渐变 + 右侧细小强调标记 */
.nav-item.active {
  background: var(--sc-nav-active-bg);
  color: var(--sc-brand-strong);
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

/* 角色插画：高度由 --sc-character-height 决定，默认 0 —— 未提供素材时完全不可见。
   margin-top: auto 让它在有高度时被推到底部；高度为 0 时与 .sidebar-foot 的 auto 边距
   分享剩余空间，页脚依然贴底，因此默认状态下与引入此元素之前没有区别。 */
.sidebar-art {
  height: var(--sc-character-height);
  margin-top: auto;
  background-image: var(--sc-character-image);
  background-repeat: no-repeat;
  background-position: center bottom;
  background-size: contain;
  pointer-events: none;
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
  color: var(--sc-brand-alt-strong);
  font-size: var(--sc-font-size-sm);
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
