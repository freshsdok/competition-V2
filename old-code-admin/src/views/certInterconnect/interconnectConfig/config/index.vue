<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true" label-width="95px" class="mb8">
      <SelectCompetitionBack ref="querySelectCompetitionRef" v-model="queryParams" label-width="95px">
        <template #suffix>
          <el-form-item label="来源类型" prop="certSource">
            <el-select v-model="queryParams.certSource" placeholder="请选择来源类型" clearable style="width: 180px">
              <el-option v-for="dict in cert_source" :key="dict.value" :label="dict.label"
                :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="配置名称" prop="certConfigName">
            <el-input v-model="queryParams.certConfigName" placeholder="请输入配置名称" clearable @keyup.enter="handleQuery"
              style="width: 180px" />
          </el-form-item>
          <el-form-item label="证书管理员" prop="certManagerRole">
            <el-select v-model="queryParams.certManagerRole" placeholder="请选择证书管理员" clearable style="width: 180px">
              <el-option v-for="dict in selectCertManageUserOptions" :key="dict.userId" :label="dict.nickName"
                :value="dict.userId" />
            </el-select>
          </el-form-item>
          <el-form-item label="颁奖机构" prop="orgCode">
            <el-select v-model="queryParams.orgCode" placeholder="请选择颁奖机构" clearable style="width: 180px">
              <el-option v-for="dict in selectOrganizationOptions" :key="dict.orgCode" :label="dict.orgName"
                :value="dict.orgCode" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </template>
      </SelectCompetitionBack>
    </el-form>
    <el-row class="mb8" :gutter="10">
      <el-col :span="1.5">
        <el-button type="primary" @click="open = true" plain :disabled="isSaveDisabled" icon="Finished"
          v-hasPermi="['competition:competitionCertExchangeRule:save']">暂 存 配
          置</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button @click="handleBack" icon="Back" plain type="warning">返回</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-tabs v-model="tabActive" @tab-change="handleTabChange">
      <el-tab-pane label="源证书配置" name="originCertList" />
      <el-tab-pane label="目标证书配置" name="targetCertList" />

    </el-tabs>


    <div class="table-content">
      <el-table style="border: 1px solid #ebeef5;" :data="tableData" ref="tableRef" row-key="certConfigId" stripe
        @selection-change='handleSelectionChange' :key="tabActive" v-loading="loading">
        <el-table-column type="selection" width="55" reserve-selection />
        <el-table-column prop="certSource" label="来源类型" min-width="80" show-overflow-tooltip>
          <template #default="{ row }">
            {{cert_source.find(item => item.value === row.certSource)?.label ?? '-'}}
          </template>
        </el-table-column>
        <el-table-column prop="certConfigName" label="配置名称" min-width="100" show-overflow-tooltip align="center" />
        <el-table-column label="赛事名称" prop="name" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            {{ `${scope.row.competitionName ?? '-'}` }}
          </template>
        </el-table-column>
        <el-table-column label="赛道/组别" prop="name" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            {{ `${scope.row.competitionTrackName ?? ''}-${scope.row.secondLevelName ?? ''}` }}
          </template>
        </el-table-column>
        <el-table-column prop="competitionSeriesName" label="届数" width="100" show-overflow-tooltip align="center" />
        <el-table-column prop="awardsName" label="奖项类型" width="100" show-overflow-tooltip align="center">
          <template #default="{ row }">
            {{awards_name.find(item => item.value === row.awardsName)?.label ?? '-'}}
          </template>
        </el-table-column>
        <el-table-column prop="competitionStageName" label="阶段" min-width="100" show-overflow-tooltip align="center" />
        <el-table-column prop="ownYear" label="拥有年限" width="120" v-if="tabActive === 'originCertList'" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.ownYear" placeholder="年限" clearable :controls="false" style="width: 80px"
              :class="{ 'required-input': formSource[tabActive].some(item => item.certConfigId === row.certConfigId) && (row.ownYear == null || row.ownYear === '') }"
              @change="updateOwnYear(row, $event)" />
          </template>
        </el-table-column>
        <el-table-column prop="score" :label="`${tabActive === 'originCertList' ? '源' : '目标'}证书分值`" width="120"
          align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.originCertScore" placeholder="分值" clearable :controls="false"
              style="width: 80px"
              :class="{ 'required-input': formSource[tabActive].some(item => item.certConfigId === row.certConfigId) && (row.originCertScore == null || row.originCertScore === '') }"
              v-if="tabActive === 'originCertList'" @change="updateOriginScore(row, $event)" />
            <el-input-number v-if="tabActive === 'targetCertList'" v-model="row.targetCertScore" placeholder="分值"
              clearable :controls="false" style="width: 80px"
              :class="{ 'required-input': formSource[tabActive].some(item => item.certConfigId === row.certConfigId) && (row.targetCertScore == null || row.targetCertScore === '') }"
              @change="updateTargetScore(row, $event)" />
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 分页 -->
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
    <!-- 配置表单 -->
    <Setting v-model="open" v-model:data="formSource" @confirm="handleBack" />
  </div>
