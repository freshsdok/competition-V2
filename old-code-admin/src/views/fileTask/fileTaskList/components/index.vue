<template>
  <div class="app-container">
    <el-dialog
      :title="title"
      v-model="open"
      width="1200px"
      append-to-body
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :before-close="dialogClose"
    >
      <div
        style="height: 75vh; overflow: auto"
        class="dialog-content"
        ref="dialogContentRef"
      >
        <el-form
          :model="form"
          ref="formRef"
          :rules="rules"
          label-width="120px"
          :disabled="isdisabled"
        >
          <div style="display: flex; align-items: center">
            <el-form-item label="任务名称" prop="taskName">
              <el-input
                v-model="form.taskName"
                placeholder="请输入任务名称"
                clearable
                style="width: 300px"
              />
            </el-form-item>
            <el-form-item
              label="选择用户组"
              prop="userGroupIds"
              @click="!isdisabled ? openShowclick() : ''"
            >
              <!-- <el-input disabled v-model="form.userGroupNames" placeholder="请选择用户组" clearable style="width: 300px" /> -->
              <div
                style="
                  width: 300px;
                  padding: 1px 11px;
                  height: 32px;
                  line-height: 29px;
                  font-family:
                    Helvetica Neue,
                    Helvetica,
                    PingFang SC,
                    Hiragino Sans GB,
                    Microsoft YaHei,
                    Arial,
                    sans-serif;
                  border-radius: 4px 4px;
                  border: 1px solid #dcdfe6;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  /* overflow-x: auto; */
                "
              >
                <span
                  v-if="!form.userGroupNames"
                  style="
                    color: var(--el-text-color-placeholder);
                    font-style: inherit;
                    font-size: inherit;
                  "
                  >请选择用户组</span
                >
                <span v-else style="color: #606266; font-size: inherit" :title="form.userGroupNames">{{
                  form.userGroupNames
                }}</span>
              </div>
            </el-form-item>
          </div>
          <el-form-item prop="name" label-position="top" label-width="120px">
            <div
              style="
                width: 110px;
                text-align: right;
                font-size: 14px;
                font-weight: bold;
              "
            >
              任务配置
            </div>
            <div
              style="
                width: 100%;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
              "
            >
              <el-empty
                v-if="form.fileTaskConfigList.length === 0"
                description="暂无任务"
              ></el-empty>

              <div
                v-if="form.fileTaskConfigList.length === 0"
                style="
                  width: 100%;
                  display: flex;
                  justify-content: center;
                  margin-top: 20px;
                "
              >
                <el-button type="primary" @click="hadleAdd('1')"
                  >添加交互任务</el-button
                >
                <el-button type="primary" @click="hadleAdd('2')"
                  >添加分发任务</el-button
                >
              </div>
            </div>
            <!-- 交互任务 -->
            <el-card
              class="box-card"
              style="width: 100%"
              v-for="(item, index) in form.fileTaskConfigList"
              :key="index"
            >
              <template #header>
                <div class="clearfixClass">
                  <span style="font-size: 16px; font-weight: bold">{{
                    item.taskType === "2" ? "分发任务" : "交互任务"
                  }}</span>
                  <el-button type="danger" @click="handleDelete(index)"
                    >删除</el-button
                  >
                </div>
              </template>
              <div v-if="item.taskType === '2'">
                <el-row :gutter="20">
                  <el-col :span="24" style="margin-bottom: 15px">
                    <el-form-item
                      :label="'文件名称'"
                      :prop="'fileTaskConfigList.' + index + '.fileName'"
                      :rules="rules.fileName"
                    >
                      <el-input
                        v-model="item.fileName"
                        placeholder="请输入文件名称"
                        clearable
                        style="width: 300px"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="10" style="margin-bottom: 15px">
                    <el-form-item
                      label="允许下载时间"
                      :prop="'fileTaskConfigList.' + index + '.uploadStart'"
                      :rules="rules.uploadStart"
                    >
                      <el-date-picker
                        v-model="item.uploadStart"
                        type="datetime"
                        placeholder="请选择允许下载时间"
                        format="YYYY-M-D HH:mm:ss"
                        value-format="YYYY-M-D HH:mm:ss"
                        :disabled="item.perminate"
                      >
                      </el-date-picker>
                    </el-form-item>
                  </el-col>
                  <el-col :span="10">
                    <el-form-item
                      label="下载截止时间"
                      :prop="'fileTaskConfigList.' + index + '.uploadEnd'"
                      :rules="rules.uploadEnd"
                    >
                      <el-date-picker
                        v-model="item.uploadEnd"
                        type="datetime"
                        placeholder="请选择下载截止时间"
                        format="YYYY-M-D HH:mm:ss"
                        value-format="YYYY-M-D HH:mm:ss"
                        :disabled="item.perminate"
                      >
                      </el-date-picker>
                    </el-form-item>
                  </el-col>
                  <el-col :span="3">
                    <el-checkbox
                      v-model="item.perminate"
                      @change="handlePerminateChange(item)"
                      >永久有效</el-checkbox
                    >
                  </el-col>
                  <el-col :span="24" style="margin-bottom: 15px">
                    <el-form-item
                      label="下载须知"
                      :prop="'fileTaskConfigList.' + index + '.annoucement'"
                    >
                      <el-input
                        type="textarea"
                        :autosize="{ minRows: 4, maxRows: 4 }"
                        placeholder="请输入下载须知"
                        v-model="item.annoucement"
                        style="width: 600px"
                      >
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    
                    <el-form-item
                      label="需分发的文件"
                      :prop="'fileTaskConfigList.' + index + '.fileList'"
                      :rules="rules.fileList"
                    >
                      <div style="display: flex; flex-direction: column">
                      <el-upload
                        ref="uploadRef"
                        class="upload-demo"
                        action="#"
                        :on-preview="handlePreview"
                        :show-file-list="false"
                        :http-request="
                          (res) => handleHttpRequest(res, item, index)
                        "
                        :on-success="
                          (res, file) =>
                            handleUploadSuccess(res, file, item, index)
                        "
                        :on-error="handleUploadError"
                        :before-remove="beforeRemove"
                        multiple
                        :limit="1"
                        :on-exceed="handleExceed"
                        :file-list="item.fileList || []"
                        :headers="headers"
                        style="width: 600px"
                      >
                        <div>
                          <el-button size="small" type="primary"
                            >点击上传</el-button
                          >
                          <template slot="tip" class="el-upload__tip">
                            提示：支持上传PDF、Word、Excel、PPT、图片等格式的文件
                          </template>
                        </div>
                      </el-upload>
                      <div
                        v-for="child in item.fileList"
                        :key="child.url"
                        style="display: flex; align-items: center"
                      >
                        <div class="uploasdList">{{ child.name }}</div>
                        <el-button  @click="handleRemove(item, index)" icon="Close" link ></el-button>
                      </div>
                      <el-progress
                        v-if="
                          item.percentage &&
                          item.percentage != 0 &&
                          item.percentage != 100
                        "
                        :percentage="item.percentage"
                        :status="item.percentage == 100 ? 'success' : null"
                      />
                      </div>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
              <div v-if="item.taskType === '1'">
                <el-row :gutter="20">
                  <el-col :span="12" style="margin-bottom: 15px">
                    <el-form-item
                      label="文件名称"
                      :prop="'fileTaskConfigList.' + index + '.fileName'"
                      :rules="rules.fileName"
                    >
                      <el-input
                        v-model="item.fileName"
                        placeholder="请输入文件名称"
                        clearable
                        style="width: 300px"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12" style="margin-bottom: 15px">
                    <el-form-item
                      label="大小要求(MB)"
                      :prop="'fileTaskConfigList.' + index + '.fileSize'"
                      :rules="rules.fileSize"
                    >
                      <el-input-number
                        v-model="item.fileSize"
                        placeholder="请输入大小要求"
                        style="width: 300px"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12" style="margin-bottom: 15px">
                    <el-form-item
                      label="允许上传时间"
                      :prop="'fileTaskConfigList.' + index + '.uploadStart'"
                      :rules="rules.uploadStart"
                    >
                      <el-date-picker
                        v-model="item.uploadStart"
                        type="datetime"
                        placeholder="请选择允许上传时间"
                        format="YYYY-M-D HH:mm:ss"
                        value-format="YYYY-M-D HH:mm:ss"
                        style="width: 300px"
                      >
                      </el-date-picker>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12" style="margin-bottom: 15px">
                    <el-form-item
                      label="上传截止时间"
                      :prop="'fileTaskConfigList.' + index + '.uploadEnd'"
                      :rules="rules.uploadEnd"
                    >
                      <el-date-picker
                        v-model="item.uploadEnd"
                        type="datetime"
                        placeholder="请选择上传截止时间"
                        format="YYYY-M-D HH:mm:ss"
                        value-format="YYYY-M-D HH:mm:ss"
                        :disabled="item.perminate"
                        style="width: 300px"
                      >
                      </el-date-picker>
                    </el-form-item>
                  </el-col>
                  <el-col :span="24" style="margin-bottom: 15px">
                    <el-form-item label="上传模板">
                      <div style="display: flex; flex-direction: column">
                        <el-upload
                          ref="uploadRef"
                          class="upload-demo"
                          action="#"
                          :on-preview="handlePreview"
                          :data="{
                            bizSign: 'race',
                            bizCode: '77',
                          }"
                          :show-file-list="false"
                          :http-request="
                            (res) => handleHttpRequest(res, item, index)
                          "
                          :on-success="
                            (res, file) =>
                              handleUploadSuccess(res, file, item, index)
                          "
                          :on-error="handleUploadError"
                          :before-remove="beforeRemove"
                          multiple
                          :limit="1"
                          :on-exceed="handleExceed"
                          :file-list="item.fileList || []"
                          :headers="headers"
                          style="width: 600px"
                        >
                          <div>
                            <el-button size="small" type="primary"
                              >点击上传</el-button
                            >

                            <div slot="tip" class="el-upload__tip">
                              提示：上传模板文件，用户可下载参考
                            </div>
                          </div>
                        </el-upload>
                        <div
                          v-for="child in item.fileList"
                          :key="child.url"
                          style="display: flex; align-items: center"
                        >
                          <div class="uploasdList">{{ child.name }}</div>
                        
                            <el-button  @click="handleRemove(item, index)" icon="Close" link ></el-button>
                        </div>
                        <el-progress
                          v-if="
                            item.percentage &&
                            item.percentage != 0 &&
                            item.percentage != 100
                          "
                          :percentage="item.percentage"
                          :status="item.percentage == 100 ? 'success' : null"
                        />
                      </div>

                      <!-- <el-upload ref="uploadRef" class="upload-demo" :action="uploadFileUrl" :on-preview="handlePreview"
                        :data="{
                          bizSign: 'race',
                          bizCode: '77',
                        }" :on-remove="(file, fileList) =>
                            handleRemove(file, fileList, item, index)
                          " :on-success="(res, file) =>
                            handleUploadSuccess(res, file, item, index)
                          " :on-error="handleUploadError" :before-remove="beforeRemove" multiple :limit="1"
                        :on-exceed="handleExceed" :file-list="item.fileList || []" :headers="headers"
                        style="width: 600px">
                        <div>
                          <el-button size="small" type="primary">点击上传</el-button>

                          <div slot="tip" class="el-upload__tip">
                            提示：上传模板文件，用户可下载参考
                          </div>
                        </div>
                      </el-upload> -->
                    </el-form-item>
                  </el-col>
                  <el-col :span="24" style="margin-bottom: 15px">
                    <el-form-item
                      label="允许的文件类型"
                      :prop="'fileTaskConfigList.' + index + '.fileType'"
                    >
                      <div style="display: flex; flex-direction: column">
                        <el-checkbox-group v-model="item.fileType">
                          <el-checkbox
                            v-for="item in file_type"
                            :label="item.value"
                            :key="item.value"
                            >{{ item.label }}</el-checkbox
                          >
                        </el-checkbox-group>
                        <div
                          style="
                            display: block;
                            font-size: 12px;
                            color: #606266;
                          "
                        >
                          提示：不选则不做任何文件类型限制
                        </div>
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="24" style="margin-bottom: 15px">
                    <el-form-item
                      label="上传须知"
                      :prop="'fileTaskConfigList.' + index + '.annoucement'"
                    >
                      <el-input
                        type="textarea"
                        :autosize="{ minRows: 4, maxRows: 4 }"
                        placeholder="请输入上传须知"
                        v-model="item.annoucement"
                        style="width: 600px"
                      >
                      </el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </el-card>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer">
        <div
          v-if="!isdisabled && form.fileTaskConfigList.length != 0"
          style="
            width: 100%;
            display: flex;
            justify-content: center;
            margin-top: 10px;
          "
        >
          <el-button type="primary" @click="hadleAdd('1')"
            >添加交互任务</el-button
          >
          <el-button type="primary" @click="hadleAdd('2')"
            >添加分发任务</el-button
          >
        </div>
        <div class="dialog-footer" v-if="!isdisabled">
          <el-button @click="dialogClose">取 消</el-button>
          <el-button type="warning" @click="submit('1')">保存草稿</el-button>
          <el-button type="success" @click="submit('2')">发布任务</el-button>
        </div>
        <div style="display: flex; justify-content: center" v-if="isdisabled">
          <el-button type="primary" @click="dialogClose">确 定</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="openShow"
      width="1100px"
      append-to-body
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-row :gutter="20">
        <el-col :span="16">
          <div style="display: flex; margin-bottom: 15px">
            <el-input
              style="width: 200px"
              placeholder="请输入用户组"
              v-model.trim="queryParams.name"
              clearable
            >
            </el-input>
            <el-button
              style="margin-left: 10px"
              type="primary"
              @click="searchUserGroup"
              >搜 索</el-button
            >
          </div>
          <el-table
            ref="multipleTableRef"
            :data="userData"
            row-key="id"
            @select="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="name" label="用户组"></el-table-column>
            <el-table-column prop="descripe" label="描述"></el-table-column>
          </el-table>
          <pagination
            v-show="total > 0"
            :total="total"
            size="small"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="getUserList"
          />
        </el-col>
        <el-col :span="8">
          <div
            style="
              display: flex;
              height: 32px;
              justify-content: space-between;
              margin-bottom: 15px;
              align-items: center;
            "
          >
            <span>已选择 {{ selectedUsers.length }} 项</span>
            <span style="color: red; cursor: pointer" @click="handleClear">
              清空
            </span>
          </div>
          <div style="display: flex; flex-direction: column">
            <el-tag
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin: 0px 0 10px 0;
              "
              v-for="(item, index) in selectedUsers"
              :key="item.name"
              type="info"
              closable
              @close="handleClose(item, index)"
              >{{ item.name }}</el-tag
            >
          </div>
        </el-col>
      </el-row>
      <div slot="footer">
        <div class="dialog-footer">
          <el-button @click="clearUser">取 消</el-button>
          <el-button type="primary" @click="submitUser">确 定</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, toRefs, nextTick } from "vue";
