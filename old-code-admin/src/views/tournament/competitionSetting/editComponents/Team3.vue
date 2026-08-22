<template>
<el-form ref="fromRef" :model="form" :rules="rules" label-width="180px" :disabled="onlyShow">
  <el-row>
    <el-col :span="12">
      <el-form-item label="参赛方式" prop="joinType">
        <el-select v-model="form.joinType" placeholder="请选择参赛方式" @change="updateRules">
          <el-option v-for="item in joinTypeArr" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="是否必须含有指导教师" prop="isTeacherNess"> 
        <el-radio-group v-model="form.isTeacherNess" @change="updateRules">
          <el-radio label="1">是</el-radio>
          <el-radio label="2">否</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="指导教师人数下限" prop="minTeacherNum">
        <el-input-number v-model="form.minTeacherNum" placeholder="请输入指导教师人数下限" type="number"  style="width: 100%;"  :step="1" :min="0" :precision="0" :max="99999999"/>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="指导教师人数上限" prop="maxTeacherNum">
        <el-input-number v-model="form.maxTeacherNum" placeholder="请输入指导教师人数上限" type="number"  style="width: 100%;"  :step="1" :min="1" :precision="0" :max="99999999"/> 
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="最少组队人数" prop="minPernNum">
        <el-input-number v-model="form.minPernNum" placeholder="请输入最少组队人数" type="number" style="width: 100%;" :step="1" :min="1" :precision="0" :max="99999999"/>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="最多组队人数" prop="maxPernNum">
        <el-input-number v-model="form.maxPernNum" placeholder="请输入最多组队人数" type="number"  style="width: 100%;" :step="1" :min="1" :precision="0" :max="99999999"/> 
      </el-form-item>
    </el-col>
    <el-col :span="24">
      <el-form-item label="组队规则说明" prop="teamRule">
        <el-input v-model="form.teamRule" placeholder="请输入组队规则说明" type="textarea" :rows="4" />
      </el-form-item>
    </el-col>
  </el-row>
  <!--  -->
</el-form>
</template>

<script setup name="Team3Form">
import { useCompetitionDetail } from './useCompetitionDetail';
import { ElMessage } from 'element-plus';
import { watchEffect } from 'vue';

const props = defineProps({
  joinTypeArr: {
    type: [Array],
    default: []
  },
  onlyShow: {
    type: Boolean,
    default: false
  }
})

// 不再需要changeNextTab事件，因为现在使用统一的暂存功能
const emit = defineEmits([])

///** 使用赛事详情hooks */
const { currentTabConfig } = useCompetitionDetail();

// 表单数据
let form = $ref({
  joinType: '',
  isTeacherNess: '2',
  minTeacherNum: null,
  maxTeacherNum: null,
  minPernNum: null,
  maxPernNum: null,
  teamRule: ''
})

// 动态验证规则生成函数
function generateRules() {
  const newRules = {
    joinType: [{ required: true, message: "参赛方式不能为空", trigger: "blur" }],
    isTeacherNess: [{ required: true, message: "请选择是否必须含有指导教师", trigger: "change" }],
    // 指导教师人数仅在isTeacherNess为1（是）时必填
    minTeacherNum: form.isTeacherNess === '1' ? 
      [{ required: true, message: "指导教师人数下限不能为空", trigger: "blur" }] : [],
    maxTeacherNum: form.isTeacherNess === '1' ? 
      [{ required: true, message: "指导教师人数上限不能为空", trigger: "blur" }] : [],
    // 组队人数仅在joinType为2（团体赛）时必填
    minPernNum: form.joinType === '2' ? 
      [{ required: true, message: "最少组队人数不能为空", trigger: "blur" }] : [],
    maxPernNum: form.joinType === '2' ? 
      [{ required: true, message: "最多组队人数不能为空", trigger: "blur" }] : [],
    // 组队规则说明永远不必填
    teamRule: []
  }
  return newRules
}

// 验证规则（响应式）
const rules = $ref(generateRules())

// 表单引用
let fromRef = ref(null)

// 更新验证规则的函数
const updateRules = () => {
  Object.assign(rules, generateRules())
  // 如果表单已经被验证过，重新验证以显示最新的必填提示
  if (fromRef.value) {
    fromRef.value.clearValidate()
  }
}
// 加载状态已移除，因为现在使用统一的暂存功能
// 提交表单和API调用方法已移除，因为现在使用统一的暂存功能

/** 获取表单数据，包含验证逻辑 */
const getFormData = async () => {
  // 更新验证规则
  Object.assign(rules, generateRules())
  
  // 定义表单数据
  let formData = { ...form }
  
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
  
  return {
    valid: isValid,
    data: formData
  }
}

// 监听当前标签页配置变化，更新表单数据
const updateFormFromConfig = () => {
  if (currentTabConfig.value) {
    Object.assign(form, {
      joinType: currentTabConfig.value.joinType || '',
      isTeacherNess: currentTabConfig.value.isTeacherNess || '2',
      minTeacherNum: currentTabConfig.value.minTeacherNum ?? null,
      maxTeacherNum: currentTabConfig.value.maxTeacherNum || null,
      minPernNum: currentTabConfig.value.minPernNum || null,
      maxPernNum: currentTabConfig.value.maxPernNum || null,
      teamRule: currentTabConfig.value.teamRule || ''
    })
  }
}

// 使用watchEffect监听currentTabConfig变化
watchEffect(() => {
  updateFormFromConfig()
})

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
