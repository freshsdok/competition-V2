<template>
  <el-dialog
    v-model="visible"
    title="变更记录"
    width="1100px"
    :close-on-click-modal="false"
    destroy-on-close
    style="margin-bottom: 6vh;"
  >
  <div>
    <el-table v-loading="loading"
              :data="detailData">
      <el-table-column label="变更时间" prop="changeTime" width="160"></el-table-column>
      <el-table-column label="操作人" prop="operatorUser" min-width="100"></el-table-column>
      <el-table-column label="变更类型" prop="changeType" min-width="140">
        <template #default="scope">
            <dict-tag :options="changeTypeArr" :value="scope.row.changeType" />
        </template>
      </el-table-column>
      <el-table-column label="变更前" min-width="200">
        <template #default="scope">
          <div v-if="scope.row.changeType === 'changeTeacher'" v-html="highlightDiff(scope.row.teacherNameOld, scope.row.teacherNameNew)"></div>
          <div v-else v-html="highlightDiff(scope.row.memberNameOld, scope.row.memberNameNew)"></div>
        </template>
      </el-table-column>
      <el-table-column label="变更后" min-width="200">
        <template #default="scope">
          <div v-if="scope.row.changeType === 'changeTeacher'" v-html="highlightDiff(scope.row.teacherNameNew, scope.row.teacherNameOld)"></div>
          <div v-else v-html="highlightDiff(scope.row.memberNameNew, scope.row.memberNameOld)"></div>
        </template>
      </el-table-column>
    </el-table>
  </div>
  </el-dialog>
</template>

<script setup>
import { cloneDeep } from 'lodash-es';
// 定义组件属性
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  detailData: {
    type: [Array, Object],
    default: () => ([])
  },
  changeTypeArr: {
    type: Array,
    default: () => ([])
  }
})

// 定义事件
const emit = defineEmits(['update:visible', 'save-success'])

// 监听visible变化，更新父组件状态
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

let loading = $ref(false)

// 高亮对比函数
function highlightDiff(currentStr, compareStr) {
  // 防错处理：确保输入是字符串
  if (!currentStr || typeof currentStr !== 'string') {
    return currentStr || ''
  }
  if (!compareStr || typeof compareStr !== 'string') {
    compareStr = ''
  }
  
  // 分割成数组
  const currentArr = currentStr.split(',').map(item => item.trim()).filter(Boolean)
  const compareArr = compareStr.split(',').map(item => item.trim()).filter(Boolean)
  
  // 统计每个名字在两个数组中的出现次数
  const currentCount = {}
  const compareCount = {}
  
  currentArr.forEach(item => {
    currentCount[item] = (currentCount[item] || 0) + 1
  })
  
  compareArr.forEach(item => {
    compareCount[item] = (compareCount[item] || 0) + 1
  })
  
  // 生成高亮HTML
  const result = []
  const tempCount = {}
  
  currentArr.forEach(item => {
    // 记录当前处理的名字的临时计数
    tempCount[item] = (tempCount[item] || 0) + 1
    
    // 如果当前名字的临时计数超过对比数组中的计数，则高亮
    if (tempCount[item] > (compareCount[item] || 0)) {
      result.push(`<span style="background-color: rgba(255, 0, 0, 1); color: white; padding: 0 2px; border-radius: 2px;">${item}</span>`)
    } else {
      result.push(item)
    }
  })
  
  return result.join(', ')
}

</script>

<style scoped lang="scss">
:deep(.el-descriptions) {
  .el-descriptions__table{
    width: 100%;
    .el-descriptions__content{
      max-width: 0;
    }
  }
  .el-descriptions__label{
    padding: 2px 4px !important;
  }
}
.form-title{
  color: #303133;
  font-size: 16px;
  font-weight: bold;
  margin: 20px 0 10px;
}
.footer{ 
  text-align: center;
}
</style>