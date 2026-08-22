<template>
  <div>
    <el-row :gutter="20">

      <!-- 左侧信息 -->
      <el-col :span="16">
        <div class="left-container">
          <el-tabs tab-position="top" :model-value="existTaskForm === true ? 'approval' : 'form'"
            @tab-change="handleTabChange">

            <el-tab-pane label="任务办理" name="approval" v-if="existTaskForm">
              <el-card class="box-card" header="填写表单" shadow="hover">
                <el-row>
                  <el-col :span="24">
                    <v-form-render ref="taskFormParserRef" :form-json="taskFormData.formModel">
                    </v-form-render>
                  </el-col>
                </el-row>
              </el-card>
            </el-tab-pane>

            <el-tab-pane label="表单信息" name="form">
              <el-card class="box-card mb20" :header="formInfo.title" shadow="never"
                v-for="(formInfo, index) in processFormList" :key="index">
                <!--流程处理表单模块-->
                <el-row>
                  <el-col :span="24">
                    <v-form-render :ref="setVFormRenderRef" :disabled-mode="true" :form-json="formInfo.formModel"
                      :form-data="formInfo.formData">
                    </v-form-render>
                  </el-col>
                </el-row>
              </el-card>
            </el-tab-pane>

            <el-tab-pane label="流程跟踪" name="track">
              <el-card class="box-card" shadow="never">
                <process-viewer v-if="processViewerVisible" :key="`designer-${loadIndex}`"
                  :style="{ 'height': `calc(100vh - 280px)` }" :xml="xmlData" :finishedInfo="finishedInfo"
                  :allCommentList="historyProcNodeList" />
              </el-card>
            </el-tab-pane>

          </el-tabs>
        </div>
      </el-col>

      <!-- 右侧审批 -->
      <el-col :span="8">
        <div class="right-container">
          <el-card header="审批流程" class="box-card mb20" shadow="hover" v-if="processed === true && process">
            <el-row>
              <el-col :span="24">
                <el-form ref="taskFormRef1" :model="taskForm" :rules="rules" label-width="90px" label-position="top">
                  <div class="item mb10">
                    <span class="label">当前环节：</span>
                    <span class="value">{{ nodeInfo.activityName }}</span>
                  </div>
                  <div class="item mb10">
                    <span class="label">当前处理人：</span>
                    <span class="value">
                      <!-- {{ nodeInfo.candidate }} -->

                    </span>
                  </div>
                  <div class="item mb10">
                    <span class="label">流程发起时间：</span>
                    <span class="value">{{ nodeInfo.createTime }}</span>
                  </div>
                  <!-- <el-form-item prop="comment">
                    <el-input type="textarea" :rows="5" v-model="taskForm.comment" placeholder="请输入审批意见(必填)" />
                  </el-form-item> -->

                  <!-- <el-form-item prop="copyUserIds">
                    <el-button class="mr10" type="primary" icon="Plus" size="small"
                      @click="onSelectCopyUsers">添加抄送人</el-button>
                    <el-tag :key="index" v-for="(item, index) in copyUser" closable :disable-transitions="false"
                      @close="handleClose('copy', item)">
                      {{ item.nickName }}
                    </el-tag>
                  </el-form-item>
                  <el-form-item prop="copyUserIds">
                    <el-button class="mr10" type="primary" icon="Plus" size="small"
                      @click="onSelectNextUsers">指定审批人</el-button>
                    <el-tag :key="index" v-for="(item, index) in nextUser" closable :disable-transitions="false"
                      @close="handleClose('next', item)">
                      {{ item.nickName }}
                    </el-tag>
                  </el-form-item> -->
                </el-form>
              </el-col>
            </el-row>
            <el-row :gutter="10" type="flex" justify="space-between" v-if="taskFormParserRef">
              <!-- <el-col :span="1.5" >
                <el-button :loading="rejectLoading" type="danger" @click="debounce(handleReject, 500)">驳回</el-button>
              </el-col>
              <el-col :span="1.5" >
                <el-button type="warning" @click="debounce(handleReturn, 500)">退回</el-button>
              </el-col>

              <el-col :span="1.5">
                <el-button type="info" @click="debounce(handleTransfer, 500)">转办</el-button>
              </el-col> -->
              <el-col :span="1.5">
                <el-button :loading="agreeLoading" type="primary" @click="debounce(handleComplete, 500)">提交</el-button>
              </el-col>
              <!-- <el-col :span="1.5">
                                <el-button icon="ChatLineSquare" type="primary" @click="handleDelegate">委派</el-button>
                            </el-col> -->
            </el-row>
          </el-card>

          <!-- 审批节点信息 -->
          <el-card header="流转记录" class="box-card" shadow="hover">
            <el-timeline>
              <el-timeline-item v-for="(item, index) in historyProcNodeList" :key="index" :icon="setIcon(item.endTime)"
                :color="setColor(item.endTime)" :timestamp="item.createTime" placement="top">
                <div v-if="item.activityType === 'startEvent'">{{ item.assigneeName }} 发起流程</div>
                <div v-if="item.activityType === 'userTask'">
                  <div class="between">
                    <div v-if="item.assigneeName">{{ item.assigneeName }}</div>
                    <div v-else>
                      <span v-if="!administrator">-</span>
                      <el-button v-else class="mr10" type="primary" icon="Plus" size="small"
                        @click="handleAddUsers(item)">添加人员</el-button>
                    </div>
                    <div class="activity-name">{{ item.activityName }}</div>
                  </div>
                  <div class="comment-list mt10" v-if="item.commentList && item.commentList.length > 0">
                    <div class="mb10" v-for="(comment, index) in item.commentList" :key="index">
                      <el-tag :type="returnComment(commentType, 'type', comment.type)">
                        {{ returnComment(commentType, 'label', comment.type) }}
                      </el-tag>
                      <span class="ml10">{{ formatDateTime(comment.time) }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="item.activityType === 'endEvent'">结束流程</div>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </div>
      </el-col>

    </el-row>

    <!--退回流程-->
    <el-dialog :title="returnTitle" v-model="returnOpen" width="40%" append-to-body>
      <el-form ref="taskFormRef2" :model="taskForm" label-width="80px">
        <el-form-item label="退回节点" prop="targetKey">
          <el-radio-group v-model="taskForm.targetKey">
            <el-radio-button v-for="item in returnTaskList" :key="item.id" :label="item.id">{{ item.name
            }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="returnOpen = false">取 消</el-button>
          <el-button :loading="returnLoading" type="primary" @click="debounce(submitReturn, 500)">确 定</el-button>
        </span>
      </template>
    </el-dialog>


  </div>
</template>

<script setup>
import { detailProcess } from '@/api/workflow/process';
import { complete, delegate, transfer, rejectTask, returnList, returnTask, designatedPerson } from '@/api/workflow/task';
import ProcessViewer from '@/components/ProcessViewer'
import { ElMessage, ElMessageBox } from "element-plus";

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const commentType = ref([
  { label: "解决", type: 'success', value: "1" },
  { label: "终止", type: 'danger', value: "2" },
  { label: "驳回", type: 'danger', value: "3" },
  { label: "上报", type: 'primary', value: "4" },
  { label: "转办", type: 'success', value: "5" },
  { label: "终止", type: 'danger', value: "6" },
  { label: "撤回", type: 'warning', value: "7" },
]);
const processViewerVisible = ref(false);
const loadIndex = ref(0); // 模型xml数据
const xmlData = ref(undefined);

const finishedInfo = reactive({
  finishedSequenceFlowSet: [],
  finishedTaskSet: [],
  unfinishedTaskSet: [],
  rejectedTaskSet: []
})

const historyProcNodeList = ref([])

// 部门名称
const deptName = ref(undefined)

// 部门树选项
const deptOptions = ref([])

const userLoading = ref(false)

const userTableRef = ref(null)
// 用户表格数据
const userList = ref([])

const deptProps = reactive({
  children: "children",
  label: "label"
})
// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 100,
  deptId: undefined
})

