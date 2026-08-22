<template>
  <div class="info-block flex justify-start items-center">{{ topName }}</div>
  <div class="u-i-line">
    <p class="u-i-line-title">报名账号</p>
    <p class="u-i-line-content">{{ userInfo?.nickName || userInfo?.userName || "-" }}</p>
  </div>
  <div class="u-i-line">
    <p class="u-i-line-title">手机号</p>
    <p class="u-i-line-content">{{ userInfo?.phonenumber || "-" }}</p>
  </div>
  <div class="u-i-line">
    <p class="u-i-line-title">说明</p>
    <p class="u-i-line-content">教师赛选手信息以报名表为准，无需选手注册或完成身份认证。</p>
  </div>
</template>
<script setup>
const props = defineProps({
  topName: {
    type: String,
    default: "",
  },
  value: {
    type: [Object, Array],
    default: {},
  },
});
let userInfo = $ref({});
watch(
  () => props.value,
  (newValue, oldValue) => {
    userInfo = newValue;
  },
  { immediate: true, deep: true }
);
</script>
<style scoped lang="scss">
</style>
