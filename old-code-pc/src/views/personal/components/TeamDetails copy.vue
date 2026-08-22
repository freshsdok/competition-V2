<template>
  <div class="bg">
    <div class="container-custom">
      <Breadcrumbar />
      <el-card class="card">
        <div class="title">
          {{ queryParams.competitionName }}-{{
            queryParams.competitionTrackName
          }}-{{ queryParams.secondLevelName }}
        </div>
        <div class="tuandui">
          {{ queryParams.tuanduimingcheng }}
        </div>
        <div class="biaotou">参赛信息修改</div>
        <div class="cansaixinxi">
          <div class="xiangqing">
            <div class="xiangqing-title">操作要求</div>
            <div
              class="xiangqing-content"
              v-html="
                caozuoyaoqiu.find((item) => item.operationType == 1)?.hintText1
              "
            ></div>
          </div>
          <div class="caisaibd">
            <div class="header-actions">
              <div v-if="!isEditingGroup">
                <el-button
                  type="primary"
                  class="search-btn"
                  @click="handleEditGroup"
                  >更换组别（{{
                    queryParams.secondLevelOperateCount
                  }}次）</el-button
                >
                <span style="display: inline-block; margin-left: 20px"
                  >当前组别：
                  <span style="font-weight: bold">{{
                    queryParams.secondLevelName
                  }}</span></span
                >
              </div>
              <div v-else class="group-edit-actions">
                <el-select
                  v-model="selectedGroup"
                  placeholder="请选择组别"
                  style="width: 200px"
                >
                  <el-option
                    :label="item.secondLevelName"
                    :value="item.secondLevelCode"
                    v-for="(item, index) in competitionTrackConfigInfoList"
                    :key="index"
                  />
                </el-select>
                <el-button type="primary" size="small" @click="handleSaveGroup"
                  >保存</el-button
                >
                <el-button size="small" @click="handleCancelGroup"
                  >取消</el-button
                >
              </div>
            </div>
            <el-table
              :data="queryParams.competitionApplyInfoList"
              style="width: 100%"
            >
              <el-table-column
                prop="userName"
                label="姓名"
                min-width="100"
                text-align="center"
              >
                <template #default="{ row }">
                  <el-input
                    v-if="row.isEditing && row.isNew"
                    v-model="row.userName"
                    placeholder="请输入姓名"
                    size="small"
                  />
                  <span v-else>{{ row.userName }}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="competitionRoleName"
                label="身份"
                min-width="80"
                text-align="center"
              >
                <template #default="{ row }">
                  <el-select
                    v-if="row.isEditing && row.isNew"
                    v-model="row.competitionRoleName"
                    placeholder="请选择身份"
                    size="small"
                  >
                    <el-option label="队长" value="队长" />
                    <el-option label="队员" value="队员" />
                  </el-select>
                  <span v-else>{{ row.competitionRoleName }}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="idCard"
                label="身份证号"
                min-width="200"
                text-align="center"
              >
                <template #default="{ row }">
                  <el-input
                    v-if="row.isEditing && row.isNew"
                    v-model="row.idCard"
                    placeholder="请输入身份证号"
                    size="small"
                  />
                  <span v-else>{{ row.idCard }}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="phone"
                label="手机号"
                min-width="180"
                text-align="center"
              >
                <template #default="{ row }">
                  <el-input
                    v-if="row.isEditing"
                    v-model="row.phone"
                    placeholder="请输入手机号"
                    size="small"
                  />
                  <span v-else>{{ row.phone }}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="email"
                label="邮箱"
                min-width="200"
                text-align="center"
              >
                <template #default="{ row }">
                  <el-input
                    v-if="row.isEditing"
                    v-model="row.email"
                    placeholder="请输入邮箱"
                    size="small"
                  />
                  <span v-else>{{ row.email }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" min-width="200" text-align="center">
                <template #default="{ row }">
                  <div v-if="chengyuan">
                    <el-button
                      size="mini"
                      type="danger"
                      @click="handleDelete(row, 'competitionApplyInfoList')"
                      >删除</el-button
                    >
                  </div>
                  <div v-else>
                    <div v-if="row.isEditing">
                      <el-button
                        type="primary"
                        size="mini"
                        @click="handleSave(row)"
                        >保存</el-button
                      >
                      <el-button size="mini" @click="handleCancel(row)">
                        取消
                      </el-button>
                    </div>
                    <el-button
                      v-else
                      type="primary"
                      size="mini"
                      @click="handleEdit(row)"
                      >编辑({{ row.applyInfoChangeOperateCount }}次)</el-button
                    >
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <div style="margin-top: 20px" v-if="false">
              <div v-if="!chengyuan">
                <el-button type="warning" @click="isnummenber"
                  >成员变更{{ queryParams.memberOperateCount }}次）</el-button
                >
              </div>
              <div v-if="chengyuan">
                <el-button type="success" @click="handleSaveMember"
                  >保存</el-button
                >
                <el-button type="success" @click="handleAddMember"
                  >新增成员</el-button
                >
              </div>
            </div>
          </div>
        </div>
        <div class="biaotou">指导教师修改</div>
        <div class="cansaixinxi">
          <div class="xiangqing">
            <div class="xiangqing-title">操作要求</div>
            <div
              class="xiangqing-content"
              v-html="
                caozuoyaoqiu.find((item) => item.operationType == 2)?.hintText1
              "
            ></div>
          </div>
          <div class="caisaibd">
            <el-table
              :data="queryParams.guideTeacherApplyInfoList"
              style="width: 100%"
            >
              <el-table-column
                prop="userName"
                label="姓名"
                min-width="100"
                text-align="center"
              >
                <template #default="{ row }">
                  <el-input
                    v-if="row.isEditing && row.isNew"
                    v-model="row.userName"
                    placeholder="请输入姓名"
                    size="small"
                  />
                  <span v-else>{{ row.userName }}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="competitionRoleName"
                label="身份"
                min-width="80"
                text-align="center"
              >
                <template #default="{ row }">
                  <span>{{ row.competitionRoleName }}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="phone"
                label="手机号"
                min-width="180"
                text-align="center"
              >
                <template #default="{ row }">
                  <el-input
                    v-if="row.isEditing"
                    v-model="row.phone"
                    placeholder="请输入手机号"
                    size="small"
                  />
                  <span v-else>{{ row.phone }}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="email"
                label="邮箱"
                min-width="200"
                text-align="center"
              >
                <template #default="{ row }">
                  <el-input
                    v-if="row.isEditing"
                    v-model="row.email"
                    placeholder="请输入邮箱"
                    size="small"
                  />
                  <span v-else>{{ row.email }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" min-width="200" text-align="center">
                <template #default="{ row }">
                  <div v-if="Teacher">
                    <el-button
                      size="mini"
                      type="danger"
                      @click="handleDelete(row, 'guideTeacherApplyInfoList')"
                      >删除</el-button
                    >
                  </div>
                  <div v-else>
                    <div v-if="row.isEditing">
                      <el-button
                        type="primary"
                        size="mini"
                        @click="handleSave(row)"
                        >保存</el-button
                      >
                      <el-button size="mini" @click="handleCancel(row)"
                        >取消</el-button
                      >
                    </div>
                    <el-button
                      v-else
                      type="primary"
                      size="mini"
                      @click="handleEdit(row)"
                      >编辑({{ row.applyInfoChangeOperateCount }}次)</el-button
                    >
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <div style="margin-top: 20px" v-if="false">
              <div v-if="!Teacher">
                <el-button type="warning" @click="isnumTeacher"
                  >成员变更{{
                    queryParams.guideTeacherOperateCount
                  }}次）</el-button
                >
              </div>
              <div v-if="Teacher">
                <el-button type="success" @click="handleSaveTeacher"
                  >保存</el-button
                >
                <el-button type="success" @click="handleAddTeacher"
                  >新增指导教师</el-button
                >
              </div>
            </div>
          </div>
        </div>
        <div class="tuifei" v-if="false">
          <div class="tuifeichongjiao">
            <div class="biaotou">退费重缴</div>
            <div style="margin-top: 20px" class="tuifei">
              <div class="xiangqing"></div>
              <div class="tuifei-btn">
                <el-button type="primary" size="mini">申请退费重缴</el-button>
              </div>
            </div>
          </div>
          <div class="tuisai">
            <div class="biaotou">退赛申请</div>
            <div style="margin-top: 20px" class="tuifei">
              <div class="xiangqing"></div>
              <div class="tuifei-btn">
                <el-button type="primary" size="mini">开始申请</el-button>
              </div>
            </div>
          </div>
        </div>
        <div class="fanhuishangji">
          <el-button type="primary" size="mini" @click="handleBack"
            >返回队伍列表</el-button
          >
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import Breadcrumbar from "@/components/breadcrumbar.vue";
import {
  getUserCompetitionApplyInfo,
  changeCompetitionApplyInfo,
  selectCompetitionTrackConfigInfo,
  selectCompetitionOperationConfigInfo,
} from "@/api/team";
import { useRoute, useRouter } from "vue-router";
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();
const queryParams = ref({});
const isEditingGroup = ref(false);
const selectedGroup = ref("");
const originalGroupName = ref("");

// 编辑成员信息
const handleEdit = (row) => {
  if (row.applyInfoChangeOperateCount == 0) {
    ElMessage.warning("成员暂无修改次数");
    return;
  }
  row.isEditing = true;
  row.originalData = {
    name: row.name,
    phone: row.phone,
    email: row.email,
    competitionRoleName: row.competitionRoleName,
  };
};

// 编辑组别
const handleEditGroup = () => {
  if (queryParams.value.secondLevelOperateCount == 0) {
    ElMessage.warning("组别暂无修改次数");
    return;
  }
  originalGroupName.value = queryParams.value.secondLevelName;
  selectedGroup.value = queryParams.value.secondLevelName;
  isEditingGroup.value = true;
};

// 保存组别修改
const handleSaveGroup = () => {
  const params = {
    competitionSeriesId: queryParams.value.competitionSeriesId,
    competitionName: queryParams.value.competitionName,
    competitionTrackName: queryParams.value.competitionTrackName,
    competitionTrackId: queryParams.value.competitionTrackId,
    secondLevelCode: selectedGroup.value,
    secondLevelName:competitionTrackConfigInfoList.value.find(item => item.secondLevelCode == selectedGroup.value).secondLevelName,
    teamCode: queryParams.value.teamCode,
    teamName: queryParams.value.teamName,
    changeType: "group",
    competitionApplyInfoList: queryParams.value.competitionApplyInfoList,
};
    changeCompetitionApplyInfo(params).then((res) => {
      if (res.code == 200) {
        isEditingGroup.value = false;
        getlist()
      }
    });
};

// 取消组别编辑
const handleCancelGroup = () => {
  selectedGroup.value = originalGroupName.value;
  isEditingGroup.value = false;
};

// 保存成员信息修改
const handleSave = (row) => {
  const params = {
    competitionSeriesId: queryParams.value.competitionSeriesId,
    competitionName: queryParams.value.competitionName,
    competitionTrackName: queryParams.value.competitionTrackName,
    competitionTrackId: queryParams.value.competitionTrackId,
    secondLevelCode: queryParams.value.secondLevelCode,
    secondLevelName: queryParams.value.secondLevelName,
    teamCode: queryParams.value.teamCode,
    teamName: queryParams.value.teamName,
    changeType: "info",
    competitionApplyInfoList: [row],
  };
  changeCompetitionApplyInfo(params).then((res) => {
    if (res.code == 200) {
      row.isEditing = false;
      row.isNew = false;
      getlist()
    }
  });
};

// 取消成员信息编辑
const handleCancel = (row) => {
  if (row.isNew) {
    const index1 = queryParams.value.competitionApplyInfoList.findIndex((item) => item === row);
    if (index1 !== -1) {
      queryParams.value.competitionApplyInfoList.splice(index1, 1);
    }
    const index2 = queryParams.value.guideTeacherApplyInfoList.findIndex((item) => item === row);
    if (index2 !== -1) {
      queryParams.value.guideTeacherApplyInfoList.splice(index2, 1);
    }
  } else {
    if (row.originalData) {
      row.name = row.originalData.name;
      row.phone = row.originalData.phone;
      row.email = row.originalData.email;
      row.competitionRoleName = row.originalData.competitionRoleName;
    }
    row.isEditing = false;
  }
};

const chengyuan = ref(false);
// 检查团队成员修改次数
const isnummenber = () => {
  if (queryParams.value.memberOperateCount == 0) {
    ElMessage.warning("团队成员暂无修改次数");
    return;
  }
  chengyuan.value = true;
};

// 添加新成员
const handleAddMember = () => {
  queryParams.value.competitionApplyInfoList.push({
    userName: "",
    phone: "",
    competitionRoleName: "队员",
    email: "",
    isEditing: true,
    isNew: true,
  });
};

// 保存成员变更
const handleSaveMember = () => {
  chengyuan.value = false;
  queryParams.value.competitionApplyInfoList.forEach((item) => {
    item.isEditing = false;
    item.isNew = false;
  });
};

const Teacher = ref(false);

// 删除成员或指导教师
const handleDelete = (row, listName) => {
  const index = queryParams.value[listName].findIndex((item) => item === row);
  if (index !== -1) {
    queryParams.value[listName].splice(index, 1);
  }
};

// 检查指导教师修改次数
const isnumTeacher = () => {
  if (queryParams.value.guideTeacherOperateCount == 0) {
    ElMessage.warning("指导教师暂无修改次数");
    return;
  }
  Teacher.value = true;
};

// 添加新指导教师
const handleAddTeacher = () => {
  queryParams.value.guideTeacherApplyInfoList.push({
    userName: "",
    competitionRoleName: "指导教师",
    phone: "",
    email: "",
    isEditing: true,
    isNew: true,
  });
};

// 保存指导教师变更
const handleSaveTeacher = () => {
  Teacher.value = false;
  queryParams.value.guideTeacherApplyInfoList.forEach((item) => {
    item.isEditing = false;
    item.isNew = false;
  });
};

// 获取参赛信息列表
const getlist = () => {
  getUserCompetitionApplyInfo({
    teamCode: route.query.teamCode,
  }).then((res) => {
    console.log(res.data);
    if (res.data) {
      queryParams.value = res.data[0];
      console.log(queryParams.value, 12346);
      const paramsTrackConfig = {
        competitionSeriesId: queryParams.value.competitionSeriesId,
        competitionTrackId: queryParams.value.competitionTrackId,
      };
      selectCompetitionTrackConfigInfolist(paramsTrackConfig);
      selectCompetitionOperationConfigInfolist(
        queryParams.value.competitionSeriesId
      );
    }
  });
};

const competitionTrackConfigInfoList = ref([]);
// 获取竞赛赛道配置信息列表
const selectCompetitionTrackConfigInfolist = (paramsTrackConfig) => {
  selectCompetitionTrackConfigInfo(paramsTrackConfig).then((res) => {
    if (res.code == 200) {
      competitionTrackConfigInfoList.value = res.data;
    }
  });
};

const caozuoyaoqiu = ref([]);
// 获取竞赛操作配置信息列表
const selectCompetitionOperationConfigInfolist = (competitionSeriesId) => {
  selectCompetitionOperationConfigInfo(competitionSeriesId).then((res) => {
    if (res.code == 200) {
      caozuoyaoqiu.value = res.data;
    }
  });
};

// 返回上一页
const handleBack = () => {
  router.push({
    path: "/personal/list",
    query: {
      lefttabs: "我的团队",
    },
  });
};
onMounted(() => {
  getlist();
});
</script>

<style scoped lang="scss">
:deep(.el-card__body) {
  padding: 0;
}

.bg {
  width: 100%;
}

.card {
  width: 100%;
  margin-top: 40px;
  padding: 20px 25px;
  min-height: 700px;
  margin-bottom: 30px;
}

.title {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: bold;
  font-size: 20px;
  color: #333333;
  line-height: 29px;
  text-align: left;
  font-style: normal;
  text-transform: none;
}

.tuandui {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 14px;
  color: #333333;
  line-height: 20px;
  text-align: left;
  font-style: normal;
  text-transform: none;
  margin-top: 15px;
}

.biaotou {
  border-left: 4px solid #3169f8;
  height: 20px;
  padding-left: 10px;
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: bold;
  font-size: 20px;
  color: #333333;
  line-height: 29px;
  text-align: left;
  font-style: normal;
  text-transform: none;
  display: flex;
  align-items: center;
  margin-top: 45px;
}

.cansaixinxi {
  display: flex;
  align-items: center;
  margin-top: 25px;
}

.caisaibd {
  min-height: 300px;
  padding-left: 20px;
  width: calc(100% - 300px);
}

.xiangqing {
  min-width: 300px;
  max-width: 300px;
  min-height: 300px;
  background: #ffffff;
  border-radius: 0px 0px 0px 0px;
  border: 1px solid #e4e4e4;
  padding: 20px;
  .xiangqing-title {
    font-family: Source Han Sans CN, Source Han Sans CN;
    font-weight: bold;
    font-size: 16px;
    color: #333333;
    line-height: 22px;
    text-align: left;
    font-style: normal;
    text-transform: none;
  }
  .xiangqing-content {
    font-family: Source Han Sans CN, Source Han Sans CN;
    font-weight: 400;
    font-size: 14px;
    color: #999999;
    line-height: 24px;
    text-align: left;
    font-style: normal;
    text-transform: none;
    margin-top: 15px;
    text-indent: 30px;
  }
}

.tuifei {
  display: flex;
  margin-top: 25px;
}

.tuifeichongjiao {
  width: 50%;
}

.tuisai {
  width: 50%;
}

.header-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  align-items: center;
}

.group-edit-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.tuifei {
  margin-top: 20px;
  display: flex;
  align-items: center;
}
.tuifei-btn {
  margin-left: 120px;
}
.fanhuishangji {
  margin-top: 60px;
  display: flex;
  justify-content: center;
}
</style>
