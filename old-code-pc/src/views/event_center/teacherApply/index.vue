<template>
  <div class="base-page event-detail-commons">
    <div class="container-custom self-custom">
      <Breadcrumbar />
      <EventLayout
        :tabs="leftTabsList"
        v-model:active-tab="leftTabsIndex"
        :showLeft="false"
        :user-info="userInfo"
      >
        <!-- 内容区域 -->
        <div class="page-right-content">
          <!-- 实际业务内容 -->
          <div class="content-card">
            <MyInfo :value="userInfo" topName="带队老师信息" />
            <div class="info-block flex justify-start items-center mt-6">
              带队老师报名
            </div>
            <div class="from-box">
              <div class="progress-box" v-if="upLoading">
                <!-- 上传进度 -->
                <el-progress
                  :percentage="progressPercent"
                  type="circle"
                ></el-progress>
                <div class="progress-text">正在解析文件中...</div>
              </div>
              <div class="u-i-line">
                <p class="u-i-line-title">
                  赛事名称
                </p>
                <p
                  class="u-i-line-content u-i-line-content-active"
                  @click="toDetail()"
                >
                  {{ pageDetail?.competitionSeriesName
                  }}{{ pageDetail?.competitionName || "-" }}
                </p>
              </div>
              <div class="action-up">
                <div class="action-xlsx flex items-end">
                  <!-- <a href="/报名模板.xlsx" class="action-xlsx-btn hvr-grow"
                    >报名模板下载</a
                  > -->
                  <span
                    class="action-xlsx-btn hvr-grow"
                    @click="downloadTemplate"
                    >报名模板下载</span
                  >
                  <el-upload
                    :action="upload.url"
                    :headers="upload.headers"
                    ref="uploadRef"
                    :limit="1"
                    accept=".xlsx,.xls"
                    :before-upload="beforeUpload"
                    :on-error="handleFileError"
                    :on-exceed="handleExceed"
                    :on-progress="handleProgress"
                    :on-success="onUploadSuccess"
                    v-model:file-list="fileList"
                    :show-file-list="false"
                  >
                    <div class="action-btn flex items-end">
                      <div class="action-xlsx-btn hvr-grow">
                        {{
                          tableData.length ? "重新上传报名信息" : "报名信息上传"
                        }}
                      </div>
                      <div class="action-xlsx-tips">
                        支持Excel格式(.xlsx/.xls)，单次限传1个文件，大小不超过50MB
                      </div>
                    </div>
                  </el-upload>
                </div>
                <div class="action-apply-container">
                  <div
                    class="action-apply hvr-grow"
                    :class="{
                      'disabled-btn':
                        !(tableData && tableData.length > 0) &&
                        isgouwuche.length == 0,
                    }"
                    @click="
                      () => {
                        if (
                          (tableData && tableData.length > 0) ||
                          isgouwuche.length > 0
                        )
                          goPay();
                      }
                    "
                    v-loading="payLoading"
                  >
                    <el-tooltip
                      class="box-item"
                      effect="dark"
                      placement="top"
                      v-if="isgouwuche.length > 0"
                    >
                      <template #content>
                        我的赛事已有{{ isgouwuche.length }}个团队<br />
                      </template>
                     查看已上传队伍/报名缴费
                    </el-tooltip>
                    <span v-else> 查看已上传队伍/报名缴费 </span>
                  </div>
                  <div class="order-tip" v-if="!(tableData && tableData.length > 0) && isgouwuche.length == 0">
                    已支付订单可在<span  class="order-link" @click="routerorder">我的订单</span>中查看
                  </div>
                </div>
              </div>
              <div v-if="upResoult?.type" class="mb-4">
                <el-alert :title="upResoult?.title" 
                          :type="upResoult?.type" 
                          show-icon
                          :description="upResoult?.description"
                          @close="closeAlert">
                    <template #default>
                      <div>
                        <div>{{ upResoult?.description }}</div>
                        <!-- 成功但是需要提示的情况 -->
                        <div v-if="upResoult?.type == 'success'&& (upResoult.successAlert && upResoult.successAlert?.length > 0)">
                          <div v-for="(item, index) in upResoult?.successAlert" 
                                :key="index"
                                class="success-tips">
                            {{ item }}
                          </div>
                        </div>
                      </div>
                    </template>
                </el-alert>
              </div>
              <vxe-table
                :data="tableData"
                style="width: 100%"
                border
                :header-cell-style="{
                  background: '#F2F5F7',
                }"
                empty-text="暂无报名信息"
              >
                <vxe-column type="seq" width="40"></vxe-column>
                <vxe-column
                  field="competitionTrackName"
                  title="参赛赛道"
                  min-width="100"
                >
                  <template #default="{ row }">
                    {{ row.competitionTrackName || "-" }}
                  </template>
                </vxe-column>
                <vxe-column
                  field="secondLevelName"
                  title="参赛组别/赛题"
                  min-width="130"
                >
                  <template #default="{ row }">
                    {{ row.secondLevelName || "-" }}
                  </template>
                </vxe-column>
                <!-- <vxe-column field="teamExcelId" title="团队编号" min-width="70">
                  <template #default="{ row }">
                    {{ row.teamCode || "-" }}
                  </template>
                </vxe-column> -->
                <vxe-column field="teamName" title="团队名称" min-width="76">
                  <template #default="{ row }">
                    {{ row.teamName || "-" }}
                  </template>
                </vxe-column>
                <vxe-column
                  field="competitionRoleName"
                  title="角色"
                  min-width="56"
                >
                  <template #default="{ row }">
                    {{ row.competitionRoleName || "-" }}
                  </template>
                </vxe-column>
                <vxe-column field="userName" title="姓名" min-width="50">
                  <template #default="{ row }">
                    {{ row.userName || "-" }}
                  </template>
                </vxe-column>
                <vxe-column field="idCard" title="证件号" min-width="110">
                  <template #default="{ row }">
                    {{ row.idCard || "-" }}
                  </template>
                </vxe-column>
                <vxe-column field="phone" title="手机号" min-width="72">
                  <template #default="{ row }">
                    {{ row.phone || "-" }}
                  </template>
                </vxe-column>
                <vxe-column field="email" title="邮箱" min-width="80">
                  <template #default="{ row }">
                    {{ row.email || "-" }}
                  </template>
                </vxe-column>
                <vxe-column field="sex" title="性别" min-width="40">
                  <template #default="{ row }">
                    {{ row.sex || "-" }}
                  </template>
                </vxe-column>
                <vxe-column field="classInfo" title="学级" min-width="60">
                  <template #default="{ row }">
                    {{ row.classInfo || "-" }}
                  </template>
                </vxe-column>
                <vxe-column field="profession" title="专业" min-width="60">
                  <template #default="{ row }">
                    {{ row.profession || "-" }}
                  </template>
                </vxe-column>
                <vxe-column field="nationalityName" title="国籍" min-width="60">
                  <template #default="{ row }">
                    {{ row.nationalityName || "-" }}
                  </template>
                </vxe-column>
                <vxe-column field="departmentName" title="院系" min-width="60">
                  <template #default="{ row }">
                    {{ row.departmentName || "-" }}
                  </template>
                </vxe-column>
              </vxe-table>
            </div>
          </div>
        </div>
      </EventLayout>
    </div>
  </div>
