<template>
  <div class="global-page">
    <PageBanner :banner="bannerSrc" />
    <!-- 搜索 -->
    <div class="__search mt-[60px] mb-[70px] ">
      <el-input v-model="queryParams.keyWord" placeholder="请输入关键词搜索" class="mr-[15px] h-[54px]">
        <template #prefix>
          <el-icon>
            <Search class="text-[#64666A]" />
          </el-icon>
        </template>
      </el-input>
      <el-button @click="handleKeyWordChange" size="default" type="primary">搜索</el-button>
    </div>
    <!-- item -->
    <div class="w-[1320px] flex flex-wrap m-auto gap-[30px] mb-[20px] min-h-[50px]" v-loading="loading">
      <template v-for="(item, index) in tableData">
        <CertRuleItem :coverImg="item.icon" :title="item.rulerName" :certConditions="item.certConditions"
          :originCertList="item.originCertList" :time="item.updateTime ?? item.createTime"
          @click="() => handleRuleDetail(item)" />
      </template>
      <div class="w-[100%] flex items-center justify-center" v-if="total === 0 && !loading"><el-empty /></div>
      <div class="w-[100%] flex justify-center">
        <pagination v-show="total > 0" :total="total" :page-sizes="[10, 20, 30, 50, 100]"
          v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>
  </div>
</template>

<script setup>
  // *********** 插件 ***********
  import { useRouter } from "vue-router";
  const router = useRouter();

  // *********** API ***********
  import { getCertInterconnectRuleList } from "@/api/certInterconnect/index.js"

  // *********** store *********** 
  import { useCounterStore } from "@/stores/index";

  // *********** 工具 ***********
  import { getToken, getinfo } from "@/utils/auth";
  import Modal from "@/plugins/modal.js";

  // *********** 组件 ***********
  import CertRuleItem from "./components/CertRuleItem.vue"
  import PageBanner from "@/components/PageBanner/index.vue"
  import bannerSrc from '@/assets/images/certInterconnect_banner.png'

  // *********** 初始化 ***********
  const queryParams = ref({
    keyWord: '',
    pageNum: 1,
    pageSize: 10,
  })
  const loading = ref(true) // 是否加载中
  const tableData = ref([]) // 列表数据
  const total = ref(0) // 总条数

  // *********** computed ***********
  const isAuth = computed(() => !!getToken())

  // *********** 业务 ***********
  // 搜索
  const handleKeyWordChange = () => { 
    queryParams.value.pageNum = 1;
    getList()
  }
  // 获取列表
  const getList = async () => {
    loading.value = true;
    const { rows, total: count } = await getCertInterconnectRuleList(queryParams.value)
    tableData.value = rows;
    total.value = count;
    loading.value = false;
  }
  // 前往详情
  const handleRuleDetail = async (row) => {
    router.push(`/certInterconnect/description?ruleId=${row.ruleId}`)
  }

  onMounted(() => {
    getList()
  })
</script>

<style lang="scss" scoped>
  @keyframes slideUpBackground {
    0% {
      transform: scale(1);
    }

    100% {
      transform: scale(1.1);
    }
  }

  .__banner {
    img {
      width: 100%;
      object-fit: cover;
      animation: slideUpBackground 5s ease-out;
    }
  }

  .__search {
    display: flex;
    align-items: center;
    justify-content: center;

    & .el-input {
      width: 600px !important;
    }

    & .el-button {
      height: 52px !important;
      background: #3169F8 !important;
      padding: 0 25px !important;
    }
  }

  :deep(.el-pagination.is-background .el-pager li.is-active){
    background: #3169F8 !important;
  }
</style>