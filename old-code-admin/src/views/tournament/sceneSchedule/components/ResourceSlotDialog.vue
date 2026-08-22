<template>
  <el-dialog
    :title="dialogTitle"
    v-model="visible"
    width="1180px"
    append-to-body
    class="resource-slot-dialog"
  >
    <el-descriptions v-if="scheduleResource" :column="4" border class="slot-summary">
      <el-descriptions-item label="资源名称">{{ scheduleResource.resourceName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="部署位置">{{ scheduleResource.deploymentLocation || '-' }}</el-descriptions-item>
      <el-descriptions-item label="部署设备数">{{ scheduleResource.deployedDeviceCount || 0 }}</el-descriptions-item>
      <el-descriptions-item label="每台工位数">{{ scheduleResource.workstationsPerDevice || 0 }}</el-descriptions-item>
      <el-descriptions-item label="总工位数">{{ scheduleResource.totalWorkstations || 0 }}</el-descriptions-item>
      <el-descriptions-item label="默认周期">{{ scheduleResource.slotDurationMinutes || 0 }} 分钟</el-descriptions-item>
      <el-descriptions-item label="预约状态">
        <el-tag :type="tagType(bookingStatusOptions, scheduleResource.bookingStatus)">
          {{ tagLabel(bookingStatusOptions, scheduleResource.bookingStatus) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="共享占用">{{ scheduleResource.sharedOccupancy ? '是' : '否' }}</el-descriptions-item>
    </el-descriptions>

    <div class="slot-toolbar">
      <div>
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAddSlot"
          v-hasPermi="['competition:sceneResourceSlot:add']"
        >新增时段</el-button>
        <el-button
          type="success"
          plain
          icon="Calendar"
          @click="handleBatchSlot"
          v-hasPermi="['competition:sceneResourceSlot:add']"
        >批量生成</el-button>
        <el-button icon="Refresh" @click="getList">刷新</el-button>
      </div>
      <el-select v-model="queryParams.slotStatus" placeholder="时段状态" clearable style="width: 140px" @change="handleQuery">
        <el-option
          v-for="item in slotStatusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </div>

    <el-table v-loading="loading" :data="slotList" stripe>
      <el-table-column label="开始时间" prop="startTime" width="170" />
      <el-table-column label="结束时间" prop="endTime" width="170" />
      <el-table-column label="设备容量" prop="deviceCapacity" width="100" align="center" />
      <el-table-column label="工位容量" prop="workstationCapacity" width="100" align="center" />
      <el-table-column label="已预约设备" prop="reservedDeviceCount" width="110" align="center" />
      <el-table-column label="剩余设备" prop="remainingDeviceCount" width="100" align="center" />
      <el-table-column label="已预约工位" prop="reservedWorkstationCount" width="110" align="center" />
      <el-table-column label="剩余工位" prop="remainingWorkstationCount" width="100" align="center" />
      <el-table-column label="允许组别" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ formatAllowedGroups(row.allowedGroupNames) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="slotStatus" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="tagType(slotStatusOptions, row.slotStatus)">
            {{ tagLabel(slotStatusOptions, row.slotStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="版本" prop="version" width="80" align="center" />
      <el-table-column label="更新时间" prop="updateTime" width="170" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="handleEditSlot(row)"
            v-hasPermi="['competition:sceneResourceSlot:edit']"
          >编辑</el-button>
          <el-button
            link
            type="success"
            :disabled="row.slotStatus === 'OPEN'"
            @click="handleChangeSlotStatus(row, 'OPEN')"
            v-hasPermi="['competition:sceneResourceSlot:changeStatus']"
          >开放</el-button>
          <el-button
            link
            type="warning"
            :disabled="row.slotStatus === 'CLOSED'"
            @click="handleChangeSlotStatus(row, 'CLOSED')"
            v-hasPermi="['competition:sceneResourceSlot:changeStatus']"
          >关闭</el-button>
          <el-button
            link
            type="danger"
            @click="handleDeleteSlot(row)"
            v-hasPermi="['competition:sceneResourceSlot:remove']"
          >删除</el-button>
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

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">关 闭</el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog :title="slotFormTitle" v-model="slotFormOpen" width="720px" append-to-body>
    <el-form ref="slotFormRef" :model="slotForm" :rules="slotRules" label-width="140px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="slotForm.startTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="slotForm.endTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备容量" prop="deviceCapacity">
            <el-input-number
              v-model="slotForm.deviceCapacity"
              :min="1"
              :max="maxDeviceCapacity"
              :step="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工位容量预览">
            <el-input :model-value="slotWorkstationPreview" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="时段状态" prop="slotStatus">
            <el-select v-model="slotForm.slotStatus" placeholder="请选择" style="width: 100%">
              <el-option
                v-for="item in slotStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="允许预约组别">
            <el-select
              v-model="slotForm.allowedGroupCodes"
              multiple
              filterable
              clearable
              collapse-tags
              collapse-tags-tooltip
              placeholder="不选择表示不限组别"
              style="width: 100%"
            >
              <el-option
                v-for="item in groupOptions"
                :key="item.allowedGroupCode"
                :label="formatGroupOption(item)"
                :value="item.allowedGroupCode"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="slotFormOpen = false">取 消</el-button>
        <el-button type="primary" :loading="slotSubmitting" @click="submitSlotForm">保 存</el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog title="批量生成时段" v-model="batchOpen" width="760px" append-to-body>
    <el-form ref="batchFormRef" :model="batchForm" :rules="batchRules" label-width="150px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="日期" prop="date">
            <el-date-picker
              v-model="batchForm.date"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="每场时长（分钟）" prop="slotDurationMinutes">
            <el-input-number
              v-model="batchForm.slotDurationMinutes"
              :min="1"
              :step="5"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startClock">
            <el-time-picker
              v-model="batchForm.startClock"
              value-format="HH:mm:ss"
              placeholder="请选择"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间" prop="endClock">
            <el-time-picker
              v-model="batchForm.endClock"
              value-format="HH:mm:ss"
              placeholder="请选择"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="每时段设备容量" prop="deviceCapacity">
            <el-input-number
              v-model="batchForm.deviceCapacity"
              :min="1"
              :max="maxDeviceCapacity"
              :step="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="每时段工位容量">
            <el-input :model-value="batchWorkstationPreview" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="时段状态" prop="slotStatus">
            <el-select v-model="batchForm.slotStatus" placeholder="请选择" style="width: 100%">
              <el-option
                v-for="item in batchStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="允许预约组别">
            <el-select
              v-model="batchForm.allowedGroupCodes"
              multiple
              filterable
              clearable
              collapse-tags
              collapse-tags-tooltip
              placeholder="不选择表示不限组别"
              style="width: 100%"
            >
              <el-option
                v-for="item in groupOptions"
                :key="item.allowedGroupCode"
                :label="formatGroupOption(item)"
                :value="item.allowedGroupCode"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预计生成">
            <el-input :model-value="`${batchPreviewCount} 个时段`" disabled />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="batchOpen = false">取 消</el-button>
        <el-button type="primary" :loading="batchSubmitting" @click="submitBatchForm">生 成</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="ResourceSlotDialog">
import {
  listSceneResourceSlot,
  getSceneResourceSlot,
  addSceneResourceSlot,
  updateSceneResourceSlot,
  delSceneResourceSlot,
  changeSceneResourceSlotStatus,
  batchGenerateSceneResourceSlot
} from '@/api/tournament/sceneResourceSlot'
import {
  listSceneResourceSlotGroupScopeBySlot,
  listSceneResourceSlotGroupOptions
} from '@/api/tournament/sceneResourceSlotGroupScope'
import Pagination from '@/components/Pagination'
import modal from '@/plugins/modal'

const visible = ref(false)
const scheduleResource = ref(null)
const loading = ref(false)
const slotList = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  scheduleResourceId: undefined,
  slotStatus: undefined
})

const slotFormRef = ref(null)
const slotFormOpen = ref(false)
const slotFormMode = ref('add')
const slotSubmitting = ref(false)
const slotForm = ref({})

const batchFormRef = ref(null)
const batchOpen = ref(false)
const batchSubmitting = ref(false)
const batchForm = ref({})
const groupOptions = ref([])

const bookingStatusOptions = [
  { label: '草稿', value: 'DRAFT', type: 'info' },
  { label: '待发布', value: 'READY', type: 'warning' },
  { label: '开放预约', value: 'OPEN', type: 'success' },
  { label: '暂停预约', value: 'PAUSED', type: 'warning' },
  { label: '已关闭', value: 'CLOSED', type: 'info' }
]

const slotStatusOptions = [
  { label: '待开放', value: 'PENDING', type: 'info' },
  { label: '开放', value: 'OPEN', type: 'success' },
  { label: '已满', value: 'FULL', type: 'danger' },
  { label: '关闭', value: 'CLOSED', type: 'info' },
  { label: '已过期', value: 'EXPIRED', type: 'info' }
]

const batchStatusOptions = slotStatusOptions.filter(item => ['PENDING', 'OPEN', 'CLOSED'].includes(item.value))

const dialogTitle = computed(() => {
  const name = scheduleResource.value?.resourceName || '资源'
  return `${name} - 配置时段`
})
const slotFormTitle = computed(() => slotFormMode.value === 'edit' ? '编辑时段' : '新增时段')
const maxDeviceCapacity = computed(() => Number(scheduleResource.value?.deployedDeviceCount || 1))
const workstationsPerDevice = computed(() => Number(scheduleResource.value?.workstationsPerDevice || 0))
const slotWorkstationPreview = computed(() => Number(slotForm.value.deviceCapacity || 0) * workstationsPerDevice.value)
const batchWorkstationPreview = computed(() => Number(batchForm.value.deviceCapacity || 0) * workstationsPerDevice.value)
const batchPreviewCount = computed(() => {
  const duration = Number(batchForm.value.slotDurationMinutes || 0)
  const start = buildBatchTimestamp(batchForm.value.date, batchForm.value.startClock)
  const end = buildBatchTimestamp(batchForm.value.date, batchForm.value.endClock)
  if (!duration || !start || !end || end <= start) return 0
  return Math.floor((end - start) / (duration * 60 * 1000))
})

const positiveIntegerValidator = (message, maxGetter) => {
  return (_rule, value, callback) => {
    if (value === undefined || value === null || value === '') {
      callback(new Error(message))
      return
    }
    if (!Number.isInteger(Number(value)) || Number(value) <= 0) {
      callback(new Error(message))
      return
    }
    const max = maxGetter ? maxGetter() : undefined
    if (max !== undefined && Number(value) > Number(max)) {
      callback(new Error(`不能大于部署设备数 ${max}`))
      return
    }
    callback()
  }
}

const timeRangeValidator = (_rule, _value, callback) => {
  const start = toTimestamp(slotForm.value.startTime)
  const end = toTimestamp(slotForm.value.endTime)
  if (start && end && end <= start) {
    callback(new Error('结束时间必须晚于开始时间'))
    return
  }
  callback()
}

const batchTimeRangeValidator = (_rule, _value, callback) => {
  const start = buildBatchTimestamp(batchForm.value.date, batchForm.value.startClock)
  const end = buildBatchTimestamp(batchForm.value.date, batchForm.value.endClock)
  if (start && end && end <= start) {
    callback(new Error('结束时间必须晚于开始时间'))
    return
  }
  callback()
}

const slotRules = {
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' },
    { validator: timeRangeValidator, trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    { validator: timeRangeValidator, trigger: 'change' }
  ],
  deviceCapacity: [{ validator: positiveIntegerValidator('设备容量必须大于0', () => maxDeviceCapacity.value), trigger: 'change' }],
  slotStatus: [{ required: true, message: '请选择时段状态', trigger: 'change' }]
}

const batchRules = {
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  startClock: [
    { required: true, message: '请选择开始时间', trigger: 'change' },
    { validator: batchTimeRangeValidator, trigger: 'change' }
  ],
  endClock: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    { validator: batchTimeRangeValidator, trigger: 'change' }
  ],
  slotDurationMinutes: [{ validator: positiveIntegerValidator('每场时长必须大于0分钟'), trigger: 'change' }],
  deviceCapacity: [{ validator: positiveIntegerValidator('设备容量必须大于0', () => maxDeviceCapacity.value), trigger: 'change' }],
  slotStatus: [{ required: true, message: '请选择时段状态', trigger: 'change' }]
}

function tagLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function tagType(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.type : 'info'
}

function toTimestamp(value) {
  if (!value) return 0
  return new Date(String(value).replace(' ', 'T')).getTime()
}

function buildBatchTimestamp(date, clock) {
  if (!date || !clock) return 0
  return new Date(`${date}T${clock}`).getTime()
}

function buildBatchDateTime(date, clock) {
  return date && clock ? `${date} ${clock}` : undefined
}

function open(row) {
  scheduleResource.value = row
  visible.value = true
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    scheduleResourceId: row?.scheduleResourceId,
    slotStatus: undefined
  }
  loadGroupOptions()
  getList()
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function getList() {
  if (!scheduleResource.value?.scheduleResourceId) return
  loading.value = true
  queryParams.value.scheduleResourceId = scheduleResource.value.scheduleResourceId
  listSceneResourceSlot(queryParams.value).then(response => {
    slotList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function loadGroupOptions() {
  if (!scheduleResource.value?.scheduleId && !scheduleResource.value?.eventId) {
    groupOptions.value = []
    return Promise.resolve()
  }
  return listSceneResourceSlotGroupOptions({
    scheduleId: scheduleResource.value?.scheduleId,
    competitionSeriesId: scheduleResource.value?.eventId
  }).then(response => {
    groupOptions.value = response.data || []
  })
}

function resetSlotForm(row = {}) {
  slotForm.value = {
    slotId: row.slotId,
    scheduleResourceId: scheduleResource.value?.scheduleResourceId,
    startTime: row.startTime,
    endTime: row.endTime,
    deviceCapacity: row.deviceCapacity || scheduleResource.value?.deployedDeviceCount || 1,
    slotStatus: row.slotStatus || 'PENDING',
    allowedGroupCodes: (row.allowedGroups || [])
      .map(item => item.allowedGroupCode)
      .filter(Boolean)
  }
  nextTick(() => slotFormRef.value?.clearValidate())
}

function handleAddSlot() {
  slotFormMode.value = 'add'
  resetSlotForm()
  slotFormOpen.value = true
}

function handleEditSlot(row) {
  slotFormMode.value = 'edit'
  resetSlotForm(row)
  slotFormOpen.value = true
  Promise.all([
    getSceneResourceSlot(row.slotId),
    listSceneResourceSlotGroupScopeBySlot({ slotId: row.slotId })
  ]).then(([slotResponse, groupResponse]) => {
    resetSlotForm({
      ...(slotResponse.data || row),
      allowedGroups: groupResponse.data || []
    })
  })
}

function buildSlotPayload() {
  return {
    slotId: slotForm.value.slotId,
    scheduleResourceId: scheduleResource.value?.scheduleResourceId,
    startTime: slotForm.value.startTime,
    endTime: slotForm.value.endTime,
    deviceCapacity: slotForm.value.deviceCapacity,
    slotStatus: slotForm.value.slotStatus,
    allowedGroups: buildAllowedGroups(slotForm.value.allowedGroupCodes)
  }
}

function submitSlotForm() {
  slotFormRef.value?.validate(valid => {
    if (!valid) return
    slotSubmitting.value = true
    const request = slotFormMode.value === 'edit' ? updateSceneResourceSlot : addSceneResourceSlot
    request(buildSlotPayload()).then(() => {
      modal.msgSuccess('保存成功')
      slotFormOpen.value = false
      getList()
    }).finally(() => {
      slotSubmitting.value = false
    })
  })
}

function resetBatchForm() {
  batchForm.value = {
    date: undefined,
    startClock: undefined,
    endClock: undefined,
    slotDurationMinutes: scheduleResource.value?.slotDurationMinutes || 30,
    deviceCapacity: scheduleResource.value?.deployedDeviceCount || 1,
    slotStatus: 'PENDING',
    allowedGroupCodes: []
  }
  nextTick(() => batchFormRef.value?.clearValidate())
}

function handleBatchSlot() {
  resetBatchForm()
  batchOpen.value = true
}

function submitBatchForm() {
  batchFormRef.value?.validate(valid => {
    if (!valid) return
    if (batchPreviewCount.value <= 0) {
      modal.msgWarning('当前时间范围无法生成完整时段')
      return
    }
    batchSubmitting.value = true
    batchGenerateSceneResourceSlot({
      scheduleResourceId: scheduleResource.value?.scheduleResourceId,
      startTime: buildBatchDateTime(batchForm.value.date, batchForm.value.startClock),
      endTime: buildBatchDateTime(batchForm.value.date, batchForm.value.endClock),
      slotDurationMinutes: batchForm.value.slotDurationMinutes,
      deviceCapacity: batchForm.value.deviceCapacity,
      slotStatus: batchForm.value.slotStatus,
      allowedGroups: buildAllowedGroups(batchForm.value.allowedGroupCodes)
    }).then(response => {
      modal.msgSuccess(`生成成功，新增 ${response.data || 0} 个时段`)
      batchOpen.value = false
      getList()
    }).finally(() => {
      batchSubmitting.value = false
    })
  })
}

function buildAllowedGroups(codes = []) {
  const optionMap = new Map(groupOptions.value.map(item => [item.allowedGroupCode, item]))
  return (codes || []).map(code => {
    const option = optionMap.get(code) || {}
    return {
      allowedGroupCode: code,
      allowedGroupName: option.allowedGroupName || code
    }
  })
}

function formatAllowedGroups(names = []) {
  return names && names.length ? names.join('、') : '不限组别'
}

function formatGroupOption(item) {
  if (!item) return '-'
  return item.allowedGroupName && item.allowedGroupName !== item.allowedGroupCode
    ? `${item.allowedGroupName}（${item.allowedGroupCode}）`
    : item.allowedGroupCode
}

function handleChangeSlotStatus(row, slotStatus) {
  const label = tagLabel(slotStatusOptions, slotStatus)
  modal.confirm(`确认将该时段切换为“${label}”吗？`).then(() => {
    return changeSceneResourceSlotStatus({
      slotId: row.slotId,
      slotStatus
    })
  }).then(() => {
    modal.msgSuccess('状态已更新')
    getList()
  }).catch(() => {})
}

function handleDeleteSlot(row) {
  modal.confirm('确认删除该预约时段吗？').then(() => {
    return delSceneResourceSlot(row.slotId)
  }).then(() => {
    modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

defineExpose({
  open
})
</script>

<style scoped lang="scss">
.slot-summary {
  margin-bottom: 12px;
}

.slot-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}
</style>
