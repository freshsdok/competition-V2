<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
      label-width="100px"
    >
      <el-form-item label="公司名称" prop="merName">
        <el-input
          v-model.trim="queryParams.merName"
          placeholder="请输入公司名称"
          clearable
          style="width: 200px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          @change="handleQuery"
          style="width: 200px"
        >
          <el-option label="开启" value="1" />
          <el-option label="关闭" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          >新增</el-button
        >
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <!-- 表格区域 -->
    <el-table
      v-loading="loading"
      :data="merchantList"
      @selection-change="handleSelectionChange"
      fit
    >
      <!-- <el-table-column type="selection" width="55" align="center" /> -->
      <el-table-column label="序号" align="center" type="index" width="50" />
      <el-table-column
        label="公司名称"
        align="left"
        prop="merName"
        :show-overflow-tooltip="true"
        min-width="200"
      />
      <el-table-column
        label="商户号"
        align="center"
        prop="merId"
        :show-overflow-tooltip="true"
        min-width="200"
      />
      <el-table-column
        label="状态"
        align="center"
        prop="status"
        min-width="120"
      >
        <template #default="scope">
          <el-switch
            :model-value="scope.row.status == '1'"
            :active-value="true"
            :inactive-value="false"
            @change="(val) => handleSwitchChange(scope.row, val)"
          />
        </template>
      </el-table-column>
      <!-- <el-table-column
        label="作用范围"
        align="center"
        prop="work"
        :show-overflow-tooltip="true"
        min-width="120"
      /> -->
      <el-table-column
        label="开户行"
        align="center"
        prop="bank"
        :show-overflow-tooltip="true"
        min-width="150"
      />
      <el-table-column
        label="卡号"
        align="center"
        prop="account"
        :show-overflow-tooltip="true"
        min-width="200"
      />

      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        min-width="180"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        min-width="280"
        fixed="right"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-button link type="primary" @click="handleUpdateBase(scope.row)"
            >基本信息</el-button
          >
          <el-button link type="primary" @click="handleUpdatePayment(scope.row)"
            >支付配置</el-button
          >
          <el-button link type="primary" @click="handleUpdateInvoice(scope.row)"
            >开票配置</el-button
          >
          <el-button link type="danger" @click="handleDelete(scope.row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页区域 -->
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 基本信息弹窗 -->
    <el-dialog :title="title" v-model="openBase" width="1000px" append-to-body>
      <el-form
        ref="formBaseRef"
        :model="form"
        :rules="rules.base"
        label-width="120px"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="公司名称" prop="merName">
              <el-input v-model="form.merName" placeholder="请输入公司名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开户行" prop="bank">
              <el-input v-model="form.bank" placeholder="请输入开户行" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="卡号" prop="account">
              <el-input v-model="form.account" placeholder="请输入卡号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开户行地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入开户行地址" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="作用范围" prop="work">
              <div class="tree-container">
                <el-tree
                  ref="scopeTreeRef"
                  :data="scopeTreeData"
                  show-checkbox
                  node-key="id"
                  :default-expanded-keys="expandedKeys"
                  :props="treeProps"
                  @check="handleScopeTreeCheck"
                  class="scope-tree"
                />
                <div
                  class="tree-selected-info"
                  v-if="form.workScopeList && form.workScopeList.length > 0"
                >
                  <span>已选择 {{ form.workScopeList.length }} 项</span>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="action-btns">
        <el-button @click="cancelBase">取消</el-button>
        <el-button type="primary" @click="submitBaseForm">确定</el-button>
      </div>
    </el-dialog>

    <!-- 支付配置弹窗 -->
    <el-dialog
      :title="payTitle"
      v-model="openPayment"
      width="1000px"
      append-to-body
    >
      <el-form
        ref="formPaymentRef"
        :model="form"
        :rules="rules.payment"
        label-width="120px"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="商户号" prop="merId">
              <el-input v-model="form.merId" placeholder="请输入商户号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收银用户ID" prop="feeUserId">
              <el-input
                v-model="form.feeUserId"
                placeholder="请输入收银用户ID"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="终端号" prop="termId">
              <el-input v-model="form.termId" placeholder="请输入终端号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付APPID" prop="payAppId">
              <el-input v-model="form.payAppId" placeholder="请输入支付APPID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付APP密钥" prop="payAppSecret">
              <el-input
                v-model="form.payAppSecret"
                type="password"
                placeholder="请输入支付APP密钥"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="支付私钥" prop="payPrivateKey">
              <el-input
                v-model="form.payPrivateKey"
                type="textarea"
                rows="4"
                placeholder="请输入支付私钥"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="支付公钥" prop="payPublicKey">
              <el-input
                v-model="form.payPublicKey"
                type="textarea"
                rows="4"
                placeholder="请输入支付公钥"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="action-btns">
        <el-button @click="cancelPayment">取消</el-button>
        <el-button type="primary" @click="submitPaymentForm">确定</el-button>
      </div>
    </el-dialog>

    <!-- 开票配置弹窗 -->
    <el-dialog
      :title="invoiceTitle"
      v-model="openInvoice"
      width="1000px"
      append-to-body
    >
      <el-form
        ref="formInvoiceRef"
        :model="form"
        :rules="rules.invoice"
        label-width="180px"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="发票APPKEY" prop="invoiceAppKey">
              <el-input
                v-model="form.invoiceAppKey"
                placeholder="请输入发票APPKEY"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票APP密钥" prop="invoiceAppSecret">
              <el-input
                v-model="form.invoiceAppSecret"
                type="password"
                placeholder="请输入发票APP密钥"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票accessToken" prop="invoiceAccessToken">
              <el-input
                v-model="form.invoiceAccessToken"
                type="password"
                placeholder="请输入发票accessToken"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="税号" prop="taxNum">
              <el-input v-model="form.taxNum" placeholder="请输入税号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分机号" prop="extension">
              <el-input v-model="form.extension" placeholder="请输入分机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="税率" prop="taxRate">
              <el-input-number
                v-model="form.taxRate"
                :min="0"
                :max="1"
                :step="0.01"
                :precision="2"
                placeholder="请输入税率"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开票人" prop="clerk">
              <el-input v-model="form.clerk" placeholder="请输入开票人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审核人" prop="checker">
              <el-input v-model="form.checker" placeholder="请输入审核人" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="开票内容" prop="contentList">
              <div class="invoice-content-container">
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-select
                      v-model="currentInvoiceGoodsCode"
                      placeholder="请选择商品编码"
                      clearable
                    >
                      <el-option
                        v-for="dict in invoice_goods_code"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                      />
                    </el-select>
                  </el-col>
                  <el-col :span="8">
                    <el-select
                      v-model="currentFeeType"
                      placeholder="请选择收费类型"
                      clearable
                    >
                      <el-option
                        v-for="dict in fee_type"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                      />
                    </el-select>
                  </el-col>

                  <el-col :span="8">
                    <el-button type="primary" @click="addInvoiceContent"
                      >添加</el-button
                    >
                  </el-col>
                </el-row>
                <div
                  class="invoice-content-list"
                  v-if="form.contentList && form.contentList.length > 0"
                >
                  <el-tag
                    v-for="(item, index) in form.contentList"
                    :key="index"
                    closable
                    @close="removeInvoiceContent(index)"
                    style="margin: 5px"
                  >
                    {{ getInvoiceContentDisplay(item) }}
                  </el-tag>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="action-btns">
        <el-button @click="cancelInvoice">取消</el-button>
        <el-button type="primary" @click="submitInvoiceForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="MerchantConfig">
