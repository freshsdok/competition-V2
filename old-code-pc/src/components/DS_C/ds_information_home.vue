<template>
  <div class="w-full model-page" :style="detail.style">
    <div class="container-custom container-custom-self">
      <!-- 列表 -->
      <div class="w-full t-title font-bold text-center text-[#333333]">
        {{ (detail && detail.title) || "公告通知" }}
      </div>
      <!-- <el-tabs v-model="activeName" @tab-click="handleClick"> -->
      <el-tabs v-model="activeName">
        <!-- <el-tab-pane label="最新资讯" name="first"></el-tab-pane> -->
        <el-tab-pane label="公告通知" name="second"></el-tab-pane>
      </el-tabs>
      <template v-if="comData.length">
        <div class="filter-list">
          <div
            class="filter-list-item cursor-pointer flex flex-col justify-between hvr-grow-shadow"
            v-for="(item, index) in comData"
            :key="index"
            @click="routerHandleClick(item)"
          >
            <div class="top-con">
              <el-image
                :src="
                  activeName == 'second' ? item.noticeImage : item.newsImage
                "
                fit="cover"
                class="w-full h-full top-img"
              ></el-image>
            </div>
            <div class="filter-list-content">
              <div class="filter-info">
                <div class="flex justify-between align-center">
                  <div class="title">
                    {{ item.noticeTitle || item.newsTitle }}
                  </div>
                </div>
                <div class="description flex justify-start items-center">
                  <div v-if="item.type == 'cert'"></div>
                  <div v-else>{{ item.noticeAbstract || item.newsAbstract }}</div>
                </div>
              </div>
              <div class="flex justify-between action">
                <div class="time">
                  {{ moment(item.publishTime).format("YYYY-MM-DD") }}
                </div>
                <div class="flex items-center justify-end more hvr-float">
                  {{ item.type == 'cert' ? '申请证书' : '查看更多' }}
                </div>
              </div>
            </div>
          </div>
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
import { useRouter } from "vue-router";
const router = useRouter();
import moment from "moment";
const props = defineProps({
  // 数据
  info: {
    type: Object,
    default: {},
  },
});

// 活动名称
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
  if(item.type == 'cert'){ //证书
    router.push(`/certInterconnect/description?ruleId=${item.noticeId}`)
    return;
  }
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
let comData = $ref([]);
// 筛选
let query = reactive({});
let detail = $ref({});
const initQuery = (dataSourceUrl) => {
  pageLoading = null;
  comData = [];
  getDsApi(dataSourceUrl);
};

// 状态管理
let pageNum = $ref(1);
let pageLoading = $ref();
const getDsApi = async (dataSourceUrl) => {
  pageLoading = true;
  let params = {
    pageNum: pageNum,
    pageSize: 6,
    ...query,
  };
  comData = [];
  visualizationPageMobile(dataSourceUrl, "get", params)
    .then((res) => {
      if (res.code === 200) {
        let resRows = res.rows || [];
        pageLoading = false;
        comData = resRows;
      } else {
        comData = [];
      }
      pageLoading = false;
    })
    .catch(() => {
      pageLoading = false;
    });
};

// 监听props变化
// 状态管理
watch(
  () => props.info,
  (newVal) => {
    if (newVal) {
      detail = newVal;
      if (!detail || !detail.dataSourceTwoUrl) {
        return;
      }
      console.log(detail,'detail')
      activeName = "second";
      initQuery(detail.dataSourceTwoUrl);
    }
  },
  { immediate: true }
); // 立即执行，处理初始值
</script>

<style lang="scss" scoped>
.model-page {
  background: #ffffff;
  overflow: hidden;
}
.container-custom-self {
  padding-top: 45px;
  padding-bottom: 100px;
  position: relative;
  .t-title {
    font-size: 36px;
    margin-bottom: 50px;
  }
}
.filter-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  grid-gap: 40px;
  .filter-list-item {
    background: #ffffff;
    border-radius: 8px;
    box-sizing: border-box;
    box-shadow: 0 2px 15px 0 rgba(0, 0, 0, 0.1);
    .top-con {
      .top-img {
        width: 100%;
        height: 300px;
        border-radius: 8px 8px 0 0;
      }
    }
    .filter-info {
      padding-bottom: 15px;
      .title {
        font-size: 20px;
        color: #000000;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .price {
        margin-top: 10px;
        font-size: 15px;
        color: #ff0000;
      }
      .description {
        font-size: 15px;
        color: #999999;
        margin-top: 4px;
        height: 66px;
        @include ellipsis(3);
      }
    }
    .filter-list-content {
      padding: 15px;
      .action {
        font-size: 16px;
        color: #666666;

        .more {
          font-size: 18px;
          color: #3169f8;
        }
      }
    }
  }
}

:deep(.el-tabs) {
  margin-bottom: 35px;
  .el-tabs__item {
    width: 114px;
    height: 36px;
    font-size: 20px;
    color: #333333;
    border-radius: 14px;
    border-radius: 8px;
    padding: 0;
    // 添加过渡效果
    transition: all 0.2s ease;
    &:hover {
      font-size: 20px;
      color: #ffffff !important;
      background: #3169f8;
    }
    + .el-tabs__item {
      margin-left: 20px;
    }
  }
  .el-tabs__header {
    margin: 0px;
  }
  .el-tabs__content {
    margin: 0;
  }
  .is-active {
    font-size: 20px;
    color: #ffffff;
    background: #3169f8;
  }
  .el-tabs__active-bar {
    display: none;
    height: 0;
  }
  .el-tabs__nav-wrap {
    &::after {
      background: none;
      display: none;
      height: 0;
    }
  }
}
</style>
