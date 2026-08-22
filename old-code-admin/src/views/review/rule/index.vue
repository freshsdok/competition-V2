<template>
  <div class="app-container review-rule-page">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="92px">
      <el-form-item label="评审活动" prop="activityId">
        <el-select v-model="queryParams.activityId" placeholder="请选择活动" clearable filterable style="width: 240px" @change="handleActivityFilterChange">
          <el-option v-for="item in activityOptions" :key="item.id" :label="item.activityName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="评审轮次" prop="roundId">
        <el-select v-model="queryParams.roundId" placeholder="请选择轮次" clearable filterable style="width: 200px">
          <el-option v-for="item in filteredRoundOptions(queryParams.activityId)" :key="item.id" :label="item.roundName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model.trim="queryParams.ruleName" placeholder="请输入规则名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="计算方式" prop="scoreMode">
        <el-select v-model="queryParams.scoreMode" placeholder="请选择" clearable style="width: 160px">
          <el-option v-for="item in scoreModeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否启用" prop="enabled">
        <el-select v-model="queryParams.enabled" placeholder="请选择" clearable style="width: 120px">
          <el-option label="启用" value="Y" />
          <el-option label="停用" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['competition:review:rule:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="ruleList" stripe>
      <el-table-column label="规则名称" prop="ruleName" min-width="180" show-overflow-tooltip />
      <el-table-column label="所属活动" prop="activityId" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ activityName(row.activityId) }}</template>
      </el-table-column>
      <el-table-column label="所属轮次" prop="roundId" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ roundName(row.roundId) }}</template>
      </el-table-column>
      <el-table-column label="计算方式" prop="scoreMode" width="140">
        <template #default="{ row }">{{ optionLabel(scoreModeOptions, row.scoreMode) }}</template>
      </el-table-column>
      <el-table-column label="总分" prop="totalScore" width="100" />
      <el-table-column label="匿名模式" prop="anonymousMode" width="120">
        <template #default="{ row }">{{ row.anonymousMode === 'FIELD_ONLY' ? '字段匿名' : '不匿名' }}</template>
      </el-table-column>
      <el-table-column label="是否启用" prop="enabled" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 'Y' ? 'success' : 'info'">{{ row.enabled === 'Y' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="430" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleEdit(row)" v-hasPermi="['competition:review:rule:edit']">编辑</el-button>
          <el-button link type="success" icon="Operation" @click="openCriteria(row)" v-hasPermi="['competition:review:criteria:list']">配置指标</el-button>
          <el-button link type="warning" icon="Check" @click="handleValidate(row)" v-hasPermi="['competition:review:rule:query']">校验</el-button>
          <el-button v-if="row.enabled !== 'Y'" link type="success" icon="CircleCheck" @click="handleEnable(row)" v-hasPermi="['competition:review:rule:edit']">启用</el-button>
          <el-button v-else link type="info" icon="CircleClose" @click="handleDisable(row)" v-hasPermi="['competition:review:rule:edit']">停用</el-button>
          <el-button link type="primary" icon="CopyDocument" @click="handleCopy(row)" v-hasPermi="['competition:review:rule:add']">复制</el-button>
          <el-button link type="primary" icon="Link" @click="openBind(row)" v-hasPermi="['competition:review:round:edit']">绑定轮次</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)" v-hasPermi="['competition:review:rule:remove']">删除</el-button>
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

    <el-dialog :title="ruleDialogTitle" v-model="ruleDialogOpen" width="760px" append-to-body>
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleRules" label-width="112px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="规则名称" prop="ruleName">
              <el-input v-model.trim="ruleForm.ruleName" maxlength="200" placeholder="请输入规则名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属活动" prop="activityId">
              <el-select v-model="ruleForm.activityId" :disabled="!!ruleForm.id" filterable style="width: 100%" @change="ruleForm.roundId = undefined">
                <el-option v-for="item in activityOptions" :key="item.id" :label="item.activityName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属轮次">
              <el-select v-model="ruleForm.roundId" :disabled="!!ruleForm.id" clearable filterable style="width: 100%">
                <el-option v-for="item in filteredRoundOptions(ruleForm.activityId)" :key="item.id" :label="item.roundName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计算方式" prop="scoreMode">
              <el-select v-model="ruleForm.scoreMode" style="width: 100%">
                <el-option v-for="item in scoreModeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总分" prop="totalScore">
              <el-input-number v-model="ruleForm.totalScore" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="匿名模式">
              <el-select v-model="ruleForm.anonymousMode" style="width: 100%">
                <el-option label="不匿名" value="NONE" />
                <el-option label="字段匿名" value="FIELD_ONLY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-alert :title="scoreModeHint(ruleForm.scoreMode)" type="info" show-icon :closable="false" />
          </el-col>
          <el-col :span="24">
            <el-form-item label="规则说明" class="mt16">
              <el-input v-model="ruleForm.description" type="textarea" :rows="3" maxlength="1000" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogOpen = false">取消</el-button>
        <el-button type="primary" @click="submitRule">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="criteriaDrawerOpen" size="76%" :title="criteriaDrawerTitle" append-to-body>
      <div class="criteria-toolbar">
        <el-button type="primary" icon="Plus" @click="handleCriteriaAdd" v-hasPermi="['competition:review:criteria:add']">新增指标</el-button>
        <el-button icon="Check" @click="validateActiveRule">校验规则</el-button>
      </div>

      <el-alert v-if="validateResult" :type="validateResult.valid ? 'success' : 'error'" show-icon :closable="false" class="mb16">
        <template #title>
          {{ validateResult.valid ? '规则校验通过' : '规则校验未通过' }}
        </template>
        <div class="validate-lines">
          <div>计算方式：{{ optionLabel(scoreModeOptions, validateResult.scoreMode) }}；总分：{{ validateResult.totalScore ?? '-' }}；最高分合计：{{ validateResult.maxScoreSum ?? 0 }}；权重合计：{{ validateResult.weightSum ?? 0 }}</div>
          <div v-for="item in validateResult.errors || []" :key="'e' + item" class="error-line">{{ item }}</div>
          <div v-for="item in validateResult.warnings || []" :key="'w' + item" class="warning-line">{{ item }}</div>
        </div>
      </el-alert>

      <el-table v-loading="criteriaLoading" :data="criteriaList" stripe>
        <el-table-column label="排序" prop="sortOrder" width="80" />
        <el-table-column label="指标名称" prop="criteriaName" min-width="180" show-overflow-tooltip />
        <el-table-column label="指标类型" prop="scoreType" width="130">
          <template #default="{ row }">{{ optionLabel(criteriaTypeOptions, row.scoreType) }}</template>
        </el-table-column>
        <el-table-column label="最低分" prop="minScore" width="100" />
        <el-table-column label="最高分" prop="maxScore" width="100" />
        <el-table-column label="权重" prop="weight" width="100" />
        <el-table-column label="必填" prop="required" width="90" align="center">
          <template #default="{ row }">{{ row.required === 'Y' ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="启用" prop="enabled" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 'Y' ? 'success' : 'info'">{{ row.enabled === 'Y' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340" fixed="right" align="center">
          <template #default="{ row, $index }">
            <el-button link type="primary" icon="Edit" @click="handleCriteriaEdit(row)" v-hasPermi="['competition:review:criteria:edit']">编辑</el-button>
            <el-button link icon="Top" :disabled="$index === 0" @click="moveCriteria($index, -1)">上移</el-button>
            <el-button link icon="Bottom" :disabled="$index === criteriaList.length - 1" @click="moveCriteria($index, 1)">下移</el-button>
            <el-button v-if="row.enabled !== 'Y'" link type="success" icon="CircleCheck" @click="toggleCriteria(row, 'Y')" v-hasPermi="['competition:review:criteria:edit']">启用</el-button>
            <el-button v-else link type="info" icon="CircleClose" @click="toggleCriteria(row, 'N')" v-hasPermi="['competition:review:criteria:edit']">停用</el-button>
            <el-button link type="danger" icon="Delete" @click="handleCriteriaDelete(row)" v-hasPermi="['competition:review:criteria:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog :title="criteriaDialogTitle" v-model="criteriaDialogOpen" width="820px" append-to-body>
      <el-form ref="criteriaFormRef" :model="criteriaForm" :rules="criteriaRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="指标名称" prop="criteriaName">
              <el-input v-model.trim="criteriaForm.criteriaName" maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="指标类型" prop="scoreType">
              <el-select v-model="criteriaForm.scoreType" style="width: 100%" @change="handleCriteriaTypeChange">
                <el-option v-for="item in criteriaTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="criteriaForm.scoreType === 'NUMBER'" :span="12">
            <el-form-item label="最低分">
              <el-input-number v-model="criteriaForm.minScore" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col v-if="criteriaForm.scoreType === 'NUMBER'" :span="12">
            <el-form-item label="最高分" prop="maxScore">
              <el-input-number v-model="criteriaForm.maxScore" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col v-if="criteriaForm.scoreType !== 'TEXT'" :span="12">
            <el-form-item label="权重">
              <el-input-number v-model="criteriaForm.weight" :min="0" :precision="4" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="criteriaForm.sortOrder" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否必填">
              <el-switch v-model="criteriaForm.required" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用">
              <el-switch v-model="criteriaForm.enabled" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="指标说明">
              <el-input v-model="criteriaForm.criteriaDesc" type="textarea" :rows="3" maxlength="1000" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="criteriaForm.scoreType === 'SINGLE_CHOICE'">
          <el-divider content-position="left">单选项</el-divider>
          <el-table :data="optionRows" border>
            <el-table-column label="选项名称" min-width="150">
              <template #default="{ row }">
                <el-input v-model.trim="row.label" placeholder="如 优秀" />
              </template>
            </el-table-column>
            <el-table-column label="选项值" min-width="120">
              <template #default="{ row }">
                <el-input v-model.trim="row.value" placeholder="如 A" />
              </template>
            </el-table-column>
            <el-table-column label="分值" width="150">
              <template #default="{ row }">
                <el-input-number v-model="row.score" :precision="2" :step="1" style="width: 120px" />
              </template>
            </el-table-column>
            <el-table-column label="排序" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.sortOrder" :min="0" :step="1" style="width: 90px" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" icon="Delete" @click="removeOption($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="option-actions">
            <el-button icon="Plus" @click="addOption">添加选项</el-button>
          </div>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="criteriaDialogOpen = false">取消</el-button>
        <el-button type="primary" @click="submitCriteria">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog title="绑定评审轮次" v-model="bindDialogOpen" width="520px" append-to-body>
      <el-form label-width="96px">
        <el-form-item label="评分规则">
          <el-input :model-value="activeRule?.ruleName" disabled />
        </el-form-item>
        <el-form-item label="评审轮次">
          <el-select v-model="bindRoundId" filterable style="width: 100%">
            <el-option v-for="item in filteredRoundOptions(activeRule?.activityId)" :key="item.id" :label="item.roundName" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindDialogOpen = false">取消</el-button>
        <el-button type="primary" @click="submitBind">绑定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import RightToolbar from '@/components/RightToolbar'
