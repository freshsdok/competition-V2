<template>
  <div class="app-container">
    <el-tabs
      tab-position="top"
      :model-value="processed === true ? 'approval' : 'form'"
      @tab-change="handleTabChange">

      <el-tab-pane label="任务办理" name="approval" v-if="processed === true">
        <el-card class="box-card" header="填写表单" shadow="hover" v-if="existTaskForm">
          <el-row>
            <el-col :span="20" :offset="2">
              <v-form-render
                  ref="taskFormParserRef"
                  :form-json="taskFormData.formModel">
              </v-form-render>
            </el-col>
          </el-row>
        </el-card>
        <el-card header="审批流程" class="box-card" shadow="hover">
          <el-row>
            <el-col :span="20" :offset="2">
              <el-form ref="taskFormRef1" :model="taskForm" :rules="rules" label-width="120px">
                <el-form-item label="审批意见" prop="comment">
                  <el-input type="textarea" :rows="5" v-model="taskForm.comment" placeholder="请输入审批意见" />
                </el-form-item>
                <el-form-item label="抄送人" prop="copyUserIds">
                  <el-tag
                    :key="index"
                    v-for="(item, index) in copyUser"
                    closable
                    :disable-transitions="false"
                    @close="handleClose('copy', item)">
                    {{ item.nickName }}
                  </el-tag>
                  <el-button class="button-new-tag" type="primary" icon="el-icon-plus" size="small" circle @click="onSelectCopyUsers" />
                </el-form-item>
                <el-form-item label="指定审批人" prop="copyUserIds">
                  <el-tag
                    :key="index"
                    v-for="(item, index) in nextUser"
                    closable
                    :disable-transitions="false"
                    @close="handleClose('next', item)">
                    {{ item.nickName }}
                  </el-tag>
                  <el-button class="button-new-tag" type="primary" icon="el-icon-plus" size="small" circle @click="onSelectNextUsers" />
                </el-form-item>
              </el-form>
            </el-col>
          </el-row>
          <el-row :gutter="10" type="flex" justify="center" >
            <el-col :span="1.5">
              <el-button icon="Check" type="success" @click="handleComplete">通过</el-button>
            </el-col>
            <!-- <el-col :span="1.5">
              <el-button icon="ChatLineSquare" type="primary" @click="handleDelegate">委派</el-button>
            </el-col> -->
            <el-col :span="1.5">
              <el-button icon="User" type="success" @click="handleTransfer">转办</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button icon="Back" type="warning" @click="handleReturn">退回</el-button>
            </el-col>
            <!-- <el-col :span="1.5">
              <el-button icon="Close" type="danger" @click="handleReject">驳回</el-button>
            </el-col> -->
          </el-row>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="表单信息" name="form">
        <div>
          <el-card
            class="box-card"
            :header="formInfo.title"
            shadow="never"
            v-for="(formInfo, index) in processFormList"
            :key="index">
            <!--流程处理表单模块-->
            <el-row>
              <el-col :span="20" :offset="2">
                <v-form-render
                    :ref="setVFormRenderRef"
                    :form-json="formInfo.formModel"
                    :form-data="formInfo.formData">
                </v-form-render>
              </el-col>
            </el-row>
          </el-card>
        </div>
      </el-tab-pane >

      <el-tab-pane label="流转记录" name="record">
        <el-card class="box-card" shadow="never">
          <el-row>
            <el-col :span="20" :offset="2">
              <div class="block">
                <el-timeline>
                  <el-timeline-item
                    v-for="(item,index) in historyProcNodeList"
                    :key="index"
                    :icon="setIcon(item.endTime)"
                    :color="setColor(item.endTime)">
                    <p style="font-weight: 700">{{ item.activityName }}</p>
                    <el-card v-if="item.activityType === 'startEvent'" class="box-card" shadow="hover">
                      {{ item.assigneeName }} 在 {{ item.createTime }} 发起流程
                    </el-card>
                    <el-card v-if="item.activityType === 'userTask'" class="box-card" shadow="hover">
                      <el-descriptions :column="3" :labelStyle="{'font-weight': 'bold'}">
                        <el-descriptions-item label="实际办理:">{{ item.assigneeName || '-'}}</el-descriptions-item>
                        <el-descriptions-item label="候选办理:">{{ item.candidate || '-'}}</el-descriptions-item>
                        <el-descriptions-item label="接收时间:">{{ item.createTime || '-'}}</el-descriptions-item>
                        <el-descriptions-item label="办结时间:">{{ item.endTime || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="耗时:">{{ item.duration || '-'}}</el-descriptions-item>
                      </el-descriptions>
                      <div v-if="item.commentList && item.commentList.length > 0">
                        <div v-for="(comment, index) in item.commentList" :key="index">
                          <el-divider content-position="left">
                            <el-tag :type="returnComment(commentType, 'type', comment.type)">
                              {{ returnComment(commentType, 'label', comment.type) }}
                            </el-tag>
                            <el-tag type="info">{{ parseTime(comment.time) }}</el-tag>
                          </el-divider>
                          <span>{{ comment.fullMessage }}</span>
                        </div>
                      </div>
                    </el-card>
                    <el-card
                      v-if="item.activityType === 'endEvent'"
                      class="box-card"
                      shadow="hover">
                      {{ item.createTime }} 结束流程
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="流程跟踪" name="track">
        <el-card class="box-card" shadow="never">
          <process-viewer
            v-if="processViewerVisible"
            :key="`designer-${loadIndex}`"
            :style="{'height': `calc(100vh - 280px)`}"
            :xml="xmlData"
            :finishedInfo="finishedInfo"
            :allCommentList="historyProcNodeList"/>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!--退回流程-->
    <el-dialog :title="returnTitle" v-model="returnOpen" width="40%" append-to-body>
      <el-form ref="taskFormRef2" :model="taskForm" label-width="80px" >
        <el-form-item label="退回节点" prop="targetKey">
          <el-radio-group v-model="taskForm.targetKey">
            <el-radio-button
              v-for="item in returnTaskList"
              :key="item.id"
              :label="item.id"
            >{{item.name}}</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="returnOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitReturn">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <!--选择人员-->
    <user-select
        v-if="userData.open"
        ref="userSelect"
        :multiple="userData.type === 'copy' ||userData.type === 'next'"
        :defaultSelectList="userMultipleSelection"
        @close="userData.open = false"
        @confirm="submitUserData">
    </user-select>

  </div>
</template>

<script setup>
import { detailProcess } from '@/api/workflow/process';
import { complete, delegate, transfer, rejectTask, returnList, returnTask } from '@/api/workflow/task'
import { listUser, selectAllDeptTreeList } from '@/api/system/user'
import ProcessViewer from '@/components/ProcessViewer'
import { ElMessage, ElMessageBox } from "element-plus";
import UserSelect from "@/components/UserSelect/index.vue";

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const commentType = ref([
  { label: "通过", type: 'success', value: "1" },
  { label: "退回", type: 'warning', value: "2" },
  { label: "驳回", type: 'danger', value: "3" },
  { label: "委派", type: 'primary', value: "4" },
  { label: "转办", type: 'success', value: "5" },
  { label: "终止", type: 'danger', value: "6" },
  { label: "撤回", type: 'info', value: "7" },
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
  pageSize: 10,
  deptId: undefined
})

const total = ref(0)
// 遮罩层
const loading = ref(true)

const taskFormRef1 = ref(null)
const taskFormRef2 = ref(null)

const taskForm = reactive({
  comment:"", // 意见内容
  procInsId: "", // 流程实例编号
  taskId: "" ,// 流程任务编号
  copyUserIds: "", // 抄送人Id
  variables: null,
  vars: "",
  targetKey:""
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
    el.disableForm();
  }
};
const taskFormData = reactive({})// 流程变量数据
const processFormList = ref([])// 流程变量数据
const returnTaskList = ref([]) // 回退列表数

const processed = ref(false)
const returnTitle = ref(null)
const returnOpen = ref(false)
const rejectOpen = ref(false)
const rejectTitle = ref(null)

const userData = reactive({
  title: '',
  type: '',
  open: false,
})

const copyUser = ref([])
const nextUser = ref([])
const userMultipleSelection = ref([])
const userDialogTitle = ref('')
const userOpen = ref(false)

function initData() {
  taskForm.procInsId = route.params && route.params.procInsId;
  taskForm.taskId  = route.query && route.query.taskId;
  processed.value = route.query && eval(route.query.processed || false);
  // 流程任务重获取变量表单
  getProcessDetails(taskForm.procInsId, taskForm.taskId);
  loadIndex.value = taskForm.procInsId;
}

initData()

/** 查询部门下拉树结构 */
function getTreeSelect() {
  return
  selectAllDeptTreeList().then(response => {
    deptOptions.value = response.data;
  });
}

/** 查询用户列表 */
function getList() {
  userLoading.value = true;
  listUser(queryParams).then(response => {
    userList.value = response.rows;
    total.value = response.total;
    toggleSelection(userMultipleSelection.value);
    userLoading.value = false;
  });
}

// 筛选节点
function filterNode(value, data) {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
}

// 节点单击事件
function handleNodeClick(data) {
  queryParams.deptId = data.id;
  getList();
}

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
    nextTick(()=> {
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

/** 流程变量赋值 */
function handleCheckChange(val) {
  if (val instanceof Array) {
    taskForm.values = {
      "approval": val.join(',')
    }
  } else {
    taskForm.values = {
      "approval": val
    }
  }
}

function getProcessDetails(procInsId, taskId) {
  const params = {procInsId: procInsId, taskId: taskId}
  detailProcess(params).then(res => {
    const data = res.data;
    xmlData.value = data.bpmnXml;
    processFormList.value = data.processFormList;
    existTaskForm.value = data.existTaskForm;
    if (existTaskForm.value) {
      Object.assign(taskFormData, data.taskFormData)
    }
    historyProcNodeList.value = data.historyProcNodeList;
    Object.assign(finishedInfo, data.flowViewer);
  })
}

/** 切换tab */
function handleTabChange (value) {
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
  getTreeSelect();
  getList()
  userData.open = true;
}

/** 判断是否有附带表单 */
function validateFormRender () {
  return new Promise((resolve, reject) => {
    // 校验表单
    if (existTaskForm.value) {
      taskFormParserRef.value.getFormData().then(res => {
        taskForm.variables = res;
        resolve()
      });
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
        complete(taskForm).then(response => {
          ElMessage.success(response.msg);
          goBack();
        });
      }
    });
  })
}

/** 委派任务 */
function handleDelegate() {
  proxy.$refs["taskFormRef1"].validate(valid => {
    if (valid) {
      userMultipleSelection.value = [];
      userData.type = 'delegate';
      userData.title = '委派任务'
      userData.open = true;
      getTreeSelect();
    }
  })
}

/** 转办任务 */
function handleTransfer(){
  proxy.$refs["taskFormRef1"].validate(valid => {
    if (valid) {
      userMultipleSelection.value = [];
      userData.type = 'transfer';
      userData.title = '转办任务';
      userData.open = true;
      getTreeSelect();
    }
  })
}

/** 拒绝任务 */
function handleReject() {
  proxy.$refs["taskFormRef1"].validate(valid => {
    if (valid) {
      ElMessageBox.confirm('驳回审批单流程会终止，是否继续？').then(function() {
        return rejectTask(taskForm);
      }).then(res => {
        ElMessage.success(res.msg);
        goBack();
      });
    }
  });
}

function changeCurrentUser(val) {
  currentUserId.value = val.userId
}

/** 返回页面 */
function goBack() {
  // 关闭当前标签页并返回上个页面
  proxy.$tab.closePage(route)
  router.back()
}

function submitUserData(val) {
  userMultipleSelection.value = val;
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
      returnTask(taskForm).then(res => {
        ElMessage.success(res.msg);
        goBack()
      });
    }
  });
}

/** 返回comment */
function returnComment (list, field, val) {
  let obj = list.find(item => {
    return item.value == val
  })
  if (obj) {
    return obj[field]
  }
}

</script>

<style lang="scss" scoped>
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}

.box-card {
  width: 100%;
  margin-bottom: 20px;
}

.el-tag + .el-tag {
  margin-left: 10px;
}

.el-row {
  margin-bottom: 20px;
  &:last-child {
    margin-bottom: 0;
  }
}
.el-col {
  border-radius: 4px;
}

.button-new-tag {
  margin-left: 10px;
}
.head-container {
  height: 523px;
  overflow: auto;
}
</style>
