<template>
   <div class="app-container app-cell-form">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
         <el-row>
            <el-form-item label="赛事名称" prop="competitionName">
                  <el-input
                     v-model.trim="queryParams.competitionName"
                     placeholder="请输入赛事名称"
                     style="width: 160px;"
                     clearable/>
            </el-form-item>
            <el-form-item label="赛事届数" prop="competitionSeriesName">
               <el-input
                  v-model.trim="queryParams.competitionSeriesName"
                  placeholder="请输入赛事届数"
                  style="width: 160px;"
                  clearable/>
            </el-form-item>
         <el-form-item label="赛事状态" prop="checkStatus">
            <el-select v-model="queryParams.checkStatus" placeholder="请选择赛事状态" clearable style="width: 160px;" multiple collapse-tags>
               <el-option
                  v-for="dict in competition_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </el-select>
         </el-form-item>
          <el-form-item label="赛事类型" prop="competitionType">
               <el-select v-model="queryParams.competitionType" placeholder="请选择赛事类型" clearable style="width: 160px;">
                  <el-option
                     v-for="dict in competition_type"
                     :key="dict.value"
                     :label="dict.label"
                     :value="dict.value"
                  />
               </el-select>
         </el-form-item>
         <el-form-item label="赛事时间" style="width: 450px">
               <el-date-picker
                  v-model="dateRangeSaiShi"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
               ></el-date-picker>
         </el-form-item>
         <el-form-item label="主办方名称" prop="organizer">
            <el-input
               v-model.trim="queryParams.organizer"
               placeholder="请输入主办方名称"
               style="width: 160px;"
               clearable/>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
         </el-row>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button
               type="primary"
               plain
               icon="Plus"
               @click="handleAdd"
               v-hasPermi="['competition:competitionManager:add']"
            >新增</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="tableList" >
         <el-table-column label="序号" align="center" type="index" width="50" />
         <el-table-column
            label="赛事编码"
            align="center"
            prop="competitionCode"
            width="120"
            :show-overflow-tooltip="true"
         />         
         <el-table-column
            label="赛事名称"
            align="left"
            prop="competitionName"
            width="260"
            :show-overflow-tooltip="true"
         />
         <el-table-column
            label="赛事届数"
            align="left"
            prop="competitionSeriesName"
            width="100"
            :show-overflow-tooltip="true"
         />
         <el-table-column label="赛事状态" align="center" prop="checkStatus" min-width="100">
            <template #default="scope">
               <dict-tag :options="competition_status" :value="scope.row.checkStatus" />
            </template>
         </el-table-column>
         <el-table-column label="审核意见" align="center" prop="applyReason" min-width="160" show-overflow-tooltip>
            <template #default="scope">
               {{ scope.row.applyReason || '-' }}
            </template>
         </el-table-column>
         <el-table-column label="对外发布时间" align="center" prop="publishTime" min-width="120">
            <template #default="scope">
               <span>{{ parseTime(scope.row.publishTime, '{y}-{m}-{d}') }}</span>
            </template>
         </el-table-column>
         <el-table-column label="赛事开始时间" align="center" prop="competitionStartTime" min-width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.competitionStartTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
            </template>
         </el-table-column>
         <el-table-column label="赛事结束时间" align="center" prop="competitionEndTime" min-width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.competitionEndTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
            </template>
         </el-table-column>
         <el-table-column label="赛事类型" align="center" prop="competitionType" width="100">
            <template #default="scope">
               <dict-tag :options="competition_type" :value="scope.row.competitionType" />
            </template>
         </el-table-column>
         <el-table-column label="主办方名称" :show-overflow-tooltip="true" align="left" prop="organizer" width="280" />
         <el-table-column min-width="230" label="操作" align="center" fixed="right" class-name="auto-fit-fixed-column">
            <template #default="scope">
               <!-- 详情查看不限制 -->
               <el-tooltip content="查看" placement="top">
                  <el-button link type="primary" icon="View" @click="handleUpdate(scope.row,true)" v-hasPermi="['competition:competitionManager:query']" ></el-button>
               </el-tooltip>
               <!-- （草稿、审核驳回、已撤销发布）可以修改 -->
               <template  v-if="['1','4','5','9'].includes(scope.row.checkStatus)">
                  <el-tooltip content="修改" placement="top">
                     <el-button link type="success" icon="Edit" @click="handleUpdate(scope.row,false)" v-hasPermi="['competition:competitionManager:edit']"></el-button>
                  </el-tooltip>
               </template>
               <el-tooltip content="权限配置" placement="top">
                  <el-button link type="success" icon="Setting" @click="handleSetting(scope.row)" v-hasPermi="['competition:competitionOperationConfig:query']"></el-button>
               </el-tooltip>
               <!-- （审核通过、已撤销发布）可以发布 -->
               <template v-if="['4','9'].includes(scope.row.checkStatus)">
                  <el-tooltip content="发布" placement="top">
                      <el-button link type="success" icon="Promotion" @click="handleStatus(scope.row,'6')" v-hasPermi="['competition:competitionManager:editStatus']" ></el-button>
                  </el-tooltip>
               </template>
               <!-- （已发布：未开始的）可以撤销发布 -->
               <template v-if="['6'].includes(scope.row.checkStatus)">
                  <el-tooltip content="撤销发布" placement="top">
                     <el-button link type="warning"  @click="handleStatus(scope.row,'9')" v-hasPermi="['competition:competitionManager:editStatus']">
                        <svg-icon icon-class="enter" style="font-size: 12px;" />
                     </el-button>
                  </el-tooltip>
               </template>               
               <!-- 只有（草稿）提交审核 -->
               <template v-if="scope.row.checkStatus == '1'">
                  <el-tooltip content="提交审核" placement="top">
                     <el-button link type="success" icon="Finished"  @click="handleTask(scope.row)" v-hasPermi="['race:task:submit']" ></el-button>
                  </el-tooltip>
               </template>
               <!-- 只有（草稿）可以删除 -->
               <template v-if="scope.row.checkStatus == '1'">
                  <el-tooltip content="删除" placement="top">
                     <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['competition:competitionManager:remove']" ></el-button>
                  </el-tooltip>
               </template>
               <!-- （已发布：）可以启用 -->
               <!-- <template v-if="['6'].includes(scope.row.checkStatus)">
                  <el-button link type="success"  @click="handleStatus(scope.row,'7')" v-hasPermi="['competition:competitionManager:editStatus']" >开赛</el-button>
               </template> -->
               <!-- （进行中）可以结束 -->
               <!-- <template v-if="['7'].includes(scope.row.checkStatus)">
                  <el-button link type="danger"  @click="handleStatus(scope.row,'8')" v-hasPermi="['competition:competitionManager:editStatus']" >结束</el-button>
               </template> -->
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

      <!-- 引入赛事表单组件 -->
      <template v-if="open">
         <edit-form 
            v-if="open"
            v-model:open="open" 
            :title="title"
            :competition-id="currentCompetitionId"
            :competition-series-id="currentCompetitionSeriesId"
            :competition-type-arr="competition_type"
            :score-way-arr="score_way"
            :only-show="isOnlyShow"
            @refresh="getList"
            @setCompetitionInfo="setCompetitionInfo"
            />
      </template>
    </div>

  <!-- 权限配置弹窗 -->
  <PermissionSetting
    v-model:visible="settingVisible"
    ref="PermissionSettingRef"
  />
