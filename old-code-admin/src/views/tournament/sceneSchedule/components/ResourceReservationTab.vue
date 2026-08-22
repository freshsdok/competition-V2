<template>
  <div class="resource-reservation-tab">
    <el-empty v-if="!schedule?.scheduleId" description="请先选择一个赛场安排。" />
    <template v-else>
      <div class="pane-toolbar">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['competition:sceneScheduleResource:add']"
        >新增设备布置</el-button>
        <el-button icon="Refresh" @click="getList">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="resourceList" stripe>
        <el-table-column label="资源名称" prop="resourceName" min-width="160" show-overflow-tooltip />
        <el-table-column label="资源类型" prop="resourceType" width="120" align="center">
          <template #default="{ row }">
            <el-tag>{{ tagLabel(resourceTypeOptions, row.resourceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="部署位置" prop="deploymentLocation" min-width="150" show-overflow-tooltip />
        <el-table-column label="部署设备数" prop="deployedDeviceCount" width="110" align="center" />
        <el-table-column label="每台工位数" prop="workstationsPerDevice" width="110" align="center" />
        <el-table-column label="总工位数" prop="totalWorkstations" width="100" align="center" />
        <el-table-column label="单场周期" prop="slotDurationMinutes" width="100" align="center">
          <template #default="{ row }">{{ row.slotDurationMinutes }} 分钟</template>
        </el-table-column>
        <el-table-column label="共享占用" prop="sharedOccupancy" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.sharedOccupancy ? 'success' : 'info'">
              {{ row.sharedOccupancy ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="运维确认" prop="needOpsConfirm" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.needOpsConfirm ? 'warning' : 'info'">
              {{ row.needOpsConfirm ? '需要' : '不需要' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="运维联系人" prop="opsContactName" min-width="120" show-overflow-tooltip />
        <el-table-column label="运维电话" prop="opsContactPhone" min-width="130" show-overflow-tooltip />
        <el-table-column label="预约状态" prop="bookingStatus" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="tagType(bookingStatusOptions, row.bookingStatus)">
              {{ tagLabel(bookingStatusOptions, row.bookingStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开放时间" prop="bookingOpenTime" width="170" />
        <el-table-column label="关闭时间" prop="bookingCloseTime" width="170" />
        <el-table-column label="更新时间" prop="updateTime" width="170" />
        <el-table-column label="操作" width="420" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              @click="handleEdit(row)"
              v-hasPermi="['competition:sceneScheduleResource:edit']"
            >编辑</el-button>
            <el-button
              link
              type="success"
              :disabled="row.bookingStatus === 'OPEN'"
              @click="handleChangeStatus(row, 'OPEN')"
              v-hasPermi="['competition:sceneScheduleResource:changeBookingStatus']"
            >发布预约</el-button>
            <el-button
              link
              type="warning"
              :disabled="row.bookingStatus === 'PAUSED'"
              @click="handleChangeStatus(row, 'PAUSED')"
              v-hasPermi="['competition:sceneScheduleResource:changeBookingStatus']"
            >暂停预约</el-button>
            <el-button
              link
              type="info"
              :disabled="row.bookingStatus === 'CLOSED'"
              @click="handleChangeStatus(row, 'CLOSED')"
              v-hasPermi="['competition:sceneScheduleResource:changeBookingStatus']"
            >关闭预约</el-button>
            <el-button
              link
              type="primary"
              @click="handleConfigSlots(row)"
              v-hasPermi="['competition:sceneResourceSlot:list']"
            >配置时段</el-button>
            <el-button
              link
              type="primary"
              @click="handleScope(row)"
              v-hasPermi="['competition:sceneScheduleResource:edit']"
            >预约范围</el-button>
            <el-button
              link
              type="primary"
              @click="handleReservations(row)"
              v-hasPermi="['competition:sceneScheduleResource:list']"
            >预约记录</el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(row)"
              v-hasPermi="['competition:sceneScheduleResource:remove']"
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
    </template>

    <schedule-resource-dialog
      ref="dialogRef"
      v-model="dialogOpen"
      :schedule="schedule"
      @success="getList"
    />
    <resource-slot-dialog ref="slotDialogRef" />

    <el-dialog
      :title="scopeDialogTitle"
      v-model="scopeOpen"
      width="840px"
      append-to-body
    >
      <el-descriptions v-if="scopeResource" :column="3" border class="dialog-summary">
        <el-descriptions-item label="资源名称">{{ scopeResource.resourceName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="部署赛场">{{ scopeResource.scheduleName || schedule?.scheduleName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="绑定方式">MANUAL_BIND</el-descriptions-item>
      </el-descriptions>

      <div class="scope-toolbar">
        <el-select
          v-model="scopeForm.allowedScheduleIds"
          multiple
          collapse-tags
          collapse-tags-tooltip
          filterable
          clearable
          placeholder="批量选择允许预约来源赛场"
          style="width: 520px"
        >
          <el-option
            v-for="item in scheduleOptions"
            :key="item.scheduleId"
            :label="formatScheduleOption(item)"
            :value="item.scheduleId"
            :disabled="isScopeBound(item.scheduleId)"
          />
        </el-select>
        <el-button
          type="primary"
          :loading="scopeSubmitting"
          :disabled="!scopeForm.allowedScheduleIds.length"
          @click="submitScope"
        >批量添加绑定</el-button>
        <el-button icon="Refresh" @click="loadScopeList">刷新</el-button>
      </div>

      <el-table v-loading="scopeLoading" :data="scopeList" border>
        <el-table-column label="允许预约赛场" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.allowedScheduleName || row.allowedScheduleId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="来源类型" prop="sourceType" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="row.sourceType === 'AUTO_LOCATION' ? 'success' : 'primary'">
              {{ row.sourceType || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用" prop="enabled" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button
              link
              type="danger"
              :disabled="row.sourceType !== 'MANUAL_BIND'"
              @click="removeScope(row)"
            >移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="scopeOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      :title="reservationDialogTitle"
      v-model="reservationOpen"
      width="1280px"
      append-to-body
    >
      <div class="reservation-toolbar">
        <el-select
          v-model="reservationQuery.reservationStatus"
          placeholder="预约状态"
          clearable
          style="width: 150px"
          @change="handleReservationQuery"
        >
          <el-option
            v-for="item in reservationStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button icon="Refresh" @click="loadReservations">刷新</el-button>
      </div>
      <el-table v-loading="reservationLoading" :data="reservationList" border>
        <el-table-column label="预约主体" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ subjectTypeLabel(row.subjectType) }} / {{ row.subjectCode || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作人" prop="operatorName" min-width="110" show-overflow-tooltip />
        <el-table-column label="来源赛场" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.reservationSourceScheduleName || row.reservationSourceScheduleId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="组别" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.groupName || row.groupCode || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="预约时段" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.slotStartTime || '-' }} 至 {{ row.slotEndTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="占用人数" prop="occupyPeopleCount" width="90" align="center" />
        <el-table-column label="设备数" prop="reservedDeviceCount" width="80" align="center" />
        <el-table-column label="工位数" prop="reservedWorkstationCount" width="80" align="center" />
        <el-table-column label="共享快照" prop="sharedOccupancySnapshot" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.sharedOccupancySnapshot ? 'success' : 'info'">
              {{ row.sharedOccupancySnapshot ? '共享' : '独占' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="每台工位快照" prop="workstationCountSnapshot" width="120" align="center" />
        <el-table-column label="状态" prop="reservationStatus" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="reservationStatusType(row.reservationStatus)">
              {{ reservationStatusLabel(row.reservationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
      </el-table>
      <pagination
        v-show="reservationTotal > 0"
        :total="reservationTotal"
        v-model:page="reservationQuery.pageNum"
        v-model:limit="reservationQuery.pageSize"
        @pagination="loadReservations"
      />

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reservationOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ResourceReservationTab">
import {
  listSceneScheduleResource,
  delSceneScheduleResource,
  changeSceneScheduleResourceBookingStatus
} from '@/api/tournament/sceneScheduleResource'
import { listSceneSchedule } from '@/api/tournament/sceneSchedule'
import {
  listSceneResourceScheduleScope,
  batchEnsureSceneResourceScheduleScope,
  removeSceneResourceScheduleScope
} from '@/api/tournament/sceneResourceScheduleScope'
import { listSceneResourceReservation } from '@/api/tournament/sceneResourceReservation'
import Pagination from '@/components/Pagination'
import modal from '@/plugins/modal'
import ScheduleResourceDialog from './ScheduleResourceDialog.vue'
import ResourceSlotDialog from './ResourceSlotDialog.vue'

const props = defineProps({
  schedule: {
    type: Object,
    default: null
  },
  active: {
    type: Boolean,
    default: false
  }
})

const resourceTypeOptions = [
  { label: '房间', value: 'ROOM' },
  { label: '实验室', value: 'LAB' },
  { label: '设备', value: 'DEVICE' },
  { label: '工位', value: 'WORKSTATION' },
  { label: '服务器', value: 'SERVER' },
  { label: '软件', value: 'SOFTWARE' },
  { label: '其他', value: 'OTHER' }
]

const bookingStatusOptions = [
  { label: '草稿', value: 'DRAFT', type: 'info' },
  { label: '待发布', value: 'READY', type: 'warning' },
  { label: '开放预约', value: 'OPEN', type: 'success' },
  { label: '暂停预约', value: 'PAUSED', type: 'warning' },
  { label: '已关闭', value: 'CLOSED', type: 'info' }
]

const loading = ref(false)
const resourceList = ref([])
const total = ref(0)
const dialogRef = ref(null)
const slotDialogRef = ref(null)
const dialogOpen = ref(false)
const scopeOpen = ref(false)
const scopeLoading = ref(false)
const scopeSubmitting = ref(false)
const scopeResource = ref(null)
const scopeList = ref([])
const scheduleOptions = ref([])
const scopeForm = ref({
  allowedScheduleIds: []
})
const reservationOpen = ref(false)
const reservationLoading = ref(false)
const reservationResource = ref(null)
const reservationList = ref([])
const reservationTotal = ref(0)
const reservationQuery = ref({
  pageNum: 1,
  pageSize: 10,
  scheduleResourceId: undefined,
  reservationStatus: undefined
})
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  scheduleId: undefined
})

const reservationStatusOptions = [
  { label: '已预约', value: 'RESERVED', type: 'success' },
  { label: '已取消', value: 'CANCELLED', type: 'info' },
  { label: '已核销', value: 'CHECKED', type: 'warning' },
  { label: '已过期', value: 'EXPIRED', type: 'info' }
]

const scopeDialogTitle = computed(() => {
  const name = scopeResource.value?.resourceName || '资源'
  return `${name} - 可预约赛场范围`
})

const reservationDialogTitle = computed(() => {
  const name = reservationResource.value?.resourceName || '资源'
  return `${name} - 预约记录`
})

watch(
  () => [props.schedule?.scheduleId, props.active],
  ([scheduleId, active]) => {
    if (!active) return
    queryParams.value.scheduleId = scheduleId
    queryParams.value.pageNum = 1
    getList()
  },
  { immediate: true }
)

function tagLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function tagType(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.type : 'info'
}

function getList() {
  if (!props.schedule?.scheduleId) {
    resourceList.value = []
    total.value = 0
    return
  }
  loading.value = true
  queryParams.value.scheduleId = props.schedule.scheduleId
  listSceneScheduleResource(queryParams.value).then(response => {
    resourceList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleAdd() {
  dialogRef.value?.openAdd()
}

function handleEdit(row) {
  dialogRef.value?.openEdit(row)
}

function handleConfigSlots(row) {
  slotDialogRef.value?.open(row)
}

function handleScope(row) {
  scopeResource.value = row
  scopeForm.value.allowedScheduleIds = []
  scopeOpen.value = true
  loadScopeOptions()
  loadScopeList()
}

function loadScopeOptions() {
  return listSceneSchedule({
    pageNum: 1,
    pageSize: 9999,
    competitionSeriesId: props.schedule?.competitionSeriesId
  }).then(response => {
    scheduleOptions.value = response.rows || []
  })
}

function loadScopeList() {
  if (!scopeResource.value?.scheduleResourceId) return
  scopeLoading.value = true
  listSceneResourceScheduleScope({
    scheduleResourceId: scopeResource.value.scheduleResourceId
  }).then(response => {
    scopeList.value = response.data || []
  }).finally(() => {
    scopeLoading.value = false
  })
}

function formatScheduleOption(item) {
  const parts = [item.scheduleName, item.competitionStageName, item.competitionTrackName, item.secondLevelName]
    .filter(Boolean)
  return parts.join(' / ') || item.scheduleId
}

function isScopeBound(scheduleId) {
  return scopeList.value.some(item => item.allowedScheduleId === scheduleId && item.enabled === 1)
}

function submitScope() {
  const allowedScheduleIds = [...new Set(scopeForm.value.allowedScheduleIds || [])].filter(Boolean)
  if (!allowedScheduleIds.length) {
    modal.msgWarning('请选择允许预约来源赛场')
    return
  }
  scopeSubmitting.value = true
  batchEnsureSceneResourceScheduleScope({
    scheduleResourceId: scopeResource.value?.scheduleResourceId,
    resourceId: scopeResource.value?.resourceId,
    allowedScheduleIds
  }).then(() => {
    modal.msgSuccess(`已添加 ${allowedScheduleIds.length} 个赛场绑定`)
    scopeForm.value.allowedScheduleIds = []
    loadScopeList()
    getList()
  }).finally(() => {
    scopeSubmitting.value = false
  })
}

function removeScope(row) {
  modal.confirm(`确认移除“${row.allowedScheduleName || row.allowedScheduleId}”的预约权限吗？`).then(() => {
    return removeSceneResourceScheduleScope({
      scheduleResourceId: scopeResource.value?.scheduleResourceId,
      allowedScheduleId: row.allowedScheduleId
    })
  }).then(() => {
    modal.msgSuccess('移除成功')
    loadScopeList()
    getList()
  }).catch(() => {})
}

function handleReservations(row) {
  reservationResource.value = row
  reservationQuery.value = {
    pageNum: 1,
    pageSize: 10,
    scheduleResourceId: row.scheduleResourceId,
    reservationStatus: undefined
  }
  reservationOpen.value = true
  loadReservations()
}

function handleReservationQuery() {
  reservationQuery.value.pageNum = 1
  loadReservations()
}

function loadReservations() {
  if (!reservationResource.value?.scheduleResourceId) return
  reservationLoading.value = true
  reservationQuery.value.scheduleResourceId = reservationResource.value.scheduleResourceId
  listSceneResourceReservation(reservationQuery.value).then(response => {
    reservationList.value = response.rows || []
    reservationTotal.value = response.total || 0
  }).finally(() => {
    reservationLoading.value = false
  })
}

function subjectTypeLabel(value) {
  const map = {
    TEAM: '团队',
    USER: '个人'
  }
  return map[value] || value || '-'
}

function reservationStatusLabel(value) {
  return tagLabel(reservationStatusOptions, value)
}

function reservationStatusType(value) {
  return tagType(reservationStatusOptions, value)
}

function handleDelete(row) {
  modal.confirm(`确认移除资源“${row.resourceName || row.resourceId}”的赛场布置吗？`).then(() => {
    return delSceneScheduleResource(row.scheduleResourceId)
  }).then(() => {
    modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

function handleChangeStatus(row, bookingStatus) {
  const label = tagLabel(bookingStatusOptions, bookingStatus)
  modal.confirm(`确认将“${row.resourceName || row.resourceId}”切换为“${label}”吗？`).then(() => {
    return changeSceneScheduleResourceBookingStatus({
      scheduleResourceId: row.scheduleResourceId,
      bookingStatus
    })
  }).then(() => {
    modal.msgSuccess('状态已更新')
    getList()
  }).catch(() => {})
}

defineExpose({
  refresh: getList
})
</script>

<style scoped lang="scss">
.resource-reservation-tab {
  .pane-toolbar {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
  }

  .dialog-summary,
  .scope-toolbar,
  .reservation-toolbar {
    margin-bottom: 12px;
  }

  .scope-toolbar,
  .reservation-toolbar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
