<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="操作人" prop="operator">
        <el-input
          v-model.trim="queryParams.operator"
          placeholder="请输入操作人名称"
          clearable
          style="width: 160px;"
        />
      </el-form-item>
      <el-form-item label="操作日期范围" style="width: 400px">
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

    <el-table v-loading="loading" 
              :data="contentList">
      <el-table-column label="操作人" align="center" prop="operator" min-width="100" />
      <el-table-column label="操作时间" align="center" prop="operationTime" min-width="100" />
      <el-table-column label="操作IP" align="center" prop="operationIp" min-width="120" />
      <el-table-column label="验证类型" align="center" prop="verifyType" min-width="140" />
      <el-table-column label="验证结果" align="center" prop="verifyResult" min-width="100" />
      <el-table-column label="原因" align="center" prop="reason" min-width="100">
        <template #default="scope">
          {{ scope.row.reason || '-'}}
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
  </div>
</template>

<script setup name="informationRetrieval">
import { getSysSenderMessageLogList } from "@/api/tournament/tournament"
import { addDateRangeSAE } from "@/utils/ruoyi"
import { cloneDeep } from 'lodash-es';
const queryRef = ref(null)

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
  let query = addDateRangeSAE(arr, dateRange.value, 'operationTimeStart', 'operationTimeEnd')
  if(query?.competitionRoleNameReq){
    query.competitionRoleNameReq = query.competitionRoleNameReq.join(',')
  }
  getSysSenderMessageLogList(query).then(response => {
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
