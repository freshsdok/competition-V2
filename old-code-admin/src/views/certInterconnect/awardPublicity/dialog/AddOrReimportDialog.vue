<template>
  <el-dialog
    :title="dialogTitle"
    v-model="visible"
    width="600px"
    append-to-body
    :close-on-click-modal="false"
    @close="cancel"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="比赛名称" v-if="isReimport">
        <span>{{ currentRow?.competitionName }}</span>
      </el-form-item>
      <el-form-item label="选择赛事" prop="competitionSeriesId" v-else>
        <el-select
          v-model="form.competitionSeriesId"
          placeholder="请选择赛事"
          style="width: 100%;"
          clearable
          @change="onCompetitionChange"
        >
          <el-option
            v-for="item in competitionOptions"
            :key="item.competitionSeriesId"
            :label="item.competitionName"
            :value="item.competitionSeriesId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="下载导入模板">
        <el-link :underline="false" @click="handleDownloadTemplate" type="primary">获奖公示导入模板.xlsx</el-link>
        <el-link :underline="false" @click="handleDownloadTemplate" type="primary" style="margin-left: 10px">下载</el-link>
      </el-form-item>
      <el-form-item label="导入Excel" prop="file">
        <el-upload
          ref="uploadRef"
          v-model:file-list="fileList"
          action="#"
          :before-upload="beforeUpload"
          :http-request="httpRequest"
        >
          <el-button type="primary" :icon="Upload">
            {{ fileList.length > 0 ? '重新选择' : '选择文件' }}
          </el-button>
          <template #tip>
            <div class="upload-tip">
              支持 xlsx / xls 格式，需包含：团队编号、奖项 字段
            </div>
          </template>
        </el-upload>
      </el-form-item>
      <el-form-item label="导入方式" prop="importType">
        <el-radio-group v-model="form.importType">
          <el-radio label="addition">
            追加导入
            <el-tag size="small" type="info" v-if="isReimport">保留原有数据</el-tag>
          </el-radio>
          <el-radio label="replace">
            替换导入
            <el-tag size="small" type="info" v-if="isReimport">清空后重新导入</el-tag>
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label-width="0" style="margin-top: -20px;">
        <div class="import-rules">
          <p class="rules-title">导入规则说明：</p>
          <div>1. 追加导入：保留上一次导入的所有获奖名单，追加新的获奖名单（遇到相同团队编号，会创建新的数据）。</div>
          <div>2. 替换导入：清空上一次导入的所有获奖名单，重新导入获奖名单。</div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">
          {{ isReimport ? '确定导入' : '确 定' }}
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog
    title="导入结果"
    v-model="resultVisible"
    width="520px"
    append-to-body
    :close-on-click-modal="false"
    @close="closeResult"
  >
    <div class="result-container">
      <!-- 导入失败情况 -->
      <template v-if="!importResult.importSuccess">
        <div class="result-header">
          <el-icon :size="48" color="#f56c6c">
            <CircleCloseFilled />
          </el-icon>
          <span class="result-title">导入失败</span>
        </div>
        <div class="result-summary">
          <div class="result-item result-item--fail">
            <el-icon :size="20">
              <WarningFilled />
            </el-icon>
            <span>{{ importResult.msg }}</span>
          </div>
        </div>
      </template>
      <!-- 导入成功情况 -->
      <template v-else>
        <div class="result-header">
          <el-icon :size="48" color="#67c23a">
            <CircleCheckFilled />
          </el-icon>
          <span class="result-title">导入完成</span>
        </div>
        <div class="result-summary">
          <div class="result-item result-item--success">
            <el-icon :size="20">
              <SuccessFilled />
            </el-icon>
            <span>成功导入 <strong>{{ importResult.successCount }}</strong> 条数据</span>
          </div>
          <div v-if="importResult.failTeamCodes && importResult.failTeamCodes.length > 0" class="result-item result-item--fail">
            <el-icon :size="20">
              <WarningFilled />
            </el-icon>
            <div class="fail-info">
              <span class="fail-label">未导入数据团队编号：</span>
              <div class="fail-tags">
                <el-tag
                  v-for="code in importResult.failTeamCodes"
                  :key="code"
                  type="danger"
                  size="small"
                  class="fail-tag"
                >
                  {{ code }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
    <template #footer>
      <el-button type="primary" @click="closeResult">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { importAwardPublicity } from '@/api/tournament/awardPublicity'
import { getSelectCompetitionList } from '@/api/certInterconnect/certConfig'
import modal from '@/plugins/modal'
import { Upload, CircleCheckFilled, CircleCloseFilled, SuccessFilled, WarningFilled } from '@element-plus/icons-vue'
import { downloadJS } from '@/utils/request'

const props = defineProps({
  mode: {
    type: String,
    default: 'add'
  }
})

const emit = defineEmits(['success'])

const visible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)
const fileList = ref([])
const currentRow = ref(null)
const competitionOptions = ref([])
const resultVisible = ref(false)
const importResult = ref({
  successCount: 0,
  failTeamCodes: [],
  importSuccess: true,
  msg: ''
})

