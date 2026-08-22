<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="120px">
      <el-form-item label="签到二维码名称" prop="codeConfigName">
        <el-input
          v-model.trim="queryParams.codeConfigName"
          placeholder="请输入"
          clearable
          style="width: 200px;"
        />
      </el-form-item>
      <el-form-item label="关联赛事" prop="competitionSeriesId">
        <el-select
          v-model="queryParams.competitionSeriesId"
          placeholder="请选择"
          clearable
          style="width: 200px;"
        >
          <el-option
            v-for="item in competitionOptions"
            :key="item.competitionSeriesId"
            :label="item.competitionName"
            :value="item.competitionSeriesId"
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
        >新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表区域 -->
    <el-table v-loading="loading" :data="tableList" stripe>
      <el-table-column
        label="签到二维码名称"
        align="left"
        prop="codeConfigName"
        min-width="160"
        show-overflow-tooltip
      />
      <el-table-column
        label="关联赛事"
        align="left"
        prop="competitionName"
        min-width="160"
        show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.competitionSeriesName || '' }}{{ scope.row.competitionName || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        label="比赛时间"
        align="center"
        prop="competitionTime"
        width="200"
        show-overflow-tooltip
      >
        <template #default="scope">
          {{ scope.row.competitionStartTime || '-' }} ~ {{ scope.row.competitionEndTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="赛事状态" align="center" prop="checkStatus" width="100">
        <template #default="scope">
            <dict-tag :options="competition_status" :value="scope.row.checkStatus" />
        </template>
      </el-table-column>
      <el-table-column
        label="用户组设置"
        align="left"
        prop="userGroupNames"
        min-width="180"
        show-overflow-tooltip
      >
        <template #default="scope">
          <div class="user-group-cell">
            <span class="user-group-text">{{ scope.row.userGroupNames || '-' }}</span>
            <el-button
              link
              type="primary"
              icon="Edit"
              class="edit-btn"
              @click="handleUserGroupSetting(scope.row)"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="handlePromptConfig(scope.row)"
          >提示语配置</el-button>
          <el-button
            link
            type="primary"
            @click="handleQrCodeManage(scope.row)"
          >二维码管理</el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row)"
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

    <!-- 新增/编辑弹框 -->
    <el-dialog :title="dialogTitle" v-model="dialogOpen" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item label="选择赛事" prop="competitionSeriesId">
          <el-select v-model="form.competitionSeriesId" placeholder="请选择赛事" style="width: 100%;">
            <el-option
              v-for="item in competitionOptions"
              :key="item.competitionSeriesId"
              :label="item.competitionName"
              :value="item.competitionSeriesId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="签到二维码名称" prop="codeConfigName">
          <el-input v-model="form.codeConfigName" placeholder="请输入签到二维码名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="用户组" prop="userGroupIds">
          <div class="user-group-select" @click="openUserGroupSelect">
            <div v-if="selectedUserGroups.length > 0" class="selected-tags">
              <el-tag
                v-for="group in selectedUserGroups"
                :key="group.id"
                closable
                size="small"
                @close="removeUserGroup(group)"
              >
                {{ group.name }}
              </el-tag>
            </div>
            <span v-else class="placeholder">请选择用户组</span>
            <el-icon class="arrow-icon"><ArrowDown /></el-icon>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
          <el-button type="primary" @click="submitForm">保 存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户组设置弹框（列表页编辑用） -->
    <UserGroupDialog
      ref="userGroupDialogRef"
      @success="getList"
    />

    <!-- 新增用户组选择弹框 -->
    <UserGroupDialog
      ref="addUserGroupDialogRef"
      mode="select"
    />

    <!-- 提示语配置弹框 -->
    <PromptConfigDialog
      ref="promptConfigDialogRef"
      @success="getList"
    />
  </div>
</template>

<script setup name="SignInQrCode">
import {
  getSignInQrCodeList,
  addSignInQrCode,
  updateSignInQrCode,
  deleteSignInQrCode
} from '@/api/tournament/signInQrCode'
import { getSelectCompetitionList } from '@/api/certInterconnect/certConfig'
import Pagination from '@/components/Pagination'
import RightToolbar from '@/components/RightToolbar'
import modal from '@/plugins/modal'
import UserGroupDialog from './dialog/UserGroupDialog.vue'
import PromptConfigDialog from './dialog/PromptConfigDialog.vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useDict } from '@/utils/dict'
const { competition_status } = useDict("competition_status")
const router = useRouter()

// 搜索显示控制
const showSearch = ref(true)
const queryRef = ref(null)

// 表格数据相关
const loading = ref(false)
const tableList = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  codeConfigName: undefined,
  competitionSeriesId: undefined
})

// 关联赛事选项
const competitionOptions = ref([])

// 弹框相关
const dialogOpen = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = ref({
  id: undefined,
  codeConfigName: '',
  competitionSeriesId: undefined,
  userGroupIds: []
})
const rules = {
  codeConfigName: [
    { required: true, message: '请输入签到二维码名称', trigger: 'blur' }
  ],
  competitionSeriesId: [
    { required: true, message: '请选择赛事', trigger: 'change' }
  ]
}

// 新增弹框中的用户组选择
const selectedUserGroups = ref([])
const addUserGroupDialogRef = ref(null)

// 弹框组件引用
const userGroupDialogRef = ref(null)
const promptConfigDialogRef = ref(null)

// 赛事状态映射
function getStatusLabel(status) {
  const map = {
    '1': '进行中',
    '2': '未开始',
    '3': '已结束'
  }
  return map[status] || '未知'
}

function getStatusType(status) {
  const map = {
    '1': 'success',
    '2': 'info',
    '3': 'danger'
  }
  return map[status] || 'info'
}

/** 查询列表 */
function getList() {
  loading.value = true
  getSignInQrCodeList(queryParams.value).then(response => {
    tableList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
    loading.value = false
  })
}

/** 查询关联赛事选项 */
function loadCompetitionOptions() {
  getSelectCompetitionList().then(response => {
    if (response.code === 200) {
      // 复用之前的赛事数据结构
      const data = response.data || []
      competitionOptions.value = data.map(item => ({
        competitionSeriesId: item.competitionSeriesId,
        competitionName: item.competitionSeriesName + item.competitionName
      }))
    }
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    codeConfigName: undefined,
    competitionSeriesId: undefined
  }
  getList()
}

/** 新增按钮操作 */
function handleAdd() {
  resetForm()
  dialogTitle.value = '新增签到二维码配置'
  dialogOpen.value = true
}

/** 表单重置 */
function resetForm() {
  form.value = {
    id: undefined,
    codeConfigName: '',
    competitionSeriesId: undefined,
    userGroupIds: []
  }
  selectedUserGroups.value = []
  nextTick(() => {
    if (formRef.value) {
      formRef.value.resetFields()
    }
  })
}

/** 取消按钮 */
function cancel() {
  dialogOpen.value = false
  resetForm()
}

/** 提交表单 */
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      // 新增时的参数：codeConfigName, competitionSeriesId, userGroupIds
      const data = {
        codeConfigName: form.value.codeConfigName,
        competitionSeriesId: form.value.competitionSeriesId,
        userGroupIds: selectedUserGroups.value.map(item => item.id).join(',')
      }
      addSignInQrCode(data).then(response => {
        if (response.code === 200) {
          modal.msgSuccess('新增成功')
          dialogOpen.value = false
          getList()
        } else {
          modal.msgWarning(response.msg || '操作失败')
        }
      })
    }
  })
}

