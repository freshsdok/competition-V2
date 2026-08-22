<template>
  <div class="common_top">
      <img src="@/assets/images/saishi.png" class="saishi">
      <div class="common_top_text">
          <p class="text-[36px] font-bold text-white">赛事中心</p>
          <p class="text-[24px] text-white mt-[15px]">科技赋能赛事，匠心呈现精彩</p>
      </div>
  </div>
  <div class="detail-page container-custom flex justify-between align-start" v-if="pageDetail && (pageDetail.competitionId || pageDetail.competitionSeriesId)">
    <div class="tabs-left">
      <div class="tabs-left-item flex items-center justify-center" 
          v-for="(item, index) in tabsList" :key="index"
                :class="{ active: activetabsName === index }"
                @click="changeTabs(index)">
        {{ item.modelName || '' }}
      </div>
    </div>
    <div class="tabs-right">
      <!-- 公共标题 -->
      <div class="jieshao-container">
        <div class="top-bar w-full flex justify-between items-center">
          <div class="nav-item active flex justify-start items-center">
            <span>{{ nowModelName }}</span>
            <div class="nav-item-line w-full bg-[#3169F8]"></div>
          </div>
          <div class="nav-buttons"> 
            <el-button class="nav-buttons-item hvr-grow" @click="toTeamApply">赛事报名</el-button>
          </div>
        </div>
        <div class="description flex justify-between items-center w-full">
          <div class="description-left flex justify-start items-start">
            <p class="description-left-p">赛事主办方：{{ pageDetail.organizer || '' }}</p>
            <p class="description-left-p ml-12 flex-shrink-0 mr-8">发布时间：{{ pageDetail.publishTime || '' }}</p>
          </div>
          <div class="description-right flex justify-end items-center flex-shrink-0">
            <!-- <p class="description-right-p flex flex-col justify-center items-center cursor-pointer hvr-grow"
                @click="collectClickToggle()">
              <img src="@/assets/images/star-active.png" alt="" class="mr-1 mb-2 description-right-p-img" v-if="isCollect"/>
              <img src="@/assets/images/star.png" alt="" class="mr-1 mb-2 description-right-p-img" v-else/>
              <span>{{ isCollect ? '取消收藏' : '收藏' }}</span>
            </p> -->
            <p class="description-right-p flex flex-col justify-center items-center ml-8 cursor-pointer hvr-grow"
              @click="copyLink">
              <img src="@/assets/images/share.png" alt="" class="mr-1 mb-2 description-right-p-img" />
              <span>分享</span>
            </p>
          </div>
        </div>
      </div>
      <!-- 不同的富文本 -->
       <div class="ql-container ql-snow">
        <div class="rich-content ql-editor" v-html="getTabsText()">
        </div>
       </div>
    </div>
  </div>
  <el-empty description="暂无数据" v-if="(pageDetailLoading === false) && (!pageDetail || !pageDetail.competitionId)"></el-empty>
</template>

<script setup>
import {
  getUserCompetitionDetailInfoById,
  getPublicCompetitionTrackList,
  shareCompetition,
  checkCollect,
  addCollect,
  removeCollect
} from "@/api/visualization/index.js";
import Modal from "@/plugins/modal.js";
import useClipboard from "vue-clipboard3";
import { useRoute, useRouter } from "vue-router";
import { getToken,getinfo,setinfo } from "@/utils/auth";
import { getAuthInfo } from "@/api/index";
import "@vueup/vue-quill/dist/vue-quill.snow.css"
import { useCounterStore } from "@/stores/index";

let isAuth = $computed(() => {
  return getToken() ? true : false;
});


const route = useRoute();
const router = useRouter();
const { toClipboard } = useClipboard();
let activetabsName = $ref(0);
const changeTabs = (index) => {
  activetabsName = index;
};
const getTabsText = () => {
  let item = tabsList[activetabsName];
  return (item && item.modelContent) || "暂无数据";
};
const nowModelName = computed(() => {
  let item = tabsList[activetabsName];
  return (item && item.modelName) || "";
});
// 用户个人认证信息
let userInfo = $computed(() => {
  let res = {}
  try {
    const info = getinfo();
    res = JSON.parse(info)
  } catch (error) {
    res = {}
  }
  return res
})

