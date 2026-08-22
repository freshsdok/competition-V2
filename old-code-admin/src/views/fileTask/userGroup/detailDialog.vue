<template>
<el-dialog :title="title"
            v-model="open" 
            width="68%" 
            style="margin-bottom: 50px;"
            append-to-body>
  <el-form  ref="formInfoRef" 
            :model="form" 
            :rules="rules" 
            label-position="right"
            v-if="open"
            :disabled="dialogDisabled"
            label-width="120px">
    <div class="mon-title">基本信息</div>
    <el-form-item label="用户组名称" prop="name">
      <el-input v-model="form.name" placeholder="请输入用户组名称"  />
    </el-form-item>
    <el-form-item label="用户组管理员" prop="groupManagerList">
       <div v-if="form.groupManagerList && form.groupManagerList.length > 0">
         <el-tag v-for="tag in form.groupManagerList"
                :key="tag"
                :closable="!dialogDisabled"
                style="margin-right: 10px;"
                @close="handleClose(tag)">
          {{ getShowName(tag) }}<span v-if="tag.phoneNumber">（{{ tag.phoneNumber }}）</span>
        </el-tag>
      </div>
      <div v-else style="color: #909399;font-size: 12px;margin-right: 10px;">请设置用户组管理员</div>
      <el-button type="primary" @click="handleAdminClick" v-if="!dialogDisabled">设置用户组管理员</el-button>
    </el-form-item>
    <el-form-item label="用户组说明" prop="descripe">
      <el-input v-model="form.descripe" type="textarea" placeholder="请输入用户组说明" rows="3" />
    </el-form-item>
    <div class="mon-title">规则配置</div>
     <el-form-item label="选择赛事" prop="sysUserGroupCompetitionRelationList">
      <el-tree
        :data="treeData"
        class="tree-border"
        multiple
        show-checkbox
        node-key="id"
        ref="treeRef"
        style="width: 100%"
        empty-text="加载中，请稍候">
      </el-tree>
    </el-form-item>
    <el-form-item label="允许身份" prop="identifyType">
      <el-checkbox-group v-model="form.identifyType">
        <el-checkbox label="教师（已通过教师认证）" value="teacher" />
        <el-checkbox label="学生（已通过学生认证）" value="student" />
      </el-checkbox-group>
    </el-form-item>
    <el-form-item label="允许角色" prop="allowRoleName">
      <el-checkbox-group v-model="form.allowRoleName">
        <el-checkbox label="队长" value="队长" />
        <el-checkbox label="队员" value="队员" />
        <el-checkbox label="指导教师" value="指导教师" />
      </el-checkbox-group>
    </el-form-item>
    <div class="mon-title">添加白名单</div>
    <div style="margin-bottom: 10px;">
      <el-button type="primary" @click="handleAddUser" v-if="!dialogDisabled" style="margin-right: 10px;">添加白名单</el-button>
      <span>已添加白名单（共{{ form.userIds && form.userIds.length || 0 }}人）</span>
    </div>
    <el-table :data="form.userIds" style="width: 100%">
      <el-table-column label="用户名称" align="left" prop="label" min-width="120px" >
        <template #default="scope">
          {{getShowName(scope.row)}}
        </template>
      </el-table-column>
      <el-table-column label="手机号" align="left" prop="phoneNumber" width="110px"/>
      <el-table-column label="身份" align="left" prop="position" show-overflow-tooltip/>
      <el-table-column label="学校" align="left" prop="schoolName" min-width="160px"  show-overflow-tooltip/>
      <el-table-column label="邮箱" align="left" prop="email" min-width="140px" show-overflow-tooltip/>
      <el-table-column label="操作" width="80" v-if="!dialogDisabled">
        <template #default="scope">
           <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)"></el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="mon-title">禁止名单</div>
    <div style="margin-bottom: 10px;">
      <el-button type="primary" @click="handleHeimingdan" v-if="!dialogDisabled" style="margin-right: 10px;">添加禁止名单</el-button> 
      <span>已添加禁止名单（共{{ form.blackUserIds && form.blackUserIds.length || 0 }}人）</span>
    </div>
    <el-table :data="form.blackUserIds" style="width: 100%">
      <el-table-column label="用户名称" align="left" prop="label" min-width="120px" >
        <template #default="scope">
          {{getShowName(scope.row)}}
        </template>
      </el-table-column>
      <el-table-column label="手机号" align="left" prop="phoneNumber" width="110px"/>
      <el-table-column label="身份" align="left" prop="position" show-overflow-tooltip/>
      <el-table-column label="学校" align="left" prop="schoolName" min-width="160px"  show-overflow-tooltip/>
      <el-table-column label="邮箱" align="left" prop="email" min-width="140px" show-overflow-tooltip/>
      <el-table-column label="操作" width="80" v-if="!dialogDisabled">
        <template #default="scope">
           <el-button link type="danger" icon="Delete" @click="handleDeleteHeimingdan(scope.row)"></el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-form>
  <template #footer>
    <div class="dialog-footer">
      <el-button type="primary" @click="submitForm" v-if="!dialogDisabled">确 定</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>
  </template>
