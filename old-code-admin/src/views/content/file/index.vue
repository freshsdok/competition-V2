<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="文件名称" prop="fileName">
        <el-input
          v-model="queryParams.fileName"
          placeholder="请输入文件名称"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="栏目ID" prop="columnId">
        <el-input
          v-model="queryParams.columnId"
          placeholder="请输入栏目ID"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
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
          v-hasPermi="['content:file:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['content:file:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['content:file:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['content:file:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="fileList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="文件名称" align="center" prop="fileName" />
      <el-table-column label="栏目ID" align="center" prop="columnId" />
      <el-table-column label="文件类型" align="center" prop="fileType" />
      <el-table-column label="文件大小" align="center" prop="fileSize">
        <template #default="scope">
          <span>{{ formatFileSize(scope.row.fileSize) }}</span>
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
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['content:file:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['content:file:remove']">删除</el-button>
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

    <!-- 添加或修改文件对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="栏目ID" prop="columnId">
          <el-input-number v-model="form.columnId" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="文件名称" prop="fileName">
          <el-input v-model="form.fileName" placeholder="请输入文件名称" />
        </el-form-item>
        <el-form-item label="文件URL" prop="fileUrl">
          <el-input v-model="form.fileUrl" placeholder="请输入文件URL" />
        </el-form-item>
        <el-form-item label="文件类型" prop="fileType">
          <el-input v-model="form.fileType" placeholder="请输入文件类型" />
        </el-form-item>
        <el-form-item label="文件大小" prop="fileSize">
          <el-input-number v-model="form.fileSize" controls-position="right" :min="0" />
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

<script setup name="ContentFile">
import { listContentFile, getContentFile, addContentFile, updateContentFile, delContentFile } from '@/api/content/file'
import { useDict } from '@/utils/dict'
import { parseTime } from '@/utils/ruoyi'

const { sys_status } = useDict('sys_status')

const router = useRouter()
const { proxy } = getCurrentInstance()

const fileList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    fileName: null,
    columnId: null,
    status: null
  },
  rules: {
    columnId: [{ required: true, message: '栏目ID不能为空', trigger: 'blur' }],
    fileName: [{ required: true, message: '文件名称不能为空', trigger: 'blur' }],
    fileUrl: [{ required: true, message: '文件URL不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const formRef = ref()
const queryRef = ref()

/** 查询文件列表 */
function getList() {
  loading.value = true
  listContentFile(queryParams.value).then(response => {
    fileList.value = response.rows
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
  ids.value = selection.map(item => item.fileId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加文件'
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const fileId = row.fileId || ids.value[0]
  getContentFile(fileId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改文件'
  })
}

/** 提交按钮 */
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      if (form.value.fileId != null) {
        updateContentFile(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addContentFile(form.value).then(response => {
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
  const fileIds = row.fileId || ids.value
  proxy.$modal.confirm('是否确认删除文件编号为"' + fileIds + '"的数据项?').then(function() {
    return delContentFile(fileIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('contentFile/export', {
    ...queryParams.value
  }, `file_${new Date().getTime()}.xlsx`)
}

/** 格式化文件大小 */
function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

function reset() {
  form.value = {
    fileId: null,
    columnId: null,
    fileName: null,
    fileUrl: null,
    fileType: null,
    fileSize: null,
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
})
</script>
