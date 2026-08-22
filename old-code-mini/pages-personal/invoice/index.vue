<template>
  <view class="page">
    <view class="summary">
      <view><text class="summary-title">开票记录</text><text class="summary-desc">仅展示当前登录账号的发票</text></view>
      <button class="apply-btn" @click="goSelect">申请开票</button>
    </view>

    <view v-for="item in invoices" :key="item.id" class="invoice-card">
      <view class="head">
        <view class="title-wrap">
          <text class="buyer">{{ item.buyerName || '发票' }}</text>
          <text class="invoice-no">{{ item.invoiceNum ? `发票号码 ${item.invoiceNum}` : `申请流水 ${item.invoiceSerialNum || '-'}` }}</text>
        </view>
        <text class="status" :class="statusClass(item.issuedStatus)">{{ issuedLabel(item.issuedStatus) }}</text>
      </view>
      <view class="amount">¥{{ money(item.amount) }}</view>
      <view class="row"><text>抬头类型</text><text>{{ item.invoiceClass === '1' ? '个人' : '企业' }}</text></view>
      <view class="row"><text>关联订单</text><text selectable>{{ item.orderId || '-' }}</text></view>
      <view class="row"><text>申请/开具时间</text><text>{{ item.issuedTime || item.createTime || '-' }}</text></view>
      <view v-if="item.failReason" class="fail-reason">{{ item.failReason }}</view>
      <view class="actions">
        <button v-if="String(item.issuedStatus) === '1'" class="plain-btn" @click="download(item)">查看发票</button>
        <button v-else class="plain-btn" :disabled="refreshingId === item.id" @click="refresh(item)">
          {{ refreshingId === item.id ? '更新中...' : '更新状态' }}
        </button>
      </view>
    </view>

    <view v-if="!loading && !invoices.length" class="empty">暂无开票记录</view>
    <view v-if="loading" class="empty">加载中...</view>
    <view v-else-if="finished && invoices.length" class="bottom-tip">没有更多了</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listPersonalInvoices, refreshInvoiceResult, getInvoicePdfUrl } from '@/api/invoice'
import { getToken } from '@/utils/auth'

const invoices = ref([])
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const finished = ref(false)
const refreshingId = ref(null)

onShow(() => load(true))
onPullDownRefresh(async () => {
  await load(true)
  uni.stopPullDownRefresh()
})
onReachBottom(() => load(false))

async function load(reset) {
  if (loading.value || (!reset && finished.value)) return
  if (reset) { pageNum.value = 1; finished.value = false }
  loading.value = true
  try {
    const res = await listPersonalInvoices({}, { pageNum: pageNum.value, pageSize })
    const rows = Array.isArray(res?.rows) ? res.rows : []
    invoices.value = reset ? rows : invoices.value.concat(rows)
    finished.value = invoices.value.length >= Number(res?.total || 0) || rows.length < pageSize
    if (!finished.value) pageNum.value += 1
  } finally {
    loading.value = false
  }
}

function issuedLabel(value) {
  return String(value) === '1' ? '开票完成' : String(value) === '2' ? '开票失败' : String(value) === '3' ? '签章处理中' : '开票中'
}
function statusClass(value) { return String(value) === '1' ? 'success' : String(value) === '2' ? 'failed' : '' }
function money(value) { return Number(value || 0).toFixed(2) }
function goSelect() { uni.navigateTo({ url: '/pages-personal/invoice/select' }) }
async function refresh(item) {
  if (refreshingId.value) return
  refreshingId.value = item.id
  try {
    await refreshInvoiceResult({
      serialNos: item.invoiceSerialNum ? [item.invoiceSerialNum] : [],
      orderNos: item.orderId ? [item.orderId] : [],
      isOfferInvoiceDetail: 0
    })
    uni.showToast({ title: '状态已更新', icon: 'success' })
    await load(true)
  } finally {
    refreshingId.value = null
  }
}
function download(item) {
  uni.showLoading({ title: '发票加载中' })
  uni.downloadFile({
    url: getInvoicePdfUrl(item.id),
    header: { Authorization: `Bearer ${getToken()}`, tjPlatformType: 'miniProgram' },
    success: res => {
      if (res.statusCode !== 200) {
        uni.showToast({ title: '发票文件暂不可用', icon: 'none' })
        return
      }
      uni.openDocument({ filePath: res.tempFilePath, fileType: 'pdf', showMenu: true })
    },
    fail: () => uni.showToast({ title: '发票加载失败', icon: 'none' }),
    complete: () => uni.hideLoading()
  })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: 24rpx 28rpx 60rpx; background: #f5f7fb; box-sizing: border-box; }
.summary { display: flex; align-items: center; justify-content: space-between; padding: 28rpx; border-radius: 22rpx; color: #fff; background: linear-gradient(135deg, #3169f8, #5b8ff9); }
.summary-title, .summary-desc { display: block; }
.summary-title { font-size: 32rpx; font-weight: 700; }
.summary-desc { margin-top: 8rpx; color: rgba(255,255,255,.8); font-size: 22rpx; }
button::after { border: none; }
.apply-btn { height: 64rpx; line-height: 64rpx; margin: 0; padding: 0 24rpx; border-radius: 34rpx; color: #3169f8; background: #fff; font-size: 24rpx; }
.invoice-card { margin-top: 22rpx; padding: 26rpx; border-radius: 22rpx; background: #fff; box-shadow: 0 6rpx 20rpx rgba(24, 52, 110, .05); }
.head { display: flex; justify-content: space-between; gap: 20rpx; }
.buyer, .invoice-no { display: block; }
.buyer { color: #27334a; font-size: 28rpx; font-weight: 700; }
.invoice-no { max-width: 470rpx; margin-top: 8rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #929cad; font-size: 21rpx; }
.status { align-self: flex-start; padding: 6rpx 15rpx; border-radius: 20rpx; color: #b46b1b; background: #fff1de; font-size: 21rpx; }
.status.success { color: #168657; background: #e5f7ef; }
.status.failed { color: #c74343; background: #ffeaea; }
.amount { padding: 24rpx 0 18rpx; color: #e14c42; font-size: 38rpx; font-weight: 700; }
.row { display: flex; justify-content: space-between; gap: 24rpx; padding: 10rpx 0; color: #8791a2; font-size: 23rpx; }
.row text:last-child { max-width: 450rpx; color: #3b465a; text-align: right; word-break: break-all; }
.fail-reason { margin-top: 14rpx; padding: 16rpx; border-radius: 12rpx; color: #a33d3d; background: #fff1f1; font-size: 22rpx; }
.actions { display: flex; justify-content: flex-end; padding-top: 20rpx; }
.plain-btn { height: 60rpx; line-height: 58rpx; margin: 0; padding: 0 25rpx; border: 1rpx solid #3169f8; border-radius: 31rpx; color: #3169f8; background: #fff; font-size: 23rpx; }
.empty { padding: 120rpx 0; color: #99a2b2; text-align: center; font-size: 25rpx; }
.bottom-tip { padding: 26rpx 0; color: #a0a8b6; text-align: center; font-size: 23rpx; }
</style>
