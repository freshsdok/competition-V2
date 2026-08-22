<template>
  <view class="page">
    <view v-if="loading" class="empty-card">加载中...</view>
    <view v-else-if="credentialList.length === 0" class="empty-card">暂无现场证件，请等待管理员生成</view>

    <template v-else>
      <scroll-view v-if="competitionGroups.length > 1" scroll-x class="competition-scroll" :show-scrollbar="false">
        <view class="competition-row">
          <view
            v-for="group in competitionGroups"
            :key="group.key"
            class="competition-chip"
            :class="{ active: selectedCompetitionKey === group.key }"
            @click="selectCompetition(group)"
          >
            <text class="competition-chip-title">{{ group.competitionName }}</text>
            <text class="competition-chip-sub">{{ group.sceneCredentials.length }} 个赛场</text>
          </view>
        </view>
      </scroll-view>

      <view
        class="hero"
        :class="{
          empty: !currentCompetitionCredential,
          reported: isReportDone(currentCompetitionCredential)
        }"
      >
        <view class="hero-head">
          <view class="hero-copy">
            <view class="hero-card-title">
              <image class="hero-card-logo" src="/static/xiaotubiao.png" mode="aspectFit" />
              <text class="hero-card-title-text">大学生新一代信息通信科技大赛参赛证</text>
            </view>
            <text class="hero-title">{{ competitionHeroTitle(currentCompetition, currentCompetitionCredential) }}</text>
            <text class="hero-sub">{{ competitionHeroSub(currentCompetitionCredential) }}</text>
            <view v-if="teamMemberTexts(currentCompetitionCredential).length" class="hero-members">
              <view class="hero-members-label">队友</view>
              <view class="hero-members-list">{{ teamMemberTexts(currentCompetitionCredential).join('、') }}</view>
            </view>
          </view>
          <view v-if="currentCompetitionCredential" class="hero-role">
            {{ credentialTypeLabel(currentCompetitionCredential.credentialType) }}
          </view>
        </view>

        <view v-if="currentCompetitionCredential" class="hero-credential">
          <view v-if="credentialQrValue(currentCompetitionCredential)" class="hero-qr-card">
            <view class="hero-qr-box">
              <canvas
                :id="credentialCanvasId(currentCompetitionCredential)"
                :canvas-id="credentialCanvasId(currentCompetitionCredential)"
                class="hero-qr-canvas"
                :style="{ width: qrCanvasSize + 'px', height: qrCanvasSize + 'px' }"
              />
            </view>
            <view class="hero-qr-copy">
              <view class="hero-qr-title">大赛证二维码</view>
              <view class="hero-qr-desc">现场报到、领取资料和身份核验时出示此二维码</view>
            </view>
          </view>
          <view v-else class="hero-qr-missing">
            暂无大赛证二维码，请联系现场工作人员
          </view>
        </view>

        <view v-if="!currentCompetitionCredential" class="hero-empty-text">当前大赛暂无大赛总证</view>

        <button
          v-if="currentCompetitionCredential && credentialQrValue(currentCompetitionCredential)"
          class="hero-download-btn"
          :disabled="!credentialQrValue(currentCompetitionCredential)"
          @click="downloadCredentialQrCode(currentCompetitionCredential)"
        >
          下载大赛证
        </button>

        <view v-if="hasCurrentNotices" class="hero-notice-panel">
          <view class="hero-notice-head">
            <text class="hero-notice-title">现场通知</text>
            <text class="hero-notice-count">{{ currentNoticeCount }} 条</text>
          </view>

          <view v-if="currentNoticeGroup.personalNotices.length" class="notice-group">
            <view class="notice-group-title">注意事项</view>
            <view
              v-for="noticeItem in currentNoticeGroup.personalNotices"
              :key="noticeItem.noticeId"
              class="notice-item personal"
              :class="noticeLevelClass(noticeItem.noticeLevel)"
            >
              <view class="notice-item-head">
                <text class="notice-item-title">{{ noticeItem.title }}</text>
                <text class="notice-level">{{ noticeLevelLabel(noticeItem.noticeLevel) }}</text>
              </view>
              <rich-text class="notice-rich-content" :nodes="noticeItem.content || ''" />
            </view>
          </view>

          <view v-if="currentNoticeGroup.announcements.length" class="notice-group">
            <view class="notice-group-title">大赛公告</view>
            <view
              v-for="noticeItem in currentNoticeGroup.announcements"
              :key="noticeItem.noticeId"
              class="notice-item announcement"
              :class="noticeLevelClass(noticeItem.noticeLevel)"
            >
              <view class="notice-item-head">
                <text class="notice-item-title">{{ noticeItem.title }}</text>
                <text class="notice-level">{{ noticeLevelLabel(noticeItem.noticeLevel) }}</text>
              </view>
              <rich-text class="notice-rich-content" :nodes="noticeItem.content || ''" />
            </view>
          </view>
        </view>
      </view>

      <view class="scene-section">
        <view class="scene-section-head">
          <view>
            <view class="scene-section-title">赛场信息</view>
            <view class="scene-section-sub">{{ currentSceneCredentials.length }} 个赛场</view>
          </view>
        </view>

        <view v-if="currentSceneCredentials.length === 0" class="empty-card compact">暂无赛场证件</view>

        <view
          v-for="credential in currentSceneCredentials"
          :key="credentialKey(credential)"
          class="credential-card"
        >
          <view class="credential-head" @click="toggleCredential(credential)">
            <view class="credential-head-main">
              <view class="credential-title">{{ scheduleTitle(credential) }}</view>
              <!-- <view class="credential-sub">{{ sceneCardSub(credential) }}</view> -->
              <view class="credential-meta">
                {{ credentialMetaText(credential) }}
              </view>
            </view>
            <view class="credential-head-side">
              <view class="status-tag" :class="credentialStatusClass(credential.credentialStatus)">
                {{ credentialStatusLabel(credential.credentialStatus) }}
              </view>
              <view class="expand-arrow" :class="{ expanded: isCredentialExpanded(credential) }"></view>
            </view>
          </view>

          <view v-if="isCredentialExpanded(credential)" class="credential-body">
            <view class="info-section">
              <view class="section-title">参赛信息</view>
              <view class="info-row">
                <text>姓名</text>
                <text>{{ participantText(credential) }}</text>
              </view>
              <view v-if="credential.teamName" class="info-row">
                <text>团队名称</text>
                <text>{{ credential.teamName || '-' }}</text>
              </view>
              <view v-if="teamMemberTexts(credential).length" class="info-row">
                <text>队友</text>
                <text>{{ teamMemberTexts(credential).join('、') }}</text>
              </view>
              <view class="info-row">
                <text>赛道/组别</text>
                <text>{{ joinText([credential.competitionTrackName, credential.secondLevelName]) }}</text>
              </view>
              <view class="info-row">
                <text>学校/机构</text>
                <text>{{ schoolDisplayName(credential) || '-' }}</text>
              </view>
              <view class="info-row">
                <text>角色</text>
                <text>{{ targetRoleLabel(credential.competitionRoleName) }}</text>
              </view>
              <!-- <view class="info-row">
                <text>证件范围</text>
                <text>{{ scopeTypeLabel(credential.scopeType) }}</text>
              </view> -->
            </view>

            <view class="info-section">
              <view class="section-title">现场安排</view>
              <view class="info-row">
                <text>签到时间</text>
                <text>{{ formatRange(credential.reportStartTime, credential.reportEndTime) }}</text>
              </view>
              <view class="info-row">
                <text>签到地点</text>
                <text>{{ credential.reportLocation || '-' }}</text>
              </view>
              <view class="info-row">
                <text>比赛时间</text>
                <text>{{ formatRange(credential.contestStartTime, credential.contestEndTime) }}</text>
              </view>
              <view class="info-row">
                <text>赛场地点</text>
                <text>{{ joinText([credential.contestLocation, credential.contestRoom]) }}</text>
              </view>
              <view class="info-row">
                <text>候场安排</text>
                <text>{{ waitingText(credential) }}</text>
              </view>
              <view class="info-row">
                <text>资料领取</text>
                <text>{{ credential.materialLocation || '-' }}</text>
              </view>
            </view>

            <view v-if="noticeLines(credential.notice).length" class="info-section">
              <view class="section-title">注意事项</view>
              <view class="notice">
                <view
                  v-for="(line, index) in noticeLines(credential.notice)"
                  :key="credentialKey(credential) + '_notice_' + index"
                  class="notice-line"
                >
                  {{ line || ' ' }}
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { computed, getCurrentInstance, nextTick, ref, watch } from 'vue'
import { onReady, onShow } from '@dcloudio/uni-app'
import { listMySceneCredential } from '@/api/sceneCredential'
import { listMySceneNotice } from '@/api/sceneNotice'
import { createQrModules } from '@/utils/qrcode'

