<template>
  <view class="container">
    <template v-if="mode === 'scene'">
      <view v-if="loading" class="state-block">
        <view class="spinner-circle"></view>
        <text class="state-title">扫码核验中</text>
      </view>

      <view v-else-if="scanStatus === 'fail'" class="state-block fail">
        <view class="state-icon">!</view>
        <text class="state-title">{{ detail.resultMessage || '扫码核验未通过' }}</text>
        <button class="primary-btn" @click="goBack">返回首页</button>
      </view>

      <template v-else>
        <view class="scene-header">
          <view>
            <text class="eyebrow">{{ resultLabel(detail.operationResult) }}</text>
            <text class="scene-title">{{ credential.competitionName || '现场证件' }}</text>
          </view>
          <view class="result-pill" :class="resultClass(detail.operationResult)">
            {{ resultLabel(detail.operationResult) }}
          </view>
        </view>

        <view class="info-panel">
          <view class="subject-line">
            <text class="subject-name">{{ credential.teamName || credential.userName || '-' }}</text>
            <text class="credential-no">{{ credential.credentialNo || '-' }}</text>
          </view>
		  <view class="meta-row">
		    <text>姓名</text>
		    <text>{{ credential.userName||'' }}</text>
		  </view>
		  <view class="meta-row">
		    <text>手机号</text>
		    <text>{{ credential.phone }}</text>
		  </view>
          <view class="meta-row">
            <text>证件类型</text>
            <text>{{ credentialDisplayName(credential) }}</text>
          </view>
          <view class="meta-row">
            <text>证件范围</text>
            <text>{{ scopeTypeLabel(credential.scopeType) }}</text>
          </view>
          <view class="meta-row">
            <text>扫码角色</text>
            <text>{{ detail.operatorRoleLabel || '-' }}</text>
          </view>
