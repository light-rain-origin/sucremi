<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteDecision, getDecision, listDecisionRevisions, supersedeDecision, updateDecision, type SupersedePayload } from '@/api/decision'
import type { DecisionPayload, DecisionRevision, ProjectDecision } from '@/types/decision'
import DecisionStatusBadge from '@/components/decision/DecisionStatusBadge.vue'
import DecisionEditor from '@/components/decision/DecisionEditor.vue'
import DecisionRevisionList from '@/components/decision/DecisionRevisionList.vue'
import DecisionSupersedeDialog from '@/components/decision/DecisionSupersedeDialog.vue'
import { formatDateTime } from '@/utils/datetime'

const route = useRoute(); const router = useRouter()
const projectId = computed(() => Number(route.params.id)); const decisionId = computed(() => Number(route.params.decisionId))
const decision = ref<ProjectDecision | null>(null); const revisions = ref<DecisionRevision[]>([])
const loading = ref(false); const error = ref(''); const revisionsError = ref(''); const revisionsLoading = ref(false)
const editorVisible = ref(false); const supersedeVisible = ref(false); const saving = ref(false)

async function load() {
  loading.value = true; error.value = ''
  try {
    const response = await getDecision(projectId.value, decisionId.value)
    if (response.code !== 0) throw new Error(response.message)
    decision.value = response.data
    await loadRevisions()
  } catch (reason) { error.value = (reason as Error).message || '决策读取失败' }
  finally { loading.value = false }
}
async function loadRevisions() {
  revisionsLoading.value = true; revisionsError.value = ''
  try { const response = await listDecisionRevisions(projectId.value, decisionId.value); if (response.code !== 0) throw new Error(response.message); revisions.value = response.data.list }
  catch (reason) { revisionsError.value = (reason as Error).message || '历史版本读取失败' }
  finally { revisionsLoading.value = false }
}
watch(() => [projectId.value, decisionId.value], load, { immediate: true })

async function save(payload: DecisionPayload) {
  if (!decision.value) return
  saving.value = true
  try { const response = await updateDecision(projectId.value, decisionId.value, payload); if (response.code !== 0) throw new Error(response.message); editorVisible.value = false; ElMessage.success('决策已更新'); await load() }
  catch (reason) { ElMessage.error((reason as Error).message || '保存失败') }
  finally { saving.value = false }
}
async function supersede(payload: SupersedePayload) {
  saving.value = true
  try { const response = await supersedeDecision(projectId.value, decisionId.value, payload); if (response.code !== 0) throw new Error(response.message); supersedeVisible.value = false; ElMessage.success('新决策已创建，旧决策已替代'); await router.push(`/projects/${projectId.value}/decisions/${response.data.id}`) }
  catch (reason) { ElMessage.error((reason as Error).message || '替代失败') }
  finally { saving.value = false }
}
async function remove() {
  if (!decision.value) return
  try { await ElMessageBox.confirm('仅提议中的决策可以软删除，历史版本仍会保留。确定删除这条决策吗？', '删除决策', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }) }
  catch { return }
  try { const response = await deleteDecision(projectId.value, decisionId.value); if (response.code !== 0) throw new Error(response.message); ElMessage.success('决策已删除'); await router.push(`/projects/${projectId.value}`) }
  catch (reason) { ElMessage.error((reason as Error).message || '删除失败') }
}
</script>

<template>
  <main class="decision-page" :aria-busy="loading">
    <RouterLink class="back" :to="`/projects/${projectId}`">← 返回项目</RouterLink>
    <div v-if="loading" class="state">正在加载决策…</div>
    <section v-else-if="error" class="state error" role="alert"><strong>决策暂时无法读取</strong><p>{{ error }}</p><el-button @click="load">重试</el-button></section>
    <template v-else-if="decision">
      <article class="surface detail-card">
        <header><div><div class="title-line"><h1>{{ decision.title }}</h1><DecisionStatusBadge :status="decision.status" /></div><p class="updated">更新于 {{ formatDateTime(decision.updatedAt) }} · 版本 {{ decision.version }}</p></div>
          <div class="toolbar"><el-button v-if="decision.status !== 'SUPERSEDED'" @click="editorVisible = true">编辑</el-button><el-button v-if="decision.status !== 'SUPERSEDED'" type="primary" plain @click="supersedeVisible = true">发起替代</el-button><el-button v-if="decision.status === 'PROPOSED'" type="danger" plain @click="remove">删除草稿</el-button></div>
        </header>
        <section><h2>背景与问题</h2><p>{{ decision.context }}</p></section>
        <section v-if="decision.options.length"><h2>备选方案</h2><ul><li v-for="(option, index) in decision.options" :key="index">{{ option }}</li></ul></section>
        <section v-if="decision.decision"><h2>最终选择</h2><p>{{ decision.decision }}</p></section>
        <section v-if="decision.rationale"><h2>选择理由</h2><p>{{ decision.rationale }}</p></section>
        <section v-if="decision.consequences"><h2>预期影响与取舍</h2><p>{{ decision.consequences }}</p></section>
        <section v-if="decision.revisitCondition"><h2>重新评估条件</h2><p>{{ decision.revisitCondition }}</p></section>
        <p v-if="decision.supersededById" class="superseded-link">已由 <RouterLink :to="`/projects/${projectId}/decisions/${decision.supersededById}`">新决策 #{{ decision.supersededById }}</RouterLink> 替代</p>
      </article>
      <DecisionRevisionList :revisions="revisions" :loading="revisionsLoading" :error="revisionsError" />
      <DecisionEditor v-model="editorVisible" :initial="decision" :busy="saving" @save="save" />
      <DecisionSupersedeDialog v-model="supersedeVisible" :decision="decision" :busy="saving" @supersede="supersede" />
    </template>
  </main>
</template>

<style scoped>
.decision-page{max-width:1000px;margin:0 auto;padding:4px 0 36px}.back{display:inline-flex;margin:0 0 16px;color:var(--sc-brand-alt-strong)}.surface{padding:24px;border:1px solid var(--sc-border-soft);border-radius:var(--sc-radius-xl);background:var(--sc-card-surface);box-shadow:var(--sc-shadow-sm)}.detail-card{margin-bottom:20px}.detail-card>header{display:flex;justify-content:space-between;align-items:flex-start;gap:16px;padding-bottom:18px;border-bottom:1px solid var(--sc-border-soft)}.title-line{display:flex;align-items:center;gap:12px;flex-wrap:wrap}.title-line h1{margin:0;color:var(--sc-text);font-size:26px}.updated{margin:8px 0 0;color:var(--sc-text-secondary);font-size:13px}.toolbar{display:flex;gap:8px;flex-wrap:wrap}.detail-card section{padding-top:18px}.detail-card h2{margin:0 0 8px;color:var(--sc-brand-alt-strong);font-size:15px}.detail-card p,.detail-card li{color:var(--sc-text);line-height:1.75;white-space:pre-wrap}.detail-card section p{margin:0}.detail-card ul{margin:0;padding-left:22px}.superseded-link{margin:18px 0 0;color:var(--sc-text-secondary)}.superseded-link a{color:var(--sc-brand-alt-strong)}.state{padding:48px 16px;text-align:center;color:var(--sc-text-secondary)}.error{color:var(--el-color-danger)}@media(max-width:700px){.detail-card{padding:17px}.detail-card>header{display:grid}.title-line h1{font-size:22px}}
</style>
