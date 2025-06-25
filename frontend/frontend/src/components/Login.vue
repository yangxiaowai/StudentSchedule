<template>
  <div class="login-page">
    <h2>登录系统</h2>
    <form @submit.prevent="login">
      <input v-model="username" placeholder="用户名" required />
      <input v-model="password" type="password" placeholder="密码" required />
      <button type="submit">登录</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
const username = ref('')
const password = ref('')
const router = useRouter()
function login() {
  // 这里可以加实际的登录校验
  if (username.value && password.value) {
    localStorage.setItem('isLogin', '1')
    router.push('/data-integration')
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
}
form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 240px;
}
input, button {
  padding: 10px;
  font-size: 1rem;
}
button {
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
button:hover {
  background: #1565c0;
}
</style>