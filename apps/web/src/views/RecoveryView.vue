<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  createAction,
  createCheckpoint,
  deleteAction,
  deleteCheckpoint,
  getProjectRecovery,
  listCheckpoints,
  reorderActions,
  updateActionStatus,
} from '@/api/recovery'
import type { ProjectCheckpoint, ProjectRecovery } from '@/types/recovery'
import { formatDateTime } from '@/utils/datetime'

const route = useRoute()
const recovery = ref<ProjectRecovery | null>(null)
const checkpointHistory = ref<ProjectCheckpoint[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const mutationError = ref<string | null>(null)
const savingCheckpoint = ref(false)
const savingAction = ref(false)
const updatingAction = ref<number | null>(null)
const checkpointForm = reactive({ summary: '', currentFocus: '', blockers: '' })
const actionForm = reactive({ title: '', details: '' })
const projectId = computed(() => Number(route.params.id))

async function load() {
  if (!Number.isSafeInteger(projectId.value) || projectId.value <= 0) {
    error.value = '项目编号无效'
    recovery.value = null
    return
  }
  loading.value = true
  error.value = null
  mutationError.value = null
  try {
    const [response, historyResponse] = await Promise.all([
      getProjectRecovery(projectId.value),
      listCheckpoints(projectId.value),
    ])
    if (response.code !== 0) throw new Error(response.message || '恢复信息读取失败')
    if (historyResponse.code !== 0) throw new Error(historyResponse.message || '检查点历史读取失败')
    recovery.value = response.data
    checkpointHistory.value = historyResponse.data
  } catch (reason) {
    error.value = (reason as Error).message || '恢复信息读取失败'
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, load, { immediate: true })

const olderCheckpoints = computed(() =>
  checkpointHistory.value.filter((checkpoint) => checkpoint.id !== recovery.value?.latestCheckpoint?.id),
)

async function saveCheckpoint() {
  if (!checkpointForm.summary.trim()) return
  savingCheckpoint.value = true
  mutationError.value = null
  try {
    const response = await createCheckpoint(projectId.value, {
      summary: checkpointForm.summary.trim(),
      ...(checkpointForm.currentFocus.trim() && { currentFocus: checkpointForm.currentFocus.trim() }),
      ...(checkpointForm.blockers.trim() && { blockers: checkpointForm.blockers.trim() }),
    })
    if (response.code !== 0) throw new Error(response.message || '检查点保存失败')
    checkpointForm.summary = ''
    checkpointForm.currentFocus = ''
    checkpointForm.blockers = ''
    await load()
  } catch (reason) {
    mutationError.value = (reason as Error).message || '检查点保存失败'
  } finally {
    savingCheckpoint.value = false
  }
}

async function addAction() {
  if (!actionForm.title.trim()) return
  savingAction.value = true
  mutationError.value = null
  try {
    const response = await createAction(projectId.value, {
      title: actionForm.title.trim(),
      ...(actionForm.details.trim() && { details: actionForm.details.trim() }),
    })
    if (response.code !== 0) throw new Error(response.message || '行动保存失败')
    actionForm.title = ''
    actionForm.details = ''
    if (recovery.value) recovery.value.actions.push(response.data)
  } catch (reason) {
    mutationError.value = (reason as Error).message || '行动保存失败'
  } finally {
    savingAction.value = false
  }
}

async function confirmDelete(message: string) {
  try {
    await ElMessageBox.confirm(message, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    return true
  } catch {
    return false
  }
}

async function removeAction(actionId: number, title: string) {
  if (!(await confirmDelete(`确定删除下一步行动“${title}”吗？`))) return
  mutationError.value = null
  try {
    const response = await deleteAction(projectId.value, actionId)
    if (response.code !== 0) throw new Error(response.message || '行动删除失败')
    if (recovery.value) {
      recovery.value.actions = recovery.value.actions.filter((action) => action.id !== actionId)
      if (recovery.value.latestCheckpoint) recovery.value.recoverySummary = buildLocalSummary(recovery.value)
    }
  } catch (reason) {
    mutationError.value = (reason as Error).message || '行动删除失败'
  }
}

async function removeCheckpoint(checkpointId: number) {
  if (!(await confirmDelete('确定删除这条检查点吗？删除后无法恢复。'))) return
  mutationError.value = null
  try {
    const response = await deleteCheckpoint(projectId.value, checkpointId)
    if (response.code !== 0) throw new Error(response.message || '检查点删除失败')
    await load()
  } catch (reason) {
    mutationError.value = (reason as Error).message || '检查点删除失败'
  }
}

async function setActionStatus(actionId: number, status: 'TODO' | 'DONE') {
  updatingAction.value = actionId
  mutationError.value = null
  try {
    const response = await updateActionStatus(projectId.value, actionId, status)
    if (response.code !== 0) throw new Error(response.message || '行动状态更新失败')
    const index = recovery.value?.actions.findIndex((action) => action.id === actionId) ?? -1
    if (recovery.value && index >= 0) recovery.value.actions[index] = response.data
    if (recovery.value?.latestCheckpoint) {
      recovery.value.recoverySummary = buildLocalSummary(recovery.value)
    }
  } catch (reason) {
    mutationError.value = (reason as Error).message || '行动状态更新失败'
  } finally {
    updatingAction.value = null
  }
}

async function moveAction(index: number, offset: -1 | 1) {
  const actions = recovery.value?.actions
  if (!actions) return
  const targetIndex = index + offset
  if (targetIndex < 0 || targetIndex >= actions.length) return
  const actionIds = actions.map((action) => action.id)
  ;[actionIds[index], actionIds[targetIndex]] = [actionIds[targetIndex]!, actionIds[index]!]
  mutationError.value = null
  try {
    const response = await reorderActions(projectId.value, actionIds)
    if (response.code !== 0) throw new Error(response.message || '行动顺序保存失败')
    recovery.value!.actions = response.data
    if (recovery.value!.latestCheckpoint) recovery.value!.recoverySummary = buildLocalSummary(recovery.value!)
  } catch (reason) {
    mutationError.value = (reason as Error).message || '行动顺序保存失败'
  }
}

function buildLocalSummary(data: ProjectRecovery) {
  const checkpoint = data.latestCheckpoint
  if (!checkpoint) return null
  const parts = [checkpoint.summary]
  if (checkpoint.currentFocus) parts.push(`当前关注：${checkpoint.currentFocus}`)
  if (checkpoint.blockers) parts.push(`阻塞事项：${checkpoint.blockers}`)
  const next = data.actions.find((action) => action.status === 'TODO')
  if (next) parts.push(`下一步：${next.title}`)
  return parts.join(' ')
}
</script>

<template>
  <main class="recovery-page" :aria-busy="loading">
    <RouterLink v-if="recovery" class="back-link" :to="`/projects/${projectId}`">
      ← 返回 {{ recovery.project.name }}
    </RouterLink>
    <RouterLink v-else class="back-link" to="/projects">← 返回项目列表</RouterLink>

    <div v-if="loading" class="page-state" role="status">正在找回项目进度…</div>

    <section v-else-if="error" class="page-state error-state" role="alert">
      <strong>恢复信息暂时无法读取</strong>
      <p>{{ error }}</p>
      <button type="button" class="secondary-button" @click="load">重试</button>
    </section>

    <template v-else-if="recovery">
      <header class="page-heading">
        <div>
          <p class="eyebrow">PROJECT RECOVERY</p>
          <h1>{{ recovery.project.name }}</h1>
          <p class="heading-description">把上次的状态、阻塞和下一步放在一起，轻松接回工作。</p>
        </div>
        <span class="project-progress">当前进度 {{ recovery.project.progress }}%</span>
      </header>

      <section v-if="recovery.latestCheckpoint" class="surface summary-panel" aria-labelledby="recovery-summary-title">
        <div class="section-heading">
          <span class="section-icon checkpoint-icon" aria-hidden="true">✦</span>
          <div>
            <h2 id="recovery-summary-title">上次停在这里</h2>
            <p>保存于 {{ formatDateTime(recovery.latestCheckpoint.createdAt) }}</p>
          </div>
          <button
            type="button"
            class="delete-button checkpoint-delete"
            aria-label="删除最新检查点"
            @click="removeCheckpoint(recovery.latestCheckpoint.id)"
          >删除检查点</button>
        </div>
        <p class="summary-text">{{ recovery.recoverySummary }}</p>
        <dl class="checkpoint-details">
          <div v-if="recovery.latestCheckpoint.currentFocus">
            <dt>当前关注</dt><dd>{{ recovery.latestCheckpoint.currentFocus }}</dd>
          </div>
          <div v-if="recovery.latestCheckpoint.blockers">
            <dt>阻塞事项</dt><dd>{{ recovery.latestCheckpoint.blockers }}</dd>
          </div>
        </dl>
      </section>

      <section v-if="olderCheckpoints.length" class="surface checkpoint-history" aria-labelledby="checkpoint-history-title">
        <div class="section-heading history-heading">
          <div>
            <h2 id="checkpoint-history-title">更早的检查点</h2>
            <p>保留每次记录，也可以单独删除不再需要的记录。</p>
          </div>
        </div>
        <article v-for="checkpoint in olderCheckpoints" :key="checkpoint.id" class="history-row">
          <div class="history-copy">
            <time>{{ formatDateTime(checkpoint.createdAt) }}</time>
            <p>{{ checkpoint.summary }}</p>
          </div>
          <button
            type="button"
            class="delete-button"
            :aria-label="`删除检查点：${formatDateTime(checkpoint.createdAt)}`"
            @click="removeCheckpoint(checkpoint.id)"
          >删除</button>
        </article>
      </section>

      <section v-if="!recovery.latestCheckpoint" class="surface no-checkpoint" aria-labelledby="empty-checkpoint-title">
        <span class="empty-sparkle" aria-hidden="true">✧</span>
        <div>
          <h2 id="empty-checkpoint-title">还没有保存过检查点</h2>
          <p>简单记下目前做到哪里、正关注什么，下次打开项目就能从这里继续。</p>
        </div>
      </section>

      <section class="surface actions-panel" aria-labelledby="actions-title">
        <div class="section-heading">
          <span class="section-icon action-icon" aria-hidden="true">✓</span>
          <div>
            <h2 id="actions-title">下一步行动</h2>
            <p>完成一项后，下一次打开时会继续保留状态。</p>
          </div>
        </div>

        <p v-if="mutationError" class="mutation-error" role="alert">{{ mutationError }}</p>

        <div v-if="recovery.actions.length" class="action-list">
          <article v-for="(action, index) in recovery.actions" :key="action.id" class="action-row">
            <button
              type="button"
              class="status-toggle"
              :class="{ completed: action.status === 'DONE' }"
              :aria-label="action.status === 'DONE' ? `重新打开：${action.title}` : `完成：${action.title}`"
              :disabled="updatingAction === action.id"
              @click="setActionStatus(action.id, action.status === 'DONE' ? 'TODO' : 'DONE')"
            >
              {{ action.status === 'DONE' ? '✓' : '' }}
            </button>
            <div class="action-copy" :class="{ completed: action.status === 'DONE' }">
              <strong>{{ action.title }}</strong>
              <p v-if="action.details">{{ action.details }}</p>
            </div>
            <span class="action-state">{{ action.status === 'DONE' ? '已完成' : '待完成' }}</span>
            <div class="order-controls" aria-label="调整行动顺序">
              <button type="button" :aria-label="`上移：${action.title}`" :disabled="index === 0" @click="moveAction(index, -1)">↑</button>
              <button type="button" :aria-label="`下移：${action.title}`" :disabled="index === recovery.actions.length - 1" @click="moveAction(index, 1)">↓</button>
            </div>
            <button type="button" class="delete-button" :aria-label="`删除行动：${action.title}`" @click="removeAction(action.id, action.title)">删除</button>
          </article>
        </div>
        <p v-else class="no-actions">还没有下一步行动，先记下一件最容易开始的小事吧。</p>

        <form class="action-form" @submit.prevent="addAction">
          <label>
            <span>添加下一步</span>
            <input v-model="actionForm.title" maxlength="200" placeholder="例如：补充登录接口的错误处理" required />
          </label>
          <label class="details-input">
            <span>补充说明 <i>可选</i></span>
            <input v-model="actionForm.details" maxlength="1000" placeholder="给未来的自己一点提示" />
          </label>
          <button class="primary-button" type="submit" :disabled="savingAction || !actionForm.title.trim()">
            {{ savingAction ? '保存中…' : '添加行动' }}
          </button>
        </form>
      </section>

      <section class="surface checkpoint-form-panel" aria-labelledby="checkpoint-form-title">
        <div class="section-heading">
          <span class="section-icon note-icon" aria-hidden="true">↗</span>
          <div>
            <h2 id="checkpoint-form-title">保存当前检查点</h2>
            <p>留下一段真实记录，帮助未来的自己快速找回上下文。</p>
          </div>
        </div>

        <form class="checkpoint-form" @submit.prevent="saveCheckpoint">
          <label>
            <span>目前进展</span>
            <textarea v-model="checkpointForm.summary" maxlength="10000" rows="3" placeholder="已经完成了什么？现在的代码或设计进展如何？" required />
          </label>
          <div class="form-columns">
            <label>
              <span>当前关注 <i>可选</i></span>
              <input v-model="checkpointForm.currentFocus" maxlength="255" placeholder="现在正在处理什么？" />
            </label>
            <label>
              <span>阻塞事项 <i>可选</i></span>
              <input v-model="checkpointForm.blockers" maxlength="10000" placeholder="有没有需要解决的问题？" />
            </label>
          </div>
          <button class="primary-button" type="submit" :disabled="savingCheckpoint || !checkpointForm.summary.trim()">
            {{ savingCheckpoint ? '保存中…' : '保存检查点' }}
          </button>
        </form>
      </section>
    </template>
  </main>
</template>

<style scoped>
.recovery-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

.back-link {
  align-self: flex-start;
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-md);
}

.back-link:hover {
  color: var(--sc-brand-alt-strong);
}

.page-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 7px 2px 4px;
}

.eyebrow {
  margin: 0 0 6px;
  color: var(--sc-brand-alt-strong);
  font-size: var(--sc-font-size-xs);
  font-weight: 700;
  letter-spacing: 0.14em;
}

.page-heading h1 {
  margin: 0;
  color: var(--sc-text);
  font-size: var(--sc-font-size-5xl);
  font-weight: 650;
}

.heading-description,
.section-heading p {
  margin: 5px 0 0;
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-md);
}

