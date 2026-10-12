# apps/web —— Sucremi 前端

个人开发工作台的前端（Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus）。

项目背景、环境要求、数据库与联调方式见**仓库根目录 [`README.md`](../../README.md)** 与
[`docs/DEVELOPMENT.md`](../../docs/DEVELOPMENT.md)，此处只列本包命令。

## 命令

```sh
npm install          # 安装依赖
npm run dev          # 开发服务器（默认 5173，/api 代理到 8080）
npm run build        # 类型检查 + 生产构建
npm run test:unit -- --run   # 单元测试（Vitest）
npm run lint         # oxlint + eslint
```

## 目录约定

- `src/api/` —— 后端接口封装（统一 `ApiResponse` 结构）
- `src/views/` —— 页面；`src/router/` 路由；`src/stores/` Pinia 状态
- `src/components/` —— 可复用组件；`src/styles/tokens.css` 设计变量
- `src/types/` —— 与后端 DTO 对应的类型