const qrCanvasSize = 220
const instance = getCurrentInstance()

const loading = ref(false)
const credentialList = ref([])
const noticeGroups = ref([])
const selectedCompetitionKey = ref('')
const expandedCredentialIds = ref([])

onShow(() => {
  loadCredentials()
})

onReady(() => {
  nextTick(() => {
    drawVisibleQrCodes()
  })
})

const competitionGroups = computed(() => {
  const groupMap = new Map()
  credentialList.value.forEach(credential => {
    const info = getCredentialMatchInfo(credential)
    const key = firstFilled(info.seriesId, info.competitionName, credential.credentialId)
    if (!key) return
    if (!groupMap.has(key)) {
      groupMap.set(key, {
        key,
        competitionSeriesId: info.seriesId,
        competitionName: info.competitionName || '现场证件',
        credentials: []
      })
    }
    const group = groupMap.get(key)
    if (group.competitionName === '现场证件' && info.competitionName) {
      group.competitionName = info.competitionName
    }
    group.credentials.push(credential)
  })
  return Array.from(groupMap.values()).map(group => {
    const competitionCredentials = group.credentials.filter(isCompetitionScope)
    const sceneCredentials = group.credentials.filter(item => !isCompetitionScope(item))
    return {
      ...group,
      competitionCredential: pickCompetitionCredential(competitionCredentials),
      sceneCredentials
    }
  })
})

