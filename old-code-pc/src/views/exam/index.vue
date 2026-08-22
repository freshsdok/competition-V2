<template>
  <div class="global-page">
    <div class="container-custom font-sans pb-[60px]">
      <Breadcrumbar />
      <!-- 加载中状态 -->
      <div v-if="pageLoading" class="flex flex-col items-center justify-center py-[100px]">
        <el-icon class="is-loading text-[48px] text-[#409EFF]"><Loading /></el-icon>
        <p class="mt-4 text-[#909399]">加载中...</p>
      </div>
      <!-- 有数据时显示模块列表 -->
      <template v-else-if="configIdList && configIdList.length > 0">
        <div class="bg-[#fff] pb-[65px] mb-[20px]">
          <!-- 标题和新增按钮 -->
         <div class="flex justify-between items-center px-[20px] w-full">
           <div class="w-full flex justify-between items-center border-b border-[#EBEEF5]">
              <h2 class="text-[18px] font-medium text-[#303133] m-0 py-[15px] border-b-[2px] border-[#3169F8]">我的赛场</h2>
              <el-button type="primary" @click="handleAdd(activeCodeConfigId)">新增</el-button>
            </div>
         </div>
          <!-- Tabs -->
          <div class="exam-tabs-wrapper">
            <el-tabs v-model="activeCodeConfigId" class="exam-tabs" @tab-change="handleTabChange">
              <el-tab-pane 
                v-for="item in configIdList" 
                :key="item.codeConfigId"
                :label="`${item.competitionSeriesName || ''}${item.competitionName || ''}`" 
                :name="item.codeConfigId" 
              />
            </el-tabs>
          </div>
          <!-- 搜索条件 -->
          <div class="pl-[15px] pr-[15px]">
            <div class="flex justify-between items-center mb-6 search-box">
              <el-form :model="moduleDataMap[activeCodeConfigId].queryParams" :inline="true" class="demo-form-inline">
                <el-form-item label="学校" prop="schoolName" class="!mb-0">
                  <el-input v-model="moduleDataMap[activeCodeConfigId].queryParams.schoolName" placeholder="请输入学校名称" clearable />
                </el-form-item>
                <el-form-item label="赛场" prop="examinationHall"  class="!mb-0">
                  <el-input v-model="moduleDataMap[activeCodeConfigId].queryParams.examinationHall" placeholder="请输入赛场名称" clearable />
                </el-form-item>
                <el-form-item  class="!mb-0">
                  <el-button type="primary" @click="handleQuery(activeCodeConfigId)">搜索</el-button>
                  <el-button @click="handleReset(activeCodeConfigId)">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            <!-- 列表数据 -->
            <el-table :data="moduleDataMap[activeCodeConfigId].tableData" class="w-[100%]" border empty-text="暂无赛场信息" v-loading="moduleDataMap[activeCodeConfigId].loading" :header-cell-style="{ background: '#F9FAFB', color: '#64666A' }">
              <el-table-column prop="schoolName" label="学校" show-overflow-tooltip />
              <el-table-column prop="examinationHall" label="赛场" show-overflow-tooltip />
              <el-table-column prop="examTime" label="竞赛时间" show-overflow-tooltip />
              <el-table-column prop="codeStatus" label="二维码状态" show-overflow-tooltip>
                <template #default="{ row }">
                  <el-tag :type="row.codeStatus === '1' ? 'success' : 'info'">
                    {{ row.codeStatus === '1' ? '已生成' : '未生成' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" show-overflow-tooltip />
              <el-table-column label="操作" width="150" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handlePreview(row)" :loading="(previewLoading && (currentRow.recordId == row.recordId))">预览</el-button>
                  <el-button link type="primary" @click="handleEdit(row, activeCodeConfigId)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
            <!-- 分页组件 -->
            <div class="flex justify-end mt-[20px]">
              <pagination v-show="moduleDataMap[activeCodeConfigId].total > 0" :total="moduleDataMap[activeCodeConfigId].total" v-model:page="moduleDataMap[activeCodeConfigId].queryParams.pageNum"
                v-model:limit="moduleDataMap[activeCodeConfigId].queryParams.pageSize" @pagination="getList(activeCodeConfigId)" />
            </div>
          </div>
        </div>
      </template>
      <!-- 空数据状态 -->
      <div v-else class="bg-[#fff] rounded-[4px] py-[100px] px-[20px]">
        <el-empty description="暂无赛场数据">
        </el-empty>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学校" prop="schoolName">
          <el-input v-model="form.schoolName" placeholder="请输入学校名称" />
        </el-form-item>
        <el-form-item label="赛场" prop="examinationHall">
          <el-input v-model="form.examinationHall" placeholder="请输入赛场名称" />
        </el-form-item>
        <el-form-item label="竞赛时间" prop="examTime">
          <el-date-picker v-model="form.examTime" type="datetime" placeholder="可据实际场次填写竞赛时间，竞赛时间仅供选手参考" format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <div v-if="isAdd" class="text-orange-500 text-sm mt-2 ml-[100px]">
        保存成功后即可生成赛场对应的二维码
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 二维码预览弹窗 -->
    <el-dialog v-model="qrDialogVisible" :title="`${currentRow?.schoolName || ''} ${currentRow?.examinationHall || ''}`"  width="400px" append-to-body center :loading="previewLoading">
      <div class="flex flex-col items-center justify-center py-4">
        <img v-if="currentRow.codeBase64" :src="currentRow.codeBase64" alt="二维码" class="w-[200px] h-[200px]" />
        <div v-else class="w-[200px] h-[200px] flex items-center justify-center bg-gray-100 text-gray-500">
          暂无二维码
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer flex justify-center gap-6">
          <el-button @click="qrDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleDownloadQrCode" :disabled="!currentRow.codeBase64">下载二维码</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Document } from '@element-plus/icons-vue'
import Breadcrumbar from "@/components/breadcrumbar.vue";
import { getToken } from "@/utils/auth";
import {
  getExamRoomList,
  addExamRoom,
  updateExamRoom,
  getExamRoomConfig,
  getExamRoomDetail
} from '@/api/exam/index'

// ********** 组件 ***********

// ********** 初始化 **********
const activeCodeConfigId = ref(null) // 当前激活的codeConfigId
const pageLoading = ref(false) // 页面加载状态

// 模块数据映射 - 每个codeConfigId对应独立的数据
const moduleDataMap = reactive({})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isAdd = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const currentCodeConfigId = ref(null) // 当前操作的codeConfigId
const form = reactive({
  recordId: null,
  schoolName: '',
  examinationHall: '',
  examTime: ''
})

// 表单校验规则
const rules = {
}

// 二维码弹窗相关
const qrDialogVisible = ref(false)
const currentRow = ref(null)

//  ********** 业务 **********

// 初始化模块数据
const initModuleData = (codeConfigId) => {
  if (!moduleDataMap[codeConfigId]) {
    moduleDataMap[codeConfigId] = {
      tableData: [], // 列表数据
      total: 0, // 总条数
      loading: false, // 加载状态
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        schoolName: '',
        examinationHall: '',
      }
    }
  }
}

