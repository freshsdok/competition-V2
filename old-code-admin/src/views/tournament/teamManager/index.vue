<template>
  <div class="">
    <!-- "dictLabel": "团队", -->
      <Reviewtask :auditType="'team'" permiString="team:task:audit"/>
  </div>
</template>

<script setup name="TeamManagerIndex">
import Reviewtask from '@/components/Reviewtask/index.vue'
</script>



<style scoped>
</style>