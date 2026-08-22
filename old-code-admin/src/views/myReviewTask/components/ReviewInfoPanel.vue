<template>
  <!-- 右侧信息面板 -->
  <div class="info-section">
    <!-- 项目信息 -->
    <div class="info-card">
      <div class="card-header">
        <span>项目信息</span>
      </div>
      <div class="card-body scrollable">
        <div class="info-item">
          <span class="label">项目编号：</span>
          <span class="value ellipsis">{{ taskInfo?.id || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">赛事：</span>
          <el-tooltip :content="taskInfo?.competition_name || '-'" placement="top" :show-after="500">
            <span class="value ellipsis">{{ taskInfo?.competition_name || '-' }}</span>
          </el-tooltip>
        </div>
        <div class="info-item">
          <span class="label">赛项/组别：</span>
          <el-tooltip :content="taskInfo?.competition_track_name || '-'" placement="top" :show-after="500">
            <span class="value ellipsis">
              {{ taskInfo?.competition_track_name || '-' }}
              /
              {{ taskInfo?.second_level_name || '-' }}
            </span>
          </el-tooltip>
        </div>
        <div class="info-item">
          <span class="label">截止时间：</span>
          <span class="value">{{ taskInfo?.reviewEndTime || '-' }}</span>
        </div>
      </div>
    </div>

    <!-- 专家评审参考文件 -->
    <div class="info-card">
      <div class="card-header">
        <span>专家评审参考文件</span>
      </div>
      <div class="card-body scrollable">
        <div v-if="taskInfo?.referenceDocument?.length > 0" class="file-list">
          <div v-for="(file, index) in taskInfo?.referenceDocument" :key="index" class="file-item"
            @click="$emit('downloadFile', file)">
            <el-icon :size="14">
              <Paperclip />
            </el-icon>
            <el-tooltip :content="file.fileName" placement="top" :show-after="500">
              <span class="file-name">{{ file.fileName }}</span>
            </el-tooltip>
            <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
          </div>
        </div>
        <div v-else class="no-data">暂无参考文件</div>
      </div>
    </div>

    <!-- 评审备注 -->
    <div class="info-card">
      <div class="card-header">
        <span>评审备注</span>
      </div>
      <div class="card-body scrollable">
        <div v-if="taskInfo?.reviewDesc" class="remark-content">{{ taskInfo.reviewDesc }}</div>
        <div v-else class="no-data">暂无评审备注</div>
      </div>
    </div>

    <!-- 审阅备注 -->
    <div class="info-card">
      <div class="card-header">
        <span>审阅备注
          <span class="remark-tip">(仅本人可看，仅提供给专家备注使用)</span>
        </span>
      </div>
      <div class="card-body">
        <div class="remark-history scrollable">
          <div v-for="(item, index) in reviewRemarks" :key="index" class="remark-item">
            <div class="remark-header">
              <div class="remark-time">{{ item.createTime }}</div>
              <el-button link type="danger" v-if="taskInfo.continueFlag" size="small" @click="handleDeleteRemark(item)">删除</el-button>
            </div>
            <div class="remark-text">{{ item.describe }}</div>
          </div>
        </div>
        <div class="remark-input-area">
          <el-input v-model="localNewRemark" type="textarea" :rows="2" placeholder="请输入您的审阅备注..." maxlength="200"
            show-word-limit resize="none" />
          <el-button type="primary" class="full-width-btn" @click="handleAddRemark" v-if="taskInfo.continueFlag">添加审阅备注</el-button>
        </div>
      </div>
    </div>

    <!-- 审阅状态 -->
    <div class="info-card">
      <div class="card-header">
        <span>审阅状态</span>
      </div>
      <div class="card-body">
        <el-button type="success" 
                  size="medium" 
                  v-if="taskInfo.continueFlag"
                  class="submit-review-btn" 
                  @click="$emit('submitReview')">
          {{ taskInfo?.review_status == '1' ? '已审阅' : '变更审阅状态' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Paperclip } from '@element-plus/icons-vue'
import { addNotes, deleteNotes } from '@/api/tournament/reviewManage'

const props = defineProps({
  taskInfo: {
    type: Object,
    default: () => ({})
  },
  reviewRemarks: {
    type: Array,
    default: () => []
  },
  processedId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['downloadFile', 'submitReview', 'refreshNotes'])

// 本地备注输入
const localNewRemark = ref('')

// 添加备注
function handleAddRemark() {
  if (!props.taskInfo?.continueFlag) {
    ElMessage.warning('审阅截止时间已过，不能添加备注')
    return
  }
  if (!localNewRemark.value.trim()) {
    ElMessage.warning('请输入备注内容')
    return
  }
  addNotes({
    processedRelationId: props.processedId,
    describe: localNewRemark.value.trim()
  }).then(res => {
    if (res.code === 200) {
      localNewRemark.value = ''
      ElMessage.success('添加成功')
      emit('refreshNotes')
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  }).catch(() => {
    ElMessage.error('添加失败')
  })
}

// 删除备注
function handleDeleteRemark(remark) {
  if (!props.taskInfo?.continueFlag) {
    ElMessage.warning('审阅截止时间已过，不能删除')
    return
  }
  if (!remark?.id) return
  ElMessageBox.confirm(
    '确定要删除这条审阅备注吗？',
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    deleteNotes(props.processedId,remark.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        emit('refreshNotes')
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  }).catch(() => {
    // 用户取消，不处理
  })
}

// 格式化文件大小
function formatFileSize(bytes) {
  if (bytes === null || bytes === undefined || bytes === '') return '-'
  const size = parseInt(bytes)
  if (isNaN(size) || size === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(size) / Math.log(k))
  return (size / Math.pow(k, i)).toFixed(2) + ' ' + units[i]
}
</script>

<style scoped lang="scss">
.info-section {
  width: 380px;
  display: flex;
  flex-direction: column;
  overflow: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;

  // 自定义滚动条样式 - 更明显
  &::-webkit-scrollbar {
    width: 16px;
    height: 16px;
  }

  &::-webkit-scrollbar-track {
    background: #e0e0e0;
    border-radius: 8px;
  }

  &::-webkit-scrollbar-thumb {
    background: #555;
    border-radius: 8px;
    border: 2px solid #e0e0e0;

    &:hover {
      background: #333;
    }
  }

  .info-card {
    background: #fff;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    border-bottom: 1px solid #e4e7ed;

    &:nth-child(1) {
      flex: 0 0 132px;
    }

    // 项目信息
    &:nth-child(2) {
      flex: 0 0 90px;
    }

    // 参考文件
    &:nth-child(3) {
      flex: 0 0 90px;
    }

    // 评审备注
    &:nth-child(4) {
      flex: 1 1 auto;
      min-height: 230px;
    }

    // 审阅备注
    &:nth-child(5) {
      flex: 0 0 90px;
      border-bottom: none;
    }

    // 审阅状态
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      background: #f5f7fa;
      border-bottom: 1px solid #e4e7ed;
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      flex-shrink: 0;
    }

    .card-body {
      padding: 8px 12px;
      overflow: hidden;
      display: flex;
      flex-direction: column;

      &.scrollable {
        overflow-y: auto;
      }

      .info-item {
        display: flex;
        margin-bottom: 4px;
        font-size: 12px;
        line-height: 1.4;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          color: #909399;
          flex-shrink: 0;
          width: 70px;
        }

        .value {
          color: #303133;
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;

          &.ellipsis {
            display: inline-block;
            max-width: 100%;
          }
        }
      }

      .file-list {
        .file-item {
          display: flex;
          align-items: center;
          gap: 6px;
          padding: 4px 0;
          border-bottom: 1px solid #ebeef5;
          font-size: 12px;
          cursor: pointer;

          &:hover {
            .file-name {
              color: #409eff;
            }
          }

          &:last-child {
            border-bottom: none;
          }

          .el-icon {
            color: #409eff;
            font-size: 14px;
            flex-shrink: 0;
          }

          .file-name {
            flex: 1;
            color: #409eff;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .file-size {
            color: #909399;
            font-size: 11px;
            flex-shrink: 0;
          }
        }
      }

      .remark-content {
        font-size: 12px;
        color: #606266;
        line-height: 1.5;
      }

      .remark-history {
        flex: 1;
        overflow-y: auto;
        margin-bottom: 8px;

        .remark-item {
          padding: 8px;
          margin-bottom: 8px;
          background: #f5f7fa;
          border-radius: 4px;

          &:last-child {
            margin-bottom: 0;
          }

          .remark-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 4px;

            .remark-time {
              font-size: 11px;
              color: #909399;
            }

            .el-button {
              padding: 0;
              height: auto;
              font-size: 11px;
            }
          }

          .remark-text {
            font-size: 12px;
            color: #303133;
            line-height: 1.5;
          }
        }
      }

      .remark-input-area {
        display: flex;
        flex-direction: column;
        gap: 8px;
        flex-shrink: 0;

        .full-width-btn {
          width: 100%;
        }

      }

      .submit-review-btn {
        width: 100%;
        margin-top: 3px;
      }
    }
  }
  .remark-tip {
    font-size: 11px;
    color: #e6a23c;
    text-align: center;
  }
}

.no-data {
  text-align: center;
  color: #909399;
  font-size: 13px;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding-top: 12px;
}
</style>
