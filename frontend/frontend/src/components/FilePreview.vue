<template>
  <div v-if="visible" class="file-preview-overlay" @click="handleOverlayClick">
    <div class="file-preview-modal" @click.stop>
      <!-- 头部 -->
      <div class="preview-header">
        <h3 class="preview-title">{{ fileName }}</h3>
        <div class="preview-actions">
          <button 
            v-if="downloadUrl" 
            @click="downloadFile" 
            class="action-btn download-btn"
            title="下载文件"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
            </svg>
            下载
          </button>
          <button @click="closePreview" class="action-btn close-btn" title="关闭">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="preview-content" ref="contentRef">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>正在加载预览...</p>
          <!-- 预览进度条 -->
          <div class="preview-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: previewProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ previewProgress }}%</span>
          </div>
        </div>

        <!-- 错误状态 -->
        <div v-else-if="error" class="error-container">
          <div class="error-icon">⚠️</div>
          <h4>预览失败</h4>
          <p>{{ error }}</p>
          <button @click="retryPreview" class="retry-btn">重试</button>
        </div>

        <!-- 预览内容 -->
        <div v-else class="preview-content-inner">
      <!-- PDF预览 -->
      <div v-if="fileType === 'pdf'" class="pdf-preview">
        <div v-if="pdfPages.length > 0" class="pdf-pages">
          <div v-for="(page, index) in pdfPages" :key="index" class="pdf-page">
            <div class="page-header">
              <span class="page-number">第 {{ index + 1 }} 页 / 共 {{ pdfPages.length }} 页</span>
            </div>
            <img 
              :src="`data:image/jpeg;base64,${page}`" 
              :alt="`PDF页面 ${index + 1}`"
              class="pdf-page-image"
              @error="handlePdfImageError($event, index)"
              @load="handleImageLoad(index)"
            />
          </div>
        </div>
        <div v-else class="no-content">
          <p>PDF文件为空或无法解析</p>
        </div>
      </div>

      <!-- PowerPoint预览 -->
      <div v-else-if="fileType === 'ppt' || fileType === 'pptx'" class="pdf-preview">
        <div v-if="pdfPages.length > 0" class="pdf-pages">
          <div v-for="(page, index) in pdfPages" :key="index" class="pdf-page">
            <div class="page-header">
              <span class="page-number">第 {{ index + 1 }} 页 / 共 {{ pdfPages.length }} 页</span>
            </div>
            <img 
              :src="`data:image/jpeg;base64,${page}`" 
              :alt="`PowerPoint页面 ${index + 1}`"
              class="pdf-page-image"
              @error="handlePdfImageError($event, index)"
              @load="handleImageLoad(index)"
            />
          </div>
        </div>
        <div v-else class="no-content">
          <p>PowerPoint文件为空或无法解析</p>
        </div>
      </div>

      <!-- 图片预览 -->
      <div v-else-if="isImageFile" class="image-preview">
        <img 
          :src="previewUrl" 
          :alt="fileName"
          class="preview-image"
          @error="handleImageError()"
        />
      </div>

      <!-- 文本预览 -->
      <div v-else-if="isTextFile" class="text-preview">
        <pre class="text-content">{{ textContent }}</pre>
        <div v-if="isTruncated" class="truncation-notice">
          <p>⚠️ 文件内容过长，仅显示前 {{ maxTextSize }} 个字符</p>
        </div>
      </div>

      <!-- Office文件预览 -->
      <div v-else-if="isOfficeFile" class="office-preview">
        <div v-if="officeHtmlContent" class="office-html-container">
          <div v-html="officeHtmlContent" class="office-content"></div>
        </div>
        <div v-else class="office-fallback">
          <div class="fallback-content">
            <div class="file-icon">📄</div>
            <h4>Office文件预览</h4>
            <p>该文件类型需要下载后使用相应软件打开</p>
            <button @click="downloadFile" class="download-btn-large">
              下载文件
            </button>
          </div>
        </div>
      </div>

      <!-- 视频预览 -->
      <div v-else-if="isVideoFile" class="video-preview">
        <video 
          :src="previewUrl" 
          controls 
          class="preview-video"
          @error="handleVideoError"
        >
          您的浏览器不支持视频播放
        </video>
      </div>

      <!-- 不支持的文件类型 -->
      <div v-else class="unsupported-preview">
        <div class="unsupported-content">
          <div class="file-icon">📎</div>
          <h4>不支持预览</h4>
          <p>该文件类型暂不支持在线预览</p>
          <p class="file-info-text">文件名: {{ fileName }}</p>
          <p class="file-info-text">文件类型: {{ fileType.toUpperCase() }}</p>
          <button @click="downloadFile" class="download-btn-large">
            下载文件
          </button>
        </div>
        </div>
      </div>
    </div>
  </div>
 </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import httpClient from '../utils/http.js'
