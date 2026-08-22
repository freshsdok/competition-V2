<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="详情标题" prop="detailTitle">
        <el-input
          v-model="queryParams.detailTitle"
          placeholder="请输入详情标题"
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
          v-hasPermi="['content:detail:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['content:detail:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['content:detail:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['content:detail:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="detailList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="详情标题" align="center" prop="detailTitle" />
      <el-table-column label="栏目ID" align="center" prop="columnId" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['content:detail:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['content:detail:remove']">删除</el-button>
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

    <!-- 添加或修改详情对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="栏目ID" prop="columnId">
          <el-input-number v-model="form.columnId" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="详情标题" prop="detailTitle">
          <el-input v-model="form.detailTitle" placeholder="请输入详情标题" />
        </el-form-item>
        <el-form-item label="详情内容" prop="detailContent">
          <el-input v-model="form.detailContent" type="textarea" :rows="10" placeholder="请输入详情内容" />
        </el-form-item>
        <el-form-item label="详情图片" prop="detailImage">
          <el-input v-model="form.detailImage" placeholder="请输入详情图片URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">提 交</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ContentDetail">
import { listContentDetail, getContentDetail, addContentDetail, updateContentDetail, delContentDetail } from '@/api/content/detail'
import { parseTime } from '@/utils/ruoyi'

const router = useRouter()
const { proxy } = getCurrentInstance()

const detailList = ref([])
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
    detailTitle: null,
    columnId: null
  },
  rules: {
    columnId: [{ required: true, message: '栏目ID不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const formRef = ref()
const queryRef = ref()

/** 查询详情列表 */
function getList() {
  loading.value = true
  listContentDetail(queryParams.value).then(response => {
    detailList.value = response.rows
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
  ids.value = selection.map(item => item.detailId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加详情'
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const detailId = row.detailId || ids.value[0]
  getContentDetail(detailId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改详情'
  })
}

/** 提交按钮 */
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      if (form.value.detailId != null) {
        updateContentDetail(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addContentDetail(form.value).then(response => {
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
  const detailIds = row.detailId || ids.value
  proxy.$modal.confirm('是否确认删除详情编号为"' + detailIds + '"的数据项?').then(function() {
    return delContentDetail(detailIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('contentDetail/export', {
    ...queryParams.value
  }, `detail_${new Date().getTime()}.xlsx`)
}

function reset() {
  form.value = {
    detailId: null,
    columnId: null,
    detailTitle: null,
    detailContent: null,
    detailImage: null
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
