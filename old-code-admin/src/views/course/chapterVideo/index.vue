<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="章节名称" prop="chapterName">
        <el-input
          v-model="queryParams.chapterName"
          placeholder="请输入章节名称"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="序号" prop="chapterNum">
        <el-input
          v-model="queryParams.chapterNum"
          placeholder="请输入序号"
          clearable
          style="width: 200px;"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属课程" prop="courseId">
        <el-select v-model="queryParams.courseId" placeholder="请选择所属课程" clearable style="width: 200px;">
          <el-option
            v-for="item in courseOptions"
            :key="item.courseId"
            :label="item.name"
            :value="item.courseId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="是否免费" prop="isFree">
        <el-select v-model="queryParams.isFree" placeholder="请选择是否免费" clearable style="width: 200px;">
          <el-option
            v-for="dict in sys_yes_no"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发布状态" prop="publishStatus">
        <el-select v-model="queryParams.publishStatus" placeholder="请选择发布状态" clearable style="width: 200px;">
          <el-option
            v-for="dict in competition_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="学时范围" prop="creditHourRange">
        <el-input
          v-model="queryParams.creditHourMin"
          placeholder="最小"
          clearable
          style="width: 90px;"
        />
        <span style="margin: 0 10px;">-</span>
        <el-input
          v-model="queryParams.creditHourMax"
          placeholder="最大"
          clearable
          style="width: 90px;"
        />
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
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
          v-hasPermi="['course:chapterVideo:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="chapterInfoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="章节名称" align="center" prop="chapterName" min-width="150px" show-overflow-tooltip>
        <template #default="scope">
          <el-link type="primary" @click="handleUpdate(scope.row)" style="white-space: nowrap;">{{ scope.row.chapterName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="序号" align="center" prop="chapterNum" width="80px"/>
      <el-table-column label="所属课程" align="center" prop="courseName" min-width="200px" show-overflow-tooltip/>
      <el-table-column label="描述" align="center" prop="description" min-width="150px" show-overflow-tooltip/>
      <el-table-column label="学时" align="center" prop="creditHour" width="80px"/>
      <el-table-column label="是否免费" align="center" prop="isFree" width="100px">
        <template #default="scope">
          <dict-tag :options="sys_yes_no" :value="scope.row.isFree"/>
        </template>
      </el-table-column>
      <el-table-column label="发布状态" align="center" prop="publishStatus" width="100px">
        <template #default="scope">
          <dict-tag :options="competition_status" :value="scope.row.publishStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160px">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" align="center" prop="publishTime" width="160px">
        <template #default="scope">
          <span>{{ parseTime(scope.row.publishTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200px" fixed="right">
        <template #default="scope">
          <el-button 
            v-if="scope.row.publishStatus !== '4' && scope.row.publishStatus !== '6'" 
            link type="primary" icon="Edit" @click="handleUpdate(scope.row)" 
            v-hasPermi="['course:chapterVideo:edit']">编辑</el-button>
          <el-button 
            v-if="scope.row.publishStatus !== '4' && scope.row.publishStatus !== '6'" 
            link type="danger" icon="Delete" @click="handleDelete(scope.row)" 
            v-hasPermi="['course:chapterVideo:remove']">删除</el-button>
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

    <!-- 添加或修改章节信息对话框 -->
    <el-dialog :title="title" v-model="open" width="1200px" append-to-body>
      <el-form ref="chapterInfoRef" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="章节名称" prop="chapterName">
              <el-input v-model="form.chapterName" placeholder="请输入章节名称" maxlength="200" show-word-limit/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="序号" prop="chapterNum">
              <el-input v-model="form.chapterNum" placeholder="请输入序号" maxlength="50"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属课程" prop="courseId">
              <el-select 
                v-model="form.courseId" 
                placeholder="请选择所属课程" 
                clearable 
                style="width: 100%;"
                @change="() => chapterInfoRef?.validateField('courseId')"
              >
                <el-option
                  v-for="item in courseOptions"
                  :key="item.courseId"
                  :label="item.name"
                  :value="item.courseId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否免费" prop="isFree">
              <el-select v-model="form.isFree" placeholder="请选择是否免费" clearable style="width: 100%;">
                <el-option
                  v-for="dict in sys_yes_no"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="学习时长" prop="creditHour">
              <el-input v-model="form.creditHour" placeholder="请输入学习时长（学时）" maxlength="50"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" maxlength="500" show-word-limit/>
        </el-form-item>
        
        <el-divider content-position="left">视频管理</el-divider>
        <el-form-item label="视频列表">
          <el-button type="primary" icon="Plus" @click="handleAddVideo">增加视频</el-button>
          <el-table :data="form.chapterVideoList" style="width: 100%; margin-top: 10px;" border>
            <el-table-column label="视频序号" prop="videoNum" width="150px">
              <template #default="scope">
                <el-input v-model="scope.row.videoNum" placeholder="请输入序号" maxlength="10" />
              </template>
            </el-table-column>
            <el-table-column label="视频名称" prop="videoName" min-width="200px">
              <template #default="scope">
                <el-input v-model="scope.row.videoName" placeholder="请输入视频名称" maxlength="200" />
              </template>
            </el-table-column>
            <el-table-column label="视频" prop="videoFile" min-width="300px">
              <template #default="scope">
                <el-upload
                  :action="uploadFileUrl"
                  :headers="headers"
                  :on-success="(res) => handleVideoUploadSuccess(res, scope.$index)"
                  :on-error="handleUploadError"
                  :before-upload="beforeVideoUpload"
                  :show-file-list="false"
                >
                  <el-button type="primary" size="small">上传视频</el-button>
                </el-upload>
                <div v-if="scope.row.videoFile" style="margin-top: 5px;">
                  <el-link type="primary" @click="previewVideo(scope.row.videoFile)">预览</el-link>
                  <el-button link type="danger" size="small" @click="scope.row.videoFile = ''">删除</el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100px" align="center">
              <template #default="scope">
                <el-button link type="danger" icon="Delete" @click="handleRemoveVideo(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="章节详情" v-model="detailOpen" width="800px" append-to-body>
      <div v-loading="detailLoading">
        <el-descriptions :column="2" border v-if="detailData">
          <el-descriptions-item label="章节名称">{{ detailData.chapterName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="序号">{{ detailData.chapterNum || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属课程">{{ detailData.courseName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="是否免费">
            <dict-tag :options="sys_yes_no" :value="detailData.isFree"/>
          </el-descriptions-item>
          <el-descriptions-item label="学时">{{ detailData.creditHour || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布状态">
            <dict-tag :options="competition_status" :value="detailData.publishStatus"/>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTime ? parseTime(detailData.createTime, '{y}-{m}-{d} {h}:{i}:{s}') : '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ detailData.publishTime ? parseTime(detailData.publishTime, '{y}-{m}-{d} {h}:{i}:{s}') : '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="视频列表" :span="2" v-if="detailData.chapterVideoList && detailData.chapterVideoList.length > 0">
            <el-table :data="detailData.chapterVideoList" style="width: 100%;" border>
              <el-table-column label="视频名称" prop="videoName" />
              <el-table-column label="视频序号" prop="videoNum" width="100px"/>
              <el-table-column label="视频" prop="videoFile">
                <template #default="scope">
                  <el-link type="primary" @click="previewVideo(scope.row.videoFile)">查看视频</el-link>
                </template>
              </el-table-column>
            </el-table>
          </el-descriptions-item>
          <el-descriptions-item label="视频列表" :span="2" v-else>
            <span style="color: #999;">暂无视频</span>
          </el-descriptions-item>
        </el-descriptions>
        <div v-else style="text-align: center; padding: 40px; color: #999;">
          暂无数据
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup name="ChapterVideo">
import { listChapterInfo, getChapterInfo, addChapterInfo, updateChapterInfo, delChapterInfo, getVideoListByChapterId } from "@/api/course/chapterVideo"
import { listCourseInfo } from "@/api/course/courseInfo"
import { getToken } from "@/utils/auth"
import { useDict } from "@/utils/dict"
import { parseTime } from "@/utils/ruoyi"
import modal from "@/plugins/modal"
import { getCurrentInstance } from 'vue'
import { replaceFileOrigin } from '@/utils/fileOrigin'

const { proxy } = getCurrentInstance()
const { competition_status, sys_yes_no } = useDict('competition_status', 'sys_yes_no')
const queryRef = ref(null)
const chapterInfoRef = ref(null)
const chapterInfoList = ref([])
const courseOptions = ref([])
const open = ref(false)
const detailOpen = ref(false)
const loading = ref(true)
const detailLoading = ref(false)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const detailData = ref(null)

const data = reactive({
  form: {
    chapterId: null,
    chapterName: null,
    chapterNum: null,
    courseId: null,
    description: null,
    creditHour: null,
    isFree: null,
    chapterVideoList: []
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    chapterName: null,
    chapterNum: null,
    courseId: null,
    isFree: null,
    publishStatus: null,
    creditHourMin: null,
    creditHourMax: null,
  },
  rules: {
    chapterName: [
      { required: true, message: "章节名称不能为空", trigger: "blur" }
    ],
    chapterNum: [
      { required: true, message: "序号不能为空", trigger: "blur" }
    ],
    courseId: [
      { required: true, message: "所属课程不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询课程列表 */
function getCourseList() {
  listCourseInfo({ pageNum: 1, pageSize: 1000 }).then(response => {
    courseOptions.value = response.rows || []
  }).catch(() => {
    courseOptions.value = []
  })
}

/** 查询章节信息列表 */
function getList() {
  loading.value = true
  // 由于后端没有直接的章节列表接口，需要通过课程接口查询然后展开章节
  // 这里先查询所有课程，然后展开章节列表
  const params = proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime')
  listChapterInfo(params).then(response => {
    // 展开所有课程的章节列表
    const allChapters = []
    if (response.rows && response.rows.length > 0) {
      response.rows.forEach(course => {
        if (course.courseChapterInfoList && course.courseChapterInfoList.length > 0) {
          course.courseChapterInfoList.forEach(chapter => {
            // 添加课程名称
            chapter.courseName = course.name
            chapter.publishStatus = course.publishStatus
            chapter.publishTime = course.publishTime
            // 过滤条件
            if (queryParams.value.chapterName && !chapter.chapterName.includes(queryParams.value.chapterName)) {
              return
            }
            if (queryParams.value.chapterNum && chapter.chapterNum !== queryParams.value.chapterNum) {
              return
            }
            if (queryParams.value.courseId && chapter.courseId !== queryParams.value.courseId) {
              return
            }
            if (queryParams.value.isFree && chapter.isFree !== queryParams.value.isFree) {
              return
            }
            if (queryParams.value.publishStatus && course.publishStatus !== queryParams.value.publishStatus) {
              return
            }
            allChapters.push(chapter)
          })
        }
      })
    }
    chapterInfoList.value = allChapters
    total.value = allChapters.length
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
    chapterId: null,
    chapterName: null,
    chapterNum: null,
    courseId: null,
    description: null,
    creditHour: null,
    isFree: null,
    chapterVideoList: []
  }
  if (chapterInfoRef.value) {
    chapterInfoRef.value.resetFields()
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.chapterId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  // 重新加载课程列表，确保获取最新的课程数据
  getCourseList()
  open.value = true
  title.value = "添加章节信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  // 重新加载课程列表，确保获取最新的课程数据
  getCourseList()
  // 通过课程ID查询详情（包含章节信息）
  getChapterInfo(row.courseId).then(response => {
    const course = response.data
    if (course && course.courseChapterInfoList) {
      const chapter = course.courseChapterInfoList.find(c => c.chapterId === row.chapterId)
      if (chapter) {
        form.value = {
          chapterId: chapter.chapterId,
          chapterName: chapter.chapterName,
          chapterNum: chapter.chapterNum,
          courseId: chapter.courseId,
          description: chapter.description,
          creditHour: chapter.creditHour,
          isFree: chapter.isFree || null,
          chapterVideoList: chapter.chapterVideoList || []
        }
      }
    }
    open.value = true
    title.value = "修改章节信息"
  })
}

/** 提交按钮 */
function submitForm() {
  chapterInfoRef.value.validate(valid => {
    if (!valid) {
      console.log('表单验证失败', valid)
      return false
    }
    
    // 检查 courseId，注意：courseId 可能是 0（数字类型），所以不能用 !form.value.courseId
    if (form.value.courseId === null || form.value.courseId === undefined || form.value.courseId === '') {
      console.log('courseId 为空:', form.value.courseId)
      modal.msgError("请选择所属课程")
      return false
    }
    
    console.log('提交表单，courseId:', form.value.courseId, 'form:', form.value)
    
    // 获取课程信息，更新章节列表
    getChapterInfo(form.value.courseId).then(courseResponse => {
      const course = courseResponse.data
      if (!course) {
        modal.msgError("课程不存在")
        return
      }
      
      // 准备章节数据
      const chapterData = {
        chapterId: form.value.chapterId,
        chapterName: form.value.chapterName,
        chapterNum: form.value.chapterNum,
        courseId: form.value.courseId,
        description: form.value.description,
        creditHour: form.value.creditHour,
        isFree: form.value.isFree,
        chapterVideoList: (form.value.chapterVideoList || []).filter(v => v.videoName && v.videoFile).map((video, index) => ({
          videoId: video.videoId || null,
          videoName: video.videoName,
          videoNum: video.videoNum || String(index + 1),
          videoFile: video.videoFile,
          chapterId: form.value.chapterId || null
        }))
      }
      
      // 更新课程的章节列表
      if (!course.courseChapterInfoList) {
        course.courseChapterInfoList = []
      }
      
      if (form.value.chapterId) {
        // 修改章节
        const index = course.courseChapterInfoList.findIndex(c => c.chapterId === form.value.chapterId)
        if (index >= 0) {
          // 保留原有的其他属性
          Object.assign(course.courseChapterInfoList[index], chapterData)
        } else {
          modal.msgError("章节不存在")
          return
        }
      } else {
        // 新增章节
        course.courseChapterInfoList.push(chapterData)
      }
      
      // 更新课程信息（包含章节列表）
      updateChapterInfo(course).then(() => {
        modal.msgSuccess(form.value.chapterId ? "修改成功" : "新增成功")
        open.value = false
        getList()
      }).catch((error) => {
        console.error('保存失败:', error)
        modal.msgError(form.value.chapterId ? "修改失败" : "新增失败")
      })
    }).catch((error) => {
      console.error('获取课程信息失败:', error)
      modal.msgError("获取课程信息失败，请重试")
    })
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  // 验证必要字段
  if (!row.chapterId) {
    modal.msgError("章节ID不存在，无法删除")
    return
  }
  
  modal.confirm('是否确认删除章节"' + (row.chapterName || row.chapterId) + '"？').then(function() {
    // 直接根据章节ID删除单条章节
    return delChapterInfo(row.chapterId)
  }).then(() => {
    getList()
    modal.msgSuccess("删除成功")
  }).catch((error) => {
    console.error('删除失败:', error)
    
    // 尝试提取更详细的错误信息
    let errorMsg = '未知错误'
    if (error?.response?.data?.msg) {
      errorMsg = error.response.data.msg
    } else if (error?.msg) {
      errorMsg = error.msg
    } else if (error?.message) {
      errorMsg = error.message
    } else if (typeof error === 'string') {
      errorMsg = error
    }
    
    modal.msgError("删除失败：" + errorMsg)
  })
}

/** 详情 */
function handleDetail(row) {
  // 先重置数据
  detailData.value = null
  detailLoading.value = true
  detailOpen.value = true
  console.log('点击的章节行数据:', row)
  console.log('要查找的章节ID:', row.chapterId, '类型:', typeof row.chapterId)
  getChapterInfo(row.courseId).then(response => {
    console.log('获取章节详情响应:', response)
    const course = response.data
    console.log('课程数据:', course)
    // 尝试多种可能的字段名
    const chapterList = course?.courseChapterInfoList || course?.chapterList || course?.chapters || []
    console.log('课程章节列表:', chapterList)
    console.log('章节列表长度:', chapterList.length)
    
    if (course && chapterList && chapterList.length > 0) {
      // 打印所有章节的ID用于调试
      console.log('所有章节ID:', chapterList.map(c => ({ id: c.chapterId, type: typeof c.chapterId, name: c.chapterName })))
      // 尝试多种匹配方式（处理类型不匹配问题）
      const chapter = chapterList.find(c => {
        // 使用宽松比较处理类型不匹配
        const match = c.chapterId == row.chapterId || 
               String(c.chapterId) === String(row.chapterId) ||
               Number(c.chapterId) === Number(row.chapterId)
        if (match) {
          console.log('匹配成功:', { 
            courseChapterId: c.chapterId, 
            rowChapterId: row.chapterId,
            courseType: typeof c.chapterId,
            rowType: typeof row.chapterId
          })
        }
        return match
      })
      console.log('找到的章节:', chapter)
      if (chapter) {
        // 准备详情数据，确保包含所有必要字段
        detailData.value = {
          ...chapter,
          courseName: course.name || row.courseName,
          chapterVideoList: chapter.chapterVideoList || chapter.videoList || []
        }
        console.log('设置的详情数据:', detailData.value)
      } else {
        console.error('未找到匹配的章节')
        console.error('查找的ID:', row.chapterId, '类型:', typeof row.chapterId)
        console.error('可用的章节ID:', chapterList.map(c => ({ id: c.chapterId, type: typeof c.chapterId })))
        modal.msgError("未找到章节信息，请检查数据")
        detailData.value = null
      }
    } else {
      console.error('课程数据或章节列表不存在或为空')
      console.error('课程对象:', course)
      console.error('章节列表:', chapterList)
      // 如果章节列表为空，尝试直接使用行数据
      if (row && row.chapterId) {
        console.log('尝试使用行数据直接显示')
        detailData.value = {
          ...row,
          chapterVideoList: row.chapterVideoList || []
        }
      } else {
        modal.msgError("未找到课程信息或章节列表为空")
        detailData.value = null
      }
    }
  }).catch(error => {
    console.error('获取章节详情失败:', error)
    modal.msgError("获取章节详情失败，请重试")
    detailData.value = null
  }).finally(() => {
    detailLoading.value = false
  })
}

/** 增加视频 */
function handleAddVideo() {
  if (!form.value.chapterVideoList) {
    form.value.chapterVideoList = []
  }
  const nextNum = form.value.chapterVideoList.length > 0 
    ? Math.max(...form.value.chapterVideoList.map(v => parseInt(v.videoNum) || 0)) + 1
    : 1
  form.value.chapterVideoList.push({
    videoId: null,
    videoName: '',
    videoNum: String(nextNum),
    videoFile: '',
    chapterId: form.value.chapterId
  })
}

/** 删除视频 */
function handleRemoveVideo(index) {
  form.value.chapterVideoList.splice(index, 1)
  // 重新排序
  form.value.chapterVideoList.forEach((video, idx) => {
    if (!video.videoNum) {
      video.videoNum = String(idx + 1)
    }
  })
}

/** 视频上传成功 */
function handleVideoUploadSuccess(res, index) {
  res = replaceFileOrigin(res)
  if (res.code === 200 && res.data && res.data.url) {
    form.value.chapterVideoList[index].videoFile = res.data.url
    modal.msgSuccess("视频上传成功")
  } else {
    modal.msgError("视频上传失败")
  }
}

/** 视频上传前验证 */
function beforeVideoUpload(file) {
  const isVideo = file.type.startsWith('video/')
  const isLt500M = file.size / 1024 / 1024 < 500

  if (!isVideo) {
    modal.msgError('只能上传视频文件!')
    return false
  }
  if (!isLt500M) {
    modal.msgError('视频大小不能超过 500MB!')
    return false
  }
  return true
}

/** 预览视频 */
function previewVideo(url) {
  if (url) {
    window.open(url, '_blank')
  }
}

getList()
getCourseList()

// 文件上传
const uploadFileUrl = ref(import.meta.env.VITE_APP_BASE_API + "/file/upload")
const headers = ref({ Authorization: "Bearer " + getToken() })

function handleUploadError(err) {
  modal.msgError('上传文件失败')
}
</script>

<style scoped>
:deep(.el-form-item__label) {
  white-space: nowrap;
}
</style>
