<script setup lang="ts">
import type { Project } from '@/types/project'
import { formatDateTime } from '@/utils/datetime'
import StatusTag from './StatusTag.vue'
import ProgressBar from './ProgressBar.vue'

defineProps<{ project: Project }>()
defineEmits<{ open: [id: number] }>()
</script>

<template>
  <el-card shadow="hover" class="project-card" @click="$emit('open', project.id)">
    <div class="pc-head">
      <span class="pc-name">{{ project.name }}</span>
      <StatusTag :status="project.status" />
    </div>
    <p class="pc-summary">{{ project.summary || '暂无简介' }}</p>
    <ProgressBar :value="project.progress" />
    <div class="pc-foot">更新于 {{ formatDateTime(project.updatedAt) }}</div>
  </el-card>
</template>

<style scoped>
.project-card {
  border-radius: var(--sc-radius-lg);
  cursor: pointer;
}
.pc-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.pc-name {
  font-weight: 600;
  font-size: 16px;
  color: var(--sc-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.pc-summary {
  color: var(--sc-text-secondary);
  margin: 8px 0 12px;
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.pc-foot {
  margin-top: 10px;
  font-size: 12px;
  color: var(--sc-text-placeholder);
}
</style>
