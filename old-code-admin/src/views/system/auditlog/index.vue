<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="操作用户" prop="userName">
        <el-input
          v-model.trim="queryParams.userName"
          placeholder="请输入操作用户"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审计类型" prop="auditTypeList">
        <el-select v-model="queryParams.auditTypeList" placeholder="请选择审计类型" clearable multiple collapse-tags style="width: 240px">
          <el-option label="登录审计" value="登录审计" />
          <el-option label="权限审计" value="权限审计" />
          <el-option label="数据审计" value="数据审计" />
          <el-option label="配置审计" value="配置审计" />
          <el-option label="操作审计" value="操作审计" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作模块" prop="operationModuleList">
        <el-select v-model="queryParams.operationModuleList" placeholder="请选择操作模块" clearable multiple collapse-tags style="width: 240px">
          <el-option label="用户管理" value="用户管理" />
          <el-option label="角色管理" value="角色管理" />
          <el-option label="菜单管理" value="菜单管理" />
          <el-option label="机构管理" value="机构管理" />
          <el-option label="岗位管理" value="岗位管理" />
          <el-option label="字典管理" value="字典管理" />
          <el-option label="参数设置" value="参数设置" />
          <el-option label="通知公告" value="通知公告" />
          <el-option label="日志管理" value="日志管理" />
        </el-select>
      </el-form-item>
      <el-form-item label="事件名称" prop="eventName">
        <el-input
          v-model.trim="queryParams.eventName"
          placeholder="请输入事件名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="IP地址" prop="ipAddress">
        <el-input
          v-model.trim="queryParams.ipAddress"
          placeholder="请输入IP地址"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="结果" prop="isAbnormalList">
        <el-select v-model="queryParams.isAbnormalList" placeholder="请选择结果" clearable multiple collapse-tags style="width: 200px">
          <el-option label="正常" value="0" />
          <el-option label="异常" value="1" />
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
      <el-form-item label="操作时间" prop="operationTime">
        <el-date-picker
          v-model="operationTimeRange"
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
          v-hasPermi="['system:auditlog:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          @click="handleClean"
          v-hasPermi="['system:auditlog:remove']"
        >清空</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:auditlog:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="auditLogList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="left" type="index" width="50" />
      <el-table-column label="审计编号" align="center" prop="auditId" width="80" />
      <el-table-column label="审计类型" align="center" prop="auditType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.auditType === '登录审计'" type="primary">登录审计</el-tag>
          <el-tag v-else-if="scope.row.auditType === '权限审计'" type="warning">权限审计</el-tag>
          <el-tag v-else-if="scope.row.auditType === '数据审计'" type="success">数据审计</el-tag>
          <el-tag v-else-if="scope.row.auditType === '配置审计'" type="info">配置审计</el-tag>
          <el-tag v-else-if="scope.row.auditType === '操作审计'">操作审计</el-tag>
          <el-tag v-else>{{ scope.row.auditType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="风险级别" align="center" prop="riskLevel" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.riskLevel === 'LOW'" type="success">低</el-tag>
          <el-tag v-else-if="scope.row.riskLevel === 'MEDIUM'" type="warning">中</el-tag>
          <el-tag v-else-if="scope.row.riskLevel === 'HIGH'" type="danger">高</el-tag>
          <el-tag v-else-if="scope.row.riskLevel === 'CRITICAL'" type="danger">严重</el-tag>
          <el-tag v-else>{{ scope.row.riskLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="事件名称" align="center" prop="eventName" :show-overflow-tooltip="true" min-width="150" />
      <el-table-column label="操作用户" align="center" prop="userName" width="100" />
      <el-table-column label="操作模块" align="center" prop="operationModule" width="120" />
      <el-table-column label="IP地址" align="center" prop="ipAddress" width="130" />
      <el-table-column label="浏览器" align="center" prop="browser" width="100" />
      <el-table-column label="是否异常" align="center" prop="isAbnormal" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.isAbnormal === '0'" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.isAbnormal === '1'" type="danger">异常</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审计状态" align="center" prop="auditStatus" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.auditStatus === '0'" type="warning">待审计</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === '1'" type="success">已审计</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === '2'" type="info">已忽略</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作时间" align="center" prop="operationTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.operationTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200" fixed="right">
        <template #default="scope">
          <el-button 
            link 
            type="primary" 
            icon="View" 
            @click="handleView(scope.row)"
            v-hasPermi="['system:auditlog:query']"
          >详情</el-button>
          <el-button 
            v-if="scope.row.auditStatus === '0'"
            link 
            type="success" 
            icon="Check" 
            @click="handleAudit(scope.row)"
            v-hasPermi="['system:auditlog:edit']"
          >审计</el-button>
          <el-button 
            v-if="scope.row.auditStatus === '0'"
            link 
            type="warning" 
            icon="Close" 
            @click="handleIgnore(scope.row)"
            v-hasPermi="['system:auditlog:edit']"
          >忽略</el-button>
          <el-button 
            link 
            type="danger" 
            icon="Delete" 
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:auditlog:remove']"
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
    <el-dialog title="审计日志详情" v-model="viewOpen" width="1000px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="审计编号">{{ viewForm.auditId }}</el-descriptions-item>
        <el-descriptions-item label="审计类型">
          <el-tag v-if="viewForm.auditType === '登录审计'" type="primary">登录审计</el-tag>
          <el-tag v-else-if="viewForm.auditType === '权限审计'" type="warning">权限审计</el-tag>
          <el-tag v-else-if="viewForm.auditType === '数据审计'" type="success">数据审计</el-tag>
          <el-tag v-else-if="viewForm.auditType === '配置审计'" type="info">配置审计</el-tag>
          <el-tag v-else-if="viewForm.auditType === '操作审计'">操作审计</el-tag>
          <el-tag v-else>{{ viewForm.auditType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审计分类">{{ viewForm.auditCategory }}</el-descriptions-item>
        <el-descriptions-item label="风险级别">
          <el-tag v-if="viewForm.riskLevel === 'LOW'" type="success">低</el-tag>
          <el-tag v-else-if="viewForm.riskLevel === 'MEDIUM'" type="warning">中</el-tag>
          <el-tag v-else-if="viewForm.riskLevel === 'HIGH'" type="danger">高</el-tag>
          <el-tag v-else-if="viewForm.riskLevel === 'CRITICAL'" type="danger">严重</el-tag>
          <el-tag v-else>{{ viewForm.riskLevel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="事件名称" :span="2">{{ viewForm.eventName }}</el-descriptions-item>
        <el-descriptions-item label="事件描述" :span="2">{{ viewForm.eventDesc }}</el-descriptions-item>
        <el-descriptions-item label="操作用户">{{ viewForm.userName }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">{{ viewForm.userType }}</el-descriptions-item>
        <el-descriptions-item label="机构名称">{{ viewForm.deptName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ viewForm.operationType }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ viewForm.operationModule }}</el-descriptions-item>
        <el-descriptions-item label="操作方法">{{ viewForm.operationMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ viewForm.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ viewForm.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ viewForm.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="IP归属地">{{ viewForm.ipLocation }}</el-descriptions-item>
        <el-descriptions-item label="浏览器">{{ viewForm.browser }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ viewForm.os }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{ viewForm.deviceType }}</el-descriptions-item>
        <el-descriptions-item label="是否异常">
          <el-tag v-if="viewForm.isAbnormal === '0'" type="success">正常</el-tag>
          <el-tag v-else-if="viewForm.isAbnormal === '1'" type="danger">异常</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="异常原因" :span="2" v-if="viewForm.abnormalReason">{{ viewForm.abnormalReason }}</el-descriptions-item>
        <el-descriptions-item label="审计状态">
          <el-tag v-if="viewForm.auditStatus === '0'" type="warning">待审计</el-tag>
          <el-tag v-else-if="viewForm.auditStatus === '1'" type="success">已审计</el-tag>
          <el-tag v-else-if="viewForm.auditStatus === '2'" type="info">已忽略</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">
          {{ parseTime(viewForm.operationTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ viewForm.costTime }} 毫秒</el-descriptions-item>
        <el-descriptions-item label="审计人" v-if="viewForm.auditBy">{{ viewForm.auditBy }}</el-descriptions-item>
        <el-descriptions-item label="审计时间" v-if="viewForm.auditTime">
          {{ parseTime(viewForm.auditTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item label="审计备注" :span="2" v-if="viewForm.auditRemark">{{ viewForm.auditRemark }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <el-input v-model="viewForm.requestParam" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2">
          <el-input v-model="viewForm.responseResult" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="变更前数据" :span="2" v-if="viewForm.oldValue">
          <el-input v-model="viewForm.oldValue" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="变更后数据" :span="2" v-if="viewForm.newValue">
          <el-input v-model="viewForm.newValue" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审计对话框 -->
    <el-dialog title="审计日志" v-model="auditOpen" width="600px" append-to-body>
      <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
        <el-form-item label="事件名称">
          <div style="color: #606266; line-height: 1.5;">{{ auditForm.eventName }}</div>
        </el-form-item>
        <el-form-item label="审计备注" prop="auditRemark">
          <el-input 
            v-model="auditForm.auditRemark" 
            type="textarea" 
            :rows="4"
            placeholder="请输入审计备注" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAudit">确 定</el-button>
          <el-button @click="auditOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AuditLog">
import { listAuditLog, getAuditLog, delAuditLog, cleanAuditLog, auditAuditLog, ignoreAuditLog } from "@/api/system/auditlog"

const { proxy } = getCurrentInstance()

const auditLogList = ref([])
const viewOpen = ref(false)
const auditOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const operationTimeRange = ref([])

const data = reactive({
  viewForm: {},
  auditForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    auditTypeList: [],
    operationModuleList: [],
    eventName: undefined,
    ipAddress: undefined,
    isAbnormalList: [],
    browser: undefined
  },
  auditRules: {
    auditRemark: [{ required: true, message: "审计备注不能为空", trigger: "blur" }]
  }
})

const { queryParams, viewForm, auditForm, auditRules } = toRefs(data)

/** 查询审计日志列表 */
function getList() {
  loading.value = true
  let params = { ...queryParams.value }
  
  // 处理操作时间范围
  if (operationTimeRange.value && operationTimeRange.value.length === 2) {
    params.params = params.params || {}
    params.params.beginTime = operationTimeRange.value[0]
    params.params.endTime = operationTimeRange.value[1]
  }
  
  // 处理审计类型多选
  if (params.auditTypeList && params.auditTypeList.length > 0) {
    params.auditTypeList = params.auditTypeList.join(',')
  } else {
    delete params.auditTypeList
  }
  
  // 处理模块多选
  if (params.operationModuleList && params.operationModuleList.length > 0) {
    params.operationModuleList = params.operationModuleList.join(',')
  } else {
    delete params.operationModuleList
  }
  
  // 处理结果多选
  if (params.isAbnormalList && params.isAbnormalList.length > 0) {
    params.isAbnormalList = params.isAbnormalList.join(',')
  } else {
    delete params.isAbnormalList
  }
  
  listAuditLog(params).then(response => {
    auditLogList.value = response.rows
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
  operationTimeRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.auditId)
  multiple.value = !selection.length
}

/** 查看详情 */
function handleView(row) {
  const auditId = row.auditId
  getAuditLog(auditId).then(response => {
    viewForm.value = response.data
    viewOpen.value = true
  })
}

/** 审计按钮操作 */
function handleAudit(row) {
  auditForm.value = {
    auditId: row.auditId,
    eventName: row.eventName,
    auditRemark: undefined
  }
  auditOpen.value = true
}

/** 提交审计 */
function submitAudit() {
  proxy.$refs["auditRef"].validate(valid => {
    if (valid) {
      auditAuditLog(auditForm.value).then(response => {
        proxy.$modal.msgSuccess("审计成功")
        auditOpen.value = false
        getList()
      })
    }
  })
}

/** 忽略按钮操作 */
function handleIgnore(row) {
  proxy.$modal.confirm('是否确认忽略该审计日志？').then(function() {
    return ignoreAuditLog({ auditId: row.auditId })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("操作成功")
  }).catch(() => {})
}

/** 删除按钮操作 */
function handleDelete(row) {
  const auditIds = row.auditId || ids.value
  proxy.$modal.confirm('是否确认删除该数据项？').then(function() {
    return delAuditLog(auditIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 清空按钮操作 */
function handleClean() {
  proxy.$modal.confirm('是否确认清空所有审计日志数据项？').then(function() {
    return cleanAuditLog()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("清空成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/auditlog/export', {
    ...queryParams.value
  }, `auditlog_${new Date().getTime()}.xlsx`)
}

getList()
</script>
