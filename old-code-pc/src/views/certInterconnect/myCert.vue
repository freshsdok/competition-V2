<template>
  <div class="global-page">
    <div class="container-custom font-sans pb-[60px]">
      <Breadcrumbar />
      <div class="bg-[#fff] pb-[65px]">
        <el-tabs v-model="activeName"><el-tab-pane label="我的证书" name="我的证书" /></el-tabs>
        <div class="pl-[15px] pr-[15px]">
          <div v-if="guidedSummary.certificateCount > 0" class="guided-download-card">
            <div>
              <div class="guided-download-title">团队报名负责人证书打包下载</div>
              <div class="guided-download-desc">
                当前账号负责 {{ guidedSummary.teamCount }} 个获证团队，共 {{ guidedSummary.certificateCount }} 个不重复学生证书编号。
              </div>
            </div>
            <el-button type="primary" :disabled="!guidedSummary.downloadable" @click="openGuidedCertificateDialog">
              打包下载我负责团队的证书
            </el-button>
          </div>

          <el-form :model="queryParams" :inline="true" class="demo-form-inline">
            <el-form-item label="关键字搜索" prop="keyWords">
              <el-input v-model.trim="queryParams.keyWords" placeholder="请输入关键字" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery">查询</el-button>
              <el-button type="primary" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="tableData" class="w-[100%]" border v-loading="loading" empty-text="暂无证书信息"
            :header-cell-style="{ background: '#F9FAFB', color: '#64666A' }">
            <el-table-column prop="certName" label="证书名称" min-width="300" show-overflow-tooltip>
              <template #default="{ row }">{{ row.certName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="orgName" label="颁发机构" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">{{ row.orgName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="player" label="参赛选手" width="130" show-overflow-tooltip>
              <template #default="{ row }">{{ row.player || '-' }}</template>
            </el-table-column>
            <el-table-column prop="guideTeacher" label="指导教师" width="130" show-overflow-tooltip>
              <template #default="{ row }">{{ row.guideTeacher || '-' }}</template>
            </el-table-column>
            <el-table-column prop="certCode" label="证书编号" width="190" show-overflow-tooltip>
              <template #default="{ row }">{{ row.certCode || '-' }}</template>
            </el-table-column>
            <el-table-column prop="certUrl" label="证书查询地址" width="110">
              <template #default="{ row }">
                <a v-if="row.certUrl" :href="row.certUrl" class="text-[#409EFF] cursor-pointer" target="_blank">查询地址</a>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <pagination class="mt-[20px]" v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize" @pagination="getList" />
        </div>
      </div>
    </div>

    <el-dialog v-model="packageDialogVisible" title="我负责团队的学生证书" width="92%" top="5vh" append-to-body
      :close-on-click-modal="!packageLoading" :close-on-press-escape="!packageLoading" @closed="stopCertificatePolling">
      <div class="certificate-dialog-summary">
        当前筛选共 {{ guidedTotal }} 条，已跨页选择 {{ selectedCertCodes.size }} 张。
        <el-button v-if="selectedCertCodes.size" link type="primary" @click="clearSelectedCertificates">清空选择</el-button>
      </div>
      <el-alert v-if="currentPageMissingCount > 0" class="mb-[12px]" type="warning" :closable="false" show-icon
        :title="missingPictureAlert" />

      <el-form :model="certificateFilter" :inline="true" class="certificate-filter-form">
        <el-form-item label="姓名/证书编号">
          <el-input v-model.trim="certificateFilter.keyword" clearable placeholder="姓名或证书编号" style="width: 190px" />
        </el-form-item>
        <el-form-item label="赛事名称">
          <el-select v-model="certificateFilter.contestName" clearable filterable placeholder="全部赛事" style="width: 250px">
            <el-option v-for="item in filterOptions.contestNames" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="届数">
          <el-select v-model="certificateFilter.session" clearable placeholder="全部" style="width: 110px">
            <el-option v-for="item in filterOptions.sessions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="赛区">
          <el-select v-model="certificateFilter.contestArea" clearable filterable placeholder="全部赛区" style="width: 150px">
            <el-option v-for="item in filterOptions.contestAreas" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份">
          <el-select v-model="certificateFilter.runingNumYear" clearable placeholder="全部" style="width: 110px">
            <el-option v-for="item in filterOptions.runingNumYears" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片状态">
          <el-select v-model="certificateFilter.cacheStatus" clearable placeholder="全部" style="width: 130px">
            <el-option label="已缓存" value="SUCCESS" /><el-option label="待获取" value="PENDING" />
            <el-option label="获取中" value="SYNCING" /><el-option label="未找到" value="NOT_FOUND" />
            <el-option label="获取失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchGuidedCertificates">查询</el-button>
          <el-button @click="resetCertificateFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table ref="guidedCertificateTableRef" v-loading="pictureListLoading" :data="guidedCertificateRows" height="410"
        border row-key="certCode" empty-text="暂无可展示的证书信息"
        :header-cell-style="{ background: '#F9FAFB', color: '#64666A' }" @select="handleRowSelection" @select-all="handlePageSelection">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" width="70" align="center">
          <template #default="{ $index }">{{ (guidedPage.pageNum - 1) * guidedPage.pageSize + $index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="certCode" label="证书编号" min-width="200" />
        <el-table-column prop="name" label="获证人" min-width="100"><template #default="{ row }">{{ row.name || '-' }}</template></el-table-column>
        <el-table-column prop="contestName" label="赛事名称" min-width="260" show-overflow-tooltip><template #default="{ row }">{{ row.contestName || '-' }}</template></el-table-column>
        <el-table-column prop="session" label="届数" width="80" align="center"><template #default="{ row }">{{ row.session || '-' }}</template></el-table-column>
        <el-table-column prop="contestArea" label="赛区" min-width="120"><template #default="{ row }">{{ row.contestArea || '-' }}</template></el-table-column>
        <el-table-column prop="runingNumYear" label="年份" width="80" align="center"><template #default="{ row }">{{ row.runingNumYear || '-' }}</template></el-table-column>
        <el-table-column label="图片状态" width="100" align="center">
          <template #default="{ row }"><el-tag :type="cacheStatusType(row.cacheStatus)">{{ cacheStatusLabel(row.cacheStatus) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button link type="primary" :loading="previewingCode === row.certCode" @click="previewCertificate(row)">
              {{ row.cacheStatus === 'SUCCESS' ? '查看' : '获取' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="guidedTotal > 0" :total="guidedTotal" v-model:page="guidedPage.pageNum"
        v-model:limit="guidedPage.pageSize" :page-sizes="[10, 20, 50]" @pagination="loadGuidedCertificatePage" />

      <div v-if="exportTask" class="export-progress-panel">
        <div class="export-progress-title">{{ exportTask.phase || '正在处理导出任务' }}</div>
        <el-progress :percentage="Number(exportTask.progress || 0)"
          :status="exportTask.taskStatus === 'FAILED' ? 'exception' : exportProgressStatus" />
        <div class="export-progress-detail">
          已处理 {{ exportTask.processedCount || 0 }}/{{ exportTask.totalCount || 0 }}，成功 {{ exportTask.successCount || 0 }}，未获取 {{ exportTask.failureCount || 0 }}。
        </div>
        <div v-if="exportTask.lastError" class="export-error">{{ exportTask.lastError }}</div>
        <el-button v-if="['COMPLETED', 'PARTIAL'].includes(exportTask.taskStatus)" class="mt-[10px]" type="primary" link
          @click="downloadExportResult(exportTask.taskId)">重新下载压缩包</el-button>
      </div>

      <template #footer>
        <el-button :disabled="packageLoading" @click="packageDialogVisible = false">关闭</el-button>
        <el-button type="primary" plain :loading="packageLoading" :disabled="selectedCertCodes.size === 0" @click="createExport('SELECTED')">
          导出选中（{{ selectedCertCodes.size }}）
        </el-button>
        <el-button type="primary" :loading="packageLoading" :disabled="guidedSummary.certificateCount === 0" @click="createExport('ALL')">
          全部导出（{{ guidedSummary.certificateCount }}）
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialogVisible" :title="`证书预览${previewCertCode ? `（${previewCertCode}）` : ''}`"
      width="80%" top="5vh" append-to-body destroy-on-close>
      <div class="certificate-preview-container">
        <el-image v-if="previewImageUrl" :src="previewImageUrl" fit="contain" class="certificate-preview-image"
          @error="handlePreviewImageError" />
      </div>
      <template #footer>
        <a v-if="previewImageUrl" :href="previewImageUrl" target="_blank" rel="noopener noreferrer">
          <el-button style="margin-right: 10px;">下载</el-button>
        </a>
        <el-button type="primary" @click="previewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import Breadcrumbar from "@/components/breadcrumbar.vue";
import {
  createGuidedCertificateExportTask, fallbackGuidedCertificatePictures,
  getCertInterconnectApplyDetailNoAuthList, getGuidedCertificateExportDownload,
  getGuidedCertificateExportTask, getGuidedCertificateFilterOptions, getGuidedCertificatePage,
  getGuidedCertificatePreview, getGuidedCertificateSummary,
} from "@/api/certInterconnect";
import { ElMessage } from "element-plus";

const activeName = ref("我的证书");
const tableData = ref([]);
const total = ref(0);
const loading = ref(false);
const pictureListLoading = ref(false);
const packageLoading = ref(false);
const packageDialogVisible = ref(false);
const guidedCertificateTableRef = ref();
const guidedCertificateRows = ref([]);
const guidedTotal = ref(0);
const selectedCertCodes = reactive(new Set());
const fallbackRequested = new Set();
const previewingCode = ref("");
const previewDialogVisible = ref(false);
const previewImageUrl = ref("");
const previewCertCode = ref("");
const exportTask = ref(null);
let certificatePollingTimer;
let exportPollingTimer;

const queryParams = ref({ pageNum: 1, pageSize: 10, keyWords: "", issuanceStartTime: "", issuanceEndTime: "" });
const guidedPage = ref({ pageNum: 1, pageSize: 10 });
const certificateFilter = ref({ keyword: "", contestName: "", session: "", contestArea: "", runingNumYear: "", cacheStatus: "" });
const filterOptions = ref({ contestNames: [], sessions: [], contestAreas: [], runingNumYears: [] });
const guidedSummary = ref({ teamCount: 0, certificateCount: 0, downloadable: false });
const currentPageMissingCount = computed(() => guidedCertificateRows.value.filter((row) => row.cacheStatus !== "SUCCESS").length);
const currentPageFetchingCount = computed(() => guidedCertificateRows.value
  .filter((row) => ["PENDING", "SYNCING"].includes(row.cacheStatus)).length);
const missingPictureAlert = computed(() => currentPageFetchingCount.value > 0
  ? `当前页有 ${currentPageFetchingCount.value} 张图片正在排队或获取；所有记录仍可勾选导出。`
  : `当前页有 ${currentPageMissingCount.value} 张图片未找到或获取失败，已进入重试冷却；记录仍可勾选导出。`);
const exportProgressStatus = computed(() => ["COMPLETED", "PARTIAL"].includes(exportTask.value?.taskStatus) ? "success" : undefined);

const handleQuery = () => { queryParams.value.pageNum = 1; getList(); };
const handleReset = () => { queryParams.value = { pageNum: 1, pageSize: 10, keyWords: "", issuanceStartTime: "", issuanceEndTime: "" }; getList(); };
const getList = async () => {
  loading.value = true;
  try {
    const response = await getCertInterconnectApplyDetailNoAuthList(queryParams.value);
    tableData.value = response?.rows || [];
    total.value = response?.total || 0;
  } catch (error) { console.error("获取列表数据失败:", error); } finally { loading.value = false; }
};
const loadGuidedSummary = async () => {
  try {
    const response = await getGuidedCertificateSummary();
    guidedSummary.value = { ...guidedSummary.value, ...(response?.data || {}) };
  } catch (error) { console.error("获取负责人证书统计失败:", error); }
};

const openGuidedCertificateDialog = async () => {
  packageDialogVisible.value = true;
  selectedCertCodes.clear();
  exportTask.value = null;
  guidedPage.value.pageNum = 1;
  resetCertificateFilter(false);
  try {
    const response = await getGuidedCertificateFilterOptions();
    filterOptions.value = response?.data || filterOptions.value;
  } catch (error) { console.error("获取证书筛选项失败:", error); }
  await loadGuidedCertificatePage();
};
const loadGuidedCertificatePage = async () => {
  pictureListLoading.value = true;
  try {
    const response = await getGuidedCertificatePage(certificateFilter.value, guidedPage.value.pageNum, guidedPage.value.pageSize);
    guidedCertificateRows.value = response?.rows || [];
    guidedTotal.value = response?.total || 0;
    await restoreCurrentPageSelection();
    queueCurrentPageFallback();
  } catch (error) { ElMessage.error(error?.message || "获取负责人证书列表失败"); }
  finally { pictureListLoading.value = false; }
};
const restoreCurrentPageSelection = async () => {
  await nextTick();
  guidedCertificateTableRef.value?.clearSelection();
  guidedCertificateRows.value.forEach((row) => {
    if (selectedCertCodes.has(row.certCode)) guidedCertificateTableRef.value?.toggleRowSelection(row, true);
  });
};
const handleRowSelection = (selection, row) => {
  if (selection.some((item) => item.certCode === row.certCode)) selectedCertCodes.add(row.certCode);
  else selectedCertCodes.delete(row.certCode);
};
const handlePageSelection = (selection) => {
  const currentSelected = new Set(selection.map((row) => row.certCode));
  guidedCertificateRows.value.forEach((row) => currentSelected.has(row.certCode)
    ? selectedCertCodes.add(row.certCode) : selectedCertCodes.delete(row.certCode));
};
const clearSelectedCertificates = async () => { selectedCertCodes.clear(); await restoreCurrentPageSelection(); };

const queueCurrentPageFallback = async () => {
  const missing = guidedCertificateRows.value
    .filter((row) => row.cacheStatus === "PENDING" && !fallbackRequested.has(row.certCode))
    .map((row) => row.certCode);
  if (!missing.length) {
    if (currentPageFetchingCount.value === 0) stopCertificatePolling();
    else startCertificatePolling();
    return;
  }
  missing.forEach((code) => fallbackRequested.add(code));
  try { await fallbackGuidedCertificatePictures(missing); startCertificatePolling(); }
  catch (error) { console.error("提交证书图片兜底任务失败:", error); }
};
const startCertificatePolling = () => {
  if (certificatePollingTimer) return;
  certificatePollingTimer = window.setInterval(async () => {
    if (!packageDialogVisible.value || packageLoading.value) return;
    await loadGuidedCertificatePage();
    guidedCertificateRows.value.filter((row) => row.cacheStatus === "SUCCESS")
      .forEach((row) => fallbackRequested.delete(row.certCode));
    if (currentPageFetchingCount.value === 0) stopCertificatePolling();
  }, 5000);
};
const stopCertificatePolling = () => {
  if (certificatePollingTimer) window.clearInterval(certificatePollingTimer);
  certificatePollingTimer = undefined;
};

const searchGuidedCertificates = () => { guidedPage.value.pageNum = 1; loadGuidedCertificatePage(); };
const resetCertificateFilter = (reload = true) => {
  certificateFilter.value = { keyword: "", contestName: "", session: "", contestArea: "", runingNumYear: "", cacheStatus: "" };
  guidedPage.value.pageNum = 1;
  if (reload) loadGuidedCertificatePage();
};
const previewCertificate = async (row) => {
  if (row.cacheStatus !== "SUCCESS") {
    try {
      fallbackRequested.add(row.certCode);
      const response = await fallbackGuidedCertificatePictures([row.certCode]);
      if (Number(response?.data?.acceptedCount || 0) > 0) {
        startCertificatePolling();
        ElMessage.info("已优先提交图片获取任务，请稍后查看");
      } else {
        ElMessage.warning("该证书正在获取或失败冷却中，请稍后再试");
      }
    } catch (error) {
      fallbackRequested.delete(row.certCode);
      ElMessage.error(error?.message || "提交图片获取任务失败");
    }
    return;
  }
  previewingCode.value = row.certCode;
  try {
    const response = await getGuidedCertificatePreview(row.certCode);
    console.log(response);
    
    const imageUrl = response.msg;
    if (!imageUrl || !["http:", "https:"].includes(new URL(imageUrl, window.location.origin).protocol)) {
      throw new Error("接口未返回有效的证书图片地址");
    }
    previewImageUrl.value = imageUrl;
    previewCertCode.value = row.certCode;
    previewDialogVisible.value = true;
  } catch (error) { ElMessage.error(error?.message || "打开证书图片失败"); }
  finally { previewingCode.value = ""; }
};
const handlePreviewImageError = () => {
  ElMessage.error("证书图片加载失败，下载地址可能已过期，请关闭后重试");
};

const createExport = async (scope) => {
  if (packageLoading.value) return;
  packageLoading.value = true;
  stopCertificatePolling();
  try {
    const response = await createGuidedCertificateExportTask({ scope, certCodes: scope === "SELECTED" ? [...selectedCertCodes] : [] });
    exportTask.value = response?.data;
    startExportPolling(exportTask.value.taskId);
  } catch (error) {
    packageLoading.value = false;
    ElMessage.error(error?.message || "创建证书导出任务失败");
  }
};
const startExportPolling = (taskId) => {
  if (exportPollingTimer) window.clearInterval(exportPollingTimer);
  exportPollingTimer = window.setInterval(async () => {
    try {
      const response = await getGuidedCertificateExportTask(taskId);
      exportTask.value = response?.data || exportTask.value;
      if (["COMPLETED", "PARTIAL"].includes(exportTask.value.taskStatus)) {
        stopExportPolling();
        await downloadExportResult(taskId);
        packageLoading.value = false;
        ElMessage.success(exportTask.value.taskStatus === "PARTIAL" ? "压缩包已生成，内含未下载编号清单" : "证书压缩包已生成");
      } else if (exportTask.value.taskStatus === "FAILED") {
        stopExportPolling(); packageLoading.value = false;
        ElMessage.error(exportTask.value.lastError || "证书导出失败");
      }
    } catch (error) {
      stopExportPolling(); packageLoading.value = false;
      ElMessage.error(error?.message || "查询导出进度失败");
    }
  }, 2000);
};
const stopExportPolling = () => {
  if (exportPollingTimer) window.clearInterval(exportPollingTimer);
  exportPollingTimer = undefined;
};
const downloadExportResult = async (taskId) => {
  const response = await getGuidedCertificateExportDownload(taskId);
  const result = response?.data || {};
  if (!result.downloadUrl) throw new Error("未获取到压缩包下载地址");
  const link = document.createElement("a");
  link.href = result.downloadUrl;
  link.download = result.fileName || "学生获奖证书.zip";
  link.rel = "noopener noreferrer";
  document.body.appendChild(link); link.click(); link.remove();
};
const cacheStatusLabel = (value) => ({ PENDING: "待获取", SYNCING: "获取中", SUCCESS: "已缓存", NOT_FOUND: "未找到", FAILED: "获取失败" }[value] || "待获取");
const cacheStatusType = (value) => ({ SUCCESS: "success", SYNCING: "primary", NOT_FOUND: "warning", FAILED: "danger" }[value] || "info");

onMounted(() => { getList(); loadGuidedSummary(); });
onBeforeUnmount(() => { stopCertificatePolling(); stopExportPolling(); });
</script>

<style lang="scss" scoped>
:deep(.el-tabs__nav .el-tabs__item) { margin: 15px; font-size: 20px; }
.guided-download-card { display: flex; align-items: center; justify-content: space-between; gap: 24px; margin: 0 0 20px; padding: 18px 20px; border: 1px solid #d9ecff; border-radius: 4px; background: #f5faff; }
.guided-download-title { margin-bottom: 8px; color: #303133; font-size: 16px; font-weight: 600; }
.guided-download-desc, .certificate-dialog-summary { color: #606266; line-height: 22px; }
.certificate-dialog-summary { margin-bottom: 12px; }
.certificate-filter-form { margin-bottom: 4px; }
.export-progress-panel { margin-top: 16px; padding: 14px 16px; border: 1px solid #d9ecff; border-radius: 4px; background: #f5faff; }
.export-progress-title { margin-bottom: 10px; color: #303133; font-weight: 600; }
.export-progress-detail { margin-top: 8px; color: #606266; }
.export-error { margin-top: 6px; color: #f56c6c; }
.certificate-preview-container { display: flex; justify-content: center; min-height: 240px; max-height: 72vh; overflow: auto; background: #f5f7fa; }
.certificate-preview-image { width: 100%; min-height: 240px; max-height: 72vh; }
@media (max-width: 768px) { .guided-download-card { align-items: flex-start; flex-direction: column; } }
</style>
