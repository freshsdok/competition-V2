<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true" label-width="95px" class="mb8">
      <SelectCompetitionBack ref="querySelectCompetitionRef" v-model="queryParams" label-width="95px">
        <template #suffix>
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
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          v-hasPermi="['competition:certConfigInfo:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table style="border: 1px solid #ebeef5;" v-loading="loading" :data="tableData">
      <el-table-column label="序号" align="left" type="index" width="50" />
      <el-table-column label="配置名称" prop="certConfigName" min-width="150" show-overflow-tooltip />
      <el-table-column label="赛事名称" prop="competitionName" min-width="220" show-overflow-tooltip />
     <el-table-column label="赛道/组别" align="left" prop="competitionTrackName" min-width="200">
        <template #default="scope">
          <span style="color: #ff8800">{{
            scope.row.competitionTrackName
          }}</span>
          <span>-</span>
          <span style="color: #51c512">{{ scope.row.secondLevelName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="阶段" prop="competitionStageName" min-width="120" show-overflow-tooltip align="center" />
      <el-table-column label="有效期" prop="certPeriodType" min-width="250" show-overflow-tooltip align="center">
        <template #default="scope">
          {{ scope.row.certPeriodType === '1' ? '永久' : (scope.row.certPeriodTime ?
            `有效期截至${parseTime(scope.row.certPeriodTime, '{y}-{m}-{d} {h}:{i}:{s}')}` :
            '-') }}
        </template>
      </el-table-column>
      <el-table-column label="证书管理员" prop="certManagerRoleName" min-width="120" show-overflow-tooltip align="center" />  
      <el-table-column label="颁奖机构" prop="orgCode" min-width="150" show-overflow-tooltip align="center">
        <template #default="scope">
          {{selectOrganizationOptions.find(item => item.orgCode === scope.row.orgCode)?.orgName ?? '-'}}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" min-width="180" show-overflow-tooltip algin="center" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" link type="primary" icon="Promotion" @click="handleIssue(scope.row)"
            v-hasPermi="['competition:certConfigInfo:issue']">颁发</el-button>
          <el-button size="small" link type="success" @click="handleUpdate(scope.row)"
            v-hasPermi="['competition:certConfigInfo:edit']" icon="Edit">编辑</el-button>
          <el-button size="small" link type="danger" @click="handleDelete(scope.row)"
            v-hasPermi="['competition:certConfigInfo:remove']" icon="Delete">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
<el-dialog v-model="open" :title="title" width="1000px" destroy-on-close :show-close="!disabled"
      :close-on-click-modal="!disabled">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" v-if="open">
        <el-row :gutter="20">
          <SelectCompetitionBack ref="formSelectCompetitionRef" label-width="120px" :span="8" v-model="form"
            v-if="open">
            <template #prefix>
              <el-form-item label="是否关联大赛" prop="isCompetition">
                <el-radio-group v-model="form.isCompetition" style="width: 180px">
                  <el-radio :label="true">是</el-radio>
                  <el-radio :label="false">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否学习大赛" prop="isCourse">
                <el-radio-group v-model="form.isCourse" disabled style="width: 180px">
                  <el-radio :label="true">是</el-radio>
                  <el-radio :label="false">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否关联培训" prop="isTrainingProgram">
                <el-radio-group v-model="form.isTrainingProgram" disabled style="width: 180px">
                  <el-radio :label="true">是</el-radio>
                  <el-radio :label="false">否</el-radio>
                </el-radio-group>
              </el-form-item>
            </template>
            <template #suffix>
              <el-form-item label="证书来源" prop="certSource">
                <el-select v-model="form.certSource" placeholder="请选择证书来源" clearable style="width: 180px">
                  <el-option v-for="dict in cert_source" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
              <template v-if="form.certSource === '2'">
                <el-form-item label="链接名称" prop="certLinkName">
                  <el-input v-model="form.certLinkName" placeholder="请输入链接名称" clearable style="width: 180px" />
                </el-form-item>
                <el-form-item label="链接地址" prop="certLinkUrl">
                  <el-input v-model="form.certLinkUrl" placeholder="请输入链接地址" clearable style="width: 180px" />
                </el-form-item>
              </template>
              <el-form-item label="配置名称" prop="certConfigName">
                <el-input v-model="form.certConfigName" placeholder="请输入配置名称" clearable style="width: 180px" />
              </el-form-item>
              <el-form-item label="有效期" prop="certPeriodType">
                <el-select v-model="form.certPeriodType" placeholder="请选择有效期" clearable style="width: 180px">
                  <el-option label="永久" value="1" />
                  <el-option label="指定日期" value="2" />
                </el-select>
              </el-form-item>
              <template v-if="form.certPeriodType === '2'">
                <el-form-item label="有效期截止日期" prop="certPeriodTime">
                  <el-date-picker v-model="form.certPeriodTime" type="datetime" placeholder="有效期截止日期"
                    style="width: 180px" />
                </el-form-item>
              </template>
              <el-form-item label="证书管理员" prop="certManagerRole">
                <el-select v-model="form.certManagerRole" placeholder="请选择证书管理员" clearable style="width: 180px">
                  <el-option v-for="dict in selectCertManageUserOptions" :key="dict.userId" :label="dict.nickName"
                    :value="dict.userId" />
                </el-select>
              </el-form-item>
              <el-form-item label="颁奖机构" prop="orgCode">
                <el-select v-model="form.orgCode" placeholder="请选择颁奖机构" clearable style="width: 180px">
                  <el-option v-for="dict in selectOrganizationOptions" :key="dict.orgCode" :label="dict.orgName"
                    :value="dict.orgCode" />
                </el-select>
              </el-form-item>
              <el-form-item label="证书状态" prop="certStatus">
                <el-select v-model="form.certStatus" placeholder="请选择证书状态" clearable style="width: 180px">
                  <el-option v-for="dict in cert_status" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="奖项类型" prop="awardsName">
                <el-select v-model="form.awardsName" placeholder="请选择奖项类型" clearable style="width: 180px">
                  <el-option v-for="dict in awards_name" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
            </template>
          </SelectCompetitionBack>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="handleSave" :disabled="disabled" :loading="disabled">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
  // ******** 插件 ********
  const { proxy } = getCurrentInstance();
  const router = useRouter();

  // ******** 组件 ********
  import SelectCompetitionBack from "@/views/certInterconnect/components/SelectCompetitionBack.vue"

  // ******** api ********
  import { getCertConfigList, getCertConfigInfo, addCertConfigInfo, updateCertConfigInfo, delCertConfig, getSelectCertManageUserList, getSelectOrganizationList } from "@/api/certInterconnect/certConfig.js"

  // ******* 字典 ********
  const {
    cert_source,
    cert_status,
    awards_name
  } = proxy.useDict(
    "cert_source", // 证书来源
    "cert_status", // 证书状态
    "awards_name", // 奖项类型
  );

  // ******** 初始化 ********
  const tableData = ref([]); // table数据
  const open = ref(false); // 弹窗
  const loading = ref(true); // 加载状态
  const showSearch = ref(true); // 显示搜索条件
  const total = ref(0); // 数据总条数
  const title = ref(""); // 弹窗标题
  const selectCertManageUserOptions = ref([]) // 证书管理员
  const selectOrganizationOptions = ref([]) // 证书颁发机构
  const querySelectCompetitionRef = ref() // 搜索条件赛事选择组件
  const formSelectCompetitionRef = ref() // 表单赛事选择组件
  const disabled = ref(false) // 表单提交防重复
  const initFormData = {
    isCompetition: false,
    isCourse: false,
    isTrainingProgram: false,
    certSource: '',
    certLinkName: '',
    certLinkUrl: '',
    certConfigName: '',
    certPeriodType: '',
    certPeriodTime: '',
    certManagerRole: '',
    orgCode: '',
    certStatus: '',
    awardsName: '',
    competitionSeriesId: '',
    competitionTrackId: '',
    secondLevelCode: '',
    competitionStageId: ''
  }
  // 表单参数： 搜索条件表单数据、新增编辑表单数据、新增编辑表单校验规则
  const formState = reactive({
    queryParams: {
      pageNum: 1,
      pageSize: 10,
      competitionSeriesId: '',
      competitionTrackId: '',
      competitionStageId: '',
      secondLevelCode: '',
      certConfigName: '',
      certManagerRole: '',
      orgCode: '',
    },
    form: JSON.parse(JSON.stringify(initFormData)),
    rules: {
      competitionSeriesId: [{ required: true, message: '请选择赛事名称', trigger: ['blur', 'change'] }],
      certSource: [{ required: true, message: '请选择证书来源', trigger: ['blur', 'change'] }],
      certConfigName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
      certPeriodType: [{ required: true, message: '请选择证书有效期', trigger: ['blur', 'change'] }],
      certPeriodTime: [{ required: true, message: '请选择有效期截止日期', trigger: ['blur', 'change'] }],
      certManagerRole: [{ required: true, message: '请选择证书管理员', trigger: ['blur', 'change'] }],
      orgCode: [{ required: true, message: '请选择颁奖机构', trigger: ['blur', 'change'] }],
      certStatus: [{ required: true, message: '请选择证书状态', trigger: ['blur', 'change'] }],
      awardsName: [{ required: true, message: '请选择证书状态', trigger: ['blur', 'change'] }],
      certLinkName: [{ required: true, message: '请输入链接名称', trigger: 'blur' }],
      certLinkUrl: [{ required: true, message: '请输入链接地址', trigger: 'blur' }],
    }
  })

  // 解构响应式对象，获取其中的属性
  const { queryParams, form, rules } = toRefs(formState);


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
  // 搜索
  const handleQuery = () => {
    queryParams.value.pageNum = 1;
    getList();
  };
  // 重置搜索条件
  const resetQuery = () => {
    proxy.resetForm("queryRef");
    if (querySelectCompetitionRef.value) {
      querySelectCompetitionRef.value.reset();
      setTimeout(() => handleQuery())
    }

  };
  // 查询列表
  const getList = async () => {
    try {
      loading.value = true;
      const params = JSON.parse(JSON.stringify(queryParams.value))
      delete params.competitionSeriesName
      delete params.competitionName
      delete params.competitionTrackName
      delete params.secondLevelName
      delete params.competitionStageName
      const { rows, code, total: count } = await getCertConfigList(params)
      if (code === 200) {
        loading.value = false;
        tableData.value = rows;
        total.value = count
      }
    } catch (error) {
      proxy.$modal.msgWarning(error?.message ?? '操作失败');
    }

  };
  // 新增
  const handleAdd = () => {
    title.value = "新增证书配置";
    resetForm();
    open.value = true;
  };
  // 编辑
  const handleUpdate = async (row) => {
    try {
      const { code, data } = await getCertConfigInfo(row.certConfigId)
      if (code === 200) {
        title.value = "编辑证书配置";
        // 将ID字段转为字符串，确保与SelectCompetitionBack组件中的el-option value类型一致
        form.value = { 
          ...data,
          competitionSeriesId: data.competitionSeriesId ? String(data.competitionSeriesId) : '',
          competitionStageId: data.competitionStageId ? String(data.competitionStageId) : '',
          competitionTrackId: data.competitionTrackId ? String(data.competitionTrackId) : '',
          secondLevelCode: data.secondLevelCode ? String(data.secondLevelCode) : ''
        };
        open.value = true;
      }
    } catch (error) {
      proxy.$modal.msgWarning(error?.message ?? '操作失败');
    }

  };
  // 颁发证书
  const handleIssue = (row) => {
    router.push({
      path: `/certManage/certIssue/index/${row.certConfigId}`,
      query: {
        competitionId: row.competitionId,
        competitionSeriesId: row.competitionSeriesId,
        competitionSeriesName: row.competitionSeriesName,
        competitionStageId: row.competitionStageId,
        competitionTrackId: row.competitionTrackId,
        secondLevelCode: row.secondLevelCode,
      }
    });
  }
  // 表单重置
  const resetForm = () => {
    formSelectCompetitionRef.value?.reset()
    form.value = JSON.parse(JSON.stringify(initFormData));
  }
  // 删除
  const handleDelete = (row) => {
    const ids = row?.certConfigId ? [row?.certConfigId] : []
    proxy.$confirm("是否删除该证书配置？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }).then(function () {
      return delCertConfig(ids);
    })
      .then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
      })
      .catch(() => { });
  };

  // 保存（新增/编辑）
  const handleSave = () => {
    proxy.$refs.formRef.validate(async (valid) => {
      try {
        if (!valid) return;
        disabled.value = true;
        form.value.certPeriodTime = Number(form.value.certPeriodTime)
        if (form.value.certConfigId) {
          const { code, msg } = await updateCertConfigInfo(form.value)
          if (code === 200) {
            proxy.$modal.msgSuccess("编辑成功");
          } else {
            throw new Error(msg || '编辑失败')
          }
        } else {
          const { code, msg } = await addCertConfigInfo(form.value)
          if (code === 200) {
            proxy.$modal.msgSuccess("新增成功");
          } else {
            throw new Error(msg || '新增失败')
          }
        }
        open.value = false;
        disabled.value = false;
        getList()
      } catch (error) {
        disabled.value = false;
      }
    });
  };

  // 监听证书来源变化，动态设置链接名称和链接地址的验证规则
  watch(() => form.value.certSource, (newVal) => {
    // 切换证书来源时清空链接名称和链接地址
    form.value.certLinkName = ''
    form.value.certLinkUrl = ''
    if (newVal === '2') {
      // 外部证书：链接名称和链接地址必填
      rules.value.certLinkName = [{ required: true, message: '请输入链接名称', trigger: 'blur' }]
      rules.value.certLinkUrl = [{ required: true, message: '请输入链接地址', trigger: 'blur' }]
    } else {
      // 内部证书或其他：链接名称和链接地址不必填
      rules.value.certLinkName = [{ required: false }]
      rules.value.certLinkUrl = [{ required: false }]
      // 清空验证错误信息
      if (proxy.$refs.formRef) {
        proxy.$refs.formRef.clearValidate(['certLinkName', 'certLinkUrl'])
      }
    }
  }, { immediate: true })

  onMounted(() => {
    handleQuery();
    getSelects();
  });
</script>

<style lang="scss" scoped></style>