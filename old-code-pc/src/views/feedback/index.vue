<template>
<div class="feedback">
  <div class="container-custom">
    <div class="feedback-title">意见反馈</div>
    <el-form  :model="form"
              label-width="0"
              ref="formRef"
              :rules="currentRules">
      <div class="feedback-item-des">
        <span class="feedback-item-des-num">1.</span>
        <span class="required">*</span>
        <span>反馈类型</span>
      </div>
      <el-form-item prop="type" class="feedback-item-mrbtm"> 
        <el-radio-group v-model="form.type" size="large" fill="#6cf">
          <el-radio-button v-for="item in sugg_back_type"
                           :key="item.value"
                           class="feedback-radio-btn"
                           :label="item.label"
                           :value="item.value" />
        </el-radio-group>
      </el-form-item>
      <div class="feedback-item-des">
        <span class="feedback-item-des-num">2.</span>
        <span class="required">*</span>
        <span>Hi，请输入您的问题反馈或建议吧～</span>
      </div>
      <el-form-item prop="title"  class="feedback-item-mrbtm">
        <el-input
            v-model="form.title"
            placeholder="请描述使用问题，某个功能无法使用/不好用、流程受阻、页面卡顿、或其他产品建议（5个字以上）"
            maxlength="2000"
            style="width: 100%;"
            :autosize="{ minRows: 8, maxRows: 16 }"
            type="textarea"
            show-word-limit
          />
      </el-form-item>
      <div class="feedback-item-des">
        <span class="feedback-item-des-num">3.</span>
        <span>上传“有效文件”，可以让问题</span>
        <span class="feedback-item-des-highlight">优先被发现</span>
        <span>哦!</span>
        <span class="feedback-item-des-gray">（最多可以上传3个文件，每个文件大小不超过5MB）</span>
      </div>
      <el-form-item class="feedback-item-mrbtm">
       <el-upload class="upload-demo"
                  drag
                  ref="uploadRef"
                  :limit="3"
                  :action="upload.url"
                  :headers="upload.headers"
                  :before-upload="beforeUpload"
                  :on-error="handleFileError"
                  :on-exceed="handleExceed"
                  :on-success="onUploadSuccess"
                  :on-preview="handlePreview"
                   :show-file-list="false"
                  multiple
                  v-model:file-list="form.fileList"
                >
          <el-icon class="el-icon--upload"  size="30"><Upload /></el-icon>
          <div class="el-upload__text">可在此处粘贴、拖拽、上传多个文件</div>
        </el-upload>
      </el-form-item>
      <div class="feedback-item-des">
        <span class="feedback-item-des-num">4.</span>
        <span >联系电话</span>
        <span class="feedback-item-des-gray">（解决过程中，方便我们与您联系）</span>
      </div>
      <el-form-item prop="phone"  class="feedback-item-mrbtm">
        <el-input
            v-model="form.phone"
            placeholder="请输入您的手机号码"
            maxlength="11"
            class="feedback-input"
          />
      </el-form-item>
      <el-form-item>
          <el-button type="primary"
                      @click="onSubmit"
                      class="feedback-submit-btn hvr-grow-shadow">提交反馈</el-button>
        </el-form-item>
    </el-form>
  </div>
</div>
</template>

<script setup>
import { getToken } from "@/utils/auth";
import Modal from "@/plugins/modal.js";
import { useDict } from '@/utils/dict'
import { nextTick } from "vue";
const { sugg_back_type} = useDict('sugg_back_type')
let currentRules = $ref({
  type: [
    { required: true, message: '请选择反馈类型', trigger: 'blur' },
  ],
  title: [
    { required: true, message: '请输入您的问题反馈或建议', trigger: 'blur' },
    { min: 5, message: '请输入5个字以上', trigger: 'blur' }
  ],
  phone: [
    {
      required: false,
      message: '请输入您的手机号码',
      trigger: 'blur'
    },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确格式的手机号码',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        // 如果为空，直接通过验证
        if (!value || value === '') {
          callback();
        } else {
          // 如果不为空，验证格式
          const phoneRegex = /^1[3-9]\d{9}$/;
          if (phoneRegex.test(value)) {
            callback();
          } else {
            callback(new Error('请输入正确格式的手机号码'));
          }
        }
      }
    }
  ],
})

let form = $ref({
  type: '',
  title: '',
  fileList: [],
  phone: '',
})

let formRef = ref(null)
// 提交反馈
const onSubmit = async () => {
  console.log(form,'.........................')
  formRef.value?.validate((valid) => {
    console.log('valid', valid)
    if (valid) {
      // Modal.loading("正在提交反馈...");
    } else {
      toTop()
    }
  });
}
const uploadRef = ref(null);
const upload = reactive({
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url: import.meta.env.VITE_APP_BASE_API + "/file/upload",
});
/** 文件上传失败 */
const handleFileError = (error, file, fileList) => {
  Modal.notifyError("上传失败");
};
const onUploadSuccess = (response, file) => {
  if (response.code == 200) {
    uploadRef.value.handleRemove(file);
    nextTick(() => {
      setTimeout(() => {
        form.fileList.push(response.data);
        console.log(form.fileList)
      }, 500);
    })
    Modal.notifySuccess("上传成功");
  } else {
    Modal.notifyError(response.msg);
  }
};
// ====== 超出文件数量限制时触发 ======

const handleExceed = () => {
  Modal.msgError(`上传文件数量不能超过3个!`);
};
// ====== 点击文件列表移除文件 ======
const handlePreview = (file) => {
  window.open(file.url);
};
const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 5
  // 检查文件大小
  if (!isLt10M) {
    Modal.msgError('上传图片大小不能超过 10MB!')
    return false
  }
  return true
}
const toTop = () => {
 try {
    window.scrollTo({
      top: 100,
      behavior: 'smooth'
    });
  } catch (e) {
    // 如果不支持options参数，回退到传统方式
    window.scrollTo(0, 0);
  }
}
</script>

<style scoped lang="scss">
.feedback{
  width: 100%;
  height: 100%;
  .feedback-title{
    margin: 80px 0 30px;
    width: 100%;
    color: #111;
    font-size: 26px;
    font-weight: 500;
    text-align: center;
    letter-spacing: 2px;
  }
  .required{
    color: #f56c6c;
  }
  .feedback-item-des-highlight{
    color: #3169f8;
  }
  .feedback-item-des-gray{
    color: #999999;
    font-size: 16px;
  }
  .feedback-item-des-num{
    font-size: 22px;
    font-weight: 500;
    margin-right: 8px;
  }
  .feedback-item-des{
    font-size: 20px;
    margin-bottom: 12px;
  }
  .feedback-submit-btn{
    background: linear-gradient(#3169f8 0%, #33dbdb 100%);
    min-width: 120px;
    height: 40px;
    border-radius: 10px;
    font-weight: bold;
    font-size: 18px;
    color: #ffffff;
    margin-right: 20px;
    padding-left: 16px;
    padding-right: 16px;
  }
  .feedback-input{
    width: 400px;
  }
  .upload-demo{
    width: 400px;
  }
  .feedback-item-mrbtm{
    margin-bottom: 50px;
  }
  .feedback-radio-btn{

  }
}
:deep(.el-icon--close-tip){
  display: none !important;
}
:deep(.el-radio-group){
  .is-active{
    .el-radio-button__inner{
      background-color: #3169f8 !important;
    }
  }
  .el-radio-button__inner{
    &:hover{
      color: #ffffff !important;
      background-color: #3169f8 !important;
    }
  }
}
</style>