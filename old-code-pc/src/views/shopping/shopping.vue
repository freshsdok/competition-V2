  <template>
  <div class="base-page">
    <div class="container-custom self-custom">
      <Breadcrumbar />
      <div class="card-block">
        <div class="top-info flex items-start justify-between">
          <div class="flex items-start justify-start">
            <div class="top-info-title mr-[10px]">我的赛事</div>
            <div class="top-info-num">共<span class="font-bold">{{ tableData.length || 0}}</span>个团队需要缴费</div>
          </div>
          <el-input v-model="iptValue"
                    placeholder="请输入姓名"
                    style="width: 100%"
                    clearable
                    @input="handleChange"
                    class="ipt"
                  />
        </div>
        <div class="table-wrap">
          <vxe-table :data="tableData" 
                  :header-cell-style="{
                    background: '#F2F5F7'
                  }"
                  border="inner"
                  ref="vuxTableRef"
                  @checkbox-all="handleSelectionChange"
                  @checkbox-change="selectChangeEvent"
                  empty-text="暂无结算信息">
            <vxe-column type="checkbox" title="全选" width="80" class-name="selection-column"></vxe-column>
            <vxe-column field="date" title="赛事信息" min-width="380" align="left">
              <template #default="scope">
                <div>
                  <div class="text-[#333333] text-[18px] font-[600]">
                    <span>团队名称：</span>{{ scope.row.teamName }}
                  </div>
                  <div class="mt-[6px] font-[600] text-[16px]">
                    <span class="text-[#3169f8]">{{ scope.row.competitionName }}</span>
                    <span>-</span>
                    <span class="text-[#FF8800]">{{ scope.row.competitionTrackName }}</span>
                    <span>-</span>
                    <span class="text-[#51C512]">{{ scope.row.secondLevelName }}</span>
                  </div>
                  <div  class="mt-[10px] text-[14px]">
                    <p v-if="scope.row && scope.row.playersList && scope.row.playersList.length > 0">
                      <span class="text-[#666666] font-[500]">队员：</span>
                       <span class="text-[#666666] font-[400]" v-for="(item, index) in scope.row.playersList" :key="index">
                        {{ item.userName }}（{{ item.idCard }}）
                      </span>
                    </p>
                    <p v-if="scope.row && scope.row.instructorList && scope.row.instructorList.length > 0">
                      <span class="text-[#666666] font-[500]">指导教师：</span>
                       <span class="text-[#666666] font-[400]" v-for="(item, index) in scope.row.instructorList" :key="index">
                        {{ item.userName }}<span v-if="item.guideTeacherPhone">（{{ item.guideTeacherPhone }}）</span>
                      </span>
                    </p>
                  </div>
                </div>
              </template>
            </vxe-column>
            <vxe-column field="fee" title="每队员" align="center" width="100">
              <template #default="scope">
                <div class="red-color">¥{{ scope.row.fee }}</div>
              </template>
            </vxe-column>
            <vxe-column field="teamSize" title="队员数量" align="center" width="100">        
              <template #default="scope">
                <div class="font-[400] text-[18px]">{{ scope.row.teamSize }}</div>
              </template>
            </vxe-column>
            <vxe-column title="小计" align="center" width="100"> 
              <template #default="scope">
                <div class="red-color">{{ scope.row.subtotal || 0 }}</div>
              </template>
            </vxe-column>
            <vxe-column title="操作" align="center" fixed="right" width="80"> 
               <template #default="scope">
                  <el-button type="danger" class="del-btn" plain size="small"
                            @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </vxe-column>
          </vxe-table>
        </div>
        <el-affix :offset="0" position="bottom" :z-index="10" v-if="showAffix">
          <div class="table-bottom">
            <div class="flex">
              <div class="left mr-[20px] hvr-grow" @click="goBack">继续报名</div>
              <div class="left hvr-grow" @click="deleteSelected">删除选中</div>
            </div>
            <div class="right">
              <div class="r-total">
                <span>已选：<span class="r-t-des">{{ checkedAll.length || 0}}</span>个团队（共{{tableTeamNum || 0}}名队员）</span>
                <span class="r-total-text">合计：</span>
                <span  class="r-total-num">￥{{ tableTotalAmount || 0 }}</span>
              </div>
              <div :class="[
                'settlement',
                  {'hvr-grow': checkedAll.length}
                ]"
                @click="submit" 
                v-loading="submintLoading">去结算</div>
            </div>
          </div>
        </el-affix>
      </div>
    </div>
  </div>
