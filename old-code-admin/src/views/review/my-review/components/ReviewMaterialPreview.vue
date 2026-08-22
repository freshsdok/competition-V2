<template>
  <div class="review-material-preview">
    <div class="preview-toolbar">
      <div class="preview-title">
        <span>附件预览</span>
        <el-tag v-if="previewInfo?.fileType" size="small" effect="plain">{{ previewInfo.fileType }}</el-tag>
        <span class="preview-file-name">{{ previewInfo?.fileName || '' }}</span>
      </div>
      <div class="preview-actions">
        <el-button
          v-if="canFullscreen"
          link
          type="primary"
          icon="FullScreen"
          @click="fullscreenVisible = true"
        >
          全屏预览
        </el-button>
        <el-button
          v-if="previewInfo"
          link
          type="primary"
          icon="Download"
          @click="handleDownload"
        >
          下载
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="preview-body">
      <el-empty v-if="!hasFileUrl" description="请选择附件材料进行预览" />
      <el-result
        v-else-if="errorMessage"
        icon="warning"
        :title="errorMessage"
        sub-title="当前文件暂不支持在线预览，请下载查看"
      >
        <template #extra>
          <el-button type="primary" icon="Download" @click="handleDownload">下载查看</el-button>
        </template>
      </el-result>
      <iframe
        v-else-if="previewInfo?.previewType === 'pdf' && objectUrl"
        class="pdf-frame"
        :src="objectUrl"
        title="PDF 预览"
      />
      <VueOfficeDocx
        v-else-if="isDocxPreview"
        class="office-preview"
        :src="objectUrl"
        @error="handleOfficeError"
      />
      <VueOfficeExcel
        v-else-if="isExcelPreview"
        class="office-preview"
        :src="objectUrl"
        :options="excelOptions"
        @error="handleOfficeError"
      />
      <VueOfficePptx
        v-else-if="isPptxPreview"
        class="office-preview"
        :src="objectUrl"
        @error="handleOfficeError"
      />
      <iframe
        v-else-if="officeOnlineUrl"
        class="office-online-frame"
        :src="officeOnlineUrl"
        title="Office 在线预览"
      />
      <div v-else-if="previewInfo?.previewType === 'image' && objectUrl" class="image-preview-wrap">
        <el-image
          class="image-preview"
          :src="objectUrl"
          fit="contain"
          :preview-src-list="[objectUrl]"
          preview-teleported
          @error="handleImageError"
        />
      </div>
      <div v-else-if="previewInfo?.previewType === 'video' && objectUrl" class="video-preview-wrap">
        <video class="video-preview" :src="objectUrl" controls preload="metadata" @error="handleMediaError" />
      </div>
      <div v-else-if="previewInfo?.previewType === 'audio' && objectUrl" class="audio-preview-wrap">
        <audio class="audio-preview" :src="objectUrl" controls preload="metadata" @error="handleMediaError" />
      </div>
      <iframe
        v-else-if="previewInfo?.previewType === 'frame' && objectUrl"
        class="generic-frame"
        :src="objectUrl"
        title="文件预览"
      />
      <el-empty v-else-if="!loading" description="请选择附件材料进行预览" />
    </div>

    <el-dialog
      v-model="fullscreenVisible"
      :title="previewInfo?.fileName || '附件预览'"
      width="90vw"
      append-to-body
      destroy-on-close
      class="material-preview-dialog"
    >
      <iframe
        v-if="previewInfo?.previewType === 'pdf' && objectUrl"
        class="dialog-pdf-frame"
        :src="objectUrl"
        title="PDF 预览"
      />
      <VueOfficeDocx
        v-else-if="isDocxPreview"
        :src="objectUrl"
        class="dialog-office-preview"
        @error="handleOfficeError"
      />
      <VueOfficeExcel
        v-else-if="isExcelPreview"
        :src="objectUrl"
        :options="excelOptions"
        class="dialog-office-preview"
        @error="handleOfficeError"
      />
      <VueOfficePptx
        v-else-if="isPptxPreview"
        :src="objectUrl"
        class="dialog-office-preview"
        @error="handleOfficeError"
      />
      <iframe
        v-else-if="officeOnlineUrl"
        class="dialog-office-online-frame"
        :src="officeOnlineUrl"
        title="Office 在线预览"
      />
      <div v-else-if="previewInfo?.previewType === 'image' && objectUrl" class="dialog-image-wrap">
        <img :src="objectUrl" alt="评审材料图片预览" />
      </div>
      <div v-else-if="previewInfo?.previewType === 'video' && objectUrl" class="dialog-video-wrap">
        <video class="dialog-video-preview" :src="objectUrl" controls preload="metadata" @error="handleMediaError" />
      </div>
      <div v-else-if="previewInfo?.previewType === 'audio' && objectUrl" class="dialog-audio-wrap">
        <audio class="dialog-audio-preview" :src="objectUrl" controls preload="metadata" @error="handleMediaError" />
      </div>
      <iframe
        v-else-if="previewInfo?.previewType === 'frame' && objectUrl"
        class="dialog-generic-frame"
        :src="objectUrl"
        title="文件预览"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, defineAsyncComponent, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import VueOfficeDocx from '@vue-office/docx'
