<template>
  <div class="expert-panel">
    <div class="panel-title">评审专家</div>

    <!-- 模式切换 -->
    <div class="mode-switch">
      <span
        class="mode-item"
        :class="{ active: mode === 'expert' }"
        @click="switchMode('expert')"
      >专家</span>
      <span
        class="mode-item"
        :class="{ active: mode === 'group' }"
        @click="switchMode('group')"
      >专家组</span>
    </div>

    <!-- 搜索 -->
    <div class="search-container">
      <el-input
        v-if="mode === 'expert'"
        v-model.trim="searchKeyword"
        placeholder="搜索专家姓名/学校/省份" 
        clearable
        size="small"
        class="expert-search"
      />
    </div>

    <!-- 状态筛选 -->
    <div v-if="mode === 'expert'" class="filter-tabs">
      <span
        v-for="tab in filterTabs"
        :key="tab.value"
        class="filter-item"
        :class="{ active: filterStatus === tab.value }"
        @click="handleFilterChange(tab.value)"
      >
        {{ tab.label }}
      </span>
    </div>

    <!-- 专家列表 - 使用原生滚动 -->
    <div v-loading="mode === 'expert' ? loading : groupLoading" class="expert-list">
      <!-- 专家模式 -->
      <template v-if="mode === 'expert'">
        <div class="native-scroll-container">
          <div
            v-for="expert in filteredExperts"
            :key="expert.userId"
            class="expert-card"
            :class="{ selected: selectedIds.includes(expert.userId), assigned: expert.reviewTaskInfoList?.length > 0 }"
            @click="handleExpertItemClick(expert)"
          >
            <div class="expert-main">
              <div class="expert-avatar">
                <img :src="expert.avatar || defAva" alt="专家头像" />
              </div>
              <div class="expert-content">
                <div class="expert-top-row">
                  <div class="expert-name-title">
                    <el-tooltip :content="expert?.nickName || ''" placement="top" :show-after="500">
                      <span class="name text-ellipsis">{{ expert?.nickName || '-' }}</span>
                    </el-tooltip>
                    <el-tooltip :content="expert?.position || ''" placement="top" :show-after="500">
                      <span class="title text-ellipsis">{{ expert?.position || '-' }}</span>
                    </el-tooltip>
                  </div>
                  <el-tooltip :content="`${expert?.schoolName || ''}（${expert?.province || ''}）`" placement="top" :show-after="500">
                    <div class="expert-dept">
                      <span class="school-name text-ellipsis">{{ expert?.schoolName || '-' }}</span>
                      <span class="province-wrap">(<span class="province-name">{{ expert?.province || '-' }}</span>)</span>
                    </div>
                  </el-tooltip>
                </div>
                <div class="expert-bottom-row">
                  <el-tag :type="expert?.reviewTaskInfoList?.length > 0 ? 'success' : 'info'" size="small" class="status-tag">
                    {{ expert?.reviewTaskInfoList?.length > 0 ? '已分配' : '空闲' }}
                  </el-tag>
                  <span class="task-count">{{ expert?.reviewTaskInfoList?.length || 0 }}个任务</span>
                </div>
              </div>
            </div>
            <div v-if="expert.reviewTaskInfoList?.length > 0" class="view-btn">
              <el-button link type="primary" size="small" @click.stop="$emit('view-expert-tasks', expert)">
                查看已分配项目({{ expert.reviewTaskInfoList.length }})
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <!-- 专家组模式 -->
      <template v-else>
        <div class="native-scroll-container">
          <div
            v-for="group in expertGroups"
            :key="group.groupId"
            class="group-card"
            :class="{ selected: selectedGroupIds.includes(group.groupId) }"
            @click="handleGroupItemClick(group)"
          >
            <div class="group-header">
              <el-tooltip :content="group?.groupName || ''" placement="top" :show-after="500">
                <span class="group-name text-ellipsis">{{ group?.groupName || '-' }}</span>
              </el-tooltip>
              <div class="group-actions">
                <el-tag type="info" size="small">{{ group?.reviewGroupSpecialistRelationList?.length || 0 }}人</el-tag>
                <el-button
                  link
                  type="primary"
                  size="small"
                  class="action-btn"
                  @click.stop="$emit('edit-expert-group', group)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button
                  link
                  type="danger"
                  size="small"
                  class="action-btn"
                  @click.stop="handleDeleteGroup(group)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <div v-if="group?.reviewGroupSpecialistRelationList?.length > 0" class="group-experts">
              <el-tooltip 
                v-for="relation in group.reviewGroupSpecialistRelationList" 
                :key="relation.userId" 
                :content="relation.userName" 
                placement="top" 
                :show-after="500"
              >
                <el-tag size="small" type="info" class="tag-ellipsis">
                  {{ relation.userName }}
                </el-tag>
              </el-tooltip>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 底部统计和按钮 -->
    <div class="panel-footer">
      <span>共{{ mode === 'expert' ? experts.length : expertGroups.length }}{{ mode === 'expert' ? '人' : '个专家组' }}</span>
      <el-button
        v-if="mode === 'expert'"
        type="primary"
        :disabled="selectedIds.length === 0"
        size="small"
        @click="$emit('create-expert-group', selectedIds)"
      >
        创建专家分组
      </el-button>
      <el-button
        v-else
        type="primary"
        size="small"
        @click="$emit('create-expert-group', [])"
      >
        创建专家分组
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Edit, Delete } from '@element-plus/icons-vue'
import defAva from '@/assets/images/profile.png'

