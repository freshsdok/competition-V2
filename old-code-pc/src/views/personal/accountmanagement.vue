<template>
  <div class="bg">
    <div class="container-custom">
      <!-- <el-breadcrumb :separator-icon="ArrowRight" style="margin-top: 40px">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/personal/list' }"
          >个人中心</el-breadcrumb-item
        >
        <el-breadcrumb-item>账号设置</el-breadcrumb-item>
      </el-breadcrumb> -->
      <Breadcrumbar />

      <el-card class="card">
        <div style="width: 100%; display: flex">
          <div class="cardleft">
            <div class="centerlefttx">
              <img
                :src="chuchun.avatar ? chuchun.avatar : img1"
                class="toux"
                alt=""
              />
              <div class="zhangsan">
                <!-- {{ chuchun?.authInfo?.realName || chuchun?.nickName }} -->
                {{
                  chuchun.authStatus == 5
                    ? chuchun?.authInfo?.realName
                    : chuchun?.nickName
                }}
              </div>
            </div>
            <div
              :class="
                xuanzhong == 'personaldata'
                  ? 'centerleftxuanzhong'
                  : 'centerleft'
              "
              @click="xuanzhongout('personaldata')"
            >
              个人资料
            </div>
            <div
              :class="
                xuanzhong == 'nameauthentication'
                  ? 'centerleftxuanzhong'
                  : 'centerleft'
              "
              style="position: relative"
              @click="xuanzhongout('nameauthentication')"
            >
              实名认证

              <img
                :src="baomingchenggong"
                alt=""
                v-if="chuchun.authStatus == 5"
                class="shimingrenzheng"
              />
              <img :src="tanhao" alt="" v-else class="shimingrenzheng" />
            </div>
            <div
              :class="
                xuanzhong == 'identityauthentication'
                  ? 'centerleftxuanzhong'
                  : 'centerleft'
              "
              style="position: relative"
              @click="xuanzhongout('identityauthentication')"
            >
              身份认证
              <img
                :src="baomingchenggong"
                alt=""
                v-if="
                  chuchun.identityInfoList.length > 0 &&
                  chuchun.identityInfoList[0].checkStatus == 6
                "
                class="shimingrenzheng"
              />
              <img :src="tanhao" alt="" v-else class="shimingrenzheng" />
            </div>
            <div
              :class="
                xuanzhong == 'changepassword'
                  ? 'centerleftxuanzhong'
                  : 'centerleft'
              "
              @click="xuanzhongout('changepassword')"
            >
              修改密码
            </div>
            <div
              v-if="false"
              :class="
                xuanzhong == 'accountsecurity'
                  ? 'centerleftxuanzhong'
                  : 'centerleft'
              "
              @click="xuanzhongout('accountsecurity')"
            >
              账号安全
            </div>
          </div>
          <div class="zongkuan">
            <div class="titlexiang">
              {{
                leftfenlie.find((item) => {
                  return item.value == xuanzhong;
                })?.name
              }}
            </div>
            <div class="meirong" v-if="xuanzhong == 'personaldata'">
              <el-form
                :model="form"
                label-width="120px"
                class="w600"
                ref="userFormRef"
                :rules="userRules"
              >
                <el-form-item label="头像" class="avatar-item">
                  <el-upload
                    ref="uploadRef"
                    :headers="upload.headers"
                    :limit="1"
                    accept="image/*"
                    :action="upload.url"
                    :on-error="handleFileError"
                    :on-success="onUploadSuccess"
                    :on-exceed="handleExceed"
                    :file-list="fileList"
                    :disabled="isxiugai"
                    :show-file-list="false"
                  >
                    <img
                      :src="form?.avatar ? form.avatar : img1"
                      class="toux"
                      alt=""
                    />
                    <!-- <el-button class="sctx">上传头像</el-button> -->
                  </el-upload>
                </el-form-item>
                <el-form-item :label="form.authStatus == 5 ? '姓名' : '昵称'">
                  <!-- <el-input
                    v-model="form.nickName"
                    maxlength="30"
                    class="ipth50"
                    show-word-limit
                    disabled
                  /> -->
                  <span style="margin-left: 10px; color: #666">
                    {{
                      form.authStatus == 5
                        ? form?.authInfo?.realName
                        : form?.nickName
                    }}</span
                  >
                </el-form-item>
                <el-form-item label="性别">
                  <el-radio-group v-model="form.sex" :disabled="isxiugai">
                    <el-radio value="0">男</el-radio>
                    <el-radio value="1">女</el-radio>
                    <el-radio value="2">未设置</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="个人简介">
                  <el-input
                    v-model="form.briefIntr"
                    type="textarea"
                    maxlength="150"
                    :rows="4"
                    show-word-limit
                    :disabled="isxiugai"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button
                    v-if="isxiugai"
                    type="primary"
                    @click="isxiugai = false"
                    class="but"
                    >编辑</el-button
                  >

                  <el-button
                    v-else
                    type="primary"
                    @click="onuserlist"
                    class="but"
                    >保存</el-button
                  >
                </el-form-item>
              </el-form>
            </div>
            <div class="meirong" v-if="xuanzhong == 'nameauthentication'">
              <div class="erweima">
                <el-form
                  :model="shimingform"
                  :rules="shimingRules"
                  ref="shimingFormRef"
                  label-width="120px"
                  class="w600"
                  v-if="form?.authStatus == 1"
                >
                  <el-form-item label="国籍" prop="countryName">
                    <el-select
                      class="ipth50"
                      v-model="shimingform.countryName"
                      placeholder="请选择国籍"
                      filterable
                    >
                      <el-option
                        v-for="dict in country_name"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="证件类型" prop="idCardType">
                    <el-select
                      class="ipth50"
                      v-model="shimingform.idCardType"
                      placeholder="请选择证件类型"
                    >
                      <el-option
                        v-for="dict in document_type"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                      ></el-option>
                    </el-select>
                  </el-form-item>

                  <el-form-item label="真实姓名" prop="realName">
                    <el-input
                      v-model="shimingform.realName"
                      class="ipth50"
                      maxlength="20"
                      show-word-limit
                    />
                  </el-form-item>
                  <el-form-item label="证件号" prop="idCard">
                    <el-input
                      v-model="shimingform.idCard"
                      class="ipth50"
                      maxlength="50"
                      show-word-limit
                    />
                  </el-form-item>

                  <el-form-item
                    label="证件正面照"
                    class="avatar-item"
                    prop="idCardFront"
                    v-if="shimingform.idCardType != '1'"
                  >
                    <el-upload
                      ref="uploadRefzhengmian"
                      :headers="upload.headers"
                      :limit="1"
                      accept="image/*"
                      :action="upload.url"
                      :show-file-list="false"
                      :on-error="handleFileError"
                      :on-success="onUploadSuccezhengmian"
                      :on-exceed="handleExceedzhengmian"
                      :file-list="fileList"
                      :before-upload="beforeUploadBeimian"
                    >
                      <img
                        v-if="shimingform?.idCardFront"
                        :src="
                          shimingform.idCardFront
                            ? shimingform.idCardFront
                            : shangc
                        "
                        class="touxtup"
                        alt=""
                      />
                      <div class="shangchuan" v-else>
                        <el-icon><Plus /></el-icon>
                      </div>
                      <el-button class="sctx">上传证件正面照</el-button>
                    </el-upload>
                  </el-form-item>
                  <el-form-item
                    label="证件背面照"
                    class="avatar-item"
                    prop="idCardContrary"
                    v-if="shimingform.idCardType != '1'"
                  >
                    <el-upload
                      ref="uploadRefbeimian"
                      :headers="upload.headers"
                      :limit="1"
                      accept="image/*"
                      :action="upload.url"
                      :show-file-list="false"
                      :on-error="handleFileError"
                      :on-success="onUploadSuccebeimian"
                      :on-exceed="handleExceedbeimian"
                      :file-list="fileList"
                      :before-upload="beforeUploadBeimian"
                    >
                      <img
                        v-if="shimingform?.idCardContrary"
                        :src="
                          shimingform?.idCardContrary
                            ? shimingform.idCardContrary
                            : shangc
                        "
                        class="touxtup"
                        alt=""
                      />
                      <div class="shangchuan" v-else>
                        <el-icon><Plus /></el-icon>
                      </div>
                      <el-button class="sctx">上传证件背面照</el-button>
                    </el-upload>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="shiming" class="but">
                      认证
                    </el-button>
                  </el-form-item>
                </el-form>
                <div
                  v-else-if="form?.authStatus == 3"
                  style="
                    text-align: center;
                    display: flex;
                    justify-content: center;
                  "
                >
                  <div class="renzhengtup">
                    <img
                      src="@/assets/images/dairenzheng.png"
                      alt=""
                      class="renzhengtupimg"
                    />
                    <p class="remzheng">认证中</p>
                  </div>
                </div>
                <div
                  v-else-if="form?.authStatus == 6"
                  style="
                    text-align: center;
                    display: flex;
                    justify-content: center;
                  "
                >
                  <div class="renzhengtup">
                    <img
                      src="@/assets/images/yanzhengshibai.png"
                      alt=""
                      class="renzhengtupimg"
                    />
                    <p class="remzheng">
                      认证失败
                      <span
                        style="color: #3cabe1; cursor: pointer"
                        @click="form.authStatus = 1"
                        >重新认证</span
                      >
                    </p>
                  </div>
                </div>
                <div
                  v-else-if="form?.authStatus == 5"
                  style="
                    text-align: center;
                    display: flex;
                    justify-content: center;
                  "
                >
                  <div class="renzhengtup">
                    <img
                      src="@/assets/images/yirenzheng.png"
                      alt=""
                      class="renzhengtupimg"
                    />
                    <p class="remzheng">
                      已认证
                      <!-- <span
                        style="color: #3cabe1"
                        @click="form.authStatus = 1"
                        >重新认证</span
                      > -->
                    </p>
                    <div class="shiming" v-if="form.authInfo">
                      <div class="lable">国籍：</div>
                      <div>
                        {{
                          country_name.find((item) => {
                            return item.value == form.authInfo.countryName;
                          })?.label
                        }}
                      </div>
                    </div>
                    <div style="clear: both"></div>
                    <div class="shiming" v-if="form.authInfo">
                      <div class="lable">证件类型：</div>
                      <div>
                        {{
                          document_type.find((item) => {
                            return item.value == form.authInfo.idCardType;
                          })?.label
                        }}
                      </div>
                    </div>
                    <div style="clear: both"></div>
                    <div class="shiming">
                      <div class="lable">姓名：</div>
                      <div>{{ form.authInfo?.realName }}</div>
                    </div>
                    <div style="clear: both"></div>
                    <div class="shiming">
                      <div class="lable">证件号：</div>
                      {{ decrypt(form.authInfo?.idCard) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="meirong" v-if="xuanzhong == 'identityauthentication'">
              <authentication :chuchun="chuchun" />
            </div>
            <div class="meirong" v-if="xuanzhong == 'changepassword'">
              <el-form
                :model="powlist"
                :rules="rules"
                ref="passwordFormRef"
                label-width="120px"
                class="w600"
              >
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input
                    v-model="powlist.oldPassword"
                    type="password"
                    class="ipth50"
                    autocomplete="new-password"
                    placeholder="请输入原本密码"
                    show-password
                    @input="powlist.oldPassword = powlist.oldPassword.replace(/\s/g, '')"
                  />
                </el-form-item>

                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="powlist.newPassword"
                    type="password"
                    class="ipth50"
                    autocomplete="new-password"
                    show-password
                    placeholder="请输入新密码"
                    @input="powlist.newPassword = powlist.newPassword.replace(/\s/g, '')"
                  />
                </el-form-item>

                <el-form-item label="确认密码" prop="newPasswordis">
                  <el-input
                    v-model="powlist.newPasswordis"
                    type="password"
                    autocomplete="new-password"
                    class="ipth50"
                    show-password
                    placeholder="请确认新密码"
                    @input="powlist.newPasswordis = powlist.newPasswordis.replace(/\s/g, '')"
                  />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="xiugaimima" class="but">
                    保存
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            <div class="meirong" v-if="xuanzhong == 'accountsecurity'">
              <el-form
                ref="shimingRef"
                :model="form"
                :rules="shenfenrules"
                label-width="120px"
                class="w600"
              >
                <el-form-item label="当前登录账号">
                  {{ chuchun.nickName }}
                </el-form-item>

                <el-form-item label="邮箱账号" prop="email">
                  <!-- <el-input
                    v-model="form.email"
                    class="ipth50"
                  /> -->
                  <span style="display: inline-block; width: 200px">
                    {{ form.email }}
                  </span>

                  <el-button type="primary" @click="xiugai('邮箱')" class="but">
                    编辑
                  </el-button>
                </el-form-item>

                <el-form-item label="手机账号" prop="phonenumber">
                  <!-- <el-input
                    v-model="form.phonenumber"
                    class="ipth50"
                  /> -->
                  <span style="display: inline-block; width: 200px">
                    {{ form.phonenumber }}</span
                  >
                  <el-button
                    type="primary"
                    @click="xiugai('手机号')"
                    class="but"
                  >
                    编辑
                  </el-button>
                </el-form-item>

                <!-- <el-form-item label="微信账号" prop="wxCode">
                  <el-input
                    v-model="form.wxCode"
                    class="ipth50"
                    :disabled="wx"
                  />
                </el-form-item> -->
              </el-form>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    <phoneregister
      v-if="counterStore.changePhoneEmail"
      :phoneemail="isphoneemail"
    />
    <el-dialog
      title="温馨提示"
      v-model="dialogVisible"
      width="500"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div>
        系统检测到您之前已注册账号，已同步您之前账号数据，请退出账号重新登录后查看
      </div>
      <template #footer>
        <el-button type="primary" @click="handleSubmit"> 重新登录 </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import img1 from "@/assets/images/shawn-avatar.png";
import baomingchenggong from "@/assets/images/baomingchenggong.png";
import tanhao from "@/assets/images/tanhao.png";
import phoneregister from "./components/phoneregister.vue";
import shangc from "@/assets/images/addshangc.png";
import { encrypt, decrypt } from "@/utils/jsencrypt.js";
import { logout } from "@/api/index";
import Cookies from "js-cookie";
import {
  updateUserInfo,
  updatePwd,
  saveAuthInfo,
  taskpcrealName,
} from "@/api/accountmanagement/index";
import Breadcrumbar from "@/components/breadcrumbar.vue";
import { getAuthInfo } from "@/api/index";
import { getinfo, getToken, setinfo, removeToken } from "@/utils/auth";
import { nextTick, onMounted } from "vue";
import { ElMessage, genFileId } from "element-plus";
import authentication from "./components/authentication.vue";
import { useDict } from "@/utils/dict";
import { useRoute, useRouter } from "vue-router";
import { useCounterStore } from "@/stores/index";
const counterStore = useCounterStore();
const route = useRoute();

const router = useRouter();
// 校验规则
const userRules = reactive({
  nickName: [
    {
      required: true,
      message: "请输入昵称",
      trigger: "blur",
    },
    {
      max: 30,
      message: "昵称不能超过30个字符",
      trigger: "blur",
    },
  ],
});
const { real_name_auth_status, document_type, country_name } = useDict(
  "real_name_auth_status",
  "document_type",
  "country_name"
);
const leftfenlie = ref([
  {
    name: "个人资料",
    value: "personaldata",
  },
  {
    name: "实名认证",
    value: "nameauthentication",
  },

  {
    name: "身份认证",
    value: "identityauthentication",
  },

  {
    name: "修改密码",
    value: "changepassword",
  },
  {
    name: "账号安全",
    value: "accountsecurity",
  },
]);
// 背景图上传前校验
const beforeUploadBeimian = (file) => {
  // 1. 校验是否为图片类型（MIME 类型以 image/ 开头）
  if (!file.type.startsWith("image/")) {
    ElMessage.error("请上传有效的图片文件（如 JPG、PNG、GIF 等）！");
    return false;
  }

  // 2. 可选：限制文件大小（例如 10MB）
  const maxSize = 10 * 1024 * 1024; // 10MB
  if (file.size > maxSize) {
    ElMessage.error("图片大小不能超过 10MB！");
    return false;
  }

  return true; // 允许上传
};
const fileList = ref([]); // 控制显示的文件列表
/*** 用户导入参数 */
const upload = reactive({
  // 是否显示弹出层（用户导入）
  open: false,
  // 弹出层标题（用户导入）
  title: "",
  // 是否禁用上传
  isUploading: false,
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url: import.meta.env.VITE_APP_BASE_API + "/file/upload",
});
/** 文件上传失败 */
const handleFileError = (error, file, fileList) => {
  ElMessage.error("上传失败");
};
const shenfenrules = reactive({
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "邮箱格式不正确", trigger: "blur" },
  ],
  phonenumber: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "手机号格式不正确",
      trigger: "blur",
    },
  ],
  wxCode: [
    { required: true, message: "请输入微信账号", trigger: "blur" },
    { min: 2, max: 30, message: "长度在 2 到 30 个字符", trigger: "blur" },
  ],
});
const shimingRef = ref(null);
const onUploadSuccess = (response, file) => {
  if (response.code == 200) {
    form.value.avatar = response.data.url;
    form.value.avatarName = response.data.name;
    ElMessage.success("上传成功");
  } else {
    ElMessage.error(response.msg);
  }
};