import VueOfficeExcel from '@vue-office/excel'
import '@vue-office/docx/lib/index.css'
import '@vue-office/excel/lib/index.css'
import {
  getOssPreviewUrl,
  isOssFileUrl,
  normalizeDirectFileUrl
} from '@/api/review/materialPreview'
import { ossFileFuc } from '@/hooks/download'

const props = defineProps({
  fileId: {
    type: [Number, String],
    default: null
  },
  fileUrl: {
    type: String,
    default: ''
  },
  fileName: {
    type: String,
    default: ''
  },
  fileType: {
    type: String,
    default: ''
  }
})

const loading = ref(false)
const fullscreenVisible = ref(false)
const previewInfo = ref(null)
const objectUrl = ref('')
const officeOnlineUrl = ref('')
const errorMessage = ref('')
const excelOptions = {
  showContextmenu: false
}
const { downloadOssFile } = ossFileFuc()
const VueOfficePptx = defineAsyncComponent(() => import('@vue-office/pptx'))
const FRONTEND_PDF_TYPES = new Set(['pdf'])
const FRONTEND_IMAGE_TYPES = new Set(['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'])
const FRONTEND_VIDEO_TYPES = new Set(['mp4', 'webm', 'ogg', 'ogv', 'mov'])
const FRONTEND_AUDIO_TYPES = new Set(['mp3', 'wav', 'ogg', 'm4a', 'aac', 'flac'])
const FRONTEND_DOCX_TYPES = new Set(['docx'])
const FRONTEND_EXCEL_TYPES = new Set(['xls', 'xlsx'])
const FRONTEND_PPTX_TYPES = new Set(['pptx'])
const LEGACY_OFFICE_TYPES = new Set(['doc', 'ppt'])

const canFullscreen = computed(() => {
  return !errorMessage.value && previewInfo.value && (
    (previewInfo.value.previewType === 'pdf' && objectUrl.value) ||
    isDocxPreview.value ||
    isExcelPreview.value ||
    isPptxPreview.value ||
    officeOnlineUrl.value ||
    (previewInfo.value.previewType === 'image' && objectUrl.value) ||
    (previewInfo.value.previewType === 'video' && objectUrl.value) ||
    (previewInfo.value.previewType === 'audio' && objectUrl.value) ||
    (previewInfo.value.previewType === 'frame' && objectUrl.value)
  )
})

const hasFileUrl = computed(() => Boolean(props.fileUrl))
const fileExt = computed(() => resolvePreviewExt(previewInfo.value))
const isDocxPreview = computed(() => FRONTEND_DOCX_TYPES.has(fileExt.value) && objectUrl.value)
const isExcelPreview = computed(() => FRONTEND_EXCEL_TYPES.has(fileExt.value) && objectUrl.value)
const isPptxPreview = computed(() => FRONTEND_PPTX_TYPES.has(fileExt.value) && objectUrl.value)

