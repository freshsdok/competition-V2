<template>
  <view class="page">
    <view class="tip">仅可选择已支付且未开票的项目；同一收款方将合并开票。</view>

    <view v-if="competitionGroups.length" class="section">
      <view class="section-title">赛事报名</view>
      <view v-for="team in competitionGroups" :key="`${team.orderId}-${team.teamCode}`" class="group-card">
        <view class="group-head">
          <view>
            <text class="group-name">{{ team.teamName || '参赛团队' }}</text>
            <text class="competition">{{ competitionName(team) }}</text>
          </view>
          <text class="team-code">{{ team.teamCode }}</text>
        </view>
        <view v-for="member in team.userInfo || []" :key="member.memberId" class="choice" @click="toggle(memberKey(team, member), { orderId: team.orderId, userId: member.memberId })">
          <view class="check" :class="{ checked: isSelected(memberKey(team, member)) }">{{ isSelected(memberKey(team, member)) ? '✓' : '' }}</view>
          <view class="choice-main">
            <text class="choice-name">{{ member.userName || '-' }}</text>
            <text class="choice-sub">{{ member.competitionRoleName || '参赛队员' }} · {{ maskId(member.idCard) }}</text>
          </view>
          <text class="fee">¥{{ money(member.fee) }}</text>
        </view>
      </view>
    </view>

    <view v-if="certificates.length" class="section">
      <view class="section-title">赛证互通</view>
      <view v-for="cert in certificates" :key="cert.orderId" class="cert-card" @click="toggle(certKey(cert), { orderId: cert.orderId, userId: cert.userId })">
        <view class="check" :class="{ checked: isSelected(certKey(cert)) }">{{ isSelected(certKey(cert)) ? '✓' : '' }}</view>
        <view class="choice-main">
          <text class="choice-name">{{ cert.ruleName || '赛证互通证书' }}</text>
          <text class="choice-sub">{{ cert.userName || '-' }} · {{ certSummary(cert) }}</text>
        </view>
        <text class="fee">¥{{ money(cert.repayAmount) }}</text>
      </view>
    </view>

    <view v-if="!loading && !competitionGroups.length && !certificates.length" class="empty">暂无可开票项目</view>
    <view v-if="loading" class="empty">加载中...</view>

    <view class="footer-space"></view>
    <view class="footer">
      <view><text class="selected-count">已选 {{ selectedKeys.length }} 项</text><text class="select-all" @click="toggleAll">{{ allSelected ? '取消全选' : '全选' }}</text></view>
      <button class="next-btn" :disabled="!selectedKeys.length" @click="next">下一步</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { listInvoiceCandidates } from '@/api/invoice'

const competitionGroups = ref([])
const certificates = ref([])
const selectedKeys = ref([])
const selectedPayload = ref({})
const loading = ref(true)
const filterOrderId = ref('')

const allChoices = computed(() => {
  const values = []
  competitionGroups.value.forEach(team => (team.userInfo || []).forEach(member => {
    values.push({ key: memberKey(team, member), payload: { orderId: team.orderId, userId: member.memberId } })
  }))
  certificates.value.forEach(cert => values.push({ key: certKey(cert), payload: { orderId: cert.orderId, userId: cert.userId } }))
  return values
})
const allSelected = computed(() => allChoices.value.length > 0 && selectedKeys.value.length === allChoices.value.length)

onLoad(async options => {
  filterOrderId.value = options?.orderId ? String(options.orderId) : ''
  try {
    const res = await listInvoiceCandidates({})
    const data = res?.data || {}
    const competition = Array.isArray(data.competition) ? data.competition : []
    const cert = Array.isArray(data.cert) ? data.cert : []
    competitionGroups.value = filterOrderId.value ? competition.filter(item => String(item.orderId) === filterOrderId.value) : competition
    certificates.value = filterOrderId.value ? cert.filter(item => String(item.orderId) === filterOrderId.value) : cert
  } finally {
    loading.value = false
  }
})