.project-progress {
  flex: 0 0 auto;
  padding: 8px 13px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-pill);
  background: var(--sc-panel-strong);
  color: var(--sc-brand-alt-strong);
  font-size: var(--sc-font-size-md);
  font-weight: 600;
}

.surface {
  padding: 22px 24px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-xl);
  background: var(--sc-card-surface);
  box-shadow: var(--sc-shadow-md);
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-heading > div:last-child {
  flex: 1;
}

.checkpoint-delete {
  margin-left: auto;
}

.checkpoint-history {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-heading {
  margin-bottom: 5px;
}

.history-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-top: 1px solid var(--sc-border-soft);
}

.history-copy {
  flex: 1;
  min-width: 0;
}

.history-copy time {
  color: var(--sc-text-placeholder);
  font-size: var(--sc-font-size-xs);
}

.history-copy p {
  margin: 4px 0 0;
  color: var(--sc-text);
  font-size: var(--sc-font-size-md);
  line-height: 1.65;
  white-space: pre-wrap;
}

.section-heading h2,
.no-checkpoint h2 {
  margin: 0;
  color: var(--sc-text);
  font-size: var(--sc-font-size-xl);
  font-weight: 650;
}

.section-heading p {
  font-size: var(--sc-font-size-sm);
}

.section-icon {
  display: grid;
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  place-items: center;
  border-radius: 14px;
  font-size: 20px;
  font-weight: 700;
}

