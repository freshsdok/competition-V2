<script setup>
import { computed, nextTick, onMounted, onBeforeUnmount, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { startTeamCollection, confirmTeamCollection, releaseTeamCollection } from '@/api/teamCollection'
import { getMyNativeCollections, enterNativeCollection } from '@/api/teamCollectionNative'
import { isAllowedNativeCollectionEntry } from '@/utils/nativeCollectionEntry'
import { createNativeProgressNonce, parseNativeProgressMessage, verifyNativeSettlementProgress } from '@/utils/nativeCollectionProgress'

const teams = ref([]), loading = ref(true), busy = ref(false), message = ref(''), unavailable = ref(false)
const frame = ref(null), frameElement = ref(null), progress = ref(null), verified = ref(false)
const pageRoot = ref(null), navigationInset = ref(null)
const checkingProgress = ref(false), guide = ref(false), frameHeight = ref(1100)
const step = computed(() => progress.value?.phase === 'provided' ? 3 : progress.value?.phase === 'review' ? 2 : 1)
let poll, channel, disposed = false, loadSequence = 0, progressSequence = 0
let lastGuidedReceipt = ''
let navigationHeader, navigationObserver, scrollingRoot, previousScrollPadding, previousScrollPriority, appliedScrollPadding
const adaptedShells = []

function installResponsiveShell() {
  // Both App and mainLayout impose desktop minimum widths. Adapt only this route's own ancestors.
  for (let element = pageRoot.value?.parentElement; element; element = element.parentElement) {
    if (element.matches('.app, .app-glb-container') && !element.classList.contains('native-collection-shell')) {
      element.classList.add('native-collection-shell')
      adaptedShells.push(element)
    }
  }
}
function restoreResponsiveShell() {
  adaptedShells.splice(0).forEach(element => element.classList.remove('native-collection-shell'))
}

function updateNavigationInset() {
  if (disposed || !navigationHeader || !scrollingRoot) return
  const height = navigationHeader.getBoundingClientRect().height
  if (!Number.isFinite(height)) return
  navigationInset.value = `${Math.ceil(Math.max(0, height)) + 12}px`
  appliedScrollPadding = navigationInset.value
  scrollingRoot.style.setProperty('scroll-padding-top', appliedScrollPadding, previousScrollPriority)
}
function installNavigationInset() {
  // Only the current route borrows the host document's scroll offset. The iframe shares that scroll root.
  const host = pageRoot.value?.closest('.app-glb-container')
  navigationHeader = host && Array.from(host.children).find(element => element.tagName === 'HEADER')
  if (!navigationHeader) return
  scrollingRoot = document.scrollingElement || document.documentElement
  previousScrollPadding = scrollingRoot.style.getPropertyValue('scroll-padding-top')
  previousScrollPriority = scrollingRoot.style.getPropertyPriority('scroll-padding-top')
  updateNavigationInset()
  if (typeof ResizeObserver !== 'undefined') {
    navigationObserver = new ResizeObserver(updateNavigationInset)
    navigationObserver.observe(navigationHeader)
  }
}
function restoreNavigationInset() {
  navigationObserver?.disconnect()
  // Do not overwrite a later owner's change when this route leaves.
  if (scrollingRoot && scrollingRoot.style.getPropertyValue('scroll-padding-top') === appliedScrollPadding
    && scrollingRoot.style.getPropertyPriority('scroll-padding-top') === previousScrollPriority) {
    if (previousScrollPadding) scrollingRoot.style.setProperty('scroll-padding-top', previousScrollPadding, previousScrollPriority)
    else scrollingRoot.style.removeProperty('scroll-padding-top')
  }
}

function time(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('zh-CN', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit',
    day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }).format(new Date(value))
}
function stillHeld(opened) {
  return teams.value.some(row => row.id === opened.teamId && row.status === 'IN_PROGRESS' && row.heldByMe && row.version === opened.version)
}
function invalidateProgress(closeGuide = true) {
  ++progressSequence
  verified.value = false
  checkingProgress.value = false
  if (closeGuide) guide.value = false
}
function closeFrame() {
  invalidateProgress()
  progress.value = null
  frame.value = null
  lastGuidedReceipt = ''
}
async function refresh() {
  const sequence = ++loadSequence
  try {
    const response = await getMyNativeCollections()
    if (disposed || sequence !== loadSequence) return false
    if (!Array.isArray(response?.data)) throw new Error('INVALID_STATUS')
    teams.value = response.data
    unavailable.value = false
    if (frame.value && !stillHeld(frame.value)) {
      closeFrame()
      message.value = '本队办理状态已变化，填报窗口已关闭。'
    }
    return true
  } catch {
    if (disposed || sequence !== loadSequence) return false
    closeFrame()
    unavailable.value = true
    message.value = '暂时无法核实办理状态，请刷新后继续。原办理名额不会自动释放。'
    return false
  } finally {
    if (!disposed && sequence === loadSequence) loading.value = false
  }
}
function connectFrame() {
  if (!frame.value || !frameElement.value?.contentWindow) return
  frameElement.value.contentWindow.postMessage({ type: 'deshi:native-settlement:connect', protocolVersion: 1, channel: frame.value.nonce }, window.location.origin)
}
function onFrameLoad() {
  if (!frame.value) return
  invalidateProgress()
  progress.value = null
  frame.value.nonce = createNativeProgressNonce()
  connectFrame()
}
async function open(row) {
  closeFrame()
  const response = await enterNativeCollection(row.id, row.version)
  if (disposed) return
  const entryPath = response?.data?.entryPath
  if (!isAllowedNativeCollectionEntry(entryPath)) throw new Error('INVALID_ENTRY')
  const opened = { teamId: row.id, version: row.version, entryPath, nonce: createNativeProgressNonce() }
  if (await refresh() && stillHeld(opened)) {
    frameHeight.value = 1100
    frame.value = opened
  }
}
async function showGuide() {
  if (!verified.value || !frame.value || busy.value) return
  guide.value = true
  await nextTick()
  if (!guide.value) return
  const button = document.getElementById('collection-complete-' + frame.value.teamId)
  button?.closest('.team-summary')?.scrollIntoView?.({ block: 'center', behavior: 'instant' })
  button?.focus({ preventScroll: true })
}
function hideGuide() {
  guide.value = false
  document.getElementById('collection-complete-' + frame.value?.teamId)?.focus({ preventScroll: true })
}
function onKeydown(event) {
  if (!guide.value) return
  if (event.key === 'Escape') { event.preventDefault(); hideGuide(); return }
  if (event.key !== 'Tab') return
  const targets = [document.getElementById('collection-complete-' + frame.value?.teamId), document.getElementById('collection-guide-dismiss')].filter(Boolean)
  const index = targets.indexOf(document.activeElement)
  event.preventDefault()
  targets[(index + (event.shiftKey ? targets.length - 1 : 1)) % targets.length]?.focus({ preventScroll: true })
}
// iframe 消息只提示需要核对。启用按钮之前，独立读取本人资料并重新核对队伍办理权。
async function checkProgress(candidate, offerGuide = true, background = false) {
  const opened = frame.value
  if (!opened || !stillHeld(opened)) return false
  // Window focus can precede a pointer click. Keep an already verified action available
  // during its background recheck; clicking it still starts a separate fresh verification.
  const retainVerified = background === true && verified.value && candidate === progress.value
  invalidateProgress(offerGuide || busy.value)
  verified.value = retainVerified
  const sequence = progressSequence, nonce = opened.nonce
  progress.value = candidate
  checkingProgress.value = true
  const current = () => !disposed && sequence === progressSequence && frame.value === opened && opened.nonce === nonce && progress.value === candidate
  try {
    await verifyNativeSettlementProgress(opened.entryPath, candidate)
    if (!current() || !await refresh() || !current() || !stillHeld(opened)) return false
    verified.value = true
    message.value = ''
    const receipt = [opened.teamId, nonce, candidate.profileVersion, candidate.tenantId, candidate.accessVersion].join(':')
    if (offerGuide && receipt !== lastGuidedReceipt) {
      lastGuidedReceipt = receipt
      void showGuide()
    }
    return true
  } catch {
    if (current()) {
      verified.value = false
      guide.value = false
      message.value = '暂未核实资料提供结果。请刷新状态重试，或在下方更新已保存状态后核对接收单位。'
    }
    return false
  } finally {
    if (sequence === progressSequence) checkingProgress.value = false
  }
}
function onFrameMessage(event) {
  const opened = frame.value, target = frameElement.value?.contentWindow
  if (!opened || !target || event.source !== target || event.origin !== window.location.origin || event.data?.protocolVersion !== 1) return
  if (event.data.type === 'deshi:native-settlement:ready') { connectFrame(); return }
  if (event.data.channel !== opened.nonce) return
  if (event.data.type === 'deshi:native-settlement:height') {
    if (Number.isFinite(event.data.height)) frameHeight.value = Math.min(2800, Math.max(980, Math.ceil(event.data.height)))
    return
  }
  if (event.data.type === 'deshi:native-settlement:guide') { void showGuide(); return }
  const candidate = parseNativeProgressMessage(event, target, opened.nonce)
  if (!candidate) return
  if (candidate.phase === 'provided') {
    if (JSON.stringify(candidate) === JSON.stringify(progress.value) && (verified.value || checkingProgress.value)) return
    // reactive ref 会代理对象，后续异步比较使用 ref 中的同一实例。
    progress.value = candidate
    void checkProgress(progress.value)
  } else {
    invalidateProgress()
    progress.value = candidate
  }
}
async function act(row, action) {
  if (busy.value || unavailable.value || (action === 'confirm' && (!verified.value || frame.value?.teamId !== row.id))) return
  busy.value = true
  message.value = ''
  try {
    if (action === 'confirm') {
      const opened = frame.value, candidate = progress.value
      // 此按钮本身就是办理人的最终确认；点击后再次核验，避免确认旧版本或其他队伍。
      if (!await checkProgress(candidate, false) || frame.value !== opened || !verified.value || !stillHeld(opened)) return
      await confirmTeamCollection(row.id, opened.version)
      if (disposed) return
      closeFrame()
      channel?.postMessage({ teamId: row.id })
      await refresh()
      if (!unavailable.value) message.value = '已记录您的完成确认，本队填报入口已关闭。'
    } else if (action === 'release') {
      try {
        await ElMessageBox.confirm('请先停止填写并关闭其他填报窗口。放弃后其他队员可以办理；本操作不会删除您已保存或提供的资料。', '放弃本次办理', {
          confirmButtonText: '放弃并释放名额', cancelButtonText: '继续办理', type: 'warning',
        })
      } catch { return }
      closeFrame()
      await releaseTeamCollection(row.id, row.version)
      channel?.postMessage({ teamId: row.id })
      await refresh()
      if (!unavailable.value) message.value = '已放弃本次办理，其他队员可以申请。'
    } else {
      let selected = row
      if (action === 'start') {
        const response = await startTeamCollection(row.id, row.version)
        if (disposed) return
        selected = response.data
        channel?.postMessage({ teamId: row.id })
      }
      await open(selected)
    }
  } catch {
    invalidateProgress()
    const refreshed = await refresh()
    if (refreshed) {
      message.value = action === 'confirm' && teams.value.some(team => team.id === row.id && team.status === 'USER_CONFIRMED_COMPLETE')
        ? '最新队伍状态：本队已确认完成。'
        : '操作未完成，请查看最新队伍状态。当前页面内容仍保留，可更新状态后重试。'
    }
  } finally { busy.value = false }
}
async function refreshAll(background = false) {
  if (busy.value || checkingProgress.value) return
  if (progress.value?.phase === 'provided') await checkProgress(progress.value, false, background)
  else await refresh()
}
function onFocus() { void refreshAll(true) }
function onResize() { updateNavigationInset(); if (guide.value) void showGuide() }
onMounted(() => {
  installResponsiveShell()
  installNavigationInset()
  refresh()
  poll = setInterval(() => { if (!document.hidden) void refreshAll(true) }, 15000)
  window.addEventListener('focus', onFocus)
  window.addEventListener('message', onFrameMessage)
  window.addEventListener('keydown', onKeydown)
  window.addEventListener('resize', onResize)
  if (typeof BroadcastChannel !== 'undefined') {
    channel = new BroadcastChannel('v1-team-collection')
    channel.onmessage = (event) => {
      if (frame.value?.teamId === event.data?.teamId) closeFrame()
      refresh()
    }
  }
})
onBeforeUnmount(() => {
  disposed = true
  restoreNavigationInset()
  restoreResponsiveShell()
  ++loadSequence
  clearInterval(poll)
  channel?.close()
  window.removeEventListener('focus', onFocus)
  window.removeEventListener('message', onFrameMessage)
  window.removeEventListener('keydown', onKeydown)
  window.removeEventListener('resize', onResize)
  closeFrame()
  // 离开、刷新、断网都不自动释放；办理人再次进入可以继续。
})
</script>

