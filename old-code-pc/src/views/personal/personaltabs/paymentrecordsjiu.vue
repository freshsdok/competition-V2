<template>
  <div style="background-color: #f5f5f5">
    <div class="container-custom">
      <Breadcrumbar />
      <el-card style="background-color: #fff">
        <el-tabs
          v-model="activeName"
          class="demo-tabs"
          @tab-change="handleClick"
        >
          <el-tab-pane
            :label="'全部订单(' + (perStatusCountshuliang.total || 0) + ')'"
            name=""
          ></el-tab-pane>
            <template v-for="(item, index) in pay_status" :key="index">
            <div>
              <el-tab-pane
                v-if="item.value != 'paying' && item.value != 'failed'"
                :name="item.value"
              >
                <template #label>
                  <div
                    v-if="
                      item.value == 'pending' &&
                      perStatusCountshuliang[item.value] > 0
                    "
                    class="daizhifu"
                  >
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
                </template></el-tab-pane
              >
            </div>
          </template>
        </el-tabs>
        <el-form :model="queryParams" ref="queryRef" :inline="true">
          <el-form-item label="" prop="id">
            <el-input
              v-model="queryParams.id"
              placeholder="搜索订单号"
              clearable
              class="wid300"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="hqlist">搜索</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <div style="margin: 30px 0" v-if="orderlists.length > 0">
        <el-card
          style="background-color: #fff; padding: 0; margin-top: 20px"
          v-for="(item, index) in orderlists"
          :key="index"
        >
          <template #header>
            <div style="display: flex; justify-content: space-between">
              <div>
                <span style="font-weight: 500; font-size: 14px; color: #333">
                  订单号：{{ item.id }}
                </span>
                <span
                  style="
                    font-size: 14px;
                    color: #666;
                    display: inline-block;
                    margin-left: 40px;
                  "
                  >创建时间：{{ item.createTime }}</span
                >
              </div>

              <div>
                状态：
                <el-tag
                  :type="
                    pay_status.find((xxx) => {
                      return xxx.value == item.payStatus;
                    })?.elTagType
                  "
                  size="large"
                >
                  {{
                    pay_status.find((xxx) => {
                      return xxx.value == item.payStatus;
                    })?.label
                  }}
                </el-tag>
              </div>
            </div>
          </template>
          <div v-for="(x, i) in item.competitionList" :key="i">
            <div
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
              "
            >
              <div>
                <div style="font-weight: 500; margin-bottom: 5px">
                  团队编号：{{ x.teamCode }}
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
                  {{ x.playersList.length }} 名队员* ￥ {{ x.fee }}/人
                </div>
              </div>
              <div style="font-size: 16px; color: #e53935; font-weight: 500">
                ￥
                <span>{{ x.subtotal ? x.subtotal : 0 }}</span>
              </div>
            </div>
            <el-divider border-style="dashed" />
          </div>
          <div
            style="
              display: flex;
              justify-content: flex-end;
              align-items: center;
              color: #666;
            "
          >
            <!-- 共 {{ item.tuandui.length }} 个团队，{{
              item.tuandui.length * 2
            }}
            名队员 合计：￥<span
              style="font-size: 20px; color: #e53935; font-weight: bold"
              >{{ item.tuandui.length * 2 * 120 }}</span
            > -->
            <el-button
              @click="quxiaodingdan(item)"
              v-if="item.payStatus == 'pending'"
              >取消订单</el-button
            >
            <el-button type="primary" @click="zhanghaoshezhi(item)"
              >查看详情</el-button
            >

            <el-button
              type="primary"
              v-if="item.payStatus == 'paid' && item.invoiceStatus == 3"
              @click="shenqingkaipiao(item)"
              >申请开票</el-button
            >
            <el-button
              type="primary"
              v-if="item.payStatus == 'paid' && item.invoiceStatus == 0"
              @click="shuaxinfapiao(item)"
              >同步发票状态</el-button
            >
            <el-button
              type="primary"
              v-if="item.invoiceStatus == 1 && false"
              @click="handleExport(item)"
              >发票下载</el-button
            >
            <el-button
              type="primary"
              v-if="
                item.payMethod != 'offline' &&
                (item.payStatus == 'pending' || item.payStatus == 'paying')
              "
              @click="saomazhifu(item)"
              >去支付</el-button
            >
          </div>
        </el-card>

        <div
          style="
            display: flex;
            justify-content: center;
            margin-top: 20px;
            padding: 20px 0;
          "
        >
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            style="display: flex; justify-content: center"
          />
        </div>
      </div>
      <div
        v-else
        style="
          font-size: 20px;
          text-align: center;
          line-height: 30px;
          min-height: 400px;
        "
      >
        <el-empty description="暂无数据"></el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import Breadcrumbar from "@/components/breadcrumbar.vue";
import modal from "@/plugins/modal";
import {
  orderlist,
  cancelOrder,
  queryInvoiceResult,
  perStatusCount,
} from "@/api/personal/index";
import { onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRoute, useRouter } from "vue-router";
const router = useRouter();
const route = useRoute();
console.log(route.query.status);
const { proxy } = getCurrentInstance();
const { pay_status } = proxy.useDict(
  "pay_status"
);

import { getinfo } from "@/utils/auth";
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
const handleClick = () => {
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
const perStatusCountlist = () => {
  perStatusCount({
    userId: userinfo.value.userId,
  }).then((res) => {
    if (res.code == 200) {
      perStatusCountshuliang.value = res.data;
      // 计算所有项的总数
      const totalCount = Object.values(res.data).reduce(
        (sum, count) => sum + (count || 0),
        0
      );
      perStatusCountshuliang.value.total = totalCount;
    }
  });
};

const hqlist = () => {
  perStatusCountlist();
  orderlist({
    payStatus: activeName.value,
    userId: userinfo.value.userId,
    pageSize: queryParams.value.pageSize,
    pageNum: queryParams.value.pageNum,
    id: queryParams.value.id,
  }).then((res) => {
    orderlists.value = res.rows;
    total.value = res.total;
  });
};
const zhanghaoshezhi = (item) => {
  router.push({
    path: "/personal/paymentrecords/OrderDetails",
    query: {
      id: item.id,
    },
  });
};
const shenqingkaipiao = (item) => {
  // ElMessageBox.alert("发票开具功能将于近期开放，给您造成的不便请谅解", "提示", {
  //   type: "warning", // ← 这会自动显示黄色感叹号图标
  //   confirmButtonText: "取消",
  //   center: false,
  // });

  router.push({
    path: "/personal/paymentrecords/invoiceIssuance",
    query: {
      id: item.id,
    },
  });
};
const saomazhifu = (item) => {
  router.push({
    path: "/personal/paymentrecords/payment",
    query: {
      id: item.id,
    },
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
    .catch(() => {});
};

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