const currentCompetition = computed(() => {
  return competitionGroups.value.find(item => item.key === selectedCompetitionKey.value) || competitionGroups.value[0] || null
})

const currentCompetitionCredential = computed(() => {
  return currentCompetition.value?.competitionCredential || null
})

const currentSceneCredentials = computed(() => {
  return currentCompetition.value?.sceneCredentials || []
})

const currentNoticeGroup = computed(() => {
  const competitionSeriesId = currentCompetition.value?.competitionSeriesId
  if (!competitionSeriesId) return null
  return noticeGroups.value.find(item => `${item.competitionSeriesId}` === `${competitionSeriesId}`) || null
})

const hasCurrentNotices = computed(() => {
  const group = currentNoticeGroup.value
  return Boolean(group && ((group.personalNotices || []).length || (group.announcements || []).length))
})

const currentNoticeCount = computed(() => {
  const group = currentNoticeGroup.value
  return (group?.personalNotices?.length || 0) + (group?.announcements?.length || 0)
})

const visibleQrCredentials = computed(() => {
  return [currentCompetitionCredential.value].filter(item => credentialQrValue(item))
})

watch(competitionGroups, () => {
  ensureSelection()
})

watch(visibleQrCredentials, () => {
  nextTick(() => {
    setTimeout(drawVisibleQrCodes, 60)
  })
})

async function loadCredentials() {
  loading.value = true
  try {
    const [credentialResult, noticeResult] = await Promise.allSettled([
      listMySceneCredential(),
      listMySceneNotice()
    ])
    credentialList.value = credentialResult.status === 'fulfilled' ? getResponseList(credentialResult.value) : []
    noticeGroups.value = noticeResult.status === 'fulfilled' ? getResponseList(noticeResult.value) : []
    ensureSelection()
  } finally {
    loading.value = false
  }
  await nextTick()
  setTimeout(drawVisibleQrCodes, 60)
}

function ensureSelection() {
  const groups = competitionGroups.value
  if (groups.length === 0) {
    selectedCompetitionKey.value = ''
    expandedCredentialIds.value = []
    return
  }
  let group = groups.find(item => item.key === selectedCompetitionKey.value)
  if (!group) {
    group = groups[0]
    selectedCompetitionKey.value = group.key
  }
  ensureExpandedCredentials(group)
}

function ensureExpandedCredentials(group) {
  const validIds = group.sceneCredentials.map(credentialKey)
  const validSet = new Set(validIds)
  const remainedIds = expandedCredentialIds.value.filter(item => validSet.has(item))
  if (remainedIds.length > 0 || validIds.length === 0) {
    expandedCredentialIds.value = remainedIds
    return
  }
  expandedCredentialIds.value = [validIds[0]]
}

function selectCompetition(group) {
  selectedCompetitionKey.value = group.key
  expandedCredentialIds.value = group.sceneCredentials[0] ? [credentialKey(group.sceneCredentials[0])] : []
  nextTick(() => {
    setTimeout(drawVisibleQrCodes, 60)
  })
}

function toggleCredential(credential) {
  const key = credentialKey(credential)
  const expandedSet = new Set(expandedCredentialIds.value)
  if (expandedSet.has(key)) {
    expandedSet.delete(key)
  } else {
    expandedSet.add(key)
  }
  expandedCredentialIds.value = Array.from(expandedSet)
}

function isCredentialExpanded(credential) {
  return expandedCredentialIds.value.includes(credentialKey(credential))
}

function pickCompetitionCredential(credentials) {
  return credentials.find(item => item.credentialStatus === 'EFFECTIVE') || credentials[0] || null
}

function getResponseList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.rows)) return res.rows
  if (Array.isArray(res?.data?.rows)) return res.data.rows
  return []
}

function normalizeKey(value) {
  if (value === null || value === undefined) return ''
  return `${value}`.trim()
}

function firstFilled(...values) {
  for (const value of values) {
    const normalized = normalizeKey(value)
    if (normalized) return normalized
  }
  return ''
}

function getCredentialSnapshot(credential) {
  if (!credential?.credentialSnapshotJson) return {}
  try {
    return JSON.parse(credential.credentialSnapshotJson) || {}
  } catch (error) {
    return {}
  }
}