<!--          <view class="meta-row">
            <text>对象角色</text>
            <text>{{ detail.targetRoleLabel || targetRoleLabel(credential.competitionRoleName) }}</text>
          </view> -->
          <view class="meta-row">
            <text>赛道/组别</text>
            <text>{{ joinText([credential.competitionTrackName, credential.secondLevelName]) }}</text>
          </view>
          <view class="meta-row">
            <text>学校/机构</text>
            <text>{{ credential.schoolName || credential.orgName || '-' }}</text>
          </view>
        </view>

        <view class="status-grid">
          <view class="status-item" :class="{ done: isDoneStatus(credential.reportStatus) }">
            <text>报到</text>
            <text>{{ doneLabel(credential.reportStatus) }}</text>
          </view>
          <view class="status-item" :class="{ done: isDoneStatus(credential.materialStatus) }">
            <text>资料</text>
            <text>{{ doneLabel(credential.materialStatus) }}</text>
            <text v-if="materialDelegateText(credential)" class="status-extra">{{ materialDelegateText(credential) }}</text>
          </view>
          <view v-if="!isCompetitionScope(credential)" class="status-item" :class="{ done: isDoneStatus(credential.waitingStatus) }">
            <text>候场</text>
            <text>{{ doneLabel(credential.waitingStatus) }}</text>
          </view>
        </view>

        <view class="training-flow-panel">
          <view class="training-head">
            <view>
              <text class="section-title">工作人员操作流程</text>
              <text class="training-subtitle">用于现场扫码报到、资料发放培训</text>
            </view>
            <text class="training-badge">培训图</text>
          </view>

          <view class="flow-track">
            <view
              v-for="(step, index) in staffFlowSteps"
              :key="step.code"
              class="flow-step"
              :class="{ done: isFlowStepDone(step.key), active: isFlowStepActive(step.key) }"
            >
              <view class="flow-node">
                <text>{{ step.code }}</text>
              </view>
              <view class="flow-card">
                <view class="flow-card-head">
                  <text class="flow-title">{{ step.title }}</text>
                  <text class="flow-status">{{ flowStepStatusText(step.key) }}</text>
                </view>
                <text class="flow-desc">{{ step.desc }}</text>
                <view v-if="step.key === 'material'" class="flow-choice-row">
                  <text>本人领取</text>
                  <text>同队代领需再扫代领人证件</text>
                </view>
              </view>
              <view v-if="index < staffFlowSteps.length - 1" class="flow-connector"></view>
            </view>
          </view>

          <view class="exception-board">
            <view
              v-for="item in staffExceptionTips"
              :key="item.title"
              class="exception-item"
            >
              <text class="exception-title">{{ item.title }}</text>
              <text class="exception-desc">{{ item.desc }}</text>
            </view>
          </view>
        </view>

        <view v-if="sceneArrangementGroups.length > 0" class="info-panel">
          <view class="section-title">现场安排</view>
          <view
            v-for="(group, index) in sceneArrangementGroups"
            :key="arrangementGroupKey(group, index)"
            class="arrangement-group"
          >
            <view v-if="group.title" class="arrangement-title">{{ group.title }}</view>
            <view
              v-for="row in group.rows"
              :key="row.label"
              class="meta-row"
            >
              <text>{{ row.label }}</text>
              <text>{{ row.value }}</text>
            </view>
          </view>
        </view>

        <view class="action-panel">
          <view class="section-title">可执行操作</view>
          <view v-if="actions.length === 0 && scheduleActionGroups.length === 0" class="empty-action">
            {{ detail.matrixMessage || '当前暂无可执行操作' }}
          </view>

          <view v-if="competitionActions.length > 0" class="action-section">
            <view class="action-section-title">大赛操作</view>
            <view
              v-for="action in competitionActions"
              :key="actionKey(action)"
              class="action-row"
            >
              <view class="action-main">
                <text class="action-title">{{ action.actionLabel }}</text>
                <text class="action-desc">{{ action.message }}</text>
              </view>
              <button
                class="action-btn"
                :class="{ disabled: !action.enabled, cancel: isCancelAction(action) }"
                :disabled="!action.enabled || !!loadingAction"
                :loading="loadingAction === actionKey(action)"
                @click="handleAction(action)"
              >
                {{ actionButtonText(action) }}
              </button>
            </view>
          </view>

          <view
            v-for="(group, index) in scheduleActionGroups"
            :key="scheduleGroupKey(group, index)"
            class="schedule-action-card"
          >
            <view class="schedule-action-head">
              <view>
                <text class="schedule-action-title">{{ group.scheduleName || '赛场' }}</text>
                <text v-if="joinText([group.scheduleTime, group.scheduleLocation]) !== '-'" class="schedule-action-subtitle">
                  {{ joinText([group.scheduleTime, group.scheduleLocation]) }}
                </text>
              </view>
            </view>
            <view class="schedule-status-row">
              <view class="schedule-status" :class="{ done: isDoneStatus(group.reportStatus) }">
                <text>报到</text>
                <text>{{ doneLabel(group.reportStatus) }}</text>
              </view>
              <view class="schedule-status" :class="{ done: isDoneStatus(group.materialStatus) }">
                <text>资料</text>
                <text>{{ doneLabel(group.materialStatus) }}</text>
              </view>
              <view class="schedule-status" :class="{ done: isDoneStatus(group.waitingStatus) }">
                <text>候场</text>
                <text>{{ doneLabel(group.waitingStatus) }}</text>
              </view>
            </view>
            <view v-if="groupActions(group).length === 0" class="empty-schedule-action">暂无可执行操作</view>
            <view
              v-for="action in groupActions(group)"
              :key="actionKey(action)"
              class="action-row schedule-row"
            >
              <view class="action-main">
                <text class="action-title">{{ action.actionLabel }}</text>
                <text class="action-desc">{{ action.message }}</text>
              </view>
              <button
                class="action-btn"
                :class="{ disabled: !action.enabled, cancel: isCancelAction(action) }"
                :disabled="!action.enabled || !!loadingAction"
                :loading="loadingAction === actionKey(action)"
                @click="handleAction(action)"
              >
                {{ actionButtonText(action) }}
              </button>
            </view>
          </view>
        </view>

        <button class="ghost-btn" @click="goBack">完成</button>
      </template>
    </template>

    <template v-else>
      <view v-if="legacyStatus === 'success'" class="state-block success">
        <image src="https://www.ksup.cn/statics/wxApp/applyok.png" mode="widthFix" class="success-img"></image>
        <text class="state-title">{{ legacyDetail.userName }}，恭喜您，签到成功！</text>
        <view class="legacy-info">
          <view class="meta-row">
            <text>大赛名称</text>
            <text>{{ legacyDetail.competitionName || '-' }}</text>
          </view>
          <view class="meta-row">
            <text>签到人</text>
            <text>{{ legacyDetail.userName || '-' }}</text>
          </view>
          <view class="meta-row">
            <text>学校/场地</text>
            <text>{{ joinText([legacyDetail.schoolName, legacyDetail.examinationHall]) }}</text>
          </view>
          <view class="meta-row">
            <text>签到时间</text>
            <text>{{ legacyDetail.checkInTime || '-' }}</text>
          </view>
        </view>
        <button class="primary-btn" @click="goBack">确认</button>
      </view>

      <view v-else-if="legacyStatus === 'optimistic-success'" class="state-block success">
        <image src="https://www.ksup.cn/statics/wxApp/applyok.png" mode="widthFix" class="success-img"></image>
        <text class="state-title">已签到成功</text>
        <button class="primary-btn" @click="goBack">确认</button>
      </view>

      <view v-else-if="legacyStatus === 'fail'" class="state-block fail">
        <view class="state-icon">!</view>
        <text class="state-title">{{ legacyDetail.improperTitle || '暂不满足签到条件' }}</text>
        <text class="state-desc">{{ legacyDetail.improperDesc || '' }}</text>
        <button class="primary-btn" @click="goToQuery">确认信息</button>
      </view>

      <view v-else class="state-block">
        <view class="spinner-circle"></view>
        <text class="state-title">签到确认中</text>
      </view>
    </template>

    <view v-if="remarkDialog.visible" class="remark-mask">
      <view class="remark-dialog">
        <text class="remark-title">{{ remarkDialog.title }}</text>
        <text v-if="remarkDialog.message" class="remark-message">{{ remarkDialog.message }}</text>
        <textarea
          v-model="remarkDialog.remark"
          class="remark-input"
          maxlength="500"
          auto-height
          placeholder="备注（选填）"
          placeholder-class="remark-placeholder"
        />
        <view class="remark-count">{{ remarkDialog.remark.length }}/500</view>
        <view class="remark-actions">
          <button class="remark-cancel" :disabled="!!loadingAction" @click="closeRemarkDialog">取消</button>
          <button class="remark-confirm" :loading="!!loadingAction" :disabled="!!loadingAction" @click="submitRemarkDialog">确认</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getToken } from '@/utils/auth'
