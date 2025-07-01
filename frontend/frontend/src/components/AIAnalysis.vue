<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Chart, registerables } from 'chart.js'
import { Chart as VueChart } from 'vue-chartjs'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import axios from 'axios'

// 注册Chart.js组件
Chart.register(...registerables)

// 定义props
const props = defineProps({
  selectedTasks: {
    type: Array,
    default: () => []
  }
})

// 从localStorage获取选中的任务
const localSelectedTasks = ref([])

// 计算属性：优先使用props，如果没有则使用localStorage的数据
const currentSelectedTasks = computed(() => {
  if (props.selectedTasks && props.selectedTasks.length > 0) {
    return props.selectedTasks
  }
  return localSelectedTasks.value
})

// 状态管理
const radarData = ref({
  labels: [],
  datasets: [{
    label: '完成度',
    data: [],
    backgroundColor: 'rgba(54, 162, 235, 0.2)',
    borderColor: 'rgba(54, 162, 235, 1)',
    borderWidth: 2
  }]
})

const barData = ref({
  labels: [],
  datasets: [{
    label: '完成度',
    data: [],
    backgroundColor: 'rgba(75, 192, 192, 0.2)',
    borderColor: 'rgba(75, 192, 192, 1)',
    borderWidth: 2
  }]
})

const planData = ref({
  subjects: [],
  days: []
})

// 分析文本
const summaryText = ref('')
const compareText = ref('')
const suggestionText = ref('')
const analysisText = ref('')

// 智能分析数据
const smartSuggestions = ref([])
const learningEfficiency = ref({})
const predictiveAnalysis = ref({})
const studyPatterns = ref({})
const motivationalInsights = ref({})

// 学习效率分析（加权分析方案）
const efficiencyData = computed(() => {
  // 直接使用当前选中的任务，确保与选择的任务匹配
  const selectedTasks = currentSelectedTasks.value
  
  // 任务完成度权重（70%）
  const progressWeight = 0.7
  // 时间效率权重（30%）
  const timeWeight = 0.3
  
  // 计算任务完成度得分（基于所有任务的进度）
  const progressScore = selectedTasks.length > 0
    ? selectedTasks.reduce((sum, t) => sum + (t.progress || 0), 0) / selectedTasks.length
    : 0
  
  // 计算时间效率得分
  const timeScore = calculateTimeEfficiency(selectedTasks)
  
  // 计算加权效率得分
  const weightedEfficiency = (progressScore * progressWeight + timeScore * timeWeight).toFixed(1)
  
  return {
    totalTasks: selectedTasks.length,
    completedTasks: selectedTasks.filter(t => t.progress >= 100).length,
    averageProgress: progressScore.toFixed(1),
    efficiency: weightedEfficiency,
    progressScore: progressScore.toFixed(1),
    timeScore: timeScore.toFixed(1),
    efficiencyLevel: getEfficiencyLevel(weightedEfficiency)
  }
})

// 预测分析数据
const predictionData = computed(() => ({
  labels: ['下周一', '下周二', '下周三', '下周四', '下周五', '下周六', '下周日'],
  datasets: [{
    label: '预测完成度',
    data: predictiveAnalysis.value.weeklyPrediction || [75, 80, 85, 78, 82, 70, 88],
    backgroundColor: 'rgba(255, 193, 7, 0.3)',
    borderColor: 'rgba(255, 193, 7, 1)',
    borderWidth: 2,
    fill: true
  }]
}))

// 学习模式分析
const patternData = computed(() => ({
  labels: ['早晨', '上午', '下午', '晚上', '深夜'],
  datasets: [{
    label: '学习效率',
    data: studyPatterns.value.timeEfficiency || [65, 85, 75, 90, 45],
    backgroundColor: [
      'rgba(255, 99, 132, 0.3)',
      'rgba(54, 162, 235, 0.3)',
      'rgba(255, 205, 86, 0.3)',
      'rgba(75, 192, 192, 0.3)',
      'rgba(153, 102, 255, 0.3)'
    ],
    borderColor: [
      'rgba(255, 99, 132, 1)',
      'rgba(54, 162, 235, 1)',
      'rgba(255, 205, 86, 1)',
      'rgba(75, 192, 192, 1)',
      'rgba(153, 102, 255, 1)'
    ],
    borderWidth: 2
  }]
}))

// 学习目标
const learningGoals = ref({
  weekly: { target: 85, current: 0 },
  monthly: { target: 80, current: 0 },
  subjects: {}
})

// 个性化计划
const personalizedPlan = ref([])

// 格式化AI建议文本
const formattedSuggestionText = computed(() => {
  if (!suggestionText.value || suggestionText.value === 'AI建议生成失败' || suggestionText.value === '正在生成AI建议...') {
    return suggestionText.value
  }
  
  // 去除星号
  let text = suggestionText.value.replace(/\*/g, '')
  
  // 检查是否包含换行符
  if (text.includes('\n')) {
    // 如果包含换行符，按换行符分割成主要建议和步骤
    const lines = text.split('\n').map(s => s.trim()).filter(s => s.length > 0)
    
    // 将每个建议转换为对象，包含主要内容和步骤
    const suggestions = []
    let currentSuggestion = null
    
    lines.forEach(line => {
      // 检查是否是新的建议（以数字开头）
      const isNewSuggestion = /^\d+\s*[.．]/.test(line)
      
      if (isNewSuggestion) {
        // 如果是新建议，创建新对象并添加到数组
        if (currentSuggestion) {
          suggestions.push(currentSuggestion)
        }
        
        currentSuggestion = {
          main: line.replace(/^\d+\s*[.．]\s*/, ''), // 移除序号
          steps: []
        }
      } else if (currentSuggestion) {
        // 如果是步骤，添加到当前建议的步骤数组
        currentSuggestion.steps.push(line)
      }
    })
    
    // 添加最后一个建议
    if (currentSuggestion) {
      suggestions.push(currentSuggestion)
    }
    
    return suggestions.length > 0 ? suggestions : text
  } else {
    // 检查是否已经包含序号格式（如：1. 2. 3.）
    const hasNumbering = /\d+\s*[.．]/.test(text)
    
    if (hasNumbering) {
      // 如果已有序号，按序号分割
      const items = text.split(/(?=\d+\s*[.．])/)
        .map(s => s.trim())
        .filter(s => s.length > 0)
        .map(s => s.replace(/^\d+\s*[.．]\s*/, '')) // 移除原有序号
      
      return items.length > 1 ? items : text
    } else {
      // 如果没有序号，按句号、分号、感叹号分割
      const sentences = text.split(/[。；！]/)
        .map(s => s.trim())
        .filter(s => s.length > 0)
      
      return sentences.length > 1 ? sentences : text
    }
  }
})

// 表格数据计算
const subjectTable = computed(() => {
  if (!radarData.value.labels.length) return []
  return radarData.value.labels.map((label, index) => ({
    label,
    current: radarData.value.datasets[0].data[index],
    plan: planData.value.subjects[index],
    diff: radarData.value.datasets[0].data[index] - planData.value.subjects[index]
  }))
})

