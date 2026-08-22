<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
      <el-form-item label="模型标识" prop="modelKey">
        <el-input v-model="queryParams.modelKey" placeholder="请输入模型标识" clearable @change="handleQuery"
          :maxlength="500" />
      </el-form-item>
      <el-form-item label="模型名称" prop="modelName">
        <el-input v-model="queryParams.modelName" placeholder="请输入模型名称" clearable @change="handleQuery"
          :maxlength="500" />
      </el-form-item>
      <el-form-item label="流程分类" prop="category">
        <el-select v-model="queryParams.category" clearable placeholder="请选择" @change="handleQuery"
          style="width: 200px;">
          <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName"
            :value="item.code">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button plain type="primary" icon="el-icon-plus" @click="handleAdd"
          v-hasPermi="['workflow:model:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['workflow:model:remove']">删除</el-button>
      </el-col>
      <!-- <el-col :span="1.5">
        <el-button icon="el-icon-download" @click="handleExport" v-hasPermi="['workflow:model:export']">导出</el-button>
      </el-col> -->
    </el-row>

    <el-table v-loading="loading" :data="modelList" border stripe header-cell-class-name="headerCellClassName"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="模型标识" align="center" prop="modelKey" :show-overflow-tooltip="true" />
      <el-table-column label="模型名称" align="center" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-button type="text" @click="handleProcessView(scope.row)">
            <span>{{ scope.row.modelName }}</span>
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="流程分类" align="center" prop="categoryName" :formatter="(v) => categoryFormat(v)" />
      <el-table-column label="模型版本" align="center">
        <template #default="scope">
          <el-tag size="small">v{{ scope.row.version }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述" align="center" prop="description" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          {{ scope.row.createTime}}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250" fixed="right">
        <template #default="scope">
          <el-button type="text" @click="handleUpdate(scope.row)" v-hasPermi="['workflow:model:edit']">编辑</el-button>
          <el-button type="text" @click="handleDesigner(scope.row)"
            v-hasPermi="['workflow:model:designer']">设计</el-button>
          <el-button type="text" v-hasPermi="['workflow:model:deploy']" @click="handleDeploy(scope.row)">部署</el-button>
          <el-dropdown class="el-dropdown-more" v-hasPermi="[
            'workflow:model:query',
            'workflow:model:list',
            'workflow:model:remove',
          ]">
            <span class="el-dropdown-link">
              更多
              <el-icon class="el-icon--right">
                <arrow-down />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <span v-hasPermi="['workflow:model:query']">
                  <el-dropdown-item @click="handleProcessView(scope.row)">流程图</el-dropdown-item>
                </span>
                <span v-hasPermi="['workflow:model:list']">
                  <el-dropdown-item @click="handleHistory(scope.row)">历史</el-dropdown-item>
                </span>
                <span v-hasPermi="['workflow:model:remove']">
                  <el-dropdown-item @click="handleDelete(scope.row)">删除</el-dropdown-item>
                </span>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>



    <!--  添加或修改模型信息对话框  -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body @close="cancel()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="模型标识" prop="modelKey">
          <el-input v-model="form.modelKey" clearable :maxlength="500" />
        </el-form-item>
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="form.modelName" clearable :maxlength="500" />
        </el-form-item>
        <el-form-item label="流程分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择" clearable style="width: 180px">
            <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName"
              :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" :rows="3" type="textarea" placeholder="请输入内容" maxlength="250"
            show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="loading" @click="submitForm">确 定</el-button>
          <el-button @click="cancel()">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 流程图 -->
    <el-dialog :title="processView.title" v-model="processView.open" width="70%" append-to-body destroy-on-close
      :before-close="handleCloseViewer">
      <process-viewer :key="`designer-${processView.index}`" :xml="processView.xmlData" />
    </el-dialog>

    <el-dialog title="模型历史" v-model="history.open" width="70%">
      <el-table v-loading="history.loading" fit :data="historyList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="模型标识" align="center" prop="modelKey" :show-overflow-tooltip="true" />
        <el-table-column label="模型名称" align="center" :show-overflow-tooltip="true">
          <template #default="scope">
            <el-button type="text" @click="handleProcessView(scope.row)">
              <span>{{ scope.row.modelName }}</span>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="流程分类" align="center" prop="categoryName" :formatter="(v) => categoryFormat(v)" />
        <el-table-column label="模型版本" align="center">
          <template #default="scope">
            <el-tag size="small">v{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" align="center" prop="description" :show-overflow-tooltip="true" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="160">
          <template #default="scope">
            {{ scope.row.createTime }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="150">
          <template #default="scope">
            <el-button type="text" v-hasPermi="['workflow:model:deploy']"
              @click="handleDeploy(scope.row)">部署</el-button>
            <el-button type="text" v-hasPermi="['workflow:model:save']"
              @click="handleLatest(scope.row)">设为最新</el-button>
          </template>
        </el-table-column>
      </el-table>


    </el-dialog>

    <el-dialog :title="designerData.title" v-model="designerOpen" append-to-body fullscreen>
      <process-designer :key="designerOpen" style="border: 1px solid rgba(0, 0, 0, 0.1)" ref="modelDesigner"
        v-loading="designerData.loading" :bpmnXml="designerData.bpmnXml" :designerForm="designerData.form"
        @save="onSaveDesigner" />
    </el-dialog>
  </div>
</template>

<script setup name="Model">
// import useTagsViewStore from "@/store/modules/tagsView";
import { download } from "@/utils/request";
import {
  getBpmnXml,
  listModel,
  historyModel,
  latestModel,
  addModel,
  updateModel,
  saveModel,
  delModel,
  deployModel,
} from "@/api/workflow/model";
import { listCategory } from "@/api/workflow/category";
import ProcessDesigner from "@/components/ProcessDesigner";
import ProcessViewer from "@/components/ProcessViewer";
import { useRoute,useRouter  } from 'vue-router'
const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();
// const tagsViewStore = useTagsViewStore();

const loading = ref(true); // 遮罩层
const ids = ref([]); // 选中数组
const single = ref(true); // 非单个禁用
const multiple = ref(true); // 非多个禁用
const total = ref(0); // 总条数
const modelList = ref([]); // 流程模型表格数据
const categoryOptions = ref([]);
const open = ref(false);
const title = ref("");

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    modelKey: null,
    modelName: null,
    category: null,
  },
  form: {},
  rules: {
    modelKey: [
      { required: true, message: "模型标识不能为空", trigger: "blur" },
    ],
    modelName: [
      { required: true, message: "模型名称不能为空", trigger: "blur" },
    ],
    category: [{ required: true, message: "请选择类型", trigger: "change" }],
  },
});

