<template>
  <el-dialog :title="title" 
              :model-value="open" 
              @update:model-value="(val) => emit('update:open', val)" 
              width="80%" 
              style="margin-bottom: 6vh;"
              :before-close="beforeDialogClose"
              :modal="noDialog ? false : true"
              :modal-append-to-body="noDialog ? false : true"	
              :show-close="noDialog ? false : true"
              :class="noDialog ? 'show-preview-no-dialog' : ''"
              :append-to-body="noDialog ? false : true">
      <div class="content">
        <div>
          <TopAddForm ref="topAddFormRef" 
                      :competition-track-type-arr="competitionTrackTypeArr"
                      :row="diaLogRow"
                      :checkPackageList="checkPackageList"
                      :only-show="onlyShow"
                      @addSuccess="handleAddSuccess"/>
          <el-tabs  v-model="editableTabsValue"
                    class="demo-tabs"
                    :closable="!onlyShow"
                    @tab-change="handleTabChange"
                    @tab-remove="handleTabRemove">
            <el-tab-pane v-for="item in tabList" 
                          :key="item.competitionTrackConfigId" 
                          :label="item.secondLevelName"
                          :name="item.competitionTrackConfigId">
              <template #label>
                <template v-if="(editableTabsValue == item.competitionTrackConfigId) && !onlyShow">
                  <el-input v-model="item.secondLevelName" placeholder="请输入二级分类名称" type="text" 
                            class="tab-input"
                            @input.stop
                            @keydown.stop 
                            @keyup.stop 
                            @keypress.stop/>
                </template>
                <template v-else>
                  {{ item.secondLevelName }}
                </template>
              </template>
            </el-tab-pane>
          </el-tabs>
        </div>
        <div v-loading="detailLoading.value" class="bottom-content">
          <!-- 只有当tabList不为空时才显示表单内容，否则显示暂无数据 -->
          <template v-if="tabList.length > 0">
            <el-tabs ref="tabsRef" 
                    v-model="activeTab" 
                    type="card" 
                    tab-position="left">
              <!-- 个人/组队设置 -->
              <el-tab-pane label="个人/组队设置" :name="1">
                <Suspense>
                  <template #default>
                    <component :is="TeamSettingForm" 
                                ref="teamSettingFormRef"
                                :key="`team-${editableTabsValue}`"
                                :only-show="onlyShow"
                                :tab-code="editableTabsValue"
                                :join-type-arr="joinTypeArr"/>
                  </template>
                  <template #fallback>
                    <!-- 加载中的状态 -->
                    <div class="Suspense-fallback">加载配置中...</div>
                  </template>
                </Suspense>
              </el-tab-pane>

              <!-- 报名条件 -->
              <el-tab-pane label="报名条件设置" :name="2">
                <Suspense>
                  <template #default>
                    <component :is="RegistrationCriteriaForm" 
                                ref="registrationCriteriaFormRef"
                                :key="`reg-${editableTabsValue}`"
                                :class-request-arr="classRequestArr"
                                :only-show="onlyShow"
                                :tab-code="editableTabsValue"
                                :professional-requirements-arr="professionalRequirementsArr"/>
                  </template>
                  <template #fallback>
                    <!-- 加载中的状态 -->
                    <div class="Suspense-fallback">加载配置中...</div>
                  </template>
                </Suspense>
              </el-tab-pane>

              <!-- 作品设置 -->
              <el-tab-pane label="作品提交设置" :name="3">
                <Suspense>
                  <template #default>
                    <component :is="WorkSettingForm" 
                                  ref="workSettingFormRef"
                                  :key="`work-${editableTabsValue}`"
                                  :only-show="onlyShow"
                                  :tab-code="editableTabsValue"
                                  :works-submit-way-arr="worksSubmitWayArr" 
                              :file-format-restrictions-arr="fileFormatRestrictionsArr"/>
                  </template>
                  <template #fallback>
                    <!-- 加载中的状态 -->
                    <div class="Suspense-fallback">加载配置中...</div>
                  </template>
                </Suspense>
              </el-tab-pane>

              <!-- 赞助企业设置 -->
              <el-tab-pane label="赞助企业设置" :name="4">
                <Suspense>
                  <template #default>
                     <component :is="CompanyForm" 
                                  ref="companyFormRef"
                                  :key="`company-${editableTabsValue}`"
                                  :only-show="onlyShow"
                                  :tab-code="editableTabsValue" />
                  </template>
                  <template #fallback>
                    <!-- 加载中的状态 -->
                    <div class="Suspense-fallback">加载配置中...</div>
                  </template>
                </Suspense>
              </el-tab-pane>
            </el-tabs>
            <div class="bottom-footer" v-if="!onlyShow">
              <el-button type="primary" @click="handleSave" v-hasPermi="['competition:competitionTrackConfig:add']">暂存</el-button>
            </div>
          </template>
          <template v-else>
            <div class="no-data">
              <el-empty description="暂无数据，请先新增二级分类" />
            </div>
          </template>
        </div>
      </div>
  </el-dialog>
