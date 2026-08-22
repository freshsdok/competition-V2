<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="团队名称" prop="teamName">
        <el-input
          v-model.trim="queryParams.teamName"
          placeholder="请输入团队名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="队长姓名" prop="captainName">
        <el-input
          v-model.trim="queryParams.captainName"
          placeholder="请输入队长姓名"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="审核状态" prop="checkStatus">
        <el-select v-model="queryParams.checkStatus" placeholder="请选择审核状态" clearable style="width: 160px;">
          <el-option
            v-for="dict in check_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="赛事名称" prop="competitionName">
        <el-input
          v-model.trim="queryParams.competitionName"
          placeholder="请输入赛事名称"
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
      <el-form-item label="队员数量" >
        <div class="flex-center-input">
           <el-input
           v-model.trim="queryParams.teamNumStart"
           placeholder="最小数量"
           style="width: 160px;"
           type="number"
           clearable/>
        <span class="c-line">到</span>
        <el-input
           v-model.trim="queryParams.teamNumEnd"
           placeholder="最大数量"
           style="width: 160px;"
           type="number"
           clearable/>
        </div>
      </el-form-item>
      <el-form-item label="创建时间范围" style="width: 450px">
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
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" 
              :data="contentList">
      <el-table-column label="团队编号" align="left" prop="teamCode" min-width="100" />
      <el-table-column label="团队名称" align="left" prop="teamName" min-width="120" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.teamName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="队长" align="left" prop="captainName" width="70" >
        <template #default="scope">
          <span>{{ scope.row.captainName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="队员" align="left" prop="teamMemberRelaList" width="120"  show-overflow-tooltip>
        <template #default="scope">
          <span v-for="(item,index) in scope.row.teamMemberRelaList" :key="index">
            <span>{{ item.userName || '-' }}</span>
            <span v-if="index < scope.row.teamMemberRelaList.length - 1">,</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="带队老师" align="left" prop="leaderTeacherName" width="80" show-overflow-tooltip/>
      <el-table-column label="指导教师" align="left" prop="guideTeacher" width="120"  show-overflow-tooltip>
        <template #default="scope">
           <el-tooltip placement="top">
            <template #content>
              <div v-if="scope.row?.guidTeacherList">
                <div v-for="(item,index) in scope.row.guidTeacherList" :key="index">
                  <span style="margin-right: 10px;">姓名：{{ item.guideTeacher || '-' }}</span>
                  <span style="margin-right: 10px;">手机号：{{ item.guideTeacherPhone || '-' }}</span>
                  <span>邮箱：{{ item.guideTeacherEmail || '-' }}</span>
                </div>
              </div>
            </template>
            <div v-if="scope.row?.guidTeacherList">
              <span v-for="(item,index) in scope.row.guidTeacherList" :key="index">
                <span>{{ item.guideTeacher || '-' }}</span>
                <span v-if="index < scope.row.guidTeacherList.length - 1">,</span>
              </span>
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="队员数量" align="left" prop="teamNum" width="76" >
        <template #default="scope">
          <span>{{ scope.row.teamNum || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="赛道" align="left" prop="competitionName" min-width="100" >
        <template #default="scope">
          <el-tooltip
            placement="top">
             <template #content>
               <ul>
                 <li>赛道: {{ scope.row.competitionTrackName || '-' }}</li>
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
      <el-table-column label="组别" align="left" prop="secondLevelName" min-width="100" >
        <template #default="scope">
          <el-tooltip
            placement="top">
             <template #content>
               <ul>
                 <li>组别: {{ scope.row.secondLevelName || '-' }}</li>
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

      <!-- <el-table-column label="赛道名称" align="left" prop="competitionTrackName" min-width="200"  show-overflow-tooltip/> -->
      <!-- <el-table-column label="组别" align="left" prop="secondLevelName" min-width="200"  show-overflow-tooltip/> -->
      <el-table-column label="审核状态" align="center" prop="checkStatus"width="90">
        <template #default="scope">
            <dict-tag :options="check_status" :value="scope.row.checkStatus" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="left" prop="createTime" width="156" />
      <el-table-column label="操作" align="center" width="140" fixed="right">
        <template #default="scope">
          <!-- 只有（待审核）提交审核 -->
          <template v-if="scope.row.checkStatus == '2'">
            <el-button link type="success"  @click="handleTask(scope.row)" v-hasPermi="['team:task:submit']" >提交审核</el-button>
          </template>
          <el-tooltip content="查看" placement="top">
            <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['competition:teamManager:query']"></el-button>
          </el-tooltip>
          <template v-if="scope.row.teamManagerInfoOldData && scope.row.teamManagerInfoOldData.length > 0">
            <el-tooltip content="人员变更记录" placement="top">
              <el-button type="warning"
                        link
                        icon="UserFilled"
                        @click="showChangeLog(scope.row)"
                        v-hasPermi="['competition:teamManager:query']"
                      ></el-button>
            </el-tooltip>
          </template>
          <!-- 只有（审核通过）修改 -->
          <template v-if="scope.row.checkStatus == '2' || scope.row.checkStatus == '5'">
            <el-button
              type="primary"
              link
              @click="handleEdit(scope.row)"
              v-hasPermi="['competition:teamManager:edit']"
            >修改</el-button>
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
      <TeamDetail v-model:info="form" 
                :checkStatus="check_status" 
                :competitionTypeArr="competition_type"
                :payStatusArr="pay_status"
                :disabled="isDetail"/>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
          <el-button v-if="!isDetail" type="primary" @click="submitForm">保 存</el-button>
        </div>
      </template>
    </el-dialog>

    <ChangeLog  ref="changeLogRef" 
                v-model:visible="settingVisible"
                :detail-data="changeData"
                :changeTypeArr="change_type"/>
  </div>
</template>

<script setup name="TeamIndex">
import { teamManagerList, updateTeamManagerInfo } from "@/api/tournament/tournament"
import { useDict } from '@/utils/dict'
import { addDateRangeSAE } from "@/utils/ruoyi"
import { ElMessage } from 'element-plus'
import { systemTask } from '@/api/business'
import TeamDetail from "./detail.vue"
import ChangeLog from "./changeLog.vue"
const { check_status, competition_type,pay_status,change_type } = useDict('check_status', 'competition_type','pay_status','change_type')
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
    teamCode: null,
    teamName: null,
    teamDesc: null,
    captainName: null,
    employeeCode: null,
    teamNum: null,
    competitionName: null,
    competitionType: null,
    checkStatus: null,
    createTime: null
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    teamName: null,
    captainName: null,
    employeeCode: null,
    teamNumStart: null,
    teamNumEnd: null,
    competitionName: null,
    competitionType: null,
    checkStatus: null,
    leaderTeacherName: null,
    guideTeacher: null,
  }
})

const { queryParams, form } = toRefs(data)
/** 查询组件库信息列表 */
let dateRange = ref([])
function getList() {
  loading.value = true
  let query = addDateRangeSAE(queryParams.value, dateRange.value, 'createStartTime', 'createEndTime')
  teamManagerList({pageNum:query.pageNum,pageSize:query.pageSize},query).then(response => {
    contentList.value = response.rows
    total.value = response.total
    loading.value = false
    console.log(contentList.value,'contentList.value-contentList.value ')
  }).catch(error => {
    loading.value = false
  })
}

/** 变更记录按钮操作 */
let changeData = $ref({})
let settingVisible = $ref(false)
function showChangeLog(row) {
  changeData = row.teamManagerInfoOldData || []
  settingVisible = true
}

// 获取参赛真实名字
function getAxisIdPropName(e) {
  if (!e) return ''
  let arr = e.filter(item => item.userName).map(item => item.userName)
  return arr.join('，')
}

/** 处理提交审核 */
function handleTask(row) {
   ElMessageBox.confirm('是否确认操作？', '系统提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
   }).then(() => { 
      loading.value = true
      systemTask({businessId: row.teamId,auditType: 'team'}).then(() => {
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

const isDetail = ref(true) // 是否为详情模式

// 组件挂载后执行
onMounted(() => {
  getList()
})

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    teamCode: null,
    teamName: null,
    teamDesc: null,
    captainName: null,
    employeeCode: null,
    teamNum: null,
    competitionName: null,
    competitionType: null,
    checkStatus: null,
    createTime: null
  }
  if (contentRef.value) {
    contentRef.value.resetFields()
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  contentList.value = []
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  contentList.value = []
  queryParams.value.teamNumStart = null
  queryParams.value.teamNumEnd = null
  dateRange.value = []
  handleQuery()
}



/** 详情按钮操作 */
function handleDetail(row) {
  reset()
  isDetail.value = true
  // 回显数据，与表格字段对应
  form.value = row
  open.value = true
  title.value = "查看团队详情"
}

/** 修改按钮操作 */
function handleEdit(row) {
  reset()
  isDetail.value = false
  // 回显数据，与表格字段对应
  form.value = {
    teamCode: row.teamCode,
    teamName: row.teamName,
    teamDesc: row.teamDesc,
    captainName: row.captainName,
    employeeCode: row.employeeCode,
    teamNum: row.teamNum,
    competitionName: row.competitionName,
    competitionType: row.competitionType,
    checkStatus: row.checkStatus,
    createTime: row.createTime
  }
  open.value = true
  title.value = "修改团队信息"
}

/** 提交表单 */
function submitForm() {
  let params ={
    teamCode: form.value.teamCode,
    teamName: form.value.teamName,
    teamDesc: form.value.teamDesc,
  }
  updateTeamManagerInfo(params).then(response => {
    ElMessage.success('修改成功')
    open.value = false
    getList()
  }).catch(error => {
    ElMessage.error('修改失败')
  })
}

// 移除导出功能，因为当前页面不需要
</script>

<style scoped lang="scss">
.flex-center-input {
  display: flex;
  align-items: center;
  .c-line {
    margin: 0 6px;
  }
}
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
