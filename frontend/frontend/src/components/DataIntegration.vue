<template>
  <div class="material-container data-integration-container" :class="{ 'page-loaded': pageLoaded }">
    <!-- 顶部搜索栏 -->
    <div class="search-section">
      <div class="search-bar">
        <input
            v-model="searchQuery"
            type="text"
            placeholder="输入关键词搜索资料..."
            @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch" :class="{ 'searching': isSearching }">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16" class="search-icon">
            <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/>
          </svg>
          <span class="search-text">搜索</span>
          <div class="search-loading">
            <div class="loading-dot"></div>
            <div class="loading-dot"></div>
            <div class="loading-dot"></div>
          </div>
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
        <!-- 在上传模态框的modal-body中添加 -->
        <div v-if="uploadProgress > 0" class="upload-progress">
          <progress :value="uploadProgress" max="100"></progress>
          <span>{{ uploadProgress }}%</span>
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
            v-for="(material, index) in filteredMaterials"
            :key="material.id"
            class="material-card"
            :style="{ '--animation-delay': index * 0.1 + 's' }"
            @click="openMaterial(material)"
            @mouseenter="handleCardHover(material.id, true)"
            @mouseleave="handleCardHover(material.id, false)"
        >
          <div class="material-icon" :class="{ 'icon-hover': hoveredCard === material.id }">
            <span class="subject-icon">{{ getSubjectIcon(material.subject) }}</span>
            <div class="icon-glow"></div>
          </div>
          <div class="material-info">
            <h3>{{ material.name }}</h3>
            <p class="material-meta">
              <span>{{ material.subject }} · {{ material.type }}</span>
              <span>{{ formatDate(material.uploadTime) }}</span>
            </p>
          </div>
          <div class="material-actions">
            <button class="action-btn download-btn" @click.stop="downloadMaterial(material); createRippleEffect($event)" title="下载文件">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5z"/>
                <path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708l3 3z"/>
              </svg>
              <span class="btn-ripple"></span>
            </button>
            <button class="action-btn delete-btn" @click.stop="deleteMaterial(material); createRippleEffect($event)" title="删除文件">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"/>
              </svg>
              <span class="btn-ripple"></span>
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
        <div class="upload-icon-wrapper">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" viewBox="0 0 16 16" class="upload-icon">
            <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5z"/>
            <path d="M7.646 1.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1-.708.708L8.5 2.707V11.5a.5.5 0 0 1-1 0V2.707L5.354 4.854a.5.5 0 1 1-.708-.708l3-3z"/>
          </svg>
          <div class="upload-glow"></div>
        </div>
        <span class="upload-text">上传资料</span>
        <div class="upload-particles">
          <div class="particle"></div>
          <div class="particle"></div>
          <div class="particle"></div>
        </div>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'

// 定义props
// Props定义（如果需要的话可以在这里添加其他props）
// const props = defineProps({})

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
const uploadProgress = ref(0)

// 动画相关状态
const pageLoaded = ref(false)
const isSearching = ref(false)
const hoveredCard = ref(null)

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
const handleSearch = async () => {
  if (isSearching.value) return;
  
  isSearching.value = true;
  console.log('搜索:', searchQuery.value);
  
  // 模拟搜索延迟
  await new Promise(resolve => setTimeout(resolve, 800));
  
  isSearching.value = false;
  // 实际项目中这里会调用API
}

