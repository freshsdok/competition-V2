<template>
  <div class="app-container checkin-overview-page">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="!isMobile"
      label-width="96px"
      class="overview-filter"
    >
      <el-form-item label="关联赛事" prop="competitionSeriesId">
        <el-select v-model="queryParams.competitionSeriesId" placeholder="请选择" clearable filterable>
          <el-option
            v-for="item in competitionOptions"
            :key="item.competitionSeriesId"
            :label="item.competitionName"
            :value="item.competitionSeriesId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间" prop="startRange">
        <el-date-picker
          v-model="startRange"
          type="datetimerange"
          start-placeholder="开始"
          end-placeholder="结束"
          value-format="YYYY-MM-DD HH:mm:ss"
          :shortcuts="dateShortcuts"
        />
      </el-form-item>
      <el-form-item label="签到地点" prop="checkinLocation">
        <el-input v-model.trim="queryParams.checkinLocation" placeholder="请输入" clearable />
      </el-form-item>
      <el-form-item label="签到状态" prop="checkinStatus">
        <el-select v-model="queryParams.checkinStatus" placeholder="请选择">
          <el-option v-for="item in checkinStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警等级" prop="warningLevel">
        <el-select v-model="queryParams.warningLevel" placeholder="请选择" clearable>
          <el-option v-for="item in warningOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item class="filter-actions">
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-switch v-model="autoRefresh" active-text="自动刷新" />
        <el-button icon="RefreshRight" :loading="loading" @click="refreshAll">刷新</el-button>
      </el-form-item>
    </el-form>

    <div class="update-line">
      <span>最后更新时间：{{ lastUpdateTime || '-' }}</span>
    </div>

    <section class="stat-grid">
      <div v-for="item in statCards" :key="item.key" class="stat-card">
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">{{ item.value }}</div>
      </div>
    </section>

    <section class="chart-grid">
      <div class="chart-panel">
        <div class="panel-title">整体签到</div>
        <div ref="donutChartRef" class="chart-box"></div>
      </div>
      <div class="chart-panel">
        <div class="panel-title">低签到率赛场排行</div>
        <div ref="rankChartRef" class="chart-box"></div>
      </div>
      <div class="chart-panel chart-panel-wide">
        <div class="panel-title">按比赛开始时间统计</div>
        <div ref="timeChartRef" class="chart-box"></div>
      </div>
    </section>

    <section class="schedule-section" v-loading="loading">
      <div class="section-title">赛场签到卡片</div>
      <div class="schedule-grid">
        <article
          v-for="item in scheduleList"
          :key="item.scheduleId"
          class="schedule-card"
          @click="openDetail(item)"
        >
          <div class="card-head">
            <div>
              <h3>{{ item.scheduleName || '未命名赛场' }}</h3>
              <p>{{ item.competitionName || '-' }}</p>
            </div>
            <el-tag :type="warningTagType(item.warningLevel)">{{ warningLabel(item.warningLevel) }}</el-tag>
          </div>
          <div class="meta-list">
            <span>开始：{{ formatTime(item.competitionStartTime) }}</span>
            <span>地点：{{ item.checkinLocation || '-' }}</span>
            <span>最后签到：{{ formatTime(item.lastCheckinTime) }}</span>
          </div>
          <el-progress :percentage="rateNumber(item.checkinRate)" :stroke-width="10" />
          <div class="count-row">
            <span>应到 {{ item.totalPersonCount || 0 }}</span>
            <span>已到 {{ item.signedPersonCount || 0 }}</span>
            <span>未到 {{ item.unsignedPersonCount || 0 }}</span>
            <span>队员 {{ item.memberSignedCount || 0 }}/{{ item.memberTotalCount || 0 }}</span>
          </div>
          <div class="team-row">
            <span>团队 {{ item.teamTotalCount || 0 }}</span>
            <span>全员 {{ item.completedTeamCount || 0 }}</span>
            <span>部分 {{ item.partialTeamCount || 0 }}</span>
            <span>无人 {{ item.unsignedTeamCount || 0 }}</span>
          </div>
          <p v-if="item.warningMessage" class="warning-message">{{ item.warningMessage }}</p>
        </article>
      </div>
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getScheduleList"
      />
    </section>

    <el-drawer
      v-model="detailVisible"
      :title="detailData?.scheduleName || '赛场签到详情'"
      size="70%"
      class="checkin-detail-drawer"
    >
      <template v-if="detailData">
        <div class="detail-summary">
          <div>比赛时间：{{ formatRange(detailData.competitionStartTime, detailData.competitionEndTime) }}</div>
          <div>签到地点：{{ detailData.checkinLocation || '-' }}</div>
          <div>签到率：{{ detailData.checkinRate || 0 }}%</div>
        </div>
        <el-tabs v-model="detailTab">
          <el-tab-pane label="团队" name="teams">
            <div class="team-detail-list">
              <div v-for="team in detailData.teams || []" :key="team.teamCode" class="team-detail-card">
                <div>
                  <strong>{{ team.teamName || '未分组' }}</strong>
                  <span>{{ team.signedPersonCount || 0 }}/{{ team.totalPersonCount || 0 }}</span>
                </div>
                <el-tag>{{ teamStatusLabel(team.teamStatus) }}</el-tag>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="人员" name="persons">
            <el-form :model="personQuery" :inline="!isMobile" class="person-filter">
              <el-form-item label="团队">
                <el-select v-model="personQuery.teamCode" placeholder="全部" clearable>
                  <el-option
                    v-for="team in detailData.teams || []"
                    :key="team.teamCode"
                    :label="team.teamName || '未分组'"
                    :value="team.teamCode"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="personQuery.checkinStatus" placeholder="全部" clearable>
                  <el-option label="已签到" value="SIGNED" />
                  <el-option label="未签到" value="UNSIGNED" />
                </el-select>
              </el-form-item>
              <el-form-item label="关键词">
                <el-input v-model.trim="personQuery.keyword" placeholder="姓名/团队/证件" clearable />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="loadPersons">查询</el-button>
              </el-form-item>
            </el-form>
            <el-table v-if="!isMobile" v-loading="personLoading" :data="personList" stripe>
              <el-table-column label="团队" prop="teamName" min-width="160" show-overflow-tooltip />
              <el-table-column label="姓名" prop="personName" min-width="120" show-overflow-tooltip />
              <el-table-column label="角色" prop="roleName" width="90" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.checkinStatus === 'SIGNED' ? 'success' : 'info'">
                    {{ row.checkinStatus === 'SIGNED' ? '已签到' : '未签到' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="签到时间" prop="checkinTime" min-width="170" />
            </el-table>
            <div v-else class="person-list" v-loading="personLoading">
              <div v-for="person in personList" :key="person.targetId" class="person-card">
                <div>
                  <strong>{{ person.personName || '-' }}</strong>
                  <span>{{ person.teamName || '未分组' }}</span>
                </div>
                <el-tag :type="person.checkinStatus === 'SIGNED' ? 'success' : 'info'">
                  {{ person.checkinStatus === 'SIGNED' ? '已签到' : '未签到' }}
                </el-tag>
              </div>
            </div>
            <pagination
              v-show="personTotal > 0"
              :total="personTotal"
              v-model:page="personQuery.pageNum"
              v-model:limit="personQuery.pageSize"
              @pagination="loadPersons"
            />
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="CompetitionCheckinOverview">
import { getSelectCompetitionList } from '@/api/certInterconnect/certConfig'
import {
  getCheckinOverviewSchedule,
  getCheckinOverviewStatistics,
  listCheckinOverviewPersons,
  listCheckinOverviewSchedules
} from '@/api/tournament/checkinOverview'
import * as echarts from 'echarts'
import { useDocumentVisibility, useIntervalFn, useWindowSize } from '@vueuse/core'

const { proxy } = getCurrentInstance()
const { width } = useWindowSize()
const visibility = useDocumentVisibility()

const isMobile = computed(() => width.value <= 768)
const loading = ref(false)
const personLoading = ref(false)
const autoRefresh = ref(true)
const lastUpdateTime = ref('')
const competitionOptions = ref([])
const statistics = ref({})
const scheduleList = ref([])
const total = ref(0)
const startRange = ref([])
const detailVisible = ref(false)
const detailData = ref(null)
const detailTab = ref('teams')
const personList = ref([])
const personTotal = ref(0)
const donutChartRef = ref(null)
const rankChartRef = ref(null)
const timeChartRef = ref(null)
let donutChart
let rankChart
let timeChart

const queryParams = reactive({
  pageNum: 1,
  pageSize: 12,
  competitionSeriesId: undefined,
  checkinLocation: '',
  checkinStatus: 'ALL',
  warningLevel: '',
  startTimeBegin: '',
  startTimeEnd: ''
})

const personQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  teamCode: '',
  checkinStatus: '',
  keyword: ''
})

