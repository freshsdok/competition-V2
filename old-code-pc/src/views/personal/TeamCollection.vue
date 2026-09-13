<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getMyTeamCollections, startTeamCollection, confirmTeamCollection, releaseTeamCollection } from '@/api/teamCollection'
import { enterEmbeddedSettlement } from '@/api/internalEmbeddedBridge'
import { isAllowedEmbeddedEntryPath } from '@/utils/embeddedEntryPath'

const teams = ref([]), loading = ref(true), busy = ref(false), message = ref(''), unavailable = ref(false)
const frame = ref(null)
let poll, channel, disposed = false, loadSequence = 0

function time(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('zh-CN', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit',
    day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }).format(new Date(value))
}
function stillHeld(opened) {
  return teams.value.some(row => row.id === opened.teamId && row.heldByMe && row.version === opened.version)
}
async function refresh() {
  const sequence = ++loadSequence
  try {
    const response = await getMyTeamCollections()
    if (disposed || sequence !== loadSequence) return false
    if (!Array.isArray(response?.data)) throw new Error('INVALID_STATUS')
    teams.value = response.data
    unavailable.value = false
    if (frame.value && !stillHeld(frame.value)) {
      frame.value = null
      message.value = '本队办理状态已变化，填报窗口已关闭。'
    }
    return true
  } catch {
    if (disposed || sequence !== loadSequence) return false
    frame.value = null
    unavailable.value = true
    message.value = '暂时无法核实办理状态，请刷新后继续。原办理名额不会自动释放。'
    return false
  } finally {
    if (!disposed && sequence === loadSequence) loading.value = false
  }
}
async function open(row) {
  frame.value = null
  const response = await enterEmbeddedSettlement(row.id, row.version)
  if (disposed) return
  const entryPath = response?.data?.entryPath
  if (!isAllowedEmbeddedEntryPath(entryPath)) throw new Error('INVALID_ENTRY')
  const opened = { teamId: row.id, version: row.version, entryPath }
  if (await refresh() && stillHeld(opened)) frame.value = opened
}
async function act(row, action) {
  if (busy.value || unavailable.value) return
  busy.value = true
  message.value = ''
  try {
    if (action === 'confirm' || action === 'release') {
      const text = action === 'confirm'
        ? '请确认下方填报页面已提示保存成功。确认后本队不再开放入口，同队学生可查看您的姓名和确认时间。此状态由您确认，不是系统核验保存结果。'
        : '请先停止填写并关闭其他填报窗口。放弃后其他队员可以办理；本操作不会删除您在 V2 已保存的资料。'
      try {
        await ElMessageBox.confirm(text, action === 'confirm' ? '确认完成填报' : '放弃本次办理', {
          confirmButtonText: action === 'confirm' ? '我确认已完成' : '放弃并释放名额', cancelButtonText: '继续办理', type: 'warning',
        })
      } catch { return }
      frame.value = null
      if (action === 'confirm') await confirmTeamCollection(row.id, row.version)
      else await releaseTeamCollection(row.id, row.version)
      channel?.postMessage({ teamId: row.id })
      await refresh()
      if (!unavailable.value) message.value = action === 'confirm' ? '已记录您的完成确认，本队填报入口已关闭。' : '已放弃本次办理，其他队员可以申请。'
    } else {
      let selected = row
      if (action === 'start') {
        const response = await startTeamCollection(row.id, row.version)
        selected = response.data
        channel?.postMessage({ teamId: row.id })
      }
      await open(selected)
    }
  } catch {
    frame.value = null
    const refreshed = await refresh()
    if (refreshed) message.value = '操作未完成，请查看最新队伍状态。若仍由您办理，可重新进入或主动放弃。'
  } finally { busy.value = false }
}
function onFocus() { if (!busy.value) refresh() }
onMounted(() => {
  refresh()
  poll = setInterval(() => { if (!busy.value && !document.hidden) refresh() }, 15000)
  window.addEventListener('focus', onFocus)
  if (typeof BroadcastChannel !== 'undefined') {
    channel = new BroadcastChannel('v1-team-collection')
    channel.onmessage = (event) => {
      if (frame.value?.teamId === event.data?.teamId) frame.value = null
      refresh()
    }
  }
})
onBeforeUnmount(() => {
  disposed = true
  ++loadSequence
  clearInterval(poll)
  channel?.close()
  window.removeEventListener('focus', onFocus)
  frame.value = null
  // 离开、刷新、断网都不自动释放；办理人再次进入可以继续。
})
</script>

