<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '@/components/AppIcon.vue'
import SkinSwitcher from '@/components/SkinSwitcher.vue'

defineEmits<{ toggle: [] }>()

const route = useRoute()
const router = useRouter()
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
const colorModeButton = ref<HTMLButtonElement | null>(null)
const notificationOpen = ref(false)
const notificationButton = ref<HTMLButtonElement | null>(null)
const searchExpanded = ref(false)
const searchQuery = ref('')
const searchInput = ref<HTMLInputElement | null>(null)

watch(
  () => route.query.q,
  (value) => {
    const search = Array.isArray(value) ? value[0] : value
    searchQuery.value = typeof search === 'string' ? search : ''
  },
  { immediate: true },
)

watch(notificationOpen, (open, _previous, onCleanup) => {
  if (!open) return

  const handlePointerDown = (event: PointerEvent) => {
    const target = event.target
    const popover = document.querySelector('.notification-popover-popper')
    if (target instanceof Node && (notificationButton.value?.contains(target) || popover?.contains(target))) return
    notificationOpen.value = false
  }
  const handleKeydown = (event: KeyboardEvent) => {
    if (event.key === 'Escape') notificationOpen.value = false
  }

  document.addEventListener('pointerdown', handlePointerDown, true)
  document.addEventListener('keydown', handleKeydown, true)
  onCleanup(() => {
    document.removeEventListener('pointerdown', handlePointerDown, true)
    document.removeEventListener('keydown', handleKeydown, true)
  })
})

function applyColorMode() {
  document.documentElement.dataset.colorMode = colorMode.value
  document.documentElement.style.colorScheme = colorMode.value
}

applyColorMode()

function commitColorMode(nextMode: ColorMode) {
  colorMode.value = nextMode
  applyColorMode()
  try {
    window.localStorage.setItem(COLOR_MODE_KEY, colorMode.value)
  } catch {
    // 本次会话仍可切换；存储不可用时刷新后回到浅色。
  }
}

function toggleColorMode(event: MouseEvent) {
  const nextMode: ColorMode = colorMode.value === 'light' ? 'dark' : 'light'
  const button = colorModeButton.value
  if (button) {
    const bounds = button.getBoundingClientRect()
    const originX = event.detail > 0 ? event.clientX : bounds.left + bounds.width / 2
    const originY = event.detail > 0 ? event.clientY : bounds.top + bounds.height / 2
    document.documentElement.style.setProperty('--sc-theme-reveal-x', `${originX}px`)
    document.documentElement.style.setProperty('--sc-theme-reveal-y', `${originY}px`)
  }

  const transitionDocument = document as Document & {
    startViewTransition?: (update: () => void | Promise<void>) => { finished: Promise<void> }
  }
  const startViewTransition = transitionDocument.startViewTransition
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

  if (!startViewTransition || reducedMotion) {
    commitColorMode(nextMode)
    return
  }

  try {
    const transition = startViewTransition.call(document, async () => {
      commitColorMode(nextMode)
      await nextTick()
    })
    void transition.finished.catch(() => undefined)
  } catch {
    commitColorMode(nextMode)
  }
}

function focusSearch() {
  searchExpanded.value = true
  searchInput.value?.focus()
}

function collapseSearch() {
  searchExpanded.value = false
}

function submitSearch() {
  const query = searchQuery.value.trim()
  if (!query) return

  searchExpanded.value = false
  searchInput.value?.blur()
  void router.push({ name: 'projects', query: { q: query } })
}

function handleSearchShortcut(event: KeyboardEvent) {
  if (!(event.ctrlKey || event.metaKey) || event.key.toLowerCase() !== 'k') return
  event.preventDefault()
  focusSearch()
}

onMounted(() => window.addEventListener('keydown', handleSearchShortcut))
onBeforeUnmount(() => window.removeEventListener('keydown', handleSearchShortcut))
</script>

