<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
      <el-form-item label="流程编号" prop="traceabilityCode">
        <el-input style="width: 240px" v-model="queryParams.traceabilityCode" placeholder="请输入流程编号" clearable :maxlength="500"
          @change="handleQuery" />
      </el-form-item>
      <el-form-item label="紧急程度" prop="urgentLevel">
        <el-select style="width: 240px" v-model="queryParams.urgentLevel" clearable placeholder="请选择紧急程度"
          @change="handleQuery">
          <el-option v-for="item in urgent_level" :key="item.value" :label="item.label" :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="流程类型" prop="issueType">
        <el-select style="width: 240px" v-model="queryParams.issueType" clearable placeholder="请选择流程类型"
          @change="handleQuery">
          <el-option v-for="item in issue_type" :key="item.value" :label="item.label" :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
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
          v-hasPermi="['workflow:process:export']"
          @click="handleExport"
        >导出</el-button>
      </el-col>
    </el-row> -->

    <el-table v-loading="loading" :data="copyList" border stripe header-cell-class-name="headerCellClassName">
      <el-table-column label="序号" type="index" width="55" align="center" fixed="left" />
      <!-- <el-table-column label="流程名称" align="center" prop="processName" :show-overflow-tooltip="true" /> -->
      <el-table-column label="流程编号" align="center" width="300" prop="traceabilityCode" show-overflow-tooltip />
      <el-table-column label="流程类型" align="center">
        <template #default="scope">
          <dict-tag :options="issue_type" :value="scope.row.issueType" />
        </template>
      </el-table-column>
      <el-table-column label="流程状态" align="center" width="100">
        <template #default="scope">
          <dict-tag :options="wf_process_status" :value="scope.row.processStatus" />
        </template>
      </el-table-column>
      <el-table-column label="紧急程度" align="center" width="100">
        <template #default="scope">
          <dict-tag :options="urgent_level" :value="scope.row.urgentLevel" />
        </template>
      </el-table-column>
      <el-table-column label="发起人" align="center" prop="startUserName" />
      <el-table-column label="抄送人" align="center" prop="originatorName" el-table-column />
      <el-table-column label="创建时间" align="center" prop="createTime" el-table-column>
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right">
        <template #default="scope">
          <el-button type="text" icon="el-icon-tickets" @click="handleFlowRecord(scope.row)"
            v-hasPermi="['workflow:process:copy']">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

  </div>
</template>

<script setup name="Copy">
import { listCopyProcess, getStartUserList } from "@/api/workflow/process"
import { download } from '@/utils/request';

const { proxy } = getCurrentInstance();

const route = useRoute();
const router = useRouter();
const { wf_process_status } = proxy.useDict("wf_process_status");
// 流程类型issue_typ
const { issue_type } = proxy.useDict("issue_type");
// 紧急程度urgent_level
const { urgent_level } = proxy.useDict("urgent_level");
const loading = ref(true) // 遮罩层
const total = ref(0) // 总条数
const copyList = ref([]) // 流程抄送表格数据


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

/** 查询流程抄送列表 */
function getList() {
  loading.value = true;
  listCopyProcess(queryParams.value).then(response => {
    copyList.value = response.rows;
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
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 查看详情 */
function handleFlowRecord(row) {
  router.push({
    path: '/workflow/process/detail/' + row.instanceId,
    query: {
      processed: false
    }
  })
}

/** 导出按钮操作 */
function handleExport() {
  download('workflow/process/copyExport', {
    ...queryParams.value
  }, `wf_copy_process_${new Date().getTime()}.xlsx`)
}

getList();

</script>
