
<template>
  <div class="app-container">
    <ul class="grid" v-if="dataStatus == 1">
      <div class="grid-item" v-for="item in processList" :key="item.processKey">
        <el-card>
          <template #header>
            <div class="card-header">
              <span class="process-name">{{ item.competitionName }}</span>
              <dict-tag
                :options="competition_status"
                :value="item.checkStatus"
              />
            </div>
          </template>
          <el-scrollbar height="50px">
            <div class="content">
              <dict-tag
                :options="competition_type"
                :value="item.competitionType"
              />
              <el-link
                type="primary"
                :underline="false"
                @click="handleStart(item)"
              >
                >> 评分
              </el-link>
            </div>
          </el-scrollbar>
        </el-card>
      </div>
    </ul>
    <empty v-if="dataStatus == 2" description="暂无上线流程"></empty>
  </div>
</template>
<script setup name="ScoreIndex">
import { listCompetition } from "@/api/tournament/competition";
const router = useRouter();

import { useDict } from "@/utils/dict";
// 字典数据
const {
  competition_status,
  competition_type,
  join_type,
  class_request,
  score_way,
  works_submit_way,
  professional_requirements,
  file_format_restrictions,
  awards_name,
} = useDict(
  "competition_status",
  "awards_name",
  "competition_type",
  "join_type",
  "class_request",
  "score_way",
  "professional_requirements",
  "works_submit_way",
  "file_format_restrictions"
);
const loading = ref(true); // 遮罩层
const processList = ref([]); // 流程定义表格数据
const total = ref(0); // 总条数
const dataStatus = ref(0);

// 查询参数
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 100,
    checkStatus: 7,
  },
});

const { queryParams } = toRefs(data);

/** 查询列表 */
const getList = () => {
  loading.value = true;
  listCompetition(queryParams.value).then((response) => {
    processList.value = response.rows;
    total.value = response.total;
    if (total.value > 0) {
      dataStatus.value = 1;
    } else {
      dataStatus.value = 2;
    }
    loading.value = false;
  });
};

/** 发起流程 */
function handleStart(item) {
  router.push({
    path: "/tournament/score-data/Selectevent",
    query: {
      competitionSeriesId: item.competitionSeriesId,
    },
  });
}

getList();
</script>
<style lang="scss" scoped>
.grid {
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-gap: 20px;
  :deep(.el-card__header) {
    background: #0062b1;
    color: #ffffff;
  }
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    .process-name {
      font-weight: bold;
    }
  }
  .content {
    margin-top: 5px;
    font-size: 14px;
    text-align: right;
    display: flex;
    justify-content: space-between;
  }
}
</style>