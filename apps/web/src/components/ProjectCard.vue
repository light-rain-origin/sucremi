<script setup lang="ts">
import type { Project } from '@/types/project'
import { formatDateTime } from '@/utils/datetime'
import AppIcon from './AppIcon.vue'
import StatusTag from './StatusTag.vue'
import ProgressBar from './ProgressBar.vue'

defineProps<{ project: Project }>()
defineEmits<{ open: [id: number] }>()
</script>

<template>
  <el-card shadow="never" class="project-card" @click="$emit('open', project.id)">
    <div class="pc-head">
      <span class="pc-name">{{ project.name }}</span>
      <StatusTag :status="project.status" />
    </div>
    <p class="pc-summary">{{ project.summary || '暂无简介' }}</p>
    <ProgressBar :value="project.progress" />
    <div class="pc-foot">
      <AppIcon name="clock" :size="14" />
      <span>更新于 {{ formatDateTime(project.updatedAt) }}</span>
    </div>
  </el-card>
</template>

<style scoped>
.project-card {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition:
    box-shadow 0.2s ease,
    border-color 0.2s ease,
    transform 0.2s ease;
}

/* 顶部细渐变条：呼应侧栏选中项的品牌标记 */
.project-card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 3px;
  background: var(--sc-gradient-brand);
  opacity: 0.85;
}

.project-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--sc-shadow-lg);
  border-color: var(--sc-pink);
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
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  font-size: 12px;
  color: var(--sc-text-placeholder);
}
</style>