// ====== 超出文件数量限制时触发 ======
const uploadRef = ref(null);

const handleExceed = (files) => {
  const file = files[0];

  // 1. 清空之前的文件
  uploadRef.value.clearFiles();

  // 2. 手动设置新文件的 uid（避免 Vue 响应式警告）
  file.uid = genFileId();

  // 3. 开始上传新文件
  uploadRef.value.handleStart(file);

  // ✅ 关键：必须手动调用 submit() 才能真正发起请求
  uploadRef.value.submit();
};

const onUploadSuccezhengmian = (response, file) => {
  if (response.code == 200) {
    shimingform.value.idCardFront = response.data.url;
    shimingFormRef.value?.validateField("idCardFront");
    ElMessage.success("上传成功");
  } else {
    ElMessage.error(response.msg);
  }
};

// ====== 超出文件数量限制时触发 ======
const uploadRefzhengmian = ref(null);

const handleExceedzhengmian = (files) => {
  const file = files[0];

  // 1. 清空之前的文件
  uploadRefzhengmian.value.clearFiles();

  // 2. 手动设置新文件的 uid（避免 Vue 响应式警告）
  file.uid = genFileId();

  // 3. 开始上传新文件
  uploadRefzhengmian.value.handleStart(file);

  // ✅ 关键：必须手动调用 submit() 才能真正发起请求
  uploadRefzhengmian.value.submit();
};