function getCredentialMatchInfo(credential) {
  const snapshot = getCredentialSnapshot(credential)
  const schedule = snapshot.schedule || {}
  const target = snapshot.target || {}
  return {
    seriesId: firstFilled(
      credential?.competitionSeriesId,
      credential?.scopeType === 'COMPETITION' ? credential?.scopeRefId : '',
      snapshot.competitionSeriesId,
      schedule.competitionSeriesId,
      target.competitionSeriesId
    ),
    competitionName: firstFilled(
      credential?.competitionName,
      snapshot.competitionName,
      schedule.competitionName,
      target.competitionName
    )
  }
}

function credentialQrValue(credential) {
  return firstFilled(credential?.qrContent, credential?.credentialToken)
}

function credentialKey(credential) {
  return firstFilled(credential?.credentialId, credential?.credentialNo, credential?.credentialToken)
}

function credentialCanvasId(credential) {
  const key = credentialKey(credential).replace(/[^A-Za-z0-9_]/g, '_')
  return `credentialQrCanvas_${key || 'empty'}`
}

function drawVisibleQrCodes() {
  visibleQrCredentials.value.forEach(drawCredentialQr)
}

function drawCredentialQr(credential, callback) {
  const value = credentialQrValue(credential)
  if (!value) return
  try {
    const modules = createQrModules(value)
    const ctx = uni.createCanvasContext(credentialCanvasId(credential), instance?.proxy)
    const margin = 4
    const cellCount = modules.length + margin * 2
    const cellSize = qrCanvasSize / cellCount
    ctx.setFillStyle('#ffffff')
    ctx.fillRect(0, 0, qrCanvasSize, qrCanvasSize)
    ctx.setFillStyle('#111827')
    modules.forEach((row, y) => {
      row.forEach((isDark, x) => {
        if (!isDark) return
        ctx.fillRect(
          Math.round((x + margin) * cellSize),
          Math.round((y + margin) * cellSize),
          Math.ceil(cellSize),
          Math.ceil(cellSize)
        )
      })
    })
    ctx.draw(false, () => {
      if (typeof callback === 'function') callback()
    })
  } catch (error) {
    uni.showToast({ title: '二维码生成失败', icon: 'none' })
  }
}

function downloadCredentialQrCode(credential) {
  if (!credentialQrValue(credential)) {
    uni.showToast({ title: '暂无二维码', icon: 'none' })
    return
  }
  drawCredentialQr(credential, () => {
    uni.canvasToTempFilePath({
      canvasId: credentialCanvasId(credential),
      width: qrCanvasSize,
      height: qrCanvasSize,
      destWidth: qrCanvasSize * 4,
      destHeight: qrCanvasSize * 4,
      success: (res) => {
        saveQrImage(res.tempFilePath)
      },
      fail: () => {
        uni.showToast({ title: '生成图片失败', icon: 'none' })
      }
    }, instance?.proxy)
  })
}

function saveQrImage(filePath) {
  if (typeof uni.saveImageToPhotosAlbum !== 'function') {
    uni.previewImage({ urls: [filePath] })
    return
  }
  ensureAlbumPermission(() => {
    uni.saveImageToPhotosAlbum({
      filePath,
      success: () => {
        uni.showToast({ title: '已保存到相册', icon: 'success' })
      },
      fail: (error) => {
        handleSaveImageFail(error, filePath)
      }
    })
  })
}

function ensureAlbumPermission(callback) {
  if (!isWeixinMiniProgram() || typeof uni.getSetting !== 'function' || typeof uni.authorize !== 'function') {
    callback()
    return
  }
  uni.getSetting({
    success: (setting) => {
      const albumAuth = setting?.authSetting?.['scope.writePhotosAlbum']
      if (albumAuth === true) {
        callback()
        return
      }
      if (albumAuth === false) {
        promptOpenAlbumSetting()
        return
      }
      uni.authorize({
        scope: 'scope.writePhotosAlbum',
        success: callback,
        fail: promptOpenAlbumSetting
      })
    },
    fail: callback
  })
}

function promptOpenAlbumSetting() {
  uni.showModal({
    title: '无法保存',
    content: '请在设置中允许保存到相册后重试',
    confirmText: '去设置',
    cancelText: '取消',
    confirmColor: '#3169F8',
    success: (modalRes) => {
      if (modalRes.confirm && typeof uni.openSetting === 'function') {
        uni.openSetting()
      }
    }
  })
}

function handleSaveImageFail(error, filePath) {
  const msg = error?.errMsg || ''
  if (isAlbumAuthError(msg)) {
    promptOpenAlbumSetting()
    return
  }
  if (msg.includes('cancel')) {
    uni.showToast({ title: '已取消保存', icon: 'none' })
    return
  }
  if (typeof uni.previewImage === 'function') {
    uni.previewImage({ urls: [filePath] })
    return
  }
  uni.showToast({ title: '保存失败', icon: 'none' })
}

