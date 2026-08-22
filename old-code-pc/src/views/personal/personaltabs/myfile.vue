<template>
  <div class="wenjian">
    <!-- 顶部赛事信息 - 可折叠 -->
    <div v-if="competitionData.length > 0">
      <div
        class="competition-item"
        :class="{ 'has-notification': getNotificationCount(item) > 0 }"
        v-for="(item, index) in competitionData"
        :key="item.id"
      >
        <div
          style="
            display: flex;
            justify-content: space-between;
            align-items: center;
          "
        >
          <div class="competition-title" @click="toggleCompetition(index)">
            <el-icon class="icon" :class="{ rotate: ss[index] }">
              <CaretBottom v-if="ss[index]" />
              <CaretRight v-else />
            </el-icon>

            <el-badge is-dot class="item" v-if="!item.readCountFlag">
              <span class="task-title-text">{{ item.taskName }}</span>
            </el-badge>
            <span v-else class="task-title-text">{{ item.taskName }}</span>
            <span
              v-if="getNotificationCount(item) > 0"
              class="notification-badge"
            >
              通知 {{ formatNotificationCount(item.notificationCount) }}
            </span>
          </div>
        </div>
        <div class="w-full pr-[20px] pl-[20px] mb-2">
          <el-alert type="error"
                  show-icon
                  v-if="alertMsgInfo && alertMsgInfo[item.id] && alertMsgInfo[item.id].submit"
                  @close="closeAlertMsg(item.id)">
          <div>{{ alertMsgInfo[item.id].content }}</div>
        </el-alert>
        </div>
        <div
          style="
            height: 1px;
            background-color: #e4e4e4;
            width: calc(100% - 40px);
            margin: 0 auto;
          "
        ></div>
        <!-- 赛事详情 -->
        <!-- <div
          class="competition-info"
          v-for="(
            RelationList, RelationListindex
          ) in item.sysUserGroupCompetitionRelationList"
          :key="RelationListindex"
        >
          <span class="info-item" v-for="(x, i) in RelationList" :key="i">{{
            x.label
          }}</span>
        </div> -->
        <!-- 展开内容 -->
        <Transition name="slide-fade">
          <div v-if="ss[index]">
            <div
              v-if="getNotificationCount(item) > 0"
              class="task-notifications"
              @click.stop
            >
              <div class="task-notifications__header">
                <span>任务通知</span>
                <span>共 {{ formatNotificationCount(item.notificationCount) }} 条</span>
              </div>
              <div
                v-if="notificationLoading[item.id]"
                class="task-notifications__state"
              >
                通知加载中...
              </div>
              <template v-else>
                <button
                  v-for="notice in notificationLists[item.id] || []"
                  :key="notice.notificationId || notice.id"
                  type="button"
                  class="task-notification-link"
                  @click.stop="openNotification(item, notice)"
                >
                  <span>{{ notice.title }}</span>
                  <time v-if="notice.sendTime">{{ notice.sendTime }}</time>
                </button>
                <div
                  v-if="!(notificationLists[item.id] || []).length"
                  class="task-notifications__state"
                >
                  暂无有效通知
                </div>
              </template>
            </div>
            <div
              class="competition-content"
              v-for="(section, sectionindex) in item.fileTaskConfigList"
              :key="sectionindex"
            >
              <div class="sctop" style="display: flex; align-items: stretch">
                <div class="shangchaun">
                  <img :src="uploadIcon" alt="" v-if="section.taskType == 1" />
                  <img
                    :src="downloadIcon"
                    alt=""
                    @click="handleDownloadss(section, section, item)"
                    style="cursor: pointer"
                    v-else
                  />
                </div>
                <div style="width: 1px; background-color: #e4e4e4"></div>
                <div class="neirong">
                  <div v-if="section.taskType == 1">
                    <div class="sectiontitle">{{ section.fileName }}</div>
                    <div class="fileType fileType-short">
                      格式限制：{{ section.fileType }}
                    </div>
                    <div class="fileSize">
                      大小限制：≤{{ section.fileSize }}MB
                    </div>
                    <div class="fileName" v-if="section.tempFileName">
                      {{ section.tempFileName }}

                      <span
                        @click="
                          handleDownload(section.tempFile, section.tempFileName)
                        "
                      >
                        <span class="download-text">下载</span>
                      </span>
                    </div>
                  </div>
                  <div v-else>
                    <div
                      class="sectiontitle"
                      @click="handleDownloadss(section, section, item)"
                      style="cursor: pointer"
                    >
                      {{ section.fileName }}
                    </div>

                    <template v-if="isContentOver5Lines(section.annoucement)">
                      <el-popover trigger="hover" placement="top" :width="800">
                        <template #reference>
                          <div class="fileType">
                            文件说明：{{ section.annoucement }}
                          </div>
                        </template>
                        <div class="popover-content">
                          文件说明：{{ section.annoucement }}
                        </div>
                      </el-popover>
                    </template>
                    <div v-else class="fileType">
                      文件说明：{{ section.annoucement }}
                    </div>
                  </div>
                  <div class="scbottom">
                    <div class="sctime">
                      {{ calculateTimeStatus(section).text }}
                    </div>
                    <div class="scbut" v-if="section.taskType == 1">
                      <el-upload
                        :action="uploadConfig.url"
                        :headers="uploadConfig.headers"
                        :timeout="uploadConfig.timeout"
                        :http-request="
                          (files) => uploadOssFilelist(files, section, item)
                        "
                        :ref="(el) => setUploadRef(el, section)"
                        :limit="1"
                        :accept="section.fileType"
                        :before-upload="(file) => beforeUpload(file, section)"
                        :on-error="
                          (error, file) => handleFileError(error, file, section)
                        "
                        :on-exceed="
                          (files, fileList) =>
                            handleExceed(files, fileList, section)
                        "
                        :on-success="
                          (response, file) =>
                            onUploadSuccess(response, file, section, item)
                        "
                        :show-file-list="false"
                        :disabled="calculateTimeStatus(section).disabled"
                        :data="{
                          bizSign: 'race', // 文件任务填race
                          bizCode: section.id, // 对应任务ID
                        }"
                      >
                        <el-button
                          :type="calculateTimeStatus(section).type"
                          :disabled="calculateTimeStatus(section).disabled"
                        >
                          {{ calculateTimeStatus(section).taskType }}
                        </el-button>
                      </el-upload>
                      <!-- <el-progress
                            v-if="uploadProgress[section.id] !== undefined"
                            :percentage="uploadProgress[section.id]"
                            type="circle"
                            :show-text="true"
                            class="quan"
                          /> -->
                    </div>
                    <div class="scbut" v-else>
                      <el-button
                        :type="calculateTimeStatus(section).type"
                        :disabled="calculateTimeStatus(section).disabled"
                        @click="handleDownloadss(section, section, item)"
                      >
                        <span>
                          <span class="download-text">下载</span>
                        </span>
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>

              <div class="scline" v-if="section.taskType == '1'">
                <template v-if="isContentOver5Lines(section.annoucement)">
                  <el-popover trigger="hover" placement="top" :width="1000">
                    <template #reference>
                      <div>
                        <div class="scxuzhi">
                          上传须知：{{ section.annoucement }}
                        </div>
                        <div class="w-full !mt-2 mb-3 !pr-[10px] !pl-[10px]">
                          <el-alert 
                                  type="error"
                                  show-icon
                                  v-if="alertMsgInfo && alertMsgInfo[section.id]"
                                  @close="closeAlertMsg(section.id)">
                          <div>{{ alertMsgInfo[section.id].content }}</div>
                        </el-alert>
                        </div>
                      </div>
                    </template>
                    <div class="popover-content">
                      上传须知：{{ section.annoucement }}
                    </div>
                  </el-popover>
                </template>
                <div v-else class="scxuzhi">
                  <div>上传须知：{{ section.annoucement }}</div>
                  <el-alert class="!mt-2" 
                            type="error"
                            show-icon
                            v-if="alertMsgInfo && alertMsgInfo[section.id]"
                            @close="closeAlertMsg(section.id)">
                    <div>{{ alertMsgInfo[section.id].content }}</div>
                  </el-alert>
                </div>

                <!-- 上传进度条 -->
                <el-progress
                  class="quan"
                  v-if="uploadProgress[section.id] !== undefined"
                  :percentage="uploadProgress[section.id]"
                />

                <div
                  v-for="(File, Filesindex) in jsonparse(
                    section.fileUploadRecord ? section.fileUploadRecord : []
                  )"
                  :key="Filesindex"
                  class="scfile"
                >
                  <div class="filename">
                    <div><img :src="fileIcon" alt="" /></div>
                    <div>
                      {{ File.fileName }}
                      <span
                        style="
                          color: #7BCE57;
                          font-size: 14px;
                          display: inline-block;
                          margin-left: 50px;
                        "
                        >已上传
                      </span>
                    </div>
                  </div>
                  <div class="filebut">
                    <span
                      class="xiazhai"
                      @click="handleDownload(File.downloadLink, File.fileName)"
                    >
                      <span class="download-text">下载</span>
                    </span>

                    <span
                      class="shanchu"
                      @click="deleteFile(File, section, item)"
                      v-if="!calculateTimeStatus(section).disabled"
                      >删除</span
                    >
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </div>
    <div
      v-else
      style="
        font-size: 20px;
        text-align: center;
        line-height: 30px;
        min-height: 400px;
      "
    >
      <el-empty description="暂无数据"></el-empty>
    </div>

    <el-dialog
      v-model="notificationDialogVisible"
      :title="notificationDetail.title || '通知详情'"
      width="720px"
      append-to-body
      destroy-on-close
      @closed="resetNotificationDetail"
    >
      <div v-loading="notificationDetailLoading" class="notification-dialog">
        <div v-if="notificationDetail.sendTime" class="notification-dialog__time">
          发布时间：{{ notificationDetail.sendTime }}
        </div>
        <div
          v-if="notificationDetail.content"
          class="notification-dialog__content"
          v-html="notificationDetail.content"
        ></div>
        <el-empty
          v-else-if="!notificationDetailLoading"
          description="暂无通知内容"
          :image-size="80"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { ElMessage, ElMessageBox, ElPopover, ElProgress } from "element-plus";