import { getApiUrl } from '../config/api.js'
import fileService from '../services/fileService.js'

// Props
const props = defineProps({
  fileName: {
    type: String,
    required: true
  },
  fileId: {
    type: [String, Number],
    default: null
  },
  fileSize: {
    type: Number,
    default: 0
  },
  visible: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['close', 'download', 'error'])

// 响应式数据
const loading = ref(false)
const error = ref(null)
const previewData = ref(null)
const contentRef = ref(null)
const pdfPages = ref([])
const pageLoadingStates = ref({})
const textContent = ref('')
const isTruncated = ref(false)
const maxTextSize = ref(50000)
const zoomLevel = ref(1)
const fontSize = ref(14)
const wordWrap = ref(true)
const officeError = ref(false)
const videoRef = ref(null)
const videoDuration = ref(0)
const videoProgress = ref(0)
const videoCurrentTime = ref(0)
const videoPlaying = ref(false)
const videoMuted = ref(false)
const videoLoading = ref(false)
const previewProgress = ref(0)
const officeHtmlContent = ref('')

// 计算属性
const fileType = computed(() => {
  const ext = props.fileName.split('.').pop()?.toLowerCase()
  return ext || 'unknown'
})

const isImageFile = computed(() => {
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(fileType.value)
})

const isTextFile = computed(() => {
  return ['txt', 'md', 'json', 'xml', 'csv', 'log'].includes(fileType.value)
})

const isOfficeFile = computed(() => {
  return ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(fileType.value)
})

const isVideoFile = computed(() => {
  return ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm'].includes(fileType.value)
})

const previewUrl = computed(() => {
  if (!props.fileName) return ''
  return getApiUrl('FILE', 'PREVIEW', props.fileName)
})

const downloadUrl = computed(() => {
  if (props.fileId) {
    return getApiUrl('FILE', 'DOWNLOAD_BY_ID', props.fileId)
  }
  return getApiUrl('FILE', 'DOWNLOAD', props.fileName)
})

// 移除在线预览服务依赖，改为使用本地HTML预览

// 缩放控制
const zoomIn = () => {
  if (zoomLevel.value < (fileType.value === 'image' ? 3 : 2)) {
    zoomLevel.value += 0.25
  }
}

const zoomOut = () => {
  if (zoomLevel.value > 0.5) {
    zoomLevel.value -= 0.25
  }
}

const resetZoom = () => {
  zoomLevel.value = 1
}

// 字体大小控制
const increaseFontSize = () => {
  if (fontSize.value < 24) {
    fontSize.value += 2
  }
}

const decreaseFontSize = () => {
  if (fontSize.value > 10) {
    fontSize.value -= 2
  }
}

// 文本操作
const copyText = async () => {
  try {
    await navigator.clipboard.writeText(textContent.value)
    console.log('文本已复制到剪贴板')
  } catch (err) {
    console.error('复制失败:', err)
  }
}

const toggleWordWrap = () => {
  wordWrap.value = !wordWrap.value
}

// 视频控制
const togglePlayPause = () => {
  if (videoRef.value) {
    if (videoRef.value.paused) {
      videoRef.value.play()
    } else {
      videoRef.value.pause()
    }
  }
}

const toggleMute = () => {
  if (videoRef.value) {
    videoRef.value.muted = !videoRef.value.muted
    videoMuted.value = videoRef.value.muted
  }
}

const toggleFullscreen = () => {
  if (videoRef.value) {
    if (videoRef.value.requestFullscreen) {
      videoRef.value.requestFullscreen()
    }
  }
}

const handleVideoPlay = () => {
  videoPlaying.value = true
}

const handleVideoPause = () => {
  videoPlaying.value = false
}

// Office预览重新加载
const refreshOfficePreview = () => {
  officeError.value = false
  officeHtmlContent.value = ''
  loadPreview()
}

// 图片滚轮缩放
const handleImageWheel = (event) => {
  event.preventDefault()
  if (event.deltaY < 0) {
    zoomIn()
  } else {
    zoomOut()
  }
}

// 工具函数
const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const formatFileSize = (bytes) => {
  if (!bytes) return '未知大小'
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

// 方法
const loadPreview = async () => {
  if (!props.fileName) return
  
  loading.value = true
  error.value = null
  previewProgress.value = 0
  
  try {
    // 模拟进度更新
    const progressInterval = setInterval(() => {
      if (previewProgress.value < 90) {
        previewProgress.value += Math.random() * 20
      }
    }, 100)
    
    // 使用fileService获取预览数据
    const data = await fileService.getFilePreview(props.fileName, props.fileId)
    
    // 清除进度更新定时器
    clearInterval(progressInterval)
    previewProgress.value = 100
    
    previewData.value = data
    await processPreviewData(data)
    
  } catch (err) {
    error.value = err.message || '预览加载失败'
    emit('error', error.value)
  } finally {
    loading.value = false
    previewProgress.value = 0
  }
}

const processPreviewData = async (data) => {
  switch (fileType.value) {
    case 'pdf':
      await processPdfData(data)
      break
    case 'txt':
    case 'md':
    case 'json':
    case 'xml':
    case 'csv':
    case 'log':
      processTextData(data)
      break
    case 'doc':
    case 'docx':
    case 'xls':
    case 'xlsx':
      processOfficeData(data)
      break
    case 'ppt':
    case 'pptx':
      processPptData(data)
      break
    default:
      // 其他文件类型不需要特殊处理
      break
  }
}

const processPdfData = async (data) => {
  try {
    if (data.content && typeof data.content === 'string') {
      // 解析逗号分隔的Base64图片数据
      const pages = data.content.split(',').filter(page => page.trim())
      pdfPages.value = pages
      // 初始化页面加载状态
      pages.forEach((_, index) => {
        pageLoadingStates.value[index] = true
      })
    } else {
      throw new Error('PDF数据格式错误')
    }
  } catch (err) {
    error.value = 'PDF解析失败: ' + err.message
  }
}

const processTextData = (data) => {
  if (data.content) {
    textContent.value = data.content
    isTruncated.value = data.truncated || false
  }
}

const processOfficeData = (data) => {
  if (data.content) {
    // 解码Base64内容为HTML
    try {
      const decodedContent = atob(data.content)
      officeHtmlContent.value = decodedContent
    } catch (err) {
      console.error('Office内容解码失败:', err)
      error.value = 'Office文件内容解析失败'
    }
  }
}

const processPptData = async (data) => {
  try {
    if (data.content && typeof data.content === 'string') {
      // 解析逗号分隔的Base64图片数据
      const pages = data.content.split(',').filter(page => page.trim())
      pdfPages.value = pages
      // 初始化页面加载状态
      pages.forEach((_, index) => {
        pageLoadingStates.value[index] = true
      })
    } else {
      throw new Error('PowerPoint数据格式错误')
    }
  } catch (err) {
    error.value = 'PowerPoint解析失败: ' + err.message
  }
}

const handleImageError = (pageIndex = null) => {
  console.error('图片加载失败:', pageIndex !== null ? `第${pageIndex + 1}页` : '图片')
  if (pageIndex !== null) {
    pageLoadingStates.value[pageIndex] = false
  }
}

const handlePdfImageError = (event, pageIndex) => {
  console.error('PDF页面图片加载失败:', `第${pageIndex + 1}页`)
  pageLoadingStates.value[pageIndex] = false
}

const handleImageLoad = (pageIndex = null) => {
  if (pageIndex !== null) {
    pageLoadingStates.value[pageIndex] = false
    console.log(`第${pageIndex + 1}页加载完成`)
  }
}

const handleOfficeLoad = () => {
  console.log('Office预览加载完成')
}



const handleVideoLoad = () => {
  if (videoRef.value) {
    videoDuration.value = videoRef.value.duration
    videoLoading.value = false
  }
}

const handleVideoError = () => {
  console.error('视频加载失败')
  videoLoading.value = false
  error.value = '视频加载失败'
}

const handleVideoTimeUpdate = () => {
  if (videoRef.value) {
    videoCurrentTime.value = videoRef.value.currentTime
    videoProgress.value = (videoRef.value.currentTime / videoRef.value.duration) * 100
  }
}

const retryPreview = () => {
  loadPreview()
}

const downloadFile = () => {
  window.open(downloadUrl.value, '_blank')
  emit('download', { fileName: props.fileName, url: downloadUrl.value })
}

const closePreview = () => {
  emit('close')
}

const handleOverlayClick = () => {
  closePreview()
}

// 监听器
watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadPreview()
  } else {
    // 清理状态
    pdfPages.value = []
    pageLoadingStates.value = {}
    textContent.value = ''
    error.value = null
    officeError.value = false
    officeHtmlContent.value = ''
    previewProgress.value = 0
    zoomLevel.value = 1
    fontSize.value = 14
    wordWrap.value = true
  }
})

