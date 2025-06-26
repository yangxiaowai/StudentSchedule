<template>
  <div class="material-container">
    <!-- 顶部搜索栏 -->
    <div class="search-section">
      <div class="search-bar">
        <input
            v-model="searchQuery"
            type="text"
            placeholder="输入关键词搜索资料..."
            @keyup.enter="handleSearch"
        />
        <button @click="handleSearch">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
            <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/>
          </svg>
          搜索
        </button>
      </div>

      <div class="search-options" v-if="showAdvancedSearch">
        <div class="filter-group">
          <label>学科分类：</label>
          <select v-model="selectedSubject">
            <option value="">全部</option>
            <option v-for="subject in subjects" :value="subject.value">{{ subject.label }}</option>
          </select>
        </div>

        <div class="filter-group">
          <label>内容类型：</label>
          <select v-model="selectedType">
            <option value="">全部</option>
            <option v-for="type in contentTypes" :value="type.value">{{ type.label }}</option>
          </select>
        </div>

        <button class="toggle-advanced" @click="showAdvancedSearch = false">
          简化搜索
        </button>
      </div>
      <button v-else class="toggle-advanced" @click="showAdvancedSearch = true">
        高级搜索
      </button>
    </div>

    <!-- 资料库展示区域 -->
    <div class="material-library">
      <div class="library-header">
        <h2>我的资料库</h2>
        <div class="sort-options">
          <span>排序方式：</span>
          <select v-model="sortOption">
            <option value="time-desc">最近上传</option>
            <option value="time-asc">最早上传</option>
            <option value="name-asc">名称(A-Z)</option>
            <option value="name-desc">名称(Z-A)</option>
            <option value="subject-asc">学科(A-Z)</option>
            <option value="subject-desc">学科(Z-A)</option>
          </select>
        </div>
      </div>

      <div class="material-grid">
        <div
            v-for="material in filteredMaterials"
            :key="material.id"
            class="material-card"
            @click="openMaterial(material)"
        >
          <div class="material-icon">
            <span class="subject-icon">{{ getSubjectIcon(material.subject) }}</span>
          </div>
          <div class="material-info">
            <h3>{{ material.name }}</h3>
            <p class="material-meta">
              <span>{{ material.subject }} · {{ material.type }}</span>
              <span>{{ formatDate(material.uploadTime) }}</span>
            </p>
          </div>
          <div class="material-actions">
            <button @click.stop="downloadMaterial(material)">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5z"/>
                <path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708l3 3z"/>
              </svg>
            </button>
            <button @click.stop="deleteMaterial(material)">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"/>
              </svg>
            </button>
          </div>
        </div>

        <div v-if="filteredMaterials.length === 0" class="empty-library">
          <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" fill="currentColor" viewBox="0 0 16 16">
            <path d="M.54 3.87.5 3a2 2 0 0 1 2-2h3.672a2 2 0 0 1 1.414.586l.828.828A2 2 0 0 0 9.828 3h3.982a2 2 0 0 1 1.992 2.181l-.637 7A2 2 0 0 1 13.174 14H2.826a2 2 0 0 1-1.991-1.819l-.637-7a1.99 1.99 0 0 1 .342-1.31zM2.19 4a1 1 0 0 0-.996 1.09l.637 7a1 1 0 0 0 .995.91h10.348a1 1 0 0 0 .995-.91l.637-7A1 1 0 0 0 13.81 4H2.19zm4.69-1.707A1 1 0 0 0 6.172 2H2.5a1 1 0 0 0-1 .981l.006.139C1.72 3.042 1.95 3 2.19 3h5.396l-.707-.707z"/>
          </svg>
          <p>暂无资料，请上传或搜索资料</p>
        </div>
      </div>
    </div>

    <!-- 底部上传按钮 -->
    <div class="upload-section">
      <button class="upload-btn" @click="showUploadModal = true">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" viewBox="0 0 16 16">
          <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5z"/>
          <path d="M7.646 1.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1-.708.708L8.5 2.707V11.5a.5.5 0 0 1-1 0V2.707L5.354 4.854a.5.5 0 1 1-.708-.708l3-3z"/>
        </svg>
        上传资料
      </button>
    </div>

    <!-- 上传资料模态框 -->
    <div v-if="showUploadModal" class="modal-overlay" @click.self="showUploadModal = false">
      <div class="upload-modal">
        <div class="modal-header">
          <h3>上传资料</h3>
          <button class="close-btn" @click="showUploadModal = false">
            &times;
          </button>
        </div>

        <div class="modal-body">
          <div class="upload-area" @dragover.prevent @drop.prevent="handleDrop">
            <input
                type="file"
                id="fileInput"
                ref="fileInput"
                @change="handleFileSelect"
                multiple
                style="display: none;"
            />

            <div v-if="!selectedFiles.length" class="drop-zone">
              <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" fill="currentColor" viewBox="0 0 16 16">
                <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5z"/>
                <path d="M7.646 1.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1-.708.708L8.5 2.707V11.5a.5.5 0 0 1-1 0V2.707L5.354 4.854a.5.5 0 1 1-.708-.708l3-3z"/>
              </svg>
              <p>拖放文件到此处或<button @click="triggerFileInput">点击选择文件</button></p>
            </div>

            <div v-else class="file-list">
              <div v-for="(file, index) in selectedFiles" :key="index" class="file-item">
                <span>{{ file.name }}</span>
                <span>{{ formatFileSize(file.size) }}</span>
                <button @click="removeFile(index)">×</button>
              </div>
            </div>
          </div>

          <div class="upload-options">
            <div class="form-group">
              <label>学科分类：</label>
              <select v-model="uploadSubject" required>
                <option value="" disabled selected>请选择学科</option>
                <option v-for="subject in subjects" :value="subject.value">{{ subject.label }}</option>
              </select>
            </div>

            <div class="form-group">
              <label>内容类型：</label>
              <select v-model="uploadType" required>
                <option value="" disabled selected>请选择类型</option>
                <option v-for="type in contentTypes" :value="type.value">{{ type.label }}</option>
              </select>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="cancel-btn" @click="cancelUpload">取消</button>
          <button
              class="confirm-btn"
              @click="confirmUpload"
              :disabled="!canUpload"
          >
            上传
          </button>
        </div>
      </div>
    </div>
  </div>
  <!-- AI学习资源检索 -->
  <div class="ai-search-section">
    <div class="ai-search-box">
      <input
          v-model="aiSearchQuery"
          type="text"
          placeholder="输入学习主题，AI将推荐相关学习网站..."
          @keyup.enter="handleAiSearch"
      />
      <button @click="handleAiSearch" :disabled="aiLoading">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
          <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/>
        </svg>
        {{ aiLoading ? '搜索中...' : 'AI推荐' }}
      </button>
    </div>

    <div v-if="aiLoading" class="ai-loading">
      <div class="loading-spinner"></div>
      <p>AI正在搜索最佳学习资源...</p>
    </div>

    <div v-else-if="aiError" class="ai-error">
      <p>{{ aiError }}</p>
      <button @click="handleAiSearch" class="retry-button">重试</button>
    </div>

    <div v-else-if="aiResults.length > 0" class="ai-results">
      <h3>AI推荐的学习资源：</h3>
      <ul>
        <li v-for="(result, index) in aiResults" :key="index">
          <a :href="result.url" target="_blank" rel="noopener noreferrer">{{ result.title }}</a>
          <p>{{ result.description }}</p>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 搜索相关状态
