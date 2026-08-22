<template>
  <div class="app-container">
    <el-alert
      title="现场联调顺序：新建场次 -> 配置场次对象顺序 -> 进入现场控制台或手动设为当前对象 -> 专家端按 sessionId 高亮当前对象。"
      type="info"
      show-icon
      :closable="false"
      class="mb16"
    />

    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="96px">
      <el-form-item label="评审活动" prop="activityId">
        <el-select v-model="queryParams.activityId" placeholder="请选择活动" clearable filterable style="width: 240px">
          <el-option v-for="item in activityOptions" :key="item.id" :label="item.activityName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="场次名称" prop="sessionName">
        <el-input v-model.trim="queryParams.sessionName" placeholder="请输入场次名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="场次编码" prop="sessionCode">
        <el-input v-model.trim="queryParams.sessionCode" placeholder="请输入场次编码" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 140px">
          <el-option v-for="item in sessionStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['competition:review:session:add']">新增场次</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="sessionList" stripe>
      <el-table-column label="场次名称" prop="sessionName" min-width="180" show-overflow-tooltip />
      <el-table-column label="场次编码" prop="sessionCode" min-width="130" show-overflow-tooltip />
      <el-table-column label="活动" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ activityName(row.activityId) }}</template>
      </el-table-column>
      <el-table-column label="现场轮次" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ roundName(row.roundId) }}</template>
      </el-table-column>
      <el-table-column label="地点" prop="location" min-width="150" show-overflow-tooltip />
      <el-table-column label="开始时间" prop="startTime" width="170" />
      <el-table-column label="结束时间" prop="endTime" width="170" />
      <el-table-column label="秘书" min-width="130" show-overflow-tooltip>
        <template #default="{ row }">{{ userName(row.secretaryUserId) }}</template>
      </el-table-column>
      <el-table-column label="当前对象" prop="currentObjectId" width="100" />
      <el-table-column label="状态" prop="status" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="sessionStatusTag(row.status)">{{ optionLabel(sessionStatusOptions, row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="360" align="center">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleEdit(row)" v-hasPermi="['competition:review:session:edit']">编辑</el-button>
          <el-button link type="success" icon="Tickets" @click="openObjectDrawer(row)" v-hasPermi="['competition:review:session:list']">对象顺序</el-button>
          <el-button link type="warning" icon="Clock" @click="openEventLog(row)" v-hasPermi="['competition:review:session:list']">日志</el-button>
          <el-button link type="primary" icon="Position" @click="openSecretaryConsole(row)" v-hasPermi="['competition:review:secretary:query']">现场控制台</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)" v-hasPermi="['competition:review:session:remove']">删除</el-button>
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

    <el-dialog :title="dialogTitle" v-model="open" width="900px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="评审活动" prop="activityId">
              <el-select v-model="form.activityId" :disabled="!!form.id" placeholder="请选择活动" filterable style="width: 100%" @change="handleFormActivityChange">
                <el-option v-for="item in activityOptions" :key="item.id" :label="item.activityName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="现场轮次" prop="roundId">
              <el-select
                v-model="form.roundId"
                :disabled="!!form.id || !form.activityId"
                placeholder="请选择活动中预置的现场答辩轮次"
                clearable
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="item in roundOptions"
                  :key="item.id"
                  :label="`${item.roundName || '现场答辩'}（第${item.roundNo || item.id}轮）`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="form.activityId && roundOptions.length === 0" :span="24">
            <el-alert
              title="当前活动还没有现场答辩轮次。请先到“评审活动”编辑页的“评审轮次”中新增现场答辩轮次，再创建现场场次。"
              type="warning"
              show-icon
              :closable="false"
              class="mb16"
            />
          </el-col>
          <el-col :span="12">
            <el-form-item label="场次名称" prop="sessionName">
              <el-input v-model.trim="form.sessionName" maxlength="200" placeholder="请输入场次名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="场次编码" prop="sessionCode">
              <el-input v-model.trim="form.sessionCode" maxlength="100" placeholder="如 DEFENSE-01" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="场地">
              <el-input v-model.trim="form.location" maxlength="200" placeholder="请输入现场地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="秘书用户">
              <el-select
                v-model="form.secretaryUserId"
                placeholder="请选择现场秘书"
                clearable
                filterable
                remote
                reserve-keyword
                :remote-method="searchUsers"
                :loading="userLoading"
                style="width: 100%"
                @visible-change="visible => visible && loadUsers()"
              >
                <el-option
                  v-for="item in userOptions"
                  :key="item.userId"
                  :label="userOptionLabel(item)"
                  :value="item.userId"
                >
                  <div class="select-option-main">{{ userOptionLabel(item) }}</div>
                  <div class="select-option-sub">{{ [item.userName, item.phonenumber, item.email].filter(Boolean).join(' / ') }}</div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">保 存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="objectDrawerOpen" size="82%" :title="objectDrawerTitle" append-to-body>
      <div v-if="currentSession.id" class="drawer-toolbar">
        <el-form :model="objectForm" :inline="true" label-width="88px">
          <el-form-item label="评审对象">
            <el-select v-model="objectForm.objectId" placeholder="选择当前活动对象" filterable clearable style="width: 360px">
              <el-option
                v-for="item in availableObjects"
                :key="item.id"
                :label="`${item.objectCode || item.id} - ${item.objectName || '未命名'}（${objectStatusLabel(item.submitStatus)}）`"
                :value="item.id"
                :disabled="hasSessionObject(item.id)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="序号">
            <el-input-number v-model="objectForm.sequenceNo" :min="1" controls-position="right" style="width: 120px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Plus" :loading="objectActionLoading" @click="handleAddSessionObject" v-hasPermi="['competition:review:session:add']">加入场次</el-button>
            <el-button type="success" plain icon="Finished" :loading="objectActionLoading" @click="handleBulkAddAvailable" v-hasPermi="['competition:review:session:add']">加入全部未加入对象</el-button>
            <el-button icon="Refresh" @click="refreshSessionObjects">刷新</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table v-loading="objectLoading" :data="sessionObjectRows" stripe>
        <el-table-column label="序号" width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.sequenceNo" :min="1" controls-position="right" size="small" style="width: 90px" />
          </template>
        </el-table-column>
        <el-table-column label="对象编号" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ objectInfo(row.objectId).objectCode || row.objectId }}</template>
        </el-table-column>
        <el-table-column label="项目名称" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ objectInfo(row.objectId).objectName || '-' }}</template>
        </el-table-column>
        <el-table-column label="提交状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="objectStatusTag(objectInfo(row.objectId).submitStatus)">{{ objectStatusLabel(objectInfo(row.objectId).submitStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到场状态" width="110" align="center">
          <template #default="{ row }">{{ checkinStatusLabel(row.checkinStatus) }}</template>
        </el-table-column>
        <el-table-column label="评审状态" width="110" align="center">
          <template #default="{ row }">{{ reviewStatusLabel(row.reviewStatus) }}</template>
        </el-table-column>
        <el-table-column label="备注" min-width="180">
          <template #default="{ row }">
            <el-input v-model.trim="row.secretaryNote" maxlength="500" placeholder="秘书备注" />
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="270" align="center">
          <template #default="{ row }">
            <el-button link type="primary" icon="Check" @click="handleSaveSessionObject(row)" v-hasPermi="['competition:review:session:edit']">保存</el-button>
            <el-button link type="success" icon="VideoPlay" @click="handleSetCurrent(row)" v-hasPermi="['competition:review:session:edit']">设为当前</el-button>
            <el-button link type="danger" icon="Delete" @click="handleRemoveSessionObject(row)" v-hasPermi="['competition:review:session:remove']">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-drawer v-model="eventLogOpen" size="70%" :title="eventLogTitle" append-to-body>
      <el-table v-loading="eventLogLoading" :data="eventLogList" stripe>
        <el-table-column label="事件类型" prop="eventType" width="140" />
        <el-table-column label="对象ID" prop="objectId" width="100" />
        <el-table-column label="操作人" prop="operatorUserId" width="100" />
        <el-table-column label="事件时间" prop="eventTime" width="170" />
        <el-table-column label="事件内容" prop="eventContent" min-width="260" show-overflow-tooltip />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import RightToolbar from '@/components/RightToolbar'