// 监听文件名变化
watch(() => props.fileName, () => {
  if (props.fileName && props.visible) {
    loadPreview()
  }
}, { immediate: true })

// 键盘事件
const handleKeydown = (event) => {
  if (event.key === 'Escape') {
    closePreview()
  }
}

// 组件挂载时加载预览
onMounted(() => {
  if (props.fileName && props.visible) {
    loadPreview()
  }
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
/* 主容器样式 */
.file-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 20px;
  box-sizing: border-box;
  backdrop-filter: blur(4px);
}

.file-preview-modal {
  background: white;
  border-radius: 12px;
  width: 95%;
  max-width: 1200px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

/* 头部样式 */
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px 12px 0 0;
}

.preview-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: white;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.download-btn:hover {
  background: rgba(40, 167, 69, 0.9);
}

.close-btn:hover {
  background: rgba(220, 53, 69, 0.9);
}

/* 内容区域 */
.preview-content {
  flex: 1;
  overflow: auto;
  position: relative;
  background: #f8f9fa;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
  text-align: center;
}

.preview-progress {
  margin-top: 20px;
  width: 100%;
  max-width: 300px;
  margin-left: auto;
  margin-right: auto;
}

.preview-progress .progress-bar {
  height: 20px;
  background-color: #e9ecef;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #dee2e6;
  margin-bottom: 8px;
}

.preview-progress .progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50 0%, #45a049 100%);
  border-radius: 10px;
  transition: width 0.3s ease;
  position: relative;
  overflow: hidden;
}

