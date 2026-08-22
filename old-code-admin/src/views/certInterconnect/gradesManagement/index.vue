<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="state.showSearch" :inline="true" label-width="100px"
      class="mb8">
      <SelectCompetitionBack ref="querySelectCompetitionRef" v-model="queryParams" label-width="100px">
        <template #suffix>
          <el-form-item label="参赛者姓名" prop="userName">
            <el-input v-model="queryParams.userName" placeholder="请输入参赛者姓名" clearable @keyup.enter="handleQuery"
              style="width: 180px;" />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="queryParams.phone" placeholder="请输入联系电话" clearable @keyup.enter="handleQuery"
              style="width: 180px;" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="queryParams.email" placeholder="请输入邮箱" clearable @keyup.enter="handleQuery"
              style="width: 180px;" />
          </el-form-item>
          <el-form-item label="带队老师姓名" prop="leaderTeacherName">
            <el-input v-model="queryParams.leaderTeacherName" placeholder="请输入带队老师姓名" clearable @keyup.enter="handleQuery"
              style="width: 180px;" />
          </el-form-item>
          <el-form-item label="筛选方式">
            <el-radio-group v-model="filtrate">
              <el-radio label="A">按成绩</el-radio>
              <el-radio label="B">按排名</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="筛选条件" :prop="filtrate === 'A' ? 'conditions' : 'ranking'">
            <el-select placeholder="请选择筛选条件" clearable style="width: 180px"
              v-model="queryParams.filterConditions[filtrate === 'A' ? 'conditions' : 'ranking']">
              <el-option v-for="dict in conditions" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="filtrate === 'A' ? '分值' : '排名值'" prop="userScore">
            <el-input-number v-model="queryParams.filterConditions[filtrate === 'A' ? 'userScore' : 'topN']"
              :placeholder="filtrate === 'A' ? '请输入分值' : '请输入排名值'" clearable @keyup.enter="handleQuery" style="width: 180px"
              :controls="false"
              v-if="queryParams.filterConditions[filtrate === 'A' ? 'conditions' : 'ranking'] !== '4'" />
            <!-- 区间 -->
            <span v-else style="display: flex; align-items: center;">
              <el-input-number v-model="queryParams.filterConditions[filtrate === 'A' ? 'userScoreStart' : 'topN']"
                :placeholder="filtrate === 'A' ? '开始分值' : '开始排名'" style="width: 85px" :controls="false" />
              <span style="width: 10px;text-align: center;">-</span>
              <el-input-number v-model="queryParams.filterConditions[filtrate === 'A' ? 'userScoreEnd' : 'lowN']"
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

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain @click="handleOpenImport" icon="Upload"
          v-hasPermi="['competition:competitionGradeInfo:import']">成绩导入</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="state.showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table style="border: 1px solid #ebeef5;" v-loading="state.loading" :data="state.tableData" stripe>
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="userName" label="姓名" min-width="80" show-overflow-tooltip align="center" />
      <el-table-column prop="gradeSource" label="来源类型" min-width="80" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{ row?.gradeSource === 'import' ? '导入' : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="sourceData" label="来源数据" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row?.competitionName }} / {{ row?.competitionTrackName }}
        </template>
      </el-table-column>
      <el-table-column prop="stageName" label="阶段" min-width="120" show-overflow-tooltip align="center" />
      <el-table-column prop="phone" label="联系电话" min-width="120" show-overflow-tooltip />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column prop="idCard" label="身份证号" min-width="180" show-overflow-tooltip />
      <el-table-column prop="leaderTeacherName" label="带队老师" min-width="80" show-overflow-tooltip />
      <el-table-column prop="competitionRoleName" label="角色" min-width="90" show-overflow-tooltip align="center" />
      <el-table-column prop="score" label="成绩" min-width="100" align="center">
        <template #default="{ row }">
          <el-input-number v-if="row.isEdit" v-model="row.score" placeholder="成绩" style="width: 70px"
            :controls="false" />
          <span v-else>{{ row.score ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="ranking" label="排名" min-width="80" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.isEdit" v-hasPermi="['competition:competitionGradeInfo:edit']" link type="success"
            size="small" icon="Finished" @click="handleUpdate(row)">保存</el-button>
          <el-button v-else link type="warning" size="small" icon="Edit" @click="row.isEdit = true"
            v-hasPermi="['competition:competitionGradeInfo:edit']">编辑</el-button>
          <el-button link type="danger" size="small" icon="Delete" @click="handleDelete(row)"
            v-hasPermi="['competition:competitionGradeInfo:del']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="state.total > 0" :total="state.total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <ImportData ref="importRef" v-model="state.importOpen" title="成绩导入" tempName="成绩" @import="handleImport">
      <SelectCompetitionBack ref="importSelectCompetitionRef" v-model="selectCompetition" label-width="100px" />
    </ImportData>

    <!-- 导入结果确认弹框 -->
    <ImportResultDialog v-model="state.importResultOpen" :table-data="state.importResultData" @cover-update="handleCoverUpdate" @give-up="handleGiveUp" />

  </div>
</template>

<script setup>
  // ******* 组件 ********
  import SelectCompetitionBack from "@/views/certInterconnect/components/SelectCompetitionBack.vue"
  import ImportData from "@/views/certInterconnect/components/ImportData.vue"
  import ImportResultDialog from "./ImportResultDialog.vue"

  // ******* API ********
  import { getCompetitionGradeList, updateCompetitionGradeInfo, delCompetitionGrade, importCompetitionGrade, updateGradeInfo } from "@/api/certInterconnect/gradesManagement.js"

  // ******* 插件 ********
  const { proxy } = getCurrentInstance()

  // ******* 字典 ********
  const {
    conditions,
  } = proxy.useDict(
    "conditions",
  );

  // ******* 初始化 ********
  const querySelectCompetitionRef = ref() // 搜索条件赛事选择组件
  const importSelectCompetitionRef = ref()

  const filtrate = ref('A')

  const initData = {
    userName: undefined,
    teamName: undefined,
    sourceType: undefined,
    phone: undefined,
    email: undefined,
    competitionRoleName: undefined,
    leaderTeacherName: undefined,
    filterConditions: {
      conditions: undefined,
      userScore: undefined,
      userScoreEnd: undefined,
      ranking: undefined,
      topN: undefined,
      lowN: undefined
    }
  }

  const queryParams = ref({
    pageNum: 1,
    pageSize: 10,
    ...JSON.parse(JSON.stringify(initData))
  })

  const state = reactive({
    tableData: [],
    total: 0,
    loading: false,
    showSearch: true,
    open: false,
    importOpen: false,
    disabled: false,
    importResultOpen: false, // 导入结果弹框显示状态
    importResultData: [], // 导入结果数据
  })

  // 赛事组件信息
  const selectCompetition = ref({
    competitionSeriesId: null,
    competitionStageId: null,
    secondLevelCode: null,
    competitionTrackId: null,
  })
  const importRef = ref()


  const getList = async () => {
    state.loading = true
    const params = JSON.parse(JSON.stringify(queryParams.value))
    delete params.competitionSeriesName
    delete params.competitionName
    delete params.competitionTrackName
    delete params.secondLevelName
    delete params.competitionStageName
    if(params?.competitionStageId){
      params.stageName = params?.competitionStageId
    }else{
      params.stageName = ''
    }
    try {
      const { rows, total } = await getCompetitionGradeList(params);
      state.tableData = rows;
      state.total = total;
      state.loading = false
    } catch (error) {
      state.loading = false
    }
  }

  // 搜索
  const handleQuery = () => {
    queryParams.pageNum = 1;
    if(queryParams.value?.filterConditions){
      let filterConditions = {}
      if(filtrate.value === 'A'){
        let conditions = queryParams.value?.filterConditions?.conditions
        if((conditions === '4' && conditions)) {
          filterConditions = {
            conditions: queryParams.value?.filterConditions?.conditions,
            userScoreStart: queryParams.value?.filterConditions?.userScoreStart,
            userScoreEnd: queryParams.value?.filterConditions?.userScoreEnd
          }
        }else if(conditions !== '4' && conditions){
          filterConditions = {
            conditions: queryParams.value?.filterConditions?.conditions,
            userScore: queryParams.value?.filterConditions?.userScore
          }
        }
      }
      if(filtrate.value === 'B'){
        let ranking = queryParams.value?.filterConditions?.ranking
        if(ranking === '4' && ranking) {
          filterConditions = {
            ranking: queryParams.value.filterConditions.ranking,
            topN: queryParams.value?.filterConditions?.topN,
            lowN: queryParams.value?.filterConditions?.lowN,
          }
        }else if(ranking !== '4' && ranking){
          filterConditions = {
            ranking: queryParams.value?.filterConditions?.ranking,
            topN: queryParams.value?.filterConditions?.topN,
          }
        }
      }
      queryParams.value.filterConditions = filterConditions
    }
    getList()
  }

  // 重置搜索条件
  const resetQuery = () => {
    proxy.resetForm("queryRef");
    if (querySelectCompetitionRef.value) {

      querySelectCompetitionRef.value.reset();
      setTimeout(() => {
        filtrate.value = 'A'
        queryParams.value = {
          ...queryParams.value,
          ...JSON.parse(JSON.stringify(initData))
        }
        handleQuery()
      })
    }

  }

  // 导入
  const handleOpenImport = () => {
    state.importOpen = true
    selectCompetition.value = {
      competitionSeriesId: null,
      competitionStageId: null,
      secondLevelCode: null,
      competitionTrackId: null,
    }
    importSelectCompetitionRef.value?.reset();
  }
  const handleImport = async (file) => {
    try {
      const formData = new FormData()
      formData.append("file", file);
      const { code, data, msg } = await importCompetitionGrade(formData, selectCompetition.value)
      importRef.value.callback(code)
      if (code === 200) {
        getList();
        if (data && data.length > 0) {
          // 有重复数据，显示确认弹框
          state.importResultData = data;
          state.importResultOpen = true;
        } else {
          // 无重复数据，直接刷新列表
          proxy.$modal.msgSuccess(`${msg}`);
        }
      }
    } catch (error) {
      importRef.value.callback()
    }
  }

  // 覆盖更新
  const handleCoverUpdate = async (data) => {
    try {
      const { code, msg } = await updateGradeInfo(data);
      if (code === 200) {
        proxy.$modal.msgSuccess(`${msg || '覆盖更新成功'}`);
        state.importResultOpen = false;
        state.importResultData = [];
        getList();
      }
    } catch (error) {
      console.error('覆盖更新失败:', error);
    }
  }

  // 放弃导入
  const handleGiveUp = () => {
    state.importResultOpen = false;
    state.importResultData = [];
  }

  // 修改成绩
  const handleUpdate = async (row) => {
    await updateCompetitionGradeInfo(row);
    proxy.$message.success('用户成绩信息修改成功')
    getList()
  }

  // 删除
  const handleDelete = async row => {
    proxy.$confirm('是否删除该条记录？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(async () => {
      await delCompetitionGrade(row.gradeId)
      proxy.$message.success('删除成功')
      getList()
    })
  }

  onMounted(() => {
    getList()
  })
</script>
