<template>
  <div class="zong">
    <div v-if="saishi.length == 0" style="font-size: 20px;text-align: center;line-height: 30px;min-height: 400px;">  <el-empty description="暂无数据"></el-empty></div>
    <div v-else>
      <div v-for="(item, index) in saishi" :key="index">
        <div style="display: flex">
          <div class="neirong">
            <div class="neiimg">
              <img
                :src="item.competitionImage"
                alt=""
                style="cursor: pointer"
                @click="
                  routerTo(
                    `/event/detail?competitionId=${item.competitionId}&competitionSeriesId=${item.competitionSeriesId}`
                  )
                "
              />
              <!-- <dict-tag
              :options="competition_status"
              :value="item.checkStatus"
              style="margin-right: 10px; z-index: 10; position: absolute"
            /> -->
            </div>
            <div class="neitext">
              <div class="title">
                {{ item.competitionName }}{{ item.stageName }}
              </div>
              <div class="price">{{ item.competitionDesc }}</div>
              <div class="bq">
                <div style="display: flex">
                  <dict-tag
                    :options="join_type"
                    :value="item.joinType"
                    style="margin-right: 10px"
                  />
                  <dict-tag
                    :options="competition_type"
                    :value="item.competitionType"
                  />
                </div>
              </div>
              <div class="time">
                <div>赛事报名时间：{{ item.registrationTime }}</div>
                <div>
                  <span>
                    <Star
                      style="width: 16px; height: 16px; display: inline-block"
                    />
                    {{ item.competitionCollectNum }}
                  </span>
                  <span>
                    <Share
                      style="width: 16px; height: 16px; display: inline-block"
                    />
                    {{ item.competitionShareNum }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="zhuangtai">
            <div class="zhuangti" v-if="item.applyStatus == 4">已报名</div>
            <div
              class="zhuangti"
              v-else-if="item.applyStatus == 'paid' && item.worksFlag == 0"
            >
              待上传作品
            </div>
            <div
              class="zhuangti"
              v-else-if="
                item.applyStatus == 'paid' &&
                item.worksFlag == 1 &&
                !item.worksStatus
              "
            >
              已上传作品
            </div>
            <div class="zhuangti" v-else>
              <dict-tag
                :options="works_status"
                :value="item.worksStatus"
                style="margin-right: 10px; z-index: 10"
              />
            </div>

            <div style="margin-top: 30px">
              <el-tag
                type="success"
                class="eltags"
                v-if="item.applyStatus == 'paid'"
                @click="shangchuan(item)"
                >上传作品</el-tag
              >
              <!-- worksFlag是否已上传作品   applyStatus == 'paid'是否支付 worksStatus是否评审  -->
              <el-tag
                type="warning"
                v-if="item.applyStatus == 'paid' && item.worksFlag == 1"
                class="eltags"
                @click="chengjichaxun(item)"
                >成绩查询</el-tag
              >
              <el-tag
                type="primary"
                class="eltags"
                v-if="item.applyStatus == 'paid'"
                @click="jiaofeijiluchaxun(item)"
                >我的订单</el-tag
              >
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
  </div>
</template>
<script setup>
import { userGradeInfolist } from "@/api/index";
import shangchuanzuopins from "@/views/personal/components/shangchuan.vue";
import { ElMessage, genFileId } from "element-plus";
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
} from "@/api/personal/index";
import { useRouter } from "vue-router";
const router = useRouter();
// 路由跳转
function routerTo(path) {
  router.push(path);
}
const saishi = ref([]);
const saishigetlist = () => {
  userCompetition().then((res) => {
    saishi.value = res.data;
  });
};
saishigetlist();
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
</style>