const checkinStatusOptions = [
  { label: '全部', value: 'ALL' },
  { label: '已全部签到', value: 'COMPLETED' },
  { label: '部分签到', value: 'PARTIAL' },
  { label: '尚未签到', value: 'NOT_STARTED' },
  { label: '存在预警', value: 'WARNING' }
]

const warningOptions = [
  { label: '正常', value: 'NORMAL' },
  { label: '敬请关注', value: 'YELLOW' },
  { label: '敬请关注', value: 'ORANGE' },
  { label: '敬请关注', value: 'RED' }
]

const dateShortcuts = [
  {
    text: '今天',
    value: () => {
      const start = new Date()
      start.setHours(0, 0, 0, 0)
      const end = new Date()
      end.setHours(23, 59, 59, 999)
      return [start, end]
    }
  }
]

const statCards = computed(() => [
  { key: 'total', label: '应签到人数', value: statistics.value.totalPersonCount || 0 },
  { key: 'signed', label: '已签到人数', value: statistics.value.signedPersonCount || 0 },
  { key: 'unsigned', label: '未签到人数', value: statistics.value.unsignedPersonCount || 0 },
  { key: 'rate', label: '整体签到率', value: `${statistics.value.checkinRate || 0}%` },
  { key: 'schedule', label: '赛场总数', value: statistics.value.scheduleCount || 0 },
  { key: 'warning', label: '预警赛场数', value: statistics.value.warningScheduleCount || 0 }
])

