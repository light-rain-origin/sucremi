<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <section>
    <h2 class="page-h">项目详情</h2>
    <p class="muted">项目 #{{ route.params.id }} 详情占位 —— 概览/任务/时间线/检查点将在 T10 接入。</p>
  </section>
</template>

<style scoped>
.page-h {
  margin: 0 0 8px;
  font-size: 20px;
}
.muted {
  color: var(--sc-text-secondary);
}
</style>
