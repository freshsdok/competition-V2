<template>
  <div class="user-select-content">
    <el-dialog v-model="dialogVisible" :title="title" width="1200px" @close="close" :close-on-click-modal="false" append-to-body>

      <div class="main-content" @click.stop="popoverVisible=false">
        <div class="org-user">

          <!-- 查询框 -->
          <div class="search-box">
            <el-popover :visible="popoverVisible" placement="bottom" :width="555">
              <template #default>
                <ul v-if="searchValue" class="user-list" v-loading="listLoading" element-loading-text="拼命加载中">
                  <li 
                    v-for="item in allUserList" :key="item[props.propsUser.key]"
                    class="user-item" :class="isSelectedItem(item) ? 'user-item-disabled' : ''"
                    @click.stop="chooseUser(item)">
                    <div class="nick-name">{{ item[props.propsUser.value] }} ({{ item[props.propsUser.workId] }})</div>
                    <div class="dept-name">{{ item[props.propsUser.fullPath] || "/" }}</div>
                  </li>
                  <el-empty v-if="allUserListStatus == 2" :image="emptyImg"></el-empty>
                </ul>
                <div v-else>请输入“工号” 或者 “姓名”关键字查询</div>
              </template>
              <template #reference>
                <el-input 
                  v-model="searchValue" 
                  class="search-input" 
                  placeholder="请输入工号或姓名"
                  prefix-icon="Search" 
                  clearable 
                  @clear="handleClearInput" 
                  @input="debounce(handleInput, 500)"
                  @keyup.enter="handleInput"
                  @click.stop="popoverVisible=!popoverVisible" />
              </template>
            </el-popover>
          </div>

          <!-- 组织架构面包屑 -->
          <div class="breadcrumb-box">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item 
                v-for="(item, index) in breadcrumbList" 
                :key="index"
                :class="{ 'bread-active': breadcrumbActiveIndex == index }"
                @click="handleChange(item, index)" >
                {{ item[props.propsOrg.value] }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="org-user-box">
            <div class="org-list" v-loading="orgLoading">
              <div class="org-list-item" v-for="(item, index) in orgList" :key="index" @click="handleOrgClick(item[props.propsOrg.key])">
                <img src="../../assets/images/user.png" alt="" srcset="">
                <div :class="['org-name', { active: item[props.propsOrg.key] == currentOrgId }]">
                  {{ item[props.propsOrg.value] }}
                  <span style="color: #aeaeae;" v-show="item[props.propsOrg.hasChild]">（{{ item[props.propsOrg.total] }}）</span>
                </div>
                <div class="to-next-btn" v-show="!item[props.propsOrg.hasChild]" @click.stop="handleNext(item)">
                  <el-divider direction="vertical"></el-divider>下级
                </div>
              </div>
            </div>
            <div class="line"></div>
            <div class="all-users" v-loading="loading">
              <el-empty v-if="userList.length == 0" :image="emptyImg"></el-empty>
              <el-checkbox
                v-model="checkAll"
                v-show="userList.length > 0 && multiple"
                :indeterminate="isIndeterminate"
                @change="handleCheckAllChange">全选</el-checkbox>
              <div class="mt10">
                <el-checkbox-group v-model="checkedList" @change="handleCheckedChange" v-if="multiple">
                  <div class="check-item" v-for="item in userList" :key="item[props.propsUser.key]">
                    <el-checkbox :label="item">
                      <img src="../../assets/images/user.png" alt="" srcset="">
                      <div>
                        {{ item[props.propsUser.value] }}
                        <span style="color:#aeaeae;" v-show="item[props.propsUser.workId]">（{{ item[props.propsUser.workId] }}）</span>
                      </div>
                    </el-checkbox>
                  </div>
                </el-checkbox-group>
                <el-radio-group v-model="checkedList" v-if="!multiple">
                  <div class="check-item" v-for="item in userList" :key="item[props.propsUser.key]">
                    <el-radio :label="item">
                      <img src="../../assets/images/user.png" alt="" srcset="">
                      <div>
                        {{ item[props.propsUser.value] }}
                        <span style="color:#aeaeae;" v-show="item[props.propsUser.workId]">（{{ item[props.propsUser.workId] }}）</span>
                      </div>
                    </el-radio>
                  </div>
                </el-radio-group>
              </div>
            </div>
          </div>
        </div>
        <div class="line"></div>

        <!-- 已选择的人员 -->
        <div class="select-users">
          <div class="title-box">
            <span><i class="el-icon-warning"></i>已选：{{ selectedList.length }}人</span>
            <span class="clear-btn" @click.stop="handleClear">清空</span>
          </div>
          <div class="select-content">
            <div class="select-content-item" v-for="(item, index) in selectedList" :key=item[props.propsUser.key]>
              <img src="../../assets/images/org.png" alt="" srcset="">
              <div class="content">
                <span>
                  {{ item[props.propsUser.value] }}
                  <span style="color: #aeaeae" v-show="item[props.propsUser.workId]">
                    （{{ item[props.propsUser.workId] }}）
                  </span>
                </span>
                <div class="dept-name">{{ item[props.propsUser.fullPath] || "/" }}</div>
              </div>
              <el-icon><Close @click.stop="handleDel(item, index)" /></el-icon>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="close">取 消</el-button>
          <el-button type="primary" @click="handleConfirm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="UserSelect">
import { deptTreeSelect1, getlistUser, getUserWhere } from "@/api/userSelect";
import empty from "@/assets/images/empty.png";

const { proxy } = getCurrentInstance();

const emit = defineEmits(['confirm', "close"])

const props = defineProps({
  multiple: {
    type: Boolean,
    default: false
  },
  propsOrg: {
    type: Object,
    default: () => {
      return { key: 'id',  value: 'label', hasChild: 'leaf', total: 'userTotal' }
    }
  },
  propsUser: {
    type: Object,
    default: () => {
      return { key: 'userId', value: 'nickName', workId: 'userName', fullPath: 'cOrgFullName' };// id,名称,工号,组织架构全称
    }
  },
  defaultSelectList: {
    type: Array,
    default: () =>  {
      return []
    }
  }
})

const popoverVisible = ref(false);

const title = ref("");
const dialogVisible = ref(true);
const orgLoading = ref(false);
const loading = ref(false);
const listLoading = ref(false);
const emptyImg = ref(empty);
const breadcrumbList = ref([]); //面包屑集合
const breadcrumbActiveIndex = ref(0); //选中的面包屑的索引
const breadcrumbActiveId = ref(null); //选中的面包屑组织的id
const isIndeterminate = ref(false); //半全选
const orgList = ref([]); //当前层级的可选机构
const orgStorageList = ref([]); //缓存机构的选择项
const currentOrgId = ref(""); //当前点击的组织Id
const checkAll = ref(false); //全选
const userList = ref([]); //可选人员集合
const userStorageList = ref([]); //缓存的可选人员集合
const checkedList = ref([]); //当前层选择的人员
const selectedList = ref([]); //所有选择的人员
const searchValue = ref(""); //查询内容
const allUserList = ref([]); // 所有的人员列表
const allUserListStatus = ref(0);

// 选中的人回显
watch(() => props.defaultSelectList, val => {
  selectedList.value = val;
},{immediate: true})

//当前层级可选项数据变化时
watch(() => userList.value, val => {
  //回显当前层级选中的值
  let arr = []
  val.forEach(item=>{
    selectedList.value.forEach(item1=>{
      if(item[props.propsUser.key] == item1[props.propsUser.key]){
        arr.push(item)
      }
    })
  })
  checkedList.value = props.multiple ? arr : arr.length>0 ? arr[0] : [] //单选和多选
  checkAll.value = arr.length === val.length;
  isIndeterminate.value = arr.length > 0 && arr.length < userList.value.length;
  
})

//当前层级选中数据变化时
watch(() => checkedList.value, val => {
  if(!props.multiple){//单选
    val = Array.isArray(val) ? val : [val]
    selectedList.value = val.length > 0 ? val : selectedList.value;
    return false
  }

  checkAll.value = val.length === userList.value.length;
  isIndeterminate.value = val.length > 0 && val.length <userList.value.length;

  //全部选中的
  let arr = []
  let selectedList2 = [...selectedList.value]
  selectedList2.forEach(item1=>{
    arr.push(item1[props.propsUser.key])
  })

  //当前层级可选的值
  let arr1 = []
  let orgList = [...userList.value]
  orgList.forEach(item1=>{
    arr1.push(item1[props.propsUser.key])
  })

  //当前层级没选中的值
  let arr2 = [...arr1]

  //当前层级选中的数据
  val.forEach(item=>{
    //全选中的里面没有就添加进去
    if(arr.indexOf(item[props.propsUser.key]) < 0 ){
      selectedList.value.push(item)
    }
    arr2.splice(arr2.indexOf(item[props.propsUser.key]),1)
  })

  //没有选中的值就从全部选择的里面进行删除
  let newArr = []
  selectedList.value.forEach((item,index)=>{
    if(arr2.indexOf(item[props.propsUser.key]) < 0){
      newArr.push(item)
    }
  })
  selectedList.value = newArr
})

// 初始化
function getInit(){
  if (props.multiple) {
    title.value = "选择人员（多选）"
  } else {
    title.value = "选择人员（单选）"
  }
  // if (props.defaultSelectList.length>0) {
  //   getUserListByIds();
  // };
  deptTreeSelect1().then(res=>{
    breadcrumbList.value = res.data
    breadcrumbActiveId.value = breadcrumbList.value[0][props.propsOrg.key]
    getNextOrg(breadcrumbList.value[0][props.propsOrg.key])
    getUserList(breadcrumbList.value[0][props.propsOrg.key])
  })
}

// 根据ids查询用户列表回显
function getUserListByIds () {
  let userIds = props.defaultSelectList.map(item => item[props.propsUser.key]);
  selectUserByIds(userIds).then(res => {
    selectedList.value = res.data;
  })
}

//获取下级机构
function getNextOrg(id,n=0){
  orgLoading.value = true;
  deptTreeSelect1({parentId: id}).then(res=>{
    orgList.value = res.data
    orgStorageList.value.splice(n)
    orgStorageList.value.push(res.data)
    orgLoading.value = false
  })
}

//机构点击
function handleOrgClick(id){
  currentOrgId.value = id
  getUserList(id,false)
}

//下级点击
function handleNext(item){
  searchValue.value = ''
  breadcrumbActiveIndex.value +=1
  breadcrumbActiveId.value = item[props.propsOrg.key]
  getNextOrg(item[props.propsOrg.key], breadcrumbActiveIndex.value)
  breadcrumbList.value.splice(breadcrumbActiveIndex.value)
  breadcrumbList.value.push(item)
  getUserList(item[props.propsOrg.key])
}

//获取用户
function getUserList(id,bool=true){
  loading.value = true
  getlistUser({deptId: id}).then(res=>{
    userList.value = res.data
    if(bool){//初始和点击下级的时候才缓存
      userStorageList.value.splice(breadcrumbActiveIndex.value)
      userStorageList.value.push(res.data)
    }
    loading.value = false
  })
}

//面包屑切换
function handleChange(item,index){
  breadcrumbActiveId.value = item[props.propsOrg.key]
  currentOrgId.value = ''
  searchValue.value = ''
  breadcrumbActiveIndex.value = index
  orgList.value = orgStorageList.value[index]
  userList.value = userStorageList.value[index]
}

//全选操作
function handleCheckAllChange(val){
  checkedList.value = val ? userList.value : [];
  isIndeterminate.value = false;
}

function handleCheckedChange(value){
  let checkedCount = value.length;
  checkAll.value = checkedCount === userList.value.length;
  isIndeterminate.value = checkedCount > 0 && checkedCount < userList.value.length;
}

// 选择人员
function chooseUser (row) {
  // 下标大于-1说明已选择, 则取消选择
  let index = selectedList.value.findIndex(item => {
    return item[props.propsUser.key] == row[props.propsUser.key];
  });
  // 判断是否允许多选
  if (props.multiple) {
    if (index==-1) {
      selectedList.value.push(row);
    } else {
      selectedList.value.splice(index, 1);
    }
  } else {
    if (index==-1) {
      selectedList.value = [row];
    } else {
      selectedList.value = [];
    }
  };
  // 左侧可选人员列表和右侧已选择的人员列表比较, 回显选中状态
  let checkedList2 = [];
  userList.value.forEach(item => {
    selectedList.value.forEach(item2 => {
      if (item[props.propsUser.key] == item2[props.propsUser.key]) {
        checkedList2.push(item);
      }
    })
  });
  if (props.multiple) {
    checkedList.value = checkedList2;
  } else {
    checkedList.value = checkedList2[0] || [];
  }
}

// 判断查询出来的结果是否已被选择
function isSelectedItem (row) {
  let index = selectedList.value.findIndex(item => {
    return item[props.propsUser.key] == row[props.propsUser.key];
  });
  if (index==-1) {
    return false
  } else {
    return true
  };
}

//查询
function handleInput(){
  allUserListStatus.value = 1;
  if (searchValue.value) {
    listLoading.value = true;
    getUserWhere({userName: searchValue.value}).then(res => {
      allUserList.value = res.data;
      listLoading.value = false;
      if (res.data&&res.data.length>0) {
        allUserListStatus.value = 1;
      } else {
        allUserListStatus.value = 2;
      }
    }).catch(err => {
      listLoading.value = false;
    })
  } else {
    allUserList.value = [];
  }
}

// 清除查询 
function handleClearInput () {
  allUserList.value = [];
}

//清空
function handleClear(){
  selectedList.value = []
  checkedList.value = []
  checkAll.value = false
  isIndeterminate.value = false
}

//删除已选择
function handleDel(data,index){
  selectedList.value.splice(index,1)

  if(!props.multiple){//单选
    checkedList.value = Array.isArray(checkedList.value) ? checkedList.value : [checkedList.value]
  }

  let checkedList2 = [...checkedList.value]
  checkedList2.forEach((item,i)=>{
    if(item[props.propsUser.key] == data[props.propsUser.key]){
      checkedList2.splice(i,1)
    }
  })
  checkedList.value = checkedList2
}

//确定
function handleConfirm(){
  emit('confirm', selectedList.value)
}

//关闭
function close() {
  emit('close');
}

getInit();
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.module.scss";

ul,
li {
  margin: 0;
  padding: 0;
  list-style: none;
}

.main-content {
  display: flex;
  flex-wrap: nowrap;

  .org-user {
    flex: 1;

    .search-box {
      width: 100%;
      display: flex;
      align-items: center;
      padding: 10px 10px 10px 0;

      .search-input {
        width: 555px;
      }
    }

    .org-user-box {
      display: flex;
      padding-right: 10px;
      height: 380px;
      border-top: 1px solid #eee;

      .org-list {
        overflow: auto;
        flex: 1;
        padding-top: 20px;

        .org-list-item {
          display: flex;
          align-items: center;
          padding-right: 10px;
          margin-bottom: 20px;
          cursor: pointer;

          img {
            margin-right: 8px;
          }

          .org-name {
            flex: 1;
          }

          .active {
            color: $--color-primary;
          }

          .to-next-btn {
            font-size: 14px;
            width: 80px;
            color: $--color-primary;
            text-align: right;
            padding-right: 10px;
          }
        }
      }

      .all-users {
        overflow: auto;
        width: 41%;
        padding-left: 10px;
        padding-top: 10px;
      }
    }

    .check-item {
      display: flex;
      margin-bottom: 10px;

      ::v-deep .el-checkbox {
        flex: 1;
        display: flex;
        align-items: center;

        .el-checkbox__label {
          flex: 1;
          display: flex !important;
          align-items: center;

          img {
            margin-right: 5px;
          }

          div {
            flex: 1;
          }
        }
      }

      ::v-deep .el-radio {
        flex: 1;
        display: flex;
        align-items: center;

        .el-radio__label {
          flex: 1;
          display: flex !important;
          align-items: center;

          img {
            margin-right: 5px;
          }

          div {
            flex: 1;
          }
        }
      }

      .to-next-btn {
        font-size: 14px;
        width: 80px;
        color: $--color-primary;
        text-align: right;
        padding-right: 10px;
        cursor: pointer;
      }
    }
  }

  .select-users {
    flex: 1;
    margin: 10px 0 0 10px;

    .title-box {
      box-sizing: border-box;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 20px;
      height: 36px;
      line-height: 36px;
      font-size: 14px;
      background: #e5f1ff;
      border: 1px solid #8bbcff;
      border-radius: 3px;

      .el-icon-warning {
        color: #204ed7;
        margin-right: 4px;
      }

      .clear-btn {
        color: $--color-primary;
        cursor: pointer;
        font-size: 13px;
      }
    }

    .select-content {
      margin-top: 15px;
      height: 415px;
      overflow: auto;

      .select-content-item {
        display: flex;
        align-items: center;
        background: #F2F9FF;
        padding: 8px;
        margin: 10px 0 0 0;
        cursor: pointer;

        img {
          margin-right: 8px;
        }

        .content {
          flex: 1;

          .dept-name {
            font-size: 12px;
            color: rgba(23, 26, 29, 0.4);
            margin-top: 5px;
          }
        }

        .el-icon-close {
          width: 30px;
          text-align: center;
        }
      }
    }
  }
}

.line {
  width: 1px;
  background: #eee;
}

// 查询列表的样式
.user-list {
  height: 330px;
  padding: 0 10px 0 0;
  overflow-y: auto;

  .user-item {
    box-sizing: border-box;
    border-radius: 8px;
    padding: 8px;
    margin-bottom: 10px;
    background: rgba(126, 134, 142, 0.12);
    user-select: none;
    cursor: pointer;

    &:last-child {
      margin-bottom: 0;
    }

    .nick-name {
      font-size: 14px;
      color: #0062b1;
    }

    .dept-name {
      font-size: 12px;
      color: rgba(23, 26, 29, 0.4);
      margin-top: 5px;
    }
  }

  .user-item-disabled {
    opacity: 0.6;
  }
}

// 面包屑

.breadcrumb-box {
  padding: 0 10px 10px 0;
  :deep(.el-breadcrumb__item) {
    height: 30px;
    line-height: 30px;
    cursor: pointer;
  }
  .bread-active {
    :deep(.el-breadcrumb__inner) {
      color: $--color-primary;
      font-weight: bold;
    }
  }
  :deep(.el-breadcrumb__inner) {
    cursor: pointer !important;
  }
}
</style>