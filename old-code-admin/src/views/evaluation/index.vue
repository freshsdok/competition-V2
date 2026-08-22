<template>
  <div class="">
    <el-table v-loading="loading" :data="configList">
      <el-table-column label="赛事名称" align="center" prop="competitionName" />
      <el-table-column label="赛事阶段" align="center" prop="stageName" />
      <el-table-column label="创建时间" align="center" prop="createTime" />
      <el-table-column label="作品名称" align="center" prop="worksName" />

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
  </div>
  <el-dialog v-model="centerDialogVisible" title="提取" width="500" center>
    <el-form :model="tiqulist" label-width="100px">
      <el-form-item label="提取码" prop="noticeTitle">
        <el-input
          v-model="tiqulist.tiqumaiqu"
          placeholder="请输入提取码"
          clearable
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="tiqushujv"> 提取 </el-button>
      </div>
    </template>
  </el-dialog>
  <!-- 添加或修改参数配置对话框 -->
    <el-dialog title="评分" v-model="open" width="600px" append-to-body>
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
</template>
<script setup>
import { getSpecialistList ,getLinkCompetitionWorksInfo, updateCompetitionWorksScore,} from "@/api/system/config";
const { proxy } = getCurrentInstance();
const tiqulist = ref({});
const centerDialogVisible = ref(true);
const configList = ref([]);


const tiqushujv = () => {
  const params = {
    extractionCode: tiqulist.value.tiqumaiqu,
  };
  getSpecialistList(params).then((response) => {
    configList.value = response.data;
      centerDialogVisible.value = false;
  });

};

const data = reactive({
  form: {},
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
const { form, rules } = toRefs(data);
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
const open=ref(false)
/** 评分按钮操作 */
function handleUpdate(row) {
  reset();
  const worksId = row.worksId || ids.value;
  getLinkCompetitionWorksInfo(worksId).then((response) => {
    form.value = response.data;
    open.value = true;
  });
}
// 下载作品
const xiazaizuopin = () => {
  proxy.downloadJS(
    import.meta.env.VITE_APP_BASE_API +
      `/file/common/resourceLinkDownload?resource=${form.value.worksUrl}`,
    `${form.value.worksName}.${form.value.worksUrl.split(".").at(-1)}`
  );
};
/** 提交按钮 */
function submitForm() {
  proxy.$refs["configRef"].validate((valid) => {
    if (valid) {
      console.log(form.value);
      form.value.extractionCode= tiqulist.value.tiqumaiqu
      updateCompetitionWorksScore(form.value).then(() => {
        open.value = false;
        tiqushujv();
      });
    }
  });
}
</script>



<style scoped>
</style>