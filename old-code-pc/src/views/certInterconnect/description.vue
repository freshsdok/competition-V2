<template>
  <PageBanner :banner="bannerSrc" />
  <div class="detail-page container-custom flex justify-between align-start">
    <div class="tabs-right">
      <!-- 公共标题 -->
      <div class="jieshao-container">
        <div class="top-bar w-full flex justify-between items-center">
          <div class="nav-item active flex justify-start items-center">
            <span>{{ pageDetail.rulerName || '' }}</span>
            <div class="nav-item-line w-full bg-[#3169F8]"></div>
          </div>
          <div class="nav-buttons"> 
            <el-button class="nav-buttons-item hvr-grow" @click="toApply">申请证书</el-button>
          </div>
        </div>
        <div class="description flex justify-between items-center w-full">
          <div class="description-left flex justify-start items-start">
          </div>
        </div>
      </div>
      <!-- 不同的富文本 -->
       <div class="ql-container ql-snow">
        <div class="rich-content ql-editor" v-html="pageDetail?.applyDesc || ''">
        </div>
       </div>
    </div>
  </div>
  <el-empty description="暂无数据" v-if="(pageDetailLoading === false) && (!pageDetail)"></el-empty>
</template>

<script setup>
import Modal from "@/plugins/modal.js";
import useClipboard from "vue-clipboard3";
import { useRoute, useRouter } from "vue-router";
import { getToken,getinfo,setinfo } from "@/utils/auth";
import { getAuthInfo } from "@/api/index";
import "@vueup/vue-quill/dist/vue-quill.snow.css"
import { useCounterStore } from "@/stores/index";
import PageBanner from "@/components/PageBanner/index.vue"
import bannerSrc from '@/assets/images/certInterconnect_banner.png'
import { getCertInterconnectRuleNoAuth } from "@/api/certInterconnect/index.js"
const route = useRoute();
const router = useRouter();



// 获取赛事详情
let pageDetail = $ref({});
let pageDetailLoading = $ref(null);
const getDetail = () => {
  pageDetailLoading = true;
  getCertInterconnectRuleNoAuth(route?.query?.ruleId).then(async (res) => {
    if (res.code == 200) {
      pageDetail = res.data || {};
    }
    pageDetailLoading = false;
  }).catch(() => {
    pageDetailLoading = false;
  });
};

const isAuth = computed(() => !!getToken())
// 申请证书
const toApply = async () => {
  if (!isAuth.value) {
    const counterStore = useCounterStore();
    counterStore.increment();// 登录弹窗
    return;
  }
  const info = await getinfo();
  const userInfo = JSON.parse(info)
  if (!(userInfo?.authInfo && userInfo?.authInfo?.idCard && userInfo?.authInfo.realName)) {
    Modal.confirm('赛证互通需要实名认证，请先完成实名认证', '', {
      confirmButtonText: '去认证',
      cancelButtonText: '关闭',
      showClose: false,
      type: 'warning'
    }).then(() => { 
      router.push('/personal/accountmanagement?classification=personaldata')
    }).catch()
    return;
  }
  router.push(`/certInterconnect/details/${route?.query?.ruleId || ''}`)
}

getDetail();
</script>

<style scoped lang="scss">
.common_top {
  position: relative;
  .common_top_text {
    position: absolute;
    top: 50%;
    left: 10%;
    transform: translateY(-50%);
  }
}
.detail-page {
  padding-top: 45px;
  padding-bottom: 80px;
}
.tabs-left {
  flex-shrink: 0;
  padding-right: 36px;
  .tabs-left-item {
    font-weight: 500;
    font-size: 18px;
    color: #333333;
    width: 290px;
    height: 70px;
    background: #ffffff;
    cursor: pointer;
  }
  .active {
    color: #ffffff !important;
    background: #3169f8 !important;
    position: relative;
  }
  .active::after {
    content: "";
    position: absolute;
    top: 50%;
    right: -34px;
    transform: translateY(-50%);
    width: 0;
    height: 0;
    border-left: 35px solid #3169f8;
    border-top: 35px solid transparent;
    border-bottom: 35px solid transparent;
    z-index: 1;
  }
}
.tabs-right {
  flex: 1;
  margin-left: 25px;
}
.top-bar {
  border-bottom: 2px solid #e4e4e4;
  position: relative;
  height: 70px;
  .nav-item {
    height: 100%;
    position: relative;
    font-size: 20px;
    color: #333333;
    .nav-item-line {
      position: absolute;
      bottom: -2px;
      left: 0;
      height: 2px;
    }
  }
  .nav-buttons {
    .nav-buttons-item {
      // background: linear-gradient(#3169f8 0%, #33dbdb 100%);
      min-width: 120px;
      background:#3169F8;
      height: 40px;
      border-radius: 10px;
      font-weight: bold;
      font-size: 18px;
      color: #ffffff;
      margin-right: 20px;
      padding-left: 16px;
      padding-right: 16px;
      &:last-child {
        margin-right: 0;
      }
    }
    .apply-btn {
      font-size: 18px;
      color: #ffffff;
      height: 40px;
      background: linear-gradient(#f68801 0%, #ffdc2d 100%);
    }
    .upfile-btn {
      font-size: 18px;
      color: #ffffff;
      height: 40px;
      background: linear-gradient(180deg, #7dec40 0%, #389f00 100%);
    }
    .err-btn {
      font-size: 18px;
      color: #ffffff;
      height: 40px;
      background: linear-gradient(180deg, #ff4d4f 0%, #ff7875 100%);
    }
  }
}
.jieshao-container {
  margin-bottom: 20px;
}
.page-title {
  margin-top: 40px;
  margin-bottom: 20px;
  font-weight: bold;
  font-size: 24px;
  color: #333333;
}
.description {
  margin-top: 40px;
}
.description-left-p {
  font-weight: 400;
  font-size: 16px;
  color: #666666;
}
.description-right-p {
  font-weight: 400;
  font-size: 14px;
  color: #999999;
  display: flex !important;
  .description-right-p-img {
    width: 24px;
  }
}
.saishi{
  width: 100%;
}
</style>
<style>
.eventCenterDetail {
  background: #ffffff;
}
</style>