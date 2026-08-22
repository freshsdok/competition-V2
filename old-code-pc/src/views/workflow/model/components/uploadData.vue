<template>
    <div>
        <!--  &lt;!&ndash; bpmn20.xml导入对话框 &ndash;&gt;-->
  <!--  <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body @close="cancel('uploadForm')">-->
  <!--    <el-upload-->
  <!--      ref="uploadRef"-->
  <!--      :limit="1"-->
  <!--      accept=".xml"-->
  <!--      :headers="upload.headers"-->
  <!--      :action="upload.url + '?name=' + upload.name+'&category='+ upload.category"-->
  <!--      :disabled="upload.isUploading"-->
  <!--      :on-progress="handleFileUploadProgress"-->
  <!--      :on-success="handleFileSuccess"-->
  <!--      :auto-upload="false"-->
  <!--      drag-->
  <!--    >-->
  <!--      <i class="el-icon-upload"></i>-->
  <!--      <div class="el-upload__text">-->
  <!--        将文件拖到此处，或-->
  <!--        <em>点击上传</em>-->
  <!--      </div>-->
  <!--      <div class="el-upload__tip" slot="tip">-->
  <!--        <el-form ref="uploadForm" :model="upload"  :rules="rules" label-width="80px">-->
  <!--          <el-form-item label="流程名称" prop="name">-->
  <!--            <el-input v-model="upload.name" clearable/>-->
  <!--          </el-form-item>-->
  <!--          <el-form-item label="流程分类" prop="category">-->
  <!--            <el-select v-model="upload.category" placeholder="请选择" clearable style="width:100%">-->
  <!--              <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName"-->
  <!--                         :value="item.code"/>-->
  <!--            </el-select>-->
  <!--          </el-form-item>-->
  <!--        </el-form>-->
  <!--      </div>-->
  <!--      <div class="el-upload__tip" style="color:red" slot="tip">提示：仅允许导入“bpmn20.xml”格式文件！</div>-->
  <!--    </el-upload>-->
  <!--    <div slot="footer" class="dialog-footer">-->
  <!--      <el-button type="primary" @click="submitFileForm">确 定</el-button>-->
  <!--      <el-button @click="cancel('uploadForm')">取 消</el-button>-->
  <!--    </div>-->
  <!--  </el-dialog>-->
    </div>
</template>
<script setup>
import { getToken } from "@/utils/auth";
// bpmn.xml 导入e
const uploadRef = ref(null)
const uploadFormRef = ref(null)
const upload = reactive({
  // 是否显示弹出层（xml导入）
  open: false,
  // 弹出层标题（xml导入）
  title: "",
  // 是否禁用上传
  isUploading: false,
  name: null,
  category: null,
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url: import.meta.env.VUE_APP_BASE_API + "/workflow/definition/import"
})

/** 导入bpmn.xml文件 */
function handleImport() {
  upload.title = "bpmn20.xml文件导入";
  upload.open = true;
}

// 文件上传中处理
function handleFileUploadProgress(event, file, fileList) {
  upload.isUploading = true;
}

// 文件上传成功处理
function handleFileSuccess(response, file, fileList) {
  upload.open = false;
  upload.isUploading = false;
  uploadRef.value.clearFiles();
  ElMessage.success(response.msg);
  getList();
}

// 提交上传文件
function submitFileForm() {
  uploadFormRef.value.validate(valid => {
    if (valid) {
      uploadRef.value.submit();
    }
  });
}
</script>