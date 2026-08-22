<template>
  <div class="app-container">
    <el-row :gutter="24">
      <!--用户数据-->
      <el-col>
        <el-form
          :model="queryParams"
          ref="queryRefsss"
          :inline="true"
          v-show="showSearch"
          label-width="100px"
        >
          <el-form-item label="审核标题" prop="auditTitle">
            <el-input
              v-model.trim="queryParams.auditTitle"
              placeholder="请输入审核标题"
              clearable
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item label="提交人" prop="subPer">
            <el-input
              v-model.trim="queryParams.subPer"
              placeholder="请输入提交人"
              clearable
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item label="所在学校" prop="teacherSchoolName" v-if="props.auditType == 'teacher'">
            <el-input
              v-model.trim="queryParams.teacherSchoolName"
              placeholder="请输入所在学校"
              clearable
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item label="审核结果" v-if="selectedTab == '已结束'" prop="checkStatus">
            <el-select
              v-model="queryParams.checkStatus"
              placeholder="请选择审核结果"
              clearable
              style="width: 240px"
              @change="handleQuery"
            >
              <!-- <el-option
                v-for="dict in check_status"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option> -->
               <el-option
                label="已通过"
                value="4"
              ></el-option>
               <el-option
                label= "已拒绝"
                value="5"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="类型" prop="auditType" v-if="!auditType">
            <el-select
              v-model="queryParams.auditType"
              placeholder="请选择任务类型"
              clearable
              style="width: 240px"
              @change="handleQuery"
            >
              <el-option
                v-for="dict in audit_flow_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
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
        <el-col style="padding-left: 0px;margin-bottom: 5px;" v-if="props.auditType == 'teacher' && selectedTab == '进行中'">
          <el-button type="primary" @click="batchAudit" v-hasPermi="[props.permiString]">批量通过</el-button>
        </el-col>
        <el-tabs v-model="selectedTab" @tab-change="qiehuan">
          <el-tab-pane label="进行中" name="进行中"> </el-tab-pane>
          <el-tab-pane label="已结束" name="已结束"> </el-tab-pane>
        </el-tabs>
        <el-table
          v-loading="loading"
          :data="userList"
          @selection-change="handleSelectionChange"
          style="width: 100%"
        >
          <el-table-column type="selection" width="50" align="center" v-if="props.auditType == 'teacher' && selectedTab == '进行中'"/>
          <el-table-column
            label="序号"
            align="center"
            width="50"
            type="index"
          />
          <el-table-column
            label="审核标题"
            align="left"
            key="auditTitle"
            prop="auditTitle"
            min-width="136"
          />
          <el-table-column
            label="类型"
            align="left"
            key="auditType"
            prop="auditType"
            width="80"
          >
            <template #default="scope">
              <dict-tag
                :options="audit_flow_type"
                :value="scope.row.auditType"
              />
            </template>
          </el-table-column>
          <el-table-column
            label="所在学校"
            align="left"
            key="teacherSchoolName"
            prop="teacherSchoolName"
            v-if="props.auditType == 'teacher'"
            min-width="110"
          >
          </el-table-column>
           <el-table-column
            label="已开通人数"
            align="center"
            key="schoolTeacherCount"
            prop="schoolTeacherCount"
            v-if="props.auditType == 'teacher'"
            width="90"
            show-overflow-tooltip
          >
          <template #default="scope">
            <el-popover placement="right" :width="300">
              <el-table :data="scope.row?.schoolTeacherNames || []" 
                        style="width: 100%" 
                        height="400" 
                        border>
                <el-table-column type="index" label="序号" align="left" width="55" />
                <el-table-column prop="realName" label="已开通教师姓名" align="left" show-overflow-tooltip> </el-table-column>
                <el-table-column prop="identityTime" label="开通时间" align="left" show-overflow-tooltip width="100"></el-table-column>
              </el-table>
              <template #reference>
                <div class="text-num-wrapper">
                  <el-icon><View /></el-icon>
                  <span class="text-num">{{ scope.row.schoolTeacherCount || 0 }}</span>
                </div>
              </template>
            </el-popover>
          </template>
          </el-table-column>
          <el-table-column
            label="事项标题"
            align="left"
            key="businessName"
            prop="businessName"
            min-width="120"
            show-overflow-tooltip
          >
          </el-table-column>
          <el-table-column
            label="提交人"
            align="left"
            key="subPer"
            prop="subPer"
            min-width="80"
          />
          <el-table-column
            label="提交时间"
            align="left"
            key="subTime"
            prop="subTime"
            width="158"
            show-overflow-tooltip
          />

          <!-- <el-table-column label="当前审核环节" align="center" key="levelName" prop="levelName"
            :show-overflow-tooltip="true" />
          <el-table-column label="审核人员" align="center" key="checkPer" prop="checkPer" v-if="selectedTab == '已结束'">
          </el-table-column> -->
          <el-table-column
            label="审核结果"
            align="left"
            key="checkStatus"
            prop="checkStatus"
            width="78"
            v-if="selectedTab == '已结束'"
          >
            <template #default="scope">
              <dict-tag
                :options="check_status"
                :value="scope.row.checkStatus"
              />
            </template>
          </el-table-column>
          <el-table-column
            label="审核时间"
            align="left"
            key="checkTime"
            prop="checkTime"
            width="158"
            show-overflow-tooltip
            v-if="selectedTab == '已结束'"
          >
            <template #default="scope">
              <div class="text-ellipsis">
                {{ scope.row.checkTime }}
              </div>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            min-width="120"
            fixed="right"
            align="center"
            class-name="small-padding fixed-width"
          >
            <template #default="scope">
              <template v-if="scope.row?.auditType == 'teacher'">
                <el-button link type="primary" 
                          @click="showPicker(scope.row)" 
                          v-hasPermi="[props.permiString]"
                          class="out-line"
                  >查看图片</el-button
                >
              </template>
              <el-button
                link
                v-if="selectedTab == '进行中'"
                type="primary"
                @click="handleUpdate(scope.row)"
                v-hasPermi="[props.permiString]"
                >审核</el-button
              >

              <el-button link type="primary" @click="chakan(scope.row)"  v-hasPermi="[props.permiString]"
                >查看</el-button
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
      </el-col>
    </el-row>

    <!-- 添加或修改用户配置对话框 -->
    <el-dialog
      :title="title"
      v-model="open"
      :width="form.auditType === 'chapterVideo' ? '1600px' : '1450px'"
    >
      <el-form :model="form" :rules="rules" ref="userRef">
        <el-row v-if="form.auditType !== 'chapterVideo'">
          <el-col :span="18">
            <div class="userRefEditForm" v-if="open">
              <EditForm
                v-if="form.auditType == 'race' && open"
                v-model:open="open"
                :competition-id="currentCompetitionId"
                :competition-series-id="currentCompetitionSeriesId"
                :competition-type-arr="competition_type"
                :join-type-arr="join_type"
                :businessDetail="form.businessDetail"
                :class-request-arr="class_request"
                :score-way-arr="score_way"
                :competition-track-arr="competition_track"
                :competition-group-arr="competition_group"
                :awards-name-arr="awards_name"
                :file-format-restrictions-arr="file_format_restrictions"
                :works-submit-way-arr="works_submit_way"
                :professional-requirements-arr="professional_requirements"
                :only-show="true"
                :noDialog="true"
              />
              <sdEditForm
                v-if="form.auditType == 'raceTrack' && open"
                v-model:open="open"
                :join-type-arr="join_type"
                :class-request-arr="class_request"
                :only-show="true"
                :checkPackageList="checkPackageList"
                :file-format-restrictions-arr="file_format_restrictions"
                :works-submit-way-arr="works_submit_way"
                :professional-requirements-arr="professional_requirements"
                :competition-track-type-arr="competition_track_type"
                :noDialog="true"
                :row="form.businessDetail"
              />
              <editContent
                v-if="form.auditType == 'page' && open"
                :pageId="form.businessDetail.pageId"
                :businessDetail="form.businessDetail"
                :displayPlatform="form.businessDetail.displayPlatform"
                :pageStatus="false"
                :preview="true"
              />
              <SignUpDetail
                v-if="form.auditType == 'apply' && open"
                :info="form.businessDetail"
                :professionalRequirements="professional_requirements"
                :classRequest="class_request"
                :checkStatus="check_status"
                :competitionTypeArr="competition_type"
                :realNameAuthStatusArr="real_name_auth_status"
                :joinTypeArr="join_type"
                :disabled="true"
              />
              <!-- 教师身份认证 -->
              <useredit
                :xiangqing="form.businessDetail"
                :certification_type="certification_type"
         
                :class_info="class_info"
                v-if="form.auditType == 'teacher' && open"
              />
              <!-- 学生身份认证 -->
              <useredit
                :xiangqing="form.businessDetail"
                :certification_type="certification_type"
          
                :class_info="class_info"
                v-if="form.auditType == 'student' && open"
              />
              <!-- 学校认证 -->
              <useredit
                :xiangqing="form.businessDetail"
                :certification_type="certification_type"
             
                :class_info="class_info"
                v-if="form.auditType == 'school' && open"
              />
              <!-- 企业认证 -->
              <useredit
                :xiangqing="form.businessDetail"
                :certification_type="certification_type"
           
                :class_info="class_info"
                v-if="form.auditType == 'enterprise' && open"
              />
              <TeamDetail
                v-model:info="form.businessDetail"
                :checkStatus="check_status"
                :competitionTypeArr="competition_type"
                :disabled="true"
                v-if="form.auditType == 'team' && open"
              />

              <zixun
                :form="form.businessDetail"
                v-if="form.auditType == 'info' && open"
              />
              <gonggao
                :form="form.businessDetail"
                :disp="true"
                v-if="form.auditType == 'notice' && open"
              />
              <courseDetail
                :form="form.businessDetail || form"
                v-if="form.auditType == 'course' && open"
              />
              <!-- 实名认证 -->
              <shiming
                :form="form.businessDetail || form"
                v-if="form.auditType == 'realName' && open"
              />
            </div>

            <div
              style="margin-top: 40px; padding: 0 16px"
              v-if="title == '审核'"
            >
              <el-form-item label="审核意见">
                <el-input
                  v-model="form.checkOpinion"
                  type="textarea"
                  maxlength="100"
                  show-word-limit
                  placeholder="请输入审核意见"
                />
              </el-form-item>
            </div>
          </el-col>
          <el-col :span="5" style="margin-left: 30px">
            <!-- 审批节点信息 -->
            <el-card header="流转记录" class="box-card" shadow="hover">
              <el-timeline>
                <el-timeline-item
                  v-for="(item, index) in historyProcNodeList"
                  :key="index"
                  :color="setColor(item.color)"
                  placement="top"
                >
                  <div class="timelinediyi">
                    <div
                      :class="
                        item.color != 'gray' ? 'yiyoujiedian' : 'weiyongjiedian'
                      "
                    >
                      {{ item.nodeName }}
                    </div>
                    <div class="adminjiedian">
                      <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="此节点已由管理员介入"
                        placement="top-start"
                      >
                        <img
                          src="@/assets/images/tanhao.png"
                          alt=""
                          v-if="item.adminIntervention == 'Y'"
                        />
                      </el-tooltip>
                    </div>
                    <div v-if="item.checkStatus" class="chestatu">
                      <dict-tag
                        :options="check_status"
                        :value="item.checkStatus"
                      />
                    </div>
                  </div>
                  <div style="clear: both"></div>
                  <div class="shenheren">
                    <!-- <div
                      v-for="(x, i) in item.checkPerson?.split(',')"
                      :key="i"
                    >
                      {{ x }}
                    </div> -->
                    {{ item.checkPerson }}
                  </div>
                  <div class="shijian">
                    {{ parseTime(item.checkTime) }}
                  </div>

                  <div class="shenheyijian" v-if="item.checkOpinion">
                    {{ item.checkOpinion }}
                  </div>
                </el-timeline-item>
              </el-timeline>
            </el-card>
          </el-col>
        </el-row>
        <!-- 视频审核特殊布局 -->
        <el-row v-if="form.auditType === 'chapterVideo'">
          <el-col :span="16">
            <chapterVideo
              :form="form"
              :isAudit="title == '审核'"
              ref="chapterVideoRef"
              v-if="open"
              @video-approve="handleVideoApprove"
              @video-reject="handleVideoReject"
            />
          </el-col>
          <el-col :span="7" style="margin-left: 30px">
            <!-- 审批节点信息 -->
            <el-card header="流转记录" class="box-card" shadow="hover">
              <el-timeline>
                <el-timeline-item
                  v-for="(item, index) in historyProcNodeList"
                  :key="index"
                  :color="setColor(item.color)"
                  placement="top"
                >
                  <div class="timelinediyi">
                    <div
                      :class="
                        item.color != 'gray' ? 'yiyoujiedian' : 'weiyongjiedian'
                      "
                    >
                      {{ item.nodeName }}
                    </div>
                    <div class="adminjiedian">
                      <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="此节点由管理员介入审核"
                        placement="top-start"
                      >
                        <img
                          src="@/assets/images/tanhao.png"
                          alt=""
                          v-if="item.adminIntervention == 'Y'"
                        />
                      </el-tooltip>
                    </div>
                    <div v-if="item.checkStatus" class="chestatu">
                      <dict-tag
                        :options="check_status"
                        :value="item.checkStatus"
                      />
                    </div>
                  </div>
                  <div style="clear: both"></div>
                  <div class="shenheren">
                    <!-- <div
                      v-for="(x, i) in item.checkPerson?.split(',')"
                      :key="i"
                    >
                      {{ x }}
                    </div> -->
                    {{ item.checkPerson }}
                  </div>
                  <div class="shijian">
                    {{ parseTime(item.checkTime) }}
                  </div>

                  <div class="shenheyijian" v-if="item.checkOpinion">
                    {{ item.checkOpinion }}
                  </div>
                </el-timeline-item>
              </el-timeline>
            </el-card>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div
          class="dialog-footer"
          v-if="title == '审核' && form.auditType !== 'chapterVideo'"
        >
          <el-button type="primary" @click="submitForm(4)">通过</el-button>
          <el-button type="danger" @click="submitForm(5)">驳回</el-button>
          <el-button @click="open = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>

  <!-- 图片预览组件 -->
  <el-image-viewer
    v-if="showPreview"
    :url-list="previewImageList"
    :initial-index="0"
    :close-on-press-escape="true"
    :hide-on-click-modal="true"
    @close="showPreview = false">
    <template #progress>
      <span class="preview-text">按 ESC 键、点击图片外区域、点击关闭按钮退出预览</span>
    </template>
  </el-image-viewer>
