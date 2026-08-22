<template>
  <div class="process-viewer">
    <div class="process-canvas" ref="processCanvasRef"></div>

    <!-- 自定义箭头样式，用于成功状态下流程连线箭头 -->
    <defs ref="customSuccessDefsRef">
      <marker id="sequenceflow-end-white-success" viewBox="0 0 20 20" refX="11" refY="10" markerWidth="10" markerHeight="10" orient="auto">
        <path class="success-arrow" d="M 1 5 L 11 10 L 1 15 Z" style="stroke-width: 1px; stroke-linecap: round; stroke-dasharray: 10000, 1;"></path>
      </marker>
      <marker id="conditional-flow-marker-white-success" viewBox="0 0 20 20" refX="-1" refY="10" markerWidth="10" markerHeight="10" orient="auto">
        <path class="success-conditional" d="M 0 10 L 8 6 L 16 10 L 8 14 Z" style="stroke-width: 1px; stroke-linecap: round; stroke-dasharray: 10000, 1;"></path>
      </marker>
    </defs>

    <!-- 自定义箭头样式，用于失败状态下流程连线箭头 -->
    <defs ref="customFailDefsRef">
      <marker id="sequenceflow-end-white-fail" viewBox="0 0 20 20" refX="11" refY="10" markerWidth="10" markerHeight="10" orient="auto">
        <path class="fail-arrow" d="M 1 5 L 11 10 L 1 15 Z" style="stroke-width: 1px; stroke-linecap: round; stroke-dasharray: 10000, 1;"></path>
      </marker>
      <marker id="conditional-flow-marker-white-fail" viewBox="0 0 20 20" refX="-1" refY="10" markerWidth="10" markerHeight="10" orient="auto">
        <path class="fail-conditional" d="M 0 10 L 8 6 L 16 10 L 8 14 Z" style="stroke-width: 1px; stroke-linecap: round; stroke-dasharray: 10000, 1;"></path>
      </marker>
    </defs>

    <!-- 已完成节点悬浮弹窗 -->
    <el-dialog modal-class="comment-dialog" width="1100px" :title="dlgTitle || '审批记录'" v-model="dialogVisible">
      <el-row>
        <el-table :data="taskCommentList" size="default" border header-cell-class-name="table-header-gray">
          <el-table-column label="序号" header-align="center" align="center" type="index" width="55px" fixed="left" />
          <el-table-column label="实际办理" prop="assigneeName" min-width="150" align="center" show-overflow-tooltip/>
          <el-table-column label="候选办理" prop="candidate" min-width="150" align="center" show-overflow-tooltip/>
          <el-table-column label="处理时间" prop="createTime" width="160" align="center"/>
          <el-table-column label="办结时间" prop="endTime" width="160" align="center" />
          <el-table-column label="耗时" prop="duration" width="180" align="center"/>
          <el-table-column label="审批意见" align="center" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.commentList && row.commentList[0] ? row.commentList[0].fullMessage : '' }}
            </template>
          </el-table-column>
        </el-table>
      </el-row>
    </el-dialog>

    <div class="right-btns">
      <el-row type="flex" justify="end">
        <el-button-group key="scale-control">
          <el-button type="default" :plain="true" :disabled="defaultZoom <= 0.3" icon="Minus" @click="processZoomOut()" />
          <el-button type="default" style="width: 90px;">{{ Math.floor(defaultZoom * 10 * 10) + "%" }}</el-button>
          <el-button type="default" :plain="true" :disabled="defaultZoom >= 3.9" icon="Plus" @click="processZoomIn()" />
          <el-button type="default" icon="ScaleToOriginal" @click="processReZoom()" />
          <slot></slot>
        </el-button-group>
      </el-row>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, watch, nextTick, onUnmounted, onMounted } from 'vue'
import '@/plugins/package/theme/index.scss';
import BpmnViewer from 'bpmn-js/lib/Viewer';
import MoveCanvasModule from 'diagram-js/lib/navigation/movecanvas';

const props = defineProps({
  xml: {
    type: String
  },
  finishedInfo: {
    type: Object,
    default: () => ({})
  },
  // 所有节点审批记录
  allCommentList: {
    type: Array,
    default: () => []
  }
})

const processCanvasRef = ref(null)
const customSuccessDefsRef = ref(null)
const customFailDefsRef = ref(null)

const dialogVisible = ref(false)
const dlgTitle = ref(undefined)
const defaultZoom = ref(1)
// 是否正在加载流程图
const isLoading = ref(false)
const bpmnViewer = ref(undefined)
// 已完成流程元素
const processNodeInfo = reactive({})
// 当前任务id
const selectTaskId = ref(undefined)
// 任务节点审批记录
const taskCommentList = ref([])
// 已完成任务悬浮延迟Timer
const hoverTimer = ref(null)

/** 初始化流程图 */
function init () {
  bpmnViewer.value = new BpmnViewer({
    container: processCanvasRef.value,
    additionalModules: [
      // 移动整个画布
      MoveCanvasModule
    ],
  });
  if (props.xml) {
    createNewDiagram();
  } else {
    clearViewer();
  }
}

