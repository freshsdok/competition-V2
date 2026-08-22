<template>
  <div style="background-color: #f5f5f5">
    <div class="container-custom" v-loading="loading">
      <Breadcrumbar />
      <el-tabs v-model="orderType" class="demo-tabs" @tab-click="handleClick" v-if="showTabs">
        <el-tab-pane label="报名" name="competition"></el-tab-pane>
        <el-tab-pane label="赛证互通" name="cert"></el-tab-pane>
      </el-tabs>
      <el-card
        style="background-color: #fff; margin: 20px 0"
        v-for="(team, teamIndex) in teamList"
        :key="team.id"
      > 
        <!-- 赛证互通 -->
        <div class="team-list" v-if="team?.commodityType === 'cert'">
          <div class="team-item">
            <!-- 团队模块，整合赛事信息和团队信息 -->
            <div class="team-module">
              <!-- 团队基本信息，包括赛事名称、团队编号、团队名称、带队老师和指导教师 -->
              <div class="team-header">
                <div
                  class="competition-name"
                  style="
                    font-size: 18px;
                    font-weight: bold;
                    margin-bottom: 12px;
                  "
                >
                  {{ team.ruleName }}
                </div>
              </div>
              <!-- 队员信息表格，直接展示 -->
              <el-table
                :ref="(el) => setTableRef(el, teamIndex)"
                :data="[team]"
                style="width: 100%; margin-bottom: 20px"
                :header-cell-style="{ background: '#fafafa', padding: '8px 0' }"
                :cell-style="{ padding: '8px 0' }"
                @selection-change="
                  (selection) => handleSelectionChange(selection, teamIndex)
                "
                row-key="userId"
              >
                <el-table-column
                  type="selection"
                  width="30"
                  align="center"
                  :reserve-selection="true"
                  :selectable="(row) => row.invoiceStatus == 0"
                />
                <el-table-column
                  prop="originCertList"
                  label="申请人姓名"
                  width="160"
                  align="center">
                  <template #default="scope">
                    {{ scope.row.userName || '-' }}
                  </template>
                </el-table-column>
                <el-table-column
                  prop="originCertList"
                  label="源证书名称"
                  min-width="100"
                  align="center">
                  <template #default="scope">
                    {{ scope.row.originCertList.map((item) => item.certConfigName).join(',') }}
                  </template>
                </el-table-column>
                <el-table-column
                  prop="targetCertList"
                  label="目标证书名称"
                  min-width="100">
                  <template #default="scope">
                    {{ scope.row.targetCertList.map((item) => item.certConfigName).join(',') }}
                  </template>
                </el-table-column>
                <el-table-column
                  prop="invoiceStatus"
                  label="状态"
                  min-width="80"
                  align="center"
                >
                  <template #default="scope">
                    <!-- {{ scope.row.invoiceStatus == 1 ? "已开票" : "未开票" }} -->
                    <span v-if="scope.row.invoiceStatus == 0">未开票</span>
                    <span v-if="scope.row.invoiceStatus == 1" style="color: red"
                      >已开票</span
                    >
                    <span v-if="scope.row.invoiceStatus == 2" style="color: red"
                      >开票中</span
                    >
                  </template>
                </el-table-column>
                <el-table-column
                  prop="repayAmount"
                  label="金额"
                  min-width="110"
                  align="center"
                >
                  <template #default="scope"> ￥{{ scope.row.repayAmount }} </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>
        <!-- 团队信息列表，去掉嵌套卡片 -->
        <div class="team-list" v-else>
          <div class="team-item">
            <!-- 团队模块，整合赛事信息和团队信息 -->
            <div class="team-module">
              <!-- 团队基本信息，包括赛事名称、团队编号、团队名称、带队老师和指导教师 -->
              <div class="team-header">
                <div
                  class="competition-name"
                  style="
                    font-size: 18px;
                    font-weight: bold;
                    margin-bottom: 12px;
                  "
                >
                  {{ team.competitionName }}-{{ team.competitionTrackName }}-{{
                    team.secondLevelName
                  }}
                </div>
                <div
                  class="team-basic-info"
                  style="
                    display: flex;
                    flex-wrap: wrap;
                    gap: 20px;
                    margin-bottom: 15px;
                  "
                >
                  <div class="info-item">
                    <span class="label">团队编号：</span>
                    <span class="value">{{ team.teamCode }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">团队名称：</span>
                    <span class="value">{{ team.teamName }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">带队老师：</span>
                    <span class="value">{{ team.leaderTeacher }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">指导教师：</span>
                    <span class="value">{{ team.guideTeacher || "-" }}</span>
                  </div>
                </div>
              </div>

              <!-- 队员信息表格，直接展示 -->
              <el-table
                :ref="(el) => setTableRef(el, teamIndex)"
                :data="team.userInfo"
                style="width: 100%; margin-bottom: 20px"
                :header-cell-style="{ background: '#fafafa', padding: '8px 0' }"
                :cell-style="{ padding: '8px 0' }"
                @selection-change="
                  (selection) => handleSelectionChange(selection, teamIndex)
                "
                row-key="memberId"
              >
                <el-table-column
                  type="selection"
                  width="30"
                  align="center"
                  :reserve-selection="true"
                  :selectable="(row) => row.invoiceStatus == 0"
                />
                <el-table-column
                  prop="userName"
                  label="队员姓名"
                  width="100"
                  align="center"
                />
                <el-table-column
                  prop="idCard"
                  label="证件号"
                  width="180"
                  align="center"
                />
                <el-table-column
                  prop="phone"
                  label="手机号"
                  width="130"
                  align="center"
                />
                <el-table-column
                  prop="invoiceStatus"
                  label="状态"
                  width="80"
                  align="center"
                >
                  <template #default="scope">
                    <!-- {{ scope.row.invoiceStatus == 1 ? "已开票" : "未开票" }} -->
                    <span v-if="scope.row.invoiceStatus == 0">未开票</span>
                    <span v-if="scope.row.invoiceStatus == 1" style="color: red"
                      >已开票</span
                    >
                    <span v-if="scope.row.invoiceStatus == 2" style="color: red"
                      >开票中</span
                    >
                  </template>
                </el-table-column>
                <el-table-column
                  prop="fee"
                  label="金额"
                  width="110"
                  align="center"
                >
                  <template #default="scope"> ￥{{ scope.row.fee }} </template>
                </el-table-column>
                <el-table-column
                  prop="email"
                  label="邮箱"
                  width="200"
                  align="center"
                />
                <el-table-column
                  prop="classInfo"
                  label="学级"
                  width="80"
                  align="center"
                >
                  <template #default="scope">
                    {{ scope.row.classInfo || "-" }}
                  </template>
                </el-table-column>
                <el-table-column
                  prop="profession"
                  label="专业"
                  min-width="120"
                  align="center"
                >
                  <template #default="scope">
                    {{ scope.row.profession || "-" }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>
        
      </el-card>
     
      <el-card style="background-color: #fff; margin: 20px 0" v-if="teamList && teamList.length > 0">
        <!-- 底部操作栏 -->
        <div class="dibu">
          <div style="display: flex; align-items: center">
            <el-checkbox :model="selectAll" @change="handleSelectAll"
              >全选</el-checkbox
            >
            <span class="ml-[15px]">已选择 {{ selectedCount }}</span>
          </div>

          <el-button
            type="primary"
            size="large"
            class="go-invoice-btn"
            @click="handleGoInvoice"
          >
            去开票
          </el-button>
        </div>
      </el-card>
      
      <!-- 空数据提示 -->
      <el-empty v-if="!loading && teamList.length === 0" description="暂无数据" />
    </div>
  </div>
</template>

<script setup name="InvoicePreparation">
import Breadcrumbar from "@/components/breadcrumbar.vue";
import { ref, reactive, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { queryTeamAndUserByOrderId } from "@/api/personal/index";
import Cookies from "js-cookie";
const route = useRoute();
const router = useRouter();
const loading = ref(false);
const orderType = ref("competition");
const allDataCache = ref({ cert: [], competition: [] });
const orderindex = ref(0);
const ordertime = ref(null);
const showTabs = computed(() => {
  return allDataCache.value.cert.length > 0 && allDataCache.value.competition.length > 0;
});
const queryTeamAndUserByOrderIdlist = () => {
  loading.value = true;
  clearLoadMoreTimer();
  queryTeamAndUserByOrderId({}).then((res) => {
    if (res.code === 200) {
      allDataCache.value ={ 
        cert: res.data?.cert || [], competition: res.data?.competition || [] };
      allDataCache.value.competition.forEach((team) => {
        if (team.userInfo) {
          team.userInfo.forEach((player) => {
            player.orderId = team.orderId;
          });
        }
      });
      allDataCache.value.cert.forEach((team) => {
        team.orderId = team.orderId || team.id;
      });
      if (allDataCache.value.competition.length > 0) {
        orderType.value = "competition";
      } else if (allDataCache.value.cert.length > 0) {
        orderType.value = "cert";
      }
      updateTeamList();
    } else {
      allDataCache.value = { cert: [], competition: [] };
      teamList.value = [];
    }
  }).catch(() => {
    allDataCache.value = { cert: [], competition: [] };
    teamList.value = [];
  }).finally(() => {
    loading.value = false;
  });
};
const clearLoadMoreTimer = () => {
  if (ordertime.value) {
    clearTimeout(ordertime.value);
    ordertime.value = null;
  }
};
const updateTeamList = () => {
  clearLoadMoreTimer();
  teamList.value = [];
  playerTableRefs.value = [];
  orderindex.value = 0;
  const data = allDataCache.value[orderType.value] || [];
  data.forEach((team) => {
    if (team.userInfo) {
      team.userInfo.forEach((player) => {
        player.orderId = team.orderId;
      });
    }
  });
  teamList.value = data.slice(0, 30);
  if (data.length > 30) {
    ordertime.value = setTimeout(() => {
      loadMoreData();
    }, 100);
  }
};
const loadMoreData = () => {
  const data = allDataCache.value[orderType.value] || [];
  const ss = setInterval(() => {
    if (orderindex.value >= data.length) {
      clearInterval(ss);
      clearLoadMoreTimer();
      return;
    }
    orderindex.value += 30;
    const sss = data.slice(orderindex.value, orderindex.value + 30);
    teamList.value = [...teamList.value, ...sss];
  }, 500);
};
const handleClick = (tab) => {
  orderType.value = tab.props.name;
  selectAll.value = false;
  teamSelections.value = {};
  selectedPlayers.value = [];
  selectedCount.value = 0;
  playerTableRefs.value = [];
  updateTeamList();
}
queryTeamAndUserByOrderIdlist();
// 团队列表
const teamList = ref([]);

// 表格引用数组，用于多表格操作
const playerTableRefs = ref([]);

// 团队选择状态
const teamSelections = ref({});

// 全选状态
const selectAll = ref(false);

// 已选择的队员数量
const selectedCount = ref(0);

// 选中项数据
const selectedPlayers = ref([]);

// 初始化团队列表
onMounted(() => {
  // 从URL获取订单ID列表
  const orderIds = route.query.orderIds ? route.query.orderIds.split(",") : [];
});

// 设置表格引用
const setTableRef = (el, teamIndex) => {
  if (el) {
    playerTableRefs.value[teamIndex] = el;
  }
};

// 表格选择变化处理
const handleSelectionChange = (selection, teamIndex) => {
  // 更新当前团队的选择
  teamSelections.value[teamIndex] = selection;
  console.log(selection, teamIndex,teamSelections.value,'xx...............');
  
  // 更新选中的队员数据
  updateSelectedPlayers();

  // 检查是否所有表格都已全选（只考虑可勾选的项）
  const allTablesSelected = teamList.value.every((team, index) => {
    const currentSelection = teamSelections.value[index] || [];
    // 赛证互通类型：直接判断team本身
    if (team?.commodityType === 'cert') {
      return team.invoiceStatus == 0 ? currentSelection.length === 1 : true;
    }
    // 报名类型：判断userInfo
    const selectableCount = team.userInfo.filter(
      (player) => player.invoiceStatus == 0
    ).length;
    return currentSelection.length === selectableCount;
  });

  // 更新底部全选状态
  selectAll.value = allTablesSelected;
};

// 更新选中的队员数据
const updateSelectedPlayers = () => {
  selectedPlayers.value = [];

  Object.values(teamSelections.value).forEach((selectedRows) => {
    if (selectedRows && selectedRows.length > 0) {
      selectedPlayers.value.push(...selectedRows);
    }
  });
  // 更新已选择的队员数量
  selectedCount.value = selectedPlayers.value.length;
};
const kaipiaoconter = ref({});

// 全选/取消全选
const handleSelectAll = (value) => {
  loading.value = true;
  // 确保selectAll状态与传入的value一致
  selectAll.value = value;
  if (value) {
    // 全选所有表格的所有行
    teamList.value.forEach((team, index) => {
      const tableRef = playerTableRefs.value[index];

      if (tableRef) {
        // 赛证互通类型：直接处理team本身（单条数据）
        if (team?.commodityType === 'cert') {
          if (team.invoiceStatus == 0) {
            tableRef.toggleRowSelection(team, true);
            teamSelections.value[index] = [team];
          }
        } else {
          // 报名类型：处理team.userInfo（队员列表）
          const selectablePlayers = team.userInfo.filter(
            (player) => player.invoiceStatus == 0
          );
          selectablePlayers.forEach((player) => {
            tableRef.toggleRowSelection(player, true);
          });
          teamSelections.value[index] = selectablePlayers;
        }
        loading.value = false;
      }
    });
  } else {
    // 取消全选所有表格的所有行
    teamList.value.forEach((team, index) => {
      const tableRef = playerTableRefs.value[index];
      if (tableRef) {
        // 赛证互通类型：取消team的选中
        if (team?.commodityType === 'cert') {
          tableRef.toggleRowSelection(team, false);
        } else {
          // 报名类型：取消所有队员的选中
          team.userInfo.forEach((player) => {
            tableRef.toggleRowSelection(player, false);
          });
        }
        teamSelections.value[index] = [];
        loading.value = false;
      }
    });
  }

  // 更新选中的队员数据
  updateSelectedPlayers();
};

// 去开票按钮点击事件
const handleGoInvoice = () => {
  // 使用实际选中的队员信息
  // const selectedPlayersForInvoice = selectedPlayers.value.map((player) => {
  //   // 找到该队员所属的团队信息
  //   const team = teamList.value.find((t) =>
  //     t.userInfo.some((p) => p.id === player.id)
  //   );
  //   return {
  //     teamId: team ? team.id : null,
  //     teamCode: team ? team.teamCode : "",
  //     teamName: team ? team.teamName : "",
  //     ...player,
  //   };
  // });
  
  kaipiaoconter.value = selectedPlayers.value.map((player) => {
    let userId = player.memberId;
    if (player?.commodityType === 'cert') {
      userId = player.userId;
    }
    return {
      orderId: player.orderId,
      userId: userId,
    };
  });
  console.log(selectedPlayers.value,'selectedPlayers.value');
  if (kaipiaoconter.value.length === 0) {
    ElMessage.warning("请至少选择一名队员");
    return;
  }

  // 这里可以跳转到开票页面
  ElMessage.success(
    "已选择 " + kaipiaoconter.value.length + " 名队员，准备开票..."
  );

  localStorage.setItem("kaipiaoconter", JSON.stringify(kaipiaoconter.value));
  router.push({
    path: "/personal/paymentrecords/invoiceIssuance",
    query: {
      orderType: orderType.value,
    }
  });
};
</script>

<style scoped lang="scss">
// 辅助类
.mb20 {
  margin-bottom: 20px;
}

.mb16 {
  margin-bottom: 16px;
}

.mb10 {
  margin-bottom: 10px;
}

// 团队列表样式
.team-list {
  .team-item {
    margin-bottom: 20px;
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;
  }

  .team-module {
    // 团队模块样式
  }

  .team-header {
    // 团队头部样式
  }

  .team-basic-info {
    .info-item {
      .label {
        display: inline-block;
        width: 80px;
        font-weight: 500;
        color: #666;
      }
      .value {
        color: #333;
      }
    }
  }
}

// 底部操作栏样式

.go-invoice-btn {
  // 样式参考"我的赛事"中的"去结算"按钮
  width: 120px;
  height: 40px;
  font-size: 16px;
  font-weight: bold;
}
.dibu {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.demo-tabs{
  padding: 0 2px;
  :deep(.el-tabs__header){
    margin-bottom: 0;
    .el-tabs__item{
      font-size: 16px;
      font-weight: bold;
    }
  }
}
</style>