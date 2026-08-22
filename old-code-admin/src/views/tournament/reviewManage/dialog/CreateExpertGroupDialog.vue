<template>
  <el-dialog v-model="visible" title="创建评审专家分组" width="600px">
    <el-form label-position="top">
      <el-form-item label="分组名称" required>
        <el-input
          v-model="form.groupName"
          placeholder="请输入分组名称，如：软件类专家组"
          size="small"
        />
      </el-form-item>
      <el-form-item :label="`已选${selectedExperts?.length || 0}个专家`">
        <div class="selected-list">
          <div v-for="(expert, index) in selectedExperts" :key="expert?.userId" class="selected-item">
            {{ index + 1 }}、{{ expert?.nickName || '-' }}（{{ expert?.schoolName || '-' }}）
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
import { reactive, watch, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createExpertGroup } from '@/api/tournament/reviewManage'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  selectedExperts: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'switch-to-group'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive({
  groupName: ''
})

const loading = ref(false)

watch(() => props.modelValue, (val) => {
  if (val) {
    form.groupName = ''
  }
})

function handleCancel() {
  visible.value = false
}

async function handleConfirm() {
  if (!form.groupName) {
    ElMessage.warning('请输入分组名称')
    return
  }
  loading.value = true
  try {
    await createExpertGroup({
      groupName: form.groupName,
      specialistUserIdList: props.selectedExperts?.map(e => e?.userId) || []
    })
    ElMessage.success('创建成功')
    visible.value = false
    emit('success')
    emit('switch-to-group')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.selected-list {
  max-height: 200px;
  overflow-y: auto;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 4px;
  width: 100%;

  .selected-item {
    font-size: 14px;
    color: #606266;
    line-height: 2;
  }
}
</style>
