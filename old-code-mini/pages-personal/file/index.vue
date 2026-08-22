<template>
  <view class="page">
    <view class="summary">
      <view>
        <text class="summary-title">我的文件</text>
        <text class="summary-desc">查看资料并按任务要求提交文件</text>
      </view>
      <view class="summary-count">{{ tasks.length }}<text>项任务</text></view>
    </view>

    <view
      v-for="task in tasks"
      :key="task.id"
      class="task-card"
      :class="{ 'has-notification': notificationCount(task) > 0 }"
    >
      <view class="task-head" @click="toggleTask(task)">
        <view class="task-title-wrap">
          <view v-if="!task.readCountFlag" class="unread-dot"></view>
          <text class="task-title">{{ task.taskName || '文件任务' }}</text>
        </view>
        <view class="task-head-actions">
          <text v-if="notificationCount(task) > 0" class="notification-badge">
            通知 {{ formatNotificationCount(task.notificationCount) }}
          </text>
          <text class="expand-icon" :class="{ expanded: isExpanded(task.id) }">›</text>
        </view>
      </view>

      <view v-if="isExpanded(task.id)" class="task-content">
        <view v-if="notificationCount(task) > 0" class="notification-panel">
          <view class="notification-panel-head">
            <text>任务通知</text>
            <text>共 {{ formatNotificationCount(task.notificationCount) }} 条</text>
          </view>
          <view v-if="notificationLoading[task.id]" class="notification-state">通知加载中...</view>
          <template v-else>
            <view
              v-for="notice in notificationLists[task.id] || []"
              :key="notice.notificationId || notice.id"
              class="notification-row"
              @click.stop="openNotification(task, notice)"
            >
              <text class="notification-title">{{ notice.title }}</text>
              <text v-if="notice.sendTime" class="notification-time">{{ notice.sendTime }}</text>
            </view>
            <view v-if="!(notificationLists[task.id] || []).length" class="notification-state">
              暂无有效通知
            </view>
          </template>
        </view>

        <view
          v-for="section in task.fileTaskConfigList || []"
          :key="section.id"
          class="section-card"
          :class="{ upload: isUploadTask(section) }"
        >
          <view class="section-head">
            <view class="section-icon" :class="isUploadTask(section) ? 'upload-icon' : 'download-icon'">
              {{ isUploadTask(section) ? '传' : '下' }}
            </view>
            <view class="section-main">
              <text class="section-title">{{ section.fileName || (isUploadTask(section) ? '文件上传' : '资料下载') }}</text>
              <text class="time-status" :class="{ expired: sectionStatus(section).disabled }">{{ sectionStatus(section).text }}</text>
            </view>
            <button
              v-if="isUploadTask(section)"
              class="action-btn upload-btn"
              :disabled="sectionStatus(section).disabled || isOperating(section.id)"
              @click.stop="chooseAndUpload(section, task)"
            >
              {{ uploadButtonText(section) }}
            </button>
            <button
              v-else
              class="action-btn download-btn"
              :disabled="sectionStatus(section).disabled || isOperating(section.id)"
              @click.stop="downloadTaskFile(section, task)"
            >
              {{ isOperating(section.id) ? '下载中' : '下载' }}
            </button>
          </view>

          <view v-if="isUploadTask(section)" class="limit-box">
            <text>格式限制：{{ section.fileType || '不限' }}</text>
            <text>大小限制：{{ formatFileSizeLimit(section.fileSize) }}</text>
          </view>

          <view v-if="section.annoucement" class="announcement">
            <text class="announcement-label">{{ isUploadTask(section) ? '上传须知' : '文件说明' }}</text>
            <text class="announcement-text">{{ section.annoucement }}</text>
          </view>

          <view v-if="isUploadTask(section) && section.tempFile" class="file-row template-row" @click="downloadTemplate(section)">
            <view class="file-mark">模</view>
            <view class="file-info">
              <text class="file-name">{{ section.tempFileName || '下载模板' }}</text>
              <text class="file-state">任务模板</text>
            </view>
            <text class="file-action">下载</text>
          </view>

          <view v-if="uploadProgress[section.id] != null" class="progress-wrap">
            <view class="progress-track"><view class="progress-bar" :style="{ width: `${uploadProgress[section.id]}%` }"></view></view>
            <text>{{ uploadProgress[section.id] }}%</text>
          </view>

          <view
            v-for="(file, fileIndex) in uploadedFiles(section)"
            :key="`${file.fileName}-${fileIndex}`"
            class="file-row uploaded-row"
          >
            <view class="file-mark uploaded">文</view>
            <view class="file-info" @click="downloadUploadedFile(file, section)">
              <text class="file-name">{{ file.fileName || '已上传文件' }}</text>
              <text class="file-state success">已上传</text>
            </view>
            <view class="file-buttons">
              <text class="file-action" @click="downloadUploadedFile(file, section)">下载</text>
              <text
                v-if="!sectionStatus(section).disabled"
                class="file-action delete"
                @click="confirmDelete(section, task)"
              >删除</text>
            </view>
          </view>
        </view>

        <view v-if="!(task.fileTaskConfigList || []).length" class="task-empty">暂无文件配置</view>
      </view>
    </view>

    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="!tasks.length" class="state">暂无文件任务</view>

    <uni-popup
      ref="notificationPopup"
      type="center"
      :is-mask-click="true"
      @change="handleNotificationPopupChange"
    >
      <view class="notification-popup">
        <view class="notification-popup-head">
          <text class="notification-popup-title">{{ notificationDetail.title || '通知详情' }}</text>
          <text class="notification-popup-close" @click.stop="closeNotification">×</text>
        </view>
        <text v-if="notificationDetail.sendTime" class="notification-popup-time">
          发布时间：{{ notificationDetail.sendTime }}
        </text>
        <scroll-view scroll-y class="notification-popup-scroll">
          <view v-if="notificationDetailLoading" class="notification-popup-state">通知加载中...</view>
          <rich-text
            v-else-if="notificationDetail.content"
            class="notification-rich-text"
            :nodes="notificationDetail.content"
          />
          <view v-else class="notification-popup-state">暂无通知内容</view>
        </scroll-view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad, onPullDownRefresh, onUnload } from '@dcloudio/uni-app'