const dayTable = computed(() => {
  if (!barData.value.labels.length) return []
  return barData.value.labels.map((label, index) => ({
    label,
    current: barData.value.datasets[0].data[index],
    plan: planData.value.days[index],
    diff: barData.value.datasets[0].data[index] - planData.value.days[index]
  }))
})

// 功能选项
const tableOptions = ref([
  { key: 'radar', label: '学科雷达图', checked: true },
  { key: 'bar', label: '每日完成度柱状图', checked: true },
  { key: 'efficiency', label: '学习效率统计', checked: true },
  { key: 'subjectTable', label: '学科对比表', checked: true },
  { key: 'dayTable', label: '每日对比表', checked: true },
  { key: 'smartSuggestions', label: '智能建议卡片', checked: true }
])

// 错误处理状态
const error = ref({
  show: false,
  message: ''
})

// 加载状态
const loading = ref({
  analysis: false,
  suggestions: false,
  plan: false
})

// 方法定义
async function generateSmartSuggestions() {
  if (loading.value.suggestions) return
  loading.value.suggestions = true
  error.value.show = false
  
  try {
    const token = localStorage.getItem('accessToken')
    if (!token) {
      throw new Error('用户未登录，请先登录后再进行操作')
    }
    
    const { data } = await axios.post('http://localhost:8080/api/analysis/smart-suggestions', {
      taskIds: currentSelectedTasks.value.map(t => t.id),
      efficiencyData: efficiencyData.value
    }, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
    smartSuggestions.value = data.suggestions
    learningEfficiency.value = data.efficiency
    predictiveAnalysis.value = data.prediction
    studyPatterns.value = data.patterns
    motivationalInsights.value = data.motivation
  } catch (error) {
    console.error('智能建议生成失败:', error)
    error.value = {
      show: true,
      message: '智能建议生成失败，请稍后重试'
    }
  } finally {
    loading.value.suggestions = false
  }
}

async function generatePersonalizedPlan() {
  if (loading.value.plan) return
  loading.value.plan = true
  error.value.show = false

  try {
    const token = localStorage.getItem('accessToken')
    if (!token) {
      throw new Error('用户未登录，请先登录后再进行操作')
    }
    
    const { data } = await axios.post('http://localhost:8080/api/analysis/personalized-plan', {
      taskIds: currentSelectedTasks.value.map(t => t.id),
      goals: learningGoals.value,
      patterns: studyPatterns.value
    }, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
    personalizedPlan.value = data.plan
  } catch (error) {
    console.error('个性化计划生成失败:', error)
    error.value = {
      show: true,
      message: '个性化计划生成失败，请稍后重试'
    }
  } finally {
    loading.value.plan = false
  }
}

async function generateAIReport() {
  if (!currentSelectedTasks.value.length || loading.value.analysis) return
  loading.value.analysis = true
  error.value.show = false
  suggestionText.value = '正在生成AI建议...'
  
  try {
    console.log('准备分析的任务数据:', currentSelectedTasks.value)
    console.log('任务数据类型:', typeof currentSelectedTasks.value, Array.isArray(currentSelectedTasks.value))
    console.log('任务数据详情:', JSON.stringify(currentSelectedTasks.value))
    
    if (!Array.isArray(currentSelectedTasks.value) || currentSelectedTasks.value.length === 0) {
      throw new Error('没有有效的任务数据可供分析')
    }
    
    // 提取任务ID并确保是数字类型
    const taskIds = []
    
    for (const task of currentSelectedTasks.value) {
      console.log('处理任务:', task)
      
      if (!task) {
        console.error('任务对象为空')
        continue
      }
      
      let taskId = task.id
      console.log('原始任务ID:', taskId, '类型:', typeof taskId)
      
      // 确保ID是数字类型
      if (typeof taskId === 'string') {
        taskId = parseInt(taskId, 10)
        if (isNaN(taskId)) {
          console.error('无法将任务ID转换为数字:', task.id)
          continue
        }
      } else if (typeof taskId !== 'number') {
        console.error('任务ID不是有效的数字或字符串:', taskId)
        continue
      }
      
      taskIds.push(taskId)
    }
    
    console.log('处理后的任务ID数组:', taskIds)
    
    if (taskIds.length === 0) {
      throw new Error('没有有效的任务ID可供分析')
    }
    
    console.log('发送任务分析请求，任务IDs:', taskIds)
    console.log('发送前的任务ID JSON:', JSON.stringify(taskIds))
    
    // 获取认证token
    const token = localStorage.getItem('accessToken')
    if (!token) {
      throw new Error('用户未登录，请先登录后再进行AI分析')
    }
    
    const { data } = await axios.post('http://localhost:8080/api/analysis/tasks', taskIds, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
    
    console.log('后端返回的完整数据:', data)
    console.log('subjectNames:', data.subjectNames)
    console.log('subjectData:', data.subjectData)
    console.log('dayNames:', data.dayNames)
    console.log('dayData:', data.dayData)
    
    // 检查数据有效性
    if (!data.subjectNames || !Array.isArray(data.subjectNames)) {
      console.error('subjectNames数据无效:', data.subjectNames)
      throw new Error('后端返回的学科名称数据无效')
    }
    
    if (!data.subjectData || !Array.isArray(data.subjectData)) {
      console.error('subjectData数据无效:', data.subjectData)
      throw new Error('后端返回的学科数据无效')
    }
    
    // 更新图表数据
    radarData.value.labels = data.subjectNames
    radarData.value.datasets[0].data = data.subjectData
    barData.value.labels = data.dayNames
    barData.value.datasets[0].data = data.dayData
    planData.value.subjects = data.planSubjects
    planData.value.days = data.planDays
    
    console.log('更新后的radarData:', radarData.value)
    console.log('更新后的barData:', barData.value)
    console.log('更新后的subjectTable:', subjectTable.value)
    console.log('更新后的dayTable:', dayTable.value)
    
    // 生成分析文本
    const avgCompletion = (data.subjectData.reduce((a, b) => a + b, 0) / data.subjectData.length).toFixed(1)
    const maxSubject = data.subjectNames[data.subjectData.indexOf(Math.max(...data.subjectData))]
    const minSubject = data.subjectNames[data.subjectData.indexOf(Math.min(...data.subjectData))]
    
    summaryText.value = `本周平均完成度为${avgCompletion}%，其中${maxSubject}最高（${Math.max(...data.subjectData)}%），${minSubject}最低（${Math.min(...data.subjectData)}%）。`
    
    const compareArr = data.subjectData.map((v, i) => v - data.planSubjects[i])
    const compareStr = compareArr.map((v, i) => {
      if (v > 0) return `${data.subjectNames[i]}超出计划${v}%`
      if (v < 0) return `${data.subjectNames[i]}低于计划${-v}%`
      return `${data.subjectNames[i]}与计划持平`
    }).join('，')
    
    compareText.value = `与原计划对比：${compareStr}。`
    suggestionText.value = data.suggestion || 'AI建议生成失败'
    analysisText.value = `${summaryText.value}\n${compareText.value}\n${suggestionText.value}`
    
    // 生成扩展分析
    await Promise.all([
      generateSmartSuggestions(),
      generatePersonalizedPlan()
    ])
    
  } catch (error) {
    console.error('AI分析生成失败:', error)
    error.value = {
      show: true,
      message: 'AI分析生成失败，请稍后重试'
    }
    suggestionText.value = 'AI建议生成失败'
  } finally {
    loading.value.analysis = false
  }
}

async function exportEnhancedPDF() {
  try {
    const element = document.querySelector('.ai-analysis-wrapper')
    if (!element) throw new Error('导出元素不存在')
    
    const canvas = await html2canvas(element, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#ffffff',
      logging: false
    })
    
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgData = canvas.toDataURL('image/png')
    const imgWidth = 210
    const pageHeight = 295
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    let heightLeft = imgHeight
    let position = 0
    
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight
    
    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }
    
    pdf.save(`AI学习分析报告_${new Date().toLocaleDateString()}.pdf`)
  } catch (error) {
    console.error('PDF导出失败:', error)
    error.value = {
      show: true,
      message: 'PDF导出失败，请稍后重试'
    }
  }
}

// 从localStorage加载选中的任务
function loadSelectedTasksFromStorage() {
  try {
    const stored = localStorage.getItem('selectedTasksForAnalysis')
    console.log('从localStorage加载数据:', stored)
    if (stored) {
      try {
        const parsedData = JSON.parse(stored)
        console.log('解析后的原始数据:', parsedData)
        console.log('数据类型:', typeof parsedData, Array.isArray(parsedData))
        
        // 确保每个任务对象都有id字段
        if (Array.isArray(parsedData)) {
          parsedData.forEach((task, index) => {
            console.log(`任务[${index}]:`, task)
            if (!task.id) {
              console.error(`任务[${index}]缺少ID:`, task)
            }
          })
        } else {
          console.error('解析后的数据不是数组')
        }
        
        localSelectedTasks.value = parsedData
        console.log('设置到localSelectedTasks后:', localSelectedTasks.value)
      } catch (parseError) {
        console.error('JSON解析失败:', parseError, '原始数据:', stored)
      }
      
      // 清除localStorage中的数据，避免重复使用
      localStorage.removeItem('selectedTasksForAnalysis')
    } else {
      console.log('localStorage中没有找到任务数据')
    }
  } catch (error) {
    console.error('加载选中任务失败:', error)
  }
}

// 计算时间效率得分（基于任务的开始时间和结束时间）
function calculateTimeEfficiency(tasks) {
  if (!tasks || tasks.length === 0) return 0
  
  // 计算每个任务的时间效率得分
  const taskScores = tasks.map(task => {
    // 如果没有开始时间或结束时间，返回默认分数50
    if (!task.startTime || !task.endTime) return 50
    
    // 解析时间
    const startTime = new Date(task.startTime)
    const endTime = new Date(task.endTime)
    const now = new Date()
    
    // 计算计划时长（天）
    const plannedDuration = (endTime - startTime) / (1000 * 60 * 60 * 24)
    if (plannedDuration <= 0) return 50 // 无效的时间范围
    
    // 如果任务已完成（进度100%）
    if (task.progress >= 100) {
      // 提前完成得高分，拖延完成得低分
      if (now < endTime) {
        // 提前完成
        const daysAhead = (endTime - now) / (1000 * 60 * 60 * 24)
        const aheadRatio = Math.min(daysAhead / plannedDuration, 1) // 最多提前100%
        return 80 + (aheadRatio * 20) // 80-100分
      } else {
        // 按时或拖延完成
        const daysLate = (now - endTime) / (1000 * 60 * 60 * 24)
        const lateRatio = Math.min(daysLate / plannedDuration, 1) // 最多拖延100%
        return 80 - (lateRatio * 30) // 50-80分
      }
    } else {
      // 任务未完成
      if (now < endTime) {
        // 还在截止日期内
        const progressRatio = task.progress / 100
        const timeRatio = (now - startTime) / (endTime - startTime)
        
        // 进度超前于时间比例得高分，落后得低分
        if (progressRatio >= timeRatio) {
          const aheadRatio = Math.min((progressRatio - timeRatio) / timeRatio, 1)
          return 70 + (aheadRatio * 20) // 70-90分
        } else {
          const behindRatio = Math.min((timeRatio - progressRatio) / timeRatio, 1)
          return 70 - (behindRatio * 40) // 30-70分
        }
      } else {
        // 已过截止日期但未完成
        const daysLate = (now - endTime) / (1000 * 60 * 60 * 24)
        const lateRatio = Math.min(daysLate / plannedDuration, 1)
        return Math.max(50 - (lateRatio * 50), 0) // 0-50分
      }
    }
  })
  
  // 返回平均分数
  return taskScores.reduce((sum, score) => sum + score, 0) / taskScores.length
}

// 根据效率得分获取效率等级
function getEfficiencyLevel(score) {
  const numScore = Number(score)
  if (numScore >= 90) return { level: 'S', text: '卓越', color: '#FF5722' }
  if (numScore >= 80) return { level: 'A', text: '优秀', color: '#4CAF50' }
  if (numScore >= 70) return { level: 'B', text: '良好', color: '#2196F3' }
  if (numScore >= 60) return { level: 'C', text: '一般', color: '#FFC107' }
  if (numScore >= 50) return { level: 'D', text: '需努力', color: '#FF9800' }
  return { level: 'E', text: '急需改进', color: '#F44336' }
}

// 监听选中任务变化
watch(() => currentSelectedTasks.value, (newTasks) => {
  if (newTasks.length) {
    generateAIReport()
  }
}, { deep: true })

// 生命周期钩子
onMounted(() => {
  // 先从localStorage加载数据
  loadSelectedTasksFromStorage()
  
  // 如果有选中的任务，生成分析报告
  if (currentSelectedTasks.value.length) {
    generateAIReport()
  }
})
</script>

<template>
  <div class="ai-analysis-bg">
    <div class="ai-analysis-wrapper">
      <!-- 错误提示 -->
      <div v-if="error.show" class="error-message">
        {{ error.message }}
        <button @click="error.show = false" class="close-btn">×</button>
      </div>

      <!-- 顶部标题区 -->
      <header class="ai-header">
        <h1 class="ai-title">🤖 AI 智能学习分析</h1>
        <div class="ai-btn-group">
          <button 
            class="ai-btn primary" 
            @click="generateAIReport"
            :disabled="loading.analysis || !currentSelectedTasks.length"
          >
            <i class="icon-brain"></i>
            {{ loading.analysis ? '生成中...' : '生成AI分析' }}
          </button>
          <button 
            class="ai-btn secondary" 
            @click="exportEnhancedPDF"
            :disabled="loading.analysis || !currentSelectedTasks.length"
          >
            <i class="icon-download"></i>导出增强报告
          </button>
          <button 
            class="ai-btn accent" 
            @click="generatePersonalizedPlan"
            :disabled="loading.plan || !currentSelectedTasks.length"
          >
            <i class="icon-target"></i>
            {{ loading.plan ? '生成中...' : '生成学习计划' }}
          </button>
        </div>
      </header>

      <!-- 无任务提示 -->
      <div v-if="!currentSelectedTasks.length" class="no-tasks-notice">
        <div class="notice-icon">📋</div>
        <h2>欢迎使用 AI 智能学习分析</h2>
        <p>请先在任务管理页面选择需要分析的学习任务，然后返回此页面进行AI分析。</p>
        <div class="notice-steps">
          <div class="step">
            <button class="step-btn">
              <span class="step-number">1</span>
              <span class="step-text">前往任务管理页面</span>
            </button>
          </div>
          <div class="step">
            <button class="step-btn">
              <span class="step-number">2</span>
              <span class="step-text">选择要分析的任务</span>
            </button>
          </div>
          <div class="step">
            <button class="step-btn">
              <span class="step-number">3</span>
              <span class="step-text">返回AI分析页面</span>
            </button>
          </div>
        </div>
        <router-link to="/task-manager" class="go-to-tasks-btn">
          <i class="icon-arrow-right"></i>
          前往任务管理
        </router-link>
      </div>

      <!-- 功能选择区 -->
      <div v-if="currentSelectedTasks.length" class="feature-selector">
        <h3>📊 分析模块选择</h3>
        <div class="table-options">
          <label 
            v-for="item in tableOptions" 
            :key="item.key" 
            class="table-option"
          >
            <input 
              type="checkbox" 
              v-model="item.checked" 
              :disabled="loading.analysis"
            />
            <span class="checkmark"></span>
            <span>{{ item.label }}</span>
          </label>
        </div>
      </div>

      <!-- 主内容区 -->
      <main v-if="currentSelectedTasks.length" class="ai-main-content" ref="pdfRef">
        <!-- 学习效率概览 -->
        <section 
          v-if="tableOptions.find(i=>i.key==='efficiency').checked" 
          class="efficiency-overview"
        >
          <h3>📈 学习效率概览</h3>
          <div class="efficiency-grade" :style="{backgroundColor: efficiencyData.efficiencyLevel.color + '22'}">
            <div class="grade-circle" :style="{backgroundColor: efficiencyData.efficiencyLevel.color}">
              {{ efficiencyData.efficiencyLevel.level }}
            </div>
            <div class="grade-info">
              <h4>学习效率等级</h4>
              <div class="grade-text">{{ efficiencyData.efficiencyLevel.text }}</div>
              <div class="grade-score">{{ efficiencyData.efficiency }}分</div>
            </div>
          </div>
          <div class="efficiency-cards">
            <div class="efficiency-card">
              <div class="card-icon">📚</div>
              <div class="card-content">
                <h4>总任务数</h4>
                <span class="card-value">{{ efficiencyData.totalTasks }}</span>
              </div>
            </div>
            <div class="efficiency-card">
              <div class="card-icon">✅</div>
              <div class="card-content">
                <h4>已完成</h4>
                <span class="card-value">{{ efficiencyData.completedTasks }}</span>
              </div>
            </div>
            <div class="efficiency-card">
              <div class="card-icon">📊</div>
              <div class="card-content">
                <h4>任务完成度</h4>
                <span class="card-value">{{ efficiencyData.progressScore }}%</span>
                <span class="card-weight">(权重70%)</span>
              </div>
            </div>
            <div class="efficiency-card">
              <div class="card-icon">⏱️</div>
              <div class="card-content">
                <h4>时间效率</h4>
                <span class="card-value">{{ efficiencyData.timeScore }}分</span>
                <span class="card-weight">(权重30%)</span>
              </div>
            </div>
          </div>
          <div class="efficiency-description">
            <p>学习效率评分采用加权分析方案，结合任务完成度(70%)和时间效率(30%)进行综合评估。</p>
            <p>时间效率考虑了任务的提前/拖延完成情况，以及进度与时间的匹配度。</p>
          </div>
        </section>

        <!-- 图表区域 -->
        <section class="ai-charts-grid">
          <!-- 雷达图 -->
          <div 
            v-if="tableOptions.find(i=>i.key==='radar').checked" 
            class="ai-chart-card"
          >
            <VueChart 
              type="radar" 
              :data="radarData" 
              :options="{
                plugins: { 
                  legend: { labels: { font: { size: 16 } } },
                  title: { display: true, text: '各学科完成度分析' }
                },
                scales: { 
                  r: { 
                    angleLines: { display: true }, 
                    suggestedMin: 0, 
                    suggestedMax: 100, 
                    pointLabels: { font: { size: 14 } } 
                  } 
                }
              }" 
            />
            <div class="ai-chart-label">📚 各学科平均完成度</div>
          </div>

          <!-- 柱状图 -->
          <div 
            v-if="tableOptions.find(i=>i.key==='bar').checked" 
            class="ai-chart-card"
          >
            <VueChart 
              type="bar" 
              :data="barData" 
              :options="{
                plugins: { 
                  legend: { labels: { font: { size: 16 } } },
                  title: { display: true, text: '每日学习完成度' }
                },
                scales: { 
                  x: { ticks: { font: { size: 14 } } }, 
                  y: { 
                    beginAtZero: true, 
                    suggestedMax: 100, 
                    ticks: { font: { size: 14 } } 
                  } 
                }
              }" 
            />
            <div class="ai-chart-label">📅 每日平均完成度</div>
          </div>

          <!-- 预测分析图表和学习模式分析已删除 -->
        </section>

        <!-- 智能建议卡片 -->
        <section 
          v-if="tableOptions.find(i=>i.key==='smartSuggestions').checked && smartSuggestions.length" 
          class="smart-suggestions"
        >
          <h3>🧠 AI智能建议</h3>
          <div class="suggestions-grid">
            <div 
              v-for="(suggestion, index) in smartSuggestions" 
              :key="index" 
              class="suggestion-card"
            >
              <div class="suggestion-icon">{{ suggestion.icon }}</div>
              <div class="suggestion-content">
                <h4>{{ suggestion.title }}</h4>
                <div class="suggestion-details">
                  <template v-if="suggestion.content.includes('\n')">
                    <p v-for="(line, lineIndex) in suggestion.content.split('\n')" :key="lineIndex" 
                       :class="{'suggestion-main': lineIndex === 0, 'suggestion-step': lineIndex > 0 && line.match(/^\d+\./)}"
                       :data-number="lineIndex > 0 && line.match(/^(\d+)\./)?line.match(/^(\d+)\./)[1]:''"
                    >
                      {{ line.replace(/^\d+\.\s*/, '') }}
                    </p>
                  </template>
                  <p v-else>{{ suggestion.content }}</p>
                </div>
                <div 
                  class="suggestion-priority" 
                  :class="suggestion.priority"
                >
                  {{ suggestion.priority === 'high' ? '高优先级' : 
                     suggestion.priority === 'medium' ? '中优先级' : '低优先级' }}
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 个性化学习计划已删除 -->

        <!-- 对比表格 -->
        <section 
          v-if="tableOptions.find(i=>i.key==='subjectTable').checked" 
          class="ai-table-section"
        >
          <h3>📊 学科对比表</h3>
          <div class="table-container">
            <table class="ai-table">
            <thead>
              <tr>
                <th>学科</th>
                <th>当前完成度</th>
                <th>计划完成度</th>
                <th>差值</th>
                <th>趋势</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in subjectTable" :key="row.label">
                <td>{{ row.label }}</td>
                <td>{{ row.current }}%</td>
                <td>{{ row.plan }}%</td>
                <td :style="{color: row.diff>0?'#4caf50':row.diff<0?'#f44336':'#666'}">{{ row.diff }}%</td>
                <td>
                  <span v-if="row.diff > 0" class="trend-up">📈</span>
                  <span v-else-if="row.diff < 0" class="trend-down">📉</span>
                  <span v-else class="trend-stable">➡️</span>
                </td>
              </tr>
            </tbody>
          </table>
          </div>
        </section>

        <!-- 每日对比表 -->
        <section 
          v-if="tableOptions.find(i=>i.key==='dayTable').checked" 
          class="ai-table-section"
        >
          <h3>📅 每日对比表</h3>
          <div class="table-container">
            <table class="ai-table">
            <thead>
              <tr>
                <th>日期</th>
                <th>当前完成度</th>
                <th>计划完成度</th>
                <th>差值</th>
                <th>趋势</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in dayTable" :key="row.label">
                <td>{{ row.label }}</td>
                <td>{{ row.current }}%</td>
                <td>{{ row.plan }}%</td>
                <td :style="{color: row.diff>0?'#4caf50':row.diff<0?'#f44336':'#666'}">{{ row.diff }}%</td>
                <td>
                  <span v-if="row.diff > 0" class="trend-up">📈</span>
                  <span v-else-if="row.diff < 0" class="trend-down">📉</span>
                  <span v-else class="trend-stable">➡️</span>
                </td>
              </tr>
            </tbody>
          </table>
          </div>
        </section>

        <!-- AI分析报告 -->
        <section class="ai-analysis-card enhanced">
          <h2>🤖 AI深度分析报告</h2>
          <div class="ai-report-section">
            <div class="ai-report-block summary">
              <strong>📊 完成情况总结：</strong>
              <span>{{ summaryText }}</span>
            </div>
            <div class="ai-report-block comparison">
              <strong>📈 与原计划对比：</strong>
              <span>{{ compareText }}</span>
            </div>
            <div class="ai-report-block suggestion">
              <strong>💡 AI学习建议：</strong>
              <div v-if="Array.isArray(formattedSuggestionText)" class="suggestion-list">
                <div 
                  v-for="(item, index) in formattedSuggestionText" 
                  :key="index" 
                  class="suggestion-item"
                >
                  <span class="suggestion-number">{{ index + 1 }}.</span>
                  <div class="suggestion-content-wrapper">
                    <!-- 如果是对象（包含主要内容和步骤） -->
                    <template v-if="typeof item === 'object' && item.main">
                      <div class="suggestion-main-content">{{ item.main }}</div>
                      <div v-if="item.steps && item.steps.length" class="suggestion-steps">
                        <div 
                          v-for="(step, stepIndex) in item.steps" 
                          :key="stepIndex"
                          class="suggestion-step"
                          :data-number="step.match(/^\d+\./) ? step.match(/^(\d+)\./)[1] : ''"
                        >
                          {{ step.replace(/^\d+\.\s*/, '') }}
                        </div>
                      </div>
                    </template>
                    <!-- 如果是简单字符串 -->
                    <span v-else class="suggestion-content">{{ item }}</span>
                  </div>
                </div>
              </div>
              <span v-else>{{ formattedSuggestionText }}</span>
            </div>
            <div 
              v-if="motivationalInsights.message" 
              class="ai-report-block motivation"
            >
              <strong>🎯 激励洞察：</strong>
              <span>{{ motivationalInsights.message }}</span>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* CSS 变量定义 */
