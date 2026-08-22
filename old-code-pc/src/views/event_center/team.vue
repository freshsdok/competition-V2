<template>
<div class="base-page">
  <div class="container-custom self-custom">
    <Breadcrumbar />
    <div class="page-content flex justify-between align-start">
      <div class="page-content-left">
        <div class="page-content-left-user flex justify-start align-center">
          <img alt="" :src="userInfo.avatar" v-if="userInfo.avatar" class="page-content-left-user-avatar">
          <img alt="" src="@/assets/images/avatar.png" v-else class="page-content-left-user-avatar">
          <div class="page-content-left-user-name flex flex-col justify-center items-center">
            <div class="use-name flex justify-center items-center">{{ userInfo?.nickName }}</div>
          </div>
        </div>
        <div class="page-content-left-title">我的团队</div>
      </div>
      <div class="page-content-right">
        <div class="top-bar">
          <div class="top-bar-text flex justify-center items-center">
            <span>我的团队</span>
            <div class="top-bar-line w-full bg-[#3169F8]"></div>
          </div>
        </div>
        <div class="btm-content">
          <el-tabs v-model="activeName" @tab-click="handleClick">
            <el-tab-pane label="创建团队" name="first">
              <div class="info-block flex justify-start items-center mt-6">队长信息</div>
              <div class="team-user-info">
                <MyInfo :value="userInfo"/>
              </div>
              <div class="info-block flex justify-start items-center mt-8">团队信息</div>
              <el-form ref="fromRef" 
                        :model="form" 
                        :validate-on-rule-change="false"
                        :rules="rules" 
                        v-loading="teamLoading"
                        :disabled="applyDisable"
                        label-width="120px" 
                        class="join-team-form">
                <el-form-item label="关联赛事"  required>
                  <div class="saiShiName" @click="handleClickSaiShi">{{ pageDetail.competitionName || '-' }}</div>
                </el-form-item>
                <el-form-item label="团队名称" prop="teamName">
                  <el-input v-model="form.teamName" placeholder="请输入团队名称" :disabled="(form.teamCode && form.checkStatus != '5') ? true : false"/>
                </el-form-item>
                <el-form-item label="团队说明" prop="teamDesc">
                  <el-input v-model="form.teamDesc" placeholder="请输入团队说明" type="textarea" :rows="5" maxlength="300" show-word-limit :disabled="(form.teamCode && form.checkStatus != '5') ? true : false"/>
                </el-form-item>
                <el-form-item label="">
                  <template v-if="form.teamCode">
                    <div class="teamCode flex justify-start items-center">
                      <!-- 2待审核3审核中4已通过5已拒绝 -->
                      <span>当前团队审核状态：</span>
                      <span :class="['check_status','check_status'+form.checkStatus]">{{ getDictLabel(form.checkStatus) }}</span>
                    </div>
                  </template>  
                  <el-button v-else type="primary"  @click="submitForm" class="w-btn bgcolor mt-4" v-loading="submitFormLoading">提交</el-button>       
                </el-form-item>
                <el-form-item v-if="form.teamCode">
                  <el-button v-if="form.checkStatus && form.checkStatus == '5'" type="primary" v-loading="submitFormLoading"  @click="submitForm()" class="w-btn bgcolor mt-4">重新提交</el-button> 
                </el-form-item>
                <el-form-item label="团队队长" v-if="form.checkStatus == '4'">
                  <div class="ml-3">{{ form.captainName || '-' }}</div>
                </el-form-item>
                <el-form-item label="团队成员"  v-if="(form.teamCode && form.checkStatus == '4') && form && form.teamLeaderId" >
                  <div class="w-full mt-[-4px]">
                    <el-collapse expand-icon-position="left">
                      <el-table :data="form.teamMemberRelaList" style="width: 100%">
                        <el-table-column type="expand" width="20px" class="expand-rows">
                          <template #default="scope">
                            <div class="pl-12 color-[#999] text-[14px]">
                              <p class="flex justify-start items-center"><p class="w-[100px]">手机号码:</p><span>{{ scope.row.phone || '-'  }}</span></p>
                              <p class="flex justify-start items-center"><p class="w-[100px]">邮箱：</p><span>{{ scope.row.email || '-'  }}</span></p>
                              <p class="flex justify-start items-center"><p class="w-[100px]">性别：</p><span>{{ scope.row.sex == '2' ? '男' : '女' || '-'  }}</span></p>
                              <p class="flex justify-start items-center"><p class="w-[100px]">学校/机构：</p><span>{{ scope.row.orgName || '-' }}</span></p>
                            </div>
                          </template>
                        </el-table-column>
                        <el-table-column prop="userName" label="成员名称" >
                          <template #default="scope">
                              <span>{{ scope.row.userName }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column prop="checkStatus" label="处理状态" >
                          <template  #default="scope">
                            <span v-if="scope.row.checkStatus == '1'" :class="['check_status','check_status_o'+scope.row.checkStatus]">待处理</span>
                            <span v-if="scope.row.checkStatus == '2'" :class="['check_status','check_status_o'+scope.row.checkStatus]">已同意加入</span>
                            <span v-if="scope.row.checkStatus == '3'" :class="['check_status','check_status_o'+scope.row.checkStatus]">已拒绝加入</span>
                          </template>
                        </el-table-column>
                        <el-table-column label="可进行操作">
                          <template  #default="scope">
                              <template v-if="scope.row.checkStatus == '1'">
                                <el-button type="danger" link @click="agreeJoinTeamAction(scope.row,'3')">拒绝</el-button>
                                <el-button type="primary" link @click="agreeJoinTeamAction(scope.row,'2')" class="bgcolor">同意</el-button>
                              </template>
                              <template v-if="scope.row.checkStatus == '2'">
                                <el-button type="danger"  link @click="agreeJoinTeamAction(scope.row,'3',true)">移除该成员</el-button>
                              </template>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-collapse>
                  </div>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="加入团队" name="second">
              <div class="all-team-list">
                <div class="flex justify-start items-center">
                   <el-select-v2  v-model="selectTeamCode"
                                  :options="allTeamList"
                                  filterable
                                  remote
                                  :remote-method="getAllTeamLists"
                                  :loading="teamSearchLoading"
                                  placeholder="请搜索或选择团队"
                                  class="max-w-96 mr-2"
                                />
                  <el-button type="primary"  @click="handleJoinTeam(null)" class="w-btn" :disabled="applyDisable">申请入队</el-button>
                </div>
                <div v-if="userJoinTeamList && userJoinTeamList.length > 0" class="mt-8">
                  <div class="text-lg mt-8">我的团队</div>
                  <div class="team-list-item mt-2" v-for="item in userJoinTeamList" :key="item.teamCode">
                    <div class="team-name"><span class="text-name-lable">团队名称：</span><span class="text-name-content">{{ item.teamName }}</span></div>
                    <div class="team-name"><span class="text-name-lable">队长名称：</span><span class="text-name-content">{{ item.captainName }}</span></div>
                    <div class="team-name team-name2"><span class="text-name-lable">团队说明：</span><span class="text-name-content text-name-des">{{ item.teamDesc }}</span></div>  
                    <div class="w-full flex justify-end items-center team-status" v-if="item.captainFlag">
                      <span :class="['check_status','check_status'+item.checkStatus]">{{ getDictLabel(item.checkStatus) }}</span>
                    </div>
                    <div class="w-full flex justify-end items-center team-status" v-else>
                      <span v-if="item.memberCheckStatus == '1'" :class="['check_status','check_status_o'+item.memberCheckStatus]">申请中</span>
                      <span v-if="item.memberCheckStatus == '2'" :class="['check_status','check_status_o'+item.memberCheckStatus]">已加入</span>
                      <span v-if="item.memberCheckStatus == '3'" :class="['check_status','check_status_o'+item.memberCheckStatus]">已拒绝</span>
                      <el-button v-if="item.memberCheckStatus == '3'" type="primary"  @click="handleJoinTeam(item)" class="w-btn ml-4" :disabled="applyDisable">重新申请</el-button>
                    </div>
                  </div>
                </div>
                <el-empty description="暂无数据"v-else ></el-empty>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</div>
</template>

<script setup>
import Breadcrumbar from '@/components/breadcrumbar.vue'
import { getUserCenterInfo } from '@/api/index.js'
import {useRouter,useRoute} from 'vue-router'
import { getTeamMemberList,saveTeamManagerInfo,getAllTeamList,updateTeamManagerInfo,applyJoinTeam,userJoinTeaList,agreeJoinTeam,checkCompetitionApplyStatusByUser,getUserCompetitionDetailInfoById} from '@/api/visualization/index.js'
import Modal from '@/plugins/modal.js'
import { cloneDeep } from 'lodash'
import { useDict } from '@/utils/dict'
import MyInfo from './components/myinfo.vue'
const { check_status } = useDict('check_status')
let route = useRoute()
let router = useRouter()
let activeName = $ref('first')

// 所有团队列表
let allTeamList = $ref([])
let allTeamListIndex = $ref(1)
let teamSearchLoading = $ref(false)
const getAllTeamLists = (key)=>{
  let query = {
    competitionId: route.query.competitionId,
    competitionSeriesId: route.query.competitionSeriesId,
    pageNum: allTeamListIndex,
    pageSize: 10,
    teamName: key || '',
  }
  teamSearchLoading = true
  getAllTeamList(query).then(res => {
    if (res.code === 200) {
     allTeamList = res.data ? res.data.map((item)=>{
      return {
        label: item.teamName,
        value: item.teamCode,
      }
     }) : []
    }
    console.log(allTeamList,'xxx11')
    teamSearchLoading = false
  })
}

// 已申请加入的团队列表
let userJoinTeamList = $ref([])
const getUserJoinTeaList = ()=>{
  let query = {
    competitionId: route.query.competitionId,
    competitionSeriesId: route.query.competitionSeriesId,
  }
  userJoinTeaList(query).then(res => {
    if (res.code === 200) {
     userJoinTeamList = res.data || []
    }
  })
}


// 团队信息提交
let form = $ref({
  teamName: '',
  teamDesc:''
})
let teamLoading = $ref(false)
const getTeamDetail = ()=>{
  let query = {
    competitionId: route.query.competitionId,
    competitionSeriesId: route.query.competitionSeriesId,
  }
  teamLoading = true
  getTeamMemberList(query).then(res => {
    if (res.code === 200) {
      let item = res.data || {};
      form = {
        ...item,
        teamName: item.teamName || '',
        teamDesc: item.teamDesc || '',
        teamCode: item.teamCode || ''
      }
    }
    teamLoading = false
  })
}

// 同意或者拒绝加入团队
const agreeJoinTeamAction = (item,status,remove) => {
  let msgs = remove ? '确认成员将从团队中移除吗？' :  status == '2' ? '确认同意吗？' : '确认拒绝吗？'
  Modal.confirm(msgs).then(() => { 
    let query = {
      teamCode: item.teamCode,
      checkStatus: status,
      userIds: item.userId,
      competitionSeriesId: route.query.competitionSeriesId,
      competitionId: route.query.competitionId
    }
    agreeJoinTeam(query).then(res => {
      if (res.code === 200) {
        let msg = remove ? '该成员已从团队中移除' : status == '2' ? '已同意加入团队' : '已拒绝加入团队'
        Modal.notifySuccess(msg)
        getTeamDetail()
      }else{
        Modal.notifyError(res.msg || '操作失败')
      }
    })
  }).catch(() => {})
}

// 切换tabs
const handleClick = (tab, event) => {
  if (tab.props.name == 'second') {
    getAllTeamLists()
    getUserJoinTeaList()
  }
}
// 团队状态
const getDictLabel = (value) => { 
  let item  = check_status.value.find(item => item.value == value)
  return item?.label || '未知'
}

const baseRules = {
  teamName: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
  ]
}
let rules = $ref(baseRules)
let pageDetail = $ref({})
const getDetail = ()=>{
  let query = {
    competitionId: route.query.competitionId,
    competitionSeriesId: route.query.competitionSeriesId,
  }
  getUserCompetitionDetailInfoById(query).then(async res => {
    if(res.code == 200) { 
      pageDetail = res.data
    }
  })
}

