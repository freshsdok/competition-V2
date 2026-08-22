<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="120px">
      <el-form-item label="推荐位名称" prop="remdName">
        <el-input
          v-model="queryParams.remdName"
          placeholder="请输入推荐位名称"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型" clearable style="width: 200px;">
          <el-option label="精品" value="精品" />
          <el-option label="热门" value="热门" />
        </el-select>
      </el-form-item>
      <el-form-item label="权重范围" prop="weightRange">
        <el-select v-model="queryParams.weightRange" placeholder="请选择权重范围" clearable style="width: 200px;">
          <el-option label="1-3" value="1-3" />
          <el-option label="4-6" value="4-6" />
          <el-option label="7-10" value="7-10" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标用户群体" prop="targetUserGroup">
        <el-input
          v-model="queryParams.targetUserGroup"
          placeholder="请输入目标用户群体"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="推荐状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择推荐状态" clearable style="width: 200px;">
          <el-option label="未推荐" :value="0" />
          <el-option label="已推荐" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="推荐课程数量范围" prop="courseCountRange">
        <el-input
          v-model="queryParams.courseCountMin"
          placeholder="0"
          clearable
          style="width: 90px;"
        />
        <span style="margin: 0 10px;">~</span>
        <el-input
          v-model="queryParams.courseCountMax"
          placeholder="10"
          clearable
          style="width: 90px;"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="createTimeStart">
        <el-date-picker
          v-model="queryParams.createTimeStart"
          style="width: 200px"
          value-format="YYYY-MM-DD"
          type="date"
          placeholder="选择开始日期"
        ></el-date-picker>
        <span style="margin: 0 10px;">至</span>
        <el-date-picker
          v-model="queryParams.createTimeEnd"
          style="width: 200px"
          value-format="YYYY-MM-DD"
          type="date"
          placeholder="选择结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="展示时间" prop="displayStartTime">
        <el-date-picker
          v-model="queryParams.displayStartTime"
          style="width: 200px"
          value-format="YYYY-MM-DD"
          type="date"
          placeholder="选择开始日期"
        ></el-date-picker>
        <span style="margin: 0 10px;">至</span>
        <el-date-picker
          v-model="queryParams.displayEndTime"
          style="width: 200px"
          value-format="YYYY-MM-DD"
          type="date"
          placeholder="选择结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['course:recommendInfo:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recommendInfoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="推荐位名称" align="center" prop="remdName" min-width="150px" show-overflow-tooltip />
      <el-table-column label="类型" align="center" prop="type" width="100px" />
      <el-table-column label="权重" align="center" prop="weight" width="80px" />
      <el-table-column label="展示时间范围" align="center" prop="displayTimeRange" width="200px">
        <template #default="scope">
          <span v-if="scope.row.displayStartTime && scope.row.displayEndTime">
            {{ parseTime(scope.row.displayStartTime, '{y}-{m}-{d}') }}~{{ parseTime(scope.row.displayEndTime, '{y}-{m}-{d}') }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="目标用户群体" align="center" prop="targetUserGroup" min-width="150px" show-overflow-tooltip />
      <el-table-column label="推荐状态" align="center" prop="status" width="80px">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="(value) => handleRecommendStatusChange(scope.row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160px">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="240px" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['course:recommendInfo:edit']">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['course:recommendInfo:remove']">删除</el-button>
          <el-button link type="info" icon="View" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改课程推荐信息对话框 -->
    <el-dialog :title="title" v-model="open" width="900px" append-to-body>
      <el-form ref="recommendInfoRef" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="推荐位名称" prop="remdName">
              <el-input v-model="form.remdName" placeholder="请输入推荐位名称" maxlength="200" show-word-limit/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权重" prop="weight">
              <el-input-number v-model="form.weight" :min="1" :max="10" placeholder="请输入权重" style="width: 100%;"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="目标用户群体" prop="targetUserGroup">
              <el-input v-model="form.targetUserGroup" placeholder="请输入目标用户群体" maxlength="200"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择类型" clearable style="width: 100%;">
                <el-option label="精品" value="精品" />
                <el-option label="热门" value="热门" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="推荐状态" prop="status">
              <el-switch
                v-model="form.status"
                :active-value="1"
                :inactive-value="0"
                active-text="已推荐"
                inactive-text="未推荐"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="展示时间" prop="displayStartTime">
              <el-date-picker
                v-model="form.displayStartTime"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                type="date"
                placeholder="选择开始日期"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="展示时间止" prop="displayEndTime">
              <el-date-picker
                v-model="form.displayEndTime"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                type="date"
                placeholder="选择结束日期"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" icon="Plus" @click="handleAddCourse">增加推荐课程</el-button>
        </el-form-item>
        <el-form-item label="推荐课程列表">
          <el-table 
            :data="form.recommendCourseList || []" 
            border 
            style="width: 100%;"
            empty-text="暂无推荐课程，请点击'增加推荐课程'按钮添加">
            <el-table-column label="课程名称" prop="courseName" min-width="200px" show-overflow-tooltip>
              <template #default="scope">
                {{ scope.row.courseName || '未知课程' }}
              </template>
            </el-table-column>
            <el-table-column label="授课教师" prop="teacher" width="150px">
              <template #default="scope">
                {{ scope.row.teacher || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="排序" prop="sort" width="120px" align="center">
              <template #default="scope">
                <el-input-number 
                  v-model="scope.row.sort" 
                  :min="1" 
                  placeholder="排序" 
                  style="width: 100%;"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100px" align="center">
              <template #default="scope">
                <el-button link type="danger" icon="Delete" @click="handleRemoveCourse(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
          <el-button type="primary" @click="submitForm">保 存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 选择课程对话框 -->
    <el-dialog title="选择课程" v-model="courseSelectOpen" width="1000px" append-to-body>
      <el-form :model="courseQueryParams" ref="courseQueryRef" :inline="true" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input
            v-model="courseQueryParams.name"
            placeholder="请输入课程名称"
            clearable
            style="width: 200px;"
            @keyup.enter="handleCourseQuery"
          />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacher">
          <el-input
            v-model="courseQueryParams.teacher"
            placeholder="请输入授课教师"
            clearable
            style="width: 200px;"
            @keyup.enter="handleCourseQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleCourseQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetCourseQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="courseLoading" :data="courseList" @selection-change="handleCourseSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="课程名称" align="center" prop="name" min-width="200px" show-overflow-tooltip />
        <el-table-column label="授课教师" align="center" prop="teacher" width="150px"/>
        <el-table-column label="分类" align="center" prop="classifyName" width="120px"/>
        <el-table-column label="学时" align="center" prop="creditHour" width="80px"/>
      </el-table>
      <pagination
        v-show="courseTotal>0"
        :total="courseTotal"
        v-model:page="courseQueryParams.pageNum"
        v-model:limit="courseQueryParams.pageSize"
        @pagination="getCourseList"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="courseSelectOpen = false">取 消</el-button>
          <el-button type="primary" @click="handleConfirmCourseSelect">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="课程推荐详情" v-model="detailOpen" width="800px" append-to-body>
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="推荐位名称">{{ detailData.remdName }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detailData.type }}</el-descriptions-item>
        <el-descriptions-item label="权重">{{ detailData.weight }}</el-descriptions-item>
        <el-descriptions-item label="展示时间范围">
          <span v-if="detailData.displayStartTime && detailData.displayEndTime">
            {{ parseTime(detailData.displayStartTime, '{y}-{m}-{d}') }}~{{ parseTime(detailData.displayEndTime, '{y}-{m}-{d}') }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="目标用户群体">{{ detailData.targetUserGroup }}</el-descriptions-item>
        <el-descriptions-item label="推荐状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'info'">
            {{ detailData.status === 1 ? '已推荐' : '未推荐' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(detailData.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ parseTime(detailData.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup name="RecommendInfo">
import { listRecommendInfo, getRecommendInfo, delRecommendInfo, addRecommendInfo, updateRecommendInfo } from "@/api/course/recommendInfo"
import { listCourseInfo, getCourseInfo } from "@/api/course/courseInfo"
import { useDict } from "@/utils/dict"
import { parseTime } from "@/utils/ruoyi"
import modal from "@/plugins/modal"
import { getCurrentInstance } from 'vue'

const { proxy } = getCurrentInstance()
const { competition_status } = useDict('competition_status')
const queryRef = ref(null)
const recommendInfoRef = ref(null)
const courseQueryRef = ref(null)
const recommendInfoList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const courseSelectOpen = ref(false)
const loading = ref(true)
const courseLoading = ref(false)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const courseTotal = ref(0)
const title = ref("")
const detailData = ref(null)
const courseList = ref([])
const selectedCourses = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    remdName: null,
    code: null,
    type: null,
    status: null,
    weightRange: null,
    targetUserGroup: null,
    courseCountMin: null,
    courseCountMax: null,
    createTimeStart: null,
    createTimeEnd: null,
    displayStartTime: null,
    displayEndTime: null,
  },
  courseQueryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    teacher: null,
  },
  rules: {
    remdName: [
      { required: true, message: "推荐位名称不能为空", trigger: "blur" }
    ],
    type: [
      { required: true, message: "类型不能为空", trigger: "change" }
    ],
    weight: [
      { required: true, message: "权重不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules, courseQueryParams } = toRefs(data)

/** 查询课程推荐信息列表 */
function getList() {
  loading.value = true
  listRecommendInfo(queryParams.value).then(response => {
    recommendInfoList.value = (response.rows || []).map(item => {
      // 确保 status 字段是数字类型
      if (item.status !== null && item.status !== undefined) {
        item.status = Number(item.status) === 1 ? 1 : 0
      } else {
        item.status = 0
      }
      return item
    })
    total.value = response.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    remdId: null,
    remdName: null,
    code: null,
    type: null,
    weight: null,
    displayStartTime: null,
    displayEndTime: null,
    targetUserGroup: null,
    status: 0, // 默认为未推荐
    recommendCourseList: [] // 确保初始化为空数组
  }
  // 确保 recommendCourseList 是响应式数组
  if (!form.value.recommendCourseList) {
    form.value.recommendCourseList = []
  }
  if (recommendInfoRef.value) {
    recommendInfoRef.value.resetFields()
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.remdId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  // 确保 recommendCourseList 已初始化
  if (!form.value.recommendCourseList) {
    form.value.recommendCourseList = []
  }
  open.value = true
  title.value = "添加课程推荐信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _remdId = row.remdId || ids.value
  getRecommendInfo(_remdId).then(response => {
    form.value = response.data
    
    // 如果后端返回的日期是时间戳或日期对象，需要转换为 YYYY-MM-DD 格式
    if (form.value.displayStartTime) {
      form.value.displayStartTime = parseTime(form.value.displayStartTime, '{y}-{m}-{d}')
    }
    if (form.value.displayEndTime) {
      form.value.displayEndTime = parseTime(form.value.displayEndTime, '{y}-{m}-{d}')
    }
    
    // 确保推荐状态有默认值，并转换为数字类型
    if (form.value.status === null || form.value.status === undefined) {
      form.value.status = 0
    } else {
      // 确保 status 是数字类型（0 或 1）
      form.value.status = Number(form.value.status) === 1 ? 1 : 0
    }
    
    // 初始化 recommendCourseList
    if (!form.value.recommendCourseList) {
      form.value.recommendCourseList = []
    }
    
    // 将后端的 courseRecommendRelaList 转换为前端的 recommendCourseList
    if (form.value.courseRecommendRelaList && form.value.courseRecommendRelaList.length > 0) {
      // 批量查询课程详情
      const courseIdList = form.value.courseRecommendRelaList.map(item => item.courseId).filter(id => id != null)
      
      if (courseIdList.length > 0) {
        const coursePromises = courseIdList.map(courseId => getCourseInfo(courseId).catch(err => {
          console.error('获取课程详情失败:', courseId, err)
          return { data: null }
        }))
        
        Promise.all(coursePromises).then(courseResponses => {
          form.value.recommendCourseList = form.value.courseRecommendRelaList.map((rela, index) => {
            const courseInfo = courseResponses[index]?.data
            return {
              id: rela.id, // 保留关联关系ID，用于更新时识别
              courseId: rela.courseId,
              courseName: courseInfo?.name || '',
              teacher: courseInfo?.teacher || '',
              sort: rela.sort || 1 // 默认为1
            }
          })
          open.value = true
          title.value = "修改课程推荐信息"
        }).catch(err => {
          console.error('批量查询课程详情失败:', err)
          // 如果查询课程详情失败，至少保留 courseId
          form.value.recommendCourseList = form.value.courseRecommendRelaList.map((rela, index) => {
            return {
              id: rela.id,
              courseId: rela.courseId,
              courseName: '',
              teacher: '',
              sort: rela.sort || 1 // 默认为1
            }
          })
          open.value = true
          title.value = "修改课程推荐信息"
        })
      } else {
        form.value.recommendCourseList = []
        open.value = true
        title.value = "修改课程推荐信息"
      }
    } else {
      form.value.recommendCourseList = []
      open.value = true
      title.value = "修改课程推荐信息"
    }
  }).catch(err => {
    console.error('获取推荐信息失败:', err)
    modal.msgError('获取推荐信息失败')
  })
}

/** 提交按钮 */
function submitForm() {
  recommendInfoRef.value.validate(valid => {
    if (valid) {
      // 准备提交数据，确保字段名与后端一致
      const submitData = {
        ...form.value
      }
      // 将日期格式从 YYYY-MM-DD 转换为 YYYY-MM-DD HH:mm:ss
      if (submitData.displayStartTime) {
        submitData.displayStartTime = submitData.displayStartTime + ' 00:00:00'
      }
      if (submitData.displayEndTime) {
        submitData.displayEndTime = submitData.displayEndTime + ' 23:59:59'
      }
      
      // 确保 status 字段是数字类型（0 或 1）
      if (submitData.status !== null && submitData.status !== undefined) {
        submitData.status = Number(submitData.status) === 1 ? 1 : 0
      } else {
        submitData.status = 0
      }
      
      // 确保 recommendCourseList 存在
      if (!submitData.recommendCourseList) {
        submitData.recommendCourseList = []
      }
      
      // 将 recommendCourseList 转换为 courseRecommendRelaList
      if (submitData.recommendCourseList && submitData.recommendCourseList.length > 0) {
        submitData.courseRecommendRelaList = submitData.recommendCourseList
          .filter(item => item.courseId != null) // 过滤掉无效的课程
          .map((item, index) => {
            return {
              id: item.id || null, // 如果是编辑已有记录，保留 id
              courseId: item.courseId,
              sort: (item.sort && item.sort >= 1) ? item.sort : 1 // 默认为1
            }
          })
      } else {
        submitData.courseRecommendRelaList = []
      }
      
      // 删除前端使用的 recommendCourseList，只保留后端需要的 courseRecommendRelaList
      delete submitData.recommendCourseList
      
      if (form.value.remdId != null) {
        updateRecommendInfo(submitData).then(() => {
          modal.msgSuccess("修改成功")
          open.value = false
          getList()
        }).catch(err => {
          console.error('更新失败:', err)
          modal.msgError('更新失败：' + (err.msg || err.message || '未知错误'))
        })
      } else {
        addRecommendInfo(submitData).then(() => {
          modal.msgSuccess("新增成功")
          open.value = false
          getList()
        }).catch(err => {
          console.error('新增失败:', err)
          modal.msgError('新增失败：' + (err.msg || err.message || '未知错误'))
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _remdIds = row.remdId || ids.value
  modal.confirm('是否确认删除课程推荐编号为"' + _remdIds + '"的数据项？').then(function() {
    return delRecommendInfo(_remdIds)
  }).then(() => {
    getList()
    modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 详情 */
function handleDetail(row) {
  getRecommendInfo(row.remdId).then(response => {
    detailData.value = response.data
    detailOpen.value = true
  })
}

/** 增加推荐课程 */
function handleAddCourse() {
  courseSelectOpen.value = true
  getCourseList()
}

/** 查询课程列表 */
function getCourseList() {
  courseLoading.value = true
  listCourseInfo(courseQueryParams.value).then(response => {
    courseList.value = response.rows || []
    courseTotal.value = response.total || 0
    courseLoading.value = false
  }).catch(() => {
    courseLoading.value = false
  })
}

/** 课程查询 */
function handleCourseQuery() {
  courseQueryParams.value.pageNum = 1
  getCourseList()
}

/** 重置课程查询 */
function resetCourseQuery() {
  if (courseQueryRef.value) {
    courseQueryRef.value.resetFields()
  }
  handleCourseQuery()
}

/** 课程选择变化 */
function handleCourseSelectionChange(selection) {
  selectedCourses.value = selection
}

/** 确认选择课程 */
function handleConfirmCourseSelect() {
  if (selectedCourses.value.length === 0) {
    modal.msgWarning("请至少选择一个课程")
    return
  }
  // 确保 recommendCourseList 存在
  if (!form.value.recommendCourseList) {
    form.value.recommendCourseList = []
  }
  // 添加选中的课程，避免重复
  selectedCourses.value.forEach(course => {
    // 检查是否已存在（通过 courseId 判断）
    const exists = form.value.recommendCourseList.some(item => item.courseId === course.courseId)
    if (!exists) {
      form.value.recommendCourseList.push({
        id: null, // 新增的课程没有 id
        courseId: course.courseId,
        courseName: course.name || '',
        teacher: course.teacher || '',
        sort: form.value.recommendCourseList.length > 0 
          ? Math.max(...form.value.recommendCourseList.map(item => item.sort || 0)) + 1 
          : 1 // 默认为1，如果有其他课程则取最大值+1
      })
    } else {
      modal.msgWarning(`课程"${course.name}"已存在，请勿重复添加`)
    }
  })
  courseSelectOpen.value = false
  selectedCourses.value = []
  // 清空课程选择表格的选中状态
  if (proxy.$refs.courseSelectTable) {
    proxy.$refs.courseSelectTable.clearSelection()
  }
}

/** 移除推荐课程 */
function handleRemoveCourse(index) {
  if (form.value.recommendCourseList && form.value.recommendCourseList.length > index) {
    form.value.recommendCourseList.splice(index, 1)
    // 重新设置排序（保持原有sort值，如果不存在则设置为1）
    form.value.recommendCourseList.forEach((item, idx) => {
      if (!item.sort || item.sort < 1) {
        item.sort = 1
      }
    })
  }
}

/** 处理推荐状态变化 */
function handleRecommendStatusChange(row, newStatus) {
  // 保存原始状态值（新值的相反值）
  const originalStatus = newStatus === 1 ? 0 : 1
  // 确保新值是数字类型
  const statusValue = newStatus === 1 ? 1 : 0
  
  const submitData = {
    remdId: row.remdId,
    status: statusValue
  }
  updateRecommendInfo(submitData).then(() => {
    // 更新成功，确保状态值正确
    row.status = statusValue
    getList()
  }).catch(err => {
    console.error('更新推荐状态失败:', err)
    // 恢复原始值
    row.status = originalStatus
    modal.msgError('更新推荐状态失败：' + (err.msg || err.message || '未知错误'))
    // 刷新列表以恢复正确状态
    getList()
  })
}

getList()
</script>

<style scoped>
:deep(.el-form-item__label) {
  white-space: nowrap;
}
</style>

