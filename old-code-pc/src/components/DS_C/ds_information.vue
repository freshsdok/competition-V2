<template>
  <div class="w-full page" :style="detail.style">
    <!-- <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick" > -->
    <!-- <el-tab-pane label="最新资讯" name="first"></el-tab-pane> -->
    <el-tabs v-model="activeName" class="demo-tabs">
      <el-tab-pane label="公告通知" name="second"></el-tab-pane>
    </el-tabs>
    <div class="container-custom container-custom-self">
      <!-- 列表 -->
      <template v-if="comData.length">
        <!-- 前三条 -->
        <template v-if="showTopComData">
          <div class="flex p-[30px] bg-[#ffffff] shadow-md rounded-[12px]">
            <el-carousel
              indicator-position="outside"
              @change="handleChange"
              :initial-index="initialIndex"
              class="shrink-0 w-[500px]"
            >
              <el-carousel-item
                v-for="(item, index) in topComData"
                :key="index"
              >
                <el-image
                  :src="
                    activeName == 'second' ? item.noticeImage : item.newsImage
                  "
                  alt=""
                  class="w-full h-full object-cover rounded-[12px]"
                >
                  <template #error>
                    <el-skeleton animated class="w-full h-full">
                      <template #template>
                        <el-skeleton-item
                          style="width: 100%; height: 300px"
                          variant="image"
                        />
                      </template>
                    </el-skeleton>
                  </template>
                </el-image>
              </el-carousel-item>
            </el-carousel>
            <div
              class="ml-20 w-[calc(100%-570px)] flex flex-col justify-between"
            >
              <div>
                <div
                  class="text-[24px] text-[#333333] font-bold overflow-hidden whitespace-nowrap text-ellipsis"
                >
                  {{
                    activeName == "first"
                      ? topComData[initialIndex]?.newsTitle
                      : topComData[initialIndex]?.noticeTitle
                  }}
                </div>
                <div class="pt-[35px] text-[#999999]">
                  <span class="mr-[50px]"
                    >发布时间：{{
                      moment(topComData[initialIndex]?.publishTime).format(
                        "YYYY-MM-DD"
                      )
                    }}</span
                  >
                  <span v-if="activeName == 'first'"
                    >浏览量：{{
                      topComData[initialIndex]?.readingQuantity
                    }}</span
                  >
                </div>
                <div class="pt-[35px] text-[18px] text-[#666666] line-clamp-3">
                  {{
                    activeName == "first"
                      ? topComData[initialIndex]?.newsAbstract
                      : topComData[initialIndex]?.noticeAbstract
                  }}
                </div>
              </div>
              <div
                class="pt-[35px] text-[18px] text-[#3169F8] flex items-center mb-[60px] cursor-pointer"
                @click="routerHandleClick(topComData[initialIndex])"
              >
                <span>查看更多 ⇀</span>
              </div>
            </div>
          </div>
        </template>
        <!--  剩余数据/或者所有 -->
        <div class="mt-[30px] px-[30px] bg-[#ffffff] shadow-md">
          <div
            class="border-b border-[#E1E1E1] flex py-6 w-full box-border"
            v-for="(item, index) in bottomComData"
            :key="index"
            @click="routerHandleClick(item)"
          >
            <div
              class="flex items-center border-r-2 pl-[30px] pr-[50px] box-border"
            >
              <span class="text-bold text-[36px]">{{
                getDay(item.publishTime)
              }}</span>
              <span class="text-[38px] text-[#999999] mx-[10px]">/</span>
              <span class="flex flex-col text-[14px] text-[#666666] font-bold">
                <text>{{ getMonth(item.publishTime) }}月</text>
                <text>{{ getYear(item.publishTime) }}</text>
              </span>
            </div>
            <div
              class="pl-12 box-border w-[81%] overflow-hidden whitespace-nowrap text-ellipsis"
            >
              <div
                class="text-bold text-[20px] overflow-hidden whitespace-nowrap text-ellipsis"
              >
                {{ item.newsTitle || item.noticeTitle }}
              </div>
              <div
                class="text-[16px] text-[#666666] overflow-hidden whitespace-nowrap text-ellipsis py-4"
              >
                {{ item.newsAbstract || item.noticeAbstract }}
              </div>
              <div class="text-[14px] text-[#3169F8] cursor-pointer">
                查看更多 ⇀
              </div>
            </div>
          </div>
        </div>
        <div class="flex justify-center items-center my-[50px]">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="pageTotal"
            v-model:current-page="pageNum"
            @current-change="handleCurrentChange"
          />
        </div>
      </template>
      <template v-else>
        <!-- 骨架屏，用于数据加载时显示 -->
        <el-skeleton animated v-if="pageLoading" :rows="10"> </el-skeleton>
        <el-empty
          description="暂无数据"
          v-if="pageLoading === false && (!comData || comData.length === 0)"
        ></el-empty>
      </template>
    </div>
  </div>
</template>