// 获取列表数据
const getList = async (codeConfigId) => {
  // 确保模块数据已初始化
  initModuleData(codeConfigId)

  moduleDataMap[codeConfigId].loading = true
  try {
    const res = await getExamRoomList({
      ...moduleDataMap[codeConfigId].queryParams,
      codeConfigId
    })
    if (res.code === 200) {
      moduleDataMap[codeConfigId].tableData = res?.rows || []
      moduleDataMap[codeConfigId].total = res.total || 0
    } else {
      ElMessage.error(res.msg || '获取列表失败')
    }
  } catch (error) {
    console.error('获取列表数据失败:', error)
    ElMessage.error('获取列表数据失败')
  } finally {
    moduleDataMap[codeConfigId].loading = false
  }
}

// 搜索
const handleQuery = (codeConfigId) => {
  initModuleData(codeConfigId)
  moduleDataMap[codeConfigId].queryParams.pageNum = 1
  getList(codeConfigId)
}

// 重置
const handleReset = (codeConfigId) => {
  initModuleData(codeConfigId)
  moduleDataMap[codeConfigId].queryParams.schoolName = ''
  moduleDataMap[codeConfigId].queryParams.examinationHall = ''
  moduleDataMap[codeConfigId].queryParams.pageNum = 1
  getList(codeConfigId)
}