const total = ref(0)
// 遮罩层
const loading = ref(true)
const rejectLoading = ref(false); // 驳回loading
const returnLoading = ref(false); // 退回loading
const agreeLoading = ref(false);  // 通过loading

const taskFormRef1 = ref(null)
const taskFormRef2 = ref(null)

const taskForm = reactive({
  // comment: "同意", // 意见内容
  procInsId: "", // 流程实例编号
  taskId: "",// 流程任务编号
  copyUserIds: "", // 抄送人Id
  variables: null,
  vars: "",
  targetKey: ""
})

const rules = {
  comment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }],
}

const currentUserId = ref(null)

const variables = ref([])// 流程变量数据
const existTaskForm = ref(false) // 审批页面是否配置了业务表单
const taskFormParserRef = ref(null) // 审批页面的表单
const setVFormRenderRef = (el) => {
  if (el) {
    // el.setReadMode();
    // el.disableForm();
  }
};
const taskFormData = reactive({})// 流程变量数据
const processFormList = ref([])// 流程变量数据
const returnTaskList = ref([]) // 回退列表数

const processed = ref(false) // 前端路径参数区分是否审核
const process = ref(false) // 后端返回的参数区分是否审核
const administrator = ref(route.query && eval(route.query.administrator || false)) // 是否是管理员(从流程监控进入)
const returnTitle = ref(null)
const returnOpen = ref(false)
const rejectOpen = ref(false)
const rejectTitle = ref(null)
const currentTargetId = ref(null)