.preview-progress .progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.2) 50%, transparent 100%);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.preview-progress .progress-text {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
  text-align: center;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e3e3e3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 24px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
  text-align: center;
}

.error-icon {
  font-size: 64px;
  margin-bottom: 24px;
  opacity: 0.7;
}

.retry-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

/* 预览主体 */
.preview-body {
  background: white;
  min-height: 100%;
}

/* PDF 预览样式 */
.pdf-preview {
  padding: 24px;
}

.pdf-pages {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.pdf-page {
  text-align: center;
}

.page-header {
  font-weight: 600;
  margin-bottom: 16px;
  color: #495057;
  font-size: 16px;
}

.page-number {
  background: #f8f9fa;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  color: #6c757d;
  border: 1px solid #e9ecef;
}

.pdf-page-image {
  max-width: 100%;
  height: auto;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s ease;
}

.pdf-page-image:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

/* 图片预览样式 */
.image-preview {
  padding: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  transition: transform 0.3s ease;
}

/* 文本预览样式 */
.text-preview {
  padding: 24px;
}

.text-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #dee2e6;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #495057;
  max-height: 60vh;
  overflow: auto;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.06);
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.truncation-notice {
  margin-top: 16px;
  padding: 12px 16px;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  color: #856404;
  border-left: 4px solid #ffc107;
}

