<template>
  <div style="background-color: #f5f5f5">
    <div class="container-custom">
      <Breadcrumbar />
      <!-- 顶部操作栏 -->
      <div style="
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
        ">
        <div style="font-size: 20px; font-weight: bold">我的订单</div>
      </div>

      <el-card style="background-color: #fff">
        <el-tabs v-model="activeName" class="demo-tabs" @tab-change="handleClick">
          <el-tab-pane :label="'全部订单(' + (perStatusCountshuliang.total || 0) + ')'" name=""></el-tab-pane>
          <template v-for="(item, index) in pay_status" :key="index">
            <div>
              <el-tab-pane v-if="
                item.value != 'paying' &&
                item.value != 'failed' &&
                item.value != 'shopping' &&
                item.value != '0'
              " :name="item.value">
                <template #label>
                  <div v-if="
                    item.value == 'pending' &&
                    perStatusCountshuliang[item.value] > 0
                  " class="daizhifu">
                    {{
                      perStatusCountshuliang[item.value]
                        ? perStatusCountshuliang[item.value]
                        : 0
                    }}
                  </div>
                  {{
                    item.value == "pending"
                      ? item.label
                      : item.label +
                      "(" +
                      (perStatusCountshuliang[item.value] || 0) +
                      ")"
                  }}
                </template></el-tab-pane>
            </div>
          </template>
        </el-tabs>
        <el-form :model="queryParams" ref="queryRef" :inline="true">
          <el-form-item label="" prop="idStr">
            <el-input v-model="queryParams.idStr" placeholder="搜索订单号" clearable class="wid300" />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="listLoading"
              :disabled="listLoading"
              @click="hqlist"
            >
              搜索
            </el-button>
            <el-button type="success" @click="handleApplyInvoice" style="margin-left: 10px" :loading="loading">
              申请开票
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <div
        v-loading="listLoading"
        element-loading-text="订单加载中..."
        class="order-list-wrapper"
      >
        <div style="margin: 10px 0" v-if="orderlists.length > 0">
        <!-- 全选按钮 -->
        <!-- <el-card style="background-color: #fff; padding: 0; margin-top: 20px">

          <div style="display: flex; align-items: center; margin-bottom: 10px">
            <el-checkbox v-model="selectAll" @change="handleSelectAll"
              >全选</el-checkbox
            >
            <span style="margin-left: 10px; color: #666">
              已选择 {{ selectedOrderIds.length }} 个订单
            </span>
               <el-button
              type="success"
              @click="handleApplyInvoice"
              :disabled="!selectedOrderIds.length"
              style="margin-left: 10px"
            > </el-button>
          </div></el-card
        > -->
        <el-card style="background-color: #fff; padding: 0; margin-top: 20px" v-for="(item, index) in orderlists"
          :key="index">
          <template #header>
            <div style="
                display: flex;
                justify-content: space-between;
                align-items: center;
              ">
              <div style="display: flex; align-items: center">
                <!-- <el-checkbox
                  v-model="item.checked"
                  @change="handleOrderCheck(item)"
                
                  :disabled="
                    item.payStatus !== 'paid' || item.invoiceStatus == 1
                  "
                ></el-checkbox> -->
                <span style="
                    font-weight: 500;
                    font-size: 14px;
                    color: #333;
                    margin-left: 10px;
                  ">
                  订单号：{{ item.id }}
                </span>
                <span style="
                    font-size: 14px;
                    color: #666;
                    display: inline-block;
                    margin-left: 40px;
                  ">创建时间：{{ item.createTime }}</span>
              </div>

              <div>
                <!-- 开票状态：orderType退费订单不可开票-->
                <el-tag v-if="item.orderType != 'refund'" :type="order_invoice_status.find((xxx) => {
                  return xxx.value == item.invoiceStatus;
                })?.elTagType
                  " size="large">
                  {{ item.invoiceStatus == 1 ? "已开票" : "未开票" }}
                </el-tag>
                <!-- 状态： -->
                <el-tag :type="pay_status.find((xxx) => {
                  return xxx.value == item.payStatus;
                })?.elTagType
                  " style="margin-left: 20px" size="large">
                  {{
                    pay_status.find((xxx) => {
                      return xxx.value == item.payStatus;
                    })?.label
                  }}
                </el-tag>
                <el-tag :type="order_type.find((xxx) => {
                  return xxx.value == item.orderType;
                })?.elTagType
                  " style="margin-left: 20px" size="large">
                  {{
                    order_type.find((xxx) => {
                      return xxx.value == item.orderType;
                    })?.label
                  }}
                </el-tag>
              </div>
            </div>
          </template>

          <div v-for="(x, i) in item.teamInfoLists" :key="i" v-if="item.commodityType === 'competition'">
            <div style="
                display: flex;
                justify-content: space-between;
                align-items: center;
              ">
              <!-- 赛事报名 -->
              <div>
                <div style="font-weight: 500; margin-bottom: 5px">
                  团队编号：{{ x.teamCode }}
                </div>
                <div style="font-weight: 500; margin-bottom: 5px">
                  团队名称：{{ x.teamName }}
                </div>
                <div class="mt-[6px] font-[600] text-[16px]">
                  <span class="text-[#3169f8]">{{ x.competitionName }}</span>
                  <span>-</span>
                  <span class="text-[#FF8800]">{{
                    x.competitionTrackName
                  }}</span>
                  <span>-</span>
                  <span class="text-[#51C512]">{{ x.secondLevelName }}</span>
                </div>
                <div style="font-size: 12px; color: #666; margin-top: 5px">
                  {{ x.teamSize }} 名队员* ￥ {{ x.fee }}/人
                </div>
              </div>
              <div style="font-size: 16px; color: #e53935; font-weight: 500">
                ￥
                <span>{{ x.subtotal || 0 }}</span>
                <!-- payOrderId存在是补费订单 -->
                <!-- <span>{{item.payOrderId? item.relAmount: x.subtotal }}</span> -->
              </div>
            </div>
            <el-divider border-style="dashed" />
          </div>
          <!-- 赛证互通 -->
          <div v-if="item.commodityType === 'cert'">
            <div class="flex items-center justify-between">
              <div class="flex-1 text-[#444]">
                <div class="mb-[5px]">赛证互通：{{ item?.commodityName }}</div>
                <div class="mb-[5px]">
                  <span>源证书：</span> 
                  <span class="text-[#000]" v-for="cert in item.teamInfoLists?.originCertList"> 【{{ cert.certConfigName }}】</span> 
                </div>
                <div class="mb-[5px]">
                  <span>目标证书：</span> 
                  <span class="text-[#3169f8]" v-for="cert in item.teamInfoLists?.targetCertList">【{{ cert.certConfigName }}】</span>
                </div>
              </div>
              <div class="text-[16px] text-[#e53935]">￥ {{ item.amount }}</div>
            </div>
          </div>
          <div style="
              display: flex;
              justify-content: flex-end;
              align-items: center;
              color: #666;
            ">
            <span v-if="item.amount">
              结算金额：<span style="font-size: 14px; color: #e53935; font-weight: 500">￥{{ item.amount }}</span>
              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            </span>
            <el-button @click="quxiaodingdan(item)"
              v-if="item.payStatus == 'pending' && item.changeType == null">取消订单</el-button>
            <el-button @click="quxiaodingdantuifei(item)"
              v-if="item.payStatus == 'pending' && item.changeType == 'repayment'">取消订单</el-button>
            <el-button type="primary" @click="zhanghaoshezhi(item)" v-if="item.orderType == 'pay'">查看详情</el-button>

            <el-button type="primary" v-if="item.payStatus == 'pending'" @click="saomazhifu(item)">去支付</el-button>
          </div>
        </el-card>

        <div style="
            display: flex;
            justify-content: center;
            margin-top: 20px;
            padding: 20px 0;
          ">
          <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="total"
            @size-change="handleSizeChange" @current-change="handleCurrentChange"
            :disabled="listLoading"
            style="display: flex; justify-content: center" />
        </div>
        </div>
        <div v-else style="
            font-size: 20px;
            text-align: center;
            line-height: 30px;
            min-height: 400px;
          ">
          <el-empty description="暂无数据"></el-empty>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import Breadcrumbar from "@/components/breadcrumbar.vue";
  import modal from "@/plugins/modal";
  import { updatePayMethod } from "@/api/pay.js";
  import {
    orderlist,
    cancelOrder,
    queryInvoiceResult,
    perStatusCount,
    queryTeamAndUserByOrderId,
    cancelRepaymentOrder
  } from "@/api/personal/index";
  import { onBeforeUnmount, onMounted, ref, computed } from "vue";
  import { ElMessage, ElMessageBox } from "element-plus";
  import { useRoute, useRouter } from "vue-router";
  const router = useRouter();
  const route = useRoute();
  console.log(route.query.status);
  const { proxy } = getCurrentInstance();
  const { order_invoice_status, pay_status, order_type } = proxy.useDict(
    "order_invoice_status",
    "pay_status",
    "order_type"
  );

  // 选中的订单ID列表
  const selectedOrderIds = ref([]);
  // 全选状态
  const selectAll = ref(false);

  // 更新发票状态
  const shuaxinfapiao = (item) => {
    const params = {
      serialNos: [],
      orderNos: [item.orderId],
      isOfferInvoiceDetail: 0,
    };
    queryInvoiceResult(params).then((res) => {
      if (res.code == 200) {
        setTimeout(() => {
          location.reload();
        }, 200);
      }
    });
  };

  const activeName = ref("");
  if (route.query.status) {
    activeName.value = route.query.status;
  }
  const userinfo = ref(null);
  const orderlists = ref([]);

  // 处理全选
  const handleSelectAll = () => {
    orderlists.value.forEach((item) => {
      if (item.payStatus === "paid" && item.invoiceStatus != 1) {
        item.checked = selectAll.value;
      }
    });
    updateSelectedOrderIds();
  };

  // 处理单个订单勾选
  const handleOrderCheck = (item) => {
    updateSelectedOrderIds();
    // 更新全选状态
    const paidOrders = orderlists.value.filter(
      (item) => item.payStatus === "paid"
    );
    if (paidOrders.length === 0) {
      selectAll.value = false;
    } else {
      selectAll.value = paidOrders.every((item) => item.checked);
    }
  };

  // 更新选中的订单ID列表
  const updateSelectedOrderIds = () => {
    selectedOrderIds.value = orderlists.value
      .filter((item) => item.checked)
      .map((item) => item.id);
  };
  // 处理申请开票
  const loading = ref(false);
  const handleApplyInvoice = () => {
    router.push({
      path: "/personal/paymentrecords/invoice-preparation",
      query: {
        orderIds: selectedOrderIds.value.join(',')
      }
    });
    // loading.value = true;
    // // if (selectedOrderIds.value.length === 0) return;
    // // 跳转到开票准备页，传递选中的订单ID
    // queryTeamAndUserByOrderId().then((res) => {
    //   if (res.code == 200) {
    //     if (res.data.length >0) {
    //       loading.value = false;
    //       router.push({
    //         path: "/personal/invoice-preparation",
    //         query: {
    //           orderIds: selectedOrderIds.value.join(',')
    //         }
    //       });
    //     }
    //   }
    // });
  };

  const handleClick = () => {
    // 清空全选状态
    selectAll.value = false;
    // 清空选中的订单ID列表
    selectedOrderIds.value = [];
    // 清空所有订单的选中状态
    orderlists.value.forEach((item) => {
      item.checked = false;
    });

    router.push({
      path: "/personal/paymentrecords",
      query: {
        status: activeName.value,
      },
    });
    hqlist();
  };
  const total = ref(10);
  const queryParams = ref({
    pageSize: 10,
    pageNum: 1,
  });
  const handleSizeChange = (val) => {
    queryParams.value.pageSize = val;
    hqlist();
  };
  const handleCurrentChange = (val) => {
    queryParams.value.pageNum = val;
    hqlist();
  };
  const perStatusCountshuliang = ref({});
  const perStatusCountlist = async () => {
    const res = await perStatusCount({
      userId: userinfo.value.userId,
    });
    if (res.code == 200) {
      perStatusCountshuliang.value = res.data;
      // 计算所有项的总数
      const totalCount = Object.values(res.data).reduce(
        (sum, count) => sum + (count || 0),
        0
      );
      perStatusCountshuliang.value.total = totalCount;
    }
  };

  const listLoading = ref(false);
  const LIST_REQUEST_DEBOUNCE_DELAY = 300;
  let listRequestTimer = null;
  let refreshAfterCurrentRequest = false;
  let componentUnmounted = false;

  const loadOrderList = async () => {
    if (!userinfo.value || componentUnmounted) return;

    // 慢请求执行期间只记录一次刷新意图，避免产生并发请求。
    if (listLoading.value) {
      refreshAfterCurrentRequest = true;
      return;
    }

    listLoading.value = true;
    try {
      const params = {
        payStatus: activeName.value,
        userId: userinfo.value.userId,
        pageSize: queryParams.value.pageSize,
        pageNum: queryParams.value.pageNum,
        idStr: queryParams.value.idStr,
      };
      const [countResult, orderResult] = await Promise.allSettled([
        perStatusCountlist(),
        orderlist(params),
      ]);
      if (countResult.status === "rejected") {
        console.error("加载订单状态统计失败", countResult.reason);
      }
      if (orderResult.status === "rejected") {
        throw orderResult.reason;
      }
      const res = orderResult.value;
      if (res.code === 200) {
        // 为每个订单添加checked属性，并补充Mock队员信息
        orderlists.value = res.rows?.map((order) => ({
          ...order,
          checked: false,
          competitionList: order.competitionList?.map((competition) => ({
            ...competition,
          })),
          teamInfoLists: JSON.parse(order.teamInfoList || '[]')
        }));
        total.value = res.total;
      }
    } catch (error) {
      console.error("加载订单列表失败", error);
    } finally {
      listLoading.value = false;

      // 请求期间发生多次筛选、分页操作时，仅使用最新条件再刷新一次。
      if (refreshAfterCurrentRequest && !componentUnmounted) {
        refreshAfterCurrentRequest = false;
        hqlist();
      }
    }
  };

  const hqlist = () => {
    if (listRequestTimer) {
      clearTimeout(listRequestTimer);
    }

    listRequestTimer = setTimeout(() => {
      listRequestTimer = null;
      loadOrderList();
    }, LIST_REQUEST_DEBOUNCE_DELAY);
  };

  onBeforeUnmount(() => {
    componentUnmounted = true;
    refreshAfterCurrentRequest = false;
    if (listRequestTimer) {
      clearTimeout(listRequestTimer);
      listRequestTimer = null;
    }
  });
  const zhanghaoshezhi = (item) => {
    router.push({
      path: "/personal/paymentrecords/OrderDetails",
      query: {
        id: item.id,
      },
    });
  };

  const saomazhifu = (item) => {
    const params = {
      id: item.id,
      payMethod: "online", //online-线上转账，offline-线下转账
    };
    updatePayMethod(params).then((res) => {
      router.push({
        path: "/personal/paymentrecords/payment",
        query: {
          id: item.id,
        },
      });
    });

  };
  const quxiaodingdan = (item) => {
    modal
      .confirm("取消后订单中报名信息需要重新导入，确认取消？")
      .then(function () {
        cancelOrder(item.id).then((res) => {
          if (res.code == 200) {
            location.reload();
          }
        });
      })
      .catch(() => { });
  };
  const quxiaodingdantuifei = (item) => {
    modal
      .confirm("是否确认取消退费重缴申请？")
      .then(function () {
        cancelRepaymentOrder(item.id).then((res) => {
          if (res.code == 200) {
            location.reload();
          }
        });
      })
      .catch(() => { });
  };

  import { getinfo } from "@/utils/auth";
  onMounted(() => {
    if (getinfo()) {
      userinfo.value = JSON.parse(getinfo());
      hqlist();
    } else {
      router.push({
        path: "/",
      });
    }
  });
  /** 导出按钮操作 */
  function handleExport(row) {
    proxy.downloadJS(
      import.meta.env.VITE_APP_BASE_API +
      `/system/invoice/pdf/download?pdfUrl=${row.cUrl}`,
      `${row.invoiceNum}.pdf`
    );
  }
</script>



<style scoped lang="scss">
  :deep(.el-card__header) {
    background: #fafafa;
  }

  .wid300 {
    width: 300px;
  }

  :deep(.el-message-box__btns) {
    justify-content: flex-end !important;
  }

  .daizhifu {
    position: absolute;
    width: 20px;
    height: 20px;
    background: red;
    border-radius: 50%;
    top: 0px;
    right: 0px;
    text-align: center;
    line-height: 20px;
    color: #fff;
    cursor: pointer;
    font-size: 14px;
  }
</style>
