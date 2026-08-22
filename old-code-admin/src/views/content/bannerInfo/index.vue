<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="图片名称" prop="bannerName">
        <el-input
          v-model.trim="queryParams.bannerName"
          placeholder="请输入图片名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="图片地址" prop="bannerUrl">
        <el-input
          v-model.trim="queryParams.bannerUrl"
          placeholder="请输入图片地址"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="所属模块" prop="bannerModule">
        <el-select v-model="queryParams.bannerModule" placeholder="请选择所属模块" clearable style="width: 160px;">
          <el-option
            v-for="dict in banner_module"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="标识" prop="bannerLogotype">
        <el-input
          v-model.trim="queryParams.bannerLogotype"
          placeholder="请输入标识"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="描述" prop="bannerDesc">
        <el-input
          v-model.trim="queryParams.bannerDesc"
          placeholder="请输入描述"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['content:bannerInfo:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="bannerInfoList" @selection-change="handleSelectionChange">
      <el-table-column label="序号" align="center" type="index" width="50" />
      <el-table-column label="图片所属模块" align="center" prop="bannerModule" width="150">
        <template #default="scope">
          <dict-tag :options="banner_module" :value="scope.row.bannerModule"/>
        </template>
      </el-table-column>
      <el-table-column label="图片所属模块ID" align="center" prop="bannerModule" width="150"></el-table-column>
      <el-table-column label="图片名称" align="left" prop="bannerName"  min-width="300px" show-overflow-tooltip/>
      <el-table-column label="排序" align="center" prop="sortNum" width="50" />
      <el-table-column label="图片标识" align="center" prop="bannerLogotype" width="150"/>
      <el-table-column label="图片地址" align="left" prop="bannerUrl" min-width="600px" show-overflow-tooltip/>
      <el-table-column label="图片描述" align="left" prop="bannerDesc" min-width="150px" show-overflow-tooltip/>
      <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['content:bannerInfo:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['content:bannerInfo:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改banner图管理对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="bannerInfoRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="图片地址" prop="bannerUrl">
          <!-- <el-input v-model="form.bannerUrl" placeholder="请输入地址" /> -->
          <el-upload  :action="uploadFileUrl" 
                    :file-list="competitionImage" :limit="1"
                    :on-exceed="handleExceed"
                    :before-upload="beforeUpload"
                    :on-preview="previewurl" :on-error="handleUploadError" :on-success="handleUploadSuccess"
                    :on-remove="handleRemove" :show-file-list="true" :headers="headers" class="upload-file-uploader"
                    style="width: 300px;"
                    ref="upload">
                    <el-button type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="名称" prop="bannerName">
          <el-input v-model="form.bannerName" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="所属模块" prop="bannerModule">
          <el-select v-model="form.bannerModule" placeholder="请选择所属模块">
            <el-option
              v-for="dict in banner_module"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标识" prop="bannerLogotype">
          <el-input v-model="form.bannerLogotype" placeholder="请输入标识" />
        </el-form-item>
        <el-form-item label="描述" prop="bannerDesc">
          <el-input v-model="form.bannerDesc" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sortNum">
          <el-input v-model.number="form.sortNum" placeholder="请输入排序号" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BannerInfo">
import { listBannerInfo, getBannerInfo, delBannerInfo, addBannerInfo, updateBannerInfo } from "@/api/content/bannerInfo"
import { getToken } from "@/utils/auth";
import { useDict } from "@/utils/dict";
import { download } from "@/utils/request";
import modal from "@/plugins/modal";
import { resetForm } from "@/utils/ruoyi";
import { beforeUpload } from '@/utils/file'
import { replaceFileOrigin } from '@/utils/fileOrigin'
const { banner_module } = useDict('banner_module')
const queryRef = ref(null)
const bannerInfoRef = ref(null)

const bannerInfoList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
    form: {
      id: null,
      bannerUrl: null,
      bannerDesc: null,
      sortNum: null,
    },
  queryParams: {
      pageNum: 1,
      pageSize: 10,
      bannerName: null,
      bannerUrl: null,
      bannerModule: null,
      bannerLogotype: null,
      bannerDesc: null
    },
  rules: {
      bannerUrl: [
        { required: true, message: "地址不能为空", trigger: "blur" }
      ]
    }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询banner图管理列表 */
function getList() {
  loading.value = true
  listBannerInfo(queryParams.value).then(response => {
    bannerInfoList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    bannerUrl: null,
    bannerDesc: null,
    sortNum: null,
  }
  if (bannerInfoRef.value) {
    bannerInfoRef.value.resetFields()
  }
  competitionImage = []
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  competitionImage = []
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加banner图管理"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getBannerInfo(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改banner图管理"
  })
}

/** 提交按钮 */
function submitForm() {
  bannerInfoRef.value.validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateBannerInfo(form.value).then(response => {
          modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addBannerInfo(form.value).then(response => {
          modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  modal.confirm('是否确认删除').then(function() {
    return delBannerInfo(_ids)
  }).then(() => {
    getList()
    modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()


// 文件上传
let competitionImage = $ref([])
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
  competitionImage = [];
  competitionImage = [
    {...res.data}
  ]
  if(competitionImage.length > 0) {
    let item = competitionImage[0]
    form.value.bannerName = item.name
    form.value.bannerUrl = item.url
  }
}
function handleRemove(res) {
  competitionImage = []
}
const previewurl = (row) => {
  window.open(row.url, '_blank');
}
</script>
