<template>
  <div class="app-container certificate-image-sync-page">
    <el-alert
      title="图片正文保存到私有 OSS/MinIO；本页只展示缓存元数据和同步状态。所有外部查询共享全局限速。"
      type="info"
      :closable="false"
      show-icon
      class="page-alert"
    />

    <div class="summary-grid" v-loading="overviewLoading">
      <el-card v-for="item in summaryItems" :key="item.label" shadow="never" class="summary-card">
        <div class="summary-label">{{ item.label }}</div>
        <div class="summary-value" :class="item.className">{{ item.value }}</div>
      </el-card>
    </div>

    <el-card shadow="never" class="control-card">
      <template #header>
        <div class="card-header">
          <span>同步控制</span>
          <el-tag :type="runStatusType(currentRun?.runStatus)">
            {{ runStatusLabel(currentRun?.runStatus) }}
          </el-tag>
        </div>
      </template>

      <div class="control-meta">
        <span>当前限速：{{ overview.requestsPerSecond || 1 }} 次/秒</span>
        <span>最近执行：{{ formatDate(overview.lastRunTime) }}</span>
        <span>预计剩余：{{ formatDuration(overview.estimatedRemainingSeconds) }}</span>
      </div>

      <template v-if="currentRun">
        <el-descriptions :column="4" border class="run-description">
          <el-descriptions-item label="任务来源">{{ sourceLabel(currentRun.source) }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDate(currentRun.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="当前证书">{{ currentRun.currentCertCode || "-" }}</el-descriptions-item>
          <el-descriptions-item label="成功/失败">
            {{ currentRun.successCount || 0 }} / {{ currentRun.failureCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRun.lastError" label="最近错误" :span="4">
            <span class="error-text">{{ currentRun.lastError }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-progress
          :percentage="runProgress"
          :status="currentRun.runStatus === 'FAILED' ? 'exception' : undefined"
          :stroke-width="16"
          class="run-progress"
        >
          <span>{{ currentRun.processedCount || 0 }} / {{ currentRun.totalCount || 0 }}</span>
        </el-progress>
      </template>
      <el-empty v-else description="当前没有运行中的同步任务" :image-size="70" />

      <div class="action-row">
        <el-button
          type="primary"
          :loading="actionLoading === 'start'"
          :disabled="Boolean(currentRun)"
          v-hasPermi="['competition:certificateImageSync:start']"
          @click="handleStart"
        >启动同步</el-button>
        <el-button
          v-if="currentRun?.runStatus === 'RUNNING'"
          type="warning"
          :loading="actionLoading === 'pause'"
          v-hasPermi="['competition:certificateImageSync:pause']"
          @click="handlePause"
        >暂停同步</el-button>
        <el-button
          v-if="currentRun?.runStatus === 'PAUSED'"
          type="success"
          :loading="actionLoading === 'resume'"
          v-hasPermi="['competition:certificateImageSync:pause']"
          @click="handleResume"
        >继续同步</el-button>
        <el-button
          type="danger"
          plain
          :loading="actionLoading === 'retry'"
          :disabled="currentRun?.runStatus === 'RUNNING'"
          v-hasPermi="['competition:certificateImageSync:retry']"
          @click="handleRetry"
        >重试失败记录</el-button>
        <el-button @click="refreshAll">刷新</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="list-card">
      <template #header><span>缓存记录</span></template>
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="证书编号">
          <el-input v-model.trim="queryParams.certCode" clearable placeholder="证书编号" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model.trim="queryParams.name" clearable placeholder="获证人" />
        </el-form-item>
        <el-form-item label="赛事">
          <el-input v-model.trim="queryParams.contestName" clearable placeholder="赛事名称" />
        </el-form-item>
        <el-form-item label="赛区">
          <el-input v-model.trim="queryParams.contestArea" clearable placeholder="赛区" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.cacheStatus" clearable placeholder="全部" style="width: 150px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="listLoading" :data="rows" border>
        <el-table-column prop="certCode" label="证书编号" width="210" />
        <el-table-column prop="name" label="获证人" width="120" show-overflow-tooltip />
        <el-table-column prop="contestName" label="赛事名称" min-width="260" show-overflow-tooltip />
        <el-table-column prop="session" label="届数" width="80" />
        <el-table-column prop="contestArea" label="赛区" width="140" show-overflow-tooltip />
        <el-table-column prop="runingNumYear" label="年份" width="80" align="center" />
        <el-table-column prop="cacheStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="cacheStatusType(row.cacheStatus)">{{ cacheStatusLabel(row.cacheStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试" width="70" align="center" />
        <el-table-column prop="fileSize" label="大小" width="100" align="right">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="lastSyncTime" label="最后同步" width="170" />
        <el-table-column prop="lastError" label="最近错误" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="['competition:certificateImageSync:retry']"
              @click="handleResetCertificate(row)"
            >重置</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="loadList"
      />
    </el-card>

    <el-card shadow="never" class="history-card">
      <template #header><span>最近同步记录</span></template>
      <el-table :data="history" border max-height="360">
        <el-table-column prop="runId" label="任务ID" width="100" />
        <el-table-column label="来源" width="100"><template #default="{ row }">{{ sourceLabel(row.source) }}</template></el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }">{{ runStatusLabel(row.runStatus) }}</template></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="processedCount" label="已处理" width="90" />
        <el-table-column prop="successCount" label="成功" width="80" />
        <el-table-column prop="failureCount" label="失败" width="80" />
        <el-table-column prop="operatorName" label="操作人" width="110" />
        <el-table-column prop="lastError" label="错误" min-width="220" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import {
  getCertificateImageHistory,
  getCertificateImageList,
  getCertificateImageOverview,
  pauseCertificateImageSync,
  resetCertificateImage,
  resumeCertificateImageSync,
  retryCertificateImageFailures,
  startCertificateImageSync,
} from "@/api/certInterconnect/certificateImageSync";

const { proxy } = getCurrentInstance();
const overviewLoading = ref(false);
const listLoading = ref(false);
const actionLoading = ref("");
const overview = ref({ requestsPerSecond: 1 });
const rows = ref([]);
const total = ref(0);
const history = ref([]);
let pollingTimer;

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  certCode: "",
  name: "",
  contestName: "",
  contestArea: "",
  cacheStatus: "",
});
const statusOptions = [
  { value: "PENDING", label: "待同步" },
  { value: "SYNCING", label: "同步中" },
  { value: "SUCCESS", label: "已缓存" },
  { value: "NOT_FOUND", label: "未找到" },
  { value: "FAILED", label: "失败" },
];
const currentRun = computed(() => overview.value.currentRun || null);
const runProgress = computed(() => {
  const totalCount = Number(currentRun.value?.totalCount || 0);
  return totalCount ? Math.min(100, Math.round(Number(currentRun.value?.processedCount || 0) * 100 / totalCount)) : 0;
});
const summaryItems = computed(() => [
  { label: "应同步总数", value: overview.value.totalCount || 0 },
  { label: "已缓存", value: overview.value.successCount || 0, className: "success" },
  { label: "待同步", value: overview.value.pendingCount || 0 },
  { label: "同步中", value: overview.value.syncingCount || 0, className: "primary" },
  { label: "未找到", value: overview.value.notFoundCount || 0, className: "warning" },
  { label: "失败", value: overview.value.failedCount || 0, className: "danger" },
  { label: "缓存覆盖率", value: `${Number(overview.value.coverageRate || 0).toFixed(2)}%`, className: "success" },
]);

const loadOverview = async () => {
  overviewLoading.value = true;
  try {
    const response = await getCertificateImageOverview();
    overview.value = response?.data || {};
  } finally {
    overviewLoading.value = false;
  }
};
const loadList = async () => {
  listLoading.value = true;
  try {
    const response = await getCertificateImageList(queryParams.value);
    rows.value = response?.rows || [];
    total.value = response?.total || 0;
  } finally {
    listLoading.value = false;
  }
};
const loadHistory = async () => {
  const response = await getCertificateImageHistory();
  history.value = response?.data || [];
};
const refreshAll = async () => Promise.all([loadOverview(), loadList(), loadHistory()]);

const handleStart = async () => {
  const remaining = Number(overview.value.totalCount || 0) - Number(overview.value.successCount || 0);
  const estimate = formatDuration(Math.ceil(remaining / Number(overview.value.requestsPerSecond || 1)));
  await proxy.$modal.confirm(`将同步 ${remaining} 条非成功记录，按当前限速预计至少 ${estimate}。是否启动？`);
  actionLoading.value = "start";
  try {
    await startCertificateImageSync();
    proxy.$modal.msgSuccess("同步任务已启动");
    await refreshAll();
  } finally {
    actionLoading.value = "";
  }
};
const handlePause = async () => runAction("pause", pauseCertificateImageSync, "同步任务已暂停");
const handleResume = async () => runAction("resume", resumeCertificateImageSync, "同步任务已继续");
const handleRetry = async () => {
  await proxy.$modal.confirm("将把未找到和失败记录重置为待同步，是否继续？");
  await runAction("retry", retryCertificateImageFailures, "失败记录已重新激活");
};
const handleResetCertificate = async (row) => {
  await proxy.$modal.confirm(`将证书 ${row.certCode} 重置为待同步，旧图片会在新图片同步成功后删除。是否继续？`);
  await resetCertificateImage(row.certCode);
  proxy.$modal.msgSuccess("证书已重置为待同步");
  await refreshAll();
};
const runAction = async (name, action, message) => {
  actionLoading.value = name;
  try {
    await action();
    proxy.$modal.msgSuccess(message);
    await refreshAll();
  } finally {
    actionLoading.value = "";
  }
};
const handleQuery = () => { queryParams.value.pageNum = 1; loadList(); };
const resetQuery = () => {
  queryParams.value = { pageNum: 1, pageSize: 10, certCode: "", name: "", contestName: "", contestArea: "", cacheStatus: "" };
  loadList();
};

const cacheStatusLabel = (value) => statusOptions.find((item) => item.value === value)?.label || value || "-";
const cacheStatusType = (value) => ({ SUCCESS: "success", SYNCING: "primary", NOT_FOUND: "warning", FAILED: "danger" }[value] || "info");
const runStatusLabel = (value) => ({ RUNNING: "运行中", PAUSED: "已暂停", COMPLETED: "已完成", FAILED: "失败" }[value] || "无任务");
const runStatusType = (value) => ({ RUNNING: "primary", PAUSED: "warning", COMPLETED: "success", FAILED: "danger" }[value] || "info");
const sourceLabel = (value) => ({ MANUAL: "管理员", SCHEDULED: "夜间定时" }[value] || value || "-");
const formatDate = (value) => value || "-";
const formatDuration = (seconds) => {
  const value = Number(seconds || 0);
  if (!value) return "0分钟";
  const days = Math.floor(value / 86400);
  const hours = Math.floor((value % 86400) / 3600);
  const minutes = Math.ceil((value % 3600) / 60);
  return [days && `${days}天`, hours && `${hours}小时`, minutes && `${minutes}分钟`].filter(Boolean).join("");
};
const formatSize = (bytes) => {
  const value = Number(bytes || 0);
  if (!value) return "-";
  return value >= 1024 * 1024 ? `${(value / 1024 / 1024).toFixed(2)} MB` : `${(value / 1024).toFixed(1)} KB`;
};

onMounted(() => {
  refreshAll();
  pollingTimer = window.setInterval(() => {
    if (currentRun.value) refreshAll();
  }, 5000);
});
onBeforeUnmount(() => window.clearInterval(pollingTimer));
</script>

<style scoped lang="scss">
.page-alert { margin-bottom: 16px; }
.summary-grid { display: grid; grid-template-columns: repeat(7, minmax(130px, 1fr)); gap: 12px; margin-bottom: 16px; }
.summary-card :deep(.el-card__body) { padding: 18px; }
.summary-label { color: #909399; font-size: 13px; }
.summary-value { margin-top: 8px; color: #303133; font-size: 26px; font-weight: 600; }
.summary-value.success { color: #67c23a; }
.summary-value.primary { color: #409eff; }
.summary-value.warning { color: #e6a23c; }
.summary-value.danger { color: #f56c6c; }
.control-card, .list-card, .history-card { margin-bottom: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; font-weight: 600; }
.control-meta { display: flex; flex-wrap: wrap; gap: 28px; margin-bottom: 16px; color: #606266; }
.run-description { margin-bottom: 16px; }
.run-progress { margin: 18px 0; }
.action-row { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 16px; }
.error-text { color: #f56c6c; }
@media (max-width: 1400px) { .summary-grid { grid-template-columns: repeat(4, minmax(130px, 1fr)); } }
@media (max-width: 760px) { .summary-grid { grid-template-columns: repeat(2, minmax(130px, 1fr)); } }
</style>
