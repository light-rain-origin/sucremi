<script setup lang="ts">
import { computed } from 'vue'
import { ICON_PATHS, type IconName } from './icons'

const props = withDefaults(defineProps<{ name: IconName; size?: number | string }>(), { size: 20 })

const paths = computed(() => ICON_PATHS[props.name])
</script>

<template>
  <svg
    class="sc-icon"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.7"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    focusable="false"
  >
    <path v-for="(d, i) in paths" :key="i" :d="d" />
  </svg>
</template>

<style scoped>
.sc-icon {
  display: inline-block;
  flex-shrink: 0;
  vertical-align: -0.14em;
}
</style>