const onUploadSuccebeimian = (response, file) => {
  if (response.code == 200) {
    shimingform.value.idCardContrary = response.data.url;
    ElMessage.success("上传成功");
  } else {
    ElMessage.error(response.msg);
  }
};
// ====== 超出文件数量限制时触发 ======
const uploadRefbeimian = ref(null);

const handleExceedbeimian = (files) => {
  const file = files[0];

  // 1. 清空之前的文件
  uploadRefbeimian.value.clearFiles();

  // 2. 手动设置新文件的 uid（避免 Vue 响应式警告）
  file.uid = genFileId();

  // 3. 开始上传新文件
  uploadRefbeimian.value.handleStart(file);

  // ✅ 关键：必须手动调用 submit() 才能真正发起请求
  uploadRefbeimian.value.submit();
};

// 用户信息
const usergetinfo = () => {
  getAuthInfo().then((row) => {
    if (row.code === 200) {
      setinfo(JSON.stringify(row.data));
      form.value = row.data;
      chuchun.value = JSON.parse(JSON.stringify(row.data));
    }
  });
  // form.value = JSON.parse(getinfo());
  // chuchun.value = JSON.parse(getinfo());
};

const xuanzhong = ref("个人资料");

const xuanzhongout = (val) => {
  xuanzhong.value = val;
  router.push({
    path: "/personal/accountmanagement",
    query: {
      classification: val,
    },
  });
};
const form = ref({
  nickName: "",
  sex: "",
  briefIntr: "",
  avatar: null,
});
const sss = getinfo();
const chuchun = ref({
  identityInfoList: [],
});
const powlist = ref({});
// 自定义校验：确认密码必须等于新密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("请再次输入新密码"));
  } else if (value !== powlist.value.newPassword) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

