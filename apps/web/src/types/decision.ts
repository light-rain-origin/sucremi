export type DecisionStatus = 'PROPOSED' | 'ACCEPTED' | 'SUPERSEDED'

export interface ProjectDecision {
  id: number
  projectId: number
  title: string
  context: string
  options: string[]
  decision: string | null
  rationale: string | null
  consequences: string | null
  revisitCondition: string | null
  status: DecisionStatus
  supersededById: number | null
  version: number
  createdAt: string
  updatedAt: string
}

export interface DecisionRevision extends Omit<ProjectDecision, 'createdAt' | 'updatedAt'> {
  revisionNumber: number
  changeSummary: string | null
  createdAt: string
}

export interface DecisionPayload {
  title: string
  context: string
  options: string[]
  decision?: string
  rationale?: string
  consequences?: string
  revisitCondition?: string
  status: DecisionStatus
  changeSummary?: string
  expectedVersion?: number
}

export interface KeyDecision {
  id: number
  title: string
  decision: string
  rationaleSummary: string | null
}
