<script setup lang="ts">
defineProps<{ value: number }>()
</script>

<template>
  <el-progress :percentage="value" :stroke-width="8" />
</template>
