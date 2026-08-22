<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true" label-width="100px" class="mb8">
      <SelectCompetitionBack ref="querySelectCompetitionRef" v-model="queryParams.params" label-width="100px">
        <template #suffix>
          <el-form-item label="参赛者姓名" prop="userName">
            <el-input v-model="queryParams.params.userName" placeholder="请输入参赛者姓名" clearable
              @keyup.enter="handleQuery" style="width: 180px" />
          </el-form-item>
          <el-form-item label="来源类型" prop="sourceType">
             <el-select v-model="queryParams.params.sourceType" placeholder="请选择来源类型" clearable style="width: 180px;">
              <el-option v-for="item in source_type" :key="item" :label="item.label" :value="item.value"/>
             </el-select>
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="queryParams.params.phone" placeholder="请输入联系电话" clearable @keyup.enter="handleQuery"
              style="width: 180px" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="queryParams.params.email" placeholder="请输入邮箱" clearable @keyup.enter="handleQuery"
              style="width: 180px" />
          </el-form-item>
          <el-form-item label="团队名称" prop="teamName">
            <el-input v-model="queryParams.params.teamName" placeholder="请输入团队名称" clearable @keyup.enter="handleQuery"
              style="width: 180px" />
          </el-form-item>
          <el-form-item label="带队老师" prop="leaderTeacherName">
            <el-input v-model="queryParams.params.leaderTeacherName" placeholder="请输入带队老师" clearable
              @keyup.enter="handleQuery" style="width: 180px" />
          </el-form-item>
          <el-form-item label="指导教师" prop="guideTeacherName">
            <el-input v-model="queryParams.params.guideTeacherName" placeholder="请输入指导教师" clearable
              @keyup.enter="handleQuery" style="width: 180px" />
          </el-form-item>
          <el-form-item label="角色" prop="competitionRoleNameReq">
            <el-select v-model="queryParams.params.competitionRoleNameReq" placeholder="请选择角色" clearable style="width: 180px;" multiple>
              <el-option label="队长" value="队长"/>
              <el-option label="队员" value="队员"/>
              <el-option label="指导教师" value="指导教师"/>
            </el-select>
          </el-form-item>
          <el-form-item label="筛选方式">
            <el-radio-group v-model="filtrate" style="width: 180px" >
              <el-radio label="A">按成绩</el-radio>
              <el-radio label="B">按排名</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="筛选条件" :prop="`filterConditions.${filtrate === 'A' ? 'conditions' : 'ranking'}`">
            <el-select placeholder="请选择筛选条件" clearable style="width: 180px"
              v-model="queryParams.params.filterConditions[filtrate === 'A' ? 'conditions' : 'ranking']">
              <el-option v-for="dict in conditions" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="filtrate === 'A' ? '分值' : '排名值'"
            :prop="`filterConditions.${filtrate === 'A' ? 'userScore' : 'topN'}`">
            <el-input-number v-model="queryParams.params.filterConditions[filtrate === 'A' ? 'userScore' : 'topN']"
              :placeholder="filtrate === 'A' ? '请输入分值' : '请输入排名值'" clearable @keyup.enter="handleQuery" style="width: 180px"
              :controls="false"
              v-if="queryParams.params.filterConditions[filtrate === 'A' ? 'conditions' : 'ranking'] !== '4'" />
            <!-- 区间 -->
            <span v-else style="display: flex; align-items: center;">
              <el-input-number
                v-model="queryParams.params.filterConditions[filtrate === 'A' ? 'userScoreStart' : 'topN']"
                :placeholder="filtrate === 'A' ? '开始分值' : '开始排名'" style="width: 85px" :controls="false" />
              <span style="width: 10px;text-align: center;">-</span>
              <el-input-number
                v-model="queryParams.params.filterConditions[filtrate === 'A' ? 'userScoreEnd' : 'lowN']"
                :placeholder="filtrate === 'A' ? '结束分值' : '结束排名'" style="width: 85px" :controls="false" />
            </span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </template>
      </SelectCompetitionBack>
    </el-form>

    <el-row :gutter="10" class="mb8" type="flex">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Upload" @click="open = true"
          v-hasPermi="['competition:candidateCertInfo:import']">数据导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain @click="handleBack" icon="Back">返回</el-button>
      </el-col>
      <el-col style="text-align: right; flex: 1">
        <el-button type="primary" @click="handleView" icon="View">查看选定候选人（{{ selectionData.length
        }}）</el-button>
      </el-col>
    </el-row>

    <div class="table-content">
      <el-table ref="tableRef" style="border: 1px solid #ebeef5;" v-loading="loading" 
        :data="tableData" 
        stripe
        row-key="memberId" @selection-change="handleSelectionChange" reserve-selection>
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="userName" label="参赛者姓名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="sourceType" label="来源类型" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">
            {{source_type.find(item => item.value === row.sourceType)?.label ?? '-'}}
          </template>
        </el-table-column>
        <el-table-column label="数据来源" min-width="260"  show-overflow-tooltip>
          <template #default="scope">
            {{ `${scope.row.competitionName ?? '-'} / ${scope.row.competitionTrackName ?? '-'} / ${scope.row.secondLevelName ?? '-'}` }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" min-width="120" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="idCard" label="身份证号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="schoolName" label="学校" min-width="120" show-overflow-tooltip />
        <el-table-column prop="profession" label="专业" min-width="120" show-overflow-tooltip />
        <el-table-column prop="teamName" label="团队名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="leaderTeacherName" label="带队老师" min-width="120" show-overflow-tooltip />
        <el-table-column prop="competitionRoleName" label="角色" min-width="100" show-overflow-tooltip />
        <el-table-column prop="userScore" label="成绩" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.userScore ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="ranking" label="排名" min-width="80" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.ranking ?? '-' }}
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 分页 -->
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pagination.pageNum"
      v-model:limit="queryParams.pagination.pageSize" :page-sizes="[100, 200, 300, 500]" @pagination="getList" />
    <!-- 已选择候选人 -->
    <!-- <SelectData v-model="selectVisible" :data="selectionData" @delete="handleDelete" /> -->
    <!-- 导入 -->
    <ImportData ref="importRef" v-model="open" title="候选人信息导入" temp-name="候选人信息" @import="handleImport">
      <!-- <el-form-item label="是否导入为候选人">
        <el-radio-group v-model="radioVal">
          <el-radio :value="true">是</el-radio>
          <el-radio :value="false">否</el-radio>
        </el-radio-group>
      </el-form-item> -->
    </ImportData>
  </div>
