<template>
  <div class="app-container review-assignment">
    <el-alert
      title="专家端“我的评审任务”来源于此处生成的评审任务分配记录。现场场次只负责当前对象切换，不会自动给专家生成评分任务。"
      type="info"
      show-icon
      :closable="false"
      class="mb12"
    />

    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="96px">
      <el-form-item label="评审活动" prop="activityId">
        <el-select v-model="queryParams.activityId" placeholder="请选择活动" clearable filterable style="width: 240px" @change="handleQueryActivityChange">
          <el-option v-for="item in activityOptions" :key="item.id" :label="item.activityName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="评审轮次" prop="roundId">
        <el-select v-model="queryParams.roundId" placeholder="请选择轮次" clearable filterable style="width: 220px">
          <el-option v-for="item in queryRoundOptions" :key="item.id" :label="formatRoundLabel(item)" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="评审对象ID" prop="objectId">
        <el-input v-model.trim="queryParams.objectId" placeholder="对象ID" clearable style="width: 130px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="专家用户ID" prop="reviewerUserId">
        <el-input v-model.trim="queryParams.reviewerUserId" placeholder="用户ID" clearable style="width: 130px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 160px">
          <el-option v-for="item in assignmentStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="openBatchDialog" v-hasPermi="['competition:review:assignment:add']">
          批量分配
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="assignmentList" stripe>
      <el-table-column label="任务ID" prop="id" width="90" />
      <el-table-column label="评审活动" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ activityName(row.activityId) }}</template>
      </el-table-column>
      <el-table-column label="评审轮次" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ roundName(row.roundId) }}</template>
      </el-table-column>
      <el-table-column label="评审对象" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <span>{{ objectName(row.objectId) }}</span>
          <span class="muted"> #{{ row.objectId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="专家用户" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ reviewerName(row.reviewerUserId) }}</template>
      </el-table-column>
      <el-table-column label="专家用户ID" prop="reviewerUserId" width="110" />
      <el-table-column label="专家画像ID" prop="reviewerId" width="110" />
      <el-table-column label="分配类型" prop="assignmentType" width="110">
        <template #default="{ row }">{{ optionLabel(assignmentTypeOptions, row.assignmentType) }}</template>
      </el-table-column>
      <el-table-column label="任务状态" prop="status" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="assignmentStatusTag(row.status)">{{ optionLabel(assignmentStatusOptions, row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分配时间" prop="assignedTime" width="170" />
      <el-table-column label="提交时间" prop="submittedTime" width="170" />
      <el-table-column label="备注" prop="remark" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="90" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            link
            type="danger"
            icon="Delete"
            :disabled="!canRemove(row)"
            @click="handleDelete(row)"
            v-hasPermi="['competition:review:assignment:remove']"
          >
            删除
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

    <el-dialog title="批量分配专家任务" v-model="batchOpen" width="1120px" append-to-body destroy-on-close>
      <el-form ref="batchFormRef" :model="batchForm" :rules="batchRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="评审活动" prop="activityId">
              <el-select v-model="batchForm.activityId" placeholder="请选择评审活动" filterable style="width: 100%" @change="handleBatchActivityChange">
                <el-option v-for="item in activityOptions" :key="item.id" :label="item.activityName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评审轮次" prop="roundId">
              <el-select v-model="batchForm.roundId" placeholder="请选择评审轮次" filterable style="width: 100%">
                <el-option v-for="item in batchRoundOptions" :key="item.id" :label="formatRoundLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分配类型">
              <el-select v-model="batchForm.assignmentType" style="width: 100%">
                <el-option v-for="item in assignmentTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重复处理">
              <el-checkbox v-model="batchForm.overwriteExisting">覆盖未提交任务</el-checkbox>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model.trim="batchForm.remark" maxlength="500" placeholder="可填写本次分配说明" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-divider content-position="left">选择评审对象</el-divider>
      <el-form :model="objectQuery" :inline="true" label-width="86px">
        <el-form-item label="对象名称">
          <el-input v-model.trim="objectQuery.objectName" placeholder="请输入" clearable style="width: 180px" @keyup.enter="loadBatchObjects" />
        </el-form-item>
        <el-form-item label="对象编号">
          <el-input v-model.trim="objectQuery.objectCode" placeholder="请输入" clearable style="width: 160px" @keyup.enter="loadBatchObjects" />
        </el-form-item>
        <el-form-item label="提交状态">
          <el-select v-model="objectQuery.submitStatus" placeholder="不限" clearable style="width: 140px">
            <el-option v-for="item in objectStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" :disabled="!batchForm.activityId" @click="loadBatchObjects">查询对象</el-button>
        </el-form-item>
      </el-form>
      <el-table
        ref="objectTableRef"
        v-loading="objectLoading"
        :data="objectOptions"
        row-key="id"
        stripe
        height="260"
        @selection-change="handleObjectSelectionChange"
      >
        <el-table-column type="selection" reserve-selection width="48" />
        <el-table-column label="对象编号" prop="objectCode" min-width="130" show-overflow-tooltip />
        <el-table-column label="项目名称" prop="objectName" min-width="220" show-overflow-tooltip />
        <el-table-column label="所属单位" prop="orgName" min-width="150" show-overflow-tooltip />
        <el-table-column label="负责人" prop="contactName" width="110" show-overflow-tooltip />
        <el-table-column label="状态" prop="submitStatus" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="objectStatusTag(row.submitStatus)">{{ optionLabel(objectStatusOptions, row.submitStatus) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="selection-summary">已选评审对象 {{ selectedObjects.length }} 个</div>

      <el-divider content-position="left">选择专家用户</el-divider>
      <div class="reviewer-toolbar">
        <el-button type="primary" plain icon="User" @click="openReviewerDialog">选择专家用户</el-button>
        <span class="selection-summary">已选专家 {{ selectedReviewers.length }} 人</span>
      </div>
      <el-table :data="selectedReviewers" size="small" stripe max-height="180">
        <el-table-column label="用户ID" prop="userId" width="100" />
        <el-table-column label="姓名" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ getUserDisplayName(row) }}</template>
        </el-table-column>
        <el-table-column label="账号" prop="userName" min-width="130" show-overflow-tooltip />
        <el-table-column label="手机号" prop="phonenumber" min-width="120" show-overflow-tooltip />
        <el-table-column label="邮箱" prop="email" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button link type="danger" icon="Delete" @click="removeReviewer(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="batchOpen = false">取 消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="submitBatchAssign">生成评审任务</el-button>
      </template>
    </el-dialog>

    <el-dialog title="选择专家用户" v-model="reviewerOpen" width="960px" append-to-body>
      <el-form ref="reviewerQueryRef" :model="reviewerQuery" :inline="true" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model.trim="reviewerQuery.userName" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleReviewerQuery" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model.trim="reviewerQuery.realName" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleReviewerQuery" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model.trim="reviewerQuery.phonenumber" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleReviewerQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleReviewerQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetReviewerQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table
        ref="reviewerTableRef"
        v-loading="reviewerLoading"
        :data="reviewerList"
        row-key="userId"
        stripe
        height="420"
        @selection-change="handleReviewerSelectionChange"
      >
        <el-table-column type="selection" reserve-selection width="48" />
        <el-table-column label="用户ID" prop="userId" width="100" />
        <el-table-column label="账号" prop="userName" min-width="130" show-overflow-tooltip />
        <el-table-column label="姓名" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ getUserDisplayName(row) }}</template>
        </el-table-column>
        <el-table-column label="手机号" prop="phonenumber" min-width="120" show-overflow-tooltip />
        <el-table-column label="邮箱" prop="email" min-width="170" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="String(row.status) === '0' ? 'success' : 'info'">{{ String(row.status) === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="reviewerTotal > 0"
        :total="reviewerTotal"
        v-model:page="reviewerQuery.pageNum"
        v-model:limit="reviewerQuery.pageSize"
        @pagination="loadReviewers"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewerOpen = false">取 消</el-button>
          <el-button type="primary" @click="confirmReviewerSelection">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import RightToolbar from '@/components/RightToolbar'
import { listReviewActivity } from '@/api/review/activity'
import { listReviewRound } from '@/api/review/round'
import { listReviewObject } from '@/api/review/object'
import { batchAssignReviewAssignment, delReviewAssignment, listReviewAssignment } from '@/api/review/assignment'
import { getWorkUserList } from '@/api/system/user'
import { selectUserByIds } from '@/api/userSelect'

const route = useRoute()
const showSearch = ref(true)
const loading = ref(false)
const total = ref(0)
const assignmentList = ref([])
const activityOptions = ref([])
const roundOptions = ref([])
const objectNameMap = ref({})
const userNameMap = ref({})
const queryRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  activityId: undefined,
  roundId: undefined,
  objectId: '',
  reviewerUserId: '',
  status: ''
})

const assignmentStatusOptions = [
  { label: '已分配', value: 'ASSIGNED', type: 'info' },
  { label: '评审中', value: 'IN_PROGRESS', type: 'warning' },
  { label: '已提交', value: 'SUBMITTED', type: 'success' },
  { label: '已退回', value: 'RETURNED', type: 'danger' },
  { label: '已锁定', value: 'LOCKED', type: 'primary' },
  { label: '已取消', value: 'CANCELLED', type: 'info' }
]

const assignmentTypeOptions = [
  { label: '普通评审', value: 'NORMAL' },
  { label: '现场评审', value: 'ONSITE' }
]

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

const queryRoundOptions = computed(() => {
  if (!queryParams.activityId) return roundOptions.value
  return roundOptions.value.filter(item => item.activityId === queryParams.activityId)
})

const batchRoundOptions = computed(() => {
  if (!batchForm.activityId) return []
  return roundOptions.value.filter(item => item.activityId === batchForm.activityId)
})

const batchOpen = ref(false)
const batchLoading = ref(false)
const batchFormRef = ref(null)
const batchForm = reactive({
  activityId: undefined,
  roundId: undefined,
  assignmentType: 'ONSITE',
  overwriteExisting: false,
  remark: ''
})
const batchRules = {
  activityId: [{ required: true, message: '请选择评审活动', trigger: 'change' }],
  roundId: [{ required: true, message: '请选择评审轮次', trigger: 'change' }]
}

const objectTableRef = ref(null)
const objectLoading = ref(false)
const objectOptions = ref([])
const selectedObjects = ref([])
const objectQuery = reactive({
  objectName: '',
  objectCode: '',
  submitStatus: ''
})

const reviewerOpen = ref(false)
const reviewerLoading = ref(false)
const reviewerTotal = ref(0)
const reviewerList = ref([])
const selectedReviewers = ref([])
const reviewerDialogSelected = ref([])
const reviewerTableRef = ref(null)
const syncingReviewerSelection = ref(false)
const reviewerQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  realName: '',
  phonenumber: '',
  status: '0'
})

