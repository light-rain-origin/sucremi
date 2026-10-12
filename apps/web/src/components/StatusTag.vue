<script setup lang="ts">
import type { ProjectStatus } from '@/types/project'

defineProps<{ status: ProjectStatus }>()

const map: Record<ProjectStatus, { label: string; type: 'primary' | 'warning' | 'success' }> = {
  ACTIVE: { label: '进行中', type: 'primary' },
  PAUSED: { label: '暂停', type: 'warning' },
  DONE: { label: '完成', type: 'success' },
}
</script>

<template>
  <el-tag :type="map[status].type" effect="light" round size="default" class="status-tag">
    <span class="status-dot" aria-hidden="true" />
    {{ map[status].label }}
  </el-tag>
</template>

<style scoped>
/* 小圆点强化状态区分；颜色沿用 el-tag 的类型色 */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 0 10px;
  font-weight: 500;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
</style>
