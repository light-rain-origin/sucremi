import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getDashboardSummary, getProjectFlow } from '@/api/dashboard'
import type { DashboardSummary, ProjectFlowSummary } from '@/types/dashboard'

export const useDashboardStore = defineStore('dashboard', () => {
  const summary = ref<DashboardSummary | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const projectFlow = ref<ProjectFlowSummary | null>(null)
  const projectFlowLoading = ref(false)
  const projectFlowError = ref<string | null>(null)

  async function fetchSummary() {
    loading.value = true
    error.value = null
    try {
      const res = await getDashboardSummary()
      if (res.code === 0) {
        summary.value = res.data
      } else {
        error.value = res.message
      }
    } catch (e) {
      error.value = (e as Error).message || '请求失败'
    } finally {
      loading.value = false
    }
  }

  async function fetchProjectFlow() {
    projectFlowLoading.value = true
    projectFlowError.value = null
    try {
      const res = await getProjectFlow()
      if (res.code === 0) {
        projectFlow.value = res.data
      } else {
        projectFlowError.value = res.message
      }
    } catch (e) {
      projectFlowError.value = (e as Error).message || '请求失败'
    } finally {
      projectFlowLoading.value = false
    }
  }

  return {
    summary,
    loading,
    error,
    fetchSummary,
    projectFlow,
    projectFlowLoading,
    projectFlowError,
    fetchProjectFlow,
  }
})
