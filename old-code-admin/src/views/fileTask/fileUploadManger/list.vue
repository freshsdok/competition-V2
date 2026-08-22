<template>
  <div class="app-container mini_size_table">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="任务名称" prop="fileTaskName">
        <el-input
          v-model.trim="queryParams.fileTaskName"
          placeholder="请输入任务名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
       <el-form-item label="赛事名称" prop="competitionName">
        <el-input
          v-model.trim="queryParams.competitionName"
          placeholder="请输入赛事名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="赛道名称" prop="competitionTrackName">
        <el-input
          v-model.trim="queryParams.competitionTrackName"
          placeholder="请输入赛道名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
       <el-form-item label="组别/赛项名称" prop="secondLevelName">
        <el-input
          v-model.trim="queryParams.secondLevelName"
          placeholder="请输入组别/赛项名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="用户姓名" prop="userName">
        <el-input
          v-model.trim="queryParams.userName"
          placeholder="请输入用户姓名"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="队伍名称" prop="teamName">
        <el-input
          v-model.trim="queryParams.teamName"
          placeholder="请输入队伍名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="指导教师" prop="guideTeacher">
        <el-input
          v-model.trim="queryParams.guideTeacher"
          placeholder="请输入指导教师"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="带队老师姓名" prop="leaderTeacherName">
        <el-input
          v-model.trim="queryParams.leaderTeacherName"
          placeholder="请输入带队老师姓名"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="上传时间范围" style="width: 480px">
        <el-date-picker
           v-model="dateRange"
           value-format="YYYY-MM-DD HH:mm:ss"
           type="datetimerange"
           range-separator="至"
           start-placeholder="开始时间"
           end-placeholder="结束时间"
             :default-time="[new Date('1970-01-01 00:00:00'), new Date('1970-01-01 23:59:59')]"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-button type="success"  @click="handleExportAll('all')" v-loading="loadingExcelAll" v-hasPermi="['system:fileUploadManager:export']">导出列表</el-button>
      <el-button type="success"  @click="handleExportAll('filter')" v-loading="loadingExcelFilter" v-hasPermi="['system:fileUploadManager:export']">导出检索列表</el-button>
      <el-button type="primary"  @click="handleFileExport" v-loading="loadingFile" v-hasPermi="['system:fileUploadManager:fileExport']">导出检索文件</el-button>
      <el-button type="primary"  @click="handleFileExportSelected" v-loading="loadingFile2" v-hasPermi="['system:fileUploadManager:fileExport']">导出选中文件</el-button>
      <el-button type="warning" icon="Upload" @click="handleReviewImportSelected" v-hasPermi="['competition:review:object:import']">导入选中到评审</el-button>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" 
              @selection-change="handleSelectionChange"
              :data="tableList">
      <el-table-column label="选择" type="selection" width="30" />
      <el-table-column label="任务名称" align="left" prop="fileTaskName" min-width="120"/>
      <el-table-column label="赛事名称" align="left" min-width="120">
        <template #default="scope">
          <el-tooltip :content="scope.row.competitionName" placement="top" :disabled="!scope.row.competitionName">
            <div class="text-ellipsis-3">{{ scope.row.competitionName }}</div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="赛道" align="left" min-width="100">
        <template #default="scope">
          <el-tooltip :content="scope.row.competitionTrackName" placement="top" :disabled="!scope.row.competitionTrackName">
            <div class="text-ellipsis-3">{{ scope.row.competitionTrackName }}</div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="组别/赛项" align="left" min-width="120">
        <template #default="scope">
          <el-tooltip :content="scope.row.secondLevelName" placement="top" :disabled="!scope.row.secondLevelName">
            <div class="text-ellipsis-3">{{ scope.row.secondLevelName }}</div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="用户姓名" align="center" prop="userName" width="60" />
      <el-table-column label="队伍名称" align="left" prop="teamName" width="120" show-overflow-tooltip/>
      <el-table-column label="带队老师" align="center" prop="leaderTeacherName" width="60" show-overflow-tooltip/>
      <el-table-column label="指导教师" align="center" prop="guideTeacher" width="96"  show-overflow-tooltip/>
      <el-table-column label="上传时间" align="center" prop="uploadTime" width="76"/>
      <el-table-column label="上传文件" align="left" prop="uploadFileName" min-width="210" >
        <template #default="scope">
          <div v-for="(item,index) in scope.row?.fileInfo || []" :key="index" >
            <span class="download-link"
              @click="downloadOssFile(item.downloadLink,item.fileName)"
              target="_blank">{{item.fileName}}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="总大小" align="center" prop="totalSize" width="70">
        <template #default="scope">
          {{getTotalSize(scope.row)}}
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
  </div>
</template>

<script setup name="CompetitionApplyList">
import modal from "@/plugins/modal";
import { fileUploadManagerList,exportManageExportFileFilter,exportManageExportExecl,exportManageExportFileSelect} from "@/api/fileTask"
import { addDateRangeSAE } from "@/utils/ruoyi"
import { cloneDeep } from 'lodash-es';
import { ossFileFuc } from "@/hooks/download";
import { useRouter } from "vue-router";
const {downloadOssFile} = ossFileFuc()
const router = useRouter()
/** 查询组件库信息列表 */
let dateRange = ref([])
const loading = ref(false)
const tableList = ref([])
const total = ref(0)
let queryParams = $ref({
  pageNum: 1,
  pageSize: 10
})
function getList() {
  loading.value = true
  let arr = cloneDeep(queryParams)
  let query = addDateRangeSAE(arr, dateRange.value, 'uploadTimeStart', 'uploadTimeEnd')
  if(query?.competitionRoleNameReq){
    query.competitionRoleNameReq = query.competitionRoleNameReq.join(',')
  }
  fileUploadManagerList(query).then(response => {
    tableList.value = response.rows.map(item => {
      return {
        ...item,
        fileInfo: getRowInfo(item) || []
      }
    })
    total.value = response.total
    loading.value = false
  }).catch(error => {
    loading.value = false
  })
}


