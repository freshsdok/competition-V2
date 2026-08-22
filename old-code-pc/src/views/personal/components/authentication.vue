<template>
  <div class="" v-if="chuchun.authStatus == 5">
    <div class="shenfen">
      <div
        v-for="(item, index) in shenfenlist"
        :key="index"
        class="shenfen-item-container"
      >
        <div
          class="shenfen-item"
          @click="xuanzhong(item.value)"
          :class="{
            'shenfen-itemnnn': xuanzhongxiang == item.value,
            // 'tongguo': item.checkStatus == 6,
            // 'tongguoout': item.checkStatus != 6,
          }"
        >
          <div class="shenfen-title">
            {{ item.title }}
          </div>
          <div style="display: flex; align-items: center">
            <img
              :src="item.checkStatus == 6 ? baomingchenggong : tanhao"
              alt=""
              class="xiaotub"
              style="margin-right: 10px"
            />
            {{
              identity_status.find((xx) => {
                return xx.value == item.checkStatus;
              })
                ? identity_status.find((xx) => {
                    return xx.value == item.checkStatus;
                  }).label
                : "未认证"
            }}
          </div>
          <div class="shenfen-content">{{ item.content }}</div>
        </div>
      </div>
    </div>
    <div class="gerenxinxi">个人信息</div>

    <div>
      <el-form :model="form" label-width="180px">
        <el-row :gutter="24" style="width: 100%">
          <el-form-item label="姓名">
            {{ chuchun.authInfo?.realName }}
          </el-form-item>
          <el-form-item
            :label="
              document_type.find(
                (item) => item.value == chuchun.authInfo.idCardType
              )?.label
            "
          >
            {{ decrypt(chuchun.authInfo?.idCard) }}
            <img
              :src="baomingchenggong"
              alt=""
              class="xiaotub"
              style="margin-left: 10px"
            />
          </el-form-item>
        </el-row>
      </el-form>
    </div>
    <div class="gerenxinxi">认证信息</div>
    <!-- <div v-if="form.checkStatus == 2">
      <div style="">
        <img
          src="@/assets/images/shenhe.png"
          style="width: 100px; margin-left: calc(50% - 50px)"
          alt=""
        />
        <p style="text-align: center; margin-top: 30px">审核中</p>
      </div>
    </div> -->
    <div>
      <el-form
        :model="form"
        label-width="180px"
        class="w800"
        ref="formRef"
        :rules="currentRules"
        :disabled="
          form.checkStatus == 6 ||
          form.checkStatus == 2 ||
          form.checkStatus == 3
        "
      >
        <el-form-item
          label="驳回意见:"
          class="avataritemss"
          prop="specialty"
          v-if="form.checkStatus == 7 && form.refusalReasons"
        >
          <span style="color: red; font-size: 18px">
            {{ form.refusalReasons }}
          </span>
        </el-form-item>
        <template v-if="xuanzhongxiang == 'student'">
          <el-form-item label="所在学校" class="avataritemss" prop="school">
            <el-select
              v-model="form.school"
              filterable
              remote
              reserve-keyword
              placeholder="请选择所在学校"
              remote-show-suffix
              :remote-method="remoteMethod"
              :loading="loading"
              @change="getTeacherslist"
            >
              <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>

            <!-- <div class="fankui" @click="fankui"> -->
            <div @click="fankui" style="cursor: pointer" v-if="!form.school">
              <img
                src="@/assets/images/xueshengrenzheng.png"
                class="xuexiaoicon"
                alt=""
              />
              <div style="display: inline-block">所在学校暂无，立即反馈</div>
            </div>
          </el-form-item>

          <el-form-item label="我的专业" class="avataritemss" prop="specialty">
            <el-select
              v-model="form.specialty"
              filterable
              remote
              reserve-keyword
              placeholder="请选择我的专业"
              remote-show-suffix
              :remote-method="specialtyremoteMethod"
              :loading="loading"
            >
              <el-option
                v-for="item in specialtyoptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label="入学年份"
            class="avataritemss"
            prop="enrollmentYear"
          >
            <el-date-picker
              class="avataritemss"
              v-model="form.enrollmentYear"
              type="year"
              :disabled-date="disabledYear"
              placeholder="请选择入学年份"
              value-format="YYYY"
            />
          </el-form-item>
          <el-form-item label="我的学号" class="avataritemss">
            <el-input
              v-model="form.employeeCode"
              class="elinput"
              placeholder="请输入我的学号"
              maxlength="30"
              show-word-limit
            />
          </el-form-item>
          <el-form-item
            label="上传学生证"
            class="avatar-item"
            prop="studentCardId"
          >
            <el-upload
              ref="uploadRef"
              :headers="upload.headers"
              :limit="1"
              :action="upload.url"
              :show-file-list="false"
              :file-list="fileList"
              :on-error="handleFileError"
              :on-exceed="handleExceed"
              :on-success="onUploadSuccess"
              :before-upload="beforeUpload"
              accept=".jpg,.jpeg,.png"
            >
              <img
                v-if="form.studentCardId"
                :src="form.studentCardId ? form.studentCardId : img1"
                class="toux"
                alt=""
              />

              <div class="shangchuan" v-else>
                <el-icon><Plus /></el-icon>
              </div>
              <!-- <el-button class="sctx">上传学生证</el-button> -->
              <div class="tupgeshi">
                照片仅支持 jpg、 jpeg 或 png 格式，大小不超过 1M
              </div>
            </el-upload>
          </el-form-item>
          <el-form-item label="是否留学生" class="avataritemss">
            <el-radio-group v-model="form.isForeignStudent" disabled>
              <el-radio value="Y">是</el-radio>
              <el-radio value="N">否</el-radio>
            </el-radio-group>
            <span class="isForeignStudent" v-if="form.isForeignStudent == 'Y'">
              您选择的所在学校为中国大学，默认您为来华外国留学生身份。如果要修改为非留学生身份，请选择非中国大学。</span
            >
          </el-form-item>
          <el-form-item label="带队老师" class="avataritemss" prop="teamLeader">
            <el-select
              v-model="form.teamLeader"
              placeholder="请选择带队老师"
              clearable
            >
              <el-option
                v-for="item in zhidaolaoshi"
                :key="item.value"
                :label="item.realName ? item.realName : item.nickName"
                :value="item.userId"
              />
            </el-select>
            <div class="ddlsbeizhu" v-if="!form.teamLeader">
              如果没有可选的带队老师，请联系本学校带队老师，注册并完成教师身份认证
            </div>
          </el-form-item>
        </template>
        <template v-if="xuanzhongxiang == 'teacher'">
          
   
          <el-form-item label="所在学校" class="avataritemss" prop="school">
             <span v-if=" ['2', '3', '6'].indexOf(form.checkStatus) != -1">{{
              options.find((item) => item.value == form.school)?.label
            }}</span>
            <el-select
              v-model="form.school"
              filterable
              remote
              reserve-keyword
              placeholder="请选择所在学校"
              remote-show-suffix
              :remote-method="remoteMethod"
              :loading="loading"
              v-else
            >
              <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
           
            <div @click="fankui" style="cursor: pointer" v-if="!form.school">
              <img
                src="@/assets/images/xueshengrenzheng.png"
                class="xuexiaoicon"
                alt=""
              />
              <div style="display: inline-block">所在学校暂无，立即反馈</div>
            </div>
          </el-form-item>
          <el-form-item label="所在学院" class="avataritemss" prop="institute">
             <span v-if=" ['2', '3', '6'].indexOf(form.checkStatus) != -1">{{ form.institute }}</span>
            <el-input
              v-model="form.institute"
              class="elinput"
              placeholder="请输入所在学院"
              maxlength="30"
              show-word-limit
              v-else
            />
           
          </el-form-item>
          <el-form-item label="我的职位" class="avataritemss" prop="position">
              <span  v-if=" ['2', '3', '6'].indexOf(form.checkStatus) != -1">{{ form.position }}</span>
            <el-input
              v-model="form.position"
              placeholder="请输入我的职位"
              class="elinput"
              maxlength="30"
              show-word-limit
             v-else
            />
          
          </el-form-item>
          <el-form-item
            label="上传工作证"
            class="avatar-item"
            prop="workCardUrl"
          >
            <el-upload
              ref="uploadRef"
              :headers="upload.headers"
              :limit="1"
              :action="upload.url"
              :show-file-list="false"
              :file-list="fileList"
              :on-error="handleFileError"
              :on-exceed="handleExceed"
              :on-success="onUploadSuccess"
              :before-upload="beforeUpload"
              accept=".jpg,.jpeg,.png"
            >
              <img
                v-if="form.workCardUrl"
                :src="form.workCardUrl ? form.workCardUrl : img1"
                class="toux"
                alt=""
              />
              <div class="shangchuan" v-else>
                <el-icon><Plus /></el-icon>
              </div>

              <!-- <el-button class="sctx">上传工作证</el-button> -->
              <div class="tupgeshi">
                照片仅支持 jpg 、jpeg 或 png 格式，大小不超过 1M
              </div>
            </el-upload>
          </el-form-item>
        </template>

        <template v-if="xuanzhongxiang == 'school'">
          <el-form-item label="学校全称" class="avataritemss" prop="school">
            <el-input
              v-model="form.school"
              class="elinput"
              placeholder="请输入学校全称"
              maxlength="30"
              show-word-limit
            />
          </el-form-item>
          <el-form-item
            label="审批文号"
            class="avataritemss"
            prop="apprDocNumber"
          >
            <el-input
              v-model="form.apprDocNumber"
              class="elinput"
              placeholder="请输入审批文号"
              maxlength="30"
              show-word-limit
            />
          </el-form-item>
          <el-form-item
            label="统一社会信用代码"
            class="avataritemss"
            prop="creditIdent"
          >
            <el-input
              v-model="form.creditIdent"
              class="elinput"
              maxlength="30"
              show-word-limit
              placeholder="请输入统一社会信用代码"
            />
          </el-form-item>
          <el-form-item
            label="学校资质备案证书"
            class="avatar-item"
            prop="schoolCertUrl"
          >
            <el-upload
              ref="uploadRef"
              :headers="upload.headers"
              :limit="1"
              :action="upload.url"
              :show-file-list="false"
              :file-list="fileList"
              :on-error="handleFileError"
              :on-exceed="handleExceed"
              :on-success="onUploadSuccess"
              :before-upload="beforeUpload"
              accept=".jpg,.jpeg,.png"
            >
              <img
                v-if="form.schoolCertUrl"
                :src="form.schoolCertUrl ? form.schoolCertUrl : img1"
                class="toux"
                alt=""
              />
              <div class="shangchuan" v-else>
                <el-icon><Plus /></el-icon>
              </div>

              <!-- <el-button class="sctx">上传学校资质备案证书</el-button> -->
              <div class="tupgeshi">
                照片仅支持 jpg 、jpeg 或 png 格式，大小不超过 1M
              </div>
            </el-upload>
          </el-form-item>
        </template>
        <template v-if="xuanzhongxiang == 'enterprise'">
          <el-form-item
            label="公司名称"
            class="avataritemss"
            prop="companyName"
          >
            <el-input
              v-model="form.companyName"
              class="elinput"
              maxlength="30"
              placeholder="请输入公司名称"
              show-word-limit
            />
          </el-form-item>
          <el-form-item
            label="营业执照号"
            class="avataritemss"
            prop="bussLicenseNum"
          >
            <el-input
              v-model="form.bussLicenseNum"
              class="elinput"
              maxlength="30"
              placeholder="请输入营业执照号"
              show-word-limit
            />
          </el-form-item>
          <el-form-item
            label="法人姓名"
            class="avataritemss"
            prop="legalPersName"
          >
            <el-input
              v-model="form.legalPersName"
              class="elinput"
              placeholder="请输入法人姓名"
              maxlength="10"
              show-word-limit
            />
          </el-form-item>
          <el-form-item
            label="法人身份证号"
            class="avataritemss"
            prop="legalIdCard"
          >
            <el-input
              v-model="form.legalIdCard"
              class="elinput"
              placeholder="请输入法人身份证号"
              maxlength="18"
              show-word-limit
            />
          </el-form-item>
          <el-form-item
            label="上传营业执照"
            class="avatar-item"
            prop="bussLicenseUrl"
          >
            <el-upload
              ref="uploadRef"
              :headers="upload.headers"
              :limit="1"
              :action="upload.url"
              :file-list="fileList"
              :show-file-list="false"
              :on-error="handleFileError"
              :on-exceed="handleExceed"
              :on-success="onUploadSuccess"
              :before-upload="beforeUpload"
              accept=".jpg,.jpeg,.png"
            >
              <img
                v-if="form.bussLicenseUrl"
                :src="form.bussLicenseUrl ? form.bussLicenseUrl : img1"
                class="toux"
                alt=""
              />
              <div class="shangchuan" v-else>
                <el-icon><Plus /></el-icon>
              </div>
              <!-- <el-button class="sctx">上传营业执照</el-button> -->
              <div class="tupgeshi">
                照片仅支持 jpg、 jpeg 或 png 格式，大小不超过 1M
              </div>
            </el-upload>
          </el-form-item>
        </template>
      </el-form>
      <div class="marleft" v-if="form.checkStatus != 3">
        <el-button
          v-if="form.checkStatus != 6 && form.checkStatus != 2"
          type="primary"
          @click="jiaoshitanchaung"
          class="but"
          :loading="submitting"
          :disabled="submitting || saveDisabled"
          >保存</el-button
        >
        <el-button
          v-if="form.checkStatus == 6"
          type="primary"
          @click="handleReauth"
          class="but"
          :loading="reauthDisabled"
          :disabled="reauthDisabled"
          >重新认证</el-button
        >
      </div>
    </div>
    <el-dialog
      title="带队教师须知"
      v-model="jiaoshitanchaungopen"
      width="1200px"
    >
      <div class="notice-content">
        <h3>教师认证用户须知</h3>
        <p>
          本须知为大赛带队教师认证的核心确认事项，带队教师完成认证即视为已充分阅读、理解并自愿遵守本须知全部条款，承担相应责任与义务。
        </p>

        <h4>一、信息填报与赛事贯宣责任</h4>
        <p>
          带队教师作为学生参赛的第一责任人，须严格秉持诚信原则，确保所收集并填报的学生信息真实、准确、完整，严禁伪造、篡改学生身份信息、学历信息及其他参赛相关资料。同时，带队教师应主动查阅大赛官网发布的赛事章程、报名须知、竞赛规则等官方文档，以线上宣讲、线下培训等有效方式，向参赛学生完整贯宣赛事相关要求，确保学生充分理解赛事流程、竞赛规范。
        </p>

        <h4>二、竞赛组织责任</h4>
        <p>
          带队教师须切实担负起竞赛组织管理职责，严格遵照大赛组委会的统一赛事安排，统筹推进学生参赛的各项组织工作。在集中竞赛阶段，带队教师应负责在本校指定地点组织学生集中参赛，履行监考职责，维护考场纪律，同时保障竞赛场地网络环境稳定畅通，提前检查学生参赛设备状态。此外，带队教师需在竞赛开始前再次向学生贯宣考试操作要求、纪律要求及应急处理流程，确保竞赛工作安全、有序、规范开展。
        </p>

        <h4>三、认证资格</h4>
        <p>
          <strong>认证主体范围：</strong>仅限具备以下条件之一的人员申请认证：①
          持有有效教师资格证的专业教师；②
          学校或学院指定负责竞赛组织工作的教职工；③ 承担大学生管理工作的辅导员。
        </p>
        <p>
          <strong>集中报名特殊要求：</strong
          >若学校或学院内部已明确要求竞赛报名工作由竞赛处统一收口、集中办理，则仅限竞赛处指定教师申请认证，其他人员不得擅自申请。
        </p>
        <p>
          <strong>禁止认证情形：</strong
          >学生不得申请带队教师认证，学生直接提交的认证申请将予以驳回。
        </p>
      </div>
      <div class="action-btns">
        <el-button type="danger" @click="jiaoshitanchaungopen = false"
          >取消</el-button
        >
        <el-button type="primary" @click="onSubmit" :disabled="submitDisabled">
          {{
            submitDisabled ? `请仔细阅读须知（${submitCountdown}秒）` : "提交"
          }}
        </el-button>
      </div>
    </el-dialog>
  </div>
  <div v-else style="padding-top: 60px">
    <img :src="weirenz" alt="" class="renzhengimg" />
    <div class="fsize20">
      尚未通过实名认证<span
        style="color: rgb(35 56 217); font-weight: 500; cursor: pointer"
        @click="shimingrenzhengrout"
        >去认证</span
      >
    </div>
  </div>
