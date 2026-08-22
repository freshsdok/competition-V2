<template>
  <div class="">
    <!-- 教师身份审核 -->
    <Reviewtask :auditType="'teacher'"  permiString="teacher:task:audit"/>
  </div>
</template>

<script setup>
import Reviewtask from "@/components/Reviewtask/index.vue";
</script>



<style scoped>
</style>