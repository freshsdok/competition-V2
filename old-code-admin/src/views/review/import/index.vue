<template>
  <div class="app-container">
    <el-alert
      title="导入预览不会写入数据库；执行导入会创建或同步评审对象、成员、填报权限、外部关联、参赛证映射和文件任务材料。"
      type="info"
      show-icon
      :closable="false"
      class="mb16"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="132px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="评审活动" prop="activityId">
            <el-select v-model="form.activityId" placeholder="请选择评审活动" filterable style="width: 100%">
              <el-option
                v-for="item in activityOptions"
                :key="item.id"
                :label="`${item.activityName || '-'}（${item.activityCode || item.id}）`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="来源模块">
            <el-input v-model.trim="form.sourceModule" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="匹配方式" prop="sourceBizType">
            <el-select v-model="form.sourceBizType" style="width: 100%">
              <el-option v-for="item in sourceBizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="isDefenseScheduleSource">
          <el-form-item label="赛事系列" prop="competitionSeriesId">
            <el-select v-model="form.competitionSeriesId" placeholder="请选择赛事" clearable filterable style="width: 100%">
              <el-option
                v-for="item in competitionOptions"
                :key="item.competitionSeriesId"
                :label="item.competitionName"
                :value="item.competitionSeriesId"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="isFileTaskMaterialSource">
          <el-form-item label="文件任务" prop="fileTaskId">
            <el-select v-model="form.fileTaskId" placeholder="请选择文件任务" clearable filterable style="width: 100%">
              <el-option
                v-for="item in fileTaskOptions"
                :key="item.id"
                :label="`${item.taskName || '-'}（${item.id}）`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="评审对象类型">
            <el-select v-model="form.defaultObjectType" style="width: 100%">
              <el-option label="项目 PROJECT" value="PROJECT" />
              <el-option label="团队 TEAM" value="TEAM" />
              <el-option label="作品 WORK" value="WORK" />
              <el-option label="其他 OTHER" value="OTHER" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="填报授权模式">
            <el-select v-model="form.permissionUserMode" style="width: 100%">
              <el-option label="负责人/联系人" value="LEADER" />
              <el-option label="仅联系人" value="CONTACT" />
              <el-option label="全体成员" value="ALL_MEMBERS" />
              <el-option label="指定用户" value="SPECIFIED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="导入后状态">
            <el-select v-model="form.initialSubmitStatus" clearable placeholder="默认草稿" style="width: 100%">
              <el-option label="草稿 DRAFT" value="DRAFT" />
              <el-option label="锁定 LOCKED" value="LOCKED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="form.permissionUserMode === 'SPECIFIED'">
          <el-form-item label="指定授权用户">
            <el-input
              v-model.trim="specifiedUserIdsText"
              placeholder="请输入用户ID，多个用逗号或换行分隔"
              type="textarea"
              :rows="2"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item :label="sourceBizIdLabel" prop="sourceBizIdText">
            <el-input
              v-model="form.sourceBizIdText"
              :placeholder="sourceBizIdPlaceholder"
              type="textarea"
              :rows="isDefenseScheduleSource ? 8 : 4"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8" v-if="isFileTaskMaterialSource">
          <el-form-item label="只导入已提交">
            <el-switch v-model="form.submittedOnly" active-text="是" inactive-text="否" />
          </el-form-item>
        </el-col>
        <el-col :span="8" v-if="isFileTaskMaterialSource">
          <el-form-item label="同步上传文件">
            <el-switch v-model="form.syncMaterial" active-text="同步" inactive-text="不同步" />
          </el-form-item>
        </el-col>
        <el-col :span="8" v-if="isFileTaskMaterialSource">
          <el-form-item label="同链接材料">
            <el-select v-model="form.materialOverwriteMode" clearable placeholder="保留已有" style="width: 100%">
              <el-option label="保留已有" value="" />
              <el-option label="覆盖同步" value="REPLACE_BY_SOURCE" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="覆盖已存在对象">
            <el-switch v-model="form.overwriteExisting" active-text="覆盖同步" inactive-text="重复跳过" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="同步参赛证">
            <el-switch v-model="form.syncCertificate" active-text="同步" inactive-text="不同步" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button type="primary" icon="View" :loading="previewLoading" @click="handlePreview">预览导入</el-button>
        <el-button type="success" icon="Upload" :loading="importLoading" @click="handleImport" v-hasPermi="['competition:review:object:import']">导入选中</el-button>
        <el-button icon="Refresh" @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">导入预览</el-divider>
    <div class="preview-toolbar">
      <el-form :inline="true" :model="previewSearchForm">
        <el-form-item label="团队编号">
          <el-input
            v-model.trim="previewSearchForm.teamCode"
            placeholder="请输入团队编号"
            clearable
            @keyup.enter="handlePreviewQuery"
          />
        </el-form-item>
        <el-form-item label="团队名称">
          <el-input
            v-model.trim="previewSearchForm.teamName"
            placeholder="请输入团队名称"
            clearable
            @keyup.enter="handlePreviewQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handlePreviewQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetPreviewQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="preview-selection">
        <span>已选 {{ selectedImportCount }} 条 / 当前 {{ filteredPreviewList.length }} 条</span>
        <el-button link type="primary" :disabled="selectedImportCount === 0" @click="clearPreviewSelection">清空选择</el-button>
      </div>
    </div>
    <el-table
      ref="previewTableRef"
      v-loading="previewLoading"
      :data="filteredPreviewList"
      :row-key="previewRowKey"
      stripe
      @selection-change="handlePreviewSelectionChange"
    >
      <el-table-column
        type="selection"
        width="48"
        align="center"
        :reserve-selection="true"
        :selectable="isPreviewSelectable"
      />
      <el-table-column label="业务ID" prop="sourceBizId" min-width="120" show-overflow-tooltip />
      <el-table-column label="匹配方式" prop="sourceBizType" width="160">
        <template #default="{ row }">{{ sourceBizTypeLabel(row.sourceBizType) }}</template>
      </el-table-column>
      <el-table-column v-if="hasDefensePreview" label="答辩顺序" prop="defenseOrder" width="100" align="center" />
      <el-table-column v-if="hasDefensePreview" label="输入单位" prop="inputOrgName" min-width="160" show-overflow-tooltip />
      <el-table-column v-if="hasDefensePreview" label="输入队名" prop="inputTeamName" min-width="160" show-overflow-tooltip />
      <el-table-column v-if="hasDefensePreview" label="输入负责人" prop="inputLeaderName" min-width="120" show-overflow-tooltip />
      <el-table-column label="团队编号" prop="teamCode" min-width="120" show-overflow-tooltip />
      <el-table-column label="团队名称" prop="teamName" min-width="160" show-overflow-tooltip />
      <el-table-column label="预计对象名称" prop="objectName" min-width="180" show-overflow-tooltip />
      <el-table-column label="负责人/联系人" prop="leaderName" min-width="140" show-overflow-tooltip />
      <el-table-column label="成员数" prop="memberCount" width="90" align="center" />
      <el-table-column label="证件数" prop="certificateCount" width="90" align="center" />
      <el-table-column label="材料数" prop="materialCount" width="90" align="center" />
      <el-table-column label="预计授权用户" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ (row.permissionUsers || []).join('，') || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.canImport ? 'success' : 'warning'">{{ row.canImport ? '可导入' : '需处理' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提示" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ (row.warnings || []).join('；') || '-' }}</template>
      </el-table-column>
    </el-table>

    <el-divider content-position="left">导入结果</el-divider>
    <el-descriptions v-if="importResult" :column="4" border>
      <el-descriptions-item label="请求数量">{{ importResult.totalCount }}</el-descriptions-item>
      <el-descriptions-item label="成功数量">{{ importResult.successCount }}</el-descriptions-item>
      <el-descriptions-item label="跳过数量">{{ importResult.skipCount }}</el-descriptions-item>
      <el-descriptions-item label="失败数量">{{ importResult.failedCount }}</el-descriptions-item>
      <el-descriptions-item label="新建/同步对象" :span="2">
        <el-button
          v-for="id in importResult.createdObjectIds || []"
          :key="id"
          link
          type="primary"
          @click="goObjectDetail(id)"
        >
          {{ id }}
        </el-button>
        <span v-if="!importResult.createdObjectIds || importResult.createdObjectIds.length === 0">-</span>
      </el-descriptions-item>
      <el-descriptions-item label="消息" :span="2">{{ importResult.message || '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-row :gutter="16" v-if="importResult" class="mt16">
      <el-col :span="12">
        <el-table :data="toItemRows(importResult.skippedItems)" size="small" border>
          <el-table-column label="跳过项" prop="message" />
        </el-table>
      </el-col>
      <el-col :span="12">
        <el-table :data="toItemRows(importResult.failedItems)" size="small" border>
          <el-table-column label="失败项" prop="message" />
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listReviewActivity } from '@/api/review/activity'
import { importFromBusiness, importPreview } from '@/api/review/object'
import { getLists as listFileTask } from '@/api/fileTask/task'
import { getSelectCompetitionList } from '@/api/certInterconnect/certConfig'

const FILE_UPLOAD_MANAGER = 'FILE_UPLOAD_MANAGER'
const DEFENSE_SCHEDULE = 'DEFENSE_SCHEDULE'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const previewTableRef = ref(null)
const previewLoading = ref(false)
const importLoading = ref(false)
const activityOptions = ref([])
const fileTaskOptions = ref([])
const competitionOptions = ref([])
const previewList = ref([])
const importResult = ref(null)
const specifiedUserIdsText = ref('')
const selectedPreviewRows = ref([])
const lastPreviewSourceKey = ref('')

const form = reactive(defaultForm())
const previewSearchForm = reactive({
  teamCode: '',
  teamName: ''
})
const previewQuery = reactive({
  teamCode: '',
  teamName: ''
})

const sourceBizTypeOptions = [
  { label: '团队编号', value: 'TEAM' },
  { label: '报名ID', value: 'REGISTRATION' },
  { label: '报名teamCode', value: 'REGISTRATION_TEAM_CODE' },
  { label: '文件任务上传', value: FILE_UPLOAD_MANAGER },
  { label: '答辩安排表', value: DEFENSE_SCHEDULE }
]

const isFileTaskSource = computed(() => form.sourceBizType === FILE_UPLOAD_MANAGER)
const isDefenseScheduleSource = computed(() => form.sourceBizType === DEFENSE_SCHEDULE)
const isFileTaskMaterialSource = computed(() => isFileTaskSource.value || isDefenseScheduleSource.value)
const hasDefensePreview = computed(() => isDefenseScheduleSource.value || previewList.value.some(row => row && row.sourceBizType === DEFENSE_SCHEDULE))

const filteredPreviewList = computed(() => {
  const teamCode = normalizeKeyword(previewQuery.teamCode)
  const teamName = normalizeKeyword(previewQuery.teamName)
  return previewList.value.filter(row => {
    const codeMatched = !teamCode || normalizeKeyword(row.teamCode).includes(teamCode)
    const nameMatched = !teamName || [row.teamName, row.objectName, row.inputTeamName].some(value => normalizeKeyword(value).includes(teamName))
    return codeMatched && nameMatched
  })
})

const selectedImportRows = computed(() => selectedPreviewRows.value.filter(row => row && row.canImport))
const selectedImportCount = computed(() => selectedImportRows.value.length)

const sourceBizIdLabel = computed(() => {
  if (isFileTaskSource.value) return '上传记录ID列表'
  if (isDefenseScheduleSource.value) return '答辩安排表'
  if (form.sourceBizType === 'REGISTRATION') return '报名ID列表'
  if (form.sourceBizType === 'REGISTRATION_TEAM_CODE') return '报名teamCode列表'
  return '团队编号列表'
})

const sourceBizIdPlaceholder = computed(() => {
  if (isFileTaskSource.value) {
    return '可选。为空时按所选文件任务导入；填写时按 file_upload_manager.id 精确导入，多个可用换行、空格或逗号分隔'
  }
  if (isDefenseScheduleSource.value) {
    return '从答辩安排表粘贴数据，每行一个对象：答辩顺序\t所属单位\t队名\t负责人姓名'
  }
  if (form.sourceBizType === 'REGISTRATION') {
    return '请输入报名 memberId，多个可用换行、空格或逗号分隔'
  }
  if (form.sourceBizType === 'REGISTRATION_TEAM_CODE') {
    return '请输入报名数据中的 teamCode，多个可用换行、空格或逗号分隔'
  }
  return '请输入团队编号或团队ID，多个可用换行、空格或逗号分隔'
})

const rules = {
  activityId: [{ required: true, message: '请选择评审活动', trigger: 'change' }],
  sourceBizType: [{ required: true, message: '请选择匹配方式', trigger: 'change' }],
  competitionSeriesId: [{ validator: validateDefenseCompetition, trigger: 'change' }],
  fileTaskId: [{ validator: validateImportSource, trigger: 'change' }],
  sourceBizIdText: [{ validator: validateImportSource, trigger: 'blur' }]
}

function defaultForm() {
  return {
    activityId: undefined,
    sourceModule: 'competition',
    sourceBizType: 'TEAM',
    competitionSeriesId: undefined,
    fileTaskId: undefined,
    submittedOnly: true,
    sourceBizIdText: '',
    defaultObjectType: 'PROJECT',
    permissionUserMode: 'LEADER',
    overwriteExisting: false,
    syncCertificate: false,
    syncMaterial: false,
    initialSubmitStatus: '',
    materialOverwriteMode: ''
  }
}

function resetForm() {
  Object.assign(form, defaultForm())
  applyRouteDefaults()
  syncSourceModule()
  if (isFileTaskMaterialSource.value) {
    loadFileTasks()
  }
  specifiedUserIdsText.value = ''
  previewList.value = []
  importResult.value = null
  lastPreviewSourceKey.value = ''
  resetPreviewQuery()
  clearPreviewSelection()
  formRef.value?.clearValidate()
}

function applyRouteDefaults() {
  if (route.query.activityId) {
    form.activityId = Number(route.query.activityId)
  }
  if (route.query.sourceBizType) {
    form.sourceBizType = String(route.query.sourceBizType)
  }
  if (route.query.fileTaskId) {
    if (!isDefenseScheduleSource.value) {
      form.sourceBizType = FILE_UPLOAD_MANAGER
    }
    form.fileTaskId = Number(route.query.fileTaskId)
  }
  if (route.query.competitionSeriesId) {
    form.competitionSeriesId = Number(route.query.competitionSeriesId)
  }
  if (route.query.sourceBizIds) {
    form.sourceBizType = FILE_UPLOAD_MANAGER
    form.sourceBizIdText = String(route.query.sourceBizIds).replace(/,/g, '\n')
  }
  if (route.query.initialSubmitStatus) {
    form.initialSubmitStatus = String(route.query.initialSubmitStatus)
  }
  if (route.query.activityId && isFileTaskSource.value) {
    form.sourceModule = 'system'
  }
}

function syncSourceModule() {
  form.sourceModule = isFileTaskSource.value ? 'system' : 'competition'
}

function validateDefenseCompetition(rule, value, callback) {
  if (!isDefenseScheduleSource.value || form.competitionSeriesId) {
    callback()
    return
  }
  callback(new Error('请选择赛事系列'))
}

function validateImportSource(rule, value, callback) {
  const sourceBizIds = splitIds(form.sourceBizIdText)
  if (isDefenseScheduleSource.value) {
    if (!form.fileTaskId) {
      callback(new Error('请选择文件任务'))
      return
    }
    if (!String(form.sourceBizIdText || '').trim()) {
      callback(new Error('请粘贴答辩安排表'))
      return
    }
    callback()
    return
  }
  if (isFileTaskSource.value) {
    if (form.fileTaskId || sourceBizIds.length > 0) {
      callback()
      return
    }
    callback(new Error('请选择文件任务或输入上传记录ID'))
    return
  }
  if (sourceBizIds.length === 0) {
    callback(new Error('请输入导入匹配值'))
    return
  }
  callback()
}

function loadFileTasks() {
  listFileTask({ pageNum: 1, pageSize: 200 }).then(res => {
    fileTaskOptions.value = res.rows || []
  })
}

function loadCompetitionOptions() {
  getSelectCompetitionList().then(res => {
    const data = res.data || []
    competitionOptions.value = data.map(item => ({
      competitionId: item.competitionId,
      competitionSeriesId: item.competitionSeriesId,
      competitionSeriesName: item.competitionSeriesName,
      competitionName: `${item.competitionSeriesName || ''}${item.competitionName || ''}`
    }))
  })
}

watch(() => form.sourceBizType, () => {
  syncSourceModule()
  if (isDefenseScheduleSource.value) {
    form.syncMaterial = true
    loadFileTasks()
  } else if (!isFileTaskSource.value) {
    form.fileTaskId = undefined
    form.submittedOnly = true
    form.syncMaterial = false
    form.materialOverwriteMode = ''
    form.competitionSeriesId = undefined
  } else {
    loadFileTasks()
  }
  clearPreviewData()
  formRef.value?.clearValidate(['competitionSeriesId', 'fileTaskId', 'sourceBizIdText'])
})

watch(
  () => [
    form.activityId,
    form.competitionSeriesId,
    form.fileTaskId,
    form.submittedOnly,
    form.sourceBizIdText,
    form.defaultObjectType,
    form.permissionUserMode,
    form.overwriteExisting,
    specifiedUserIdsText.value
  ],
  () => clearPreviewData()
)

function loadActivities() {
  listReviewActivity({ pageNum: 1, pageSize: 200 }).then(res => {
    activityOptions.value = res.rows || []
  })
}

function buildPayload() {
  const sourceBizIds = isDefenseScheduleSource.value ? [] : splitIds(form.sourceBizIdText)
  return {
    activityId: form.activityId,
    sourceModule: form.sourceModule,
    sourceBizType: form.sourceBizType,
    sourceBizIds,
    competitionSeriesId: isDefenseScheduleSource.value ? form.competitionSeriesId : undefined,
    defenseScheduleText: isDefenseScheduleSource.value ? form.sourceBizIdText : undefined,
    fileTaskId: isFileTaskMaterialSource.value ? form.fileTaskId : undefined,
    submittedOnly: isFileTaskMaterialSource.value ? form.submittedOnly : undefined,
    defaultObjectType: form.defaultObjectType,
    permissionUserMode: form.permissionUserMode,
    overwriteExisting: form.overwriteExisting,
    syncCertificate: form.syncCertificate,
    syncMaterial: form.syncMaterial,
    initialSubmitStatus: form.initialSubmitStatus || undefined,
    materialOverwriteMode: form.materialOverwriteMode || undefined,
    specifiedUserIds: splitIds(specifiedUserIdsText.value).map(item => Number(item)).filter(item => !Number.isNaN(item))
  }
}

function buildPreviewSourceKey(payload) {
  return JSON.stringify({
    activityId: payload.activityId,
    sourceModule: payload.sourceModule,
    sourceBizType: payload.sourceBizType,
    sourceBizIds: payload.sourceBizIds,
    competitionSeriesId: payload.competitionSeriesId,
    defenseScheduleText: payload.defenseScheduleText,
    fileTaskId: payload.fileTaskId,
    submittedOnly: payload.submittedOnly,
    defaultObjectType: payload.defaultObjectType,
    permissionUserMode: payload.permissionUserMode,
    overwriteExisting: payload.overwriteExisting,
    specifiedUserIds: payload.specifiedUserIds
  })
}

function splitIds(text) {
  return (text || '')
    .split(/[\s,，;；]+/)
    .map(item => item.trim())
    .filter(Boolean)
}

function sourceBizTypeLabel(value) {
  const option = sourceBizTypeOptions.find(item => item.value === value)
  return option ? option.label : (value || '-')
}

function normalizeKeyword(value) {
  return String(value || '').trim().toLowerCase()
}

function handlePreviewQuery() {
  previewQuery.teamCode = previewSearchForm.teamCode
  previewQuery.teamName = previewSearchForm.teamName
}

function resetPreviewQuery() {
  previewSearchForm.teamCode = ''
  previewSearchForm.teamName = ''
  handlePreviewQuery()
}

function clearPreviewSelection() {
  selectedPreviewRows.value = []
  previewTableRef.value?.clearSelection()
}

function clearPreviewData() {
  if (previewList.value.length === 0 && selectedPreviewRows.value.length === 0 && !importResult.value) {
    lastPreviewSourceKey.value = ''
    return
  }
  previewList.value = []
  importResult.value = null
  lastPreviewSourceKey.value = ''
  clearPreviewSelection()
}

function previewRowKey(row) {
  return `${row.sourceBizType || ''}::${row.sourceBizId || ''}`
}

function isPreviewSelectable(row) {
  return Boolean(row && row.canImport)
}

function handlePreviewSelectionChange(selection) {
  selectedPreviewRows.value = selection || []
}

function validateAndRun(callback) {
  formRef.value.validate(valid => {
    if (!valid) return
    const payload = buildPayload()
    if (isDefenseScheduleSource.value) {
      if (!payload.competitionSeriesId) {
        ElMessage.warning('请选择赛事系列')
        return
      }
      if (!payload.fileTaskId) {
        ElMessage.warning('请选择文件任务')
        return
      }
      if (!String(payload.defenseScheduleText || '').trim()) {
        ElMessage.warning('请粘贴答辩安排表')
        return
      }
      callback(payload)
      return
    }
    if (!isFileTaskSource.value && payload.sourceBizIds.length === 0) {
      ElMessage.warning('请输入至少一个导入匹配值')
      return
    }
    if (isFileTaskSource.value && !payload.fileTaskId && payload.sourceBizIds.length === 0) {
      ElMessage.warning('请选择文件任务或输入上传记录ID')
      return
    }
    callback(payload)
  })
}

function handlePreview() {
  validateAndRun(payload => {
    previewLoading.value = true
    const previewSourceKey = buildPreviewSourceKey(payload)
    importPreview(payload).then(res => {
      previewList.value = res.data || []
      importResult.value = null
      lastPreviewSourceKey.value = previewSourceKey
      nextTick(() => clearPreviewSelection())
    }).finally(() => {
      previewLoading.value = false
    })
  })
}

function handleImport() {
  validateAndRun(payload => {
    if (previewList.value.length === 0) {
      ElMessage.warning('请先预览并选择要导入的数据')
      return
    }
    if (buildPreviewSourceKey(payload) !== lastPreviewSourceKey.value) {
      ElMessage.warning('预览条件已变更，请重新预览后再选择导入')
      return
    }
    const sourceBizIds = Array.from(new Set(selectedImportRows.value.map(row => row.sourceBizId).filter(Boolean)))
    if (sourceBizIds.length === 0) {
      ElMessage.warning('请选择至少一条可导入的预览数据')
      return
    }
    const importPayload = {
      ...payload,
      sourceBizIds
    }
    ElMessageBox.confirm(`确认导入选中的 ${sourceBizIds.length} 条预览数据？该操作会写入评审模块数据。`, '导入确认', {
      type: 'warning'
    }).then(() => {
      importLoading.value = true
      importFromBusiness(importPayload).then(res => {
        importResult.value = res.data
        ElMessage.success('导入执行完成')
        nextTick(() => clearPreviewSelection())
      }).finally(() => {
        importLoading.value = false
      })
    }).catch(() => {})
  })
}

function toItemRows(items) {
  return (items || []).map(message => ({ message }))
}

function goObjectDetail(id) {
  router.push({ path: `/review/object-detail/index/${id}` })
}

onMounted(() => {
  loadCompetitionOptions()
  loadActivities()
  resetForm()
})
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}

.mt16 {
  margin-top: 16px;
}

.preview-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.preview-toolbar :deep(.el-form-item) {
  margin-bottom: 8px;
}

.preview-selection {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 32px;
  color: var(--el-text-color-regular);
  white-space: nowrap;
}
</style>
