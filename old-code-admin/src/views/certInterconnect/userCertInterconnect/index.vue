<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true" label-width="95px" class="mb8">
      <el-row :gutter="10">
        <el-col :span="6">
          <el-form-item label="参赛单位" prop="schoolName">
            <el-input v-model="queryParams.schoolName" placeholder="请输入参赛单位" clearable @keyup.enter="handleQuery"
              style="width: 180px" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="参赛者姓名" prop="userName">
            <el-input v-model="queryParams.userName" placeholder="请输入参赛选手" clearable @keyup.enter="handleQuery"
              style="width: 180px" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="指导教师" prop="guideTeacherName">
            <el-input v-model="queryParams.guideTeacherName" placeholder="请输入指导教师" clearable
              @keyup.enter="handleQuery" style="width: 180px" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="订单状态" prop="payStatus">
            <el-select v-model="queryParams.payStatus" placeholder="全部" clearable style="width: 180px;">
              <el-option label="全部" value="" />
              <el-option v-for="dict in pay_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain @click="handleExport('all')" icon="Download"
          v-hasPermi="['competition:competitionCertExchangeApply:export']">全部导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain @click="handleExport('filter')" icon="Finished"
          v-hasPermi="['competition:competitionCertExchangeApply:export']">检索结果导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="tableData" stripe style="border: 1px solid #ebeef5;">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="schoolName" label="参赛单位" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userName" label="参赛者姓名" min-width="140" show-overflow-tooltip />
      <el-table-column prop="guideTeacherName" label="指导教师" min-width="120" show-overflow-tooltip />
      <el-table-column prop="idCard" label="身份证号" min-width="160" show-overflow-tooltip />
      <el-table-column prop="originCertName" label="源证书名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="targetCertName" label="互换证书名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="applyTime" label="申请时间" min-width="180" />
      <el-table-column prop="payStatus" label="订单状态" min-width="100">
        <template #default="{ row }">
          <dict-tag :options="pay_status" :value="row.payStatus" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup>
  // ******** 插件 ************
  const { proxy } = getCurrentInstance()

  // 组件
  import SelectCompetitionBack from "@/views/certInterconnect/components/SelectCompetitionBack.vue"


  // ********* API *********** 
  import { getUserCertInterconnectList, exportUserCertInterconnect } from "@/api/certInterconnect/userCertInterconnect.js"

  // ****** 工具方法 ****** 
  import { handleAsyncExport } from "@/utils/export";


  // ******* 字典 ********
  const {
    pay_status,
  } = proxy.useDict(
    "pay_status",
  );

  // ******* 初始化 ********
  const queryParams = ref({
    pageNum: 1,
    pageSize: 10,
    userName: '',
    guideTeacherName: '',
    schoolName: '',
    payStatus: ''
  })
  const tableData = ref([])
  const total = ref([])
  const loading = ref(false)
  const showSearch = ref(true)



  const getList = async () => {
    loading.value = true
    const params = JSON.parse(JSON.stringify(queryParams.value))
    delete params.competitionSeriesName
    delete params.competitionName
    delete params.competitionTrackName
    delete params.secondLevelName
    delete params.competitionStageName
    const { rows, total: count } = await getUserCertInterconnectList(params)
    tableData.value = rows;
    total.value = count;
    loading.value = false;
  }
  // 重置
  const handleQuery = () => {
    queryParams.value.pageNum = 1
    getList()
  }
  // 搜索
  const resetQuery = () => {
    proxy.resetForm("queryRef");
    handleQuery()
  }

  // 导出
  const handleExport = (type) => {
    handleAsyncExport(exportUserCertInterconnect, {
      ...queryParams.value,
      exportType: type,
    });
  }



  onMounted(() => {
    getList()
  })
</script>

<style scoped></style>