</el-dialog>
<userSelect ref="userSelectRef" @submit="selectUuserGroupAdmin"></userSelect>
</template>
<script setup name="UserGroupDetailDialog">
// 导入组件
import userSelect from "./userSelect.vue";
// 导入工具
import modal from "@/plugins/modal";
import { selectAllCompetitionDetailInfo,systemUserGroupAdd,systemUserGroupUpdate,systemUserGroupDetail } from "@/api/fileTask";
import { cloneDeep } from "lodash";
import { nextTick } from "vue";
// ************** 弹窗基础配置 **************
// 弹窗标题
let title = $ref('用户组')
// 弹窗显示状态
let open = $ref(false)
// 表单数据
const baseForm = {
  name: '', // 用户组名称
  groupManagerList: [], // 用户组管理员列表
  descripe: '', // 用户组说明
  sysUserGroupCompetitionRelationList: [], // 选择赛事
  identifyType: [], // 允许身份
  allowRoleName: [], // 允许角色
  userIds: [], // 关联用户列表
  blackUserIds: [] // 禁止名单
}
let form = $ref(cloneDeep(baseForm))
// 表单验证规则
let rules = $ref({
  name: [
    { required: true, message: '请输入用户组名称', trigger: 'blur' }
  ]
})

// ************** 弹窗控制方法 **************
/**
 * 打开弹窗
 * @param {Object} info - 弹窗配置信息
 * @param {string} info.title - 弹窗标题
 * @param {Object} [info.row] - 表单数据行，用于编辑和查看
 */
let dialogDisabled = $ref(false)
const openDilog = (info) => { 
  title = info.title
  dialogDisabled = info.disabled || false
  resetQuery()
  open = true
  getDetailInfo(info?.row?.id)
}

const getDetailInfo = async (id) => { 
  if(!id) { return }
  systemUserGroupDetail(id).then(response => {
    if (response.code == 200) {
      form = response.data
      try {
        form.identifyType = form.identifyType ? form.identifyType.split(',') : []
        form.allowRoleName = form.allowRoleName ? form.allowRoleName.split(',') : []
        form.userIds = form.userList || []
        form.blackUserIds = form.blackUserList || []
        setCheckedTreeKeys(form.sysUserGroupCompetitionRelationList || [])
      } catch (error) {}
    }
  })
}

const setCheckedTreeKeys = (deom) => {
  setTimeout(() => {
    if(!treeRef.value) { return }
    if(!deom || deom.length === 0) { return }
    deom.forEach((v) => {
      nextTick(() => {
        treeRef.value.setChecked(v.id, true, false);
      });
    });
  }, 10)
}
// 重置表单数据
function resetQuery() {
  if(formInfoRef.value) {
    formInfoRef.value.resetFields()
  }
  form = cloneDeep(baseForm)
}
/**
 * 关闭弹窗
 */
const cancel = () => { 
  open = false
}

// ************** 通用用户选择处理 **************
// 用户选择组件引用
const userSelectRef = ref(null)

/**
 * 通用用户选择回调函数
 * @param {Array} val - 选中的用户数组
 * @param {string} formKey - 表单字段名
 * @description 合并现有用户和新选中的用户，并使用Map进行去重（O(n)时间复杂度）
 */
const handleUserSelect = (val, formKey) => {
  let arr = val || []
  let existingArr = form[formKey] || []
  // 合并数组
  let mergedArr = existingArr.concat(arr)
  // 使用Map去重，O(n)时间复杂度，比filter+findIndex的O(n²)更高效
  const uniqueMap = new Map()
  mergedArr.forEach(item => {
    uniqueMap.set(item.userId, item)
  })
  // 转换回数组
  form[formKey] = Array.from(uniqueMap.values())
  console.log(form[formKey],'form[formKey]')
}

// 获取用户名称
const getShowName = (row) => {
  let name = row.realName || row.nickName || row.userName || '-'
  return name
}

// ************** 用户组管理员相关 **************
/**
 * 打开设置用户组管理员弹窗
 */
const handleAdminClick = () => {
  userSelectRef.value.openDilogUser({
    selectedUsers: form.groupManagerList || [],
    formKey: 'groupManagerList',
    title: '设置用户组管理员',
    userType: '0'
  })
}

/**
 * 选择用户组管理员回调
 * @param {Array} val - 选中的用户数组
 * @param {string} formKey - 表单字段名
 */
const selectUuserGroupAdmin = (val, formKey) => {
  // handleUserSelect(val, formKey)
  form[formKey] = val
}