// 组件挂载后执行
onMounted(() => {
  getList()
})

/** 获取行信息 */
function getRowInfo(row){
  let fileInfo = row.fileInfo;
  if(fileInfo){
    fileInfo = JSON.parse(fileInfo)
  }else{
    return []
  }
  return fileInfo
}

/** 获取总大小 */
function getTotalSize(row){
  let totalSize = row.totalSize || 0;
  // 转换为数值类型，确保计算准确
  totalSize = Number(totalSize);
  if (totalSize >= 1024) {
    // 如果大于等于1024MB，转换为GB，保留2位小数
    return (totalSize / 1024).toFixed(2) + 'GB';
  } else {
    // 如果小于1024MB，保留2位小数，显示MB
    return totalSize.toFixed(2) + 'MB';
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  tableList.value = []
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮操作 */
const queryRef = ref(null)
const showSearch = ref(true)
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  dateRange.value = []
  handleQuery()
}

let loadingExcelAll = $ref(false)
let loadingExcelFilter = $ref(false)
/** 导出按钮操作 */
function handleExportAll(type) {
  let query = cloneDeep(queryParams)
  query = addDateRangeSAE(query, dateRange.value, 'uploadTimeStart', 'uploadTimeEnd')
  if(type === 'all'){
   query = {}
   loadingExcelAll = true
  }else{
    loadingExcelFilter = true
  }
  exportManageExportExecl(query).then(response => { 
    if(type === 'all'){
      loadingExcelAll = false
    }else{
      loadingExcelFilter = false
    }
    if(response.code === 200){
      goExportPage()
    }
  }).catch(error => {
    if(type === 'all'){
      loadingExcelAll = false
    }else{
      loadingExcelFilter = false
    }
  })
}
let loadingFile = $ref(false)
function handleFileExport() {
  let query = cloneDeep(queryParams)
  query = addDateRangeSAE(query, dateRange.value, 'uploadTimeStart', 'uploadTimeEnd')
  loadingFile = true
  exportManageExportFileFilter(query).then(response => { 
    loadingFile = false
    if(response.code === 200){
      let data = response.data || {}
      let times = filterTimeLable(data.time)
       modal.confirm(`共计${data?.count || 0}文件，共计${data?.totalSize},预计导出时间${times}，请稍后在"导出管理"列表查看文件`,'导出成功',{
        confirmButtonText: '去查看',
        cancelButtonText: '取消',
        type: 'success',
      }).then(() => {
        router.push({ path: '/fileTask/exportList' })
      }).catch(() => {})
    }
  }).catch(error => {
    loadingFile = false
  })
}
/** 过滤时间标签 */
function filterTimeLable(time){
  if(!time){return 0}
  // 给出秒 比如200 超过60秒则转换成分，换算为几小时几分几秒，如果不够小时则为几分几秒
  let seconds = Number(time)
  if(seconds < 60){
    return `${seconds}秒`
  }
  let minutes = Math.floor(seconds / 60)
  let hours = Math.floor(minutes / 60)
  let remainingMinutes = minutes % 60
  let remainingSeconds = seconds % 60
  if(hours > 0){
    return `${hours}小时${remainingMinutes}分${remainingSeconds}秒`
  }
  return `${minutes}分${remainingSeconds}秒`
}

/** 跳转导出管理列表 */
function goExportPage() {
   modal.confirm('导出成功，请稍后在"导出管理"列表查看文件','导出成功',{
    confirmButtonText: '去查看',
    cancelButtonText: '取消',
    type: 'success',
  }).then(() => {
    router.push({ path: '/fileTask/exportList' })
  }).catch(() => {})
}

/** 选择行操作 */
let selectedRows = $ref([])
function handleSelectionChange(val) {
  selectedRows = val
  console.log(selectedRows,'sele1111ctedRows')
}

let loadingFile2 = $ref(false)
function handleFileExportSelected(){
  if(selectedRows.length === 0){
    modal.msgWarning('请先选择文件','提示')
    return
  }
  let exportArr = selectedRows.map(item => String(item.id))
   exportManageExportFileSelect(exportArr).then(response => { 
    loadingFile2 = false
    if(response.code === 200){
      let data = response.data || {}
      let times = filterTimeLable(data.time)
       modal.confirm(`共计${data?.count || 0}文件，共计${data?.totalSize},预计导出时间${times}，请稍后在"导出管理"列表查看文件`,'导出成功',{
        confirmButtonText: '去查看',
        cancelButtonText: '取消',
        type: 'success',
      }).then(() => {
        router.push({ path: '/fileTask/exportList' })
      }).catch(() => {})
    }
  }).catch(error => {
    loadingFile2 = false
  })
}

function handleReviewImportSelected() {
  if(selectedRows.length === 0){
    modal.msgWarning('请先选择文件','提示')
    return
  }
  router.push({
    path: '/review/import',
    query: {
      sourceBizType: 'FILE_UPLOAD_MANAGER',
      sourceBizIds: selectedRows.map(item => item.id).join(',')
    }
  })
}
</script>
<style scoped lang="scss">
.download-link{
  color: #409eff;
  text-decoration: underline;
  word-break: break-all;
  cursor: pointer;
}
.color_delete{
  color: #999999;
}
.color_add{
  color: #67c23a;
}
.color_update{
  color: #ed9108;
}
.text-ellipsis-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}
</style>
