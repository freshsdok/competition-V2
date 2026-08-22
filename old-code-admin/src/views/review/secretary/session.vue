<template>
  <div class="secretary-page">
    <div class="session-banner">
      <div>
        <div class="session-name">{{ sessionInfo.sessionName || '现场评审控制台' }}</div>
        <div class="session-meta">
          <span>{{ sessionInfo.location || '未设置场地' }}</span>
          <span>{{ sessionInfo.activityName || '未关联活动' }}</span>
          <span>{{ sessionInfo.roundName || '未关联轮次' }}</span>
        </div>
      </div>
      <el-tag :type="sessionStatusType(sessionInfo.status)" size="large">
        {{ sessionStatusLabel(sessionInfo.status) }}
      </el-tag>
    </div>

    <section class="current-panel">
      <div class="section-title">当前评审对象</div>
      <div v-if="sessionInfo.currentObjectId" class="current-object">
        <div>
          <div class="object-code">{{ sessionInfo.currentObjectCode }}</div>
          <div class="object-name">{{ sessionInfo.currentObjectName }}</div>
          <div class="time-line">开始时间：{{ sessionInfo.currentStartedTime || '-' }}</div>
        </div>
        <el-button type="primary" size="large" plain @click="refreshAll">刷新</el-button>
      </div>
      <el-empty v-else description="暂无当前评审对象" :image-size="72" />
    </section>

    <section class="scan-panel">
      <div class="section-title">扫码识别参赛证</div>
      <el-input
        v-model.trim="certificateCode"
        size="large"
        placeholder="输入或粘贴参赛证编号"
        clearable
        @keyup.enter="handleResolveCertificate"
      >
        <template #append>
          <el-button :loading="resolving" @click="handleResolveCertificate">解析</el-button>
        </template>
      </el-input>
      <div class="scan-actions">
        <el-button type="primary" plain icon="Camera" size="large" @click="openCameraScanner">打开摄像头扫码</el-button>
      </div>
      <div class="scan-tip">扫码后只展示识别结果，需秘书二次确认后才会切换当前对象；手机浏览器无法调用摄像头时，可使用证件编号模拟扫码。</div>

      <div v-if="resolveResult" class="resolve-box">
        <div class="resolve-head">
          <span>识别结果</span>
          <el-tag>{{ resolveCandidates.length }} 个候选</el-tag>
        </div>
        <el-alert v-if="resolveResult.warningMessage" :title="resolveResult.warningMessage" type="warning" show-icon :closable="false" />
        <el-radio-group v-model="selectedObjectId" class="candidate-list">
          <label
            v-for="candidate in resolveCandidates"
            :key="`${candidate.objectId}-${candidate.certificateCode}`"
            class="candidate-card"
            :class="{ selected: selectedObjectId === candidate.objectId }"
          >
            <el-radio :label="candidate.objectId" />
            <div class="candidate-body">
              <div class="object-code">{{ candidate.objectCode }}</div>
              <div class="object-name">{{ candidate.objectName }}</div>
              <div class="candidate-meta">
                <span>{{ candidate.memberName || '-' }}</span>
                <span>{{ memberRoleLabel(candidate.memberRole) }}</span>
                <span>{{ candidate.certificateCode }}</span>
              </div>
              <el-alert
                v-if="candidate.warningMessage"
                :title="candidate.warningMessage"
                type="warning"
                show-icon
                :closable="false"
              />
            </div>
          </label>
        </el-radio-group>
        <el-button
          class="full-button"
          type="primary"
          size="large"
          :disabled="!selectedObjectId"
          :loading="switching"
          @click="confirmSetCurrentFromScan"
        >
          设为当前评审对象
        </el-button>
      </div>
    </section>

    <el-dialog
      v-model="cameraDialogOpen"
      title="摄像头扫码"
      width="92%"
      class="camera-dialog"
      append-to-body
      @closed="stopCameraScanner"
    >
      <div class="camera-scanner">
        <video ref="cameraVideoRef" class="camera-video" playsinline muted />
        <div class="camera-frame">
          <span></span>
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
      <el-alert
        :title="scannerMessage || '请将参赛证二维码置于取景框内，识别后会自动解析，但不会自动切换当前对象。'"
        :type="scannerMessage ? 'warning' : 'info'"
        show-icon
        :closable="false"
        class="mt12"
      />
      <template #footer>
        <el-button @click="cameraDialogOpen = false">关闭</el-button>
      </template>
    </el-dialog>

    <section class="action-panel">
      <el-button type="success" size="large" :loading="switching" @click="handleNextObject">下一位</el-button>
      <el-button size="large" @click="refreshAll">刷新列表</el-button>
    </section>

    <section class="object-list-panel">
      <div class="section-title">场次对象顺序</div>
      <div v-loading="loading" class="session-object-list">
        <el-empty v-if="!loading && sessionObjects.length === 0" description="暂无场次对象" :image-size="72" />
        <article
          v-for="item in sessionObjects"
          :key="item.sessionObjectId"
          class="session-object-card"
          :class="{ current: item.objectId === sessionInfo.currentObjectId }"
        >
          <div class="card-top">
            <div class="sequence">#{{ item.sequenceNo || '-' }}</div>
            <div class="object-main">
              <div class="object-code">{{ item.objectCode }}</div>
              <div class="object-name">{{ item.objectName }}</div>
              <div class="object-sub">
                <span>{{ item.orgName || '-' }}</span>
                <span>负责人：{{ item.leaderName || '-' }}</span>
              </div>
            </div>
            <el-tag v-if="item.objectId === sessionInfo.currentObjectId" type="danger">当前答辩中</el-tag>
          </div>
          <div class="status-row">
            <el-tag :type="checkinTagType(item.checkinStatus)">{{ checkinLabel(item.checkinStatus) }}</el-tag>
            <el-tag :type="reviewTagType(item.reviewStatus)">{{ reviewStatusLabel(item.reviewStatus) }}</el-tag>
            <span class="progress">评分 {{ item.scoreProgress?.displayText || '0/0' }}</span>
          </div>
          <div class="button-grid">
            <el-button type="primary" plain @click="handleSetCurrentManual(item)">设为当前</el-button>
            <el-button type="success" plain @click="handleStatus(item, { checkinStatus: 'PRESENT' })">到场</el-button>
            <el-button type="warning" plain @click="handleStatus(item, { checkinStatus: 'ABSENT' })">缺席</el-button>
            <el-button plain @click="handleStatus(item, { reviewStatus: 'SKIPPED' })">跳过</el-button>
            <el-button plain @click="handleStatus(item, { reviewStatus: 'DELAYED' })">延后</el-button>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { resolveCertificate } from '@/api/review/object'
