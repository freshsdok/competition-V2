<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="96px">
      <el-form-item label="活动名称" prop="activityName">
        <el-input v-model.trim="queryParams.activityName" placeholder="请输入活动名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="活动编码" prop="activityCode">
        <el-input v-model.trim="queryParams.activityCode" placeholder="请输入活动编码" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="活动状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 150px">
          <el-option v-for="item in activityStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['competition:review:activity:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="activityList" stripe>
      <el-table-column label="活动名称" prop="activityName" min-width="180" show-overflow-tooltip />
      <el-table-column label="活动编码" prop="activityCode" min-width="130" show-overflow-tooltip />
      <el-table-column label="活动类型" prop="activityType" width="120" show-overflow-tooltip />
      <el-table-column label="对象类型" prop="objectType" width="110">
        <template #default="{ row }">{{ optionLabel(objectTypeOptions, row.objectType) }}</template>
      </el-table-column>
      <el-table-column label="填报模式" prop="submissionMode" width="140">
        <template #default="{ row }">{{ optionLabel(submissionModeOptions, row.submissionMode) }}</template>
      </el-table-column>
      <el-table-column label="填报截止" prop="submitDeadline" width="170" />
      <el-table-column label="评审开始" prop="reviewStartTime" width="170" />
      <el-table-column label="评审结束" prop="reviewEndTime" width="170" />
      <el-table-column label="状态" prop="status" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ optionLabel(activityStatusOptions, row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="410" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)" v-hasPermi="['competition:review:activity:query']">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleEdit(row)" v-hasPermi="['competition:review:activity:edit']">编辑</el-button>
          <el-button link type="success" icon="Upload" @click="handleImport(row)" v-hasPermi="['competition:review:object:import']">导入</el-button>
          <el-button link type="warning" icon="List" @click="handleObjects(row)" v-hasPermi="['competition:review:object:list']">对象</el-button>
          <el-button link type="primary" icon="UserFilled" @click="handleAssignments(row)" v-hasPermi="['competition:review:assignment:list']">分配任务</el-button>
          <el-button link type="danger" icon="Lock" @click="handleCloseSubmission(row)" v-hasPermi="['competition:review:submission:close']">关闭填报</el-button>
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

    <el-dialog :title="dialogTitle" v-model="open" width="980px" append-to-body>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="base">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" :disabled="detailMode">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="活动名称" prop="activityName">
                  <el-input v-model.trim="form.activityName" maxlength="200" placeholder="请输入活动名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="活动编码" prop="activityCode">
                  <el-input v-model.trim="form.activityCode" maxlength="100" placeholder="请输入活动编码" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="活动类型">
                  <el-input v-model.trim="form.activityType" maxlength="50" placeholder="如 PROJECT_REVIEW" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="对象类型">
                  <el-select v-model="form.objectType" style="width: 100%">
                    <el-option v-for="item in objectTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="来源模块">
                  <el-input v-model.trim="form.sourceModule" maxlength="100" placeholder="competition" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="来源业务类型">
                  <el-input v-model.trim="form.sourceBizType" maxlength="100" placeholder="TEAM / REGISTRATION" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="填报模式">
                  <el-select v-model="form.submissionMode" style="width: 100%">
                    <el-option v-for="item in submissionModeOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态">
                  <el-select v-model="form.status" style="width: 100%">
                    <el-option v-for="item in activityStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="填报开始">
                  <el-date-picker v-model="form.submitStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="填报截止">
                  <el-date-picker v-model="form.submitDeadline" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="评审开始">
                  <el-date-picker v-model="form.reviewStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="评审结束">
                  <el-date-picker v-model="form.reviewEndTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="匿名模式">
                  <el-select v-model="form.anonymousMode" style="width: 100%">
                    <el-option label="不匿名" value="NONE" />
                    <el-option label="字段匿名" value="FIELD_ONLY" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="发布模式">
                  <el-select v-model="form.resultPublishMode" style="width: 100%">
                    <el-option label="不发布" value="NONE" />
                    <el-option label="手动发布" value="MANUAL" />
                    <el-option label="自动发布" value="AUTO" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="活动说明">
                  <el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" show-word-limit />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="评审轮次" name="round">
          <el-alert
            v-if="!form.id"
            title="请先保存评审活动基本信息，保存成功后即可配置材料评审、现场答辩等轮次。"
            type="warning"
            show-icon
            :closable="false"
            class="mb16"
          />
          <template v-else>
            <el-row :gutter="10" class="mb8">
              <el-col :span="1.5">
                <el-button type="primary" plain icon="Plus" :disabled="detailMode" @click="handleAddRound">新增轮次</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="success" plain icon="VideoPlay" :disabled="detailMode" @click="handleAddOnsiteRound">新增现场答辩轮次</el-button>
              </el-col>
            </el-row>
            <el-table v-loading="roundLoading" :data="roundList" stripe>
              <el-table-column label="序号" prop="roundNo" width="80" />
              <el-table-column label="轮次名称" prop="roundName" min-width="180" show-overflow-tooltip />
              <el-table-column label="轮次类型" prop="roundType" width="150">
                <template #default="{ row }">{{ optionLabel(roundTypeOptions, row.roundType) }}</template>
              </el-table-column>
              <el-table-column label="开始时间" prop="startTime" width="170" />
              <el-table-column label="结束时间" prop="endTime" width="170" />
              <el-table-column label="评分规则ID" prop="ruleId" width="110" />
              <el-table-column label="状态" prop="status" width="110">
                <template #default="{ row }">{{ optionLabel(roundStatusOptions, row.status) }}</template>
              </el-table-column>
              <el-table-column label="说明" prop="description" min-width="180" show-overflow-tooltip />
              <el-table-column v-if="!detailMode" label="操作" width="160" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" icon="Edit" @click="handleEditRound(row)">编辑</el-button>
                  <el-button link type="danger" icon="Delete" @click="handleDeleteRound(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="open = false">关 闭</el-button>
        <el-button v-if="!detailMode && activeTab === 'base'" type="primary" @click="submitForm">保存基本信息</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="roundDialogTitle" v-model="roundDialogOpen" width="720px" append-to-body>
      <el-form ref="roundFormRef" :model="roundForm" :rules="roundRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="轮次名称" prop="roundName">
              <el-input v-model.trim="roundForm.roundName" maxlength="200" placeholder="请输入轮次名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="轮次序号" prop="roundNo">
              <el-input-number v-model="roundForm.roundNo" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="轮次类型" prop="roundType">
              <el-select v-model="roundForm.roundType" style="width: 100%">
                <el-option v-for="item in roundTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="轮次状态">
              <el-select v-model="roundForm.status" :disabled="!!roundForm.id" style="width: 100%">
                <el-option v-for="item in roundStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker v-model="roundForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker v-model="roundForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评分规则ID">
              <el-input-number v-model="roundForm.ruleId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="轮次说明">
              <el-input v-model="roundForm.description" type="textarea" :rows="3" maxlength="1000" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="roundDialogOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitRoundForm">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import RightToolbar from '@/components/RightToolbar'
