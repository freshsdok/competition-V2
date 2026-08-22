<template>
  <div class="app-container">
    <div class="content-editor-container">
      <!-- 顶部工具栏 -->
      <div class="editor-header">
        <div class="header-left">
          <h2 class="title">可视化页面编辑器</h2>
        </div>
        <div class="header-right">
          <template v-if="!pageStatus">
            <el-button @click="clearCanvas" v-if="!preview">清空画布</el-button>
            <el-button type="primary" @click="savePage" :loading="saving"  v-if="!preview"
              >保存页面</el-button
            >
          </template>
          <el-button @click="previewPage">预览</el-button>
        </div>
      </div>

      <!-- 主编辑区域 -->
      <div class="editor-main">
        <!-- 左侧组件面板 -->
        <div class="component-panel" v-if="!preview">
          <ComponentPanel
            @componentDrag="handleComponentDrag"
            @componentAdd="handleComponentAdd"
          />
        </div>

        <!-- 中间画布区域 -->
        <div class="canvas-area">
          <CanvasArea
            v-model="pageComponents"
            @componentSelect="handleComponentSelect"
            :preview="preview"
            :displayPlatform="displayPlatform"
          />
        </div>

        <!-- 右侧属性面板 -->
        <div class="property-panel" v-if="!preview">
          <PropertyPanel
            :selectedComponent="selectedComponent"
            :componentIndex="selectedComponentIndex"
            @update:component="handleComponentUpdate"
          />
          <div v-if="!selectedComponent" class="no-selection">
            <el-empty description="请选择一个组件进行编辑" />
          </div>
        </div>
      </div>
    </div>
    

  </div>
</template>

