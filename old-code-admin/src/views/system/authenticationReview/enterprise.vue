<template>
  <div class="">
    <!-- 企业身份审核 -->
    <Reviewtask :auditType="'enterprise'" permiString="enterprise:task:audit" />
  </div>
</template>

<script setup>
import Reviewtask from "@/components/Reviewtask/index.vue";
</script>



<style scoped>
</style>