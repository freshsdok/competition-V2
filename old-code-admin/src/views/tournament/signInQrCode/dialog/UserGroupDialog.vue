<template>
  <el-dialog
    :title="dialogTitle"
    v-model="visible"
    width="1100px"
    append-to-body
    :close-on-click-modal="false"
  >
    <el-row :gutter="20">
      <!-- 左侧用户组列表 -->
      <el-col :span="16">
        <div style="display: flex; margin-bottom: 15px">
          <el-input
            style="width: 200px"
            placeholder="请输入用户组"
            v-model.trim="leftQuery.name"
            clearable
          />
          <el-button
            style="margin-left: 10px"
            type="primary"
            @click="handleSearch"
          >搜索</el-button>
        </div>
        <el-table
          ref="leftTableRef"
          :data="userGroupList"
          row-key="id"
          @select="handleSelect"
          @select-all="handleSelectAll"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="name" label="用户组" />
          <el-table-column prop="descripe" label="描述" />
        </el-table>
        <pagination
          v-show="leftTotal > 0"
          :total="leftTotal"
          size="small"
          v-model:page="leftQuery.pageNum"
          v-model:limit="leftQuery.pageSize"
          @pagination="loadUserGroupList"
        />
      </el-col>

      <!-- 右侧已选用户组 -->
      <el-col :span="8">
        <div
          style="
            display: flex;
            height: 32px;
            justify-content: space-between;
            margin-bottom: 15px;
            align-items: center;
          "
        >
          <span>已选择 {{ selectedGroups.length }} 项</span>
          <span style="color: red; cursor: pointer" @click="handleClear">清空</span>
        </div>
        <div style="display: flex; flex-direction: column">
          <el-tag
            style="
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin: 0px 0 10px 0;
            "
            v-for="(item, index) in selectedGroups"
            :key="item.id"
            type="info"
            closable
            @close="handleClose(item, index)"
          >{{ item.name }}</el-tag>
        </div>
      </el-col>
    </el-row>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import Pagination from '@/components/Pagination'
import { saveUserGroupSetting } from '@/api/tournament/signInQrCode'
import { systemUserGroupMangerList } from '@/api/fileTask'
import modal from '@/plugins/modal'

const props = defineProps({
  mode: {
    type: String,
    default: 'edit' // 'edit' 或 'select'
  }
})

const emit = defineEmits(['success'])

const visible = ref(false)
const loading = ref(false)
const currentRow = ref(null)
const dialogTitle = ref('用户组设置')

// 回调函数（select模式用）
let selectCallback = null

// 左侧列表
const leftTableRef = ref(null)
const userGroupList = ref([])
const leftTotal = ref(0)
const leftQuery = ref({
  pageNum: 1,
  pageSize: 10,
  name: undefined
})

// 右侧已选
const selectedGroups = ref([])

/** 打开弹框（编辑模式） */
function openDialog(row) {
  dialogTitle.value = '用户组设置'
  currentRow.value = row
  selectCallback = null
  visible.value = true
  leftQuery.value = {
    pageNum: 1,
    pageSize: 10,
    name: undefined
  }
  // 初始化已选数据 - 从列表传入的userGroupIds和userGroupNames解析
  if (row.userGroupIds && row.userGroupNames) {
    const ids = row.userGroupIds.split(',').filter(id => id)
    const names = row.userGroupNames.split(',').filter(name => name)
    selectedGroups.value = ids.map((id, index) => ({
      id: parseInt(id),
      name: names[index] || id
    }))
  } else {
    selectedGroups.value = []
  }
  nextTick(() => {
    loadUserGroupList()
  })
}

/** 打开弹框（新增选择模式） */
function openDialogForAdd(initialGroups = [], callback) {
  dialogTitle.value = '选择用户组'
  currentRow.value = null
  selectCallback = callback
  visible.value = true
  leftQuery.value = {
    pageNum: 1,
    pageSize: 10,
    name: undefined
  }
  // 初始化已选数据
  selectedGroups.value = initialGroups ? [...initialGroups] : []
  nextTick(() => {
    loadUserGroupList()
  })
}

/** 加载用户组列表 */
async function loadUserGroupList() {
  loading.value = true
  let response = await systemUserGroupMangerList(leftQuery.value)
  loading.value = false
  if (response.code == 200) {
    userGroupList.value = response.rows || []
    leftTotal.value = response.total || 0
    nextTick(() => {
      syncSelection()
    })
  }
  return response
}

/** 同步左侧选中状态 */
function syncSelection() {
  if (!leftTableRef.value) return
  leftTableRef.value.clearSelection()
  console.log(selectedGroups.value)
  userGroupList.value.forEach(row => {
    const isSelected = selectedGroups.value.some(item => item.id === row.id)
    if (isSelected) {
      leftTableRef.value.toggleRowSelection(row, true)
    }
  })
}

/** 搜索 */
function handleSearch() {
  leftQuery.value.pageNum = 1
  loadUserGroupList()
}

/** 单选 */
function handleSelect(selection, row) {
  const isSelected = selection.some(item => item.id === row.id)
  if (isSelected) {
    // 选中，添加到右侧
    const exists = selectedGroups.value.some(item => item.id === row.id)
    if (!exists) {
      selectedGroups.value.push(row)
    }
  } else {
    // 取消选中，从右侧移除
    const index = selectedGroups.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      selectedGroups.value.splice(index, 1)
    }
  }
}

/** 全选 */
function handleSelectAll(selection) {
  if (selection.length > 0) {
    // 全选，添加所有到右侧
    selection.forEach(row => {
      const exists = selectedGroups.value.some(item => item.id === row.id)
      if (!exists) {
        selectedGroups.value.push(row)
      }
    })
  } else {
    // 取消全选，从右侧移除当前页的数据
    userGroupList.value.forEach(row => {
      const index = selectedGroups.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        selectedGroups.value.splice(index, 1)
      }
    })
  }
}

/** 移除单个 - 根据ID删除，不使用index */
function handleClose(item) {
  const index = selectedGroups.value.findIndex(group => group.id === item.id)
  console.log(item,'xxxxxxxxxxxxxxxxxxxx')
   console.log(index) 
   console.log(selectedGroups.value) 
  if (index > -1) {
    selectedGroups.value.splice(index, 1)
  }
  nextTick(() => {
    syncSelection()
  })
}

/** 清空 */
function handleClear() {
  selectedGroups.value = []
  nextTick(() => {
    syncSelection()
  })
}

/** 取消 */
function cancel() {
  visible.value = false
}

/** 提交 */
function submitForm() {
  // 如果是选择模式，直接回调
  if (props.mode === 'select' && selectCallback) {
    selectCallback(selectedGroups.value)
    visible.value = false
    return
  }

  // 编辑模式，保存到后端
  const data = {
    codeConfigId: currentRow.value.codeConfigId,
    userGroupIds: selectedGroups.value.map(item => item.id).join(',')
  }
  saveUserGroupSetting(data).then(response => {
    if (response.code === 200) {
      modal.msgSuccess('保存成功')
      visible.value = false
      emit('success')
    } else {
      modal.msgWarning(response.msg || '保存失败')
    }
  })
}

defineExpose({
  openDialog,
  openDialogForAdd
})
</script>
