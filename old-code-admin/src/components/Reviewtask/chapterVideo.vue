<template>
  <div class="chapter-video-audit">
    <!-- 待审核视频列表 -->
    <template v-if="videoList.length > 0">
      <div v-for="(video, index) in videoList" :key="video.pageId || index" class="video-item">
        <!-- 视频信息卡片 -->
        <el-card class="video-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="video-title">{{ video.videoName || '-' }}</span>
              <span class="video-index">视频 {{ index + 1 }}/{{ videoList.length }}</span>
            </div>
          </template>
          
          <!-- 视频基本信息 -->
          <el-row :gutter="20" class="video-info">
            <el-col :span="6">
              <div class="info-item">
                <span class="label">视频时长：</span>
                <span class="value">{{ video.videoDuration || '-' }} 秒</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="info-item">
                <span class="label">是否免费：</span>
                <dict-tag v-if="video.isFree" :options="sys_yes_no" :value="video.isFree" />
                <span v-else>-</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">价格：</span>
                <span class="value">
                  <span v-if="video.isFree === 'Y' || video.isFree === 1">免费</span>
                  <span v-else-if="video.price || courseInfo?.price">{{ video.price || courseInfo?.price }}</span>
                  <span v-else>-</span>
                </span>
                <el-button 
                  type="primary" 
                  size="default"
                  @click="handlePreviewVideo(video)"
                  style="margin-left: 10px;"
                >
                  <el-icon style="margin-right: 6px;"><VideoPlay /></el-icon>预览视频
                </el-button>
              </div>
            </el-col>
          </el-row>
          
          <!-- 审核意见 -->
          <el-form-item label="审核意见" class="audit-opinion">
            <el-input 
              v-model="video.auditOpinion" 
              type="textarea" 
              :rows="3"
              maxlength="200"
              show-word-limit
              placeholder="请输入审核意见"
            />
          </el-form-item>
          
          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button type="success" @click="handleApprove(video)">通过</el-button>
            <el-button type="danger" @click="handleReject(video)">驳回</el-button>
          </div>
        </el-card>
      </div>
    </template>
    <template v-else>
      <el-empty description="暂无待审核视频" />
    </template>
  </div>

  <!-- 视频预览对话框 -->
  <el-dialog 
    v-model="previewVideoDialogOpen" 
    title="视频预览" 
    width="800px"
    append-to-body
  >
    <div class="video-preview-container">
      <video 
        :src="previewVideoUrl" 
        controls 
        style="width: 100%; max-height: 500px; background-color: #000;"
      />
    </div>
  </el-dialog>
</template>

<script setup>
import { useDict } from "@/utils/dict";
import { getCourseInfo } from "@/api/course/courseInfo";
import { ref, watch } from "vue";
import { VideoPlay } from "@element-plus/icons-vue";