</template>

<script setup name="TournamentCompetitionIndex">
import { listCompetition, removeCompetitionMainInfo,updateCompetitionInfoStatus } from "@/api/tournament/competition"
import Pagination from '@/components/Pagination'
import { defineAsyncComponent } from "vue"
import RightToolbar from '@/components/RightToolbar'
import DictTag from '@/components/DictTag'
import EditForm from './editForm.vue'
import { addDateRangeSAE, parseTime } from "@/utils/ruoyi"
import { useDict } from '@/utils/dict'
import { resetCompetitionDetailState } from './editComponents/useCompetitionDetail';
import { systemTask } from '@/api/business'
import modal from "@/plugins/modal"
// 字典数据
const { competition_status, 
   competition_type,
   score_way
} = useDict("competition_status",
   "competition_type",
   "score_way")

// 搜索显示控制
const showSearch = ref(true)


// 日期范围选择器

const dateRangeSaiShi = ref([])
// 表格数据相关
const loading = ref(false)
const tableList = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  competitionName: undefined,
  organizer: undefined,
  checkStatus: [],
  competitionType: undefined,
  competitionSeriesName: undefined
})

/** 查询赛事列表 */
function getList() {
  loading.value = true
  let query = addDateRangeSAE(queryParams.value, dateRangeSaiShi.value, 'competitionStartTime', 'competitionEndTime')
  query = {
    ...query,
    checkStatus: query.checkStatus.join(',')
  }
  listCompetition(query).then(response => {
    tableList.value = response.rows
    total.value = response.total
  }).finally(() => {
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
tableList.value = []
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
const queryRef = ref()
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  dateRangeSaiShi.value = []
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    competitionName: undefined,
    organizer: undefined,
    checkStatus: [],
    competitionType: undefined,
    competitionSeriesName: undefined
  }   
  handleQuery()
}

/** 处理提交审核 */
function handleTask(row) {
   ElMessageBox.confirm('是否确认操作？', '系统提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
   }).then(() => { 
      loading.value = true
      systemTask({businessId: row.competitionId,auditType: 'race'}).then(() => {
         ElMessage({
            message: "操作成功",
            type: "success"
         })
         getList()
      }).catch(() => {
         loading.value = false
         ElMessage({
            message: "操作失败",
            type: "error"
         })
      })
   })
}

