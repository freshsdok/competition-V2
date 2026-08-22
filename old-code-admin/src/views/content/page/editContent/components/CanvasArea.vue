<template>
  <div class="canvas-area-container">
    <div class="canvas-wrapper">
      <div class="description">
        页面实际效果以预览页面为准,当前页面为：{{ $route.query?.pageName }}
      </div>
      <div
        class="canvas"
        :class="{ 'show-mini': displayPlatform === 'mini' }"
        @dragover="handleCanvasDragOver"
        @drop="handleDrop"
        @click="handleCanvasClick"
      >
        <!-- 空画布提示 -->
        <div v-if="canvasComponents.length === 0" class="empty-canvas">
          <i class="el-icon-upload2"></i>
          <p>拖拽组件到此处或从左侧组件库点击添加</p>
        </div>

        <!-- 组件列表 -->
        <div
          v-for="(component, index) in canvasComponents"
          :key="component.id || index"
          class="canvas-component"
          :class="{ selected: selectedComponentId === (component.id || index) }"
          @click.stop="selectComponent(component, index)"
          draggable="true"
          @dragstart="handleDragStart($event, component, index)"
          @dragover.prevent="handleDragOver($event, index)"
          @dragleave="handleDragLeave($event)"
          @drop="handleComponentDrop($event, index)"
          @dragend="handleDragEnd()"
        >
          <component
            :is="getComponentRenderer(component.type)"
            :componentData="component"
            :key="component.id || index"
            @update:component-data="updateComponentData(index, $event)"
          />
          <div
            class="component-controls"
            v-if="selectedComponentId === (component.id || index)"
          >
            <el-button
              size="small"
              type="primary"
              @click.stop="moveComponentUp(index)"
              :disabled="index === 0"
              class="move-button"
            >
              <!-- <el-icon><ArrowUp /></el-icon> -->
              <el-icon><Top /></el-icon>
            </el-button>
            <el-button
              size="small"
              type="primary"
              @click.stop="moveComponentDown(index)"
              :disabled="index === canvasComponents.length - 1"
              class="move-button"
            >
              <el-icon><Bottom /></el-icon>
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click.stop="removeComponent(index)"
              class="delete-button"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 底部放置区域，用于支持拖放到画布最底部 -->
        <div
          class="canvas-bottom-drop-zone"
          @dragover.prevent="handleBottomDropZoneDragOver"
          @dragleave="handleBottomDropZoneDragLeave"
          @drop="handleBottomDropZoneDrop"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup name="CanvasArea">
import {
  ref,
  defineProps,
  defineEmits,
  computed,
  nextTick,
  onMounted,
  onUnmounted,
} from "vue";
import { useRoute } from "vue-router";
import { createComponentInstance } from "../utils/componentCreator";
import { getComponentRenderer } from "../utils/componentRenderer";
import { cloneDeep } from "lodash";
import { Delete, ArrowUp, ArrowDown } from "@element-plus/icons-vue";
const route = useRoute();
let displayPlatform = computed(() =>
  props.preview ? props.displayPlatform : route.query?.displayPlatform
);

// 定义props
const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
  preview: {
    type: Boolean,
    default: false,
  },
  displayPlatform: {
    type: String,
    default: null,
  },
});

// 定义emits
const emit = defineEmits(["update:modelValue", "componentSelect"]);

// 画布组件数据
const canvasComponents = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit("update:modelValue", value);
  },
});

// 当前选中的组件ID
const selectedComponentId = ref(null);

// 拖拽状态
const draggedComponent = ref(null);
const draggedIndex = ref(-1);
const dropTargetIndex = ref(-1); // 记录从组件库拖拽到画布时的目标位置
const isDraggingFromLibrary = ref(false); // 标记是否从组件库拖拽

