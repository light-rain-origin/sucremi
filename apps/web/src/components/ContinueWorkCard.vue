<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { getProjectRecovery } from '@/api/recovery'
import type { Project } from '@/types/project'
import type { ProjectRecovery } from '@/types/recovery'
import { formatDateTime } from '@/utils/datetime'

const props = defineProps<{
  projects: Project[]
  projectsLoading: boolean
  projectsError: string | null
}>()

const emit = defineEmits<{ retryProjects: [] }>()

const loading = ref(false)
const error = ref<string | null>(null)
const current = ref<ProjectRecovery | null>(null)
const nextAction = computed(() => current.value?.actions.find((action) => action.status === 'TODO') ?? null)
let requestSequence = 0

async function load() {
  const sequence = ++requestSequence
  error.value = null
  current.value = null

  if (props.projectsLoading) {
    loading.value = true
    return
  }
  if (props.projectsError) {
    loading.value = false
    error.value = props.projectsError
    return
  }
  if (props.projects.length === 0) {
    loading.value = false
    return
  }

  loading.value = true
  try {
    const recoveries = await Promise.all(
      props.projects.map(async (project) => {
        const response = await getProjectRecovery(project.id)
        if (response.code !== 0) throw new Error(response.message || '恢复信息读取失败')
        return response.data
      }),
    )
    if (sequence !== requestSequence) return

    const withCheckpoint = recoveries
      .filter((recovery) => recovery.latestCheckpoint)
      .sort(
        (a, b) =>
          new Date(b.latestCheckpoint!.createdAt).getTime() -
          new Date(a.latestCheckpoint!.createdAt).getTime(),
      )
    current.value =
      withCheckpoint[0] ??
      recoveries.sort(
        (a, b) => new Date(b.project.updatedAt).getTime() - new Date(a.project.updatedAt).getTime(),
      )[0] ??
      null
  } catch (reason) {
    if (sequence === requestSequence) error.value = (reason as Error).message || '恢复信息读取失败'
  } finally {
    if (sequence === requestSequence) loading.value = false
  }
}

watch(
  () => [props.projects.map((project) => project.id).join(','), props.projectsLoading, props.projectsError],
  load,
  { immediate: true },
)
</script>

<template>
  <section class="continue-card" aria-labelledby="continue-card-title" :aria-busy="loading">
    <header class="continue-header">
      <span class="continue-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24"><path d="M12 4.5a7.5 7.5 0 1 0 7.5 7.5"/><path d="M12 8v4.5l3 1.8M15.5 4.5H20v4.5"/></svg>
      </span>
      <div class="continue-heading">
        <h2 id="continue-card-title">继续工作</h2>
        <p>把上次停下的地方，变成今天的下一步。</p>
      </div>
      <span v-if="current?.latestCheckpoint" class="saved-at">
        最近保存 {{ formatDateTime(current.latestCheckpoint.createdAt) }}
      </span>
    </header>

    <div v-if="loading" class="continue-state" role="status">正在整理项目进度…</div>

    <div v-else-if="error" class="continue-state error-state" role="alert">
      <p>{{ error }}</p>
      <button v-if="projectsError" type="button" @click="emit('retryProjects')">重新加载项目</button>
      <button v-else type="button" @click="load">重试</button>
    </div>

    <div v-else-if="!current" class="continue-state empty-state">
      <p>先创建一个项目，记录下每次努力的起点。</p>
      <RouterLink to="/projects">前往项目列表</RouterLink>
    </div>

    <div v-else-if="!current.latestCheckpoint" class="continue-content onboarding-state">
      <div class="project-mark" aria-hidden="true">✦</div>
      <div class="continue-copy">
        <strong>{{ current.project.name }}</strong>
        <p>还没有保存检查点。写下当前进展，下次回来就能从这里继续。</p>
      </div>
      <RouterLink class="continue-link" :to="`/projects/${current.project.id}/recovery`">
        保存第一条检查点 <span aria-hidden="true">→</span>
      </RouterLink>
    </div>

    <div v-else class="continue-content">
      <div class="project-mark" aria-hidden="true">✦</div>
      <div class="continue-copy">
        <strong>{{ current.project.name }}</strong>
        <p class="recovery-summary">{{ current.recoverySummary }}</p>
        <p v-if="nextAction" class="next-action"><span>下一步</span>{{ nextAction.title }}</p>
      </div>
      <RouterLink class="continue-link" :to="`/projects/${current.project.id}/recovery`">
        继续这个项目 <span aria-hidden="true">→</span>
      </RouterLink>
    </div>
  </section>
