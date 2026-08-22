<template>
    <div>
      
      <el-select
        style="width: 100%;"
        ref="selectRef"
        :size="size"
        v-model="selectedData"
        :multiple="multiple"
        :clearable="clearable"
        :collapse-tags="collapseTags"
        @click="clickSelect"
        @remove-tag="removeSelectedNodes"
        @clear="removeSelectedNode"
        @change="changeSelectedNodes"
        class="tree-select">
        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>

      <el-drawer
        v-model="isShowSelect"
        title="选择部门"
        direction="rtl"
        :before-close="handleClose">
        <el-input
          v-model="filterText"
          style="width: 100%"
          placeholder="请输入关键字查询"
        />
        <el-tree
          class="common-tree"
          ref="treeRef"
          :style="`width: 100%; height: 100%`"
          :data="data"
          :props="defaultProps"
          :show-checkbox="multiple"
          :node-key="nodeKey"
          :check-strictly="checkStrictly"
          :default-expand-all="false"
          :expand-on-click-node="false"
          :check-on-click-node="multiple"
          :highlight-current="true"
          :filter-node-method="filterNode"
          @node-click="handleNodeClick"
          @check-change="handleCheckChange"
        />
        <template #footer>
          <div class="dialog-footer">
              <el-button @click="cancel">取 消</el-button>
              <el-button type="primary" @click="submitForm">确 定</el-button>
          </div>
        </template>
      </el-drawer>
      
    </div>
  </template>
  
  <script setup name="TreeSelectFlow">
  import { nextTick } from 'vue';
