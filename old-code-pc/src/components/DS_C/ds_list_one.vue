<template>
<div class="w-full model-page" :style="detail.style">
  <div class="container-custom container-custom-self">
    <!-- 筛选 -->
    <div class="list-one-filter">
      <div class="fliter-left">
        <div class="left-line">
          <span class="fliter-title">赛事类型</span>
          <span class="filter-part"> </span>
          <p class="filter-scroll">
            <span class="filter-option" 
                  v-for="item in competitionTypeMap" 
                  :key="item.value" 
                  @click="filterChange(item.value)"
                  :class="{ 'filter-select': item.value === query.competitionType }">{{ item.label }}</span>
          </p>
        </div>
        <div class="flex items-center justify-start">
          <span class="fliter-title w-[60px] inline-block">时间</span> 
          <span class="filter-part"> </span>
          <el-date-picker class="ipt-width" 
                          v-model="query.competitionStartTime" 
                          size="default" 
                          type="date" 
                          @change="handleDateChange"
                          value-format="YYYY-MM-DD HH:mm:ss"
                          placeholder="赛事开始时间" />
          <span class="w-[20px] inline-block text-center">-</span>
          <el-date-picker class="ipt-width"  
                          v-model="query.competitionEndTime" 
                          size="default" 
                          type="date" 
                          @change="handleDateChange"
                          value-format="YYYY-MM-DD HH:mm:ss"
                          placeholder="赛事结束时间" />
        </div>
      </div>
      <div class="fliter-right">
        <div class="left-line">
          <span class="fliter-title">赛事状态</span>
          <span class="filter-part"></span>
          <p class="filter-scroll">
            <span class="filter-option" 
                  v-for="item in checkStatusMap" 
                  :key="item.value" 
                  @click="filterStatusChange(item.value)"
                  :class="{ 'filter-select': item.value === query.checkStatus }">{{ item.label }}</span>
          </p>
        </div>
        <div>
          <span class="fliter-title w-[60px] inline-block">赛事名称</span>
          <span class="filter-part"> </span>
          <el-input v-model="query.competitionName" 
                    size="default"
                    class="ipt-width2" 
                    clearable
                    @input="handleCompetitionNameChange"
                    placeholder="赛事名称" />
        </div>
      </div>
    </div>
    <!-- 列表 -->
   <template v-if="comData.length">
      <div class="filter-list">
        <div class="filter-list-item cursor-pointer hvr-float-shadow" 
              v-for="(item, index) in comData" 
              :key="index"
              @click="routerHandleClick(item)">
          <div class="filter-list-img">
            <el-image :src="item.competitionImage"
                      fit="cover"
                      class="w-full h-full rounded-[8px_0px_0px_8px]"></el-image>
            <!-- <span class="filter-status" :class="['status-'+item.checkStatus]">
              {{ getCheckStatusName(item.checkStatus) }}
            </span> -->
          </div>
          <div class="filter-list-content">
            <div class="content-title text-ellipsis">{{ item.competitionName }}</div>
            <div class="content-desc text-ellipsis rich-content" v-html="item.competitionDesc"></div>
            <!-- <div class="tur-tags flex items-center justify-start"> </div> -->
            <div class="content-time"><span>赛事开始时间：{{ item.competitionStartTime }}</span>
              <div class="flex items-center justify-end"> 
                <!-- <div class="flex items-center justify-end" style="line-height: 0;"><img src="@/assets/images/star.png" alt="" class="mr-1 acticon-img">{{ item.competitionCollectNum || 0 }}</div> -->
                <!-- <div class="flex items-center justify-end ml-4 leading-none" style="line-height: 0;"><img src="@/assets/images/share.png" alt="" class="mr-1 acticon-img">{{ item.competitionShareNum || 0 }}</div> -->
            </div>
            </div>
          </div>
        </div>

      </div>
    </template>
    <template v-else>
      <!-- 骨架屏，用于数据加载时显示 -->
      <el-skeleton 
        animated
        class="mt-12"
        v-if="pageLoading"
        :rows="10">
      </el-skeleton>
      <el-empty description="暂无数据" v-if="(pageLoading === false) && (!comData || comData.length === 0)"></el-empty>
    </template>
    <div class="load-more cursor-pointer hvr-grow-shadow" @click="loadMore" 
      v-if="((comData && comData.length) < pageTotal) && ((comData && comData.length))">
      <span>加载更多</span>
      <img style="width: 11px;margin-left: 5px;" src="@/assets/images/load-more.png" alt="">
    </div>
    <div class="no-more text-center text-gray-400 absolute" v-if="(comData && comData.length) && (comData && comData.length) >= pageTotal">没有更多了</div>
  </div>
