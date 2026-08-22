<template>
  <div class="">
    <!-- "dictLabel": "赛事", -->
      <Reviewtask :auditType="'race'" permiString="race:task:audit"/>
  </div>
</template>

<script setup name="ReviewtaskIndex">
import Reviewtask from '@/components/Reviewtask/index.vue'
</script>



<style scoped>
</style>