<template>
  <section ref="pageRoot" class="team-collection" :style="navigationInset ? { '--collection-top-inset': navigationInset } : undefined">
    <header class="page-header">
      <div><p class="eyebrow">获奖团队 · 结算办理</p><h1>银行卡资料填报</h1><p class="page-description">每队一人，使用本人账号和银行卡。</p></div>
      <button class="secondary refresh-button" :disabled="busy || checkingProgress" @click="refreshAll()">刷新状态</button>
    </header>
    <details class="instructions" open>
      <summary>填报须知 <span>首次办理建议阅读</span></summary>
      <p><strong>以团队为发奖单位，每队仅采集首位办理人的银行卡信息。一人完成后，其他队员不能再填报。</strong></p>
      <ol>
        <li>先使用与原平台办理人一致的本人账号二次登录；未认领账号选择“原账号 / 密码”。</li>
        <li>填写后点击“保存并继续”。如出现安全验证，按提示验证手机验证码；未绑定手机号可验证密码。</li>
        <li>选择大赛指定的接收单位，勾选同意复选框，再点击“确认提供”。</li>
        <li>提供成功后，点击上方“我已完成填报”，完成本队最终确认。</li>
      </ol>
      <p class="hint">本次入口有效30分钟。关闭页面不会释放名额，可稍后继续；不再办理时，请在“更多操作”中主动放弃。</p>
    </details>
    <p v-if="message" role="status" class="notice">{{ message }}</p>
    <p v-if="loading" class="empty-state" role="status">正在查询办理资格…</p>
    <p v-else-if="!unavailable && !teams.length" class="empty-state">您当前没有可查看的获奖队伍填报记录。</p>
    <article v-for="row in teams" :key="row.id" class="team-card">
      <div class="team-summary" :class="{ 'tour-active': guide && frame?.teamId === row.id, 'form-working': frame?.teamId === row.id && progress?.phase === 'unavailable' }">
        <div class="summary-main">
          <div><h2>{{ row.teamName || row.teamCode }}</h2><p class="team-details">{{ row.teamCode }}<span v-if="row.awardLevel"> · {{ row.awardLevel }}</span><span v-if="row.handlerName"> · 办理人：{{ row.handlerName }}</span></p></div>
          <span v-if="row.status === 'AVAILABLE'" class="status-tag">待填报</span>
          <span v-else-if="row.status === 'IN_PROGRESS'" class="status-tag in-progress">{{ row.heldByMe ? (verified && frame?.teamId === row.id ? '待最终确认' : '由您办理中') : '队员办理中' }}</span>
          <span v-else-if="row.status === 'USER_CONFIRMED_COMPLETE'" class="status-tag completed">已确认完成</span>
        </div>
        <div v-if="row.status === 'IN_PROGRESS' && row.heldByMe" class="completion-control">
          <button :id="'collection-complete-' + row.id" class="complete-button" :disabled="busy || unavailable || !verified || frame?.teamId !== row.id" @click="act(row, 'confirm')">{{ busy && frame?.teamId === row.id ? '正在处理…' : '我已完成填报' }}</button>
          <small>{{ verified && frame?.teamId === row.id ? '还差最后一步，点击完成团队确认' : checkingProgress && frame?.teamId === row.id ? '正在核对资料提供结果…' : '请先在下方完成“确认提供”' }}</small>
        </div>
        <aside v-if="guide && frame?.teamId === row.id" class="tour-bubble" role="dialog" aria-labelledby="collection-guide-title" aria-describedby="collection-guide-body">
          <p class="eyebrow">最后一步 · 团队确认</p><h3 id="collection-guide-title">点击这里，才算办完</h3>
          <p id="collection-guide-body">资料已提供。请点击上方高亮的“我已完成填报”，确认本队办理完成。</p>
          <button id="collection-guide-dismiss" class="text-button" @click="hideGuide">稍后确认，收起引导</button>
        </aside>
      </div>
      <div class="team-body">
        <template v-if="row.status === 'AVAILABLE'">
          <p class="team-status">本队尚未开始办理，请由一名队员提供本人银行卡信息。</p>
          <button :disabled="busy || unavailable" @click="act(row, 'start')">申请办理并进入填报</button>
        </template>
        <template v-else-if="row.status === 'IN_PROGRESS'">
          <template v-if="row.heldByMe">
            <ol v-if="frame?.teamId === row.id" class="progress-steps" aria-label="填报进度">
              <li v-for="(label, index) in ['填写银行卡', '确认提供', '完成填报']" :key="label" :class="{ active: step === index + 1, done: step > index + 1 }" :aria-current="step === index + 1 ? 'step' : undefined"><span>{{ step > index + 1 ? '✓' : index + 1 }}</span>{{ label }}</li>
            </ol>
            <div v-if="frame?.teamId !== row.id" class="resume-panel"><p>办理名额已为您保留，继续上次填报。</p><button :disabled="busy || unavailable" @click="act(row, 'open')">继续填报</button></div>
            <details class="more-actions"><summary>更多操作</summary><div class="team-actions"><button v-if="frame?.teamId === row.id" class="secondary" :disabled="busy || unavailable" @click="act(row, 'open')">重新进入填报</button><button class="secondary" :disabled="busy || unavailable" @click="act(row, 'release')">放弃本次办理，释放名额</button></div><p class="hint">{{ time(row.startedAt) }} 开始办理。重新进入需重新核对资料；放弃不会删除已保存或提供的资料。</p></details>
          </template>
          <template v-else><p class="team-status">{{ row.handlerName }} 于 {{ time(row.startedAt) }} 开始办理。</p><p class="hint">本队正在办理，暂不能进入填报。完成后，其他队员无需重复填报。</p></template>
        </template>
        <div v-else-if="row.status === 'USER_CONFIRMED_COMPLETE'" class="completion-receipt"><span class="receipt-check" aria-hidden="true">✓</span><h3>本队已完成填报</h3><p>{{ row.handlerName }} 已于 {{ time(row.confirmedAt) }} 确认完成收款资料填报。</p><p class="hint">此状态由办理人确认，本队填报入口已关闭。结算进度以大赛通知为准。</p></div>
        <p v-else class="hint">状态暂不可识别，请联系工作人员。</p>
        <div v-if="frame?.teamId === row.id" class="collection-window">
          <iframe :key="frame.entryPath" :ref="el => { frameElement = el }" :src="frame.entryPath" :style="{ height: frameHeight + 'px' }" title="本人收款资料填报" :inert="guide || busy"
            sandbox="allow-forms allow-scripts allow-same-origin allow-downloads allow-popups" referrerpolicy="same-origin" @load="onFrameLoad" />
        </div>
      </div>
    </article>
    <p class="time-note">本页面显示时间均为北京时间。</p>
    <Teleport to="body"><div v-if="guide" class="collection-tour-backdrop" aria-hidden="true" @click="hideGuide" /></Teleport>
  </section>