import { listReviewActivity } from '@/api/review/activity'
import { listReviewObject } from '@/api/review/object'
import { listReviewRound } from '@/api/review/round'
import { listUser } from '@/api/system/user'
import {
  addReviewSession,
  addReviewSessionObject,
  delReviewSession,
  delReviewSessionObject,
  getReviewSession,
  listReviewSession,
  listReviewSessionEventLog,
  listReviewSessionObject,
  setCurrentReviewObject,
  updateReviewSession,
  updateReviewSessionObject
} from '@/api/review/session'

const router = useRouter()
const showSearch = ref(true)
const loading = ref(false)
const total = ref(0)
const sessionList = ref([])
const activityOptions = ref([])
const roundOptions = ref([])
const allRoundOptions = ref([])
const userOptions = ref([])
const userLoading = ref(false)
const queryRef = ref(null)
const formRef = ref(null)
const open = ref(false)
const dialogTitle = ref('新增现场场次')
const objectDrawerOpen = ref(false)
const objectLoading = ref(false)
const objectActionLoading = ref(false)
const availableObjects = ref([])
const sessionObjects = ref([])
const eventLogOpen = ref(false)
const eventLogLoading = ref(false)
const eventLogList = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  activityId: undefined,
  sessionName: '',
  sessionCode: '',
  status: ''
})