const openMaterial = async (material) => {
  // 显示加载提示
  const loadingModal = createLoadingModal(material.fileName || material.name);
  
  try {
    const token = localStorage.getItem('accessToken');
    
    // 添加超时控制
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 30000); // 30秒超时
    
    const response = await fetch(`/api/preview/file/${material.id}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      signal: controller.signal
    });
    
    clearTimeout(timeoutId);
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const previewData = await response.json();
    
    // 关闭加载提示
    document.body.removeChild(loadingModal);

    if (previewData.error) {
      throw new Error(previewData.error);
    }

    // 检查fileType是否存在，如果不存在则从文件名中提取
    if (!previewData.fileType) {
      const fileExtension = material.fileName.split('.').pop();
      previewData.fileType = fileExtension || 'unknown';
    }

    // 根据文件类型调用不同的预览方法
    switch (previewData.fileType.toLowerCase()) {
      case 'txt':
        await previewTextFile(previewData);
        break;
      case 'pdf':
        await previewPdfFile(previewData);
        break;
      case 'doc':
      case 'docx':
      case 'ppt':
      case 'pptx':
        await previewOfficeFile(previewData);
        break;
      case 'xls':
      case 'xlsx':
        await previewExcelFile(previewData);
        break;
      case 'jpg':
      case 'jpeg':
      case 'png':
      case 'gif':
      case 'bmp':
      case 'webp':
        await previewImageFile(previewData);
        break;
      default:
        // 对于不支持的文件类型，提供下载选项
        const modal = createModal(previewData.fileName);
        const content = modal.querySelector('.modal-content');
        content.innerHTML = `
          <div style="text-align: center; padding: 20px;">
            <div style="margin-bottom: 15px;">
              <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
            </div>
            <p style="margin-bottom: 15px;">不支持预览此文件类型 (${previewData.fileType})</p>
            <a href="/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}"
               target="_blank" style="display: inline-block; padding: 8px 16px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px;">
              下载文件
            </a>
          </div>
        `;
        break;
    }
  } catch (error) {
    // 关闭加载提示（如果还存在）
    try {
      if (loadingModal && loadingModal.parentNode) {
        document.body.removeChild(loadingModal);
      }
    } catch (e) {}
    
    console.error('预览失败:', error);
    
    // 显示友好的错误信息
    let errorMessage = '预览失败';
    if (error.name === 'AbortError') {
      errorMessage = '预览超时，请检查网络连接或稍后重试';
    } else if (error.message.includes('HTTP 404')) {
      errorMessage = '文件不存在或已被删除';
    } else if (error.message.includes('HTTP 413')) {
      errorMessage = '文件过大，无法预览';
    } else if (error.message.includes('HTTP 500')) {
      errorMessage = '服务器错误，请稍后重试';
    } else {
      errorMessage = `预览失败: ${error.message}`;
    }
    
    // 创建错误提示模态框
    const errorModal = createModal('预览失败');
    const content = errorModal.querySelector('.modal-content');
    content.innerHTML = `
      <div style="text-align: center; padding: 20px;">
        <div style="margin-bottom: 15px;">
          <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#ff6b6b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="15" y1="9" x2="9" y2="15"></line>
            <line x1="9" y1="9" x2="15" y2="15"></line>
          </svg>
        </div>
        <p style="margin-bottom: 15px; color: #666;">${errorMessage}</p>
        <a href="/api/files/download?fileName=${encodeURIComponent(material.fileName || material.name)}"
           target="_blank" style="display: inline-block; padding: 8px 16px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px;">
          下载文件
        </a>
      </div>
    `;
  }
};


// 图片文件预览
const previewImageFile = async (previewData) => {
  try {
    // 创建预览模态框
    const modal = createModal(previewData.fileName);
    const content = modal.querySelector('.modal-content');
    
    // 创建图片容器
    const imageContainer = document.createElement('div');
    imageContainer.style.textAlign = 'center';
    imageContainer.style.padding = '10px';
    content.appendChild(imageContainer);
    
    // 添加下载链接
    const downloadLink = document.createElement('a');
    downloadLink.href = `/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}`;
    downloadLink.textContent = '下载原图';
    downloadLink.target = '_blank';
    downloadLink.style.display = 'inline-block';
    downloadLink.style.marginBottom = '15px';
    imageContainer.appendChild(downloadLink);
    
    // 创建图片元素
    const img = document.createElement('img');
    img.src = `data:image/${previewData.fileType};base64,${previewData.content}`;
    img.style.maxWidth = '100%';
    img.style.maxHeight = '80vh';
    img.style.boxShadow = '0 2px 5px rgba(0,0,0,0.2)';
    imageContainer.appendChild(img);
    
  } catch (error) {
    console.error('图片预览失败:', error);
    const modal = createModal(previewData.fileName);
    modal.querySelector('.modal-content').innerHTML = `
      <div style="text-align: center; padding: 20px;">
        <p>图片预览失败: ${error.message}</p>
        <a href="/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}"
           target="_blank" style="display: inline-block; margin-top: 15px; padding: 8px 16px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px;">
          下载图片
        </a>
      </div>
    `;
  }
};

// 文本文件预览
const previewTextFile = async (previewData) => {
  // 创建模态框而不是新窗口
  const modal = document.createElement('div');
  modal.style.position = 'fixed';
  modal.style.top = '0';
  modal.style.left = '0';
  modal.style.width = '100%';
  modal.style.height = '100%';
  modal.style.backgroundColor = 'rgba(0,0,0,0.8)';
  modal.style.zIndex = '1000';
  modal.style.display = 'flex';
  modal.style.justifyContent = 'center';
  modal.style.alignItems = 'center';

  // 内容容器
  const content = document.createElement('div');
  content.style.backgroundColor = 'white';
  content.style.padding = '20px';
  content.style.borderRadius = '8px';
  content.style.maxWidth = '80%';
  content.style.maxHeight = '80%';
  content.style.overflow = 'auto';

  // 标题和关闭按钮
  const header = document.createElement('div');
  header.style.display = 'flex';
  header.style.justifyContent = 'space-between';
  header.style.marginBottom = '10px';

  const title = document.createElement('h3');
  title.textContent = previewData.fileName;

  const closeBtn = document.createElement('button');
  closeBtn.textContent = '×';
  closeBtn.style.background = 'none';
  closeBtn.style.border = 'none';
  closeBtn.style.fontSize = '20px';
  closeBtn.style.cursor = 'pointer';
  closeBtn.onclick = () => document.body.removeChild(modal);

  header.appendChild(title);
  header.appendChild(closeBtn);

  // 内容区域
  const textContent = document.createElement('pre');
  textContent.style.whiteSpace = 'pre-wrap';
  textContent.style.fontFamily = 'monospace';
  textContent.textContent = decodeURIComponent(escape(atob(previewData.content)));

  content.appendChild(header);
  content.appendChild(textContent);
  modal.appendChild(content);

  // 添加到DOM
  document.body.appendChild(modal);
};

// PDF文件预览（渲染所有页面）
const previewPdfFile = async (previewData) => {
  try {
    // 创建预览模态框
    const modal = createModal(previewData.fileName);
    const content = modal.querySelector('.modal-content');
    
    // 创建PDF容器
    const pdfContainer = document.createElement('div');
    pdfContainer.className = 'pdf-container';
    pdfContainer.style.textAlign = 'center';
    content.appendChild(pdfContainer);
    
    // 添加下载链接
    const downloadLink = document.createElement('a');
    downloadLink.href = `/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}`;
    downloadLink.className = 'download-link';
    downloadLink.textContent = '下载原文件';
    downloadLink.target = '_blank';
    downloadLink.style.display = 'inline-block';
    downloadLink.style.marginTop = '15px';
    downloadLink.style.marginBottom = '15px';
    pdfContainer.appendChild(downloadLink);
    
    // 如果是多页面文档
    if (previewData.multiPage) {
      // 分割页面内容
      const pageImages = previewData.content.split(',');
      
      // 创建页面导航
      const pageNav = document.createElement('div');
      pageNav.className = 'page-navigation';
      pageNav.style.marginBottom = '15px';
      pageNav.style.display = 'flex';
      pageNav.style.justifyContent = 'center';
      pageNav.style.gap = '10px';
      
      // 添加页面计数器
      const pageCounter = document.createElement('div');
      pageCounter.className = 'page-counter';
      pageCounter.style.margin = '0 10px';
      pageCounter.style.lineHeight = '30px';
      
      // 添加上一页按钮
      const prevBtn = document.createElement('button');
      prevBtn.textContent = '上一页';
      prevBtn.style.padding = '5px 10px';
      prevBtn.style.cursor = 'pointer';
      
      // 添加下一页按钮
      const nextBtn = document.createElement('button');
      nextBtn.textContent = '下一页';
      nextBtn.style.padding = '5px 10px';
      nextBtn.style.cursor = 'pointer';
      
      pageNav.appendChild(prevBtn);
      pageNav.appendChild(pageCounter);
      pageNav.appendChild(nextBtn);
      pdfContainer.appendChild(pageNav);
      
      // 创建图片容器
      const imageContainer = document.createElement('div');
      imageContainer.className = 'pdf-image-container';
      imageContainer.style.maxWidth = '100%';
      imageContainer.style.margin = '0 auto';
      pdfContainer.appendChild(imageContainer);
      
      // 当前页码
      let currentPage = 0;
      let maxPageReached = 0; // 记录用户浏览过的最大页码
      
      // 显示指定页面
      const showPage = (pageIndex) => {
        // 清空容器
        imageContainer.innerHTML = '';
        
        // 更新页码显示
        pageCounter.textContent = `第 ${pageIndex + 1} 页 / 共 ${pageImages.length} 页`;
        
        // 创建图片元素
        const img = document.createElement('img');
        img.src = `data:image/png;base64,${pageImages[pageIndex]}`;
        img.style.maxWidth = '100%';
        img.style.boxShadow = '0 2px 5px rgba(0,0,0,0.2)';
        imageContainer.appendChild(img);
        
        // 更新按钮状态
        prevBtn.disabled = pageIndex === 0;
        nextBtn.disabled = pageIndex === pageImages.length - 1;
        
        // 更新最大浏览页码
        if (pageIndex > maxPageReached) {
          maxPageReached = pageIndex;
          // 计算进度百分比（基于浏览的页数）
          const progress = Math.round(((maxPageReached + 1) / pageImages.length) * 100);
          console.log(`PDF阅读进度: ${progress}% (已浏览 ${maxPageReached + 1}/${pageImages.length} 页)`);
          // 更新任务进度（仅在有任务ID时）
          if (currentTaskId) {
            updateTaskProgress(currentTaskId, progress);
          } else {
            console.log(`PDF阅读进度: ${progress}% (已浏览 ${maxPageReached + 1}/${pageImages.length} 页) - 无关联任务`);
          }
        }
      };
      
      // 显示第一页
      showPage(currentPage);
      
      // 初始化时设置第一页的进度
      maxPageReached = 0;
      const initialProgress = Math.round(((maxPageReached + 1) / pageImages.length) * 100);
      console.log(`PDF初始进度: ${initialProgress}% (已浏览 ${maxPageReached + 1}/${pageImages.length} 页)`);
      if (currentTaskId) {
        updateTaskProgress(currentTaskId, initialProgress);
      } else {
        console.log('PDF预览 - 无关联任务，仅记录阅读进度');
      }
      
      // 绑定按钮事件
      prevBtn.addEventListener('click', () => {
        if (currentPage > 0) {
          currentPage--;
          showPage(currentPage);
        }
      });
      
      nextBtn.addEventListener('click', () => {
        if (currentPage < pageImages.length - 1) {
          currentPage++;
          showPage(currentPage);
        }
      });
      
    } else {
      // 单页PDF（兼容旧版本）
      const img = document.createElement('img');
      img.src = `data:image/png;base64,${previewData.content}`;
      img.style.maxWidth = '100%';
      img.style.boxShadow = '0 2px 5px rgba(0,0,0,0.2)';
      pdfContainer.appendChild(img);
      
      // 单页PDF直接设置为100%完成
      if (currentTaskId) {
        console.log('DataIntegration: 单页PDF，设置进度为100%');
        updateTaskProgress(currentTaskId, 100);
      } else {
        console.log('DataIntegration: 单页PDF预览完成 - 无关联任务');
      }
    }
    
  } catch (error) {
    console.error('PDF渲染失败:', error);
    const modal = createModal(previewData.fileName);
    modal.querySelector('.modal-content').innerHTML = `
      <p>PDF预览失败: ${error.message}</p>
      <a href="/api/files/download/${encodeURIComponent(previewData.fileName)}"
         target="_blank" class="download-link">
        下载文件
      </a>
    `;
  }
};

const previewOfficeFile = async (previewData) => {
  try {
    const modal = createModal(previewData.fileName);
    const content = modal.querySelector('.modal-content');
    
    // 如果是PPT/PPTX且是多页面文档（使用新的预览方式）
    if ((previewData.fileType === 'ppt' || previewData.fileType === 'pptx') && previewData.multiPage) {
      // 创建PPT容器
      const pptContainer = document.createElement('div');
      pptContainer.className = 'ppt-container';
      pptContainer.style.textAlign = 'center';
      content.appendChild(pptContainer);
      
      // 添加下载链接
      const downloadLink = document.createElement('a');
      downloadLink.href = `/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}`;
      downloadLink.className = 'download-link';
      downloadLink.textContent = '下载原文件';
      downloadLink.target = '_blank';
      downloadLink.style.display = 'inline-block';
      downloadLink.style.marginTop = '15px';
      downloadLink.style.marginBottom = '15px';
      pptContainer.appendChild(downloadLink);
      
      // 分割幻灯片内容
      const slideImages = previewData.content.split(',');
      
      // 创建幻灯片导航
      const slideNav = document.createElement('div');
      slideNav.className = 'slide-navigation';
      slideNav.style.marginBottom = '15px';
      slideNav.style.display = 'flex';
      slideNav.style.justifyContent = 'center';
      slideNav.style.gap = '10px';
      
      // 添加幻灯片计数器
      const slideCounter = document.createElement('div');
      slideCounter.className = 'slide-counter';
      slideCounter.style.margin = '0 10px';
      slideCounter.style.lineHeight = '30px';
      
      // 添加上一页按钮
      const prevBtn = document.createElement('button');
      prevBtn.textContent = '上一页';
      prevBtn.style.padding = '5px 10px';
      prevBtn.style.cursor = 'pointer';
      
      // 添加下一页按钮
      const nextBtn = document.createElement('button');
      nextBtn.textContent = '下一页';
      nextBtn.style.padding = '5px 10px';
      nextBtn.style.cursor = 'pointer';
      
      slideNav.appendChild(prevBtn);
      slideNav.appendChild(slideCounter);
      slideNav.appendChild(nextBtn);
      pptContainer.appendChild(slideNav);
      
      // 创建图片容器
      const imageContainer = document.createElement('div');
      imageContainer.className = 'ppt-image-container';
      imageContainer.style.maxWidth = '100%';
      imageContainer.style.margin = '0 auto';
      pptContainer.appendChild(imageContainer);
      
      // 当前幻灯片索引
      let currentSlide = 0;
      
      // 显示指定幻灯片
      const showSlide = (slideIndex) => {
        // 清空容器
        imageContainer.innerHTML = '';
        
        // 更新幻灯片计数器
        slideCounter.textContent = `第 ${slideIndex + 1} 页 / 共 ${slideImages.length} 页`;
        
        // 创建图片元素
        const img = document.createElement('img');
        img.src = `data:image/png;base64,${slideImages[slideIndex]}`;
        img.style.maxWidth = '100%';
        img.style.boxShadow = '0 2px 5px rgba(0,0,0,0.2)';
        imageContainer.appendChild(img);
        
        // 更新按钮状态
        prevBtn.disabled = slideIndex === 0;
        nextBtn.disabled = slideIndex === slideImages.length - 1;
      };
      
      // 显示第一页
      showSlide(currentSlide);
      
      // 绑定按钮事件
      prevBtn.addEventListener('click', () => {
        if (currentSlide > 0) {
          currentSlide--;
          showSlide(currentSlide);
        }
      });
      
      nextBtn.addEventListener('click', () => {
        if (currentSlide < slideImages.length - 1) {
          currentSlide++;
          showSlide(currentSlide);
        }
      });
      
    } else if ((previewData.fileType === 'doc' || previewData.fileType === 'docx') && previewData.multiPage) {
      // 对于DOCX文件转换的多页面图片，使用图片轮播模式
      const docContainer = document.createElement('div');
      docContainer.className = 'doc-container';
      docContainer.style.textAlign = 'center';
      content.appendChild(docContainer);
      
      // 添加下载链接
      const downloadLink = document.createElement('a');
      downloadLink.href = `/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}`;
      downloadLink.className = 'download-link';
      downloadLink.textContent = '下载原文件';
      downloadLink.target = '_blank';
      downloadLink.style.display = 'inline-block';
      downloadLink.style.marginTop = '15px';
      downloadLink.style.marginBottom = '15px';
      docContainer.appendChild(downloadLink);
      
      // 解码HTML内容并提取图片
      let htmlContent;
      try {
        const binaryString = atob(previewData.content);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
          bytes[i] = binaryString.charCodeAt(i);
        }
        const decoder = new TextDecoder('utf-8');
        htmlContent = decoder.decode(bytes);
      } catch (error) {
        htmlContent = decodeURIComponent(escape(atob(previewData.content)));
      }
      
      // 从HTML中提取Base64图片数据
      const imgRegex = /data:image\/png;base64,([^'"]+)/g;
      const pageImages = [];
      let match;
      while ((match = imgRegex.exec(htmlContent)) !== null) {
        pageImages.push(match[1]);
      }
      
      if (pageImages.length > 0) {
        // 创建页面导航
        const slideNav = document.createElement('div');
        slideNav.className = 'slide-navigation';
        slideNav.style.marginBottom = '15px';
        slideNav.style.display = 'flex';
        slideNav.style.justifyContent = 'center';
        slideNav.style.gap = '10px';
        
        // 添加页面计数器
        const slideCounter = document.createElement('div');
        slideCounter.className = 'slide-counter';
        slideCounter.style.margin = '0 10px';
        slideCounter.style.lineHeight = '30px';
        
        // 添加上一页按钮
        const prevBtn = document.createElement('button');
        prevBtn.textContent = '上一页';
        prevBtn.style.padding = '5px 10px';
        prevBtn.style.cursor = 'pointer';
        
        // 添加下一页按钮
        const nextBtn = document.createElement('button');
        nextBtn.textContent = '下一页';
        nextBtn.style.padding = '5px 10px';
        nextBtn.style.cursor = 'pointer';
        
        slideNav.appendChild(prevBtn);
        slideNav.appendChild(slideCounter);
        slideNav.appendChild(nextBtn);
        docContainer.appendChild(slideNav);
        
        // 创建图片容器
        const imageContainer = document.createElement('div');
        imageContainer.className = 'doc-image-container';
        imageContainer.style.maxWidth = '100%';
        imageContainer.style.margin = '0 auto';
        docContainer.appendChild(imageContainer);
        
        // 当前页面索引
        let currentPage = 0;
        
        // 显示指定页面
        const showPage = (pageIndex) => {
          imageContainer.innerHTML = '';
          slideCounter.textContent = `第 ${pageIndex + 1} 页 / 共 ${pageImages.length} 页`;
          
          const img = document.createElement('img');
          img.src = `data:image/png;base64,${pageImages[pageIndex]}`;
          img.style.maxWidth = '100%';
          img.style.boxShadow = '0 2px 5px rgba(0,0,0,0.2)';
          img.style.border = '1px solid #ddd';
          imageContainer.appendChild(img);
          
          prevBtn.disabled = pageIndex === 0;
          nextBtn.disabled = pageIndex === pageImages.length - 1;
        };
        
        // 显示第一页
        showPage(currentPage);
        
        // 绑定按钮事件
        prevBtn.addEventListener('click', () => {
          if (currentPage > 0) {
            currentPage--;
            showPage(currentPage);
          }
        });
        
        nextBtn.addEventListener('click', () => {
          if (currentPage < pageImages.length - 1) {
            currentPage++;
            showPage(currentPage);
          }
        });
      } else {
        // 如果没有找到图片，显示原始HTML内容
        const htmlContainer = document.createElement('div');
        htmlContainer.innerHTML = htmlContent;
        htmlContainer.style.padding = '20px';
        htmlContainer.style.backgroundColor = '#fff';
        htmlContainer.style.border = '1px solid #ddd';
        htmlContainer.style.borderRadius = '5px';
        htmlContainer.style.maxHeight = '600px';
        htmlContainer.style.overflow = 'auto';
        docContainer.appendChild(htmlContainer);
      }
      
    } else if (previewData.fileType === 'doc' || previewData.fileType === 'docx') {
      // 对于Word文档的文本模式，显示HTML内容
      const docContainer = document.createElement('div');
      docContainer.className = 'doc-container';
      docContainer.style.padding = '20px';
      docContainer.style.backgroundColor = '#fff';
      docContainer.style.border = '1px solid #ddd';
      docContainer.style.borderRadius = '5px';
      docContainer.style.maxHeight = '600px';
      docContainer.style.overflow = 'auto';
      docContainer.style.fontFamily = 'Arial, sans-serif';
      docContainer.style.lineHeight = '1.6';
      
      // 改进的Base64解码方式，更好地处理UTF-8编码
      let htmlContent;
      try {
        const binaryString = atob(previewData.content);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
          bytes[i] = binaryString.charCodeAt(i);
        }
        const decoder = new TextDecoder('utf-8');
        htmlContent = decoder.decode(bytes);
      } catch (error) {
        console.warn('UTF-8解码失败，使用备用方法:', error);
        htmlContent = decodeURIComponent(escape(atob(previewData.content)));
      }
      
      // 使用innerHTML来渲染HTML格式的内容
      docContainer.innerHTML = htmlContent;
      
      // 添加下载链接
      const downloadLink = document.createElement('a');
      downloadLink.href = `/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}`;
      downloadLink.textContent = '下载原文件';
      downloadLink.target = '_blank';
      downloadLink.style.display = 'block';
      downloadLink.style.marginTop = '15px';
      downloadLink.style.textAlign = 'center';
      
      content.appendChild(docContainer);
      content.appendChild(downloadLink);
      
    } else {
      // 使用永中DCS在线预览（国内可直接访问）
      const iframe = document.createElement('iframe');
      iframe.style.width = '100%';
      iframe.style.height = '80vh';
      iframe.style.border = 'none';

      // 构造永中DCS预览URL（使用当前域名和新的查询参数格式）
      const fileUrl = encodeURIComponent(
          window.location.origin + '/api/files/download?fileName=' + encodeURIComponent(previewData.fileName)
      );
      iframe.src = `https://dcs.yozosoft.com/onlinePreview?url=${fileUrl}`;

      content.appendChild(iframe);

      // 添加备用下载链接
      const downloadLink = document.createElement('a');
      downloadLink.href = `/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}`;
      downloadLink.className = 'download-link';
      downloadLink.textContent = '下载文件';
      downloadLink.target = '_blank';
      content.appendChild(downloadLink);
    }

  } catch (error) {
    console.error('Office文件预览失败:', error);
    const modal = createModal(previewData.fileName);
    modal.querySelector('.modal-content').innerHTML = `
      <div style="text-align: center; padding: 20px;">
        <p>预览失败: ${error.message}</p>
        <a href="/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}"
           target="_blank" style="display: inline-block; margin-top: 15px; padding: 8px 16px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px;">
          下载文件
        </a>
      </div>
    `;
  }
};

