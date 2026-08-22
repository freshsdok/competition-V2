<template>
<div class="detail-page">
  <div class="container-custom">
    <div class="custom-content">
      <div class="title">{{ detail.title }}</div>
      <div class="time">{{ moment(detail.sendTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
      <div class="msg-content">{{ detail.content }}</div>
    </div>
  </div>     
</div>
</template>
<script setup>
import { getInboxNotificationDetail } from "@/api/site/index";
import { useRoute } from 'vue-router'
import { getToken } from "@/utils/auth";
import moment from "moment";
const route = useRoute();

let detail = $ref({});
const getDetail = () => {
  if(!route.query.siteId){
    return;
  }
  getInboxNotificationDetail(route.query.siteId).then(async (res) => {
    detail = res?.data?.[0] || {};
  }).catch(() => {
  });
};

watch(() => route, () => {
  if(getToken()){
    getDetail();
  }
}, { immediate: true,deep: true })

</script>
<style scoped lang="scss">
.detail-page{
  background-color: #F5F5F5 ;
  min-height: 45vh;
}
.container-custom{
  background: #FFFFFF;
  border-radius: 10px;
  margin: 40px auto;
  padding: 60px 120px;
}
.custom-content{
  padding: 16px 74px;
  font-size: 14px;
  line-height: 24px;
  border: 1px solid rgb(225, 230, 235);
  margin: 24px 15px 20px;
  text-align: center;
  color: #333333;
  .title{
    margin: 16px 0px 10px;
    font-size: 20px;
  }
  .time{
    font-size: 16px;
    margin-bottom: 16px;
  }
  .msg-content{
    border-top: 1px solid #EDEEF0;
    padding: 70px 55px 55px;
    font-size: 16px;
    text-align: left;
  }
}
</style>