const props = defineProps({
  experts: { type: Array, default: () => [] },
  expertGroups: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  groupLoading: { type: Boolean, default: false },
  activeMode: { type: String, default: '' },
  activeFilter: { type: String, default: '' }
})

const emit = defineEmits(['select-experts', 'select-group', 'create-expert-group', 'view-expert-tasks', 'mode-change', 'delete-expert-group', 'edit-expert-group', 'filter-change'])

const mode = ref('expert')

// 监听父组件传入的模式变化
watch(() => props.activeMode, (newMode) => {
  if (newMode && newMode !== mode.value) {
    mode.value = newMode
    selectedIds.value = []
    selectedGroupIds.value = []
    emit('select-experts', [])
  }
}, { immediate: true })

const searchKeyword = ref('')
const filterStatus = ref('all')
const selectedIds = ref([])
const selectedGroupIds = ref([])  // 改为多选专家组

// 监听父组件传入的筛选状态变化
watch(() => props.activeFilter, (newFilter) => {
  // 将 distributeStatus 映射回 filterStatus
  const statusMap = {
    '': 'all',
    '1': 'assigned',
    '0': 'free'
  }
  const newFilterStatus = statusMap[newFilter] || 'all'
  if (newFilterStatus !== filterStatus.value) {
    filterStatus.value = newFilterStatus
    // 筛选状态变化时清空选中状态
    selectedIds.value = []
    selectedGroupIds.value = []
    emit('select-experts', [])
  }
}, { immediate: true })

const filterTabs = [
  { label: '全部', value: 'all' },
  { label: '已分配', value: 'assigned' },
  { label: '空闲', value: 'free' }
]

const filteredExperts = computed(() => {
  let list = props.experts

  // 前端只处理搜索关键词，状态筛选通过后端接口处理
  if (searchKeyword.value) {
    const kw = searchKeyword.value.trim().toLowerCase()
    list = list.filter(e =>
      e.nickName?.toLowerCase().includes(kw) ||
      e.schoolName?.toLowerCase().includes(kw) ||
      e.province?.toLowerCase().includes(kw)
    )
  }

  return list
})

// 处理状态筛选变化
function handleFilterChange(value) {
  filterStatus.value = value
  // 切换筛选状态时清空专家选中
  selectedIds.value = []
  selectedGroupIds.value = []
  emit('select-experts', [])
  // 通知父组件调用接口
  const statusMap = {
    'all': '',
    'assigned': '1',
    'free': '0'
  }
  emit('filter-change', statusMap[value])
}

function switchMode(newMode) {
  mode.value = newMode
  selectedIds.value = []
  selectedGroupIds.value = []
  emit('select-experts', [])
  // 不通过 select-group 事件清空，由父组件在 mode-change 中统一处理
  emit('mode-change', newMode)  // 通知父组件模式切换
}

function toggleExpert(id) {
  // 防止无效值
  if (!id) return
  const idx = selectedIds.value.indexOf(id)
  if (idx > -1) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
  emit('select-experts', selectedIds.value)
}

// 虚拟列表点击专家项
function handleExpertItemClick(expert) {
  toggleExpert(expert.userId)
}

// 虚拟列表点击专家组项
function handleGroupItemClick(group) {
  selectGroup(group.groupId)
}

function selectGroup(groupId) {
  // 防止无效值
  if (!groupId) return
  const idx = selectedGroupIds.value.indexOf(groupId)
  if (idx > -1) {
    selectedGroupIds.value.splice(idx, 1)
  } else {
    selectedGroupIds.value.push(groupId)
  }
  emit('select-group', groupId)  // 传递单个groupId，父组件处理多选逻辑
}

function getExpertName(id) {
  const expert = props.experts.find(e => e.userId === id)
  return expert ? expert.nickName : ''
}

// 处理删除专家组
function handleDeleteGroup(group) {
  emit('delete-expert-group', group)
}
</script>