</div>
</template>


<script setup>
import { visualizationPageMobile } from '@/api/visualization'
import { useDict } from '@/utils/dict'
const { competition_type,join_type } = useDict('competition_type','join_type')
import { useRouter } from 'vue-router'
const router = useRouter()
import { debounce } from 'lodash'

import modal from "@/plugins/modal";
const props = defineProps({
  // 数据
  info: {
    type: Object,
    default: {},
  }
})
const competitionTypeMap = computed(() => {
  const map = [
    {label: '全部', value: ''},
    ...competition_type.value
  ]
  return map
})
const joinTypeMap = computed(() => {
  const map = [
    {label: '全部', value: ''},
    ...join_type.value
  ]
  return map
})

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

// 检查状态名称映射
const checkStatusMap = [
  {label: '全部', value: '6,7,8'},
  {label: '未开始', value: '6'},
  {label: '进行中', value: '7'},
  {label: '已结束', value: '8'},
]
// 获取检查状态名称
const getCheckStatusName = (status) => {
  return checkStatusMap.find(item => item.value == status)?.label || '未知状态'
}

let comData = $ref([])
// 筛选
let query = reactive({
  competitionType: '',
  checkStatus: '6,7,8',
  competitionStartTime: '',
  competitionEndTime: '',
  feeStart: '',
  competitionName: '',
})
let detail = $ref({})
const initQuery = () => {
  pageTotal = 0
  pageLoading = null
  pageNum = 1
  comData = []
  getDsApi(detail)
}
const filterChange = (type) => {
  query.competitionType = type
  initQuery()
}
const filterStatusChange = (type) => {
  query.checkStatus = type
  initQuery()
}

// 使用防抖函数包装搜索方法，避免频繁请求
const handleCompetitionNameChange = debounce((type) => {
  initQuery()
}, 500)