import { getToken } from "@/utils/auth";
import { saveFileTask, editFileTask } from "@/api/fileTask/task";
import { ElMessage } from "element-plus";
import { systemUserGroupMangerList } from "@/api/fileTask";
import { ossFileFuc } from "@/hooks/download";
import { replaceFileOrigin } from "@/utils/fileOrigin";
const { uploadOssFile } = ossFileFuc();
const emit = defineEmits(["refresh"]);
const formRef = ref(null);
const open = ref(false);
const openShow = ref(false);
const title = ref("创建新任务");
const dialogContentRef = ref(null);
const isdisabled = ref(false);
const value = ref("");
// const uploadFileUrl = ref(import.meta.env.VITE_APP_BASE_API + "/file/upload");
const uploadFileUrl = ref(
  import.meta.env.VITE_APP_BASE_API + "/file/oss/upload",
);
const { proxy } = getCurrentInstance();
const { file_type } = proxy.useDict("file_type");
const headers = ref({ Authorization: "Bearer " + getToken() });
const multipleTableRef = ref(null);
const total = ref(0);
const uploadRef = ref(null);
const options = ref([
  {
    value: "1",
    label: "用户组1",
  },
  {
    value: "2",
    label: "用户组2",
  },
]);
const optionsTest = ref([
  {
    value: "选项1",
    label: "选项1",
  },
]);
const userList = ref([
  {
    name: "用户组1",
    desc: "描述1",
  },
]);
const data = reactive({
  form: {
    remark: null,
    id: "",
    taskName: "",
    userGroupIds: [],
    userGroupNames: "",
    taskStatus: "1",
    fileTaskConfigList: [],
  },
  queryParams: {
    pageNum: 1, // 当前页码
    pageSize: 10,
    name: "",
  },
  selectedRowsOld: [], // 表格选中框
  selectedUsers: [],
  userData: [],

  rules: {
    taskName: [
      { required: true, message: "任务名称不能为空", trigger: "change" },
      {
        min: 2,
        max: 50,
        message: "任务名称长度在 2 到 50 个字符",
        trigger: "blur",
      },
    ],
    userGroupIds: [
      { required: true, message: "请选择用户组", trigger: "change" },
      {
        validator: (rule, value, callback) => {
          if (!value || value.length === 0) {
            callback(new Error("请选择用户组"));
          } else {
            callback();
          }
        },
        trigger: "blur",
      },
    ],
    fileName: [
      { required: true, message: "文件名称不能为空", trigger: "blur" },
    ],
    fileSize: [
      { required: true, message: "大小要求不能为空", trigger: "blur" },
      {
        validator: (rule, value, callback) => {
          if (value !== null && value !== undefined && value <= 0) {
            callback(new Error("大小要求必须大于0"));
          } else {
            callback();
          }
        },
        trigger: "blur",
      },
    ],
    uploadStart: [
      {
        required: true,
        validator: (rule, value, callback) => {
          const index = rule.field.split(".")[1];
          const item = form.value.fileTaskConfigList[index];
          if (!item.perminate) {
            if (!value) {
              callback(new Error("允许上传时间不能为空"));
            } else if (
              item.uploadEnd &&
              new Date(value) > new Date(item.uploadEnd)
            ) {
              callback(new Error("允许上传时间不能大于上传截止时间"));
            } else {
              callback();
            }
          } else {
            callback();
          }
        },
        trigger: "change",
      },
    ],
    uploadEnd: [
      {
        required: true,
        validator: (rule, value, callback) => {
          const index = rule.field.split(".")[1];
          const item = form.value.fileTaskConfigList[index];
          if (!item.perminate) {
            if (!value) {
              callback(new Error("上传截止时间不能为空"));
            } else if (
              item.uploadStart &&
              new Date(value) < new Date(item.uploadStart)
            ) {
              callback(new Error("上传截止时间不能小于允许上传时间"));
            } else {
              callback();
            }
          } else {
            callback();
          }
        },
        trigger: "change",
      },
    ],
    fileList: [{ required: true, message: "请上传文件", trigger: "change" }],
    templateFileList: [
      { required: true, message: "请上传模板文件", trigger: "change" },
    ],
    fileType: [
      { required: true, message: "请选择允许的文件类型", trigger: "change" },
      {
        validator: (rule, value, callback) => {
          if (!value || value.length === 0) {
            callback(new Error("请选择允许的文件类型"));
          } else {
            callback();
          }
        },
        trigger: "change",
      },
    ],
    annoucement: [
      { required: true, message: "上传须知不能为空", trigger: "blur" },
    ],
  },
});