import {
  getSecretarySession,
  listSecretarySessionObjects,
  nextSecretaryObject,
  setSecretaryCurrentObject,
  updateSecretarySessionObjectStatus
} from '@/api/review/secretary'

const route = useRoute()
const sessionId = computed(() => route.params.sessionId)
const loading = ref(false)
const resolving = ref(false)
const switching = ref(false)
const certificateCode = ref('')
const resolveResult = ref(null)
const selectedObjectId = ref(undefined)
const sessionObjects = ref([])
const cameraDialogOpen = ref(false)
const cameraVideoRef = ref(null)
const cameraStream = ref(null)
const scannerMessage = ref('')
const barcodeDetector = ref(null)
const scanTimer = ref(null)
const scanning = ref(false)
const sessionInfo = reactive({
  sessionId: undefined,
  sessionName: '',
  sessionCode: '',
  location: '',
  activityId: undefined,
  activityName: '',
  roundId: undefined,
  roundName: '',
  status: '',
  currentObjectId: undefined,
  currentObjectCode: '',
  currentObjectName: '',
  currentStartedTime: ''
})

const resolveCandidates = computed(() => {
  const result = resolveResult.value
  if (!result) {
    return []
  }
  if (Array.isArray(result.candidates) && result.candidates.length > 0) {
    return result.candidates
  }
  if (result.objectId) {
    return [result]
  }
  return []
})

function refreshAll() {
  loadSession()
  loadObjects()
}

function loadSession() {
  return getSecretarySession(sessionId.value).then(res => {
    Object.assign(sessionInfo, res.data || {})
  })
}

