
<template>
  <div style="padding: 16px">
    <div class="gerenxinxi">个人信息</div>
    <el-form :model="xiangqing" label-width="180px" style="width: 600px">
      <el-form-item label="姓名"> {{ xiangqing.realName }} </el-form-item>
      <el-form-item label="身份证号"> {{ xiangqing.idCard }} </el-form-item>
    
    </el-form>
    <div class="gerenxinxi">认证信息</div>

    <el-form
      :model="xiangqing"
      label-width="180px"
      style="width: 600px"
      
    >
      <template v-if="xiangqing.certificationType == 'student'">
        <el-form-item label="所在学校" class="avataritemss">
          {{ xiangqing.schoolName||'-' }}
        </el-form-item>
        <el-form-item label="我的专业" class="avataritemss">
          {{ xiangqing.specialty||'-' }}
        </el-form-item>
        <el-form-item label="入学年份" class="avataritemss">
          {{ xiangqing.enrollmentYear||'-' }}
        </el-form-item>
        <el-form-item label="我的学号" class="avataritemss">
          {{ xiangqing.employeeCode||'-' }}
        </el-form-item>
        <el-form-item label="是否留学生" class="avataritemss">
          {{ xiangqing.isForeignStudent === 'Y' ? '是' : xiangqing.isForeignStudent === 'N' ? '否' : '-' }}
        </el-form-item>
        <el-form-item label="带队老师" class="avataritemss">
          {{ getTeacherName(xiangqing.teamLeader)||'-' }}
        </el-form-item>
        <el-form-item label="学生证" class="avatar-item">
          <el-image
            style="width: 100px"
            :src="xiangqing.studentCardId"
            :preview-src-list="[xiangqing.studentCardId]"
            fit="cover"
          />
        </el-form-item>
      </template>
      <template v-if="xiangqing.certificationType == 'teacher'">
        <el-form-item label="所在学校" class="avataritemss">
          {{ xiangqing.schoolName||'-' }}
        </el-form-item>
        <el-form-item label="所在学院" class="avataritemss">
          {{ xiangqing.institute||'-' }}
        </el-form-item>
        <el-form-item label="我的职位" class="avataritemss">
          {{ xiangqing.position||'-' }}
        </el-form-item>
        <el-form-item label="上传工作证" class="avatar-item">
          <el-image
            style="width: 100px; height: 100px"
            :src="xiangqing.workCardUrl"
            :preview-src-list="[xiangqing.workCardUrl]"
            fit="cover"
          />
        </el-form-item>
      </template>

      <template v-if="xiangqing.certificationType == 'school'">
        <el-form-item label="学校全称" class="avataritemss">
          {{ xiangqing.school||'-' }}
        </el-form-item>
        <el-form-item label="审批文号" class="avataritemss">
          {{ xiangqing.apprDocNumber||'-' }}
        </el-form-item>
        <el-form-item label="统一社会信用代码" class="avataritemss">
          {{ xiangqing.creditIdent||'-' }}
        </el-form-item>
        <el-form-item label="学校资质备案证书" class="avatar-item">
          <el-image
            style="width: 100px; height: 100px"
            :src="xiangqing.schoolCertUrl"
            :preview-src-list="[xiangqing.schoolCertUrl]"
            fit="cover"
          />
        </el-form-item>
      </template>
      <template v-if="xiangqing.certificationType == 'enterprise'">
        <el-form-item label="公司名称" class="avataritemss">
          {{ xiangqing.companyName||'-' }}
        </el-form-item>
        <el-form-item label="营业执照号" class="avataritemss">
          {{ xiangqing.bussLicenseNum||'-' }}
        </el-form-item>
        <el-form-item label="法人姓名" class="avataritemss">
          {{ xiangqing.legalPersName||'-' }}
        </el-form-item>
        <el-form-item label="法人身份证号" class="avataritemss">
          {{ xiangqing.legalIdCard||'-' }}
        </el-form-item>
        <el-form-item label="营业执照" class="avatar-item">
          <el-image
            style="width: 100px; height: 100px"
            :src="xiangqing.bussLicenseUrl"
            :preview-src-list="[xiangqing.bussLicenseUrl]"
            fit="cover"
          />
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>
<script setup>
import { schoollist, personalCentergetTeachers } from "@/api/system/process.js";
const props = defineProps({
  xiangqing: {
    type: Object,
    default: {},
  },
  certification_type: {
    type: Array,
  },

  class_info: {
    type: Array,
  },
});
const zhidaolaoshi=ref([])

// 根据教师ID获取教师姓名
const getTeacherName = (teacherId) => {
  if (!teacherId || !zhidaolaoshi.value) return '';
  const teacher = zhidaolaoshi.value.find(item => item.userId === teacherId);
  return teacher ? (teacher.realName ? teacher.realName : teacher.nickName) : '';
}

if (props.xiangqing.school) {
  const params = {
    id: props.xiangqing.school,
  };
  schoollist(params).then((res) => {
    props.xiangqing.schoolName = res.data[0].schoolName;
  });
}
if (props.xiangqing.certificationType=='student'&&props.xiangqing.school) {
  const params = {
    schoolId: props.xiangqing.school,
  };
  personalCentergetTeachers(params).then((res) => {
     zhidaolaoshi.value = res.data;
  });
}
</script>


<style scoped lang="scss">
.gerenxinxi {
  font-size: 16px;
  color: #000;
  margin-top: 20px;
  margin-bottom: 20px;
  background: #e9e6e6;
  padding: 10px 0 15px 30px;
}
.avatar-item {
  img {
    width: 100px;
    height: 100px;
  }
}
</style>