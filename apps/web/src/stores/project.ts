import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import {
  archiveProject,
  createProject,
  listProjects,
  unarchiveProject,
  type ProjectListParams,
} from '@/api/project'
import type { Project, ProjectForm } from '@/types/project'

export const useProjectStore = defineStore('project', () => {
  const items = ref<Project[]>([])
  const total = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const query = reactive<Required<ProjectListParams>>({
    q: '',
    status: '',
    archived: false,
    page: 1,
    size: 12,
    sort: 'updatedAt,desc',
  })

  async function fetchList() {
    loading.value = true
    error.value = null
    try {
      const res = await listProjects(query)
      if (res.code === 0) {
        items.value = res.data.list
        total.value = res.data.total
      } else {
        error.value = res.message
      }
    } catch (e) {
      error.value = (e as Error).message || '请求失败'
    } finally {
      loading.value = false
    }
  }

  async function create(form: ProjectForm): Promise<Project> {
    const res = await createProject(form)
    if (res.code !== 0) throw new Error(res.message)
    return res.data
  }

  async function archive(id: number) {
    const res = await archiveProject(id)
    if (res.code !== 0) throw new Error(res.message)
  }

  async function unarchive(id: number) {
    const res = await unarchiveProject(id)
    if (res.code !== 0) throw new Error(res.message)
  }

  return { items, total, loading, error, query, fetchList, create, archive, unarchive }
})