// Excel文件预览
const previewExcelFile = async (previewData) => {
  try {
    const modal = createModal(previewData.fileName);
    const content = modal.querySelector('.modal-content');
    
    // 创建Excel容器
    const excelContainer = document.createElement('div');
    excelContainer.className = 'excel-container';
    excelContainer.style.padding = '20px';
    excelContainer.style.backgroundColor = '#fff';
    excelContainer.style.border = '1px solid #ddd';
    excelContainer.style.borderRadius = '5px';
    excelContainer.style.maxHeight = '600px';
    excelContainer.style.overflow = 'auto';
    excelContainer.style.fontFamily = 'Arial, sans-serif';
    
    // 解码Base64内容 - 正确处理UTF-8编码
    const htmlContent = decodeURIComponent(escape(atob(previewData.content)));
    // 使用innerHTML来渲染HTML格式的表格内容
    excelContainer.innerHTML = htmlContent;
    
    // 添加下载链接
    const downloadLink = document.createElement('a');
    downloadLink.href = `/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}`;
    downloadLink.textContent = '下载原文件';
    downloadLink.target = '_blank';
    downloadLink.style.display = 'block';
    downloadLink.style.marginTop = '15px';
    downloadLink.style.textAlign = 'center';
    downloadLink.style.padding = '8px 16px';
    downloadLink.style.backgroundColor = '#4CAF50';
    downloadLink.style.color = 'white';
    downloadLink.style.textDecoration = 'none';
    downloadLink.style.borderRadius = '4px';
    
    content.appendChild(excelContainer);
    content.appendChild(downloadLink);
    
  } catch (error) {
    console.error('Excel文件预览失败:', error);
    const modal = createModal(previewData.fileName);
    modal.querySelector('.modal-content').innerHTML = `
      <div style="text-align: center; padding: 20px;">
        <p>Excel预览失败: ${error.message}</p>
        <a href="/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}"
           target="_blank" style="display: inline-block; margin-top: 15px; padding: 8px 16px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px;">
          下载文件
        </a>
      </div>
    `;
  }
};

