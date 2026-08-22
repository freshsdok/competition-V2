<template>
  <div v-if="ssss">
    <!-- {{ shangchuanxinxi.worksFlag }}
    --
    {{ shangchuanxinxi.worksSubmitFlag }}
    {{
      shangchuanxinxi.worksFlag == 1 || shangchuanxinxi.worksSubmitFlag
        ? "x"
        : ","
    }} -->
    <el-form
      ref="zuopinFormRef"
      :model="zuopiin"
      :rules="zuopinRules"
      label-width="100px"
      style="width: 500px"
      :disabled="
        shangchuanxinxi.worksFlag == 1 ||
        shangchuanxinxi.worksSubmitFlag ||
        !zuopiin.stageName ||
        !zuopiin.stageId
      "
    >
      <el-form-item label="当前阶段" class="avataritemss">
        <el-input v-model="zuopiin.stageName" disabled />
      </el-form-item>
      <el-form-item label="赛事赛道名称" class="avataritemss">
        <el-select
          v-model="zuopiin.competitionTrackName"
          placeholder="请选择赛事赛道名称"
          clearable
          disabled
        >
          <el-option
            v-for="dict in competition_track"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="赛事组别" class="avataritemss">
        <el-select
          v-model="zuopiin.groupClassify"
          placeholder="请选择赛事组别"
          clearable
          disabled
        >
          <el-option
            v-for="dict in competition_group"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="作品名称" prop="worksName" class="avataritemss">
        <el-input v-model="zuopiin.worksName" placeholder="请输入作品名称" />
      </el-form-item>

      <el-form-item label="作品说明" prop="worksDesc" class="avataritemss">
        <el-input
          v-model="zuopiin.worksDesc"
          type="textarea"
          placeholder="请输入作品说明（可选）"
          :rows="3"
        />
      </el-form-item>
      <el-form-item label="上传" prop="worksUrl" class="avataritemss">
        <el-upload
          ref="uploadRef"
          :accept="zuopinlist.worksFormat"
          :headers="upload.headers"
          :limit="1"
          :action="upload.url"
          :on-error="handleFileError"
          :on-success="onUploadSuccess"
          :on-exceed="handleExceed"
          :file-list="fileList"
          drag
          
          :auto-upload="true"

        >
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <el-button class="sctx">上传作品</el-button>
          <template #tip>
            <span>仅允许导入 {{ zuopinlist.worksFormat }} 格式文件。</span
            ><br />
            <span>文件大小不超过 {{ zuopinlist.worksFormatSize }} MB。</span>
          </template>
        </el-upload>
      </el-form-item>
    </el-form>
    <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
      <el-tab-pane
        :label="item.stageName"
        :name="item.stageId"
        v-for="(item, index) in lishizuopin"
        :key="index"
      >
        <el-form :model="item" label-width="100px" style="width: 500px">
          <el-form-item label="作品名称" class="avataritemss">
            <el-input v-model="item.worksName" disabled />
          </el-form-item>
          <el-form-item label="作品说明" class="avataritemss">
            <el-input v-model="item.worksDesc" disabled />
          </el-form-item>
          <el-form-item label="作品" class="avataritemss">
            <el-button
              type="primary"
              @click="xiazaizuopin(item)"
              :disabled="false"
            >
              点击下载
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
    <div
      style="height: 50px"
      v-if="
        shangchuanxinxi.worksFlag == 0 &&
        !shangchuanxinxi.worksSubmitFlag &&
        zuopiin.stageName &&
        zuopiin.stageId
      "
    >
      <el-button type="primary" @click="tijiaozuopin" style="float: right">
        确定
      </el-button>
    </div>
  </div>
