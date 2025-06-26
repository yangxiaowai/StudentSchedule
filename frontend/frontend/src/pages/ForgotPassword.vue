<template>
  <div class="forgot-password-container">
    <div class="forgot-password-form">
      <h2>找回密码</h2>
      
      <!-- 错误提示 -->
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
      
      <!-- 成功提示 -->
      <div v-if="success" class="success-message">
        {{ success }}
      </div>
      
      <div v-if="!emailSent">
        <p class="description">
          请输入您注册时使用的邮箱地址，我们将向您发送密码重置链接。
        </p>
        
        <form @submit.prevent="handleSendEmail">
          <div class="form-group">
            <label for="email">邮箱地址:</label>
            <input
              type="email"
              id="email"
              v-model="email"
              :disabled="loading"
              placeholder="请输入您的邮箱地址"
              required
            />
          </div>
          
          <button 
            type="submit" 
            class="send-btn"
            :disabled="loading"
          >
            {{ loading ? '发送中...' : '发送重置链接' }}
          </button>
        </form>
      </div>
      
      <div v-else class="email-sent">
        <div class="success-icon">✓</div>
        <h3>邮件已发送</h3>
        <p>我们已向 <strong>{{ email }}</strong> 发送了密码重置链接。</p>
        <p>请检查您的邮箱（包括垃圾邮件文件夹），并点击链接重置密码。</p>
        
        <div class="resend-section">
          <p>没有收到邮件？</p>
          <button 
            @click="resendEmail" 
            class="resend-btn"
            :disabled="resendLoading || resendCountdown > 0"
          >
            {{ resendCountdown > 0 ? `${resendCountdown}秒后可重发` : (resendLoading ? '重新发送中...' : '重新发送') }}
          </button>
        </div>
      </div>
      
      <div class="form-links">
        <router-link to="/login" class="link">返回登录</router-link>
        <router-link to="/register" class="link">还没有账号？立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { forgotPassword } from '../api/user.js'

export default {
  name: 'ForgotPassword',
  setup() {
    const email = ref('')
    const loading = ref(false)
    const error = ref('')
    const success = ref('')
    const emailSent = ref(false)
    const resendLoading = ref(false)
    const resendCountdown = ref(0)
    
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
    
    const startResendCountdown = () => {
      resendCountdown.value = 60
      const timer = setInterval(() => {
        resendCountdown.value--
        if (resendCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }
    
    const handleSendEmail = async () => {
      if (!validateEmail()) {
        return
      }
      
      loading.value = true
      error.value = ''
      success.value = ''
      
      try {
        const result = await forgotPassword(email.value.trim())
        
        if (result.success) {
          emailSent.value = true
          success.value = '密码重置邮件发送成功'
          startResendCountdown()
        } else {
          error.value = result.message || '发送失败，请稍后重试'
        }
      } catch (err) {
        error.value = '网络错误，请稍后重试'
        console.error('发送密码重置邮件错误:', err)
      } finally {
        loading.value = false
      }
    }
    
    const resendEmail = async () => {
      resendLoading.value = true
      error.value = ''
      
      try {
        const result = await forgotPassword(email.value.trim())
        
        if (result.success) {
          success.value = '邮件已重新发送'
          startResendCountdown()
        } else {
          error.value = result.message || '重新发送失败，请稍后重试'
        }
      } catch (err) {
        error.value = '网络错误，请稍后重试'
        console.error('重新发送密码重置邮件错误:', err)
      } finally {
        resendLoading.value = false
      }
    }
    
    return {
      email,
      loading,
      error,
      success,
      emailSent,
      resendLoading,
      resendCountdown,
      handleSendEmail,
      resendEmail
    }
  }
}
</script>

<style scoped>
.forgot-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #6c5ce7 0%, #a29bfe 100%);
  padding: 1rem;
}

.forgot-password-form {
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 450px;
}

.forgot-password-form h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
  font-size: 1.8rem;
  font-weight: 600;
}

.description {
  color: #666;
  margin-bottom: 1.5rem;
  line-height: 1.5;
  text-align: center;
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
  border-color: #6c5ce7;
}

.form-group input:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

.send-btn {
  width: 100%;
  padding: 0.875rem;
  background: linear-gradient(135deg, #6c5ce7 0%, #a29bfe 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(108, 92, 231, 0.4);
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.email-sent {
  text-align: center;
}

.success-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #00b894 0%, #00cec9 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
}

.email-sent h3 {
  color: #333;
  margin-bottom: 1rem;
  font-size: 1.3rem;
}

.email-sent p {
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.resend-section {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #eee;
}

.resend-section p {
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.resend-btn {
  background: transparent;
  color: #6c5ce7;
  border: 2px solid #6c5ce7;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.resend-btn:hover:not(:disabled) {
  background: #6c5ce7;
  color: white;
}

.resend-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-links {
  margin-top: 2rem;
  text-align: center;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.link {
  color: #6c5ce7;
  text-decoration: none;
  font-size: 0.9rem;
  margin: 0 0.5rem;
  transition: color 0.3s ease;
}

.link:hover {
  color: #a29bfe;
  text-decoration: underline;
}
</style>