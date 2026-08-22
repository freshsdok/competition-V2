<template>
  <view class="page">
    <template v-if="!selectedSessionId">
      <view class="hero">
        <view>
          <view class="hero-title">秘书现场控制台</view>
          <view class="hero-sub">查看并控制分配给你的现场评审场次</view>
        </view>
        <view class="hero-count">{{ filteredSessions.length }}</view>
      </view>

      <view class="filter-panel">
        <view class="search-box">
          <text class="search-icon">⌕</text>
          <input
            v-model.trim="keyword"
            class="search-input"
            placeholder="搜索场次、活动、轮次、地点"
            confirm-type="search"
          />
          <text v-if="keyword" class="clear-icon" @click="keyword = ''">×</text>
        </view>
        <scroll-view scroll-x class="status-scroll" :show-scrollbar="false">
          <view class="status-row">
            <view
              v-for="item in sessionStatusFilters"
              :key="item.value"
              class="status-chip"
              :class="{ active: statusFilter === item.value }"
              @click="statusFilter = item.value"
            >
              {{ item.label }}
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="list-head">
        <view class="section-title">我的现场场次</view>
        <button class="light-btn small" :disabled="listLoading" @click="loadSessions">刷新</button>
      </view>

      <view v-if="listLoading" class="empty-card">加载中...</view>
      <view v-else-if="filteredSessions.length === 0" class="empty-card">
        暂无可操作的秘书场次
      </view>

      <view
        v-for="item in filteredSessions"
        :key="item.sessionId"
        class="session-card"
        @click="openSession(item)"
      >
        <view class="card-top">
          <view class="card-main">
            <view class="session-name">{{ item.sessionName || '未命名场次' }}</view>
            <view class="session-sub">{{ item.activityName || '未关联活动' }}</view>
          </view>
          <view class="status-pill" :class="sessionStatusClass(item.status)">
            {{ sessionStatusLabel(item.status) }}
          </view>
        </view>
        <view class="meta-grid">
          <text>{{ item.roundName || '未关联轮次' }}</text>
          <text>{{ item.location || '未设置场地' }}</text>
          <text>{{ formatRange(item.startTime, item.endTime) }}</text>
          <text>对象 {{ item.objectCount || 0 }} 个</text>
        </view>
        <view v-if="item.currentObjectId" class="current-line">
          当前：{{ item.currentObjectCode || '' }} {{ item.currentObjectName || '' }}
        </view>
      </view>
    </template>

    <template v-else>
      <view class="console-head">
        <view class="head-actions">
          <button class="light-btn small" @click="backToList">返回列表</button>
          <button class="light-btn small" :disabled="consoleLoading" @click="refreshConsole">刷新</button>
        </view>
        <view class="console-title-row">
          <view>
            <view class="console-title">{{ sessionInfo.sessionName || '现场评审控制台' }}</view>
            <view class="console-sub">
              {{ sessionInfo.location || '未设置场地' }} · {{ sessionInfo.activityName || '未关联活动' }}
            </view>
          </view>
          <view class="status-pill" :class="sessionStatusClass(sessionInfo.status)">
            {{ sessionStatusLabel(sessionInfo.status) }}
          </view>
        </view>
      </view>

      <view class="panel current-panel">
        <view class="section-title">当前评审对象</view>
        <view v-if="sessionInfo.currentObjectId" class="current-object">
          <view>
            <view class="object-code">{{ sessionInfo.currentObjectCode || '-' }}</view>
            <view class="object-name">{{ sessionInfo.currentObjectName || '-' }}</view>
            <view class="time-line">开始时间：{{ sessionInfo.currentStartedTime || '-' }}</view>
          </view>
        </view>
        <view v-else class="empty-inline">暂无当前评审对象</view>
      </view>

      <view class="panel">
        <view class="section-title">扫码识别参赛证</view>
        <view class="scan-input-row">
          <input
            v-model.trim="certificateCode"
            class="scan-input"
            placeholder="输入或粘贴参赛证编号"
            confirm-type="search"
            @confirm="handleResolveCertificate"
          />
          <button class="primary-btn inline" :disabled="resolving" @click="handleResolveCertificate">
            {{ resolving ? '解析中' : '解析' }}
          </button>
        </view>
        <button class="outline-btn" @click="scanCertificate">扫一扫</button>
        <view class="tip">识别后需秘书确认，才会切换当前评审对象。</view>

        <view v-if="resolveResult" class="resolve-box">
          <view class="resolve-head">
            <text>识别结果</text>
            <text class="count-badge">{{ resolveCandidates.length }} 个候选</text>
          </view>
          <view v-if="resolveResult.warningMessage" class="warning-box">
            {{ resolveResult.warningMessage }}
          </view>
          <view
            v-for="candidate in resolveCandidates"
            :key="`${candidate.objectId}-${candidate.certificateCode}`"
            class="candidate-row"
            :class="{ selected: selectedObjectId === candidate.objectId }"
            @click="selectedObjectId = candidate.objectId"
          >
            <view class="radio-dot"></view>
            <view class="candidate-main">
              <view class="object-code">{{ candidate.objectCode || '-' }}</view>
              <view class="object-name">{{ candidate.objectName || '-' }}</view>
              <view class="candidate-meta">
                {{ candidate.memberName || '-' }} · {{ memberRoleLabel(candidate.memberRole) }} · {{ candidate.certificateCode || '-' }}
              </view>
              <view v-if="candidate.warningMessage" class="warning-text">{{ candidate.warningMessage }}</view>
            </view>
          </view>
          <button
            class="primary-btn full"
            :disabled="!selectedObjectId || switching"
            @click="confirmSetCurrentFromScan"
          >
            设为当前评审对象
          </button>
        </view>
      </view>

      <view class="action-row">
        <button class="success-btn" :disabled="switching" @click="handleNextObject">下一位</button>
        <button class="light-btn" :disabled="consoleLoading" @click="refreshConsole">刷新列表</button>
      </view>

      <view class="panel">
        <view class="section-title">场次对象顺序</view>
        <view v-if="consoleLoading" class="empty-inline">加载中...</view>
        <view v-else-if="sessionObjects.length === 0" class="empty-inline">暂无场次对象</view>
        <view
          v-for="item in sessionObjects"
          :key="item.sessionObjectId"
          class="object-row"
          :class="{ current: item.objectId === sessionInfo.currentObjectId }"
        >
          <view class="object-row-top">
            <view class="sequence">#{{ item.sequenceNo || '-' }}</view>
            <view class="object-main">
              <view class="object-code">{{ item.objectCode || '-' }}</view>
              <view class="object-name">{{ item.objectName || '-' }}</view>
              <view class="object-sub">
                {{ item.orgName || '-' }} · 负责人：{{ item.leaderName || '-' }}
              </view>
            </view>
            <view v-if="item.objectId === sessionInfo.currentObjectId" class="current-tag">当前</view>
          </view>
          <view class="tag-row">
            <view class="mini-tag" :class="checkinClass(item.checkinStatus)">{{ checkinLabel(item.checkinStatus) }}</view>
            <view class="mini-tag" :class="reviewClass(item.reviewStatus)">{{ reviewStatusLabel(item.reviewStatus) }}</view>
            <view class="progress-text">评分 {{ item.scoreProgress?.displayText || '0/0' }}</view>
          </view>
          <view class="button-grid">
            <button class="grid-btn primary" @click="handleSetCurrentManual(item)">设为当前</button>
            <button class="grid-btn success" @click="handleStatus(item, { checkinStatus: 'PRESENT' })">到场</button>
            <button class="grid-btn warning" @click="handleStatus(item, { checkinStatus: 'ABSENT' })">缺席</button>
            <button class="grid-btn muted" @click="handleStatus(item, { reviewStatus: 'SKIPPED' })">跳过</button>
            <button class="grid-btn muted" @click="handleStatus(item, { reviewStatus: 'DELAYED' })">延后</button>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import {
  getSecretarySession,
  listMySecretarySessions,
  listSecretarySessionObjects,
  nextSecretaryObject,
  resolveReviewCertificate,
  setSecretaryCurrentObject,
  updateSecretarySessionObjectStatus
} from '@/api/reviewSecretary'

