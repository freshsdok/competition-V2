<template>
  <div class="app-container">
    <el-page-header @back="goBack">
      <template #content>
        <span>{{ detail.objectName || '评审对象详情' }}</span>
      </template>
      <template #extra>
        <el-button icon="Refresh" @click="loadDetail">刷新</el-button>
        <el-button
          v-if="detail.submitStatus === 'WITHDRAW_REQUESTED'"
          type="success"
          icon="Check"
          @click="handleApprove"
          v-hasPermi="['competition:review:submission:approve']"
        >
          通过撤回
        </el-button>
        <el-button
          v-if="detail.submitStatus === 'WITHDRAW_REQUESTED'"
          type="danger"
          icon="Close"
          @click="handleReject"
          v-hasPermi="['competition:review:submission:approve']"
        >
          驳回撤回
        </el-button>
        <el-button
          type="warning"
          icon="Connection"
          :disabled="!canResync"
          :loading="syncLoading"
          @click="handleResyncCertificate"
          v-hasPermi="['competition:review:object:import']"
        >
          重新同步参赛证
        </el-button>
      </template>
    </el-page-header>

    <el-tabs v-model="activeTab" class="detail-tabs">
      <el-tab-pane label="基本信息" name="basic">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="评审对象编号">{{ detail.objectCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ detail.objectName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="评审活动">{{ activity.activityName || detail.activityId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="对象类型">{{ detail.objectType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属单位">{{ detail.orgName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detail.contactName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ detail.contactPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ detail.contactEmail || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学科代码1">{{ detail.subjectCode1 || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学科代码2">{{ detail.subjectCode2 || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学科代码3">{{ detail.subjectCode3 || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提交状态">
            <el-tag :type="statusTag(detail.submitStatus)">{{ optionLabel(objectStatusOptions, detail.submitStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建来源">{{ detail.createdFrom || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源模块">{{ detail.sourceModule || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源业务类型">{{ detail.sourceBizType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源业务ID">{{ detail.sourceBizId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源团队ID">{{ detail.sourceTeamId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源报名ID">{{ detail.sourceRegistrationId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="分类字段" :span="2">{{ detail.categoryCodes || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关键词" :span="2">{{ detail.keywords || '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目摘要" :span="2">{{ detail.summary || '-' }}</el-descriptions-item>
          <el-descriptions-item label="扩展数据" :span="2">
            <el-input :model-value="detail.extraData || '-'" type="textarea" :rows="4" readonly />
          </el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>

      <el-tab-pane label="成员信息" name="members">
        <el-table v-loading="loading.members" :data="members" stripe>
          <el-table-column label="姓名" prop="memberName" min-width="120" show-overflow-tooltip />
          <el-table-column label="角色" width="110">
            <template #default="{ row }">{{ optionLabel(memberRoleOptions, row.memberRole) }}</template>
          </el-table-column>
          <el-table-column label="是否负责人" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isPrimary === 'Y' ? 'success' : 'info'">{{ row.isPrimary === 'Y' ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="手机号" prop="phone" min-width="130" show-overflow-tooltip />
          <el-table-column label="邮箱" prop="email" min-width="160" show-overflow-tooltip />
          <el-table-column label="所属单位" prop="orgName" min-width="160" show-overflow-tooltip />
          <el-table-column label="证件编号" prop="certificateCode" min-width="160" show-overflow-tooltip />
          <el-table-column label="证件类型" prop="certificateType" width="120" />
          <el-table-column label="来源业务ID" prop="sourceBizId" min-width="130" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="填报权限" name="permissions">
        <el-table v-loading="loading.permissions" :data="permissions" stripe>
          <el-table-column label="用户ID" prop="userId" min-width="110" />
          <el-table-column label="权限类型" prop="permissionType" min-width="130" />
          <el-table-column label="权限状态" prop="status" min-width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="授权来源" prop="sourceModule" min-width="140" />
          <el-table-column label="来源业务ID" prop="sourceBizId" min-width="140" show-overflow-tooltip />
          <el-table-column label="授权时间" prop="grantedTime" min-width="170" />
          <el-table-column label="使用时间" prop="usedTime" min-width="170" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="参赛证映射" name="certificates">
        <el-table v-loading="loading.certificates" :data="certificates" stripe>
          <el-table-column label="证件编号" prop="certificateCode" min-width="180" show-overflow-tooltip />
          <el-table-column label="证件类型" prop="certificateType" width="120" />
          <el-table-column label="持证人姓名" prop="memberName" min-width="130" show-overflow-tooltip />
          <el-table-column label="成员角色" prop="memberRole" width="120">
            <template #default="{ row }">{{ optionLabel(memberRoleOptions, row.memberRole) }}</template>
          </el-table-column>
          <el-table-column label="有效状态" prop="validStatus" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.validStatus === 'VALID' ? 'success' : 'danger'">{{ row.validStatus || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="来源团队ID" prop="sourceTeamId" min-width="130" show-overflow-tooltip />
          <el-table-column label="来源报名ID" prop="sourceRegistrationId" min-width="130" show-overflow-tooltip />
          <el-table-column label="来源业务ID" prop="sourceBizId" min-width="130" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="外部业务关联" name="externalRefs">
        <el-table v-loading="loading.externalRefs" :data="externalRefs" stripe>
          <el-table-column label="来源模块" prop="sourceModule" min-width="120" />
          <el-table-column label="来源业务类型" prop="sourceBizType" min-width="130" />
          <el-table-column label="来源业务ID" prop="sourceBizId" min-width="140" show-overflow-tooltip />
          <el-table-column label="来源业务编码" prop="sourceBizCode" min-width="140" show-overflow-tooltip />
          <el-table-column label="来源团队ID" prop="sourceTeamId" min-width="140" show-overflow-tooltip />
          <el-table-column label="来源报名ID" prop="sourceRegistrationId" min-width="140" show-overflow-tooltip />
          <el-table-column label="关联类型" prop="relationType" min-width="120" />
          <el-table-column label="扩展数据" prop="extraData" min-width="240" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewActivity } from '@/api/review/activity'
import {
  getReviewObject,
  importFromBusiness,
  listObjectCertificates,
  listObjectExternalRefs,
  listObjectMembers,
  listObjectPermissions
} from '@/api/review/object'
import { approveWithdraw, rejectWithdraw } from '@/api/review/submission'

const route = useRoute()
const router = useRouter()
const objectId = computed(() => Number(route.params.id))
const activeTab = ref('basic')
const syncLoading = ref(false)
const detail = reactive({})
const activity = reactive({})
const members = ref([])
const permissions = ref([])
const certificates = ref([])
const externalRefs = ref([])
const loading = reactive({
  members: false,
  permissions: false,
  certificates: false,
  externalRefs: false
})

const objectStatusOptions = [
  { label: '草稿', value: 'DRAFT', type: 'info' },
  { label: '已提交', value: 'SUBMITTED', type: 'success' },
  { label: '申请撤回', value: 'WITHDRAW_REQUESTED', type: 'warning' },
  { label: '撤回通过', value: 'WITHDRAW_APPROVED', type: 'info' },
  { label: '撤回驳回', value: 'WITHDRAW_REJECTED', type: 'danger' },
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

const canResync = computed(() => detail.activityId && detail.sourceModule === 'competition' && detail.sourceBizType && detail.sourceBizId)

function loadDetail() {
  getReviewObject(objectId.value).then(res => {
    Object.keys(detail).forEach(key => delete detail[key])
    Object.assign(detail, res.data || {})
    if (detail.activityId) {
      getReviewActivity(detail.activityId).then(activityRes => {
        Object.keys(activity).forEach(key => delete activity[key])
        Object.assign(activity, activityRes.data || {})
      })
    }
  })
  loadMembers()
  loadPermissions()
  loadCertificates()
  loadExternalRefs()
}

function loadMembers() {
  loading.members = true
  listObjectMembers(objectId.value).then(res => {
    members.value = res.data || []
  }).finally(() => {
    loading.members = false
  })
}

function loadPermissions() {
  loading.permissions = true
  listObjectPermissions(objectId.value).then(res => {
    permissions.value = res.data || []
  }).finally(() => {
    loading.permissions = false
  })
}

function loadCertificates() {
  loading.certificates = true
  listObjectCertificates(objectId.value).then(res => {
    certificates.value = res.data || []
  }).finally(() => {
    loading.certificates = false
  })
}

function loadExternalRefs() {
  loading.externalRefs = true
  listObjectExternalRefs(objectId.value).then(res => {
    externalRefs.value = res.data || []
  }).finally(() => {
    loading.externalRefs = false
  })
}

function handleResyncCertificate() {
  if (!canResync.value) return
  ElMessageBox.confirm('确认重新同步该对象的参赛证映射？旧有效映射会标记为 INVALID。', '同步确认', {
    type: 'warning'
  }).then(() => {
    syncLoading.value = true
    importFromBusiness({
      activityId: detail.activityId,
      sourceModule: detail.sourceModule,
      sourceBizType: detail.sourceBizType,
      sourceBizIds: [detail.sourceBizId],
      defaultObjectType: detail.objectType || 'PROJECT',
      permissionUserMode: 'LEADER',
      overwriteExisting: true,
      syncCertificate: true
    }).then(() => {
      ElMessage.success('同步完成')
      loadDetail()
    }).finally(() => {
      syncLoading.value = false
    })
  }).catch(() => {})
}

function handleApprove() {
  ElMessageBox.prompt('请输入审批意见', '通过撤回申请', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(({ value }) => {
    return approveWithdraw(objectId.value, { actionReason: value })
  }).then(() => {
    ElMessage.success('已通过撤回申请')
    loadDetail()
  }).catch(() => {})
}

function handleReject() {
  ElMessageBox.prompt('请输入驳回原因', '驳回撤回申请', {
    confirmButtonText: '驳回',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(({ value }) => {
    return rejectWithdraw(objectId.value, { actionReason: value })
  }).then(() => {
    ElMessage.success('已驳回撤回申请')
    loadDetail()
  }).catch(() => {})
}

function goBack() {
  router.back()
}

function optionLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function statusTag(status) {
  const item = objectStatusOptions.find(option => option.value === status)
  return item?.type || 'info'
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-tabs {
  margin-top: 16px;
}
</style>
