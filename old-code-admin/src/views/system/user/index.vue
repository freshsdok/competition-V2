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
          label-width="100px"
        >
          <el-form-item label="用户账号" prop="userName">
            <el-input
              v-model.trim="queryParams.userName"
              placeholder="请输入用户账号"
              clearable
              style="width: 180px"
              @change="handleQuery"
            />
          </el-form-item>
          <el-form-item label="姓名" prop="realName">
            <el-input
              v-model.trim="queryParams.realName"
              placeholder="请输入姓名"
              clearable
              style="width: 180px"
              @change="handleQuery"
            />
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-select
              v-model="queryParams.sex"
              placeholder="请选择性别"
              clearable
              style="width: 180px"
              @change="handleQuery"
            >
              <el-option
                v-for="dict in sys_user_sex"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="用户类型" prop="userType">
            <el-select
              v-model="queryParams.userType"
              placeholder="请选择用户类型"
              clearable
              style="width: 180px"
              @change="handleQuery"
            >
              <el-option
                v-for="dict in user_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="手机号码" prop="phonenumber">
            <el-input
              v-model.trim="queryParams.phonenumber"
              placeholder="请输入手机号码"
              clearable
              style="width: 180px"
              @change="handleQuery"
            />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model.trim="queryParams.email"
              placeholder="请输入邮箱"
              clearable
              style="width: 180px"
              @change="handleQuery"
            />
          </el-form-item>
          <el-form-item label="用户来源" prop="userSources">
            <el-select
              v-model="queryParams.userSources"
              placeholder="请选择用户来源"
              clearable
              @change="handleQuery"
              style="width: 180px"
            >
              <el-option
                v-for="dict in user_sources"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="实名认证状态" prop="authStatus">
            <el-select
              v-model="queryParams.authStatus"
              placeholder="请选择实名认证状态"
              clearable
              @change="handleQuery"
              style="width: 180px"
            >
              <el-option
                v-for="dict in real_name_auth_status"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="身份认证类型" prop="certificationType">
            <el-select
              v-model="queryParams.certificationType"
              placeholder="请选择身份认证类型"
              clearable
              @change="handleQuery"
              style="width: 180px"
            >
              <el-option
                v-for="dict in certification_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="账号状态" prop="status">
            <el-select
              v-model="queryParams.status"
              placeholder="请选择账号状态"
              clearable
              @change="handleQuery"
              style="width: 180px"
            >
              <el-option
                v-for="dict in sys_normal_disable"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="所在学校" prop="schoolName">
            <el-input
              v-model.trim="queryParams.schoolName"
              placeholder="请输入所在学校"
              clearable
              style="width: 180px"
              @change="handleQuery"
            />
          </el-form-item>
          <el-form-item label="职务" prop="position">
            <el-input
              v-model.trim="queryParams.position"
              placeholder="请输入职务"
              clearable
              style="width: 180px"
              @change="handleQuery"
            />
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['system:user:add']"
            >新增</el-button
          >
          <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="!ids.length"
            @click="handleDelete"
            v-hasPermi="['system:user:remove']"
            >删除</el-button
          >
          <el-button
            type="success"
            plain
            icon="CopyDocument"
            @click="exportExcel('all')"
            v-hasPermi="['system:user:export']"
            >导出所有</el-button
          >
          <el-button
            type="success"
            plain
            icon="CopyDocument"
            @click="exportExcel('filter')"
            v-hasPermi="['system:user:export']"
            >导出检索结果</el-button
          >
          <el-button
            type="primary"
            icon="Search"
            @click="handleQuery"
            style="margin-left: 100px"
            >搜索</el-button
          >
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-row>

        <vxe-table
          :data="userList"
          border="inner"
          v-loading="loading"
          :header-cell-style="{
            fontSize: '13px',
          }"
          :cell-style="{
            fontSize: '13px',
          }"
          @checkbox-change="handleSelectionChange"
          @checkbox-all="handleSelectionChange"
          style="width: 100%"
        >
          <vxe-column type="checkbox" width="30" align="left" />
          <vxe-column
            title="用户账号"
            align="left"
            field="userName"
            width="100"
          />
          <vxe-column title="姓名" align="left" field="realName" width="60">
            <template #default="{ row }">
              {{ row?.authInfo?.realName || row?.nickName || "-" }}
            </template>
          </vxe-column>
          <vxe-column title="性别" align="left" field="sex" width="46">
            <template #default="{ row }">
              <div v-if="row.sex == 0">男</div>
              <div v-else-if="row.sex == 1">女</div>
              <div v-else>未知</div>
            </template>
          </vxe-column>
          <vxe-column title="用户类型" align="left" field="userType" width="70">
            <template #default="{ row }">
              <dict-tag :options="user_type" :value="row.userType" />
            </template>
          </vxe-column>

          <vxe-column
            title="手机号码"
            align="left"
            field="phonenumber"
            width="95"
          >
            <template #default="{ row }">
              {{ row.phonenumber || "-" }}
            </template>
          </vxe-column>
          <vxe-column title="邮箱" align="left" field="email" min-width="140">
            <template #default="{ row }">
              {{ row.email || "-" }}
            </template>
          </vxe-column>
          <vxe-column
            title="用户来源"
            align="left"
            field="userSources"
            width="75"
          >
            <template #default="{ row }">
              <dict-tag :options="user_sources" :value="row.userSources" />
            </template>
          </vxe-column>
          <vxe-column
            title="所在学校"
            align="left"
            field="schoolName"
            min-width="120"
          >
            <template #default="{ row }">
              {{ row?.schoolName || "-" }}
            </template>
          </vxe-column>
          <vxe-column title="职务" align="left" field="position" width="70">
            <template #default="{ row }">
              {{ row?.position || "-" }}
            </template>
          </vxe-column>
          <vxe-column title="账号状态" align="left" width="70" field="status">
            <template #default="{ row }">
              <dict-tag :options="sys_normal_disable" :value="row.status" />
            </template>
          </vxe-column>
          <vxe-column
            title="实名认证状态"
            align="left"
            field="authStatus"
            width="95"
          >
            <template #default="{ row }">
              <dict-tag
                :options="real_name_auth_status"
                :value="row?.authInfo?.authStatus"
              />
            </template>
          </vxe-column>
          <vxe-column
            title="身份认证类型"
            align="center"
            field="identityInfoList"
            width="95"
          >
            <template #default="{ row }">
              <div v-if="row.identityInfoList.length > 0">
                <dict-tag
                  v-for="(item, index) in row.identityInfoList"
                  :key="index"
                  :options="certification_type"
                  :value="item.certificationType"
                />
              </div>
              <div v-else>暂未认证</div>
              <!-- {{ scope.row.identityInfoList.length>0?scope.row.identityInfoList[0].certificationType:'-'}} -->
            </template>
          </vxe-column>

          <vxe-column
            title="操作"
            align="center"
            width="auto"
            class-name="small-padding fixed-width"
          >
            <template #default="{ row }">
              <el-tooltip content="查看" placement="top">
                <el-button
                  link
                  type="primary"
                  icon="View"
                  @click="disphandleUpdate(row)"
                ></el-button>
              </el-tooltip>
              <template v-if="row.userId !== 1">
                <el-tooltip content="编辑" placement="top">
                  <el-button
                    link
                    type="success"
                    icon="Edit"
                    @click="handleUpdate(row)"
                    v-hasPermi="['system:user:edit']"
                  ></el-button>
                </el-tooltip>
                <el-tooltip content="重置密码" placement="top">
                  <el-button
                    link
                    type="warning"
                    icon="Unlock"
                    @click="handleResetPwd(row)"
                    v-hasPermi="['system:user:resetPwd']"
                  ></el-button>
                </el-tooltip>
                <el-tooltip content="分配角色" placement="top">
                  <el-button
                    link
                    type="success"
                    icon="User"
                    @click="handleAuthRole(row)"
                    v-hasPermi="['system:user:assignRoles']"
                  ></el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button
                    link
                    type="danger"
                    icon="Delete"
                    @click="handleDelete(row)"
                    v-hasPermi="['system:user:remove']"
                  ></el-button>
                </el-tooltip>
              </template>
            </template>
          </vxe-column>
        </vxe-table>
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
    <el-dialog
      :title="title"
      v-model="open"
      width="600px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form
        :model="form"
        :rules="rules"
        ref="userRef"
        label-width="80px"
        :disabled="chakan"
      >
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户账号" prop="userName">
              <el-input
                v-model="form.userName"
                placeholder="请输入用户账号"
                maxlength="30"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickName">
              <el-input
                v-model="form.nickName"
                placeholder="请输入昵称"
                :disabled="title == '修改用户'"
                maxlength="30"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="title == '查看用户'">
            <el-form-item label="姓名">
              <el-input
                v-model="form.realName"
                placeholder="请输入姓名"
                maxlength="30"
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
            <el-form-item label="机构">
              <el-cascader
                :options="deptList"
                placeholder="请选择机构"
                :props="props1"
                clearable
                v-model="form.orgId"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phonenumber">
              <el-input
                v-model="form.phonenumber"
                placeholder="请输入手机号码"
                maxlength="11"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="title == '添加用户'">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                placeholder="请输入密码"
                type="password"
                maxlength="20"
                show-password
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户性别">
              <el-select v-model="form.sex" placeholder="请选择用户性别">
                <el-option
                  v-for="dict in sys_user_sex"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户类型" prop="userType">
              <el-select v-model="form.userType" placeholder="请选择用户类型">
                <el-option
                  v-for="dict in user_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="title != '添加用户'">
            <el-form-item label="账号状态">
              <el-select v-model="form.status" placeholder="请选择账号状态">
                <el-option
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="title == '查看用户'">
            <el-form-item label="用户来源">
              <el-select
                v-model="form.userSources"
                placeholder="请选择用户来源"
              >
                <el-option
                  v-for="dict in user_sources"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input
                v-model="form.remark"
                type="textarea"
                placeholder="请输入内容"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer" v-if="!chakan">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
        <div class="dialog-footer" v-else>
          <el-button @click="cancel">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="User">
