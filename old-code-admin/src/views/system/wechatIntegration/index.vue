<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="微信OpenID" prop="wxOpenId">
        <el-input
          v-model.trim="queryParams.wxOpenId"
          placeholder="请输入微信OpenID"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input
          v-model.trim="queryParams.userId"
          placeholder="请输入用户ID"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="微信昵称" prop="nickName">
        <el-input
          v-model.trim="queryParams.nickName"
          placeholder="请输入微信昵称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="绑定状态" prop="statusList">
        <el-select v-model="queryParams.statusList" placeholder="请选择绑定状态" clearable multiple collapse-tags style="width: 200px">
          <el-option label="未绑定" value="0" />
          <el-option label="已绑定" value="1" />
          <el-option label="已解绑" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="绑定时间" prop="bindTime">
        <el-date-picker
          v-model="bindTimeRange"
          style="width: 240px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="最后活跃时间" prop="lastActivTime">
        <el-date-picker
          v-model="activTimeRange"
          style="width: 240px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:wechatIntegration:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="wechatIntegrationList" @selection-change="handleSelectionChange" style="width: 100%">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="用户ID" align="center" prop="userId" min-width="100" />
      <el-table-column label="微信OpenID" align="center" prop="wxOpenId" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="微信昵称" align="center" prop="nickName" min-width="120" />
      <el-table-column label="绑定时间" align="center" prop="bindTime" min-width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.bindTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="info">未绑定</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">已绑定</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="warning">已解绑</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后活跃时间" align="center" prop="lastActivTime" min-width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.lastActivTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150" fixed="right">
        <template #default="scope">
          <el-tooltip content="详情" placement="top">
            <el-button 
              link 
              type="primary" 
              icon="View" 
              @click="handleView(scope.row)"
              v-hasPermi="['system:wechatIntegration:query']"
            ></el-button>
          </el-tooltip>
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

    <!-- 查看详情对话框 -->
    <el-dialog title="微信集成详情" v-model="viewOpen" width="780px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="主键ID">{{ viewForm.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ viewForm.userId }}</el-descriptions-item>
        <el-descriptions-item label="微信OpenID" :span="2">{{ viewForm.wxOpenId }}</el-descriptions-item>
        <el-descriptions-item label="微信昵称">{{ viewForm.nickName }}</el-descriptions-item>
        <el-descriptions-item label="绑定状态">
          <el-tag v-if="viewForm.status === '0'" type="info">未绑定</el-tag>
          <el-tag v-else-if="viewForm.status === '1'" type="success">已绑定</el-tag>
          <el-tag v-else-if="viewForm.status === '2'" type="warning">已解绑</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="绑定时间" :span="2">
          {{ parseTime(viewForm.bindTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item label="最后活跃时间" :span="2">
          {{ parseTime(viewForm.lastActivTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ viewForm.createBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ parseTime(viewForm.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item label="更新人">{{ viewForm.updateBy }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ parseTime(viewForm.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WechatIntegration">
import { listWechatIntegration, getWechatIntegration, delWechatIntegration, resetWechatIntegration } from "@/api/system/wechatIntegration"

const { proxy } = getCurrentInstance()

const wechatIntegrationList = ref([])
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const bindTimeRange = ref([])
const activTimeRange = ref([])

const data = reactive({
  viewForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: undefined,
    wxOpenId: undefined,
    nickName: undefined,
    statusList: []
  }
})

const { queryParams, viewForm } = toRefs(data)

/** 查询微信集成列表 */
function getList() {
  loading.value = true
  let params = { ...queryParams.value }
  
  // 处理绑定时间范围
  if (bindTimeRange.value && bindTimeRange.value.length === 2) {
    params.params = params.params || {}
    params.params.beginBindTime = bindTimeRange.value[0]
    params.params.endBindTime = bindTimeRange.value[1]
  }
  
  // 处理最后活跃时间范围
  if (activTimeRange.value && activTimeRange.value.length === 2) {
    params.params = params.params || {}
    params.params.beginActivTime = activTimeRange.value[0]
    params.params.endActivTime = activTimeRange.value[1]
  }
  
  // 处理状态多选
  if (params.statusList && params.statusList.length > 0) {
    params.statusList = params.statusList.join(',')
  } else {
    delete params.statusList
  }
  
  listWechatIntegration(params).then(response => {
    wechatIntegrationList.value = response.rows
    total.value = response.total
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
  bindTimeRange.value = []
  activTimeRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

/** 查看详情 */
function handleView(row) {
  const id = row.id
  getWechatIntegration(id).then(response => {
    viewForm.value = response.data
    viewOpen.value = true
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const wechatIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除该数据项？').then(function() {
    return delWechatIntegration(wechatIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>
