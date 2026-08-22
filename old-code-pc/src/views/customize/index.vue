<template>
<div class="base-page">
  <div class="container-custom self-custom">
    <div v-if="pageDetail?.menuName" class="menuName">{{ pageDetail?.menuName }}</div>
    <!-- 内容1 -->
     <template v-if="pageDetail?.columnType == '1'">

      
      <!-- 资讯列表 -->
      <div class="news-grid" v-if="pageDetail?.detailList">
        <div v-for="item in pageDetail.detailList" class="news-card" @click="goDetail(item)">
          <!-- 图片区域 -->
          <div class="news-image-wrapper">
            <el-image :src="item.detailImage"
                      fit="cover"
                      class="news-image"></el-image>
            <!-- 图片加载占位 -->
            <div class="image-placeholder" slot="placeholder">
              <el-icon :size="32" color="#dcdfe6"><Picture /></el-icon>
            </div>
            <!-- 图片错误处理 -->
            <div class="image-error" slot="error">
              <el-icon :size="32" color="#f56c6c"><PictureRounded /></el-icon>
            </div>
          </div>
          
          <!-- 内容区域 -->
          <div class="news-content">
            <h4 class="news-title">{{item.detailTitle}}</h4>
            <p class="news-description" >
              点击查看详情
            </p>
          </div>
          
          <!-- 悬停效果层 -->
          <div class="news-hover-layer">
            <el-icon :size="24"><Right /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 空状态优化 -->
      <div class="empty-state" v-else>
        <div class="empty-content">
          <el-icon :size="64" color="#e0e0e0"><DocumentRemove /></el-icon>
          <h3 class="empty-title">暂无资讯</h3>
          <p class="empty-description">当前分类下还没有相关资讯</p>
        </div>
      </div>      
     </template>
    <!-- 文件列表2 -->
     <template v-else-if="pageDetail?.columnType == '2'">
      <div class="file-section" v-if="pageDetail?.detailList">
        <!-- 标题和统计 -->
        <div class="file-section-header">
          <div class="file-count flex items-center">
            <el-icon :size="12" class="mr-[6px]"><FolderOpened /></el-icon>
            <span>{{ pageDetail.detailList.length }} 个文件</span>
          </div>
        </div>
        
        <!-- 文件列表 -->
        <div class="file-list">
          <div v-for="(item, index) in pageDetail.detailList" class="file-item" @click="changeNav(item)">
            <!-- 文件图标和类型 -->
            <div class="file-icon">
              <el-icon :size="24" color="#666666"><Document /></el-icon>
            </div>
            <!-- 文件信息 -->
            <div class="file-info">{{item.fileName}}</div>
            
            <!-- 文件操作 -->
            <div class="file-action">
              <el-button type="text" size="small" @click.stop="changeNav(item)">
                <el-icon :size="16" color="#3169F8"><Download /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <!-- 空状态优化 -->
      <div class="empty-state" v-else>
        <div class="empty-content">
          <el-icon :size="64" color="#e0e0e0"><DocumentRemove /></el-icon>
          <h3 class="empty-title">暂无文件</h3>
          <p class="empty-description">当前分类下还没有上传任何文件</p>
        </div>
      </div>      
     </template>
    <!-- 详情3 -->
     <template v-else-if="pageDetail?.columnType == '3'">
      <div class="ql-container ql-snow" v-if="pageDetail?.detailList && pageDetail.detailList.length > 0">
        <div class="rich-content ql-editor" v-html="pageDetail?.detailList[0]?.detailContent" v-if="pageDetail?.detailList[0]?.detailContent">
        </div>
        <el-empty class="mt-[100px]" description="暂无数据" v-else style="height: 200px;"></el-empty>
      </div>
      <el-empty class="mt-[100px]" description="暂无数据" v-else style="height: 200px;"></el-empty>
     </template>
      <!-- 二级详情1 -->
     <template v-else-if="route?.query?.detailId">
        <div class="ql-container ql-snow" v-if="pageDetail?.detailContent">
          <div class="rich-content ql-editor" v-html="pageDetail?.detailContent"></div>
        </div>
        <el-empty class="mt-[100px]" description="暂无数据" v-else style="height: 200px;"></el-empty>
     </template>
     <el-empty class="mt-[100px]" description="暂无数据" v-else style="height: 200px;"></el-empty>
  </div>
</div>
</template>

<script setup>
import "@vueup/vue-quill/dist/vue-quill.snow.css"
import { getMenuDetailById,getMenuDetailSecondById } from "@/api/index";
import {useRoute, useRouter} from 'vue-router'
let route = useRoute()
let router = useRouter()

