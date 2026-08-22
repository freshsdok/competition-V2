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
  <div class="footer" v-if="!onlyShow">
    <el-button type="primary" @click="submitForm" :loading="submitLoading">暂存</el-button>
  </div>
</div>
</template>

<script setup name="Prize7Form">
import { useCompetitionDetail } from './useCompetitionDetail';
import { updateCompetitionInfo, saveCompetitionInfo } from '@/api/tournament/competition';
import { cloneDeep } from 'lodash'
import { sponsoringEnterpriseList} from "@/api/tournament/sponsoringEnterprise"
const props = defineProps({
  competitionId: {
    type: [Number, String],
    default: undefined
  },
  competitionSeriesId: {
    type: [Number, String],
    default: undefined
  },
  onlyShow: {
    type: Boolean,
    default: false
  },
})

let form = $ref({
  enterpriseId: null,
  spopAmount: null,
  coptStartTime: null,
  coptEndTime: null,
})
// 验证规则
const baseRules = reactive({
  enterpriseId: [{ required: true, message: "请选择赞助企业", trigger: "blur" }],
  // spopAmount: [{ required: true, message: "请输入赞助金额", trigger: "blur" }],
  // coptStartTime: [{ required: true, message: "请选择赞助开始日期", trigger: "blur" }],
  // coptEndTime: [{ required: true, message: "请选择赞助结束日期", trigger: "blur" }],
})

/** 获取赞助企业名称 */
function getEnterpriseName(enterpriseName,enterpriseId) {
  if(enterpriseName){
    return enterpriseName
  }
  return epOptions.find(item => item.enterpriseId == enterpriseId)?.enterpriseName || ''
}

let tableData = $ref([])
/** 确认录入 */
function addCk() {
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
      tableData.push(cloneDeep(form))
      form = {
        enterpriseId: null,
        spopAmount: null,
        coptStartTime: null,
        coptEndTime: null,
      }
    }
  })
}
/** 删除行 */
function deleteRow(index) {
  tableData.splice(index, 1);
}

const emit = defineEmits(['changeNextTab'])

// 使用赛事详情hooks
const { fetchDetail } = useCompetitionDetail();
/** 提交表单 */
// 验证规则（响应式）
let rules = $ref(baseRules)
let fromRef = ref()
function submitForm() {
  // 在提交前更新验证规则
  if(!tableData || tableData.length === 0){
    ElMessage({
      showClose: true,
      message: '请录入赞助企业信息',
      type: 'warning',
    })
    return
  }
  postApi()
}

// 使用统一的提交方法
const { submitForm: competitionSubmitForm, submitLoading } = useCompetitionDetail()

const postApi = async () => {
  // 准备提交数据
  const apiSendData = {
    competitionEnterpriseRelaList: tableData
  }
  
  // 使用统一的提交方法
  await competitionSubmitForm({
    data: apiSendData,
    componentType: 'company8',
    emit,
    props,
    saveApi: saveCompetitionInfo,
    updateApi: updateCompetitionInfo
  })
}

/**
 * 获取详情
 */
function getDetail(){
  if(props.competitionId && props.competitionSeriesId){
    const params = { 
      competitionId: props.competitionId, 
      competitionSeriesId: props.competitionSeriesId 
    }
    
    fetchDetail(params, (data) => {
      tableData = Array.isArray(data.competitionEnterpriseRelaList) ? data.competitionEnterpriseRelaList : []
    }, true)
  }
}

// 初始化获取详情
getDetail()


// 获取赞助企业列表
let epLoading = $ref(false)
let epOptions = $ref([])

/**
 * 远程搜索企业
 * @param {String} query - 搜索关键词
 * @param {Boolean} first - 是否首次加载
 */
const remoteMethod = (query = '', first = false) => {
  if (query || first) {
    epLoading = true
    
    sponsoringEnterpriseList({pageNum: 1, pageSize: 100}, {enterpriseName: query})
      .then(res => {
        if(res.code === 200){
          const rows = res.rows || []
          
          // 使用Map进行去重，保留第一个出现的元素
          const tempMap = new Map()
          rows.forEach(item => {
            if (!tempMap.has(item.enterpriseId)) {
              tempMap.set(item.enterpriseId, item)
            }
          })
          
          epOptions = Array.from(tempMap.values())
        }
      })
      .catch(err => {
        console.error('获取企业列表失败:', err)
        epOptions = []
      })
      .finally(() => {
        epLoading = false
      })
  }
}

// 首次加载企业列表
remoteMethod('', true)
</script>

<style scoped lang="scss">
.footer{
  margin-top: 20px;
  text-align: right;
}
</style>