<template>
<div class="global-page pb-10">
  <div class="container-custom rounded-[10px]">
    <div class="mt-5">
      <div class="text-[28px] font-bold text-[#303133]">获奖公示</div>
      <div class="text-[14px] text-[#999999]">
        当前共有{{ competitionTabs.length }}个比赛的获奖公示
        <template v-if="competitionTabs.length > 1">，请切换标签页查看不同比赛的公示信息</template>
      </div>
    </div>
    <div class="mt-2 border border-[#e4e7ed] bg-[#ffffff]">
      <!-- 比赛Tab栏 -->
      <div class="competition-tabs mb-5" v-loading="tabsLoading">
        <div class="tabs-wrapper">
          <div
            v-for="item in competitionTabs"
            :key="item.id"
            :class="['tab-item', { active: activeCompetitionId === item.id }]"
            @click="handleTabChange(item.id)"
          >
            <span class="tab-name">{{ item.competitionName }}</span>
            <span :class="['tab-status', item.expired ? 'active' : 'ended']">{{ item.expired ? '公示中' : '已截止' }}</span>
          </div>
        </div>
      </div>
      <div v-if="!tabsLoading && competitionTabs.length === 0" class="text-center py-16 text-[#909399] text-sm">暂无公示数据</div>
      <template v-if="activeCompetitionId">
      <div class="px-4">
        <!-- 页面标题 -->
        <div class="flex justify-between items-start mb-5">
          <div class="header-left">
            <h2 class="text-[26px] font-bold text-[#303133] m-0 mb-2">{{ currentCompetition?.competitionName || '获奖公示' }}</h2>
            <div class="text-sm text-[#909399]">
              <span>公示截止时间：{{ currentCompetition?.expirationTime || '-' }}</span>
            </div>
          </div>
          <div class="header-right">
            <el-button type="success" icon="Download" :loading="exportLoading" @click="handleExport" :disabled="isEditing">导出获奖名单</el-button>
          </div>
        </div>

        <!-- 公示说明 -->
        <div class="mb-5 border border-[#E6A23C] rounded p-3 bg-[rgba(255,247,230,0.6)]" v-if="currentCompetition?.tipInfo">
          <div class="ql-container ql-snow !my-[0]">
            <div class="rich-content ql-editor" v-html="currentCompetition?.tipInfo || ''">
            </div>
          </div>
        </div>

        <!-- 筛选区域 -->
        <el-form :model="queryParams"
                  ref="queryRef"
                  :inline="true"
                  class="pt-6 bg-[#ffffff] rounded"
                  label-width="90px">
          <el-form-item label="团队名称" prop="teamName">
            <el-input
              v-model.trim="queryParams.teamName"
              placeholder="请输入团队名称"
              clearable
              style="width: 180px;"
            />
          </el-form-item>
          <el-form-item label="学校" prop="schoolName">
            <el-input
              v-model.trim="queryParams.schoolName"
              placeholder="请输入学校"
              clearable
              style="width: 180px;"
            />
          </el-form-item>
          <el-form-item label="赛道" prop="competitionTrackName">
            <el-input
              v-model.trim="queryParams.competitionTrackName"
              placeholder="请输入赛道"
              clearable
              style="width: 180px;"
            />
          </el-form-item>
          <el-form-item label="奖项" prop="awardsName">
            <el-input
              v-model.trim="queryParams.awardsName"
              placeholder="请输入奖项"
              clearable
              style="width: 180px;"
            />
          </el-form-item>
          <el-form-item label="学生姓名" prop="userName">
            <el-input
              v-model.trim="queryParams.userName"
              placeholder="请输入学生姓名"
              clearable
              style="width: 180px;"
            />
          </el-form-item>
          <el-form-item label="指导教师" prop="guiderTeacherName">
            <el-input
              v-model.trim="queryParams.guiderTeacherName"
              placeholder="请输入指导教师"
              clearable
              style="width: 180px;"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 操作按钮 -->
        <div class="w-full flex justify-end gap-2 my-4">
          <template v-if="isInPublicityPeriod">
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
            row-key="id"
            height="400px"
            :cell-class-name="cellClassName"
          >
            <el-table-column label="序号" align="center" type="index" width="60" />
            <el-table-column label="团队名称" align="left" prop="teamName" min-width="150" show-overflow-tooltip>
              <template #default="scope">
                <span>{{ scope.row.teamName }}</span>
              </template>
            </el-table-column>
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
            <el-table-column label="奖项" align="center" prop="awardsName" width="120" sortable>
              <template #default="scope">
                <el-tag :type="getAwardTagType(scope.row.awardsName)">{{ scope.row.awardsName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="学生姓名（可调整顺序）" align="left" min-width="200">
              <template #default="scope">
                <template v-if="isEditing">
                  <div :class="['student-edit-list', 'student-sortable-' + scope.row.id]" :data-row-id="scope.row.id">
                    <div
                      v-for="(student, idx) in (scope.row.playerList || [])"
                      :key="scope.row.id + '-' + idx + '-' + student.userName"
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
                        :disabled="idx === (scope.row.playerList ? scope.row.playerList.length - 1 : 0)"
                        @click="moveStudent(scope.row, idx, 1)"
                      />
                    </div>
                  </div>
                </template>
                <span v-else>{{ formatStudentNames(scope.row.playerList) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="指导教师1" align="left" min-width="120">
              <template #default="scope">
                <template v-if="isEditing">
                  <div class="teacher-edit-wrapper">
                    <el-input v-model.trim="scope.row.guiderTeacherList[0].userName" size="small" />
                    <div class="teacher-tip" :class="{ 'is-error': !scope.row.guiderTeacherList[0].userName?.trim() && !scope.row.guiderTeacherList[1].userName?.trim() }">至少需要一名指导教师</div>
                  </div>
                </template>
                <span v-else>{{ getTeacherName(scope.row, 1) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="指导教师2" align="left" min-width="120">
              <template #default="scope">
                <template v-if="isEditing">
                  <div class="teacher-edit-wrapper">
                    <el-input v-model.trim="scope.row.guiderTeacherList[1].userName" size="small" />
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
import { getAwardPublicityTabs, getAwardDetailsList, updateAwardDetailsList } from '@/api/awardPublicity/index.js'
import Modal from '@/plugins/modal.js'
import { downloadJS } from '@/utils/request'
import "@vueup/vue-quill/dist/vue-quill.snow.css"
const queryRef = ref(null)
const loading = ref(false)
const tabsLoading = ref(false)
const isEditing = ref(false)
const saveLoading = ref(false)
const exportLoading = ref(false)
const isInPublicityPeriod = computed(() => {
  return currentCompetition.value?.expired === true
})

// 是否有未填写指导教师的行
const hasInvalidTeacherRows = computed(() => {
  return tableList.value.some(row => {
    const teacher1 = row.guiderTeacherList?.[0]?.userName?.trim()
    const teacher2 = row.guiderTeacherList?.[1]?.userName?.trim()
    return !teacher1 && !teacher2
  })
})

// 备份数据用于取消编辑
const backupList = ref([])
const sortableInstances = []

// 比赛Tab数据
const competitionTabs = ref([])
const activeCompetitionId = ref(null)

// 当前选中的比赛
const currentCompetition = computed(() => {
  return competitionTabs.value.find(item => item.id === activeCompetitionId.value)
})

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 50,
  teamName: undefined,
  schoolName: undefined,
  competitionTrackName: undefined,
  awardsName: undefined,
  userName: undefined,
  guiderTeacherName: undefined
})

// 表格数据
const tableList = ref([])
const total = ref(0)

function getAwardTagType(award) {
  const map = { '一等奖': 'danger', '二等奖': 'warning', '三等奖': 'success', '优秀奖': 'info' }
  return map[award] || ''
}

function formatStudentNames(playerList) {
  if (!playerList || playerList.length === 0) return '-'
  const sorted = [...playerList].sort((a, b) => (a.teamSort || 0) - (b.teamSort || 0))
  return sorted.map((m, idx) => `${idx + 1}、${m.userName || m}`).join('，')
}

function getTeacherName(row, sort) {
  const list = row.guiderTeacherList
  if (!list || list.length === 0) return '-'
  const teacher = list.find(t => t.teamSort === sort)
  return teacher?.userName || '-'
}

function updateTeamSort(list) {
  list.forEach((item, i) => { item.teamSort = i + 1 })
}

/** 数据单元格类名 */
function cellClassName({ column }) {
  const readonlyColumns = ['序号','团队名称', '学校', '赛道', '奖项']
  const editableColumns = ['学生姓名（可调整顺序）', '指导教师1', '指导教师2']
  
  if (readonlyColumns.includes(column.label)) {
    return 'readonly-cell'
  }
  if (editableColumns.includes(column.label)) {
    return 'editable-cell'
  }
  return ''
}

/** 移动学生顺序 */
function moveStudent(row, index, direction) {
  const list = row.playerList
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
  activeCompetitionId.value = competitionId
  queryParams.value.teamName = undefined
  queryParams.value.schoolName = undefined
  queryParams.value.competitionTrackName = undefined
  queryParams.value.userName = undefined
  queryParams.value.guiderTeacherName = undefined
  queryParams.value.awardsName = undefined
  queryParams.value.pageNum = 1
  getList()
}

/** 查询列表 */
function getList() {
  if (!activeCompetitionId.value) return
  isEditing.value = false
  destroySortables()
  loading.value = true
  const params = {
    ...queryParams.value,
    pageNum: queryParams.value.pageNum,
    pageSize: queryParams.value.pageSize,
    awardPublicityId: activeCompetitionId.value
  }
  getAwardDetailsList(params).then(response => {
    const rows = (response.rows || []).map(row => ({
      ...row,
      playerList: row.playerList || [],
      guiderTeacherList: Array.isArray(row.guiderTeacherList) ? row.guiderTeacherList : []
    }))
    tableList.value = rows
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
    teamName: undefined,
    schoolName: undefined,
    competitionTrackName: undefined,
    awardsName: undefined,
    userName: undefined,
    guiderTeacherName: undefined
  }
  getList()
}

/** 编辑 */
function handleEdit() {
  if (isEditing.value) return
  if (!tableList.value.length) return
  tableList.value.forEach(row => {
    if (!row.playerList) row.playerList = []
    if (!row.guiderTeacherList) row.guiderTeacherList = []
    const list = row.guiderTeacherList
    row.guiderTeacherList = [
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
    const rowIndex = tableList.value.findIndex(item => String(item.id) === rowId)
    if (rowIndex === -1) return
    const row = tableList.value[rowIndex]
    const instance = Sortable.create(container, {
      handle: '.student-edit-item',
      animation: 150,
      onEnd: (evt) => {
        const playerList = row.playerList
        if (!playerList) return
        const movedItem = playerList.splice(evt.oldIndex, 1)[0]
        playerList.splice(evt.newIndex, 0, movedItem)
        updateTeamSort(playerList)
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
  updateAwardDetailsList(changedRows).then(response => {
    if (response.code === 200) {
      isEditing.value = false
      destroySortables()
      Modal.msgSuccess('保存成功')
      getList()
    } else {
      Modal.msgError(response.msg || '保存失败')
    }
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
      `/competition/awardDetailsUser/export?awardPublicityId=${activeCompetitionId.value}`,
    '获奖名单.xlsx',
    "addName",
    () => {
      exportLoading.value = false
    },
    'post'
  )
}

/** 获取Tab列表 */
function fetchTabs() {
  tabsLoading.value = true
  getAwardPublicityTabs().then(response => {
    const list = response.data || []
    competitionTabs.value = list
    if (list.length > 0) {
      activeCompetitionId.value = list[0].id
      getList()
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

/* 表格数据单元格颜色区分 */
:deep(.el-table__body .readonly-cell) {
  background-color: #F5F5F5;
  // color: #C8C8C8;
}

:deep(.el-table__body .editable-cell) {
  // background-color: #ecf5ff;
}

/* hover 时恢复默认行hover效果 */
:deep(.el-table__body tr:hover > .readonly-cell),
:deep(.el-table__body tr:hover > .editable-cell) {
  background-color: transparent;
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

      &.ended {
        background: #909399;
        color: #fff;
      }
    }
  }
}
</style>
