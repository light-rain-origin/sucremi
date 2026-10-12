import { fileURLToPath } from 'node:url'
import { mergeConfig, defineConfig, configDefaults } from 'vitest/config'
import viteConfig from './vite.config.ts'

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
      exclude: [...configDefaults.exclude, 'e2e/**'],
      root: fileURLToPath(new URL('./', import.meta.url)),
      server: {
        deps: {
          // element-plus 的表单校验依赖 async-validator 的默认导出，
          // 交给 Vite 统一转换（与浏览器一致），避免 Node 外部化时的互操作差异。
          inline: ['element-plus'],
        },
      },
    },
  }),
)
