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
  <el-tag :type="map[status].type" effect="light" round>{{ map[status].label }}</el-tag>
</template>
