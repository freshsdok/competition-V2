<template>
<div>
  <el-form ref="fromRef" :model="form" :rules="rules" label-width="130px" :disabled="onlyShow || isViewing">
    <el-row class="w-full-editor">
      <el-col :span="24">
        <el-form-item label="展示标题" prop="modelName">
          <el-input v-model="form.modelName"  style="width: 100%;" placeholder="请输入展示标题(最多16个字符)" maxlength="16"/>
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item label="展示内容" prop="modelContent"> 
          <editor v-model="form.modelContent" :min-height="300" :readOnly="onlyShow || isViewing" v-if="showViewEditor"/>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
  <el-row justify="end">
    <!-- 编辑模式显示取消编辑 -->
    <el-button plain @click="cancelEdit" v-if="isEditing">取消编辑</el-button>
    <!-- 查看模式显示取消查看 -->
    <el-button type="primary" plain @click="cancelView" v-if="isViewing">取消查看</el-button>
    <!-- 编辑模式显示编辑按钮 -->
    <el-button type="primary" plain @click="editData" v-if="isEditing">保存编辑</el-button>
    <!-- 非只读非编辑非查看模式显示确认录入 -->
    <el-button type="primary" plain @click="addCk" v-if="!onlyShow && !isEditing && !isViewing">确认录入</el-button>
  </el-row>
  <el-row style="width: 100%; margin-top: 20px;padding-left: 130px;">
    <el-table :data="tableData" style="width: 100%" max-height="300">
      <el-table-column prop="modelName" label="展示标题" min-width="120" />
      <!-- <el-table-column prop="modelContent" label="展示内容"  show-overflow-tooltip/> -->
       <el-table-column prop="modelContent" label="展示内容" > 
        <template #default="scope">
          <div class="show-content">{{ scope.row.modelContent }}</div>
        </template>
       </el-table-column>
      <!-- 操作列 -->
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="scope">
          <!-- 所有模式都可以查看详情 -->
          <el-button type="primary" size="mini" @click="viewDetail(scope.row)">详情</el-button>
          <!-- 非只读模式可以编辑和删除 -->
          <template v-if="!onlyShow">
            <el-button type="success" size="mini" @click="editRow(scope.row, scope.$index)">编辑</el-button>
            <el-button type="danger" size="mini" @click="deleteRow(scope.$index)">删除</el-button>
          </template>
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
import { reactive } from 'vue'

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
  awardsNameArr: {
    type: [Array],
    default: []
  },
})

let form = $ref({
  modelName: '',
  modelContent: undefined,
})

// 编辑状态相关变量
let isEditing = $ref(false)
let editingIndex = $ref(-1)
let isViewing = $ref(false)
let originalForm = $ref({})

// 验证规则
const baseRules = reactive({
  modelName: [{ required: true, message: "展示标题不能为空", trigger: "blur" }],
  modelContent: [{ required: true, message: "展示内容不能为空", trigger: "blur" }],
})

let tableData = $ref([])

/** 确认录入 */
function addCk() {
  fromRef.value.validate((valid) => {
    if (valid) {
      tableData.push(cloneDeep(form))
      // 重置表单状态
      form = {
        modelName: '',
        modelContent: undefined,
      }
    }
  })
}

/** 编辑行 */
function editRow(row, index) {
  // 重置所有状态
  isViewing = false
  isEditing = true
  editingIndex = index
  originalForm = cloneDeep(form) // 保存当前表单状态，用于取消编辑
  form = cloneDeep(row) // 将选中行数据赋值给表单
}

/** 执行编辑操作 */
function editData() {
  fromRef.value.validate((valid) => {
    if (valid) {
      tableData[editingIndex] = cloneDeep(form)
      // 重置编辑状态
      isEditing = false
      editingIndex = -1
      form = {
        modelName: '',
        modelContent: undefined,
      }
    }
  })
}

/** 重置表单状态 */
let showViewEditor = $ref(true)
function resetFormState() {
  try {
    isEditing = false
    isViewing = false
    editingIndex = -1
    form = {
      modelName: '',
      modelContent: undefined,
    }
    setTimeout(() => {
      nextTick(() => {
        showViewEditor = true
      })    
    }, 50)
  } catch (error) {}
}

/** 取消编辑 */
function cancelEdit() {
  resetFormState()
}

/** 查看详情 */
function viewDetail(row) {
  // 重置所有状态
  resetFormState()
  isViewing = true
  originalForm = cloneDeep(form) // 保存当前表单状态
  form = cloneDeep(row) // 将选中行数据赋值给表单
}

/** 取消查看 */
function cancelView() {
  resetFormState()
}

/** 删除行 */
function deleteRow(index) {
  // 如果正在编辑的是要删除的行，重置表单状态
  if (isEditing && editingIndex === index) {
    resetFormState()
  }
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
      message: '请录入展示信息',
      type: 'warning',
    })
    return
  }
  postApi()
}

// 使用统一的提交方法
const { submitForm: competitionSubmitForm, submitLoading } = useCompetitionDetail()

const postApi = async () => {
  // 确保tableData是数组格式
  const dataToSend = Array.isArray(tableData) ? tableData : [tableData];
  
  // 准备提交数据
  const apiSendData = {
    competitionExtension: JSON.stringify(cloneDeep(dataToSend))
  }
  
  // 使用统一的提交方法
  await competitionSubmitForm({
    data: apiSendData,
    componentType: 'showContent9',
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
      try{
        // 解析赛事扩展信息
        const parsedData = JSON.parse(data.competitionExtension || '[]')
        tableData = Array.isArray(parsedData) ? parsedData : []
      } catch (err) {
        console.error('解析赛事扩展信息失败:', err)
        tableData = []
      }
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
.show-content{
  // 一行换行
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  word-break: break-all;
}
</style>