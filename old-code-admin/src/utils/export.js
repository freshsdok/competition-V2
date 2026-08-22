import modal from "@/plugins/modal";
import router from "@/router";

/**
 * 异步导出通用方法
 * @param {Function} exportApi - 导出API函数，返回Promise
 * @param {Object} params - 导出参数
 * @param {Object} options - 配置选项
 * @param {string} options.successMsg - 成功提示消息，默认'导出成功，请稍后在"导出管理"列表查看文件'
 * @param {string} options.confirmButtonText - 确认按钮文字，默认'去查看'
 * @param {string} options.cancelButtonText - 取消按钮文字，默认'取消'
 * @param {string} options.redirectPath - 跳转路径，默认'/fileTask/exportList'
 * @returns {Promise}
 */
export function handleAsyncExport(exportApi, params = {}, options = {}) {
  const {
    successMsg = '导出成功，请稍后在"导出管理"列表查看文件',
    confirmButtonText = '去查看',
    cancelButtonText = '取消',
    redirectPath = '/fileTask/exportList'
  } = options;

  return new Promise((resolve, reject) => {
    exportApi(params)
      .then((response) => {
        if (response.code === 200) {
          modal.confirm(successMsg, '导出成功', {
            confirmButtonText,
            cancelButtonText,
            type: 'success',
          })
            .then(() => {
              router.push({ path: redirectPath });
            })
            .catch(() => { });
          resolve(response);
        } else {
          reject(new Error(response.msg || '导出失败'));
        }
      })
      .catch((error) => {
        reject(error);
      });
  });
}

/**
 * 带加载状态的异步导出
 * @param {Function} exportApi - 导出API函数
 * @param {Object} params - 导出参数
 * @param {Object} loadingRef - 加载状态ref对象
 * @param {Object} options - 配置选项
 */
export function handleAsyncExportWithLoading(exportApi, params, loadingRef, options = {}) {
  if (loadingRef && typeof loadingRef.value !== 'undefined') {
    loadingRef.value = true;
  }

  return handleAsyncExport(exportApi, params, options)
    .finally(() => {
      if (loadingRef && typeof loadingRef.value !== 'undefined') {
        loadingRef.value = false;
      }
    });
}

export default {
  handleAsyncExport,
  handleAsyncExportWithLoading
};