import {
  addReviewActivity,
  closeSubmission,
  getReviewActivity,
  listReviewActivity,
  updateReviewActivity
} from '@/api/review/activity'
import {
  addReviewRound,
  delReviewRound,
  listReviewRound,
  updateReviewRound
} from '@/api/review/round'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const showSearch = ref(true)
const loading = ref(false)
const total = ref(0)
const activityList = ref([])
const open = ref(false)
const detailMode = ref(false)
const dialogTitle = ref('评审活动')
const activeTab = ref('base')
const queryRef = ref(null)
const formRef = ref(null)
const roundFormRef = ref(null)
const roundDialogOpen = ref(false)
const roundDialogTitle = ref('新增评审轮次')
const roundLoading = ref(false)
const roundList = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  activityName: '',
  activityCode: '',
  status: ''
})

const form = reactive(defaultForm())
const roundForm = reactive(defaultRoundForm())

const rules = {
  activityName: [{ required: true, message: '活动名称不能为空', trigger: 'blur' }],
  activityCode: [{ required: true, message: '活动编码不能为空', trigger: 'blur' }]
}

const roundRules = {
  roundName: [{ required: true, message: '轮次名称不能为空', trigger: 'blur' }],
  roundNo: [{ required: true, message: '轮次序号不能为空', trigger: 'change' }],
  roundType: [{ required: true, message: '轮次类型不能为空', trigger: 'change' }]
}

