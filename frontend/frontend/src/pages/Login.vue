<template>
  <div class="login-container">
    <!-- 背景装饰元素 -->
    <div class="background-decoration">
      <div class="floating-element element-1"></div>
      <div class="floating-element element-2"></div>
      <div class="floating-element element-3"></div>
      <div class="floating-element element-4"></div>
    </div>
    
    <!-- 左侧学习元素 -->
    <div class="left-sidebar">
      <div class="learning-stats">
        <div class="stat-item">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm3.5 6L12 10.5 8.5 8 12 5.5 15.5 8zM12 13.5l3.5 2.5-3.5 2.5L8.5 16l3.5-2.5z" fill="#9C27B0"/>
            </svg>
          </div>
          <div class="stat-content">
            <h3>128</h3>
            <p>已刷题目</p>
          </div>
        </div>
        
        <div class="stat-item">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24">
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z" fill="#FF5722"/>
            </svg>
          </div>
          <div class="stat-content">
            <h3>45</h3>
            <p>错题整理</p>
          </div>
        </div>
        
        <div class="stat-item">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24">
              <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm-1 16H9V7h9v14z" fill="#2196F3"/>
            </svg>
          </div>
          <div class="stat-content">
            <h3>92%</h3>
            <p>目标达成率</p>
          </div>
        </div>
      </div>
      
      <div class="motivational-quote">
        <div class="quote-header">
          <svg viewBox="0 0 24 24">
            <path d="M9.5 3A6.5 6.5 0 0 1 16 9.5c0 1.61-.59 3.09-1.56 4.23l.27.27h.79l5 5-1.5 1.5-5-5v-.79l-.27-.27A6.516 6.516 0 0 1 9.5 16 6.5 6.5 0 0 1 3 9.5 6.5 6.5 0 0 1 9.5 3m0 2C7 5 5 7 5 9.5S7 14 9.5 14 14 12 14 9.5 12 5 9.5 5z" fill="#667eea"/>
          </svg>
          <h4>每日格言</h4>
        </div>
        <p>"宝剑锋从磨砺出，梅花香自苦寒来"</p>
        <span>- 古训</span>
      </div>
    </div>
    
    <div class="login-card">
      <!-- 头部区域 -->
      <div class="card-header">
        <div class="logo-section">
          <div class="logo-icon">
            <svg viewBox="0 0 64 64" class="logo-svg">
              <!-- 书本图标 -->
              <path d="M8 12h48v40H8z" fill="#667eea" opacity="0.2"/>
              <path d="M12 8h40v48H12z" fill="#667eea"/>
              <path d="M16 16h32v4H16z" fill="white"/>
              <path d="M16 24h24v2H16z" fill="white"/>
              <path d="M16 28h28v2H16z" fill="white"/>
              <path d="M16 32h20v2H16z" fill="white"/>
              <!-- 进度条 -->
              <rect x="16" y="40" width="32" height="4" rx="2" fill="white" opacity="0.3"/>
              <rect x="16" y="40" width="20" height="4" rx="2" fill="#4CAF50"/>
              <!-- 星星 -->
              <path d="M50 20l2 4h4l-3 3 1 4-4-2-4 2 1-4-3-3h4z" fill="#FFD700"/>
            </svg>
          </div>
          <div class="logo-text">
            <h1>高考冲刺学习计划</h1>
            <p>科学规划时间，冲刺理想大学</p>
          </div>
        </div>
      </div>
      
      <!-- 登录表单区域 -->
      <div class="login-form">
        <div class="form-title">
          <h2>欢迎回来</h2>
          <p>继续您的学习之旅</p>
        </div>
        
        <!-- 错误提示 -->
        <div v-if="userStore.error" class="error-message">
          <svg class="error-icon" viewBox="0 0 24 24">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" fill="#f56565"/>
          </svg>
          {{ userStore.error }}
        </div>
        
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">
              <svg class="input-icon" viewBox="0 0 24 24">
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" fill="currentColor"/>
              </svg>
              用户名
            </label>
            <input
              type="text"
              id="username"
              v-model="formData.username"
              :disabled="userStore.loading"
              placeholder="请输入您的用户名"
              required
            />
          </div>
          
          <div class="form-group">
            <label for="password">
              <svg class="input-icon" viewBox="0 0 24 24">
                <path d="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8z M12,17c-1.1,0-2-0.9-2-2s0.9-2,2-2s2,0.9,2,2S13.1,17,12,17z M15.1,8H8.9V6c0-1.71,1.39-3.1,3.1-3.1s3.1,1.39,3.1,3.1V8z" fill="currentColor"/>
              </svg>
              密码
            </label>
            <input
              type="password"
              id="password"
              v-model="formData.password"
              :disabled="userStore.loading"
              placeholder="请输入您的密码"
              required
            />
          </div>
          
          <button 
            type="submit" 
            class="login-btn"
            :disabled="userStore.loading"
          >
            <svg v-if="userStore.loading" class="loading-icon" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none" stroke-dasharray="31.416" stroke-dashoffset="31.416">
                <animate attributeName="stroke-dasharray" dur="2s" values="0 31.416;15.708 15.708;0 31.416" repeatCount="indefinite"/>
                <animate attributeName="stroke-dashoffset" dur="2s" values="0;-15.708;-31.416" repeatCount="indefinite"/>
              </circle>
            </svg>
            <svg v-else class="login-icon" viewBox="0 0 24 24">
              <path d="M10,17l5-5l-5-5v3H3v4h7V17z M21,3h-8v2h8v14h-8v2h8c1.1,0,2-0.9,2-2V5C23,3.9,22.1,3,21,3z" fill="currentColor"/>
            </svg>
            {{ userStore.loading ? '登录中...' : '开始学习' }}
          </button>
        </form>
        
        <!-- 学习激励语句 -->
        <div class="motivation-quote">
          <svg class="quote-icon" viewBox="0 0 24 24">
            <path d="M6 17h3l2-4V7H5v6h3zm8 0h3l2-4V7h-6v6h3z" fill="currentColor"/>
          </svg>
          <p>每一次努力，都是向理想大学迈进的一步</p>
          <svg class="quote-icon" viewBox="0 0 24 24">
            <path d="M6 17h3l2-4V7H5v6h3zm8 0h3l2-4V7h-6v6h3z" fill="currentColor"/>
          </svg>
        </div>
        
        <div class="form-links">
          <router-link to="/register" class="link">
            <svg class="link-icon" viewBox="0 0 24 24">
              <path d="M15,14c-2.67,0-8,1.34-8,4v2h16v-2C23,15.34,17.67,14,15,14z M6,10V7H4v3H1v2h3v3h2v-3h3v-2H6z M15,12c2.21,0,4-1.79,4-4 s-1.79-4-4-4s-4,1.79-4,4S12.79,12,15,12z" fill="currentColor"/>
            </svg>
            还没有账号？立即注册
          </router-link>
          <router-link to="/forgot-password" class="link">
            <svg class="link-icon" viewBox="0 0 24 24">
              <path d="M12,2C13.1,2 14,2.9 14,4C14,5.1 13.1,6 12,6C10.9,6 10,5.1 10,4C10,2.9 10.9,2 12,2M21,9V7L15,1H5C3.89,1 3,1.89 3,3V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V9M19,9H14V4H5V19H19V9Z" fill="currentColor"/>
            </svg>
            忘记密码？
          </router-link>
        </div>
      </div>
      
      <!-- 底部装饰 -->
      <div class="card-footer">
        <div class="progress-indicator">
          <div class="progress-step active">
            <div class="step-icon">📚</div>
            <span>开始学习</span>
          </div>
          <div class="progress-line"></div>
          <div class="progress-step">
            <div class="step-icon">🎯</div>
            <span>制定目标</span>
          </div>
          <div class="progress-line"></div>
          <div class="progress-step">
            <div class="step-icon">🏆</div>
            <span>达成成就</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 右侧学习元素 -->
    <div class="right-sidebar">
      <div class="learning-schedule">
        <div class="schedule-header">
          <svg viewBox="0 0 24 24">
            <path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z" fill="#673AB7"/>
          </svg>
          <h4>今日学习计划</h4>
        </div>
        
        <div class="schedule-item">
          <div class="time-slot">
            <span class="time">08:00</span>
            <div class="status-dot completed"></div>
          </div>
          <p>数学函数专题复习</p>
        </div>
        
        <div class="schedule-item">
          <div class="time-slot">
            <span class="time">10:00</span>
            <div class="status-dot in-progress"></div>
          </div>
          <p>英语阅读理解训练</p>
        </div>
        
        <div class="schedule-item">
          <div class="time-slot">
            <span class="time">14:30</span>
            <div class="status-dot pending"></div>
          </div>
          <p>物理力学综合练习</p>
        </div>
      </div>
      
      <div class="learning-resources">
        <div class="resources-header">
          <svg viewBox="0 0 24 24">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" fill="#E91E63"/>
          </svg>
          <h4>推荐资源</h4>
        </div>
        
        <a href="https://www.xueersi.com/" target="_blank" class="resource-item resource-link">
          <div class="resource-icon">
            📚
          </div>
          <div class="resource-content">
            <h5>高考数学专题突破</h5>
            <p>函数与导数重点解析</p>
          </div>
          <div class="link-indicator">
            <svg viewBox="0 0 24 24">
              <path d="M14,3V5H17.59L7.76,14.83L9.17,16.24L19,6.41V10H21V3M19,19H5V5H12V3H5C3.89,3 3,3.9 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V12H19V19Z" fill="currentColor"/>
            </svg>
          </div>
        </a>
        
        <a href="https://www.koolearn.com/" target="_blank" class="resource-item resource-link">
          <div class="resource-icon">
            🎯
          </div>
          <div class="resource-content">
            <h5>英语高频词汇速记</h5>
            <p>3500词汇高效记忆法</p>
          </div>
          <div class="link-indicator">
            <svg viewBox="0 0 24 24">
              <path d="M14,3V5H17.59L7.76,14.83L9.17,16.24L19,6.41V10H21V3M19,19H5V5H12V3H5C3.89,3 3,3.9 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V12H19V19Z" fill="currentColor"/>
            </svg>
          </div>
        </a>
        
        <a href="https://www.gaokao.com/" target="_blank" class="resource-item resource-link">
          <div class="resource-icon">
            📝
          </div>
          <div class="resource-content">
            <h5>历年真题解析</h5>
            <p>近五年高考试题精讲</p>
          </div>
          <div class="link-indicator">
            <svg viewBox="0 0 24 24">
              <path d="M14,3V5H17.59L7.76,14.83L9.17,16.24L19,6.41V10H21V3M19,19H5V5H12V3H5C3.89,3 3,3.9 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V12H19V19Z" fill="currentColor"/>
            </svg>
          </div>
        </a>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive } from 'vue'
