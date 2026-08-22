<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item label="机构名称" prop="orgName">
        <el-input
          v-model.trim="queryParams.orgName"
          placeholder="请输入机构名称"
          clearable
          style="width: 200px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="dict in sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
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
          v-hasPermi="['system:dept:add']"
          >新增</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll"
          >展开/折叠</el-button
        >
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="deptList"
      row-key="orgId"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column
        prop="orgName"
        label="机构名称"
        min-width="200"
        show-overflow-tooltip
      ></el-table-column>
      <el-table-column
        prop="orgCode"
        label="机构编码"
        width="80"
      ></el-table-column>
      <el-table-column
        prop="orderNum"
        label="排序"
        width="50"
      ></el-table-column>
      <el-table-column
        prop="responsiblePer"
        label="负责人"
        width="80"
      ></el-table-column>
      <el-table-column
        prop="phone"
        label="联系方式"
        width="110"
      ></el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="160"></el-table-column>
      <el-table-column
        prop="address"
        label="地址"
        min-width="160"
      >
      <template #default="scope"> 
        <span>{{ scope.row.address || '-'}}</span>

      </template>
    </el-table-column>
      <el-table-column prop="status" label="状态" width="60">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="100"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        fixed="right"
        width="120"
        class-name="small-padding fixed-width">
        <template #default="scope">
          <el-tooltip content="编辑" placement="top">
               <el-button link
                          type="success"
                          icon="Edit"
                          @click="handleUpdate(scope.row)"
                          v-hasPermi="['system:dept:edit']"></el-button>
          </el-tooltip>
           <el-tooltip content="新增" placement="top">
                <el-button link
                            type="success"
                            icon="Plus"
                            @click="handleAdd(scope.row)"
                            v-hasPermi="['system:dept:add']"></el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link
                      type="danger"
                      icon="Delete"
                      @click="handleDelete(scope.row)"
                      v-hasPermi="['system:dept:remove']"></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改机构对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="deptRef" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="24" v-if="deptOptions">
            <el-form-item label="上级机构">
              <el-tree-select
                v-model="form.parentId"
                :data="deptOptions"
                :props="{
                  value: 'orgId',
                  label: 'orgName',
                  children: 'children',
                }"
                value-key="orgId"
                placeholder="选择上级机构"
                check-strictly
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="机构状态">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :value="dict.value"
                  >{{ dict.label }}</el-radio
                >
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="机构名称" prop="orgName">
              <el-input v-model="form.orgName" placeholder="请输入机构名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="编码">
              <el-input v-model="form.orgCode" placeholder="请输入机构编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number
                v-model="form.orderNum"
                controls-position="right"
                :min="0"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="responsiblePer">
              <el-input
                v-model="form.responsiblePer"
                placeholder="请输入负责人"
                maxlength="20"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="请输入联系电话"
                maxlength="11"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="form.email"
                placeholder="请输入邮箱"
                maxlength="50"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地址">
              <el-input v-model="form.address" placeholder="请输入机构地址" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述">
              <el-input
                v-model="form.description"
                placeholder="请输入机构描述"
              />
            </el-form-item>
          </el-col>
        </el-row>
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

<script setup name="Dept">
import {
  listDept,
  getDept,
  delDept,
  addDept,
  updateDept,
  listDeptExcludeChild,
} from "@/api/system/dept";

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const deptList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const title = ref("");
const deptOptions = ref([]);
const isExpandAll = ref(true);
const refreshTable = ref(true);

const data = reactive({
  form: {},
  queryParams: {
    orgName: undefined,
    status: undefined,
  },
  rules: {
    parentId: [
      { required: true, message: "上级机构不能为空", trigger: "blur" },
    ],
    orgName: [{ required: true, message: "机构名称不能为空", trigger: "blur" }],
    orderNum: [
      { required: true, message: "显示排序不能为空", trigger: "blur" },
    ],
    email: [
      {
        type: "email",
        message: "请输入正确的邮箱地址",
        trigger: ["blur", "change"],
      },
    ],
    phone: [
      {
        pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
        message: "请输入正确的手机号码",
        trigger: "blur",
      },
    ],
  },
});

const { queryParams, form, rules } = toRefs(data);

/** 查询机构列表 */
function getList() {
  loading.value = true;
  listDept(queryParams.value).then((response) => {
    deptList.value = proxy.handleTree(response.rows, "orgId");
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
    orgId: undefined,
    parentId: undefined,
    orgName: undefined,
    orderNum: 0,
    responsiblePer: undefined,
    phone: undefined,
    email: undefined,
    status: "0",
  };
  proxy.resetForm("deptRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 新增按钮操作 */
function handleAdd(row) {
  reset();
  listDept().then((response) => {
    deptOptions.value = proxy.handleTree(response.rows, "orgId");
  });
  if (row != undefined) {
    form.value.parentId = row.orgId;
  }
  open.value = true;
  title.value = "添加机构";
}

/** 展开/折叠操作 */
function toggleExpandAll() {
  refreshTable.value = false;
  isExpandAll.value = !isExpandAll.value;
  nextTick(() => {
    refreshTable.value = true;
  });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  listDeptExcludeChild(row.orgId).then((response) => {
    if (response.data.length > 0) {
      deptOptions.value = proxy.handleTree(response.data, "orgId");
    } else {
      deptOptions.value = null;
    }
  });
  getDept(row.orgId).then((response) => {
    form.value = response.data;
    form.value.parentId = form.value.parentId == 0 ? " " : form.value.parentId;
    console.log(form.value.parentId, 123456);
    open.value = true;
    title.value = "修改机构";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["deptRef"].validate((valid) => {
    if (valid) {
      if (form.value.orgId != undefined) {
        updateDept(form.value).then((response) => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        console.log(form.value);
        addDept(form.value).then((response) => {
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
  proxy.$modal
    .confirm('是否确认删除名称为"' + row.orgName + '"的数据项?')
    .then(function () {
      return delDept(row.orgId);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

getList();
</script>
