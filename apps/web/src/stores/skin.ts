import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 皮肤清单。`id` 同时是 `document.documentElement.dataset.skin` 的取值，
 * 也是 `styles/skins/<id>.css` 的文件名 —— 三者一一对应，新增皮肤只需在这里加一行 + 加一个样式文件。
 *
 * `classic` 不出现在 `loaders` 里：它的值就是 `tokens.css` 中 `:root` 的默认值，
 * 不设 `data-skin` 属性即是 Classic，因此无需额外样式文件，也永远不会「加载失败」。
 */
export const SKINS = [
  { id: 'classic', label: '经典粉紫', hint: '默认皮肤' },
  { id: 'remi-macaron', label: 'Remi 马卡龙', hint: '抹茶绿 · 紧凑' },
] as const

export type SkinId = (typeof SKINS)[number]['id']

export const SKIN_STORAGE_KEY = 'sucremi.skin'

export const DEFAULT_SKIN: SkinId = 'classic'

export function isSkinId(value: unknown): value is SkinId {
  return SKINS.some((s) => s.id === value)
}

/** 非默认皮肤按需下载：只有用户真的选了才加载对应样式表。 */
const loaders: Partial<Record<SkinId, () => Promise<unknown>>> = {
  'remi-macaron': () => import('@/styles/skins/remi-macaron.css'),
}

function readStored(): SkinId {
  try {
    const value = window.localStorage.getItem(SKIN_STORAGE_KEY)
    return isSkinId(value) ? value : DEFAULT_SKIN
  } catch {
    // 隐私模式 / 禁用存储：当作没存过，回落默认皮肤
    return DEFAULT_SKIN
  }
}

function persist(skin: SkinId) {
  try {
    window.localStorage.setItem(SKIN_STORAGE_KEY, skin)
  } catch {
    // 存不下就只在本次会话内生效，不影响切换本身
  }
}

/**
 * 当前皮肤。只影响样式，不参与任何业务逻辑 ——
 * 接口契约、数据模型、权限与页面结构都不因皮肤而变。
 */
export const useSkinStore = defineStore('skin', () => {
  const current = ref<SkinId>(readStored())

  /**
   * 唯一的副作用入口。顺序刻意是「先加载样式、后落属性」：
   * 加载失败时属性与状态都不会被改动，界面不会停在切了一半的样子。
   */
  async function activate(skin: SkinId) {
    await loaders[skin]?.()
    document.documentElement.dataset.skin = skin
    current.value = skin
  }

  /** 用户主动切换。加载失败向上抛，由调用方决定怎么提示。 */
  async function apply(skin: unknown) {
    if (!isSkinId(skin) || skin === current.value) return
    await activate(skin)
    persist(skin)
  }

  /**
   * 启动时恢复已保存的皮肤，只落属性、不写存储 ——
   * 存储里的值本身就是它要恢复的内容，回写没有意义。
   * 样式表取不回来（离线 / 产物 chunk 失效）时回落默认皮肤，
   * 免得切换器显示「Remi 马卡龙」而界面其实是经典粉紫。
   */
  async function restore() {
    try {
      await activate(current.value)
    } catch {
      await activate(DEFAULT_SKIN)
    }
  }

  return { current, apply, restore }
})
