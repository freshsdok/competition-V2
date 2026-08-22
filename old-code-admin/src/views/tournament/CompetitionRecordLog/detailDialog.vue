<template>
  <el-dialog
    v-model="visible"
    title="变动数据详情对比"
    width="90%"
    :close-on-click-modal="false"
    destroy-on-close
    style="margin-bottom: 6vh;"
  >
  <div>
    <div class="glb-mon-title">变动前数据</div>
    <template v-if="detailData.oldDataMap">
      <SelfTable :data="detailData.oldDataMap" :showDiff="true"/>
    </template>
    <div class="g_no_data" v-else>暂无数据</div>
    <div class="glb-mon-title" style="margin: 30px 0 10px;">变动后数据</div>
    <template v-if="detailData.newDataMap">
      <SelfTable :data="detailData.newDataMap" :showDiff="true" />
    </template>
    <div v-else class="g_no_data">暂无数据</div>
  </div>
  </el-dialog>
</template>

<script setup>
import SelfTable from './table.vue'
// 定义组件属性
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  detailData: {
    type: Object,
    default: () => ({})
  }
})

// 定义事件
const emit = defineEmits(['update:visible', 'save-success'])

// 监听visible变化，更新父组件状态
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})
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