import { getToken } from "@/utils/auth";
import { CaretBottom, CaretRight } from "@element-plus/icons-vue";
import downloadIcon from "@/assets/images/download.png";
import uploadIcon from "@/assets/images/upload.png";
import fileIcon from "@/assets/images/fileicon.png";
import {
  fileDistributeUserTasklist,
  saveFileUploadRecordUser,
  downLoadRecord,
  fileTaskReadRecord,
  getSystemDate,
  saveFileUploadManagerUser,
  updateSubmitStatus,
  exportPresignedUrl,
  getFileTaskNotifications,
  getFileTaskNotificationDetail,
} from "@/api/personal/myfile.js";
import { ossFileFuc } from "@/hooks/download";
const { uploadOssFile } = ossFileFuc();
// const sss = ref({
//   id: 主id,
//   taskName: 任务名称,
//   fileUploadManagerList: [
//     {
//       fileInfo: "json",
//       totalSize:'文件大小'
//     },
//   ],
//   sysUserGroupCompetitionRelationList:sysUserGroupCompetitionRelationList
// });
const notificationLists = ref({});
const notificationLoading = reactive({});
const notificationDialogVisible = ref(false);
const notificationDetailLoading = ref(false);
const notificationDetail = ref({});
const activeNotificationTaskId = ref(null);