import { userStore } from '../store/user.js'
import { useRouter } from 'vue-router'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    
    const formData = reactive({
      username: '',
      password: ''
    })
    
    const handleLogin = async () => {
      // 清除之前的错误
      userStore.clearError()
      
      // 表单验证
      if (!formData.username.trim()) {
        userStore.error = '请输入用户名'
        return
      }
      
      if (!formData.password.trim()) {
        userStore.error = '请输入密码'
        return
      }
      
      console.log('开始登录，用户名:', formData.username)
      
      // 调用登录API
      const result = await userStore.login({
        username: formData.username.trim(),
        password: formData.password
      })
      
      console.log('登录结果:', result)
      
      if (result.success) {
        console.log('登录成功，准备跳转到主页')
        
        // 使用 router.push 进行跳转，而不是强制刷新
        router.push('/data-integration')

      } else {
        console.log('登录失败:', result.message)
      }
      // 错误信息已经在store中设置，会自动显示
    }
    
    return {
      formData,
      userStore,
      handleLogin
    }
  }
}
</script>

<style scoped>
/* 全局容器 */
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
  gap: 2rem;
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.floating-element {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.element-1 {
  width: 80px;
  height: 80px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.element-2 {
  width: 120px;
  height: 120px;
  top: 20%;
  right: 15%;
  animation-delay: 2s;
}

.element-3 {
  width: 60px;
  height: 60px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

.element-4 {
  width: 100px;
  height: 100px;
  bottom: 10%;
  right: 10%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.7;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 1;
  }
}

/* 左侧边栏 */
.left-sidebar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  width: 180px;
  padding: 1.2rem;
  position: relative;
  z-index: 2;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  animation: slideInLeft 0.6s ease-out;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.learning-stats {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
}

.stat-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.stat-content h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2d3748;
}

.stat-content p {
  margin: 0;
  font-size: 0.875rem;
  color: #718096;
  font-weight: 500;
}

.motivational-quote {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.5rem;
  border-radius: 15px;
  text-align: center;
}

.quote-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.quote-header svg {
  width: 20px;
  height: 20px;
  fill: white;
}

.quote-header h4 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.motivational-quote p {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-style: italic;
  line-height: 1.4;
}

.motivational-quote span {
  font-size: 0.875rem;
  opacity: 0.8;
}

/* 右侧边栏 */
.right-sidebar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  width: 280px;
  padding: 1.5rem;
  position: relative;
  z-index: 2;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  animation: slideInRight 0.6s ease-out;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.learning-schedule {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.schedule-header,
.resources-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #e2e8f0;
}

.schedule-header svg,
.resources-header svg {
  width: 20px;
  height: 20px;
}

.schedule-header h4,
.resources-header h4 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #2d3748;
}

