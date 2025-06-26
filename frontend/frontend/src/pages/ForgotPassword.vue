<template>
  <div class="forgot-password-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
    
    <div class="forgot-password-card">
      <!-- 头部区域 -->
      <div class="card-header">
        <div class="logo-section">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            </svg>
          </div>
          <h1>学习计划系统</h1>
        </div>
        <h2>找回密码</h2>
        
        <!-- 步骤指示器 -->
        <div class="step-indicator">
          <div class="step" :class="{ active: step >= 1, completed: step > 1 }">
            <div class="step-number">1</div>
            <div class="step-label">验证邮箱</div>
          </div>
          <div class="step-line" :class="{ active: step > 1 }"></div>
          <div class="step" :class="{ active: step >= 2, completed: step > 2 }">
            <div class="step-number">2</div>
            <div class="step-label">重置密码</div>
          </div>
          <div class="step-line" :class="{ active: step > 2 }"></div>
          <div class="step" :class="{ active: step >= 3 }">
            <div class="step-number">3</div>
            <div class="step-label">完成</div>
          </div>
        </div>
      </div>
      
      <!-- 消息提示区域 -->
      <transition name="message">
        <div v-if="error" class="message error-message">
          <div class="message-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <span>{{ error }}</span>
        </div>
      </transition>
      
      <transition name="message">
        <div v-if="success" class="message success-message">
          <div class="message-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <span>{{ success }}</span>
        </div>
      </transition>
      
      <!-- 内容区域 -->
      <div class="card-content">
        <!-- 步骤1: 输入邮箱并发送验证码 -->
        <transition name="slide" mode="out-in">
          <div v-if="step === 1" key="step1" class="step-content">
            <div class="step-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M4 4H20C21.1 4 22 4.9 22 6V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4Z" stroke="currentColor" stroke-width="2"/>
                <polyline points="22,6 12,13 2,6" stroke="currentColor" stroke-width="2"/>
              </svg>
            </div>
            <h3>验证您的邮箱</h3>
            <p class="step-description">
              请输入您注册时使用的邮箱地址，我们将向您发送一个6位数的验证码。
            </p>
            
            <form @submit.prevent="handleSendCode" class="form">
              <div class="input-group">
                <div class="input-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M4 4H20C21.1 4 22 4.9 22 6V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4Z" stroke="currentColor" stroke-width="2"/>
                    <polyline points="22,6 12,13 2,6" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </div>
                <input
                  type="email"
                  v-model="email"
                  :disabled="loading"
                  placeholder="请输入您的邮箱地址"
                  class="form-input"
                  required
                />
              </div>
              
              <button 
                type="submit" 
                class="btn btn-primary btn-large"
                :disabled="loading || countdown > 0"
                :class="{ loading: loading }"
              >
                <span v-if="loading" class="loading-spinner"></span>
                <span v-else-if="countdown > 0">{{ countdown }}秒后可重发</span>
                <span v-else>发送验证码</span>
              </button>
            </form>
          </div>
          
          <!-- 步骤2: 输入验证码和新密码 -->
          <div v-else-if="step === 2" key="step2" class="step-content">
            <div class="step-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
                <circle cx="12" cy="16" r="1" fill="currentColor"/>
                <path d="M7 11V7A5 5 0 0 1 17 7V11" stroke="currentColor" stroke-width="2"/>
              </svg>
            </div>
            <h3>设置新密码</h3>
            <p class="step-description">
              验证码已发送到 <strong class="email-highlight">{{ email }}</strong><br>
              请输入验证码并设置您的新密码。
            </p>
            
            <form @submit.prevent="handleResetPassword" class="form">
              <div class="input-group">
                <div class="input-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </div>
                <input
                  type="text"
                  v-model="verificationCode"
                  :disabled="loading"
                  placeholder="请输入6位验证码"
                  maxlength="6"
                  class="form-input code-input"
                  required
                />
              </div>
              
              <div class="input-group">
                <div class="input-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                    <path d="M7 11V7A5 5 0 0 1 17 7V11" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </div>
                <input
                  type="password"
                  v-model="newPassword"
                  :disabled="loading"
                  placeholder="请输入新密码（至少6位）"
                  class="form-input"
                  required
                />
              </div>
              
              <div class="input-group">
                <div class="input-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                    <path d="M7 11V7A5 5 0 0 1 17 7V11" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </div>
                <input
                  type="password"
                  v-model="confirmPassword"
                  :disabled="loading"
                  placeholder="请再次输入新密码"
                  class="form-input"
                  required
                />
              </div>
              
              <div class="button-group">
                <button 
                  type="submit" 
                  class="btn btn-primary btn-large"
                  :disabled="loading"
                  :class="{ loading: loading }"
                >
                  <span v-if="loading" class="loading-spinner"></span>
                  <span v-else>重置密码</span>
                </button>
                
                <button 
                  type="button" 
                  class="btn btn-secondary"
                  :disabled="countdown > 0 || loading"
                  @click="handleResendCode"
                >
                  {{ countdown > 0 ? `${countdown}秒后可重发` : '重新发送验证码' }}
                </button>
              </div>
            </form>
          </div>
          
          <!-- 步骤3: 重置成功 -->
          <div v-else-if="step === 3" key="step3" class="step-content">
            <div class="step-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              </svg>
            </div>
            <h3>密码重置成功</h3>
            <p class="step-description">
              您的密码已成功重置，请使用新密码登录。
            </p>
            <router-link to="/login" class="btn btn-primary btn-large">
              返回登录
            </router-link>
          </div>
        </transition>
      </div>
      
      <div v-if="step !== 3" class="form-links">
        <router-link to="/login" class="link">返回登录</router-link>
        <router-link to="/register" class="link">还没有账号？立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { sendVerificationCode, resetPasswordByCode } from '../api/user.js'

