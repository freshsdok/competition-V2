<template>
  <div style="background-color: #f5f5f5">
    <div class="container-custom">
      <Breadcrumbar />

      <div class="title">
        <div>订单提交成功，请尽快付款！订单号：{{ qrcodeDetail.id }}</div>
        <div>
          应付金额<span style="color: red; font-size: 20px">
            {{ qrcodeDetail.amount }}</span
          >元
        </div>
      </div>
      <!-- <div style="display: flex;justify-content: flex-end;">
          <el-button type="primary" @click="tiaozhuanleibiao"
                      >返回缴费列表</el-button
                    >
      </div> -->
      <el-card style="background-color: #fff; margin-bottom: 30px">
        <el-tabs
          v-model="activeName"
          class="demo-tabs"
          @tab-change="handleClick"
        >
          <el-tab-pane label="扫码支付" name="">
            <div class="flex flex-col justify-center items-center mb-[50px]">
              <div v-if="orderInfo?.payStatus == 'cancelled'" class="text-[26px]">
                <span>订单已被取消！去</span>
                <span @click="routerorder" class="text-[#409eff] cursor-pointer">我的订单</span>
                <span>查看</span>
              </div>
              <template v-else>
                <div
                  class="qrcode-img flex justify-center items-center"
                  v-loading="payLoading"
                >
                  <div
                    class="relative"
                    v-if="qrcodeDetail && qrcodeDetail.qrCode"
                  >
                    <QrcodeVue
                      :value="qrcodeDetail.qrCode"
                      :size="260"
                      :margin="3"
                      level="H"
                      v-if="orderInfo.payStatus != 'paid'"
                    />

                    <div
                      class="qrcode-overlay"
                      v-if="
                        orderInfo.payStatus != 'paid' &&
                        qrcodeDetail.qrCodeExpireTime &&
                        isTimeExpired(qrcodeDetail.qrCodeExpireTime)
                      "
                    >
                      <div class="overlay-content">
                        <div class="overlay-text">二维码已过期</div>
                        <div class="overlay-subtext">请点击下方按钮刷新</div>
                      </div>
                    </div>

                    <!-- 支付方式提示 -->
                    <div
                      class="w-full text-center text-sm text-[#999999]"
                      v-if="orderInfo.payStatus != 'paid'"
                    >
                      支持支付宝/微信
                    </div>
                    <!-- 二维码有效期提示 -->
                    <div
                      class="w-full text-center text-sm text-[#999999]"
                      v-if="orderInfo.payStatus != 'paid'"
                    >
                      支付二维码有效期为10分钟
                    </div>
                    <!-- 二维码倒计时 -->
                    <div
                      class="w-full text-center text-sm text-[#FF6B6B] font-bold"
                      v-if="orderInfo.payStatus != 'paid'"
                    >
                      未支付成功时不要刷新页面
                    </div>
                    <div
                      class="w-full mt-4 text-center text-[26px] text-[#FFDC2D]"
                      v-if="orderInfo.payStatus === 'paying'"
                    >
                      正在支付中...
                    </div>
                    <div
                      class="w-full mt-4 text-center text-[26px] text-[#409eff]"
                      v-if="orderInfo.payStatus == 'paid'"
                    >
                      <img
                        src="@/assets/images/baomingchenggong.png"
                        alt="成功图标"
                        class="success-icon"
                      />
                      <div class="success-title">支付成功</div>
                      <div class="order-tip">
                        <span class="chenggong">{{ qrcodeDetail.commodityType == 'cert' ? '赛证互通申请成功!':'您已报名成功!' }}</span>您可以在
                        <span   class="countdown-number" @click="routerorder" style="cursor: pointer"
                          >我的订单</span
                        >中查看缴费成功的订单。
                      </div>
                      <div class="countdown-text">
                        <span class="countdown-number">{{ daojishi }}</span
                        >秒后将为您自动跳转至该订单 |<span
                          class="countdown-number"
                          style="cursor: pointer"
                          @click="tiaozhuan"
                        >
                          查看订单
                        </span>
                      </div>
                    </div>
                    <div
                      class="w-full mt-4 text-center text-[26px] text-[#FF0000]"
                      v-if="orderInfo.payStatus === 'failed'"
                    >
                      支付失败
                    </div>
                  </div>
                </div>
                <div class="flex justify-center items-center mt-8">
                  <el-button
                    type="primary"
                    @click="handleQrcodeReset"
                    v-if="!['paid'].includes(orderInfo.payStatus)"
                    >刷新支付二维码</el-button
                  >
                </div>
              </template>
            </div>
          </el-tab-pane>
          <el-tab-pane label="对公转账" name="yinlian" v-if="qrcodeDetail.commodityType !== 'cert'">
            <div class="zzxx">转账信息</div>
            <div class="lankuang">
              <el-row>
                <el-col :span="12">
                  <div class="zhuanzhangtitle">收款单位</div>
                  <div class="zhuanzhangvalue">
                    {{ offline.merName }}
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="zhuanzhangtitle">银行账号</div>
                  <div class="zhuanzhangvalue">
                    {{ offline.account }}
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="zhuanzhangtitle">开户行</div>
                  <div class="zhuanzhangvalue">
                    {{ offline.bank }}
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="zhuanzhangtitle">转账金额</div>
                  <div
                    class="zhuanzhangvalue"
                    style="color: red; font-weight: bold"
                  >
                    ¥{{ qrcodeDetail.amount }}
                  </div>
                </el-col>
              </el-row>
              <div class="beizhu">
                <div>
                  <span style="font-weight: bold">必填备注：</span
                  >订单号+学校名称
                </div>
                <div>
                  （该备注将用于系统自动精准匹配订单，请务必准确无误。）
                </div>
              </div>
            </div>
            <div style="display: flex; justify-content: flex-end">
              <el-button class="sctx" type="primary" @click="tijiao"
                >去支付</el-button
              >
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import Breadcrumbar from "@/components/breadcrumbar.vue";
import { useRoute, useRouter } from "vue-router";
import { onMounted, ref } from "vue";
import {
  regeneratePaymentUrl,
  getOrderDetail,
  updatePayMethod,
  getOfflineBankInfo,
} from "@/api/pay.js";
import QrcodeVue from "qrcode.vue";
import { cloneDeep, set } from "lodash";
import Modal from "@/plugins/modal.js";
const userInfo = ref({});
const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
import { ElMessage } from "element-plus";
// 记录tab页
const activeName = ref("");
const handleClick = () => {};
onMounted(() => {
  orderchaklist();
});