// 跳转带队到报名
const toTeamApply = () => {
  if (!isAuth) {
    // 提示用户请先登录 
    const counterStore = useCounterStore();
    counterStore.increment();// 登录弹窗
    return;
  }else{
    if (isTeacherCompetition) {
      router.push({
        path: "/event/detail/teacherApply",
        query: { competitionSeriesId: pageDetail.competitionSeriesId },
      });
      return;
    }
    // 判断不是带队以外的角色
    let identityInfoList = userInfo?.identityInfoList || []
    let findTeacher = identityInfoList.findIndex(item => item.certificationType === 'teacher');
    // 如果有角色，但是角色里面没有带队老师，则弹出这个提示
    if((identityInfoList && identityInfoList.length) && (findTeacher < 0)){
      Modal.confirm('当前赛事仅支持带队老师报名，请联系带队老师进行报名','',{
        showConfirmButton: false,
        showClose: false,
        cancelButtonText: '关闭'
      }).then(() => {})
      return
    }
    
    // 有报名权限仅限带队老师，有权限字符串的apply:user:add
    let permissions = userInfo?.permissions || []
    if(permissions.includes('apply:user:add')){
      router.push({
        path: "/event/detail/teacherApply",
        query: {
          competitionSeriesId: pageDetail.competitionSeriesId
        },
      });
    }else{
      Modal.confirm('当前赛事仅支持带队老师报名，请先完成带队老师认证','',{
        confirmButtonText: '去认证',
        cancelButtonText: '关闭',
        showClose: false,
        type: 'warning'
      }).then(() => { 
        router.push({
          path: '/personal/accountmanagement',
          query: {
            classification:'identityauthentication',
          }
        })
      })
    }
  }
};

// 分享
const copyLink = async () => {
  const link = window.location.href;
  shareCompetition({
    competitionId: route?.query?.competitionId || '',
    competitionSeriesId: route?.query?.competitionSeriesId || '',
  })
    .then(async (res) => {
      if (res.code == 200) {
        await toClipboard(link);
        Modal.notifySuccess({
          message: "链接复制成功，快去分享给好友吧",
          type: "success",
          title: "复制成功",
        });
      }
    })
    .catch((err) => {});
};

// 获取赛事详情
let pageDetail = $ref({});
let tabsList = $ref([]);
let pageDetailLoading = $ref(null);
let isTeacherCompetition = $ref(false);
const getDetail = () => {
  let query = {
    competitionId: route.query.competitionId,
    competitionSeriesId: route.query.competitionSeriesId,
  };
  pageDetailLoading = true;
  getUserCompetitionDetailInfoById(query).then(async (res) => {
    if (res.code == 200) {
      pageDetail = res.data || {};
      try {
        const trackResult = await getPublicCompetitionTrackList(pageDetail.competitionSeriesId);
        isTeacherCompetition = (trackResult.data || []).some(
          item => item.competitionTrackName === '教师赛'
        );
      } catch (error) {
        isTeacherCompetition = false;
      }
      // 如果已经登录，才获取是否收藏
      if (isAuth && (pageDetail.competitionId || pageDetail.competitionSeriesId)) {
        usergetinfo();
      }
      const competitionExtension =
        (res.data && res.data.competitionExtension) || [];
      if (competitionExtension && competitionExtension.length) {
        tabsList = JSON.parse(competitionExtension);
        console.log(tabsList);  
      }
    }
    pageDetailLoading = false;
  }).catch(() => {
    pageDetailLoading = false;
  });
};

// 是否收藏获取
let isCollect = $ref(false);
const getCollect = () => {
  checkCollect({
    competitionId: route?.query?.competitionId || '',
    competitionSeriesId: route?.query?.competitionSeriesId || '',
  }).then((res) => {
    if (res.code == 200) {
      isCollect = res.data || false;
    }
  });
};

const collectClickToggle = () => {
  let collectApi = isCollect ? removeCollect : addCollect;
  collectApi({
    competitionId: route?.query?.competitionId || '',
    competitionSeriesId: route?.query?.competitionSeriesId || '',
  }).then((res) => {
    if (res.code == 200) {
      let msg = isCollect ? "已取消收藏" : "已收藏成功";
      Modal.notifySuccess({
        message: msg,
        type: "success",
        title: msg,
      });
      isCollect = !isCollect;
    }
  });
};

getDetail();

const usergetinfo = () => {
  getAuthInfo().then((row) => {
    if (row.code === 200) {
      setinfo(JSON.stringify(row.data));
    }
  });
};
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
