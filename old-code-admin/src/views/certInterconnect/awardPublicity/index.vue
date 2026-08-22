<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="100px">
      <el-form-item label="比赛名称" prop="competitionName">
        <el-input
          v-model.trim="queryParams.competitionName"
          placeholder="请输入比赛名称"
          clearable
          style="width: 200px;"
        />
      </el-form-item>
      <el-form-item label="创建人" prop="createBy">
        <el-input
          v-model.trim="queryParams.createBy"
          placeholder="请输入创建人"
          clearable
          style="width: 200px;"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="createTimeRange">
        <el-date-picker
          v-model="queryParams.createTimeRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 240px;"
        />
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
        label="比赛名称"
        align="left"
        prop="competitionName"
        min-width="200"
        show-overflow-tooltip
      />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        min-width="100"
      />
      <el-table-column
        label="更新时间"
        align="center"
        prop="updateTime"
        min-width="100"
      >
        <template #default="scope">
          {{ scope.row.updateTime || '-' }}
        </template>
      </el-table-column>

      <el-table-column
        label="创建人"
        align="center"
        prop="createBy"
        width="100"
        show-overflow-tooltip
      >
        <template #default="scope">
          {{ scope.row.createBy || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        label="修改人"
        align="center"
        prop="updateBy"
        width="100"
        show-overflow-tooltip
      >
        <template #default="scope">
          {{ scope.row.updateBy || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        label="过期时间"
        align="center"
        prop="expirationTime"
        min-width="100"
      >
        <template #default="scope">
          {{ scope.row.expirationTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="230" fixed="right">
        <template #default="scope">
          <el-button
            link
            icon="Clock"
            type="primary"
            @click="handleExpireTime(scope.row)"
          >过期时间</el-button>
          <el-button
            link
            icon="Warning"
            type="warning"
            @click="handleHintText(scope.row)"
          >提示信息</el-button>
          <el-button
            link
            icon="Refresh"
            type="success"
            @click="handleReimport(scope.row)"
          >重导</el-button>
          <el-button
            link
            icon="View"
            type="primary"
            @click="handleDetail(scope.row)"
          >明细</el-button>
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

    <!-- 新建获奖公示弹框 -->
    <AddOrReimportDialog
      ref="addDialogRef"
      mode="add"
      @success="getList"
    />

    <!-- 重新导入获奖名单弹框 -->
    <AddOrReimportDialog
      ref="reimportDialogRef"
      mode="reimport"
      @success="getList"
    />

    <!-- 设置过期时间弹框 -->
    <ExpireTimeDialog
      ref="expireTimeDialogRef"
      @success="getList"
    />

    <!-- 设置提示信息弹框 -->
    <HintTextDialog
      ref="hintTextDialogRef"
      @success="getList"
    />

    <!-- 明细弹框 -->
    <DetailDialog
      ref="detailDialogRef"
    />
  </div>
</template>

<script setup name="AwardPublicity">
import { getAwardPublicityList, deleteAwardPublicity } from '@/api/tournament/awardPublicity'
import modal from '@/plugins/modal'
import Pagination from '@/components/Pagination'
import AddOrReimportDialog from './dialog/AddOrReimportDialog.vue'
import ExpireTimeDialog from './dialog/ExpireTimeDialog.vue'
import HintTextDialog from './dialog/HintTextDialog.vue'
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
  createBy: undefined,
  createTimeRange: null
})

// 弹框组件引用
const addDialogRef = ref(null)
const reimportDialogRef = ref(null)
const expireTimeDialogRef = ref(null)
const hintTextDialogRef = ref(null)
const detailDialogRef = ref(null)

// 状态映射
function getStatusType(status) {
  const map = {
    '公示中': 'success',
    '未开始': 'primary',
    '已结束': 'info'
  }
  return map[status] || 'info'
}

/** 查询列表 */
function getList() {
  loading.value = true
  const params = {
    pageNum: queryParams.value.pageNum,
    pageSize: queryParams.value.pageSize,
    competitionName: queryParams.value.competitionName,
    createBy: queryParams.value.createBy
  }
  if (queryParams.value.createTimeRange && queryParams.value.createTimeRange.length === 2) {
    params.params = {
      beginTime: queryParams.value.createTimeRange[0],
      endTime: queryParams.value.createTimeRange[1]
    }
  }

  getAwardPublicityList(params).then(response => {
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
    createBy: undefined,
    createTimeRange: null
  }
  getList()
}

/** 新建按钮操作 */
function handleAdd() {
  addDialogRef.value.openDialog()
}

/** 过期时间设置 */
function handleExpireTime(row) {
  expireTimeDialogRef.value.openDialog(row)
}

/** 提示信息设置 */
function handleHintText(row) {
  hintTextDialogRef.value.openDialog(row)
}

/** 重新导入 */
function handleReimport(row) {
  reimportDialogRef.value.openDialog(row)
}

/** 明细 */
function handleDetail(row) {
  detailDialogRef.value.openDialog(row)
}

/** 删除 */
function handleDelete(row) {
  modal.confirm('确定要删除吗？').then(() => {
    deleteAwardPublicity(row.id).then(response => {
      if (response.code === 200) {
        modal.msgSuccess('删除成功')
        getList()
      } else {
        modal.msgError(response.msg || '删除失败')
      }
    })
  }).catch(() => {})
}

// 初始化
onMounted(() => {
  getList()
})
</script>
