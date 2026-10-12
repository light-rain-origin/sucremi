<script setup lang="ts">
defineEmits<{ navigate: [] }>()

const items = [
  { to: '/', label: '概览', exact: true },
  { to: '/projects', label: '项目', exact: false },
]
</script>

<template>
  <aside class="sidebar">
    <div class="brand">
      <span class="brand-mark">S</span>
      <span class="brand-name">Sucremi</span>
    </div>
    <nav class="nav">
      <RouterLink
        v-for="it in items"
        :key="it.to"
        :to="it.to"
        class="nav-item"
        active-class="active"
        :exact-active-class="it.exact ? 'active' : ''"
        @click="$emit('navigate')"
      >
        {{ it.label }}
      </RouterLink>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sc-sidebar-w);
  flex-shrink: 0;
  background: var(--sc-bg-elevated);
  border-right: 1px solid var(--sc-border);
  display: flex;
  flex-direction: column;
  padding: 16px 12px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px 20px;
  font-size: 18px;
  font-weight: 700;
  color: var(--sc-text);
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: var(--sc-primary);
  color: #fff;
  font-weight: 700;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  padding: 10px 12px;
  border-radius: var(--sc-radius-md);
  color: var(--sc-text-secondary);
  font-weight: 500;
  transition: background 0.15s, color 0.15s;
}

.nav-item:hover {
  background: var(--sc-bg-muted);
  color: var(--sc-text);
}

.nav-item.active {
  background: var(--el-color-primary-light-9);
  color: var(--sc-primary-active);
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    z-index: 30;
    transform: translateX(-100%);
    transition: transform 0.2s ease;
    box-shadow: var(--sc-shadow-md);
  }
  .sidebar.open {
    transform: translateX(0);
  }
}
</style>
