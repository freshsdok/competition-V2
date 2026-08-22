<template>
  <div class="base-page">
    <div class="container-custom self-custom">
      <Breadcrumbar />
      <div class="page-content flex justify-between align-start">
        <div class="page-content-left">
          <div class="page-content-left-user flex justify-start align-center">
            <img
              alt=""
              :src="userInfo.avatar"
              v-if="userInfo.avatar"
              class="page-content-left-user-avatar"
            />
            <img
              alt=""
              src="@/assets/images/avatar.png"
              v-else
              class="page-content-left-user-avatar"
            />
            <div
              class="page-content-left-user-name flex flex-col justify-center items-center"
            >
              <div class="use-name flex justify-center items-center">
                {{ userInfo?.nickName }}
              </div>
            </div>
          </div>
          <div
            class="page-content-left-title"
            :class="{ active: activeIndex == 1 }"
          >
            赛事报名
          </div>
        </div>
        <div class="page-content-right">
          <div class="top-bar">
            <div class="top-bar-text flex justify-center items-center">
              <span>赛事报名</span>
              <div class="top-bar-line w-full bg-[#3169F8]"></div>
            </div>
          </div>
          <div
            class="btm-content"
            v-if="activeIndex == 1"
            v-loading="applyloading"
          >
            <template
              v-if="
                applyDetail && applyDetail.applyStatus && applyloading === false
              "
            >
              <div
                v-if="
                  applyDetail.applyStatus == '2' ||
                  applyDetail.applyStatus == '3'
                "
                class="w-full flex-col flex justify-center items-center mt-16"
              >
                <img
                  src="@/assets/images/shenhe.png"
                  alt=""
                  class="mb-4 text-[#333333]"
                />
                <span>报名审核中...</span>
                <el-button
                  type="primary"
                  size="medium"
                  class="mt-4 back-com"
                  @click="backCom()"
                  >返回赛事</el-button
                >
              </div>
              <div
                v-else-if="applyDetail.applyStatus == '5'"
                class="w-full flex-col flex justify-center items-center mt-16"
              >
                <img
                  src="@/assets/images/close.png"
                  alt=""
                  class="mb-4 text-[#333333]"
                />
                <span>报名失败</span>
                <span v-if="applyDetail.applyReason">{{
                  applyDetail.applyReason
                }}</span>
                <div class="flex justify-center items-center">
                  <el-button
                    type="primary"
                    size="medium"
                    class="mt-4 back-com"
                    @click="reApply()"
                    >重新报名</el-button
                  >
                  <el-button
                    type="primary"
                    size="medium"
                    class="mt-4 back-com"
                    @click="backCom()"
                    >返回赛事</el-button
                  >
                </div>
              </div>
              <div
                v-else
                class="w-full flex-col flex justify-center items-center mt-16"
              >
                <img
                  src="@/assets/images/baomingchenggong.png"
                  alt=""
                  class="mb-4 text-[#333333]"
                />
                <span>报名成功</span>
                <div class="flex justify-center items-center">
                  <!-- flag 1 个人 2 队张 3 团员 -->
                  <span
                    v-if="applyDetail.flag == '3'"
                    class="text-[#999999] mr-2"
                  >
                  </span>
                  <template v-else>
                    <template
                      v-if="
                        [4, '4', 'pending'].includes(applyDetail.applyStatus)
                      "
                    >
                      <el-button
                        class="apply-btn nav-buttons-item mt-4"
                        @click="paymentApply('1')"
                        >已报名，立即缴费</el-button
                      >
                    </template>
                    <template
                      v-else-if="['failed'].includes(applyDetail.applyStatus)"
                    >
                      <el-button
                        class="apply-btn nav-buttons-item mt-4"
                        @click="paymentApply('2')"
                        >支付失败，重新缴费</el-button
                      >
                    </template>
                    <template
                      v-else-if="['paid'].includes(applyDetail.applyStatus)"
                    >
                      <div class="paid mt-4 mr-4 text-[#999999]">
                        您已支付成功，返回赛事即可上传作品
                      </div>
                    </template>
                  </template>
                  <el-button class="nav-buttons-item mt-4" @click="backCom()"
                    >返回赛事</el-button
                  >
                </div>
              </div>
            </template>
            <template
              v-if="
                (!applyDetail || !applyDetail.applyStatus) &&
                applyloading === false
              "
            >
              <div class="info-block flex justify-start items-center mt-6">
                个人信息
              </div>
              <div class="team-user-info">
                <MyInfo :value="userInfo" />
                <div
                  class="u-i-line"
                  v-if="
                    getTeamMemberListArr && getTeamMemberListArr.teamLeaderId
                  "
                  style="align-items: flex-start"
                >
                  <p class="u-i-line-title">团队成员</p>
                  <div
                    class="u-i-line-content flex-1 flex flex-wrap"
                    v-if="
                      getTeamMemberListArr?.teamMemberRelaList &&
                      getTeamMemberListArr?.teamMemberRelaList.length > 0
                    "
                  >
                    <p
                      v-for="item in getTeamMemberListArr?.teamMemberRelaList"
                      :key="item.userId"
                      class="mr-4 mb-4"
                    >
                      <span
                        v-if="getTeamMemberListArr.teamLeaderId == item.userId"
                        class="text-[#3169F8]"
                      >
                        <span>{{ item.userName }}(队长)</span>
                      </span>
                      <span v-else>{{ item.userName }}</span>
                    </p>
                  </div>
                </div>
              </div>
              <div class="info-block flex justify-start items-center mt-8">
                报名信息
              </div>
              <el-form
                ref="fromRef"
                :model="form"
                :rules="rules"
                :validate-on-rule-change="false"
                v-loading="teamLoading"
                label-width="120px"
                class="join-team-form"
              >
                <el-form-item label="关联赛事" required>
                  <div>{{ route.query?.saiShiName }}</div>
                </el-form-item>
                <el-form-item label="赛道" prop="competitionTrackName">
                  <el-select
                    v-model="form.competitionTrackName"
                    filterable
                    style="width: 100%"
                    @change="getTrackGroupList"
                    placeholder="请选择赛道"
                  >
                    <el-option
                      v-for="item in competitionTrackList"
                      :key="item.competitionTrackName"
                      :label="item.competitionTrackNameDesc"
                      :value="item.competitionTrackName"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="组别" prop="groupClassify">
                  <el-select
                    v-model="form.groupClassify"
                    filterable
                    style="width: 100%"
                    placeholder="请选择组别"
                    no-data-text="请先选择赛道"
                  >
                    <el-option
                      v-for="item in tracKgroupList"
                      :key="item.dictValue"
                      :label="item.dictLabel"
                      :value="item.dictValue"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="指导教师" prop="guideTeacher">
                  <el-select
                    v-model="form.guideTeacher"
                    multiple
                    filterable
                    remote
                    reserve-keyword
                    allow-create
                    style="width: 100%"
                    remote-show-suffix
                    default-first-option
                    placeholder="请输入或搜索选择指导教师"
                    :remote-method="remoteMethod"
                    :loading="supervisorLoading"
                  >
                    <el-option
                      v-for="item in supervisorOptions"
                      :key="item.userId"
                      :label="item?.authInfo?.realName || item.userName"
                      :value="item.userId"
                    >
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="GPA分数" prop="gapScore">
                  <el-input-number
                    v-model="form.gapScore"
                    placeholder="请输入GPA分数"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="">
                  <el-button
                    type="primary"
                    @click="submitForm"
                    class="w-btn bgcolor"
                    v-loading="submitFormLoading"
                    >立即报名</el-button
                  >
                </el-form-item>
                <el-form-item v-if="form.teamCode">
                  <el-button
                    v-if="form.checkStatus && form.checkStatus == '5'"
                    v-loading="submitFormLoading"
                    type="primary"
                    @click="submitForm()"
                    class="w-btn bgcolor"
                    >重新报名</el-button
                  >
                </el-form-item>
              </el-form>
            </template>
          </div>
        </div>
      </div>
    </div>
    <!-- 支付弹窗 -->
    <PayDialog ref="payDialogRef" @reLoad="reLoad" />
  </div>
