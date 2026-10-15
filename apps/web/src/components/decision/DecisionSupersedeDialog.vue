<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { ProjectDecision } from '@/types/decision'
import type { SupersedePayload } from '@/api/decision'

const props = defineProps<{ modelValue: boolean; decision: ProjectDecision; busy?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean]; supersede: [payload: SupersedePayload] }>()
const form = reactive({ title: '', context: '', options: '', decision: '', rationale: '', consequences: '', revisitCondition: '', changeSummary: '' })
watch(() => props.modelValue, (visible) => { if (visible) Object.assign(form, { title: '', context: '', options: '', decision: '', rationale: '', consequences: '', revisitCondition: '', changeSummary: '' }) })
function submit() {
  if (!form.title.trim() || !form.context.trim() || !form.decision.trim() || !form.rationale.trim()) return
  emit('supersede', { expectedVersion: props.decision.version, title: form.title.trim(), context: form.context.trim(),
    options: form.options.split('\n').map((value) => value.trim()).filter(Boolean), decision: form.decision.trim(), rationale: form.rationale.trim(),
    consequences: form.consequences.trim() || undefined, revisitCondition: form.revisitCondition.trim() || undefined,
    changeSummary: form.changeSummary.trim() || undefined })
}
</script>

<template>
  <el-dialog :model-value="modelValue" title="用新决策替代" width="min(650px, 94vw)" @close="emit('update:modelValue', false)">
    <p class="intro">旧决策会保留为“已替代”，并与新决策建立关联。</p>
    <form class="form" @submit.prevent="submit">
      <label>新决策标题<input v-model="form.title" maxlength="200" required /></label>
      <label>背景与问题<textarea v-model="form.context" rows="2" required /></label>
      <label>备选方案 <small>每行一项</small><textarea v-model="form.options" rows="2" /></label>
      <label>最终选择<textarea v-model="form.decision" rows="2" required /></label>
      <label>选择理由<textarea v-model="form.rationale" rows="2" required /></label>
      <label>预期影响与取舍<textarea v-model="form.consequences" rows="2" /></label>
      <label>重新评估条件<textarea v-model="form.revisitCondition" rows="2" /></label>
      <label>替代说明<input v-model="form.changeSummary" maxlength="500" placeholder="可选" /></label>
      <div class="actions"><el-button @click="emit('update:modelValue', false)">取消</el-button><el-button type="primary" native-type="submit" :loading="busy">确认替代</el-button></div>
    </form>
  </el-dialog>
</template>

<style scoped>
.intro{margin-top:0;color:var(--sc-text-secondary);font-size:14px}.form{display:grid;gap:12px}.form label{display:grid;gap:5px;color:var(--sc-text);font-size:14px;font-weight:600}.form small{display:inline;color:var(--sc-text-secondary);font-weight:400}.form input,.form textarea{box-sizing:border-box;width:100%;border:1px solid var(--sc-border-soft);border-radius:9px;background:var(--sc-card-surface);padding:9px 11px;color:var(--sc-text);font:inherit;font-weight:400;resize:vertical}.actions{display:flex;justify-content:flex-end;gap:8px}
</style>
