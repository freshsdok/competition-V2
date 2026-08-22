<template>
  <div class="longin">
    <div class="loginimg">
      <!-- <img src="@/assets/images/home-bg2.png" alt="" style="width: 100%;height: 100vh;object-fit: cover;" /> -->
    </div>
    <div class="loginright">
      <div class="huanyingdengl">欢迎登录</div>
      <el-tabs v-model="activeName" class="demo-tabs" @tab-change="chongzhi">
        <el-tab-pane label="账户密码登录" name="first"></el-tab-pane>
        <el-tab-pane label="手机验证码登录" name="fourth"></el-tab-pane>
      </el-tabs>
      <div v-if="activeName === 'first'" class="loginform">
        <el-form :model="form" label-width="auto" :rules="rules" ref="formRef">
          <el-form-item prop="userName">
            <el-input
              v-model="form.userName"
              placeholder="请输入用户名"
              class="ipt"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              show-password
              placeholder="请输入密码"
              class="ipt"
            />
          </el-form-item>
          <div
            style="display: flex; display: flex; justify-content: space-between"
          >
            <el-checkbox-group v-model="form.type">
              <el-checkbox value="trueword" name="type"> 记住密码 </el-checkbox>
            </el-checkbox-group>
            <el-button style="border: 0" @click="passwords">忘记密码</el-button>
          </div>
          <el-button type="primary" @click="onSubmit" class="denglu"
            >登录</el-button
          >
          <el-form-item prop="yhxy">
            <el-checkbox-group v-model="form.yhxy">
              <el-checkbox value="trueword" name="type">
                阅读并同意 <span class="lansebut">《用户协议》</span>和<span
                  class="lansebut"
                  >《隐私协议》</span
                >
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <!-- <el-divider style="margin-top: 40px"> 其他登录方式 </el-divider>
          <div class="weixintubiao">
            <img src="@/assets/images/weixin.png" alt="" />
          </div> -->
          <div class="zhuc">
            没有账号，<span class="lansebut" @click="register">立即注册</span>
          </div>
        </el-form>
      </div>
      <div v-if="activeName === 'fourth'" class="loginform">
        <el-form
          :model="form"
          label-width="auto"
          ref="fourthformRef"
          :rules="rulesCode"
        >
          <el-form-item prop="userName">
            <el-input
              v-model="form.userName"
              placeholder="请输入手机号"
              class="ipt"
            />
          </el-form-item>
          <el-form-item style="display: flex" prop="msgCode">
            <div style="display: flex; width: 100%">
              <el-input
                v-model="form.msgCode"
                placeholder="请输入验证码"
                style="width: 100%"
                class="ipt"
              />
              <el-button
                style="
                  width: 130px;
                  margin-left: 20px;
                  flex-shrink: 0;
                  color: #fff;
                "
                class="ipt"
                @click="huoquyanzhengma"
                type="primary"
                >{{
                  typeof yanzhengma == "number" ? yanzhengma + "s" : yanzhengma
                }}</el-button
              >
            </div>
          </el-form-item>
          <div
            style="display: flex; display: flex; justify-content: space-between"
          >
            <el-checkbox-group v-model="form.type">
              <el-checkbox value="trueword" name="type">
                记住登录状态
              </el-checkbox>
            </el-checkbox-group>
            <el-button style="border: 0" @click="passwords">忘记密码</el-button>
          </div>
          <el-button type="primary" @click="onphoneSubmit" class="denglu"
            >登录</el-button
          >
          <!-- 用户协议（可选验证） -->
          <el-form-item>
            <el-checkbox-group v-model="form.yhxy">
              <el-checkbox value="trueword" name="type">
                阅读并同意 <span class="lansebut">《用户协议》</span>和<span
                  class="lansebut"
                  >《隐私协议》</span
                >
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <!-- <el-divider style="margin-top: 40px"> 其他登录方式 </el-divider>
          <div class="weixintubiao">
            <img src="@/assets/images/weixin.png" alt="" />
          </div> -->
          <div class="zhuc">
            没有账号，<span class="lansebut" @click="register">立即注册</span>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { authlogin, authinfo, getPhoneCode, userInfoLogin } from "@/api/index";
import { useRouter } from "vue-router";
import { setToken, setinfo } from "@/utils/auth";
import { ElMessage } from "element-plus";
import Cookies from "js-cookie";
import { onMounted, ref } from "vue";
const router = useRouter();

const activeName = ref("first");

