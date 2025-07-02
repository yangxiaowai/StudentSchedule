<template>
  <div class="task-manager">
    <div class="header-section">
      <h2>任务管理</h2>
      <div class="header-buttons">
        <button @click="showModal = true">
          <i class="fas fa-plus"></i> 添加任务
        </button>
        <button 
          v-if="selectedTasks.length > 0" 
          @click="goToAnalysis" 
          class="analysis-btn"
        >
          <i class="fas fa-chart-line"></i> 分析选中任务 ({{ selectedTasks.length }})
        </button>
      </div>
    </div>

    <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>任务名称</th>
              <th>学科</th>
              <th>内容</th>
              <th>内容类型</th>
              <th>开始时间</th>
              <th>结束时间</th>
              <th>进度</th>
              <th>附件</th>
              <th>操作</th>
              <th>选择分析</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in tasks" :key="task.id">
              <td>{{ task.name }}</td>
              <td>{{ task.subject }}</td>
              <td>{{ task.content }}</td>
              <td>{{ getContentTypeLabel(task.contentType || task.type) }}</td>
              <td>{{ formatDateTime(task.startTime) || '未设置' }}</td>
              <td>{{ formatDateTime(task.endTime) || '未设置' }}</td>
              <td class="progress-cell">
                <div class="progress-container">
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{width: (task.progress || 0) + '%'}"></div>
                  </div>
                  <span class="progress-text">{{ task.progress || 0 }}%</span>
                </div>
              </td>
              <td class="file-cell">
                <button v-if="task.fileUrl" class="icon-button file" title="预览文件" @click="openFile(task.fileUrl, task)">
                  <i class="fas fa-file-alt"></i>
                  {{ task.fileName || '附件' }}
                </button>
                <span v-else class="no-file">无附件</span>
              </td>
              <td class="actions-cell">
                <button class="icon-button edit" title="编辑" @click="editTask(task)">
                  <i class="fas fa-edit"></i>
                </button>
                <button class="icon-button delete" title="删除" @click="deleteTask(task.id)">
                  <i class="fas fa-trash-alt"></i>
                </button>
              </td>
              <td class="select-cell">
                <input 
                  type="checkbox" 
                  v-model="selectedTasks" 
                  :value="task" 
                  class="task-checkbox"
                />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 添加任务弹窗 -->
      <div v-if="showModal" class="modal" @click.self="showModal = false">
        <div class="modal-content">
          <h3>添加新任务</h3>
          <form @submit.prevent="addTask">
            <div class="form-group" :class="{ 'has-error': formErrors.name }">
               <label>任务名称 <span class="required">*</span></label>
               <input 
                 v-model="newTask.name" 
                 placeholder="请输入任务名称" 
                 :class="{ 'error': formErrors.name }"
                 @focus="formErrors.name = null"
                 required
               >
               <span class="error-message" v-if="formErrors.name">{{ formErrors.name }}</span>
             </div>
             
             <div class="form-group" :class="{ 'has-error': formErrors.subject }">
               <label>学科 <span class="required">*</span></label>
               <select 
                 v-model="newTask.subject" 
                 :class="{ 'error': formErrors.subject }"
                 @focus="formErrors.subject = null"
                 required
               >
                 <option disabled value="">请选择学科</option>
                 <option value="chinese">语文</option>
                 <option value="math">数学</option>
                 <option value="english">英语</option>
                 <option value="physics">物理</option>
                 <option value="chemistry">化学</option>
                 <option value="biology">生物</option>
                 <option value="politics">政治</option>
                 <option value="history">历史</option>
                 <option value="geography">地理</option>
                 <option value="other">其他</option>
               </select>
               <span class="error-message" v-if="formErrors.subject">{{ formErrors.subject }}</span>
             </div>
             
             <div class="form-group" :class="{ 'has-error': formErrors.content }">
               <label>内容 <span class="required">*</span></label>
               <textarea 
                 v-model="newTask.content" 
                 placeholder="请输入任务内容" 
                 :class="{ 'error': formErrors.content }"
                 @focus="formErrors.content = null"
                 required
               ></textarea>
               <span class="error-message" v-if="formErrors.content">{{ formErrors.content }}</span>
             </div>
             
             <div class="form-row">
               <div class="form-group half">
                 <label>开始时间 <span class="required">*</span></label>
                 <input 
                   type="datetime-local" 
                   v-model="newTask.startTime"
                   :class="{ 'error': formErrors.startTime }"
                   @focus="formErrors.startTime = null"
                 >
                 <span class="error-message" v-if="formErrors.startTime">{{ formErrors.startTime }}</span>
               </div>
               
               <div class="form-group half" :class="{ 'has-error': formErrors.endTime }">
                 <label>截止时间 <span class="required">*</span></label>
                 <input 
                   type="datetime-local" 
                   v-model="newTask.endTime"
                   :class="{ 'error': formErrors.endTime }"
                   @focus="formErrors.endTime = null"
                 >
                 <span class="error-message" v-if="formErrors.endTime">{{ formErrors.endTime }}</span>
               </div>
             </div>
            
            <div class="form-group" :class="{ 'has-error': formErrors.type }">
              <label>内容类型 <span class="required">*</span></label>
              <select 
                v-model="newTask.type"
                :class="{ 'error': formErrors.type }"
                @focus="formErrors.type = null"
                required
              >
                <option disabled value="">请选择内容类型</option>
                <option value="textbook">教材</option>
                <option value="notes">笔记</option>
                <option value="exam">真题</option>
                <option value="exercise">习题</option>
                <option value="ppt">课件</option>
              </select>
              <span class="error-message" v-if="formErrors.type">{{ formErrors.type }}</span>
            </div>

            <div class="form-group">
              <label>进度</label>
              <div class="progress-input">
                <input type="range" v-model="newTask.progress" min="0" max="100" step="5">
                <span class="progress-value">{{ newTask.progress || 0 }}%</span>
              </div>
            </div>
            
            <div class="form-group">
              <label>备注</label>
              <textarea v-model="newTask.remark" placeholder="请输入备注信息"></textarea>
            </div>
            
            <div class="form-group">
              <label>附件</label>
              <div class="file-upload">
                <i class="fas fa-cloud-upload-alt"></i>
                <span>点击或拖拽文件到此处上传</span>
                <input type="file" @change="handleFileUpload">
              </div>
              <div v-if="uploadedFileInfo" class="uploaded-file-info">
                <i class="fas fa-file-alt"></i>
                <span>{{ uploadedFileInfo.originalName }}</span>
                <button type="button" @click="removeUploadedFile" class="remove-file-btn">
                  <i class="fas fa-times"></i>
                </button>
              </div>
            </div>
            
            <div class="form-actions">
              <div class="form-actions-content">
                <button type="button" @click="showModal = false" class="btn-secondary">
                  <i class="fas fa-times"></i> 取消
                </button>
                <button type="submit" class="btn-primary">
                  <i class="fas fa-check"></i> 保存
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { uploadForTask } from '@/utils/fileUpload'

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 将内容类型英文值转换为中文显示
function getContentTypeLabel(type) {
  const typeMap = {
    'textbook': '教材',
    'notes': '笔记', 
    'exam': '真题',
    'exercise': '习题',
    'ppt': '课件'
  }
  return typeMap[type] || type || '未设置'
}