var arr = $ref(['1','2','3']);
const changeNav = (value) => {
  window.open(value.fileUrl, "_blank");
  // const { href } = router.resolve({
  //   path: '/customize',
  //   query: {
  //     competitionId: route.query.competitionId,
  //     competitionSeriesId: route.query.id
  //   },
  // });
  // window.open(href, "_blank");
}

const goDetail = (value) => {
  const { href } = router.resolve({
    path: '/customize',
    query: {
      detailId: value.detailId
    },
  });
  window.open(href, "_blank");
}

// 获取赛事详情
let pageDetail = $ref({});
let pageDetailLoading = $ref(false);
const getDetail = () => {
  pageDetailLoading = true;
  let apiFunc = null
  let apiId = null
  if(route?.query.detailId){
    apiFunc = getMenuDetailSecondById
    apiId = route?.query.detailId
  }else{
    apiFunc = getMenuDetailById
    apiId = route?.query.id
  }
  apiFunc(apiId).then(async (res) => {
    if (res.code == 200) {
      pageDetail = res.data || {};
    }
    pageDetailLoading = false;
  }).catch(() => {
    pageDetailLoading = false;
  });
};

watch(() => route, () => {
  getDetail();
}, { immediate: true,deep: true })
</script>

<style scoped lang="scss">
.self-custom{
  margin-top: 30px;
  background: #ffffff;
  border-radius: 10px;
  padding: 20px;
  min-height: 500px;
  margin-bottom: 50px;
  width: 60%;
  min-width: 900PX
}

// 资讯列表样式
.content-section-header {
  display: flex;
  justify-content:flex-start;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f2f5;
}

.content-count {
  font-size: 14px;
  color: #909399;
  background: #ecf5ff;
  padding: 4px 12px;
  border-radius: 15px;
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 40px;
  margin-bottom: 40px;
}

.news-card {
  background: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  
  &:hover {
    box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
    transform: translateY(-4px);
    border-color: $main-color;
  }
  
  &:active {
    transform: translateY(-2px);
  }
}

.news-image-wrapper {
  width: 100%;
  height: 200px;
  overflow: hidden;
  position: relative;
  background: #f5f7fa;
}

.news-image {
  width: 100%;
  height: 100%;
  transition: transform 0.5s ease;
  
  .news-card:hover & {
    transform: scale(1.05);
  }
}

.image-placeholder,
.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
}

.news-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.news-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
  @include ellipsis(2);
  .news-card:hover & {
    color: $main-color;
  }
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
  transition: color 0.3s ease;
  
  .news-card:hover & {
    color: #606266;
  }
}

.meta-text {
  white-space: nowrap;
}

.news-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.news-hover-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(64, 158, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  
  .news-card:hover & {
    opacity: 1;
  }
  
  el-icon {
    color: $main-color;
    background: rgba(255, 255, 255, 0.9);
    padding: 12px;
    border-radius: 50%;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  }
}



// 文件列表样式
.file-section {
  background: #fafafa;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.file-section-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f2f5;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  margin-right: 10px;
}

.file-count {
  font-size: 14px;
  color: #909399;
  background: #ecf5ff;
  padding: 4px 12px;
  border-radius: 15px;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
  margin-bottom: 10px;

  
  &:hover {
    border-color: $main-color;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
    transform: translateY(-1px);
    
    &::before {
      background: $main-color;
    }
  }
  
  &:active {
    transform: translateY(0);
  }
}

.file-icon {
  margin-right: 16px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-info {
  flex: 1;
  min-width: 0;
  @include ellipsis(2);
}

.file-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.file-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  @include ellipsis(2);
  flex: 1;
  min-width: 100px;
}

.file-type-tag {
  padding: 3px 10px;
  background: linear-gradient(135deg, #ecf5ff 0%, #e6f0ff 100%);
  color: $main-color;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid #d9ecff;
  white-space: nowrap;
}

.file-description {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.file-meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
  transition: color 0.3s ease;
  
  .file-item:hover & {
    color: #606266;
  }
}

.file-action {
  margin-left: 20px;
  flex-shrink: 0;
  
  .el-button {
    padding: 6px 12px;
    transition: all 0.3s ease;
    border-radius: 6px;
    
    &:hover {
      background: #ecf5ff;
      color: $main-color;
    }
  }
}

.download-text {
  margin-left: 4px;
  font-size: 13px;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
  background: #ffffff;
  border: 2px dashed #ebeef5;
  border-radius: 12px;
  margin: 20px 0;
}

.empty-content {
  text-align: center;
  color: #909399;
}

.empty-title {
  font-size: 18px;
  font-weight: 500;
  color: #606266;
  margin: 16px 0 8px;
}

.empty-description {
  font-size: 14px;
  color: #c0c4cc;
  margin: 0;
}
.menuName{
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
}
</style>