.checkpoint-icon,
.note-icon {
  background: color-mix(in srgb, var(--sc-brand) 15%, var(--sc-card-surface));
  color: var(--sc-brand-strong);
}

.action-icon {
  background: color-mix(in srgb, var(--sc-status-done) 18%, var(--sc-card-surface));
  color: var(--sc-status-done);
}

.summary-text {
  margin: 18px 0 0;
  color: var(--sc-text);
  font-size: var(--sc-font-size-lg);
  line-height: 1.85;
  white-space: pre-wrap;
}

.checkpoint-details {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 15px 0 0;
}

.checkpoint-details div {
  min-width: 0;
  padding: 12px 14px;
  border-radius: var(--sc-radius-md);
  background: var(--sc-panel);
}

.checkpoint-details dt {
  margin-bottom: 4px;
  color: var(--sc-text-placeholder);
  font-size: var(--sc-font-size-xs);
}

.checkpoint-details dd {
  margin: 0;
  color: var(--sc-text);
  font-size: var(--sc-font-size-md);
  white-space: pre-wrap;
}

.no-checkpoint {
  display: flex;
  align-items: center;
  gap: 14px;
  border-color: color-mix(in srgb, var(--sc-brand) 28%, var(--sc-border-soft));
  background: color-mix(in srgb, var(--sc-brand) 5%, var(--sc-card-surface));
}