const userData = reactive({
  title: '',
  type: '',
  open: false,
})

const copyUser = ref([])
const nextUser = ref([])
const userMultipleSelection = ref([])
const nodeInfo = reactive({
  activityName: '',
  candidate: '',
  createTime: '',
})

function initData() {
  taskForm.procInsId = route.params && route.params.procInsId;
  taskForm.taskId = route.query && route.query.taskId;


  taskForm.changeStatusFlag = route.query && route.query.changeStatusFlag;
  processed.value = route.query && eval(route.query.processed || false);
  // 流程任务重获取变量表单
  getProcessDetails(taskForm.procInsId, taskForm.taskId, taskForm.changeStatusFlag);
  loadIndex.value = taskForm.procInsId;
}

initData()



function setIcon(val) {
  if (val) {
    return "el-icon-check";
  } else {
    return "el-icon-time";
  }
}

function setColor(val) {
  if (val) {
    return "#2bc418";
  } else {
    return "#b3bdbb";
  }
}

// 多选框选中数据
function handleSelectionChange(selection) {
  userMultipleSelection.value = selection
}

function toggleSelection(selection) {
  if (selection && selection.length > 0) {
    nextTick(() => {
      selection.forEach(item => {
        let row = userList.value.find(k => k.userId === item.userId);
        userTableRef.value.toggleRowSelection(row);
      })
    })
  } else {
    nextTick(() => {
      userTableRef.value.clearSelection();
    });
  }
}

// 关闭标签
function handleClose(type, tag) {
  let userObj = userMultipleSelection.value.find(item => item.userId === tag.id);
  userMultipleSelection.value.splice(userMultipleSelection.value.indexOf(userObj), 1);
  if (type === 'copy') {
    copyUser.value = userMultipleSelection.value;
    // 设置抄送人ID
    if (copyUser.value && copyUser.value.length > 0) {
      const val = copyUser.value.map(item => item.id);
      taskForm.copyUserIds = val instanceof Array ? val.join(',') : val;
    } else {
      taskForm.copyUserIds = '';
    }
  } else if (type === 'next') {
    nextUser.value = userMultipleSelection.value;
    // 设置抄送人ID
    if (nextUser.value && nextUser.value.length > 0) {
      const val = nextUser.value.map(item => item.id);
      taskForm.nextUserIds = val instanceof Array ? val.join(',') : val;
    } else {
      taskForm.nextUserIds = '';
    }
  }
}

