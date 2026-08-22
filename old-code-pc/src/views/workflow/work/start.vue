<template>
  <div class="app-container">
    <el-card class="box-card container-custom">
      <el-tabs v-model="activeName" @tab-change="tabChange">
        <el-tab-pane label="表单信息" name="first">
          <el-row>
            <el-col :span="16" :offset="4">
              <v-form-render ref="vFormRenderRef"></v-form-render>
              <div class="button-box mb15">
                <el-button
                  :loading="submitLoading"
                  type="primary"
                  @click="debounce(submit, 500)"
                  >提交</el-button
                >
                <el-button @click="handleClose">返回</el-button>
                <!-- <el-button @click="reset">重置</el-button> -->
                <!-- <el-button type="info">保存草稿</el-button> -->
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <!-- 流程图 -->
        <!-- <el-tab-pane label="流程图" name="second">
          <el-row>
            <el-col :span="24">
          
              <process-viewer v-if="processView.open" :key="`designer-${processView.index}`" :xml="processView.xmlData" />
            </el-col>
          </el-row>
        </el-tab-pane> -->
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup name="WorkStart">
import {
  getProcessForm,
  startProcess,
  getBpmnXml,
} from "@/api/workflow/process";
import { ElMessage } from "element-plus";
import ProcessViewer from "@/components/ProcessViewer";
import { getAuthInfo } from "@/api/index";
import { getCurrentInstance, onMounted, reactive, ref, toRefs } from "vue";
import { useRoute, useRouter } from "vue-router";
const { proxy } = getCurrentInstance();

const route = useRoute();
const router = useRouter();

const processName = ref(""); // 流程名称
const activeName = ref("first"); // tab卡片下标
const vFormRenderRef = ref(); // 表单渲染器
const submitLoading = ref(false);

const data = reactive({
  queryParams: {
    deployId: route.params && route.params.deployId,
    definitionId: route.query && route.query.definitionId,
    procInsId: route.query && route.query.procInsId,
  },
  form: {},
  processView: {
    open: false,
    index: route.query && route.query.definitionId,
    xmlData: "",
  },
});

const { queryParams, form, processView } = toRefs(data);

function initData() {
  getProcessForm(queryParams.value).then((res) => {
    if (res.data) {
      processName.value = res.data.processName;
      proxy.$nextTick(() => {
        vFormRenderRef.value.formData.ApplicantId = userinfo.value.userId;
        vFormRenderRef.value.formData.ApplicantName = userinfo.value.realName;
        vFormRenderRef.value.formData.operationType = "retired";
        vFormRenderRef.value.formData.oldData = JSON.stringify([
          DataSetlist.value,
        ]),
              vFormRenderRef.value.formData.userNum=DataSetlist.value.competitionApplyInfoList.length,
             vFormRenderRef.value.formData.userIds=DataSetlist.value.competitionApplyInfoList.map(item=>item.userId).join(',')
              vFormRenderRef.value.formData.secondLevelCode  =DataSetlist.value.secondLevelCode
        vFormRenderRef.value.formData.teamCode = DataSetlist.value.teamCode,
        vFormRenderRef.value.formData.teamInfo = `${DataSetlist.value.competitionName}-${DataSetlist.value.competitionTrackName}-${DataSetlist.value.secondLevelName}-${DataSetlist.value.teamName}`,
          vFormRenderRef.value.setFormJson(res.data.formModel);
        // vFormRenderRef.value.setFormData(res.data.formData || {});
        console.log(vFormRenderRef.value.formData)
      });
    }
  });
}

/** 查看流程图 */
function handleProcessView() {
  // 发送请求，获取xml
  getBpmnXml(queryParams.value.definitionId).then((res) => {
    processView.value.xmlData = res.msg;
  });
}

/** 切换tab */
function tabChange(value) {
  if (value == "second") {
    processView.value.open = true;
  } else {
    processView.value.open = false;
  }
}

/** 提交 */
function submit() {
  // console.log("提交表单数据", vFormRenderRef.value.getFormData());

  // 启动流程并将表单数据加入流程变量
  if (queryParams.value.definitionId) {
    vFormRenderRef.value.getFormData().then((res) => {
      submitLoading.value = true;
      startProcess(queryParams.value.definitionId, JSON.stringify(res))
        .then((res) => {
          ElMessage.success(res.data);
        })
        .finally(() => {
          submitLoading.value = false;
          window.history.back();
        });
    });
  }
}
// 获取最新用户信息
const userinfo = ref({});
const userinfolist = () => {
  getAuthInfo()
    .then((res) => {
      userinfo.value = res.data;
    })
    .then(() => {
      initData();
    });
};

/** 重置 */
function reset() {
  vFormRenderRef.value.resetForm();
}

/** 关闭 */
function handleClose() {
  router.go(-1);
}

const DataSetlist = ref({});
onMounted(() => {
  DataSetlist.value = JSON.parse(localStorage.getItem("DataSet"));
  userinfolist();
  handleProcessView();
});
</script>

<style lang="scss" scoped>
.app-container {
  padding: 0;
  .between {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
  }
  .box-card {
    margin-top: 50px;
    margin-bottom: 50px;
    ::v-deep(.el-card__header) {
      padding: 10px 20px !important;
    }
  }
  .button-box {
    text-align: center;
  }
}
</style>