import { confirmSceneCredential, getSignResult, scanSceneCredential } from '@/api/scan'
import { formatDateTime } from '@/utils/date'
import { useUserStore } from '@/store/modules/user'

const token = ref(getToken())
const userStore = useUserStore()
const mode = ref('scene')
const loading = ref(false)
const loadingAction = ref('')
const scanStatus = ref('')
const detail = ref({})
const sceneValue = ref('')
const remarkDialog = ref({
  visible: false,
  action: null,
  extraPayload: {},
  title: '',
  message: '',
  remark: ''
})

const legacyStatus = ref('')
const legacyDetail = ref({})
const currentRid = ref('')
const isRequested = ref(false)

const staffFlowSteps = [
  {
    code: '01',
    key: 'scan',
    title: '扫描参赛证',
    desc: '工作人员打开扫一扫，对准参赛人或团队出示的现场证件二维码。'
  },
  {
    code: '02',
    key: 'verify',
    title: '核对人员信息',
    desc: '确认姓名/团队、证件类型、赛道组别、学校机构与现场人员一致。'
  },
  {
    code: '03',
    key: 'report',
    title: '确认报到',
    desc: '报到未完成时点击确认；已完成时不重复登记，按页面提示处理。'
  },
  {
    code: '04',
    key: 'material',
    title: '发放资料',
    desc: '资料未领取时发放物料并确认；如同队代领，先选择代领方式再扫码。'
  },
  {
    code: '05',
    key: 'finish',
    title: '完成放行',
    desc: '确认报到和资料状态无误后，提醒参赛人按现场安排进入下一环节。'
  }
]

const staffExceptionTips = [
  {
    title: '扫码未通过',
    desc: '核对是否扫错二维码，必要时请参赛人打开最新现场证件。'
  },
  {
    title: '无可执行操作',
    desc: '通常表示权限、场次或状态不匹配，按页面提示联系现场负责人处理。'
  },
  {
    title: '重复报到/领资料',
    desc: '不重复操作，查看完成时间和领取人，确认后放行或登记异常。'
  }
]

const credential = computed(() => detail.value?.credential || {})
const actions = computed(() => detail.value?.availableActions || [])
const competitionActions = computed(() => {
  const list = detail.value?.competitionActions
  if (Array.isArray(list) && list.length > 0) return list
  return actions.value.filter(action => !action?.scheduleId && !action?.targetCredentialId)
})
const scheduleActionGroups = computed(() => {
  const groups = detail.value?.scheduleActionGroups
  if (Array.isArray(groups)) return groups
  return buildScheduleGroupsFromActions(actions.value)
})
const sceneArrangementGroups = computed(() => {
  const scheduleGroups = scheduleActionGroups.value
    .map(buildScheduleArrangement)
    .filter(group => group.rows.length > 0)
  if (scheduleGroups.length > 0) return scheduleGroups

  const credentialArrangement = buildCredentialArrangement(credential.value)
  return credentialArrangement.rows.length > 0 ? [credentialArrangement] : []
})

onLoad((options) => {
  const scene = decodeScene(options)
  if (scene && scene.startsWith('rid_')) {
    mode.value = 'legacy'
    initLegacyScan(scene.substring(4), options.selectedUserId)
    return
  }
  mode.value = 'scene'
  initSceneScan(scene)
})

function decodeScene(options = {}) {
  const raw = options.scene || options.qrContent || options.q || ''
  try {
    return decodeURIComponent(raw)
  } catch (e) {
    return raw
  }
}