</template>

<script setup name="TournamentCompetitionEditForm">
import { defineAsyncComponent, onMounted } from 'vue'
import { useCompetitionDetail } from './editComponents/useCompetitionDetail';
import { ElMessageBox, ElMessage } from 'element-plus';
// 动态导入独立组件，实现懒加载
const RegistrationCriteriaForm = defineAsyncComponent(() => import('./editComponents/Registration4.vue'))
const TeamSettingForm = defineAsyncComponent(() => import('./editComponents/Team3.vue'))
const WorkSettingForm = defineAsyncComponent(() => import('./editComponents/work6.vue'))
const CompanyForm = defineAsyncComponent(() => import('./editComponents/company8.vue'))
const TopAddForm = defineAsyncComponent(() => import('./editComponents/topAdd.vue'))

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '赛事'
  },
  joinTypeArr: {
    type: [Array],
    default: []
  },
  classRequestArr: {
    type: [Array],
    default: []
  },
  professionalRequirementsArr: {
    type: [Array],
    default: []
  },
  worksSubmitWayArr: {
    type: [Array],
    default: []
  },
  fileFormatRestrictionsArr: {
    type: [Array],
    default: []
  },
  onlyShow: {
    type: Boolean,
    default: false
  },
  noDialog: {
    type: Boolean,
    default: false
  },
  competitionTrackTypeArr: {
    type: [Array],
    default: []
  },
  row: {
    type: Object,
    default: null
  },
  checkPackageList: {
    type: [Array],
    default: []
  },
})

let diaLogRow = $ref(null)
const emit = defineEmits(['update:open', 'refresh','setCompetitionInfo'])

// 二级分类标签页数据
let editableTabsValue = $ref('')

// 表单组件引用
let topAddFormRef = $ref(null)
let teamSettingFormRef = $ref(null)
let registrationCriteriaFormRef = $ref(null)
let workSettingFormRef = $ref(null)
let companyFormRef = $ref(null)
let tabsRef = ref(null)


// 定义tab页的顺序，用于判断前后关系
let activeTab = $ref(1)

// 赛事详情状态
const {detailLoading,updateTabList, fetchTabDetail, saveTabConfig, removeTab, resetState, setDetailFromRow, tabList, addTab} = useCompetitionDetail()
onMounted(() => { 
  if(props.row){
    diaLogRow = props.row
    // 使用setDetailFromRow初始化状态
    setDetailFromRow(props.row)
    // 或者直接使用updateTabList更新标签页列表
    updateTabList(props.row.competitionTrackConfigList || [])
    // 初始化editableTabsValue
    if(tabList.value && tabList.value.length > 0){
      editableTabsValue = tabList.value[0].competitionTrackConfigId
      // 一上来就请求第一个tabs的详情
      fetchTabDetail({
        tabCode: editableTabsValue
      })
    }
  }
})


/** 新增标签页成功 */
const handleAddSuccess = (row) => {
  // 使用useCompetitionDetail hook提供的addTab方法添加标签页
  addTab(row)
  editableTabsValue = row.competitionTrackConfigId
  // 获取新标签页的详情
  fetchTabDetail({
    tabCode: row.competitionTrackConfigId
  })
  activeTab = 1
  // 触发refresh事件，通知父组件刷新表格数据
  emit('refresh')
}

