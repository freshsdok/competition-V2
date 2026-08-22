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
      <div v-loading="detailLoading">
        <el-tabs ref="tabsRef" 
                v-model="activeTab" 
                type="card" 
                tab-position="left">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" :name="1" >
            <Suspense>
              <template #default>
                <component :is="BaseInfoForm" 
                            ref="baseInfoFormRef"
                            :key="`base-${localCompetitionId}-${localCompetitionSeriesId}`"
                            :only-show="onlyShow"
                            :competitionId="localCompetitionId"
                            :competitionSeriesId="localCompetitionSeriesId"
                            :competition-type-arr="competitionTypeArr"
                            @changeNextTab="changeNextTab"/>
              </template>
              <template #fallback>
                <!-- 加载中的状态 -->
                <div class="Suspense-fallback">加载配置中...</div>
              </template>
          </Suspense>
          </el-tab-pane>

          <!-- 阶段设置 -->
          <el-tab-pane label="阶段设置" :name="2">
            <Suspense>
              <template #default>
                  <component :is="PhaseForm" 
                            ref="phaseFormRef"
                            :key="`phase-${localCompetitionId}-${localCompetitionSeriesId}`"
                            :score-way-arr="scoreWayArr"
                            :competitionId="localCompetitionId"
                            :only-show="onlyShow"
                        :competitionSeriesId="localCompetitionSeriesId"
                        @changeNextTab="changeNextTab"/>
              </template>
              <template #fallback>
                <!-- 加载中的状态 -->
                <div class="Suspense-fallback">加载配置中...</div>
              </template>
            </Suspense>
          </el-tab-pane>


          <!-- 赞助企业设置 -->
          <el-tab-pane label="赞助企业设置" :name="3">
            <Suspense>
              <component :is="CompanyForm" 
                              ref="companyFormRef"
                              :key="`company-${localCompetitionId}-${localCompetitionSeriesId}`"
                              :only-show="onlyShow"
                              :competitionId="localCompetitionId"
                              :competitionSeriesId="localCompetitionSeriesId" 
                              @changeNextTab="changeNextTab"/>
              <template #fallback>
                <!-- 加载中的状态 -->
                <div class="Suspense-fallback">加载配置中...</div>
              </template>
            </Suspense>
          </el-tab-pane>

          <!-- 详情内容展示 -->
          <el-tab-pane label="详情内容展示" :name="4">
            <Suspense>
              <template #default>
                <component :is="ShowContentForm" 
                              :only-show="onlyShow"
                              ref="showContentFormRef"
                              :key="`content-${localCompetitionId}-${localCompetitionSeriesId}`" 
                             :competitionId="localCompetitionId"
                             :competitionSeriesId="localCompetitionSeriesId"   
                        @changeNextTab="changeNextTab"/>
              </template>
              <template #fallback>
                <!-- 加载中的状态 -->
                <div class="Suspense-fallback">加载配置中...</div>
              </template>
            </Suspense>
          </el-tab-pane>
        </el-tabs>
      </div>
  </el-dialog>
</template>

<script setup name="TournamentCompetitionEditForm">
import { defineAsyncComponent, watch, ref } from 'vue'
import { useCompetitionDetail } from './editComponents/useCompetitionDetail';
import { resetCompetitionDetailState } from './editComponents/useCompetitionDetail';

// 动态导入独立组件，实现懒加载
const BaseInfoForm = defineAsyncComponent(() => import('./editComponents/BaseInfo1.vue'))
const PhaseForm = defineAsyncComponent(() => import('./editComponents/phase5.vue'))
const CompanyForm = defineAsyncComponent(() => import('./editComponents/company8.vue'))
const ShowContentForm = defineAsyncComponent(() => import('./editComponents/showContent9.vue'))

// 定义组件属性
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '赛事'
  },
  competitionId: {
    type: [Number, String],
    default: undefined
  },
  competitionSeriesId: {
    type: [Number, String],
    default: undefined  
  },
  competitionTypeArr: {
    type: [Array],
    default: []
  },
  scoreWayArr: {
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
  }
})

// 定义事件
const emit = defineEmits(['update:open', 'refresh', 'setCompetitionInfo'])

// 内部状态管理
let activeTab = $ref(1) // 当前激活的标签页
let { detailLoading } = useCompetitionDetail() // 详情加载状态

// 表单组件引用
let baseInfoFormRef = ref(null)
let phaseFormRef = ref(null)
let companyFormRef = ref(null)
let tabsRef = ref(null)

// 使用响应式数据存储competitionId和competitionSeriesId，以便动态更新
let localCompetitionId = $ref(props.competitionId)
let localCompetitionSeriesId = $ref(props.competitionSeriesId)

// 监听props变化，更新本地状态
watch(() => props.competitionId, (newVal) => {
  localCompetitionId = newVal
})

watch(() => props.competitionSeriesId, (newVal) => {
  localCompetitionSeriesId = newVal
})

/**
 * 对话框关闭前触发
 * @param {Function} done - 关闭对话框的回调函数
 */
function beforeDialogClose(done) {
  // 对话框关闭时重置数据
  resetCompetitionDetailState()
  done()
}

/**
 * 切换标签页
 * @param {Object} params - 切换参数
 * @param {String} params.type - 组件类型
 * @param {Boolean} params.resetId - 是否需要重置ID
 * @param {Object} params.data - 包含新ID的数据
 */
function changeNextTab({ type, resetId, data }) {
  // 任何组件提交成功后都可能返回新的ID
  if (resetId && data && data.competitionId && data.competitionSeriesId) {
    // 更新本地状态
    localCompetitionId = data.competitionId
    localCompetitionSeriesId = data.competitionSeriesId
    // 通知父组件更新ID
    emit('setCompetitionInfo', { 
      competitionId: data.competitionId, 
      competitionSeriesId: data.competitionSeriesId 
    })
  }
  emit('refresh')
}
</script>

<style scoped lang="scss">
.Suspense-fallback{
  text-align: center;
  padding: 20px;
}
</style>