// 表单规则
const rules = reactive({
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    {
      pattern: /^.{6,20}$/,
      message: "密码长度为6-20位",
      trigger: "blur",
    },
  ],
  newPasswordis: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    { required: true, validator: validateConfirmPassword, trigger: "blur" },
  ],
});

// 表单引用
const passwordFormRef = ref(null);

// 判断是否可以修改
const isxiugai = ref(true);

const { proxy } = getCurrentInstance();
const userFormRef = ref(null);
const onuserlist = () => {
  userFormRef.value?.validate((valid) => {
    if (valid) {
      const params = {
        nickName: form.value.nickName,
        sex: form.value.sex,
        briefIntr: form.value.briefIntr,
        avatar: form.value.avatar,
        avatarName: form.value.avatarName,
      };
      updateUserInfo(params).then((res) => {
        usergetinfo();
        ElMessage.success("修改成功");
        isxiugai.value = true;
      });
    }
  });
};
const xiugaimima = () => {
  passwordFormRef.value?.validate((valid) => {
    if (valid) {
      const params = {
        newPassword: encrypt(powlist.value.newPassword),
        newPasswordis: encrypt(powlist.value.newPasswordis),
        oldPassword: encrypt(powlist.value.oldPassword),
      };
      updatePwd(params).then((res) => {
        if (res.code == 200) {
          ElMessage.success("修改成功");
          powlist.value = {};
        } else {
          ElMessage.error(res.msg);
        }
      });
    }
  });
};
const isphoneemail = ref("手机号");
const xiugai = (item) => {
  counterStore.changePhoneEmailincrement();
  isphoneemail.value = item;
};

