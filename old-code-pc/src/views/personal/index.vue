<template>
  <div class="">
    <div class="personalbg">
      <div class="neirong">
        <div class="titleleft">
          <div class="gerenxinxi">
            <div>
              <img
                :src="userinfo?.avatar ? userinfo.avatar : img1"
                class="toux"
                alt=""
              />
              <el-button
                class="buttonprimary"
                @click="zhanghaoshezhi('personaldata')"
              >
                <el-icon><Tools /></el-icon>设置</el-button
              >
            </div>

            <div class="xingming">
              <div class="name">
                <!-- {{ userinfo.nickName }} -->

                <!-- {{ userinfo?.authInfo?.realName || userinfo?.nickName }} -->
                 {{userinfo.authStatus==5? userinfo?.authInfo?.realName : userinfo?.nickName }}
                <img :src="yonghuqiehuan" class="renz" alt="" />
              </div>
              <div class="renzheng">
                <div
                  style="display: flex; align-items: center; cursor: pointer"
                  @click="zhanghaoshezhi('nameauthentication')"
                >
                  <img
                    :src="baomingchenggong"
                    class="tubiao"
                    alt=""
                    v-if="userinfo?.authStatus == 5"
                  />
                  <img :src="tanhao" class="tubiao" alt="" v-else />
                  {{
                    real_name_auth_status.find(
                      (item) => item.value == userinfo?.authStatus
                    )?.label
                  }}
                </div>
                <div
                  v-if="
                    userinfo?.identityInfoList &&
                    userinfo?.identityInfoList.length > 0
                  "
                  style="display: flex; align-items: center; cursor: pointer"
                  @click="zhanghaoshezhi('identityauthentication')"
                >
                  <template v-if="userinfo?.identityInfoList[0].checkStatus == 6">
                    <img
                      :src="baomingchenggong"
                      class="tubiao"
                      alt=""
                    />
                    {{
                      identity_status.find(
                        (item) =>
                          item.value == userinfo?.identityInfoList[0].checkStatus
                      )?.label
                    }}
                  </template>
                </div>
                <div
                  v-else
                  style="display: flex; align-items: center; cursor: pointer"
                  @click="zhanghaoshezhi('identityauthentication')"
                >
                  <img :src="tanhao" class="tubiao" alt="" />
                  身份未认证
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="titleright">
          <!-- <div>
            <p>我的赛事</p>
            <p class="sl">0</p>
          </div>
          <div>
            <p>我的收藏</p>
            <p class="sl">0</p>
          </div>
          <div>
            <p>学习时长</p>
            <p class="sl">10h</p>
          </div> -->
        </div>
      </div>
    </div>
    <div class="personalbgss">
      <div class="Studystatistics">
        <div class="Studystatisticsleft">
          <!-- <div
            class="Studystatisticsleftbq"
            :class="
              lefttabs == '学习统计'
                ? 'Studystatisticsleftbqxuanzhong'
                : 'Studystatisticsleftbqout'
            "
            @click="xuanzhongxiang('学习统计')"
          >
            学习统计
          </div> -->
                <!-- <div
            class="Studystatisticsleftbq"
            :class="
              lefttabs == '我的订单'
                ? 'Studystatisticsleftbqxuanzhong'
                : 'Studystatisticsleftbqout'
            "
            @click="xuanzhongxiang('我的订单')"
          >
            我的订单
          </div> -->
          <div
            class="Studystatisticsleftbq"
            :class="
              lefttabs == '我的赛事'
                ? 'Studystatisticsleftbqxuanzhong'
                : 'Studystatisticsleftbqout'
            "
            @click="xuanzhongxiang('我的赛事')"
          >
            我的赛事
          </div>
           <div
            class="Studystatisticsleftbq"
            :class="
              lefttabs == '我的团队'
                ? 'Studystatisticsleftbqxuanzhong'
                : 'Studystatisticsleftbqout'
            "
            @click="xuanzhongxiang('我的团队')"
          >
            我的团队
          </div>
          <div
            class="Studystatisticsleftbq"
            :class="
              lefttabs == '开票记录'
                ? 'Studystatisticsleftbqxuanzhong'
                : 'Studystatisticsleftbqout'
            "
            @click="xuanzhongxiang('开票记录')"
          >
            开票记录
          </div>
          <div
            class="Studystatisticsleftbq"
            :class="
              lefttabs == '我的文件'
                ? 'Studystatisticsleftbqxuanzhong'
                : 'Studystatisticsleftbqout'
            "
            @click="xuanzhongxiang('我的文件')"
          >
            我的文件
          </div>
          <div
            class="Studystatisticsleftbq"
            :class="
              lefttabs == '设备预约'
                ? 'Studystatisticsleftbqxuanzhong'
                : 'Studystatisticsleftbqout'
            "
            @click="xuanzhongxiang('设备预约')"
          >
            设备预约
          </div>
          <!-- <div
            class="Studystatisticsleftbq"
            :class="
              lefttabs == '访问历史'
                ? 'Studystatisticsleftbqxuanzhong'
                : 'Studystatisticsleftbqout'
            "
            @click="xuanzhongxiang('访问历史')"
          >
            访问历史
          </div> -->
        </div>
        <div class="Studystatisticsright">
          <el-tabs
            v-model="lefttabs"
            class="demo-tabs"
            @tab-click="handleClick"
          >
            <el-tab-pane :label="lefttabs" :name="lefttabs">
              <template #label>
                <span class="custom-tab-label">{{ lefttabs }}</span>
              </template>
              <Competition
                :userinfo="userinfo"
                v-if="lefttabs == '我的赛事' && isuserinfo"
              />

              <team
                :userinfo="userinfo"
                v-if="lefttabs == '我的团队' && isuserinfo"
              />
              <!-- <paymentrecords 
                :userinfo="userinfo"
                v-if="lefttabs == '我的订单' && isuserinfo"
              /> -->
              <invoice
                :userinfo="userinfo"
                v-if="lefttabs == '开票记录' && isuserinfo"
              />
              <myfile
                :userinfo="userinfo"
                v-if="lefttabs == '我的文件' && isuserinfo"
              />
              <SceneResourceReservation
                v-if="lefttabs == '设备预约' && isuserinfo"
              />
              <div
                v-if="lefttabs == '访问历史'"
               class="fangwenlishi"
              >
                <el-empty description="暂无数据"></el-empty>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import img1 from "@/assets/images/shawn-avatar.png";
