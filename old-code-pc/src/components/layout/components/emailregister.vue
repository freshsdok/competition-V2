
<template>
  <div>
    <div v-if="shurushow == 'shoujihao'">
      <el-form :model="form" label-width="auto" :rules="rules" ref="emailRef">
        <el-form-item prop="email" style="margin-top: 40px">
          <el-input v-model.trim="form.email" placeholder="请输入邮箱" class="ipt" />
        </el-form-item>
        <el-form-item>
          <el-checkbox-group v-model="form.yhxy" text-color="#3169F8">
            <el-checkbox value="trueword" name="type">
              我已阅读并同意
              <span
                class="lansebut"
                v-for="(item, index) in pcAgreement"
                :key="index"
                @click.stop="
                  (e) => {
                    openAgreement(e, item);
                  }
                "
              >
                《{{ item.label }}》
                <span
                  v-if="index < pcAgreement.length - 1"
                  class="text-[#606266]"
                  >和</span
                >
              </span>
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-button
          type="primary"
          @click="huoquyanzhengma"
          class="denglu"
          style="margin-top: 20px"
          :disabled="form.email == ''"
          >获取验证码</el-button
        >
        <div class="zhuche">
          <div class="xiangxuanxiang" @click="counterStore.phoneregister">
            <img src="@/assets/icon/phone.png" alt="" class="icon" />
            手机号注册
          </div>

          <div class="xiangxuanxiang" @click="counterStore.logintitle">
            <img src="@/assets/icon/out.png" alt="" class="icon" />返回登录
          </div>
        </div>
      </el-form>

      <div style="height: 20px"></div>
    </div>
    <div v-if="shurushow == 'yanzhengma'">
      <div class="yanzhengmatishi">
        验证码已发送至
        {{ form.email.substring(0, 3) + "****" + form.email.substring(7) }},
        请输入验证码。
      </div>

      <el-form
        :model="form"
        label-width="auto"
        :rules="rules"
        style="margin-top: 30px"
      >
        <el-form-item style="display: flex" prop="msgCode">
          <div style="display: flex; width: 100%">
            <el-input
              v-model.trim="form.msgCode"
              placeholder="请输入验证码"
              style="width: 100%"
              maxlength="6"
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
          :disabled="form.msgCode == ''"
          >验证邮箱</el-button
        >
        <div class="zhuche">
          <div class="xiangxuanxiang" @click="counterStore.phoneregister">
            <img src="@/assets/icon/phone.png" alt="" class="icon" />
            手机号注册
          </div>

          <div class="xiangxuanxiang" @click="counterStore.logintitle">
            <img src="@/assets/icon/out.png" alt="" class="icon" />返回登录
          </div>
        </div>
      </el-form>
      <div style="height: 20px"></div>
    </div>
    <div v-if="shurushow == 'possword'">
      <div>
        您的邮箱:
        {{
          form.email.substring(0, 3) + "****" + form.email.substring(7)
        }},验证码验证成功，请设置您的密码，密码复杂度要求：“长度6-16位、包含字母、数字、特殊符号”
      </div>
      <el-form
        :model="form"
        label-width="auto"
        :rules="rules"
        ref="formRef"
        style="margin-top: 30px"
      >
        <el-form-item prop="password">
          <el-input
            v-model.trim="form.password"
            show-password
            placeholder="请输入密码"
            class="ipt"
          />
        </el-form-item>
        <el-form-item prop="passwordout">
          <el-input
            v-model.trim="form.passwordout"
            show-password
            placeholder="请确认密码"
            class="ipt"
          />
        </el-form-item>
        <el-button
          type="primary"
          @click="onSubmit"
          class="denglu"
          style="margin-top: 20px"
          >注册</el-button
        >
      </el-form>
      <div class="zhuche">
        <div class="xiangxuanxiang" @click="counterStore.phoneregister">
          <img src="@/assets/icon/phone.png" alt="" class="icon" />
          手机号注册
        </div>

        <div class="xiangxuanxiang" @click="counterStore.logintitle">
          <img src="@/assets/icon/out.png" alt="" class="icon" />返回登录
        </div>
      </div>
      <div style="height: 20px"></div>
    </div>
     <div v-if="shurushow == 'chongzhichenggong'" class="success-container">
      <div class="success-content">
        <img
          src="@/assets/images/baomingchenggong.png"
          alt="成功图标"
          class="success-icon"
        />
        <div class="success-title">注册成功</div>
        <div class="countdown-text">
          页面将在<span class="countdown-number">{{ daojishijian }}</span
          >s后自动返回登录
        </div>
        <el-button type="primary" @click="fanhuidenglu" class="denglu"
          >返回登录</el-button
        >
      </div>
    </div>
      <el-dialog
      v-model="yonghuxieyiopen"
      width="500"
      top="300px"
      :show-close="false"
      :close-on-click-modal="false"
    >
      <template #header>
        <div style="display: flex; justify-content: center">
          <img src="@/assets/icon/yinsi.png" style="width: 40px" alt="" />
        </div>
        <div class="my-header">隐私协议及用户协议</div>
      </template>
      <div>请阅读并同意隐私协议和用户协议</div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="yonghuxieyiopen = false" style="width: 100px"
            >不同意</el-button
          >
          <el-button type="primary" @click="tongyi" style="width: 100px">
            同意并继续
          </el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="zhenrenjiaoyan" title="请完成安全验证" width="400"   top="300px">
      <huakuai @close="huakuaiopen" />
    </el-dialog>
  </div>
