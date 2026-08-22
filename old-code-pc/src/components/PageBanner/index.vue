<template>
  <div class="banner__wrapper">
    <img :src="banner" alt="" srcset="">
    <div class="banner__title" v-if="title">
      {{ title }}
    </div>
  </div>
</template>

<script setup>
  const props = defineProps({
    banner: {
      type: String,
      default: ''
    },
    title: {
      type: String,
      default: ''
    }
  })
</script>

<style lang="scss" scoped>
  @keyframes slideUpBackground {
    0% {
      transform: scale(1);
    }

    100% {
      transform: scale(1.1);
    }
  }

  .banner__wrapper {
    position: relative;
    img {
      width: 100%;
      object-fit: cover;
      animation: slideUpBackground 5s ease-out;
    }
    .banner__title {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 45px;
      font-weight: bold;
      color: #fff;
      letter-spacing: 3px;
    }
  }
</style>