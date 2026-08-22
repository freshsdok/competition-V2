<template>
  <div class="app-container">
    <el-row :gutter="24">
      <!--用户数据-->
      <el-col>
        <el-form
          :model="queryParams"
          ref="queryRef"
          :inline="true"
          v-show="showSearch"
          label-wid.th="100px"
        >
          <el-form-item label="审核标题" prop="auditTitle">
            <el-input
              v-model.trim="queryParams.auditTitle"
              placeholder="请输入审核标题"
              clearable
              style="width: 240px"
              @change="handleQuery"
            />
          </el-form-item>
          <el-form-item label="类型" prop="auditType">
            <el-select
              v-model="queryParams.auditType"
              placeholder="请选择类型"
              clearable
              style="width: 240px"
            >
              <el-option
                v-for="dict in audit_flow_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>

          <!-- <el-form-item label="状态" prop="status">
                        <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 240px">
                            <el-option v-for="dict in isEnable" :key="dict.value" :label="dict.label"
                                :value="dict.value" />
                        </el-select>
                    </el-form-item> -->
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
              v-hasPermi="['system:audit:add']"
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
              v-hasPermi="['system:audit:remove']"
              >删除</el-button
            >
          </el-col>
        </el-row>

        <el-table
          v-loading="loading"
          :data="userList"
          @selection-change="handleSelectionChange"
          style="width: 100%"
        >
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="left" type="index" width="50" />
          <el-table-column
            label="审核标题"
            align="center"
            key="userName"
            prop="auditTitle"
          />
          <el-table-column
            label="类型"
            align="center"
            key="nickName"
            prop="auditType"
            :show-overflow-tooltip="true"
          >
            <template #default="scope">
              <dict-tag
                :options="audit_flow_type"
                :value="scope.row.auditType"
              />
            </template>
          </el-table-column>
          <el-table-column
            label="版本号"
            align="center"
            key="version"
            prop="version"
          >
            <template #default="scope"> V{{ scope.row.version }} </template>
          </el-table-column>
          <el-table-column
            label="流程描述"
            align="center"
            key="flowDesc"
            prop="flowDesc"
            :show-overflow-tooltip="true"
          />
          <el-table-column
            label="是否开启审核"
            align="center"
            key="phonenumber"
            prop="isEnable"
            width="120"
          >
            <template #default="scope">
              <el-switch
                :model-value="scope.row.isEnable === '1'"
                :active-value="true"
                :inactive-value="false"
                @change="(val) => handleSwitchChange(scope.row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column
            label="创建时间"
            align="center"
            key="statustime"
            prop="createTime"
          >
          </el-table-column>

          <el-table-column
            label="操作"
            align="center"
            width="300"
            class-name="small-padding fixed-width"
          >
            <template #default="scope">
              <el-button link type="primary" @click="handlechek(scope.row)"
                >查看</el-button
              >

              <el-button
                link
                type="primary"
                @click="handleUpdate(scope.row)"
                v-hasPermi="['system:audit:edit']"
                :disabled="scope.row.isEnable === '1'"
                >编辑</el-button
              >

              <el-button link type="primary" @click="kelong(scope.row)"
                >克隆</el-button
              >

              <el-button
                link
                type="primary"
                @click="handleDelete(scope.row)"
                v-hasPermi="['system:audit:remove']"
                >删除</el-button
              >
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
      </el-col>
    </el-row>

    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form
        :model="form"
        :rules="rules"
        ref="userRef"
        label-width="120px"
        :disabled="title == '查看流程配置'"
      >
        <el-row>
          <el-col :span="24">
            <el-form-item label="审核标题" prop="auditTitle">
              <el-input
                v-model="form.auditTitle"
                placeholder="请输入审核标题"
                maxlength="30"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="类型" prop="auditType">
              <el-select v-model="form.auditType" placeholder="类型" clearable>
                <el-option
                  v-for="dict in audit_flow_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="流程描述" prop="auditType">
              <el-input
                v-model="form.flowDesc"
                :autosize="{ minRows: 2, maxRows: 4 }"
                type="textarea"
                placeholder="流程描述"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="开启审核">
              <el-switch
                v-model="form.isEnable"
                active-value="1"
                inactive-value="0"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider style="margin: 10px" />
        <el-row>
          <template
            v-for="(item, index) in form.sysAuditConfigList"
            :key="index"
          >
            <el-col :span="24">
              <el-form-item :label="'开启' + (index + 1) + '级审核'">
                <el-switch
                  v-model="item.isEnable"
                  active-value="1"
                  inactive-value="0"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item :label="item.levelName">
                <el-radio-group
                  v-model="item.checkPersonType"
                  :disabled="item.isEnable == '0'"
                >
                  <el-radio
                    v-for="dict in reviewer_type"
                    :key="dict.value"
                    :value="dict.value"
                    @change="qingkong(index)"
                    >{{ dict.label }}</el-radio
                  >
                </el-radio-group>
                <el-form-item
                  label="机构"
                  v-show="item.checkPersonType != 'role'"
                >
                  <el-cascader
                    :options="deptList"
                    placeholder="请选择机构"
                    :props="props1"
                    clearable
                    :disabled="item.isEnable == '0'"
                    @change="userchakan(index)"
                    v-model="item.checkPersonOrgs"
                    style="width: 300px; margin-top: 5px"
                  />
                </el-form-item>
                <el-form-item
                  label="角色"
                  v-show="
                    item.checkPersonType == 'role' ||
                    item.checkPersonType == 'deptRole'
                  "
                >
                  <el-select
                    v-model="item.checkPersonRole"
                    placeholder="请选择角色"
                    clearable
                    :disabled="item.isEnable == '0'"
                    style="width: 300px; margin-top: 5px"
                  >
                    <el-option
                      v-for="dict in roleList"
                      :key="dict.roleKey"
                      :label="dict.roleName"
                      :value="dict.roleKey + ''"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item
                  label="角色"
                  v-show="item.checkPersonType == 'user'"
                >
                  <el-select
                    v-model="item.checkPersonRole"
                    placeholder="请选择角色"
                    clearable
                    :disabled="item.isEnable == '0'"
                    @change="userchakan(index)"
                    style="width: 300px; margin-top: 5px"
                  >
                    <el-option
                      v-for="dict in roleList"
                      :key="dict.roleId"
                      :label="dict.roleName"
                      :value="dict.roleId + ''"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item
                  label="用户"
                  v-show="item.checkPersonType == 'user'"
                >
                  <el-select
                    v-model="item.checkPerson"
                    placeholder="请选择用户"
                    clearable
                    :disabled="item.isEnable == '0'"
                    style="width: 300px; margin-top: 5px"
                    v-show="item.checkPersonType == 'user'"
                  >
                    <el-option
                      v-for="dict in userlist[index]"
                      :key="dict.userId"
                      :label="dict.nickName"
                      :value="dict.userId + ''"
                    />
                  </el-select>
                </el-form-item>
              </el-form-item>
            </el-col>
          </template>
        </el-row>
      </el-form>
      <template #footer v-if="title !== '查看流程配置'">
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="User">
import { listDeptOrg } from "@/api/system/dept";
import { listRole } from "@/api/system/role";
import {
  auditlist,
  audit,
  auditroleId,
  updataaudit,
  delaudit,
  enableOrDeactivate,
  auditcopy,
  getUserList,
} from "@/api/system/process.js";
import { getPathByOrgId, getPathByOrg } from "@/utils/auth";
const { proxy } = getCurrentInstance();
const { audit_flow_type, reviewer_type } = proxy.useDict(
  "audit_flow_type",
  "reviewer_type"
);
const userList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const initPassword = ref(undefined);
const deptList = ref([]);
const jigou = ref([]);
/** 查询机构列表 */
const props1 = {
  checkStrictly: true,
  label: "orgName",
  value: "orgId",
};
function getdeptList() {
  listDeptOrg().then((response) => {
    jigou.value = response.data;
    deptList.value = proxy.handleTree(response.data, "orgId");
    console.log(deptList.value);
  });
}
getdeptList();
const roleList = ref([]);
/** 查询角色列表 */
function getRoleList() {
  listRole().then((response) => {
    roleList.value = response.rows;
  });
}
getRoleList();
// 克隆
const kelong = (row) => {
  auditcopy(row.auditId).then((response) => {
    getList();
  });
};
const data = reactive({
  form: {
    auditTitle: "",
    auditType: "",
    flowDesc: "",
    isEnable: "0",
    sysAuditConfigList: [
      {
        levelName: "", //级别名称
        levelSort: 1, //级别排序
        isEnable: "0", //是否启用0未启用，1已启用
        checkPersonType: "dept", //审核人类型
        checkPersonOrgs: [], //审核人机构
        checkPersonRole: "", //审核角色
        checkPerson: "", //审核人
      },
      {
        levelName: "", //级别名称
        levelSort: 2, //级别排序
        isEnable: "0", //是否启用0未启用，1已启用
        checkPersonType: "dept", //审核人类型
        checkPersonOrgs: [], //审核人机构
        checkPersonRole: "", //审核角色
        checkPerson: "", //审核人
      },
      {
        levelName: "", //级别名称
        levelSort: 3, //级别排序
        isEnable: "0", //是否启用0未启用，1已启用
        checkPersonType: "dept", //审核人类型
        checkPersonOrgs: [], //审核人机构
        checkPersonRole: "", //审核角色
        checkPerson: "", //审核人
      },
    ],
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined,
    status: undefined,
    deptId: undefined,
  },
  rules: {
    auditTitle: [
      { required: true, message: "请输入审核标题", trigger: "blur" },
      { max: 30, message: "长度不能超过30个字符", trigger: "blur" },
    ],
    auditType: [{ required: true, message: "请选择类型", trigger: "change" }],
  },
});
const handleSwitchChange = (row, checked) => {
  // 先记录原始值，用于失败时回滚
  const originalValue = row.isEnable;

  // 设置新值（前端预更新，提升体验）
  row.isEnable = checked ? "1" : "0";

  const params = {
    auditId: row.auditId,
    isEnable: row.isEnable,
  };

  enableOrDeactivate(params)
    .then((res) => {
      if (res.code === 200) {
        proxy.$modal.msgSuccess("操作成功");
      } else {
        // 接口返回失败，回滚状态
        row.isEnable = originalValue;
        proxy.$modal.msgError(res.msg || "操作失败");
      }
    })
    .catch((err) => {
      // 网络错误或异常，也回滚
      row.isEnable = originalValue;
    });
};
const { queryParams, form, rules } = toRefs(data);
// 切换时清空数据
const qingkong = (index) => {
  form.value.sysAuditConfigList[index].checkPersonRole = null;
  form.value.sysAuditConfigList[index].checkPerson = null;
  form.value.sysAuditConfigList[index].checkPersonOrgs = [];
  // if (form.value.sysAuditConfigList[index].checkPersonType == "dept") {
  //   form.value.sysAuditConfigList[index].checkPersonRole = null;
  //   form.value.sysAuditConfigList[index].checkPerson = null;
  // } else if (form.value.sysAuditConfigList[index].checkPersonType == "role") {
  //   form.value.sysAuditConfigList[index].checkPersonOrgs = [];
  //   form.value.sysAuditConfigList[index].checkPerson = null;
  // } else if (
  //   form.value.sysAuditConfigList[index].checkPersonType == "deptRole"
  // ) {
  //   form.value.sysAuditConfigList[index].checkPerson = null;
  // }
};