</template>

<script setup>
import Breadcrumbar from '@/components/breadcrumbar.vue'
import { debounce } from 'lodash'
import { useRoute,useRouter } from "vue-router";
import { getTeamCompetitionInfo,deleteCompetition,submitSettlement } from '@/api/teacher'
import Decimal from 'decimal.js';
import Modal from '@/plugins/modal.js'
const route = useRoute();
const router = useRouter()

// 全选
let checkedAll = $ref([])
const handleSelectionChange = (val) => {
  console.log(val)
  checkedAll = val.records || []
}
// 单选或者不单选
const selectChangeEvent = (val) => {
  checkedAll = val.records || []
}
// 一共多少队员
const tableTeamNum = $computed(() => {
  if(!checkedAll || !checkedAll.length) return 0
  return checkedAll.reduce((total, item) => {
    const teamSize = new Decimal(item.teamSize || 0);
    return total.plus(teamSize);
  }, new Decimal('0'))
})
// 合计多少
const tableTotalAmount = $computed(() => {
  if(!checkedAll || !checkedAll.length) return 0
  return checkedAll.reduce((total, item) => {
    const subtotal = new Decimal(item.subtotal || 0);
    return total.plus(subtotal);
  }, new Decimal('0'))
})
// 提交结算
let submintLoading = $ref(false)
const submit = () => {
  if(!checkedAll || !checkedAll.length) {
    return
  }
  let teamCodeList = checkedAll.map(item => item.teamCode)
  submintLoading = true
  submitSettlement({
    competitionSeriesId: route?.query?.competitionSeriesId || '',
    teamCodeList: teamCodeList
  }).then((res) => {
    submintLoading = false
    if (res.code === 200) {
      goSettlement(res.msg)
    }else{
      Modal.msgError(res.msg || '结算失败')
    }
  })
}

// 搜索
let iptValue = $ref('')
const handleChange = debounce(() => {
  checkedAll = []
  getDetail(iptValue)
}, 500)

// 所有赛事表单数据
let tableData = $ref([])
let vuxTableRef = ref(null)
const getDetail = (key) => {
  getTeamCompetitionInfo({
    competitionSeriesId: route?.query?.competitionSeriesId || '',
    keyWord:key
  }).then((res) => {
    if (res.code === 200) {
      tableData = res.data || []
    }
  })
}

// 多选删除
const deleteSelected = () => {
  if(!checkedAll || !checkedAll.length) {
    return Modal.msgError('请先选择要删除的团队')
  }
  let teamCodeList = checkedAll.map(item => item.teamCode)
  teamCodeList = teamCodeList.join(',')
  Modal.confirm('确认删除选中的团队吗？').then(() => {
    handleDeleteSendApi(teamCodeList,(code) => {
      deleteTableAddCheck(code)
    })
  })
}
// 单个删除showAffix
const handleDelete = (row) => {
  Modal.confirm('确认删除吗？').then(() => {
    handleDeleteSendApi(row.teamCode,(code) => {
      deleteTableAddCheck(code)
    })
  })
}
let showAffix = $ref(true)
// 删除api发送
const handleDeleteSendApi = (teamCode,callback) => {
  deleteCompetition({
    teamCode: teamCode
  }).then((res) => {
    if (res.code === 200) {
      Modal.msgSuccess('删除成功')
      callback && callback(teamCode)
      showAffix = false
      setTimeout(() => {
        showAffix = true
      }, 10)
    }else{
      Modal.msgError(res.msg || '删除失败')
    }
  })
}

