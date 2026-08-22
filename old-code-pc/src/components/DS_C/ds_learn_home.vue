<template>
<div class="w-full model-page" :style="detail.style">
  <div class="container-custom container-custom-self">
    <!-- 列表 -->
   <div class="w-full t-title font-bold text-center text-[#333333]">{{ (detail && detail.title) || '学习中心' }}</div>
      <template v-if="detail">
         <el-tabs v-model="activeName" @tab-click="handleClick">
            <el-tab-pane label="热门课程" name="first"></el-tab-pane>
            <el-tab-pane label="最新课程" name="second"></el-tab-pane>
            <el-tab-pane label="精品课程" name="third"></el-tab-pane>
          </el-tabs>
      </template>
      <div class="filter-list" v-if="comData.length">
        <div class="filter-list-item cursor-pointer flex flex-col justify-between" 
              v-for="(item, index) in comData" 
              :key="index"
              @click="routerHandleClick(item)">
          <div class="top-con">
            <el-image :src="item.competitionImage"
                      fit="cover"
                      class="w-full h-full top-img"></el-image>
          </div>
          <div class="filter-list-content">
            <div class="filter-info">
              <div class="flex justify-between align-center">
                <div class="title">PS-绘画0基础自学指南-线</div>
                <div class="chahua">插画</div>
              </div>
              <div class="price">¥199</div>
              <div class="description flex justify-start items-center">
                <div class="item">难度：进阶</div>
                <div class="item">学习人数：28</div>
              </div>
            </div>
            <div class="flex justify-between action">
              <div class="time">2028-09-02</div>
              <div class="flex items-center justify-end"> 
                  <div class="flex items-center justify-end" style="line-height: 0;"><img src="@/assets/images/star.png" alt="" class="mr-1 acticon-img">{{ item.competitionCollectNum || 0 }}</div>
                  <div class="flex items-center justify-end ml-4 leading-none" style="line-height: 0;"><img src="@/assets/images/share.png" alt="" class="mr-1 acticon-img">{{ item.competitionShareNum || 0 }}</div>
              </div>
            </div>
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
let activeName = $ref('')
const handleClick = (tab) => {

}

// 点击列表项跳转详情页
const routerHandleClick = (item) => {
  router.push({
    path: '/event/detail',
    query: {
      competitionId: item.competitionId,
      competitionSeriesId: item.competitionSeriesId
    }
  })
}

let comData = $ref([])
// 筛选
let query = reactive({
  competitionType: '',
  checkStatus: '6,7,8',
  joinType: '',
  competitionStartTime: '',
  competitionEndTime: '',
  feeStart: '',
  feeEnd: '',
})
let detail = $ref({})
const initQuery = () => {
  pageTotal = 0
  pageLoading = null
  pageNum = 1
  comData = []
  getDsApi(detail)
}

// 状态管理
let pageNum = $ref(1)
let pageTotal = $ref(0)
let pageLoading = $ref()
const getDsApi = async (detailData) => {
  pageLoading = true
  let params = {
    pageNum: pageNum,
    pageSize: 10,
    checkStatus: '6,7,8',
    ...query
  }
  comData = []
  pageLoading = false
  // visualizationPageMobile(detailData.dataSourceUrl,'get',params).then(res => { 
  //   if (res.code === 200) {
  //    pageTotal = res.total || 0
  //    let resRows = res.rows || []
  //     if (comData.length >= pageTotal) {
  //       pageLoading = false
  //       // 列表已加载完毕
  //       return
  //     }else{
  //       pageNum = pageNum + 1
  //     }
  //     comData = [...comData, ...resRows]
  //   } else {
  //     comData = []
  //   }
  //   pageLoading = false
  // }).catch(() => {
  //   pageLoading = false
  // })
}

// 监听props变化
// 状态管理
watch(() => props.info, (newVal) => {
  if (newVal) {
    detail = newVal
    // if (!detail || !detail.dataSourceUrl) {
    //   return
    // }
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
.filter-list{
  margin-top: -10px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  grid-gap: 40px;
  .filter-list-item{
    background: #FFFFFF;
    box-shadow: 0px 3px 6px 1px rgba(0,0,0,0.05);
    border-radius: 8px;
    border: 1px solid #E4E4E4;
    box-sizing: border-box;
    .top-con{
      .top-img{
        width: 100%;
        height: 219px;
        border-radius: 8px 0 0 8px;
      }
    }
    .filter-info{
      padding-bottom: 15px;
      .title{
        font-size: 16px;
        color: #333333;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .chahua{
        padding: 2px 12px;
        background: #FF8800;
        border-radius: 50px;
        font-size: 14px;
        color: #FFFFFF;
        flex-shrink: 0;
      }
      .price{
        margin-top: 10px;
        font-size: 15px;
        color: #FF0000;
      }
      .description{
        font-size: 15px;
        color: #999999;
        margin-top:4px;
        .item{
          width: 100%;
          text-overflow: ellipsis;
          white-space: nowrap;
          overflow: hidden;
        }
      }

    }
    .filter-list-content{
      padding: 15px;
      .action{
        font-size: 13px;
        color: #999999;
        border-top: 1px solid #E4E4E4;
        padding-top: 15px;
        .acticon-img{
          width: 16px;
        }
      }
    }
  }
}



:deep(.el-tabs){
  margin-bottom: 35px;
  .el-tabs__item{
    width: 114px;
    height: 36px;
    font-size: 20px;
    color: #333333;
    border-radius: 14px;
    border-radius: 8px;
    padding: 0;
    +.el-tabs__item{
      margin-left: 20px;
    }
  }
  .el-tabs__header{
    margin: 0px;
  }
  .el-tabs__content{
    margin: 0;
  }
  .is-active{
    font-size: 20px;
    color: #FFFFFF;
    background: #3169F8;
  }
  .el-tabs__active-bar{
    display: none;
    height: 0;
  }
  .el-tabs__nav-wrap{
    &::after{
      background: none;
      display: none;
      height: 0;
    }
  }
}
</style>
