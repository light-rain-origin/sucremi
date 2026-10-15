<script setup lang="ts">
import type { DecisionRevision } from '@/types/decision'
import DecisionStatusBadge from './DecisionStatusBadge.vue'
import { formatDateTime } from '@/utils/datetime'
defineProps<{ revisions: DecisionRevision[]; loading?: boolean; error?: string }>()
</script>

<template>
  <section class="revision-list" aria-labelledby="revision-heading">
    <h2 id="revision-heading">历史版本</h2>
    <p v-if="loading" class="empty">正在读取历史版本…</p>
    <p v-else-if="error" class="empty error" role="alert">{{ error }}</p>
    <p v-else-if="!revisions.length" class="empty">暂无历史版本</p>
    <article v-for="revision in revisions" v-else :key="revision.id" class="revision">
      <header><strong>版本 {{ revision.revisionNumber }}</strong><DecisionStatusBadge :status="revision.status" /><time>{{ formatDateTime(revision.createdAt) }}</time></header>
      <p v-if="revision.changeSummary" class="change-summary">{{ revision.changeSummary }}</p>
      <h3>{{ revision.title }}</h3><p class="body">{{ revision.context }}</p>
      <dl v-if="revision.decision"><dt>最终选择</dt><dd>{{ revision.decision }}</dd><dt>选择理由</dt><dd>{{ revision.rationale }}</dd></dl>
    </article>
  </section>
</template>

<style scoped>
.revision-list{display:grid;gap:12px}h2{margin:0;color:var(--sc-text);font-size:18px}.revision{padding:16px;border:1px solid var(--sc-border-soft);border-radius:15px;background:var(--sc-card-surface)}header{display:flex;align-items:center;gap:10px;color:var(--sc-text)}time{margin-left:auto;color:var(--sc-text-placeholder);font-size:12px}.revision h3{margin:12px 0 6px;color:var(--sc-text);font-size:15px}.body,.change-summary{margin:0;color:var(--sc-text-secondary);line-height:1.65;white-space:pre-wrap}.change-summary{margin-top:10px;color:var(--sc-brand-alt-strong)}dl{display:grid;grid-template-columns:86px 1fr;gap:6px 12px;margin:12px 0 0;font-size:14px;line-height:1.6}dt{color:var(--sc-brand-alt-strong)}dd{margin:0;color:var(--sc-text);white-space:pre-wrap}.empty{color:var(--sc-text-secondary)}.error{color:var(--el-color-danger)}
</style>
