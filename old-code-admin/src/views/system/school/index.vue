<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item label="学校名称" prop="schoolName">
        <el-input
          v-model.trim="queryParams.schoolName"
          placeholder="请输入学校名称"
          clearable
          style="width: 200px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="办学层次" prop="educationalLevel">
        <el-select
          v-model="queryParams.educationalLevel"
          placeholder="请选择办学层次"
          clearable
          @change="handleQuery"
          style="width: 200px"
        >
          <el-option label="专科" value="专科" />
          <el-option label="本科" value="本科" />
        </el-select>
      </el-form-item>
      <el-form-item
        label="是否双一流院校"
        prop="doubleFirstClassUniversityPlan"
      >
        <el-select
          v-model="queryParams.doubleFirstClassUniversityPlan"
          placeholder="请选择是否双一流院校"
          clearable
          @change="handleQuery"
          style="width: 200px"
        >
          <el-option label="是" value="是" />
          <el-option label="否" value="否" />
        </el-select>
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
          v-hasPermi="['system:school:add']"
          >新增</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:school:edit']"
          >修改</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          v-hasPermi="['system:school:remove']"
          @click="handleDelete"
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
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column type="index" label="序号" width="80" align="center"  />
      <el-table-column label="学校名称" align="left" prop="schoolName"  width="200"/>
      <el-table-column
        label="学校标识码"
        align="center"
        width="120"
        prop="schoolIdentificationCode"
      />
      <el-table-column
        label="主管部门"
        align="center"
        width="120"
        prop="competentDepartment"
      />
      <el-table-column label="省" align="center" prop="province" />
      <el-table-column label="市" align="center" prop="city" />

      <el-table-column
        label="办学层次"
        align="center"
        prop="educationalLevel"
      />
      <el-table-column label="备注" align="center" prop="remak" />
      <el-table-column label="985" align="center" prop="nineEightFive" />
      <el-table-column label="211" align="center" prop="twoOneOne" />
      <el-table-column
        label="双一流"
        align="center"
        prop="doubleFirstClassUniversityPlan"
      />
      <el-table-column
        label="操作"
        width="180"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-tooltip content="修改" placement="top">
            <el-button
            link
            type="success"
            icon="Edit"
            v-hasPermi="['system:school:edit']"
            @click="handleUpdate(scope.row)"></el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:school:remove']"></el-button>
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
        <el-form-item label="学校名称" prop="schoolName">
          <el-input v-model="form.schoolName" placeholder="请输入学校名称" />
        </el-form-item>
        <el-form-item label="主管部门">
          <el-input v-model="form.competentDepartment" placeholder="请输入主管部门" />
        </el-form-item>
        <el-form-item label="省">
          <el-input v-model="form.province" placeholder="请输入所属省份" />
        </el-form-item>
        <el-form-item label="市">
          <el-input v-model="form.city" placeholder="请输入所属市" />
        </el-form-item>
        <el-form-item label="办学层次" prop="educationalLevel">
          <el-select
            v-model="form.educationalLevel"
            placeholder="请选择办学层次"
            clearable
          >
            <el-option label="专科" value="专科" />
            <el-option label="本科" value="本科" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否有国际学院">
          <el-select
            v-model="form.internationalAcademy"
            placeholder="请选择是否有国际学院"
            clearable
          >
            <el-option label="是" value="是" />
            <el-option label="否" value="否" />
          </el-select>
        </el-form-item>
        <el-form-item label="官网网址">
          <el-input
            v-model="form.officialWebsiteAddress"
            placeholder="请输入官网网址"
          />
        </el-form-item>
        <el-form-item label="国际学院网址">
          <el-input
            v-model="form.internationalCollegeWebsite"
            placeholder="请输入国际学院网址"
          />
        </el-form-item>
        <el-form-item label="是否双一流院校">
          <el-select
            v-model="form.doubleFirstClassUniversityPlan"
            placeholder="是否双一流院校"
            clearable
          >
            <el-option label="是" value="是" />
            <el-option label="否" value="否" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否985院校">
          <el-select
            v-model="form.nineEightFive"
            placeholder="是否985院校"
            clearable
          >
            <el-option label="是" value="是" />
            <el-option label="否" value="否" />
          </el-select>
        </el-form-item>

        <el-form-item label="是否211院校">
          <el-select
            v-model="form.twoOneOne"
            placeholder="是否211院校"
            clearable
          >
            <el-option label="是" value="是" />
            <el-option label="否" value="否" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remak"
            type="textarea"
            placeholder="请输入内容"
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
  listschool,
  addschool,
  delschool,
  getschool,
  updateschool,
} from "@/api/system/school";

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
    postCode: undefined,
    postName: undefined,
    status: undefined,
  },
  rules: {
    schoolName: [
      { required: true, message: "学校名称不能为空", trigger: "blur" },
    ],
    educationalLevel: [
      { required: true, message: "请选择办学层次", trigger: "blur" },
    ],
  },
});

const { queryParams, form, rules } = toRefs(data);

/** 查询院校列表 */
function getList() {
  loading.value = true;
  listschool(queryParams.value).then((response) => {
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
  getschool(id).then((response) => {
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
        updateschool(form.value).then((response) => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addschool(form.value).then((response) => {
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
      return delschool(ids);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

getList();
</script>
<style lang="scss" scoped>
.scrollable-cell {
  width: 150px; /* 根据需要调整宽度 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}
</style>
