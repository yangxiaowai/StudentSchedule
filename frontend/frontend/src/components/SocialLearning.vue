<template>
  <div class="social-learning">
    <div class="page-header">
      <h1>社交学习</h1>
      <p>与同学一起学习，分享知识，共同进步</p>
    </div>

    <!-- 功能导航 -->
    <div class="feature-nav">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="学习小组" name="groups">
          <template #label>
            <span class="tab-label">
              <el-icon><UserFilled /></el-icon>
              学习小组
            </span>
          </template>
        </el-tab-pane>
        <!-- 学习分享标签页已隐藏 -->
        <el-tab-pane label="互助答疑" name="qa">
          <template #label>
            <span class="tab-label">
              <el-icon><QuestionFilled /></el-icon>
              互助答疑
            </span>
          </template>
        </el-tab-pane>
        <!-- 移除学习排行榜标签页 -->
      </el-tabs>
    </div>

    <!-- 功能概览卡片 -->
    <div v-if="activeTab === 'overview'" class="overview-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="feature-card" @click="activeTab = 'groups'">
            <div class="feature-content">
              <div class="feature-icon groups">
                <el-icon><UserFilled /></el-icon>
              </div>
              <div class="feature-info">
                <h3>学习小组</h3>
                <p>创建或加入学习小组，与同学共同完成学习目标</p>
                <div class="feature-stats">
                  <span>{{ groupStats.joined }} 个小组</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <!-- 学习分享功能卡片已隐藏 -->
        <el-col :span="6">
          <el-card shadow="hover" class="feature-card" @click="activeTab = 'qa'">
            <div class="feature-content">
              <div class="feature-icon qa">
                <el-icon><QuestionFilled /></el-icon>
              </div>
              <div class="feature-info">
                <h3>互助答疑</h3>
                <p>提出问题，回答疑问，与同学互相帮助</p>
                <div class="feature-stats">
                  <span>{{ qaStats.questions }} 个问题</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <!-- 移除学习排行榜功能卡片 -->
      </el-row>
    </div>



    <!-- 功能组件 -->
    <div class="feature-content">
      <!-- 学习小组 -->
      <div v-if="activeTab === 'groups'" class="tab-content">
        <StudyGroups />
      </div>
      
      <!-- 学习分享组件已隐藏 -->
      
      <!-- 互助答疑 -->
      <div v-if="activeTab === 'qa'" class="tab-content">
        <QAForum />
      </div>
      
      <!-- 移除学习排行榜组件 -->
    </div>

    <!-- 侧边栏快捷操作 -->
    <div class="sidebar-actions">
      <el-affix :offset="80">
        <div class="action-buttons">
          <el-tooltip content="创建小组" placement="left">
            <el-button 
              type="primary" 
              circle 
              size="large"
              @click="quickAction('createGroup')"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
          </el-tooltip>
          
          <!-- 发布分享按钮已隐藏 -->
          
          <el-tooltip content="提问" placement="left">
            <el-button 
              type="warning" 
              circle 
              size="large"
              @click="quickAction('askQuestion')"
            >
              <el-icon><QuestionFilled /></el-icon>
            </el-button>
          </el-tooltip>
          
          <!-- 移除查看排行按钮 -->
        </div>
      </el-affix>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activities" v-if="activeTab === 'overview'">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>最近活动</span>
            <el-button text @click="loadRecentActivities">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>
        
        <div class="activities-list">
          <div 
            v-for="activity in recentActivities" 
            :key="activity.id" 
            class="activity-item"
          >
            <div class="activity-icon">
              <el-icon v-if="activity.type === 'group'" class="group-icon"><UserFilled /></el-icon>
              <el-icon v-else-if="activity.type === 'share'" class="share-icon"><Share /></el-icon>
              <el-icon v-else-if="activity.type === 'qa'" class="qa-icon"><QuestionFilled /></el-icon>
              <el-icon v-else class="default-icon"><Bell /></el-icon>
            </div>
            <div class="activity-content">
              <div class="activity-text">{{ activity.text }}</div>
              <div class="activity-time">{{ formatTime(activity.time) }}</div>
            </div>
          </div>
        </div>
        
        <div v-if="recentActivities.length === 0" class="empty-activities">
          <el-empty description="暂无最近活动" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  UserFilled, Share, QuestionFilled, Plus, Edit, Refresh, Bell 
} from '@element-plus/icons-vue'
import StudyGroups from './StudyGroups.vue'
import StudyShares from './StudyShares.vue'
import QAForum from './QAForum.vue'
// 移除Leaderboard组件导入
import axios from 'axios'