import config from '@/config'
import { getToken } from '@/utils/auth'
import {
  getFileTaskNotificationDetail,
  getFilePresignedUrl,
  getSystemTime,
  listFileTaskNotifications,
  listMyFileTasks,
  markFileTaskRead,
  recordMyFileDownload,
  saveMyFileTaskSubmission,
  saveMyFileUploadRecord
} from '@/api/myFile'

const tasks = ref([])
const expandedIds = ref([])
const loading = ref(false)
const now = ref(Date.now())
const uploadProgress = reactive({})
const operating = reactive({})
const notificationLists = reactive({})
const notificationLoading = reactive({})
const notificationPopup = ref(null)
const notificationDetail = ref({})
const notificationDetailLoading = ref(false)
const activeNotificationTaskId = ref(null)
let clockTimer = null
let refreshTimer = null
let serverBaseTime = Date.now()
let localBaseTime = Date.now()

onLoad(async () => {
  await syncServerTime()
  startClock()
  startAutoRefresh()
  await loadTasks()
})

onPullDownRefresh(async () => {
  try {
    await syncServerTime()
    await loadTasks()
  } finally {
    uni.stopPullDownRefresh()
  }
})

onUnload(() => {
  if (clockTimer) clearInterval(clockTimer)
  if (refreshTimer) clearInterval(refreshTimer)
})

async function syncServerTime() {
  try {
    const res = await getSystemTime()
    serverBaseTime = Number(res?.data || Date.now())
  } catch (error) {
    serverBaseTime = Date.now()
  }
  localBaseTime = Date.now()
  now.value = serverBaseTime
}

function startClock() {
  if (clockTimer) clearInterval(clockTimer)
  clockTimer = setInterval(() => {
    now.value = serverBaseTime + Date.now() - localBaseTime
  }, 1000)
}

function startAutoRefresh() {
  if (refreshTimer) clearInterval(refreshTimer)
  refreshTimer = setInterval(() => loadTasks(), 300000)
}

