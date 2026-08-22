<template>
  <div class="batch-action-bar">
    <!-- 左侧选中信息 - CSS实现hover显示完整内容 -->
    <div class="selected-info-wrapper">
      <el-tooltip class="selected-info-tooltip"
                  :content="selectedInfoText"
                  placement="top">
        <div class="selected-info">
            <span v-if="selectedTaskCount && selectedTaskCount > 0">已选择 <span class="num">{{ selectedTaskCount }}</span> 个评审任务，</span>
            <span v-else>未选择任务，</span>
            <!-- 根据当前模式显示对应文案 -->
            <span v-if="expertPanelMode === 'expert'">
              <span v-if="selectedExpertCount > 0">已选择 <span class="num">{{ selectedExpertCount }}</span> 个评审专家<span v-if="selectedExpertNames">：{{ selectedExpertNames }}</span></span>
              <span v-else>未选择专家</span>
            </span>
            <span v-else-if="expertPanelMode === 'group'">
              <span v-if="selectedExpertGroupCount > 0">已选择 <span class="num">{{ selectedExpertGroupCount }}</span> 个专家组<span v-if="selectedExpertGroupNames">：{{ selectedExpertGroupNames }}</span></span>
              <span v-else>未选择专家组</span>
            </span>
        </div>
      </el-tooltip>
    </div>

    <!-- 右侧操作按钮 -->
    <div class="action-btns">
      <el-button type="primary" size="small" @click="handleBatchDoc">批量设置参考文档</el-button>
      <el-button type="primary" size="small" @click="handleBatchDeadline">批量设置截止时间</el-button>
      <el-button type="primary" size="small" @click="handleBatchRemark">批量设置给专家的备注</el-button>
      <el-button type="danger" size="small" @click="handleBatchRemove">批量移除专家</el-button>
      <el-button type="success" size="small" @click="handleConfirmAssign">确认分配</el-button>
    </div>

    <!-- 弹框：批量设置参考文档 -->
    <el-dialog v-model="dialogs.doc.visible" title="批量设置参考文档" width="480px">
      <el-form label-position="top">
        <el-form-item :label="`已选${selectedTasks?.length || 0}个任务`">
          <div class="selected-list">
            <div v-for="(task, index) in selectedTasks" :key="task?.reviewId" class="selected-item">
              {{index + 1}}、 {{ task?.reviewName }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="参考文档">
          <div class="upload-tip">选择给专家参考的文献、选择的文献可以在专家审阅时下载，最多10个文件（如果不传则清空已选任务所有参考文档）</div>
          <FileUpload
            ref="docFileUploadRef"
            v-model="dialogs.doc.fileList"
            :limit="10"
            multiple
            :oss-config="{ bizSign: 'review', bizCode: 'set' }"
            value-type="array"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogs.doc.visible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="dialogs?.doc?.loading || isDocUploading" 
          :disabled="isDocUploading"
          @click="saveBatchDoc"
        >
          {{ isDocUploading ? '上传中...' : '保存' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 弹框：批量设置截止时间 -->
    <el-dialog v-model="dialogs.deadline.visible" title="批量设置审阅截止时间" width="480px">
      <el-form label-position="top">
        <el-form-item :label="`已选${selectedTasks?.length || 0}个任务`">
          <div class="selected-list">
            <div v-for="(task, index) in selectedTasks" :key="task?.reviewId" class="selected-item">
              {{index + 1}}、 {{ task?.reviewName }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="截止时间" required>
          <el-date-picker
            v-model="dialogs.deadline.value"
            type="datetime"
            placeholder="选择截止时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            size="small"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogs.deadline.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialogs?.deadline?.loading" @click="saveBatchDeadline">保存</el-button>
      </template>
    </el-dialog>

    <!-- 弹框：批量设置评审备注 -->
    <el-dialog v-model="dialogs.remark.visible" title="批量设置评审备注" width="480px">
      <el-form label-position="top">
        <el-form-item :label="`已选${selectedTasks?.length || 0}个任务`">
          <div class="selected-list">
            <div v-for="(task, index) in selectedTasks" :key="task?.reviewId" class="selected-item">
              {{index + 1}}、 {{ task?.reviewName }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="评审备注">
          <el-input
            v-model="dialogs.remark.value"
            type="textarea"
            :rows="3"
            placeholder="请输入需要传达给评审专家的备注信息（空备注也可保存）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogs.remark.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialogs?.remark?.loading" @click="saveBatchRemark">保存</el-button>
      </template>
    </el-dialog>

    <!-- 弹框：批量移除专家 -->
    <el-dialog v-model="dialogs.remove.visible" title="批量移除专家" width="520px">
      <el-form label-position="top">
        <el-form-item :label="`已选${selectedTasks?.length || 0}个任务`">
          <div class="selected-list">
            <div v-for="(task, index) in selectedTasks" :key="task?.reviewId" class="selected-item">
              {{index + 1}}、 {{ task?.reviewName }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="选择专家">
          <div>
            <div class="upload-tip">(仅显示已选任务中已分配的专家)</div>
            <div class="select-all-bar">
              <el-button type="primary" round plain size="small" @click="selectAllExperts">选中所有专家</el-button>
              <el-button type="warning" round plain size="small" @click="clearExpertSelection">取消选中所有专家</el-button>
              <span class="selected-count">已选中 {{ dialogs?.remove?.selectedExpertIds?.length || 0 }} 个专家</span>
            </div>
            <el-checkbox-group v-model="dialogs.remove.selectedExpertIds" class="expert-checkbox-group">
              <el-checkbox
                v-for="expert in removableExperts"
                :key="expert.userId"
                :label="expert.userId"
              >
                {{ expert.userName }}
              </el-checkbox>
            </el-checkbox-group>
            <div v-if="removableExperts?.length === 0" class="empty-tip">已选任务中无分配专家</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogs.remove.visible = false">取消</el-button>
        <el-button type="danger" :loading="dialogs?.remove?.loading" @click="confirmBatchRemove">确认移除</el-button>
      </template>
    </el-dialog>

    <!-- 弹框：确认分配 -->
    <el-dialog v-model="dialogs.assign.visible" title="确认分配" width="480px">
      <el-form label-position="top">
        <el-form-item :label="`已选${selectedTasks?.length || 0}个任务`">
          <div class="selected-list">
            <div v-for="(task, index) in selectedTasks" :key="task?.reviewId" class="selected-item">
              {{index + 1}}、 {{ task?.reviewName }}
            </div>
          </div>
        </el-form-item>
        <!-- 显示已选专家 -->
        <el-form-item v-if="selectedExpertObjs?.length > 0" :label="`已选${selectedExpertObjs?.length || 0}个专家`">
          <div class="selected-list">
            <div v-for="(expert, index) in selectedExpertObjs" :key="expert?.userId" class="selected-item">
              {{index + 1}}、 {{ expert?.nickName }}（{{ expert?.schoolName }}）
            </div>
          </div>
        </el-form-item>
        <!-- 显示已选专家组 -->
        <el-form-item v-if="selectedExpertGroups?.length > 0" :label="`已选${selectedExpertGroups?.length || 0}个专家组`">
          <div class="selected-list">
            <div v-for="(group, index) in selectedExpertGroups" :key="group?.groupId" class="selected-item">
              {{index + 1}}、 {{ group?.groupName }}（{{ group?.reviewGroupSpecialistRelationList?.length || 0 }}人）
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogs.assign.visible = false">取消</el-button>
        <el-button type="success" :loading="dialogs?.assign?.loading" @click="confirmAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  batchSetDeadline,
  batchSetRemark,
  batchSetDoc,
  batchRemoveExpert,
  confirmAssignApi,
  removeSpecialistFromTask
} from '@/api/tournament/reviewManage'

const props = defineProps({
  selectedTaskCount: { type: Number, default: 0 },
  selectedExpertCount: { type: Number, default: 0 },
  selectedExpertNames: { type: String, default: '' },
  selectedTasks: { type: Array, default: () => [] },
  selectedExpertObjs: { type: Array, default: () => [] },
  selectedExpertGroupCount: { type: Number, default: 0 },
  selectedExpertGroups: { type: Array, default: () => [] },
  expertPanelMode: { type: String, default: 'expert' }  // 'expert' 或 'group'
})

const emit = defineEmits(['refresh', 'refresh-experts', 'reset-expert-filter'])

// FileUpload 组件 ref
const docFileUploadRef = ref(null)

// 选中专家组名称
const selectedExpertGroupNames = computed(() => {
  if (!props.selectedExpertGroups || props.selectedExpertGroups.length === 0) return ''
  return props.selectedExpertGroups.map(g => g.groupName).join('、')
})

// tooltip 显示文本，与下方展示内容保持一致
const selectedInfoText = computed(() => {
  const taskText = props.selectedTaskCount && props.selectedTaskCount > 0
    ? `已选择 ${props.selectedTaskCount} 个评审任务`
    : '未选择任务'
  
  let expertText = ''
  // 根据当前模式显示对应的tooltip内容
  if (props.expertPanelMode === 'expert') {
    if (props.selectedExpertCount && props.selectedExpertCount > 0) {
      expertText = `已选择 ${props.selectedExpertCount} 个评审专家：${props.selectedExpertNames}`
    } else {
      expertText = '未选择专家'
    }
  } else if (props.expertPanelMode === 'group') {
    if (props.selectedExpertGroupCount && props.selectedExpertGroupCount > 0) {
      expertText = `已选择 ${props.selectedExpertGroupCount} 个专家组：${selectedExpertGroupNames.value}`
    } else {
      expertText = '未选择专家组'
    }
  }
  
  return `${taskText}，${expertText}`
})

// 可移除的专家列表（根据userId去重）
const removableExperts = computed(() => {
  const expertMap = new Map()
  props.selectedTasks?.forEach(task => {
    task?.reviewTaskSpecialistRelationList?.forEach(expert => {
      if (expert.userId && !expertMap.has(expert.userId)) {
        expertMap.set(expert.userId, {
          userId: expert.userId,
          userName: expert.userName || expert.specialistName || '-',
          schoolName: expert.schoolName || '-'
        })
      }
    })
  })
  return Array.from(expertMap.values())
})

// 弹框状态
const dialogs = reactive({
  doc: { visible: false, fileList: [], loading: false },
  deadline: { visible: false, value: '', loading: false },
  remark: { visible: false, value: '', loading: false },
  remove: { visible: false, selectedExpertIds: [], loading: false },
  assign: { visible: false, loading: false }
})

// 是否有文件正在上传
const isDocUploading = computed(() => {
  return docFileUploadRef.value?.isUploading || false
})

// 按钮点击处理
function handleBatchDoc() {
  if (props.selectedTaskCount === 0) {
    ElMessage.warning('请先选择任务')
    return
  }
  dialogs.doc.visible = true
  dialogs.doc.fileList = []
}

function handleBatchDeadline() {
  if (props.selectedTaskCount === 0) {
    ElMessage.warning('请先选择任务')
    return
  }
  dialogs.deadline.visible = true
  dialogs.deadline.value = ''
}

function handleBatchRemark() {
  if (props.selectedTaskCount === 0) {
    ElMessage.warning('请先选择任务')
    return
  }
  dialogs.remark.visible = true
  dialogs.remark.value = ''
}

function handleBatchRemove() {
  if (props.selectedTaskCount === 0) {
    ElMessage.warning('请先选择任务')
    return
  }
  dialogs.remove.visible = true
  dialogs.remove.selectedExpertIds = []
}

// 全选专家
function selectAllExperts() {
  dialogs.remove.selectedExpertIds = removableExperts.value.map(e => e.userId)
}

// 取消选择
function clearExpertSelection() {
  dialogs.remove.selectedExpertIds = []
}

function handleConfirmAssign() {
  if (props.selectedTaskCount === 0) {
    ElMessage.warning('请先选择任务')
    return
  }
  if (props.selectedExpertCount === 0 && props.selectedExpertGroupCount === 0) {
    ElMessage.warning('请先选择专家或专家组')
    return
  }
  dialogs.assign.visible = true
}

// 保存操作
async function saveBatchDoc() {
  // 检查是否有文件正在上传
  if (isDocUploading.value) {
    ElMessage.warning('文件正在上传中，请等待上传完成后再保存')
    return
  }
  
  // FileUpload 组件设置了 value-type="array"，dialogs.doc.fileList 已经是完整对象数组
  const fileArray = dialogs.doc.fileList || []
  let fileString = ''
  try {
    console.log(fileArray,'xxxx')
    if(fileArray && fileArray.length > 0){
      // 构建文件数组，包含 fileUrl、fileName、fileSize
      const fileList = fileArray.map(file => ({
        fileUrl: file.url || file.fileUrl || '',
        fileName: file.name || file.fileName || '',
        fileSize: file.size || 0
      }))
      fileString = JSON.stringify(fileList)
    }else{
      fileString = ''
    }
  } catch (error) {
    fileString = ''
  }
  dialogs.doc.loading = true
  try {
    // 调用批量设置参考文档接口
    await batchSetDoc({
      reviewIdList: props.selectedTasks?.map(t => t?.reviewId) || [],
      referenceDocument: fileString || ''
    })
    ElMessage.success('设置成功')
    dialogs.doc.visible = false
    dialogs.doc.fileList = []
    emit('refresh')
  } finally {
    dialogs.doc.loading = false
  }
}

async function saveBatchDeadline() {
  if (!dialogs.deadline.value) {
    ElMessage.warning('请选择截止时间')
    return
  }
  dialogs.deadline.loading = true
  try {
    await batchSetDeadline({
      reviewIdList: props.selectedTasks?.map(t => t?.reviewId) || [],
      reviewEndTime: dialogs.deadline.value
    })
    ElMessage.success('设置成功')
    dialogs.deadline.visible = false
    emit('refresh')
  } finally {
    dialogs.deadline.loading = false
  }
}

async function saveBatchRemark() {
  dialogs.remark.loading = true
  try {
    await batchSetRemark({
      reviewIdList: props.selectedTasks?.map(t => t?.reviewId) || [],
      reviewDesc: dialogs.remark.value || ''
    })
    ElMessage.success('设置成功')
    dialogs.remark.visible = false
    emit('refresh')
  } finally {
    dialogs.remark.loading = false
  }
}

async function confirmBatchRemove() {
  if (dialogs.remove.selectedExpertIds?.length === 0) {
    ElMessage.warning('请至少选择一位专家')
    return
  }
  dialogs.remove.loading = true
  try {
    const res = await removeSpecialistFromTask({
      reviewIdList: props.selectedTasks?.map(t => t?.reviewId) || [],
      userIdList: dialogs.remove.selectedExpertIds
    })
    if (res.code === 200) {
      ElMessage.success('移除成功')
      dialogs.remove.visible = false
      dialogs.remove.selectedExpertIds = []
      emit('refresh')
      emit('refresh-experts')
    } else {
      ElMessage.error(res.msg || '移除失败')
    }
  } finally {
    dialogs.remove.loading = false
  }
}

async function confirmAssign() {
  dialogs.assign.loading = true
  try {
    // 收集所有专家ID（包括单独选择的专家和专家组内的专家）
    const expertIds = new Set()
    
    // 添加单独选择的专家
    props.selectedExpertObjs?.forEach(e => expertIds.add(e?.userId))
    
    // 添加专家组内的专家
    props.selectedExpertGroups?.forEach(group => {
      group?.reviewGroupSpecialistRelationList?.forEach(relation => {
        expertIds.add(relation?.userId)
      })
    })
    
    await confirmAssignApi({
      reviewIdList: props.selectedTasks?.map(t => t?.reviewId) || [],
      userIdList: Array.from(expertIds)
    })
    ElMessage.success('分配成功')
    dialogs.assign.visible = false
    // 清空专家和专家组选择
    emit('clear-expert-selection')
    emit('clear-group-selection')
    emit('refresh')
    // 刷新专家列表并重置筛选为全部
    emit('refresh-experts')
    emit('reset-expert-filter')
  } finally {
    dialogs.assign.loading = false
  }
}
</script>

<style scoped lang="scss">
.batch-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 12px;
  gap: 16px;
  margin-top: -8px;

  .selected-info-wrapper {
    position: relative;
    flex: 1;
    min-width: 0;

    .selected-info {
      font-size: 14px;
      color: #606266;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      cursor: default;
      font-weight: 600;
      .num {
        color: #409eff;
        font-weight: 600;
      }

      .expert-names {
        color: #333;
        font-weight: 500;
      }
    }

    .selected-info-tooltip {
      position: absolute;
      top: 100%;
      left: 0;
      z-index: 100;
      padding: 8px 12px;
      background: #303133;
      color: #fff;
      font-size: 13px;
      border-radius: 4px;
      white-space: nowrap;
      opacity: 0;
      visibility: hidden;
      transition: opacity 0.2s;
      margin-top: 6px;

      &::before {
        content: '';
        position: absolute;
        top: -4px;
        left: 20px;
        width: 8px;
        height: 8px;
        background: #303133;
        transform: rotate(45deg);
      }
    }

    &:hover .selected-info-tooltip {
      opacity: 1;
      visibility: visible;
    }
  }

  .action-btns {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }
}

.selected-list {
  max-height: 120px;
  overflow-y: auto;
  padding: 4px 8px;
  background: #f5f7fa;
  border-radius: 4px;
  width: 100%;
  .selected-item {
    font-size: 12px;
    color: #606266;
    line-height: 1.6;
  }
}

.empty-tip {
  color: #909399;
  font-size: 13px;
  padding: 12px 0;
}

.upload-tip {
  color: #e6a23c;
  font-size: 12px;
  margin: -6px 0 10px;
}

.select-all-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;

  .selected-count {
    margin-left: auto;
    color: #606266;
    font-size: 13px;
  }
}

:deep(.el-form-item__label) {
  padding-bottom: 2px;
  line-height: 20px !important;
  height: 20px !important;
}

:deep(.el-form-item) {
  margin-bottom: 12px;
}

:deep(.el-form-item__content) {
  line-height: 1.4;
}
</style>
