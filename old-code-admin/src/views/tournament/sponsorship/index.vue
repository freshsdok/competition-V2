<template>
  <div class="app-container app-cell-form">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" >
      <el-form-item label="企业名称" prop="enterpriseName">
        <el-input
          v-model.trim="queryParams.enterpriseName"
          placeholder="请输入企业名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="赞助类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择赞助类型" style="width: 160px;">
          <el-option v-for="item in cooperation_type" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="展示状态" prop="displayStatus">
        <el-select v-model="queryParams.displayStatus" placeholder="请选择展示状态" style="width: 160px;">
          <el-option label="展示" value="1" />
          <el-option label="不展示" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="联系人" prop="contactPerson">
        <el-input
          v-model.trim="queryParams.contactPerson"
          placeholder="请输入联系人"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input
          v-model.trim="queryParams.phone"
          placeholder="请输入联系电话"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model.trim="queryParams.email"
          placeholder="请输入邮箱"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="赞助金额" >
          <div class="flex-center-input">
            <el-input
            v-model.trim="queryParams.spopAmountStart"
            placeholder="最小金额"
            style="width: 95px;"
            type="number"
            min="0"
            step="1"
            clearable/>
          <span class="c-line">到</span>
          <el-input
            v-model.trim="queryParams.spopAmountEnd"
            placeholder="最大金额"
            style="width: 95px;"
            type="number"
            min="0"
            step="1"
            clearable/>
          </div>
      </el-form-item>
      <el-form-item label="合作日期范围" style="width: 450px">
        <el-date-picker
            v-model="filterRange"
            value-format="YYYY-MM-DD"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
        ></el-date-picker>
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
          v-hasPermi="['competition:sponsoringEnterprise:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="contentList">
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="企业名称" prop="enterpriseName" align="left"   min-width="160"
        show-overflow-tooltip/>
      <el-table-column label="赞助类型" align="center" prop="type" min-width="100" >
        <template #default="scope">
          <dict-tag :options="cooperation_type" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="赞助金额（元）" align="center" prop="spopAmount" min-width="120"  />
      <el-table-column label="展示状态" align="center" prop="displayStatus" min-width="100" >
        <template #default="scope">
                <dict-tag :options="displayStatus" :value="scope.row.displayStatus" />
        </template>
      </el-table-column>
      <el-table-column label="开始合作日期" align="center" prop="coptStartTime" min-width="160">
        <template #default="scope">
          {{ parseTime(scope.row.coptStartTime, '{y}-{m}-{d}') }}
        </template>
      </el-table-column>
      <el-table-column label="结束合作日期" align="center" prop="coptEndTime"  min-width="140" >
        <template #default="scope">
          {{ parseTime(scope.row.coptEndTime, '{y}-{m}-{d}') }}
        </template>
      </el-table-column>
      <el-table-column label="联系人" align="center" prop="contactPerson" min-width="100"  show-overflow-tooltip/>
      <el-table-column label="联系电话" align="center" prop="phone"  min-width="120"
        show-overflow-tooltip />
      <el-table-column label="邮箱" align="center" prop="email" min-width="120"  show-overflow-tooltip/>
      <el-table-column label="地址" align="center" prop="enterpriseAddr" min-width="180" show-overflow-tooltip/>
      <el-table-column label="操作" align="center" fixed="right" width="150">
        <template #default="scope">
          <el-button
            type="primary"
            link
            @click="handleUpdate(scope.row,'onlyShow')"
            v-hasPermi="['competition:sponsoringEnterprise:query']"
          >详情</el-button>
          <el-button
            type="primary"
            link
            @click="handleUpdate(scope.row)"
            v-hasPermi="['competition:sponsoringEnterprise:edit']"
          >编辑</el-button>
          <el-button
            type="danger"
            link
            @click="handleDelete(scope.row)"
            v-hasPermi="['competition:sponsoringEnterprise:remove']"
          >删除</el-button>
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

    <!-- 添加或修改组件库信息对话框 -->
    <el-dialog :title="title" v-model="open" width="60%" append-to-body>
      <el-form ref="contentRef" :model="form" :rules="rules" label-width="130px" :disabled="detailtype === 'onlyShow'">
        <el-row>
          <el-col :span="12">
            <el-form-item label="企业名称" prop="enterpriseName">
              <el-input v-model="form.enterpriseName" placeholder="请输入企业名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
              <el-form-item label="企业logo">
                <el-upload  :action="uploadFileUrl" 
                            :file-list="form.enterpriseLogo" 
                            :limit="1"
                            :on-exceed="handleExceed"
                            :before-upload="beforeUpload"
                            list-type="picture-card"
                  :on-preview="previewurl" :on-error="handleUploadError" :on-success="handleUploadSuccess"
                  :on-remove="handleRemove" :show-file-list="true" :headers="headers" class="upload-file-uploader"
                  style="width: 100%;"
                  ref="upload">
                   <el-icon><Plus /></el-icon>
                </el-upload>
              </el-form-item>
          </el-col>
           <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话/座机号码" :maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地址" prop="enterpriseAddr">
              <el-input v-model="form.enterpriseAddr" placeholder="请输入地址" :maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="官网链接" prop="officialWebsiteLink">
              <el-input v-model="form.officialWebsiteLink" placeholder="请输入网址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赞助金额（元）" prop="spopAmount">
              <el-input-number v-model="form.spopAmount" placeholder="请输入赞助金额" style="width: 100%;" type="number" :min="0" :step="0.01" />

            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赞助类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择赞助类型">
                <el-option v-for="item in cooperation_type" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合作日期范围" >
                <el-date-picker
                  v-model="dateRange"
                  value-format="YYYY-MM-DD"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="展示状态" prop="displayStatus">
              <el-radio-group v-model="form.displayStatus">
                <el-radio label="1">展示</el-radio>
                <el-radio label="2">不展示</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介" prop="enterpriseDesc">
              <el-input v-model="form.enterpriseDesc" placeholder="请输入简介" type="textarea" :rows="3" :maxlength="500" show-word-limit/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" v-if="detailtype !== 'onlyShow'" :loading="submitLoading">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SponsorshipIndex">
