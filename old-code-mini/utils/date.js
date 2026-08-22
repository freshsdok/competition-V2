/**
 * 日期格式化工具函数
 */

/**
 * 格式化日期为 YYYY-MM-DD
 * @param {string|number|Date} value - 日期值（字符串、时间戳、Date对象）
 * @param {string} format - 格式化模板，默认 'YYYY-MM-DD'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(value, format = 'YYYY-MM-DD') {
  if (!value) return ''

  try {
    let date

    if (value instanceof Date) {
      date = value
    } else if (typeof value === 'number') {
      date = new Date(value)
    } else if (typeof value === 'string') {
      if (/^\d+$/.test(value)) {
        date = new Date(Number(value))
      } else {
        date = new Date(value.replace(/-/g, '/'))
      }
    } else {
      return String(value)
    }

    if (isNaN(date.getTime())) {
      return String(value)
    }

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')

    return format
      .replace('YYYY', year)
      .replace('MM', month)
      .replace('DD', day)
      .replace('HH', hours)
      .replace('mm', minutes)
      .replace('ss', seconds)
  } catch (e) {
    return String(value)
  }
}

/**
 * 格式化日期时间为 YYYY-MM-DD HH:mm:ss
 * @param {string|number|Date} value - 日期值
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(value) {
  return formatDate(value, 'YYYY-MM-DD HH:mm:ss')
}

/**
 * 格式化时间为 HH:mm:ss
 * @param {string|number|Date} value - 日期值
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(value) {
  return formatDate(value, 'HH:mm:ss')
}