<script setup name="PageEditor">
import { ref, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import ComponentPanel from "./components/ComponentPanel.vue";
import CanvasArea from "./components/CanvasArea.vue";
import PropertyPanel from "./components/PropertyPanel.vue";
import { getPageContent, savePageContent } from "@/api/content/page";
import { useRoute } from "vue-router";
import modal from "@/plugins/modal";
import { useUrlRedirect } from "@/hooks/preview";
import './utils/backgroundStyles.css';
const props = defineProps({
  preview: {
    type: Boolean,
    default: false,
  },
  displayPlatform: {
    type: [String, Number],
    default: null,
  },
  pageId: {
    type: [String, Number],
    default: null,
  },
  pageStatus: {
    type: Boolean,
    default: false,
  },
  businessDetail: {
    type: Object,
    default: () => ({}),
  },
});

// 响应式数据
const canvasRef = ref(null);
const selectedComponent = ref(null);
const selectedComponentIndex = ref(-1);
const saving = ref(false);
const previewVisible = ref(false);
const pageId = computed(() =>props.preview?props.pageId:useRoute().query?.pageId);
const route = useRoute();
const pageStatus = computed(() =>props.preview?props.pageStatus: route.query?.pageStatus);
const displayPlatform = computed(() => props.preview?props.displayPlatform:route.query?.displayPlatform);
// 页面组件数据 - 这将是保存到数据库的JSON内容
const pageComponents = ref([]);

// 获取预览组件渲染器
const getPreviewComponentRenderer = (type) => {
  // 动态组件渲染进行预览
  return {
    render: (createElement) => {
      switch (type) {
        case "text":
          const textComp = pageComponents.value.find((c) => c.type === "text");
          return createElement(
            "div",
            {
              style: {
                fontSize: textComp?.fontSize,
                color: textComp?.color,
                textAlign: textComp?.textAlign,
              },
            },
            textComp?.content || "文本内容"
          );
        case "title":
          const titleComp = pageComponents.value.find(
            (c) => c.type === "title"
          );
          const level = titleComp?.level || 2;
          return createElement(
            `h${level}`,
            {
              style: {
                fontSize: titleComp?.fontSize,
                color: titleComp?.color,
                textAlign: titleComp?.textAlign,
                fontWeight: titleComp?.fontWeight,
              },
            },
            titleComp?.content || "标题"
          );
        case "paragraph":
          const paraComp = pageComponents.value.find(
            (c) => c.type === "paragraph"
          );
          return createElement(
            "p",
            {
              style: {
                fontSize: paraComp?.fontSize,
                lineHeight: paraComp?.lineHeight,
                color: paraComp?.color,
                textAlign: paraComp?.textAlign,
              },
            },
            paraComp?.content || "段落内容"
          );
        case "image":
          const imgComp = pageComponents.value.find((c) => c.type === "image");
          return createElement("div", {}, [
            createElement("img", {
              attrs: {
                src: imgComp?.src || "",
                alt: imgComp?.alt || "图片",
              },
              style: {
                width: imgComp?.width,
                height: imgComp?.height,
              },
            }),
          ]);
        case "button":
          const btnComp = pageComponents.value.find((c) => c.type === "button");
          return createElement(
            "el-button",
            {
              props: {
                type: btnComp?.type || "primary",
                size: btnComp?.size || "medium",
              },
            },
            btnComp?.text || "按钮"
          );
        case "card":
          const cardComp = pageComponents.value.find((c) => c.type === "card");
          return createElement(
            "el-card",
            {
              props: {
                shadow: cardComp?.shadow || "hover",
              },
            },
            [
              createElement("template", { slot: "header" }, [
                createElement("div", cardComp?.title || "卡片标题"),
              ]),
              createElement("div", cardComp?.content || "卡片内容"),
            ]
          );
        default:
          const defaultComp = pageComponents.value.find((c) => c.type === type);
          return createElement(
            "div",
            {
              style: {
                padding: "16px",
                border: "1px solid #e0e0e0",
                marginBottom: "20px",
              },
            },
            `${type} 组件内容`
          );
      }
    },
  };
};

// 处理组件拖拽
const handleComponentDrag = (componentType) => {
  // 不需要特殊处理，仅用于调试
};

// 处理组件添加
const handleComponentAdd = (component) => {
  // 创建新的数组副本，确保响应式更新
  const newComponents = [...pageComponents.value];
  newComponents.push(component);
  // 更新整个数组
  pageComponents.value = newComponents;
  // 选中新添加的组件
  selectedComponent.value = component;
  selectedComponentIndex.value = newComponents.length - 1;
};

// 处理组件选择
const handleComponentSelect = (component, index) => {
  // 确保组件存在且有效
  if (
    component &&
    index !== undefined &&
    index >= 0 &&
    index < pageComponents.value.length
  ) {
    selectedComponent.value = pageComponents.value[index];
    selectedComponentIndex.value = index;
  } else {
    // 清空选择状态
    selectedComponent.value = null;
    selectedComponentIndex.value = -1;
  }
};

// 处理组件更新
const handleComponentUpdate = (updatedComponent) => {
  if (selectedComponentIndex.value !== -1) {
    // 创建新的数组副本，确保响应式更新
    const newComponents = [...pageComponents.value];
    // 使用展开运算符合并更新的组件数据，保留不变的属性
    newComponents[selectedComponentIndex.value] = {
      ...newComponents[selectedComponentIndex.value],
      ...updatedComponent,
    };
    // 更新整个数组，避免直接修改数组元素导致的响应性问题
    pageComponents.value = newComponents;
    // 更新选中的组件引用
    selectedComponent.value = newComponents[selectedComponentIndex.value];
  }
};

// 公共函数：处理组件样式，确保所有样式属性都保留在style对象中
const processComponentStyles = (component) => {
  // 确保style对象存在
  if (!component.style) {
    component.style = {};
  }

  // 定义可能存在于根级需要移到style的通用样式属性
  const commonStyleProperties = [
    "padding",
    "margin",
    "borderRadius",
    "backgroundColor",
    "minHeight",
    "textAlign",
    "fontSize",
    "textDecoration",
    "color",
    "height",
    "width",
    "backgroundImage",
    "backgroundSize",
    "backgroundPosition",
    "backgroundRepeat",
  ];

  // 检查根级是否有需要移到style的属性，但要排除轮播图的高度属性
  commonStyleProperties.forEach((prop) => {
    // 对于轮播图组件，特殊处理，确保高度属性保留在根级别
    if (component.type === "global_banner" && prop === "height") {
      // 轮播图组件的高度属性不应移到style中，也不应该删除
    } else if (
      component[prop] !== undefined &&
      component[prop] !== "" &&
      component.style[prop] === undefined
    ) {
      // 只有当style对象中不存在该属性时，才从根级别移动
      component.style[prop] = component[prop];
      // 删除根级别的样式属性，避免重复
      delete component[prop];
    } else {
      // 删除根级别的样式属性，避免重复
      delete component[prop];
    }
  });

  return component;
};

// 保存页面 - 按照要求格式：{pageId: xxx, pageContent: json字符串}
const savePage = async () => {
  modal.confirm('已审核通过的页面,保存后会重新发起审核，是否确认保存？').then(async () => { 
    saving.value = true
    try {
      // 处理组件数据，确保只有指定的样式属性在style对象中
      const processedComponents = JSON.parse(
        JSON.stringify(pageComponents.value)
      ).map(processComponentStyles);
      const data = {
        pageId: pageId.value,
        displayPlatform: displayPlatform.value,
        pageContent: JSON.stringify(processedComponents), // 处理后的页面内容的JSON字符串
      };
      const response = await savePageContent(data);
      if (response.code === 200) {
        ElMessage.success("页面保存成功");
      } else {
        ElMessage.error("页面保存失败");
      }
    } catch (error) {
      console.error("保存页面失败:", error);
      ElMessage.error("保存失败，请重试");
    } finally {
      saving.value = false;
    }
  });
};

// 清空画布
const clearCanvas = () => {
  pageComponents.value = [];
  selectedComponent.value = null;
  selectedComponentIndex.value = -1;
};

// 预览页面
const {redirectToUrl} = useUrlRedirect()
let pageDetail = $ref(null)
const previewPage = () => {
  console.log('previewPage',pageDetail)
  redirectToUrl(`${pageDetail.url}?pageId=${pageDetail.pageId}&preview=${true}`,props.preview)
}
// 加载页面数据
const loading = ref(false);
const loadPageData = async () => {
  loading.value = true;
  try {
    if (pageId.value) {
      let response = ''
      // 使用async/await语法以保持代码一致性
      if(props.businessDetail && props.businessDetail.pageId) {
        response = {
          code: 200,
          data: props.businessDetail
        }
      }else{
        response = await getPageContent(pageId.value);
      }
      if (response.code === 200) {
        pageDetail = response.data
        let jsonData = response.data.pageContent
        if(jsonData) {
          try {
            const components = JSON.parse(jsonData) || [];
            console.log(components, "components");
            // 处理组件数据，确保所有组件使用统一的样式处理逻辑
            pageComponents.value = components.map(processComponentStyles);
          } catch (parseError) {
            console.error("解析页面内容失败:", parseError);
            ElMessage.error("页面内容格式错误，请联系管理员");
            pageComponents.value = [];
          }
        } else {
          pageComponents.value = [];
        }
      } else {
        ElMessage.error("获取页面详情失败");
      }
    } else {
      ElMessage.warning("未指定页面ID，使用空模板");
    }
  } catch (error) {
    console.error("加载页面数据失败:", error);
    ElMessage.error("加载页面数据失败");
  } finally {
    loading.value = false;
  }
};

// 组件挂载后加载数据
onMounted(() => {


    loadPageData();

});
</script>

<style scoped>
.content-editor-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.editor-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.title {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.header-right {
  display: flex;
  gap: 10px;
}

.editor-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  background: #f5f7fa;
}

.component-panel {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
}

.canvas-area {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.property-panel {
  width: 320px;
  background: #fff;
  border-left: 1px solid #e0e0e0;
  overflow-y: auto;
  padding: 20px;
}

.no-selection {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.preview-container {
  height: 100%;
  overflow: auto;
  padding: 40px;
  background: white;
}

.preview-component {
  margin-bottom: 30px;
}
</style>

