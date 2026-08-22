<template>
  <div>
    <el-dialog
      title="赛事缴费"
      v-model="dialogVisible"
      width="400px"
      :before-close="handleClose"
    >
      <div class="flex flex-col justify-center items-center mb-[50px]">
        <div
          class="qrcode-img flex justify-center items-center"
          v-loading="payLoading"
        >
          <div class="relative" v-if="qrcodeDetail && qrcodeDetail.qrCode">
            <QrcodeVue
              :value="qrcodeDetail.qrCode"
              :size="260"
              :margin="3"
              level="H"
            />
            <!-- 二维码有效期提示 -->
            <div class="w-full text-center text-sm text-[#999999]">
              支付二维码有效期为10分钟
            </div>
            <div
              class="w-full mt-4 text-center text-[26px] text-[#FFDC2D]"
              v-if="orderInfo.payStatus === 'paying'"
            >
              正在支付中...
            </div>
            <div
              class="w-full mt-4 text-center text-[26px] text-[#409eff]"
              v-if="orderInfo.payStatus === 'paid'"
            >
              支付成功
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
      </div>
    </el-dialog>
  </div>
</template>
<script setup>
import {
  getPaymentUrl,
  regeneratePaymentUrl,
  getOrderDetail,
} from "@/api/pay.js";
import QrcodeVue from "qrcode.vue";
import { cloneDeep } from "lodash";
import Modal from "@/plugins/modal.js";
// 支付弹框
let dialogVisible = $ref(false);
let timer = null;
let startTime = 0;
const MAX_POLLING_TIME = 10 * 60 * 1000; // 10分钟最大轮询时长
// 轮询订单状态
// 关闭弹窗
const handleClose = () => {
  dialogVisible = false;
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
};
// 重新获取支付二维码
let qrcodeDetail = $ref({});
let payDeatil = $ref({});
let payLoading = $ref(false);
// 打开弹框，显示支付
const resetPayment = (result) => {
  payLoading = true;
  payDeatil = result;
  dialogVisible = true;
  console.log(result, "result");
  getPaymentUrl({
    userId: payDeatil.userId,
    userName: payDeatil.userName,
    commodityName: payDeatil.commodityName,
    commodityType: payDeatil.commodityType,
    commodityId: payDeatil.commodityId,
    amount: payDeatil.amount,
    orderId: payDeatil.orderId,
    payMethod: "online", //online-线上转账，offline-线下转账
  })
    .then((res) => {
      if (res.code == 200) {
        qrcodeDetail = res.data;
      }
      payLoading = false;
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
    })
    .catch(() => {
      payLoading = false;
    });
};

// 处理二维码重置
const handleQrcodeReset = () => {
  let sendData = cloneDeep(qrcodeDetail);
  payLoading = true;
  qrcodeDetail = {};
  regeneratePaymentUrl(sendData.id).then((res) => {
    payLoading = false;
    if (res.code == 200) {
      qrcodeDetail = res.data;
    }
  });
};

// 获取订单信息
const emits = defineEmits(["reLoad"]);
let orderInfo = $ref({});
const getOrderInfo = () => {
  // 确保有订单ID
  if (!qrcodeDetail.userId && !qrcodeDetail.commodityId) return;
  let sendData = cloneDeep(qrcodeDetail);
  // let params = { userId: sendData.userId, commodityId: sendData.commodityId };
  getOrderDetail(sendData.id).then((res) => {
    if (res.code == 200) {
      orderInfo = res.data;
      // 如果订单已支付成功，可以提前清除轮询
      if (orderInfo.payStatus === "paid" || orderInfo.payStatus === "failed") {
        if (timer) {
          clearInterval(timer);
          timer = null;
        }
        if (orderInfo.payStatus === "paid") {
          Modal.notifySuccess({
            message: "支付成功",
            type: "success",
            title: "支付成功",
          });
        }
        if (orderInfo.payStatus === "failed") {
          Modal.notifyError({
            message: "支付失败",
            type: "error",
            title: "支付失败",
          });
        }
        dialogVisible = false;
        // 触发重新加载事件
        emits("reLoad");
      }
    }
  });
};

// 暴露出方法
defineExpose({
  resetPayment,
});

watch(
  () => dialogVisible,
  (newValue, oldValue) => {
    if (!newValue) {
      if (timer) {
        clearInterval(timer);
        timer = null;
      }
    }
  }
);
</script>
<style scoped lang="scss">
.qrcode-img {
  min-height: 260px;
}
</style>