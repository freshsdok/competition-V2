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
      <el-table-column label="操作人" prop="operatorUser" width="100"></el-table-column>
      <el-table-column label="变更类型" prop="changeType" width="140">
        <template #default="scope">
            <dict-tag :options="changeTypeArr" :value="scope.row.changeType" />
        </template>
      </el-table-column>
      <el-table-column label="变更前手机号" prop="phoneOld" min-width="120"></el-table-column>
      <el-table-column label="变更后手机号" prop="phoneNew" min-width="120">
        <template #default="scope">
          <span :class="{'news': scope.row.phoneNew !== scope.row.phoneOld}">{{ scope.row.phoneNew }}</span>
        </template>
      </el-table-column>
      <el-table-column label="变更前邮箱" prop="emailOld" min-width="160"></el-table-column>
      <el-table-column label="变更后邮箱" prop="emailNew" min-width="160">
        <template #default="scope">
          <span :class="{'news': scope.row.emailNew !== scope.row.emailOld}">{{ scope.row.emailNew }}</span>
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
.news{
  color: red;

}
</style>