</template>

<script setup>
import Breadcrumbar from "@/components/breadcrumbar.vue";
import EventLayout from "../components/layout.vue";
import MyInfo from "./components/myinfo.vue";
import Modal from "@/plugins/modal.js";
import { getinfo, getToken } from "@/utils/auth";
import { useRoute, useRouter } from "vue-router";
// import { downloadJS } from '@/utils/request'
import {
  saveApplyCompetitionData,
  getTeamCompetitionInfo,
} from "@/api/teacher";
import { getUserCompetitionDetailInfoById } from "@/api/visualization/index.js";
import Decimal from 'decimal.js';
import { onUnmounted } from 'vue';
const route = useRoute();
const router = useRouter();
// 左侧一级选项卡数据
const leftTabsIndex = $ref("1");
const leftTabsList = $ref([{ id: "1", name: "赛事报名" }]);

// 已经上传的表格数据
let tableData = $ref([]);
// 排序
const sortChange = (sort) => {
  console.log(sort, "xx");
};

// 报名模板下载
const downloadTemplate = () => {
   downloadJS(
    import.meta.env.VITE_APP_BASE_API + `/file/excel/download`,
    `报名模板.xlsx`,
    "addName"
  );
};

// 去缴费
let payLoading = $ref(false);
const goPay = () => {
  payLoading = true;
  // 提交报名信息
  saveApplyCompetitionData({
    competitionSeriesId: route?.query?.competitionSeriesId || "",
  })
    .then((res) => {
      payLoading = false;
      if (res.code == 200) {
        goShopping();
      } else {
        Modal.msgError(res.msg || "报名信息提交失败");
      }
    })
    .catch((err) => {
      payLoading = false;
    });
};

