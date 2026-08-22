<template>
  <div class="course-detail">
    <el-row :gutter="20">
      <!-- 左侧封面图 -->
      <el-col :span="8">
        <div class="cover-image-container">
          <el-image
            v-if="form?.coverImage"
            :src="form.coverImage"
            fit="cover"
            class="cover-image"
            :preview-src-list="[form.coverImage]"
          />
          <div v-else class="cover-placeholder">
            <span>暂无封面</span>
          </div>
        </div>
      </el-col>
      
      <!-- 右侧课程信息 -->
      <el-col :span="16">
        <div class="course-info">
          <h2 class="course-title">{{ form?.name || '-' }}</h2>
          <p class="course-desc">{{ form?.details || '-' }}</p>
          
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">授课教师：</span>
              <span class="info-value">{{ form?.teacher || '-' }}</span>
            </div>
            
            <div class="info-item">
              <span class="info-label">学分：</span>
              <span class="info-value">{{ form?.studyScore || '-' }}</span>
            </div>
            
            <div class="info-item">
              <span class="info-label">难度：</span>
              <span class="info-value">
                <dict-tag 
                  v-if="form?.difficultyLevel" 
                  :options="course_difficulty" 
                  :value="form.difficultyLevel" 
                />
                <span v-else>-</span>
              </span>
            </div>
            
            <div class="info-item">
              <span class="info-label">学时：</span>
              <span class="info-value">{{ form?.creditHour || '-' }}</span>
            </div>
            
            <div class="info-item">
              <span class="info-label">价格：</span>
              <span class="info-value">
                {{ form?.price ? form.price : '未设置' }}
              </span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useDict } from "@/utils/dict";

const props = defineProps({
  form: {
    type: Object,
    default: () => ({}),
  },
});

const { course_difficulty } = useDict("course_difficulty");
</script>

<style scoped>
.course-detail {
  padding: 16px;
}

.cover-image-container {
  width: 100%;
  height: 300px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
  background-color: #f5f7fa;
}

.course-info {
  padding-left: 10px;
  height: 100%;
}

.course-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 10px 0;
  line-height: 1.5;
}

.course-desc {
  font-size: 14px;
  color: #606266;
  margin: 0 0 20px 0;
  line-height: 1.6;
  word-break: break-word;
}

.info-list {
  margin-top: 20px;
}

.info-item {
  margin-bottom: 16px;
  font-size: 14px;
  line-height: 1.8;
}

.info-label {
  color: #606266;
  font-weight: 500;
  display: inline-block;
  min-width: 80px;
}

.info-value {
  color: #303133;
}
</style>

