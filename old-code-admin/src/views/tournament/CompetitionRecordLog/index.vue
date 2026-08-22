<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="96px">
      <el-form-item label="申请人" prop="createBy">
        <el-input
          v-model.trim="queryParams.createBy"
          placeholder="请输入申请人名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="数据变动类型" prop="changeType">
        <el-select v-model="queryParams.changeType" placeholder="请选择数据变动类型" clearable style="width: 170px;">
          <el-option v-for="item in change_type" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="变更日期范围" style="width: 400px">
        <el-date-picker
           v-model="dateRange"
           value-format="YYYY-MM-DD"
           type="daterange"
           range-separator="至"
           start-placeholder="开始日期"
           end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-button type="primary"  @click="handleExportAll" v-hasPermi="['competition:competitionApply:export']">全部导出</el-button>
      <el-button type="primary"  @click="handleExport" v-hasPermi="['competition:competitionApply:export']">检索结果导出</el-button>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" 
              :data="contentList" 
              :header-cell-style="{
                fon
              }"
              row-key="id">
      <el-table-column label="变更时间" align="left" prop="changeTime" width="100" />
      <el-table-column label="申请人" align="left" prop="createBy" width="80" />
      <el-table-column label="数据变动类型" align="left" prop="changeType" width="140" >
        <template #default="scope">
           <dict-tag :options="change_type" :value="scope.row.changeType" />
        </template>
      </el-table-column>
      <el-table-column label="结果" align="left" prop="result" width="80">
        <template #default="scope">
           {{ scope.row.result || '-'}}
        </template>
      </el-table-column>
      <el-table-column label="团队名称" align="left" prop="teamName" width="160" />
      <el-table-column label="变动详情" align="left" prop="changeDetails" min-width="200">
        <template #default="scope">
          <span class="text-num">{{ scope.row.changeDetails || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column width="80" label="操作" align="center" fixed="right">
        <template #default="scope">
          <el-tooltip content="变动数据对比" placement="top">
              <el-button link type="primary" icon="View"  @click="showDetailDialog(scope.row)" v-hasPermi="['race:task:submit']" ></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
    <DetailDialog
      ref="detailDialogRef"
      v-model:visible="settingVisible"
      :detail-data="detailData"
      :payStatusDict="pay_status"
      :payMethodDict="pay_method"
      :payModeDict="pay_mode"
    />
  </div>
</template>

<script setup name="CompetitionRecordLog">
import { getCompetitionLogList } from "@/api/tournament/tournament"
import { useDict } from '@/utils/dict'
import { addDateRange } from "@/utils/ruoyi"
import { download } from '@/utils/request'
import { cloneDeep } from 'lodash-es';
import DetailDialog from "./detailDialog.vue";
const queryRef = ref(null)

// 字典数据
const {change_type} = useDict('change_type')

const contentList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10
  }
})

const { queryParams} = toRefs(data)

/** 查询组件库信息列表 */
let dateRange = ref([])
function getList() {
  loading.value = true
  let arr = cloneDeep(queryParams.value)
  let query = addDateRange(arr, dateRange.value)
  if(query?.competitionRoleNameReq){
    query.competitionRoleNameReq = query.competitionRoleNameReq.join(',')
  }
  getCompetitionLogList(query).then(response => {
    contentList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(error => {
    loading.value = false
  })
}

// 组件挂载后执行
onMounted(() => {
  getList()
})

let settingVisible = $ref(false);
let detailData = $ref({});
let detailDialogRef = $ref(null);
function showDetailDialog(row) {
  detailData = row;
  settingVisible = true;
}

/** 搜索按钮操作 */
function handleQuery() {
  contentList.value = []
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  dateRange.value = []
  handleQuery()
}

/** 导出按钮操作 */
function handleExportAll() {
  download("competition/log/export",{
    exportType:'all'
  }, `参赛信息变动日志.xlsx`)
}
function handleExport() {
  let query = cloneDeep(queryParams.value)
  query = addDateRange(query, dateRange.value)
  if(query?.pageSize){
    delete query.pageSize
  }
  if(query?.pageNum){
    delete query.pageNum
  }
  download("competition/log/export",{
    ...query,
    exportType:'filter'
  }, `参赛信息变动日志.xlsx`)
}
</script>
<style scoped lang="scss">
.text-num-wrapper {
  // 超出2行省略号
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box !important;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  .icon{
    color: #409EFF;
    cursor: pointer;
    font-size: 14px;
    margin-top: 2px;
    margin-right: 2px;
  }
  .text-num {
    margin-left: 2px;
  }
}
</style>