</template>

<script setup name="User">
import { ref } from 'vue'
import { ElImageViewer } from 'element-plus'
import { getCheckPackage } from "@/api/tournament/competition"
import {
  tasklist,
  taskfinish,
  taskroleId,
  taskaudit,
  taskVideoAudit,
  taskgetUser,
} from "@/api/system/process.js";
import { getTaskPic,sendTaskAudit } from "@/api/system/process.js";
import { parseTime } from "@/utils/ruoyi";
// 赛事
import EditForm from "@/views/tournament/competition/editForm.vue";
// 页面详情
import editContent from "@/views/content/page/editContent/index.vue";

// 报名详情
import SignUpDetail from "@/views/tournament/signUp/detail.vue";

// 用户身份详情
import useredit from "@/views/system/authentication/components/edit.vue";
// 团队
import TeamDetail from "@/views/tournament/team/detail.vue";
// 咨询
import zixun from "@/components/Reviewtask/zixun.vue";
// 公告
import gonggao from "@/components/Reviewtask/gonggao.vue";
// 实名认证
import shiming from "@/components/Reviewtask/shiming.vue";
// 视频审核
import chapterVideo from "@/components/Reviewtask/chapterVideo.vue";
// 课程详情
import courseDetail from "@/components/Reviewtask/courseDetail.vue";
// 赛道
import sdEditForm from "@/views/tournament/competitionSetting/editForm.vue";
import { resetCompetitionDetailState } from "@/views/tournament/competition/editComponents/useCompetitionDetail.js";
import modal from "@/plugins/modal";
const props = defineProps({
  auditType: {
    type: String,
    default: null,
  },
  permiString: {
    type: [Number, String],
    default: null,
  },
});
// const shenheren = ref([]);
// // 获取审核人
// const taskgetUserlist = () => {
//   if (queryParams.value.auditType) {
//     taskgetUser(queryParams.value.auditType).then((res) => {
//       shenheren.value = res.data;
//     });
//   } else {
//     taskgetUser("system").then((res) => {
//       shenheren.value = res.data;
//     });
//   }
// };

