<template>
  <div class="component-panel-container">
    <div class="panel-header">
      <h3>组件库</h3>
    </div>
    
    <div class="component-categories">
      <!-- 动态渲染组件分类 -->
      <div 
        v-for="(categoryComponents, categoryKey) in componentCategories"
        :key="categoryKey"
        class="category"
      >
        <div class="category-title">
          <span>{{ getCategoryName(categoryKey) }}</span>
        </div>
        <div class="component-list">
          <div 
            class="component-item"
            v-for="component in categoryComponents"
            :key="component.componentId"
            draggable="true"
            @dragstart="handleDragStart($event, component)"
          >
            <span class="component-icon" :class="getComponentIcon(component.componentLogotype)">
              {{ component.componentName }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="ComponentPanel">
import { ref, onMounted, computed } from 'vue'
import { subassemblyGetList } from '@/api/content/content'
import { useDict } from '@/utils/dict'
const { cms_subassembly_classify } = useDict('cms_subassembly_classify')
import { useRoute } from 'vue-router'
const route = useRoute()

// 定义emits
const emit = defineEmits(['componentDrag', 'componentAdd'])

// 组件库数据 - 按分类存储
const componentCategories = ref({})

// 组件图标映射
  const componentIconMap = {
    'text': 'el-icon-document',
    'global_banner': 'el-icon-picture',
    'pc_home_tournament': 'el-icon-s-data',
    'pc_home_learn': 'el-icon-s-data',
    'pc_home_information': 'el-icon-s-data',
    'pc_support': 'el-icon-s-data',
    'pc_tournament_list': 'el-icon-s-data',
    'min_home_search': 'el-icon-search',
    'min_home_tabs': 'el-icon-menu',
    'mini_home_tournament': 'el-icon-s-data',
    'mini_home_learn': 'el-icon-s-data',
    'mini_home_information': 'el-icon-s-data',
    'mini_tournament_tabs': 'el-icon-s-grid',
    'mini_tournament_list': 'el-icon-document',
    // 默认图标
    'default': 'el-icon-menu'
  }

// 根据组件类型获取图标
const getComponentIcon = (logotype) => {
  return componentIconMap[logotype] || componentIconMap.default
}

// 根据分类标识获取分类名称
const getCategoryName = (categoryKey) => {
  // 尝试从字典中获取分类名称
  let arr = cms_subassembly_classify.value
  let item = arr.find(item => item.value === categoryKey)
  
  return item ? item.label : categoryKey
}

// 处理拖拽开始
const handleDragStart = (event, component) => {
  // 设置拖拽数据
  event.dataTransfer.setData('component', JSON.stringify(component))
  // 通知父组件开始拖拽
  emit('componentDrag', component.componentLogotype)
}

// 处理组件点击（快速添加）
const handleComponentClick = (component) => {
  // 创建组件实例
  const componentInstance = createComponentInstance(component)
  // 通知父组件添加组件
  emit('componentAdd', componentInstance)
}

// 创建组件实例
const createComponentInstance = (component) => {
  // 基础组件结构
  const baseComponent = {
    componentId: component.componentId,
    type: component.componentLogotype,
    name: component.componentName,
    description: component.componentDesc,
    style: {}
  }
  
  // 尝试解析componentJson
  let componentConfig = {}
  if (component.componentJson) {
    try {
      componentConfig = JSON.parse(component.componentJson)
    } catch (e) {
      console.error('解析组件JSON配置失败:', e)
    }
  }
  
  // 根据组件类型设置默认属性
  switch (component.componentLogotype) {
    case 'text':
      // 确保style对象存在并设置样式属性
      const textStyle = { ...baseComponent.style, ...componentConfig.style };
      // 设置默认样式属性
      if (!textStyle.fontSize) textStyle.fontSize = '16px';
      if (!textStyle.color) textStyle.color = '#333';
      if (!textStyle.textAlign) textStyle.textAlign = 'left';
      
      return {
        ...baseComponent,
        ...componentConfig,
        content: componentConfig.title || '文本内容',
        style: textStyle
      }
    case 'hyperlink':
      // 确保style对象存在并设置样式属性
      const hyperlinkStyle = { ...baseComponent.style, ...componentConfig.style };
      // 设置默认样式属性
      if (!hyperlinkStyle.fontSize) hyperlinkStyle.fontSize = '14px';
      if (!hyperlinkStyle.color) hyperlinkStyle.color = '#1890ff'; // 默认蓝色
      if (!hyperlinkStyle.textAlign) hyperlinkStyle.textAlign = 'left';
      
      return {
        ...baseComponent,
        ...componentConfig,
        content: componentConfig.content || '超链接',
        href: componentConfig.href || '#',
        target: componentConfig.target || '_blank',
        showUnderline: componentConfig.showUnderline !== false, // 默认显示下划线
        style: hyperlinkStyle
      }
    case 'global_banner':
      return {
        ...baseComponent,
        ...componentConfig,
        images: [],
        autoPlay: true,
        interval: 3000,
        loop: true,
        dataSourceId: '',
        dataSourceName: ''
      }
    case 'pc_home_tournament':
      return {
        ...baseComponent,
        ...componentConfig,
        title: '赛事中心', // 默认标题
        tournamentList: [],
        dataSourceId: '', // 数据源ID
        dataSourceName: '', // 数据源名称
        style: {
        minHeight: '400px',
        padding: '0',
        borderRadius: '0'
      }
      }
    case 'min_home_search':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '搜索框',
        description: '搜索功能模块',
        placeholder: '请输入搜索内容',
        buttonText: '搜索',
        style: {
          minHeight: '70px',
          padding: '0',
          borderRadius: '0',
          backgroundColor: '#f0f0f0'
        }
      }
    case 'mini_tournament_list':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '赛事列表',
        description: '赛事列表展示组件',
        dataSourceId: '',
        dataSourceName: '',
        style: {
        minHeight: '200px',
        padding: '0',
        borderRadius: '0'
      }
      }
    case 'blank_spacing':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '空白间距',
        description: '空白间距组件',
        style: {
          height: '20px',
          padding: '',
          margin: '',
          borderRadius: '0'
        }
      }
    case 'mini_tournament_tabs':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '赛事标签',
        description: '赛事导航标签组件',
        // 默认显示所有三个标签
        showMyTeam: true,
        showMyTournament: true,
        showScoreQuery: true,
        style: {
          padding: '0',
          borderRadius: '0',
          backgroundColor: 'white'
        }
      }
    case 'min_home_tabs':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '首页标签',
        description: '首页导航标签组件',
        // 默认显示所有五个标签
        showTournamentCenter: true,
        showLearningCenter: true,
        showNewsCenter: true,
        showTechSupport: true,
        showDatangCup: true,
        style: {
          padding: '0',
          borderRadius: '0',
          backgroundColor: 'white'
        }
      }
    case 'mini_home_tournament':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '赛事中心',
        description: '移动端赛事中心组件',
        title: '赛事中心',
        dataSourceId: '',
        dataSourceName: '',
        style: {
        minHeight: '200px',
        padding: '0',
        borderRadius: '0'
      }
      }
    case 'mini_home_learn':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '学习中心',
        description: '移动端学习中心组件',
        title: '学习中心',
        dataSourceId: '',
        dataSourceName: '',
        style: {
        minHeight: '200px',
        padding: '0',
        borderRadius: '0'
      }
      }
    case 'mini_home_information':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '资讯中心',
        description: '移动端资讯中心组件',
        title: '资讯中心',
        dataSourceId: '',
        dataSourceName: '',
        style: {
        minHeight: '200px',
        padding: '0',
        borderRadius: '0'
      }
      }
    case 'pc_home_learn':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '学习中心',
        description: 'PC端学习中心组件',
        title: '学习中心',
        dataSourceId: '',
        dataSourceName: '',
        style: {
        minHeight: '400px',
        padding: '0',
        borderRadius: '0'
      }
      }
    case 'pc_home_information':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '资讯中心',
        description: 'PC端资讯中心组件',
        title: '资讯中心',
        dataSourceId: '',
        dataSourceName: '',
        style: {
        minHeight: '400px',
        padding: '0',
        borderRadius: '0'
      }
      }
    case 'pc_support':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '技术支持',
        description: 'PC端技术支持组件',
        title: '技术支持',
        dataSourceId: '',
        dataSourceName: '',
        style: {
        minHeight: '400px',
        padding: '0',
        borderRadius: '0'
      }
      }
    case 'pc_tournament_list':
      return {
        ...baseComponent,
        ...componentConfig,
        name: '赛事列表',
        description: 'PC端赛事列表组件',
        title: '赛事列表',
        dataSourceId: '',
        dataSourceName: '',
        style: {
        minHeight: '400px',
        padding: '0',
        borderRadius: '0'
      }
      }
    default:
      return {
        ...baseComponent,
        ...componentConfig
      }
  }
}