const router = useRouter()

onMounted(() => {
  fetchTasks()
  
  // 监听来自DataIntegration的进度更新事件
  const handleProgressUpdate = (event) => {
    const { taskId, progress } = event.detail
    console.log('TaskManager: 接收到进度更新事件', { taskId, progress })
    
    // 更新本地任务数据
    const taskIndex = tasks.value.findIndex(t => t.id === taskId)
    if (taskIndex !== -1) {
      tasks.value[taskIndex].progress = progress
      console.log(`TaskManager: 本地任务 ${taskId} 进度已更新为 ${progress}%`)
    }
  }
  
  window.addEventListener('taskProgressUpdated', handleProgressUpdate)
})

const showModal = ref(false)
const isEditing = ref(false)
const tasks = ref([])
const selectedTasks = ref([])
const newTask = ref({
  name: '',
  subject: '',
  content: '',
  startTime: '',
  endTime: '',
  progress: 0,
  type: '',
  remark: '',
  file: null,
  isCompleted: false
})

const formErrors = ref({})

const validateForm = () => {
  formErrors.value = {}
  
  if (!newTask.value.name?.trim()) {
    formErrors.value.name = '请输入任务名称'
  }
  
  if (!newTask.value.subject) {
    formErrors.value.subject = '请选择学科'
  }
  
  if (!newTask.value.content?.trim()) {
    formErrors.value.content = '请输入任务内容'
  }
  
  // 添加内容类型验证
  if (!newTask.value.type) {
    formErrors.value.type = '请选择内容类型'
  }
  
  if (!newTask.value.startTime) {
    formErrors.value.startTime = '请选择开始时间'
  }
  
  if (!newTask.value.endTime) {
    formErrors.value.endTime = '请选择截止时间'
  }
  
  if (newTask.value.startTime && newTask.value.endTime) {
    const start = new Date(newTask.value.startTime)
    const end = new Date(newTask.value.endTime)
    if (end < start) {
      formErrors.value.endTime = '截止时间不能早于开始时间'
    }
  }
  
  return Object.keys(formErrors.value).length === 0
}

