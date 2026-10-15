<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { archiveProject, getProject, unarchiveProject, updateProject } from '@/api/project'
import type { Project, ProjectForm } from '@/types/project'
import StatusTag from '@/components/StatusTag.vue'
import ProgressBar from '@/components/ProgressBar.vue'
import ProjectFormDialog from '@/components/ProjectFormDialog.vue'
import { formatDateTime } from '@/utils/datetime'

const route = useRoute()
const id = route.params.id as string

const project = ref<Project | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const editVisible = ref(false)

const editInitial = computed<Partial<ProjectForm>>(() =>
  project.value
    ? {
        name: project.value.name,
        summary: project.value.summary ?? '',
        goal: project.value.goal ?? '',
        status: project.value.status,
        progress: project.value.progress,
      }
    : {},
)

async function load() {
  loading.value = true
  error.value = null
  try {
    const res = await getProject(id)
    if (res.code === 0) project.value = res.data
    else error.value = res.message
  } catch (e) {
    error.value = (e as Error).message || '加载失败'
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function onEdit(form: ProjectForm) {
  try {
    const res = await updateProject(id, form)
    if (res.code !== 0) throw new Error(res.message)
    project.value = res.data
    editVisible.value = false
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error((e as Error).message || '保存失败')
  }
}

async function toggleArchive() {
  if (!project.value) return
  const archived = project.value.archived
  try {
    await ElMessageBox.confirm(
      archived ? '确定取消归档该项目吗？' : '确定归档该项目吗？归档后默认列表将不再显示。',
      archived ? '取消归档' : '归档项目',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    const res = archived ? await unarchiveProject(id) : await archiveProject(id)
    if (res.code !== 0) throw new Error(res.message)
    ElMessage.success(archived ? '已取消归档' : '已归档')
    load()
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}
</script>

<template>
  <section v-loading="loading" class="detail">
    <RouterLink to="/projects" class="back">← 返回项目列表</RouterLink>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="mb" />

    <template v-if="project">
      <div class="head">
        <div class="head-main">
          <h2 class="name">{{ project.name }}</h2>
          <StatusTag :status="project.status" />
          <el-tag v-if="project.archived" type="info" effect="plain" round>已归档</el-tag>
        </div>
        <div class="head-ops">
          <RouterLink class="recovery-entry" :to="`/projects/${project.id}/recovery`">恢复上下文</RouterLink>
          <el-button @click="editVisible = true">编辑</el-button>
          <el-button :type="project.archived ? 'primary' : 'default'" @click="toggleArchive">
            {{ project.archived ? '取消归档' : '归档' }}
          </el-button>
        </div>
      </div>

      <el-card shadow="never" class="overview">
        <p class="field"><span class="label">简介</span>{{ project.summary || '暂无简介' }}</p>
        <p class="field"><span class="label">目标</span>{{ project.goal || '暂无目标' }}</p>
        <div class="field">
          <span class="label">进度</span>
          <div class="progress-wrap"><ProgressBar :value="project.progress" /></div>
        </div>
        <p class="meta">
          创建于 {{ formatDateTime(project.createdAt) }} · 更新于 {{ formatDateTime(project.updatedAt) }}
        </p>
      </el-card>

      <div class="future-grid">
        <el-card shadow="never" class="future-card recovery-card">
          <div class="future-title">检查点与下一步</div>
          <div class="recovery-hint">管理项目进展记录、检查点历史和后续行动</div>
          <RouterLink class="feature-link" :to="`/projects/${project.id}/recovery`">
            打开恢复上下文 <span aria-hidden="true">→</span>
          </RouterLink>
        </el-card>
        <el-card v-for="s in ['进度 / 活动时间线', '决策记录 & 知识资料']"
                 :key="s" shadow="never" class="future-card">
          <div class="future-title">{{ s }}</div>
          <div class="future-hint">即将推出</div>
        </el-card>
      </div>
    </template>

    <ProjectFormDialog v-model="editVisible" mode="edit" :initial="editInitial" @submit="onEdit" />
  </section>
</template>

<style scoped>
.back {
  display: inline-block;
  margin-bottom: 16px;
  color: var(--sc-text-secondary);
}
.back:hover {
  color: var(--sc-brand);
}
.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.head-main {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.name {
  margin: 0;
  font-size: var(--sc-font-size-4xl);
}
.head-ops {
  display: flex;
  align-items: center;
  gap: 8px;
}
.recovery-entry {
  padding: 8px 13px;
  border: 1px solid var(--sc-border-strong);
  border-radius: var(--sc-radius-pill);
  color: var(--sc-brand-alt-strong);
  font-size: var(--sc-font-size-md);
  transition: background 0.18s ease, border-color 0.18s ease;
}
.recovery-entry:hover {
  border-color: var(--sc-brand-alt);
  background: color-mix(in srgb, var(--sc-brand-alt) 8%, var(--sc-card-surface));
}
.overview {
  border-radius: var(--sc-radius-lg);
  margin-bottom: 20px;
}
.field {
  display: flex;
  gap: 12px;
  margin: 0 0 12px;
  align-items: baseline;
}
.label {
  flex-shrink: 0;
  width: 48px;
  color: var(--sc-text-secondary);
}
.progress-wrap {
  flex: 1;
}
.meta {
  margin: 4px 0 0;
  font-size: var(--sc-font-size-sm);
  color: var(--sc-text-placeholder);
}
.future-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}
.future-card {
  border-radius: var(--sc-radius-lg);
  border-style: dashed;
}
.recovery-card {
  border-style: solid;
  border-color: color-mix(in srgb, var(--sc-brand-alt) 38%, var(--sc-border-soft));
  background: color-mix(in srgb, var(--sc-brand-alt) 5%, var(--sc-card-surface));
}
.future-title {
  font-weight: 600;
  margin-bottom: 6px;
}
.recovery-hint {
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-md);
  line-height: 1.6;
}
.feature-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-top: 12px;
  color: var(--sc-brand-alt-strong);
  font-size: var(--sc-font-size-md);
  font-weight: 600;
}
.feature-link:hover {
  color: var(--sc-brand-strong);
}
.future-hint {
  color: var(--sc-text-placeholder);
  font-size: var(--sc-font-size-md);
}
.mb {
  margin-bottom: 12px;
}
</style>
