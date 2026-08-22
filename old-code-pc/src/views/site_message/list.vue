<template>
<div class="detail-page">
  <div class="container-custom">
    <div class="custom-content">
      <div class="filter">
         <el-select v-model="filterFrom.isRead" 
                    placeholder="请选择消息类型" 
                    class="!w-[260px]"
                    @change="reset"
                    clearable>
            <el-option
              v-for="item in typesOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-input v-model="filterFrom.title" 
                    class="!w-[260px] !ml-[20px]" 
                    placeholder="请输入标题名称"
                    @input="reset"
                    clearable />
      </div>
      <!-- <el-tabs v-model="filterFrom.type2" class="filterFrom-tabs" @tab-click="handleClick">
        <el-tab-pane label="全部" name="first">
          <template #label>
           <el-badge :value="12" class="in-site-badge" :offset="[11, 10]">全部</el-badge>
          </template>
        </el-tab-pane>
        <el-tab-pane label="账户资金消息" name="second">
           <template #label>
           <el-badge :value="12" class="in-site-badge" :offset="[11, 10]">账户资金消息</el-badge>
          </template>
        </el-tab-pane>
      </el-tabs> -->
      <div class="el-table-content">
        <el-table :data="tableData"
                  style="width: 100%"
                  :loading="loading"
                  :row-class-name="tableRowClassName"
                  @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="60" align="center" />
          <el-table-column prop="date" label="Date" width="180">
            <template #default="scope">
              <div @click="routerDetail(scope.row)" class="detail-cell">{{ moment(scope.row.sendTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
            </template>
          </el-table-column>       
          <el-table-column prop="title" label="title" show-overflow-tooltip width="220">
            <template #default="scope">
              <div @click="routerDetail(scope.row)" class="detail-cell">{{ scope.row.title }}</div>
            </template>
          </el-table-column>       
          <el-table-column prop="content" label="content" show-overflow-tooltip>
            <template #default="scope">
              <div @click="routerDetail(scope.row)" class="detail-cell">{{ scope.row.content }}</div>
            </template>
          </el-table-column>   
        </el-table>
        <div class="self-header">
          <div class="self-header-content">
            <el-button plain :disabled="!selectRows.length" @click="deleteSelected">删除</el-button>
            <el-button plain :disabled="!selectRows.length" @click="readSelected">标记已读</el-button>
            <el-button plain :disabled="!tableData.length" @click="readAll">全部已读</el-button>
            <el-button plain :disabled="!tableData.length" @click="deleteAll">全部删除</el-button>
          </div>
        </div>
      </div>
      <div class="pagination-container">
            <pagination v-show="total > 0"
                          :total="total"
                          :page-sizes="[10, 20, 30, 50, 100]"
                          v-model:page="filterFrom.pageNum"
                          v-model:limit="filterFrom.pageSize"
                          @pagination="getList"
                        />
        </div>
    </div>
  </div>     
</div>
</template>
<script setup>
import { getInboxNotificationList,deleteInboxNotification,getInboxNotificationDetail,deleteAllInboxNotification,readAllInboxNotification } from "@/api/site/index";
import { getToken } from "@/utils/auth";
import moment from "moment";
import Modal from "@/plugins/modal.js";
import { useRouter } from 'vue-router'
import { debounce } from 'lodash'
import { useCounterStore } from "@/stores/index";
const router = useRouter()
const store = useCounterStore()

let filterFrom = ref({
  isRead: '',
  title: '',
  type2: 'first',
  pageNum: 1,
  pageSize: 20
})
const typesOptions = [
  {value: '',label: '全部消息'},
  {value: '0',label: '未读消息'},
  {value: '1',label: '已读消息'},
]

const tableRowClassName = ({row,rowIndex,}) => {
  if (row.isRead == '1') {
    return 'read-row all-row'
  }
  return 'all-row'
}

/** 选择条数  */
let selectRows = $ref([])
function handleSelectionChange(selection) {
  selectRows = selection;
}
// 删除选中
const deleteSelected = ()=>{
  if(!selectRows.length) {
    return Modal.msgError('请先选择要删除的消息!')
  }
  Modal.confirm('确定删除选中消息吗？','',{
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => { 
    let ids = selectRows.map(item => item.id)?.join(',')
    deleteInboxNotification(ids).then(res => {
      if(res.code == 200) {
        Modal.msgSuccess('删除成功!')
        reset()
      }
    })
  })
}
// 已读选中
const readSelected = ()=>{
  if(!selectRows.length) {
    return Modal.msgError('请先选择要删除的消息!')
  }
  Modal.confirm('确定标记选中消息为已读吗？','',{
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => { 
    let ids = selectRows.map(item => item.id)?.join(',')
    getInboxNotificationDetail(ids).then(res => {
      if(res.code == 200) {
        Modal.msgSuccess('已读成功!')
        reset()
      }
    })
  })
}


// 全部已读
const readAll = () => {
  Modal.confirm('确定全部已读吗？','',{
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => { 
    readAllInboxNotification({}).then(res => {
      if(res.code == 200) {
        Modal.msgSuccess('已全部已读!')
        reset()
      }
    })
  })
}
// 全部删除
const deleteAll = () => {
  Modal.confirm('确定全部删除吗？','',{
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    deleteAllInboxNotification({}).then(res => {
      if(res.code == 200) {
        Modal.msgSuccess('已全部删除!')
        reset()
      }
    })
  }).catch(() => {
    // 取消删除
  })
}

/** 获取列表 */
let tableData = $ref([])
let total = $ref(0)
let loading = $ref(false);
const getList = () => {
  loading = true;
  getInboxNotificationList(filterFrom.value).then(async (res) => {
  loading = false;
    if(res.code == 200){
      tableData = res.rows || [];
      total = res.total || 0;
    }
  }).catch(() => {
      loading = false;
  });
}

// 使用防抖函数包装搜索方法，避免频繁请求
const reset = debounce((type) => {
  if(getToken()){
    filterFrom.value.pageNum = 1;
    getList();
    // 触发消息更新，通知 site 组件重新获取未读消息数量
    store.triggerMessageUpdate();
  }
}, 500)

// 点击跳转详情
const routerDetail = (row) => {
  console.log(row,'xxx');
  if(row.id){
    router.push({path:'/site/detail',query:{siteId:row.id}});
  }
}

onMounted(() => {
  filterFrom.value.pageNum = 1;
  if(getToken()){ getList();}
});
</script>
<style scoped lang="scss">
.detail-page{
  background-color: #F5F5F5 ;
  min-height: 45vh;
}
.container-custom{
  border-radius: 10px;
  margin: 40px auto;
  padding: 60px 120px;
}
.custom-content{
  background: #FFFFFF;
  border-radius: 4px;
  padding: 60px;
}
.detail-cell{
  width: 100%;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
  cursor: pointer;
}
:deep(.filterFrom-tabs){
  width: 100%;
  margin: 20px 0 0 10px;
  // .is-active{
  //   color: $main-color;
  // }
  // .el-tabs__active-bar{
  //   background-color: $main-color;
  // }
  // .el-tabs__item{
  //   &:hover{
  //     color: $main-color;
  //   }
  // }
}
:deep(.el-table){
  .read-row{
    background: rgb(247, 249, 250);
    color: rgba(51, 51, 51, 0.5);
  }
  .el-table__header{
    padding-bottom: 10px;
    .el-table__cell{
      opacity: 0;
      visibility: hidden;
      display: none;
      border: none;
    }
    .el-table-column--selection{
      opacity: 1;
      visibility: visible;
      display: table-cell;
    }
  }
}
.pagination-container{
  margin-top: 20px;
  width: 100%;
  text-align: right;
  display: flex;
  justify-content: flex-end;
}
.el-table-content{
  position: relative;
  width: 100%;
  margin-top: 30px;
  .self-header{
    position: absolute;
    top: 0;
    left: 55px;
    height: 60px;
    z-index: 12;
    width: calc(100% - 55px);
    .self-header-content{
      display: flex;
      justify-content: flex-start;
      align-items: center;
      padding: 5px 20px;
    }
  }
}
.filter{
  padding-bottom: 30px;
  border-bottom: 1px solid #ebeef5;
}
</style>
