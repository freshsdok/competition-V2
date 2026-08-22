<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="1180px"
    append-to-body
    destroy-on-close
    class="task-recipient-dialog"
    @closed="handleClosed"
  >
    <div class="task-overview">
      <span class="task-overview__label">任务状态</span>
      <el-tag :type="taskStatusType">{{ taskStatusLabel }}</el-tag>
      <span class="task-overview__label">任务编号</span>
      <span>{{ currentTask.id || "-" }}</span>
      <span class="task-overview__label">用户组</span>
      <span class="task-overview__groups">{{ currentTask.userGroupNames || "-" }}</span>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="上传人员" name="recipients">
        <div class="summary-grid">
          <div class="summary-card">
            <span class="summary-card__label">应上传人数</span>
            <strong>{{ recipientSummary.totalCount }}</strong>
          </div>
          <div class="summary-card summary-card--success">
            <span class="summary-card__label">已上传</span>
            <strong>{{ recipientSummary.uploadedCount }}</strong>
          </div>
          <div class="summary-card summary-card--warning">
            <span class="summary-card__label">未上传</span>
            <strong>{{ recipientSummary.notUploadedCount }}</strong>
          </div>
        </div>

        <el-alert
          v-if="!canSend"
          title="当前任务不是已发布状态，可查看人员和历史通知，但不能发送新通知。"
          type="info"
          :closable="false"
          show-icon
          class="send-state-alert"
        />

        <div class="recipient-toolbar">
          <el-form :model="recipientQuery" :inline="true" class="recipient-query">
            <el-form-item label="人员">
              <el-input
                v-model.trim="recipientQuery.keyword"
                clearable
                placeholder="姓名、账号、手机号或学校"
                style="width: 250px"
                @keyup.enter="handleRecipientQuery"
                @clear="handleRecipientQuery"
              />
            </el-form-item>
            <el-form-item label="上传状态">
              <el-select v-model="recipientQuery.uploadStatus" style="width: 140px" @change="handleRecipientQuery">
                <el-option label="全部" value="ALL" />
                <el-option label="已上传" value="UPLOADED" />
                <el-option label="未上传" value="NOT_UPLOADED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleRecipientQuery">查询</el-button>
              <el-button icon="Refresh" @click="resetRecipientQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="batch-actions">
            <el-button
              type="primary"
              plain
              :disabled="!canSend || recipientSummary.totalCount === 0"
              v-hasPermi="['system:fileDistributeTask:notify']"
              @click="openSendDialog('ALL')"
            >通知全体</el-button>
            <el-button
              type="success"
              plain
              :disabled="!canSend || recipientSummary.uploadedCount === 0"
              v-hasPermi="['system:fileDistributeTask:notify']"
              @click="openSendDialog('UPLOADED')"
            >通知已上传</el-button>
            <el-button
              type="warning"
              plain
              :disabled="!canSend || recipientSummary.notUploadedCount === 0"
              v-hasPermi="['system:fileDistributeTask:notify']"
              @click="openSendDialog('NOT_UPLOADED')"
            >通知未上传</el-button>
          </div>
        </div>

        <el-table v-loading="recipientLoading" :data="recipientList" stripe>
          <el-table-column label="姓名" min-width="110" show-overflow-tooltip>
            <template #default="{ row }">{{ recipientName(row) }}</template>
          </el-table-column>
          <el-table-column label="账号" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">{{ recipientAccount(row) }}</template>
          </el-table-column>
          <el-table-column label="手机号" min-width="125" show-overflow-tooltip>
            <template #default="{ row }">{{ recipientPhone(row) }}</template>
          </el-table-column>
          <el-table-column label="学校" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">{{ recipientSchool(row) }}</template>
          </el-table-column>
          <el-table-column label="所属用户组" min-width="170" show-overflow-tooltip>
            <template #default="{ row }">{{ recipientGroups(row) }}</template>
          </el-table-column>
          <el-table-column label="上传状态" width="95" align="center">
            <template #default="{ row }">
              <el-tag :type="isUploaded(row) ? 'success' : 'info'">
                {{ isUploaded(row) ? "已上传" : "未上传" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="最近上传时间" prop="uploadTime" width="170">
            <template #default="{ row }">{{ row.uploadTime || "-" }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                :disabled="!canSend"
                v-hasPermi="['system:fileDistributeTask:notify']"
                @click="openSendDialog('SINGLE', row)"
              >发送通知</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="recipientTotal > 0"
          :total="recipientTotal"
          v-model:page="recipientQuery.pageNum"
          v-model:limit="recipientQuery.pageSize"
          @pagination="loadRecipients"
        />
      </el-tab-pane>

      <el-tab-pane label="通知记录" name="notifications">
        <div class="history-toolbar">
          <el-button icon="Refresh" @click="loadNotifications">刷新</el-button>
        </div>
        <el-table v-loading="notificationLoading" :data="notificationList" stripe>
          <el-table-column label="标题" prop="title" min-width="220" show-overflow-tooltip />
          <el-table-column label="发送范围" width="120" align="center">
            <template #default="{ row }">{{ targetTypeLabel(row.targetType) }}</template>
          </el-table-column>
          <el-table-column label="实际人数" prop="recipientCount" width="90" align="center" />
          <el-table-column label="发送人" min-width="100" show-overflow-tooltip>
            <template #default="{ row }">{{ senderName(row) }}</template>
          </el-table-column>
          <el-table-column label="发送时间" prop="sendTime" width="170">
            <template #default="{ row }">{{ row.sendTime || "-" }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'WITHDRAWN' ? 'info' : 'success'">
                {{ row.status === "WITHDRAWN" ? "已撤回" : "有效" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="145" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetailDialog(row)">查看</el-button>
              <el-button
                v-if="row.status !== 'WITHDRAWN'"
                link
                type="warning"
                v-hasPermi="['system:fileDistributeTask:notify']"
                @click="handleWithdraw(row)"
              >撤回</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="notificationTotal > 0"
          :total="notificationTotal"
          v-model:page="notificationQuery.pageNum"
          v-model:limit="notificationQuery.pageSize"
          @pagination="loadNotifications"
        />
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="visible = false">关 闭</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="sendVisible"
    title="发送任务通知"
    width="820px"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
  >
    <el-alert
      :title="sendTargetDescription"
      type="info"
      :closable="false"
      show-icon
      class="send-target-alert"
    />
    <el-form ref="sendFormRef" :model="sendForm" :rules="sendRules" label-width="88px">
      <el-form-item label="标题" prop="title">
        <el-input
          v-model.trim="sendForm.title"
          maxlength="255"
          show-word-limit
          placeholder="请输入通知标题"
        />
      </el-form-item>
      <el-form-item label="通知内容" prop="content">
        <Editor v-model="sendForm.content" :min-height="280" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="sendVisible = false">取 消</el-button>
      <el-button type="primary" :loading="sendSubmitting" @click="submitNotification">发 送</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="detailVisible"
    title="通知详情"
    width="760px"
    append-to-body
    destroy-on-close
  >
    <div v-loading="detailLoading" class="notification-detail">
      <template v-if="!detailLoading">
        <h3>{{ notificationDetail.title || "-" }}</h3>
        <div class="notification-detail__meta">
          <span>发送范围：{{ targetTypeLabel(notificationDetail.targetType) }}</span>
          <span>实际人数：{{ notificationDetail.recipientCount ?? "-" }}</span>
          <span>发送时间：{{ notificationDetail.sendTime || "-" }}</span>
          <el-tag :type="notificationDetail.status === 'WITHDRAWN' ? 'info' : 'success'" size="small">
            {{ notificationDetail.status === "WITHDRAWN" ? "已撤回" : "有效" }}
          </el-tag>
        </div>
        <div class="notification-detail__content" v-html="notificationDetail.content || ''"></div>
      </template>
    </div>
    <template #footer>
      <el-button @click="detailVisible = false">关 闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import Editor from "@/components/Editor";
import Pagination from "@/components/Pagination";
import {
  getFileTaskNotification,
  getFileTaskNotifications,
  getFileTaskRecipients,
  sendFileTaskNotification,
  withdrawFileTaskNotification,
} from "@/api/fileTask/task";

const visible = ref(false);
const activeTab = ref("recipients");
const currentTask = reactive({});

const recipientLoading = ref(false);
const recipientList = ref([]);
const recipientTotal = ref(0);
const recipientSummary = reactive({
  totalCount: 0,
  uploadedCount: 0,
  notUploadedCount: 0,
});
const recipientQuery = reactive(defaultRecipientQuery());

const notificationLoading = ref(false);
const notificationList = ref([]);
const notificationTotal = ref(0);
const notificationQuery = reactive({
  pageNum: 1,
  pageSize: 10,
});

const sendVisible = ref(false);
const sendSubmitting = ref(false);
const sendFormRef = ref(null);
const sendForm = reactive(defaultSendForm());
const selectedRecipient = ref(null);

const detailVisible = ref(false);
const detailLoading = ref(false);
const notificationDetail = reactive({});

const dialogTitle = computed(() => `上传人员与通知${currentTask.taskName ? ` - ${currentTask.taskName}` : ""}`);
const canSend = computed(() => String(currentTask.taskStatus) === "2");
const taskStatusLabel = computed(() => {
  const labels = { "1": "草稿", "2": "已发布", "3": "已撤回" };
  return labels[String(currentTask.taskStatus)] || "-";
});
const taskStatusType = computed(() => {
  const types = { "1": "info", "2": "success", "3": "danger" };
  return types[String(currentTask.taskStatus)] || "info";
});
const sendTargetDescription = computed(() => {
  if (sendForm.targetType === "SINGLE") {
    const name = selectedRecipient.value ? recipientName(selectedRecipient.value) : "-";
    const account = selectedRecipient.value ? recipientAccount(selectedRecipient.value) : "-";
    return `单人发送：${name}（${account}）`;
  }
  const descriptions = {
    ALL: `发送给任务当前全部 ${recipientSummary.totalCount} 名应上传人员`,
    UPLOADED: `发送给任务当前 ${recipientSummary.uploadedCount} 名已上传人员`,
    NOT_UPLOADED: `发送给任务当前 ${recipientSummary.notUploadedCount} 名未上传人员`,
  };
  return `${descriptions[sendForm.targetType] || "发送任务通知"}；提交时服务端会重新计算实际收件范围。`;
});

const sendRules = {
  title: [
    { required: true, message: "请输入通知标题", trigger: "blur" },
    { max: 255, message: "标题不能超过 255 个字符", trigger: "blur" },
  ],
  content: [{
    validator: (_rule, value, callback) => {
      if (!hasVisualContent(value)) {
        callback(new Error("请输入通知内容"));
        return;
      }
      callback();
    },
    trigger: "change",
  }],
};

function defaultRecipientQuery() {
  return {
    pageNum: 1,
    pageSize: 10,
    keyword: "",
    uploadStatus: "ALL",
  };
}

function defaultSendForm() {
  return {
    targetType: "ALL",
    targetUserId: undefined,
    title: "",
    content: "",
  };
}

function assignReactive(target, source) {
  Object.keys(target).forEach((key) => delete target[key]);
  Object.assign(target, source);
}

function openDialog(task) {
  assignReactive(currentTask, { ...(task || {}) });
  assignReactive(recipientQuery, defaultRecipientQuery());
  Object.assign(recipientSummary, {
    totalCount: 0,
    uploadedCount: 0,
    notUploadedCount: 0,
  });
  recipientList.value = [];
  recipientTotal.value = 0;
  notificationList.value = [];
  notificationTotal.value = 0;
  notificationQuery.pageNum = 1;
  activeTab.value = "recipients";
  visible.value = true;
  loadRecipients();
}

function handleClosed() {
  sendVisible.value = false;
  detailVisible.value = false;
  selectedRecipient.value = null;
  assignReactive(currentTask, {});
}

async function loadRecipients() {
  if (!currentTask.id) return;
  recipientLoading.value = true;
  try {
    const response = await getFileTaskRecipients(currentTask.id, { ...recipientQuery });
    const data = response.data || {};
    recipientList.value = Array.isArray(data.rows) ? data.rows : [];
    recipientTotal.value = Number(data.total) || 0;
    recipientSummary.totalCount = Number(data.totalCount) || 0;
    recipientSummary.uploadedCount = Number(data.uploadedCount) || 0;
    recipientSummary.notUploadedCount = Number(data.notUploadedCount) || 0;
  } finally {
    recipientLoading.value = false;
  }
}

function handleRecipientQuery() {
  recipientQuery.pageNum = 1;
  loadRecipients();
}

function resetRecipientQuery() {
  assignReactive(recipientQuery, defaultRecipientQuery());
  loadRecipients();
}

async function handleTabChange(name) {
  if (name === "notifications") {
    notificationQuery.pageNum = 1;
    await loadNotifications();
  }
}

async function loadNotifications() {
  if (!currentTask.id) return;
  notificationLoading.value = true;
  try {
    const response = await getFileTaskNotifications(currentTask.id, { ...notificationQuery });
    notificationList.value = Array.isArray(response.rows) ? response.rows : [];
    notificationTotal.value = Number(response.total) || 0;
  } finally {
    notificationLoading.value = false;
  }
}

function openSendDialog(targetType, recipient) {
  if (!canSend.value) {
    ElMessage.warning("仅已发布任务可以发送通知");
    return;
  }
  selectedRecipient.value = recipient || null;
  assignReactive(sendForm, {
    ...defaultSendForm(),
    targetType,
    targetUserId: targetType === "SINGLE" ? recipient?.userId : undefined,
  });
  sendVisible.value = true;
  nextTick(() => sendFormRef.value?.clearValidate());
}

async function submitNotification() {
  if (!sendFormRef.value) return;
  const valid = await sendFormRef.value.validate().catch(() => false);
  if (!valid) return;
  sendSubmitting.value = true;
  try {
    const response = await sendFileTaskNotification(currentTask.id, {
      targetType: sendForm.targetType,
      targetUserId: sendForm.targetUserId,
      title: sendForm.title,
      content: sendForm.content,
    });
    const recipientCount = Number(response.data?.recipientCount ?? response.recipientCount) || 0;
    ElMessage.success(`发送成功，实际通知 ${recipientCount} 人`);
    sendVisible.value = false;
    if (activeTab.value === "notifications") {
      notificationQuery.pageNum = 1;
      await loadNotifications();
    }
  } finally {
    sendSubmitting.value = false;
  }
}

async function openDetailDialog(row) {
  detailVisible.value = true;
  detailLoading.value = true;
  assignReactive(notificationDetail, {});
  try {
    const response = await getFileTaskNotification(currentTask.id, row.id || row.notificationId);
    assignReactive(notificationDetail, response.data || {});
  } finally {
    detailLoading.value = false;
  }
}

async function handleWithdraw(row) {
  try {
    await ElMessageBox.confirm(
      `撤回后客户端将不再展示通知“${row.title}”，发送历史仍会保留。是否继续？`,
      "撤回通知",
      {
        confirmButtonText: "撤 回",
        cancelButtonText: "取 消",
        type: "warning",
      }
    );
  } catch {
    return;
  }
  await withdrawFileTaskNotification(currentTask.id, row.id || row.notificationId);
  ElMessage.success("通知已撤回");
  await loadNotifications();
}

function recipientName(row) {
  return row.displayName || row.realName || row.nickName || row.name || "-";
}

function recipientAccount(row) {
  return row.userName || row.account || "-";
}

function recipientPhone(row) {
  return row.phoneNumber || row.phonenumber || row.phone || "-";
}

function recipientSchool(row) {
  return row.schoolName || row.school || "-";
}

function recipientGroups(row) {
  const value = row.userGroupNames ?? row.groupNames;
  if (Array.isArray(value)) return value.join("、") || "-";
  return value || "-";
}

function isUploaded(row) {
  return row.uploaded === true || row.uploaded === 1 || row.uploaded === "1";
}

function targetTypeLabel(value) {
  const labels = {
    SINGLE: "单人",
    ALL: "全体",
    UPLOADED: "已上传",
    NOT_UPLOADED: "未上传",
  };
  return labels[value] || value || "-";
}

function senderName(row) {
  return row.senderName || row.senderUserName || row.createBy || "-";
}

function hasVisualContent(html) {
  if (!html || typeof html !== "string") return false;
  const container = document.createElement("div");
  container.innerHTML = html;
  return Boolean(container.textContent?.trim() || container.querySelector("img"));
}

defineExpose({
  openDialog,
});
</script>

<style scoped lang="scss">
.task-overview {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  margin: -4px 0 10px;
  padding: 0 14px;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border-radius: 4px;
}

.task-overview__label {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
}

.task-overview__label:first-child {
  margin-left: 0;
}

.task-overview__groups {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(140px, 220px));
  gap: 14px;
  margin-bottom: 14px;
}

.summary-card {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 14px 18px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-7);
  border-radius: 6px;
}

.summary-card--success {
  color: var(--el-color-success);
  background: var(--el-color-success-light-9);
  border-color: var(--el-color-success-light-7);
}

.summary-card--warning {
  color: var(--el-color-warning);
  background: var(--el-color-warning-light-9);
  border-color: var(--el-color-warning-light-7);
}

.summary-card__label {
  color: var(--el-text-color-regular);
}

.summary-card strong {
  font-size: 24px;
}

.send-state-alert,
.send-target-alert {
  margin-bottom: 14px;
}

.recipient-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.recipient-query {
  flex: 1;
}

.batch-actions {
  display: flex;
  flex-shrink: 0;
  gap: 8px;
}

.batch-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.history-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.notification-detail {
  min-height: 220px;
}

.notification-detail h3 {
  margin: 0 0 12px;
  font-size: 20px;
  line-height: 1.5;
}

.notification-detail__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 20px;
  padding-bottom: 14px;
  color: var(--el-text-color-secondary);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.notification-detail__content {
  max-height: 52vh;
  padding: 18px 4px;
  overflow-y: auto;
  line-height: 1.75;
  overflow-wrap: anywhere;
}

.notification-detail__content :deep(img) {
  max-width: 100%;
  height: auto;
}

.notification-detail__content :deep(table) {
  max-width: 100%;
  border-collapse: collapse;
}

.notification-detail__content :deep(td),
.notification-detail__content :deep(th) {
  padding: 6px 8px;
  border: 1px solid var(--el-border-color);
}

@media (max-width: 1100px) {
  .recipient-toolbar {
    display: block;
  }

  .batch-actions {
    margin-bottom: 14px;
  }
}
</style>