function memberKey(team, member) { return `competition-${team.orderId}-${member.memberId}` }
function certKey(cert) { return `cert-${cert.orderId}-${cert.userId}` }
function isSelected(key) { return selectedKeys.value.includes(key) }
function toggle(key, payload) {
  if (isSelected(key)) {
    selectedKeys.value = selectedKeys.value.filter(item => item !== key)
    const next = { ...selectedPayload.value }
    delete next[key]
    selectedPayload.value = next
  } else {
    selectedKeys.value = selectedKeys.value.concat(key)
    selectedPayload.value = { ...selectedPayload.value, [key]: payload }
  }
}
function toggleAll() {
  if (allSelected.value) {
    selectedKeys.value = []
    selectedPayload.value = {}
    return
  }
  const payload = {}
  allChoices.value.forEach(item => { payload[item.key] = item.payload })
  selectedKeys.value = allChoices.value.map(item => item.key)
  selectedPayload.value = payload
}
function next() {
  if (!selectedKeys.value.length) return
  const data = selectedKeys.value.map(key => selectedPayload.value[key])
  uni.setStorageSync('personal_invoice_selection', data)
  uni.navigateTo({ url: '/pages-personal/invoice/apply' })
}
function competitionName(team) { return [team.competitionName, team.competitionTrackName, team.secondLevelName].filter(Boolean).join(' - ') }
function maskId(value) {
  if (!value) return '证件号未填写'
  if (value.length < 8) return value
  return `${value.slice(0, 3)}******${value.slice(-4)}`
}
function money(value) { return Number(value || 0).toFixed(2) }
function certSummary(cert) {
  const targets = Array.isArray(cert.targetCertList) ? cert.targetCertList.map(item => item.certConfigName).filter(Boolean) : []
  return targets.length ? `目标：${targets.join('、')}` : '证书互通申请'
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: 24rpx 28rpx 0; background: #f5f7fb; box-sizing: border-box; }
.tip { padding: 19rpx 22rpx; border-radius: 14rpx; color: #855314; background: #fff2df; font-size: 23rpx; line-height: 1.5; }
.section { margin-top: 28rpx; }
.section-title { margin: 0 4rpx 16rpx; color: #25314a; font-size: 30rpx; font-weight: 700; }
.group-card, .cert-card { margin-bottom: 20rpx; border-radius: 20rpx; background: #fff; box-shadow: 0 6rpx 20rpx rgba(24, 52, 110, .05); }
.group-card { padding: 0 24rpx; }
.group-head { display: flex; justify-content: space-between; gap: 20rpx; padding: 24rpx 0 20rpx; border-bottom: 1rpx solid #edf0f5; }
.group-name, .competition { display: block; }
.group-name { color: #27334a; font-size: 27rpx; font-weight: 700; }
.competition { margin-top: 8rpx; color: #667388; font-size: 22rpx; line-height: 1.45; }
.team-code { flex: 0 0 auto; color: #3169f8; font-size: 21rpx; }
.choice, .cert-card { display: flex; align-items: center; gap: 18rpx; }
.choice { min-height: 105rpx; border-bottom: 1rpx solid #f0f2f6; }
.choice:last-child { border-bottom: 0; }
.cert-card { padding: 24rpx; }
.check { flex: 0 0 36rpx; width: 36rpx; height: 36rpx; line-height: 36rpx; border: 2rpx solid #cfd5e1; border-radius: 50%; color: #fff; text-align: center; font-size: 22rpx; }
.check.checked { border-color: #3169f8; background: #3169f8; }
.choice-main { flex: 1; min-width: 0; }
.choice-name, .choice-sub { display: block; }
.choice-name { color: #2c374c; font-size: 25rpx; font-weight: 600; }
.choice-sub { margin-top: 7rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #8b95a6; font-size: 21rpx; }
.fee { color: #e05248; font-size: 26rpx; font-weight: 600; }
.empty { padding: 150rpx 0; color: #99a2b2; text-align: center; font-size: 25rpx; }
.footer-space { height: 125rpx; }
.footer { position: fixed; right: 0; bottom: 0; left: 0; display: flex; align-items: center; justify-content: space-between; padding: 18rpx 30rpx calc(18rpx + env(safe-area-inset-bottom)); background: #fff; box-shadow: 0 -5rpx 20rpx rgba(25, 46, 89, .08); }
.selected-count { color: #3d485c; font-size: 25rpx; }
.select-all { margin-left: 24rpx; color: #3169f8; font-size: 23rpx; }
button::after { border: none; }
.next-btn { width: 220rpx; height: 76rpx; line-height: 76rpx; margin: 0; border-radius: 40rpx; color: #fff; background: #3169f8; font-size: 28rpx; }
.next-btn[disabled] { color: #fff; background: #aebfe9; }
</style>