const { queryParams, form, rules, userData, selectedRowsOld, selectedUsers } =
  toRefs(data);

function reset() {
  form.value = {
    remark: null,
    id: "",
    taskName: "",
    userGroupIds: [],
    taskStatus: "1",
    fileTaskConfigList: [],
  };
}
// 打开弹窗
function openDialog(rows = null, bool = false) {
  isdisabled.value = bool;
  let row = JSON.parse(JSON.stringify(rows));
  open.value = true;
  if (bool) {
    title.value = "任务详情";
  } else {
    title.value = row && row.id ? "编辑任务" : "创建新任务";
  }
  selectedUsers.value = [];
  let userGroupName = [];

  if (row) {
    if (row.userGroupIds) {
      row.userGroupIds = row.userGroupIds.split(",");
    }
    if (row.userGroupNames) {
      userGroupName = row.userGroupNames.split(",");
    }
    row.userGroupIds.forEach((item, index) => {
      selectedUsers.value.push({
        id: item,
        name: userGroupName[index],
      });
    });
    if (row.fileTaskConfigList) {
      row.fileTaskConfigList.forEach((item) => {
        item.fileType = item.fileType.split(",");
        if (item.tempFile) {
          item.fileList = [{ name: item.tempFileName, url: item.tempFile }];
        }
        if (item.fileSize) {
          item.fileSize = Number(item.fileSize);
        }
      });
    }
    console.log(row, "获取信息");
    form.value = { ...row };
  }
  nextTick(() => {
    if (formRef.value) {
      console.log(formRef.value, "----");
      formRef.value.clearValidate();
    }
  });
}