<script setup>
import { visualizationPageMobile } from "@/api/visualization";
import { useDict } from "@/utils/dict";
const { competition_type, join_type } = useDict(
  "competition_type",
  "join_type"
);
import { useRouter, useRoute } from "vue-router";
const router = useRouter();
const route = useRoute();
import moment from "moment";
const props = defineProps({
  // 数据
  info: {
    type: Object,
    default: {},
  },
});
let detail = $ref({});
let activeName = $ref("second");
const handleClick = (tab, event) => {
  if (tab.props.name == "first") {
    initQuery(detail.dataSourceUrl);
  }
  if (tab.props.name == "second") {
    initQuery(detail.dataSourceTwoUrl);
  }
};

// 点击列表项跳转详情页
const routerHandleClick = (item) => {
  if (activeName == "first") {
    router.push({
      path: "/information/detail",
      query: {
        newsId: item.newsId,
      },
    });
  }
  if (activeName == "second") {
    router.push({
      path: "/information/detail",
      query: {
        noticeId: item.noticeId,
      },
    });
  }
};
// 获取月份
const getMonth = (time) => {
  let month = moment(time).format("MM");
  return month;
};
// 获取年份
const getYear = (time) => {
  let year = moment(time).format("YYYY");
  return year;
};
// 获取日期
const getDay = (time) => {
  let day = moment(time).format("DD");
  return day;
};

let comData = $ref([]);
// 筛选
let query = reactive({});

const initQuery = (dataSourceUrl) => {
  pageTotal = 0;
  pageLoading = null;
  pageNum = 1;
  comData = [];
  getDsApi(dataSourceUrl);
};
// 分页切换
const handleCurrentChange = (val) => {
  console.log("val11111111111111111111", val, activeName);
  pageNum = val;
  if (activeName == "first") {
    pageLoading = null;
    comData = [];
    getDsApi(detail.dataSourceUrl);
  }
  if (activeName == "second") {
    pageLoading = null;
    comData = [];
    getDsApi(detail.dataSourceTwoUrl);
  }
};
// 轮播索引
let initialIndex = $ref(0);
const handleChange = (index) => {
  initialIndex = index;
};
// 是否展示前三条
const showTopComData = $computed(() => {
  let show =
    ((activeName == "first" && detail.topInfoCarousel) ||
      (activeName == "second" && detail.topNoticeCarousel)) &&
    pageNum == "1";
  return show;
});
// 获取前三条数据
const topComData = $computed(() => {
  if (showTopComData) {
    return comData.slice(0, 3);
  }
});
// 获取后三条数据或者全部数据
const bottomComData = $computed(() => {
  if (showTopComData) {
    return comData.slice(3);
  } else {
    return comData;
  }
});
// API调用函数
// 状态管理
let pageNum = $ref(1);
let pageTotal = $ref(0);
let pageLoading = $ref();
const getDsApi = async (dataSourceUrl) => {
  pageLoading = true;
  let params = {
    pageNum: pageNum,
    pageSize: 10,
    ...query,
  };
  visualizationPageMobile(dataSourceUrl, "get", params)
    .then((res) => {
      if (res.code === 200) {
        pageTotal = res.total || 0;
        let resRows = res.rows || [];
        if (comData.length >= pageTotal) {
          pageLoading = false;
          // 列表已加载完毕
          return;
        }
        comData = [...comData, ...resRows];
      } else {
        comData = [];
      }
      pageLoading = false;
    })
    .catch(() => {
      pageLoading = false;
    });
};

watch(
  () => props.info,
  (newVal) => {
    detail = newVal;
    // 确保detail及其数据源URL存在
    if (!detail || !detail.dataSourceUrl || !detail.dataSourceTwoUrl) {
      return;
    }
    // 根据tabs值决定显示哪个标签页和加载哪个数据源
    const tabs = route?.query?.tabs;
    if (tabs == "first") {
      activeName = "first";
      initQuery(detail.dataSourceUrl);
    } else {
      // 合并重复的逻辑分支：tabs为'first'或其他值时都显示第一个标签页
      activeName = "second";
      initQuery(detail.dataSourceTwoUrl);
    }

    // watchEffect默认会立即执行一次，不需要额外配置immediate选项
  },
  { immediate: true }
); // 立即执行，处理初始值
</script>

<style lang="scss" scoped>
.page {
  overflow: hidden;
}
.container-custom-self {
  padding-top: 45px;
  padding-bottom: 100px;
  position: relative;
}
.el-tabs {
  background: #ffffff;
  :deep(.el-tabs__nav-scroll) {
    padding-left: 15%;
  }
  :deep(.el-tabs__item) {
    height: 60px;
  }
  :deep(.el-tabs__header) {
    margin: 0;
  }
}

.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 180px;
  height: 50px;
  background: #eff4ff;
  border-radius: 10px 10px 10px 10px;
  margin: 045px auto;
  font-size: 14px;
  color: #3169f8;
}
.no-more {
  position: absolute;
  width: 100%;
  text-align: center;
  bottom: 20px;
  font-size: 14px;
}
.acticon-img {
  width: 12px;
}
</style>