async function loadTasks() {
  if (loading.value) return
  loading.value = true
  try {
    const res = await listMyFileTasks()
    const rows = Array.isArray(res?.data) ? res.data : []
    tasks.value = rows
    const currentTaskIds = new Set(rows.map(task => String(task.id)))
    Object.keys(notificationLists).forEach(taskId => {
      if (!currentTaskIds.has(String(taskId))) delete notificationLists[taskId]
    })
    const validIds = expandedIds.value.filter(id => rows.some(item => item.id === id))
    expandedIds.value = validIds.length ? validIds : (rows[0] ? [rows[0].id] : [])
    await Promise.all(rows.map(task => {
      if (!notificationCount(task)) {
        notificationLists[task.id] = []
        return Promise.resolve()
      }
      return expandedIds.value.includes(task.id)
        ? loadTaskNotifications(task, true)
        : Promise.resolve()
    }))
    if (activeNotificationTaskId.value) {
      if (!currentTaskIds.has(String(activeNotificationTaskId.value))) {
        closeNotification()
      } else {
        const activeNotificationId = notificationDetail.value.notificationId || notificationDetail.value.id
        const activeList = notificationLists[activeNotificationTaskId.value] || []
        if (
          activeNotificationId &&
          !activeList.some(notice => String(notice.notificationId || notice.id) === String(activeNotificationId))
        ) {
          closeNotification()
        }
      }
    }
    const firstExpanded = rows.find(item => expandedIds.value.includes(item.id))
    if (firstExpanded && !firstExpanded.readCountFlag) await setTaskRead(firstExpanded)
  } catch (error) {
    // 请求层已统一提示错误，保留页面现有数据。
  } finally {
    loading.value = false
  }
}

function isExpanded(id) {
  return expandedIds.value.includes(id)
}

async function toggleTask(task) {
  if (isExpanded(task.id)) {
    expandedIds.value = expandedIds.value.filter(id => id !== task.id)
  } else {
    expandedIds.value = expandedIds.value.concat(task.id)
    await loadTaskNotifications(task, true)
    if (!task.readCountFlag) await setTaskRead(task)
  }
}

function notificationCount(task) {
  const count = Number(task?.notificationCount || 0)
  return Number.isFinite(count) && count > 0 ? Math.floor(count) : 0
}

function formatNotificationCount(count) {
  const normalized = Number(count || 0)
  return normalized > 99 ? '99+' : normalized
}

async function loadTaskNotifications(task, force = false) {
  const taskId = task?.id
  if (!taskId) return
  if (!notificationCount(task)) {
    notificationLists[taskId] = []
    return
  }
  if (!force && Object.prototype.hasOwnProperty.call(notificationLists, taskId)) return
  if (notificationLoading[taskId]) return

  notificationLoading[taskId] = true
  try {
    const res = await listFileTaskNotifications(taskId)
    notificationLists[taskId] = Array.isArray(res?.data)
      ? res.data
      : Array.isArray(res?.data?.rows)
        ? res.data.rows
        : Array.isArray(res?.rows)
          ? res.rows
          : []
  } catch (error) {
    // 请求层已统一提示，保留上一次成功加载的通知列表。
  } finally {
    notificationLoading[taskId] = false
  }
}

async function openNotification(task, notice) {
  const notificationId = notice?.notificationId || notice?.id
  if (!task?.id || !notificationId) return

  activeNotificationTaskId.value = task.id
  notificationDetail.value = {
    notificationId,
    title: notice.title || '通知详情',
    sendTime: notice.sendTime,
    content: ''
  }
  notificationDetailLoading.value = true
  notificationPopup.value?.open()
  try {
    const res = await getFileTaskNotificationDetail(task.id, notificationId)
    notificationDetail.value = res?.data || notificationDetail.value
  } catch (error) {
    closeNotification()
  } finally {
    notificationDetailLoading.value = false
  }
}

function closeNotification() {
  notificationPopup.value?.close()
  activeNotificationTaskId.value = null
  notificationDetail.value = {}
  notificationDetailLoading.value = false
}