/**
 * 关闭用户组管理员标签
 * @param {Object} tag - 要关闭的管理员标签对象
 */
const handleClose = (tag) => {
  console.log(tag,form.groupManagerList,'tag')
  let index = form.groupManagerList.findIndex(item => item.userId === tag.userId)
  if (index !== -1) {
    form.groupManagerList.splice(index, 1)
  }
}

// ************** 关联用户相关 **************
/**
 * 打开添加用户弹窗
 */
const handleAddUser = () => {
  userSelectRef.value.openDilogUser({
    selectedUsers: form.userIds || [],
    formKey: 'userIds',
    title: '添加白名单',
    userType: '2'
  })
}

/**
 * 删除关联用户
 * @param {Object} row - 要删除的用户对象
 */
const handleDelete = (row) => {
  let index = form.userIds.findIndex(item => item.userId === row.userId)
  if (index !== -1) {
    form.userIds.splice(index, 1)
  }
}

// ************** 禁止名单相关 **************
/**
 * 打开添加禁止名单弹窗
 */
const handleHeimingdan = () => {
  userSelectRef.value.openDilogUser({
    selectedUsers: form.blackUserIds || [],
    formKey: 'blackUserIds',
    title: '添加禁止名单',
    userType: '2'
  })
}

/**
 * 删除禁止名单用户
 * @param {Object} row - 要删除的用户对象
 */
const handleDeleteHeimingdan = (row) => {
  let index = form.blackUserIds.findIndex(item => item.userId === row.userId)
  if (index !== -1) {
    form.blackUserIds.splice(index, 1)
  }
}

// ************** 树状结构数据 **************
// 关联赛事树状数据
let treeLoading = ref(false)
let treeData = $ref([])
function getTreeData() {
  treeLoading.value = true
  // 实际项目中应调用API获取数据
  selectAllCompetitionDetailInfo().then(response => {
     treeLoading.value = false
    if (response.code == 200) {
      let treeArr = response.data;
      treeData = treeArr.map((item)=>{
        let children = []
        let competitionStageConfigList = item.competitionStageConfigList.map((stageItem)=>{
          return {
            ...stageItem,
            id: stageItem.stageId,
            label: stageItem.stageName
          }
        })
        children = [...item.competitionChildren || [],...competitionStageConfigList || []]
        return {
          ...item,
          id: item.competitionSeriesId,
          label: item.competitionSeriesName+item.competitionName,
          children: children || []
        }
      })
    }
  }).catch(() => {
    treeLoading.value = false
  })
}

// ************** 表单提交相关 **************
// 定义事件
const emit = defineEmits(['submit'])
// 表单引用
const formInfoRef = ref(null)
/**
 * 提交表单
 * @description 验证表单，提交成功后关闭弹窗并触发提交事件
 */
let treeRef = ref(null)
const submitForm = () => {
  let formData = cloneDeep(form)
  const checkedNodes = treeRef.value.getCheckedNodes(); // 全选节点
  formData.sysUserGroupCompetitionRelationList = checkedNodes
  formData = {
    ...formData,
    groupManagerList: formData?.groupManagerList?.length ? formData.groupManagerList.map((e)=>{
      return {
        userId: e.userId,
        userName: getShowName(e),
        phoneNumber: e.phoneNumber,
      }
    }) : [],
    sysUserGroupCompetitionRelationList: [...checkedNodes],
    userIds: formData?.userIds?.length ? formData.userIds.map(item => item.userId).join(',') : '',
    blackUserIds: formData?.blackUserIds?.length ? formData.blackUserIds.map(item => item.userId).join(',') : '',
    identifyType: formData?.identifyType?.length ? formData.identifyType.join(',') : '',
    allowRoleName: formData?.allowRoleName?.length ? formData.allowRoleName.join(',') : '',
  }
  delete formData.userList
  delete formData.blackUserList
  formInfoRef.value.validate((valid) => {
    if (valid) {
      let func = formData.id ? systemUserGroupUpdate : systemUserGroupAdd
      func(formData).then(response => {
        if (response.code == 200) {
          modal.msgSuccess('提交成功')
          open = false
          emit('submit', form) // 传递完整表单数据，而非固定字符串
        } else {
          modal.msgWarning(response.msg || '提交失败')
        }
      })
    } else {
      modal.msgWarning('请填写正确的信息')
    }
  })
}

getTreeData()

// ************** 暴露方法 **************
// 暴露给父组件的方法
defineExpose({
  openDilog
})
</script>
<style scoped lang="scss">
.mon-title {
  font-weight: 600;
  margin-bottom: 10px;
  position: relative;
  background: #F5F5F5;
  padding: 8px 15px;
  &::after {
    content: '';
    display: block;
    width: 4px;
    height: 50%;
    position: absolute;
    top: 25%;
    left: 6px;
    border-radius: 3px;
    background-color:#409EFF;
  }
}
</style>