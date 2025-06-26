<template>
  <div class="register-container">
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
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  padding: 1rem;
}

.register-form {
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 450px;
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
</style>