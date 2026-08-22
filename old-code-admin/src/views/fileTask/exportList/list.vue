<!-- 导出管理 -->
<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      v-show="showSearch"
      :inline="true"
      label-width="68px"
    >
      <!-- <el-form-item label="用户" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="任务创建人"
          clearable
          style="width: 240px"
          @change="handleQuery"
        />
      </el-form-item> -->
      <!-- <el-form-item label="任务名称" prop="roleName">
        <el-input
          v-model="queryParams.roleName"
          placeholder="任务名称"
          clearable
          style="width: 240px"
          @change="handleQuery"
        />
      </el-form-item> -->
      <el-form-item label="导出状态" prop="exportStatus">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择导出状态"
          style="width: 240px"
        >
          <el-option
            v-for="item in file_export_status"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
      </el-form-item>

      <!-- <el-form-item label="创建时间" style="width: 308px">
        <el-date-picker
          v-model="queryParams.startTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetime"
          placeholder="选择日期时间"
        />
      </el-form-item>
       <el-form-item label="结束时间" style="width: 308px">
        <el-date-picker
          v-model="queryParams.endTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetime"
          placeholder="选择日期时间"
        />
      </el-form-item> -->
      <el-form-item label="创建时间" style="width: 308px">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <!-- <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:role:add']"
          >新增任务</el-button
        >
      </el-col> -->

      <!-- <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:role:remove']"
          >删除</el-button
        >
      </el-col> -->
      <!-- <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:role:export']"
          >导出</el-button
        >
      </el-col> -->
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <!-- 表格数据 -->
    <el-table
      v-loading="loading"
      :data="roleList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编号" align="left" prop="id" width="80" />
      <!-- <el-table-column label="任务id" prop="id" />
      <el-table-column label="任务名称" prop="roleId" >
        <template #default="scope">
         <span>第一届航天辩论大赛</span>
        </template>
        </el-table-column> -->

      <el-table-column
        label="任务创建时间"
        align="center"
        prop="createTime"
        :show-overflow-tooltip="true"
      />
      <!-- <template #default="scope">
          <span style="white-space: normal">{{
            parseTime(scope.row.createTime)
          }}</span>
        </template>
      </el-table-column> -->
      <el-table-column
        label="任务完成时间"
        align="center"
        prop="updateTime"
        :show-overflow-tooltip="true"
      />
      <!-- <template #default="scope">
          <span style="white-space: normal">{{
            parseTime(scope.row.endTime)
          }}</span>
        </template>
      </el-table-column> -->
      <el-table-column
        label="创建人"
        prop="userName"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="导出状态" prop="roleId">
        <template #default="scope">
          <!-- <el-tag v-if="scope.row.status === '0'" effect="dark" type="success">
            导出中</el-tag
          >
          <el-tag v-else effect="dark" type="warning">导出完成</el-tag> -->
          <dict-tag :options="file_export_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        label="文件大小"
        prop="size"
        :show-overflow-tooltip="true"
      >
        <template #default="scope">
          <span style="white-space: normal">{{
            scope.row.size ? scope.row.size : "-"
          }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="下载链接"
        prop="fileUrl"
        :show-overflow-tooltip="true"
      >
        <template #default="scope">
          <!-- <a :href="scope.row.fileUrl" style="color: #409eff" target="do">{{
            scope.row.title?scope.row.title:'-'
          }}</a> -->

          <!-- <span
            class="download-link"
            @click="downloadOssFile(item.downloadLink, item.fileName)"
            target="_blank"
            >{{ item.fileName }}</span
          > -->

          <el-button
          v-if="scope.row.title"
           icon="Download"
            type="primary"
            link
             @click="downloadOssFile(scope.row.fileUrl, scope.row.title)"
          >
            {{ scope.row.title}}
          </el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <!-- <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-tooltip content="修改" placement="top">
            <el-button
              link
              type="primary"
              icon="Edit"
              @click="handleUpdate(scope.row)"
            ></el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button
              link
              type="primary"
              icon="Delete"
              @click="handleDelete(scope.row)"
            ></el-button>
          </el-tooltip>

          <el-tooltip content="撤回" placement="top">
            <el-button link type="primary" icon="RefreshLeft"></el-button>
          </el-tooltip>
          <el-tooltip
            content="发布"
            placement="top"
            v-if="scope.row.roleId !== 1"
          >
            <el-button link type="primary" icon="Position"></el-button>
          </el-tooltip>
        </template>
      </el-table-column> -->
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="fileTaskList">
import { listRole } from "@/api/system/role";
import { exportManageList } from "@/api/fileTask/task";
import { download, downloadJS } from "@/utils/request";
import { ossFileFuc } from "@/hooks/download";
const { downloadOssFile } = ossFileFuc();
const { proxy } = getCurrentInstance();
const { file_export_status } = proxy.useDict("file_export_status");
const optionsStatus = ref([
  {
    value: "0",
    label: "导出中",
  },
  {
    value: "1",
    label: "导出完成",
  },
]);
const roleList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const menuOptions = ref([]);
const menuExpand = ref(false);
const menuNodeAll = ref(false);
const deptExpand = ref(true);
const deptNodeAll = ref(false);
const deptOptions = ref([]);
const openDataScope = ref(false);
const menuRef = ref(null);
const deptRef = ref(null);

/** 数据范围选项*/

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    status: undefined,
    startTime: undefined,
    endTime: undefined,
  },
  rules: {
    roleName: [
      { required: true, message: "角色名称不能为空", trigger: "blur" },
    ],
    roleKey: [{ required: true, message: "编码不能为空", trigger: "blur" }],
    roleSort: [
      { required: true, message: "角色顺序不能为空", trigger: "blur" },
    ],
  },
});

const { queryParams, form, rules } = toRefs(data);

function downFile(row) {
  if (row.status == "0") {
    proxy.$message.warning("文件导出中，不能下载");
    return;
  }
  if (row.status == "2") {
    proxy.$message.error("文件导出失败，不能下载");
    return;
  }
  downloadJS(row.fileUrl, row.title);
}
/** 查询角色列表 */
function getList() {
  loading.value = true;
  exportManageList({
    ...proxy.addDateRange(queryParams.value),
    createStartTime: dateRange.value[0] || "",
    createEndTime: dateRange.value[1] || "",
  }).then((response) => {
    roleList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  queryParams.value.userName = undefined;
  queryParams.value.status = undefined;
  queryParams.value.startTime = undefined;
  queryParams.value.endTime = undefined;
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 删除按钮操作 */
function handleDelete(row) {}

/** 导出按钮操作 */
function handleExport() {}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.roleId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 重置新增的表单以及其他数据  */
function reset() {
  if (menuRef.value != undefined) {
    menuRef.value.setCheckedKeys([]);
  }
  menuExpand.value = false;
  menuNodeAll.value = false;
  deptExpand.value = true;
  deptNodeAll.value = false;
  form.value = {
    roleId: undefined,
    roleName: undefined,
    roleKey: undefined,
    roleSort: 0,
    status: "0",
    menuIds: [],
    deptIds: [],
    menuCheckStrictly: true,
    deptCheckStrictly: true,
    remark: undefined,
  };
  proxy.resetForm("roleRef");
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["roleRef"].validate((valid) => {});
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

getList();
</script>
<style scoped lang="scss">
.tree {
  display: flex;
  width: 800px;
  height: 30px;
  justify-content: space-between;
}
:deep(.el-tree-node__content) {
  height: 35px;
}
</style>
