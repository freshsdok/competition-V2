<template>
  <view class="container">
    <view class="title">{{ detail.noticeTitle }}</view>
    <view class="meta">
      <view class="date">发布时间：{{ formatDate(detail.publishTime) }}</view>
      <view class="author" v-if="detail.noticeAuthor">发布人：{{ detail.noticeAuthor }}</view>
    </view>
    <view class="ql-container ql-snow">
      <rich-text class="rich-content ql-editor" :nodes="detail.noticeContent" ></rich-text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getNoticeDetail } from '@/api/home'
import { formatDate } from '@/utils/date.js'

const detail = ref({})

onLoad((options) => {
  const id = options.id
  if (id) {
    fetchDetail(id)
  }
})

async function fetchDetail(id) {
  try {
    const res = await getNoticeDetail(id)
    detail.value = res.data || {}
    // 设置页面标题
    uni.setNavigationBarTitle({
      title: detail.value.title || '公告详情'
    })
  } catch (e) {
    console.error('获取详情失败', e)
  }
}
</script>
<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #ffffff;
  padding: 30rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  line-height: 1.5;
}

.meta {
  // display: flex;
  // gap: 20rpx;
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.date, .author {
  font-size: 26rpx;
  color: #999;
}
.date {
  margin-bottom: 10rpx;
}

.content {
  font-size: 30rpx;
  color: #333;
  line-height: 1.8;
}
</style>
