<template>
  <div class="">
    <!-- 实名审核 -->
    <Reviewtask :auditType="'realName'" permiString="realName:task:audit" />
  </div>
</template>

<script setup>
import Reviewtask from "@/components/Reviewtask/index.vue";
</script>



<style scoped>
</style>