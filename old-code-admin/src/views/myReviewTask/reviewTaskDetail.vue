<template>
  <div class="review-task-detail-page">
    <!-- 顶部导航栏 -->
    <div class="detail-header">
      <div class="header-left">
        <el-button size="small" type="primary" @click="goBack">
          <el-icon :size="14"><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <el-divider direction="vertical" />
        <span class="project-title">在线审阅</span>
        <span class="project-des">{{ taskInfo?.id || '-' }}</span>
      </div>
      <div class="header-right">
      </div>
    </div>

    <!-- 人员内容 -->
    <div class="detail-body">
      <!-- 左侧PDF预览 -->
      <div class="pdf-section" ref="pdfSectionRef">
        <div class="pdf-container">
          <!-- PDF 加载进度条 -->
          <div v-if="pdfLoading" class="pdf-progress-bar">
            <div class="pdf-loading-text">{{ loadingText }}({{ pdfProgress }}%)...</div>
            <el-progress :percentage="pdfProgress" :stroke-width="3" :show-text="false" />
          </div>
          <!-- 水印层 - 放在 pdf-container 层级，不随滚动移动 -->
          <pdf-watermark v-if="pdfSource && (pdfProgress && pdfProgress>69)" :text="watermarkText" :protect="true" />
          <!-- PDF 预览组件 -->
          <div v-if="pdfSource" ref="pdfWrapperRef" class="pdf-wrapper" @scroll="handlePdfScroll">
            <vue-pdf-embed
              ref="pdfRef"
              :source="pdfSource"
              :width="pdfWidth"
              @loaded="handlePdfLoaded"
              @rendered="handlePdfRendered"
              @loading-failed="handlePdfError"
              @progress="handlePdfProgress"
            />
          </div>
          <!-- PDF 加载失败或空白时的提示 -->
          <div v-if="!pdfSource && !pdfLoading" class="pdf-empty">
            <el-empty description="PDF加载失败，请刷新页面重试" />
          </div>
        </div>
      </div>

      <!-- 右侧信息面板 -->
      <review-info-panel
        :task-info="taskInfo"
        :review-remarks="reviewRemarks"
        :processed-id="processedId"
        @download-file="downloadFile"
        @refresh-notes="loadNotesList"
        @submit-review="handleSubmitReview"
      />
    </div>
  </div>
</template>

<script setup name="ReviewTaskDetail">
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { debounce } from 'lodash-es'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import VuePdfEmbed from 'vue-pdf-embed'
import { getReviewTaskDetail, submitReview, getNotesList, getPreviewUrl, saveLastPage } from '@/api/tournament/reviewManage'
import { ossFileFuc } from '@/hooks/download'
import PdfWatermark from './components/PdfWatermark.vue'
import ReviewInfoPanel from './components/ReviewInfoPanel.vue'
import { usePdfProtection } from './composables/usePdfProtection'

const { downloadOssFile } = ossFileFuc()

const route = useRoute()
const router = useRouter()
const processedId = route?.query?.id || ''

const taskInfo = ref({})
const reviewRemarks = ref([])

// PDF 相关
const pdfRef = ref(null)
const pdfSource = ref('')
const pdfLoading = ref(false)
const pdfProgress = ref(0)
const loadingText = ref('正在加载文件中')
const lastScrollTop = ref(0)   // 上次阅读滚动位置（像素值）
const pdfWidth = ref(700)
const pdfWrapperRef = ref(null) // PDF 容器 ref
const pdfSectionRef = ref(null) // PDF 区域 ref，用于防护
let fakeProgressTimer = null    // 假进度定时器

// 水印文字（使用当前用户名或固定文字）
const watermarkText = computed(() => {
  const userName = taskInfo.value?.userName || ''
  const phone = taskInfo.value?.phone || ''
  const parts = []
  if (userName) parts.push(userName)
  if (phone) parts.push(phone)
  return parts.length > 0 ? parts.join(' ') : ''
})

// 启用 PDF 防护（禁用右键、快捷键等）- 只对PDF区域生效
usePdfProtection({
  disableContextMenu: true,
  disableShortcuts: true,
  disableDevTools: false, // 开发者工具检测可选
  targetRef: pdfSectionRef, // 只保护PDF区域
  onDevToolsOpen: () => {
    ElMessage.warning('请关闭开发者工具')
  }
})

