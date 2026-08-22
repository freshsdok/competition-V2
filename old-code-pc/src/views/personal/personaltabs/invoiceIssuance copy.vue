<template>
  <div style="background-color: #f5f5f5">
    <div class="container-custom">
      <Breadcrumbar />
      <el-card class="kapian">
        <div class="zzxx">订单信息</div>
        <div class="invoice-order-info">
          <el-row :gutter="24">
            <el-col :span="12">
              <div class="invoice-order-info-item">
                <span class="invoice-order-info-label">订单号：</span>
                <span class="invoice-order-info-label">{{
                  xinxi.id
                }}</span>
              </div></el-col
            >
            <el-col :span="12">
              <div class="invoice-order-info-item">
                <span class="invoice-order-info-label">订单金额：</span>
                <span
                  class="invoice-order-info-value"
                  style="color: #e53935; font-weight: 500"
                  >¥{{ xinxi.amount }}</span
                >
              </div></el-col
            >
            <el-col :span="12">
              <div class="invoice-order-info-item">
                <span class="invoice-order-info-label">下单时间：</span>
                <span class="invoice-order-info-value">
                  {{ xinxi.createTime }}</span
                >
              </div></el-col
            >
            <el-col :span="12">
              <div class="invoice-order-info-item">
                <span class="invoice-order-info-label">赛事信息：</span>
                <span class="invoice-order-info-value">
                  <span class="event-name"> {{ xinxi.commodityName }}</span>
                  <!-- <span class="event-name">大唐杯2025大赛</span>-
              <span class="track-name">产教融合5G+创新应用赛</span>-
              <span class="group-name">本科B组</span> -->
                </span>
              </div></el-col
            >
          </el-row>
        </div>
      </el-card>
      <el-card class="kapian">
        <div class="zzxx">开票信息</div>
        <el-form
          ref="ruleFormRef"
          :model="ruleForm"
          :rules="fprules"
          label-width="180"
        >
          <el-row>
              <el-col :span="24">
              <div
                style="margin-left: 30px; font-size: 20px; font-weight: bold;margin-top: 20px;margin-bottom: 20px;"
              >
                收款信息
              </div>
            </el-col>
          
            <el-col :span="12">
              <el-form-item label="收款单位名称:" prop="invoiceClass">
               大唐杯科技有限公司
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="收款单位纳税人识别号:" prop="invoiceLine">
              1234567890123456
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <div
                style="margin-left: 30px; font-size: 20px; font-weight: bold;margin-top: 20px;margin-bottom: 20px;"
              >
                抬头信息
              </div>
            </el-col>
            <el-col :span="12">
              <el-form-item label="发票抬头类型:" prop="invoiceClass">
                <el-radio-group
                  v-model="ruleForm.invoiceClass"
                  @change="isgeren"
                >
                  <el-radio value="2">企业</el-radio>
                  <el-radio value="1">个人</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="发票类型:" prop="invoiceLine">
                <el-radio-group v-model="ruleForm.invoiceLine">
                  <!-- <el-radio value="Sponsorship">增值税普通发票</el-radio>
                  <el-radio value="Venue">增值税专用发票</el-radio> -->
                  <el-radio
                    v-for="(item, index) in invoice_line"
                    :key="index"
                    :value="item.value"
                    >{{ item.label }}</el-radio
                  >
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="发票抬头:" prop="buyerNametype">
                <el-input
                  v-if="ruleForm.invoiceClass == 2"
                  v-model="ruleForm.buyerNametype"
                  placeholder="请输入发票抬头"
                  clearable
                />
                <el-select
                  v-else
                  v-model="ruleForm.buyerNametype"
                  placeholder="请选择发票抬头"
                >
                  <el-option label="个人" value="个人" />
                  <el-option label="个人实名" value="个人实名" />
                </el-select>
              </el-form-item>
            </el-col>
            <!-- <el-col :span="12" v-if="ruleForm.invoiceClass == 2">
              <el-form-item label="纳税人识别号:" prop="buyerTaxNumber">
                <el-input
                  v-model="ruleForm.buyerTaxNumber"
                  placeholder="请输入纳税人识别号"
                  clearable
                />
              </el-form-item>
            </el-col> -->
            <el-col :span="12" v-if="ruleForm.invoiceClass == 2">
              <el-form-item label="纳税人识别号:">
                <el-input
                  v-model="ruleForm.buyerTaxNumber"
                  placeholder="请输入纳税人识别号"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col :span="24" v-if="ruleForm.buyerName == '个人实名'">
              <el-form-item label="个人实名:">
                {{ JSON.parse(getinfo()).nickName }}
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <div
                style="font-size: 20px; font-weight: bold; margin: 20px 30px"
              >
                发票内容
              </div>
            </el-col>
            <el-col :span="12">
              <el-form-item label="发票内容:" prop="goodsCode">
               <el-select
                 
                  v-model="ruleForm.buyerneirong"
                  placeholder="请选择发票内容"
                >
                  <el-option label="*现代服务*报名费" value="*现代服务*报名费" />
                  <el-option label="个人实名" value="个人实名" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="开票金额:" prop="goodsCode">
                <span
                  class="invoice-order-info-value"
                  style="color: #e53935; font-weight: 500"
                  >¥{{ xinxi.amount }}</span
                >
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <div
                style="font-size: 20px; font-weight: bold; margin: 20px 30px"
              >
                发票备注
                <span style="font-size: 18px; color: #999; font-weight: 500"
                  >选填</span
                >
              </div>
            </el-col>

            <el-col :span="24">
              <el-form-item label="备注:" prop="remark">
                <el-input
                  v-model="ruleForm.remark"
                  type="textarea"
                  placeholder="填写后展示在发票备注区域"
                  @input="handleRemarkInput"
                ></el-input>
                <div style="color: #ccc">
                  发票备注不可超过230个字节，一个中文字符为两个字节
                  <!-- ，当前已输入{{
                    byteLength
                  }}字符 -->
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="接收邮箱:">
                <el-input
                  v-model="ruleForm.email"
                  placeholder="请输入接收邮箱"
                  clearable
                />
                <div style="color: #e6a23c">如需邮箱接收发票时填写</div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <div style="margin-top: 20px; display: flex; justify-content: flex-end">
          <el-button class="sctx" @click="zhanghaoshezhi"
            >返回订单列表</el-button
          >
          <el-button class="sctx" type="primary" @click="tijiao"
            >申请开票</el-button
          >
        </div>
      </el-card>
    </div>
  </div>
