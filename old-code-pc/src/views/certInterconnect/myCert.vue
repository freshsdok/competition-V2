<template>
  <div class="global-page">
    <div class="container-custom font-sans pb-[60px]">
      <Breadcrumbar />
      <div class="bg-[#fff] pb-[65px]">
        <el-tabs v-model="activeName">
          <el-tab-pane label="我的证书" name="我的证书" />
        </el-tabs>
        <!-- 搜索条件 -->
        <div class="pl-[15px] pr-[15px]">
          <div v-if="guidedSummary.certificateCount > 0" class="guided-download-card">
            <div>
              <div class="guided-download-title">团队报名负责人证书打包下载</div>
              <div class="guided-download-desc">
                当前账号负责 {{ guidedSummary.teamCount }} 个获证团队，
                共 {{ guidedSummary.certificateCount }} 个不重复学生证书编号。
              </div>
              <div v-if="packageStatus" class="guided-download-status">
                {{ packageStatus }}
              </div>
            </div>
            <el-button
              type="primary"
              :loading="pictureListLoading"
              :disabled="!guidedSummary.downloadable || pictureListLoading || packageLoading"
              @click="openGuidedCertificateDialog"
            >
              打包下载我负责团队的证书
            </el-button>
          </div>
          <el-form :model="queryParams" :inline="true" class="demo-form-inline">
            <el-form-item label="关键字搜索" prop="keyWords">
              <el-input v-model.trim="queryParams.keyWords" placeholder="请输入关键字" />
            </el-form-item>
            <!-- <el-form-item label="时间阶段">
              <el-date-picker v-model="dateRange" style="width: 300px" value-format="YYYY-MM-DD" type="daterange"
                range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
            </el-form-item> -->
            <el-form-item>
              <el-button type="primary" @click="handleQuery">查询</el-button>
              <el-button type="primary" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
          <!-- 列表数据 -->
          <el-table :data="tableData" class="w-[100%]" border v-loading="loading" empty-text="暂无证书信息"
            :header-cell-style="{ background: '#F9FAFB', color: '#64666A' }">
            <el-table-column prop="certName" label="证书名称" min-width="300" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.certName || '-' }}
              </template>
            </el-table-column>
            <!-- <el-table-column prop="issuanceDate" label="颁发时间" width="120" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.issuanceDate || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="certStatus" label="证书状态" width="90" show-overflow-tooltip>
              <template #default="{ row }">
                <span :class="{
                      'text-[#409EFF]': row.certStatus == '0'}">
                  {{ cert_status[row.certStatus]?.label || '-' }}
                </span>
              </template>
            </el-table-column> -->
            <el-table-column prop="orgName" label="颁发机构" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.orgName || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="player" label="参赛选手" width="130" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.player || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="guideTeacher" label="指导教师" width="130" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.guideTeacher || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="certCode" label="证书编号" width="170" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.certCode || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="certUrl" label="证书查询地址" width="110" show-overflow-tooltip>
              <template #default="{ row }">
                <a :href="row.certUrl" v-if="row.certUrl" class="text-[#409EFF] cursor-pointer" target="_blank">查询地址</a>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <!-- 分页组件 -->
          <pagination class="mt-[20px]"  v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize" @pagination="getList" />
        </div>


      </div>
    </div>

    <el-dialog
      v-model="packageDialogVisible"
      title="我负责团队的学生证书"
      width="92%"
      top="6vh"
      append-to-body
      :close-on-click-modal="!packageLoading"
      :close-on-press-escape="!packageLoading"
    >
      <div class="certificate-dialog-summary">
        共 {{ guidedCertificateRows.length }} 个证书编号，
        可下载 {{ downloadableCertificateList.length }} 张，
        当前筛选 {{ filteredGuidedCertificateRows.length }} 条，
        已选择 {{ selectedCertificateList.length }} 张。
      </div>
      <el-alert
        v-if="missingCertCodeList.length > 0"
        class="mb-[12px]"
        type="warning"
        :closable="false"
        show-icon
        :title="`${missingCertCodeList.length} 个证书编号暂未获取到图片，不能勾选下载`"
      />
      <el-form :model="certificateFilter" :inline="true" class="certificate-filter-form">
        <el-form-item label="姓名/证书编号">
          <el-input
            v-model.trim="certificateFilter.keyword"
            clearable
            placeholder="请输入姓名或证书编号"
            style="width: 190px"
          />
        </el-form-item>
        <el-form-item label="赛事名称">
          <el-select
            v-model="certificateFilter.contestName"
            clearable
            filterable
            placeholder="全部赛事"
            style="width: 260px"
          >
            <el-option
              v-for="item in contestNameOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="届数">
          <el-select
            v-model="certificateFilter.session"
            clearable
            placeholder="全部"
            style="width: 100px"
          >
            <el-option
              v-for="item in sessionOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="赛区">
          <el-select
            v-model="certificateFilter.contestArea"
            clearable
            filterable
            placeholder="全部赛区"
            style="width: 150px"
          >
            <el-option
              v-for="item in contestAreaOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="年份">
          <el-select
            v-model="certificateFilter.runingNumYear"
            clearable
            placeholder="全部"
            style="width: 110px"
          >
            <el-option
              v-for="item in runingNumYearOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetCertificateFilter">重置筛选</el-button>
        </el-form-item>
      </el-form>
      <el-table
        ref="guidedCertificateTableRef"
        v-loading="pictureListLoading"
        :data="filteredGuidedCertificateRows"
        height="420"
        border
        row-key="certCode"
        empty-text="暂无可展示的证书信息"
        :header-cell-style="{ background: '#F9FAFB', color: '#64666A' }"
        @selection-change="handleGuidedCertificateSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
          align="center"
          reserve-selection
          :selectable="isCertificateSelectable"
        />
        <el-table-column type="index" label="序号" width="65" align="center" />
        <el-table-column prop="certCode" label="证书编号" min-width="200" />
        <el-table-column prop="name" label="获证人" min-width="100">
          <template #default="{ row }">
            {{ row.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="contestName" label="赛事名称" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.contestName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="session" label="届数" width="75" align="center">
          <template #default="{ row }">
            {{ row.session || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="contestArea" label="赛区" min-width="120">
          <template #default="{ row }">
            {{ row.contestArea || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="runingNumYear" label="年份" width="80" align="center">
          <template #default="{ row }">
            {{ row.runingNumYear || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="获取状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.certPicture" type="success">可下载</el-tag>
            <el-tag v-else type="warning">暂无图片</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="证书图片" width="90" align="center">
          <template #default="{ row }">
            <el-link
              v-if="row.certPicture"
              type="primary"
              :href="row.certPicture"
              target="_blank"
            >
              查看
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="packageStatus" class="certificate-dialog-status">
        {{ packageStatus }}
      </div>
      <template #footer>
        <el-button :disabled="packageLoading" @click="packageDialogVisible = false">
          关闭
        </el-button>
        <el-button
          type="primary"
          plain
          :loading="packageLoading"
          :disabled="selectedCertificateList.length === 0"
          @click="downloadSelectedCertificates"
        >
          导出选中（{{ selectedCertificateList.length }}）
        </el-button>
        <el-button
          type="primary"
          :loading="packageLoading"
          :disabled="downloadableCertificateList.length === 0"
          @click="downloadAllCertificates"
        >
          全部导出（{{ downloadableCertificateList.length }}）
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
  // ********** 组件 ***********
  import Breadcrumbar from "@/components/breadcrumbar.vue";
  import {
    getCertInterconnectApplyDetailNoAuthList,
    getGuidedCertificatePictures,
    getGuidedCertificateSummary,
  } from "@/api/certInterconnect";
  import { useDict } from "@/utils/dict";
  import { ElMessage } from "element-plus";
  import { saveAs } from "file-saver";
  import JSZip from "jszip";
  const { cert_status } = useDict("cert_status");

  // ********** 初始化 **********
  const activeName = ref('我的证书') // 当前激活的标签页
  const tableData = ref([]) // 列表数据
  const total = ref(0) // 总条数
  const loading = ref(false) // 加载状态
  const dateRange = ref([]); // 时间范围
  const pictureListLoading = ref(false)
  const packageLoading = ref(false)
  const packageStatus = ref('')
  const packageDialogVisible = ref(false)
  const guidedCertificateTableRef = ref()
  const guidedCertificateRows = ref([])
  const downloadableCertificateList = ref([])
  const selectedCertificateList = ref([])
  const missingCertCodeList = ref([])
  const certificateFilter = ref({
    keyword: '',
    contestName: '',
    session: '',
    contestArea: '',
    runingNumYear: '',
  })
  const guidedSummary = ref({
    teamCount: 0,
    certificateCount: 0,
    downloadable: false,
  })
  const queryParams = ref({
    pageNum: 1,
    pageSize: 10,
    keyWords: '',
    issuanceStartTime: '',
    issuanceEndTime: '',
  }) // 查询参数

  //  ********** 业务 **********
  // 搜索
  const handleQuery = () => {
    queryParams.value.pageNum = 1;
    getList()
  }

  // 重置
  const handleReset = () => {
    queryParams.value.keyWords = ''
    queryParams.value.pageNum = 1
    queryParams.value.issuanceStartTime = ''
    queryParams.value.issuanceEndTime = ''
    dateRange.value = []
    getList()
  }
  // 获取列表数据
  const getList = async () => {
    loading.value = true
    if (dateRange.value.length === 2) {
      queryParams.value.issuanceStartTime = dateRange.value[0]
      queryParams.value.issuanceEndTime = dateRange.value[1]
    } else {
      queryParams.value.issuanceStartTime = ''
      queryParams.value.issuanceEndTime = ''
    }
    try {
      const {rows, total:count} = await getCertInterconnectApplyDetailNoAuthList(queryParams.value)
      tableData.value = rows
      total.value = count
    } catch (error) {
      console.error('获取列表数据失败:', error)
    } finally {
      loading.value = false
    }
  }

  const loadGuidedSummary = async () => {
    try {
      const response = await getGuidedCertificateSummary()
      guidedSummary.value = {
        ...guidedSummary.value,
        ...(response?.data || {}),
      }
    } catch (error) {
      console.error('获取负责人证书统计失败:', error)
    }
  }

  const buildZipFileName = () => {
    const now = new Date()
    const pad = (value) => String(value).padStart(2, '0')
    const timestamp = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}`
      + `${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`
    return `学生获奖证书_${timestamp}.zip`
  }

  const appendPicturesToZip = async (zip, certPictureList) => {
    let nextIndex = 0
    let completed = 0
    const failedCertCodes = []
    const workerCount = Math.min(4, certPictureList.length)

    const worker = async () => {
      while (nextIndex < certPictureList.length) {
        const item = certPictureList[nextIndex++]
        try {
          const response = await fetch(item.certPicture, {
            method: 'GET',
            credentials: 'omit',
          })
          if (!response.ok) {
            throw new Error(`HTTP ${response.status}`)
          }
          zip.file(item.fileName || `${item.certCode}.jpg`, await response.blob())
        } catch (error) {
          console.error(`下载证书图片失败: ${item.certCode}`, error)
          failedCertCodes.push(item.certCode)
        } finally {
          completed += 1
          packageStatus.value = `正在下载证书图片（${completed}/${certPictureList.length}）...`
        }
      }
    }

    await Promise.all(Array.from({ length: workerCount }, () => worker()))
    return failedCertCodes
  }

  const isCertificateSelectable = (row) => Boolean(row.certPicture)

  const buildCertificateFilterOptions = (fieldName) => [...new Set(
    downloadableCertificateList.value
      .map((item) => item[fieldName])
      .filter((value) => value !== null && value !== undefined && value !== ''),
  )].sort((left, right) => String(left).localeCompare(String(right), 'zh-CN', { numeric: true }))

  const contestNameOptions = computed(() => buildCertificateFilterOptions('contestName'))
  const sessionOptions = computed(() => buildCertificateFilterOptions('session'))
  const contestAreaOptions = computed(() => buildCertificateFilterOptions('contestArea'))
  const runingNumYearOptions = computed(() => (
    buildCertificateFilterOptions('runingNumYear').sort((left, right) => Number(right) - Number(left))
  ))

  const filteredGuidedCertificateRows = computed(() => {
    const filters = certificateFilter.value
    const keyword = filters.keyword.toLowerCase()
    return guidedCertificateRows.value.filter((item) => {
      const matchesKeyword = !keyword
        || String(item.name || '').toLowerCase().includes(keyword)
        || String(item.certCode || '').toLowerCase().includes(keyword)
      const matchesContestName = !filters.contestName
        || item.contestName === filters.contestName
      const matchesSession = !filters.session || item.session === filters.session
      const matchesContestArea = !filters.contestArea
        || item.contestArea === filters.contestArea
      const matchesYear = filters.runingNumYear === ''
        || String(item.runingNumYear) === String(filters.runingNumYear)
      return matchesKeyword
        && matchesContestName
        && matchesSession
        && matchesContestArea
        && matchesYear
    })
  })

  const resetCertificateFilter = () => {
    certificateFilter.value = {
      keyword: '',
      contestName: '',
      session: '',
      contestArea: '',
      runingNumYear: '',
    }
  }

  const handleGuidedCertificateSelectionChange = (selection) => {
    selectedCertificateList.value = selection
  }

  const openGuidedCertificateDialog = async () => {
    if (!guidedSummary.value.downloadable || pictureListLoading.value || packageLoading.value) {
      return
    }

    packageDialogVisible.value = true
    packageStatus.value = ''
    selectedCertificateList.value = []
    guidedCertificateRows.value = []
    downloadableCertificateList.value = []
    missingCertCodeList.value = []
    resetCertificateFilter()
    pictureListLoading.value = true
    try {
      const response = await getGuidedCertificatePictures()
      const result = response?.data || {}
      const certPictureList = Array.isArray(result.certPictureList)
        ? result.certPictureList
        : []
      const unavailableCertCodes = Array.isArray(result.missingCertCodeList)
        ? result.missingCertCodeList
        : []

      downloadableCertificateList.value = certPictureList
      missingCertCodeList.value = unavailableCertCodes
      guidedCertificateRows.value = [
        ...certPictureList,
        ...unavailableCertCodes.map((certCode) => ({
          certCode,
          certPicture: '',
          fileName: '',
        })),
      ]

      await nextTick()
      guidedCertificateTableRef.value?.toggleAllSelection()

      if (certPictureList.length === 0 && unavailableCertCodes.length > 0) {
        ElMessage.warning('证书查询平台暂未返回可下载的证书图片')
      }
    } catch (error) {
      ElMessage.error(error?.message || '获取证书信息失败，请稍后重试')
      console.error('获取负责人证书信息失败:', error)
    } finally {
      pictureListLoading.value = false
    }
  }

  const downloadCertificatePackage = async (certPictureList, unavailableCertCodes = []) => {
    if (!Array.isArray(certPictureList) || certPictureList.length === 0 || packageLoading.value) {
      return
    }

    packageLoading.value = true
    packageStatus.value = '正在准备下载证书图片...'
    try {
      const zip = new JSZip()
      const imageDownloadFailures = await appendPicturesToZip(zip, certPictureList)
      const failedCertCodes = [...new Set([
        ...unavailableCertCodes,
        ...imageDownloadFailures,
      ])]
      const failureCount = failedCertCodes.length
      const successCount = certPictureList.length - imageDownloadFailures.length
      if (successCount === 0) {
        throw new Error('证书图片下载失败，请稍后重试')
      }
      if (failureCount > 0) {
        zip.file(
          '未下载证书编号.txt',
          `以下证书暂未获取成功，请稍后重试：\r\n${failedCertCodes.join('\r\n')}\r\n`,
        )
      }

      packageStatus.value = '正在生成证书压缩包...'
      const zipBlob = await zip.generateAsync(
        { type: 'blob', compression: 'STORE' },
        ({ percent }) => {
          packageStatus.value = `正在生成证书压缩包（${Math.round(percent)}%）...`
        },
      )
      saveAs(zipBlob, buildZipFileName())

      if (failureCount > 0) {
        packageStatus.value = `已打包 ${successCount} 张，另有 ${failureCount} 张暂未获取成功。`
        ElMessage.warning(`已下载 ${successCount} 张证书，另有 ${failureCount} 张暂未获取成功`)
      } else {
        packageStatus.value = `已成功打包下载 ${successCount} 张证书。`
        ElMessage.success('证书压缩包已生成')
      }
    } catch (error) {
      packageStatus.value = ''
      ElMessage.error(error?.message || '证书打包下载失败，请稍后重试')
      console.error('证书打包下载失败:', error)
    } finally {
      packageLoading.value = false
    }
  }

  const downloadSelectedCertificates = () => {
    downloadCertificatePackage([...selectedCertificateList.value])
  }

  const downloadAllCertificates = () => {
    downloadCertificatePackage(
      [...downloadableCertificateList.value],
      [...missingCertCodeList.value],
    )
  }

  onMounted(() => {
    getList()
    loadGuidedSummary()
  })
</script>

<style lang="scss" scoped>
  :deep(.el-tabs__nav .el-tabs__item) {
    margin: 15px;
    font-size: 20px;
  }

  /* 文本溢出省略号样式 */
  .truncate-text {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: bottom;
  }

  /* vxe-table tooltip 样式优化 */
  :deep(.vxe-table--tooltip-wrapper) {
    max-width: 400px;
    word-break: break-all;
  }

  .guided-download-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
    margin: 0 0 20px;
    padding: 18px 20px;
    border: 1px solid #d9ecff;
    border-radius: 4px;
    background: #f5faff;
  }

  .guided-download-title {
    margin-bottom: 8px;
    color: #303133;
    font-size: 16px;
    font-weight: 600;
  }

  .guided-download-desc {
    color: #606266;
    line-height: 22px;
  }

  .guided-download-status {
    margin-top: 6px;
    color: #409eff;
    line-height: 20px;
  }

  .certificate-dialog-summary {
    margin-bottom: 12px;
    color: #606266;
    line-height: 22px;
  }

  .certificate-filter-form {
    margin-bottom: 4px;
  }

  .certificate-dialog-status {
    margin-top: 12px;
    color: #409eff;
    line-height: 22px;
  }

  @media (max-width: 768px) {
    .guided-download-card {
      align-items: flex-start;
      flex-direction: column;
    }
  }
</style>