onMounted(() => {
  if (!processedId) {
    ElMessage.error('缺少必要的参数')
    return
  }
  loadDetail()
  loadNotesList()
  // 计算 PDF 宽度
  nextTick(() => {
    calcPdfWidth()
  })
  // 监听窗口大小变化
  window.addEventListener('resize', calcPdfWidth)
  // 监听页面可见性变化，页面重新可见时刷新PDF
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

// 页面可见性变化处理
function handleVisibilityChange() {
  if (document.visibilityState === 'visible') {
    // 页面重新可见时，检查PDF是否已丢失
    if (taskInfo.value?.newUrl && !pdfSource.value) {
      // PDF已丢失，重新加载
      loadPdf(taskInfo.value.newUrl)
    }
  }
}

// 计算 PDF 显示宽度
function calcPdfWidth() {
  const pdfSection = document.querySelector('.pdf-section')
  if (pdfSection) {
    pdfWidth.value = pdfSection.clientWidth - 60 // 减去 padding
  }
}

function loadDetail(action) {
  getReviewTaskDetail({ processedId }).then(res => {
    if (res.code === 200) {
      if (!action || action !== 'onRefreshPdf') {
        // 保存上次阅读滚动位置（像素值）
        const scrollTop = parseInt(res.data?.lastPage)
        lastScrollTop.value = isNaN(scrollTop) ? 0 : scrollTop
        loadPdf(res.data?.newUrl)
      }
      taskInfo.value = { ...res.data || {} }
      if (res.data?.referenceDocument) {
        try {
          taskInfo.value.referenceDocument = JSON.parse(res.data.referenceDocument)
        } catch (error) {
          taskInfo.value.referenceDocument = []
        }
      }
    } else {
      ElMessage.error(res.msg || '加载详情失败')
    }
  }).catch(() => {
    ElMessage.error('加载详情失败')
  })
}

// 清理假进度定时器
function clearFakeProgress() {
  if (fakeProgressTimer) {
    clearInterval(fakeProgressTimer)
    fakeProgressTimer = null
  }
}

// 加载 PDF
function loadPdf(fileKey) {
  // 重新加载时清理假进度
  clearFakeProgress()
  if (!fileKey) {
    pdfSource.value = ''
    return
  }
  pdfLoading.value = true
  pdfProgress.value = 0
  loadingText.value = '正在加载文件中'
  getPreviewUrl({ fileKey }).then(res => {
    if (res.code === 200 && res.data) {
      pdfSource.value = res.data
    } else {
      pdfSource.value = ''
      pdfLoading.value = false
      clearFakeProgress()
      ElMessage.error('PDF加载失败')
    }
  }).catch(() => {
    pdfSource.value = ''
    pdfLoading.value = false
    clearFakeProgress()
    ElMessage.error('PDF加载失败')
  })
}

// PDF 加载完成（资源下载完成）
function handlePdfLoaded() {
  // 停在70%，开始假进度
  pdfProgress.value = 70
  loadingText.value = '正在渲染文档'

  // 启动假进度：每秒增加约3-4%，慢慢走到99%
  clearFakeProgress()
  fakeProgressTimer = setInterval(() => {
    if (pdfProgress.value < 99) {
      pdfProgress.value += Math.floor(Math.random() * 3) + 2 // 每次增加2-4%
      if (pdfProgress.value > 99) pdfProgress.value = 99
    }
  }, 1000)
}

// 恢复滚动位置（带重试机制）
function restoreScrollPositionWithRetry(targetScrollTop, attempt = 0) {
  if (!pdfWrapperRef.value || targetScrollTop <= 0) return

  const maxScrollTop = pdfWrapperRef.value.scrollHeight - pdfWrapperRef.value.clientHeight

  // 如果目标位置大于最大可滚动位置，说明 PDF 还未完全渲染
  if (targetScrollTop > maxScrollTop && attempt < 5) {
    // 最多重试 5 次，每次间隔 300ms
    setTimeout(() => {
      restoreScrollPositionWithRetry(targetScrollTop, attempt + 1)
    }, 300)
    return
  }

  // 确保不超过最大滚动范围
  const finalScrollTop = Math.min(targetScrollTop, maxScrollTop)
  pdfWrapperRef.value.scrollTop = finalScrollTop

  console.log(`[PDF] 恢复滚动位置: 目标=${targetScrollTop}, 实际=${finalScrollTop}, 最大=${maxScrollTop}, 尝试次数=${attempt}`)
}

// 恢复到指定滚动位置（像素值）- 简单版本
function restoreScrollPosition(scrollTop) {
  if (!pdfWrapperRef.value || scrollTop <= 0) return

  // 确保不超过最大滚动范围
  const maxScrollTop = pdfWrapperRef.value.scrollHeight - pdfWrapperRef.value.clientHeight
  const targetScrollTop = Math.min(scrollTop, maxScrollTop)

  pdfWrapperRef.value.scrollTop = targetScrollTop
}

// PDF 滚动事件处理
function handlePdfScroll() {
  if (!taskInfo.value?.continueFlag) {
    return
  }
  if (!pdfWrapperRef.value) return

  // 检查是否滚动到底部（防抖 500ms）
  debouncedCheckBottom()

  // 保存页码（使用 debounce 防抖）
  debouncedSavePage()
}

// 使用 lodash debounce 防抖检查底部（500ms）
const debouncedCheckBottom = debounce(() => {
  checkScrollToBottom()
}, 500)



// 使用 lodash debounce 防抖保存滚动位置（1秒）
const debouncedSavePage = debounce(() => {
  if (!pdfWrapperRef.value) return

  const scrollTop = pdfWrapperRef.value.scrollTop
  saveLastPage({
    fileId: processedId,
    lastPage: String(scrollTop) // 直接保存像素值
  }).catch(() => {
    // 保存失败不提示，避免打扰用户
  })
}, 1000)

// 检查是否滚动到底部
function checkScrollToBottom() {
  if (!taskInfo.value?.continueFlag) {
    return
  }
  if (!pdfWrapperRef.value) return

  const scrollTop = pdfWrapperRef.value.scrollTop
  const scrollHeight = pdfWrapperRef.value.scrollHeight
  const clientHeight = pdfWrapperRef.value.clientHeight

  // 防护: 滚动高度明显异常时不触发（PDF 可能未正确加载）
  // 放宽条件：只有滚动高度为0或小于视口高度的一半时才跳过
  if (scrollHeight === 0 || scrollHeight < clientHeight / 2) {
    console.log('[PDF] 底部检测: 滚动高度异常，跳过')
    return
  }

  // 判断是否滚动到底部（距离底部 50px 内）
  const isBottom = scrollTop + clientHeight >= scrollHeight - 50

  if (isBottom && taskInfo.value?.review_status !== '1') {
    console.log('[PDF] 底部检测: 触发提交提示')
    ElMessageBox.confirm(
      '您已阅读完毕，是否变更审阅状态？',
      '阅读完成',
      {
        confirmButtonText: '变更为已审阅',
        cancelButtonText: '暂不变更',
        type: 'info'
      }
    ).then(() => {
      // 用户确认提交，调用提交接口
      submitReview({ fileId: processedId }).then(res => {
        if (res.code === 200) {
          ElMessage.success('提交成功')
          loadDetail('onRefreshPdf')
        } else {
          ElMessage.error(res.msg || '提交失败')
        }
      }).catch(() => {
        ElMessage.error('提交失败')
      })
    }).catch(() => {
      // 用户取消，不处理
    })
  }
}

// PDF 加载进度
function handlePdfProgress(progress) {
  if (progress && progress.loaded && progress.total) {
    // 最多显示到70%，剩下的30%留给假进度
    const actualProgress = (progress.loaded / progress.total) * 100
    pdfProgress.value = Math.min(Math.round(actualProgress * 0.7), 70)
  }
}

// PDF 渲染完成
function handlePdfRendered() {
  // 停止假进度，直接完成
  clearFakeProgress()
  pdfProgress.value = 100
  pdfLoading.value = false
  loadingText.value = '正在加载文件中'

  // 平滑滚动到上次阅读位置
  setTimeout(() => {
    smoothScrollTo(lastScrollTop.value)
  }, 300)
}

// 平滑滚动到指定位置
function smoothScrollTo(targetScrollTop, duration = 800) {
  if (!pdfWrapperRef.value || targetScrollTop <= 0) return

  const maxScrollTop = pdfWrapperRef.value.scrollHeight - pdfWrapperRef.value.clientHeight
  const finalScrollTop = Math.min(targetScrollTop, maxScrollTop)

  if (finalScrollTop <= 0) return

  const startScrollTop = pdfWrapperRef.value.scrollTop
  const distance = finalScrollTop - startScrollTop
  const startTime = performance.now()

  function animate(currentTime) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)

    // 缓动函数 easeOutCubic
    const easeProgress = 1 - Math.pow(1 - progress, 3)

    pdfWrapperRef.value.scrollTop = startScrollTop + distance * easeProgress

    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }

  requestAnimationFrame(animate)
}