const searchQuery = ref('')
const selectedSubject = ref('')
const selectedType = ref('')
const showAdvancedSearch = ref(false)

// 资料库相关状态
const sortOption = ref('time-desc')
const materials = ref([])

// 上传相关状态
const showUploadModal = ref(false)
const selectedFiles = ref([])
const uploadSubject = ref('')
const uploadType = ref('')

// 学科和内容类型选项
const subjects = ref([
  { value: 'chinese', label: '语文' },
  { value: 'math', label: '数学' },
  { value: 'english', label: '英语' },
  { value: 'physics', label: '物理' },
  { value: 'chemistry', label: '化学' },
  { value: 'biology', label: '生物' },
  { value: 'politics', label: '政治' },
  { value: 'history', label: '历史' },
  { value: 'geography', label: '地理' }
])

const contentTypes = ref([
  { value: 'textbook', label: '教材' },
  { value: 'notes', label: '笔记' },
  { value: 'exam', label: '真题' },
  { value: 'exercise', label: '习题' },
  { value: 'ppt', label: '课件' }
])

// 模拟数据
onMounted(() => {
  materials.value = [
    {
      id: 1,
      name: '高等数学上册.pdf',
      subject: '数学',
      type: '教材',
      uploadTime: '2023-05-15T10:30:00',
      size: 4500000,
      url: '#'
    },
    {
      id: 2,
      name: '英语四级词汇表.docx',
      subject: '英语',
      type: '笔记',
      uploadTime: '2023-06-20T14:45:00',
      size: 1200000,
      url: '#'
    },
    {
      id: 3,
      name: '政治经济学讲义.pptx',
      subject: '政治',
      type: '课件',
      uploadTime: '2023-07-10T09:15:00',
      size: 3200000,
      url: '#'
    }
  ]
})

