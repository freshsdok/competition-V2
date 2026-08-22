<template>
<div class="w-full model-page" :style="detail.style">
  <div class="container-custom container-custom-self">

      <div class="tournament-carousel-container w-full">
        <div class="w-full t-title font-bold text-center text-[#333333]">{{ (detail && detail.title) || '赛事中心' }}</div>
        <div class="w-full tur-content flex flex-nowrap justify-between"  v-if="comData.length">
          <div class="tur-content-left h-full rounded-[14px]">
              <el-carousel class="w-full h-full rounded-[14px]"
                          :arrow="comData.length > 1 ? 'hover' : 'never'"
                          :indicator-position="comData.length > 1 ? '' : 'none'"
                          :loop="true"
                          height="480px"
                          @change="handleChange"
                          :initial-index="initialIndex"
                          :interval="5000"
                          :autoplay="isAutoplay">
                <el-carousel-item v-for="item in comData" :key="item.competitionCode" class="w-full h-full rounded-[14px]">
                    <!-- <div class="tur-content-left-title" :class="['status-'+item.checkStatus]">
                      {{ getCheckStatusName(tourDetail?.checkStatus) }}
                    </div> -->
                     <el-image :src="item.competitionImage"
                                fit="cover"
                                class="w-full h-full rounded-[14px] hvr-grow-shadow"></el-image>
                </el-carousel-item>
              </el-carousel>
          </div>
          <div class="tur-content-right h-full flex flex-col justify-between" @mouseenter="autoStart" @mouseleave="autoEnd">
            <div class="w-full tur-right-top">
              <div class="tur-content-right-title w-full">{{ tourDetail?.competitionName || '' }}</div>
              <div class="tur-tags flex items-center justify-start">
              </div>
              <div class="w-full rich-content tur-content-right-desc" v-html="tourDetail?.competitionDesc || ''"></div>
            </div>
            <div class="tur-content-right-btn w-full flex items-center justify-between">
              <!-- <el-button class="btn-apply" @click="toTeamApply(tourDetail)">报名参赛</el-button> -->
              <el-button class="btn-grade hvr-float" @click="routerHandleClick(tourDetail)">查看详情</el-button>
            </div>
          </div>
        </div>
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
</template>
<script setup>
import { visualizationPageMobile } from '@/api/visualization'
import { useRouter } from 'vue-router'
const router = useRouter()
const props = defineProps({
  // 数据
  info: {
    type: Object,
    default: {},
  }
})

// 状态管理
let detail = $ref({})
let comData = $ref([])
let isAutoplay = $ref(true)
const autoStart = () => {
  isAutoplay = false
}
const autoEnd = () => {
  isAutoplay = true
}

// 路由跳转
const routerHandleClick = (item) => {
  router.push({
    path: '/event/detail',
    query: {
      competitionId: item.competitionId,
      competitionSeriesId: item.competitionSeriesId
    }
  })
}
// 跳转到报名
const toTeamApply = (item)=>{
  router.push({
    path: '/event/detail/apply',
    query: {
      competitionId: item.competitionId,
      competitionSeriesId: item.competitionSeriesId,
      saiShiName: item.competitionName
    }
  })
}

// 检查状态名称映射
const checkStatusMap = {
  6: '未开始',
  7: '进行中',
}
// 获取检查状态名称
const getCheckStatusName = (status) => {
  return checkStatusMap[status] || '未知状态'
}

// API调用函数
// 当前赛事index,和所有赛事的列表
let initialIndex = $ref(0)
let pageLoading = $ref()
const getDsApi = async (detailData) => {
  pageLoading = true
  let query = {
    pageNum: 1,
    pageSize: 10,
    checkStatus: '6,7'
  }
  visualizationPageMobile(detailData.dataSourceUrl,'get',query).then(res => { 
    if (res.code === 200) {
      comData = res.rows || []
    } else {
      comData = []
    }
    pageLoading = false
  }).catch(() => {
    pageLoading = false
  })
}
const handleChange = (index) => {
  initialIndex = index
}
// 当前赛事详情
const tourDetail = $computed(()=>{
  return comData[initialIndex]
})

