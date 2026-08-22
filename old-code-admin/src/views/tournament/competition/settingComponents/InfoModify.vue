<template>
<el-form ref="baseInfoFormRef" :model="form" :rules="rules" label-position="right" label-width="150px" :disabled="onlyShow">
  <el-row gutter="25" >
    <el-col :span="24"><div class="mon-title">基本配置</div></el-col>
    <el-col :span="11">
      <el-form-item label="次数限制" prop="maxTimes">
        <el-input-number v-model="form.maxTimes" placeholder="不填写或者-1表示不限制次数" type="number" style="width: 100%;" :step="1" :min="-1" :precision="0"/>
      </el-form-item>
    </el-col>
    <el-col :span="13">
      <el-form-item label="可操作参赛身份" prop="allowedUserTypes">
        <el-select v-model="form.allowedUserTypes" 
                   placeholder="请选择可操作参赛身份（可多选）" 
                   multiple
                  filterable
                  clearable>
          <el-option v-for="item in competitionAlloweUserType" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </el-col>
    <!-- 参赛信息修改范围 - 仅在参赛信息修改(operationType=1)时显示 -->
    <div v-if="operationType == '1'" class="modifyContainer">
      <el-col :span="24"><div class="mon-title">参赛信息修改范围<span class="required">*</span></div></el-col>
      <el-form-item prop="modifyScope" style="width: 100%;">
        <el-checkbox-group v-model="form.modifyScope" @change="handleModifyScopeChange">
          <el-checkbox value="1" >
            <span class="checkbox-title">允许修改所有参赛信息</span>
            <span class="sub-title">（队员信息、指导教师信息均可修改）</span>
          </el-checkbox>
          <el-checkbox value="2" :disabled="form?.modifyScope?.includes('1')">
            <span class="checkbox-title">只允许调整学生顺序</span>
            <span class="sub-title">（仅可调整队员的排列顺序，不可修改任何信息内容）</span>
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </div>
   <el-col :span="24"><div class="mon-title">时间配置</div></el-col>
    <el-col :span="24">
      <el-form-item label="允许时间区间" style="align-items: flex-start;" prop="allowedTimeRanges">
        <div>
          <template v-if="form.allowedTimeRanges && form.allowedTimeRanges.length > 0">
            <div v-for="(item, index) in form.allowedTimeRanges" 
                  :key="index" 
                  style="display: flex;margin-bottom: 10px;">
                <el-date-picker v-model="item.start" 
                                  type="datetime" placeholder="请选择开始时间" 
                                  style="width: 100%;min-width: 250px;"
                                  value-format="YYYY-MM-DD HH:mm:ss" />
                  <div style="margin: 0 10px;">至</div>
                  <el-date-picker v-model="item.end" 
                                    type="datetime" placeholder="请选择结束时间" 
                                    style="width: 100%;min-width: 250px;"
                                    value-format="YYYY-MM-DD HH:mm:ss" 
                                    :default-time="new Date('1970-01-01 23:59:59')"/>
                  <el-button type="danger" link icon="el-icon-delete" @click="removeTimeRange(index)" style="margin-left: 10px;"></el-button>
            </div>
          </template>
          <div>
            <el-button type="primary" icon="el-icon-plus" @click="addTimeRange">添加时间区间</el-button>
          </div>
        </div>
      </el-form-item>
    </el-col>  
    <el-col :span="24"><div class="mon-title">提示信息配置</div></el-col>
    <el-col :span="24">
      <el-form-item label="附件" style="align-items: flex-start;" prop="attachments">
        <FileUpload
          v-model="form.attachments"
          :limit="5"
          :oss-config="{ bizSign: 'race', bizCode: competitionSeriesId }"
          style="width: 100%"
        />
      </el-form-item>
    </el-col>
    <el-col :span="24">
      <el-form-item label="页面提示文字" prop="hintText1" style="align-items: flex-start;"> 
        <editor v-model="form.hintText1" :min-height="150" :readOnly="onlyShow" />
      </el-form-item>
    </el-col>
    <el-col :span="11">
      <el-form-item label="强制阅读时长(秒)" prop="forceReadSeconds">
        <el-input-number v-model="form.forceReadSeconds" placeholder="请输入强制阅读时长（秒）"
                         type="number" 
                         style="width: 100%;" :step="1" :min="1" :precision="0"
                         @input="updateRules"/>
      </el-form-item>
    </el-col>
    <el-col :span="24">
      <el-form-item label="提交时弹窗确认文字" prop="hintText2" style="align-items: flex-start;"> 
        <editor v-model="form.hintText2" :min-height="150" :readOnly="onlyShow" />
      </el-form-item>
    </el-col>
  </el-row>
  <div class="footer" v-if="!onlyShow">
    <el-button type="primary" @click="submitForm" :loading="submitLoading">保存配置</el-button>
  </div>
</el-form>
</template>

