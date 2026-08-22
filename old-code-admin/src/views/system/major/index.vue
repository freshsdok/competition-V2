<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item label="学科门类" prop="disciplineCategory">
        <el-input
          v-model.trim="queryParams.disciplineCategory"
          placeholder="请输入学科门类"
          clearable
          style="width: 200px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="专业类名称" prop="majorClass">
        <el-input
          v-model.trim="queryParams.majorClass"
          placeholder="请输入专业类名称"
          clearable
          style="width: 200px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="专业名称" prop="minorClass">
        <el-input
          v-model.trim="queryParams.minorClass"
          placeholder="请输入专业名称"
          clearable
          style="width: 200px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:major:add']"
          >新增</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:major:remove']"
          >删除</el-button
        >
      </el-col>

      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="postList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="40" align="center" />
      <el-table-column type="index" label="序号" width="50" align="center"  />
      <el-table-column
        label="学科门类"
        align="center"
        min-width="100px"
        prop="disciplineCategory"
      />
      <el-table-column
        label="学科门类编码（如01哲学）"
        align="center"
        min-width="200px"
        prop="disciplineCategoryCode"
      />
      <el-table-column label="专业类名称" align="center" prop="majorClass"  min-width="150px"/>
      <el-table-column
        label="专业类代码（如0101哲学类）"
        align="center"
        prop="majorClassCode"
         min-width="200px"
      />
      <el-table-column label="专业名称" align="center" prop="minorClass"  min-width="120px"/>

      <el-table-column label="专业代码" align="center" prop="minorClassCode"  min-width="120px"/>

      <el-table-column
        label="操作"
        width="140"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-tooltip content="修改" placement="top">
            <el-button
              link
              type="success"
              icon="Edit"
              v-hasPermi="['system:major:edit']"
              @click="handleUpdate(scope.row)"></el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button
                  link
                  type="danger"
                  icon="Delete"
                  @click="handleDelete(scope.row)"
                  v-hasPermi="['system:major:remove']"></el-button>
          </el-tooltip>
          
          
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

    <!-- 添加或修改院校对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="postRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="学科门类" prop="disciplineCategory">
          <el-input
            v-model="form.disciplineCategory"
            placeholder="请输入学科门类"
          />
        </el-form-item>
        <el-form-item label="学科门类编码" prop="disciplineCategoryCode">
          <el-input
            v-model="form.disciplineCategoryCode"
            placeholder="请输入学科门类编码（如01哲学）"
          />
        </el-form-item>
        <el-form-item label="专业类名称" prop="majorClass">
          <el-input v-model="form.majorClass" placeholder="请输入专业类名称" />
        </el-form-item>

        <el-form-item label="专业类代码" prop="majorClassCode">
          <el-input
            v-model="form.majorClassCode"
            placeholder="请输入专业类代码（如0101哲学类）"
          />
        </el-form-item>

        <el-form-item label="专业名称" prop="minorClass">
          <el-input v-model="form.minorClass" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="专业代码" prop="minorClassCode">
          <el-input
            v-model="form.minorClassCode"
            placeholder="请输入专业代码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Post">
import {
  listdiscipline,
  adddiscipline,
  deldiscipline,
  getdiscipline,
  updatediscipline,
} from "@/api/system/major";

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const postList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
  rules: {
    disciplineCategory: [
      { required: true, message: "学校名称不能为空", trigger: "blur" },
    ],
    disciplineCategoryCode: [
      { required: true, message: "请选择办学层次", trigger: "blur" },
    ],
     majorClass: [
      { required: true, message: "学校名称不能为空", trigger: "blur" },
    ],
    majorClassCode: [
      { required: true, message: "请选择办学层次", trigger: "blur" },
    ],
     minorClass: [
      { required: true, message: "学校名称不能为空", trigger: "blur" },
    ],
    minorClassCode: [
      { required: true, message: "请选择办学层次", trigger: "blur" },
    ],
  },
});

const { queryParams, form, rules } = toRefs(data);

/** 查询院校列表 */
function getList() {
  loading.value = true;
  listdiscipline(queryParams.value).then((response) => {
    postList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    postCode: undefined,
    postName: undefined,
    postSort: 0,
    status: "0",
    remark: undefined,
  };
  proxy.resetForm("postRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加院校";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const id = row.id || ids.value;
  getdiscipline(id).then((response) => {
    form.value = response.data;
    open.value = true;
    title.value = "修改院校";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["postRef"].validate((valid) => {
    if (valid) {
      if (form.value.id != undefined) {
        updatediscipline(form.value).then((response) => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        adddiscipline(form.value).then((response) => {
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
  const ids = row.id || ids.value;
  proxy.$modal
    .confirm("是否确认删除该数据项？")
    .then(function () {
      return deldiscipline(ids);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

getList();
</script>