// 新增
const handleAdd = async (codeConfigId) => {
  currentCodeConfigId.value = codeConfigId
  isAdd.value = true
  dialogTitle.value = '新增二维码'
  // 先关闭对话框（如果之前是打开状态）
  dialogVisible.value = false
  // 等待 DOM 更新后再重置表单并打开对话框
  await nextTick()
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row,codeConfigId) => {
  currentCodeConfigId.value = codeConfigId
  isAdd.value = false
  dialogTitle.value = '编辑赛场'
  resetForm()
  // 回显数据
  form.recordId = row.recordId
  form.schoolName = row.schoolName
  form.examinationHall = row.examinationHall
  form.examTime = row.examTime
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  // 先重置表单字段（清除校验状态）
  if (formRef.value) {
    formRef.value.resetFields()
  }
  // 再手动清空数据（确保数据被清空）
  form.recordId = null
  form.schoolName = ''
  form.examinationHall = ''
  form.examTime = ''
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (isAdd.value) {
          res = await addExamRoom({
            ...form,
            codeConfigId: currentCodeConfigId.value
          })
        } else {
          res = await updateExamRoom({
            ...form,
            codeConfigId: currentCodeConfigId.value
          })
        }

        if (res.code === 200) {
          ElMessage.success(isAdd.value ? '新增成功' : '修改成功')
          dialogVisible.value = false
          // 新增时刷新对应模块的列表
          getList(currentCodeConfigId.value)
        } else {
          ElMessage.error(res.msg || (isAdd.value ? '新增失败' : '修改失败'))
        }
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(isAdd.value ? '新增失败' : '修改失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 预览二维码
const previewLoading = ref(false)
const handlePreview = async (row) => {
 previewLoading.value = true
 currentRow.value = row
 getExamRoomDetail(row.recordId).then(res => {
  if(res.code === 200){
    qrDialogVisible.value = true
    currentRow.value = {
      ...row,
      codeBase64: res?.data?.codeBase64
    }
  } else {
    ElMessage.error(res.msg || '获取赛场详情失败')
  }
 }).finally(() => {
  previewLoading.value = false
 })
}

// 下载二维码
const handleDownloadQrCode = () => {
  if (!currentRow.value?.codeBase64) return

  const link = document.createElement('a')
  link.href = currentRow.value?.codeBase64
  link.download = `${currentRow.value?.schoolName || '赛场'}_${currentRow.value?.examinationHall || '二维码'}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

let configIdList = $ref([])

// Tab切换事件
const handleTabChange = (codeConfigId) => {
  activeCodeConfigId.value = codeConfigId
  // 如果该模块数据未加载，则加载数据
  if (!moduleDataMap[codeConfigId] || moduleDataMap[codeConfigId].tableData.length === 0) {
    initModuleData(codeConfigId)
    getList(codeConfigId)
  }
}

onMounted(async () => {
  if(getToken()){
    pageLoading.value = true
    try {
      const res = await getExamRoomConfig({})
      configIdList = res?.rows || []
      // 默认使用第一个codeConfigId
      if (configIdList.length > 0) {
        activeCodeConfigId.value = configIdList[0]?.codeConfigId || ''
        // 初始化第一个模块的数据并加载列表
        initModuleData(activeCodeConfigId.value)
        getList(activeCodeConfigId.value)
      }
    } catch (error) {
      console.error('获取赛场配置失败:', error)
      ElMessage.error('获取赛场配置失败')
    } finally {
      pageLoading.value = false
    }
  }
})
</script>

<style lang="scss" scoped>
:deep(.vxe-table--header) {
  margin: 15px;
  font-size: 20px;
}
.search-box {
  background: #FFFFFF;
  box-shadow: 0px 0px 4px 0px rgba(0,0,0,0.1) !important;
  border-radius: 2rpx 2rpx 2rpx 2rpx;
  padding: 20px;
}

/* Tabs 样式 */
.exam-tabs-wrapper {
  padding: 20px 15px;
  background-color: #fff;
}

.exam-tabs {
  :deep(.el-tabs__header) {
    margin: 0;
    border-bottom: none;
  }
  
  :deep(.el-tabs__nav) {
    border: none;
  }
  
  :deep(.el-tabs__nav-wrap) {
    &::after {
      display: none;
    }
  }
  
  :deep(.el-tabs__item) {
    height: 40px;
    line-height: 40px;
    padding: 4px 12px !important;
    margin-right: 8px;
    font-size: 14px;
    color: #999999;
    background: #F8F8F8;
    border: none;
    border-radius: 4px;
    transition: all 0.3s;
    font-weight: bold;
    &:hover {
      color: #FF8800;
    }
    &.is-active {
      color: #FF8800;
      background: #FFF8F1;
      border: 1rpx solid #FFC17A !important;
    }
  }
  
  :deep(.el-tabs__active-bar) {
    display: none;
  }
}
</style>
