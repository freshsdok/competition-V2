const OLD_FILE_ORIGIN = "https://dtcup.dtxiaotangren.com";
const NEW_FILE_ORIGIN =
  import.meta.env.VITE_APP_FILE_ORIGIN || "https://www.ksup.cn";

/**
 * 递归替换接口响应中的旧静态资源域名。
 * 同时兼容普通字符串、数组、嵌套对象以及包含资源地址的富文本内容。
 */
export function replaceFileOrigin(value) {
  if (typeof value === "string") {
    return value.split(OLD_FILE_ORIGIN).join(NEW_FILE_ORIGIN);
  }

  if (Array.isArray(value)) {
    return value.map(replaceFileOrigin);
  }

  if (value !== null && typeof value === "object") {
    Object.keys(value).forEach((key) => {
      value[key] = replaceFileOrigin(value[key]);
    });
  }

  return value;
}
