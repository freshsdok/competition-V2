<template>
  <div class="team">
    <el-card class="cardw" shadow="always">
      <el-form
        :model="queryParams"
        ref="queryRefsss"
        :inline="true"
        class="formw"
      >
        <el-form-item label="关键字搜索" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入关键字"
            clearable
            class="ipt"
          />
        </el-form-item>

        <el-form-item label="报名时间阶段">
          <el-date-picker
            v-model="queryParams.time"
            type="daterange"
            range-separator="→"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD HH:mm:ss"
            :default-time="[
              new Date('1970-01-01 00:00:00'),
              new Date('1970-01-01 23:59:59'),
            ]"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="search-btn" @click="sousuo"
            >搜索</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>
    <el-card
      class="cardw"
      shadow="always"
      v-for="(item, index) in xvnishujv"
      :key="index"
    >
      <div class="team-header">
        <div class="team-info">
          <div class="team-name">
            {{ item.competitionName }}-{{ item.competitionTrackName }}--{{
              item.secondLevelName
            }}
          </div>
          <div class="team-title">
            <span>团队名称:{{ item.teamName }} </span>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <span>团队编号:{{ item.teamCode }} </span>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <span> 报名时间：{{ item.registrationTime }} </span>
          </div>
        </div>

        <div class="team-payment">
          <!-- <div class="team-status">{{ item.status }}</div> -->
          <el-tag
            v-if="
              change_type.find((xxx) => {
                return xxx.value == item.operationStatus;
              })
            "
            :type="
              change_type.find((xxx) => {
                return xxx.value == item.operationStatus;
              })?.elTagType
            "
            style="margin-left: 20px"
            size="large"
          >
            {{
              change_type.find((xxx) => {
                return xxx.value == item.operationStatus;
              })?.label
            }}
          </el-tag>
        </div>
      </div>
      <el-table :data="item.competitionApplyInfoList">
        <el-table-column
          prop="userName"
          label="姓名"
          width="100"
          align="center"
        />
        <el-table-column
          prop="competitionRoleName"
          label="身份"
          width="100"
          align="center"
        />
        <el-table-column
          prop="idCard"
          label="身份证号"
          min-width="180"
          align="center"
        >
          <template #default="{ row }">
            {{ formatIdCard(row.idCard) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="phone"
          label="手机号"
          min-width="150"
          align="center"
        >
          <template #default="{ row }">
            <div style="display: flex; justify-content: center">
              {{ formatPhone(row.phone) }}
              <!-- userInfoFlag 2手机号不一致  1邮箱不一致 -->
              <el-popover
                title=""
                placement="top-start"
              >
                <div>
                 该队员报名手机号信息同本人注册账号不一致,注册手机号为
                 <p>
                  {{ row.userInfoDateList?.phone }}
                 </p>
                </div>
                <template #reference>
                  <Warning
                    style="width: 20px; color: #ffc400; margin-left: 5px"
                    v-if="row.userInfoFlag?.split(',')?.indexOf('2') != -1"
                  /> </template
              ></el-popover>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="email"
          label="邮箱"
          min-width="150"
          align="center"
        >
          <template #default="{ row }">
            <div style="display: flex; justify-content: center">
              {{ formatPhone(row.email) }}
              <el-popover
                title=""
                placement="top-start"
              >
                <div>
                 该队员报名邮箱信息同本人注册账号不一致, 注册邮箱为
                 <p>
                  {{ row.userInfoDateList?.email }}
                 </p>
                </div>
                <template #reference>
                  <Warning
                    style="width: 20px; color: #ffc400; margin-left: 5px"
                    v-if="row.userInfoFlag?.split(',')?.indexOf('1') != -1"
                  /> </template
              ></el-popover>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div
        class="tiaozheng"
        v-if="
          auth.hasPermi('competition:getUserCompetitionApplyInfo:query') &&
          item.operationStatus != 'retired'
        "
      >
        <el-button type="primary" class="xinxi-btn" @click="handleClick(item)"
          >报名信息调整</el-button
        >
        <div class="tuisai">
          可申请退赛、调整队员、指导教师信息、或重新缴费等操作
        </div>
      </div>
    </el-card>
    <pagination
      v-show="page.total > 0"
      :total="page.total"
      style="margin: 30px 0"
      v-model:page="page.pageNum"
      v-model:limit="page.pageSize"
      @pagination="getlist"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { teamList } from "@/api/team";
import { useRoute, useRouter } from "vue-router";
// 分页组件
import { ElMessage, ElMessageBox } from "element-plus";
import { getinfo } from "@/utils/auth.js";
import Pagination from "@/components/Pagination";
import auth from "@/plugins/auth.js";
const route = useRoute();
const router = useRouter();
const queryParams = ref({});
const { proxy } = getCurrentInstance();
const { change_type } = proxy.useDict("change_type");
const userinfo = ref({});
onMounted(() => {
  userinfo.value = JSON.parse(getinfo() || "{}");
  console.log(userinfo.value);
  if (userinfo.value.authStatus != 5) {
    ElMessageBox.confirm("暂未进行实名认证,去认证", "提示", {
      confirmButtonText: "确定",
      type: "warning",
    }).then(() => {
      router.push({
        path: "/personal/accountmanagement",
        query: {
          classification: "nameauthentication",
        },
      });
    });
  }
}
)
const formatIdCard = (idCard) => {
  if (!idCard) return "";
  return idCard.substring(0, 3) + "*******" + idCard.substring(10);
};

const formatPhone = (phone) => {
  if (!phone) return "";
  return phone.substring(0, 3) + "******" + phone.substring(9);
};

const sousuo = () => {
  if (queryParams.value.time) {
    queryParams.value.registrationStartTime = queryParams.value.time[0];
    queryParams.value.registrationEndTime = queryParams.value.time[1];
  } else {
    queryParams.value.registrationStartTime = null;
    queryParams.value.registrationEndTime = null;
  }
  if (queryParams.value.keyword) {
    queryParams.value.keyword = queryParams.value.keyword.trim();
  } else {
    queryParams.value.keyword = null;
  }
  getlist();
};
const page = ref({
  total: 0,
  pageNum: 1,
  pageSize: 10,
});
// 虚拟数据
const xvnishujv = ref([]);
const getlist = () => {
  const params = {
    pageNum: page.value.pageNum,
    pageSize: page.value.pageSize,
  };
  teamList(queryParams.value, params).then((res) => {
    console.log(res);
    xvnishujv.value = res.rows;

    page.value.total = res.total;
  });
};
const handleClick = (item) => {
  console.log(item.teamCode);
  router.push({
    path: "/personal/teamdetails",
    query: {
      teamCode: item.teamCode,
    },
  });
};

watch(
  () => queryParams.value.keyword,
  (newVal) => {
    if (newVal) {
      queryParams.value.keyword = newVal.trim();
    }
  }
);

getlist();
</script>

<style scoped lang="scss">
:deep(.el-form-item__label) {
  font-weight: bold;
}

.cardw {
  // display: flex;
  // align-items: center;
  margin-bottom: 20px;
}

.formw {
  width: 1095px;
  // display: flex;
}
.ipt {
  width: 180px;
  height: 48px;
}

:deep(.el-form-item__label) {
  width: 125px;
  font-size: 14px;
}

:deep(.el-range-editor.el-input__wrapper) {
  width: 320px;
  padding: 0;
  height: 48px;
  font-size: 12px;
}
:deep(.el-date-editor .el-range-input) {
  font-size: 14px;
}
:deep(.el-date-editor .el-range-separator) {
  padding: 0;
  flex: 0;
}
.search-btn {
  background: #3169f8;
  width: 100px;
  height: 48px;
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 16px;
  color: #ffffff;
  line-height: 22px;
  text-align: center;
  font-style: normal;
  text-transform: none;
}
// .team{
//   width: 100%;
// }

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 15px;
}

.team-name {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: bold;
  font-size: 20px;
  color: #333333;
  line-height: 29px;
  text-align: left;
  font-style: normal;
  text-transform: none;
}

.team-title {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 14px;
  color: #333333;
  line-height: 20px;
  text-align: left;
  font-style: normal;
  text-transform: none;
  margin-top: 10px;
}
.team-payment {
  font-size: 14px;
  color: #909399;
  padding: 4px 12px;
  background: #f4f4f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 20px;
}
.team-status {
  background: #ffffff;
  border-radius: 6px 6px 6px 6px;
  border: 1px solid #3169f8;
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 16px;
  color: #3169f8;
  line-height: 22px;
  text-align: center;
  font-style: normal;
  text-transform: none;
  padding: 4px 12px;
}
.team-tuankuanstatus {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 16px;
  color: #51c512;
  line-height: 22px;
  text-align: left;
  font-style: normal;
  text-transform: none;
}
.tiaozheng {
  margin-top: 20px;
  display: flex;

  align-items: center;
}
.tuisai {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 14px;
  color: #ffa743;
  line-height: 20px;
  text-align: center;
  font-style: normal;
  text-transform: none;
}
.xinxi-btn {
  margin-right: 20px;
  width: 140px;
  height: 40px;
  background: #3169f8;
  border-radius: 6px 6px 6px 6px;
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 16px;
  color: #ffffff;
  line-height: 22px;
  text-align: center;
  font-style: normal;
  text-transform: none;
}
</style>