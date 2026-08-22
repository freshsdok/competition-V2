<template>
  <view class="page">
    <view v-if="order" class="content">
      <view class="status-card">
        <view>
          <text class="status-title">{{ statusLabel(order.payStatus) }}</text>
          <text class="status-desc">{{ statusDescription(order.payStatus) }}</text>
        </view>
        <text class="status-amount">¥{{ money(order.amount) }}</text>
      </view>

      <view class="card">
        <view class="card-title">基本信息</view>
        <view class="info-row"><text>订单号</text><text selectable>{{ order.id }}</text></view>
        <view class="info-row"><text>创建时间</text><text>{{ order.createTime || '-' }}</text></view>
        <view class="info-row"><text>订单类型</text><text>{{ order.orderType === 'refund' ? '退费订单' : '缴费订单' }}</text></view>
        <view class="info-row"><text>支付方式</text><text>{{ payMethodLabel(order.payMethod) }}</text></view>
        <view class="info-row"><text>开票状态</text><text>{{ invoiceLabel(order.invoiceStatus) }}</text></view>
      </view>



      <view v-if="canInvoice" class="footer-space"></view>
      <view v-if="canInvoice" class="footer">
        <button class="primary-btn" @click="goInvoice">申请开票</button>
      </view>
    </view>
    <view v-else-if="loading" class="empty">加载中...</view>
    <view v-else class="empty">订单不存在或无权访问</view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPersonalOrder } from '@/api/order'

const order = ref(null)
const loading = ref(true)
const statusMap = {
  pending: '待支付', paying: '支付中', paid: '已支付', approving: '审核中',
  approve_rejected: '审核驳回', failed: '支付失败', cancelled: '已取消',
  refunding: '退款中', refunded: '已退款', repay_refunded: '重缴已退款'
}

const teams = computed(() => {
  if (Array.isArray(order.value?.teamInfoLists)) return order.value.teamInfoLists
  if (Array.isArray(order.value?.teamInfoList)) return order.value.teamInfoList
  return []
})
const certInfo = computed(() => {
  const teamInfoLists = order.value?.teamInfoLists
  const value = teamInfoLists == null ? order.value?.teamInfoList : teamInfoLists
  if (value && !Array.isArray(value) && typeof value === 'object') return value
  if (typeof value === 'string') {
    try { return JSON.parse(value) } catch (_) { return {} }
  }
  return {}
})
const canInvoice = computed(() => order.value?.payStatus === 'paid'
  && order.value?.orderType !== 'refund' && String(order.value?.invoiceStatus) === '0')

onLoad(async options => {
  if (!options?.id) {
    loading.value = false
    return
  }
  try {
    const res = await getPersonalOrder(options.id)
    order.value = res?.data || null
  } finally {
    loading.value = false
  }
})

function statusLabel(value) { return statusMap[value] || value || '未知状态' }
function statusDescription(value) {
  if (value === 'paid') return '订单已完成支付，可查看详情或申请开票'
  if (value === 'pending') return '小程序暂不支持支付，请通过原有 PC 流程处理'
  if (value === 'approving') return '付款信息正在审核，请耐心等待'
  return '订单当前状态以系统记录为准'
}
function payMethodLabel(value) { return value === 'online' ? '在线支付' : value === 'offline' ? '线下转账' : value || '-' }
function invoiceLabel(value) { return String(value) === '1' ? '已开票' : String(value) === '2' ? '开票中' : '未开票' }
function money(value) { return Number(value || 0).toFixed(2) }
function competitionName(team) {
  return [team.competitionName, team.competitionTrackName, team.secondLevelName].filter(Boolean).join(' - ') || '-'
}
function memberList(team) { return Array.isArray(team?.playersList) ? team.playersList : [] }
function teacherList(team) { return Array.isArray(team?.instructorList) ? team.instructorList : [] }
function certNames(list) { return Array.isArray(list) ? list.map(item => item.certConfigName).filter(Boolean).join('、') || '-' : '-' }
function goInvoice() { uni.navigateTo({ url: `/pages-personal/invoice/select?orderId=${order.value.id}` }) }
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: 24rpx 28rpx 60rpx; background: #f5f7fb; box-sizing: border-box; }
.status-card { display: flex; align-items: center; justify-content: space-between; padding: 34rpx 30rpx; border-radius: 22rpx; color: #fff; background: linear-gradient(135deg, #3169f8, #5d8df9); box-shadow: 0 10rpx 28rpx rgba(49, 105, 248, .22); }
.status-title, .status-desc { display: block; }
.status-title { font-size: 34rpx; font-weight: 700; }
.status-desc { max-width: 430rpx; margin-top: 10rpx; color: rgba(255,255,255,.82); font-size: 22rpx; line-height: 1.45; }
.status-amount { font-size: 34rpx; font-weight: 700; }
.card { margin-top: 22rpx; padding: 26rpx; border-radius: 22rpx; background: #fff; box-shadow: 0 6rpx 20rpx rgba(24, 52, 110, .05); }
.card-title { margin-bottom: 14rpx; color: #263249; font-size: 29rpx; font-weight: 700; }
.info-row, .mini-row, .total-row { display: flex; justify-content: space-between; gap: 24rpx; padding: 15rpx 0; color: #7c8798; font-size: 24rpx; }
.info-row text:last-child, .mini-row text:last-child { color: #303b50; text-align: right; word-break: break-all; }
.team-block { padding: 22rpx 0; border-top: 1rpx solid #edf0f5; }
.team-name, .competition { display: block; }
.team-name { color: #263249; font-size: 27rpx; font-weight: 600; }
.competition { margin-top: 8rpx; color: #3169f8; font-size: 23rpx; line-height: 1.5; }
.members { display: flex; gap: 18rpx; padding-top: 12rpx; color: #525e72; font-size: 23rpx; line-height: 1.6; }
.member-label { flex: 0 0 110rpx; color: #8c96a7; }
.cert-block { padding-top: 12rpx; }
.sub-empty, .empty { padding: 100rpx 0; color: #99a2b2; text-align: center; font-size: 25rpx; }
.opinion { color: #5f6b7d; font-size: 24rpx; line-height: 1.7; }
.amount-card { margin-bottom: 10rpx; }
.total-row { margin-top: 10rpx; padding-top: 24rpx; border-top: 1rpx solid #edf0f5; color: #25314a; font-size: 29rpx; font-weight: 700; }
.total-row text:last-child { color: #e14c42; }
.footer-space { height: 120rpx; }
.footer { position: fixed; right: 0; bottom: 0; left: 0; padding: 18rpx 30rpx calc(18rpx + env(safe-area-inset-bottom)); background: #fff; box-shadow: 0 -5rpx 20rpx rgba(25, 46, 89, .08); }
button::after { border: none; }
.primary-btn { height: 76rpx; line-height: 76rpx; border-radius: 40rpx; color: #fff; background: #3169f8; font-size: 28rpx; }
</style>
