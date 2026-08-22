<template>
  <div class="app-container">
    <el-row :gutter="24">
      <!--用户数据-->
      <el-col>
        <el-form
          :model="queryParams"
          ref="queryRefsss"
          :inline="true"
          label-width="100px"
        >
          <el-form-item label="认证状态" prop="checkStatus">
            <el-select
              v-model="queryParams.checkStatus"
              placeholder="请选择认证状态"
              clearable
              style="width: 240px"
              @change="handleQuery"
            >
              <el-option
                v-for="dict in identity_status"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>

          <!-- <el-form-item label="类型" prop="specialty">
            <el-select
              v-model="queryParams.specialty"
              placeholder="请选择认证类型"
              clearable
              style="width: 240px"
              @change="handleQuery"
            >
              <el-option
                v-for="dict in certification_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item> -->
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery"
              >搜索</el-button
            >
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="userList" style="width: 100%">
          <el-table-column label="序号" align="left" type="index" width="50" />
          <el-table-column
            label="用户姓名"
            align="center"
            key="realName"
            prop="realName"
          />
          <el-table-column
            label="认证类型"
            align="center"
            key="specialty"
            prop="specialty"
          >
            <template #default="scope">
              <dict-tag
                :options="certification_type"
                :value="scope.row.certificationType"
              />
            </template>
          </el-table-column>
           <!-- <el-table-column
            label="专业"
            align="center"
            key="identityTime"
            prop="specialty"
          >
          </el-table-column>
           <el-table-column
            label="证件号"
            align="center"
            key="identityTime"
            prop="idCard"
          >
          </el-table-column> -->
          <el-table-column
            label="认证时间"
            align="center"
            key="identityTime"
            prop="identityTime"
          >
          </el-table-column>
          <el-table-column
            label="认证状态"
            align="center"
            key="classInfo"
            prop="classInfo"
            :show-overflow-tooltip="true"
          >
            <template #default="scope">
              <dict-tag
                :options="identity_status"
                :value="scope.row.checkStatus"
              />
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            align="center"
            width="300"
            class-name="small-padding fixed-width"
          >
            <template #default="scope">
              <el-button link type="primary" @click="chakan(scope.row)"
                >查看</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </el-col>
    </el-row>

    <el-dialog :title="imgtitle" v-model="imgopen" width="850px">
      <edit :xiangqing="xiangqing" :certification_type="certification_type" v-if="imgopen"/>
    </el-dialog>
  </div>
</template>

<script setup name="User">
import {
  identityInfolist,
  getIdentityInfoDetail,
} from "@/api/system/process.js";
import edit from "./components/edit.vue";
const imgopen = ref(false);
const imgtitle = ref(null);
const xiangqing = ref({});

const { proxy } = getCurrentInstance();
const { identity_status, certification_type } = proxy.useDict(
  "identity_status",
  "certification_type"
);

const userList = ref([]);
const loading = ref(true);
const total = ref(0);
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
});
// 详情
/** 查询用户列表 */
function getList() {
  loading.value = true;

  identityInfolist(proxy.addDateRange(queryParams.value)).then((res) => {
    loading.value = false;
    userList.value = res.rows;
    total.value = res.total;
  });
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRefsss");
  queryParams.value.deptId = undefined;
  handleQuery();
}

const chakan = (row) => {
  getIdentityInfoDetail(row.authId).then((res) => {
    xiangqing.value = res.data;
    imgopen.value = true;
    imgtitle.value = "查看";
  });
};

onMounted(() => {
  getList();
});
</script>
<style lang="scss" scoped>
.userRefEditForm {
  :deep(.el-modal-dialog) {
    position: relative !important;

    .el-overlay-dialog {
      position: relative !important;

      .el-dialog {
        width: 100%;
        margin: 0 !important;

        .el-dialog__header {
          display: none;
          padding: 0;
        }
      }
    }
  }
}
</style>
