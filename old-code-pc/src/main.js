import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import '@/styles/index.scss'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 全局导入vxe-table基础组件和列组件
import VxeUITable from 'vxe-table'
import 'vxe-table/lib/style.css'
import { useDict } from '@/utils/dict'
import DictTag from '@/components/DictTag'
// import 'amfe-flexible'
import '@/utils/rem.js'
import 'animate.css'
import 'hover.css/css/hover-min.css'
import { download,downloadJS } from '@/utils/request'
import plugins from '@/plugins'

import * as VForm3 from "@/../lib/vform/designer.umd.js"; //引入VForm3库
import "../lib/vform/designer.style.css"; //引入VForm3样式
// 引入防抖节流函数文件
import { debounce, throttle } from "@/utils/dbucTrtl";

import Pagination from '@/components/Pagination'
import { ossFileFuc } from "@/hooks/download";
const { uploadOssFile,downloadOssFile } = ossFileFuc();

const app = createApp(App)
app.config.globalProperties.useDict = useDict
app.config.globalProperties.download = download
app.config.globalProperties.downloadJS = downloadJS
app.config.globalProperties.debounce = debounce;
app.config.globalProperties.throttle = throttle;
app.config.globalProperties.uploadOssFile = uploadOssFile;
window.uploadOssFile = uploadOssFile;
app.config.globalProperties.downloadOssFile = downloadOssFile;
window.downloadOssFile = downloadOssFile;
// 全局注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}


app.component('DictTag', DictTag)
app.use(createPinia())
app.use(plugins)
app.use(VForm3)
app.component('Pagination', Pagination)
app.use(ElementPlus, {
  locale: zhCn,
})
// 注册vxe-table组件
app.use(VxeUITable)

app.use(router)

app.mount('#app')
