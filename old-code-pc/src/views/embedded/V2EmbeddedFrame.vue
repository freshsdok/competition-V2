<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { enterEmbeddedCredential } from '@/api/internalEmbeddedBridge'
import { isAllowedEmbeddedEntryPath } from '@/utils/embeddedEntryPath'

const props = defineProps({ target: { type: String, required: true } })
const route = useRoute(), busy = ref(false), message = ref(''), source = ref('')
async function open() {
  if (busy.value) return
  busy.value = true; message.value = ''; source.value = ''
  try {
    // 收款入口已交由队伍办理页控制，此通用组件不能退回无队伍校验的旧路径。
    if (props.target !== 'CREDENTIAL_EXCHANGE') throw new Error('TEAM_COLLECTION_REQUIRED')
    const response = await enterEmbeddedCredential(Number(route.params.programId))
    const path = response?.data?.entryPath
    if (!isAllowedEmbeddedEntryPath(path)) throw new Error('INVALID_EMBEDDED_ENTRY')
    source.value = path
  } catch {
    message.value = '安全入口暂时不可用。请确认已登录原平台，稍后重新进入。'
  } finally { busy.value = false }
}
onMounted(open)
</script>

<template>
  <section class="v2-embedded-host">
    <header>
      <h1>{{ target === 'SETTLEMENT_PROFILE' ? '我的收款信息' : '赛证互通' }}</h1>
      <button v-if="message" :disabled="busy" @click="open">重新进入</button>
    </header>
    <p v-if="busy" role="status">正在建立安全办理会话…</p>
    <el-alert v-else-if="message" :title="message" type="warning" :closable="false" />
    <iframe v-else-if="source" :src="source" title="新版安全办理页"
      sandbox="allow-forms allow-scripts allow-same-origin allow-downloads allow-popups"
      referrerpolicy="same-origin" />
  </section>
</template>

<style scoped>
.v2-embedded-host { min-height: 720px; padding: 18px; background: #f6f8fb; }
header { display: flex; align-items: center; justify-content: space-between; }
iframe { display: block; width: 100%; min-height: 100vh; border: 1px solid #dbe4f0; border-radius: 10px; background: #fff; }
</style>