</template>

<script setup>
import Breadcrumbar from "@/components/breadcrumbar.vue";
import { getUserCenterInfo } from "@/api/index.js";
import { useRouter, useRoute } from "vue-router";
import {
  getUserCompetitionDetailInfoById,
  getTeamMemberList,
  teacherList,
  userSaveApplyCompetitionInfo,
  checkCompetitionApplyStatusByUser,
  getCompetitionTrackList,
} from "@/api/visualization/index.js";
import Modal from "@/plugins/modal.js";
import { cloneDeep } from "lodash";
import { useDict } from "@/utils/dict";
import PayDialog from "./components/pay.vue";
import MyInfo from "./components/myinfo.vue";
const { pay_status } = useDict("pay_status");
let route = useRoute();
let router = useRouter();
let activeIndex = $ref(1);
const changeTabs = (index) => {
  activeIndex = index;
};

// 返回赛事
const backCom = () => {
  router.replace({
    path: "/event/detail",
    query: {
      competitionId: route.query.competitionId,
      competitionSeriesId: route.query.competitionSeriesId,
      saiShiName: route.query.saiShiName,
    },
  });
};
const baseRules = {
  groupClassify: [{ required: true, message: "请选择组别", trigger: "blur" }],
  competitionTrackName: [
    { required: true, message: "请选择赛道", trigger: "blur" },
  ],
};
// 初始化rules为空对象而不是null，避免后续动态设置时自动触发表单验证
let rules = $ref({});
let pageDetail = $ref({});
const getDetail = () => {
  let query = {
    competitionId: route.query.competitionId,
    competitionSeriesId: route.query.competitionSeriesId,
  };
  getUserCompetitionDetailInfoById(query).then(async (res) => {
    if (res.code == 200) {
      pageDetail = res.data;
      // 使用nextTick确保DOM更新后再设置rules，避免自动触发表单验证
      if (pageDetail.isTeacherNess == "1") {
        rules = {
          ...baseRules,
          guideTeacher: [
            { required: true, message: "请选择指导教师", trigger: "blur" },
          ],
        };
      } else {
        rules = { ...baseRules };
      }
    }
  });
};

