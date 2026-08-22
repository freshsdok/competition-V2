<template>
  <div class="app-container review-result-page">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="96px">
      <el-form-item label="评审活动" prop="activityId">
        <el-select v-model="queryParams.activityId" placeholder="请选择活动" clearable filterable style="width: 240px" @change="handleActivityChange">
          <el-option v-for="item in activityOptions" :key="item.id" :label="item.activityName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="评审轮次" prop="roundId">
        <el-select v-model="queryParams.roundId" placeholder="请选择轮次" clearable filterable style="width: 200px">
          <el-option v-for="item in roundOptions" :key="item.id" :label="item.roundName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="对象编号" prop="objectCode">
        <el-input v-model.trim="queryParams.objectCode" placeholder="请输入对象编号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目名称" prop="objectName">
        <el-input v-model.trim="queryParams.objectName" placeholder="请输入项目名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="所属单位" prop="orgName">
        <el-input v-model.trim="queryParams.orgName" placeholder="请输入单位" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="结果状态" prop="resultStatus">
        <el-select v-model="queryParams.resultStatus" placeholder="请选择" clearable style="width: 150px">
          <el-option v-for="item in resultStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="完成度" prop="completionStatus">
        <el-select v-model="queryParams.completionStatus" placeholder="请选择" clearable style="width: 150px">
          <el-option v-for="item in completionStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Cpu" @click="handleGenerate()" v-hasPermi="['competition:review:result:generate']">
          生成/重算结果
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Refresh" :disabled="selection.length === 0" @click="handleGenerateSelected" v-hasPermi="['competition:review:result:generate']">
          生成选中对象
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-alert
      title="系统计算分仅由已提交专家评分汇总生成；评价结论用于发布说明，不是人工改分入口。"
      type="info"
      show-icon
      class="mb8"
    />

    <el-table v-loading="loading" :data="resultList" stripe @selection-change="selection = $event">
      <el-table-column type="selection" width="50" />
      <el-table-column label="评审活动" prop="activityName" min-width="180" show-overflow-tooltip />
      <el-table-column label="评审轮次" prop="roundName" min-width="140" show-overflow-tooltip />
      <el-table-column label="对象编号" prop="objectCode" min-width="130" show-overflow-tooltip />
      <el-table-column label="项目名称" prop="objectName" min-width="210" show-overflow-tooltip />
      <el-table-column label="所属单位" prop="orgName" min-width="160" show-overflow-tooltip />
      <el-table-column label="评分完成" width="130" align="center">
        <template #default="{ row }">
          <el-tag :type="completionTag(row.completionStatus)">{{ row.completionText || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="完成状态" width="120" align="center">
        <template #default="{ row }">{{ optionLabel(completionStatusOptions, row.completionStatus) }}</template>
      </el-table-column>
      <el-table-column label="系统计算分" prop="calculatedScore" width="120" align="center" />
      <el-table-column label="排名" prop="calculatedRank" width="80" align="center" />
      <el-table-column label="评价结论" prop="evaluationConclusion" min-width="180" show-overflow-tooltip />
      <el-table-column label="结果状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="resultStatusTag(row.resultStatus)">{{ optionLabel(resultStatusOptions, row.resultStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="生成时间" prop="generatedTime" width="170" />
      <el-table-column label="发布时间" prop="publishedTime" width="170" />
      <el-table-column label="操作" width="320" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" icon="Document" @click="openRecords(row)" v-hasPermi="['competition:review:result:record']">评分记录</el-button>
          <el-button link type="success" icon="EditPen" :disabled="!row.resultId" @click="openConclusion(row)" v-hasPermi="['competition:review:result:edit']">结论</el-button>
          <el-button link type="warning" icon="Cpu" :disabled="row.resultStatus === 'PUBLISHED'" @click="handleGenerate(row)" v-hasPermi="['competition:review:result:generate']">重算</el-button>
          <el-button v-if="row.resultStatus !== 'PUBLISHED'" link type="success" icon="Promotion" :disabled="!row.resultId" @click="handlePublish(row)" v-hasPermi="['competition:review:result:publish']">发布</el-button>
          <el-button v-else link type="danger" icon="RefreshLeft" @click="handleRevoke(row)" v-hasPermi="['competition:review:result:revoke']">撤回</el-button>
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

    <el-dialog v-model="recordDialogVisible" title="专家评分记录" width="960px" append-to-body>
      <el-table v-loading="recordLoading" :data="recordList" stripe>
        <el-table-column label="专家" prop="reviewerName" min-width="120" show-overflow-tooltip />
        <el-table-column label="用户ID" prop="reviewerUserId" width="100" />
        <el-table-column label="记录状态" prop="recordStatus" width="110" align="center" />
        <el-table-column label="总分" prop="totalScore" width="90" align="center" />
        <el-table-column label="推荐意见" prop="recommendation" min-width="120" show-overflow-tooltip />
        <el-table-column label="综合评语" prop="commentText" min-width="200" show-overflow-tooltip />
        <el-table-column label="提交时间" prop="submittedTime" width="170" />
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="openScoreDetails(row)">明细</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="评分明细快照" width="820px" append-to-body>
      <el-table v-loading="detailLoading" :data="scoreDetailList" stripe>
        <el-table-column label="指标名称" prop="criteriaName" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" prop="scoreType" width="120" />
        <el-table-column label="分值" prop="scoreValue" width="100" align="center" />
        <el-table-column label="选项" prop="optionValue" min-width="120" show-overflow-tooltip />
        <el-table-column label="文本评价" prop="textValue" min-width="180" show-overflow-tooltip />
        <el-table-column label="权重" prop="weight" width="100" align="center" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="conclusionDialogVisible" title="填写评价结论" width="560px" append-to-body>
      <el-form label-width="96px">
        <el-form-item label="项目名称">{{ currentRow?.objectName || '-' }}</el-form-item>
        <el-form-item label="系统分">{{ currentRow?.calculatedScore ?? '-' }}</el-form-item>
        <el-form-item label="评价结论">
          <el-input v-model="conclusionForm.evaluationConclusion" type="textarea" :rows="5" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="conclusionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConclusion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import RightToolbar from '@/components/RightToolbar'
import { listReviewActivity } from '@/api/review/activity'
import { listReviewRound } from '@/api/review/round'
import {
  generateReviewResult,
  listReviewResult,
  listReviewResultRecords,
  listReviewScoreDetails,
  publishReviewResult,
  revokeReviewResult,
  updateResultConclusion
} from '@/api/review/result'

const showSearch = ref(true)
const loading = ref(false)
const total = ref(0)
const resultList = ref([])
const selection = ref([])
const activityOptions = ref([])
const roundOptions = ref([])
const queryRef = ref(null)

const recordDialogVisible = ref(false)
const recordLoading = ref(false)
const recordList = ref([])
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const scoreDetailList = ref([])
const conclusionDialogVisible = ref(false)
const currentRow = ref(null)
const conclusionForm = reactive({ evaluationConclusion: '' })

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  activityId: undefined,
  roundId: undefined,
  objectCode: '',
  objectName: '',
  orgName: '',
  resultStatus: '',
  completionStatus: ''
})

