<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input
          v-model.trim="queryParams.categoryName"
          placeholder="请输入分类名称"
          clearable
          @change="handleQuery"
          :maxlength="500"
        />
      </el-form-item>
      <el-form-item label="分类编码" prop="code">
        <el-input
          v-model.trim="queryParams.code"
          placeholder="请输入分类编码"
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
          v-hasPermi="['workflow:category:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
        type="danger" plain
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['workflow:category:remove']"
        >删除</el-button>
      </el-col>
      <!-- <el-col :span="1.5">
        <el-button
          icon="el-icon-download"
          @click="handleExport"
          v-hasPermi="['workflow:category:export']"
        >导出</el-button>
      </el-col> -->
    </el-row>

    <el-table 
      v-loading="loading" 
      :data="categoryList" 
      @selection-change="handleSelectionChange"
      border 
      stripe
      header-cell-class-name="headerCellClassName">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" type="index" width="55"/>
      <el-table-column label="分类名称" align="center" prop="categoryName" />
      <el-table-column label="分类编码" align="center" prop="code" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button
            type="text"
            @click="handleUpdate(row)"
            v-hasPermi="['workflow:category:edit']"
          >编辑</el-button>
          <el-button
            type="text"
            @click="handleDelete(row)"
            v-hasPermi="['workflow:category:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination 
      v-show="total > 0" 
      :total="total" 
      v-model:page="queryParams.pageNum" 
      v-model:limit="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改【请填写功能名称】对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" :maxlength="500"/>
        </el-form-item>
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入分类编码" :maxlength="500"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" :maxlength="500"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Category">
import { listCategory, getCategory, delCategory, addCategory, updateCategory } from "@/api/workflow/category";
import { download } from '@/utils/request'

const { proxy } = getCurrentInstance();

const buttonLoading = ref(false) // 按钮loading
const loading = ref(true) // 遮罩层
const ids = ref([]) // 选中数组
const single = ref(true) // 非单个禁用
const multiple = ref(true) // 非多个禁用
const total = ref(0) // 总条数
const categoryList = ref([]) // 【请填写功能名称】表格数据
const open = ref(false) // 是否显示弹出层
const title = ref("") // 弹出层标题

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    categoryName: undefined,
    code: undefined,
  },
  form: {},
  rules: {
    categoryName: [
      { required: true, message: "分类名称不能为空", trigger: "blur" }
    ],
    code: [
      { required: true, message: "分类编码不能为空", trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listCategory(queryParams.value).then(response => {
    categoryList.value = response.rows;
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
    categoryId: null,
    categoryName: null,
    code: null,
    remark: null
  }
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
  ids.value = selection.map(item => item.categoryId)
  single.value = selection.length!==1
  multiple.value = !selection.length
}
/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加流程分类";
}
/** 修改按钮操作 */
function handleUpdate(row) {
  loading.value = true;
  reset();
  const categoryId = row.categoryId || ids.value
  getCategory(categoryId).then(response => {
    loading.value = false;
    Object.assign(form.value, response.data);
    open.value = true;
    title.value = "修改流程分类";
  }).catch(() => {
    loading.value = false;
  });
}
/** 提交按钮 */
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.categoryId != null) {
        updateCategory(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        }).finally(() => {
          buttonLoading.value = false;
        });
      } else {
        addCategory(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        }).finally(() => {
          buttonLoading.value = false;
        });
      }
    }
  });
}
/** 删除按钮操作 */
function handleDelete(row) {
  const categoryIds = row.categoryId || ids.value;
  proxy.$modal.confirm('是否确认删除该分类？').then(() =>{
    return delCategory(categoryIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => { 
    
  });
}
/** 导出按钮操作 */
function handleExport() {
  download('workflow/category/export', {
    ...queryParams.value
  }, `category_${new Date().getTime()}.xlsx`)
}

getList()
</script>