/** 切换标签页 */
const handleTabChange = (tabCode) => {
  // 切换标签页时，重置底部配置项为第一个（个人/组队设置）
  activeTab = 1
  // 获取标签页详情
  fetchTabDetail({
    tabCode: tabCode
  })
}

/** 删除标签页 */
const handleTabRemove = (tabCode) => {
  // 显示删除确认对话框
  ElMessageBox.confirm('确定要删除吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    // 调用API删除标签页
    await removeTab({
      tabCode: tabCode
    })
    
    // 如果删除的是当前激活的标签页，切换到第一个标签页
    if (editableTabsValue === tabCode && tabList.value.length > 0) {
      editableTabsValue = tabList.value[0].competitionTrackConfigId
      fetchTabDetail({
        tabCode: editableTabsValue
      })
    } else if (tabList.value.length === 0) {
      editableTabsValue = ''
    }
  }).catch(() => {
    // 用户取消删除
    console.log('用户取消删除标签页')
  })
}

/** 暂存 */
const handleSave = async () => {
  // 收集当前标签页的配置信息
  const currentTab = tabList.value.find(item => item.competitionTrackConfigId === editableTabsValue)
  if (currentTab) {
    try {
      // 1. 获取各个子表单的数据和验证结果，使用await等待异步验证完成
      const teamSettingResult = teamSettingFormRef && (activeTab == 1) ? await teamSettingFormRef.getFormData() : { valid: true, data: {} }
      const registrationSettingResult = registrationCriteriaFormRef && (activeTab == 2) ? await registrationCriteriaFormRef.getFormData() : { valid: true, data: {} }
      const workSettingResult = workSettingFormRef && (activeTab == 3) ? await workSettingFormRef.getFormData() : { valid: true, data: {} }
      const companySettingResult = companyFormRef && (activeTab == 4) ? await companyFormRef.getFormData() : { valid: true, data: {} }

      // 2. 检查所有组件的验证结果
      const formResults = [teamSettingResult, registrationSettingResult, workSettingResult, companySettingResult]
      if (formResults.some(result => !result.valid)) {
        return
      }
      
      // 3. 提取验证通过的数据
      const teamSetting = teamSettingResult.data
      const registrationSetting = registrationSettingResult.data
      const workSetting = workSettingResult.data
      const companySetting = companySettingResult.data
      
      // 4. 平铺所有表单数据
      const tabConfig = {
        secondLevelName: currentTab.secondLevelName,
        competitionTrackConfigId: currentTab.competitionTrackConfigId,
        competitionTrackId: currentTab.competitionTrackId,
        competitionConfig: {
          ...teamSetting,
          ...registrationSetting,
          ...workSetting
        },
        // 只有当 companySetting.competitionTrackEnterpriseList 存在且长度大于 0 时才添加
        ...(companySetting?.competitionTrackEnterpriseList?.length > 0 && {
          competitionTrackEnterpriseList: companySetting.competitionTrackEnterpriseList || []
        })
      }
      // 5. 调用API保存配置
      saveTabConfig(tabConfig,(res)=>{
        if(res.code === 200){ 
          ElMessage.success("暂存成功")
          // 触发refresh事件，通知父组件刷新表格数据
          emit('refresh')
        }else{
          ElMessage.error("暂存失败")
        }
      })
    } catch (error) {
      console.error('暂存失败:', error)
      ElMessage.error("暂存失败")
    }
  }
}

/** 对话框关闭前触发 */
function beforeDialogClose(done) {
  // 对话框关闭时重置数据
  resetState()
  done()
}
</script>

<style scoped lang="scss">
.Suspense-fallback{
  text-align: center;
  padding: 20px;
}
.demo-tabs{
  background: #f1f1f1;
  margin-bottom: 20px;
  :deep(.el-tabs__header) {
    margin-bottom: 0 !important;
    .is-active{

    }
    .el-tabs__item{
      padding: 0 20px !important;
    }
    .tab-input{
      min-width: 150px;
    }
  }
}
.bottom-content{
  position: relative;
}
.bottom-footer{
  margin-top: 20px;
  width: 100%;
  text-align: right;
  z-index: 10;
}
.content{
  height: 100%;
  display: flex;
  flex-direction: column;
}
.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  margin: 20px 0;
}
</style>