.schedule-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.schedule-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(103, 58, 183, 0.15);
}

.time-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  min-width: 60px;
}

.time {
  font-size: 0.75rem;
  font-weight: 600;
  color: #673AB7;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.status-dot.completed {
  background: #4CAF50;
}

.status-dot.in-progress {
  background: #FF9800;
}

.status-dot.pending {
  background: #9E9E9E;
}

.schedule-item p {
  margin: 0;
  font-size: 0.875rem;
  color: #2d3748;
  font-weight: 500;
}

.learning-resources {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}



.resource-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
  font-size: 1.2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.resource-content h5 {
  margin: 0 0 0.25rem 0;
  font-size: 0.875rem;
  font-weight: 600;
  color: #be185d;
}

.resource-content p {
  margin: 0;
  font-size: 0.75rem;
  color: #ec4899;
  opacity: 0.8;
}

.resource-link {
  text-decoration: none;
  color: inherit;
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: linear-gradient(135deg, #fdf2f8 0%, #fce7f3 100%);
  border-radius: 12px;
  border: 1px solid #f9a8d4;
  transition: all 0.3s ease;
  position: relative;
}

.resource-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(233, 30, 99, 0.15);
  border-color: #ec4899;
}

.resource-link:hover .link-indicator {
  opacity: 1;
  transform: translateX(0);
}

