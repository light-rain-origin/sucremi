import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { DEFAULT_SKIN, SKIN_STORAGE_KEY, isSkinId, useSkinStore } from '../skin'

function currentAttr() {
  return document.documentElement.dataset.skin
}

describe('useSkinStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    window.localStorage.clear()
    document.documentElement.removeAttribute('data-skin')
    vi.restoreAllMocks()
  })

  it('默认皮肤是 classic，恢复时只落属性、不写存储', async () => {
    const store = useSkinStore()
    expect(store.current).toBe(DEFAULT_SKIN)

    await store.restore()

    expect(currentAttr()).toBe('classic')
    expect(window.localStorage.getItem(SKIN_STORAGE_KEY)).toBeNull()
  })

  it('切换皮肤会落 data-skin 并记住选择', async () => {
    const store = useSkinStore()

    await store.apply('remi-macaron')

    expect(store.current).toBe('remi-macaron')
    expect(currentAttr()).toBe('remi-macaron')
    expect(window.localStorage.getItem(SKIN_STORAGE_KEY)).toBe('remi-macaron')
  })

  it('启动时恢复已保存的皮肤', async () => {
    window.localStorage.setItem(SKIN_STORAGE_KEY, 'remi-macaron')
    const store = useSkinStore()
    expect(store.current).toBe('remi-macaron')

    await store.restore()

    expect(currentAttr()).toBe('remi-macaron')
  })

  it('存储里是非法值时回落默认皮肤', () => {
    window.localStorage.setItem(SKIN_STORAGE_KEY, 'not-a-skin')
    expect(useSkinStore().current).toBe(DEFAULT_SKIN)
  })

  it('非法取值被忽略，不改变当前皮肤', async () => {
    const store = useSkinStore()

    await store.apply('remi')
    await store.apply(null)
    await store.apply({ id: 'remi-macaron' })

    expect(store.current).toBe(DEFAULT_SKIN)
    expect(currentAttr()).toBeUndefined()
    expect(window.localStorage.getItem(SKIN_STORAGE_KEY)).toBeNull()
  })

  it('存储不可用（隐私模式）时切换照常生效', async () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('denied')
    })
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('denied')
    })
    const store = useSkinStore()
    expect(store.current).toBe(DEFAULT_SKIN)

    await store.apply('remi-macaron')

    expect(currentAttr()).toBe('remi-macaron')
  })

  it('isSkinId 只认清单里的 id', () => {
    expect(isSkinId('classic')).toBe(true)
    expect(isSkinId('remi-macaron')).toBe(true)
    expect(isSkinId('Remi-Macaron')).toBe(false)
    expect(isSkinId(undefined)).toBe(false)
  })
})
