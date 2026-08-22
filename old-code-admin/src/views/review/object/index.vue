<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="96px">
      <el-form-item label="评审活动" prop="activityId">
        <el-select v-model="queryParams.activityId" placeholder="请选择活动" clearable filterable style="width: 240px">
          <el-option v-for="item in activityOptions" :key="item.id" :label="item.activityName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="对象名称" prop="objectName">
        <el-input v-model.trim="queryParams.objectName" placeholder="请输入对象名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="对象编号" prop="objectCode">
        <el-input v-model.trim="queryParams.objectCode" placeholder="请输入对象编号" clearable style="width: 170px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="提交状态" prop="submitStatus">
        <el-select v-model="queryParams.submitStatus" placeholder="请选择" clearable style="width: 140px">
          <el-option v-for="item in objectStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源类型" prop="sourceBizType">
        <el-select v-model="queryParams.sourceBizType" placeholder="请选择" clearable style="width: 190px">
          <el-option label="TEAM" value="TEAM" />
          <el-option label="REGISTRATION" value="REGISTRATION" />
          <el-option label="REGISTRATION_TEAM_CODE" value="REGISTRATION_TEAM_CODE" />
          <el-option label="FILE_UPLOAD_MANAGER" value="FILE_UPLOAD_MANAGER" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源团队" prop="sourceTeamId">
        <el-input v-model.trim="queryParams.sourceTeamId" placeholder="团队编号" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="来源报名" prop="sourceRegistrationId">
        <el-input v-model.trim="queryParams.sourceRegistrationId" placeholder="报名ID" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="所属单位" prop="orgName">
        <el-input v-model.trim="queryParams.orgName" placeholder="请输入单位" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="关键词" prop="keywords">
        <el-input v-model.trim="queryParams.keywords" placeholder="请输入关键词" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="success" plain icon="Upload" @click="goImport" v-hasPermi="['competition:review:object:import']">导入评审对象</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Refresh" @click="openSyncDialog" v-hasPermi="['competition:review:object:import']">同步文件任务材料</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="objectList" stripe>
      <el-table-column label="对象编号" prop="objectCode" min-width="140" show-overflow-tooltip />
      <el-table-column label="项目名称" prop="objectName" min-width="200" show-overflow-tooltip />
      <el-table-column label="活动名称" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ activityName(row.activityId) }}</template>
      </el-table-column>
      <el-table-column label="对象类型" prop="objectType" width="100" />
      <el-table-column label="所属单位" prop="orgName" min-width="160" show-overflow-tooltip />
      <el-table-column label="负责人" prop="contactName" width="110" show-overflow-tooltip />
      <el-table-column label="学科代码" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ [row.subjectCode1, row.subjectCode2, row.subjectCode3].filter(Boolean).join(' / ') || '-' }}</template>
      </el-table-column>
      <el-table-column label="分类字段" prop="categoryCodes" min-width="170" show-overflow-tooltip />
      <el-table-column label="关键词" prop="keywords" min-width="150" show-overflow-tooltip />
      <el-table-column label="提交状态" prop="submitStatus" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.submitStatus)">{{ optionLabel(objectStatusOptions, row.submitStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="来源类型" prop="sourceBizType" width="120" />
      <el-table-column label="来源团队ID" prop="sourceTeamId" min-width="130" show-overflow-tooltip />
      <el-table-column label="来源报名ID" prop="sourceRegistrationId" min-width="130" show-overflow-tooltip />
      <el-table-column label="创建来源" prop="createdFrom" width="140" />
      <el-table-column label="提交时间" prop="submitTime" width="170" />
      <el-table-column label="操作" width="230" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="goDetail(row)" v-hasPermi="['competition:review:object:query']">详情</el-button>
          <el-button
            v-if="row.submitStatus === 'WITHDRAW_REQUESTED'"
            link
            type="success"
            icon="Check"
            @click="handleApprove(row)"
            v-hasPermi="['competition:review:submission:approve']"
          >通过撤回</el-button>
          <el-button
            v-if="row.submitStatus === 'WITHDRAW_REQUESTED'"
            link
            type="danger"
            icon="Close"
            @click="handleReject(row)"
            v-hasPermi="['competition:review:submission:approve']"
          >驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog title="同步文件任务材料" v-model="syncDialogOpen" width="560px" append-to-body>
      <el-form ref="syncFormRef" :model="syncForm" :rules="syncRules" label-width="118px">
        <el-form-item label="评审活动" prop="activityId">
          <el-select v-model="syncForm.activityId" placeholder="请选择活动" filterable style="width: 100%">
            <el-option v-for="item in activityOptions" :key="item.id" :label="item.activityName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件任务" prop="fileTaskId">
          <el-select v-model="syncForm.fileTaskId" placeholder="请选择文件任务" filterable style="width: 100%">
            <el-option
              v-for="item in fileTaskOptions"
              :key="item.id"
              :label="`${item.taskName || '-'}（${item.id}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="只同步已提交">
          <el-switch v-model="syncForm.submittedOnly" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="同链接材料">
          <el-select v-model="syncForm.materialOverwriteMode" clearable placeholder="保留已有" style="width: 100%">
            <el-option label="保留已有" value="" />
            <el-option label="覆盖同步" value="REPLACE_BY_SOURCE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="syncDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="syncLoading" @click="handleSyncMaterials">同步</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import RightToolbar from '@/components/RightToolbar'