watch(() => [props.fileId, props.fileUrl, props.fileName, props.fileType], () => {
  loadPreview()
}, { immediate: true })

onBeforeUnmount(() => {
  revokeObjectUrl()
})

async function loadPreview() {
  revokeObjectUrl()
  previewInfo.value = null
  officeOnlineUrl.value = ''
  errorMessage.value = ''
  fullscreenVisible.value = false

  if (!props.fileUrl) {
    return
  }

  loading.value = true
  try {
    const directExt = resolvePreviewExt()
    const previewUrl = await fetchDocumentPreviewSource()
    if (LEGACY_OFFICE_TYPES.has(directExt)) {
      if (canUseOfficeOnline(previewUrl)) {
        previewInfo.value = buildDirectPreviewInfo(directExt, true)
        officeOnlineUrl.value = buildOfficeOnlineUrl(previewUrl)
        return
      }
    }
    previewInfo.value = buildDirectPreviewInfo(directExt, false)
    objectUrl.value = previewUrl
  } catch (e) {
    errorMessage.value = e?.message || '材料预览失败，请下载查看'
  } finally {
    loading.value = false
  }
}

function buildDirectPreviewInfo(ext, useOfficeOnline) {
  return {
    fileId: props.fileId,
    fileName: props.fileName || fileNameFromUrl(props.fileUrl),
    fileType: ext || 'file',
    previewType: directPreviewType(ext, useOfficeOnline)
  }
}

function directPreviewType(ext, useOfficeOnline) {
  if (useOfficeOnline) {
    return 'office-online'
  }
  if (FRONTEND_PDF_TYPES.has(ext)) {
    return 'pdf'
  }
  if (FRONTEND_IMAGE_TYPES.has(ext)) {
    return 'image'
  }
  if (FRONTEND_VIDEO_TYPES.has(ext)) {
    return 'video'
  }
  if (FRONTEND_AUDIO_TYPES.has(ext)) {
    return 'audio'
  }
  if (FRONTEND_DOCX_TYPES.has(ext)) {
    return 'docx'
  }
  if (FRONTEND_EXCEL_TYPES.has(ext)) {
    return 'excel'
  }
  if (FRONTEND_PPTX_TYPES.has(ext)) {
    return 'pptx'
  }
  return 'frame'
}

async function fetchDocumentPreviewSource() {
  if (!props.fileUrl) {
    throw new Error('材料文件地址为空')
  }
  if (!isOssFileUrl(props.fileUrl)) {
    return normalizeDirectFileUrl(props.fileUrl)
  }
  const response = await getOssPreviewUrl(props.fileUrl)
  if (response.code !== 200 || !response.data) {
    throw new Error(response.msg || '获取文件预览地址失败')
  }
  return normalizeDirectFileUrl(response.data)
}

