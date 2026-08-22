<template>
  <div class="my-process-designer">
    <div class="my-process-designer__header">
      <slot name="control-header"></slot>
      <template v-if="!$slots['control-header']">
        <el-button-group key="file-control">
          <el-button :size="headerButtonSize" :type="headerButtonType" icon="el-icon-edit" @click="onSave">保存流程</el-button>
          <el-button :size="headerButtonSize" :type="headerButtonType" icon="el-icon-folder-opened" @click="fileRef.click()">打开文件</el-button>
          <el-tooltip effect="light">
            <template #content>
              <el-button :size="headerButtonSize" type="text" @click="downloadProcessAsXml()">下载为XML文件</el-button>
              <br />
              <el-button :size="headerButtonSize" type="text" @click="downloadProcessAsSvg()">下载为SVG文件</el-button>
              <br />
              <el-button :size="headerButtonSize" type="text" @click="downloadProcessAsBpmn()">下载为BPMN文件</el-button>
            </template>
            <el-button :size="headerButtonSize" :type="headerButtonType" icon="el-icon-download">下载文件</el-button>
          </el-tooltip>
          <el-tooltip effect="light">
            <template #content>
              <el-button :size="headerButtonSize" type="text" @click="previewProcessXML">预览XML</el-button>
              <br />
              <el-button :size="headerButtonSize" type="text" @click="previewProcessJson">预览JSON</el-button>
            </template>
            <el-button :size="headerButtonSize" :type="headerButtonType" icon="el-icon-view">预览</el-button>
          </el-tooltip>
          <el-tooltip v-if="simulation" effect="light" :content="simulationStatus ? '退出模拟' : '开启模拟'">
            <el-button :size="headerButtonSize" :type="headerButtonType" icon="el-icon-cpu" @click="processSimulation">
              模拟
            </el-button>
          </el-tooltip>
        </el-button-group>
        <el-button-group key="align-control">
          <el-tooltip effect="light" content="向左对齐">
            <el-button :size="headerButtonSize" class="align align-left" @click="elementsAlign('left')" >
              <svg t="1730163469070" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="13053" width="14" height="14"><path d="M907.6 264.8c27.2 0 49.4-22.2 49.4-49.4S934.8 166 907.6 166H116.4c-27.2 0-49.4 22.2-49.4 49.4s22.2 49.4 49.4 49.4h791.2zM512 462.6c27.2 0 49.4-22.2 49.4-49.4s-22.2-49.4-49.4-49.4H116.4c-27.2 0-49.4 22.2-49.4 49.4s22.2 49.4 49.4 49.4H512z m445 148.2c0 27.2-22.2 49.4-49.4 49.4H116.4c-27.2 0-49.4-22.2-49.4-49.4s22.2-49.4 49.4-49.4h791.2c27.2 0 49.4 22.2 49.4 49.4zM512 858.2c27.2 0 49.4-22.2 49.4-49.4s-22.2-49.4-49.4-49.4H116.4c-27.2 0-49.4 22.2-49.4 49.4s22.2 49.4 49.4 49.4H512z" fill="#333333" p-id="13054"></path></svg>
            </el-button>
          </el-tooltip>
          <el-tooltip effect="light" content="向右对齐">
            <el-button :size="headerButtonSize" class="align align-right" @click="elementsAlign('right')">
              <svg t="1730163574852" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="15752" width="14" height="14"><path d="M870.4 768a51.2 51.2 0 1 1 0 102.4H153.6a51.2 51.2 0 1 1 0-102.4h716.8z m0-409.6a51.2 51.2 0 1 1 0 102.4H153.6a51.2 51.2 0 1 1 0-102.4h716.8z m0 204.8a51.2 51.2 0 1 1 0 102.4H460.8a51.2 51.2 0 1 1 0-102.4h409.6z m0-409.6a51.2 51.2 0 1 1 0 102.4H460.8a51.2 51.2 0 1 1 0-102.4h409.6z" fill="#2c2c2c" p-id="15753"></path></svg>
            </el-button>
          </el-tooltip>
          <el-tooltip effect="light" content="向上对齐">
            <el-button :size="headerButtonSize" class="align align-top" @click="elementsAlign('top')">
              <svg t="1730163617139" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="17638" width="14" height="14"><path d="M0 64.003161v896.002709c0 35.338559 28.653764 63.992324 64.001355 63.992324 35.349396 0 64.001355-28.651958 64.001354-63.992324V64.003161C128.000903 28.651958 99.350751 0 64.001355 0 28.653764 0 0 28.651958 0 64.003161zM895.995484 63.990517v640.006322c0 35.338559 28.650152 63.992324 64.001355 63.992324 35.336753 0 64.001355-28.651958 64.001355-63.992324V63.990517C1023.996388 28.651958 995.333592 0 959.995033 0c-35.349396 0-63.999548 28.651958-63.999549 63.990517zM296.955882 63.990517v640.006322c0 35.338559 28.653764 63.992324 64.004967 63.992324 35.345784 0 64.001355-28.651958 64.001355-63.992324V63.990517C424.960397 28.651958 396.306633 0 360.960849 0c-35.351203 0-64.004967 28.651958-64.004967 63.990517zM599.034184 64.003161v896.002709c0 35.338559 28.653764 63.992324 64.004967 63.992324 35.336753 0 64.001355-28.651958 64.001355-63.992324V64.003161C727.0387 28.651958 698.375904 0 663.039151 0c-35.349396 0-64.004967 28.651958-64.004967 64.003161z" fill="#2c2c2c" p-id="17639"></path></svg>
            </el-button>
          </el-tooltip>
          <el-tooltip effect="light" content="向下对齐">
            <el-button :size="headerButtonSize" class="align align-bottom" @click="elementsAlign('bottom')">
              <svg t="1730163678983" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="22021" width="14" height="14"><path d="M9.9 962.4V63.9c0-37.1 18.6-55.7 55.7-55.7s55.7 18.6 55.7 55.7v898.4c0 37.1-18.6 55.7-55.7 55.7-37.2 0.1-55.7-18.5-55.7-55.6z m0 0M307 962.4v-527c0-37.1 18.6-55.7 55.7-55.7s55.7 18.6 55.7 55.7v527c0 37.1-18.6 55.7-55.7 55.7S307 999.5 307 962.4z m0 0M901.3 962.4v-527c0-37.1 18.6-55.7 55.7-55.7s55.7 18.6 55.7 55.7v527c0 37.1-18.6 55.7-55.7 55.7-37.2 0-55.7-18.6-55.7-55.7z m0 0M604.1 962.4V63.9c0-37.1 18.6-55.7 55.7-55.7s55.7 18.6 55.7 55.7v898.4c0 37.1-18.6 55.7-55.7 55.7s-55.7-18.5-55.7-55.6z m0 0" fill="#231815" p-id="22022"></path></svg>
            </el-button>
          </el-tooltip>
          <el-tooltip effect="light" content="水平居中">
            <el-button :size="headerButtonSize" class="align align-center" @click="elementsAlign('center')">
              <svg t="1730163404662" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="12026" width="14" height="14"><path d="M809.69697 797.090909V229.365657c0-22.238384 17.971717-40.727273 40.727272-40.727273h40.727273c22.238384 0 40.727273 17.971717 40.727273 40.727273v567.20808c0 22.238384-17.971717 40.727273-40.727273 40.727273h-40.727273c-22.755556 0-40.727273-17.971717-40.727272-40.210101zM566.626263 898.456566v-770.585859c0-22.238384 17.971717-40.727273 40.727272-40.727273h40.727273c22.238384 0 40.727273 17.971717 40.727273 40.727273v770.068687c0 22.238384-17.971717 40.727273-40.727273 40.727273h-40.727273c-22.755556 0-40.727273-17.971717-40.727272-40.210101zM323.555556 776.40404V249.535354c0-22.238384 17.971717-40.727273 40.727272-40.727273h40.727273c22.238384 0 40.727273 17.971717 40.727273 40.727273v526.997979c0 22.238384-17.971717 40.727273-40.727273 40.727273h-40.727273c-22.755556-0.646465-40.727273-18.618182-40.727272-40.856566zM201.761616 87.660606V938.666667c0 22.238384-17.971717 40.727273-40.727273 40.727272H120.177778c-22.238384 0-40.727273-17.971717-40.727273-40.727272V87.143434c0-22.238384 17.971717-40.727273 40.727273-40.727272h40.727273c22.755556 0.517172 40.856566 18.488889 40.856565 41.244444z" fill="#2c2c2c" p-id="12027"></path></svg>
            </el-button>
          </el-tooltip>
          <el-tooltip effect="light" content="垂直居中">
            <el-button :size="headerButtonSize" class="align align-middle" @click="elementsAlign('middle')">
              <svg t="1730163352506" class="icon" viewBox="0 0 1025 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10197" width="14" height="14"><path d="M830.221166 73.142857a50.761143 50.761143 0 0 1 6.875428 101.083429l-6.875428 0.438857H187.00288a50.761143 50.761143 0 0 1-6.875429-101.083429L187.00288 73.142857h643.218286z m143.872 253.878857a50.761143 50.761143 0 0 1 6.875428 101.083429l-6.875428 0.512H60.100023a50.761143 50.761143 0 0 1-6.875429-101.156572l6.875429-0.438857h913.993143z m-203.117715 253.878857a50.761143 50.761143 0 0 1 6.875429 101.083429l-6.875429 0.512H187.00288a50.761143 50.761143 0 0 1-6.875429-101.156571l6.875429-0.438858h583.972571z m194.633143 253.878858a50.761143 50.761143 0 0 1 6.875429 101.083428l-6.875429 0.512H51.615451A50.761143 50.761143 0 0 1 44.740023 835.291429l6.875428-0.512H965.608594z" p-id="10198"></path></svg>
            </el-button>
          </el-tooltip>
        </el-button-group>
        <el-button-group key="scale-control">
          <el-tooltip effect="light" content="缩小视图">
            <el-button :size="headerButtonSize" :disabled="defaultZoom <= 0.3" icon="el-icon-zoom-out" @click="processZoomOut()" />
          </el-tooltip>
          <el-button :size="headerButtonSize">{{ Math.floor(defaultZoom * 10 * 10) + "%" }}</el-button>
          <el-tooltip effect="light" content="放大视图">
            <el-button :size="headerButtonSize" :disabled="defaultZoom >= 3.9" icon="el-icon-zoom-in" @click="processZoomIn()" />
          </el-tooltip>
          <el-tooltip effect="light" content="重置视图并居中">
            <el-button :size="headerButtonSize" @click="processReZoom()">
              <svg t="1730164054923" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29747" width="14" height="14"><path d="M812.992 180.992a58.24 58.24 0 0 1 43.008 17.024 58.24 58.24 0 0 1 16.96 42.944v482.048c0 16.64-5.76 30.848-17.472 42.496a57.856 57.856 0 0 1-42.496 17.536H210.944a57.856 57.856 0 0 1-42.496-17.536 57.856 57.856 0 0 1-17.472-42.496V240.96a58.24 58.24 0 0 1 16.96-42.944 58.24 58.24 0 0 1 43.008-17.024h602.048z m0-60.992H210.944c-16 0.64-31.36 4.032-46.016 9.984-14.656 6.08-27.648 14.72-38.976 26.048-11.328 11.328-20.16 24.32-26.496 39.04-6.336 14.656-9.536 30.016-9.536 46.016v481.984c0 16 3.2 31.36 9.536 46.016 6.336 14.72 15.168 27.712 26.496 39.04a119.808 119.808 0 0 0 85.056 35.008h601.984c16 0 31.36-3.008 46.016-9.024 14.72-6.016 27.648-14.72 39.04-25.984 11.264-11.392 20.096-24.384 26.432-39.04 6.4-14.656 9.536-30.016 9.536-46.016V241.088c0-16-3.2-31.36-9.536-46.08a124.416 124.416 0 0 0-26.496-38.976 117.952 117.952 0 0 0-38.976-26.048 135.104 135.104 0 0 0-46.016-9.984z m-120 180.992a28.352 28.352 0 0 0-21.504 8.96 29.568 29.568 0 0 0-8.512 21.056v300.928a29.12 29.12 0 0 0 30.016 30.08 29.12 29.12 0 0 0 30.016-30.08V330.944a29.12 29.12 0 0 0-30.016-30.016v0.064z m-361.984 0a28.8 28.8 0 0 0-20.992 8.96 28.8 28.8 0 0 0-8.96 21.056v300.928a29.12 29.12 0 0 0 29.952 30.08 29.12 29.12 0 0 0 30.08-30.08V330.944a29.568 29.568 0 0 0-8.576-20.992 28.352 28.352 0 0 0-21.504-8.96zM512 361.024a32.896 32.896 0 0 0-21.504 9.472 29.12 29.12 0 0 0-8.512 21.504v30.016A29.12 29.12 0 0 0 512 451.072a29.12 29.12 0 0 0 30.016-29.056v-30.016a28.352 28.352 0 0 0-8.96-21.504A33.792 33.792 0 0 0 512 361.024zM512 512a29.12 29.12 0 0 0-30.016 30.016v30.016A29.12 29.12 0 0 0 512 602.048a29.12 29.12 0 0 0 30.016-30.016v-30.016a28.8 28.8 0 0 0-8.96-20.992 28.8 28.8 0 0 0-21.056-8.96V512z" p-id="29748"></path></svg>
            </el-button>
          </el-tooltip>
        </el-button-group>
        <el-button-group key="stack-control">
          <el-tooltip effect="light" content="撤销">
            <el-button :size="headerButtonSize" :disabled="!revocable" icon="el-icon-refresh-left" @click="processUndo()" />
          </el-tooltip>
          <el-tooltip effect="light" content="恢复">
            <el-button :size="headerButtonSize" :disabled="!recoverable" icon="el-icon-refresh-right" @click="processRedo()" />
          </el-tooltip>
          <el-tooltip effect="light" content="重新绘制">
            <el-button :size="headerButtonSize" icon="el-icon-refresh" @click="processRestart" />
          </el-tooltip>
        </el-button-group>
      </template>
      <!-- 用于打开本地文件-->
      <input type="file" id="files" ref="fileRef" style="display: none" accept=".xml, .bpmn" @change="importLocalFile" />
    </div>
    <div class="my-process-designer__container">
      <div class="my-process-designer__canvas" ref="bpmnCanvasRef"></div>
    </div>
    <el-dialog title="预览" width="60%" v-model="previewModelVisible" append-to-body destroy-on-close>
      <highlightjs :language="previewType" :autodetect="false" :code="previewResult" style="height: 60vh" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from "vue"