export default {
  name: 'SocialLearning',
  components: {
    UserFilled,
    Share,
    QuestionFilled,
    Plus,
    Edit,
    Refresh,
    Bell,
    StudyGroups,
    StudyShares,
    QAForum
  },
  setup() {
    const activeTab = ref('groups')
    const currentUserId = ref(parseInt(localStorage.getItem('userId')) || 51) // 从localStorage获取用户ID
    const recentActivities = ref([])
    
    // 统计数据
    const groupStats = reactive({
      joined: 0,
      created: 0
    })
    
    const shareStats = reactive({
      count: 0,
      likes: 0
    })
    
    const qaStats = reactive({
      questions: 0,
      answers: 0
    })


    const loadStats = async () => {
      try {
        // 获取用户统计数据
        const token = localStorage.getItem('token')
        const response = await axios.get('https://localhost:8443/api/users/stats', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        const stats = response.data
        
        // 更新统计数据
        groupStats.joined = stats.groupsJoined || 0
        groupStats.created = stats.groupsCreated || 0
        shareStats.count = stats.sharesCount || 0
        shareStats.likes = stats.likesReceived || 0
        qaStats.questions = stats.questionsAsked || 0
        qaStats.answers = stats.bestAnswersCount || 0
      } catch (error) {
        console.error('加载统计数据失败', error)
        // 如果API调用失败，使用默认值
        groupStats.joined = 0
        groupStats.created = 0
        shareStats.count = 0
        shareStats.likes = 0
        qaStats.questions = 0
        qaStats.answers = 0
      }
    }

    // 加载最近活动
    const loadRecentActivities = () => {
      // 模拟最近活动数据
      recentActivities.value = [
        {
          id: 1,
          type: 'group',
          text: '你加入了"高考数学冲刺"学习小组',
          time: new Date(Date.now() - 2 * 60 * 60 * 1000) // 2小时前
        },
        {
          id: 2,
          type: 'share',
          text: '你发布了学习分享"数学解题技巧总结"',
          time: new Date(Date.now() - 5 * 60 * 60 * 1000) // 5小时前
        },
        {
          id: 3,
          type: 'qa',
          text: '你回答了问题"如何快速记忆英语单词？"',
          time: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000) // 1天前
        },
        {
          id: 4,
          type: 'group',
          text: '"物理实验讨论"小组有新的分享',
          time: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000) // 2天前
        },
        {
          id: 5,
          type: 'qa',
          text: '你的回答被设为最佳答案',
          time: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000) // 3天前
        }
      ]
    }

    // 标签页切换
    const handleTabChange = (tabName) => {
      activeTab.value = tabName
    }

    // 快捷操作
    const quickAction = (action) => {
      switch (action) {
        case 'createGroup':
          activeTab.value = 'groups'
          // 这里可以触发创建小组的对话框
          ElMessage.info('切换到学习小组页面')
          break
        case 'createShare':
          activeTab.value = 'shares'
          // 这里可以触发创建分享的对话框
          ElMessage.info('切换到学习分享页面')
          break
        case 'askQuestion':
          activeTab.value = 'qa'
          // 这里可以触发提问的对话框
          ElMessage.info('切换到互助答疑页面')
          break
        // 移除查看排行榜的快捷操作
      }
    }

    // 格式化时间
    const formatTime = (time) => {
      const now = new Date()
      const diff = now - time
      const minutes = Math.floor(diff / (1000 * 60))
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      
      if (minutes < 60) {
        return `${minutes}分钟前`
      } else if (hours < 24) {
        return `${hours}小时前`
      } else {
        return `${days}天前`
      }
    }

    onMounted(() => {
      loadStats()
      loadRecentActivities()
    })

    return {
      activeTab,
      currentUserId,
      recentActivities,
      groupStats,
      shareStats,
      qaStats,
      handleTabChange,
      quickAction,
      loadRecentActivities,
      formatTime
    }
  }
}
</script>

