<template>
  <el-dialog :title="dialogTitle" v-model="visible" width="920px" append-to-body @closed="handleClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="150px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="赛场安排">
            <el-input :model-value="schedule?.scheduleName || '-'" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="资源" prop="resourceId">
            <el-select
              v-model="form.resourceId"
              placeholder="请选择资源"
              filterable
              :disabled="isEdit"
              :loading="resourceLoading"
              style="width: 100%"
              @change="handleResourceChange"
            >
              <el-option
                v-for="item in resourceOptions"
                :key="item.resourceId"
                :label="formatResourceLabel(item)"
                :value="item.resourceId"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="部署位置" prop="deploymentLocation">
            <el-input v-model.trim="form.deploymentLocation" placeholder="请输入部署位置" maxlength="255" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="部署设备数" prop="deployedDeviceCount">
            <el-input-number v-model="form.deployedDeviceCount" :min="1" :step="1" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预约开放时间" prop="bookingOpenTime">
            <el-date-picker
              v-model="form.bookingOpenTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预约关闭时间" prop="bookingCloseTime">
            <el-date-picker
              v-model="form.bookingCloseTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="每台设备工位数" prop="workstationsPerDevice">
            <el-input-number v-model="form.workstationsPerDevice" :min="1" :step="1" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="总工位数预览">
            <el-input :model-value="previewTotalWorkstations" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单场周期（分钟）" prop="slotDurationMinutes">
            <el-input-number v-model="form.slotDurationMinutes" :min="1" :step="5" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预约状态" prop="bookingStatus">
            <el-select v-model="form.bookingStatus" placeholder="请选择" style="width: 100%">
              <el-option
                v-for="item in bookingStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="共享占用" prop="sharedOccupancy">
            <el-radio-group v-model="form.sharedOccupancy">
              <el-radio :label="true">是</el-radio>
              <el-radio :label="false">否</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="需要运维确认" prop="needOpsConfirm">
            <el-radio-group v-model="form.needOpsConfirm">
              <el-radio :label="true">是</el-radio>
              <el-radio :label="false">否</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="运维联系人" prop="opsContactName">
            <el-input v-model.trim="form.opsContactName" placeholder="请输入" maxlength="100" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="运维联系电话" prop="opsContactPhone">
            <el-input v-model.trim="form.opsContactPhone" placeholder="请输入" maxlength="32" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="安全须知">
            <el-input v-model="form.safetyNoticeOverride" type="textarea" :rows="3" maxlength="2000" show-word-limit />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="注意事项">
            <el-input v-model="form.attentionNotesOverride" type="textarea" :rows="3" maxlength="2000" show-word-limit />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="使用说明">
            <el-input v-model="form.usageInstructionsOverride" type="textarea" :rows="3" maxlength="4000" show-word-limit />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="管理员备注">
            <el-input v-model="form.adminRemark" type="textarea" :rows="3" maxlength="500" show-word-limit />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保 存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="ScheduleResourceDialog">
import { listSceneResource } from '@/api/tournament/sceneResource'
import {
  getSceneScheduleResource,
  addSceneScheduleResource,
  updateSceneScheduleResource
} from '@/api/tournament/sceneScheduleResource'
import modal from '@/plugins/modal'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  schedule: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const bookingStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '待发布', value: 'READY' },
  { label: '开放预约', value: 'OPEN' },
  { label: '暂停预约', value: 'PAUSED' },
  { label: '已关闭', value: 'CLOSED' }
]

const formRef = ref(null)
const form = ref({})
const mode = ref('add')
const resourceOptions = ref([])
const resourceLoading = ref(false)
const submitLoading = ref(false)

const isEdit = computed(() => mode.value === 'edit')
const dialogTitle = computed(() => isEdit.value ? '编辑资源布置' : '新增资源布置')
const previewTotalWorkstations = computed(() => {
  const deviceCount = Number(form.value.deployedDeviceCount || 0)
  const workstationCount = Number(form.value.workstationsPerDevice || 0)
  return deviceCount > 0 && workstationCount > 0 ? deviceCount * workstationCount : 0
})

const positiveIntegerValidator = (message) => {
  return (_rule, value, callback) => {
    if (value === undefined || value === null || value === '') {
      callback(new Error(message))
      return
    }
    if (!Number.isInteger(Number(value)) || Number(value) <= 0) {
      callback(new Error(message))
      return
    }
    callback()
  }
}

const booleanRequiredValidator = (message) => {
  return (_rule, value, callback) => {
    if (value === undefined || value === null) {
      callback(new Error(message))
      return
    }
    callback()
  }
}

