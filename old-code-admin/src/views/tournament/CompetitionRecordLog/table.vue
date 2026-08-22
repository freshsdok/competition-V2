<template>
<el-table :data="data">
    <el-table-column label="参赛人名称" align="left" prop="userName" min-width="60">
      <template #default="scope">
        <span :class="{'lightActive':lightActive('userName',scope.row)}">{{ scope.row.userName || '-' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="角色" align="left" prop="competitionRoleName" 
                      min-width="50">
      <template #default="scope">
        <span :class="{'lightActive':lightActive('competitionRoleName',scope.row)}">{{ scope.row.competitionRoleName || '-' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="性别" align="left" prop="sex" 
                      min-width="40">
      <template #default="scope">
        <span :class="{'lightActive':lightActive('sex',scope.row)}">{{ scope.row.sex === 1 ? '男' : '女' || '-' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="身份证" align="left" prop="idCard" 
                      min-width="120" >
      <template #default="scope">
        <span :class="{'lightActive':lightActive('idCard',scope.row)}">{{ scope.row.idCard || '-' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="邮箱" align="left" prop="email" min-width="80" >
      <template #default="scope">
        <span :class="{'lightActive':lightActive('email',scope.row)}">{{ scope.row.email || '-' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="手机号" align="left" prop="phone" min-width="80" >
      <template #default="scope">
        <span :class="{'lightActive':lightActive('phone',scope.row)}">{{ scope.row.phone || '-' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="机构/学校名称" align="left" prop="schoolName" min-width="80" >
      <template #default="scope">
        <span :class="{'lightActive':lightActive('schoolName',scope.row)}">{{ scope.row.schoolName || '-' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="团队名称" align="left" prop="teamName" min-width="120" >
      <template #default="scope">
        <span :class="{'lightActive':lightActive('teamName',scope.row)}">{{ scope.row.teamName || '-' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="带队老师" align="left" prop="leaderTeacher" min-width="60" >
      <template #default="scope">
        <span :class="{'lightActive':lightActive('leaderTeacher',scope.row)}">{{ scope.row.leaderTeacher || '-' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="赛事名称" align="left" prop="competitionName" min-width="80" >
      <template #default="scope">
        <span :class="{'lightActive':lightActive('competitionName',scope.row)}">{{ scope.row.competitionName || '-' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="赛道名称" align="left" prop="competitionTrackName" min-width="80" >
      <template #default="scope">
        <span :class="{'lightActive':lightActive('competitionTrackName',scope.row)}">{{ scope.row.competitionTrackName || '-' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="组别名称" align="left" prop="secondLevelName" min-width="80" >
      <template #default="scope">
        <span :class="{'lightActive':lightActive('secondLevelName',scope.row)}">{{ scope.row.secondLevelName || '-' }}</span>
      </template>
    </el-table-column>  
  </el-table>
</template>

<script setup name="CompetitionRecordLogTable">
// 定义组件属性
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  data: {
    type: [Array, Object,String],
    default: () => ([])
  },
  showDiff: {
    type: [Boolean,String],
    default: () => (false)
  },
})

function lightActive(key,row) { 
  if(!props.showDiff){
    return false
  }
  if(!row.keys || !row.keys.length){
    return false
  }
  return row.keys.includes(key)
}

</script>

<style scoped lang="scss">
:deep(.lightActive) {
  color: red;
}
</style>