function isAlbumAuthError(msg) {
  return ['auth', 'authorize', 'permission', 'privacy', 'deny'].some(keyword => msg.includes(keyword))
}

function isWeixinMiniProgram() {
  return typeof wx !== 'undefined' && typeof wx.getSetting === 'function'
}

function credentialTypeLabel(value) {
  const map = {
    PARTICIPANT: '参赛证',
    COMPETITOR: '参赛证',
    TEACHER: '教师证',
    EXPERT: '专家证',
    STAFF: '工作人员证',
    VIP: '贵宾证',
    TEMP: '临时证'
  }
  return map[value] || value || '-'
}

function credentialDisplayName(credential) {
  return credential?.credentialName || credentialTypeLabel(credential?.credentialType)
}

function credentialMetaText(credential) {
  return [credentialDisplayName(credential), participantText(credential), credential?.credentialNo]
    .filter(item => item && item !== '-')
    .join(' · ') || '-'
}

function subjectName(credential) {
  return studentName(credential) || '-'
}

function studentName(credential) {
  const snapshot = getCredentialSnapshot(credential)
  const target = snapshot.target || {}
  return firstFilled(
    credential?.userName,
    target.userName,
    snapshot.userName,
    snapshot.subjectType === 'USER' ? snapshot.subjectName : ''
  )
}

function schoolDisplayName(credential) {
  const snapshot = getCredentialSnapshot(credential)
  const target = snapshot.target || {}
  return firstFilled(credential?.schoolName, target.schoolName, snapshot.schoolName, credential?.orgName, target.orgName)
}

function participantText(credential) {
  return personSchoolText(studentName(credential), schoolDisplayName(credential))
}

function personSchoolText(name, school) {
  const displayName = normalizeKey(name)
  const displaySchool = normalizeKey(school)
  if (displayName && displaySchool) return `${displayName}（${displaySchool}）`
  return displayName || displaySchool || '-'
}

function teamMemberTexts(credential) {
  return teamMembers(credential)
    .filter(member => !isCurrentParticipant(member, credential))
    .map(member => personSchoolText(memberName(member), memberSchool(member)))
    .filter(text => text !== '-')
}

function teamMembers(credential) {
  if (!credential) return []
  const snapshot = getCredentialSnapshot(credential)
  const target = snapshot.target || {}
  const candidates = [
    credential.teamMembers,
    credential.teamMemberList,
    credential.memberList,
    credential.members,
    credential.teammates,
    snapshot.teamMembers,
    snapshot.teamMemberList,
    snapshot.memberList,
    snapshot.members,
    target.teamMembers,
    target.teamMemberList,
    target.memberList,
    target.teamMemberRelaList,
    namesFromText(credential.userNames),
    namesFromText(credential.memberNames),
    namesFromText(snapshot.userNames),
    namesFromText(target.userNames)
  ]
  const memberMap = new Map()
  candidates.flatMap(normalizeMemberSource).forEach(member => {
    const name = memberName(member)
    if (!name) return
    const key = firstFilled(member.userId, member.memberId, `${name}_${memberSchool(member)}`)
    if (!memberMap.has(key)) memberMap.set(key, member)
  })
  return Array.from(memberMap.values())
}

function normalizeMemberSource(value) {
  if (!value) return []
  if (Array.isArray(value)) return value.flatMap(normalizeMemberSource)
  if (typeof value === 'string') return namesFromText(value)
  if (typeof value === 'object') return [value]
  return []
}

function namesFromText(value) {
  const text = normalizeKey(value)
  if (!text) return []
  return text.split(/[、,，/]/).map(item => normalizeKey(item)).filter(Boolean).map(userName => ({ userName }))
}

function memberName(member) {
  return firstFilled(member?.userName, member?.memberName, member?.name, member?.realName)
}

function memberSchool(member) {
  return firstFilled(member?.schoolName, member?.orgName, member?.school)
}

function isCurrentParticipant(member, credential) {
  if (!member || !credential) return false
  const memberId = firstFilled(member.memberId)
  const userId = firstFilled(member.userId)
  if (memberId && memberId === firstFilled(credential.memberId)) return true
  if (userId && userId === firstFilled(credential.userId)) return true
  return !memberId && !userId && memberName(member) && memberName(member) === studentName(credential)
}

function isCompetitionScope(credential) {
  return (credential?.scopeType || 'SCHEDULE') === 'COMPETITION'
}

function scopeTypeLabel(value) {
  const map = {
    COMPETITION: '大赛级',
    SCHEDULE: '赛场级',
    VIP: '贵宾',
    EXPERT: '专家',
    STAFF: '工作人员',
    TEMP: '临时'
  }
  return map[value || 'SCHEDULE'] || value || '-'
}