import { ElMessage, ElMessageBox } from "element-plus";

// 生产环境时优化
// const BpmnModeler = window.BpmnJS;
import BpmnModeler from "bpmn-js/lib/Modeler";
import DefaultEmptyXML from "./plugins/defaultEmpty";
// 翻译方法
import customTranslate from "./plugins/translate/customTranslate";
import translationsCN from "./plugins/translate/zh";
// 模拟流转流程
import tokenSimulation from "bpmn-js-token-simulation";
// 标签解析构建器
// import bpmnPropertiesProvider from "bpmn-js-properties-panel/lib/provider/bpmn";
// 标签解析 Moddle
import camundaModdleDescriptor from "./plugins/descriptor/camundaDescriptor.json";
import activitiModdleDescriptor from "./plugins/descriptor/activitiDescriptor.json";
import flowableModdleDescriptor from "./plugins/descriptor/flowableDescriptor.json";
// 标签解析 Extension
import camundaModdleExtension from "./plugins/extension-moddle/camunda";
import activitiModdleExtension from "./plugins/extension-moddle/activiti";
import flowableModdleExtension from "./plugins/extension-moddle/flowable";
// 引入json转换与高亮
import convert from "xml-js";

import highlightjs from "@/plugins/package/highlight/index.js"

const props = defineProps({
  modelValue: String, // xml 字符串
  processId: String,
  processName: String,
  translations: Object, // 自定义的翻译文件
  additionalModel: [Object, Array], // 自定义model
  moddleExtension: Object, // 自定义moddle
  onlyCustomizeAddi: {
      type: Boolean,
    default: false
  },
  onlyCustomizeModdle: {
    type: Boolean,
    default: false
  },
  simulation: {
    type: Boolean,
    default: true
  },
  keyboard: {
    type: Boolean,
    default: true
  },
  prefix: {
    type: String,
    default: "flowable"
  },
  events: {
    type: Array,
    default: () => ["element.click"]
  },
  headerButtonSize: {
    type: String,
    default: "small",
    validator: value => ["default", "medium", "small"].indexOf(value) !== -1
  },
  headerButtonType: {
    type: String,
    default: "primary",
    validator: value => ["default", "primary", "success", "warning", "danger", "info"].indexOf(value) !== -1
  } 
})
const emits = defineEmits(["save", "init-finished", "event", "commandStack-changed", "input", "change", "canvas-viewbox-changed", "destroy"])
const defaultZoom = ref(1)
const previewModelVisible = ref(false)
const simulationStatus = ref(false)
const previewResult = ref("")
const previewType = ref("xml")
const recoverable = ref(false)
const revocable = ref(false)
const bpmnModeler = ref(null)
const bpmnCanvasRef = ref(null)
const fileRef = ref(null)