.resource-link:hover .resource-content h5 {
  color: #be185d;
}

.link-indicator {
  width: 20px;
  height: 20px;
  color: #be185d;
  opacity: 0.6;
  transform: translateX(-5px);
  transition: all 0.3s ease;
  margin-left: auto;
}

.link-indicator svg {
  width: 100%;
  height: 100%;
}

/* 主卡片 */
.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 480px;
  position: relative;
  z-index: 2;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 卡片头部 */
.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
  text-align: center;
  color: white;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.logo-svg {
  width: 60px;
  height: 60px;
}

.logo-text h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.logo-text p {
  margin: 0.5rem 0 0 0;
  font-size: 0.9rem;
  opacity: 0.9;
  font-weight: 300;
}

/* 登录表单 */
.login-form {
  padding: 2rem;
}

.form-title {
  text-align: center;
  margin-bottom: 2rem;
}

.form-title h2 {
  margin: 0;
  color: #2d3748;
  font-size: 1.8rem;
  font-weight: 700;
}

.form-title p {
  margin: 0.5rem 0 0 0;
  color: #718096;
  font-size: 1rem;
}

/* 错误提示 */
.error-message {
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
  color: #c53030;
  padding: 1rem;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  border: 1px solid #fca5a5;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  animation: slideIn 0.3s ease-out;
}

.error-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 表单组 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  color: #4a5568;
  font-weight: 600;
  font-size: 0.9rem;
}

.input-icon {
  width: 18px;
  height: 18px;
  color: #667eea;
}

.form-group input {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: #f8fafc;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.form-group input:disabled {
  background-color: #f1f5f9;
  cursor: not-allowed;
  opacity: 0.6;
}

.form-group input::placeholder {
  color: #a0aec0;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 1rem;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.login-btn:hover:not(:disabled)::before {
  left: 100%;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.login-icon,
.loading-icon {
  width: 20px;
  height: 20px;
}

/* 激励语句 */
.motivation-quote {
  background: linear-gradient(135deg, #f0f4ff 0%, #e0e7ff 100%);
  border: 1px solid #c7d2fe;
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  text-align: center;
}

.quote-icon {
  width: 24px;
  height: 24px;
  color: #667eea;
  flex-shrink: 0;
}

.motivation-quote p {
  margin: 0;
  color: #4c51bf;
  font-style: italic;
  font-size: 0.9rem;
  line-height: 1.4;
  text-align: center;
}

/* 表单链接 */
.form-links {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 1rem;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem;
  border-radius: 8px;
  background: rgba(102, 126, 234, 0.05);
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.link:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.link-icon {
  width: 16px;
  height: 16px;
}

/* 底部装饰 */
.card-footer {
  background: #f8fafc;
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
}

.progress-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  opacity: 0.5;
  transition: all 0.3s ease;
}

.progress-step.active {
  opacity: 1;
  transform: scale(1.1);
}

.step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.progress-step span {
  font-size: 0.75rem;
  color: #4a5568;
  font-weight: 500;
  text-align: center;
}

.progress-line {
  width: 30px;
  height: 2px;
  background: #e2e8f0;
  border-radius: 1px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .left-sidebar,
  .right-sidebar {
    display: none;
  }
  
  .login-container {
    justify-content: center;
  }
}

@media (max-width: 640px) {
  .login-container {
    padding: 10px;
  }
  
  .login-card {
    max-width: 100%;
  }
  
  .card-header {
    padding: 1.5rem;
  }
  
  .login-form {
    padding: 1.5rem;
  }
  
  .logo-text h1 {
    font-size: 1.3rem;
  }
  
  .form-title h2 {
    font-size: 1.5rem;
  }
  
  .progress-indicator {
    flex-direction: column;
    gap: 1rem;
  }
  
  .progress-line {
    width: 2px;
    height: 20px;
  }
  
  .floating-element {
    display: none;
  }
}

@media (max-width: 480px) {
  .card-header {
    padding: 1rem;
  }
  
  .login-form {
    padding: 1rem;
  }
  
  .logo-icon {
    width: 60px;
    height: 60px;
  }
  
  .logo-svg {
    width: 40px;
    height: 40px;
  }
}
</style>