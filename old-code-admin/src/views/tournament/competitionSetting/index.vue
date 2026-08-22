<template>
  <div class="app-container app-cell-form">
     <!-- 搜索条件 -->
     <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
        <el-row>
         <el-form-item label="赛道名称" prop="competitionTrackName">
            <el-input
               v-model.trim="queryParams.competitionTrackName"
               placeholder="请输入赛道名称"
               style="width: 160px;"
               clearable/>
         </el-form-item>
           <el-form-item label="赛事名称" prop="competitionName">
               <el-input
                  v-model.trim="queryParams.competitionName"
                  placeholder="请输入赛事名称"
                  style="width: 160px;"
                  clearable/>
            </el-form-item>
            <el-form-item label="二级分类" prop="competitionTrackType">
              <el-select v-model="queryParams.competitionTrackType" placeholder="请选择二级分类" clearable style="width: 160px;">
                 <el-option
                    v-for="dict in competition_track_type"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                 />
              </el-select>
           </el-form-item>
            <el-form-item label="赛道状态" prop="checkStatus">
              <el-select v-model="queryParams.checkStatus" placeholder="请选择赛道状态" clearable style="width: 160px;" >
                 <el-option
                    v-for="dict in check_status"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                 />
              </el-select>
           </el-form-item>
           <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
           </el-form-item>
        </el-row>
     </el-form>

     <!-- 操作按钮 -->
     <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
           <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['competition:competitionTrackInfo:add']"
           >新增</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
     </el-row>

     <!-- 数据表格 -->
     <el-table v-loading="loading" :data="tableList" >
        <el-table-column label="序号" align="center" type="index" width="50" />
        <el-table-column
           label="赛道名称"
           align="left"
           prop="competitionTrackName"
           width="300"
           :show-overflow-tooltip="true"
        />
          <el-table-column
           label="赛事名称"
           align="left"
           prop="competitionName"
           width="260"
           :show-overflow-tooltip="true"
        />
         <el-table-column label="二级分类" align="center" prop="competitionTrackType" min-width="100">
            <template #default="scope">
               <dict-tag :options="competition_track_type" :value="scope.row.competitionTrackType" />
            </template>
         </el-table-column>
        <el-table-column
           label="赛事届数"
           align="left"
           prop="competitionSeriesName"
           width="100"
           :show-overflow-tooltip="true"
        />
         <el-table-column label="赛道状态" align="center" prop="checkStatus" min-width="100">
           <template #default="scope">
              <dict-tag :options="check_status" :value="scope.row.checkStatus" />
           </template>
        </el-table-column>
         <el-table-column label="审核意见" align="center" prop="applyReason" min-width="100" show-overflow-tooltip>
            <template #default="scope">
               {{ scope.row.applyReason || '-' }}
            </template>
         </el-table-column>
        <el-table-column min-width="120" label="操作" align="center" fixed="right" class-name="auto-fit-fixed-column">
           <template #default="scope">
              <!-- 只有（草稿）可以删除 -->
              <template v-if="['2','5'].includes(scope.row.checkStatus)">
                 <el-button link type="danger"  @click="handleDelete(scope.row)" v-hasPermi="['competition:competitionTrackInfo:remove']" >删除</el-button>
              </template>
              <!-- （草稿、审核驳回、已撤销发布）可以修改 -->
              <template  v-if="['2','4','5'].includes(scope.row.checkStatus)">
                 <el-button link type="primary" @click="handleUpdate(scope.row,false)" v-hasPermi="['competition:competitionTrackInfo:edit']">修改</el-button>
              </template>
               <template v-if="scope.row.checkStatus == '2'">
                  <el-button link type="success"  @click="handleTask(scope.row)" v-hasPermi="['raceTrack:task:submit']" >提交审核</el-button>
               </template>
              <!-- 详情查看不限制 -->
              <el-button link type="primary"  @click="handleUpdate(scope.row,true)" v-hasPermi="['competition:competitionTrackInfo:query']" >查看</el-button>
           </template>
        </el-table-column>
     </el-table>
     
     <!-- 分页 -->
     <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
     />

     <!-- 赛事表单组件 -->
     <template v-if="open">
        <edit-form 
           v-if="open"
           :checkPackageList="checkPackageList"
           v-model:open="open" 
           :title="title"
           :join-type-arr="join_type"
           :class-request-arr="class_request"
           :only-show="isOnlyShow"
           :file-format-restrictions-arr="file_format_restrictions"
           :works-submit-way-arr="works_submit_way"
           :professional-requirements-arr="professional_requirements"
           @refresh="getList"
           :competition-track-type-arr="competition_track_type"
           @setCompetitionInfo="setCompetitionInfo"
           :row="currentRow"
           />
     </template>
   </div>
 </template>

