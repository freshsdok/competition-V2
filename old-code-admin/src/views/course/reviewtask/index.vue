<template>
  <div class="">
    <!-- 课程 -->
    <Reviewtask :auditType="'course'" permiString="course:task:audit"/>
  </div>
</template>

<script setup>
import Reviewtask from "@/components/Reviewtask/index.vue";
</script>



<style scoped>
</style>