function handleNotificationPopupChange(event) {
  if (event?.show) return
  activeNotificationTaskId.value = null
  notificationDetail.value = {}
  notificationDetailLoading.value = false
}

async function setTaskRead(task) {
  task.readCountFlag = true
  try {
    await markFileTaskRead(task.id)
  } catch (error) {
    task.readCountFlag = false
  }
}

function isUploadTask(section) {
  return String(section?.taskType) === '1'
}

function sectionStatus(section) {
  if (section?.perminate) return { text: '永久有效', disabled: false }
  const start = parseDate(section?.uploadStart)
  const end = parseDate(section?.uploadEnd)
  if (start && now.value < start) return { text: `距离开始还有${formatDuration(start - now.value)}`, disabled: true }
  if (end && now.value >= end) return { text: '截止时间已到期', disabled: true }
  if (end) return { text: isUploadTask(section) ? `距离截止还有${formatDuration(end - now.value)}` : `${formatDuration(end - now.value)}后失效`, disabled: false }
  return { text: '有效期内', disabled: false }
}

function parseDate(value) {
  if (!value) return 0
  if (typeof value === 'number') return value
  const parsed = new Date(String(value).replace(/-/g, '/')).getTime()
  return Number.isNaN(parsed) ? 0 : parsed
}

function formatDuration(value) {
  const seconds = Math.max(0, Math.ceil(Number(value || 0) / 1000))
  const days = Math.floor(seconds / 86400)
  if (days >= 1) return `${days}天`
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainSeconds = seconds % 60
  const parts = []
  if (hours) parts.push(`${hours}小时`)
  if (minutes) parts.push(`${minutes}分钟`)
  if (remainSeconds || !parts.length) parts.push(`${remainSeconds}秒`)
  return parts.join('')
}

function safeParseFiles(fileInfo) {
  if (!fileInfo) return []
  if (Array.isArray(fileInfo)) return fileInfo
  try {
    const parsed = JSON.parse(fileInfo)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return []
  }
}

function uploadedFiles(section) {
  return safeParseFiles(section?.fileUploadRecord?.fileInfo)
}

function uploadButtonText(section) {
  if (isOperating(section.id)) return uploadProgress[section.id] != null ? '上传中' : '处理中'
  return uploadedFiles(section).length ? '重新上传' : '上传'
}

function isOperating(id) {
  return Boolean(operating[id])
}

async function chooseAndUpload(section, task) {
  if (sectionStatus(section).disabled || isOperating(section.id)) return
  const file = await chooseFile(section)
  if (!file) return
  const validationMessage = validateFile(file, section)
  if (validationMessage) {
    uni.showToast({ title: validationMessage, icon: 'none' })
    return
  }
  operating[section.id] = true
  uploadProgress[section.id] = 0
  try {
    const objectKey = await uploadToOss(file, section)
    const fileInfo = JSON.stringify([{ fileName: file.name, downloadLink: objectKey }])
    const totalSize = (Number(file.size || 0) / 1024 / 1024).toFixed(5)
    await saveMyFileUploadRecord({
      fileTaskId: section.id,
      fileTaskName: section.fileName,
      uploadTime: section.uploadEnd,
      totalSize,
      id: section.fileUploadRecord?.id,
      fileInfo,
      sysUserGroupCompetitionRelationList: task.sysUserGroupCompetitionRelationList
    })
    await submitTaskFiles(task, section.id, { fileInfo, totalSize })
    uni.showToast({ title: uploadedFiles(section).length ? '重新上传成功' : '上传成功', icon: 'success' })
    await loadTasks()
  } catch (error) {
    // 上传接口或请求层已给出错误提示。
  } finally {
    delete operating[section.id]
    delete uploadProgress[section.id]
  }
}

function chooseFile(section) {
  const extensions = allowedExtensions(section.fileType)
  return new Promise(resolve => {
    const options = {
      count: 1,
      type: 'file',
      success: res => resolve(res.tempFiles?.[0] || null),
      fail: () => resolve(null)
    }
    if (extensions.length) options.extension = extensions
    uni.chooseMessageFile(options)
  })
}

