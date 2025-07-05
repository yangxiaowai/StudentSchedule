<template>
  <div class="container">
    <h2 class="title">学习历史记录</h2>

    <div v-if="records.length === 0" class="empty">
      暂无学习记录
    </div>

    <div v-else class="record-list">
      <div v-for="(record, index) in records" :key="index" class="record-card">
        <div><strong>文件名称：</strong>{{ record.filename }}</div>
        <div><strong>学习时间：</strong>{{ record.time }}</div>
        <div><strong>学科分类：</strong>{{ record.subject }}</div>
        <div><strong>备注：</strong>{{ record.note }}</div>
        <div class="progress-container">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: record.progress + '%' }"></div>
          </div>
          <div class="progress-info">
            {{ record.progress }}%
            <span v-if="record.progress === 100" class="completed">✅ 已完成</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const records = ref([
  {
    filename: '第1章 线性代数复习讲义.pdf',
    time: '2025-06-20 14:30',
    subject: '数学',
    note: '重点复习了矩阵的秩与线性方程组求解。',
    progress: 100
  },
  {
    filename: '历史试题卷2024.docx',
    time: '2025-06-18 09:00',
    subject: '历史',
    note: '做了2024年全国卷，掌握了近现代史核心考点。',
    progress: 70
  },
  {
    filename: '英语听力练习05.mp3',
    time: '2025-06-16 21:00',
    subject: '英语',
    note: '听力速度略快，听写正确率80%，下次需再练习。',
    progress: 45
  }
])
</script>

<style scoped>
.container {
  max-width: 700px;
  margin: 30px auto;
  padding: 16px;
  font-family: Arial, sans-serif;
}

.title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 20px;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-card {
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 14px;
  background-color: #f9f9f9;
  box-shadow: 1px 1px 5px rgba(0, 0, 0, 0.05);
}

.empty {
  text-align: center;
  color: #999;
  margin-top: 40px;
}

.progress-container {
  margin-top: 10px;
}

.progress-bar {
  width: 100%;
  height: 12px;
  background-color: #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #4caf50;
  transition: width 0.3s ease;
}

.progress-info {
  margin-top: 4px;
  font-size: 14px;
  color: #333;
}

.completed {
  margin-left: 10px;
  color: green;
  font-weight: bold;
}
</style>
