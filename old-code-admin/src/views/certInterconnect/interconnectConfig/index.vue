<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true" label-width="100px" class="mb8">
      <SelectCompetitionBack ref="querySelectCompetitionRef" v-model="queryParams" label-width="100px">
        <template #suffix>
          <el-form-item label="源证书" prop="originCertName">
            <el-input v-model="queryParams.originCertName" placeholder="请输入源证书" clearable style="width:180px" />
          </el-form-item>
          <el-form-item label="目标证书" prop="targetCertName">
            <el-input v-model="queryParams.targetCertName" placeholder="请输入目标证书" clearable style="width:180px" />
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
        <el-button type="primary" plain @click="handleAction('add')" icon="Plus"
          v-hasPermi="['competition:competitionCertExchangeRule:add']">新增赛证互通配置</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table style="border: 1px solid #ebeef5;" v-loading="loading" :data="tableData" stripe>
      <el-table-column type="expand" width="20">
        <template #default="scope">
          <el-divider content-position="left">源证书</el-divider>
          <el-table :data="scope.row.originCertList" stripe size="small">
            <el-table-column prop="certConfigName" label="配置名称" min-width="100" show-overflow-tooltip align="center" />
            <el-table-column label="赛事名称" prop="name" min-width="180" show-overflow-tooltip>
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
            <el-table-column prop="competitionStageName" label="阶段" min-width="100" show-overflow-tooltip
              align="center" />
            <el-table-column prop="awardsName" label="奖项类型" width="100" show-overflow-tooltip align="center">
              <template #default="{ row }">
                {{awards_name.find(item => item.value === row.awardsName)?.label ?? '-'}}
              </template>
            </el-table-column>
            <el-table-column prop="ownYear" label="拥有年限" width="120" align="center" />
            <el-table-column prop="originCertScore" :label="`源证书分值`" width="100" align="center" />
          </el-table>
          <el-divider content-position="left">目标证书</el-divider>
          <el-table
            :data="(scope.row?.targetCertList && scope.row?.targetCertList.length > 0) ? scope.row?.targetCertList : scope.row?.detailList"
            stripe size="small">
            <el-table-column prop="certConfigName" label="配置名称" min-width="100" show-overflow-tooltip align="center" />
            <el-table-column label="赛事名称" prop="name" min-width="180" show-overflow-tooltip>
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
            <el-table-column prop="competitionStageName" label="阶段" min-width="100" show-overflow-tooltip
              align="center" />
            <el-table-column prop="awardsName" label="奖项类型" width="100" show-overflow-tooltip align="center">
              <template #default="{ row }">
                {{awards_name.find(item => item.value === row.awardsName)?.label ?? '-'}}
              </template>
            </el-table-column>
            <el-table-column prop="targetCertScore" :label="`目标证书分值`" width="130" align="center" />
          </el-table>
        </template>
      </el-table-column>
      <el-table-column label="排序" width="100" align="center">
        <template #default="{ row, $index }">
          <span v-if="!row.edit" style="display: flex; align-items: center; justify-content: center">
            <span>{{ row.sort }}</span>
            <el-icon style="margin-left: 5px;cursor: pointer;" @click="row.edit = true">
              <EditPen />
            </el-icon>
          </span>
          <el-input-number v-else min="0" step="1" v-model="row.sort" placeholder="序号" clearable :controls="false" style="width: 60px"
            @blur="handleSort(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="rulerName" label="赛证互通规则名称" min-width="220" show-overflow-tooltip align="center" />
      <el-table-column prop="certConditions" label="源证书兑换关系" min-width="120" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{cert_conditions.find(dict => dict.value === row.certConditions)?.label ?? '-'}}
        </template>
      </el-table-column>
      <el-table-column label="是否首页显示" align="center" width="150">
        <template #default="{ row }">
          <el-switch :model-value="row.isTope" active-value="1" inactive-value="0"
            @change="val => handleSwitchTop(row, val)" inline-prompt active-text="是" inactive-text="否"
            style="--el-switch-off-color: #E6A23C" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" icon="View" @click="handleAction('view', row)"
            v-hasPermi="['competition:competitionCertExchangeRule:query']">
            查看
          </el-button>
          <el-button size="small" link type="success" icon="Edit" @click="handleAction('edit', row)"
            v-hasPermi="['competition:competitionCertExchangeRule:edit']">编辑</el-button>
          <el-button size="small" link type="primary" icon="Promotion" @click="handleAction('issue', row)"
            v-hasPermi="['competition:competitionCertExchangeRule:issue']" v-if="row.rulerStatus === '0'">发布</el-button>
          <el-button size="small" link type="warning" icon="RefreshLeft" @click="handleAction('backOut', row)"
            v-hasPermi="['competition:competitionCertExchangeRule:backOut']" v-if="row.rulerStatus === '1'">撤销</el-button>
          <el-button size="small" link type="danger" icon="Delete" @click="deleteRow(row)"
            v-hasPermi="['competition:competitionCertExchangeRule:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <Setting v-model="open" :readonly="true" :data="ruleData" />
  </div>