:root {
  --primary-color: #4a5fc1;
  --secondary-color: #5a4b9d;
  --accent-color: #2196f3;
  --success-color: #2e7d32;
  --warning-color: #f57c00;
  --error-color: #d32f2f;
  --text-primary: #2c3e50;
  --text-secondary: #5a6c7d;
  --text-contrast: #ffffff;
  --bg-primary: rgba(255, 255, 255, 0.98);
  --bg-secondary: rgba(255, 255, 255, 0.92);
  --shadow-light: 0 2px 8px rgba(120, 180, 255, 0.12);
  --shadow-medium: 0 4px 20px rgba(0, 0, 0, 0.15);
  --shadow-heavy: 0 8px 32px rgba(120, 180, 255, 0.18);
  --border-radius: 16px;
  --border-radius-large: 22px;
  --transition-fast: 0.2s ease;
  --transition-medium: 0.3s ease;
  --font-family: 'Inter', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
  --border-color: #e0e0e0;
  --border-color-strong: #bdbdbd;
}

/* 基础布局 */
.ai-analysis-bg {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #fffacd 0%, #fff8dc 50%, #ffffe0 100%);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 2rem 1rem;
  font-family: var(--font-family);
  color: var(--text-primary);
  font-size: 1.1rem;
  line-height: 1.7;
}

