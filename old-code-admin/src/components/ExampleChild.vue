<template>
  <div class="child-container">
    <h3>子组件</h3>
    <p>子组件接收的数据: {{ info.name }} - {{ info.value }}</p>
    
    <div class="form-group">
      <label>修改名称:</label>
      <input 
        type="text" 
        v-model="localInfo.name"
        @input="updateParentData"
        placeholder="输入名称"
      />
    </div>
    
    <div class="form-group">
      <label>修改值:</label>
      <input 
        type="text" 
        v-model="localInfo.value"
        @input="updateParentData"
        placeholder="输入值"
      />
    </div>
    
    <button @click="updateParentData" class="update-btn">更新父组件数据</button>
  </div>
</template>

<script>
import { ref, watch } from 'vue'

export default {
  name: 'ExampleChild',
  // 定义props，接收从父组件传递过来的数据
  props: {
    info: {
      type: Object,
      required: true,
      default: () => ({})
    }
  },
  emits: ['update:info'], // 声明要发出的事件
  setup(props, { emit }) {
    // 创建本地响应式数据，用于表单绑定
    const localInfo = ref({ ...props.info })
    
    // 监听props.info的变化，同步到本地数据
    watch(() => props.info, (newInfo) => {
      localInfo.value = { ...newInfo }
    }, { deep: true })
    
    // 更新父组件数据的方法
    const updateParentData = () => {
      // 发送update:info事件，这是v-model:info双向绑定的关键
      emit('update:info', { ...localInfo.value })
    }
    
    return {
      localInfo,
      updateParentData
    }
  }
}
</script>

<style scoped>
.child-container {
  padding: 15px;
  border: 1px solid #4a90e2;
  margin-top: 10px;
  border-radius: 5px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.update-btn {
  background-color: #4a90e2;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.update-btn:hover {
  background-color: #357abd;
}
</style>