</template>

<script setup>
  // ******* 组件 ********
  import Setting from './config/setting.vue'
  import SelectCompetitionBack from "@/views/certInterconnect/components/SelectCompetitionBack.vue"
  // ******* 插件 ********
  const { proxy } = getCurrentInstance()
  const router = useRouter()

  // ******* API ********
  import { getCertExchangeRuleList, getCertExchangeRuleInfo, updateCertExchangeRuleInfo, deleteCertExchangeRule } from "@/api/certInterconnect/interconnectConfig.js"

  // ******** 数据字典 ********
  const {
    cert_conditions,
    awards_name,
  } = proxy.useDict(
    "cert_conditions",
    "awards_name",
  );

  // ******** 初始化 ********
  const queryParams = ref({
    pageNum: 1,
    pageSize: 10,
    originCertName: '',
    targetCertName: '',
    conditionRemark: ''
  })
  const tableData = ref([]); // table数据
  const total = ref(0) // 总数
  const open = ref(false); // 弹窗
  const loading = ref(true); // 加载状态
  const showSearch = ref(true); // 显示搜索条件
  const querySelectCompetitionRef = ref() // 搜索条件赛事选择组件
  const ruleData = ref({}) // 规则信息
  // 操作类型
  const ACTION = {
    VIEW: 'view',
    ADD: 'add',
    EDIT: 'edit',
    ISSUE: 'issue',
    BACK_OUT: 'backOut'
  }

  const getList = async () => {
    try {
      loading.value = true;
      const { rows, code, total: count } = await getCertExchangeRuleList(queryParams.value)
      if (code === 200) {
        tableData.value = rows.map(row => {
          return {
            isTope: row.isTope ?? '0',
            ...row
          }
        });
        total.value = count
        loading.value = false;
      }
    } catch (error) {
      proxy.$modal.msgWarning(error?.message ?? '操作失败');
    }
  }


  // 搜索
  const handleQuery = () => {
    queryParams.value.pageNum = 1
    getList()
  }
  // 重置
  const resetQuery = () => {
    proxy.resetForm("queryRef");
    if (querySelectCompetitionRef.value) {
      querySelectCompetitionRef.value.reset();
      setTimeout(() => handleQuery())
    }

  }

  // 新增或者编辑互通规则
  const handleAction = async (action, row) => {
    // 非法动作直接返回
    if (!Object.values(ACTION).includes(action)) return
    try {
      // 查看 需要请求接口
      if (action === ACTION.VIEW) {
        const { data, code } = await getCertExchangeRuleInfo(row.ruleId)
        if (code !== 200) throw new Error('查询配置信息失败')
        ruleData.value = data;
        open.value = true
      } else if (action === ACTION.ISSUE || action === ACTION.BACK_OUT) {
        handleIssueOrBackOut(action === 'issue' ? '' : '撤销', row)
      } else {
        router.push({
          name: "actionInterconnectConfig",
          query: { ruleId: row?.ruleId }
        });
      }
    } catch (error) {
      console.log(error);
    }
  }

  // 发布 与 撤销
  const handleIssueOrBackOut = async (txt, row) => {
    await proxy.$confirm(`是否确认${txt}发布该数据？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await updateCertExchangeRuleInfo({
        ruleId: row.ruleId,
        rulerStatus: row.rulerStatus === '0' ? '1' : '0'
      })
    }).then(() => {
      proxy.$message.success(`发布成功`);
      getList();
    }).catch();
  }

  // 排序
  const handleSort = async row => {
    try {
      row.edit = false
      await updateCertExchangeRuleInfo({ ruleId: row.ruleId, sort: row.sort })
      handleQuery()
    } catch (error) {
      console.log(error);
    }
  }

  // 是否置顶
  const handleSwitchTop = async (row, isTope) => {
    try {
      // 关键：必须先确认，再改变状态
      const statusTxt = isTope === '1' ? '首页显示' : '取消首页显示';
      await proxy.$confirm(`是否首页显示改数据？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await updateCertExchangeRuleInfo({
          ruleId: row.ruleId,
          isTope
        })
      }).then(() => {
        proxy.$message.success(`${statusTxt}成功`);
        getList();
      }).catch(() => {
        row.isTope = isTope === '0' ? '1' : '0';
      });
    } catch (error) {
      // 用户取消 或 报错 → 还原 switch 状态
      row.isTope = isTope === '0' ? '1' : '0';
    }
  };

  // 删除
  const deleteRow = row => {
    proxy
      .$confirm('确认删除该条目？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
      .then(async () => {
        return await deleteCertExchangeRule(row.ruleId)
      }).then(() => {
        proxy.$message.success('删除成功')
        handleQuery()
      }).catch()
  }


  onMounted(() => {
    getList()
  })
</script>

<style scoped></style>
