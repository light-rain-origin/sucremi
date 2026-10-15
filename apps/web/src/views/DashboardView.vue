<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/stores/dashboard'
import DashboardBanner from '@/components/DashboardBanner.vue'
import DashboardStatsCards from '@/components/DashboardStatsCards.vue'
import ProjectFlowCard from '@/components/ProjectFlowCard.vue'

const router = useRouter()
const dashboard = useDashboardStore()
const { summary, projectFlow, projectFlowLoading, projectFlowError } = storeToRefs(dashboard)

onMounted(() => {
  dashboard.fetchProjectFlow()
  dashboard.fetchSummary()
})

function openDetail(id: number) {
  router.push(`/projects/${id}`)
}
</script>

<template>
  <section class="dashboard" aria-label="首页概览">
    <DashboardBanner />
    <DashboardStatsCards :summary="summary" />
    <ProjectFlowCard
      :flow="projectFlow"
      :loading="projectFlowLoading"
      :error="projectFlowError"
      @open="openDetail"
      @view-all="router.push('/projects')"
    />
  </section>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
</style>