function loadObjects() {
  loading.value = true
  return listSecretarySessionObjects(sessionId.value).then(res => {
    sessionObjects.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

function handleResolveCertificate() {
  if (!certificateCode.value) {
    ElMessage.warning('请输入参赛证编号')
    return
  }
  if (!sessionInfo.activityId) {
    ElMessage.warning('场次活动信息尚未加载')
    return
  }
  resolving.value = true
  resolveCertificate({
    activityId: sessionInfo.activityId,
    sessionId: sessionId.value,
    certificateCode: certificateCode.value
  }).then(res => {
    resolveResult.value = res.data
    const candidates = resolveCandidates.value
    selectedObjectId.value = candidates.length === 1 ? candidates[0].objectId : undefined
    if (candidates.length === 0) {
      ElMessage.warning('未解析到可用评审对象')
    }
  }).finally(() => {
    resolving.value = false
  })
}

async function openCameraScanner() {
  scannerMessage.value = ''
  if (!window.isSecureContext) {
    scannerMessage.value = '当前页面不是安全上下文，手机浏览器通常会禁止调用摄像头；请使用 HTTPS、localhost 或手动输入证件编号。'
    ElMessage.warning(scannerMessage.value)
    return
  }
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    scannerMessage.value = '当前浏览器不支持摄像头访问，请使用证件编号兜底输入。'
    ElMessage.warning(scannerMessage.value)
    return
  }
  if (!('BarcodeDetector' in window)) {
    scannerMessage.value = '当前浏览器不支持原生二维码识别，请使用证件编号兜底输入。'
    ElMessage.warning(scannerMessage.value)
    return
  }
  try {
    let formats = ['qr_code']
    if (window.BarcodeDetector.getSupportedFormats) {
      const supportedFormats = await window.BarcodeDetector.getSupportedFormats()
      if (Array.isArray(supportedFormats) && supportedFormats.length > 0 && !supportedFormats.includes('qr_code')) {
        scannerMessage.value = '当前浏览器的原生扫码能力不支持二维码格式，请使用证件编号兜底输入。'
        ElMessage.warning(scannerMessage.value)
        return
      }
      formats = supportedFormats.includes('qr_code') ? ['qr_code'] : formats
    }
    barcodeDetector.value = new window.BarcodeDetector({ formats })
    cameraDialogOpen.value = true
    await nextTick()
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: false,
      video: {
        facingMode: { ideal: 'environment' },
        width: { ideal: 1280 },
        height: { ideal: 720 }
      }
    })
    cameraStream.value = stream
    if (cameraVideoRef.value) {
      cameraVideoRef.value.srcObject = stream
      await cameraVideoRef.value.play()
    }
    scanning.value = true
    scanNextFrame()
  } catch (error) {
    stopCameraScanner()
    scannerMessage.value = error?.name === 'NotAllowedError'
      ? '摄像头权限被拒绝，请授权后重试，或使用证件编号兜底输入。'
      : '摄像头扫码启动失败，请使用证件编号兜底输入。'
    ElMessage.warning(scannerMessage.value)
  }
}

function scanNextFrame() {
  if (!scanning.value || !barcodeDetector.value || !cameraVideoRef.value) {
    return
  }
  barcodeDetector.value.detect(cameraVideoRef.value).then(results => {
    if (Array.isArray(results) && results.length > 0) {
      const rawValue = results[0].rawValue || results[0].displayValue || ''
      if (rawValue) {
        certificateCode.value = rawValue.trim()
        cameraDialogOpen.value = false
        stopCameraScanner()
        ElMessage.success('已识别证件编号，请确认候选评审对象')
        handleResolveCertificate()
        return
      }
    }
    scanTimer.value = window.setTimeout(scanNextFrame, 280)
  }).catch(() => {
    scanTimer.value = window.setTimeout(scanNextFrame, 500)
  })
}

function stopCameraScanner() {
  scanning.value = false
  if (scanTimer.value) {
    window.clearTimeout(scanTimer.value)
    scanTimer.value = null
  }
  if (cameraVideoRef.value) {
    cameraVideoRef.value.pause()
    cameraVideoRef.value.srcObject = null
  }
  if (cameraStream.value) {
    cameraStream.value.getTracks().forEach(track => track.stop())
    cameraStream.value = null
  }
  barcodeDetector.value = null
}

function confirmSetCurrentFromScan() {
  setCurrentObject(selectedObjectId.value, 'SCAN', certificateCode.value)
}

function handleSetCurrentManual(item) {
  ElMessageBox.confirm(`确认将 ${item.objectCode || ''} ${item.objectName || ''} 设为当前评审对象？`, '确认切换', {
    type: 'warning'
  }).then(() => {
    setCurrentObject(item.objectId, 'MANUAL')
  }).catch(() => {})
}

function setCurrentObject(objectId, sourceType, code) {
  if (!objectId) {
    ElMessage.warning('请选择评审对象')
    return
  }
  switching.value = true
  setSecretaryCurrentObject(sessionId.value, {
    objectId,
    sourceType,
    certificateCode: code
  }).then(() => {
    ElMessage.success('已设置当前评审对象')
    resolveResult.value = null
    selectedObjectId.value = undefined
    refreshAll()
  }).finally(() => {
    switching.value = false
  })
}

function handleNextObject() {
  ElMessageBox.confirm('确认切换到下一位可评审对象？', '下一位', {
    type: 'warning'
  }).then(() => {
    switching.value = true
    nextSecretaryObject(sessionId.value).then(() => {
      ElMessage.success('已切换下一位')
      refreshAll()
    }).finally(() => {
      switching.value = false
    })
  }).catch(() => {})
}

