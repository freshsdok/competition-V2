<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="96px">
      <el-form-item label="提交状态" prop="submitStatus">
        <el-select v-model="queryParams.submitStatus" placeholder="请选择" clearable style="width: 160px">
          <el-option v-for="item in objectStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="filteredList" stripe>
      <el-table-column label="评审活动" prop="activityName" min-width="180" show-overflow-tooltip />
      <el-table-column label="评审对象编号" prop="objectCode" min-width="140" show-overflow-tooltip />
      <el-table-column label="项目名称" prop="objectName" min-width="220" show-overflow-tooltip />
      <el-table-column label="所属单位" prop="orgName" min-width="160" show-overflow-tooltip />
      <el-table-column label="提交状态" prop="submitStatus" width="130" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.submitStatus)">{{ optionLabel(objectStatusOptions, row.submitStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="填报截止时间" prop="submitDeadline" width="170" />
      <el-table-column label="是否可编辑" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.editable ? 'success' : 'info'">{{ row.editable ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否可撤回" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.withdrawable ? 'warning' : 'info'">{{ row.withdrawable ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后更新时间" prop="lastUpdateTime" width="170" />
      <el-table-column label="提示" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ statusHint(row) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            v-if="row.editable"
            link
            type="primary"
            icon="Edit"
            @click="goDetail(row)"
            v-hasPermi="['competition:review:submission:query']"
          >
            {{ row.submitStatus === 'WITHDRAW_APPROVED' ? '重新填写' : '填写/编辑' }}
          </el-button>
          <el-button
            v-else
            link
            type="primary"
            icon="View"
            @click="goDetail(row)"
            v-hasPermi="['competition:review:submission:query']"
          >
            查看
          </el-button>
          <el-button
            v-if="canSubmit(row)"
            link
            type="success"
            icon="Check"
            @click="handleSubmit(row)"
            v-hasPermi="['competition:review:submission:submit']"
          >
            {{ row.submitStatus === 'WITHDRAW_APPROVED' ? '重新提交' : '提交' }}
          </el-button>
          <el-button
            v-if="row.withdrawable"
            link
            type="warning"
            icon="RefreshLeft"
            @click="handleWithdraw(row)"
            v-hasPermi="['competition:review:submission:withdraw']"
          >
            申请撤回
          </el-button>
          <el-button
            v-if="canViewResult(row)"
            link
            type="success"
            icon="Medal"
            @click="handleViewResult(row)"
            v-hasPermi="['competition:review:submission:query']"
          >
            查看结果
          </el-button>
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

    <el-dialog v-model="resultDialogVisible" title="评审结果" width="560px" append-to-body>
      <el-skeleton v-if="resultLoading" :rows="5" animated />
      <el-descriptions v-else :column="1" border>
        <el-descriptions-item label="评审活动">{{ submissionResult.activityName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评审轮次">{{ submissionResult.roundName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ submissionResult.objectName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="系统计算分">{{ submissionResult.calculatedScore ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="排名">{{ submissionResult.calculatedRank ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="评价结论">{{ submissionResult.evaluationConclusion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ submissionResult.publishedTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import RightToolbar from '@/components/RightToolbar'
import { getSubmissionResult, listMySubmission, submitSubmission, withdrawSubmission } from '@/api/review/submission'

const router = useRouter()
const showSearch = ref(true)
const loading = ref(false)
const total = ref(0)
const taskList = ref([])
const queryRef = ref(null)
const resultDialogVisible = ref(false)
const resultLoading = ref(false)
const submissionResult = ref({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  submitStatus: ''
})

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

const filteredList = computed(() => {
  if (!queryParams.submitStatus) {
    return taskList.value
  }
  return taskList.value.filter(item => item.submitStatus === queryParams.submitStatus)
})

function getList() {
  loading.value = true
  listMySubmission(queryParams).then(res => {
    taskList.value = res.rows || []
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

function goDetail(row) {
  router.push({ path: `/review/my-submission-detail/index/${row.objectId}` })
}

function canSubmit(row) {
  return row.editable && ['DRAFT', 'WITHDRAW_APPROVED'].includes(row.submitStatus)
}

function canViewResult(row) {
  return ['LOCKED', 'REVIEWING', 'REVIEWED', 'ARCHIVED'].includes(row.submitStatus)
}

function handleSubmit(row) {
  ElMessageBox.confirm('确认提交评审资料？提交后将锁定材料，撤回需管理员审批。', '提交确认', {
    type: 'warning'
  }).then(() => {
    return submitSubmission(row.objectId)
  }).then(() => {
    ElMessage.success('提交成功')
    getList()
  }).catch(() => {})
}

function handleWithdraw(row) {
  ElMessageBox.prompt('请输入撤回原因', '申请撤回', {
    confirmButtonText: '提交申请',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(({ value }) => {
    return withdrawSubmission(row.objectId, { actionReason: value })
  }).then(() => {
    ElMessage.success('撤回申请已提交')
    getList()
  }).catch(() => {})
}

function handleViewResult(row) {
  resultDialogVisible.value = true
  resultLoading.value = true
  submissionResult.value = {}
  getSubmissionResult(row.objectId).then(res => {
    submissionResult.value = res.data || {}
  }).catch(() => {
    resultDialogVisible.value = false
  }).finally(() => {
    resultLoading.value = false
  })
}

function statusHint(row) {
  if (row.submitStatus === 'WITHDRAW_REQUESTED') {
    return '撤回申请审核中'
  }
  if (row.submitStatus === 'INVALID') {
    return '已作废'
  }
  if (row.submitStatus === 'LOCKED') {
    return '已锁定'
  }
  return '-'
}

function optionLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function statusTag(status) {
  const item = objectStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

onMounted(getList)
</script>