// 图片预览相关
const showPreview = ref(false);
const previewImageList = ref([]);

// 查看图片
const showPicker = (item) => {
  getTaskPic(item.taskId).then((res) => {
    let imgurl = res.data;
    // 设置预览图片列表
    previewImageList.value = [imgurl];
    // 显示图片预览
    showPreview.value = true;
  });
};

const router = useRouter();

const { proxy } = getCurrentInstance();
const {
  audit_flow_type,
  check_status,
  competition_type,
  certification_type,
  class_info,
  awards_name,
  real_name_auth_status,
  competition_track_type,
} = proxy.useDict(
  "audit_flow_type",
  "check_status",
  "competition_type",
  "certification_type",
  "class_info",
  "awards_name",
  "real_name_auth_status",
  "competition_track_type"
);
const userList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const selectRows = ref([]);
const total = ref(0);
const title = ref("");
const initPassword = ref(undefined);
const userRef = ref(null);
const currentAuditConfigId = ref(null); // 存储当前审核的 auditConfigId
function setIcon(val) {
  if (val) {
    return "el-icon-time";
  } else {
    return "el-icon-check";
  }
}

function setColor(val) {
  if (val == "green") {
    return "#2bc418";
  } else if (val == "gray") {
    return "";
  } else if (val == "yellow") {
    return "#fbca0c";
  } else if (val == "red") {
    return "red";
  }
}
const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
  rules: {
    userName: [
      { required: true, message: "用户名称不能为空", trigger: "blur" },
      {
        min: 2,
        max: 20,
        message: "用户名称长度必须介于 2 和 20 之间",
        trigger: "blur",
      },
    ],
    password: [
      { required: true, message: "用户密码不能为空", trigger: "blur" },
      {
        min: 5,
        max: 20,
        message: "用户密码长度必须介于 5 和 20 之间",
        trigger: "blur",
      },
      {
        pattern: /^[^<>"'|\\]+$/,
        message: "不能包含非法字符：< > \" ' \\ |",
        trigger: "blur",
      },
    ],
    email: [
      {
        type: "email",
        message: "请输入正确的邮箱地址",
        trigger: ["blur", "change"],
      },
    ],
    phonenumber: [
      {
        pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
        message: "请输入正确的手机号码",
        trigger: "blur",
      },
    ],
  },
});

