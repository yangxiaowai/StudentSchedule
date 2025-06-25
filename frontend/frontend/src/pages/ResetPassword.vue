<template>
  <div class="reset-password-container">
    <div class="reset-password-form">
      <h2>重置密码</h2>
      
      <!-- 错误提示 -->
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
      
      <!-- 成功提示 -->
      <div v-if="success" class="success-message">
        {{ success }}
      </div>
      
      <!-- 验证中状态 -->
      <div v-if="validating" class="validating">
        <div class="spinner"></div>
        <p>正在验证重置链接...</p>
      </div>
      
      <!-- 无效链接 -->
      <div v-else-if="!tokenValid" class="invalid-token">
        <div class="error-icon">✗</div>
        <h3>链接无效或已过期</h3>
        <p>密码重置链接无效或已过期，请重新申请密码重置。</p>
        <router-link to="/forgot-password" class="btn btn-primary">
          重新申请密码重置
        </router-link>
      </div>
      
      <!-- 重置密码表单 -->
      <div v-else-if="!resetSuccess">
        <p class="description">
          请输入您的新密码。
        </p>
        
        <form @submit.prevent="handleResetPassword">
          <div class="form-group">
            <label for="password">新密码:</label>
            <input
              type="password"
              id="password"
              v-model="formData.password"
              :disabled="loading"
              placeholder="请输入新密码"
              required
            />
            <small class="form-hint">密码长度至少6个字符</small>
          </div>
          
          <div class="form-group">
            <label for="confirmPassword">确认新密码:</label>
            <input
              type="password"
              id="confirmPassword"
              v-model="formData.confirmPassword"
              :disabled="loading"
              placeholder="请再次输入新密码"
              required
            />
          </div>
          
          <button 
            type="submit" 
            class="reset-btn"
            :disabled="loading"
          >
            {{ loading ? '重置中...' : '重置密码' }}
          </button>
        </form>
      </div>
      
      <!-- 重置成功 -->
      <div v-else class="reset-success">
        <div class="success-icon">✓</div>
        <h3>密码重置成功</h3>
        <p>您的密码已成功重置，现在可以使用新密码登录了。</p>
        <router-link to="/login" class="btn btn-primary">
          立即登录
        </router-link>
      </div>
      
      <div class="form-links" v-if="tokenValid && !resetSuccess">
        <router-link to="/login" class="link">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { validateResetToken, resetPassword } from '../api/user.js'

export default {
  name: 'ResetPassword',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const formData = reactive({
      password: '',
      confirmPassword: ''
    })
    
    const loading = ref(false)
    const validating = ref(true)
    const error = ref('')
    const success = ref('')
    const tokenValid = ref(false)
    const resetSuccess = ref(false)
    const resetToken = ref('')
    
    // 验证重置令牌
    const validateToken = async () => {
      const token = route.query.token
      
      if (!token) {
        error.value = '缺少重置令牌'
        validating.value = false
        return
      }
      
      resetToken.value = token
      
      try {
        const result = await validateResetToken(token)
        
        if (result.success) {
          tokenValid.value = true
        } else {
          error.value = result.message || '重置链接无效或已过期'
        }
      } catch (err) {
        error.value = '验证重置链接时发生错误'
        console.error('验证重置令牌错误:', err)
      } finally {
        validating.value = false
      }
    }
    
    const validateForm = () => {
      error.value = ''
      
      if (!formData.password) {
        error.value = '请输入新密码'
        return false
      }
      
      if (formData.password.length < 6) {
        error.value = '密码长度至少6个字符'
        return false
      }
      
      if (!formData.confirmPassword) {
        error.value = '请确认新密码'
        return false
      }
      
      if (formData.password !== formData.confirmPassword) {
        error.value = '两次输入的密码不一致'
        return false
      }
      
      return true
    }
    
    const handleResetPassword = async () => {
      if (!validateForm()) {
        return
      }
      
      loading.value = true
      error.value = ''
      success.value = ''
      
      try {
        const result = await resetPassword(resetToken.value, formData.password)
        
        if (result.success) {
          resetSuccess.value = true
          success.value = '密码重置成功'
        } else {
          error.value = result.message || '密码重置失败，请稍后重试'
        }
      } catch (err) {
        error.value = '网络错误，请稍后重试'
        console.error('重置密码错误:', err)
      } finally {
        loading.value = false
      }
    }
    
    onMounted(() => {
      validateToken()
    })
    
    return {
      formData,
      loading,
      validating,
      error,
      success,
      tokenValid,
      resetSuccess,
      handleResetPassword
    }
  }
}
</script>

<style scoped>
.reset-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #fd79a8 0%, #fdcb6e 100%);
  padding: 1rem;
}

.reset-password-form {
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 450px;
}

.reset-password-form h2 {
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

.validating {
  text-align: center;
  padding: 2rem 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #fd79a8;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.invalid-token,
.reset-success {
  text-align: center;
  padding: 1rem 0;
}

.error-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #e17055 0%, #d63031 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
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

.invalid-token h3,
.reset-success h3 {
  color: #333;
  margin-bottom: 1rem;
  font-size: 1.3rem;
}

.invalid-token p,
.reset-success p {
  color: #666;
  margin-bottom: 1.5rem;
  line-height: 1.5;
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
  border-color: #fd79a8;
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

.reset-btn,
.btn {
  padding: 0.875rem 1.5rem;
  background: linear-gradient(135deg, #fd79a8 0%, #fdcb6e 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}

.reset-btn {
  width: 100%;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.reset-btn:hover:not(:disabled),
.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(253, 121, 168, 0.4);
}

.btn-primary:hover {
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.reset-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.form-links {
  margin-top: 2rem;
  text-align: center;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.link {
  color: #fd79a8;
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.3s ease;
}

.link:hover {
  color: #fdcb6e;
  text-decoration: underline;
}
</style>