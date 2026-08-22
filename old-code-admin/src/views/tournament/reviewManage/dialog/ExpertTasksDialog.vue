<template>
  <el-dialog v-model="visible" :title="`专家已分配项目 - ${expert?.nickName || '-'}`" width="800px">
    <div v-if="expert" class="expert-tasks-dialog">
      <el-table :data="expert?.reviewTaskInfoList || []" size="small" height="400px">
        <el-table-column prop="reviewName" label="项目名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="赛事/赛项/组别" min-width="150">
          <template #default="{ row }">
            <el-tooltip :content="`${row.competitionName || '-'}/${row.competitionTrackName || '-'}/${row.secondLevelName || '-'}`" placement="top" :show-after="500">
              <div class="multi-line-ellipsis">
                <span>{{ row.competitionName || '-' }}</span>
                <span>/{{ row.competitionTrackName || '-' }}</span>
                <span>/{{ row.secondLevelName || '-' }}</span>
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="province" label="省份" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.distributeStatus == '1' ? 'success' : 'info'">{{ row.distributeStatus == '1' ? '已分配' : '分配中' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <el-button size="small" @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  expert: {
    type: Object,
    default: null
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
.expert-tasks-dialog {
  .expert-info {
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #ebeef5;

    strong {
      font-size: 16px;
      margin-right: 12px;
    }

    span {
      color: #909399;
      margin-right: 12px;
    }
  }
}
</style>
