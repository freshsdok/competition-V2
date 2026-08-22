// rem等比适配配置文件
// 基准大小
// const baseSize = 16;
// 16是最佳默认的rem基准值，可以按照自己的需求修改，我稍微大一点
// postcss.config.js 同步rootValue修改
const baseSize = 16;
// 设置 rem 函数
function setRem() {
  // 当前页面屏幕分辨率相对于 1920 宽的缩放比例，可根据自己需要修改
  let scale = document.documentElement.clientWidth / 1920;
  let rem = baseSize * scale;
  if(rem <= 11){
    rem = 11
  }
  if(rem >= 16){
    rem = 16
  }
  // console.log(rem,'rem---123')
  // 设置页面根节点字体大小，确保字体不会太小导致布局混乱
  document.documentElement.style.fontSize = `${rem}px`;
}
// 初始化
setRem();

// 改变窗口大小时重新设置 rem
window.onresize = () => {
  setRem();
};