const getNotificationCount = (task) => {
  const count = Number(task?.notificationCount || 0);
  return Number.isFinite(count) && count > 0 ? Math.floor(count) : 0;
};

const formatNotificationCount = (count) => {
  const normalized = Number(count || 0);
  return normalized > 99 ? "99+" : normalized;
};

const loadTaskNotifications = async (task, force = false) => {
  const taskId = task?.id;
  if (!taskId) return;
  if (!getNotificationCount(task)) {
    notificationLists.value[taskId] = [];
    return;
  }
  if (!force && Object.prototype.hasOwnProperty.call(notificationLists.value, taskId)) {
    return;
  }
  if (notificationLoading[taskId]) return;

  notificationLoading[taskId] = true;
  try {
    const res = await getFileTaskNotifications(taskId);
    const data = Array.isArray(res?.data)
      ? res.data
      : Array.isArray(res?.data?.rows)
        ? res.data.rows
        : Array.isArray(res?.rows)
          ? res.rows
          : [];
    notificationLists.value[taskId] = data;
  } catch (error) {
    // 请求层已统一提示，保留上一次成功加载的通知列表。
  } finally {
    notificationLoading[taskId] = false;
  }
};

const openNotification = async (task, notice) => {
  const notificationId = notice?.notificationId || notice?.id;
  if (!task?.id || !notificationId) return;

  activeNotificationTaskId.value = task.id;
  notificationDetail.value = {
    notificationId,
    title: notice.title || "通知详情",
    sendTime: notice.sendTime,
    content: "",
  };
  notificationDialogVisible.value = true;
  notificationDetailLoading.value = true;
  try {
    const res = await getFileTaskNotificationDetail(task.id, notificationId);
    notificationDetail.value = res?.data || notificationDetail.value;
  } catch (error) {
    notificationDialogVisible.value = false;
  } finally {
    notificationDetailLoading.value = false;
  }
};

const resetNotificationDetail = () => {
  activeNotificationTaskId.value = null;
  notificationDetail.value = {};
  notificationDetailLoading.value = false;
};

// 展开/折叠控制方法
const toggleCompetition = async (index) => {
  const task = competitionData.value[index];
  if (ss.value[index]) {
    ss.value[index] = false;
  } else {
    ss.value[index] = true;
    await loadTaskNotifications(task, true);
  }
  if (!task.readCountFlag) {
    task.readCountFlag = true;
    try {
      await fileTaskReadRecord({ fileTaskId: task.id });
    } catch (error) {
      task.readCountFlag = false;
    }
  }
};
let alertMsgInfo = $ref({});

// 自动提交：上传/删除后自动调用提交接口
// fileInfoList: [{fileInfo, totalSize}] 直接传入，避免从旧item中读取
// operationType: 'upload' | 'delete' 用于区分操作类型显示不同提示
const autoSubmit = (item, fileInfoList, isReupload = false, operationType = 'upload') => {
  const params = {
    id: item.id,
    taskName: item.taskName,
    submitStatus: true,
    sysUserGroupCompetitionRelationList:
      item.sysUserGroupCompetitionRelationList,
    fileUploadManagerList: fileInfoList,
  };

  saveFileUploadManagerUser(params).then((res) => {
    if (res.code == 200) {
      if (operationType === 'delete') {
        ElMessage.success("删除成功");
      } else {
        ElMessage.success(isReupload ? "重新上传成功" : "上传成功");
      }
      alertMsgInfo = {};
    }
    getlist();
  });
};
// 上传配置
const uploadConfig = reactive({
  headers: { Authorization: "Bearer " + getToken() },
  url: import.meta.env.VITE_APP_BASE_API + `/file/oss/upload`,
  timeout: 0, // 设置超时时间为0，即不限制超时时间
});