const { queryParams, form, rules } = toRefs(data);

const selectedTab = ref("进行中");
const qiehuan = (tab) => {
  handleQuery()
};
/** 查询用户列表 */
function getList() {
  loading.value = true;
  queryParams.value.auditType = props.auditType
    ? props.auditType
    : queryParams.value.auditType;

  if (selectedTab.value == "进行中") {
    tasklist(proxy.addDateRange(queryParams.value)).then((res) => {
      loading.value = false;
      userList.value = res.rows;
      total.value = res.total;
    });
  } else {
    taskfinish(proxy.addDateRange(queryParams.value)).then((res) => {
      loading.value = false;
      userList.value = res.rows;
      total.value = res.total;
    });
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRefsss");
  queryParams.value.deptId = undefined;
queryParams.value.checkStatus = undefined;
  handleQuery();
}

/** 选择条数  */
function handleSelectionChange(selection) {
  selectRows.value = selection;
}
/** 批量审核 */
function batchAudit() {
  if (!selectRows.value || selectRows.value.length == 0) {
    modal.msgWarning("请先选择要批量审核的任务");
    return;
  }
  console.log(selectRows.value);
  let isAdmin = selectRows.value.some((item) => item.adminAccessing == "Y");
  modal.prompt(isAdmin ? "是否确认介入审核，将所选人员状态更新为“已通过”？" : "是否确定将所选人员状态更新为“已通过”？",'系统提示',{
    inputPlaceholder: "请输入审核通过意见",
    type: "textarea",
  }).then(({value })=>{
      let sendApiData = selectRows.value.map((item) => {
        return {
          adminIntervention: item.adminAccessing,
          taskId: item.taskId,
          checkOpinion: value,
          checkStatus: 4,
          auditConfigId: item.nowCheckStep || item.taskId,
        };
      });
      sendTaskAudit(sendApiData).then((response) => {
        modal.msgSuccess("批量通过成功");
        getList();
      });
  }).catch(() => {});
}

/** 重置操作表单 */
function reset() {
  form.value = {
    taskId: undefined,
    deptId: undefined,
    userName: undefined,
    password: undefined,
    phonenumber: undefined,
    email: undefined,
    sex: undefined,
    status: "0",
    remark: undefined,
    postIds: [],
    roleIds: [],
  };
  proxy.resetForm("userRef");
}

// 流转记录
const historyProcNodeList = ref([]);

// 获取用户详细信息
// 字典数据
const {
  join_type,
  class_request,
  score_way,
  works_submit_way,
  professional_requirements,
  file_format_restrictions,
  competition_track,
  competition_group,
} = proxy.useDict(
  "join_type",
  "class_request",
  "score_way",
  "professional_requirements",
  "works_submit_way",
  "file_format_restrictions",
  "competition_track",
  "competition_group"
);
const currentCompetitionId = ref(undefined);
const currentCompetitionSeriesId = ref(undefined);
const chapterVideoRef = ref(null);
const chakan = (row) => {
  reset();
  form.value = {};
  const taskId = row.taskId;
  taskroleId(taskId).then((res) => {
    open.value = true;
    title.value = "查看";
    form.value = res.data;
    historyProcNodeList.value = res.data.reviewProcess;
    if (form.value.auditType == "race") {
      resetCompetitionDetailState(res.data.businessDetail);
      currentCompetitionId.value = res.data.businessDetail.competitionId;
      currentCompetitionSeriesId.value =
        res.data.businessDetail.competitionSeriesId;
    }
  });
};

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const taskId = row.taskId;
  taskroleId(taskId).then((res) => {
    open.value = true;
    title.value = "审核";
    form.value = res.data;
    historyProcNodeList.value = res.data.reviewProcess;
    // 设置当前审核的 auditConfigId（使用 taskId 作为 auditConfigId）
    currentAuditConfigId.value = res.data.nowCheckStep || taskId;
    if (queryParams.value.auditType == "race") {
      resetCompetitionDetailState(res.data.businessDetail);
      currentCompetitionId.value = res.data.businessDetail.competitionId;
      currentCompetitionSeriesId.value =
        res.data.businessDetail.competitionSeriesId;
    }
  });
}