const isReimport = computed(() => props.mode === 'reimport')
const dialogTitle = computed(() => isReimport.value ? '重新导入获奖名单' : '新建获奖公示')

const form = ref({
  competitionSeriesId: undefined,
  competitionName: undefined,
  file: null,
  importType: 'addition'
})

const rules = {
  competitionSeriesId: [
    { required: true, message: '请选择赛事', trigger: 'change' }
  ],
  file: [
    { required: true, message: '请选择文件', trigger: 'change' }
  ],
  importType: [
    { required: true, message: '请选择导入方式', trigger: 'change' }
  ]
}

/** 加载赛事选项 */
function loadCompetitionOptions() {
  getSelectCompetitionList().then(response => {
    if (response.code === 200) {
      const data = response.data || []
      competitionOptions.value = data.map(item => ({
        competitionSeriesId: item.competitionSeriesId,
        competitionName: item.competitionSeriesName + item.competitionName
      }))
    }
  })
}

function onCompetitionChange(val) {
  const selected = competitionOptions.value.find(item => item.competitionSeriesId === val)
  form.value.competitionName = selected ? selected.competitionName : undefined
}

/** 下载导入模板 */
function handleDownloadTemplate() {
  downloadJS(import.meta.env.VITE_APP_BASE_API + '/file/excel/download', '获奖公示导入模板.xlsx', 'addName')
}

/** 上传前钩子 */
function beforeUpload(file) {
  fileList.value = []
  form.value.file = null
  const fileSuffix = file?.name.split('.').pop().toLowerCase()
  if (!['xlsx', 'xls'].includes(fileSuffix)) {
    modal.msgWarning('上传文件格式不正确，仅支持 xlsx、xls 格式')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    modal.msgWarning('文件大小不能超过 10MB')
    return false
  }
  return true
}

/** 自定义上传 */
function httpRequest({ file }) {
  fileList.value = [file]
  form.value.file = file
  if (formRef.value) {
    formRef.value.validateField('file')
  }
  nextTick(() => {
    const input = uploadRef.value?.$el?.querySelector('input[type="file"]')
    if (input) input.value = ''
  })
}

/** 打开弹框 */
function openDialog(row) {
  visible.value = true
  currentRow.value = row || null
  resetForm()
  if (!isReimport.value) {
    loadCompetitionOptions()
  }
}

/** 重置表单 */
function resetForm() {
  form.value = {
    competitionSeriesId: undefined,
    competitionName: undefined,
    file: null,
    importType: 'addition'
  }
  fileList.value = []
  nextTick(() => {
    if (formRef.value) {
      formRef.value.resetFields()
    }
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
  })
}

/** 取消 */
function cancel() {
  visible.value = false
  resetForm()
}

/** 提交表单 */
function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return

    submitLoading.value = true
    const formData = new FormData()
    formData.append('file', form.value.file)
    formData.append('importType', form.value.importType)

    if (isReimport.value) {
      formData.append('awardPublicityId', currentRow.value.id)
    } else {
      formData.append('competitionSeriesId', form.value.competitionSeriesId)
      formData.append('competitionName', form.value.competitionName)
    }

    importAwardPublicity(formData).then(handleResponse).finally(() => {
      submitLoading.value = false
    })
  })
}

function handleResponse(response) {
  if (response.code === 200) {
    const data = response.data || {}
    importResult.value = {
      successCount: data.successCount ?? 0,
      failTeamCodes: data.failTeamCodes || [],
      importSuccess: data.importSuccess !== false,
      msg: data.msg || ''
    }
    resultVisible.value = true
    if (importResult.value.importSuccess) {
      visible.value = false
      emit('success')
    }
  } else {
    modal.msgWarning(response.msg || '操作失败')
  }
}

function closeResult() {
  resultVisible.value = false
}

defineExpose({
  openDialog
})
</script>

<style scoped lang="scss">
.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.import-rules {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1.8;
  margin-left: 100px;

  .rules-title {
    font-weight: bold;
    margin-bottom: 4px;
    font-size: 14px;
  }
}

.result-container {
  padding: 8px 0;
}

.result-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.result-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.result-summary {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 10px;
}

.result-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 0;

  & + & {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed #dcdfe6;
  }
}

.result-item--success {
  color: #67c23a;
  align-items: center;

  strong {
    color: #67c23a;
    font-size: 16px;
  }
}

.result-item--fail {
  color: #f56c6c;
}

.fail-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fail-label {
  color: #606266;
  font-size: 13px;
}

.fail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 120px;
  overflow-y: auto;
}

.fail-tag {
  margin: 0;
}
</style>
