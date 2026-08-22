<template>
  <div class="app-container review-manage">
    <!-- 顶部批量操作栏 -->
    <BatchActionBar
      :selected-task-count="selectedTaskIds.length"
      :selected-expert-count="selectedExpertIds.length"
      :selected-expert-names="selectedExpertNames"
      :selected-tasks="selectedTasks"
      :selected-expert-objs="selectedExpertObjs"
      :selected-expert-group-count="selectedExpertGroups.length"
      :selected-expert-groups="selectedExpertGroups"
      :expert-panel-mode="expertPanelMode"
      @refresh="loadData"
      @refresh-experts="handleRefreshExperts"
      @clear-expert-selection="clearExpertSelection"
      @clear-group-selection="clearGroupSelection"
      @reset-expert-filter="resetExpertFilter"
    />

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 左侧任务区 -->
      <div class="left-panel">
        <div class="panel-title">评审任务</div>
        <TaskFilter
          :province-options="provinceOptions"
          :task-group-options="taskGroupOptions"
          @create-group="showCreateGroupDialog"
          @filter-change="handleFilterChange"
          @search="handleSearch"
          @reset="handleReset"
        />

        <!-- 任务表格 -->
        <div class="table-container">
          <el-table
            ref="taskTable"
            class="task-table"
            v-loading="loading"
            :data="filteredTaskList"
            height="100%"
            size="small"
            @selection-change="handleTaskSelectionChange"
            @row-click="handleRowClick"
          >
            <el-table-column type="selection" width="50" align="center" :selectable="isPdfFile" />
            <el-table-column prop="reviewName" label="项目名称" min-width="120">
              <template #default="{ row }">
                <el-tooltip :content="row.reviewName" placement="top" :show-after="500">
                  <div class="multi-line-ellipsis">{{ row.reviewName || '-'  }}</div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="competitionName" label="赛事/赛项/组别" min-width="150">
              <template #default="{ row }">
                <el-tooltip :content="`${row.competitionName || '-'}/${row.competitionTrackName || '-'}/${row.secondLevelName || '-'}`" placement="top" :show-after="500">
                  <div class="multi-line-ellipsis">
                    <span style="font-weight: bold;">{{ row.competitionName || '-' }}</span>
                    <span>/{{ row.competitionTrackName || '-' }}</span>
                    <span>/{{ row.secondLevelName || '-' }}</span>
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="schoolName" label="学校/省份/带队老师/队长" width="158" show-overflow-tooltip>
              <template #default="{ row }">
                <div style="display: flex;align-items: center;">
                  <div class="multi-line-ellipsis">{{ row.schoolName || '-' }}</div>
                  <el-tag size="small" type="primary" style="margin-left: 3px;">{{ row.province || '-' }}</el-tag>
                </div>
                <div class="sub-info multi-line-ellipsis">
                  {{ row.leaderTeacherName }}/{{ row.userName }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="reviewEndTime" label="审阅截止时间" width="95">
              <template #default="{ row }">
                <div class="deadline">{{ row.reviewEndTime || '-'  }}</div>
              </template>
            </el-table-column>
            <el-table-column label="所在组" width="105">
              <template #default="{ row }">
                <div v-if="row.reviewTaskAllotGroupRelationList?.length > 0" class="group-tags-scroll">
                  <template v-for="(relation, index) in row.reviewTaskAllotGroupRelationList" :key="index">
                    <el-tooltip :content="relation.allotGroupName" placement="top" :show-after="500">
                      <el-tag size="small" type="success" class="group-tag tag-ellipsis" @click="showGroupDetail(relation)">{{ relation.allotGroupName }}</el-tag>
                    </el-tooltip>
                  </template>
                </div>
                <span v-else class="text-gray">-</span>
              </template>
            </el-table-column>
            <el-table-column label="评审专家" width="160">
              <template #default="{ row }">
                <div v-if="row.reviewTaskSpecialistRelationList?.length > 0" class="expert-tags-scroll">
                 <template v-for="expert in row.reviewTaskSpecialistRelationList"
                            :key="expert.userId">
                    <el-tooltip placement="top" :show-after="500" :content="expert.userName">
                      <el-tag
                        size="small"
                        closable
                        class="group-tag tag-ellipsis expert-name-tag"
                        @click="showExpertDetailByName(expert.userName)"
                        @close="removeExpertFromTask(row, expert)">
                        {{ expert.userName }}
                      </el-tag>
                    </el-tooltip>
                 </template>
                </div>
                <span v-else class="text-gray">分配中</span>
              </template>
            </el-table-column>
            <el-table-column label="评审备注" width="80" show-overflow-tooltip>
              <template #default="{ row }">
                <span @click="showRemarkEditDialog(row)" style="cursor: pointer;color: #409eff;">{{ row.reviewDesc ? row.reviewDesc : '点击添加' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="参考文档" min-width="100">
              <template #default="{ row }">
                <div v-if="row.referenceDocument" class="document-tags">
                  <template v-for="(doc, index) in parseDocuments(row.referenceDocument)" :key="index">
                    <el-tooltip placement="top" :show-after="500" :content="doc.fileName">
                      <span @click="downloadDocument(doc)" class="document-tag tag-ellipsis">{{ doc.fileName }}</span>
                    </el-tooltip>
                  </template>
                </div>
                <span v-else class="text-gray">-</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 底部分页 -->
          <div class="table-footer">
            <pagination
              size="small"
              v-show="total > 0"
              :total="total"
              v-model:page="queryParams.pageNum"
              v-model:limit="queryParams.pageSize"
              :page-sizes="[50, 100, 150]"
              @pagination="handlePagination"
            />
          </div>
        </div>
      </div>

      <!-- 右侧专家面板 -->
      <div class="right-panel">
        <ExpertPanel
          ref="expertPanelRef"
          :experts="expertList"
          :expert-groups="expertGroupList"
          :loading="expertLoading"
          :group-loading="expertGroupLoading"
          :active-mode="expertPanelMode"
          :active-filter="currentExpertFilterStatus"
          @select-experts="handleExpertSelect"
          @select-group="handleExpertGroupSelect"
          @create-expert-group="showCreateExpertGroupDialog"
          @view-expert-tasks="showExpertTasksDialog"
          @mode-change="handleModeChange"
          @delete-expert-group="handleDeleteExpertGroup"
          @edit-expert-group="showEditExpertGroupDialog"
          @filter-change="handleExpertFilterChange"
        />
      </div>
    </div>

    <!-- 弹框组件 -->
    <!-- 创建任务分组弹框 - 由任务列表左上角"创建分组"按钮触发 -->
    <CreateTaskGroupDialog
      v-model="dialogs.createGroup.visible"
      :selected-tasks="selectedTasks"
      @success="handleCreateGroupSuccess"
    />

    <!-- 创建专家分组弹框 - 由右侧专家面板底部"创建专家分组"按钮触发 -->
    <CreateExpertGroupDialog
      v-model="dialogs.createExpertGroup.visible"
      :selected-experts="selectedExpertObjs"
      @success="handleCreateExpertGroupSuccess"
      @switch-to-group="switchToGroupMode"
    />

    <!-- 编辑评审备注弹框 - 由表格"评审备注"列的"点击添加"文字触发 -->
    <EditRemarkDialog
      v-model="dialogs.editRemark.visible"
      :task="dialogs.editRemark.task"
      @success="loadData"
    />

    <!-- 专家已分配任务弹框 - 由右侧专家面板专家卡片上的"查看已分配项目"按钮触发 -->
    <ExpertTasksDialog
      v-model="dialogs.expertTasks.visible"
      :expert="dialogs.expertTasks.expert"
    />

    <!-- 分组详情弹框 - 由表格"所在组"列的组标签点击触发 -->
    <GroupDetailDialog
      v-model="dialogs.groupDetail.visible"
      :group-name="dialogs.groupDetail.groupName"
      :tasks="dialogs.groupDetail.tasks"
      @remove="handleRemoveFromGroup"
      @batchRemove="handleBatchRemoveFromGroup"
    />

    <!-- 专家详情弹框 - 由表格"评审专家"列的专家标签点击触发 -->
    <ExpertDetailDialog
      v-model="dialogs.expertDetail.visible"
      :expert="dialogs.expertDetail.expert"
      :loading="dialogs.expertDetail.loading"
    />

    <!-- 编辑专家组弹框 - 由右侧专家面板专家组的编辑按钮触发 -->
    <EditExpertGroupDialog
      v-model="dialogs.editExpertGroup.visible"
      :group="dialogs.editExpertGroup.group"
      :experts="expertList"
      @success="handleEditExpertGroupSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BatchActionBar from './components/BatchActionBar.vue'
import TaskFilter from './components/TaskFilter.vue'
import ExpertPanel from './components/ExpertPanel.vue'
import CreateTaskGroupDialog from './dialog/CreateTaskGroupDialog.vue'
import CreateExpertGroupDialog from './dialog/CreateExpertGroupDialog.vue'
import EditRemarkDialog from './dialog/EditRemarkDialog.vue'
import ExpertTasksDialog from './dialog/ExpertTasksDialog.vue'
import GroupDetailDialog from './dialog/GroupDetailDialog.vue'
import ExpertDetailDialog from './dialog/ExpertDetailDialog.vue'
import EditExpertGroupDialog from './dialog/EditExpertGroupDialog.vue'
import {
  getReviewTaskList,
  getExpertList,
  getExpertGroupList,
  deleteExpertGroup,
  getProvinceOptions,
  getTaskGroupOptions,
  getGroupDetail,
  removeGroupRelation,
  createTaskGroup,
  createExpertGroup,
  removeSpecialistFromTask
} from '@/api/tournament/reviewManage'
import { ossFileFuc } from '@/hooks/download'

const { downloadOssFile } = ossFileFuc()

// 加载状态
const loading = ref(false)
const expertLoading = ref(false)
const expertGroupLoading = ref(false)

// ExpertPanel 组件 ref
const expertPanelRef = ref(null)

// 数据列表
const taskList = ref([])
const expertList = ref([])
const expertGroupList = ref([])

// 分页参数
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 50
})

// 选项数据
const provinceOptions = ref([])
const taskGroupOptions = ref([])

// 选中状态
const selectedTaskIds = ref([])
const selectedExpertIds = ref([])
const selectedExpertGroupIds = ref([])  // 改为多选专家组

// 当前专家面板模式（'expert' 或 'group'）
const expertPanelMode = ref('expert')

// 筛选条件
const currentFilter = ref({})

// 当前专家筛选状态（all/assigned/free）
const currentExpertFilterStatus = ref('')

// 弹框状态
const dialogs = reactive({
  createGroup: { visible: false },
  createExpertGroup: { visible: false },
  editRemark: { visible: false, task: null },
  expertTasks: { visible: false, expert: null },
  groupDetail: { visible: false, groupName: '', reviewGroupId: null, tasks: [] },
  expertDetail: { visible: false, expert: null, loading: false },
  editExpertGroup: { visible: false, group: null }
})

// 计算属性
const selectedTasks = computed(() => {
  return taskList.value.filter(t => selectedTaskIds.value.includes(t.reviewId))
})

const selectedExpertObjs = computed(() => {
  return expertList.value.filter(e => selectedExpertIds.value.includes(e.userId))
})

const selectedExpertNames = computed(() => {
  return selectedExpertObjs.value.map(e => e.nickName).join('、')
})

// 选中的专家组
const selectedExpertGroups = computed(() => {
  // 过滤掉无效的groupId，只返回存在于 expertGroupList 中的组
  return expertGroupList.value.filter(g => g.groupId && selectedExpertGroupIds.value.includes(g.groupId))
})

// 合并单个选择的专家和专家组内的专家（用于分配）
const selectedAllExpertObjs = computed(() => {
  const experts = [...selectedExpertObjs.value]
  const expertIds = new Set(selectedExpertIds.value)
  
  // 将选中专家组内的专家展开添加
  selectedExpertGroups.value.forEach(group => {
    group.reviewGroupSpecialistRelationList?.forEach(relation => {
      const userId = relation.userId
      if (!expertIds.has(userId)) {
        const expert = expertList.value.find(e => e.userId === userId)
        if (expert) {
          experts.push(expert)
          expertIds.add(userId)
        }
      }
    })
  })
  
  return experts
})

// 直接使用后端返回的数据（服务端已分页）
const filteredTaskList = computed(() => {
  return taskList.value
})

// 监听筛选条件变化，重置分页
watch(() => currentFilter.value, () => {
  queryParams.pageNum = 1
}, { deep: true })



// 初始化加载数据
onMounted(() => {
  initLoad()
})

// 初始化加载（包含省份等选项数据）
function initLoad() {
  loadTaskData()
  loadOptions()
  loadExpertList()
  loadExpertGroupList()
}

// 加载选项数据（省份、任务分组等，初始化时只加载一次）
function loadOptions() {
  // 使用 .then() 替代 Promise.all，互不影响
  getProvinceOptions().then(provinceRes => {
    // 处理省份数据，适配下拉组件格式
    const provinceData = provinceRes.data || []
    provinceOptions.value = provinceData.map(item => ({
      label: item.province || item,
      value: item.province || item
    }))
  })
  
  getTaskGroupOptions().then(taskGroupRes => {
    taskGroupOptions.value = taskGroupRes.data || []
  })
}

// 加载任务数据（搜索、重置、分页时调用）
function loadTaskData() {
  // 构建查询参数，包含分页和筛选条件
  const { competitionName, competitionTrackQuery, province, taskGroupId, keyWords, distributeStatus } = currentFilter.value || {}
  const query = {
    pageNum: queryParams.pageNum,
    pageSize: queryParams.pageSize,
    ...(competitionName && { competitionName }),
    ...(competitionTrackQuery && { competitionTrackQuery }),
    ...(province && { province }),
    ...(taskGroupId && { taskGroupId }),
    ...(keyWords && { keyWords }),
    ...(distributeStatus !== '' && distributeStatus !== undefined && { distributeStatus })
  }
  loading.value = true
  getReviewTaskList(query).then(taskRes => {
    // 直接使用API返回的数据，不做字段映射
    taskList.value = taskRes.rows || taskRes.data || []
    // 更新总条数
    total.value = taskRes.total || 0
  }).finally(() => {
    loading.value = false
  })
}

// 加载专家列表
function loadExpertList(distributeStatus = '') {
  expertLoading.value = true
  const data = distributeStatus ? { distributeStatus } : {}
  getExpertList(data).then(expertRes => {
    // 专家列表接口不分页，直接取 data
    expertList.value = expertRes.data || []
  }).finally(() => {
    expertLoading.value = false
  })
}

// 加载专家组列表
function loadExpertGroupList() {
  expertGroupLoading.value = true
  getExpertGroupList().then(groupRes => {
    const data = groupRes.rows || groupRes.data || []
    // 直接使用API返回的数据，组件中使用API原始字段
    expertGroupList.value = data
  }).finally(() => {
    expertGroupLoading.value = false
  })
}

// 兼容旧调用（搜索、重置、分页时使用）
async function loadData() {
  loading.value = true
  try {
    await loadTaskData()
  } finally {
    loading.value = false
  }
}

// 判断是否是PDF文件（以.pdf结尾，不区分大小写）
function isPdfFile(row) {
  if (!row || !row.reviewName) return false
  return row.reviewName.toLowerCase().endsWith('.pdf')
}

// 事件处理
function handleTaskSelectionChange(selection) {
  selectedTaskIds.value = selection.map(item => item.reviewId)
}

// 行点击事件 - 切换选中状态（仅特定字段列可点击）
const taskTable = ref(null)
function handleRowClick(row, column) {
  // 仅以下字段列点击时可切换选中状态
  const clickableProperties = ['reviewName', 'competitionName', 'schoolName', 'reviewEndTime']
  if (!clickableProperties.includes(column.property)) {
    return
  }
  // 非PDF文件不允许选中
  if (!isPdfFile(row)) {
    return
  }
  const table = taskTable.value
  if (table) {
    nextTick(() => {
      table.toggleRowSelection(row)
    })
  }
}

function handleFilterChange(filter) {
  currentFilter.value = filter
}

// 搜索按钮点击
function handleSearch(filter) {
  currentFilter.value = filter
  queryParams.pageNum = 1
  // 触发数据重新加载
  loadData()
}

// 重置按钮点击
function handleReset() {
  currentFilter.value = {}
  queryParams.pageNum = 1
  // 触发数据重新加载
  loadData()
}

function handleExpertSelect(ids) {
  selectedExpertIds.value = ids
}

function handleExpertGroupSelect(groupId) {
  // 防止无效值被添加
  if (!groupId) return
  const index = selectedExpertGroupIds.value.indexOf(groupId)
  if (index > -1) {
    selectedExpertGroupIds.value.splice(index, 1)
  } else {
    selectedExpertGroupIds.value.push(groupId)
  }
}

// 处理专家面板模式切换
function handleModeChange(mode) {
  expertPanelMode.value = mode
  // 切换模式时清空对方的选择，实现互斥
  if (mode === 'expert') {
    // 切换到专家模式，清空专家组选择
    selectedExpertGroupIds.value = []
  } else if (mode === 'group') {
    // 切换到专家组模式，清空专家选择
    selectedExpertIds.value = []
  }
}

// 处理专家状态筛选变化
function handleExpertFilterChange(distributeStatus) {
  currentExpertFilterStatus.value = distributeStatus
  loadExpertList(distributeStatus)
}

// 刷新专家列表（保持当前筛选状态）
function handleRefreshExperts() {
  loadExpertList(currentExpertFilterStatus.value)
}

// 重置专家筛选为全部
function resetExpertFilter() {
  currentExpertFilterStatus.value = ''
  loadExpertList('')
}

// 分页变化处理
function handlePagination() {
  // 服务端分页，需要重新加载数据
  loadData()
}

// 弹框显示
function showCreateGroupDialog() {
  if (selectedTaskIds.value.length === 0) {
    ElMessage.warning('请先选择任务')
    return
  }
  dialogs.createGroup.visible = true
  dialogs.createGroup.name = ''
}

function showCreateExpertGroupDialog(expertIds) {
  if (expertIds.length === 0) {
    ElMessage.warning('请先选择专家')
    return
  }
  selectedExpertIds.value = expertIds
  dialogs.createExpertGroup.visible = true
  dialogs.createExpertGroup.name = ''
}

function showRemarkEditDialog(task) {
  dialogs.editRemark.task = task
  dialogs.editRemark.value = task.remark || ''
  dialogs.editRemark.visible = true
}

function showExpertTasksDialog(expert) {
  dialogs.expertTasks.expert = expert
  dialogs.expertTasks.visible = true
}

// 根据姓名显示专家详情
async function showExpertDetailByName(name) {
  try {
    // 先从本地列表中查找专家，获取 userId
    const localExpert = expertList.value.find(e => e.userName === name || e.nickName === name)
    if (!localExpert?.userId) {
      ElMessage.error('专家信息获取失败')
      return
    }

    // 先展示弹框，使用本地已有数据作为初始展示
    dialogs.expertDetail.expert = { ...localExpert }
    dialogs.expertDetail.visible = true
    dialogs.expertDetail.loading = true

    // 调用 API 获取专家详情
    const res = await getExpertList({ userId: localExpert.userId })
    if (res.code === 200 && res.data && res.data.length > 0) {
      dialogs.expertDetail.expert = res.data[0]
    } else {
      ElMessage.error('专家详情获取失败')
    }
  } catch (error) {
    ElMessage.error('获取专家详情时出错')
  } finally {
    dialogs.expertDetail.loading = false
  }
}

// 清空专家选择
function clearExpertSelection() {
  selectedExpertIds.value = []
}

// 创建任务分组成功后的处理
async function handleCreateGroupSuccess() {
  // 同步请求：先刷新任务列表，成功后刷新分组列表和分组详情
  await loadTaskData()
  await loadTaskGroupOptions()
}

// 创建专家分组成功后的处理
function handleCreateExpertGroupSuccess() {
  // 刷新任务数据
  loadData()
}

// 编辑专家分组成功后的处理
function handleEditExpertGroupSuccess() {
  // 刷新专家组列表
  loadExpertGroupList()
}

// 加载任务分组选项
async function loadTaskGroupOptions() {
  const res = await getTaskGroupOptions()
  if (res.code === 200) {
    taskGroupOptions.value = res.data || []
  }
}

// 切换到专家组模式
function switchToGroupMode() {
  // 切换到专家组模式
  expertPanelMode.value = 'group'
  // 清空专家选择
  selectedExpertIds.value = []
  selectedExpertGroupIds.value = []
  // 刷新专家组列表
  loadExpertGroupList()
}

// 清空专家组选择
function clearGroupSelection() {
  selectedExpertGroupIds.value = []
}

// 显示分组详情
async function showGroupDetail(relation) {
  try {
    const res = await getGroupDetail(relation.reviewGroupId)
    if (res.code === 200) {
      dialogs.groupDetail.groupName = res.data?.allotGroupName || relation.allotGroupName
      dialogs.groupDetail.reviewGroupId = relation.reviewGroupId
      dialogs.groupDetail.tasks = res.data?.reviewTaskAllotGroupRelationList || []
      dialogs.groupDetail.visible = true
    } else {
      ElMessage.error(res.msg || '获取分组详情失败')
    }
  } catch (error) {
    ElMessage.error('获取分组详情时出错')
  }
}

// 解析参考文档JSON
function parseDocuments(referenceDocument) {
  if (!referenceDocument) return []
  try {
    return JSON.parse(referenceDocument)
  } catch {
    return []
  }
}

// 下载文档
function downloadDocument(doc) {
  if (!doc.fileUrl) {
    ElMessage.warning('文档链接不存在')
    return
  }
  // 使用 downloadOssFile 方法，通过后端获取临时下载链接
  downloadOssFile(doc.fileUrl, doc.fileName || '下载文件')
}

// 分组详情移除处理
async function handleRemoveFromGroup(row) {
  try {
    const res = await removeGroupRelation({
      reviewIdList: [row.reviewId],
      reviewGroupIdList: [dialogs.groupDetail.reviewGroupId]
    })
    if (res.code === 200) {
      // 同步请求：先刷新任务列表，成功后刷新分组列表和分组详情
      await loadTaskData()
      await loadTaskGroupOptions()
      // 重新加载分组详情
      const detailRes = await getGroupDetail(dialogs.groupDetail.reviewGroupId)
      if (detailRes.code === 200) {
        dialogs.groupDetail.tasks = detailRes.data?.reviewTaskAllotGroupRelationList || []
      }
      ElMessage.success('移除成功')
    } else {
      ElMessage.error(res.msg || '移除失败')
    }
  } catch (error) {
    ElMessage.error('移除时出错')
  }
}

async function handleBatchRemoveFromGroup(ids) {
  try {
    const res = await removeGroupRelation({
      reviewIdList: ids,
      reviewGroupIdList: [dialogs.groupDetail.reviewGroupId]
    })
    if (res.code === 200) {
      // 同步请求：先刷新任务列表，成功后刷新分组列表和分组详情
      await loadTaskData()
      await loadTaskGroupOptions()
      // 重新加载分组详情
      const detailRes = await getGroupDetail(dialogs.groupDetail.reviewGroupId)
      if (detailRes.code === 200) {
        dialogs.groupDetail.tasks = detailRes.data?.reviewTaskAllotGroupRelationList || []
      }
      ElMessage.success('移除成功')
    } else {
      ElMessage.error(res.msg || '移除失败')
    }
  } catch (error) {
    ElMessage.error('移除时出错')
  }
}

// 删除专家组
async function handleDeleteExpertGroup(group) {
  try {
    await ElMessageBox.confirm(
      `确定删除专家组「${group.groupName}」吗？删除后不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteExpertGroup(group.groupId)
    ElMessage.success('删除成功')
    loadExpertGroupList()
  } catch (error) {
    // 用户取消或删除失败
  }
}

// 显示编辑专家组弹框
function showEditExpertGroupDialog(group) {
  dialogs.editExpertGroup.group = group
  dialogs.editExpertGroup.visible = true
}

async function removeExpertFromTask(task, expert) {
  try {
    await ElMessageBox.confirm(
      `确定从该项目中移除专家「${expert.userName}」吗？`,
      '确认移除',
      { type: 'warning' }
    )
    const res = await removeSpecialistFromTask({
      reviewIdList: [task.reviewId],
      userIdList: [expert.userId]
    })
    if (res.code === 200) {
      // 移除成功后刷新任务列表和专家列表
      await loadTaskData()
      await loadExpertList(currentExpertFilterStatus.value)
      ElMessage.success('移除成功')
    } else {
      ElMessage.error(res.msg || '移除失败')
    }
  } catch {
    // 取消或请求失败不刷新列表
  }
}
</script>

<style scoped lang="scss">
.review-manage {
  .main-content {
    display: flex;
    gap: 12px;
    height: calc(100vh - 160px);
    min-height: 500px;

    .left-panel {
      flex: 2;
      display: flex;
      flex-direction: column;
      background: #fff;
      padding: 8px;
      overflow: hidden;
      border: 1px solid #ebeef5;
      min-height: 0;
      .panel-title {
        padding: 8px 6px;
        font-weight: 600;
        font-size: 14px;
        background: #f5f5f5;
        width: calc(100% + 16px);
        margin: -8px 0 6px -8px;
      }
      .table-container {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        align-items: flex-start;
        min-height: 0;
        
        .sub-info {
          display: flex;
          align-items: center;
          gap: 6px;
          margin-top: 4px;
          font-size: 12px;
          color: #606266;
        }

        .deadline {
          font-size: 13px;
          color: #606266;
          cursor: pointer;
        }

        .expert-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 4px;
        }

        .expert-tags-scroll {
          display: flex;
          flex-wrap: wrap;
          gap: 4px;
          max-height: 60px;
          overflow-y: auto;
          overflow-x: hidden;
          padding-right: 2px;

          &::-webkit-scrollbar {
            width: 4px;
          }

          &::-webkit-scrollbar-thumb {
            background-color: #c0c4cc;
            border-radius: 2px;
          }

          &::-webkit-scrollbar-track {
            background-color: #f5f7fa;
          }
        }

        .group-tags-scroll {
          display: flex;
          flex-wrap: wrap;
          gap: 4px;
          max-height: 60px;
          overflow-y: auto;
          overflow-x: hidden;
          padding-right: 2px;

          &::-webkit-scrollbar {
            width: 4px;
          }

          &::-webkit-scrollbar-thumb {
            background-color: #c0c4cc;
            border-radius: 2px;
          }

          &::-webkit-scrollbar-track {
            background-color: #f5f7fa;
          }
        }

        .document-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 4px;
          max-height: 60px;
          overflow-y: auto;
          overflow-x: hidden;
          padding-right: 2px;

          &::-webkit-scrollbar {
            width: 4px;
          }

          &::-webkit-scrollbar-thumb {
            background-color: #c0c4cc;
            border-radius: 2px;
          }

          &::-webkit-scrollbar-track {
            background-color: #f5f7fa;
          }
        }

        .document-tag {
          cursor: pointer;
          color: #409eff;
          &:hover {
            opacity: 0.8;
          }
        }

        .text-gray {
          color: #909399;
        }

        .group-tag {
          cursor: pointer;
          transition: opacity 0.2s;

          &:hover {
            opacity: 0.8;
          }
        }

        .tag-ellipsis {
          max-width: 90px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          :deep(.el-tag__content) {
            max-width: 90px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        .multi-line-ellipsis {
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
          overflow: hidden;
          text-overflow: ellipsis;
          line-height: 1.5;
          max-height: 4.5em;
          cursor: pointer;
        }

        .table-footer {
          display: flex;
          justify-content: flex-start;
          font-size: 13px;
          color: #606266;
          border-top: 1px solid #ebeef5;
          flex-shrink: 0;
          :deep(.pagination-container) {
            margin-top: 12px;
            margin-left: 10px;
          }
        }
      }
    }

    .right-panel {
      flex: 0.55;
      min-width: 280px;
      max-width: 360px;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      border: 1px solid #ebeef5;
    }
  }

  .expert-name-tag {
    cursor: pointer;

    &:hover {
      opacity: 0.8;
    }
  }
}
:deep(.el-table--small)  {
  .cell{
    padding: 0 4px !important;
  }
}
</style>
