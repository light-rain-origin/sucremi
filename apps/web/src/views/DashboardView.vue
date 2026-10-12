<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/stores/dashboard'
import { useProjectStore } from '@/stores/project'
import type { ProjectForm } from '@/types/project'
import AppIcon from '@/components/AppIcon.vue'
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

/** 统计项与后端的 DashboardSummary 字段一一对应，仅补充展示用的图标与色调。 */
const stats = computed(() => {
  const d = summary.value
  return [
    { label: '全部项目', value: d?.totalProjects ?? '—', icon: 'grid', tone: 'purple' },
    { label: '进行中', value: d?.activeProjects ?? '—', icon: 'play', tone: 'pink' },
    { label: '已暂停', value: d?.pausedProjects ?? '—', icon: 'pause', tone: 'amber' },
    { label: '已完成', value: d?.doneProjects ?? '—', icon: 'check', tone: 'mint' },
    { label: '已归档', value: d?.archivedProjects ?? '—', icon: 'archive', tone: 'slate' },
  ] as const
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
    <!-- 欢迎区：粉紫柔光面板，星星仅为装饰（aria-hidden），不承载信息 -->
    <section class="hero">
      <span class="hero-glow" aria-hidden="true" />
      <AppIcon class="hero-sparkle s1" name="sparkle" :size="18" />
      <AppIcon class="hero-sparkle s2" name="sparkle" :size="12" />
      <div class="hero-text">
        <h2 class="greeting">今天推进哪一步？</h2>
        <p class="hero-sub">这里是你的开发工作台：先看一眼全局，再挑一个项目继续。</p>
      </div>
      <el-button type="primary" size="large" @click="dialogVisible = true">
        <AppIcon name="plus" :size="16" />新建项目
      </el-button>
    </section>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="mb" />

    <div v-loading="loading" class="stats">
      <StatCard
        v-for="s in stats"
        :key="s.label"
        :label="s.label"
        :value="s.value"
        :icon="s.icon"
        :tone="s.tone"
      />
    </div>

    <el-card shadow="never" class="block">
      <template #header>
        <div class="block-head">
          <span class="block-title"><AppIcon name="folder" :size="17" />最近更新</span>
          <RouterLink to="/projects" class="more">
            全部项目<AppIcon name="arrow-right" :size="14" />
          </RouterLink>
        </div>
      </template>
      <EmptyState v-if="!loading && recent.length === 0" text="还没有项目，创建第一个吧">
        <el-button type="primary" @click="dialogVisible = true">
          <AppIcon name="plus" :size="16" />新建项目
        </el-button>
      </EmptyState>
      <div v-else class="grid">
        <ProjectCard v-for="p in recent" :key="p.id" :project="p" @open="openDetail" />
      </div>
    </el-card>

    <!-- 占位区：明确标注尚未实现，避免看起来像可用功能 -->
    <el-card shadow="never" class="block placeholder">
      <template #header>
        <div class="block-head">
          <span class="block-title"><AppIcon name="list" :size="17" />下一步行动</span>
          <span class="soon-pill">规划中 · 尚未实现</span>
        </div>
      </template>
      <p class="muted">
        任务清单与「下一步做什么」的提醒将在后续版本推出，用于中断后快速找回上下文。
      </p>
    </el-card>

    <ProjectFormDialog v-model="dialogVisible" mode="create" @submit="onCreate" />
  </section>
</template>

<style scoped>
/* ---- 欢迎区 ---- */
.hero {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 24px 26px;
  margin-bottom: 20px;
  border-radius: var(--sc-radius-xl);
  border: 1px solid var(--sc-border-soft);
  background: var(--sc-gradient-soft);
  box-shadow: var(--sc-shadow-sm);
}

/* 右上角的粉色柔光，让面板不那么平 */
.hero-glow {
  position: absolute;
  top: -70px;
  right: -40px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(245, 168, 203, 0.42), transparent 68%);
}

.hero-text {
  position: relative;
}

.greeting {
  margin: 0 0 4px;
  font-size: 21px;
  font-weight: 700;
}

.hero-sub {
  margin: 0;
  color: var(--sc-text-secondary);
}

.hero-sparkle {
  position: absolute;
  color: var(--sc-pink);
  opacity: 0.7;
}

.hero-sparkle.s1 {
  top: 18px;
  right: 26%;
}

.hero-sparkle.s2 {
  bottom: 20px;
  right: 34%;
  color: var(--sc-purple);
  opacity: 0.5;
}

/* 缓慢闪烁：仅作氛围，不用夸张的动效 */
@media (prefers-reduced-motion: no-preference) {
  .hero-sparkle {
    animation: twinkle 4.5s ease-in-out infinite;
  }
  .hero-sparkle.s2 {
    animation-delay: 1.6s;
  }
}

@keyframes twinkle {
  0%,
  100% {
    opacity: 0.28;
    transform: scale(0.9);
  }
  50% {
    opacity: 0.85;
    transform: scale(1.06);
  }
}

/* ---- 统计 ---- */
.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(148px, 1fr));
  gap: 14px;
  margin-bottom: 20px;
}

/* ---- 内容卡片 ---- */
.block {
  border-radius: var(--sc-radius-xl);
  margin-bottom: 20px;
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.block-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--sc-text);
}

.block-title .sc-icon {
  color: var(--sc-purple);
}

.more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--sc-text-secondary);
  transition: color 0.18s ease;
}

.more:hover {
  color: var(--sc-pink-strong);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.muted {
  color: var(--sc-text-secondary);
  margin: 0;
}

.placeholder {
  border-style: dashed;
  background: rgba(255, 252, 254, 0.55);
}

.placeholder .muted {
  font-size: 13px;
  line-height: 1.7;
}

.soon-pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--sc-radius-pill);
  background: var(--sc-purple-soft);
  color: var(--sc-purple-strong);
  font-size: 12px;
  font-weight: 500;
}

.mb {
  margin-bottom: 12px;
}
</style>
