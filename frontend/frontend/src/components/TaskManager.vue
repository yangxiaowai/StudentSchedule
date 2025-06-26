<template>
  <div class="task-manager">
    <h2>任务管理</h2>
    <button @click="showModal = true">
      <i class="fas fa-plus"></i> 添加任务
    </button>

    <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>任务名称</th>
              <th>学科</th>
              <th>内容</th>
              <th>开始时间</th>
              <th>结束时间</th>
              <th>进度</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in tasks" :key="task.id">
              <td>{{ task.name }}</td>
              <td>{{ task.subject }}</td>
              <td>{{ task.content }}</td>
              <td>{{ formatDateTime(task.startTime) || '未设置' }}</td>
              <td>{{ formatDateTime(task.endTime) || '未设置' }}</td>
              <td :style="{'--progress': task.progress || 0}" class="progress-cell">
                <span class="progress-text">{{ task.progress || 0 }}%</span>
              </td>
              <td class="actions-cell">
                <button class="icon-button edit" title="编辑" @click="editTask(task)">
                  <i class="fas fa-edit"></i>
                </button>
                <button class="icon-button delete" title="删除" @click="deleteTask(task.id)">
                  <i class="fas fa-trash-alt"></i>
                </button>
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
                 <option>语文</option>
                 <option>数学</option>
                 <option>英语</option>
                 <option>物理</option>
                 <option>化学</option>
                 <option>生物</option>
                 <option>政治</option>
                 <option>历史</option>
                 <option>地理</option>
                 <option>其他</option>
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
            
            <div class="form-group">
              <label>内容类型</label>
              <select v-model="newTask.type">
                <option disabled value="">请选择内容类型</option>
                <option>教材</option>
                <option>笔记</option>
                <option>真题</option>
                <option>习题</option>
                <option>课件</option>
              </select>
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
import { ref, computed } from 'vue'
import { onMounted } from 'vue'

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

onMounted(() => {
  fetchTasks()
})

const showModal = ref(false)
const isEditing = ref(false)
const tasks = ref([])
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
  
  // 如果有文件，添加到formData
  if (newTask.value.file) {
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
    type: '', remark: '', file: null, progress: 0, isCompleted: false
  })
  formErrors.value = {}
  isEditing.value = false
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

function handleFileUpload(event) {
  const file = event.target.files[0]
  if (file) {
    if (file.size > 10 * 1024 * 1024) {
      alert('文件大小不能超过10MB')
      event.target.value = ''
      return
    }
    newTask.value.file = file
  }
}</script>

<style scoped>
.task-manager {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  background: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.task-manager h2 {
  color: #2c3e50;
  margin-bottom: 1.5rem;
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

/* 进度条样式 */
td:nth-child(6) {
  position: relative;
}

td:nth-child(6)::after {
  content: '';
  position: absolute;
  left: 1rem;
  bottom: 0.5rem;
  height: 4px;
  width: calc(100% - 2rem);
  background: #e9ecef;
  border-radius: 2px;
}

td:nth-child(6)::before {
  content: '';
  position: absolute;
  left: 1rem;
  bottom: 0.5rem;
  height: 4px;
  width: calc((100% - 2rem) * var(--progress) / 100);
  background: #4CAF50;
  border-radius: 2px;
  z-index: 1;
}
</style>