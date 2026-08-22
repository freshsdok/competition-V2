<template>
  <div class="detail-page">
    <div class="container-custom">
      <Breadcrumbar :breadcrumbarArr="breadcrumbarArr"/>
       <div class="custom-content">
         <div class="title">{{ pageDetail.newsTitle || pageDetail.noticeTitle }}</div>
         <div class="info flex justify-between items-center">
            <div class="flex flex-row justify-start items-center">
              <span class="mr-8">发布时间：{{ moment(pageDetail.publishTime).format('YYYY-MM-DD') }}</span>
              <span class="mr-8" v-if="pageDetail.newsSource">资讯来源：{{ pageDetail.newsSource }}</span>
              <span class="mr-8" v-if="pageDetail.noticeAuthor">发布人：{{ pageDetail.noticeAuthor }}</span>
              <span v-if="pageDetail.readingQuantity">浏览次数：{{ pageDetail.readingQuantity || 0 }}</span>
            </div>
            <p class="description-right-p flex justify-center items-center cursor-pointer"
                  @click="copyLink">
              <span>分享</span>
              <img src="@/assets/images/share.png" alt="" class="ml-1 description-right-p-img"></img>
            </p>
         </div>
         <div class="ql-container ql-snow">
            <div class="rich-content ql-editor" v-html="pageDetail.newsCont || pageDetail.noticeContent"></div>
         </div>
       </div>
    </div>     
  </div>
</template>
<script>
import moment from 'moment'
export default {
  name: 'detail',
  data() {
    return {
      breadcrumbarArr:  [],
    }
  },
  beforeRouteEnter(to, from, next) {
    let matchedArr = to.matched
    let breadcrumbarArr = matchedArr.map(item => {
      let meta = item.meta;
      if(item.path == to.path){
        if(to.query.newsId){
          meta.label = '新闻详情'
        }else if(to.query.noticeId){
          meta.label = '公告详情'
        }
      }
      return {
        ...item,
        meta
      }
    })
    next(vm => {
      vm.breadcrumbarArr = breadcrumbarArr
    });
  }
}
</script>
<script setup>
import Breadcrumbar from '@/components/breadcrumbar.vue'
import Modal from '@/plugins/modal.js'
import useClipboard from 'vue-clipboard3'
import { useRoute } from "vue-router";
import { getNewsInfo,getNoticeInfo } from '@/api/visualization/index.js'
import "@vueup/vue-quill/dist/vue-quill.snow.css"

// 复制分享链接
const { toClipboard } = useClipboard()
const copyLink = async()=>{
  const link = window.location.href
  await toClipboard(link)
  Modal.notifySuccess({
    message: '链接复制成功，快去分享给好友吧',
    type: 'success',
    title: '复制成功',
  })
}

let pageDetail = $ref({})

const route = useRoute();
const getDetail = ()=>{
  let apiFuc = ''
  let apiId = ''
  if(route.query.newsId){
    apiFuc = getNewsInfo
    apiId = route.query.newsId
  }else if(route.query.noticeId){
    apiFuc = getNoticeInfo
    apiId = route.query.noticeId
  }
  apiFuc(apiId).then(res => {
    if(res.code == 200) { 
      pageDetail = res.data
    }
  })
}
getDetail()
// 如何设置当前页面的meta数据
console.log(route.meta.label)
</script>
<style scoped lang="scss">
.detail-page{
  background-color: #F5F5F5 ;
  min-height: 45vh;
}
.custom-content{
  background: #FFFFFF;
  border-radius: 10px;
  margin: 40px 0px;
  padding: 60px 120px;
  .title{
    font-weight: bold;
    font-size: 30px;
    color: #333333;
  }
  .info{
    margin-top: 30px;
    background: #FAFAFA;
    border-radius: 2px;
    border: 1px solid #F6F6F6;
    font-size: 16px;
    color: #999999;
    padding: 20px 35px;
  }
  .description-right-p-img{
    width: 22px;
  }
}
</style>