function initSceneScan(scene) {
  if (!scene) {
    scanStatus.value = 'fail'
    detail.value = { resultMessage: '无效的二维码' }
    return
  }
  sceneValue.value = scene
  if (!ensureLogin('/pages/scan/result?scene=' + encodeURIComponent(scene), '登录后即可使用现场扫码功能，是否前往登录？')) {
    return
  }
  fetchSceneMatrix()
}

async function fetchSceneMatrix() {
  loading.value = true
  scanStatus.value = ''
  try {
    const operatorInfo = await getOperatorInfo()
    const res = await scanSceneCredential({
      qrContent: sceneValue.value,
      operatorPhone: getOperatorPhone(operatorInfo),
      deviceInfo: getDeviceInfoText()
    })
    const data = res?.data || {}
    detail.value = data
    scanStatus.value = data.operationResult === 'PASS' ? 'success' : 'fail'
  } catch (e) {
    if (e === 401) return
    detail.value = { resultMessage: '扫码核验失败，请稍后重试' }
    scanStatus.value = 'fail'
  } finally {
    loading.value = false
  }
}

function handleAction(action) {
  if (!action) return
  if (action.actionKind === 'PROMPT') {
    uni.showModal({
      title: action.actionLabel || '提示',
      content: action.message || detail.value.reviewEntryMessage || '请进入专家评审入口继续操作',
      showCancel: false,
      confirmColor: '#3169F8'
    })
    return
  }
  if (!action.enabled) {
    uni.showToast({ title: action.message || '该操作当前不可执行', icon: 'none' })
    return
  }
  if (action.actionType === 'MATERIAL_RECEIVE') {
    handleMaterialAction(action)
    return
  }
  openRemarkDialog(action)
}

function handleMaterialAction(action) {
  uni.showActionSheet({
    itemList: ['本人领取', '同队代领'],
    success: async (res) => {
      if (res.tapIndex === 0) {
        openRemarkDialog(action)
        return
      }
      try {
        const scanRes = await scanDelegateCredential()
        openRemarkDialog(action, { delegateQrContent: scanRes.result || scanRes.scanResult || '' })
      } catch (e) {
        if (e?.errMsg && e.errMsg.indexOf('cancel') >= 0) return
        uni.showToast({ title: '代领人扫码失败', icon: 'none' })
      }
    }
  })
}

function scanDelegateCredential() {
  return new Promise((resolve, reject) => {
    uni.scanCode({
      onlyFromCamera: true,
      success: resolve,
      fail: reject
    })
  })
}

function openRemarkDialog(action, extraPayload = {}) {
  remarkDialog.value = {
    visible: true,
    action,
    extraPayload,
    title: action.actionLabel || '确认操作',
    message: action.message || '确认执行该现场操作？',
    remark: ''
  }
}

function closeRemarkDialog() {
  if (loadingAction.value) return
  remarkDialog.value = {
    visible: false,
    action: null,
    extraPayload: {},
    title: '',
    message: '',
    remark: ''
  }
}

async function submitRemarkDialog() {
  const action = remarkDialog.value.action
  if (!action) return
  const remark = normalizeRemark(remarkDialog.value.remark)
  const extraPayload = { ...(remarkDialog.value.extraPayload || {}) }
  if (remark) {
    extraPayload.remark = remark
  }
  const success = await confirmAction(action, extraPayload)
  if (success) {
    closeRemarkDialog()
  }
}

function normalizeRemark(value) {
  return String(value || '').trim().slice(0, 500)
}

async function confirmAction(action, extraPayload = {}) {
  loadingAction.value = actionKey(action)
  try {
    const currentCredential = credential.value || {}
    const operatorInfo = await getOperatorInfo()
    const res = await confirmSceneCredential({
      qrContent: sceneValue.value,
      operationType: action.actionType,
      scheduleId: action.scheduleId,
      targetCredentialId: action.targetCredentialId,
      receiverName: currentCredential.userName || currentCredential.teamName,
      receiverPhone: currentCredential.phone,
      receiverIdSuffix: currentCredential.idCardSuffix,
      operatorPhone: getOperatorPhone(operatorInfo),
      deviceInfo: getDeviceInfoText(),
      ...extraPayload
    })
    const data = res?.data || {}
    detail.value = data
    scanStatus.value = data.operationResult === 'FAIL' || data.operationResult === 'EXCEPTION' ? 'fail' : 'success'
    uni.showToast({ title: data.resultMessage || '操作完成', icon: 'none' })
    return true
  } catch (e) {
    if (e !== 401) {
      uni.showToast({ title: '操作失败，请稍后重试', icon: 'none' })
    }
    return false
  } finally {
    loadingAction.value = ''
  }
}