import team from "./components/team.vue";
import Competition from "./personaltabs/Competition.vue";
import paymentrecords from "./personaltabs/paymentrecords.vue";
import invoice from "./personaltabs/invoice.vue";
import myfile from "./personaltabs/myfile.vue";
import SceneResourceReservation from "./personaltabs/SceneResourceReservation.vue";
import { useRouter } from "vue-router";
import { getAuthInfo } from "@/api/index";
import { onMounted } from "vue";
import { getinfo, getToken, setinfo } from "@/utils/auth";
import baomingchenggong from "@/assets/images/baomingchenggong.png";
import tanhao from "@/assets/images/tanhao.png";
import yonghuqiehuan from "@/assets/images/yonghuqiehuan.png";
import { useRoute } from "vue-router";
const route = useRoute();
const router = useRouter();
const userinfo = ref({});
const lefttabs = ref("我的团队");

const { proxy } = getCurrentInstance();
const { identity_status, real_name_auth_status } = proxy.useDict(
  "identity_status",
  "real_name_auth_status"
);
const xuanzhongxiang = (tabs) => {
  lefttabs.value = tabs;
  router.push({
    path: "/personal/list",
    query: {
      lefttabs: tabs,
    },
  });
};
const zhanghaoshezhi = (item) => {
  router.push({
    path: "accountmanagement",
    query: {
      classification: item,
    },
  });
};
const isuserinfo = ref(false);
const userinfolist = () => {
  // userinfo.value = JSON.parse(getinfo());
  // isuserinfo.value = true;
  getAuthInfo().then((res) => {
    userinfo.value = res.data;
    setinfo(JSON.stringify(userinfo.value));
    isuserinfo.value = true;
  });
};