const shimingform = ref({
  countryName: "CN",
  idCardType: "1",
  realName: "",
  idCard: "",
  idCardFront: "", // ← 改为字符串空值，而不是 null
  idCardContrary: "", // ← 同上
});
// 自定义身份证校验函数
const validateIdCard = (rule, value, callback) => {
  if (!value) {
    return callback(new Error("请输入证件号"));
  }

  callback();
};
// 获取表单实例
const shimingFormRef = ref(null);

// 表单验证规则
const shimingRules = reactive({
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  idCard: [{ required: true, validator: validateIdCard, trigger: "blur" }],
  idCardFront: [{ required: true, message: "请输入微信账号", trigger: "blur" }],
  idCardContrary: [
    { required: true, message: "请上传证件背面照", trigger: "change" },
  ],
});
// 是否存在实名信息
const dialogVisible = ref(false);

const shiming = () => {
  shimingFormRef.value?.validate((valid) => {
    if (valid) {
      const params = JSON.parse(JSON.stringify(shimingform.value));
      params.idCard = encrypt(params.idCard);
      if (shimingform.value.idCardType == "1") {
        saveAuthInfo(params).then((res) => {
          // 从新获取用户信息
          if (res.code == 200) {
            //     usergetinfo();
            if (res.data == 2) {
              dialogVisible.value = true;
            } else {
              location.reload();
            }

          }
        });
      } else {
        taskpcrealName(params).then((res) => {
          // 从新获取用户信息
          if (res.code == 200) {
            ElMessage.success("提交成功");
            usergetinfo();
          } else {
            ElMessage.error("实名认证失败");
          }
        });
      }
    }
  });
};
const handleSubmit = () => {
  dialogVisible.value = false;
  logout()
    .then(() => {
      pcLogout();
    })
    .catch(() => {
      pcLogout();
    });
};
const pcLogout = () => {
  removeToken();
  localStorage.clear();
  sessionStorage.clear();
  Cookies.remove("Path");
  Cookies.remove("authinfo");
  setTimeout(() => {
    console.log("退出登录");
    location.reload();
  }, 1000);
};
onMounted(() => {
  nextTick(() => {
    form.value = JSON.parse(sss);
    chuchun.value = JSON.parse(sss);
  });
  xuanzhong.value = route.query.classification;
  usergetinfo();
});
</script>