// 监听props变化
watch(() => props.info, (newVal) => {
  if (newVal) {
    detail = newVal
    if (!detail || !detail.dataSourceUrl) {
      return
    }
    getDsApi(detail)
  }
}, { immediate: true }) // 立即执行，处理初始值

</script>
<style lang="scss" scoped>
.model-page{
  background: #FFFFFF;
  overflow: hidden;
}
.container-custom-self {
  overflow: hidden;
  padding-top: 80px;
  padding-bottom: 80px;
  .t-title{
    font-size: 36px;
  }
  .tur-content{
    height: 480px;
    margin-top:50px;
  }
  .tur-content-left{
    width: 700px;
    height: 480px;
    position: relative;
    flex-shrink: 0;
    .tur-content-left-title{
      font-weight: bold;
      font-size: 30px;
      color: #333333;
      line-height: 42px;
      text-align: left;
      position: absolute;
      top: 0;
      left: 0;
      z-index: 1;
      width: 116px;
      height: 46px;
      line-height: 46px;
      border-radius: 14px 0px 14px 0px;
      font-size: 28px;
      color: #FFFFFF;
      text-align: center;
    }
    .status-6{
      background: #51C512;
    }
    .status-7{
      background: #4B7EFF;
    }
    .status-8{
      background: #999999;
    }
  }
  .tur-content-right{
    margin-left: 40px;
    flex:1;
    flex-shrink: 0;
    height: 100%;
    overflow: hidden;
    .tur-content-right-title{
      font-weight: bold;
      font-size: 30px;
      color: #333333;
      line-height: 42px;
      text-align: left;
      // 超过3行省略号
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp:2;
      -webkit-box-orient: vertical;
    }
    .tur-content-right-btn{
      .btn-apply{
        font-weight: bold;
        font-size: 24px;
        color: #FFFFFF;
        text-align: center;
        width: 50%;
        height: 80px;
        background: linear-gradient(#3169F8 0%, #33DBDB 100%);
        border-radius: 10px;
        margin-right: 40px;
      }
      .btn-grade{
        font-weight: bold;
        font-size: 24px;
        color: #FFFFFF;
        text-align: center;
        width: 50%;
        height: 80px;
        // background: linear-gradient(#F68801 0%, #FFDC2D 100%);
        background: #F68801;
        border-radius: 10px;
      }
    }
    .tur-content-right-desc{
      margin-top: 30px;
      overflow: hidden;
      font-weight: 400;
      font-size: 18px;
      color: #333333;
      line-height: 36px;
      text-align: left;
      text-indent: 2em;
      overflow: hidden;
      max-height: 215px;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp:6;
      -webkit-box-orient: vertical;
    }
    .tur-tags{
      margin-top: 20px;
      .tur-tags-design{
        width: 100px;
        height: 36px;
        background: #EBF0FF;
        border-radius: 4px;
        font-size: 18px;
        color: #3169F8;
        text-align: center;
        margin-right: 20px;
      }
      .tur-tags-single{
        width: 100px;
        height: 36px;
        background: #F2FFEB;
        border-radius: 4px;
        font-size: 18px;
        color: #51C512;
        text-align: center;
      }
    }
  }
  .tur-btm{
    width: 100%;
    padding: 26px 45px 26px 45px;
    border-radius: 10px;
    background: #FFFFFF;
    box-shadow: 0px 0px 6px 1px rgba(0,0,0,0.1);
    .tur-btm-item{
      .tur-btm-item-icon{
        width: 78px;
        height: 78px;
      }
      .tur-btm-item-name{
        font-weight: bold;
        font-size: 20px;
        color: #333333;
        margin-left:  20px;
      }
    }
    .tur-btm-item-line{
      width: 1px;
      height: 78px;
      background: #E4E4E4;
    }
  }
}
</style>