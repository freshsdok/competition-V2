<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createPayoutTransitionEntry } from '@/api/payoutTransition'
import { isAllowedPayoutTransitionUrl } from '@/utils/payoutTransitionUrl'

const busy = ref(false)

async function enterV2Payout() {
  if (busy.value) return
  busy.value = true
  try {
    const response = await createPayoutTransitionEntry()
    const entryUrl = response?.data?.entryUrl
    if (!isAllowedPayoutTransitionUrl(entryUrl)) {
      throw new Error('付款办理入口暂不可用')
    }
    window.location.assign(entryUrl)
  } catch (error) {
    ElMessage.error(error?.message || '暂时无法进入付款资料办理，请稍后重试')
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section class="payout-transition-card">
    <p class="eyebrow">付款资料办理</p>
    <h1>前往新版安全办理页</h1>
    <p>新版页面会根据您在V2中已发布的付款明细展示可办理内容；V1不会计算金额，也不会保存银行卡和发票材料。</p>
    <el-alert title="一次性链接有效期不超过5分钟，失效后请重新进入。" type="info" :closable="false" />
    <el-button type="primary" :loading="busy" @click="enterV2Payout">进入付款资料办理</el-button>
  </section>
</template>

<style scoped>
.payout-transition-card { max-width: 720px; margin: 48px auto; padding: 32px; border: 1px solid #dbe4f0; border-radius: 16px; background: #fff; }
.eyebrow { color: #2864dc; font-weight: 700; }
.payout-transition-card .el-alert { margin: 20px 0; }
</style>
