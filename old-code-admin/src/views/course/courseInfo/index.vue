<template>
  <div class="app-container">
    <el-row :gutter="20" class="page-layout">
      <!-- 左侧树形图 -->
      <el-col :span="4" class="tree-container">
        <div class="tree-header">
          <span>课程分类</span>
        </div>
        <!-- 搜索框 -->
        <div class="tree-search">
          <el-input
            v-model="classifySearchText"
            placeholder="搜索分类"
            clearable
            prefix-icon="Search"
            @input="handleClassifySearch"
            @clear="handleClassifySearchClear"
          />
        </div>
        <el-tree
          ref="treeRef"
          :data="filteredClassifyTreeData"
          :props="{ children: 'children', label: 'classifyName' }"
          node-key="classifyId"
          :default-expand-all="false"
          :expanded-keys="expandedKeys"
          highlight-current
          :expand-on-click-node="false"
          @node-click="handleTreeClick"
          @expand="handleTreeExpand"
          @collapse="handleTreeCollapse"
          class="classify-tree"
          :indent="30"
        >
          <template #default="{ node, data }">
            <span class="tree-node">
              <span class="tree-node-label">{{ node.label }}</span>
            </span>
          </template>
        </el-tree>
      </el-col>

      <!-- 右侧内容区 -->
      <el-col :span="20" class="content-container">
        <!-- 搜索条件区域 -->
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px" class="search-form">
          <el-form-item label="课程名称" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入课程名称"
              clearable
              style="width: 200px;"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="编码" prop="courseCode">
            <el-input
              v-model="queryParams.courseCode"
              placeholder="请输入编码"
              clearable
              style="width: 200px;"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="难度" prop="difficultyLevel">
            <el-select v-model="queryParams.difficultyLevel" placeholder="请选择难度" clearable style="width: 200px;">
              <el-option
                v-for="dict in course_difficulty"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="学时" prop="creditHour">
            <el-input
              v-model="queryParams.creditHour"
              placeholder="请输入学时"
              clearable
              style="width: 200px;"
              @input="(val) => handleQueryIntegerInput('creditHour', val)"
            />
          </el-form-item>
          <el-form-item label="授课教师" prop="teacher">
            <el-input
              v-model="queryParams.teacher"
              placeholder="请输入授课教师"
              clearable
              style="width: 200px;"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="发布状态" prop="publishStatus">
            <el-select v-model="queryParams.publishStatus" placeholder="请选择发布状态" clearable style="width: 200px;">
              <el-option
                v-for="dict in publish_status"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="审核状态" prop="checkStatus">
            <el-select v-model="queryParams.checkStatus" placeholder="请选择审核状态" clearable style="width: 200px;">
              <el-option
                v-for="dict in check_status"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 操作按钮区域 -->
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['course:courseInfo:add']"
            >新增</el-button>
          </el-col>
          
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <!-- 列表区域 -->
        <el-table v-loading="loading" :data="courseInfoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="课程名称" align="center" prop="name" min-width="200px" show-overflow-tooltip>
        <template #default="scope">
          <el-link type="primary" @click="handleUpdate(scope.row)" style="white-space: nowrap;">{{ scope.row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="课程编号" align="center" prop="courseCode" width="120px"/>
      <el-table-column label="分类" align="center" prop="classifyName" width="120px"/>
      <el-table-column label="授课教师" align="center" prop="teacher" width="120px"/>
      <el-table-column label="简介" align="center" prop="details" min-width="200px" show-overflow-tooltip>
        <template #default="scope">
          <div v-html="scope.row.details" style="max-height: 50px; overflow: hidden; text-overflow: ellipsis;"></div>
        </template>
      </el-table-column>
      <el-table-column label="学时" align="center" prop="creditHour" width="80px"/>
      <el-table-column label="学分" align="center" prop="studyScore" width="80px"/>
      <el-table-column label="难度" align="center" prop="difficultyLevel" width="100px">
        <template #default="scope">
          <dict-tag :options="course_difficulty" :value="scope.row.difficultyLevel"/>
        </template>
      </el-table-column>
      <el-table-column label="发布状态" align="center" prop="publishStatus" width="100px">
        <template #default="scope">
          <dict-tag :options="publish_status" :value="scope.row.publishStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" prop="checkStatus" width="100px">
        <template #default="scope">
          <dict-tag :options="check_status" :value="scope.row.checkStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="审核意见" align="center" prop="applyReason" min-width="160px" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.applyReason || '-' }}</span>
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
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="350px" fixed="right">
        <template #default="scope">
          <el-button 
            v-if="!(scope.row.checkStatus === '4' || scope.row.publishStatus === '2' || scope.row.checkStatus === '3')"
            link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['course:courseInfo:edit']">课程编辑</el-button>
          <el-button 
            v-if="scope.row.checkStatus === '4'"
            link type="primary" icon="Edit" @click="handleChapterEdit(scope.row)" v-hasPermi="['course:courseInfo:edit']">视频编辑</el-button>
          <el-button 
            v-if="(scope.row.publishStatus === '1' || scope.row.checkStatus === '5') && scope.row.checkStatus !== '4' && scope.row.checkStatus !== '3'" 
            link type="success" icon="Promotion" @click="handleSubmitAudit(scope.row)" 
            v-hasPermi="['course:task:submit']">提交审核</el-button>
          <el-button 
            v-if="scope.row.checkStatus === '4' && scope.row.publishStatus !== '2'" 
            link type="success" icon="VideoPlay" @click="handlePublish(scope.row)" 
            v-hasPermi="['course:courseInfo:publish']">发布</el-button>
          <el-button 
            v-if="scope.row.publishStatus === '2'" 
            link type="warning" icon="VideoPause" @click="handleOffline(scope.row)" 
            v-hasPermi="['course:courseInfo:offline']">下架</el-button>
          <el-button link type="info" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button 
            v-if="!(scope.row.checkStatus === '4' || scope.row.publishStatus === '2' || scope.row.checkStatus === '3')"
            link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['course:courseInfo:remove']">删除</el-button>
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
      </el-col>
    </el-row>

    <!-- 添加或修改课程信息对话框 -->
    <el-dialog :title="title" v-model="open" width="1200px" append-to-body>
      <el-form ref="courseInfoRef" :model="form" :rules="rules" label-width="120px">
        <!-- 第一行：课程名称（左侧）、分类（右侧） -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="课程名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入课程名称" maxlength="200" show-word-limit/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="classifyId">
              <el-popover
                v-model:visible="classifySelectVisible"
                placement="bottom-start"
                :width="300"
                trigger="click"
                popper-class="classify-tree-select-popover"
              >
                <template #reference>
                  <el-input
                    v-model="selectedClassifyName"
                    placeholder="请选择课程分类"
                    readonly
                    clearable
                    @clear="handleClassifyClear"
                    style="width: 100%;"
                  >
                    <template #suffix>
                      <el-icon class="el-input__icon">
                        <ArrowDown v-if="!classifySelectVisible" />
                        <ArrowUp v-else />
                      </el-icon>
                    </template>
                  </el-input>
                </template>
                <el-tree
                  :data="classifyTreeData"
                  :props="{ children: 'children', label: 'classifyName' }"
                  node-key="classifyId"
                  :default-expand-all="false"
                  :expand-on-click-node="false"
                  highlight-current
                  @node-click="handleClassifyTreeSelect"
                  class="classify-select-tree"
                  :indent="20"
                >
                  <template #default="{ node, data }">
                    <span class="tree-select-node">
                      <span class="tree-select-node-label">{{ node.label }}</span>
                    </span>
                  </template>
                </el-tree>
              </el-popover>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 第二行：简介（全宽） -->
        <el-form-item label="简介" prop="details">
          <el-input v-model="form.details" type="textarea" :rows="4" placeholder="请输入课程简介" maxlength="200" show-word-limit/>
        </el-form-item>
        
        <!-- 第三行：封面图（左侧）、其他字段（右侧两列） -->
        <el-row>
          <el-col :span="8">
            <el-form-item label="封面图" prop="coverImage">
              <el-upload  
                :action="uploadFileUrl" 
                :file-list="coverImageList" 
                :limit="1"
                :on-exceed="handleExceed"
                :before-upload="beforeUploadCover"
                :on-preview="previewurl" 
                :on-error="handleUploadError" 
                :on-success="handleUploadSuccess"
                :on-remove="handleRemove" 
                :show-file-list="true" 
                :headers="headers" 
                accept="image/jpeg,image/jpg,image/png"
                class="upload-file-uploader"
                list-type="picture-card"
                ref="upload">
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="el-upload__tip" style="color: #606266; font-size: 12px; margin-top: 7px;">
                只能上传 JPG/PNG 格式的图片，且不超过 5MB
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-row>
              <el-col :span="12">
                <el-form-item label="难度" prop="difficultyLevel">
                  <el-select v-model="form.difficultyLevel" placeholder="请选择难度" clearable style="width: 100%;">
                    <el-option
                      v-for="dict in course_difficulty"
                      :key="dict.value"
                      :label="dict.label"
                      :value="dict.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="授课教师" prop="teacher">
                  <el-input v-model="form.teacher" placeholder="请输入授课教师" maxlength="100"/>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="12">
                <el-form-item label="学时" prop="creditHour">
                  <el-input 
                    v-model="form.creditHour" 
                    placeholder="请输入学时" 
                    maxlength="50"
                    @input="handleIntegerInput('creditHour', $event)"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="学分" prop="studyScore">
                  <el-input 
                    v-model="form.studyScore" 
                    placeholder="请输入学分" 
                    maxlength="50"
                    @input="handleIntegerInput('studyScore', $event)"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="12">
                <el-form-item label="价格" prop="price">
                  <el-input-number 
                    v-model="form.price" 
                    placeholder="请输入价格" 
                    :min="0" 
                    :precision="2" 
                    :step="0.01"
                    style="width: 100%;"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-col>
        </el-row>
        
        <!-- 章节管理表格 -->
        <el-divider content-position="left">章节管理</el-divider>
        <el-form-item label="章节列表">
          <el-table :data="filteredChapterList" style="width: 100%;" border>
            <el-table-column label="章节名称" prop="chapterName" min-width="200px">
              <template #default="scope">
                <el-input v-model="scope.row.chapterName" placeholder="请输入章节名称" maxlength="200" />
              </template>
            </el-table-column>
            <el-table-column label="序号" prop="chapterNum" width="100px">
              <template #default="scope">
                <el-input v-model="scope.row.chapterNum" placeholder="序号" maxlength="10" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200px" align="center">
              <template #default="scope">
                <el-button 
                  link 
                  type="primary" 
                  :disabled="scope.$index === 0"
                  @click="moveChapterUp(scope.row)">
                  上移
                </el-button>
                <el-button 
                  link 
                  type="primary" 
                  :disabled="scope.$index === filteredChapterList.length - 1"
                  @click="moveChapterDown(scope.row)">
                  下移
                </el-button>
                <el-button 
                  link 
                  type="danger" 
                  @click="handleRemoveChapter(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" icon="Plus" @click="handleAddChapter" style="margin-top: 10px;">添加章节</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm('1')">保 存</el-button>
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog title="审核课程" v-model="auditOpen" width="500px" append-to-body>
      <el-form ref="auditRef" :model="auditForm" label-width="100px">
        <el-form-item label="审核状态" prop="checkStatus">
          <el-radio-group v-model="auditForm.checkStatus">
            <el-radio label="4">通过</el-radio>
            <el-radio label="5">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="remark">
          <el-input v-model="auditForm.remark" type="textarea" :rows="4" placeholder="请输入审核意见"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAudit">确 定</el-button>
          <el-button @click="auditOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="课程详情" v-model="detailOpen" width="1200px" append-to-body>
      <div v-if="detailData">
        <!-- 上方：封面图和基本信息 -->
        <el-row :gutter="20" class="detail-header">
          <!-- 左侧封面图 -->
          <el-col :span="6">
            <div class="cover-image-container">
              <el-image 
                v-if="detailData.coverImage"
                :src="detailData.coverImage" 
                style="width: 100%; height: 250px;" 
                fit="cover"
              />
              <div v-else class="no-image">暂无封面</div>
            </div>
          </el-col>

          <!-- 右侧基本信息 -->
          <el-col :span="18">
            <div class="course-info">
              <h3 class="course-name">{{ detailData.name }}</h3>
              <p class="course-description" v-if="detailData.details">{{ detailData.details }}</p>
              
              <el-row :gutter="20" class="info-grid">
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">授课教师：</span>
                    <span class="value">{{ detailData.teacher || '未设置' }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">学时：</span>
                    <span class="value">{{ detailData.creditHour || '未设置' }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">学分：</span>
                    <span class="value">{{ detailData.studyScore || '未设置' }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">价格：</span>
                    <span class="value">{{ detailData.price || '未设置' }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">难度：</span>
                    <dict-tag :options="course_difficulty" :value="detailData.difficultyLevel"/>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-col>
        </el-row>

        <el-divider></el-divider>

        <!-- 下方：章节目录 -->
        <div class="chapter-directory">
          <h4 class="section-title">课程章节</h4>
          <el-collapse accordion>
            <el-collapse-item 
              v-for="chapter in detailData.courseChapterInfoList" 
              :key="chapter.chapterId"
              :title="`${chapter.chapterNum}. ${chapter.chapterName}`"
              :name="chapter.chapterId"
            >
              <el-table 
                v-if="chapter && chapter.chapterVideoList && chapter.chapterVideoList.length > 0"
                :data="chapter.chapterVideoList" 
                style="width: 100%;"
                stripe
              >
                <el-table-column label="视频序号" prop="videoNum" width="80px" align="center" />
                <el-table-column label="视频名称" prop="videoName" min-width="200px" show-overflow-tooltip />
                <el-table-column label="视频时长" prop="videoDuration" width="100px" align="center" />
                <el-table-column label="价格" prop="expenses" width="100px" align="center">
                  <template #default="scope">
                    <span>{{ scope.row.expenses === 0 || scope.row.expenses ? scope.row.expenses : '-' }}</span>
                  </template>
                </el-table-column>
              </el-table>
              <div v-else class="no-video">
                暂无视频
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 视频编辑对话框 -->
    <el-dialog title="视频编辑" v-model="chapterEditOpen" width="1400px" append-to-body @close="handleChapterEditClose">
      <el-row :gutter="20" style="height: 700px;">
        <!-- 左侧章节列表 -->
        <el-col :span="6" style="height: 100%; display: flex;">
          <div class="chapter-list-container" style="height: 100%; width: 100%; display: flex; flex-direction: column;">
            <div class="chapter-list-header">
              <!--<span>章节列表</span>-->
            </div>
            <div style="flex: 1; overflow: auto; min-height: 0;">
              <el-table 
                ref="editChapterTableRef"
                :data="editChapterList" 
                style="width: 100%;" 
                border
                highlight-current-row
                @current-change="handleChapterSelect"
                @row-click="handleChapterRowClick"
              >
                <el-table-column label="章节名称" prop="chapterName" min-width="100px" show-overflow-tooltip />

              </el-table>
            </div>
          </div>
        </el-col>

        <!-- 右侧视频列表 -->
        <el-col :span="18" style="height: 100%; display: flex;">
          <div class="video-list-container" style="height: 100%; width: 100%; display: flex; flex-direction: column;">
            <div class="video-list-header">
              <span>视频列表</span>
              <div>
                <el-button 
                  v-if="selectedChapter && selectedVideoIds.length > 0" 
                  type="danger" 
                  size="small" 
                  icon="Delete"
                  @click="handleBatchDeleteVideo"
                >批量删除</el-button>
                <el-button 
                  v-if="selectedChapter" 
                  type="primary" 
                  icon="Plus"
                  @click="handleAddVideo"
                >添加视频</el-button>
              </div>
            </div>
            <div style="flex: 1; overflow: auto; min-height: 0;">
              <el-table 
                :data="selectedChapterVideos" 
                style="width: 100%;" 
                border
                v-if="selectedChapter"
                @selection-change="handleVideoSelectionChange"
              >
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column label="视频序号" prop="videoNum" width="100px" align="center">
                <template #header>
                  <span>视频序号<span style="color: red;">*</span></span>
                </template>
                <template #default="scope">
                  <el-input 
                    v-model="scope.row.videoNum" 
                    placeholder="序号" 
                    maxlength="10"
                    :disabled="scope.row.checkStatus === '3'"
                    @input="(val) => handleVideoNumberInput(scope.row, val)"
                    :class="{ 'required-field': !scope.row.videoNum || scope.row.videoNum.toString().trim() === '' }"
                  />
                </template>
              </el-table-column>
              <el-table-column label="视频名称" prop="videoName" min-width="150px">
                <template #header>
                  <span>视频名称<span style="color: red;">*</span></span>
                </template>
                <template #default="scope">
                  <el-input 
                    v-model="scope.row.videoName" 
                    placeholder="请输入视频名称" 
                    maxlength="200"
                    :disabled="scope.row.checkStatus === '3'"
                    :class="{ 'required-field': !scope.row.videoName || scope.row.videoName.trim() === '' }"
                  />
                </template>
              </el-table-column>
              <el-table-column label="视频时长" prop="duration" width="100px" align="center" />
              <el-table-column label="是否免费" prop="isFree" width="120px" align="center">
                <template #header>
                  <span>是否免费<span style="color: red;">*</span></span>
                </template>
                <template #default="scope">
                  <el-select 
                    v-model="scope.row.isFree" 
                    placeholder="请选择" 
                    style="width: 100%;"
                    :disabled="scope.row.checkStatus === '3'"
                    @change="handleIsFreeChange(scope.row)"
                    :class="{ 'required-field': !scope.row.isFree || (scope.row.isFree !== 'Y' && scope.row.isFree !== 'N') }"
                  >
                    <el-option label="免费" value="Y" />
                    <el-option label="付费" value="N" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="价格" prop="expenses" width="120px" align="center">
                <template #header>
                  <span>价格<span style="color: red;">*</span></span>
                </template>
                <template #default="scope">
                  <el-input-number 
                    v-model="scope.row.expenses" 
                    :min="0" 
                    :precision="2" 
                    :step="0.01"
                    :disabled="scope.row.isFree === 'Y' || scope.row.checkStatus === '3'"
                    placeholder="价格"
                    style="width: 100%;"
                    controls-position="right"
                    :class="{ 'required-field': (scope.row.expenses === undefined || scope.row.expenses === null || scope.row.expenses === '') && scope.row.isFree === 'N' }"
                  />
                </template>
              </el-table-column>
              <el-table-column label="审核状态" prop="checkStatus" width="120px" align="center">
                <template #default="scope">
                  <dict-tag :options="check_status" :value="scope.row.checkStatus"/>
                </template>
              </el-table-column>
              <el-table-column label="审核意见" prop="applyReason" min-width="150px" show-overflow-tooltip>
                <template #default="scope">
                  <span>{{ scope.row.applyReason || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180px" align="center">
                <template #default="scope">
                  <el-button link type="primary" size="small" :disabled="scope.row.checkStatus === '3'" @click="handleUploadVideo(scope.row)">上传视频</el-button>
                  <el-button link type="danger" size="small" :disabled="scope.row.checkStatus === '3'" @click="handleDeleteVideo(scope.row)">删除</el-button>
                </template>
              </el-table-column>
              </el-table>
            </div>
            <div v-if="selectedChapter" style="padding: 10px; text-align: right; border-top: 1px solid #e4e7ed; flex-shrink: 0;">
              <el-button 
                type="success" 
                icon="Check"
                @click="submitChapterVideo"
              >保存章节视频</el-button>
              <el-button 
                type="primary" 
                icon="Promotion"
                @click="handleSubmitVideoAudit"
                :disabled="!hasPendingVideos"
                style="margin-left: 10px;"
              >提交审核</el-button>
            </div>
            <div v-else style="flex: 1; display: flex; align-items: center; justify-content: center; color: #909399;">
              请选择左侧章节
            </div>
          </div>
        </el-col>
      </el-row>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="chapterEditOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 视频上传对话框 -->
    <el-dialog title="上传视频" v-model="videoUploadDialogOpen" width="500px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="视频文件">
          <el-upload
            :action="uploadVideoUrl"
            :file-list="videoUploadList"
            :limit="1"
            :on-exceed="handleVideoUploadExceed"
            :on-success="handleVideoUploadSuccess"
            :on-error="handleVideoUploadError"
            :on-remove="handleVideoUploadRemove"
            :headers="headers"
            class="upload-file-uploader"
            accept="video/*"
          >
            <el-button type="primary" size="small">选择视频</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传视频文件，单个文件不超过500MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="视频时长" v-if="currentUploadVideo && currentUploadVideo.duration">
          <el-input 
            v-model="currentUploadVideo.duration" 
            placeholder="视频时长由后端自动获取"
            readonly
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="videoUploadDialogOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitVideoUpload" :disabled="!currentUploadVideo || (!currentUploadVideo.videoUrl && !currentUploadVideo.videoPath)">确 定</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup name="CourseInfo">
import { listCourseInfo, getCourseInfo, delCourseInfo, addCourseInfo, updateCourseInfo, exportCourseInfo, updateCourseStatus } from "@/api/course/courseInfo"
import { delChapterVideo, addChapterVideo, updateChapterVideo, getVideoListByChapterId } from "@/api/course/chapterVideo"
import { ElMessageBox } from 'element-plus'
import { systemTask } from '@/api/business'
import { getToken } from "@/utils/auth"
import { useDict } from "@/utils/dict"
import { parseTime } from "@/utils/ruoyi"
import modal from "@/plugins/modal"
import { Plus, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import Editor from '@/components/Editor'
import { getCurrentInstance, nextTick, computed, watch, ref, reactive, toRefs } from 'vue'
import { listClassify } from "@/api/course/courseType"
import { replaceFileOrigin } from '@/utils/fileOrigin'

const { proxy } = getCurrentInstance()
const { competition_status, course_difficulty, check_status, publish_status } = useDict('competition_status', 'course_difficulty', 'check_status', 'publish_status')
const queryRef = ref(null)
const courseInfoRef = ref(null)
const auditRef = ref(null)
const treeRef = ref(null)
const courseInfoList = ref([])
const classifyOptions = ref([])
const classifyTreeData = ref([])
const classifySearchText = ref('')
const expandedKeys = ref([])
const selectedClassifyId = ref(null)
const classifySelectVisible = ref(false)
const selectedClassifyName = ref('')
const open = ref(false)
const auditOpen = ref(false)
const detailOpen = ref(false)
const chapterEditOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const detailData = ref(null)
const editChapterList = ref([])
const selectedChapter = ref(null)
const currentEditCourseId = ref(null)
const videoUploadDialogOpen = ref(false)
const currentUploadVideo = ref(null)
const videoUploadList = ref([])
const editChapterTableRef = ref(null)
const selectedVideoIds = ref([])
const selectedVideos = ref([])

const data = reactive({
  form: {},
  auditForm: {
    checkStatus: '4',
    remark: ''
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    courseCode: null,
    classifyId: null,
    difficultyLevel: null,
    creditHour: null,
    teacher: null,
    publishStatus: null,
    checkStatus: null,
  },
  rules: {
    name: [
      { required: true, message: "课程名称不能为空", trigger: "blur" }
    ],
    classifyId: [
      { required: true, message: "课程分类不能为空", trigger: "change" }
    ],
    difficultyLevel: [
      { required: true, message: "难度不能为空", trigger: "change" }
    ],
    details: [
      { 
        required: true, 
        message: "简介不能为空", 
        trigger: "blur"
      },
      {
        max: 200,
        message: "简介不能超过200字",
        trigger: "blur"
      }
    ],
  }
})

const { queryParams, form, auditForm, rules } = toRefs(data)

/** 过滤后的章节列表（只显示未删除的章节，delFlag=0或未设置） */
const filteredChapterList = computed(() => {
  if (!form.value.courseChapterInfoList || !Array.isArray(form.value.courseChapterInfoList)) {
    return []
  }
  // 只显示 delFlag=0 或未设置 delFlag 的章节（过滤掉 delFlag=2 的已删除章节）
  return form.value.courseChapterInfoList.filter(chapter => {
    // delFlag 为 0 或未设置（undefined/null）的章节显示，delFlag=2 的已删除章节不显示
    return chapter.delFlag === undefined || chapter.delFlag === null || chapter.delFlag === 0 || chapter.delFlag === '0'
  })
})

/** 选中章节的视频列表 */
const selectedChapterVideos = computed(() => {
  if (!selectedChapter.value || !selectedChapter.value.courseVideoInfoList) {
    return []
  }
  return selectedChapter.value.courseVideoInfoList || []
})

/** 检查选中章节是否有待审核的视频 */
const hasPendingVideos = computed(() => {
  if (!selectedChapter.value || !selectedChapter.value.courseVideoInfoList) {
    return false
  }
  return selectedChapter.value.courseVideoInfoList.some(v => {
    return v.videoId && (v.checkStatus === '2' || v.checkStatus === 2)
  })
})

/** 检查课程的所有视频是否都审核通过（checkStatus='4'） */
const allVideosApproved = computed(() => {
  if (!form.value.courseChapterInfoList || !Array.isArray(form.value.courseChapterInfoList)) {
    return false
  }
  
  // 遍历所有章节的视频，检查是否都审核通过
  for (const chapter of form.value.courseChapterInfoList) {
    if (chapter.courseVideoInfoList && Array.isArray(chapter.courseVideoInfoList)) {
      for (const video of chapter.courseVideoInfoList) {
        // 如果有任何视频不是审核通过状态（checkStatus !== '4'），则返回false
        if (video.checkStatus !== '4') {
          return false
        }
      }
    }
  }
  
  // 如果没有视频，也返回false（不显示视频编辑按钮）
  let hasVideos = false
  for (const chapter of form.value.courseChapterInfoList) {
    if (chapter.courseVideoInfoList && chapter.courseVideoInfoList.length > 0) {
      hasVideos = true
      break
    }
  }
  
  return hasVideos
})

/** 过滤后的树形数据 */
const filteredClassifyTreeData = computed(() => {
  if (!classifySearchText.value || classifySearchText.value.trim() === '') {
    return classifyTreeData.value
  }
  
  const searchText = classifySearchText.value.trim().toLowerCase()
  
  // 递归过滤树形数据
  function filterTree(nodes) {
    const result = []
    nodes.forEach(node => {
      const match = node.classifyName && node.classifyName.toLowerCase().includes(searchText)
      const children = node.children && node.children.length > 0 ? filterTree(node.children) : []
      
      // 如果当前节点匹配或有子节点匹配，则保留该节点
      if (match || children.length > 0) {
        result.push({
          ...node,
          children: children
        })
      }
    })
    return result
  }
  
  return filterTree(classifyTreeData.value)
})

/** 查询课程分类列表 */
function getClassifyList() {
  listClassify().then(response => {
    const list = response.data || []
    console.log('课程分类列表数据:', list)
    console.log('第一条数据:', list[0])
    classifyOptions.value = list
    // 转换为树形结构
    classifyTreeData.value = buildTree(list)
    console.log('转换后的树形数据:', classifyTreeData.value)
    // 默认展开第一级节点（根节点）
    nextTick(() => {
      if (classifyTreeData.value.length > 0) {
        // 获取所有根节点的key
        const rootKeys = classifyTreeData.value.map(item => item.classifyId)
        // 设置展开的节点keys
        expandedKeys.value = rootKeys
      } else {
        expandedKeys.value = []
      }
    })
  }).catch((error) => {
    console.error('获取课程分类列表失败:', error)
    classifyOptions.value = []
    classifyTreeData.value = []
    expandedKeys.value = []
  })
}

/** 构建树形结构 */
function buildTree(list) {
  if (!list || list.length === 0) {
    return []
  }
  
  // 如果数据已经是树形结构，直接返回
  if (list.some(item => item.children && Array.isArray(item.children))) {
    return list
  }
  
  // 构建树形结构
  const map = {}
  const roots = []
  
  // 自动检测父级字段名
  let parentFieldName = null
  if (list.length > 0) {
    const possibleFields = ['parent_classify', 'parentClassify', 'parentClassifyId', 'parentId', 'pid', 'parent_id']
    for (const field of possibleFields) {
      if (list[0].hasOwnProperty(field)) {
        parentFieldName = field
        break
      }
    }
  }
  
  if (!parentFieldName) {
    // 如果没有找到父级字段，返回扁平结构
    return list.map(item => ({ ...item, children: [] }))
  }
  
  // 创建映射，初始化children数组
  list.forEach(item => {
    map[item.classifyId] = { ...item, children: [] }
  })
  
  // 构建树形关系
  list.forEach(item => {
    const node = map[item.classifyId]
    // 使用检测到的父级字段作为父级分类ID
    let parentId = item[parentFieldName]
    
    // 处理各种可能的空值情况：null, undefined, '', 'null', 'undefined', 0
    // 转换为字符串进行比较，处理类型不一致的情况
    const parentIdStr = String(parentId || '').trim().toLowerCase()
    const isEmpty = !parentId || 
                    parentId === '' || 
                    parentId === null || 
                    parentId === undefined ||
                    parentId === 0 ||
                    parentIdStr === 'null' || 
                    parentIdStr === 'undefined' ||
                    parentIdStr === ''
    
    // 判断是否有有效的父节点ID，并且父节点存在于map中
    // 同时需要处理类型转换（parentId 可能是字符串或数字）
    if (!isEmpty) {
      // 尝试多种类型匹配（处理字符串和数字类型不一致的情况）
      const parentNode = map[parentId] || map[String(parentId)] || map[Number(parentId)]
      if (parentNode) {
        // 有父节点，添加到父节点的children中
        parentNode.children.push(node)
        // 已添加到父节点，不需要添加到根节点，直接返回
        return
      } else {
        // 父节点不存在，可能是数据问题，作为根节点处理
      }
    }
    
    // 没有父节点（父级字段为空），作为根节点
    roots.push(node)
  })
  
  // 对每个节点的children进行排序（如果有sort字段）
  function sortChildren(nodes) {
    nodes.forEach(node => {
      if (node.children && node.children.length > 0) {
        node.children.sort((a, b) => {
          const sortA = a.sort || a.sortOrder || a.orderNum || 0
          const sortB = b.sort || b.sortOrder || b.orderNum || 0
          return sortA - sortB
        })
        sortChildren(node.children)
      }
    })
  }
  
  // 对根节点排序
  roots.sort((a, b) => {
    const sortA = a.sort || a.sortOrder || a.orderNum || 0
    const sortB = b.sort || b.sortOrder || b.orderNum || 0
    return sortA - sortB
  })
  
  // 递归排序所有子节点
  sortChildren(roots)
  
  // 如果没有父节点关系，直接返回扁平结构作为树
  if (roots.length === 0) {
    return list.map(item => ({ ...item, children: [] }))
  }
  
  return roots
}

/** 树节点点击事件 */
function handleTreeClick(data) {
  if (data && data.classifyId) {
    queryParams.value.classifyId = data.classifyId
    // 保存选中的分类ID，用于新增时默认选中
    selectedClassifyId.value = data.classifyId
    handleQuery()
  } else {
    // 点击根节点，清空分类筛选
    queryParams.value.classifyId = null
    selectedClassifyId.value = null
    handleQuery()
  }
}

/** 树节点展开事件 */
function handleTreeExpand(data, node) {
  // 更新展开的keys
  if (!expandedKeys.value.includes(data.classifyId)) {
    expandedKeys.value.push(data.classifyId)
  }
}

/** 树节点折叠事件 */
function handleTreeCollapse(data, node) {
  // 从展开的keys中移除
  const index = expandedKeys.value.indexOf(data.classifyId)
  if (index > -1) {
    expandedKeys.value.splice(index, 1)
  }
}

/** 分类搜索处理 */
function handleClassifySearch() {
  // 搜索时自动展开所有匹配的节点
  if (classifySearchText.value && classifySearchText.value.trim()) {
    nextTick(() => {
      const searchText = classifySearchText.value.trim().toLowerCase()
      const keysToExpand = new Set()
      
      // 递归收集需要展开的节点keys
      function collectKeys(nodes, parentPath = []) {
        nodes.forEach(node => {
          const currentPath = [...parentPath, node.classifyId]
          const match = node.classifyName && node.classifyName.toLowerCase().includes(searchText)
          
          // 如果当前节点匹配，展开路径上的所有节点
          if (match) {
            currentPath.forEach(key => keysToExpand.add(key))
          }
          
          // 递归处理子节点
          if (node.children && node.children.length > 0) {
            collectKeys(node.children, currentPath)
            // 如果子节点有匹配，也要展开当前节点
            const hasMatchingChild = node.children.some(child => 
              child.classifyName && child.classifyName.toLowerCase().includes(searchText)
            )
            if (hasMatchingChild) {
              currentPath.forEach(key => keysToExpand.add(key))
            }
          }
        })
      }
      
      collectKeys(filteredClassifyTreeData.value)
      expandedKeys.value = Array.from(keysToExpand)
    })
  } else {
    // 清空搜索时，只展开根节点
    if (classifyTreeData.value.length > 0) {
      const rootKeys = classifyTreeData.value.map(item => item.classifyId)
      expandedKeys.value = rootKeys
    }
  }
}

/** 清空分类搜索 */
function handleClassifySearchClear() {
  classifySearchText.value = ''
  // 恢复只展开根节点
  if (classifyTreeData.value.length > 0) {
    const rootKeys = classifyTreeData.value.map(item => item.classifyId)
    expandedKeys.value = rootKeys
  }
}

/** 查询课程信息列表 */
function getList() {
  loading.value = true
  listCourseInfo(queryParams.value).then(response => {
    courseInfoList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 通过list接口获取单个课程信息（用于获取章节列表） */
function getCourseInfoByList(courseId) {
  if (!courseId) {
    // 如果没有courseId，回退到详情接口
    return Promise.reject(new Error('courseId is required'))
  }
  
  // 使用list接口查询，传入具体的courseId参数
  // 注意：这里传入的是每个课程各自的courseId，确保每个课程使用自己的ID进行查询
  const queryParams = { 
    courseId: courseId,  // 传入具体课程的ID（每个课程各自的ID，不是统一的）
    pageNum: 1, 
    pageSize: 100  // 增大pageSize以确保能获取到目标课程
  }
  
  return listCourseInfo(queryParams).then(response => {
    if (response.rows && response.rows.length > 0) {
      // 从返回的列表中精确查找对应courseId的课程
      // 确保匹配的是传入的具体courseId，而不是其他课程的ID
      const course = response.rows.find(c => {
        // 处理可能的类型不一致问题（字符串 vs 数字）
        const courseIdMatch = c.courseId == courseId || 
                              String(c.courseId) === String(courseId) || 
                              Number(c.courseId) === Number(courseId)
        return courseIdMatch
      })
      
      if (course) {
        return { data: course }
      } else {
        // 如果没找到，尝试查询所有课程然后过滤（备选方案）
        return listCourseInfo({ pageNum: 1, pageSize: 1000 }).then(allResponse => {
          if (allResponse.rows && allResponse.rows.length > 0) {
            const foundCourse = allResponse.rows.find(c => {
              return c.courseId == courseId || 
                     String(c.courseId) === String(courseId) || 
                     Number(c.courseId) === Number(courseId)
            })
            if (foundCourse) {
              return { data: foundCourse }
            }
          }
          // 如果还是没找到，回退到详情接口
          return getCourseInfo(courseId)
        }).catch(() => {
          return getCourseInfo(courseId)
        })
      }
    } else {
      // 如果列表接口没有返回数据，回退到详情接口
      return getCourseInfo(courseId)
    }
  }).catch((error) => {
    // 如果列表接口失败，回退到详情接口
    return getCourseInfo(courseId)
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
    courseId: null,
    name: null,
    courseCode: null,
    classifyId: null,
    teacher: null,
    creditHour: null,
    studyScore: null,
    price: null,
    difficultyLevel: null,
    details: null,
    coverImage: null,
    publishStatus: '1',
    checkStatus: null,
    courseChapterInfoList: []
  }
  if (courseInfoRef.value) {
    courseInfoRef.value.resetFields()
  }
  coverImageList.value = []
  // 重置分类选择相关状态
  classifySelectVisible.value = false
  selectedClassifyName.value = ''
}

/** 处理数字输入，只允许输入数字和小数点 */
function handleNumberInput(field, value) {
  // Element Plus 的 @input 事件直接传递值，而不是事件对象
  const inputValue = typeof value === 'string' ? value : (value?.target?.value || '')
  // 只保留数字和小数点
  const numericValue = inputValue.replace(/[^\d.]/g, '')
  // 确保只有一个小数点
  const parts = numericValue.split('.')
  const filteredValue = parts.length > 2 
    ? parts[0] + '.' + parts.slice(1).join('')
    : numericValue
  // 更新表单字段值
  form.value[field] = filteredValue
}

/** 处理整数输入，只允许输入数字 */
function handleIntegerInput(field, value) {
  // Element Plus 的 @input 事件直接传递值，而不是事件对象
  const inputValue = typeof value === 'string' ? value : (value?.target?.value || '')
  // 只保留数字
  const integerValue = inputValue.replace(/[^\d]/g, '')
  // 更新表单字段值
  form.value[field] = integerValue
}

/** 处理查询条件中的整数输入 */
function handleQueryIntegerInput(field, value) {
  // 只保留数字
  const integerValue = value.replace(/[^\d]/g, '')
  // 更新查询参数
  queryParams.value[field] = integerValue
}

/** 处理视频序号输入，只允许输入数字 */
function handleVideoNumberInput(video, value) {
  // 只保留数字
  const integerValue = value.replace(/[^\d]/g, '')
  // 更新视频序号
  video.videoNum = integerValue
}

/** 分类树选择处理 */
function handleClassifyTreeSelect(data) {
  if (data && data.classifyId) {
    form.value.classifyId = data.classifyId
    selectedClassifyName.value = data.classifyName
    classifySelectVisible.value = false
  }
}

/** 清空分类选择 */
function handleClassifyClear() {
  form.value.classifyId = null
  selectedClassifyName.value = ''
}

/** 根据分类ID获取分类名称 */
function getClassifyNameById(classifyId) {
  if (!classifyId) return ''
  
  function findInTree(nodes, id) {
    for (const node of nodes) {
      if (node.classifyId === id) {
        return node.classifyName
      }
      if (node.children && node.children.length > 0) {
        const found = findInTree(node.children, id)
        if (found) return found
      }
    }
    return ''
  }
  
  return findInTree(classifyTreeData.value, classifyId)
}

// 监听 form.classifyId 变化，更新显示的分类名称
watch(() => form.value.classifyId, (newVal) => {
  if (newVal) {
    selectedClassifyName.value = getClassifyNameById(newVal)
  } else {
    selectedClassifyName.value = ''
  }
}, { immediate: true })

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  queryParams.value.pageSize = 10
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
  ids.value = selection.map(item => item.courseId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  // 重新加载分类列表，确保获取最新的分类数据
  getClassifyList()
  // 如果有选中的分类，设置为默认分类
  // 使用 nextTick 确保在表单重置后设置
  nextTick(() => {
    if (selectedClassifyId.value) {
      form.value.classifyId = selectedClassifyId.value
    }
  })
  open.value = true
  title.value = "添加课程信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  // 重新加载分类列表，确保获取最新的分类数据
  getClassifyList()
  const _courseId = row.courseId || ids.value
  // 使用list接口获取课程信息，确保能获取到最新的章节列表
  getCourseInfoByList(_courseId).then(response => {
    form.value = response.data
    // 确保章节列表存在，如果后端返回的字段名不同，尝试多种可能的字段名
    // 注意：这里不过滤已删除的章节，因为需要保留所有章节数据（包括已删除的）
    // 显示时会通过 filteredChapterList 计算属性自动过滤
    form.value.courseChapterInfoList = response.data.courseChapterInfoList || 
                                        response.data.chapterList || 
                                        response.data.chapters || 
                                        []
    // 确保所有章节都有 delFlag 字段（如果没有，默认为 '0'）
    form.value.courseChapterInfoList.forEach(chapter => {
      if (chapter.delFlag !== undefined && chapter.delFlag !== null) {
        // 如果已经有 delFlag，确保是字符串类型
        chapter.delFlag = String(chapter.delFlag)
      } else {
        // 如果都没有，默认为 '0'（未删除，字符串类型）
        chapter.delFlag = '0'
      }
    })
    // 如果有图片，设置图片列表
    if (form.value.coverImage) {
      coverImageList.value = [{
        name: '课程封面',
        url: form.value.coverImage
      }]
    }
    // 确保分类名称正确显示
    nextTick(() => {
      if (form.value.classifyId) {
        selectedClassifyName.value = getClassifyNameById(form.value.classifyId)
      }
    })
    open.value = true
    title.value = "修改课程信息"
  })
}

/** 提交按钮 */
function submitForm(status) {
  courseInfoRef.value.validate(valid => {
    if (valid) {
      // 设置状态
      if (status) {
        form.value.publishStatus = status
      }
      
      // 确保章节列表存在
      if (!form.value.courseChapterInfoList) {
        form.value.courseChapterInfoList = []
      }
      
      // 过滤章节列表
      // 1. 移除没有章节名称且未删除的章节（空章节）
      // 2. 移除没有 chapterId 且已删除的章节（新添加的章节，删除后不需要保存）
      // 3. 保留有章节名称的章节
      // 4. 保留有 chapterId 且已删除的章节（需要保存删除标记到后端）
      form.value.courseChapterInfoList = form.value.courseChapterInfoList.filter(chapter => {
        const isDeleted = chapter.delFlag === 2 || chapter.delFlag === '2'
        const hasChapterName = chapter.chapterName && chapter.chapterName.trim()
        const hasChapterId = chapter.chapterId !== null && chapter.chapterId !== undefined
        
        // 如果有章节名称，保留
        if (hasChapterName) {
          return true
        }
        
        // 如果是已删除的章节，且有 chapterId，保留（需要保存删除标记）
        if (isDeleted && hasChapterId) {
          return true
        }
        
        // 其他情况，移除
        return false
      })
      
      // 更新章节的courseId和delFlag
      // 确保每个章节的delFlag都被明确设置为字符串类型（数据库字段为char(1)）
      form.value.courseChapterInfoList.forEach(chapter => {
        chapter.courseId = form.value.courseId
        
        // 判断章节是否已删除（支持数字2和字符串'2'）
        const isDeleted = chapter.delFlag === 2 || chapter.delFlag === '2'
        
        if (isDeleted) {
          // 已删除的章节：设置为字符串 '2'（数据库char(1)类型）
          chapter.delFlag = '2'
          // 确保已删除的章节有必要的字段
          // 如果chapterName为null或undefined，设置为空字符串（避免后端处理null时出现问题）
          if (chapter.chapterName === null || chapter.chapterName === undefined) {
            chapter.chapterName = ''
          }
          // 确保courseId已设置
          if (!chapter.courseId) {
            chapter.courseId = form.value.courseId
          }
        } else {
          // 未删除的章节：设置为字符串 '0'（数据库char(1)类型）
          chapter.delFlag = '0'
        }
      })
      
      // 在提交前，验证已删除的章节数据完整性
      const deletedChaptersBeforeSubmit = form.value.courseChapterInfoList.filter(ch => {
        return ch.delFlag === '2' || ch.delFlag === 2
      })
      
      // 检查已删除的章节是否有必要的字段
      const invalidDeletedChapters = deletedChaptersBeforeSubmit.filter(ch => {
        return !ch.chapterId || (ch.delFlag !== '2' && ch.delFlag !== 2)
      })
      
      if (invalidDeletedChapters.length > 0) {
        console.warn('警告：已删除的章节数据不完整:', invalidDeletedChapters)
        modal.msgWarning('部分已删除的章节数据不完整，可能无法正确保存')
      }
      
      // 打印调试信息（开发时可以查看，生产环境可以移除）
      if (deletedChaptersBeforeSubmit.length > 0) {
        console.log('提交前已删除的章节数量:', deletedChaptersBeforeSubmit.length)
        console.log('已删除的章节详情:', deletedChaptersBeforeSubmit.map(ch => ({
          chapterId: ch.chapterId,
          chapterName: ch.chapterName || '(空)',
          delFlag: ch.delFlag,
          courseId: ch.courseId
        })))
      }
      
      if (form.value.courseId != null) {
        // 打印提交的数据（仅包含章节列表的关键信息）
        console.log('提交的章节列表:', form.value.courseChapterInfoList.map(ch => ({
          chapterId: ch.chapterId,
          chapterName: ch.chapterName || '(空)',
          delFlag: ch.delFlag,
          courseId: ch.courseId
        })))
        
        updateCourseInfo(form.value).then(response => {
          // 如果是提交审核（status='3'），需要创建审核任务
          if (status === '3') {
            return systemTask({businessId: form.value.courseId, auditType: 'course'})
          }
          return Promise.resolve()
        }).then(() => {
          modal.msgSuccess(status === '3' ? "提交审核成功" : "修改成功")
          // 保存成功后，使用list接口重新获取课程信息以显示最新的章节信息
          const savedCourseId = form.value.courseId
          
          // 保存当前已删除的章节信息（delFlag='2'），因为后端查询时可能不会返回这些章节
          const deletedChapters = (form.value.courseChapterInfoList || []).filter(chapter => {
            return chapter.delFlag === 2 || chapter.delFlag === '2'
          }).map(chapter => ({
            ...chapter,
            delFlag: '2'  // 使用字符串 '2' 确保数据库正确保存
          }))
          
          return getCourseInfoByList(savedCourseId).then(response => {
            // 保留当前表单的其他状态（如对话框打开状态等）
            const currentCoverImageList = coverImageList.value
            const currentSelectedClassifyName = selectedClassifyName.value
            
            // 更新表单数据
            form.value = response.data
            
            // 确保章节列表存在，如果后端返回的字段名不同，尝试多种可能的字段名
            // 直接从 response.data 中获取，确保获取到最新的章节列表
            let newChapterList = response.data.courseChapterInfoList || 
                                response.data.chapterList || 
                                response.data.chapters || 
                                []
            
            // 确保所有章节都有 delFlag 字段（如果没有，默认为 '0'）
            newChapterList.forEach(chapter => {
              if (chapter.delFlag !== undefined && chapter.delFlag !== null) {
                // 如果已经有 delFlag，确保是字符串类型
                chapter.delFlag = String(chapter.delFlag)
              } else {
                // 如果都没有，默认为 '0'（未删除）
                chapter.delFlag = '0'
              }
            })
            
            // 合并已删除的章节信息，确保删除状态不丢失
            // 创建一个映射，用于快速查找章节
            const chapterMap = new Map()
            newChapterList.forEach(chapter => {
              if (chapter.chapterId) {
                chapterMap.set(chapter.chapterId, chapter)
              }
            })
            
            // 将已删除的章节添加到列表中（如果它们不在新列表中）
            deletedChapters.forEach(deletedChapter => {
              if (deletedChapter.chapterId) {
                // 如果这个已删除的章节不在新列表中，或者在新列表中但 delFlag 不是 '2'，则添加/更新它
                if (!chapterMap.has(deletedChapter.chapterId)) {
                  // 不在新列表中，添加已删除的章节
                  newChapterList.push(deletedChapter)
                } else {
                  // 在新列表中，确保 delFlag 保持为 '2'（字符串）
                  const existingChapter = chapterMap.get(deletedChapter.chapterId)
                  existingChapter.delFlag = '2'
                }
              }
            })
            
            // 更新章节列表
            form.value.courseChapterInfoList = newChapterList
            
            // 如果有图片，更新图片列表
            if (form.value.coverImage) {
              coverImageList.value = [{
                name: '课程封面',
                url: form.value.coverImage
              }]
            } else {
              coverImageList.value = currentCoverImageList
            }
            
            // 更新分类名称
            if (form.value.classifyId) {
              selectedClassifyName.value = getClassifyNameById(form.value.classifyId)
            } else {
              selectedClassifyName.value = currentSelectedClassifyName
            }
            
            // 刷新主列表
            getList()
            // 保存成功后关闭对话框
            open.value = false
          }).catch(() => {
            // 如果重新获取失败，至少刷新主列表
            getList()
            // 即使重新获取失败，也关闭对话框
            open.value = false
          })
        }).catch(error => {
          console.error('更新课程信息失败:', error)
          console.error('错误详情:', {
            message: error?.message,
            response: error?.response?.data,
            status: error?.response?.status,
            requestData: {
              courseId: form.value.courseId,
              chapterCount: form.value.courseChapterInfoList?.length,
              deletedChapters: form.value.courseChapterInfoList?.filter(ch => ch.delFlag === '2' || ch.delFlag === 2).length
            }
          })
          const errorMsg = error?.response?.data?.msg || error?.msg || error?.message || '保存失败'
          modal.msgError(errorMsg)
        })
      } else {
        addCourseInfo(form.value).then(response => {
          // 获取新增后的课程ID
          const newCourseId = form.value.courseId || (response.data && response.data.courseId)
          // 如果是提交审核（status='3'），需要创建审核任务
          if (status === '3' && newCourseId) {
            return systemTask({businessId: newCourseId, auditType: 'course'})
          }
          return Promise.resolve(newCourseId)
        }).then((newCourseId) => {
          modal.msgSuccess(status === '3' ? "提交审核成功" : "新增成功")
          // 如果是新增，保存课程ID并重新获取详情
          if (newCourseId) {
            form.value.courseId = newCourseId
            // 使用list接口获取课程信息，确保能获取到最新的章节列表
            getCourseInfoByList(newCourseId).then(response => {
              form.value = response.data
              // 确保章节列表存在，如果后端返回的字段名不同，尝试多种可能的字段名
              // 注意：这里不过滤已删除的章节，因为需要保留所有章节数据（包括已删除的）
              // 显示时会通过 filteredChapterList 计算属性自动过滤
              form.value.courseChapterInfoList = response.data.courseChapterInfoList || 
                                                  response.data.chapterList || 
                                                  response.data.chapters || 
                                                  (form.value.courseChapterInfoList || [])
              // 确保所有章节都有 delFlag 字段（如果没有，默认为 '0'）
              form.value.courseChapterInfoList.forEach(chapter => {
                if (chapter.delFlag !== undefined && chapter.delFlag !== null) {
                  // 如果已经有 delFlag，确保是字符串类型
                  chapter.delFlag = String(chapter.delFlag)
                } else {
                  // 如果都没有，默认为 '0'（未删除，字符串类型）
                  chapter.delFlag = '0'
                }
              })
            }).catch(() => {
            })
          }
          // 刷新主列表
          getList()
          // 保存成功后关闭对话框
          open.value = false
        }).catch(error => {
          console.error('新增课程信息失败:', error)
          console.error('错误详情:', {
            message: error?.message,
            response: error?.response?.data,
            status: error?.response?.status
          })
          const errorMsg = error?.response?.data?.msg || error?.msg || error?.message || '保存失败'
          modal.msgError(errorMsg)
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _courseIds = row.courseId || ids.value
  modal.confirm('是否确认删除课程编号为"' + _courseIds + '"的数据项？').then(function() {
    return delCourseInfo(_courseIds)
  }).then(() => {
    getList()
    modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 提交审核 */
function handleSubmitAudit(row) {
  ElMessageBox.confirm('是否确认提交审核课程"' + row.name + '"？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'reverse-confirm-buttons'
  }).then(() => {
    return systemTask({businessId: row.courseId, auditType: 'course'})
  }).then(() => {
    getList()
    modal.msgSuccess('提交成功')
  }).catch(() => {})
}

/** 审核 */
function handleAudit(row) {
  auditForm.value = {
    businessId: row.courseId,
    checkStatus: '4',
    remark: '',
    courseIds: null
  }
  auditOpen.value = true
}

/** 提交审核 */
function submitAudit() {
  if (auditForm.value.courseIds && auditForm.value.courseIds.length > 0) {
    // 批量审核
    submitBatchAudit()
  } else if (auditForm.value.businessId) {
    // 单个审核
    updateCourseStatus({
      pageId: auditForm.value.businessId,
      checkStatus: auditForm.value.checkStatus
    }).then(() => {
      modal.msgSuccess("审核成功")
      auditOpen.value = false
      getList()
    }).catch(() => {
      modal.msgError("审核失败")
    })
  }
}

/** 发布课程 */
function handlePublish(row) {
  modal.confirm('是否确认发布课程"' + row.name + '"？').then(function() {
    // 设置发布状态为已发布，并设置发布时间为当前时间
    const now = new Date()
    // 格式化为 yyyy-MM-dd HH:mm:ss 格式
    const publishTime = parseTime(now, '{y}-{m}-{d} {h}:{i}:{s}')
    form.value = { 
      courseId: row.courseId, 
      publishStatus: '2',
      publishTime: publishTime
    }
    return updateCourseInfo(form.value)
  }).then(() => {
    getList()
    modal.msgSuccess("发布成功")
  }).catch(() => {})
}

/** 下架课程 */
function handleOffline(row) {
  modal.confirm('是否确认下架课程"' + row.name + '"？').then(function() {
    form.value = { courseId: row.courseId, publishStatus: '3' }
    return updateCourseInfo(form.value)
  }).then(() => {
    getList()
    modal.msgSuccess("下架成功")
  }).catch(() => {})
}

/** 详情 */
function handleDetail(row) {
  // 使用 /course/courseInfo/{courseId} 接口获取完整的课程信息，包括章节和视频
  getCourseInfo(row.courseId).then(response => {
    const courseData = response.data
    // 过滤出未删除的章节
    if (courseData.courseChapterInfoList) {
      courseData.courseChapterInfoList = courseData.courseChapterInfoList.filter(chapter => {
        return chapter.delFlag === undefined || chapter.delFlag === null || chapter.delFlag === 0 || chapter.delFlag === '0'
      })
      // 确保每个章节都有 chapterVideoList 属性
      courseData.courseChapterInfoList.forEach(chapter => {
        if (!chapter.chapterVideoList) {
          chapter.chapterVideoList = []
        }
      })
    }
    detailData.value = courseData
    detailOpen.value = true
  }).catch(error => {
    ElMessage.error('获取课程详情失败：' + (error.message || '未知错误'))
  })
}

/** 视频编辑 */
function handleChapterEdit(row) {
  currentEditCourseId.value = row.courseId
  // 获取课程详情，包含章节和视频信息
  getCourseInfoByList(row.courseId).then(response => {
    const courseData = response.data
    // 获取未删除的章节列表
    let chapterList = courseData.courseChapterInfoList || []
    editChapterList.value = chapterList.filter(chapter => {
      return chapter.delFlag === undefined || chapter.delFlag === null || chapter.delFlag === 0 || chapter.delFlag === '0'
    })
    
    // 默认选中第一个章节
    if (editChapterList.value.length > 0) {
      selectedChapter.value = editChapterList.value[0]
      // 确保视频列表存在
      if (!selectedChapter.value.courseVideoInfoList) {
        selectedChapter.value.courseVideoInfoList = []
      }
    } else {
      selectedChapter.value = null
    }
    chapterEditOpen.value = true
    
    // 在对话框打开后，设置表格的当前行（使用双重nextTick确保DOM已渲染）
    nextTick(() => {
      nextTick(() => {
        if (editChapterTableRef.value && editChapterList.value.length > 0) {
          const firstChapter = editChapterList.value[0]
          editChapterTableRef.value.setCurrentRow(firstChapter)
          // 触发选择事件以确保所有状态正确初始化
          handleChapterSelect(firstChapter)
        }
      })
    })
  }).catch(() => {
    modal.msgError('获取课程信息失败')
  })
}

/** 章节选择 */
function handleChapterSelect(row) {
  if (!row) {
    selectedChapter.value = null
    selectedVideoIds.value = []
    selectedVideos.value = []
    return
  }
  
  selectedChapter.value = row
  
  // 如果章节有ID且没有视频列表，或者视频列表为空，尝试从后端加载
  if (row.chapterId && (!row.courseVideoInfoList || row.courseVideoInfoList.length === 0)) {
    getVideoListByChapterId(row.chapterId).then(response => {
      if (response.data && Array.isArray(response.data)) {
        // 将获取到的视频列表映射到当前章节
        const videoList = response.data.map(video => {
          return {
            videoId: video.videoId,
            videoName: video.videoName || '',
            videoNum: video.videoNum || '',
            duration: video.videoDuration || video.duration || '',
            videoDuration: video.videoDuration || video.duration || '',
            isFree: video.isFree || 'Y',
            expenses: video.expenses !== undefined && video.expenses !== null ? Number(video.expenses) : 0,
            videoUrl: video.videoFile || video.videoUrl || '',
            videoPath: video.videoFile || video.videoPath || '',
            videoFile: video.videoFile || '',
            checkStatus: video.checkStatus || '2',
            applyReason: video.applyReason || '',
            chapterId: row.chapterId,
            courseId: currentEditCourseId.value
          }
        })
        
        // 更新当前选中章节的视频列表
        if (selectedChapter.value && selectedChapter.value.chapterId === row.chapterId) {
          selectedChapter.value.courseVideoInfoList = videoList
          // 初始化 expenses 字段
          videoList.forEach(video => {
            if (video.expenses === undefined || video.expenses === null) {
              video.expenses = 0
            }
            if (video.isFree === 'Y' && video.expenses !== 0) {
              video.expenses = 0
            }
          })
        }
      }
    }).catch(error => {
      console.error('获取视频列表失败:', error)
      // 如果获取失败，初始化为空数组
      if (selectedChapter.value && selectedChapter.value.chapterId === row.chapterId) {
        selectedChapter.value.courseVideoInfoList = []
      }
    })
  } else {
    // 确保视频列表存在
    if (!row.courseVideoInfoList) {
      row.courseVideoInfoList = []
    }
    // 初始化视频的 expenses 字段（如果不存在）
    row.courseVideoInfoList.forEach(video => {
      if (video.expenses === undefined || video.expenses === null) {
        video.expenses = 0
      } else {
        // 确保 expenses 是数字类型
        video.expenses = Number(video.expenses) || 0
      }
      // 如果免费，确保价格为0
      if (video.isFree === 'Y' && video.expenses !== 0) {
        video.expenses = 0
      }
    })
  }
  
  // 切换章节时清空视频选择
  selectedVideoIds.value = []
  selectedVideos.value = []
}

/** 章节行点击事件 */
function handleChapterRowClick(row) {
  // 点击行时设置当前选中行
  if (editChapterTableRef.value) {
    editChapterTableRef.value.setCurrentRow(row)
  }
  // 触发选择事件
  handleChapterSelect(row)
}

/** 添加视频 */
function handleAddVideo() {
  if (!selectedChapter.value) {
    modal.msgWarning('请先选择章节')
    return
  }
  
  if (!selectedChapter.value.courseVideoInfoList) {
    selectedChapter.value.courseVideoInfoList = []
  }
  
  // 计算下一个视频序号
  const maxNum = selectedChapter.value.courseVideoInfoList.length > 0
    ? Math.max(...selectedChapter.value.courseVideoInfoList.map(v => parseInt(v.videoNum) || 0))
    : 0
  
  selectedChapter.value.courseVideoInfoList.push({
    videoId: null,
    videoName: '',
    videoNum: String(maxNum + 1),
    duration: '',
    isFree: 'Y',
    expenses: 0,  // 价格默认为0
    checkStatus: '2',  // 审核状态默认为待审核
    chapterId: selectedChapter.value.chapterId,
    courseId: currentEditCourseId.value
  })
}

/** 是否免费变化处理 */
function handleIsFreeChange(video) {
  if (video.isFree === 'Y') {
    // 免费时，价格自动设置为0
    video.expenses = 0
  } else {
    // 付费时，如果价格为0或未设置，可以保持为空让用户输入
    if (video.expenses === undefined || video.expenses === null) {
      video.expenses = 0
    }
  }
  
  // 同步更新课程详情中对应视频的isFree值
  if (detailData.value && detailData.value.courseChapterInfoList && video.videoId && video.chapterId) {
    // 找到对应的章节
    const chapter = detailData.value.courseChapterInfoList.find(ch => 
      ch.chapterId === video.chapterId || 
      String(ch.chapterId) === String(video.chapterId)
    )
    
    if (chapter && chapter.chapterVideoList) {
      // 找到对应的视频
      const detailVideo = chapter.chapterVideoList.find(v => 
        v.videoId === video.videoId || 
        String(v.videoId) === String(video.videoId)
      )
      
      if (detailVideo) {
        // 同步更新isFree值
        detailVideo.isFree = video.isFree
      }
    }
  }
}

/** 上传视频 */
function handleUploadVideo(video) {
  // 打开视频上传对话框
  currentUploadVideo.value = video
  videoUploadList.value = []
  videoUploadDialogOpen.value = true
}

/** 视频上传超出限制 */
function handleVideoUploadExceed(files, fileList) {
  modal.msgWarning('最多上传1个视频文件')
}

/** 视频上传成功 */
function handleVideoUploadSuccess(res, file) {
  res = replaceFileOrigin(res)
  if (res.code === 0 || res.code === 200) {
    // 上传成功，获取视频信息
    if (currentUploadVideo.value) {
      const videoUrl = res.data.url || res.data.videoUrl
      currentUploadVideo.value.videoUrl = videoUrl
      currentUploadVideo.value.videoPath = videoUrl || res.data.videoPath
      
      // uploadVideo接口会直接返回视频时长信息
      // 从后端返回数据中获取视频时长（支持多种可能的字段名）
      if (res.data && res.data.duration) {
        currentUploadVideo.value.duration = res.data.duration
      } else if (res.data && res.data.videoDuration) {
        currentUploadVideo.value.duration = res.data.videoDuration
      } else if (res.data && res.data.time) {
        currentUploadVideo.value.duration = res.data.time
      } else if (res.data && res.data.length) {
        currentUploadVideo.value.duration = res.data.length
      }
      
      // 将视频状态重置为待审核（'2'）
      currentUploadVideo.value.checkStatus = '2'
      currentUploadVideo.value.applyReason = ''
      
      modal.msgSuccess('视频上传成功' + (currentUploadVideo.value.duration ? '，已获取视频时长' : '') + '，状态已重置为待审核')
    }
  } else {
    modal.msgError(res.msg || '视频上传失败')
  }
}

/** 视频上传失败 */
function handleVideoUploadError(err) {
  modal.msgError('视频上传失败，请重试')
}

/** 视频上传移除 */
function handleVideoUploadRemove(file, fileList) {
  videoUploadList.value = fileList
}

/** 提交视频上传 */
function submitVideoUpload() {
  if (!currentUploadVideo.value) {
    modal.msgWarning('请先选择视频')
    return
  }
  
  if (!currentUploadVideo.value.videoUrl && !currentUploadVideo.value.videoPath) {
    modal.msgWarning('请先上传视频文件')
    return
  }
  
  // 视频时长由后端返回，如果后端没有返回，可以设置为空字符串或提示
  // 这里不强制要求视频时长，因为可能后端需要处理视频后才能获取时长
  
  videoUploadDialogOpen.value = false
  videoUploadList.value = []
  modal.msgSuccess('视频信息已保存')
}

/** 视频选择变化 */
function handleVideoSelectionChange(selection) {
  selectedVideos.value = selection
  selectedVideoIds.value = selection
    .filter(v => v.videoId) // 只包含有ID的视频
    .map(v => v.videoId)
}

/** 批量删除视频 */
function handleBatchDeleteVideo() {
  if (!selectedChapter.value || !selectedChapter.value.courseVideoInfoList) {
    modal.msgWarning('请先选择章节')
    return
  }
  
  if (selectedVideoIds.value.length === 0 && selectedVideos.value.length === 0) {
    modal.msgWarning('请选择要删除的视频')
    return
  }
  
  // 分离有ID和没有ID的视频
  const videosWithId = selectedVideos.value.filter(v => v.videoId)
  const videosWithoutId = selectedVideos.value.filter(v => !v.videoId)
  
  // 如果只有没有ID的视频（新添加的），直接删除
  if (videosWithId.length === 0 && videosWithoutId.length > 0) {
    modal.confirm(`是否确认删除选中的 ${videosWithoutId.length} 个视频？`).then(() => {
      videosWithoutId.forEach(video => {
        const index = selectedChapter.value.courseVideoInfoList.findIndex(v => v === video)
        if (index > -1) {
          selectedChapter.value.courseVideoInfoList.splice(index, 1)
        }
      })
      // 清空选择
      selectedVideoIds.value = []
      selectedVideos.value = []
      modal.msgSuccess('删除成功')
    }).catch(() => {
      // 用户取消
    })
    return
  }
  
  // 先处理没有ID的视频（新添加的），直接从列表中移除
  videosWithoutId.forEach(video => {
    const index = selectedChapter.value.courseVideoInfoList.findIndex(v => v === video)
    if (index > -1) {
      selectedChapter.value.courseVideoInfoList.splice(index, 1)
    }
  })
  
  // 如果有ID的视频，调用接口删除
  if (videosWithId.length > 0) {
    const chapterId = selectedChapter.value.chapterId
    if (!chapterId) {
      modal.msgError('章节ID不能为空')
      return
    }
    
    const videoIds = videosWithId.map(v => v.videoId)
    modal.confirm(`是否确认删除选中的 ${videoIds.length} 个视频？`).then(() => {
      return delChapterVideo(videoIds, chapterId)
    }).then(() => {
      // 删除成功，从列表中移除
      videoIds.forEach(videoId => {
        const index = selectedChapter.value.courseVideoInfoList.findIndex(v => v.videoId === videoId)
        if (index > -1) {
          selectedChapter.value.courseVideoInfoList.splice(index, 1)
        }
      })
      // 清空选择
      selectedVideoIds.value = []
      selectedVideos.value = []
      modal.msgSuccess('删除成功')
    }).catch(() => {
      // 删除失败或用户取消
    })
  } else {
    // 只有没有ID的视频，直接移除成功
    selectedVideoIds.value = []
    selectedVideos.value = []
    modal.msgSuccess('删除成功')
  }
}

/** 删除视频 */
function handleDeleteVideo(video) {
  if (!selectedChapter.value || !selectedChapter.value.courseVideoInfoList) {
    modal.msgWarning('请先选择章节')
    return
  }
  
  if (!video.videoId) {
    // 如果视频还没有ID（新添加的视频），直接从列表中移除
    const index = selectedChapter.value.courseVideoInfoList.findIndex(v => v === video)
    if (index > -1) {
      selectedChapter.value.courseVideoInfoList.splice(index, 1)
    }
    modal.msgSuccess('删除成功')
    return
  }
  
  // 确认删除
  modal.confirm('是否确认删除视频"' + (video.videoName || '') + '"？').then(() => {
    // 调用删除接口，传递视频ID数组（即使只有一个）
    const videoIds = [video.videoId]
    const chapterId = selectedChapter.value.chapterId
    
    if (!chapterId) {
      modal.msgError('章节ID不能为空')
      return
    }
    
    return delChapterVideo(videoIds, chapterId)
  }).then(() => {
    // 删除成功，从列表中移除
    const index = selectedChapter.value.courseVideoInfoList.findIndex(v => v.videoId === video.videoId)
    if (index > -1) {
      selectedChapter.value.courseVideoInfoList.splice(index, 1)
    }
    modal.msgSuccess('删除成功')
  }).catch(() => {
    // 删除失败或用户取消
  })
}

/** 保存章节视频（只保存当前选中章节的视频） */
function submitChapterVideo() {
  if (!selectedChapter.value) {
    modal.msgWarning('请先选择章节')
    return
  }
  
  if (!selectedChapter.value.courseVideoInfoList || selectedChapter.value.courseVideoInfoList.length === 0) {
    modal.msgWarning('当前章节没有视频')
    return
  }
  
  const chapterId = selectedChapter.value.chapterId
  if (!chapterId) {
    modal.msgError('章节ID不能为空')
    return
  }
  
  const videos = selectedChapter.value.courseVideoInfoList
  
  // 验证必填字段
  for (let i = 0; i < videos.length; i++) {
    const video = videos[i]
    
    // 验证视频序号
    if (!video.videoNum || video.videoNum.toString().trim() === '') {
      modal.msgError(`第${i + 1}个视频的序号不能为空`)
      return
    }
    
    // 验证视频名称
    if (!video.videoName || video.videoName.trim() === '') {
      modal.msgError(`第${i + 1}个视频的名称不能为空`)
      return
    }
    
    // 验证是否免费
    if (!video.isFree || (video.isFree !== 'Y' && video.isFree !== 'N')) {
      modal.msgError(`第${i + 1}个视频的"是否免费"不能为空`)
      return
    }
    
    // 验证价格
    if (video.expenses === undefined || video.expenses === null || video.expenses === '') {
      modal.msgError(`第${i + 1}个视频的价格不能为空`)
      return
    }
    
    // 确保价格为数字类型
    const expensesNum = Number(video.expenses)
    if (isNaN(expensesNum)) {
      modal.msgError(`第${i + 1}个视频的价格必须为数字`)
      return
    }
    
    // 价格必须大于等于0
    if (expensesNum < 0) {
      modal.msgError(`第${i + 1}个视频的价格必须大于等于0`)
      return
    }
    
    // 如果是免费，价格必须为0
    if (video.isFree === 'Y' && expensesNum !== 0) {
      modal.msgError(`第${i + 1}个视频为免费，价格必须为0`)
      return
    }
  }
  
  // 分离需要更新的视频和需要新增的视频
  const videosToUpdate = []
  const videosToAdd = []
  
  // 遍历所有视频，分类保存
  try {
    videos.forEach(video => {
      // 准备视频数据
      const videoData = {
        chapterId: chapterId,
        videoName: video.videoName || '',
        videoNum: video.videoNum || '',
        videoDuration: video.duration || video.videoDuration || '',
        isFree: video.isFree || 'Y',
        expenses: video.expenses !== undefined && video.expenses !== null ? Number(video.expenses) : 0,
        videoFile: video.videoFile || video.videoUrl || video.videoPath || ''
      }
      
      // 如果视频有ID，说明是已存在的视频，需要更新
      if (video.videoId) {
        videoData.videoId = video.videoId
        videosToUpdate.push({ video, videoData })
      } else {
        // 没有ID，说明是新添加的视频，需要新增
        // 新视频必须上传文件才能保存
        if (!videoData.videoFile && !video.videoUrl && !video.videoPath) {
          throw new Error(`新视频"${video.videoName || '未命名'}"必须上传文件才能保存`)
        }
        videosToAdd.push({ video, videoData })
      }
    })
  } catch (error) {
    modal.msgError(error.message)
    return
  }
  
  // 如果没有需要保存的视频，提示用户
  if (videosToUpdate.length === 0 && videosToAdd.length === 0) {
    modal.msgWarning('没有需要保存的视频（新视频需要先上传文件）')
    return
  }
  
  // 串行执行保存操作，避免死锁
  // 先执行所有更新操作，再执行所有新增操作
  const executeSave = async () => {
    try {
      // 先执行所有更新操作（串行）
      for (const { video, videoData } of videosToUpdate) {
        await updateChapterVideo(videoData).catch(error => {
          console.error('更新视频失败:', video, error)
          throw error
        })
      }
      
      // 再执行所有新增操作（串行）
      for (const { video, videoData } of videosToAdd) {
        await addChapterVideo(videoData).catch(error => {
          console.error('新增视频失败:', video, error)
          throw error
        })
      }
      
      return Promise.resolve()
    } catch (error) {
      return Promise.reject(error)
    }
  }
  
  // 执行保存操作
  executeSave().then(() => {
    const totalCount = videosToUpdate.length + videosToAdd.length
    const updateCount = videosToUpdate.length
    const addCount = videosToAdd.length
    let successMsg = `保存成功`
    if (updateCount > 0 && addCount > 0) {
      successMsg = `保存成功（更新 ${updateCount} 个，新增 ${addCount} 个）`
    } else if (updateCount > 0) {
      successMsg = `保存成功（更新 ${updateCount} 个视频）`
    } else if (addCount > 0) {
      successMsg = `保存成功（新增 ${addCount} 个视频）`
    }
    modal.msgSuccess(successMsg)
    // 保存成功后，重新获取当前章节的视频列表
    if (chapterId) {
      getVideoListByChapterId(chapterId).then(response => {
        if (response.data && Array.isArray(response.data)) {
          // 将获取到的视频列表映射到当前章节
          const videoList = response.data.map(video => {
            // 确保字段映射正确
            return {
              videoId: video.videoId,
              videoName: video.videoName || '',
              videoNum: video.videoNum || '',
              duration: video.videoDuration || video.duration || '',
              videoDuration: video.videoDuration || video.duration || '',
              isFree: video.isFree || 'Y',
              expenses: video.expenses !== undefined && video.expenses !== null ? Number(video.expenses) : 0,
              videoUrl: video.videoFile || video.videoUrl || '',
              videoPath: video.videoFile || video.videoPath || '',
              videoFile: video.videoFile || '',
              checkStatus: video.checkStatus || '2',
              applyReason: video.applyReason || '',
              chapterId: chapterId,
              courseId: currentEditCourseId.value
            }
          })
          
          // 更新当前选中章节的视频列表
          if (selectedChapter.value && selectedChapter.value.chapterId === chapterId) {
            selectedChapter.value.courseVideoInfoList = videoList
            // 初始化 expenses 字段
            videoList.forEach(video => {
              if (video.expenses === undefined || video.expenses === null) {
                video.expenses = 0
              }
              if (video.isFree === 'Y' && video.expenses !== 0) {
                video.expenses = 0
              }
            })
          }
          
          // 同步更新课程详情中对应章节的视频列表（如果课程详情对话框是打开的）
          if (detailData.value && detailData.value.courseChapterInfoList) {
            const detailChapter = detailData.value.courseChapterInfoList.find(ch => 
              ch.chapterId === chapterId || 
              String(ch.chapterId) === String(chapterId)
            )
            if (detailChapter) {
              // 更新章节的视频列表，确保字段名匹配
              detailChapter.chapterVideoList = videoList.map(video => ({
                ...video,
                // 确保字段名与详情显示一致
                videoDuration: video.duration || video.videoDuration || ''
              }))
            }
          }
        }
      }).catch(error => {
        console.error('获取视频列表失败:', error)
        // 如果获取失败，尝试从课程信息中获取
        if (currentEditCourseId.value) {
          getCourseInfoByList(currentEditCourseId.value).then(response => {
            const courseData = response.data
            let chapterList = courseData.courseChapterInfoList || []
            chapterList = chapterList.filter(chapter => {
              return chapter.delFlag === undefined || chapter.delFlag === null || chapter.delFlag === 0 || chapter.delFlag === '0'
            })
            
            // 更新章节列表
            editChapterList.value = chapterList
            
            // 更新当前选中章节的视频列表
            const updatedChapter = chapterList.find(ch => ch.chapterId === chapterId)
            if (updatedChapter) {
              // 尝试从多个可能的字段名中获取视频列表
              const videoList = updatedChapter.courseVideoInfoList || 
                               updatedChapter.chapterVideoList || 
                               updatedChapter.videoList || 
                               []
              
              selectedChapter.value = updatedChapter
              selectedChapter.value.courseVideoInfoList = videoList
              
              // 初始化 expenses 字段
              videoList.forEach(video => {
                if (video.expenses === undefined || video.expenses === null) {
                  video.expenses = 0
                }
                if (video.isFree === 'Y' && video.expenses !== 0) {
                  video.expenses = 0
                }
              })
              
              // 同步更新课程详情中对应章节的视频列表（如果课程详情对话框是打开的）
              if (detailData.value && detailData.value.courseChapterInfoList) {
                const detailChapter = detailData.value.courseChapterInfoList.find(ch => 
                  ch.chapterId === chapterId || 
                  String(ch.chapterId) === String(chapterId)
                )
                if (detailChapter) {
                  // 更新章节的视频列表，确保字段名匹配
                  detailChapter.chapterVideoList = videoList.map(video => ({
                    ...video,
                    // 确保字段名与详情显示一致
                    videoDuration: video.duration || video.videoDuration || ''
                  }))
                }
              }
            }
          }).catch(() => {
            // 如果重新获取失败，不影响保存成功的提示
          })
        }
      })
    }
  }).catch(error => {
    console.error('保存视频失败:', error)
    const errorMsg = error?.response?.data?.msg || error?.msg || error?.message || '保存失败'
    modal.msgError(errorMsg)
  })
}

/** 刷新当前选中章节的视频列表 */
function refreshSelectedChapterVideoList(chapterId) {
  if (!chapterId) {
    return Promise.resolve()
  }
  return getVideoListByChapterId(chapterId).then(response => {
    if (response.data && Array.isArray(response.data)) {
      const videoList = response.data.map(video => ({
        videoId: video.videoId,
        videoName: video.videoName || '',
        videoNum: video.videoNum || '',
        duration: video.videoDuration || video.duration || '',
        videoDuration: video.videoDuration || video.duration || '',
        isFree: video.isFree || 'Y',
        expenses: video.expenses !== undefined && video.expenses !== null ? Number(video.expenses) : 0,
        videoUrl: video.videoFile || video.videoUrl || '',
        videoPath: video.videoFile || video.videoPath || '',
        videoFile: video.videoFile || '',
        checkStatus: video.checkStatus || '2',
        applyReason: video.applyReason || '',
        chapterId,
        courseId: currentEditCourseId.value
      }))
      videoList.forEach(video => {
        if (video.expenses === undefined || video.expenses === null) {
          video.expenses = 0
        }
        if (video.isFree === 'Y' && video.expenses !== 0) {
          video.expenses = 0
        }
      })
      if (selectedChapter.value && selectedChapter.value.chapterId === chapterId) {
        selectedChapter.value.courseVideoInfoList = videoList
      }
    }
  }).catch(error => {
    console.error('刷新章节视频列表失败:', error)
  })
}

/** 提交视频审核 */
function handleSubmitVideoAudit() {
  if (!selectedChapter.value) {
    modal.msgWarning('请先选择章节')
    return
  }
  
  const chapterId = selectedChapter.value?.chapterId
  if (!chapterId) {
    modal.msgWarning('章节信息不完整，请刷新后重试')
    return
  }
  
  // 获取所有审核状态为待审核（'2'）的视频
  const videosToAudit = (selectedChapter.value.courseVideoInfoList || []).filter(v => {
    return v.videoId && (v.checkStatus === '2' || v.checkStatus === 2)
  })
  
  if (videosToAudit.length === 0) {
    modal.msgWarning('当前章节中没有审核状态为待审核的视频')
    return
  }
  
  // 确认提交审核
  modal.confirm(`是否确认提交 ${videosToAudit.length} 个视频的审核？`).then(() => {
    // 构建 pageInfo 数组，包含所有待审核视频的 pageId
    const pageInfo = videosToAudit.map(video => ({
      pageId: video.videoId
    }))
    
    // 一次性提交所有视频
    const payload = {
      auditType: 'chapterVideo',
      chapterAuditResult: {
        chapterId,
        pageInfo: pageInfo
      }
    }
    
    systemTask(payload)
      .then(() => {
        modal.msgSuccess(`成功提交 ${videosToAudit.length} 个视频的审核`)
        
        // 刷新当前章节视频列表，更新审核状态与意见
        refreshSelectedChapterVideoList(chapterId)
        
        // 重新获取章节信息以更新审核状态
        if (currentEditCourseId.value) {
          getCourseInfoByList(currentEditCourseId.value).then(response => {
            const courseData = response.data
            let chapterList = courseData.courseChapterInfoList || []
            chapterList = chapterList.filter(chapter => {
              return chapter.delFlag === undefined || chapter.delFlag === null || chapter.delFlag === 0 || chapter.delFlag === '0'
            })
            
            // 更新章节列表
            editChapterList.value = chapterList
          
            // 更新当前选中章节的视频列表
            const updatedChapter = chapterList.find(ch => ch.chapterId === selectedChapter.value.chapterId)
            if (updatedChapter) {
              selectedChapter.value = updatedChapter
              if (!selectedChapter.value.courseVideoInfoList) {
                selectedChapter.value.courseVideoInfoList = []
              }
            }
          }).catch(() => {
            // 如果重新获取失败，不影响提交成功的提示
          })
        }
      })
      .catch(error => {
        modal.msgError(`提交审核失败：${error.message || '未知错误'}`)
        console.error('提交审核失败:', error)
      })
  }).catch(() => {
    // 用户取消
  })
}

/** 添加章节 */
function handleAddChapter() {
  if (!form.value.courseChapterInfoList) {
    form.value.courseChapterInfoList = []
  }
  // 计算下一个序号（只统计未删除的章节）
  const validChapters = form.value.courseChapterInfoList.filter(c => {
    return c.delFlag === undefined || c.delFlag === null || c.delFlag === 0 || c.delFlag === '0'
  })
  const nextNum = validChapters.length > 0 
    ? Math.max(...validChapters.map(c => parseInt(c.chapterNum) || 0)) + 1
    : 1
  form.value.courseChapterInfoList.push({
    chapterId: null,
    chapterName: '',
    chapterNum: String(nextNum),
    courseId: form.value.courseId,
    description: '',
    creditHour: '',
    isFree: 'Y',
    delFlag: '0'  // 新增章节时，delFlag 默认为 '0'（未删除，字符串类型）
  })
}

/** 上移章节 */
function moveChapterUp(chapter) {
  // 获取过滤后的章节列表
  const validChapters = filteredChapterList.value
  const currentIndex = validChapters.findIndex(c => {
    if (chapter.chapterId) {
      return c.chapterId === chapter.chapterId
    }
    return c === chapter
  })
  
  if (currentIndex > 0) {
    // 找到原列表中的索引
    const prevChapter = validChapters[currentIndex - 1]
    const currentIndexInFull = form.value.courseChapterInfoList.findIndex(c => {
      if (chapter.chapterId) {
        return c.chapterId === chapter.chapterId
      }
      return c === chapter
    })
    const prevIndexInFull = form.value.courseChapterInfoList.findIndex(c => {
      if (prevChapter.chapterId) {
        return c.chapterId === prevChapter.chapterId
      }
      return c === prevChapter
    })
    
    if (currentIndexInFull >= 0 && prevIndexInFull >= 0) {
      // 交换位置
      const temp = form.value.courseChapterInfoList[currentIndexInFull]
      form.value.courseChapterInfoList[currentIndexInFull] = form.value.courseChapterInfoList[prevIndexInFull]
      form.value.courseChapterInfoList[prevIndexInFull] = temp
      
      // 更新序号（只对未删除的章节重新排序）
      let validIndex = 0
      form.value.courseChapterInfoList.forEach((ch) => {
        if (ch.delFlag === undefined || ch.delFlag === null || ch.delFlag === 0 || ch.delFlag === '0') {
          ch.chapterNum = String(validIndex + 1)
          validIndex++
        }
      })
    }
  }
}

/** 下移章节 */
function moveChapterDown(chapter) {
  // 获取过滤后的章节列表
  const validChapters = filteredChapterList.value
  const currentIndex = validChapters.findIndex(c => {
    if (chapter.chapterId) {
      return c.chapterId === chapter.chapterId
    }
    return c === chapter
  })
  
  if (currentIndex < validChapters.length - 1) {
    // 找到原列表中的索引
    const nextChapter = validChapters[currentIndex + 1]
    const currentIndexInFull = form.value.courseChapterInfoList.findIndex(c => {
      if (chapter.chapterId) {
        return c.chapterId === chapter.chapterId
      }
      return c === chapter
    })
    const nextIndexInFull = form.value.courseChapterInfoList.findIndex(c => {
      if (nextChapter.chapterId) {
        return c.chapterId === nextChapter.chapterId
      }
      return c === nextChapter
    })
    
    if (currentIndexInFull >= 0 && nextIndexInFull >= 0) {
      // 交换位置
      const temp = form.value.courseChapterInfoList[currentIndexInFull]
      form.value.courseChapterInfoList[currentIndexInFull] = form.value.courseChapterInfoList[nextIndexInFull]
      form.value.courseChapterInfoList[nextIndexInFull] = temp
      
      // 更新序号（只对未删除的章节重新排序）
      let validIndex = 0
      form.value.courseChapterInfoList.forEach((ch) => {
        if (ch.delFlag === undefined || ch.delFlag === null || ch.delFlag === 0 || ch.delFlag === '0') {
          ch.chapterNum = String(validIndex + 1)
          validIndex++
        }
      })
    }
  }
}

/** 删除章节（逻辑删除，设置delFlag=2） */
function handleRemoveChapter(chapter) {
  // 找到章节在原列表中的索引
  const index = form.value.courseChapterInfoList.findIndex(c => {
    // 通过 chapterId 或对象引用来查找
    if (chapter.chapterId) {
      return c.chapterId === chapter.chapterId
    }
    return c === chapter
  })
  
  if (index >= 0) {
    // 设置 delFlag='2' 表示已删除（逻辑删除，不是物理删除）
    // 使用字符串 '2' 因为数据库字段为 char(1) 类型
    form.value.courseChapterInfoList[index].delFlag = '2'
    
    // 重新排序（只对未删除的章节重新排序）
    let validIndex = 0
    form.value.courseChapterInfoList.forEach((ch) => {
      // 只对未删除的章节重新排序（delFlag 为 undefined、null、0 或 '0'）
      if (ch.delFlag === undefined || ch.delFlag === null || ch.delFlag === 0 || ch.delFlag === '0') {
        ch.chapterNum = String(validIndex + 1)
        validIndex++
      }
    })
  }
}



/** 批量审核 */
function handleBatchAudit() {
  if (ids.value.length === 0) {
    modal.msgWarning("请选择要审核的课程")
    return
  }
  auditForm.value = {
    courseIds: ids.value,
    checkStatus: '4',
    remark: ''
  }
  auditOpen.value = true
}

/** 提交批量审核 */
function submitBatchAudit() {
  if (auditForm.value.courseIds && auditForm.value.courseIds.length > 0) {
    // 批量审核：循环调用单个审核接口
    const promises = auditForm.value.courseIds.map(courseId => {
      return updateCourseStatus({
        pageId: courseId,
        checkStatus: auditForm.value.checkStatus
      })
    })
    Promise.all(promises).then(() => {
      modal.msgSuccess("批量审核成功")
      auditOpen.value = false
      getList()
    }).catch(() => {
      modal.msgError("批量审核失败")
    })
  }
}

getList()
getClassifyList()

// 文件上传
const coverImageList = ref([])
const uploadFileUrl = ref(import.meta.env.VITE_APP_BASE_API + "/file/upload")
const uploadVideoUrl = ref(import.meta.env.VITE_APP_BASE_API + "/file/uploadVideo")
const headers = ref({ Authorization: "Bearer " + getToken() })

function handleUploadError(err) {
  modal.msgError('上传文件失败')
}

// 超出文件数量限制回调
function handleExceed(files, fileList) {
  modal.msgWarning('最多上传1个文件')
}

// 上传前验证文件格式和大小
function beforeUploadCover(file) {
  // 允许的图片格式
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png']
  const allowedExtensions = ['jpg', 'jpeg', 'png']
  
  // 检查文件类型
  const fileExtension = file.name.split('.').pop().toLowerCase()
  const isTypeOk = allowedTypes.includes(file.type) || allowedExtensions.includes(fileExtension)
  
  if (!isTypeOk) {
    modal.msgError('上传封面图只能是 JPG、JPEG、PNG 格式!')
    return false
  }
  
  // 检查文件大小（5MB）
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    modal.msgError('上传封面图大小不能超过 5MB!')
    return false
  }
  
  return true
}

// 上传成功回调
function handleUploadSuccess(res, file) {
  res = replaceFileOrigin(res)
  coverImageList.value = []
  coverImageList.value = [{
    name: res.data.name,
    url: res.data.url
  }]
  form.value.coverImage = res.data.url
}

function handleRemove(res) {
  coverImageList.value = []
  form.value.coverImage = null
}

const previewurl = (file) => {
  window.open(file.url, '_blank')
}
</script>

<style scoped>
.upload-file-uploader {
  margin-bottom: 10px;
}

:deep(.el-form-item__label) {
  white-space: nowrap;
}

/* 页面布局样式 */
.page-layout {
  min-height: 600px;
}

/* 左侧树形图容器 */
.tree-container {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  min-height: 600px;
  max-height: calc(100vh - 120px);
  overflow: auto;
}

.tree-header {
  padding: 10px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 10px;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.classify-tree {
  background: transparent;
}

.classify-tree :deep(.el-tree-node) {
  margin: 0;
}

.classify-tree :deep(.el-tree-node__content) {
  height: 40px;
  line-height: 40px;
  padding-left: 8px !important;
  border-radius: 4px;
  margin-bottom: 2px;
  transition: all 0.3s;
}

/* 增加子节点的缩进，让层级关系更明显 */
.classify-tree :deep(.el-tree-node__children .el-tree-node__content) {
  padding-left: 38px !important;
}

/* 第三级及更深层级的节点 */
.classify-tree :deep(.el-tree-node__children .el-tree-node__children .el-tree-node__content) {
  padding-left: 68px !important;
}

/* 第四级节点 */
.classify-tree :deep(.el-tree-node__children .el-tree-node__children .el-tree-node__children .el-tree-node__content) {
  padding-left: 98px !important;
}

.classify-tree :deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

.classify-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.classify-tree :deep(.el-tree-node__expand-icon) {
  color: #606266;
  font-size: 14px;
  padding: 0 4px;
  transition: transform 0.3s;
}

.classify-tree :deep(.el-tree-node__expand-icon.is-leaf) {
  color: transparent;
  cursor: default;
}

.classify-tree :deep(.el-tree-node__expand-icon.expanded) {
  transform: rotate(90deg);
}

.classify-tree :deep(.el-icon) {
  font-size: 14px;
}

.tree-node {
  display: flex;
  align-items: center;
  width: 100%;
  font-size: 14px;
  flex: 1;
}

.tree-node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 搜索框样式 */
.tree-search {
  margin-bottom: 10px;
}

.tree-search :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.tree-search :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.tree-search :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

/* 右侧内容区 */
.content-container {
  padding-left: 0;
}

.search-form {
  background: #fff;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 10px;
}

/* 分类树形选择器样式 */
.classify-select-tree {
  max-height: 300px;
  overflow-y: auto;
}

.classify-select-tree :deep(.el-tree-node__content) {
  height: 36px;
  line-height: 36px;
  padding-left: 8px !important;
  border-radius: 4px;
  margin-bottom: 2px;
  transition: all 0.3s;
}

.classify-select-tree :deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

.classify-select-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

/* 增加子节点的缩进 */
.classify-select-tree :deep(.el-tree-node__children .el-tree-node__content) {
  padding-left: 38px !important;
}

.classify-select-tree :deep(.el-tree-node__children .el-tree-node__children .el-tree-node__content) {
  padding-left: 68px !important;
}

.classify-select-tree :deep(.el-tree-node__children .el-tree-node__children .el-tree-node__children .el-tree-node__content) {
  padding-left: 98px !important;
}

.tree-select-node {
  display: flex;
  align-items: center;
  width: 100%;
  font-size: 14px;
  flex: 1;
}

.tree-select-node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 树形选择器弹出层样式 */
:deep(.classify-tree-select-popover) {
  padding: 8px;
}

/* 视频编辑对话框样式 */
.chapter-list-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.chapter-list-header {
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  flex-shrink: 0;
}

/* 章节列表表格样式 */
.chapter-list-container :deep(.el-table) {
  cursor: pointer;
}

/* 章节列表行悬停样式 */
.chapter-list-container :deep(.el-table__body tr:hover > td) {
  background-color: #f0f9ff !important;
  cursor: pointer;
}

/* 章节列表选中行样式 */
.chapter-list-container :deep(.el-table__body tr.current-row > td) {
  background-color: #e1f3ff !important;
  border-left: 3px solid #409eff;
  font-weight: 500;
}

.chapter-list-container :deep(.el-table__body tr.current-row:hover > td) {
  background-color: #d4edff !important;
}

.video-list-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.video-list-header {
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 视频列表表格样式 - 放大字体 */
.video-list-container :deep(.el-table) {
  font-size: 14px;
}

.video-list-container :deep(.el-table .el-input__inner) {
  font-size: 14px;
}

.video-list-container :deep(.el-table .el-input__wrapper) {
  font-size: 14px;
}

.video-list-container :deep(.el-table .el-select .el-input__inner) {
  font-size: 14px;
}

/* 必填字段样式 - 当字段为空时高亮显示 */
.video-list-container :deep(.required-field .el-input__wrapper) {
  border-color: #f56c6c;
  box-shadow: 0 0 0 1px #f56c6c inset;
}

.video-list-container :deep(.required-field.el-input-number) {
  border-color: #f56c6c;
}

.video-list-container :deep(.required-field.el-input-number .el-input__wrapper) {
  border-color: #f56c6c;
  box-shadow: 0 0 0 1px #f56c6c inset;
}

.video-list-container :deep(.required-field.el-select .el-input__wrapper) {
  border-color: #f56c6c;
  box-shadow: 0 0 0 1px #f56c6c inset;
}

/* 详情对话框样式 */
.detail-header {
  margin-bottom: 20px;
}

.cover-image-container {
  width: 100%;
  height: 250px;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image-container .no-image {
  color: #909399;
  font-size: 14px;
}

.course-info {
  padding: 0 10px;
}

.course-name {
  margin: 0 0 10px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.course-description {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
}

.info-grid {
  margin-top: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}

.info-item .label {
  font-weight: 600;
  color: #303133;
  margin-right: 8px;
  min-width: 80px;
}

.info-item .value {
  color: #606266;
}

.chapter-directory {
  margin-top: 20px;
}

.section-title {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.no-video {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

:deep(.el-collapse-item__header) {
  background-color: #f5f7fa;
  font-weight: 600;
}

:deep(.el-collapse-item__content) {
  padding: 15px 20px;
}
</style>