function competitionHeroSub(credential) {
  if (!credential) return '请联系现场工作人员生成或刷新证件'
  const participant = participantText(credential)
  return joinText([participant === '-' ? '' : participant, credential.teamName ? `团队：${credential.teamName}` : '', credential.credentialNo])
}

function competitionHeroTitle(group, credential) {
  const title = group?.competitionName || credential?.competitionName || '现场证件'
  const statusText = competitionDoneStatusText(credential)
  return statusText ? `${title}（${statusText}）` : title
}

function competitionDoneStatusText(credential) {
  const statuses = []
  if (isReportDone(credential)) statuses.push('已报到')
  if (isMaterialDone(credential)) statuses.push('已领取资料')
  return statuses.join('、')
}

function isReportDone(credential) {
  return isDoneValue(credential?.reportStatus) || isDoneValue(credential?.reportStateStatus)
}

function isMaterialDone(credential) {
  return isDoneValue(credential?.materialStatus) || isDoneValue(credential?.materialStateStatus)
}

function isDoneValue(value) {
  return normalizeKey(value) === '1'
}

function credentialStatusLabel(value) {
  const map = {
    EFFECTIVE: '有效',
    REVOKED: '已作废',
    EXPIRED: '已过期'
  }
  return map[value] || value || '-'
}

function credentialStatusClass(value) {
  if (value === 'EFFECTIVE') return 'success'
  if (value === 'REVOKED') return 'danger'
  return 'muted'
}

function targetRoleLabel(value) {
  const map = {
    TEACHER: '教师',
    MEMBER: '队员',
    EXPERT: '专家',
    CAPTAIN: '队长',
    MATERIAL_STAFF: '发资料工作人员',
    CHECKIN_STAFF: '签到工作人员',
    STAFF: '现场工作人员',
    VOLUNTEER: '志愿者'
  }
  return map[value] || value || '-'
}

function noticeLevelLabel(value) {
  const map = {
    NORMAL: '普通',
    IMPORTANT: '重要',
    URGENT: '紧急'
  }
  return map[value] || value || '普通'
}

function noticeLevelClass(value) {
  return `level-${(value || 'NORMAL').toLowerCase()}`
}

function formatRange(start, end) {
  if (!start && !end) return '-'
  return `${start || '-'} ~ ${end || '-'}`
}

function joinText(values) {
  return values.filter(Boolean).join(' / ') || '-'
}

function optionalJoinText(values) {
  return values.filter(Boolean).join(' / ')
}

function scheduleTitle(credential) {
  const snapshot = getCredentialSnapshot(credential)
  const schedule = snapshot.schedule || {}
  return firstFilled(
    schedule.scheduleName,
    credential?.scheduleName,
    optionalJoinText([credential?.competitionStageName, credential?.competitionTrackName, credential?.secondLevelName]),
    optionalJoinText([schedule.competitionStageName, schedule.competitionTrackName, schedule.secondLevelName]),
    credential?.contestLocation,
    credentialDisplayName(credential),
    '赛场证件'
  )
}

function sceneCardSub(credential) {
  const time = formatRange(credential.contestStartTime, credential.contestEndTime)
  const place = joinText([credential.contestLocation, credential.contestRoom])
  return [time === '-' ? '' : time, place === '-' ? '' : place].filter(Boolean).join(' · ') || '现场核验使用'
}

function waitingText(credential) {
  const time = formatRange(credential.waitingStartTime, credential.waitingEndTime)
  const place = credential.waitingLocation || ''
  const group = credential.waitingGroupName || credential.waitingGroupCode || ''
  return [time === '-' ? '' : place].filter(Boolean).join(' / ') || '-'
}

function normalizeNoticeText(value) {
  return normalizeKey(value)
    .replace(/\\r\\n/g, '\n')
    .replace(/\\n/g, '\n')
    .replace(/\\r/g, '\n')
    .replace(/\r\n?/g, '\n')
    .replace(/\t/g, '    ')
}

function noticeLines(value) {
  const text = normalizeNoticeText(value)
  return text ? text.split('\n') : []
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f6f7fb;
  padding: 24rpx;
}

.empty-card {
  margin-top: 24rpx;
  padding: 80rpx 30rpx;
  border-radius: 16rpx;
  background: #fff;
  color: #909399;
  text-align: center;
  font-size: 28rpx;
}

.empty-card.compact {
  margin-top: 18rpx;
  padding: 48rpx 30rpx;
}

.competition-scroll {
  margin-bottom: 24rpx;
  white-space: nowrap;
}

.competition-row {
  display: inline-flex;
  gap: 18rpx;
}

.competition-chip {
  width: 310rpx;
  padding: 22rpx;
  border-radius: 16rpx;
  border: 2rpx solid #e5e7eb;
  background: #fff;
  display: inline-flex;
  flex-direction: column;
  gap: 8rpx;
  vertical-align: top;
}