function getProcessDetails(procInsId, taskId, changeStatusFlag) {
  const params = { procInsId: procInsId, taskId: taskId, changeStatusFlag: changeStatusFlag }
  detailProcess(params).then(res => {

    const data = res.data;
    xmlData.value = data.bpmnXml;
    process.value = data.process;
    processFormList.value = data.processFormList;

    existTaskForm.value = data.existTaskForm;
    //  data.taskFormData.formModel.widgetList[0].options.defaultValue=2

    if (existTaskForm.value) {
      Object.assign(taskFormData, data.taskFormData)
    }

    historyProcNodeList.value = data.historyProcNodeList;
    console.log(historyProcNodeList)
    // 获取最新节点和发起时间
    if (data.historyProcNodeList.length > 0) {
      nodeInfo.createTime = data.historyProcNodeList[data.historyProcNodeList.length - 1].createTime;
      nodeInfo.candidate = data.historyProcNodeList[0].candidate;
      nodeInfo.activityName = data.historyProcNodeList[0].activityName;
    } else {
      nodeInfo.activityName = "";
      nodeInfo.candidate = "";
      nodeInfo.createTime = "";
    }
    Object.assign(finishedInfo, data.flowViewer);
  })
}

/** 切换tab */
function handleTabChange(value) {
  if (value == 'track') {
    processViewerVisible.value = true;
  } else {
    processViewerVisible.value = false;
  }
}

function onSelectCopyUsers() {
  userMultipleSelection.value = copyUser.value;
  onSelectUsers('添加抄送人', 'copy')
}
function onSelectNextUsers() {
  userMultipleSelection.value = nextUser.value;
  onSelectUsers('指定审批人', 'next')
}

function onSelectUsers(title, type) {
  userData.title = title;
  userData.type = type;
  userData.open = true;
}

/** 管理员添加人员 */
function handleAddUsers(item) {
  currentTargetId.value = item.activityId;
  userMultipleSelection.value = [];
  userData.open = true;
}

/** 判断是否有附带表单 */
function validateFormRender() {

  return new Promise((resolve, reject) => {
    // 校验表单
    if (existTaskForm.value) {

      taskFormParserRef.value.getFormData().then(res => {
        taskForm.variables = res;


        resolve()
      }).catch(() => {
        proxy.$modal.msgWarning("请完善'任务办理'中的流程表单!");
      })
    } else {
      resolve()
    }
  })
}

/** 通过任务 */
function handleComplete() {
  validateFormRender().then(() => {
    proxy.$refs["taskFormRef1"].validate(valid => {
      if (valid) {
        agreeLoading.value = true;
        if (taskForm.variables.treatmentType22 == 5) {
          if (taskForm.variables.organizer.indexOf(taskForm.variables.sponsor) > -1) {
            ElMessage.error('协办人中不能包括主办人');
            agreeLoading.value = false;
            return
          }
          taskForm.copyUserIds = taskForm.variables.organizer.join(",")
          console.log(taskForm.copyUserIds)
        }
        complete(taskForm).then(response => {
          ElMessage.success(response.msg);
          goBack();
        }).finally(() => {
          agreeLoading.value = false;
        });
      }
    });
  })
}



/** 返回页面 */
function goBack() {
  // 关闭当前标签页并返回上个页面
  proxy.$tab.closePage(route)
  router.back()
}

/** 
 * 选择人员 
 * 区分抄送、转办和手动添加审核人员 
 */
function submitUserData(val) {
  console.log(administrator.value)
  if (administrator.value) {
    targetAddUsers(val);
  } else {
    copyAndNextUsers(val);
  }
}

