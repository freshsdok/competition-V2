<template>
<el-dialog :title="`指定用户`"
            v-model="userOpen" 
            width="55%" 
            style="top: 10%;margin-bottom: 110px;"
            append-to-body>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form  :model="queryParams" 
              ref="queryRef" 
              :inline="true" 
              v-show="showSearch" 
              label-width="60px">
       <el-form-item label="手机号" prop="phonenumber">
        <el-input
          v-model="queryParams.phonenumber"
          placeholder="请输入手机号"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 用户表格 -->
    <el-table v-loading="loading" 
              :data="userList"
              ref="userTableRef"
              row-key="userId"
              @selection-change="handleSelectionChange">
      <el-table-column type="selection" reserve-selection width="40" align="center" />
      <el-table-column label="姓名" align="left" prop="label" min-width="120px" >
        <template #default="scope">
          {{getShowName(scope.row)}}
        </template>
      </el-table-column>
      <el-table-column label="手机号" align="left" prop="phonenumber" min-width="110px"/>
      <el-table-column label="邮箱" align="left" prop="email" min-width="140px" show-overflow-tooltip/>
    </el-table>
    
    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"/>
  </div>
  
  <!-- 弹窗底部按钮 -->
  <template #footer>
    <div class="dialog-footer">
      <el-button type="primary" @click="submitForm">确定</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>
  </template>
</el-dialog>
</template>

<script setup name="UserSelect">
// 导入API
import { getWorkUserList } from "@/api/system/user";
// 导入工具
import modal from "@/plugins/modal";

// ************** 弹窗基础配置 **************
// 弹窗显示状态
let userOpen = $ref(false)
// 查询参数
let queryParams = $ref({
  pageNum: 1,  // 当前页码
  pageSize: 10, // 每页条数
  userName: '' // 用户名称
})
// 是否显示搜索区域
let showSearch = $ref(true)

// ************** 表格数据相关 **************

// 选中的用户数组
let selectedUsers = $ref([])
// 表格引用
const userTableRef = ref(null)
// ************** 事件定义 **************
// 定义提交事件
const emit = defineEmits(['confirm'])

// ************** 弹窗控制方法 **************
/**
 * 打开用户选择弹窗
 * @param {Object} info - 弹窗配置信息
 * @param {Array} info.selectedUsers - 已选中的用户数组
 */
let title = $ref('选择用户')
// 用户类型
let userType = $ref('')
let canSelect = $ref(false)
const openDilogUser = (info) => { 
  canSelect = false
  userOpen = true
  // 重置选择状态
  beforeClose()
  title = info.title || '选择用户'
  userType = info.userType || ''
  resetQuery()
  console.log(info.selectedUsers, 'info')
  selectedUsers = info.selectedUsers || []
}
const selectionChange = () => {
  nextTick(() => {
    setTimeout(() => {
      console.log('*************************************************',selectedUsers)
      if (selectedUsers && selectedUsers.length > 0) {
        selectedUsers.forEach(item => {
          userTableRef.value.toggleRowSelection(item, true)
        })
      }
      canSelect = true
    }, 100)
  })
}
// 获取用户名称
const getShowName = (row) => {
  let name = row.realName || row.nickName || row.userName || '-'
  return name
}

/**
 * 关闭弹窗前的处理
 * @description 重置选择状态和表单字段
 */
const beforeClose = () => {
  selectedUsers = []
  // 异步清空表格选择
  nextTick(() => {
    if (userTableRef.value) {
      userTableRef.value.clearSelection()
    }
  })
}

/**
 * 关闭弹窗
 */
const cancel = () => { 
  userOpen = false
  beforeClose()
}

// ************** 搜索相关方法 **************
/**
 * 搜索按钮操作
 * @description 重置页码为1并重新获取列表数据
 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/**
 * 重置按钮操作
 * @description 重置搜索表单并重新查询数据
 */
// 搜索表单引用
const queryRef = ref(null)
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  handleQuery()
}

/**
 * 获取用户列表数据
 * @description 调用API获取用户数据，更新表格和分页信息
 */
// 加载状态
const loading = ref(false)
// 总条数
const total = ref(0)
// 用户数据列表
let userList = $ref([])
function getList() {
  loading.value = true
  // 实际项目中应调用API获取数据
  getWorkUserList({
    ...queryParams,
    userType: userType
  }).then(response => {
     loading.value = false
    if (response.code == 200) {
      userList = response.rows
      total.value = response.total
    }
    // 初始化选中用户
    selectionChange()
  }).catch(() => {
    loading.value = false
  })
}

// ************** 选择相关方法 **************
/**
 * 处理表格选择变化
 * @param {Array} val - 选中的用户数组
 */
const handleSelectionChange = (val) => {
  if(!canSelect) return
  selectedUsers = val
  console.log('handleSelectionChange1111111', val)
}

/**
 * 提交选中的用户
 * @description 验证选择的用户数量，然后触发提交事件
 */
const submitForm = () => {
  if (selectedUsers.length === 0) {
    modal.msgWarning('请选择用户')
    return
  }
  console.log(selectedUsers,'selectedUsers')
  selectedUsers = selectedUsers.map(item => ({
    ...item,
    nickName: getShowName(item) || ''
  }))
  emit('confirm', selectedUsers)
  cancel()
}

// ************** 暴露方法 **************
// 暴露给父组件的方法
defineExpose({
  openDilogUser
})
</script>

<style scoped lang="scss"></style>