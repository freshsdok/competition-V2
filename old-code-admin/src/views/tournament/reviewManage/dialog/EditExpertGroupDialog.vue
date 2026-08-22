<template>
  <el-dialog
    v-model="visible"
    title="修改专家组"
    width="560px"
    :close-on-click-modal="false"
  >
    <el-form label-position="top">
      <!-- 组名 -->
      <el-form-item label="组名">
        <el-input
          v-model="form.groupName"
          placeholder="请输入专家组名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <!-- 已添加专家 -->
      <el-form-item>
        <template #label>
          <span>已添加专家 ({{ selectedExperts.length }}人)</span>
        </template>
        <div class="selected-experts-container">
          <div v-if="selectedExperts.length > 0" class="selected-experts-grid">
            <el-tooltip
              v-for="expert in selectedExperts"
              :key="expert.userId"
              placement="top"
              :show-after="200"
            >
              <template #content>
                <div class="expert-tooltip">
                  <div class="tooltip-name">{{ expert.nickName }}</div>
                  <div class="tooltip-info">职称：{{ expert.position || '-' }}</div>
                  <div class="tooltip-info">学校：{{ expert.schoolName || '-' }}</div>
                  <div class="tooltip-info">省份：{{ expert.province || '-' }}</div>
                </div>
              </template>
              <div class="expert-card">
                <el-avatar :size="24" :src="expert.avatar || defAva" />
                <span class="card-name" :title="expert.nickName">{{ expert.nickName }}</span>
                <el-button
                  link
                  type="danger"
                  size="small"
                  class="remove-btn"
                  @click="removeExpert(expert.userId)"
                >
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
            </el-tooltip>
          </div>
          <div v-else class="empty-tip">暂无专家，请从下方添加</div>
        </div>
      </el-form-item>

      <!-- 可添加专家 -->
      <el-form-item>
        <template #label>
          <div class="section-header">
            <span>可添加专家</span>
          </div>
        </template>
        <!-- 搜索框 -->
        <el-input
          v-model="availableSearchKeyword"
          placeholder="搜索专家姓名/学院"
          clearable
          size="small"
          class="search-input"
        />
        <div class="experts-list-container">
          <div v-for="expert in filteredAvailableExperts" :key="expert.userId" class="expert-row">
            <el-avatar :size="24" :src="expert.avatar || defAva" />
            <span class="expert-name">{{ expert.nickName }}</span>
            <span class="expert-title">{{ expert.position || '-' }}</span>
            <span class="expert-dept">{{ expert.schoolName || '-' }}</span>
            <span class="expert-province">{{ expert.province || '-' }}</span>
            <el-button
              link
              type="primary"
              size="large"
              @click="addExpert(expert.userId)"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
          <div v-if="filteredAvailableExperts.length === 0" class="empty-tip">
            {{ availableSearchKeyword ? '无匹配的专家' : '暂无可添加的专家' }}
          </div>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSave">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { updateExpertGroup } from '@/api/tournament/reviewManage'
import defAva from '@/assets/images/profile.png'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  group: { type: Object, default: () => null },
  experts: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const form = ref({
  groupId: null,
  groupName: '',
  specialistUserIdList: []
})

// 搜索关键词（仅可添加专家区域使用）
const availableSearchKeyword = ref('')

// 已添加专家列表
const selectedExperts = computed(() => {
  return props.experts.filter(e => form.value.specialistUserIdList.includes(e.userId))
})

// 筛选后的可添加专家 - 排除已添加的专家
const filteredAvailableExperts = computed(() => {
  // 获取已添加专家的 userId 集合
  const selectedUserIds = new Set(form.value.specialistUserIdList)
  // 过滤掉已添加的专家
  let list = props.experts.filter(e => !selectedUserIds.has(e.userId))
  // 搜索过滤
  if (availableSearchKeyword.value) {
    const kw = availableSearchKeyword.value.toLowerCase()
    list = list.filter(e =>
      e.nickName?.toLowerCase().includes(kw) ||
      e.schoolName?.toLowerCase().includes(kw)
    )
  }
  return list
})

// 监听group变化，初始化表单
watch(() => props.group, (newGroup) => {
  if (newGroup) {
    form.value = {
      groupId: newGroup.groupId,
      groupName: newGroup.groupName || '',
      specialistUserIdList: newGroup.reviewGroupSpecialistRelationList?.map(r => r.userId) || []
    }
    // 清空搜索
    availableSearchKeyword.value = ''
  }
}, { immediate: true })

// 添加专家到组
function addExpert(userId) {
  if (form.value.specialistUserIdList.includes(userId)) {
    const expert = props.experts.find(e => e.userId === userId)
    ElMessage.warning(`专家「${expert?.nickName || ''}」已添加`)
    return
  }
  form.value.specialistUserIdList.push(userId)
}

// 从组中移除专家
function removeExpert(userId) {
  const index = form.value.specialistUserIdList.indexOf(userId)
  if (index > -1) {
    form.value.specialistUserIdList.splice(index, 1)
  }
}

// 保存
async function handleSave() {
  if (!form.value.groupName.trim()) {
    ElMessage.warning('请输入专家组名称')
    return
  }
  if (form.value.specialistUserIdList.length === 0) {
    ElMessage.warning('请至少选择一位专家')
    return
  }

  // 检查是否有重复专家
  const uniqueIds = [...new Set(form.value.specialistUserIdList)]
  if (uniqueIds.length !== form.value.specialistUserIdList.length) {
    ElMessage.warning('专家列表中存在重复，已自动去重')
    form.value.specialistUserIdList = uniqueIds
  }

  loading.value = true
  try {
    await updateExpertGroup({
      groupId: form.value.groupId,
      groupName: form.value.groupName.trim(),
      specialistUserIdList: form.value.specialistUserIdList
    })
    ElMessage.success('修改成功')
    visible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-input {
  margin-bottom: 8px;
}

// 已添加专家区域 - 卡片式并排展示
.selected-experts-container {
  max-height: 150px;
  overflow-y: auto;
  padding: 8px;
  width: 100%;
}

.selected-experts-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .expert-card {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 8px;
    transition: all 0.2s;
    cursor: pointer;
    background: #f5f5f5;

    &:hover {
      background: #f5f7fa;
      border-radius: 4px;
    }

    .card-name {
      font-size: 13px;
      color: #303133;
      max-width: 80px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .remove-btn {
      padding: 2px;
      margin-left: 2px;
    }
  }
}

// Tooltip 样式
.expert-tooltip {
  .tooltip-name {
    font-weight: 500;
    font-size: 14px;
    margin-bottom: 4px;
    color: #fff;
  }

  .tooltip-info {
    font-size: 12px;
    color: #e0e0e0;
    line-height: 1.6;
  }
}

// 可添加专家区域
.experts-list-container {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 8px;
  width: 100%;
}

.expert-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #f5f7fa;
  }

  .expert-name {
    width: 60px;
    font-size: 13px;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .expert-title {
    width: 60px;
    font-size: 12px;
    color: #606266;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .expert-dept {
    width: 100px;
    font-size: 12px;
    color: #909399;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
  }

  .expert-province {
    width: 50px;
    font-size: 12px;
    color: #909399;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 20px;
  font-size: 13px;
}
</style>