// 用户个人认证信息
let userInfo = $computed(() => {
  let res = {};
  try {
    const info = getinfo();
    res = JSON.parse(info);
  } catch (error) {
    res = {};
  }
  return res;
});

// 跳转我的赛事购物车
const goShopping = () => {
  router.push({
    path: "/event/detail/teacherApply/shopping",
    query: {
      competitionSeriesId: route?.query?.competitionSeriesId || "",
    },
  });
};
const isgouwuche = ref([]);
const cheshi = () => {
  getTeamCompetitionInfo({
    competitionSeriesId: route?.query?.competitionSeriesId || "",
  }).then((res) => {
    if (res.code === 200) {
      isgouwuche.value = res.data;
    }
  });
};
cheshi();
// 跳转赛事详情
const toDetail = () => {
  // router.replace({
  //   path: '/event/detail',
  //   query: {
  //     competitionSeriesId: route?.query?.competitionSeriesId || ""
  //   }
  // })
  const competitionSeriesId = route?.query?.competitionSeriesId || "";
  const url = `/event/detail?competitionSeriesId=${encodeURIComponent(
    competitionSeriesId
  )}`;

  // 在新标签页中打开
  window.open(url, "_blank");
};
// 上传文件相关api.......
let upLoading = $ref(false);
let progressPercent = $ref(0);
let fileList = $ref([]);
let uploadTimer = null; // 存储定时器ID
const uploadRef = ref(null);
const upload = reactive({
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url:
    import.meta.env.VITE_APP_BASE_API +
    `/competition/userCompetition/importData/${route?.query?.competitionSeriesId}`,
});
// 上传成功后的处理
let upResoult = $ref({
  type: null,
  title: null,
  description: null,
  successAlert: []
});
// 关闭弹窗
const closeAlert = () => {
  upResoult = {
    type: null,
    title: null,
    description: null,
    successAlert: []
  };
}
const onUploadSuccess = (response, file) => {
  // 清除定时器
  if (uploadTimer) {
    clearInterval(uploadTimer);
    uploadTimer = null;
  }
  
  fileList = [];
  upLoading = false;
  if (response.code == 200) {
    tableData = response.data || [];
    uploadRef.value.clearFiles();
    upResoult = {
      type: "success",
      title: "上传成功",
      description: "报名信息上传成功",
      successAlert: []
    };
    if(tableData?.length > 0){
      tableData.forEach(item => {
        if(item?.message){
          upResoult.successAlert.push(item.message)
        }
      })
    }
  } else {
    upResoult = {
      type: "error",
      title: "上传失败",
      description: response.msg || "报名信息上传失败"
    };
    tableData = [];
  }
  // 重置进度
  progressPercent = 0;
};

// 独立的定时器方法，每隔200毫秒增加0-2之间的随机数
const startUploadTimer = () => {
  // 清除可能存在的旧定时器
  if (uploadTimer) {
    clearInterval(uploadTimer);
    uploadTimer = null;
  }
  
  // 创建新定时器
  uploadTimer = setInterval(() => {
    if (progressPercent < 99) {
      let randomIncrement;
      // 根据当前进度设置不同的随机增量范围
      if (parseFloat(progressPercent) < 60) {
        // 90%以下：0-1之间的随机数
        randomIncrement = Math.random() * 20;
      }else if (parseFloat(progressPercent) < 70) {
        // 90%以下：0-1之间的随机数
        randomIncrement = Math.random() * 5;
      }else if (parseFloat(progressPercent) < 80) {
        // 90%以下：0-1之间的随机数
        randomIncrement = Math.random() * 2;
      }else if (parseFloat(progressPercent) < 90) {
        // 90%以下：0-1之间的随机数
        randomIncrement = Math.random() * 0.5;
      }else {
        // 99%以下：0-0.5之间的随机数
        randomIncrement = Math.random() * 0.1;
      }
      // 更新进度，确保不超过99%
      const newProgress = Math.min(99, parseFloat(progressPercent) + randomIncrement);
      progressPercent = new Decimal(newProgress).toFixed(2);
    } else {
      // 达到99%时清除定时器
      clearInterval(uploadTimer);
      uploadTimer = null;
    }
  }, 220);
};

