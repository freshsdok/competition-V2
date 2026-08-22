<template>
  <div class="app-container">
    <el-row :gutter="20" class="full-height">
      <!-- 左侧：栏目列表 -->
      <el-col :span="6" class="column-panel">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>下载栏目</span>
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

      <!-- 右侧：文件管理 -->
      <el-col :span="18" class="content-panel">
        <el-card class="box-card" v-if="selectedColumn">
          <template #header>
            <div class="card-header">
              <span>{{ selectedColumn.columnName }} - 文件下载管理</span>
              <el-button type="primary" plain icon="Plus" @click="handleAdd" size="small" v-hasPermi="['content:file:add']">上传</el-button>
            </div>
          </template>

          <!-- 文件列表（单文件模式） -->
          <el-table v-loading="loading" :data="fileListData" border max-height="400">
            <el-table-column label="文件名称" align="left" prop="fileName" min-width="200">
              <template #default="scope">
                <el-link type="primary" @click="handleDownload(scope.row)">{{ scope.row.fileName }}</el-link>
              </template>
            </el-table-column>
            
            <el-table-column label="创建时间" align="center" prop="createTime" width="180">
              <template #default="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="120">
              <template #default="scope">
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" size="small" v-hasPermi="['content:file:remove']">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && fileListData.length === 0" description="暂无文件" style="margin-top: 20px;" />
        </el-card>

        <el-empty v-else description="请先选择栏目" />
      </el-col>
    </el-row>

    <!-- 上传文件对话框 -->
    <el-dialog title="上传文件" v-model="uploadOpen" width="500px" append-to-body>
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="80px">
        <el-form-item label="文件名称" prop="fileName">
          <el-input v-model="uploadForm.fileName" placeholder="请输入文件名称" />
        </el-form-item>
        <el-form-item label="选择文件" prop="fileUrl">
          <el-upload
            ref="uploadRef"
            :action="uploadFileUrl"
            :limit="1"
            :on-exceed="handleExceed"
            :on-error="handleUploadError"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :show-file-list="true"
            :headers="headers"
            class="upload-file-uploader"
          >
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                支持上传任意格式的文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitUpload" :loading="uploading">保 存</el-button>
        <el-button @click="uploadOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FileDownload">
import { getColumnTree } from '@/api/content/column'
import { getFileListByColumnId, uploadFile, addContentFile, delContentFile } from '@/api/content/file'
import { parseTime } from '@/utils/ruoyi'
import { getToken } from '@/utils/auth'
import { replaceFileOrigin } from '@/utils/fileOrigin'

const { proxy } = getCurrentInstance()

const columnTree = ref([])
const columnTreeRef = ref()
const selectedColumn = ref(null)
const fileListData = ref([])
const loading = ref(false)
const uploadOpen = ref(false)
const uploading = ref(false)

const data = reactive({
  uploadForm: {
    fileName: null,
    fileUrl: null
  },
  uploadRules: {
    fileName: [{ required: true, message: '文件名称不能为空', trigger: 'blur' }],
    fileUrl: [{ required: true, message: '请选择文件', trigger: 'change' }]
  }
})

const { uploadForm, uploadRules } = toRefs(data)

const uploadFormRef = ref()
const uploadRef = ref()

// 文件上传相关
const uploadFileUrl = ref(import.meta.env.VITE_APP_BASE_API + '/file/upload')
const headers = ref({ Authorization: 'Bearer ' + getToken() })

// 根据栏目类型筛选树（只显示文件下载类型的栏目）
const filteredColumnTree = computed(() => {
  const filterTree = (nodes) => {
    return nodes.filter(node => {
      // 只显示类型为4（文件下载）的栏目
      if (node.columnType === '4') {
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

/** 过滤栏目树（只显示文件下载类型的栏目） */
function filterColumnTree(nodes) {
  if (!nodes || nodes.length === 0) {
    return []
  }
  return nodes.filter(node => {
    // 只显示类型为4（文件下载）的栏目
    if (node.columnType === '4') {
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

/** 查询文件列表 */
function getList() {
  if (!selectedColumn.value) {
    proxy.$modal.msgWarning('请先选择栏目')
    return
  }
  loading.value = true
  getFileListByColumnId(selectedColumn.value.columnId).then(response => {
    const files = response.data || []
    // 只取第一个文件（一对一关系）
    fileListData.value = files.length > 0 ? [files[0]] : []
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 新增文件 */
function handleAdd() {
  uploadForm.value = {
    fileName: null,
    fileUrl: null
  }
  uploadRef.value?.clearFiles()
  uploadOpen.value = true
}

/** 上传失败处理 */
function handleUploadError(err) {
  proxy.$modal.msgError('上传文件失败')
}

/** 超出文件数量限制回调 */
function handleExceed(files, fileList) {
  proxy.$modal.msgWarning('最多上传1个文件')
}

/** 上传成功回调 */
function handleUploadSuccess(res, file) {
  res = replaceFileOrigin(res)
  // /file/upload 返回的文件信息
  uploadForm.value.fileUrl = res.data.url || res.data
}

/** 移除文件回调 */
function handleRemove(res) {
  uploadForm.value.fileUrl = null
}

/** 提交上传 */
function submitUpload() {
  uploadFormRef.value.validate(valid => {
    if (valid && uploadForm.value.fileUrl) {
      uploading.value = true
      
      // 创建 ContentFile 对象并保存到数据库
      const contentFile = {
        columnId: selectedColumn.value.columnId,
        fileName: uploadForm.value.fileName,
        fileUrl: uploadForm.value.fileUrl,
        status: '0',
        delFlag: '0'
      }
      
      // 如果有旧文件，先删除旧文件，然后再添加新文件
      const oldFile = fileListData.value.length > 0 ? fileListData.value[0] : null
      const deletePromise = oldFile && oldFile.fileId
        ? delContentFile(oldFile.fileId)
        : Promise.resolve()
      
      // 删除旧文件后再添加新文件
      deletePromise.then(() => {
        return addContentFile(contentFile)
      }).then(() => {
        proxy.$modal.msgSuccess('保存成功')
        uploadOpen.value = false
        uploadFormRef.value.resetFields()
        uploadRef.value?.clearFiles()
        getList()
      }).catch(() => {
        uploading.value = false
      }).finally(() => {
        uploading.value = false
      })
    }
  })
}

/** 下载文件 */
function handleDownload(row) {
  // 如何始终在新标签页打开文件
  window.open(row.fileUrl, '_blank')
  // if (row.fileUrl) {
  //   const link = document.createElement('a')
  //   link.href = row.fileUrl
  //   link.download = row.fileName
  //   link.click()
  // } else {
  //   proxy.$modal.msgWarning('文件URL不存在')
  // }
}

/** 删除文件 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该文件?').then(function() {
    return delContentFile(row.fileId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 格式化文件大小 */
function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
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
</style>