function normalizeQuery() {
  queryParams.startTimeBegin = startRange.value?.[0] || ''
  queryParams.startTimeEnd = startRange.value?.[1] || ''
  return { ...queryParams }
}

async function refreshAll() {
  if (loading.value) return
  loading.value = true
  try {
    const query = normalizeQuery()
    const [statRes, listRes] = await Promise.all([
      getCheckinOverviewStatistics(query),
      listCheckinOverviewSchedules(query)
    ])
    statistics.value = statRes.data || {}
    scheduleList.value = listRes.rows || []
    total.value = listRes.total || 0
    lastUpdateTime.value = proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}')
    nextTick(renderCharts)
  } finally {
    loading.value = false
  }
}

function getScheduleList() {
  refreshAll()
}

function handleQuery() {
  queryParams.pageNum = 1
  refreshAll()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  startRange.value = []
  queryParams.checkinStatus = 'ALL'
  queryParams.warningLevel = ''
  queryParams.pageNum = 1
  refreshAll()
}

async function openDetail(row) {
  detailVisible.value = true
  detailTab.value = 'teams'
  personList.value = []
  personTotal.value = 0
  personQuery.pageNum = 1
  personQuery.teamCode = ''
  personQuery.checkinStatus = ''
  personQuery.keyword = ''
  const res = await getCheckinOverviewSchedule(row.scheduleId)
  detailData.value = res.data
}

async function loadPersons() {
  if (!detailData.value?.scheduleId) return
  personLoading.value = true
  try {
    const res = await listCheckinOverviewPersons(detailData.value.scheduleId, personQuery)
    personList.value = res.rows || []
    personTotal.value = res.total || 0
  } finally {
    personLoading.value = false
  }
}

function renderCharts() {
  renderDonut()
  renderRank()
  renderTimeGroup()
}

function renderDonut() {
  if (!donutChartRef.value) return
  donutChart = donutChart || echarts.init(donutChartRef.value)
  const signed = statistics.value.signedPersonCount || 0
  const unsigned = statistics.value.unsignedPersonCount || 0
  donutChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['58%', '78%'],
      center: ['50%', '52%'],
      label: { formatter: '{b}: {c}' },
      data: [
        { name: '已签到', value: signed },
        { name: '未签到', value: unsigned }
      ]
    }],
    graphic: [{
      type: 'text',
      left: 'center',
      top: '45%',
      style: {
        text: `${statistics.value.checkinRate || 0}%\n${signed}/${statistics.value.totalPersonCount || 0}`,
        textAlign: 'center',
        fill: '#303133',
        fontSize: 18,
        lineHeight: 26
      }
    }]
  })
}

