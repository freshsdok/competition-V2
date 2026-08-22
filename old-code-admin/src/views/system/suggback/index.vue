<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="反馈编号" prop="backCode">
        <el-input
          v-model.trim="queryParams.backCode"
          placeholder="请输入反馈编号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="反馈标题" prop="title">
        <el-input
          v-model.trim="queryParams.title"
          placeholder="请输入反馈标题"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
       <el-form-item label="反馈内容" prop="content">
        <el-input
          v-model.trim="queryParams.content"
          placeholder="请输入反馈内容"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系方式" prop="phone">
        <el-input
          v-model.trim="queryParams.phone"
          placeholder="请输入联系方式"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="处理人员" prop="updateBy">
        <el-input
          v-model.trim="queryParams.updateBy"
          placeholder="请输入处理人员"
          clearable
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="反馈类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择反馈类型" clearable style="width: 200px">
          <el-option label="功能建议" value="功能建议" />
          <el-option label="问题反馈" value="问题反馈" />
          <el-option label="投诉举报" value="投诉举报" />
          <el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>
      <el-form-item label="用户姓名" prop="userName">
        <el-input
          v-model.trim="queryParams.userName"
          placeholder="请输入用户姓名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="处理状态" prop="dealStatus">
        <el-select v-model="queryParams.dealStatus" placeholder="请选择处理状态" clearable style="width: 200px">
          <el-option label="待处理" value="0" />
          <el-option label="处理中" value="1" />
          <el-option label="已回复" value="2" />
          <el-option label="已关闭" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="反馈时间" prop="suggTime">
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
          v-hasPermi="['system:suggBack:add']"
        >新增反馈</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:suggBack:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:suggBack:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="suggBackList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="反馈编号" align="center" prop="backCode" width="180" />
      <el-table-column label="反馈类型" align="center" prop="type" width="100" />
      <el-table-column label="反馈标题" align="center" prop="title" :show-overflow-tooltip="true" min-width="150" />
      <el-table-column label="用户名称" align="center" prop="userName" width="120" />
      <el-table-column label="反馈内容" align="center" prop="content" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="联系电话" align="center" prop="phone" width="120" />
      <el-table-column label="处理状态" align="center" prop="dealStatus" width="100">
 <template #default="scope">
          <el-tag v-if="scope.row.dealStatus === '0'" type="info">待处理</el-tag>
          <el-tag v-else-if="scope.row.dealStatus === '1'" type="warning">处理中</el-tag>
          <el-tag v-else-if="scope.row.dealStatus === '2'" type="success">已回复</el-tag>
          <el-tag v-else-if="scope.row.dealStatus === '3'">已关闭</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人员" align="center" prop="updateBy" width="120" />
      <el-table-column label="反馈时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160" fixed="right">
        <template #default="scope">
          <el-tooltip content="查看" placement="top">
            <el-button 
              link 
              type="primary" 
              icon="View" 
              @click="handleView(scope.row)"
              v-hasPermi="['system:suggBack:query']"
            ></el-button>
          </el-tooltip>
          <el-tooltip content="编辑" placement="top">
            <el-button 
              link 
              type="primary" 
              icon="Edit" 
              @click="handleUpdate(scope.row)"
              v-hasPermi="['system:suggBack:edit']"
            ></el-button>
          </el-tooltip>
          <el-tooltip content="回复" placement="top">
            <el-button 
              link 
              type="success" 
              icon="ChatLineRound" 
              @click="handleReply(scope.row)"
              v-hasPermi="['system:suggBack:reply']"
            ></el-button>
          </el-tooltip>
          <!-- <el-tooltip content="转交" placement="top">
            <el-button 
              link 
              type="warning" 
              icon="Position" 
              @click="handleTransfer(scope.row)"
              v-hasPermi="['system:suggBack:transfer']"
            ></el-button>
          </el-tooltip> -->
          <!-- <el-tooltip content="关闭" placement="top">
            <el-button 
              link 
              type="danger" 
              icon="Close" 
              @click="handleClose(scope.row)"
              v-hasPermi="['system:suggBack:close']"
            ></el-button>
          </el-tooltip>  -->
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加意见反馈对话框 -->
    <el-dialog :title="title" v-model="open" width="780px" append-to-body>
      <el-form ref="suggBackRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="反馈类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择反馈类型" style="width: 100%">
                <el-option label="功能建议" value="功能建议" />
                <el-option label="问题反馈" value="问题反馈" />
                <el-option label="投诉举报" value="投诉举报" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="反馈标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入反馈标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户姓名" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入用户姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="反馈内容" prop="content">
              <el-input 
                v-model="form.content" 
                type="textarea" 
                :rows="4"
                placeholder="请输入反馈内容" 
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改处理状态对话框 -->
    <el-dialog title="修改处理状态" v-model="updateStatusOpen" width="500px" append-to-body>
      <el-form ref="updateStatusRef" :model="updateStatusForm" :rules="updateStatusRules" label-width="100px">
        <el-form-item label="反馈编号">
          <span style="color: #606266;">{{ updateStatusForm.backCode }}</span>
        </el-form-item>
        <el-form-item label="反馈标题">
          <div style="color: #606266; line-height: 1.5;">{{ updateStatusForm.title }}</div>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag v-if="updateStatusForm.currentStatus === '0'" type="info">待处理</el-tag>
          <el-tag v-else-if="updateStatusForm.currentStatus === '1'" type="warning">处理中</el-tag>
          <el-tag v-else-if="updateStatusForm.currentStatus === '2'" type="success">已回复</el-tag>
          <el-tag v-else-if="updateStatusForm.currentStatus === '3'">已关闭</el-tag>
        </el-form-item>
        <el-form-item label="新状态" prop="dealStatus">
          <el-select v-model="updateStatusForm.dealStatus" placeholder="请选择处理状态" style="width: 100%">
            <el-option label="待处理" value="0" />
            <el-option label="处理中" value="1" />
            <el-option label="已回复" value="2" />
            <el-option label="已关闭" value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitUpdateStatus">确 定</el-button>
          <el-button @click="updateStatusOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="反馈详情" v-model="viewOpen" width="780px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="反馈ID">{{ viewForm.suggBackId }}</el-descriptions-item>
        <el-descriptions-item label="反馈编码">{{ viewForm.backCode }}</el-descriptions-item>
        <el-descriptions-item label="反馈类型">{{ viewForm.type }}</el-descriptions-item>
        <el-descriptions-item label="用户姓名">{{ viewForm.userName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ viewForm.phone }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag v-if="viewForm.dealStatus === '0'" type="info">待处理</el-tag>
          <el-tag v-else-if="viewForm.dealStatus === '1'" type="warning">处理中</el-tag>
          <el-tag v-else-if="viewForm.dealStatus === '2'" type="success">已回复</el-tag>
          <el-tag v-else-if="viewForm.dealStatus === '3'">已关闭</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人员">{{ viewForm.updateBy }}</el-descriptions-item>
        <el-descriptions-item label="反馈时间" :span="2">
          {{ parseTime(viewForm.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item label="反馈标题" :span="2">
          {{ viewForm.title }}
        </el-descriptions-item>
        <el-descriptions-item label="反馈内容" :span="2">
          {{ viewForm.content }}
        </el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2" v-if="viewForm.dealResult">
          {{ viewForm.dealResult }}
        </el-descriptions-item>
        <el-descriptions-item label="回复内容" :span="2" v-if="viewForm.replyContent">
          {{ viewForm.replyContent }}
        </el-descriptions-item>
        <el-descriptions-item label="回复时间" :span="2" v-if="viewForm.replyTime">
          {{ parseTime(viewForm.replyTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item label="满意度评价" :span="2" v-if="viewForm.saftEval">
          {{ viewForm.saftEval }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 回复对话框 -->
    <el-dialog title="回复反馈" v-model="replyOpen" width="600px" append-to-body>
      <el-form ref="replyRef" :model="replyForm" :rules="replyRules" label-width="100px">
        <el-form-item label="反馈内容">
          <div style="color: #606266; line-height: 1.5;">{{ replyForm.content }}</div>
        </el-form-item>
        <el-form-item label="回复内容" prop="replyContent">
          <el-input 
            v-model="replyForm.replyContent" 
            type="textarea" 
            :rows="6"
            placeholder="请输入回复内容" 
          />
        </el-form-item>
        <el-form-item label="处理状态" prop="dealStatus">
          <el-select v-model="replyForm.dealStatus" placeholder="请选择处理状态" style="width: 100%">
            <el-option label="处理中" value="1" />
            <el-option label="已回复" value="2" />
            <el-option label="已关闭" value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitReply">确 定</el-button>
          <el-button @click="replyOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 转交对话框 -->
    <el-dialog title="转交反馈" v-model="transferOpen" width="600px" append-to-body>
      <el-form ref="transferRef" :model="transferForm" :rules="transferRules" label-width="100px">
        <el-form-item label="反馈内容">
          <div style="color: #606266; line-height: 1.5;">{{ transferForm.content }}</div>
        </el-form-item>
        <el-form-item label="转交说明" prop="dealResult">
          <el-input 
            v-model="transferForm.dealResult" 
            type="textarea" 
            :rows="4"
            placeholder="请输入转交说明" 
          />
        </el-form-item>
        <el-form-item label="处理状态" prop="dealStatus">
          <el-select v-model="transferForm.dealStatus" placeholder="请选择处理状态" style="width: 100%">
            <el-option label="处理中" value="1" />
            <el-option label="已回复" value="2" />
            <el-option label="已关闭" value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitTransfer">确 定</el-button>
          <el-button @click="transferOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SuggBack">
import { listSuggBack, getSuggBack, delSuggBack, addSuggBack, updateSuggBack, replySuggBack, transferSuggBack, changeStatusSuggBack } from "@/api/system/suggback"

const { proxy } = getCurrentInstance()

const suggBackList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const replyOpen = ref(false)
const transferOpen = ref(false)
const updateStatusOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])

const data = reactive({
  form: {},
  viewForm: {},
  replyForm: {},
  transferForm: {},
  updateStatusForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    backCode: undefined,
    title: undefined,
    type: undefined,
    userName: undefined,
    updateBy: undefined
  },
  rules: {
    type: [{ required: true, message: "反馈类型不能为空", trigger: "change" }],
    title: [{ required: true, message: "反馈标题不能为空", trigger: "blur" }],
    userName: [{ required: true, message: "用户姓名不能为空", trigger: "blur" }],
    content: [{ required: true, message: "反馈内容不能为空", trigger: "blur" }]
  },
  replyRules: {
    replyContent: [{ required: true, message: "回复内容不能为空", trigger: "blur" }],
    dealStatus: [{ required: true, message: "处理状态不能为空", trigger: "change" }]
  },
  transferRules: {
    dealResult: [{ required: true, message: "转交说明不能为空", trigger: "blur" }],
    dealStatus: [{ required: true, message: "处理状态不能为空", trigger: "change" }]
  },
  updateStatusRules: {
    dealStatus: [{ required: true, message: "处理状态不能为空", trigger: "change" }]
  }
})

const { queryParams, form, viewForm, replyForm, transferForm, updateStatusForm, rules, replyRules, transferRules, updateStatusRules } = toRefs(data)

/** 查询意见反馈列表 */
function getList() {
  loading.value = true
  const params = proxy.addDateRange(queryParams.value, dateRange.value)
  listSuggBack(params).then(response => {
    suggBackList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    suggBackId: undefined,
    backCode: undefined,
    type: undefined,
    title: undefined,
    userName: undefined,
    phone: undefined,
    content: undefined,
    dealStatus: "0"
  }
  proxy.resetForm("suggBackRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.suggBackId)
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加意见反馈"
}

/** 修改处理状态按钮操作 */
function handleUpdate(row) {
  updateStatusForm.value = {
    suggBackId: row.suggBackId,
    backCode: row.backCode,
    title: row.title,
    currentStatus: row.dealStatus,
    dealStatus: row.dealStatus
  }
  updateStatusOpen.value = true
}

/** 查看详情 */
function handleView(row) {
  const suggBackId = row.suggBackId
  getSuggBack(suggBackId).then(response => {
    viewForm.value = response.data
    viewOpen.value = true
  })
}

/** 回复按钮操作 */
function handleReply(row) {
  replyForm.value = {
    suggBackId: row.suggBackId,
    content: row.content,
    replyContent: undefined,
    dealStatus: '2'  // 默认设置为已回复
  }
  replyOpen.value = true
}

/** 转交按钮操作 */
function handleTransfer(row) {
  transferForm.value = {
    suggBackId: row.suggBackId,
    content: row.content,
    dealResult: undefined,
    dealStatus: '1'  // 默认设置为处理中
  }
  transferOpen.value = true
}

/** 提交回复 */
function submitReply() {
  proxy.$refs["replyRef"].validate(valid => {
    if (valid) {
      replySuggBack(replyForm.value).then(response => {
        proxy.$modal.msgSuccess("回复成功")
        replyOpen.value = false
        getList()
      })
    }
  })
}

/** 提交转交 */
function submitTransfer() {
  proxy.$refs["transferRef"].validate(valid => {
    if (valid) {
      transferSuggBack(transferForm.value).then(response => {
        proxy.$modal.msgSuccess("转交成功")
        transferOpen.value = false
        getList()
      })
    }
  })
}

/** 提交修改处理状态 */
function submitUpdateStatus() {
  proxy.$refs["updateStatusRef"].validate(valid => {
    if (valid) {
      const params = {
        suggBackId: updateStatusForm.value.suggBackId,
        dealStatus: updateStatusForm.value.dealStatus
      }
      changeStatusSuggBack(params).then(response => {
        proxy.$modal.msgSuccess("修改成功")
        updateStatusOpen.value = false
        getList()
      })
    }
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["suggBackRef"].validate(valid => {
    if (valid) {
      if (form.value.suggBackId != undefined) {
        updateSuggBack(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSuggBack(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const suggBackIds = row.suggBackId || ids.value
  proxy.$modal.confirm('是否确认删除该数据项？').then(function() {
    return delSuggBack(suggBackIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/suggBack/export', {
    ...queryParams.value
  }, `suggBack_${new Date().getTime()}.xlsx`)
}

getList()
</script>
