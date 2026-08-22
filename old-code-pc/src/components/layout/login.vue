<template>
  <div>
    <el-dialog
      v-model="counterStore.loginopen"
      width="500"
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="my-header">{{ counterStore.title }}</div>
        <div v-if="counterStore.title == '账号密码登录'" class="mt-[15px] text-center text-[#606266]">
          已经报名但未完成注册的同学请点击注册，完成注册程序后可登录使用
        </div>
      </template>
      <zmlogin v-if="counterStore.title == '账号密码登录'" />
      <phonelogin v-if="counterStore.title == '手机号登录'" />
      <phoneregister v-if="counterStore.title == '手机号注册'" />
      <emailregister v-if="counterStore.title == '邮箱注册'" />
      <password v-if="counterStore.title == '重置密码'" />
    </el-dialog>
  </div>
</template>
<script setup>
import zmlogin from "./components/loginipt.vue";
import phonelogin from "./components/phonelogin.vue";
import phoneregister from "./components/phoneregister.vue";
import emailregister from "./components/emailregister.vue";
import password from "./components/password.vue";
import { useCounterStore } from "@/stores/index";

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