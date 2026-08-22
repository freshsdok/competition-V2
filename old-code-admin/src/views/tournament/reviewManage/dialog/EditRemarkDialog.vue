<template>
  <el-dialog v-model="visible" title="编辑评审备注" width="480px">
    <el-form label-position="top">
      <el-form-item label="项目">
        <div>{{ task?.reviewName || '-' }}</div>
      </el-form-item>
      <el-form-item label="评审备注">
        <el-input
          v-model="form.value"
          type="textarea"
          :rows="4"
          placeholder="请输入需要传达给评审专家的备注信息（空备注也可保存）"
          size="small"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button size="small" @click="handleCancel">取消</el-button>
      <el-button type="primary" size="small" :loading="loading" @click="handleConfirm">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, watch, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { batchSetRemark } from '@/api/tournament/reviewManage'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  task: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive({
  value: ''
})

const loading = ref(false)

watch(() => props.task, (val) => {
  form.value = val?.reviewDesc || ''
}, { immediate: true })

function handleCancel() {
  visible.value = false
}

async function handleConfirm() {
  if (!props.task) return
  
  loading.value = true
  try {
    await batchSetRemark({
      reviewIdList: [props.task.reviewId],
      reviewDesc: form.value || ''
    })
    props.task.reviewDesc = form.value
    ElMessage.success('保存成功')
    visible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}
</script>
