<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchHealth, type HealthInfo } from '@/api/health'

const loading = ref(false)
const health = ref<HealthInfo | null>(null)

async function checkHealth() {
  loading.value = true
  try {
    const res = await fetchHealth()
    if (res.code === 0) {
      health.value = res.data
      ElMessage.success('后端连接正常')
    } else {
      ElMessage.error(res.message || '健康检查失败')
    }
  } catch (e) {
    ElMessage.error('无法连接后端：' + (e as Error).message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="home">
    <h2>Sucremi 工作台</h2>
    <p class="subtitle">个人开发工作台 — 阶段 0 工程骨架。</p>
    <el-button type="primary" :loading="loading" @click="checkHealth">
      检查后端健康状态
    </el-button>
    <el-card v-if="health" class="health-card">
      <p>状态：{{ health.status }}</p>
      <p>应用：{{ health.app }}</p>
      <p>时间：{{ health.time }}</p>
    </el-card>
  </div>
</template>

<style scoped>
.home {
  padding: 24px;
}
.subtitle {
  color: var(--el-text-color-secondary, #909399);
  margin-bottom: 16px;
}
.health-card {
  margin-top: 16px;
  max-width: 360px;
}
</style>