</template>
<script setup>
import Breadcrumbar from "@/components/breadcrumbar.vue";
import {
  updatePaymentProof,
  orderchak,
  invoiceapply,
    queryInvoiceAmount,
} from "@/api/personal/index";
import Cookies from "js-cookie";
import { getToken, getinfo } from "@/utils/auth";
import { ElMessage, genFileId } from "element-plus";
import { useRoute, useRouter } from "vue-router";
const { proxy } = getCurrentInstance();
const { invoice_line, invoice_goods_code, commodity_unit } = proxy.useDict(
  "invoice_line",
  "invoice_goods_code",
  "commodity_unit"
);
const router = useRouter();
const route = useRoute();
// 返回
const zhanghaoshezhi = () => {
  router.push({
    path: "/personal/paymentrecords",
  });
};
const ruleForm = ref({
  invoiceClass: "2",
  invoiceLine: "pc",
  goodsCode: "3049900000000000000",

  remark: "",
});
// 表单验证规则
const fprules = reactive({
  invoiceLine: [
    { required: true, message: "请选择发票类型", trigger: "change" },
  ],
  invoiceClass: [
    { required: true, message: "请选择发票抬头类型", trigger: "change" },
  ],
  goodsCode: [{ required: true, message: "请选择发票内容", trigger: "change" }],
  buyerNametype: [
    { required: true, message: "请输入发票抬头", trigger: "blur" },
    { min: 1, max: 50, message: "长度在 1 到 50 个字符", trigger: "blur" },
  ],
  buyerTaxNumber: [
    { required: true, message: "请输入税号", trigger: "blur" },
    { min: 6, max: 16, message: "长度在6 到 16 个字符", trigger: "blur" },
  ],
});
// 计算字节数（中文2字节，其他1字节）
const getByteLength = (str) => {
  let bytes = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    // 中文、全角符号等通常 > 255（UTF-8 中占 2~4 字节，但发票系统一般按 GBK：中文=2字节）
    // 简化处理：非 ASCII（>127）视为 2 字节（适用于大多数国内发票场景）
    bytes += char > 127 ? 2 : 1;
  }
  return bytes;
};