// 处理日期选择器变化
const handleDateChange = () => {
  if(query.competitionStartTime && query.competitionEndTime){
    if(query.competitionStartTime > query.competitionEndTime){
      modal.msgWarning('赛事开始时间不能晚于赛事结束时间')
      return
    }
  }
  initQuery()
}
// API调用函数
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
  visualizationPageMobile(detailData.dataSourceUrl,'get',params).then(res => { 
    if (res.code === 200) {
     pageTotal = res.total || 0
     let resRows = res.rows || []
      if (comData.length >= pageTotal) {
        pageLoading = false
        // 列表已加载完毕
        return
      }else{
        pageNum = pageNum + 1
      }
      comData = [...comData, ...resRows]
    } else {
      comData = []
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
const loadMore = () => {
  getDsApi(detail)
}


</script>

<style lang="scss" scoped>
.model-page{
  overflow: hidden;
}
.container-custom-self{
  padding-top: 45px;
  padding-bottom:120px;
  position: relative;
}
.list-one-filter {
  width: 100%;
  height: 140px;
  background: #FFFFFF;
  box-shadow: 0px 1px 10px 1px rgba(0, 0, 0, 0.1);
  border-radius: 10px 10px 10px 10px;
  padding: 32px 30px;
  box-sizing: border-box;
  display: flex;
  justify-content: flex-start;
}

.fliter-left {
  max-width: 50%;
  height: 100%;
  font-weight: 400;
  font-size: 15px;
  color: #999999;
  line-height: 24px;
  text-align: left;
  font-style: normal;
  text-transform: none;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  flex-shrink: 0;
  margin-right: 20px;
  :deep(.el-date-editor){
    font-size: 15px;
    height: 40px !important;
  }
}

.list-one-filter{
  .fliter-right {
    width: 100%;
    margin-left: 5%;
    height: 100%;
    .ipt-width2{
      font-size: 15px;
      width: calc(60%) !important;
      height: 40px !important;
    }
  }
}


.filter-part {
  width: 0px !important;
  height: 15px;
  border: 1px solid #E4E4E4;
  margin: 0 20px;
}

.left-line {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.filter-part {
  width: 10px;
  height: 15px;
  font-weight: 400;
  font-size: 15px;
  color: #333333;
  line-height: 24px;
  text-align: left;
  font-style: normal;
  text-transform: none;
}

.filter-select {
  color: #3169F8 !important;

}

.fliter-title {
  font-size: 15px;
  color: #333333;
  flex-shrink: 0;
}
.filter-scroll{
    overflow-x: auto;
    width: 100%;
    display: flex;
}
.filter-option {
  margin-right: 20px;
  font-size: 15px;
  color: #999999;
  cursor: pointer;
  flex-shrink: 0;
  &:hover{
    color: #3169F8;
  }
}
:deep(.el-input__inner){
  font-size: 15px;
}

.filter-list {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  margin-top: 45px;
  gap: 30px;
  box-sizing: border-box;
}

.filter-list-item {
  display: flex;
  flex: 0 0 calc(50% - 15px);
  width: calc(50% - 15px);
  border-radius: 8px;
  box-sizing: border-box;
  font-size: 16px;
  color: #333;
  /* 确保在动态添加时保持一行两个的布局 */
  min-height: 150px;
  box-shadow: 0 2px 15px 0 rgba(0, 0, 0, 0.1);
}

.filter-list-img {
  width: 220px;
  height: 150px;
  border-radius: 8px 0px 8px 0px;
  flex-shrink: 0;
  position: relative;
}

.filter-status {
  position: absolute;
  top: 0px;
  left: 0px;
  padding: 2px 8px;
  font-size: 12px;
  color: #fff;
  border-radius: 6px 0px 8px 0px;
  font-weight: 400;
  font-size: 12px;
  color: #FFFFFF;
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


.content-title {
  padding-bottom: 13px;
  font-weight: bold;
  font-size: 18px;
  color: #333333;
  line-height: 25px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;

}

.filter-list-content {
  width: calc(100% - 220px);
  padding: 10px 20px;
  box-sizing: border-box;
}

.content-desc {
  font-weight: 400;
  font-size: 15px;
  color: #999999;
  width: 100%;
  overflow: hidden;
  @include ellipsis(2);
  margin-bottom: 13px;
}

.tur-tags{
  margin-top: 16px;
  margin-bottom: 12px;
  .tur-tags-design{
    width: 70px;
    height: 26px;
    line-height: 26px;
    background: #EBF0FF;
    border-radius: 4px;
    font-size: 12px;
    color: #3169F8;
    text-align: center;
    margin-right: 20px;
  }
  .tur-tags-single{
    width: 70px;
    height: 26px;
    line-height: 26px;
    background: #F2FFEB;
    border-radius: 4px;
    font-size: 12px;
    color: #51C512;
    text-align: center;
  }
}

.content-time {
  display: flex;
  justify-content: space-between;
  font-weight: 400;
  font-size: 14px;
  color: #999999;
  line-height: 24px;
  text-align: left;
  font-style: normal;
  text-transform: none;
}

.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 180px;
  height: 50px;
  background: #EFF4FF;
  border-radius: 10px 10px 10px 10px;
  margin: 045px auto;
  font-size: 14px;
  color: #3169F8;
}
.no-more{
  position: absolute;
  width: 100%;
  text-align: center;
  bottom: 20px;
  font-size: 14px;
}
.acticon-img{
  width: 12px;
}
</style>
