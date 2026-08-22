<template>
  <el-dialog
    v-model="visible"
    :title="managerTitle"
    width="1120px"
    append-to-body
    destroy-on-close
    @open="handleOpen"
  >
    <el-alert
      v-if="isPersonal"
      type="info"
      :closable="false"
      show-icon
      class="notice-recipient-alert"
    >
      <template #title>
        {{ personalNoticeTip }}
      </template>
    </el-alert>

    <el-form v-if="!isBatchPersonal" :model="query" :inline="true" label-width="76px" class="notice-query-form">
      <el-form-item label="标题">
        <el-input v-model.trim="query.title" clearable placeholder="请输入标题" style="width: 190px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item v-if="!isPersonal" label="关联赛事">
        <el-select v-model="query.competitionSeriesId" clearable filterable placeholder="请选择" style="width: 250px">
          <el-option
            v-for="item in competitionOptions"
            :key="item.competitionSeriesId"
            :label="item.competitionName"
            :value="item.competitionSeriesId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发布状态">
        <el-select v-model="query.publishStatus" clearable placeholder="全部" style="width: 130px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已停用" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="notice-toolbar">
      <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['competition:sceneNotice:add']">
        {{ addButtonText }}
      </el-button>
    </div>

    <el-table v-if="!isBatchPersonal" v-loading="loading" :data="noticeList" stripe>
      <el-table-column label="标题" prop="title" min-width="210" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="notice-title-cell">
            <el-tag v-if="row.isTop === '1'" size="small" type="danger" effect="plain">置顶</el-tag>
            <span>{{ row.title }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column v-if="!isPersonal" label="赛事/范围" min-width="230" show-overflow-tooltip>
        <template #default="{ row }">
          <div>{{ row.competitionName || competitionLabel(row.competitionSeriesId) || '-' }}</div>
          <div class="muted">{{ scopeLabel(row) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="级别" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="levelTagType(row.noticeLevel)">{{ levelLabel(row.noticeLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="publishStatusType(row.publishStatus)">{{ publishStatusLabel(row.publishStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="有效时间" min-width="205">
        <template #default="{ row }">
          <div>{{ row.publishTime || '发布时立即生效' }}</div>
          <div class="muted">至 {{ row.expireTime || '长期有效' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)" v-hasPermi="['competition:sceneNotice:edit']">修改</el-button>
          <el-button
            v-if="row.publishStatus !== 'PUBLISHED'"
            link
            type="success"
            @click="handlePublish(row)"
            v-hasPermi="['competition:sceneNotice:publish']"
          >发布</el-button>
          <el-button
            v-else
            link
            type="warning"
            @click="handleDisable(row)"
            v-hasPermi="['competition:sceneNotice:publish']"
          >停用</el-button>
          <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['competition:sceneNotice:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-if="!isBatchPersonal"
      v-show="total > 0"
      :total="total"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      @pagination="getList"
    />

    <template #footer>
      <el-button @click="visible = false">关 闭</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="formVisible"
    :title="formTitle"
    width="820px"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="104px">
      <el-form-item v-if="isPersonal" label="接收人">
        <el-input :model-value="recipientSummary" disabled />
      </el-form-item>
      <template v-else>
        <el-form-item label="关联赛事" prop="competitionSeriesId">
          <el-select
            v-model="form.competitionSeriesId"
            filterable
            placeholder="请选择赛事"
            style="width: 100%"
            @change="handleFormCompetitionChange"
          >
            <el-option
              v-for="item in competitionOptions"
              :key="item.competitionSeriesId"
              :label="item.competitionName"
              :value="item.competitionSeriesId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="可见范围" prop="scopeType">
          <el-radio-group v-model="form.scopeType" @change="handleScopeChange">
            <el-radio value="COMPETITION">赛事下全部持证/绑定人员</el-radio>
            <el-radio value="SCHEDULE">指定赛场人员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.scopeType === 'SCHEDULE'" label="关联赛场" prop="scheduleIds">
          <el-select
            v-model="form.scheduleIds"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择一个或多个赛场"
            style="width: 100%"
            :loading="scheduleLoading"
          >
            <el-option v-for="item in scheduleOptions" :key="item.scheduleId" :label="item.scheduleName" :value="item.scheduleId" />
          </el-select>
        </el-form-item>
      </template>

      <el-form-item label="标题" prop="title">
        <el-input v-model.trim="form.title" maxlength="255" show-word-limit placeholder="请输入通知标题" />
      </el-form-item>
      <el-form-item label="通知内容" prop="content">
        <editor v-model="form.content" :min-height="260" />
      </el-form-item>
      <el-row :gutter="18">
        <el-col :span="12">
          <el-form-item label="通知级别" prop="noticeLevel">
            <el-select v-model="form.noticeLevel" style="width: 100%">
              <el-option label="普通" value="NORMAL" />
              <el-option label="重要" value="IMPORTANT" />
              <el-option label="紧急" value="URGENT" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="是否置顶">
            <el-switch v-model="form.isTop" active-value="1" inactive-value="0" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="排序值">
            <el-input-number v-model="form.sortNo" :min="0" :max="99999" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="18">
        <el-col :span="12">
          <el-form-item label="发布时间" prop="publishTime">
            <el-date-picker
              v-model="form.publishTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="留空则发布时立即生效"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="失效时间" prop="expireTime">
            <el-date-picker
              v-model="form.expireTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="留空则长期有效"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
      </el-form-item>
      <el-alert
        :title="saveNoticeTip"
        type="warning"
        :closable="false"
        show-icon
      />
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取 消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">{{ submitButtonText }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination'
import {
  addSceneNotice,
  changeSceneNoticeStatus,
  delSceneNotice,
  getSceneNotice,
  listSceneNotice,
  publishSceneNotice,
  updateSceneNotice
} from '@/api/tournament/sceneNotice'
import { listSceneSchedule } from '@/api/tournament/sceneSchedule'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  mode: { type: String, default: 'ANNOUNCEMENT' },
  target: { type: Object, default: null },
  targets: { type: Array, default: () => [] },
  competitionOptions: { type: Array, default: () => [] },
  initialSeriesId: { type: [Number, String], default: undefined }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})
const isPersonal = computed(() => props.mode === 'PERSONAL')
const batchTargets = computed(() => Array.isArray(props.targets) ? props.targets.filter(isReceivableTarget) : [])
const isBatchPersonal = computed(() => isPersonal.value && batchTargets.value.length > 1)
const recipientName = computed(() => props.target?.targetName || props.target?.userName || props.target?.teamName || '-')
const recipientSummary = computed(() => isBatchPersonal.value ? `已选择 ${batchTargets.value.length} 人` : recipientName.value)
const managerTitle = computed(() => {
  if (isBatchPersonal.value) return `批量个人通知 - ${batchTargets.value.length} 人`
  return isPersonal.value ? `个人通知管理 - ${recipientName.value}` : '大赛公告管理'
})
const personalNoticeTip = computed(() => {
  if (isBatchPersonal.value) {
    return `已选择 ${batchTargets.value.length} 人；将为每个可接收对象分别创建相同内容的个人通知。通知仅按用户ID或报名成员ID匹配，不使用姓名匹配。`
  }
  return `接收人：${recipientName.value}；通知仅按用户ID或报名成员ID匹配，不使用姓名匹配。`
})
const addButtonText = computed(() => {
  if (isBatchPersonal.value) return '新增批量个人通知'
  return `新增${isPersonal.value ? '个人通知' : '公告'}`
})
const formTitle = computed(() => {
  if (form.noticeId) return '修改通知'
  if (isBatchPersonal.value) return '批量新增个人通知'
  return `新增${isPersonal.value ? '个人通知' : '大赛公告'}`
})
const submitButtonText = computed(() => isBatchPersonal.value ? '批量保存' : '保 存')
const saveNoticeTip = computed(() => {
  if (isBatchPersonal.value) return '批量保存后将直接发布给所选人员。'
  return form.noticeId && form.publishStatus === 'PUBLISHED'
    ? '当前通知已发布，保存修改后将继续保持发布状态。'
    : '保存后为草稿，请在列表中点击“发布”。'
})

const loading = ref(false)
const submitting = ref(false)
const formVisible = ref(false)
const formRef = ref(null)
const noticeList = ref([])
const total = ref(0)
const scheduleLoading = ref(false)
const scheduleOptions = ref([])

const query = reactive(defaultQuery())
const form = reactive(defaultForm())

const rules = {
  competitionSeriesId: [{ required: true, message: '请选择关联赛事', trigger: 'change' }],
  scopeType: [{ required: true, message: '请选择可见范围', trigger: 'change' }],
  scheduleIds: [{
    validator: (_rule, value, callback) => {
      if (!isPersonal.value && form.scopeType === 'SCHEDULE' && (!Array.isArray(value) || value.length === 0)) {
        callback(new Error('请至少选择一个赛场'))
        return
      }
      callback()
    },
    trigger: 'change'
  }],
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'change' }],
  noticeLevel: [{ required: true, message: '请选择通知级别', trigger: 'change' }],
  expireTime: [{
    validator: (_rule, value, callback) => {
      if (value && form.publishTime && new Date(value).getTime() <= new Date(form.publishTime).getTime()) {
        callback(new Error('失效时间必须晚于发布时间'))
        return
      }
      callback()
    },
    trigger: 'change'
  }]
}

function defaultQuery() {
  return {
    pageNum: 1,
    pageSize: 10,
    noticeType: props.mode,
    targetId: props.mode === 'PERSONAL' && !isBatchPersonal.value ? props.target?.targetId : undefined,
    competitionSeriesId: props.mode === 'ANNOUNCEMENT' ? props.initialSeriesId : undefined,
    title: undefined,
    publishStatus: undefined
  }
}

function defaultForm() {
  return {
    noticeId: undefined,
    noticeType: props.mode,
    targetId: props.mode === 'PERSONAL' && !isBatchPersonal.value ? props.target?.targetId : undefined,
    competitionSeriesId: props.mode === 'ANNOUNCEMENT'
      ? props.initialSeriesId
      : (isBatchPersonal.value ? batchTargets.value[0]?.competitionSeriesId : props.target?.competitionSeriesId),
    scopeType: props.mode === 'PERSONAL' ? 'PERSON' : 'COMPETITION',
    scheduleIds: [],
    title: '',
    content: '',
    noticeLevel: 'NORMAL',
    isTop: '0',
    sortNo: 100,
    publishStatus: isBatchPersonal.value ? 'PUBLISHED' : 'DRAFT',
    publishTime: undefined,
    expireTime: undefined,
    remark: ''
  }
}

function assignReactive(target, value) {
  Object.keys(target).forEach(key => delete target[key])
  Object.assign(target, value)
}

function handleOpen() {
  assignReactive(query, defaultQuery())
  if (isBatchPersonal.value) {
    noticeList.value = []
    total.value = 0
    return
  }
  getList()
}

function getList() {
  loading.value = true
  listSceneNotice({ ...query }).then(response => {
    noticeList.value = response.rows || response.data?.rows || []
    total.value = Number(response.total ?? response.data?.total ?? noticeList.value.length)
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  query.pageNum = 1
  getList()
}

function resetQuery() {
  assignReactive(query, defaultQuery())
  getList()
}

function handleAdd() {
  assignReactive(form, defaultForm())
  scheduleOptions.value = []
  if (!isPersonal.value && form.competitionSeriesId) {
    loadScheduleOptions(form.competitionSeriesId)
  }
  formVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function handleEdit(row) {
  getSceneNotice(row.noticeId).then(response => {
    const data = response.data || {}
    assignReactive(form, { ...defaultForm(), ...data, scheduleIds: data.scheduleIds || [] })
    if (!isPersonal.value && form.competitionSeriesId) {
      loadScheduleOptions(form.competitionSeriesId)
    }
    formVisible.value = true
    nextTick(() => formRef.value?.clearValidate())
  })
}

function submitForm() {
  formRef.value?.validate(valid => {
    if (!valid) return
    if (isBatchPersonal.value && !form.noticeId) {
      submitBatchPersonalNotice()
      return
    }
    submitting.value = true
    const request = form.noticeId ? updateSceneNotice(form) : addSceneNotice(form)
    request.then(() => {
      ElMessage.success('保存成功')
      formVisible.value = false
      getList()
    }).finally(() => {
      submitting.value = false
    })
  })
}

async function submitBatchPersonalNotice() {
  if (batchTargets.value.length === 0) {
    ElMessage.warning('请选择可接收通知的人员')
    return
  }
  submitting.value = true
  let successCount = 0
  let failCount = 0
  for (const target of batchTargets.value) {
    try {
      await addSceneNotice(buildBatchPersonalNoticePayload(target))
      successCount++
    } catch (error) {
      failCount++
    }
  }
  submitting.value = false
  if (successCount > 0) {
    const failText = failCount > 0 ? `，失败 ${failCount} 条` : ''
    ElMessage.success(`批量创建成功 ${successCount} 条${failText}`)
    formVisible.value = false
    visible.value = false
    return
  }
  ElMessage.warning('批量创建失败，请稍后重试')
}

function buildBatchPersonalNoticePayload(target) {
  const payload = { ...form }
  delete payload.noticeId
  return {
    ...payload,
    noticeType: 'PERSONAL',
    targetId: target.targetId,
    competitionSeriesId: target.competitionSeriesId || payload.competitionSeriesId,
    scopeType: 'PERSON',
    publishStatus: 'PUBLISHED'
  }
}

function isReceivableTarget(target) {
  return !!(target?.targetId && (target.userId || target.memberId))
}

function handlePublish(row) {
  ElMessageBox.confirm(`确定发布“${row.title}”吗？`, '发布确认', { type: 'warning' }).then(() => {
    return publishSceneNotice(row.noticeId)
  }).then(() => {
    ElMessage.success('发布成功')
    getList()
  }).catch(() => {})
}

function handleDisable(row) {
  ElMessageBox.confirm(`停用后用户端将立即不再显示“${row.title}”，是否继续？`, '停用确认', { type: 'warning' }).then(() => {
    return changeSceneNoticeStatus({ noticeId: row.noticeId, publishStatus: 'DISABLED' })
  }).then(() => {
    ElMessage.success('已停用')
    getList()
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除“${row.title}”吗？`, '删除确认', { type: 'warning' }).then(() => {
    return delSceneNotice(row.noticeId)
  }).then(() => {
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

function handleFormCompetitionChange(value) {
  form.scheduleIds = []
  loadScheduleOptions(value)
}

function handleScopeChange(value) {
  if (value !== 'SCHEDULE') {
    form.scheduleIds = []
  } else if (form.competitionSeriesId) {
    loadScheduleOptions(form.competitionSeriesId)
  }
}

function loadScheduleOptions(competitionSeriesId) {
  if (!competitionSeriesId) {
    scheduleOptions.value = []
    return
  }
  scheduleLoading.value = true
  listSceneSchedule({ competitionSeriesId, status: '0', pageNum: 1, pageSize: 1000 }).then(response => {
    scheduleOptions.value = response.rows || response.data?.rows || []
  }).finally(() => {
    scheduleLoading.value = false
  })
}

function competitionLabel(competitionSeriesId) {
  return props.competitionOptions.find(item => `${item.competitionSeriesId}` === `${competitionSeriesId}`)?.competitionName
}

function scopeLabel(row) {
  if (row.scopeType === 'COMPETITION') return '赛事级公告'
  if (row.scopeType === 'SCHEDULE') return `赛场级：${row.scheduleNames || '未配置赛场'}`
  return '个人通知'
}

function levelLabel(value) {
  return { NORMAL: '普通', IMPORTANT: '重要', URGENT: '紧急' }[value] || value || '-'
}

function levelTagType(value) {
  return { NORMAL: 'info', IMPORTANT: 'warning', URGENT: 'danger' }[value] || 'info'
}

function publishStatusLabel(value) {
  return { DRAFT: '草稿', PUBLISHED: '已发布', DISABLED: '已停用' }[value] || value || '-'
}

function publishStatusType(value) {
  return { DRAFT: 'info', PUBLISHED: 'success', DISABLED: 'warning' }[value] || 'info'
}
</script>

<style scoped lang="scss">
.notice-recipient-alert,
.notice-query-form,
.notice-toolbar {
  margin-bottom: 14px;
}

.notice-title-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.muted {
  color: #909399;
  font-size: 12px;
  line-height: 20px;
}
</style>