</template>

<script setup>
  // ******** 组件 ********
  import Setting from './setting.vue';
  import SelectCompetitionBack from "@/views/certInterconnect/components/SelectCompetitionBack.vue"

  // ****** API ******
  import { getCertExchangeRuleInfo } from "@/api/certInterconnect/interconnectConfig.js"
  import { getCertConfigList, getSelectCertManageUserList, getSelectOrganizationList } from "@/api/certInterconnect/certConfig.js"

  // ******** 插件 ********
  const { proxy } = getCurrentInstance();
  const route = useRoute()

  // ******* 字典 ********
  const {
    awards_name,
    cert_source,
  } = proxy.useDict(
    "awards_name",
    "cert_source",
  );

  // ******** 初始化 ********
  const tabActive = ref('originCertList'); // 当前激活的标签页
  const tableRef = ref(null); // table ref
  const open = ref(false); // 弹窗是否打开
  const loading = ref(true); // 是否正在恢复选择，避免触发 selection-change
  // 数据源
  const formSource = reactive({
    originCertList: [],
    targetCertList: []
  })
  const SCORE_KEY = {
    originCertList: 'originCertScore',
    targetCertList: 'targetCertScore'
  }
  // 搜索信息
  const queryParams = ref({
    pageNum: 1,
    pageSize: 10,
    competitionSeriesId: '',
    competitionTrackId: '',
    secondLevelCode: '',
    certConfigName: '',
    certSource: '',
    certManagerRole: '',
    orgCode: '',
    stageId: ''
  })
  const tableData = ref([]) // 表格数据
  const total = ref(0) //总条数
  const querySelectCompetitionRef = ref()
  const showSearch = ref(true)
  const selectCertManageUserOptions = ref([])
  const selectOrganizationOptions = ref([])



  // ******** 计算属性 ********
  const isSaveDisabled = computed(() => {
    const originList = formSource?.originCertList || [];
    const targetList = formSource?.targetCertList || [];

    const originCertValid = originList.length > 0 && originList.every(row => row.originCertScore != null && row.originCertScore !== '');
    const ownYearValid = originList.length > 0 && originList.every(row => row.ownYear != null && row.ownYear !== '');
    const targetCertValid = targetList.length > 0 && targetList.every(row => row.targetCertScore != null && row.targetCertScore !== '');

    // if (tabActive.value === 'originCertList') {
    //   // 当前在源证书页面，按源证书字段校验
    //   return !(originCertValid && ownYearValid);
    // }

    // if (tabActive.value === 'targetCertList') {
    //   // 当前在目标证书页面，按目标证书字段校验
    //   return !targetCertValid;
    // }

    // 默认：两个维度都要通过
    return !(originCertValid && ownYearValid && targetCertValid);
  });


  // ******** 业务 ********
  const getSelects = async () => {
    try {
      const { rows: certManageUserRows, code: certManageUserCode } = await getSelectCertManageUserList();
      if (certManageUserCode === 200) {
        selectCertManageUserOptions.value = certManageUserRows;
      }
      const { rows: organizationRows, code: organizationCode } = await getSelectOrganizationList();
      if (organizationCode === 200) {
        selectOrganizationOptions.value = organizationRows
      }
    } catch (error) {
      console.log(error);
    }
  }
  // 编辑获取规则配置信息
  const queryCertExchangeRuleInfo = async (ruleId) => {
    loading.value = true;
    if (ruleId) {
      const { data, code } = await getCertExchangeRuleInfo(ruleId)
      if (code !== 200) return;
      data.originCertList = data?.originCertList.map(item => ({ ...item, ownYear: item?.ownYear ?? 0 })) ?? []
      data.targetCertList = data?.targetCertList && data?.targetCertList.length > 0 ? data?.targetCertList : data?.detailList.map(item => ({ ...item, certConfigId: item.detailId }))
      Object.assign(formSource, JSON.parse(JSON.stringify(data)))
    }
    handleQuery();
  }

  // 获取列表数据
  const getList = async () => {
    try {
      loading.value = true;
      const params = JSON.parse(JSON.stringify(queryParams.value))
      delete params.competitionSeriesName
      delete params.competitionName
      delete params.competitionTrackName
      delete params.secondLevelName
      delete params.competitionStageName
      const { rows, total: count } = await getCertConfigList(params);
      tableData.value = rows;
      total.value = count;
      nextTick(() => {
        // 已选择的数据需要默认选中
        if (formSource[tabActive.value]) {
          tableData.value.forEach(row => {
            if (formSource[tabActive.value].some(item => item.certConfigId === row.certConfigId)) {
              if (tabActive.value === 'originCertList') {
                row.ownYear = Number(formSource[tabActive.value].find(item => item.certConfigId === row.certConfigId)?.ownYear ?? 0); // 保持年限同步
              }
              row[SCORE_KEY[tabActive.value]] = Number(formSource[tabActive.value].find(item => item.certConfigId === row.certConfigId)?.[SCORE_KEY[tabActive.value]] ?? 0); // 保持分值同步
              tableRef.value.toggleRowSelection(row, true)
            } else {
              row[SCORE_KEY[tabActive.value]] = null; // 没有选中则清空分值
              if (tabActive.value === 'originCertList') {
                row.ownYear = null;
              }
            }
          })
          loading.value = false;
        }
      })
    } catch (error) {
      console.log('获取数据失败:', error);
    }
  }

  // 查询数据时需要重置分页参数
  const handleQuery = () => {
    queryParams.value.pageNum = 1;
    getList();
  }

  // 标签页切换时获取对应的数据
  const handleTabChange = (val) => {
    tabActive.value = val;
    resetQuery();
  }

  // 搜索条件表单重置
  const resetQuery = () => {
    if (querySelectCompetitionRef.value) {
      querySelectCompetitionRef.value.reset();
      setTimeout(() => {
        proxy.resetForm("queryRef");
        handleQuery()
      })
    }
  };

  // 选择
  const handleSelectionChange = (selection) => {
    if (loading.value) return;
    const newSelectData = selection.filter(item => !formSource[tabActive.value].some(row => row.certConfigId === item.certConfigId));
    const rmSelectData = formSource[tabActive.value].filter(row => !selection.some(item => item.certConfigId === row.certConfigId));
    Object.assign(formSource, { [tabActive.value]: [...formSource[tabActive.value].filter(row => !rmSelectData.some(r => r.certConfigId === row.certConfigId)), ...newSelectData] })
  }

  // 更新 ownYear
  const updateOwnYear = (row, value) => {
    const item = formSource.originCertList.find(item => item.certConfigId === row.certConfigId);
    if (item) {
      item.ownYear = value;
    }
  }

  // 更新 originCertScore
  const updateOriginScore = (row, value) => {
    const item = formSource.originCertList.find(item => item.certConfigId === row.certConfigId);
    if (item) {
      item.originCertScore = value;
    }
  }

  // 更新 targetCertScore
  const updateTargetScore = (row, value) => {
    const item = formSource.targetCertList.find(item => item.certConfigId === row.certConfigId);
    if (item) {
      item.targetCertScore = value;
    }
  }

  // 返回
  const handleBack = () => {
    const obj = { path: "/certManage/interconnectConfig" }
    proxy.$tab.closeOpenPage(obj)
  }

  // ****** watch ******
  watch(() => open.value, (val) => {
    if (!val && tableRef.value) {
      loading.value = true;
      tableRef.value.clearSelection();
      getList();
    }
  }, { immediate: true })

  onMounted(() => {
    queryCertExchangeRuleInfo(route?.query?.ruleId)
    getSelects()
  })
</script>

<style lang="scss" scoped>
  .config-content {
    width: 100%;
    overflow: auto;
    display: flex;
    gap: 10px;
    flex-direction: column;
  }

  .dialog-footer {
    display: flex;
    justify-content: center;
    gap: 10px;
  }

  .table-content {
    flex: 1;
    min-height: 300px;
    /* 防止子内容高度塌陷 */
    overflow: auto;
    width: 100%;
  }



  .required-input :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #f56c6c inset !important;
  }
</style>