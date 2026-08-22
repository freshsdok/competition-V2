
<template>
  <div>
    <div v-show="shurushow == 'shoujihao'">
      <el-form
        :model="form"
        label-width="auto"
        :rules="rules"
        ref="userNameformRef"
      >
        <el-form-item prop="userName" style="margin-top: 40px">
          <el-input
            v-model="form.userName"
            placeholder="请输入邮箱"
            class="ipt"
          />
        </el-form-item>

        <el-button
          type="primary"
          @click="huoquyanzhengma"
          class="denglu"
          style="margin-top: 20px"
          >获取验证码</el-button
        >
      </el-form>
      <div style="height: 20px"></div>
    </div>
    <div v-show="shurushow == 'yanzhengma'">
      <el-form :model="form" label-width="auto" style="margin-top: 30px">
        <el-form-item style="display: flex">
          <div style="display: flex; width: 100%">
            <el-input
              v-model="form.msgCode"
              placeholder="请输入验证码"
              style="width: 100%"
              class="ipt"
            />
            <el-button
              style="width: 130px; flex-shrink: 0; color: #fff"
              class="ipt"
              @click="huoquyanzhengma"
              type="primary"
              >{{
                typeof yanzhengma == "number" ? yanzhengma + "s" : yanzhengma
              }}</el-button
            >
          </div>
        </el-form-item>

        <el-button
          type="primary"
          @click="onphoneSubmit"
          class="denglu"
          style="margin-top: 20px"
          >点击修改</el-button
        >
      </el-form>

      <div style="height: 20px"></div>
    </div>
    <el-dialog
      v-model="zhenrenjiaoyan"
      title="请完成安全验证"
      width="400"
      top="300px"
    >
      <huakuai @close="huakuaiopen" />
    </el-dialog>
  </div>
</template>
<script setup>
import { ElMessage } from "element-plus";
import huakuai from "./huakuai.vue";
import { getPhoneCode, checkPersonalAccountAvailable } from "@/api/index";
import { useCounterStore } from "@/stores/index";
import { updateUserInfoPhoneOrEmail } from "@/api/accountmanagement/index";
const counterStore = useCounterStore();
const shurushow = ref("shoujihao");
const zhenrenjiaoyan = ref(false);
const huakuaiopen = () => {
  zhenrenjiaoyan.value = false;
  huoquyanzhengma();
};
const form = ref({
  userName: "",
});
const yanzhengma = ref("获取验证码");
const userNameformRef = ref(null);
const huoquyanzhengma = () => {
  userNameformRef.value?.validate((valid) => {
    if (valid) {
      if (counterStore.shimingzhuangtai == false) {
        zhenrenjiaoyan.value = true;
        return;
      }
      const userName = {
        userName: form.value.userName,
      };
      checkPersonalAccountAvailable(userName).then((res) => {
        if (!res.data) {
          ElMessage.error("该邮箱已存在");
          return;
        } else {
          if (
            yanzhengma.value === "获取验证码" ||
            yanzhengma.value === "重新获取"
          ) {
            if (form.value.userName) {
              const params = {
                userName: form.value.userName,
              };
              getPhoneCode(params).then((res) => {
                shurushow.value = "yanzhengma";
                yanzhengma.value = 60;
                const sss = setInterval(() => {
                  if (yanzhengma.value > 0) {
                    yanzhengma.value--;
                  } else {
                    clearInterval(sss);
                    yanzhengma.value = "重新获取";
                  }
                }, 1000);
              });
            }
          } else {
            return;
          }
        }
      });
    }
  });
};
// 自定义校验规则：手机号或邮箱
const validateUserName = (rule, value, callback) => {
  // const phoneReg = /^1[3-9]\d{9}$/;
  const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

  if (!value) {
    callback(new Error("请输入邮箱"));
  } else if (!emailReg.test(value)) {
    // } else if (!phoneReg.test(value) ) {
    callback(new Error("请输入正确的邮箱"));
  } else {
    callback();
  }
};
// 验证规则
const rules = reactive({
  userName: [{ required: true, validator: validateUserName, trigger: "blur" }],
});
const onphoneSubmit = () => {
  if (form.value.msgCode) {
    const params = {
      email: form.value.userName,
      msgCode: form.value.msgCode,
    };
    updateUserInfoPhoneOrEmail(params).then((res) => {
      if (res.code == 200) {
        ElMessage.success("修改成功");
        location.reload();
        counterStore.changePhoneEmaildecrement();
      }
    });
  } else {
    ElMessage.error("请输入验证码");
  }
};
</script>

<style scoped lang="scss">
:deep(.el-dialog) {
  padding: 60px 50px 50px 50px;
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
  height: 48px;
  margin: 10px 0;
  font-size: 20px;
}
.icon {
  width: 18px;
  height: 18px;
  margin-right: 3px;
}
.xiangxuanxiang {
  display: flex;
  cursor: pointer;
  align-items: center;
}
.zhuche {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
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
.yanzhengmatishi {
  font-size: 14px;
  color: #191919;
  text-align: center;
  margin-top: 10px;
}
.success-container {
  justify-content: center;
}

.success-content {
  width: 100%;

  border-radius: 16px;

  text-align: center;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.success-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto;
}

@keyframes bounceIn {
  0%,
  20%,
  53%,
  80%,
  100% {
    transform: translate3d(0, 0, 0);
  }
  40%,
  43% {
    transform: translate3d(0, -10px, 0);
  }
  70% {
    transform: translate3d(0, -5px, 0);
  }
  90% {
    transform: translate3d(0, -2px, 0);
  }
}

.success-title {
  font-size: 26px;
  font-weight: 700;
  color: #2c3e50;
  margin: 12px 0;
  text-align: center;
}

.success-desc {
  font-size: 16px;
  color: #7f8c8d;
  margin-bottom: 10px;
  line-height: 1.5;
}

.countdown-text {
  font-size: 16px;
  color: #5c6c7c;
  margin-bottom: 32px;
}

.countdown-number {
  font-size: 24px;
  font-weight: 700;
  color: #3498db;
  margin: 0 4px;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}
</style>