.empty-sparkle {
  color: var(--sc-brand);
  font-size: 34px;
}

.no-checkpoint p,
.no-actions {
  margin: 5px 0 0;
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-md);
  line-height: 1.7;
}

.action-list {
  margin-top: 18px;
  border-top: 1px solid var(--sc-border-soft);
}

.action-row {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  padding: 13px 2px;
  border-bottom: 1px solid var(--sc-border-soft);
}

.status-toggle {
  display: grid;
  width: 22px;
  height: 22px;
  flex: 0 0 22px;
  place-items: center;
  padding: 0;
  border: 1.5px solid var(--sc-border-strong);
  border-radius: 50%;
  background: transparent;
  color: white;
  font-size: 14px;
  cursor: pointer;
}

.status-toggle.completed {
  border-color: var(--sc-status-done);
  background: var(--sc-status-done);
}

.action-copy {
  min-width: 0;
  flex: 1;
}

.action-copy strong {
  color: var(--sc-text);
  font-size: var(--sc-font-size-md);
  font-weight: 600;
}

.action-copy p {
  margin: 3px 0 0;
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-sm);
}

.action-copy.completed strong {
  color: var(--sc-text-placeholder);
  text-decoration: line-through;
}

.action-state {
  flex: 0 0 auto;
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-xs);
}

