<!-- components/AnimatedInput.vue -->
<template>
  <div
    class="animated-input"
    :class="{ filled: isFilled, focused: isFocused }"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <!-- 实际输入框 -->
    <input
      :type="type"
      class="animated-input__input"
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      @focus="isFocused = true"
      @blur="isFocused = false"
    />

    <!-- 动画占位符：逐字显示 placeholder -->
    <div class="animated-input__placeholder">
      <span
        v-for="(letter, index) in placeholder"
        :key="index"
        class="animated-input__letter"
        :style="{
          animationDelay: `${index * 0.05}s`,
          animationPlayState: shouldAnimate ? 'running' : 'paused'
        }"
      >
        {{ letter }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'

// 定义 props
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: 'Input'
  },
  type: {
    type: String,
    default: 'text'
  }
})

// 定义事件
defineEmits(['update:modelValue'])

// 响应式状态
const isFocused = ref(false)
const isHovered = ref(false)
const shouldAnimate = ref(true)

// 是否已输入内容
const isFilled = computed(() => props.modelValue !== '')

// 组件挂载后启用动画
onMounted(() => {
  shouldAnimate.value = true
})
</script>

<style scoped>
.animated-input {
  position: relative;
  width: 620px;
  margin: 20px 0;
}

.animated-input__input {
  width: 100%;
  padding: 16px 20px;
  font-size: 16px;
  color: #ffffff;
  background: #1a1a1f;
  border: 2px solid #2a2a3a;
  border-radius: 12px;
  outline: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
}

.animated-input__input::placeholder {
  color: transparent;
}

.animated-input.focused .animated-input__input,
.animated-input:hover .animated-input__input {
  border-color: #6c63ff;
  box-shadow: 0 0 0 4px rgba(108, 99, 255, 0.2);
}

/* 占位符容器 */
.animated-input__placeholder {
  position: absolute;
  left: 20px;
  top: 16px;
  font-size: 16px;
  pointer-events: none;
  display: flex;
}

/* 字母动画 */
.animated-input__letter {
  display: inline-block;
  color: #5a5a72;
  opacity: 0;
  transform: translateY(-20px);
  animation: letterDrop 0.8s ease-out forwards;
}

@keyframes letterDrop {
  0% {
    opacity: 0;
    transform: translateY(-20px) rotate(-10deg);
  }
  50% {
    opacity: 1;
    transform: translateY(5px) rotate(5deg);
  }
  100% {
    opacity: 1;
    transform: translateY(0) rotate(0);
  }
}

/* 输入或聚焦时：字母上浮缩小 */
.animated-input.filled .animated-input__letter,
.animated-input.focused .animated-input__letter {
  animation: letterRise 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@keyframes letterRise {
  0% {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
  100% {
    transform: translateY(-22px) scale(0.85);
    opacity: 0.7;
  }
}
</style>