// 创建模态框的通用方法
const createModal = (title) => {
  const modal = document.createElement('div');
  modal.className = 'file-preview-modal';
  modal.style.position = 'fixed';
  modal.style.top = '0';
  modal.style.left = '0';
  modal.style.width = '100%';
  modal.style.height = '100%';
  modal.style.backgroundColor = 'rgba(0,0,0,0.8)';
  modal.style.zIndex = '1000';
  modal.style.display = 'flex';
  modal.style.justifyContent = 'center';
  modal.style.alignItems = 'center';

  const content = document.createElement('div');
  content.className = 'modal-content';
  content.style.backgroundColor = 'white';
  content.style.padding = '20px';
  content.style.borderRadius = '8px';
  content.style.width = '90%';
  content.style.maxWidth = '900px';
  content.style.maxHeight = '90vh';
  content.style.overflow = 'auto';
  content.style.position = 'relative';

  const closeBtn = document.createElement('button');
  closeBtn.textContent = '×';
  closeBtn.style.position = 'absolute';
  closeBtn.style.top = '10px';
  closeBtn.style.right = '10px';
  closeBtn.style.background = 'none';
  closeBtn.style.border = 'none';
  closeBtn.style.fontSize = '24px';
  closeBtn.style.cursor = 'pointer';
  closeBtn.onclick = () => document.body.removeChild(modal);

  const titleElement = document.createElement('h3');
  titleElement.textContent = title;
  titleElement.style.marginTop = '0';

  content.appendChild(closeBtn);
  content.appendChild(titleElement);
  modal.appendChild(content);

  document.body.appendChild(modal);
  return modal;
};

