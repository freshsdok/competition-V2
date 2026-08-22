<template>
  <el-dialog
    title="设置公示过期时间"
    v-model="visible"
    width="500px"
    append-to-body
    :close-on-click-modal="false"
    @close="cancel"
  >
    <el-form
      ref="formRef"
      :model="form"
      label-width="100px"
    >
      <el-form-item label="比赛名称">
        <span>{{ currentRow?.competitionName }}</span>
      </el-form-item>
      <el-form-item label="过期时间" prop="expirationTime">
        <el-date-picker
          v-model="form.expirationTime"
          type="datetime"
          placeholder="请选择过期时间"
          style="width: 100%;"
          value-format="YYYY-MM-DD HH:mm:ss"
          :default-time="new Date('1970-01-01 23:59:59')"
          clearable
        />
        <span class="form-tip">若为空，则不公示</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { setExpireTime } from '@/api/tournament/awardPublicity'
import modal from '@/plugins/modal'

const emit = defineEmits(['success'])

const visible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const currentRow = ref(null)

const form = ref({
  expirationTime: ''
})

/** 打开弹框 */
async function openDialog(row) {
  visible.value = true
  currentRow.value = row
  await resetForm()
  // 带出现有过期时间，如果有的话
  if (row.expirationTime) {
    form.value.expirationTime = row.expirationTime
  }
}

/** 重置表单 */
function resetForm() {
  return new Promise((resolve) => {
    form.value = {
      expirationTime: ''
    }
    nextTick(() => {
      if (formRef.value) {
        formRef.value.resetFields()
      }
      resolve()
    })
  })
}

/** 取消 */
function cancel() {
  visible.value = false
  resetForm()
}

/** 提交表单 */
function submitForm() {
  submitLoading.value = true
  const data = {
    id: currentRow.value.id,
    expirationTime: form.value.expirationTime
  }
  setExpireTime(data).then(response => {
    if (response.code === 200) {
      modal.msgSuccess('设置成功')
      visible.value = false
      emit('success')
    } else {
      modal.msgWarning(response.msg || '操作失败')
    }
  }).finally(() => {
    submitLoading.value = false
  })
}

defineExpose({
  openDialog
})
</script>

<style scoped>
.form-tip {
  color: #e6a23c;
  font-size: 12px;
  margin-left: 8px;
}
</style>
