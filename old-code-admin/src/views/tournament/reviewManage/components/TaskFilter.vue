<template>
  <div class="task-filter">
    <!-- 筛选下拉 -->
    <div class="filter-row">
      <!-- 1. 赛事名称输入框 -->
      <el-input
        v-model="filter.competitionName"
        placeholder="搜索赛事"
        clearable
        size="small"
        style="width: 160px"
      />
      <!-- 2. 赛道组别聚合查询输入框 -->
      <el-input
        v-model="filter.competitionTrackQuery"
        placeholder="搜索赛项、组别"
        clearable
        size="small"
        style="width: 160px"
      />
      <!-- 3. 省份改为可多选下拉 -->
      <el-select
        v-model="selectedProvinces"
        placeholder="全部省份"
        clearable
        filterable
        multiple
        collapse-tags
        :max-collapse-tags="2"
        :filter-method="filterProvince"
        size="small"
        style="width: 230px"
      >
        <el-option
          v-for="item in filteredProvinceOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-select
        v-model="filter.taskGroupId"
        placeholder="全部任务分组"
        clearable
        filterable
        size="small"
        style="width: 140px"
      >
        <el-option
          v-for="item in taskGroupOptions"
          :key="item.reviewGroupId"
          :label="item.allotGroupName"
          :value="item.reviewGroupId"
        />
      </el-select>
      <el-input
        v-model="filter.keyWords"
        placeholder="搜索学校/带队老师/队长"
        clearable
        size="small"
        style="width: 200px"
      />
    </div>

    <!-- 状态筛选 -->
    <div class="status-row">
      <div class="status-tabs">
        <span
          v-for="tab in statusTabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: filter.distributeStatus === tab.value }"
          @click="handleStatusChange(tab.value)"
        >
          {{ tab.label }}
        </span>
      </div>
      <div class="action-btns">
        <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
        <el-button size="small" @click="handleReset">重置</el-button>
        <el-button type="primary" size="small" @click="$emit('create-group')">创建分组</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, ref, computed } from 'vue'

const props = defineProps({
  provinceOptions: { type: Array, default: () => [] },
  taskGroupOptions: { type: Array, default: () => [] }
})

const emit = defineEmits(['create-group', 'filter-change', 'search', 'reset'])

const filter = reactive({
  competitionName: '',
  competitionTrackQuery: '',
  province: '',
  taskGroupId: '',
  keyWords: '',
  distributeStatus: ''
})

// 多选省份数组
const selectedProvinces = ref([])

// 省份过滤关键字
const provinceFilterText = ref('')

// 过滤后的省份选项
const filteredProvinceOptions = computed(() => {
  if (!provinceFilterText.value) return props.provinceOptions
  return props.provinceOptions.filter(item =>
    item.label.includes(provinceFilterText.value)
  )
})

// 省份过滤方法
function filterProvince(query) {
  provinceFilterText.value = query
}

const statusTabs = [
  { label: '全部', value: '' },
  { label: '已分配', value: '1' },
  { label: '分配中', value: '0' }
]

watch(filter, (val) => {
  emit('filter-change', val)
}, { deep: true })

function handleSearch() {
  try {
    // 将多选省份数组转换为逗号分隔字符串
    const searchFilter = { ...filter }
    searchFilter.province = selectedProvinces.value.length > 0 ? selectedProvinces.value.join(',') : ''
    emit('search', searchFilter)
  } catch (error) {
    console.error('搜索参数处理失败:', error)
  }
}

function handleStatusChange(value) {
  filter.distributeStatus = value
  // 状态改变立即触发搜索
  emit('search', filter)
}

function handleReset() {
  filter.competitionName = ''
  filter.competitionTrackQuery = ''
  filter.province = ''
  filter.taskGroupId = ''
  filter.keyWords = ''
  filter.distributeStatus = ''
  selectedProvinces.value = []
  // 重置后立即触发搜索（不传distributeStatus）
  emit('search', filter)
}
</script>

<style scoped lang="scss">
.task-filter {
  padding: 6px 0;
  background: #fff;
  border-bottom: 1px solid #ebeef5;

  .filter-row {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
    flex-wrap: wrap;

    .el-select {
      width: 160px;
    }
  }

  .status-row {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .status-tabs {
      display: flex;
      gap: 12px;

      .tab-item {
        padding: 3px 12px;
        border-radius: 20px;
        border: 1px solid #dcdfe6;
        font-size: 13px;
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

    .action-btns {
      display: flex;
      gap: 8px;
    }
  }
}
</style>
