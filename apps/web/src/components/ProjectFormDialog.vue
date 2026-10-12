<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { ProjectForm, ProjectStatus } from '@/types/project'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  initial?: Partial<ProjectForm>
}>()
const emit = defineEmits<{ 'update:modelValue': [boolean]; submit: [ProjectForm] }>()

const formRef = ref<FormInstance>()
const form = reactive<ProjectForm>({
  name: '',
  summary: '',
  goal: '',
  status: 'ACTIVE',
  progress: 0,
})

const rules: FormRules<ProjectForm> = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { max: 100, message: '名称最多 100 字', trigger: 'blur' },
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const statusOptions: { value: ProjectStatus; label: string }[] = [
  { value: 'ACTIVE', label: '进行中' },
  { value: 'PAUSED', label: '暂停' },
  { value: 'DONE', label: '完成' },
]

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      form.name = props.initial?.name ?? ''
      form.summary = props.initial?.summary ?? ''
      form.goal = props.initial?.goal ?? ''
      form.status = props.initial?.status ?? 'ACTIVE'
      form.progress = props.initial?.progress ?? 0
      formRef.value?.clearValidate()
    }
  },
)

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) emit('submit', { ...form })
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="mode === 'create' ? '新建项目' : '编辑项目'"
    width="520px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" maxlength="100" placeholder="项目名称" />
      </el-form-item>
      <el-form-item label="简介" prop="summary">
        <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="500" placeholder="一句话简介" />
      </el-form-item>
      <el-form-item label="目标" prop="goal">
        <el-input v-model="form.goal" type="textarea" :rows="2" placeholder="项目目标" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" style="width: 100%">
          <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="进度" prop="progress">
        <el-slider v-model="form.progress" :min="0" :max="100" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="onSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>