/** 节点手动添加审核人员 */
function targetAddUsers(val) {
  if (!val || val.length <= 0) {
    ElMessage.error("请选择用户");
    return false;
  };
  let params = {
    processInstanceId: taskForm.procInsId,
    activityId: currentTargetId.value,
    users: val.map(k => k.userId).join(","),
  };
  proxy.$modal.confirm('是否添加选中的人员为此节点审核人?  添加后不允许更改！！！').then(function () {
    return designatedPerson(params);
  }).then(() => {
    userData.open = false;
    initData();
    proxy.$modal.msgSuccess("操作成功");
  }).catch(() => { });
}

/** 抄送和转办人员 */
function copyAndNextUsers(val) {

  userMultipleSelection.value = val;
  if (val.length > 0) {
    currentUserId.value = val[0]?.userId
  }
  let type = userData.type;
  if (type === 'copy' || type === 'next') {
    if (!userMultipleSelection.value || userMultipleSelection.value.length <= 0) {
      ElMessage.error("请选择用户");
      return false;
    }
    let userIds = userMultipleSelection.value.map(k => k.userId);
    if (type === 'copy') {
      // 设置抄送人ID信息
      copyUser.value = userMultipleSelection.value;
      taskForm.copyUserIds = userIds instanceof Array ? userIds.join(',') : userIds;
    } else if (type === 'next') {
      // 设置下一级审批人ID信息
      nextUser.value = userMultipleSelection.value;
      taskForm.nextUserIds = userIds instanceof Array ? userIds.join(',') : userIds;
    }
    userData.open = false;
  } else {
    if (!taskForm.comment) {
      ElMessage.error("请输入审批意见");
      return false;
    }
    if (!currentUserId.value) {
      ElMessage.error("请选择用户");
      return false;
    }
    taskForm.userId = currentUserId.value;
    if (type === 'delegate') {
      delegate(taskForm).then(res => {
        ElMessage.success(res.msg);
        goBack();
      });
    }
    if (type === 'transfer') {
      transfer(taskForm).then(res => {
        ElMessage.success(res.msg);
        goBack();
      });
    }
  }
}

/** 可退回任务列表 */
function handleReturn() {
  proxy.$refs["taskFormRef1"].validate(valid => {
    if (valid) {
      returnTitle.value = "退回流程";
      returnList(taskForm).then(res => {
        returnTaskList.value = res.data;
        taskForm.values = null;
        // 
        returnOpen.value = true;
      })
    }
  });

}

/** 提交退回任务 */
function submitReturn() {
  proxy.$refs["taskFormRef2"].validate(valid => {
    if (valid) {
      if (!taskForm.targetKey) {
        ElMessage.error("请选择退回节点！");
        return
      }
      returnLoading.value = true;
      returnTask(taskForm).then(res => {
        ElMessage.success(res.msg);
        goBack()
      }).finally(() => {
        returnLoading.value = false;
      });
    }
  });
}

/** 返回comment */
function returnComment(list, field, val) {
  let obj = list.find(item => {
    return item.value == val
  })
  if (obj) {
    return obj[field]
  }
}
// 格式话时间
function formatDateTime(isoString) {
  const date = new Date(isoString);

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0'); // 月份从0开始
  const day = String(date.getDate()).padStart(2, '0');

  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}
</script>

<style lang="scss" scoped>
.left-container {
  margin: 20px 0 20px 20px;
  padding: 20px;
  background: #ffffff;
  border-radius: 10px;
  min-height: calc(100vh - 138px);
}

.right-container {
  margin: 20px 20px 20px 0;
  padding: 20px;
  background: #ffffff;
  border-radius: 10px;
  min-height: calc(100vh - 138px);

  .item {
    font-size: 14px;

    .label {
      display: inline-block;
      width: 100px;
      text-align-last: justify;
    }

    .value {
      font-weight: bold;
    }
  }

  .between {
    display: flex;
    justify-content: space-between;

    .activity-name {
      font-weight: bold;
      white-space: nowrap;
      margin-left: 15px;
    }
  }
}

.box-card {
  width: 100%;
}

.el-tag+.el-tag {
  margin-left: 10px;
}

.el-col {
  border-radius: 4px;
}

.el-timeline {
  padding: 0;
}
</style>