/** 提交按钮 */
function submitForm(item) {
  // 视频审核特殊处理
  if (form.value.auditType === "chapterVideo") {
    if (!chapterVideoRef.value) {
      proxy.$modal.msgError("获取视频审核信息失败");
      return;
    }
    // 检查审核意见
    if (!form.value.checkOpinion || form.value.checkOpinion.trim() === "") {
      proxy.$modal.msgError("请填写审核意见");
      return;
    }

    const chapterAuditResult = chapterVideoRef.value.getAuditResult(item);
    if (
      !chapterAuditResult ||
      !chapterAuditResult.pageInfo ||
      chapterAuditResult.pageInfo.length === 0
    ) {
      proxy.$modal.msgError("没有可审核的视频");
      return;
    }

    // 所有视频使用相同的审核状态（通过或驳回）
    submitVideoAudit(item, chapterAuditResult);
  } else {
    // 普通审核
    console.log(form.value);
    userRef.value.validate((valid) => {
      if (valid) {
        const params = {
          taskId: form.value.taskId,
          checkOpinion: form.value.checkOpinion,
          checkStatus: item,
          auditConfigId: currentAuditConfigId.value,
        };
        if (form.value.adminAccessing == "Y") {
          modal
            .confirm("是否确认介入审核？")
            .then(function () {
              params.adminIntervention = "Y";
              taskaudit(params).then((response) => {
                proxy.$modal.msgSuccess("操作成功");
                open.value = false;
                getList();
              });
            })
            .catch(() => {});
        } else {
          taskaudit(params).then((response) => {
            proxy.$modal.msgSuccess("操作成功");
            open.value = false;
            getList();
          });
        }
      }
    });
  }
}

