<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      v-show="showSearch"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="角色名称" prop="roleName">
        <el-input
          v-model.trim="queryParams.roleName"
          placeholder="请输入角色名称"
          clearable
          style="width: 240px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="编码" prop="roleKey">
        <el-input
          v-model.trim="queryParams.roleKey"
          placeholder="请输入编码"
          clearable
          style="width: 240px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          style="width: 240px"
        >
          <el-option
            v-for="dict in sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
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
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:role:add']"
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
          v-hasPermi="['system:role:remove']"
          >删除</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:role:export']"
          >导出</el-button
        >
      </el-col>
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
      <el-table-column label="序号" align="left" type="index" width="50" />
      <el-table-column label="角色编号" prop="roleId" width="120" />
      <el-table-column
        label="角色名称"
        prop="roleName"
        :show-overflow-tooltip="true"
        width="150"
      />
      <el-table-column
        label="编码"
        prop="roleKey"
        :show-overflow-tooltip="true"
        width="150"
      />
      <el-table-column label="显示顺序" prop="roleSort" width="100" />
      <el-table-column label="独立角色" prop="exclusionFlag" width="100">
        <template #default="scope">
          {{ scope.row.exclusionFlag ? "是" : "否" }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" align="center" prop="createTime">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-tooltip
            content="修改"
            placement="top"
            v-if="scope.row.roleId !== 1"
          >
            <el-button
              link
              type="success"
              icon="Edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['system:role:edit']"
            ></el-button>
          </el-tooltip>
          <el-tooltip
            content="分配用户"
            placement="top"
            v-if="scope.row.roleId !== 1"
          >
            <el-button
              link
              type="success"
              icon="User"
              @click="handleAuthUser(scope.row)"
              v-hasPermi="['system:role:edit']"
            ></el-button>
          </el-tooltip>
          <el-tooltip
            content="删除"
            placement="top"
            v-if="scope.row.roleId !== 1"
          >
            <el-button
              link
              type="danger"
              icon="Delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['system:role:remove']"
            ></el-button>
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

    <!-- 添加或修改角色配置对话框 -->
    <el-dialog :title="title" v-model="open" width="1000px" append-to-body>
      <el-form ref="roleRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="角色名称" prop="roleName">
              <el-input
                v-model="form.roleName"
                placeholder="请输入角色名称"
              /> </el-form-item
          ></el-col>
          <el-col :span="8">
            <el-form-item prop="roleKey">
              <template #label>
                <span>
                  <el-tooltip
                    content="控制器中定义的编码，如：@PreAuthorize(`@ss.hasRole('admin')`)"
                    placement="top"
                  >
                    <el-icon><question-filled /></el-icon>
                  </el-tooltip>
                  编码
                </span>
              </template>
              <el-input v-model="form.roleKey" placeholder="请输入编码" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="角色顺序" prop="roleSort">
              <el-input-number
                v-model="form.roleSort"
                controls-position="right"
                :min="0"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="角色状态">
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
          <el-col :span="8">
            <el-form-item label="是否锁定">
              <el-radio-group v-model="form.lockFlag">
                <el-radio :value="true">是</el-radio>
                <el-radio :value="false">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="授权方式" @change="dataScopeSelectChange">
              <el-checkbox v-model="form.studentFlag">认证学生</el-checkbox>
              <el-checkbox v-model="form.teacherFlag">认证老师</el-checkbox>
              <el-checkbox v-model="form.authFlag">已实名认证</el-checkbox>
              <el-checkbox v-model="form.competitionFlag">比赛用户</el-checkbox>
              <el-checkbox v-model="form.captainFlag">比赛队长</el-checkbox>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否独立角色" @change="dataScopeSelectChange">
              <el-checkbox v-model="form.exclusionFlag">独立角色</el-checkbox>
            </el-form-item>
          </el-col>
          <!-- <el-col :span="12" v-if="form.exclusionFlag">
            <el-form-item label="级别">
              <el-select
                v-model="form.exclusionLevel"
                placeholder="独立角色级别"
                clearable
              >
                <el-option
                  v-for="dict in exclusion_level"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col> -->
          <el-col :span="24">
            <el-form-item label="菜单权限">
              <el-checkbox
                v-model="menuExpand"
                @change="handleCheckedTreeExpand($event, 'menu')"
                >展开/折叠</el-checkbox
              >
              <el-checkbox
                v-model="menuNodeAll"
                @change="handleCheckedTreeNodeAll($event, 'menu')"
                >全选/全不选</el-checkbox
              >
              <el-checkbox
                v-model="form.menuCheckStrictly"
                @change="handleCheckedTreeConnect($event, 'menu')"
                >父子联动</el-checkbox
              >
              <el-tree
                class="tree-border"
                :data="menuOptions"
                show-checkbox
                ref="menuRef"
                node-key="id"
                :expand-on-click-node="false"
                :check-strictly="!form.menuCheckStrictly"
                empty-text="加载中，请稍候"
                :props="{ label: 'label', children: 'children' }"
              >
                <template #default="{ node, data }">
                  <div class="tree">
                    <span>{{ node.label }}</span>
                    <div v-if="data.menuType == 'C'">
                      <el-select style="width: 240px" v-model="data.dataScope">
                        <el-option
                          v-for="item in dataScopeOptions"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        ></el-option>
                      </el-select>
                    </div>
                  </div>
                </template>
              </el-tree>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述">
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
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Role">
import {
  addRole,
  changeRoleStatus,
  dataScope,
  delRole,
  getRole,
  listRole,
  updateRole,
  deptTreeSelect,
} from "@/api/system/role";
import {
  roleMenuTreeselectNew,
  treeselectNoPc,
} from "@/api/system/menu";

const router = useRouter();
const { proxy } = getCurrentInstance();
const { sys_normal_disable, exclusion_level } = proxy.useDict(
  "sys_normal_disable",
  "exclusion_level"
);

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
const dataScopeOptions = ref([
  { value: "1", label: "全部数据权限" },
  // { value: "2", label: "自定数据权限" },
  { value: "3", label: "本机构数据权限" },
  { value: "4", label: "本机构及以下数据权限" },
  { value: "5", label: "仅本人数据权限" },
]);
const dataScopeSelectChange = () => {
  console.log(form.value);
};
const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    roleName: undefined,
    roleKey: undefined,
    status: undefined,
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
  listRole(proxy.addDateRange(queryParams.value, dateRange.value)).then(
    (response) => {
      roleList.value = response.rows;
      total.value = response.total;
      loading.value = false;
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
  dateRange.value = [];
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 删除按钮操作 */
function handleDelete(row) {
  const roleIds = row.roleId || ids.value;
  proxy.$modal
    .confirm('是否确认删除角色编号为"' + roleIds + '"的数据项?')
    .then(function () {
      return delRole(roleIds);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download(
    "system/role/export",
    {
      ...queryParams.value,
    },
    `角色信息统计.xlsx`
  );
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.roleId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 角色状态修改 */
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用";
  proxy.$modal
    .confirm('确认要"' + text + '""' + row.roleName + '"角色吗?')
    .then(function () {
      return changeRoleStatus(row.roleId, row.status);
    })
    .then(() => {
      proxy.$modal.msgSuccess(text + "成功");
    })
    .catch(function () {
      row.status = row.status === "0" ? "1" : "0";
    });
}

/** 分配用户 */
function handleAuthUser(row) {
  router.push("/system/role-auth/user/" + row.roleId);
}

/** 查询菜单树结构 */
function getMenuTreeselect() {
  treeselectNoPc().then((response) => {
    menuOptions.value = response.data;
  });
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

/** 添加角色 */
function handleAdd() {
  reset();
  getMenuTreeselect();
  open.value = true;
  title.value = "添加角色";
}

/** 修改角色 */
function handleUpdate(row) {
  reset();
  const roleId = row.roleId || ids.value;
  const roleMenu = getRoleMenuTreeselect(roleId);
  getRole(roleId).then((response) => {
    form.value = response.data;
    form.value.roleSort = Number(form.value.roleSort);
    open.value = true;
    nextTick(() => {
      roleMenu.then((res) => {
        let checkedKeys = res.checkedKeys;
        checkedKeys.forEach((v) => {
          nextTick(() => {
            menuRef.value.setChecked(v.menuId, true, false);
          });
        });
      });
    });
  });
  title.value = "修改角色";
}

/** 根据角色ID查询菜单树结构 */
function getRoleMenuTreeselect(roleId) {
  return roleMenuTreeselectNew(roleId).then((response) => {
    menuOptions.value = response.menus;
    return response;
  });
}

/** 根据角色ID查询机构树结构 */
function getDeptTree(roleId) {
  return deptTreeSelect(roleId).then((response) => {
    deptOptions.value = response.depts;
    return response;
  });
}

/** 树权限（展开/折叠）*/
function handleCheckedTreeExpand(value, type) {
  if (type == "menu") {
    let treeList = menuOptions.value;
    for (let i = 0; i < treeList.length; i++) {
      menuRef.value.store.nodesMap[treeList[i].id].expanded = value;
    }
  } else if (type == "dept") {
    let treeList = deptOptions.value;
    for (let i = 0; i < treeList.length; i++) {
      deptRef.value.store.nodesMap[treeList[i].id].expanded = value;
    }
  }
}

/** 树权限（全选/全不选） */
function handleCheckedTreeNodeAll(value, type) {
  if (type == "menu") {
    menuRef.value.setCheckedNodes(value ? menuOptions.value : []);
  } else if (type == "dept") {
    deptRef.value.setCheckedNodes(value ? deptOptions.value : []);
  }
}

/** 树权限（父子联动） */
function handleCheckedTreeConnect(value, type) {
  if (type == "menu") {
    form.value.menuCheckStrictly = value ? true : false;
  } else if (type == "dept") {
    form.value.deptCheckStrictly = value ? true : false;
  }
}

/** 所有菜单节点数据（包含半选节点） */
function getMenuAllCheckedKeys() {
  const checkedNodes = menuRef.value.getCheckedNodes(); // 全选节点
  const halfCheckedNodes = menuRef.value.getHalfCheckedNodes(); // 半选节点

  // 合并：全选 + 半选
  const allCheckedNodes = [...checkedNodes, ...halfCheckedNodes];

  // 提取 id 和 dataScope
  const result = allCheckedNodes.map((node) => ({
    menuId: node.id,
    dataScope: node.dataScope,
  }));

  return result;
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["roleRef"].validate((valid) => {
    if (valid) {
      if (form.value.roleId != undefined) {
        form.value.menuIds = getMenuAllCheckedKeys();
        updateRole(form.value).then((response) => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        form.value.menuIds = getMenuAllCheckedKeys();
        addRole(form.value).then((response) => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
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