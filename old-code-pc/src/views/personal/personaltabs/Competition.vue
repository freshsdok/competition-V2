<template>
  <div class="zong">
    <div class="teacher-credential-panel">
      <div class="teacher-panel-head">
        <div>
          <div class="teacher-panel-title">指导学生参赛证</div>
          <div class="teacher-panel-sub">仅展示当前账号指导团队/学生的参赛证信息</div>
        </div>
        <el-button
          plain
          :loading="teacherCredentialLoading"
          @click="teacherCredentialGetList"
        >
          刷新
        </el-button>
      </div>

      <div v-if="teacherCredentialGroups.length === 0 && !teacherCredentialLoading" class="teacher-empty">
        <el-empty description="暂无指导学生参赛证"></el-empty>
      </div>
      <div v-else class="teacher-team-list">
        <div
          v-for="group in teacherCredentialGroups"
          :key="group.key"
          class="teacher-team-block"
        >
          <div class="teacher-team-head">
            <div>
              <div class="teacher-team-title">{{ group.competitionName }}</div>
              <div class="teacher-team-sub">
                {{ group.teamName || group.teamCode || '-' }} · {{ group.groupName || '-' }}
              </div>
            </div>
            <el-tag effect="plain">{{ group.items.length }} 条记录</el-tag>
          </div>
          <el-table
            :data="group.items"
            size="small"
            class="teacher-credential-table"
          >
            <el-table-column label="学生" min-width="120">
              <template #default="scope">
                {{ scope.row.studentName || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="角色" min-width="100">
              <template #default="scope">
                {{ targetRoleLabel(scope.row.roleName) }}
              </template>
            </el-table-column>
            <el-table-column label="组别" min-width="140">
              <template #default="scope">
                {{ joinText([scope.row.studentGroupName || scope.row.groupName]) }}
              </template>
            </el-table-column>
            <el-table-column label="证件状态" min-width="120">
              <template #default="scope">
                <el-tag
                  :type="teacherCredentialStatusType(scope.row.credentialStatus)"
                  effect="plain"
                >
                  {{ teacherCredentialStatusLabel(scope.row.credentialStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="现场状态" min-width="180">
              <template #default="scope">
                <div class="teacher-status-line">
                  <span :class="{ done: isReportDone(scope.row) }">
                    {{ isReportDone(scope.row) ? '已报到' : '未报到' }}
                  </span>
                  <span :class="{ done: isMaterialDone(scope.row) }">
                    {{ isMaterialDone(scope.row) ? '已领资料' : '未领资料' }}
                  </span>
                  <span :class="{ done: isWaitingDone(scope.row) }">
                    {{ isWaitingDone(scope.row) ? '已候场' : '未候场' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="scope">
                <el-button
                  v-if="scope.row.credentialId"
                  type="primary"
                  link
                  :loading="teacherCredentialDetailLoading"
                  @click="openTeacherCredentialDetail(scope.row)"
                >
                  查看参赛证
                </el-button>
                <span v-else class="teacher-no-credential">未生成参赛证</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <div
      v-if="displayCompetitionList.length === 0 && !credentialLoading"
      class="zhanwu"
    >
      <el-empty description="暂无数据"></el-empty>
    </div>
    <div v-else>
      <div
        v-for="(item, index) in displayCompetitionList"
        :key="item._credentialGroupKey || item.competitionSeriesId || index"
      >
        <div class="competition-row">
          <div class="neirong">
            <div class="neiimg">
              <img
                v-if="item.competitionImage"
                :src="item.competitionImage"
                alt=""
                :style="{ cursor: canOpenCompetitionDetail(item) ? 'pointer' : 'default' }"
                @click="goCompetitionDetail(item)"
              />
              <div v-else class="competition-placeholder">
                {{ competitionCoverText(item) }}
              </div>
            </div>
            <div class="neitext">
              <div
                class="title"
                :style="{ cursor: canOpenCompetitionDetail(item) ? 'pointer' : 'default' }"
                @click="goCompetitionDetail(item)"
              >
                {{ item.competitionName }}{{ item.stageName }}
              </div>
              <div class="price">{{ competitionDescText(item) }}</div>
              <div class="bq">
                <div style="display: flex">
                  <dict-tag
                    v-if="item.joinType"
                    :options="join_type"
                    :value="item.joinType"
                    style="margin-right: 10px"
                  />
                  <dict-tag
                    v-if="item.competitionType"
                    :options="competition_type"
                    :value="item.competitionType"
                  />
                </div>
              </div>
            </div>
          </div>
          <div class="credential-actions">
            <el-button
              type="primary"
              plain
              :loading="credentialLoading"
              @click="openCredentialDialog(item)"
            >
              现场证件
            </el-button>
            <div class="credential-tip" v-if="credentialCount(item)">
              可查看二维码和现场信息
            </div>
            <div class="credential-tip muted-tip" v-else-if="credentialLoading">
              加载中
            </div>
            <div class="credential-tip muted-tip" v-else>
              暂未生成
            </div>
          </div>
        </div>

        <el-divider />
      </div>
    </div>

    <el-dialog v-model="dialogchengji" title="赛事成绩" width="1200">
      <el-table :data="chengjilist" style="width: 100%">
        <el-table-column
          label="排名"
          align="center"
          prop="ranks"
          :show-overflow-tooltip="true"
        >
        </el-table-column>
        <el-table-column
          label="是否获奖"
          align="center"
          prop="isAward"
          :show-overflow-tooltip="true"
        >
        </el-table-column>
        <el-table-column
          label="奖项名称"
          align="center"
          prop="awardsName"
          :show-overflow-tooltip="true"
        >
        </el-table-column>
        <el-table-column
          label="奖项金额"
          align="center"
          prop="awardsMoney"
          :show-overflow-tooltip="true"
        >
        </el-table-column>
        <el-table-column
          label="证书地址"
          align="center"
          prop="certificateUrl"
          :show-overflow-tooltip="true"
        >
        </el-table-column>
        <el-table-column
          label="分析结果"
          align="center"
          prop="analyseResult"
          :show-overflow-tooltip="true"
        >
        </el-table-column>
        <el-table-column
          label="赛事名称"
          align="center"
          prop="competitionName"
          :show-overflow-tooltip="true"
          min-width="150"
        >
        </el-table-column>
        <el-table-column
          label="获奖人(组)"
          align="center"
          prop="ranks"
          :show-overflow-tooltip="true"
        >
          <template #default="scope">
            {{ scope.row.teamName ? scope.row.teamName : scope.row.userName }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="dialogchengji = false">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="shangchuanzuopin" title="上传作品" width="800px">
      <shangchuanzuopins
        v-if="shangchuanzuopin == true"
        :shangchuanxinxi="shangchuanxinxi"
        @sczuopin="emits"
      />
    </el-dialog>
    <el-dialog v-model="jiaofeijilu" title="我的订单" width="1200">
      <div class="bt">
        <el-row>
          <el-col :span="2" class="th"> 编号 </el-col>
          <el-col :span="6" class="th"> 缴费信息 </el-col>
          <el-col :span="5" class="th"> 缴费时间 </el-col>
          <el-col :span="5" class="th"> 缴费金额 </el-col>
          <el-col :span="6" class="th"> 交易状态 </el-col>
        </el-row>
      </div>

      <div class="centen">
        <el-row>
          <el-col :span="2" class="tr"> 1</el-col>
          <el-col :span="6" class="tr">
            {{ jiaofeilist.commodityName }}
          </el-col>
          <el-col :span="5" class="tr"> {{ jiaofeilist.payTime }} </el-col>
          <el-col :span="5" class="tr"> {{ jiaofeilist.amount }} </el-col>
          <el-col :span="6" class="tr" style="border-right: 1px solid #e4e4e4">
            <dict-tag :options="pay_status" :value="jiaofeilist.payStatus" />
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <el-dialog
      v-model="credentialDialogVisible"
      width="1040px"
      class="credential-dialog"
    >
      <template #header="{ titleId, titleClass }">
        <div :id="titleId" :class="['credential-dialog-title', titleClass]">
          <img
            class="credential-dialog-logo"
            src="@/assets/images/xiaotubiao.png"
            alt=""
          />
          <span>大学生新一代信息通信科技大赛参赛证</span>
        </div>
      </template>
      <div v-if="currentCredentials.length === 0" class="credential-empty">
        <el-empty description="暂无现场证件，请等待管理员生成"></el-empty>
      </div>
      <div v-else class="credential-dialog-body">
        <div
          class="competition-credential-hero"
          :class="{
            empty: !currentCompetitionCredential,
            reported: isReportDone(currentCompetitionCredential)
          }"
        >
          <div class="hero-head">
            <div class="hero-copy">
              <div class="hero-kicker">大赛现场证件</div>
              <div class="hero-title">
                {{ competitionHeroTitle(currentCompetition, currentCompetitionCredential) }}
              </div>
              <div class="hero-sub">
                {{ competitionHeroSub(currentCompetitionCredential) }}
              </div>
            </div>
            <el-tag
              v-if="currentCompetitionCredential"
              class="hero-role"
              effect="dark"
              :type="credentialStatusType(currentCompetitionCredential.credentialStatus)"
            >
              {{ credentialTypeLabel(currentCompetitionCredential.credentialType) }}
            </el-tag>
          </div>

          <div v-if="currentCompetitionCredential" class="hero-pass-panel">
            <template v-if="credentialQrValue(currentCompetitionCredential)">
              <div class="hero-qr-box">
                <QrcodeVue
                  :value="credentialQrValue(currentCompetitionCredential)"
                  :size="210"
                  :margin="2"
                  level="H"
                  render-as="canvas"
                />
              </div>
              <div class="hero-pass-copy">
                <div class="hero-pass-title">大赛证二维码</div>
                <div class="hero-pass-desc">现场报到、领取资料和身份核验时出示此二维码</div>
                <div class="hero-pass-status">
                  <span :class="{ done: isReportDone(currentCompetitionCredential) }">
                    {{ isReportDone(currentCompetitionCredential) ? '已报到' : '未报到' }}
                  </span>
                  <span :class="{ done: isMaterialDone(currentCompetitionCredential) }">
                    {{ isMaterialDone(currentCompetitionCredential) ? '已领取资料' : '未领取资料' }}
                  </span>
                </div>
              </div>
            </template>
            <div v-else class="hero-qr-missing">
              暂无大赛证二维码，请联系现场工作人员
            </div>
          </div>

          <div v-if="!currentCompetitionCredential" class="hero-empty-text">
            当前赛事暂无大赛总证
          </div>

          <div v-if="hasCurrentNotices" class="hero-notice-panel">
            <div class="hero-notice-head">
              <strong>现场通知</strong>
              <span>{{ currentNoticeCount }} 条</span>
            </div>

            <section v-if="currentNoticeGroup.personalNotices.length" class="notice-group">
              <div class="notice-group-title">个人通知</div>
              <article
                v-for="noticeItem in currentNoticeGroup.personalNotices"
                :key="noticeItem.noticeId"
                class="notice-item personal"
                :class="noticeLevelClass(noticeItem.noticeLevel)"
              >
                <div class="notice-item-head">
                  <strong>{{ noticeItem.title }}</strong>
                  <el-tag size="small" :type="noticeLevelType(noticeItem.noticeLevel)">
                    {{ noticeLevelLabel(noticeItem.noticeLevel) }}
                  </el-tag>
                </div>
                <div class="notice-rich-content" v-html="noticeItem.content"></div>
              </article>
            </section>

            <section v-if="currentNoticeGroup.announcements.length" class="notice-group">
              <div class="notice-group-title">大赛公告</div>
              <article
                v-for="noticeItem in currentNoticeGroup.announcements"
                :key="noticeItem.noticeId"
                class="notice-item announcement"
                :class="noticeLevelClass(noticeItem.noticeLevel)"
              >
                <div class="notice-item-head">
                  <strong>{{ noticeItem.title }}</strong>
                  <el-tag size="small" :type="noticeLevelType(noticeItem.noticeLevel)">
                    {{ noticeLevelLabel(noticeItem.noticeLevel) }}
                  </el-tag>
                </div>
                <div class="notice-rich-content" v-html="noticeItem.content"></div>
              </article>
            </section>
          </div>
        </div>

        <div class="scene-credential-section">
          <div class="scene-section-head">
            <div>
              <div class="scene-section-title">赛场信息</div>
              <div class="scene-section-sub">{{ currentSceneCredentials.length }} 个赛场</div>
            </div>
          </div>

          <div v-if="currentSceneCredentials.length === 0" class="scene-empty">
            暂无赛场信息
          </div>

          <div
            v-for="credential in currentSceneCredentials"
            :key="credentialKey(credential)"
            class="scene-credential-card"
          >
            <div class="scene-card-head" @click="toggleCredential(credential)">
              <div class="scene-card-main">
                <div class="scene-card-title">{{ scheduleTitle(credential) }}</div>
                <!-- <div class="scene-card-sub">{{ sceneCardSub(credential) }}</div> -->
                <div class="scene-card-meta">
                  {{ credentialDisplayName(credential) }} · {{ credential.credentialNo || '-' }}
                </div>
              </div>
              <div class="scene-card-side">
                <el-tag :type="credentialStatusType(credential.credentialStatus)">
                  {{ credentialStatusLabel(credential.credentialStatus) }}
                </el-tag>
                <span class="expand-arrow" :class="{ expanded: isCredentialExpanded(credential) }"></span>
              </div>
            </div>

            <div v-if="isCredentialExpanded(credential)" class="scene-card-body">
              <div class="scene-card-content">
                <div class="scene-info">
                  <div class="info-section">
                    <div class="section-title">参赛信息</div>
                    <div class="info-grid">
                      <div class="info-item">
                        <span>姓名</span>
                        <strong>{{ subjectName(credential) }}</strong>
                      </div>
                      <div class="info-item">
                        <span>赛道/组别</span>
                        <strong>{{ joinText([credential.competitionTrackName, credential.secondLevelName]) }}</strong>
                      </div>
                      <div class="info-item">
                        <span>学校/机构</span>
                        <strong>{{ credential.schoolName || credential.orgName || '-' }}</strong>
                      </div>
                      <div class="info-item">
                        <span>角色</span>
                        <strong>{{ targetRoleLabel(credential.competitionRoleName) }}</strong>
                      </div>
                      <!-- <div class="info-item">
                        <span>证件范围</span>
                        <strong>{{ scopeTypeLabel(credential.scopeType) }}</strong>
                      </div> -->
                    </div>
                  </div>

                  <div class="info-section">
                    <div class="section-title">现场安排</div>
                    <div class="schedule-line">
                      <span>赛场签到时间</span>
                      <strong>{{ formatRange(credential.reportStartTime, credential.reportEndTime) }}</strong>
                    </div>
                    <div class="schedule-line">
                      <span>赛场签到地点</span>
                      <strong>{{ credential.reportLocation || '-' }}</strong>
                    </div>
                    <div class="schedule-line">
                      <span>比赛时间</span>
                      <strong>{{ formatRange(credential.contestStartTime, credential.contestEndTime) }}</strong>
                    </div>
                    <div class="schedule-line">
                      <span>赛场地点</span>
                      <strong>{{ joinText([credential.contestLocation, credential.contestRoom]) }}</strong>
                    </div>
                    <div class="schedule-line">
                      <span>候场安排</span>
                      <strong>{{ waitingText(credential) }}</strong>
                    </div>
                    <div class="schedule-line">
                      <span>资料领取</span>
                      <strong>{{ credential.materialLocation || '-' }}</strong>
                    </div>
                  </div>

                  <div class="info-section" v-if="credential.notice">
                    <div class="section-title">注意事项</div>
                    <div class="notice-content">{{ credential.notice }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="teacherCredentialDialogVisible"
      width="760px"
      class="teacher-credential-dialog"
      title="指导学生参赛证"
    >
      <div v-if="teacherCredentialDetailData" class="teacher-detail-body">
        <div class="teacher-detail-main">
          <div class="teacher-detail-qr">
            <QrcodeVue
              v-if="credentialQrValue(teacherCredentialDetailData)"
              :value="credentialQrValue(teacherCredentialDetailData)"
              :size="180"
              :margin="2"
              level="H"
              render-as="canvas"
            />
            <div v-else class="teacher-qr-empty">暂无二维码</div>
          </div>
          <div class="teacher-detail-info">
            <div class="teacher-detail-title">
              {{ teacherCredentialDetailData.credentialName || credentialTypeLabel(teacherCredentialDetailData.credentialType) }}
            </div>
            <div class="teacher-detail-sub">
              {{ teacherCredentialDetailData.competitionName || '-' }}
            </div>
            <div class="teacher-detail-grid">
              <div>
                <span>学生</span>
                <strong>{{ teacherCredentialDetailData.studentName || '-' }}</strong>
              </div>
              <div>
                <span>团队</span>
                <strong>{{ teacherCredentialDetailData.teamName || teacherCredentialDetailData.teamCode || '-' }}</strong>
              </div>
              <div>
                <span>证件编号</span>
                <strong>{{ teacherCredentialDetailData.credentialNo || '-' }}</strong>
              </div>
              <div>
                <span>证件状态</span>
                <strong>{{ teacherCredentialStatusLabel(teacherCredentialDetailData.credentialStatus) }}</strong>
              </div>
              <div>
                <span>赛场</span>
                <strong>{{ teacherCredentialDetailData.scheduleName || '-' }}</strong>
              </div>
              <div>
                <span>地点</span>
                <strong>{{ teacherCredentialDetailData.scheduleLocation || '-' }}</strong>
              </div>
            </div>
          </div>
        </div>
        <div class="teacher-state-grid">
          <div>
            <span>报道状态</span>
            <strong>{{ isReportDone(teacherCredentialDetailData) ? '已报到' : '未报到' }}</strong>
          </div>
          <div>
            <span>资料领取</span>
            <strong>{{ isMaterialDone(teacherCredentialDetailData) ? '已领取资料' : '未领取资料' }}</strong>
          </div>
          <div>
            <span>候场状态</span>
            <strong>{{ isWaitingDone(teacherCredentialDetailData) ? '已候场' : '未候场' }}</strong>
          </div>
        </div>
        <div v-if="teacherCredentialDetailData.delegateInfo" class="teacher-delegate-line">
          代领信息：{{ teacherCredentialDetailData.delegateInfo }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script setup>
import { userGradeInfolist } from "@/api/index";
import shangchuanzuopins from "@/views/personal/components/shangchuan.vue";
import QrcodeVue from "qrcode.vue";
import { ElMessage } from "element-plus";
const { proxy } = getCurrentInstance();
const props = defineProps({
  userinfo: {
    type: Object,
    required: true,
  },
});
const { userinfo } = toRefs(props);
const {
  join_type,
  competition_type,
  works_status,
  competition_status,
  pay_status,
} = proxy.useDict(
  "join_type",
  "competition_type",
  "works_status",
  "competition_status",
  "pay_status"
);
import {
  userCompetition,
  getOrderByUserIdAndCommodityId,
  mySceneCredentialList,
  mySceneNoticeList,
  teacherStudentCredentials,
  teacherStudentCredentialDetail,
} from "@/api/personal/index";
import { useRouter } from "vue-router";
const router = useRouter();
// 路由跳转
function routerTo(path) {
  router.push(path);
}
const canOpenCompetitionDetail = (competition) => {
  return !!(competition?.competitionId && competition?.competitionSeriesId);
};
const goCompetitionDetail = (competition) => {
  if (!canOpenCompetitionDetail(competition)) {
    return;
  }
  routerTo(
    `/event/detail?competitionId=${competition.competitionId}&competitionSeriesId=${competition.competitionSeriesId}`
  );
};
const saishi = ref([]);
const credentialList = ref([]);
const noticeGroups = ref([]);
const credentialLoading = ref(false);
const teacherCredentialList = ref([]);
const teacherCredentialLoading = ref(false);
const teacherCredentialDetailLoading = ref(false);
const teacherCredentialDialogVisible = ref(false);
const teacherCredentialDetailData = ref(null);

const getResponseList = (res) => {
  if (Array.isArray(res)) {
    return res;
  }
  if (Array.isArray(res?.data)) {
    return res.data;
  }
  if (Array.isArray(res?.rows)) {
    return res.rows;
  }
  if (Array.isArray(res?.data?.rows)) {
    return res.data.rows;
  }
  return [];
};

const saishigetlist = () => {
  userCompetition().then((res) => {
    saishi.value = getResponseList(res);
  });
};
const credentialGetList = () => {
  credentialLoading.value = true;
  Promise.allSettled([mySceneCredentialList(), mySceneNoticeList()])
    .then(([credentialResult, noticeResult]) => {
      credentialList.value = credentialResult.status === "fulfilled"
        ? getResponseList(credentialResult.value)
        : [];
      noticeGroups.value = noticeResult.status === "fulfilled"
        ? getResponseList(noticeResult.value)
        : [];
    })
    .finally(() => {
      credentialLoading.value = false;
    });
};
const teacherCredentialGetList = () => {
  teacherCredentialLoading.value = true;
  teacherStudentCredentials()
    .then((res) => {
      teacherCredentialList.value = getResponseList(res);
    })
    .finally(() => {
      teacherCredentialLoading.value = false;
    });
};
saishigetlist();
credentialGetList();
teacherCredentialGetList();

const credentialDialogVisible = ref(false);
const currentCompetition = ref({});
const expandedCredentialIds = ref([]);

const normalizeKey = (value) => {
  if (value === null || value === undefined) {
    return "";
  }
  return `${value}`.trim();
};

const firstFilled = (...values) => {
  for (const value of values) {
    const normalized = normalizeKey(value);
    if (normalized) {
      return normalized;
    }
  }
  return "";
};

const getCredentialSnapshot = (credential) => {
  if (!credential?.credentialSnapshotJson) {
    return {};
  }
  try {
    return JSON.parse(credential.credentialSnapshotJson) || {};
  } catch (error) {
    return {};
  }
};

const getCredentialMatchInfo = (credential) => {
  const snapshot = getCredentialSnapshot(credential);
  const schedule = snapshot.schedule || {};
  const target = snapshot.target || {};
  return {
    seriesId: firstFilled(
      credential?.competitionSeriesId,
      credential?.scopeType === "COMPETITION" ? credential?.scopeRefId : "",
      snapshot.competitionSeriesId,
      schedule.competitionSeriesId,
      target.competitionSeriesId
    ),
    competitionName: firstFilled(
      credential?.competitionName,
      snapshot.competitionName,
      schedule.competitionName,
      target.competitionName
    ),
    teamCode: firstFilled(credential?.teamCode, target.teamCode),
    userId: firstFilled(credential?.userId, target.userId),
    memberId: firstFilled(credential?.memberId, target.memberId),
  };
};

const getCompetitionMatchInfo = (competition) => {
  return {
    seriesId: firstFilled(
      competition?.competitionSeriesId,
      competition?.seriesId
    ),
    competitionName: firstFilled(competition?.competitionName),
    teamCode: firstFilled(competition?.teamCode),
    userId: firstFilled(competition?.userId),
    memberId: firstFilled(competition?.memberId),
  };
};

const isSameCompetitionItem = (left, right) => {
  const leftInfo = getCompetitionMatchInfo(left);
  const rightInfo = getCompetitionMatchInfo(right);

  if (leftInfo.seriesId && rightInfo.seriesId) {
    return leftInfo.seriesId === rightInfo.seriesId;
  }
  if (leftInfo.teamCode && rightInfo.teamCode) {
    return leftInfo.teamCode === rightInfo.teamCode;
  }
  if (leftInfo.memberId && rightInfo.memberId) {
    return leftInfo.memberId === rightInfo.memberId;
  }
  if (
    leftInfo.userId &&
    rightInfo.userId &&
    leftInfo.competitionName &&
    rightInfo.competitionName
  ) {
    return (
      leftInfo.userId === rightInfo.userId &&
      leftInfo.competitionName === rightInfo.competitionName
    );
  }
  if (leftInfo.competitionName && rightInfo.competitionName) {
    return leftInfo.competitionName === rightInfo.competitionName;
  }
  return false;
};

const isCredentialBelongsToCompetition = (credential, competition) => {
  const credentialInfo = getCredentialMatchInfo(credential);
  const competitionInfo = getCompetitionMatchInfo(competition);

  if (credentialInfo.seriesId && competitionInfo.seriesId) {
    return credentialInfo.seriesId === competitionInfo.seriesId;
  }
  if (credentialInfo.teamCode && competitionInfo.teamCode) {
    return credentialInfo.teamCode === competitionInfo.teamCode;
  }
  if (credentialInfo.memberId && competitionInfo.memberId) {
    return credentialInfo.memberId === competitionInfo.memberId;
  }
  if (
    credentialInfo.userId &&
    competitionInfo.userId &&
    credentialInfo.competitionName &&
    competitionInfo.competitionName
  ) {
    return (
      credentialInfo.userId === competitionInfo.userId &&
      credentialInfo.competitionName === competitionInfo.competitionName
    );
  }
  if (credentialInfo.competitionName && competitionInfo.competitionName) {
    return credentialInfo.competitionName === competitionInfo.competitionName;
  }
  return false;
};

const credentialCompetitionList = computed(() => {
  const competitionMap = new Map();
  credentialList.value.forEach((credential) => {
    const info = getCredentialMatchInfo(credential);
    const groupKey = firstFilled(
      info.seriesId,
      info.competitionName,
      info.teamCode,
      credential.credentialId
    );
    if (!groupKey || competitionMap.has(groupKey)) {
      return;
    }
    competitionMap.set(groupKey, {
      _credentialGroupKey: groupKey,
      competitionSeriesId: info.seriesId || undefined,
      competitionName: info.competitionName || "参赛证",
      competitionDesc: firstFilled(
        credential.teamName,
        credential.userName,
        credential.credentialNo
      ),
      teamCode: info.teamCode,
      teamName: credential.teamName,
      userId: info.userId || undefined,
      memberId: info.memberId || undefined,
    });
  });
  return Array.from(competitionMap.values());
});

const displayCompetitionList = computed(() => {
  const competitionList = [...saishi.value];
  credentialCompetitionList.value.forEach((credentialCompetition) => {
    const hasCompetition = competitionList.some((competition) =>
      isSameCompetitionItem(competition, credentialCompetition)
    );
    if (!hasCompetition) {
      competitionList.push(credentialCompetition);
    }
  });
  return competitionList;
});

const teacherCredentialGroups = computed(() => {
  const groupMap = new Map();
  teacherCredentialList.value.forEach((item) => {
    const groupKey = firstFilled(
      item.competitionSeriesId,
      item.competitionName,
      item.teamCode,
      item.teamName,
      item.memberId,
      item.userId
    );
    const key = `${groupKey}:${firstFilled(item.teamCode, item.teamName, "team")}`;
    if (!groupMap.has(key)) {
      groupMap.set(key, {
        key,
        competitionName: item.competitionName || "赛事",
        teamCode: item.teamCode,
        teamName: item.teamName,
        groupName: item.groupName || item.studentGroupName,
        items: [],
      });
    }
    groupMap.get(key).items.push(item);
  });
  return Array.from(groupMap.values());
});

const competitionCoverText = (competition) => {
  return firstFilled(competition?.competitionName, "证").slice(0, 1);
};

const competitionDescText = (competition) => {
  return firstFilled(competition?.competitionDesc, competition?.teamName, "现场参赛证");
};

const credentialCount = (competition) => {
  return getCredentialsByCompetition(competition).length;
};

const getCredentialsByCompetition = (competition) => {
  if (!competition) {
    return [];
  }
  return credentialList.value.filter((item) => {
    return isCredentialBelongsToCompetition(item, competition);
  });
};

const isCompetitionLevelCredential = (credential) => {
  const scopeType = normalizeKey(credential?.scopeType);
  const competitionScopes = ["COMPETITION", "STAFF", "VIP", "EXPERT", "TEMP"];
  if (scopeType) {
    return competitionScopes.includes(scopeType);
  }
  if (credential?.scheduleId) {
    return false;
  }
  return credential?.issueChannel === "COMPETITION_DIRECT";
};

const credentialKey = (credential) => {
  return firstFilled(credential?.credentialId, credential?.credentialNo, credential?.credentialToken);
};

const pickCompetitionCredential = (credentials) => {
  return credentials.find((item) => item.credentialStatus === "EFFECTIVE") || credentials[0] || null;
};

const currentCredentials = computed(() => {
  return getCredentialsByCompetition(currentCompetition.value);
});

const currentCompetitionCredential = computed(() => {
  const competitionCredentials = currentCredentials.value.filter(isCompetitionLevelCredential);
  return pickCompetitionCredential(competitionCredentials);
});

const currentSceneCredentials = computed(() => {
  return currentCredentials.value.filter((item) => !isCompetitionLevelCredential(item));
});

const currentNoticeGroup = computed(() => {
  const competitionSeriesId = getCompetitionMatchInfo(currentCompetition.value).seriesId;
  if (!competitionSeriesId) {
    return null;
  }
  return noticeGroups.value.find(
    (item) => normalizeKey(item.competitionSeriesId) === competitionSeriesId
  ) || null;
});

const hasCurrentNotices = computed(() => {
  const group = currentNoticeGroup.value;
  return Boolean(
    group && ((group.personalNotices || []).length || (group.announcements || []).length)
  );
});

const currentNoticeCount = computed(() => {
  const group = currentNoticeGroup.value;
  return (group?.personalNotices?.length || 0) + (group?.announcements?.length || 0);
});

const ensureExpandedCredentials = () => {
  const validIds = currentSceneCredentials.value.map(credentialKey);
  const validSet = new Set(validIds);
  const remainedIds = expandedCredentialIds.value.filter((item) => validSet.has(item));
  if (remainedIds.length > 0 || validIds.length === 0) {
    expandedCredentialIds.value = remainedIds;
    return;
  }
  expandedCredentialIds.value = [validIds[0]];
};

const openCredentialDialog = (competition) => {
  currentCompetition.value = competition || {};
  ensureExpandedCredentials();
  credentialDialogVisible.value = true;
};

watch(currentSceneCredentials, () => {
  if (!credentialDialogVisible.value) {
    return;
  }
  ensureExpandedCredentials();
});

const toggleCredential = (credential) => {
  const key = credentialKey(credential);
  const expandedSet = new Set(expandedCredentialIds.value);
  if (expandedSet.has(key)) {
    expandedSet.delete(key);
  } else {
    expandedSet.add(key);
  }
  expandedCredentialIds.value = Array.from(expandedSet);
};

const isCredentialExpanded = (credential) => {
  return expandedCredentialIds.value.includes(credentialKey(credential));
};

const credentialTypeLabel = (value) => {
  const map = {
    PARTICIPANT: "参赛证",
    COMPETITOR: "参赛证",
    TEACHER: "教师证",
    EXPERT: "专家证",
    STAFF: "工作人员证",
    VIP: "贵宾证",
    TEMP: "临时证",
  };
  return map[value] || value || "-";
};

const noticeLevelLabel = (value) => {
  const map = {
    NORMAL: "普通",
    IMPORTANT: "重要",
    URGENT: "紧急",
  };
  return map[value] || value || "普通";
};

const noticeLevelType = (value) => {
  return { NORMAL: "info", IMPORTANT: "warning", URGENT: "danger" }[value] || "info";
};

const noticeLevelClass = (value) => {
  return `level-${normalizeKey(value || "NORMAL").toLowerCase()}`;
};

const credentialDisplayName = (credential) => {
  return credential?.credentialName || credentialTypeLabel(credential?.credentialType);
};

const subjectName = (credential) => {
  return credential?.teamName || credential?.userName || "-";
};

const scopeTypeLabel = (value) => {
  const map = {
    COMPETITION: "大赛级",
    SCHEDULE: "赛场级",
    VIP: "贵宾",
    EXPERT: "专家",
    STAFF: "工作人员",
    TEMP: "临时",
  };
  return map[value || "SCHEDULE"] || value || "-";
};

const targetRoleLabel = (value) => {
  const map = {
    TEACHER: "教师",
    MEMBER: "队员",
    EXPERT: "专家",
    CAPTAIN: "队长",
    MATERIAL_STAFF: "发资料工作人员",
    CHECKIN_STAFF: "签到工作人员",
  };
  return map[value] || value || "-";
};

const credentialStatusLabel = (value) => {
  const map = {
    EFFECTIVE: "有效",
    REVOKED: "已作废",
    EXPIRED: "已过期",
  };
  return map[value] || value || "-";
};

const credentialStatusType = (value) => {
  const map = {
    EFFECTIVE: "success",
    REVOKED: "danger",
    EXPIRED: "info",
  };
  return map[value] || "info";
};

const teacherCredentialStatusLabel = (value) => {
  const map = {
    NOT_GENERATED: "未生成参赛证",
    EFFECTIVE: "有效",
    REVOKED: "已作废",
    EXPIRED: "已过期",
  };
  return map[value] || value || "未生成参赛证";
};

const teacherCredentialStatusType = (value) => {
  const map = {
    NOT_GENERATED: "info",
    EFFECTIVE: "success",
    REVOKED: "danger",
    EXPIRED: "info",
  };
  return map[value] || "info";
};

const formatRange = (start, end) => {
  if (!start && !end) {
    return "-";
  }
  return `${start || "-"} ~ ${end || "-"}`;
};

const joinText = (values) => {
  return values.filter(Boolean).join(" / ") || "-";
};

const optionalJoinText = (values) => {
  return values.filter(Boolean).join(" / ");
};

const competitionHeroSub = (credential) => {
  if (!credential) {
    return "请联系现场工作人员生成或刷新证件";
  }
  return joinText([subjectName(credential), credential.credentialNo]);
};

const competitionHeroTitle = (competition, credential) => {
  const title = competition?.competitionName || credential?.competitionName || "现场证件";
  const statusText = competitionDoneStatusText(credential);
  return statusText ? `${title}（${statusText}）` : title;
};

const competitionDoneStatusText = (credential) => {
  const statuses = [];
  if (isReportDone(credential)) {
    statuses.push("已报到");
  }
  if (isMaterialDone(credential)) {
    statuses.push("已领取资料");
  }
  return statuses.join("、");
};

const isReportDone = (credential) => {
  return isDoneValue(credential?.reportStatus) || isDoneValue(credential?.reportStateStatus);
};

const isMaterialDone = (credential) => {
  return isDoneValue(credential?.materialStatus) || isDoneValue(credential?.materialStateStatus);
};

const isWaitingDone = (credential) => {
  return isDoneValue(credential?.waitingStatus) || isDoneValue(credential?.waitingStateStatus);
};

const isDoneValue = (value) => {
  return normalizeKey(value) === "1";
};

const scheduleTitle = (credential) => {
  const snapshot = getCredentialSnapshot(credential);
  const schedule = snapshot.schedule || {};
  return firstFilled(
    schedule.scheduleName,
    credential?.scheduleName,
    optionalJoinText([
      credential?.competitionStageName,
      credential?.competitionTrackName,
      credential?.secondLevelName,
    ]),
    optionalJoinText([
      schedule.competitionStageName,
      schedule.competitionTrackName,
      schedule.secondLevelName,
    ]),
    credential?.contestLocation,
    credentialDisplayName(credential),
    "赛场证件"
  );
};

const sceneCardSub = (credential) => {
  const time = formatRange(credential.contestStartTime, credential.contestEndTime);
  const place = joinText([credential.contestLocation, credential.contestRoom]);
  return [time === "-" ? "" : time, place === "-" ? "" : place].filter(Boolean).join(" · ") || "现场核验使用";
};

const waitingText = (credential) => {
  const time = formatRange(credential.waitingStartTime, credential.waitingEndTime);
  const place = credential.waitingLocation || "";
  const group = credential.waitingGroupName || credential.waitingGroupCode || "";
  return [time === "-" ? "" :  place].filter(Boolean).join(" / ") || "-";
};

const credentialQrValue = (credential) => {
  return firstFilled(credential?.qrContent, credential?.credentialToken);
};

const openTeacherCredentialDetail = (row) => {
  if (!row?.credentialId) {
    return;
  }
  teacherCredentialDetailLoading.value = true;
  teacherStudentCredentialDetail(row.credentialId)
    .then((res) => {
      teacherCredentialDetailData.value = res?.data || row;
      teacherCredentialDialogVisible.value = true;
    })
    .catch((error) => {
      ElMessage.error(error?.msg || "无权限查看该学生参赛证");
    })
    .finally(() => {
      teacherCredentialDetailLoading.value = false;
    });
};
const dialogchengji = ref(false);
const chengjilist = ref([]);
const chengjichaxun = (item) => {
  const params = {
    competitionSeriesId: item.competitionSeriesId,
  };
  userGradeInfolist(params).then((res) => {
    dialogchengji.value = true;
    chengjilist.value = res.rows;
  });
};

const shangchuanzuopin = ref(false);

// 上传作品信息  需要stageName stageId competitionSeriesId
const shangchuanxinxi = ref({});
// 获取当前订单信息
const shangchuan = (item) => {
  // if (item.stageName && item.stageId) {
  shangchuanxinxi.value = item;
  shangchuanzuopin.value = true;
  // }else{
  //    ElMessage.error('当前未处在任何阶段');
  // }
};
// 我的订单
const jiaofeijilu = ref(false);
const jiaofeilist = ref({});
const jiaofeijiluchaxun = (item) => {
  const params = {
    userId: userinfo.value.userId,
    commodityId: item.competitionSeriesId,
  };
  getOrderByUserIdAndCommodityId(params).then((res) => {
    jiaofeilist.value = res.data;
    jiaofeijilu.value = true;
  });
};
const emits = (item) => {
  shangchuanzuopin.value = item;
};
</script>


<style scoped lang="scss">
.zong {
  width: 100%;
  padding: 0;

  .competition-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
  }

  .neirong {
    width: calc(100% - 200px);
    height: 150px;
    margin: 10px 0;

    .neiimg {
      width: 220px;
      height: 150px;
      float: left;
      position: relative;

      img {
        width: 220px;
        height: 150px;
        position: absolute;
        border-radius: 8px 8px 8px 8px;
      }

      .competition-placeholder {
        width: 220px;
        height: 150px;
        position: absolute;
        border-radius: 8px;
        background: linear-gradient(135deg, #eef4ff 0%, #f7fbff 100%);
        border: 1px solid #d8e5ff;
        color: #3169f8;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 42px;
        font-weight: 700;
      }
    }

    .neitext {
      float: left;
      width: calc(100% - 300px);
      margin-left: 20px;

      .title {
        font-weight: bold;
        font-size: 18px;
        color: #333333;
        line-height: 25px;
        text-align: left;
        font-style: normal;
        text-transform: none;
        margin-top: 10px;
      }

      .price {
        display: -webkit-box;
        min-height: 50px;
        -webkit-line-clamp: 3;
        /* 最多显示3行 */
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
        overflow: hidden;
        font-weight: 400;
        font-size: 15px;
        color: #999999;
        line-height: 24px;
        text-align: left;
        font-style: normal;
        text-transform: none;
        margin-top: 10px;
      }

      .bq {
        margin-top: 10px;
      }

      .time {
        margin-top: 10px;
        font-weight: 400;
        font-size: 14px;
        color: #999999;
        line-height: 24px;
        text-align: left;
        font-style: normal;
        text-transform: none;
        display: flex;
        justify-content: space-between;
      }
    }
  }

  .credential-actions {
    width: 170px;
    min-height: 96px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .credential-tip {
      margin-top: 8px;
      font-size: 12px;
      color: #606266;
      line-height: 18px;
    }

    .muted-tip {
      color: #a8abb2;
    }
  }

  .zhuangtai {
    width: 200px;
    height: 150px;
    margin: 10px 0;
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    .zhuangti {
      margin-top: 40px;
      width: 100px;
      text-align: center;
      font-weight: 400;
      font-size: 18px;
      color: #3169f8;
    }
  }
}

.credential-empty {
  padding: 30px 0;
}

.credential-dialog-body {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 4px;
}

.credential-dialog-title {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-right: 44px;
  color: #303133;
  font-size: 20px;
  font-weight: 700;
  line-height: 28px;
}

.credential-dialog-logo {
  width: 72px;
  height: 72px;
  flex-shrink: 0;
  object-fit: contain;
}

.competition-credential-hero {
  padding: 24px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1d4ed8, #2563eb 58%, #0f766e);
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.18);
  color: #ffffff;
}

.competition-credential-hero.reported {
  background: linear-gradient(135deg, #047857, #059669 58%, #0f766e);
  box-shadow: 0 12px 24px rgba(5, 150, 105, 0.18);
}

.competition-credential-hero.empty {
  background: #ffffff;
  box-shadow: none;
  border: 1px solid #ebeef5;
  color: #303133;
}

.hero-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.hero-copy {
  min-width: 0;
}

.hero-kicker {
  color: rgba(255, 255, 255, 0.78);
  font-size: 13px;
  line-height: 20px;
}

.competition-credential-hero.empty .hero-kicker,
.competition-credential-hero.empty .hero-sub {
  color: #909399;
}

.hero-title {
  margin-top: 8px;
  color: #ffffff;
  font-size: 24px;
  font-weight: 700;
  line-height: 34px;
  word-break: break-all;
}

.competition-credential-hero.empty .hero-title {
  color: #303133;
}

.hero-sub {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 14px;
  line-height: 22px;
  word-break: break-all;
}

.hero-role {
  flex-shrink: 0;
}

.hero-pass-panel {
  margin-top: 24px;
  padding: 18px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  gap: 22px;
}

.hero-qr-box {
  padding: 10px;
  border-radius: 10px;
  background: #ffffff;
  flex-shrink: 0;
}

.hero-pass-copy {
  flex: 1;
  min-width: 0;
}

.hero-pass-title {
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
  line-height: 28px;
}

.hero-pass-desc {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 14px;
  line-height: 22px;
}

.hero-pass-status {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;

  span {
    height: 28px;
    padding: 0 12px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.16);
    color: rgba(255, 255, 255, 0.82);
    font-size: 13px;
    line-height: 28px;
  }

  .done {
    background: #ffffff;
    color: #1d4ed8;
    font-weight: 600;
  }
}

.competition-credential-hero.reported .hero-pass-status .done {
  color: #047857;
}

.hero-qr-missing {
  width: 100%;
  padding: 26px 18px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.14);
  color: rgba(255, 255, 255, 0.86);
  font-size: 14px;
  line-height: 22px;
  text-align: center;
}

.hero-empty-text {
  margin-top: 20px;
  padding: 18px;
  border-radius: 8px;
  background: #f8f9fb;
  color: #909399;
  text-align: center;
}

.hero-notice-panel {
  margin-top: 22px;
  padding: 20px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.97);
  color: #303133;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.12);
}

.hero-notice-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 14px;
  border-bottom: 1px solid #ebeef5;

  strong {
    color: #303133;
    font-size: 18px;
  }

  span {
    padding: 3px 10px;
    border-radius: 12px;
    background: #eef2ff;
    color: #4338ca;
    font-size: 12px;
  }
}

.notice-group {
  margin-top: 16px;
}

.notice-group-title {
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
  font-weight: 700;
}

.notice-item {
  margin-top: 10px;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #94a3b8;
  background: #f8fafc;
}

.notice-item.personal {
  background: #f0f7ff;
  border-left-color: #3b82f6;
}

.notice-item.level-important {
  background: #fffbeb;
  border-left-color: #f59e0b;
}

.notice-item.level-urgent {
  background: #fff5f5;
  border-left-color: #ef4444;
}

.notice-item-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;

  strong {
    color: #303133;
    font-size: 15px;
    line-height: 22px;
    word-break: break-word;
  }
}

.notice-rich-content {
  margin-top: 10px;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;

  :deep(p) {
    margin: 6px 0;
  }

  :deep(img) {
    max-width: 100%;
    height: auto;
  }
}

.scene-credential-section {
  margin-top: 22px;
}

.scene-section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.scene-section-title {
  color: #303133;
  font-size: 18px;
  font-weight: 700;
  line-height: 26px;
}

.scene-section-sub {
  margin-top: 2px;
  color: #909399;
  font-size: 13px;
  line-height: 20px;
}

.scene-empty {
  padding: 28px;
  border-radius: 8px;
  background: #f8f9fb;
  color: #909399;
  text-align: center;
}

.scene-credential-card {
  margin-top: 12px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #ffffff;
  overflow: hidden;
}

.scene-card-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
  cursor: pointer;
}

.scene-card-main {
  flex: 1;
  min-width: 0;
}

.scene-card-title {
  color: #303133;
  font-size: 17px;
  font-weight: 700;
  line-height: 25px;
  word-break: break-all;
}

.scene-card-sub {
  margin-top: 6px;
  color: #606266;
  font-size: 13px;
  line-height: 20px;
  word-break: break-all;
}

.scene-card-meta {
  margin-top: 6px;
  color: #a8abb2;
  font-size: 12px;
  line-height: 18px;
  word-break: break-all;
}

.scene-card-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 14px;
  flex-shrink: 0;
}

.expand-arrow {
  width: 10px;
  height: 10px;
  margin-right: 8px;
  border-right: 2px solid #a8abb2;
  border-bottom: 2px solid #a8abb2;
  transform: rotate(45deg);
  transition: transform 0.2s;
}

.expand-arrow.expanded {
  transform: rotate(-135deg);
}

.scene-card-body {
  padding: 0 18px 18px;
  border-top: 1px solid #ebeef5;
}

.scene-card-content {
  display: block;
  padding-top: 18px;
}

.scene-info {
  flex: 1;
  min-width: 0;
}

.info-section {
  margin-bottom: 18px;

  .section-title {
    position: relative;
    padding-left: 10px;
    margin-bottom: 12px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    line-height: 24px;

    &::before {
      content: "";
      position: absolute;
      left: 0;
      top: 5px;
      width: 3px;
      height: 14px;
      background: #3169f8;
      border-radius: 2px;
    }
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.info-item,
.schedule-line {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-height: 28px;
  font-size: 14px;
  line-height: 22px;

  span {
    width: 90px;
    flex-shrink: 0;
    color: #909399;
  }

  strong {
    flex: 1;
    min-width: 0;
    color: #303133;
    font-weight: 500;
    word-break: break-all;
  }
}

.notice-content {
  padding: 12px;
  background: #f8f9fb;
  border-radius: 8px;
  color: #606266;
  font-size: 14px;
  line-height: 24px;
  white-space: pre-wrap;
}

.teacher-credential-panel {
  margin-bottom: 18px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.teacher-panel-head,
.teacher-team-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.teacher-panel-title {
  color: #303133;
  font-size: 18px;
  font-weight: 700;
  line-height: 26px;
}

.teacher-panel-sub,
.teacher-team-sub {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
  line-height: 20px;
}

.teacher-empty {
  padding: 12px 0 0;
}

.teacher-team-list {
  margin-top: 14px;
}

.teacher-team-block {
  margin-top: 12px;
  padding: 14px;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #fafbfc;
}

.teacher-team-title {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
  word-break: break-all;
}

.teacher-credential-table {
  margin-top: 12px;
  border-radius: 8px;
  overflow: hidden;
}

.teacher-status-line {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;

  span {
    display: inline-flex;
    align-items: center;
    min-height: 22px;
    padding: 0 8px;
    border-radius: 8px;
    background: #eef0f3;
    color: #909399;
    font-size: 12px;
    line-height: 18px;
  }

  .done {
    background: #e8f8f0;
    color: #047857;
    font-weight: 600;
  }
}

.teacher-no-credential {
  color: #909399;
  font-size: 13px;
}

.teacher-detail-body {
  color: #303133;
}

.teacher-detail-main {
  display: flex;
  gap: 22px;
  align-items: flex-start;
}

.teacher-detail-qr {
  width: 204px;
  min-height: 204px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  flex-shrink: 0;
}

.teacher-qr-empty {
  color: #909399;
  font-size: 14px;
}

.teacher-detail-info {
  flex: 1;
  min-width: 0;
}

.teacher-detail-title {
  font-size: 20px;
  font-weight: 700;
  line-height: 28px;
  word-break: break-all;
}

.teacher-detail-sub {
  margin-top: 6px;
  color: #606266;
  font-size: 14px;
  line-height: 22px;
  word-break: break-all;
}

.teacher-detail-grid,
.teacher-state-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 16px;
  margin-top: 16px;

  div {
    min-width: 0;
  }

  span {
    display: block;
    color: #909399;
    font-size: 12px;
    line-height: 18px;
  }

  strong {
    display: block;
    margin-top: 2px;
    color: #303133;
    font-size: 14px;
    line-height: 22px;
    font-weight: 500;
    word-break: break-all;
  }
}

.teacher-state-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.teacher-delegate-line {
  margin-top: 14px;
  color: #606266;
  font-size: 14px;
  line-height: 22px;
}

.eltags {
  margin-right: 10px;
  cursor: pointer;
}
.bt {
  height: 70px;
  background: #4b7eff;
  border-radius: 10px 10px 0px 0px;

  .th {
    font-family: Source Han Sans CN, Source Han Sans CN;
    font-weight: 400;
    font-size: 18px;
    color: #ffffff;
    line-height: 70px;
    text-align: center;
    font-style: normal;
    text-transform: none;
  }
}
.centen {
  .tr {
    font-family: Source Han Sans CN, Source Han Sans CN;
    font-weight: 400;
    font-size: 16px;
    color: #333333;
    line-height: 60px;
    text-align: center;
    font-style: normal;
    text-transform: none;
    border-left: 1px solid #e4e4e4;
    border-bottom: 1px solid #e4e4e4;
  }
}
.zhanwu{
    font-size: 20px;
        text-align: center;
        line-height: 30px;
        min-height: 400px;
}
</style>