const byteLength = computed(() => getByteLength(ruleForm.value.remark));
// 输入时自动截断超长内容
const handleRemarkInput = () => {
  let str = ruleForm.value.remark;
  console.log(12111);
  while (getByteLength(str) > 230) {
    str = str.slice(0, -1); // 逐个删除末尾字符
  }
  if (str !== ruleForm.value.remark) {
    ruleForm.value.remark = str;
  }
};
const isgeren = () => {
  if (ruleForm.value.invoiceClass == 1) {
    ruleForm.value = {
      invoiceLine: "pc",
      goodsCode: "3049900000000000000",
      invoiceClass: "1",
      remark: "",
    };
    // ruleForm.value.buyerName = JSON.parse(getinfo()).nickName;
    ruleForm.value.buyerName = "个人";
    console.log(ruleForm.value.invoiceClass);
  } else {
    ruleForm.value = {
      invoiceLine: "pc",
      goodsCode: "3049900000000000000",
      invoiceClass: "2",

      remark: "",
    };
    // ruleForm.value.buyerName = "";
  }
};
const xinxi = ref({
  payStatus: "paid",
});
const orderchaklist = () => {
  // console.log(route.query)
  orderchak(route.query.id).then((res) => {
    xinxi.value = res.data;
  });
};
orderchaklist();
const ruleFormRef = ref(null);
const tijiao = () => {
  ruleFormRef.value.validate((valid) => {
    if (valid) {
      ruleForm.value.id = route.query.id;
      ruleForm.value.invoiceType = "1";

      if (ruleForm.value.invoiceClass == 1) {
        if (ruleForm.value.buyerNametype == "个人") {
          ruleForm.value.buyerName = "个人";
        } else {
          ruleForm.value.buyerName = JSON.parse(getinfo()).nickName;
        }
      } else {
        ruleForm.value.buyerName = ruleForm.value.buyerNametype;
      }

      invoiceapply(ruleForm.value).then((res) => {
        if (res.code == 200) {
          setTimeout(() => {
            router.push({
              path: "/personal/paymentrecords",
            });
          }, 500);
        }
      });
    }
  });
};
</script>



<style scoped lang="scss">
.kapian {
  background-color: #fff;
  padding: 0;
  margin: 20px 0;
  .zzxx {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e0e0e0;
  }
  .invoice-order-info {
    background-color: #fafafa;
    padding: 15px;
    border-radius: 4px;
    margin-bottom: 20px;
    border: 1px solid #e0e0e0;
    .invoice-order-info-item {
      display: flex;
      gap: 20px;
      margin-bottom: 10px;
      .invoice-order-info-label {
        width: 100px;
        font-weight: 500;
        color: #666;
      }
      .invoice-order-info-value {
        flex: 1;
        .event-name {
          color: #1976d2;
          font-weight: 500;
        }
        .track-name {
          color: #ff9800;
        }
        .group-name {
          color: #4caf50;
        }
      }
    }
  }
}
:deep(.el-upload-list__item) {
  width: 200px;
  margin: 0 auto;
}
.submit-btn {
  width: 100%;
  padding: 15px;
  height: 50px;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
}
</style>