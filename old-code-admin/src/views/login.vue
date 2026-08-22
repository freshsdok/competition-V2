<template>
  <div class="login-container">
    <pointwave :color="0x33ccff" />
    <div
      style="
        position: absolute;
        width: 100%;
        left: 0;
        top: 0;
        height: 99%;
        z-index: 1;
        opacity: 0.3;
      "
    ></div>
    <div class="glass-panel">
      <el-form
        ref="loginRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <div class="title-container">
          <h3 class="title">
            <span class="title-text">得时综合教育平台</span>
            <span class="title-subtext">tianda-admin</span>
          </h3>
        </div>
        <el-form-item prop="username">
          <!-- <ipt  v-model="loginForm.username" :placeholder="'账号'" :type="'text'" /> -->
          <el-input
            v-model="loginForm.username"
            type="text"
            size="large"
            auto-complete="off"
             autocomplete="new-password"
            placeholder="账号"
          >
            <template #prefix><svg-icon icon-class="user" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            auto-complete="off"
             autocomplete="new-password"
            placeholder="密码"
            
          >
            <template #prefix><svg-icon icon-class="password" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code" v-if="captchaEnabled">
          <el-input
            v-model="loginForm.code"
            size="large"
            auto-complete="off"
            placeholder="验证码"
            style="width: 63%"
            
          >
            <template #prefix><svg-icon icon-class="validCode" /></template>
          </el-input>
          <div class="login-code">
            <img :src="codeUrl" @click="getCode" class="login-code-img" />
          </div>
        </el-form-item>
        <el-checkbox
          v-model="loginForm.rememberMe"
          style="margin: 0px 0px 25px 0px"
          >记住密码</el-checkbox
        >
        <el-form-item style="width: 100%">
          <el-button
            :loading="loading"
            size="large"
            type="primary"
            style="width: 100%"
            @click.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
          <div style="float: right" v-if="register">
            <router-link class="link-type" :to="'/register'"
              >立即注册</router-link
            >
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from "@/utils/jsencrypt";
import useUserStore from "@/store/modules/user";
import Pointwave from "@/components/ThreeJs/Pointwave";
import ipt from "@/components/hsipt/index.vue";
const title = import.meta.env.VITE_APP_TITLE;
const userStore = useUserStore();
const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();

const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
  code: "",
  uuid: "",
});

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }],
};

const codeUrl = ref("");
const loading = ref(false);
// 验证码开关
const captchaEnabled = ref(false);
// 注册开关
const register = ref(false);
const redirect = ref(undefined);

watch(
  route,
  (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect;
  },
  { immediate: true }
);

function handleLogin() {
  proxy.$refs.loginRef.validate((valid) => {
    if (valid) {
      loading.value = true;
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 });
        Cookies.set("password", encrypt(loginForm.value.password), {
          expires: 30,
        });
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 });
      } else {
        // 否则移除
        Cookies.remove("username");
        Cookies.remove("password");
        Cookies.remove("rememberMe");
      }
      // 调用action的登录方法
      userStore
        .login(loginForm.value)
        .then(() => {
          const query = route.query;
          const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
            if (cur !== "redirect") {
              acc[cur] = query[cur];
            }
            return acc;
          }, {});
          router.push({ path: redirect.value || "/", query: otherQueryParams });
        })
        .catch(() => {
          loading.value = false;
          // 重新获取验证码
          if (captchaEnabled.value) {
            getCode();
          }
        });
    }
  });
}

function getCode() {
  getCodeImg().then((res) => {
    captchaEnabled.value =
      res.captchaEnabled === undefined ? true : res.captchaEnabled;
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img;
      loginForm.value.uuid = res.uuid;
    }
  });
}

function getCookie() {
  const username = Cookies.get("username");
  const password = Cookies.get("password");
  const rememberMe = Cookies.get("rememberMe");
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password:
      password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
  };
}