import { ref, reactive, toRefs, getCurrentInstance } from "vue";
import Pagination from "@/components/Pagination/index.vue";
import RightToolbar from "@/components/RightToolbar/index.vue";
import { parseTime } from "@/utils/ruoyi";
import {
  merchantParamConfiglist,
  addmerchantParamConfig,
  merchantParamConfigid,
  updatamerchantParamConfig,
  deletemerchantParamConfig,
  merchantParamConfigidchangeStatus,
  getSecondList,
} from "@/api/iPayment/merchant";


const { proxy } = getCurrentInstance();
const { commodity_type, fee_type, invoice_goods_code } = proxy.useDict(
  "commodity_type",
  "fee_type",
  "invoice_goods_code"
);
const getSecond = () => {
  getSecondList().then((res) => {
    // 转换为Tree所需的数据结构
    scopeTreeData.value = commodity_type.value.map((item) => {
      // 创建一级节点
      const firstLevelNode = {
        id: `${item.value}-null`,
        label: item.label,
        value: item.value,
        categoryCode: item.value,
        categoryName: item.label,
        children: [],
      };

      // 如果有二级数据，添加为子节点
      if (res.data && res.data[item.value]) {
        firstLevelNode.children = res.data[item.value].map((secondItem) => ({
          id: `${item.value}-${secondItem.competitionSeriesId}`,
          label: secondItem.competitionName,
          value: secondItem.competitionSeriesId,
          eventId: secondItem.competitionSeriesId,
          eventName: secondItem.competitionName,
          categoryCode: item.value,
          categoryName: item.label,
          leaf: true,
        }));
      }

      return firstLevelNode;
    });

    // 默认展开所有一级节点
    expandedKeys.value = commodity_type.value.map((item) => item.value);

    console.log(scopeTreeData.value, "Tree数据结构");
  });
};
getSecond();
// 表格数据
const merchantList = ref([]);
// 加载状态
const loading = ref(true);
// 显示搜索
const showSearch = ref(true);
// 选中数组
const ids = ref([]);
// 单条数据
const single = ref(true);
// 多条数据
const multiple = ref(true);
// 总条数
const total = ref(0);
// 弹窗状态
const openBase = ref(false);
const openPayment = ref(false);
const openInvoice = ref(false);
// 弹窗标题
const title = ref("");
const payTitle = ref("");
const invoiceTitle = ref("");