<style scoped lang="scss">
:deep(.el-card__body) {
  padding: 0;
}

.bg {
  width: 100%;
  background: #f0f0f0;
}

.centerlefttx {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 30px 0 0 20px;
}

.zhangsan {
  font-weight: bold;
  font-size: 20px;
  color: #333333;
  line-height: 29px;
  text-align: left;
  font-style: normal;
  text-transform: none;
  text-indent: 15px;
  width: 150px;
  white-space: nowrap; /* 禁止换行 */
  overflow: hidden; /* 隐藏溢出内容 */
  text-overflow: ellipsis; /* 溢出部分显示省略号 */
}

.centerleft {
  width: 100%;
  text-align: center;
  height: 60px;
  line-height: 60px;
  color: #000;
  cursor: pointer;
  font-weight: 400;
  font-size: 20px;
  color: #333333;
  text-align: center;
  font-style: normal;
  text-transform: none;
}

.centerleft:hover {
  background: #eaeaea;
}

.centerleftxuanzhong {
  width: 100%;
  text-align: center;
  height: 60px;
  line-height: 60px;
  color: #fff;
  background: #3169f8;
  opacity: 0.6;
  font-weight: 400;
  font-size: 20px;
  color: #ffffff;
  text-align: center;
  font-style: normal;
  text-transform: none;
}

