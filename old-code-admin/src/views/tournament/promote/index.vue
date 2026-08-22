<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="100px">
      <el-form-item label="赛事名称" prop="competitionName">
        <el-input
          v-model.trim="queryParams.competitionName"
          placeholder="请输入赛事名称"
          clearable
          style="width: 200px;"
        />
      </el-form-item>
      <el-form-item label="状态" prop="competitionApplyStatus">
        <el-select
          v-model="queryParams.competitionApplyStatus"
          placeholder="请选择状态"
          clearable
          style="width: 200px;"
        >
          <el-option label="报名中" value="0" />
          <el-option label="未开始" value="1" />
          <el-option label="已结束" value="2" />
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
        >新建</el-button>
      </el-col>
    </el-row>

    <!-- 列表区域 -->
    <el-table v-loading="loading" :data="tableList" stripe>
      <el-table-column label="序号" align="center" type="index" width="60" />
      <el-table-column
        label="赛事名称"
        align="left"
        prop="competitionName"
        min-width="150"
        show-overflow-tooltip
      />
      <el-table-column
        label="赛事届数"
        align="center"
        prop="competitionSeriesName"
        width="80"
      />
      <el-table-column
        label="晋级队伍数"
        align="center"
        prop="teamNum"
        width="90">
        <template #default="scope">
          {{ scope.row.teamNum ?? '-' }}
        </template>
      </el-table-column>

      <el-table-column
        label="已报名数"
        align="center"
        prop="applyTeamNum"
        width="80">
        <template #default="scope">
          {{ scope.row.applyTeamNum ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column
        label="费用"
        align="left"
        width="100"
      >
        <template #default="scope">
          <div class="cell-inline">
            <span>{{ scope.row.fee ? '¥' + scope.row.fee : '-' }}</span>
            <el-button
              link
              type="primary"
              :icon="Edit"
              class="edit-icon-btn"
              @click="handleFee(scope.row)"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column
        label="报名开始时间"
        align="left"
        width="120"
      >
        <template #default="scope">
          <div class="cell-inline">
            <span>{{ scope.row.applyStartTime || '-' }}</span>
            <el-button
              link
              type="primary"
              :icon="Edit"
              class="edit-icon-btn"
              @click="handleRegistrationTime(scope.row)"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column
        label="报名结束时间"
        align="left"
        width="130"
      >
        <template #default="scope">
          <div class="cell-inline">
            <span>{{ scope.row.applyEndTime || '-' }}</span>
            <el-button
              link
              type="primary"
              :icon="Edit"
              class="edit-icon-btn"
              @click="handleRegistrationTime(scope.row)"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="competitionApplyStatus" width="90">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.competitionApplyStatus)">
            {{ getStatusLabel(scope.row.competitionApplyStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="提示语"
        align="left"
        min-width="160"
      >
        <template #default="scope">
          <div class="hint-text-wrapper">
            <div
              v-if="scope.row.promotedHint"
              class="hint-text-content"
              v-html="scope.row.promotedHint"
            />
            <span v-else class="hint-text-empty">未设置</span>
            <el-button
              link
              type="primary"
              :icon="Edit"
              class="edit-icon-btn"
              @click="handleHintText(scope.row)"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button
            link
            icon="View"
            type="primary"
            @click="handleDetail(scope.row)"
          >查看晋级名单</el-button>
          <el-button
            link
            icon="Refresh"
            type="success"
            @click="handleReimport(scope.row)"
          >重新导入</el-button>
          <el-button
            link
            icon="Delete"
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

    <!-- 新建晋级公示弹框 -->
    <AddOrReimportDialog
      ref="addDialogRef"
      mode="add"
      @success="getList"
    />

    <!-- 重新导入晋级名单弹框 -->
    <AddOrReimportDialog
      ref="reimportDialogRef"
      mode="reimport"
      @success="getList"
    />

    <!-- 编辑晋级信息弹框（费用、报名时间、提示语） -->
    <EditPromoteDialog
      ref="editPromoteDialogRef"
      @success="getList"
    />

    <!-- 明细弹框 -->
    <DetailDialog
      ref="detailDialogRef"
      @closed="getList"
    />
  </div>
</template>

<script setup name="Promote">
import { Edit } from '@element-plus/icons-vue'
import { getPromotionList, deletePromotion } from '@/api/tournament/promote'
import modal from '@/plugins/modal'
import Pagination from '@/components/Pagination'
import AddOrReimportDialog from './dialog/AddOrReimportDialog.vue'
import EditPromoteDialog from './dialog/EditPromoteDialog.vue'
import DetailDialog from './dialog/DetailDialog.vue'

const queryRef = ref(null)

// 表格数据相关
const loading = ref(false)
const tableList = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  competitionName: undefined,
  competitionApplyStatus: undefined
})

// 弹框组件引用
const addDialogRef = ref(null)
const reimportDialogRef = ref(null)
const editPromoteDialogRef = ref(null)
const detailDialogRef = ref(null)

// 状态类型映射
function getStatusType(status) {
  const map = {
    '1': 'success',
    '0': 'primary',
    '2': 'info'
  }
  return map[status] || 'info'
}

// 状态标签映射
function getStatusLabel(status) {
  const map = {
    '1': '报名中',
    '0': '未开始',
    '2': '已结束'
  }
  return map[status] || '-'
}

/** 查询列表 */
function getList() {
  loading.value = true
  const params = {
    pageNum: queryParams.value.pageNum,
    pageSize: queryParams.value.pageSize,
    competitionName: queryParams.value.competitionName,
    competitionApplyStatus: queryParams.value.competitionApplyStatus
  }

  getPromotionList(params).then(response => {
    tableList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
    loading.value = false
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
    competitionName: undefined,
    competitionApplyStatus: undefined
  }
  getList()
}

/** 新建按钮操作 */
function handleAdd() {
  addDialogRef.value.openDialog()
}

/** 费用设置 */
function handleFee(row) {
  editPromoteDialogRef.value.openDialog(row, 'fee')
}

/** 报名时间设置 */
function handleRegistrationTime(row) {
  editPromoteDialogRef.value.openDialog(row, 'time')
}

/** 提示信息设置 */
function handleHintText(row) {
  editPromoteDialogRef.value.openDialog(row, 'hint')
}

/** 重新导入 */
function handleReimport(row) {
  reimportDialogRef.value.openDialog(row)
}

/** 删除 */
function handleDelete(row) {
  modal.confirm('确定要删除该晋级公示吗？').then(() => {
    deletePromotion(row.promotedId).then(response => {
      if (response.code === 200) {
        modal.msgSuccess('删除成功')
        getList()
      } else {
        modal.msgError(response.msg || '删除失败')
      }
    })
  }).catch(() => {})
}

/** 明细 */
function handleDetail(row) {
  detailDialogRef.value.openDialog(row)
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style scoped>
.cell-inline {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.edit-icon-btn {
  padding: 2px;
  height: auto;
  font-size: 14px;
}

.hint-text-wrapper {
  display: flex;
  align-items: center;
  /* gap: 4px; */
}

.hint-text-content {
  /* flex: 1; */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
  max-height: 3em;
}

.hint-text-content :deep(img) {
  max-height: 1.5em;
  vertical-align: middle;
}

.hint-text-empty {
  /* flex: 1; */
  color: #909399;
}
</style>