<style scoped lang="scss">
.expert-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 4px;

  .panel-title {
    padding: 8px 6px;
    font-weight: 600;
    font-size: 14px;
    background: #f5f5f5;
  }

  .mode-switch {
    display: flex;
    margin: 8px 6px;
    background: #f5f7fa;
    border-radius: 4px;
    padding: 3px;

    .mode-item {
      flex: 1;
      text-align: center;
      padding: 4px 0;
      font-size: 13px;
      cursor: pointer;
      border-radius: 3px;
      color: #606266;
      transition: all 0.2s;

      &.active {
        background: #fff;
        color: #409eff;
        font-weight: 500;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
      }
    }
  }
  .search-container {
    margin: 0px 6px 12px;
  }

  .filter-tabs {
    display: flex;
    gap: 8px;
    padding: 0 6px 12px;

    .filter-item {
      padding: 4px 12px;
      border-radius: 20px;
      border: 1px solid #dcdfe6;
      font-size: 12px;
      color: #606266;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        color: #409eff;
        border-color: #c6e2ff;
      }

      &.active {
        background: #ecf5ff;
        color: #409eff;
        border-color: #409eff;
      }
    }
  }

  .expert-list {
    flex: 1;
    overflow: hidden;
    padding: 0 6px;

    // 原生滚动容器
    .native-scroll-container {
      height: 100%;
      overflow-y: auto;
      overflow-x: hidden;
    }

    // 专家卡片
    .expert-card {
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      padding: 8px;
      margin-bottom: 8px;
      cursor: pointer;
      transition: all 0.2s;
      box-sizing: border-box;

      &:hover {
        border-color: #c6e2ff;
        box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
      }

      &.selected {
        border-color: #409eff;
        background: #ecf5ff;
      }

      .expert-main {
        display: flex;
        gap: 5px;

        .expert-avatar {
          width: 30px;
          height: 30px;
          border-radius: 50%;
          background: #f5f7fa;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;
          color: #606266;
          overflow: hidden;
          flex-shrink: 0;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .expert-content {
          flex: 1;
          min-width: 0;
          display: flex;
          flex-direction: column;
          gap: 6px;

          .expert-top-row {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 8px;

            .expert-name-title {
              display: flex;
              align-items: center;
              gap: 6px;
              min-width: 0;
              flex: 1;
              overflow: hidden;

              .name {
                font-weight: 600;
                font-size: 14px;
                color: #333;
                max-width: 58px;
                flex-shrink: 0;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }

              .title {
                font-size: 12px;
                color: #909399;
                max-width: 40px;
                flex-shrink: 0;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
            }

            .expert-dept {
              font-size: 12px;
              color: #606266;
              white-space: nowrap;
              max-width: 106px;
              text-align: right;
              display: flex;
              align-items: center;
              justify-content: flex-end;

              .school-name {
                overflow: hidden;
                text-overflow: ellipsis;
                flex: 1;
                min-width: 0;
              }

              .province-wrap {
                flex-shrink: 0;
              }

              .province-name {
                font-weight: bold;
                max-width: 36px;
                display: inline-block;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                vertical-align: middle;
                text-align: right;
              }
            }
          }

          .expert-bottom-row {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .status-tag {
              border-radius: 3px;
              padding: 0 6px;
              height: 20px;
              line-height: 18px;
            }

            .task-count {
              font-size: 12px;
              color: #909399;
            }
          }
        }
      }

      .view-btn {
        margin-top: 8px;
        padding: 4px;
        text-align: center;
        background: #ffffff;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 12px;
        border: 1px solid #409eff;

        :deep(.el-button) {
          font-size: 12px;
        }
      }
    }

    // 专家组卡片
    .group-card {
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      padding: 12px;
      margin-bottom: 8px;
      cursor: pointer;
      transition: all 0.2s;
      box-sizing: border-box;

      &:hover {
        border-color: #c6e2ff;
      }

      &.selected {
        border-color: #409eff;
        background: #ecf5ff;
      }

      .group-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        gap: 8px;

        .group-name {
          font-weight: 600;
          font-size: 14px;
          max-width: 120px;
        }

        .group-actions {
          display: flex;
          align-items: center;
          gap: 4px;

          .action-btn {
            padding: 2px;
            margin: 0;
            height: auto;
          }
        }
      }

      .group-experts {
        display: flex;
        flex-wrap: nowrap;
        gap: 4px;
        overflow: hidden;
      }
    }

    .text-ellipsis {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      display: inline-block;
      vertical-align: middle;
    }

    .tag-ellipsis {
      flex-shrink: 0;
      flex-grow: 0;
      flex-basis: auto;
      min-width: 40px;
      max-width: none;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.panel-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 6px;
  border-top: 1px solid #ebeef5;
  font-size: 13px;
  color: #606266;
  background: #f5f5f5;
}

</style>
