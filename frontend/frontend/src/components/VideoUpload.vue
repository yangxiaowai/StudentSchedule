<template>
  <div class="video-upload-container">
    <div class="upload-header">
      <h3>视频上传</h3>
      <p class="upload-tips">支持格式：MP4, AVI, MOV, WMV, FLV, WEBM, MKV, M4V（最大200MB）</p>
    </div>
    
    <!-- 拖拽上传区域 -->
    <div 
      class="upload-area"
      :class="{ 'drag-over': isDragOver, 'uploading': isUploading }"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @click="triggerFileInput"
    >
      <input 
        ref="fileInput"
        type="file"
        accept="video/*,.mp4,.avi,.mov,.wmv,.flv,.webm,.mkv,.m4v"
        @change="handleFileSelect"
        style="display: none"
      />
      
      <div v-if="!selectedFile && !isUploading" class="upload-placeholder">
        <i class="upload-icon">📹</i>
        <p class="upload-text">点击或拖拽视频文件到此处</p>
        <p class="upload-subtext">支持多种视频格式，最大200MB</p>
      </div>
      
      <div v-if="selectedFile && !isUploading" class="file-preview">
        <div class="file-info">
          <i class="file-icon">🎬</i>
          <div class="file-details">
            <p class="file-name">{{ selectedFile.name }}</p>
            <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
          </div>
          <button @click.stop="removeFile" class="remove-btn">✕</button>
        </div>
        
        <!-- 视频预览 -->
        <div v-if="videoPreviewUrl" class="video-preview">
          <video 
            :src="videoPreviewUrl" 
            controls 
            preload="metadata"
            style="max-width: 100%; max-height: 200px;"
          >
            您的浏览器不支持视频预览
          </video>
        </div>
      </div>
      
      <div v-if="isUploading" class="upload-progress">
        <div class="progress-circle">
          <div class="progress-text">{{ uploadProgress }}%</div>
        </div>
        <p class="progress-label">正在上传视频...</p>
      </div>
    </div>
    
    <!-- 上传配置 -->
    <div v-if="selectedFile && !isUploading" class="upload-config">
      <div class="config-row">
        <label>学科分类：</label>
        <select v-model="subject" class="config-select">
          <option value="数学">数学</option>
          <option value="语文">语文</option>
          <option value="英语">英语</option>
          <option value="物理">物理</option>
          <option value="化学">化学</option>
          <option value="生物">生物</option>
          <option value="历史">历史</option>
          <option value="地理">地理</option>
          <option value="政治">政治</option>
          <option value="其他">其他</option>
        </select>
      </div>
      
      <div class="config-row">
        <label>视频类型：</label>
        <select v-model="videoType" class="config-select">
          <option value="教学视频">教学视频</option>
          <option value="课程录像">课程录像</option>
          <option value="实验演示">实验演示</option>
          <option value="学习笔记">学习笔记</option>
          <option value="其他">其他</option>
        </select>
      </div>
    </div>
    
    <!-- 操作按钮 -->
    <div v-if="selectedFile && !isUploading" class="upload-actions">
      <button @click="startUpload" class="upload-btn" :disabled="!canUpload">
        开始上传
      </button>
      <button @click="resetUpload" class="cancel-btn">
        取消
      </button>
    </div>
    
    <!-- 上传结果 -->
    <div v-if="uploadResult" class="upload-result">
      <div v-if="uploadResult.success" class="success-message">
        <i class="success-icon">✅</i>
        <p>视频上传成功！</p>
        <p class="result-details">文件名：{{ uploadResult.fileName }}</p>
      </div>
      <div v-else class="error-message">
        <i class="error-icon">❌</i>
        <p>上传失败：{{ uploadResult.error }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { uploadForLibrary } from '@/utils/fileUpload'

export default {
  name: 'VideoUpload',
  data() {
    return {
      selectedFile: null,
      videoPreviewUrl: null,
      isDragOver: false,
      isUploading: false,
      uploadProgress: 0,
      subject: '其他',
      videoType: '教学视频',
      uploadResult: null
    }
  },
  computed: {
    canUpload() {
      return this.selectedFile && this.subject && this.videoType
    }
  },
  methods: {
    triggerFileInput() {
      if (!this.isUploading) {
        this.$refs.fileInput.click()
      }
    },
    
    handleFileSelect(event) {
      const file = event.target.files[0]
      if (file) {
        this.processFile(file)
      }
    },
    
    handleDrop(event) {
      event.preventDefault()
      this.isDragOver = false
      
      if (this.isUploading) return
      
      const files = event.dataTransfer.files
      if (files.length > 0) {
        this.processFile(files[0])
      }
    },
    
    handleDragOver(event) {
      event.preventDefault()
      this.isDragOver = true
    },
    
    handleDragLeave(event) {
      event.preventDefault()
      this.isDragOver = false
    },
    
    processFile(file) {
      // 验证文件类型
      const videoTypes = ['video/mp4', 'video/avi', 'video/quicktime', 'video/x-ms-wmv', 
                         'video/x-flv', 'video/webm', 'video/x-matroska', 'video/x-m4v']
      
      if (!videoTypes.includes(file.type) && !this.isVideoFile(file.name)) {
        alert('请选择有效的视频文件！')
        return
      }
      
      // 验证文件大小 (200MB)
      const maxSize = 200 * 1024 * 1024
      if (file.size > maxSize) {
        alert('视频文件过大，最大支持200MB！')
        return
      }
      
      this.selectedFile = file
      this.uploadResult = null
      
      // 创建视频预览
      this.createVideoPreview(file)
    },
    
    createVideoPreview(file) {
      if (this.videoPreviewUrl) {
        URL.revokeObjectURL(this.videoPreviewUrl)
      }
      this.videoPreviewUrl = URL.createObjectURL(file)
    },
    
    isVideoFile(filename) {
      const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm', '.mkv', '.m4v']
      const extension = filename.toLowerCase().substring(filename.lastIndexOf('.'))
      return videoExtensions.includes(extension)
    },
    
    removeFile() {
      this.selectedFile = null
      if (this.videoPreviewUrl) {
        URL.revokeObjectURL(this.videoPreviewUrl)
        this.videoPreviewUrl = null
      }
      this.uploadResult = null
      this.$refs.fileInput.value = ''
    },
    
    async startUpload() {
      if (!this.canUpload) return
      
      this.isUploading = true
      this.uploadProgress = 0
      this.uploadResult = null
      
      try {
        // 模拟上传进度
        const progressInterval = setInterval(() => {
          if (this.uploadProgress < 90) {
            this.uploadProgress += Math.random() * 10
          }
        }, 200)
        
        const response = await uploadForLibrary(this.selectedFile, this.subject, this.videoType)
        
        clearInterval(progressInterval)
        this.uploadProgress = 100
        
        setTimeout(() => {
          this.uploadResult = {
            success: true,
            fileName: response.originalFileName || this.selectedFile.name
          }
          this.isUploading = false
          this.$emit('upload-success', response)
        }, 500)
        
      } catch (error) {
        this.uploadResult = {
          success: false,
          error: error.message || '上传失败，请重试'
        }
        this.isUploading = false
        console.error('视频上传失败:', error)
      }
    },
    
    resetUpload() {
      this.removeFile()
      this.subject = '其他'
      this.videoType = '教学视频'
      this.uploadProgress = 0
      this.isUploading = false
    },
    
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
  },
  
  beforeUnmount() {
    if (this.videoPreviewUrl) {
      URL.revokeObjectURL(this.videoPreviewUrl)
    }
  }
}
</script>

<style scoped>
.video-upload-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Arial', sans-serif;
}

