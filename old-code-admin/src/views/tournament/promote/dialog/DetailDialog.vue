<template>
  <el-drawer
    v-model="visible"
    title="晋级名单明细"
    size="85%"
    :direction="direction"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <div class="detail-container">
      <!-- 基本信息 -->
      <div class="section">
        <div class="section-title">基本信息</div>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="赛事名称" width="400px">
            {{ baseInfo.competitionName }}
          </el-descriptions-item>
          <el-descriptions-item label="赛事届数" width="400px">
            {{ baseInfo.competitionSeriesName }}
          </el-descriptions-item>
          <el-descriptions-item label="状态" width="400px">
            <el-tag :type="getStatusType(baseInfo.competitionApplyStatus)">{{ getStatusLabel(baseInfo.competitionApplyStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="晋级队伍数">
            {{ baseInfo.teamNum || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="已报名数">
            {{ baseInfo.applyTeamNum || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="费用">
            {{ baseInfo.fee ? '¥' + baseInfo.fee : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="报名开始时间">
            {{ baseInfo.applyStartTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="报名结束时间">
            {{ baseInfo.applyEndTime || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 晋级名单 -->
      <div class="section">
        <div class="section-header">
          <div class="section-title">晋级名单</div>
        </div>

        <!-- 筛选区域 -->
        <el-form :model="queryParams"
                  ref="queryRef"
                  :inline="true"
                  class="filter-form"
                  label-width="100px">
          <el-form-item label="团队编号" prop="teamCode">
            <el-input
              v-model.trim="queryParams.teamCode"
              placeholder="请输入团队编号"
              clearable
              style="width: 160px;"
            />
          </el-form-item>
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
          <el-form-item label="学生" prop="userName">
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

        <div class="section-actions">
          <template v-if="!isEditing">
            <span class="diff-tip">表格淡红色背景的为异常数据！</span>
            <el-button type="primary" icon="Edit" @click="handleEdit">编辑</el-button>
            <el-button type="success" icon="Download" @click="handleExport('all')">导出所有</el-button>
            <el-button type="success" icon="Download" @click="handleExport('filter')">导出检索结果</el-button>
          </template>
          <template v-else>
            <el-button type="primary" icon="Check" @click="handleSave" :loading="saveLoading">保存</el-button>
            <el-button icon="Close" @click="handleCancel">取消</el-button>
          </template>
        </div>
        <!-- 列表 -->
        <el-table
          v-loading="loading"
          :data="tableList"
          row-key="id"
          height="600px"
          :row-class-name="tableRowClassName"
        >
          <el-table-column label="序号" align="center" type="index" width="50" />
          <el-table-column label="团队编号" align="left" prop="teamCode" width="220" show-overflow-tooltip />
          <el-table-column label="团队名称" align="left" prop="teamName" min-width="120" show-overflow-tooltip />
          <el-table-column label="学校" align="left" prop="schoolName" min-width="140" show-overflow-tooltip />
          <el-table-column label="赛道" align="left" min-width="130" show-overflow-tooltip sortable prop="competitionTrackName">
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
          <el-table-column label="学生姓名（可调整顺序）" align="left" min-width="180">
            <template #default="scope">
              <template v-if="isEditing">
                <div :class="['student-edit-list', 'student-sortable-' + scope.row.teamCode]" :data-row-id="scope.row.teamCode">
                  <div
                    v-for="(student, idx) in scope.row.playerInfoList"
                    :key="student.applyId || scope.row.teamCode + '-' + idx"
                    class="student-edit-item"
                  >
                    <span class="student-index">{{ idx + 1 }}、</span>
                    <span class="student-name">{{ student.userName || '-' }}</span>
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
                      :disabled="idx === scope.row.playerInfoList.length - 1"
                      @click="moveStudent(scope.row, idx, 1)"
                    />
                  </div>
                </div>
              </template>
              <span v-else>{{ formatStudentNames(scope.row.playerInfoList) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="指导教师1" align="left" width="100">
            <template #default="scope">
              <el-input v-if="isEditing" v-model="scope.row.guideTeacherInfoList[0].userName" size="small" />
              <span v-else>{{ getTeacherName(scope.row, 1) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="指导教师2" align="left" width="100">
            <template #default="scope">
              <el-input v-if="isEditing" v-model="scope.row.guideTeacherInfoList[1].userName" size="small" />
              <span v-else>{{ getTeacherName(scope.row, 2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="60" fixed="right">
            <template #default="scope">
              <el-button
                link
                icon="Delete"
                type="danger"
                v-if="!isEditing"
                @click="handleDetailDelete(scope.row)"
              ></el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
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
  </el-drawer>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { getPromotionDetail, getPromotionDetailsList, removePromotionDetail, editPromotionDetails, exportPromotionDetails } from '@/api/tournament/promote'
import modal from '@/plugins/modal'
import Pagination from '@/components/Pagination'
import { Top, Bottom } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { handleAsyncExport } from '@/utils/export'

const emit = defineEmits(['success', 'closed'])

const visible = ref(false)
const loading = ref(false)
const direction = ref('rtl')
const queryRef = ref(null)
const currentRow = ref(null)
const isEditing = ref(false)
const saveLoading = ref(false)

// 备份数据用于取消编辑
const backupList = ref([])

// 基本信息
const baseInfo = ref({})

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 50,
  competitionSeriesId: undefined,
  teamCode: undefined,
  userName: undefined,
  guideTeacher: undefined,
  schoolName: undefined,
  competitionTrackName: undefined
})

// 表格数据
const tableList = ref([])
const total = ref(0)

// 状态类型映射
function getStatusType(status) {
  const map = {
    '0': 'success',
    '1': 'primary',
    '2': 'info'
  }
  return map[status] || 'info'
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

function formatStudentNames(playerInfoList) {
  if (!playerInfoList || playerInfoList.length === 0) return '-'
  const sorted = [...playerInfoList].sort((a, b) => (a.teamSort || 0) - (b.teamSort || 0))
  return sorted.map((m, idx) => `${idx + 1}、${m.userName || '-'}`).join('，')
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
  const list = row.playerInfoList
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= list.length) return
  const temp = list[index]
  list[index] = list[newIndex]
  list[newIndex] = temp
  updateTeamSort(list)
}

/** 查询列表 */
const totalSum = ref(0)
function getList() {
  loading.value = true
  let params = {
    competitionSeriesId: currentRow.value.competitionSeriesId,
    ...queryParams.value
  }
  getPromotionDetailsList(params).then(response => {
    if (response.code === 200) {
      tableList.value = response.rows || []
      total.value = response.total || 0
      totalSum.value = response.totalSum || 0
    }
  }).finally(() => {
    loading.value = false
  })
}

function tableRowClassName({ row }) {
  if (!row.teamName) {
    return 'diff-series-row'
  }
  return ''
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
    competitionSeriesId: currentRow.value?.competitionSeriesId,
    teamCode: undefined,
    userName: undefined,
    guideTeacher: undefined,
    schoolName: undefined,
    competitionTrackName: undefined
  }
  handleQuery()
}

/** 编辑 */
function handleEdit() {
  tableList.value.forEach(row => {
    if (!row.guideTeacherInfoList) row.guideTeacherInfoList = []
    const list = row.guideTeacherInfoList
    // 保留原有的applyId，如果没有则设为null（新增时）
    const teacher1 = list.find(t => t.teamSort === 1)
    const teacher2 = list.find(t => t.teamSort === 2)
    row.guideTeacherInfoList = [
      teacher1 || { applyId: null, teamSort: 1, userName: '' },
      teacher2 || { applyId: null, teamSort: 2, userName: '' }
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
  const containers = document.querySelectorAll('.student-edit-list')
  containers.forEach(container => {
    const rowId = container.getAttribute('data-row-id')
    const rowIndex = tableList.value.findIndex(item => String(item.teamCode) === rowId)
    if (rowIndex === -1) return
    Sortable.create(container, {
      handle: '.student-edit-item',
      animation: 150,
      onEnd: (evt) => {
        const currentRow = tableList.value[rowIndex]
        const playerInfoList = currentRow.playerInfoList
        const movedItem = playerInfoList.splice(evt.oldIndex, 1)[0]
        playerInfoList.splice(evt.newIndex, 0, movedItem)
        updateTeamSort(playerInfoList)
        tableList.value.splice(rowIndex, 1, { ...currentRow })
      }
    })
  })
}

/** 保存 */
function handleSave() {
  const changedRows = tableList.value.filter((row, index) => {
    return JSON.stringify(row) !== JSON.stringify(backupList.value[index])
  })
  if (changedRows.length === 0) {
    isEditing.value = false
    modal.msgSuccess('保存成功')
    return
  }

  // 构建API需要的数据结构
  const submitData = changedRows.map(row => {
    // 学生只提交applyId和teamSort
    const playerInfoList = (row.playerInfoList || []).map(student => ({
      applyId: student.applyId,
      teamSort: student.teamSort
    }))

    // 指导教师提交applyId、userName、teamSort
    const guideTeacherInfoList = (row.guideTeacherInfoList || []).map(teacher => ({
      applyId: teacher.applyId,
      userName: teacher.userName,
      teamSort: teacher.teamSort
    }))

    return {
      playerInfoList,
      guideTeacherInfoList
    }
  })

  saveLoading.value = true
  editPromotionDetails(submitData).then(response => {
    if (response.code === 200) {
      modal.msgSuccess('保存成功')
      isEditing.value = false
      getList()
    } else {
      modal.msgError(response.msg || '保存失败')
    }
  }).finally(() => {
    saveLoading.value = false
  })
}

/** 取消 */
function handleCancel() {
  tableList.value = JSON.parse(JSON.stringify(backupList.value))
  isEditing.value = false
}

/** 删除晋级名单明细 */
function handleDetailDelete(row) {
  modal.confirm('确定要删除吗？').then(() => {
    const competitionSeriesId = currentRow.value.competitionSeriesId
    const teamCodes = row.teamCode
    removePromotionDetail(competitionSeriesId, teamCodes).then(response => {
      if (response.code === 200) {
        modal.msgSuccess('删除成功')
        fetchDetail(currentRow.value.promotedId)
        getList()
      } else {
        modal.msgError(response.msg || '删除失败')
      }
    })
  }).catch(() => {})
}

/** 导出 */
function handleExport(type) {
  let params = {
    competitionSeriesId: currentRow.value.competitionSeriesId,
    exportType: type
  }
  if (type === 'filter') {
    // 检索导出时添加其他筛选条件
    const { teamCode, userName, guideTeacher, schoolName, competitionTrackName } = queryParams.value
    Object.assign(params, { teamCode, userName, guideTeacher, schoolName, competitionTrackName })
  }
  handleAsyncExport(exportPromotionDetails, params)
}

/** 打开弹框 */
function openDialog(row) {
  visible.value = true
  currentRow.value = row
  isEditing.value = false
  // 初始化competitionSeriesId
  queryParams.value.competitionSeriesId = row.competitionSeriesId
  fetchDetail(row.promotedId)
  resetQuery()
}

function fetchDetail(id) {
  getPromotionDetail(id).then(response => {
    if (response.code === 200) {
      const data = response.data || {}
      baseInfo.value = data
    }
  })
}

/** 弹框关闭 */
function handleClosed() {
  emit('closed')
}

defineExpose({
  openDialog
})
</script>

<style scoped lang="scss">
.detail-container {
  margin-top: -20px;
  padding: 0 0px 20px;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .section-title {
    margin-bottom: 0;
  }
}

.filter-form {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

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

.student-index {
  color: #606266;
  font-size: 13px;
  min-width: 24px;
}

.student-name {
  color: #303133;
  font-size: 13px;
  min-width: 60px;
}
.section-actions {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom:10px;
}
.detail-container{
  .diff-tip {
    color: rgba(245, 108, 108, 1);
    font-size: 13px;
    line-height: 32px;
  }
  :deep(.el-table) {
    .el-table__body-wrapper{
      .diff-series-row {
        background: rgba(245, 108, 108, 0.2) !important;
        .el-table__cell {
          background: rgba(245, 108, 108, 0.2) !important;
        }
      }
    }
  }
}

</style>
