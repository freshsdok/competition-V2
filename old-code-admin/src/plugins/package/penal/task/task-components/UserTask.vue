<template>
  <div>
    <el-row>
      <el-col :span="24">
        <h4><b>审批人设置</b></h4>
      </el-col>
      <el-radio-group v-model="dataType" @change="changeDataType">
        <el-radio label="USERS">指定用户</el-radio>
        <el-radio label="ROLES">角色</el-radio>
        <!-- <el-radio label="DEPTS">部门</el-radio>
        <el-radio label="POSTS">岗位</el-radio> -->
        <el-radio label="INITIATOR">发起人</el-radio>
      </el-radio-group>
    </el-row>
    <el-row style="width: 100%;">
      <div v-if="dataType === 'USERS'" style="width: 100%;">
        <el-tag v-for="userText in selectedUser.text" :key="userText" effect="plain">
          {{userText}}
        </el-tag>
        <div class="element-drawer__button">
          <el-button size="default" type="primary" @click="onSelectUsers()">指定用户</el-button>
        </div>
      </div>
      <div style="width: 100%;" v-if="dataType === 'ROLES'">
        <el-select style="width: 100%;" v-model="roleIds" multiple filterable size="default" placeholder="请选择角色" @change="changeSelectRoles">
          <el-option
            v-for="item in roleOptions"
            :key="item.roleId"
            :label="item.roleName"
            :value="`ROLE${item.roleId}`"
            :disabled="item.status === 1">
          </el-option>
        </el-select>
      </div>
      <div style="width: 100%;" v-if="dataType === 'DEPTS'">
        <tree-select-flow
          v-if="deptTreeData.length>0"
          :width="320"
          :height="400"
          :data="deptTreeData"
          :defaultProps="deptProps"
          multiple
          clearable
          checkStrictly
          nodeKey="id"
          :checkedKeys="deptIds"
          @change="checkedDeptChange">
        </tree-select-flow>
      </div>
      <div style="width: 100%;" v-if="dataType === 'POSTS'">
        <el-select style="width: 100%;" v-model="postIds" multiple filterable placeholder="请选择岗位" @change="changeSelectPosts">
          <el-option
              v-for="item in postOptions"
              :key="item.postId"
              :label="item.postName"
              :value="`POST${item.postId}`"
              :disabled="item.status === 1">
          </el-option>
        </el-select>
      </div>
    </el-row>
    <el-row>
      <div v-show="showMultiFlog">
        <el-divider />
        <h4><b>多实例审批方式</b></h4>
        <el-row>
          <el-radio-group v-model="multiLoopType" @change="changeMultiLoopType()">
            <div>
              <el-radio label="Null">无</el-radio>
              <el-radio label="SequentialMultiInstance">会签（需所有审批人同意）</el-radio>
              <el-radio label="ParallelMultiInstance">或签（一名审批人同意即可）</el-radio>
            </div>
          </el-radio-group>
        </el-row>
        <el-row v-if="multiLoopType !== 'Null'">
          <el-tooltip content="开启后，实例需按顺序轮流审批" placement="top-start" @click.stop.prevent>
            <el-icon><InfoFilled /></el-icon>
          </el-tooltip>
          <span class="custom-label">顺序审批：</span>
          <el-switch v-model="isSequential" @change="changeMultiLoopType()" />
        </el-row>
      </div>
    </el-row>

    <!--  选择用户  -->
    <user-select
        ref="userSelect"
        :defaultSelectList="selectedUserDate"
        @confirm="handleTaskUserComplete">
    </user-select>

  </div>

</template>

<script>
import { listUser, deptTreeSelect } from "@/api/system/user";
import { listRole } from "@/api/system/role";
import { listPost } from "@/api/system/post";
import TreeSelectFlow from "@/components/TreeSelectFlow/index.vue";
import UserSelect from "@/components/UserSelect/index.vue";
import {selectUserByIds} from "@/api/userSelect";

const userTaskForm = {
  dataType: '',
  assignee: '',
  candidateUsers: '',
  candidateGroups: '',
  text: '',
  // dueDate: '',
  // followUpDate: '',
  // priority: ''
}