<template>
  <header class="app-top-navigation">
    <div class="navigation-heading">
      <button class="menu-toggle" aria-label="菜单" @click="$emit('toggle')">
        <AppIcon name="menu" :size="20" />
      </button>
      <h1 class="page-title">
        <span class="page-title-accent" aria-hidden="true" />
        <span class="page-title-viewport">
          <Transition :name="titleTransition">
            <span :key="title" class="page-title-text">{{ title }}</span>
          </Transition>
        </span>
      </h1>
    </div>

    <form
      class="search-display"
      :class="{ 'is-expanded': searchExpanded }"
      role="search"
      aria-label="搜索项目"
      @submit.prevent="submitSearch"
      @click="focusSearch"
    >
      <svg class="search-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <circle cx="10.8" cy="10.8" r="6.6" />
        <path d="m16 16 4.2 4.2" />
      </svg>
      <input
        ref="searchInput"
        v-model="searchQuery"
        class="search-input"
        type="search"
        aria-label="搜索项目名称或简介"
        placeholder="搜索项目名称或简介..."
        autocomplete="off"
        @focus="searchExpanded = true"
        @blur="collapseSearch"
      />
      <span v-if="!searchExpanded" class="search-shortcut">Ctrl + K</span>
    </form>

    <div class="navigation-actions">
      <el-popover
        v-model:visible="notificationOpen"
        placement="bottom-end"
        trigger="click"
        :width="264"
        :show-arrow="false"
        popper-class="header-tool-popover notification-popover-popper"
      >
        <template #reference>
          <el-tooltip content="通知" placement="bottom" :show-after="350">
            <button
              class="toolbar-button notification-action"
              ref="notificationButton"
              type="button"
              aria-label="通知"
              aria-haspopup="dialog"
              :aria-expanded="notificationOpen"
              @keydown.esc.prevent.stop="notificationOpen = false"
            >
              <svg class="lucide-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M10.268 21a2 2 0 0 0 3.464 0" />
                <path d="M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.89 18 8a6 6 0 0 0-12 0c0 4.89-1.408 5.956-2.738 7.326Z" />
              </svg>
            </button>
          </el-tooltip>
        </template>
        <section class="notification-popover" role="dialog" aria-label="站内通知">
          <h2>站内通知</h2>
          <p>目前没有新通知</p>
        </section>
      </el-popover>
      <el-tooltip
        :content="colorMode === 'light' ? '切换至深色模式' : '切换至浅色模式'"
        placement="bottom"
        :show-after="350"
      >
        <button
          class="toolbar-button color-mode-action"
          ref="colorModeButton"
          type="button"
          :aria-label="colorMode === 'light' ? '切换至深色模式' : '切换至浅色模式'"
          @click="toggleColorMode"
        >
          <Transition name="toolbar-icon" mode="out-in">
            <svg :key="colorMode" class="lucide-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <template v-if="colorMode === 'light'">
                <circle cx="12" cy="12" r="4" />
                <path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42M17.65 17.65l1.42 1.42M2 12h2M20 12h2M4.93 19.07l1.42-1.42M17.65 6.35l1.42-1.42" />
              </template>
              <path v-else d="M20.985 12.486a9 9 0 1 1-9.473-9.472c.405-.022.617.46.402.803a6.5 6.5 0 0 0 8.268 8.268c.344-.215.825-.003.803.401" />
            </svg>
          </Transition>
        </button>
      </el-tooltip>
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
  grid-template-columns: minmax(0, 1fr) minmax(180px, 1.4fr) minmax(220px, 1fr);
  align-items: center;
  height: 48px;
  min-height: 48px;
  margin: 20px 21px 0;
  padding: 0 20px;
  border: 1px solid color-mix(in srgb, var(--sc-border-soft) 80%, transparent);
  border-radius: 15px;
  background: color-mix(in srgb, var(--sc-panel) 80%, transparent);
  box-shadow: none;
}

.navigation-heading,
.navigation-actions {
  display: flex;
  min-width: 0;
  align-items: center;
}

.navigation-heading {
  gap: 12px;
  margin-left: -4px;
}

.page-title {
  display: flex;
  width: 165px;
  max-width: 100%;
  min-width: 0;
  height: 18px;
  align-items: center;
  gap: 10px;
  flex: 0 1 165px;
  margin: 0;
  color: #51445f;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.2;
}

.page-title-accent {
  width: 3px;
  height: 16px;
  flex: 0 0 3px;
  border-radius: 999px;
  background: #d5b4f5;
}

.page-title-viewport {
  position: relative;
  height: 1.2em;
  min-width: 0;
  flex: 1 1 auto;
  overflow: hidden;
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

:global(html[data-color-mode='dark'] .page-title) {
  color: var(--sc-text-secondary);
}

:global(html[data-color-mode='dark'] .page-title-text) {
  color: var(--sc-text);
}

:global(html[data-color-mode='dark'] .page-title-accent) {
  background: color-mix(in srgb, var(--sc-brand-alt) 45%, var(--sc-bg));
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
  position: relative;
  z-index: 3;
  display: flex;
  min-width: 0;
  width: min(100%, clamp(220px, 24vw, 360px));
  max-width: 100%;
  height: 35px;
  align-items: center;
  gap: 8px;
  justify-self: center;
  margin: 0;
  padding: 0 9px 0 11px;
  border: 1px solid var(--sc-border-soft);
  border-radius: 999px;
  background: var(--sc-panel-strong);
  color: var(--sc-text-placeholder);
  transition:
    width 320ms cubic-bezier(0.4, 0, 0.2, 1),
    border-color 320ms cubic-bezier(0.4, 0, 0.2, 1),
    box-shadow 320ms cubic-bezier(0.4, 0, 0.2, 1);
}

.search-display.is-expanded {
  width: min(620px, max(100%, 40vw));
  border-color: var(--sc-brand-alt);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--sc-brand-alt) 10%, transparent);
}