const timeRangeValidator = (_rule, _value, callback) => {
  const openTime = form.value.bookingOpenTime
  const closeTime = form.value.bookingCloseTime
  if (openTime && closeTime && new Date(openTime).getTime() > new Date(closeTime).getTime()) {
    callback(new Error('预约开放时间不能晚于关闭时间'))
    return
  }
  callback()
}

const rules = {
  resourceId: [{ required: true, message: '请选择资源', trigger: 'change' }],
  deployedDeviceCount: [{ validator: positiveIntegerValidator('部署设备数必须大于0'), trigger: 'change' }],
  workstationsPerDevice: [{ validator: positiveIntegerValidator('每台设备工位数必须大于0'), trigger: 'change' }],
  slotDurationMinutes: [{ validator: positiveIntegerValidator('单场周期必须大于0分钟'), trigger: 'change' }],
  sharedOccupancy: [{ validator: booleanRequiredValidator('请选择是否共享占用'), trigger: 'change' }],
  needOpsConfirm: [{ validator: booleanRequiredValidator('请选择是否需要运维确认'), trigger: 'change' }],
  bookingStatus: [{ required: true, message: '请选择预约状态', trigger: 'change' }],
  bookingOpenTime: [{ validator: timeRangeValidator, trigger: 'change' }],
  bookingCloseTime: [{ validator: timeRangeValidator, trigger: 'change' }]
}

function resetForm() {
  form.value = {
    scheduleResourceId: undefined,
    scheduleId: props.schedule?.scheduleId,
    resourceId: undefined,
    eventId: props.schedule?.competitionSeriesId,
    deploymentLocation: '',
    deployedDeviceCount: 1,
    workstationsPerDevice: 1,
    slotDurationMinutes: 30,
    sharedOccupancy: true,
    needOpsConfirm: false,
    opsContactName: '',
    opsContactPhone: '',
    bookingStatus: 'DRAFT',
    bookingOpenTime: undefined,
    bookingCloseTime: undefined,
    safetyNoticeOverride: '',
    attentionNotesOverride: '',
    usageInstructionsOverride: '',
    adminRemark: ''
  }
  nextTick(() => formRef.value?.clearValidate())
}

function loadEnabledResources() {
  resourceLoading.value = true
  return listSceneResource({
    pageNum: 1,
    pageSize: 9999,
    resourceStatus: 'ENABLED'
  }).then(response => {
    resourceOptions.value = response.rows || []
  }).finally(() => {
    resourceLoading.value = false
  })
}

function formatResourceLabel(item) {
  const brand = item.brandModel ? ` / ${item.brandModel}` : ''
  return `${item.resourceName || item.resourceCode}${brand}`
}

function handleResourceChange(resourceId) {
  const resource = resourceOptions.value.find(item => item.resourceId === resourceId)
  if (!resource) return
  form.value.workstationsPerDevice = resource.workstationCount
  form.value.slotDurationMinutes = resource.defaultSlotDurationMinutes
  form.value.sharedOccupancy = resource.defaultSharedOccupancy
  form.value.needOpsConfirm = resource.needOpsConfirm
  form.value.opsContactName = resource.opsContactName
  form.value.opsContactPhone = resource.opsContactPhone
  form.value.safetyNoticeOverride = resource.safetyNotice
  form.value.attentionNotesOverride = resource.attentionNotes
  form.value.usageInstructionsOverride = resource.usageInstructions
}

async function openAdd() {
  if (!props.schedule?.scheduleId) {
    modal.msgWarning('请先选择一个赛场安排')
    return
  }
  mode.value = 'add'
  resetForm()
  visible.value = true
  await loadEnabledResources()
}

async function openEdit(row) {
  if (!row?.scheduleResourceId) return
  mode.value = 'edit'
  resetForm()
  visible.value = true
  await loadEnabledResources()
  getSceneScheduleResource(row.scheduleResourceId).then(response => {
    form.value = {
      ...form.value,
      ...(response.data || {}),
      scheduleId: props.schedule?.scheduleId || response.data?.scheduleId
    }
    nextTick(() => formRef.value?.clearValidate())
  })
}

function buildSubmitPayload() {
  const payload = { ...form.value }
  delete payload.totalWorkstations
  return payload
}

function submitForm() {
  formRef.value?.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    const request = isEdit.value ? updateSceneScheduleResource : addSceneScheduleResource
    request(buildSubmitPayload()).then(() => {
      modal.msgSuccess('保存成功')
      visible.value = false
      emit('success')
    }).finally(() => {
      submitLoading.value = false
    })
  })
}

function handleClosed() {
  submitLoading.value = false
}

defineExpose({
  openAdd,
  openEdit
})
</script>