.upload-header {
  text-align: center;
  margin-bottom: 20px;
}

.upload-header h3 {
  color: #333;
  margin-bottom: 8px;
}

.upload-tips {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.upload-area {
  border: 2px dashed #ddd;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area:hover {
  border-color: #007bff;
  background: #f0f8ff;
}

.upload-area.drag-over {
  border-color: #007bff;
  background: #e3f2fd;
  transform: scale(1.02);
}

.upload-area.uploading {
  border-color: #28a745;
  background: #f0fff0;
  cursor: not-allowed;
}

.upload-placeholder .upload-icon {
  font-size: 48px;
  margin-bottom: 16px;
  display: block;
}

.upload-text {
  font-size: 18px;
  color: #333;
  margin-bottom: 8px;
}

.upload-subtext {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.file-preview {
  width: 100%;
}

.file-info {
  display: flex;
  align-items: center;
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.file-icon {
  font-size: 24px;
  margin-right: 12px;
}

.file-details {
  flex: 1;
  text-align: left;
}

.file-name {
  font-weight: bold;
  margin: 0 0 4px 0;
  color: #333;
}

.file-size {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.remove-btn {
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-btn:hover {
  background: #ff3838;
}

.video-preview {
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.upload-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.progress-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: conic-gradient(#007bff 0deg, #e9ecef 0deg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  position: relative;
}

.progress-circle::before {
  content: '';
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: white;
  position: absolute;
}

.progress-text {
  font-weight: bold;
  color: #007bff;
  z-index: 1;
}

.progress-label {
  color: #666;
  margin: 0;
}

.upload-config {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.config-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.config-row:last-child {
  margin-bottom: 0;
}

.config-row label {
  width: 100px;
  font-weight: bold;
  color: #333;
}

.config-select {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.upload-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 20px;
}

.upload-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  transition: background 0.3s;
}

.upload-btn:hover:not(:disabled) {
  background: #0056b3;
}

.upload-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.cancel-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.3s;
}

.cancel-btn:hover {
  background: #545b62;
}

.upload-result {
  margin-top: 20px;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
}

.success-message {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.error-message {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.success-icon, .error-icon {
  font-size: 24px;
  margin-bottom: 8px;
  display: block;
}

.result-details {
  font-size: 14px;
  margin-top: 8px;
  opacity: 0.8;
}
</style>