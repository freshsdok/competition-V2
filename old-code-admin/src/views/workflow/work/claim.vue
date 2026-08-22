<template>
  <div class="app-container">

    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
      <el-form-item label="流程名称" prop="processName">
        <el-input
          style="width: 220px"
          v-model="queryParams.processName"
          placeholder="请输入流程名称"
          clearable
          :maxlength="500"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="接收时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 220px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
           @change="handleQuery"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          v-hasPermi="['workflow:process:claimExport']"
          @click="handleExport"
        >导出</el-button>
      </el-col>
    </el-row> -->

    <el-table
      v-loading="loading"
      :data="claimList"
      border
      stripe
      header-cell-class-name="headerCellClassName">
      <el-table-column label="序号" type="index" width="55" align="center" fixed="left" />
      <el-table-column label="企业名称" align="left" prop="deptNameNew" min-width="200" show-overflow-tooltip fixed="left">
        <template #default="scope">
          {{ scope.row.deptNameNew || scope.row.deptName }}
        </template>
      </el-table-column>
      <el-table-column label="开户银行" align="left" prop="accountName" width="200" show-overflow-tooltip />
      <el-table-column label="流程名称" align="center" prop="procDefName"/>
      <el-table-column label="任务编号" align="center" prop="taskId" :show-overflow-tooltip="true"/>
      <el-table-column label="任务节点" align="center" prop="taskName"/>
      <el-table-column label="流程版本" align="center">
        <template #default="scope">
          <el-tag>v{{scope.row.procDefVersion}}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="流程发起人" align="center" prop="startUserName" width="100" show-overflow-tooltip />
      <el-table-column label="接收时间" align="center" prop="createTime" width="180"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            type="text"
            icon="el-icon-s-claim"
            @click="handleClaim(scope.row)"
            v-hasPermi="['workflow:process:claim']"
          >签收
          </el-button>
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

  </div>
</template>

<script setup name="Claim">
import { listClaimProcess } from '@/api/workflow/process';
import { claimTask } from '@/api/workflow/task';
import { ElMessage, ElMessageBox } from "element-plus";
import { download } from '@/utils/request'

const { proxy } = getCurrentInstance();

const route = useRoute();
const router = useRouter();

const loading = ref(true) // 遮罩层
const total = ref(0) // 总条数
const claimList = ref([]) // 流程待办任务表格数据
const dateRange = ref([]) // 日期范围

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    processName: null
  }
})

// 表单参数
const { queryParams } = toRefs(data);

/** 查询流程定义列表 */
function getList() {
  loading.value = true;
  listClaimProcess(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    claimList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

getList()

/** 查询按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 签收 */
function handleClaim(row) {
  claimTask({taskId: row.taskId}).then(response => {
    ElMessage.success(response.msg);
    router.push({
      path: '/work/todo'
    })
  })
}

/** 导出按钮操作 */
function handleExport() {
  download('workflow/process/claimExport', {
    ...queryParams.value
  }, `wf_claim_process_${new Date().getTime()}.xlsx`)
}
</script>

<style scoped>

</style>