.competition-chip.active {
  border-color: #3169f8;
  box-shadow: 0 8rpx 22rpx rgba(49, 105, 248, 0.14);
}

.competition-chip-title {
  color: #111827;
  font-size: 28rpx;
  font-weight: 700;
  line-height: 38rpx;
  white-space: normal;
}

.competition-chip-sub {
  color: #6b7280;
  font-size: 24rpx;
}

.hero {
  padding: 30rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #1d4ed8, #2563eb 58%, #0f766e);
  box-shadow: 0 16rpx 34rpx rgba(37, 99, 235, 0.22);
  color: #fff;
}

.hero.reported {
  background: linear-gradient(135deg, #047857, #059669 58%, #0f766e);
  box-shadow: 0 16rpx 34rpx rgba(5, 150, 105, 0.22);
}

.hero.empty {
  background: #fff;
  box-shadow: none;
  border: 1rpx solid #e5e7eb;
  color: #111827;
}

.hero-head {
  display: flex;
  justify-content: space-between;
  gap: 22rpx;
}

.hero-copy {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.hero-card-title {
  display: flex;
  align-items: center;
  gap: 14rpx;
  color: #fff;
  min-width: 0;
}

.hero-card-logo {
  width: 72rpx;
  height: 72rpx;
  flex-shrink: 0;
}

.hero-card-title-text {
  flex: 1;
  min-width: 0;
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
  line-height: 38rpx;
  word-break: break-word;
}

.hero.empty .hero-card-title-text {
  color: #111827;
}

.hero.empty .hero-sub {
  color: #6b7280;
}

.hero-title {
  margin-top: 32rpx;
  color: #fff;
  font-size: 38rpx;
  font-weight: 700;
  line-height: 48rpx;
  word-break: break-word;
}

.hero.empty .hero-title {
  color: #111827;
}

.hero-sub {
  margin-top: 10rpx;
  color: rgba(255, 255, 255, 0.86);
  font-size: 24rpx;
  line-height: 34rpx;
  word-break: break-word;
}

.hero-members {
  margin-top: 14rpx;
}

.hero-members-label {
  color: rgba(255, 255, 255, 0.72);
  font-size: 24rpx;
  line-height: 34rpx;
}

.hero-members-list {
  margin-top: 4rpx;
  color: #fff;
  font-size: 24rpx;
  line-height: 34rpx;
  word-break: break-word;
}

.hero.empty .hero-members-label {
  color: #6b7280;
}

.hero.empty .hero-members-list {
  color: #111827;
}

.hero-role {
  height: 48rpx;
  line-height: 48rpx;
  padding: 0 20rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.18);
  border: 1rpx solid rgba(255, 255, 255, 0.35);
  color: #fff;
  font-size: 24rpx;
  flex-shrink: 0;
}

.hero-credential {
  margin-top: 28rpx;
}

.hero-qr-card {
  padding: 24rpx;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.12);
  border: 1rpx solid rgba(255, 255, 255, 0.18);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-qr-box {
  padding: 12rpx;
  border-radius: 16rpx;
  background: #fff;
  align-self: center;
  flex-shrink: 0;
}

.hero-qr-copy {
  width: 100%;
  margin-top: 20rpx;
  text-align: center;
}

.hero-qr-title {
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
  line-height: 40rpx;
}

.hero-qr-canvas {
  display: block;
  background: #fff;
}

.hero-qr-desc {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.86);
  font-size: 24rpx;
  line-height: 34rpx;
  text-align: center;
}

.hero-status-row {
  margin-top: 18rpx;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 14rpx;
}

.hero-status-row text {
  height: 48rpx;
  line-height: 48rpx;
  padding: 0 20rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.16);
  color: rgba(255, 255, 255, 0.82);
  font-size: 24rpx;
}

.hero-status-row text.done {
  background: #fff;
  color: #1d4ed8;
  font-weight: 700;
}

.hero.reported .hero-status-row text.done {
  color: #047857;
}

.hero-qr-missing {
  padding: 40rpx 28rpx;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.14);
  color: rgba(255, 255, 255, 0.86);
  font-size: 26rpx;
  line-height: 36rpx;
  text-align: center;
}

.hero-empty-text {
  margin-top: 28rpx;
  padding: 30rpx;
  border-radius: 14rpx;
  background: #f8fafc;
  color: #6b7280;
  font-size: 26rpx;
  text-align: center;
}

.hero-download-btn {
  width: 100%;
  height: 76rpx;
  line-height: 76rpx;
  margin: 28rpx 0 0;
  border-radius: 38rpx;
  background: #fff;
  color: #1d4ed8;
  font-size: 28rpx;
  font-weight: 700;
}

.hero.reported .hero-download-btn {
  color: #047857;
}

.hero-download-btn[disabled] {
  background: rgba(255, 255, 255, 0.5);
  color: rgba(29, 78, 216, 0.55);
}

