<template>
<el-form ref="baseInfoFormRef" :model="form" :rules="rules" label-width="160px" :disabled="onlyShow">
  <el-row>

    <el-col :span="24">
      <el-form-item label="赛事名称" prop="competitionName">
        <el-input v-model="form.competitionName" placeholder="请输入赛事名称" maxlength="100" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="赛事届数" prop="competitionSeriesName">
        <el-input v-model="form.competitionSeriesName" placeholder="请输入赛事届数" maxlength="100"/>
      </el-form-item>
    </el-col>
     <el-col :span="12">
      <el-form-item label="赛事类型" prop="competitionType">
        <el-select v-model="form.competitionType" placeholder="请选择赛事类型">
          <el-option v-for="item in competitionTypeArr" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="赛事开始时间" prop="competitionStartTime"> 
        <el-date-picker v-model="form.competitionStartTime" 
        type="datetime" placeholder="请选择赛事开始时间" style="width: 100%;"
        value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="赛事结束时间" prop="competitionEndTime">
        <el-date-picker v-model="form.competitionEndTime" 
        type="datetime" placeholder="请选择赛事结束时间" style="width: 100%;"
        value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
    </el-col>
    <el-col :span="24">
      <el-form-item label="赛事描述" prop="competitionDesc">
        <el-input v-model="form.competitionDesc" placeholder="请输入赛事描述" type="textarea" :rows="3" />
      </el-form-item>
    </el-col>
    <el-col :span="24">
      <el-form-item label="赛事主办方" prop="organizer">
        <el-input v-model="form.organizer" placeholder="请输入赛事主办方名称" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="赛事封面" required>
        <el-upload  :action="uploadFileUrl" 
                    :before-upload="beforeUpload"
                    :file-list="form.competitionImage" :limit="1"
                    :on-exceed="handleExceed"
          :on-preview="previewurl" :on-error="handleUploadError" :on-success="handleUploadSuccess"
          :on-remove="handleRemove" :show-file-list="true" :headers="headers" class="upload-file-uploader"
          style="width: 300px;"
          ref="upload">
          <el-button type="primary">赛事封面上传</el-button>
        </el-upload>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="对外展示时间" prop="publishTime"> 
        <el-date-picker v-model="form.publishTime" 
        type="date" placeholder="请选择对外展示时间" style="width: 100%;"
        value-format="YYYY-MM-DD" />
      </el-form-item>
    </el-col>
  </el-row>
  <div class="footer" v-if="!onlyShow">
    <el-button type="primary" @click="submitForm" :loading="submitLoading">暂存</el-button>
  </div>
</el-form>
</template>

<script setup name="BaseInfoForm">
import { saveCompetitionInfo,updateCompetitionInfo } from "@/api/tournament/competition";
import { useCompetitionDetail } from "./useCompetitionDetail.js";
import { getToken } from "@/utils/auth";
import { cloneDeep } from 'lodash-es';
import { beforeUpload } from '@/utils/file'
import moment from 'moment';
import { replaceFileOrigin } from '@/utils/fileOrigin'
const props = defineProps({
  competitionTypeArr: {
    type: [Array],
    default: []
  },
  competitionId: {
    type: [Number, String],
    default: undefined
  },
  competitionSeriesId: {
    type: [Number, String],
    default: undefined  
  },
  onlyShow: {
    type: [Boolean],
    default: false
  },
})
let form = $ref({
  competitionName: '',
  competitionSeriesName: '',
  competitionType: '',
  competitionDesc: '',
  competitionStartTime: '',
  competitionEndTime: '',
  organizer: '',
  publishTime: '',
  competitionImage: []
})
// 验证规则
const rules = reactive({
  competitionName: [{ required: true, message: "赛事名称不能为空", trigger: "blur" }],
  competitionSeriesName: [{ required: true, message: "赛事届数不能为空", trigger: "blur" }],
  competitionDesc: [{ required: true, message: "赛事描述不能为空", trigger: "blur" }],
  competitionType: [{ required: true, message: "赛事类型不能为空", trigger: "change" }],
  organizer: [{ required: true, message: "主办方不能为空", trigger: "blur" }],
  publishTime: [{ required: true, message: "请选择对外展示时间", trigger: "change" }],
  competitionStartTime: [{ required: true, message: "请选择赛事开始时间", trigger: "change" }],
  competitionEndTime: [{ required: true, message: "请选择赛事结束时间", trigger: "change" }]
})


