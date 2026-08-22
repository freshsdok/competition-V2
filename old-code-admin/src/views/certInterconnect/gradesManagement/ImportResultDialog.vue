<template>
  <el-dialog v-model="visible" title="重复导入确认" width="900px" :close-on-click-modal="false" :show-close="false">
    <div class="dialog-header-btns">
      <el-button type="primary" @click="handleCoverUpdate">覆盖更新</el-button>
      <el-button @click="handleGiveUp">放弃</el-button>
    </div>
    <el-table :data="tableData" stripe border style="margin-top: 16px;">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="userName" label="姓名" show-overflow-tooltip />
      <el-table-column prop="idCard" label="身份证号" show-overflow-tooltip />
      <el-table-column prop="score" label="成绩" align="center">
        <template #default="{ row }">
          <span>{{ row.score ?? '-' }}</span>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  tableData: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'cover-update', 'give-up'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const handleCoverUpdate = () => {
  emit('cover-update', props.tableData)
}

const handleGiveUp = () => {
  emit('give-up')
}
</script>

<style scoped>
.dialog-header-btns {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}
</style>
