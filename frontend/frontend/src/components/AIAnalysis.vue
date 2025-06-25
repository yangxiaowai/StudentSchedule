<script setup>
import { ref, computed } from 'vue'
import { Chart, registerables } from 'chart.js'
import { Chart as VueChart } from 'vue-chartjs'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'

Chart.register(...registerables)

// 示例数据
const radarData = ref({
  labels: ['语文', '数学', '英语', '物理', '化学'],
  datasets: [
    {
      label: '学科平均完成度',
      data: [85, 78, 92, 70, 65],
      backgroundColor: 'rgba(54, 162, 235, 0.2)',
      borderColor: 'rgba(54, 162, 235, 1)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(54, 162, 235, 1)'
    }
  ]
})

const barData = ref({
  labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  datasets: [
    {
      label: '每日平均完成度',
      data: [80, 75, 90, 85, 70, 60, 95],
      backgroundColor: 'rgba(75, 192, 192, 0.3)',
      borderColor: 'rgba(75, 192, 192, 1)',
      borderWidth: 2
    }
  ]
})

// 原计划数据（示例）
const planData = ref({
  subjects: [80, 80, 85, 75, 70], // 语文、数学、英语、物理、化学
  days: [85, 80, 85, 80, 75, 70, 90]
})

// AI分析报告内容
const analysisText = ref('')
const suggestionText = ref('')
const compareText = ref('')
const summaryText = ref('')

// 生成AI分析
function generateAIReport() {
  // 完成情况总结
  const avg = arr => arr.reduce((a, b) => a + b, 0) / arr.length
  const subjectNames = ['语文', '数学', '英语', '物理', '化学']
  const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const subjectData = radarData.value.datasets[0].data
  const dayData = barData.value.datasets[0].data
  const planSubjects = planData.value.subjects
  const planDays = planData.value.days

  // 总结
  summaryText.value = `本周平均完成度为${avg(subjectData).toFixed(1)}%，其中${subjectNames[subjectData.indexOf(Math.max(...subjectData))]}最高（${Math.max(...subjectData)}%），${subjectNames[subjectData.indexOf(Math.min(...subjectData))]}最低（${Math.min(...subjectData)}%）。`

  // 对比原计划
  let compareArr = subjectData.map((v, i) => v - planSubjects[i])
  let compareStr = compareArr.map((v, i) => {
    if (v > 0) return `${subjectNames[i]}超出计划${v}%`
    if (v < 0) return `${subjectNames[i]}低于计划${-v}%`
    return `${subjectNames[i]}与计划持平`
  }).join('，')
  compareText.value = `与原计划对比：${compareStr}。`

  // 建议
  let minSubject = subjectNames[subjectData.indexOf(Math.min(...subjectData))]
  let minDay = dayNames[dayData.indexOf(Math.min(...dayData))]
  suggestionText.value = `建议加强${minSubject}的学习投入，合理安排${minDay}的学习时间，保持整体学习节奏。可适当调整计划，关注薄弱环节。`

  // 综合分析
  analysisText.value = `${summaryText.value}\n${compareText.value}\n${suggestionText.value}`
}

// 默认加载一次
generateAIReport()

