import { ref, toRefs } from 'vue'
import { getCompetition } from '@/api/tournament/competition'
import { cloneDeep } from 'lodash-es';
import { ElMessage } from 'element-plus';

// 创建单例状态，确保所有组件访问同一个状态实例
const state = {
  editFromdetail: ref({}), // 赛事详情数据
  detailLoading: ref(false), // 详情加载状态
  submitLoading: ref(false), // 提交加载状态
  error: ref(null), // 错误信息
  fetching: ref(false), // 请求锁定状态，防止并发请求
  fetchPromise: null // 存储当前正在进行的请求Promise
}

/**
 * 统一错误处理函数
 * @param {Object|String} error - 错误对象或错误信息
 * @param {String} source - 错误来源
 */
const handleError = (error, source) => {
  const errorMessage = error?.message || error || '操作失败'
  console.error(`${source}失败:`, error)
  state.error.value = errorMessage
  state.detailLoading.value = false
  state.submitLoading.value = false
}

/**
 * 获取赛事详情
 * @param {Object} params - 请求参数，包含competitionId和competitionSeriesId
 * @param {Function} callback - 成功回调函数
 * @param {Boolean} noSend - 是否不发送请求，直接使用缓存数据
 * @returns {Promise<Object>} - 返回Promise对象，包含详情数据
 */
const fetchDetail = async (params, callback, noSend) => {
  // 如果noSend为true且已有缓存数据，直接返回缓存
  if (noSend && state.editFromdetail.value.competitionId) {
    const cachedData = cloneDeep(state.editFromdetail.value)
    callback?.(cachedData)
    return cachedData
  }

  // 参数验证
  if (!params || !params.competitionId || !params.competitionSeriesId) {
    handleError('获取赛事详情失败：参数不完整', '获取赛事详情')
    callback?.({})
    return {}
  }

  // 如果已经有请求在进行中，返回同一个Promise
  if (state.fetching.value) {
    if (state.fetchPromise) {
      try {
        const res = await state.fetchPromise
        const data = res.code === 200 ? (res.data || {}) : {}
        callback?.(data)
        return data
      } catch (error) {
        handleError(error, '获取赛事详情')
        callback?.({
          competitionSeriesName: '',
          competitionName: '',
          competitionType: '',
          competitionDesc: '',
          competitionStartTime: '',
          competitionEndTime: '',
          organizer: '',
          competitionImage: []
        })
        return {}
      }
    }
    return {}
  }

  try {
    state.fetching.value = true
    state.detailLoading.value = true
    state.editFromdetail.value = {}
    state.error.value = null
    
    // 保存当前请求的Promise，供其他调用复用
    state.fetchPromise = getCompetition(params)
    const res = await state.fetchPromise
    state.detailLoading.value = false
    
    if (res.code === 200) {
      const data = res.data || {}
      state.editFromdetail.value = data
      callback?.(data)
      return data
    } else {
      handleError(res.message || '未知错误', '获取赛事详情')
      callback?.({})
      return {}
    }
  } catch (error) {
    handleError(error, '获取赛事详情')
    callback?.({
      competitionSeriesName: '',
      competitionName: '',
      competitionType: '',
      competitionDesc: '',
      competitionStartTime: '',
      competitionEndTime: '',
      organizer: '',
      competitionImage: []
    })
    return {}
  } finally {
    // 重置请求状态
    state.fetching.value = false
    state.fetchPromise = null
  }
}

/**
 * 检查是否可以切换到下一个标签页
 * @param {String} nexTTabName - 下一个标签页名称
 * @returns {Boolean} - 返回是否可以切换
 */
const canNextTable = (nexTTabName) => {
  // 目前总是返回true，可根据实际需求扩展
  return true
}

/**
 * 通用的表单提交方法
 * @param {Object} options - 提交配置选项
 * @param {Object} options.data - 要提交的数据
 * @param {String} options.componentType - 组件类型，用于通知父组件
 * @param {Function} options.emit - 事件发射函数
 * @param {Object} options.props - 组件属性，包含competitionId和competitionSeriesId
 * @param {Function} options.saveApi - 新增数据的API函数
 * @param {Function} options.updateApi - 更新数据的API函数
 * @returns {Promise<Object>} - 返回提交结果
 */
const submitForm = async (options) => {
  const {
    data,
    componentType,
    emit,
    props,
    saveApi,
    updateApi
  } = options
  
  // 参数验证
  if (!saveApi || !updateApi) {
    handleError('缺少必要的API函数', '表单提交')
    return { success: false, message: '缺少必要的API函数' }
  }
  
  // 准备提交数据
  const apiSendData = { ...data }
  
  // 选择API函数
  const isNew = !props.competitionId || !props.competitionSeriesId
  const apiFuc = isNew ? saveApi : updateApi  
  
  // 如果是更新操作，添加ID信息
  if(!isNew){
    apiSendData.competitionId = props.competitionId
    apiSendData.competitionSeriesId = props.competitionSeriesId
  }
  
  // 更新状态
  state.submitLoading.value = true
  state.error.value = null
  
  try {
    const res = await apiFuc(apiSendData)
    
    if(res.code === 200){
      // 显示暂存成功提示
      ElMessage({
        showClose: true,
        message: '暂存成功',
        type: 'success',
      })
      
      // 准备参数用于获取最新详情
      const params = {
        competitionId: isNew ? res.data.competitionId : props.competitionId,
        competitionSeriesId: isNew ? res.data.competitionSeriesId : props.competitionSeriesId
      }
      
      // 通知父组件
      const emitData = { type: componentType }
      if(isNew && res.data && res.data.competitionId && res.data.competitionSeriesId){
        emitData.resetId = true
        emitData.data = res.data
      }
      
      // 获取最新详情
      await fetchDetail(params, null, false)
      
      // 通知父组件
      emit('changeNextTab', emitData)
      state.submitLoading.value = false
      
      return { success: true, data: res.data }
    }else{
      // 显示失败提示
      const errorMsg = res.message || '暂存失败'
      // ElMessage({
      //   showClose: true,
      //   message: errorMsg,
      //   type: 'error',
      // })
      handleError(errorMsg, '表单提交')
      
      return { success: false, message: errorMsg }
    }
  } catch (error) {
    const errorMsg = error.message || '网络错误，暂存失败'
    // ElMessage({
    //   showClose: true,
    //   message: errorMsg,
    //   type: 'error',
    // })
    handleError(error, '表单提交')
    
    return { success: false, message: errorMsg }
  }
}

/**
 * 重置赛事详情状态
 */
export function resetCompetitionDetailState(businessDetail) {
  state.editFromdetail.value = {}
  console.log('resetCompetitionDetailState', businessDetail)
  if(businessDetail) {
    state.editFromdetail.value = businessDetail
  }
  state.detailLoading.value = false
  state.submitLoading.value = false
  state.error.value = null
}

/**
 * 使用赛事详情hooks
 * @returns {Object} - 返回赛事详情相关的状态和方法
 */
export function useCompetitionDetail() {
  // 每次调用返回同一个单例实例的引用
  return {
    fetchDetail,
    submitForm, // 新增通用提交方法
    canNextTable,
    resetState: resetCompetitionDetailState,
    ...toRefs(state) // 暴露所有状态
  }
}

