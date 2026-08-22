<template>
  <div class="base-page">
    <div class="container-custom self-custom">
      <Breadcrumbar />
      <div class="card-block">
        <div class="top-info">
          <div class="top-info-title mr-[10px]">确认订单</div>
          <div class="top-info-num">请仔细核对订单信息，确认无误后提交订单</div>
        </div>
        <div class="table-wrap">
          <div class="left-card">
            <div class="card-title">报名信息确认</div>
            <div class="card-teacher">
              <div class="info-card">
                <!-- 首行缩进 -->
                <div class="flex align-items flex-between w-full">
                  <div class="font-[600] text-[#333333] mb-[6px] w-[130px]">带队老师信息</div>
                  <div>姓名：{{ userInfo?.authInfo?.realName || '-' }}</div>
                  <div class="ml-[2em]">身份证号：{{ decrypt(userInfo?.authInfo?.idCard) || '-' }}</div>
                  <div class="ml-[2em]">所属学校：{{ userInfo?.schoolName || '-' }}</div>
                </div>
                <div class="flex align-items flex-between w-full mt-[10px]">
                  <div class="font-[600] text-[#333333] mb-[6px] w-[130px]">注意事项</div>
                  <div>
                    <div class="list-item" v-for="prs in precautions">{{ prs.label }}</div>
                  </div>
                </div>
                </div>
            </div>
            <div class="card-title !mb-[0]">订单信息</div>
            <div class="left-card-content-item flex justify-between items-center"
                 v-if="pageDetail && pageDetail.list && pageDetail.list.length > 0"
                 v-for="item in pageDetail.list">
              <div class="left-c-item" >
                <div class="text-[#333333] text-[18px] font-[600]"><span>团队名称：</span>{{ item.teamName }}</div>
                <div class="mt-[6px] font-[600] text-[16px]">
                    <span class="text-[#3169f8]">{{ item.competitionName }}</span>
                    <span>-</span>
                    <span class="text-[#FF8800]">{{ item.competitionTrackName }}</span>
                    <span>-</span>
                    <span class="text-[#51C512]">{{ item.secondLevelName }}</span>
                  </div>
              </div>
              <div class="left-c-num flex justify-end items-center">
                <div class="mr-[10px] text-[14px] text-[#666666] font-[400]">({{item.teamSize}}名队员 × ¥{{item.fee}}/人)</div>
                <p class="red-color">￥{{ item.subtotal || 0 }}</p>
              </div>
            </div>
          </div>
          <el-affix :offset="80" position="top" >
            <div class="right-card" v-if="pageDetail">
              <div class="right-card-btm">
                 <!-- !important怎么写 -->
                 <div class="card-title !mb-[0px]">结算信息</div>
                 <div class="pay-line">
                   <span>商品总额：</span>
                   <span>¥{{ pageDetail?.totalFee || 0 }}</span>
                 </div>
                 <div class="pay-line !items-start" v-if="pageDetail.detail && pageDetail.detail.length > 0">
                   <span>明细：</span>
                   <p>
                    <p v-for="e in pageDetail.detail">{{ e }}</p>
                   </p>
                 </div>

                 <div class="pay-line">
                   <span class="pay-line-txt">实付金额：</span>
                   <span class="pay-line-total">¥{{ pageDetail?.totalFee || 0 }}</span>
                 </div>
                 <el-button class="nav-buttons-item hvr-grow" @click="toTeamApply" v-loading="orderLoading">提交订单</el-button>
              </div>
            </div>
          </el-affix>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import Breadcrumbar from '@/components/breadcrumbar.vue'
import { debounce } from 'lodash'
import { useRoute,useRouter } from "vue-router";
import { getConfirmOrder } from '@/api/teacher'
import Modal from '@/plugins/modal.js'
import { getinfo } from "@/utils/auth";
import { decrypt } from "@/utils/jsencrypt.js";
import { getPaymentUrl} from '@/api/pay.js'
import { useDict } from "@/utils/dict";
const { precautions } = useDict("precautions");
const route = useRoute();
const router = useRouter()


