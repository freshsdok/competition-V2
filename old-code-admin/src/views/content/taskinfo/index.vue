<template>
  <div class="">
    <!-- 咨询审核 -->
    <Reviewtask :auditType="'info'" permiString="info:task:audit" />
  </div>
</template>

<script setup>
import Reviewtask from "@/components/Reviewtask/index.vue";
</script>
<style scoped>
</style>