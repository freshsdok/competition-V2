<template>
  <view class="page">
    <view class="search-card">
      <view class="search-row">
        <input v-model.trim="keyword" class="search-input" placeholder="搜索订单号" confirm-type="search" @confirm="search" />
        <button class="search-btn" @click="search">搜索</button>
      </view>
      <scroll-view scroll-x class="tabs" :show-scrollbar="false">
        <view class="tabs-inner">
          <view v-for="tab in statusTabs" :key="tab.value" class="tab" :class="{ active: status === tab.value }" @click="changeStatus(tab.value)">
            {{ tab.label }}<text v-if="statusCount(tab.value) > 0">({{ statusCount(tab.value) }})</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="notice">小程序暂不提供支付功能；已支付且未开票的订单可申请开票。</view>

    <view v-for="item in orders" :key="item.id" class="order-card">
      <view class="order-head">
        <view>
          <text class="order-no">订单号 {{ item.id }}</text>
          <text class="time">{{ item.createTime || '-' }}</text>
        </view>
        <view class="tags">
          <text class="tag" :class="statusClass(item.payStatus)">{{ statusLabel(item.payStatus) }}</text>
          <text v-if="item.orderType !== 'refund'" class="tag invoice-tag">{{ invoiceLabel(item.invoiceStatus) }}</text>
        </view>
      </view>

      <view class="product" @click="goDetail(item.id)">
        <view class="product-main">
          <text class="product-name">{{ item.commodityName || commodityLabel(item.commodityType) }}</text>
          <text class="product-sub">{{ productSummary(item) }}</text>
        </view>
        <text class="amount">¥{{ money(item.amount) }}</text>
      </view>

      <view class="order-actions">
        <button v-if="canCancel(item)" class="plain-btn" :disabled="actionId === item.id" @click="confirmCancel(item)">取消订单</button>
        <button class="plain-btn" @click="goDetail(item.id)">查看详情</button>
        <button v-if="canInvoice(item)" class="primary-btn" @click="goInvoice(item.id)">申请开票</button>
      </view>
    </view>

    <view v-if="!loading && !orders.length" class="empty">暂无订单</view>
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else-if="finished && orders.length" class="bottom-tip">没有更多了</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import {
  listPersonalOrders,
  getPersonalOrderStatusCount,
  cancelPersonalOrder,
  cancelPersonalRepaymentOrder
} from '@/api/order'

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待支付', value: 'pending' },
  { label: '已支付', value: 'paid' },
  { label: '审核中', value: 'approving' },
  { label: '已取消', value: 'cancelled' },
  { label: '已退款', value: 'refunded' }
]
const statusMap = {
  pending: '待支付', paying: '支付中', paid: '已支付', approving: '审核中',
  approve_rejected: '审核驳回', failed: '支付失败', cancelled: '已取消',
  refunding: '退款中', refunded: '已退款', repay_refunded: '重缴已退款'
}

const orders = ref([])
const counts = ref({})
const keyword = ref('')
const status = ref('')
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const finished = ref(false)
const actionId = ref(null)

onShow(() => load(true))
onPullDownRefresh(async () => {
  await load(true)
  uni.stopPullDownRefresh()
})
onReachBottom(() => load(false))

async function load(reset) {
  if (loading.value || (!reset && finished.value)) return
  if (reset) {
    pageNum.value = 1
    finished.value = false
  }
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize,
      idStr: keyword.value || undefined,
      payStatus: status.value || undefined
    }
    const [listRes, countRes] = await Promise.all([
      listPersonalOrders(params),
      reset ? getPersonalOrderStatusCount() : Promise.resolve(null)
    ])
    const rows = Array.isArray(listRes?.rows) ? listRes.rows : []
    orders.value = reset ? rows : orders.value.concat(rows)
    const total = Number(listRes?.total || 0)
    finished.value = orders.value.length >= total || rows.length < pageSize
    if (!finished.value) pageNum.value += 1
    if (countRes?.data) counts.value = countRes.data
  } finally {
    loading.value = false
  }
}