/** 查询用户列表 */
function getList() {
  loading.value = true;
  auditlist(proxy.addDateRange(queryParams.value, dateRange.value)).then(
    (res) => {
      loading.value = false;
      userList.value = res.rows;
      total.value = res.total;
    }
  );
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.deptId = undefined;
  // proxy.$refs.deptTreeRef.setCurrentKey(null)
  handleQuery();
}

/** 删除按钮操作 */
function handleDelete(row) {
  const userIds = row.auditId || ids.value;
  proxy.$modal
    .confirm("是否确认删除该数据项？")
    .then(function () {
      return delaudit(userIds);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

/** 选择条数  */
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.auditId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 重置操作表单 */
function reset() {
  (form.value = {
    auditTitle: "",
    auditType: "",
    flowDesc: "",
    isEnable: "0",
    sysAuditConfigList: [
      {
        levelName: "一级审核配置", //级别名称
        levelSort: 1, //级别排序
        isEnable: "0", //是否启用0未启用，1已启用
        checkPersonType: "dept", //审核人类型
        checkPersonOrgs: [], //审核人机构
        checkPersonRole: "", //审核角色
        checkPerson: "", //审核人
      },
      {
        levelName: "二级审核配置", //级别名称
        levelSort: 2, //级别排序
        isEnable: "0", //是否启用0未启用，1已启用
        checkPersonType: "dept", //审核人类型
        checkPersonOrgs: [], //审核人机构
        checkPersonRole: "", //审核角色
        checkPerson: "", //审核人
      },
      {
        levelName: "三级审核配置", //级别名称
        levelSort: 3, //级别排序
        isEnable: "0", //是否启用0未启用，1已启用
        checkPersonType: "dept", //审核人类型
        checkPersonOrgs: [], //审核人机构
        checkPersonRole: "", //审核角色
        checkPerson: "", //审核人
      },
    ],
  }),
    proxy.resetForm("userRef");
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 新增按钮操作 */
function handleAdd() {
  reset();

  open.value = true;
  title.value = "新增流程配置";
}
/** 查看按钮操作 */
function handlechek(row) {
  reset();
  const auditId = row.auditId;
  auditroleId(auditId)
    .then((response) => {
      form.value = response.data;
    })
    .then(() => {
      form.value.sysAuditConfigList.forEach((item, index) => {
        if (item.checkPersonOrg) {
          item.checkPersonOrgs = getPathByOrgId(
            jigou.value,
            item.checkPersonOrg
          );
          if (item.checkPersonType == "user") {
            userchakan(index);
          }
        } else {
          item.checkPersonOrgs = [];
        }
      });
      open.value = true;
      title.value = "查看流程配置";
    });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const auditId = row.auditId;
  auditroleId(auditId)
    .then((response) => {
      form.value = response.data;
      form.value.sysAuditConfigList.forEach((item, index) => {
        if (item.checkPersonOrg) {
          item.checkPersonOrgs = getPathByOrgId(
            jigou.value,
            item.checkPersonOrg
          );
          userchakan(index);
        } else {
          item.checkPersonOrgs = [];
        }
      });
    })
    .then(() => {
      open.value = true;
      title.value = "修改流程配置";
    });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["userRef"].validate((valid) => {
    if (valid) {
      form.value.sysAuditConfigList.forEach((item) => {
        item.checkPersonOrg = item.checkPersonOrgs.at(-1);
      });
      if (form.value.auditId != undefined) {
        updataaudit(form.value).then((response) => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        audit(form.value).then((response) => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}
const userlist = ref([]);
// 获取用户
const userchakan = (index) => {
  if (
    form.value.sysAuditConfigList[index].checkPersonOrgs?.length > 0 &&
    form.value.sysAuditConfigList[index].checkPersonRole
  ) {
    getUserList(
      form.value.sysAuditConfigList[index].checkPersonOrgs.at(-1),
      form.value.sysAuditConfigList[index].checkPersonRole
    ).then((res) => {
      userlist.value[index] = res.data;
    });
  }
};
onMounted(() => {
  getList();
  proxy.getConfigKey("sys.user.initPassword").then((response) => {
    initPassword.value = response.msg;
  });
});
</script>