const listLoading = ref(false)
const consoleLoading = ref(false)
const resolving = ref(false)
const switching = ref(false)
const sessions = ref([])
const sessionObjects = ref([])
const selectedSessionId = ref('')
const keyword = ref('')
const statusFilter = ref('')
const certificateCode = ref('')
const resolveResult = ref(null)
const selectedObjectId = ref('')

const sessionInfo = reactive(defaultSessionInfo())

const sessionStatusFilters = [
  { label: '全部', value: '' },
  { label: '未开始', value: 'NOT_STARTED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已暂停', value: 'PAUSED' },
  { label: '已结束', value: 'ENDED' }
]

const filteredSessions = computed(() => {
  const text = normalize(keyword.value)
  return sessions.value.filter(item => {
    const statusMatched = !statusFilter.value || item.status === statusFilter.value
    const textMatched = !text || [
      item.sessionName,
      item.sessionCode,
      item.activityName,
      item.roundName,
      item.location,
      item.currentObjectCode,
      item.currentObjectName
    ].some(value => normalize(value).includes(text))
    return statusMatched && textMatched
  })
})

const resolveCandidates = computed(() => {
  const result = resolveResult.value
  if (!result) return []
  if (Array.isArray(result.candidates) && result.candidates.length > 0) return result.candidates
  if (result.objectId) return [result]
  return []
})