// PDF 加载失败
function handlePdfError(error) {
  clearFakeProgress()
  pdfLoading.value = false
  pdfSource.value = ''
  ElMessage.error('PDF加载失败')
  console.error('PDF加载失败:', error)
}

function goBack() {
  router.back()
}

function downloadFile(file) {
  if (!file?.fileUrl) {
    ElMessage.warning('文件链接不存在')
    return
  }
  downloadOssFile(file.fileUrl, file.fileName)
}

function loadNotesList() {
  if (!processedId) return
  getNotesList({ processedRelationId: processedId }).then(res => {
    if (res.code === 200) {
      reviewRemarks.value = res.data || []
    } else {
      reviewRemarks.value = []
    }
  }).catch(() => {
    reviewRemarks.value = []
  })
}

function handleSubmitReview() {
  if (!taskInfo.value?.continueFlag) {
    ElMessage.warning('审阅截止时间已过，不能提交')
    return
  }
  if (taskInfo.value?.review_status == '1') {
    ElMessage.warning('已审阅，不能重复提交')
    return
  }
  ElMessageBox.confirm(
    '确定变更审阅状态吗？',
    '变更审阅状态',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    submitReview({ fileId: processedId }).then(res => {
      if (res.code === 200) {
        ElMessage.success('提交成功')
        loadDetail('onRefreshPdf')
      } else {
        ElMessage.error(res.msg || '提交失败')
      }
    }).catch(() => {
      ElMessage.error('提交失败')
    })
  }).catch(() => {
    // 用户取消，不处理
  })
}