const form = reactive({
  userName: "",
  password: "",
  code: "",
  type: [],
  yhxy: [],
});
// 验证规则
const rules = reactive({
  userName: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 20, message: "用户名长度为2-20位", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度为6-20位", trigger: "blur" },
  ],
  yhxy: [
    {
      validator: (rule, value, callback) => {
        if (value == undefined || value.length == 0) {
          callback(new Error("请阅读并同意用户协议和隐私协议"));
        } else {
          callback();
        }
      },
      trigger: "change",
    },
  ],
});

const rulesCode = reactive({
  userName: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 20, message: "用户名长度为2-20位", trigger: "blur" },
  ],
  msgCode: [{ required: true, message: "请输入验证码", trigger: "blur" }],
  yhxy: [
    {
      validator: (rule, value, callback) => {
        if (value == undefined || value.length == 0) {
          callback(new Error("请阅读并同意用户协议和隐私协议"));
        } else {
          callback();
        }
      },
      trigger: "change",
    },
  ],
});

const chongzhi = () => {
  form.userName = "";
  form.password = "";
  form.msgCode = "";
};
const formRef = ref(null);
const onSubmit = () => {
  formRef.value?.validate((valid) => {
    if (valid) {
      if (form.type.length !== 0) {
        Cookies.set(
          "trueword",
          JSON.stringify({
            userName: form.userName,
            password: form.password,
          })
        );
      } else {
        Cookies.remove("trueword");
      }
      const params = {
        userName: form.userName,
        password: form.password,
      };
      authlogin(params).then((res) => {
        if (res.code === 200) {
          setToken(res.data.access_token);
          authinfo().then((row) => {
            if (res.code === 200) {
              setinfo(JSON.stringify(row.data));
              router.push("/home");
            }
          });
        } else {
          ElMessage.error(res.msg);
        }
      });
    } else {
      console.log("表单验证失败");
    }
  });
};
const fourthformRef = ref(null);
const onphoneSubmit = () => {
  fourthformRef.value?.validate((valid) => {
    if (valid) {
      const params = {
        userName: form.userName,
        msgCode: form.msgCode,
      };
      userInfoLogin(params).then((res) => {
        if (res.code === 200) {
          setToken(res.data.access_token);
          authinfo().then((row) => {
            if (res.code === 200) {
              setinfo(JSON.stringify(row.data));
              router.push("/home");
            }
          });
        } else {
          ElMessage.error(res.msg);
        }
      });
    } else {
     
    }
  });
};
const yanzhengma = ref("获取验证码");
const huoquyanzhengma = () => {
  if (!form.userName) {
    ElMessage.error("请输入手机号");
    return;
  }
  if (yanzhengma.value === "获取验证码" || yanzhengma.value === "重新获取") {
    yanzhengma.value = 60;
    const sss = setInterval(() => {
      if (yanzhengma.value > 0) {
        yanzhengma.value--;
      } else {
        clearInterval(sss);
        yanzhengma.value = "重新获取";
      }
    }, 1000);
    if (form.userName) {
      const params = {
        userName: form.userName,
      };
      getPhoneCode(params).then((res) => {});
    }
  } else {
    return;
  }
};
const register = () => {
  router.push("/register");
};
const passwords = () => {
  router.push("/password");
};
onMounted(() => {
  if (Cookies.get("trueword")) {
    form.userName = JSON.parse(Cookies.get("trueword"))?.userName;
    form.password = JSON.parse(Cookies.get("trueword"))?.password;
  }
});
</script>


<style scoped lang="scss">
.longin {
  display: flex;

  .loginimg {
    height: 100vh;
    width: 60%;
    background: url("@/assets/images/home-bg2.png") no-repeat center center;
    background-size: cover;
  }

  .loginright {
    width: 40%;

    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    .huanyingdengl {
      font-size: 30px;
      text-align: center;
      // margin-top: 22%;
      font-weight: bold;
    }

    .demo-tabs {
      width: 60%;
      margin: 0 auto;
      margin-top: 30px;
      // margin-bottom: 15px;
    }

    .loginform {
      width: 60%;
      margin: 0 auto;
    }

    .ipt {
      height: 40px;
    }

    .denglu {
      width: 100%;
      height: 40px;
      margin: 30px 0;
    }

    .weixintubiao {
      width: 40px;
      margin: 0 auto;

      img {
        width: 100%;
      }
    }

    .zhuc {
      text-align: center;
      font-size: 12px;
      margin-top: 24px;
    }
  }
}

.lansebut {
  color: #105ec5;
  cursor: pointer;
  /* 手型，常用于可点击元素 */
}
:deep(.el-breadcrumb__inner a, .el-breadcrumb__inner.is-link) {
  color: red !important;
}
</style>