onShow(() => {
  if (selectedSessionId.value) {
    refreshConsole()
  } else {
    loadSessions()
  }
})

onPullDownRefresh(async () => {
  try {
    if (selectedSessionId.value) {
      await refreshConsole()
    } else {
      await loadSessions()
    }
  } finally {
    uni.stopPullDownRefresh()
  }
})

function defaultSessionInfo() {
  return {
    sessionId: undefined,
    sessionName: '',
    sessionCode: '',
    location: '',
    startTime: '',
    endTime: '',
    activityId: undefined,
    activityName: '',
    roundId: undefined,
    roundName: '',
    status: '',
    objectCount: 0,
    currentObjectId: undefined,
    currentObjectCode: '',
    currentObjectName: '',
    currentStartedTime: ''
  }
}

async function loadSessions() {
  listLoading.value = true
  try {
    const res = await listMySecretarySessions()
    sessions.value = getResponseList(res)
  } finally {
    listLoading.value = false
  }
}

async function openSession(item) {
  selectedSessionId.value = item.sessionId
  resetScanState()
  await refreshConsole()
}

function backToList() {
  selectedSessionId.value = ''
  sessionObjects.value = []
  Object.assign(sessionInfo, defaultSessionInfo())
  resetScanState()
  loadSessions()
}

async function refreshConsole() {
  if (!selectedSessionId.value) return
  consoleLoading.value = true
  try {
    const [sessionRes, objectRes] = await Promise.all([
      getSecretarySession(selectedSessionId.value),
      listSecretarySessionObjects(selectedSessionId.value)
    ])
    Object.assign(sessionInfo, defaultSessionInfo(), sessionRes.data || {})
    sessionObjects.value = getResponseList(objectRes)
  } finally {
    consoleLoading.value = false
  }
}

async function handleResolveCertificate() {
  if (!certificateCode.value) {
    uni.showToast({ title: '请输入参赛证编号', icon: 'none' })
    return
  }
  if (!sessionInfo.activityId) {
    uni.showToast({ title: '场次信息未加载', icon: 'none' })
    return
  }
  resolving.value = true
  try {
    const res = await resolveReviewCertificate({
      activityId: sessionInfo.activityId,
      sessionId: selectedSessionId.value,
      certificateCode: certificateCode.value
    })
    resolveResult.value = res.data
    const candidates = resolveCandidates.value
    selectedObjectId.value = candidates.length === 1 ? candidates[0].objectId : ''
    if (candidates.length === 0) {
      uni.showToast({ title: '未解析到可用对象', icon: 'none' })
    }
  } finally {
    resolving.value = false
  }
}

