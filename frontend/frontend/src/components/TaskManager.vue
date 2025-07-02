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

    <!-- DDL提醒弹窗 -->
    <div v-if="showDdlAlert" class="ddl-alert-modal" @click.self="closeDdlAlert">
      <div class="ddl-alert-content">
        <div class="ddl-alert-icon">
          <i class="fas fa-exclamation-triangle"></i>
        </div>
        <h3>⏰ DDL提醒</h3>
        <p class="ddl-alert-message">{{ ddlAlertMessage }}</p>
        <div class="ddl-alert-buttons">
          <button class="btn-secondary" @click="closeDdlAlert">
            <i class="fas fa-check"></i>
            知道啦
          </button>
          <button class="btn-primary" @click="goToTask">
            <i class="fas fa-eye"></i>
            带我去看看
          </button>
        </div>
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
              <th>DDL管理</th>
              <th>进度</th>
              <th>附件</th>
              <th>操作</th>
              <th>选择分析</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in sortedTasks" :key="task.id" :class="getTaskRowClass(task)" :data-task-id="task.id">
              <td>{{ task.name }}</td>
              <td>{{ task.subject }}</td>
              <td>{{ task.content }}</td>
              <td>{{ getContentTypeLabel(task.contentType || task.type) }}</td>
              <td>{{ formatDateTime(task.startTime) || '未设置' }}</td>
              <td>{{ formatDateTime(task.endTime) || '未设置' }}</td>
              <td class="ddl-cell">
                <div class="ddl-container">
                  <div class="clock-icon" :class="{ 'ticking': !isTaskExpired(task) }">
                    <i class="fas fa-clock"></i>
                  </div>
                  <div class="ddl-info">
                    <div class="time-remaining" :class="getTimeRemainingClass(task)">
                      {{ getTimeRemaining(task) }}
                    </div>
                    <div class="status-indicator">
                      <i v-if="isTaskCompleted(task)" class="fas fa-check completed-icon"></i>
                      <i v-else-if="isTaskExpired(task)" class="fas fa-times expired-icon"></i>
                      <i v-else class="fas fa-hourglass-half pending-icon"></i>
                    </div>
                  </div>
                </div>
              </td>
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
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { uploadForTask } from '@/utils/fileUpload'

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  
  // 创建Date对象时，如果字符串不包含时区信息，会被当作本地时间
  let date
  if (dateStr.includes('T') && !dateStr.includes('Z') && !dateStr.includes('+')) {
    // 如果是YYYY-MM-DDTHH:mm格式且没有时区标识，当作本地时间处理
    date = new Date(dateStr)
  } else {
    // 如果包含时区信息，需要转换为本地时间
    date = new Date(dateStr)
  }
  
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 添加时间格式化辅助函数
function formatTimeForServer(timeStr) {
  if (!timeStr) return ''
  // 确保时间格式正确，添加秒数
  return timeStr.includes(':') ? timeStr + (timeStr.split(':').length === 2 ? ':00' : '') : timeStr
}