// 计算属性
const filteredMaterials = computed(() => {
  let result = [...materials.value]

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(m =>
        m.name.toLowerCase().includes(query) ||
        m.subject.toLowerCase().includes(query) ||
        m.type.toLowerCase().includes(query))
  }

  // 学科过滤
  if (selectedSubject.value) {
    result = result.filter(m => m.subject === selectedSubject.value)
  }

  // 类型过滤
  if (selectedType.value) {
    result = result.filter(m => m.type === selectedType.value)
  }

  // 排序
  switch (sortOption.value) {
    case 'time-desc':
      return result.sort((a, b) => new Date(b.uploadTime) - new Date(a.uploadTime))
    case 'time-asc':
      return result.sort((a, b) => new Date(a.uploadTime) - new Date(b.uploadTime))
    case 'name-asc':
      return result.sort((a, b) => a.name.localeCompare(b.name))
    case 'name-desc':
      return result.sort((a, b) => b.name.localeCompare(a.name))
    case 'subject-asc':
      return result.sort((a, b) => a.subject.localeCompare(b.subject))
    case 'subject-desc':
      return result.sort((a, b) => b.subject.localeCompare(a.subject))
    default:
      return result
  }
})

const canUpload = computed(() => {
  return selectedFiles.value.length > 0 &&
      uploadSubject.value &&
      uploadType.value
})

// 方法
const handleSearch = () => {
  console.log('搜索:', searchQuery.value)
  // 实际项目中这里会调用API
}

const openMaterial = (material) => {
  console.log('打开资料:', material.name)
  // 实际项目中这里会打开资料详情或预览
}

const downloadMaterial = (material) => {
  console.log('下载资料:', material.name)
  // 实际项目中这里会触发下载
}

const deleteMaterial = (material) => {
  console.log('删除资料:', material.name)
  if (confirm(`确定要删除 "${material.name}" 吗？`)) {
    materials.value = materials.value.filter(m => m.id !== material.id)
  }
}

const triggerFileInput = () => {
  document.getElementById('fileInput').click()
}

const handleFileSelect = (e) => {
  selectedFiles.value = Array.from(e.target.files)
}

const handleDrop = (e) => {
  e.preventDefault()
  selectedFiles.value = Array.from(e.dataTransfer.files)
}

const removeFile = (index) => {
  selectedFiles.value.splice(index, 1)
}

const cancelUpload = () => {
  selectedFiles.value = []
  uploadSubject.value = ''
  uploadType.value = ''
  showUploadModal.value = false
}

