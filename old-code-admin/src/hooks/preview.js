// 预览，跳转对应的url
import modal from "@/plugins/modal";
export const useUrlRedirect = () => {
  /**
   * 域名映射配置，键为源域名，值为目标域名
   * 格式：{ '源域名': '目标域名' }
   */
  const domainMapping = {
    // 示例配置/本地
    'http://localhost:80': 'http://localhost:81',
    'http://localhost': 'http://localhost:81',
    'http://localhost:81': 'http://localhost:80',
    // 公司测试
    'http://192.168.1.202:8897': 'http://192.168.1.202:8899',
    // 阿里云
    'http://8.130.171.65:8897': 'https://www.ksup.cn',
    'http://8.130.171.65:8877': 'http://8.130.171.65:7788'
  }

  /**
   * 获取当前域名并根据配置映射到目标域名
   * @returns {string} 目标域名或当前域名
   */
  const getTargetDomain = () => {
    if (typeof window === 'undefined') {
      return ''
    }
    // 使用origin属性获取完整域名（包含协议和主机名，可能包含端口）
    const currentOrigin = window.location.origin
    // 检查是否存在对应的映射域名
    if (domainMapping[currentOrigin]) {
      return domainMapping[currentOrigin]
    }
    // 如果没有映射，返回当前域名
    return currentOrigin
  }
  const formatPath = (relativePath) => {
    if (!relativePath) return ''
    let path = relativePath.trim()
    // 确保路径以斜杠开头
    if (!path.startsWith('/')) {
      path = `/${path}`
    }
    return path
  }

  /**
   * 跳转到指定相对路径，自动处理域名映射
   * @param {string} relativePath - 相对路径
   */
  const redirectToUrl = (relativePath = '',noShowPreview = false) => {
    if (!relativePath) {
      modal.msgError("未获取到页面路径")
      return
    }
    if (typeof window === 'undefined') {
      return
    }
    // 不展示询问弹窗
    if(noShowPreview){
      const targetDomain = getTargetDomain()
      const formattedPath = formatPath(relativePath)
      const fullUrl = `${targetDomain}${formattedPath}`
      // 在新窗口中打开URL
      window.open(fullUrl, '_blank')
      return
    }
    modal.confirm('预览需要基于最新保存的内容，请确认是否已保存页面？').then(function () {
      const targetDomain = getTargetDomain()
      const formattedPath = formatPath(relativePath)
      const fullUrl = `${targetDomain}${formattedPath}`
      // 在新窗口中打开URL
      window.open(fullUrl, '_blank')
    }).catch(function () {})
  }
  return {
    redirectToUrl
  }
}
