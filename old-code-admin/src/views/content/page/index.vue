<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="应用页面" prop="url" >
        <el-select v-model="queryParams.url" placeholder="请选择应用页面" style="width: 160px;">
          <el-option
            v-for="dict in cms_page_path"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="应用平台" prop="displayPlatform" >
        <el-select v-model="queryParams.displayPlatform" placeholder="请选择应用平台" style="width: 160px;">
          <el-option
            v-for="dict in display_platform"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="checkStatus">
        <el-select v-model="queryParams.checkStatus" placeholder="请选择审核状态" clearable style="width: 160px;">
          <el-option
            v-for="dict in check_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="发布状态" prop="publishStatus">
        <el-select v-model="queryParams.publishStatus" placeholder="请选择发布状态" clearable style="width: 200px;">
          <el-option
            v-for="dict in page_publish_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="创建日期范围" style="width: 450px">
        <el-date-picker
           v-model="dateRange"
           value-format="YYYY-MM-DD"
           type="daterange"
           range-separator="至"
           start-placeholder="开始日期"
           end-placeholder="结束日期"
        ></el-date-picker>
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
          v-hasPermi="['content:page:add']"
        >新增</el-button>
        <span class="txt-des">同一应用页面+应用平台，多个已发布的，以最后的版本为准</span>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="contentList">
      <el-table-column label="序号" type="index" width="100" align="center" />
      <el-table-column label="展示路径" align="center" prop="url" width="160"></el-table-column>
      <el-table-column label="应用页面" align="center" prop="url" min-width="160"
        show-overflow-tooltip>
        <template #default="scope">
          <div :style="{ 
                      paddingLeft: scope.row.needIndent ? '0px' : '0',
                      fontWeight: scope.row.needIndent ? 'normal' : 'normal'
                      }">
              <dict-tag :options="cms_page_path" :value="scope.row.url" />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="应用平台" align="center" prop="displayPlatform" min-width="120">
        <template #default="scope">
          <div >
            <dict-tag :options="display_platform" :value="scope.row.displayPlatform" />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="页面版本" align="center" prop="version" min-width="120"  show-overflow-tooltip>
        <template #default="scope">
            V{{ scope.row.version || '-' }}
            <span v-if="scope.row.publishVersion == 'Y'" class="text-success">（生效中）</span>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" prop="checkStatus" min-width="120">
        <template #default="scope">
            <dict-tag :options="check_status" :value="scope.row.checkStatus" />
        </template>
      </el-table-column>
      <el-table-column label="审核意见" align="center" prop="applyReason" min-width="160" show-overflow-tooltip/>
      <el-table-column label="发布状态" align="center" prop="publishStatus" min-width="120">
        <template #default="scope">
            <dict-tag :options="page_publish_status" :value="scope.row.publishStatus" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" min-width="130" show-overflow-tooltip/>
        <el-table-column label="页面描述" align="left" prop="pageDesc" min-width="120" show-overflow-tooltip/>
      <el-table-column label="操作" align="center" width="400" fixed="right">
        <template #default="scope">
          <template v-if="showDkButton(scope.row)">
            <el-button
              type="primary"
              link
              @click="handleViewEdit(scope.row)"
              v-hasPermi="['content:page:visualization']"
            >可视化编辑</el-button>
          </template>
          <template v-else> 
            <el-button
              type="primary"
              link
              @click="handleViewEdit(scope.row,true)"
              v-hasPermi="['content:page:visualization']"
            >详情</el-button>
          </template>
          <template v-if="(scope.row.publishStatus == '0' || scope.row.publishStatus == '2') && (scope.row.checkStatus == '4')">
            <el-button
              type="success"
              link
              @click="handleStatus(scope.row,'1')"
              v-hasPermi="['content:page:release']"
            >发布</el-button>
          </template>
           <template v-if="(scope.row.publishStatus == '1') ">
            <el-button
              type="warning"
              link
              @click="handleStatus(scope.row,'2')"
              v-hasPermi="['content:page:takeDown']"
            >下架</el-button>
          </template>
          <template v-if="showShButton(scope.row)">
            <el-button
              type="success"
              link
              @click="handleTask(scope.row)" 
              v-hasPermi="['page:task:submit']" >提交审核</el-button>
          </template>
          <el-button
            type="success"
            link
            @click="handleCopy(scope.row)"
            v-hasPermi="['content:page:copy']"
          >复制页面</el-button>
          <el-button
            type="primary"
            link
            @click="handleUpdate(scope.row)"
            v-hasPermi="['content:page:editBasic']"
          >基本信息编辑</el-button>
          <!-- 删除 -->
          <template  v-if="showDkButton(scope.row)">
            <el-button
              type="danger"
              link
              @click="handleDelete(scope.row)"
              v-hasPermi="['content:page:remove']"
            >删除</el-button>
          </template>
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

    <!-- 添加或修改页面信息对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="contentRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="应用页面" prop="url">
          <el-select v-model="form.url" placeholder="请选择应用页面" :disabled="form.pageId">
            <el-option
              v-for="dict in cms_page_path"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="应用平台" prop="displayPlatform">
          <el-select v-model="form.displayPlatform" placeholder="请选择应用平台" :disabled="form.pageId">
            <el-option
              v-for="dict in display_platform"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="页面描述" prop="pageDesc">
          <el-input v-model="form.pageDesc" placeholder="请输入页面描述" type="textarea" :rows="3" maxlength="500"
                  show-word-limit/>
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

