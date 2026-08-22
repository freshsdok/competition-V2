<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
      label-width="100px"
    >
      <el-form-item label="赛事名称" prop="competitionName">
        <el-input
          v-model="queryParams.competitionName"
          placeholder="请输入赛事名称"
          clearable
          style="width: 160px"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="contentList" row-key="teamCode">
      <el-table-column
        label="链接url"
        align="center"
        prop="linkName"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column
        label="赛事名称"
        align="center"
        prop="competitionName"
        min-width="160"
      />
      <el-table-column
        label="赛事阶段名称"
        align="center"
        prop="stageName"
        min-width="160"
      />
      <el-table-column
        label="抽取码"
        align="center"
        prop="extractionCode"
        min-width="160"
      />
      <el-table-column
        label="抽取码到期时间"
        align="center"
        prop="extractionCodeTime"
        min-width="160"
    
      />
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)"
            >修改</el-button
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

    <!-- 添加或修改参数配置对话框 -->
    <el-dialog title="修改" v-model="open" width="600px" append-to-body>
      <el-form
        :model="form"
        ref="configRef"
        :rules="rules"
        label-width="140px"
        style="width: 500px"
      >
        <el-form-item label="抽取码到期时间" prop="extractionCodeTime">
          <el-date-picker
            clearable
            v-model="form.extractionCodeTime"
            type="date"
                 value-format="YYYY-MM-DD"
            placeholder="抽取码到期时间"
          >
          </el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open.value = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="InterlinkageIndex">
import { competitionWorkLinklist ,updateCompetitionWorkLinkInfo} from "@/api/system/config";
import { useDict } from "@/utils/dict";
import { ElMessage } from "element-plus";

const queryRef = ref(null);

const contentList = ref([]);

const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
});

const { queryParams, form } = toRefs(data);
/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  contentList.value = [];
  getList();
}
const getList = () => {
  competitionWorkLinklist(queryParams.value).then((res) => {
    contentList.value = res.rows;
    total.value = res.total;
    loading.value = false;
  });
};
/** 重置按钮操作 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields();
  }
  handleQuery();
}
const open = ref(false);
const handleEdit = (item) => {
  form.value = item;
  open.value = true;
};
const submitForm=()=>{
updateCompetitionWorkLinkInfo(form.value).then((res)=>{
    open.value = false;
    getList()
})
}
onMounted(() => {
  getList();
});

// 移除导出功能，因为当前页面不需要
</script>

<style scoped lang="scss">
.flex-center-input {
  display: flex;
  align-items: center;
  .c-line {
    margin: 0 6px;
  }
}
</style>
