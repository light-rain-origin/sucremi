<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useProjectStore } from '@/stores/project'
import type { ProjectForm } from '@/types/project'
import ProjectCard from '@/components/ProjectCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import ProjectFormDialog from '@/components/ProjectFormDialog.vue'

const router = useRouter()
const store = useProjectStore()
const { items, total, loading, error, query } = storeToRefs(store)

const dialogVisible = ref(false)

let timer: ReturnType<typeof setTimeout> | undefined
function debouncedFetch() {
  clearTimeout(timer)
  timer = setTimeout(() => store.fetchList(), 250)
}

watch(
  () => [query.value.q, query.value.status, query.value.sort, query.value.archived],
  () => {
    query.value.page = 1
    debouncedFetch()
  },
)
watch(() => query.value.page, () => store.fetchList())

onMounted(() => store.fetchList())

function openDetail(id: number) {
  router.push(`/projects/${id}`)
}

async function onCreate(form: ProjectForm) {
  try {
    await store.create(form)
    dialogVisible.value = false
    ElMessage.success('项目已创建')
    query.value.page = 1
    store.fetchList()
  } catch (e) {
    ElMessage.error((e as Error).message || '创建失败')
  }
}
</script>

<template>
  <section class="projects">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="query.q" placeholder="搜索名称/简介" clearable class="search" />
        <el-select v-model="query.status" placeholder="全部状态" clearable class="w140">
          <el-option label="进行中" value="ACTIVE" />
          <el-option label="暂停" value="PAUSED" />
          <el-option label="完成" value="DONE" />
        </el-select>
        <el-select v-model="query.sort" class="w160">
          <el-option label="最近更新" value="updatedAt,desc" />
          <el-option label="最早创建" value="createdAt,asc" />
          <el-option label="名称" value="name,asc" />
          <el-option label="进度高→低" value="progress,desc" />
        </el-select>
      </div>
      <el-button type="primary" @click="dialogVisible = true">新建项目</el-button>
    </div>

    <el-radio-group v-model="query.archived" class="seg">
      <el-radio-button :value="false">进行中</el-radio-button>
      <el-radio-button :value="true">已归档</el-radio-button>
    </el-radio-group>

    <div v-loading="loading" class="list-area">
      <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="mb" />
      <EmptyState
        v-else-if="!loading && items.length === 0"
        :text="query.archived ? '没有已归档的项目' : '还没有项目，创建第一个吧'"
      >
        <el-button v-if="!query.archived" type="primary" @click="dialogVisible = true">
          新建项目
        </el-button>
      </EmptyState>
      <div v-else class="grid">
        <ProjectCard v-for="p in items" :key="p.id" :project="p" @open="openDetail" />
      </div>
    </div>

    <div v-if="total > query.size" class="pager">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="query.size"
        :current-page="query.page"
        @current-change="(p: number) => (query.page = p)"
      />
    </div>

    <ProjectFormDialog v-model="dialogVisible" mode="create" @submit="onCreate" />
  </section>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.search {
  width: 220px;
}
.w140 {
  width: 140px;
}
.w160 {
  width: 160px;
}
.seg {
  margin-bottom: 16px;
}
.list-area {
  min-height: 160px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.pager {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.mb {
  margin-bottom: 12px;
}
</style>