const emit = defineEmits(['changeNextTab'])
const { fetchDetail } = useCompetitionDetail() 
// 表单引用
const baseInfoFormRef = ref(null)
/** 提交表单 */
function submitForm() {
  baseInfoFormRef.value.validate((valid) => {
    if (valid) {
      // 验证封面是否上传
      if(form.competitionImage.length === 0){
        ElMessage({
          showClose: true,
          message: '请上传赛事封面',
          type: 'warning',
        })
        return
      }
      
      // 验证时间范围
      if(new Date(form.competitionStartTime) > new Date(form.competitionEndTime)){
        ElMessage({
          showClose: true,
          message: '赛事开始时间不能晚于结束时间',
          type: 'warning',
        })
        return
      }
      
      // 准备提交数据
      const sendForm = cloneDeep(form)
      
      // 处理封面图片，确保格式正确
      if(sendForm.competitionImage && sendForm.competitionImage.length > 0) {
        const imgItem = sendForm.competitionImage[0]
        sendForm.competitionImage = imgItem.url || ''
        sendForm.competitionImageName = imgItem.name || ''
      }
      
      // 提交数据
      postApi(sendForm)
    }
  })
}

const { submitForm: competitionSubmitForm, submitLoading } = useCompetitionDetail()

const postApi = async (sendForm) => {
  await competitionSubmitForm({
    data: sendForm,
    componentType: 'BaseInfo1',
    emit,
    props,
    saveApi: saveCompetitionInfo,
    updateApi: updateCompetitionInfo
  })
}



// 文件上传
const uploadFileUrl = ref(import.meta.env.VITE_APP_BASE_API + "/file/upload"); // 上传附件的服务器地址
const headers = ref({ Authorization: "Bearer " + getToken() });
function handleUploadError(err) {
  ElMessage({
    showClose: true,
    message: '上传文件失败',
    type: 'error',
  })
}
// 超出文件数量限制回调
function handleExceed(files, fileList) {
  ElMessage({
    showClose: true,
    message: '最多上传1个文件',
    type: 'warning',
  })
}
// 上传成功回调
function handleUploadSuccess(res, file) {
  res = replaceFileOrigin(res)
  console.log(res,'resres')
  // 清空现有列表，因为限制只能上传一个文件
  form.competitionImage = [];
  form.competitionImage.push({
    ...res.data
  })
}
function handleRemove() {
  form.competitionImage =  []
}
const previewurl = (row) => {
  window.open(row.url, '_blank');
}

/**
 * 获取赛事详情
 */
function getDetail(){
  if(props.competitionId && props.competitionSeriesId){
    const params = {competitionId: props.competitionId,competitionSeriesId: props.competitionSeriesId}
    fetchDetail(params,(data)=>{
      form = {
        competitionSeriesName: data.competitionSeriesName || '',
        competitionName: data.competitionName || '',
        competitionType: data.competitionType || '',
        competitionDesc: data.competitionDesc || '',
        competitionStartTime: moment(data.competitionStartTime).format('YYYY-MM-DD HH:mm:ss') || '',
        competitionEndTime: moment(data.competitionEndTime).format('YYYY-MM-DD HH:mm:ss') || '',
        organizer: data.organizer || '',
        publishTime: moment(data.publishTime).format('YYYY-MM-DD') || '',
        competitionImage: data.competitionImage ? [{
          url: data.competitionImage,
          name: data.competitionImageName || ''
        }] : [] 
      }
    }, true)
  }
}
getDetail()
</script>

<style scoped lang="scss">
.footer{
  text-align: right;
}

// 隐藏上传文件后的"按住delete可删除"提示文字
:deep(.el-icon--close-tip) {
  display: none !important;
}
</style>