/** 处理单个视频通过 */
function handleVideoApprove(auditData) {
  submitSingleVideoAudit(auditData);
}

/** 处理单个视频驳回 */
function handleVideoReject(auditData) {
  submitSingleVideoAudit(auditData);
}

/** 提交单个视频审核 */
function submitSingleVideoAudit(auditData) {
  // 确保 pageInfo 包含 auditConfigId
  const pageInfo = auditData.pageInfo || {};
  if (!pageInfo.auditConfigId) {
    pageInfo.auditConfigId = currentAuditConfigId.value;
  }

  const params = {
    taskId: form.value.taskId,
    auditConfigId: currentAuditConfigId.value,
    chapterAuditResult: {
      chapterId: form.value.businessDetail?.chapterId,
      pageInfo: [pageInfo],
    },
  };

  taskVideoAudit(params)
    .then((response) => {
      proxy.$modal.msgSuccess("审核成功");
      // 重新获取当前审核任务的详细信息，刷新审核对话框
      const taskId = form.value.taskId;
      taskroleId(taskId)
        .then((res) => {
          form.value = res.data;
          // 恢复 auditConfigId
          currentAuditConfigId.value =
            res.data.nowCheckStep || currentAuditConfigId.value;
          historyProcNodeList.value = res.data.reviewProcess;
          // 检查是否还有待审核的视频
          const videoData =
            res.data.courseChapterVideos ||
            (res.data.businessDetail &&
              res.data.businessDetail.courseChapterVideos) ||
            (res.data.businessDetail &&
              res.data.businessDetail.chapterVideoList) ||
            (res.data.businessDetail && res.data.businessDetail.videoList) ||
            res.data.chapterVideoList ||
            res.data.videoList ||
            [];

          // 过滤掉已审核的视频（checkStatus 为 4-已通过 或 5-已拒绝）
          const pendingVideos = videoData.filter((item) => {
            const checkStatus = item.checkStatus;
            // 只保留待审核的视频（checkStatus 为 2-待审核、3-审核中，或者没有 checkStatus）
            return (
              !checkStatus ||
              checkStatus === "2" ||
              checkStatus === "3" ||
              checkStatus === 2 ||
              checkStatus === 3
            );
          });

          // 如果没有待审核的视频了，自动提交任务审核（通过）来结束审核任务
          if (!pendingVideos || pendingVideos.length === 0) {
            // 自动提交任务审核，状态设为通过（4）
            const auditParams = {
              taskId: form.value.taskId,
              checkOpinion: form.value.checkOpinion || "视频审核已完成",
              checkStatus: 4, // 通过
              auditConfigId: currentAuditConfigId.value,
            };
            taskaudit(auditParams)
              .then(() => {
                proxy.$modal.msgSuccess("视频审核完成，审核任务已结束");
                // 先刷新列表，再关闭对话框
                getList().then(() => {
                  open.value = false;
                });
              })
              .catch((error) => {
                proxy.$modal.msgError(
                  "自动提交审核失败：" + (error.msg || "未知错误")
                );
                // 即使失败也刷新列表
                getList().then(() => {
                  open.value = false;
                });
              });
          }
        })
        .catch((error) => {
          console.error("刷新审核任务详情失败:", error);
          // 即使刷新失败，也刷新列表
          getList().then(() => {
            open.value = false;
          });
        });
    })
    .catch((error) => {
      proxy.$modal.msgError(error.msg || "审核失败");
    });
}