function initLegacyScan(rid, selectedUserId) {
  currentRid.value = rid
  if (!rid) {
    uni.showToast({ title: '无效的二维码', icon: 'none' })
    return
  }
  if (!ensureLogin('/pages/scan/result?scene=rid_' + rid, '登录后即可使用扫码签到功能，是否前往登录？')) {
    return
  }
  if (isRequested.value) return
  isRequested.value = true
  const existingRecord = uni.getStorageSync('pending_sign_in')
  if (!existingRecord || !existingRecord.rid || existingRecord.rid !== rid) {
    savePendingRecord(rid)
  }
  fetchLegacySignResult(rid, selectedUserId)
}

function fetchLegacySignResult(rid, selectedUserId) {
  const pendingRecord = uni.getStorageSync('pending_sign_in')
  let firstTime = pendingRecord && pendingRecord.firstTime
  if (!firstTime) {
    firstTime = formatDateTime(new Date())
  }
  const params = { recordId: rid, signTime: firstTime }
  if (selectedUserId) {
    params.selectedUserId = selectedUserId
  }
  getSignResult(params).then(res => {
    if (res.code === 200 && res.data) {
      const data = res.data
      legacyStatus.value = data.checkInFlag ? 'success' : 'fail'
      legacyDetail.value = data
      clearPendingRecord()
    } else {
      legacyStatus.value = 'optimistic-success'
    }
  }).catch((e) => {
    if (e && e === 401) return
    legacyStatus.value = 'optimistic-success'
  })
}

function ensureLogin(redirectUrl, content) {
  token.value = getToken()
  if (token.value) return true
  uni.setStorageSync('redirect_after_login', redirectUrl)
  uni.showModal({
    title: '提示',
    content,
    confirmText: '去登录',
    confirmColor: '#3169F8',
    cancelText: '去首页',
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({ url: '/pages/login' })
      } else {
        uni.switchTab({ url: '/pages/index/index' })
      }
    }
  })
  return false
}

function savePendingRecord(rid) {
  uni.setStorageSync('pending_sign_in', {
    rid,
    firstTime: formatDateTime(new Date()),
    status: 'pending'
  })
}

function clearPendingRecord() {
  uni.removeStorageSync('pending_sign_in')
}

function goBack() {
  uni.switchTab({ url: '/pages/index/index' })
}

function goToQuery() {
  uni.navigateTo({ url: '/pages/scan/query?rid=' + currentRid.value })
}

function getDeviceInfoText() {
  try {
    const info = uni.getSystemInfoSync()
    return [info.brand, info.model, info.system, info.platform].filter(Boolean).join(' / ')
  } catch (e) {
    return ''
  }
}

async function getOperatorInfo() {
  if (userStore.userInfo) {
    return userStore.userInfo
  }
  try {
    return await userStore.getUserInfo()
  } catch (e) {
    return {}
  }
}

function getOperatorPhone(info = {}) {
  return info.phone || info.phonenumber || info.phoneNumber || info.mobile || ''
}

function joinText(list) {
  const text = (list || []).filter(item => item !== undefined && item !== null && item !== '').join(' / ')
  return text || '-'
}

function formatRange(start, end) {
  if (start && end) return start + ' - ' + end
  return start || end || '-'
}

function doneLabel(value) {
  return isDoneStatus(value) ? '已完成' : '未完成'
}

function isDoneStatus(value) {
  return value === '1' || value === 'DONE'
}

function isFlowStepDone(key) {
  if (key === 'scan' || key === 'verify') return scanStatus.value === 'success'
  if (key === 'report') return isDoneStatus(credential.value.reportStatus)
  if (key === 'material') return isDoneStatus(credential.value.materialStatus)
  if (key === 'finish') {
    return isDoneStatus(credential.value.reportStatus) && isDoneStatus(credential.value.materialStatus)
  }
  return false
}

function isFlowStepActive(key) {
  if (isFlowStepDone(key)) return false
  if (key === 'report') return !isDoneStatus(credential.value.reportStatus)
  if (key === 'material') return isDoneStatus(credential.value.reportStatus) && !isDoneStatus(credential.value.materialStatus)
  if (key === 'finish') return isDoneStatus(credential.value.reportStatus) && isDoneStatus(credential.value.materialStatus)
  return false
}

function flowStepStatusText(key) {
  if (isFlowStepDone(key)) return '已完成'
  if (isFlowStepActive(key)) return '当前处理'
  return '待处理'
}

function resultLabel(value) {
  const map = {
    PASS: '通过',
    FAIL: '未通过',
    DUPLICATE: '重复',
    EXCEPTION: '异常'
  }
  return map[value] || '待核验'
}

function resultClass(value) {
  return {
    pass: value === 'PASS',
    duplicate: value === 'DUPLICATE',
    fail: value === 'FAIL' || value === 'EXCEPTION'
  }
}

function actionButtonText(action) {
  if (action.actionKind === 'PROMPT') return '查看'
  if (isCancelAction(action)) return '取消'
  if (action.status === 'DONE') return '已完成'
  return '确认'
}

