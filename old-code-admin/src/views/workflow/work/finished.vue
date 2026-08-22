<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
      <el-form-item label="流程编号" prop="traceabilityCode">
        <el-input style="width: 240px" v-model.trim="queryParams.traceabilityCode" placeholder="请输入流程编号" clearable :maxlength="500"
          @change="handleQuery" />
      </el-form-item>
      <el-form-item label="操作类型"
                    prop="operationType">
        <el-select
          style="width: 240px"
          v-model="queryParams.operationType"
          clearable
          placeholder="请选择操作类型"
          @change="handleQuery"
        >
        <el-option label="报名团队人员变更" value="change"></el-option>
        <el-option label="退赛" value="retired"></el-option>
        <el-option label="退费重缴费" value="repayment"></el-option>
        </el-select>
      </el-form-item>
      <!-- <el-form-item label="审批时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item> -->
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          v-hasPermi="['workflow:process:finishedExport']"
          @click="handleExport"
        >导出</el-button>
      </el-col>
    </el-row> -->

    <el-table v-loading="loading" :data="finishedList" border stripe header-cell-class-name="headerCellClassName">
      <el-table-column label="序号" type="index" width="55" align="center" fixed="left" />

      <!-- <el-table-column label="流程名称" align="center" prop="procDefName" width="200" :show-overflow-tooltip="true"/> -->
      <el-table-column label="流程编号" align="center" prop="traceabilityCode" show-overflow-tooltip min-width="180"/>
      <el-table-column label="流程状态" align="center" width="80">
        <template #default="scope">
          <dict-tag :options="wf_process_status" :value="scope.row.processStatus" />
        </template>
      </el-table-column>
      <el-table-column label="任务节点" align="center" prop="taskName" min-width="100" />
      <el-table-column label="流程发起人" align="center" prop="startUserName" width="100" show-overflow-tooltip />
       <el-table-column label="操作类型" align="center" min-width="160">
        <template #default="scope">
          <dict-tag
            :options="change_type"
            :value="scope.row.operationType"
          />
        </template>
      </el-table-column>
      <el-table-column label="接收时间" align="center" prop="createTime" width="160" show-overflow-tooltip />
      <el-table-column label="审批时间" align="center" prop="finishTime" width="160" show-overflow-tooltip />
      <el-table-column label="耗时" align="center" prop="duration" min-width="100" />
      <el-table-column label="操作" align="center" width="90" >
        <template #default="scope">
          <el-button type="text" @click="handleFlowRecord(scope.row)"
            v-hasPermi="['workflow:process:finished']">流转记录</el-button>
          <!-- <el-button
            type="text"
            @click="handleRevoke(scope.row)"
            v-hasPermi="['workflow:process:revoke']"
          >撤回
          </el-button> -->
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

  </div>
</template>

<script setup name="Finished">
import { listFinishedProcess, getStartUserList } from '@/api/workflow/process';
import { revokeProcess } from "@/api/workflow/finished";
import { ElMessage, ElMessageBox } from "element-plus";
import { download } from '@/utils/request';

const { proxy } = getCurrentInstance();

// 流程状态
const { wf_process_status,change_type } = proxy.useDict("wf_process_status","change_type");
const route = useRoute();
const router = useRouter();

const loading = ref(true) // 遮罩层
const total = ref(0) // 总条数
const finishedList = ref([]) // 已办任务列表数据
const dateRange = ref([])


// 查询参数
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    traceabilityCode: null,
    urgentLevel: null,
    issueType: null,
    startUserId: null
  }
})

const { queryParams } = toRefs(data);

/** 查询流程定义列表 */
function getList() {
  loading.value = true;
  listFinishedProcess(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    finishedList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

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

/** 流程流转记录 */
function handleFlowRecord(row) {
  router.push({
    path: '/workflow/process/detail/' + row.procInsId,
    query: {
      processed: false
    }
  })
}

/** 撤回任务 */
function handleRevoke(row) {
  const params = {
    procInsId: row.procInsId,
    taskId: row.taskId
  };
  revokeProcess(params).then(res => {
    ElMessage.success(res.msg);
    getList();
  });
}

/** 导出按钮操作 */
function handleExport() {
  download('workflow/process/finishedExport', {
    ...queryParams.value
  }, `wf_finished_process_${new Date().getTime()}.xlsx`)
}

getList();
</script>