/** 处理状态变更 */
function handleStatus(row,status) {
   ElMessageBox.confirm('是否确认操作？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    loading.value = true
    updateCompetitionInfoStatus({
      competitionId: row.competitionId,
      competitionSeriesId: row.competitionSeriesId,
      checkStatus: status
    }).then(() => {
      ElMessage({
        message: "操作成功",
        type: "success"
      })
      getList()
    }).catch(() => {
      loading.value = false
      ElMessage({
        message: "操作失败",
        type: "error"
      })
    })
  }) 
}

// 权限配置相关
const settingVisible = ref(false)
const PermissionSettingRef = ref(null)
const handleSetting = (row) => {
   settingVisible.value = true
   PermissionSettingRef?.value?.initDialog(row.competitionSeriesId)
}

// 引入权限配置组件
const PermissionSetting = defineAsyncComponent(() => import('./setting.vue'))


// 表单操作相关
const open = ref(false)
const title = ref("")
const currentCompetitionId = ref(undefined)
const currentCompetitionSeriesId= ref(undefined)
/** 新增按钮操作 */
let isOnlyShow = $ref(false)
function handleAdd() {
  isOnlyShow = false
  resetCompetitionDetailState()
  open.value = false
  currentCompetitionId.value = undefined
  currentCompetitionSeriesId.value = undefined
  title.value = "添加赛事信息"
  // 在打开对话框前重置赛事详情状态
   setTimeout(() => {
      open.value = true
   }, 100);
}
/** 修改按钮操作 */
function handleUpdate(row,onlyShow) {
   // 提取共同操作到单独函数中
   const openCompetitionDialog = () => {
      isOnlyShow = onlyShow
      open.value = false
      // 在打开对话框前重置赛事详情状态
      resetCompetitionDetailState()
      currentCompetitionId.value = row.competitionId;
      currentCompetitionSeriesId.value = row.competitionSeriesId;
      console.log(row,currentCompetitionId.value,currentCompetitionSeriesId.value,'currentCompetitionSeriesId.value')
      title.value = onlyShow ? "查看赛事信息" : "修改赛事信息" 
      setTimeout(() => {
         open.value = true
      }, 100);
   }

   if(!onlyShow){
      if(['4','9'].includes(row.checkStatus)){
         modal.confirm('注意：当前赛事状态，修改内容将导致原有审核结果失效，需重新提交审核。确认继续修改吗？',
            '提示',
            {
               customClass: 'custom-confirm-dialog-long-saishi'
            }
         ).then(openCompetitionDialog).catch(() => {})
         return 
      }
   }
   
   // 直接打开对话框（无需确认的情况）
   openCompetitionDialog()
}
// 更新数据赛事的id
function setCompetitionInfo({competitionId,competitionSeriesId}){
  currentCompetitionId.value = competitionId
  currentCompetitionSeriesId.value = competitionSeriesId
}


/** 删除按钮操作 */
function handleDelete(row) {
  ElMessageBox.confirm('是否确认删除？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(function() {
   loading.value = true
    return removeCompetitionMainInfo({
      competitionId: row.competitionId,
      competitionSeriesId: row.competitionSeriesId
    })
  }).then(() => {
   loading.value = false
    getList()
    ElMessage.success("删除成功")
  }).catch(() => {
   loading.value = false
  })
}

// 初始加载数据
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