const resultStatusOptions = [
  { label: '未生成', value: '', type: 'info' },
  { label: '已生成', value: 'GENERATED', type: 'warning' },
  { label: '已发布', value: 'PUBLISHED', type: 'success' },
  { label: '已撤回', value: 'REVOKED', type: 'danger' },
  { label: '已归档', value: 'ARCHIVED', type: 'info' }
]

const completionStatusOptions = [
  { label: '未开始', value: 'NOT_STARTED', type: 'info' },
  { label: '部分完成', value: 'PARTIAL', type: 'warning' },
  { label: '已完成', value: 'COMPLETED', type: 'success' }
]

function loadActivities() {
  listReviewActivity({ pageNum: 1, pageSize: 200 }).then(res => {
    activityOptions.value = res.rows || []
  })
}

function loadRounds(activityId) {
  if (!activityId) {
    roundOptions.value = []
    return
  }
  listReviewRound({ pageNum: 1, pageSize: 200, activityId }).then(res => {
    roundOptions.value = res.rows || []
  })
}

function handleActivityChange(value) {
  queryParams.roundId = undefined
  loadRounds(value)
}

function getList() {
  loading.value = true
  listReviewResult(queryParams).then(res => {
    resultList.value = res.rows || []
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
  roundOptions.value = []
  handleQuery()
}

function handleGenerate(row) {
  if (!row && (!queryParams.activityId || !queryParams.roundId)) {
    ElMessage.warning('请先选择评审活动和评审轮次')
    return
  }
  const objectIds = row ? [row.objectId] : undefined
  const payload = {
    activityId: row ? row.activityId : queryParams.activityId,
    roundId: row ? row.roundId : queryParams.roundId,
    objectIds
  }
  ElMessageBox.confirm('确认按已提交专家评分生成/重算结果？未完成评分的对象会返回警告。', '生成确认', {
    type: 'warning'
  }).then(() => generateReviewResult(payload)).then(res => {
    showGenerateResult(res.data)
    getList()
  }).catch(() => {})
}

function handleGenerateSelected() {
  if (selection.value.length === 0) {
    return
  }
  const first = selection.value[0]
  const sameRound = selection.value.every(item => item.activityId === first.activityId && item.roundId === first.roundId)
  if (!sameRound) {
    ElMessage.warning('请只选择同一活动、同一轮次的对象')
    return
  }
  const payload = {
    activityId: first.activityId,
    roundId: first.roundId,
    objectIds: selection.value.map(item => item.objectId)
  }
  ElMessageBox.confirm(`确认生成选中的 ${selection.value.length} 个对象结果？`, '生成确认', {
    type: 'warning'
  }).then(() => generateReviewResult(payload)).then(res => {
    showGenerateResult(res.data)
    getList()
  }).catch(() => {})
}

function showGenerateResult(data) {
  const message = `生成 ${data?.generatedCount || 0} 个，跳过 ${data?.skippedCount || 0} 个`
  if (data?.warnings && data.warnings.length > 0) {
    ElMessageBox.alert(`${message}\n\n${data.warnings.join('\n')}`, '生成结果提示', {
      type: 'warning'
    })
  } else {
    ElMessage.success(message)
  }
}

function openRecords(row) {
  currentRow.value = row
  recordDialogVisible.value = true
  recordLoading.value = true
  listReviewResultRecords({
    activityId: row.activityId,
    roundId: row.roundId,
    objectId: row.objectId
  }).then(res => {
    recordList.value = res.data || []
  }).finally(() => {
    recordLoading.value = false
  })
}

function openScoreDetails(row) {
  detailDialogVisible.value = true
  detailLoading.value = true
  listReviewScoreDetails(row.recordId).then(res => {
    scoreDetailList.value = res.data || []
  }).finally(() => {
    detailLoading.value = false
  })
}

function openConclusion(row) {
  currentRow.value = row
  conclusionForm.evaluationConclusion = row.evaluationConclusion || ''
  conclusionDialogVisible.value = true
}

function submitConclusion() {
  if (!currentRow.value?.resultId) {
    return
  }
  updateResultConclusion(currentRow.value.resultId, {
    evaluationConclusion: conclusionForm.evaluationConclusion
  }).then(() => {
    ElMessage.success('评价结论已保存')
    conclusionDialogVisible.value = false
    getList()
  })
}

function handlePublish(row) {
  ElMessageBox.prompt('请输入发布说明', '发布评审结果', {
    confirmButtonText: '发布',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(({ value }) => {
    return publishReviewResult(row.resultId, {
      publishScope: 'ALL',
      publishContent: value
    })
  }).then(() => {
    ElMessage.success('发布成功')
    getList()
  }).catch(() => {})
}

function handleRevoke(row) {
  ElMessageBox.prompt('请输入撤回原因', '撤回发布', {
    confirmButtonText: '撤回',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(({ value }) => {
    return revokeReviewResult(row.resultId, { revokeReason: value })
  }).then(() => {
    ElMessage.success('已撤回发布')
    getList()
  }).catch(() => {})
}

function optionLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function resultStatusTag(status) {
  const item = resultStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

function completionTag(status) {
  const item = completionStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

onMounted(() => {
  loadActivities()
  getList()
})
</script>

<style scoped>
.review-result-page :deep(.el-alert__content) {
  line-height: 1.4;
}
</style>
