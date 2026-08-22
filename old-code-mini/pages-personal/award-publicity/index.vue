<template>
  <view class="page">
    <view class="page-head">
      <text class="page-title">获奖公示</text>
      <text class="page-desc">当前共有 {{ publicities.length }} 个比赛的获奖公示</text>
    </view>

    <scroll-view v-if="publicities.length" class="tabs-scroll" scroll-x :show-scrollbar="false">
      <view class="tabs">
        <view
          v-for="item in publicities"
          :key="item.id"
          class="tab"
          :class="{ active: activeId === item.id }"
          @click="changePublicity(item.id)"
        >
          <text class="tab-name">{{ item.competitionName || '获奖公示' }}</text>
          <text class="tab-status" :class="{ ongoing: item.expired }">{{ item.expired ? '公示中' : '已截止' }}</text>
        </view>
      </view>
    </scroll-view>

    <view v-if="currentPublicity" class="summary-card">
      <view class="summary-head">
        <view class="summary-main">
          <text class="competition-name">{{ currentPublicity.competitionName || '获奖公示' }}</text>
          <text class="deadline">公示截止时间：{{ currentPublicity.expirationTime || '-' }}</text>
        </view>
        <text class="summary-status" :class="{ ongoing: currentPublicity.expired }">
          {{ currentPublicity.expired ? '公示中' : '已截止' }}
        </text>
      </view>
      <view v-if="currentPublicity.tipInfo" class="notice">
        <rich-text :nodes="currentPublicity.tipInfo" />
      </view>
    </view>

    <view v-if="currentPublicity" class="filter-bar">
      <view>
        <text class="filter-title">获奖名单</text>
        <text class="filter-count">共 {{ total }} 条</text>
      </view>
      <button class="filter-toggle" :class="{ selected: hasFilters }" @click="showFilters = !showFilters">
        {{ hasFilters ? '已筛选' : '筛选' }} {{ showFilters ? '收起' : '›' }}
      </button>
    </view>

    <view v-if="currentPublicity && showFilters" class="filter-card">
      <view class="filter-grid">
        <input v-model.trim="filters.teamName" class="filter-input" placeholder="团队名称" confirm-type="search" @confirm="search" />
        <input v-model.trim="filters.schoolName" class="filter-input" placeholder="学校" confirm-type="search" @confirm="search" />
        <input v-model.trim="filters.competitionTrackName" class="filter-input" placeholder="赛道" confirm-type="search" @confirm="search" />
        <input v-model.trim="filters.awardsName" class="filter-input" placeholder="奖项" confirm-type="search" @confirm="search" />
        <input v-model.trim="filters.userName" class="filter-input" placeholder="学生姓名" confirm-type="search" @confirm="search" />
        <input v-model.trim="filters.guiderTeacherName" class="filter-input" placeholder="指导教师" confirm-type="search" @confirm="search" />
      </view>
      <view class="filter-actions">
        <button class="reset-btn" @click="resetFilters">重置</button>
        <button class="search-btn" @click="search">查询</button>
      </view>
    </view>

    <view v-for="(item, index) in details" :key="item.id || `${item.teamCode}-${index}`" class="award-card">
      <view class="award-head">
        <view class="award-main">
          <text class="team-name">{{ item.teamName || '未命名团队' }}</text>
          <text class="track-name">{{ trackName(item) }}</text>
        </view>
        <text class="award-tag" :class="awardClass(item.awardsName)">{{ item.awardsName || '获奖' }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">学校</text>
        <text class="info-value">{{ item.schoolName || '-' }}</text>
      </view>
      <view class="people-block">
        <view class="people-row">
          <text class="people-label">参赛学生</text>
          <text class="people-value">{{ playerNames(item.playerList) }}</text>
        </view>
        <view class="people-row">
          <text class="people-label">指导教师</text>
          <text class="people-value">{{ teacherNames(item.guiderTeacherList) }}</text>
        </view>
      </view>
    </view>

    <view v-if="tabsLoading || listLoading" class="state">加载中...</view>
    <view v-else-if="!publicities.length" class="state">暂无公示数据</view>
    <view v-else-if="!details.length" class="state">暂无与当前账号相关的获奖信息</view>
    <view v-else-if="finished" class="bottom-tip">没有更多了</view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listAwardPublicities, listAwardDetails } from '@/api/awardPublicity'

const publicities = ref([])
const activeId = ref(null)
const details = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 20
const tabsLoading = ref(false)
const listLoading = ref(false)
const finished = ref(false)
const showFilters = ref(false)
const filters = reactive(emptyFilters())
let requestSequence = 0

