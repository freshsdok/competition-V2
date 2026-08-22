<template>
  <div class="global-page">
    <el-empty description="正在开发中......" ></el-empty>
  </div>
</template>
<script>
import { getQuestions } from "@/api/content";
</script>
<style scoped lang="scss">
.global-page{
  min-height: 45vh;
}
</style>