.search-input {
  width: 100%;
  min-width: 0;
  padding: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--sc-text);
  font: inherit;
  font-size: 12px;
}

.search-input::placeholder {
  color: var(--sc-text-placeholder);
}

.search-input::-webkit-search-cancel-button {
  cursor: pointer;
}

.search-display:focus-within {
  border-color: var(--sc-brand-alt);
}

.search-icon {
  width: 15px;
  height: 15px;
  flex: 0 0 auto;
  stroke: #a99ac0;
  stroke-linecap: round;
  stroke-width: 2.4;
}

.search-placeholder {
  overflow: hidden;
  min-width: 0;
  flex: 1;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-shortcut {
  flex: 0 0 auto;
  padding: 2px 6px;
  border: 1px solid var(--sc-border-soft);
  border-radius: 10px;
  color: var(--sc-text-secondary);
  font-size: 10px;
  line-height: 1.2;
}

.navigation-actions {
  justify-content: flex-end;
  gap: 8px;
}

.navigation-actions :deep(.toolbar-button) {
  position: relative;
  display: grid;
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  place-items: center;
  padding: 0;
  border: 0;
  border-radius: 9px;
  background: transparent;
  color: #9a80bc;
  cursor: pointer;
  transition: color 150ms ease, background-color 150ms ease;
}

.navigation-actions :deep(.toolbar-button:hover) {
  color: #8062a9;
  background: #f3ebfa;
}

.navigation-actions :deep(.toolbar-button:active) {
  background: #ede0f8;
}

.navigation-actions :deep(.toolbar-button:focus-visible) {
  outline: 2px solid var(--sc-brand-alt-strong);
  outline-offset: 2px;
}

.navigation-actions :deep(.lucide-icon) {
  width: 16px;
  height: 16px;
  flex: 0 0 auto;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

:global(html[data-color-mode='dark']) .navigation-actions :deep(.toolbar-button) {
  color: var(--sc-text-secondary);
}

:global(html[data-color-mode='dark']) .navigation-actions :deep(.toolbar-button:hover) {
  color: var(--sc-brand-alt);
  background: var(--sc-nav-icon-bg);
}

:global(html[data-color-mode='dark']) .navigation-actions :deep(.toolbar-button:active) {
  background: var(--sc-nav-icon-bg-active);
}

.toolbar-icon-enter-active,
.toolbar-icon-leave-active {
  transition: opacity 150ms ease, transform 150ms ease;
}

.toolbar-icon-enter-from {
  opacity: 0;
  transform: translateY(2px);
}

.toolbar-icon-leave-to {
  opacity: 0;
  transform: translateY(-2px);
}

@media (prefers-reduced-motion: reduce) {
  .search-display {
    transition-duration: 1ms;
  }

  .navigation-actions :deep(.toolbar-button) {
    transition-duration: 1ms;
  }

  .toolbar-icon-enter-active,
  .toolbar-icon-leave-active {
    transition-duration: 1ms;
  }
}

.notification-popover h2 {
  margin: 0;
  color: var(--sc-text);
  font-size: 14px;
  font-weight: 600;
}

.notification-popover p {
  margin: 20px 0 12px;
  color: var(--sc-text-secondary);
  font-size: 13px;
  text-align: center;
}

:global(.header-tool-popover) {
  --el-popover-bg-color: var(--sc-card-surface);
  --el-popover-border-color: var(--sc-border-soft);
  border-radius: var(--sc-radius-lg);
}

.profile-display {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-left: 8px;
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
    grid-template-columns: minmax(0, 1fr) minmax(160px, 1fr) minmax(220px, 1fr);
    margin-right: 16px;
    margin-left: 16px;
    padding-right: 14px;
    padding-left: 20px;
  }

  .navigation-actions {
    gap: 8px;
  }
}

@media (max-width: 900px) and (min-width: 769px) {
  .app-top-navigation {
    grid-template-columns: minmax(0, 1fr) minmax(160px, 1.2fr) minmax(0, 1fr);
  }

  .profile-display {
    display: none;
  }
}

@media (max-width: 768px) {
  .app-top-navigation {
    grid-template-columns: minmax(0, 1fr) auto;
    height: 45px;
    min-height: 45px;
    margin: 20px 12px 0;
    padding: 0 12px;
    border-radius: 16px;
  }

  .menu-toggle {
    display: inline-flex;
  }

  .navigation-heading {
    margin-left: 0;
  }

  .search-display {
    display: none;
  }

  .navigation-actions {
    gap: 8px;
  }

  .profile-display {
    display: none;
  }
}
</style>