function isCancelAction(action) {
  return String(action?.actionType || '').indexOf('CANCEL_') === 0
}

function actionKey(action) {
  return [
    action?.actionType || '',
    action?.targetCredentialId || '',
    action?.scheduleId || ''
  ].join('_')
}

function groupActions(group) {
  return Array.isArray(group?.actions) ? group.actions : []
}

function scheduleGroupKey(group, index) {
  return [group?.targetCredentialId || '', group?.scheduleId || '', index].join('_')
}

function arrangementGroupKey(group, index) {
  return [group?.targetCredentialId || '', group?.scheduleId || '', group?.title || '', index].join('_')
}

function buildScheduleArrangement(group = {}) {
  return {
    scheduleId: group.scheduleId,
    targetCredentialId: group.targetCredentialId,
    title: group.scheduleName || '赛场安排',
    rows: [
      filledRow('赛场时间', group.scheduleTime),
      filledRow('地点/候场', group.scheduleLocation),
      filledRow('资料领取', group.materialLocation)
    ].filter(Boolean)
  }
}

function buildCredentialArrangement(value = {}) {
  return {
    title: '',
    rows: [
      filledRow('签到时间', formatRange(value.reportStartTime, value.reportEndTime)),
      filledRow('签到地点', value.reportLocation),
      filledRow('比赛时间', formatRange(value.contestStartTime, value.contestEndTime)),
      filledRow('赛场地点', joinText([value.contestLocation, value.contestRoom])),
      filledRow('候场时间', formatRange(value.waitingStartTime, value.waitingEndTime)),
      // filledRow('候场地点', joinText([value.waitingLocation, value.waitingGroupName])),
      filledRow('资料领取', value.materialLocation)
    ].filter(Boolean)
  }
}

function filledRow(label, value) {
  if (value === undefined || value === null || value === '' || value === '-') return null
  return { label, value }
}

function buildScheduleGroupsFromActions(list = []) {
  const groupMap = new Map()
  list.forEach(action => {
    if (!action?.scheduleId && !action?.targetCredentialId) return
    const key = [action.targetCredentialId || '', action.scheduleId || ''].join('_')
    if (!groupMap.has(key)) {
      groupMap.set(key, {
        scheduleId: action.scheduleId,
        targetCredentialId: action.targetCredentialId,
        scheduleName: action.scheduleName,
        scheduleTime: action.scheduleTime,
        scheduleLocation: action.scheduleLocation,
        actions: []
      })
    }
    groupMap.get(key).actions.push(action)
  })
  return Array.from(groupMap.values())
}

function credentialTypeLabel(value) {
  const map = {
    PARTICIPANT: '参赛证',
    COMPETITOR: '参赛证',
    TEACHER: '教师证',
    EXPERT: '专家证',
    STAFF: '工作证',
    VIP: '贵宾证',
    TEMP: '临时证'
  }
  return map[value] || value || '-'
}

function credentialDisplayName(value) {
  return value?.credentialName || credentialTypeLabel(value?.credentialType)
}

function isCompetitionScope(value) {
  return (value?.scopeType || 'SCHEDULE') === 'COMPETITION'
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

function materialDelegateText(value) {
  if (!value || value.materialStatus !== '1' || !value.materialDelegateName) return ''
  const prefix = value.materialDelegateRelation === 'TEAM_MEMBER' ? '代领人' : '领取人'
  const time = value.materialStateTime || value.materialTime || ''
  return `${prefix}：${value.materialDelegateName}${time ? ' · ' + time : ''}`
}

function targetRoleLabel(value) {
  const map = {
    TEACHER: '教师',
    MEMBER: '队员',
    EXPERT: '专家',
    CAPTAIN: '队长',
    MATERIAL_STAFF: '资料工作人员',
    CHECKIN_STAFF: '签到工作人员',
    STAFF: '现场工作人员',
    VOLUNTEER: '志愿者'
  }
  return map[value] || value || '-'
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f6f8fb;
  padding: 32rpx 28rpx 56rpx;
  box-sizing: border-box;
}

.state-block {
  min-height: 70vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 22rpx;
}

.state-block.fail .state-title {
  color: #d93026;
}

.state-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #ffb020;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 54rpx;
  font-weight: 700;
}

.success-img {
  width: 108rpx;
  height: 108rpx;
}

.state-title {
  font-size: 34rpx;
  color: #1f2937;
  font-weight: 600;
  line-height: 1.45;
}

.state-desc {
  font-size: 28rpx;
  color: #667085;
  line-height: 1.6;
}

.spinner-circle {
  width: 82rpx;
  height: 82rpx;
  border: 7rpx solid #e5eaf2;
  border-top-color: #3169f8;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.scene-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24rpx;
  margin-bottom: 24rpx;
}

