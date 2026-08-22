<template>
  <div class="longin">
    <div class="loginimg"></div>
    <div class="loginright">
      <div class="huanyingdengl">用户注册</div>
      <el-tabs v-model="activeName" class="demo-tabs" @tab-change="chongzhi">
        <el-tab-pane label="手机号注册" name="first"></el-tab-pane>
        <el-tab-pane label="邮箱注册" name="fourth"></el-tab-pane>
      </el-tabs>
      <div v-if="activeName === 'first'" class="loginform">
        <el-form :model="form" label-width="auto" :rules="rules" ref="formRef">
          <el-form-item prop="phonenumber">
            <el-input
              v-model="form.phonenumber"
              placeholder="请输入手机号"
              class="ipt"
            />
          </el-form-item>
          <el-form-item prop="msgCode">
            <div style="display: flex; width: 100%">
              <el-input
                v-model="form.msgCode"
                placeholder="请输入验证码"
                style="width: 100%"
                class="ipt"
              />
              <el-button
              type="primary"
                style="width: 130px; margin-left: 20px; flex-shrink: 0;color: #fff;"
                class="ipt"
                @click="huoquyanzhengma"
                > {{ typeof msgCode=='number'?msgCode+'s':msgCode }}</el-button
              >
            </div>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              show-password
              placeholder="请输入密码"
              class="ipt"
            />
          </el-form-item>
          <el-form-item prop="passwordout">
            <el-input
              v-model="form.passwordout"
              show-password
              placeholder="请确认密码"
              class="ipt"
            />
          </el-form-item>
      <div class="zhuc">
            已有账号，<span class="lansebut" @click="register">去登录</span>
          </div>
          <el-button type="primary" @click="onSubmit" class="denglu"
            >注册</el-button
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
     
    
        </el-form>
      </div>
      <div v-if="activeName === 'fourth'" class="loginform">
        <el-form :model="form" label-width="auto" :rules="rules" ref="formRef">
          <el-form-item prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              class="ipt"
            />
          </el-form-item>
          <el-form-item prop="msgCode">
            <div style="display: flex; width: 100%">
              <el-input
                v-model="form.msgCode"
                placeholder="请输入验证码"
                style="width: 100%"
                class="ipt"
              />
              <el-button
                style="width: 130px; margin-left: 20px; flex-shrink: 0;color: #fff;"
                class="ipt"
                type="primary"
                @click="emailhuoquyanzhengma"
                >{{   typeof emailmsgCode=='number'?emailmsgCode+'s':emailmsgCode }}</el-button
              >
            </div>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              show-password
              placeholder="请输入密码"
              class="ipt"
            />
          </el-form-item>
          <el-form-item prop="passwordout">
            <el-input
              v-model="form.passwordout"
              show-password
              placeholder="请确认密码"
              class="ipt"
            />
          </el-form-item>
                 <div class="zhuc">
            已有账号，<span class="lansebut" @click="register">去登录</span>
          </div>
          <el-button type="primary" @click="onSubmit" class="denglu"
            >注册</el-button
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
          
   
        </el-form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { authregister, getPhoneCode, sendEmailCode } from "@/api/index";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
const router = useRouter();
const activeName = ref("first");
const form = reactive({
  phonenumber: "",
  msgCode: "",
  password: "",
  passwordout: "",
});
const chongzhi=()=>{
  form.phonenumber=''
  form.msgCode=''
  form.password= "",
  form.passwordout=''
  form.email=''
}
// 验证规则
const rules = reactive({
  phonenumber: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    {
      type: "email",
      message: "请输入正确的邮箱地址",
      trigger: ["blur", "change"],
    },
  ],
  msgCode: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { len: 6, message: "验证码为6位", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度为6-20位", trigger: "blur" },
  ],
  passwordout: [
    { required: true, message: "请确认密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error("两次密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
  yhxy: [
    {
      validator: (rule, value, callback) => {
        console.log(value)
        if (value==undefined||value.length==0) {
          callback(new Error("请阅读并同意用户协议和隐私协议"));
        } else {
          callback();
        }
      },
      trigger: "change",
    },
  ],
});
const msgCode = ref("获取验证码");
const huoquyanzhengma = () => {
  if (!form.phonenumber) {
    ElMessage.error("请输入手机号");
    return;
  }
  if (msgCode.value === "获取验证码" || msgCode.value === "重新获取") {
    msgCode.value = 60;
    const sss = setInterval(() => {
      if (msgCode.value > 0) {
        msgCode.value--;
      } else {
        clearInterval(sss);
        msgCode.value = "重新获取";
      }
    }, 1000);
    if (form.phonenumber) {
      const params = {
        userName: form.phonenumber,
      };
      getPhoneCode(params).then((res) => {});
    }
  } else {
    return;
  }
};

const emailmsgCode = ref("获取验证码");
const emailhuoquyanzhengma = () => {
    if (!form.email) {
    ElMessage.error("请输入邮箱");
    return;
  }
  if (
    emailmsgCode.value === "获取验证码" ||
    emailmsgCode.value === "重新获取"
  ) {
    emailmsgCode.value = 60;
    const sss = setInterval(() => {
      if (emailmsgCode.value > 0) {
        emailmsgCode.value--;
      } else {
        clearInterval(sss);
        emailmsgCode.value = "重新获取";
      }
    }, 1000);
    if (form.email) {
      const params = {
        userName: form.email,
      };
      sendEmailCode(params).then((res) => {});
    }
  } else {
    return;
  }
};
const formRef = ref(null);
// 注册
const onSubmit = () => {
  formRef.value?.validate((valid) => {
    if (valid) {
      const params = {
        phonenumber: form.phonenumber,
        msgCode: form.msgCode,
        password: form.password,
        email: form.email,
        userType: 2, //用户类型
      };
      authregister(params).then((res) => {
        if (res.code === 200) {
          ElMessage({
            message: "注册成功",
            type: "success",
          });
          router.push("/login");
        }
      });
    }
  });
};
const register = () => {
  router.push("/login");
};
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
      // margin-top: 15%;
      font-weight: bold;
    }

    .demo-tabs {
      width: 60%;
      margin: 0 auto;
      margin-top: 30px;
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
      text-align: end;
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
</style>