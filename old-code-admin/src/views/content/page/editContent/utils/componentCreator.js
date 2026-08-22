import { nanoid } from 'nanoid';
import { cloneDeep } from 'lodash';

/**
 * 创建组件实例 - 使用深拷贝避免引用问题
 * @param {Object} componentData - 组件数据
 * @returns {Object} 新创建的组件实例
 */
export const createComponentInstance = (componentData) => {
  // 适配新的API组件数据结构
  // 判断是旧格式还是新格式的组件数据
  const isNewApiFormat = componentData.hasOwnProperty('componentLogotype')
  
  // 基础组件结构，优先使用新格式字段
  const baseComponent = {
    id: nanoid(),
    type: isNewApiFormat ? componentData.componentLogotype : componentData.type,
    name: isNewApiFormat ? componentData.componentName : componentData.label,
    description: isNewApiFormat ? componentData.componentDesc : '',
    componentId: isNewApiFormat ? componentData.componentId : '',
    style: {}
  }
  
  // 尝试解析componentJson（如果是新API格式）
  let componentConfig = {}
  if (isNewApiFormat && componentData.componentJson) {
    try {
      componentConfig = JSON.parse(componentData.componentJson)
      // 深拷贝配置对象，避免共享引用
      componentConfig = cloneDeep(componentConfig)
    } catch (e) {
      console.error('解析组件JSON配置失败:', e)
    }
  }
  
  // 组件默认配置模板 - 确保每个组件都有独立的默认配置
  const defaultTemplates = {
    text: {
      content: '这是一段文本',
      fontSize: '16px',
      color: '#333',
      textAlign: 'left'
    },
    textComponent: {
      content: '这是一段文本',
      fontSize: '16px',
      color: '#333',
      textAlign: 'left'
    },
    global_banner: {
        images: [],
        autoPlay: true,
        interval: 3000,
        content: '轮播图展示',
        style: {
          borderRadius: '0'
        }
      },
    pc_home_tournament: {
      title: '赛事中心',
      tournamentList: [],
      dataSourceId: '',
      dataSourceName: '',
      style: {
        minHeight: '400px',
        padding: '0',
        borderRadius: '0'
      }
    },
    min_home_search: {
      placeholder: '请输入搜索内容',
      buttonText: '搜索',
      style: {
        minHeight: '100px',
        padding: '0',
        borderRadius: '0'
      }
    },
    min_home_tabs: {
      showTournamentCenter: true,
      showLearningCenter: true,
      showNewsCenter: true,
      showTechSupport: true,
      showDatangCup: true,
      style: {
        padding: '',
        margin: '',
        borderRadius: '0'
      }
    },
    mini_tournament_tabs: {
      showMyTeam: true,
      showMyTournament: true,
      showScoreQuery: true,
      style: {
        padding: '',
        margin: '',
        borderRadius: '0'
      }
    },
    mini_tournament_list: {
      style: {
        minHeight: '500px',
        padding: '0',
        borderRadius: '0'
      }
    },
    mini_home_tournament: {
      title: '赛事中心',
      style: {
        minHeight: '400px',
        padding: '0',
        borderRadius: '0'
      }
    },
    mini_home_learn: {
      title: '学习中心',
      displayTabs: true,
      style: {
        minHeight: '500px',
        padding: '0',
        borderRadius: '0'
      }
    },
    pc_home_learn: {
      title: '学习中心',
      style: {
        minHeight: '500px',
        padding: '0',
        borderRadius: '0'
      }
    },
    pc_home_information: {
      title: '资讯中心',
      dataSourceId: '',
      dataSourceName: '',
      dataSourceTwoId: '',
      dataSourceTwoName: '',
      style: {
        minHeight: '500px',
        padding: '0',
        borderRadius: '0'
      }
    },
    pc_support: {
      title: '技术支持',
      style: {
        minHeight: '500px',
        padding: '0',
        borderRadius: '0'
      }
    },
    pc_tournament_list: {
      title: '赛事列表',
      style: {
        minHeight: '500px',
        padding: '0',
        borderRadius: '0'
      }
    },
    mini_home_information: {
      title: '资讯中心',
      displayTabs: true,
      // 最新资讯数据源
      dataSourceId: '',
      dataSourceName: '',
      // 公告通知数据源
      dataSourceTwoId: '',
      dataSourceTwoName: '',
      style: {
        minHeight: '500px',
        padding: '0',
        borderRadius: '0'
      }
    },
    mini_information: {
      title: '信息展示',
      // 最新资讯数据源
      dataSourceId: '',
      dataSourceName: '',
      // 公告通知数据源
      dataSourceTwoId: '',
      dataSourceTwoName: '',
      // 是否置顶轮播最新资讯前三条
      topInfoCarousel: true,
      // 是否置顶轮播公告通知前三条
      topNoticeCarousel: true,
      style: {
        minHeight: '500px',
        padding: '0',
        borderRadius: '0'
      }
    },
    pc_information: {
      title: '信息展示',
      // 最新资讯数据源
      dataSourceId: '',
      dataSourceName: '',
      // 公告通知数据源
      dataSourceTwoId: '',
      dataSourceTwoName: '',
      // 是否置顶轮播最新资讯前三条
      topInfoCarousel: true,
      // 是否置顶轮播公告通知前三条
      topNoticeCarousel: true,
      style: {
        minHeight: '500px',
        padding: '0',
        borderRadius: '0'
      }
    },
    blank_spacing: {
      style: {
        height: '20px',
        padding: '',
        margin: '',
        borderRadius: '0'
      }
    },
    title: {
      content: '标题文本',
      level: 2,
      fontWeight: 'bold',
      style: {
        fontSize: '24px',
        color: '#333',
        textAlign: 'left'
      }
    },
    paragraph: {
      content: '这是一个段落，包含了一些描述性的文本内容。',
      lineHeight: '1.6',
      style: {
        fontSize: '14px',
        color: '#666',
        textAlign: 'left'
      }
    },
    image: {
      src: '',
      alt: '图片描述',
      width: '100%',
      height: 'auto'
    },
    button: {
      text: '按钮文本',
      buttonType: 'primary',
      size: 'medium',
      action: ''
    },
    card: {
      title: '卡片标题',
      content: '卡片内容',
      shadow: 'hover'
    },
    hyperlink: {
      content: '超链接',
      href: '#',
      showUnderline: true,
      style: {
        fontSize: '14px',
        color: '',
        textAlign: 'left'
      }
    }
  }
  
  // 先合并基础组件和配置
  const baseInstance = {
    ...baseComponent,
    ...componentConfig
  }
  
  // 获取默认模板
  const defaultTemplate = defaultTemplates[baseComponent.type] ? cloneDeep(defaultTemplates[baseComponent.type]) : {}
  
  // 创建最终实例，但先不合并所有属性
  let instance = {
    ...baseInstance
  }
  
  // 特殊处理标题内容
  if (instance.content === '这是一段文本' && componentConfig.title) {
    instance.content = componentConfig.title
  }
  
  // 确保样式对象是独立的并正确合并
  instance.style = {}
  
  // 先合并默认模板的样式
  if (defaultTemplate.style) {
    instance.style = { ...cloneDeep(defaultTemplate.style) }
  }
  
  // 再合并配置中的样式（优先级更高）
  if (componentConfig.style) {
    instance.style = { ...instance.style, ...cloneDeep(componentConfig.style) }
  }
  
  // 最后合并baseComponent中的样式（如果有）
  if (baseComponent.style) {
    instance.style = { ...instance.style, ...cloneDeep(baseComponent.style) }
  }
  
  // 将根级别的样式属性移到style对象中（处理兼容性）
  const styleProps = ['fontSize', 'color', 'textAlign']
  styleProps.forEach(prop => {
    // 检查是否有根级别的样式属性
    if (instance[prop] && !instance.style[prop]) {
      instance.style[prop] = instance[prop]
    }
    // 删除根级别的样式属性，避免重复
    delete instance[prop]
  })
  
  // 合并其他默认模板属性（排除已处理的style）
  const { style, ...otherDefaults } = defaultTemplate
  instance = { ...instance, ...otherDefaults }
  
  // 初始化通用样式属性，确保拖拽新组件时不会受到上一个组件的样式影响
  // 设置默认圆角为0
  instance.style.padding = instance.style.padding || ''
  instance.style.margin = instance.style.margin || ''
  instance.style.borderRadius = instance.style.borderRadius || '0'
  
  // 最终再深拷贝一次，确保所有嵌套对象都是独立的
  return cloneDeep(instance)
  }