/* 主容器 */
.ai-analysis-wrapper {
  width: 100%;
  max-width: 1200px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius-large);
  box-shadow: var(--shadow-heavy), 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 0 0 2rem 0;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: slideInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

/* 错误提示 */
.error-message {
  background: linear-gradient(135deg, #ffebee, #ffcdd2);
  color: var(--text-primary);
  padding: 1rem 1.5rem;
  margin: 1rem 2rem;
  border-radius: var(--border-radius);
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-medium);
  animation: slideInDown 0.3s ease;
  border: 2px solid var(--error-color);
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-primary);
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition-fast);
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 头部区域 */
.ai-header {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: var(--text-primary);
  padding: 2rem;
  border-radius: var(--border-radius-large) var(--border-radius-large) 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1.5rem;
  border: 2px solid var(--primary-color);
}

.ai-title {
  font-size: 2.2rem;
  font-weight: 800;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.02em;
}

.ai-btn-group {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

/* 按钮样式 */
.ai-btn {
  position: relative;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-medium);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  white-space: nowrap;
  overflow: hidden;
}

.ai-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.ai-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.ai-btn.primary {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: var(--text-primary);
  border: 2px solid var(--primary-color);
  box-shadow: 0 4px 15px rgba(74, 95, 193, 0.4);
  font-weight: 700;
}

