<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true" label-width="95px">
      <SelectCompetitionBack ref="querySelectCompetitionRef" v-model="queryParams" label-width="95px">
        <template #suffix>
          <el-form-item label="参赛单位" prop="schoolName">
            <el-input v-model="queryParams.schoolName" placeholder="请输入参赛单位" clearable @keyup.enter="handleQuery"
              style="width: 180px;" />
          </el-form-item>
          <el-form-item label="参赛者姓名" prop="userName">
            <el-input v-model="queryParams.userName" placeholder="请输入参赛选手" clearable @keyup.enter="handleQuery"
              style="width: 180px;" />
          </el-form-item>
          <el-form-item label="参赛选手" prop="player">
            <el-input v-model="queryParams.player" placeholder="请输入参赛选手" clearable @keyup.enter="handleQuery"
              style="width: 180px;" />
          </el-form-item>
          <el-form-item label="指导教师" prop="guideTeacher">
            <el-input v-model="queryParams.guideTeacher" placeholder="请输入指导教师" clearable @keyup.enter="handleQuery"
              style="width: 180px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </template>
      </SelectCompetitionBack>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain @click="handleExport('all')" icon="Download"
          v-hasPermi="['competition:userCertificate:export']">全部导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain @click="handleExport('filter')" icon="Finished"
          v-hasPermi="['competition:userCertificate:export']">检索结果导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table style="border: 1px solid #ebeef5;" v-loading="loading" :data="tableData" stripe>
      <el-table-column type="index" label="序号" width="50" />
      <el-table-column prop="schoolName" label="参赛单位" min-width="130" show-overflow-tooltip >
        <template #default="{ row }">
          {{row.schoolName ?? '-'}}
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="参赛者姓名" min-width="140" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{row.userName ?? '-'}}
        </template>
      </el-table-column>
      <el-table-column prop="competitionName" label="赛事" min-width="140" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{row.competitionName ?? '-'}}
        </template>
      </el-table-column>
      <el-table-column label="赛道/组别" align="left" prop="commodityName" min-width="150" >
        <template #default="scope">
          <span style="color: #ff8800">{{
            scope.row.competitionTrackName
          }}</span>
          <span>-</span>
          <span style="color: #51c512">{{ scope.row.secondLevelName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="competitionStageName" label="阶段" min-width="100" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{row.competitionStageName ?? '-'}}
        </template>
      </el-table-column>
      <el-table-column prop="awardsName" label="奖项" min-width="80" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{awards_name.find(dict => dict.value === row.awardsName)?.label ?? row.awardsName ?? '-'}}
        </template>
      </el-table-column>
      <el-table-column prop="issuanceDate" label="颁发日期" min-width="100" show-overflow-tooltip align="center" >
        <template #default="{ row }">
          {{parseTime(row.issuanceDate, '{y}-{m}-{d}')}}
        </template>
      </el-table-column>
      <el-table-column prop="player" label="参赛选手" min-width="140" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{row.player ?? '-'}}
        </template>
      </el-table-column>
      <el-table-column prop="guideTeacher" label="指导教师" min-width="120" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{row.guideTeacher ?? '-'}}
        </template>
      </el-table-column>
      <el-table-column prop="certName" label="证书名称" min-width="140" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{row.certName ?? '-'}}
        </template>
      </el-table-column>
      <el-table-column prop="certCode" label="证书编号" min-width="140" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{row.certCode ?? '-'}}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="success" @click="handleEdit(row)" icon="Edit">编辑</el-button>
          <el-button size="small" link type="danger" @click="handleDelete(row)" icon="Delete">删除</el-button>
        </template>
      </el-table-column>
    </el-table>


    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="证书编辑" v-model="open" width="480px" :destroy-on-close="!disabled" :show-close="disabled">
      <el-form :model="form" ref="formRef" label-width="95px">
        <el-form-item label="证书名称" prop="certName">
          <el-input v-model="form.certName" placeholder="请输入证书名称" />
        </el-form-item>
        <el-form-item label="奖项" prop="awardsName">
          <el-select v-model="form.awardsName" placeholder="请选择" clearable>
            <el-option v-for="dict in awards_name" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="证书编号" prop="certCode">
          <el-input v-model="form.certCode" placeholder="请输入证书编号" />
        </el-form-item>
        <el-form-item label="颁发日期" prop="issuanceDate">
          <el-date-picker v-model="form.issuanceDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择颁发日期" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="open = false" :disabled="disabled">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :disabled="disabled" :loading="disabled">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
  // ****** 插件 ******
  const { proxy } = getCurrentInstance()
  import { parseTime } from "@/utils/ruoyi"
  // ******* 组件 ********
  import SelectCompetitionBack from "@/views/certInterconnect/components/SelectCompetitionBack.vue"

  // ****** API ******
  import { getUserCertList, getUserCertInfo, updateUserCertInfo, exportUserCert, delUserCert } from "@/api/certInterconnect/userCert.js"

  // ****** 工具方法 ****** 
  import { handleAsyncExport } from "@/utils/export";

  // ******* 字典 ********
  const {
    awards_name
  } = proxy.useDict(
    "awards_name", // 奖项
  );

  // ******* 初始化 ********
  // 搜索条件
  const queryParams = ref({
    pageNum: 1,
    pageSize: 10,
    name: '',
    competitionSeriesName: '',
    secondLevelTrack: '',
    thirdLevelTrack: '',
    player: '',
    userName: '',
    guideTeacher: ''
  })
  const tableData = ref([]) // 列表数据源
  const total = ref(0) // 总条数
  const loading = ref(0) // 列表加载状态
  const showSearch = ref(true) // 是否显示搜索条件
  const open = ref(false) // 弹窗是否打开
  const querySelectCompetitionRef = ref() // 搜索条件赛事选择组件
  const formRef = ref() // 表单 ref
  const form = reactive({}) // 表单
  const disabled = ref(false) // 表单提交中


  const getList = async () => {
    try {
      loading.value = true;
      const params = JSON.parse(JSON.stringify(queryParams.value))
      delete params.competitionSeriesName
      delete params.competitionName
      delete params.competitionTrackName
      delete params.secondLevelName
      delete params.competitionStageName
      const { rows, code, total: count } = await getUserCertList(params)
      if (code === 200) {
        loading.value = false;
        tableData.value = rows;
        total.value = count
      }
    } catch (error) {
      proxy.$modal.msgWarning(error?.message ?? '操作失败');
    }
  }

  const handleQuery = () => {
    queryParams.value.pageNum = 1
    getList()
  }

  const resetQuery = () => {
    proxy.resetForm("queryRef");
    if (querySelectCompetitionRef.value) {
      querySelectCompetitionRef.value.reset();
    }
    handleQuery();
    handleQuery()
  }



  const handleExport = (type) => {
    handleAsyncExport(exportUserCert, {
      ...queryParams.value,
      exportType: type,
    });
  }

  const handleEdit = async (row) => {
    const { data, code } = await getUserCertInfo({
      certId: row.certId || '',
      competitionSeriesId: row.competitionSeriesId || '',
      userId: row.userId || ''
    })
    if (code !== 200) return
    Object.keys(form).forEach(key => delete form[key])
    Object.assign(form, data)
    form.certCode = form.certCode || ''
    form.userId = form.userId || ''
    form.certId = form.certId || ''
    open.value = true;
  }

  const handleSubmit = async () => {
    if (formRef.value) {
      await formRef.value.validate(async (valid, fields) => {
        if (valid) {
          open.value = false;
          if (form.certId) {
            await updateUserCertInfo(form);
            proxy.$message.success('用户证书信息修改成功')
            getList()
          }
        }
      })
    }
  }

  // 删除
  const handleDelete = (row) => {
    proxy.$confirm('是否确认删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const { code } = await delUserCert({
        certCode: row.certCode || '',
        userId: row.userId || '',
        certId: row.certId || '',
        competitionSeriesId: row.competitionSeriesId || ''
      })
      if (code === 200) {
        proxy.$modal.msgSuccess('删除成功')
        getList()
      }
    }).catch(() => {})
  }

  onMounted(() => {
    handleQuery()
  })
</script>

<style scoped></style>
