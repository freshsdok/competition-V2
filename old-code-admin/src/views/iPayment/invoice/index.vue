<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
      label-width="100px"
    >
      <el-form-item label="发票号码" prop="invoiceNum">
        <el-input
          v-model.trim="queryParams.invoiceNum"
          placeholder="请输入发票号码"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="开票抬头" prop="buyerName">
        <el-input
          v-model.trim="queryParams.buyerName"
          placeholder="请输入开票抬头"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户姓名" prop="userName">
        <el-input
          v-model.trim="queryParams.userName"
          placeholder="请输入用户姓名"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="税号" prop="buyerTaxNum">
        <el-input
          v-model.trim="queryParams.buyerTaxNum"
          placeholder="请输入税号"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item>
   
      <!-- <el-form-item label="买方手机" prop="buyerPhone">
        <el-input
          v-model="queryParams.buyerPhone"
          placeholder="请输入买方手机"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item> -->
      <el-form-item label="邮箱" prop="buyerEmail">
        <el-input
          v-model.trim="queryParams.buyerEmail"
          placeholder="请输入邮箱"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item>
     
      <el-form-item label="开票种类" prop="invoiceClass">
        <el-select
          v-model="queryParams.invoiceClass"
          placeholder="请选择开票种类"
          clearable
          style="width: 160px"
          @change="handleQuery"
        >
          <el-option
            v-for="dict in invoice_class"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
         <el-form-item label="开具状态" prop="issuedStatus">
        <el-select
          v-model="queryParams.issuedStatus"
          placeholder="请选择开具状态"
          clearable
          style="width: 160px"
          @change="handleQuery"
        >
          <el-option
            v-for="dict in issued_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
       <el-form-item label="开票备注" prop="remark">
        <el-input
          v-model.trim="queryParams.remark"
          placeholder="请输入开票备注"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所在学校" prop="schoolName">
        <el-input
          v-model.trim="queryParams.schoolName"
          placeholder="请输入所在学校"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="开票内容" prop="invoiceGoodsCode" style="margin-right: 30px;">
          <el-select
            v-model="queryParams.invoiceGoodsCode"
            placeholder="请选择内容类型"
            clearable
            style="width: 160px;">
              <el-option
              v-for="dict in invoice_goods_code"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
              />
          </el-select>
      </el-form-item>
      <el-form-item label="" label-width="0" prop="feeType">
        <el-select
            v-model="queryParams.feeType"
            placeholder="请选择收费类型"
            clearable
            style="width: 160px;">
              <el-option
              v-for="dict in fee_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
              />
        </el-select>
      </el-form-item>
      <el-form-item label="开票金额" prop="invoiceFee">
        <el-input-number
          v-model.trim="queryParams.invoiceFee[0]"
          :min="0"
          :max="queryParams.invoiceFee[1] || undefined"
          placeholder="最小金额"
          clearable
          controls-position="right"
          style="width: 120px"
        />
        <span style="margin: 0 8px; display: inline-block">~</span>
        <el-input-number
          v-model.trim="queryParams.invoiceFee[1]"
          :min="queryParams.invoiceFee[0] || 0"
          placeholder="最大金额"
          clearable
          controls-position="right"
          style="width: 120px"
        />
      </el-form-item>
      <el-form-item label="开票时间" prop="invoiceApplyTime">
        <el-date-picker
          v-model="queryParams.invoiceApplyTime"
          style="width: 260px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleQuery"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="postList">
      <el-table-column label="序号" align="center" type="index" width="40" />
      <el-table-column
        label="发票号码"
        align="left"
        prop="invoiceNum"
        min-width="165"
      />
      <el-table-column
        label="开票抬头"
        align="left"
        prop="buyerName"
        min-width="100"
        show-overflow-tooltip
      />
      <el-table-column
        label="用户姓名"
        align="left"
        prop="userName"
        width="65"/>
        <el-table-column
        label="所在学校"
        align="left"
        prop="schoolName"
        min-width="100"
        show-overflow-tooltip
      />
      <el-table-column label="税号" align="left" prop="buyerTaxNum" min-width="160">  
        <template #default="scope">
          {{ scope.row.buyerTaxNum || "-" }}
        </template>
      </el-table-column>
      <el-table-column
        label="开票金额"
        align="center"
        prop="amount"
        width="65"
      />
      <el-table-column
        label="开票种类"
        align="center"
        prop="invoiceClass"
        width="65"
      >
        <template #default="scope">
          <dict-tag :options="invoice_class" :value="scope.row.invoiceClass" />
        </template>
      </el-table-column>
      <el-table-column
        label="开票内容"
        align="center"
        prop="invoiceContentName"
        width="70"
      ></el-table-column>
      <el-table-column
        label="开票备注"
        align="center"
        prop="remark"
        show-overflow-tooltip
        width="100">
      </el-table-column>
      <el-table-column
        label="开具状态"
        align="center"
        prop="issuedStatus"
        width="65"
      >
        <template #default="scope">
          <dict-tag :options="issued_status" :value="scope.row.issuedStatus" />
        </template>
      </el-table-column>
      <el-table-column
        label="开具时间"
        align="left"
        prop="issuedTime"
        width="80"
      />
      <!-- <el-table-column
        label="失败原因"
        align="center"
        prop="failReason"
        min-width="180"
        show-overflow-tooltip
      >
        <template #default="scope">
          {{ scope.row.failReason || "-" }}
        </template>
      </el-table-column>
      <el-table-column
        label="开票备注"
        align="center"
        prop="remark"
        min-width="180"
        show-overflow-tooltip
      /> -->
      <el-table-column
        label="操作"
        width="118"
        align="center"
        fixed="right"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <div>
            <el-button link type="primary" @click="chakan(scope.row)"
              >查看详情</el-button
            >
            <el-button
              link
              type="primary"
              @click="handleExport(scope.row)"
              v-if="scope.row.issuedStatus == 1"
              >下载</el-button
            >
            <!-- <el-button
              link
              type="primary"
              @click="yulan(scope.row)"
              v-if="scope.row.issuedStatus == 1"
              >预览</el-button
            > -->
            <el-button
              link
              type="primary"
              v-if="scope.row.issuedStatus != 1"
              @click="fapiaogengxin(scope.row)"
              >更新发票状态</el-button
            >
            <el-button
              link
              type="primary"
              @click="congshi(scope.row)"
              v-if="scope.row.issuedStatus == 2 || scope.row.issuedStatus == 3"
              >重开</el-button
            >
          </div>
        </template>
      </el-table-column>
    </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />

    <!-- 预览弹窗 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <iframe :src="fapurl" width="100%" height="600px" frameborder="0">
      </iframe>
    </el-dialog>
    <!-- 详情 -->
    <el-dialog
      :title="xiangqingtitle"
      v-model="xiangqingopen"
      width="800px"
      append-to-body
    >
      <el-form
        ref="fppostRef"
        :model="fpform"
        :rules="fprules"
        disabled
        label-width="120px"
      >
        <el-form-item label="发票号码" prop="invoiceNum">
          {{ fpform.invoiceNum || "-" }}
        </el-form-item>
        <el-form-item label="开票抬头" prop="buyerName">
          {{ fpform.buyerName || "-" }}
        </el-form-item>
        <el-form-item label="税号" prop="buyerTaxNum">
          {{ fpform.buyerTaxNum || "-" }}
        </el-form-item>
        <el-form-item label="邮箱" prop="buyerEmail">
          {{ fpform.buyerEmail || "-" }}
        </el-form-item>

        <el-form-item label="开票种类" prop="invoiceLine">
          <el-radio-group v-model="fpform.invoiceClass">
            <el-radio
              v-for="(item, index) in invoice_class"
              :key="index"
              :value="item.value"
              >{{ item.label || "-" }}</el-radio
            >
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开具状态" prop="issuedStatus">
          <el-radio-group v-model="fpform.issuedStatus">
            <el-radio
              v-for="(item, index) in issued_status"
              :key="index"
              :value="item.value"
              >{{ item.label || "-" }}</el-radio
            >
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开具时间" prop="issuedTime">
          {{ fpform.issuedTime || "-" }}
        </el-form-item>
        <el-form-item label="开票内容" prop="invoiceContentName">
          {{ fpform.invoiceContentName || "-" }}
        </el-form-item>
        <el-form-item label="开票备注" prop="remark">
          {{ fpform.remark || "-" }}
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup name="Post">
import {
  invoicelist,
  invoicereInvoice,
  queryInvoiceResult,
} from "@/api/iPayment/index";
import { ref } from "vue";

