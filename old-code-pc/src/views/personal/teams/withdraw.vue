<!-- 退赛 -->
<template>
  <div class="tuifei-btn">
  
    <div v-if="queryParams.retiredAuditInfo" class="audit-info-container">
      <div class="audit-info-item">
        <span class="audit-info-label">退赛原因：</span>
        <span class="audit-info-value">{{
          queryParams.retiredAuditInfo.formData.reason
        }}</span>
      </div>
      <div class="audit-info-item">
        <span class="audit-info-label">附件：</span>
        <!-- <span class="audit-info-value">{{
          queryParams.retiredAuditInfo.formData.fileupload80014 || "没有附件"
        }}</span> -->
        <span
          v-if="
            queryParams.retiredAuditInfo.formData.fileupload80014?.length > 0
          "
        >
          <p
            v-for="(item, index) in queryParams.retiredAuditInfo.formData
              .fileupload80014"
            :key="index"
            @click="downloadOssFile(item.url, item.fileName)"
            style="cursor: pointer; color: #3169f8"
          >
            {{ item.fileName }}
          </p>
          <br />
        </span>
        <span v-else> 没有附件 </span>
      </div>
      <div class="audit-info-item">
        <span class="audit-info-label">审核状态：</span>
        <span
          class="audit-info-value"
          :class="{
            'status-running': queryParams.retiredAuditInfo.status === 'RUNNING',
            'status-rejected':
              queryParams.retiredAuditInfo.status === 'REJECTED',
            'status-completed':
              queryParams.retiredAuditInfo.status === 'COMPLETED',
          }"
        >
          {{
            shenhe.find(
              (item) => item.value == queryParams.retiredAuditInfo.status
            )?.label || "未知"
          }}
        </span>
      </div>
      <div
        v-for="(x, i) in queryParams.retiredAuditInfo.approvalHistory"
        :key="i"
      >
        <div
          v-if="x.status == 'REJECTED' && x.comments.length > 0"
          class="audit-info-item"
        >
          <span class="audit-info-label">审核意见：</span>
          <span class="audit-info-value">
            <span v-for="(xx, ii) in x.comments" :key="ii">
              {{ xx }}
            </span>
          </span>
        </div>
      </div>
    </div>
    <div v-if="isshow && queryParams.orderPayFlag ==0">
      <el-button
        v-if="
          queryParams.retiredAuditInfo?.status === 'REJECTED' ||
          !queryParams.retiredAuditInfo
        "
        type="primary"
        size="mini"
        @click="huoqliucheng"
        :disabled="
          !queryParams.flag ||
          !isCurrentTimeInRange(hintText1.jsonallowedTimeRanges)
        "
        class="tuisai-btn"
        >开始申请</el-button
      >
    </div>
      <div
      v-if="!isCurrentTimeInRange(hintText1.jsonallowedTimeRanges)"
      style="margin-top: 10px; margin-right: 10px"
    >
      <el-alert type="primary" show-icon>
        <div>
          <div>当前时间未处于可修改时间，可修改时间为：</div>
          <div>
            <p v-for="(x, i) in hintText1?.jsonallowedTimeRanges" :key="i">
              {{ x.start }}至{{ x.end }}
            </p>
          </div>
        </div>
      </el-alert>
    </div>
    <div v-else-if="!queryParams.flag" style="margin-top: 0.625rem">
      <el-alert type="warning" show-icon @close="isxiugaizubie = ''">
        当前正在处理退费,流程结束后再操作
      </el-alert>
    </div>
    <el-dialog
      v-model="tusaiVisible"
      title="提示信息"
      width="1000"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="ql-container ql-snow">
        <div class="rich-content ql-editor" v-html="xiangqing.hintText2"></div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="quxiaotuifei">取消</el-button>
          <el-button
            type="primary"
            @click="tuisai"
            :disabled="xiangqing.forceReadSeconds > 0"
            :loading="grouploading"
            class="search-btn"
          >
            {{
              xiangqing.forceReadSeconds > 0
                ? xiangqing.forceReadSeconds + "秒后可提交"
                : "我已阅读，确认提交"
            }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { exportPresignedUrl } from "@/api/personal/myfile.js";
import { workflowstartProcess } from "@/api/team";
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();
const props = defineProps({
  queryParams: {
    type: Object,
    default: () => {},
  },
  hintText1: {
    type: String,
    default: "",
  },
  isshow: {
    type: Boolean,
    default: false,
  },
});
console.log(props.hintText1, 123456);
const grouploading=ref(false)
const isCurrentTimeInRange = (timeRangesStr) => {
  if (!timeRangesStr) return true;
  try {
    const timeRanges = timeRangesStr;
    if (!Array.isArray(timeRanges) || timeRanges.length === 0) return false;

    const currentTime = new Date();
    for (const range of timeRanges) {
      if (range.start && range.end) {
        const startTime = new Date(range.start);
        const endTime = new Date(range.end);
        if (currentTime >= startTime && currentTime <= endTime) {
          return true;
        }
      }
    }
    return false;
  } catch (error) {
    console.error("解析时间范围失败:", error);
    return false;
  }
};
const xiangqing = ref(null);
const shenhe = ref([
  {
    label: "审核中",
    value: "RUNNING",
  },
  {
    label: "驳回",
    value: "REJECTED",
  },
  {
    label: "通过",
    value: "COMPLETED",
  },
]);
const qiangzhitime = ref(null);
const huoqliucheng = () => {
  if (!props.queryParams.flag) {
    return;
  }

  xiangqing.value = JSON.parse(JSON.stringify(props.hintText1 || {}));
  if (xiangqing.value.hintText2 == "" || xiangqing.value.hintText2 == null) {
    tuisai();
    return;
  }
  qiangzhitime.value = setInterval(() => {
    if (xiangqing.value.forceReadSeconds > 0) {
      xiangqing.value.forceReadSeconds--;
    }
    if (xiangqing.value.forceReadSeconds == 0) {
      clearInterval(qiangzhitime.value);
    }
  }, 1000);
  tusaiVisible.value = true;
};
const downloadOssFile = (url, filename) => {
  exportPresignedUrl({ fileKey: url }).then((res) => {
    const ossUrl = res.data;
    // 创建隐藏的链接并触发下载
    const a = document.createElement("a");
    a.style.display = "none";
    a.href = ossUrl;
    document.body.appendChild(a);
    a.click();
    setTimeout(() => {
      document.body.removeChild(a);
    }, 500);
  });
};
const tuisai = () => {
  grouploading.value=true
  const params = {
    category: "retired",
    teamCode: route.query.teamCode,
  };
  workflowstartProcess(params).then((res) => {
    grouploading.value=false
    if (res.code == 200) {
      localStorage.setItem("DataSet", JSON.stringify(props.queryParams));
      router.push({
        path: `/workflow/process/start/${res.data.deploymentId}`,
        query: {
          definitionId: res.data.definitionId,
        },
      });
    }
  });
};
const tusaiVisible = ref(false);
const quxiaotuifei = () => {
   tusaiVisible.value = false;
};
</script>

<style scoped lang="scss">
.tuifei-btn {
  margin-left: 1.25rem;
  display: flex;
  flex-direction: column;
  height: 300px;
}

.audit-info-container {
  width: 100%;
}

.audit-info-item {
  display: flex;
  margin-bottom: 10px;
}

.audit-info-label {
  width: 100px;
  text-align: right;
  margin-right: 10px;
  font-weight: 500;
}

.audit-info-value {
  flex: 1;
  text-align: left;
  overflow: auto;
  max-height: 100px;
}

.status-running {
  color: #ffc400;
  font-weight: 500;
}

.status-rejected {
  color: #f56c6c;
  font-weight: 500;
}

.status-completed {
  color: #67c23a;
  font-weight: 500;
}
.tuisai-btn {
  width: 12.5rem;
  height: 3.125rem;
  background: #3169f8;
  border-radius: 0.375rem 0.375rem 0.375rem 0.375rem;
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 1rem;
  color: #ffffff;
  line-height: 1.375rem;
  text-align: center;
  font-style: normal;
  text-transform: none;
}
.search-btn {
  background: #3169f8;
  border-radius: 0.375rem 0.375rem 0.375rem 0.375rem;
  border: 0;
}
</style>