.hero-notice-panel {
  margin-top: 28rpx;
  padding: 24rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.97);
  color: #1f2937;
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.12);
}

.hero-notice-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 18rpx;
  border-bottom: 1rpx solid #e5e7eb;
}

.hero-notice-title {
  color: #111827;
  font-size: 30rpx;
  font-weight: 700;
}

.hero-notice-count {
  padding: 4rpx 14rpx;
  border-radius: 18rpx;
  background: #eef2ff;
  color: #4338ca;
  font-size: 22rpx;
}

.notice-group {
  margin-top: 20rpx;
}

.notice-group-title {
  margin-bottom: 12rpx;
  color: #4b5563;
  font-size: 24rpx;
  font-weight: 700;
}

.notice-item {
  margin-top: 12rpx;
  padding: 20rpx;
  border-radius: 14rpx;
  border-left: 6rpx solid #94a3b8;
  background: #f8fafc;
}

.notice-item.personal {
  background: #f0f7ff;
  border-left-color: #3b82f6;
}

.notice-item.level-important {
  background: #fffbeb;
  border-left-color: #f59e0b;
}

.notice-item.level-urgent {
  background: #fff5f5;
  border-left-color: #ef4444;
}

.notice-item-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.notice-item-title {
  flex: 1;
  color: #111827;
  font-size: 27rpx;
  font-weight: 700;
  line-height: 38rpx;
  word-break: break-word;
}

.notice-level {
  flex-shrink: 0;
  padding: 4rpx 10rpx;
  border-radius: 8rpx;
  background: rgba(255, 255, 255, 0.86);
  color: #6b7280;
  font-size: 20rpx;
}

.notice-rich-content {
  display: block;
  margin-top: 12rpx;
  color: #374151;
  font-size: 25rpx;
  line-height: 1.7;
  word-break: break-word;
}

.scene-section {
  margin-top: 30rpx;
}

.scene-section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.scene-section-title {
  color: #111827;
  font-size: 32rpx;
  font-weight: 700;
  line-height: 42rpx;
}

.scene-section-sub {
  margin-top: 4rpx;
  color: #6b7280;
  font-size: 24rpx;
}

.credential-card {
  margin-top: 18rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.credential-head {
  padding: 26rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20rpx;
}

.credential-head-main {
  min-width: 0;
  flex: 1;
}

.credential-head-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 18rpx;
  flex-shrink: 0;
}

.credential-title {
  color: #111827;
  font-size: 32rpx;
  font-weight: 700;
  line-height: 42rpx;
  word-break: break-word;
}

.credential-sub {
  margin-top: 8rpx;
  color: #6b7280;
  font-size: 24rpx;
  line-height: 34rpx;
  word-break: break-word;
}

.credential-meta {
  margin-top: 8rpx;
  color: #94a3b8;
  font-size: 22rpx;
  line-height: 32rpx;
  word-break: break-word;
}

.credential-body {
  padding: 0 26rpx 28rpx;
  border-top: 1rpx solid #edf0f5;
}

.expand-arrow {
  width: 18rpx;
  height: 18rpx;
  margin-right: 12rpx;
  border-right: 3rpx solid #94a3b8;
  border-bottom: 3rpx solid #94a3b8;
  transform: rotate(45deg);
}

.expand-arrow.expanded {
  transform: rotate(-135deg);
}

.status-tag {
  height: 44rpx;
  line-height: 44rpx;
  padding: 0 18rpx;
  border-radius: 22rpx;
  font-size: 24rpx;
  flex-shrink: 0;
}

.status-tag.success {
  color: #059669;
  background: #ecfdf5;
}

.status-tag.danger {
  color: #dc2626;
  background: #fef2f2;
}

.status-tag.muted {
  color: #6b7280;
  background: #f3f4f6;
}

.info-section {
  margin-top: 28rpx;
}

.section-title {
  margin-bottom: 12rpx;
  color: #111827;
  font-size: 30rpx;
  font-weight: 700;
}

.info-row {
  padding: 18rpx 0;
  border-bottom: 1rpx solid #edf0f5;
  display: flex;
  justify-content: space-between;
  gap: 30rpx;
  align-items: flex-start;
  color: #6b7280;
  font-size: 26rpx;
  line-height: 36rpx;
}

.info-row text:first-child {
  flex-shrink: 0;
}

.info-row text:last-child {
  flex: 1;
  color: #111827;
  text-align: right;
  word-break: break-word;
}

.notice {
  color: #374151;
  font-size: 26rpx;
  line-height: 40rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 18rpx;
}

.notice-line {
  line-height: 40rpx;
  white-space: pre-wrap;
  word-break: break-word;
}

.notice-line + .notice-line {
  margin-top: 8rpx;
}

button::after {
  border: none;
}
</style>
