<!-- 文件任务管理 -->
<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true" label-width="68px">
      <el-form-item label="任务名称" prop="taskName">
        <el-input v-model.trim="queryParams.taskName" placeholder="请输入任务名称" clearable style="width: 240px"
          @change="handleQuery" />
      </el-form-item>
      <el-form-item label="任务类型" prop="taskType">
        <el-select v-model="queryParams.taskType" placeholder="请选择任务类型" clearable style="width: 240px">
          <el-option v-for="dict in task_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="用户组" prop="status">
        <!-- <el-input
          v-model="queryParams.userGroupName"
          placeholder="任务名称"
          clearable
          style="width: 240px"
          @input="getUserList"
        /> -->
        <!-- <el-autocomplete
          v-model.trim="queryParams.userGroupName"
          :fetch-suggestions="getUserList"
          placeholder="请输入用户组"
        ></el-autocomplete> -->
        <el-select v-model.trim="queryParams.userGroupName" filterable remote reserve-keyword placeholder="请输入用户组"
          :remote-method="getUserList" :loading="loading" style="width: 240px" clearable>
          <el-option v-for="item in userList" :key="item.name" :label="item.name" :value="item.name" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="taskStatus">
        <el-select v-model="queryParams.taskStatus" placeholder="请选择状态" clearable style="width: 240px">
          <el-option v-for="dict in task_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <!-- <el-form-item label="创建时间" style="width: 308px">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item> -->

      <!-- <el-form-item label="开始时间" style="width: 308px">
        <el-date-picker
          v-model="queryParams.createTimeStart"
          value-format="YYYY-MM-DD"
          type="datetime"
          placeholder="选择日期时间"
        />
      </el-form-item>
       <el-form-item label="结束时间" style="width: 308px">
        <el-date-picker
          v-model="queryParams.createTimeEnd"
          value-format="YYYY-MM-DD"
          type="datetime"
          placeholder="选择日期时间"
        />
      </el-form-item> -->

      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD HH:mm:ss" type="datetimerange" range-separator="至"
          start-placeholder="开始时间" end-placeholder="结束时间" :default-time="[
            new Date('1970-01-01 00:00:00'),
            new Date('1970-01-01 23:59:59'),
          ]"></el-date-picker>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          v-hasPermi="['system:fileDistributeTask:add']">新增任务</el-button>
      </el-col>

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
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 表格数据 -->
    <el-table v-loading="loading" :data="roleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <!-- <el-table-column label="序号" align="left" type="index" width="50" /> -->
      <el-table-column label="任务编号" prop="id" width="78" />
      <el-table-column label="任务名称" prop="taskName" :show-overflow-tooltip="true" min-width="160" />
      <el-table-column label="任务类型" prop="taskType" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="用户组" prop="userGroupNames" :show-overflow-tooltip="true" min-width="130" />
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <el-tag effect="dark" :type="scope.row.taskStatus == '1'
            ? 'info'
            : scope.row.taskStatus == '2'
              ? 'success'
              : 'danger'
            ">{{
              scope.row.taskStatus == "1"
                ? "草稿"
                : scope.row.taskStatus == "2"
                  ? "已发布"
                  : "已撤回"
            }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" align="center" prop="createTime"   width="160"/>
      <!-- <template #default="scope">
          <span style="white-space: normal">{{
            parseTime(scope.row.createTime)
          }}</span>
        </template>
      </el-table-column> -->
      <el-table-column label="创建人" prop="createBy" :show-overflow-tooltip="true" width="80" />
      <el-table-column label="阅读情况" prop="roleKey" :show-overflow-tooltip="true" width="80">
        <template #default="scope">
          <span>{{ scope.row.readCount }}/{{ scope.row.peopleCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="上传情况" prop="roleKey" :show-overflow-tooltip="true" width="80">
        <template #default="scope">
          <span v-if="scope.row.taskType !== '分发任务'">{{ scope.row.uploadedCount }}/{{ scope.row.peopleCount }}</span>
          <span v-else>-/-</span>
        </template>
      </el-table-column>
      <el-table-column label="下载量" prop="roleKey" :show-overflow-tooltip="true" width="80">
        <template #default="scope">
          <span v-if="scope.row.taskType !== '交互任务'">{{ scope.row.downCount }}/{{ scope.row.peopleCount }}</span>
          <span v-else>-/-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" min-width="280">
        <template #default="scope">
          <el-tooltip content="详情" placement="top">
            <el-button link type="primary" icon="View" @click="handleUpdate(scope.row, true)"></el-button>
          </el-tooltip>
          <el-button
            v-if="isUploadTask(scope.row)"
            link
            type="warning"
            icon="ChatDotRound"
            @click="openRecipientDialog(scope.row)"
          >上传人员与通知</el-button>
          <el-tooltip content="导入评审" placement="top">
            <template v-if="scope.row.taskType === '交互任务'">
              <el-button
                link
                type="success"
                icon="Upload"
                v-hasPermi="['competition:review:object:import']"
                @click="goReviewImport(scope.row)"
              ></el-button>
            </template>
          </el-tooltip>
          <el-tooltip content="修改" placement="top">
            <template v-if="scope.row.taskStatus != '2'">
              <el-button link type="primary" icon="Edit" v-hasPermi="['system:fileDistributeTask:edit']"
                          @click="handleUpdate(scope.row)"></el-button>
            </template>
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <template v-if="scope.row.taskStatus != '2'">
              <el-button link type="primary" icon="Delete" v-hasPermi="['system:fileDistributeTask:remove']"
                          @click="handleDelete(scope.row)"></el-button>
            </template>
          </el-tooltip>
          <el-tooltip content="撤回" placement="top">
            <template v-if="scope.row.taskStatus != '3' && scope.row.taskStatus != '1'">
                <el-button link type="primary" v-hasPermi="['system:fileDistributeTask:edit']"
                          @click="handleStatus(scope.row, 3)">
                          <svg-icon icon-class="enter" style="font-size: 12px;"/>
                        </el-button>
            </template>
          </el-tooltip>
          <el-tooltip content="发布" placement="top">
            <template v-if="scope.row.taskStatus != '2'">
               <el-button link type="primary" icon="Position" v-hasPermi="['system:fileDistributeTask:edit']"
                        @click="handleStatus(scope.row, 2)" ></el-button>
            </template>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
    <!-- 新增/编辑操作 -->
    <EditDialog ref="editDialogRef" @refresh="getList" />
    <TaskRecipientNotificationDialog ref="recipientNotificationDialogRef" />
  </div>
</template>

<script setup name="fileTaskList">
import EditDialog from "./components/index.vue";
import TaskRecipientNotificationDialog from "./components/TaskRecipientNotificationDialog.vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { systemUserGroupMangerList } from "@/api/fileTask";
import {
  getLists,
  removeFileTask,
  updateTaskStatus,
} from "@/api/fileTask/task";
import { ref } from "vue";
import { useRouter } from "vue-router";

const { proxy } = getCurrentInstance();
const router = useRouter();

const { task_status, task_type } = proxy.useDict("task_status", "task_type");

console.log(proxy, "===");

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
const editDialogRef = ref(null);
const recipientNotificationDialogRef = ref(null);
const userData = ref([]);

/** 数据范围选项*/

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    taskStatus: undefined,
    taskName: undefined,
    userGroupNames: undefined,
    userGroupName: undefined,
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

/** 查询角色列表 */
function getList() {
  loading.value = true;
  getLists({
    ...proxy.addDateRange(queryParams.value, dateRange.value),
    createTimeStart: dateRange.value[0],
    createTimeEnd: dateRange.value[1],
  }).then((response) => {
    roleList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}
/** 新增任务 */
function handleAdd() {
  if (editDialogRef.value) {
    editDialogRef.value.openDialog();
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  queryParams.value.userGroupName = undefined;
  queryParams.value.createTimeStart = undefined;
  queryParams.value.createTimeEnd = undefined;
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleUpdate(row, bool = false) {
  if (editDialogRef.value) {
    editDialogRef.value.openDialog(row, bool);
  }
}

function goReviewImport(row) {
  router.push({
    path: "/review/import",
    query: {
      sourceBizType: "FILE_UPLOAD_MANAGER",
      fileTaskId: row.id,
    },
  });
}

function isUploadTask(row) {
  const configs = Array.isArray(row.fileTaskConfigList)
    ? row.fileTaskConfigList
    : [];
  if (configs.length > 0) {
    return configs.some((item) => String(item.taskType) === "1");
  }
  return row.taskType === "上传任务" || row.taskType === "交互任务";
}

function openRecipientDialog(row) {
  recipientNotificationDialogRef.value?.openDialog(row);
}

/** 删除按钮操作 */
function handleDelete(row) {
  ElMessageBox.confirm(`是否删除任务【${row.taskName}】`, "系统提示", {
    confirmButtonText: "确 定",
    cancelButtonText: "取 消",
    type: "warning",
  })
    .then(() => {
      removeFileTask(row.id).then(() => {
        ElMessage({
          type: "success",
          message: "删除成功",
        });
        getList();
      });
    })
    .catch(() => {
      ElMessage({
        type: "info",
        message: "取消删除",
      });
    });
}

/** 更新任务状态 */
function handleStatus(row, status) {
  ElMessageBox.confirm(
    `是否${status == "1" ? "草稿" : status == "2" ? "发布" : "撤回"}任务【${row.taskName
    }】`,
    "系统提示",
    {
      confirmButtonText: "确 定",
      cancelButtonText: "取 消",
      type: "warning",
    }
  )
    .then(() => {
      updateTaskStatus({
        id: row.id,
        taskStatus: status,
      }).then(() => {
        ElMessage({
          type: "success",
          message: `操作成功`,
        });
        getList();
      });
    })
    .catch(() => {
      ElMessage({
        type: "info",
        message: "取消操作",
      });
    });
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.roleId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 自动完成选择事件 */
function handleSelect(item) {
  queryParams.value.userGroupName = item.name;
}

let userList = $ref([])
/**获取用户列表 */
async function getUserList(val, cb) {
  let response = await systemUserGroupMangerList({ name: val });
  if (response.code == 200) {
    userList = response.rows;
    // response.rows.forEach((item) => {
    //   item.value = item.name;
    // });
    console.log(userList, "===");
    if (typeof cb === "function") {
      cb(response.rows);
    }
  }
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
