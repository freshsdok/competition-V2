<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="用户账号" prop="userName">
            <el-input
               v-model.trim="queryParams.userName"
               placeholder="请输入用户账号"
               clearable
               style="width: 200px;"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select
               v-model="queryParams.status"
               placeholder="请选择状态"
               clearable
               style="width: 200px"
            >
               <el-option label="成功" value="0" />
               <el-option label="失败" value="1" />
            </el-select>
         </el-form-item>
         <el-form-item label="IP地址" prop="ipaddr">
            <el-input
               v-model.trim="queryParams.ipaddr"
               placeholder="请输入IP地址"
               clearable
               style="width: 200px;"
               @change="handleQuery"
            />
         </el-form-item>
         <el-form-item label="浏览器" prop="browser">
            <el-input
               v-model.trim="queryParams.browser"
               placeholder="请输入浏览器"
               clearable
               style="width: 200px;"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="访问时间">
            <el-date-picker
               v-model="dateRange"
               value-format="YYYY-MM-DD"
               type="daterange"
               range-separator="-"
               start-placeholder="开始日期"
               end-placeholder="结束日期"
               style="width: 240px"
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
               v-hasPermi="['system:logininfor:remove']"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               @click="handleClean"
               v-hasPermi="['system:logininfor:remove']"
            >清空</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="primary"
               plain
               icon="Unlock"
               :disabled="single"
               @click="handleUnlock"
               v-hasPermi="['system:logininfor:unlock']"
            >解锁</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['system:logininfor:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table ref="logininforRef" v-loading="loading" :data="logininforList" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
         <el-table-column type="selection" width="50" align="center" />
         <el-table-column label="序号" align="left" type="index" width="50" /> 
         <el-table-column label="用户账号" align="center" prop="userName" width="120" :show-overflow-tooltip="true" sortable="custom" :sort-orders="['descending', 'ascending']" />
         <el-table-column label="状态" align="center" prop="status" width="80">
            <template #default="scope">
               <el-tag v-if="scope.row.status === '0'" type="success">成功</el-tag>
               <el-tag v-else type="danger">失败</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="IP地址" align="center" prop="ipaddr" width="140" :show-overflow-tooltip="true" />
         <el-table-column label="描述" align="center" prop="msg" :show-overflow-tooltip="true" />
         <el-table-column label="操作系统" align="center" prop="os" width="140" :show-overflow-tooltip="true" />
         <el-table-column label="浏览器" align="center" prop="browser" width="120" :show-overflow-tooltip="true" />
         <el-table-column label="访问编号" align="center" prop="infoId" width="100" />
         <el-table-column label="访问时间" align="center" prop="accessTime" sortable="custom" :sort-orders="['descending', 'ascending']" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.accessTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
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
   </div>
</template>

<script setup name="Logininfor">
import { list, delLogininfor, cleanLogininfor, unlockLogininfor } from "@/api/system/logininfor"

const { proxy } = getCurrentInstance()

const logininforList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const selectName = ref("")
const total = ref(0)
const dateRange = ref([])
const defaultSort = ref({ prop: "accessTime", order: "descending" })

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  userName: undefined,
  ipaddr: undefined,
  browser: undefined,
  status: undefined,
  orderByColumn: undefined,
  isAsc: undefined
})

/** 查询登录日志列表 */
function getList() {
  loading.value = true
  list(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    logininforList.value = response.rows
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
  dateRange.value = []
  proxy.resetForm("queryRef")
  queryParams.value.pageNum = 1
  proxy.$refs["logininforRef"].sort(defaultSort.value.prop, defaultSort.value.order)
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.infoId)
  multiple.value = !selection.length
  single.value = selection.length != 1
  selectName.value = selection.map(item => item.userName)
}

/** 排序触发事件 */
function handleSortChange(column, prop, order) {
  queryParams.value.orderByColumn = column.prop
  queryParams.value.isAsc = column.order
  getList()
}

/** 删除按钮操作 */
function handleDelete(row) {
  const infoIds = row.infoId || ids.value
  proxy.$modal.confirm('是否确认删除访问编号为"' + infoIds + '"的数据项?').then(function () {
    return delLogininfor(infoIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 清空按钮操作 */
function handleClean() {
  proxy.$modal.confirm("是否确认清空所有登录日志数据项?").then(function () {
    return cleanLogininfor()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("清空成功")
  }).catch(() => {})
}

/** 解锁按钮操作 */
function handleUnlock() {
  const username = selectName.value
  proxy.$modal.confirm('是否确认解锁该数据项？').then(function () {
    return unlockLogininfor(username)
  }).then(() => {
    proxy.$modal.msgSuccess("用户" + username + "解锁成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("system/logininfor/export", {
    ...queryParams.value,
  }, `登录日志.xlsx`)
}

getList()
</script>
