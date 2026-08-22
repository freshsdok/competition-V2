<template>
<div>
  <el-form ref="fromRef" :model="form" :rules="rules" label-width="140px" :disabled="onlyShow">
  <el-row>
    <el-col :span="12">
      <el-form-item label="赞助企业" prop="enterpriseId">
        <el-select
          v-model="form.enterpriseId"
          filterable
          remote
          reserve-keyword
          placeholder="输入企业名称搜索，选择赞助企业"
          :remote-method="remoteMethod"
          :loading="epLoading"
          remote-show-suffix
          style="width: 100%"
        >
          <el-option
            v-for="item in epOptions"
            :key="item.enterpriseId"
            :label="item.enterpriseName"
            :value="item.enterpriseId"
          />
        </el-select>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="赞助金额（元）" prop="spopAmount">
        <el-input-number v-model="form.spopAmount" placeholder="请输入赞助金额" type="number"  style="width: 100%;"  :step="0.01" :min="0" :max="99999999"/>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="赞助开始日期" prop="coptStartTime"> 
        <el-date-picker v-model="form.coptStartTime" 
        type="date" placeholder="请选择赞助开始日期" style="width: 100%;"
        value-format="YYYY-MM-DD" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="赞助结束日期" prop="coptEndTime">
        <el-date-picker v-model="form.coptEndTime" 
        type="date" placeholder="请选择赞助结束日期" style="width: 100%;"
        value-format="YYYY-MM-DD" />
      </el-form-item>
    </el-col>
  </el-row>
  </el-form>
  <el-row justify="end"><el-button type="primary" plain @click="addCk" v-if="!onlyShow">确认录入</el-button></el-row>
  <el-row style="width: 100%; margin-top: 20px;padding-left: 130px;" class="el-table-tooltip-width">
    <el-table :data="tableData" style="width: 100%" max-height="300">
      <el-table-column type="index" label="序号" width="50px" fixed="left" />
      <el-table-column label="赞助企业" prop="enterpriseId" show-overflow-tooltip>
        <template #default="scope">
          {{getEnterpriseName(scope.row.enterpriseName,scope.row.enterpriseId)}}  
        </template>
      </el-table-column>
      <el-table-column label="赞助金额（元）" prop="spopAmount" >
        <template #default="scope">
          {{scope.row.spopAmount || '-'}}
        </template>
      </el-table-column>
      <el-table-column label="赞助开始日期" prop="coptStartTime" >
        <template #default="scope">
          {{scope.row.coptStartTime || '-'}}
        </template>
      </el-table-column>
      <el-table-column label="赞助结束日期" prop="coptEndTime"  >
        <template #default="scope">
          {{scope.row.coptEndTime || '-'}}
        </template>
      </el-table-column>
      <!-- 操作列 -->
      <el-table-column label="操作" width="120" fixed="right" v-if="!onlyShow">
        <template #default="scope">
          <el-button type="danger" size="mini" @click="deleteRow(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-row>

</div>
</template>

<script setup name="company8">
import { useCompetitionDetail } from './useCompetitionDetail';
import { ElMessage } from 'element-plus';
import { sponsoringEnterpriseList} from "@/api/tournament/sponsoringEnterprise"
import { watchEffect } from 'vue';

const props = defineProps({
  onlyShow: {
    type: Boolean,
    default: false
  }
})

// 表单数据
let form = $ref({
  enterpriseId: null,
  spopAmount: null,
  coptStartTime: null,
  coptEndTime: null,
})

// 验证规则
let baseRules = $ref({
  enterpriseId: [{ required: true, message: "请选择赞助企业", trigger: "blur" }]
})

// 已录入的赞助企业列表
let tableData = $ref([])

// 获取赞助企业名称
const getEnterpriseName = (enterpriseName,enterpriseId) => {
  if(enterpriseName){
    return enterpriseName
  }
  return epOptions.find(item => item.enterpriseId == enterpriseId)?.enterpriseName || ''
}

// 确认录入
const addCk = () => {
  fromRef.value.validate((valid) => {
    if (valid) {
      if(tableData.find(item => item.enterpriseId == form.enterpriseId)){
        ElMessage({
          showClose: true,
          message: '不能重复录入赞助企业，请重新选择',
          type: 'warning',
        })
        return
      }
      if((form.coptStartTime && form.coptEndTime) && form.coptStartTime > form.coptEndTime){
        // 开始时间不能大于结束时间
        ElMessage({
          showClose: true,
          message: '赞助开始日期不能大于赞助结束日期',
          type: 'warning',
        })
        return
      }
      tableData.push({
        enterpriseId: form.enterpriseId,
        spopAmount: form.spopAmount,
        coptStartTime: form.coptStartTime,
        coptEndTime: form.coptEndTime,
      })
      // 重置表单
      Object.assign(form, {
        enterpriseId: null,
        spopAmount: null,
        coptStartTime: null,
        coptEndTime: null,
      })
      // 清空验证
      fromRef.value.clearValidate()
    }
  })
}

// 删除行
const deleteRow = (index) => {
  tableData.splice(index, 1);
}

// 不再需要changeNextTab事件，因为现在使用统一的暂存功能
const emit = defineEmits([])

// 表单引用
let fromRef = ref(null)

// 使用赛事详情hooks
const {currentTabDetail} = useCompetitionDetail();

// 验证规则（响应式）
let rules = $ref(baseRules)





// 获取表单数据，包含验证逻辑
const getFormData = async () => {
  // 定义验证结果和数据
  let isValid = true
  let formData = {
    competitionTrackEnterpriseList: [...tableData]
  }
  
  // 进行表单验证
  if (!tableData || tableData.length === 0) {
    isValid = false
    ElMessage({
      showClose: true,
      message: '请录入赞助企业信息',
      type: 'warning',
    })
  }
  
  return {
    valid: isValid,
    data: formData
  }
}

watchEffect(() => {
  if (currentTabDetail.value) {
    tableData = currentTabDetail.value.competitionTrackEnterpriseList || [];
  }
});

// 赞助企业选择相关
let epLoading = $ref(false)
let epOptions = $ref([])

// 远程搜索赞助企业
const remoteMethod = (query, first) => {
  if (query || first) {
    epLoading = true
    epOptions = []
    sponsoringEnterpriseList({pageNum: 1,pageSize: 100},{enterpriseName: query}).then(res => {
      if(res.code === 200){
        let rows = res.rows;
        // 使用reduce进行数组合并和去重
        const tempMap = [...epOptions, ...rows].reduce((map, item) => {
          if (!map.has(item.enterpriseId)) {
            map.set(item.enterpriseId, item);
          }
          return map;
        }, new Map());
        // 转换Map值为数组
        epOptions = Array.from(tempMap.values());
        epLoading = false
      }else{
        epLoading = false
      }
    }).catch(err => {
      epLoading = false
    })
  }
}

// 初始加载赞助企业列表
remoteMethod('', true)

// 暴露方法给父组件
defineExpose({
  getFormData
})
</script>

<style scoped lang="scss">
.footer{
  margin-top: 20px;
  text-align: right;
}
</style>