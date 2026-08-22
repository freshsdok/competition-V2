<template>
  <div class="">
    <div>
      <div class="bt">
        <el-row>
          <el-col :span="2" class="th"> 序号 </el-col>
          <el-col :span="4" class="th"> 发票抬头 </el-col>
          <el-col :span="4" class="th"> 开票金额 </el-col>
          <el-col :span="4" class="th"> 开具时间 </el-col>
          <el-col :span="3" class="th"> 开票种类 </el-col>
          <el-col :span="3" class="th"> 开具状态 </el-col>
          <el-col :span="4" class="th"> 操作 </el-col>
        </el-row>
      </div>

      <div class="centen" v-if="orderlists.length > 0" v-loading="loading">
        <el-row v-for="(item, index) in orderlists" :key="index">
          <el-col :span="2" class="tr"> {{ index + 1 }} </el-col>
          <el-col :span="4" class="tr"> {{ item.buyerName }} </el-col>
          <el-col :span="4" class="tr"> {{ item.amount }} </el-col>
          <el-col :span="4" class="tr"> {{ item.issuedTime }} </el-col>
          <el-col :span="3" class="tr">
            <dict-tag :options="invoice_class" :value="item.invoiceClass" />
          </el-col>
          <el-col :span="3" class="tr">
            <dict-tag
              :options="issued_status"
              :value="item.issuedStatus"
              v-if="item.issuedStatus != 2 && item.issuedStatus != 3"
            />
            <span v-else>开票中</span>
          </el-col>
          <el-col :span="4" class="tr" style="border-right: 1px solid #e4e4e4">
            <el-button
              link
              type="primary"
              @click="handleExport(item)"
              v-if="item.issuedStatus == 1"
              >下载</el-button
            >
            <el-button
              link
              type="primary"
              v-if="item.issuedStatus != 1"
              @click="gengxin(item)"
              >更新开票状态</el-button
            >
          </el-col>
        </el-row>
        <pagination
          v-show="total > 0"
          :total="total"
          style="margin: 30px 0"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="hqlist"
        />
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
    <!-- <div style="font-size: 20px;text-align: center;line-height: 30px;margin-top: 50px;">
        <el-empty description="发票开具功能将于近期开放，给您带来的不变请谅解"></el-empty>
      
    </div> -->
  </div>
</template>

<script setup>
import { invoicelist, queryInvoiceResult } from "@/api/personal/index";
import { onMounted } from "vue";
const { proxy } = getCurrentInstance();
const { invoice_class, issued_status } = proxy.useDict(
  "invoice_class",
  "issued_status"
);
const loading = ref(false);
// 分页组件
import Pagination from "@/components/Pagination";
import { set } from "lodash";
const props = defineProps({
  userinfo: {
    type: Object,
    required: true,
  },
});
const { userinfo } = toRefs(props);
const orderlists = ref([]);
const total = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
});
const hqlist = () => {
  loading.value = true;
  invoicelist(
    {
      userId: userinfo.value?.userId,
    },
    queryParams
  ).then((res) => {
    if (res.code == 200) {
      orderlists.value = res.rows;
      total.value = res.total;
      loading.value = false;
    }
  });
};
/** 导出按钮操作 */
function handleExport(row) {
  proxy.downloadJS(
    import.meta.env.VITE_APP_BASE_API +
      `/system/invoice/personal/pdf/${row.id}`,
    `${row.invoiceNum}.pdf`
  );
}
const gengxin = (item) => {
  loading.value = true;

  const params = {
    serialNos: [item.invoiceSerialNum],
    orderNos: [item.orderId],
    isOfferInvoiceDetail: 0,
  };
  queryInvoiceResult(params)
    .then((res) => {
      if (res.code == 200) {
        hqlist();
        setTimeout(() => {
          loading.value = false;
        }, 300);
      }
    })
    .catch(() => {
      setTimeout(() => {
        loading.value = false;
      }, 300);
    });
};
onMounted(() => {
  hqlist();
});
</script>



<style scoped lang="scss">
.bt {
  padding: 10px;
  width: 1000px;
  height: 70px;
  background: #4b7eff;
  border-radius: 10px 10px 0px 0px;

  .th {
    font-family: Source Han Sans CN, Source Han Sans CN;
    font-weight: 400;
    font-size: 18px;
    color: #ffffff;
    line-height: 70px;
    text-align: center;
    font-style: normal;
    text-transform: none;
  }
}
.centen {
  width: 1000px;
  min-height: 400px;
  .tr {
    font-family: Source Han Sans CN, Source Han Sans CN;
    font-weight: 400;
    font-size: 16px;
    color: #333333;
    line-height: 60px;
    text-align: center;
    font-style: normal;
    text-transform: none;
    border-left: 1px solid #e4e4e4;
    border-bottom: 1px solid #e4e4e4;
  }
}
</style>