// PDF导出相关
const pdfRef = ref(null)
async function exportPDF() {
  const pdfDom = pdfRef.value
  if (!pdfDom) return
  const canvas = await html2canvas(pdfDom, { scale: 2, useCORS: true })
  const imgData = canvas.toDataURL('image/png')
  const pdf = new jsPDF('p', 'mm', 'a4')
  const pageWidth = pdf.internal.pageSize.getWidth()
  const pageHeight = pdf.internal.pageSize.getHeight()
  const imgProps = pdf.getImageProperties(imgData)
  const pdfWidth = pageWidth - 20
  const pdfHeight = (imgProps.height * pdfWidth) / imgProps.width
  pdf.addImage(imgData, 'PNG', 10, 10, pdfWidth, pdfHeight)
  pdf.save('学习任务分析报告.pdf')
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
</script>

<template>
  <div class="analysis-main">
    <header class="header">
      <h1 class="title">学习任务分析</h1>
      <div class="btn-group">
        <button class="btn" @click="generateAIReport">生成AI分析</button>
        <button class="btn" @click="exportPDF">导出PDF报告</button>
      </div>
    </header>
    <main class="main-content" ref="pdfRef">
      <section class="charts-row">
        <div class="chart-card">
          <VueChart type="radar" :data="radarData" :options="{
            plugins: { legend: { labels: { font: { size: 16 } } } },
            scales: { r: { angleLines: { display: true }, suggestedMin: 0, suggestedMax: 100, pointLabels: { font: { size: 14 } } } }
          }" />
          <div class="chart-label">各学科平均完成度</div>
        </div>
        <div class="chart-card">
          <VueChart type="bar" :data="barData" :options="{
            plugins: { legend: { labels: { font: { size: 16 } } } },
            scales: { x: { ticks: { font: { size: 14 } } }, y: { beginAtZero: true, suggestedMax: 100, ticks: { font: { size: 14 } } } }
          }" />
          <div class="chart-label">每日平均完成度</div>
        </div>
      </section>
      <!-- 对比表已删除 -->
      <section class="analysis-card">
        <h2>AI分析报告</h2>
        <div class="ai-section">
          <div class="ai-block">
            <strong>完成情况总结：</strong>
            <span>{{ summaryText }}</span>
          </div>
          <div class="ai-block">
            <strong>与原计划对比：</strong>
            <span>{{ compareText }}</span>
          </div>
          <div class="ai-block">
            <strong>未来学习建议：</strong>
            <span>{{ suggestionText }}</span>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
}
.analysis-main {
  min-height: 100vh;
  width: 100vw;
  background: linear-gradient(120deg, #e3ecfa 0%, #f9f9f9 100%);
  display: flex;
  flex-direction: column;
  justify-content: stretch;
  align-items: stretch;
  box-sizing: border-box;
  padding: 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 36px 48px 18px 48px;
  background: transparent;
}

.title {
  font-size: 2.6rem;
  color: #2a4d69;
  letter-spacing: 2px;
  font-weight: 800;
  margin: 0;
}

.btn-group {
  display: flex;
  gap: 16px;
}
.btn {
  background: linear-gradient(90deg, #4fa3f7 0%, #38cfd9 100%);
  color: #fff;
  font-size: 1.05rem;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  padding: 10px 24px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(60, 120, 180, 0.10);
  transition: background 0.2s, transform 0.2s;
}
.btn:hover {
  background: linear-gradient(90deg, #38cfd9 0%, #4fa3f7 100%);
  transform: translateY(-2px) scale(1.04);
}

.main-content {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: stretch;
  padding: 0 48px 36px 48px;
  box-sizing: border-box;
}

.charts-row {
  display: flex;
  gap: 48px;
  width: 100%;
  justify-content: center;
  align-items: stretch;
  margin-bottom: 40px;
  flex-wrap: wrap;
  flex: 1 1 0;
}

.chart-card {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(60, 120, 180, 0.10);
  padding: 36px 24px 24px 24px;
  width: 100%;
  max-width: 480px;
  min-width: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1 1 340px;
}

.chart-card canvas {
  max-width: 400px !important;
  max-height: 320px !important;
}

.chart-label {
  margin-top: 18px;
  font-size: 1.18rem;
  color: #3a5a7a;
  font-weight: 500;
  letter-spacing: 1px;
}

.analysis-card {
  width: 100%;
  background: linear-gradient(90deg, #e3ecfa 0%, #f9f9f9 100%);
  padding: 40px 36px;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(60, 120, 180, 0.10);
  text-align: center;
  margin-top: 0;
  margin-bottom: 0;
  flex: 0 0 auto;
}

.analysis-card h2 {
  color: #2a4d69;
  margin-bottom: 22px;
  font-size: 1.7rem;
  font-weight: 700;
}

.ai-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
  align-items: flex-start;
  margin: 0 auto;
  max-width: 700px;
}
.ai-block {
  background: #fff;
  border-radius: 8px;
  padding: 16px 18px;
  box-shadow: 0 2px 8px rgba(60, 120, 180, 0.06);
  font-size: 1.13rem;
  color: #444;
  width: 100%;
  text-align: left;
  line-height: 1.8;
}

/* 新增表格样式 */
.table-section {
  margin: 32px 0 0 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(60,120,180,0.06);
  padding: 24px 18px;
}
.table-section h3 {
  margin-bottom: 12px;
  color: #2a4d69;
  font-size: 1.2rem;
  font-weight: 600;
}
.report-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 0;
  font-size: 1.05rem;
}
.report-table th, .report-table td {
  border: 1px solid #e3ecfa;
  padding: 8px 12px;
  text-align: center;
}
.report-table th {
  background: #f4f8fb;
  color: #2a4d69;
  font-weight: 700;
}

@media (max-width: 1200px) {
  .main-content {
    padding: 0 16px 24px 16px;
  }
  .header {
    padding: 28px 16px 12px 16px;
  }
  .charts-row {
    gap: 24px;
  }
}

@media (max-width: 900px) {
  .charts-row {
    flex-direction: column;
    align-items: center;
    gap: 24px;
  }
  .chart-card {
    width: 98%;
    min-width: unset;
    max-width: 100vw;
  }
}

@media (max-width: 600px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 18px 4vw 8px 4vw;
  }
  .main-content {
    padding: 0 2vw 12px 2vw;
  }
  .analysis-card {
    padding: 24px 6px;
  }
  .chart-card {
    padding: 18px 2vw 12px 2vw;
  }
  .title {
    font-size: 1.5rem;
  }
}
</style>