const additionalModules = computed(() => {
  const Modules = [];
  // 仅保留用户自定义扩展模块
  if (props.onlyCustomizeAddi) {
    if (Object.prototype.toString.call(props.additionalModel) === "[object Array]") {
      return props.additionalModel || [];
    }
    return [props.additionalModel];
  }

  // 插入用户自定义扩展模块
  if (Object.prototype.toString.call(props.additionalModel) === "[object Array]") {
    Modules.push(...props.additionalModel);
  } else {
    props.additionalModel && Modules.push(props.additionalModel);
  }

  // 翻译模块
  const TranslateModule = {
    translate: ["value", customTranslate(props.translations || translationsCN)]
  };
  Modules.push(TranslateModule);

  // 模拟流转模块
  if (props.simulation) {
    Modules.push(tokenSimulation);
  }

  // 根据需要的流程类型设置扩展元素构建模块
  // if (props.prefix === "bpmn") {
  //   Modules.push(bpmnModdleExtension);
  // }
  if (props.prefix === "camunda") {
    Modules.push(camundaModdleExtension);
  }
  if (props.prefix === "flowable") {
    Modules.push(flowableModdleExtension);
  }
  if (props.prefix === "activiti") {
    Modules.push(activitiModdleExtension);
  }

  return Modules;
})