function scanCertificate() {
  uni.scanCode({
    onlyFromCamera: false,
    scanType: ['qrCode', 'barCode'],
    success: result => {
      certificateCode.value = parseScanValue(result.result || result.scanResult || '')
      if (certificateCode.value) {
        handleResolveCertificate()
      }
    },
    fail: () => {
      uni.showToast({ title: '无法调用扫码，请手动输入', icon: 'none' })
    }
  })
}

function parseScanValue(value) {
  const text = String(value || '').trim()
  if (!text) return ''
  try {
    const json = JSON.parse(text)
    return json.credentialToken || json.qrContent || json.certificateCode || json.credentialCode || json.code || json.token || text
  } catch (error) {
    const match = text.match(/(?:^|[?&])(credentialToken|qrContent|certificateCode|credentialCode|code|token)=([^&]+)/)
    return match ? decodeURIComponent(match[2]) : text
  }
}

function confirmSetCurrentFromScan() {
  setCurrentObject(selectedObjectId.value, 'SCAN', certificateCode.value)
}

function handleSetCurrentManual(item) {
  uni.showModal({
    title: '确认切换',
    content: `确认将 ${item.objectCode || ''} ${item.objectName || ''} 设为当前评审对象？`,
    confirmColor: '#3169F8',
    success: res => {
      if (res.confirm) {
        setCurrentObject(item.objectId, 'MANUAL')
      }
    }
  })
}

async function setCurrentObject(objectId, sourceType, code) {
  if (!objectId) {
    uni.showToast({ title: '请选择评审对象', icon: 'none' })
    return
  }
  switching.value = true
  try {
    await setSecretaryCurrentObject(selectedSessionId.value, {
      objectId,
      sourceType,
      certificateCode: code
    })
    uni.showToast({ title: '已设置当前对象', icon: 'success' })
    resetScanState()
    await refreshConsole()
  } finally {
    switching.value = false
  }
}

function handleNextObject() {
  uni.showModal({
    title: '下一位',
    content: '确认切换到下一位可评审对象？',
    confirmColor: '#3169F8',
    success: async res => {
      if (!res.confirm) return
      switching.value = true
      try {
        await nextSecretaryObject(selectedSessionId.value)
        uni.showToast({ title: '已切换下一位', icon: 'success' })
        await refreshConsole()
      } finally {
        switching.value = false
      }
    }
  })
}

function handleStatus(item, statusData) {
  const label = statusData.checkinStatus ? checkinLabel(statusData.checkinStatus) : reviewStatusLabel(statusData.reviewStatus)
  uni.showModal({
    title: '状态确认',
    content: `确认将 ${item.objectCode || ''} ${item.objectName || ''} 标记为“${label}”？`,
    confirmColor: '#3169F8',
    success: async res => {
      if (!res.confirm) return
      await updateSecretarySessionObjectStatus(item.sessionObjectId, statusData)
      uni.showToast({ title: '状态已更新', icon: 'success' })
      await refreshConsole()
    }
  })
}

function resetScanState() {
  certificateCode.value = ''
  resolveResult.value = null
  selectedObjectId.value = ''
}

function getResponseList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.rows)) return res.rows
  if (Array.isArray(res?.data?.rows)) return res.data.rows
  return []
}

function normalize(value) {
  return String(value || '').trim().toLowerCase()
}

function formatRange(start, end) {
  if (!start && !end) return '未设置时间'
  if (start && end) return `${shortTime(start)} 至 ${shortTime(end)}`
  return shortTime(start || end)
}

function shortTime(value) {
  return String(value || '').replace(/^\d{4}-/, '')
}

function sessionStatusLabel(status) {
  const map = {
    NOT_STARTED: '未开始',
    IN_PROGRESS: '进行中',
    PAUSED: '已暂停',
    ENDED: '已结束',
    ARCHIVED: '已归档'
  }
  return map[status] || status || '-'
}

function sessionStatusClass(status) {
  if (status === 'IN_PROGRESS') return 'success'
  if (status === 'PAUSED') return 'warning'
  if (status === 'ENDED' || status === 'ARCHIVED') return 'muted'
  return ''
}