.order-controls {
  display: flex;
  gap: 4px;
}

.order-controls button {
  width: 28px;
  height: 28px;
  border: 1px solid var(--sc-border-soft);
  border-radius: 9px;
  background: var(--sc-panel-strong);
  color: var(--sc-text-secondary);
  cursor: pointer;
}

.order-controls button:disabled {
  opacity: 0.38;
  cursor: default;
}

.delete-button {
  flex: 0 0 auto;
  padding: 6px 10px;
  border: 1px solid color-mix(in srgb, var(--sc-error-strong) 28%, var(--sc-border-soft));
  border-radius: var(--sc-radius-pill);
  background: color-mix(in srgb, var(--sc-error-strong) 5%, var(--sc-card-surface));
  color: var(--sc-error-strong);
  font: inherit;
  font-size: var(--sc-font-size-xs);
  cursor: pointer;
}

.delete-button:hover {
  background: color-mix(in srgb, var(--sc-error-strong) 12%, var(--sc-card-surface));
}

.action-form,
.checkpoint-form {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-top: 17px;
}

.action-form label,
.checkpoint-form label {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 6px;
}

.action-form label > span,
.checkpoint-form label > span {
  color: var(--sc-text-secondary);
  font-size: var(--sc-font-size-sm);
  font-weight: 600;
}

.action-form i,
.checkpoint-form i {
  color: var(--sc-text-placeholder);
  font-style: normal;
  font-weight: 400;
}

.action-form input,
.checkpoint-form input,
.checkpoint-form textarea {
  width: 100%;
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid var(--sc-border);
  border-radius: var(--sc-radius-md);
  outline: none;
  background: var(--sc-bg-elevated);
  color: var(--sc-text);
  font: inherit;
  font-size: var(--sc-font-size-md);
}

.checkpoint-form textarea {
  min-height: 88px;
  resize: vertical;
}

.action-form input:focus,
.checkpoint-form input:focus,
.checkpoint-form textarea:focus {
  border-color: var(--sc-brand-alt);
  box-shadow: var(--sc-focus-ring);
}

.action-form input::placeholder,
.checkpoint-form input::placeholder,
.checkpoint-form textarea::placeholder {
  color: var(--sc-text-placeholder);
}

.primary-button,
.secondary-button {
  flex: 0 0 auto;
  min-height: 40px;
  padding: 9px 16px;
  border: 0;
  border-radius: var(--sc-radius-pill);
  background: var(--sc-gradient-brand);
  box-shadow: var(--sc-shadow-brand);
  color: var(--sc-brand-on);
  font: inherit;
  font-size: var(--sc-font-size-md);
  font-weight: 600;
  cursor: pointer;
}

.primary-button:disabled {
  opacity: 0.56;
  cursor: default;
}

.secondary-button {
  border: 1px solid var(--sc-border-strong);
  background: var(--sc-panel-strong);
  box-shadow: none;
  color: var(--sc-text);
}

.no-actions {
  margin-top: 18px;
  padding: 15px;
  border-radius: var(--sc-radius-md);
  background: var(--sc-panel);
  text-align: center;
}

.checkpoint-form {
  align-items: stretch;
  flex-direction: column;
}

.form-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.checkpoint-form .primary-button {
  align-self: flex-end;
}

.mutation-error {
  margin: 14px 0 0;
  color: var(--sc-error-strong);
  font-size: var(--sc-font-size-md);
}

.page-state {
  display: flex;
  min-height: 250px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  border: 1px solid var(--sc-border-soft);
  border-radius: var(--sc-radius-xl);
  background: var(--sc-card-surface);
  color: var(--sc-text-secondary);
  text-align: center;
}

.page-state strong {
  color: var(--sc-text);
  font-size: var(--sc-font-size-xl);
}

.page-state p {
  margin: 0 0 5px;
}

@media (max-width: 700px) {
  .surface {
    padding: 18px;
  }

  .page-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .checkpoint-details,
  .form-columns {
    grid-template-columns: minmax(0, 1fr);
  }

  .action-form {
    align-items: stretch;
    flex-direction: column;
  }

  .action-form .primary-button {
    align-self: flex-end;
  }

  .action-row {
    flex-wrap: wrap;
  }

  .action-state {
    margin-left: 34px;
  }
}
</style>
