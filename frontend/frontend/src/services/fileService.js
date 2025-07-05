import httpClient from '@/utils/http'
import { API_ENDPOINTS } from '@/config/api'

/**
 * 文件服务类 - 统一管理文件相关的API调用
 */
class FileService {
  /**
   * 获取文件预览数据
   * @param {string} fileName - 文件名
   * @param {string|number} fileId - 文件ID（可选）
   * @returns {Promise<Object>} 预览数据
   */
  async getFilePreview(fileName, fileId = null) {
    try {
      const endpoint = fileId 
        ? API_ENDPOINTS.PREVIEW.BY_ID(fileId)
        : API_ENDPOINTS.PREVIEW.BY_NAME(fileName)
      
      const response = await httpClient.get(endpoint)
      return response.data
    } catch (error) {
      console.error('获取文件预览失败:', error)
      throw new Error(error.response?.data?.message || '文件预览加载失败')
    }
  }

  /**
   * 获取文件下载链接
   * @param {string} fileName - 文件名
   * @param {string|number} fileId - 文件ID（可选）
   * @returns {string} 下载链接
   */
  getDownloadUrl(fileName, fileId = null) {
    if (fileId) {
      return API_ENDPOINTS.FILE.DOWNLOAD(fileId)
    }
    return API_ENDPOINTS.FILE.DOWNLOAD(fileName)
  }

  /**
   * 下载文件
   * @param {string} fileName - 文件名
   * @param {string|number} fileId - 文件ID（可选）
   */
  downloadFile(fileName, fileId = null) {
    const downloadUrl = this.getDownloadUrl(fileName, fileId)
    window.open(downloadUrl, '_blank')
  }

