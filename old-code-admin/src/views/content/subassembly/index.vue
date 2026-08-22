<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="组件名称" prop="componentName">
        <el-input
          v-model.trim="queryParams.componentName"
          placeholder="请输入组件名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="组件分类" prop="componentClassify">
        <el-select v-model="queryParams.componentClassify" placeholder="请选择组件分类" clearable style="width: 160px;">
          <el-option
            v-for="dict in cms_subassembly_classify"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="应用平台" prop="displayPlatform">
        <el-select v-model="queryParams.displayPlatform" placeholder="请选择应用平台" clearable style="width: 160px;">
          <el-option
            v-for="dict in display_platform"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="描述" prop="componentDesc">
        <el-input
          v-model.trim="queryParams.componentDesc"
          placeholder="请输入描述"
          clearable
          style="width: 160px;"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <!-- <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['content:subassembly:add']"
        >新增</el-button> -->
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="contentList">
      <el-table-column label="序号" align="left" type="index" width="100" />
      <el-table-column label="组件名称" align="left" prop="componentName" />
      <el-table-column label="组件标识" align="left" prop="componentLogotype" />
      <el-table-column label="应用平台" align="left" prop="displayPlatform">
        <template #default="scope">
          <dict-tag :options="display_platform" :value="scope.row.displayPlatform"/>
        </template>
      </el-table-column>
      <el-table-column label="组件分类" align="left" prop="componentClassify">
        <template #default="scope">
          <dict-tag :options="cms_subassembly_classify" :value="scope.row.componentClassify"/>
        </template>
      </el-table-column>
      <el-table-column label="描述信息" align="left" prop="componentDesc" show-overflow-tooltip/>
      <el-table-column label="操作" align="center" width="120">
        <template #default="scope">
          <template v-if="scope.row.isBuiltIn !== 'Y'">
            <el-button
                type="primary"
                link
                @click="handleUpdate(scope.row)"
                v-hasPermi="['content:subassembly:edit']"
              >编辑</el-button>
          </template>
          <!-- <el-button
            type="danger"
            link
            @click="handleDelete(scope.row)"
            v-hasPermi="['content:subassembly:remove']"
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
      <el-form ref="contentRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="组件名称" prop="componentName">
          <el-input v-model="form.componentName" placeholder="请输入组件名称" />
        </el-form-item>
        <el-form-item label="组件分类" prop="componentClassify">
          <el-select v-model="form.componentClassify" placeholder="请选择组件分类">
            <el-option
              v-for="dict in cms_subassembly_classify"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="应用平台" prop="displayPlatform">
          <el-select v-model="form.displayPlatform" placeholder="请选择应用平台" :disabled="form.componentId">
            <el-option
              v-for="dict in display_platform"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="组件标识" prop="componentLogotype">
          <el-input v-model="form.componentLogotype" placeholder="请输入组件标识"  :disabled="form.componentId"/>
        </el-form-item>
        <el-form-item label="描述" prop="componentDesc">
          <el-input v-model="form.componentDesc" placeholder="请输入描述" type="textarea" :rows="3" />
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

<script setup name="Content">
import { listContent, addContent, updateContent, delContent } from "@/api/content/content"
import { useDict } from '@/utils/dict'

const { cms_subassembly_classify,display_platform } = useDict('cms_subassembly_classify','display_platform')
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
    componentName: null,
    componentClassify: null,
    componentLogotype: null,
    componentDesc: null,
    displayPlatform: null
  },
  queryParams: {
      pageNum: 1,
      pageSize: 10,
      componentName: null,
      componentClassify: null,
      componentDesc: null,
      displayPlatform: null
    },
  rules: {
    componentName: [
      { required: true, message: "组件名称不能为空", trigger: "blur" }
    ],
    componentClassify: [
      { required: true, message: "组件分类不能为空", trigger: "change" }
    ],
    componentLogotype: [
      { required: true, message: "组件标识不能为空", trigger: "blur" }
    ],
    displayPlatform: [
      { required: true, message: "应用平台不能为空", trigger: "change" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询组件库信息列表 */
function getList() {
  loading.value = true
  listContent(queryParams.value).then(response => {
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
    componentName: null,
    componentClassify: null,
    componentLogotype: null,
    componentDesc: null,
    displayPlatform: null
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
  title.value = "添加组件库信息"
}

/** 提交按钮 */
function submitForm() {
  contentRef.value.validate(valid => {
    if (valid) {
      submitLoading.value = true
      const submitFunc = form.value.componentId ? updateContent : addContent
      const successMsg = form.value.componentId ? "修改成功" : "新增成功"
      
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
  // 回显数据（只回显可编辑的字段）
  form.value = {
    componentId: row.componentId,
    componentName: row.componentName,
    componentClassify: row.componentClassify,
    componentDesc: row.componentDesc,
    componentLogotype: row.componentLogotype, // 虽然不编辑，但需要保留原值
    displayPlatform: row.displayPlatform
  }
  open.value = true
  title.value = "修改组件库信息"
}

/** 删除按钮操作 */
function handleDelete(row) {
  ElMessageBox.confirm(
    "是否确认删除该组件库信息？",
    "系统提示",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }
  ).then(() => {
    deleteLoading.value = true
    delContent(row.componentId).then(() => {
      ElMessage.success("删除成功")
      getList()
    }).finally(() => {
      deleteLoading.value = false
    })
  })
}
</script>