/** 提交视频审核 */
function submitVideoAudit(checkStatus, chapterAuditResult) {
  // 确保每个视频都有 auditConfigId
  if (
    chapterAuditResult &&
    chapterAuditResult.pageInfo &&
    Array.isArray(chapterAuditResult.pageInfo)
  ) {
    chapterAuditResult.pageInfo.forEach((pageInfo) => {
      if (!pageInfo.auditConfigId) {
        pageInfo.auditConfigId = currentAuditConfigId.value;
      }
    });
  }

  const params = {
    taskId: form.value.taskId,
    auditConfigId: currentAuditConfigId.value,
    checkStatus: checkStatus, // 整体审核状态
    chapterAuditResult: chapterAuditResult,
  };

  taskVideoAudit(params)
    .then((response) => {
      proxy.$modal.msgSuccess("操作成功");
      // 重新获取当前审核任务的详细信息，刷新审核对话框
      const taskId = form.value.taskId;
      taskroleId(taskId)
        .then((res) => {
          form.value = res.data;
          // 恢复 auditConfigId
          currentAuditConfigId.value =
            res.data.nowCheckStep || currentAuditConfigId.value;
          historyProcNodeList.value = res.data.reviewProcess;
          // 检查是否还有待审核的视频
          const videoData =
            res.data.courseChapterVideos ||
            (res.data.businessDetail &&
              res.data.businessDetail.courseChapterVideos) ||
            (res.data.businessDetail &&
              res.data.businessDetail.chapterVideoList) ||
            (res.data.businessDetail && res.data.businessDetail.videoList) ||
            res.data.chapterVideoList ||
            res.data.videoList ||
            [];

          // 过滤掉已审核的视频（checkStatus 为 4-已通过 或 5-已拒绝）
          const pendingVideos = videoData.filter((item) => {
            const checkStatus = item.checkStatus;
            // 只保留待审核的视频（checkStatus 为 2-待审核、3-审核中，或者没有 checkStatus）
            return (
              !checkStatus ||
              checkStatus === "2" ||
              checkStatus === "3" ||
              checkStatus === 2 ||
              checkStatus === 3
            );
          });

          // 如果没有待审核的视频了，自动提交任务审核（通过）来结束审核任务
          if (!pendingVideos || pendingVideos.length === 0) {
            // 自动提交任务审核，状态设为通过（4）
            const auditParams = {
              taskId: form.value.taskId,
              checkOpinion: form.value.checkOpinion || "视频审核已完成",
              checkStatus: 4, // 通过
              auditConfigId: currentAuditConfigId.value,
            };
            taskaudit(auditParams)
              .then(() => {
                proxy.$modal.msgSuccess("视频审核完成，审核任务已结束");
                // 先刷新列表，再关闭对话框
                getList().then(() => {
                  open.value = false;
                });
              })
              .catch((error) => {
                proxy.$modal.msgError(
                  "自动提交审核失败：" + (error.msg || "未知错误")
                );
                // 即使失败也刷新列表
                getList().then(() => {
                  open.value = false;
                });
              });
          }
        })
        .catch((error) => {
          console.error("刷新审核任务详情失败:", error);
          // 即使刷新失败，也刷新列表
          getList().then(() => {
            open.value = false;
          });
        });
    })
    .catch((error) => {
      proxy.$modal.msgError(error.msg || "操作失败");
    });
}
/** 查询数据包 */
let checkPackageList = $ref([])
function getCheckPackageList() {
  let query = {}
  getCheckPackage(query).then(response => {
    checkPackageList = response.data || []
  })
}