function allowedExtensions(fileType) {
  const aliases = {
    excel: ['xls', 'xlsx'], word: ['doc', 'docx'], ppt: ['ppt', 'pptx'],
    image: ['jpg', 'jpeg', 'png'], photo: ['jpg', 'jpeg', 'png']
  }
  return String(fileType || '')
    .split(/[,，、/\s]+/)
    .map(value => value.trim().toLowerCase().replace(/^\./, ''))
    .filter(Boolean)
    .reduce((result, value) => result.concat(aliases[value] || value), [])
    .filter((value, index, array) => array.indexOf(value) === index)
}

function validateFile(file, section) {
  const name = file.name || file.path?.split('/').pop() || ''
  file.name = name
  const extension = name.includes('.') ? name.split('.').pop().toLowerCase() : ''
  const extensions = allowedExtensions(section.fileType)
  if (extensions.length && !extensions.includes(extension)) return `只允许上传${section.fileType}格式的文件`
  const maxBytes = getMaxFileBytes(section.fileSize)
  if (maxBytes && Number(file.size || 0) > maxBytes) return `文件大小不能超过${formatFileSizeLimit(section.fileSize).replace('≤', '')}`
  return ''
}

function formatFileSizeLimit(value) {
  if (value === '' || value == null) return '不限'
  const text = String(value).trim()
  return `≤${/[a-zA-Z]/.test(text) ? text : `${text}MB`}`
}

function getMaxFileBytes(value) {
  if (value === '' || value == null) return 0
  const text = String(value).trim()
  const amount = Number.parseFloat(text)
  if (!Number.isFinite(amount) || amount <= 0) return 0
  const unit = text.replace(/[\d.\s]/g, '').toUpperCase()
  if (unit === 'KB') return amount * 1024
  if (unit === 'GB') return amount * 1024 * 1024 * 1024
  return amount * 1024 * 1024
}

function uploadToOss(file, section) {
  return new Promise((resolve, reject) => {
    const task = uni.uploadFile({
      url: `${config.baseUrl}/file/oss/upload`,
      filePath: file.path,
      name: 'file',
      header: { Authorization: `Bearer ${getToken()}`, tjPlatformType: 'miniProgram' },
      formData: { bizSign: 'race', bizCode: String(section.id) },
      timeout: 120000,
      success: response => {
        let body
        try { body = JSON.parse(response.data) } catch (error) { body = null }
        if (response.statusCode === 200 && Number(body?.code) === 200 && body?.data) resolve(body.data)
        else {
          uni.showToast({ title: body?.msg || '文件上传失败', icon: 'none' })
          reject(new Error(body?.msg || '文件上传失败'))
        }
      },
      fail: error => {
        uni.showToast({ title: '文件上传失败', icon: 'none' })
        reject(error)
      }
    })
    if (task?.onProgressUpdate) task.onProgressUpdate(progress => { uploadProgress[section.id] = progress.progress })
  })
}

function buildTaskFileList(task, changedSectionId, replacement) {
  const result = []
  ;(task.fileTaskConfigList || []).forEach(section => {
    if (!isUploadTask(section)) return
    if (section.id === changedSectionId) {
      if (replacement) result.push(replacement)
      return
    }
    if (uploadedFiles(section).length) {
      result.push({
        fileInfo: section.fileUploadRecord.fileInfo,
        totalSize: section.fileUploadRecord.totalSize || '0'
      })
    }
  })
  return result
}

function submitTaskFiles(task, changedSectionId, replacement) {
  return saveMyFileTaskSubmission({
    id: task.id,
    taskName: task.taskName,
    submitStatus: true,
    sysUserGroupCompetitionRelationList: task.sysUserGroupCompetitionRelationList,
    fileUploadManagerList: buildTaskFileList(task, changedSectionId, replacement)
  })
}