const createLoadingModal = (fileName) => {
  const modal = document.createElement('div');
  modal.className = 'loading-modal';
  modal.style.position = 'fixed';
  modal.style.top = '0';
  modal.style.left = '0';
  modal.style.width = '100%';
  modal.style.height = '100%';
  modal.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
  modal.style.zIndex = '10001';
  modal.style.display = 'flex';
  modal.style.alignItems = 'center';
  modal.style.justifyContent = 'center';
  
  const content = document.createElement('div');
  content.className = 'loading-content';
  content.style.background = 'white';
  content.style.padding = '30px';
  content.style.borderRadius = '8px';
  content.style.textAlign = 'center';
  content.style.boxShadow = '0 4px 20px rgba(0, 0, 0, 0.3)';
  
  const spinner = document.createElement('div');
  spinner.className = 'loading-spinner';
  spinner.style.width = '40px';
  spinner.style.height = '40px';
  spinner.style.border = '4px solid #f3f3f3';
  spinner.style.borderTop = '4px solid #3498db';
  spinner.style.borderRadius = '50%';
  spinner.style.animation = 'spin 1s linear infinite';
  spinner.style.margin = '0 auto 15px';
  
  const text = document.createElement('p');
  text.textContent = `正在预览 ${fileName}...`;
  text.style.margin = '0 0 10px 0';
  
  const subText = document.createElement('p');
  subText.textContent = '请稍候，预览可能需要几秒钟';
  subText.style.fontSize = '12px';
  subText.style.color = '#666';
  subText.style.margin = '0';
  
  // 添加旋转动画样式
  const style = document.createElement('style');
  style.textContent = `
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `;
  document.head.appendChild(style);
  
  content.appendChild(spinner);
  content.appendChild(text);
  content.appendChild(subText);
  modal.appendChild(content);
  
  document.body.appendChild(modal);
  return modal;
};

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