.eyebrow {
  display: block;
  font-size: 24rpx;
  color: #3169f8;
  margin-bottom: 8rpx;
}

.scene-title {
  display: block;
  font-size: 40rpx;
  color: #101828;
  font-weight: 700;
  line-height: 1.3;
}

.result-pill {
  min-width: 108rpx;
  height: 52rpx;
  line-height: 52rpx;
  border-radius: 26rpx;
  text-align: center;
  font-size: 25rpx;
  color: #667085;
  background: #eef2f6;
  flex-shrink: 0;
}

.result-pill.pass {
  color: #067647;
  background: #dcfae6;
}

.result-pill.duplicate {
  color: #b54708;
  background: #fef0c7;
}

.result-pill.fail {
  color: #b42318;
  background: #fee4e2;
}

.info-panel,
.action-panel,
.legacy-info {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 36rpx rgba(16, 24, 40, 0.06);
}

.subject-line {
  margin-bottom: 20rpx;
}

.subject-name {
  display: block;
  font-size: 34rpx;
  color: #111827;
  font-weight: 700;
  line-height: 1.4;
}

.credential-no {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #667085;
}

.section-title {
  font-size: 30rpx;
  color: #101828;
  font-weight: 700;
  margin-bottom: 18rpx;
}

.action-section {
  border-bottom: 1rpx solid #eef2f6;
  margin-bottom: 8rpx;
}

.action-section-title {
  font-size: 25rpx;
  color: #3169f8;
  font-weight: 600;
  margin-bottom: 4rpx;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #eef2f6;
  font-size: 27rpx;
  color: #667085;
  line-height: 1.45;
}

.meta-row:last-child {
  border-bottom: 0;
}

.meta-row text:last-child {
  color: #111827;
  text-align: right;
  flex: 1;
}

.arrangement-group + .arrangement-group {
  margin-top: 22rpx;
  padding-top: 22rpx;
  border-top: 1rpx solid #eef2f6;
}

.arrangement-title {
  display: block;
  font-size: 28rpx;
  color: #101828;
  font-weight: 600;
  line-height: 1.45;
  margin-bottom: 4rpx;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.status-item {
  height: 116rpx;
  border-radius: 14rpx;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  color: #667085;
  font-size: 26rpx;
  box-shadow: 0 10rpx 28rpx rgba(16, 24, 40, 0.05);
}

.status-item.done {
  color: #067647;
  background: #ecfdf3;
}

.status-extra {
  max-width: 100%;
  font-size: 20rpx;
  color: #667085;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.training-flow-panel {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 36rpx rgba(16, 24, 40, 0.06);
}

.training-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
  margin-bottom: 22rpx;
}

.training-subtitle {
  display: block;
  margin-top: 4rpx;
  font-size: 24rpx;
  color: #667085;
  line-height: 1.45;
}

.training-head .section-title {
  display: block;
  margin-bottom: 0;
}

.training-badge {
  flex-shrink: 0;
  height: 44rpx;
  line-height: 44rpx;
  padding: 0 18rpx;
  border-radius: 22rpx;
  background: #eef4ff;
  color: #3169f8;
  font-size: 23rpx;
  font-weight: 600;
}

.flow-track {
  position: relative;
}

.flow-step {
  position: relative;
  display: flex;
  align-items: stretch;
  gap: 18rpx;
}

.flow-step + .flow-step {
  margin-top: 18rpx;
}

.flow-node {
  position: relative;
  width: 58rpx;
  height: 58rpx;
  border-radius: 50%;
  background: #eef2f6;
  color: #667085;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
  flex-shrink: 0;
  margin-top: 14rpx;
  z-index: 1;
}

.flow-step.done .flow-node {
  background: #12b76a;
  color: #ffffff;
}

.flow-step.active .flow-node {
  background: #3169f8;
  color: #ffffff;
}

.flow-card {
  flex: 1;
  min-width: 0;
  border: 1rpx solid #e4e7ec;
  border-radius: 14rpx;
  padding: 18rpx 20rpx;
  background: #fbfcfe;
}

.flow-step.done .flow-card {
  border-color: #abefc6;
  background: #f6fef9;
}

.flow-step.active .flow-card {
  border-color: #b2ccff;
  background: #f5f8ff;
}

.flow-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.flow-title {
  font-size: 28rpx;
  color: #101828;
  font-weight: 700;
  line-height: 1.4;
}

.flow-status {
  flex-shrink: 0;
  font-size: 22rpx;
  color: #667085;
  line-height: 1.45;
}

.flow-step.done .flow-status {
  color: #067647;
}

.flow-step.active .flow-status {
  color: #3169f8;
}

.flow-desc {
  display: block;
  font-size: 24rpx;
  color: #667085;
  line-height: 1.55;
}

.flow-choice-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 14rpx;
}

