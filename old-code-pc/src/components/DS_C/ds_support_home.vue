<template>
<div class="w-full model-page" :style="detail.style">
  <div class="container-custom container-custom-self">
    <!-- 列表 -->
   <div class="w-full t-title font-bold text-center text-[#333333]">{{ (detail && detail.title) || '技术支持' }} </div>
      <div class="w-full question flex items-start justify-start">
        <div class="support-img-box">
          <div class="support-title">常见问题解答</div>
          <img src="@/assets/images/support.png" class="support-img"></img>
        </div>
        <div class="support-right">
        <div class="flex items-center justify-start ipt-container">
            <el-input v-model="keyWord" 
                      size="default"
                      class="ipt-width2" 
                      clearable
                      @clear="handleKeyWordChange"
                      placeholder="请输入关键词搜索" >
              <template #prefix>
                <el-icon><Search class="text-[#64666A]"/></el-icon>
              </template>
            </el-input>
            <el-button @click="handleKeyWordChange" size="default" type="primary" class="search-btn">搜索</el-button>
          </div>
          <el-collapse v-model="activeName" v-if="comData.length">
             <el-collapse-item  v-for="(item,index) in comData"
                                :title="item.questions"
                                :name="index">
               <template #title>
                <div class="w-full flex items-center justify-start">
                  <el-icon class="transform-icon"><CaretRight class="text-[#999999]" /></el-icon>
                  <div class="ml-[15px]" v-html="highlightKeyword(item.questions, keyWord)"></div>
                </div>
              </template>
              <div class="text-[#666666] text-[16px]" v-html="highlightKeyword(item.answer, keyWord)"></div>
             </el-collapse-item>
          </el-collapse>
          <template v-else>
            <!-- 骨架屏，用于数据加载时显示 -->
            <el-skeleton 
              animated 
              v-if="pageLoading"
              :rows="10">
            </el-skeleton>
            <el-empty description="暂无数据" v-if="(pageLoading === false) && (!comData || comData.length === 0)"></el-empty>
          </template>
        </div>
      </div>
  </div>
</div>
</template>


<script setup>
import { visualizationPageMobile } from '@/api/visualization'
import { useRouter } from 'vue-router'
const router = useRouter()
import modal from "@/plugins/modal";
const props = defineProps({
  // 数据
  info: {
    type: Object,
    default: {},
  }
})
// 活动名称
let activeName = $ref([0])

let comData = $ref([])

let detail = $ref({})
const initQuery = () => {
  pageLoading = null
  comData = []
  getDsApi(detail)
}

const keyWord = $ref('')
const handleKeyWordChange = (val) => {
  initQuery()
}

// 关键词高亮函数
const highlightKeyword = (text, keyword) => {
  if (!keyword) return text
  // 转义正则特殊字符
  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  // 使用正则表达式全局匹配关键词，忽略大小写
  const regex = new RegExp(`(${escapedKeyword})`, 'gi')
  // 替换匹配的关键词，添加高亮标签
  return text.replace(regex, '<span class="highlight">$1</span>')
}

// 状态管理
let pageLoading = $ref()
const getDsApi = async (detailData) => {
  pageLoading = true
  let params = {
    keyWord: keyWord
  }
  visualizationPageMobile(detailData.dataSourceUrl,'get',params).then(res => { 
    if (res.code === 200) {
     let resRows = res.rows || []
     comData = resRows
     if(!keyWord){
       activeName = [0]
     }else{
      if(resRows.length){
        activeName = []
        for(let i = 0;i<resRows.length;i++){
          activeName.push(i)
        }
      }
     }
  
    }
    pageLoading = false
  }).catch(() => {
    pageLoading = false
  })
}


// 监听props变化
// 状态管理
watch(() => props.info, (newVal) => {
  if (newVal) {
    detail = newVal
    if (!detail || !detail.dataSourceUrl) {
      return
    }
    initQuery()
  }
}, { immediate: true }) // 立即执行，处理初始值


</script>

<style lang="scss" scoped>
.model-page{
  background: #F2F5F7;
  overflow: hidden;
}
.container-custom-self{
  padding-top: 45px;
  padding-bottom:100px;
  position: relative;
  .t-title{
    font-size: 36px;
    margin-bottom: 50px;
  }
}
.question{
  position: relative;
  height: 666px;
  .support-img-box{
    height: 100%;
    position: relative;
    .support-title{
      width: 100%;
      position: absolute;
      top: 80px;
      left: 0;
      text-align: center;
      font-weight: 500;
      font-size: 36px;
      color: #FFFFFF;
    }
    .support-img{
      width: 370px;
      height: 100%;
    }
  }
  .support-right{
    height: 100%;
    flex: 1;
    background: #ffffff;
    border-radius: 10px;
    padding-left: 30px;
    padding-right: 30px;
    padding-top: 30px;
    position: relative;
    display: flex;
    justify-content: space-between;
    flex-direction: column;
    align-items: flex-start;
    :deep(.ipt-container){
      width: 100%;
      border-radius: 10px 10px 10px 10px;
      border: 2px solid #ebeef5;
      padding-right: 10px;
      margin-bottom: 30px;
      .el-input__wrapper{
        padding: 10px 10px 10px 25px;
        border-radius: 12px;
        border-right: none;
        box-shadow: none !important;
      }
      .el-input-group__append{
        background: transparent !important;
      }
      .search-btn{
        width: 138px;
        height: 45px;
        background: $main-color;
        border-radius: 10px;
        font-size: 20px;
        color: #FFFFFF;
      }
    }
    :deep(.el-collapse){
      width: 100%;
      flex: 1;
      overflow-y: auto;
      border: none !important;
      .el-collapse-item{
        border: none !important;
        box-shadow: none !important;
        &:first-child{
          border-top: none;
        }
        .el-collapse-item__header{
          border: none;
          .el-collapse-item__title{
            font-size: 20px;
            color: #333333;
            fong-weight: 500;
            @include ellipsis(1);
            border-bottom: 1px solid #ebeef5;
          }
        }
        .el-collapse-item__content{
          padding: 30px 0px 30px;
          text-indent: 2em;
        }
        .el-collapse-item__arrow {
          display: none;
        }
        // 高亮样式
        .highlight{
          color: $main-color;
        }
        .is-active{
          .transform-icon{
            transform: rotate(90deg);
          }
        }
      }
    }
  }
}
</style>