function searchUserGroup() {
  // queryParams.value.userGroupName = value.value;
  getUserList();
}

/**获取用户列表 */
function getUserList() {
  // 实际项目中应调用API获取数据
  systemUserGroupMangerList(queryParams.value)
    .then((response) => {
      console.log(response, "获取用户信息");
      // loading.value = false
      if (response.code == 200) {
        userData.value = response.rows;
        total.value = response.total;
        handleAddSelectedUsers();
      }
    })
    .catch(() => {
      // loading.value = false;
    });
}

/**选中用户 */
function handleSelectionChange(arr, val) {
  let isShowIndex = selectedUsers.value.findIndex((i) => i.id == val.id);
  if (isShowIndex == -1) {
    selectedUsers.value.push(val);
  } else {
    selectedUsers.value.splice(isShowIndex, 1);
  }
  // console.log(selectedUsers.value);
}

/**删除选中的用户 */
function handleClose(item, index) {
  console.log(item, index);
  selectedUsers.value.splice(index, 1);
  multipleTableRef.value.toggleRowSelection(item, undefined);
}
/*给表格添加选中的用户 */
function handleAddSelectedUsers() {
  console.log(selectedUsers.value, "======");
  nextTick(() => {
    selectedUsers.value.forEach((item) => {
      multipleTableRef.value.toggleRowSelection(item, undefined);
    });
  });
}
/**清空选中的用户 */
function handleClear() {
  multipleTableRef.value.clearSelection();
  selectedUsers.value = [];
}
/**添加模块 */
function hadleAdd(taskType) {
  let uploadEnd = null;
  let uploadEndLoop = true;
  if (form.value.fileTaskConfigList.length !== 0) {
    form.value.fileTaskConfigList.forEach((item) => {
      if (item.uploadEnd && uploadEndLoop) {
        uploadEnd = item.uploadEnd;
        uploadEndLoop = false;
      }
    });
  }

  form.value.fileTaskConfigList.push({
    percentage: 0,
    remark: null,
    id: null,
    taskId: null,
    taskType: taskType,
    fileName: "",
    fileSize: null,
    uploadStart: null,
    uploadEnd: uploadEnd,
    tempFile: "",
    fileType: [],
    annoucement: "",
    perminate: false,
    delFlag: "0",
    fileList: [],
  });

  console.log(form.value.fileTaskConfigList, "======添加");

  nextTick(() => {
    if (dialogContentRef.value) {
      dialogContentRef.value.scrollTop = dialogContentRef.value.scrollHeight;
    }
  });
}
/**自定义上传文件 */
function handleHttpRequest(config, item, index) {
  console.log(config, "======自定义上传文件");
  uploadOssFile(config.file, { bizSign: "race", bizCode: "77" }, (p) => {
    item.percentage = Math.floor(p * 100);
  }).then((res) => {
    if (!item.fileList) {
      item.fileList = [];
    }

    item.fileList = [
      {
        name: res.fileName,
        url: res.ossUrl,
      },
    ];
    item.tempFile = item.fileList.map((f) => f.url).join(",");
    item.tempFileName = item.fileList.map((f) => f.name).join(",");
    nextTick(() => {
      if (formRef.value) {
        formRef.value.clearValidate([
          "fileTaskConfigList." + index + ".fileList",
        ]);
      }
    });
  });
}

