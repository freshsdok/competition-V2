<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="赛事阶段" prop="stageId">
        <el-select
          v-model="queryParams.stageId"
          disabled
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
      <el-form-item label="赛事赛道" prop="stageId">
        <el-select
          v-model="queryParams.stageId"
          placeholder="请选择赛事赛道"
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
      <el-form-item label="赛事组别" prop="stageId">
        <el-select
          v-model="queryParams.stageId"
          placeholder="请选择赛事组别"
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
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row>
      <el-col :span="3">
        <el-button type="primary" @click="xiugaijiangxiang"
          >修改奖项配置</el-button
        >
      </el-col>
      <el-col :span="6" v-for="(item, index) in jiangxianglist" :key="index">
        <el-card style="width: 90%">
          <div style="display: flex">
            <div>奖项名称 ：</div>
            <dict-tag :options="awards_name" :value="item.awardsName" />
          </div>

          <div>已分配数量：{{ item.allocatedNum }}</div>
          <div>未分配数量：{{ item.unabsorbedNum }}</div>
          <div>总金额：{{ item.bonusNum }}</div>
          <div>剩余金额：{{ item.unabsorbedNum }}</div>
        </el-card>
      </el-col></el-row
    >
    <el-table
      v-loading="loading"
      :data="configList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="赛事名称" align="center" prop="competitionName" />
      <el-table-column label="赛事阶段" align="center" prop="stageName" />
      <el-table-column label="创建时间" align="center" prop="createTime" />
      <el-table-column label="作品名称" align="center" prop="worksName" />
      <el-table-column label="参赛人" align="center" prop="userName" />
      <el-table-column label="分数" align="center" prop="worksScore" />
      <el-table-column
        label="操作"
        align="center"
        width="150"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            v-if="scope.row.isAdvance == null"
            @click="handleUpdate(scope.row)"
            >评分</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
    <el-dialog v-model="jiangxiangopen" :title="jxtitle" width="1000" center>
      <el-form ref="fromRef" :model="jxform" label-width="130px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="奖项名称" prop="awardsName">
              <el-select
                v-model="jxform.awardsName"
                placeholder="请选择奖项名称"
              >
                <el-option
                  v-for="item in awards_name"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="获奖人数/队伍数" prop="awardNum">
              <el-input-number
                v-model="jxform.awardNum"
                placeholder="请输入获奖人数/队伍数"
                type="number"
                :precision="0"
                :step="1"
                :min="1"
                style="width: 100%"
                :max="99999999"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="奖金金额（元）" prop="bonusNum">
              <el-input-number
                v-model="jxform.bonusNum"
                placeholder="请输入奖金金额"
                type="number"
                :step="0.01"
                :min="0"
                style="width: 100%"
                :max="99999999"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-row justify="end"
        ><el-button type="primary" plain @click="addCk"
          >确认录入</el-button
        ></el-row
      >
      <el-row style="width: 100%; margin-top: 20px; padding-left: 130px">
        <el-table :data="tableData" style="width: 100%" max-height="300">
          <el-table-column
            type="index"
            label="序号"
            width="50px"
            fixed="left"
          />
          <el-table-column
            label="奖项名称"
            align="left"
            prop="awardsName"
            width="200"
            show-overflow-tooltip
          >
            <template #default="scope">
              <dict-tag :options="awards_name" :value="scope.row.awardsName" />
            </template>
          </el-table-column>
          <el-table-column
            prop="awardNum"
            label="获奖人数/队伍数"
            min-width="120"
          />
          <el-table-column
            prop="bonusNum"
            label="奖金金额（元）"
            min-width="120"
          />

          <!-- 操作列 -->
          <el-table-column
            label="操作"
            width="120"
            fixed="right"
            v-if="!onlyShow"
          >
            <template #default="scope">
              <el-button
                type="danger"
                size="mini"
                @click="deleteRow(scope.$index)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-row>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
          <el-button v-if="!isDetail" type="primary" @click="submitForm"
            >保 存</el-button
          >
        </div>
      </template>
    </el-dialog>
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
  updateCompetitionAwardsConfig
} from "@/api/tournament/prize.js";
import { useDict } from "@/utils/dict";
import { onMounted } from "vue";
import modal from "@/plugins/modal";
import { cloneDeep } from "lodash";
// 字典数据
const { awards_name } = useDict("awards_name");

const route = useRoute();
const { proxy } = getCurrentInstance();
// 列表
const configList = ref([]);
const loading = ref(true);

const ids = ref([]);

const total = ref(0);
const data = reactive({
  jxform: {
    awardsName: "",
    awardNum: null,
    bonusNum: null,
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    competitionSeriesId: route.query.competitionSeriesId,
    stageId: null,
  },
});

const { queryParams, jxform } = toRefs(data);

/** 查询列表 */
function getList() {
  loading.value = false;

  competitionWorkslist(queryParams.value).then((res) => {
    configList.value = res.rows;
  });
}

// 获取当前赛事阶段列表
const jieduanlieb = ref([]);
function chaxunxiangqing() {
  const params = {
    competitionSeriesId: route.query.competitionSeriesId,
  };

  getCompetition(params).then((response) => {
    jieduanlieb.value = response.data.competitionStageList;
    queryNowCompetitionStageConfig(params).then((res) => {
      queryParams.value.stageId = res.data.stageId;
      /** 查询列表 */
      getList();
      // 查询奖项设置
      competitionAwardsConfig();
    });
  });
}
// 查询奖项设置
const jiangxianglist = ref([]);
const jiangxiangopen = ref(false);
const jxtitle = ref("配置奖项");
const competitionAwardsConfig = () => {
  const params = {
    competitionSeriesId: queryParams.value.competitionSeriesId,
    stageId: queryParams.value.stageId,
  };
  competitionAwardsConfiglist(params).then((res) => {
    if (res.data.length == 0) {
      modal.confirm("当前暂未设置奖项，是否立即设置").then(function () {
        jxtitle.value = "配置奖项";
        jiangxiangopen.value = true;
      });
    } else {
      jiangxianglist.value = res.data;
    }
  });
};
/** 确认录入奖项 */
const fromRef = ref(null);
const tableData = ref([]);
function addCk() {
  fromRef.value.validate((valid) => {
    if (valid) {
      tableData.value.push(cloneDeep(jxform.value));
      jxform.value = {
        awardsName: "",
        awardNum: null,
        bonusNum: null,
      };
    }
  });
}
/** 删除行 */
function deleteRow(index) {
  tableData.value.splice(index, 1);
}

const submitForm = () => {
  tableData.value.forEach((item) => {
    item.competitionSeriesId = queryParams.value.competitionSeriesId;
    item.stageId = queryParams.value.stageId;
  });
  if (jxtitle.value == "配置奖项") {
    addCompetitionAwardsConfig(tableData.value).then((res) => {
      // 从新获取奖项信息
      competitionAwardsConfig();
    });
  }else if(jxtitle.value == "修改配置奖项"){
     updateCompetitionAwardsConfig(tableData.value).then((res) => {
      // 从新获取奖项信息
      competitionAwardsConfig();
    });
  }
};
const xiugaijiangxiang = () => {
      jiangxiangopen.value = true;
  jxtitle.value = "修改配置奖项";
};
/** 奖项表单重置 */
function reset() {
  jxform.value = {};
  proxy.resetForm("configRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.worksId);
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const worksId = row.worksId || ids.value;
}

onMounted(() => {
  // 获取当前赛事的阶段id和赛事id
  chaxunxiangqing();
});
</script>
