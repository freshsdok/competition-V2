<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="栏目名称" prop="columnName">
        <el-input
          v-model.trim="queryParams.columnName"
          placeholder="请输入栏目名称"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="栏目编码" prop="columnCode">
        <el-input
          v-model.trim="queryParams.columnCode"
          placeholder="请输入栏目编码"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="栏目类型" prop="columnType">
        <el-select v-model="queryParams.columnType" placeholder="请选择栏目类型" clearable style="width: 200px;">
          <el-option label="内容列表" value="1" />
          <el-option label="文件列表" value="2" />
          <el-option label="详情页" value="3" />
          <el-option label="文件下载" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px;">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
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
          v-hasPermi="['content:column:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['content:column:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['content:column:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="columnList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="栏目名称" align="center" prop="columnName" />
      
      <el-table-column label="栏目类型" align="center" prop="columnType">
        <template #default="scope">
          <el-tag v-if="scope.row.columnType === '1'" type="success">内容列表</el-tag>
          <el-tag v-else-if="scope.row.columnType === '2'" type="info">文件列表</el-tag>
          <el-tag v-else-if="scope.row.columnType === '3'" type="warning">详情页</el-tag>
          <el-tag v-else-if="scope.row.columnType === '4'" type="danger">文件下载</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="绑定菜单" align="center" prop="menuId" width="120">
        <template #default="scope">
          <span v-if="scope.row.menuId">{{ getMenuName(scope.row.menuId) }}</span>
          <span v-else style="color: #999;">未绑定</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="sys_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="orderNum" width="80" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['content:column:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['content:column:remove']">删除</el-button>
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

    <!-- 添加或修改栏目对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="栏目名称" prop="columnName">
          <el-input v-model="form.columnName" placeholder="请输入栏目名称" />
        </el-form-item>
        
        <el-form-item label="栏目类型" prop="columnType">
          <el-select v-model="form.columnType" placeholder="请选择栏目类型">
            <el-option label="内容列表" value="1" />
            <el-option label="文件列表" value="2" />
            <el-option label="详情页" value="3" />
            <el-option label="文件下载" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定菜单" prop="menuId">
          <el-tree-select
            v-model="form.menuId"
            :data="menuOptions"
            :props="{ children: 'children', label: 'label', value: 'id' }"
            value-key="id"
            placeholder="选择绑定的菜单"
            check-strictly
            clearable
          />
        </el-form-item>
        
        <el-form-item label="栏目描述" prop="columnDesc">
          <el-input v-model="form.columnDesc" type="textarea" placeholder="请输入栏目描述" />
        </el-form-item>
        <el-form-item label="排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">提 交</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ContentColumn">
import { listContentColumn, getContentColumn, addContentColumn, updateContentColumn, delContentColumn } from '@/api/content/column'
import { pcTreeselect } from '@/api/system/menu'
import { useDict } from '@/utils/dict'
import { parseTime } from '@/utils/ruoyi'

const { sys_status } = useDict('sys_status')

const router = useRouter()
const { proxy } = getCurrentInstance()

const columnList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const columnOptions = ref([])
const menuOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    columnName: null,
    columnCode: null,
    columnType: null,
    status: null
  },
  rules: {
    columnName: [{ required: true, message: '栏目名称不能为空', trigger: 'blur' }],
    columnType: [{ required: true, message: '栏目类型不能为空', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const formRef = ref()
const queryRef = ref()

/** 查询栏目列表 */
function getList() {
  loading.value = true
  listContentColumn(queryParams.value).then(response => {
    columnList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  queryRef.value.resetFields()
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.columnId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  getColumnTree()
  getMenuTree()
  open.value = true
  title.value = '添加栏目'
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const columnId = row.columnId || ids.value[0]
  getContentColumn(columnId).then(response => {
    form.value = response.data
    getColumnTree()
    getMenuTree()
    open.value = true
    title.value = '修改栏目'
  })
}

/** 提交按钮 */
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      if (form.value.columnId != null) {
        updateContentColumn(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addContentColumn(form.value).then(response => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const columnIds = row.columnId || ids.value
  proxy.$modal.confirm('是否确认删除栏目编号为"' + columnIds + '"的数据项?').then(function() {
    return delContentColumn(columnIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('contentColumn/export', {
    ...queryParams.value
  }, `column_${new Date().getTime()}.xlsx`)
}

/** 获取栏目树 */
function getColumnTree() {
  listContentColumn({}).then(response => {
    columnOptions.value = proxy.handleTree(response.rows, 'columnId', 'parentId')
  })
}

/** 获取菜单树 */
function getMenuTree() {
  // 传递 platformType 参数，让后端直接过滤出 pc 类型的菜单
  pcTreeselect({ }).then(response => {
    menuOptions.value = response.data || []
  })
}

/** 递归过滤菜单树，只保留 platform_type 为指定值的菜单 */
function filterMenuByPlatformType(menus, platformType) {
  if (!menus || menus.length === 0) {
    return []
  }
  
  return menus
    .map(menu => {
      // 深拷贝菜单对象，避免修改原数据
      const newMenu = { ...menu }
      
      // 如果有子节点，先递归处理子节点
      if (newMenu.children && newMenu.children.length > 0) {
        newMenu.children = filterMenuByPlatformType(newMenu.children, platformType)
      }
      
      // 判断该菜单是否应该保留
      // 1. 如果菜单的 platformType 匹配，保留
      // 2. 如果菜单有符合条件的子节点，保留（即使自身不匹配）
      const hasMatchingChildren = newMenu.children && newMenu.children.length > 0
      const isMatchingMenu = newMenu.platformType === platformType
      
      return (isMatchingMenu || hasMatchingChildren) ? newMenu : null
    })
    .filter(menu => menu !== null) // 过滤掉 null 值
}

/** 根据菜单ID获取菜单名称 */
function getMenuName(menuId) {
  if (!menuId) return ''
  const findMenu = (menus) => {
    for (let menu of menus) {
      if (menu.id === menuId) {
        return menu.label
      }
      if (menu.children && menu.children.length > 0) {
        const result = findMenu(menu.children)
        if (result) return result
      }
    }
    return null
  }
  return findMenu(menuOptions.value) || '菜单不存在'
}

function reset() {
  form.value = {
    columnId: null,
    columnName: null,
    columnCode: null,
    columnType: null,
    menuId: null,
    parentId: 0,
    columnDesc: null,
    orderNum: 0,
    status: '0'
  }
  formRef.value?.clearValidate()
}

function cancel() {
  open.value = false
  reset()
}

onMounted(() => {
  getList()
  getMenuTree()
})
</script>