getCode();
getCookie();
</script>
<style scoped>
.login-container {
  width: 100vw;
  height: 100vh; /* 关键：高度也要设为 100% 视口高度 */
  background: url("../assets/images/login-background.png") center center / cover
    no-repeat;
  position: fixed;
  top: 0;
  left: 0;
  /* 可选：防止内容溢出 */
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-page {
  -webkit-border-radius: 5px;
  border-radius: 10px;
  margin: 280px auto;
  width: 350px;
  padding: 35px 35px 15px;
  background: #fff;
  border: 1px solid #eaeaea;
  box-shadow: 0 0 25px #cac6c6;
}

label.el-checkbox.remember {
  margin: 0px 0px 25px;
  text-align: left;
}
</style>
<style lang="scss" scoped>
$bg: #15255b;
$dark_gray: #889aa4;
$light_gray: #eee;

.login-container {
  min-height: 100%;
  width: 100%;
  background-color: $bg;
  overflow: hidden;

  .glass-panel {
    position: relative;
    width: 90%;
    max-width: 520px;
    padding: 40px;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(12px);
    border-radius: 16px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    z-index: 2;
    transition: box-shadow 0.5s cubic-bezier(0.23, 1, 0.32, 1);
  }

  .glass-panel:hover {
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  }

  .login-form {
    width: 100%;
    overflow: hidden;
  }

  .el-input {
    position: relative;
    transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
    opacity: 0;
    transform: translateY(20px);
    animation: inputFadeIn 0.6s cubic-bezier(0.23, 1, 0.32, 1) forwards;
  }

  .el-input:nth-child(2) {
    animation-delay: 0.2s;
  }

  .el-input:nth-child(3) {
    animation-delay: 0.3s;
  }

  .el-input:focus-within {
    transform: translateX(8px) translateY(-2px);
  }

  .el-input .el-input__wrapper {
    background: rgba(255, 255, 255, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 12px;
    backdrop-filter: blur(5px);
    transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
    overflow: hidden;
    position: relative;
  }

  .el-input .el-input__wrapper::before {
    content: "";
    position: absolute;
    top: -1px;
    left: -1px;
    right: -1px;
    height: 1px;
    background: linear-gradient(90deg, transparent, #33ccff, transparent);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  .el-input .el-input__wrapper:focus-within {
    box-shadow: 0 4px 20px rgba(51, 204, 255, 0.3);
    border-color: #33ccff;
    background: rgba(255, 255, 255, 0.25);
    transform: translateY(-1px);
  }

  .el-input .el-input__wrapper:focus-within::before {
    opacity: 1;
  }

  .el-input .el-input__inner {
    color: #ffffff;
    font-size: 17px;
    padding: 15px 16px;
    background: transparent;
  }

  :deep(.el-input__inner) {
    color: #ffffff !important;
  }

  .el-input .el-input__prefix {
    color: rgba(255, 255, 255, 0.7);
    transition: color 0.3s ease;
  }

  .el-input:focus-within .el-input__prefix {
    color: #33ccff;
  }

  @keyframes inputFadeIn {
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .el-button {
    position: relative;
    overflow: hidden;
    transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
    font-size: 18px;
    font-weight: 600;
    letter-spacing: 1px;
    background: linear-gradient(135deg, #33ccff, #0099cc);
    border: none;
    border-radius: 12px;
    padding: 14px 0;
    color: white;
    opacity: 0;
    transform: translateY(20px);
    animation: buttonFadeIn 0.6s cubic-bezier(0.23, 1, 0.32, 1) 0.4s forwards;
  }

  .el-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(51, 204, 255, 0.25);
    background: linear-gradient(135deg, #4dd2ff, #00a3e6);
  }

  .el-button:active {
    transform: translateY(-2px);
  }

  .el-button::before {
    content: "";
    position: absolute;
    top: -1px;
    left: -1px;
    right: -1px;
    height: 2px;
    background: linear-gradient(90deg, transparent, #ffffff, transparent);
    opacity: 0.7;
    animation: buttonGlow 2s infinite;
  }

  .el-button::after {
    content: "";
    position: absolute;
    top: 50%;
    left: 50%;
    width: 150%;
    height: 150%;
    background: radial-gradient(
      circle,
      rgba(255, 255, 255, 0.4) 0%,
      rgba(255, 255, 255, 0) 80%
    );
    transform: translate(-50%, -50%) scale(0);
    transition: transform 0.5s cubic-bezier(0.23, 1, 0.32, 1);
  }

  .el-button:active::after {
    transform: translate(-50%, -50%) scale(1);
  }

  .el-button--loading .el-loading-spinner {
    color: white;
  }

  @keyframes buttonFadeIn {
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes buttonGlow {
    0%,
    100% {
      transform: translateX(-100%);
    }
    50% {
      transform: translateX(100%);
    }
  }

  :deep(.el-checkbox) {
    color: rgba(255, 255, 255, 0.7);
  }

  .tips {
    font-size: 14px;
    color: #fff;
    margin-bottom: 10px;

    span {
      &:first-of-type {
        margin-right: 16px;
      }
    }
  }

  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
  }

  .title-container {
    position: relative;
    margin-bottom: 40px;

    .title {
      font-size: 2.5rem;
      color: $light_gray;
      margin: 0;
      text-align: center;
      font-weight: 700;
      letter-spacing: 1px;
      position: relative;
      display: flex;
      flex-direction: column;
      align-items: center;
      opacity: 0;
      transform: translateY(20px);
      animation: titleFadeIn 0.8s cubic-bezier(0.215, 0.61, 0.355, 1) forwards;
    }

    .title-text {
      display: block;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
      background: linear-gradient(135deg, #ffffff, #b0e2ff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: 5px;
    }

    .title-subtext {
      font-size: 1.2rem;
      font-weight: 500;
      color: rgba(255, 255, 255, 0.8);
      text-shadow: 0 1px 5px rgba(0, 0, 0, 0.3);
      display: block;
    }

    .title::after {
      content: "";
      position: absolute;
      bottom: -15px;
      width: 160px;
      height: 3px;
      background: linear-gradient(90deg, transparent, #33ccff, transparent);
      border-radius: 1.5px;
      animation: titleGlow 2s ease-in-out infinite;
    }
  }

  @keyframes titleFadeIn {
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes titleGlow {
    0%,
    100% {
      opacity: 0.6;
      transform: scaleX(0.8);
    }
    50% {
      opacity: 1;
      transform: scaleX(1);
    }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }
}

#indexLizi {
  position: absolute;
  width: 100%;
  top: 0;
  bottom: 0;
  overflow: hidden;
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
:deep(.el-input--large .el-input__wrapper) {
  background: transparent;
  width: 400px;
  height: 60px;
}

:deep(.el-form-item__error) {
  position: absolute;
  top: calc(100% + 2px);
  left: 0;
  margin-top: 0;
  padding: 0;
  font-size: 12px;
  color: #f56c6c;
  line-height: 1;
  white-space: nowrap;
  z-index: 10;
}
.login-code {
  height: 60px;
}
</style>
