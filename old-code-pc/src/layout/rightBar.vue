<template>
<div class="rightBar">
  <!-- <div class="rightBar-item rightBar-item2" @click="goFeedback()">
    <div class="rightBar-item-img rightBar-item-img2"></div>
    <div class="rightBar-item-text">反馈</div>
  </div>
  <div class="rightBar-item rightBar-item3" @click="goQa()">
    <div class="rightBar-item-img rightBar-item-img3"></div>
    <div class="rightBar-item-text">帮助</div>
  </div> -->
  <div class="rightBar-item rightBar-item4" @click="toTop()">
    <div class="rightBar-item-img rightBar-item-img4"></div>
    <div class="rightBar-item-text">返回顶部</div>
  </div>
</div>
</template>
<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

// 跳转反馈页
const goFeedback = () => {
  router.push({ path: '/feedback' })
}

// 跳转帮助中心页
const goQa = () => {
  router.push({ path: '/qa' })
}



// 返回顶部
const toTop = () => {
 try {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  } catch (e) {
    // 如果不支持options参数，回退到传统方式
    window.scrollTo(0, 0);
  }
}
</script>
<style scoped lang="scss">
.rightBar {
  background: #FFFFFF;
  box-shadow: 0px 1px 5px 1px rgba(0,0,0,0.16);
  border-radius: 8px 0px 0px 8px;
  position: fixed;
  top: 80%;
  right: 0;
  z-index: 999;
  .rightBar-item{
    width: 80px;
    height: 78px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    color: #333333;
    cursor: pointer;
    +.rightBar-item{ 
      border-top: 1px solid #E4E4E4;
    }
    &:last-child{
      border-radius: 7px 0px 0px 7px;
    }
    // &:first-child{
    //   border-radius: 8px 0px 0px 0px;
    // }
    .rightBar-item-img{
      width: 30px;
      height: 30px;
      background-size: cover;
      background-repeat: no-repeat;
      background-position: center center;
      margin-bottom: 5px;
    }
    .rightBar-item-img2{
      background: url('@/assets/images/rightBar/r2.png');
      transition: background-image 0.2s ease;
    }
    .rightBar-item-img4{
      background: url('@/assets/images/rightBar/r4.png');
      transition: background-image 0.2s ease;
    }
    .rightBar-item-img3{
      background: url('@/assets/images/rightBar/r3.png');
      transition: background-image 0.2s ease;
    }
  }
  .rightBar-item2{ 
    &:hover{
      background: #3169f8;
      color: #FFFFFF;
      .rightBar-item-img2{
        background: url('@/assets/images/rightBar/r2-a.png');
      }
    }
  }
  .rightBar-item3{ 
    &:hover{
      background: #3169f8;
      color: #FFFFFF;
      .rightBar-item-img3{
        background: url('@/assets/images/rightBar/r3-a.png');
      }
    }
  }
  .rightBar-item4{ 
    &:hover{
      background: #3169f8;
      color: #FFFFFF;
      .rightBar-item-img4{
        background: url('@/assets/images/rightBar/r4-a.png');
      }
    }
  }
}
</style>
