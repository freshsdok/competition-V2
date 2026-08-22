<template>
  <div class="">
    <Reviewtask :auditType="'team'" permiString="team:task:audit"/>
  </div>
</template>

<script setup name="TeamIndex">
import Reviewtask from "@/components/Reviewtask/index.vue";
</script>



<style scoped>
</style>