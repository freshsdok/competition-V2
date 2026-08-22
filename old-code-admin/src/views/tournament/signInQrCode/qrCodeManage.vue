<template>
  <div class="app-container">
    <!-- 返回按钮和搜索条件一行 -->
    <el-row :gutter="10" class="mb8" align="middle">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Back" @click="goBack">返回</el-button>
      </el-col>
      <el-col :span="20">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="60px" class="inline-form">
          <el-form-item label="学校" prop="schoolName">
            <el-input
              v-model.trim="queryParams.schoolName"
              placeholder="请输入"
              clearable
              style="width: 160px;"
            />
          </el-form-item>
          <el-form-item label="赛场" prop="examinationHall">
            <el-input
              v-model.trim="queryParams.examinationHall"
              placeholder="请输入"
              clearable
              style="width: 160px;"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="2" style="text-align: right;">
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-col>
    </el-row>

    <!-- 二维码列表 -->
    <el-table v-loading="loading" :data="qrCodeList" stripe>
      <el-table-column
        label="学校"
        align="left"
        prop="schoolName"
        min-width="150"
        show-overflow-tooltip
      />
      <el-table-column
        label="赛场"
        align="left"
        prop="examinationHall"
        min-width="120"
        show-overflow-tooltip
      />
      <el-table-column
        label="竞赛时间"
        align="center"
        prop="examTime"
        width="160"
      />
      <el-table-column
        label="二维码状态"
        align="center"
        prop="codeStatus"
        width="100"
      >
        <template #default="scope">
          <el-tag :type="scope.row.codeStatus === '1' ? 'success' : 'info'" size="small">
            {{ scope.row.codeStatus === '1' ? '已生成' : '未生成' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="160"
      />
      <el-table-column
        label="二维码预览"
        align="center"
        width="160"
      >
        <template #default="scope">
          <el-button
            type="text"
            :loading="loadingMap[scope.row.recordId]"
            @click="showPreview(scope.row.recordId)"
          >
            预览
          </el-button>
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

    <!-- 图片预览弹框 -->
    <el-dialog
      v-model="previewVisible"
      title="二维码预览"
      width="500px"
      append-to-body
      :close-on-click-modal="true"
    >
      <div style="display: flex; justify-content: center; align-items: center;">
        <img :src="previewImage" style="max-width: 100%; max-height: 400px;" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="QrCodeManage">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getQrCodeList, getCodeBase64 } from '@/api/tournament/signInQrCode'
import Pagination from '@/components/Pagination'
import RightToolbar from '@/components/RightToolbar'
import modal from '@/plugins/modal'

const route = useRoute()
const router = useRouter()

// 搜索显示控制
const showSearch = ref(true)
const queryRef = ref(null)

// 二维码列表
const loading = ref(false)
const qrCodeList = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  schoolName: undefined,
  examinationHall: undefined,
  codeConfigId: undefined
})

// 图片预览
const previewVisible = ref(false)
const previewImage = ref('')
const loadingMap = ref({})

// 从路由参数获取codeConfigId
onMounted(() => {
  const id = route.query.id
  if (id) {
    queryParams.value.codeConfigId = id
    getList()
  }
})

/** 查询列表 */
function getList() {
  if (!queryParams.value.codeConfigId) {
    modal.msgWarning('缺少必要的参数')
    return
  }
  loading.value = true
  getQrCodeList(queryParams.value).then(response => {
    qrCodeList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
    loading.value = false
  })
}

/** 搜索 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    schoolName: undefined,
    examinationHall: undefined,
    codeConfigId: queryParams.value.codeConfigId
  }
  getList()
}

/** 显示图片预览 */
async function showPreview(recordId) {
  loadingMap.value[recordId] = true
  try {
    getCodeBase64(recordId).then(response => {
      console.log(response,'xxx')
      previewImage.value = response.data?.codeBase64 || ''
    }).finally(() => {
      previewVisible.value = true
    })
  } catch (error) {
    modal.msgError('获取二维码失败')
  } finally {
    loadingMap.value[recordId] = false
  }
}

/** 返回 */
function goBack() {
  router.back()
}
</script>

<style scoped lang="scss">
.mb8 {
  margin-bottom: 8px;
}

.inline-form {
  margin-bottom: 0;

  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 10px;
  }
}

.qr-preview {
  display: flex;
  justify-content: center;
  align-items: center;

  img {
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: scale(1.1);
    }
  }
}
</style>
