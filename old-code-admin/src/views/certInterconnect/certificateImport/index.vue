<template>
  <div class="app-container certificate-import-page">
    <el-alert
      title="本页面只校验 Excel 并生成 SQL 文件，不会直接修改数据库"
      type="info"
      :closable="false"
      show-icon
      class="page-alert"
    />

    <el-card shadow="never" class="config-card">
      <template #header>
        <div class="card-title">
          <el-icon><Tickets /></el-icon>
          <span>导入配置</span>
        </div>
      </template>

      <el-form ref="configFormRef" :model="configForm" :rules="configRules" label-width="110px">
        <div class="config-grid">
          <SelectCompetitionBack
            v-model="configForm"
            :label-prop="competitionLabelProp"
          />
          <el-form-item label="证书类型" prop="certificateType">
            <el-select v-model="configForm.certificateType" placeholder="请选择证书类型" style="width: 240px">
              <el-option
                v-for="option in certificateTypeOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="发证日期" prop="issuanceDate">
            <el-date-picker
              v-model="configForm.issuanceDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择发证日期"
              style="width: 240px"
            />
          </el-form-item>
        </div>
      </el-form>

      <el-descriptions v-if="configForm.competitionSeriesId" :column="3" border class="selected-competition">
        <el-descriptions-item label="赛事系列ID">{{ configForm.competitionSeriesId }}</el-descriptions-item>
        <el-descriptions-item label="所属大赛ID">{{ configForm.competitionId || "-" }}</el-descriptions-item>
        <el-descriptions-item label="届数">{{ configForm.competitionSeriesName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="大赛" :span="3">{{ configForm.competitionName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="写入规则" :span="3">
          全部有效数据写入历史证书表；成功唯一关联账号的数据同时写入用户源证书表
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="upload-card">
      <template #header>
        <div class="card-title">
          <el-icon><UploadFilled /></el-icon>
          <span>上传证书 Excel</span>
        </div>
      </template>

      <div class="header-label">
        {{ selectedTypeLabel || "所选证书类型" }}模板表头
      </div>
      <div class="header-tags">
        <el-tag v-for="header in currentHeaders" :key="header" type="info" effect="plain">
          {{ header }}
        </el-tag>
      </div>
      <div class="minor-tip">“序号”和“流水号”可保留但会被忽略；Excel 中的证书日期用于人工核对，实际以导入配置中的发证日期为准。</div>

      <el-upload
        ref="uploadRef"
        v-model:file-list="fileList"
        class="certificate-uploader"
        drag
        action="#"
        accept=".xlsx,.xls"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :on-exceed="handleFileExceed"
        :disabled="processing"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖入文件，或<em>点击选择</em></div>
        <template #tip>
          <div class="el-upload__tip">支持 xlsx、xls，单个文件不超过 20MB，最多处理 50000 条证书数据。</div>
        </template>
      </el-upload>

      <el-alert type="warning" :closable="false" show-icon class="rule-alert">
        <template #title>当前类型生成规则</template>
        <div class="rule-content">
          <template v-if="configForm.certificateType === 'STUDENT_PERSONAL'">
            <div>个人证书按“参赛姓名 + 参赛单位 + 大赛名称中的赛道”关联所选赛事系列的已缴费报名用户。</div>
            <div>证书名称示例：第十三届大学生新一代信息通信科技大赛工程实践赛道中，荣获全国总决赛一等奖，特此表彰！</div>
          </template>
          <template v-else-if="configForm.certificateType === 'STUDENT_TEAM'">
            <div>只使用完整参赛队员名单唯一定位所选赛事系列中的团队，不跨赛事系列兜底；团队名称、学校和指导教师不参与匹配。</div>
            <div>匹配成功后，每名参赛队员都获得同一证书编号，指导教师不发证；优胜奖保存为奖项编码4。</div>
          </template>
          <template v-else-if="configForm.certificateType === 'TEACHER_HONOR'">
            <div>按“老师姓名 + 参赛单位 + 奖项中的赛道”定位指导教师报名；仅在账号归属唯一时进入“我的证书”。</div>
            <div>未匹配账号或账号不唯一的教师证书只写历史表，组合奖项保留完整中文。</div>
          </template>
          <template v-else-if="configForm.certificateType === 'ORGANIZATION_HONOR'">
            <div>优秀组织单位证书只写历史表，用于证书编号查询，不进入任何个人“我的证书”。</div>
            <div>称号保留 Excel 完整中文。</div>
          </template>
          <div v-else>请先选择证书类型，页面会展示对应模板与匹配规则。</div>
          <div>未关联或关联不唯一的数据仍进入历史证书表，详情会显示在预览中并写入 SQL 顶部。</div>
        </div>
      </el-alert>

      <div class="action-row">
        <el-button
          type="primary"
          size="large"
          :icon="Search"
          :loading="previewing"
          :disabled="!selectedFile || generating"
          v-hasPermi="['competition:certificateImport:generateSql']"
          @click="handlePreview"
        >
          {{ previewing ? "正在校验" : "校验并预览" }}
        </el-button>
        <el-button
          type="success"
          size="large"
          :icon="Download"
          :loading="generating"
          :disabled="!previewResult || !previewCurrent || previewing"
          v-hasPermi="['competition:certificateImport:generateSql']"
          @click="handleGenerate"
        >
          {{ generating ? "正在生成" : "生成并下载 SQL" }}
        </el-button>
        <span class="privacy-tip">生成的 SQL 含用户关联信息，请妥善保管。</span>
      </div>
    </el-card>

    <el-card v-if="previewResult" shadow="never" class="result-card">
      <template #header>
        <div class="card-title">
          <el-icon><DataAnalysis /></el-icon>
          <span>导入预览</span>
        </div>
      </template>
      <div class="summary-grid">
        <div class="summary-item">
          <span>历史证书</span>
          <strong>{{ previewResult.historyRowCount }}</strong>
        </div>
        <div class="summary-item success">
          <span>用户源证书</span>
          <strong>{{ previewResult.originRowCount }}</strong>
        </div>
        <div class="summary-item" :class="{ warning: previewResult.warningCount > 0 }">
          <span>关联提醒</span>
          <strong>{{ previewResult.warningCount }}</strong>
        </div>
      </div>

      <el-alert
        v-if="previewResult.warningCount"
        title="存在只写历史表或部分队员未关联的数据，请确认后再下载 SQL"
        type="warning"
        :closable="false"
        show-icon
        class="preview-warning"
      />
      <el-table v-if="previewWarnings.length" :data="previewWarnings" border max-height="420">
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="message" label="关联提醒" min-width="680" show-overflow-tooltip />
      </el-table>
      <div v-if="hiddenWarningCount > 0" class="hidden-warning-tip">
        页面仅展示前 {{ MAX_PREVIEW_WARNINGS }} 条，另有 {{ hiddenWarningCount }} 条会完整写入 SQL 顶部。
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { DataAnalysis, Download, Search, Tickets, UploadFilled } from "@element-plus/icons-vue";
import { genFileId } from "element-plus";
import { saveAs } from "file-saver";
import SelectCompetitionBack from "@/views/certInterconnect/components/SelectCompetitionBack.vue";
import {
  generateCertificateImportSql,
  previewCertificateImport,
} from "@/api/certInterconnect/certificateImport";

const { proxy } = getCurrentInstance();
const MAX_FILE_SIZE = 20 * 1024 * 1024;
const MAX_PREVIEW_WARNINGS = 100;

const certificateTypeOptions = [
  { value: "STUDENT_PERSONAL", label: "学生个人证书" },
  { value: "STUDENT_TEAM", label: "学生团队证书" },
  { value: "TEACHER_HONOR", label: "优秀指导教师证书" },
  { value: "ORGANIZATION_HONOR", label: "优秀组织单位证书" },
];
const certificateHeaders = {
  STUDENT_PERSONAL: ["参赛姓名", "届数", "大赛名称", "赛区", "奖项", "证书编号", "参赛单位", "参赛队员", "指导教师"],
  STUDENT_TEAM: ["参赛姓名（团队名称）或团队名称", "届数", "大赛名称", "赛区", "奖项", "证书编号", "参赛单位", "参赛队员", "指导教师"],
  TEACHER_HONOR: ["老师姓名", "届数", "大赛名称", "赛区", "奖项", "证书编号", "参赛单位"],
  ORGANIZATION_HONOR: ["届数", "大赛名称", "称号", "证书编号", "参赛单位"],
};
const competitionLabelProp = {
  name: "赛事系列",
  sessionNum: "届数",
  stage: "",
  track: "",
  second: "",
};

const configFormRef = ref();
const uploadRef = ref();
const fileList = ref([]);
const selectedFile = ref(null);
const previewing = ref(false);
const generating = ref(false);
const previewResult = ref(null);
const previewSignature = ref("");
const configForm = ref({
  competitionSeriesId: undefined,
  competitionSeriesName: undefined,
  competitionId: undefined,
  competitionName: undefined,
  certificateType: undefined,
  issuanceDate: undefined,
});
const configRules = {
  competitionSeriesId: [{ required: true, message: "请选择赛事系列", trigger: "change" }],
  certificateType: [{ required: true, message: "请选择证书类型", trigger: "change" }],
  issuanceDate: [{ required: true, message: "请选择发证日期", trigger: "change" }],
};

const processing = computed(() => previewing.value || generating.value);
const selectedTypeLabel = computed(() => certificateTypeOptions.find(
  (item) => item.value === configForm.value.certificateType,
)?.label);
const currentHeaders = computed(() => certificateHeaders[configForm.value.certificateType] || [
  "请先选择证书类型",
]);
const previewWarnings = computed(() => (previewResult.value?.warnings || [])
  .slice(0, MAX_PREVIEW_WARNINGS)
  .map((message) => ({ message })));
const hiddenWarningCount = computed(() => Math.max(
  0,
  (previewResult.value?.warnings?.length || 0) - MAX_PREVIEW_WARNINGS,
));
const previewCurrent = computed(() => previewSignature.value === buildPreviewSignature());

watch(
  () => [
    configForm.value.competitionSeriesId,
    configForm.value.certificateType,
    configForm.value.issuanceDate,
  ],
  () => invalidatePreview(),
);

function validateFile(file) {
  const extension = file.name.split(".").pop()?.toLowerCase();
  if (!["xlsx", "xls"].includes(extension)) {
    proxy.$modal.msgWarning("仅支持 xlsx 或 xls 格式文件");
    return false;
  }
  if (file.size > MAX_FILE_SIZE) {
    proxy.$modal.msgWarning("Excel 文件不能超过 20MB");
    return false;
  }
  return true;
}

function handleFileChange(uploadFile) {
  const rawFile = uploadFile.raw;
  if (!rawFile || !validateFile(rawFile)) {
    uploadRef.value?.clearFiles();
    selectedFile.value = null;
    invalidatePreview();
    return;
  }
  fileList.value = [uploadFile];
  selectedFile.value = rawFile;
  invalidatePreview();
}

function handleFileRemove() {
  selectedFile.value = null;
  invalidatePreview();
}

function handleFileExceed(files) {
  uploadRef.value?.clearFiles();
  const file = files[0];
  if (file) {
    file.uid = genFileId();
    uploadRef.value?.handleStart(file);
  }
}

async function validateInputs() {
  if (!selectedFile.value || !validateFile(selectedFile.value)) {
    proxy.$modal.msgWarning("请先选择证书 Excel 文件");
    return false;
  }
  try {
    await configFormRef.value?.validate();
    return true;
  } catch (_) {
    return false;
  }
}

async function handlePreview() {
  if (!(await validateInputs())) {
    return;
  }
  previewing.value = true;
  previewResult.value = null;
  previewSignature.value = "";
  try {
    const response = await previewCertificateImport(selectedFile.value, configForm.value);
    previewResult.value = response.data;
    previewSignature.value = buildPreviewSignature();
    if (response.data.warningCount > 0) {
      proxy.$modal.msgWarning(`校验完成，${response.data.warningCount} 行存在用户关联提醒`);
    } else {
      proxy.$modal.msgSuccess("Excel 校验通过，未发现用户关联提醒");
    }
  } catch (error) {
    console.error("证书导入预览失败", error);
  } finally {
    previewing.value = false;
  }
}

async function handleGenerate() {
  if (!(await validateInputs()) || !previewCurrent.value) {
    proxy.$modal.msgWarning("文件或导入配置已变化，请重新校验预览");
    return;
  }
  generating.value = true;
  try {
    const blob = await generateCertificateImportSql(selectedFile.value, configForm.value);
    const sqlText = await blob.text();
    const errorMessage = parseErrorMessage(sqlText, blob.type);
    if (errorMessage) {
      proxy.$modal.msgError(errorMessage);
      return;
    }
    const sqlBlob = new Blob([sqlText], { type: "text/plain;charset=utf-8" });
    saveAs(sqlBlob, buildDownloadFileName(selectedFile.value.name));
    proxy.$modal.msgSuccess("SQL 已生成并下载");
  } catch (error) {
    console.error("生成证书导入SQL失败", error);
  } finally {
    generating.value = false;
  }
}

function invalidatePreview() {
  previewResult.value = null;
  previewSignature.value = "";
}

function buildPreviewSignature() {
  const file = selectedFile.value;
  return [
    file?.name || "",
    file?.size || 0,
    file?.lastModified || 0,
    configForm.value.competitionSeriesId || "",
    configForm.value.certificateType || "",
    configForm.value.issuanceDate || "",
  ].join("|");
}

function parseErrorMessage(text, contentType) {
  const trimmed = text.trim();
  if (!contentType?.includes("application/json") && !trimmed.startsWith("{")) {
    return "";
  }
  try {
    const data = JSON.parse(trimmed);
    return data.msg || data.message || "生成 SQL 失败";
  } catch (_) {
    return "生成 SQL 失败，请检查 Excel 内容";
  }
}

function buildDownloadFileName(originalName) {
  const baseName = originalName.replace(/\.(xlsx|xls)$/i, "").replace(/[^\p{L}\p{N}_-]/gu, "_") || "证书导入";
  const now = new Date();
  const pad = (value) => String(value).padStart(2, "0");
  const timestamp = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`;
  return `${baseName}_证书导入_${timestamp}.sql`;
}
</script>

<style scoped lang="scss">
.certificate-import-page {
  max-width: 1180px;
  margin: 0 auto;
}

.page-alert,
.config-card,
.upload-card,
.result-card {
  margin-bottom: 16px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.config-grid {
  display: flex;
  flex-wrap: wrap;
  column-gap: 28px;
}

.selected-competition {
  margin-top: 4px;
}

.header-label {
  margin-bottom: 10px;
  color: #303133;
  font-weight: 600;
}

.header-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.minor-tip,
.privacy-tip,
.hidden-warning-tip {
  color: #909399;
  font-size: 13px;
}

.minor-tip {
  margin-top: 12px;
}

.certificate-uploader {
  margin-top: 18px;
}

.rule-alert {
  margin-top: 18px;
}

.rule-content {
  line-height: 1.8;
}

.action-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(160px, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.summary-item {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 18px 20px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #f8f9fb;
  color: #606266;
}

.summary-item strong {
  color: #303133;
  font-size: 28px;
}

.summary-item.success {
  border-color: #b3e19d;
  background: #f0f9eb;
}

.summary-item.warning {
  border-color: #f3d19e;
  background: #fdf6ec;
}

.preview-warning {
  margin-bottom: 14px;
}

.hidden-warning-tip {
  margin-top: 10px;
  text-align: right;
}

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
