<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="数据源名称" prop="dataName">
        <el-input
          v-model.trim="queryParams.dataName"
          placeholder="请输入数据源名称"
          clearable
        />
      </el-form-item>
      <el-form-item label="接口地址" prop="interfaceUrl">
        <el-input
          v-model.trim="queryParams.interfaceUrl"
          placeholder="请输入接口地址"
          clearable
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
          v-hasPermi="['content:source:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="contentList">
      <el-table-column label="序号" align="left" type="index" width="100" />
      <el-table-column label="数据源名称" align="left" width="200" prop="dataName" show-overflow-tooltip/>
      <el-table-column label="接口地址" align="left" prop="interfaceUrl" show-overflow-tooltip/>
      <el-table-column label="描述" align="left" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="120">
        <template #default="scope">
          <el-button
            type="primary"
            link
            @click="handleUpdate(scope.row)"
            v-hasPermi="['content:source:edit']"
          >编辑</el-button>
          <!-- <el-button
            type="danger"
            link
            @click="handleDelete(scope.row)"
            v-hasPermi="['content:source:remove']"
            :loading="deleteLoading"
          >删除</el-button> -->
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改组件库信息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="contentRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="数据源名称" prop="dataName">
          <el-input v-model="form.dataName" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="接口地址" prop="interfaceUrl">
          <el-input v-model="form.interfaceUrl" placeholder="请输入接口地址"  />
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入描述" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ContentSource">
import { listSource, addSource, updateSource, delSource } from "@/api/content/source"
import RightToolbar from '@/components/RightToolbar/index.vue'
// 表单引用
const contentRef = ref(null)
const queryRef = ref(null)

const contentList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const submitLoading = ref(false)
const deleteLoading = ref(false)

const data = reactive({
  form: {
    dataId: null,
    dataName: null,
    interfaceUrl: null,
    remark: null
  },
  queryParams: {
      pageNum: 1,
      pageSize: 10,
      dataName: null,
      interfaceUrl: null
    },
  rules: {
    dataName: [
      { required: true, message: "数据源名称不能为空", trigger: "blur" }
    ],
    interfaceUrl: [
      { required: true, message: "接口地址不能为空", trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询数据源列表 */
function getList() {
  loading.value = true
  listSource(queryParams.value).then(response => {
    contentList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 组件挂载后执行
onMounted(() => {
  getList()
})

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    dataId: null,
    dataName: null,
    interfaceUrl: null,
    remark: null
  }
  if (contentRef.value) {
    contentRef.value.resetFields()
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加数据源"
}

/** 提交按钮 */
function submitForm() {
  contentRef.value.validate(valid => {
    if (valid) {
      submitLoading.value = true
      const submitFunc = form.value.dataId ? updateSource : addSource
      const successMsg = form.value.dataId ? "修改成功" : "新增成功"
      
      submitFunc(form.value).then(response => {
        ElMessage.success(successMsg)
        open.value = false
        getList()
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  // 回显数据
  form.value = {
    dataId: row.dataId,
    dataName: row.dataName,
    interfaceUrl: row.interfaceUrl,
    remark: row.remark
  }
  open.value = true
  title.value = "修改数据源"
}

/** 删除按钮操作 */
function handleDelete(row) {
  ElMessageBox.confirm(
    "是否确认删除该数据源？",
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }
  ).then(() => {
    deleteLoading.value = true
    delSource(row.dataId).then(() => {
      ElMessage.success("删除成功")
      getList()
    }).finally(() => {
      deleteLoading.value = false
    })
  })
}

getList()
</script>
