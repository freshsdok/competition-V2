<template>
  <header class="bg-white shadow-sm sticky top-0 z-50">
    <div v-if="pc_inform.length > 0" class="tongzhi-xiangqing">
      <!-- <div style="clear: both"></div> -->
      <el-carousel class="tongzhi-xiangqing-text" height="70px" direction="vertical" :interval="5000">
        <el-carousel-item v-for="item in pc_inform" :key="item" class="notice-item">
          <div class="notice-item">
            <img src="@/assets/icon/laba.png" class="laba" alt="" />
            {{ item.title }}
            <!-- <img src="@/assets/icon/chahao.png" class="chahao" alt=""  @click="tongzhishow = false"> -->
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
    <div>
      <div class="flex items-center justify-between header-con">
        <!-- Logo -->
        <router-link to="/" class="flex items-center gap-2">
          <!-- <div class="global-title">得时综合教育平台</div> -->
          <div class="global-title">
            <img src="@/assets/images/logo.png" alt="" />
          </div>
        </router-link>

        <!-- 导航菜单 - 桌面端 -->
        <nav class="flex items-center">
          <div v-for="item in navItems" :key="item.path" :to="item.path" @click="handleClickOne(item)"
            class="nav-link position-box cursor-pointer" :class="{ 'nav-link-active': getActiveClass(item) }">
            <div>
              {{ item?.label }}
              <div class="absolute bottomtop-0 absolute-pop" v-if="item.children && item.children.length > 0">
                <div class="absolute-content">
                  <div class="absolute-content-item" v-for="value in item.children"
                    @click.prevent.stop="handleClick(item, value)" :class="{
                      'absolute-content-item-active': getActiveClassChiild(
                        item,
                        value
                      ),
                    }">
                    {{ value?.label }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!-- <button v-if="getToken()" class="out" @click="outlogin">
            退出登录
          </button> -->
        </nav>
        <div style="display: flex; align-items: center">
          <Site v-if="getToken()" />
          <!-- <div class="tongzhi" @click="tongzhishow = true" v-if="pc_inform.length>0">通知</div> -->
          <div v-if="getToken()" class="out">
            <div style="font-size: 16px; margin-right: 15px">
              {{
                userinfo.authStatus == 5
                  ? userinfo?.authInfo?.realName
                  : userinfo?.nickName
              }}
            </div>
            <el-badge :value="weichulizongshu > 0 ? weichulizongshu : ''
              ">
              <img :src="userinfo.avatar ? userinfo.avatar : img1" alt="" class="touxiang" />
            </el-badge>

            <div class="xiangqing">
              <div class="txandrz">
                <div>
                  <img :src="userinfo.avatar ? userinfo.avatar : img1" alt="" class="rztouxiang" />
                </div>

                <div class="xingming">
                  <div class="name">
                    <!-- {{ userinfo.nickName }} -->
                    <!-- {{ userinfo?.authInfo?.realName || userinfo?.nickName }} -->
                    {{
                      userinfo.authStatus == 5
                        ? userinfo?.authInfo?.realName
                        : userinfo?.nickName
                    }}
                  </div>
                  <div class="renzheng">
                    <div style="
                        display: flex;
                        align-items: center;
                        cursor: pointer;
                      " @click="zhanghaoshezhi('nameauthentication')">
                      <img :src="baomingchenggong" class="tubiao" alt="" v-if="userinfo.authStatus == 5" />
                      <img :src="tanhao" class="tubiao" alt="" v-else />
                      {{
                        real_name_auth_status.find(
                          (item) => item.value == userinfo.authStatus
                        )?.label
                      }}
                    </div>
                    <div v-if="
                      userinfo.identityInfoList &&
                      userinfo.identityInfoList.length > 0
                    " style="
                        display: flex;
                        align-items: center;
                        cursor: pointer;
                        margin-left: 25px;
                      " @click="zhanghaoshezhi('identityauthentication')">
                      <template 
                          v-if="userinfo.identityInfoList[0].checkStatus == 6" >
                        <img :src="baomingchenggong" class="tubiao" alt=""/>
                        {{
                          identity_status.find(
                            (item) =>
                              item.value ==
                              userinfo.identityInfoList[0].checkStatus
                          )?.label
                        }}
                      </template>
                    </div>
                    <div v-else style="
                        display: flex;
                        align-items: center;
                        cursor: pointer;
                        margin-left: 25px;
                      " @click="zhanghaoshezhi('identityauthentication')">
                      <img :src="tanhao" class="tubiao" alt="" />
                      未身份认证
                    </div>
                  </div>
                </div>
              </div>
              <div class="xuexi">
                <div class="gerenzhongxin" @click="routerTo('/personal/list')">
                  <img src="@/assets/xuanfuchuang/saishi.png" alt="" /> 我的赛事
                </div>
                <div class="xiangxi">
                  <div @click="routerTo('/personal/list?lefttabs=我的团队')" class="danxiang">
                    <img src="@/assets/icon/saishi.png" alt="" />
                    我的团队
                  </div>
                  <div style="width: 30%">
                    <!-- pending 待支付 -->
                    <el-badge :value="userinfo.noPayOrderNum > 0 ? userinfo.noPayOrderNum : ''
                      " class="item">
                      <div @click="
                        routerTo(
                          `/personal/paymentrecords${userinfo.noPayOrderNum > 0
                            ? '?status=pending'
                            : ''
                          }`
                        )
                        " class="danxiang" style="width: 100%">
                        <img src="@/assets/icon/jiaofei.png" alt="" />
                        我的订单
                      </div>
                    </el-badge>
                  </div>

                  <div @click="routerTo('/personal/list?lefttabs=开票记录')" class="danxiang">
                    <img src="@/assets/icon/kaipiao.png" alt="" />
                    开票记录
                  </div>
                  <!-- <div
                    @click="routerTo('/personal/list?lefttabs=我的文件')"
                    class="danxiang"
                  >
                    <img src="@/assets/icon/wodewenjian.png" alt="" />
                    我的文件
                  </div> -->
                  <div style="width: 30%">
                    <!-- pending 待支付 -->
                    <el-badge :value="unReadCountNum > 0 ? unReadCountNum : ''
                      " class="item">
                      <div @click="routerTo('/personal/list?lefttabs=我的文件')" class="danxiang" style="width: 100%">
                        <img src="@/assets/icon/wodewenjian.png" alt="" />
                        我的文件
                      </div>
                    </el-badge>
                  </div>
                  <div style="width: 30%">
                    <div class="item">
                      <div @click="routerTo('/certInterconnect/myCert')" class="danxiang" style="width: 100%">
                        <img src="@/assets/icon/cert.png" alt="" />
                        我的证书
                      </div>
                    </div>
                  </div>
                  <div style="width: 30%" v-if="isTeacher">
                    <div class="item">
                      <div @click="routerTo('/exam')" class="danxiang" style="width: 100%">
                        <img src="@/assets/icon/exam.png" alt="" />
                        我的赛场
                      </div>
                    </div>
                  </div>
                  <div style="width: 30%" v-if="isTeacher">
                    <div class="item">
                      <div @click="routerTo('/awardPublicity')" class="danxiang" style="width: 100%">
                        <img src="@/assets/icon/awardPublicity.png" alt="" />
                        获奖公示
                      </div>
                    </div>
                  </div>
                  <div style="width: 30%" v-if="isTeacher">
                    <div class="item">
                      <div @click="routerTo('/promotion')" class="danxiang" style="width: 100%">
                        <img src="@/assets/icon/promotion.png" alt="" />
                        晋级队伍
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <!-- <div class="saishi"></div> -->
              <div class="gerenshezhi">
                <div class="geren" @click="
                  routerTo(
                    '/personal/accountmanagement?classification=personaldata'
                  )
                  ">
                  账号设置
                </div>
                <div class="shuxian"></div>
                <div class="outlogin" @click="outlogin">退出登录</div>
              </div>
            </div>
          </div>
          <div v-if="!getToken()" class="out">
            <div @click="counterStore.increment" style="
                cursor: pointer;
                margin-right: 10px;
                color: #0e3cb1;
                font-size: 16px;
              ">
              登录
            </div>
            <div style="color: #c6c6c6">|</div>
            <div @click="counterStore.phoneincrement" style="
                margin-left: 15px;
                cursor: pointer;
                color: #0e3cb1;
                font-size: 16px;
              ">
              注册
            </div>
          </div>
        </div>
      </div>
    </div>
    <login v-if="counterStore.loginopen" />
  </header>
</template>

<script setup>

import img1 from "@/assets/images/shawn-avatar.png";
import Cookies from "js-cookie";
import webGroups from "@/router/webGroups";
// import router from "@/router";
import { useRoute, useRouter } from "vue-router";
import { getToken, removeToken, setinfo } from "@/utils/auth";
import modal from "@/plugins/modal";
import login from "./login.vue";
import { useCounterStore } from "@/stores/index";

import baomingchenggong from "@/assets/images/baomingchenggong.png";
import tanhao from "@/assets/images/tanhao.png";
import { getAuthInfo, logout, getNotices, unReadCount } from "@/api/index";

import Site from "./site.vue";
const { proxy } = getCurrentInstance();
const { identity_status, real_name_auth_status } = proxy.useDict(
  "identity_status",
  "real_name_auth_status"
);
const pc_inform = ref([]);
const getNoticeslist = () => {
  getNotices().then((res) => {
    if (res.code === 200) {
      pc_inform.value = res.data || [];
    }
  });
};
getNoticeslist();

// 获取我的文件未阅读数
const unReadCountNum = ref(0);
const unReadCountlist = () => {
  unReadCount().then((res) => {
    unReadCountNum.value = res.data;
    // 更新未处理总数
    updateWeichulizongshu();
  });
};

const weichulizongshu = ref(0);

// 更新未处理总数
const updateWeichulizongshu = () => {
  const noPayOrderNum = userinfo.value.noPayOrderNum || 0;
  const unReadNum = unReadCountNum.value || 0;
  weichulizongshu.value = Number(noPayOrderNum) + Number(unReadNum);
};
const tongzhishow = ref(false);

const counterStore = useCounterStore();
let navItems = $computed(() => {
  return counterStore?.menuList?.pcHeader || [];
});
const route = useRoute();
const router = useRouter();

const zhanghaoshezhi = (item) => {
  router.push({
    path: "/personal/accountmanagement",
    query: {
      classification: item,
    },
  });
  setTimeout(() => {
    location.reload();
  }, 500);
};

// 菜单相关逻辑封装

// 一级菜单点击处理
const handleClickOne = (item) => {
  // 处理 /customize 路径的特殊情况
  if (item && item.path === "/customize") {
    // 如果有子菜单则不跳转
    if (item.children && item.children.length > 0) {
      return;
    } else {
      // 必须有id才跳转
      if (!item.id) return;
      // 跳转到customize页面并传递id参数
      router.push({
        path: "/customize",
        query: { id: item.id },
      });
    }
    return;
  } else {
    // 其他路径直接跳转
    router.push(item.path);
  }
};

// 二级菜单点击处理
const handleClick = (item, e) => {
  if (item && item.path === "/customize") {
    // 处理文件类型(columnType == '4')，直接打开文件链接
    if (e.columnType === "4") {
      window.open(e.fileUrl);
      return;
    }

    // 检查是否在路由映射中（路径以已注册路由开头即可）
    const routerMap = webGroups.children || [];
    const routerMapArr = routerMap.map((c) => `/${c.path}`);
    const matchedRoute = routerMapArr.find(route => e.path?.startsWith(route));
    if (matchedRoute) {
      router.push({ path: e.path });
      return;
    }

    // 必须有id才跳转
    if (!e.id) return;

    // 跳转到customize页面并传递id参数
    router.push({
      path: item.path,
      query: { id: e.id },
    });
  } else {
    // 父菜单不是/customize的情况
    if (e.path === "/customize") {
      // 处理文件类型(columnType == '4')，直接打开文件链接
      if (e.columnType === "4") {
        window.open(e.fileUrl);
        return;
      }

      // 必须有id才跳转
      if (!e.id) return;

      // 跳转到customize页面并传递id参数
      router.push({
        path: e.path,
        query: { id: e.id },
      });
      return;
    } else {
      // 其他路径直接跳转
      router.push(e.path);
    }
  }
};

// 退出登录处理
const outlogin = async () => {
  modal
    .confirm("是否确认退出登录")
    .then(async () => {
      logout()
        .then(() => {
          pcLogout();
        })
        .catch(() => {
          pcLogout();
        });
    })
    .catch(() => { });
};
const pcLogout = () => {
  removeToken();
  localStorage.clear();
  sessionStorage.clear();
  Cookies.remove("Path");
  Cookies.remove("authinfo");
  router.push("/");
  setTimeout(() => {
    location.reload();
  }, 1000);
};
// 路由跳转封装（带刷新）
async function routerTo(path) {
  await router.push(path);
  location.reload();
}

// 获取菜单项激活状态
const getActiveClass = (item) => {
  // 精确路径匹配
  if (route.fullPath === item.path) {
    return true;
  }

  // 检查子菜单是否有激活项
  if (item.children && item.children.length > 0) {
    return item.children.some((child) => getActiveClassChiild(item, child));
  }

  // 处理 /customize 路径的特殊情况
  if (item.path === "/customize") {
    return route.fullPath === `${item.path}?id=${item.id}`;
  }
};

// 获取子菜单项激活状态
const getActiveClassChiild = (item, value) => {
  // 父菜单是/customize的情况
  if (item.path === "/customize") {
    const childPath = `${item.path}?id=${value.id}`;
    return route.fullPath === childPath || value.path === route.fullPath;
  } else {
    // 子菜单是/customize的情况
    if (value.path === "/customize") {
      const childPath = `${value.path}?id=${value.id}`;
      return route.fullPath === childPath;
    } else {
      // 精确路径匹配
      return value.path === route.fullPath;
    }
  }
  return false;
};
const userinfo = ref({});
const userinfolist = () => {
  getAuthInfo().then((res) => {
    userinfo.value = res.data;
    setinfo(JSON.stringify(res.data));
    // 更新未处理总数
    updateWeichulizongshu();
  });
};
let isTeacher = $computed(() => {
  return userinfo.value?.identityInfoList?.some((item) => (item.certificationType == 'teacher' && item.checkStatus == "6"));
});
if (getToken()) {
  userinfolist();
  unReadCountlist();
}
</script>


<style scoped lang="scss">
:deep(.el-dialog) {
  padding: 60px 50px 20px 50px;
}

.global-title {
  margin-left: 80px;

  img {
    height: 56px;
  }
}

.nav-link {
  color: #333333;
  font-weight: 500;
  transition: color 0.15s ease-in-out;
  margin-left: 70px;
  padding: 28px 0;
  font-size: 20px;
  color: #333333;
  flex-shrink: 0;
}

.nav-link:hover {
  color: #3169f8;
}

.nav-link-active {
  color: #3169f8;
  font-weight: 600;
  position: relative;
}

.nav-link-active::after {
  content: "";
  position: absolute;
  bottom: 1px;
  left: 0;
  background-color: #3169f8;
  border-radius: 1.5px;
  width: 100%;
  height: 6px;
  background: #3169f8;
  border-radius: 10px 10px 0px 0px;
}

.position-box:hover .absolute-pop {
  display: block;
}

.position-box:hover .nav-link {
  color: #3169f8;
}

.absolute-pop {
  transform: translateX(-25%);
  min-height: 134px;
  padding-top: 32px;
  display: none;
}

.absolute-content {
  height: 100%;
  background: #ffffff;
  box-shadow: 0px 1px 15px 1px rgba(0, 0, 0, 0.1);
  border-radius: 8px 8px 8px 8px;
  color: #333333;
  display: grid;
  grid-template-columns: 1fr;
  /* 每行一个元素 */
  grid-row-gap: 20px;
  /* 控制上下间距 */
  padding: 20px 24px;
  min-width: 200px;
}

.absolute-content-item {
  display: flex;
  justify-content: center;
  gap: 20px;
  font-size: 20px;
  color: #333333;
  font-weight: 400;
}

.absolute-content-item:hover {
  color: #3169f8;
}

.absolute-content-item-active {
  color: #3169f8;
}

.tongzhi {
  font-size: 20px;
  color: #333333;
  font-weight: 400;
  margin-right: 30px;
  cursor: pointer;
}

.tongzhi-xiangqing {
  width: 100%;
  height: 50px;
  border-radius: 10px;
  background-color: #fffae9;

  line-height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tongzhi-xiangqing-text {
  width: 99%;
  font-size: 20px;
  color: #c49401;
  font-weight: 600;
  text-indent: 10px;
  height: 50px;
  overflow: hidden;
  line-height: 50px;
}

.out {
  margin-right: 50px;
  font-size: 20px;
  color: #333333;
  font-weight: 400;
  position: relative;
  // left: 50px;
  display: flex;
  align-items: center;

  img {
    width: 60px;
    height: 60px;
    border-radius: 50%;
  }
}

.touxiang {}

.out:hover .xiangqing {
  opacity: 1;
  visibility: visible;
  transition: opacity 0.3s ease, visibility 0s ease;
}

.xiangqing {
  opacity: 0;
  visibility: hidden;
  position: absolute;
  top: 60px;
  left: -400px;
  width: 520px;
  z-index: 999999 !important;
  // height: 612px;
  background: #ffffff;
  box-shadow: 0px 3px 20px 1px rgba(0, 0, 0, 0.1);
  border-radius: 10px 10px 10px 10px;
  transition: opacity 0.3s ease 1s, visibility 0s ease 0.5s;
}

.txandrz {
  width: 100%;
  display: flex;

  margin-top: 25px;
  padding-left: 30px;

  .rztouxiang {
    width: 80px;
    height: 80px;
    border-radius: 50%;
  }

  .xingming {
    margin-top: 10px;
    margin-left: 20px;

    .name {
      font-family: Source Han Sans CN, Source Han Sans CN;
      font-weight: bold;
      font-size: 22px;
      color: #333333;

      text-align: left;
      font-style: normal;
      text-transform: none;
    }

    .renzheng {
      margin-top: 5px;
      display: flex;
      justify-content: space-between;
      font-family: Source Han Sans CN, Source Han Sans CN;
      font-weight: 400;
      font-size: 17px;
      color: #999999;
      line-height: 24px;
      text-align: left;
      font-style: normal;
      text-transform: none;

      .tubiao {
        width: 18px;
        height: 18px;
      }
    }
  }
}

.xuexi {
  width: 460px;
  // padding-bottom: 30px;
  // height: 133px;
  background: #f3f7fe;
  border-radius: 6px 6px 6px 6px;
  margin: 30px;
  box-sizing: border-box;

  .gerenzhongxin {
    font-family: Source Han Sans CN, Source Han Sans CN;
    font-weight: 400;
    font-size: 23px;
    color: #333333;
    line-height: 32px;
    text-align: left;
    font-style: normal;
    text-transform: none;
    padding-top: 20px;
    cursor: pointer;
    display: flex;
    align-items: center;

    img {
      width: 36px;
      height: 36px;
      background: #cbdefb;
      margin-right: 15px;
      margin-left: 22px;
    }
  }

  .xiangxi {
    font-family: Source Han Sans CN, Source Han Sans CN;
    font-weight: 400;
    font-size: 17px;
    color: #666666;
    line-height: 24px;
    text-align: left;
    font-style: normal;
    text-transform: none;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-start;
    margin-top: 30px;
    padding: 0 30px 0 calc(22px + 18px - 9px);
    cursor: pointer;

    .danxiang {
      display: flex;
      align-items: center;
      margin-bottom: 32px;
      width: 30%;

      img {
        width: 18px;
        height: 18px;
        background: #36c29c;
        margin-right: 8px;
      }
    }
  }
}

.saishi {
  width: 460px;
  height: 186px;
  background: #f7f4ff;
  border-radius: 6px 6px 6px 6px;
  margin: 30px;
}

.gerenshezhi {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 72px;
  background: #ffffff;
  border-radius: 0px 0px 10px 10px;
  border-top: 1px solid #e4e4e4;

  .geren {
    width: 49%;
    height: 72px;
    font-family: Source Han Sans CN, Source Han Sans CN;
    font-weight: 400;
    font-size: 20px;
    color: #999999;
    line-height: 72px;
    text-align: center;
    font-style: normal;
    text-transform: none;
    cursor: pointer;
  }

  .shuxian {
    width: 1px;
    height: 14px;
    border: 1px solid #e4e4e4;
  }

  .outlogin {
    width: 49%;
    font-family: Source Han Sans CN, Source Han Sans CN;
    font-weight: 400;
    font-size: 20px;
    color: #999999;
    line-height: 72px;
    text-align: center;
    font-style: normal;
    text-transform: none;
    cursor: pointer;
  }
}

.notice-item {
  width: 100%;
  height: 50px;
  line-height: 50px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  padding-left: 30px;

  .laba {
    width: 25px;
  }
}

.chahao {
  width: 18px;
  margin-right: 20px;
  cursor: pointer;
  position: absolute;
  right: 20px;
}
</style>