const uploadFile = async (file) => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch('/api/upload', {
    method: 'POST',
    body: formData,
    // 添加进度监控
    onUploadProgress: (progressEvent) => {
      const percent = Math.round(
          (progressEvent.loaded / progressEvent.total) * 100
      );
      console.log(`上传进度: ${percent}%`);
    }
  });
  // ...处理响应
};

// 替换原有的 confirmUpload 方法
const confirmUpload = async () => {
      if (!canUpload.value) return;

      try {
        console.log('开始上传文件 - 文件名:', selectedFiles.value[0].name, 
                  ', 大小:', selectedFiles.value[0].size, 
                  ', 类型:', selectedFiles.value[0].type);
        console.log('学科:', uploadSubject.value, ', 内容类型:', uploadType.value);
        
        const formData = new FormData();
        // 只上传第一个文件（如需多文件上传需调整）
        formData.append('file', selectedFiles.value[0]);
        formData.append('subject', uploadSubject.value);
        formData.append('type', uploadType.value);

        // 添加JWT认证头
        const token = localStorage.getItem('accessToken');
        console.log('使用的令牌:', token ? token.substring(0, 20) + '...' : '无令牌');

    // 删除下面这行重复的声明
    // const response = await fetch('/api/files/upload', {

    console.log('发送上传请求到: /api/files/upload');
        const uploadResponse = await fetch('/api/files/upload', {
          method: 'POST',
          body: formData,
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        console.log('上传响应状态:', uploadResponse.status);

    if (!uploadResponse.ok) {
      const errorData = await uploadResponse.json();
      throw new Error(errorData.message || '上传失败');
    }

    const data = await uploadResponse.json();
        console.log('上传响应数据:', data);

        // 添加到文件列表
        materials.value.unshift({
          id: data.id,
          name: data.fileName,
          subject: data.subject,
          type: data.contentType,
          uploadTime: data.uploadTime || new Date().toISOString(),
          size: data.size,
          url: data.fileDownloadUri
        });
        console.log('文件已添加到资料库列表');

    // 重置上传状态
    cancelUpload();

    alert('文件上传成功');
  } catch (error) {
        console.error('上传错误:', error);
        
        // 添加详细的错误信息输出
        if (error.response) {
          console.error('错误响应状态:', error.response.status);
          console.error('错误响应数据:', error.response.data);
        } else {
          console.error('错误详细信息:', error.message);
        }
        
        alert(`上传失败: ${error.message}`);
      }
};



// 替换原有的 downloadMaterial 方法
const downloadMaterial = async (material) => {
  try {
    // 获取授权令牌
    const token = localStorage.getItem('accessToken');
    
    // 从URL中提取文件名参数
    const urlParams = new URLSearchParams(material.url.split('?')[1]);
    const fileName = urlParams.get('fileName');
    
    if (!fileName) {
      throw new Error('无法获取文件名');
    }
    
    // 使用API下载并添加授权令牌
    const response = await fetch(`/api/files/download?fileName=${encodeURIComponent(fileName)}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || '下载失败');
    }
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = material.name;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url); // 释放URL对象
  } catch (error) {
    console.error('下载错误:', error);
    alert(`下载失败: ${error.message}`);
  }
};


// 替换原有的 deleteMaterial 方法
const deleteMaterial = async (material) => {
  if (!confirm(`确定要删除 "${material.name}" 吗？`)) return;

  try {
    const token = localStorage.getItem('accessToken');
    const response = await fetch(`/api/files/delete/${material.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || '删除失败');
    }

    // 从列表中移除
    materials.value = materials.value.filter(m => m.id !== material.id);
    alert('文件删除成功');
  } catch (error) {
    console.error('删除错误:', error);
    alert(`删除失败: ${error.message}`);
  }
};


// 修改 onMounted 加载文件列表
onMounted(() => {
  loadMaterials();
  
  // 页面加载动画
  setTimeout(() => {
    pageLoaded.value = true;
  }, 100);
  
  // 为资料卡片添加进入动画延迟
  setTimeout(() => {
    const cards = document.querySelectorAll('.material-card');
    cards.forEach((card, index) => {
      card.style.setProperty('--animation-delay', `${index * 0.1}s`);
    });
  }, 200);
  
  // 监听来自TaskManager的文件预览事件
  const handleTaskFilePreview = async (event) => {
    console.log('DataIntegration: 接收到previewTaskFile事件', event.detail);
    const { fileName, originalFileName, taskName, fileUrl, taskId } = event.detail;
    
    // 确保资料库数据已加载
    if (materials.value.length === 0) {
      console.log('DataIntegration: 资料库数据未加载，开始加载...');
      await loadMaterials();
    }
    
    // 设置当前任务ID用于进度更新
    currentTaskId = taskId;
    
    console.log('DataIntegration: 当前资料库文件数量:', materials.value.length);
    console.log('DataIntegration: 查找文件:', fileName, '原始文件名:', originalFileName);
    console.log('DataIntegration: 关联任务ID:', taskId);
    
    // 查找对应的文件（支持多种匹配方式，优先使用原始文件名）
    const material = materials.value.find(m => {
      // 优先匹配原始文件名
      if (originalFileName) {
        const originalMatch = m.name === originalFileName || 
                            m.fileName === originalFileName ||
                            m.name.includes(originalFileName) ||
                            originalFileName.includes(m.name);
        if (originalMatch) {
          console.log('DataIntegration: 通过原始文件名找到匹配文件:', m);
          return true;
        }
      }
      
      // 如果原始文件名匹配失败，使用当前文件名匹配
      const currentMatch = m.name === fileName || 
             m.fileName === fileName ||
             m.name.includes(fileName) ||
             fileName.includes(m.name);
      if (currentMatch) {
        console.log('DataIntegration: 通过当前文件名找到匹配文件:', m);
      }
      return currentMatch;
    });
    
    if (material) {
      console.log('DataIntegration: 准备预览文件:', material.name);
      // 延迟一下确保页面完全加载
      setTimeout(() => {
        console.log(`DataIntegration: 正在预览文件: ${material.name}`);
        openMaterial(material);
      }, 200);
    } else {
      // 如果没找到，显示详细的提示信息
      const availableFiles = materials.value.map(m => m.name || m.fileName).join('\n- ');
      console.log('DataIntegration: 未找到匹配文件，可用文件:', availableFiles);
      const searchInfo = originalFileName ? `${fileName} (原始名称: ${originalFileName})` : fileName;
      alert(`未在资料库中找到文件: ${searchInfo}\n\n当前资料库中的文件:\n- ${availableFiles}\n\n提示：请确保文件已上传到资料库，或检查文件名是否匹配。`);
    }
  };
  
  // 添加事件监听器
  window.addEventListener('previewTaskFile', handleTaskFilePreview);
  console.log('DataIntegration: 事件监听器已添加');
  
  // 组件卸载时移除事件监听器
  onUnmounted(() => {
    window.removeEventListener('previewTaskFile', handleTaskFilePreview);
    console.log('DataIntegration: 事件监听器已移除');
  });
});

const loadMaterials = async () => {
  try {
    const token = localStorage.getItem('accessToken');
    const response = await fetch('/api/files/list', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (!response.ok) {
      throw new Error('获取文件列表失败');
    }

    const data = await response.json();
    materials.value = data.map(file => ({
      id: file.id,
      name: file.fileName,
      subject: file.subject,
      type: file.contentType,
      uploadTime: file.uploadTime || new Date().toISOString(),
      size: file.size,
      url: file.fileDownloadUri
    }));
  } catch (error) {
    console.error('加载文件错误:', error);
    alert(`加载文件列表失败: ${error.message}`);
  }
};


const formatDate = (dateString) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getSubjectIcon = (subject) => {
  // 将学科值转换为中文
  const subjectMap = {
    'chinese': '语文',
    'math': '数学',
    'english': '英语',
    'physics': '物理',
    'chemistry': '化学',
    'biology': '生物',
    'politics': '政治',
    'history': '历史',
    'geography': '地理'
  };

  // 获取中文名称
  const chineseName = subjectMap[subject] || subject;

  const iconMap = {
    '语文': '语',
    '数学': '数',
    '英语': '英',
    '物理': '物',
    '化学': '化',
    '生物': '生',
    '政治': '政',
    '历史': '史',
    '地理': '地'
  };

  return iconMap[chineseName] || '文';
}

// AI搜索相关状态
const aiSearchQuery = ref('')
const aiResults = ref([])
const aiLoading = ref(false)
const aiError = ref('')

// 当前关联的任务ID（用于更新阅读进度）
let currentTaskId = null;

// 更新任务进度函数
const updateTaskProgress = async (taskId, progress) => {
  if (!taskId) {
    console.log('DataIntegration: 无任务ID，跳过进度更新');
    return;
  }
  
  try {
    const token = localStorage.getItem('accessToken');
    console.log(`DataIntegration: 准备更新任务 ${taskId} 进度为 ${progress}%`);
    console.log(`DataIntegration: 使用token: ${token ? '已获取' : '未获取'}`);
    
    const response = await fetch(`/api/tasks/${taskId}/progress`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ progress })
    });
    
    console.log(`DataIntegration: API响应状态: ${response.status}`);
    
    if (response.ok) {
      const result = await response.json();
      console.log(`DataIntegration: 任务 ${taskId} 进度已更新为 ${progress}%`, result);
      // 通知TaskManager更新本地数据
      const progressUpdateEvent = new CustomEvent('taskProgressUpdated', {
        detail: { taskId, progress }
      });
      window.dispatchEvent(progressUpdateEvent);
    } else {
      const errorText = await response.text();
      console.error('DataIntegration: 更新任务进度失败:', response.status, response.statusText, errorText);
    }
  } catch (error) {
     console.error('DataIntegration: 更新任务进度网络错误:', error);
   }
 }

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