export default {
  name: 'ForgotPassword',
  setup() {
    const step = ref(1) // 1: 输入邮箱, 2: 输入验证码和密码, 3: 重置成功
    const email = ref('')
    const verificationCode = ref('')
    const newPassword = ref('')
    const confirmPassword = ref('')
    const loading = ref(false)
    const error = ref('')
    const success = ref('')
    const countdown = ref(0)
    
    const validateEmail = () => {
      error.value = ''
      
      if (!email.value.trim()) {
        error.value = '请输入邮箱地址'
        return false
      }
      
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(email.value.trim())) {
        error.value = '请输入有效的邮箱地址'
        return false
      }
      
      return true
    }
    
    const validatePassword = () => {
      error.value = ''
      
      if (!newPassword.value || newPassword.value.length < 6) {
        error.value = '密码长度不能少于6位'
        return false
      }
      
      if (newPassword.value !== confirmPassword.value) {
        error.value = '两次输入的密码不一致'
        return false
      }
      
      return true
    }
    
    const startCountdown = () => {
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }
    
    const handleSendCode = async () => {
      if (!validateEmail()) {
        return
      }
      
      loading.value = true
      error.value = ''
      success.value = ''
      
      try {
        const result = await sendVerificationCode(email.value.trim())
        
        if (result.success) {
          step.value = 2
          success.value = '验证码已发送到您的邮箱'
          startCountdown()
        } else {
          error.value = result.message || '发送失败，请稍后重试'
        }
      } catch (err) {
        error.value = '网络错误，请稍后重试'
        console.error('发送验证码错误:', err)
      } finally {
        loading.value = false
      }
    }
    
    const handleResetPassword = async () => {
      if (!verificationCode.value.trim()) {
        error.value = '请输入验证码'
        return
      }
      
      if (verificationCode.value.length !== 6) {
        error.value = '验证码应为6位数字'
        return
      }
      
      if (!validatePassword()) {
        return
      }
      
      loading.value = true
      error.value = ''
      success.value = ''
      
      try {
        const result = await resetPasswordByCode(
          email.value.trim(),
          verificationCode.value.trim(),
          newPassword.value
        )
        
        if (result.success) {
          step.value = 3
          success.value = '密码重置成功'
        } else {
          error.value = result.message || '重置失败，请稍后重试'
        }
      } catch (err) {
        error.value = '网络错误，请稍后重试'
        console.error('重置密码错误:', err)
      } finally {
        loading.value = false
      }
    }
    
    const handleResendCode = async () => {
      loading.value = true
      error.value = ''
      
      try {
        const result = await sendVerificationCode(email.value.trim())
        
        if (result.success) {
          success.value = '验证码已重新发送'
          startCountdown()
        } else {
          error.value = result.message || '重新发送失败，请稍后重试'
        }
      } catch (err) {
        error.value = '网络错误，请稍后重试'
        console.error('重新发送验证码错误:', err)
      } finally {
        loading.value = false
      }
    }
    
    return {
      step,
      email,
      verificationCode,
      newPassword,
      confirmPassword,
      loading,
      error,
      success,
      countdown,
      handleSendCode,
      handleResetPassword,
      handleResendCode
    }
  }
}
</script>

<style scoped>
/* 全局容器 */
.forgot-password-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 10%;
  animation-delay: 2s;
}

.circle-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 主卡片 */
.forgot-password-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 480px;
  position: relative;
  z-index: 1;
  overflow: hidden;
}