.ai-btn.secondary {
  background: linear-gradient(135deg, #fce4ec 0%, #f8bbd9 100%);
  color: var(--text-primary);
  border: 2px solid #e91e63;
  box-shadow: 0 4px 15px rgba(233, 30, 99, 0.4);
  font-weight: 700;
}

.ai-btn.accent {
  background: linear-gradient(135deg, #e1f5fe 0%, #b3e5fc 100%);
  color: var(--text-primary);
  border: 2px solid var(--accent-color);
  box-shadow: 0 4px 15px rgba(33, 150, 243, 0.4);
  font-weight: 700;
}

/* 功能选择器 */
.feature-selector {
  background: linear-gradient(135deg, #fff8e1 0%, #fffde7 100%);
  border-radius: 16px;
  padding: 1.5rem 2rem;
  margin: 1.5rem 2rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  border: 2px solid #ffd54f;
}

.feature-selector h3 {
  margin: 0 0 1.2rem 0;
  color: #ff6f00;
  font-size: 1.6rem;
  font-weight: 800;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
  letter-spacing: 0.02em;
  text-align: center;
  position: relative;
  padding-bottom: 0.8rem;
}

.feature-selector h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #ffd54f, #ff6f00);
  border-radius: 3px;
}

.table-options {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 1rem;
  padding: 0.5rem;
}

.table-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.8rem 1.2rem;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  border: 2px solid #e0e0e0;
  background: white;
  font-weight: 600;
  color: #424242;
  font-size: 0.95rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  margin: 0.3rem;
}

.table-option:hover {
  background: #f5f5f5;
  border-color: #ffc107;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(255, 193, 7, 0.2);
}

.table-option input[type="checkbox"] {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.table-option .checkmark {
  position: relative;
  display: inline-block;
  width: 1.2rem;
  height: 1.2rem;
  border: 2px solid #ffc107;
  border-radius: 50%;
  transition: all 0.3s ease;
  background: white;
  margin-right: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.table-option:hover .checkmark {
  transform: scale(1.1);
  box-shadow: 0 3px 6px rgba(255, 193, 7, 0.3);
}

.table-option input[type="checkbox"]:checked + .checkmark {
  background: #ffc107;
  border-color: #ff6f00;
  box-shadow: 0 3px 6px rgba(255, 193, 7, 0.4);
}

.table-option input[type="checkbox"]:checked + .checkmark:after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 0.5rem;
  height: 0.5rem;
  background: white;
  border-radius: 50%;
  animation: checkPulse 0.3s ease-out;
}

@keyframes checkPulse {
  0% { transform: translate(-50%, -50%) scale(0); opacity: 0; }
  50% { transform: translate(-50%, -50%) scale(1.5); opacity: 0.7; }
  100% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
}

.table-option input[type="checkbox"]:checked ~ span {
  color: #ff6f00;
  font-weight: 700;
}

.table-option:has(input[type="checkbox"]:checked) {
  background: #fff8e1;
  border-color: #ff6f00;
  box-shadow: 0 6px 12px rgba(255, 193, 7, 0.2);
}

/* 主内容区 */
.ai-main-content {
  padding: 0 2rem;
}

/* 效率概览 */
.efficiency-overview {
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
  border-radius: 16px;
  padding: 1.5rem 2rem;
  margin: 1.5rem 2rem 2rem 2rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  border: 2px solid #90caf9;
}

.efficiency-overview h3 {
  margin: 0 0 1.2rem 0;
  color: #1976d2;
  font-size: 1.6rem;
  font-weight: 800;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
  letter-spacing: 0.02em;
  text-align: center;
  position: relative;
  padding-bottom: 0.8rem;
}

.efficiency-overview h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #90caf9, #1976d2);
  border-radius: 3px;
}

/* 效率等级显示 */
.efficiency-grade {
  display: flex;
  align-items: center;
  padding: 1.5rem;
  border-radius: 16px;
  margin-bottom: 1.5rem;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease;
  border: 2px solid transparent;
}

.efficiency-grade:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.grade-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  font-weight: 900;
  color: white;
  margin-right: 2rem;
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.2);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.grade-info {
  flex: 1;
}

