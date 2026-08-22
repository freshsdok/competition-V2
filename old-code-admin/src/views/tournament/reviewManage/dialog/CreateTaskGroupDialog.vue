<template>
  <el-dialog v-model="visible" title="创建评审任务分组" width="480px">
    <el-form label-position="top">
      <el-form-item label="分组名称" required>
        <el-input
          v-model="form.name"
          placeholder="请输入分组名称，如：软件类初赛组"
          size="small"
        />
      </el-form-item>
      <el-form-item :label="`已选${selectedTasks?.length || 0}个任务`">
        <div class="selected-list">
          <div v-for="(task, index) in selectedTasks" :key="task?.reviewId" class="selected-item">
            {{ index + 1 }}. {{ task?.reviewName }}
          </div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button size="small" @click="handleCancel">取消</el-button>
      <el-button type="primary" size="small" :loading="loading" @click="handleConfirm">创建分组</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createTaskGroup } from '@/api/tournament/reviewManage'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  selectedTasks: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive({
  name: ''
})

const loading = ref(false)

watch(() => props.modelValue, (val) => {
  if (val) {
    form.name = ''
  }
})

function handleCancel() {
  visible.value = false
}

async function handleConfirm() {
  if (!form.name) {
    ElMessage.warning('请输入分组名称')
    return
  }
  loading.value = true
  try {
    const res = await createTaskGroup({
      allotGroupName: form.name,
      reviewIdList: props.selectedTasks?.map(t => t?.reviewId) || []
    })
    if (res.code === 200) {
      ElMessage.success('创建成功')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(res.msg || '创建失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.selected-list {
  max-height: 120px;
  overflow-y: auto;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  width: 100%;
  .selected-item {
    font-size: 12px;
    color: #606266;
    line-height: 1.8;
  }
}
</style>
