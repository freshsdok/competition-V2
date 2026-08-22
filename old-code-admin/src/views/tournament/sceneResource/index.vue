<template>
  <div class="app-container scene-resource-page">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="96px">
      <el-form-item label="资源编号" prop="resourceCode">
        <el-input
          v-model.trim="queryParams.resourceCode"
          placeholder="请输入"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="资源名称" prop="resourceName">
        <el-input
          v-model.trim="queryParams.resourceName"
          placeholder="请输入"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="资源类型" prop="resourceType">
        <el-select v-model="queryParams.resourceType" placeholder="请选择" clearable style="width: 150px">
          <el-option
            v-for="item in resourceTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="资源状态" prop="resourceStatus">
        <el-select v-model="queryParams.resourceStatus" placeholder="请选择" clearable style="width: 150px">
          <el-option
            v-for="item in resourceStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['competition:sceneResource:add']"
        >新增资源</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
          v-hasPermi="['competition:sceneResource:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      v-loading="loading"
      :data="resourceList"
      stripe
      row-key="resourceId"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="资源编号" prop="resourceCode" min-width="150" show-overflow-tooltip />
      <el-table-column label="资源名称" prop="resourceName" min-width="180" show-overflow-tooltip />
      <el-table-column label="资源类型" prop="resourceType" width="120" align="center">
        <template #default="{ row }">
          <el-tag>{{ tagLabel(resourceTypeOptions, row.resourceType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="资源状态" prop="resourceStatus" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="tagType(resourceStatusOptions, row.resourceStatus)">
            {{ tagLabel(resourceStatusOptions, row.resourceStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="品牌型号" prop="brandModel" min-width="150" show-overflow-tooltip />
      <el-table-column label="设备数量" prop="deviceQuantity" width="95" align="center" />
      <el-table-column label="单台工位数" prop="workstationCount" width="110" align="center" />
      <el-table-column label="默认周期" prop="defaultSlotDurationMinutes" width="110" align="center">
        <template #default="{ row }">{{ row.defaultSlotDurationMinutes }} 分钟</template>
      </el-table-column>
      <el-table-column label="共享占用" prop="defaultSharedOccupancy" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.defaultSharedOccupancy ? 'success' : 'info'">
            {{ row.defaultSharedOccupancy ? '是' : '否' }}
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
      <el-table-column label="排序" prop="sortOrder" width="80" align="center" />
      <el-table-column label="更新时间" prop="updateTime" width="170" />
      <el-table-column label="操作" width="270" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="handleUpdate(row)"
            v-hasPermi="['competition:sceneResource:edit']"
          >修改</el-button>
          <el-dropdown
            trigger="click"
            @command="status => handleStatusCommand(row, status)"
            v-hasPermi="['competition:sceneResource:changeStatus']"
          >
            <el-button link type="warning">
              状态<el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in nextStatusOptions(row.resourceStatus)"
                  :key="item.value"
                  :command="item.value"
                >
                  {{ item.actionLabel }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button
            link
            type="danger"
            @click="handleDelete(row)"
            v-hasPermi="['competition:sceneResource:remove']"
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

    <el-dialog :title="dialogTitle" v-model="dialogOpen" width="920px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="150px">
        <el-row :gutter="16">
          <el-col v-if="form.resourceId" :span="12">
            <el-form-item label="资源编号" prop="resourceCode">
              <el-input v-model.trim="form.resourceCode" disabled placeholder="自动生成" maxlength="64" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源名称" prop="resourceName">
              <el-input v-model.trim="form.resourceName" placeholder="请输入资源名称" maxlength="255" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源类型" prop="resourceType">
              <el-select v-model="form.resourceType" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="item in resourceTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源状态" prop="resourceStatus">
              <el-select v-model="form.resourceStatus" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="item in resourceStatusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌型号" prop="brandModel">
              <el-input v-model.trim="form.brandModel" placeholder="请输入品牌型号" maxlength="255" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备数量" prop="deviceQuantity">
              <el-input-number v-model="form.deviceQuantity" :min="1" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单台设备工位数" prop="workstationCount">
              <el-input-number v-model="form.workstationCount" :min="1" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认单场周期（分钟）" prop="defaultSlotDurationMinutes">
              <div class="duration-input">
                <el-input-number v-model="form.defaultSlotDurationMinutes" :min="1" :step="5" controls-position="right" />
                <span class="duration-unit">分钟</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可跨团队共享" prop="defaultSharedOccupancy">
              <el-radio-group v-model="form.defaultSharedOccupancy">
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
            <el-form-item label="安全须知" prop="safetyNotice">
              <el-input v-model="form.safetyNotice" type="textarea" :rows="3" maxlength="2000" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="注意事项" prop="attentionNotes">
              <el-input v-model="form.attentionNotes" type="textarea" :rows="3" maxlength="2000" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="主要参数列表">
              <div class="parameter-editor">
                <div v-if="parameterRows.length === 0" class="parameter-empty">
                  <span>暂无参数</span>
                  <el-button type="primary" plain icon="Plus" @click="addParameterRow">添加参数</el-button>
                </div>
                <div v-for="(item, index) in parameterRows" :key="item.id" class="parameter-row">
                  <el-input v-model.trim="item.name" placeholder="请输入参数名称" maxlength="100" />
                  <el-input v-model.trim="item.value" placeholder="请输入参数值" maxlength="255" />
                  <el-button type="danger" plain icon="Delete" @click="removeParameterRow(index)">删除</el-button>
                </div>
                <el-button
                  v-if="parameterRows.length > 0"
                  type="primary"
                  plain
                  icon="Plus"
                  class="parameter-add"
                  @click="addParameterRow"
                >添加参数</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="使用说明" prop="usageInstructions">
              <el-input v-model="form.usageInstructions" type="textarea" :rows="4" maxlength="4000" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="管理员备注" prop="adminRemark">
              <el-input v-model="form.adminRemark" type="textarea" :rows="3" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogOpen = false">取 消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">保 存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SceneResource">
import {
  listSceneResource,
  getSceneResource,
  addSceneResource,
  updateSceneResource,
  delSceneResource,
  changeSceneResourceStatus
} from '@/api/tournament/sceneResource'
import Pagination from '@/components/Pagination'
import RightToolbar from '@/components/RightToolbar'
import modal from '@/plugins/modal'

const showSearch = ref(true)
const queryRef = ref(null)
const formRef = ref(null)
const loading = ref(false)
const submitLoading = ref(false)
const resourceList = ref([])
const total = ref(0)
const selectedIds = ref([])
const dialogOpen = ref(false)
const dialogTitle = ref('')
const parameterRows = ref([])
let parameterRowSeed = 0

const resourceTypeOptions = [
  { label: '房间', value: 'ROOM' },
  { label: '实验室', value: 'LAB' },
  { label: '设备', value: 'DEVICE' },
  { label: '工位', value: 'WORKSTATION' },
  { label: '服务器', value: 'SERVER' },
  { label: '软件', value: 'SOFTWARE' },
  { label: '其他', value: 'OTHER' }
]

const resourceStatusOptions = [
  { label: '启用', value: 'ENABLED', type: 'success', actionLabel: '启用' },
  { label: '停用', value: 'DISABLED', type: 'info', actionLabel: '停用' },
  { label: '维护中', value: 'MAINTENANCE', type: 'warning', actionLabel: '设为维护中' }
]

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  resourceCode: undefined,
  resourceName: undefined,
  resourceType: undefined,
  resourceStatus: undefined
})

const form = ref({})

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

const rules = {
  resourceCode: [{ required: true, message: '资源编号不能为空', trigger: 'blur' }],
  resourceName: [{ required: true, message: '请输入资源名称', trigger: 'blur' }],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }],
  resourceStatus: [{ required: true, message: '请选择资源状态', trigger: 'change' }],
  deviceQuantity: [{ validator: positiveIntegerValidator('设备数量必须大于0'), trigger: 'change' }],
  workstationCount: [{ validator: positiveIntegerValidator('单台设备工位数必须大于0'), trigger: 'change' }],
  defaultSlotDurationMinutes: [{ validator: positiveIntegerValidator('默认单场周期必须大于0分钟'), trigger: 'change' }],
  defaultSharedOccupancy: [{ validator: booleanRequiredValidator('请选择可跨团队共享'), trigger: 'change' }],
  needOpsConfirm: [{ validator: booleanRequiredValidator('请选择是否需要运维确认'), trigger: 'change' }]
}

function createParameterRow(name = '', value = '') {
  parameterRowSeed += 1
  return {
    id: parameterRowSeed,
    name,
    value
  }
}

function normalizeText(value) {
  if (value === undefined || value === null) return ''
  return String(value).trim()
}

function formatParameterValue(value) {
  if (value === undefined || value === null) return ''
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

function parseParameterRows(parameterJson) {
  if (!parameterJson) return []
  let parsed
  try {
    parsed = typeof parameterJson === 'string' ? JSON.parse(parameterJson) : parameterJson
  } catch (_e) {
    return [createParameterRow('参数', String(parameterJson))]
  }
  if (Array.isArray(parsed)) {
    return parsed
      .map(item => {
        if (item && typeof item === 'object') {
          const itemName = item.name ?? item.paramName ?? item.parameterName ?? item.key ?? item.label ?? ''
          const itemValue = item.value ?? item.paramValue ?? item.parameterValue ?? item.val ?? ''
          const entries = Object.entries(item)
          if (!itemName && !itemValue && entries.length === 1) {
            return createParameterRow(String(entries[0][0]), formatParameterValue(entries[0][1]))
          }
          return createParameterRow(formatParameterValue(itemName), formatParameterValue(itemValue))
        }
        return createParameterRow('', formatParameterValue(item))
      })
      .filter(item => item.name || item.value)
  }
  if (parsed && typeof parsed === 'object') {
    return Object.entries(parsed)
      .map(([name, value]) => createParameterRow(String(name), formatParameterValue(value)))
      .filter(item => item.name || item.value)
  }
  return [createParameterRow('参数', formatParameterValue(parsed))]
}

function addParameterRow() {
  parameterRows.value.push(createParameterRow())
}

function removeParameterRow(index) {
  parameterRows.value.splice(index, 1)
}

function syncParameterJson() {
  const rows = []
  for (let index = 0; index < parameterRows.value.length; index += 1) {
    const name = normalizeText(parameterRows.value[index].name)
    const value = normalizeText(parameterRows.value[index].value)
    if (!name && !value) continue
    if (!name || !value) {
      modal.msgWarning(`请完善第 ${index + 1} 个主要参数的名称和值`)
      return false
    }
    rows.push({ name, value })
  }
  form.value.parameterJson = rows.length > 0 ? JSON.stringify(rows) : undefined
  return true
}

function padTime(value) {
  return String(value).padStart(2, '0')
}

function generateResourceCode() {
  const now = new Date()
  const timestamp = [
    now.getFullYear(),
    padTime(now.getMonth() + 1),
    padTime(now.getDate()),
    padTime(now.getHours()),
    padTime(now.getMinutes()),
    padTime(now.getSeconds())
  ].join('')
  const random = Math.random().toString(36).slice(2, 6).toUpperCase()
  return `RES-${timestamp}-${random}`
}

function tagLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function tagType(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.type : 'info'
}

function nextStatusOptions(currentStatus) {
  return resourceStatusOptions.filter(item => item.value !== currentStatus)
}

function getList() {
  loading.value = true
  listSceneResource(queryParams.value).then(response => {
    resourceList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function resetForm() {
  form.value = {
    resourceId: undefined,
    resourceCode: undefined,
    resourceName: undefined,
    resourceType: 'DEVICE',
    resourceStatus: 'ENABLED',
    brandModel: undefined,
    deviceQuantity: 1,
    workstationCount: 1,
    defaultSlotDurationMinutes: 30,
    defaultSharedOccupancy: true,
    needOpsConfirm: false,
    opsContactName: undefined,
    opsContactPhone: undefined,
    safetyNotice: undefined,
    attentionNotes: undefined,
    parameterJson: undefined,
    usageInstructions: undefined,
    imageUrls: undefined,
    adminRemark: undefined,
    sortOrder: 0
  }
  parameterRows.value = []
  formRef.value?.clearValidate()
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增资源'
  dialogOpen.value = true
}

function handleUpdate(row) {
  resetForm()
  getSceneResource(row.resourceId).then(response => {
    form.value = {
      ...form.value,
      ...(response.data || {})
    }
    parameterRows.value = parseParameterRows(form.value.parameterJson)
    dialogTitle.value = '修改资源'
    dialogOpen.value = true
  })
}

function submitForm() {
  if (!syncParameterJson()) return
  if (!form.value.resourceId && !form.value.resourceCode) {
    form.value.resourceCode = generateResourceCode()
  }
  formRef.value?.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    const request = form.value.resourceId ? updateSceneResource : addSceneResource
    request({ ...form.value }).then(() => {
      modal.msgSuccess('保存成功')
      dialogOpen.value = false
      getList()
    }).finally(() => {
      submitLoading.value = false
    })
  })
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.resourceId)
}

function handleDelete(row) {
  modal.confirm(`确认删除资源“${row.resourceName}”吗？`).then(() => {
    return delSceneResource(row.resourceId)
  }).then(() => {
    modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  modal.confirm(`确认删除选中的 ${selectedIds.value.length} 个资源吗？`).then(() => {
    return delSceneResource(selectedIds.value.join(','))
  }).then(() => {
    modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

function handleStatusCommand(row, resourceStatus) {
  const label = tagLabel(resourceStatusOptions, resourceStatus)
  modal.confirm(`确认将资源“${row.resourceName}”状态切换为“${label}”吗？`).then(() => {
    return changeSceneResourceStatus({
      resourceId: row.resourceId,
      resourceStatus
    })
  }).then(() => {
    modal.msgSuccess('状态已更新')
    getList()
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.scene-resource-page :deep(.el-table .cell) {
  line-height: 1.45;
}

.duration-input {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.duration-input :deep(.el-input-number) {
  flex: 1;
}

.duration-unit {
  flex: none;
  color: var(--el-text-color-regular);
  white-space: nowrap;
}

.parameter-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.parameter-empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 38px;
  padding: 0 12px;
  color: var(--el-text-color-secondary);
  border: 1px dashed var(--el-border-color);
  border-radius: 4px;
}

.parameter-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.4fr) auto;
  gap: 8px;
  align-items: center;
}

.parameter-add {
  align-self: flex-start;
}

@media (max-width: 768px) {
  .parameter-row {
    grid-template-columns: minmax(0, 1fr);
  }

  .parameter-row .el-button {
    justify-self: flex-start;
  }
}
</style>
