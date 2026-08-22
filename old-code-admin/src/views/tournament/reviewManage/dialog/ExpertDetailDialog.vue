<template>
  <el-dialog v-model="visible" title="专家详情" width="600px">
    <div v-loading="loading" class="expert-detail-dialog">
      <div v-if="expert" class="expert-detail-content">
        <div class="expert-header">
          <div class="expert-avatar-large">
            <img :src="expert.avatar || defAva" alt="专家头像" />
          </div>
          <div class="expert-basic-info">
            <div class="expert-name-title">
              <span class="name">{{ expert?.nickName || '-' }}</span>
              <span class="title">{{ expert?.schoolName || '-' }}</span>
            </div>
            <div class="expert-dept">{{ expert?.schoolName || '-' }}（{{ expert?.province || '-' }}）</div>
            <div class="expert-position">
              <span class="label">职务：</span>
              <span class="value">{{ expert?.position || '-' }}</span>
            </div>
            <div class="expert-status-row">
              <el-tag :type="expert?.reviewTaskInfoList?.length > 0 ? 'success' : 'info'" size="small">
                {{ expert?.reviewTaskInfoList?.length > 0 ? '已分配' : '空闲' }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="expert-tasks-section">
          <div class="section-title">已分配项目（{{ expert?.reviewTaskInfoList?.length || 0 }}）</div>
          <div v-if="expert?.reviewTaskInfoList?.length > 0" class="task-list">
            <div v-for="(task, index) in expert?.reviewTaskInfoList" :key="task?.reviewId" class="task-item">
              <span class="task-dot">{{ index + 1 }}、</span>
              <span class="task-name">{{ task?.reviewName || '-' }}</span>
              <span class="task-info">({{ task?.secondLevelName || '-' }} / {{ task?.province || '-' }})</span>
            </div>
          </div>
          <div v-else class="no-tasks">暂无分配项目</div>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button size="small" @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import defAva from '@/assets/images/profile.png'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  expert: {
    type: Object,
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

function handleClose() {
  visible.value = false
}
</script>

<style scoped lang="scss">
.expert-detail-dialog {
  .expert-header {
    display: flex;
    gap: 16px;
    padding-bottom: 20px;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 20px;

    .expert-avatar-large {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: #606266;
      overflow: hidden;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .expert-basic-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 6px;

      .expert-name-title {
        display: flex;
        align-items: center;
        gap: 8px;

        .name {
          font-size: 18px;
          font-weight: 600;
          color: #333;
        }

        .title {
          font-size: 14px;
          color: #909399;
        }
      }

      .expert-dept {
        font-size: 14px;
        color: #606266;
      }

      .expert-position {
        font-size: 13px;
        color: #409eff;

        .label {
          color: #606266;
        }
      }

      .expert-status-row {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-top: 4px;

        .task-count {
          font-size: 13px;
          color: #909399;
        }
      }
    }
  }

  .expert-tasks-section {
    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #333;
      margin-bottom: 12px;
    }

    .task-list {
      height: 300px;
      overflow: auto;
      .task-item {
        display: flex;
        align-items: flex-start;
        gap: 6px;
        padding: 8px 0;
        font-size: 13px;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .task-dot {
          font-size: 14px;
        }

        .task-name {
          color: #333;
        }

        .task-info {
          color: #909399;
          max-width: 40%;
          flex-shrink: 0;
        }
      }
    }

    .no-tasks {
      text-align: center;
      padding: 20px;
      color: #909399;
      font-size: 13px;
    }
  }
}
</style>