const activityStatusOptions = [
  { label: '草稿', value: 'DRAFT', type: 'info' },
  { label: '填报中', value: 'SUBMITTING', type: 'success' },
  { label: '填报截止', value: 'SUBMIT_CLOSED', type: 'warning' },
  { label: '评审中', value: 'REVIEWING', type: 'primary' },
  { label: '汇总中', value: 'SUMMARYING', type: 'warning' },
  { label: '已发布', value: 'PUBLISHED', type: 'success' },
  { label: '已归档', value: 'ARCHIVED', type: 'info' },
  { label: '已停用', value: 'DISABLED', type: 'danger' }
]
const objectTypeOptions = [
  { label: '项目', value: 'PROJECT' },
  { label: '团队', value: 'TEAM' },
  { label: '个人', value: 'PERSON' },
  { label: '作品', value: 'WORK' },
  { label: '其他', value: 'OTHER' }
]
const submissionModeOptions = [
  { label: '开放填报', value: 'OPEN' },
  { label: '指定用户', value: 'ASSIGNED_USER' },
  { label: '业务导入', value: 'BUSINESS_IMPORTED' }
]
const roundTypeOptions = [
  { label: '材料评审', value: 'MATERIAL_REVIEW' },
  { label: '现场答辩', value: 'ONSITE_DEFENSE' },
  { label: '资格审核', value: 'QUALIFICATION_CHECK' },
  { label: '专家组评审', value: 'GROUP_REVIEW' },
  { label: '终评确认', value: 'FINAL_CONFIRM' }
]
const roundStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '未开始', value: 'NOT_STARTED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已结束', value: 'ENDED' },
  { label: '已归档', value: 'ARCHIVED' },
  { label: '已停用', value: 'DISABLED' }
]

function defaultForm() {
  return {
    id: undefined,
    activityName: '',
    activityCode: '',
    activityType: '',
    sourceModule: 'competition',
    sourceBizType: '',
    objectType: 'PROJECT',
    submissionMode: 'BUSINESS_IMPORTED',
    submitStartTime: '',
    submitDeadline: '',
    reviewStartTime: '',
    reviewEndTime: '',
    anonymousMode: 'NONE',
    resultPublishMode: 'NONE',
    status: 'DRAFT',
    description: ''
  }
}

function defaultRoundForm() {
  return {
    id: undefined,
    activityId: undefined,
    roundName: '',
    roundNo: 1,
    roundType: 'MATERIAL_REVIEW',
    startTime: '',
    endTime: '',
    ruleId: undefined,
    status: 'DRAFT',
    description: ''
  }
}

function resetForm() {
  Object.assign(form, defaultForm())
  activeTab.value = 'base'
  roundList.value = []
  formRef.value?.clearValidate()
}

function resetRoundForm() {
  Object.assign(roundForm, defaultRoundForm(), {
    activityId: form.id,
    roundNo: nextRoundNo()
  })
  roundFormRef.value?.clearValidate()
}

