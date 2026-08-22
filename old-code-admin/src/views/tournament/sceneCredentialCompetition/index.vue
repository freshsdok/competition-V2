<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="90px">
      <el-form-item label="赛事" prop="competitionSeriesId">
        <el-select v-model="queryParams.competitionSeriesId" placeholder="请选择赛事" clearable filterable style="width: 260px">
          <el-option
            v-for="item in competitionOptions"
            :key="item.competitionSeriesId"
            :label="item.competitionName"
            :value="item.competitionSeriesId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="证件类型" prop="credentialType">
        <el-select v-model="queryParams.credentialType" placeholder="请选择" clearable style="width: 150px">
          <el-option
            v-for="item in credentialTypeQueryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="对象编码" prop="subjectCode">
        <el-input v-model.trim="queryParams.subjectCode" placeholder="请输入对象编码" clearable style="width: 170px" />
      </el-form-item>
      <el-form-item label="证件编号" prop="credentialNo">
        <el-input v-model.trim="queryParams.credentialNo" placeholder="请输入证件编号" clearable style="width: 190px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
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
          v-hasPermi="['competition:sceneCredential:add']"
        >
          新增发证
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="credentialList" stripe>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="证件编号" prop="credentialNo" min-width="190" show-overflow-tooltip />
      <el-table-column label="赛事" prop="competitionName" min-width="180" show-overflow-tooltip />
      <el-table-column label="证件名称" min-width="130" align="center">
        <template #default="{ row }">
          <el-tag :type="credentialTypeTagType(row.credentialType)">
            {{ row.credentialName || credentialTypeLabel(row.credentialType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发证对象" min-width="190" show-overflow-tooltip>
        <template #default="{ row }">
          <div>{{ row.teamName || row.userName || '-' }}</div>
          <div class="muted">{{ subjectTypeLabel(row.subjectType) }} · {{ row.subjectCode || row.teamCode || row.userId || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="能力" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ abilityText(row.abilityJson) }}</template>
      </el-table-column>
      <el-table-column label="报道/资料" width="150" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.reportStatus === '1' ? 'success' : 'info'">报道</el-tag>
          <el-tag size="small" :type="row.materialStatus === '1' ? 'success' : 'info'">资料</el-tag>
          <div v-if="row.materialStatus === '1' && row.materialDelegateName" class="muted">
            {{ row.materialDelegateRelation === 'TEAM_MEMBER' ? '代领' : '领取' }}：{{ row.materialDelegateName }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="证件状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="tagType(credentialStatusOptions, row.credentialStatus)">
            {{ tagLabel(credentialStatusOptions, row.credentialStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" prop="updateTime" width="170" />
      <el-table-column label="操作" width="130" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)" v-hasPermi="['competition:sceneCredential:query']">详情</el-button>
          <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['competition:sceneCredential:remove']">删除</el-button>
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

    <el-dialog title="大赛级直接发证" v-model="open" width="860px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="赛事" prop="competitionSeriesId">
              <el-select v-model="form.competitionSeriesId" placeholder="请选择赛事" filterable style="width: 100%">
                <el-option
                  v-for="item in competitionOptions"
                  :key="item.competitionSeriesId"
                  :label="item.competitionName"
                  :value="item.competitionSeriesId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="证件类型" prop="credentialType">
              <el-select v-model="form.credentialType" placeholder="请选择证件类型" style="width: 100%" @change="handleCredentialTypeChange">
                <el-option
                  v-for="item in credentialTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                  :disabled="item.disabled"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="展示名称" prop="credentialName">
              <el-input v-model.trim="form.credentialName" placeholder="默认按证件类型生成" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="发证用户" required>
              <div class="credential-user-picker">
                <el-input
                  :model-value="formatSelectedUserLabel()"
                  placeholder="请选择用户"
                  readonly
                  clearable
                  @clear="clearSelectedUsers"
                >
                  <template #append>
                    <el-button icon="User" @click="openUserSelectDialog">选择</el-button>
                  </template>
                </el-input>
                <el-button v-if="hasSelectedUsers" link type="danger" @click="clearSelectedUsers">清空</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="用户组白名单">
              <div class="credential-user-picker">
                <el-input
                  :model-value="formatSelectedUserGroupLabel()"
                  placeholder="请选择用户组"
                  readonly
                >
                  <template #append>
                    <el-button icon="UserFilled" @click="openGroupSelectDialog">选择</el-button>
                  </template>
                </el-input>
              </div>
            </el-form-item>
          </el-col>
          <el-col v-if="selectedUsers.length > 0" :span="24">
            <el-form-item label="已选用户">
              <div class="selected-user-tags">
                <el-tag
                  v-for="user in selectedUsers.slice(0, 16)"
                  :key="getUserId(user)"
                  closable
                  @close="removeSelectedUser(user)"
                >
                  {{ formatUserTag(user) }}
                </el-tag>
                <el-tag v-if="selectedUsers.length > 16" type="info">
                  +{{ selectedUsers.length - 16 }}
                </el-tag>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitting" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="选择用户" v-model="userDialogOpen" width="980px" append-to-body>
      <el-form ref="userQueryRef" :model="userQuery" :inline="true" label-width="78px">
        <el-form-item label="用户账号" prop="userName">
          <el-input
            v-model.trim="userQuery.userName"
            placeholder="请输入"
            clearable
            style="width: 160px"
            @keyup.enter="handleUserQuery"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input
            v-model.trim="userQuery.nickName"
            placeholder="请输入"
            clearable
            style="width: 150px"
            @keyup.enter="handleUserQuery"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phonenumber">
          <el-input
            v-model.trim="userQuery.phonenumber"
            placeholder="请输入"
            clearable
            style="width: 150px"
            @keyup.enter="handleUserQuery"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model.trim="userQuery.email"
            placeholder="请输入"
            clearable
            style="width: 180px"
            @keyup.enter="handleUserQuery"
          />
        </el-form-item>
        <el-form-item label="学校" prop="schoolName">
          <el-input
            v-model.trim="userQuery.schoolName"
            placeholder="请输入"
            clearable
            style="width: 180px"
            @keyup.enter="handleUserQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userQuery.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleUserQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetUserQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        ref="userTableRef"
        v-loading="userLoading"
        :data="userList"
        row-key="userId"
        stripe
        height="420"
        @selection-change="handleUserSelectionChange"
        @row-dblclick="handleUserRowDblClick"
      >
        <el-table-column type="selection" reserve-selection width="50" align="center" />
        <el-table-column label="用户账号" prop="userName" min-width="130" show-overflow-tooltip />
        <el-table-column label="姓名" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ getUserRealName(row) }}</template>
        </el-table-column>
        <el-table-column label="手机号" prop="phonenumber" min-width="120" show-overflow-tooltip />
        <el-table-column label="邮箱" prop="email" min-width="170" show-overflow-tooltip />
        <el-table-column label="学校/机构" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getUserSchoolName(row) || row.orgName || '-' }}</template>
        </el-table-column>
        <el-table-column label="认证状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getAuthStatusType(row?.authInfo?.authStatus)">
              {{ getAuthStatusLabel(row?.authInfo?.authStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="String(row.status) === '0' ? 'success' : 'info'">
              {{ String(row.status) === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="userTotal > 0"
        :total="userTotal"
        v-model:page="userQuery.pageNum"
        v-model:limit="userQuery.pageSize"
        @pagination="getUserList"
      />

      <template #footer>
        <div class="dialog-footer user-dialog-footer">
          <span class="muted">{{ formatUserDialogFooterText() }}</span>
          <span>
            <el-button @click="userDialogOpen = false">取 消</el-button>
            <el-button type="primary" :disabled="isUserConfirmDisabled" @click="confirmUserSelection">确 定</el-button>
          </span>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="选择用户组" v-model="groupDialogOpen" width="900px" append-to-body>
      <el-form ref="groupQueryRef" :model="groupQuery" :inline="true" label-width="96px">
        <el-form-item label="用户组名称" prop="name">
          <el-input
            v-model.trim="groupQuery.name"
            placeholder="请输入"
            clearable
            style="width: 180px"
            @keyup.enter="handleGroupQuery"
          />
        </el-form-item>
        <el-form-item label="用户组管理员" prop="groupManager">
          <el-input
            v-model.trim="groupQuery.groupManager"
            placeholder="请输入"
            clearable
            style="width: 180px"
            @keyup.enter="handleGroupQuery"
          />
        </el-form-item>
        <el-form-item label="关联身份" prop="identifyType">
          <el-select v-model="groupQuery.identifyType" placeholder="请选择" clearable style="width: 180px">
            <el-option label="教师（已通过教师认证）" value="teacher" />
            <el-option label="学生（已通过学生认证）" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleGroupQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetGroupQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="groupLoading"
        :data="groupList"
        stripe
        highlight-current-row
        height="420"
        @current-change="handleGroupCurrentChange"
        @row-dblclick="handleGroupRowDblClick"
      >
        <el-table-column label="用户组名称" prop="name" min-width="180" show-overflow-tooltip />
        <el-table-column label="用户组管理员" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row?.groupManagerList?.map(item => item.userName).join(', ') || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="人数" prop="userIdCount" width="90" align="center" />
        <el-table-column label="用户组说明" prop="descripe" min-width="200" show-overflow-tooltip />
      </el-table>
      <pagination
        v-show="groupTotal > 0"
        :total="groupTotal"
        v-model:page="groupQuery.pageNum"
        v-model:limit="groupQuery.pageSize"
        @pagination="getGroupList"
      />

      <template #footer>
        <div class="dialog-footer user-dialog-footer">
          <span class="muted">{{ groupCurrentRow ? `当前选择：${groupCurrentRow.name}` : '未选择用户组' }}</span>
          <span>
            <el-button @click="groupDialogOpen = false">取 消</el-button>
            <el-button type="primary" :loading="groupImporting" :disabled="!groupCurrentRow" @click="confirmGroupSelection">确 定</el-button>
          </span>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="大赛级证件详情" v-model="detailOpen" width="760px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="证件编号">{{ detail.credentialNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="证件名称">{{ detail.credentialName || credentialTypeLabel(detail.credentialType) }}</el-descriptions-item>
        <el-descriptions-item label="赛事">{{ detail.competitionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="作用域">大赛级</el-descriptions-item>
        <el-descriptions-item label="对象类型">{{ subjectTypeLabel(detail.subjectType) }}</el-descriptions-item>
        <el-descriptions-item label="对象编码">{{ detail.subjectCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="对象名称">{{ detail.teamName || detail.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="证件状态">{{ tagLabel(credentialStatusOptions, detail.credentialStatus) }}</el-descriptions-item>
        <el-descriptions-item label="报道状态">{{ doneLabel(detail.reportStatus) }}</el-descriptions-item>
        <el-descriptions-item label="资料状态">{{ doneLabel(detail.materialStatus) }}</el-descriptions-item>
        <el-descriptions-item label="候场能力">无候场功能</el-descriptions-item>
        <el-descriptions-item label="能力">{{ abilityText(detail.abilityJson) }}</el-descriptions-item>
        <el-descriptions-item label="二维码内容" :span="2">
          <el-input :model-value="detail.qrContent || detail.credentialToken || '-'" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import RightToolbar from '@/components/RightToolbar'
import modal from '@/plugins/modal'
import { getSelectCompetitionList } from '@/api/certInterconnect/certConfig'
import { listUser } from '@/api/system/user'
import { systemUserGroupMangerList, systemUserGroupDetail } from '@/api/fileTask'
import {
  competitionDirectIssue,
  delCompetitionSceneCredential,
  getCompetitionSceneCredential,
  listCompetitionSceneCredential
} from '@/api/tournament/sceneCredentialCompetition'

const showSearch = ref(true)
const loading = ref(false)
const total = ref(0)
const credentialList = ref([])
const competitionOptions = ref([])
const open = ref(false)
const detailOpen = ref(false)
const detail = ref(null)
const queryRef = ref(null)
const formRef = ref(null)
const userQueryRef = ref(null)
const userTableRef = ref(null)
const groupQueryRef = ref(null)

const credentialTypeOptions = [
  { label: '参赛证', value: 'PARTICIPANT', type: 'success' },
  { label: '教师证', value: 'TEACHER', type: 'warning' },
  { label: '专家证', value: 'EXPERT', type: 'primary' },
  { label: '工作人员证', value: 'STAFF', type: 'info' },
  { label: '贵宾证（预留）', value: 'VIP', type: 'danger', disabled: true }
]
const credentialTypeQueryOptions = credentialTypeOptions.map(item => ({ ...item, disabled: false }))
const subjectTypeOptions = [
  { label: '个人/用户', value: 'USER' },
  { label: '团队', value: 'TEAM' },
  { label: '专家', value: 'EXPERT' },
  { label: '工作人员', value: 'STAFF' },
  { label: '贵宾（预留）', value: 'VIP', disabled: true }
]
const credentialStatusOptions = [
  { label: '有效', value: 'EFFECTIVE', type: 'success' },
  { label: '作废', value: 'REVOKED', type: 'danger' },
  { label: '过期', value: 'EXPIRED', type: 'info' }
]

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  competitionSeriesId: undefined,
  credentialType: undefined,
  subjectCode: undefined,
  credentialNo: undefined
})

const form = ref({})
const submitting = ref(false)
const selectedUsers = ref([])
const selectedUserGroup = ref(null)
const userDialogOpen = ref(false)
const userLoading = ref(false)
const userList = ref([])
const userTotal = ref(0)
const userDialogSelectedRows = ref([])
const syncingUserSelection = ref(false)
const userQuery = ref({
  pageNum: 1,
  pageSize: 10,
  userName: undefined,
  nickName: undefined,
  phonenumber: undefined,
  email: undefined,
  schoolName: undefined,
  status: '0'
})
const groupDialogOpen = ref(false)
const groupLoading = ref(false)
const groupImporting = ref(false)
const groupList = ref([])
const groupTotal = ref(0)
const groupCurrentRow = ref(null)
const groupQuery = ref({
  pageNum: 1,
  pageSize: 10,
  name: undefined,
  groupManager: undefined,
  identifyType: undefined
})
const rules = {
  competitionSeriesId: [{ required: true, message: '请选择赛事', trigger: 'change' }],
  credentialType: [{ required: true, message: '请选择证件类型', trigger: 'change' }]
}

const hasSelectedUsers = computed(() => selectedUsers.value.length > 0)
const isUserConfirmDisabled = computed(() => userDialogSelectedRows.value.length === 0)

onMounted(() => {
  loadCompetitionOptions()
  getList()
})

function loadCompetitionOptions() {
  getSelectCompetitionList().then(response => {
    const data = response.data || []
    competitionOptions.value = data.map(item => ({
      competitionId: item.competitionId,
      competitionSeriesId: item.competitionSeriesId,
      competitionSeriesName: item.competitionSeriesName,
      competitionName: `${item.competitionSeriesName || ''}${item.competitionName || ''}`
    }))
  })
}

function getList() {
  loading.value = true
  listCompetitionSceneCredential(queryParams.value).then(response => {
    credentialList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function getUserList() {
  userLoading.value = true
  listUser(userQuery.value).then(response => {
    userList.value = response.rows || []
    userTotal.value = response.total || 0
    nextTick(() => syncUserTableSelection())
  }).finally(() => {
    userLoading.value = false
  })
}

function getGroupList() {
  groupLoading.value = true
  systemUserGroupMangerList(groupQuery.value).then(response => {
    groupList.value = response.rows || []
    groupTotal.value = response.total || 0
  }).finally(() => {
    groupLoading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    competitionSeriesId: undefined,
    credentialType: undefined,
    subjectCode: undefined,
    credentialNo: undefined
  }
  getList()
}

function resetForm() {
  selectedUsers.value = []
  selectedUserGroup.value = null
  form.value = {
    competitionSeriesId: undefined,
    credentialType: 'PARTICIPANT',
    credentialName: '参赛证',
    subjectType: 'USER',
    subjectCode: '',
    subjectName: '',
    remark: undefined
  }
  formRef.value?.resetFields()
}

function handleAdd() {
  resetForm()
  open.value = true
}

function handleCredentialTypeChange(value) {
  form.value.credentialName = credentialTypeLabel(value)
}

function submitForm() {
  formRef.value?.validate(async valid => {
    if (!valid) return
    const users = mergeUsers(selectedUsers.value).filter(item => getUserId(item))
    if (users.length === 0) {
      modal.msgWarning('请选择发证用户')
      return
    }

    submitting.value = true
    let successCount = 0
    let failCount = 0
    try {
      for (const user of users) {
        try {
          await competitionDirectIssue(buildDirectIssuePayload(user))
          successCount++
        } catch (error) {
          failCount++
        }
      }
    } finally {
      submitting.value = false
    }

    if (successCount > 0) {
      const failText = failCount > 0 ? `，失败 ${failCount} 张` : ''
      modal.msgSuccess(`发证成功 ${successCount} 张${failText}`)
      open.value = false
      selectedUsers.value = []
      selectedUserGroup.value = null
      getList()
    } else {
      modal.msgWarning('发证失败，请检查所选用户后重试')
    }
  })
}

function cancel() {
  open.value = false
}

function handleUserQuery() {
  userQuery.value.pageNum = 1
  getUserList()
}

function resetUserQuery() {
  userQueryRef.value?.resetFields()
  userQuery.value = {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    nickName: undefined,
    phonenumber: undefined,
    email: undefined,
    schoolName: undefined,
    status: '0'
  }
  getUserList()
}

function openUserSelectDialog() {
  userDialogOpen.value = true
  userDialogSelectedRows.value = [...selectedUsers.value]
  resetUserQuery()
}

function handleUserRowDblClick(row) {
  const selected = userDialogSelectedRows.value.some(item => getUserId(item) === getUserId(row))
  userTableRef.value?.toggleRowSelection(row, !selected)
}

function syncUserTableSelection() {
  if (!userTableRef.value) return
  syncingUserSelection.value = true
  userTableRef.value.clearSelection()
  userList.value.forEach(row => {
    const selected = userDialogSelectedRows.value.some(item => getUserId(item) === getUserId(row))
    if (selected) {
      userTableRef.value.toggleRowSelection(row, true)
    }
  })
  nextTick(() => {
    syncingUserSelection.value = false
  })
}

function mergeUsers(users) {
  const uniqueMap = new Map()
  users.filter(item => getUserId(item)).forEach(item => {
    uniqueMap.set(getUserId(item), item)
  })
  return Array.from(uniqueMap.values())
}

function handleUserSelectionChange(rows) {
  if (syncingUserSelection.value) return
  const currentPageIds = new Set(userList.value.map(getUserId).filter(Boolean))
  const preservedRows = userDialogSelectedRows.value.filter(item => !currentPageIds.has(getUserId(item)))
  userDialogSelectedRows.value = mergeUsers([...preservedRows, ...rows])
}

function confirmUserSelection() {
  applySelectedUsers(userDialogSelectedRows.value)
  userDialogOpen.value = false
}

function applySelectedUsers(users) {
  selectedUsers.value = mergeUsers(users)
  const firstUser = selectedUsers.value[0]
  form.value.subjectCode = firstUser ? getUserId(firstUser) : ''
  form.value.subjectName = firstUser ? getIssueUserName(firstUser) : ''
}

function removeSelectedUser(user) {
  selectedUsers.value = selectedUsers.value.filter(item => getUserId(item) !== getUserId(user))
  if (selectedUsers.value.length === 0) {
    clearSelectedUsers()
    return
  }
  applySelectedUsers(selectedUsers.value)
}

function clearSelectedUsers() {
  selectedUsers.value = []
  selectedUserGroup.value = null
  form.value.subjectCode = ''
  form.value.subjectName = ''
}

function openGroupSelectDialog() {
  groupDialogOpen.value = true
  groupCurrentRow.value = null
  resetGroupQuery()
}

function handleGroupQuery() {
  groupQuery.value.pageNum = 1
  getGroupList()
}

function resetGroupQuery() {
  groupQueryRef.value?.resetFields()
  groupQuery.value = {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    groupManager: undefined,
    identifyType: undefined
  }
  getGroupList()
}

function handleGroupCurrentChange(row) {
  groupCurrentRow.value = row
}

function handleGroupRowDblClick(row) {
  groupCurrentRow.value = row
  confirmGroupSelection()
}

function confirmGroupSelection() {
  if (!groupCurrentRow.value) return
  groupImporting.value = true
  systemUserGroupDetail(groupCurrentRow.value.id).then(response => {
    const groupDetail = response.data || {}
    const whitelistUsers = groupDetail.userList || []
    if (whitelistUsers.length === 0) {
      modal.msgWarning('该用户组暂无白名单成员')
      return
    }
    selectedUserGroup.value = {
      ...groupCurrentRow.value,
      ...groupDetail,
      userIdCount: whitelistUsers.length
    }
    applySelectedUsers([...selectedUsers.value, ...whitelistUsers])
    modal.msgSuccess(`已添加 ${whitelistUsers.length} 名白名单成员`)
    groupDialogOpen.value = false
  }).finally(() => {
    groupImporting.value = false
  })
}

function buildDirectIssuePayload(user) {
  return {
    competitionSeriesId: form.value.competitionSeriesId,
    credentialType: form.value.credentialType,
    credentialName: form.value.credentialName,
    subjectType: 'USER',
    subjectCode: getUserId(user),
    subjectName: getIssueUserName(user),
    remark: form.value.remark
  }
}

function handleDetail(row) {
  getCompetitionSceneCredential(row.credentialId).then(response => {
    detail.value = response.data || row
    detailOpen.value = true
  })
}

function handleDelete(row) {
  modal.confirm(`确定删除证件“${row.credentialNo || ''}”吗？`).then(() => {
    return delCompetitionSceneCredential(row.credentialId)
  }).then(() => {
    modal.msgSuccess('删除成功')
    getList()
  })
}

function getUserId(row) {
  return row?.userId === undefined || row?.userId === null ? '' : String(row.userId)
}

function getIssueUserName(row) {
  return row?.userName || row?.authInfo?.realName || row?.realName || row?.nickName || getUserId(row)
}

function getUserRealName(row) {
  return row?.authInfo?.realName || row?.realName || row?.nickName || '-'
}

function getUserSchoolName(row) {
  return row?.schoolName || row?.identityInfoList?.[0]?.schoolName || ''
}

function getAuthStatusLabel(value) {
  const statusMap = {
    1: '未实名',
    2: '审核中',
    3: '已驳回',
    4: '待审核',
    5: '已认证',
    6: '认证失败'
  }
  return statusMap[value] || value || '-'
}

function getAuthStatusType(value) {
  if (value === '5' || value === 5) return 'success'
  if (value === '6' || value === 6 || value === '3' || value === 3) return 'danger'
  if (value === '2' || value === 2 || value === '4' || value === 4) return 'warning'
  return 'info'
}

function formatUserTag(user) {
  const account = getIssueUserName(user)
  const realName = getUserRealName(user)
  const nameText = realName && realName !== '-' && realName !== account ? `${account} / ${realName}` : account
  return `${nameText}（ID：${getUserId(user)}）`
}

function formatSelectedUserLabel() {
  if (selectedUsers.value.length === 0) return ''
  const names = selectedUsers.value.slice(0, 3).map(getIssueUserName).join('、')
  const suffix = selectedUsers.value.length > 3 ? `等 ${selectedUsers.value.length} 人` : `${selectedUsers.value.length} 人`
  return `${names}（${suffix}）`
}

function formatSelectedUserGroupLabel() {
  if (!selectedUserGroup.value) return ''
  const count = selectedUserGroup.value.userIdCount || selectedUserGroup.value.userList?.length || 0
  return count ? `${selectedUserGroup.value.name}（${count} 人）` : selectedUserGroup.value.name
}

function formatUserDialogFooterText() {
  return userDialogSelectedRows.value.length ? `已选择 ${userDialogSelectedRows.value.length} 人` : '未选择用户'
}

function tagLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function tagType(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.type : 'info'
}

function credentialTypeLabel(value) {
  return tagLabel(credentialTypeOptions, value)
}

function credentialTypeTagType(value) {
  return tagType(credentialTypeOptions, value)
}

function subjectTypeLabel(value) {
  return tagLabel(subjectTypeOptions, value)
}

function doneLabel(value) {
  return value === '1' ? '已完成' : '未完成'
}

function abilityText(json) {
  const ability = parseAbility(json)
  const labels = []
  if (ability.report) labels.push('报道')
  if (ability.material) labels.push('资料')
  if (ability.waiting) labels.push('候场')
  if (ability.review) labels.push('评审')
  if (ability.resourceReservation) labels.push('资源预约')
  if (ability.vipAccess) labels.push('贵宾通行')
  return labels.length ? labels.join(' / ') : '--'
}

function parseAbility(json) {
  if (!json) return {}
  try {
    return JSON.parse(json) || {}
  } catch (error) {
    return {}
  }
}
</script>

<style scoped lang="scss">
.credential-user-picker {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 8px;
}

.selected-user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 32px;
  align-items: center;
}

.user-dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.muted {
  color: #909399;
  font-size: 12px;
  line-height: 20px;
}

:deep(.el-tag + .el-tag) {
  margin-left: 4px;
}
</style>