let pageDetail = $ref({})
const getDetail = (key) => {
  getConfirmOrder({
    token: route?.query?.msgCode || '',
    competitionSeriesId: route?.query?.competitionSeriesId || ''
  }).then((res) => {
    if (res.code === 200) {
      pageDetail = res.data || {}
    }
  }).catch((err) => {
    if(err.code === 5008){
      Modal.confirm(err.msg || '结算信息异常，请重新选择','').then(() => {
        goShopping()
      })
    }
  })
}
const goShopping = () => {
  router.replace({
    path: "/event/detail/teacherApply/shopping",
    query: {
      competitionSeriesId: route?.query?.competitionSeriesId || "",
    },
  });
}

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

let orderLoading = $ref(false)
// 提交订单
const toTeamApply = () => {
  orderLoading = true
  getPaymentUrl({
    commodityName: pageDetail.commodityName,
    commodityType: "competition",
    payMethod:"online",
    token: route?.query?.msgCode || '',
    competitionSeriesId: route?.query?.competitionSeriesId || ''
  }).then(res=>{
    orderLoading = false
    if(res.code == 200){
      let data = res.data
      goOrderPay(data?.id)
    }else{
      Modal.alertError(res.msg || '获取支付信息失败')
    }
  }).catch(()=>{
    orderLoading = false
  })
}

 // 支付页面
const goOrderPay = (id) => {
  router.push({
    path: "/personal/paymentrecords/payment",
    query: {
      id
    },
  });
}


getDetail()
</script>

<style scoped lang="scss">
.base-page{
  font-size: 16px;
  color: #666666;
}
.top-info{
  background: #FFFFFF;
  padding: 10px 20px;
  border-radius: 5px;
  margin-bottom: 20px;
  .top-info-title{
    font-size: 26px;
    color: #333333;
    font-weight: bold;
    margin-bottom: 6px;
  }
  .top-info-num{
    font-size: 14px;
  }
  .ipt{
    margin-left: 50px;
    width: 300px !important;
  }
}

.del-btn{
  color: #999999;
  border:none !important;
  background: none !important;
  font-size: 16px;
  &:hover{
    color: $main-red-color;
  }
}
.card-title{
  font-size: 20px;
  color: #333333;
  font-weight: bold;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #E5E5E5;
}
/* 为选择列表头添加"全选"文字 */
:deep(.table-wrap){
  margin-top: 5px;
  margin-bottom: 20px;
  position: relative;
  display: flex;
  justify-content: space-between;
  align-self: flex-start;
  .left-card{
    background: #FFFFFF;
    padding: 20px;
    border-radius: 5px;
    flex: 1;
    margin-right: 20px;
  }
  .card-teacher{
    color: #666666;
    font-weight: 400;
    margin-bottom: 20px;
    .info-card{
      background: #F5F5F5;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 20px;
    }
    .notice-wrap{
      background: #fff3cd;
      padding: 20px;
      border-radius: 8px;
      border-left: 6px solid #ffc107;
      .phone{
        color: $main-color;
      }
    }
  }
}
.right-card{
  width: 500px;
  flex-shrink: 0;
  .right-card-top{
    background: #FFFFFF;
    padding: 20px;
    border-radius: 5px;
    margin-bottom: 20px;
  }
  .right-card-btm{
    background: #FFFFFF;
    padding: 20px;
    border-radius: 5px;
    color: #666666;
    font-size: 16px;
    font-weight: 400;
    .pay-line{
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 0;
      +.pay-line{
        border-top: 1px solid #E5E5E5;
      }
      .pay-line-txt{
        font-size: 20px;
        color: #333333;
        font-weight: bold;
      }
      .pay-line-total{
        font-size: 32px;
        color: $main-red-color;
        font-weight: bold;
      }
    }
    .nav-buttons-item{
      width: 100%;  
      height: 60px;
      letter-spacing: 1px;
      padding: 18px 40px;
      background: $main-red-color;
      border-radius: 80px;
      font-size: 16px;
      color: #FFFFFF;
      cursor: pointer;
      flex-shrink: 0;
      line-height: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      &:hover{
        transform: scale(1.05) !important; 
      }
    }
  }
}
.red-color{
  color: $main-red-color;
  font-size: 20px;
}
.left-card-content-item{
  padding: 10px 0px;
  border-bottom: 1px solid #E5E5E5;
  &:last-child{
    border-bottom: none;
  }
  .left-c-num{
    flex-shrink: 0;
    margin-left: 50px;
  }
}
</style>