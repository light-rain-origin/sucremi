import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useProjectStore } from '../project'
import type { Project } from '@/types/project'

vi.mock('@/api/project', () => ({
  listProjects: vi.fn<typeof import('@/api/project').listProjects>(),
  createProject: vi.fn<typeof import('@/api/project').createProject>(),
  archiveProject: vi.fn<typeof import('@/api/project').archiveProject>(),
  unarchiveProject: vi.fn<typeof import('@/api/project').unarchiveProject>(),
}))

import { archiveProject, createProject, listProjects, unarchiveProject } from '@/api/project'

const mocked = {
  list: vi.mocked(listProjects),
  create: vi.mocked(createProject),
  archive: vi.mocked(archiveProject),
  unarchive: vi.mocked(unarchiveProject),
}

const project: Project = {
  id: 1,
  name: 'P',
  summary: null,
  goal: null,
  status: 'ACTIVE',
  progress: 0,
  archived: false,
  createdAt: '2026-10-12T11:34:37',
  updatedAt: '2026-10-12T11:34:37',
}

const ok = <T>(data: T) => ({ code: 0, message: 'OK', data, requestId: 'r', timestamp: 1 })
const fail = (code: number, message: string) => ({
  code,
  message,
  data: undefined as never,
  requestId: 'r',
  timestamp: 1,
})

describe('useProjectStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    Object.values(mocked).forEach((m) => m.mockReset())
  })

  it('loads list into items and total', async () => {
    mocked.list.mockResolvedValue(ok({ list: [project], total: 1, page: 1, size: 12 }))
    const store = useProjectStore()

    await store.fetchList()

    expect(store.items).toEqual([project])
    expect(store.total).toBe(1)
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('records error when the list request fails', async () => {
    mocked.list.mockRejectedValue(new Error('网络异常'))
    const store = useProjectStore()

    await store.fetchList()

    expect(store.error).toBe('网络异常')
    expect(store.loading).toBe(false)
  })

  it('throws on create failure and returns project on success', async () => {
    const store = useProjectStore()

    mocked.create.mockResolvedValue(fail(40000, '名称不能为空'))
    await expect(
      store.create({ name: '', summary: '', goal: '', status: 'ACTIVE', progress: 0 }),
    ).rejects.toThrow('名称不能为空')

    mocked.create.mockResolvedValue(ok(project))
    await expect(
      store.create({ name: 'P', summary: '', goal: '', status: 'ACTIVE', progress: 0 }),
    ).resolves.toEqual(project)
  })

  it('throws on archive failure and resolves on success', async () => {
    const store = useProjectStore()

    mocked.archive.mockResolvedValue(fail(40400, '项目不存在'))
    await expect(store.archive(99)).rejects.toThrow('项目不存在')

    mocked.archive.mockResolvedValue(ok(undefined))
    await expect(store.archive(1)).resolves.toBeUndefined()
  })

  it('throws on unarchive failure and resolves on success', async () => {
    const store = useProjectStore()

    mocked.unarchive.mockResolvedValue(fail(40400, '项目不存在'))
    await expect(store.unarchive(99)).rejects.toThrow('项目不存在')

    mocked.unarchive.mockResolvedValue(ok(undefined))
    await expect(store.unarchive(1)).resolves.toBeUndefined()
  })
})