export default {
  name: "UserTask",
  props: {
    id: String,
    type: String
  },
  components: {UserSelect, TreeSelectFlow },
  data() {
    return {
      loading: false,
      dataType: 'USERS',
      selectedUser: {
        ids: [],
        text: []
      },
      userOpen: false,
      deptName: undefined,
      deptOptions: [],
      deptProps: {
        children: "children",
        label: "label"
      },
      deptTempOptions: [],
      userTableList: [],
      userTotal: 0,
      selectedUserDate: [],
      roleOptions: [],
      roleIds: [],
      deptTreeData: [],
      deptIds: [],
      postOptions:[],
      postIds:[],
      // 查询参数
      queryParams: {
        deptId: undefined
      },
      showMultiFlog: false,
      isSequential: false,
      multiLoopType: 'Null',
    };
  },
  watch: {
    id: {
      immediate: true,
      handler() {
        this.bpmnElement = window.bpmnInstances.bpmnElement;
        this.$nextTick(() => this.resetTaskForm());
      }
    },
    // 根据名称筛选部门树
    deptName(val) {
      this.$refs.tree.filter(val);
    }
  },
  beforeDestroy() {
    this.bpmnElement = null;
  },
  methods: {
    resetTaskForm() {
      const bpmnElementObj = this.bpmnElement?.businessObject;
      if (!bpmnElementObj) {
        return;
      }
      this.clearOptionsData()
      this.dataType = bpmnElementObj['dataType'];
      if (this.dataType === 'USERS') {
        let userIdData = bpmnElementObj['candidateUsers'] || bpmnElementObj['assignee'];
        let userText = bpmnElementObj['text'] || [];
               this.selectedUserDate = [];
        if (userIdData && userIdData.toString().length > 0 && userText && userText.length > 0) {
          this.selectedUser.ids = userIdData?.toString().split(',');
          this.selectedUser.text = userText?.split(',');
          this.selectedUserDate = this.selectedUser.ids.map((item,index) => ({
            userId: item,
            nickName: this.selectedUser.text[index] || ''
          }))
        }else {
          this.selectedUser.ids = [];
          this.selectedUser.text = [];
          this.selectedUserDate = [];
        }
        console.log(userIdData,'userIdDataxxxx')
        console.log(userText,'userIdD123123123ataxxxx')
        console.log(this.selectedUserDate,'userIdD123123123ataxxxx')
        if (this.selectedUser.ids.length > 1) {
          this.showMultiFlog = true;
        };
      } else if (this.dataType === 'ROLES') {
        this.getRoleOptions();
        let roleIdData = bpmnElementObj['candidateGroups'] || [];
        if (roleIdData && roleIdData.length > 0) {
          this.roleIds = roleIdData.split(',')
        }
        this.showMultiFlog = true;
      } else if (this.dataType === 'DEPTS') {
        this.getDeptTreeData();
        let deptIdData = bpmnElementObj['candidateGroups'] || [];
        if (deptIdData && deptIdData.length > 0) {
          this.deptIds = deptIdData.split(',');
        }
        this.showMultiFlog = true;
      } else if (this.dataType === 'POSTS'){
        this.getPostOptions();
        let postIdData = bpmnElementObj['candidateGroups'] || [];
        if (postIdData && postIdData.length > 0) {
          this.postIds = postIdData.split(',')
        }
        this.showMultiFlog = true;
      }
      this.getElementLoop(bpmnElementObj);
    },
    /**
     * 清空选项数据
     */
    clearOptionsData() {
      this.selectedUser.ids = [];
      this.selectedUser.text = [];
      this.roleIds = [];
      this.deptIds = [];
      this.postIds = [];
    },
    /**
     * 跟新节点数据
     */
    updateElementTask() {
      const taskAttr = Object.create(null);
      for (let key in userTaskForm) {
          taskAttr[key] = userTaskForm[key];
      }
      window.bpmnInstances.modeling.updateProperties(this.bpmnElement, taskAttr);
    },
    /**
     * 查询部门下拉树结构
     */
    getDeptOptions() {
      return new Promise((resolve, reject) => {
        if (!this.deptOptions || this.deptOptions.length <= 0) {
          deptTreeSelect().then(response => {
            this.deptTempOptions = response.data;
            this.deptOptions = response.data;
            resolve()
          })
        } else {
          reject()
        }
      });
    },
    /**
     * 查询部门下拉树结构（含部门前缀）
     */
    getDeptTreeData() {
      function refactorTree(data) {
        return data.map(node => {
          let treeData = { id: `DEPT${node.id}`, label: node.label, parentId: node.parentId, weight: node.weight };
          if (node.children && node.children.length > 0) {
            treeData.children = refactorTree(node.children);
          }
          return treeData;
        });
      }
      return new Promise((resolve, reject) => {
        if (!this.deptTreeData || this.deptTreeData.length <= 0) {
          this.getDeptOptions().then(() => {
            this.deptTreeData = refactorTree(this.deptOptions);
            resolve()
          }).catch(() => {
            reject()
          })
        } else {
          resolve()
        }
      })
    },
    /**
     * 查询角色列表
     */
    getRoleOptions() {
      if (!this.roleOptions || this.roleOptions.length <= 0) {
        listRole({
          pageNum: 1,
          pageSize: 1000
        }).then(response => this.roleOptions = response.rows);
      }
    },
    /** 查询岗位列表 */
    getPostOptions() {
      if (!this.postOptions || this.postOptions.length <= 0) {
        listPost({
          pageNum: 1,
          pageSize: 1000
        }).then(response => this.postOptions = response.rows);
      }
    },
    /** 查询用户列表 */
    getUserList() {
      listUser(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.userTableList = response.rows;
        this.userTotal = response.total;
      });
    },
    // 筛选节点
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    // 节点单击事件
    handleNodeClick(data) {
      this.queryParams.deptId = data.id;
      this.getUserList();
    },
    // 关闭标签
    handleClose(tag) {
      this.selectedUserDate.splice(this.selectedUserDate.indexOf(tag), 1);
      this.$refs.multipleTable.toggleRowSelection(tag);
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.selectedUserDate = selection;
    },
    onSelectUsers() {
      console.log('点击一下？？？','onSelectUsers',this.selectedUserDate)
      this.userOpen = true;
      this.$refs.userSelect?.openDilogUser({
        selectedUsers: this.selectedUserDate || []
      })
    },
    handleTaskUserComplete(val) {
      this.selectedUserDate = val;
      console.log(val,'val/handleTaskUserComplete')
      if (!this.selectedUserDate || this.selectedUserDate.length <= 0) {
        this.$modal.msgError('请选择用户');
        return;
      }
      userTaskForm.dataType = 'USERS';
      this.selectedUser.text = this.selectedUserDate.map(k => k.nickName) || [];
      if (this.selectedUserDate.length === 1) {
        let data = this.selectedUserDate[0];
        userTaskForm.assignee = data.userId;
        userTaskForm.text = data.nickName;
        userTaskForm.candidateUsers = null;
        this.showMultiFlog = false;
        this.multiLoopType = 'Null';
        this.changeMultiLoopType();
      } else {
        userTaskForm.candidateUsers = this.selectedUserDate.map(k => k.userId).join() || null;
        userTaskForm.text = this.selectedUserDate.map(k => k.nickName).join() || null;
        userTaskForm.assignee = null;
        this.showMultiFlog = true;
      }
      this.updateElementTask()
      this.userOpen = false;
    },
    changeSelectRoles(val) {
      let groups = null;
      let text = null;
      if (val && val.length > 0) {
        userTaskForm.dataType = 'ROLES';
        groups = val.join() || null;
        let textArr = this.roleOptions.filter(k => val.indexOf(`ROLE${k.roleId}`) >= 0);
        text = textArr?.map(k => k.roleName).join() || null;
      } else {
        userTaskForm.dataType = null;
        this.multiLoopType = 'Null';
      }
      userTaskForm.candidateGroups = groups;
      userTaskForm.text = text;
      this.updateElementTask();
      this.changeMultiLoopType();
    },
    checkedDeptChange(checkedIds) {
      let groups = null;
      let text = null;
      this.deptIds = checkedIds;
      if (checkedIds && checkedIds.length > 0) {
        userTaskForm.dataType = 'DEPTS';
        groups = checkedIds.join() || null;
        let textArr = []
        let treeStarkData = JSON.parse(JSON.stringify(this.deptTreeData));
        checkedIds.forEach(id => {
          let stark = []
          stark = stark.concat(treeStarkData);
          while(stark.length) {
            let temp = stark.shift();
            if(temp.children) {
              stark = temp.children.concat(stark);
            }
            if(id === temp.id) {
              textArr.push(temp);
            }
          }
        })
        text = textArr?.map(k => k.label).join() || null;
      } else {
        userTaskForm.dataType = null;
        this.multiLoopType = 'Null';
      }
      userTaskForm.candidateGroups = groups;
      userTaskForm.text = text;
      this.updateElementTask();
      this.changeMultiLoopType();
    },
    changeSelectPosts(val) {
      let groups = null;
      let text = null;
      if (val && val.length > 0) {
        userTaskForm.dataType = 'POSTS';
        groups = val.join() || null;
        let textArr = this.postOptions.filter(k => val.indexOf(`POST${k.postId}`) >= 0);
        text = textArr?.map(k => k.postName).join() || null;
      } else {
        userTaskForm.dataType = null;
        this.multiLoopType = 'Null';
      }
      userTaskForm.candidateGroups = groups;
      userTaskForm.text = text;
      this.updateElementTask();
      this.changeMultiLoopType();
    },
    changeDataType(val) {
      if (val === 'ROLES' || val === 'DEPTS' || val === 'POSTS' || (val === 'USERS' && this.selectedUser.ids.length > 1)) {
        this.showMultiFlog = true;
      } else {
        this.showMultiFlog = false;
      }
      this.multiLoopType = 'Null';
      this.changeMultiLoopType();
      // 清空 userTaskForm 所有属性值
      Object.keys(userTaskForm).forEach(key => userTaskForm[key] = null);
      userTaskForm.dataType = val;
      if (val === 'USERS') {
        if (this.selectedUser && this.selectedUser.ids && this.selectedUser.ids.length > 0) {
          if (this.selectedUser.ids.length === 1) {
            userTaskForm.assignee = this.selectedUser.ids[0];
          } else {
            userTaskForm.candidateUsers = this.selectedUser.ids.join()
          }
          userTaskForm.text = this.selectedUser.text?.join() || null
        }
      } else if (val === 'ROLES') {
        this.getRoleOptions();
        if (this.roleIds && this.roleIds.length > 0) {
          userTaskForm.candidateGroups = this.roleIds.join() || null;
          let textArr = this.roleOptions.filter(k => this.roleIds.indexOf(`ROLE${k.roleId}`) >= 0);
          userTaskForm.text = textArr?.map(k => k.roleName).join() || null;
        }
      } else if (val === 'DEPTS') {
        this.getDeptTreeData();
        if (this.deptIds && this.deptIds.length > 0) {
          userTaskForm.candidateGroups = this.deptIds.join() || null;
          let textArr = []
          let treeStarkData = JSON.parse(JSON.stringify(this.deptTreeData));
          this.deptIds.forEach(id => {
            let stark = []
            stark = stark.concat(treeStarkData);
            while(stark.length) {
              let temp = stark.shift();
              if(temp.children) {
                stark = temp.children.concat(stark);
              }
              if(id === temp.id) {
                textArr.push(temp);
              }
            }
          })
          userTaskForm.text = textArr?.map(k => k.label).join() || null;
        }
      }else if (val === 'POSTS'){
        this.getPostOptions();
        if (this.postIds && this.postIds.length > 0) {
          userTaskForm.candidateGroups = this.postIds.join() || null;
          let textArr = this.postOptions.filter(k => this.postIds.indexOf(`POST${k.postId}`) >= 0);
          userTaskForm.text = textArr?.map(k => k.postName).join() || null;
        }
      }else if (val === 'INITIATOR') {
        userTaskForm.assignee = "${initiator}";
        userTaskForm.text = "流程发起人";
      }
      this.updateElementTask();
    },
    getElementLoop(businessObject) {
      if (!businessObject.loopCharacteristics) {
        this.multiLoopType = "Null";
        return;
      }
      this.isSequential = businessObject.loopCharacteristics.isSequential;
      if (businessObject.loopCharacteristics.completionCondition) {
        if (businessObject.loopCharacteristics.completionCondition.body === "${nrOfCompletedInstances >= nrOfInstances}") {
          this.multiLoopType = "SequentialMultiInstance";
        } else {
          this.multiLoopType = "ParallelMultiInstance";

        }
      }
    },
    changeMultiLoopType() {
      // 取消多实例配置
      if (this.multiLoopType === "Null") {
        window.bpmnInstances.modeling.updateProperties(this.bpmnElement, { loopCharacteristics: null, assignee: null });
        return;
      }
      this.multiLoopInstance = window.bpmnInstances.moddle.create("bpmn:MultiInstanceLoopCharacteristics", { isSequential: this.isSequential });
      // 更新多实例配置
      window.bpmnInstances.modeling.updateProperties(this.bpmnElement, {
        loopCharacteristics: this.multiLoopInstance,
        assignee: '${assignee}'
      });
      // 完成条件
      let completionCondition = null;
      // 会签
      if (this.multiLoopType === "SequentialMultiInstance") {
        completionCondition = window.bpmnInstances.moddle.create("bpmn:FormalExpression", { body: "${nrOfCompletedInstances >= nrOfInstances}" });
      }
      // 或签
      if (this.multiLoopType === "ParallelMultiInstance") {
        completionCondition = window.bpmnInstances.moddle.create("bpmn:FormalExpression", { body: "${nrOfCompletedInstances > 0}" });
      }
      // 更新模块属性信息
      window.bpmnInstances.modeling.updateModdleProperties(this.bpmnElement, this.multiLoopInstance, {
        collection: '${multiInstanceHandler.getUserIds(execution)}',
        elementVariable: 'assignee',
        completionCondition
      });
    },
  }
};
</script>

<style scoped lang="scss">
.el-radio-group {
  margin-bottom: 15px;
  .el-radio {
    line-height: 28px;
  }
}
.el-tag {
  margin-bottom: 10px;
  + .el-tag {
    margin-left: 10px;
  }
}

.custom-label {
  padding-left: 5px;
  font-weight: 500;
  font-size: 14px;
  color: #606266;
}
.head-container {
  height: 523px;
  overflow: auto;
}
</style>
