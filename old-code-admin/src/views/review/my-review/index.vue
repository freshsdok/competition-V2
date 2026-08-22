<template>
  <div class="app-container review-my-review">
    <section v-if="!selectedActivityRound" class="activity-selector">
      <div class="activity-selector__header">
        <div>
          <h2>选择评审活动与轮次</h2>
          <p>请选择要处理的评审轮次，仅展示当前账号已被分配评审任务的活动轮次。</p>
        </div>
        <div class="activity-selector__actions">
          <el-input
            v-model.trim="activityRoundKeyword"
            placeholder="搜索活动名称、编码或轮次"
            clearable
            prefix-icon="Search"
          />
          <el-button icon="Refresh" :loading="activityRoundLoading" @click="getActivityRoundList">刷新</el-button>
        </div>
      </div>

      <div v-loading="activityRoundLoading" class="activity-list-wrap">
        <div v-if="filteredActivityRoundList.length" class="activity-card-grid">
          <article
            v-for="activityRound in filteredActivityRoundList"
            :key="`${activityRound.activityId}-${activityRound.roundId}`"
            class="activity-card"
            tabindex="0"
            role="button"
            :aria-label="`进入${activityRound.activityName || '评审活动'}${activityRound.roundName || '评审轮次'}的任务列表`"
            @click="selectActivityRound(activityRound)"
            @keydown.enter.prevent="selectActivityRound(activityRound)"
            @keydown.space.prevent="selectActivityRound(activityRound)"
          >
            <div class="activity-card__accent" />
            <div class="activity-card__header">
              <div class="activity-card__identity">
                <span class="activity-card__eyebrow">评审活动 · 评审轮次</span>
                <h3>{{ activityRound.activityName || '未命名评审活动' }}</h3>
                <span class="activity-card__code">{{ activityRound.activityCode || `活动 #${activityRound.activityId}` }}</span>
              </div>
              <el-tag :type="activityStatusTag(activityRound.status)" effect="light" round>
                {{ optionLabel(activityStatusOptions, activityRound.status) }}
              </el-tag>
            </div>

            <div class="activity-card__round">
              <div>
                <span>第 {{ activityRound.roundNo || '-' }} 轮</span>
                <strong>{{ activityRound.roundName || '未命名评审轮次' }}</strong>
              </div>
              <div class="activity-card__round-tags">
                <el-tag size="small" effect="plain">{{ optionLabel(roundTypeOptions, activityRound.roundType) }}</el-tag>
                <el-tag size="small" :type="roundStatusTag(activityRound.roundStatus)" effect="plain">
                  {{ optionLabel(roundStatusOptions, activityRound.roundStatus) }}
                </el-tag>
                <el-tag v-if="activityRound.sessionId" size="small" type="success" effect="plain">现场轮询已关联</el-tag>
              </div>
            </div>

            <div class="activity-card__period">
              <span class="activity-card__period-label">本轮时间</span>
              <span>{{ activityRoundReviewPeriod(activityRound) }}</span>
            </div>

            <div class="activity-card__stats">
              <div>
                <strong>{{ activityRound.taskCount || 0 }}</strong>
                <span>任务总数</span>
              </div>
              <div>
                <strong class="is-warning">{{ activityRound.pendingTaskCount || 0 }}</strong>
                <span>待处理</span>
              </div>
              <div>
                <strong class="is-success">{{ activityRound.submittedTaskCount || 0 }}</strong>
                <span>已提交</span>
              </div>
            </div>

            <div class="activity-card__progress">
              <div class="activity-card__progress-meta">
                <span>完成进度</span>
                <span>{{ activityCompletionPercentage(activityRound) }}%</span>
              </div>
              <el-progress
                :percentage="activityCompletionPercentage(activityRound)"
                :show-text="false"
                :stroke-width="6"
              />
            </div>

            <div class="activity-card__footer">
              <span>进入本轮评审任务</span>
              <span aria-hidden="true">→</span>
            </div>
          </article>
        </div>
        <el-empty
          v-else
          :description="activityRoundKeyword ? '没有找到匹配的评审活动轮次' : '暂无已分配的评审活动轮次'"
        />
      </div>
    </section>

    <section v-else class="task-list-page">
      <div class="task-list-page__header">
        <el-page-header title="返回活动轮次" @back="backToActivityRounds">
          <template #content>
            <div class="task-list-page__title">
              <span>{{ selectedActivityRound.activityName || '评审任务' }}</span>
              <small>
                {{ selectedActivityRound.roundName || `轮次 #${selectedActivityRound.roundId}` }}
                · {{ selectedActivityRound.activityCode || `活动 #${selectedActivityRound.activityId}` }}
              </small>
            </div>
          </template>
          <template #extra>
            <el-tag :type="roundStatusTag(selectedActivityRound.roundStatus)" effect="light">
              {{ optionLabel(roundStatusOptions, selectedActivityRound.roundStatus) }}
            </el-tag>
          </template>
        </el-page-header>
      </div>

      <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="96px">
        <el-form-item label="项目名称">
          <el-input v-model.trim="queryParams.objectName" placeholder="请输入项目名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="对象编号">
          <el-input v-model.trim="queryParams.objectCode" placeholder="请输入对象编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="评分状态">
          <el-select v-model="queryParams.assignmentStatus" placeholder="请选择" clearable style="width: 160px">
            <el-option v-for="item in assignmentStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model.trim="queryParams.keywords" placeholder="请输入关键词" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="queryParams.sessionId"
        :title="`现场轮询已开启，每 3 秒同步当前答辩对象并自动置顶${selectedActivityRound.sessionName ? `（${selectedActivityRound.sessionName}）` : ''}`"
        type="success"
        :closable="false"
        show-icon
        class="mb8"
      />

      <el-alert
        v-if="currentObjectWarning"
        :title="currentObjectWarning"
        type="info"
        show-icon
        class="mb8"
      />

      <el-row :gutter="10" class="mb8">
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table
        v-loading="loading"
        :data="displayList"
        stripe
        :row-class-name="reviewRowClass"
      >
        <el-table-column label="当前" width="96" align="center">
          <template #default="{ row }">
            <el-tag v-if="isCurrentObject(row)" type="danger" effect="dark">当前答辩中</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="评审轮次" prop="roundName" min-width="140" show-overflow-tooltip />
        <el-table-column label="评审对象编号" prop="objectCode" min-width="140" show-overflow-tooltip />
        <el-table-column label="项目名称" prop="objectName" min-width="220" show-overflow-tooltip />
        <el-table-column label="所属单位" prop="orgName" min-width="160" show-overflow-tooltip />
        <el-table-column label="学科代码" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ compactCodes(row) }}</template>
        </el-table-column>
        <el-table-column label="分类字段" prop="categoryCodes" min-width="160" show-overflow-tooltip />
        <el-table-column label="关键词" prop="keywords" min-width="160" show-overflow-tooltip />
        <el-table-column label="对象状态" prop="objectStatus" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="objectStatusTag(row.objectStatus)">{{ optionLabel(objectStatusOptions, row.objectStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评分状态" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="assignmentStatusTag(row.assignmentStatus)">{{ optionLabel(assignmentStatusOptions, row.assignmentStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已提交时间" prop="submittedTime" width="170" />
        <el-table-column label="提示" min-width="190" show-overflow-tooltip>
          <template #default="{ row }">{{ row.canReview ? '可评分' : row.cannotReviewReason }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.canReview"
              link
              type="primary"
              icon="Edit"
              @click="openReviewDialog(row, false)"
              v-hasPermi="['competition:review:my-review:query']"
            >
              查看并评分
            </el-button>
            <el-button
              v-else
              link
              type="primary"
              icon="View"
              @click="openReviewDialog(row, true)"
              v-hasPermi="['competition:review:my-review:query']"
            >
              查看
            </el-button>
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
    </section>

    <el-dialog
      v-model="reviewDialogVisible"
      class="review-score-dialog"
      fullscreen
      append-to-body
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header">
          <div>
            <div class="dialog-title">{{ detail.reviewObject?.objectName || '专家评分' }}</div>
            <div class="dialog-subtitle">
              {{ detail.reviewObject?.objectCode || '-' }} · {{ detail.round?.roundName || '-' }}
            </div>
          </div>
          <el-tag v-if="detail.existingRecord?.recordStatus === 'SUBMITTED'" type="success">已提交</el-tag>
          <el-tag v-else-if="detail.canReview" type="warning">可评分</el-tag>
          <el-tag v-else type="info">{{ detail.cannotReviewReason || '只读' }}</el-tag>
        </div>
      </template>

      <el-skeleton v-if="detailLoading" :rows="8" animated />
      <div
        v-else
        ref="workspaceRef"
        class="review-workspace"
        :class="{ 'is-resizing': paneResizing }"
        :style="workspaceResizeStyle"
      >
        <section class="review-left-pane">
          <el-alert
            v-if="detail.cannotReviewReason"
            :title="detail.cannotReviewReason"
            type="warning"
            show-icon
            class="mb12"
          />

          <div class="review-section-header">
            <div>
              <span class="review-section-title">评审对象资料</span>
              <span v-if="objectInfoCollapsed" class="review-section-summary">
                {{ detail.reviewObject?.objectName || '-' }}
              </span>
            </div>
            <el-button
              link
              type="primary"
              :icon="objectInfoCollapsed ? 'ArrowDown' : 'ArrowUp'"
              @click="objectInfoCollapsed = !objectInfoCollapsed"
            >
              {{ objectInfoCollapsed ? '展开' : '收起' }}
            </el-button>
          </div>
          <el-collapse-transition>
            <el-descriptions v-show="!objectInfoCollapsed" :column="2" border>
              <el-descriptions-item label="项目名称" :span="2">{{ detail.reviewObject?.objectName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="项目编号">{{ detail.reviewObject?.objectCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所属单位">{{ detail.reviewObject?.orgName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="学科代码">{{ compactCodes(detail.reviewObject || {}) }}</el-descriptions-item>
              <el-descriptions-item label="关键词">{{ detail.reviewObject?.keywords || '-' }}</el-descriptions-item>
              <el-descriptions-item label="分类字段" :span="2">{{ detail.reviewObject?.categoryCodes || '-' }}</el-descriptions-item>
              <el-descriptions-item label="项目摘要" :span="2">
                <div class="summary-text">{{ detail.reviewObject?.summary || '-' }}</div>
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-transition>

          <div class="review-section-header mt12">
            <div>
              <span class="review-section-title">人员信息</span>
              <span class="review-section-summary">{{ (detail.members || []).length }} 人</span>
            </div>
            <el-button
              link
              type="primary"
              :icon="memberInfoCollapsed ? 'ArrowDown' : 'ArrowUp'"
              @click="memberInfoCollapsed = !memberInfoCollapsed"
            >
              {{ memberInfoCollapsed ? '展开' : '收起' }}
            </el-button>
          </div>
          <el-collapse-transition>
            <el-table v-show="!memberInfoCollapsed" :data="detail.members || []" size="small" stripe>
              <el-table-column label="姓名" prop="memberName" min-width="120" show-overflow-tooltip />
              <el-table-column label="角色" width="110">
                <template #default="{ row }">{{ optionLabel(memberRoleOptions, row.memberRole) }}</template>
              </el-table-column>
              <el-table-column label="所属单位" prop="orgName" min-width="150" show-overflow-tooltip />
              <el-table-column label="证件编号" prop="certificateCode" min-width="150" show-overflow-tooltip />
            </el-table>
          </el-collapse-transition>

          <el-divider content-position="left">附件材料</el-divider>
          <el-empty v-if="!detail.materials || detail.materials.length === 0" description="暂无可查看材料" />
          <el-table
            v-else
            :data="detail.materials"
            size="small"
            stripe
            :row-class-name="materialRowClass"
          >
            <el-table-column label="材料名称" prop="materialName" min-width="160" show-overflow-tooltip />
            <el-table-column label="材料类型" width="100">
              <template #default="{ row }">{{ optionLabel(materialTypeOptions, row.materialType) }}</template>
            </el-table-column>
            <el-table-column label="文件类型" width="90">
              <template #default="{ row }">{{ materialFileType(row) }}</template>
            </el-table-column>
            <el-table-column label="文件名" prop="fileName" min-width="200" show-overflow-tooltip />
            <el-table-column label="大小" width="100">
              <template #default="{ row }">{{ fileSizeText(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column label="上传时间" prop="uploadTime" width="160" />
            <el-table-column label="操作" width="150" align="center" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" icon="View" @click="previewMaterial(row)">预览</el-button>
                <el-button link type="primary" icon="Download" @click="downloadMaterial(row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>

          <ReviewMaterialPreview
            v-if="activeMaterialId"
            class="mt12"
            :file-id="activeMaterialId"
            :file-url="activeMaterial?.fileUrl"
            :file-name="activeMaterial?.fileName || activeMaterial?.materialName"
            :file-type="materialFileType(activeMaterial || {})"
          />
          <el-empty
            v-else-if="detail.materials && detail.materials.length > 0"
            class="material-preview-placeholder"
            description="请选择附件材料进行预览"
            :image-size="80"
          />
        </section>

        <div
          class="review-pane-resizer"
          role="separator"
          aria-orientation="vertical"
          tabindex="0"
          title="拖拽调整材料区和评分区宽度"
          @pointerdown="startPaneResize"
          @keydown.left.prevent="adjustPaneWidth(-40)"
          @keydown.right.prevent="adjustPaneWidth(40)"
        />

        <section class="review-right-pane">
          <div class="score-header">
            <div>
              <div class="score-title">{{ detail.rule?.ruleName || '评分表' }}</div>
              <div class="score-subtitle">
                计分模式：{{ optionLabel(scoreModeOptions, detail.rule?.scoreMode) }}
              </div>
            </div>
            <div class="score-total">
              <span>合计</span>
              <strong>{{ computedTotal }}</strong>
            </div>
          </div>

          <el-empty v-if="!detail.rule" description="当前任务未配置评分规则，不能评分" />
          <el-form v-else ref="scoreFormRef" label-position="top" class="score-form" :disabled="readonlyScore">
            <div
              v-for="criteria in detail.criteriaList"
              :key="criteria.id"
              class="criteria-item"
            >
              <div class="criteria-title">
                <span>{{ criteria.criteriaName }}</span>
                <el-tag v-if="criteria.required === 'Y'" size="small" type="danger">必填</el-tag>
              </div>
              <div v-if="criteria.criteriaDesc" class="criteria-desc">{{ criteria.criteriaDesc }}</div>
              <div class="criteria-meta">
                <span v-if="criteria.scoreType === 'NUMBER'">范围：{{ criteria.minScore ?? '-' }} - {{ criteria.maxScore ?? '-' }}</span>
                <span v-if="criteria.weight">权重：{{ criteria.weight }}</span>
              </div>

              <template v-if="scoreMap[criteria.id]">
                <el-input-number
                  v-if="criteria.scoreType === 'NUMBER'"
                  v-model="scoreMap[criteria.id].scoreValue"
                  :min="criteria.minScore == null ? undefined : Number(criteria.minScore)"
                  :max="criteria.maxScore == null ? undefined : Number(criteria.maxScore)"
                  :precision="2"
                  controls-position="right"
                  style="width: 180px"
                />
                <el-radio-group
                  v-else-if="criteria.scoreType === 'SINGLE_CHOICE'"
                  v-model="scoreMap[criteria.id].optionValue"
                >
                  <el-radio
                    v-for="option in criteriaOptions(criteria)"
                    :key="option.value"
                    :label="option.value"
                  >
                    {{ option.label }}
                    <span v-if="option.score !== undefined && option.score !== null" class="option-score">({{ option.score }}分)</span>
                  </el-radio>
                </el-radio-group>
                <el-input
                  v-else
                  v-model="scoreMap[criteria.id].textValue"
                  type="textarea"
                  :rows="3"
                  maxlength="1000"
                  show-word-limit
                  placeholder="请输入评价内容"
                />
              </template>
            </div>

            <el-form-item label="推荐意见">
              <el-input v-model="scoreForm.recommendation" maxlength="100" placeholder="可填写推荐等级或建议" />
            </el-form-item>
            <el-form-item label="综合意见">
              <el-input v-model="scoreForm.commentText" type="textarea" :rows="5" maxlength="3000" show-word-limit placeholder="请输入综合意见" />
            </el-form-item>
          </el-form>

          <div class="score-actions">
            <el-button icon="Close" @click="reviewDialogVisible = false">关闭</el-button>
            <el-button
              v-if="!readonlyScore"
              type="primary"
              icon="DocumentChecked"
              :loading="draftLoading"
              @click="handleSaveDraft"
              v-hasPermi="['competition:review:my-review:edit']"
            >
              保存草稿
            </el-button>
            <el-button
              v-if="!readonlyScore"
              type="success"
              icon="Check"
              :loading="submitLoading"
              @click="handleSubmitScore"
              v-hasPermi="['competition:review:my-review:submit']"
            >
              提交评分
            </el-button>
          </div>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import RightToolbar from '@/components/RightToolbar'
import ReviewMaterialPreview from './components/ReviewMaterialPreview.vue'
import { listMyReviewActivityRounds, listMyReview, getMyReviewCurrentObject, getMyReviewDetail, saveMyReviewDraft, submitMyReview } from '@/api/review/myReview'
import { isOssFileUrl, normalizeDirectFileUrl } from '@/api/review/materialPreview'
import { ossFileFuc } from '@/hooks/download'

const showSearch = ref(true)
const activityRoundLoading = ref(false)
const loading = ref(false)
const detailLoading = ref(false)
const draftLoading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const activityRoundList = ref([])
const selectedActivityRound = ref(null)
const activityRoundKeyword = ref('')
const taskList = ref([])
const queryRef = ref(null)
const scoreFormRef = ref(null)
const workspaceRef = ref(null)
const currentObjectId = ref(null)
const currentObjectName = ref('')
const pollingTimer = ref(null)
const pollingRequestPending = ref(false)
const reviewDialogVisible = ref(false)
const readonlyByOpenMode = ref(false)
const activeAssignmentId = ref(null)
const activeMaterialId = ref(null)
const activeMaterial = ref(null)
const objectInfoCollapsed = ref(false)
const memberInfoCollapsed = ref(true)
const leftPaneWidth = ref(0)
const paneResizing = ref(false)

const MIN_LEFT_PANE_WIDTH = 360
const MIN_RIGHT_PANE_WIDTH = 420
const RESIZER_WIDTH = 10
const { downloadOssFile } = ossFileFuc()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  activityId: '',
  roundId: '',
  objectName: '',
  objectCode: '',
  assignmentStatus: '',
  keywords: '',
  sessionId: ''
})

const detail = reactive(defaultDetail())
const scoreForm = reactive({
  commentText: '',
  recommendation: ''
})
const scoreMap = reactive({})

const assignmentStatusOptions = [
  { label: '已分配', value: 'ASSIGNED', type: 'info' },
  { label: '评分中', value: 'IN_PROGRESS', type: 'warning' },
  { label: '已提交', value: 'SUBMITTED', type: 'success' },
  { label: '已退回', value: 'RETURNED', type: 'danger' },
  { label: '已锁定', value: 'LOCKED', type: 'primary' },
  { label: '已取消', value: 'CANCELLED', type: 'info' }
]
const activityStatusOptions = [
  { label: '草稿', value: 'DRAFT', type: 'info' },
  { label: '填报中', value: 'SUBMITTING', type: 'success' },
  { label: '填报截止', value: 'SUBMIT_CLOSED', type: 'warning' },
  { label: '评审中', value: 'REVIEWING', type: 'primary' },
  { label: '汇总中', value: 'SUMMARYING', type: 'warning' },
  { label: '已发布', value: 'PUBLISHED', type: 'success' },
  { label: '已归档', value: 'ARCHIVED', type: 'info' },
  { label: '已停用', value: 'DISABLED', type: 'danger' }
]
const roundStatusOptions = [
  { label: '草稿', value: 'DRAFT', type: 'info' },
  { label: '未开始', value: 'NOT_STARTED', type: 'info' },
  { label: '进行中', value: 'IN_PROGRESS', type: 'success' },
  { label: '已结束', value: 'ENDED', type: 'warning' },
  { label: '已归档', value: 'ARCHIVED', type: 'info' },
  { label: '已停用', value: 'DISABLED', type: 'danger' }
]
const roundTypeOptions = [
  { label: '材料评审', value: 'MATERIAL_REVIEW' },
  { label: '现场答辩', value: 'ONSITE_DEFENSE' },
  { label: '资格审核', value: 'QUALIFICATION_CHECK' },
  { label: '专家组评审', value: 'GROUP_REVIEW' },
  { label: '终评确认', value: 'FINAL_CONFIRM' }
]
const objectStatusOptions = [
  { label: '草稿', value: 'DRAFT', type: 'info' },
  { label: '已提交', value: 'SUBMITTED', type: 'success' },
  { label: '申请撤回', value: 'WITHDRAW_REQUESTED', type: 'warning' },
  { label: '撤回通过', value: 'WITHDRAW_APPROVED', type: 'info' },
  { label: '已锁定', value: 'LOCKED', type: 'primary' },
  { label: '已作废', value: 'INVALID', type: 'danger' },
  { label: '评审中', value: 'REVIEWING', type: 'warning' },
  { label: '已评审', value: 'REVIEWED', type: 'success' },
  { label: '已归档', value: 'ARCHIVED', type: 'info' }
]
const memberRoleOptions = [
  { label: '负责人', value: 'LEADER' },
  { label: '成员', value: 'MEMBER' },
  { label: '联系人', value: 'CONTACT' },
  { label: '指导教师', value: 'TEACHER' },
  { label: '其他', value: 'OTHER' }
]
const materialTypeOptions = [
  { label: '申报书', value: 'DECLARATION' },
  { label: 'PPT', value: 'PPT' },
  { label: 'PDF', value: 'PDF' },
  { label: '图片', value: 'IMAGE' },
  { label: '视频', value: 'VIDEO' },
  { label: 'Word文档', value: 'DOC' },
  { label: '压缩包', value: 'ZIP' },
  { label: '其他', value: 'OTHER' }
]
const scoreModeOptions = [
  { label: '求和', value: 'SUM' },
  { label: '加权求和', value: 'WEIGHTED_SUM' },
  { label: '平均分', value: 'AVERAGE' }
]

const filteredActivityRoundList = computed(() => {
  const keyword = activityRoundKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return activityRoundList.value
  }
  return activityRoundList.value.filter(activityRound => {
    return [activityRound.activityName, activityRound.activityCode, activityRound.roundName]
      .some(value => String(value || '').toLowerCase().includes(keyword))
  })
})

const displayList = computed(() => {
  return [...taskList.value].sort((a, b) => {
    if (isCurrentObject(a) && !isCurrentObject(b)) {
      return -1
    }
    if (!isCurrentObject(a) && isCurrentObject(b)) {
      return 1
    }
    return Number(b.assignmentId || 0) - Number(a.assignmentId || 0)
  })
})

const readonlyScore = computed(() => {
  return readonlyByOpenMode.value || !detail.canReview || detail.existingRecord?.recordStatus === 'SUBMITTED'
})

const currentObjectWarning = computed(() => {
  if (!queryParams.sessionId || !currentObjectId.value) {
    return ''
  }
  const exists = taskList.value.some(item => Number(item.objectId) === Number(currentObjectId.value))
  return exists ? '' : `当前现场对象${currentObjectName.value ? `“${currentObjectName.value}”` : ''}不在您的评审任务中。`
})

const computedTotal = computed(() => {
  if (!detail.rule) {
    return '0.00'
  }
  let total = 0
  let count = 0
  for (const criteria of detail.criteriaList || []) {
    const model = scoreMap[criteria.id]
    if (!model) {
      continue
    }
    const value = scoreValueForCriteria(criteria, model)
    if (value === null || value === undefined || value === '') {
      continue
    }
    count += 1
    if (detail.rule.scoreMode === 'WEIGHTED_SUM') {
      total += Number(value) * Number(criteria.weight || 1)
    } else {
      total += Number(value)
    }
  }
  if (detail.rule.scoreMode === 'AVERAGE' && count > 0) {
    total = total / count
  }
  return Number(total || 0).toFixed(2)
})

const workspaceResizeStyle = computed(() => {
  if (!leftPaneWidth.value) {
    return {}
  }
  return {
    '--review-left-pane-width': `${leftPaneWidth.value}px`
  }
})

function defaultDetail() {
  return {
    assignment: null,
    reviewObject: null,
    round: null,
    members: [],
    materials: [],
    rule: null,
    criteriaList: [],
    existingRecord: null,
    existingScoreDetails: [],
    canReview: false,
    cannotReviewReason: ''
  }
}

function getActivityRoundList() {
  activityRoundLoading.value = true
  listMyReviewActivityRounds().then(res => {
    activityRoundList.value = res.data || []
  }).finally(() => {
    activityRoundLoading.value = false
  })
}

function selectActivityRound(activityRound) {
  selectedActivityRound.value = activityRound
  queryParams.activityId = activityRound.activityId
  queryParams.roundId = activityRound.roundId
  queryParams.sessionId = activityRound.roundType === 'ONSITE_DEFENSE'
    ? (activityRound.sessionId || '')
    : ''
  queryParams.pageNum = 1
  getList()
  startCurrentObjectPolling()
}

function backToActivityRounds() {
  stopCurrentObjectPolling()
  selectedActivityRound.value = null
  queryParams.activityId = ''
  queryParams.roundId = ''
  queryParams.sessionId = ''
  taskList.value = []
  total.value = 0
  currentObjectId.value = null
  currentObjectName.value = ''
  getActivityRoundList()
}

function getList() {
  if (!selectedActivityRound.value) {
    return
  }
  loading.value = true
  listMyReview(cleanQuery(queryParams)).then(res => {
    taskList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function cleanQuery(query) {
  const data = { ...query }
  ;['activityId', 'roundId', 'sessionId'].forEach(key => {
    if (data[key] === '') {
      delete data[key]
    }
  })
  return data
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    activityId: selectedActivityRound.value?.activityId || '',
    roundId: selectedActivityRound.value?.roundId || '',
    objectName: '',
    objectCode: '',
    assignmentStatus: '',
    keywords: '',
    sessionId: selectedActivityRound.value?.roundType === 'ONSITE_DEFENSE'
      ? (selectedActivityRound.value?.sessionId || '')
      : ''
  })
  currentObjectId.value = null
  currentObjectName.value = ''
  stopCurrentObjectPolling()
  getList()
  startCurrentObjectPolling()
}

function openReviewDialog(row, readonly) {
  activeAssignmentId.value = row.assignmentId
  activeMaterialId.value = null
  activeMaterial.value = null
  objectInfoCollapsed.value = false
  memberInfoCollapsed.value = true
  readonlyByOpenMode.value = readonly
  reviewDialogVisible.value = true
  loadDetail(row.assignmentId)
}

function loadDetail(assignmentId) {
  detailLoading.value = true
  getMyReviewDetail(assignmentId).then(res => {
    Object.assign(detail, defaultDetail(), res.data || {})
    activeMaterialId.value = null
    activeMaterial.value = null
    initScoreForm()
  }).finally(() => {
    detailLoading.value = false
  })
}

function initScoreForm() {
  Object.keys(scoreMap).forEach(key => delete scoreMap[key])
  scoreForm.commentText = detail.existingRecord?.commentText || ''
  scoreForm.recommendation = detail.existingRecord?.recommendation || ''
  const existingMap = {}
  ;(detail.existingScoreDetails || []).forEach(item => {
    existingMap[item.criteriaId] = item
  })
  ;(detail.criteriaList || []).forEach(criteria => {
    const existing = existingMap[criteria.id] || {}
    scoreMap[criteria.id] = {
      criteriaId: criteria.id,
      scoreValue: existing.scoreValue,
      optionValue: existing.optionValue,
      textValue: existing.textValue
    }
  })
  nextTick(() => scoreFormRef.value?.clearValidate?.())
}

function buildScorePayload() {
  const scoreDetails = (detail.criteriaList || []).map(criteria => {
    const model = scoreMap[criteria.id] || {}
    return {
      criteriaId: criteria.id,
      scoreValue: model.scoreValue,
      optionValue: model.optionValue,
      textValue: model.textValue
    }
  })
  return {
    scoreDetails,
    commentText: scoreForm.commentText,
    recommendation: scoreForm.recommendation
  }
}

function handleSaveDraft() {
  draftLoading.value = true
  saveMyReviewDraft(activeAssignmentId.value, buildScorePayload()).then(() => {
    ElMessage.success('评分草稿已保存')
    loadDetail(activeAssignmentId.value)
    getList()
  }).finally(() => {
    draftLoading.value = false
  })
}

function handleSubmitScore() {
  const missing = findMissingRequired()
  if (missing) {
    ElMessage.warning(`请填写必填评分项：${missing}`)
    return
  }
  ElMessageBox.confirm('确认提交评分？提交后将不能再次编辑。', '提交确认', {
    type: 'warning'
  }).then(() => {
    submitLoading.value = true
    return submitMyReview(activeAssignmentId.value, buildScorePayload())
  }).then(() => {
    ElMessage.success('评分提交成功')
    loadDetail(activeAssignmentId.value)
    getList()
  }).finally(() => {
    submitLoading.value = false
  }).catch(() => {})
}

function findMissingRequired() {
  for (const criteria of detail.criteriaList || []) {
    if (criteria.required !== 'Y') {
      continue
    }
    const model = scoreMap[criteria.id] || {}
    if (criteria.scoreType === 'NUMBER' && (model.scoreValue === undefined || model.scoreValue === null || model.scoreValue === '')) {
      return criteria.criteriaName
    }
    if (criteria.scoreType === 'SINGLE_CHOICE' && !model.optionValue) {
      return criteria.criteriaName
    }
    if (criteria.scoreType === 'TEXT' && !model.textValue) {
      return criteria.criteriaName
    }
  }
  return ''
}

function criteriaOptions(criteria) {
  if (!criteria.optionsJson) {
    return []
  }
  try {
    const parsed = JSON.parse(criteria.optionsJson)
    if (Array.isArray(parsed)) {
      return parsed.map(item => {
        if (typeof item === 'object') {
          return {
            label: item.label || item.name || item.value,
            value: item.value || item.label || item.name,
            score: item.score ?? item.scoreValue
          }
        }
        return { label: item, value: item }
      })
    }
    if (parsed && typeof parsed === 'object') {
      return Object.keys(parsed).map(key => {
        const value = parsed[key]
        if (value && typeof value === 'object') {
          return {
            label: value.label || value.name || key,
            value: value.value || key,
            score: value.score ?? value.scoreValue
          }
        }
        return { label: key, value: key, score: typeof value === 'number' ? value : undefined }
      })
    }
  } catch (e) {
    return []
  }
  return []
}

function scoreValueForCriteria(criteria, model) {
  if (criteria.scoreType === 'NUMBER') {
    return model.scoreValue
  }
  if (criteria.scoreType === 'SINGLE_CHOICE') {
    const option = criteriaOptions(criteria).find(item => item.value === model.optionValue)
    return option ? option.score : null
  }
  return null
}

function startCurrentObjectPolling() {
  stopCurrentObjectPolling()
  if (!queryParams.sessionId) {
    return
  }
  pollCurrentObject()
  pollingTimer.value = window.setInterval(pollCurrentObject, 3000)
}

function stopCurrentObjectPolling() {
  if (pollingTimer.value) {
    window.clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

function pollCurrentObject() {
  if (!queryParams.sessionId || pollingRequestPending.value) {
    return
  }
  pollingRequestPending.value = true
  getMyReviewCurrentObject(queryParams.sessionId).then(res => {
    const nextObjectId = res.data?.objectId || null
    const currentChanged = Number(nextObjectId || 0) !== Number(currentObjectId.value || 0)
    currentObjectId.value = nextObjectId
    currentObjectName.value = res.data?.objectName || ''
    if (currentChanged) {
      queryParams.pageNum = 1
      getList()
    }
  }).catch(() => {
    currentObjectId.value = null
    currentObjectName.value = ''
  }).finally(() => {
    pollingRequestPending.value = false
  })
}

function isCurrentObject(row) {
  return currentObjectId.value && Number(row.objectId) === Number(currentObjectId.value)
}

function reviewRowClass({ row }) {
  return isCurrentObject(row) ? 'current-review-row' : ''
}

function materialRowClass({ row }) {
  return activeMaterialId.value && Number(row.id) === Number(activeMaterialId.value) ? 'active-material-row' : ''
}

function startPaneResize(event) {
  if (event.pointerType === 'mouse' && event.button !== 0) {
    return
  }
  event.preventDefault()
  paneResizing.value = true
  event.currentTarget?.setPointerCapture?.(event.pointerId)
  updatePaneWidth(event.clientX)
  document.addEventListener('pointermove', handlePaneResize)
  document.addEventListener('pointerup', stopPaneResize)
  document.addEventListener('pointercancel', stopPaneResize)
}

function handlePaneResize(event) {
  if (!paneResizing.value) {
    return
  }
  updatePaneWidth(event.clientX)
}

function stopPaneResize() {
  paneResizing.value = false
  document.removeEventListener('pointermove', handlePaneResize)
  document.removeEventListener('pointerup', stopPaneResize)
  document.removeEventListener('pointercancel', stopPaneResize)
}

function adjustPaneWidth(delta) {
  const rect = workspaceRef.value?.getBoundingClientRect()
  if (!rect) {
    return
  }
  const currentWidth = leftPaneWidth.value || Math.round((rect.width - RESIZER_WIDTH) * 0.53)
  leftPaneWidth.value = clampPaneWidth(currentWidth + delta, rect.width)
}

function updatePaneWidth(clientX) {
  const rect = workspaceRef.value?.getBoundingClientRect()
  if (!rect) {
    return
  }
  leftPaneWidth.value = clampPaneWidth(clientX - rect.left, rect.width)
}

function clampPaneWidth(width, workspaceWidth) {
  const maxWidth = Math.max(MIN_LEFT_PANE_WIDTH, workspaceWidth - MIN_RIGHT_PANE_WIDTH - RESIZER_WIDTH)
  return Math.min(Math.max(Math.round(width), MIN_LEFT_PANE_WIDTH), maxWidth)
}

function compactCodes(row) {
  return [row.subjectCode1, row.subjectCode2, row.subjectCode3].filter(Boolean).join(' / ') || '-'
}

function optionLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function assignmentStatusTag(status) {
  const item = assignmentStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

function objectStatusTag(status) {
  const item = objectStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

function activityStatusTag(status) {
  const item = activityStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

function roundStatusTag(status) {
  const item = roundStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

function activityCompletionPercentage(activity) {
  const totalTasks = Number(activity.taskCount || 0)
  if (!totalTasks) {
    return 0
  }
  return Math.min(100, Math.round(Number(activity.submittedTaskCount || 0) * 100 / totalTasks))
}

function activityRoundReviewPeriod(activityRound) {
  const start = formatActivityTime(activityRound.roundStartTime || activityRound.reviewStartTime)
  const end = formatActivityTime(activityRound.roundEndTime || activityRound.reviewEndTime)
  if (start && end) {
    return `${start} 至 ${end}`
  }
  if (start) {
    return `${start} 开始`
  }
  if (end) {
    return `截止 ${end}`
  }
  return '时间待定'
}

function formatActivityTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}

function fileSizeText(size) {
  if (!size) {
    return '-'
  }
  const kb = Number(size) / 1024
  if (kb < 1024) {
    return `${kb.toFixed(1)} KB`
  }
  return `${(kb / 1024).toFixed(1)} MB`
}

function materialFileType(row) {
  const ext = (row.fileExt || fileExtFromName(row.fileName) || fileExtFromName(row.fileUrl) || '').replace(/^\./, '')
  return ext ? ext.toUpperCase() : '-'
}

function fileExtFromName(fileName) {
  if (!fileName) {
    return ''
  }
  const cleanName = String(fileName).split('?')[0].split('#')[0]
  const index = cleanName.lastIndexOf('.')
  return index >= 0 ? cleanName.slice(index + 1) : ''
}

function previewMaterial(row) {
  if (!row.id) {
    ElMessage.warning('材料文件ID为空')
    return
  }
  activeMaterialId.value = row.id
  activeMaterial.value = row
}

function downloadMaterial(row) {
  if (!row.fileUrl) {
    ElMessage.warning('材料文件地址为空')
    return
  }
  if (isOssFileUrl(row.fileUrl)) {
    downloadOssFile(row.fileUrl, row.fileName || row.materialName)
    return
  }
  window.open(normalizeDirectFileUrl(row.fileUrl), '_blank')
}

onMounted(() => {
  getActivityRoundList()
})

onBeforeUnmount(() => {
  stopCurrentObjectPolling()
  stopPaneResize()
})
</script>

<style scoped>
.activity-selector {
  min-height: calc(100vh - 124px);
}

.activity-selector__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding: 8px 4px 24px;
  border-bottom: 1px solid #ebeef5;
}

.activity-selector__header h2 {
  margin: 0;
  font-size: 24px;
  line-height: 1.35;
  color: #1f2d3d;
}

.activity-selector__header p {
  margin: 8px 0 0;
  font-size: 14px;
  color: #7a8494;
}

.activity-selector__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.activity-selector__actions .el-input {
  width: 280px;
}

.activity-list-wrap {
  min-height: 320px;
  padding: 24px 2px;
}

.activity-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.activity-card {
  position: relative;
  min-width: 0;
  padding: 22px 22px 18px;
  overflow: hidden;
  cursor: pointer;
  background: linear-gradient(145deg, #fff 0%, #fff 68%, #f7fbff 100%);
  border: 1px solid #e5eaf1;
  border-radius: 12px;
  box-shadow: 0 6px 22px rgb(31 45 61 / 6%);
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.activity-card:hover,
.activity-card:focus-visible {
  border-color: #8cc5ff;
  outline: none;
  box-shadow: 0 12px 30px rgb(64 158 255 / 16%);
  transform: translateY(-3px);
}

.activity-card__accent {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  height: 4px;
  background: linear-gradient(90deg, #409eff 0%, #67c23a 100%);
}

.activity-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.activity-card__identity {
  min-width: 0;
}

.activity-card__eyebrow {
  display: block;
  margin-bottom: 5px;
  font-size: 11px;
  font-weight: 600;
  color: #409eff;
  letter-spacing: 0.12em;
}

.activity-card__identity h3 {
  display: -webkit-box;
  min-height: 50px;
  margin: 0;
  overflow: hidden;
  font-size: 19px;
  line-height: 1.35;
  color: #25364d;
  overflow-wrap: anywhere;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.activity-card__code {
  display: block;
  margin-top: 7px;
  overflow: hidden;
  font-size: 12px;
  color: #9098a5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-card__round {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-top: 18px;
  padding: 14px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f5faff 100%);
  border: 1px solid #d9ecff;
  border-radius: 9px;
}

.activity-card__round > div:first-child {
  min-width: 0;
}

.activity-card__round > div:first-child span,
.activity-card__round > div:first-child strong {
  display: block;
}

.activity-card__round > div:first-child span {
  margin-bottom: 4px;
  font-size: 11px;
  color: #409eff;
}

.activity-card__round > div:first-child strong {
  overflow: hidden;
  font-size: 15px;
  color: #25364d;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-card__round-tags {
  display: flex;
  flex: none;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 6px;
  max-width: 58%;
}

.activity-card__period {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 20px;
  padding: 11px 13px;
  font-size: 13px;
  color: #526071;
  background: #f6f8fb;
  border-radius: 8px;
}

.activity-card__period-label {
  flex: none;
  font-weight: 600;
  color: #303b4a;
}

.activity-card__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  margin-top: 20px;
}

.activity-card__stats > div {
  min-width: 0;
  text-align: center;
  border-right: 1px solid #ebeef5;
}

.activity-card__stats > div:last-child {
  border-right: 0;
}

.activity-card__stats strong,
.activity-card__stats span {
  display: block;
}

.activity-card__stats strong {
  font-size: 21px;
  line-height: 1.2;
  color: #303b4a;
}

.activity-card__stats strong.is-warning {
  color: #e6a23c;
}

.activity-card__stats strong.is-success {
  color: #67c23a;
}

.activity-card__stats span {
  margin-top: 5px;
  overflow: hidden;
  font-size: 12px;
  color: #9098a5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-card__progress {
  margin-top: 20px;
}

.activity-card__progress-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 7px;
  font-size: 12px;
  color: #7a8494;
}

.activity-card__footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 7px;
  margin-top: 18px;
  padding-top: 15px;
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  border-top: 1px solid #edf1f5;
}

.task-list-page__header {
  padding: 4px 4px 18px;
  margin-bottom: 18px;
  border-bottom: 1px solid #ebeef5;
}

.task-list-page__title {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.task-list-page__title > span {
  overflow: hidden;
  font-size: 18px;
  font-weight: 600;
  color: #25364d;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-list-page__title small {
  margin-top: 3px;
  font-size: 12px;
  font-weight: 400;
  color: #9098a5;
}

.review-my-review :deep(.current-review-row) {
  background: #fff3e8;
}

.review-my-review :deep(.current-review-row td) {
  background: #fff3e8 !important;
}

.review-my-review :deep(.active-material-row td) {
  background: #ecf5ff !important;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.dialog-subtitle,
.score-subtitle,
.criteria-desc,
.criteria-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #6b778c;
}

.review-workspace {
  position: relative;
  display: grid;
  grid-template-columns: minmax(360px, var(--review-left-pane-width, 1fr)) 10px minmax(420px, 0.9fr);
  gap: 0;
  height: calc(100vh - 104px);
  overflow: hidden;
}

.review-workspace.is-resizing {
  cursor: col-resize;
  user-select: none;
}

.review-workspace.is-resizing::after {
  position: absolute;
  inset: 0;
  z-index: 2;
  content: '';
  cursor: col-resize;
}

.review-left-pane,
.review-right-pane {
  min-width: 0;
  overflow: auto;
}

.review-left-pane {
  padding: 4px 8px 16px;
}

.review-right-pane {
  padding: 4px 8px 16px 14px;
}

.review-pane-resizer {
  position: relative;
  z-index: 3;
  align-self: stretch;
  cursor: col-resize;
  outline: none;
}

.review-pane-resizer::before {
  position: absolute;
  top: 4px;
  bottom: 16px;
  left: 4px;
  width: 2px;
  content: '';
  background: #dcdfe6;
  border-radius: 2px;
  transition: background-color 0.2s, width 0.2s;
}

.review-pane-resizer:hover::before,
.review-pane-resizer:focus-visible::before,
.review-workspace.is-resizing .review-pane-resizer::before {
  left: 3px;
  width: 4px;
  background: #409eff;
}

.summary-text {
  white-space: pre-wrap;
  line-height: 1.7;
}

.review-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 34px;
  padding: 2px 0 8px;
}

.review-section-header > div {
  min-width: 0;
}

.review-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.review-section-summary {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
  vertical-align: 1px;
}

.score-header {
  position: sticky;
  top: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 0 12px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.score-title {
  font-size: 17px;
  font-weight: 600;
  color: #1f2d3d;
}

.score-total {
  min-width: 120px;
  text-align: right;
}

.score-total span {
  display: block;
  font-size: 12px;
  color: #6b778c;
}

.score-total strong {
  font-size: 24px;
  color: #409eff;
}

.score-form {
  padding: 12px 0 84px;
}

.criteria-item {
  padding: 14px 0;
  border-bottom: 1px solid #ebeef5;
}

.criteria-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-weight: 600;
  color: #303133;
}

.criteria-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 10px;
}

.option-score {
  color: #909399;
}

.score-actions {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 0 0;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

.mb8 {
  margin-bottom: 8px;
}

.mb12 {
  margin-bottom: 12px;
}

.mt12 {
  margin-top: 12px;
}

.material-preview-placeholder {
  margin-top: 12px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
}

@media (max-width: 1100px) {
  .activity-selector__header {
    flex-direction: column;
  }

  .activity-selector__actions {
    width: 100%;
  }

  .activity-selector__actions .el-input {
    flex: 1;
    width: auto;
  }

  .review-workspace {
    grid-template-columns: 1fr;
    height: auto;
    overflow: visible;
  }

  .review-pane-resizer {
    display: none;
  }

  .review-left-pane,
  .review-right-pane {
    overflow: visible;
    padding: 4px 8px 16px;
  }
}

@media (max-width: 600px) {
  .activity-card-grid {
    grid-template-columns: 1fr;
  }

  .activity-selector__actions {
    align-items: stretch;
    flex-direction: column;
  }

  .activity-card {
    padding: 20px 16px 16px;
  }

  .activity-card__round {
    align-items: flex-start;
    flex-direction: column;
  }

  .activity-card__round-tags {
    justify-content: flex-start;
    max-width: none;
  }

  .activity-card__stats strong {
    font-size: 18px;
  }
}
</style>
