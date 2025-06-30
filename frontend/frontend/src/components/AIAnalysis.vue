<script setup>
import { ref, computed, watch } from 'vue'
import { Chart, registerables, RadialLinearScale } from 'chart.js'
import { Chart as VueChart } from 'vue-chartjs'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import axios from 'axios'

// 注册所有组件（推荐）：
Chart.register(...registerables)
// 或只注册雷达图需要的
// Chart.register(RadialLinearScale);

// 接收父组件传递的选中任务
const props = defineProps({
  selectedTasks: {
    type: Array,
    default: () => []
  }
})

// 监听选中任务变化，自动分析
watch(() => props.selectedTasks, () => {
  generateAIReport()
}, { immediate: true })

// 动态生成学科和日期
const subjectNames = computed(() => {
  // 只取已到截至时间的任务
  const now = new Date()
  return [...new Set(props.selectedTasks
    .filter(t => t.endTime && new Date(t.endTime) <= now)
    .map(t => t.subject)
    .filter(Boolean)
  )]
})

const dayNames = computed(() => {
  // 只取已到截至时间的任务，按周几
  const weekMap = ['周日','周一','周二','周三','周四','周五','周六']
  return [...new Set(props.selectedTasks
    .filter(t => t.endTime && new Date(t.endTime) <= new Date())
    .map(t => {
      const d = new Date(t.endTime)
      return weekMap[d.getDay()]
    })
    .filter(Boolean)
  )]
})

// 统计每个学科的平均完成度
const subjectData = computed(() => {
  return subjectNames.value.map(subject => {
    const tasks = props.selectedTasks.filter(
      t => t.subject === subject && t.endTime && new Date(t.endTime) <= new Date()
    )
    if (!tasks.length) return 0
    return (
      tasks.reduce((sum, t) => sum + (t.progress || 0), 0) / tasks.length
    ).toFixed(1)
  })
})

// 统计每一天的平均完成度
const dayData = computed(() => {
  const weekMap = ['周日','周一','周二','周三','周四','周五','周六']
  return dayNames.value.map(day => {
    const tasks = props.selectedTasks.filter(
      t => {
        if (!t.endTime) return false
        const d = new Date(t.endTime)
        return weekMap[d.getDay()] === day && d <= new Date()
      }
    )
    if (!tasks.length) return 0
    return (
      tasks.reduce((sum, t) => sum + (t.progress || 0), 0) / tasks.length
    ).toFixed(1)
  })
})

// 图表数据
const radarData = computed(() => ({
  labels: subjectNames.value,
  datasets: [
    {
      label: '学科平均完成度',
      data: subjectData.value,
      backgroundColor: 'rgba(54, 162, 235, 0.2)',
      borderColor: 'rgba(54, 162, 235, 1)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(54, 162, 235, 1)'
    }
  ]
}))

const barData = computed(() => ({
  labels: dayNames.value,
  datasets: [
    {
      label: '每日平均完成度',
      data: dayData.value,
      backgroundColor: 'rgba(75, 192, 192, 0.3)',
      borderColor: 'rgba(75, 192, 192, 1)',
      borderWidth: 2
    }
  ]
}))

// 生成AI分析
async function generateAIReport() {
  if (!props.selectedTasks.length) return
  // 假设后端支持传递任务id数组
  const selectedTaskIds = props.selectedTasks.map(t => t.id)
  suggestionText.value = '正在生成AI建议...'
  try {
    const { data } = await axios.post('/api/analysis/tasks', selectedTaskIds)
    // 用后端返回的数据更新前端
    radarData.value.labels = data.subjectNames
    radarData.value.datasets[0].data = data.subjectData
    barData.value.labels = data.dayNames
    barData.value.datasets[0].data = data.dayData
    planData.value.subjects = data.planSubjects
    planData.value.days = data.planDays
    summaryText.value = `本周平均完成度为${(data.subjectData.reduce((a, b) => a + b, 0) / data.subjectData.length).toFixed(1)}%，其中${data.subjectNames[data.subjectData.indexOf(Math.max(...data.subjectData))]}最高（${Math.max(...data.subjectData)}%），${data.subjectNames[data.subjectData.indexOf(Math.min(...data.subjectData))]}最低（${Math.min(...data.subjectData)}%）。`
    let compareArr = data.subjectData.map((v, i) => v - data.planSubjects[i])
    let compareStr = compareArr.map((v, i) => {
      if (v > 0) return `${data.subjectNames[i]}超出计划${v}%`
      if (v < 0) return `${data.subjectNames[i]}低于计划${-v}%`
      return `${data.subjectNames[i]}与计划持平`
    }).join('，')
    compareText.value = `与原计划对比：${compareStr}。`
    suggestionText.value = data.suggestion || 'AI建议生成失败'
    analysisText.value = `${summaryText.value}\n${compareText.value}\n${suggestionText.value}`
  } catch (e) {
    suggestionText.value = 'AI建议生成失败'
  }
}

