<template>
      <el-form ref="contentRef" :model="form" :rules="rules" label-width="120px" :disabled="disabled" style="padding: 16px;">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="团队名称" prop="teamName">
              {{ form.teamName }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提交时间" prop="createTime">
              {{ moment(form.createTime).format('YYYY-MM-DD HH:mm:ss') }}
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <!-- <el-col :span="12">
            <el-form-item label="已加入队员" prop="teamMemberRelaList">
                <el-popover
                placement="bottom"
                title="已加入队员"
                :width="200"
                trigger="hover"
                :content="getAxisIdPropName(form.teamMemberRelaList)"
              >
                <template #reference>
                  <div class="getAxisIdPropName">{{ getAxisIdPropName(form.teamMemberRelaList) }}</div>
                </template>
              </el-popover>

            </el-form-item>
          </el-col> -->
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="队员数量" prop="teamNum" >
              {{ form.teamNum }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赛事名称" prop="competitionName" >
              {{ form.competitionName }}
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="赛事类型" prop="competitionType">

              <dict-tag :options="competitionTypeArr" :value="form.competitionType" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审核状态" prop="checkStatus">
              <dict-tag :options="checkStatus" :value="form.checkStatus" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付状态" prop="payStatus">
              <!-- {{ form.payStatus }}
              {{ payStatusArr }} -->
              <dict-tag :options="payStatusArr" :value="form.payStatus" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="赛道名称" prop="competitionTrackName">
              {{ form.competitionTrackName || '-'  }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组别" prop="secondLevelName">
              {{ form.secondLevelName || '-'  }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="带队老师姓名" prop="leaderTeacherName">
              {{ form.leaderTeacherName || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="队长姓名" prop="captainName">
              {{ form.captainName || '-' }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="指导教师" prop="guidTeacherList">
              <div v-if="form?.guidTeacherList">
                <div v-for="(item,index) in form.guidTeacherList" :key="index">
                  <span style="margin-right: 10px;">姓名：{{ item.guideTeacher || '-' }}</span>
                  <span style="margin-right: 10px;">手机号：{{ item.guideTeacherPhone || '-' }}</span>
                  <span>邮箱：{{ item.guideTeacherEmail || '-' }}</span>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="队员" prop="captainPhone">
            <div v-if="form?.teamMemberRelaList">
              <span v-for="(item,index) in form?.teamMemberRelaList" :key="index">
                <span>{{ item.userName || '-' }}</span>
                <span v-if="index < form?.teamMemberRelaList.length - 1">,</span>
              </span>
            </div>
            <div v-else>-</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="团队介绍" prop="teamDesc">
              {{ form.teamDesc || '-'  }}
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
</template>

<script setup name="TeamDetail">
import { cloneDeep } from 'lodash-es';
import moment from 'moment';
const props = defineProps({
  info: {
    type: [Array,Object],
    default: []
  },
  checkStatus: {
    type: [Array],
    default: []
  },
  payStatusArr: {
    type: Array,
    default: []
  },
  disabled: {
    type: Boolean,
    default: true
  },
  competitionTypeArr: {
    type: Array,
    default: []
  },
})
let rules = $ref({
  teamName: [
    { required: true, message: '请输入团队名称', trigger: 'blur' }
  ],
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
// 获取参赛真实名字
function getAxisIdPropName(e) {
  if (!e) return ''
  let arr = e.filter(item => item.userName).map(item => item.userName)
  return arr.join('，')
}
</script>
<style scoped lang="scss">
.getAxisIdPropName{
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  word-break: break-all;
}
</style>