// 删除表格的选中项,不能刷新tableData，只能处理tableData
const deleteTableAddCheck = (code) => {
  if(!code) return
  // 处理单个或多个teamCode
  const codeArray = code.split(',')
  
  // 从tableData中删除对应的团队（直接操作原数组，避免重新渲染）
  for(let i = tableData.length - 1; i >= 0; i--) {
    if(codeArray.includes(tableData[i].teamCode)) {
      tableData.splice(i, 1)
    }
  }
  
  // 从checkedAll中删除对应的团队（直接操作原数组）
  for(let i = checkedAll.length - 1; i >= 0; i--) {
    if(codeArray.includes(checkedAll[i].teamCode)) {
      checkedAll.splice(i, 1)
    }
  }
}


// 继续报名
const goBack = () => {
  router.replace({
    path: "/event/detail/teacherApply",
    query: {
      competitionSeriesId: route?.query?.competitionSeriesId || "",
    },
  });
}
// 去结算
const goSettlement = (msgCode) => {
  router.push({
    path: "/event/detail/teacherApply/order",
    query: {
      msgCode: msgCode,
      competitionSeriesId: route?.query?.competitionSeriesId
    },
  })
}
getDetail()
</script>

<style scoped lang="scss">
.base-page{
  font-size: 16px;
  color: #666666;
}
.top-info{
  background: #FFFFFF;
  padding: 10px 20px;
  border-radius: 5px;
  margin-bottom: 20px;
  .top-info-title{
    font-size: 20px;
    color: #333333;
    font-weight: bold;
  }
  .top-info-num{
    font-size: 14px;
    color: #999999;
    .font-bold{
      font-size: 20px;
      color: $main-color;
      padding: 0 5px;
      font-weight: bold;
    }
  }
  .ipt{
    margin-left: 50px;
    width: 300px !important;
  }
}

.del-btn{
  color: #999999;
  border:none !important;
  background: none !important;
  font-size: 16px;
  &:hover{
    color: $main-red-color;
  }
}
/* 为选择列表头添加"全选"文字 */
:deep(.table-wrap){
  background: #FFFFFF;
  padding: 20px;
  border-radius: 5px;
  margin-top: 5px;
  margin-bottom: 20px;
  position: relative;
  .vxe-table{
    .selection-column{
      vertical-align: top;
      .vxe-cell{
        padding-top: 0 !important;
      }
    }
    .is--checked{
      .vxe-checkbox--icon{
        color: $main-color;
      }
      .vxe-checkbox--label{
        color: $main-color;
        font-weight: 400;
        line-height: 1;
      }
    }
    .is--indeterminate{
      .vxe-checkbox--icon{
        color: $main-color;
      }
      .vxe-checkbox--label{
        color: $main-color;
      }
    }
    .vxe-table--header{
      .vxe-cell--title{
        font-size: 15px;
      }
    }
  }
}
.table-bottom{
  width: 100%;
  padding: 20px;
  border-radius: 5px;
  font-size: 20px;
  color: #333333;
  font-weight: 400;
  margin-top: 5px;
  margin-bottom: 20px;
  background: #FFFFFF;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 9;
  position: relative;
  .left{
    padding: 13px 27px;
    background: #ffffff;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    font-size: 16px;
    color: #000000;
    cursor: pointer;
    flex-shrink: 0;
    line-height: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .right{ 
    display: flex;
    align-items: center;
    justify-content: flex-end;
    .r-total{
      font-size: 14px;
      color: #666666;
      .r-t-des{
        font-size: 20px;
        color: $main-color;
        margin-right: 6px;
      }
      .r-total-text{
        margin-left: 10px;
        font-size: 20px;
        color: #333333;
      }
      .r-total-num{
        font-size: 26px;
        color: $main-red-color;
        font-weight: bold;
      }
    }
  }
  .settlement{
    margin-left: 20px;
    letter-spacing: 1px;
    padding: 18px 40px;
    background: $main-red-color;
    border-radius: 4px;
    font-size: 16px;
    color: #FFFFFF;
    cursor: pointer;
    flex-shrink: 0;
    line-height: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
:deep(.el-affix--fixed){
  .table-bottom{
    box-shadow: 0px 1px 10px 1px rgba(0, 0, 0, 0.1);
  }
}
.red-color{
  color: $main-red-color;
  font-size: 20px;
}
</style>