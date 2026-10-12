import type { Project } from './project'

/**
 * 与后端 DashboardSummary 对应。
 * 计数均为当前用户、未归档项目（archivedProjects 除外）。
 */
export interface DashboardSummary {
  totalProjects: number
  activeProjects: number
  pausedProjects: number
  doneProjects: number
  archivedProjects: number
  recentProjects: Project[]
}

/** 项目流程卡片数据：仅包括当前用户未归档项目。 */
export interface ProjectFlowSummary {
  unfinishedCount: number
  ongoingCount: number
  completedCount: number
  projects: Project[]
}
