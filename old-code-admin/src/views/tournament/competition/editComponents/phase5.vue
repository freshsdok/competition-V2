<template>
<div>
  <el-form ref="fromRef" :model="form" :rules="rules" label-width="130px" :disabled="onlyShow">
    <el-row>
      <el-col :span="8">
        <el-form-item label="阶段名称" prop="stageName">
          <el-input v-model="form.stageName" placeholder="请输入阶段名称" />
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="评分方式" prop="scoreWay">
          <el-select v-model="form.scoreWay" placeholder="请选择评分方式">
            <el-option v-for="item in scoreWayArr" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="晋级人数/队伍数" prop="promoteNum">
          <el-input-number style="width: 100%;" v-model="form.promoteNum" placeholder="请输入晋级人数/队伍数" type="number"  :step="1" :min="0"  :precision="0" :max="99999999"/>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="晋级分数" prop="promoteScore">
          <el-input-number  style="width: 100%;" v-model="form.promoteScore" placeholder="请输入晋级分数" type="number"  :step="0.01" :min="0.01" :max="99999999"/>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="开始时间" prop="stageStartTime"> 
          <el-date-picker value-format="YYYY-MM-DD HH:mm:ss" v-model="form.stageStartTime" type="datetime" placeholder="请选择开始时间" style="width: 100%;" />
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="结束时间" prop="stageEndTime">
          <el-date-picker v-model="form.stageEndTime" 
                          type="datetime" 
                          placeholder="请选择结束时间"
                          value-format="YYYY-MM-DD HH:mm:ss"
                          style="width: 100%;" />
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item label="阶段描述" prop="stageDesc"> 
          <el-input v-model="form.stageDesc" placeholder="请输入阶段描述" type="textarea" :rows="3" />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
  <el-row justify="end"><el-button type="primary" plain @click="addCk" v-if="!onlyShow">确认录入</el-button> </el-row>
  <el-row style="width: 100%; margin-top: 20px;padding-left: 130px;" class="el-table-tooltip-width">
    <el-table :data="tableData" style="width: 100%;" max-height="300">
      <el-table-column type="index" label="序号" width="50px" fixed="left" />
      <el-table-column prop="stageName" label="阶段名称" width="150px" show-overflow-tooltip/>
      <el-table-column prop="scoreWay" label="评分方式" width="100px">
          <template #default="scope">
              <dict-tag :options="scoreWayArr" :value="scope.row.scoreWay" />
          </template>
      </el-table-column>
      <el-table-column prop="promoteNum" label="晋级人数/队伍数" width="120px" />
      <el-table-column prop="promoteScore" label="晋级分数" width="80px"/>
      <el-table-column prop="stageStartTime" label="开始时间" width="160px" >
        <template #default="scope">
          {{ moment(scope.row.stageStartTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column prop="stageEndTime" label="结束时间" width="160px" >
        <template #default="scope">
          {{ moment(scope.row.stageEndTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column prop="stageDesc" label="阶段描述" width="200px" show-overflow-tooltip>
      </el-table-column>
      <!-- 操作列 -->
      <el-table-column label="操作" width="100px"  fixed="right" v-if="!onlyShow">
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

<script setup name="PhaseForm">
import { useCompetitionDetail } from './useCompetitionDetail';
import { updateCompetitionInfo, saveCompetitionInfo } from '@/api/tournament/competition';
import { cloneDeep } from 'lodash'
import moment from 'moment';
const props = defineProps({
  competitionId: {
    type: [Number, String],
    default: undefined
  },
  competitionSeriesId: {
    type: [Number, String],
    default: undefined
  },
  scoreWayArr: {
    type: [Array],
    default: []
  },
  onlyShow: {
    type: Boolean,
    default: false
  },
})

let form = $ref({
  stageName: '',
  scoreWay: null,
  promoteNum: null,
  promoteScore: null,
  stageStartTime: '',
  stageEndTime: '',
  stageDesc: ''
})
// 验证规则
const baseRules = reactive({
  stageName: [{ required: true, message: "阶段名称不能为空", trigger: "blur" }],
  scoreWay: [{ required: true, message: "评分方式不能为空", trigger: "change" }],
  promoteNum: [{ required: true, message: "晋级人数/队伍数不能为空", trigger: "blur" }],
  promoteScore: [{ required: true, message: "晋级分数不能为空", trigger: "blur" }], // 评分方式
  stageStartTime: [{ required: true, message: "开始时间不能为空", trigger: "change" }],
  stageEndTime: [{ required: true, message: "结束时间不能为空", trigger: "change" }]
})

let tableData = $ref([])
/** 确认录入 */
function addCk() {
  fromRef.value.validate((valid) => {
    if (valid) {
      if(new Date(form.stageStartTime) > new Date(form.stageEndTime)){
        ElMessage({
          showClose: true,
          message: '开始时间不能大于结束时间',
          type: 'warning',
        })
        return
      }
      tableData.push(cloneDeep(form))
      form = {
        stageName: '',
        scoreWay: null,
        promoteNum: null,
        promoteScore: null,
        stageStartTime: '',
        stageEndTime: '',
        stageDesc: ''
      }
      nextTick(() => {
        setTimeout(() => {
          fromRef.value.clearValidate()
        }, 20)
      })
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
      message: '请录入阶段信息',
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
    competitionStageList: tableData
  }
  
  // 使用统一的提交方法
  await competitionSubmitForm({
    data: apiSendData,
    componentType: 'phase5',
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
      tableData = Array.isArray(data.competitionStageList) ? data.competitionStageList : []
    }, true)
  }
}
getDetail()
</script>

<style scoped lang="scss">
.footer{
  margin-top: 20px;
  text-align: right;
}
:deep(.el-table__header-wrapper){
  .el-table__header{
    width: 100%;
  }
}
</style>