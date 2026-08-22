<template>
  <view class="container">
    <view class="form-box">
      <view class="form-item">
        <text class="label">学校名称</text>
        <uni-easyinput
          v-model="form.schoolName"
          placeholder="请输入完整学校名称"
          :inputBorder="false"
          :clearable="true"
        />
      </view>
      
      <view class="form-item">
        <text class="label">学生姓名</text>
        <uni-easyinput
          v-model="form.studentName"
          placeholder="请输入学生姓名"
          :inputBorder="false"
          :clearable="true"
        />
      </view>
      
      <view class="form-item">
        <text class="label">证件号后6位</text>
        <uni-easyinput
          v-model="form.idCard"
          placeholder="请输入证件号后6位"
          :inputBorder="false"
          :clearable="true"
          maxlength="6"
        />
      </view>
    </view>
    
    <button class="query-btn" :loading="loading" :disabled="loading" @click="handleQuery">查询</button>
    
    <!-- 查询结果区域 -->
     <template v-if="queryResult && queryResult.teams && queryResult.teams.length > 0">
      <view class="result-box">
        <text class="result-title">查询结果</text>
        <view 
          class="team-card" 
          v-for="(team, index) in queryResult.teams" 
          :key="index"
          :class="{ selected: (selectedId && (selectedId == team.userId)) }"
          @click="selectTeam(team.userId)"
        >
          <view class="team-row">
            <text class="team-label">姓名：</text>
            <text class="team-value">{{ team.userName || '-' }}</text>
          </view>
          <view class="team-row">
            <text class="team-label">邮箱：</text>
            <text class="team-value">{{ team.email || '-' }}</text>
          </view>
          <view class="team-row">
            <text class="team-label">团队名称：</text>
            <text class="team-value">{{ team.teamName || '-' }}</text>
          </view>
          <view class="team-row">
            <text class="team-label">队员：</text>
            <text class="team-value">{{ team.userNames || '-' }}</text>
          </view>
          <view class="team-row">
            <text class="team-label">带队老师：</text>
            <text class="team-value">{{ team.leaderTeacher || '-' }}</text>   
          </view>
          <view class="team-row">
            <text class="team-label">指导老师：</text>
            <text class="team-value">{{ team.guideTeacher || '-' }}</text>
          </view>
          <view class="team-row">
            <text class="team-label">赛道：</text>
            <text class="team-value">{{ team.track || '-' }}</text>
          </view>
          <view class="team-row">
            <text class="team-label">组别：</text>
            <text class="team-value">{{ team.group || '-' }}</text>
          </view>
          <!-- 单选按钮 -->
          <view class="radio-box">
            <view class="radio" :class="{ checked: (selectedId && (selectedId == team.userId)) }">
              <view class="radio-inner" v-if="(selectedId && (selectedId == team.userId))"></view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 是我/不是我按钮 -->
      <view class="action-btns">
        <button 
          class="btn-yes" 
          :class="{ disabled: selectedId === null }"
          @click="handleYes"
        >是我</button>
        <button class="btn-no" @click="handleNo()">不是我</button>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { retrySign } from '@/api/scan'
import { onLoad } from '@dcloudio/uni-app'

const form = ref({
  schoolName: '',
  studentName: '',
  idCard: ''
})

let currentRid = ref('')
const queryResult = ref({})
const selectedId = ref(null)
const loading = ref(false)

onLoad((options) => {
  currentRid.value = options.rid || ''
})

function handleQuery() {
  if (!form.value.schoolName.trim()) {
    uni.showToast({ title: '请输入完整学校名称', icon: 'none' })
    return
  }
  if (!form.value.studentName.trim()) {
    uni.showToast({ title: '请输入学生姓名', icon: 'none' })
    return
  }
  if (!form.value.idCard.trim()) {
    uni.showToast({ title: '请输入证件号后6位', icon: 'none' })
    return
  }
  if (form.value.idCard.length !== 6) {
    uni.showToast({ title: '证件号后6位必须为6位', icon: 'none' })
    return
  }
  
  loading.value = true
  queryResult.value = {}
  selectedId.value = null
  retrySign({ 
    recordId: currentRid.value,
    schoolName: form.value.schoolName,
    studentName: form.value.studentName,
    idCard: form.value.idCard
  }).then(res => {
    if(res?.data?.checkInFlag === false){
      uni.showModal({
        title: '提示',
        content: res?.data?.msg,
        showCancel: false,
        confirmText: '确认'
      })
      return
    }
    if (res.code === 200 && res.data) {
      queryResult.value = res.data
      // 重置选择状态
      let teamId = res.data?.teams[0]?.userId
      selectedId.value = teamId
    } else {
      uni.showToast({ title: res.msg || '查询失败', icon: 'none' })
    }
  }).catch((e) => {
    uni.showToast({ title: e || '服务器异常，请稍后重试', icon: 'none' })
  }).finally(() => {
    loading.value = false
  })
}