</template>

<script setup>
import {
  listpersonalCenter,
  updateIdentityInfo,
  saveIdentityInfo,
  schoollist,
  disciplinelist,
  personalCentergetTeachers,
} from "@/api/accountmanagement";
import baomingchenggong from "@/assets/images/baomingchenggong.png";
import tanhao from "@/assets/images/tanhao.png";
import { encrypt, decrypt } from "@/utils/jsencrypt.js";
import { getToken } from "@/utils/auth";
import { ElMessage, genFileId } from "element-plus";
import img1 from "@/assets/images/addshangc.png";
import { useRouter } from "vue-router";
import weirenz from "@/assets/icon/weirenzheng.png";
const router = useRouter();
const { proxy } = getCurrentInstance();
const {
  certification_type,
  identity_status,
  real_name_auth_status,
  document_type,
} = proxy.useDict(
  "certification_type",
  "identity_status",
  "real_name_auth_status",
  "document_type"
);
const shimingrenzhengrout = () => {
  router.push({
    path: "/personal/accountmanagement",
    query: {
      classification: "nameauthentication",
    },
  });
  setTimeout(() => {
    window.location.reload();
  }, 200);
};
const props = defineProps({
  chuchun: {
    type: Object,
    default: () => {},
  },
});
const { chuchun } = toRefs(props);
console.log(chuchun.value, 12345);
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
const onUploadSuccess = (response, file) => {
  if (response.code == 200) {
    if (xuanzhongxiang.value == "student") {
      form.value.studentCardId = response.data.url;
      formRef.value?.validateField("studentCardId");
    } else if (xuanzhongxiang.value == "teacher") {
      form.value.workCardUrl = response.data.url;
      formRef.value?.validateField("workCardUrl");
    } else if (xuanzhongxiang.value == "school") {
      form.value.schoolCertUrl = response.data.url;
      formRef.value?.validateField("schoolCertUrl");
    } else if (xuanzhongxiang.value == "enterprise") {
      form.value.bussLicenseUrl = response.data.url;
      formRef.value?.validateField("bussLicenseUrl");
    }
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

// 文件上传前校验
const beforeUpload = (file) => {
  const allowedTypes = ["image/jpeg", "image/png"];
  const isValidType = allowedTypes.includes(file.type);

  if (!isValidType) {
    ElMessage.error("只能上传JPG、JPEG和PNG格式数据！");
    return false; // 阻止上传
  }
  const maxSize = 1 * 1024 * 1024; // 1MB（单位：字节）
  if (file.size > maxSize) {
    ElMessage.error("上传文件不能超过 1MB！");
    return false; // 阻止上传
  }
  return true; // 允许上传
};

const shenfenlist = ref([
  {
    title: "教师身份认证",
    value: "teacher",
    content: "需上传教师资格证进行认证",
    checkStatus: null,
  },
  // {
  //   title: "学生身份认证",
  //   value: "student",
  //   content: "需上传学生证进行认证",
  //   checkStatus: null,
  // },

  // {
  //   title: "学校认证",
  //   value: "school",
  //   content: "需上传学校资质备案进行认证",
  //   checkStatus: null,
  // },
  // {
  //   title: "企业认证",
  //   value: "enterprise",
  //   content: "需上传企业资质备案进行认证",
  //   checkStatus: null,
  // },
]);
const form = ref({
  school: "",
  specialty: "",
  employeeCode: "",
});

// 通用非空规则
const requiredRule = (msg) => ({
  required: true,
  message: msg,
  trigger: "blur",
});
const requiredSelectRule = (msg) => ({
  required: true,
  message: msg,
  trigger: "change",
});

// 动态规则
const currentRules = computed(() => {
  const rules = {};

  if (xuanzhongxiang.value === "student") {
    rules.school = [requiredRule("请选择所在学校")];
    rules.specialty = [requiredSelectRule("请选择我的专业")];
    rules.enrollmentYear = [requiredSelectRule("请选择入学时间")];
    rules.employeeCode = [requiredRule("请输入我的学号")];
    rules.teamLeader = [requiredRule("请选择带队老师")];
    rules.studentCardId = [
      { required: true, message: "请上传学生证", trigger: "blur" },
    ];
  } else if (xuanzhongxiang.value === "teacher") {
    rules.school = [requiredRule("请选择所在学校")];
    rules.institute = [requiredRule("请输入所在学院")];
    rules.position = [requiredRule("请输入我的职位")];
    rules.workCardUrl = [
      { required: true, message: "请上传工作证", trigger: "blur" },
    ];
  } else if (xuanzhongxiang.value === "school") {
    rules.school = [requiredRule("请输入学校全称")];
    rules.apprDocNumber = [requiredRule("请输入审批文号")];
    rules.creditIdent = [requiredRule("请输入统一社会信用代码")];
    rules.schoolCertUrl = [
      { required: true, message: "请上传学校资质备案证书", trigger: "blur" },
    ];
  } else if (xuanzhongxiang.value === "enterprise") {
    rules.companyName = [requiredRule("请输入公司名称")];
    rules.bussLicenseNum = [requiredRule("请输入营业执照号")];
    rules.legalPersName = [requiredRule("请输入法人姓名")];
    rules.legalIdCard = [
      { required: true, message: "请输入法人身份证号", trigger: "blur" },
      {
        pattern:
          /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/,
        message: "身份证格式不正确",
        trigger: "blur",
      },
    ];
    rules.bussLicenseUrl = [
      { required: true, message: "请上传营业执照", trigger: "blur" },
    ];
  }
  return rules;
});
const zong = ref([]);
const getlist = () => {
  listpersonalCenter().then((res) => {
    console.log(res, 666);
    zong.value = res.data;
    res.data.forEach((item) => {
      if (
        shenfenlist.value.find((xx) => {
          return xx.value == item.certificationType;
        })
      ) {
        shenfenlist.value.find((xx) => {
          return xx.value == item.certificationType;
        }).checkStatus = item.checkStatus;
      }

      if (item.checkStatus == 6) {
        if (xuanzhongxiang.value != "teacher") {
          xuanzhongxiang.value = item.certificationType;
        }
      }
      if (xuanzhongxiang.value == null) {
        if (item.checkStatus == 3) {
          if (xuanzhongxiang.value != "teacher") {
            xuanzhongxiang.value = item.certificationType;
          }
        }
      }
    });

    if (xuanzhongxiang.value == null) {
      xuanzhongxiang.value = "teacher";
    }
    // 通过学校id查询回显所在所学校
    res.data.forEach((item) => {
      if (item.school) {
        const params = {
          id: item.school,
        };
        schoollist(params).then((rr) => {
          rr.data.forEach((xxx) => {
            xxx.label = xxx.schoolName;
            xxx.value = xxx.id;
            if (!options.value.some((item) => item.id == xxx.id)) {
              options.value.push(xxx);
            }
          });
        });
      }
      if (item.certificationType == "student") {
        const param = {
          schoolId: item.school,
        };
        personalCentergetTeachers(param).then((res) => {
          console.log(res);
          zhidaolaoshi.value = res.data;
        });
      }
    });

    form.value = res.data.find((item) => {
      return item.certificationType == xuanzhongxiang.value;
    })
      ? res.data.find((item) => {
          return item.certificationType == xuanzhongxiang.value;
        })
      : {};
  });
};
getlist();
const xuanzhongxiang = ref("teacher");
const xuanzhong = (row) => {
  xuanzhongxiang.value = row;

  form.value = zong.value.find((item) => {
    return item.certificationType == row;
  })
    ? zong.value.find((item) => {
        return item.certificationType == row;
      })
    : {};
};
const formRef = ref(null);
const saveDisabled = ref(false);
const reauthDisabled = ref(false);
const jiaoshitanchaungopen = ref(false);
const submitCountdown = ref(0);
const submitDisabled = ref(false);
let timer = ref(null);
const jiaoshitanchaung = () => {
  formRef.value?.validate((valid) => {
    if (valid) {
      if (xuanzhongxiang.value == "teacher") {
        jiaoshitanchaungopen.value = true;
        submitCountdown.value = 10;
        submitDisabled.value = true;
        if (timer.value) {
          clearInterval(timer.value);
        }
        timer.value = setInterval(() => {
          submitCountdown.value--;
          if (submitCountdown.value <= 0) {
            clearInterval(timer.value);
            submitDisabled.value = false;
          }
        }, 1000);
      } else {
        onSubmit();
      }
    }
  });
};
const onSubmit = () => {
  if (form.value.authId) {
    updateIdentityInfo(form.value).then((res) => {
      ElMessage.success("身份认证已提交，请耐心等待审核");
      // getlist();
      location.reload();
    });
  } else {
    form.value.certificationType = xuanzhongxiang.value;
    saveIdentityInfo(form.value).then((res) => {
      ElMessage.success("身份认证已提交，请耐心等待审核");
      // getlist();
      location.reload();
    });
  }
};
const handleReauth = () => {
  if (reauthDisabled.value) return;
  reauthDisabled.value = true;
  saveDisabled.value = true;
  form.value.checkStatus = 5;
  setTimeout(() => {
    reauthDisabled.value = false;
  }, 500);
  setTimeout(() => {
    saveDisabled.value = false;
  }, 2000);
};
const options = ref([]);
const loading = ref(false);
const remoteMethod = (query) => {
  if (query) {
    loading.value = true;
    const params = {
      schoolName: query,
    };
    schoollist(params).then((res) => {
      loading.value = false;

      res.data.forEach((item) => {
        item.label = item.schoolName;
        item.value = item.id;
      });
      options.value = res.data;
    });
  } else {
    options.value = [];
  }
};
const specialtyoptions = ref([]);
const specialtyloading = ref(false);
const specialtyremoteMethod = (query) => {
  if (query) {
    specialtyloading.value = true;
    const params = {
      minorClass: query,
    };
    disciplinelist(params).then((res) => {
      specialtyloading.value = false;

      res.data.forEach((item) => {
        item.label = item.minorClass;
        item.value = item.minorClass;
      });
      specialtyoptions.value = res.data;
    });
  } else {
    specialtyoptions.value = [];
  }
};
const fankui = () => {
  // router.push({
  //   path: "/feedback",
  // });
  ElMessage.warning("功能暂未开通");
};
const zhidaolaoshi = ref([]);
const getTeacherslist = () => {
  form.value.isForeignStudent =
    chuchun.value.authInfo.countryName == "CN" ? "N" : "Y";
  const params = {
    schoolId: form.value.school,
  };
  personalCentergetTeachers(params).then((res) => {
    console.log(res);
    zhidaolaoshi.value = res.data;
  });
};
// 禁用超过当前年份的选项
const disabledYear = (date) => {
  const currentYear = new Date().getFullYear();
  const year = date.getFullYear();
  return year > currentYear;
};
</script>


<style scoped lang="scss">
.shenfen {
  // width: 1000px;
  display: flex;
  justify-content: flex-start;
  .shenfen-item-container {
    + .shenfen-item-container {
      margin-left: 20px;
      cursor: pointer;
    }
  }
  .shenfen-item {
    width: 220px;
    border: 1px solid #eaeaea;
    padding: 20px 30px;
    border-radius: 10px;
    height: 100px;
    .shenfen-title {
      font-size: 16px;
      color: #000;
    }

    .shenfen-content {
      font-size: 12px;
      color: #999;
      white-space: nowrap; /* 禁止换行 */
      overflow: hidden; /* 隐藏溢出内容 */
      text-overflow: ellipsis; /* 溢出部分用省略号表示 */
    }
  }

  .shenfen-itemnnn {
    border: 1px solid #3b82f6;
  }
  .tongguo {
    background: #51c512;
  }
  .tongguoout {
    background: red;
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

.toux {
  width: 80px;
  height: 80px;
  margin: 20px;
}

.elinput {
  width: 600px;
  height: 50px;
}

:deep(.el-select__wrapper) {
  height: 50px;
  width: 600px;
}
:deep(.el-date-editor.el-input, .el-date-editor.el-input__wrapper) {
  height: 50px;
  width: 600px;
}
.fankui {
  position: absolute;
  left: 620px;
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: bold;
  font-size: 16px;
  color: #3169f8;
  line-height: 50px;
  text-align: left;
  font-style: normal;
  text-transform: none;
  cursor: pointer;
}
.isForeignStudent {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 14px;
  color: #b2b4bb;
  line-height: 20px;
  text-align: left;
  font-style: normal;
  text-transform: none;
  text-indent: 20px;
}
.tupgeshi {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 14px;
  color: #ff8800;
  line-height: 20px;
  text-align: left;
  font-style: normal;
  text-transform: none;
}
.ddlsbeizhu {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 14px;
  color: #b2b4bb;
  line-height: 20px;
  text-align: left;
  font-style: normal;
  text-transform: none;
}
:deep(.el-form-item--label-right .el-form-item__label) {
  height: 50px;
  line-height: 50px;
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
.shimingrenzheng {
  cursor: pointer;
  color: rgb(64, 158, 255);
}
.xiaotub {
  width: 12px;
  height: 12px;
}
.w700 {
  width: 1000px;
}
.w800 {
  width: 800px;
}
.xuexiaoicon {
  width: 16px;
  height: 16px;
  display: inline-block;
}
.but {
  width: 120px;
  height: 40px;
}
.marleft {
  margin-left: 180px;
}
.fsize20 {
  text-align: center;
  font-size: 20px;
}
.renzhengimg {
  margin: 0 auto;
  width: 100px;
}

.notice-content {
  padding: 20px;
  line-height: 1.8;

  h3 {
    font-size: 20px;
    color: #333;
    margin-bottom: 20px;
    text-align: center;
  }

  h4 {
    font-size: 16px;
    color: #333;
    margin: 25px 0 15px 0;
    font-weight: bold;
  }

  p {
    font-size: 14px;
    color: #666;
    margin-bottom: 15px;
    text-indent: 2em;

    &.intro {
      text-indent: 0;
      font-weight: 500;
      color: #333;
    }

    strong {
      color: #333;
      font-weight: bold;
    }
  }
}
.action-btns {
  display: flex;
  justify-content: flex-end;
}
</style>