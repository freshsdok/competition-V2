<template>
  <div class="app-container">
    <el-row :gutter="20" class="full-height">
      <!-- 左侧：栏目列表 -->
      <el-col :span="6" class="column-panel">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>内容栏目</span>
            </div>
          </template>
          <el-tree
            ref="columnTreeRef"
            :data="filteredColumnTree"
            node-key="columnId"
            :props="{ children: 'children', label: 'columnName' }"
            @node-click="handleColumnSelect"
            highlight-current
            default-expand-all
          />
        </el-card>
      </el-col>

      <!-- 右侧：内容编辑 -->
      <el-col :span="18" class="content-panel">
        <el-card class="box-card" v-if="selectedColumn">
          <template #header>
            <div class="card-header">
              <span>{{ selectedColumn.columnName }} - 内容管理</span>
              <el-button type="primary" plain icon="Plus" @click="handleAdd" size="small" v-hasPermi="['content:detail:add']">新增</el-button>
            </div>
          </template>

          <!-- 内容列表 -->
          <div class="content-list-section">
            <el-table v-loading="loading" :data="contentList" border max-height="600">
              <el-table-column label="标题" align="left" prop="detailTitle" min-width="150">
                <template #default="scope">
                  <span>{{ scope.row.detailTitle }}</span>
                </template>
              </el-table-column>
              <el-table-column label="文件排序" align="center" prop="orderNum" width="80">
                <template #default="scope">
                  <span>{{ scope.row.orderNum }}</span>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                  <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="更新时间" align="center" prop="updateTime" width="160">
                <template #default="scope">
                  <span>{{ parseTime(scope.row.updateTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="130">
                <template #default="scope">
                  <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" size="small" v-hasPermi="['content:detail:edit']">编辑</el-button>
                  <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" size="small" v-hasPermi="['content:detail:remove']">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loading && contentList.length === 0" description="暂无内容" style="margin-top: 20px;" />
          </div>
        </el-card>

        <el-empty v-else description="请先选择栏目" />
      </el-col>
    </el-row>

    <!-- 添加或修改内容对话框 -->
    <el-dialog :title="title" v-model="open" width="900px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="内容标题" prop="detailTitle">
          <el-input v-model="form.detailTitle" placeholder="请输入内容标题" />
        </el-form-item>
        <el-form-item label="文件排序" prop="orderNum" >
          <el-input-number v-model="form.orderNum" placeholder="请输入文件排序" />
        </el-form-item>
        <el-form-item label="封面图片" prop="detailImage">
          <el-upload
            :action="uploadFileUrl"
            :file-list="imageFileList"
            :limit="1"
            :on-exceed="handleExceed"
            :on-error="handleUploadError"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :show-file-list="true"
            :headers="headers"
            accept="image/*"
            list-type="picture-card"
            class="upload-image-uploader"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="内容" prop="detailContent">
          <editor v-model="form.detailContent" :min-height="300" v-if="open"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">保 存</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ContentList">
import { getColumnTree } from '@/api/content/column'
import { listContentDetail, getContentDetail, addContentDetail, updateContentDetail, delContentDetail } from '@/api/content/detail'
import { parseTime } from '@/utils/ruoyi'
import { getToken } from '@/utils/auth'
import { Plus } from '@element-plus/icons-vue'
import { replaceFileOrigin } from '@/utils/fileOrigin'

const { proxy } = getCurrentInstance()

const columnTree = ref([])
const columnTreeRef = ref()
const selectedColumn = ref(null)
const contentList = ref([])
const loading = ref(false)
const open = ref(false)
const title = ref('')
const submitLoading = ref(false)
const imageFileList = ref([])

const data = reactive({
  form: {},
  rules: {
    detailTitle: [{ required: true, message: '内容标题不能为空', trigger: 'blur' }],
    detailContent: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
    orderNum: [{ required: true, message: '文件排序不能为空', trigger: 'blur' }],
  }
})

const { form, rules } = toRefs(data)

const formRef = ref()

// 文件上传相关
const uploadFileUrl = ref(import.meta.env.VITE_APP_BASE_API + '/file/upload')
const headers = ref({ Authorization: 'Bearer ' + getToken() })

// 根据栏目类型筛选树（只显示内容列表类型的栏目）
const filteredColumnTree = computed(() => {
  const filterTree = (nodes) => {
    return nodes.filter(node => {
      // 只显示类型为1（内容列表）的栏目
      if (node.columnType === '1') {
        if (node.children && node.children.length > 0) {
          node.children = filterTree(node.children)
        }
        return true
      }
      return false
    })
  }
  return filterTree(JSON.parse(JSON.stringify(columnTree.value)))
})

/** 获取第一个栏目节点（递归查找） */
function getFirstColumnNode(nodes) {
  if (!nodes || nodes.length === 0) {
    return null
  }
  // 返回第一个节点（可以是父节点）
  return nodes[0]
}

/** 过滤栏目树（只显示内容列表类型的栏目） */
function filterColumnTree(nodes) {
  if (!nodes || nodes.length === 0) {
    return []
  }
  return nodes.filter(node => {
    // 只显示类型为1（内容列表）的栏目
    if (node.columnType === '1') {
      if (node.children && node.children.length > 0) {
        node.children = filterColumnTree(node.children)
      }
      return true
    }
    return false
  })
}

/** 获取栏目树 */
function getColumnTreeData() {
  getColumnTree({}).then(response => {
    columnTree.value = response.data || []
    // 过滤并获取第一个栏目
    const filtered = filterColumnTree(JSON.parse(JSON.stringify(columnTree.value)))
    if (filtered && filtered.length > 0) {
      const firstColumn = getFirstColumnNode(filtered)
      if (firstColumn) {
        // 延迟执行，确保树已渲染
        nextTick(() => {
          handleColumnSelect(firstColumn)
          // 设置树节点选中状态
          if (columnTreeRef.value) {
            columnTreeRef.value.setCurrentKey(firstColumn.columnId)
          }
        })
      }
    }
  })
}

/** 选择栏目 */
function handleColumnSelect(data) {
  selectedColumn.value = data
  getList()
}

/** 查询内容列表 */
function getList() {
  if (!selectedColumn.value) {
    proxy.$modal.msgWarning('请先选择栏目')
    return
  }
  loading.value = true
  listContentDetail({ columnId: selectedColumn.value.columnId }).then(response => {
    contentList.value = response.rows || []
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 新增内容 */
function handleAdd() {
  if (!selectedColumn.value) {
    proxy.$modal.msgWarning('请先选择栏目')
    return
  }
  resetForm()
  form.value.columnId = selectedColumn.value.columnId
  open.value = true
  title.value = '新增内容'
}

/** 编辑内容 */
function handleEdit(row) {
  resetForm()
  getContentDetail(row.detailId).then(response => {
    form.value = response.data
    // 如果有图片URL，设置图片列表用于显示
    if (form.value.detailImage) {
      imageFileList.value = [{
        name: '封面图片',
        url: form.value.detailImage
      }]
    }
    open.value = true
    title.value = '修改内容'
  })
}

/** 上传失败处理 */
function handleUploadError(err) {
  proxy.$modal.msgError('上传图片失败')
}

/** 超出文件数量限制回调 */
function handleExceed(files, fileList) {
  proxy.$modal.msgWarning('最多上传1张图片')
}

/** 上传成功回调 */
function handleUploadSuccess(res, file) {
  res = replaceFileOrigin(res)
  // /file/upload 返回的文件信息
  imageFileList.value = [{
    name: res.data.name || file.name,
    url: res.data.url
  }]
  form.value.detailImage = res.data.url
}

/** 移除图片回调 */
function handleRemove() {
  imageFileList.value = []
  form.value.detailImage = null
}

/** 提交表单 */
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      submitLoading.value = true
      if (form.value.detailId != null) {
        updateContentDetail(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        }).finally(() => {
          submitLoading.value = false
        })
      } else {
        addContentDetail(form.value).then(response => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        }).finally(() => {
          submitLoading.value = false
        })
      }
    }
  })
}

/** 删除内容 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该内容?').then(function() {
    return delContentDetail(row.detailId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 取消 */
function cancel() {
  open.value = false
  resetForm()
}

function resetForm() {
  form.value = {
    detailId: null,
    columnId: selectedColumn.value?.columnId,
    detailTitle: null,
    detailContent: null,
    detailImage: null,
    orderNum: 0,
  }
  imageFileList.value = []
  formRef.value?.clearValidate()
}

onMounted(() => {
  getColumnTreeData()
})
</script>

<style scoped>
.full-height {
  min-height: calc(100vh - 100px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.column-panel {
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}

.content-panel {
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}

.content-list-section {
  margin-bottom: 20px;
}

.edit-form {
  margin-top: 20px;
}

.upload-image-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.upload-image-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.upload-image-uploader :deep(.el-icon) {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}
</style>