// 处理画布空白区域的拖拽放置
const handleDrop = (event) => {
  event.preventDefault();

  try {
    // 检查是否从组件库拖拽
    const componentDataStr = event.dataTransfer.getData("component");
    if (componentDataStr && componentDataStr.trim() !== "") {
      try {
        // 解析组件数据
        const componentData = JSON.parse(componentDataStr);

        if (componentData && typeof componentData === "object") {
          const componentInstance = createComponentInstance(componentData);
          const deepCopiedInstance = cloneDeep(componentInstance);

          // 创建数组的深拷贝
          const newComponents = cloneDeep(canvasComponents.value);

          // 添加到末尾
          newComponents.push(deepCopiedInstance);

          // 更新组件数组
          canvasComponents.value = newComponents;

          // 更新选中状态
          selectedComponentId.value = null;
          nextTick(() => {
            const newIndex = newComponents.length - 1;
            const selectedComponent = cloneDeep(deepCopiedInstance);
            selectedComponentId.value = selectedComponent.id;
            emit("componentSelect", selectedComponent, newIndex);
          });
        }
      } catch (parseError) {
        console.warn("组件数据解析失败:", parseError);
      }
    }
  } catch (error) {
    console.error("拖拽处理异常:", error);
  } finally {
    // 清理状态
    setTimeout(() => {
      dropTargetIndex.value = -1;
      isDraggingFromLibrary.value = false;
      handleDragEnd();
    }, 0);
  }
};

// 选择组件 - 使用深拷贝避免引用问题
const selectComponent = (component, index) => {
  selectedComponentId.value = component.id || index;
  // 传递深拷贝的组件数据给父组件，避免属性修改时影响原组件
  const deepCopiedComponent = cloneDeep(component);
  emit("componentSelect", deepCopiedComponent, index);
};

// 更新组件数据 - 使用深拷贝避免引用问题
const updateComponentData = (index, newData) => {
  // 创建数组的深拷贝
  const newComponents = cloneDeep(canvasComponents.value);

  // 对特定组件进行深度合并更新
  // 对于轮播图组件，特别确保dataSourceId和dataSourceName被正确更新
  if (newData.type === "global_banner") {
    console.log(
      "更新轮播图组件数据，dataSourceId:",
      newData.dataSourceId,
      "dataSourceName:",
      newData.dataSourceName
    );

    // 先复制原有组件的所有属性
    newComponents[index] = cloneDeep(newComponents[index]);

    // 然后手动合并新数据中的所有属性，确保不丢失任何字段
    for (const key in newData) {
      if (newData.hasOwnProperty(key)) {
        // 特别处理style对象，确保深度合并
        if (key === "style" && typeof newData.style === "object") {
          newComponents[index].style = newComponents[index].style || {};
          Object.assign(newComponents[index].style, newData.style);
        } else {
          // 对于其他属性，直接赋值
          newComponents[index][key] = cloneDeep(newData[key]);
        }
      }
    }
  } else {
    // 对于非轮播图组件，使用标准合并
    newComponents[index] = { ...newComponents[index], ...cloneDeep(newData) };
  }

  canvasComponents.value = newComponents;
};

// 处理组件拖拽开始
const handleDragStart = (event, component, index) => {
  draggedComponent.value = component;
  draggedIndex.value = index;

  // 设置拖拽数据
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = "move";
    event.dataTransfer.setData("text/plain", `component_${index}`);
  }

  // 设置拖拽时的样式提示
  setTimeout(() => {
    const element = event.target;
    if (element) {
      element.classList.add("dragging");
    }
  }, 0);
};

// 处理拖拽悬停
const handleDragOver = (event, index) => {
  event.preventDefault();
  // 添加拖拽悬停样式
  const element = event.currentTarget;
  if (element) {
    element.classList.add("drag-over");
  }

  // 检查是否从组件库拖拽
  const componentDataStr = event.dataTransfer.getData("component");
  isDraggingFromLibrary.value =
    !!componentDataStr && draggedComponent.value === null;

  // 更新目标位置，无论是从组件库拖拽还是内部组件拖拽
  dropTargetIndex.value = index;
};

// 处理拖拽离开
const handleDragLeave = (event) => {
  const element = event.currentTarget;
  if (element) {
    element.classList.remove("drag-over");
  }
};

