<script setup lang="ts">
defineProps<{ value: number }>()
</script>

<template>
  <el-progress :percentage="value" :stroke-width="10" />
</template>

<style scoped>
/* 进度条填充走品牌渐变，与统计卡的强调色呼应。
   :deep() 生成的 `[data-v-*] .el-progress__*` 选择器优先级已高于 Element Plus 自身规则，
   因此无需 !important。 */
:deep(.el-progress-bar__inner) {
  background-image: var(--sc-gradient-brand);
  background-color: var(--sc-brand);
  border-radius: var(--sc-radius-pill);
}

:deep(.el-progress-bar__outer) {
  border-radius: var(--sc-radius-pill);
}

:deep(.el-progress__text) {
  color: var(--sc-brand-alt-strong);
  font-size: var(--sc-font-size-md);
  font-weight: 600;
}
</style>
