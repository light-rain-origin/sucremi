import type { Project } from './project'
import type { KeyDecision } from './decision'

/** 与后端检查点响应对应。 */
export interface ProjectCheckpoint {
  id: number
  projectId: number
  summary: string
  currentFocus: string | null
  blockers: string | null
  createdAt: string
}

export type ProjectActionStatus = 'TODO' | 'DONE'

/** 与后端下一步行动响应对应。 */
export interface ProjectAction {
  id: number
  projectId: number
  title: string
  details: string | null
  status: ProjectActionStatus
  sortOrder: number
  createdAt: string
  updatedAt: string
  completedAt: string | null
}

/** 项目恢复接口聚合出的项目、检查点和行动数据。 */
export interface ProjectRecovery {
  project: Project
  latestCheckpoint: ProjectCheckpoint | null
  actions: ProjectAction[]
  recoverySummary: string | null
  keyDecisions: KeyDecision[]
}

export interface CreateCheckpointPayload {
  summary: string
  currentFocus?: string
  blockers?: string
}

export interface CreateActionPayload {
  title: string
  details?: string
}