const form = reactive(defaultForm())
const currentSession = reactive({})
const objectForm = reactive({
  objectId: undefined,
  sequenceNo: 1
})

const rules = {
  activityId: [{ required: true, message: '评审活动不能为空', trigger: 'change' }],
  roundId: [{ required: true, message: '评审轮次不能为空', trigger: 'change' }],
  sessionName: [{ required: true, message: '场次名称不能为空', trigger: 'blur' }],
  sessionCode: [{ required: true, message: '场次编码不能为空', trigger: 'blur' }]
}

const sessionStatusOptions = [
  { label: '未开始', value: 'NOT_STARTED', type: 'info' },
  { label: '进行中', value: 'IN_PROGRESS', type: 'success' },
  { label: '已暂停', value: 'PAUSED', type: 'warning' },
  { label: '已结束', value: 'ENDED', type: 'danger' },
  { label: '已归档', value: 'ARCHIVED', type: 'info' }
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

const checkinStatusOptions = [
  { label: '等待', value: 'WAITING' },
  { label: '已到场', value: 'PRESENT' },
  { label: '缺席', value: 'ABSENT' },
  { label: '迟到', value: 'LATE' }
]

const reviewStatusOptions = [
  { label: '等待', value: 'WAITING' },
  { label: '评审中', value: 'REVIEWING' },
  { label: '已评分', value: 'SCORED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已跳过', value: 'SKIPPED' },
  { label: '已延后', value: 'DELAYED' }
]

const sessionObjectRows = computed(() => {
  return [...sessionObjects.value].sort((a, b) => {
    const seqA = a.sequenceNo || 0
    const seqB = b.sequenceNo || 0
    return seqA === seqB ? (a.id || 0) - (b.id || 0) : seqA - seqB
  })
})

const objectDrawerTitle = computed(() => {
  return currentSession.sessionName ? `场次对象顺序 - ${currentSession.sessionName}` : '场次对象顺序'
})

const eventLogTitle = computed(() => {
  return currentSession.sessionName ? `现场事件日志 - ${currentSession.sessionName}` : '现场事件日志'
})

function defaultForm() {
  return {
    id: undefined,
    activityId: undefined,
    roundId: undefined,
    sessionName: '',
    sessionCode: '',
    location: '',
    startTime: '',
    endTime: '',
    secretaryUserId: undefined
  }
}

function resetForm() {
  Object.assign(form, defaultForm())
  delete form.panelId
  roundOptions.value = []
  formRef.value?.clearValidate()
}

function loadActivities() {
  listReviewActivity({ pageNum: 1, pageSize: 200 }).then(res => {
    activityOptions.value = res.rows || []
  })
}

function loadAllRounds() {
  return listReviewRound({ pageNum: 1, pageSize: 500 }).then(res => {
    allRoundOptions.value = res.rows || []
  })
}

function loadRounds(activityId) {
  if (!activityId) {
    roundOptions.value = []
    return Promise.resolve()
  }
  return listReviewRound({ activityId, pageNum: 1, pageSize: 200 }).then(res => {
    roundOptions.value = (res.rows || [])
      .filter(item => item.roundType === 'ONSITE_DEFENSE')
      .sort((a, b) => {
        const noA = a.roundNo || 0
        const noB = b.roundNo || 0
        return noA === noB ? (a.id || 0) - (b.id || 0) : noA - noB
      })
  })
}

function getList() {
  loading.value = true
  listReviewSession(queryParams).then(res => {
    sessionList.value = res.rows || []
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

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增现场场次'
  open.value = true
  loadUsers()
}

function handleEdit(row) {
  resetForm()
  dialogTitle.value = '编辑现场场次'
  getReviewSession(row.id).then(res => {
    Object.assign(form, defaultForm(), res.data || {})
    delete form.panelId
    appendUserIds([form.secretaryUserId])
    return loadRounds(form.activityId)
  }).then(() => {
    open.value = true
  })
}

function handleFormActivityChange(activityId) {
  form.roundId = undefined
  loadRounds(activityId)
}

function submitForm() {
  formRef.value?.validate(valid => {
    if (!valid) {
      return
    }
    const payload = { ...form }
    delete payload.panelId
    const request = form.id ? updateReviewSession(form.id, payload) : addReviewSession(payload)
    request.then(() => {
      ElMessage.success('保存成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除场次“${row.sessionName}”吗？`, '删除确认', {
    type: 'warning'
  }).then(() => delReviewSession(row.id)).then(() => {
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

function openObjectDrawer(row) {
  Object.assign(currentSession, row)
  objectDrawerOpen.value = true
  objectForm.objectId = undefined
  objectForm.sequenceNo = 1
  refreshSessionObjects()
}

function refreshSessionObjects() {
  return Promise.all([
    loadAvailableObjects(),
    loadSessionObjects()
  ]).then(() => {
    objectForm.sequenceNo = nextSequenceNo()
  })
}

function loadAvailableObjects() {
  if (!currentSession.activityId) {
    availableObjects.value = []
    return Promise.resolve()
  }
  return listReviewObject({
    activityId: currentSession.activityId,
    pageNum: 1,
    pageSize: 500
  }).then(res => {
    availableObjects.value = res.rows || []
  })
}

function loadSessionObjects() {
  objectLoading.value = true
  return listReviewSessionObject({
    sessionId: currentSession.id,
    pageNum: 1,
    pageSize: 500
  }).then(res => {
    sessionObjects.value = res.rows || []
  }).finally(() => {
    objectLoading.value = false
  })
}

function handleAddSessionObject() {
  if (!objectForm.objectId) {
    ElMessage.warning('请先选择评审对象')
    return
  }
  objectActionLoading.value = true
  addReviewSessionObject({
    activityId: currentSession.activityId,
    roundId: currentSession.roundId,
    sessionId: currentSession.id,
    objectId: objectForm.objectId,
    sequenceNo: objectForm.sequenceNo
  }).then(() => {
    ElMessage.success('已加入场次')
    objectForm.objectId = undefined
    return refreshSessionObjects()
  }).finally(() => {
    objectActionLoading.value = false
  })
}

async function handleBulkAddAvailable() {
  const addableObjects = availableObjects.value.filter(item => !hasSessionObject(item.id))
  if (addableObjects.length === 0) {
    ElMessage.warning('当前活动没有可加入的评审对象')
    return
  }
  await ElMessageBox.confirm(`确认将 ${addableObjects.length} 个未加入对象加入该场次吗？对象状态不限。`, '批量加入确认', {
    type: 'warning'
  })
  objectActionLoading.value = true
  try {
    let sequenceNo = nextSequenceNo()
    for (const item of addableObjects) {
      await addReviewSessionObject({
        activityId: currentSession.activityId,
        roundId: currentSession.roundId,
        sessionId: currentSession.id,
        objectId: item.id,
        sequenceNo: sequenceNo++
      })
    }
    ElMessage.success('批量加入成功')
    await refreshSessionObjects()
  } finally {
    objectActionLoading.value = false
  }
}

function handleSaveSessionObject(row) {
  updateReviewSessionObject(row.id, {
    sequenceNo: row.sequenceNo,
    secretaryNote: row.secretaryNote
  }).then(() => {
    ElMessage.success('保存成功')
    refreshSessionObjects()
  })
}

function handleRemoveSessionObject(row) {
  ElMessageBox.confirm('确认将该对象从场次顺序中移除吗？', '移除确认', {
    type: 'warning'
  }).then(() => delReviewSessionObject(row.id)).then(() => {
    ElMessage.success('移除成功')
    refreshSessionObjects()
  }).catch(() => {})
}

function handleSetCurrent(row) {
  const info = objectInfo(row.objectId)
  ElMessageBox.confirm(`确认将“${info.objectName || row.objectId}”设为当前评审对象吗？`, '切换当前对象', {
    type: 'warning'
  }).then(() => {
    return setCurrentReviewObject(currentSession.id, {
      objectId: row.objectId,
      sourceType: 'MANUAL'
    })
  }).then(() => {
    ElMessage.success('已设为当前对象')
    refreshSessionObjects()
    getList()
  }).catch(() => {})
}

function openEventLog(row) {
  Object.assign(currentSession, row)
  eventLogOpen.value = true
  eventLogLoading.value = true
  listReviewSessionEventLog({
    sessionId: row.id,
    pageNum: 1,
    pageSize: 200
  }).then(res => {
    eventLogList.value = res.rows || []
  }).finally(() => {
    eventLogLoading.value = false
  })
}

function openSecretaryConsole(row) {
  router.push(`/review/secretary/session/index/${row.id}`)
}

function loadUsers(keyword = '') {
  userLoading.value = true
  const query = {
    pageNum: 1,
    pageSize: 100,
    status: '0'
  }
  if (keyword) {
    query.userName = keyword
  }
  return listUser(query).then(res => {
    appendUserOptions(res.rows || [])
  }).finally(() => {
    userLoading.value = false
  })
}

function searchUsers(keyword) {
  loadUsers(keyword)
}

function appendUserOptions(users) {
  const map = new Map()
  userOptions.value.forEach(item => {
    if (item.userId) map.set(Number(item.userId), item)
  })
  users.forEach(item => {
    const userId = Number(item.userId || item.id)
    if (userId) {
      map.set(userId, { ...item, userId })
    }
  })
  userOptions.value = Array.from(map.values())
}

function appendUserIds(ids) {
  const existing = new Set(userOptions.value.map(item => Number(item.userId)))
  ids.filter(Boolean).forEach(id => {
    const userId = Number(id)
    if (userId && !existing.has(userId)) {
      userOptions.value.push({ userId })
      existing.add(userId)
    }
  })
}

function activityName(activityId) {
  const item = activityOptions.value.find(activity => String(activity.id) === String(activityId))
  return item ? item.activityName : (activityId || '-')
}

function roundName(roundId) {
  const item = [...roundOptions.value, ...allRoundOptions.value].find(round => String(round.id) === String(roundId))
  if (!item) return roundId || '-'
  return `${item.roundName || '现场轮次'}${item.roundNo ? `（第${item.roundNo}轮）` : ''}`
}

function userName(userId) {
  const item = userOptions.value.find(user => Number(user.userId) === Number(userId))
  return item ? userOptionLabel(item) : (userId || '-')
}

function userOptionLabel(item) {
  if (!item) return '-'
  return item.nickName || item.userName || item.name || (item.userId ? `用户${item.userId}` : '-')
}

function objectInfo(objectId) {
  return availableObjects.value.find(item => String(item.id) === String(objectId)) || {}
}

function hasSessionObject(objectId) {
  return sessionObjects.value.some(item => String(item.objectId) === String(objectId))
}

function nextSequenceNo() {
  const max = sessionObjects.value.reduce((value, item) => Math.max(value, item.sequenceNo || 0), 0)
  return max + 1
}

function optionLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function sessionStatusTag(status) {
  const item = sessionStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

function objectStatusLabel(status) {
  return optionLabel(objectStatusOptions, status)
}

function objectStatusTag(status) {
  const item = objectStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

function checkinStatusLabel(status) {
  return optionLabel(checkinStatusOptions, status)
}

function reviewStatusLabel(status) {
  return optionLabel(reviewStatusOptions, status)
}

onMounted(() => {
  loadActivities()
  loadAllRounds()
  loadUsers()
  getList()
})
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}

.drawer-toolbar {
  margin-bottom: 12px;
  padding: 12px;
  background: #f8f9fb;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.inline-selector {
  display: flex;
  width: 100%;
  gap: 8px;
}

.member-selector {
  width: 100%;
}

.group-import {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.select-option-main {
  line-height: 20px;
}

.select-option-sub {
  color: #909399;
  font-size: 12px;
  line-height: 18px;
}
</style>
