import { ref } from 'vue'
import { getCompetition, listCompetitionTracks, getCompetitionTrackConfig, saveCompetitionTrack, saveCompetitionTrackRule, deleteCompetitionTrack } from '@/api/tournament/competition'
import { ElMessage } from 'element-plus';
// 创建单例状态，确保所有组件访问同一个状态实例
let state = {
  // 赛事详情
  competitionDetail: ref({}),
  // 标签页列表（赛道列表）
  tabList: ref([]),
  // 当前标签页详情
  currentTabDetail: ref({}),
  // 当前标签页配置
  currentTabConfig: ref({}),
  // 加载状态
  detailLoading: ref(false),
  // 保存状态
  saveLoading: ref(false),
  // 删除状态
  deleteLoading: ref(false)
}


/**
 * 从row数据初始化状态
 * @param {Object} row - 完整的行数据
 */
const setDetailFromRow = (row) => {
  // 设置赛事详情
  state.competitionDetail.value = row;
  
  // 初始化标签页列表
  state.tabList.value = row.competitionTrackConfigList  || []
};

/**
 * 获取标签页详情（赛道配置详情）
 * @param {Object} params - 请求参数， tabCode
 * @param {Function} callback - 回调函数
 */
const fetchTabDetail = async (params, callback) => {
  state.detailLoading.value = true
  try {
    // 使用真实API获取赛道配置详情
    const res = await getCompetitionTrackConfig(params.tabCode)
    state.detailLoading.value = false
    
    if (res.code === 200) {
      const data = res.data || {}
      state.currentTabDetail.value = data
      // 初始化当前标签页配置
      if (data.competitionConfig) {
        state.currentTabConfig.value = data.competitionConfig
      }
      callback && callback(data)
    }
  } catch (error) {
    console.error('获取标签页详情失败:', error)
    state.detailLoading.value = false
  }
}



/**
 * 保存标签页配置
 * @param {Object} tabConfig - 标签页配置，包含name, code, json
 * @param {Function} callback - 回调函数
 */
const saveTabConfig = async (tabConfig, callback) => {
  state.saveLoading.value = true
  try {
    // 使用真实API保存赛道配置规则
    const res = await saveCompetitionTrackRule(tabConfig)
    state.saveLoading.value = false
    if (res.code === 200) {
      // 成功后保存输入的信息
      if(tabConfig.competitionTrackEnterpriseList && tabConfig.competitionTrackEnterpriseList.length > 0){
        state.currentTabDetail.value = {
          ...state.currentTabDetail.value,
          competitionTrackEnterpriseList: tabConfig.competitionTrackEnterpriseList || []
        }
      }
      state.currentTabConfig.value = {
        ...state.currentTabConfig.value,
        ...tabConfig.competitionConfig
      }
    }
    callback && callback(res)
  } catch (error) {
    console.error('保存标签页配置失败:', error)
    state.saveLoading.value = false
  }
}

/**
 * 删除标签页
 * @param {Object} params - 请求参数，tabCode
 * @param {Function} callback - 回调函数
 */
const removeTab = async (params, callback) => {
  state.deleteLoading.value = true
  try {
    const res = await deleteCompetitionTrack(params.tabCode)
    state.deleteLoading.value = false
    
    if (res.code === 200) {
      const tabCode = params.tabCode
      // 更新标签页列表
      const index = state.tabList.value.findIndex(item => item.competitionTrackConfigId === tabCode)
      if (index !== -1) {
        state.tabList.value.splice(index, 1)
        // 如果删除的是当前标签页，清空当前标签页详情
        if (state.currentTabDetail.value.competitionTrackConfigId === tabCode) {
          state.currentTabDetail.value = {}
          state.currentTabConfig.value = {}
        }
      }
      // 显示删除成功提示
      ElMessage.success('删除成功')
      callback && callback(res.data)
    }
  } catch (error) {
    console.error('删除标签页失败:', error)
    ElMessage.error('删除失败')
    state.deleteLoading.value = false
  }
}

/**
 * 更新标签页列表
 * @param {Array} tabs - 标签页列表
 */
const updateTabList = (tabs) => {
  state.tabList.value = tabs
}

/**
 * 添加标签页
 * @param {Object} tab - 标签页数据
 */
const addTab = (tab) => {
  state.tabList.value.push(tab)
}

/**
 * 更新当前标签页名称
 * @param {String} tabCode - 标签页编码
 * @param {String} newName - 新名称
 */
const updateTabName = (tabCode, newName) => {
  const tabIndex = state.tabList.value.findIndex(item => item.code === tabCode)
  if (tabIndex !== -1) {
    state.tabList.value[tabIndex].name = newName
  }
  if (state.currentTabDetail.value.code === tabCode) {
    state.currentTabDetail.value.name = newName
  }
}

/**
 * 检查是否可以进入下一个表
 * @param {String} nexTTabName - 下一个表名
 */
const canNextTable = (nexTTabName) => {
  return true
}

/**
 * 重置状态
 */
export function resetCompetitionDetailSettingState() {
  state.competitionDetail.value = {}
  state.tabList.value = []
  state.currentTabDetail.value = {}
  state.currentTabConfig.value = {}
  state.detailLoading.value = false
  state.saveLoading.value = false
  state.deleteLoading.value = false
}

/**
 * 重置当前标签页状态
 */
export function resetCurrentTabState() {
  state.currentTabDetail.value = {}
  state.currentTabConfig.value = {}
}

/**
 * 赛事详情hook
 */
export function useCompetitionDetail() {
  // 每次调用返回同一个单例实例的引用
  return {
    competitionDetail: state.competitionDetail,
    tabList: state.tabList,
    currentTabDetail: state.currentTabDetail,
    currentTabConfig: state.currentTabConfig,
    detailLoading: state.detailLoading,
    saveLoading: state.saveLoading,
    deleteLoading: state.deleteLoading,
    fetchTabDetail,
    setDetailFromRow,
    saveTabConfig,
    updateTabList,
    addTab,
    removeTab,
    updateTabName,
    canNextTable,
    resetState: resetCompetitionDetailSettingState,
    resetCurrentTabState
  }
}