// 表单数据
const data = reactive({
  form: {
    id: undefined,
    merName: undefined,
    merId: undefined,
    feeUserId: undefined,
    termId: undefined,
    payAppId: undefined,
    payAppSecret: undefined,
    payPrivateKey: undefined,
    payPublicKey: undefined,
    invoiceAppKey: undefined,
    invoiceAppSecret: undefined,
    invoiceAccessToken: undefined,
    taxNum: undefined,
    extension: undefined,
    taxRate: 0.13,
    clerk: undefined,
    checker: undefined,
    bank: undefined,
    account: undefined,
    address: undefined,
    work: undefined,
    workScopeList: [],
    contentList: [],
    status: "0",
    createTime: undefined,
  },
  // 查询参数
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    merName: undefined,
    status: undefined,
  },
  // 表单校验规则
  rules: {
    // 基本信息校验规则
    base: {
      merName: [
        { required: true, message: "公司名称不能为空", trigger: "blur" },
      ],
      bank: [{ required: true, message: "开户行不能为空", trigger: "blur" }],
      account: [{ required: true, message: "卡号不能为空", trigger: "blur" }],
      work: [{ required: true, message: "作用范围不能为空", trigger: "blur" }],
    },
    // 支付配置校验规则
    payment: {
      merId: [{ required: true, message: "商户号不能为空", trigger: "blur" }],
      payAppId: [
        { required: true, message: "支付APPID不能为空", trigger: "blur" },
      ],
      payAppSecret: [
        { required: true, message: "支付APP密钥不能为空", trigger: "blur" },
      ],
      payPrivateKey: [
        { required: true, message: "支付私钥不能为空", trigger: "blur" },
      ],
      payPublicKey: [
        { required: true, message: "支付公钥不能为空", trigger: "blur" },
      ],
    },
    // 开票配置校验规则
    invoice: {
      invoiceAppKey: [
        { required: true, message: "发票APPKEY不能为空", trigger: "blur" },
      ],
      invoiceAppSecret: [
        { required: true, message: "发票APP密钥不能为空", trigger: "blur" },
      ],
      invoiceAccessToken: [
        { required: true, message: "发票accessToken不能为空", trigger: "blur" },
      ],
      taxNum: [{ required: true, message: "税号不能为空", trigger: "blur" }],
      taxRate: [{ required: true, message: "税率不能为空", trigger: "blur" }],
      contentList: [
        { required: true, message: "开票内容不能为空", trigger: "change" },
      ],
    },
  },
});

const { form, queryParams, rules } = toRefs(data);

// 当前选中的收费类型和商品编码
const currentFeeType = ref("");
const currentInvoiceGoodsCode = ref("");

// Tree树形控件相关数据
const scopeTreeRef = ref(null);
const scopeTreeData = ref([]);
const expandedKeys = ref([]);
const treeProps = {
  children: "children",
  label: "label",
  value: "value",
};

/** 查询收款单位列表 */
function getList() {
  loading.value = true;
  merchantParamConfiglist(queryParams.value).then((res) => {
    if (res.code == 200) {
      merchantList.value = res.rows;
      total.value = res.total;
      loading.value = false;
    }
  });
}

/** 取消按钮 - 基本信息 */
function cancelBase() {
  openBase.value = false;
  reset();
}

/** 取消按钮 - 支付配置 */
function cancelPayment() {
  openPayment.value = false;
  reset();
}

