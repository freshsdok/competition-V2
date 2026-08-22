<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="参赛者名称" prop="userName">
        <el-input
          v-model.trim="queryParams.userName"
          placeholder="请输入参赛者名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="支付状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" placeholder="请选择支付状态" clearable style="width: 160px;">
          <el-option
            v-for="dict in pay_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="团队名称" prop="teamName">
        <el-input
          v-model.trim="queryParams.teamName"
          placeholder="请输入团队名称"
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
      <el-form-item label="赛道" prop="competitionTrackName">
        <el-input
          v-model.trim="queryParams.competitionTrackName"
          placeholder="请输入赛道"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="组别" prop="secondLevelName">
        <el-input
          v-model.trim="queryParams.secondLevelName"
          placeholder="请输入组别"
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
      <el-form-item label="带队老师姓名" prop="leaderTeacherName">
        <el-input
          v-model.trim="queryParams.leaderTeacherName"
          placeholder="请输入带队老师姓名"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="指导教师姓名" prop="guideTeacher">
        <el-input
          v-model.trim="queryParams.guideTeacher"
          placeholder="请输入指导教师姓名"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="角色" prop="competitionRoleNameReq">
        <el-select v-model="queryParams.competitionRoleNameReq" placeholder="请选择角色" clearable style="width: 160px;" multiple>
          <el-option label="队长" value="队长"/>
          <el-option label="队员" value="队员"/>
          <el-option label="指导教师" value="指导教师"/>
        </el-select>
      </el-form-item>
      <el-form-item label="报名时间范围" style="width: 450px">
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
      <el-button type="primary" :loading="loadingAll" @click="handleExportAll" v-hasPermi="['competition:competitionApply:export']">全部导出</el-button>
      <el-button type="primary" :loading="loadingFilter" @click="handleExport" v-hasPermi="['competition:competitionApply:export']">检索结果导出</el-button>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" 
              :data="contentList" 
              :header-cell-style="{
                fon
              }"
              row-key="id">
      <el-table-column label="参赛者名称" align="left" prop="userName" width="90" />
      <el-table-column label="支付状态" align="left" prop="payStatus" width="76">
        <template #default="scope">
            <dict-tag :options="pay_status" :value="scope.row.payStatus" />
        </template>
      </el-table-column>
      <el-table-column label="赛道" align="left" prop="competitionTrackName" min-width="100" >
        <template #default="scope">
          <el-tooltip
            content="查看"
            placement="top">
             <template #content>
               <ul>
                 <li>赛道: {{ scope.row.competitionTrackName || '-' }}</li>
                 <li>团队编号: {{ scope.row.teamCode || '-' }}</li>
                 <li>团队名称: {{ scope.row.teamName || '-' }}</li>
                 <li>赛事名称: {{ scope.row.competitionName || '-' }}</li>
               </ul>
             </template>
             <div class="text-num-wrapper">
                <el-icon class="icon"><View /></el-icon>
                <span class="text-num">{{ scope.row.competitionTrackName || '-' }}</span>
              </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="组别" align="left" prop="secondLevelName" min-width="100">
        <template #default="scope">
          <el-tooltip
            content="查看"
            placement="top">
             <template #content>
               <ul>
                 <li>组别: {{ scope.row.secondLevelName || '-' }}</li>
                 <li>团队编号: {{ scope.row.teamCode || '-' }}</li>
                 <li>团队名称: {{ scope.row.teamName || '-' }}</li>
                 <li>赛事名称: {{ scope.row.competitionName || '-' }}</li>
               </ul>
             </template>
             <div class="text-num-wrapper">
                <el-icon class="icon"><View /></el-icon>
                <span class="text-num">{{ scope.row.secondLevelName || '-' }}</span>
              </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="联系电话" align="left" prop="phone" width="110" />
      <el-table-column label="邮箱" align="left" prop="email" min-width="120" />
      <el-table-column label="报名时间" align="left" prop="registrationTime" width="156" />
      <el-table-column label="带队老师" align="left" prop="leaderTeacherName" width="80" show-overflow-tooltip/>
      <el-table-column label="角色" align="left" prop="competitionRoleName" width="90" show-overflow-tooltip/>
      <el-table-column label="操作" align="center" width="140" fixed="right">
        <template #default="scope">
          <!-- 只有（待审核）提交审核 -->
          <template v-if="scope.row.checkStatus == '2'">
            <el-button link type="success"  @click="handleTask(scope.row)" v-hasPermi="['apply:task:submit']" >提交审核</el-button>
          </template>
          <el-tooltip content="查看" placement="top">
            <el-button link type="primary" icon="View" @click="handleUpdate(scope.row)" v-hasPermi="['competition:competitionApply:query']"></el-button>
          </el-tooltip>
          <template v-if="scope.row.teamMemberOldDateList && scope.row.teamMemberOldDateList.length > 0">
            <el-tooltip content="信息变更记录" placement="top">
              <el-button type="warning"
                        link
                        icon="UserFilled"
                        @click="showChangeLog(scope.row)"
                        v-hasPermi="['competition:competitionApply:query']"
                      ></el-button>
            </el-tooltip>
          </template>
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

    <!-- 详情展示对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <SignUpDetail :info="form" 
                    :professionalRequirements="professional_requirements"
                    :classRequest="class_request"
                    :checkStatus="check_status" 
                    :competitionTypeArr="competition_type"
                    :joinTypeArr="join_type"
                    :payStatusArr="pay_status"
                    :competitionTrackArr="competition_track"
                    :competitionGroupArr="competition_group"
                    :realNameAuthStatusArr="real_name_auth_status"
                    :disabled="true" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>   
    <ChangeLog  ref="changeLogRef" 
                :changeTypeArr="change_type"
                v-model:visible="settingVisible"
                :detail-data="changeData"/>
  </div>
