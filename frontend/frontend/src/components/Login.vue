<template>
  <div class="login-page">
    <h2>用户登录</h2>
    <form @submit.prevent="login">
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
      <input v-model="username" placeholder="用户名" required :disabled="isLoading" />
      <input v-model="password" type="password" placeholder="密码" required :disabled="isLoading" />
      <button type="submit" :disabled="isLoading">
        {{ isLoading ? '登录中...' : '登录' }}
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const username = ref('')
const password = ref('')
const router = useRouter()
const errorMessage = ref('')
const isLoading = ref(false)

async function login() {
  if (!username.value || !password.value) {
    errorMessage.value = '请输入用户名和密码'
    return
  }
  
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    const response = await axios.post('http://localhost:8080/api/user/login', {
      username: username.value,
      password: password.value
    }, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    })
    
    if (response.data.success) {
      // 保存登录信息
      localStorage.setItem('isLogin', '1')
      localStorage.setItem('accessToken', response.data.data.accessToken)
      localStorage.setItem('refreshToken', response.data.data.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify(response.data.data.userInfo))
      
      // 跳转到主页面
      await router.push('/data-integration')
      console.log('跳转到:', '/data-integration')
    } else {
      errorMessage.value = response.data.message || '登录失败'
    }
  } catch (error) {
    console.error('登录错误:', error)
    if (error.response) {
      errorMessage.value = error.response.data.message || '登录失败，请检查用户名和密码'
    } else {
      errorMessage.value = '网络错误，请稍后重试'
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

h2 {
  color: white;
  margin-bottom: 2rem;
  font-size: 2rem;
}

form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 320px;
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #ffcdd2;
  font-size: 0.9rem;
  text-align: center;
}

input, button {
  padding: 12px;
  font-size: 1rem;
  border-radius: 6px;
  border: 1px solid #ddd;
}

input:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.2);
}

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

button {
  background: #1976d2;
  color: #fff;
  border: none;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

button:hover:not(:disabled) {
  background: #1565c0;
  transform: translateY(-1px);
}

button:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}
</style>