/**上传成功回调 */
function handleUploadSuccess(res, file, item, index) {
  res = replaceFileOrigin(res);
  console.log(res, "上传成功文件");
  if (res.code === 200) {
    if (!item.fileList) {
      item.fileList = [];
    }
    item.fileList = [
      {
        name: file.name,
        url: res.data,
      },
    ];
    item.tempFile = item.fileList.map((f) => f.url).join(",");
    item.tempFileName = item.fileList.map((f) => f.name).join(",");
    nextTick(() => {
      if (formRef.value) {
        formRef.value.clearValidate([
          "fileTaskConfigList." + index + ".fileList",
        ]);
      }
    });
  } else {
    console.error("上传失败:", res.msg);
    ElMessage.error("文件上传失败");
    uploadRef.value[index].clearFiles();
  }
}
/**上传失败回调 */
function handleUploadError(err) {
  console.error("上传文件失败", err);
}
/**永久有效变更 */
function handlePerminateChange(item) {
  if (item.perminate) {
    item.uploadStart = "";
    item.uploadEnd = "";
  }
}
/**删除模块 */
function handleDelete(index) {
  form.value.fileTaskConfigList.splice(index, 1);
}

/**上传文件预览 */
function handlePreview(file) {
  console.log("预览文件", file);
}
/**移除文件 */
function handleRemove(item, index) {
  if (item) {
    item.fileList = [];
    uploadRef.value[index].clearFiles();
    nextTick(() => {
      if (formRef.value) {
        formRef.value.validateField([
          "fileTaskConfigList." + index + ".fileList",
        ]);
      }
    });
  }
}
/**移除前确认 */
function beforeRemove(file, fileList) {
  return true;
}
/**超出文件限制 */
function handleExceed(files, fileList) {
  ElMessage.warning(`最多只能上传1个文件`);
}