</template>
<script setup>
import { saveCompetitionWorks } from "@/api/index";
import { getToken } from "@/utils/auth";
import { ElMessage, genFileId } from "element-plus";
import { onMounted, ref } from "vue";
import { competitionWorks } from "@/api/personal/index";
const props = defineProps({
  shangchuanxinxi: {
    type: Object,
    default: () => {},
  },
});
const { proxy } = getCurrentInstance();
const { competition_track, competition_group } = proxy.useDict(
  "competition_track",
  "competition_group"
);
console.log(props.shangchuanxinxi, 123456);
const ssss = ref(false);
const fileList = ref([]);
// 关闭弹窗sczuopin
const emits = defineEmits(["sczuopin"]);
const handleClick = () => {};
const { shangchuanxinxi } = toRefs(props);
const zuopiin = ref({
  competitionSeriesId: "", //竞赛id
  worksName: "", //作品名称
  worksUrl: "", //作品url
  teamCode: "", //团队code
  worksDesc: "", //作品说明
});

//作品的格式类型
const zuopinlist = ref({});
//历史阶段内容
const lishizuopin = ref([]);
const activeName = ref([]);

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
  url:
    import.meta.env.VITE_APP_BASE_API +
    `/competition/competitionWorks/uploadCompetitionWorks/${shangchuanxinxi.value.competitionSeriesId}`,
});

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

/** 文件上传失败 */
const handleFileError = (error, file, fileList) => {
  ElMessage.error("上传失败");
};
const onUploadSuccess = (response, file) => {
  if (response.code == 200) {
    zuopiin.value.worksUrl = response.data.url;
    ElMessage.success("上传成功");
  } else {
    ElMessage.error(response.msg);
  }
};

// 下载作品
const xiazaizuopin = (item) => {
  proxy.downloadJS(
    import.meta.env.VITE_APP_BASE_API +
      `file/common/download?resource=${item.worksUrl}`,
    `${item.worksName}.${item.worksUrl.split(".").at(-1)}`
  );
};

// 自定义校验：检查 worksUrl 是否存在（表示已上传成功）
const validateWorksUrl = (rule, value, callback) => {
  if (!zuopiin.value.worksUrl) {
    callback(new Error("请先上传作品文件"));
  } else {
    callback();
  }
};

// 表单验证规则
const zuopinRules = reactive({
  worksName: [
    { required: true, message: "作品名称不能为空", trigger: "blur" },
    { max: 100, message: "作品名称不能超过100个字符", trigger: "blur" },
  ],
  worksDesc: [
    // 如果作品说明是可选的，可以不加 required；如需必填，加上：
    // { required: true, message: '作品说明不能为空', trigger: 'blur' },
    { max: 200, message: "作品说明不能超过200字", trigger: "blur" },
  ],
  worksUrl: [{ validator: validateWorksUrl, trigger: "blur" }],
});
const zuopinFormRef = ref(null);
// 提交
const tijiaozuopin = () => {
  console.log(zuopiin.value);

  zuopinFormRef.value?.validate((valid) => {
    if (valid) {
      zuopiin.value.competitionSeriesId = zuopinlist.value.competitionSeriesId;
      zuopiin.value.teamCode = zuopinlist.value.teamCode;
      saveCompetitionWorks(zuopiin.value).then((res) => {
        if (res.code == 200) {
          ElMessage.success("提交成功");
          emits("sczuopin", false);
        } else {
          ElMessage.error(res.msg);
        }
      });
    }
  });
};
const getlist = () => {
  const params = {
    competitionSeriesId: shangchuanxinxi.value.competitionSeriesId,
  };
  competitionWorks(params)
    .then((res) => {
      // 当前阶段名称阶段id
      zuopiin.value.stageName = shangchuanxinxi.value.stageName;
      zuopiin.value.stageId = shangchuanxinxi.value.stageId;
      zuopiin.value.groupClassify = shangchuanxinxi.value.groupClassify;
      zuopiin.value.competitionTrackName =
        shangchuanxinxi.value.competitionTrackName;
      lishizuopin.value = res.data;

      zuopinlist.value = JSON.parse(JSON.stringify(shangchuanxinxi.value));

      zuopinlist.value.worksFormat = zuopinlist.value.worksFormat
        .split(",") // 拆分成数组 ['zip', 'docx', 'doc', 'png']
        .map((ext) => "." + ext) // 每个元素前面加点
        .join(",");

      // 历史记录的默认值
      activeName.value = lishizuopin?.value[0]?.stageId;
    })
    .then(() => {
      ssss.value = true;
    });
};
onMounted(() => {
  getlist();
});
</script>


<style scoped>
</style>