import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getDashboardSummary } from '@/api/dashboard'
import type { DashboardSummary } from '@/types/dashboard'

export const useDashboardStore = defineStore('dashboard', () => {
  const summary = ref<DashboardSummary | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

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

  return { summary, loading, error, fetchSummary }
})
