<template>
  <el-dialog
    :title="dialogTitle"
    v-model="visible"
    width="700px"
    append-to-body
    :close-on-click-modal="false"
    @close="cancel"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="150px"
      width="600px"
    >
      <el-form-item label="比赛名称" v-if="isReimport">
        <span>{{ currentRow?.competitionName }}</span>
      </el-form-item>
      <el-form-item label="选择赛事" prop="competitionSeriesId" v-else >
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
      <el-form-item label="下载导入模板" width="100%">
        <el-link :underline="false" @click="handleDownloadTemplate" type="primary">赛事晋级报名模板.xlsx</el-link>
        <el-link :underline="false" @click="handleDownloadTemplate" type="primary" style="margin-left: 10px">下载</el-link>
      </el-form-item>
      <el-form-item label="导入晋级名单Excel" prop="file" width="100%">
        <el-upload
          ref="uploadRef"
          v-model:file-list="fileList"
          action="#"
          width="100%"
          :before-upload="beforeUpload"
          :http-request="httpRequest"
        >
          <el-button type="primary" :icon="Upload">
            {{ fileList.length > 0 ? '重新选择' : '选择文件' }}
          </el-button>
          <template #tip>
            <div class="upload-tip">
              Excel需包含：团队编号 字段
            </div>
          </template>
        </el-upload>
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
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { importPromotion } from '@/api/tournament/promote'
import { getSelectCompetitionList } from '@/api/certInterconnect/certConfig'
import modal from '@/plugins/modal'
import { Upload } from '@element-plus/icons-vue'
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

const isReimport = computed(() => props.mode === 'reimport')
const dialogTitle = computed(() => isReimport.value ? '重新导入晋级名单' : '新建晋级赛事')

const form = ref({
  competitionSeriesId: undefined,
  competitionName: undefined,
  file: null
})

const rules = {
  competitionSeriesId: [
    { required: true, message: '请选择赛事', trigger: 'change' }
  ],
  file: [
    { required: true, message: '请选择文件', trigger: 'change' }
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
    file: null
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

    if (isReimport.value) {
      formData.append('competitionSeriesId', currentRow.value.competitionSeriesId)
    } else {
      formData.append('competitionSeriesId', form.value.competitionSeriesId)
    }

    importPromotion(formData).then(handleResponse).finally(() => {
      submitLoading.value = false
    })
  })
}

function handleResponse(response) {
  if (response.code === 200) {
    modal.msgSuccess(response.msg || '导入成功')
    visible.value = false
    emit('success')
  } else {
    modal.msgWarning(response.msg || '操作失败')
  }
}

/** 下载导入模板 */
function handleDownloadTemplate() {
  downloadJS(import.meta.env.VITE_APP_BASE_API + '/file/excel/download', '赛事晋级报名模板.xlsx', 'addName')
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
  width: 500px;
}
</style>