function search() { load(true) }
function changeStatus(value) {
  if (status.value === value) return
  status.value = value
  load(true)
}
function statusCount(value) {
  return Number(value ? counts.value?.[value] : counts.value?.total) || 0
}
function statusLabel(value) { return statusMap[value] || value || '未知状态' }
function statusClass(value) {
  if (value === 'paid') return 'success'
  if (['cancelled', 'failed', 'approve_rejected'].includes(value)) return 'muted'
  if (['refunding', 'approving', 'paying'].includes(value)) return 'warning'
  return 'primary'
}
function invoiceLabel(value) {
  return String(value) === '1' ? '已开票' : String(value) === '2' ? '开票中' : '未开票'
}
function commodityLabel(value) { return value === 'cert' ? '赛证互通' : '赛事报名' }
function money(value) { return Number(value || 0).toFixed(2) }
function teamList(item) {
  if (Array.isArray(item?.teamInfoLists)) return item.teamInfoLists
  if (Array.isArray(item?.teamInfoList)) return item.teamInfoList
  return []
}
function productSummary(item) {
  if (item.commodityType === 'cert') return '赛证互通证书服务'
  const teams = teamList(item)
  if (!teams.length) return '赛事报名订单'
  return teams.map(team => team.teamName || team.teamCode).filter(Boolean).join('、')
}
function canCancel(item) { return item.payStatus === 'pending' }
function canInvoice(item) {
  return item.payStatus === 'paid' && item.orderType !== 'refund' && String(item.invoiceStatus) === '0'
}
function goDetail(id) { uni.navigateTo({ url: `/pages-personal/order/detail?id=${id}` }) }
function goInvoice(id) { uni.navigateTo({ url: `/pages-personal/invoice/select?orderId=${id}` }) }
function confirmCancel(item) {
  uni.showModal({
    title: '取消订单',
    content: '取消后该订单将无法继续操作，是否确认？',
    confirmColor: '#3169F8',
    success: async result => {
      if (!result.confirm || actionId.value) return
      actionId.value = item.id
      try {
        const fn = item.changeType === 'repayment' ? cancelPersonalRepaymentOrder : cancelPersonalOrder
        await fn(item.id)
        uni.showToast({ title: '订单已取消', icon: 'success' })
        await load(true)
      } finally {
        actionId.value = null
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: 24rpx 28rpx 60rpx; background: #f5f7fb; box-sizing: border-box; }
.search-card, .order-card { background: #fff; border-radius: 22rpx; box-shadow: 0 6rpx 22rpx rgba(24, 52, 110, .06); }
.search-card { padding: 24rpx 22rpx 0; }
.search-row { display: flex; gap: 16rpx; }
.search-input { flex: 1; height: 70rpx; padding: 0 24rpx; border-radius: 36rpx; background: #f3f6fb; font-size: 26rpx; }
button::after { border: none; }
.search-btn { width: 120rpx; height: 70rpx; line-height: 70rpx; margin: 0; padding: 0; border-radius: 35rpx; background: #3169f8; color: #fff; font-size: 26rpx; }
.tabs { width: 100%; margin-top: 20rpx; white-space: nowrap; }
.tabs-inner { display: inline-flex; height: 78rpx; gap: 34rpx; }
.tab { height: 75rpx; line-height: 75rpx; color: #667085; font-size: 25rpx; border-bottom: 5rpx solid transparent; }
.tab.active { color: #3169f8; font-weight: 600; border-color: #3169f8; }
.notice { margin: 22rpx 4rpx; padding: 18rpx 22rpx; border-radius: 14rpx; background: #fff5e9; color: #9a5a13; font-size: 23rpx; line-height: 1.5; }
.order-card { margin-bottom: 22rpx; padding: 24rpx; }
.order-head { display: flex; justify-content: space-between; gap: 16rpx; padding-bottom: 20rpx; border-bottom: 1rpx solid #edf0f5; }
.order-no, .time { display: block; }
.order-no { color: #27334a; font-size: 25rpx; font-weight: 600; }
.time { margin-top: 9rpx; color: #98a1b2; font-size: 21rpx; }
.tags { max-width: 240rpx; text-align: right; }
.tag { display: inline-block; margin: 0 0 8rpx 8rpx; padding: 5rpx 13rpx; border-radius: 18rpx; color: #3169f8; background: #eaf0ff; font-size: 20rpx; }
.tag.success { color: #168657; background: #e5f7ef; }
.tag.warning { color: #bf711e; background: #fff1de; }
.tag.muted { color: #7b8495; background: #eef0f4; }
.invoice-tag { color: #7157ba; background: #f0ebff; }
.product { display: flex; align-items: center; padding: 24rpx 0; }
.product-main { flex: 1; min-width: 0; }
.product-name, .product-sub { display: block; }
.product-name { color: #25314a; font-size: 28rpx; font-weight: 600; }
.product-sub { margin-top: 10rpx; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; color: #7d8798; font-size: 23rpx; }
.amount { margin-left: 20rpx; color: #e14c42; font-size: 31rpx; font-weight: 700; }
.order-actions { display: flex; justify-content: flex-end; gap: 14rpx; padding-top: 18rpx; border-top: 1rpx solid #edf0f5; }
.plain-btn, .primary-btn { height: 60rpx; line-height: 58rpx; margin: 0; padding: 0 24rpx; border-radius: 31rpx; font-size: 23rpx; }
.plain-btn { color: #4e5c72; background: #fff; border: 1rpx solid #d9deea; }
.primary-btn { color: #fff; background: #3169f8; border: 1rpx solid #3169f8; }
.empty, .loading, .bottom-tip { padding: 100rpx 0; color: #9aa3b2; text-align: center; font-size: 25rpx; }
.bottom-tip { padding: 24rpx 0; }
</style>
