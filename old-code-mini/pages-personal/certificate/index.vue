<template>
  <view class="page">
    <view class="search-card">
      <input v-model.trim="keyword" class="search-input" placeholder="搜索证书名称、编号、选手或机构" confirm-type="search" @confirm="search" />
      <button class="search-btn" @click="search">搜索</button>
    </view>

    <view v-for="item in certificates" :key="item.id || item.certCode" class="cert-card">
      <view class="cert-mark">证</view>
      <view class="cert-main">
        <view class="head">
          <text class="cert-name">{{ item.certName || '赛事证书' }}</text>
          <text v-if="item.certStatus != null" class="status">{{ statusLabel(item.certStatus) }}</text>
        </view>
        <view class="row"><text>证书编号</text><text selectable>{{ item.certCode || '-' }}</text></view>
        <view class="row"><text>参赛选手</text><text>{{ item.player || '-' }}</text></view>
        <view class="row"><text>指导教师</text><text>{{ item.guideTeacher || '-' }}</text></view>
        <view class="row"><text>颁发机构</text><text>{{ item.orgName || '-' }}</text></view>
        <view v-if="item.issuanceDate" class="row"><text>颁发时间</text><text>{{ item.issuanceDate }}</text></view>
        <button v-if="item.certUrl" class="link-btn" @click="copyUrl(item.certUrl)">复制证书查询地址</button>
      </view>
    </view>

    <view v-if="!loading && !certificates.length" class="empty">暂无证书信息</view>
    <view v-if="loading" class="empty">加载中...</view>
    <view v-else-if="finished && certificates.length" class="bottom-tip">没有更多了</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listPersonalCertificates } from '@/api/certificate'

const certificates = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const finished = ref(false)

onShow(() => load(true))
onPullDownRefresh(async () => { await load(true); uni.stopPullDownRefresh() })
onReachBottom(() => load(false))

async function load(reset) {
  if (loading.value || (!reset && finished.value)) return
  if (reset) { pageNum.value = 1; finished.value = false }
  loading.value = true
  try {
    const res = await listPersonalCertificates({ pageNum: pageNum.value, pageSize, keyWords: keyword.value || undefined })
    const rows = Array.isArray(res?.rows) ? res.rows : []
    certificates.value = reset ? rows : certificates.value.concat(rows)
    finished.value = certificates.value.length >= Number(res?.total || 0) || rows.length < pageSize
    if (!finished.value) pageNum.value += 1
  } finally { loading.value = false }
}

function search() { load(true) }
function statusLabel(value) {
  const map = { 0: '有效', 1: '已撤销', valid: '有效', revoked: '已撤销' }
  return map[value] || value
}
function copyUrl(url) {
  uni.setClipboardData({
    data: url,
    success: () => uni.showToast({ title: '查询地址已复制', icon: 'success' })
  })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: 24rpx 28rpx 60rpx; background: #f5f7fb; box-sizing: border-box; }
.search-card { display: flex; gap: 16rpx; padding: 24rpx; border-radius: 22rpx; background: #fff; box-shadow: 0 6rpx 20rpx rgba(24, 52, 110, .05); }
.search-input { flex: 1; height: 70rpx; padding: 0 22rpx; border-radius: 36rpx; background: #f3f6fb; font-size: 24rpx; }
button::after { border: none; }
.search-btn { width: 116rpx; height: 70rpx; line-height: 70rpx; margin: 0; padding: 0; border-radius: 36rpx; color: #fff; background: #3169f8; font-size: 25rpx; }
.cert-card { position: relative; display: flex; gap: 22rpx; margin-top: 22rpx; padding: 26rpx; overflow: hidden; border-radius: 22rpx; background: #fff; box-shadow: 0 6rpx 20rpx rgba(24, 52, 110, .05); }
.cert-card::after { content: ''; position: absolute; top: 0; right: 0; width: 120rpx; height: 120rpx; border-radius: 0 0 0 120rpx; background: #f0ebff; opacity: .55; }
.cert-mark { flex: 0 0 70rpx; width: 70rpx; height: 70rpx; line-height: 70rpx; border-radius: 20rpx; color: #7b55dc; background: #efe9ff; text-align: center; font-size: 29rpx; font-weight: 700; }
.cert-main { flex: 1; min-width: 0; z-index: 1; }
.head { display: flex; justify-content: space-between; gap: 18rpx; margin-bottom: 14rpx; }
.cert-name { color: #27334a; font-size: 28rpx; font-weight: 700; line-height: 1.45; }
.status { flex: 0 0 auto; align-self: flex-start; padding: 5rpx 13rpx; border-radius: 18rpx; color: #168657; background: #e5f7ef; font-size: 20rpx; }
.row { display: flex; justify-content: space-between; gap: 18rpx; padding: 9rpx 0; color: #8b95a6; font-size: 22rpx; }
.row text:last-child { max-width: 370rpx; color: #445166; text-align: right; word-break: break-all; }
.link-btn { height: 60rpx; line-height: 58rpx; margin: 20rpx 0 0 auto; padding: 0 24rpx; border: 1rpx solid #7b55dc; border-radius: 31rpx; color: #7b55dc; background: #fff; font-size: 22rpx; }
.empty { padding: 140rpx 0; color: #99a2b2; text-align: center; font-size: 25rpx; }
.bottom-tip { padding: 26rpx 0; color: #a0a8b6; text-align: center; font-size: 23rpx; }
</style>