import { ref, reactive, onMounted, watch } from 'vue'
  
  const props = defineProps({
    // 树结构数据
    data: {
      type: Array,
      default() {
        return [];
      }
    },
    defaultProps: {
      type: Object,
      default() {
        return {};
      }
    },
    // 配置是否可多选
    multiple: {
      type: Boolean,
      default: false
    },
    // 配置是否可清空选择
    clearable: {
      type: Boolean,
      default: false
    },
    // 配置多选时是否将选中值按文字的形式展示
    collapseTags: {
      type: Boolean,
      default: false
    },
    nodeKey: {
      type: String,
      default: 'id'
    },
    // 显示复选框情况下，是否严格遵循父子不互相关联
    checkStrictly: {
      type: Boolean,
      default: false
    },
    // 默认选中的节点key数组
    checkedKeys: {
      type: Array,
      default() {
        return [];
      }
    },
    size: {
      type: String,
      default: 'default'
    },
    width: {
      type: Number,
      default: 250
    },
    height: {
      type: Number,
      default: 300
    }
  })
  
  const emits = defineEmits(['change', 'check-change'])
  
  const treeRef = ref(null)
  const selectRef = ref(null)
  
  const isShowSelect = ref(false) // 是否显示树状选择器
  const options = ref([])
  const selectedData = ref([]) // 选中的节点
  // const style = ref('width:' + props.width + 'px;' + 'height:' + props.height + 'px;')
  // const selectStyle = ref('width:' + (props.width + 24) + 'px;')
  const checkedIds = ref([])
  const checkedData = ref([])
  const filterText = ref('');
  
  watch(isShowSelect, () => {
    selectRef.value.blur();
  })
  
  watch(() => props.checkedKeys, (newVal) => {
    if (!newVal) return;
    selectedData.value = newVal;
    options.value = newVal.map((item) => {
      let obj = findNodeById(props.data, item);
      return {
        label: obj.label,
        value: obj.id
      }
    });
    
  },{ immediate: true })

  watch(filterText, (val) => {
    treeRef.value.filter(val)
  })

  const filterNode = (value, data) => {
    if (!value) return true
    return data.label.includes(value)
  }

  function findNodeById(nodes, id) {
    for (const node of nodes) {
        if (node.id === id) {
            return node; // 找到节点，返回它
        } else if (node.children) {
            const found = findNodeById(node.children, id); // 递归查找子节点
            if (found) {
                return found; // 如果在子节点中找到，返回它
            }
        }
    }
    return null; // 未找到节点，返回null
  }

  
  // 单选时点击tree节点，设置select选项
  function setSelectOption(node) {
    let tmpMap = {};
    tmpMap.value = node.key;
    tmpMap.label = node.label;
    options.value = [];
    options.value.push(tmpMap);
    selectedData.value = node.key;
  }
  
  // 单选，选中传进来的节点
  function checkSelectedNode(checkedKeys) {
    var item = checkedKeys[0];
    treeRef.value.setCurrentKey(item);
    var node = treeRef.value.getNode(item);
    setSelectOption(node);
  }
  
  // 多选，勾选上传进来的节点
  function checkSelectedNodes(checkedKeys) {
    nextTick(() => {
      treeRef.value.setCheckedKeys(checkedKeys);
    })
  }
  
  // 单选，清空选中
  function clearSelectedNode() {
    treeRef.value.setCurrentKey(null);
  }
  
  // 多选，清空所有勾选
  function clearSelectedNodes() {
    nextTick(() => {
      var checkedKeys = treeRef.value.getCheckedKeys(); // 所有被选中的节点的 key 所组成的数组数据
      for (let i = 0; i < checkedKeys.length; i++) {
        treeRef.value.setChecked(checkedKeys[i], false);
      }
    })

  }
  
  function initCheckedData() {
    if (props.multiple) {
      // 多选
      if (props.checkedKeys.length > 0) {
        selectedData.value = props.checkedKeys;
        checkSelectedNodes(props.checkedKeys);
      } else {
        selectedData.value = '';
        clearSelectedNodes();
      }
    } else {
      // 单选
      if (props.checkedKeys.length > 0) {
        selectedData.value = props.checkedKeys[0];
        checkSelectedNode(props.checkedKeys);
      } else {
        selectedData.value = '';
        clearSelectedNode();
      }
    }
  }
  
  // onMounted(() => {
    
  // })
  
  function a2b(ls) {
    return ls.map(obj => {
      let result = { id: '', text: '' };
      if (obj.children && obj.children.length > 0) {
        result.id = obj.value;
        result.text = obj.label;
        result.arr = a2b(obj.children);
        return result;
      } else {
        result.id = obj.value;
        result.text = obj.label;
        return result;
      }
    });
  }
  
  function popoverHide() {
    if (props.multiple) {
      checkedIds.value = treeRef.value.getCheckedKeys(); // 所有被选中的节点的 key 所组成的数组数据
      checkedData.value = treeRef.value.getCheckedNodes(); // 所有被选中的节点所组成的数组数据
    } else {
      checkedIds.value = treeRef.value.getCurrentKey();
      checkedData.value = treeRef.value.getCurrentNode();
    }
    emits('checked-change', checkedIds.value, checkedData.value);
  }
  
  // 单选，节点被点击时的回调,返回被点击的节点数据
  function handleNodeClick(data, node) {
    if (!props.multiple) {
      setSelectOption(node);
      isShowSelect.value = !isShowSelect.value;
      // emits('change', selectedData.value);
    }
  }
  
  // 多选，节点勾选状态发生变化时的回调
  function handleCheckChange() {
    var checkedKeys = treeRef.value.getCheckedKeys(); // 所有被选中的节点的 key 所组成的数组数据
    options.value = checkedKeys.map((item) => {
      var node = treeRef.value.getNode(item); // 所有被选中的节点对应的node
      let tmpMap = {};
      tmpMap.value = node.key;
      tmpMap.label = node.label;
      return tmpMap;
    });
    selectedData.value = options.value.map((item) => {
      return item.value;
    });
    // emits('change', selectedData.value);
  }
  
  // 多选,删除任一select选项的回调
  function removeSelectedNodes(val) {
    treeRef.value.setChecked(val, false);
    var node = treeRef.value.getNode(val);
    if (!props.checkStrictly && node.childNodes.length > 0) {
      treeToList(node).map(item => {
        if (item.childNodes.length <= 0) {
          treeRef.value.setChecked(item, false);
        }
      });
      handleCheckChange();
    }
    emits('change', selectedData.value);
  }
  
  function treeToList(tree) {
    var queen = [];
    var out = [];
    queen = queen.concat(tree);
    while (queen.length) {
      var first = queen.shift();
      if (first.childNodes) {
        queen = queen.concat(first.childNodes);
      }
      out.push(first);
    }
    return out;
  }
  
  // 单选,清空select输入框的回调
  function removeSelectedNode() {
    clearSelectedNode();
    emits('change', selectedData.value);
  }
  
  // 选中的select选项改变的回调
  function changeSelectedNodes(selectedData) {
    // 多选,清空select输入框时，清除树勾选
    if (props.multiple && selectedData.length <= 0) {
      clearSelectedNodes();
    }
    // emits('change', selectedData.value);
  }

  // 点击select
  function clickSelect () {
    isShowSelect.value = true;
    initCheckedData();
  }

  // 关闭
  function handleClose (done) {
    done();
  }

  // 弹窗取消
  function cancel () {
    isShowSelect.value = false;
  }

  // 弹窗确定
  function submitForm () {
    emits('change', selectedData.value);
    isShowSelect.value = false;
  }

  </script>
  
  <style scoped>
  .container {
    width: 100%;
  }
  .common-tree {
    overflow: auto;
  }
  
  .tree-select {
    z-index: 111;
  }
  </style>
  