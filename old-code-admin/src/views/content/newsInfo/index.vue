<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="资讯标题" prop="newsTitle">
        <el-input
          v-model.trim="queryParams.newsTitle"
          placeholder="请输入资讯标题"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="资讯副标题" prop="newsViceTitle">
        <el-input
          v-model.trim="queryParams.newsViceTitle"
          placeholder="请输入资讯副标题"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="资讯内容" prop="newsCont">
        <el-input
          v-model.trim="queryParams.newsCont"
          placeholder="请输入资讯内容"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="资讯摘要" prop="newsAbstract">
        <el-input
          v-model.trim="queryParams.newsAbstract"
          placeholder="请输入资讯摘要"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="发布人" prop="newsAuthor">
        <el-input
          v-model.trim="queryParams.newsAuthor"
          placeholder="请输入发布人"
          clearable
          style="width: 160px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="资讯来源" prop="newsSource">
        <el-input
          v-model.trim="queryParams.newsSource"
          placeholder="请输入资讯来源"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="资讯类型" prop="newsType">
        <el-select v-model="queryParams.newsType" placeholder="请选择资讯类型" clearable style="width: 200px;">
          <el-option
            v-for="dict in news_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="newsStatus">
        <el-select v-model="queryParams.newsStatus" placeholder="请选择状态" clearable style="width: 200px;">
          <el-option
            v-for="dict in competition_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发布时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
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
          v-hasPermi="['content:newsInfo:add']"
        >新增资讯</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['content:newsInfo:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="newsInfoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="left" type="index" width="50" />
      <el-table-column label="资讯标题" align="left" prop="newsTitle" min-width="250px" show-overflow-tooltip>
        <template #default="scope">
          <el-link type="primary" @click="handleUpdate(scope.row)" class="text-ellipsis">{{ scope.row.newsTitle }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="发布人" align="center" prop="newsAuthor" width="200px"/>
      <el-table-column label="标签" align="center" prop="newsTag" width="200px">
        <template #default="scope">
          <span class="text-ellipsis">{{ scope.row.newsTag }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="newsStatus" width="100px">
        <template #default="scope">
          <dict-tag :options="competition_status" :value="scope.row.newsStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160px">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" align="center" prop="publishTime" width="160px">
        <template #default="scope">
          <span>{{ parseTime(scope.row.publishTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="320px" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button 
            v-if="scope.row.newsStatus === '1' || scope.row.newsStatus === '4' || scope.row.newsStatus === '5'" 
            link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['content:newsInfo:edit']">编辑</el-button>
          <el-button 
            v-if="scope.row.newsStatus === '1' || scope.row.newsStatus === '5'" 
            link type="success" icon="Promotion" @click="handleSubmitAudit(scope.row)" 
            v-hasPermi="['info:task:submit']">提交审核</el-button>
          <el-button 
            v-if="scope.row.newsStatus === '4'" 
            link type="success" icon="VideoPlay" @click="handlePublish(scope.row)" 
            v-hasPermi="['content:newsInfo:publish']">发布</el-button>
          <el-button 
            v-if="scope.row.newsStatus === '6'" 
            link type="warning" icon="VideoPause" @click="handleOffline(scope.row)" 
            v-hasPermi="['content:newsInfo:offline']">下架</el-button>
          <el-button 
            v-if="scope.row.newsStatus === '1' || scope.row.newsStatus === '4' || scope.row.newsStatus === '5'" 
            link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['content:newsInfo:remove']">删除</el-button>
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

    <!-- 添加或修改资讯信息对话框 -->
    <el-dialog :title="title" v-model="open" width="1000px" append-to-body>
      <el-form ref="newsInfoRef" :model="form" :rules="rules" label-width="100px" :disabled="isViewOnly">
        <el-row>
          <el-col :span="12">
            <el-form-item label="资讯标题" prop="newsTitle">
              <el-input v-model="form.newsTitle" placeholder="请输入资讯标题" maxlength="200" show-word-limit/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资讯副标题" prop="newsViceTitle">
              <el-input v-model="form.newsViceTitle" placeholder="请输入资讯副标题" maxlength="200" show-word-limit/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="发布人" prop="newsAuthor">
              <el-input v-model="form.newsAuthor" placeholder="请输入发布人" maxlength="100"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资讯标签" prop="newsTag">
              <el-input v-model="form.newsTag" placeholder="请输入资讯标签，多个用逗号分隔" maxlength="200"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="资讯来源" prop="newsSource">
              <el-input v-model="form.newsSource" placeholder="请输入资讯来源" maxlength="100"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资讯类型" prop="newsType">
              <el-select v-model="form.newsType" placeholder="请选择资讯类型" clearable style="width: 100%;">
                <el-option
                  v-for="dict in news_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="资讯封面" prop="newsImage">
          <el-upload  
            :action="uploadFileUrl" 
            :file-list="newsImageList" 
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
        <el-form-item label="资讯摘要" prop="newsAbstract">
          <el-input v-model="form.newsAbstract" type="textarea" :rows="3" placeholder="请输入资讯摘要" maxlength="500" show-word-limit/>
        </el-form-item>
        <el-form-item label="资讯内容" prop="newsCont">
          <editor v-model="form.newsCont" :min-height="300" v-if="open"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="!isViewOnly" type="primary" @click="submitForm('1')" :loading="submitLoading" :disabled="submitLoading">保存草稿</el-button>
          <el-button @click="cancel" :disabled="submitLoading">{{ isViewOnly ? '关闭' : '取 消' }}</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup name="NewsInfo">
import { listNewsInfo, getNewsInfo, delNewsInfo, addNewsInfo, updateNewsInfo, publishNews, offlineNews, submitAudit as submitAuditApi } from "@/api/content/newsInfo"
import { systemTask } from '@/api/business'
import { getToken } from "@/utils/auth"
import { useDict } from "@/utils/dict"
import { parseTime } from "@/utils/ruoyi"
import modal from "@/plugins/modal"
import { Plus } from '@element-plus/icons-vue'
import Editor from '@/components/Editor'
import { getCurrentInstance } from 'vue'
import { replaceFileOrigin } from '@/utils/fileOrigin'

const { proxy } = getCurrentInstance()
const { competition_status, news_type } = useDict('competition_status', 'news_type')
const queryRef = ref(null)
const newsInfoRef = ref(null)
const newsInfoList = ref([])
const open = ref(false)
const loading = ref(true)
const submitLoading = ref(false)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const isViewOnly = ref(false)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    newsTitle: null,
    newsViceTitle: null,
    newsCont: null,
    newsAbstract: null,
    newsAuthor: null,
    newsSource: null,
    newsType: null,
    newsStatus: null,
    newsTag: null,
  },
  rules: {
    newsTitle: [
      { required: true, message: "资讯标题不能为空", trigger: "blur" }
    ],
    newsAbstract: [
      { required: true, message: "资讯摘要不能为空", trigger: "blur" }
    ],
    newsAuthor: [
      { required: true, message: "发布人不能为空", trigger: "blur" }
    ],
    newsSource: [
      { required: true, message: "资讯来源不能为空", trigger: "blur" }
    ],
    newsCont: [
      { required: true, message: "资讯内容不能为空", trigger: "change" }
    ],
    newsImage: [
      { required: true, message: "资讯封面不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询资讯信息列表 */
function getList() {
  loading.value = true
  const params = proxy.addDateRange(queryParams.value, dateRange.value, 'PublishTime')
  listNewsInfo(params).then(response => {
    // 对列表进行排序：已发布的按发布时间排序，其他按创建时间排序
    const rows = response.rows || []
    rows.sort((a, b) => {
      // 已发布状态（'6'）按发布时间降序排列
      if (a.newsStatus === '6' && b.newsStatus === '6') {
        return new Date(b.publishTime || 0) - new Date(a.publishTime || 0)
      }
      // 一个已发布，一个未发布，已发布的排在前面
      if (a.newsStatus === '6') return -1
      if (b.newsStatus === '6') return 1
      // 其他状态按创建时间降序排列
      return new Date(b.createTime || 0) - new Date(a.createTime || 0)
    })
    newsInfoList.value = rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  submitLoading.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    newsId: null,
    newsTitle: null,
    newsViceTitle: null,
    newsCont: null,
    newsAbstract: null,
    newsImage: null,
    newsTag: null,
    newsAuthor: null,
    newsSource: null,
    newsType: null,
    publishTime: null,
    newsStatus: '1',
    classifyId: null,
    readingQuantity: 0,
    likesNum: 0,
    isTop: '0',
    checkStatus: null,
  }
  if (newsInfoRef.value) {
    newsInfoRef.value.resetFields()
  }
  newsImageList.value = []
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.newsId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  isViewOnly.value = false
  open.value = true
  title.value = "添加资讯信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  isViewOnly.value = false
  const _newsId = row.newsId || ids.value
  getNewsInfo(_newsId).then(response => {
    form.value = response.data
    // 如果有图片，设置图片列表
    if (form.value.newsImage) {
      newsImageList.value = [{
        name: '资讯封面',
        url: form.value.newsImage
      }]
    }
    open.value = true
    title.value = "修改资讯信息"
  })
}

/** 查看按钮操作 */
function handleView(row) {
  reset()
  isViewOnly.value = true
  const _newsId = row.newsId || ids.value
  getNewsInfo(_newsId).then(response => {
    form.value = response.data
    // 如果有图片，设置图片列表
    if (form.value.newsImage) {
      newsImageList.value = [{
        name: '资讯封面',
        url: form.value.newsImage
      }]
    }
    open.value = true
    title.value = "查看资讯信息"
  })
}

/** 提交按钮 */
function submitForm(status) {
  // 防止重复提交
  if (submitLoading.value) {
    return
  }

  newsInfoRef.value.validate(valid => {
    if (valid) {
      submitLoading.value = true
      // 设置状态
      if (status) {
        form.value.newsStatus = status
      }
      
      const isNewRecord = form.value.newsId == null
      
      const submitPromise = isNewRecord
        ? addNewsInfo(form.value).then(response => {
            // 如果是提交审核（status='3'），需要创建审核任务
            if (status === '3') {
              const newsId = form.value.newsId || (response.data && response.data.newsId)
              if (newsId) {
                return systemTask({businessId: newsId, auditType: 'info'})
              }
            }
            return Promise.resolve()
          }).then(() => {
            modal.msgSuccess(status === '3' ? "提交审核成功" : "新增成功")
            open.value = false
            getList()
          })
        : updateNewsInfo(form.value).then(response => {
            // 如果是提交审核（status='3'），需要创建审核任务
            if (status === '3') {
              return systemTask({businessId: form.value.newsId, auditType: 'info'})
            }
            return Promise.resolve()
          }).then(() => {
            modal.msgSuccess(status === '3' ? "提交审核成功" : "修改成功")
            open.value = false
            getList()
          })
      
      submitPromise.finally(() => {
        submitLoading.value = false
      }).catch(() => {
        // 错误已在finally中处理，这里可以添加错误提示
      })
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _newsIds = row.newsId || ids.value
  modal.confirm('是否确认删除资讯编号为"' + _newsIds + '"的数据项？').then(function() {
    return delNewsInfo(_newsIds)
  }).then(() => {
    getList()
    modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 提交审核 */
function handleSubmitAudit(row) {
  modal.confirm('是否确认提交审核资讯"' + row.newsTitle + '"？').then(function() {
    return systemTask({businessId: row.newsId, auditType: 'info'})
  }).then(() => {
    getList()
    modal.msgSuccess("提交成功")
  }).catch(() => {})
}

/** 发布资讯 */
function handlePublish(row) {
  modal.confirm('是否确认发布资讯"' + row.newsTitle + '"？').then(function() {
    return publishNews(row.newsId)
  }).then(() => {
    getList()
    modal.msgSuccess("发布成功")
  }).catch(() => {})
}

/** 下架资讯 */
function handleOffline(row) {
  modal.confirm('是否确认下架资讯"' + row.newsTitle + '"？').then(function() {
    return offlineNews(row.newsId)
  }).then(() => {
    getList()
    modal.msgSuccess("下架成功")
  }).catch(() => {})
}


getList()

// 文件上传
const newsImageList = ref([])
const uploadFileUrl = ref(import.meta.env.VITE_APP_BASE_API + "/file/upload")
const headers = ref({ Authorization: "Bearer " + getToken() })

function handleUploadError(err) {
  modal.msgError('上传文件失败')
}

// 超出文件数量限制回调
function handleExceed(files, fileList) {
  modal.msgWarning('最多上传1个文件')
}

// 上传成功回调
function handleUploadSuccess(res, file) {
  res = replaceFileOrigin(res)
  newsImageList.value = []
  newsImageList.value = [{
    name: res.data.name,
    url: res.data.url
  }]
  form.value.newsImage = res.data.url
}

function handleRemove(res) {
  newsImageList.value = []
  form.value.newsImage = null
}

const previewurl = (file) => {
  window.open(file.url, '_blank')
}

/** 富文本内容格式化，只显示纯文本并移除图片 */
function formatRichText(html) {
  if (!html) {
    return ''
  }
  const withoutImg = html.replace(/<img[\s\S]*?>/gi, '')
  const temp = document.createElement('div')
  temp.innerHTML = withoutImg
  const text = temp.textContent || temp.innerText || ''
  return text.replace(/\s+/g, ' ').trim()
}
</script>

<style scoped>
.upload-file-uploader {
  margin-bottom: 10px;
}

/* 文本截断样式 */
.text-ellipsis {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
