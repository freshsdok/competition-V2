<template>
<view class="view-page">
  <view class="container">
    <!-- Banner轮播图 -->
    <swiper class="banner" indicator-dots autoplay circular indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#ffffff">
      <swiper-item v-for="(item, index) in bannerList" :key="index" >
        <image class="banner-img" :src="item.bannerUrl" mode="widthFix" />
      </swiper-item>
    </swiper>

    <!-- 公告通知 -->
    <view class="notice-section">
      <view class="section-header">
        <text class="section-title">公告通知</text>
      </view>
      
      <view class="notice-list">
        <view class="notice-item" v-for="(item, index) in noticeList" :key="index" @click="goToDetail(item)">
          <image class="notice-img" :src="item.noticeImage" mode="aspectFill" />
          <view class="notice-content">
            <text class="notice-title">{{ item.noticeTitle }}</text>
            <view class="notice-footer">
              <text class="notice-date">{{ formatDate(item.publishTime) }}</text>
              <text class="view-more">查看更多</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</view>
  <custom-tabbar />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBannerList, getNoticeList } from '@/api/home'
import { formatDate } from '@/utils/date.js'

const bannerList = ref([])
const noticeList = ref([])

// 获取轮播图数据
async function fetchBannerList() {
  try {
    const res = await getBannerList({
      bannerModule: 'home',
      number: 20
    })
    bannerList.value = res.data || []
  } catch (e) {
    console.error('获取轮播图失败', e)
  }
}

// 获取公告列表
async function fetchNoticeList() {
  try {
    const res = await getNoticeList({
      pageNum: 1,
      pageSize: 20,
	  noticeStatus:6
    })
    noticeList.value = res.rows || []
  } catch (e) {
    console.error('获取公告失败', e)
  }
}

// 轮播图点击 - 预览图片
function onBannerClick(item) {
  uni.previewImage({
    urls: [item.bannerUrl],
    success: () => {
      console.log('预览图片成功')
    },
    fail: (err) => {
      console.error('预览图片失败', err)
    }
  })
}

// 进入公告详情
function goToDetail(item) {
  uni.navigateTo({
    url: `/pages/notice/detail?id=${item.noticeId}`
  })
}

// 更多公告
function goToMoreNotice() {
  uni.navigateTo({
    url: '/pages/notice/list'
  })
}

onMounted(() => {
  fetchBannerList()
  fetchNoticeList()
})
</script>

<style lang="scss" scoped>
.view-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 220rpx;
}
.container {
  background: linear-gradient(180deg, #3169F8 0%, #ffffff 500rpx);
  padding: 20rpx;
  min-height: calc(100vh - 230rpx);
}

/* Banner轮播图 */
.banner {
  width: 100%;
  height: 266rpx;
  border-radius: 16rpx;
  overflow: hidden;
  margin: 10rpx 0 30rpx;
}

.banner-img {
  width: 100%;
  height: 100%;
  border-radius: 16rpx;
}

/* 公告通知区域 */
.notice-section {
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.more {
  font-size: 26rpx;
  color: #999;
}

.notice-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
}

.notice-item {
  overflow: hidden;
  width: calc(50% - 20rpx);
  margin-right: 36rpx;

  &:nth-child(2n) {
    margin-right: 0;
  }
}

.notice-img {
  width: 100%;
  height: 180rpx;
  border-radius: 12rpx;
}

.notice-content {
  padding: 16rpx;
}

.notice-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12rpx;
  min-height: 80rpx;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notice-date {
  font-size: 24rpx;
  color: #999;
}

.view-more {
  font-size: 24rpx;
  color: $theme-color;
}

/* 模拟测试按钮 */
.mock-section {
  margin-top: 40rpx;
}

.mock-btns {
  display: flex;
  gap: 20rpx;
}

.mock-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  border-radius: 40rpx;
  border: none;
  color: #ffffff;

  &::after {
    border: none;
  }

  &.success {
    background-color: #52c41a;
  }

  &.fail {
    background-color: #f5222d;
  }
}
</style>