.card {
  width: 100%;
  margin-top: 40px;
  padding: 0;
  min-height: 700px;
  margin-bottom: 30px;
}

.titlexiang {
  width: 100%;
  height: 60px;
  border-bottom: 1px solid #eaeaea;
  line-height: 60px;
  text-indent: 40px;
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 20px;
  color: #333333;
  line-height: 60px;
  font-style: normal;
  text-transform: none;
}

.sctx {
  border: 0;
  color: #0769c5;
}

.meirong {
  padding: 30px;
}

:deep(.avatar-item .el-form-item__label) {
  line-height: 100px;
}

.erweima {
}

.shenfen {
  width: 1000px;
  display: flex;
  justify-content: space-around;

  .shenfen-item {
    width: 220px;
    border: 1px solid #eaeaea;
    padding: 20px 30px;
    border-radius: 10px;

    .shenfen-title {
      font-size: 16px;
      color: #000;
    }

    .shenfen-content {
      font-size: 12px;
      color: #999;
    }
  }
}

.gerenxinxi {
  font-size: 16px;
  color: #000;
  margin-top: 20px;
  margin-bottom: 20px;
  background: #e9e6e6;
  padding: 10px 0 15px 30px;
}

.ipth50 {
  height: 50px;
}
.toux {
  width: 70px;
  height: 70px;
  border-radius: 50%;
}
.remzheng {
  ont-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 24px;
  color: #333333;
  line-height: 34px;
  text-align: center;
  font-style: normal;
  text-transform: none;
  margin: 30px 0;
}
.shiming {
  width: 300px;
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 18px;
  color: #333333;
  line-height: 25px;
  text-align: left;
  font-style: normal;
  text-transform: none;
  margin-top: 20px;
  .lable {
    width: 100px;
    float: left;
    text-align: end;
  }
}

:deep(.el-form-item--label-right .el-form-item__label) {
  height: 50px;
  line-height: 50px;
}
:deep(.el-select__wrapper) {
  height: 50px;
}
.shangchuan {
  width: 80px;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid #a9a5a5;
  margin: 20px;
  font-size: 30px;
}
.cardleft {
  width: 300px;
  border-right: 1px solid #eaeaea;
  padding: 0;
}
.shimingrenzheng {
  width: 15px;
  height: 15px;
  position: absolute;
  top: 20px;
  right: 40px;
}
.zongkuan {
  width: calc(100% - 200px);
}
.w600 {
  width: 600px;
}
.but {
  width: 120px;
  height: 40px;
}
.touxtup {
  width: 100px;

  margin: 20px;
}
.renzhengtup {
  width: 300px;
  margin-top: 60px;
}
.renzhengtupimg {
  width: 100px;
  margin-left: 100px;
}
</style>