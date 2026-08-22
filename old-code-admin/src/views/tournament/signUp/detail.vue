<template>
      <el-form ref="contentRef" :model="form"  label-width="120px" :disabled="true">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="参赛者名称" prop="userName">
              {{ form.userName || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              {{ form.idCard || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              {{ form.phone || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              {{ form.email || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业/系别" prop="profession">
              {{ form.profession || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级" prop="classInfo">
              {{ form.classInfo || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="带队老师姓名" prop="leaderTeacherName">
              {{ form.leaderTeacherName || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="指导教师" prop="guideTeacher">
              {{ form.guideTeacher || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团队名称" prop="teamName">
              {{ form.teamName || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赛事名称" prop="competitionName">
              {{ form.competitionName || '-' }}  
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赛道" prop="competitionTrackName">
              {{ form.competitionTrackName || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组别" prop="secondLevelName">
              {{ form.secondLevelName || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报名时间" prop="registrationTime">
              {{formatDate(form.registrationTime)  || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付状态" prop="payStatus">
              <dict-tag :options="payStatusArr" :value="form.payStatus" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审核状态" prop="checkStatus">
              <dict-tag :options="checkStatus" :value="form.checkStatus" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
</template>

<script setup name="SignUpDetail">
import { cloneDeep } from 'lodash-es';
import { formatDate } from '@/utils/index.js'
const props = defineProps({
  info: {
    type: [Array,Object],
    default: []
  },
  professionalRequirements: {
    type: [Array],
    default: []
  },
  classRequest: {
    type: [Array],
    default: []
  },
  checkStatus: {
    type: [Array],
    default: []
  },
  joinTypeArr: {
    type: [Array],
    default: []
  },
  competitionTypeArr: {
    type: [Array],
    default: []
  },
  disabled: {
    type: Boolean,
    default: true
  },
  payStatusArr: {
    type: [Array],
    default: []
  },
  realNameAuthStatusArr: {
    type: [Array],
    default: []
  },
  competitionTrackArr: {
    type: [Array],
    default: []
  },
  competitionGroupArr: {
    type: [Array],
    default: []
  },
})
let form = $ref({})
watch(() => props.info, (newVal) => {
  if (newVal) {
    form = newVal
  }
}, { immediate: true,deep: true })
const emit = defineEmits(['update:info'])
watch(() => form, (newVal) => {
  if (newVal) {
    if(cloneDeep(newVal) !== cloneDeep(props.info)){
      emit('update:info', newVal)
    }
  }
}, { immediate: true,deep: true })
</script>
