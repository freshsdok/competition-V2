<template>
  <view class="page">
    <view class="section">
      <view class="section-header" @click="toggleSection('reservations')">
        <view>
          <view class="section-title">我的预约</view>
          <view class="section-sub">{{ reservations.length }} 条记录</view>
        </view>
        <view class="section-toggle">{{ expandedSections.reservations ? '收起' : '展开' }}</view>
      </view>
      <view v-if="expandedSections.reservations">
        <view v-if="reservations.length === 0" class="empty small">暂无预约记录</view>
        <view v-for="item in reservations" :key="item.reservationId" class="reservation-card">
          <view class="card-top">
            <view>
              <view class="name">{{ item.resourceName }}</view>
              <view class="sub">{{ item.deploymentLocation || '-' }}</view>
            </view>
            <view class="tag muted-tag">{{ reservationStatusText(item) }}</view>
          </view>
          <view class="meta one">
            <text>{{ item.slotStartTime }} 至 {{ item.slotEndTime }}</text>
            <text>设备数 {{ item.reservedDeviceCount }}，覆盖工位 {{ item.coveredWorkstationCount }}</text>
          </view>
          <button
            v-if="item.reservationStatus === 'RESERVED'"
            class="cancel-btn"
            @click="cancel(item)"
          >
            取消预约
          </button>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-header" @click="toggleSection('resources')">
        <view>
          <view class="section-title">可预约资源</view>
          <view class="section-sub">{{ resources.length }} 个资源</view>
        </view>
        <view class="section-toggle">{{ expandedSections.resources ? '收起' : '展开' }}</view>
      </view>
      <view v-if="expandedSections.resources">
        <view v-if="loading" class="empty">加载中...</view>
        <view v-else-if="resources.length === 0" class="empty">暂无可预约资源</view>
        <view
          v-for="item in resources"
          :key="item.scheduleResourceId"
          class="resource-card"
          :class="{ active: currentResource && currentResource.scheduleResourceId === item.scheduleResourceId }"
          @click="selectResource(item)"
        >
          <view class="card-top">
            <view>
              <view class="name">{{ item.resourceName }}</view>
              <view class="sub">{{ item.brandModel || item.resourceType }}</view>
            </view>
            <view class="tag" :class="{ warning: item.existingReservation }">
              {{ item.existingReservation ? '已预约' : '可预约' }}
            </view>
          </view>
          <view class="meta">
            <text>位置：{{ item.deploymentLocation || '-' }}</text>
            <text>剩余设备：{{ item.remainingDeviceCount || 0 }}</text>
            <text>剩余工位：{{ item.remainingWorkstationCount || 0 }}</text>
            <text>每台工位：{{ item.workstationsPerDevice || 1 }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-header" @click="toggleSection('slots')">
        <view>
          <view class="section-title">可预约时段</view>
          <view class="section-sub">{{ currentResource ? currentResource.resourceName : '请选择资源' }}</view>
        </view>
        <view class="section-toggle">{{ expandedSections.slots ? '收起' : '展开' }}</view>
      </view>
      <view v-if="expandedSections.slots">
        <view v-if="!currentResource" class="empty small">请先选择可预约资源</view>
        <template v-else>
          <view class="summary">
            <view>预约主体：{{ subjectLabel(currentResource) }}</view>
            <view>参赛人数：{{ currentResource.participantCount || 1 }}</view>
            <view>建议设备数：{{ currentResource.suggestedDeviceCount || 1 }}</view>
            <view>覆盖工位数：{{ currentResource.coveredWorkstationCount || 1 }}</view>
          </view>
          <view v-if="currentResource.existingReservation" class="notice warning-bg">
            已有预约：{{ currentResource.existingReservation.resourceName || '' }}
            {{ currentResource.existingReservation.slotStartTime || '' }}
          </view>
          <view class="notice" v-if="currentResource.safetyNotice">安全须知：{{ currentResource.safetyNotice }}</view>
          <view class="notice" v-if="currentResource.attentionNotes">  <h2 style="font-weight: 800;">注意事项：</h2> {{ currentResource.attentionNotes }}</view>
          <view v-if="slots.length === 0" class="empty small">暂无开放时段</view>
          <view v-for="slot in slots" :key="slot.slotId" class="slot-row">
            <view>
              <view class="slot-time">{{ slot.startTime }}</view>
              <view class="slot-time">{{ slot.endTime }}</view>
              <view class="sub">剩余设备 {{ slot.remainingDeviceCount }}，剩余工位 {{ slot.remainingWorkstationCount }}</view>
            </view>
            <button
              class="reserve-btn"
              :disabled="!!currentResource.existingReservation"
              @click.stop="reserve(slot)"
            >
              预约
            </button>
          </view>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  cancelSceneResourceReservation,
  getBookableSceneResource,
  listBookableSceneResource,
  listBookableSceneResourceSlot,
  listMySceneResourceReservation,
  submitSceneResourceReservation
} from '@/api/sceneResource'

const loading = ref(false)
const resources = ref([])
const slots = ref([])
const reservations = ref([])
const currentResource = ref(null)
const expandedSections = ref({
  reservations: true,
  resources: true,
  slots: true
})

onShow(() => {
  loadAll()
})

async function loadAll() {
  await Promise.all([loadResources(), loadReservations()])
}

