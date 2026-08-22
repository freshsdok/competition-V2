<template>
  <div class="my-review-task-page">

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-item">
        <span class="label">项目编号</span>
        <el-input
          v-model.trim="queryParams.processedStr"
          placeholder="请输入项目编号"
          clearable
          class="filter-input"
        />
      </div>
      <div class="filter-item">
        <span class="label">赛事</span>
        <el-input
          v-model="queryParams.competitionName"
          placeholder="搜索赛事"
          clearable
          class="filter-input"
        />
      </div>
      <div class="filter-item">
        <span class="label">赛项/组别</span>
        <el-input
          v-model="queryParams.competitionTrackName"
          placeholder="搜索赛项/组别"
          clearable
          class="filter-input"
        />
      </div>
      <div class="filter-item">
        <span class="label">审阅状态</span>
        <el-select v-model="queryParams.reviewStatus" placeholder="全部" clearable class="filter-select">
          <el-option label="全部" value="" />
          <el-option label="未审阅" value="0" />
          <el-option label="已审阅" value="1" />
        </el-select>
      </div>
      <div class="filter-item">
        <el-button type="primary" icon="Search"@click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table :data="taskList" v-loading="loading" class="review-table">
        <el-table-column label="项目编号" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip :content="row.processedId" placement="top" :show-after="500">
              {{ row.processedId }}
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="赛事" min-width="150">
          <template #default="{ row }">
            {{ row.competitionName }}
          </template>
        </el-table-column>
        <el-table-column label="赛项/组别" min-width="150">
          <template #default="{ row }">
            <span>{{ row.competitionTrackName || '-' }}</span>
            <span> / </span>
            <span>{{ row.secondLevelName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="审阅截止时间" width="160">
          <template #default="{ row }">
            {{ row.reviewEndTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="审阅状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.reviewStatus == '1' ? 'success' : 'info'" size="small">
              {{ row.reviewStatus == '1' ? '已审阅' : '未审阅' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleReview(row)" v-if="row.continueFlag">
              在线审阅
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </div>
</template>

<script setup name="MyReviewTask">
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { getMyReviewTaskList } from '@/api/tournament/reviewManage'
import Pagination from '@/components/Pagination'

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const taskList = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  processedStr: '',
  competitionName: '',
  competitionTrackName: '',
  reviewStatus: ''
})

onMounted(() => {
  getList()
})

onActivated(() => {
  getList()
})

async function getList() {
  loading.value = true
  try {
    const res = await getMyReviewTaskList(queryParams)
    taskList.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.pageNum = 1
  queryParams.processedStr = ''
  queryParams.competitionName = ''
  queryParams.competitionTrackName = ''
  queryParams.reviewStatus = ''
  getList()
}

function handleReview(row) {
  router.push({
    name: 'ReviewTaskDetail',
    query: { id: row.processedId }
  })
}
</script>

<style scoped lang="scss">
.my-review-task-page {
  padding: 20px;
  background: #ffffff;
  .page-header {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    background: #fff;
    border-radius: 4px;
    margin-bottom:20px;
    .header-icon {
      font-size: 24px;
      color: #409eff;
      margin-top: 2px;
    }

    .header-content {
      .title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
      }

      .subtitle {
        font-size: 13px;
        color: #909399;
        margin: 0;
      }
    }
  }

  .filter-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
    background: #fff;
    border-radius: 4px;

    .filter-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .label {
        font-size: 13px;
        color: #606266;
        white-space: nowrap;
      }

      .filter-input {
        width: 180px;
      }

      .filter-select {
        width: 140px;
      }
    }
  }

  .table-container {
    background: #fff;
    border-radius: 4px;
    margin-bottom: 16px;

    .project-name {
      color: #409eff;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    background: #fff;
    padding: 12px 20px;
    border-radius: 4px;
  }
}
</style>
