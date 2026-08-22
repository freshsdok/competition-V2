import request from '@/utils/request'

// 查询数据源信息列表
export function listPage(query) {
  return request({
    url: '/content/page/list',
    method: 'get',
    params: query
  })
}

// 查询数据源信息详细
export function getPage(pageId) {
  return request({
    url: '/content/page/' + pageId,
    method: 'get'
  })
}

// 新增数据源信息
export function addPage(data) {
  return request({
    url: '/content/page',
    method: 'post',
    data: data
  })
}

// 修改数据源信息
export function updatePage(data) {  
  return request({
    url: '/content/page',
    method: 'put',
    data: data
  })
}

// 删除数据源信息
export function delPage(data) {
  return request({
    url: '/content/page',
    method: 'delete',
    data: data
  })
}



/**
 * 保存页面配置
 * @param {Object} pageData - 页面数据
 * @param {string} pageData.pageId - 页面ID
 * @param {string} pageData.pageContent - 页面内容JSON字符串
 * @returns {Promise} 保存结果
 */
export const savePageContent = async (pageData) => {
  return request({
    url: '/content/page/editContent',
    method: 'put',
    data: pageData
  })
}

/**
 * 获取页面配置
 * @param {string} pageId - 页面ID
 * @returns {Promise} 页面配置数据
 */
export const getPageContent = async (pageId) => {
  return request({
    url: '/content/page/' + pageId,
    method: 'get'
  })
}


/**
 * 复制页面
 * @param {string} pageId - 页面ID
 * @returns {Promise} 页面配置数据
 */
export const copyPageContent = async (pageId) => {
  return request({
    url: '/content/page/copy/' + pageId,
    method: 'get'
  })
}

