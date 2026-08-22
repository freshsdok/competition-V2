
<template>
  <div>
    <el-form :model="form" label-width="auto" :rules="rules" ref="formRef">
      <el-form-item prop="userName">
        <el-input
          v-model.trim="form.userName"
          autocomplete="userName"
          placeholder="邮箱/手机号"
          class="ipt"
        />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model.trim="form.password"
          type="password"
          autocomplete="new-password"
          placeholder="密码"
          class="ipt"
        />
      </el-form-item>

      <!-- <el-form-item prop="yhxy">
        <el-checkbox-group v-model="form.yhxy">
          <el-checkbox value="trueword" name="type">
            阅读并同意 <span class="lansebut">《用户协议》</span>和<span
              class="lansebut"
              >《隐私协议》</span
            >
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item> -->
      <!-- <el-checkbox-group v-model="form.type">
        <el-checkbox value="trueword" name="type"> 记住密码 </el-checkbox>
      </el-checkbox-group> -->
      <el-button type="primary" @click="onSubmit" class="denglu"
        >登录</el-button
      >
      <div class="zhuche">
        <div class="xiangxuanxiang" @click="counterStore.phoneregister">
          <img src="@/assets/icon/register.png" alt="" class="icon" />
          立即注册
        </div>
        <div class="xiangxuanxiang" @click="counterStore.password">
          <img
            src="@/assets/icon/chongzhimima.png"
            alt=""
            class="icon"
          />重置密码
        </div>
        <div class="xiangxuanxiang" @click="counterStore.phonelogintitle">
          <img src="@/assets/icon/phone.png" alt="" class="icon" />手机号登录
        </div>
      </div>
    </el-form>
    <el-divider> 更多登录方式 </el-divider>
    <div class="weixintubiao" @click="weixindenglu">
      <img src="@/assets/images/weixin.png" alt="" />
    </div>
    <!-- <el-dialog v-model="zhenrenjiaoyan" title="请完成安全验证" width="400">
      <huakuai @close="huakuaiopen" />
    </el-dialog> -->
  </div>
</template>
<script setup>
import { authlogin, authinfo } from "@/api/index";
import { setToken, setinfo, getinfo } from "@/utils/auth";
import { encrypt, decrypt } from "@/utils/jsencrypt.js";
import { ElMessage } from "element-plus";
import { useCounterStore } from "@/stores/index";
import huakuai from "./huakuai.vue";
import Cookies from "js-cookie";
import { set } from "lodash";
const zhenrenjiaoyan = ref(false);
const huakuaiopen = () => {
  zhenrenjiaoyan.value = false;
  onSubmit();
};

const counterStore = useCounterStore();
// 验证规则
const validatePassword = (rule, value, callback) => {
  if (!value) {
    return callback(new Error("请输入密码"));
  }

  if (value.length < 6 || value.length > 16) {
    return callback(new Error("密码长度必须为6-16位"));
  }

  callback();
};
const rules = reactive({
  userName: [{ required: true, message: "请输入邮箱/手机号", trigger: "blur" }],
  password: [{ validator: validatePassword, trigger: "blur" }],
});
const form = ref({
  type: [],
});
const formRef = ref(null);

const onSubmit = () => {
  formRef.value?.validate((valid) => {
    if (valid) {
      if (form.value.type.length !== 0) {
        Cookies.set(
          "trueword",
          JSON.stringify({
            userName: form.value.userName,
            password: encrypt(form.value.password),
          })
        );
      } else {
        Cookies.remove("trueword");
      }
      // if (counterStore.shimingzhuangtai == false) {
      //   zhenrenjiaoyan.value = true;
      //   return;
      // }
      const params = {
        userName: form.value.userName,
        password: encrypt(form.value.password),
      };
      authlogin(params).then((res) => {
        if (res.code === 200) {
          setToken(res.data.access_token);
          authinfo().then((row) => {
            if (row.code === 200) {
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
      console.log("表单验证失败");
    }
  });
};
const weixindenglu = () => {
  ElMessage.warning("功能暂未开通");
};
onMounted(() => {
  if (Cookies.get("trueword")) {
    form.value.userName = JSON.parse(Cookies.get("trueword"))?.userName;
    form.value.password = decrypt(
      JSON.parse(Cookies.get("trueword"))?.password
    );
  }
});
</script>

<style scoped lang="scss">
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
  margin-top: 10px;
  margin-bottom: 10px;
}
.weixintubiao {
  width: 40px;
  margin: 0 auto;
  cursor: pointer;
  img {
    width: 100%;
  }
}

:deep(.el-divider__text.is-center) {
  color: #aeaeae;
}
</style>