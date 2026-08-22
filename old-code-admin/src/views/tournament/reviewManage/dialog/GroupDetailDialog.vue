<template>
  <el-dialog v-model="visible" :title="`分组详情 - ${groupName || ''}`" width="700px">
    <el-table
      ref="tableRef"
      :data="pagedTasks"
      size="small"
      height="400px"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column prop="reviewName" label="项目名称" min-width="150">
        <template #default="{ row }">
          {{ row?.reviewTaskInfo?.reviewName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="赛事/赛项/组别" min-width="150">
        <template #default="{ row }">
          <el-tooltip :content="`${row?.reviewTaskInfo?.competitionName || '-'}/${row?.reviewTaskInfo?.competitionTrackName || '-'}/${row?.reviewTaskInfo?.secondLevelName || '-'}`" placement="top" :show-after="500">
            <div class="multi-line-ellipsis">
              <span>{{ row?.reviewTaskInfo?.competitionName || '-' }}</span>
              <span>/{{ row?.reviewTaskInfo?.competitionTrackName || '-' }}</span>
              <span>/{{ row?.reviewTaskInfo?.secondLevelName || '-' }}</span>
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="province" label="省份" width="80">
        <template #default="{ row }">
          {{ row?.reviewTaskInfo?.province || '-' }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="80" align="center">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleRemove(row)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="dialog-pagination">
      <pagination
        size="small"
        v-show="tasks?.length > 0"
        :total="tasks?.length || 0"
        v-model:page="pageNum"
        v-model:limit="pageSize"
        @pagination="handlePagination"
      />
    </div>
    <template #footer>
      <el-button type="danger" size="small" :disabled="selectedIds?.length === 0" @click="handleBatchRemove">批量移除</el-button>
      <el-button size="small" @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  groupName: {
    type: String,
    default: ''
  },
  tasks: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'remove', 'batchRemove'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const tableRef = ref(null)
const selectedIds = ref([])
const pageNum = ref(1)
const pageSize = ref(10)

const pagedTasks = computed(() => {
  return props.tasks || []
})

function handleSelectionChange(selection) {
  selectedIds.value = selection?.map(item => item?.reviewId) || []
}

function handlePagination() {
  // 前端分页，无需重新加载数据
}

async function handleRemove(row) {
  try {
    await ElMessageBox.confirm('确定将该项目从分组中移除吗？', '确认移除', { type: 'warning' })
    emit('remove', row)
    // 如果当前页没有数据了，回到上一页
    const totalPages = Math.ceil((props.tasks?.length || 0) / pageSize.value)
    if (pageNum.value > totalPages && totalPages > 0) {
      pageNum.value = totalPages
    }
  } catch {
    // 取消
  }
}

async function handleBatchRemove() {
  if (selectedIds.value?.length === 0) return
  try {
    await ElMessageBox.confirm(`确定将选中的 ${selectedIds.value?.length} 个任务从分组中移除吗？`, '确认移除', { type: 'warning' })
    emit('batchRemove', selectedIds.value)
    selectedIds.value = []
    // 如果当前页没有数据了，回到上一页
    const totalPages = Math.ceil((props.tasks?.length || 0) / pageSize.value)
    if (pageNum.value > totalPages && totalPages > 0) {
      pageNum.value = totalPages
    }
  } catch {
    // 取消
  }
}

function handleClose() {
  visible.value = false
  selectedIds.value = []
  pageNum.value = 1
}
</script>

<style scoped lang="scss">
.dialog-pagination {
  display: flex;
  justify-content: flex-start;
  padding: 12px 0 0;
  border-top: 1px solid #ebeef5;
  margin-top: 12px;

  :deep(.pagination-container) {
    margin: 0;
  }
}
</style>