function submit(status) {
  formRef.value.validate((valid) => {
    if (valid) {
      if (form.value.fileTaskConfigList.length === 0) {
        ElMessage.warning("请至少添加一个任务配置");
        return;
      }

      form.value.taskStatus = status;

      const submitData = JSON.parse(JSON.stringify(form.value));
      submitData.userGroupIds = submitData.userGroupIds.join(",");
      submitData.fileTaskConfigList.forEach((item) => {
        delete item.fileList;
        delete item.templateFileList;
        if (Array.isArray(item.fileType)) {
          item.fileType = item.fileType.join(",");
        }
      });
      if (form.value.id) {
        console.log(submitData, "修改的数据");

        submitData.fileTaskConfigList.forEach((item) => {
          item.taskId = submitData.id;
        });
        editFileTask(submitData).then((res) => {
          if (res.code === 200) {
            ElMessage.success("修改成功");
            open.value = false;
            emit("refresh");
            reset();
            formRef.value.resetFields();
          } else {
            ElMessage.error(res.msg || "操作失败");
          }
        });
      } else {
        saveFileTask(submitData).then((res) => {
          if (res.code === 200) {
            ElMessage.success("操作成功");
            open.value = false;
            reset();
            formRef.value.resetFields();
            emit("refresh");
          } else {
            ElMessage.error(res.msg || "操作失败");
          }
        });
      }
    }
  });
}
/**选择用户确定 */
function submitUser() {
  form.value.userGroupIds = selectedUsers.value.map((i) => i.id);
  form.value.userGroupNames = selectedUsers.value.map((i) => i.name).join(",");
  openShow.value = false;
}
/**选择用户取消 */
function clearUser() {
  // reset();
  selectedUsers.value = [];
  multipleTableRef.value.clearSelection();
  openShow.value = false;
}
/**用户关闭弹窗 */
function dialogClose() {
  reset();
  open.value = false;
  formRef.value.resetFields();
}

/*打开用户弹窗*/
function openShowclick() {
  openShow.value = true;
  getUserList();
}
defineExpose({
  openDialog,
});
</script>

<style scoped lang="scss">
.dialog-footer {
  display: flex;
  justify-content: end;
  align-items: center;
}

.box-card {
  margin: 0 20px 10px 20px;
  // margin-bottom: 15px;
}

.clearfixClass {
  width: 100%;
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

:deep(.el-tag__content) {
}

:deep(.el-table__header-wrapper .el-checkbox) {
  display: none;
}

.box-card:deep(.el-form-item__label) {
  line-height: 32px;
  margin-bottom: 0;
}
.uploasdList {
  width: 500px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