.grade-info h4 {
  margin: 0 0 0.8rem 0;
  font-size: 1.3rem;
  font-weight: 700;
  color: #424242;
}

.grade-text {
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 0.5rem;
  color: #1976d2;
}

.grade-score {
  font-size: 1.4rem;
  font-weight: 600;
  opacity: 0.8;
  color: #616161;
}

.efficiency-description {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  padding: 1.2rem 1.5rem;
  font-size: 1rem;
  color: #616161;
  line-height: 1.6;
  margin-top: 1rem;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  border: 1px solid #e0e0e0;
}

.efficiency-description p {
  margin: 0.5rem 0;
}

.efficiency-cards {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 1.5rem;
  padding: 0.5rem;
}

.efficiency-card {
  background: linear-gradient(135deg, #e8f5e8 0%, #e1f5fe 100%);
  color: #1976d2;
  padding: 1.8rem;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 2px solid #90caf9;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  min-width: 220px;
  flex: 1;
  max-width: 320px;
  position: relative;
  overflow: hidden;
}

.efficiency-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.8) 0%, rgba(255,255,255,0) 70%);
  opacity: 0;
  transform: scale(0.5);
  transition: transform 0.5s, opacity 0.5s;
}

.efficiency-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
  border-color: #1976d2;
}

.efficiency-card:hover::before {
  opacity: 1;
  transform: scale(1);
}

.card-icon {
  font-size: 2.2rem;
  color: #1976d2;
}

.card-content h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: #757575;
}

.card-weight {
  font-size: 0.9rem;
  color: #757575;
  opacity: 0.8;
  display: block;
  margin-top: 0.3rem;
  font-style: italic;
}

.card-value {
  font-size: 2.4rem;
  font-weight: 800;
  display: block;
  color: #1976d2;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
  background: linear-gradient(90deg, #1976d2, #64b5f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-top: 0.3rem;
}

/* 图表网格 */
.ai-charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 2rem;
  margin: 0 2rem 2.5rem;
}

.ai-chart-card {
  background: linear-gradient(135deg, #ffffff 0%, #f5f5f5 100%);
  border-radius: 20px;
  padding: 2.5rem 2rem;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: 2px solid #e0e0e0;
}

.ai-chart-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.12);
  border-color: #90caf9;
}

.ai-chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
  background: linear-gradient(90deg, #1976d2, #64b5f6);
  border-radius: 5px 5px 0 0;
}

.ai-chart-label {
  text-align: center;
  margin-top: 1.5rem;
  font-weight: 700;
  color: #1976d2;
  font-size: 1.1rem;
  letter-spacing: 0.02em;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

/* 智能建议 */
.smart-suggestions {
  background: linear-gradient(135deg, #f3e5f5 0%, #e8f5e9 100%);
  border-radius: 20px;
  padding: 2.5rem;
  margin: 0 2rem 2.5rem;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  border: 2px solid #ce93d8;
}

.smart-suggestions h3 {
  margin: 0 0 1.8rem 0;
  color: #7b1fa2;
  font-size: 1.6rem;
  font-weight: 800;
  letter-spacing: 0.02em;
  text-align: center;
  position: relative;
  padding-bottom: 0.8rem;
}

.smart-suggestions h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #7b1fa2, #ce93d8);
  border-radius: 3px;
}

.suggestions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: 2rem;
  padding: 0.5rem;
}

.suggestion-card {
  background: white;
  border-radius: 16px;
  padding: 1.8rem;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  border-left: 5px solid #9c27b0;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.suggestion-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
  border-left-color: #7b1fa2;
}

.suggestion-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.8) 0%, rgba(255,255,255,0) 70%);
  opacity: 0;
  transform: scale(0.5);
  transition: transform 0.5s, opacity 0.5s;
}

.suggestion-card:hover::before {
  opacity: 1;
  transform: scale(1);
}

.suggestion-icon {
  font-size: 2.2rem;
  margin-bottom: 1rem;
  color: #9c27b0;
}

.suggestion-content h4 {
  margin: 0 0 1rem 0;
  color: #4a148c;
  font-size: 1.4rem;
  font-weight: 800;
  letter-spacing: 0.01em;
}

.suggestion-content p {
  margin: 0 0 1.2rem 0;
  color: #424242;
  line-height: 1.8;
  font-weight: 500;
  font-size: 1.05rem;
}

.suggestion-details {
  margin-bottom: 1.2rem;
}

