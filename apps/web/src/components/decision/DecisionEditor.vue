<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import type { DecisionPayload, ProjectDecision } from '@/types/decision'

const props = defineProps<{ modelValue: boolean; initial?: ProjectDecision | null; busy?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean]; save: [payload: DecisionPayload] }>()
const form = reactive({ title: '', context: '', options: '', decision: '', rationale: '', consequences: '', revisitCondition: '', status: 'PROPOSED' as DecisionPayload['status'], changeSummary: '' })
const accepted = computed(() => form.status === 'ACCEPTED')

watch(() => [props.modelValue, props.initial] as const, () => {
  if (!props.modelValue) return
  const item = props.initial
  Object.assign(form, {
    title: item?.title ?? '', context: item?.context ?? '', options: item?.options.join('\n') ?? '',
    decision: item?.decision ?? '', rationale: item?.rationale ?? '', consequences: item?.consequences ?? '',
    revisitCondition: item?.revisitCondition ?? '', status: item?.status === 'ACCEPTED' ? 'ACCEPTED' : 'PROPOSED', changeSummary: '',
  })
}, { immediate: true })

function submit() {
  if (!form.title.trim() || !form.context.trim() || (accepted.value && (!form.decision.trim() || !form.rationale.trim()))) return
  emit('save', {
    title: form.title.trim(), context: form.context.trim(), options: form.options.split('\n').map((value) => value.trim()).filter(Boolean),
    decision: form.decision.trim() || undefined, rationale: form.rationale.trim() || undefined,
    consequences: form.consequences.trim() || undefined, revisitCondition: form.revisitCondition.trim() || undefined,
    status: form.status, changeSummary: form.changeSummary.trim() || undefined,
    ...(props.initial && { expectedVersion: props.initial.version }),
  })
}
</script>

<template>
  <el-dialog :model-value="modelValue" :title="initial ? '编辑决策' : '记录决策'" width="min(680px, 94vw)" @close="emit('update:modelValue', false)">
    <form class="decision-form" @submit.prevent="submit">
      <label>决策标题<input v-model="form.title" maxlength="200" required placeholder="例如：前端状态管理采用 Pinia" /></label>
      <label>背景与问题<textarea v-model="form.context" maxlength="20000" rows="3" required placeholder="当时遇到了什么问题？" /></label>
      <label>备选方案 <small>每行一项，可留空</small><textarea v-model="form.options" rows="3" placeholder="方案 A&#10;方案 B" /></label>
      <label>状态<select v-model="form.status" :disabled="initial?.status === 'ACCEPTED'"><option value="PROPOSED">提议中</option><option value="ACCEPTED">已采纳</option></select></label>
      <template v-if="accepted">
        <label>最终选择<textarea v-model="form.decision" maxlength="20000" rows="2" required /></label>
        <label>选择理由<textarea v-model="form.rationale" maxlength="20000" rows="2" required /></label>
      </template>
      <label>预期影响与取舍<textarea v-model="form.consequences" maxlength="20000" rows="2" /></label>
      <label>重新评估条件<textarea v-model="form.revisitCondition" maxlength="20000" rows="2" /></label>
      <label v-if="initial">本次变更说明<input v-model="form.changeSummary" maxlength="500" placeholder="可选" /></label>
      <div class="form-actions"><el-button @click="emit('update:modelValue', false)">取消</el-button><el-button type="primary" native-type="submit" :loading="busy">保存决策</el-button></div>
    </form>
  </el-dialog>
</template>

<style scoped>
.decision-form{display:grid;gap:14px}.decision-form label{display:grid;gap:6px;color:var(--sc-text);font-size:14px;font-weight:600}.decision-form small{display:inline;color:var(--sc-text-secondary);font-weight:400}.decision-form input,.decision-form textarea,.decision-form select{width:100%;box-sizing:border-box;border:1px solid var(--sc-border-soft);border-radius:10px;background:var(--sc-card-surface);padding:10px 12px;color:var(--sc-text);font:inherit;font-weight:400;resize:vertical}.form-actions{display:flex;justify-content:flex-end;gap:8px;margin-top:4px}
</style>