<template>
  <section class="team-collection">
    <header><h1>我的收款信息</h1><button :disabled="busy" @click="refresh">刷新状态</button></header>
    <p>每队由一名名单内学生办理，请填写本人收款资料。保存后请在本页确认完成。</p>
    <p class="hint">关闭页面不会释放名额，您可以稍后继续。若不再办理，请主动放弃。时间均为北京时间。</p>
    <p v-if="message" role="status" class="notice">{{ message }}</p>
    <p v-if="loading">正在查询办理资格…</p>
    <p v-else-if="!unavailable && !teams.length">您当前没有可查看的获奖队伍填报记录。</p>
    <article v-for="row in teams" :key="row.id" class="team-card">
      <h2>{{ row.teamName || row.teamCode }}</h2>
      <p>队伍编号：{{ row.teamCode }} <span v-if="row.awardLevel"> · {{ row.awardLevel }}</span></p>
      <template v-if="row.status === 'AVAILABLE'">
        <p>本队尚未开始办理。</p>
        <button :disabled="busy || unavailable" @click="act(row, 'start')">申请办理并进入填报</button>
      </template>
      <template v-else-if="row.status === 'IN_PROGRESS'">
        <p>{{ row.handlerName }} 于 {{ time(row.startedAt) }} 开始办理。</p>
        <template v-if="row.heldByMe">
          <button :disabled="busy || unavailable" @click="act(row, 'open')">{{ frame?.teamId === row.id ? '重新进入填报' : '继续填报' }}</button>
          <button :disabled="busy || unavailable" @click="act(row, 'confirm')">我已完成填报</button>
          <button class="secondary" :disabled="busy || unavailable" @click="act(row, 'release')">放弃本次办理，释放名额</button>
        </template>
        <p v-else>本队正在办理，暂不能进入填报。</p>
      </template>
      <template v-else-if="row.status === 'USER_CONFIRMED_COMPLETE'">
        <p>{{ row.handlerName }} 已于 {{ time(row.confirmedAt) }} 确认完成收款资料填报。</p>
        <p class="hint">此状态由办理人确认，本队填报入口已关闭。</p>
      </template>
      <p v-else>状态暂不可识别，请联系工作人员。</p>
      <iframe v-if="frame?.teamId === row.id" :key="frame.entryPath" :src="frame.entryPath" title="本人收款资料填报"
        sandbox="allow-forms allow-scripts allow-same-origin allow-downloads allow-popups" referrerpolicy="same-origin" />
    </article>
  </section>
</template>

<style scoped>
.team-collection { max-width: 1160px; margin: auto; padding: 24px; color: #253347; }
header { display: flex; align-items: center; justify-content: space-between; }
h1 { font-size: 24px; } h2 { font-size: 19px; }
.hint { color: #637083; font-size: 14px; line-height: 1.7; }
.notice { padding: 12px 16px; background: #fff4db; border-radius: 6px; }
.team-card { margin: 18px 0; padding: 20px; border: 1px solid #dde3eb; border-radius: 10px; background: #fff; }
button { border: 1px solid #3169c8; border-radius: 5px; padding: 9px 14px; margin: 4px 10px 4px 0; color: #fff; background: #3169c8; cursor: pointer; }
button.secondary { color: #435169; border-color: #abb5c4; background: #fff; }
button:disabled { opacity: .5; cursor: not-allowed; }
iframe { display: block; width: 100%; height: 1050px; margin-top: 20px; border: 1px solid #dde3eb; background: #fff; }
</style>
