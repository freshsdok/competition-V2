<template>
  <div class="app-container">
    <!-- 查询条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="90px">
      <el-form-item label="参赛者姓名" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入参赛者姓名" clearable style="width: 180px;" />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入联系电话" clearable style="width: 180px;" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="queryParams.email" placeholder="请输入邮箱" clearable style="width: 180px;" />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="queryParams.idCard" placeholder="请输入身份证号" clearable style="width: 180px;" />
      </el-form-item>
      <el-form-item label="学校" prop="schoolName">
        <el-input v-model="queryParams.schoolName" placeholder="请输入学校" clearable style="width: 180px;" />
      </el-form-item>
      <el-form-item label="团队编号" prop="teamCode">
        <el-input v-model="queryParams.teamCode" placeholder="请输入团队编号" clearable style="width: 180px;" />
      </el-form-item>
      <el-form-item label="团队名称" prop="teamName">
        <el-input v-model="queryParams.teamName" placeholder="请输入团队名称" clearable style="width: 180px;" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10">
      <el-col :span="1.5">
        <el-button type="primary" plain @click="handleExport" icon="Download"
          v-hasPermi="['competition:candidateCertInfo:export']" v-loading="loadingAll">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain @click="handlePullAwardData" icon="Refresh"
          v-hasPermi="['competition:candidateCertInfo:pull']">同步获奖数据</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain @click="handleBack" icon="Back">返回</el-button>
      </el-col>
      <right-toolbar :search="false" @queryTable="getList"></right-toolbar>
    </el-row>
    <div class="table-content">
      <el-table ref="tableRef" 
                style="border: 1px solid #ebeef5;margin-top: 10px;" 
                :data="tableData" 
                height="calc(100vh - 310px)"
                stripe v-loading="loadingTable">
        <el-table-column prop="userName" label="参赛者姓名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="sourceType" label="来源类型" min-width="80" show-overflow-tooltip>
          <template #default="{ row }">
            {{source_type.find(item => item.value === row.sourceType)?.label ?? '-'}}
          </template>
        </el-table-column>
        <el-table-column prop="sourceData" label="数据来源" min-width="100" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" min-width="120" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="120" show-overflow-tooltip />
        <el-table-column prop="schoolName" label="学校" min-width="120" show-overflow-tooltip />
        <el-table-column prop="profession" label="专业" min-width="120" show-overflow-tooltip />
        <el-table-column prop="idCard" label="身份证号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="teamName" label="团队名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="teamCode" label="团队编号" min-width="220" show-overflow-tooltip />
        <el-table-column prop="leaderTeacherName" label="带队老师" min-width="100" show-overflow-tooltip />
        <el-table-column prop="competitionRoleName" label="角色" min-width="100" show-overflow-tooltip />
        <el-table-column prop="score" label="成绩" min-width="80" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.score ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="80" fixed="right"
          v-hasPermi="['competition:candidateCertInfo:remove']">
          <template #default="scope">
            <el-button size="small" link type="danger" @click="handleDelete(scope.row)" icon="Delete">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="footer-content">
      <el-form :inline="true" class="issue-form" :model="issueForm" :rules="issueRules" ref="issueFormRef">
        <el-form-item label="颁发时间" prop="issuanceDate">
          <el-date-picker v-model="issueForm.issuanceDate" type="date" placeholder="请选择颁发时间" value-format="YYYY-MM-DD"
            :disabled="disabled" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :disabled="disabled && tableData.length === 0"
            v-hasPermi="['competition:userCertificate:add']" :loading="disabled">确认信息无误，生成证书</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
  // ******** 插件 ********
  const { proxy } = getCurrentInstance();
  const route = useRoute();
  import modal from "@/plugins/modal";
  const router = useRouter()
  // ******** API *********
  import { getCandidateCertList, updateCandidateCertList, exportCandidateCertList, pullAwardData } from "@/api/certInterconnect/certConfig.js"
  import { issueUserCerts } from "@/api/certInterconnect/userCert.js"

  // ******** 工具 *********
  // 导出
  import { handleAsyncExportWithLoading } from "@/utils/export";

  // ******* 字典 ********
  const {
    source_type,
  } = proxy.useDict(
    "source_type"
  );

  // ******** 初始化 ********
  const certConfigId = computed(() => route?.params?.certConfigId)
  const tableData = ref([]) // 列表数据
  const disabled = ref(false) // 是否正在生成证书
  const issueFormRef = ref(null)
  const issueForm = ref({
    issuanceDate: ''
  })
  const issueRules = {
    issuanceDate: [{ required: true, message: '请选择颁发时间', trigger: 'change' }]
  }

  // 查询参数
  const queryParams = ref({
    userName: '',
    phone: '',
    email: '',
    idCard: '',
    schoolName: '',
    teamCode: '',
    teamName: ''
  })

  // ******** 业务 ********
  // 获取列表
  const loadingTable = ref(false)
  const getList = async () => {
    loadingTable.value = true
    const params = {
      certConfigId: certConfigId.value,
      ...queryParams.value
    }
    const { data, code } = await getCandidateCertList(params);
    if (code === 200) {
      tableData.value = data;
    }
    loadingTable.value = false
  }

  // 搜索
  const handleQuery = () => {
    getList()
  }

  // 重置查询
  const resetQuery = () => {
    queryParams.value = {
      userName: '',
      phone: '',
      email: '',
      idCard: '',
      schoolName: '',
      teamCode: '',
      teamName: ''
    }
    getList()
  }

  // 拉取获奖数据
  const handlePullAwardData = async () => {
    proxy.$confirm("是否确认拉取获奖数据？此操作将拉取相同赛事下的获奖公示数据到当前列表。", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }).then(async () => {
      try {
        const { code, msg } = await pullAwardData({
          certConfigId: certConfigId.value
        })
        if (code === 200) {
          proxy.$modal.msgSuccess(msg || '拉取获奖数据成功');
          getList()
        }
      } catch (error) {
        console.log(error);
      }
    }).catch()
  }

  // 删除
  const handleDelete = async (row) => {
    if (!row) return;
    proxy.$confirm("是否确认删除该候选人？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }).then(async () => {
      const { candidateId, userId, userName, memberId, phone, email, idCard, competitionSeriesId, competitionTrackId, secondLevelCode, teamCode, competitionRoleName, leaderTeacherId } = row || {};
      const params = {
        certConfigId: certConfigId.value,
        candidateId,
        userName,
        memberId,
        phone,
        email,
        idCard,
        competitionSeriesId,
        competitionTrackId,
        secondLevelCode,
        teamCode,
        competitionRoleName,
        leaderTeacherId,
        userId,
        delFlag: '1'
      }
      const { code } = await updateCandidateCertList(params)
      if (code === 200) {
        proxy.$modal.msgSuccess(`候选人删除成功`);
        getList()
      }
    }).catch()


  }
  // 处理提交逻辑，例如生成证书
  const handleSubmit = async () => {
    const valid = await issueFormRef.value?.validate().catch(() => false)
    if (!valid) return

    try {
      disabled.value = true
      const params = {
        userCertificateList: tableData.value,
        issuanceDate: issueForm.value.issuanceDate
      }
      const { msg } = await issueUserCerts(params)
      proxy.$modal.msgSuccess(msg || '证书颁发成功')
      issueForm.value.issuanceDate = ''
      getList()
    } catch (error) {
      console.log(error)
    } finally {
      disabled.value = false
    }

  }
  // 导出选定候选人列表
  let loadingAll = $ref(false)
  function handleExport() {
    handleAsyncExportWithLoading(
      exportCandidateCertList,
      { certConfigId: certConfigId.value },
      loadingAll
    );
  }
  // 返回
  const handleBack = () => {
    router.back()
  }

  onMounted(() => getList())
</script>

<style lang="scss" scoped>
.footer-content{
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
}
</style>
