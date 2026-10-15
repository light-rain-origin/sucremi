<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { SKINS, useSkinStore } from '@/stores/skin'
import AppIcon from '@/components/AppIcon.vue'

/** 皮肤切换入口。只切换样式：不触发任何请求，也不影响页面结构与数据。 */
const skin = useSkinStore()

const currentLabel = computed(() => SKINS.find((s) => s.id === skin.current)?.label ?? '皮肤')

/** el-dropdown 的 command 是宽松类型，合法性与去重都交给 store 判断。 */
async function onCommand(command: unknown) {
  try {
    await skin.apply(command)
  } catch {
    // store 是「先加载样式、后落属性」，失败时外观未被改动，这里如实告知即可
    ElMessage.error('皮肤样式加载失败，已保持当前外观')
  }
}
</script>

<template>
  <el-dropdown trigger="click" placement="bottom-end" @command="onCommand">
    <button class="skin-trigger" type="button" :title="`当前皮肤：${currentLabel}`">
      <AppIcon name="droplet" :size="16" />
      <span class="skin-trigger-label">{{ currentLabel }}</span>
    </button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="s in SKINS"
          :key="s.id"
          :command="s.id"
          :disabled="s.id === skin.current"
        >
          <span class="skin-option">
            <span class="skin-swatch" :class="`swatch-${s.id}`" aria-hidden="true" />
            <span class="skin-name">{{ s.label }}</span>
            <span class="skin-hint">{{ s.hint }}</span>
          </span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped>
.skin-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-pill);
  background: var(--sc-panel-strong);
  color: var(--sc-text-secondary);
  font-family: inherit;
  font-size: var(--sc-font-size-md);
  cursor: pointer;
  transition:
    color 0.18s ease,
    border-color 0.18s ease,
    background 0.18s ease;
}

.skin-trigger:hover {
  color: var(--sc-brand-strong);
  border-color: var(--sc-brand);
  background: var(--sc-brand-soft);
}

.skin-trigger:focus-visible {
  outline: 2px solid var(--sc-brand);
  outline-offset: 2px;
}

.skin-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.skin-swatch {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
  border-radius: 50%;
  box-shadow: inset 0 0 0 1px var(--sc-swatch-ring);
}

.swatch-classic {
  background: var(--sc-swatch-classic);
}

.skin-name {
  font-weight: 500;
}

.skin-hint {
  color: var(--sc-text-placeholder);
  font-size: var(--sc-font-size-sm);
}

/* 窄屏只留图标，把标题栏空间让给页面标题 */
@media (max-width: 768px) {
  .skin-trigger-label {
    display: none;
  }

  .skin-trigger {
    padding: 6px 9px;
  }
}
</style>