let tracKgroupList = $ref([]);
// 获取赛道组别列表
const getTrackGroupList = (id) => {
  form.groupClassify = "";
  let item = competitionTrackList.find(
    (item) => item.competitionTrackName == id
  );
  if (item) {
    tracKgroupList = item.competitionGroupList || [];
  } else {
    tracKgroupList = [];
  }
};

const payDialogRef = ref(null);
const paymentApply = (type) => {
  const res = {
    myActionType: type,
    userId: applyDetail.userId || "",
    commodityName: route.query.saiShiName || "",
    commodityType: "competition",
    commodityId: route.query.competitionSeriesId || "",
    amount: applyDetail.amount,
  };
  payDialogRef.value.resetPayment(res);
};

// 信息提交
let form = $ref({
  gapScore: null,
  groupClassify: "",
  competitionTrackName: "",
  guideTeacher: [],
});
let fromRef = ref();
let submitFormLoading = $ref(false);
const submitForm = () => {
  fromRef.value.validate((valid) => {
    if (valid) {
      Modal.confirm("提交后，报名信息将不可修改", "确认提交吗？").then(() => {
        console.log("submit!", "form", form);
        let apiFunc = userSaveApplyCompetitionInfo;
        let query = cloneDeep(form);
        // 去除guideTeacher中的前缀
        const cleanGuideTeacher = query.guideTeacher.map((id) =>
          id.startsWith("xx_") ? id.substring(3) : id
        );

        query = {
          ...query,
          competitionId: route.query.competitionId,
          competitionSeriesId: route.query.competitionSeriesId,
          guideTeacher: cleanGuideTeacher.length
            ? cleanGuideTeacher.join(",")
            : "",
        };
        submitFormLoading = true;
        apiFunc(query)
          .then((res) => {
            submitFormLoading = false;
            if (res.code === 200) {
              Modal.notifySuccess("提交成功");
              form = {
                gapScore: null,
                groupClassify: "",
                competitionTrackName: "",
                guideTeacher: [],
              };
              getApplyStatus();
            } else {
              Modal.notifyError(res.msg || "提交失败");
            }
          })
          .catch((err) => {
            submitFormLoading = false;
          });
      });
    }
  });
};

