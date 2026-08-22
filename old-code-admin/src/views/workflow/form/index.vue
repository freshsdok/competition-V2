<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px" @submit.prevent>
      <el-form-item label="表单名称" prop="formName">
        <el-input
          v-model.trim="queryParams.formName"
          placeholder="请输入表单名称"
          clearable
          @change="handleQuery"
          :maxlength="500"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          @click="handleAdd"
          v-hasPermi="['workflow:form:add']"
        >新增</el-button>
      </el-col>
     
      <el-col :span="1.5">
        <el-button
        type="danger" plain
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['workflow:form:remove']"
        >删除</el-button>
      </el-col>
      <!-- <el-col :span="1.5">
        <el-button
          icon="el-icon-download"
          @click="handleExport"
          v-hasPermi="['workflow:form:export']"
        >导出</el-button>
      </el-col> -->
    </el-row>

    <el-table 
      v-loading="loading" 
      :data="formList" 
      @selection-change="handleSelectionChange"
      border
      stripe
      header-cell-class-name="headerCellClassName">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" type="index" width="60" />
      <el-table-column label="表单名称" align="center" prop="formName" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            type="text"
            @click="handleDesignerPC(scope.row)"
            v-hasPermi="['workflow:form:designerPC']"
          >表单设计(PC)</el-button>
          <el-button
            type="text"
            @click="handleDesignerM(scope.row)"
            v-hasPermi="['workflow:form:designerM']"
          >表单设计(M)</el-button>
          <!-- <el-button
            type="text"
            @click="handlePreview(scope.row)"
          >预览</el-button> -->
          <el-button
            type="text"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['workflow:form:edit']"
          >编辑</el-button>
          <el-button
            type="text"
            @click="handleDelete(scope.row)"
            v-hasPermi="['workflow:form:remove']"
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

    <!-- 添加或修改流程表单对话框 -->
    <el-dialog 
      :title="title" 
      v-model="open" 
      width="500px" 
      append-to-body 
      :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="表单名称" prop="formName">
          <el-input v-model="form.formName" placeholder="请输入表单名称" :maxlength="500"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" :maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="loading" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!--表单配置详情-->
    <el-dialog 
      :title="formTitle" 
      v-model="formConfOpen" 
      width="60%" 
      append-to-body
      :before-close="handleClosePreview">
      <div class="test-form">
        <v-form-render ref="vFormRenderRef"></v-form-render>
      </div>
    </el-dialog>

    <!-- 表单设计 -->
    <form-designer 
      v-if="designerOpenPC" 
      :form="form" 
      @save="handleSuccess" 
      @cancel="designerOpenPC=false">
    </form-designer>

    <!-- 表单设计mobile -->
    <m-form-designer 
      v-if="designerOpenM" 
      :form="form" 
      @save="handleSuccess" 
      @cancel="designerOpenM=false">
    </m-form-designer>

  </div>
</template>

<script setup name="Form">
import { listForm, getForm, delForm, addForm, updateForm } from "@/api/workflow/form";
import { download } from '@/utils/request'
import formDesigner from "./components/formDesigner";
import mFormDesigner from "./components/mFormDesigner";

const { proxy } = getCurrentInstance();

const route = useRoute();
const router = useRouter();

const loading = ref(true)
const ids = ref([]) // 选中数组
const single = ref(true) // 非单个禁用
const multiple = ref(true) // 非多个禁用
const total = ref(0) // 总条数
const formList = ref([]) // 流程表单表格数据
const open = ref(false) // 是否显示弹出层
const title = ref("") // 弹出层标题
const formConf = reactive({}) // 默认表单数据
const formConfOpen = ref(false)
const formTitle = ref("")
const designerOpenPC = ref(false) // 表单设计器PC
const designerOpenM = ref(false) // 表单设计器mobile
const vFormRenderRef = ref() // 表单渲染器

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    formName: null,
    content: null,
  },
  form: {},
  rules: {
    formName: [{ required: true, message: "表单名称", trigger: "blur" }],
  },
})

const { queryParams, form, rules } = toRefs(data);

/** 查询流程表单列表 */
function getList() {
  loading.value = true;
  listForm(queryParams.value).then(response => {
    formList.value = response.rows;
    let item =formList.value[0]
    console.log(JSON.parse(item.content))
    total.value = response.total;
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}
// 表单重置
function reset() {
  form.value = {
    formId: null,
    formName: null,
    content: null,
    remark: null
  };
  proxy.resetForm("formRef");
}

/** 按钮操作 */
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
  ids.value = selection.map(item => item.formId)
  single.value = selection.length!==1
  multiple.value = !selection.length
}
/** 表单配置信息 */
function handlePreview(row){
  formConfOpen.value = true;
  formTitle.value = "流程表单预览";
  proxy.$nextTick(() => {
    vFormRenderRef.value.setFormJson(JSON.parse(row.content));
  })
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加流程表单";
  // router.push({ path: '/tool/variantForm', query: {formId: null }})
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const formId = row.formId || ids.value
  getForm(formId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改流程表单";
  });
  // router.push({ path: '/tool/build/index', query: {formId: row.formId }})
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.formId != null) {
        updateForm(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addForm(form.value).then(response => {
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
  const formIds = row.formId || ids.value;
  proxy.$modal.confirm('是否确认删除?').then(function () {
    return delForm(formIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => { });
}

/** 表单设计PC */
function handleDesignerPC(row){

  form.value = row;
  // console.log(form.value,244)
  designerOpenPC.value = true;
}

/** 表单设计mobile */
function handleDesignerM(row){
  form.value = row;
  designerOpenM.value = true;
}

/** 保存成功 */
function handleSuccess() {
  designerOpenPC.value = false;
  designerOpenM.value = false;
  getList();
}

/** 预览关闭 */
function handleClosePreview (done) {
  done();
  formConfOpen.value = false;
}

/** 导出按钮操作 */
function handleExport() {
  proxy.$modal.confirm('是否确认导出所有流程表单数据项?').then(function () {
    download('/workflow/form/export', {
      ...queryParams.value
    }, `form_${new Date().getTime()}.xlsx`)
  }).catch(() => { });
}

getList()
</script>

<style lang="scss" scoped>
.test-form {
  margin: 0 auto;
  width: 800px;
}
</style>