// 卡片悬停事件处理
const handleCardHover = (material, isHovering) => {
  hoveredCard.value = isHovering ? material.id : null;
};

// 按钮点击涟漪效果
const createRippleEffect = (event) => {
  const button = event.currentTarget;
  const ripple = document.createElement('span');
  const rect = button.getBoundingClientRect();
  const size = Math.max(rect.width, rect.height);
  const x = event.clientX - rect.left - size / 2;
  const y = event.clientY - rect.top - size / 2;
  
  ripple.style.width = ripple.style.height = size + 'px';
  ripple.style.left = x + 'px';
  ripple.style.top = y + 'px';
  ripple.classList.add('ripple');
  
  button.appendChild(ripple);
  
  setTimeout(() => {
    ripple.remove();
  }, 600);
};

// 辅助函数：加载外部脚本
const loadScript = (url) => {
  return new Promise((resolve, reject) => {
    if (document.querySelector(`script[src="${url}"]`)) {
      resolve();
      return;
    }

    const script = document.createElement('script');
    script.src = url;
    script.onload = resolve;
    script.onerror = reject;
    document.head.appendChild(script);
  });
};
</script>

<style scoped>
.material-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 20px;
  background-color: rgba(245, 247, 250, 0.85);
}

.search-section {
  background-color: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
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
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 20px;
  overflow-y: auto;
  backdrop-filter: blur(10px);
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
  border: 1px solid rgba(238, 238, 238, 0.8);
  border-radius: 6px;
  padding: 15px;
  display: flex;
  gap: 15px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);
}

