<template>
<div class="info-block flex justify-start items-center mt-6">{{ topName }}</div>
<div class="u-i-line">
  <p class="u-i-line-title">姓名</p>
  <p class="u-i-line-content">{{ userInfo?.authInfo?.realName || '-' }}</p>
</div>
<div class="u-i-line">
  <p class="u-i-line-title">身份证号</p>
  <p class="u-i-line-content" v-if=" userInfo?.authInfo?.idCard">{{ decrypt(userInfo?.authInfo?.idCard) }}</p>
  <p class="flex justify-start items-center rz-line">
    <template v-if="userInfo && userInfo.authStatus == '5'">
      <img src="@/assets/images/baomingchenggong.png" class="rz-img">
      <span>已实名认证</span>
    </template>
    <span class="rz-btn" v-else>您还未进行实名认证,<span class="rz-btn-renzheng" @click="toAuth('nameauthentication')">去认证</span></span>
  </p>

</div>
<div class="u-i-line">
  <p class="u-i-line-title">性别</p>
  <p class="u-i-line-content" v-if=" userInfo?.sex">
      {{ userInfo.sex == '0' ? '男' : userInfo.sex == '1' ? '女' : '未知' }}
    <span></span>
  </p>
</div>
<div class="u-i-line">
  <p class="u-i-line-title">所属学校</p>
  <p class="u-i-line-content" v-if="userInfo?.org?.orgName">{{ userInfo?.org?.orgName }}</p>
    <p class="flex justify-start items-center rz-line">
    <template v-if="getIsAuth()">
      <img src="@/assets/images/baomingchenggong.png" class="rz-img">
      <span>已身份认证</span>
    </template>
    <span class="rz-btn" v-else>您还未进行身份认证,<span class="rz-btn-renzheng" @click="toAuth('identityauthentication')">去认证</span></span>
  </p>
</div>
<div class="u-i-line">
  <p class="u-i-line-title">专业</p>
  <p class="u-i-line-content">{{ userInfo?.authInfo?.realName || '-' }}</p>
</div>
<div class="u-i-line">
  <p class="u-i-line-title">入学年份</p>
  <p class="u-i-line-content">{{ userInfo?.authInfo?.realName || '-' }}</p>
</div>
<div class="u-i-line">
  <p class="u-i-line-title">手机号</p>
  <p class="u-i-line-content">{{ userInfo?.authInfo?.realName || '-' }}</p>
</div>
<div class="u-i-line">
  <p class="u-i-line-title">邮箱</p>
  <p class="u-i-line-content">{{ userInfo?.authInfo?.realName || '-' }}</p>
</div>
</template>
<script setup>
import {useRouter} from 'vue-router'
import { decrypt } from "@/utils/jsencrypt.js";
let router = useRouter()
const props = defineProps({
  topName:{
    type: String,
    default: ''
  },
  value:{
    type: [Object, Array],
    default: {}
  }
});
// 去认证
const toAuth = (type) => {
  router.push({
    path: '/personal/accountmanagement',
    query: {
      classification:type,
    }
  })
}

const getIsAuth = ()=>{
  return userInfo && userInfo.identityInfoList && userInfo.identityInfoList[0] && userInfo.identityInfoList[0].checkStatus== '6'
}


let userInfo = $ref({})
watch(()=>props.value, (newValue, oldValue)=>{
  userInfo = newValue
},{immediate: true,deep: true})
</script>
<style scoped lang="scss">
.info-block{
  padding: 15px 20px;
  height: 50px;
  background: #F5F5F5;
  font-size: 20px;
  color: #333333;
}
.u-i-line{
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin:30px 0;
  .u-i-line-title{
    flex-shrink: 0;
    font-size: 18px;
    color: #333333;
    width: 120px;
    text-align: right;
    margin-right: 50px;
  }
  .u-i-line-content{
    font-size: 18px;
    color: #333333;
    line-height: 1;
    margin-right: 16px;
    @include ellipsis(1);
    max-width: calc(100% - 250px); /* 添加固定宽度 */
  }
  .rz-line{
    font-size: 16px;
    color: #999999;
    line-height: 1;
    flex-shrink: 0; /* 防止被压缩 */
  }
  .rz-img{
    width: 16px;
    height: 16px;
    margin-right: 5px;
  }
  .rz-btn-renzheng{
    font-size: 16px;
    color: #3B72FF;
    font-weight: 500;
    cursor: pointer;
  }
}
</style>