// 重新获取支付二维码
let qrcodeDetail = $ref({}); //订单信息
let timer = null; // 计时器
let startTime = 0; // 记录开始时间
const MAX_POLLING_TIME = 11 * 60 * 1000; // 10分钟最大轮询时长
let ss = null;
const orderchaklist = () => {
  payLoading = true;
  regeneratePaymentUrl(route.query.id)
    .then((res) => {
      if (res.code == 200) {
        qrcodeDetail = res.data;
        orderInfo = res.data;
        payLoading = false;
        if (orderInfo.payStatus === "paid") {
          ss = setInterval(() => {
            daojishi.value--;
            if (daojishi.value <= 0) {
              tiaozhuan();
            }
          }, 1000);
        } else {
          if(['cancelled'].includes(orderInfo.payStatus)){
            return
          }
          // 启动二维码倒计时
          // startQrCodeCountdown();
          // 记录开始时间
          startTime = Date.now();
          timer = setInterval(() => {
            // 检查是否超过最大轮询时间
            if (Date.now() - startTime > MAX_POLLING_TIME) {
              clearInterval(timer);
              timer = null;
              return;
            }
            getOrderInfo();
          }, 5000);
        }
      } else if (res.code == 300) {
        ElMessage.error(res.msg);
        tiaozhuan();
      }
    })
    .catch((reg) => {
      router.push({
        path: "/personal/paymentrecords",
      });
    });
};
const routerorder=()=>{
    clearInterval(ss);
  router.push({
    path: "/personal/paymentrecords",
  });
}
// 清理定时器
onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
  if (qrCodeTimer) {
    clearInterval(qrCodeTimer);
    qrCodeTimer = null;
  }
  if (ss) {
    clearInterval(ss);
    ss = null;
  }
});

