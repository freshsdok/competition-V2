<template>
  <div class="app-container">
    <el-row :gutter="20" class="full-height">
      <!-- 左侧：栏目列表 -->
      <el-col :span="6" class="column-panel">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>详情栏目</span>
            </div>
          </template>
          <el-tree
            ref="columnTreeRef"
            :data="filteredColumnTree"
            node-key="columnId"
            :props="{ children: 'children', label: 'columnName' }"
            @node-click="handleColumnSelect"
            highlight-current
            default-expand-all
          />
        </el-card>
      </el-col>

      <!-- 右侧：详情编辑 -->
      <el-col :span="18" class="content-panel">
        <el-card class="box-card" v-if="selectedColumn">
          <template #header>
            <div class="card-header">
              <span>{{ selectedColumn.columnName }} - 详情管理</span>
              <template v-if="detailData && !editMode" >
                <el-button type="primary" plain @click="handleEdit" size="small" v-hasPermi="['content:detail:edit']">编辑</el-button>
              </template>
              <template v-else-if="!editMode">
                <el-button type="primary" plain @click="handleAdd" v-hasPermi="['content:detail:add']"  size="small">创建</el-button>
              </template>
            </div>
          </template>

          <!-- 详情展示 -->
          <div v-if="detailData && !editMode" class="detail-view">
            <div class="detail-content" v-html="detailData.detailContent"></div>
          </div>

          <!-- 编辑表单 -->
          <el-form v-if="editMode" ref="formRef" :model="form" :rules="rules" label-width="80px" class="edit-form">
            <el-divider />
            <el-form-item label="详情内容" prop="detailContent">
              <editor v-model="form.detailContent" :min-height="300" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitForm">保 存</el-button>
              <el-button @click="cancelEdit">取 消</el-button>
            </el-form-item>
          </el-form>

          <!-- 无详情提示 -->
          <el-empty v-if="!detailData && !editMode" description="暂无详情内容">
            <el-button type="primary" @click="handleAdd">创建详情</el-button>
          </el-empty>
        </el-card>

        <el-empty v-else description="请先选择栏目" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="DetailPage">
import { getColumnTree } from '@/api/content/column'
import { getDetailByColumnId, addContentDetail, updateContentDetail } from '@/api/content/detail'
import { parseTime } from '@/utils/ruoyi'

const { proxy } = getCurrentInstance()

const columnTree = ref([])
const columnTreeRef = ref()
const selectedColumn = ref(null)
const detailData = ref(null)
const editMode = ref(false)

const data = reactive({
  form: {},
  rules: {
    detailContent: [{ required: true, message: '详情内容不能为空', trigger: 'blur' }]
  }
})

const { form, rules } = toRefs(data)

const formRef = ref()

// 根据栏目类型筛选树（只显示详情页类型的栏目）
const filteredColumnTree = computed(() => {
  const filterTree = (nodes) => {
    return nodes.filter(node => {
      // 只显示类型为3（详情页）的栏目
      if (node.columnType === '3') {
        if (node.children && node.children.length > 0) {
          node.children = filterTree(node.children)
        }
        return true
      }
      return false
    })
  }
  return filterTree(JSON.parse(JSON.stringify(columnTree.value)))
})

/** 获取第一个栏目节点（递归查找） */
function getFirstColumnNode(nodes) {
  if (!nodes || nodes.length === 0) {
    return null
  }
  // 返回第一个节点（可以是父节点）
  return nodes[0]
}

/** 过滤栏目树（只显示详情页类型的栏目） */
function filterColumnTree(nodes) {
  if (!nodes || nodes.length === 0) {
    return []
  }
  return nodes.filter(node => {
    // 只显示类型为3（详情页）的栏目
    if (node.columnType === '3') {
      if (node.children && node.children.length > 0) {
        node.children = filterColumnTree(node.children)
      }
      return true
    }
    return false
  })
}

/** 获取栏目树 */
function getColumnTreeData() {
  getColumnTree({}).then(response => {
    columnTree.value = response.data || []
    // 过滤并获取第一个栏目
    const filtered = filterColumnTree(JSON.parse(JSON.stringify(columnTree.value)))
    if (filtered && filtered.length > 0) {
      const firstColumn = getFirstColumnNode(filtered)
      if (firstColumn) {
        // 延迟执行，确保树已渲染
        nextTick(() => {
          handleColumnSelect(firstColumn)
          // 设置树节点选中状态
          if (columnTreeRef.value) {
            columnTreeRef.value.setCurrentKey(firstColumn.columnId)
          }
        })
      }
    }
  })
}

/** 选择栏目 */
function handleColumnSelect(data) {
  selectedColumn.value = data
  editMode.value = false
  getDetailData()
}

/** 获取详情数据 */
function getDetailData() {
  if (!selectedColumn.value) {
    return
  }
  getDetailByColumnId(selectedColumn.value.columnId).then(response => {
    detailData.value = response.data
  }).catch(() => {
    detailData.value = null
  })
}

/** 创建详情 */
function handleAdd() {
  resetForm()
  form.value.columnId = selectedColumn.value.columnId
  editMode.value = true
}

/** 编辑详情 */
function handleEdit() {
  resetForm()
  form.value = { ...detailData.value }
  console.log(form.value)
  editMode.value = true
}

/** 提交表单 */
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      if (form.value.detailId != null) {
        updateContentDetail(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          editMode.value = false
          getDetailData()
        })
      } else {
        addContentDetail(form.value).then(response => {
          proxy.$modal.msgSuccess('创建成功')
          editMode.value = false
          getDetailData()
        })
      }
    }
  })
}

/** 取消编辑 */
function cancelEdit() {
  editMode.value = false
  resetForm()
}

function resetForm() {
  form.value = {
    detailId: null,
    columnId: selectedColumn.value?.columnId,
    detailContent: null
  }
  formRef.value?.clearValidate()
}

onMounted(() => {
  getColumnTreeData()
})
</script>

<style scoped>
.full-height {
  min-height: calc(100vh - 100px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.column-panel {
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}

.content-panel {
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}

.detail-view {
  padding: 20px 0;
}


.detail-content {
  line-height: 1.8;
  color: #333;
  font-size: 14px;
}

.detail-content :deep(p) {
  margin: 10px 0;
}

.detail-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 10px 0;
  border-radius: 4px;
}

.edit-form {
  margin-top: 20px;
}
</style>
