<template>
  <div class="app-container">
    <el-card class="box-card">
     <template #header>
       <div class="between">
         <div class="title">{{ processName }}</div>
         <div><el-button @click="handleClose">关闭</el-button></div>
       </div>
     </template>
      <el-tabs v-model="activeName" @tab-change="tabChange">
        <el-tab-pane label="表单信息" name="first">
          <el-row>
            <el-col :span="16" :offset="4">
              <v-form-render ref="vFormRenderRef"></v-form-render>
              <div class="button-box mb15">
                <el-button :loading="submitLoading" type="primary" @click="debounce(submit, 500)">提交</el-button>
                <el-button @click="reset">重置</el-button>
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
import { getProcessForm, startProcess, getBpmnXml } from '@/api/workflow/process';
import { ElMessage } from "element-plus";
import ProcessViewer from '@/components/ProcessViewer';

const { proxy } = getCurrentInstance();

const route = useRoute();
const router = useRouter();

const processName = ref(''); // 流程名称
const activeName = ref('first'); // tab卡片下标
const vFormRenderRef = ref() // 表单渲染器
const submitLoading = ref(false);

const data = reactive({
  queryParams: {
    deployId: route.params && route.params.deployId,
    definitionId: route.query && route.query.definitionId,
    procInsId: route.query && route.query.procInsId
  },
  form: {},
  processView: {
    open: false,
    index: route.query && route.query.definitionId,
    xmlData:"",
  }
})

const { queryParams, form, processView } = toRefs(data);

function initData() {
  getProcessForm(queryParams.value).then(res => {
    if (res.data) {
      processName.value = res.data.processName;
      proxy.$nextTick(() => {
        vFormRenderRef.value.setFormJson(res.data.formModel);
        vFormRenderRef.value.setFormData(res.data.formData || {});
      })
    }
  })
}

/** 查看流程图 */
function handleProcessView() {
  // 发送请求，获取xml
  getBpmnXml(queryParams.value.definitionId).then(res => {
    processView.value.xmlData = res.msg;
  })
}

/** 切换tab */
function tabChange (value) {
  if (value == 'second') {
    processView.value.open = true;
  } else {
    processView.value.open = false;
  }
}

/** 提交 */
function submit() {
  // console.log('提交表单数据',vFormRenderRef.value.getFormData());

  // 启动流程并将表单数据加入流程变量
  if (queryParams.value.definitionId) {
    vFormRenderRef.value.getFormData().then(res => {
      submitLoading.value = true;
      startProcess(queryParams.value.definitionId, JSON.stringify(res)).then(res => {
        ElMessage.success(res.msg);
        proxy.$tab.closeOpenPage({
          path: '/wenti/owns'
        })
      }).finally(() => {
        submitLoading.value = false;
      })
    });
  }
}

/** 重置 */
function reset() {
  vFormRenderRef.value.resetForm();
}

/** 关闭 */
function handleClose() {
  proxy.$tab.closePage();
  router.go(-1);
}

initData();
handleProcessView();
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
    ::v-deep(.el-card__header) {
      padding: 10px 20px !important;
    }
  }
  .button-box {
    text-align: center;
  }
}
</style>
