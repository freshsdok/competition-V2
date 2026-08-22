<template>
  <div>
    <el-dialog
      v-model="counterStore.changePhoneEmail"
      width="500"
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="my-header">修改{{ props.phoneemail }}</div>
      </template>
      <phone v-if="props.phoneemail === '手机号'"/>
      <email v-else/>
    </el-dialog>
  </div>
</template>
<script setup>
import { useCounterStore } from "@/stores/index";
import phone from "./phone.vue";
import email from "./email.vue";
const props = defineProps({
  phoneemail: {
    type: String,
    default: '手机号'
  }
})
console.log(props.phoneemail)
const counterStore = useCounterStore();
</script>



<style scoped lang="scss">
:deep(.el-dialog) {
  padding: 60px 50px 20px 50px;
  z-index: 999999 !important;
}
:deep(.el-dialog__header.show-close) {
  padding: 0 0 30px 0;
}
.my-header {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
}
.ipt {
  height: 50px;
}
.denglu {
  width: 100%;
  height: 40px;
  margin: 10px 0;
}
.icon {
  width: 18px;
  height: 18px;
  margin-right: 3px;
}
.xiangxuanxiang {
  display: flex;
  cursor: pointer;
}
.zhuche {
  display: flex;
  justify-content: space-evenly;
  margin-top: 10px;
  margin-bottom: 10px;
}
.weixintubiao {
  width: 40px;
  margin: 0 auto;
  margin-bottom: 40px;
  img {
    width: 100%;
  }
}

:deep(.el-divider__text.is-center) {
  color: #aeaeae;
}
</style>