import { listReviewActivity } from '@/api/review/activity'
import { bindReviewRoundRule, listReviewRound } from '@/api/review/round'
import {
  addReviewRule,
  copyReviewRule,
  delReviewRule,
  disableReviewRule,
  enableReviewRule,
  getReviewRule,
  listReviewRule,
  updateReviewRule,
  validateReviewRule
} from '@/api/review/rule'
import {
  addReviewCriteria,
  delReviewCriteria,
  listReviewCriteria,
  updateReviewCriteria
} from '@/api/review/criteria'

const showSearch = ref(true)
const loading = ref(false)
const criteriaLoading = ref(false)
const total = ref(0)
const ruleList = ref([])
const criteriaList = ref([])
const activityOptions = ref([])
const roundOptions = ref([])
const queryRef = ref(null)
const ruleFormRef = ref(null)
const criteriaFormRef = ref(null)
const ruleDialogOpen = ref(false)
const criteriaDrawerOpen = ref(false)
const criteriaDialogOpen = ref(false)
const bindDialogOpen = ref(false)
const ruleDialogTitle = ref('新增评分规则')
const criteriaDialogTitle = ref('新增评分指标')
const activeRule = ref(null)
const validateResult = ref(null)
const bindRoundId = ref(undefined)
const optionRows = ref([])

