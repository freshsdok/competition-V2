<template>
  <div>
    <div class="app-container">
      <div class="appleft">
        <el-form
          :model="queryParams"
          ref="queryRef"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="赛事阶段" prop="stageId">
            <el-select
              v-model="queryParams.stageId"
              placeholder="请选择赛事阶段"
              style="width: 160px"
              @change="handleQuery"
            >
              <el-option
                v-for="dict in jieduanlieb"
                :key="dict.stageId"
                :label="dict.stageName"
                :value="dict.stageId"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div v-for="(item, index) in saidao" :key="index">
          <div v-for="(x, i) in item.competitionGroupList" :key="i">
            赛道：{{ item.competitionTrackName }} 组别：{{ x.dictLabel }}
          </div>
        </div>
      </div>
      <div class="appright">
        <div class="jiangxianglieb">
          <div class="addjiangxiang">
            <img src="@/assets/images/add.png" alt="" />
          </div>
          <div
            class="jiangxiang"
            v-for="(item, index) in jiangxianglist"
            :key="index"
          >
            奖项名称：1521521
          </div>
        </div>
      </div>
    </div>
    <div>
      <div class="tables">
        <!-- 左侧：可选数据 -->
        <el-table
          ref="leftTable"
          :data="leftData"
          @selection-change="handleLeftSelectionChange"
          height="400"
          style="width: 30%"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="desc" label="描述" />
        </el-table>

        <!-- 中间操作按钮 -->
        <div class="transfer-buttons">
          <el-button
            type="primary"
            :disabled="!selectedLeft.length"
            @click="moveToRight"
            size="small"
          >
            &gt;
          </el-button>
          <el-button
            type="primary"
            :disabled="!selectedRight.length"
            @click="moveToLeft"
            size="small"
          >
            &lt;
          </el-button>
        </div>

        <!-- 右侧：已选数据 -->
        <el-table
          ref="rightTable"
          :data="rightData"
          @selection-change="handleRightSelectionChange"
          height="400"
          style="width: 30%"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="desc" label="描述" />
        </el-table>

        <el-table
          :data="allData"
          @selection-change="SelectionChange"
          height="400"
          style="width: 30%"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="desc" label="描述" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup name="PrizeConfig">
import {
  getCompetition,
  queryNowCompetitionStageConfig,
} from "@/api/tournament/competition";
import {
  competitionWorkslist,
  competitionAwardsConfiglist,
  addCompetitionAwardsConfig,
  updateCompetitionAwardsConfig,
} from "@/api/tournament/prize.js";
import { onMounted } from "vue";
const allData = [
  { id: 1, name: "项目A", desc: "描述A" },
  { id: 2, name: "项目B", desc: "描述B" },
  { id: 3, name: "项目C", desc: "描述C" },
  { id: 4, name: "项目D", desc: "描述D" },
];
const leftData = ref([]);
const rightData = ref([]);

const selectedLeft = ref([]);
const selectedRight = ref([]);

const handleLeftSelectionChange = (val) => {
  selectedLeft.value = val;
};

const handleRightSelectionChange = (val) => {
  selectedRight.value = val;
};
const SelectionChange = (val) => {
  rightData.value = val;
};
// 移到右侧
const moveToRight = () => {
  const ids = selectedLeft.value.map((item) => item.id);
  rightData.value.push(...selectedLeft.value);
  leftData.value = leftData.value.filter((item) => !ids.includes(item.id));
  selectedLeft.value = [];
};

// 移回左侧
const moveToLeft = () => {
  const ids = selectedRight.value.map((item) => item.id);
  leftData.value.push(...selectedRight.value);
  rightData.value = rightData.value.filter((item) => !ids.includes(item.id));
  selectedRight.value = [];
};

const route = useRoute();
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  competitionSeriesId: route.query.competitionSeriesId,
  stageId: null,
});
// 赛道列表
const saidao = ref([]);
// 获取当前赛事阶段列表
const jieduanlieb = ref([]);
function chaxunxiangqing() {
  const params = {
    competitionSeriesId: route.query.competitionSeriesId,
  };

  getCompetition(params).then((response) => {
    jieduanlieb.value = response.data.competitionStageList;
    saidao.value = response.data.competitionTrackList;
    // queryParams.value.stageId = jieduanlieb.value[0].stageId;
    // competitionAwardsConfig();
    queryNowCompetitionStageConfig(params).then((res) => {
      queryParams.value.stageId = res.data.stageId;
      // /** 查询列表 */
      // getList();
      // // 查询奖项设置
      competitionAwardsConfig();
    });
  });
}

// 查询奖项设置
const jiangxianglist = ref([]);
const competitionAwardsConfig = () => {
  const params = {
    competitionSeriesId: queryParams.value.competitionSeriesId,
    stageId: queryParams.value.stageId,
  };
  competitionAwardsConfiglist(params).then((res) => {
    jiangxianglist.value = res.data;
  });
};

onMounted(() => {
  chaxunxiangqing();
});
</script>
<style lang="scss" scoped>
.app-container {
  display: flex;
  justify-content: space-around;
  .appleft {
    width: 400px;
    background: rgb(228, 223, 223);
    height: 300px;
    padding: 30px 0 0 40px;
  }
  .appright {
    width: calc(100% - 450px);
    height: 300px;
    .jiangxianglieb {
      display: flex;
      .addjiangxiang {
        width: 200px;
        height: 200px;
        padding: 30px;
        border: 10px solid rgb(231, 231, 229);
        img {
          width: 120px;
          height: 120px;
          cursor: pointer;
        }
      }
      .jiangxiang {
        border: 10px solid rgb(231, 231, 229);
        width: 200px;
        height: 200px;
        margin-left: 10px;
      }
    }
  }
}
.tables {
  display: flex;
}
</style>