import type { PageResult } from './common'

export type ProjectActivityType =
  | 'PROJECT_CREATED'
  | 'PROJECT_STATUS_CHANGED'
  | 'PROJECT_ARCHIVED'
  | 'PROJECT_UNARCHIVED'
  | 'CHECKPOINT_CREATED'
  | 'ACTION_CREATED'
  | 'ACTION_COMPLETED'
  | 'ACTION_REOPENED'

export interface ProjectActivity {
  id: number
  projectId: number
  projectName: string
  eventType: ProjectActivityType
  metadataVersion: number
  metadata: string
  createdAt: string
}

export type ProjectActivityPage = PageResult<ProjectActivity>