onMounted(() => {
  userinfolist();
  lefttabs.value = route.query.lefttabs ? route.query.lefttabs : lefttabs.value;
  console.log(lefttabs.value,123)
});
</script>


<style scoped lang="scss">
.personalbg {
  background-image: url("../../assets/images/bg.png");
  background-size: cover;
  .neirong {
    width: 1280px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;

    .titleleft {
      .gerenxinxi {
        display: flex;
        padding: 30px 0 30px 30px;

        .toux {
          width: 70px;
          height: 70px;
          border-radius: 50%;
        }
        .buttonprimary {
          margin-left: 10px;
          margin-top: 20px;
          width: 60px;
          height: 28px;
          background: #3169f8;
          border-radius: 4px 4px 4px 4px;
          border: 0;
          font-family: Source Han Sans CN, Source Han Sans CN;
          font-weight: 400;
          font-size: 15px;
          color: #ffffff;
          line-height: 21px;
          text-align: left;
          font-style: normal;
          text-transform: none;
        }
        .xingming {
          margin-top: 10px;
          margin-left: 20px;

          .name {
            font-family: Source Han Sans CN, Source Han Sans CN;
            font-weight: bold;
            font-size: 24px;
            color: #ffffff;
            line-height: 34px;
            text-align: left;
            font-style: normal;
            text-transform: none;
            display: flex;
            align-items: center;
            .renz {
              width: 22px;
              cursor: pointer;
            }
          }

          .renzheng {
            display: flex;
            justify-content: space-between;
            font-family: Source Han Sans CN, Source Han Sans CN;
            font-weight: 400;
            font-size: 16px;
            color: #ffffff;
            line-height: 22px;
            text-align: left;
            font-style: normal;
            text-transform: none;
            padding-top: 10px;
            div {
              margin-right: 20px;
            }
          }
        }
      }
    }

    .titleright {
      display: flex;
      justify-content: space-between;
      padding: 60px 0 30px 30px;
      font-family: Source Han Sans CN, Source Han Sans CN;
      font-weight: 400;
      font-size: 16px;
      color: #ffffff;
      line-height: 22px;
      text-align: center;
      font-style: normal;
      text-transform: none;
      p {
        font-size: 14px;
        text-align: center;
        color: #fff;
        margin-left: 30px;
      }
      .sl {
        margin-top: 10px;
      }
    }
  }
}

.personalbgss {
  padding:  30px 0;
  background-color: #fff;
  .Studystatistics {
    width: 1440px;
    margin: 30px auto;
    display: flex;

    .Studystatisticsleft {
      width: 300px;
      .Studystatisticsleftbq {
        height: 70px;
        line-height: 70px;
        text-indent: 70px;
        margin-top: 5px;
        cursor: pointer;
        font-size: 20px;
      }

      .Studystatisticsleftbqxuanzhong {
        background: #3169f8;
        clip-path: polygon(0% 0%, 0% 100%, 90% 100%, 100% 50%, 90% 0%);
        color: #fff;
        border-radius: 5px 0 0 5px;
      }

      .Studystatisticsleftbqout:hover {
        background: #3169f8;
        clip-path: polygon(0% 0%, 0% 100%, 90% 100%, 100% 50%, 90% 0%);
        color: #fff;
        border-radius: 5px 0 0 5px;
      }
    }

    .Studystatisticsright {
      width: 74%;
      margin-left: 5%;
    }
  }
}
.tubiao {
  width: 16px;
  height: 16px;
  margin-right: 5px;
}
:deep(.custom-tab-label) {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 20px;
  color: #333333;
  line-height: 28px;
  text-align: center;
  font-style: normal;
  text-transform: none;
}
.fangwenlishi{
font-size: 20px;
                  text-align: center;
                  line-height: 30px;
                  min-height: 400px;
}
</style>
