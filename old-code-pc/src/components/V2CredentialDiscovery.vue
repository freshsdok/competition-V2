<script setup>
import { onMounted, ref } from 'vue'
import { discoverEmbeddedCredentials } from '../api/internalEmbeddedBridge'
import { useRouter } from 'vue-router'
const items=ref([]), busy=ref(false), message=ref('')
const router=useRouter()
onMounted(async () => {
  try { items.value=(await discoverEmbeddedCredentials()).data ?? [] } catch { items.value=[] }
})
async function enter(item) {
  if(busy.value) return
  busy.value=true
  try {
    await router.push(`/credential-exchange/${item.offeringId}`)
  } catch { message.value='请先登录原平台；入口暂时不可用时，可稍后重新进入。';busy.value=false }
}
</script>
<template>
  <section v-if="items.length" aria-label="赛证互通">
    <h2>赛证互通</h2>
    <p>查看可办理的凭证项目；个人资格和申请由新平台安全核验。</p>
    <ul><li v-for="item in items" :key="item.offeringId">
      {{ item.displayName }} · {{ item.targetCredentialName }} · {{ item.free ? '免费' : '付费' }}
      <button :disabled="busy" @click="enter(item)">{{ busy ? '正在进入…' : '立即办理' }}</button>
    </li></ul>
    <p role="status">{{ message }}</p>
  </section>
</template>