const { queryParams, form, rules } = toRefs(data);
/**
 * 列表相关
 */
/** 查询流程分类列表 */
function getCategoryList() {
  listCategory().then((response) => {
    categoryOptions.value = response.rows;
  });
}

/** 查询流程模型列表 */
function getList() {
  loading.value = true;
  listModel(queryParams.value).then((response) => {
    modelList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function cancel() {
  open.value = false;
}

// 表单重置
function reset() {
  form.value = {
    modelId: null,
    modelKey: null,
    modelName: null,
    category: null,
    description: null,
  };
  proxy.resetForm("formRef");
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

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.modelId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 部署流程 */
function handleDeploy(row) {
  loading.value = true;
  deployModel({
    modelId: row.modelId,
  })
    .then((response) => {
      proxy.$modal.msgSuccess(response.msg);
      let obj = { name: "Deploy", path: "/workflow/deploy" };
      // return tagsViewStore.delCachedView(obj).then(() => {
      //   router.push(obj);
      // });
    })
    .finally(() => {
      loading.value = false;
    });
}

/** 新增 */
function handleAdd() {
  reset();
  title.value = "新增流程模型";
  const dateTime = new Date().getTime();
  form.value.modelKey = `Process_${dateTime}`;
  form.value.modelName = `业务流程_${dateTime}`;
  open.value = true;
}

/** 修改按钮操作 */
function handleUpdate(row) {
  title.value = "修改流程模型";
  form.value = row;
  open.value = true;
}

/** 提交 */
function submitForm() {
  proxy.$refs["formRef"].validate((valid) => {
    if (valid) {
      if (form.value.modelId !== null) {
        updateModel(form.value).then((response) => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addModel(form.value).then((response) => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const modelIds = row.modelId || ids.value;
  proxy.$modal
    .confirm('是否确认删除模型编号为"' + modelIds + '"的数据项？')
    .then(function () {
      loading.value = true;
      return delModel(modelIds);
    })
    .then(() => {
      loading.value = false;
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {
      loading.value = false;
    });
}

/** 导出按钮操作 */
function handleExport() {
  download(
    "workflow/model/export",
    {
      ...queryParams.value,
    },
    `wf_model_${new Date().getTime()}.xlsx`
  );
}

getList();
getCategoryList();

/**
 * 流程设计器相关
 */
const designerOpen = ref(false);
const dData = reactive({
  designerData: {
    loading: false,
    bpmnXml: "",
    modelId: null,
    form: {
      processName: null,
      processKey: null,
    },
  },
  processView: {
    title: "",
    open: false,
    index: undefined,
    xmlData: "",
  },
});

const { designerData, processView } = toRefs(dData);

/** 查看流程图 */
function handleProcessView(row) {
  processView.value.title = "流程图";
  processView.value.index = row.modelId;
  // 发送请求，获取xml
  getBpmnXml(row.modelId).then((response) => {
    processView.value.xmlData = response.msg;
    setTimeout(() => {
      processView.value.open = true;
    }, 100);
  });
}

/** 关闭流程图 */
function handleCloseViewer(done) {
  done();
  processView.value.open = false;
}

/** 设计按钮操作 */
function handleDesigner(row) {
  designerData.value.title = "流程设计 - " + row.modelName;
  designerData.value.modelId = row.modelId;
  designerData.value.form.processName = row.modelName;
  designerData.value.form.processKey = row.modelKey;
  if (row.modelId) {
    designerData.value.loading = true;
    getBpmnXml(row.modelId).then((response) => {
      designerData.value.bpmnXml = response.msg || "";
      designerData.value.loading = false;
      designerOpen.value = true;
    });
  }
}

/**保存新版本 */
function onSaveDesigner(bpmnXml) {
  let dataBody = {
    modelId: designerData.value.modelId,
    bpmnXml,
  };
  proxy.$modal
    .confirm("是否将此模型保存为新版本？")
    .then(function () {
      confirmSave(dataBody, true);
    })
    .catch((action) => {
      console.log("保存流程设计结果：",);
      // if (action === "cancel") {
      //   confirmSave(dataBody, false);
      // }
    });
}

/** 确认保存 */
function confirmSave(body, newVersion) {
  designerData.value.loading = true;
  saveModel(
    Object.assign(body, {
      newVersion: newVersion,
    })
  )
    .then(() => {
      designerOpen.value = false;
      getList();
    })
    .catch(() => {
      designerData.loading = false;
    });
}

/**
 * 历史版本相关
 */

const historyList = ref([]);
const historyTotal = ref(0);
const lData = reactive({
  history: {
    open: false,
    loading: false,
  },
  queryHistoryParams: {
    pageNum: 1,
    pageSize: 10,
    modelKey: null,
  },
});

const { history, queryHistoryParams } = toRefs(lData);

/** 设为最新版 */
function handleLatest(row) {
  proxy.$modal
    .confirm("是否确认将此版本设为最新？")
    .then(function () {
      history.value.loading = true;
      return latestModel({ modelId: row.modelId });
    })
    .then(() => {
      history.value.open = false;
      getList();
      proxy.$modal.msgSuccess(response.msg);
    })
    .catch(() => {
      history.value.loading = false;
    });
}

/** 查询历史版本 */
function getHistoryList() {
  history.value.loading = true;
  historyModel(queryHistoryParams.value).then((response) => {
    historyTotal.value = response.total;
    historyList.value = response.rows;
    history.value.loading = false;
  });
}

/** 历史版本 */
function handleHistory(row) {
  history.value.open = true;
  queryHistoryParams.value.modelKey = row.modelKey;
  getHistoryList();
}

function categoryFormat(row, column) {
  let obj = categoryOptions.value.find((item) => {
    return item.code == row.category;
  });
  if (obj) {
    return obj.categoryName;
  }
}
</script>
<style lang="scss" scoped>
.el-dropdown-more {
  margin-top: 9px;

  .el-dropdown-link {
    font-size: 14px;
    display: flex;
    align-items: center;
  }
}

:deep(.el-select) {
  width: 100%;
}
</style>