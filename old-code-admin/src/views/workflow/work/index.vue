<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
      <!-- <el-form-item label="流程标识" prop="processKey">
        <el-input
          v-model="queryParams.processKey"
          placeholder="请输入流程标识"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item> -->
      <el-form-item label="流程名称" prop="processName">
        <el-input
          v-model="queryParams.processName"
          placeholder="请输入流程名称"
          clearable
          @keyup.enter="handleQuery"
          :maxlength="500"
        />
      </el-form-item>
      <el-form-item label="流程分类" prop="category">
        <el-select v-model="queryParams.category" clearable placeholder="请选择" @change="handleQuery">
          <el-option
            v-for="item in categoryOptions"
            :key="item.categoryId"
            :label="item.categoryName"
            :value="item.code">
          </el-option>
        </el-select>
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
          v-hasPermi="['workflow:process:startExport']"
          @click="handleExport"
        >导出</el-button>
      </el-col>
    </el-row> -->
    <el-table
      v-loading="loading"
      :data="processList"
      border
      stripe
      header-cell-class-name="headerCellClassName">
      <el-table-column label="序号" type="index" align="center" width="50"></el-table-column>
      <!-- <el-table-column label="流程标识" align="center" prop="processKey" :show-overflow-tooltip="true" /> -->
      <el-table-column label="流程名称" align="center" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-button type="text" @click="handleProcessView(scope.row)">
            <span>{{ scope.row.processName }}</span>
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="流程分类" align="center" prop="categoryName" :formatter="(v) => categoryFormat(v)" />
      <el-table-column label="流程版本" align="center">
        <template #default="scope">
          <el-tag>v{{ scope.row.version }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center">
        <template #default="scope">
          <el-tag type="success" v-if="!scope.row.suspended">激活</el-tag>
          <el-tag type="warning" v-if="scope.row.suspended">挂起</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="部署时间" align="center" prop="deploymentTime" width="180"  show-overflow-tooltip>
        <template #default="scope">
          {{ parseTime(scope.row.deploymentTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            type="text"
            icon="el-icon-video-play"
            @click="handleStart(scope.row)"
            v-hasPermi="['workflow:process:start']"
          >发起</el-button>
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

    <!-- 流程图 -->
    <el-dialog
      :title="processView.title"
      v-model="processView.open"
      width="70%"
      append-to-body
      destroy-on-close
      :before-close="handleCloseViewer">
      <process-viewer :key="`designer-${processView.index}`" :xml="processView.xmlData" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from "vue-router";
import { listProcess, getBpmnXml } from "@/api/workflow/process";
import { listAllCategory } from '@/api/workflow/category';
import ProcessViewer from '@/components/ProcessViewer';
import { download } from '@/utils/request';

const { proxy } = getCurrentInstance();

const route = useRoute();
const router = useRouter();

const loading = ref(true) // 遮罩层

const total = ref(0) // 总条数
const categoryOptions = ref([])
const processList = ref([]) // 流程定义表格数据

// 查询参数
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    processKey: undefined,
    processName: undefined,
    category: undefined
  },
  processView: {
    title: '',
    open: false,
    index: undefined,
    xmlData:"",
  }
})

const { queryParams, processView } = toRefs(data);

/** 查询流程分类列表 */
function getCategoryList() {
  listAllCategory().then(response => categoryOptions.value = response.data)
}

/** 查询流程定义列表 */
function getList() {
  loading.value = true;
  listProcess(queryParams.value).then(response => {
    processList.value = response.rows;
    total.value = response.total;
    loading.value = false
  })
}

// 查询按钮操作
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

// 重置按钮操作
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 查看流程图 */
function handleProcessView(row) {
  let definitionId = row.definitionId;
  processView.value.title = "流程图";
  processView.value.index = definitionId;
  // 发送请求，获取xml
  getBpmnXml(definitionId).then(res => {
    processView.value.xmlData = res.msg;
    processView.value.open = true;
  })
}

/** 关闭流程图 */
function handleCloseViewer(done) {
  done();
  processView.value.open = false;
}

function handleStart(row) {
  router.push({
    path: '/workflow/process/start/' + row.deploymentId,
    query: {
      definitionId: row.definitionId,
    }
  })
}

/** 导出按钮操作 */
function handleExport() {
  download('workflow/process/startExport', {
    ...queryParams.value
  }, `wf_start_process_${new Date().getTime()}.xlsx`)
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
getCategoryList();

</script>

<style scoped>

</style>
