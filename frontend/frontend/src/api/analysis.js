import axios from 'axios'

// 获取任务分析数据
export function getTaskAnalysis(taskIds) {
  return axios.post('/api/analysis/tasks', taskIds)
}