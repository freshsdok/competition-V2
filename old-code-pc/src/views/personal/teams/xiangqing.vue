<template>
  <div>
    <div class="xiangqing">
      <div class="xiangqing-title">操作要求</div>
      <div class="xiangqing-content">
        <div class="ql-container ql-snow">
          <div class="rich-content ql-editor" v-html="hintText1"></div>
        </div>
      </div>
    </div>
    <div class="mt-4">
      <div v-for="(file, index) in fileNames" :key="index" class="file-item">
        <span class="file-name" @click="downloadOssFile(file.url)">{{ file.name }}</span>
        <!-- <span class="file-url">{{ file.url }}</span> -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  hintText1: {
    type: String,
    default: "",
  },
  fujian: {
    type: Object,
    default: () => {},
  },
});

// 从附件URL中提取文件名
const fileNames = computed(() => {
  if (!props.fujian?.attachments) return [];

  const urls = props.fujian.attachments.split(",");
  const files = urls.map((url) => {
    const trimmedUrl = url.trim();
    const parts = trimmedUrl.split("/");
    const name = parts[parts.length - 1];
    return { name, url: trimmedUrl };
  });

  return files;
});
</script>

<style scoped lang="scss">
.xiangqing {
  min-width: 300px;
  max-width: 300px;
  height: 300px;
  background: #ffffff;
  border-radius: 0px 0px 0px 0px;
  border: 1px solid #e4e4e4;
  padding: 20px;
  overflow: auto;
  .xiangqing-title {
    font-family: Source Han Sans CN, Source Han Sans CN;
    font-weight: bold;
    font-size: 16px;
    color: #333333;
    line-height: 22px;
    text-align: left;
    font-style: normal;
    text-transform: none;
  }
  .xiangqing-content {
    height: 200px;
    margin-top: -20px;
  }
}
.file-item {
  margin-bottom: 1px;
  padding: 2px;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  cursor: pointer;
 
}
.file-name {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
  width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;
}
</style>