function loadBaseOptions() {
  listReviewActivity({ pageNum: 1, pageSize: 500 }).then(res => {
    activityOptions.value = res.rows || []
  })
  listReviewRound({ pageNum: 1, pageSize: 1000 }).then(res => {
    roundOptions.value = res.rows || []
  })
}

function getList() {
  loading.value = true
  listReviewAssignment(normalizeQuery(queryParams)).then(res => {
    assignmentList.value = res.rows || []
    total.value = res.total || 0
    loadListNames()
  }).finally(() => {
    loading.value = false
  })
}

function loadListNames() {
  const activityIds = [...new Set(assignmentList.value.map(item => item.activityId).filter(Boolean))]
  activityIds.forEach(activityId => {
    if (!objectNameMap.value[`activity:${activityId}`]) {
      loadObjectNames(activityId)
    }
  })
  const userIds = [...new Set(assignmentList.value.map(item => item.reviewerUserId).filter(Boolean))]
  if (userIds.length) {
    selectUserByIds(userIds.join(',')).then(res => {
      const rows = normalizeRows(res)
      rows.forEach(row => {
        userNameMap.value[row.userId] = getUserDisplayName(row)
      })
    }).catch(() => {})
  }
}

function loadObjectNames(activityId) {
  listReviewObject({ pageNum: 1, pageSize: 1000, activityId }).then(res => {
    const map = { ...objectNameMap.value }
    ;(res.rows || []).forEach(item => {
      map[item.id] = item.objectName || item.objectCode || item.id
    })
    map[`activity:${activityId}`] = true
    objectNameMap.value = map
  }).catch(() => {})
}