async function fetchTasks() {
  try {
    const token = localStorage.getItem('accessToken')
    const response = await fetch('http://localhost:8080/api/tasks', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (response.ok) {
      const data = await response.json()
      tasks.value = data.filter(task => task !== null && task !== undefined)
    }
  } catch (error) {
    console.error('获取任务列表失败:', error)
  }
}

async function addTask() {
  if (!validateForm()) return
  
  // 创建任务数据对象
  const taskData = {
    name: newTask.value.name.trim(),
    subject: newTask.value.subject,
    content: newTask.value.content.trim(),
    startTime: newTask.value.startTime ? new Date(newTask.value.startTime).toISOString().slice(0, 16) : '',
    endTime: newTask.value.endTime ? new Date(newTask.value.endTime).toISOString().slice(0, 16) : '',
    type: newTask.value.type || '',
    remark: newTask.value.remark?.trim() || '',
    progress: newTask.value.progress || 0,
    isCompleted: newTask.value.isCompleted || false
  }

  const formData = new FormData()
  
  // 添加任务数据
  Object.entries(taskData).forEach(([key, value]) => {
    // 确保所有值都被转换为字符串，包括布尔值和数字
    const stringValue = value === null || value === undefined ? '' :
      typeof value === 'boolean' ? String(value) :
      typeof value === 'number' ? String(value) :
      String(value).trim()
    formData.append(key, stringValue)
  })
  
  // 如果有文件信息，添加到formData
  if (uploadedFileInfo.value) {
    // 如果是新上传的文件，文件信息已经在uploadedFileInfo中
    formData.append('fileName', uploadedFileInfo.value.fileName)
    formData.append('fileUrl', uploadedFileInfo.value.fileUrl)
  } else if (newTask.value.file) {
    // 兼容原有的文件上传方式
    formData.append('file', newTask.value.file)
  }

  try {
    const token = localStorage.getItem('accessToken')
    const url = isEditing.value 
      ? `http://localhost:8080/api/tasks/${newTask.value.id}`
      : 'http://localhost:8080/api/tasks'

    const response = await fetch(url, {
      method: isEditing.value ? 'PUT' : 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        // 不要设置Content-Type，让浏览器自动设置正确的multipart/form-data
      },
      body: formData
    })

    // 首先检查响应状态
    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(errorText || `服务器错误 (${response.status})`)
    }

    const responseText = await response.text()
    let result
    try {
      // 检查响应文本是否为空
      if (!responseText.trim()) {
        throw new Error('服务器返回了空响应，请检查服务器日志')
      }
      result = JSON.parse(responseText)
    } catch (e) {
      console.error('Response parsing error:', e, '\nResponse text:', responseText)
      throw new Error('服务器响应格式错误: ' + (responseText.trim().substring(0, 100) || e.message))
    }

      if (response.ok) {
        if (!isEditing.value) {
          tasks.value.push(result)
        } else {
          const index = tasks.value.findIndex(t => t.id === result.id)
          if (index !== -1) {
            tasks.value[index] = result
          }
        }
        showModal.value = false
        resetForm()
      } else {
        throw new Error(result.message || '保存失败，请稍后重试')
      }
  } catch (error) {
    console.error('保存任务失败:', error)
    alert(error.message || '保存失败，请稍后重试')
  }
}

function resetForm() {
  Object.assign(newTask.value, {
    name: '', subject: '', content: '', startTime: '', endTime: '',
    type: '', remark: '', file: null, progress: 0, isCompleted: false,
    fileName: '', fileUrl: ''
  })
  formErrors.value = {}
  isEditing.value = false
  uploadedFileInfo.value = null
  
  // 清除文件输入框
  const fileInput = document.querySelector('input[type="file"]')
  if (fileInput) {
    fileInput.value = ''
  }
}

function editTask(task) {
  isEditing.value = true
  // 格式化日期时间为本地格式
  const formattedTask = {
    ...task,
    startTime: task.startTime ? new Date(task.startTime).toISOString().slice(0, 16) : '',
    endTime: task.endTime ? new Date(task.endTime).toISOString().slice(0, 16) : ''
  }
  Object.assign(newTask.value, formattedTask)
  
  // 如果任务有文件信息，设置uploadedFileInfo
  if (task.fileName && task.fileUrl) {
    uploadedFileInfo.value = {
      fileName: task.fileName,
      fileUrl: task.fileUrl,
      originalName: task.fileName
    }
  } else {
    uploadedFileInfo.value = null
  }
  
  showModal.value = true
}

