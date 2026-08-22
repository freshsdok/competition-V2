import { parseTime } from "@/utils/ruoyi";

/**
 * 获取当月的第一天和最后一天
 */
export function getCurrentMonthDateRange() {
    let range = []; 
    let y = new Date().getFullYear(); //获取年份
    let m = new Date().getMonth() + 1; //获取月份
    m = m < 10 ? "0" + m : m; //月份补 0
    //当前月第一天
    let fd = "01";
    //获取当月最后一日
    let ld = new Date(y, m, 0).getDate();
    range = [[y, m, fd].join("-"), [y, m, ld].join("-")]
    return range
}

/**
 * 获取当前日期的上个月
 * @returns 上个月 {y}-{m}
 */
export function getLastMonth(day) {
  let year,lastMonth;
  let date = day ? new Date(day) : new Date();
  let nowYear = date.getFullYear();   //当前年：四位数字
  let nowMonth = date.getMonth();     //当前月：0-11
  if (nowMonth == 0) {   //如果是0，则说明是1月份，上一个月就是去年的12月
    year = nowYear - 1;
    lastMonth = 12;
  }else { //不是1月份，年份为当前年，月份本来是要减1的，但是由于`getMonth()`的月份本身就是少了1的，所以月份不用变。
    year = nowYear;
    lastMonth = nowMonth;
  }
  lastMonth = lastMonth < 10 ? ('0' + lastMonth) : lastMonth;   //月份格式化：月份小于10则追加个0
  let lastYearMonth = year + '-' + lastMonth;
  return parseTime(lastYearMonth, '{y}-{m}');
}

/**
 * 获取当前月份
 */
export function getCurrentMonth(day) {
  return parseTime(new Date(), '{y}-{m}');
}

/**
 * 获取N天前
 */
export function getNDaysAgo(n=0) {
  let date = new Date();
  date.setDate(date.getDate() - n);
  return parseTime(date, '{y}-{m}-{d}');
}

/** 秒数转为 XX时XX分XX秒 */
export function secondToTime(seconds) {
  if (!seconds) return "";
  let str;
  let hours = Math.floor(seconds / 3600);
  let minutes = Math.floor((seconds % 3600) / 60);
  let remainingSeconds = seconds % 60;
  str = remainingSeconds + "秒";
  if (minutes > 0) {
    str = minutes + "分 " + str;
  };
  if (hours > 0) {
    str = hours + "时 " + str;
  };
  return str;
}

/**
 * 获取两个日期之间的所有日期
 */
export function getDatesBetween(startDate, endDate) {
  const dates = [];
  let currentDate = new Date(startDate);
  const end = new Date(endDate);

  while (currentDate <= end) {
      dates.push(new Date(currentDate).toISOString().split('T')[0]);
      currentDate.setDate(currentDate.getDate() + 1);
  }

  return dates;
}