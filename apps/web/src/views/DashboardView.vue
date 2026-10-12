<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/stores/dashboard'
import { useProjectStore } from '@/stores/project'
import type { ProjectForm } from '@/types/project'
import StatCard from '@/components/StatCard.vue'
import ProjectCard from '@/components/ProjectCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import ProjectFormDialog from '@/components/ProjectFormDialog.vue'

const router = useRouter()
const dashboard = useDashboardStore()
const projectStore = useProjectStore()
const { summary, loading, error } = storeToRefs(dashboard)

const dialogVisible = ref(false)

const recent = computed(() => summary.value?.recentProjects ?? [])

const stats = computed(() => {
  const d = summary.value
  return [
    { label: '全部项目', value: d?.totalProjects ?? '—' },
    { label: '进行中', value: d?.activeProjects ?? '—' },
    { label: '已暂停', value: d?.pausedProjects ?? '—' },
    { label: '已完成', value: d?.doneProjects ?? '—' },
    { label: '已归档', value: d?.archivedProjects ?? '—' },
  ]
})

onMounted(() => dashboard.fetchSummary())

function openDetail(id: number) {
  router.push(`/projects/${id}`)
}

async function onCreate(form: ProjectForm) {
  try {
    const project = await projectStore.create(form)
    dialogVisible.value = false
    ElMessage.success('项目已创建')
    dashboard.fetchSummary()
    router.push(`/projects/${project.id}`)
  } catch (e) {
    ElMessage.error((e as Error).message || '创建失败')
  }
}
</script>

<template>
  <section class="dashboard">
    <div class="head">
      <div>
        <h2 class="greeting">今天推进哪一步？</h2>
        <p class="muted">这里是你的开发工作台，先看一眼全局，再挑一个项目继续。</p>
      </div>
      <el-button type="primary" @click="dialogVisible = true">新建项目</el-button>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="mb" />

    <div v-loading="loading" class="stats">
      <StatCard v-for="s in stats" :key="s.label" :label="s.label" :value="s.value" />
    </div>

    <el-card shadow="never" class="block">
      <template #header>
        <div class="block-head">
          <span class="block-title">最近更新</span>
          <RouterLink to="/projects" class="more">全部项目 →</RouterLink>
        </div>
      </template>
      <EmptyState v-if="!loading && recent.length === 0" text="还没有项目，创建第一个吧">
        <el-button type="primary" @click="dialogVisible = true">新建项目</el-button>
      </EmptyState>
      <div v-else class="grid">
        <ProjectCard v-for="p in recent" :key="p.id" :project="p" @open="openDetail" />
      </div>
    </el-card>

    <el-card shadow="never" class="block placeholder">
      <template #header>
        <div class="block-head"><span class="block-title">下一步行动</span></div>
      </template>
      <p class="muted">
        任务清单与「下一步做什么」的提醒将在后续版本推出，用于中断后快速找回上下文。
      </p>
    </el-card>

    <ProjectFormDialog v-model="dialogVisible" mode="create" @submit="onCreate" />
  </section>
</template>

<style scoped>
.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.greeting {
  margin: 0 0 4px;
  font-size: 20px;
}
.muted {
  color: var(--sc-text-secondary);
  margin: 0;
}
.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 14px;
  margin-bottom: 20px;
}
.block {
  border-radius: var(--sc-radius-lg);
  margin-bottom: 20px;
}
.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.block-title {
  font-weight: 600;
}
.more {
  font-size: 13px;
  color: var(--sc-text-secondary);
}
.more:hover {
  color: var(--sc-primary);
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.placeholder {
  border-style: dashed;
}
.placeholder .muted {
  font-size: 13px;
  line-height: 1.7;
}
.mb {
  margin-bottom: 12px;
}
</style>