// 重新报名,显示报名表单
const reApply = () => {
  applyDetail.applyStatus = null;
};

// 模糊搜索老师列表
let supervisorLoading = $ref(false);
let supervisorOptions = $ref([]);
const remoteMethod = (query) => {
  if (query) {
    console.log("query", query, supervisorOptions);
    supervisorLoading = true;
    teacherList({ userName: query })
      .then((res) => {
        if (res.code === 200) {
          let rows = res.data;
          // 为rows中的userId添加前缀
          const prefixedRows = rows.map((item) => ({
            ...item,
            userId: `xx_${item.userId}`,
          }));

          // 使用reduce进行数组合并和去重
          const tempMap = [...supervisorOptions, ...prefixedRows].reduce(
            (map, item) => {
              // 只有当Map中不存在该teacherId时才添加，确保保留第一个出现的元素
              if (!map.has(item.userId)) {
                map.set(item.userId, item);
              }
              return map;
            },
            new Map()
          );
          // 转换Map值为数组
          supervisorOptions = Array.from(tempMap.values());
          supervisorLoading = false;
        } else {
          supervisorLoading = false;
        }
      })
      .catch((err) => {
        supervisorLoading = false;
      });
  }
};
// 去认证
const toAuth = (type) => {
  router.push({
    path: "/personal/accountmanagement",
    query: {
      classification: type,
    },
  });
};
// 去赛事详情
const handleClickSaiShi = () => {
  router.push({
    path: "/event/detail",
    query: {
      competitionId: route.query.competitionId,
      competitionSeriesId: route.query.competitionSeriesId,
    },
  });
};

// 用户个人认证信息
let userInfo = $ref({});
const getUserInfo = async () => {
  getUserCenterInfo().then((res) => {
    if (res.code == 200) {
      userInfo = res.data || {};
      getTeamMemberLists();
      getApplyStatus();
      getDetail();
      getTrackList();
    }
  });
};

let competitionTrackList = $ref([]);
const getTrackList = () => {
  getCompetitionTrackList({
    competitionId: route.query.competitionId,
    competitionSeriesId: route.query.competitionSeriesId,
  }).then((res) => {
    if (res.code === 200) {
      competitionTrackList = res.data || [];
    }
  });
};

// 队长下面的成员
let getTeamMemberListArr = $ref({});
const getTeamMemberLists = () => {
  let query = {
    competitionId: route.query.competitionId,
    competitionSeriesId: route.query.competitionSeriesId,
  };
  getTeamMemberList(query).then((res) => {
    if (res.code === 200) {
      getTeamMemberListArr = res.data || {};
    }
  });
};

// 查询用户赛事报名状态
let applyDetail = $ref({});
let applyloading = $ref(null);
const getApplyStatus = () => {
  applyloading = true;
  let query = {
    competitionId: route.query.competitionId,
    competitionSeriesId: route.query.competitionSeriesId,
  };
  checkCompetitionApplyStatusByUser(query)
    .then((res) => {
      if (res.code === 200) {
        applyDetail = res.data || {};
      }
      applyloading = false;
    })
    .catch((err) => {
      applyloading = false;
    });
};

// 我的团队详情
let teamLoading = $ref(false);

// 重新加载
const reLoad = () => {
  getUserInfo();
};

// 初始化
getUserInfo();
</script>

