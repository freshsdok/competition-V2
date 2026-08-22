<template>
  <div class="app-container">
    <el-form  :model="queryParams" 
              ref="queryRef" 
              :inline="true" 
              v-show="showSearch" 
              label-width="100px">
      <el-form-item label="用户组名称" prop="name">
        <el-input
          v-model.trim="queryParams.name"
          placeholder="请输入用户组名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
       <el-form-item label="用户组管理员" prop="groupManager">
        <el-input
          v-model.trim="queryParams.groupManager"
          placeholder="请输入用户组管理员"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="关联身份" prop="identifyType">
        <el-select  v-model="queryParams.identifyType"
                    placeholder="请选择关联身份"
                    clearable
                    style="width: 200px"> 
          <el-option label="教师（已通过教师认证）" value="teacher" />
          <el-option label="学生（已通过学生认证）" value="student" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
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
          v-hasPermi="['system:userGroup:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" 
              :data="tableData">
      <el-table-column label="用户组名称" align="left" prop="name" min-width="150px" show-overflow-tooltip/>
      <el-table-column label="用户组管理员" align="left" prop="groupManagerList" min-width="150px" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row?.groupManagerList.map(item => item.userName).join(', ') || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="人数" align="left" prop="userIdCount" min-width="50px" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row?.userIdCount }}
        </template>
      </el-table-column>
      <el-table-column label="用户组说明" align="left" prop="descripe" min-width="150px" show-overflow-tooltip/>
      <el-table-column label="创建人" align="left" prop="createUserName" min-width="150px" show-overflow-tooltip/>
      <el-table-column label="创建时间" align="left" prop="createTime" min-width="150px" show-overflow-tooltip/>
      <el-table-column label="操作" align="center" width="110">
        <template #default="scope">
          <el-tooltip content="查看" placement="top">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['system:userGroup:query']"></el-button>
          </el-tooltip>
          <template v-if="scope.row.admin">
            <el-tooltip content="编辑" placement="top">
               <el-button link type="success" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:userGroup:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:userGroup:remove']"></el-button>
            </el-tooltip>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"/>
    
    <DetailDialog ref="detailDialogRef" 
                  @submit="handleQuery"/>

  </div>
</template>

<script setup name="UserGroupList">
// 导入组件和工具
import DetailDialog from "./detailDialog.vue";
import { useDict } from "@/utils/dict";
import modal from "@/plugins/modal";
import { systemUserGroupMangerList, systemUserGroupDelete } from "@/api/fileTask";
// 使用字典
const { banner_module } = useDict('banner_module')

// ************** 筛选相关 **************
// 搜索表单引用
const queryRef = ref(null)
// 查询参数
let queryParams = $ref({
  pageNum: 1,  // 当前页码
  pageSize: 10
})
// 是否显示搜索区域
let showSearch = $ref(true)

/**
 * 搜索按钮操作
 * 重置页码为1并重新获取列表数据
 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/**
 * 重置按钮操作
 * 重置搜索表单并重新查询数据
 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  handleQuery()
}

// ************** 表格数据相关 **************
// 表格数据列表
let tableData = $ref([])
const loading = ref(false)
const total = ref(0)
/**
 * 获取用户组列表数据
 * @description 调用API获取用户组数据，更新表格和分页信息
 */
function getList() {
  console.log('获取用户组列表数据')
  loading.value = true
  // 实际项目中应调用API获取数据
  systemUserGroupMangerList(queryParams).then(response => {
    loading.value = false
    if (response.code == 200) {
      tableData = response.rows
      total.value = response.total
    }
  }).catch(() => {
    loading.value = false 
  })
}

// ************** 弹窗交互相关 **************
// 详情弹窗引用
const detailDialogRef = ref(null)
function handleAdd() {
  detailDialogRef.value.openDilog({
    title: '新增用户组',
  })
}

function handleUpdate(row) {
  detailDialogRef.value.openDilog({
    title: '修改用户组',
    row: row,
  })
}

function handleView(row) {
  detailDialogRef.value.openDilog({
    title: '查看用户组',
    row: row,
    disabled: true,
  })
}

/**
 * 删除按钮操作
 * 提示确认后删除用户组
 * @param {Object} row - 当前行数据
 */
function handleDelete(row) {
  modal.confirm('是否确认删除').then(function() {
    // 实际项目中应调用删除API
    return systemUserGroupDelete(row.id)
  }).then(() => {
    getList()
    modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// ************** 初始化 **************
resetQuery()
</script>
