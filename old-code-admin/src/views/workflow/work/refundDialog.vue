<template>
  <div>
    <el-descriptions title="原订单" border :column="3" size="small" label-width="82px" class="uniform-descriptions" >
      <el-descriptions-item label="大赛名称">{{payOrder.commodityName}}</el-descriptions-item>
      <el-descriptions-item label="收款公司">{{payOrder.merName}}</el-descriptions-item>
      <el-descriptions-item label="购买人">{{payOrder.userName}}</el-descriptions-item>
      <el-descriptions-item label="联系方式">{{payOrder.phoneNumber}}</el-descriptions-item>
      <el-descriptions-item label="所在学校">{{payOrder.schoolName}}</el-descriptions-item>
      <el-descriptions-item label="交易流水号">{{payOrder.cmbOrderId}}</el-descriptions-item>
      <el-descriptions-item label="订单号">{{payOrder.id}}</el-descriptions-item>
      <el-descriptions-item label="金额(元)">{{payOrder.amount}}</el-descriptions-item>
      <el-descriptions-item label="状态"><dict-tag :options="payStatusDict" :value="payOrder.payStatus" /></el-descriptions-item>
      <el-descriptions-item label="支付方式"> <dict-tag :options="payMethodDict" :value="payOrder.payMethod" /></el-descriptions-item>
      <el-descriptions-item label="在线支付方式"><dict-tag :options="payModeDict" :value="payOrder.payMode" /></el-descriptions-item>
      <el-descriptions-item label="支付时间">
        <span style="white-space: normal">
          <template v-if="payOrder.payMethod == 'offline'">
            {{ (payOrder.payStatus == "paid" && payOrder.payTime) || "-" }}
          </template>
          <template v-else>
            {{ payOrder.payTime || "-" }}
          </template>
        </span>
      </el-descriptions-item>
      <el-descriptions-item label="开票状态">{{ payOrder.invoiceStatus == 1 ? "已开票" : "未开票" }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">  <span style="white-space: normal"> {{ payOrder.createTime }}</span></el-descriptions-item>
    </el-descriptions>
    <el-descriptions style="margin: 20px 0 0px;" title="退款订单" border :column="3" size="small" label-width="82px" class="uniform-descriptions" >
      <el-descriptions-item label="大赛名称">{{refundOrder.commodityName}}</el-descriptions-item>
      <el-descriptions-item label="收款公司">{{refundOrder.merName}}</el-descriptions-item>
      <el-descriptions-item label="申请人">{{refundOrder.userName}}</el-descriptions-item>
      <el-descriptions-item label="联系方式">{{refundOrder.phoneNumber}}</el-descriptions-item>
      <el-descriptions-item label="所在学校">{{refundOrder.schoolName}}</el-descriptions-item>
      <el-descriptions-item label="订单号">{{refundOrder.id}}</el-descriptions-item>
      <el-descriptions-item label="金额(元)">{{refundOrder.amount}}</el-descriptions-item>
      <el-descriptions-item label="状态"><dict-tag :options="payStatusDict" :value="refundOrder.payStatus" /></el-descriptions-item>
      <el-descriptions-item label="支付方式"> <dict-tag :options="payMethodDict" :value="refundOrder.payMethod" /></el-descriptions-item>
      <el-descriptions-item label="创建时间">  <span style="white-space: normal"> {{ refundOrder.createTime }}</span></el-descriptions-item>  

    </el-descriptions>
    <el-table v-if="refundOrder.teamInfoList" 
              :data="JSON.parse(refundOrder.teamInfoList||'[]')" 
              border 
              style="margin: -1px 0 20px;"
              header-row-class-name="table-header-color">
      <el-table-column label="团队编号" align="left" prop="teamCode" min-width="200"></el-table-column>
      <el-table-column label="团队名称" align="left" prop="teamName" min-width="200"></el-table-column>
      <el-table-column label="赛道/组别" align="left" prop="commodityName" min-width="200">
        <template #default="scope">
          <span style="color: #ff8800">{{
            scope.row.competitionTrackName
          }}</span>
          <span>-</span>
          <span style="color: #51c512">{{ scope.row.secondLevelName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="队员" align="left" prop="commodityName" min-width="200">
        <template #default="scope">
          <span
            class="text-[#666666] font-[400]"
            v-for="(item, index) in  scope.row.playersList"
            :key="index"
          >
            {{ item.userName }}（{{ item.idCard }}）
              <span v-if="item.delFlag!=0" class='status-deleted'>（已删除）</span><br />
          </span>
        </template>
      </el-table-column>
      <el-table-column label="指导教师" align="left" prop="commodityName" min-width="200">
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
  </div>
</template>

<script setup>
// 定义组件属性
const props = defineProps({
  payOrder: {
    type: Object,
    default: () => ({})
  },
  refundOrder: {
    type: Object,
    default: () => ({})
  },
  payStatusDict: {
    type: Array,
    default: () => []
  },
  payMethodDict: {
    type: Array,
    default: () => []
  },
  payModeDict: {
    type: Array,
    default: () => []
  },
})

</script>

<style scoped lang="scss">
:deep(.el-descriptions) {
  .el-descriptions__table{
    width: 100%;
    .el-descriptions__content{
      max-width: 0;
    }
  }
  .el-descriptions__label{
    padding: 2px 4px !important;
  }
}
.form-title{
  color: #303133;
  font-size: 16px;
  font-weight: bold;
  margin: 20px 0 10px;
}
.footer{ 
  text-align: center;
}
:deep(.table-header-color){
  .el-table__cell{
    background-color: #f5f7fa !important;
  }
}
  .status-deleted {
    color: #f56c6c;
  }
</style>