import useAppStore from "@/store/modules/app";
import {
  listUser,
  resetUserPwd,
  delUser,
  getUser,
  updateUser,
  addUser,
} from "@/api/system/user";
import { getPathByOrgId, getPathByOrg } from "@/utils/auth";
import { listDeptNoAuth } from "@/api/system/dept";
import { download } from "@/utils/request";

const router = useRouter();
const appStore = useAppStore();
const { proxy } = getCurrentInstance();
const userList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const {
  sys_user_sex,
  identity_status,
  real_name_auth_status,
  user_type,
  certification_type,
  sys_normal_disable,
  user_sources,
} = proxy.useDict(
  "sys_user_sex",
  "identity_status",
  "real_name_auth_status",
  "user_type",
  "certification_type",
  "sys_normal_disable",
  "user_sources"
);
const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined,
    status: undefined,
    deptId: undefined,
    userType: undefined,
    authStatus: undefined,
    certificationType: undefined,
    schoolName: undefined,
    position: undefined,
  },
  rules: {
    userName: [
      { required: true, message: "请输入用户账号", trigger: "blur" },
      {
        min: 2,
        max: 30,
        message: "用户账号长度为 2 到 30 个字符",
        trigger: "blur",
      },
    ],

    nickName: [
      { required: true, message: "请输入昵称", trigger: "blur" },
      {
        min: 2,
        max: 30,
        message: "昵称长度为 2 到 30 个字符",
        trigger: "blur",
      },
    ],
    email: [
      { required: true, message: "请输入邮箱地址", trigger: "blur" },
      {
        type: "email",
        message: "请输入正确的邮箱地址",
        trigger: ["blur", "change"],
      },
    ],
    phonenumber: [
      { required: true, message: "请输入手机号码", trigger: "blur" },
      {
        pattern: /^1[3-9]\d{9}$/,
        message: "请输入正确的手机号码",
        trigger: "blur",
      },
    ],
    password: [
      { required: true, message: "请输入密码", trigger: "blur" },
      {
        min: 6,
        max: 20,
        message: "密码长度为 6 到 20 个字符",
        trigger: "blur",
      },
      // 可选：增加复杂度校验
      // { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
    ],
    sex: [{ required: true, message: "请选择用户性别", trigger: "change" }],
    userType: [
      { required: true, message: "请选择用户类型", trigger: "change" },
    ],
    orgId: [{ required: true, message: "请选择机构", trigger: "change" }],
    remark: [
      // 备注可选，但可以限制长度
      { max: 200, message: "备注不能超过 200 个字符", trigger: "blur" },
    ],
  },
});