function getList() {
  loading.value = true
  listReviewActivity(queryParams).then(res => {
    activityList.value = res.rows || []
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
  detailMode.value = false
  dialogTitle.value = '新增评审活动'
  open.value = true
}

function handleDetail(row) {
  getReviewActivity(row.id).then(res => {
    resetForm()
    Object.assign(form, res.data || {})
    detailMode.value = true
    dialogTitle.value = '评审活动详情'
    loadRounds(form.id)
    open.value = true
  })
}

function handleEdit(row) {
  getReviewActivity(row.id).then(res => {
    resetForm()
    Object.assign(form, res.data || {})
    detailMode.value = false
    dialogTitle.value = '编辑评审活动'
    loadRounds(form.id)
    open.value = true
  })
}

function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    const isNew = !form.id
    const request = form.id ? updateReviewActivity(form.id, form) : addReviewActivity(form)
    request.then(res => {
      if (isNew && res.data?.id) {
        form.id = res.data.id
        dialogTitle.value = '编辑评审活动'
        activeTab.value = 'round'
        loadRounds(form.id)
        ElMessage.success('活动已保存，请继续配置评审轮次')
      } else {
        ElMessage.success('保存成功')
      }
      getList()
    })
  })
}

function loadRounds(activityId) {
  if (!activityId) {
    roundList.value = []
    return
  }
  roundLoading.value = true
  listReviewRound({
    activityId,
    pageNum: 1,
    pageSize: 200
  }).then(res => {
    roundList.value = (res.rows || []).sort((a, b) => {
      const noA = a.roundNo || 0
      const noB = b.roundNo || 0
      return noA === noB ? (a.id || 0) - (b.id || 0) : noA - noB
    })
  }).finally(() => {
    roundLoading.value = false
  })
}

function nextRoundNo() {
  return roundList.value.reduce((max, item) => Math.max(max, item.roundNo || 0), 0) + 1
}

function handleAddRound() {
  resetRoundForm()
  roundDialogTitle.value = '新增评审轮次'
  roundDialogOpen.value = true
}

function handleAddOnsiteRound() {
  resetRoundForm()
  roundForm.roundName = '现场答辩'
  roundForm.roundType = 'ONSITE_DEFENSE'
  roundForm.status = 'NOT_STARTED'
  roundDialogTitle.value = '新增现场答辩轮次'
  roundDialogOpen.value = true
}

function handleEditRound(row) {
  Object.assign(roundForm, defaultRoundForm(), row)
  roundDialogTitle.value = '编辑评审轮次'
  roundDialogOpen.value = true
}

function submitRoundForm() {
  roundFormRef.value.validate(valid => {
    if (!valid) return
    const payload = { ...roundForm, activityId: form.id }
    const request = payload.id ? updateReviewRound(payload.id, payload) : addReviewRound(payload)
    request.then(() => {
      ElMessage.success('保存成功')
      roundDialogOpen.value = false
      loadRounds(form.id)
    })
  })
}

function handleDeleteRound(row) {
  ElMessageBox.confirm(`确认删除轮次“${row.roundName}”吗？`, '删除确认', {
    type: 'warning'
  }).then(() => delReviewRound(row.id)).then(() => {
    ElMessage.success('删除成功')
    loadRounds(form.id)
  }).catch(() => {})
}

function handleImport(row) {
  router.push({ path: '/review/import', query: { activityId: row.id } })
}

function handleObjects(row) {
  router.push({ path: '/review/object', query: { activityId: row.id } })
}

function handleAssignments(row) {
  router.push({ path: '/review/assignment', query: { activityId: row.id } })
}

function handleCloseSubmission(row) {
  ElMessageBox.confirm('确认关闭该活动填报？已提交对象将锁定，未完成提交对象将作废。', '关闭填报确认', {
    type: 'warning'
  }).then(() => {
    return closeSubmission(row.id)
  }).then(res => {
    ElMessage.success(res.data?.message || '关闭填报完成')
    getList()
  }).catch(() => {})
}

function optionLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function statusTag(status) {
  const item = activityStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

onMounted(getList)
</script>
