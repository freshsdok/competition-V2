<template>
  <div style="background-color: #f5f5f5" v-loading="loading">
    <div class="container-custom">
      <Breadcrumbar />
      <el-card class="kapian" v-for="(item, index) in orderIds" :key="index">
        <div class="zzxx">开票信息</div>
        <el-form
          :model="ruleForm"
          :rules="rules"
          ref="invoiceFormRef"
          label-width="180"
        >
          <el-row>
            <template v-if="item?.commodityType === 'cert'">
              <el-col :span="24">
                <div
                  style="
                    margin-left: 30px;
                    font-size: 20px;
                    font-weight: bold;
                    margin-top: 20px;
                    margin-bottom: 20px;
                  "
                >
                  赛证互通信息
                </div>
              </el-col>
              <el-col
                :span="12"
                v-for="(team, i) in item?.certApplyInfo || []"
                :key="i"
                style="padding: 0 20px"
              >
                <el-card style="background-color: #fff; margin-bottom: 10px">
                  <div style="display: flex">
                    <div
                      style="
                        min-width: 100px;
                        text-align: end;
                        padding: 0 10px 4px 0px;
                        color: #3169f8;
                        font-weight: bold;
                      "
                    >
                      {{ team.rulerName }}
                    </div>
                  </div>
                  <div style="display: flex">
                    <div
                      style="
                        min-width: 120px;
                        text-align: end;
                        padding-right: 10px;
                      "
                    >
                      申请人姓名:
                    </div>
                    <div style="display: flex; flex-wrap: wrap">
                       {{ team.userName }}
                    </div>
                  </div>
                  <div style="display: flex">
                    <div
                      style="
                        min-width: 120px;
                        text-align: end;
                        padding-right: 10px;
                      "
                    >
                      源证书名称:
                    </div>
                    <div style="display: flex">
                      <div>
                        {{ team?.originCertList?.map((item) => item.certConfigName).join(',') || '-'  }}
                      </div>
                    </div>
                  </div>
                  <div style="display: flex">
                    <div
                      style="
                        min-width: 120px;
                        text-align: end;
                        padding-right: 10px;
                      "
                    >
                      目标证书名称:
                    </div>
                    <div style="display: flex">
                      <div>
                        {{ team?.targetCertList?.map((item) => item.certConfigName).join(',') || '-'  }}
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </template>
            <template v-else>
              <el-col :span="24">
                <div
                  style="
                    margin-left: 30px;
                    font-size: 20px;
                    font-weight: bold;
                    margin-top: 20px;
                    margin-bottom: 20px;
                  "
                >
                  报名信息
                </div>
              </el-col>
              <el-col
                :span="12"
                v-for="(team, i) in item?.members || []"
                :key="i"
                style="padding: 0 20px"
              >
                <el-card style="background-color: #fff; margin-bottom: 10px">
                  <div style="display: flex">
                    <div
                      style="
                        min-width: 100px;
                        text-align: end;
                        padding-right: 10px;
                      "
                    >
                      赛事名称:
                    </div>
                    <div>
                      <span class="text-[#3169f8]">{{
                        team.competitionName
                      }}</span>
                      <span>-</span>
                      <span class="text-[#FF8800]">{{
                        team.competitionTrackName
                      }}</span>
                      <span>-</span>
                      <span class="text-[#51C512]">{{
                        team.secondLevelName
                      }}</span>
                    </div>
                  </div>
                  <div style="display: flex">
                    <div
                      style="
                        min-width: 100px;
                        text-align: end;
                        padding-right: 10px;
                      "
                    >
                      队员:
                    </div>
                    <div style="display: flex; flex-wrap: wrap">
                      <div
                        v-for="(member, memberindex) in team.userInfo"
                        :key="memberindex"
                      >
                        {{ member.userName }}（{{ member.idCard }}）
                      </div>
                    </div>
                  </div>
                  <div style="display: flex">
                    <div
                      style="
                        min-width: 100px;
                        text-align: end;
                        padding-right: 10px;
                      "
                    >
                      指导教师:
                    </div>
                    <div style="display: flex">
                      <div>
                        {{ team.guideTeacher }}
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </template>
            <el-col :span="24">
              <div
                style="
                  margin-left: 30px;
                  font-size: 20px;
                  font-weight: bold;
                  margin-top: 20px;
                  margin-bottom: 20px;
                "
              >
                收款信息
              </div>
            </el-col>

            <el-col :span="12">
              <el-form-item label="收款单位名称:">
                {{ item.merName }}
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="收款单位纳税人识别号:">
                {{ item.taxNum }}
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <div
                style="
                  margin-left: 30px;
                  font-size: 20px;
                  font-weight: bold;
                  margin-top: 20px;
                  margin-bottom: 20px;
                "
              >
                抬头信息
              </div>
            </el-col>
            <el-col :span="12">
              <el-form-item
                label="发票抬头类型:"
                :prop="`${index}.invoiceClass`"
              >
                <el-radio-group
                  v-model="ruleForm[index].invoiceClass"
                  @change="isgeren(index)"
                >
                  <el-radio value="2">企业</el-radio>
                  <el-radio value="1">个人</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="发票类型:" :prop="`${index}.invoiceLine`">
                <el-radio-group v-model="ruleForm[index].invoiceLine">
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
            <el-col :span="24" v-if="ruleForm[index].invoiceClass == 1">
              <div style="width: 90%; margin: 0 auto;margin-bottom: 20px;">
                <el-alert type="primary" show-icon>
                  <div style="display: flex">
                    <div>
                      个人抬头的发票一般无法在单位报销，请您确认发票抬头。
                    </div>
                  </div>
                </el-alert>
              </div>
            </el-col>
            <el-col :span="12">
              <el-form-item label="发票抬头:" :prop="`${index}.buyerName`">
                <!-- <el-input
                  v-if="ruleForm[index].invoiceClass == 2"
                  v-model="ruleForm[index].buyerName"
                  placeholder="请输入发票抬头"
                  clearable
                /> -->
                <el-autocomplete
                  v-if="ruleForm[index].invoiceClass == 2"
                  v-model="ruleForm[index].buyerName"
                  :fetch-suggestions="querySearch"
                  placeholder="请输入发票抬头"
                  clearable
                  @select="(item) => xuanzhetaitou(item, index)"
                />
                <el-select
                  v-else
                  v-model="ruleForm[index].buyerNametype"
                  @change="isbuyerName(index)"
                  placeholder="请选择发票抬头"
                >
                  <el-option label="个人" value="个人" />
                  <el-option label="个人实名" value="个人实名" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="ruleForm[index].invoiceClass == 2">
              <el-form-item
                label="纳税人识别号:"
                :prop="`${index}.buyerTaxNumber`"
              >
                <el-input
                  v-model="ruleForm[index].buyerTaxNumber"
                  placeholder="请输入纳税人识别号"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col
              :span="24"
              v-if="ruleForm[index].buyerNametype == '个人实名'"
            >
              <el-form-item label="个人实名:">
                {{ ruleForm[index].buyerName }}
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
              <el-form-item label="发票内容:" :prop="`${index}.goodsCode`">
                <el-select
                  v-model="ruleForm[index].goodsCode"
                  placeholder="请选择发票内容"
                >
                  <el-option
                    v-for="(x, i) in item.invoiceContent"
                    :key="i"
                    :label="Object.values(x)[0]"
                    :value="Object.keys(x)[0]"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="开票金额:">
                <span
                  class="invoice-order-info-value"
                  style="color: #e53935; font-weight: 500"
                  >¥{{ item.invoiceAmount }}</span
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
              <el-form-item label="备注:">
                <el-input
                  v-model="ruleForm[index].remark"
                  type="textarea"
                  placeholder="填写后展示在发票备注区域"
                  @input="handleRemarkInput(index)"
                ></el-input>
                <div style="color: #ccc">
                  发票备注不可超过230个字节，一个中文字符为两个字节
                </div>
                <div style="margin-bottom: 30px; width: 100%">
                  <span style="font-size: 14px; color: #999"> 快捷备注</span>
                  <br />
                  <el-button
                    @click="handleNextPage(index, x)"
                    v-for="(x, i) in ruleForm[index].kuaijiebeizhu"
                    :key="i"
                    style="margin: 10px 10px 0 0"
                  >
                    {{
                      x.label != "队员姓名" && x.label != "指导教师"
                        ? x.value
                        : x.label
                    }}
                  </el-button>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="接收邮箱:" :prop="`${index}.email`">
                <el-input
                  v-model="ruleForm[index].email"
                  placeholder="请输入接收邮箱"
                  clearable
                />
                <div style="color: #e6a23c">如需邮箱接收发票时填写</div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
      <div style="margin: 20px 0; display: flex; justify-content: flex-end">
        <el-button class="sctx" @click="zhanghaoshezhi">返回订单列表</el-button>
        <el-button class="sctx" type="primary" @click="tijiao" :loading="loding"
          >申请开票</el-button
        >
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, getCurrentInstance } from "vue";
import Breadcrumbar from "@/components/breadcrumbar.vue";
import {
  queryInvoiceAmount,
  invoiceapplyNew,
  selectCompetitionApplyInfoListByTeamCode,
  selectInvoicePerInfo,
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
const loading = ref(false);
const gerenxinxi = ref({
  enterpriseName: "",
  taxpayerIdentificationNumber: "",
});
const gerenku = () => {
  const params = {
    userId: JSON.parse(getinfo()).userId,
  };
  selectInvoicePerInfo(params).then((res) => {
    console.log(res);
    if (res.data.length > 0) {
      gerenxinxi.value = res.data[0];
    }
  });
};
const querySearch = (queryString, cb) => {
  let params = {};
  if (queryString == "") {
    params = {
      userId: JSON.parse(getinfo()).userId,
      pageNum: 1,
      pageSize: 10,
    };
  } else {
    params = {
      enterpriseName: queryString,
      pageNum: 1,
      pageSize: 10,
    };
  }

  selectInvoicePerInfo(params).then((res) => {
    res.data?.map((item) => {
      item.value = item.enterpriseName;
      item.label = item.taxpayerIdentificationNumber;
    });
    cb(res.data);
  });
};
const xuanzhetaitou = (item, index) => {
  // ruleForm.value[index].buyerName = item.value;
  ruleForm.value[index].buyerTaxNumber = item.taxpayerIdentificationNumber;
};
// gerenku();
const router = useRouter();
const route = useRoute();
const handleNextPage = (index, x) => {
  // ruleForm.value[index].remark+=x.label+':'
  ruleForm.value[index].remark += x.value + "，";
  handleRemarkInput(index);
};
// 返回
const zhanghaoshezhi = () => {
  router.push({
    path: "/personal/paymentrecords",
  });
};

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

// 输入时自动截断超长内容
const handleRemarkInput = (index) => {
  let str = ruleForm.value[index].remark;
  while (getByteLength(str) > 230) {
    str = str.slice(0, -1); // 逐个删除末尾字符
  }
  if (str !== ruleForm.value[index].remark) {
    ruleForm.value[index].remark = str;
  }
};
const isgeren = (index) => {
  if (ruleForm.value[index].invoiceClass == 1) {
    ruleForm.value[index] = {
      merId: orderIds.value[index].merId,
      invoiceAmount: orderIds.value[index].invoiceAmount,
      userIds: orderIds.value[index].userIds,
      orderIds: orderIds.value[index].orderIds,
      invoiceClass: "1",
      invoiceLine: "pc",
      remark: "",
      invoiceType: 1,
      commodityType: orderIds.value[index]?.commodityType || '',
      randomId: generateUUID(),
      goodsCode: Object.keys(orderIds.value[index].invoiceContent[0])[0],
    };
    ruleForm.value[index].buyerNametype = "个人";
    ruleForm.value[index].buyerName = "个人";
    kuaijiebeizhu(
      orderIds.value[index].teamCodes,
      orderIds.value[index].userIds,
      index
    );
  } else {
    ruleForm.value[index] = {
      merId: orderIds.value[index].merId,
      invoiceAmount: orderIds.value[index].invoiceAmount,
      userIds: orderIds.value[index].userIds,
      orderIds: orderIds.value[index].orderIds,
      invoiceClass: "2",
      invoiceLine: "pc",
      remark: "",
      commodityType: orderIds.value[index]?.commodityType || '',
      invoiceType: 1,
      randomId: generateUUID(),
      goodsCode: Object.keys(orderIds.value[index].invoiceContent[0])[0],
    };
    kuaijiebeizhu(
      orderIds.value[index].teamCodes,
      orderIds.value[index].userIds,
      index
    );
  }
  // 清除表单验证状态
  if (invoiceFormRef.value[index]) {
    invoiceFormRef.value[index].clearValidate();
  }
};
const isbuyerName = (index) => {
  if (ruleForm.value[index].buyerNametype == "个人") {
    ruleForm.value[index].buyerName = "个人";
  } else {
    ruleForm.value[index].buyerName = JSON.parse(getinfo()).nickName;
  }
};
// 表单引用
const invoiceFormRef = ref([]);

// 验证规则
const rules = reactive({
  // 动态生成每个表单项的验证规则
});

// 动态生成验证规则的函数
const generateRules = () => {
  const newRules = {};
  orderIds.value.forEach((_, index) => {
    newRules[`${index}.invoiceClass`] = [
      { required: true, message: "请选择发票抬头类型", trigger: "blur" },
    ];
    newRules[`${index}.invoiceLine`] = [
      { required: true, message: "请选择发票类型", trigger: "change" },
    ];
    newRules[`${index}.buyerName`] = [
      { required: true, message: "请输入发票抬头", trigger: "blur" },
    ];
    newRules[`${index}.buyerTaxNumber`] = [
      { required: true, message: "请输入纳税人识别号", trigger: "blur" },
      {
        pattern: /^[A-Z0-9]{8,20}$/,
        message: "纳税人识别号格式不正确",
        trigger: "blur",
      },
    ];
    newRules[`${index}.goodsCode`] = [
      { required: true, message: "请选择发票内容", trigger: "change" },
    ];
    newRules[`${index}.email`] = [
      { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" },
    ];
  });
  Object.assign(rules, newRules);
};

const ruleForm = ref([]);

const orderIds = ref([]);

const kuaijiebeizhu = (teamCodes, userIds, index) => {
  // 证书发票需要特殊处理
  let item = orderIds?.value[index] || null;
  if (item && item?.commodityType === 'cert') {
    ruleForm.value[index].kuaijiebeizhu = item?.certApplyRemarkInfo || [];
    return;
  };
  const params = {
    teamCodes: teamCodes,
    memberIds: userIds.join(","),
  };

  selectCompetitionApplyInfoListByTeamCode(params).then((res) => {
    ruleForm.value[index].kuaijiebeizhu = res.data;
  });
};
const orderchaklist = () => {
  loading.value = true;
  const params = JSON.parse(localStorage.getItem("kaipiaoconter"));
  queryInvoiceAmount(params).then((res) => {
    if (res.code === 200) {
      orderIds.value = res.data;
      ruleForm.value = [];
      for (let i = 0; i < orderIds.value.length; i++) {
        ruleForm.value[i] = {
          merId: orderIds.value[i].merId,
          invoiceAmount: orderIds.value[i].invoiceAmount,
          userIds: orderIds.value[i].userIds,
          orderIds: orderIds.value[i].orderIds,
          invoiceClass: "2",
          invoiceLine: "pc",
          remark: "",
          commodityType: orderIds.value[i]?.commodityType || '',
          invoiceType: 1,
          randomId: generateUUID(),
          goodsCode: Object.keys(orderIds.value[i].invoiceContent[0])[0],
        };
        kuaijiebeizhu(
          orderIds.value[i].teamCodes,
          orderIds.value[i].userIds,
          i
        );
      }
      loading.value = false;
      // 生成验证规则
      generateRules();
    }
  });
};
orderchaklist();

function generateUUID() {
  return "xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx".replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0;
    const v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

console.log(generateUUID(), 23);
const loding = ref(false);
const tijiao = () => {
  // 验证所有表单
  const validatePromises = invoiceFormRef.value?.map((form, index) => {
    if (!form) return Promise.resolve(true);
    return new Promise((resolve) => {
      form.validate((valid) => {
        resolve(valid);
      });
    });
  });
  Promise.all(validatePromises).then((results) => {
    const allValid = results.every((result) => result);
    if (allValid) {
      loding.value = true;
      invoiceapplyNew(ruleForm.value).then((res) => {
        loding.value = false;
        if (res.code === 200) {
          router.push({
            path: "/personal/list",
            query: {
              lefttabs: "开票记录",
            },
          });
        }
      }).finally(() => {
        loding.value = false;
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