// 处理画布拖拽悬停
const handleCanvasDragOver = (event) => {
  event.preventDefault();
  // 检查是否从组件库拖拽
  const componentDataStr = event.dataTransfer.getData("component");
  isDraggingFromLibrary.value =
    !!componentDataStr && draggedComponent.value === null;

  // 如果是从组件库拖拽到画布空白区域，设置插入位置为末尾
  if (isDraggingFromLibrary.value) {
    dropTargetIndex.value = -1;
  }
};

// 处理拖拽结束
const handleDragEnd = () => {
  // 移除所有拖拽相关的样式
  const elements = document.querySelectorAll(".canvas-component");
  elements.forEach((el) => {
    el.classList.remove("dragging");
    el.classList.remove("drag-over");
  });

  // 移除底部放置区域的样式
  const bottomZone = document.querySelector(".canvas-bottom-drop-zone");
  if (bottomZone) {
    bottomZone.classList.remove("drag-over");
  }

  // 重置拖拽状态
  draggedComponent.value = null;
  draggedIndex.value = -1;
  isDraggingFromLibrary.value = false;
};

// 处理底部放置区域拖拽悬停
const handleBottomDropZoneDragOver = (event) => {
  event.preventDefault();
  event.stopPropagation();

  // 检查是否是有效的拖拽（从组件库或内部组件）
  const componentDataStr = event.dataTransfer.getData("component");
  const isLibraryDrag = !!componentDataStr && draggedComponent.value === null;
  const isInternalDrag = draggedComponent.value !== null;

  // 支持从组件库拖拽和内部组件拖拽
  if (isLibraryDrag || isInternalDrag) {
    const element = event.currentTarget;
    if (element) {
      element.classList.add("drag-over");
    }
    dropTargetIndex.value = -1; // 表示添加到末尾
  }
};

// 处理底部放置区域拖拽离开
const handleBottomDropZoneDragLeave = (event) => {
  const element = event.currentTarget;
  if (element) {
    element.classList.remove("drag-over");
  }
};

// 处理底部放置区域拖拽放置
const handleBottomDropZoneDrop = (event) => {
  event.preventDefault();
  event.stopPropagation();

  try {
    // 检查是从组件库拖拽还是内部组件拖拽
    const componentDataStr = event.dataTransfer.getData("component");
    const isLibraryDrag = !!componentDataStr && draggedComponent.value === null;
    const isInternalDrag = draggedComponent.value !== null;

    if (isLibraryDrag) {
      // 从组件库拖拽到最底部的情况
      if (componentDataStr && componentDataStr.trim() !== "") {
        try {
          const componentData = JSON.parse(componentDataStr);

          if (componentData && typeof componentData === "object") {
            const componentInstance = createComponentInstance(componentData);
            const deepCopiedInstance = cloneDeep(componentInstance);

            const newComponents = cloneDeep(canvasComponents.value);
            newComponents.push(deepCopiedInstance);

            canvasComponents.value = newComponents;

            selectedComponentId.value = null;
            nextTick(() => {
              const newIndex = newComponents.length - 1;
              const selectedComponent = cloneDeep(deepCopiedInstance);
              selectedComponentId.value = selectedComponent.id;
              emit("componentSelect", selectedComponent, newIndex);
            });
          }
        } catch (parseError) {
          console.warn("组件数据解析失败:", parseError);
        }
      }
    } else if (isInternalDrag) {
      // 内部组件拖拽到最底部的情况
      if (draggedIndex.value !== -1 && draggedComponent.value !== null) {
        const newComponents = cloneDeep(canvasComponents.value);
        const removed = newComponents.splice(draggedIndex.value, 1)[0];

        // 直接添加到数组末尾
        newComponents.push(removed);

        canvasComponents.value = newComponents;

        // 更新选中状态
        const newIndex = newComponents.length - 1;
        selectedComponentId.value = removed.id || newIndex;
        emit("componentSelect", cloneDeep(removed), newIndex);
      }
    }
  } catch (error) {
    console.error("拖拽处理异常:", error);
  } finally {
    // 清理状态
    const element = event.currentTarget;
    if (element) {
      element.classList.remove("drag-over");
    }
    dropTargetIndex.value = -1;
    isDraggingFromLibrary.value = false;
    handleDragEnd();
  }
};