/** 取消按钮 - 开票配置 */
function cancelInvoice() {
  openInvoice.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    merName: undefined,
    merId: undefined,
    feeUserId: undefined,
    termId: undefined,
    payAppId: undefined,
    payAppSecret: undefined,
    payPrivateKey: undefined,
    payPublicKey: undefined,
    invoiceAppKey: undefined,
    invoiceAppSecret: undefined,
    invoiceAccessToken: undefined,
    taxNum: undefined,
    extension: undefined,
    taxRate: 0.13,
    clerk: undefined,
    checker: undefined,
    bank: undefined,
    account: undefined,
    address: undefined,
    work: undefined,
    workScopeList: [],
    contentList: [],
    status: "0",
    createTime: undefined,
  };

  // 重置Tree控件
  if (scopeTreeRef.value) {
    scopeTreeRef.value.setCheckedKeys([]);
  }

  // 重置所有表单引用
  proxy.resetForm("formBaseRef");
  proxy.resetForm("formPaymentRef");
  proxy.resetForm("formInvoiceRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 处理作用范围多选变化 */
function handleWorkScopeChange(values) {
  // 将选中的值转换为指定格式
  form.value.workScopeList = values.map((value) => {
    const dict = commodity_type.value.find((item) => item.value === value);
    return {
      categoryCode: value, // label
      categoryName: dict ? dict.label : value, // value
    };
  });
}

/** 处理作用范围Tree选中事件 */
function handleScopeTreeCheck(data, checkedInfo) {
  // 获取所有选中的节点
  const checkedNodes = checkedInfo.checkedNodes;

  // 过滤出叶子节点（二级节点）
  const leafNodes = checkedNodes.filter(
    (node) => node.leaf || !node.children || node.children.length === 0
  );
  // 转换为所需的数据结构
  form.value.workScopeList = leafNodes.map((node) => ({
    categoryCode: node.categoryCode,
    categoryName: node.categoryName,
    eventId: node.eventId,
    eventName: node.eventName,
  }));

  // 同时更新work字段，用于表单验证
  form.value.work = leafNodes.map((node) => node.id);
  console.log(form.value.workScopeList, "选中的作用范围数据");
}

/** 根据workScopeList设置Tree控件的选中状态 */
function setScopeTreeCheckedKeys() {
  if (
    scopeTreeRef.value &&
    form.value.workScopeList &&
    form.value.workScopeList.length > 0
  ) {
    // 从workScopeList中提取eventId，组合成Tree的id
    const treeIds = form.value.workScopeList.map((item) => {
      return `${item.categoryCode}-${item.eventId}`;
    });
    // 设置Tree控件的选中状态
    scopeTreeRef.value.setCheckedKeys(treeIds);
  }
}

/** 添加开票内容 */
function addInvoiceContent() {
  if (currentFeeType.value && currentInvoiceGoodsCode.value) {
    const content = `${currentInvoiceGoodsCode.value}&${currentFeeType.value}`;
    // 检查是否已存在相同内容
    if (!form.value.contentList.includes(content)) {
      form.value.contentList.push(content);
    }
    // 重置选择
    currentFeeType.value = "";
    currentInvoiceGoodsCode.value = "";
  } else {
    proxy.$modal.msgError("请同时选择收费类型和商品编码");
  }
}

/** 删除开票内容 */
function removeInvoiceContent(index) {
  form.value.contentList.splice(index, 1);
}

/** 获取开票内容显示文本 */
function getInvoiceContentDisplay(item) {
  // item格式为 "fee_type&invoice_goods_code"
  if (!item || !item.includes("&")) return item;

  const [goodsCodeValue, feeTypeValue] = item.split("&");
  // 查找对应的label
  const feeType = fee_type.value.find((dict) => dict.value == feeTypeValue);
  const goodsCode = invoice_goods_code.value.find(
    (dict) => dict.value == goodsCodeValue
  );
  const feeTypeLabel = feeType ? feeType.label : feeTypeValue;
  const goodsCodeLabel = goodsCode ? goodsCode.label : goodsCodeValue;

  return `${goodsCodeLabel}${feeTypeLabel}`;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  openBase.value = true;
  title.value = "添加收款单位";
}

/** 基本信息编辑 */
function handleUpdateBase(row) {
  reset();

  merchantParamConfigid(row.id).then((res) => {
    if (res.code == 200) {
      form.value = res.data;
      form.value.work = form.value.workScopeList.map(
        (item) => item.categoryCode
      );
      openBase.value = true;
      title.value = "修改基本信息";

      // 在下一个事件循环中设置Tree控件的选中状态
      // 确保Tree控件已经渲染完成
      setTimeout(() => {
        setScopeTreeCheckedKeys();
      }, 0);
    }
  });
}

/** 支付配置编辑 */
function handleUpdatePayment(row) {
  reset();
  merchantParamConfigid(row.id).then((res) => {
    if (res.code == 200) {
      form.value = res.data;
      form.value.work = form.value.workScopeList.map(
        (item) => item.categoryCode
      );
      openPayment.value = true;
      payTitle.value = "修改支付配置";
    }
  });
}

/** 开票配置编辑 */
function handleUpdateInvoice(row) {
  reset();
  merchantParamConfigid(row.id).then((res) => {
    if (res.code == 200) {
      form.value = res.data;
      form.value.work = form.value.workScopeList.map(
        (item) => item.categoryCode
      );
      // 如果contentList不存在，初始化为空数组
      if (!form.value.contentMapList) {
        form.value.contentMapList = [];
      }
      form.value.contentList = form.value.contentMapList.map(
        (obj) => Object.keys(obj)[0]
      );
      openInvoice.value = true;
      invoiceTitle.value = "修改开票配置";
    }
  });
}

/** 提交基本信息 */
function submitBaseForm() {
  proxy.$refs["formBaseRef"].validate((valid) => {
    if (valid) {
      if (form.value.id != undefined) {
        // 修改
        updatamerchantParamConfig(form.value).then((res) => {
          if (res.code == 200) {
            proxy.$modal.msgSuccess("修改收款单位成功");
          }
        });
      } else {
        // 新增

        addmerchantParamConfig(form.value).then((res) => {
          if (res.code == 200) {
            proxy.$modal.msgSuccess("新增收款单位成功");
          }
        });
      }
      openBase.value = false;
      getList();
    }
  });
}

/** 提交支付配置 */
function submitPaymentForm() {
  proxy.$refs["formPaymentRef"].validate((valid) => {
    if (valid) {
      // 修改
      updatamerchantParamConfig(form.value).then((res) => {
        if (res.code == 200) {
          proxy.$modal.msgSuccess("修改收款单位成功");
          openPayment.value = false;
          getList();
        }
      });
    }
  });
}

/** 提交开票配置 */
function submitInvoiceForm() {
  proxy.$refs["formInvoiceRef"].validate((valid) => {
    if (valid) {
      // 修改
      updatamerchantParamConfig(form.value).then((res) => {
        if (res.code == 200) {
          proxy.$modal.msgSuccess("修改收款单位成功");
          openInvoice.value = false;
          getList();
        }
      });
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const merchantIds = row.id || ids.value;
  proxy.$modal
    .confirm("是否确认删除该数据项？")
    .then(function () {
      return deletemerchantParamConfig(merchantIds);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}
const handleSwitchChange = (row, checked) => {
  // 先记录原始值，用于失败时回滚
  const originalValue = row.status;

  // 设置新值（前端预更新，提升体验）
  row.status = checked ? "1" : "0";
  merchantParamConfigidchangeStatus(row.id, { status: row.status })
    .then((res) => {
      if (res.code === 200) {
        proxy.$modal.msgSuccess("操作成功");
      } else {
        // 接口返回失败，回滚状态
        row.status = originalValue;
        proxy.$modal.msgError(res.msg || "操作失败");
      }
    })
    .catch((err) => {
      // 网络错误或异常，也回滚
      row.status = originalValue;
    });
};
// 初始化查询
getList();
</script>

<style scoped lang="scss">
.app-container {
  min-height: 100%;
  padding: 20px;
  background-color: #fff;
}

.mb8 {
  margin-bottom: 8px;
}

.small-padding {
  padding: 0 5px;
}

.fixed-width {
  width: 120px;
}

.action-btns {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 20px 0;
}

.invoice-content-container {
  width: 100%;

  .invoice-content-list {
    margin-top: 10px;
    width: 100%;
    min-height: 40px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    padding: 5px;
    background-color: #f5f7fa;
  }
}

.tree-container {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;

  .scope-tree {
    max-height: 300px;
    overflow-y: auto;
    padding: 10px;

    :deep(.el-tree-node__content) {
      height: 32px;
    }

    :deep(.el-tree-node__content:hover) {
      background-color: #f5f7fa;
    }

    :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
      background-color: #409eff;
      border-color: #409eff;
    }
  }

  .tree-selected-info {
    padding: 8px 15px;
    background-color: #f5f7fa;
    border-top: 1px solid #dcdfe6;
    font-size: 14px;
    color: #606266;
  }
}
</style>