const moddleExtensions = computed(() => {
  const Extensions = {};
  // 仅使用用户自定义模块
  if (props.onlyCustomizeModdle) {
    return props.moddleExtension || null;
  }

  // 插入用户自定义模块
  if (props.moddleExtension) {
    for (let key in props.moddleExtension) {
      Extensions[key] = props.moddleExtension[key];
    }
  }

  // 根据需要的 "流程类型" 设置 对应的解析文件
  if (props.prefix === "activiti") {
    Extensions.activiti = activitiModdleDescriptor;
  }
  if (props.prefix === "flowable") {
    Extensions.flowable = flowableModdleDescriptor;
  }
  if (props.prefix === "camunda") {
    Extensions.camunda = camundaModdleDescriptor;
  }

  return Extensions;
})

function onSave () {
  return new Promise((resolve, reject) => {
    console.log(bpmnModeler,6666)
    if (bpmnModeler.value == null) {
      reject();
    }
    bpmnModeler.value.saveXML({ format: true }).then(({ xml }) => {
      emits('save', xml);
      resolve(xml);
    });
  })
}

function initBpmnModeler() {
  if (bpmnModeler.value) return;
  bpmnModeler.value = new BpmnModeler({
    container: bpmnCanvasRef.value,
    keyboard: props.keyboard ? { bindTo: document } : null,
    additionalModules: additionalModules.value,
    moddleExtensions: moddleExtensions.value
  });
  emits("init-finished", bpmnModeler.value);
  initModelListeners();
}

