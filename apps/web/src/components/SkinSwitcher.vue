<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { SKINS, useSkinStore } from '@/stores/skin'

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
      <svg class="skin-trigger-icon" viewBox="0 0 24 24" aria-hidden="true">
        <path d="M12 2.8c3.35 3.85 6.2 7.35 6.2 11.05a6.2 6.2 0 0 1-12.4 0c0-3.7 2.85-7.2 6.2-11.05Z" />
      </svg>
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
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  padding: 0;
  border: 1px solid rgba(155, 134, 248, 0.8);
  border-radius: 50%;
  background: #f3eaff;
  color: #9b86f8;
  font-family: inherit;
  font-size: var(--sc-font-size-md);
  cursor: pointer;
  transition:
    color 0.18s ease,
    border-color 0.18s ease,
    background 0.18s ease;
}

.skin-trigger:hover {
  color: #5b43c2;
  background: #e9ddff;
}

.skin-trigger:focus-visible {
  outline: 2px solid var(--sc-brand);
  outline-offset: 2px;
}

.skin-trigger-label {
  display: none;
}

.skin-trigger-icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
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

</style>