</template>

<script setup>
  // ******** 组件 ********
  // 已选择人员弹窗列表
  // import SelectData from "./selectData.vue";
  // 赛事选择
  import SelectCompetitionBack from "@/views/certInterconnect/components/SelectCompetitionBack.vue"
  // 导入
  import ImportData from "@/views/certInterconnect/components/ImportData.vue"

  // ******** API ********
  import { getCompetitionApplyList, getCandidateCertList, updateCandidateCertList,batchInsertCandidateCertInfo, importCandidateCertInfo } from "@/api/certInterconnect/certConfig.js"

  // ******** 插件 ********
  const { proxy } = getCurrentInstance();
  const route = useRoute();
  const router = useRouter();

  // ******* 字典 ********
  const {
    conditions,
    source_type,
  } = proxy.useDict(
    "conditions",
    "source_type"
  );

  // ******** 初始化 ********
  const tableData = ref([]); // table数据
  const selectionData = ref([]); // 选中数据
  const selectedIds = ref(new Set()); // 选中id集合
  const currentSelectData = ref([]) // 当前分页选中的数据
  const tableRef = ref(); // table ref
  const selectVisible = ref(false); // 选择的候选人数据弹窗
  const loading = ref(true); // 加载状态
  const showSearch = ref(true); // 显示搜索条件
  const open = ref(false) // 是否打开导入弹窗
  const total = ref(0); // 数据总条数
  const filtrate = ref('A') // 筛选方式
  const querySelectCompetitionRef = ref() // 搜索条件赛事选择组件
  const importRef = ref()
  const radioVal = ref(true)
  const initData = reactive({
    userName: undefined,
    teamName: undefined,
    sourceType: undefined,
    competitionName: undefined,
    phone: undefined,
    email: undefined,
    leaderTeacher: undefined,
    guideTeacherName: undefined,
    competitionRoleNameReq: undefined,
    competitionRoleName: undefined,
    filterConditions: {
      conditions: undefined,
      userScore: undefined,
      userScoreEnd: undefined,
      ranking: undefined,
      topN: undefined,
      lowN: undefined
    }
  })
  // 表单数据
  const formState = reactive({
    queryParams: {
      params: JSON.parse(JSON.stringify(initData)),
      pagination: {
        pageNum: 1,
        pageSize: 100
      }
    },
  })

  const { queryParams } = toRefs(formState);
  const certConfigId = ref(null) // certConfigId
  const isInitialFill = ref(false) // 监听表格选中是否结束

  // ******** 业务 ********
  // 搜索
  const handleQuery = () => {
    queryParams.value.pagination.pageNum = 1
    if(queryParams.value?.params?.filterConditions){
      let filterConditions = {}
      if(filtrate.value === 'A'){
        let conditions = queryParams.value?.params?.filterConditions?.conditions
        if((conditions === '4' && conditions)) {
          filterConditions = {
            conditions: queryParams.value?.params?.filterConditions?.conditions,
            userScoreStart: queryParams.value?.params?.filterConditions?.userScoreStart,
            userScoreEnd: queryParams.value?.params?.filterConditions?.userScoreEnd
          }
        }else if(conditions !== '4' && conditions){
          filterConditions = {
            conditions: queryParams.value?.params?.filterConditions?.conditions,
            userScore: queryParams.value?.params?.filterConditions?.userScore
          }
        }
      }
      if(filtrate.value === 'B'){
        let ranking = queryParams.value?.params?.filterConditions?.ranking
        if(ranking === '4' && ranking) {
          filterConditions = {
            ranking: queryParams.value?.params?.filterConditions?.ranking,
            topN: queryParams.value?.params?.filterConditions?.topN,
            lowN: queryParams.value?.params?.filterConditions?.lowN,
          }
        }else if(ranking !== '4' && ranking){
          filterConditions = {
            ranking: queryParams.value?.params?.filterConditions?.ranking,
            topN: queryParams.value?.params?.filterConditions?.topN,
          }
        }
      }
      queryParams.value.params.filterConditions = filterConditions
    }
    getList()
  }
  // 重置
  const resetQuery = () => {
    if (querySelectCompetitionRef.value) {
      querySelectCompetitionRef.value.reset();
      setTimeout(() => {
        proxy.resetForm("queryRef");
        queryParams.value.params = JSON.parse(JSON.stringify(initData))
        handleQuery();
      })
    }
    
  }
  // 查询候选人列表
  const queryCandidateCertList = async () => {

    isInitialFill.value = true
    const { data, code } = await getCandidateCertList({ certConfigId: certConfigId.value });
    if (code === 200) {
      selectionData.value = data;
      selectedIds.value = new Set(selectionData.value.map(item => item.memberId))
      // console.log(selectionData.value, 'selectionData.value');
      nextTick(() => {
        // tableRef.value.clearSelection()
        tableData.value.forEach(row => {
          tableRef.value.toggleRowSelection(row, selectedIds.value.has(row.memberId))
        })
        currentSelectData.value = tableData.value.filter(row => selectedIds.value.has(row.memberId))
        isInitialFill.value = false
      })
    }

  }
  // 查询参赛人员列表
  const getList = async () => {
    try {
      loading.value = true;
      const params = JSON.parse(JSON.stringify(queryParams.value))
      delete params.params.competitionSeriesName
      delete params.params.competitionName
      delete params.params.competitionTrackName
      delete params.params.secondLevelName
      delete params.params.competitionStageName
      if(params.params.competitionRoleNameReq) {
        params.params.competitionRoleNameReq = params.params.competitionRoleNameReq.join(',')
      }
      const { rows, code, total: count } = await getCompetitionApplyList(params)
      if (code === 200) {
        tableData.value = rows;
        total.value = count;
        loading.value = false;
        queryCandidateCertList();
      }
    } catch (error) {
      console.log(error);
    }
  }
  // 选择变化
  const handleSelectionChange = async (selection) => {
    console.log(selection,'selectionxxxxxxxxxxxxxxxxxxxxxxxxxxx');
    if (isInitialFill.value) return;
    loading.value = true;
    batchInsertCandidateCertInfo(route?.params?.certConfigId,selection).then(res => {
      if(res.code === 200) {
        proxy.$modal.msgSuccess(`更新候选人列表成功`);
        queryCandidateCertList();
      }
      else {
        proxy.$modal.msgError(`更新候选人列表失败`);  
      }
      loading.value = false;
    }).catch(() => {
      loading.value = false;
    })
  }

  // 查看候选人
  const handleView = async () => {
    router.push(`/certManage/certIssue/select/${certConfigId.value}`)
  }

  // 导入
  const handleImport = async (file) => {
    try {
      const formData = new FormData()
      formData.append("file", file)
      importCandidateCertInfo(formData, { certConfigId: certConfigId.value }).then(res => { 
        if (res.code === 200) {
          proxy.$modal.msgSuccess(`${res.msg}`);
          getList();
          importRef.value.callback(res.code)
        }else {
          importRef.value.callback()
        }
      }).catch(() => {
        importRef.value.callback()
      })
    } catch (error) {
      importRef.value.callback()
    }
  }
  // 返回
  const handleBack = () => {
    const obj = { path: "/certManage/certConfig" }
    proxy.$tab.closeOpenPage(obj)
  }


  // ******** watch ********
  watch(() => route?.params, async (params) => {
    certConfigId.value = params.certConfigId;
    queryParams.value.params.certConfigId = params.certConfigId;
    let initQueryParams = {
      ...queryParams.value.params,
      competitionId: route?.query?.competitionId,
      competitionSeriesId: route?.query?.competitionSeriesId,
      competitionSeriesName: route?.query?.competitionSeriesName,
      competitionStageId: route?.query?.competitionStageId, 
      competitionTrackId: route?.query?.competitionTrackId,
      secondLevelCode: route?.query?.secondLevelCode,
    }
    console.log(initQueryParams,'xxx');
    queryParams.value.params = initQueryParams
    handleQuery();

  }, { immediate: true })

</script>

<style lang="scss" scoped>
  .app-container {
    display: flex;
    flex-direction: column;
    height: calc(100vh - 85px);
    width: 100%;
  }

  .table-content {
    flex: 1;
    min-height: 0;
    /* 防止子内容高度塌陷 */
    overflow: auto;
  }

  // :deep(.el-table__header-wrapper .el-table-column--selection .el-checkbox) {
  //   display: none;
  // }
</style>