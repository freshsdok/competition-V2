<template>
<el-popover :title="`未读消息(${unreadTotal || 0})`"
            width="300"
            popper-class="badge-site-list-popover"
            placement="right-start"
            v-model:visible="popoverVisible">
  <template #reference>
    <el-badge :value="unreadTotal || 0" :hidden="(!unreadTotal || unreadTotal== 0)" class="in-site-badge" :offset="[-1, 6]">
      <img src="@/assets/images/in-site.png" class="in-site w-[30px] h-[30px]" />
    </el-badge>
  </template>
  <div class="site-list">
    <template v-for="(item,index) in unreadNotificationList" :key="index">
      <div class="site-list-item" 
            v-if="index <= 5"
          @click="goDetail(item)">
        <div class="site-list-item-title">{{ item.title }}</div>
        <div class="site-list-item-time">{{ moment(item.sendTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
      </div>
    </template>
    <el-empty description="暂无数据" image-size="80" v-if="(!unreadNotificationList || unreadNotificationList.length === 0)"></el-empty>
    <div class="site-list-more">
      <div class="hvr-grow" @click="goMore">查看更多</div>
    </div>
  </div>
</el-popover>
</template>
<script setup name="Site">
import { getUnreadNotificationList } from "@/api/site/index";
import moment from "moment";
import {useRoute,useRouter} from 'vue-router'
import { useCounterStore } from "@/stores/index";
import { getToken } from "@/utils/auth";
let route = useRoute()
let router = useRouter()
const store = useCounterStore()

let unreadNotificationList = $ref([]);
let unreadTotal = $ref(null);

const getDetail = () => {
  if(getToken()){
    getUnreadNotificationList().then(async (res) => {
      unreadNotificationList = res.rows || [];
      unreadTotal = res.total || 0;
    }).catch(() => {});
  }
};

let popoverVisible = $ref(false);
const goDetail = (item) => {
  router.push({path:'/site/detail',query:{siteId:item.id}})
  popoverVisible = false;
}
const goMore = () => {
  router.push({path:'/site/list',query:{}})
  popoverVisible = false;
}



watch(() => route, () => {
  if((route.path == '/site/detail' || route.path == '/site/list')){
    nextTick(() => {
      setTimeout(() => {
        getDetail()
      }, 500)
    })
  }
}, { immediate: true,deep: true })

// 监听消息更新触发
watch(() => store.messageUpdate, () => {
  getDetail()
})

// 初始化
onMounted(() => {
  getDetail()
})
</script>

<style lang="scss" scoped>
.in-site-badge{
  margin-right: 20px;
  .in-site{
    width: 30px !important;
    height: 30px !important;
    border-radius: none !important;
  }
}
.site-list{
  .site-list-item{
    padding-left: 12px;
    padding-right: 12px;
    &:first-child{
      padding-top: 12px;
      border-top: 1px solid #E1E1E1;
    }
    margin-bottom: 12px;
    cursor: pointer;
    .site-list-item-title{
      @include ellipsis(1);
      font-size: 16px;
      color: #333333;
      font-weight: 550;
      text-align: left;
      margin-bottom: 2px;
      &:hover{
        color: #3169F8;
      }
    }
    .site-list-item-time{
      font-size: 14px;
      color: #999999;
    }
  }
  .site-list-more{
    border-top: 1px solid #E1E1E1;
    font-size: 16px;
    color: #3169F8;
    text-align: right;
    cursor: pointer;
    width: 100%;
    padding: 12px;
  }
}
</style>
<style lang="scss">
.badge-site-list-popover{
  padding: 0 !important;
  .el-popover__title{
    padding: 12px !important;
    margin: 0 !important;
  }
}
</style>