// 计算属性：学科对比表数据
const subjectTable = computed(() =>
  radarData.value.labels.map((label, i) => ({
    label,
    current: radarData.value.datasets[0].data[i],
    plan: planData.value.subjects[i],
    diff: radarData.value.datasets[0].data[i] - planData.value.subjects[i]
  }))
)
// 计算属性：每日对比表数据
const dayTable = computed(() =>
  barData.value.labels.map((label, i) => ({
    label,
    current: barData.value.datasets[0].data[i],
    plan: planData.value.days[i],
    diff: barData.value.datasets[0].data[i] - planData.value.days[i]
  }))
)

const tableOptions = ref([
  { key: 'radar', label: '学科雷达图', checked: true },
  { key: 'bar', label: '每日完成度柱状图', checked: true },
  { key: 'subjectTable', label: '学科对比表', checked: true },
  { key: 'dayTable', label: '每日对比表', checked: true }
])
</script>

<template>
  <div class="ai-analysis-bg">
    <div class="ai-analysis-wrapper">
      <!-- 顶部标题区 -->
      <header class="ai-header">
        <h1 class="ai-title">AI 学习任务分析</h1>
        <div class="ai-btn-group">
          <button class="ai-btn" @click="generateAIReport">生成AI分析</button>
          <button class="ai-btn" @click="exportPDF">导出PDF报告</button>
        </div>
      </header>
      <!-- 新增：表格选择区 -->
      <div class="table-options">
        <label v-for="item in tableOptions" :key="item.key" class="table-option">
          <input type="checkbox" v-model="item.checked" />
          {{ item.label }}
        </label>
      </div>

      <!-- 主内容区 -->
      <main class="ai-main-content" ref="pdfRef">
        <section class="ai-charts-row">
          <div v-if="tableOptions.find(i=>i.key==='radar').checked" class="ai-chart-card">
            <VueChart type="radar" :data="radarData" :options="{
              plugins: { legend: { labels: { font: { size: 16 } } } },
              scales: { r: { angleLines: { display: true }, suggestedMin: 0, suggestedMax: 100, pointLabels: { font: { size: 14 } } } }
            }" />
            <div class="ai-chart-label">各学科平均完成度</div>
          </div>
          <div v-if="tableOptions.find(i=>i.key==='bar').checked" class="ai-chart-card">
            <VueChart type="bar" :data="barData" :options="{
              plugins: { legend: { labels: { font: { size: 16 } } } },
              scales: { x: { ticks: { font: { size: 14 } } }, y: { beginAtZero: true, suggestedMax: 100, ticks: { font: { size: 14 } } } }
            }" />
            <div class="ai-chart-label">每日平均完成度</div>
          </div>
        </section>

        <!-- 可选表格区 -->
        <section v-if="tableOptions.find(i=>i.key==='subjectTable').checked" class="ai-table-section">
          <h3>学科对比表</h3>
          <table class="ai-table">
            <thead>
              <tr>
                <th>学科</th>
                <th>当前完成度</th>
                <th>计划完成度</th>
                <th>差值</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in subjectTable" :key="row.label">
                <td>{{ row.label }}</td>
                <td>{{ row.current }}</td>
                <td>{{ row.plan }}</td>
                <td :style="{color: row.diff>0?'#38cfd9':row.diff<0?'#ff4757':'#fff'}">{{ row.diff }}</td>
              </tr>
            </tbody>
          </table>
        </section>

        <section v-if="tableOptions.find(i=>i.key==='dayTable').checked" class="ai-table-section">
          <h3>每日对比表</h3>
          <table class="ai-table">
            <thead>
              <tr>
                <th>日期</th>
                <th>当前完成度</th>
                <th>计划完成度</th>
                <th>差值</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in dayTable" :key="row.label">
                <td>{{ row.label }}</td>
                <td>{{ row.current }}</td>
                <td>{{ row.plan }}</td>
                <td :style="{color: row.diff>0?'#38cfd9':row.diff<0?'#ff4757':'#fff'}">{{ row.diff }}</td>
              </tr>
            </tbody>
          </table>
        </section>

        <section class="ai-analysis-card">
          <h2>AI分析报告</h2>
          <div class="ai-report-section">
            <div class="ai-report-block">
              <strong>完成情况总结：</strong>
              <span>{{ summaryText }}</span>
            </div>
            <div class="ai-report-block">
              <strong>与原计划对比：</strong>
              <span>{{ compareText }}</span>
            </div>
            <div class="ai-report-block">
              <strong>未来学习建议：</strong>
              <span>{{ suggestionText }}</span>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 背景和整体居中 */