function handleQueryActivityChange() {
  queryParams.roundId = undefined
  handleQuery()
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function openBatchDialog() {
  resetBatchForm()
  if (route.query.activityId) {
    batchForm.activityId = Number(route.query.activityId)
  } else if (queryParams.activityId) {
    batchForm.activityId = queryParams.activityId
  }
  handleBatchActivityChange(batchForm.activityId)
  batchOpen.value = true
}

function resetBatchForm() {
  batchForm.activityId = undefined
  batchForm.roundId = undefined
  batchForm.assignmentType = 'ONSITE'
  batchForm.overwriteExisting = false
  batchForm.remark = ''
  objectQuery.objectName = ''
  objectQuery.objectCode = ''
  objectQuery.submitStatus = ''
  objectOptions.value = []
  selectedObjects.value = []
  selectedReviewers.value = []
  nextTick(() => objectTableRef.value?.clearSelection())
}

function handleBatchActivityChange(activityId) {
  batchForm.roundId = undefined
  objectOptions.value = []
  selectedObjects.value = []
  nextTick(() => objectTableRef.value?.clearSelection())
  if (!activityId) return
  loadBatchObjects()
}

function loadBatchObjects() {
  if (!batchForm.activityId) {
    ElMessage.warning('请先选择评审活动')
    return
  }
  objectLoading.value = true
  listReviewObject({
    pageNum: 1,
    pageSize: 1000,
    activityId: batchForm.activityId,
    objectName: objectQuery.objectName,
    objectCode: objectQuery.objectCode,
    submitStatus: objectQuery.submitStatus
  }).then(res => {
    objectOptions.value = res.rows || []
    nextTick(syncObjectSelection)
  }).finally(() => {
    objectLoading.value = false
  })
}

function handleObjectSelectionChange(rows) {
  selectedObjects.value = rows || []
}

function syncObjectSelection() {
  if (!objectTableRef.value) return
  objectTableRef.value.clearSelection()
  const selectedIds = new Set(selectedObjects.value.map(item => item.id))
  objectOptions.value.forEach(row => {
    if (selectedIds.has(row.id)) objectTableRef.value.toggleRowSelection(row, true)
  })
}

function openReviewerDialog() {
  reviewerOpen.value = true
  reviewerDialogSelected.value = [...selectedReviewers.value]
  resetReviewerQuery()
}

function handleReviewerQuery() {
  reviewerQuery.pageNum = 1
  loadReviewers()
}

function resetReviewerQuery() {
  reviewerQuery.pageNum = 1
  reviewerQuery.pageSize = 10
  reviewerQuery.userName = ''
  reviewerQuery.realName = ''
  reviewerQuery.phonenumber = ''
  reviewerQuery.status = '0'
  loadReviewers()
}

function loadReviewers() {
  reviewerLoading.value = true
  getWorkUserList(reviewerQuery).then(res => {
    reviewerList.value = res.rows || []
    reviewerTotal.value = res.total || 0
    nextTick(syncReviewerTableSelection)
  }).finally(() => {
    reviewerLoading.value = false
  })
}

function syncReviewerTableSelection() {
  if (!reviewerTableRef.value) return
  syncingReviewerSelection.value = true
  reviewerTableRef.value.clearSelection()
  const selectedIds = new Set(reviewerDialogSelected.value.map(getUserId).filter(Boolean))
  reviewerList.value.forEach(row => {
    if (selectedIds.has(getUserId(row))) {
      reviewerTableRef.value.toggleRowSelection(row, true)
    }
  })
  nextTick(() => {
    syncingReviewerSelection.value = false
  })
}

function handleReviewerSelectionChange(rows) {
  if (syncingReviewerSelection.value) return
  const currentIds = new Set(reviewerList.value.map(getUserId).filter(Boolean))
  const preserved = reviewerDialogSelected.value.filter(item => !currentIds.has(getUserId(item)))
  reviewerDialogSelected.value = mergeUsers([...preserved, ...(rows || [])])
}

function confirmReviewerSelection() {
  selectedReviewers.value = mergeUsers(reviewerDialogSelected.value)
  reviewerOpen.value = false
}

function removeReviewer(row) {
  const userId = getUserId(row)
  selectedReviewers.value = selectedReviewers.value.filter(item => getUserId(item) !== userId)
}

function submitBatchAssign() {
  batchFormRef.value?.validate(valid => {
    if (!valid) return
    if (!selectedObjects.value.length) {
      ElMessage.warning('请选择评审对象')
      return
    }
    if (!selectedReviewers.value.length) {
      ElMessage.warning('请选择专家用户')
      return
    }
    ElMessageBox.confirm(
      `确认为 ${selectedObjects.value.length} 个对象、${selectedReviewers.value.length} 名专家生成评审任务吗？`,
      '批量分配确认',
      { type: 'warning' }
    ).then(() => {
      batchLoading.value = true
      return batchAssignReviewAssignment({
        activityId: batchForm.activityId,
        roundId: batchForm.roundId,
        assignmentType: batchForm.assignmentType,
        overwriteExisting: batchForm.overwriteExisting,
        remark: batchForm.remark,
        objectIds: selectedObjects.value.map(item => item.id),
        reviewerUserIds: selectedReviewers.value.map(getUserId)
      })
    }).then(res => {
      const result = res.data || {}
      ElMessage.success(`分配完成：成功 ${result.successCount || 0}，跳过 ${result.skipCount || 0}，失败 ${result.failedCount || 0}`)
      if ((result.skippedItems || []).length || (result.failedItems || []).length) {
        ElMessageBox.alert(
          [...(result.skippedItems || []), ...(result.failedItems || [])].slice(0, 30).join('\n') || '无明细',
          '分配结果明细',
          { type: 'info' }
        ).catch(() => {})
      }
      batchOpen.value = false
      getList()
    }).catch(() => {}).finally(() => {
      batchLoading.value = false
    })
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除评审任务 ${row.id} 吗？已提交、锁定、取消的任务后端会拒绝删除。`, '删除确认', {
    type: 'warning'
  }).then(() => delReviewAssignment(row.id)).then(() => {
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

function canRemove(row) {
  return !['SUBMITTED', 'LOCKED', 'CANCELLED'].includes(row.status)
}

function normalizeQuery(query) {
  const data = { ...query }
  ;['activityId', 'roundId', 'objectId', 'reviewerUserId'].forEach(key => {
    if (data[key] === '') data[key] = undefined
    if (data[key] != null && data[key] !== undefined) data[key] = Number(data[key])
  })
  return data
}

function normalizeRows(res) {
  if (Array.isArray(res?.rows)) return res.rows
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res)) return res
  return []
}

function mergeUsers(users) {
  const map = new Map()
  users.filter(item => getUserId(item)).forEach(item => {
    map.set(getUserId(item), item)
  })
  return Array.from(map.values())
}

function getUserId(row) {
  return row?.userId || row?.id
}

function getUserDisplayName(row) {
  return row?.realName || row?.nickName || row?.userName || row?.label || row?.name || '-'
}

function reviewerName(userId) {
  return userNameMap.value[userId] ? `${userNameMap.value[userId]}（${userId}）` : (userId || '-')
}

function activityName(activityId) {
  const item = activityOptions.value.find(activity => activity.id === activityId)
  return item ? item.activityName : (activityId || '-')
}

function roundName(roundId) {
  const item = roundOptions.value.find(round => round.id === roundId)
  return item ? formatRoundLabel(item) : (roundId || '-')
}

function objectName(objectId) {
  return objectNameMap.value[objectId] || objectId || '-'
}

function formatRoundLabel(row) {
  if (!row) return '-'
  const no = row.roundNo ? `${row.roundNo}. ` : ''
  const type = row.roundType ? ` / ${row.roundType}` : ''
  return `${no}${row.roundName || row.id}${type}`
}

function optionLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function assignmentStatusTag(status) {
  const item = assignmentStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

function objectStatusTag(status) {
  const item = objectStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

onMounted(() => {
  if (route.query.activityId) {
    queryParams.activityId = Number(route.query.activityId)
  }
  loadBaseOptions()
  getList()
})
</script>

<style scoped lang="scss">
.review-assignment {
  .muted {
    color: #909399;
  }

  .mb12 {
    margin-bottom: 12px;
  }

  .selection-summary {
    margin-top: 8px;
    color: #606266;
    font-size: 13px;
  }

  .reviewer-toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 10px;
  }
}
</style>
