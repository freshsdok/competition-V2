<template>
  <div class="base-page">
    <div class="container-custom font-sans pb-[60px]">
      <Breadcrumbar />
      <div class="bg-[#fff] pb-[65px]">
        <el-tabs v-model="activeName">
          <el-tab-pane label="赛证互通" name="赛证互通" />
        </el-tabs>
        <div class="text-center mt-[30px] mb-[50px]">
          <div class="font-bold text-[24px] mb-[20px]">证书兑换明细</div>
          <span class="text-[16px] text-[#999999]">阅读赛证互通兑换明细，选择源证书，支付后将跳转至缴费页面</span>
        </div>
        <div class="ml-[50px] mr-[50px]">
          <div class="text-[20px] mb-[20px]">申请条件说明</div>
          <div class="bg-[#F8F9FA] rounded-[10px]">
            <div class="rich-content ql-editor" v-html="ruleData.applyDesc"></div>
          </div>
          <div class="text-[20px] mt-[35px] mb-[25px]">源证书</div>
          <!-- 且关系 -->
          <vxe-table :data="ruleData.originCertList" class="w-[100%]" border :header-cell-style="{
            background: '#F9FAFB', color: '#64666A'
          }" empty-text="暂无源证书信息" v-if="ruleData?.certConditions === '1'">
            <vxe-column field="certConfigName" title="源证书名称" min-width="350" :show-overflow="true"/>
            <vxe-column field="year" title="拥有年份" width="250" />
            <vxe-column title="状态" width="300">
              <template #default="{ row }">
                <span class="text-[18px] flex items-center">
                  <el-icon :color="row.applyStatus === '1' ? '#51C512' : '#E6A23C'">
                    <SuccessFilled v-if="row.applyStatus === '1'" />
                    <CircleCloseFilled v-else />
                  </el-icon>
                  <span :class="[row.applyStatus === '1' ? 'text-[#51C512]' : 'text-[#E6A23C]', 'ml-[15px]']">{{
                    row.applyStatusDes }}</span>
                </span>
              </template>
            </vxe-column>
          </vxe-table>
          <!-- 或关系 -->
          <vxe-table :data="ruleData.originCertList" class="w-[100%]" border :header-cell-style="{
            background: '#F9FAFB', color: '#64666A'
          }" empty-text="暂无源证书信息" v-if="ruleData?.certConditions === '2'"  @radio-change="handleSelectOrginChange" :radio-config="radioConfig">
            <vxe-column type="radio" width="60"></vxe-column>
            <vxe-column field="certConfigName" title="源证书名称" min-width="350" :show-overflow="true"/>
            <vxe-column field="year" title="拥有年份" width="250" />
            <vxe-column title="状态" width="300">
              <template #default="{ row }">
                <span class="text-[18px] flex items-center">
                  <el-icon :color="row.applyStatus === '1' ? '#51C512' : '#E6A23C'">
                    <SuccessFilled v-if="row.applyStatus === '1'" />
                    <CircleCloseFilled v-else />
                  </el-icon>
                  <span :class="[row.applyStatus === '1' ? 'text-[#51C512]' : 'text-[#E6A23C]', 'ml-[15px]']">{{
                    row.applyStatusDes }}</span>
                </span>
              </template>
            </vxe-column>
          </vxe-table>
          <div class="text-[20px] mt-[35px] mb-[25px]">目标证书</div>
          <vxe-table :data="ruleData.targetCertList" class="w-[100%]" border empty-text="暂无目标证书信息" :show-header="false"
            @radio-change="handleSelectChange" ref="tableRef">
            <vxe-column type="radio" width="60" v-if="ruleData.targetCertList.length > 1"></vxe-column>
            <vxe-column field="certConfigName" title="目标证书名称" min-width="350" :show-overflow="true"/>
            <vxe-column title="状态" width="300">
              <template #default="{ row }">
                <span class="text-[18px]">
                  <!-- TODO 这里之后添加是否已兑换状态 已兑换显示 证书已兑换 不显示价格 如果未兑换显示价格 -->
                  <span class="flex items-center">
                    <span class="text-[#333333]">所需费用（元）:</span>
                    <span class="text-[#FF4444] text-[24px] font-bold ml-[10px]" v-if="row.newCertTag">
                      {{ row.certAmount ?? '-'}}
                    </span>
                    <span  class="text-[24px] font-bold ml-[10px]" v-else>-</span>
                  </span>
                </span>
              </template>
            </vxe-column>
          </vxe-table>
          <div class="flex items-center  mb-[25px] mt-[35px]">
            <div class="text-[20px] mr-[10px]">总费用</div> <span class="text-[#FF4848] text-[28px]">￥{{ certAmountSum
            }}</span>
          </div>
          <div class="flex items-center justify-center mt-[50px]">
            <el-button @click="checkCanPay" size="default" type="primary" :disabled="isPay">立即支付</el-button>
          </div>
        </div>
      </div>
    </div>
    <!-- 支付确认界面 -->
    <el-dialog v-model="open" title="证书互换申请" width="500">
      <div class="p-[15px] bg-[#F9FAFB] border-l-[#3169F8] border-l-[5px]">
        您正在申请将 <span class="font-bold text-[#E6A23C]" v-for="item in selectOriginCert">【{{ item.certConfigName
          }}】</span>
        兑换为
        <span class="font-bold text-[#333]" v-for="item in selectTargetCert">【{{ item.certConfigName }}】</span>
      </div>
      <div class="flex items-center justify-center mt-[50px]">
        <span class="text-[#999999]">需支付金额：</span>
        <span class="text-[#FF4848] text-[24px]">￥{{ certAmountSum }}</span>
      </div>
      <template #footer>
        <div class="text-center flex flex-col items-center justify-center">
          <el-button @click="handlePay" size="default" type="primary" :disabled="disabled"
            :loading="disabled">立即支付</el-button>
          <span class="text-[#999999] mt-[10px]">支付成功后，即完成证书的互换申请！ </span>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
  // ********** 插件 ***********
  import { useRoute, useRouter } from 'vue-router'
  const route = useRoute()
  const router = useRouter();

  // ********** 组件 ***********
  import Breadcrumbar from "@/components/breadcrumbar.vue";

  // *********** API ***********
  import { getCertInterconnectRule, getCertInterconnectAmount, orderCertInterconnect,saveUserCertExchangeApplyCheck } from "@/api/certInterconnect/index.js"
  import Modal from '@/plugins/modal.js'
  // ********* 初始化 **********
  const activeName = ref('赛证互通')
  const ruleData = ref({
    certConditions: null,
    originCertList: [],
    targetCertList: []
  }) // 证书互通规则详细信息
  const disabled = ref(false) // 下单按钮是否禁用
  const radioVal = ref() // 或关系选择的源证书值
  const selectOriginCert = ref([]) // 选择的源证书
  const selectTargetCert = ref([]) // 选择的目标证书
  const open = ref(false) // 是否打开弹窗
  const certAmountSum = ref(0) // 总价
  const tableRef = ref()

  //  ****** computed ************
  const ruleId = computed(() => route?.params?.ruleId) // 获取页面 ruleId
  // 支付按钮是否禁用:
  // 1. 当 certConditions === '1' 且关系时
  //    - 目标证书未选择时禁用
  //    - 或源证书中存在 applyStatus !== '1' 时禁用
  // 2. 当 certConditions === '2' 或关系时
  //    - 源证书或目标证书未选择时禁用
  // 3. 总价格等于 0 时禁用
  // TODO : 4. 选择的目标证书如果已兑换 支付按钮不可点击
  const isPay = computed(() => {
    const condition = ruleData.value?.certConditions
    const hasOrigin = selectOriginCert.value.length > 0
    const hasTarget = selectTargetCert.value.length > 0
    const allOriginOk = hasOrigin && selectOriginCert.value.every(row => row.applyStatus === '1')
    if (condition === '1') {
      return !hasTarget || !allOriginOk || certAmountSum.value === 0
    }
    if (condition === '2') {
      return !hasOrigin || !hasTarget || certAmountSum.value === 0
    }
    return certAmountSum.value === 0
  })

  // ******** 业务 ***********
  // 获取详情
  const getRuleData = async () => {
    const { data } = await getCertInterconnectRule(ruleId.value)
    ruleData.value = data;
    const { originCertList, targetCertList } = ruleData.value
    // 且关系时将源证书数据赋值给已选择的源证书数据
    if (ruleData.value.certConditions === '1') selectOriginCert.value = originCertList;
    // 目标证书仅有一条 时直接赋值给已选择的目标证书
    if (targetCertList.length === 1) {
      selectTargetCert.value = targetCertList
      // 当且关系时直接计算价格
      if (ruleData.value.certConditions === '1') componentAmount()
    }
  }

  // 或关系 单选源证书
  const handleChange = (val) => {
    selectOriginCert.value = ruleData.value.originCertList.filter(item => item.certConfigId === val)
    if (selectTargetCert.value && selectTargetCert.value.length > 0) {
      componentAmount()
    }
  }

  // 目标证书表格单选
  const handleSelectChange = ({ row }) => {
    if (row) {
      selectTargetCert.value = [row];
      if (selectOriginCert.value && selectOriginCert.value.length > 0) {
        componentAmount()
      }
    } else {
      certAmountSum.value = 0;
      selectTargetCert.value = []
    }
  }
  // 源证书表格单选
  const handleSelectOrginChange = ({ row }) => {
    if (row) {
      selectOriginCert.value = [row];
      if (selectTargetCert.value && selectTargetCert.value.length > 0) {
        componentAmount()
      }
    } else {
      certAmountSum.value = 0;
      selectOriginCert.value = []
    }
  }

  // 源证书单选按钮配置
  const radioConfig = {
    checkMethod: ({ row }) => {
      return row.applyStatus === '1'
    }
  }

  // 计算价格
  const componentAmount = async () => {
    const { data } = await getCertInterconnectAmount({
      ruleId: ruleId.value,
      originCertList: selectOriginCert.value,
      targetCertList: selectTargetCert.value
    })
    certAmountSum.value = data.certAmountSum;

    data?.targetCertList.forEach(item => {
      ruleData.value?.targetCertList.forEach(row => {
        if (row.certConfigId === item.certConfigId) {
          row.certAmount = item.certAmount
          row.newCertTag = true
        }else{
          row.newCertTag = false
        }
      })
    })
  }

  const checkCanPay = async () => {
    saveUserCertExchangeApplyCheck ({      
      ruleId: ruleId.value,
      originCertList: selectOriginCert.value,
      targetCertList: selectTargetCert.value,
      repayAmount: certAmountSum.value 
    }).then(res => {
      const data = res.data
      if (data?.success) {
        open.value = true
      }else {
        Modal.msgWarning(res?.data?.msg || '校验证书费用失败，请稍后重试')
      }
    }).catch(err => {
      console.log(err,'xxxxx')
    })
  }

  // 支付
  const handlePay = async () => {
    try {
      disabled.value = true;
      const { id } = await orderCertInterconnect({
        ruleId: ruleId.value,
        originCertList: selectOriginCert.value,
        targetCertList: selectTargetCert.value,
        repayAmount: certAmountSum.value
      })
      router.push({
        path: "/personal/paymentrecords/payment",
        query: {
          id: id,
        },
      });
      disabled.value = false;
    } catch (error) {
      console.log(error);
    }
  }

  onMounted(() => {
    getRuleData()
  })

</script>

<style lang="scss" scoped>
  :deep(.el-tabs__nav .el-tabs__item) {
    margin: 15px;
    font-size: 20px;
  }

  :deep(.el-button) {
    background: #3169F8 !important;
  }

  :deep(.el-button.is-disabled) {
    opacity: 0.5;
  }
</style>