<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'
import { NAV_GLYPHS, type NavGlyph } from '@/components/sidebar-icons'

const emit = defineEmits<{ navigate: [] }>()

interface NavItem {
  label: string
  glyph: keyof typeof NAV_GLYPHS
  to: string
}

const items: NavItem[] = [
  { label: '首页概览', glyph: 'overview', to: '/' },
  { label: '项目管理', glyph: 'projects', to: '/projects' },
  { label: '开发记录', glyph: 'log', to: '/development-records' },
  { label: '知识沉淀', glyph: 'notes', to: '/knowledge' },
  { label: '魔法蕾米', glyph: 'remi', to: '/remi' },
  { label: '资料文库', glyph: 'library', to: '/library' },
  { label: '系统设置', glyph: 'settings', to: '/settings' },
]

const glyphOf = (name: keyof typeof NAV_GLYPHS): NavGlyph => NAV_GLYPHS[name]

const route = useRoute()

/*
 * 选中态自己算，不用 RouterLink 的 router-link-active ——
 * 概览项指向 '/'，在嵌套路由里它解析到的父记录对任何子路由都算「命中」，
 * 于是 /projects 会把「首页概览」和「项目管理」同时点亮（实测确认）。
 * 用路径判断既能保证任何时刻只有一项亮起，也能让 /projects/:id 继续算作「项目管理」。
 */
const isActive = (item: NavItem) => {
  return item.to === '/'
    ? route.path === '/'
    : route.path === item.to || route.path.startsWith(`${item.to}/`)
}

const onSelect = () => {
  emit('navigate')
}
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
          <rect x="0" y="0" width="34" height="34" rx="6.8" fill="url(#scBrandTile)" />
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
        <span class="brand-sub-row">
          <span class="brand-sub">我的开发工作台</span>
          <span class="brand-deco" aria-hidden="true">
            <span class="brand-dot" />
            <!-- 四角星：取自设计稿的轮廓路径，10×10 视区、2px 描边（贴边处按原稿裁切） -->
            <svg class="brand-star" width="15" height="12" viewBox="0 -1 12 12" aria-hidden="true">
              <path
                d="M5 0 6.121 2.691 6.471 3.53 10 5 6.471 6.471 5 10 3.529 6.471 0 5 3.529 3.53Z"
              />
            </svg>
          </span>
        </span>
      </span>
    </div>

    <div class="sidebar-divider" aria-hidden="true" />

    <nav class="nav" aria-label="工作台导航">
      <RouterLink
        v-for="item in items"
        :key="item.label"
        :to="item.to"
        active-class=""
        exact-active-class=""
        class="nav-item"
        :class="{ active: isActive(item) }"
        @click="onSelect()"
      >
        <span class="nav-icon-wrap">
          <!--
            设计稿的图标是 0.5px 描边的轮廓线，必须按自己的视区尺寸渲染，
            不能走 AppIcon（那是 24×24 / 1.7px 的另一套），否则粗细完全不同。
          -->
          <svg
            class="nav-glyph"
            :width="glyphOf(item.glyph).size"
            :height="glyphOf(item.glyph).size"
            :viewBox="`0 0 ${glyphOf(item.glyph).size} ${glyphOf(item.glyph).size}`"
            fill="none"
            stroke="currentColor"
            stroke-width="0.5"
            aria-hidden="true"
            focusable="false"
          >
            <path v-for="(d, i) in glyphOf(item.glyph).paths" :key="i" :d="d" />
          </svg>
        </span>
        <span class="nav-label">{{ item.label }}</span>
        <!-- 右侧标记：未选中是箭头，选中换成圆点，两者绝对定位在各自的位置上 -->
        <svg
          class="nav-chevron"
          width="8"
          height="10"
          viewBox="0 0 8 10"
          fill="none"
          stroke="currentColor"
          stroke-width="1.5"
          aria-hidden="true"
          focusable="false"
        >
          <path d="M2 2C2 2 6 3 6 5 6 7 2 8 2 8" />
        </svg>
        <span class="nav-dot" aria-hidden="true" />
      </RouterLink>
    </nav>
  </aside>
</template>

<style scoped>
/*
 * 侧栏没有面板底色，直接透出 `.app-shell` 的页面氛围 ——
 * 唯一属于侧栏的装饰是左下角那团柔光（见下面的 ::after）。
 * 下面所有尺寸都对应设计稿：胶囊左缘为基准，右缘按「距胶囊右边多少」对齐。
 */
.sidebar {
  position: relative;
  /* 必须保留：否则 z-index:-1 的辉光会掉到 .app-shell 背景之后，整团看不见 */
  isolation: isolate;
  width: var(--sc-sidebar-w);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 20px 12px 16px;
}

