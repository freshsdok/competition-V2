<template>
  <div class="">
    <!-- 学校身份审核 -->
    <Reviewtask :auditType="'school'"  permiString="school:task:audit"/>
  </div>
</template>

<script setup>
import Reviewtask from "@/components/Reviewtask/index.vue";
</script>



<style scoped>
</style>