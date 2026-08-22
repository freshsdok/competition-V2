<template>
  <view class="page">
    <template v-if="team">
      <view class="hero">
        <text class="team-name">{{ team.teamName || '团队详情' }}</text>
        <text class="competition">{{ competitionName }}</text>
        <view class="hero-meta"><text>团队编号</text><text selectable>{{ team.teamCode || '-' }}</text></view>
      </view>

      <view class="card">
        <view class="card-title">团队信息</view>
        <view class="row"><text>赛事名称</text><text>{{ team.competitionName || '-' }}</text></view>
        <view class="row"><text>赛道</text><text>{{ team.competitionTrackName || '-' }}</text></view>
        <view class="row"><text>组别</text><text>{{ team.secondLevelName || '-' }}</text></view>
        <view class="row"><text>团队状态</text><text>{{ statusLabel(team.operationStatus) }}</text></view>
      </view>

      <view class="card">
        <view class="card-title">参赛成员 <text class="count">{{ members.length }} 人</text></view>
        <view v-for="(member, index) in members" :key="member.memberId || index" class="person">
          <view class="person-head">
            <view class="avatar">{{ (member.userName || '队').slice(0, 1) }}</view>
            <view class="person-main"><text class="name">{{ member.userName || '-' }}</text><text class="role">{{ member.competitionRoleName || '参赛队员' }}</text></view>
          </view>
          <view class="person-info"><text>证件号</text><text>{{ maskId(member.idCard) }}</text></view>
          <view class="person-info"><text>手机号</text><text>{{ maskPhone(member.phone) }}</text></view>
          <view class="person-info"><text>邮箱</text><text>{{ maskEmail(member.email) }}</text></view>
          <view v-if="member.schoolName" class="person-info"><text>学校</text><text>{{ member.schoolName }}</text></view>
        </view>
        <view v-if="!members.length" class="sub-empty">暂无参赛成员</view>
      </view>

      <view v-if="teachers.length" class="card">
        <view class="card-title">指导教师 <text class="count">{{ teachers.length }} 人</text></view>
        <view v-for="(teacher, index) in teachers" :key="teacher.memberId || index" class="teacher">
          <view class="avatar teacher-avatar">师</view>
          <view><text class="name">{{ teacher.userName || teacher.guideTeacher || '-' }}</text><text class="role">{{ maskPhone(teacher.phone || teacher.guideTeacherPhone) }}</text></view>
        </view>
      </view>

      <view class="readonly-tip">本期小程序仅提供团队列表与详情查看，团队信息调整请使用 PC 端。</view>
    </template>
    <view v-else-if="loading" class="empty">加载中...</view>
    <view v-else class="empty">团队不存在或无权访问</view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPersonalTeam } from '@/api/team'

const team = ref(null)
const loading = ref(true)
const members = computed(() => Array.isArray(team.value?.competitionApplyInfoList) ? team.value.competitionApplyInfoList : [])
const teachers = computed(() => Array.isArray(team.value?.guideTeacherApplyInfoList) ? team.value.guideTeacherApplyInfoList : [])
const competitionName = computed(() => [team.value?.competitionName, team.value?.competitionTrackName, team.value?.secondLevelName].filter(Boolean).join(' - ') || '-')

onLoad(async options => {
  if (!options?.teamCode) { loading.value = false; return }
  try {
    const res = await getPersonalTeam({ teamCode: decodeURIComponent(options.teamCode) })
    const rows = Array.isArray(res?.data) ? res.data : []
    team.value = rows[0] || null
  } finally { loading.value = false }
})

function statusLabel(value) {
  const map = { retired: '已退赛', change: '信息变更中', repayment: '退费重缴中' }
  return map[value] || (value ? value : '正常')
}
function maskId(value) { return value && value.length >= 8 ? `${value.slice(0, 3)}******${value.slice(-4)}` : value || '-' }
function maskPhone(value) { return value && value.length >= 7 ? `${value.slice(0, 3)}****${value.slice(-4)}` : value || '-' }
function maskEmail(value) {
  if (!value || !value.includes('@')) return value || '-'
  const [name, domain] = value.split('@')
  return `${name.slice(0, 2)}***@${domain}`
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: 24rpx 28rpx 60rpx; background: #f5f7fb; box-sizing: border-box; }
.hero { padding: 32rpx 28rpx; border-radius: 22rpx; color: #fff; background: linear-gradient(135deg, #3169f8, #5c8df9); box-shadow: 0 10rpx 28rpx rgba(49,105,248,.2); }
.team-name, .competition { display: block; }
.team-name { font-size: 34rpx; font-weight: 700; }
.competition { margin-top: 10rpx; color: rgba(255,255,255,.84); font-size: 23rpx; line-height: 1.5; }
.hero-meta { display: flex; justify-content: space-between; margin-top: 24rpx; padding-top: 20rpx; border-top: 1rpx solid rgba(255,255,255,.22); font-size: 22rpx; }
.card { margin-top: 22rpx; padding: 26rpx; border-radius: 22rpx; background: #fff; box-shadow: 0 6rpx 20rpx rgba(24, 52, 110, .05); }
.card-title { margin-bottom: 10rpx; color: #263249; font-size: 29rpx; font-weight: 700; }
.count { margin-left: 8rpx; color: #8d97a8; font-size: 21rpx; font-weight: 400; }
.row { display: flex; justify-content: space-between; gap: 24rpx; padding: 14rpx 0; color: #8a94a5; font-size: 23rpx; }
.row text:last-child { max-width: 470rpx; color: #354156; text-align: right; }
.person { padding: 24rpx 0; border-top: 1rpx solid #edf0f5; }
.person-head, .teacher { display: flex; align-items: center; gap: 16rpx; }
.avatar { width: 64rpx; height: 64rpx; line-height: 64rpx; border-radius: 50%; color: #3169f8; background: #e8efff; text-align: center; font-size: 26rpx; font-weight: 700; }
.person-main { flex: 1; }
.name, .role { display: block; }
.name { color: #2e394d; font-size: 26rpx; font-weight: 600; }
.role { margin-top: 5rpx; color: #9099aa; font-size: 21rpx; }
.person-info { display: flex; justify-content: space-between; gap: 20rpx; padding: 12rpx 0 0 80rpx; color: #8a94a5; font-size: 22rpx; }
.person-info text:last-child { max-width: 430rpx; color: #4a566a; text-align: right; }
.teacher { padding: 20rpx 0; border-top: 1rpx solid #edf0f5; }
.teacher-avatar { color: #9b651e; background: #fff1dc; }
.readonly-tip { margin-top: 22rpx; padding: 20rpx; border-radius: 14rpx; color: #6f7b8e; background: #eef2f8; text-align: center; font-size: 22rpx; line-height: 1.5; }
.sub-empty, .empty { padding: 120rpx 0; color: #99a2b2; text-align: center; font-size: 25rpx; }
</style>
