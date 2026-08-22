
<template>
  <div class="" style="padding: 16px;">
    <el-form ref="newsInfoRef" :model="form" disabled label-width="100px">
      <el-row>
        <el-col :span="12">
          <el-form-item label="资讯标题">
            <el-input
              v-model="form.newsTitle"
              placeholder="请输入资讯标题"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="资讯副标题">
            <el-input
              v-model="form.newsViceTitle"
              placeholder="请输入资讯副标题"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="发布人">
            <el-input
              v-model="form.newsAuthor"
              placeholder="请输入发布人"
              maxlength="100"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="资讯标签">
            <el-input
              v-model="form.newsTag"
              placeholder="请输入资讯标签，多个用逗号分隔"
              maxlength="200"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="资讯来源">
            <el-input
              v-model="form.newsSource"
              placeholder="请输入资讯来源"
              maxlength="100"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="资讯类型">
            <el-select
              v-model="form.newsType"
              placeholder="请选择资讯类型"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="dict in news_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="资讯封面">
        <el-image
          style="width: 100px; height: 100px"
          :src="form.newsImage"
          fit="fill"
        />
      </el-form-item>
      <el-form-item label="资讯摘要">
        <el-input
          v-model="form.newsAbstract"
          type="textarea"
          :rows="3"
          placeholder="请输入资讯摘要"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="资讯内容">
        <editor v-model="form.newsCont" :min-height="300" />
      </el-form-item>
    </el-form>
  </div>
</template>
<script setup>
import { useDict } from "@/utils/dict"
const props = defineProps({
  form: {
    type: Object,
    default: () => {},
  },
});
const {  news_type } = useDict( 'news_type')
const { form } = toRefs(props);
</script>

<style scoped>
</style>