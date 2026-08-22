
<template>
  <div class="" style="padding: 16px;">
    <el-form ref="noticeInfoRef" :model="form" :rules="rules" label-width="120px" :disabled="disp">
        <el-row>
          <el-col :span="12">
            <el-form-item label="通知公告标题" prop="noticeTitle">
              <el-input v-model="form.noticeTitle" placeholder="请输入通知公告标题" maxlength="200" show-word-limit/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发布人" prop="noticeAuthor">
              <el-input v-model="form.noticeAuthor" placeholder="请输入发布人" maxlength="100"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="通知公告类型" prop="noticeType">
              <el-select v-model="form.noticeType" placeholder="请选择通知公告类型" clearable style="width: 100%;">
                <el-option
                  v-for="dict in notice_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="通知公告摘要" prop="noticeAbstract">
          <el-input 
            v-model="form.noticeAbstract" 
            type="textarea" 
            :rows="3"
            placeholder="请输入通知公告摘要" 
            maxlength="500" 
            show-word-limit/>
        </el-form-item>
        <el-form-item label="通知公告封面" prop="noticeImage">
          <el-upload  
            :action="uploadFileUrl" 
            :file-list="noticeImageList" 
            :limit="1"
            :on-exceed="handleExceed"
            :on-preview="previewurl" 
            :on-error="handleUploadError" 
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove" 
            :show-file-list="true" 
            :headers="headers" 
            class="upload-file-uploader"
            list-type="picture-card"
            ref="upload">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="通知公告内容" prop="noticeContent">
          <editor v-model="form.noticeContent" :min-height="300" :disp="disp" readOnly/>
        </el-form-item>
      </el-form>
  </div>
</template>
<script setup>
import { useDict } from "@/utils/dict";
import Editor from '@/components/Editor'
const props = defineProps({
  form: {
    type: Object,
    default: () => {},
  },
  disp:{
    type: Boolean,
    default: () => false,
  }
});
const { sys_notice_type, sys_notice_status } = useDict(
  "sys_notice_type",
  "sys_notice_status"
);
const { competition_status, notice_type } = useDict('competition_status', 'notice_type')
const { form } = toRefs(props);
const noticeImage = form?.value?.noticeImage || ''
let noticeImageList = ref([])
if(noticeImage){
  noticeImageList.value = [
    {
      url: noticeImage,
    }
  ]
}
</script>

<style scoped>
</style>