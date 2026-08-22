<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>文件下载</span>
        </div>
      </template>

      <div v-if="file" class="file-download-container">
        <div class="file-info">
          <h2>{{ file.fileName }}</h2>
          <div class="file-details">
            <p><strong>文件大小：</strong> {{ formatFileSize(file.fileSize) }}</p>
            <p><strong>上传时间：</strong> {{ parseTime(file.createTime) }}</p>
          </div>
          <div class="file-actions">
            <el-button type="primary" size="large" @click="handleDownload">
              <el-icon><Download /></el-icon>
              下载文件
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无可下载的文件" />
    </el-card>
  </div>
</template>

<script setup name="Download">
import { getFileByColumnId } from '@/api/content/file'
import { parseTime } from '@/utils/ruoyi'
import { Download } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const file = ref(null)

// 根据路由参数获取栏目ID
const columnId = computed(() => {
  return route.query.columnId || route.params.columnId
})

/** 获取文件信息 */
function getFile() {
  if (!columnId.value) {
    return
  }
  getFileByColumnId(columnId.value).then(response => {
    file.value = response.data
  }).catch(() => {
    file.value = null
  })
}

/** 下载文件 */
function handleDownload() {
  if (file.value && file.value.fileUrl) {
    const link = document.createElement('a')
    link.href = file.value.fileUrl
    link.download = file.value.fileName
    link.click()
  }
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
  getFile()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-download-container {
  padding: 40px;
  text-align: center;
}

.file-info {
  max-width: 600px;
  margin: 0 auto;
}

.file-info h2 {
  margin: 0 0 30px 0;
  font-size: 28px;
  color: #333;
  word-break: break-all;
}

.file-details {
  margin: 30px 0;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 4px;
  text-align: left;
}

.file-details p {
  margin: 10px 0;
  font-size: 14px;
  color: #666;
}

.file-details strong {
  color: #333;
}

.file-actions {
  margin-top: 30px;
}

.file-actions :deep(.el-button) {
  padding: 12px 40px;
  font-size: 16px;
}
</style>
