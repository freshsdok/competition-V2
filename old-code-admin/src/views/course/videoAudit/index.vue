<template>
  <div class="">
    <!-- 视频审核 -->
    <Reviewtask :auditType="'chapterVideo'" :permiString="'chapterVideo:task:audit'" />
  </div>
</template>

<script setup>
import Reviewtask from "@/components/Reviewtask/index.vue";
</script>

<style scoped>
</style>
