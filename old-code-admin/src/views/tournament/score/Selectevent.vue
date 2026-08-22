<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
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

      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery"
          >搜索</el-button
        >
        <!-- <el-button icon="Refresh" @click="resetQuery">重置</el-button> -->
        <el-button icon="Refresh" @click="pack">生成打包链接</el-button>
        <el-button
          type="primary"
          @click="jinjiuser"
          v-if="configList.length > 0 && configList[0].isAdvance == null"
          >生成晋级人员列表</el-button
        >
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="configList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="赛事名称" align="center" prop="competitionName" />
      <el-table-column label="赛事阶段" align="center" prop="stageName" />
      <el-table-column label="提交时间" align="center" prop="createTime" />
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

    <!-- 添加或修改参数配置对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form
        :model="form"
        ref="configRef"
        :rules="rules"
        label-width="100px"
        style="width: 500px"
      >
        <el-form-item label="作品名称">
          <el-input v-model="form.worksName" disabled />
        </el-form-item>
        <el-form-item label="作品说明">
          <el-input v-model="form.worksDesc" disabled />
        </el-form-item>
        <el-form-item label="作品">
          <el-button type="primary" @click="xiazaizuopin" :disabled="false">
            点击下载
          </el-button>
        </el-form-item>
        <el-form-item label="分数" prop="worksScore">
          <el-input v-model="form.worksScore" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
      title="生成晋级成员"
      v-model="jinjiopen"
      width="600px"
      append-to-body
    >
      <el-form
        :model="jinjiform"
        ref="configRef"
        :rules="rules"
        label-width="120px"
        style="width: 500px"
      >
        <el-form-item label="赛事阶段" prop="stageId">
          <el-select
            v-model="jinjiform.stageId"
            placeholder="请选择赛事阶段"
            clearable
          >
            <el-option
              v-for="dict in jieduanlieb"
              :key="dict.stageId"
              :label="dict.stageName"
              :value="dict.stageId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="晋级方式">
          <el-select
            v-model="jinjiform.advanceType"
            placeholder="请选择审核状态"
            clearable
            style="width: 160px"
          >
            <el-option
              v-for="dict in advance_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="晋级分数" v-if="jinjiform.advanceType == 1">
          <el-input v-model="jinjiform.promoteScore" @change="updatajinji" />
        </el-form-item>
        <el-form-item label="晋级人数团队数" v-if="jinjiform.advanceType == 2">
          <el-input v-model="jinjiform.promoteNum" @change="updatajinji" />
        </el-form-item>
      </el-form>
      <el-table :data="jinjimingdan">
        <el-table-column label="参赛人" align="center" prop="userName" />
        <el-table-column label="排名" align="center" prop="worksRank" />
        <el-table-column label="状态" align="center" prop="isAdvance">
          <template #default="scope">
            <el-tag v-if="scope.row.isAdvance == 1">晋级</el-tag>
            <el-tag v-else>未晋级</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分数" align="center" prop="worksScore" />
      </el-table>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="quedingfengcun">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="tiqumaopen" title="提取码及链接" width="500" center>
      <p>链接：{{ lianjie }}</p>
      <p>提取码：{{ tiquma }}</p>
    </el-dialog>
  </div>
</template>

<script setup name="ScoreConfig">
import {
  competitionWorkslist,
  competitionWorksworksId,
  updateCompetitionWorks,
  saveCompetitionWorkLinkInfo,
} from "@/api/system/config";
import {
  getCompetition,
  saveUserGradeCompetitionStageConfig,
  createAdvanceUserGradeInfo,
  saveAdvanceUserGradeInfo,
  queryNowCompetitionStageConfig,
} from "@/api/tournament/competition";
import { useDict } from "@/utils/dict";
const route = useRoute();
const { proxy } = getCurrentInstance();
const { advance_type } = useDict("advance_type");
const configList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    competitionSeriesId: route.query.competitionSeriesId,
    stageName: "",
  },
  rules: {
    worksScore: [
      { required: true, message: "请输入分数", trigger: "blur" },
      {
        pattern: /^\d+(\.\d{1})?$/,
        message: "分数最多保留一位小数（如：85 或 90.5）",
        trigger: ["blur", "change"],
      },
      {
        validator: (rule, value, callback) => {
          const num = parseFloat(value);
          if (value !== "" && !isNaN(num) && (num < 0 || num > 100)) {
            callback(new Error("分数必须在 0 到 100 之间"));
          } else {
            callback();
          }
        },
        trigger: ["blur", "change"],
      },
    ],
  },
});