// 从API加载组件库
const loadComponentLibrary = async () => {
  try {
    const response = await subassemblyGetList({
      displayPlatform: route.query.displayPlatform
    })
    console.log('组件库数据:', response)
    
    // 检查响应数据结构
    if (response.code === 200 && response.data) {
      // 清空现有数据
      componentCategories.value = {}
      
      // 遍历所有分类
      const data = response.data
      Object.keys(data).forEach(categoryKey => {
        // 确保是数组并且不为空
        if (Array.isArray(data[categoryKey]) && data[categoryKey].length > 0) {
          componentCategories.value[categoryKey] = data[categoryKey]
        }
      })
      
      console.log('组件库已加载，分类:', Object.keys(componentCategories.value))
    } else {
      console.warn('组件库数据格式不正确:', response)
    }
  } catch (error) {
    console.error('加载组件库失败:', error)
  }
}

// 组件挂载后加载组件库
onMounted(() => {
  loadComponentLibrary()
})
</script>

<style scoped>
.component-panel-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  background: #fafafa;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.component-categories {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.category {
  margin-bottom: 20px;
}

.category-title {
  padding: 8px 0;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
}

.component-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.component-item {
  padding: 12px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: move;
  transition: all 0.3s;
  text-align: center;
}

.component-item:hover {
  background: #ecf5ff;
  border-color: #c6e2ff;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.component-icon {
  display: block;
  font-size: 12px;
  color: #606266;
}

/* 自定义滚动条 */
.component-categories::-webkit-scrollbar {
  width: 6px;
}

.component-categories::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.component-categories::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.component-categories::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>