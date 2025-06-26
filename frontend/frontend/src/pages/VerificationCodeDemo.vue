<template>
  <div class="verification-demo-container">
    <div class="verification-demo-form">
      <h2>邮箱验证码演示</h2>
      
      <!-- 错误提示 -->
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
      
      <!-- 成功提示 -->
      <div v-if="success" class="success-message">
        {{ success }}
      </div>
      
      <div class="form-group">
        <label for="email">邮箱地址</label>
        <input 
          type="email" 
          id="email" 
          v-model="email" 
          placeholder="请输入邮箱地址"
          :disabled="loading || verifyLoading"
        >
      </div>
      
      <div class="form-group">
        <button 
          @click="handleSendCode" 
          class="send-code-btn"
          :disabled="loading || countdown > 0"
        >
          {{ countdown > 0 ? `${countdown}秒后可重发` : (loading ? '发送中...' : '发送验证码') }}
        </button>
      </div>
      
      <div class="form-group" v-if="codeSent">
        <label for="code">验证码</label>
        <input 
          type="text" 
          id="code" 
          v-model="verificationCode" 
          placeholder="请输入6位验证码"
          maxlength="6"
          :disabled="verifyLoading"
        >
      </div>
      
      <div class="form-group" v-if="codeSent">
        <button 
          @click="handleVerifyCode" 
          class="verify-btn"
          :disabled="verifyLoading || !verificationCode"
        >
          {{ verifyLoading ? '验证中...' : '验证验证码' }}
        </button>
      </div>
      
      <div class="form-links">
        <router-link to="/login" class="link">返回登录</router-link>
        <router-link to="/forgot-password" class="link">忘记密码</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { sendVerificationCode, verifyCode } from '../api/user.js'

export default {
  name: 'VerificationCodeDemo',
  setup() {
    const email = ref('')
    const verificationCode = ref('')
    const loading = ref(false)
    const verifyLoading = ref(false)
    const error = ref('')
    const success = ref('')
    const codeSent = ref(false)
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
          codeSent.value = true
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
    
    const handleVerifyCode = async () => {
      if (!verificationCode.value.trim()) {
        error.value = '请输入验证码'
        return
      }
      
      if (verificationCode.value.length !== 6) {
        error.value = '验证码应为6位数字'
        return
      }
      
      verifyLoading.value = true
      error.value = ''
      success.value = ''
      
      try {
        const result = await verifyCode(email.value.trim(), verificationCode.value.trim())
        
        if (result.success) {
          success.value = '验证码验证成功！'
          // 这里可以执行验证成功后的逻辑
        } else {
          error.value = result.message || '验证失败，请重试'
        }
      } catch (err) {
        error.value = '网络错误，请稍后重试'
        console.error('验证验证码错误:', err)
      } finally {
        verifyLoading.value = false
      }
    }
    
    return {
      email,
      verificationCode,
      loading,
      verifyLoading,
      error,
      success,
      codeSent,
      countdown,
      handleSendCode,
      handleVerifyCode
    }
  }
}
</script>

<style scoped>
.verification-demo-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.verification-demo-form {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.verification-demo-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #555;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 5px;
  font-size: 16px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.send-code-btn, .verify-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.send-code-btn:hover:not(:disabled), .verify-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.send-code-btn:disabled, .verify-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 10px;
  border-radius: 5px;
  margin-bottom: 20px;
  border: 1px solid #fcc;
}

.success-message {
  background-color: #efe;
  color: #363;
  padding: 10px;
  border-radius: 5px;
  margin-bottom: 20px;
  border: 1px solid #cfc;
}

.form-links {
  text-align: center;
  margin-top: 20px;
}

.link {
  color: #667eea;
  text-decoration: none;
  margin: 0 10px;
  font-size: 14px;
}

.link:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .verification-demo-form {
    padding: 30px 20px;
  }
  
  .verification-demo-form h2 {
    font-size: 20px;
  }
}
</style>