import { listReviewActivity } from '@/api/review/activity'
import { listReviewObject, syncFileTaskMaterials } from '@/api/review/object'
import { approveWithdraw, rejectWithdraw } from '@/api/review/submission'
import { getLists as listFileTask } from '@/api/fileTask/task'

const route = useRoute()
const router = useRouter()
const showSearch = ref(true)
const loading = ref(false)
const total = ref(0)
const objectList = ref([])
const activityOptions = ref([])
const fileTaskOptions = ref([])
const queryRef = ref(null)
const syncFormRef = ref(null)
const syncDialogOpen = ref(false)
const syncLoading = ref(false)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  activityId: undefined,
  objectName: '',
  objectCode: '',
  submitStatus: '',
  sourceBizType: '',
  sourceTeamId: '',
  sourceRegistrationId: '',
  keywords: '',
  orgName: ''
})

const syncForm = reactive({
  activityId: undefined,
  fileTaskId: undefined,
  submittedOnly: true,
  materialOverwriteMode: ''
})

const syncRules = {
  activityId: [{ required: true, message: '请选择评审活动', trigger: 'change' }],
  fileTaskId: [{ required: true, message: '请选择文件任务', trigger: 'change' }]
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

function loadActivities() {
  listReviewActivity({ pageNum: 1, pageSize: 200 }).then(res => {
    activityOptions.value = res.rows || []
  })
}

function loadFileTasks() {
  listFileTask({ pageNum: 1, pageSize: 200 }).then(res => {
    fileTaskOptions.value = res.rows || []
  })
}

function getList() {
  loading.value = true
  listReviewObject(queryParams).then(res => {
    objectList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function goImport() {
  router.push({ path: '/review/import', query: queryParams.activityId ? { activityId: queryParams.activityId } : {} })
}

function openSyncDialog() {
  syncForm.activityId = queryParams.activityId || undefined
  syncForm.fileTaskId = undefined
  syncForm.submittedOnly = true
  syncForm.materialOverwriteMode = ''
  if (fileTaskOptions.value.length === 0) {
    loadFileTasks()
  }
  syncDialogOpen.value = true
  syncFormRef.value?.clearValidate()
}

function handleSyncMaterials() {
  syncFormRef.value?.validate(valid => {
    if (!valid) return
    syncLoading.value = true
    syncFileTaskMaterials({
      activityId: syncForm.activityId,
      fileTaskId: syncForm.fileTaskId,
      submittedOnly: syncForm.submittedOnly,
      materialOverwriteMode: syncForm.materialOverwriteMode || undefined
    }).then(res => {
      const result = res.data || {}
      ElMessage.success(result.message || '同步完成')
      syncDialogOpen.value = false
      getList()
    }).finally(() => {
      syncLoading.value = false
    })
  })
}

function goDetail(row) {
  router.push({ path: `/review/object-detail/index/${row.id}` })
}

function handleApprove(row) {
  ElMessageBox.prompt('请输入审批意见', '通过撤回申请', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(({ value }) => {
    return approveWithdraw(row.id, { actionReason: value })
  }).then(() => {
    ElMessage.success('已通过撤回申请')
    getList()
  }).catch(() => {})
}

function handleReject(row) {
  ElMessageBox.prompt('请输入驳回原因', '驳回撤回申请', {
    confirmButtonText: '驳回',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(({ value }) => {
    return rejectWithdraw(row.id, { actionReason: value })
  }).then(() => {
    ElMessage.success('已驳回撤回申请')
    getList()
  }).catch(() => {})
}

function activityName(activityId) {
  const item = activityOptions.value.find(activity => activity.id === activityId)
  return item ? item.activityName : (activityId || '-')
}

function optionLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function statusTag(status) {
  const item = objectStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

onMounted(() => {
  if (route.query.activityId) {
    queryParams.activityId = Number(route.query.activityId)
  }
  loadActivities()
  loadFileTasks()
  getList()
})
</script>