const { queryParams, form, rules } = toRefs(data);
/** 查询机构列表 */
const props1 = {
  checkStrictly: true,
  label: "orgName",
  value: "orgId",
};
const deptList = ref([]);
const jigou = ref([]);
function getdeptList() {
  const params = {
    status: 0,
  };
  listDeptNoAuth(params).then((response) => {
    jigou.value = response.rows;
    deptList.value = proxy.handleTree(response.rows, "orgId");
    getList();
  });
}
getdeptList();
/** 查询用户列表 */
function getList() {
  ids.value = [];
  loading.value = true;
  listUser(queryParams.value).then((res) => {
    loading.value = false;
    userList.value = res.rows;
    userList.value.forEach((item) => {
      item.orgIdss = getPathByOrg(jigou.value, item.orgId);
    });
    console.log(userList.value);
    total.value = res.total;
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
  proxy.resetForm("queryRef");

  handleQuery();
}

/** 删除按钮操作 */
function handleDelete(row) {
  const userIds = row.userId || ids.value;
  proxy.$modal
    .confirm("是否确认删除该用户？")
    .then(function () {
      return delUser(userIds);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

/** 跳转角色分配 */
function handleAuthRole(row) {
  const userId = row.userId;
  router.push("/system/user-auth/role/" + userId);
}

/** 重置密码按钮操作 */
function handleResetPwd(row) {
  proxy
    .$prompt('请输入"' + row.userName + '"的新密码', "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      closeOnClickModal: false,
      inputPattern: /^.{5,20}$/,
      inputErrorMessage: "用户密码长度必须介于 5 和 20 之间",
      inputValidator: (value) => {
        if (/<|>|"|'|\||\\/.test(value)) {
          return "不能包含非法字符：< > \" ' \\ |";
        }
      },
    })
    .then(({ value }) => {
      resetUserPwd(row.userId, value).then((response) => {
        proxy.$modal.msgSuccess("修改成功，新密码是：" + value);
      });
    })
    .catch(() => {});
}

/** 选择条数  */
function handleSelectionChange({ records }) {
  console.log(records, "selectionselectionselectionselection");
  ids.value = records.map((item) => item.userId);
}

/** 重置操作表单 */
function reset() {
  form.value = {};
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
  chakan.value = false;
  title.value = "添加用户";
}
const chakan = ref(false);
/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const userId = row.userId || ids.value;
  getUser(userId).then((response) => {
    if (response.code == 200) {
      form.value = response.data;
      form.value.orgId = getPathByOrgId(jigou.value, form.value.orgId);

      open.value = true;
      title.value = "修改用户";
      form.password = "";
      chakan.value = false;
    }
  });
}

/** 查看按钮操作 */
function disphandleUpdate(row) {
  reset();
  const userId = row.userId || ids.value;
  getUser(userId).then((response) => {
    if (response.code == 200) {
      form.value = response.data;
      form.value.orgId = getPathByOrgId(jigou.value, form.value.orgId);

      open.value = true;
      title.value = "查看用户";
      form.password = "";
      chakan.value = true;
    }
  });
}
/** 提交按钮 */
function submitForm() {
  proxy.$refs["userRef"].validate((valid) => {
    if (valid) {
      try {
        if (Array.isArray(form.value.orgId) && form.value.orgId.length > 0) {
          form.value.orgId = form.value.orgId.at(-1);
        }
        if (form.value.orgId.length == 0) {
          delete form.value.orgId;
        }
      } catch (e) {}
      if (form.value.userId != undefined) {
        updateUser(form.value).then((response) => {
          if (response.code == 200) {
            proxy.$modal.msgSuccess("修改成功");
            open.value = false;
            getList();
          }
        });
      } else {
        addUser(form.value).then((response) => {
          if (response.code == 200) {
            proxy.$modal.msgSuccess("新增成功");
            open.value = false;
            getList();
          }
        });
      }
    }
  });
}

/** 导出按钮操作 */
const exportExcel = (type) => {
  download(
    "system/user/export",
    {
      ...queryParams.value,
      exportType: type,
    },
    `用户信息统计.xlsx`
  );
};
</script>