function initModelListeners() {
  const EventBus = bpmnModeler.value.get("eventBus");
  // 注册需要的监听事件, 将. 替换为 - , 避免解析异常
  props.events.forEach(event => {
    EventBus.on(event, function(eventObj) {
      let eventName = event.replace(/\./g, "-");
      let element = eventObj ? eventObj.element : null;
      emits(eventName, element, eventObj);
      emits('event', eventName, element, eventObj);
    });
  });
  // 监听图形改变返回xml
  EventBus.on("commandStack.changed", async event => {
    try {
      recoverable.value = bpmnModeler.value.get("commandStack").canRedo();
      revocable.value = bpmnModeler.value.get("commandStack").canUndo();
      let { xml } = await bpmnModeler.value.saveXML({ format: true });
      emits("commandStack-changed", event);
      emits("input", xml);
      emits("change", xml);
    } catch (e) {
      console.error(`[Process Designer Warn]: ${e.message || e}`);
    }
  });
  // 监听视图缩放变化
  bpmnModeler.value.on("canvas.viewbox.changed", ({ viewbox }) => {
    emits("canvas-viewbox-changed", { viewbox });
    const { scale } = viewbox;
    defaultZoom.value = Math.floor(scale * 100) / 100;
  });
}

/* 创建新的流程图 */
async function createNewDiagram(xml) {
  // 将字符串转换成图显示出来
  let newId = props.processId || `Process_${new Date().getTime()}`;
  let newName = props.processName || `业务流程_${new Date().getTime()}`;
  let xmlString = xml || DefaultEmptyXML(newId, newName, props.prefix);
  try {
    let { warnings } = await bpmnModeler.value.importXML(xmlString);
    if (warnings && warnings.length) {
      warnings.forEach(warn => console.warn(warn));
    }
  } catch (e) {
    console.error(`[Process Designer Warn]: ${e.message || e}`);
  }
}