async function deleteTask(taskId) {
  if (!confirm('确定要删除这个任务吗？')) return
  
  try {
    const token = localStorage.getItem('accessToken')
    const response = await fetch(`http://localhost:8080/api/tasks/${taskId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.ok) {
      await fetchTasks()
    } else {
      throw new Error('删除失败')
    }
  } catch (error) {
    console.error('删除任务失败:', error)
    alert('删除失败：' + error.message)
  }
}

// 文件上传处理
const uploadedFileInfo = ref(null)

const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证是否已选择学科和内容类型
  if (!newTask.value.subject) {
    alert('请先选择学科再上传文件')
    event.target.value = ''
    return
  }
  
  if (!newTask.value.type) {
    alert('请先选择内容类型再上传文件')
    event.target.value = ''
    return
  }

  if (file.size > 10 * 1024 * 1024) {
    alert('文件大小不能超过10MB')
    event.target.value = ''
    return
  }

  try {
    // 使用统一的文件上传API（与资料库相同）
    const formData = new FormData();
    formData.append('file', file);
    // 传递用户选择的学科，如果未选择则默认为'other'
    formData.append('subject', newTask.value.subject || 'other');
    // 传递用户选择的内容类型，如果未选择则默认为'other'
    formData.append('type', newTask.value.type || 'other');

    const token = localStorage.getItem('accessToken');
    const response = await fetch('/api/files/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    });

    if (!response.ok) {
      throw new Error('文件上传失败');
    }

    const result = await response.json();
    
    // 保存上传结果
    uploadedFileInfo.value = {
      fileName: result.fileName,
      fileUrl: result.fileDownloadUri,
      originalName: file.name
    }
    
    // 将文件信息添加到任务对象
    newTask.value.fileName = result.fileName
    newTask.value.fileUrl = result.fileDownloadUri
    
    alert('文件上传成功！')
  } catch (error) {
    console.error('文件上传失败:', error)
    alert('文件上传失败: ' + error.message)
    event.target.value = ''
  }
}

// 打开文件 - 跳转到资料库预览
const openFile = async (fileUrl, task) => {
  if (!fileUrl) return
  
  // 从fileUrl中提取文件名
  const fileName = fileUrl.split('/').pop()
  if (!fileName) {
    alert('无法获取文件信息')
    return
  }
  
  try {
    // 跳转到资料库页面
    await router.push('/data-integration')
    
    // 使用nextTick确保DOM完全渲染后再触发事件
     await nextTick()
     
     // 等待DataIntegration组件完全加载的函数
      const waitForDataIntegration = () => {
        return new Promise((resolve) => {
          let attempts = 0;
          const maxAttempts = 30; // 最多尝试30次，每次100ms
          
          const checkInterval = setInterval(() => {
            attempts++;
            console.log(`TaskManager: 检查DataIntegration组件加载状态 (${attempts}/${maxAttempts})`);
            
            // 检查DataIntegration组件是否已加载（通过检查特定元素）
            const dataIntegrationElement = document.querySelector('.data-integration-container') || 
                                         document.querySelector('[data-component="data-integration"]') ||
                                         document.querySelector('.materials-grid');
            
            if (dataIntegrationElement) {
              console.log('TaskManager: DataIntegration组件已检测到');
              clearInterval(checkInterval);
              resolve(true);
            } else if (attempts >= maxAttempts) {
              console.log('TaskManager: 达到最大检测次数，继续执行');
              clearInterval(checkInterval);
              resolve(false);
            }
          }, 100);
        });
      };
     
     // 等待组件加载完成后触发事件
      const componentDetected = await waitForDataIntegration();
      
      // 根据检测结果调整延迟时间
      const delay = componentDetected ? 300 : 1000;
      console.log(`TaskManager: 组件检测${componentDetected ? '成功' : '失败'}，将在${delay}ms后触发事件`);
      
      // 延迟触发事件确保事件监听器已设置
      setTimeout(() => {
        console.log('TaskManager: 准备触发previewTaskFile事件', {
          fileName: fileName,
          taskName: task.name,
          fileUrl: fileUrl
        })
        
        const event = new CustomEvent('previewTaskFile', {
          detail: {
            fileName: fileName,
            originalFileName: task.fileName, // 原始文件名
            taskName: task.name,
            fileUrl: fileUrl,
            taskId: task.id // 任务ID用于更新进度
          }
        })
        window.dispatchEvent(event)
        console.log('TaskManager: previewTaskFile事件已触发')
        
        // 如果事件触发后1秒内没有响应，再次尝试
        setTimeout(() => {
           console.log('TaskManager: 备用事件触发机制');
           const backupEvent = new CustomEvent('previewTaskFile', {
              detail: {
                fileName: fileName,
                originalFileName: task.fileName, // 原始文件名
                taskName: task.name,
                fileUrl: fileUrl,
                taskId: task.id // 任务ID用于更新进度
              }
            });
           window.dispatchEvent(backupEvent);
         }, 1000);
      }, delay)
    
  } catch (error) {
    console.error('跳转到资料库失败:', error)
    alert('跳转到资料库失败: ' + error.message)
  }
}




// 移除已上传的文件
const removeUploadedFile = () => {
  uploadedFileInfo.value = null
  newTask.value.fileName = ''
  newTask.value.fileUrl = ''
  
  // 清除文件输入框
  const fileInput = document.querySelector('input[type="file"]')
  if (fileInput) {
    fileInput.value = ''
  }
}

// 创建模态框的通用方法
const createModal = (title) => {
  const modal = document.createElement('div')
  modal.className = 'file-preview-modal'
  modal.style.position = 'fixed'
  modal.style.top = '0'
  modal.style.left = '0'
  modal.style.width = '100%'
  modal.style.height = '100%'
  modal.style.backgroundColor = 'rgba(0,0,0,0.8)'
  modal.style.zIndex = '1000'
  modal.style.display = 'flex'
  modal.style.justifyContent = 'center'
  modal.style.alignItems = 'center'

  const content = document.createElement('div')
  content.className = 'modal-content'
  content.style.backgroundColor = 'white'
  content.style.padding = '20px'
  content.style.borderRadius = '8px'
  content.style.width = '90%'
  content.style.maxWidth = '900px'
  content.style.maxHeight = '90vh'
  content.style.overflow = 'auto'
  content.style.position = 'relative'

  const closeBtn = document.createElement('button')
  closeBtn.textContent = '×'
  closeBtn.style.position = 'absolute'
  closeBtn.style.top = '10px'
  closeBtn.style.right = '10px'
  closeBtn.style.background = 'none'
  closeBtn.style.border = 'none'
  closeBtn.style.fontSize = '24px'
  closeBtn.style.cursor = 'pointer'
  closeBtn.onclick = () => document.body.removeChild(modal)

  const titleElement = document.createElement('h3')
  titleElement.textContent = title
  titleElement.style.marginTop = '0'

  content.appendChild(closeBtn)
  content.appendChild(titleElement)
  modal.appendChild(content)

  document.body.appendChild(modal)
  return modal
}

const createLoadingModal = (fileName) => {
  const modal = document.createElement('div')
  modal.className = 'loading-modal'
  modal.style.position = 'fixed'
  modal.style.top = '0'
  modal.style.left = '0'
  modal.style.width = '100%'
  modal.style.height = '100%'
  modal.style.backgroundColor = 'rgba(0, 0, 0, 0.5)'
  modal.style.zIndex = '10001'
  modal.style.display = 'flex'
  modal.style.alignItems = 'center'
  modal.style.justifyContent = 'center'
  
  const content = document.createElement('div')
  content.className = 'loading-content'
  content.style.background = 'white'
  content.style.padding = '30px'
  content.style.borderRadius = '8px'
  content.style.textAlign = 'center'
  content.style.boxShadow = '0 4px 20px rgba(0, 0, 0, 0.3)'
  content.style.position = 'relative'
  
  // 添加关闭按钮
  const closeBtn = document.createElement('button')
  closeBtn.textContent = '×'
  closeBtn.style.position = 'absolute'
  closeBtn.style.top = '10px'
  closeBtn.style.right = '10px'
  closeBtn.style.background = 'none'
  closeBtn.style.border = 'none'
  closeBtn.style.fontSize = '20px'
  closeBtn.style.cursor = 'pointer'
  closeBtn.style.color = '#999'
  closeBtn.style.width = '30px'
  closeBtn.style.height = '30px'
  closeBtn.style.borderRadius = '50%'
  closeBtn.style.display = 'flex'
  closeBtn.style.alignItems = 'center'
  closeBtn.style.justifyContent = 'center'
  closeBtn.onmouseover = () => {
    closeBtn.style.backgroundColor = '#f0f0f0'
    closeBtn.style.color = '#333'
  }
  closeBtn.onmouseout = () => {
    closeBtn.style.backgroundColor = 'transparent'
    closeBtn.style.color = '#999'
  }
  closeBtn.onclick = () => {
    if (modal && modal.parentNode) {
      document.body.removeChild(modal)
    }
  }
  
  const spinner = document.createElement('div')
  spinner.className = 'loading-spinner'
  spinner.style.width = '40px'
  spinner.style.height = '40px'
  spinner.style.border = '4px solid #f3f3f3'
  spinner.style.borderTop = '4px solid #3498db'
  spinner.style.borderRadius = '50%'
  spinner.style.animation = 'spin 1s linear infinite'
  spinner.style.margin = '0 auto 15px'
  
  const text = document.createElement('p')
  text.textContent = `正在预览 ${fileName}...`
  text.style.margin = '0 0 10px 0'
  
  const subText = document.createElement('p')
  subText.textContent = '请稍候，预览可能需要几秒钟'
  subText.style.fontSize = '12px'
  subText.style.color = '#666'
  subText.style.margin = '0 0 15px 0'
  
  const cancelText = document.createElement('p')
  cancelText.textContent = '点击右上角 × 可取消预览'
  cancelText.style.fontSize = '11px'
  cancelText.style.color = '#999'
  cancelText.style.margin = '0'
  
  // 添加旋转动画样式
  const style = document.createElement('style')
  style.textContent = `
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `
  document.head.appendChild(style)
  
  content.appendChild(closeBtn)
  content.appendChild(spinner)
  content.appendChild(text)
  content.appendChild(subText)
  content.appendChild(cancelText)
  modal.appendChild(content)
  
  document.body.appendChild(modal)
  return modal
}

// PDF文件预览
const previewPdfFile = async (previewData, task) => {
  try {
    // 创建预览模态框
    const modal = createModal(previewData.fileName)
    const content = modal.querySelector('.modal-content')
    
    // 创建PDF容器
    const pdfContainer = document.createElement('div')
    pdfContainer.className = 'pdf-container'
    pdfContainer.style.textAlign = 'center'
    content.appendChild(pdfContainer)
    
    // 添加下载链接
    const downloadLink = document.createElement('a')
    downloadLink.href = task.fileUrl || `/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}`
    downloadLink.className = 'download-link'
    downloadLink.textContent = '下载原文件'
    downloadLink.target = '_blank'
    downloadLink.style.display = 'inline-block'
    downloadLink.style.marginTop = '15px'
    downloadLink.style.marginBottom = '15px'
    pdfContainer.appendChild(downloadLink)
    
    // 如果是多页面文档
    if (previewData.multiPage) {
      // 分割页面内容
      const pageImages = previewData.content.split(',')
      
      // 创建页面导航
      const pageNav = document.createElement('div')
      pageNav.className = 'page-navigation'
      pageNav.style.marginBottom = '15px'
      pageNav.style.display = 'flex'
      pageNav.style.justifyContent = 'center'
      pageNav.style.gap = '10px'
      
      // 添加页面计数器
      const pageCounter = document.createElement('div')
      pageCounter.className = 'page-counter'
      pageCounter.style.margin = '0 10px'
      pageCounter.style.lineHeight = '30px'
      
      // 添加上一页按钮
      const prevBtn = document.createElement('button')
      prevBtn.textContent = '上一页'
      prevBtn.style.padding = '5px 10px'
      prevBtn.style.cursor = 'pointer'
      
      // 添加下一页按钮
      const nextBtn = document.createElement('button')
      nextBtn.textContent = '下一页'
      nextBtn.style.padding = '5px 10px'
      nextBtn.style.cursor = 'pointer'
      
      pageNav.appendChild(prevBtn)
      pageNav.appendChild(pageCounter)
      pageNav.appendChild(nextBtn)
      pdfContainer.appendChild(pageNav)
      
      // 创建图片容器
      const imageContainer = document.createElement('div')
      imageContainer.className = 'pdf-image-container'
      imageContainer.style.maxWidth = '100%'
      imageContainer.style.margin = '0 auto'
      pdfContainer.appendChild(imageContainer)
      
      // 当前页码
      let currentPage = 0
      let maxPageReached = 0 // 记录用户浏览过的最大页码
      
      // 显示指定页面
      const showPage = (pageIndex) => {
        // 清空容器
        imageContainer.innerHTML = ''
        
        // 更新页码显示
        pageCounter.textContent = `第 ${pageIndex + 1} 页 / 共 ${pageImages.length} 页`
        
        // 创建图片元素
        const img = document.createElement('img')
        img.src = `data:image/png;base64,${pageImages[pageIndex]}`
        img.style.maxWidth = '100%'
        img.style.boxShadow = '0 2px 5px rgba(0,0,0,0.2)'
        imageContainer.appendChild(img)
        
        // 更新按钮状态
        prevBtn.disabled = pageIndex === 0
        nextBtn.disabled = pageIndex === pageImages.length - 1
        
        // 更新最大浏览页码
        if (pageIndex > maxPageReached) {
          maxPageReached = pageIndex
          // 计算进度百分比（基于浏览的页数）
          const progress = Math.round(((maxPageReached + 1) / pageImages.length) * 100)
          // 更新任务进度
          updateTaskProgress(task.id, progress)
        }
      }
      
      // 显示第一页
      showPage(currentPage)
      
      // 初始化时设置第一页的进度
      maxPageReached = 0
      const initialProgress = Math.round(((maxPageReached + 1) / pageImages.length) * 100)
      console.log(`TaskManager PDF初始进度: ${initialProgress}% (已浏览 ${maxPageReached + 1}/${pageImages.length} 页)`)
      updateTaskProgress(task.id, initialProgress)
      
      // 绑定按钮事件
      prevBtn.addEventListener('click', () => {
        if (currentPage > 0) {
          currentPage--
          showPage(currentPage)
        }
      })
      
      nextBtn.addEventListener('click', () => {
        if (currentPage < pageImages.length - 1) {
          currentPage++
          showPage(currentPage)
        }
      })
      
    } else {
      // 单页PDF（兼容旧版本）
      const img = document.createElement('img')
      img.src = `data:image/png;base64,${previewData.content}`
      img.style.maxWidth = '100%'
      img.style.boxShadow = '0 2px 5px rgba(0,0,0,0.2)'
      pdfContainer.appendChild(img)
      
      // 单页PDF直接设置为100%完成
      updateTaskProgress(task.id, 100)
    }
    
  } catch (error) {
    console.error('PDF渲染失败:', error)
    const modal = createModal(previewData.fileName)
    modal.querySelector('.modal-content').innerHTML = `
      <p>PDF预览失败: ${error.message}</p>
      <a href="/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}"
         target="_blank" class="download-link">
        下载文件
      </a>
    `
  }
}

// 更新任务进度
const updateTaskProgress = async (taskId, progress) => {
  try {
    const token = localStorage.getItem('accessToken')
    console.log(`TaskManager: 准备更新任务 ${taskId} 进度为 ${progress}%`)
    console.log(`TaskManager: 使用token: ${token ? '已获取' : '未获取'}`)
    
    const response = await fetch(`/api/tasks/${taskId}/progress`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ progress })
    })
    
    console.log(`TaskManager: API响应状态: ${response.status}`)
    
    if (response.ok) {
      const result = await response.json()
      // 更新本地任务数据
      const taskIndex = tasks.value.findIndex(t => t.id === taskId)
      if (taskIndex !== -1) {
        tasks.value[taskIndex].progress = progress
        console.log(`TaskManager: 本地任务数据已更新`)
      }
      console.log(`TaskManager: 任务 ${taskId} 进度已更新为 ${progress}%`, result)
    } else {
      const errorText = await response.text()
      console.error('TaskManager: 更新任务进度失败:', response.status, response.statusText, errorText)
    }
  } catch (error) {
    console.error('TaskManager: 更新任务进度网络错误:', error)
  }
}

// 文本文件预览
const previewTextFile = async (previewData) => {
  const modal = createModal(previewData.fileName)
  const content = modal.querySelector('.modal-content')
  
  const textContainer = document.createElement('div')
  textContainer.style.maxHeight = '600px'
  textContainer.style.overflow = 'auto'
  textContainer.style.whiteSpace = 'pre-wrap'
  textContainer.style.fontFamily = 'monospace'
  textContainer.style.fontSize = '14px'
  textContainer.style.lineHeight = '1.5'
  textContainer.style.padding = '15px'
  textContainer.style.backgroundColor = '#f8f9fa'
  textContainer.style.border = '1px solid #e9ecef'
  textContainer.style.borderRadius = '4px'
  
  textContainer.textContent = previewData.content
  content.appendChild(textContainer)
}

// Office文件预览
const previewOfficeFile = async (previewData) => {
  const modal = createModal(previewData.fileName)
  const content = modal.querySelector('.modal-content')
  
  if (previewData.content && previewData.content.trim()) {
    const docContainer = document.createElement('div')
    docContainer.style.maxHeight = '600px'
    docContainer.style.overflow = 'auto'
    docContainer.style.fontFamily = 'Arial, sans-serif'
    docContainer.style.lineHeight = '1.6'
    
    try {
      const htmlContent = atob(previewData.content)
      docContainer.innerHTML = htmlContent
    } catch (error) {
      docContainer.textContent = previewData.content
    }
    
    content.appendChild(docContainer)
  } else {
    content.innerHTML = `
      <div style="text-align: center; padding: 20px;">
        <p>无法预览此Office文件</p>
        <a href="/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}"
           target="_blank" style="display: inline-block; margin-top: 15px; padding: 8px 16px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px;">
          下载文件
        </a>
      </div>
    `
  }
}

// Excel文件预览
const previewExcelFile = async (previewData) => {
  const modal = createModal(previewData.fileName)
  const content = modal.querySelector('.modal-content')
  
  content.innerHTML = `
    <div style="text-align: center; padding: 20px;">
      <p>Excel文件预览功能开发中</p>
      <a href="/api/files/download?fileName=${encodeURIComponent(previewData.fileName)}"
         target="_blank" style="display: inline-block; margin-top: 15px; padding: 8px 16px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px;">
        下载文件
      </a>
    </div>
  `
}

// 图片文件预览
const previewImageFile = async (previewData) => {
  const modal = createModal(previewData.fileName)
  const content = modal.querySelector('.modal-content')
  
  const img = document.createElement('img')
  img.src = `data:image/${previewData.fileType};base64,${previewData.content}`
  img.style.maxWidth = '100%'
  img.style.maxHeight = '70vh'
  img.style.objectFit = 'contain'
  
  content.appendChild(img)
}

// 跳转到AI分析页面
const goToAnalysis = () => {
  if (selectedTasks.value.length === 0) {
    alert('请先选择要分析的任务')
    return
  }
  
  console.log('原始选中任务:', selectedTasks.value)
  
  // 只提取必要的字段，确保ID是数字类型
  const simplifiedTasks = selectedTasks.value.map(task => {
    console.log('处理任务:', task, '原始任务ID:', task.id, '类型:', typeof task.id)
    
    // 确保ID是数字类型
    let taskId = task.id
    if (typeof taskId === 'string') {
      taskId = parseInt(taskId, 10)
      if (isNaN(taskId)) {
        console.error('无法将任务ID转换为数字:', task.id)
        taskId = null
      }
    }
    
    if (!taskId) {
      console.error('任务缺少有效ID:', task)
    }
    
    // 只返回必要的字段
    return {
      id: taskId,
      name: task.name,
      subject: task.subject,
      startTime: task.startTime,
      endTime: task.endTime,
      progress: task.progress
    }
  }).filter(task => task.id) // 过滤掉没有有效ID的任务
  
  console.log('简化后的任务数据:', simplifiedTasks)
  
  if (simplifiedTasks.length === 0) {
    alert('没有有效的任务可供分析')
    return
  }
  
  // 将选中的任务转换为JSON字符串
  const tasksJson = JSON.stringify(simplifiedTasks)
  console.log('转换为JSON后:', tasksJson)
  
  // 将选中的任务存储到localStorage
  localStorage.setItem('selectedTasksForAnalysis', tasksJson)
  
  // 验证存储是否成功
  const storedData = localStorage.getItem('selectedTasksForAnalysis')
  console.log('验证存储的数据:', storedData)
  
  // 跳转到AI分析页面
  window.location.href = '/ai-analysis'
}
</script>

<style scoped>
.task-manager {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  background: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-buttons {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.task-manager h2 {
  color: #2c3e50;
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
}

button {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 0.8rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  min-width: 100px;
}

.progress-input {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.progress-input input[type="range"] {
  flex: 1;
  height: 6px;
  -webkit-appearance: none;
  background: #e9ecef;
  border-radius: 3px;
  outline: none;
  border: none;
  padding: 0;
}

.progress-input input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  background: #4CAF50;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;
}

.progress-input input[type="range"]::-webkit-slider-thumb:hover {
  transform: scale(1.1);
}

.progress-value {
  min-width: 48px;
  text-align: right;
  font-weight: 500;
  color: #4CAF50;
}

button:hover {
  background: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
}

.analysis-btn {
  background: linear-gradient(135deg, #2196F3, #1976D2);
  color: white;
  border: none;
  padding: 0.8rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  min-width: 120px;
}

.analysis-btn:hover {
  background: linear-gradient(135deg, #1976D2, #1565C0);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
}

.icon-button {
  background: transparent;
  padding: 0.5rem;
  border-radius: 4px;
  color: #6c757d;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
}

.icon-button:hover {
  transform: translateY(-1px);
  box-shadow: none;
}

.icon-button.edit:hover {
  color: #2196F3;
  background: rgba(33, 150, 243, 0.1);
}

.icon-button.delete:hover {
  color: #dc3545;
  background: rgba(220, 53, 69, 0.1);
}

.icon-button i {
  font-size: 1rem;
}

.actions-cell {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-start;
  align-items: center;
}

.table-container {
  overflow-x: auto;
  margin: 1rem 0;
  padding: 0;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  background: white;
}

.progress-cell {
  padding-bottom: 1.5rem !important;
}

.progress-text {
  font-weight: 500;
  color: #2c3e50;
}

table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  margin: 0;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

th {
  background: #f1f3f5;
  color: #495057;
  font-weight: 600;
  padding: 1rem;
  text-align: left;
  border-bottom: 2px solid #e9ecef;
}

td {
  padding: 1rem;
  border-bottom: 1px solid #e9ecef;
  color: #495057;
}

tr:hover {
  background: #f8f9fa;
}

.select-cell {
  text-align: center;
  padding: 1rem;
}

.task-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #4CAF50;
  transform: scale(1.2);
}

.task-checkbox:hover {
  transform: scale(1.3);
}

.modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  backdrop-filter: blur(4px);
  padding: 1rem;
  overflow-y: auto;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  max-height: calc(100vh - 2rem);
  overflow-y: auto;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  margin: 1rem auto;
  position: relative;
}

.modal-content h3 {
  color: #2c3e50;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
  font-weight: 600;
}

.modal-content form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  margin-bottom: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group.half {
  margin: 0;
}

.file-upload {
  border: 2px dashed #e9ecef;
  padding: 1.5rem;
  text-align: center;
  cursor: pointer;
  position: relative;
  border-radius: 6px;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.file-upload:hover {
  border-color: #4CAF50;
  background: #f1f8f1;
}

.file-upload i {
  font-size: 2rem;
  color: #4CAF50;
  margin-bottom: 0.5rem;
}

.file-upload span {
  display: block;
  color: #6c757d;
}

.file-upload input[type="file"] {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.modal-content input,
.modal-content select,
.modal-content textarea {
  padding: 0.8rem;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.modal-content input:focus,
.modal-content select:focus,
.modal-content textarea:focus {
  outline: none;
  border-color: #4CAF50;
}

.required {
  color: #dc3545;
}

.has-error input,
.has-error select,
.has-error textarea,
input.error,
select.error,
textarea.error {
  border-color: #dc3545;
  background-color: #fff8f8;
}

.error-message {
  color: #dc3545;
  font-size: 0.8rem;
  margin-top: 0.25rem;
  display: block;
}

.modal-content textarea {
  min-height: 100px;
  resize: vertical;
}

.form-actions {
  position: sticky;
  bottom: -1.5rem;
  background: white;
  margin: 1rem -1.5rem -1.5rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid #e9ecef;
  z-index: 1;
}

.form-actions-content {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.modal-content button[type="submit"] {
  background: #4CAF50;
}

.modal-content button[type="button"] {
  background: #6c757d;
}

.modal-content button[type="button"]:hover {
  background: #5a6268;
  box-shadow: 0 4px 12px rgba(108, 117, 125, 0.2);
}

input[type="file"] {
  border: 2px dashed #e9ecef;
  padding: 1.5rem;
  text-align: center;
  cursor: pointer;
}

input[type="file"]:hover {
  border-color: #4CAF50;
  background: #f8f9fa;
}

/* 文件相关样式 */
.file-cell {
  text-align: center;
}

.icon-button.file {
  background: #17a2b8;
  color: white;
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  font-size: 0.8rem;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.icon-button.file:hover {
  background: #138496;
  transform: translateY(-1px);
}

.no-file {
  color: #6c757d;
  font-style: italic;
  font-size: 0.9rem;
}

.uploaded-file-info {
  margin-top: 0.5rem;
  padding: 0.75rem; /* 增加内边距 */
  background: #e8f5e8;
  border: 1px solid #4CAF50;
  border-radius: 6px; /* 增加圆角 */
  display: flex;
  align-items: center;
  gap: 0.75rem; /* 增加间距 */
  font-size: 0.9rem;
  position: relative; /* 确保定位正确 */
}

.uploaded-file-info i {
  color: #4CAF50;
}

.remove-file-btn {
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;  /* 增加尺寸 */
  height: 24px; /* 增加尺寸 */
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 0.8rem; /* 稍微增大字体 */
  margin-left: auto;
  z-index: 10; /* 确保在最上层 */
  transition: all 0.2s ease; /* 添加过渡效果 */
}

.remove-file-btn:hover {
  background: #c82333;
  transform: scale(1.1); /* 悬停时稍微放大 */
}

/* 进度条样式 */
.progress-cell {
  padding: 1rem;
  min-width: 140px;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-bar {
  flex: 1;
  height: 20px;
  background-color: #e9ecef;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #dee2e6;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50 0%, #45a049 100%);
  border-radius: 10px;
  transition: width 0.3s ease;
  position: relative;
  overflow: hidden;
}

.progress-fill::after {
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

.progress-text {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
  min-width: 40px;
  text-align: right;
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
  max-height: 70vh;
}

.page-navigation {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin: 15px 0;
}

.page-navigation button {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.page-navigation button:hover:not(:disabled) {
  background-color: #0056b3;
}

.page-navigation button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.page-counter {
  font-weight: bold;
  color: #333;
  margin: 0 10px;
}

.pdf-image-container {
  text-align: center;
  margin: 20px 0;
}

.pdf-image-container img {
  max-width: 100%;
  height: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.download-link {
  display: inline-block;
  padding: 10px 20px;
  background-color: #28a745;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  margin: 10px 0;
  font-weight: bold;
}

.download-link:hover {
  background-color: #218838;
  color: white;
  text-decoration: none;
}

/* 加载模态框样式 */
.loading-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 10001;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

</style>