  /**
   * 上传文件
   * @param {File} file - 文件对象
   * @param {Object} metadata - 文件元数据
   * @param {string} metadata.subject - 学科
   * @param {string} metadata.type - 内容类型
   * @param {Function} onProgress - 进度回调函数
   * @returns {Promise<Object>} 上传结果
   */
  async uploadFile(file, metadata = {}, onProgress = null) {
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      if (metadata.subject) {
        formData.append('subject', metadata.subject)
      }
      if (metadata.type) {
        formData.append('type', metadata.type)
      }

      const config = {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }

      if (onProgress) {
        config.onUploadProgress = (progressEvent) => {
          const percent = Math.round(
            (progressEvent.loaded / progressEvent.total) * 100
          )
          onProgress(percent)
        }
      }

      const response = await httpClient.post(
        API_ENDPOINTS.FILE.UPLOAD,
        formData,
        config
      )
      
      return response.data
    } catch (error) {
      console.error('文件上传失败:', error)
      throw new Error(error.response?.data?.message || '文件上传失败')
    }
  }

  /**
   * 获取文件列表
   * @param {Object} params - 查询参数
   * @param {string} params.subject - 学科筛选
   * @param {string} params.type - 类型筛选
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页大小
   * @returns {Promise<Object>} 文件列表数据
   */
  async getFileList(params = {}) {
    try {
      const response = await httpClient.get(API_ENDPOINTS.FILE.LIST, {
        params
      })
      return response.data
    } catch (error) {
      console.error('获取文件列表失败:', error)
      throw new Error(error.response?.data?.message || '获取文件列表失败')
    }
  }

  /**
   * 删除文件
   * @param {string|number} fileId - 文件ID
   * @returns {Promise<Object>} 删除结果
   */
  async deleteFile(fileId) {
    try {
      const response = await httpClient.delete(
        API_ENDPOINTS.FILE.DELETE(fileId)
      )
      return response.data
    } catch (error) {
      console.error('删除文件失败:', error)
      throw new Error(error.response?.data?.message || '删除文件失败')
    }
  }

  /**
   * 获取文件信息
   * @param {string|number} fileId - 文件ID
   * @returns {Promise<Object>} 文件信息
   */
  async getFileInfo(fileId) {
    try {
      const response = await httpClient.get(
        `/api/files/info/${fileId}`
      )
      return response.data
    } catch (error) {
      console.error('获取文件信息失败:', error)
      throw new Error(error.response?.data?.message || '获取文件信息失败')
    }
  }

  /**
   * 检测文件类型
   * @param {string} fileName - 文件名
   * @returns {string} 文件类型
   */
  detectFileType(fileName) {
    if (!fileName) return 'unknown'
    
    const ext = fileName.split('.').pop()?.toLowerCase()
    
    const typeMap = {
      // PDF
      pdf: 'pdf',
      // 图片
      jpg: 'image', jpeg: 'image', png: 'image', gif: 'image', 
      bmp: 'image', webp: 'image', svg: 'image', ico: 'image',
      // 文本
      txt: 'text', md: 'text', json: 'text', xml: 'text', 
      csv: 'text', log: 'text', js: 'text', css: 'text', 
      html: 'text', htm: 'text', vue: 'text', jsx: 'text',
      ts: 'text', tsx: 'text', py: 'text', java: 'text',
      cpp: 'text', c: 'text', h: 'text', php: 'text',
      rb: 'text', go: 'text', rs: 'text', swift: 'text',
      // Office
      doc: 'office', docx: 'office', xls: 'office', xlsx: 'office', 
      ppt: 'office', pptx: 'office', pdf: 'office',
      // 视频
      mp4: 'video', avi: 'video', mov: 'video', wmv: 'video', 
      flv: 'video', webm: 'video', mkv: 'video', m4v: 'video',
      // 音频
      mp3: 'audio', wav: 'audio', flac: 'audio', aac: 'audio',
      ogg: 'audio', wma: 'audio', m4a: 'audio',
      // 压缩文件
      zip: 'archive', rar: 'archive', '7z': 'archive', 
      tar: 'archive', gz: 'archive', bz2: 'archive'
    }
    
    return typeMap[ext] || 'unknown'
  }

  /**
   * 格式化文件大小
   * @param {number} bytes - 字节数
   * @returns {string} 格式化后的文件大小
   */
  formatFileSize(bytes) {
    if (!bytes || bytes === 0) return '0 B'
    
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(1024))
    
    if (i === 0) return bytes + ' ' + sizes[i]
    
    return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + sizes[i]
  }

  /**
   * 验证文件类型
   * @param {File} file - 文件对象
   * @param {Array<string>} allowedTypes - 允许的文件类型
   * @returns {boolean} 是否为允许的类型
   */
  validateFileType(file, allowedTypes = []) {
    if (!file || !allowedTypes.length) return true
    
    const fileType = this.detectFileType(file.name)
    return allowedTypes.includes(fileType)
  }

  /**
   * 验证文件大小
   * @param {File} file - 文件对象
   * @param {number} maxSize - 最大文件大小（字节）
   * @returns {boolean} 是否符合大小限制
   */
  validateFileSize(file, maxSize = 100 * 1024 * 1024) { // 默认100MB
    if (!file) return false
    return file.size <= maxSize
  }

  /**
   * 生成Office文件预览URL
   * @param {string} fileUrl - 文件URL
   * @param {string} fileName - 文件名
   * @returns {string} 预览URL
   */
  generateOfficePreviewUrl(fileUrl, fileName) {
    const dcsUrl = 'https://dcs.yozocloud.cn/view/url?'
    const params = new URLSearchParams({
      url: encodeURIComponent(fileUrl),
      fileName: encodeURIComponent(fileName)
    })
    return dcsUrl + params.toString()
  }

  /**
   * 处理文件预览错误
   * @param {Error} error - 错误对象
   * @returns {string} 用户友好的错误信息
   */
  handlePreviewError(error) {
    const errorMessages = {
      'NETWORK_ERROR': '网络连接失败，请检查网络设置',
      'FILE_NOT_FOUND': '文件不存在或已被删除',
      'FILE_TOO_LARGE': '文件过大，无法预览',
      'UNSUPPORTED_FORMAT': '不支持的文件格式',
      'PERMISSION_DENIED': '没有权限访问此文件',
      'SERVER_ERROR': '服务器错误，请稍后重试'
    }

    const errorCode = error.code || error.response?.data?.code
    return errorMessages[errorCode] || error.message || '预览失败，请稍后重试'
  }
}

// 创建单例实例
const fileService = new FileService()

export default fileService
export { FileService }