.ai-analysis-bg {
  min-height: 100vh;
  width: 100vw;
  /* 使用渐变+SVG背景图片，SVG可换成你喜欢的图案 */
  background: 
    linear-gradient(120deg, #e3f0ff 0%, #cbe5ff 100%),
    url('https://www.transparenttextures.com/patterns/cubes.png');
  background-repeat: repeat;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 0;
  font-family: 'Inter', 'Segoe UI', Arial, sans-serif;
  color: #183153;
  font-size: 1.08rem;
}

/* 主容器卡片 */
.ai-analysis-wrapper {
  margin: 48px 0 48px 0;
  background: rgba(255,255,255,0.70); /* 半透明白色 */
  border-radius: 22px;
  box-shadow: 0 8px 32px rgba(120,180,255,0.13), 0 1.5px 8px 0 rgba(120,180,255,0.10);
  width: 100%;
  max-width: 1080px;
  padding: 0 0 32px 0;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  animation: fadeInUp 0.8s cubic-bezier(.25,.8,.25,1);
}

/* 顶部标题区 */
.ai-header {
  padding: 38px 48px 18px 48px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-radius: 22px 22px 0 0;
  background: rgba(255,255,255,0.7); /* 半透明白色 */
}

.ai-title {
  font-size: 2.2rem;
  color: #1976d2;
  letter-spacing: 2px;
  font-family: 'Inter', 'Segoe UI', Arial, sans-serif;
}

/* 按钮组 */
.ai-btn-group {
  display: flex;
  gap: 18px;
}
.ai-btn {
  position: relative;
  overflow: hidden;
  z-index: 1;
  background: linear-gradient(90deg, #b3e0ff 0%, #5bbcff 100%);
  color: #1976d2;
  font-size: 1rem;
  font-family: 'Inter', 'Segoe UI', Arial, sans-serif;
  font-weight: 700;
  border: none;
  border-radius: 10px;
  padding: 10px 28px;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(120,180,255,0.10);
  transition: background 0.2s, color 0.2s, transform 0.2s;
  letter-spacing: 1px;
}
.ai-btn::before {
  content: "";
  position: absolute;
  left: 50%; top: 50%;
  width: 0; height: 0;
  background: rgba(91,188,255,0.18);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.4s cubic-bezier(.25,.8,.25,1), height 0.4s cubic-bezier(.25,.8,.25,1);
  z-index: -1;
}
.ai-btn:hover {
  background: linear-gradient(90deg, #5bbcff 0%, #b3e0ff 100%);
  color: #1976d2;
  transform: translateY(-2px) scale(1.04);
}
.ai-btn:hover::before {
  width: 220%;
  height: 600%;
}

/* 主内容区 */
.ai-main-content {
  padding: 0 32px;
  display: flex;
  flex-direction: column;
  gap: 36px;
}

/* 图表区 */
.ai-charts-row {
  display: flex;
  gap: 32px;
  width: 100%;
  justify-content: center;
  align-items: stretch;
  margin-top: 18px;
  margin-bottom: 0;
  flex-wrap: wrap;
}

.ai-chart-card {
  background: rgba(255,255,255,0.85); /* 半透明白色 */
  border-radius: 18px;
  box-shadow: 0 2px 16px rgba(120,180,255,0.08);
  padding: 28px 18px 18px 18px;
  width: 100%;
  max-width: 420px;
  min-width: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1 1 280px;
  border: 1px solid #e3f0ff;
  transition: box-shadow 0.3s cubic-bezier(.25,.8,.25,1), transform 0.3s cubic-bezier(.25,.8,.25,1);
  will-change: box-shadow, transform;
  animation: fadeInUp 0.8s cubic-bezier(.25,.8,.25,1);
}
.ai-chart-card:hover {
  box-shadow: 0 8px 32px 0 rgba(91,188,255,0.18), 0 1.5px 8px 0 rgba(91,188,255,0.12);
  transform: translateY(-6px) scale(1.025);
}
.ai-chart-card canvas {
  max-width: 340px !important;
  max-height: 260px !important;
}
.ai-chart-label {
  margin-top: 14px;
  font-size: 1.08rem;
  color: #1976d2;
  font-weight: 700;
  letter-spacing: 1px;
  text-shadow: 0 1px 4px #e3f0ff;
}

/* 分析报告卡片 */
.ai-analysis-card {
  width: 100%;
  background: rgba(255,255,255,0.92);
  padding: 32px 18px;
  border-radius: 18px;
  box-shadow: 0 2px 16px rgba(120,180,255,0.08);
  text-align: center;
  border: 1px solid #e3f0ff;
  transition: box-shadow 0.3s cubic-bezier(.25,.8,.25,1), transform 0.3s cubic-bezier(.25,.8,.25,1);
  will-change: box-shadow, transform;
  animation: fadeInUp 0.8s cubic-bezier(.25,.8,.25,1);
}
.ai-analysis-card:hover {
  box-shadow: 0 8px 32px 0 rgba(91,188,255,0.18), 0 1.5px 8px 0 rgba(91,188,255,0.12);
  transform: translateY(-6px) scale(1.025);
}
.ai-analysis-card h2 {
  color: #1976d2;
  margin-bottom: 18px;
  font-size: 1.5rem;
  font-weight: 800;
  letter-spacing: 1px;
  text-shadow: 0 1px 4px #e3f0ff;
}

/* 报告内容块 */
.ai-report-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
  align-items: flex-start;
  margin: 0 auto;
  max-width: 600px;
}
.ai-report-block {
  background: rgba(255,255,255,0.7); /* 半透明白色 */
  color: #183153;
  border-left: 4px solid #5bbcff;
  padding: 14px 18px;
  box-shadow: 0 1px 4px rgba(91,188,255,0.06);
  font-size: 1.08rem;
  width: 100%;
  text-align: left;
  line-height: 1.7;
  font-family: 'D-DIN-Medium', Arial, Verdana, sans-serif;
  animation: fadeInUp 0.8s cubic-bezier(.25,.8,.25,1);
}
.ai-report-block strong {
  color: #1976d2;
  font-weight: 700;
  margin-right: 8px;
  letter-spacing: 1px;
  text-shadow: 0 1px 8px #b3e0ff;
}
.ai-report-block span {
  transition: color 0.3s;
}
.ai-report-block:hover span {
  color: #5bbcff;
}

/* 新增：表格相关样式 */
.table-options {
  background: rgba(255,255,255,0.7);
  border-radius: 8px;
  padding: 8px 24px;
  margin-bottom: 12px;
  display: flex;
  gap: 18px;
  position: relative;
  z-index: 10; /* 增加层级，确保不被覆盖 */
}
.table-option {
  font-size: 1.02rem;
  color: #1976d2;
}
.ai-table-section {
  margin: 24px 0;
  background: rgba(255,255,255,0.85); /* 半透明白色 */
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 2px 8px rgba(120,180,255,0.08);
}
.ai-table {
  width: 100%;
  border-collapse: collapse;
  color: #1976d2;
  font-size: 1rem;
  background: transparent;
}
.ai-table th, .ai-table td {
  background: transparent;
  border-bottom: 1px solid #b3e0ff;
  padding: 8px 12px;
  text-align: center;
}
.ai-table th {
  font-size: 1.05rem;
  color: #1976d2;
  font-weight: 700;
}

/* 动画 */
@keyframes fadeInUp {
  from { opacity: 0; transform: translate3d(0, 40px, 0);}
  to { opacity: 1; transform: none;}
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .ai-analysis-wrapper {
    padding: 0 0 18px 0;
  }
  .ai-header {
    padding: 24px 18px 10px 18px;
  }
  .ai-main-content {
    padding: 0 8px;
  }
  .ai-charts-row {
    gap: 18px;
  }
}

@media (max-width: 900px) {
  .ai-charts-row {
    flex-direction: column;
    align-items: center;
    gap: 18px;
  }
  .ai-chart-card {
    width: 98%;
    min-width: unset;
    max-width: 100vw;
  }
}

@media (max-width: 600px) {
  .ai-title {
    font-size: 1.3rem;
  }
  .ai-header h1,
  .ai-analysis-card h2,
  .ai-table-section h3 {
    font-size: 1.08rem;
  }
  .ai-table th,
  .ai-table td,
  .ai-report-block {
    font-size: 0.98rem;
  }
}

/* 背景模糊效果 */
.ai-analysis-wrapper,
.ai-header,
.ai-chart-card,
.ai-analysis-card,
.ai-table-section {
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px); /* 兼容 Safari */
}
</style>