<style scoped lang="scss">
.page-content {
  background: #ffffff;
  border: 1px solid #e4e4e4;
  min-height: 600px;
  margin-bottom: 45px;
  .page-content-left {
    width: 300px;
    flex-shrink: 0;
    border-right: 1px solid #e4e4e4;
    .page-content-left-user {
      padding: 20px 30px;
      box-sizing: border-box;
      .page-content-left-user-avatar {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        object-fit: cover;
      }
      .page-content-left-user-name {
        padding: 3px 0;
        margin-left: 15px;
        height: 80px;
        .use-name {
          font-size: 20px;
          color: #333333;
          @include ellipsis(1);
          overflow: hidden;
          word-break: break-all;
        }
        .use-code {
          font-size: 14px;
          color: #999999;
        }
      }
    }
    .page-content-left-title {
      height: 60px;
      line-height: 60px;
      font-size: 20px;
      color: #333333;
      width: 100%;
      text-align: center;
      cursor: pointer;
    }
    .active {
      color: #ffffff;
      background: #83a5fb;
    }
  }
  .page-content-right {
    flex-grow: 1;
    .top-bar {
      box-sizing: border-box;
      border-bottom: 2px solid #e4e4e4;
      height: 60px;
      padding: 0 45px;
      .top-bar-text {
        font-size: 20px;
        color: #333333;
        width: fit-content;
        position: relative;
        height: 100%;
        .top-bar-line {
          height: 3px;
          background: #3169f8;
          position: absolute;
          left: 0;
          bottom: -2px;
          height: 2px;
        }
      }
    }
    .btm-content {
      padding: 45px;
      :deep(.el-tabs) {
        .el-tabs__item {
          width: 250px;
          height: 64px;
          font-size: 20px;
          color: #333333;
          background: #ffffff;
          border-radius: 14px;
          border: 1px solid #cecece;
          padding: 0;
          + .el-tabs__item {
            margin-left: 20px;
          }
        }
        .is-active {
          border: 1px solid #3b72ff;
        }
        .el-tabs__active-bar {
          display: none;
          height: 0;
        }
        .el-tabs__nav-wrap {
          &::after {
            background: none;
            display: none;
            height: 0;
          }
        }
      }
    }
    .info-block {
      padding: 15px 20px;
      height: 50px;
      background: #f5f5f5;
      font-size: 20px;
      color: #333333;
    }
    .saishi {
      cursor: pointer;
      color: #3b72ff;
      text-decoration: underline;
    }
    .teamCode {
      font-size: 18px;
      color: #999999;
    }

    .team-user-info {
      .u-i-line {
        display: flex;
        align-items: center;
        justify-content: flex-start;
        margin: 30px 0;
        .u-i-line-title {
          flex-shrink: 0;
          font-size: 18px;
          color: #333333;
          width: 120px;
          text-align: right;
          margin-right: 50px;
        }
        .u-i-line-content {
          font-size: 18px;
          color: #333333;
          line-height: 1;
          margin-right: 16px;
        }
        .rz-line {
          font-size: 16px;
          color: #999999;
          line-height: 1;
        }
        .rz-img {
          width: 16px;
          height: 16px;
          margin-right: 5px;
        }
        .rz-btn-renzheng {
          font-size: 16px;
          color: #3b72ff;
          font-weight: 500;
          cursor: pointer;
        }
      }
    }
    .join-team-form {
      margin-top: 35px;
      width: 70%;
      font-size: 18px;
      :deep(.el-form-item) {
        margin-bottom: 30px;
        .el-form-item__label {
          font-size: 18px;
          color: #333333;
        }
        .el-form-item__content {
          font-size: 18px;
          color: #333333;
        }
      }
    }
    .w-btn {
      width: 120px;
    }
  }
  .all-team-list {
    margin-top: 35px;
    .team-list-item {
      padding: 20px 20px 20px 20px;
      box-sizing: border-box;
      border: 1px solid #e4e4e4;
      margin-bottom: 20px;
      cursor: pointer;
      border-radius: 10px;
      .text-name-content {
        color: #999;
      }
      .text-name-des {
        @include ellipsis(3);
      }
      .team-name {
        margin-bottom: 10px;
        display: flex;
        align-items: flex-start;
        justify-content: flex-start;
        position: relative;
        .team-name-action {
          position: absolute;
          right: 0;
          top: 0;
        }
        .text-name-lable {
          flex-shrink: 0;
        }
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
  .back-com {
    background: linear-gradient(#3169f8 0%, #33dbdb 100%);
    border-radius: 10px;
    border: none;
  }
  .back-jiaofei {
    background: linear-gradient(#f68801 0%, #ffdc2d 100%);
    border-radius: 10px;
    border: none;
  }
}
.nav-buttons-item {
  background: linear-gradient(#3169f8 0%, #33dbdb 100%);
  min-width: 100px;
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
</style>