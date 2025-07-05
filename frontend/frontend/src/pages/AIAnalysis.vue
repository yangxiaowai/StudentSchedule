<script setup>
import { ref, computed, onMounted } from 'vue'
import { Chart, registerables } from 'chart.js'
import { Chart as VueChart } from 'vue-chartjs'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import { getTaskAnalysis } from '../api/analysis'
import axios from 'axios'

// 注册Chart.js组件
Chart.register(...registerables)

// 初始化空数据
const radarData = ref({
  labels: [],
  datasets: [
    {
      label: '学科平均完成度',
      data: [],
      backgroundColor: 'rgba(54, 162, 235, 0.2)',
      borderColor: 'rgba(54, 162, 235, 1)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(54, 162, 235, 1)'
    }
  ]
})

const barData = ref({
  labels: [],
  datasets: [
    {
      label: '每日平均完成度',
      data: [],
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

// 控制图表显示状态
const showRadarChart = ref(false)
const showBarChart = ref(false)

// AI分析报告内容
const analysisText = ref('')
const suggestionText = ref('')
const compareText = ref('')
const summaryText = ref('')

// 生成AI分析
async function generateAIReport() {
  // 假设你有任务ID数组
  const selectedTaskIds = [1, 2, 3, 4];
  suggestionText.value = '正在生成AI建议...';
  try {
    console.log('正在调用任务分析API...');
    const { data } = await getTaskAnalysis(selectedTaskIds);
    console.log('API返回数据:', data);
    
    // 用后端返回的数据更新前端
    if (data) {
      // 更新雷达图数据
      if (data.subjectData && data.subjectData.length > 0) {
        radarData.value.datasets[0].data = data.subjectData;
        if (data.subjectNames && data.subjectNames.length > 0) {
          radarData.value.labels = data.subjectNames;
        }
      }
      
      // 更新柱状图数据
      if (data.dayData && data.dayData.length > 0) {
        barData.value.datasets[0].data = data.dayData;
        if (data.dayNames && data.dayNames.length > 0) {
          barData.value.labels = data.dayNames;
        }
      }
      
      // 更新计划数据
      if (data.planSubjects) planData.value.subjects = data.planSubjects;
      if (data.planDays) planData.value.days = data.planDays;
      
      // 生成分析文本
      if (data.subjectData && data.subjectNames) {
        summaryText.value = `本周平均完成度为${(data.subjectData.reduce((a, b) => a + b, 0) / data.subjectData.length).toFixed(1)}%，其中${data.subjectNames[data.subjectData.indexOf(Math.max(...data.subjectData))]}最高（${Math.max(...data.subjectData)}%），${data.subjectNames[data.subjectData.indexOf(Math.min(...data.subjectData))]}最低（${Math.min(...data.subjectData)}%）。`;
        
        let compareArr = data.subjectData.map((v, i) => v - data.planSubjects[i]);
        let compareStr = compareArr.map((v, i) => {
          if (v > 0) return `${data.subjectNames[i]}超出计划${v}%`;
          if (v < 0) return `${data.subjectNames[i]}低于计划${-v}%`;
          return `${data.subjectNames[i]}与计划持平`;
        }).join('，');
        compareText.value = `与原计划对比：${compareStr}。`;
      }
      
      suggestionText.value = data.suggestion || 'AI建议生成失败';
      analysisText.value = `${summaryText.value}
${compareText.value}
${suggestionText.value}`;
    }
  } catch (e) {
    console.error('生成AI报告失败:', e);
    suggestionText.value = 'AI建议生成失败';
  }
}

// 在组件挂载时加载数据
onMounted(() => {
  console.log('组件已挂载，开始生成AI报告...');
  generateAIReport();
})

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

// 表格相关的计算属性已删除


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
      <!-- 图表区域 -->
      <div class="chart-controls">
        <button class="chart-btn" @click="showRadarChart = !showRadarChart">
          {{ showRadarChart ? '隐藏' : '显示' }}各学科平均完成度
        </button>
        <button class="chart-btn" @click="showBarChart = !showBarChart">
          {{ showBarChart ? '隐藏' : '显示' }}每日平均完成度
        </button>
      </div>
      
      <div class="chart-row" v-if="showRadarChart || showBarChart">
        <!-- 雷达图 -->
        <div class="chart-card" v-if="showRadarChart">
          <h3>各学科平均完成度</h3>
          <VueChart
            type="radar"
            :data="radarData"
            :options="{
              responsive: true,
              maintainAspectRatio: false,
              plugins: {
                legend: {
                  position: 'top',
                },
                title: {
                  display: false,
                }
              },
              scales: {
                r: {
                  angleLines: { display: true },
                  suggestedMin: 0,
                  suggestedMax: 100,
                  ticks: { stepSize: 20 }
                }
              }
            }"
            :style="{height: '300px'}"
          />
        </div>
        
        <!-- 柱状图 -->
        <div class="chart-card" v-if="showBarChart">
          <h3>每日平均完成度</h3>
          <VueChart
            type="bar"
            :data="barData"
            :options="{
              responsive: true,
              maintainAspectRatio: false,
              plugins: {
                legend: {
                  position: 'top',
                },
                title: {
                  display: false,
                }
              },
              scales: {
                y: {
                  beginAtZero: true,
                  max: 100
                }
              }
            }"
            :style="{height: '300px'}"
          />
        </div>
      </div>
      

 
      
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

/* 图表控制按钮样式 */
.chart-controls {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 24px;
}

.chart-btn {
  background: linear-gradient(90deg, #4fa3f7 0%, #38cfd9 100%);
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(60, 120, 180, 0.10);
  transition: all 0.2s ease;
}

.chart-btn:hover {
  background: linear-gradient(90deg, #38cfd9 0%, #4fa3f7 100%);
  transform: translateY(-2px);
}

/* 图表行样式 */
.chart-row {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

/* 图表卡片样式 */
.chart-card {
  flex: 1;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(60, 120, 180, 0.10);
  padding: 24px;
  transition: transform 0.2s, box-shadow 0.2s;
}
.chart-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 28px rgba(60, 120, 180, 0.12);
}
.chart-card h3 {
  color: #2a4d69;
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.4rem;
  font-weight: 700;
  text-align: center;
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

/* 表格样式 */
.table-section {
  margin: 24px 0;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(60,120,180,0.08);
  padding: 32px 24px;
  transition: transform 0.2s, box-shadow 0.2s;
}
.table-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 28px rgba(60,120,180,0.12);
}
.table-section h3 {
  margin-bottom: 20px;
  color: #2a4d69;
  font-size: 1.4rem;
  font-weight: 700;
  text-align: center;
  letter-spacing: 1px;
}
.report-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 0;
  font-size: 1.05rem;
  border-radius: 8px;
  overflow: hidden;
}
.report-table th, .report-table td {
  border: 1px solid #e8f2ff;
  padding: 12px 16px;
  text-align: center;
  transition: background-color 0.2s;
}
.report-table th {
  background: linear-gradient(90deg, #f0f7ff 0%, #e8f2ff 100%);
  color: #2a4d69;
  font-weight: 700;
  font-size: 1.1rem;
}
.report-table tbody tr:hover {
  background-color: #f8fbff;
}
.report-table tbody tr:nth-child(even) {
  background-color: #fafcff;
}

@media (max-width: 1200px) {
  .main-content {
    padding: 0 16px 24px 16px;
  }
  .header {
    padding: 28px 16px 12px 16px;
  }
  .chart-row {
    flex-direction: column;
  }
  .table-section {
    padding: 24px 16px;
  }
}

@media (max-width: 900px) {
  .chart-card {
    padding: 16px;
  }
  .table-section {
    margin: 16px 0;
    padding: 20px 12px;
  }
  .report-table {
    font-size: 0.95rem;
  }
  .report-table th, .report-table td {
    padding: 8px 6px;
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