// 启动二维码倒计时
const startQrCodeCountdown = () => {
  // 重置倒计时
  qrCodeCountdown.value = 600; // 10分钟 = 600秒
  
  // 清除之前的计时器（如果存在）
  if (qrCodeTimer) {
    clearInterval(qrCodeTimer);
    qrCodeTimer = null;
  }
  
  qrCodeTimer = setInterval(() => {
    if (qrCodeCountdown.value > 0) {
      qrCodeCountdown.value--;
    } else {
      clearInterval(qrCodeTimer);
      qrCodeTimer = null;
      // 二维码过期提示
      ElMessage.warning('二维码已过期，请点击刷新按钮重新获取');
    }
  }, 1000);
};

// 格式化倒计时显示
const formatCountdown = (seconds) => {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
};
// 过渡动画
let payLoading = $ref(false);

// 处理二维码重置
const handleQrcodeReset = () => {
  let sendData = cloneDeep(qrcodeDetail);
  payLoading = true;
  qrcodeDetail = {};
  regeneratePaymentUrl(sendData.id).then((res) => {
    payLoading = false;
    if (res.code == 200) {
      qrcodeDetail = res.data;
      orderInfo = res.data;
      // 重新启动二维码倒计时
      // if (orderInfo.payStatus != 'paid') {
      //   startQrCodeCountdown();
      // }
    }
  });
};

// 获取订单信息

let orderInfo = $ref({});
// 跳转倒计时
let daojishi = ref(10);
// 二维码有效期倒计时（10分钟）
let qrCodeCountdown = ref(600); // 10分钟 = 600秒
let qrCodeTimer = null;
const getOrderInfo = () => {
  // 确保有订单ID
  if (!qrcodeDetail.userId && !qrcodeDetail.commodityId) return;
  let sendData = cloneDeep(qrcodeDetail);

  getOrderDetail(sendData.id).then((res) => {
    if (res.code == 200) {
      orderInfo = res.data;
      // 如果订单已支付成功，可以提前清除轮询
      if (['paid','failed','cancelled'].includes(orderInfo.payStatus)) {
        if (timer) {
          clearInterval(timer);
          timer = null;
        }
        if (orderInfo.payStatus === "paid") {
          ss = setInterval(() => {
            daojishi.value--;
            if (daojishi.value <= 0) {
              tiaozhuan();
            }
          }, 1000);
          // Modal.notifySuccess({
          //   message: "支付成功",
          //   type: "success",
          //   title: "支付成功",
          // });
        }
        if (orderInfo.payStatus === "failed") {
          Modal.notifyError({
            message: "支付失败",
            type: "error",
            title: "支付失败",
          });
        }
      }
    }
  });
};
getOrderInfo();
const tiaozhuan = () => {
  clearInterval(ss);
  router.push({
    path: "/personal/paymentrecords/OrderDetails",
    query: {
      id: route.query.id,
    },
  });
};

