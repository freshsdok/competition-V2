<template>
  <el-dialog
    title="提示语配置"
    v-model="visible"
    width="1000px"
    append-to-body
    :close-on-click-modal="false"
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="签到成功提示语配置" name="success">
        <el-form
          ref="successFormRef"
          :model="successForm"
          :rules="successRules"
          label-width="140px"
        >
          <!-- <el-form-item label="标题" prop="successHintTitle">
            <el-input
              v-model="successForm.successHintTitle"
              placeholder="请输入标题"
            />
          </el-form-item>
          <el-form-item label="签到成功提示语" prop="successHintDesc">
            <el-input
              v-model="successForm.successHintDesc"
              type="textarea"
              :rows="3"
              placeholder="请输入签到成功提示语"
            />
          </el-form-item> -->
          <el-form-item label="考场规则" prop="examinationHallRuler">
            <Editor
              v-model="successForm.examinationHallRuler"
              :min-height="150"
            />
          </el-form-item>
          <el-form-item label="考生承诺" prop="examinationHallPromise">
            <Editor
              v-model="successForm.examinationHallPromise"
              :min-height="150"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="非正常提示语配置" name="abnormal">
        <el-form
          ref="abnormalFormRef"
          :model="abnormalForm"
          :rules="abnormalRules"
          label-width="140px"
        >
          <el-form-item label="标题" prop="improperTitle">
            <el-input
              v-model="abnormalForm.improperTitle"
              placeholder="请输入标题"
            />
          </el-form-item>
          <el-form-item label="非正常提示语配置" prop="improperDesc">
            <el-input
              v-model="abnormalForm.improperDesc"
              type="textarea"
              :rows="5"
              placeholder="请输入非正常提示语"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">保存配置</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { saveUserGroupSetting } from '@/api/tournament/signInQrCode'
import modal from '@/plugins/modal'
import Editor from '@/components/Editor'

const emit = defineEmits(['success'])

const visible = ref(false)
const activeTab = ref('success')
const currentRow = ref(null)

// 签到成功表单
const successFormRef = ref(null)
const successForm = ref({
  successHintTitle: '',
  successHintDesc: '',
  examinationHallRuler: '',
  examinationHallPromise: ''
})

// 自定义验证函数 - 用于富文本编辑器
const validateEditorContent = (rule, value, callback) => {
  // 去除HTML标签后判断是否为空
  const textContent = value ? value.replace(/<[^>]+>/g, '').trim() : ''
  if (!textContent) {
    callback(new Error(rule.message || '请输入内容'))
  } else {
    callback()
  }
}

const successRules = {
  // successHintTitle: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  // successHintDesc: [{ required: true, message: '请输入签到成功提示语', trigger: 'blur' }],
  examinationHallRuler: [{ required: true, validator: validateEditorContent, message: '请输入考场规则', trigger: 'change' }],
  examinationHallPromise: [{ required: true, validator: validateEditorContent, message: '请输入考生承诺', trigger: 'change' }]
}

// 非正常提示语表单
const abnormalFormRef = ref(null)
const abnormalForm = ref({
  improperTitle: '',
  improperDesc: ''
})
const abnormalRules = {
  improperTitle: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  improperDesc: [{ required: true, message: '请输入非正常提示语', trigger: 'blur' }]
}

/** 打开弹框 - 直接使用列表传入的数据 */
function openDialog(row) {
  currentRow.value = row
  visible.value = true
  activeTab.value = 'success'
  nextTick(() => {
    // 直接使用列表的字段初始化
    successForm.value = {
      successHintTitle: row.successHintTitle || '',
      successHintDesc: row.successHintDesc || '',
      examinationHallRuler: row.examinationHallRuler || '',
      examinationHallPromise: row.examinationHallPromise || ''
    }
    abnormalForm.value = {
      improperTitle: row.improperTitle || '',
      improperDesc: row.improperDesc || ''
    }
  })
}

/** 取消 */
function cancel() {
  visible.value = false
}

/** 检查另一个Tab是否有未填写的字段 */
function checkOtherTabEmpty() {
  if (activeTab.value === 'success') {
    // 当前在签到成功Tab，检查非正常提示语是否填写完整
    if (!abnormalForm.value.improperTitle || !abnormalForm.value.improperDesc) {
      return '非正常提示语配置有未填写的字段，请切换到该Tab完成配置'
    }
  } else {
    // 当前在非正常提示语Tab，检查签到成功提示语是否填写完整
    const textContent1 = successForm.value.examinationHallRuler ? successForm.value.examinationHallRuler.replace(/<[^>]+>/g, '').trim() : ''
    const textContent2 = successForm.value.examinationHallPromise ? successForm.value.examinationHallPromise.replace(/<[^>]+>/g, '').trim() : ''
    if (!textContent1 || 
        !textContent2) {
      return '签到成功提示语配置有未填写的字段，请切换到该Tab完成配置'
    }
  }
  return null
}

/** 提交表单 - 两个Tab一起提交 */
function submitForm() {
  // 先验证当前Tab
  const currentFormRef = activeTab.value === 'success' ? successFormRef : abnormalFormRef
  currentFormRef.value.validate(valid => {
    if (!valid) return
    
    // 检查另一个Tab是否填写完整
    const otherTabError = checkOtherTabEmpty()
    if (otherTabError) {
      modal.msgWarning(otherTabError)
      return
    }
    
    // 验证另一个Tab的表单
    const otherFormRef = activeTab.value === 'success' ? abnormalFormRef : successFormRef
    otherFormRef.value.validate(otherValid => {
      if (!otherValid) {
        // 另一个Tab验证失败，自动切换到那个Tab
        activeTab.value = activeTab.value === 'success' ? 'abnormal' : 'success'
        return
      }
      
      // 两个Tab都验证通过，一起提交
      const data = {
        codeConfigId: currentRow.value.codeConfigId,
        // 签到成功提示语配置
        successHintTitle: successForm.value.successHintTitle,
        successHintDesc: successForm.value.successHintDesc,
        examinationHallRuler: successForm.value.examinationHallRuler,
        examinationHallPromise: successForm.value.examinationHallPromise,
        // 非正常提示语配置
        improperTitle: abnormalForm.value.improperTitle,
        improperDesc: abnormalForm.value.improperDesc,
        msgFlag:true
      }
      
      saveUserGroupSetting(data).then(response => {
        if (response.code === 200) {
          modal.msgSuccess('保存成功')
          visible.value = false
          emit('success')
        } else {
          modal.msgWarning(response.msg || '保存失败')
        }
      })
    })
  })
}

defineExpose({
  openDialog
})
</script>

<style scoped lang="scss">
:deep(.el-tabs__nav-wrap) {
  padding: 0 20px;
}
</style>
