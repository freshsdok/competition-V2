<template>
  <el-dialog
    v-model="visible"
    title="权限配置"
    width="75%"
    :before-close="beforeClose"
    :close-on-click-modal="false"
    destroy-on-close
    style="margin-bottom: 6vh;"
  >
    <el-tabs v-model="activeTab" type="card" class="permission-tabs">
      <el-tab-pane v-for="(item,index) in competition_operation_type" 
                   :label="item.label" 
                   :name="index" 
                   :key="item.value">
            <Suspense>
                 <InfoModify :competitionSeriesId="competitionSeriesId"
                              :competitionAlloweUserType="competition_allowe_user_type"
                              :operationType="item.value"
                              @submitForm="submitForm"
                              v-model:form="tabsApiList[index]"
                              v-model:submitLoading="submitLoading"/>
              <template #fallback>
                <!-- 加载中的状态 -->
                <div class="Suspense-fallback">加载配置中...</div>
              </template>
            </Suspense>
 
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script setup>
import { useDict } from '@/utils/dict'
import { getCompetitionOperationConfig, saveCompetitionOperationConfig, updateCompetitionOperationConfig } from "@/api/tournament/competition";
const {competition_operation_type,competition_allowe_user_type} = useDict("competition_operation_type","competition_allowe_user_type")
import { cloneDeep } from 'lodash-es';
import modal from "@/plugins/modal";
// 定义组件属性
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

// 定义事件
const emit = defineEmits(['update:visible'])
// 监听visible变化，更新父组件状态
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 当前激活的标签页
let activeTab = $ref(null)
// 初始化组件数据
let competitionSeriesId = $ref('')
let tabsApiList = $ref([])
function initDialog(csId) {
  if(!competition_operation_type.value || !competition_operation_type.value.length) {
    modal.msgWarning("请先配置“competition_operation_type”字典项")
    return
  }
  competitionSeriesId = csId
  getCompetitionOperationConfig(competitionSeriesId).then(res => {
    const apiData = res?.data || []
    // 为每个标签页预处理对应的数据
    tabsApiList = competition_operation_type.value.map((item, index) => {
      // 查找是否有对应的数据
      const existingData = apiData.find(apiItem => apiItem.operationType === item.value)
      let defaultUserTypes = competition_allowe_user_type.value && competition_allowe_user_type.value.length ? 
        [competition_allowe_user_type.value[0].value] : []
      if (existingData) {
        // 处理 modifyScope：API 返回字符串 '1' 或 '2'，转换为数组供 checkbox 使用
        let modifyScopeArray = []
        if (existingData.modifyScope === '1') {
          modifyScopeArray = ['1', '2'] // 选中 1 时自动包含 2
        } else if (existingData.modifyScope === '2') {
          modifyScopeArray = ['2']
        }
        return {
          ...existingData,
          allowedUserTypes: existingData?.allowedUserTypes?.split(',') || defaultUserTypes,
          allowedTimeRanges: existingData?.allowedTimeRanges ? JSON.parse(existingData.allowedTimeRanges) : [],
          modifyScope: modifyScopeArray
        }
      }
      // 如果没有对应数据，返回默认值
      return {
        allowedUserTypes: defaultUserTypes,
        modifyScope: item.value === '1' ? ['1', '2'] : []
      }
    })
    if(!activeTab){activeTab = 0}
  })
}

function beforeClose(done) {
  activeTab = 0
  tabsApiList = []
  done()
}

let submitLoading = $ref(false)
const submitForm = (form) => {
  let sendForm = cloneDeep(form)
  sendForm = {
    ...sendForm,
    operationType: competition_operation_type.value?.[activeTab]?.value || '',
    competitionSeriesId: competitionSeriesId
  }
  submitLoading = true
  if(sendForm?.id){
    updateCompetitionOperationConfig(sendForm).then(res => {
      modal.msgSuccess("更新配置成功")
      submitLoading = false
    }).catch(() => {
      submitLoading = false
    })
  }else{
    saveCompetitionOperationConfig(sendForm).then(res => {
      modal.msgSuccess("保存配置成功")
      submitLoading = false
      // 重新获取数据
      initDialog(competitionSeriesId)
    }).catch(() => {
      submitLoading = false
    })
  }
}

// 引入子组件
const InfoModify = defineAsyncComponent(() => import('./settingComponents/InfoModify.vue'))
defineExpose({ initDialog})
</script>

<style scoped lang="scss">
.Suspense-fallback{
  text-align: center;
  padding: 20px;
}
</style>