function renderRank() {
  if (!rankChartRef.value) return
  rankChart = rankChart || echarts.init(rankChartRef.value)
  const items = statistics.value.lowRateRank || []
  rankChart.setOption({
    grid: { left: 80, right: 24, top: 20, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    yAxis: { type: 'category', data: items.map(item => item.scheduleName || '-') },
    series: [{
      type: 'bar',
      data: items.map(item => Number(item.checkinRate || 0)),
      label: { show: true, position: 'right', formatter: '{c}%' }
    }]
  })
}

function renderTimeGroup() {
  if (!timeChartRef.value) return
  timeChart = timeChart || echarts.init(timeChartRef.value)
  const items = statistics.value.startTimeGroups || []
  timeChart.setOption({
    grid: { left: 40, right: 24, top: 28, bottom: 44 },
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    xAxis: { type: 'category', data: items.map(item => formatShortTime(item.competitionStartTime)) },
    yAxis: { type: 'value' },
    series: [
      { name: '应签到', type: 'bar', data: items.map(item => item.totalPersonCount || 0) },
      { name: '已签到', type: 'bar', data: items.map(item => item.signedPersonCount || 0) },
      { name: '未签到', type: 'bar', data: items.map(item => item.unsignedPersonCount || 0) }
    ]
  })
}

function resizeCharts() {
  donutChart?.resize()
  rankChart?.resize()
  timeChart?.resize()
}

function formatTime(value) {
  return value ? proxy.parseTime(value, '{m}-{d} {h}:{i}') : '-'
}

function formatShortTime(value) {
  return value ? proxy.parseTime(value, '{m}-{d} {h}:{i}') : '-'
}

function formatRange(start, end) {
  return `${formatTime(start)} - ${formatTime(end)}`
}

function rateNumber(value) {
  return Math.max(0, Math.min(100, Number(value || 0)))
}

function warningLabel(value) {
  return ({ RED: '敬请关注', ORANGE: '敬请关注', YELLOW: '敬请关注', NORMAL: '正常' })[value] || '正常'
}

function warningTagType(value) {
  return ({ RED: 'danger', ORANGE: 'warning', YELLOW: 'warning', NORMAL: 'success' })[value] || 'success'
}

function teamStatusLabel(value) {
  return ({ COMPLETED: '全员签到', PARTIAL: '部分签到', NOT_STARTED: '无人签到' })[value] || '无人签到'
}

function loadCompetitions() {
  getSelectCompetitionList().then(response => {
    competitionOptions.value = response.data || []
  })
}

const { pause, resume } = useIntervalFn(() => {
  if (autoRefresh.value && visibility.value === 'visible') {
    refreshAll()
  }
}, 30000)

watch(autoRefresh, value => {
  if (value) resume()
  else pause()
})

watch(visibility, value => {
  if (value === 'visible') {
    if (autoRefresh.value) resume()
    refreshAll()
  } else {
    pause()
  }
})

watch(width, () => nextTick(resizeCharts))
watch(detailTab, value => {
  if (value === 'persons' && detailData.value && personList.value.length === 0) {
    loadPersons()
  }
})

onMounted(() => {
  loadCompetitions()
  refreshAll()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  pause()
  window.removeEventListener('resize', resizeCharts)
  donutChart?.dispose()
  rankChart?.dispose()
  timeChart?.dispose()
})
</script>

<style scoped>
.checkin-overview-page {
  background: #f5f7fa;
}

.overview-filter,
.chart-panel,
.schedule-section {
  background: #fff;
  border-radius: 6px;
  padding: 16px;
}

.overview-filter :deep(.el-select),
.overview-filter :deep(.el-date-editor),
.overview-filter :deep(.el-input) {
  width: 220px;
}

.filter-actions {
  white-space: nowrap;
}

.update-line {
  color: #606266;
  font-size: 13px;
  margin: 10px 0;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.stat-card {
  background: #fff;
  border-radius: 6px;
  padding: 16px;
  min-height: 84px;
}

.stat-label {
  color: #909399;
  font-size: 13px;
}

.stat-value {
  color: #303133;
  font-size: 26px;
  font-weight: 700;
  margin-top: 8px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.chart-panel-wide {
  grid-column: span 2;
}

.panel-title,
.section-title {
  color: #303133;
  font-weight: 600;
  margin-bottom: 12px;
}

.chart-box {
  height: 300px;
}

.schedule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
}

.schedule-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 14px;
  cursor: pointer;
  background: #fff;
}

.schedule-card:hover {
  border-color: #409eff;
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.card-head h3 {
  font-size: 16px;
  margin: 0 0 4px;
}

.card-head p,
.meta-list,
.warning-message {
  color: #606266;
  margin: 0;
}

.meta-list {
  display: grid;
  gap: 4px;
  font-size: 13px;
  margin: 12px 0;
}

.count-row,
.team-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #606266;
  font-size: 13px;
  margin-top: 10px;
}

.warning-message {
  color: #e6a23c;
  margin-top: 10px;
}

.detail-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.team-detail-list {
  display: grid;
  gap: 10px;
}

.team-detail-card,
.person-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
}

.team-detail-card span,
.person-card span {
  color: #909399;
  margin-left: 8px;
}

@media (max-width: 768px) {
  .overview-filter :deep(.el-form-item) {
    display: block;
    margin-right: 0;
  }

  .overview-filter :deep(.el-select),
  .overview-filter :deep(.el-date-editor),
  .overview-filter :deep(.el-input) {
    width: 100%;
  }

  .filter-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-grid,
  .schedule-grid,
  .detail-summary {
    grid-template-columns: 1fr;
  }

  .chart-panel-wide {
    grid-column: auto;
  }

  .chart-box {
    height: 260px;
  }
}
</style>
