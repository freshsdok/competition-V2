<template>
  <div class="">
    <!-- 学生身份审核 -->
    <Reviewtask :auditType="'student'"  permiString="student:task:audit"/>
  </div>
</template>

<script setup>
import Reviewtask from "@/components/Reviewtask/index.vue";
</script>



<style scoped>
</style>