const scoreModeOptions = [
  { label: '求和', value: 'SUM' },
  { label: '加权求和', value: 'WEIGHTED_SUM' },
  { label: '平均分', value: 'AVERAGE' }
]
const criteriaTypeOptions = [
  { label: '数字评分', value: 'NUMBER' },
  { label: '单选赋分', value: 'SINGLE_CHOICE' },
  { label: '文本评价', value: 'TEXT' }
]

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  activityId: undefined,
  roundId: undefined,
  ruleName: '',
  scoreMode: '',
  enabled: ''
})

const ruleForm = reactive(defaultRuleForm())
const criteriaForm = reactive(defaultCriteriaForm())

const ruleRules = {
  ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
  activityId: [{ required: true, message: '所属活动不能为空', trigger: 'change' }],
  scoreMode: [{ required: true, message: '计算方式不能为空', trigger: 'change' }],
  totalScore: [{ required: true, message: '总分不能为空', trigger: 'blur' }]
}

const criteriaRules = {
  criteriaName: [{ required: true, message: '指标名称不能为空', trigger: 'blur' }],
  scoreType: [{ required: true, message: '指标类型不能为空', trigger: 'change' }],
  maxScore: [{ required: true, message: '最高分不能为空', trigger: 'blur' }]
}

function defaultRuleForm() {
  return {
    id: undefined,
    activityId: undefined,
    roundId: undefined,
    ruleName: '',
    scoreMode: 'SUM',
    totalScore: 100,
    anonymousMode: 'NONE',
    enabled: 'N',
    description: ''
  }
}