const confirmUpload = () => {
  // 模拟上传过程
  selectedFiles.value.forEach(file => {
    const newMaterial = {
      id: materials.value.length + 1,
      name: file.name,
      subject: uploadSubject.value,
      type: uploadType.value,
      uploadTime: new Date().toISOString(),
      size: file.size,
      url: '#'
    }
    materials.value.unshift(newMaterial)
  })

  cancelUpload()
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k =1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getSubjectIcon = (subject) => {
  const iconMap = {
    '语文': '语',
    '数学': '数',
    '英语': '英',
    '物理': '物',
    '化学': '化',
    '生物': '生',
    '政治': '政',
    '历史': '史',
    '地理': '地',
    '其他': '他'
  }
  return iconMap[subject] || '文'
}

// AI搜索相关状态
const aiSearchQuery = ref('')
const aiResults = ref([])
const aiLoading = ref(false)
const aiError = ref(null)

// AI搜索方法
const handleAiSearch = async () => {
  console.log('开始执行handleAiSearch')

  // 修复这里 - 添加大括号确保逻辑正确
  if (!aiSearchQuery.value || !aiSearchQuery.value.trim()) {
    console.log('搜索内容为空，直接返回')
    return
  }

  // 取消之前的请求
  if (window.aiSearchController) {
    window.aiSearchController.abort()
  }
  window.aiSearchController = new AbortController()

  console.log('准备发起请求，搜索内容:', aiSearchQuery.value)
  aiLoading.value = true
  aiError.value = null
  aiResults.value = []

  try {
    const response = await fetch(`/api/ai-search?query=${encodeURIComponent(aiSearchQuery.value)}`, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('响应状态:', response.status)
    if (!response.ok) {
      throw new Error(`HTTP错误! 状态: ${response.status}`);
    }
    const data = await response.json();
    console.log('响应数据:', data);
    aiResults.value = data;
  } catch (error) {
    if (error.name !== 'AbortError') {
      console.error('请求详细错误:', error);
      aiError.value = `请求失败: ${error.message}`;
    }
  } finally {
    aiLoading.value = false;
    window.aiSearchController = null;
  }
}



</script>
<style scoped>
.material-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 20px;
  background-color: #f5f7fa;
}

.search-section {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.search-bar input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.search-bar button {
  padding: 10px 20px;
  background-color: #4a6cf7;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.search-options {
  display: flex;
  gap: 15px;
  align-items: center;
  padding: 10px 0;
  border-top: 1px solid #eee;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.toggle-advanced {
  background: none;
  border: none;
  color: #4a6cf7;
  cursor: pointer;
  margin-left: auto;
}

.material-library {
  flex: 1;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0);
  padding: 20px;
  overflow-y: auto;
}

.library-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.library-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-options select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.material-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.material-card {
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 15px;
  display: flex;
  gap: 15px;
  cursor: pointer;
  transition: all 0.2s;
}

.material-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.material-icon {
  width: 50px;
  height: 50px;
  background-color: #f0f4ff;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.material-icon img {
  width: 30px;
  height: 30px;
}

.material-info {
  flex: 1;
  min-width: 0;
}

.material-info h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.material-meta {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: 12px;
  color: #666;
}

.material-actions {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.material-actions button {
  background-color:#4a6cf7;
  border: none;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  border-radius: 4px;
  transition: all 0.2s;
}

.material-actions button:hover {
  background-color: #3a5ce4;
  color: white;
}
.material-actions button:nth-child(2) {
  background-color: #ff4d4f;
}

.material-actions button:nth-child(2):hover {
  background-color: #e43f40;
}

.empty-library {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  color: #999;
}

.empty-library img {
  width: 100px;
  height: 100px;
  margin-bottom: 20px;
  opacity: 0.6;
}

.upload-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.upload-btn {
  padding: 12px 30px;
  background-color: #4a6cf7;
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(74, 108, 247, 0.3);
  transition: all 0.2s;
}

.upload-btn:hover {
  background-color: #3a5ce4;
  transform: translateY(-2px);
}

.subject-icon {
  font-size: 24px;
  font-weight: bold;
  color: #4a6cf7;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.upload-modal {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.upload-area {
  border: 2px dashed #ddd;
  border-radius: 6px;
  padding: 30px;
  text-align: center;
  margin-bottom: 20px;
  transition: all 0.2s;
}

.upload-area:hover {
  border-color: #4a6cf7;
}

.drop-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #666;
}

.drop-zone button {
  background: none;
  border: none;
  color: #4a6cf7;
  text-decoration: underline;
  cursor: pointer;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.file-item button {
  background: none;
  border: none;
  color: #ff4d4f;
  cursor: pointer;
  font-size: 16px;
}

.upload-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  font-size: 14px;
  color: #666;
}

.form-group select {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn {
  padding: 8px 16px;
  background-color: #f5f5f5;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.confirm-btn {
  padding: 8px 16px;
  background-color: #4a6cf7;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.confirm-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.ai-search-section {
  margin-top: 20px;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.ai-search-box {
  display: flex;
  gap: 10px;
}

.ai-search-box input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.ai-search-box button {
  padding: 10px 20px;
  background-color: #4a6cf7;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.ai-search-box button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.ai-loading, .ai-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
  margin-top: 15px;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #4a6cf7;
  animation: spin 1s ease-in-out infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.ai-error {
  color: #ff4d4f;
}

.retry-button {
  margin-top: 10px;
  padding: 8px 16px;
  background-color: #f5f5f5;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.ai-results {
  margin-top: 15px;
}

.ai-results h3 {
  margin-bottom: 10px;
  color: #333;
}

.ai-results ul {
  list-style: none;
  padding: 0;
}

.ai-results li {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.ai-results a {
  color: #4a6cf7;
  text-decoration: none;
  font-weight: bold;
}

.ai-results a:hover {
  text-decoration: underline;
}

.ai-results p {
  margin: 5px 0 0;
  color: #666;
  font-size: 14px;
}

</style>