import { sponsoringEnterpriseList,saveSponsoringEnterpriseInfo,updateSponsoringEnterpriseInfo ,removeSponsoringEnterpriseInfo} from "@/api/tournament/sponsoringEnterprise"
import RightToolbar from '@/components/RightToolbar/index.vue'
import { useDict } from '@/utils/dict'
import { getToken } from "@/utils/auth";
import { addDateRangeSAE, parseTime } from "@/utils/ruoyi"
import { cloneDeep } from 'lodash'
import { ElMessage } from "element-plus";
import { beforeUpload } from '@/utils/file'
import { replaceFileOrigin } from '@/utils/fileOrigin'
const { cooperation_type
} = useDict("cooperation_type")
const dateRange = ref([])
const filterRange = ref([])
const displayStatus = ref([
  { label: "展示", value: "1"},
  { label: "不展示", value: "2"}
])

// 表单引用
const contentRef = ref(null)
const queryRef = ref(null)


const contentList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const submitLoading = ref(false)
const deleteLoading = ref(false)

const data = reactive({
  form: {
    enterpriseName: "",
    enterpriseLogo: [],
    contactPerson: "",
    phone: "",
    email:'',
    enterpriseAddr:'',
    officialWebsiteLink:'',
    spopAmount:null,
    type:'',
    displayStatus:'2',
    enterpriseDesc:'',
    coptStartTime: null,
    coptEndTime: null,
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    enterpriseName: "",
    contactPerson: "",
    phone: "",
    email:'',
    enterpriseAddr:'',
    officialWebsiteLink:'',
    spopAmount:null,
    type:'',
    displayStatus:'',
    enterpriseDesc:'',
    coptStartTime: null,
    coptEndTime: null,
    spopAmountStart:null,
    spopAmountEnd:null
  },
  rules: {
    enterpriseName: [
      { required: true, message: "赞助企业名称不能为空", trigger: "blur" }
    ],
    phone: [
      { required: false, message: '请输入正确的联系电话/座机', trigger: ['blur', 'change'] },
      { pattern: /^[^\u4e00-\u9fa5]*$/, message: '请输入正确的联系电话/座机', trigger: ['blur', 'change'], transform(value) {
          return value ? value : '';
        }}
    ],
    email: [
      { required: false, message: '请输入正确的邮箱格式', trigger: 'blur' },
      { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '请输入正确的邮箱格式', trigger: 'blur', transform(value) {
          return value ? value : '';
        }}
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询数据源列表 */
function getList() {
  loading.value = true
  let dateSae = cloneDeep(addDateRangeSAE(queryParams.value, filterRange.value, 'coptStartTime', 'coptEndTime'))
  const { pageNum, pageSize, ...body } = dateSae;
  const query = { pageNum, pageSize };
  sponsoringEnterpriseList(query,body).then(response => {
    contentList.value = response.rows
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
  if (contentRef.value) {
    contentRef.value.resetFields()
  }
  form.value = {
    enterpriseName: "",
    enterpriseLogo: [],
    contactPerson: "",
    phone: "",
    email:'',
    enterpriseAddr:'',
    officialWebsiteLink:'',
    spopAmount:null,
    type:'',
    displayStatus:'2',
    enterpriseDesc:'',
    coptStartTime: null,
    coptEndTime: null
  }
  dateRange.value = []
}

/** 搜索按钮操作 */
function handleQuery() {
  contentList.value = []
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  queryParams.value.spopAmountStart = null
  queryParams.value.spopAmountEnd = null
  filterRange.value = []
  handleQuery()
}



/** 提交按钮 */
function submitForm() {
  contentRef.value.validate(valid => {
    if (valid) {
      submitLoading.value = true
      let sendData = cloneDeep(addDateRangeSAE(form.value, dateRange.value, 'coptStartTime', 'coptEndTime'))
      const submitFunc = form.value.enterpriseId ? updateSponsoringEnterpriseInfo : saveSponsoringEnterpriseInfo
      const successMsg = form.value.enterpriseId ? "修改成功" : "新增成功"
      console.log(sendData,'sendData')
      sendData.enterpriseLogo = sendData.enterpriseLogo[0] && sendData.enterpriseLogo[0].url ? sendData.enterpriseLogo[0].url : ''
      submitFunc(sendData).then(response => {
        ElMessage.success(successMsg)
        open.value = false
        getList()
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}
let detailtype = $ref('')
/** 新增按钮操作 */
function handleAdd() {
  reset()
  detailtype = ''
  open.value = true
  title.value = "添加赞助企业"
}
/** 修改按钮操作 */

function handleUpdate(row,type) {
  reset()
  // 回显数据
  form.value = {
    enterpriseId: row.enterpriseId,
    enterpriseName: row.enterpriseName,
    enterpriseLogo: row.enterpriseLogo ? [{url: row.enterpriseLogo}] : [],
    contactPerson: row.contactPerson,
    phone: row.phone,
    email:row.email,
    enterpriseAddr:row.enterpriseAddr,
    officialWebsiteLink:row.officialWebsiteLink,
    spopAmount:row.spopAmount,
    type:row.type,
    displayStatus:row.displayStatus,
    enterpriseDesc:row.enterpriseDesc,
    coptStartTime: row.coptStartTime,
    coptEndTime: row.coptEndTime
  }
  dateRange.value = [row.coptStartTime, row.coptEndTime]
  open.value = true
  title.value = type === 'onlyShow' ? "赞助企业详情" : "修改赞助企业信息"
  detailtype = type || ''
}

/** 删除按钮操作 */
function handleDelete(row) {
  ElMessageBox.confirm(
    "是否确认删除该赞助企业？",
    "系统提示",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
      confirmButtonLoading: deleteLoading.value
    }
  ).then(() => {
    deleteLoading.value = true
    removeSponsoringEnterpriseInfo(row.enterpriseId).then(() => {
      ElMessage.success("删除成功")
      getList()
      deleteLoading.value = false
    }).catch(() => {
      ElMessage.error("删除失败")
      deleteLoading.value = false
    })
  })
}

getList()

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
  form.value.enterpriseLogo = [];
  form.value.enterpriseLogo.push({
    ...res.data
  })
}
function handleRemove(res) {
  form.value.enterpriseLogo = []
}
const previewurl = (row) => {
  window.open(row.url, '_blank');
}
</script>
<style scoped lang="scss">
.flex-center-input {
  display: flex;
  align-items: center;
  .c-line {
    margin: 0 6px;
  }
}
</style>