// 下载流程图到本地
async function downloadProcess(type, name) {
  try {
    // 按需要类型创建文件并下载
    if (type === "xml" || type === "bpmn") {
      const { err, xml } = await bpmnModeler.value.saveXML();
      // 读取异常时抛出异常
      if (err) {
        console.error(`[Process Designer Warn ]: ${err.message || err}`);
      }
      let { href, filename } = setEncoded(type.toUpperCase(), name, xml);
      downloadFunc(href, filename);
    } else {
      const { err, svg } = await bpmnModeler.value.saveSVG();
      // 读取异常时抛出异常
      if (err) {
        return console.error(err);
      }
      let { href, filename } = setEncoded("SVG", name, svg);
      downloadFunc(href, filename);
    }
  } catch (e) {
    console.error(`[Process Designer Warn ]: ${e.message || e}`);
  }
  // 文件下载方法
  function downloadFunc(href, filename) {
    if (href && filename) {
      let a = document.createElement("a");
      a.download = filename; //指定下载的文件名
      a.href = href; //  URL对象
      a.click(); // 模拟点击
      URL.revokeObjectURL(a.href); // 释放URL 对象
    }
  }
}

// 根据所需类型进行转码并返回下载地址
function setEncoded(type, filename = "diagram", data) {
  const encodedData = encodeURIComponent(data);
  return {
    filename: `${filename}.${type}`,
    href: `data:application/${type === "svg" ? "text/xml" : "bpmn20-xml"};charset=UTF-8,${encodedData}`,
    data: data
  };
}

