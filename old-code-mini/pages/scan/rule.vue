<template>
  <view class="container">
    <scroll-view class="content" scroll-y>
      <rich-text :nodes="content"></rich-text>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getSignRule } from '@/api/scan'

const title = ref('')
const content = ref('')
onLoad((options) => {
  const type = options.type
  if (type === '1') {
    title.value = '赛场规则'
  } else {
    title.value = '考生承诺'
  }
  // 设置页面标题
  uni.setNavigationBarTitle({
    title: title.value
  })
  fetchSignRule(type,options.codeConfigId)
})

function fetchSignRule(type, codeConfigId) {
  getSignRule({ configId: codeConfigId }).then(res => {
    if (res.code === 200 && res.data) {
      const data = res.data
      // 设置页面数据
      if(type === '1'){
        content.value = data.examinationHallRuler
      }else{
        content.value = data.examinationHallPromise
      }
    }
  }).catch(() => {
  })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #ffffff;
}

.content {
  padding: 20rpx;
}
</style>