// 显示流程图
async function createNewDiagram() {
  try {
    // 任务节点悬浮事件
    bpmnViewer.value.on('element.click', ({ element }) => {
      onSelectElement(element);
    });
    await bpmnViewer.value.importXML(props.xml);
    addCustomDefs();
    processReZoom();
  } catch (e) {
    clearViewer();
  } finally {
    setProcessStatus(processNodeInfo);
  }
}

function processReZoom() {
  let canvas = bpmnViewer.value.get('canvas');
  canvas.zoom('fit-viewport', 'auto');
  // 获取当前的缩放级别
  defaultZoom.value = canvas.viewbox().scale;
}

function processZoomIn(zoomStep = 0.1) {
  let newZoom = Math.floor(defaultZoom.value * 100 + zoomStep * 100) / 100;
  if (newZoom > 4) {
    throw new Error('[Process Designer Warn ]: The zoom ratio cannot be greater than 4');
  }
  defaultZoom.value = newZoom;
  bpmnViewer.value.get('canvas').zoom(defaultZoom.value);
}

function processZoomOut(zoomStep = 0.1) {
  let newZoom = Math.floor(defaultZoom.value * 100 - zoomStep * 100) / 100;
  if (newZoom < 0.2) {
    throw new Error('[Process Designer Warn ]: The zoom ratio cannot be less than 0.2');
  }
  defaultZoom.value = newZoom;
  bpmnViewer.value.get('canvas').zoom(defaultZoom.value);
}

function getOperationTagType(type) {
  return 'success';
  // switch (type) {
  //   case this.SysFlowTaskOperationType.AGREE:
  //   case this.SysFlowTaskOperationType.MULTI_AGREE:
  //     return 'success';
  //   case this.SysFlowTaskOperationType.REFUSE:
  //   case this.SysFlowTaskOperationType.PARALLEL_REFUSE:
  //   case this.SysFlowTaskOperationType.MULTI_REFUSE:
  //     return 'warning';
  //   case this.SysFlowTaskOperationType.STOP:
  //     return 'danger'
  //   default:
  //     return 'primary';
  // }
}

// 流程图预览清空
function clearViewer() {
  processCanvasRef.value = null;
}

// 添加自定义箭头
function addCustomDefs() {
  const canvas = bpmnViewer.value.get('canvas');
  const svg = canvas._svg;
  const customSuccessDefs = customSuccessDefsRef.value;
  const customFailDefs = customFailDefsRef.value;
  svg.appendChild(customSuccessDefs);
  svg.appendChild(customFailDefs);
}

// 任务悬浮弹窗
function onSelectElement(element) {
  selectTaskId.value = undefined;
  dlgTitle.value = undefined;

  if (processNodeInfo == null || processNodeInfo.finishedTaskSet == null) return;

  if (element == null || processNodeInfo.finishedTaskSet.indexOf(element.id) === -1) {
    return;
  }

  selectTaskId.value = element.id;
  dlgTitle.value = element.businessObject ? element.businessObject.name : undefined;
  // 计算当前悬浮任务审批记录，如果记录为空不显示弹窗
  taskCommentList.value = (props.allCommentList || []).filter(item => {
    return item.activityId === selectTaskId.value;
  });
  dialogVisible.value = true;
}

// 设置流程图元素状态
function setProcessStatus (_processNodeInfo) {
  Object.assign(processNodeInfo, _processNodeInfo);
  if (isLoading.value || processNodeInfo == null || bpmnViewer.value == null) return;
  let { finishedTaskSet, rejectedTaskSet, unfinishedTaskSet, finishedSequenceFlowSet } = processNodeInfo;
  const canvas = bpmnViewer.value.get('canvas');
  const elementRegistry = bpmnViewer.value.get('elementRegistry');
  if (Array.isArray(finishedSequenceFlowSet)) {
    finishedSequenceFlowSet.forEach(item => {
      if (item != null) {
        canvas.addMarker(item, 'success');
        let element = elementRegistry.get(item);
        const conditionExpression = element.businessObject.conditionExpression;
        if (conditionExpression) {
          canvas.addMarker(item, 'condition-expression');
        }
      }
    });
  }
  if (Array.isArray(finishedTaskSet)) {
    finishedTaskSet.forEach(item => canvas.addMarker(item, 'success'));
  }
  if (Array.isArray(unfinishedTaskSet)) {
    unfinishedTaskSet.forEach(item => canvas.addMarker(item, 'primary'));
  }
  if (Array.isArray(rejectedTaskSet)) {
    rejectedTaskSet.forEach(item => {
      if (item != null) {
        let element = elementRegistry.get(item);
        if (element.type.includes('Task')) {
          canvas.addMarker(item, 'danger');
        } else {
          canvas.addMarker(item, 'warning');
        }
      }
    })
  }
}

watch(() => props.finishedInfo, (newValue) => {
  setProcessStatus(newValue);
}, { immediate: true })

onMounted(() => {
  init();
})
</script>

<style lang="scss" scoped>
.process-viewer {
  height: 400px;
  .process-canvas {
    height: 100%;
  }
  .right-btns {
    position: absolute; 
    top: 0px; 
    left: 0px; 
    width: 100%;
  }
}
</style>