function buildOfficeOnlineUrl(url) {
  return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(url)}`
}

function canUseOfficeOnline(url) {
  if (!/^https?:\/\//i.test(url)) {
    return false
  }
  try {
    const parsed = new URL(url)
    const hostname = parsed.hostname.toLowerCase()
    return hostname !== 'localhost'
      && hostname !== '127.0.0.1'
      && !hostname.startsWith('192.168.')
      && !hostname.startsWith('10.')
      && !isPrivate172Host(hostname)
  } catch (e) {
    return false
  }
}

function isPrivate172Host(hostname) {
  const match = hostname.match(/^172\.(\d{1,2})\./)
  if (!match) {
    return false
  }
  const second = Number(match[1])
  return second >= 16 && second <= 31
}

async function handleDownload() {
  if (!props.fileUrl) {
    ElMessage.warning('材料文件地址为空')
    return
  }
  const fileName = previewInfo.value?.fileName || props.fileName || fileNameFromUrl(props.fileUrl)
  if (isOssFileUrl(props.fileUrl)) {
    downloadOssFile(props.fileUrl, fileName)
    return
  }
  window.open(normalizeDirectFileUrl(props.fileUrl), '_blank')
}

function revokeObjectUrl() {
  objectUrl.value = ''
}

function resolvePreviewExt(info) {
  const candidates = [
    info?.fileType,
    props.fileType,
    extFromName(info?.fileName),
    extFromName(props.fileName),
    extFromName(props.fileUrl)
  ]
  for (const item of candidates) {
    const ext = normalizeExt(item)
    if (ext && ext !== '-') {
      return ext
    }
  }
  return ''
}

function normalizeExt(value) {
  if (!value) {
    return ''
  }
  const ext = String(value).trim().toLowerCase()
  return ext.startsWith('.') ? ext.slice(1) : ext
}

function extFromName(value) {
  if (!value) {
    return ''
  }
  const clean = String(value).split('?')[0].split('#')[0]
  const index = clean.lastIndexOf('.')
  return index >= 0 ? clean.slice(index + 1) : ''
}

function handleImageError() {
  errorMessage.value = '图片加载失败，请下载查看'
}

function handleMediaError() {
  errorMessage.value = '媒体文件加载失败，请下载查看'
}

function handleOfficeError() {
  errorMessage.value = '文件预览失败，请下载查看'
}

function fileNameFromUrl(value) {
  if (!value) {
    return ''
  }
  const clean = String(value).split('?')[0].split('#')[0]
  const index = clean.lastIndexOf('/')
  const fileName = index >= 0 ? clean.slice(index + 1) : clean
  try {
    return decodeURIComponent(fileName)
  } catch (e) {
    return fileName
  }
}
</script>

<style scoped>
.review-material-preview {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fff;
}

.preview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 44px;
  padding: 8px 12px;
  border-bottom: 1px solid #ebeef5;
}

.preview-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  font-weight: 600;
  color: #303133;
}

.preview-file-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 400;
  color: #606266;
}

.preview-actions {
  display: flex;
  flex: none;
  align-items: center;
  gap: 8px;
}

.preview-body {
  min-height: 600px;
  max-height: 70vh;
  overflow: auto;
  background: #f8fafc;
}

.image-preview-wrap {
  display: flex;
  min-height: 600px;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.image-preview {
  max-width: 100%;
  max-height: 660px;
}

.video-preview-wrap {
  display: flex;
  min-height: 600px;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: #111827;
}

.video-preview {
  width: 100%;
  max-width: 100%;
  max-height: 660px;
  background: #000;
}

.audio-preview-wrap {
  display: flex;
  min-height: 600px;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: #f8fafc;
}

.audio-preview {
  width: min(720px, 100%);
}

.office-preview {
  width: 100%;
  min-height: 640px;
  background: #fff;
}

.pdf-frame,
.generic-frame,
.office-online-frame {
  width: 100%;
  min-height: 640px;
  border: 0;
  background: #fff;
}

.dialog-office-preview {
  width: 100%;
  height: 80vh;
  overflow: auto;
  background: #fff;
}

.dialog-pdf-frame,
.dialog-generic-frame,
.dialog-office-online-frame {
  width: 100%;
  height: 80vh;
  border: 0;
  background: #fff;
}

.dialog-video-wrap {
  display: flex;
  height: 80vh;
  align-items: center;
  justify-content: center;
  overflow: auto;
  background: #111827;
}

.dialog-video-preview {
  width: 100%;
  max-width: 100%;
  max-height: 100%;
  background: #000;
}

.dialog-audio-wrap {
  display: flex;
  height: 80vh;
  align-items: center;
  justify-content: center;
  overflow: auto;
  background: #f8fafc;
}

.dialog-audio-preview {
  width: min(720px, 100%);
}

.dialog-image-wrap {
  display: flex;
  height: 80vh;
  align-items: center;
  justify-content: center;
  overflow: auto;
  background: #f8fafc;
}

.dialog-image-wrap img {
  max-width: 100%;
  max-height: 100%;
}
</style>