const currentPublicity = computed(() => publicities.value.find(item => item.id === activeId.value))
const hasFilters = computed(() => Object.values(filters).some(value => String(value || '').trim()))

onLoad(() => loadPublicities())
onPullDownRefresh(async () => {
  await loadPublicities(true)
  uni.stopPullDownRefresh()
})
onReachBottom(() => loadDetails(false))

async function loadPublicities(keepCurrent = false) {
  if (tabsLoading.value) return
  tabsLoading.value = true
  try {
    const res = await listAwardPublicities()
    const rows = Array.isArray(res?.data) ? res.data : []
    publicities.value = rows
    const currentExists = rows.some(item => item.id === activeId.value)
    const firstPublicity = rows[0]
    const firstPublicityId = firstPublicity && firstPublicity.id != null ? firstPublicity.id : null
    activeId.value = keepCurrent && currentExists ? activeId.value : firstPublicityId
    if (activeId.value) await loadDetails(true)
    else clearDetails()
  } finally {
    tabsLoading.value = false
  }
}

function changePublicity(id) {
  if (activeId.value === id) return
  activeId.value = id
  Object.assign(filters, emptyFilters())
  showFilters.value = false
  loadDetails(true)
}

async function loadDetails(reset) {
  if (!activeId.value || (!reset && (listLoading.value || finished.value))) return
  if (reset) {
    pageNum.value = 1
    finished.value = false
    details.value = []
    total.value = 0
  }
  const sequence = ++requestSequence
  listLoading.value = true
  try {
    const res = await listAwardDetails({
      awardPublicityId: activeId.value,
      pageNum: pageNum.value,
      pageSize,
      ...normalizedFilters()
    })
    if (sequence !== requestSequence) return
    const rows = Array.isArray(res?.rows) ? res.rows : []
    const responseTotal = Number(res?.total || 0)
    details.value = reset ? rows : details.value.concat(rows)
    total.value = responseTotal
    finished.value = details.value.length >= responseTotal || rows.length < pageSize
    if (!finished.value) pageNum.value += 1
  } finally {
    if (sequence === requestSequence) listLoading.value = false
  }
}

function search() {
  loadDetails(true)
}

function resetFilters() {
  Object.assign(filters, emptyFilters())
  loadDetails(true)
}

function emptyFilters() {
  return {
    teamName: '',
    schoolName: '',
    competitionTrackName: '',
    awardsName: '',
    userName: '',
    guiderTeacherName: ''
  }
}

function normalizedFilters() {
  return Object.fromEntries(
    Object.entries(filters).map(([key, value]) => [key, String(value || '').trim() || undefined])
  )
}

function clearDetails() {
  requestSequence += 1
  details.value = []
  total.value = 0
  pageNum.value = 1
  finished.value = true
  listLoading.value = false
}

function trackName(item) {
  return [item.competitionTrackName, item.secondLevelName].filter(Boolean).join(' / ') || '-'
}

function sortedPeople(values) {
  return Array.isArray(values) ? [...values].sort((a, b) => Number(a?.teamSort || 0) - Number(b?.teamSort || 0)) : []
}

function playerNames(values) {
  const names = sortedPeople(values).map(item => item?.userName).filter(Boolean)
  return names.length ? names.map((name, index) => `${index + 1}、${name}`).join('，') : '-'
}

function teacherNames(values) {
  const names = sortedPeople(values).map(item => item?.userName).filter(Boolean)
  return names.length ? names.join('、') : '-'
}

