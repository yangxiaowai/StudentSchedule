<template>
  <div class="register-container">
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
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" fill="none"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" fill="none"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" fill="none"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">0</div>
            <div class="stat-label">待开始课程</div>
          </div>
        </div>
        
        <div class="stat-item">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24">
              <path d="M14 2H6C4.9 2 4 2.9 4 4V20C4 21.1 4.9 22 6 22H18C19.1 22 20 21.1 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" fill="none"/>
              <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2" fill="none"/>
              <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2"/>
              <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2"/>
              <polyline points="10,9 9,9 8,9" stroke="currentColor" stroke-width="2" fill="none"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">0</div>
            <div class="stat-label">学习笔记</div>
          </div>
        </div>
        
        <div class="stat-item">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24">
              <polygon points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26" stroke="currentColor" stroke-width="2" fill="none"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">0%</div>
            <div class="stat-label">知识掌握度</div>
          </div>
        </div>
      </div>
      
      <div class="daily-quote">
        <div class="quote-icon">
          <svg viewBox="0 0 24 24">
            <path d="M3 21C3 17.69 5.69 15 9 15S15 17.69 15 21V22H3V21Z" stroke="currentColor" stroke-width="2" fill="none"/>
            <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2" fill="none"/>
            <path d="M16 3.13C16.84 3.5 17.56 4.07 18.1 4.8" stroke="currentColor" stroke-width="2" fill="none"/>
            <path d="M21 21V20C21 18.13 20.15 16.47 18.85 15.43" stroke="currentColor" stroke-width="2" fill="none"/>
            <path d="M16 21V20C16 18.9 15.1 18 14 18H10" stroke="currentColor" stroke-width="2" fill="none"/>
          </svg>
        </div>
        <div class="quote-content">
          <p class="quote-text">"学而时习之，不亦说乎？"</p>
          <p class="quote-author">— 孔子</p>
        </div>
      </div>
    </div>
    
    <!-- 中央注册卡片 -->
    <div class="register-form">
      <h2>用户注册</h2>
      
      <!-- 错误提示 -->
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
      
      <!-- 成功提示 -->
      <div v-if="success" class="success-message">
        {{ success }}
      </div>
      
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">用户名:</label>
          <input
            type="text"
            id="username"
            v-model="formData.username"
            :disabled="loading"
            placeholder="请输入用户名"
            required
          />
          <small class="form-hint">用户名长度为3-20个字符</small>
        </div>
        
        <div class="form-group">
          <label for="email">邮箱:</label>
          <input
            type="email"
            id="email"
            v-model="formData.email"
            :disabled="loading"
            placeholder="请输入邮箱地址"
            required
          />
          <small class="form-hint">用于找回密码和接收通知</small>
        </div>
        
        <div class="form-group">
          <label for="password">密码:</label>
          <input
            type="password"
            id="password"
            v-model="formData.password"
            :disabled="loading"
            placeholder="请输入密码"
            required
          />
          <small class="form-hint">密码长度至少6个字符</small>
        </div>
        
        <div class="form-group">
          <label for="confirmPassword">确认密码:</label>
          <input
            type="password"
            id="confirmPassword"
            v-model="formData.confirmPassword"
            :disabled="loading"
            placeholder="请再次输入密码"
            required
          />
        </div>
        
        <button 
          type="submit" 
          class="register-btn"
          :disabled="loading"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      
      <div class="form-links">
        <router-link to="/login" class="link">已有账号？立即登录</router-link>
      </div>
    </div>
    
    <!-- 右侧注册指南 -->
    <div class="right-sidebar">
      <div class="registration-guide">
        <div class="guide-header">
          <div class="guide-icon">
            <svg viewBox="0 0 24 24">
              <path d="M9 11H15M9 15H15M17 21L12 16L7 21V5C7 3.9 7.9 3 9 3H15C16.1 3 17 3.9 17 5V21Z" stroke="currentColor" stroke-width="2" fill="none"/>
            </svg>
          </div>
          <h3>注册指南</h3>
        </div>
        
        <div class="guide-item">
          <div class="guide-step">1</div>
          <div class="guide-content">
            <h4>填写基本信息</h4>
            <p>输入用户名、邮箱和密码</p>
          </div>
        </div>
        
        <div class="guide-item">
          <div class="guide-step">2</div>
          <div class="guide-content">
            <h4>验证邮箱</h4>
            <p>确保邮箱地址正确有效</p>
          </div>
        </div>
        
        <div class="guide-item">
          <div class="guide-step">3</div>
          <div class="guide-content">
            <h4>开始学习</h4>
            <p>创建您的第一个学习计划</p>
          </div>
        </div>
      </div>
      
      <div class="learning-benefits">
        <div class="benefits-header">
          <div class="benefits-icon">
            <svg viewBox="0 0 24 24">
              <path d="M22 12H18L15 21L9 3L6 12H2" stroke="currentColor" stroke-width="2" fill="none"/>
            </svg>
          </div>
          <h3>学习优势</h3>
        </div>
        
        <div class="benefit-item">
          <div class="benefit-icon">
            <svg viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none"/>
              <polyline points="12,6 12,12 16,14" stroke="currentColor" stroke-width="2" fill="none"/>
            </svg>
          </div>
          <div class="benefit-content">
            <h4>智能时间管理</h4>
            <p>AI助手帮您优化学习时间</p>
          </div>
        </div>
        
        <div class="benefit-item">
          <div class="benefit-icon">
            <svg viewBox="0 0 24 24">
              <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" fill="none"/>
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none"/>
            </svg>
          </div>
          <div class="benefit-content">
            <h4>进度跟踪</h4>
            <p>实时监控学习进度和成果</p>
          </div>
        </div>
        
        <div class="benefit-item">
          <div class="benefit-icon">
            <svg viewBox="0 0 24 24">
              <path d="M17 21V13H7V21" stroke="currentColor" stroke-width="2" fill="none"/>
              <path d="M7 13L12 8L17 13" stroke="currentColor" stroke-width="2" fill="none"/>
              <circle cx="19" cy="5" r="2" stroke="currentColor" stroke-width="2" fill="none"/>
            </svg>
          </div>
          <div class="benefit-content">
            <h4>个性化推荐</h4>
            <p>根据您的兴趣推荐学习内容</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/user.js'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    
    const formData = reactive({
      username: '',
      email: '',
      password: '',
      confirmPassword: ''
    })
    
    const loading = ref(false)
    const error = ref('')
    const success = ref('')
    
    const validateForm = () => {
      // 清除之前的错误
      error.value = ''
      
      // 用户名验证
      if (!formData.username.trim()) {
        error.value = '请输入用户名'
        return false
      }
      
      if (formData.username.trim().length < 3 || formData.username.trim().length > 20) {
        error.value = '用户名长度应为3-20个字符'
        return false
      }
      
      // 邮箱验证
      if (!formData.email.trim()) {
        error.value = '请输入邮箱地址'
        return false
      }
      
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(formData.email.trim())) {
        error.value = '请输入有效的邮箱地址'
        return false
      }
      
      // 密码验证
      if (!formData.password) {
        error.value = '请输入密码'
        return false
      }
      
      if (formData.password.length < 6) {
        error.value = '密码长度至少6个字符'
        return false
      }
      
      // 确认密码验证
      if (!formData.confirmPassword) {
        error.value = '请确认密码'
        return false
      }
      
      if (formData.password !== formData.confirmPassword) {
        error.value = '两次输入的密码不一致'
        return false
      }
      
      return true
    }
    
    const handleRegister = async () => {
      if (!validateForm()) {
        return
      }
      
      loading.value = true
      error.value = ''
      success.value = ''
      
      try {
        const result = await register({
          username: formData.username.trim(),
          email: formData.email.trim(),
          password: formData.password,
          confirmPassword: formData.confirmPassword
        })
        
        if (result.success) {
          success.value = '注册成功！即将跳转到登录页面...'
          
          // 2秒后跳转到登录页
          setTimeout(() => {
            router.push('/login')
          }, 2000)
        } else {
          error.value = result.message || '注册失败，请稍后重试'
        }
      } catch (err) {
        error.value = '网络错误，请稍后重试'
        console.error('注册错误:', err)
      } finally {
        loading.value = false
      }
    }
    
    return {
      formData,
      loading,
      error,
      success,
      handleRegister
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  align-items: stretch;
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
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
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
  top: 60%;
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
  top: 30%;
  right: 30%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 左侧边栏 */
.left-sidebar {
  width: 260px;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2rem;
  z-index: 2;
  position: relative;
}

.learning-stats {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 1.5rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-icon {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.stat-content {
  color: white;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.8;
}

.daily-quote {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 1.5rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.quote-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.quote-icon svg {
  width: 20px;
  height: 20px;
}

.quote-content {
  color: white;
}

.quote-text {
  font-style: italic;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.quote-author {
  font-size: 0.9rem;
  opacity: 0.8;
  margin: 0;
}

/* 中央注册表单 */
.main-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  z-index: 2;
  position: relative;
  min-height: 100vh;
}

.register-form {
  width: 100%;
  max-width: 480px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 2rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  margin: auto;
}

.register-form h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
  font-size: 1.8rem;
  font-weight: 600;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1rem;
  border: 1px solid #fcc;
  font-size: 0.9rem;
}

.success-message {
  background-color: #efe;
  color: #3c763d;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1rem;
  border: 1px solid #cfc;
  font-size: 0.9rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.875rem;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #28a745;
}

.form-group input:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

.form-hint {
  display: block;
  margin-top: 0.25rem;
  color: #6c757d;
  font-size: 0.8rem;
}

.register-btn {
  width: 100%;
  padding: 0.875rem;
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(40, 167, 69, 0.4);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.form-links {
  margin-top: 1.5rem;
  text-align: center;
}

.link {
  color: #28a745;
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.3s ease;
}

.link:hover {
  color: #20c997;
  text-decoration: underline;
}

/* 右侧边栏 */
.right-sidebar {
  width: 300px;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2rem;
  z-index: 2;
  position: relative;
}

.registration-guide {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 1.5rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.guide-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  color: white;
}

.guide-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.guide-icon svg {
  width: 20px;
  height: 20px;
}

.guide-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.guide-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1rem;
  color: white;
}

.guide-item:last-child {
  margin-bottom: 0;
}

.guide-step {
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.9rem;
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.guide-content h4 {
  margin: 0 0 0.25rem 0;
  font-size: 0.95rem;
  font-weight: 600;
}

.guide-content p {
  margin: 0;
  font-size: 0.85rem;
  opacity: 0.8;
  line-height: 1.3;
}

.learning-benefits {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 1.5rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.benefits-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  color: white;
}

.benefits-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.benefits-icon svg {
  width: 20px;
  height: 20px;
}

.benefits-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1rem;
  color: white;
  transition: transform 0.2s ease;
}

.benefit-item:last-child {
  margin-bottom: 0;
}

.benefit-item:hover {
  transform: translateX(5px);
}

.benefit-icon {
  width: 35px;
  height: 35px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.benefit-icon svg {
  width: 18px;
  height: 18px;
}

.benefit-content h4 {
  margin: 0 0 0.25rem 0;
  font-size: 0.95rem;
  font-weight: 600;
}

.benefit-content p {
  margin: 0;
  font-size: 0.85rem;
  opacity: 0.8;
  line-height: 1.3;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .left-sidebar {
    width: 220px;
    padding: 1.8rem;
  }
  
  .register-form {
    max-width: 420px;
  }
}

@media (max-width: 1024px) {
  .register-container {
    flex-direction: column;
    min-height: 100vh;
  }
  
  .left-sidebar,
  .right-sidebar {
    display: none;
  }
  
  .main-content {
    width: 100%;
    padding: 2rem 1rem;
    min-height: 100vh;
  }
  
  .register-form {
    max-width: 500px;
    margin: 0 auto;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 1rem;
  }
  
  .register-form {
    max-width: 100%;
    padding: 1.5rem;
    margin: 0;
  }
  
  .register-form h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .register-form {
    padding: 1rem;
  }
  
  .register-form h2 {
    font-size: 1.3rem;
  }
  
  .form-group input {
    padding: 0.75rem;
  }
  
  .register-btn {
    padding: 0.75rem;
  }
}
</style>