<style scoped>
.social-learning {
  padding: 24px;
  position: relative;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
}

.page-header h1 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-header p {
  font-size: 18px;
  opacity: 0.9;
  font-weight: 300;
}

.feature-nav {
  margin-bottom: 30px;
  background: white;
  border-radius: 12px;
  padding: 5px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
  padding: 12px 8px;
  transition: all 0.3s ease;
}

.overview-section {
  margin-bottom: 40px;
}

.feature-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 180px;
  border-radius: 16px;
  overflow: hidden;
  border: none;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.feature-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 20px;
  background: linear-gradient(45deg, rgba(255,255,255,0.9) 0%, rgba(255,255,255,0.7) 100%);
  backdrop-filter: blur(5px);
}

.feature-icon {
  width: 70px;
  height: 70px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 28px;
  color: white;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.feature-icon.groups {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.feature-icon.shares {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.feature-icon.qa {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}



.feature-info {
  flex: 1;
}

.feature-info h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #2d3748;
  font-weight: 600;
}

.feature-info p {
  font-size: 15px;
  color: #718096;
  margin-bottom: 12px;
  line-height: 1.6;
}

.feature-stats {
  font-size: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 600;
}



.tab-content {
  margin-top: 20px;
}

.sidebar-actions {
  position: fixed;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1000;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.action-buttons .el-button {
  width: 50px;
  height: 50px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.recent-activities {
  margin-top: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activities-list {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 18px;
}

.group-icon {
  background: #409eff20;
  color: #409eff;
}

.share-icon {
  background: #67c23a20;
  color: #67c23a;
}

.qa-icon {
  background: #e6a23c20;
  color: #e6a23c;
}

.default-icon {
  background: #f0f0f0;
  color: #666;
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #999;
}

.empty-activities {
  text-align: center;
  padding: 40px;
}

/* 动画效果 */
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.page-header {
  animation: slideInUp 0.8s ease-out;
}

.feature-nav {
  animation: fadeIn 1s ease-out 0.2s both;
}

.feature-card {
  animation: slideInUp 0.6s ease-out;
}

.feature-card:nth-child(1) { animation-delay: 0.1s; }
.feature-card:nth-child(2) { animation-delay: 0.2s; }
.feature-card:nth-child(3) { animation-delay: 0.3s; }
.feature-card:nth-child(4) { animation-delay: 0.4s; }

/* 响应式设计 */
@media (max-width: 1200px) {
  .overview-section .el-col {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .social-learning {
    padding: 16px;
  }
  
  .page-header {
    padding: 30px 16px;
    margin-bottom: 30px;
  }
  
  .page-header h1 {
    font-size: 28px;
  }
  
  .page-header p {
    font-size: 16px;
  }
  
  .sidebar-actions {
    display: none;
  }
  
  .feature-card {
    height: auto;
    margin-bottom: 20px;
  }
  
  .feature-content {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }
  
  .feature-icon {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .feature-nav {
    margin-bottom: 20px;
  }
  
  .tab-label {
    font-size: 14px;
    padding: 8px 4px;
  }
}

@media (max-width: 480px) {
  .page-header h1 {
    font-size: 24px;
  }
  
  .feature-content {
    padding: 20px;
  }
  
  .feature-icon {
    width: 60px;
    height: 60px;
    font-size: 24px;
  }
  
  .feature-info h3 {
    font-size: 18px;
  }
  
  .feature-info p {
    font-size: 14px;
  }
}
</style>