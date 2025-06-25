<template>
  <div class="login-container">
    <div class="login-form">
      <h2>用户登录</h2>
      
      <!-- 错误提示 -->
      <div v-if="userStore.error" class="error-message">
        {{ userStore.error }}
      </div>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名:</label>
          <input
            type="text"
            id="username"
            v-model="formData.username"
            :disabled="userStore.loading"
            required
          />
        </div>
        <div class="form-group">
          <label for="password">密码:</label>
          <input
            type="password"
            id="password"
            v-model="formData.password"
            :disabled="userStore.loading"
            required
          />
        </div>
        <button 
          type="submit" 
          class="login-btn"
          :disabled="userStore.loading"
        >
          {{ userStore.loading ? '登录中...' : '登录' }}
        </button>
      </form>
      
      <div class="form-links">
        <router-link to="/register" class="link">还没有账号？立即注册</router-link>
        <router-link to="/forgot-password" class="link">忘记密码？</router-link>
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 1rem;
}

.login-form {
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 420px;
}

.login-form h2 {
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
  border-color: #667eea;
}

.form-group input:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

.login-btn {
  width: 100%;
  padding: 0.875rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.form-links {
  margin-top: 1.5rem;
  text-align: center;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-size: 0.9rem;
  margin: 0 0.5rem;
  transition: color 0.3s ease;
}

.link:hover {
  color: #764ba2;
  text-decoration: underline;
}
</style>