<template>
  <view class="page">
    <view class="filter-card">
      <view class="search-row">
        <input v-model.trim="keyword" class="search-input" placeholder="赛事、团队、成员或团队编号" confirm-type="search" @confirm="search" />
        <button class="search-btn" @click="search">搜索</button>
      </view>
      <view class="date-row">
        <picker mode="date" :value="startDate" @change="event => { startDate = event.detail.value; search() }">
          <view class="date-picker">{{ startDate || '开始日期' }}</view>
        </picker>
        <text>至</text>
        <picker mode="date" :value="endDate" @change="event => { endDate = event.detail.value; search() }">
          <view class="date-picker">{{ endDate || '结束日期' }}</view>
        </picker>
        <text v-if="startDate || endDate" class="clear" @click="clearDates">清除</text>
      </view>
    </view>

    <view v-for="item in teams" :key="item.teamCode" class="team-card" @click="goDetail(item.teamCode)">
      <view class="head">
        <view class="head-main">
          <text class="team-name">{{ item.teamName || '未命名团队' }}</text>
          <text class="competition">{{ competitionName(item) }}</text>
        </view>
        <text v-if="item.operationStatus" class="status">{{ statusLabel(item.operationStatus) }}</text>
      </view>
      <view class="meta-row"><text>团队编号</text><text selectable>{{ item.teamCode || '-' }}</text></view>
      <view class="meta-row"><text>报名时间</text><text>{{ item.registrationTime || '-' }}</text></view>
      <view class="members">
        <view v-for="(member, index) in visibleMembers(item)" :key="member.memberId || index" class="member">
          <view class="avatar">{{ (member.userName || '队').slice(0, 1) }}</view>
          <view><text class="member-name">{{ member.userName || '-' }}</text><text class="role">{{ member.competitionRoleName || '队员' }}</text></view>
        </view>
        <text v-if="memberCount(item) > 3" class="more">等 {{ memberCount(item) }} 人</text>
      </view>
      <view class="detail-link">查看团队详情 <text>›</text></view>
    </view>

    <view v-if="!loading && !teams.length" class="empty">暂无团队</view>
    <view v-if="loading" class="empty">加载中...</view>
    <view v-else-if="finished && teams.length" class="bottom-tip">没有更多了</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listPersonalTeams } from '@/api/team'

const teams = ref([])
const keyword = ref('')
const startDate = ref('')
const endDate = ref('')
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const finished = ref(false)
const statusMap = { retired: '已退赛', change: '信息变更中', repayment: '退费重缴中' }

onShow(() => load(true))
onPullDownRefresh(async () => { await load(true); uni.stopPullDownRefresh() })
onReachBottom(() => load(false))

async function load(reset) {
  if (loading.value || (!reset && finished.value)) return
  if (reset) { pageNum.value = 1; finished.value = false }
  loading.value = true
  try {
    const data = {
      keyword: keyword.value || undefined,
      registrationStartTime: startDate.value ? `${startDate.value} 00:00:00` : undefined,
      registrationEndTime: endDate.value ? `${endDate.value} 23:59:59` : undefined
    }
    const res = await listPersonalTeams(data, { pageNum: pageNum.value, pageSize })
    const rows = Array.isArray(res?.rows) ? res.rows : []
    teams.value = reset ? rows : teams.value.concat(rows)
    finished.value = teams.value.length >= Number(res?.total || 0) || rows.length < pageSize
    if (!finished.value) pageNum.value += 1
  } finally { loading.value = false }
}

function search() { load(true) }
function clearDates() { startDate.value = ''; endDate.value = ''; load(true) }
function competitionName(item) { return [item.competitionName, item.competitionTrackName, item.secondLevelName].filter(Boolean).join(' - ') || '-' }
function members(item) { return Array.isArray(item.competitionApplyInfoList) ? item.competitionApplyInfoList : [] }
function visibleMembers(item) { return members(item).slice(0, 3) }
function memberCount(item) { return members(item).length }
function statusLabel(value) { return statusMap[value] || value }
function goDetail(teamCode) { uni.navigateTo({ url: `/pages-personal/team/detail?teamCode=${encodeURIComponent(teamCode)}` }) }
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: 24rpx 28rpx 60rpx; background: #f5f7fb; box-sizing: border-box; }
.filter-card, .team-card { border-radius: 22rpx; background: #fff; box-shadow: 0 6rpx 20rpx rgba(24, 52, 110, .05); }
.filter-card { padding: 24rpx; }
.search-row { display: flex; gap: 16rpx; }
.search-input { flex: 1; height: 70rpx; padding: 0 22rpx; border-radius: 36rpx; background: #f3f6fb; font-size: 24rpx; }
button::after { border: none; }
.search-btn { width: 116rpx; height: 70rpx; line-height: 70rpx; margin: 0; padding: 0; border-radius: 36rpx; color: #fff; background: #3169f8; font-size: 25rpx; }
.date-row { display: flex; align-items: center; gap: 15rpx; margin-top: 20rpx; color: #9aa3b2; font-size: 22rpx; }
.date-picker { min-width: 175rpx; padding: 14rpx 18rpx; border-radius: 12rpx; color: #59667b; background: #f5f7fb; text-align: center; }
.clear { margin-left: auto; color: #3169f8; }
.team-card { margin-top: 22rpx; padding: 25rpx; }
.head { display: flex; justify-content: space-between; gap: 18rpx; }
.head-main { flex: 1; min-width: 0; }
.team-name, .competition { display: block; }
.team-name { color: #263249; font-size: 29rpx; font-weight: 700; }
.competition { margin-top: 9rpx; color: #3169f8; font-size: 22rpx; line-height: 1.45; }
.status { align-self: flex-start; padding: 6rpx 14rpx; border-radius: 18rpx; color: #a36016; background: #fff1dc; font-size: 20rpx; }
.meta-row { display: flex; justify-content: space-between; gap: 20rpx; padding-top: 15rpx; color: #8b95a6; font-size: 22rpx; }
.meta-row text:last-child { color: #4a566b; text-align: right; }
.members { display: flex; align-items: center; flex-wrap: wrap; gap: 20rpx; margin-top: 22rpx; padding: 18rpx; border-radius: 15rpx; background: #f7f9fc; }
.member { display: flex; align-items: center; gap: 10rpx; }
.avatar { width: 52rpx; height: 52rpx; line-height: 52rpx; border-radius: 50%; color: #3169f8; background: #e8efff; text-align: center; font-size: 23rpx; font-weight: 700; }
.member-name, .role { display: block; }
.member-name { color: #334056; font-size: 22rpx; }
.role { margin-top: 3rpx; color: #929cac; font-size: 18rpx; }
.more { color: #7c8798; font-size: 20rpx; }
.detail-link { margin-top: 20rpx; padding-top: 18rpx; border-top: 1rpx solid #edf0f5; color: #3169f8; text-align: right; font-size: 23rpx; }
.detail-link text { margin-left: 8rpx; font-size: 32rpx; }
.empty { padding: 130rpx 0; color: #99a2b2; text-align: center; font-size: 25rpx; }
.bottom-tip { padding: 26rpx 0; color: #a0a8b6; text-align: center; font-size: 23rpx; }
</style>
