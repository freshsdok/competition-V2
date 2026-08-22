<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      label-width="100px"
      v-show="showSearch"
    >
      <!-- <el-form-item label="订单号" prop="orderId">
        <el-input
          v-model="queryParams.orderId"
          placeholder="订单号"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item> -->
      <el-form-item label="商品名称" prop="commodityName">
        <el-input
          v-model.trim="queryParams.commodityName"
          placeholder="请输入商品名称"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品类型" prop="commodityType">
        <el-select
          @change="handleQuery"
          v-model="queryParams.commodityType"
          placeholder="请选择商品类型"
          clearable
          style="width: 160px"
        >
          <el-option
            v-for="dict in goodsType"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="购买人" prop="userName">
        <el-input
          v-model.trim="queryParams.userName"
          placeholder="请输入购买人"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系方式" prop="phoneNumber">
        <el-input
          v-model.trim="queryParams.phoneNumber"
          placeholder="请输入联系方式"
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
      <el-form-item label="交易流水号" prop="cmbOrderId">
        <el-input
          v-model.trim="queryParams.cmbOrderId"
          placeholder="请输入交易流水号"
          clearable
          style="width: 160px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单号" prop="idStr">
        <el-input
          v-model.trim="queryParams.idStr"
          placeholder="请输入订单号"
          clearable
          style="width: 160px"
          @change="handleQuery"
          @input="handleOrderIdInput"
        />
      </el-form-item>

      <el-form-item label="收款状态" prop="payStatus">
        <el-select
          @change="handleQuery"
          v-model="queryParams.payStatus"
          placeholder="请选择状态"
          clearable
          style="width: 160px"
        >
          <el-option
            v-for="dict in pay_status_list"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="支付方式" prop="payMethod">
        <el-select
          v-model="queryParams.payMethod"
          placeholder="请选择支付方式"
          clearable
          style="width: 160px"
          @change="handleQuery"
        >
          <el-option
            v-for="dict in pay_method"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="在线支付方式" prop="payMode">
        <el-select
          v-model="queryParams.payMode"
          placeholder="请选择在线支付方式"
          clearable
          style="width: 160px"
          @change="handleQuery"
        >
          <el-option
            v-for="dict in pay_mode"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="支付时间" prop="invoiceTime">
        <el-date-picker
          v-model="queryParams.invoiceTime"
          style="width: 260px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleQueryTime"
          :default-time="[
            new Date('1970-01-01 00:00:00'),
            new Date('1970-01-01 23:59:59'),
          ]"
        ></el-date-picker>
      </el-form-item>

      <el-form-item label="支付金额" prop="invoiceFee">
        <el-input-number
          v-model.trim="queryParams.amountStart"
          :min="0"
          :max="queryParams.amountEnd || undefined"
          placeholder="最小金额"
          clearable
          controls-position="right"
          style="width: 120px"
        />
        <span style="margin: 0 8px; display: inline-block">~</span>
        <el-input-number
          v-model.trim="queryParams.amountEnd"
          :min="queryParams.amountStart || 0"
          placeholder="最大金额"
          clearable
          controls-position="right"
          style="width: 120px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain @click="handleExport">导出</el-button>
      </el-col>
    </el-row> -->

    <el-form ref="queryParamsform" :model="queryParams" label-width="100px">
      <el-form-item label="大赛名称">
        <el-tag
          v-for="(dict, index) in commodityNameList"
          :key="index"
          style="margin-right: 6px; cursor: pointer"
          :effect="queryParams.commodityName == dict ? 'primary' : 'plain'"
          :type="queryParams.commodityName == dict ? ' ' : 'info'"
          @click="handleTagClick('payContestName', dict)"
          >{{ dict }}</el-tag
        >
      </el-form-item>
      <el-form-item label="收款公司">
        <el-tag
          v-for="dict in ConfigmerSelect"
          :key="dict.mer_id"
          style="margin-right: 6px; cursor: pointer"
          :effect="queryParams.merId == dict.mer_id ? 'primary' : 'plain'"
          :type="queryParams.merId == dict.mer_id ? 'success' : 'info'"
          @click="handleTagClick('payCompany', dict.mer_id)"
          >{{ dict.mer_name }}</el-tag
        >
      </el-form-item>
      <el-form-item label="支付方式">
        <el-tag
          v-for="dict in pay_method"
          :key="dict.value"
          style="margin-right: 6px; cursor: pointer"
          :effect="queryParams.payMethod == dict.value ? 'primary' : 'plain'"
          :type="queryParams.payMethod == dict.value ? 'danger' : 'info'"
          @click="handleTagClick('payMethod', dict.value)"
          >{{ dict.label }}</el-tag
        >
      </el-form-item>
      <el-form-item label="收款状态">
        <el-tag
          v-for="dict in kuaijie"
          :key="dict.value"
          style="margin-right: 6px; cursor: pointer"
          :effect="queryParams.payStatus == dict.value ? 'primary' : 'plain'"
          :type="queryParams.payStatus == dict.value ? 'warning' : 'info'"
          @click="handleTagClick('payStatus', dict.value)"
          >{{ dict.name }}</el-tag
        >
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="postList"
      @selection-change="handleSelectionChange"
    >
      <!-- <el-table-column type="selection" width="55" align="center" /> -->

      <!-- <el-table-column label="序号" align="center" type="index" width="40" /> -->
      <el-table-column
        label="商品名称"
        align="left"
        prop="commodityName"
        min-width="140"
        show-overflow-tooltip
      />
      <el-table-column label="商品类型" 
                        align="center" 
                        prop="commodityType"
                        min-width="100">
        <template #default="scope">
         {{  getDictLabel(goodsType, scope.row.commodityType) }}
        </template>
      </el-table-column>
      <!-- <el-table-column label="订单号" align="left" prop="id" min-width="158" /> -->
      <el-table-column
        label="收款公司"
        align="left"
        prop="merName"
        min-width="160"
        show-overflow-tooltip
      />
      <el-table-column
        label="购买人"
        align="center"
        prop="userName"
        width="60"
        show-overflow-tooltip
      />

      <el-table-column
        label="联系方式"
        align="center"
        prop="phoneNumber"
        width="100"
      />
      <el-table-column
        label="所在学校"
        align="center"
        prop="schoolName"
        min-width="100"
        show-overflow-tooltip
      />

      <el-table-column
        label="交易流水号"
        align="center"
        prop="cmbOrderId"
        min-width="160"
        show-overflow-tooltip
      />
      <el-table-column
        label="订单号"
        align="center"
        prop="id"
        show-overflow-tooltip
      />
      <el-table-column
        label="金额（元）"
        align="center"
        prop="amount"
        width="80"
      >
        <template #default="scope">
          {{ scope.row.amount || "-" }}
        </template>
      </el-table-column>

      <el-table-column
        label="收款状态"
        align="center"
        prop="payStatus"
        width="65"
      >
        <template #default="scope">
          <dict-tag :options="pay_status_list" :value="scope.row.payStatus" />
        </template>
      </el-table-column>

      <el-table-column
        label="支付方式"
        align="center"
        prop="payMethod"
        width="65"
      >
        <template #default="scope">
          <!-- {{ scope.row.payStatus}} -->
          <dict-tag :options="pay_method" :value="scope.row.payMethod" />
        </template>
      </el-table-column>
      <el-table-column
        label="在线支付方式"
        align="center"
        prop="payMode"
        width="90"
      >
        <template #default="scope">
          <!-- {{ scope.row.payStatus}} -->
          <dict-tag :options="pay_mode" :value="scope.row.payMode" />
        </template>
      </el-table-column>

      <el-table-column
        label="支付时间"
        align="center"
        prop="payTime"
        width="120"
        show-overflow-tooltip
      >
        <template #default="scope">
          <span style="white-space: normal">
            <template v-if="scope.row.payMethod == 'offline'">
              {{ (scope.row.payStatus == "paid" && scope.row.payTime) || "-" }}
            </template>
            <template v-else>
              {{ scope.row.payTime || "-" }}
            </template>
          </span>
        </template>
      </el-table-column>

      <el-table-column
        label="开票状态"
        align="center"
        prop="invoiceStatus"
        width="60"
      >
        <template #default="scope">
          <span v-if="['refunded','refunding','repay_refunding'].includes(scope.row.payStatus)">-</span>
          <span v-else>{{ scope.row.invoiceStatus == 1 ? "已开票" : "未开票" }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="120"
        show-overflow-tooltip
      >
        <template #default="scope">
          <span style="white-space: normal"> {{ scope.row.createTime }}</span>
        </template>
      </el-table-column>

      <el-table-column
        label="操作"
        min-width="100"
        align="center"
        fixed="right"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-button link type="primary" @click="handlechak(scope.row)"
            >查看</el-button
          >
          <el-button
            link
            type="primary"
            style="margin-left: 0px"
            v-if="scope.row.payStatus == 'approving'"
            @click="xiugaichak(scope.row)"
            >审核</el-button
          >
          <!-- <el-button
            link
            type="primary"
            v-if="scope.row.payStatus == 'paid' && scope.row.invoiceStatus == 3"
            @click="shenqinginvoice(scope.row)"
            >申请发票</el-button
          > -->
          <!-- <el-button
            link
            type="primary"
            @click="qvxiao(scope.row)"
            v-if="scope.row.payStatus == 'pending'"
            >取消订单</el-button> -->
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

    <!-- 添加或修改岗位对话框 -->
    <el-dialog :title="title" v-model="open" width="1200px" append-to-body>
      <el-form
        ref="postRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        :disabled="disp"
      >
        <el-row :gutter="24">
          <el-col :span="12">
              <el-form-item label="商品名称：">
                {{ form.commodityName || "-" }}
              </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品类型：">
              {{  getDictLabel(goodsType, form.commodityType) }}
            </el-form-item></el-col
          >
          <el-col :span="12">
            <el-form-item label="购买人：">
              {{ form.userName || "-" }}
            </el-form-item></el-col
          >
          <el-col :span="12">
            <el-form-item label="联系方式：">
              {{ form.phoneNumber || "-" }}
            </el-form-item></el-col
          >
          <el-col :span="12">
            <el-form-item label="交易流水号：">
              {{ form.cmbOrderId || "-" }}
            </el-form-item></el-col
          >
          <el-col :span="12">
            <el-form-item label="订单号：">
              {{ form.id || "-" }}
            </el-form-item></el-col
          >
          <el-col :span="12">
            <el-form-item label="第三方订单号：">
              {{ form.targetOrderId || "-" }}
            </el-form-item></el-col
          >
          <el-col :span="12">
            <el-form-item label="金额（元）：">
              {{ form.amount || "-" }}
            </el-form-item></el-col
          >
          <el-col :span="12">
            <el-form-item label="状态：">
              {{
                pay_status_list.find((item) => {
                  return item.value == form.payStatus;
                })?.label
              }}
            </el-form-item></el-col
          >
          <el-col :span="12"
            ><el-form-item label="支付方式：">
              {{
                pay_method.find((item) => {
                  return item.value == form.payMethod;
                })?.label
              }}
            </el-form-item></el-col
          >
          <el-col :span="12">
            <el-form-item label="在线支付方式：">
              {{
                pay_mode.find((item) => {
                  return item.value == form.payMode;
                })?.label || "-"
              }}
            </el-form-item></el-col
          >
          <el-col :span="12">
            <el-form-item label="创建时间：">
              {{ form.createTime || "-" }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付完成时间：">
              {{ form.payTime || "-" }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收款公司">
              {{ form.merName || "-" }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="凭证：" v-if="form.payMethod === 'offline'">
              <!-- <img src="form." alt=""> -->

              <el-image
                v-for="(item, index) in form.paymentProofFile"
                :key="index"
                style="width: 100px; height: 100px"
                :src="item"
                :zoom-rate="1.2"
                :max-scale="7"
                :min-scale="0.2"
                :preview-src-list="[item]"
                show-progress
                :initial-index="4"
                fit="cover"
              />
            </el-form-item>
          </el-col>

          <el-form-item
            label="审核意见："
            prop="remark"
            v-if="!disp"
            style="width: 50%"
          >
            <el-input
              v-model="form.auditOpinion"
              type="textarea"
              placeholder="请输入审核意见"
            ></el-input>
          </el-form-item>
        </el-row>
      </el-form>
      <div class="action-btns" v-if="!disp">
        <el-button type="danger" @click="shenhe(0)">驳回</el-button>
        <el-button type="primary" @click="shenhe(1)">通过</el-button>
      </div>
      <div v-if="form.commodityType == 'cert'" style="padding: 0px 0 20px;">
          <el-descriptions  direction="vertical" border
                            style="margin-top: 20px">
            <template v-for="(item, index) in form?.teamInfoLists?.originCertList || []" :key="index">
              <el-descriptions-item label="源证书名称">{{ item.certConfigName }}</el-descriptions-item>
            </template>
            <template v-for="(item, index) in form?.teamInfoLists?.targetCertList || []" :key="index">
              <el-descriptions-item  label="目标证书名称">{{ item.certConfigName }}</el-descriptions-item>
            </template>
          </el-descriptions>
      </div>
      <el-table :data="form.teamInfoLists" max-height="400px" border v-else>
        <el-table-column
          label="团队编号"
          align="left"
          prop="teamCode"
          min-width="100"
        >
        </el-table-column>
        <el-table-column
          label="团队名称"
          align="left"
          prop="teamName"
          min-width="100"
        ></el-table-column>
        <el-table-column
          label="赛道/组别"
          align="left"
          prop="commodityName"
          min-width="100"
        >
          <template #default="scope">
            <span style="color: #ff8800">{{
              scope.row.competitionTrackName
            }}</span>
            <span>-</span>
            <span style="color: #51c512">{{ scope.row.secondLevelName }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="队员"
          align="left"
          prop="commodityName"
          min-width="120"
        >
          <template #default="scope">
            <span
              class="text-[#666666] font-[400]"
              v-for="(item, index) in scope.row.playersList"
              :key="index"
            >
              {{ item.userName }}（{{ item.idCard }}）
                 <span v-if="item.delFlag!=0" class='status-deleted'>（已删除）</span>
              <br />
            </span>
            <!-- <span
              class="text-[#666666] font-[400]"
              v-for="(item, index) in 10"
              :key="index"
            >
              张三（410125122312341234）<br />
            </span> -->
          </template>
        </el-table-column>
        <el-table-column
          label="指导教师"
          align="left"
          prop="commodityName"
          min-width="100"
        >
          <template #default="scope">
            <span
              class="text-[#666666] font-[400]"
              v-for="(item, index) in scope.row.instructorList"
              :key="index"
            >
              {{ item.userName }}（{{ item.phone }}）<br />
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    <!-- 申请发票 -->
    <el-dialog :title="fptitle" v-model="fpopen" width="800px" append-to-body>
      <el-form
        ref="fppostRef"
        :model="fpform"
        :rules="fprules"
        label-width="120px"
      >
        <el-form-item label="个人/企业" prop="invoiceClass">
          <el-select
            v-model="fpform.invoiceClass"
            placeholder="个人/企业"
            clearable
          >
            <el-option label="企业" value="2" />
            <el-option label="个人" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="发票类型" prop="invoiceLine">
          <el-radio-group v-model="fpform.invoiceLine">
            <el-radio
              v-for="(item, index) in invoice_line"
              :key="index"
              :value="item.value"
              >{{ item.label }}</el-radio
            >
          </el-radio-group>
        </el-form-item>

        <el-form-item label="发票抬头" prop="buyerName">
          <el-input v-model="fpform.buyerName" placeholder="发票抬头" />
        </el-form-item>
        <el-form-item label="税号" v-if="fpform.invoiceClass == 2">
          <el-input v-model="fpform.buyerTaxNumber" placeholder="税号" />
        </el-form-item>
        <el-form-item label="发票内容:" prop="goodsCode">
          <el-radio-group v-model="fpform.goodsCode">
            <el-radio
              v-for="(item, index) in invoice_goods_code"
              :key="index"
              :value="item.value"
              >{{ item.label }}</el-radio
            >
          </el-radio-group>
        </el-form-item>
        <el-form-item label="接收邮箱">
          <el-input v-model="fpform.email" placeholder="接收邮箱" />
          <div style="color: #e6a23c">如需邮箱接收发票时填写</div>
        </el-form-item>

        <el-form-item label="备注信息">
          <el-input v-model="fpform.remark" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <el-table :data="List" v-if="false">
        <el-table-column label="大赛名称" align="center" prop="commodityName">
          <template #default="scope">
            <el-input v-model="scope.row.commodityName" clearable disabled />
          </template>
        </el-table-column>
        <el-table-column label="单位" align="center" prop="commodityName">
          <template #default="scope">
            <el-input v-model="scope.row.danw" clearable />
          </template>
        </el-table-column>
        <el-table-column label="数量" align="center" prop="commodityName">
          <template #default="scope">
            <el-input v-model="scope.row.numb" clearable disabled />
          </template>
        </el-table-column>
        <el-table-column
          label="单价（含税）"
          align="center"
          prop="commodityName"
        >
          <template #default="scope">
            <el-input v-model="scope.row.amount" clearable disabled />
          </template>
        </el-table-column>
        <el-table-column
          label="金额（含税）"
          align="center"
          prop="commodityName"
        >
          <template #default="scope">
            <el-input v-model="scope.row.amount" clearable disabled />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="tijiao">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Post">
import {
  orderlist,
  orderchak,
  proofAudit,
  invoiceapply,
  merchantParamConfigmerSelect,
  getCommodityNameLists,
} from "@/api/iPayment/index";
import { ElMessage } from "element-plus";
const { proxy } = getCurrentInstance();
const {
  pay_status,
  pay_method,
  commodity_unit,
  commodity_type,
  order_invoice_status,
  invoice_line,
  invoice_goods_code,
  pay_mode,
} = proxy.useDict(
  "pay_status",
  "pay_method",
  "pay_mode",
  "commodity_unit",
  "commodity_type",
  "order_invoice_status",
  "invoice_line",
  "invoice_goods_code"
);
const pay_status_list = computed(() => {
  return pay_status.value.filter((item) => {
    return item.value !== "0";
  });
});
const ConfigmerSelect = ref([]);
const merchantParam = () => {
  merchantParamConfigmerSelect().then((response) => {
    if (response.code === 200) {
      ConfigmerSelect.value = response.data;
    }
  });
};

const goodsType = ref([
  { label: "报名", value: "competition" },
  { label: "赛证互通", value: "cert" },
]);
/** 获取字典标签 */
const getDictLabel = (dictList, value) => {
  const dict = dictList.find((item) => item.value === value);
  return dict ? dict.label : value;
};
const kuaijie = ref([
  { name: "已支付", value: "paid" },
  { name: "待支付", value: "pending" },
  { name: "退款中", value: "refunding" },
  { name: "已退款", value: "refunded" },
]);
const postList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const commodityNameList = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    postCode: undefined,
    postName: undefined,
    status: undefined,
  },
  quickForm: {},
  rules: {
    postName: [
      { required: true, message: "岗位名称不能为空", trigger: "blur" },
    ],
    postCode: [
      { required: true, message: "岗位编码不能为空", trigger: "blur" },
    ],
    postSort: [
      { required: true, message: "岗位顺序不能为空", trigger: "blur" },
    ],
  },
});

const { queryParams, form, rules, quickForm } = toRefs(data);

/** 查询订单列表 */
function getList() {
  loading.value = true;
  orderlist(queryParams.value).then((response) => {
    postList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 查询大赛项目的名称 */
function getCommodityNameList() {
  getCommodityNameLists().then((response) => {
    if (response.code === 200) {
      commodityNameList.value = response.data;
    }
  });
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    postCode: undefined,
    postName: undefined,
    postSort: 0,
    status: "0",
    remark: undefined,
  };
  proxy.resetForm("postRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}
/** 搜索按钮操作 */
function handleQueryTime() {
  console.log(123);
  if (
    queryParams.value.invoiceTime &&
    queryParams.value.invoiceTime.length > 0
  ) {
    queryParams.value.payStartTime = queryParams.value.invoiceTime[0];
    queryParams.value.payEndTime = queryParams.value.invoiceTime[1];
  } else {
    queryParams.value.payStartTime = null;
    queryParams.value.payEndTime = null;
  }

  queryParams.value.pageNum = 1;
  getList();
}

/** 订单号输入限制，只允许输入数字 */
function handleOrderIdInput(value) {
  // 使用正则表达式只保留数字
  queryParams.value.idStr = value.replace(/[^0-9]/g, "");
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.amountStart = null;
  queryParams.value.amountEnd = null;
  queryParams.value.payStartTime = null;
  queryParams.value.payEndTime = null;
  queryParams.value.idStr = null;
  handleQuery();
}
/**快捷选项操作 */
function handleTagClick(type, value) {
  switch (type) {
    case "payContestName":
      queryParams.value.commodityName =
        queryParams.value.commodityName == value ? "" : value;
      break;
    case "payCompany":
      queryParams.value.merId = queryParams.value.merId == value ? "" : value;
      break;
    case "payMethod":
      queryParams.value.payMethod =
        queryParams.value.payMethod == value ? "" : value;
      break;
    case "payStatus":
      queryParams.value.payStatus =
        queryParams.value.payStatus == value ? "" : value;
      break;
    default:
      break;
  }
  handleQuery();
}
/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  console.log(ids.value);
}
const disp = ref(false);
/** 查看按钮操作 */
function handlechak(row) {
  reset();
  console.log(row);
  const postId = row.id || ids.value;
  orderchak(postId).then((response) => {
    form.value = response.data;
    form.value.paymentProofFile = form.value.paymentProofFiles
      ? form.value.paymentProofFiles.split(",")
      : [];
    form.value.teamInfoLists = JSON.parse(form.value.teamInfoList || "[]");
    console.log(form.value.teamInfoLists,'xx');
    open.value = true;
    title.value = "查看订单信息";
    disp.value = true;
  });
}
// 修改
function xiugaichak(row) {
  reset();
  console.log(row);
  const postId = row.id || ids.value;
  orderchak(postId).then((response) => {
    form.value = response.data;
    form.value.paymentProofFile = form.value.paymentProofFiles
      ? form.value.paymentProofFiles.split(",")
      : [];
  form.value.teamInfoLists = JSON.parse(form.value.teamInfoList || "[]");
    open.value = true;
    title.value = "审核订单信息";
    disp.value = false;
  });
}
const shenhe = (item) => {
  const params = {
    id: form.value.id,
    auditStatus: item,
    auditOpinion: form.value.auditOpinion,
  };
  proofAudit(params).then((res) => {
    open.value = false;

    getList();
  });
  console.log(params);
};
const fpopen = ref(false);
const fptitle = ref("");
const fpform = ref({
  invoiceLine: "pc",
  goodsCode: "3049900000000000000",
  invoiceClass: "1",
});
// 定义表单实例（用于调用验证）
const fppostRef = ref(null);

// 表单验证规则
const fprules = reactive({
  invoiceClass: [
    { required: true, message: "请选择个人或企业", trigger: "change" },
  ],

  invoiceLine: [
    { required: true, message: "请选择发票类型", trigger: "change" },
  ],
  goodsCode: [{ required: true, message: "请选择发票内容", trigger: "change" }],
  buyerName: [
    { required: true, message: "请输入发票抬头", trigger: "blur" },
    { min: 1, max: 50, message: "长度在 1 到 50 个字符", trigger: "blur" },
  ],
  buyerTaxNumber: [
    { required: true, message: "请输入税号", trigger: "blur" },
    { min: 6, max: 16, message: "长度在6 到 16 个字符", trigger: "blur" },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    {
      pattern: /^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,4}$/,
      message: "请输入正确的邮箱",
      trigger: "blur",
    },
  ],
  phone: [
    { required: true, message: "请输入联系方式", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号",
      trigger: "blur",
    },
  ],
});
// 申请发票
const List = ref([]);
const shenqinginvoice = (row) => {
  fpopen.value = true;
  fptitle.value = "申请发票";
  fpform.value.id = row.id;
  List.value = [JSON.parse(JSON.stringify(row))];
  console.log(commodity_unit.value);
  List.value[0].danw = commodity_unit.value.find((item) => {
    return item.label == row.commodityType;
  }).value;
  List.value[0].numb = 1;

  console.log(fpform.value);
  resetForm();
  fpform.value.invoiceType = "1"; // 发票类型 1付款 2退费 目前没有退款，默认传1
};

const tijiao = () => {
  fppostRef.value.validate((valid) => {
    if (valid) {
      fpform.value.commodityUnit = List.value[0].danw;
      invoiceapply(fpform.value)
        .then((response) => {
          ElMessage.success("申请成功");
          fpopen.value = false;
          loading.value = true;
          setTimeout(() => {
            getList();
          }, 1000);
        })
        .catch((error) => {
          ElMessage.error(error.message || "操作失败");
        });
    } else {
      ElMessage.error("请检查表单填写");
    }
  });
};
// 重置表单
const resetForm = () => {
  proxy.resetForm("fppostRef");
};
/** 导出按钮操作 */
function handleExport() {
  proxy.download(
    "system/order/export",
    {
      ...queryParams.value,
    },
    `订单列表.xlsx`
  );
}

getList();
getCommodityNameList();
merchantParam();
</script>
<style lang="scss" scoped>
.action-btns {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 20px 0;
}
:deep(.el-table) {
  .cell {
    padding: 0 4px !important;
  }
}
  .status-deleted {
    color: #f56c6c;
  }
:deep(.el-descriptions) {
  .el-descriptions__content {
    min-width: 380px !important;
  }
}
</style>