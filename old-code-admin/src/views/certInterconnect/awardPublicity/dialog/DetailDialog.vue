<template>
  <el-drawer
    v-model="visible"
    title="获奖名单明细"
    size="85%"
    :direction="direction"
    :close-on-click-modal="false"
  >
    <div class="detail-container">
      <!-- 基本信息 -->
      <div class="section">
        <div class="section-title">基本信息</div>
        <el-descriptions :column="3" border label-width="130px">
          <el-descriptions-item label="比赛名称" width="500px">
            {{ baseInfo.competitionName }}
          </el-descriptions-item>
          <el-descriptions-item label="状态" width="300px">
            <el-tag :type="getStatusType(baseInfo.status)">{{ baseInfo.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建人"  width="300px">{{ baseInfo.createBy }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ baseInfo.createTime }}</el-descriptions-item>
          <el-descriptions-item label="公示过期时间">{{ baseInfo.expirationTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="获奖总数">{{ totalSum ||'-' }} 条</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 获奖名单 -->
      <div class="section">
        <div class="section-header">
          <div class="section-title">获奖名单</div>
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
          <el-form-item label="团队名称" prop="teamName">
            <el-input
              v-model.trim="queryParams.teamName"
              placeholder="请输入团队名称"
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
          <el-form-item label="学生姓名" prop="userName">
            <el-input
              v-model.trim="queryParams.userName"
              placeholder="请输入学生姓名"
              clearable
              style="width: 160px;"
            />
          </el-form-item>
          <el-form-item label="指导教师" prop="guiderTeacherName">
            <el-input
              v-model.trim="queryParams.guiderTeacherName"
              placeholder="请输入指导教师"
              clearable
              style="width: 160px;"
            />
          </el-form-item>
          <el-form-item label="奖项" prop="awardsName">
            <el-input
              v-model.trim="queryParams.awardsName"
              placeholder="请输入奖项"
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
            <span class="diff-tip">表格淡红色背景的为非本赛季的数据！</span>
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
          <el-table-column label="团队编号" align="left" prop="teamCode" width="220" show-overflow-tooltip/>
          <el-table-column label="团队名称" align="left" prop="teamName" min-width="120" show-overflow-tooltip>
            <template #default="scope">
              <el-input v-if="isEditing" v-model="scope.row.teamName" size="small" />
              <span v-else>{{ scope.row.teamName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="学校" align="left" prop="schoolName" min-width="120" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.schoolName || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="赛道" align="left" min-width="130" show-overflow-tooltip sortable prop="competitionTrackName">
            <template #default="scope">
              {{ scope.row.competitionTrackName }}{{ scope.row.secondLevelName ? ' / ' + scope.row.secondLevelName : '' }}
            </template>
          </el-table-column>
          <el-table-column label="奖项" align="center" prop="awardsName" width="120" sortable show-overflow-tooltip>
            <template #default="scope">
              <template v-if="isEditing">
                <el-input v-model="scope.row.awardsName" size="small" style="width: 90px;" />
              </template>
              <template v-else>
                <el-tag :type="getAwardTagType(scope.row.awardsName)">{{ scope.row.awardsName }}</el-tag>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="学生姓名（可调整顺序）" align="left" min-width="180">
            <template #default="scope">
              <template v-if="isEditing">
                <div :class="['student-edit-list', 'student-sortable-' + scope.row.id]" :data-row-id="scope.row.id">
                  <div 
                    v-for="(student, idx) in scope.row.playerList" 
                    :key="scope.row.id + '-' + idx + '-' + student.userName"
                    class="student-edit-item"
                  >
                    <span class="student-index">{{ idx + 1 }}、</span>
                    <span class="student-name">{{ student.userName }}</span>
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
                      :disabled="idx === scope.row.playerList.length - 1"
                      @click="moveStudent(scope.row, idx, 1)"
                    />
                  </div>
                </div>
              </template>
              <span v-else>{{ formatStudentNames(scope.row.playerList) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="指导教师1" align="left" width="100">
            <template #default="scope">
              <el-input v-if="isEditing" v-model="scope.row.guiderTeacherList[0].userName" size="small" />
              <span v-else>{{ getTeacherName(scope.row, 1) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="指导教师2" align="left" width="100">
            <template #default="scope">
              <el-input v-if="isEditing" v-model="scope.row.guiderTeacherList[1].userName" size="small" />
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
import { ref, computed, nextTick } from 'vue'
import { getAwardPublicityDetail, getAwardDetailsList, removeAwardDetail, editAwardDetails, exportAwardDetails } from '@/api/tournament/awardPublicity'
import modal from '@/plugins/modal'
import Pagination from '@/components/Pagination'
import { Top, Bottom } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { handleAsyncExport } from '@/utils/export'

const emit = defineEmits(['success'])

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
  pageSize: 50
})

// 表格数据
const tableList = ref([])
const total = ref(0)

// 状态映射
function getStatusType(status) {
  const map = {
    '公示中': 'success',
    '未开始': 'primary',
    '已结束': 'info'
  }
  return map[status] || 'info'
}

function getAwardTagType(award) {
  const map = { '一等奖': 'danger', '二等奖': 'warning', '三等奖': 'success', '优秀奖': 'info' }
  return map[award] || ''
}

function formatStudentNames(playerList) {
  if (!playerList || playerList.length === 0) return '-'
  const sorted = [...playerList].sort((a, b) => (a.teamSort || 0) - (b.teamSort || 0))
  return sorted.map((m, idx) => `${idx + 1}、${m.userName}`).join('，')
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

/** 移动学生顺序 */
function moveStudent(row, index, direction) {
  const list = row.playerList
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
    awardPublicityId: currentRow.value.id,
    ...queryParams.value
  }
  getAwardDetailsList(params).then(response => {
    if (response.code === 200) {
      tableList.value = response.rows || []
      total.value = response.total || 0,
      totalSum.value = response.totalSum || 0
    }
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
  handleQuery()
}

/** 编辑 */
function handleEdit() {
  tableList.value.forEach(row => {
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
  const containers = document.querySelectorAll('.student-edit-list')
  containers.forEach(container => {
    const rowId = container.getAttribute('data-row-id')
    const rowIndex = tableList.value.findIndex(item => String(item.id) === rowId)
    if (rowIndex === -1) return
    Sortable.create(container, {
      handle: '.student-edit-item',
      animation: 150,
      onEnd: (evt) => {
        const currentRow = tableList.value[rowIndex]
        const playerList = currentRow.playerList
        const movedItem = playerList.splice(evt.oldIndex, 1)[0]
        playerList.splice(evt.newIndex, 0, movedItem)
        updateTeamSort(playerList)
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
  saveLoading.value = true
  editAwardDetails(changedRows).then(response => {
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

/** 删除获奖名单明细 */
function handleDetailDelete(row) {
  modal.confirm('确定要删除吗？').then(() => {
    removeAwardDetail(row.id).then(response => {
      if (response.code === 200) {
        modal.msgSuccess('删除成功')
        getList()
      } else {
        modal.msgError(response.msg || '删除失败')
      }
    })
  }).catch(() => {})
}

/** 导出 */
function handleExport(type) {
  let params = { awardPublicityId: currentRow.value.id, exportType: type }
  if (type === 'filter') {
    Object.assign(params, queryParams.value)
  }
  handleAsyncExport(exportAwardDetails, params)
}

/** 打开弹框 */
function openDialog(row) {
  visible.value = true
  currentRow.value = row
  isEditing.value = false
  fetchDetail(row.id)
  resetQuery()
}

function tableRowClassName({ row }) {
  console.log(row.competitionSeriesId , baseInfo.value.competitionSeriesId)
  if (row.competitionSeriesId !== baseInfo.value.competitionSeriesId) {
    return 'diff-series-row'
  }
  return ''
}

function fetchDetail(id) {
  getAwardPublicityDetail(id).then(response => {
    if (response.code === 200) {
      const data = response.data || {}
      baseInfo.value = data
    }
  })
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