/* Office 预览样式 */
.office-preview {
  padding: 24px;
  height: calc(100vh - 200px);
  max-height: 70vh;
}

.office-iframe-container {
  width: 100%;
  height: calc(100% - 60px);
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dee2e6;
}

.office-html-container {
  width: 100%;
  height: 600px;
  overflow: auto;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  background: white;
}

.office-content {
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  line-height: 1.6;
  color: #333;
}

.office-content h1, .office-content h2, .office-content h3 {
  color: #2c3e50;
  margin-top: 1.5em;
  margin-bottom: 0.5em;
}

.office-content table {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
}

.office-content table td, .office-content table th {
  border: 1px solid #e1e5e9;
  padding: 8px 12px;
  text-align: left;
}

.office-content table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.office-content p {
  margin-bottom: 1em;
}

.office-content ul, .office-content ol {
  margin: 1em 0;
  padding-left: 2em;
}

.office-fallback {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px dashed #dee2e6;
}

.fallback-content {
  text-align: center;
  padding: 40px;
}

/* 视频预览样式 */
.video-preview {
  padding: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.preview-video {
  max-width: 100%;
  max-height: 60vh;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

/* 不支持的文件类型样式 */
.unsupported-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
  text-align: center;
  min-height: 400px;
}

.unsupported-content {
  text-align: center;
  padding: 40px;
}

.file-icon {
  font-size: 80px;
  margin-bottom: 24px;
  opacity: 0.6;
}

.file-info-text {
  color: #6c757d;
  font-size: 14px;
  margin: 8px 0;
}

.download-btn-large {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 14px 28px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  margin-top: 16px;
  transition: all 0.2s ease;
}

.download-btn-large:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.no-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  color: #6c757d;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .file-preview-overlay {
    padding: 10px;
  }
  
  .file-preview-modal {
    width: 98%;
    max-height: 95vh;
  }
  
  .preview-header {
    padding: 12px 16px;
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
  
  .preview-title {
    font-size: 16px;
    max-width: 100%;
  }
  
  .action-btn {
    padding: 6px 10px;
    font-size: 12px;
  }
  
  .pdf-preview,
  .image-preview,
  .text-preview,
  .office-preview,
  .video-preview {
    padding: 16px;
  }
  
  .text-content {
    font-size: 12px;
    padding: 16px;
  }
  
  .office-preview {
    height: calc(100vh - 150px);
  }
  
  .loading-container,
  .error-container,
  .unsupported-preview {
    padding: 40px 20px;
  }
}

@media (max-width: 480px) {
  .action-btn {
    font-size: 11px;
    padding: 5px 8px;
  }
  
  .preview-title {
    font-size: 14px;
  }
  
  .file-icon {
    font-size: 60px;
  }
}

/* 滚动条样式 */
.preview-content::-webkit-scrollbar,
.text-content::-webkit-scrollbar {
  width: 8px;
}

.preview-content::-webkit-scrollbar-track,
.text-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.preview-content::-webkit-scrollbar-thumb,
.text-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.preview-content::-webkit-scrollbar-thumb:hover,
.text-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>