.suggestion-details .suggestion-main {
  font-weight: 600;
  color: #333;
  font-size: 1.1rem;
  margin-bottom: 1rem;
  padding-bottom: 0.8rem;
  border-bottom: 1px dashed rgba(156, 39, 176, 0.2);
}

.suggestion-details .suggestion-step {
  position: relative;
  padding-left: 1.8rem;
  margin-bottom: 0.8rem;
  transition: all 0.2s ease;
  border-radius: 8px;
  padding-top: 0.4rem;
  padding-bottom: 0.4rem;
  padding-right: 0.4rem;
}

.suggestion-details .suggestion-step:hover {
  background-color: rgba(156, 39, 176, 0.05);
}

.suggestion-details .suggestion-step::before {
  content: attr(data-number);
  position: absolute;
  left: 0;
  top: 0.4rem;
  font-weight: 600;
  color: #9c27b0;
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(156, 39, 176, 0.1);
  border-radius: 50%;
}

.suggestion-priority {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.suggestion-priority.high {
  background: #d32f2f !important;
  color: #ffffff !important;
  font-weight: 700;
  box-shadow: 0 2px 4px rgba(211, 47, 47, 0.3);
}

.suggestion-priority.medium {
  background: #f57c00 !important;
  color: #ffffff !important;
  font-weight: 700;
  box-shadow: 0 2px 4px rgba(245, 124, 0, 0.3);
}

.suggestion-priority.low {
  background: #2e7d32 !important;
  color: #ffffff !important;
  font-weight: 700;
  box-shadow: 0 2px 4px rgba(46, 125, 50, 0.3);
}

/* 个性化计划 */
.personalized-plan {
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-medium);
  backdrop-filter: blur(10px);
}

.personalized-plan h3 {
  margin: 0 0 1.5rem 0;
  color: #1a1a1a !important;
  font-size: 1.5rem;
  font-weight: 800;
  text-shadow: none;
  letter-spacing: -0.01em;
}

.plan-timeline {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.plan-item {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow-light);
  border-left: 4px solid var(--accent-color);
  transition: transform var(--transition-fast);
}

.plan-item:hover {
  transform: translateX(4px);
}

.plan-time {
  font-weight: 700;
  color: #1a1a1a !important;
  min-width: 80px;
  font-size: 1rem;
}

.plan-content h4 {
  margin: 0 0 0.5rem 0;
  color: #1a1a1a !important;
  font-size: 1.2rem;
  font-weight: 800;
  text-shadow: none;
}

.plan-content p {
  margin: 0 0 0.5rem 0;
  color: #2c3e50 !important;
  line-height: 1.7;
  font-weight: 600;
  font-size: 1rem;
}

.plan-duration {
  font-size: 0.9rem;
  color: var(--text-secondary);
  font-style: italic;
  font-weight: 500;
}

/* 表格样式 */
.ai-table-section {
  margin: 2rem 0 3rem;
  background: linear-gradient(135deg, #e8f5e9 0%, #e3f2fd 100%);
  border-radius: 20px;
  padding: 2.5rem;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  border: 2px solid #64b5f6;
}

.ai-table-section h3 {
  margin: 0 0 1.8rem 0;
  color: #1976d2;
  font-size: 1.6rem;
  font-weight: 800;
  text-shadow: none;
  letter-spacing: 0.02em;
  text-align: center;
  position: relative;
  padding-bottom: 0.8rem;
}

.ai-table-section h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #1976d2, #64b5f6);
  border-radius: 3px;
}

.table-container {
  overflow-x: auto;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  background: white;
  padding: 0.5rem;
  margin-top: 1.5rem;
}

.ai-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.ai-table th {
  background: linear-gradient(135deg, #1976d2, #64b5f6);
  color: white !important;
  padding: 1.2rem 1.5rem;
  text-align: left;
  font-weight: 700;
  font-size: 1rem;
  letter-spacing: 0.03em;
  text-transform: uppercase;
  border: none;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  position: relative;
}

.ai-table th:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 25%;
  height: 50%;
  width: 1px;
  background-color: rgba(255, 255, 255, 0.3);
}

.ai-table td {
  padding: 1.2rem 1.5rem;
  border-bottom: 1px solid #e0e0e0;
  color: #424242 !important;
  font-size: 1rem;
  font-weight: 500;
  border-left: none;
  border-right: none;
  transition: all 0.2s ease;
}

.ai-table tbody tr {
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.ai-table tbody tr:hover {
  background: #e3f2fd;
  transform: translateY(-2px);
}

.ai-table tbody tr:last-child td {
  border-bottom: none;
}

.trend-up {
  color: #4caf50;
  font-weight: bold;
}

.trend-down {
  color: #f44336;
  font-weight: bold;
}

.trend-stable {
  color: #ff9800;
  font-weight: bold;
}

.trend-up, .trend-down, .trend-stable {
  font-size: 1.2rem;
}

/* AI分析报告 */
.ai-analysis-card {
  background: linear-gradient(135deg, #e8eaf6 0%, #e1f5fe 100%);
  border-radius: 20px;
  padding: 2.5rem;
  margin: 0 2rem 3rem;
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 2px solid #3f51b5;
  position: relative;
  overflow: hidden;
}

.ai-analysis-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 6px;
  background: linear-gradient(90deg, #3f51b5, #2196f3);
}

.ai-analysis-card h2 {
  margin: 0 0 2rem 0;
  color: #3f51b5 !important;
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: 0.02em;
  text-align: center;
  position: relative;
  padding-bottom: 1rem;
}

.ai-analysis-card h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, #3f51b5, #2196f3);
  border-radius: 3px;
}

.ai-report-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.ai-report-block {
  padding: 1.5rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.06);
  border-left: 5px solid #3f51b5;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.ai-report-block:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.ai-report-block.summary {
  border-left-color: #3f51b5;
}

.ai-report-block.comparison {
  border-left-color: #2196f3;
}

.ai-report-block.suggestion {
  border-left-color: #ff9800;
}
  
  .suggestion-list {
    margin-top: 15px;
  }
  
  .suggestion-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 12px;
    padding: 12px 0;
    border-bottom: 1px solid rgba(255, 152, 0, 0.15);
    transition: all 0.3s ease;
  }
  
  .suggestion-item:hover {
    background-color: rgba(255, 152, 0, 0.05);
    padding-left: 8px;
    border-radius: 8px;
  }
  
  .suggestion-item:last-child {
    border-bottom: none;
    margin-bottom: 0;
  }
  
  .suggestion-number {
    color: #ff9800;
    font-weight: 700;
    margin-right: 12px;
    min-width: 24px;
    font-size: 16px;
    background: rgba(255, 152, 0, 0.1);
    height: 24px;
    width: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .suggestion-content-wrapper {
    flex: 1;
  }
  
  .suggestion-content {
    color: #424242;
    line-height: 1.6;
    font-size: 15px;
    display: block;
  }
  
  .suggestion-main-content {
    color: #333;
    line-height: 1.7;
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 10px;
    padding-bottom: 8px;
    border-bottom: 1px dashed rgba(255, 152, 0, 0.2);
  }
  
  .suggestion-steps {
    margin-top: 8px;
  }
  
  .suggestion-step {
    position: relative;
    padding-left: 28px;
    margin-bottom: 8px;
    color: #424242;
    line-height: 1.6;
    font-size: 15px;
    transition: all 0.2s ease;
    padding-top: 4px;
    padding-bottom: 4px;
    padding-right: 4px;
    border-radius: 6px;
  }
  
  .suggestion-step:hover {
    background-color: rgba(255, 152, 0, 0.05);
  }
  
  .suggestion-step::before {
    content: attr(data-number);
    position: absolute;
    left: 0;
    top: 4px;
    font-weight: 600;
    color: #ff9800;
    width: 22px;
    height: 22px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(255, 152, 0, 0.1);
    border-radius: 50%;
  }

.ai-report-block.motivation {
  border-left-color: #4caf50;
}

.ai-report-block strong {
  color: #3f51b5 !important;
  font-weight: 800;
  display: block;
  margin-bottom: 0.8rem;
  font-size: 1.3rem;
  position: relative;
  padding-left: 28px;
}

.ai-report-block strong::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  background-size: contain;
  background-repeat: no-repeat;
}