.flow-choice-row text {
  min-height: 48rpx;
  border-radius: 10rpx;
  background: #ffffff;
  border: 1rpx solid #e4e7ec;
  padding: 10rpx 12rpx;
  box-sizing: border-box;
  color: #475467;
  font-size: 22rpx;
  line-height: 1.35;
}

.flow-connector {
  position: absolute;
  left: 28rpx;
  top: 72rpx;
  bottom: -22rpx;
  width: 2rpx;
  background: #d0d5dd;
}

.flow-step.done .flow-connector {
  background: #abefc6;
}

.exception-board {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12rpx;
  margin-top: 24rpx;
  padding-top: 22rpx;
  border-top: 1rpx solid #eef2f6;
}

.exception-item {
  border-radius: 12rpx;
  background: #fffaf0;
  border: 1rpx solid #fedf89;
  padding: 16rpx 18rpx;
}

.exception-title {
  display: block;
  font-size: 25rpx;
  color: #93370d;
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 4rpx;
}

.exception-desc {
  display: block;
  font-size: 23rpx;
  color: #7a2e0e;
  line-height: 1.5;
}

.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #eef2f6;
}

.action-row:last-child {
  border-bottom: 0;
}

.schedule-action-card {
  border: 1rpx solid #e4e7ec;
  border-radius: 14rpx;
  padding: 22rpx;
  margin-top: 20rpx;
  background: #fbfcfe;
}

.schedule-action-head {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  margin-bottom: 18rpx;
}

.schedule-action-title {
  display: block;
  font-size: 30rpx;
  color: #101828;
  font-weight: 700;
  line-height: 1.4;
}

.schedule-action-subtitle {
  display: block;
  margin-top: 6rpx;
  font-size: 23rpx;
  color: #667085;
  line-height: 1.45;
}

.schedule-status-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.schedule-status {
  min-height: 76rpx;
  border-radius: 12rpx;
  background: #ffffff;
  border: 1rpx solid #eef2f6;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  color: #667085;
  font-size: 22rpx;
}

.schedule-status.done {
  color: #067647;
  background: #ecfdf3;
  border-color: #abefc6;
}

.schedule-row {
  padding: 20rpx 0 0;
  margin-top: 18rpx;
}

.empty-schedule-action {
  padding-top: 18rpx;
  font-size: 24rpx;
  color: #98a2b3;
}

.remark-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 99;
  background: rgba(16, 24, 40, 0.48);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  box-sizing: border-box;
}

.remark-dialog {
  width: 100%;
  border-radius: 18rpx;
  background: #ffffff;
  padding: 34rpx 30rpx 28rpx;
  box-sizing: border-box;
}

.remark-title {
  display: block;
  font-size: 32rpx;
  color: #101828;
  font-weight: 700;
  line-height: 1.4;
}

.remark-message {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #667085;
  line-height: 1.55;
}

.remark-input {
  width: 100%;
  min-height: 160rpx;
  margin-top: 24rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  border: 1rpx solid #d0d5dd;
  background: #ffffff;
  color: #101828;
  font-size: 27rpx;
  line-height: 1.5;
  box-sizing: border-box;
}

.remark-placeholder {
  color: #98a2b3;
}

.remark-count {
  margin-top: 8rpx;
  text-align: right;
  font-size: 22rpx;
  color: #98a2b3;
}

.remark-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
  margin-top: 24rpx;
}

.remark-cancel,
.remark-confirm {
  height: 78rpx;
  line-height: 78rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.remark-cancel {
  color: #344054;
  background: #ffffff;
  border: 1rpx solid #d0d5dd;
}

.remark-confirm {
  color: #ffffff;
  background: #3169f8;
}

.action-main {
  min-width: 0;
  flex: 1;
}

.action-title {
  display: block;
  font-size: 30rpx;
  color: #101828;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.action-desc {
  display: block;
  font-size: 24rpx;
  color: #667085;
  line-height: 1.45;
}

.action-schedule {
  margin-top: 12rpx;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  color: #475467;
  font-size: 23rpx;
  line-height: 1.4;
}

.action-btn {
  width: 136rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 32rpx;
  background: #3169f8;
  color: #ffffff;
  font-size: 26rpx;
  padding: 0;
  flex-shrink: 0;
}

.action-btn.disabled {
  background: #d0d5dd;
  color: #ffffff;
}

.action-btn.cancel {
  background: #d92d20;
}

.action-btn::after,
.primary-btn::after,
.ghost-btn::after {
  border: 0;
}

.empty-action {
  padding: 28rpx 0 6rpx;
  font-size: 27rpx;
  color: #667085;
  line-height: 1.5;
}

.primary-btn,
.ghost-btn {
  width: 420rpx;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  margin: 32rpx auto 0;
}

.primary-btn {
  background: #3169f8;
  color: #ffffff;
}

.ghost-btn {
  background: #ffffff;
  color: #3169f8;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
