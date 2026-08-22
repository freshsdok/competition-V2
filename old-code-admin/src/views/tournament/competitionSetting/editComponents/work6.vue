<template>
<el-form ref="fromRef" :model="form" :rules="rules" label-width="180px" :disabled="onlyShow">
  <el-row>
    <el-col :span="12">
      <el-form-item label="提交方式" prop="worksSubmitWay">
        <el-select v-model="form.worksSubmitWay" placeholder="请选择提交方式" multiple>
          <el-option v-for="item in worksSubmitWayArr" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="文件格式限制" prop="worksFormat">
        <el-select v-model="form.worksFormat" placeholder="请选择文件格式限制" multiple>
          <el-option v-for="item in fileFormatRestrictionsArr" :key="item.value" :label="item.label" :value="item.value" />
        </el-select> 
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="文件大小限制（MB）" prop="worksFormatSize">
        <el-input-number v-model="form.worksFormatSize" placeholder="请输入文件大小限制（MB）" type="number"  :step="1" :min="1" style="width: 100%;" :max="99999999"/>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="提交截止时间" prop="worksSubmitDate"> 
        <el-date-picker v-model="form.worksSubmitDate" type="datetime" placeholder="请选择提交截止时间" style="width: 100%;" value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
    </el-col>
    <el-col :span="24">
      <el-form-item label="提交说明" prop="worksSubmitExplain">
        <el-input v-model="form.worksSubmitExplain" placeholder="请输入提交说明" type="textarea" :rows="4" />
      </el-form-item>
    </el-col>
  </el-row>

</el-form>
</template>

<script setup name="Work6Form">
import { useCompetitionDetail } from './useCompetitionDetail';
import { ElMessage } from 'element-plus';
import { watchEffect } from 'vue';

const props = defineProps({
  worksSubmitWayArr: {
    type: [Array],
    default: []
  },
  fileFormatRestrictionsArr: {
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
  worksSubmitWay: [], // 提交方式
  worksFormat: [], // 文件格式限制
  worksFormatSize: null, // 文件大小限制
  worksSubmitDate: '', // 提交截止时间
  worksSubmitExplain: ''  // 提交说明
})

// 验证规则（响应式）
let baseRules = $ref({
  worksSubmitWay: [{ required: true, message: "提交方式不能为空", trigger: "change" }],
  worksFormat: [{ required: true, message: "文件格式限制不能为空", trigger: "change" }],
  worksFormatSize: [  
    { required: true, message: "文件大小限制不能为空", trigger: "blur" }
  ],
  worksSubmitDate: [{ required: true, message: "提交截止时间不能为空", trigger: "change" }]
})

// 不再需要changeNextTab事件，因为现在使用统一的暂存功能
const emit = defineEmits([])

// 表单引用
let fromRef = ref(null)

// 使用赛事详情hooks
const { currentTabConfig } = useCompetitionDetail();

// 验证规则（响应式）
let rules = $ref(baseRules)

// 加载状态已移除，因为现在使用统一的暂存功能
// 提交表单和API调用方法已移除，因为现在使用统一的暂存功能

/** 获取表单数据，包含验证逻辑 */
const getFormData = async () => {
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
    formData = {
    ...formData,
    worksSubmitWay: formData.worksSubmitWay && formData.worksSubmitWay.length ? formData.worksSubmitWay.join(',') : '',
    worksFormat: formData.worksFormat && formData.worksFormat.length ? formData.worksFormat.join(',') : ''
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
      worksSubmitWay: config.worksSubmitWay ? config.worksSubmitWay.split(',') : [],
      worksFormat: config.worksFormat ? config.worksFormat.split(',') : [],
      worksFormatSize: config.worksFormatSize || null,
      worksSubmitDate: config.worksSubmitDate || '',
      worksSubmitExplain: config.worksSubmitExplain || ''
    });
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