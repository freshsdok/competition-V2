<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="流程编号" prop="traceabilityCode">
        <el-input
          style="width: 240px"
          v-model="queryParams.traceabilityCode"
          placeholder="请输入流程编号"
          clearable
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="发起人" prop="startUserId">
        <el-input
          style="width: 240px"
          v-model.trim="queryParams.startUserId"
          placeholder="请输入流程发起人全称"
          clearable
          @change="handleQuery"
        />
      </el-form-item>

      <el-form-item
        label="流程状态"
        prop="state"
        v-if="activeName == 'finish'"
      >
        <el-select
          style="width: 240px"
          v-model="queryParams.state"
          clearable
          placeholder="请选择流程状态"
          @change="handleQuery"
        >
          <el-option
            v-for="item in filtered_wf_process_status"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item
        label="流程状态"
        prop="state"
        v-if="activeName == 'running'"
      >
        <el-select
          style="width: 240px"
          v-model="queryParams.state"
          clearable
          placeholder="请选择流程状态"
          @change="handleQuery"
        >
          <el-option
            v-for="item in runningprocess_status"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
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
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery"
          >查询</el-button
        >
        <el-button icon="el-icon-refresh" @click="resetQuery"
          >重置</el-button
        >
      </el-form-item>
    </el-form>
    <el-tabs
      v-model="activeName"
      class="demo-tabs"
      @tab-change="handleTabClick"
    >
      <el-tab-pane label="进行中" name="running"></el-tab-pane>
      <el-tab-pane label="已完成" name="finish"></el-tab-pane>
    </el-tabs>
    <el-table
      v-loading="loading"
      :data="list"
      border
      stripe
      header-cell-class-name="headerCellClassName"
    >
      <el-table-column
        label="序号"
        type="index"
        width="55"
        align="center"
        fixed="left"
      />
      <el-table-column
        label="流程名称"
        align="center"
        prop="procDefName"
        min-width="200"
        :show-overflow-tooltip="true"
      />

      <el-table-column
        label="流程编号"
        align="center"
        prop="traceabilityCode"
        width="300"
        show-overflow-tooltip
      />
      <el-table-column
        label="流程发起人"
        align="center"
        prop="startUserName"
        width="100"
        show-overflow-tooltip
      />
       <el-table-column label="操作类型" align="center" width="100">
        <template #default="scope">
          <dict-tag
            :options="change_type"
            :value="scope.row.operationType"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="提交时间"
        align="center"
        prop="createTime"
        width="180"
      />
      <el-table-column label="流程状态" align="center" width="100">
        <template #default="scope">
          <dict-tag
            :options="wf_process_status"
            :value="scope.row.processStatus"
          />
        </template>
      </el-table-column>
      <el-table-column label="实例状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag type="success" v-if="!row.suspended">激活</el-tag>
          <el-tag type="warning" v-if="row.suspended">挂起</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="耗时"
        align="center"
        prop="duration"
        width="180"
      />
      <!-- <el-table-column label="实例编号" align="left" prop="procInsId" width="300" show-overflow-tooltip /> -->
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button
            type="text"
            @click="handleFlowRecord(scope.row)"
            v-hasPermi="['workflow:control:query']"
            >查看</el-button
          >
          <template v-if="activeName == 'running'">
            <el-button
              v-if="!scope.row.suspended"
              type="text"
              @click="handleSuspend(scope.row)"
              v-hasPermi="['workflow:control:status']"
              >挂起</el-button
            >
            <el-button
              v-if="scope.row.suspended"
              type="text"
              @click="handleActivate(scope.row)"
              v-hasPermi="['workflow:control:status']"
              >激活</el-button
            >
            <el-button
              v-if="!scope.row.suspended"
              type="text"
              @click="handleReject(scope.row)"
              v-hasPermi="['workflow:control:reject']"
              >终止</el-button
            >
          </template>
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

<script setup name="Control">
import {
  listRunning,
  listFinish,
  flowSuspend,
  flowActivate,
  stopTask,
} from "@/api/workflow/control";
import { listAllCategory } from "@/api/workflow/category";
import { download } from "@/utils/request";
import { getStartUserList } from "@/api/workflow/process";

const { proxy } = getCurrentInstance();
const { wf_process_status,change_type } = proxy.useDict("wf_process_status","change_type");
console.log(wf_process_status);
// 创建一个计算属性，过滤掉 label == '进行中' 的项
const filtered_wf_process_status = computed(() => {
  return wf_process_status.value.filter(
    (item) => item.value !== "waiting" && item.value !== "running"
  );
});

/** 节点单击事件 */
function handleNodeClick(data) {
   queryParams.value.startDeptId = data.id;
   handleQuery();
};
const runningprocess_status = computed(() => {
  return wf_process_status.value.filter(
    (item) => item.value == "waiting" || item.value == "running"
  );
});


const route = useRoute();
const router = useRouter();

const loading = ref(true); // 遮罩层
const activeName = ref("running"); // 选中的tab
const total = ref(0); // 总条数
const categoryOptions = ref([]);
const list = ref([]); // 我发起的流程列表数据
const dateRange = ref([]); // 日期范围

const queryFormRef = ref(null);
// 查询参数
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    traceabilityCode: null,
    urgentLevel: null,
    issueType: null,
    startUserId: null,
    state: null,
  },
  form: {},
  rules: {},
});

const { queryParams, form, rules } = toRefs(data);

/** 查询流程定义列表 */
function getList() {
  loading.value = true;
  if (activeName.value == "running") {
    listRunning(proxy.addDateRange(queryParams.value, dateRange.value)).then(
      (response) => {
        list.value = response.rows;
        total.value = response.total;
        loading.value = false;
      }
    );
  } else {
    listFinish(proxy.addDateRange(queryParams.value, dateRange.value)).then(
      (response) => {
        list.value = response.rows;
        total.value = response.total;
        loading.value = false;
      }
    );
  }
}

/** 查询流程分类列表 */
function getCategoryList() {
  listAllCategory().then((response) => {
    categoryOptions.value = response.data;
  });
}

/** tab切换 */
function handleTabClick() {
  resetQuery();
}

/** 查询按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  queryParams.value.startDeptId=null
  proxy.resetForm("queryFormRef");
  handleQuery();
}

/** 流程流转记录 */
function handleFlowRecord(row) {
  router.push({
    path: "/workflow/process/detail/" + row.procInsId,
    query: {
      processed: false,
      administrator: row.processStatus == "running" ? true : "",
    },
  });
}

/** 流程挂起 */
function handleSuspend(row) {
  flowSuspend(row.procInsId).then((res) => {
    proxy.$modal.msgSuccess(res.msg);
    getList();
  });
}

/** 流程激活 */
function handleActivate(row) {
  flowActivate(row.procInsId).then((res) => {
    proxy.$modal.msgSuccess(res.msg);
    getList();
  });
}

/** 流程终止 */
function handleReject(row) {
  proxy.$modal
    .confirm("是否确认终止该流程")
    .then(function () {
      return stopTask({
        taskId: row.taskId,
        procInsId: row.procInsId,
        comment: "管理员已终止该流程",
      });
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("操作成功");
    })
    .catch(() => {});
}
/** 导出按钮操作 */
function handleExport() {
  download(
    "workflow/process/ownExport",
    {
      ...queryParams.value,
    },
    `wf_own_process_${new Date().getTime()}.xlsx`
  );
}

function categoryFormat(row, column) {
  let obj = categoryOptions.value.find((item) => {
    return item.code === row.category;
  });
  if (obj) {
    return obj.categoryName;
  }
}

getList();
getCategoryList();
</script>
