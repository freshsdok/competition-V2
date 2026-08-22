<template>
  <div class="parent-container">
    <h2>父组件</h2>
    <p>父组件中的数据: {{ from }}</p>
    <!-- 使用v-model:info进行双向绑定 -->
    <ExampleChild v-model:info="from" />
  </div>
</template>

<script>
import { ref } from 'vue'
import ExampleChild from './ExampleChild.vue'

export default {
  name: 'ExampleParent',
  components: {
    ExampleChild
  },
  setup() {
    // 父组件中的响应式数据
    const from = ref({
      name: '初始名称',
      value: '初始值'
    })
    
    return {
      from
    }
  }
}
</script>

<style scoped>
.parent-container {
  padding: 20px;
  border: 1px solid #ccc;
  margin: 20px;
}
</style>