function formatTimeForEdit(timeStr) {
  if (!timeStr) return ''
  
  let date
  if (timeStr.includes('T')) {
    // 如果已经是ISO格式
    date = new Date(timeStr)
  } else {
    // 如果是其他格式，尝试解析
    date = new Date(timeStr)
  }
  
  // 确保日期有效
  if (isNaN(date.getTime())) {
    return ''
  }
  
  // 获取本地时间组件
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  return `${year}-${month}-${day}T${hours}:${minutes}`
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
const isComponentMounted = ref(false)

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

onMounted(() => {
  isComponentMounted.value = true
  fetchTasks()
  startDdlCheck()
  window.addEventListener('taskProgressUpdated', handleProgressUpdate)
  
  // 每分钟更新一次倒计时显示
  setInterval(() => {
    if (isComponentMounted.value) {
      // 触发响应式更新
      tasks.value = [...tasks.value]
    }
  }, 60000)
  
  // 开发环境下添加测试功能
  if (process.env.NODE_ENV === 'development') {
    // 添加全局测试函数
    window.testDdlAlert = triggerTestDdlAlert
    console.log('开发模式：可使用 window.testDdlAlert() 测试DDL提醒功能')
  }
})

// 组件卸载时清理定时器
onBeforeUnmount(() => {
  isComponentMounted.value = false
  stopDdlCheck()
  window.removeEventListener('taskProgressUpdated', handleProgressUpdate)
  // 关闭任何打开的DDL提醒弹窗
  showDdlAlert.value = false
  currentAlertTask.value = null
})

const showModal = ref(false)
const isEditing = ref(false)
const tasks = ref([])
const showDdlAlert = ref(false)
const ddlAlertMessage = ref('')
const currentAlertTask = ref(null)
const ddlCheckInterval = ref(null)
const notifiedTasks = ref(new Set()) // 记录已提醒过的任务，避免重复提醒

// 计算属性：排序后的任务列表
const sortedTasks = computed(() => {
  const now = new Date()
  
  return [...tasks.value].sort((a, b) => {
    const aExpired = isTaskExpired(a)
    const bExpired = isTaskExpired(b)
    
    // 未过期的任务排在前面
    if (aExpired && !bExpired) return 1
    if (!aExpired && bExpired) return -1
    
    // 如果都过期或都未过期，按截止时间排序
    const aEndTime = new Date(a.endTime)
    const bEndTime = new Date(b.endTime)
    
    return aEndTime - bEndTime
  })
})
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
    // 使用新的时间格式化函数
    startTime: formatTimeForServer(newTask.value.startTime),
    endTime: formatTimeForServer(newTask.value.endTime),
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
  // 使用新的时间格式化函数
  const formattedTask = {
    ...task,
    startTime: formatTimeForEdit(task.startTime),
    endTime: formatTimeForEdit(task.endTime)
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

// DDL管理相关函数
const isTaskCompleted = (task) => {
  return task.completed || (task.progress && task.progress >= 100)
}

const isTaskExpired = (task) => {
  if (!task.endTime) return false
  return new Date() > new Date(task.endTime)
}

const getTimeRemaining = (task) => {
  if (!task.endTime) return '无截止时间'
  
  const now = new Date()
  const endTime = new Date(task.endTime)
  const diff = endTime - now
  
  if (diff <= 0) {
    return isTaskCompleted(task) ? '已完成' : '已过期'
  }
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  if (days > 0) {
    return `${days}天${hours}小时`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

const getTimeRemainingClass = (task) => {
  if (!task.endTime) return 'no-deadline'
  
  const now = new Date()
  const endTime = new Date(task.endTime)
  const diff = endTime - now
  
  if (diff <= 0) {
    return isTaskCompleted(task) ? 'completed' : 'expired'
  }
  
  const hours = diff / (1000 * 60 * 60)
  
  if (hours <= 24) return 'urgent'      // 24小时内
  if (hours <= 72) return 'warning'     // 3天内
  return 'normal'
}

const getTaskRowClass = (task) => {
  const classes = []
  
  if (isTaskExpired(task)) {
    classes.push('expired-task')
    if (isTaskCompleted(task)) {
      classes.push('completed-expired')
    } else {
      classes.push('failed-expired')
    }
  } else if (isTaskCompleted(task)) {
    classes.push('completed-task')
  }
  
  return classes.join(' ')
}

// 开始DDL检查
const startDdlCheck = () => {
  // 立即执行一次检查
  checkDdlAlerts()
  
  // 设置定时器，每30秒检查一次
  ddlCheckInterval.value = setInterval(() => {
    checkDdlAlerts()
  }, 30000)
  
  console.log('DDL检查已启动')
}

// 停止DDL检查
const stopDdlCheck = () => {
  if (ddlCheckInterval.value) {
    clearInterval(ddlCheckInterval.value)
    ddlCheckInterval.value = null
    console.log('DDL检查已停止')
  }
}

// 检查DDL提醒
const checkDdlAlerts = () => {
  // 检查组件是否仍然挂载
  if (!isComponentMounted.value) {
    console.log('组件已卸载，停止DDL检查')
    return
  }
  
  const now = new Date()
  
  for (const task of tasks.value) {
    if (!task.endTime || isTaskCompleted(task)) {
      continue // 跳过没有截止时间或已完成的任务
    }
    
    const endTime = new Date(task.endTime)
    const timeDiff = endTime.getTime() - now.getTime()
    const minutesRemaining = Math.floor(timeDiff / (1000 * 60))
    
    // 修改检查逻辑：在一定范围内触发提醒
    const shouldAlert = (
      (minutesRemaining <= 60 && minutesRemaining > 55 && !notifiedTasks.value.has(`${task.id}-60`)) ||
      (minutesRemaining <= 15 && minutesRemaining > 10 && !notifiedTasks.value.has(`${task.id}-15`))
    ) && minutesRemaining > 0
    
    if (shouldAlert && isComponentMounted.value) {
      const alertType = minutesRemaining <= 15 ? 15 : 60
      showDdlAlertForTask(task, alertType)
      notifiedTasks.value.add(`${task.id}-${alertType}`)
      break // 一次只显示一个提醒
    }
  }
}

// 显示DDL提醒
const showDdlAlertForTask = (task, alertType) => {
  currentAlertTask.value = task
  const timeText = alertType === 60 ? '一小时' : '15分钟'
  ddlAlertMessage.value = `${task.name}任务仅剩${timeText}，请抓紧时间呦！`
  showDdlAlert.value = true
  
  // 添加调试信息
  console.log('DDL提醒触发:', {
    taskName: task.name,
    alertType: alertType,
    endTime: task.endTime,
    currentTime: new Date().toISOString()
  })
}

// 关闭DDL提醒
const closeDdlAlert = () => {
  showDdlAlert.value = false
  currentAlertTask.value = null
  ddlAlertMessage.value = ''
}

// 前往查看任务
const goToTask = async () => {
  if (!currentAlertTask.value || !isComponentMounted.value) {
    console.warn('没有当前提醒任务或组件已卸载')
    closeDdlAlert()
    return
  }
  
  const task = currentAlertTask.value
  console.log('前往查看任务:', task)
  
  try {
    // 如果有文件，打开文件
    if (task.fileUrl) {
      console.log('打开任务文件:', task.fileUrl)
      await openFile(task.fileUrl, task)
    } else {
      // 没有文件则高亮闪烁该任务
      console.log('高亮显示任务:', task.id)
      highlightTask(task)
      
      // 安全的滚动操作
      nextTick(() => {
        if (isComponentMounted.value) {
          const taskRow = document.querySelector(`tr[data-task-id="${task.id}"]`)
          if (taskRow && taskRow.parentNode) {
            taskRow.scrollIntoView({ behavior: 'smooth', block: 'center' })
          }
        }
      })
    }
  } catch (error) {
    console.error('前往查看任务失败:', error)
    if (isComponentMounted.value) {
      alert('无法打开任务，请手动查看任务列表')
    }
  }
  
  closeDdlAlert()
}

// 高亮闪烁任务
const highlightTask = (task) => {
  // 检查组件是否仍然挂载
  if (!isComponentMounted.value) {
    console.log('组件已卸载，跳过高亮操作')
    return
  }
  
  // 使用nextTick确保DOM已更新
  nextTick(() => {
    if (!isComponentMounted.value) return
    
    const taskRow = document.querySelector(`tr[data-task-id="${task.id}"]`)
    if (taskRow) {
      // 移除可能存在的旧样式
      taskRow.classList.remove('highlight-flash')
      
      // 强制重绘后添加新样式
      setTimeout(() => {
        if (isComponentMounted.value && taskRow.parentNode) {
          taskRow.classList.add('highlight-flash')
          
          // 3秒后移除高亮效果
          setTimeout(() => {
            if (isComponentMounted.value && taskRow.parentNode) {
              taskRow.classList.remove('highlight-flash')
            }
          }, 3000)
        }
      }, 10)
      
      console.log('任务高亮效果已应用:', task.id)
    } else {
      console.warn('未找到任务行元素:', task.id)
    }
  })
}

// 添加手动触发DDL检查的函数（用于测试）
const triggerTestDdlAlert = () => {
  if (tasks.value.length > 0) {
    const testTask = tasks.value[0]
    showDdlAlertForTask(testTask, 15)
    console.log('测试DDL提醒已触发')
  }
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
  max-width: 1400px;
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
  padding: 0.25rem;
  border-radius: 4px;
  color: #6c757d;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.5rem;
  height: 1.5rem;
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
  font-size: 0.8rem;
}

.actions-cell {
  display: flex;
  gap: 0.25rem;
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
  min-width: 1200px;
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
  min-width: 1200px;
  font-size: 0.9rem;
}

th {
  background: #f1f3f5;
  color: #495057;
  font-weight: 600;
  padding: 0.75rem 0.5rem;
  text-align: left;
  border-bottom: 2px solid #e9ecef;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

td {
  padding: 0.75rem 0.5rem;
  border-bottom: 1px solid #e9ecef;
  color: #495057;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

tr:hover {
  background: #f8f9fa;
}

/* 优化各列宽度，使其更紧凑 */
th:nth-child(1), td:nth-child(1) { /* 任务名称 */
  min-width: 120px;
  max-width: 150px;
}

th:nth-child(2), td:nth-child(2) { /* 学科 */
  min-width: 60px;
  max-width: 80px;
}

th:nth-child(3), td:nth-child(3) { /* 内容 */
  min-width: 150px;
  max-width: 200px;
  white-space: normal;
  max-height: 60px;
  overflow: hidden;
}

th:nth-child(4), td:nth-child(4) { /* 内容类型 */
  min-width: 70px;
  max-width: 90px;
}

th:nth-child(5), td:nth-child(5) { /* 开始时间 */
  min-width: 110px;
  max-width: 130px;
  font-size: 0.85rem;
}

th:nth-child(6), td:nth-child(6) { /* 结束时间 */
  min-width: 110px;
  max-width: 130px;
  font-size: 0.85rem;
}

th:nth-child(7), td:nth-child(7) { /* DDL管理 */
  min-width: 120px;
  max-width: 140px;
}

th:nth-child(8), td:nth-child(8) { /* 进度 */
  min-width: 100px;
  max-width: 120px;
}

th:nth-child(9), td:nth-child(9) { /* 附件 */
  min-width: 80px;
  max-width: 100px;
}

th:nth-child(10), td:nth-child(10) { /* 操作 */
  min-width: 80px;
  max-width: 100px;
}

th:nth-child(11), td:nth-child(11) { /* 选择分析 */
  min-width: 70px;
  max-width: 90px;
  text-align: center;
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
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  max-width: 100px;
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

/* DDL管理样式 */
.ddl-cell {
  min-width: 120px;
  padding: 0.5rem;
}

.ddl-container {
  display: flex;
  align-items: center;
  gap: 6px;
}

.clock-icon {
  font-size: 16px;
  color: #666;
}

.clock-icon.ticking {
  animation: tick 1s infinite;
  color: #007bff;
}

@keyframes tick {
  0%, 50% { transform: rotate(0deg); }
  25% { transform: rotate(5deg); }
  75% { transform: rotate(-5deg); }
}

.ddl-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.time-remaining {
  font-size: 11px;
  font-weight: bold;
}

.time-remaining.normal {
  color: #28a745;
}

.time-remaining.warning {
  color: #ffc107;
}

.time-remaining.urgent {
  color: #dc3545;
  animation: blink 1s infinite;
}

.time-remaining.completed {
  color: #28a745;
}

.time-remaining.expired {
  color: #dc3545;
}

.time-remaining.no-deadline {
  color: #6c757d;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0.5; }
}

.status-indicator {
  font-size: 12px;
}

.completed-icon {
  color: #28a745;
}

.expired-icon {
  color: #dc3545;
}

.pending-icon {
  color: #ffc107;
}

/* 任务行样式 */
.completed-task {
  background-color: #f8fff8;
  border-left: 4px solid #28a745;
}

.expired-task {
  background-color: #fff8f8;
  border-left: 4px solid #dc3545;
}

.completed-expired {
  background-color: #f0f8f0;
  border-left: 4px solid #28a745;
}

.failed-expired {
  background-color: #fff0f0;
  border-left: 4px solid #dc3545;
}

/* 过期任务透明度 */
.expired-task {
  opacity: 0.8;
}

/* 进度条样式 */
.progress-cell {
  padding: 0.5rem !important;
  min-width: 100px;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 6px;
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
  font-size: 0.8rem;
  min-width: 35px;
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

/* DDL提醒弹窗样式 */
.ddl-alert-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  z-index: 1500; /* 降低z-index，确保不覆盖导航栏 */
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(4px);
  /* 添加pointer-events控制，只在弹窗显示时阻止点击 */
  pointer-events: auto;
}

.ddl-alert-content {
  background: white;
  padding: 2rem;
  border-radius: 16px;
  width: 90%;
  max-width: 450px;
  text-align: center;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
  animation: ddlAlertSlideIn 0.3s ease-out;
  position: relative;
  z-index: 1501; /* 相应调整内容区域的z-index */
}

@keyframes ddlAlertSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.ddl-alert-icon {
  font-size: 3rem;
  color: #ff9800;
  margin-bottom: 1rem;
  animation: ddlIconPulse 1s infinite;
}

@keyframes ddlIconPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.ddl-alert-content h3 {
  color: #2c3e50;
  margin-bottom: 1rem;
  font-size: 1.5rem;
  font-weight: 600;
}

.ddl-alert-message {
  color: #555;
  font-size: 1.1rem;
  margin-bottom: 2rem;
  line-height: 1.5;
}

.ddl-alert-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.ddl-alert-buttons button {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 120px;
  justify-content: center;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
  transform: translateY(-2px);
}

.btn-primary {
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: white;
}

.btn-primary:hover {
  background: linear-gradient(135deg, #0056b3, #004085);
  transform: translateY(-2px);
}

/* 任务高亮闪烁效果 */
.highlight-flash {
  animation: highlightFlash 1s ease-in-out 3; /* 增加闪烁次数 */
  position: relative;
  z-index: 10;
}

@keyframes highlightFlash {
  0%, 100% {
    background-color: transparent;
    box-shadow: none;
    transform: scale(1);
  }
  50% {
    background-color: #fff3cd;
    box-shadow: 0 0 25px rgba(255, 193, 7, 0.8);
    border-left: 6px solid #ffc107;
    transform: scale(1.02); /* 添加轻微缩放效果 */
  }
}

</style>