const props = defineProps({
  form: {
    type: Object,
    default: () => ({}),
  },
  isAudit: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['video-approve', 'video-reject']);

const { sys_yes_no } = useDict("sys_yes_no");

const chapterInfo = ref(null);
const courseInfo = ref(null);
const videoList = ref([]);
const previewVideoUrl = ref('');
const previewVideoDialogOpen = ref(false);

// 预览视频
const handlePreviewVideo = (video) => {
  if (!video.videoFile) {
    ElMessage.warning('该视频暂无文件');
    return;
  }
  previewVideoUrl.value = video.videoFile;
  previewVideoDialogOpen.value = true;
};

// 根据 courseId 查询课程信息
const fetchCourseInfo = async (courseId) => {
  if (!courseId) return;
  try {
    const res = await getCourseInfo(courseId);
    if (res.code === 200 && res.data) {
      courseInfo.value = {
        courseId: res.data.courseId,
        name: res.data.name,
        courseCode: res.data.courseCode,
        price: res.data.price,
      };
    }
  } catch (error) {
    console.error('Failed to fetch course info:', error);
  }
};

// 处理通过
const handleApprove = (video) => {
  if (!video.auditOpinion || video.auditOpinion.trim() === '') {
    ElMessage.warning('请填写审核意见');
    return;
  }
  emit('video-approve', {
    pageInfo: {
      pageId: video.pageId,
      applyReason: video.auditOpinion,
      checkStatus: 4, // 通过
    },
  });
};

// 处理驳回
const handleReject = (video) => {
  if (!video.auditOpinion || video.auditOpinion.trim() === '') {
    ElMessage.warning('请填写审核意见');
    return;
  }
  emit('video-reject', {
    pageInfo: {
      pageId: video.pageId,
      applyReason: video.auditOpinion,
      checkStatus: 5, // 驳回
    },
  });
};

watch(
  () => props.form,
  (newVal) => {
    console.log('Video Audit - props.form:', JSON.stringify(newVal, null, 2));
    
    if (!newVal) {
      chapterInfo.value = null;
      courseInfo.value = null;
      videoList.value = [];
      return;
    }
    
    // 优先级：courseChapterVideos > businessDetail.courseChapterVideos > businessDetail > form本身
    let videoData = null;
    let detailData = null;
    
    // 1. 检查顶层 courseChapterVideos（新的后端结构）
    if (newVal.courseChapterVideos && Array.isArray(newVal.courseChapterVideos) && newVal.courseChapterVideos.length > 0) {
      console.log('Found courseChapterVideos at top level');
      videoData = newVal.courseChapterVideos;
      detailData = newVal.businessDetail || newVal;
    }
    // 2. 检查 businessDetail 中的 courseChapterVideos
    else if (newVal.businessDetail && newVal.businessDetail.courseChapterVideos && Array.isArray(newVal.businessDetail.courseChapterVideos) && newVal.businessDetail.courseChapterVideos.length > 0) {
      console.log('Found courseChapterVideos in businessDetail');
      videoData = newVal.businessDetail.courseChapterVideos;
      detailData = newVal.businessDetail;
    }
    // 3. 检查 businessDetail 中的其他视频列表字段
    else if (newVal.businessDetail) {
      detailData = newVal.businessDetail;
      if (detailData.chapterVideoList && Array.isArray(detailData.chapterVideoList) && detailData.chapterVideoList.length > 0) {
        console.log('Found chapterVideoList in businessDetail');
        videoData = detailData.chapterVideoList;
      } else if (detailData.videoList && Array.isArray(detailData.videoList) && detailData.videoList.length > 0) {
        console.log('Found videoList in businessDetail');
        videoData = detailData.videoList;
      }
    }
    // 4. 检查 form 本身的其他视频列表字段
    else if (newVal.chapterVideoList && Array.isArray(newVal.chapterVideoList) && newVal.chapterVideoList.length > 0) {
      console.log('Found chapterVideoList in form');
      videoData = newVal.chapterVideoList;
      detailData = newVal;
    } else if (newVal.videoList && Array.isArray(newVal.videoList) && newVal.videoList.length > 0) {
      console.log('Found videoList in form');
      videoData = newVal.videoList;
      detailData = newVal;
    }
    
    if (detailData) {
      console.log('Video Audit - detailData:', JSON.stringify(detailData, null, 2));
      
      // 处理章节信息
      if (detailData.chapterId || detailData.chapterName) {
        chapterInfo.value = {
          chapterId: detailData.chapterId,
          chapterName: detailData.chapterName,
          chapterNum: detailData.chapterNum,
          courseId: detailData.courseId,
        };
      } else if (detailData.chapterInfo) {
        chapterInfo.value = detailData.chapterInfo;
      }
      
      // 处理课程信息
      if (detailData.courseInfo) {
        courseInfo.value = detailData.courseInfo;
      } else if (detailData.courseId) {
        if (detailData.courseName || detailData.name) {
          courseInfo.value = {
            courseId: detailData.courseId,
            name: detailData.courseName || detailData.name,
            courseCode: detailData.courseCode,
            price: detailData.price,
          };
        } else {
          fetchCourseInfo(detailData.courseId);
        }
      }
    }
    
    // 处理视频列表
    if (videoData && Array.isArray(videoData) && videoData.length > 0) {
      console.log('Processing video data:', videoData);
      // 过滤掉已审核的视频（checkStatus 为 4-已通过 或 5-已拒绝）
      const filteredVideoData = videoData.filter((item) => {
        const checkStatus = item.checkStatus;
        // 只显示待审核的视频（checkStatus 为 2-待审核、3-审核中，或者没有 checkStatus）
        return !checkStatus || checkStatus === '2' || checkStatus === '3' || checkStatus === 2 || checkStatus === 3;
      });
      
      videoList.value = filteredVideoData.map((item) => ({
        pageId: item.videoId || item.pageId,
        videoName: item.videoName,
        videoDuration: item.videoDuration || item.duration,
        isFree: item.isFree,
        price: item.expenses || item.price || detailData?.price || courseInfo.value?.price || null,
        videoFile: item.videoFile || item.videoUrl || null,
        auditOpinion: '',
        checkStatus: item.checkStatus,
      }));
    } else {
      console.log('No video data found');
      videoList.value = [];
    }
    
    console.log('Video Audit - parsed chapterInfo:', chapterInfo.value);
    console.log('Video Audit - parsed courseInfo:', courseInfo.value);
    console.log('Video Audit - parsed videoList:', videoList.value);
  },
  { immediate: true, deep: true }
);

// 获取审核结果
const getAuditResult = (checkStatus) => {
  // 收集所有已填写审核意见的视频
  const pageInfo = videoList.value
    .filter(video => video.auditOpinion && video.auditOpinion.trim() !== '')
    .map(video => ({
      pageId: video.pageId,
      applyReason: video.auditOpinion,
      checkStatus: checkStatus,
    }));
  
  return {
    chapterId: chapterInfo.value?.chapterId,
    pageInfo: pageInfo,
  };
};

// 暴露方法供父组件调用
defineExpose({
  getVideoList: () => videoList.value,
  getAuditResult: getAuditResult,
});
</script>

<style scoped>
.chapter-video-audit {
  padding: 0;
}

.video-item {
  margin-bottom: 20px;
}

.video-card {
  border-radius: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.video-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.video-index {
  font-size: 12px;
  color: #909399;
}

.video-info {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item .label {
  font-weight: 500;
  color: #606266;
  margin-right: 8px;
  min-width: 80px;
}

.info-item .value {
  color: #303133;
}

.audit-opinion {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.action-buttons :deep(.el-button) {
  min-width: 80px;
}

.video-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 4px;
  padding: 10px;
}

.video-preview-container video {
  border-radius: 4px;
}
</style>