let fromRef = ref()
let submitFormLoading = $ref(false)
const submitForm = () => {
  fromRef.value.validate((valid) => {
    if (valid) {
      Modal.confirm('提交后，团队信息将不可修改','确认提交吗？').then(() => { 
        let apiFunc = form.teamCode ? updateTeamManagerInfo : saveTeamManagerInfo
        let query = cloneDeep(form)
        query = {
          ...query,
          competitionId: route.query.competitionId,
          competitionSeriesId: route.query.competitionSeriesId
        }
        submitFormLoading = true
        apiFunc(query).then(res => {
          submitFormLoading = false
          if(res.code === 200){
            Modal.notifySuccess('提交成功')
            getTeamDetail()
          }else{
            Modal.notifyError(res.msg || '提交失败')
          }
        }).catch(err => {
          submitFormLoading = false
        })
      })
    }
  })
}
// 团队信息提交-end

// 申请入队
let selectTeamCode = $ref('')
const handleJoinTeam = (e) => {
  if(!e && !selectTeamCode){
    Modal.notifyError('请选择团队')
    return
  }
  Modal.confirm('确认入队该团队吗？','确认入队吗？').then(() => { 
    let item ={}
    if(e){
      item = e
    }else{
      item = allTeamList.find(item => item.value == selectTeamCode)
    }
    let query = {
      competitionId: route.query.competitionId,
      competitionSeriesId: route.query.competitionSeriesId,
      teamCode: e ? item.teamCode : item.value,
    }
    applyJoinTeam(query).then(res => {
      if(res.code === 200){
        Modal.notifySuccess('已发出入队申请,请耐心等待')
        getUserJoinTeaList()
      }else{
        Modal.notifyError(res.msg || '申请入队失败')
      }
    })
  })
}

