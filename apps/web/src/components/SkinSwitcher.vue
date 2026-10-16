<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { SKINS, useSkinStore } from '@/stores/skin'

/** 皮肤选择复用已有 store；当前清单只有经典粉紫，不在此处伪造额外配色。 */
const skin = useSkinStore()
const popoverOpen = ref(false)
const paletteButton = ref<HTMLButtonElement | null>(null)
const currentLabel = computed(() => SKINS.find((item) => item.id === skin.current)?.label ?? '皮肤')

watch(popoverOpen, (open, _previous, onCleanup) => {
  if (!open) return

  const handlePointerDown = (event: PointerEvent) => {
    const target = event.target
    const popover = document.querySelector('.skin-palette-popper')
    if (target instanceof Node && (paletteButton.value?.contains(target) || popover?.contains(target))) return
    popoverOpen.value = false
  }
  const handleKeydown = (event: KeyboardEvent) => {
    if (event.key === 'Escape') popoverOpen.value = false
  }

  document.addEventListener('pointerdown', handlePointerDown, true)
  document.addEventListener('keydown', handleKeydown, true)
  onCleanup(() => {
    document.removeEventListener('pointerdown', handlePointerDown, true)
    document.removeEventListener('keydown', handleKeydown, true)
  })
})

async function selectSkin(command: unknown) {
  try {
    await skin.apply(command)
    popoverOpen.value = false
  } catch {
    ElMessage.error('皮肤样式加载失败，已保持当前外观')
  }
}
</script>

<template>
  <el-popover
    v-model:visible="popoverOpen"
    placement="bottom-end"
    trigger="click"
    :width="232"
    :show-arrow="false"
    popper-class="header-tool-popover skin-palette-popper"
  >
    <template #reference>
      <el-tooltip content="主题配色" placement="bottom" :show-after="350">
        <button
          class="toolbar-button skin-trigger"
          ref="paletteButton"
          type="button"
          :aria-label="`主题配色，当前为${currentLabel}`"
          aria-haspopup="dialog"
          :aria-expanded="popoverOpen"
        >
          <svg class="lucide-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M12 22a1 1 0 0 1 0-20 10 10 0 0 1 10 10 4 4 0 0 1-4 4h-2.2a1.8 1.8 0 0 0-1.6 2.7" />
            <circle cx="7.5" cy="10.5" r=".75" fill="currentColor" stroke="none" />
            <circle cx="12" cy="7.5" r=".75" fill="currentColor" stroke="none" />
            <circle cx="16.5" cy="10.5" r=".75" fill="currentColor" stroke="none" />
            <circle cx="9" cy="16.5" r=".75" fill="currentColor" stroke="none" />
          </svg>
        </button>
      </el-tooltip>
    </template>

    <section class="skin-popover" role="dialog" aria-label="主题配色">
      <h2>主题配色</h2>
      <div class="palette-options" role="radiogroup" aria-label="选择主题配色">
        <button
          v-for="item in SKINS"
          :key="item.id"
          class="palette-option"
          type="button"
          role="radio"
          :aria-checked="skin.current === item.id"
          @click="selectSkin(item.id)"
        >
          <span class="skin-swatch" :class="`swatch-${item.id}`" aria-hidden="true" />
          <span class="skin-option-copy">
            <span class="skin-name">{{ item.label }}</span>
            <span class="skin-hint">{{ item.hint }}</span>
          </span>
          <svg v-if="skin.current === item.id" class="palette-check" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <path d="m3.5 8 3 3 6-6" />
          </svg>
        </button>
      </div>
    </section>
  </el-popover>
</template>

<style scoped>
.skin-popover h2 {
  margin: 0 0 10px;
  color: var(--sc-text);
  font-size: 14px;
  font-weight: 600;
}

.palette-options {
  display: grid;
  gap: 4px;
}

.palette-option {
  display: flex;
  width: 100%;
  min-height: 48px;
  align-items: center;
  gap: 10px;
  padding: 7px 9px;
  border: 1px solid transparent;
  border-radius: var(--sc-radius-md);
  background: transparent;
  color: var(--sc-text);
  text-align: left;
  cursor: pointer;
  transition: background-color 150ms ease, border-color 150ms ease;
}

.palette-option:hover {
  background: var(--sc-bg-muted);
}

.palette-option[aria-checked='true'] {
  border-color: var(--sc-border-soft);
  background: var(--sc-panel-faint);
}

.palette-option:focus-visible {
  outline: 2px solid var(--sc-brand-alt-strong);
  outline-offset: 2px;
}

.skin-swatch {
  width: 26px;
  height: 26px;
  flex: 0 0 auto;
  border-radius: 8px;
  background: var(--sc-gradient-brand);
  box-shadow: inset 0 0 0 1px var(--sc-swatch-ring);
}

.skin-option-copy {
  display: grid;
  flex: 1;
  gap: 2px;
}

.skin-name {
  font-size: 13px;
  font-weight: 600;
}

.skin-hint {
  color: var(--sc-text-placeholder);
  font-size: 11px;
}

.palette-check {
  width: 16px;
  height: 16px;
  stroke: var(--sc-brand-alt-strong);
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}
</style>
