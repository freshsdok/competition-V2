<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="公告通知标题" prop="noticeTitle">
        <el-input
          v-model.trim="queryParams.noticeTitle"
          placeholder="请输入公告通知标题"
          clearable
          style="width: 240px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
    
      <el-form-item label="发布人" prop="noticeAuthor">
        <el-input
          v-model.trim="queryParams.noticeAuthor"
          placeholder="请输入发布人"
          clearable
          style="width: 240px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型" prop="noticeType">
        <el-select v-model="queryParams.noticeType" placeholder="请选择类型" clearable style="width: 240px;">
          <el-option
            v-for="dict in sys_notice_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="noticeStatus">
        <el-select v-model="queryParams.noticeStatus" placeholder="请选择状态" clearable style="width: 240px;">
          <el-option
            v-for="dict in competition_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发布日期">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
            :default-time="[new Date('1970-01-01 00:00:00'), new Date('1970-01-01 23:59:59')]"
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
          v-hasPermi="['content:noticeInfo:add']"
        >新增公告通知</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['content:noticeInfo:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="noticeInfoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="left" type="index" width="50" />
      <el-table-column label="公告通知标题" align="left" prop="noticeTitle" min-width="220px">
        <template #default="scope">
          <span class="notice-title-text">{{ scope.row.noticeTitle }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发布人" align="center" prop="noticeAuthor" width="140px" show-overflow-tooltip/>
      <el-table-column label="类型" align="center" prop="noticeType" width="100px">
        <template #default="scope">
          <dict-tag :options="sys_notice_type" :value="scope.row.noticeType"/>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="noticeStatus" width="100px">
        <template #default="scope">
          <dict-tag :options="competition_status" :value="scope.row.noticeStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160px">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" align="center" prop="publishTime" width="160px">
        <template #default="scope">
          <span>{{ parseTime(scope.row.publishTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="320px" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button 
            v-if="scope.row.noticeStatus === '1' || scope.row.noticeStatus === '4' || scope.row.noticeStatus === '5'" 
            link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['content:noticeInfo:edit']">编辑</el-button>
          <el-button 
            v-if="scope.row.noticeStatus === '1' || scope.row.noticeStatus === '5'" 
            link type="success" icon="Promotion" @click="handleSubmitAudit(scope.row)" 
            v-hasPermi="['notice:task:submit']">提交审核</el-button>
          <el-button 
            v-if="scope.row.noticeStatus === '4'" 
            link type="success" icon="VideoPlay" @click="handlePublish(scope.row)" 
            v-hasPermi="['content:noticeInfo:publish']">发布</el-button>
          <el-button 
            v-if="scope.row.noticeStatus === '6'" 
            link type="warning" icon="VideoPause" @click="handleOffline(scope.row)" 
            v-hasPermi="['content:noticeInfo:offline']">下架</el-button>
          <el-button 
            v-if="scope.row.noticeStatus === '1' || scope.row.noticeStatus === '4' || scope.row.noticeStatus === '5'" 
            link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['content:noticeInfo:remove']">删除</el-button>
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

    <!-- 添加或修改公告通知信息对话框 -->
    <el-dialog :title="title" v-model="open" width="1200px" append-to-body>
      <el-form ref="noticeInfoRef" :model="form" :rules="rules" label-width="120px" :disabled="isViewOnly">
        <el-row>
          <el-col :span="12">
            <el-form-item label="公告通知标题" prop="noticeTitle">
              <el-input v-model="form.noticeTitle" placeholder="请输入公告通知标题" maxlength="200" show-word-limit/>
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
            <el-form-item label="公告通知类型" prop="noticeType">
              <el-select v-model="form.noticeType" placeholder="请选择公告通知类型" clearable style="width: 100%;">
                <el-option
                  v-for="dict in sys_notice_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发布时间" prop="publishTime">
              <el-date-picker
                v-model="form.publishTime"
                type="datetime"
                placeholder="请选择发布时间"
                style="width: 100%;"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                :disabled="isViewOnly"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="公告通知摘要" prop="noticeAbstract">
          <el-input 
            v-model="form.noticeAbstract" 
            type="textarea" 
            :rows="3"
            placeholder="请输入公告通知摘要" 
            maxlength="500" 
            show-word-limit/>
        </el-form-item>
        <el-form-item label="公告通知封面" prop="noticeImage">
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
        <el-form-item label="公告通知内容" prop="noticeContent">
          <editor v-model="form.noticeContent" :min-height="300" v-if="open"/>
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

<script setup name="NoticeInfo">
import { listNoticeInfo, getNoticeInfo, delNoticeInfo, addNoticeInfo, updateNoticeInfo, publishNotice, offlineNotice, submitAudit as submitAuditApi } from "@/api/content/noticeInfo"
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
const { competition_status, sys_notice_type } = useDict('competition_status', 'sys_notice_type')
const queryRef = ref(null)
const noticeInfoRef = ref(null)
const noticeInfoList = ref([])
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
    noticeTitle: null,
    noticeContent: null,
    noticeAuthor: null,
    noticeType: null,
    noticeStatus: null,
  },
  rules: {
    noticeTitle: [
      { required: true, message: "公告通知标题不能为空", trigger: "blur" }
    ],
    noticeAbstract: [
      { required: true, message: "公告通知摘要不能为空", trigger: "blur" }
    ],
    noticeContent: [
      { 
        required: true, 
        message: "公告通知内容不能为空", 
        trigger: "blur",
        validator: (rule, value, callback) => {
          if (!value || value.trim() === '' || value.replace(/<[^>]+>/g, '').trim() === '') {
            callback(new Error('公告通知内容不能为空'))
          } else {
            callback()
          }
        }
      }
    ],
    noticeImage: [
      { required: true, message: "通知公告封面不能为空", trigger: "change" }
    ],
    publishTime: [
      { required: true, message: "发布时间不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询公告通知信息列表 */
function getList() {
  loading.value = true
  const params = proxy.addDateRange(queryParams.value, dateRange.value, 'PublishTime')
  listNoticeInfo(params).then(response => {
    // 对列表进行排序：已发布的按发布时间排序，其他按创建时间排序
    const rows = response.rows || []
    rows.sort((a, b) => {
      // 已发布状态（'6'）按发布时间降序排列
      if (a.noticeStatus === '6' && b.noticeStatus === '6') {
        return new Date(b.publishTime || 0) - new Date(a.publishTime || 0)
      }
      // 一个已发布，一个未发布，已发布的排在前面
      if (a.noticeStatus === '6') return -1
      if (b.noticeStatus === '6') return 1
      // 其他状态按创建时间降序排列
      return new Date(b.createTime || 0) - new Date(a.createTime || 0)
    })
    noticeInfoList.value = rows
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
    noticeId: null,
    noticeTitle: null,
    noticeContent: null,
    noticeAbstract: null,
    noticeType: null,
    noticeStatus: '1',
    noticeImage: null,
    noticeAuthor: null,
    publishTime: null,
    checkStatus: null,
  }
  if (noticeInfoRef.value) {
    noticeInfoRef.value.resetFields()
  }
  noticeImageList.value = []
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
  ids.value = selection.map(item => item.noticeId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  isViewOnly.value = false
  open.value = true
  title.value = "添加公告通知信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  isViewOnly.value = false
  const _noticeId = row.noticeId || ids.value
  getNoticeInfo(_noticeId).then(response => {
    form.value = response.data
    // 如果有图片，设置图片列表
    if (form.value.noticeImage) {
      noticeImageList.value = [{
        name: '公告通知封面',
        url: form.value.noticeImage
      }]
    }
    open.value = true
    title.value = "修改公告通知信息"
  })
}

/** 查看按钮操作 */
function handleView(row) {
  reset()
  isViewOnly.value = true
  const _noticeId = row.noticeId || ids.value
  getNoticeInfo(_noticeId).then(response => {
    form.value = response.data
    // 如果有图片，设置图片列表
    if (form.value.noticeImage) {
      noticeImageList.value = [{
        name: '公告通知封面',
        url: form.value.noticeImage
      }]
    }
    open.value = true
    title.value = "查看公告通知信息"
  })
}

/** 提交按钮 */
function submitForm(status) {
  // 防止重复提交
  if (submitLoading.value) {
    return
  }
  
  noticeInfoRef.value.validate(valid => {
    if (valid) {
      submitLoading.value = true
      // 设置状态
      if (status) {
        form.value.noticeStatus = status
      }
      
      const isNewRecord = form.value.noticeId == null
      
      const submitPromise = isNewRecord
        ? addNoticeInfo(form.value).then(response => {
            // 如果是提交审核（status='3'），需要创建审核任务
            if (status === '3') {
              const noticeId = form.value.noticeId || (response.data && response.data.noticeId)
              if (noticeId) {
                return systemTask({businessId: noticeId, auditType: 'notice'})
              }
            }
            return Promise.resolve()
          }).then(() => {
            modal.msgSuccess(status === '3' ? "提交审核成功" : "新增成功")
            open.value = false
            getList()
          })
        : updateNoticeInfo(form.value).then(response => {
            // 如果是提交审核（status='3'），需要创建审核任务
            if (status === '3') {
              return systemTask({businessId: form.value.noticeId, auditType: 'notice'})
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
  const _noticeIds = row.noticeId || ids.value
  modal.confirm('是否确认删除公告通知编号为"' + _noticeIds + '"的数据项？').then(function() {
    return delNoticeInfo(_noticeIds)
  }).then(() => {
    getList()
    modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 提交审核 */
function handleSubmitAudit(row) {
  modal.confirm('是否确认提交审核公告通知"' + row.noticeTitle + '"？').then(function() {
    return systemTask({businessId: row.noticeId, auditType: 'notice'})
  }).then(() => {
    getList()
    modal.msgSuccess("提交成功")
  }).catch(() => {})
}

/** 发布公告通知 */
function handlePublish(row) {
  modal.confirm('是否确认发布公告通知"' + row.noticeTitle + '"？').then(function() {
    return publishNotice(row.noticeId)
  }).then(() => {
    getList()
    modal.msgSuccess("发布成功")
  }).catch(() => {})
}

/** 下架公告通知 */
function handleOffline(row) {
  modal.confirm('是否确认下架公告通知"' + row.noticeTitle + '"？').then(function() {
    return offlineNotice(row.noticeId)
  }).then(() => {
    getList()
    modal.msgSuccess("下架成功")
  }).catch(() => {})
}


getList()

// 文件上传
const noticeImageList = ref([])
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
  noticeImageList.value = []
  noticeImageList.value = [{
    name: res.data.name,
    url: res.data.url
  }]
  form.value.noticeImage = res.data.url
}

function handleRemove(res) {
  noticeImageList.value = []
  form.value.noticeImage = null
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

:deep(.el-form-item__label) {
  white-space: nowrap;
}

.notice-title-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
