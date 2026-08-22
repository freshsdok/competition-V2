<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="签到手机号" prop="phoneNumber">
        <el-input
          v-model.trim="queryParams.phoneNumber"
          placeholder="请输入签到手机号"
          clearable
          style="width: 180px;"
        />
      </el-form-item>
      <el-form-item label="签到姓名" prop="realName">
        <el-input
          v-model.trim="queryParams.realName"
          placeholder="请输入签到姓名"
          clearable
          style="width: 180px;"
        />
      </el-form-item>
      <el-form-item label="签到学校" prop="schoolName">
        <el-input
          v-model.trim="queryParams.schoolName"
          placeholder="请输入签到学校"
          clearable
          style="width: 180px;"
        />
      </el-form-item>
      <el-form-item label="签到IP地址" prop="ip">
        <el-input
          v-model.trim="queryParams.ip"
          placeholder="请输入签到IP地址"
          clearable
          style="width: 180px;"
        />
      </el-form-item>
      <el-form-item label="签到状态" prop="resultType">
        <el-select v-model="queryParams.resultType" placeholder="请选择签到状态" clearable style="width: 180px;">
          <el-option label="签到成功" value="1" />
          <el-option label="无需签到" value="2" />
          <el-option label="接口异常" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="签到类型" prop="checkInType">
        <el-select v-model="queryParams.checkInType" placeholder="请选择签到类型" clearable style="width: 180px;">
          <el-option label="正常扫码签到" value="正常扫码签到" />
          <el-option label="信息查询签到" value="信息查询签到" />
        </el-select>
      </el-form-item>
      <el-form-item label="签到二维码名称" prop="codeConfigName" label-width="120px">
        <el-input
          v-model.trim="queryParams.codeConfigName"
          placeholder="请输入签到二维码名称"
          clearable
          style="width: 180px;"
        />
      </el-form-item>
      <el-form-item label="签到时间范围" style="width: 480px">
        <el-date-picker
           v-model="signTimeRange"
           value-format="YYYY-MM-DD HH:mm:ss"
           type="datetimerange"
           range-separator="至"
           start-placeholder="开始时间"
           end-placeholder="结束时间"
           :default-time="[new Date('1970-01-01 00:00:00'), new Date('1970-01-01 23:59:59')]"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="签到入库时间范围" style="width: 460px" label-width="130px">
        <el-date-picker
           v-model="createTimeRange"
           value-format="YYYY-MM-DD HH:mm:ss"
           type="datetimerange"
           range-separator="至"
           start-placeholder="开始时间"
           end-placeholder="结束时间"
           :default-time="[new Date('1970-01-01 00:00:00'), new Date('1970-01-01 23:59:59')]"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain @click="handleExport('all')" icon="Download"
          v-hasPermi="['wxApp:wxSignInInfo:export']">全部导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain @click="handleExport('filter')" icon="Finished"
          v-hasPermi="['wxApp:wxSignInInfo:export']">检索结果导出</el-button>
      </el-col>
    </el-row>
    <!-- 列表区域 -->
    <el-table v-loading="loading" :data="signInList" stripe>
      <el-table-column label="签到手机号" align="center" prop="phoneNumber" width="115" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.phoneNumber || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="签到姓名" align="center" prop="nickName" width="80" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row?.realName || scope.row?.nickName || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="签到二维码名称" align="center" prop="codeConfigName" width="115" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.codeConfigName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="签到时间" align="center" prop="signTime" width="158" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.signTime || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="签到学校" align="center" prop="schoolName" min-width="170" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row?.schoolName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="签到状态" align="center" prop="resultType" width="90" show-overflow-tooltip>
        <template #default="scope">
          <el-tag :type="getResultTypeType(scope.row.resultType)" size="small">
            {{ getResultTypeLabel(scope.row.resultType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="签到类型" align="left" prop="checkInType" width="110" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.checkInType || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="签到入库时间" align="center" prop="createTime" width="158" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.createTime || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="签到人" align="center" prop="signName" width="75" show-overflow-tooltip />
      <el-table-column label="签到IP地址" align="center" prop="ip" width="150" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.ip || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="60" fixed="right">
        <template #default="scope">
          <el-tooltip content="查看详情" placement="top">
            <el-button link type="primary" icon="View" @click="handleDetail(scope.row)"></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 详情弹窗 -->
    <el-dialog title="签到详情" v-model="open" width="900px" append-to-body>
      <el-descriptions :column="2" border v-if="form">
        <el-descriptions-item label="签到手机号" :span="1" label-width="130px">{{ form.phoneNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="签到姓名" :span="1" label-width="130px">{{ form.nickName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP地址" :span="1">
          {{ form.ip || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="签到状态" :span="1">
          <el-tag :type="getResultTypeType(form.resultType)" size="small">
            {{ getResultTypeLabel(form.resultType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="签到类型" :span="1">{{ form.checkInType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="签到人" :span="1">{{ form.signName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="签到二维码名称" :span="1" label-width="130px">{{ form.codeConfigName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="签到时间" :span="1">{{ form.signTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="签到入库时间" :span="1" label-width="130px">{{ form.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="签到学校" :span="1">{{ form.schoolName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ form.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WxSignInInfo">
import { getSignInInfoList,exportSignInInfoList } from "@/api/tournament/competition"
import { ElMessage } from 'element-plus'
// ****** 工具方法 ****** 
import { handleAsyncExport } from "@/utils/export";

const queryRef = ref(null)

const signInList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    phoneNumber: null,
    realName: null,
    resultType: null,
    checkInType: null,
    ip: null,
    remark: null,
    codeConfigName: null
  }
})

const { queryParams, form } = toRefs(data)

// 时间范围
const signTimeRange = ref([])
const createTimeRange = ref([])

/** 查询签到信息列表 */
function getList() {
  loading.value = true
  
  // 构建查询参数
  let params = {
    ...queryParams.value,
    signTimeStart: signTimeRange.value && signTimeRange.value[0] ? signTimeRange.value[0] : null,
    signTimeEnd: signTimeRange.value && signTimeRange.value[1] ? signTimeRange.value[1] : null,
    createTimeStart: createTimeRange.value && createTimeRange.value[0] ? createTimeRange.value[0] : null,
    createTimeEnd: createTimeRange.value && createTimeRange.value[1] ? createTimeRange.value[1] : null
  }
  
  getSignInInfoList(params).then(response => {
    signInList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  }).catch(error => {
    loading.value = false
    ElMessage.error('获取数据失败')
  })
}

// 获取结果类型标签
function getResultTypeLabel(type) {
  const map = {
    '1': '签到成功',
    '2': '无需签到',
    '3': '接口异常'
  }
  return map[type] || '未知'
}

// 获取结果类型样式
function getResultTypeType(type) {
  const map = {
    '1': 'success',
    '2': 'warning',
    '3': 'danger'
  }
  return map[type] || 'info'
}

// 组件挂载后执行
onMounted(() => {
  getList()
})

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {}
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
  signTimeRange.value = []
  createTimeRange.value = []
  handleQuery()
}

const handleExport = (type) => {
  handleAsyncExport(exportSignInInfoList, {
    ...queryParams.value,
    exportType: type,
  });
}

/** 详情按钮操作 */
function handleDetail(row) {
  reset()
  form.value = { ...row }
  open.value = true
}
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
}

:deep(.el-descriptions__label) {
  width: 100px;
  font-weight: 600;
  background-color: #f5f7fa;
}

:deep(.el-descriptions__content) {
  min-width: 150px;
}
</style>