// 加载本地文件
function importLocalFile() {
  const file = fileRef.value.files[0];
  const reader = new FileReader();
  reader.readAsText(file);
  reader.onload = function() {
    let xmlStr = reader.result;
    createNewDiagram(xmlStr);
  };
}
/* ------------------------------------------------ refs methods ------------------------------------------------------ */
function downloadProcessAsXml() {
  downloadProcess("xml");
}

function downloadProcessAsBpmn() {
  downloadProcess("bpmn");
}

function downloadProcessAsSvg() {
  downloadProcess("svg");
}

function processSimulation() {
  simulationStatus.value = !simulationStatus.value;
  props.simulation && bpmnModeler.value.get("toggleMode").toggleMode();
}

function processRedo() {
  bpmnModeler.value.get("commandStack").redo();
}

function processUndo() {
  bpmnModeler.value.get("commandStack").undo();
}

function processZoomIn(zoomStep = 0.1) {
  let newZoom = Math.floor(defaultZoom.value * 100 + zoomStep * 100) / 100;
  if (newZoom > 4) {
    throw new Error("[Process Designer Warn ]: The zoom ratio cannot be greater than 4");
  }
  defaultZoom.value = newZoom;
  bpmnModeler.value.get("canvas").zoom(defaultZoom.value);
}

function processZoomOut(zoomStep = 0.1) {
  let newZoom = Math.floor(defaultZoom.value * 100 - zoomStep * 100) / 100;
  if (newZoom < 0.2) {
    throw new Error("[Process Designer Warn ]: The zoom ratio cannot be less than 0.2");
  }
  defaultZoom.value = newZoom;
  bpmnModeler.value.get("canvas").zoom(defaultZoom.value);
}

function processZoomTo(newZoom = 1) {
  if (newZoom < 0.2) {
    throw new Error("[Process Designer Warn ]: The zoom ratio cannot be less than 0.2");
  }
  if (newZoom > 4) {
    throw new Error("[Process Designer Warn ]: The zoom ratio cannot be greater than 4");
  }
  defaultZoom.value = newZoom;
  bpmnModeler.value.get("canvas").zoom(newZoom);
}

function processReZoom() {
  defaultZoom.value = 1;
  bpmnModeler.value.get("canvas").zoom("fit-viewport", "auto");
}

function processRestart() {
  recoverable.value = false;
  revocable.value = false;
  createNewDiagram(null).then(() => bpmnModeler.value.get("canvas").zoom(1, "auto"));
}

function elementsAlign(align) {
  const Align = bpmnModeler.value.get("alignElements");
  const Selection = bpmnModeler.value.get("selection");
  const SelectedElements = Selection.get();
  if (!SelectedElements || SelectedElements.length <= 1) {
    ElMessage.warning("请按住 Ctrl 键选择多个元素对齐");
    return;
  }
  
  ElMessageBox.confirm("自动对齐可能造成图形变形，是否继续？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => Align.trigger(SelectedElements, align));
}

/*-----------------------------    方法结束     ---------------------------------*/
function previewProcessXML() {
  bpmnModeler.value.saveXML({ format: true }).then(({ xml }) => {
    previewResult.value = xml;
    previewType.value = "xml";
    previewModelVisible.value = true;
  });
}

function previewProcessJson() {
  bpmnModeler.value.saveXML({ format: true }).then(({ xml }) => {
    previewResult.value = convert.xml2json(xml, { spaces: 2 });
    previewType.value = "json";
    previewModelVisible.value = true;
  });
}


onMounted(() => {
  initBpmnModeler();
  createNewDiagram(props.modelValue);
})

onBeforeUnmount(() => {
  if (bpmnModeler.value) bpmnModeler.value.destroy();
  emits("destroy", bpmnModeler.value);
  bpmnModeler.value = null;
})
</script>
