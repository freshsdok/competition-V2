import axios from "axios";
import {
  ElNotification,
  ElMessageBox,
  ElMessage,
  ElLoading,
} from "element-plus";
import { getToken, removeToken } from "@/utils/auth";
import errorCode from "@/utils/errorCode";
import { tansParams, blobValidate } from "@/utils/ruoyi";
import Cookies from "js-cookie";
import cache from "@/plugins/cache";
import { saveAs } from "file-saver";
import router from "../router";
import { useCounterStore } from "@/stores/index";
import { replaceFileOrigin } from "@/utils/fileOrigin";
let downloadLoadingInstance;
// 是否显示重新登录
export let isRelogin = { show: false };

// 401白名单路由，这些路由在401时跳转到首页而不是登录页
const authWhitelist = [
  "/personal/paymentrecords",
  "/event/detail/teacherApply",
];

axios.defaults.headers["Content-Type"] = "application/json;charset=utf-8";
// 创建axios实例
const service = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  baseURL: import.meta.env.VITE_APP_BASE_API,
  // 超时 (设置超时为5分钟, 部分导出较慢)
  timeout: 1000 * 60 * 5, // 请求超时时间
});
console.log(import.meta.env.VITE_APP_BASE_API);
// request拦截器
service.interceptors.request.use(
  (config) => {
    // 是否需要设置 token
    const isToken = (config.headers || {}).isToken === false;
    // 是否需要防止数据重复提交
    const isRepeatSubmit = (config.headers || {}).repeatSubmit === false;
    if (getToken() && !isToken) {
      config.headers["Authorization"] = "Bearer " + getToken(); // 让每个请求携带自定义token 请根据实际情况自行编辑
    }
    // get请求映射params参数
    if (config.method === "get" && config.params) {
      let url = config.url + "?" + tansParams(config.params);
      url = url.slice(0, -1);
      config.params = {};
      config.url = url;
    }
    if (
      !isRepeatSubmit &&
      (config.method === "post" || config.method === "put")
    ) {
      const requestObj = {
        url: config.url,
        data:
          typeof config.data === "object"
            ? JSON.stringify(config.data)
            : config.data,
        time: new Date().getTime(),
      };
      const sessionObj = cache.session.getJSON("sessionObj");
      if (
        sessionObj === undefined ||
        sessionObj === null ||
        sessionObj === ""
      ) {
        cache.session.setJSON("sessionObj", requestObj);
      } else {
        const s_url = sessionObj.url; // 请求地址
        const s_data = sessionObj.data; // 请求数据
        const s_time = sessionObj.time; // 请求时间
        const interval = 100; // 间隔时间(ms)，小于此时间视为重复提交
        if (
          s_data === requestObj.data &&
          requestObj.time - s_time < interval &&
          s_url === requestObj.url
        ) {
          const message = "数据正在处理，请勿重复提交";
          console.warn(`[${s_url}]: ` + message);
          return Promise.reject(new Error(message));
        } else {
          cache.session.setJSON("sessionObj", requestObj);
        }
      }
    }
    return config;
  },
  (error) => {
    Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (res) => {
    // 二进制数据则直接返回
    if (
      res.request.responseType === "blob" ||
      res.request.responseType === "arraybuffer"
    ) {
      return res.data;
    }
    res.data = replaceFileOrigin(res.data);
    // 未设置状态码则默认成功状态
    const code = res.data.code || 200;
    // 获取错误信息
    const msg = errorCode[code] || res.data.msg || errorCode["default"];
    if (code === 401) {
      const counterStore = useCounterStore();
      removeToken();
      Cookies.remove("Path");
      Cookies.remove("authinfo");
      localStorage.clear();
      sessionStorage.clear();

      // 检查当前路由是否在白名单中
      try {
        const currentPath = router.currentRoute.value.path;
        if (authWhitelist.some((path) => currentPath.includes(path))) {
          router.push({ path: "/" });
          return;
        }
      } catch (e) {
        // 获取路由失败，继续原有逻辑
      }
      counterStore.increment();
    } else if (code === 300) {
      // 300已支付
      return res.data
    } else if (code === 402) {
      router.push({ path: "/402" });
    } else if (code === 404) {
      ElMessage({
        message: "系统维护中，服务暂不可用",
        type: "error",
      });
    } else if (code === 500) {
      // ElMessage({
      //   message: msg,
      //   type: "error",
      // });
      return Promise.reject(res.data);
    } else if (code === 5008) {
      return Promise.reject(res.data);
    } else if (code !== 200) {
      ElNotification.error({
        title: msg,
      });
      return Promise.reject("error");
    } else {
      return Promise.resolve(res.data);
    }
  },
  (error) => {
    console.log("err" + error);
    let { message } = error;

    // 处理用户刷新页面导致的请求中止错误
    if (message.includes("aborted") || error.code === "ERR_CANCELED") {
      // 大厂最佳实践：对于用户主动刷新导致的请求中止，保持安静处理
      // 1. 不向用户显示错误信息，避免干扰用户体验
      // 2. 确保Promise链正常传播，便于开发调试
      // 3. 在控制台记录日志，方便开发定位问题
      console.debug("请求被中止，可能是用户刷新页面导致:", error.message);
      return Promise.reject(error);
    }

    if (message == "Network Error") {
      message = "后端接口连接异常";
    } else if (message.includes("timeout")) {
      message = "系统接口请求超时";
    } else if (message.includes("Request failed with status code")) {
      // message = "系统接口" + message.substr(message.length - 3) + "异常";
       message ="系统维护中，服务暂不可用";
    }

    ElMessage({
      message: message,
      type: "error",
      duration: 5 * 1000,
    });
       
    return Promise.reject(error);
  }
);

// 通用下载方法
export function download(url, params, filename) {
  downloadLoadingInstance = ElLoading.service({
    text: "正在下载数据，请稍候",
    background: "rgba(0, 0, 0, 0.7)",
  });
  return service
    .post(url, params, {
      transformRequest: [
        (params) => {
          return tansParams(params);
        },
      ],
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      responseType: "blob",
    })
    .then(async (data) => {
      const isLogin = await blobValidate(data);
      if (isLogin) {
        const blob = new Blob([data]);
        saveAs(blob, filename);
      } else {
        const resText = await data.text();
        const rspObj = JSON.parse(resText);
        const errMsg =
          errorCode[rspObj.code] || rspObj.msg || errorCode["default"];
        ElMessage.error(errMsg);
      }
      downloadLoadingInstance.close();
    })
    .catch((r) => {
      console.error(r);
      ElMessage.error("下载文件出现错误，请联系管理员！");
      downloadLoadingInstance.close();
    });
}

// js下载方法
export function downloadJS(url, fileName) {
  const xhr = new XMLHttpRequest();
  xhr.open("GET", url, true);
  xhr.responseType = "blob";
  // 是否需要设置 token
  const isToken = (xhr.headers || {}).isToken === false;
  if (getToken() && !isToken) {
    xhr.setRequestHeader("Authorization", `Bearer ${getToken()}`);
  }
  xhr.onload = () => {
    if (xhr.status === 200) {
      // ✅ 注意：responseType 是 blob，直接使用 xhr.response
      const blob = new Blob([xhr.response], {
        type: "application/octet-stream",
      }); // 推荐使用通用二进制流
      const downloadUrl = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = downloadUrl;
      link.download = fileName || "download"; // 提供默认文件名
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link); // 清理
      URL.revokeObjectURL(downloadUrl);
    } else {
      // ✅ 处理错误状态
      console.error("下载失败，状态码：", xhr.status);
      alert("下载失败，请检查权限或重试");
    }
  };

  // ✅ 错误处理
  xhr.onerror = () => {
    console.error("网络错误，下载失败");
    alert("网络错误，下载失败");
  };
  xhr.send();
}

// 挂载下载方法,自定义表单中使用
window.downloadJS = downloadJS;

export default service;
