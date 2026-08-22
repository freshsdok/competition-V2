<template>
  <div class="app-container">
    <el-page-header @back="goBack">
      <template #content>
        <span>{{ form.objectName || '评审填报详情' }}</span>
      </template>
      <template #extra>
        <el-button icon="Refresh" @click="loadDetail">刷新</el-button>
        <el-button v-if="detail.editable" type="primary" icon="DocumentChecked" :loading="saveLoading" @click="handleSave" v-hasPermi="['competition:review:submission:edit']">保存草稿</el-button>
        <el-button v-if="detail.submittable" type="success" icon="Check" :loading="submitLoading" @click="handleSubmit" v-hasPermi="['competition:review:submission:submit']">提交资料</el-button>
        <el-button v-if="detail.withdrawable" type="warning" icon="RefreshLeft" @click="handleWithdraw" v-hasPermi="['competition:review:submission:withdraw']">申请撤回</el-button>
      </template>
    </el-page-header>

    <el-alert v-if="detail.warningMessage" :title="detail.warningMessage" type="warning" show-icon class="mt16" />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="mt16" :disabled="!detail.editable">
      <el-divider content-position="left">基本信息</el-divider>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="项目名称" prop="objectName">
            <el-input v-model.trim="form.objectName" maxlength="300" placeholder="请输入项目名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属单位">
            <el-input v-model.trim="form.orgName" maxlength="200" placeholder="请输入所属单位" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系人">
            <el-input v-model.trim="form.contactName" maxlength="100" placeholder="请输入联系人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系方式" prop="contactPhone">
            <el-input v-model.trim="form.contactPhone" maxlength="100" placeholder="请输入联系方式" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系邮箱">
            <el-input v-model.trim="form.contactEmail" maxlength="200" placeholder="请输入联系邮箱" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="项目摘要" prop="summary">
            <el-input v-model="form.summary" type="textarea" :rows="4" maxlength="4000" show-word-limit placeholder="请输入项目摘要" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">分类与关键词</el-divider>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="学科代码1">
            <el-input v-model.trim="form.subjectCode1" maxlength="100" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="学科代码2">
            <el-input v-model.trim="form.subjectCode2" maxlength="100" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="学科代码3">
            <el-input v-model.trim="form.subjectCode3" maxlength="100" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分类字段">
            <el-input v-model="form.categoryCodes" type="textarea" :rows="2" placeholder="可填写JSON或逗号分隔分类" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="关键词">
            <el-input v-model="form.keywords" type="textarea" :rows="2" placeholder="可填写JSON或逗号分隔关键词" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-divider content-position="left">人员信息</el-divider>
    <el-table :data="members" stripe>
      <el-table-column label="姓名" prop="memberName" min-width="120" show-overflow-tooltip />
      <el-table-column label="角色" width="110">
        <template #default="{ row }">{{ optionLabel(memberRoleOptions, row.memberRole) }}</template>
      </el-table-column>
      <el-table-column label="是否负责人" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.isPrimary === 'Y' ? 'success' : 'info'">{{ row.isPrimary === 'Y' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="手机号" prop="phone" min-width="130" show-overflow-tooltip />
      <el-table-column label="邮箱" prop="email" min-width="160" show-overflow-tooltip />
      <el-table-column label="所属单位" prop="orgName" min-width="160" show-overflow-tooltip />
      <el-table-column label="证件编号" prop="certificateCode" min-width="160" show-overflow-tooltip />
    </el-table>

    <el-divider content-position="left">附件材料</el-divider>
    <el-form :model="materialForm" :inline="true" label-width="88px" v-if="detail.editable">
      <el-form-item label="材料类型">
        <el-select v-model="materialForm.materialType" style="width: 150px">
          <el-option v-for="item in materialTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="材料名称">
        <el-input v-model.trim="materialForm.materialName" placeholder="默认使用文件名" clearable style="width: 220px" />
      </el-form-item>
      <el-form-item>
        <el-upload
          :action="uploadFileUrl"
          :headers="headers"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
        >
          <el-button type="primary" icon="Upload" :loading="materialLoading">上传材料</el-button>
        </el-upload>
      </el-form-item>
    </el-form>
    <el-table :data="materials" stripe>
      <el-table-column label="材料名称" prop="materialName" min-width="180" show-overflow-tooltip />
      <el-table-column label="材料类型" width="120">
        <template #default="{ row }">{{ optionLabel(materialTypeOptions, row.materialType) }}</template>
      </el-table-column>
      <el-table-column label="文件名" prop="fileName" min-width="220" show-overflow-tooltip />
      <el-table-column label="上传时间" prop="uploadTime" width="170" />
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'NORMAL' ? 'success' : 'info'">{{ row.status || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" icon="Download" @click="handleDownload(row)">查看/下载</el-button>
          <el-button v-if="detail.editable" link type="danger" icon="Delete" @click="handleDeleteMaterial(row)" v-hasPermi="['competition:review:submission:edit']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-divider content-position="left">提交操作</el-divider>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="当前状态">
        <el-tag :type="statusTag(form.submitStatus)">{{ optionLabel(objectStatusOptions, form.submitStatus) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="提交时间">{{ form.submitTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="是否可编辑">{{ detail.editable ? '是' : '否' }}</el-descriptions-item>
      <el-descriptions-item label="是否可撤回">{{ detail.withdrawable ? '是' : '否' }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken } from '@/utils/auth'
import { replaceFileOrigin } from '@/utils/fileOrigin'
import {
  addSubmissionMaterial,
  deleteSubmissionMaterial,
  getSubmissionDetail,
  saveSubmissionDraft,
  submitSubmission,
  withdrawSubmission
} from '@/api/review/submission'

const route = useRoute()
const router = useRouter()
const objectId = computed(() => Number(route.params.id))
const formRef = ref(null)
const saveLoading = ref(false)
const submitLoading = ref(false)
const materialLoading = ref(false)
const detail = reactive({
  editable: false,
  submittable: false,
  withdrawable: false,
  warningMessage: ''
})
const form = reactive(defaultForm())
const members = ref([])
const materials = ref([])
const materialForm = reactive({
  materialType: 'DECLARATION',
  materialName: ''
})
const uploadFileUrl = ref(import.meta.env.VITE_APP_BASE_API + '/file/upload')
const headers = ref({ Authorization: 'Bearer ' + getToken() })

const rules = {
  objectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
  summary: [{ required: true, message: '项目摘要不能为空', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '联系方式不能为空', trigger: 'blur' }]
}

const objectStatusOptions = [
  { label: '草稿', value: 'DRAFT', type: 'info' },
  { label: '已提交', value: 'SUBMITTED', type: 'success' },
  { label: '申请撤回', value: 'WITHDRAW_REQUESTED', type: 'warning' },
  { label: '撤回通过', value: 'WITHDRAW_APPROVED', type: 'info' },
  { label: '撤回驳回', value: 'WITHDRAW_REJECTED', type: 'danger' },
  { label: '已锁定', value: 'LOCKED', type: 'primary' },
  { label: '已作废', value: 'INVALID', type: 'danger' },
  { label: '评审中', value: 'REVIEWING', type: 'warning' },
  { label: '已评审', value: 'REVIEWED', type: 'success' },
  { label: '已归档', value: 'ARCHIVED', type: 'info' }
]
const memberRoleOptions = [
  { label: '负责人', value: 'LEADER' },
  { label: '成员', value: 'MEMBER' },
  { label: '联系人', value: 'CONTACT' },
  { label: '指导教师', value: 'TEACHER' },
  { label: '其他', value: 'OTHER' }
]
const materialTypeOptions = [
  { label: '申报书', value: 'DECLARATION' },
  { label: 'PPT', value: 'PPT' },
  { label: 'PDF', value: 'PDF' },
  { label: '图片', value: 'IMAGE' },
  { label: '视频', value: 'VIDEO' },
  { label: 'Word文档', value: 'DOC' },
  { label: '压缩包', value: 'ZIP' },
  { label: '其他', value: 'OTHER' }
]

function defaultForm() {
  return {
    objectName: '',
    summary: '',
    orgName: '',
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    subjectCode1: '',
    subjectCode2: '',
    subjectCode3: '',
    categoryCodes: '',
    keywords: '',
    extraData: '',
    submitStatus: '',
    submitTime: ''
  }
}

function loadDetail() {
  getSubmissionDetail(objectId.value).then(res => {
    Object.assign(detail, {
      editable: false,
      submittable: false,
      withdrawable: false,
      warningMessage: ''
    }, res.data || {})
    Object.assign(form, defaultForm(), detail.object || {})
    members.value = detail.members || []
    materials.value = detail.materials || []
  })
}

function handleSave() {
  formRef.value.validate(valid => {
    if (!valid) return
    saveLoading.value = true
    saveSubmissionDraft(objectId.value, form).then(() => {
      ElMessage.success('草稿已保存')
      loadDetail()
    }).finally(() => {
      saveLoading.value = false
    })
  })
}

function handleSubmit() {
  formRef.value.validate(valid => {
    if (!valid) return
    ElMessageBox.confirm('确认提交评审资料？提交后材料将锁定，撤回需管理员审批。', '提交确认', {
      type: 'warning'
    }).then(() => {
      submitLoading.value = true
      const saveRequest = detail.editable ? saveSubmissionDraft(objectId.value, form) : Promise.resolve()
      return saveRequest.then(() => submitSubmission(objectId.value))
    }).then(() => {
      ElMessage.success('提交成功')
      loadDetail()
    }).finally(() => {
      submitLoading.value = false
    }).catch(() => {
      submitLoading.value = false
    })
  })
}

function handleWithdraw() {
  ElMessageBox.prompt('请输入撤回原因', '申请撤回', {
    confirmButtonText: '提交申请',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(({ value }) => {
    return withdrawSubmission(objectId.value, { actionReason: value })
  }).then(() => {
    ElMessage.success('撤回申请已提交')
    loadDetail()
  }).catch(() => {})
}

function beforeUpload(file) {
  const limitMb = 500
  if (file.size / 1024 / 1024 > limitMb) {
    ElMessage.error(`上传文件大小不能超过 ${limitMb} MB`)
    return false
  }
  materialLoading.value = true
  return true
}

function handleUploadSuccess(res, file) {
  res = replaceFileOrigin(res)
  const fileUrl = res?.data?.url || res?.data
  const fileName = res?.data?.name || file.name
  if (!fileUrl) {
    materialLoading.value = false
    ElMessage.error('上传结果缺少文件地址')
    return
  }
  const ext = fileName && fileName.includes('.') ? fileName.substring(fileName.lastIndexOf('.') + 1) : ''
  addSubmissionMaterial(objectId.value, {
    materialName: materialForm.materialName || fileName,
    materialType: materialForm.materialType,
    fileName,
    fileUrl,
    fileSize: file.size,
    mimeType: file.raw?.type || '',
    fileExt: ext,
    visibleToReviewer: 'Y'
  }).then(() => {
    ElMessage.success('材料已上传')
    materialForm.materialName = ''
    loadDetail()
  }).finally(() => {
    materialLoading.value = false
  })
}

function handleUploadError() {
  materialLoading.value = false
  ElMessage.error('上传材料失败')
}

function handleDeleteMaterial(row) {
  ElMessageBox.confirm('确认删除该材料？', '删除确认', {
    type: 'warning'
  }).then(() => {
    return deleteSubmissionMaterial(row.id)
  }).then(() => {
    ElMessage.success('删除成功')
    loadDetail()
  }).catch(() => {})
}

function handleDownload(row) {
  if (row.fileUrl) {
    window.open(row.fileUrl, '_blank')
  }
}

function goBack() {
  router.back()
}

function optionLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function statusTag(status) {
  const item = objectStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

onMounted(loadDetail)
</script>

<style scoped>
.mt16 {
  margin-top: 16px;
}
</style>
