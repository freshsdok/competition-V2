<template>
  <div class="test-container">
    <h1>Vue 3 v-model:info 双向绑定示例</h1>
    <ExampleParent />
    
    <div class="instructions">
      <h3>说明：</h3>
      <p>1. 父组件通过 <code>v-model:info="from"</code> 将数据传递给子组件</p>
      <p>2. 子组件通过 <code>emit('update:info', newData)</code> 向父组件发送更新</p>
      <p>3. 这种方式支持对象类型的数据双向绑定</p>
      <p>4. 子组件内部维护了本地副本，避免直接修改props</p>
    </div>
  </div>
</template>

<script>
import ExampleParent from '@/components/ExampleParent.vue'

export default {
  name: 'TestVModel',
  components: {
    ExampleParent
  }
}
</script>

<style scoped>
.test-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.instructions {
  margin-top: 30px;
  padding: 15px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.instructions code {
  background-color: #eee;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
}
</style>