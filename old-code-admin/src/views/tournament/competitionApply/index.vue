<template>
  <div class="">
    <!-- dictLabel": "报名", -->
      <Reviewtask :auditType="'apply'" permiString="apply:task:audit"/>
  </div>
</template>

<script setup name="competitionApply">
import Reviewtask from '@/components/Reviewtask/index.vue'
</script>



<style scoped>
</style>