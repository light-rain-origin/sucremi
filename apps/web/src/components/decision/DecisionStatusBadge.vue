<script setup lang="ts">
import type { DecisionStatus } from '@/types/decision'
defineProps<{ status: DecisionStatus }>()
const labels: Record<DecisionStatus, string> = { PROPOSED: '提议中', ACCEPTED: '已采纳', SUPERSEDED: '已替代' }
</script>

<template><span class="status-badge" :class="status.toLowerCase()">{{ labels[status] }}</span></template>

<style scoped>
.status-badge{display:inline-flex;padding:4px 10px;border-radius:999px;font-size:12px;font-weight:650;background:#f1ebff;color:#7453c7}.accepted{background:#e7f4d7;color:#557d29}.superseded{background:#f1eff4;color:#837c8d}
</style>