/** 打开用户组选择弹框（新增时） */
function openUserGroupSelect() {
  addUserGroupDialogRef.value.openDialogForAdd(selectedUserGroups.value, (groups) => {
    selectedUserGroups.value = groups
  })
}

/** 移除用户组（新增时） */
function removeUserGroup(group) {
  const index = selectedUserGroups.value.findIndex(g => g.id === group.id)
  if (index > -1) {
    selectedUserGroups.value.splice(index, 1)
  }
}

/** 用户组设置 */
function handleUserGroupSetting(row) {
  userGroupDialogRef.value.openDialog(row)
}

/** 提示语配置 */
function handlePromptConfig(row) {
  promptConfigDialogRef.value.openDialog(row)
}

/** 二维码管理 - 跳转到独立页面 */
function handleQrCodeManage(row) {
  router.push({
    path: '/tournament/qrCodeManage',
    query: { id: row.codeConfigId }
  })
}

/** 删除操作 */
function handleDelete(row) {
  modal.confirm('确定要删除吗？').then(() => {
    deleteSignInQrCode(row.codeConfigId).then(response => {
      if (response.code === 200) {
        modal.msgSuccess('删除成功')
        getList()
      } else {
        modal.msgWarning(response.msg || '删除失败')
      }
    })
  }).catch(() => {})
}

// 初始化
onMounted(() => {
  getList()
  loadCompetitionOptions()
})
</script>

<style scoped lang="scss">
.user-group-cell {
  display: flex;
  align-items: center;
  justify-content: flex-start;

  .user-group-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .edit-btn {
    margin-left: 8px;
    flex-shrink: 0;
  }
}

.user-group-select {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: border-color 0.2s;
  width: 100%;
  &:hover {
    border-color: #409eff;
  }

  .selected-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    padding: 4px 0;

    .el-tag {
      margin: 0;
    }
  }

  .placeholder {
    color: #a8abb2;
    font-size: 14px;
  }

  .arrow-icon {
    color: #a8abb2;
    font-size: 14px;
    margin-left: 8px;
  }
}
</style>