async function loadResources() {
  loading.value = true
  try {
    const res = await listBookableSceneResource()
    resources.value = res.data || []
    if (!currentResource.value && resources.value.length > 0) {
      await selectResource(resources.value[0])
    }
  } finally {
    loading.value = false
  }
}

async function loadReservations() {
  const res = await listMySceneResourceReservation()
  reservations.value = res.data || []
}

async function selectResource(item) {
  expandedSections.value.slots = true
  try {
    const detail = await getBookableSceneResource(item.scheduleResourceId)
    currentResource.value = detail.data || item
    const slotRes = await listBookableSceneResourceSlot({
      scheduleResourceId: item.scheduleResourceId
    })
    slots.value = slotRes.data || []
  } catch (error) {
    if (error && error.existingReservation) {
      currentResource.value = {
        ...item,
        existingReservation: error.existingReservation
      }
    }
  }
}

function toggleSection(key) {
  expandedSections.value[key] = !expandedSections.value[key]
}

function reserve(slot) {
  uni.showModal({
    title: '预约确认',
    content: `${slot.startTime} 至 ${slot.endTime}`,
    confirmColor: '#3169F8',
    success: async (res) => {
      if (!res.confirm) return
      try {
        const result = await submitSceneResourceReservation({
          slotId: slot.slotId,
          idempotencyKey: `${Date.now()}-${Math.random().toString(16).slice(2)}`
        })
        uni.showToast({ title: '预约成功', icon: 'success' })
        currentResource.value.existingReservation = result.data
        await loadAll()
      } catch (error) {
        if (error && error.errorCode === 'ALREADY_RESERVED_BY_SUBJECT' && error.existingReservation) {
          currentResource.value.existingReservation = error.existingReservation
          uni.showToast({ title: '已有有效预约', icon: 'none' })
          await loadReservations()
        }
      }
    }
  })
}

function cancel(item) {
  uni.showModal({
    title: '取消预约',
    content: '确认取消该预约？',
    confirmColor: '#3169F8',
    success: async (res) => {
      if (!res.confirm) return
      await cancelSceneResourceReservation({
        reservationId: item.reservationId,
        cancelReason: '用户主动取消'
      })
      uni.showToast({ title: '已取消', icon: 'success' })
      await loadAll()
    }
  })
}

function subjectLabel(item) {
  return item.subjectType === 'TEAM'
    ? `团队 ${item.subjectName || item.subjectCode || ''}`
    : `个人 ${item.subjectName || item.subjectCode || ''}`
}

function reservationStatusText(item) {
  if (item.expired && item.reservationStatus === 'RESERVED') return '已过期'
  const map = {
    RESERVED: '已预约',
    CANCELLED: '已取消',
    CHECKED: '已核销',
    EXPIRED: '已过期'
  }
  return map[item.reservationStatus] || item.reservationStatus || '-'
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f6f7fb;
  padding: 24rpx;
}

.section {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1f2937;
}

.section-sub {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 6rpx;
}

.section-toggle {
  flex: 0 0 auto;
  height: 52rpx;
  line-height: 52rpx;
  padding: 0 22rpx;
  border-radius: 26rpx;
  background: #eef2ff;
  color: #3169f8;
  font-size: 24rpx;
}

.resource-card,
.reservation-card {
  border: 2rpx solid #e5e7eb;
  border-radius: 12rpx;
  padding: 22rpx;
  margin-top: 18rpx;
}

.resource-card.active {
  border-color: #3169f8;
}

.card-top {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.name {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
}

.sub {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 6rpx;
}

.tag {
  height: 42rpx;
  line-height: 42rpx;
  padding: 0 16rpx;
  border-radius: 21rpx;
  background: #e8f7ee;
  color: #16a34a;
  font-size: 22rpx;
  white-space: nowrap;
}

.warning,
.muted-tag {
  background: #fff7ed;
  color: #ea580c;
}

.meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 18rpx;
  font-size: 24rpx;
  color: #4b5563;
}

.meta.one {
  grid-template-columns: 1fr;
}

.summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12rpx;
  font-size: 24rpx;
  color: #374151;
  margin-bottom: 18rpx;
}

.notice {
  font-size: 24rpx;
  color: #4b5563;
  line-height: 1.6;
  white-space: pre-line;
  background: #f9fafb;
  padding: 16rpx;
  border-radius: 8rpx;
  margin-bottom: 12rpx;
}

.warning-bg {
  color: #b45309;
  background: #fffbeb;
}

.slot-title {
  font-size: 28rpx;
  font-weight: 600;
  margin: 24rpx 0 12rpx;
}

.slot-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18rpx;
  border-top: 2rpx solid #f3f4f6;
  padding: 18rpx 0;
}

.slot-time {
  font-size: 24rpx;
  color: #111827;
}

.reserve-btn,
.cancel-btn {
  width: 150rpx;
  height: 64rpx;
  line-height: 64rpx;
  font-size: 24rpx;
  border-radius: 32rpx;
  color: #fff;
  background: #3169f8;
}

.reserve-btn[disabled] {
  color: #fff;
  background: #cbd5e1;
}

.cancel-btn {
  width: 180rpx;
  margin: 18rpx 0 0;
  background: #ef4444;
}

.empty {
  text-align: center;
  color: #9ca3af;
  padding: 48rpx 0;
  font-size: 26rpx;
}

.empty.small {
  padding: 28rpx 0;
}
</style>