const changeProgress = (event) => {
  progressPercent = 1;
  startUploadTimer();
}

// handleProgress 上传进度
const handleProgress = (event, file, fileList) => {
  upLoading = true;
  // 计算进度百分比（保留2位小数）
  changeProgress(event)
};

/** 文件上传失败 */

const handleFileError = (error, file, fileList) => {
  // 清除定时器
  if (uploadTimer) {
    clearInterval(uploadTimer);
    uploadTimer = null;
  }
  
  upLoading = false;
  uploadRef.value.clearFiles();
  fileList = [];
  Modal.msgError("上传失败");
  // 重置进度
  progressPercent = 0;
};
const handleExceed = (files, fileList) => {
  fileList = [];
  upLoading = false;
  Modal.msgError(`每次只能上传一个文件!`);
};
// ====== 点击文件列表移除文件 ======
const beforeUpload = (file) => {
  // 清除可能存在的旧定时器
  if (uploadTimer) {
    clearInterval(uploadTimer);
    uploadTimer = null;
  }
  
  // 重置进度
  progressPercent = 0;
  
  console.log(file, "file");
  // 检查文件类型
  const isXLSX =
    file.type ===
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  const isXLS = file.type === "application/vnd.ms-excel";
  const isExcel = isXLSX || isXLS;
  // 检查文件大小（50MB限制）
  const isLt50M = file.size / 1024 / 1024 < 50;
  if (!isExcel) {
    Modal.msgError("只能上传Excel文件(.xlsx/.xls)！");
    return false;
  }
  if (!isLt50M) {
    Modal.msgError("上传文件大小不能超过50MB!");
    return false;
  }
  return true;
}

const routerorder=()=>{
  router.push({
    path: "/personal/paymentrecords",
  });
}
let pageDetail = $ref({});
const getDetail = () => {
  let query = {
    competitionSeriesId: route.query.competitionSeriesId,
  };

  getUserCompetitionDetailInfoById(query)
    .then(async (res) => {
      if (res.code == 200) {
        pageDetail = res.data || {};
      }
    })
    .catch(() => {});
};
getDetail();

// 页面卸载时清除定时器
onUnmounted(() => {
  if (uploadTimer) {
    clearInterval(uploadTimer);
    uploadTimer = null;
  }
});
</script>

<style scoped lang="scss">
.action-up {
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  .action-xlsx-btn {
    padding: 13px 27px;
    background: #3169f8;
    border-radius: 4px;
    font-size: 16px;
    color: #ffffff;
    cursor: pointer;
    flex-shrink: 0;
  }
  .action-btn {
    margin-left: 30px;
  }
  .action-xlsx-tips {
    font-size: 14px;
    color: #ff8800;
    margin: 0 15px;
  }
  .action-apply-container {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    .action-apply {
      padding: 13px 36px;
      font-size: 16px;
      color: #ffffff;
      background: #ff8800;
      border-radius: 4px;
      cursor: pointer;
      flex-shrink: 0;
      transition: all 0.3s ease;
    }
    .action-apply.disabled-btn {
      cursor: not-allowed;
      opacity: 0.5;
    }
    .action-apply.disabled-btn.hvr-grow:hover {
      transform: none;
      box-shadow: none;
    }
    .order-tip {
      font-size: 16px;
      color: #999999;
      margin-top: 10px;
      text-align: right;
    }
    .order-link {
      color: #3169f8;
      cursor: pointer;
    }
  }
}
.from-box {
  position: relative;
  .progress-box {
    position: absolute;
    top: 0px;
    left: 0;
    bottom: 0;
    right: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(255, 255, 255, 0.8);
    border-radius: 5px;
    z-index: 11;
    display: flex;
    justify-content: flex-start;
    align-items: center;
    flex-direction: column;
    padding-top: 50px;
    .progress-text {
      margin-top: 10px;
      font-size: 14px;
      color: #333333;
    }
  }
  :deep(.el-alert){
    align-items: flex-start;
  }
}
.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
:deep(.vxe-table) {
  .vxe-table--header-wrapper {
    font-size: 16px;
  }
  .vxe-table--body-wrapper {
    font-size: 14px;
  }
  .vxe-cell {
    padding-left: 4px !important;
    padding-right: 4px !important;
    .vxe-cell--wrapper {
      text-align: center;
    }
  }
}
.success-tips{
  color: #ff8800;
}
</style>