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
const uploadProgress = ref(0)

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
const handleSearch = () => {
  console.log('搜索:', searchQuery.value)
  // 实际项目中这里会调用API
}

const openMaterial = async (material) => {
  try {
    const token = localStorage.getItem('accessToken');
    const response = await fetch(`/api/preview/${material.id}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    const previewData = await response.json();

    if (previewData.error) {
      throw new Error(previewData.error);
    }

    // 根据文件类型调用不同的预览方法
    switch (previewData.fileType) {
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
      default:
        throw new Error('不支持预览此文件类型');
    }
  } catch (error) {
    console.error('预览失败:', error);
    alert(`预览失败: ${error.message}`);
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
  textContent.textContent = atob(previewData.content);

  content.appendChild(header);
  content.appendChild(textContent);
  modal.appendChild(content);

  // 添加到DOM
  document.body.appendChild(modal);
};

// PDF文件预览（渲染所有页面）
const previewPdfFile = async (previewData) => {
  try {
    // 动态加载PDF.js库
    await loadScript('https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.4.120/pdf.min.js');

    const pdfjsLib = window['pdfjs-dist/build/pdf'];
    pdfjsLib.GlobalWorkerOptions.workerSrc =
        'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.4.120/pdf.worker.min.js';

    // 创建预览模态框
    const modal = createModal(previewData.fileName);
    const content = modal.querySelector('.modal-content');

    // 创建PDF容器
    const pdfContainer = document.createElement('div');
    pdfContainer.className = 'pdf-container';
    content.appendChild(pdfContainer);

    // 加载PDF
    const loadingTask = pdfjsLib.getDocument({
      data: Uint8Array.from(atob(previewData.content), c => c.charCodeAt(0))
    });

    const pdf = await loadingTask.promise;

    // 渲染所有页面
    for (let i = 1; i <= pdf.numPages; i++) {
      const page = await pdf.getPage(i);
      const viewport = page.getViewport({ scale: 1.0 });

      const canvas = document.createElement('canvas');
      const context = canvas.getContext('2d');
      canvas.height = viewport.height;
      canvas.width = viewport.width;

      // 添加页面分隔线（除第一页外）
      if (i > 1) {
        const divider = document.createElement('hr');
        divider.style.margin = '20px 0';
        pdfContainer.appendChild(divider);
      }

      // 添加页面标题
      const pageHeader = document.createElement('div');
      pageHeader.textContent = `第 ${i} 页`;
      pageHeader.style.marginBottom = '10px';
      pageHeader.style.fontWeight = 'bold';
      pdfContainer.appendChild(pageHeader);

      // 添加画布
      pdfContainer.appendChild(canvas);

      // 渲染页面
      await page.render({
        canvasContext: context,
        viewport: viewport
      }).promise;
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

  } catch (error) {
    console.error('预览失败:', error);
    const modal = createModal(previewData.fileName);
    modal.querySelector('.modal-content').innerHTML = `
      <div class="office-preview-fallback">
        <p>在线预览不可用，请下载后查看</p>
        <a href="/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}"
           class="download-link">
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
    // 直接使用文件下载URL
    window.open(material.url, '_blank');

    // 或者使用API下载（如果需要额外处理）
    // const token = localStorage.getItem('accessToken');
    // const response = await fetch(`/api/files/download?fileName=${encodeURIComponent(material.name)}`, {
    //   headers: {
    //     'Authorization': `Bearer ${token}`
    //   }
    // });
    // const blob = await response.blob();
    // const url = window.URL.createObjectURL(blob);
    // const a = document.createElement('a');
    // a.href = url;
    // a.download = material.name;
    // document.body.appendChild(a);
    // a.click();
    // document.body.removeChild(a);
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
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 900px;
  max-height: 90vh;
  overflow: auto;
  position: relative;
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
.office-preview-notice {
  text-align: center;
  padding: 20px;
}

.office-preview-notice p {
  margin-bottom: 15px;
  font-size: 16px;
  color: #666;
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