function awardClass(value) {
  const text = String(value || '')
  if (text.includes('一等') || text.includes('特等') || text.includes('金')) return 'first'
  if (text.includes('二等') || text.includes('银')) return 'second'
  if (text.includes('三等') || text.includes('铜')) return 'third'
  return 'other'
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: 24rpx 28rpx 60rpx; background: #f5f7fb; box-sizing: border-box; }
.page-head { padding: 10rpx 2rpx 22rpx; }
.page-title, .page-desc { display: block; }
.page-title { color: #25314a; font-size: 36rpx; font-weight: 700; }
.page-desc { margin-top: 9rpx; color: #8a94a5; font-size: 23rpx; }
.tabs-scroll { width: 100%; white-space: nowrap; }
.tabs { display: inline-flex; gap: 16rpx; padding: 2rpx 2rpx 18rpx; }
.tab { display: inline-flex; align-items: center; gap: 12rpx; max-width: 510rpx; padding: 17rpx 20rpx; border: 1rpx solid #e1e6ef; border-radius: 16rpx; color: #647086; background: #fff; }
.tab.active { border-color: #3169f8; color: #3169f8; background: #edf3ff; }
.tab-name { max-width: 350rpx; overflow: hidden; font-size: 24rpx; text-overflow: ellipsis; }
.tab-status { flex: 0 0 auto; padding: 4rpx 10rpx; border-radius: 15rpx; color: #778295; background: #edf0f4; font-size: 18rpx; }
.tab-status.ongoing { color: #13845a; background: #ddf6eb; }
.summary-card, .filter-card, .award-card { border-radius: 22rpx; background: #fff; box-shadow: 0 6rpx 20rpx rgba(24, 52, 110, .05); }
.summary-card { padding: 26rpx; }
.summary-head { display: flex; justify-content: space-between; gap: 20rpx; }
.summary-main { flex: 1; min-width: 0; }
.competition-name, .deadline { display: block; }
.competition-name { color: #263249; font-size: 29rpx; font-weight: 700; line-height: 1.45; }
.deadline { margin-top: 10rpx; color: #8792a4; font-size: 21rpx; }
.summary-status { flex: 0 0 auto; align-self: flex-start; padding: 7rpx 15rpx; border-radius: 18rpx; color: #778295; background: #edf0f4; font-size: 20rpx; }
.summary-status.ongoing { color: #13845a; background: #ddf6eb; }
.notice { margin-top: 22rpx; padding: 19rpx 20rpx; overflow: hidden; border: 1rpx solid #f0cf9c; border-radius: 14rpx; color: #80551a; background: #fff8eb; font-size: 23rpx; line-height: 1.65; word-break: break-all; }
.filter-bar { display: flex; align-items: center; justify-content: space-between; margin: 25rpx 2rpx 2rpx; }
.filter-title { color: #2a354a; font-size: 29rpx; font-weight: 700; }
.filter-count { margin-left: 12rpx; color: #97a0af; font-size: 21rpx; }
button::after { border: none; }
.filter-toggle { height: 58rpx; line-height: 56rpx; margin: 0; padding: 0 20rpx; border: 1rpx solid #dce3ee; border-radius: 30rpx; color: #566378; background: #fff; font-size: 22rpx; }
.filter-toggle.selected { border-color: #b9caff; color: #3169f8; background: #edf3ff; }
.filter-card { margin-top: 18rpx; padding: 22rpx; }
.filter-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 15rpx; }
.filter-input { min-width: 0; height: 68rpx; padding: 0 18rpx; border-radius: 13rpx; color: #344156; background: #f4f6fa; font-size: 23rpx; box-sizing: border-box; }
.filter-actions { display: flex; justify-content: flex-end; gap: 16rpx; margin-top: 20rpx; }
.reset-btn, .search-btn { width: 135rpx; height: 64rpx; line-height: 62rpx; margin: 0; padding: 0; border-radius: 33rpx; font-size: 23rpx; }
.reset-btn { border: 1rpx solid #d8dfe9; color: #637086; background: #fff; }
.search-btn { color: #fff; background: #3169f8; }
.award-card { margin-top: 20rpx; padding: 25rpx; }
.award-head { display: flex; justify-content: space-between; gap: 18rpx; }
.award-main { flex: 1; min-width: 0; }
.team-name, .track-name { display: block; }
.team-name { color: #27334a; font-size: 28rpx; font-weight: 700; line-height: 1.45; }
.track-name { margin-top: 8rpx; color: #3169f8; font-size: 21rpx; line-height: 1.4; }
.award-tag { flex: 0 0 auto; align-self: flex-start; padding: 7rpx 14rpx; border-radius: 18rpx; font-size: 20rpx; }
.award-tag.first { color: #c8463b; background: #ffebe8; }
.award-tag.second { color: #b56b13; background: #fff0d9; }
.award-tag.third { color: #16825a; background: #e2f7ee; }
.award-tag.other { color: #7755c4; background: #efe9ff; }
.info-row { display: flex; justify-content: space-between; gap: 20rpx; margin-top: 19rpx; padding: 17rpx 0; border-top: 1rpx solid #edf0f5; color: #8a94a5; font-size: 22rpx; }
.info-value { max-width: 480rpx; color: #4b586d; text-align: right; }
.people-block { padding: 17rpx 18rpx; border-radius: 14rpx; background: #f7f9fc; }
.people-row { display: flex; gap: 20rpx; padding: 7rpx 0; font-size: 22rpx; line-height: 1.55; }
.people-label { flex: 0 0 100rpx; color: #8b95a5; }
.people-value { flex: 1; color: #3d4a60; word-break: break-all; }
.state { padding: 130rpx 0; color: #99a2b2; text-align: center; font-size: 25rpx; }
.bottom-tip { padding: 28rpx 0 6rpx; color: #a0a8b6; text-align: center; font-size: 22rpx; }
</style>
