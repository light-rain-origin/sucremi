export type ProjectStatus = 'ACTIVE' | 'PAUSED' | 'DONE'

/** 与后端 ProjectResponse 对应。 */
export interface Project {
  id: number
  name: string
  summary: string | null
  goal: string | null
  status: ProjectStatus
  progress: number
  archived: boolean
  createdAt: string
  updatedAt: string
}

/** 新建/编辑项目的表单载荷。 */
export interface ProjectForm {
  name: string
  summary: string
  goal: string
  status: ProjectStatus
  progress: number
}