<script setup name="CompetitionSettingIndex">
import { listCompetitionTracks, deleteCompetitionListById,getCheckPackage } from "@/api/tournament/competition"
import Pagination from '@/components/Pagination'
import RightToolbar from '@/components/RightToolbar'
import DictTag from '@/components/DictTag'
import EditForm from './editForm.vue'
import { useDict } from '@/utils/dict'
import { resetCompetitionDetailSettingState } from './editComponents/useCompetitionDetail';
import { systemTask } from '@/api/business'
import modal from "@/plugins/modal"
// 字典数据
const { 
   check_status, 
   join_type,
   class_request,
   works_submit_way,
   professional_requirements,
   file_format_restrictions,
   competition_track_type,
} = useDict(
  "check_status",
  "join_type",
  "class_request",
  "works_submit_way",
  "professional_requirements",
  "file_format_restrictions",
  "competition_track_type"
)

// 搜索显示控制
let showSearch = $ref(true)

// 表格数据相关
let loading = $ref(false)
let tableList = $ref([])
let total = $ref(0)
let queryParams = $ref({
  pageNum: 1,
  pageSize: 10,
  competitionName: undefined,
  competitionTrackName: undefined,
  checkStatus: undefined,
  competitionType: undefined
})

/** 查询赛事列表 */
function getList() {
  loading = true
  let query = {
    ...queryParams
  }
  listCompetitionTracks(query).then(response => {
    tableList = response.rows
    total = response.total
  }).finally(() => {
    loading = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  tableList = []
  queryParams.pageNum = 1
  getList()
}

/** 处理提交审核 */
function handleTask(row) {
   ElMessageBox.confirm('是否确认操作？', '系统提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
   }).then(() => { 
      loading = true
      systemTask({businessId: row.trackId,auditType: 'raceTrack'}).then(() => {
         ElMessage({
            message: "操作成功",
            type: "success"
         })
         getList()
      }).catch(() => {
         loading = false
      })
   })
}

/** 重置按钮操作 */
let queryRef = ref(null)
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  queryParams = {
    pageNum: 1,
    pageSize: 10,
    competitionName: undefined,
    checkStatus: [],
    competitionTrackName: undefined,
    competitionType: []
  }   
  handleQuery()
}

// 表单操作相关
let open = $ref(false)
let title = $ref("")
let currentCompetitionId = $ref(undefined)
let currentCompetitionSeriesId = $ref(undefined)
let currentRow = $ref(null)
let isOnlyShow = $ref(false)

/** 新增按钮操作 */
function handleAdd() {
  isOnlyShow = false
  resetCompetitionDetailSettingState()
  open = false
  currentCompetitionId = undefined
  currentCompetitionSeriesId = undefined
  currentRow = null
  title = "添加赛事配置"
  // 在打开对话框前重置赛事详情状态
  setTimeout(() => {
    open = true
  }, 100);
}

/** 修改按钮操作 */
function handleUpdate(row, onlyShow) {
  // 提取共同操作到单独函数中
  const openCompetitionDialog = () => {
    isOnlyShow = onlyShow
    open = false
    // 在打开对话框前重置赛事详情状态
    resetCompetitionDetailSettingState()
    currentRow = row;
    title = onlyShow ? "查看赛事配置" : "修改赛事配置" 
    setTimeout(() => {
      open = true
    }, 100);
  }
  if(!onlyShow){
      if(['4'].includes(row.checkStatus)){
         modal.confirm('注意：当前赛道状态，修改内容将导致原有审核结果失效，需重新提交审核。确认继续修改吗？',
            '提示',
            {
               customClass: 'custom-confirm-dialog-long-saishi'
            }
         ).then(openCompetitionDialog).catch(() => {})
         return 
      }
   }
  // 直接打开对话框
  openCompetitionDialog()
}

/** 删除按钮操作 */
function handleDelete(row) {
  ElMessageBox.confirm('是否确认删除？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(function() {
    loading = true
    return deleteCompetitionListById(row.competitionTrackId)
  }).then(() => {
    loading = false
    getList()
    ElMessage.success("删除成功")
  }).catch(() => {
    loading = false
  })
}

// 更新数据赛事的id
function setCompetitionInfo({competitionId, competitionSeriesId}) {
  currentCompetitionId = competitionId
  currentCompetitionSeriesId = competitionSeriesId
}

/** 查询数据包 */
let checkPackageList = $ref([])
function getCheckPackageList() {
  let query = {}
  getCheckPackage(query).then(response => {
    checkPackageList = response.data || []
  })
}

// 初始加载数据
getCheckPackageList()
getList()
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