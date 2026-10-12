<script setup lang="ts"></script>

<template>
  <header class="app-header">
    <span class="brand">Sucremi</span>
  </header>
  <main>
    <RouterView />
  </main>
</template>

<style scoped>
.app-header {
  padding: 12px 24px;
  border-bottom: 1px solid var(--el-border-color, #dcdfe6);
  font-weight: 600;
}
.brand {
  font-size: 18px;
}
</style>