function handleStatus(item, statusData) {
  const label = statusData.checkinStatus ? checkinLabel(statusData.checkinStatus) : reviewStatusLabel(statusData.reviewStatus)
  ElMessageBox.confirm(`确认将 ${item.objectCode || ''} ${item.objectName || ''} 标记为“${label}”？`, '状态确认', {
    type: 'warning'
  }).then(() => {
    updateSecretarySessionObjectStatus(item.sessionObjectId, statusData).then(() => {
      ElMessage.success('状态已更新')
      refreshAll()
    })
  }).catch(() => {})
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

function sessionStatusType(status) {
  return status === 'IN_PROGRESS' ? 'success' : status === 'PAUSED' ? 'warning' : status === 'ENDED' ? 'info' : ''
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

function checkinTagType(status) {
  return status === 'PRESENT' ? 'success' : status === 'ABSENT' ? 'danger' : status === 'LATE' ? 'warning' : ''
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

function reviewTagType(status) {
  return status === 'REVIEWING' ? 'success' : status === 'SKIPPED' ? 'danger' : status === 'DELAYED' ? 'warning' : status === 'COMPLETED' ? 'info' : ''
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

onMounted(() => {
  refreshAll()
})

onBeforeUnmount(() => {
  stopCameraScanner()
})
</script>

<style scoped>
.secretary-page {
  min-height: calc(100vh - 84px);
  padding: 14px;
  background: #f5f7fb;
  color: #1f2937;
}

.session-banner,
.current-panel,
.scan-panel,
.object-list-panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.05);
}

.session-banner {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding: 16px;
}

.session-name {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.35;
}

.session-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.current-panel,
.scan-panel,
.object-list-panel {
  margin-top: 12px;
  padding: 14px;
}

.section-title {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 700;
}

.current-object {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px;
  border-radius: 8px;
  background: #ecfdf5;
}

.object-code {
  color: #475569;
  font-size: 12px;
  font-weight: 700;
}

.object-name {
  margin-top: 4px;
  color: #111827;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.35;
  word-break: break-word;
}

.time-line,
.scan-tip {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
}

.scan-actions {
  margin-top: 10px;
}

.scan-actions .el-button {
  width: 100%;
  min-height: 44px;
}

.resolve-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

.resolve-head,
.card-top,
.status-row,
.action-panel {
  display: flex;
  align-items: center;
}

.resolve-head {
  justify-content: space-between;
  font-weight: 700;
}

.candidate-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.candidate-card,
.session-object-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.candidate-card {
  display: flex;
  gap: 8px;
  padding: 12px;
}

.candidate-card.selected {
  border-color: #409eff;
  background: #eef6ff;
}

.candidate-body {
  flex: 1;
  min-width: 0;
}

.candidate-meta,
.object-sub {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

.full-button {
  width: 100%;
}

.action-panel {
  gap: 10px;
  margin-top: 12px;
}

.action-panel .el-button {
  flex: 1;
  min-height: 44px;
}

.session-object-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.session-object-card {
  padding: 12px;
}

.session-object-card.current {
  border-color: #f56c6c;
  background: #fff7f7;
}

.card-top {
  gap: 10px;
}

.sequence {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 8px;
  background: #eef2ff;
  color: #3730a3;
  font-weight: 800;
  flex: 0 0 auto;
}

.object-main {
  flex: 1;
  min-width: 0;
}

.status-row {
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.progress {
  color: #475569;
  font-size: 13px;
}

.button-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.button-grid .el-button {
  min-height: 40px;
  margin-left: 0;
}

.camera-scanner {
  position: relative;
  overflow: hidden;
  width: 100%;
  aspect-ratio: 3 / 4;
  border-radius: 8px;
  background: #111827;
}

.camera-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.camera-frame {
  position: absolute;
  inset: 18%;
  border: 1px solid rgba(255, 255, 255, 0.42);
  border-radius: 10px;
  pointer-events: none;
}

.camera-frame span {
  position: absolute;
  width: 28px;
  height: 28px;
  border-color: #67c23a;
  border-style: solid;
}

.camera-frame span:nth-child(1) {
  top: -1px;
  left: -1px;
  border-width: 4px 0 0 4px;
}

.camera-frame span:nth-child(2) {
  top: -1px;
  right: -1px;
  border-width: 4px 4px 0 0;
}

.camera-frame span:nth-child(3) {
  right: -1px;
  bottom: -1px;
  border-width: 0 4px 4px 0;
}

.camera-frame span:nth-child(4) {
  bottom: -1px;
  left: -1px;
  border-width: 0 0 4px 4px;
}

.mt12 {
  margin-top: 12px;
}

@media (min-width: 820px) {
  .secretary-page {
    max-width: 760px;
    margin: 0 auto;
  }

  .button-grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}
</style>