// 存储每个section的upload引用
const uploadRefs = ref({});

// 存储上传进度
const uploadProgress = ref({});

// 设置upload引用
const setUploadRef = (el, section) => {
  if (el) {
    uploadRefs.value[section.id] = el;
  }
};

// 判断内容是否超过5行
const isContentOver5Lines = (content) => {
  if (!content) return false;

  // 估算每行大约30个中文字符
  const lineLength = 60;
  const lines = Math.ceil(content.length / lineLength);

  return lines > 5;
};

// 计算时间状态信息的函数
const calculateTimeStatus = (section) => {
  const now = currentTime.value;
  const startTime = new Date(section.uploadStart);

  // 处理永久有效情况
  if (section.perminate) {
    // 如果是上传按钮且已有文件，则显示重新上传
    let taskType = section.taskType == 1 ? "上传" : "下载";
    if (
      section.taskType == "1" &&
      section.fileUploadRecordfileInfo &&
      section.fileUploadRecordfileInfo.length > 0
    ) {
      taskType = "重新上传";
      return {
        text: "永久有效",
        taskType: taskType,
        disabled: false,
        type: "warning",
      };
    }
    return {
      text: "永久有效",
      taskType: taskType,
      disabled: false,
      type: "primary",
    };
  }
  const endTime = new Date(section.uploadEnd);

  // 计算时间差（毫秒）
  const diffToStart = startTime - now;
  const diffToEnd = endTime - now;

  // 格式化时间差为天时分秒
  const formatTimeDiff = (diffMs) => {
    const totalSeconds = Math.ceil(Math.abs(diffMs) / 1000);
    const days = Math.floor(totalSeconds / (24 * 60 * 60));
    const hours = Math.floor((totalSeconds % (24 * 60 * 60)) / (60 * 60));
    const minutes = Math.floor((totalSeconds % (60 * 60)) / 60);
    const seconds = totalSeconds % 60;

    // 如果天数大于等于1，只显示天数
    if (days >= 1) {
      return `${days}天`;
    }

    // 天数小于1时显示时分秒
    let result = [];
    if (hours > 0) result.push(`${hours}小时`);
    if (minutes > 0) result.push(`${minutes}分钟`);
    if (seconds > 0 || result.length === 0) result.push(`${seconds}秒`);

    return result.join("");
  };

  // 根据时间关系返回不同的状态信息
  if (now < startTime) {
    // 当前时间在开始时间之前
    const timeDiffText = formatTimeDiff(diffToStart);
    return {
      text: `距离开始时间还有${timeDiffText}`,
      taskType: "未开始",
      disabled: true,
      type: "",
    };
  } else if (now < endTime) {
    // 当前时间在开始和结束时间之间
    const timeDiffText = formatTimeDiff(diffToEnd);
    let text =
      section.taskType != "1"
        ? `${timeDiffText}后失效`
        : `距离截止时间还有${timeDiffText}`;

    // 如果是上传按钮且已有文件，则显示重新上传
    let taskType = section.taskType == 1 ? "上传" : "下载";
    if (
      section.taskType == "1" &&
      section.fileUploadRecord &&
      JSON.parse(section.fileUploadRecord.fileInfo).length > 0
    ) {
      taskType = "重新上传";
      return {
        text: text,
        taskType: taskType,
        type: "warning",
        disabled: false,
      };
    }
    if (section.taskType == "1") {
      return {
        text: text,
        taskType: taskType,
        type: "success",
        disabled: false,
      };
    } else {
      return {
        text: text,
        taskType: taskType,
        type: "primary",
        disabled: false,
      };
    }
  } else {
    // 当前时间在结束时间之后
    return {
      text: "截止时间已到期",
      taskType: "已截止",
      disabled: true,
      type: "danger",
    };
  }
};
const deleteFile = (File, section, item) => {
  ElMessageBox.confirm("确定要删除该文件吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    let zong = JSON.parse(section.fileUploadRecord.fileInfo) || [];

    let index = zong.findIndex((x) => x.fileName == File.fileName);
    if (index != -1) {
      zong.splice(index, 1);
    }
    const params = {
      fileTaskId: section.id,
      fileTaskName: section.fileName,
      uploadTime: section.uploadEnd,
      totalSize: 0,
      id: section.fileUploadRecord?.id,
      uploadOperationType: "delete",
      // fileInfo: JSON.stringify(zong),
      fileInfo: section.fileUploadRecord.fileInfo,
      sysUserGroupCompetitionRelationList:
        item.sysUserGroupCompetitionRelationList,
    };
    saveFileUploadRecordUser(params).then((res) => {
      // 删除成功后自动提交
      const fileInfoList = [];
      item.fileTaskConfigList.forEach((s) => {
        if (
          s.id !== section.id &&
          s.taskType == 1 &&
          s.fileUploadRecord &&
          s.fileUploadRecord.fileInfo &&
          JSON.parse(s.fileUploadRecord.fileInfo).length > 0
        ) {
          fileInfoList.push({
            fileInfo: s.fileUploadRecord.fileInfo,
            totalSize: s.fileUploadRecord.totalSize,
          });
        }
      });
      autoSubmit(item, fileInfoList, false, 'delete');
    });
  });
};
// 统一下载方法
const handleDownload = (file, fileName) => {
  if (file) {
    // downloadJS(
    //   import.meta.env.VITE_APP_BASE_API +
    //     `/file/oss/presignedUrl?fileKey=${file}`,
    //   fileName
    // );
    downloadOssFile(file);
    // 创建临时a标签进行下载
    // const link = document.createElement("a");
    // link.href = file;
    // link.target = "_blank";
    // link.style.display = "none";

    // document.body.appendChild(link);
    // link.click();
    // document.body.removeChild(link);
  } else {
    ElMessage.error("下载链接无效");
  }
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
const handleDownloadss = (file, rizhi, item) => {
  if (calculateTimeStatus(file).disabled) {
    return;
  }
  if (file.tempFile) {
    downloadOssFile(file.tempFile);
    // downloadJS(
    //   `/file/oss/presignedUrl?fileKey=${file.tempFile}`,
    //   rizhi.fileName + "." + file.tempFileName.split(".").at(-1)
    // );
    //  downloadJS(
    //       import.meta.env.VITE_APP_BASE_API +
    //         `/file/oss/presignedUrl?fileKey=${file.tempFile}`,
    //       rizhi.fileName + "." + file.tempFileName.split(".").at(-1)
    //     );
    // // 创建临时a标签进行下载
    // const link = document.createElement("a");
    // link.href = file.tempFile;
    // link.target = "_blank";
    // link.style.display = "none";
    // document.body.appendChild(link);
    // link.click();
    // document.body.removeChild(link);

    const params = {
      taskId: item.id,
      fileName: file.tempFileName,
      fileTaskId: rizhi.taskId,
      fileTaskName: rizhi.fileName,
    };
    downLoadRecord(params).then((res) => {});
  } else {
    ElMessage.error("下载链接无效");
  }
};
// 新上传
const uploadOssFilelist = (files, section, item) => {
  let file = files.file;
  // 清除上传进度
  delete uploadProgress.value[section.id];
  uploadOssFile(
    file,
    {
      bizSign: "race", // 文件任务填race
      bizCode: section.id, // 对应任务ID
    },
    (p) => {
      uploadProgress.value[section.id] = Math.round(p * 100);
    }
  ).then((response) => {
    // ElMessage.success("文件上传成功");
    // 将上传成功的文件赋值给section.uploadedFiles，覆盖原有内容
    let wenjian = [
      {
        fileName: response.fileName,
        downloadLink: response.ossUrl || "",
      },
    ];
    const params = {
      fileTaskId: section.id,
      fileTaskName: section.fileName,
      uploadTime: section.uploadEnd,
      // totalSize: section.fileSize,
      totalSize: (file.size / 1024 / 1024).toFixed(5),
      id: section.fileUploadRecord?.id,
      fileInfo: JSON.stringify(wenjian),
      sysUserGroupCompetitionRelationList:
        item.sysUserGroupCompetitionRelationList,
    };
    saveFileUploadRecordUser(params).then((res) => {
      if (res.code == 200) {
        delete uploadProgress.value[section.id];
        // 上传成功后自动提交
        const isReupload = !!section.fileUploadRecord;
        const fileInfoList = [
          {
            fileInfo: JSON.stringify(wenjian),
            totalSize: params.totalSize,
          },
        ];
        // 收集其他已上传的文件信息
        item.fileTaskConfigList.forEach((s) => {
          if (
            s.id !== section.id &&
            s.taskType == 1 &&
            s.fileUploadRecord &&
            s.fileUploadRecord.fileInfo
          ) {
            fileInfoList.push({
              fileInfo: s.fileUploadRecord.fileInfo,
              totalSize: s.fileUploadRecord.totalSize,
            });
          }
        });
        autoSubmit(item, fileInfoList, isReupload);
      } else {
        getlist();
      }
    });
  });
};
// 文件上传前的校验
const beforeUpload = (file, section) => {
  // 校验文件格式
  let fileTypes = section.fileType;

  if (fileTypes && fileTypes != "") {
    console.log(fileTypes);
    const allowedFormats = fileTypes.split(",");
    const fileExtension = file.name.split(".").pop().toLowerCase();

    const fileType = file.type.toLowerCase();

    // 处理Excel格式的特殊情况
    const processedAllowedFormats = [];
    allowedFormats.forEach((format) => {
      if (format === "excel") {
        // 添加Excel的实际扩展名和MIME类型识别
        processedAllowedFormats.push("xls", "xlsx");
      } else {
        processedAllowedFormats.push(format);
      }
    });
    // 检查文件扩展名或MIME类型是否在允许列表中
    console.log(fileExtension, processedAllowedFormats);
    const isFormatAllowed = processedAllowedFormats.some(
      (format) => fileExtension === format || fileType.includes(format)
    );

    if (!isFormatAllowed) {
      alertMsgInfo[section.id] = {
        content: `只允许上传${fileTypes}格式的文件`,
      };
      // ElMessage.warning(`只允许上传${fileTypes}格式的文件`);
      return false;
    }
  }

  // 校验文件大小
  const fileSize = section.fileSize;
  if (fileSize && fileSize !== "") {
    const maxSize = parseFloat(fileSize);
    const unit = fileSize.replace(/[\d.]/g, "").toUpperCase();
    let maxSizeBytes = maxSize * 1024 * 1024; // 默认MB

    if (unit === "KB") {
      maxSizeBytes = maxSize * 1024;
    } else if (unit === "GB") {
      maxSizeBytes = maxSize * 1024 * 1024 * 1024;
    }

    if (file.size > maxSizeBytes) {
      alertMsgInfo[section.id] = {
        content: `文件大小不能超过${fileSize}MB`,
      };
      // ElMessage.warning(`文件大小不能超过${fileSize}MB`);
      return false;
    }
  }
  if (alertMsgInfo[section.id]) {
    delete alertMsgInfo[section.id];
  }
  return true;
};

// 关闭提示信息
const closeAlertMsg = (sectionId) => {
  if (alertMsgInfo[sectionId]) {
    delete alertMsgInfo[sectionId];
  }
};

// 上传成功后的处理
const onUploadSuccess = (response, file, section, item) => {
  // 清除上传进度
  delete uploadProgress.value[section.id];

  if (response.code === 200) {
    // ElMessage.success("文件上传成功");
    // 将上传成功的文件赋值给section.uploadedFiles，覆盖原有内容
    let wenjian = [
      {
        fileName: file.name,
        downloadLink: response.data || "",
      },
    ];
    const params = {
      fileTaskId: section.id,
      fileTaskName: section.fileName,
      uploadTime: section.uploadEnd,
      // totalSize: section.fileSize,
      totalSize: (file.size / 1024 / 1024).toFixed(5),
      id: section.fileUploadRecord?.id,
      fileInfo: JSON.stringify(wenjian),
      sysUserGroupCompetitionRelationList:
        item.sysUserGroupCompetitionRelationList,
    };
    saveFileUploadRecordUser(params).then((res) => {
      if (res.code == 200) {
        const isReupload = !!section.fileUploadRecord;
        const fileInfoList = [
          {
            fileInfo: JSON.stringify(wenjian),
            totalSize: params.totalSize,
          },
        ];
        item.fileTaskConfigList.forEach((s) => {
          if (
            s.id !== section.id &&
            s.taskType == 1 &&
            s.fileUploadRecord &&
            s.fileUploadRecord.fileInfo
          ) {
            fileInfoList.push({
              fileInfo: s.fileUploadRecord.fileInfo,
              totalSize: s.fileUploadRecord.totalSize,
            });
          }
        });
        autoSubmit(item, fileInfoList, isReupload);
      } else {
        getlist();
      }
    });
  } else {
    ElMessage.error(response.msg || "文件上传失败");
  }
};


// 上传错误处理
const handleFileError = (error, file, section) => {
  ElMessage.error("文件上传失败，请稍后重试");
  console.error("上传错误:", error);
  // 清除上传进度
  delete uploadProgress.value[section.id];
};

// 处理文件超出数量限制
const handleExceed = (files, fileList, section) => {
  // 自动替换现有文件
  const uploadRef = uploadRefs.value[section.id];
  if (uploadRef) {
    uploadRef.clearFiles();
    uploadRef.handleStart(files[0]);
    uploadRef.submit();
  }
};
// 虚拟数据
const competitionData = ref([]);
const getlist = async () => {
  const res = await fileDistributeUserTasklist();
  if (res.code == 200) {
    competitionData.value = Array.isArray(res.data) ? res.data : [];
    const currentTaskIds = new Set(
      competitionData.value.map((task) => String(task.id))
    );
    Object.keys(notificationLists.value).forEach((taskId) => {
      if (!currentTaskIds.has(String(taskId))) {
        delete notificationLists.value[taskId];
      }
    });

    await Promise.all(
      competitionData.value.map((task, index) => {
        if (!getNotificationCount(task)) {
          notificationLists.value[task.id] = [];
          return Promise.resolve();
        }
        return ss.value[index]
          ? loadTaskNotifications(task, true)
          : Promise.resolve();
      })
    );

    if (notificationDialogVisible.value && activeNotificationTaskId.value) {
      if (!currentTaskIds.has(String(activeNotificationTaskId.value))) {
        notificationDialogVisible.value = false;
      } else {
        const activeList =
          notificationLists.value[activeNotificationTaskId.value] || [];
        const activeId = notificationDetail.value.notificationId ||
          notificationDetail.value.id;
        if (
          activeId &&
          !activeList.some(
            (notice) =>
              String(notice.notificationId || notice.id) === String(activeId)
          )
        ) {
          notificationDialogVisible.value = false;
        }
      }
    }

    if (competitionData.value.length > 0 && !competitionData.value[0].readCountFlag) {
      competitionData.value[0].readCountFlag = true;
      try {
        await fileTaskReadRecord({
          fileTaskId: competitionData.value[0].id,
        });
      } catch (error) {
        competitionData.value[0].readCountFlag = false;
      }
    }
  }
};
const ss = ref([true]);
const jsonparse = (item) => {
  if (!item.fileInfo) {
    return [];
  }
  return JSON.parse(item.fileInfo) || [];
};
// 创建响应式的当前时间
const currentTime = ref(new Date());
const fuwuqitime = ref(null);
const shuaxin = ref(null);
onMounted(() => {
  getSystemDate().then((res) => {
    if (res.code == 200) {
      currentTime.value = new Date(res.data);
      // 每秒钟更新一次当前时间
      fuwuqitime.value = setInterval(() => {
        res.data += 1000;
        currentTime.value = new Date(res.data);
      }, 1000);
    }
  });
  getlist();
  shuaxin.value = setInterval(() => {
    getlist();
  }, 300000);
});
onUnmounted(() => {
  // 组件卸载时清除定时器
  if (fuwuqitime.value) {
    clearInterval(fuwuqitime.value);
  }
  if (shuaxin.value) {
    clearInterval(shuaxin.value);
  }
});
</script>

<style scoped lang="scss">
.wenjian {
  min-height: 500px;
  width: 1055px;
  padding: 20px;

  // 赛事项样式
  .competition-item {
    margin-bottom: 10px;
    background-color: #f2f5f7;
    border-radius: 6px;
    padding-bottom: 10px;
    border: 1px solid transparent;
    transition: border-color 0.2s ease, box-shadow 0.2s ease,
      background-color 0.2s ease;

    &.has-notification {
      border-color: #f3b543;
      background-color: #fffaf0;
      box-shadow: 0 4px 16px rgba(230, 158, 35, 0.14);
    }

    .competition-title {
      cursor: pointer;
      display: flex;
      align-items: center;
      flex: 1;
      min-width: 0;
      padding: 20px;
      font-family: Source Han Sans CN, Source Han Sans CN;
      font-weight: 500;
      font-size: 20px;
      color: #333333;
      text-align: left;

      .icon {
        font-size: 24px;
        color: #999999;
        margin-right: 10px;
        transition: transform 0.3s ease;
        &.rotate {
          transform: rotate(0deg);
        }
      }

      .task-title-text {
        flex: 1;
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .notification-badge {
        flex: 0 0 auto;
        margin-left: 14px;
        padding: 4px 10px;
        border-radius: 12px;
        color: #a86200;
        background: #fff0c7;
        font-size: 13px;
        font-weight: 500;
        line-height: 20px;
      }

      .status {
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 14px;
        margin-left: 10px;

        &.ongoing {
          background-color: #f0f9eb;
          color: #67c23a;
        }

        &.upcoming {
          background-color: #fef0f0;
          color: #f56c6c;
        }
      }
    }

    .task-notifications {
      width: calc(100% - 40px);
      margin: 18px auto 0;
      padding: 16px 18px;
      border: 1px solid #f3d89b;
      border-radius: 8px;
      background: #fffdf7;
      box-sizing: border-box;
    }

    .task-notifications__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;
      color: #7f5200;
      font-size: 14px;
      font-weight: 600;

      span:last-child {
        color: #a98139;
        font-size: 12px;
        font-weight: 400;
      }
    }

    .task-notification-link {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      padding: 10px 4px;
      border: 0;
      border-bottom: 1px dashed #eadfca;
      color: #4b5566;
      background: transparent;
      text-align: left;
      cursor: pointer;

      &:last-of-type {
        border-bottom: 0;
      }

      span {
        overflow: hidden;
        color: #9a6500;
        font-size: 14px;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      time {
        flex: 0 0 auto;
        margin-left: 18px;
        color: #9b9b9b;
        font-size: 12px;
      }

      &:hover span {
        color: #d58a00;
        text-decoration: underline;
      }
    }

    .task-notifications__state {
      padding: 12px 0 6px;
      color: #999;
      font-size: 13px;
      text-align: center;
    }

    .submit-btn,
    .re-submit-btn {
      padding: 5px 15px;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.3s ease;
      margin-right: 20px;
    }

    .submit-btn {
      background-color: #409eff;
      color: white;
    }

    .re-submit-btn {
      background-color: #67c23a;
      color: white;
    }
    // 展开内容样式
    .competition-content {
      width: calc(100% - 40px);
      margin: 20px auto;
      background-color: #fff;
      border-radius: 8px;

      .sctop {
        display: flex;
        justify-content: space-between;
        align-items: center;
        height: auto;
        min-height: 110px;
        .shangchaun {
          width: 100px;
          display: flex;
          justify-content: center;
          align-items: center;
          height: auto;

          img {
            width: 34px;
          }
        }
        .neirong {
          width: 950px;
          min-height: 110px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-left: 20px;
          padding: 15px 0;
          .sectiontitle {
            font-family: Source Han Sans CN, Source Han Sans CN;
            font-weight: 500;
            font-size: 16px;
            color: #333333;
            line-height: 22px;
            text-align: left;
            font-style: normal;
            text-transform: none;
          }
          .fileType {
            width: 600px;
            font-family: Source Han Sans CN, Source Han Sans CN;
            font-weight: 400;
            font-size: 14px;
            color: #999999;
            line-height: 20px;
            text-align: left;
            font-style: normal;
            text-transform: none;
            /* 限制显示5行 */
            display: -webkit-box;
            -webkit-line-clamp: 5;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
            max-height: calc(5 * 20px);
          }
          /* 短内容的fileType，不限制行数 */
          .fileType-short {
            display: block;
            -webkit-line-clamp: unset;
            -webkit-box-orient: unset;
            overflow: visible;
            text-overflow: unset;
            max-height: none;
          }
          .fileSize {
            font-family: Source Han Sans CN, Source Han Sans CN;
            font-weight: 400;
            font-size: 14px;
            color: #999999;
            line-height: 20px;
            text-align: left;
            font-style: normal;
            text-transform: none;
          }
          .fileName {
            font-family: Source Han Sans CN, Source Han Sans CN;
            font-weight: 400;
            font-size: 14px;
            color: #333333;
            line-height: 20px;
            text-align: left;
            font-style: normal;
            text-transform: none;
            span {
              font-family: Source Han Sans CN, Source Han Sans CN;
              font-weight: 400;
              font-size: 14px;
              color: #3169f8;
              line-height: 20px;
              text-align: left;
              font-style: normal;
              text-transform: none;
              cursor: pointer;
            }
          }
          .scbottom {
            width: 250px;
            text-align: end;
            margin-right: 20px;
            .sctime {
              font-family: Source Han Sans CN, Source Han Sans CN;
              font-weight: 400;
              font-size: 14px;
              color: #ff4444;
              line-height: 20px;
              text-align: right;
              font-style: normal;
              text-transform: none;
            }
            .scbut {
              margin-top: 10px;
            }
          }
        }
      }
      .scline {
        min-height: 50px;
        background: #ffffff;
        border-radius: 0px 0px 8px 8px;
        border: 1px solid #fff;
        border-top: 1px solid #e4e4e4;
        .scxuzhi {
          margin: 10px;
          font-family: Source Han Sans CN, Source Han Sans CN;
          font-weight: 400;
          font-size: 14px;
          color: #64666a;

          text-align: left;
          font-style: normal;
          text-transform: none;
          text-indent: 20px;

          /* 限制显示5行 */
          display: -webkit-box;
          -webkit-line-clamp: 5;
          -webkit-box-orient: vertical;
          overflow: hidden;
          text-overflow: ellipsis;
          line-height: 1.5;
          max-height: calc(5 * 1.5em);
        }

        .scfile {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin: 20px;
          height: 48px;
          background: #f5f5f5;
          border-radius: 8px 8px 8px 8px;
          .filename {
            display: flex;
            img {
              margin: 0 20px;
            }
          }
          .filebut {
            padding-right: 30px;
            .xiazhai {
              font-family: Source Han Sans CN, Source Han Sans CN;
              font-weight: 400;
              font-size: 14px;
              color: #3169f8;
              line-height: 20px;
              text-align: left;
              font-style: normal;
              text-transform: none;
              margin-right: 20px;
              cursor: pointer;
            }
            .shanchu {
              font-family: Source Han Sans CN, Source Han Sans CN;
              font-weight: 400;
              font-size: 14px;
              color: #ff4444;
              line-height: 20px;
              text-align: left;
              font-style: normal;
              text-transform: none;
              cursor: pointer;
            }
          }
        }
      }
    }
  }

  // 赛事信息样式
  .competition-info {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    margin-bottom: 20px;
    padding: 15px;
    border-radius: 4px;
    padding-left: 40px;
    .info-item {
      font-family: Source Han Sans CN, Source Han Sans CN;
      font-weight: 400;
      font-size: 18px;
      color: #333333;
      line-height: 25px;
      text-align: left;
      font-style: normal;
      text-transform: none;
    }
  }
}

// 过渡动画样式
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
  max-height: 0;
}

.slide-fade-enter-to,
.slide-fade-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 1000px;
}
.item {
  min-width: 0;
  max-width: calc(100% - 100px);
}
:deep(.el-badge__content.is-fixed) {
  top: 0;
  right: 0;
  width: 10px;
  height: 10px;
}
.popover-content {
  text-indent: 20px;
  line-height: 1.5;
  padding: 10px;
  font-size: 12px;
}
.quan {
  padding: 0 30px;
}

.notification-dialog {
  min-height: 160px;
}

.notification-dialog__time {
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  color: #909399;
  font-size: 13px;
}

.notification-dialog__content {
  max-height: 55vh;
  padding: 18px 4px 4px;
  overflow-y: auto;
  color: #303133;
  font-size: 15px;
  line-height: 1.75;
  overflow-wrap: anywhere;

  :deep(img) {
    max-width: 100%;
    height: auto;
  }

  :deep(table) {
    max-width: 100%;
    border-collapse: collapse;
  }

  :deep(td),
  :deep(th) {
    padding: 6px 10px;
    border: 1px solid #dcdfe6;
  }

  :deep(a) {
    color: #3169f8;
    word-break: break-all;
  }
}
</style>
