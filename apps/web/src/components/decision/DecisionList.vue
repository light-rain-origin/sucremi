<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createDecision, listDecisions, updateDecision } from '@/api/decision'
import type { DecisionPayload, DecisionStatus, ProjectDecision } from '@/types/decision'
import type { PageResult } from '@/types/common'
import DecisionCard from './DecisionCard.vue'
import DecisionEditor from './DecisionEditor.vue'

const props = defineProps<{ projectId: number }>()
const rows = ref<ProjectDecision[]>([])
const total = ref(0)
const page = ref(1)
const size = 6
const status = ref<DecisionStatus | ''>('')
const sortBy = ref<'updatedAt' | 'createdAt'>('updatedAt')
const loading = ref(false)
const error = ref('')
const editorVisible = ref(false)
const editing = ref<ProjectDecision | null>(null)
const saving = ref(false)

async function load() {
  loading.value = true; error.value = ''
  try {
    const response = await listDecisions(props.projectId, { page: page.value, size, status: status.value || undefined, sortBy: sortBy.value, direction: 'desc' })
    if (response.code !== 0) throw new Error(response.message)
    const data: PageResult<ProjectDecision> = response.data
    rows.value = data.list; total.value = data.total
  } catch (reason) { error.value = (reason as Error).message || '决策记录加载失败' }
  finally { loading.value = false }
}
onMounted(load)
watch(() => [props.projectId, status.value, sortBy.value], () => { page.value = 1; load() })

function openCreate() { editing.value = null; editorVisible.value = true }
function openEdit(item: ProjectDecision) { editing.value = item; editorVisible.value = true }
async function save(payload: DecisionPayload) {
  saving.value = true
  try {
    const response = editing.value
      ? await updateDecision(props.projectId, editing.value.id, payload)
      : await createDecision(props.projectId, payload)
    if (response.code !== 0) throw new Error(response.message)
    editorVisible.value = false
    ElMessage.success(editing.value ? '决策已更新' : '决策已记录')
    await load()
  } catch (reason) { ElMessage.error((reason as Error).message || '决策保存失败') }
  finally { saving.value = false }
}
</script>

<template>
  <section class="decision-list" aria-labelledby="decision-list-title" :aria-busy="loading">
    <header><div><h2 id="decision-list-title">决策记录</h2><p>留下当时的判断依据，让未来的自己看懂选择。</p></div><el-button type="primary" @click="openCreate">记录决策</el-button></header>
    <div class="filters"><label for="decision-status">状态</label><select id="decision-status" v-model="status"><option value="">全部</option><option value="ACCEPTED">已采纳</option><option value="PROPOSED">提议中</option><option value="SUPERSEDED">已替代</option></select><label for="decision-sort">排序</label><select id="decision-sort" v-model="sortBy"><option value="updatedAt">最近更新</option><option value="createdAt">创建时间</option></select></div>
    <p v-if="loading" class="state">正在加载决策记录…</p>
    <div v-else-if="error" class="state error" role="alert">{{ error }} <button @click="load">重试</button></div>
    <div v-else-if="!rows.length" class="state empty"><strong>还没有决策记录</strong><span>重要技术选择可以从这里开始沉淀。</span><button @click="openCreate">记录第一条决策</button></div>
    <div v-else class="cards"><DecisionCard v-for="item in rows" :key="item.id" :decision="item" @edit="openEdit" /></div>
    <div v-if="total > size" class="pagination"><el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev, pager, next" @current-change="load" /></div>
    <DecisionEditor v-model="editorVisible" :initial="editing" :busy="saving" @save="save" />
  </section>
</template>

<style scoped>
.decision-list{min-width:0}header{display:flex;justify-content:space-between;align-items:center;gap:16px;margin-bottom:14px}h2{margin:0;color:var(--sc-text);font-size:var(--sc-font-size-xl);font-weight:650}header p{margin:4px 0 0;color:var(--sc-text-secondary);font-size:var(--sc-font-size-sm)}.filters{display:flex;align-items:center;gap:8px;margin-bottom:12px;color:var(--sc-text-secondary);font-size:13px}.filters select{border:1px solid var(--sc-border-soft);border-radius:9px;background:var(--sc-card-surface);padding:7px 10px;color:var(--sc-text)}.cards{display:grid;gap:10px}.state{padding:28px 16px;text-align:center;color:var(--sc-text-secondary)}.error{color:var(--el-color-danger)}.empty{display:grid;gap:8px}.empty strong{color:var(--sc-text)}.empty button,.error button{justify-self:center;border:0;background:none;color:var(--sc-brand-alt-strong);cursor:pointer;font:inherit}.pagination{display:flex;justify-content:center;margin-top:16px}@media(max-width:600px){header{align-items:flex-start}.cards{grid-template-columns:1fr}}
</style>
