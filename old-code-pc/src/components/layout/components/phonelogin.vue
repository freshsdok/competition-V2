
<template>
  <div>
    <div v-if="shurushow == 'shoujihao'">
      <el-form :model="form" label-width="auto" :rules="rules" ref="formRef">
        <el-form-item prop="userName" style="margin-top: 40px">
          <el-input
            v-model.trim="form.userName"
            placeholder="请输入手机号"
            class="ipt"
          />
        </el-form-item>

        <el-button
          type="primary"
          @click="huoquyanzhengma"
          class="denglu"
          style="margin-top: 20px"
          :disabled="form.userName == ''"
          >验证并登录</el-button
        >
        <div class="zhuche">
          <div class="xiangxuanxiang" @click="counterStore.phoneregister">
            <img src="@/assets/icon/register.png" alt="" class="icon" />
            立即注册
          </div>

          <div class="xiangxuanxiang" @click="counterStore.logintitle">
            <img src="@/assets/icon/yonghu.png" alt="" class="icon" />账密登录
          </div>
        </div>
      </el-form>
      <el-divider style="margin-top: 60px"> 更多登录方式 </el-divider>
      <div class="weixintubiao" style="margin-top: 20px" @click="weixindenglu">
        <img src="@/assets/images/weixin.png" alt="" />
      </div>
    </div>
    <div v-if="shurushow == 'yanzhengma'">
      <div class="yanzhengmatishi">
        验证码已发送至  {{
          form.userName.substring(0, 3) +
          "****" +
          form.userName.substring(7)
        }}, 请输入验证码。
      </div>

      <el-form
        :model="form"
        label-width="auto"
        :rules="rules"
        ref="formRef"
        style="margin-top: 30px"
      >
        <el-form-item style="display: flex" prop="msgCode">
          <div style="display: flex; width: 100%">
            <el-input
              v-model.trim="form.msgCode"
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
          >验证并登录</el-button
        >
        <div style="display: flex; justify-content: flex-end">
          <div class="xiangxuanxiang" @click="counterStore.logintitle">
            <img src="@/assets/icon/yonghu.png" alt="" class="icon" />账密登录
          </div>
        </div>
      </el-form>
    </div>
    <el-dialog v-model="zhenrenjiaoyan" title="请完成安全验证" width="400"   top="300px">
      <huakuai @close="huakuaiopen" />
    </el-dialog>
  </div>
</template>
<script setup>
import { authinfo, getPhoneCodeCaptcha, userInfoLogin } from "@/api/index";
import { setToken, setinfo ,getinfo} from "@/utils/auth";
import { ElMessage } from "element-plus";
import { useCounterStore } from "@/stores/index";

import huakuai from "./huakuai.vue";
const zhenrenjiaoyan = ref(false);
const huakuaiopen = () => {
  zhenrenjiaoyan.value = false;
  huoquyanzhengma();
};
const counterStore = useCounterStore();
const shurushow = ref("shoujihao");
// 验证规则
const rules = reactive({
  userName: [{ required: true, message: "请输入手机号", trigger: "blur" }],
  msgCode: [{ required: true, message: "请输入验证码", trigger: "blur" }],
});
const form = ref({
  userName: "",
});

const yanzhengma = ref("获取验证码");
const huoquyanzhengma = () => {
  if (!form.value.userName) {
    ElMessage.error("请输入手机号");
    return;
  }
  if (counterStore.shimingzhuangtai == false) {
    zhenrenjiaoyan.value = true;
    return;
  }
  if (yanzhengma.value === "获取验证码" || yanzhengma.value === "重新获取") {
    if (form.value.userName) {
      const params = {
        userName: form.value.userName,
      };
      getPhoneCodeCaptcha(params).then((res) => {
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
      userName: form.value.userName,
      msgCode: form.value.msgCode,
    };
    userInfoLogin(params).then((res) => {
      if (res.code === 200) {
        setToken(res.data.access_token);
        authinfo().then((row) => {
          console.log(row.code,0)
          if (row.code === 200) {
            console.log(row.data,1)
            setinfo(JSON.stringify(row.data));

            counterStore.decrement();
            
            location.reload();
          }
        });
      } else {
        ElMessage.error(res.msg);
      }
    });
  } else {
    ElMessage.error("请输入验证码");
  }
};
const weixindenglu = () => {
  ElMessage.warning("功能暂未开通");
};
</script>

<style scoped lang="scss">
:deep(.el-dialog) {
  padding: 60px 50px 0 50px;
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
  justify-content: space-evenly;
  margin-top: 30px;
  margin-bottom: 10px;
}
.weixintubiao {
  width: 40px;
  margin: 0 auto;

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
</style>