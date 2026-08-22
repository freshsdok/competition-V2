<template>
  <div class="">
    <!-- 页面审核 -->
    <Reviewtask :auditType="'page'" permiString="page:task:audit" />
  </div>
</template>

<script setup>
import Reviewtask from "@/components/Reviewtask/index.vue";
</script>



<style scoped>
</style>