</template>

<script setup name="SignUpIndex">
import { competitionApplyList} from "@/api/tournament/tournament"
import { useDict } from '@/utils/dict'
import { addDateRangeSAE, parseTime } from "@/utils/ruoyi"
import { download } from '@/utils/request'
import { cloneDeep } from 'lodash-es';
import { systemTask } from '@/api/business'
import SignUpDetail from "./detail.vue"
import ChangeLog from "./changeLog.vue"
import { exportCompetitionApplyList } from '@/api/tournament/tournament'
import modal from "@/plugins/modal";
const router = useRouter()
const { class_request,check_status,
  real_name_auth_status,
  professional_requirements,
  competition_type,
  join_type,
  pay_status,
  competition_track,
  change_type,
  competition_group } 
  = useDict('class_request',
  'check_status',
  'join_type',
  'competition_type',
  'real_name_auth_status',
  'professional_requirements',
  'pay_status',
   "competition_track",
   "change_type",
   "competition_group") 
// 表单引用
const contentRef = ref(null)
const queryRef = ref(null)

const contentList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {
    userName: null,
    employeeCode: null,
    idCard: null,
    phone: null,
    email: null,
    orgName: null,
    profession: null,
    classInfo: null,
    registrationTime: null,
    checkStatus: null,
    realNameAuthStatus: null,
    payStatus: null,
    teamName: null,
    competitionName: null,
    competitionType: null,
    joinType: null,
    guideTeacherName: null,
    competitionTrackName: null,
    secondLevelName: null,
    leaderTeacherName: null,
    guideTeacher: null,
  },
  queryParams: {
      pageNum: 1,
      pageSize: 10,
      userName: null,
      employeeCode: null,
      idCard: null,
      phone: null,
      email: null,
      orgName: null,
      profession: null,
      classInfo: null,
      checkStatus: null,
      realNameAuthStatus: null,
      payStatus: null,
      teamName: null,
      competitionName: null,
      competitionType: null,
      competitionTrackName: null,
      secondLevelName: null,
      leaderTeacherName: null,
      guideTeacher: null,
      competitionRoleNameReq: [],
    },
  rules: {}
})

const { queryParams, form, rules } = toRefs(data)

/** 查询组件库信息列表 */
let dateRange = ref([])
function getList() {
  loading.value = true
  let arr = cloneDeep(queryParams.value)
  let query = addDateRangeSAE(arr, dateRange.value, 'registrationStartTime', 'registrationEndTime')
  if(query?.competitionRoleNameReq){
    query.competitionRoleNameReq = query.competitionRoleNameReq.join(',')
  }
  competitionApplyList({pageNum:query.pageNum,pageSize:query.pageSize},query).then(response => {
    contentList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(error => {
    loading.value = false
  })
}

/** 处理提交审核 */
function handleTask(row) {
   ElMessageBox.confirm('是否确认操作？', '系统提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
   }).then(() => { 
      loading.value = true
      systemTask({businessId: row.memberId,auditType: 'apply'}).then(() => {
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

// 组件挂载后执行
onMounted(() => {
  console.log('queryParams.va1111111111lue')
  getList()
})


/** 变更记录按钮操作 */
let changeData = $ref({})
let settingVisible = $ref(false)
function showChangeLog(row) {
  changeData = row.teamMemberOldDateList || []
  settingVisible = true
}


// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
   console.log('222222222.va1111111111lue')
  form.value = {}
  if (contentRef.value) {
    contentRef.value.resetFields()
  }
}

/** 搜索按钮操作 */
function handleQuery() {
     console.log('33333.va1111111111lue')
  contentList.value = []
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
     console.log('rrrrrr.va1111111111lue')
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  dateRange.value = []
  handleQuery()
}

/** 导出按钮操作 */
let loadingAll = $ref(false)
function handleExportAll() {
  loadingAll = true
  exportCompetitionApplyList({exportType:'all'}).then((response) => {
    console.log(response,'xxxxxxxxx')
    loadingAll = false
    if(response.code === 200){
      goExportPage()
    }
  }).catch(() => {
    loadingAll = false
  })
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
let loadingFilter = $ref(false)
function handleExport() {
  let query = cloneDeep(queryParams.value)
  query = addDateRangeSAE(query, dateRange.value, 'registrationStartTime', 'registrationEndTime')
  if(query?.pageSize){
    delete query.pageSize
  }
  if(query?.pageNum){
    delete query.pageNum
  }
  if(query?.competitionRoleNameReq){
    query.competitionRoleNameReq = query.competitionRoleNameReq.join(',')
  }
  loadingFilter = true
  exportCompetitionApplyList({exportType:'filter',...query}).then((response) => {
    loadingFilter = false
    if(response.code === 200){
      goExportPage()
    }
  }).catch(() => {
    loadingFilter = false
  })
}

/** 详情按钮操作 */
function handleUpdate(row) {
       console.log('444.va1111111111lue')
  reset()
  // 回显数据，与表格字段对应
  form.value = row
  open.value = true
  title.value = "查看报名详情"
}
</script>
<style scoped lang="scss">
.text-num-wrapper {
  // 超出2行省略号
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box !important;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  .icon{
    color: #409EFF;
  }
  .text-num {
    margin-left: 2px;
  }
}
</style>
