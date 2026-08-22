<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
      <el-form-item label="流程标识" prop="processKey">
        <el-input
          v-model.trim="queryParams.processKey"
          placeholder="请输入流程标识"
          clearable
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="流程名称" prop="processName">
        <el-input
          v-model.trim="queryParams.processName"
          placeholder="请输入流程名称"
          clearable
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="流程分类" prop="category">
        <el-select v-model="queryParams.category" clearable placeholder="请选择流程分类"   @change="handleQuery" style="width: 200px;">
          <el-option
            v-for="item in categoryOptions"
            :key="item.categoryId"
            :label="item.categoryName"
            :value="item.code">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="state">
        <el-select v-model="queryParams.state" clearable placeholder="请选择状态"    @change="handleQuery" style="width: 200px;">
          <el-option :key="1" label="激活" value="active" />
          <el-option :key="2" label="挂起" value="suspended" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['workflow:deploy:remove']"
        >删除</el-button>
      </el-col>
    </el-row>

    <el-table 
      v-loading="loading"
      :data="deployList" 
      @selection-change="handleSelectionChange"
      border
      stripe
      header-cell-class-name="headerCellClassName">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="流程标识" align="center" prop="processKey" :show-overflow-tooltip="true" />
        <el-table-column label="流程名称" align="center" :show-overflow-tooltip="true">
          <template #default="{ row }">
            <el-button type="text" @click="handleProcessView(row)">
              <span>{{ row.processName }}</span>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="流程分类" align="center" prop="categoryName" :formatter="(v) => categoryFormat(v)" />
        <el-table-column label="流程版本" align="center">
          <template #default="scope">
            <el-tag >v{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center">
          <template #default="scope">
            <el-tag type="success" v-if="!scope.row.suspended">激活</el-tag>
            <el-tag type="warning" v-if="scope.row.suspended">挂起</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否上线" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              active-value="1"
              inactive-value="0"
              @change="handleStatusChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="部署时间" align="center" prop="deploymentTime" width="160">
          <template #default="scope">
            {{ parseTime(scope.row.deploymentTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right">
          <template #default="scope">
            <el-button
              type="text"
              @click="handlePublish(scope.row)"
              v-hasPermi="['workflow:deploy:history']"
            >版本管理</el-button>
            <el-button
              type="text"
              @click="handleDelete(scope.row)"
              v-hasPermi="['workflow:deploy:remove']"
            >删除</el-button>
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

    <!-- 版本管理 -->
    <el-dialog title="版本管理" v-model="publish.open" width="50%" append-to-body>
      <el-table v-loading="publish.loading" :data="publish.dataList">
        <el-table-column label="流程标识" align="center" prop="processKey" :show-overflow-tooltip="true" />
        <el-table-column label="流程名称" align="center" :show-overflow-tooltip="true">
          <template #default="{ row }">
            <el-button type="text" @click="handleProcessView(row)">
              <span>{{ row.processName }}</span>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="流程版本" align="center">
          <template #default="{ row }">
            <el-tag size="medium" >v{{ row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center">
          <template #default="{ row }">
            <el-tag :type="row.suspended ? 'warning' : 'success'">{{ row.suspended ? '挂起' : '激活' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button
              type="text"
              icon="el-icon-video-pause"
              v-if="!row.suspended"
              @click="handleChangeState(row, 'suspended')"
              v-hasPermi="['workflow:deploy:status']"
            >挂起</el-button>
            <el-button
              type="text"
              icon="el-icon-video-play"
              v-if="row.suspended"
              @click="handleChangeState(row, 'active')"
              v-hasPermi="['workflow:deploy:status']"
            >激活</el-button>
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(row)"
              v-hasPermi="['workflow:deploy:remove']"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="publishTotal > 0"
        :total="publishTotal"
        v-model:page="publishQueryParams.pageNum"
        v-model:limit="publishQueryParams.pageSize"
        @pagination="getPublishList"
      />
    </el-dialog>

  </div>
</template>

<script setup name="Deploy">
import { ref, onMounted } from 'vue'
import { listAllCategory } from '@/api/workflow/category'
import { listDeploy, listPublish, getBpmnXml, changeState, delDeploy, makeItOnline } from '@/api/workflow/deploy'
import ProcessViewer from '@/components/ProcessViewer'
import { download } from '@/utils/request'
import { ElMessage, ElMessageBox } from "element-plus";

const { proxy } = getCurrentInstance();

// 遮罩层
const loading = ref(true)

// 选中数组
const ids = ref([])

// 非单个禁用
const single = ref(true)

// 非多个禁用
const multiple = ref(true)

// 总条数
const total = ref(0)

// 查询参数
const queryForm = ref(null)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  processKey: null,
  processName: null,
  category: null,
  state: null
})

const deployList = ref([])

const categoryOptions = ref([])

const processView = reactive({
  title: '',
  open: false,
  index: undefined,
  xmlData:"",
})

const publish = reactive({
  open: false,
  loading: false,
  dataList: [],
})

const publishTotal = ref(0)

const publishQueryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  processKey: "",
})

/** 查询流程分类列表 */
function getCategoryList() {
  listAllCategory().then(response => categoryOptions.value = response.data);
}

getCategoryList();

/** 查询流程部署列表 */
function getList() {
  loading.value = true;
  listDeploy(queryParams).then(response => {
    deployList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

getList();

/** 查询按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryForm");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.deploymentId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 查看流程图 */
function handleProcessView(row) {
  let definitionId = row.definitionId;
  processView.title = "流程图";
  processView.index = definitionId;
  // 发送请求，获取xml
  getBpmnXml(definitionId).then(response => {
    processView.xmlData = response.msg;
    processView.open = true;
  })
}

/** 关闭流程图 */
function handleCloseViewer(done) {
  done();
  processView.open = false;
}

function getPublishList() {
  publish.loading = true;
  listPublish(publishQueryParams).then(response => {
    publish.dataList = response.rows;
    publishTotal.value = response.total;
    publish.loading = false;
  })
}

function handlePublish(row) {
  publishQueryParams.processKey = row.processKey;
  publish.open = true;
  getPublishList();
}

/** 挂起/激活流程 */
function handleChangeState(row, state) {
  const params = {
    definitionId: row.definitionId,
    state: state
  }
  changeState(params).then(res => {
    ElMessage.success(res.msg)
    getPublishList();
    getList();
  });
}

function handleDelete(row) {
  const deploymentIds = row.deploymentId || ids.value;
  ElMessageBox.confirm('是否确认删除选中的数据项？').then(() => {
    loading.value = true;
    return delDeploy(deploymentIds);
  }).then(() => {
    loading.value = false;
    getList();
    getPublishList();
    ElMessage.success("删除成功");
  }).finally(() => {
    loading.value = false;
  });
}

/** 状态编辑  */
function handleStatusChange(row) {
  let text = row.status === "0" ? "未上线" : "已上线";
  proxy.$modal.confirm(`确认要将此流程改为【${text}】吗?`).then(function () {
    return makeItOnline(row.definitionId, row.status,row.category);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("操作成功");
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0";
  });
}

function categoryFormat(row, column) {
  let obj = categoryOptions.value.find(item => {
    return item.code === row.category
  });
  if (obj) {
    return obj.categoryName;
  }
}
</script>

<style scoped>

</style>
