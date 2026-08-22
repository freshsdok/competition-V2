<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="操作用户" prop="operName">
        <el-input
          v-model.trim="queryParams.operName"
          placeholder="请输入操作用户"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="错误类型" prop="errorTypeList">
        <el-select v-model="queryParams.errorTypeList" placeholder="请选择错误类型" clearable multiple collapse-tags style="width: 240px">
          <el-option label="系统错误" value="系统错误" />
          <el-option label="业务错误" value="业务错误" />
          <el-option label="SQL错误" value="SQL错误" />
          <el-option label="网络错误" value="网络错误" />
        </el-select>
      </el-form-item>
      <el-form-item label="IP地址" prop="operIp">
        <el-input
          v-model.trim="queryParams.operIp"
          placeholder="请输入IP地址"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="处理状态" prop="statusList">
        <el-select v-model="queryParams.statusList" placeholder="请选择处理状态" clearable multiple collapse-tags style="width: 200px">
          <el-option label="未处理" value="0" />
          <el-option label="已处理" value="1" />
          <el-option label="已忽略" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="浏览器" prop="browser">
        <el-input
          v-model.trim="queryParams.browser"
          placeholder="请输入浏览器信息"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作时间" prop="errorTime">
        <el-date-picker
          v-model="errorTimeRange"
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
          v-hasPermi="['system:errorlog:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          @click="handleClean"
          v-hasPermi="['system:errorlog:remove']"
        >清空</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:errorlog:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="errorLogList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="left" type="index" width="50" />
      <el-table-column label="错误编号" align="center" prop="errorId" width="80" />
      <el-table-column label="错误类型" align="center" prop="errorType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.errorType === '系统错误'" type="danger">系统错误</el-tag>
          <el-tag v-else-if="scope.row.errorType === '业务错误'" type="warning">业务错误</el-tag>
          <el-tag v-else-if="scope.row.errorType === 'SQL错误'" type="danger">SQL错误</el-tag>
          <el-tag v-else-if="scope.row.errorType === '网络错误'" type="info">网络错误</el-tag>
          <el-tag v-else>{{ scope.row.errorType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="错误级别" align="center" prop="errorLevel" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.errorLevel === 'ERROR'" type="danger">错误</el-tag>
          <el-tag v-else-if="scope.row.errorLevel === 'WARN'" type="warning">警告</el-tag>
          <el-tag v-else-if="scope.row.errorLevel === 'FATAL'" type="danger">致命</el-tag>
          <el-tag v-else>{{ scope.row.errorLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="错误消息" align="center" prop="errorMessage" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="异常类" align="center" prop="exceptionClass" :show-overflow-tooltip="true" width="180" />
      <el-table-column label="操作用户" align="center" prop="operName" width="100" />
      <el-table-column label="操作IP" align="center" prop="operIp" width="130" />
      <el-table-column label="浏览器" align="center" prop="browser" width="100" />
      <el-table-column label="处理状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="danger">未处理</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">已处理</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="info">已忽略</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="错误时间" align="center" prop="errorTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.errorTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200" fixed="right">
        <template #default="scope">
          <el-button 
            link 
            type="primary" 
            icon="View" 
            @click="handleView(scope.row)"
            v-hasPermi="['system:errorlog:query']"
          >详情</el-button>
          <el-button 
            v-if="scope.row.status === '0'"
            link 
            type="success" 
            icon="Check" 
            @click="handleHandle(scope.row)"
            v-hasPermi="['system:errorlog:edit']"
          >处理</el-button>
          <el-button 
            v-if="scope.row.status === '0'"
            link 
            type="warning" 
            icon="Close" 
            @click="handleIgnore(scope.row)"
            v-hasPermi="['system:errorlog:edit']"
          >忽略</el-button>
          <el-button 
            link 
            type="danger" 
            icon="Delete" 
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:errorlog:remove']"
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

    <!-- 查看详情对话框 -->
    <el-dialog title="错误日志详情" v-model="viewOpen" width="1000px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="错误编号">{{ viewForm.errorId }}</el-descriptions-item>
        <el-descriptions-item label="错误编码">{{ viewForm.errorCode }}</el-descriptions-item>
        <el-descriptions-item label="错误类型">
          <el-tag v-if="viewForm.errorType === '系统错误'" type="danger">系统错误</el-tag>
          <el-tag v-else-if="viewForm.errorType === '业务错误'" type="warning">业务错误</el-tag>
          <el-tag v-else-if="viewForm.errorType === 'SQL错误'" type="danger">SQL错误</el-tag>
          <el-tag v-else-if="viewForm.errorType === '网络错误'" type="info">网络错误</el-tag>
          <el-tag v-else>{{ viewForm.errorType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="错误级别">
          <el-tag v-if="viewForm.errorLevel === 'ERROR'" type="danger">错误</el-tag>
          <el-tag v-else-if="viewForm.errorLevel === 'WARN'" type="warning">警告</el-tag>
          <el-tag v-else-if="viewForm.errorLevel === 'FATAL'" type="danger">致命</el-tag>
          <el-tag v-else>{{ viewForm.errorLevel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="错误消息" :span="2">{{ viewForm.errorMessage }}</el-descriptions-item>
        <el-descriptions-item label="异常类名" :span="2">{{ viewForm.exceptionClass }}</el-descriptions-item>
        <el-descriptions-item label="异常方法" :span="2">{{ viewForm.exceptionMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ viewForm.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ viewForm.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作用户">{{ viewForm.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ viewForm.operIp }}</el-descriptions-item>
        <el-descriptions-item label="浏览器">{{ viewForm.browser }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ viewForm.os }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag v-if="viewForm.status === '0'" type="danger">未处理</el-tag>
          <el-tag v-else-if="viewForm.status === '1'" type="success">已处理</el-tag>
          <el-tag v-else-if="viewForm.status === '2'" type="info">已忽略</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="错误时间" :span="2">
          {{ parseTime(viewForm.errorTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item label="处理人" v-if="viewForm.handleBy">{{ viewForm.handleBy }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="viewForm.handleTime">
          {{ parseTime(viewForm.handleTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2" v-if="viewForm.handleRemark">{{ viewForm.handleRemark }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <el-input v-model="viewForm.requestParam" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="堆栈信息" :span="2">
          <el-input v-model="viewForm.stackTrace" type="textarea" :rows="10" readonly />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog title="处理错误日志" v-model="handleOpen" width="1000px" append-to-body>
      <el-form ref="handleRef" :model="handleForm" :rules="handleRules" label-width="100px">
        <el-form-item label="错误消息">
          <div class="error-message">{{ handleForm.errorMessage }}</div>
        </el-form-item>
        <el-form-item label="处理备注" prop="handleRemark">
          <el-input 
            v-model="handleForm.handleRemark" 
            type="textarea" 
            :rows="4"
            placeholder="请输入处理备注" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitHandle">确 定</el-button>
          <el-button @click="handleOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ErrorLog">
import { listErrorLog, getErrorLog, delErrorLog, cleanErrorLog, handleErrorLog, ignoreErrorLog, exportErrorLog } from "@/api/system/errorlog"

const { proxy } = getCurrentInstance()

const errorLogList = ref([])
const viewOpen = ref(false)
const handleOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const errorTimeRange = ref([])

const data = reactive({
  viewForm: {},
  handleForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    operName: undefined,
    errorTypeList: [],
    operIp: undefined,
    statusList: [],
    browser: undefined
  },
  handleRules: {
    handleRemark: [{ required: true, message: "处理备注不能为空", trigger: "blur" }]
  }
})

const { queryParams, viewForm, handleForm, handleRules } = toRefs(data)

/** 查询错误日志列表 */
function getList() {
  loading.value = true
  let params = { ...queryParams.value }
  
  // 处理操作时间范围
  if (errorTimeRange.value && errorTimeRange.value.length === 2) {
    params.params = params.params || {}
    params.params.beginTime = errorTimeRange.value[0]
    params.params.endTime = errorTimeRange.value[1]
  }
  
  // 处理类型多选
  if (params.errorTypeList && params.errorTypeList.length > 0) {
    params.errorTypeList = params.errorTypeList.join(',')
  } else {
    delete params.errorTypeList
  }
  
  // 处理状态多选
  if (params.statusList && params.statusList.length > 0) {
    params.statusList = params.statusList.join(',')
  } else {
    delete params.statusList
  }
  
  listErrorLog(params).then(response => {
    errorLogList.value = response.rows
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
  errorTimeRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.errorId)
  multiple.value = !selection.length
}

/** 查看详情 */
function handleView(row) {
  const errorId = row.errorId
  getErrorLog(errorId).then(response => {
    viewForm.value = response.data
    viewOpen.value = true
  })
}

/** 处理按钮操作 */
function handleHandle(row) {
  handleForm.value = {
    errorId: row.errorId,
    errorMessage: row.errorMessage,
    handleRemark: undefined
  }
  handleOpen.value = true
}

/** 提交处理 */
function submitHandle() {
  proxy.$refs["handleRef"].validate(valid => {
    if (valid) {
      handleErrorLog(handleForm.value).then(response => {
        proxy.$modal.msgSuccess("处理成功")
        handleOpen.value = false
        getList()
      })
    }
  })
}

/** 忽略按钮操作 */
function handleIgnore(row) {
  proxy.$modal.confirm('是否确认忽略该错误日志？').then(function() {
    return ignoreErrorLog({ errorId: row.errorId })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("操作成功")
  }).catch(() => {})
}

/** 删除按钮操作 */
function handleDelete(row) {
  const errorIds = row.errorId || ids.value
  proxy.$modal.confirm('是否确认删除该数据项？').then(function() {
    return delErrorLog(errorIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 清空按钮操作 */
function handleClean() {
  proxy.$modal.confirm('是否确认清空所有错误日志数据项？').then(function() {
    return cleanErrorLog()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("清空成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/errorlog/export', {
    ...queryParams.value
  }, `errorlog_${new Date().getTime()}.xlsx`)
}

getList()
</script>
<style scoped lang="scss">
.error-message {
  word-break: break-all;
  line-height: 1.5;
}
</style>