/* 卡片头部 */
.card-header {
  padding: 40px 40px 20px;
  text-align: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  margin-right: 12px;
  color: #667eea;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.card-header h1 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.card-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin: 0 0 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e1e5e9;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s ease;
  margin-bottom: 8px;
}

.step.active .step-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: scale(1.1);
}

.step.completed .step-number {
  background: #4CAF50;
  color: white;
}

.step-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.step.active .step-label {
  color: #667eea;
  font-weight: 600;
}

.step-line {
  width: 60px;
  height: 2px;
  background: #e1e5e9;
  margin: 0 10px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.step-line.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 消息提示 */
.message {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-radius: 12px;
  margin: 20px 40px;
  font-weight: 500;
}

.message-icon {
  width: 20px;
  height: 20px;
  margin-right: 12px;
  flex-shrink: 0;
}

.error-message {
  background: rgba(244, 67, 54, 0.1);
  color: #d32f2f;
  border: 1px solid rgba(244, 67, 54, 0.2);
}

.success-message {
  background: rgba(76, 175, 80, 0.1);
  color: #388e3c;
  border: 1px solid rgba(76, 175, 80, 0.2);
}

/* 消息动画 */
.message-enter-active, .message-leave-active {
  transition: all 0.3s ease;
}

.message-enter-from, .message-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 卡片内容 */
.card-content {
  padding: 20px 40px 40px;
}

/* 步骤内容 */
.step-content {
  text-align: center;
}

.step-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.step-icon svg {
  width: 40px;
  height: 40px;
}

.step-content h3 {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0 0 16px;
}

.step-description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 32px;
  font-size: 16px;
}

.email-highlight {
  color: #667eea;
  font-weight: 600;
}

/* 表单样式 */
.form {
  text-align: left;
}

.input-group {
  position: relative;
  margin-bottom: 24px;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #999;
  z-index: 2;
}

.form-input {
  width: 100%;
  padding: 16px 16px 16px 48px;
  border: 2px solid #e1e5e9;
  border-radius: 12px;
  font-size: 16px;
  background: white;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input:focus + .input-icon {
  color: #667eea;
}

.form-input:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

.code-input {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 4px;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 16px 24px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  position: relative;
  overflow: hidden;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background: transparent;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-secondary:hover {
  background: #667eea;
  color: white;
}

.btn-large {
  width: 100%;
  padding: 18px 24px;
  font-size: 18px;
}

.btn:disabled {
  background: #e1e5e9;
  color: #999;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn.loading {
  pointer-events: none;
}

/* 加载动画 */
.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 按钮组 */
.button-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 成功页面 */
.success-content {
  padding: 20px 0;
}

.success-animation {
  position: relative;
  margin-bottom: 32px;
}

.success-icon {
  width: 100px;
  height: 100px;
  background: #4CAF50;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  color: white;
  animation: successPulse 0.6s ease-out;
}

.success-icon svg {
  width: 50px;
  height: 50px;
}

.success-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  border: 2px solid #4CAF50;
  border-radius: 50%;
  animation: ripple 1.5s ease-out infinite;
}

@keyframes successPulse {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes ripple {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(2);
    opacity: 0;
  }
}

.success-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 底部链接 */
.form-links {
  text-align: center;
  margin-top: 25px;
  padding-top: 25px;
  border-top: 1px solid #eee;
}

.link {
  color: #667eea;
  text-decoration: none;
  margin: 0 10px;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

/* 步骤切换动画 */
.slide-enter-active, .slide-leave-active {
  transition: all 0.4s ease;
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 响应式设计 */
@media (max-width: 640px) {
  .forgot-password-container {
    padding: 16px;
  }
  
  .forgot-password-card {
    max-width: 100%;
  }
  
  .card-header {
    padding: 32px 24px 16px;
  }
  
  .card-content {
    padding: 16px 24px 32px;
  }
  
  .card-header h2 {
    font-size: 28px;
  }
  
  .step-content h3 {
    font-size: 20px;
  }
  
  .step-icon {
    width: 60px;
    height: 60px;
  }
  
  .step-icon svg {
    width: 30px;
    height: 30px;
  }
  
  .step-indicator {
    transform: scale(0.9);
  }
  
  .message {
    margin: 16px 24px;
  }
}

@media (max-width: 480px) {
  .card-header h1 {
    font-size: 18px;
  }
  
  .card-header h2 {
    font-size: 24px;
  }
  
  .step-description {
    font-size: 14px;
  }
  
  .form-input {
    font-size: 16px;
    padding: 14px 14px 14px 44px;
  }
  
  .btn {
    font-size: 16px;
    padding: 14px 20px;
  }
  
  .btn-large {
    padding: 16px 20px;
    font-size: 16px;
  }
}
</style>