<script setup name="ContentPage">
import { ref, reactive, toRefs, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPage, addPage, updatePage, delPage,copyPageContent } from "@/api/content/page.js"
import RightToolbar from '@/components/RightToolbar/index.vue'
import { useDict } from '@/utils/dict'
import { addDateRange } from "@/utils/ruoyi"
import router from '@/router'
import modal from "@/plugins/modal";
import { systemTask } from '@/api/business'
// 字典数据
const { check_status, page_publish_status,cms_page_path,display_platform } = useDict('check_status', 'page_publish_status','cms_page_path','display_platform')

// 表单引用
const contentRef = ref(null)
const queryRef = ref(null)

// 响应式数据
const contentList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const submitLoading = ref(false)
const deleteLoading = ref(false)

// 页面数据
const data = reactive({
    form: {
      pageId: null,
      pageTitle: null,
      url: null,
      pageDesc: null,
      displayPlatform: null
    },
  detailForm: {
    pageId: null,
    pageTitle: null,
    url: null,
    pageDesc: null,
    checkStatus: null,
    publishStatus: null,
    createTime: null,
    displayPlatform: null
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    pageTitle: null,
    url: null,
    pageDesc: null,
    checkStatus: null,
    publishStatus: null,
    displayPlatform: null
  },
  rules: {
    url: [
      { required: true, message: "页面URL不能为空", trigger: "blur" }
    ],
    displayPlatform: [
      { required: true, message: "应用平台不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, detailForm, rules } = toRefs(data)

/** 查询页面列表 */
const dateRange = ref([])

// 处理数据，为相同的应用页面和应用平台的行添加缩进标识
function processDataForIndentation(data) {
  // 存储已处理的应用页面+应用平台组合
  const processedPairs = new Map()
  
  return data.map((item, index) => {
    // 创建唯一标识组合
    const pairKey = `${item.url}_${item.displayPlatform}`
    
    // 检查是否已经处理过这个组合
    if (processedPairs.has(pairKey)) {
      // 如果是相同组合的第2条及以后的数据，标记为需要缩进
      return { ...item, needIndent: true }
    } else {
      // 首次出现的组合，添加到已处理集合
      processedPairs.set(pairKey, true)
      return { ...item, needIndent: false }
    }
  })
}

function getList() {
  loading.value = true
  let query = addDateRange(queryParams.value, dateRange.value)
  listPage(query).then(response => {
    // 处理数据，添加缩进标识
    contentList.value = processDataForIndentation(response.rows)
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}
/** 处理提交审核 */
function handleTask(row) {
   ElMessageBox.confirm('是否确认提交审核操作？', '系统提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
   }).then(() => { 
      loading.value = true
      systemTask({businessId: row.pageId,auditType: 'page'}).then(() => {
        loading.value = false
         ElMessage({
            message: "操作成功",
            type: "success"
         })
         getList()
      }).catch(() => {
        loading.value = false
         ElMessage({
            message: "操作失败",
            type: "error"
         })
      })
   })
}
/** 可视化编辑按钮操作 */
function handleViewEdit(row,status) {
  let pageName = ''
  try {
    let item = cms_page_path.value.find(item => item.value === row.url)
    if (item) {
      pageName = item.label
    }
    console.log(pageName)
  } catch (error) {
  }
  console.log({ pageId: row.pageId,pageName:pageName,displayPlatform:row.displayPlatform,pageStatus:status },'xxx')
  router.push({ path: '/content/editContent', query: { pageId: row.pageId,pageName:pageName,displayPlatform:row.displayPlatform,pageStatus:status } })
}

/** 复制按钮操作 */
function handleCopy(row) {
  modal.confirm('确定复制该页面吗？').then(function () {
      loading.value = true
    copyPageContent(row.pageId).then(response => {
      loading.value = false
      if (response.code === 200) {
        modal.msgSuccess("复制成功")
        getList()
      }
    }).catch(error => {
      loading.value = false
      modal.msgError(error.message || '复制失败')
    })
  }).catch(function () {})
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
      pageId: null,
      pageTitle: null,
      url: null,
      pageDesc: null,
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
  dateRange.value = []
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加页面"
}


/** 提交按钮 */
function submitForm() {
  contentRef.value.validate(valid => {
    if (valid) {
      submitLoading.value = true
      const submitFunc = form.value.pageId ? updatePage : addPage
      const successMsg = form.value.pageId ? "修改成功" : "新增成功"
      
      submitFunc(form.value).then(response => {
        ElMessage.success(successMsg)
        open.value = false
        getList()
      }).catch(error => {
        ElMessage.error(error.message || '操作失败')
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

function handleStatus(row,status) { 
  let msg = status == '1' ? '是否确认发布该页面？' : '是否确认下架该页面？'
  modal.confirm(msg).then(function () {
    let params = {
      pageId: row.pageId,
      publishStatus: status,
      url: row.url,
      displayPlatform: row.displayPlatform
    }
    loading.value = true
    updatePage(params).then(response => {
      loading.value = false
      if (response.code === 200) {
        modal.msgSuccess("操作成功")
        getList()
      }
    }).catch(error => {
      loading.value = false
    })
  }).catch(function () {})
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  // 回显数据
  form.value = {
    pageId: row.pageId,
    pageTitle: row.pageTitle,
    url: row.url,
    pageDesc: row.pageDesc,
    displayPlatform: row.displayPlatform
  }
  open.value = true
  title.value = "修改页面"
}

/** 删除按钮/可视化按钮操作 */
// 判断是否显示删除按钮可视化按钮操作
function showDkButton(row) {
  // 草稿&审核中 不显示
  if (row.publishStatus == '0' && row.checkStatus == '3') {
    return false
  }
  // 已发布不显示
  if (row.publishStatus == '1') {
    return false
  }
  // 已下架&审核中不显示
  if (row.publishStatus == '2' && row.checkStatus == '3') {
    return false
  }
  // 其余都显示
  return true
}
// 判断是否显示提交审核按钮
function showShButton(row) {
  // 草稿+待审核，草稿+已拒绝 显示
  if (row.publishStatus == '0' && (row.checkStatus == '2' || row.checkStatus == '5')) {
    return true
  }
  // 已下架+待审核，已下架+已拒绝 显示
  if (row.publishStatus == '2' && (row.checkStatus == '2' || row.checkStatus == '5')) {
    return true
  }
  // 其余都不显示
  return false
}


function handleDelete(row) {
  ElMessageBox.confirm(
    "是否确认删除该页面？",
    "系统提示",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }
  ).then(() => {
    let params = {
      pageId: row.pageId,
      url: row.url,
      displayPlatform: row.displayPlatform
    }
    loading.value = true
    delPage(params).then(() => {
      loading.value = false
      ElMessage.success("删除成功")
      getList()
    }).catch(error => {
      loading.value = false
      ElMessage.error(error.message || '删除失败')
    })
  }).catch(() => {
    // 取消删除
  })
}
</script>
<style scoped lang="scss">
.txt-des {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
.text-success{
  color: #409EFF;
}
</style>