function defaultCriteriaForm() {
  return {
    id: undefined,
    ruleId: undefined,
    criteriaName: '',
    criteriaDesc: '',
    scoreType: 'NUMBER',
    minScore: 0,
    maxScore: 10,
    weight: undefined,
    required: 'Y',
    enabled: 'Y',
    sortOrder: nextSortOrder(),
    optionsJson: ''
  }
}

function loadActivities() {
  return listReviewActivity({ pageNum: 1, pageSize: 500 }).then(res => {
    activityOptions.value = res.rows || []
  })
}

function loadRounds() {
  return listReviewRound({ pageNum: 1, pageSize: 500 }).then(res => {
    roundOptions.value = res.rows || []
  })
}

function getList() {
  loading.value = true
  listReviewRule(queryParams).then(res => {
    ruleList.value = res.rows || []
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

function handleActivityFilterChange() {
  queryParams.roundId = undefined
}

function handleAdd() {
  Object.assign(ruleForm, defaultRuleForm())
  ruleDialogTitle.value = '新增评分规则'
  ruleDialogOpen.value = true
}

function handleEdit(row) {
  getReviewRule(row.id).then(res => {
    Object.assign(ruleForm, defaultRuleForm(), res.data || {})
    ruleDialogTitle.value = '编辑评分规则'
    ruleDialogOpen.value = true
  })
}

function submitRule() {
  ruleFormRef.value?.validate(valid => {
    if (!valid) return
    const payload = { ...ruleForm }
    if (payload.id) {
      delete payload.enabled
    } else {
      payload.enabled = 'N'
    }
    const action = payload.id ? updateReviewRule(payload.id, payload) : addReviewRule(payload)
    action.then(() => {
      ElMessage.success('保存成功')
      ruleDialogOpen.value = false
      getList()
    })
  })
}

function handleValidate(row) {
  validateReviewRule(row.id).then(res => {
    validateResult.value = res.data
    if (res.data?.valid) {
      ElMessage.success('规则校验通过')
    } else {
      ElMessage.error('规则校验未通过')
    }
  })
}

function handleEnable(row) {
  enableReviewRule(row.id).then(() => {
    ElMessage.success('启用成功')
    getList()
    refreshActiveRule(row.id)
  })
}

function handleDisable(row) {
  disableReviewRule(row.id).then(() => {
    ElMessage.success('停用成功')
    getList()
    refreshActiveRule(row.id)
  })
}

function handleCopy(row) {
  copyReviewRule(row.id).then(() => {
    ElMessage.success('复制成功')
    getList()
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除评分规则“${row.ruleName}”？`, '提示', { type: 'warning' }).then(() => {
    delReviewRule(row.id).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

function openCriteria(row) {
  activeRule.value = row
  criteriaDrawerOpen.value = true
  validateResult.value = null
  loadCriteria(row.id)
}

function loadCriteria(ruleId) {
  criteriaLoading.value = true
  listReviewCriteria({ pageNum: 1, pageSize: 500, ruleId }).then(res => {
    criteriaList.value = (res.rows || []).sort(compareCriteria)
  }).finally(() => {
    criteriaLoading.value = false
  })
}

function handleCriteriaAdd() {
  Object.assign(criteriaForm, defaultCriteriaForm(), {
    ruleId: activeRule.value?.id,
    sortOrder: nextSortOrder()
  })
  optionRows.value = defaultOptionRows()
  criteriaDialogTitle.value = '新增评分指标'
  criteriaDialogOpen.value = true
}

function handleCriteriaEdit(row) {
  Object.assign(criteriaForm, defaultCriteriaForm(), { ...row })
  optionRows.value = parseOptions(row.optionsJson)
  criteriaDialogTitle.value = '编辑评分指标'
  criteriaDialogOpen.value = true
}

function submitCriteria() {
  criteriaFormRef.value?.validate(valid => {
    if (!valid) return
    const payload = buildCriteriaPayload()
    if (!payload) return
    const action = payload.id ? updateReviewCriteria(payload.id, payload) : addReviewCriteria(payload)
    action.then(() => {
      ElMessage.success('保存成功')
      criteriaDialogOpen.value = false
      loadCriteria(activeRule.value.id)
      validateResult.value = null
    })
  })
}

function buildCriteriaPayload() {
  const payload = { ...criteriaForm }
  if (payload.scoreType === 'SINGLE_CHOICE') {
    if (!validateOptionRows()) return null
    payload.optionsJson = JSON.stringify(optionRows.value.map(item => ({
      label: item.label,
      value: item.value,
      score: Number(item.score),
      sortOrder: item.sortOrder
    })))
    payload.minScore = undefined
    payload.maxScore = undefined
  } else if (payload.scoreType === 'TEXT') {
    payload.minScore = undefined
    payload.maxScore = undefined
    payload.weight = undefined
    payload.optionsJson = ''
  } else {
    payload.optionsJson = ''
    if (payload.minScore == null) payload.minScore = 0
  }
  return payload
}

function validateOptionRows() {
  if (optionRows.value.length === 0) {
    ElMessage.error('请至少配置一个单选项')
    return false
  }
  const values = new Set()
  for (const item of optionRows.value) {
    if (!item.label || !item.value) {
      ElMessage.error('单选项名称和值不能为空')
      return false
    }
    if (values.has(item.value)) {
      ElMessage.error(`单选项值重复：${item.value}`)
      return false
    }
    values.add(item.value)
    if (item.score === undefined || item.score === null || Number.isNaN(Number(item.score))) {
      ElMessage.error(`单选项分值无效：${item.value}`)
      return false
    }
  }
  return true
}

function toggleCriteria(row, enabled) {
  updateReviewCriteria(row.id, { enabled }).then(() => {
    ElMessage.success(enabled === 'Y' ? '启用成功' : '停用成功')
    loadCriteria(activeRule.value.id)
    validateResult.value = null
  })
}

function handleCriteriaDelete(row) {
  ElMessageBox.confirm(`确认删除评分指标“${row.criteriaName}”？`, '提示', { type: 'warning' }).then(() => {
    delReviewCriteria(row.id).then(() => {
      ElMessage.success('删除成功')
      loadCriteria(activeRule.value.id)
      validateResult.value = null
    })
  }).catch(() => {})
}

function moveCriteria(index, direction) {
  const targetIndex = index + direction
  if (targetIndex < 0 || targetIndex >= criteriaList.value.length) return
  const current = criteriaList.value[index]
  const target = criteriaList.value[targetIndex]
  const currentSort = current.sortOrder ?? (index + 1) * 10
  const targetSort = target.sortOrder ?? (targetIndex + 1) * 10
  Promise.all([
    updateReviewCriteria(current.id, { sortOrder: targetSort }),
    updateReviewCriteria(target.id, { sortOrder: currentSort })
  ]).then(() => {
    loadCriteria(activeRule.value.id)
  })
}

function validateActiveRule() {
  if (!activeRule.value?.id) return
  validateReviewRule(activeRule.value.id).then(res => {
    validateResult.value = res.data
  })
}

function openBind(row) {
  activeRule.value = row
  bindRoundId.value = row.roundId
  bindDialogOpen.value = true
}

function submitBind() {
  if (!bindRoundId.value) {
    ElMessage.error('请选择评审轮次')
    return
  }
  bindReviewRoundRule(bindRoundId.value, activeRule.value.id).then(() => {
    ElMessage.success('绑定成功')
    bindDialogOpen.value = false
    loadRounds().then(getList)
  })
}

function addOption() {
  optionRows.value.push({ label: '', value: '', score: 0, sortOrder: optionRows.value.length + 1 })
}

function removeOption(index) {
  optionRows.value.splice(index, 1)
}

function handleCriteriaTypeChange(type) {
  if (type === 'SINGLE_CHOICE' && optionRows.value.length === 0) {
    optionRows.value = defaultOptionRows()
  }
}

function refreshActiveRule(ruleId) {
  if (!activeRule.value || activeRule.value.id !== ruleId) return
  getReviewRule(ruleId).then(res => {
    activeRule.value = res.data
  })
}

function filteredRoundOptions(activityId) {
  if (!activityId) return roundOptions.value
  return roundOptions.value.filter(item => item.activityId === activityId)
}

function activityName(activityId) {
  const item = activityOptions.value.find(activity => activity.id === activityId)
  return item?.activityName || activityId || '-'
}

function roundName(roundId) {
  const item = roundOptions.value.find(round => round.id === roundId)
  return item?.roundName || '-'
}

function optionLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item?.label || value || '-'
}

function scoreModeHint(mode) {
  if (mode === 'WEIGHTED_SUM') {
    return '加权求和：总分 = Σ 指标得分 × 权重 / 100。权重请按百分比填写，例如 40、30、30，权重合计必须为 100。'
  }
  if (mode === 'AVERAGE') {
    return '平均分：可计分指标取平均值，文本评价不参与总分，规则总分仅作参考。'
  }
  return '求和：各可计分指标直接求和，启用或绑定前最高分合计必须等于规则总分。'
}

function parseOptions(optionsJson) {
  if (!optionsJson) return defaultOptionRows()
  try {
    const parsed = JSON.parse(optionsJson)
    if (!Array.isArray(parsed) || parsed.length === 0) return defaultOptionRows()
    return parsed.map((item, index) => ({
      label: item.label || '',
      value: item.value || '',
      score: Number(item.score ?? 0),
      sortOrder: item.sortOrder ?? index + 1
    }))
  } catch (e) {
    return defaultOptionRows()
  }
}

function defaultOptionRows() {
  return [
    { label: '优秀', value: 'A', score: 20, sortOrder: 1 },
    { label: '良好', value: 'B', score: 15, sortOrder: 2 },
    { label: '一般', value: 'C', score: 10, sortOrder: 3 }
  ]
}

function nextSortOrder() {
  if (!criteriaList.value.length) return 10
  return Math.max(...criteriaList.value.map(item => item.sortOrder || 0)) + 10
}

function compareCriteria(a, b) {
  const sortCompare = (a.sortOrder ?? 999999) - (b.sortOrder ?? 999999)
  return sortCompare || ((a.id || 0) - (b.id || 0))
}

onMounted(() => {
  Promise.all([loadActivities(), loadRounds()]).then(getList)
})
</script>

<style scoped>
.review-rule-page .mb8 {
  margin-bottom: 8px;
}

.criteria-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.mb16 {
  margin-bottom: 16px;
}

.mt16 {
  margin-top: 16px;
}

.validate-lines {
  line-height: 1.8;
}

.error-line {
  color: var(--el-color-danger);
}

.warning-line {
  color: var(--el-color-warning);
}

.option-actions {
  margin-top: 12px;
}
</style>