const tijiao = () => {
  const params = {
    id: route.query.id,
    payMethod: "offline", //online-线上转账，offline-线下转账
  };
  updatePayMethod(params).then((res) => {
    if (res.code == 200) {
      clearInterval(timer);
      timer = null;
      router.push({
        path: "/personal/paymentrecords/OrderDetails",
        query: {
          id: route.query.id,
        },
      });
    } else if (res.code == 300) {
      ElMessage.error(res.msg);
      tiaozhuan();
    }
  });
};
const { pay_status, pay_method } = proxy.useDict("pay_status", "pay_method");
const offline = ref({});
const getOfflineBankInfolist = () => {
  getOfflineBankInfo(route.query.id).then((res) => {
    if (res.code == 200) {
      console.log(res, 123456);
      offline.value = res.data;
    }
  });
};
getOfflineBankInfolist();
// 将YYYYMMDDHHmmss格式的时间字符串转换为Date对象
const convertTimeString = (timeString) => {
  if (!timeString || timeString.length !== 14) return null;

  const year = parseInt(timeString.substring(0, 4));
  const month = parseInt(timeString.substring(4, 6)) - 1; // 月份从0开始
  const day = parseInt(timeString.substring(6, 8));
  const hour = parseInt(timeString.substring(8, 10));
  const minute = parseInt(timeString.substring(10, 12));
  const second = parseInt(timeString.substring(12, 14));

  return new Date(year, month, day, hour, minute, second);
};

// 判断当前时间是否已超过目标时间
const isTimeExpired = (timeString) => {
  const targetTime = convertTimeString(timeString);
  if (!targetTime) return false;

  const currentTime = new Date();
  return currentTime > targetTime;
};
</script>


<style scoped lang="scss">
.title {
  display: flex;
  justify-content: space-between;
  color: #000;
  font-size: 18px;
  height: 40px;
  margin-bottom: 30px;
}
.qrcode-img {
  min-height: 260px;
}

.title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  .titleleft {
    font-size: 24px;
    font-weight: bold;
  }
}

/* 二维码容器样式 */
.qrcode-container {
  position: relative;
  width: 260px;
  height: 260px;
  margin: 0 auto;
}

/* 二维码过期蒙层样式 */
.qrcode-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.9);
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.overlay-content {
  text-align: center;
  color: #ffffff;
}

.overlay-text {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.overlay-subtext {
  font-size: 16px;
  opacity: 0.9;
}
.jbxx {
  margin-top: 20px;
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 15px;
  border-bottom: 1px solid #e0e0e0;
}
.zhxx {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  margin: 10px;
  font-size: 14px;
}
.zzxx {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 15px;
}
.lankuang {
  background-color: #e3f2fd;
  padding: 20px;
  border-radius: 4px;
  border-left: 4px solid #2196f3;
  margin: 20px 0;
  padding: 30px;
  .zhuanzhangtitle {
    font-size: 14px;
    color: #666;
  }
  .zhuanzhangvalue {
    font-size: 20px;
    font-weight: 500;
    color: #333;
    margin-top: 10px;
  }
  .beizhu {
    background-color: #fff;
    padding: 15px;
    border-radius: 4px;
    border: 1px solid #b3e5fc;
    margin: 15px 0;
    font-size: 14px;
  }
}
.sc {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 4px;
  margin: 20px 0;
  border: 2px dashed #dee2e6;
  text-align: center;
  .shangchuanbt {
    font-weight: bold;
  }
}
.notes {
  background-color: #fff3cd;
  padding: 15px;
  border-radius: 4px;
  margin: 20px 0;
  border-left: 4px solid #ffc107;
}
.mingxi {
  text-align: right;
  margin: 20px 0;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
  .xiang {
    display: flex;
    justify-content: space-between;
    align-items: center;
    line-height: 40px;
  }
}
.success-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto;
}
.success-title {
  font-size: 26px;
  font-weight: 700;
  color: #2c3e50;
  margin: 12px 0;
  text-align: center;
}
.countdown-text {
  font-size: 16px;
  color: #5c6c7c;
  margin-bottom: 32px;
}
.countdown-number {
  font-weight: 700;
  color: #3498db;
  margin: 0 4px;
  animation: pulse 1s infinite;
}
.order-tip {
  font-size: 16px;
  color: #999999;
}
.order-link {
  color: #3169f8;
  cursor: pointer;
}
.chenggong{
color: red;font-size: 18px;font-weight: 600;
}
</style>