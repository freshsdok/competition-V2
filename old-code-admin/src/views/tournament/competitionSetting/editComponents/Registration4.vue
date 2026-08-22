<template>
<el-form ref="fromRef" :model="form" :rules="rules" label-width="180px" :disabled="onlyShow">
  <el-row>
    <el-col :span="12">
      <el-form-item label="报名开始时间" prop="applyStartTime"> 
        <el-date-picker v-model="form.applyStartTime" 
        type="datetime" placeholder="请选择报名开始时间" style="width: 100%;"
        value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="报名结束时间" prop="applyEndTime">
        <el-date-picker v-model="form.applyEndTime" 
        type="datetime" placeholder="请选择报名结束时间" style="width: 100%;" 
        value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="每人报名费用（元）" prop="fee">
        <el-input-number v-model="form.fee" placeholder="请输入每人报名费用" type="number"  style="width: 100%;"  :step="0.01" :min="0" :max="99999999"/>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="是否必须为外籍学生" prop="isNationalityStudent">
        <el-radio-group v-model="form.isNationalityStudent">
          <el-radio label="1">是</el-radio>
          <el-radio label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="是否必须实名认证" prop="isRealNameAuth">
        <el-radio-group v-model="form.isRealNameAuth">
          <el-radio label="1">是</el-radio>
          <el-radio label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="是否必须学生身份" prop="isStudent"> 
        <el-radio-group v-model="form.isStudent" @change="handleIsStudentChange">
          <el-radio label="1">是</el-radio>
          <el-radio label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="年级要求" prop="classRequest">
        <el-select v-model="form.classRequest" 
                   placeholder="请选择年级要求" 
                   multiple
                  filterable
                  clearable
                  collapse-tags>
          <el-option v-for="item in classRequestArr" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="最低GPA要求" prop="lowestGpa">
        <el-input-number v-model="form.lowestGpa" placeholder="请输入最低GPA要求" type="number" style="width: 100%;" :step="1" :min="0" :precision="2" :max="99999999" /> 
      </el-form-item>
    </el-col>
   <el-col :span="24">
      <el-form-item label="专业要求" prop="professionRequest">
        <el-select v-model="form.professionRequest" 
                  placeholder="请选择专业要求"  
                  multiple
                  filterable
                  clearable
                  remote
                  :remote-method="remoteMethod">
          <el-option v-for="item in professionalRequirementsArr" :key="item.minorClass" :label="item.minorClass" :value="item.minorClass" />
        </el-select>
      </el-form-item>
    </el-col>
  </el-row>

</el-form>
</template>

<script setup name="Registration4Form">
import { useCompetitionDetail } from './useCompetitionDetail';
import { cloneDeep } from 'lodash-es';
import { watchEffect } from 'vue';
import { listDisciplineCategory } from '@/api/system/post';
const props = defineProps({
  classRequestArr: {
    type: [Array],
    default: []
  },
  onlyShow: {
    type: Boolean,
    default: false
  }
})

// 表单数据
let form = $ref({
  isRealNameAuth: '', // 是否必须实名认证，默认值设为2（否）
  isStudent: '', // 是否必须学生身份，默认值设为2（否）
  isNationalityStudent: '', // 是否必须为外籍学生，默认值设为2（否）
  classRequest: [], // 年级要求
  professionRequest: [], // 专业要求
  lowestGpa: null,  // 最低GPA要求
  fee: null, // 每人报名费用
  applyStartTime: '', // 报名开始时间
  applyEndTime: '', // 报名结束时间
})


// 生成验证规则函数
const generateRules = () => {
  const newRules = {
    applyStartTime: [{ required: true, message: "报名开始时间不能为空", trigger: "change" }],
    applyEndTime: [{ required: true, message: "报名结束时间不能为空", trigger: "change" }],
    isRealNameAuth: [{ required: true, message: "是否必须实名认证不能为空", trigger: "change" }],
    isStudent: [{ required: true, message: "是否必须学生身份不能为空", trigger: "change" }],
    isNationalityStudent: [{ required: true, message: "是否必须为外籍学生不能为空", trigger: "change" }],
    fee: [{ required: true, message: "每人报名费用不能为空", trigger: "blur" }]
  }
  return newRules
}

let professionalRequirementsArr = $ref([])
// 专业要求远程搜索方法
const remoteMethod = (queryString, callback) => {
  listDisciplineCategory({
    minorClass: queryString
  }).then(res => {
    if (res.code === 200) {
      professionalRequirementsArr = res.data || []
    } else {
      ElMessage.error(res.msg || '查询专业失败')
    }
  })
}

// 使用赛事详情hooks
const {currentTabConfig } = useCompetitionDetail();

// 表单引用
let fromRef = ref(null) // 表单引用

// 验证规则
const rules = $ref(generateRules())

// 更新验证规则，只更新必要的规则，避免影响其他字段
const updateRules = () => {
  Object.assign(rules, generateRules())
  // 如果表单已经被验证过，重新验证以显示最新的必填提示
  if (fromRef.value) {
    fromRef.value.clearValidate()
  }
}

// 处理是否学生身份变化
const handleIsStudentChange = (val) =>{
  if(val == '2'){
    // 非学生身份，清空相关字段
    form.classRequest = []
    form.professionRequest = []
    form.lowestGpa = null
  }
  updateRules()
  setTimeout(() => {
    nextTick(() => {
      fromRef.value.clearValidate()
    })
  }, 50)
}

/** 获取表单数据，包含验证逻辑 */
const getFormData = async () => {
  // 更新验证规则
  updateRules()
  
  // 定义表单数据
  let formData = cloneDeep(form)
  
  // 使用Promise包装表单验证的异步调用
  let isValid = await new Promise((resolve) => {
    if (fromRef.value) {
      fromRef.value.validate((valid) => {
        resolve(valid)
      })
    } else {
      // 如果表单引用不存在，默认验证通过
      resolve(true)
    }
  })
  formData = {
    ...formData,
    classRequest: formData.classRequest && formData.classRequest.length ? formData.classRequest.join(',') : '',
    professionRequest: formData.professionRequest && formData.professionRequest.length ? formData.professionRequest.join(',') : ''
  }
  return {
    valid: isValid,
    data: formData
  }
}



// 监听当前标签页配置变化，更新表单数据
watchEffect(() => {
  if (currentTabConfig.value) {
    // 直接从 currentTabConfig 中获取数据
    const config = currentTabConfig.value;
    Object.assign(form, {
      isRealNameAuth: config.isRealNameAuth || '',
      isStudent: config.isStudent || '',
      isNationalityStudent: config.isNationalityStudent || '',
      classRequest: config.classRequest ? config.classRequest.split(',') : [],
      professionRequest: config.professionRequest ? config.professionRequest.split(',') : [],
      lowestGpa: config.lowestGpa || null,
      fee: config.fee || null,
      applyStartTime: config.applyStartTime || '',
      applyEndTime: config.applyEndTime || '',
    });
    console.log('Registration11111111111114Form',form)
  }
});

// 暴露方法给父组件
defineExpose({
  getFormData
})
</script>

<style scoped lang="scss">
.footer{
  text-align: right;
}
</style>