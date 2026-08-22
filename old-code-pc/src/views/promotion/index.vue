<template>
<div class="global-page pb-10">
  <div class="container-custom rounded-[10px]">
    <div class="mt-5">
      <div class="text-[28px] font-bold text-[#303133]">我的晋级队伍</div>
      <div class="text-[14px] text-[#999999]">
        当前共有{{ competitionTabs.length }}个比赛的晋级队伍
        <template v-if="competitionTabs.length > 1">，请切换标签页查看往年及今年的晋级信息</template>
      </div>
    </div>
    <div class="mt-2 border border-[#e4e7ed] bg-[#ffffff]">
      <!-- 比赛Tab栏 -->
      <div class="competition-tabs mb-5" v-loading="tabsLoading">
        <div class="tabs-wrapper">
          <div
            v-for="item in competitionTabs"
            :key="item.promotedId"
            :class="['tab-item', { active: activeCompetitionId === item.competitionSeriesId }]"
            @click="handleTabChange(item.competitionSeriesId)"
          >
            <span class="tab-name">{{ item.competitionName }}</span>
            <span :class="['tab-status', getStatusClass(item.competitionApplyStatus)]">{{ getStatusLabel(item.competitionApplyStatus) }}</span>
          </div>
        </div>
      </div>
      <div v-if="!tabsLoading && competitionTabs.length === 0" class="text-center py-16 text-[#909399] text-sm">暂无公示数据</div>
      <template v-if="activeCompetitionId">
      <div class="px-4">
        <!-- 页面标题 -->
        <div class="flex justify-between items-start mb-5">
          <div class="header-left">
            <h2 class="text-[26px] font-bold text-[#303133] m-0 mb-2">{{ currentCompetition?.competitionName || '-' }}</h2>
            <div class="text-sm text-[#909399]">
              <span>共 {{ currentCompetition.teamNum ??  '-' }} 支晋级队伍，其中 {{ currentCompetition.applyTeamNum ??  '-' }} 支已报名，{{ getUnapplyTeamNum(currentCompetition.teamNum, currentCompetition.applyTeamNum) }} 支未报名，</span>
              <span class="text-[#E6A23C]">报名开始时间：{{ currentCompetition?.applyStartTime || '-' }} </span>
              <span class="text-[#E6A23C] ml-3">报名截止时间：{{ currentCompetition?.applyEndTime || '-' }}</span>
            </div>
          </div>
          <div class="header-right">
            <el-button type="success" icon="Download" :loading="exportLoading" @click="handleExport" :disabled="isEditing">导出</el-button>
            <el-button type="primary" icon="Check" :loading="signUpLoading" @click="handleSignUp" v-if="isInPublicityPeriod && hasUnapplyTeams" :disabled="isEditing">报名</el-button>
          </div>
        </div>

        <!-- 公示说明 -->
        <div class="mb-5 border border-[#E6A23C] rounded p-3 bg-[rgba(255,247,230,0.6)]" v-if="currentCompetition?.promotedHint">
          <div class="ql-container ql-snow !my-[0]">
            <div class="rich-content ql-editor" v-html="currentCompetition?.promotedHint || ''">
            </div>
          </div>
        </div>

        <!-- 筛选区域 -->
        <el-form :model="queryParams"
                  ref="queryRef"
                  :inline="true"
                  class="pt-6 bg-[#ffffff] rounded"
                  label-width="68px">
          <el-form-item label="学校" prop="schoolName">
            <el-input
              v-model.trim="queryParams.schoolName"
              placeholder="请输入学校"
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
          <el-form-item label="学生姓名" prop="userName">
            <el-input
              v-model.trim="queryParams.userName"
              placeholder="请输入学生姓名"
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
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 操作按钮 -->
        <div class="w-full flex justify-end gap-2 my-4">
          <template v-if="isInPublicityPeriod && hasUnapplyTeams">
            <template v-if="!isEditing">
              <el-button type="primary" icon="Edit" @click="handleEdit">编辑</el-button>
            </template>
            <template v-else>
              <el-button type="primary" icon="Check" @click="handleSave" :loading="saveLoading" :disabled="hasInvalidTeacherRows">保存所有修改</el-button>
              <el-button icon="Close" @click="handleCancel">取消</el-button>
            </template>
          </template>
        </div>

        <!-- 列表 -->
        <div class="bg-[#ffffff] p-4">
          <el-table
            v-loading="loading"
            :data="tableList"
            row-key="teamCode"
            height="400px"
            :cell-class-name="cellClassName"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" align="center" :selectable="isSelectable" />
            <el-table-column label="序号" align="center" type="index" width="60" />
            <el-table-column label="学校" align="left" prop="schoolName" min-width="150" show-overflow-tooltip>
              <template #default="scope">
                <span>{{ scope.row.schoolName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="赛道" align="left" min-width="150" show-overflow-tooltip sortable>
              <template #default="scope">
                {{ scope.row.competitionTrackName }}{{ scope.row.secondLevelName ? ' / ' + scope.row.secondLevelName : '' }}
              </template>
            </el-table-column>
            <el-table-column label="报名状态" align="center" prop="applyStatus" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.applyStatus == '1' ? 'success' : 'info'">
                  {{ scope.row.applyStatus == '1' ? '已报名' : '未报名' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="学生姓名（可调整顺序）" align="left" min-width="280">
              <template #default="scope">
                <template v-if="isEditing && scope.row.applyStatus != '1'">
                  <div :class="['student-edit-list', 'student-sortable-' + scope.row.teamCode]" :data-row-id="scope.row.teamCode">
                    <div
                      v-for="(student, idx) in (scope.row.playerInfoList || [])"
                      :key="scope.row.teamCode + '-' + idx + '-' + student.userName"
                      class="student-edit-item"
                    >
                      <span class="text-[#606266] text-[13px] min-w-[24px]">{{ idx + 1 }}、</span>
                      <span class="text-[#303133] text-[13px] min-w-[60px]">{{ student.userName }}</span>
                      <el-button
                        link
                        type="primary"
                        :icon="Top"
                        :disabled="idx === 0"
                        @click="moveStudent(scope.row, idx, -1)"
                      />
                      <el-button
                        link
                        type="primary"
                        :icon="Bottom"
                        :disabled="idx === (scope.row.playerInfoList ? scope.row.playerInfoList.length - 1 : 0)"
                        @click="moveStudent(scope.row, idx, 1)"
                      />
                    </div>
                  </div>
                </template>
                <span v-else>{{ formatStudentNames(scope.row.playerInfoList) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="指导教师1" align="left" min-width="120">
              <template #default="scope">
                <template v-if="isEditing && scope.row.applyStatus !== '1'">
                  <div class="teacher-edit-wrapper">
                    <el-input v-model.trim="scope.row.guideTeacherInfoList[0].userName" size="small" />
                    <div class="teacher-tip" :class="{ 'is-error': !scope.row.guideTeacherInfoList[0].userName?.trim() && !scope.row.guideTeacherInfoList[1].userName?.trim() }">至少需要一名指导教师</div>
                  </div>
                </template>
                <span v-else>{{ getTeacherName(scope.row, 1) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="指导教师2" align="left" min-width="120">
              <template #default="scope">
                <template v-if="isEditing && scope.row.applyStatus !== '1'">
                  <div class="teacher-edit-wrapper">
                    <el-input v-model.trim="scope.row.guideTeacherInfoList[1].userName" size="small" />
                    <div class="teacher-tip"></div>
                  </div>
                </template>
                <span v-else>{{ getTeacherName(scope.row, 2) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <!-- 分页 -->
          <div class="mt-5 w-full text-right flex justify-end">
            <pagination
              v-show="total > 0"
              :total="total"
              v-model:page="queryParams.pageNum"
              v-model:limit="queryParams.pageSize"
              :page-sizes="[50, 100, 150]"
              @pagination="getList"
            />
          </div>
        </div>
      </div>
      </template>
    </div>
  </div>
</div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { Top, Bottom } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import Pagination from '@/components/Pagination'
import { getPromotedInfoPcList, getPromotedApplyInfoPcList, updatePromotedApplyInfoPcEdit, promotedApplyInfoPcApply } from '@/api/promotion/index.js'
import Modal from '@/plugins/modal.js'
import { downloadJS } from '@/utils/request'
import "@vueup/vue-quill/dist/vue-quill.snow.css"
const queryRef = ref(null)
const loading = ref(false)
const tabsLoading = ref(false)
const isEditing = ref(false)
const saveLoading = ref(false)
const exportLoading = ref(false)
const signUpLoading = ref(false)
const selectedRows = ref([])
const isInPublicityPeriod = computed(() => {
  return currentCompetition.value?.competitionApplyStatus === '1'
})

// 是否有未填写指导教师的行
const hasInvalidTeacherRows = computed(() => {
  return tableList.value.some(row => {
    const teacher1 = row.guideTeacherInfoList?.[0]?.userName?.trim()
    const teacher2 = row.guideTeacherInfoList?.[1]?.userName?.trim()
    return !teacher1 && !teacher2
  })
})

// 是否有未报名的队伍
const hasUnapplyTeams = computed(() => {
  return tableList.value.some(row => row.applyStatus !== '1')
})

// 备份数据用于取消编辑
const backupList = ref([])
const sortableInstances = []

// 比赛Tab数据
const competitionTabs = ref([])
const activeCompetitionId = ref(null)

// 当前选中的比赛
const currentCompetition = computed(() => {
  return competitionTabs.value.find(item => item.competitionSeriesId === activeCompetitionId.value)
})

/** 获取未报名队伍数量 */
function getUnapplyTeamNum(teamNum, applyTeamNum) {
  if (teamNum === undefined || teamNum === null || teamNum === '' ||
      applyTeamNum === undefined || applyTeamNum === null || applyTeamNum === '') {
    return '-'
  }
  const num = Number(teamNum)
  const applyNum = Number(applyTeamNum)
  return num - applyNum
}

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 50,
  schoolName: undefined,
  competitionTrackName: undefined,
  userName: undefined,
  guideTeacher: undefined
})

// 表格数据
const tableList = ref([])
const total = ref(0)

function getAwardTagType(award) {
  const map = { '一等奖': 'danger', '二等奖': 'warning', '三等奖': 'success', '优秀奖': 'info' }
  return map[award] || ''
}

// 状态标签映射
function getStatusLabel(status) {
  const map = {
    '1': '报名中',
    '0': '未开始',
    '2': '已结束'
  }
  return map[status] || '-'
}

// 状态样式映射
function getStatusClass(status) {
  const map = {
    '1': 'active',
    '0': 'pending',
    '2': 'ended'
  }
  return map[status] || ''
}

function formatStudentNames(playerList) {
  if (!playerList || playerList.length === 0) return '-'
  const sorted = [...playerList].sort((a, b) => (a.teamSort || 0) - (b.teamSort || 0))
  return sorted.map((m, idx) => `${idx + 1}、${m.userName || m}`).join('，')
}

function getTeacherName(row, sort) {
  const list = row.guideTeacherInfoList
  if (!list || list.length === 0) return '-'
  const teacher = list.find(t => t.teamSort === sort)
  return teacher?.userName || '-'
}

function updateTeamSort(list) {
  list.forEach((item, i) => { item.teamSort = i + 1 })
}

/** 移动学生顺序 */
function moveStudent(row, index, direction) {
  if (!isEditing.value) return
  const list = row.playerInfoList
  if (!list || list.length === 0) return
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= list.length) return
  const temp = list[index]
  list[index] = list[newIndex]
  list[newIndex] = temp
  updateTeamSort(list)
}

/** Tab切换 */
function handleTabChange(competitionId) {
  // 退出编辑模式
  isEditing.value = false
  destroySortables()
  activeCompetitionId.value = competitionId
  queryParams.value.schoolName = undefined
  queryParams.value.competitionTrackName = undefined
  queryParams.value.userName = undefined
  queryParams.value.guideTeacher = undefined
  queryParams.value.pageNum = 1
  getList()
}

/** 查询列表 */
function getList() {
  if (!activeCompetitionId.value) return
  loading.value = true
  // 查询数据时退出编辑模式，避免新数据结构与编辑状态不兼容导致渲染错误
  isEditing.value = false
  destroySortables()
  const params = {
    ...queryParams.value,
    pageNum: queryParams.value.pageNum,
    pageSize: queryParams.value.pageSize,
    competitionSeriesId: activeCompetitionId.value
  }
  getPromotedApplyInfoPcList(params).then(response => {
    tableList.value = response.rows || []
    total.value = response.total || 0
  }).catch(() => {
    tableList.value = []
    total.value = 0
  }).finally(() => {
    loading.value = false
  })
}

/** 搜索 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  queryParams.value = {
    pageNum: 1,
    pageSize: 50,
    schoolName: undefined,
    competitionTrackName: undefined,
    userName: undefined,
    guideTeacher: undefined
  }
  getList()
}

/** 编辑 */
function handleEdit() {
  if (isEditing.value) return
  if (!tableList.value.length) return
  tableList.value.forEach(row => {
    if (!row.playerInfoList) row.playerInfoList = []
    if (!row.guideTeacherInfoList) row.guideTeacherInfoList = []
    const list = row.guideTeacherInfoList
    row.guideTeacherInfoList = [
      list.find(t => t.teamSort === 1) || { teamSort: 1, userName: '' },
      list.find(t => t.teamSort === 2) || { teamSort: 2, userName: '' }
    ]
  })
  backupList.value = JSON.parse(JSON.stringify(tableList.value))
  isEditing.value = true
  nextTick(() => {
    initStudentSortable()
  })
}

/** 初始化学生拖动排序 */
function initStudentSortable() {
  destroySortables()
  const containers = document.querySelectorAll('.student-edit-list')
  containers.forEach(container => {
    const rowId = container.getAttribute('data-row-id')
    const rowIndex = tableList.value.findIndex(item => String(item.teamCode) === rowId)
    if (rowIndex === -1) return
    const row = tableList.value[rowIndex]
    const instance = Sortable.create(container, {
      handle: '.student-edit-item',
      animation: 150,
      onEnd: (evt) => {
        const playerInfoList = row.playerInfoList
        if (!playerInfoList) return
        const movedItem = playerInfoList.splice(evt.oldIndex, 1)[0]
        playerInfoList.splice(evt.newIndex, 0, movedItem)
        updateTeamSort(playerInfoList)
        tableList.value.splice(rowIndex, 1, { ...row })
      }
    })
    sortableInstances.push(instance)
  })
}

function destroySortables() {
  sortableInstances.forEach(instance => instance.destroy())
  sortableInstances.length = 0
}

/** 保存 */
function handleSave() {
  const changedRows = tableList.value.filter((row, index) => {
    return JSON.stringify(row) !== JSON.stringify(backupList.value[index])
  })
  if (changedRows.length === 0) {
    isEditing.value = false
    destroySortables()
    Modal.msgSuccess('保存成功')
    return
  }
  saveLoading.value = true
  // 构建提交数据
  const submitData = changedRows.map(row => ({
    competitionSeriesId: activeCompetitionId.value,
    teamCode: row.teamCode,
    playerInfoList: (row.playerInfoList || []).map(player => ({
      applyId: player.applyId,
      teamSort: player.teamSort
    })),
    guideTeacherInfoList: (row.guideTeacherInfoList || []).map((teacher, index) => ({
      applyId: teacher.applyId,
      userName: teacher.userName,
      teamSort: index + 1
    }))
  }))
  // 循环提交每个变更的行
  const promises = submitData.map(data => updatePromotedApplyInfoPcEdit(data))
  Promise.all(promises).then(() => {
    isEditing.value = false
    destroySortables()
    Modal.msgSuccess('保存成功')
    getList()
  }).catch(() => {
    Modal.msgError('保存失败')
  }).finally(() => {
    saveLoading.value = false
  })
}

/** 取消 */
function handleCancel() {
  try {
    tableList.value = JSON.parse(JSON.stringify(backupList.value))
  } catch {
    tableList.value = []
  }
  isEditing.value = false
  destroySortables()
}

/** 导出 */
function handleExport() {
  if (!activeCompetitionId.value) return
  exportLoading.value = true
  downloadJS(
    import.meta.env.VITE_APP_BASE_API +
      `/competition/promotedApplyInfo/pcExport?competitionSeriesId=${activeCompetitionId.value}&exportType=all`,
    `${currentCompetition.value?.competitionName || '-'}_我的晋级队伍.xlsx`,
    "addName",
    () => {
      exportLoading.value = false
    },
    'post'
  )
}

/** 复选框是否可选（已报名不可选） */
function isSelectable(row) {
  return row.applyStatus != '1' && isInPublicityPeriod
}

/** 选中项变化 */
function handleSelectionChange(selection) {
  selectedRows.value = selection
}

/** 报名 */
function handleSignUp() {
  if (selectedRows.value.length === 0) {
    Modal.msgWarning('请选择要报名的队伍')
    return
  }
  // 确认报名
  Modal.confirm(
    '确认报名选中的 ' + selectedRows.value.length + ' 支队伍吗？报名后无法取消，请谨慎操作。',
    '报名确认',
    { confirmButtonText: '确认报名', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    signUpLoading.value = true
    // 获取当前选中的 promotedId
    const currentTab = competitionTabs.value.find(item => item.competitionSeriesId === activeCompetitionId.value)
    const promotedId = currentTab?.promotedId
    if (!promotedId) {
      Modal.msgError('获取赛事信息失败')
      return
    }
    // 构建提交数据
    const data = {
      promotedId: promotedId,
      teamCodes: selectedRows.value.map(row => row.teamCode)
    }
    return promotedApplyInfoPcApply(data)
  }).then((response) => {
    if (response?.data?.free) {
      // 退出编辑模式
      isEditing.value = false
      // 刷新 tabs 列表和表格列表，保持当前页和当前 tabs
      fetchTabs(true)
      getList()
      Modal.msgSuccess('报名成功')
    } else {
      Modal.msgWarning('报名失败，请联系管理员')
    }
  }).catch(() => {
    // 用户取消或请求失败
  }).finally(() => {
    signUpLoading.value = false
  })
}

/** 数据单元格类名 */
function cellClassName({ column }) {
  const readonlyColumns = ['序号','报名状态', '学校', '赛道',]
  const editableColumns = ['学生姓名（可调整顺序）', '指导教师1', '指导教师2']
  
  if (readonlyColumns.includes(column.label)) {
    return 'readonly-cell'
  }
  if (editableColumns.includes(column.label)) {
    return 'editable-cell'
  }
  return ''
}

/** 获取Tab列表 */
function fetchTabs(keepCurrent = false) {
  tabsLoading.value = true
  // 保存当前选中的 tab
  const currentId = activeCompetitionId.value
  getPromotedInfoPcList().then(response => {
    const list = response.data || []
    competitionTabs.value = list
    if (list.length > 0) {
      // 如果 keepCurrent 为 true 且当前 tab 在新列表中仍然存在，则保持当前选中
      const currentTabExists = keepCurrent && list.some(item => item.competitionSeriesId === currentId)
      if (!currentTabExists) {
        activeCompetitionId.value = list[0].competitionSeriesId
      }
      // 只有在没有调用 getList 的情况下才调用
      if (!keepCurrent || !currentTabExists) {
        getList()
      }
    }
  }).catch(() => {
    competitionTabs.value = []
  }).finally(() => {
    tabsLoading.value = false
  })
}

onMounted(() => {
  fetchTabs()
})

onUnmounted(() => {
  destroySortables()
})
</script>

<style scoped lang="scss">
.student-edit-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.student-edit-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: move;
  padding: 2px 4px;
  border-radius: 4px;
  transition: background-color 0.2s;

  &:hover {
    background-color: #f0f9ff;
  }

  &.sortable-ghost {
    opacity: 0.5;
    background-color: #e6f7ff;
  }
}

:deep(.sortable-ghost) {
  opacity: 0.5;
  background-color: #e6f7ff;
}

/* 比赛Tab栏样式 */
.competition-tabs {
  background-color: rgba(245, 247, 250, 1);
  .tabs-wrapper {
    display: flex;
    gap: 8px;
    padding: 0px 0;
    overflow-x: auto;

    &::-webkit-scrollbar {
      height: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #c0c4cc;
      border-radius: 2px;
    }
  }

  .tab-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    cursor: pointer;
    transition: all 0.3s;
    white-space: nowrap;
    border: 1px solid transparent;

    &:hover {
      background: #f5f7fa;
    }

    &.active {
      background: #d7eaff;

      .tab-name {
        color: #409eff;
        font-weight: 500;
      }
    }

    .tab-name {
      font-size: 14px;
      color: #606266;
    }

    .tab-status {
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 4px;
      font-weight: 500;

      &.active {
        background: #67c23a;
        color: #fff;
      }

      &.pending {
        background: #e6a23c;
        color: #fff;
      }

      &.ended {
        background: #909399;
        color: #fff;
      }
    }
  }
}

/* 指导教师编辑区域 */
.teacher-edit-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.teacher-tip {
  font-size: 12px;
  line-height: 1.2;
  min-height: 16px;
  color: transparent;
}

.teacher-tip.is-error {
  color: #e6a23c;
}
/* 表格数据单元格颜色区分 */
:deep(.el-table__body .readonly-cell) {
  background-color: #F5F5F5;
  // color: #C8C8C8;
}

:deep(.el-table__body .editable-cell) {
  // background-color: #ecf5ff;
}
</style>