.material-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.material-icon {
  width: 50px;
  height: 50px;
  background-color: rgba(240, 244, 255, 0.8);
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

.material-actions button {
  width: auto;
  padding: 6px 12px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.material-actions button:first-child {
  background: linear-gradient(135deg, #4a6cf7, #6b8cff);
}

.material-actions button:first-child:hover {
  background: linear-gradient(135deg, #3a5ce4, #5b7cff);
  transform: translateY(-2px);
}

.material-actions button:nth-child(2) {
  background: linear-gradient(135deg, #ff6b6b, #ff8787);
}

.material-actions button:nth-child(2):hover {
  background: linear-gradient(135deg, #ff5252, #ff6b6b);
  transform: translateY(-2px);
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
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: blur(15px);
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
  background-color: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
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

/* 文件预览模态框样式 */
.file-preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.file-preview-modal .modal-content {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 900px;
  max-height: 90vh;
  overflow: auto;
  position: relative;
  backdrop-filter: blur(15px);
}

.file-preview-modal .modal-content h3 {
  margin-top: 0;
  color: #333;
}

.file-preview-modal .close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.file-preview-modal .close-btn:hover {
  color: #333;
}

/* PDF容器 */
.pdf-container {
  width: 100%;
  padding: 20px;
  overflow-y: auto;
}

/* PDF页面画布 */
.pdf-container canvas {
  display: block;
  margin: 0 auto 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-width: 100%;
}

/* 下载链接 */
.download-link {
  display: inline-block;
  margin-top: 10px;
  padding: 8px 16px;
  background-color: #4a6cf7;
  color: white;
  text-decoration: none;
  border-radius: 4px;
}

.download-link:hover {
  background-color: #3a5ce4;
}

/* Word文档容器样式 */
.doc-container {
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Hiragino Sans GB', Arial, sans-serif !important;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.doc-container h1, .doc-container h2, .doc-container h3, .doc-container h4, .doc-container h5, .doc-container h6 {
  font-weight: bold;
  margin-top: 20px;
  margin-bottom: 10px;
  color: #2c3e50;
}

.doc-container p {
  margin-bottom: 12px;
  text-align: justify;
  word-wrap: break-word;
  word-break: break-all;
}

.doc-container br {
  line-height: 1.5;
}

/* 处理特殊字符显示 */
.doc-container {
  unicode-bidi: embed;
  direction: ltr;
}

/* 确保中文字符正确显示 */
.doc-container * {
  font-variant-ligatures: none;
  text-rendering: geometricPrecision;
}

/* Word转图片预览样式 */
.doc-container img {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 10px auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s ease;
}

.doc-container img:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

/* 页面分隔样式 */
.doc-container div[style*="page-break-after"] {
  border-bottom: 2px dashed #e0e0e0;
  padding-bottom: 20px;
  margin-bottom: 20px;
}

.doc-container div[style*="page-break-after"]:last-child {
  border-bottom: none;
  margin-bottom: 0;
}
.office-preview-notice {
  text-align: center;
  padding: 20px;
}

.office-preview-notice p {
  margin-bottom: 15px;
  font-size: 16px;
  color: #666;
}

/* 页面加载动画 */
.material-container {
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s ease;
}

.material-container.page-loaded {
  opacity: 1;
  transform: translateY(0);
}

/* 卡片进入和悬停动画 */
.material-card {
  opacity: 0;
  transform: translateY(30px) scale(0.95);
  animation: cardSlideIn 0.6s ease forwards;
  animation-delay: var(--animation-delay, 0s);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

@keyframes cardSlideIn {
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.material-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(255, 255, 255, 0.1);
}

.material-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.material-card:hover::before {
  left: 100%;
}

/* 图标发光效果 */
.material-icon {
  position: relative;
  transition: all 0.3s ease;
}

.icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 20px;
  height: 20px;
  background: radial-gradient(circle, rgba(74, 144, 226, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.3s ease;
  pointer-events: none;
}

.material-card:hover .icon-glow {
  transform: translate(-50%, -50%) scale(3);
}

/* 按钮涟漪效果 */
.action-btn {
  position: relative;
  overflow: hidden;
  transition: all 0.2s ease;
}

.action-btn:active {
  transform: scale(0.95);
}

.ripple {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  transform: scale(0);
  animation: rippleEffect 0.6s linear;
  pointer-events: none;
}

@keyframes rippleEffect {
  to {
    transform: scale(4);
    opacity: 0;
  }
}

/* 搜索按钮动画 */
.search-btn {
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.search-btn.searching {
  background: linear-gradient(-45deg, #4a90e2, #357abd, #4a90e2, #357abd);
  background-size: 400% 400%;
  animation: searchGradient 1.5s ease infinite;
}

@keyframes searchGradient {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.search-btn svg {
  transition: transform 0.3s ease;
}

.search-btn.searching svg {
  animation: searchSpin 1s linear infinite;
}

@keyframes searchSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 上传按钮增强动画 */
.upload-btn {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.upload-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(74, 144, 226, 0.3);
}

.upload-icon-wrapper {
  position: relative;
  display: inline-block;
}

.upload-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 30px;
  height: 30px;
  background: radial-gradient(circle, rgba(74, 144, 226, 0.4) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.3s ease;
  pointer-events: none;
}

.upload-btn:hover .upload-glow {
  transform: translate(-50%, -50%) scale(2);
}

.upload-particles {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.upload-particles .particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: #4a90e2;
  border-radius: 50%;
  opacity: 0;
}

.upload-particles .particle:nth-child(1) {
  top: 20%;
  left: 20%;
}

.upload-particles .particle:nth-child(2) {
  top: 60%;
  left: 70%;
}

.upload-particles .particle:nth-child(3) {
  top: 40%;
  left: 50%;
}

.upload-btn:hover .upload-particles .particle {
  animation: particleFloat 2s ease-in-out infinite;
}

.upload-btn:hover .upload-particles .particle:nth-child(2) {
  animation-delay: 0.3s;
}

.upload-btn:hover .upload-particles .particle:nth-child(3) {
  animation-delay: 0.6s;
}

@keyframes particleFloat {
  0%, 100% {
    opacity: 0;
    transform: translate(-50%, -50%) translateY(0);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) translateY(-20px);
  }
}

/* 微交互动画 */
.material-info h3 {
  transition: color 0.2s ease;
}

.material-card:hover .material-info h3 {
  color: #4a90e2;
}

.material-actions button {
  transition: all 0.2s ease;
}

.material-actions button:hover {
  transform: scale(1.1);
}

/* 加载动画优化 */
.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 添加上传进度条样式 */
.upload-progress {
  margin-top: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.upload-progress progress {
  flex: 1;
  height: 10px;
  border-radius: 5px;
}

.upload-progress span {
  font-size: 14px;
  color: #666;
}

/* 错误提示样式 */
.error-message {
  color: #ff4d4f;
  margin-top: 10px;
  font-size: 14px;
}

</style>

