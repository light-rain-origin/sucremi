<script setup lang="ts"></script>

<template>
  <section>
    <h2 class="page-h">概览</h2>
    <p class="muted">首页 Dashboard 占位 —— 统计卡、最近项目与下一步行动将在后续任务接入真实数据。</p>
    <el-card shadow="never" class="demo-card">
      <div class="demo-row">
        <el-button type="primary">主色按钮</el-button>
        <el-button>默认按钮</el-button>
        <el-tag>标签</el-tag>
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.page-h {
  margin: 0 0 8px;
  font-size: 20px;
}
.muted {
  color: var(--sc-text-secondary);
  margin: 0 0 16px;
}
.demo-card {
  max-width: 420px;
  border-radius: var(--sc-radius-lg);
}
.demo-row {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