// 处理画布点击
const handleCanvasClick = () => {
  // 清空选中状态
  selectedComponentId.value = null;
  emit("componentSelect", null);
};

// 处理组件上的拖拽放置（同时处理内部组件移动和从组件库拖拽）
const handleComponentDrop = (event, targetIndex) => {
  event.preventDefault();
  event.stopPropagation();

  // 检查是否从组件库拖拽
  const componentDataStr = event.dataTransfer.getData("component");
  const isLibraryDrag = !!componentDataStr && draggedComponent.value === null;

  if (isLibraryDrag) {
    // 从组件库拖拽到组件上的情况
    try {
      const componentData = JSON.parse(componentDataStr);
      if (componentData && typeof componentData === "object") {
        const componentInstance = createComponentInstance(componentData);
        const deepCopiedInstance = cloneDeep(componentInstance);

        const newComponents = cloneDeep(canvasComponents.value);
        newComponents.splice(targetIndex, 0, deepCopiedInstance);

        canvasComponents.value = newComponents;

        selectedComponentId.value = null;
        nextTick(() => {
          const selectedComponent = cloneDeep(deepCopiedInstance);
          selectedComponentId.value = selectedComponent.id;
          emit("componentSelect", selectedComponent, targetIndex);
        });
      }
    } catch (parseError) {
      console.warn("组件数据解析失败:", parseError);
    }
  } else if (draggedComponent.value !== null && draggedIndex.value !== -1) {
    // 内部组件移动的情况
    if (draggedIndex.value !== targetIndex) {
      const newComponents = cloneDeep(canvasComponents.value);
      const removed = newComponents.splice(draggedIndex.value, 1)[0];

      // 计算新的插入位置，处理所有边界情况
      let newIndex = targetIndex;

      // 特殊处理：如果拖动到最后一个组件下方，应该将其放置在数组末尾
      // 我们可以通过检查dropTargetIndex的值来判断是否是这种情况
      if (
        dropTargetIndex.value === canvasComponents.value.length - 1 &&
        draggedIndex.value < canvasComponents.value.length - 1
      ) {
        newIndex = newComponents.length; // 放置在数组末尾
      } else {
        // 常规情况：如果目标位置在原位置之后，需要调整索引
        if (targetIndex > draggedIndex.value) {
          newIndex--;
        }
      }

      // 确保索引在有效范围内
      newIndex = Math.max(0, Math.min(newIndex, newComponents.length));

      // 执行插入操作
      newComponents.splice(newIndex, 0, removed);

      canvasComponents.value = newComponents;

      // 更新选中状态
      selectedComponentId.value = removed.id || newIndex;
      emit("componentSelect", cloneDeep(removed), newIndex);
    }
  }

  // 清理状态
  dropTargetIndex.value = -1;
  isDraggingFromLibrary.value = false;
  handleDragEnd();
};

// 向上移动组件
const moveComponentUp = (index) => {
  // 检查是否已经是第一个组件
  if (index === 0) return;

  // 创建组件数组的深拷贝
  const newComponents = cloneDeep(canvasComponents.value);

  // 交换当前组件和上一个组件的位置
  const temp = newComponents[index];
  newComponents[index] = newComponents[index - 1];
  newComponents[index - 1] = temp;

  // 更新组件数组
  canvasComponents.value = newComponents;

  // 更新选中状态
  const movedComponent = newComponents[index - 1];
  selectedComponentId.value = movedComponent.id || index - 1;
  emit("componentSelect", cloneDeep(movedComponent), index - 1);
};

// 向下移动组件
const moveComponentDown = (index) => {
  // 检查是否已经是最后一个组件
  if (index === canvasComponents.value.length - 1) return;

  // 创建组件数组的深拷贝
  const newComponents = cloneDeep(canvasComponents.value);

  // 交换当前组件和下一个组件的位置
  const temp = newComponents[index];
  newComponents[index] = newComponents[index + 1];
  newComponents[index + 1] = temp;

  // 更新组件数组
  canvasComponents.value = newComponents;

  // 更新选中状态
  const movedComponent = newComponents[index + 1];
  selectedComponentId.value = movedComponent.id || index + 1;
  emit("componentSelect", cloneDeep(movedComponent), index + 1);
};