function confirmDelete(section, task) {
  if (isOperating(section.id)) return
  uni.showModal({
    title: '删除文件',
    content: '确定要删除已上传的文件吗？',
    confirmColor: '#e05248',
    success: async res => {
      if (!res.confirm) return
      operating[section.id] = true
      try {
        await saveMyFileUploadRecord({
          fileTaskId: section.id,
          fileTaskName: section.fileName,
          uploadTime: section.uploadEnd,
          totalSize: 0,
          id: section.fileUploadRecord?.id,
          uploadOperationType: 'delete',
          fileInfo: section.fileUploadRecord?.fileInfo,
          sysUserGroupCompetitionRelationList: task.sysUserGroupCompetitionRelationList
        })
        await submitTaskFiles(task, section.id, null)
        uni.showToast({ title: '删除成功', icon: 'success' })
        await loadTasks()
      } catch (error) {
        // 请求层已统一提示错误。
      } finally {
        delete operating[section.id]
      }
    }
  })
}

async function downloadTemplate(section) {
  if (!section.tempFile) return
  try {
    await downloadObject(section.tempFile, section.tempFileName || section.fileName)
  } catch (error) {
    // 下载方法已给出错误提示。
  }
}

async function downloadUploadedFile(file, section) {
  if (!file?.downloadLink || isOperating(section.id)) return
  operating[section.id] = true
  try {
    await downloadObject(file.downloadLink, file.fileName)
  } catch (error) {
    // 下载方法已给出错误提示。
  } finally {
    delete operating[section.id]
  }
}

async function downloadTaskFile(section, task) {
  if (!section.tempFile || sectionStatus(section).disabled || isOperating(section.id)) return
  operating[section.id] = true
  try {
    await downloadObject(section.tempFile, section.tempFileName || section.fileName)
    recordMyFileDownload({
      taskId: task.id,
      fileName: section.tempFileName,
      fileTaskId: section.taskId,
      fileTaskName: section.fileName
    }).catch(() => {})
  } catch (error) {
    // 下载方法已给出错误提示。
  } finally {
    delete operating[section.id]
  }
}

async function downloadObject(fileKey, fileName) {
  try {
    const res = await getFilePresignedUrl(fileKey)
    const url = res?.data
    if (!url) throw new Error('下载链接无效')
    uni.showLoading({ title: '文件下载中' })
    const result = await new Promise((resolve, reject) => {
      uni.downloadFile({ url, success: resolve, fail: reject })
    })
    if (result.statusCode !== 200) throw new Error('文件下载失败')
    await openDownloadedFile(result.tempFilePath, fileName, url)
  } catch (error) {
    uni.showToast({ title: error?.message === '下载链接无效' ? '下载链接无效' : '文件下载失败', icon: 'none' })
    throw error
  } finally {
    uni.hideLoading()
  }
}

function openDownloadedFile(filePath, fileName, sourceUrl) {
  const extension = fileExtension(fileName || sourceUrl)
  if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(extension)) {
    return new Promise(resolve => uni.previewImage({ urls: [filePath], complete: resolve }))
  }
  if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf'].includes(extension)) {
    return new Promise(resolve => {
      uni.openDocument({
        filePath,
        fileType: extension,
        showMenu: true,
        fail: () => copyDownloadLink(sourceUrl),
        complete: resolve
      })
    })
  }
  return shareOrCopyFile(filePath, fileName, sourceUrl)
}

function shareOrCopyFile(filePath, fileName, sourceUrl) {
  return new Promise(resolve => {
    // #ifdef MP-WEIXIN
    if (typeof wx !== 'undefined' && wx.shareFileMessage) {
      wx.shareFileMessage({ filePath, fileName: fileName || '文件', fail: () => copyDownloadLink(sourceUrl), complete: resolve })
      return
    }
    // #endif
    copyDownloadLink(sourceUrl)
    resolve()
  })
}

function copyDownloadLink(url) {
  uni.setClipboardData({ data: url, success: () => uni.showToast({ title: '下载地址已复制', icon: 'none' }) })
}