if(!props.auditType || props.auditType === 'raceTrack'){
  // 赛道专用数据包
  getCheckPackageList()
}
onMounted(() => {
  getList();
  proxy.getConfigKey("sys.user.initPassword").then((response) => {
    initPassword.value = response.msg;
  });
});
</script>
<style lang="scss" scoped>
.userRefEditForm {
  :deep(.el-modal-dialog) {
    position: relative !important;
    .el-overlay-dialog {
      position: relative !important;
      .el-dialog {
        width: 100%;
        margin: 0 !important;
        .el-dialog__header {
          display: none;
          padding: 0;
        }
      }
    }
  }
}
.text-ellipsis {
  white-space: nowrap; /* 不换行 */
  overflow: hidden; /* 超出部分隐藏 */
  text-overflow: ellipsis; /* 溢出部分显示省略号（可选） */
}
:deep(.el-timeline) {
  padding: 0;
}
:deep(.el-timeline-item__content) {
  position: relative;
  top: -10px;
}

.dialog-footer {
  text-align: center;
}
.timelinediyi {
  .yiyoujiedian {
    color: #000;
    float: left;
    font-size: 13px;
    max-width: 115px;
    white-space: nowrap; /* 禁止换行 */
    overflow: hidden; /* 隐藏溢出内容 */
    text-overflow: ellipsis; /* 溢出部分用省略号表示 */
  }
  .weiyongjiedian {
    color: #999999;
    float: left;
    font-size: 13px;
    max-width: 115px;
    white-space: nowrap; /* 禁止换行 */
    overflow: hidden; /* 隐藏溢出内容 */
    text-overflow: ellipsis; /* 溢出部分用省略号表示 */
  }
  .adminjiedian {
    float: left;
    img {
      width: 14px;
      margin-top: 2px;
    }
  }
  .chestatu {
    float: right;
  }
}
.shenheren {
  max-width: 166px;
  white-space: nowrap; /* 禁止换行 */
  overflow: hidden; /* 隐藏溢出内容 */
  text-overflow: ellipsis; /* 溢出部分用省略号表示 */
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 12px;
  color: #000000;
  line-height: 17px;
  text-align: left;
  font-style: normal;
  text-transform: none;
  margin-top: 2px;
}
.shijian {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 12px;
  color: #b2b4bb;
  line-height: 17px;
  text-align: left;
  font-style: normal;
  text-transform: none;
  margin-top: 5px;
}
.shenheyijian {
  width: 230px;
  min-height: 32px;
  background: #f5f5f5;
  border-radius: 6px 6px 6px 6px;
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 12px;
  color: #666666;
  line-height: 16px;
  text-align: left;
  font-style: normal;
  text-transform: none;
  padding: 0 10px;
  margin-top: 5px;

  //  display: -webkit-box;
  // -webkit-box-orient: vertical;
  // -webkit-line-clamp: 2; /* 设置最多显示的行数 */
  // overflow: hidden;
  // text-overflow: ellipsis;

  max-height: 52px;

  overflow: auto;
}
.text-num-wrapper {
  color: #409EFF;
  display: flex;
  justify-content: center;
  align-items: center;
  .text-num {
    margin-left: 2px;
  }
}
.out-line{
  outline: none !important;
}
.preview-text{
  font-size: 12px;
}
</style>