// 去赛事详情
const handleClickSaiShi = ()=>{
  const { href } = router.resolve({
    path: '/event/detail',
    query: {
      competitionId: route.query.competitionId,
      competitionSeriesId: route.query.competitionSeriesId
    },
  });
  window.open(href, "_blank");
}

// 用户个人认证信息
let userInfo = $ref({})
const getUserInfo = async () => {
  getUserCenterInfo().then(res => { 
    if(res.code == 200) { 
      userInfo = res.data || {}
      getApplyStatus()
      getTeamDetail()
      getDetail()
    }
  })
}

let applyDetail = $ref({});
const getApplyStatus = () => {
  let query = {
    competitionId: route.query.competitionId,
    competitionSeriesId: route.query.competitionSeriesId,
  };
  checkCompetitionApplyStatusByUser(query)
    .then((res) => {
      if (res.code === 200) {
        applyDetail = res.data || {};
      }
    })
    .catch((err) => {
    });
};
const applyDisable = $computed(() => {
  let res = ['null',null,'',undefined,'5',5].includes(applyDetail.applyStatus)
  let status = [6,'6','7',7].includes(pageDetail.checkStatus)
  return !res || !status
});

// 初始化
getUserInfo()
</script>

<style scoped lang="scss">
.page-content{
  background: #FFFFFF;
  border: 1px solid #E4E4E4;
  min-height: 600px;
  margin-bottom: 45px;
  .page-content-left{
    width: 300px;
    flex-shrink: 0;
    border-right:1px solid #E4E4E4;
    .page-content-left-user{
      padding: 20px 30px;
      box-sizing: border-box;
      .page-content-left-user-avatar{
        width: 80px;
        height: 80px;
        border-radius: 50%;
        object-fit: cover;
      }
      .page-content-left-user-name{
        padding: 3px 0;
        margin-left: 15px;
        height: 80px;
        .use-name{
          font-size: 20px;
          color: #333333;
          @include ellipsis(1);
          overflow: hidden;
          word-break: break-all;
        }
        .use-code{
          font-size: 14px;
          color: #999999;
        }
      }
    }
    .page-content-left-title{
      height: 60px;
      line-height: 60px;
      background: #83A5FB;
      font-size: 20px;
      color: #FFFFFF;
      width: 100%;
      text-align: center;
    }
  }
  .page-content-right{
    flex-grow: 1;
    .top-bar{
      box-sizing: border-box;
      border-bottom: 2px solid #E4E4E4;
      height: 60px;
      padding: 0 45px;
      .top-bar-text{
        font-size: 20px;
        color: #333333;
        width: fit-content;
        position: relative;
        height: 100%;
        .top-bar-line{
          height: 3px;
          background: #3169F8;
          position: absolute;
          left: 0;
          bottom: -2px;
          height: 2px;
        }
      }
    }
    .btm-content{
      padding: 45px;
      :deep(.el-tabs){
        .el-tabs__item{
          width: 250px;
          height: 64px;
          font-size: 20px;
          color: #333333;
          background: #FFFFFF;
          border-radius: 14px;
          border: 1px solid #CECECE;
          padding: 0;
          +.el-tabs__item{
            margin-left: 20px;
          }
          &:hover{
            border: 1px solid #3B72FF;
          }
        }
        .is-active{
          border: 1px solid #3B72FF;
        }
        .el-tabs__active-bar{
          display: none;
          height: 0;
        }
        .el-tabs__nav-wrap{
          &::after{
            background: none;
            display: none;
            height: 0;
          }
        }
      }
    }
    .info-block{
      padding: 15px 20px;
      height: 50px;
      background: #F5F5F5;
      font-size: 20px;
      color: #333333;
    }
    .saishi{
      cursor: pointer;
      color: #3B72FF;
      text-decoration: underline;
    }
    .teamCode{
      font-size: 18px;
      color: #999999;
    }
    .check_status{
      color: #FFDC2D;
    }
    .check_status4{
      color: #00C853;
    }
    .check_status5{
      color: #FF4444;
    }
    .check_status_o1{
      color: #FFDC2D;
    }
    .check_status_o2{
      color: #00C853;
    }
    .check_status_o3{
      color: #FF4444;
    }

    .team-user-info{
    }
    .join-team-form{
      margin-top: 35px;
      width: 80%;
      font-size: 18px;
      :deep(.el-form-item){
        margin-bottom: 30px;
        .el-form-item__label{
          font-size: 18px;
          color: #333333;
        }
        .el-form-item__content{
          font-size: 18px;
          color: #333333;
        }
      }
    }
    .w-btn{
      width: 120px;
    }
  }
  .all-team-list{
    margin-top: 35px;
    .team-list-item{
      padding: 20px 20px 20px 20px;
      box-sizing: border-box;
      border: 1px solid #E4E4E4;
      margin-bottom: 20px;
      cursor: pointer;
      position: relative;
      border-radius: 10px;
      .text-name-content{
        color: #999;
      }
      .text-name-des{
        // @include ellipsis(3);
      }
      .team-name{
        width: calc(100% - 120px);
        margin-bottom: 10px;
        display: flex;
        align-items: flex-start;
        justify-content: flex-start;
        position: relative;
        .team-name-action{
          position: absolute;
          right: 0;
          top: 0;
        }
        .text-name-lable{
          flex-shrink: 0;
        }
        &:last-child{
          margin-bottom: 0;
        }
      }
      .team-name2{
        width: 100%;
      }
      .team-status{
        position: absolute;
        right: 20px;
        top: 20px;
      }
    }
  }
}
.saiShiName{
  color: #3169F8;
  text-decoration: underline;
  cursor: pointer;
}
:deep(.el-table__expand-column){ 
  .cell{
    padding: 0 !important;
  }
}
</style>