function fileExtension(value) {
  return String(value || '').split('?')[0].split('.').pop().toLowerCase()
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: 24rpx 28rpx 70rpx; background: #f5f7fb; box-sizing: border-box; }
.summary { display: flex; align-items: center; justify-content: space-between; padding: 28rpx; border-radius: 22rpx; color: #fff; background: linear-gradient(135deg, #3169f8, #5b8ff9); }
.summary-title, .summary-desc { display: block; }
.summary-title { font-size: 32rpx; font-weight: 700; }
.summary-desc { margin-top: 8rpx; color: rgba(255,255,255,.82); font-size: 22rpx; }
.summary-count { min-width: 94rpx; text-align: center; font-size: 34rpx; font-weight: 700; }
.summary-count text { display: block; margin-top: 2rpx; color: rgba(255,255,255,.78); font-size: 19rpx; font-weight: 400; }
.task-card { margin-top: 22rpx; overflow: hidden; border: 2rpx solid transparent; border-radius: 22rpx; background: #fff; box-shadow: 0 6rpx 20rpx rgba(24,52,110,.05); transition: border-color .2s, background-color .2s; }
.task-card.has-notification { border-color: #efb74a; background: #fffaf0; box-shadow: 0 8rpx 24rpx rgba(217,145,22,.14); }
.task-head { display: flex; align-items: center; justify-content: space-between; min-height: 94rpx; padding: 0 25rpx; }
.task-title-wrap { display: flex; flex: 1; align-items: center; min-width: 0; }
.unread-dot { flex: 0 0 13rpx; width: 13rpx; height: 13rpx; margin-right: 13rpx; border-radius: 50%; background: #ef5350; }
.task-title { overflow: hidden; color: #28344a; font-size: 28rpx; font-weight: 700; text-overflow: ellipsis; white-space: nowrap; }
.task-head-actions { display: flex; flex: 0 0 auto; align-items: center; margin-left: 14rpx; }
.notification-badge { padding: 5rpx 14rpx; border-radius: 20rpx; color: #9b6100; background: #ffedbc; font-size: 20rpx; font-weight: 600; white-space: nowrap; }
.expand-icon { margin-left: 18rpx; color: #9ba4b3; font-size: 42rpx; transform: rotate(90deg); transition: transform .2s; }
.expand-icon.expanded { transform: rotate(-90deg); }
.task-content { padding: 2rpx 20rpx 22rpx; border-top: 1rpx solid #edf0f5; }
.notification-panel { margin-top: 18rpx; padding: 20rpx 21rpx 10rpx; border: 1rpx solid #f0d79d; border-radius: 17rpx; background: #fffdf7; }
.notification-panel-head { display: flex; align-items: center; justify-content: space-between; padding-bottom: 9rpx; color: #805100; font-size: 23rpx; font-weight: 700; }
.notification-panel-head text:last-child { color: #a98442; font-size: 19rpx; font-weight: 400; }
.notification-row { display: flex; align-items: center; justify-content: space-between; min-height: 68rpx; border-bottom: 1rpx dashed #eadfca; }
.notification-row:last-of-type { border-bottom: 0; }
.notification-title { flex: 1; overflow: hidden; color: #996400; font-size: 22rpx; text-overflow: ellipsis; white-space: nowrap; }
.notification-time { flex: 0 0 auto; margin-left: 18rpx; color: #a0a0a0; font-size: 18rpx; }
.notification-state { padding: 22rpx 0; color: #9ba2ae; text-align: center; font-size: 21rpx; }
.section-card { margin-top: 18rpx; padding: 22rpx; border-radius: 17rpx; background: #f7f9fc; }
.section-card.upload { background: #f8faff; }
.section-head { display: flex; align-items: center; gap: 15rpx; }
.section-icon { flex: 0 0 58rpx; width: 58rpx; height: 58rpx; line-height: 58rpx; border-radius: 17rpx; text-align: center; font-size: 23rpx; font-weight: 700; }
.upload-icon { color: #3169f8; background: #e5edff; }
.download-icon { color: #10a36b; background: #dff6ec; }
.section-main { flex: 1; min-width: 0; }
.section-title, .time-status { display: block; }
.section-title { overflow: hidden; color: #344056; font-size: 25rpx; font-weight: 700; text-overflow: ellipsis; white-space: nowrap; }
.time-status { margin-top: 6rpx; color: #4f8c70; font-size: 19rpx; }
.time-status.expired { color: #9aa3b1; }
button::after { border: none; }
.action-btn { flex: 0 0 126rpx; width: 126rpx; height: 60rpx; line-height: 58rpx; margin: 0; padding: 0; border-radius: 31rpx; font-size: 22rpx; }
.upload-btn { color: #fff; background: #3169f8; }
.download-btn { color: #fff; background: #10a36b; }
.action-btn[disabled] { color: #fff; background: #b6c0cf; }
.limit-box { display: flex; flex-wrap: wrap; gap: 10rpx 24rpx; margin-top: 18rpx; color: #8792a3; font-size: 20rpx; }
.announcement { margin-top: 17rpx; padding: 16rpx 18rpx; border-radius: 12rpx; color: #6b7586; background: #fff; font-size: 21rpx; line-height: 1.55; }
.announcement-label { color: #3f4d63; font-weight: 600; }
.announcement-label::after { content: '：'; }
.announcement-text { word-break: break-all; }
.file-row { display: flex; align-items: center; gap: 14rpx; margin-top: 16rpx; padding: 16rpx; border-radius: 13rpx; background: #fff; }
.file-mark { flex: 0 0 50rpx; width: 50rpx; height: 50rpx; line-height: 50rpx; border-radius: 13rpx; color: #b36a15; background: #fff0d9; text-align: center; font-size: 20rpx; font-weight: 700; }
.file-mark.uploaded { color: #3169f8; background: #e8efff; }
.file-info { flex: 1; min-width: 0; }
.file-name, .file-state { display: block; }
.file-name { overflow: hidden; color: #3c485c; font-size: 22rpx; text-overflow: ellipsis; white-space: nowrap; }
.file-state { margin-top: 4rpx; color: #a36a26; font-size: 18rpx; }
.file-state.success { color: #1a9968; }
.file-buttons { display: flex; gap: 18rpx; }
.file-action { flex: 0 0 auto; color: #3169f8; font-size: 21rpx; }
.file-action.delete { color: #df5148; }
.progress-wrap { display: flex; align-items: center; gap: 14rpx; margin-top: 17rpx; color: #3169f8; font-size: 19rpx; }
.progress-track { flex: 1; height: 10rpx; overflow: hidden; border-radius: 6rpx; background: #e3e9f2; }
.progress-bar { height: 100%; border-radius: 6rpx; background: #3169f8; transition: width .2s; }
.task-empty { padding: 35rpx 0 15rpx; color: #9aa3b2; text-align: center; font-size: 22rpx; }
.state { padding: 130rpx 0; color: #99a2b2; text-align: center; font-size: 25rpx; }
.notification-popup { width: 650rpx; overflow: hidden; border-radius: 24rpx; background: #fff; box-shadow: 0 16rpx 50rpx rgba(0,0,0,.18); }
.notification-popup-head { display: flex; align-items: center; justify-content: space-between; padding: 28rpx 28rpx 20rpx; border-bottom: 1rpx solid #edf0f5; }
.notification-popup-title { flex: 1; overflow: hidden; color: #28344a; font-size: 29rpx; font-weight: 700; text-overflow: ellipsis; white-space: nowrap; }
.notification-popup-close { flex: 0 0 54rpx; margin-left: 18rpx; color: #9aa3b1; font-size: 42rpx; line-height: 48rpx; text-align: center; }
.notification-popup-time { display: block; padding: 18rpx 28rpx 0; color: #9aa3b1; font-size: 20rpx; }
.notification-popup-scroll { width: 100%; height: 58vh; max-height: 760rpx; box-sizing: border-box; }
.notification-popup-state { padding: 100rpx 28rpx; color: #9aa3b1; text-align: center; font-size: 23rpx; }
.notification-rich-text { display: block; padding: 24rpx 28rpx 36rpx; color: #344056; font-size: 25rpx; line-height: 1.75; overflow-wrap: anywhere; }
.notification-rich-text :deep(img) { max-width: 100%; height: auto; }
.notification-rich-text :deep(table) { max-width: 100%; border-collapse: collapse; }
</style>