<script setup name="InfoModify">
import { cloneDeep } from 'lodash-es';
const props = defineProps({
  competitionAlloweUserType: {
    type: Array,
    default: () => []
  },
  competitionSeriesId: {
    type: [String, Number],
    default: ''
  },
  onlyShow: {
    type: [Boolean],
    default: false
  },
  operationType: {
    type: String,
    default: ''
  }
})
const form = defineModel('form', {
  default: () => ({})
});
const submitLoading = defineModel('submitLoading', {
  default: () => false
})
const baseInfoFormRef = ref(null)
// 动态验证规则生成函数
function generateRules() {
  return {
    allowedUserTypes: [{ required: true, message: "请选择可操作参赛身份", trigger: "blur" }],
    modifyScope: props.operationType == '1' ? [{ required: true, message: "请选择参赛信息修改范围", trigger: "change" }] : [],
    allowedTimeRanges: [{ required: true, message: "请添加允许时间区间", trigger: "blur" }],
    hintText2: !!form.value.forceReadSeconds && form.value.forceReadSeconds > 0 ? 
      [{
        validator: (rule, value, callback) => {
          if (!value || value.trim() === '' || value.trim() === '<p><br></p>' || value.trim() === '<p></p>') {
            callback(new Error('请输入提交时弹窗确认文字'));
          } else {
            callback();
          }
        },
        trigger: "blur"
      }] : []
  };
}
// 验证规则（响应式）
let rules = $ref(generateRules())
// 更新验证规则
const updateRules = () => {
  Object.assign(rules, generateRules())
  // 如果表单已经被验证过，重新验证以显示最新的必填提示
  if (baseInfoFormRef.value) {
    baseInfoFormRef.value.clearValidate()
  }
}

/** 添加时间范围 */
/** 处理参赛信息修改范围变化 */
function handleModifyScopeChange(val) {
  // 如果选中了 1（允许修改所有），则必须同时选中 2（只允许调整学生顺序）
  if (Array.isArray(val) && val.includes('1') && !val.includes('2')) {
    form.value.modifyScope = ['1', '2']
  }
}

/** 添加时间范围 */
function addTimeRange() {
  if(!form.value.allowedTimeRanges){
    form.value.allowedTimeRanges = []
  }
  form.value.allowedTimeRanges.push({start: '', end: ''})
}
/** 删除时间范围 */
function removeTimeRange(index) {
  form.value.allowedTimeRanges.splice(index, 1)
}

/** 提交表单 */
const emit = defineEmits(['submitForm'])
function submitForm() {
  // 更新验证规则
  Object.assign(rules, generateRules())
  if (!baseInfoFormRef.value) { return}
  // 手动触发表单验证
  baseInfoFormRef.value.validate((valid, fields) => {
    if (valid) {
      // 准备提交数据
      let sendForm = cloneDeep(form.value)
      if(sendForm.allowedTimeRanges){
        // 验证时间区间是否有重叠
        sendForm.allowedTimeRanges.forEach((item, index) => {
          if(!item.start || !item.end){
            ElMessage.error(`允许时间区间：第${index + 1}个时间区间的开始时间或结束时间未选择`)
            throw new Error(`允许时间区间：第${index + 1}个时间区间的开始时间或结束时间未选择`)
          }
        })
        // 验证时间区间是否有重叠
        sendForm.allowedTimeRanges.forEach((item, index) => {
          if(item.start > item.end){
            ElMessage.error(`允许时间区间：第${index + 1}个时间区间的开始时间不能晚于结束时间`)
            throw new Error(`允许时间区间：第${index + 1}个时间区间的开始时间不能晚于结束时间`)
          }
        })
      }
      // 处理 modifyScope：如果数组包含 '1'，则传 1；如果只包含 '2'，则传 2
      let modifyScopeValue = ''
      if (Array.isArray(sendForm.modifyScope) && sendForm.modifyScope.length > 0) {
        if (sendForm.modifyScope.includes('1')) {
          modifyScopeValue = '1'
        } else if (sendForm.modifyScope.includes('2')) {
          modifyScopeValue = '2'
        }
      }
      sendForm = {
        ...sendForm,
        hintText2: (sendForm?.hintText2 ==  '<p><br></p>'  || sendForm?.hintText2 ==  '<p></p>')? '' : sendForm.hintText2,
        allowedUserTypes: sendForm?.allowedUserTypes?.join(',') || '',
        allowedTimeRanges: sendForm?.allowedTimeRanges ? JSON.stringify(sendForm.allowedTimeRanges) : '',
        modifyScope: modifyScopeValue
      }
      emit('submitForm', sendForm)
    }
  })
}
</script>

<style scoped lang="scss">
.footer{
  text-align: right;
}
// 隐藏上传文件后的"按住delete可删除"提示文字
:deep(.el-icon--close-tip) {
  display: none !important;
}
.mon-title {
  font-weight: 600;
  margin-bottom: 10px;
  position: relative;
  background: #F5F5F5;
  padding: 8px 15px;
  &::after {
    content: '';
    display: block;
    width: 4px;
    height: 50%;
    position: absolute;
    top: 25%;
    left: 6px;
    border-radius: 3px;
    background-color:#409EFF;
  }
}
.modifyContainer{
  width: 100%;
  padding-bottom: 20px;
  :deep(.el-checkbox-group){
    flex-direction: column;
    align-items: flex-start;
    font-size: 16px;
    display: flex;
    padding-bottom: 10px;
    .radio-title{
      font-weight: 600;
    }
    .sub-title{
      font-size: 12px;
    }
    .el-checkbox{
      + .el-checkbox{
        margin-left: 6px;
        padding-left: 2em;
        border-left: 1px dashed #DCDFE6;
      }
    }
  }
}
.required{
  color: #F56C6C;
  margin-left: 4px;
}
</style>