function selectTeam(teamId) {
  selectedId.value = teamId
}

function handleYes() {
  if (selectedId.value === null) {
    uni.showToast({ title: '请先选择查询结果', icon: 'none' })
    return
  }
  uni.redirectTo({
    url: '/pages/scan/result?scene=rid_' + currentRid.value + '&selectedUserId=' + selectedId.value
  })
}

function handleNo() {
  uni.showModal({
    title: '提示',
    content: queryResult.value?.notMeMsg || '请联系现场工作人员处理',
    showCancel: false,
    confirmText: '确认'
  })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #ffffff;
  padding: 20rpx;
  box-sizing: border-box;
}

.form-box {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 0 30rpx;
}

.form-item {
  display: flex;
  align-items: center;
  height: 100rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .label {
    width: 180rpx;
    font-size: 30rpx;
    color: #333333;
    font-weight: 500;
  }
  
  :deep(.uni-easyinput) {
    flex: 1;
    
    .uni-easyinput__content {
      background-color: transparent !important;
      border: none !important;
      
      .uni-easyinput__content-input {
        font-size: 30rpx;
        color: #333333;
        text-align: right;
        padding-right: 0;
      }
      
      .uni-easyinput__placeholder-class {
        font-size: 30rpx;
        color: #CECECE;
        text-align: right;
      }
    }
  }
}

.query-btn {
  margin: 40rpx auto 0;
  width: 420rpx;
  height: 90rpx;
  line-height: 90rpx;
  background: linear-gradient(135deg, #3169F8 0%, #5B8AFF 100%);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 500;
  border-radius: 44rpx;
  border: none;
  
  &:active {
    opacity: 0.9;
  }
}

.result-box {
  margin-top: 30rpx;
  padding: 30rpx;
  
  .result-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333333;
    margin-bottom: 20rpx;
    display: block;
  }
  
  .team-card {
    background-color: #F5F5F5;
    border-radius: 16rpx;
    padding: 10rpx 60rpx 10rpx 20rpx;
    margin-bottom: 20rpx;
    position: relative;
    border: 2rpx solid transparent;
    
    &.selected {
      border-color: #3169F8;
      background-color: #F0F5FF;
    }
    
    .team-row {
      margin-bottom: 6rpx;
      &:last-child {
        margin-bottom: 0;
      }
      .team-label {
        font-size: 26rpx;
        color: #999999;
      }

      .team-value {
        font-size: 26rpx;
        color: #666666;
      }
    }
    
    .radio-box {
      position: absolute;
      top: 20rpx;
      right: 20rpx;
      
      .radio {
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;
        border: 2rpx solid #cccccc;
        display: flex;
        align-items: center;
        justify-content: center;
        
        &.checked {
          border-color: #3169F8;
          background-color: #3169F8;
        }
        
        .radio-inner {
          width: 20rpx;
          height: 20rpx;
          border-radius: 50%;
          background-color: #ffffff;
        }
      }
    }
  }
}

.action-btns {
  display: flex;
  justify-content: center;
  gap: 30rpx;
  margin-top: 10rpx;
  
  .btn-yes {
    width: 300rpx;
    height: 88rpx;
    line-height: 88rpx;
    background: linear-gradient(135deg, #3169F8 0%, #5B8AFF 100%);
    color: #ffffff;
    font-size: 32rpx;
    font-weight: 500;
    border-radius: 44rpx;
    border: none;
    
    &:active {
      opacity: 0.9;
    }
    
    &.disabled {
      opacity: 0.5;
    }
  }
  
  .btn-no {
    width: 300rpx;
    height: 88rpx;
    line-height: 88rpx;
    background-color: #ECF1FF;
    color: #3169F8;
    font-size: 32rpx;
    font-weight: 500;
    border-radius: 44rpx;
    border: 2rpx solid #3169F8;
    
    &:active {
      background-color: #f5f5f5;
    }
  }
}
</style>
