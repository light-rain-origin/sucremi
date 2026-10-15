<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/stores/dashboard'
import ProjectFlowCard from '@/components/ProjectFlowCard.vue'

const router = useRouter()
const dashboard = useDashboardStore()
const { projectFlow, projectFlowLoading, projectFlowError } = storeToRefs(dashboard)

onMounted(() => {
  dashboard.fetchProjectFlow()
})

function openDetail(id: number) {
  router.push(`/projects/${id}`)
}
</script>

<template>
  <section class="dashboard" aria-label="首页概览">
    <ProjectFlowCard
      :flow="projectFlow"
      :loading="projectFlowLoading"
      :error="projectFlowError"
      @open="openDetail"
      @view-all="router.push('/projects')"
    />
  </section>
</template>