const { queryParams, form, rules } = toRefs(data);

/** 查询列表 */
function getList() {
  loading.value = true;
  competitionWorkslist(proxy.addDateRange(queryParams.value)).then(
    (response) => {
      configList.value = response.rows;
      total.value = response.total;
      loading.value = false;
    }
  );
}

// 获取当前赛事阶段列表
const jieduanlieb = ref([]);
function chaxunxiangqing() {
  const params = {
    competitionSeriesId: route.query.competitionSeriesId,
  };

  getCompetition(params).then((response) => {
    jieduanlieb.value = response.data.competitionStageList;
    queryParams.value.stageId = jieduanlieb.value[0].stageId;
      getList();
    // queryNowCompetitionStageConfig(params).then((res) => {
    //   queryParams.value.stageId = res.data.stageId;
    //   getList();
    // });
  });
}

// 生成晋级成员
const jinjiopen = ref(false);
const jinjiform = ref({
  stageId: "",
  competitionSeriesId: route.query.competitionSeriesId,
  advanceType: "1",
});

// 生成晋级人员列表
const jinjiuser = () => {
  jinjiopen.value = true;
  jinjiform.value = {};
  jinjiform.value.stageId = queryParams.value.stageId;
  xuanzhongjieduan();
};
const xuanzhongjieduan = () => {
  jinjiform.value = jieduanlieb.value.find(
    (item) => item.stageId == jinjiform.value.stageId
  );
  jinjiform.value.advanceType = "1";
  shengchengchengji();
};
// 修改晋级标准
const updatajinji = () => {
  saveUserGradeCompetitionStageConfig(jinjiform.value).then((response) => {
    if (response.code == 200) {
      shengchengchengji();
    } else {
      proxy.$modal.msgError("操作失败");
    }
  });
};

// 生成晋级人名单
const jinjimingdan = ref([]);
const shengchengchengji = () => {
  const params = {
    competitionSeriesId: route.query.competitionSeriesId,
    stageId: jinjiform.value.stageId,
    advanceType: jinjiform.value.advanceType,
  };
  createAdvanceUserGradeInfo(params).then((res) => {
    jinjimingdan.value = res.rows;
  });
};
chaxunxiangqing();
// 封存
const quedingfengcun = () => {
  console.log(jinjimingdan.value);
  saveAdvanceUserGradeInfo(jinjimingdan.value).then((res) => {
    if (res.code == 200) {
      jinjiopen.value = false;
      getList();
    } else {
      proxy.$modal.msgError("操作失败");
    }
  });
};
/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    worksId: undefined,
    configName: undefined,
    configKey: undefined,
    configValue: undefined,
    configType: "Y",
    remark: undefined,
  };
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
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const worksId = row.worksId || ids.value;
  competitionWorksworksId(worksId).then((response) => {
    form.value = response.data;
    open.value = true;
    title.value = "评分";
  });
}
// 生成打包链接
const tiquma = ref("");
const lianjie = ref("");
const tiqumaopen = ref(false);
const pack = () => {
  const params = {
    linkName: window.location.origin + "/evaluation",
    worksId: ids.value.join(","),
    competitionSeriesId: route.query.competitionSeriesId,
    stageId: queryParams.value.stageId,
  };
  saveCompetitionWorkLinkInfo(params).then((res) => {
    console.log(res);
    tiqumaopen.value = true;
    tiquma.value = res.msg;
    lianjie.value = window.location.origin + "/evaluation";
  });
};
/** 提交按钮 */
function submitForm() {
  proxy.$refs["configRef"].validate((valid) => {
    if (valid) {
      console.log(form.value);
      updateCompetitionWorks(form.value).then(() => {
        open.value = false;

        getList();
      });
    }
  });
}

// 下载作品
const xiazaizuopin = () => {
  proxy.downloadJS(
    import.meta.env.VITE_APP_BASE_API +
      `file/common/download?resource=${form.value.worksUrl}`,
    `${form.value.worksName}.${form.value.worksUrl.split(".").at(-1)}`
  );
};
</script>