</template>
<script setup>
import {
  authregister,
  sendEmailCode,
  checkUserAccount,
  checkRegisterInfo,
} from "@/api/index";
import { ElMessage } from "element-plus";
import { encrypt, decrypt } from "@/utils/jsencrypt.js";
import modal from "@/plugins/modal";
import huakuai from "./huakuai.vue";
import { useCounterStore } from "@/stores/index";
import { useRouter } from "vue-router";
const router = useRouter();
const counterStore = useCounterStore();
let pcAgreement = $computed(() => {
  return counterStore?.menuList?.pcLogin || [];
});
const openAgreement = (event, item) => {
  event.stopPropagation();
  event.preventDefault();
  const { href } = router.resolve({
    path: "/customize",
    query: { id: item.id },
  });
  window.open(href, "_blank");
};
const zhenrenjiaoyan = ref(false);
const huakuaiopen = () => {
  zhenrenjiaoyan.value = false;
  huoquyanzhengma();
};
// 验证规则
const validatePassword = (rule, value, callback) => {
  if (!value) {
    return callback(new Error("请输入密码"));
  }

  if (value.length < 6 || value.length > 16) {
    return callback(new Error("密码长度必须为6-16位"));
  }

  // 必须包含至少一个字母（不区分大小写）
  if (!/[a-zA-Z]/.test(value)) {
    return callback(new Error("密码必须包含至少一个字母"));
  }

  // 必须包含至少一个数字
  if (!/\d/.test(value)) {
    return callback(new Error("密码必须包含至少一个数字"));
  }

  // 必须包含至少一个特殊符号
  if (
    !/[`~!@#$%^&*()_\-+=<>?:"{}|,./;'\\[\]·!@#￥%……&*（）——+={}|《》？：“”【】、；‘’，。、]/.test(
      value
    )
  ) {
    return callback(new Error("密码必须包含至少一个特殊符号"));
  }

  callback();
};
const shurushow = ref("shoujihao");
// 倒计时返回
const daojishijian = ref(5);
const timer = ref(null);
const fanhuidenglu = () => {
  clearInterval(timer.value);
  counterStore.logintitle();
};
// 验证规则
const rules = reactive({
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    {
      type: "email",
      message: "请输入正确的邮箱地址",
      trigger: "blur",
    },
  ],
  msgCode: [{ required: true, message: "请输入验证码", trigger: "blur" }],
  password: [{ validator: validatePassword, trigger: "blur" }],
  passwordout: [
    { required: true, message: "请确认密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== form.value.password) {
          callback(new Error("两次密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
});
const form = ref({
  email: "",
});
const yonghuxieyiopen = ref(false);
const tongyi = () => {
  form.value.yhxy = [];
  form.value.yhxy.push("trueword");
  yonghuxieyiopen.value = false;
  huoquyanzhengma();
};
const yanzhengma = ref("获取验证码");
const emailRef = ref(null);
const huoquyanzhengma = () => {
  if (shurushow.value == "shoujihao") {
    emailRef.value?.validate((valid) => {
      if (valid) {
        if (form.value.yhxy?.length > 0 && form.value.yhxy) {
        } else {
          yonghuxieyiopen.value = true;
          return;
        }
        if (counterStore.shimingzhuangtai == false) {
          zhenrenjiaoyan.value = true;
          return;
        }

        const params = {
          userName: form.value.email,
        };
        checkUserAccount(params).then((res) => {
          if (res.data) {
            huoqu();
          } else {
            modal
              .confirm("该邮箱已存在，去登录")
              .then(function () {
                counterStore.logintitle();
              })
              .catch(() => {});
          }
        });
      }
    });
  } else {
    huoqu();
  }
};

const huoqu = () => {
  if (yanzhengma.value === "获取验证码" || yanzhengma.value === "重新获取") {
    if (form.value.email) {
      const params = {
        userName: form.value.email,
      };
      sendEmailCode(params).then((res) => {
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
};
const onphoneSubmit = () => {
  if (form.value.msgCode) {
    const params = {
      userName: form.value.email,
      msgCode: form.value.msgCode,
    };
    checkRegisterInfo(params).then((res) => {
      shurushow.value = "possword";
    });
  } else {
    ElMessage.error("请输入验证码");
  }
};
// 注册
const formRef = ref(null);
const onSubmit = () => {
  formRef.value?.validate((valid) => {
    if (valid) {
      const params = {
        email: form.value.email,
        msgCode: form.value.msgCode,
        password: encrypt(form.value.password),
        userType: 2, //用户类型
      };
      authregister(params).then((res) => {
        if (res.code === 200) {
         
           shurushow.value = "chongzhichenggong";
          timer.value = setInterval(() => {
            daojishijian.value--;
            if (daojishijian.value <= 0) {
              fanhuidenglu();
            }
          }, 1000);
        }
      });
    }
  });
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
  justify-content: space-between;
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
.lansebut {
  color: $main-color;
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