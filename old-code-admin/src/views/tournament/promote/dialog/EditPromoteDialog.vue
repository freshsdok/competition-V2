<template>
  <el-dialog
    :title="dialogTitle"
    v-model="visible"
    width="800px"
    append-to-body
    :close-on-click-modal="false"
    @close="cancel"
  >
    <el-form
      ref="formRef"
      :model="formData"
      label-width="100px"
    >
      <el-form-item label="比赛名称">
        <span>{{ currentRow?.competitionName }}</span>
      </el-form-item>

      <!-- 费用编辑 -->
      <template v-if="editType === 'fee'">
        <el-form-item label="费用" prop="fee">
          <el-input-number
            v-model="formData.fee"
            :min="0"
            :precision="2"
            :step="10"
            :max="0"
            :disabled="true"
            placeholder="请输入费用，0表示免费"
            style="width: 100%;"
            controls-position="right"
          />
        </el-form-item>
        <div class="form-tip">单位：元，0表示免费</div>
      </template>

      <!-- 报名时间编辑 -->
      <template v-if="editType === 'time'">
        <el-form-item label="报名时间" prop="timeRange">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="报名开始时间"
            end-placeholder="报名结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
            clearable
          />
        </el-form-item>
        <div class="form-tip">设置报名开始和结束时间，不选表示清空时间</div>
      </template>

      <!-- 提示语编辑 -->
      <template v-if="editType === 'hint'">
        <el-form-item label="提示语" prop="promotedHint" style="align-items: flex-start;">
          <Editor
            v-model="formData.promotedHint"
            :min-height="200"
            placeholder="请输入提示语，支持HTML"
          />
          <span class="hint-tip">若为空，则不展示提示语</span>
        </el-form-item>
      </template>
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
import { ref, computed } from 'vue'
import { editCompetitionPromotedInfo } from '@/api/tournament/promote'
import modal from '@/plugins/modal'
import Editor from '@/components/Editor'

const emit = defineEmits(['success'])

const visible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const currentRow = ref(null)
const editType = ref('') // 'fee' | 'time' | 'hint'

// 表单数据 - 包含所有字段
const formData = ref({
  promotedId: '',
  fee: 0,
  applyStartTime: '',
  applyEndTime: '',
  promotedHint: ''
})

// 时间范围（单独处理）
const timeRange = ref([])

// 弹框标题
const dialogTitle = computed(() => {
  const titles = {
    'fee': '设置费用',
    'time': '设置报名时间',
    'hint': '设置提示语'
  }
  return titles[editType.value] || '编辑'
})

/** 打开弹框
 * @param {Object} row - 当前行数据
 * @param {string} type - 编辑类型：'fee' | 'time' | 'hint'
 */
function openDialog(row, type) {
  console.log('openDialog row:', row)
  console.log('editType:', type)
  
  visible.value = true
  currentRow.value = row
  editType.value = type

  // 初始化所有字段
  formData.value = {
    promotedId: row.promotedId,
    fee: row.fee !== undefined && row.fee !== null && row.fee !== '' ? Number(row.fee) : 0,
    applyStartTime: row.applyStartTime || '',
    applyEndTime: row.applyEndTime || '',
    promotedHint: row.promotedHint || ''
  }

  // 初始化时间范围
  if (row.applyStartTime || row.applyEndTime) {
    timeRange.value = [row.applyStartTime || '', row.applyEndTime || '']
  } else {
    timeRange.value = []
  }

  console.log('formData:', formData.value)
  console.log('timeRange:', timeRange.value)
}

/** 取消 */
function cancel() {
  visible.value = false
  resetForm()
}

/** 重置表单 */
function resetForm() {
  formData.value = {
    promotedId: '',
    fee: 0,
    applyStartTime: '',
    applyEndTime: '',
    promotedHint: ''
  }
  timeRange.value = []
  editType.value = ''
}

/** 过滤空HTML标签 */
function filterEmptyHtml(html) {
  if (!html) return ''
  const textContent = html.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, '').trim()
  if (!textContent) {
    return ''
  }
  return html
}

/** 提交表单 */
function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return

    submitLoading.value = true

    // 根据编辑类型更新对应字段
    if (editType.value === 'time') {
      formData.value.applyStartTime = timeRange.value?.[0] ?? ''
      formData.value.applyEndTime = timeRange.value?.[1] ?? ''
    } else if (editType.value === 'hint') {
      formData.value.promotedHint = filterEmptyHtml(formData.value.promotedHint)
    } else if (editType.value === 'fee') {
      // 费用默认为0
      formData.value.fee = formData.value.fee ?? 0
    }

    // 提交所有字段
    const data = {
      promotedId: formData.value.promotedId,
      fee: formData.value.fee,
      applyStartTime: formData.value.applyStartTime,
      applyEndTime: formData.value.applyEndTime,
      promotedHint: formData.value.promotedHint
    }

    console.log('提交数据:', data)

    editCompetitionPromotedInfo(data).then(response => {
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
  margin-left: 100px;
  margin-top: -10px;
  margin-bottom: 10px;
}

.hint-tip {
  color: #e6a23c;
  font-size: 12px;
  margin-left: 8px;
}
</style>