// 组件卸载时清理
onBeforeUnmount(() => {
  debouncedCheckBottom.cancel() // 取消底部检测 debounce
  debouncedSavePage.cancel() // 取消保存页码 debounce
  window.removeEventListener('resize', calcPdfWidth)
  // 移除页面可见性监听
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  // 清理假进度定时器
  clearFakeProgress()
})
</script>

<style scoped lang="scss">
.review-task-detail-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fff;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 20px;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;
    flex-shrink: 0;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .project-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
      .project-des{
        font-size: 12px;
        color: #666666;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
    }
  }

  .detail-body {
    display: flex;
    flex: 1;
    overflow: hidden;
    padding: 12px;
    gap: 12px;

    .pdf-section {
      flex: 1;
      overflow: hidden;

      .pdf-container {
        width: 100%;
        height: 100%;
        // background: #525659;
        border-radius: 4px;
        overflow: hidden;
        position: relative;

        .pdf-progress-bar {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          z-index: 10;

          .pdf-loading-text {
            text-align: center;
            font-size: 12px;
            color: #333333;
            // background: rgba(0, 0, 0, 0.6);
            padding: 4px 0;
            position: absolute;
            top: 20px;
            left: 50%;
            transform: translate(-50%, 0);
          }
        }

        .pdf-wrapper {
          width: 100%;
          height: 100%;
          overflow: auto;
          display: flex;
          justify-content: center;
          align-items: flex-start;
          padding: 20px;
          scrollbar-width: 30px;
          scrollbar-color: #555 #e0e0e0;
          scroll-behavior: smooth;
          &::-webkit-scrollbar {
            width: 30px !important;
            height: 30px !important;
            background: #e0e0e0 !important;
          }
          &::-webkit-scrollbar-track {
            background: #e0e0e0 !important;
            border-radius: 10px !important;
          }
          :deep(.vue-pdf-embed) {
            background: #fff;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
          }
        }

        .pdf-empty {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
    }

  }
}

// 全局样式
:deep(.no-data) {
  text-align: center;
  color: #909399;
  font-size: 13px;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  text-indent: 0em;
  width: 100%;
  padding-top: 12px;
}
</style>
