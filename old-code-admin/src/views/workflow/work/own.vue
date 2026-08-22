<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
      <!-- <el-form-item label="流程名称" prop="processName">
        <el-input style="width: 240px" v-model="queryParams.processName" placeholder="请输入流程名称" clearable
          @keyup.enter="handleQuery" />
      </el-form-item> -->
      <el-form-item label="流程编号" prop="traceabilityCode">
        <el-input style="width: 240px" v-model="queryParams.traceabilityCode" placeholder="请输入流程编号" clearable :maxlength="500"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="流程状态" prop="state">
        <el-select style="width: 240px" v-model="queryParams.state" clearable placeholder="请选择流程状态"
          @change="handleQuery">
          <el-option v-for="item in wf_process_status" :key="item.value" :label="item.label" :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
         <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
                  <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="ownProcessList" border stripe header-cell-class-name="headerCellClassName">
      <el-table-column label="序号" type="index" width="55" align="center" fixed="left" />
      <!-- <el-table-column label="流程名称" align="center" prop="procDefName" width="150" :show-overflow-tooltip="true" /> -->
      <el-table-column label="流程编号" align="center" prop="traceabilityCode" show-overflow-tooltip width="300"/>
      <el-table-column label="流程状态" align="center" width="100">
        <template #default="scope">
          <dict-tag :options="wf_process_status" :value="scope.row.processStatus" />
        </template>
      </el-table-column>
      

      <el-table-column label="当前节点" align="center" prop="taskName" show-overflow-tooltip />

      <el-table-column label="当前节点审核人" align="center" prop="assigneeName" width="120" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.assigneeName">{{ row.assigneeName }}</span>
          <el-tag v-else type="warning">无</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="流程发起人" align="center" prop="startUserName" show-overflow-tooltip />
      <el-table-column label="提交时间" align="center" prop="createTime" width="180"  show-overflow-tooltip />
      <el-table-column label="操作" width="150"  align="center" fixed="right">
        <template #default="scope">
          <el-button v-if="statu.indexOf(scope.row.processStatus)!==-1" type="text"
            @click="handleHasten(scope.row)">催办</el-button>
          <el-button type="text" @click="handleFlowRecord(scope.row)"
            v-hasPermi="['workflow:process:query']">查看</el-button>
 
          <el-button v-if="scope.row.processStatus == 'waiting' && scope.row.cancelFlag" type="text"
            @click="handleStop(scope.row)" v-hasPermi="['workflow:process:cancel']">撤销</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

  </div>
</template>

<script setup name="Owns">
import { listOwnProcess, stopProcess, delProcess, hastenProcess } from '@/api/workflow/process';
import { listAllCategory } from '@/api/workflow/category';
import { ElMessage, ElMessageBox } from "element-plus";
import { download } from '@/utils/request'
const statu=["running", "waiting"]
const { proxy } = getCurrentInstance();
const { wf_process_status } = proxy.useDict("wf_process_status");

const route = useRoute();
const router = useRouter();

const loading = ref(true) // 遮罩层
const total = ref(0) // 总条数
const categoryOptions = ref([])
const ownProcessList = ref([]) // 我发起的流程列表数据
const dateRange = ref([]) // 日期范围

const queryFormRef = ref(null)
// 查询参数
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    traceabilityCode: null,
    state: null,
    urgentLevel: null,
    issueType: null,
  }
})

const { queryParams } = toRefs(data);

/** 查询流程定义列表 */
function getList() {
  loading.value = true;
  listOwnProcess(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    ownProcessList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 查询流程分类列表 */
function getCategoryList() {
  listAllCategory().then(response => {
    categoryOptions.value = response.data
  })
}

/** 查询按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryFormRef");

  handleQuery();
}

function handleAgain(row) {
  router.push({
    path: '/workflow/process/start/' + row.deployId,
    query: {
      definitionId: row.procDefId,
      procInsId: row.procInsId
    }
  })
}

/**  取消流程申请 */
function handleStop(row) {
  const params = {
    procInsId: row.procInsId
  }
  stopProcess(params).then(res => {
    ElMessage.success(res.msg);
    getList();
  });
}

/** 流程流转记录 */
function handleFlowRecord(row) {
  router.push({
    path: '/wentiflow/process/detail/' + row.procInsId,
    query: {
      processed: false
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const ids = row.procInsId || ids.value;
  ElMessageBox.confirm('是否确认删除流程定义编号为"' + ids + '"的数据项?', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(function () {
    return delProcess(ids);
  }).then(() => {
    getList();
    ElMessage.success("删除成功");
  })
}

/** 催办 */
function handleHasten(row) {
  proxy.$modal.confirm('是否确认发起催办？').then(function () {
    return hastenProcess({
      assigneeId: row.assigneeIds,
      procInsId: row.procInsId,
      taskDefKey: row.taskDefKey
    });
  }).then(() => {
    proxy.$modal.msgSuccess("催办成功");
  }).catch(() => { });
}

/** 导出按钮操作 */
function handleExport() {
  download('workflow/process/ownExport', {
    ...queryParams.value
  }, `wf_own_process_${new Date().getTime()}.xlsx`)
}

function categoryFormat(row, column) {
  let obj = categoryOptions.value.find(item => {
    return item.code === row.category
  });
  if (obj) {
    return obj.categoryName;
  }
}

getList();
getCategoryList()
</script>
