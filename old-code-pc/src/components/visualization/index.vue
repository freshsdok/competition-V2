<template>
  <div class="ds_visualization_page">
    <!-- 按照pageContent数组的原始顺序渲染组件 -->
    <template v-for="(item, index) in pageContent" :key="item.id || index">
      <template v-if="item.type === 'global_banner'">
        <Ds_global_banner :info="item" />
      </template>
      <template v-else-if="item.type === 'pc_home_tournament'">
        <Ds_tournament_home :info="item" />
      </template>
      <template v-else-if="item.type === 'pc_tournament_list'">
        <Ds_list_one :info="item" />
      </template>
      <template v-else-if="item.type === 'pc_home_learn'">
        <Ds_learn_home :info="item" />
      </template>
      <template v-else-if="item.type === 'pc_home_information'">
        <Ds_information_home :info="item" />
      </template>
      <template v-else-if="item.type === 'pc_support'">
        <Ds_support_home :info="item" />
      </template>
      <template v-else-if="item.type === 'blank_spacing'">
        <Ds_blank_spacing :info="item" />
      </template>
      <template v-else-if="item.type === 'hyperlink'">
        <Ds_hyperlink :info="item" />
      </template>
      <template v-else-if="item.type === 'text'">
        <Ds_text :info="item" />
      </template>
      <template v-else-if="item.type === 'pc_information'">
        <Ds_information :info="item" />
      </template>
    </template>
    <el-empty
        v-if="(pageLoading === false) && (!pageContent || pageContent.length === 0)"
        description="暂无数据"
      />
  </div>
</template>
<script setup>
import { useRoute } from 'vue-router'
let route = useRoute()
const props = defineProps({
  pageUrl:{
      type: String,
      default: ''
  }
});
import { contentPagePC,contentPagePcById } from '@/api/visualization'

// 获取可视化页面详情
let pageDetail = $ref(null)
let pageContent = $ref(null)
let pageLoading = $ref(null)
const getDetail = ()=>{
  pageLoading = true
  if(route.query && (route.query.pageId && route.query.preview)){
    contentPagePcById(route.query.pageId).then(res => {
      if(res.code == 200 && res.data) {
        pageDetail = res.data
        pageContent = res.data.pageContent ? JSON.parse(res.data.pageContent) : []
        
        console.log(pageContent)
      } else {
        pageDetail = null
        pageContent = []
      }
      pageLoading = false
    }).catch(() => {
      pageLoading = false
    })
  }else{
    contentPagePC({
      pt: 'pc',
      url: props.pageUrl
    }).then(res => {
      if(res.code == 200 && res.data) {
        pageDetail = res.data
        pageContent = res.data.pageContent ? JSON.parse(res.data.pageContent) : []
        console.log(pageContent)
      } else {
        pageDetail = null
        pageContent = []
      }
      pageLoading = false
    }).catch(() => {
      pageLoading = false
    })
  }
}
getDetail()
</script>
<style lang="scss" scoped>
.ds_visualization_page{
  width: 100%;
  overflow: hidden;
  min-height: 50vh;
  box-sizing: border-box;
  :deep(.model-page:nth-child(odd)){
    background: #fff;
  }
  :deep(.model-page:nth-child(even)){
    background: #F2F5F7;
  }
}

</style>