.ai-report-block.summary strong::before {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%233f51b5'%3E%3Cpath d='M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zM7 10h2v7H7v-7zm4-3h2v10h-2V7zm4 6h2v4h-2v-4z'/%3E%3C/svg%3E");
}

.ai-report-block.comparison strong::before {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%232196f3'%3E%3Cpath d='M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z'/%3E%3C/svg%3E");
}

.ai-report-block.suggestion strong::before {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23ff9800'%3E%3Cpath d='M9 21c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-1H9v1zm3-19C8.14 2 5 5.14 5 9c0 2.38 1.19 4.47 3 5.74V17c0 .55.45 1 1 1h6c.55 0 1-.45 1-1v-2.26c1.81-1.27 3-3.36 3-5.74 0-3.86-3.14-7-7-7zm2.85 11.1l-.85.6V16h-4v-2.3l-.85-.6C7.8 12.16 7 10.63 7 9c0-2.76 2.24-5 5-5s5 2.24 5 5c0 1.63-.8 3.16-2.15 4.1z'/%3E%3C/svg%3E");
}

.ai-report-block.motivation strong::before {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%234caf50'%3E%3Cpath d='M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z'/%3E%3C/svg%3E");
}

.ai-report-block span {
  color: #424242 !important;
  line-height: 1.8;
  font-weight: 500;
  font-size: 1.05rem;
}

/* 动画效果 */
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translate3d(0, 30px, 0);
  }
  to {
    opacity: 1;
    transform: translate3d(0, 0, 0);
  }
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translate3d(0, -20px, 0);
  }
  to {
    opacity: 1;
    transform: translate3d(0, 0, 0);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .ai-analysis-wrapper {
    margin: 1rem;
  }
  
  .ai-main-content {
    padding: 0 1rem;
  }
  
  .ai-charts-grid {
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .ai-analysis-bg {
    padding: 1rem 0.5rem;
  }
  
  .ai-header {
    padding: 1.5rem;
    flex-direction: column;
    text-align: center;
  }
  
  .ai-title {
    font-size: 1.5rem;
  }
  
  .ai-btn-group {
    justify-content: center;
  }
  
  .ai-main-content {
    padding: 0 1rem;
  }
  
  .feature-selector,
  .efficiency-overview,
  .smart-suggestions,
  .personalized-plan,
  .ai-table-section,
  .ai-analysis-card {
    margin-left: 0;
    margin-right: 0;
  }
  
  .ai-charts-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .efficiency-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.75rem;
  }
  
  .suggestions-grid {
    grid-template-columns: 1fr;
  }
  
  .table-options {
    grid-template-columns: 1fr;
  }
  
  .plan-item {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .plan-time {
    min-width: auto;
  }
}

@media (max-width: 480px) {
  .ai-analysis-bg {
    padding: 0.5rem;
  }
  
  .ai-header {
    padding: 1rem;
  }
  
  .ai-title {
    font-size: 1.3rem;
  }
  
  .ai-btn {
    padding: 0.6rem 1rem;
    font-size: 0.8rem;
  }
  
  .efficiency-cards {
    grid-template-columns: 1fr;
  }
  
  .ai-table {
    font-size: 0.8rem;
  }
  
  .ai-table th,
  .ai-table td {
    padding: 0.5rem;
  }
  
  .no-tasks-notice {
    padding: 2rem 1rem;
  }
  
  .no-tasks-notice h2 {
    font-size: 1.5rem;
  }
  
  .notice-steps {
    flex-direction: column;
    gap: 1rem;
  }
  
  .go-to-tasks-btn {
    padding: 0.8rem 1.5rem;
    font-size: 0.9rem;
  }
}

/* 无任务提示样式 */
.no-tasks-notice {
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  padding: 3rem 2rem;
  margin: 2rem 0;
  text-align: center;
  box-shadow: var(--shadow-medium);
  backdrop-filter: blur(10px);
}

.notice-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
  opacity: 0.8;
}

.no-tasks-notice h2 {
  color: #000000;
  font-size: 1.8rem;
  font-weight: 800;
  margin-bottom: 1rem;
  letter-spacing: -0.01em;
}

.no-tasks-notice p {
  color: #000000;
  font-size: 1.1rem;
  line-height: 1.7;
  margin-bottom: 2rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.notice-steps {
  display: flex;
  justify-content: center;
  gap: 2rem;
  margin-bottom: 2.5rem;
  flex-wrap: wrap;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.step-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  border: 2px solid #90caf9;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.2);
}

.step-btn:hover {
  background: linear-gradient(135deg, #bbdefb, #90caf9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
  border-color: #2196f3;
}

.step-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(33, 150, 243, 0.2);
}

.step-number {
  width: 2.5rem;
  height: 2.5rem;
  background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.1rem;
  font-family: 'Arial', sans-serif;
}

.step-text {
  color: #000000;
  font-size: 1rem;
  font-weight: 700;
  text-align: center;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  letter-spacing: 0.5px;
}

.go-to-tasks-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.8rem;
  padding: 1.5rem 3rem;
  background: linear-gradient(135deg, #2196f3, #1976d2);
  color: white;
  text-decoration: none;
  border-radius: 16px;
  font-weight: 700;
  font-size: 1.2rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(33, 150, 243, 0.3);
  border: none;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  letter-spacing: 0.5px;
}

.go-to-tasks-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(33, 150, 243, 0.4);
  background: linear-gradient(135deg, #1976d2, #1565c0);
}

.go-to-tasks-btn:active {
  transform: translateY(-1px);
  box-shadow: 0 3px 12px rgba(33, 150, 243, 0.3);
}

/* 打印样式 */
@media print {
  .ai-analysis-bg {
    background: white;
    padding: 0;
  }
  
  .ai-analysis-wrapper {
    box-shadow: none;
    border: 1px solid #ddd;
  }
  
  .ai-btn-group {
    display: none;
  }
  
  .feature-selector {
    display: none;
  }
  
  .no-tasks-notice {
    display: none;
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  .ai-analysis-wrapper {
    border: 2px solid #000;
  }
  
  .ai-btn {
    border: 1px solid #000;
  }
  
  .ai-table {
    border: 1px solid #000;
  }
}

/* 减少动画模式支持 */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>