</template>

<style scoped>
.continue-card {
  min-width: 0;
  padding: 16px 18px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-xl);
  background: var(--sc-card-surface);
  box-shadow: var(--sc-shadow-md);
}

.continue-header {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.continue-icon,
.project-mark {
  display: grid;
  flex: 0 0 42px;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 15px;
  background: color-mix(in srgb, var(--sc-brand-alt) 13%, var(--sc-card-surface));
  color: var(--sc-brand-alt-strong);
}

.continue-icon svg {
  width: 21px;
  height: 21px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.7;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.continue-heading {
  min-width: 0;
}

.continue-heading h2 {
  margin: 0;
  color: var(--sc-text);
  font-size: var(--sc-font-size-xl);
  font-weight: 650;
}

.continue-heading p,
.continue-copy p {
  margin: 3px 0 0;
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-md);
  line-height: 1.65;
}

.saved-at {
  margin-left: auto;
  color: var(--sc-text-placeholder);
  font-size: var(--sc-font-size-sm);
  white-space: nowrap;
}

.continue-content {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 10px;
  padding: 12px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-lg);
  background: var(--sc-panel);
}

.project-mark {
  flex-basis: 40px;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  color: var(--sc-brand-strong);
  background: color-mix(in srgb, var(--sc-brand) 14%, var(--sc-card-surface));
  font-size: 23px;
}

.continue-copy {
  min-width: 0;
  flex: 1;
}

.continue-copy strong {
  color: var(--sc-text);
  font-size: var(--sc-font-size-lg);
  font-weight: 650;
}

.continue-copy .recovery-summary {
  max-width: none;
  overflow-wrap: anywhere;
  white-space: normal;
  line-height: 1.45;
}

.continue-copy .next-action {
  display: flex;
  gap: 8px;
  align-items: baseline;
  color: var(--sc-text);
}

.next-action span {
  flex: 0 0 auto;
  color: var(--sc-brand-alt-strong);
  font-size: var(--sc-font-size-sm);
  font-weight: 600;
}

.continue-link {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: var(--sc-radius-pill);
  background: var(--sc-gradient-brand);
  box-shadow: var(--sc-shadow-brand);
  color: var(--sc-brand-on);
  font-size: var(--sc-font-size-md);
  font-weight: 600;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.continue-link:hover {
  box-shadow: var(--sc-shadow-brand-hover);
  transform: translateY(-1px);
}

.continue-state {
  display: flex;
  min-height: 92px;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
  padding: 16px;
  border: 1px dashed var(--sc-border-strong);
  border-radius: var(--sc-radius-lg);
  color: var(--sc-text-secondary);
  text-align: center;
}

.continue-state p {
  margin: 0;
}

.continue-state button,
.empty-state a {
  padding: 7px 13px;
  border: 1px solid var(--sc-border-strong);
  border-radius: var(--sc-radius-pill);
  background: var(--sc-panel-strong);
  color: var(--sc-brand-alt-strong);
  font: inherit;
  cursor: pointer;
}

.error-state {
  color: var(--sc-error-strong);
}

@media (max-width: 700px) {
  .continue-card {
    padding: 14px 16px;
  }

  .continue-content {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .continue-link {
    margin-left: 64px;
  }

  .saved-at {
    display: none;
  }
}
</style>