function checkinLabel(status) {
  const map = {
    WAITING: '待签到',
    PRESENT: '已到场',
    ABSENT: '缺席',
    LATE: '迟到'
  }
  return map[status] || status || '-'
}

function checkinClass(status) {
  if (status === 'PRESENT') return 'success'
  if (status === 'ABSENT') return 'danger'
  if (status === 'LATE') return 'warning'
  return ''
}

function reviewStatusLabel(status) {
  const map = {
    WAITING: '等待中',
    REVIEWING: '评审中',
    SCORED: '已评分',
    COMPLETED: '已完成',
    SKIPPED: '已跳过',
    DELAYED: '已延后'
  }
  return map[status] || status || '-'
}

function reviewClass(status) {
  if (status === 'REVIEWING') return 'success'
  if (status === 'SKIPPED') return 'danger'
  if (status === 'DELAYED') return 'warning'
  if (status === 'COMPLETED') return 'muted'
  return ''
}

function memberRoleLabel(role) {
  const map = {
    LEADER: '负责人',
    MEMBER: '成员',
    CONTACT: '联系人',
    TEACHER: '指导教师',
    OTHER: '其他'
  }
  return map[role] || role || '-'
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: linear-gradient(180deg, #3169f8 0%, #f6f7fb 320rpx);
}

.hero,
.filter-panel,
.session-card,
.panel,
.console-head {
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 34rpx 28rpx;
  color: #fff;
  background: linear-gradient(135deg, #3169f8 0%, #5b8ff9 100%);
}

.hero-title {
  font-size: 40rpx;
  font-weight: 700;
}

.hero-sub {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.82);
}

.hero-count {
  min-width: 72rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 36rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: 700;
  color: #3169f8;
  background: #fff;
}

.filter-panel {
  margin-top: 22rpx;
  padding: 22rpx;
}

.search-box,
.scan-input-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.search-box {
  height: 76rpx;
  padding: 0 22rpx;
  border-radius: 16rpx;
  background: #f3f6ff;
}

.search-icon,
.clear-icon {
  color: #3169f8;
  font-size: 34rpx;
}

.search-input,
.scan-input {
  flex: 1;
  min-width: 0;
  height: 76rpx;
  font-size: 28rpx;
  color: #111827;
}

.status-scroll {
  margin-top: 18rpx;
  white-space: nowrap;
}

.status-row {
  display: flex;
  gap: 14rpx;
}

.status-chip {
  flex: 0 0 auto;
  height: 56rpx;
  line-height: 56rpx;
  padding: 0 24rpx;
  border-radius: 28rpx;
  font-size: 24rpx;
  color: #4b5563;
  background: #f3f4f6;
}

.status-chip.active {
  color: #fff;
  background: #3169f8;
}

.list-head,
.card-top,
.console-title-row,
.resolve-head,
.object-row-top,
.tag-row,
.head-actions,
.action-row {
  display: flex;
  align-items: center;
}

.list-head {
  justify-content: space-between;
  margin: 28rpx 0 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.session-card {
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.card-top,
.console-title-row {
  justify-content: space-between;
  gap: 18rpx;
}

.card-main,
.object-main,
.candidate-main {
  flex: 1;
  min-width: 0;
}

.session-name,
.console-title,
.object-name {
  font-size: 30rpx;
  font-weight: 700;
  line-height: 1.35;
  color: #111827;
  word-break: break-all;
}

.session-sub,
.console-sub,
.object-sub,
.candidate-meta,
.time-line,
.tip,
.progress-text {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.45;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #4b5563;
}

.current-line {
  margin-top: 18rpx;
  padding: 14rpx 16rpx;
  border-radius: 12rpx;
  color: #3169f8;
  background: #eef4ff;
  font-size: 24rpx;
}

.status-pill,
.mini-tag,
.current-tag,
.count-badge {
  flex: 0 0 auto;
  height: 44rpx;
  line-height: 44rpx;
  padding: 0 16rpx;
  border-radius: 22rpx;
  font-size: 22rpx;
  color: #3169f8;
  background: #eef4ff;
  white-space: nowrap;
}

.status-pill.success,
.mini-tag.success {
  color: #16a34a;
  background: #e8f7ee;
}

.status-pill.warning,
.mini-tag.warning {
  color: #ea580c;
  background: #fff7ed;
}

.status-pill.muted,
.mini-tag.muted {
  color: #6b7280;
  background: #f3f4f6;
}

.mini-tag.danger {
  color: #dc2626;
  background: #fef2f2;
}

.empty-card,
.empty-inline {
  padding: 48rpx 24rpx;
  border-radius: 16rpx;
  text-align: center;
  color: #6b7280;
  background: #fff;
  font-size: 28rpx;
}

.empty-inline {
  padding: 30rpx 16rpx;
  background: #f8fafc;
  font-size: 26rpx;
}

.console-head {
  padding: 24rpx;
}

.head-actions {
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.panel {
  margin-top: 20rpx;
  padding: 24rpx;
}

.current-object {
  margin-top: 18rpx;
  padding: 22rpx;
  border-radius: 16rpx;
  background: #eef4ff;
}

.object-code {
  font-size: 23rpx;
  font-weight: 700;
  color: #475569;
}

.scan-input-row {
  margin-top: 18rpx;
}

.scan-input {
  height: 78rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background: #f3f6ff;
}

.resolve-box {
  margin-top: 20rpx;
}

.resolve-head {
  justify-content: space-between;
  margin-bottom: 14rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #111827;
}

.warning-box,
.warning-text {
  color: #b45309;
  background: #fffbeb;
}

.warning-box {
  padding: 16rpx;
  border-radius: 12rpx;
  margin-bottom: 14rpx;
  font-size: 24rpx;
}

.candidate-row,
.object-row {
  border: 2rpx solid #e5e7eb;
  border-radius: 16rpx;
  background: #fff;
}

.candidate-row {
  display: flex;
  gap: 16rpx;
  padding: 20rpx;
  margin-bottom: 14rpx;
}

.candidate-row.selected {
  border-color: #3169f8;
  background: #f5f8ff;
}

.radio-dot {
  width: 28rpx;
  height: 28rpx;
  margin-top: 8rpx;
  border: 4rpx solid #cbd5e1;
  border-radius: 50%;
}

.candidate-row.selected .radio-dot {
  border-color: #3169f8;
  background: #3169f8;
}

.warning-text {
  margin-top: 10rpx;
  padding: 10rpx 12rpx;
  border-radius: 10rpx;
  font-size: 22rpx;
}

.action-row {
  gap: 16rpx;
  margin-top: 20rpx;
}

.action-row button {
  flex: 1;
}

.object-row {
  padding: 22rpx;
  margin-top: 18rpx;
}

.object-row.current {
  border-color: #3169f8;
  background: #f5f8ff;
}

.object-row-top {
  gap: 16rpx;
}

.sequence {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 68rpx;
  height: 68rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
  font-weight: 800;
  color: #3169f8;
  background: #eef4ff;
}

.current-tag {
  color: #fff;
  background: #3169f8;
}

.tag-row {
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.button-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
  margin-top: 18rpx;
}

button {
  height: 76rpx;
  line-height: 76rpx;
  border: none;
  border-radius: 16rpx;
  font-size: 28rpx;
  margin: 0;
}

button::after {
  border: none;
}

button[disabled] {
  opacity: 0.56;
}

.primary-btn,
.success-btn,
.grid-btn.primary {
  color: #fff;
  background: #3169f8;
}

.success-btn,
.grid-btn.success {
  background: #16a34a;
}

.grid-btn.warning {
  color: #fff;
  background: #f59e0b;
}

.grid-btn.muted,
.light-btn,
.outline-btn {
  color: #3169f8;
  background: #eef4ff;
}

.outline-btn {
  width: 100%;
  margin-top: 16rpx;
  border: 2rpx solid #3169f8;
  background: #fff;
}

.primary-btn.inline {
  flex: 0 0 150rpx;
}

.primary-btn.full {
  width: 100%;
  margin-top: 18rpx;
}

.light-btn.small {
  width: 160rpx;
  height: 60rpx;
  line-height: 60rpx;
  font-size: 24rpx;
}
</style>