// 删除组件
const removeComponent = (index) => {
  const newComponents = canvasComponents.value.filter((_, i) => i !== index);
  canvasComponents.value = newComponents;
  selectedComponentId.value = null;
  emit("componentSelect", null);
};

// 组件内部不再处理清空画布，由父组件处理

// 组件渲染器函数已移至 componentRenderer.js

// 不再需要暴露方法给父组件，由父组件直接管理数据
</script>

<style scoped>
.canvas-area-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #e0e0e0;
  border-right: 1px solid #e0e0e0;
}

/* 移除了canvas-header相关样式 */

.canvas-wrapper {
  flex: 1;
  overflow: auto;
  padding: 0 20px 20px;
  background: #f5f7fa;
}
.description {
  color: #e2aa53;
  padding: 0 20px 10px;
  font-size: 12px;
}

.canvas {
  min-height: 90%;
  background: white;
  border-radius: 4px;
  padding: 20px 20px 100px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: relative;
}
.show-mini {
  max-width: 500px;
  margin: 0 auto;
}

.canvas-component {
  border-radius: 4px;
  transition: all 0.3s;
  position: relative;
  box-sizing: border-box;
}

.canvas-component:hover {
  background: #f5f7fa;
}

.canvas-component.selected {
  background: #ecf5ff;
  border: 2px solid #409eff;
}

.component-controls {
  position: absolute;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  z-index: 10;
}

.component-controls {
  display: flex;
  gap: 8px;
}

.move-button,
.delete-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px 8px;
  min-width: auto;
  height: 28px;
  font-size: 12px;
}

.move-button {
  background-color: #409eff;
  border-color: #409eff;
  color: white;
}

.move-button:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.move-button:disabled {
  background-color: #a0cfff;
  border-color: #a0cfff;
  cursor: not-allowed;
}

.delete-button {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

.delete-button:hover {
  background-color: #f78989;
  border-color: #f78989;
}

/* 拖拽时的样式 */
.canvas-component.dragging {
  opacity: 0.5;
  border: 2px dashed #409eff;
}

/* 拖拽悬停时的样式 */
.canvas-component.drag-over {
  border-top: 2px solid #409eff;
}

/* 底部放置区域样式 */
.canvas-bottom-drop-zone {
  height: 50px;
  margin-top: 20px;
  transition: all 0.3s;
}

.canvas-bottom-drop-zone.drag-over {
  background-color: #ecf5ff;
  border: 2px dashed #409eff;
  border-radius: 4px;
}

.empty-canvas {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #909399;
  font-size: 14px;
  border: 2px dashed #dcdfe6;
  border-radius: 4px;
}

.empty-canvas i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

/* 组件样式 */
:deep(.component-text) {
  border: 1px solid transparent;
}

:deep(.component-title) {
  margin: 10px 0;
  font-weight: 500;
}

:deep(.component-paragraph) {
  margin: 10px 0;
  line-height: 1.6;
}

:deep(.component-image) {
  padding: 10px 0;
}

:deep(.component-image img) {
  max-width: 100%;
  border-radius: 4px;
}

:deep(.component-button) {
  margin: 10px 0;
}

:deep(.component-card) {
  margin: 10px 0;
}

/* 新组件样式 */
:deep(.component-global-banner) {
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #f5f7fa;
  margin: 10px 0;
}

:deep(.component-global-banner .banner-title) {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
  color: #303133;
}

:deep(.component-pc-home-tournament) {
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #ecf5ff;
  margin: 10px 0;
}

:deep(.component-pc-home-tournament .tournament-title) {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 10px;
  color: #303133;
  padding-bottom: 10px;
  border-bottom: 1px solid #dcdfe6;
}

/* 未知组件样式 */
:deep(.component-unknown) {
  padding: 20px;
  border: 1px dashed #909399;
  border-radius: 4px;
  background: #fafafa;
  margin: 10px 0;
  color: #909399;
  text-align: center;
}

/* 自定义滚动条 */
.canvas-wrapper::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.canvas-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.canvas-wrapper::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.canvas-wrapper::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>