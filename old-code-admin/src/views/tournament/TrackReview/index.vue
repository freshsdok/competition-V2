<template>
  <div class="">
    <!-- "dictLabel": "赛道", -->
      <Reviewtask :auditType="'raceTrack'" permiString="raceTrack:task:audit"/>
  </div>
</template>

<script setup name="TrackReviewIndex">
import Reviewtask from '@/components/Reviewtask/index.vue'
</script>



<style scoped>
</style>