/*
 * 左下角柔光：底边与侧栏底边齐平，向左溢出 150px（超出视口的部分自然被裁掉）。
 * 右缘会稍微漫进内容区的左侧留白，和设计稿的构图一致。
 */
.sidebar::after {
  position: absolute;
  z-index: -1;
  bottom: 0;
  left: -150px;
  width: 460px;
  height: 420px;
  border-radius: 50%;
  background: var(--sc-sidebar-glow-primary);
  content: '';
  pointer-events: none;
  filter: blur(26px);
}

/*
 * 品牌区按设计稿的实测行盒排：瓷贴顶 46、文字两行分别 33 / 16 高，
 * 文字行盒比瓷贴高 4px，所以瓷贴要靠 margin 压下去对齐。
 */
.brand {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 22px 2px 0 9.5px;
}

.brand-mark {
  position: relative;
  width: 50px;
  height: 50px;
  flex-shrink: 0;
  margin-top: 4px;
  filter: var(--sc-brand-glow);
}

.brand-tile {
  display: block;
  width: 50px;
  height: 50px;
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
  font-size: var(--sc-font-size-5xl);
  font-weight: 800;
  line-height: 1;
}

.brand-text {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.brand-name {
  font-size: var(--sc-font-size-5xl);
  font-weight: 700;
  color: var(--sc-text);
  line-height: 33px;
}

/* 副标题那一行：文案在左，「· ✧」被推到最右，与设计稿的右对齐一致 */
.brand-sub-row {
  display: flex;
  align-items: center;
  line-height: 16px;
}

.brand-sub {
  font-size: var(--sc-font-size-sm);
  color: var(--sc-text-secondary);
  white-space: nowrap;
}

.brand-deco {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-left: 8px;
  color: var(--sc-text-secondary);
}

.brand-dot {
  width: 2px;
  height: 2px;
  border-radius: 50%;
  background: currentColor;
}

.brand-star {
  display: block;
}

.brand-star path {
  fill: none;
  stroke: currentColor;
  stroke-width: 1;
}

.sidebar-divider {
  height: 1px;
  margin: 18px 3px 12px 9px;
  background: var(--sc-border-soft);
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/*
 * 高度由内边距 + 30px 图标圆自然得出（9 + 30 + 9 = 48），刻意不写死 height ——
 * 新增皮肤需要调整 padding 时，对应覆盖仍然可以生效。
 * 右侧内边距 6.75 是给绝对定位的箭头留位（设计稿里箭头盒右缘距胶囊右缘 6.75）。
 */
.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 6.75px 9px 13.25px;
  overflow: hidden;
  border-radius: var(--sc-nav-radius);
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-lg);
  font-weight: 500;
  transition:
    background 0.18s ease,
    color 0.18s ease;
}

/* 悬停反馈只给真的能跳转的条目；占位项保持静止，避免暗示可点 */
a.nav-item:hover {
  background: var(--sc-nav-hover-bg);
  color: var(--sc-text);
}

.nav-item-static {
  cursor: default;
}

.nav-item:focus-visible {
  outline: 2px solid var(--sc-focus-ring);
  outline-offset: -2px;
}

/* 选中态只换底色与标记：设计稿里文字既不换色也不加粗 */
.nav-item.active {
  background: var(--sc-nav-active-bg);
}

/* 选中强调条：贴在胶囊最左缘，overflow:hidden 让它随圆角自然收口 */
.nav-item::before {
  position: absolute;
  top: 50%;
  left: 0;
  width: 4px;
  height: 22px;
  border-radius: 2px;
  background: var(--sc-nav-accent);
  content: '';
  opacity: 0;
  transform: translateY(-50%);
  transition: opacity 0.18s ease;
}

.nav-item.active::before {
  opacity: 1;
}

.nav-icon-wrap {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--sc-nav-icon-bg);
  color: var(--sc-nav-icon);
  transition: background 0.18s ease;
}

.nav-item.active .nav-icon-wrap {
  background: var(--sc-nav-icon-bg-active);
}

.nav-glyph {
  display: block;
}

.nav-label {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 右侧标记槽：箭头与圆点各自绝对定位，位置按设计稿的实测值给死 */
.nav-chevron,
.nav-dot {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  transition: opacity 0.18s ease;
}

.nav-chevron {
  right: 6.75px;
  color: var(--sc-text-placeholder);
}

.nav-dot {
  right: 9px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--sc-nav-accent);
  opacity: 0;
}

.nav-item.active .nav-chevron {
  opacity: 0;
}

.nav-item.active .nav-dot {
  opacity: 1;
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
    overflow: hidden;
    box-shadow: var(--sc-shadow-lg);
    /* 桌面端侧栏是透明的；抽成浮层后必须有不透明底，否则压在内容上会穿帮 */
    background: var(--sc-panel-strong);
  }
  .sidebar.open {
    transform: translateX(0);
  }
}
</style>
