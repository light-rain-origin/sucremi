<script setup lang="ts">
import type { ProjectDecision } from '@/types/decision'
import DecisionStatusBadge from './DecisionStatusBadge.vue'
defineProps<{ decision: ProjectDecision }>()
const emit = defineEmits<{ edit: [decision: ProjectDecision] }>()
</script>

<template>
  <article class="decision-card">
    <div class="decision-top"><h3>{{ decision.title }}</h3><DecisionStatusBadge :status="decision.status" /></div>
    <p>{{ decision.context }}</p>
    <div v-if="decision.decision" class="choice"><strong>最终选择</strong><span>{{ decision.decision }}</span></div>
    <div class="actions"><RouterLink :to="`/projects/${decision.projectId}/decisions/${decision.id}`">查看详情 <span aria-hidden="true">→</span></RouterLink><button v-if="decision.status !== 'SUPERSEDED'" type="button" @click="emit('edit', decision)">编辑</button></div>
  </article>
</template>

<style scoped>
.decision-card{padding:16px;border:1px solid var(--sc-border-soft);border-radius:16px;background:var(--sc-card-surface)}.decision-top{display:flex;align-items:center;justify-content:space-between;gap:12px}.decision-top h3{margin:0;color:var(--sc-text);font-size:16px}.decision-card>p{margin:10px 0;color:var(--sc-text-secondary);line-height:1.65;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}.choice{display:grid;gap:4px;color:var(--sc-text);font-size:14px}.choice strong{color:var(--sc-brand-alt-strong);font-size:12px}.actions{display:flex;gap:16px;margin-top:12px}.actions a,.actions button{color:var(--sc-brand-alt-strong);font-weight:600;font-size:13px;background:none;border:0;padding:0;cursor:pointer}
</style>
