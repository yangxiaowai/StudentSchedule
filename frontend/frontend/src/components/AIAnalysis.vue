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

// 学习效率分析
const efficiencyData = computed(() => {
  const now = new Date()
  const recentTasks = props.selectedTasks.filter(t => {
    if (!t.endTime) return false
    const taskDate = new Date(t.endTime)
    const daysDiff = (now - taskDate) / (1000 * 60 * 60 * 24)
    return daysDiff <= 7 && taskDate <= now
  })
  
  return {
    totalTasks: recentTasks.length,
    completedTasks: recentTasks.filter(t => t.progress >= 100).length,
    averageProgress: recentTasks.length > 0 
      ? (recentTasks.reduce((sum, t) => sum + (t.progress || 0), 0) / recentTasks.length).toFixed(1)
      : 0,
    efficiency: recentTasks.length > 0 
      ? ((recentTasks.filter(t => t.progress >= 100).length / recentTasks.length) * 100).toFixed(1)
      : 0
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
  { key: 'prediction', label: '预测分析图表', checked: true },
  { key: 'pattern', label: '学习模式分析', checked: true },
  { key: 'efficiency', label: '学习效率统计', checked: true },
  { key: 'subjectTable', label: '学科对比表', checked: true },
  { key: 'dayTable', label: '每日对比表', checked: true },
  { key: 'smartSuggestions', label: '智能建议卡片', checked: true },
  { key: 'personalizedPlan', label: '个性化学习计划', checked: true }
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
    const { data } = await axios.post('/api/analysis/smart-suggestions', {
      taskIds: props.selectedTasks.map(t => t.id),
      efficiencyData: efficiencyData.value
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
    const { data } = await axios.post('/api/analysis/personalized-plan', {
      taskIds: props.selectedTasks.map(t => t.id),
      goals: learningGoals.value,
      patterns: studyPatterns.value
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
  if (!props.selectedTasks.length || loading.value.analysis) return
  loading.value.analysis = true
  error.value.show = false
  suggestionText.value = '正在生成AI建议...'
  
  try {
    const { data } = await axios.post('/api/analysis/tasks', 
      props.selectedTasks.map(t => t.id)
    )
    
    // 更新图表数据
    radarData.value.labels = data.subjectNames
    radarData.value.datasets[0].data = data.subjectData
    barData.value.labels = data.dayNames
    barData.value.datasets[0].data = data.dayData
    planData.value.subjects = data.planSubjects
    planData.value.days = data.planDays
    
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

// 监听选中任务变化
watch(() => props.selectedTasks, (newTasks) => {
  if (newTasks.length) {
    generateAIReport()
  }
}, { deep: true })

// 生命周期钩子
onMounted(() => {
  if (props.selectedTasks.length) {
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
            :disabled="loading.analysis || !props.selectedTasks.length"
          >
            <i class="icon-brain"></i>
            {{ loading.analysis ? '生成中...' : '生成AI分析' }}
          </button>
          <button 
            class="ai-btn secondary" 
            @click="exportEnhancedPDF"
            :disabled="loading.analysis || !props.selectedTasks.length"
          >
            <i class="icon-download"></i>导出增强报告
          </button>
          <button 
            class="ai-btn accent" 
            @click="generatePersonalizedPlan"
            :disabled="loading.plan || !props.selectedTasks.length"
          >
            <i class="icon-target"></i>
            {{ loading.plan ? '生成中...' : '生成学习计划' }}
          </button>
        </div>
      </header>

      <!-- 无任务提示 -->
      <div v-if="!props.selectedTasks.length" class="no-tasks-notice">
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
      <div v-if="props.selectedTasks.length" class="feature-selector">
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
            {{ item.label }}
          </label>
        </div>
      </div>

      <!-- 主内容区 -->
      <main v-if="props.selectedTasks.length" class="ai-main-content" ref="pdfRef">
        <!-- 学习效率概览 -->
        <section 
          v-if="tableOptions.find(i=>i.key==='efficiency').checked" 
          class="efficiency-overview"
        >
          <h3>📈 学习效率概览</h3>
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
                <h4>平均进度</h4>
                <span class="card-value">{{ efficiencyData.averageProgress }}%</span>
              </div>
            </div>
            <div class="efficiency-card">
              <div class="card-icon">🎯</div>
              <div class="card-content">
                <h4>学习效率</h4>
                <span class="card-value">{{ efficiencyData.efficiency }}%</span>
              </div>
            </div>
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

          <!-- 预测分析图表 -->
          <div 
            v-if="tableOptions.find(i=>i.key==='prediction').checked" 
            class="ai-chart-card"
          >
            <VueChart 
              type="line" 
              :data="predictionData" 
              :options="{
                plugins: { 
                  legend: { labels: { font: { size: 16 } } },
                  title: { display: true, text: '下周学习完成度预测' }
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
            <div class="ai-chart-label">🔮 预测分析</div>
          </div>

          <!-- 学习模式分析 -->
          <div 
            v-if="tableOptions.find(i=>i.key==='pattern').checked" 
            class="ai-chart-card"
          >
            <VueChart 
              type="doughnut" 
              :data="patternData" 
              :options="{
                plugins: { 
                  legend: { labels: { font: { size: 16 } } },
                  title: { display: true, text: '不同时段学习效率' }
                }
              }" 
            />
            <div class="ai-chart-label">⏰ 学习模式分析</div>
          </div>
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
                <p>{{ suggestion.content }}</p>
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

        <!-- 个性化学习计划 -->
        <section 
          v-if="tableOptions.find(i=>i.key==='personalizedPlan').checked && personalizedPlan.length" 
          class="personalized-plan"
        >
          <h3>🎯 个性化学习计划</h3>
          <div class="plan-timeline">
            <div 
              v-for="(item, index) in personalizedPlan" 
              :key="index" 
              class="plan-item"
            >
              <div class="plan-time">{{ item.time }}</div>
              <div class="plan-content">
                <h4>{{ item.subject }}</h4>
                <p>{{ item.task }}</p>
                <div class="plan-duration">预计用时: {{ item.duration }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 对比表格 -->
        <section 
          v-if="tableOptions.find(i=>i.key==='subjectTable').checked" 
          class="ai-table-section"
        >
          <h3>📊 学科对比表</h3>
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
        </section>

        <section 
          v-if="tableOptions.find(i=>i.key==='dayTable').checked" 
          class="ai-table-section"
        >
          <h3>📅 每日对比表</h3>
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
              <span>{{ suggestionText }}</span>
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
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  padding: 2rem 2.5rem;
  margin: 2rem 2rem;
  box-shadow: var(--shadow-medium);
  backdrop-filter: blur(10px);
}

.feature-selector h3 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.4rem;
  font-weight: 800;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.01em;
}

.table-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.5rem;
}

.table-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.5rem;
  border-radius: 10px;
  cursor: pointer;
  transition: all var(--transition-fast);
  user-select: none;
  border: 2px solid var(--border-color-strong);
  background: rgba(255, 255, 255, 0.95);
  font-weight: 600;
  color: #000000;
  font-size: 1rem;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

.table-option:hover {
  background: rgba(74, 95, 193, 0.15);
  border-color: var(--primary-color);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(74, 95, 193, 0.2);
}

.table-option input[type="checkbox"] {
  width: 1.2rem;
  height: 1.2rem;
  accent-color: var(--primary-color);
}

.checkmark {
  position: relative;
  display: inline-block;
  width: 1.2rem;
  height: 1.2rem;
  border: 2px solid var(--primary-color);
  border-radius: 4px;
  transition: all var(--transition-fast);
}

/* 主内容区 */
.ai-main-content {
  padding: 0 2rem;
}

/* 效率概览 */
.efficiency-overview {
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-medium);
  backdrop-filter: blur(10px);
}

.efficiency-overview h3 {
  margin: 0 0 1.5rem 0;
  color: #2c3e50;
  font-size: 1.5rem;
  font-weight: 800;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.01em;
}

.efficiency-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
}

.efficiency-card {
  background: linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%);
  color: var(--text-primary);
  padding: 2rem;
  border-radius: 14px;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  transition: transform var(--transition-medium), box-shadow var(--transition-medium);
  cursor: pointer;
  border: 2px solid var(--success-color);
}

.efficiency-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.card-icon {
  font-size: 2rem;
  opacity: 0.9;
}

.card-content h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.card-value {
  font-size: 2rem;
  font-weight: 800;
  display: block;
  color: var(--text-primary);
}

/* 图表网格 */
.ai-charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.ai-chart-card {
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  padding: 2rem;
  box-shadow: var(--shadow-medium);
  backdrop-filter: blur(10px);
  transition: transform var(--transition-medium), box-shadow var(--transition-medium);
  position: relative;
  overflow: hidden;
}

.ai-chart-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-heavy);
}

.ai-chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
}

.ai-chart-label {
  text-align: center;
  margin-top: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 1rem;
}

/* 智能建议 */
.smart-suggestions {
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-medium);
  backdrop-filter: blur(10px);
}

.smart-suggestions h3 {
  margin: 0 0 1.5rem 0;
  color: #2c3e50;
  font-size: 1.5rem;
  font-weight: 800;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.01em;
}

.suggestions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 1.5rem;
}

.suggestion-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: var(--shadow-light);
  border-left: 4px solid var(--primary-color);
  transition: transform var(--transition-medium), box-shadow var(--transition-medium);
}

