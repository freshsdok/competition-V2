<template>
  <el-dialog
    title="设置公示提示信息"
    v-model="visible"
    width="800px"
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
      <el-form-item label="提示信息" prop="tipInfo" style="align-items: flex-start;">
        <Editor
          v-model="form.tipInfo"
          :min-height="200"
          placeholder="请输入公示页面的提示信息，支持HTML"
        />
        <span class="form-tip">若为空，则不展示公示提示信息</span>
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
import { setHintText } from '@/api/tournament/awardPublicity'
import modal from '@/plugins/modal'
import Editor from '@/components/Editor'

const emit = defineEmits(['success'])

const visible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const currentRow = ref(null)

const form = ref({
  tipInfo: ''
})

/** 打开弹框 */
async function openDialog(row) {
  visible.value = true
  currentRow.value = row
  await resetForm()
  // 带出现有提示信息，如果有的话
  setTimeout(() => {
    nextTick(() => {
      if (row.tipInfo) {
        form.value.tipInfo = row.tipInfo
      }
    })
  }, 200)
}

/** 重置表单 */
function resetForm() {
  return new Promise((resolve) => {
    form.value = {
      tipInfo: ''
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

/** 过滤空HTML标签 */
function filterEmptyHtml(html) {
  if (!html) return ''
  // 移除所有空白字符后检查是否还有实际内容
  const textContent = html.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, '').trim()
  if (!textContent) {
    return ''
  }
  return html
}

/** 提交表单 */
function submitForm() {
  submitLoading.value = true
  const data = {
    id: currentRow.value.id,
    tipInfo: filterEmptyHtml(form.value.tipInfo)
  }
  setHintText(data).then(response => {
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
