<template>
  <div class="">
    <!-- 公告审核 -->
    <Reviewtask :auditType="'notice'" permiString="notice:task:audit"/>
  </div>
</template>

<script setup>
import Reviewtask from "@/components/Reviewtask/index.vue";
</script>



<style scoped>
</style>