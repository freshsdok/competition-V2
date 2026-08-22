<template>
  <PageBanner :banner="bannerSrc" title="大赛证书查询" />
  <div class="global-page pt-[10px]">
    <div class="container-custom font-sans pb-[60px]">
      <div class="bg-[#fff] pb-[65px]">
        <el-tabs v-model="activeName">
          <el-tab-pane label="证书查询" name="证书查询" />
        </el-tabs>
        <!-- 搜索条件 -->
        <div class="pr-[15px]">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="demo-form-inline" :rules="rules">
            <el-form-item label="查询方式">
              <el-radio-group v-model="queryParams.queryType">
                <el-radio-button value="PERSON">按人员查询</el-radio-button>
                <el-radio-button value="ORGANIZATION">按单位名称查询</el-radio-button>
                <el-radio-button value="CERT_CODE">按证书编号查询</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item
              v-if="queryParams.queryType === 'PERSON'"
              label="学校"
              prop="schoolName"
            >
              <el-input v-model.trim="queryParams.schoolName" placeholder="请输入学校名称" clearable />
            </el-form-item>
            <el-form-item
              v-if="queryParams.queryType === 'PERSON'"
              label="获证人姓名"
              prop="userName"
            >
              <el-input v-model.trim="queryParams.userName" placeholder="请输入获证人姓名" clearable />
            </el-form-item>
            <el-form-item
              v-if="queryParams.queryType === 'ORGANIZATION'"
              label="单位名称"
              prop="schoolName"
            >
              <el-input v-model.trim="queryParams.schoolName" placeholder="请输入获奖单位名称" clearable />
            </el-form-item>
            <el-form-item
              v-if="queryParams.queryType === 'CERT_CODE'"
              label="证书编号"
              prop="certCode"
            >
              <el-input v-model.trim="queryParams.certCode" placeholder="请输入完整证书编号" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery" :disabled="loading" >查询</el-button>
              <el-button @click="handleReset" :disabled="loading">重置</el-button>
            </el-form-item>
          </el-form>
          <div class="query-tip">{{ queryTip }}</div>
          <!-- 列表数据 -->
          <el-table :data="tableData" 
                      class="w-[100%] mt-[10px]" 
                      border 
                      v-loading="loading" 
                      :empty-text="emptyText"
            :header-cell-style="{ background: '#F9FAFB', color: '#64666A' }">
            <el-table-column prop="schoolName" label="获证单位" min-width="150">
              <template #default="{ row }">
                {{ row.schoolName || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="certName" label="证书名称" min-width="320" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.certName || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="competitionSeriesName" label="届数" min-width="100" >
              <template #default="{ row }">
                {{ row.competitionSeriesName || '' }}{{ row.competitionName || '' }}
              </template>
            </el-table-column>
            <el-table-column prop="competitionTrackName" label="赛区/组别" min-width="180" >
              <template #default="{ row }">
                {{ row.competitionTrackName || '-' }}{{ row.secondLevelName ? '/' + row.secondLevelName : '' }}
              </template>
            </el-table-column>
            <el-table-column prop="awardsName" label="奖项" min-width="120" >
              <template #default="{ row }">
                {{ getAwardsLabel(row.awardsName) }}
              </template>
            </el-table-column>
            <el-table-column prop="player" label="获证人/团队/单位" min-width="150">
              <template #default="{ row }">
                {{ row.player || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="guideTeacher" label="指导教师" min-width="110">
              <template #default="{ row }">
                {{ row.guideTeacher || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="certCode" label="证书编号" min-width="180">
              <template #default="{ row }">
                {{ row.certCode || '-' }}
              </template>
            </el-table-column>
          </el-table>
          <!-- 底部下载链接 -->
          <div class="text-center mt-[30px]" v-if="tableData.length > 0">
            <a href="https://cx.miitec.cn/certificateSearch?type=4" target="_blank" class="text-[#409EFF] hover:underline">
              点击跳转工信部人才交流中心证书查询平台
            </a>
          </div>
        </div>


      </div>
    </div>
  </div>
</template>

<script setup>
  // ********** 组件 ***********
  import { getCompetitionCertificateList } from "@/api/certInterconnect";
  import PageBanner from "@/components/PageBanner/index.vue"
  import bannerSrc from '@/assets/images/cert-inquiry.png'
  import { useDict } from "@/utils/dict";
  const { awards_name } = useDict("awards_name");
  // ********** 初始化 **********
  const activeName = ref('证书查询')
  const tableData = ref([])
  const loading = ref(false)
  const hasQueried = ref(false) // 标记是否已经查询过
  const queryFormRef = ref(null)
  const queryParams = ref({
    queryType: 'PERSON',
    schoolName: '',
    userName: '',
    certCode: '',
  })

  const queryTip = computed(() => {
    if (queryParams.value.queryType === 'ORGANIZATION') {
      return '用于查询优秀组织单位证书，请输入获奖单位名称。'
    }
    if (queryParams.value.queryType === 'CERT_CODE') {
      return '输入完整证书编号，可查询学生、团队、优秀指导教师和优秀组织单位证书。'
    }
    return '输入学校和获证人姓名，可查询学生个人、学生团队和优秀指导教师证书。'
  })

  const emptyText = computed(() => {
    if (!hasQueried.value) {
      return '请输入查询条件'
    }
    if (queryParams.value.queryType === 'ORGANIZATION') {
      return '未查询到相关证书，请检查单位名称是否正确'
    }
    if (queryParams.value.queryType === 'CERT_CODE') {
      return '未查询到相关证书，请检查证书编号是否正确'
    }
    return '未查询到相关证书，请检查学校名称和获证人姓名是否正确'
  })

  // 表单校验规则
  const rules = {
    schoolName: [
      { required: true, message: '请输入学校或单位名称', trigger: 'blur' },
      { min: 2, max: 100, message: '学校或单位名称长度应为2-100个字符', trigger: 'blur' }
    ],
    userName: [
      { required: true, message: '请输入获证人姓名', trigger: 'blur' },
      { min: 2, max: 100, message: '获证人姓名长度应为2-100个字符', trigger: 'blur' }
    ],
    certCode: [
      { required: true, message: '请输入证书编号', trigger: 'blur' },
      { min: 6, max: 255, message: '请输入正确的证书编号', trigger: 'blur' }
    ]
  }

  watch(() => queryParams.value.queryType, () => {
    queryParams.value.schoolName = ''
    queryParams.value.userName = ''
    queryParams.value.certCode = ''
    tableData.value = []
    hasQueried.value = false
    nextTick(() => queryFormRef.value?.clearValidate())
  })

  //  ********** 业务 **********
  // 获取奖项显示文本
  const getAwardsLabel = (awardsName) => {
    if (!awardsName) return '-'
    // 如果是数字或字符串数字，从字典数组中查找
    if (/^\d+$/.test(String(awardsName))) {
      const dictItem = awards_name.value?.find(item => item.value == awardsName)
      return dictItem?.label || awardsName
    }
    // 否则直接返回原值（中文奖项名称）
    return awardsName
  }
  // 搜索
  const handleQuery = async () => {
    if (!queryFormRef.value) return
    
    await queryFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true
        hasQueried.value = true // 标记已查询
        try {
          const { data } = await getCompetitionCertificateList(queryParams.value)
          tableData.value = data || []
        } catch (error) {
          console.error('查询失败:', error)
        } finally {
          loading.value = false
        }
      }
    })
  }

  const handleReset = () => {
    queryParams.value.schoolName = ''
    queryParams.value.userName = ''
    queryParams.value.certCode = ''
    tableData.value = []
    hasQueried.value = false
    queryFormRef.value?.clearValidate()
  }
</script>

<style lang="scss" scoped>
.global-page{
  background-color: #ffffff;
}

.query-tip {
  margin: -4px 0 12px;
  color: #909399;
  font-size: 13px;
}
  :deep(.el-tabs__nav .el-tabs__item) {
    font-size: 20px;
  }

  /* 文本溢出省略号样式 */
  .truncate-text {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: bottom;
  }
</style>
