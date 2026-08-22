<template>
  <slot name="prefix" />
  <el-form-item :label="labelProp.name" prop="competitionSeriesId" v-if="labelProp.name">
    <el-select v-model="form.competitionSeriesId" placeholder="请选择赛事名称" clearable style="width: 180px"
      @change="handleSelectName">
      <el-option v-for="dict in selectCompetitionOptions" :key="dict.competitionSeriesId"
        :label="dict.competitionName" :value="String(dict.competitionSeriesId)" />
    </el-select>
  </el-form-item>
  <el-form-item :label="labelProp.sessionNum" prop="competitionSeriesName" v-if="labelProp.sessionNum">
    <el-input v-model="form.competitionSeriesName" placeholder="届数" disabled style="width: 180px" />
  </el-form-item>
  <el-form-item :label="labelProp.stage" prop="competitionStageId" v-if="labelProp.stage">
    <el-select v-model="form.competitionStageId" placeholder="请选择阶段" clearable style="width: 180px"
      @change="handleSelectStage">
      <el-option v-for="dict in stageOptions" :key="dict.stageId" :label="dict.stageName" :value="String(dict.stageId)" />
    </el-select>
  </el-form-item>
  <el-form-item :label="labelProp.track" prop="competitionTrackId" v-if="labelProp.track">
    <el-select v-model="form.competitionTrackId" placeholder="请选择赛道" clearable style="width: 180px"
      @change="handleSelectTrack">
      <el-option v-for="dict in secondaryTrackOptions" :key="dict.id" :label="dict.label" :value="String(dict.id)" />
    </el-select>
  </el-form-item>
  <el-form-item :label="labelProp.second" prop="secondLevelCode" v-if="labelProp.second">
    <el-select v-model="form.secondLevelCode" placeholder="请选择组别" clearable style="width: 180px"
      @change="handleSelectSecond">
      <el-option v-for="dict in thirdTrackOptions" :key="dict.id" :label="dict.label" :value="String(dict.id)" />
    </el-select>
  </el-form-item>
  <slot name="suffix" />
</template>

<script setup>

  // ******** api ********
  import { getSelectCompetitionList } from "@/api/certInterconnect/certConfig.js"

  // ******** Props ********
  const props = defineProps({
    modelValue: {
      type: Object,
      default: () => ({})
    },
    labelWidth: {
      type: String,
      default: '100px'
    },
    span: {
      type: Number,
      default: 8,
    },
    labelProp: {
      type: Object,
      default: () => ({
        'name': '赛事名称',
        'sessionNum': '届数',
        'stage': '阶段',
        'track': '赛道',
        'second': '组别'
      })
    },
    rules: {
      type: Object,
      default: () => ({})
    },
    inputWidth: {
      type: String,
      default: '180px'
    }
  })

  // ******** emit ********
  const emit = defineEmits(['update:modelValue'])

  // ******** 初始化 ********
  // 赛事字典
  const selectCompetitionOptions = ref([])

  const form = ref({})
  const isInitialFill = ref(false) // 监听赋值结束

  watch(() => props.modelValue, (formSource) => {
    if (formSource && typeof formSource === 'object' && Object.keys(formSource).length > 0) {
      form.value = formSource
    }
  }, { immediate: true, deep: true })

  // ==========================================
  // 级联下拉选项 computed（自动依赖，无需手动处理）
  // ==========================================
  // 阶段
  const stageOptions = computed(() => {
    const series = selectCompetitionOptions.value.find(
      item => item.competitionSeriesId == form.value?.competitionSeriesId
    )
    return series?.competitionStageConfigList || []
  })

  // 赛道
  const secondaryTrackOptions = computed(() => {
    const series = selectCompetitionOptions.value.find(
      item => item.competitionSeriesId == form.value?.competitionSeriesId
    )
    return series?.competitionChildren || []
  })

  // 组别
  const thirdTrackOptions = computed(() => {
    const track = secondaryTrackOptions.value.find(
      item => item.id == form.value?.competitionTrackId
    )
    return track?.children || []
  })


  // ******** 业务 ********
  // ================ 统一监听 下拉操作 ==================
  // 1. 监听赛事下拉
  const handleSelectName = (id) => {
    const item = selectCompetitionOptions.value.find(i => i.competitionSeriesId == id)
    form.value.competitionSeriesId = id;
    form.value.competitionSeriesName = item?.competitionSeriesName;
    form.value.competitionId = item?.competitionId;
    form.value.competitionName = item?.competitionName;
    form.value.competitionTrackId = undefined;
    form.value.competitionTrackName = undefined;
    form.value.secondLevelCode = undefined;
    form.value.secondLevelName = undefined;
    form.value.competitionStageId = undefined;
    form.value.competitionStageName = undefined;
    emit('update:modelValue', { ...form.value })
  }
  // 2. 监听阶段下拉
  const handleSelectStage = (id) => {
    const item = stageOptions.value.find(i => i.stageId == id)
    form.value.competitionStageId = id;
    form.value.competitionStageName = item?.stageName;
    emit('update:modelValue', { ...form.value })
  }
  // 3. 监听二级下拉
  const handleSelectTrack = (id) => {
    const item = secondaryTrackOptions.value.find(i => i.id == id)
    form.value.competitionTrackId = id;
    form.value.competitionTrackName = item?.label;
    form.value.secondLevelCode = undefined;
    form.value.secondLevelName = undefined;
    emit('update:modelValue', { ...form.value })
  }
  // 4. 监听三级下拉
  const handleSelectSecond = (id) => {
    const item = thirdTrackOptions.value.find(i => i.id == id)
    form.value.secondLevelCode = id;
    form.value.secondLevelName = item?.label;
    emit('update:modelValue', { ...form.value })
  }


  // =======================================
  // 获取赛事列表
  const getList = async () => {
    try {
      const { code, data } = await getSelectCompetitionList()
      if (code === 200) selectCompetitionOptions.value = data
    } catch (error) {
      console.error('获取赛事失败：', error)
    }
  }

  // 表单重置
  const reset = () => {
    try {
      form.value.competitionSeriesId = undefined;
      form.value.competitionSeriesName = undefined;
      form.value.competitionId = undefined;
      form.value.competitionName = undefined;
      form.value.competitionTrackId = undefined;
      form.value.competitionTrackName = undefined;
      form.value.secondLevelCode = undefined;
      form.value.secondLevelName = undefined;
      form.value.competitionStageId = undefined;
      form.value.competitionStageName = undefined;
      emit('update:modelValue', { ...form.value })
    } catch (error) {
      console.log(error);
    }
  }


  onMounted(() => {
    getList()
  })

  // 暴露方法给父组件 ✅
  defineExpose({
    reset, // 表单重置
  })
</script>

<style lang="scss" ></style>