</template>

<style scoped>
.team-collection{box-sizing:border-box;max-width:1160px;margin:auto;padding:28px 24px;color:#253347;font-size:16px;line-height:1.7}
.page-header{display:flex;align-items:center;justify-content:space-between;gap:20px;margin-bottom:22px}.eyebrow{margin:0 0 6px;color:#3169c8;font-size:12px;font-weight:700;letter-spacing:.09em}h1{margin:0 0 6px;font-size:28px;line-height:1.4}h2{margin:0;font-size:20px;line-height:1.5}h3{margin:6px 0 10px;font-size:22px}.page-description{margin:0;color:#637083}
.instructions{margin-bottom:22px;padding:14px 20px;border:1px solid #dbe4f1;border-radius:10px;background:#f7f9fd}summary{cursor:pointer;font-weight:600;list-style:revert}summary:focus-visible{outline:3px solid #97b8ed;outline-offset:3px}.instructions summary span{margin-left:12px;color:#7b8799;font-size:13px;font-weight:400}.instructions p{margin:16px 0 10px}.instructions ol{list-style:decimal;padding-left:1.6em;font-size:18px;font-weight:700;line-height:1.85}.instructions li{margin:10px 0}
.hint{margin:10px 0;color:#637083;font-size:14px;line-height:1.7}.notice{margin:18px 0;padding:14px 18px;color:#785314;background:#fff4db;border:1px solid #f0d99f;border-radius:8px}.empty-state{padding:32px 20px;border:1px dashed #cbd5e3;border-radius:10px;color:#637083;text-align:center;background:#fff}
.team-card{margin:20px 0;border:1px solid #dde3eb;border-radius:14px;background:#fff}.team-summary{position:sticky;top:var(--collection-top-inset,12px);z-index:20;display:flex;align-items:center;justify-content:space-between;gap:20px;padding:22px 26px;background:#fff;border-bottom:1px solid #edf0f5;border-radius:14px 14px 0 0}.team-summary.form-working{position:relative;top:auto}.summary-main{display:flex;align-items:center;gap:16px;min-width:0}.summary-main h2{overflow-wrap:anywhere}.team-details{margin:6px 0 0;color:#637083;font-size:13px;overflow-wrap:anywhere}.team-body{padding:0 26px 24px}.team-status{margin:22px 0 16px}.status-tag{flex-shrink:0;padding:4px 10px;border-radius:20px;font-size:13px;font-weight:600;color:#4b5d77;background:#eef2f7}.in-progress{color:#2458a8;background:#eaf1ff}.completed{color:#26704b;background:#eaf6ee}.completion-control{flex-shrink:0;text-align:center}.completion-control small{display:block;margin-top:6px;color:#637083;font-size:12px}.team-actions{display:flex;align-items:center;flex-wrap:wrap;gap:12px;margin-top:14px}
button{border:1px solid #3169c8;border-radius:8px;padding:10px 18px;color:#fff;background:#3169c8;font:inherit;font-weight:600;line-height:1.5;cursor:pointer}button:hover:not(:disabled){background:#2458ad;border-color:#2458ad}button:focus-visible{outline:3px solid #97b8ed;outline-offset:3px}button.secondary{color:#435169;border-color:#cbd5e3;background:#fff}button.secondary:hover:not(:disabled){color:#2458ad;background:#f3f7ff}button.refresh-button{flex-shrink:0;font-size:14px}button.complete-button{background:#287349;border-color:#287349;min-width:176px}button.complete-button:hover:not(:disabled){background:#1e5b39;border-color:#1e5b39}button:disabled{opacity:.45;cursor:not-allowed}button.text-button{padding:4px 0;color:#2458a8;border:0;background:transparent;font-size:14px}button.text-button:hover:not(:disabled){color:#17447e;background:transparent;text-decoration:underline}
.progress-steps{display:flex;list-style:none;padding:24px 0;margin:0;border-bottom:1px solid #edf0f5}.progress-steps li{display:flex;align-items:center;flex:1;gap:10px;color:#7c889b;font-size:15px;font-weight:600}.progress-steps li+li:before{content:'';height:1px;background:#dce3ed;width:24px;margin-right:8px}.progress-steps span{display:flex;width:28px;height:28px;flex-shrink:0;align-items:center;justify-content:center;border-radius:50%;background:#edf1f7;color:#738198;font-size:13px}.progress-steps .active{color:#2458a8}.progress-steps .active span{background:#3169c8;color:white}.progress-steps .done span{background:#e7f4ec;color:#287349}.more-actions{margin:18px 0;font-size:14px;color:#637083}.resume-panel{padding:26px 0}.last-step-notice{display:flex;align-items:center;justify-content:space-between;gap:16px;margin-top:20px;padding:18px 20px;border:1px solid #bedfca;border-radius:10px;background:#f0f8f3;color:#276548}.last-step-notice p{margin:4px 0 0;font-size:14px}.collection-window{border-top:1px solid #edf0f5}iframe{display:block;width:100%;border:0;background:#fff}.time-note{margin:20px 0 0;color:#637083;font-size:12px;text-align:right}.completion-receipt{text-align:center;padding:38px 10px 24px}.completion-receipt p{margin:8px 0}.receipt-check{display:inline-flex;justify-content:center;align-items:center;width:48px;height:48px;border-radius:50%;background:#e7f4ec;color:#287349;font-size:26px;margin-bottom:14px}
.collection-tour-backdrop{position:fixed;inset:0;z-index:2990;background:rgba(16,30,52,.56)}.team-summary.tour-active{z-index:3000;border:2px solid #bad8ff;box-shadow:0 0 0 5px rgba(232,241,255,.3);border-radius:12px}.tour-bubble{position:absolute;top:calc(100% + 18px);right:0;width:330px;max-width:calc(100vw - 56px);box-sizing:border-box;padding:24px;border-radius:14px;color:#253347;background:white;box-shadow:0 16px 40px rgba(0,0,0,.15)}.tour-bubble:before{position:absolute;content:'';width:14px;height:14px;top:-7px;right:72px;transform:rotate(45deg);background:white}.tour-bubble p:not(.eyebrow){font-size:15px;margin:12px 0 18px;line-height:1.8}
@media(max-width:760px){
  /* These global selectors are activated and removed by this route's lifecycle only. */
  :global(.app.native-collection-shell),:global(.app-glb-container.native-collection-shell){width:100%;min-width:0;max-width:100%}
  :global(.app-glb-container.native-collection-shell > header .header-con){box-sizing:border-box;flex-wrap:wrap;gap:12px;padding:12px 16px}
  :global(.app-glb-container.native-collection-shell > header .header-con > a){min-width:0;max-width:46%}
  :global(.app-glb-container.native-collection-shell > header .global-title){margin-left:0}
  :global(.app-glb-container.native-collection-shell > header .global-title img){width:auto;height:auto;max-width:100%;max-height:48px}
  :global(.app-glb-container.native-collection-shell > header .header-con > nav){order:3;flex:1 1 100%;min-width:0;flex-wrap:wrap;justify-content:space-between;gap:4px 12px}
  :global(.app-glb-container.native-collection-shell > header .nav-link){margin-left:0;padding:8px 0}
  :global(.app-glb-container.native-collection-shell > header .header-con > div){min-width:0;max-width:50%}
  :global(.app-glb-container.native-collection-shell > header .out){min-width:0;margin-right:0}
  :global(.app-glb-container.native-collection-shell > header .out > div:first-child){max-width:6em;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
  :global(.app-glb-container.native-collection-shell > header .touxiang){width:40px;height:40px}
  :global(.app-glb-container.native-collection-shell > header .xiangqing){left:auto;right:0;top:100%;width:min(520px,calc(100vw - 32px));max-height:70vh;overflow:auto}
  :global(.app-glb-container.native-collection-shell > header .absolute-pop){left:0;right:0;transform:none;padding-top:8px}
  :global(.app-glb-container.native-collection-shell > header .absolute-content){min-width:0}
  :global(.app.native-collection-shell > footer){min-width:0;max-width:100%;overflow-x:auto}
}
@media(max-width:760px){.team-collection{padding:20px 12px}.page-header{gap:12px}h1{font-size:24px}.instructions{padding:14px 16px}.instructions summary span{display:none}.team-summary{position:relative;top:auto;padding:18px 16px;flex-wrap:wrap;gap:16px}.summary-main{flex:1 1 100%;justify-content:space-between;gap:10px}.team-body{padding:0 16px 20px}.completion-control{width:100%;text-align:left}.completion-control button{width:100%}.completion-control small{text-align:center}.progress-steps{padding:20px 0;gap:6px}.progress-steps li{gap:6px;font-size:13px}.progress-steps li+li:before{display:none}.progress-steps span{width:24px;height:24px}.last-step-notice{align-items:flex-start;flex-direction:column}.tour-bubble{position:relative;top:auto;right:auto;max-width:100%;width:100%;padding:16px;margin-top:2px}.tour-bubble:before{right:50%}.tour-bubble h3{font-size:20px}.tour-active .summary-main{display:none}.tour-bubble p:not(.eyebrow){margin:8px 0 12px}}
</style>