.suggestion-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

.suggestion-icon {
  font-size: 2rem;
  margin-bottom: 0.75rem;
}

.suggestion-content h4 {
  margin: 0 0 0.75rem 0;
  color: var(--text-primary);
  font-size: 1.3rem;
  font-weight: 800;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.suggestion-content p {
  margin: 0 0 1rem 0;
  color: var(--text-secondary);
  line-height: 1.7;
  font-weight: 500;
  font-size: 1rem;
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
  background: rgba(244, 67, 54, 0.1);
  color: var(--error-color);
}

.suggestion-priority.medium {
  background: rgba(255, 152, 0, 0.1);
  color: var(--warning-color);
}

.suggestion-priority.low {
  background: rgba(76, 175, 80, 0.1);
  color: var(--success-color);
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
  color: #2c3e50;
  font-size: 1.5rem;
  font-weight: 800;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
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
  font-weight: 600;
  color: var(--primary-color);
  min-width: 80px;
  font-size: 1rem;
}

.plan-content h4 {
  margin: 0 0 0.5rem 0;
  color: var(--text-primary);
  font-size: 1.2rem;
  font-weight: 800;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.plan-content p {
  margin: 0 0 0.5rem 0;
  color: var(--text-secondary);
  line-height: 1.7;
  font-weight: 500;
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
  margin: 1.5rem 0;
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  padding: 2rem;
  box-shadow: var(--shadow-medium);
  backdrop-filter: blur(10px);
}

.ai-table-section h3 {
  margin: 0 0 1.5rem 0;
  color: #2c3e50;
  font-size: 1.5rem;
  font-weight: 800;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.01em;
}

.ai-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: var(--shadow-light);
}

.ai-table th {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: var(--text-primary);
  padding: 1.2rem;
  text-align: left;
  font-weight: 800;
  font-size: 1.1rem;
  border: 2px solid var(--primary-color);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.ai-table td {
  padding: 1rem 1.2rem;
  border-bottom: 1px solid var(--border-color);
  color: var(--text-secondary);
  font-size: 1rem;
  font-weight: 500;
  border-left: 1px solid var(--border-color);
  border-right: 1px solid var(--border-color);
}

.ai-table tbody tr:hover {
  background: rgba(102, 126, 234, 0.05);
}

.ai-table tbody tr:last-child td {
  border-bottom: none;
}

.trend-up, .trend-down, .trend-stable {
  font-size: 1.2rem;
}

/* AI分析报告 */
.ai-analysis-card {
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-medium);
  backdrop-filter: blur(10px);
}

.ai-analysis-card h2 {
  margin: 0 0 1.5rem 0;
  color: var(--text-primary);
  font-size: 1.8rem;
  font-weight: 800;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.01em;
}

.ai-report-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.ai-report-block {
  padding: 1rem;
  background: white;
  border-radius: 8px;
  box-shadow: var(--shadow-light);
  border-left: 4px solid var(--primary-color);
}

.ai-report-block.summary {
  border-left-color: var(--primary-color);
}

.ai-report-block.comparison {
  border-left-color: var(--accent-color);
}

.ai-report-block.suggestion {
  border-left-color: var(--warning-color);
}

.ai-report-block.motivation {
  border-left-color: var(--success-color);
}

.ai-report-block strong {
  color: var(--text-primary);
  font-weight: 800;
  display: block;
  margin-bottom: 0.5rem;
  font-size: 1.2rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.ai-report-block span {
  color: var(--text-secondary);
  line-height: 1.7;
  font-weight: 500;
  font-size: 1rem;
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