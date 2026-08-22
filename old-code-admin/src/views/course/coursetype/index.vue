<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="上级分类"  prop="parentName">
        <el-input
            v-model="queryParams.parentName"
            placeholder="请输入上级分类名称"
            clearable
            @keyup.enter="handleQuery"
          />
      </el-form-item>
      <el-form-item label="分类名称" prop="classifyName">
        <el-input
          v-model="queryParams.classifyName"
          placeholder="请输入分类名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable :style="{ width: '160px' }">
          <el-option
            v-for="dict in subassembly_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
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
          v-hasPermi="['course:classify:add']"
        >新增</el-button>
      </el-col>
      <!-- <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['course:classify:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['course:classify:remove']"
        >删除</el-button>
      </el-col> -->
      <!-- <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['course:classify:export']"
        >导出</el-button>
      </el-col> -->
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="classifyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" type="index" width="50" />
      <el-table-column label="上级分类名称" align="center" prop="parentName" />
      <el-table-column label="分类名称" align="center" prop="classifyName" />
      <el-table-column label="描述" align="center" prop="classifyDesc" />
      <el-table-column label="排序权重" align="center" prop="weight" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="subassembly_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['course:classify:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['course:classify:remove']">删除</el-button>
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

    <!-- 添加或修改课程分类对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="classifyRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级分类名称" :label-width="120" prop="parentClassify">
          <el-tree-select
              v-model="form.parentClassify"
              :data="menuOptions"
              :props="{ value: 'classifyId', label: 'classifyName', children: 'children' }"
              value-key="classifyId"
              check-strictly
              placeholder="选择上级分类"
              clearable
            />
        </el-form-item>
        <el-form-item label="课程分类名称" :label-width="120" prop="classifyName">
          <el-input v-model="form.classifyName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" :label-width="120" prop="classifyDesc">
          <el-input v-model="form.classifyDesc" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="排序权重" :label-width="120" prop="weight">
          <el-input-number v-model="form.weight" placeholder="请输入排序权重" />
        </el-form-item>
        <el-form-item label="状态" :label-width="120" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in subassembly_status"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
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

<script setup name="Classify">
import { ref, reactive, toRefs, getCurrentInstance } from 'vue'
import { listClassify, getClassify, delClassify, addClassify, updateClassify, listClassifyGetList } from "@/api/course/coursetype"

const { proxy } = getCurrentInstance()
const { subassembly_status } = proxy.useDict('subassembly_status')

const classifyList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const menuOptions = ref([])

const data = reactive({
  form: { status: '0' },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    parentClassify: null,
    parentName: null,
    classifyName: null,
    classifyCode: null,
    classifyDesc: null,
    status: null,
  },
  rules: {
    classifyName: [
      { required: true, message: "分类名称不能为空", trigger: "blur" }
    ],
    classifyCode: [
      { required: true, message: "分类编码不能为空", trigger: "blur" }
    ],
    weight: [
      { required: true, message: "排序权重不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询课程分类列表 */
function getList() {
  loading.value = true
  listClassify(queryParams.value)
    .then(response => {
      const rows = Array.isArray(response?.rows)
        ? response.rows
        : Array.isArray(response?.data)
          ? response.data
          : []
      classifyList.value = rows
      total.value = response?.total ?? rows.length ?? 0
    })
    .catch(error => {
      classifyList.value = []
      total.value = 0
      proxy?.$modal?.msgError?.(error?.msg || "获取课程分类列表失败")
    })
    .finally(() => {
      loading.value = false
    })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    classifyId: null,
    parentClassify: null,
    classifyName: null,
    classifyCode: null,
    classifyDesc: null,
    classifyImage: null,
    weight: 0,
    status: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    version: null,
    delFlag: null,
    userId: null,
    orgId: null
  }
  proxy.resetForm("classifyRef")
}

function getTreeselect() {
    menuOptions.value = []
    // 使用listClassifyGetList接口获取树形分类数据
    return listClassifyGetList()
      .then(response => {
        // Ensure data is an array before processing
        // Handle possible different API response structures
        // Access nested data array correctly from API response
         // Use response.rows to match API structure from getList
          // Add null check for response and prioritize rows as in getList
          // Use response.data consistently with getList function
          // Use response.rows to match API structure from getList
          // Handle API responses that return arrays directly or in rows/data
          // Use response.rows consistently with getList function
          // Use response.data for treeselect data (API returns different structure without params)
          // Handle API responses that return array directly or in data property
           const data = Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : [];
        // 使用parentClassify作为父ID字段构建树形结构
        // 构建树形结构数据，主类目作为根节点
    const menu = { classifyId: 0, classifyName: "主类目", children: proxy.handleTree(data, "classifyId", "parentClassify") }
    menuOptions.value = [menu]

      })
      .catch(error => {
        console.error("Error loading course classification tree data:", error);
        menuOptions.value = []; // Ensure menuOptions remains an array even when API call fails
      })
  }

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.classifyId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
async function handleAdd() {
  reset()
  await getTreeselect()
  open.value = true
  title.value = "添加课程分类"
}

/** 修改按钮操作 */
async function handleUpdate(row) {
  reset()
  await getTreeselect()
  const _classifyId = row.classifyId || ids.value
  getClassify(_classifyId).then(response => {
    form.value = response.data
    // 设置父分类值以确保回显
    form.value.parentClassify = response.data.parentClassify || 0
    open.value = true
    title.value = "修改课程分类"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["classifyRef"].validate(valid => {
    if (valid) {
      if (form.value.classifyId != null) {
        updateClassify(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addClassify(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _classifyIds = row.classifyId || ids.value
  proxy.$modal.confirm('是否确认删除课程分类编号为"' + _classifyIds + '"的数据项？').then(function() {
    return delClassify(_classifyIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('course/classify/export', {
    ...queryParams.value
  }, `classify_${new Date().getTime()}.xlsx`)
}

getList()
</script>