const { proxy } = getCurrentInstance();
const { issued_status, invoice_class,invoice_goods_code,fee_type } = proxy.useDict(
  "issued_status",
  "invoice_class",
  "invoice_goods_code",
  "fee_type"
);

const postList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);

const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    invoiceFee: [],
  },
});

const { queryParams, form } = toRefs(data);
const fapiaogengxin = (item) => {
  const params = {
    serialNos: [item.invoiceSerialNum],
    orderNos: [item.orderId],
    isOfferInvoiceDetail: 0,
  };
  queryInvoiceResult(params).then((res) => {
    if (res.code == 200) {
      getList();
    }
  });
};
/** 查询订单列表 */
function getList() {
  loading.value = true;
  if (queryParams.value.invoiceFee.length == 0) {
    queryParams.value.amountStart = null;
    queryParams.value.amountEnd = null;
  } else {
    queryParams.value.amountStart = queryParams.value.invoiceFee[0];
    queryParams.value.amountEnd = queryParams.value.invoiceFee[1];
  }
  if (
    !queryParams.value.invoiceApplyTime ||
    queryParams.value.invoiceApplyTime.length == 0
  ) {
    queryParams.value.applyStartTime = null;
    queryParams.value.applyEndTime = null;
  } else {
    queryParams.value.applyStartTime = queryParams.value.invoiceApplyTime[0];
    queryParams.value.applyEndTime = queryParams.value.invoiceApplyTime[1];
  }
  invoicelist(queryParams.value,queryParams.value.pageNum,queryParams.value.pageSize).then((response) => {
    postList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 搜索按钮操作 */
function handleQuery() {
  console.log(queryParams.value);
  queryParams.value.pageNum = 1;
  getList();
}
const xiangqingtitle = ref("");
const xiangqingopen = ref(false);
const fpform = ref({});
const chakan = (item) => {
  xiangqingtitle.value = "查看发票详情";
  xiangqingopen.value = true;
  fpform.value = item;
};

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.invoiceFee = [];
  handleQuery();
}
const congshi = (row) => {
  const params = {
    id: row.id,
  };
  invoicereInvoice(params).then((res) => {
    setTimeout(() => {
      getList();
    }, 1000);
  });
};
/** 导出按钮操作 */
function handleExport(row) {
  proxy.downloadJS(
    import.meta.env.VITE_APP_BASE_API +
      `/system/invoice/pdf/download?pdfUrl=${row.cUrl}`,
    `${row.invoiceNum}.pdf`
  );
}
const fapurl = ref("");
function yulan(row) {
  open.value = true;
  fapurl.value = row.cUrl;
}
getList();
</script>
<style scoped lang="scss">
:deep(.el-table) {
  .cell{
    padding: 0 4px !important;
  }
}
</style>
