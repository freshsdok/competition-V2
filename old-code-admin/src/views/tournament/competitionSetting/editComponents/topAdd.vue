<template>
<el-form ref="fromRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px" 
        :disabled="onlyShow"
        :inline="true">
  <el-row>
    <el-col :span="8">
      <el-form-item label="赛事名称" prop="competitionId" style="width: 100%;">
        <el-select v-model="form.competitionId" 
                  placeholder="请选择赛事名称" 
                  style="width: 100%;"
                  @change="changeSeriesID"
                  :disabled="isEdit">
          <el-option v-for="item in competitionSeriesArr" 
                      :key="item.competitionId" 
                      style="width: 100%;max-width: 600px;"
                      :label="item.competitionName"
                      :value="item.competitionId">
          </el-option>
        </el-select>
      </el-form-item>
    </el-col>
    <el-col :span="8">
      <el-form-item label="赛事届数" prop="competitionSeriesId" style="width: 100%;">
        <el-select v-model="form.competitionSeriesId" 
                  placeholder="请选择赛事届数" style="width: 100%;"
                   @change="changeCompetitionID"
                   :disabled="isEdit">
          <el-option v-for="item in competitionSeriesArr" 
                      :key="item.competitionSeriesId" 
                      style="width: 100%;max-width: 600px;"
                      :label="item.competitionSeriesName"
                      :value="item.competitionSeriesId">
          </el-option>
        </el-select>
      </el-form-item>
    </el-col>
    <el-col :span="8">
      <el-form-item label="赛道名称" prop="competitionTrackName" style="width: 100%;">
        <el-input v-model="form.competitionTrackName" placeholder="请输入赛道名称" type="text" :disabled="isEdit" />
      </el-form-item>
    </el-col>
    <el-col :span="8">
      <el-form-item label="组队规则" prop="checkPackageId" style="width: 100%;"> 
        <el-select v-model="form.checkPackageId" 
                  placeholder="请选择组队规则" 
                  style="width: 100%;">
          <el-option v-for="item in checkPackageList" 
                      :key="item.packageId" 
                      style="width: 100%;max-width: 600px;"
                      :label="item.packageName"
                      :value="item.packageId">
          </el-option>
        </el-select>
      </el-form-item>
    </el-col>
    <el-col :span="8">
      <el-form-item label="二级分类" prop="competitionTrackType" style="width: 100%;"> 
        <el-select v-model="form.competitionTrackType" 
                  placeholder="请选择二级分类" 
                  style="width: 100%;"
                  :disabled="isEdit">
          <el-option v-for="item in competitionTrackTypeArr" 
                      :key="item.value" 
                      style="width: 100%;max-width: 600px;"
                      :label="item.label"
                      :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
    </el-col>
    <el-col :span="8">
      <el-form-item label="二级分类名称" prop="secondLevelName" style="width: 100%;">
        <el-input v-model="form.secondLevelName" placeholder="请输入二级分类名称" type="text" />
      </el-form-item>
    </el-col>
    <el-col :span="8" style="padding-left: 3.5em;">
      <el-form-item v-if="!onlyShow" label="">
          <el-button type="primary" @click="submitForm" :loading="loading">新增</el-button>
      </el-form-item>
    </el-col>
  </el-row>
</el-form>
</template>

<script setup name="TopAddForm">
import { saveCompetitionTrack,getCompetitionPullDownList } from '@/api/tournament/competition';
import { watch } from 'vue';
const props = defineProps({
  onlyShow: {
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

// changeSeriesID/changeCompetitionID联动
const changeSeriesID = (val) => {
  let item = competitionSeriesArr.find(item => item.competitionId == val)
  if(item){
    form.competitionSeriesId = item.competitionSeriesId
  }
}
const changeCompetitionID = (val) => {
  let item = competitionSeriesArr.find(item => item.competitionSeriesId == val)
  if(item){
    form.competitionId = item.competitionId
  }
}

const emit = defineEmits(['addSuccess'])

// 计算是否为编辑模式
const isEdit = $computed(() => {
  // 编辑模式判断：如果 props.row 中存在 competitionTrackId，或者表单中已经保存了 competitionTrackId，则为编辑模式
  return (props.row && props.row.competitionTrackId) || form.competitionTrackId ? true : false
})

// 表单数据
let form = $ref({
  competitionSeriesId:'',
  competitionId:'',
  competitionTrackName: '',
  competitionTrackType: '',
  secondLevelName: '',
  checkPackageId: ''
})
/** 初始化表单数据 */
const initFormData = (val) => {
  let row = props.row
  form = {
    ...form,
    competitionSeriesId: row.competitionSeriesId || '',
    competitionId: row.competitionId || row.competitionName,
    competitionTrackName: row.competitionTrackName || '',
    competitionTrackType: row.competitionTrackType || '',
    secondLevelName: row.secondLevelName || '',
    checkPackageId: row.checkPackageId || '',
    competitionTrackId: row.competitionTrackId || ''
  }
}

watch(() => props.row, (val) => {
  if(val && val.competitionTrackId){
    initFormData(val)
  }
}, { immediate: true, deep: true })



// 表单验证规则
const rules = $computed(() => {
  return {
    competitionId: [{ required: true, message: "请选择赛事名称", trigger: "change" }],
    competitionSeriesId: [{ required: true, message: "请选择赛事届数", trigger: "change" }],
    competitionTrackName:  [{ required: true, message: "赛道名称不能为空", trigger: "blur" }],
    competitionTrackType: [{ required: true, message: "二级分类不能为空", trigger: "change" }],
    secondLevelName: [{ required: true, message: "二级分类名称不能为空", trigger: "blur" }]
  };
})

// 表单引用
let fromRef = ref(null)
// 加载状态
let loading = $ref(false)

/** 提交表单 */
const submitForm = () => {
  fromRef.value.validate((valid) => {
    if (valid) {
      loading = true
      postApi()
    }
  })
}

let competitionSeriesArr = $ref([])

/** 初始化下拉列表 */
const initCompetitionSeries = async () => {
  getCompetitionPullDownList().then(res => {
    if (res.code === 200) {
      competitionSeriesArr = res.data
    }
  })
}

/** 调用API新增或保存赛道 */
const postApi = async () => {
  try {
    const params = {
      competitionSeriesId: form.competitionSeriesId,
      competitionId: form.competitionId,
      competitionTrackName: form.competitionTrackName,
      competitionTrackType: form.competitionTrackType,
      secondLevelName: form.secondLevelName,
      checkPackageId: form.checkPackageId
    }
    // 如果有competitionTrackId，说明是编辑状态新增
    if(form.competitionTrackId){
      params.competitionTrackId = form.competitionTrackId
    }
    const res = await saveCompetitionTrack(params)
    if (res.code === 200) {
      // 新增成功，保存返回的competitionTrackId到表单中，以便下次新增时使用
      form.competitionTrackId = res.data.competitionTrackId
      form.secondLevelName = ''
      // 新增或保存成功，返回赛道数据
      emit('addSuccess', res.data)
    }
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    loading = false
  }
}

/** 重置表单 */
const resetForm = () => {
  fromRef.value.resetFields()
}

// 组件挂载时初始化数据
initCompetitionSeries()
</script>

<style scoped lang="scss">
.footer{
  text-align: right;
}

.option-label {
  width: 100%;
  overflow: hidden;
}

.truncate-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>