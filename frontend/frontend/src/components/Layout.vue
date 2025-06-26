<template>
  <div class="layout">
    <nav class="sidebar">
      <div class="nav-links">
        <router-link to="task-manager" class="nav-link">学习任务管理</router-link>
        <router-link to="history" class="nav-link">历史学习记录</router-link>
        <router-link to="data-integration" class="nav-link">资料检索库</router-link>
        <router-link to="plan-manager" class="nav-link">学习计划管理</router-link>
        <router-link to="ai-analysis" class="nav-link">AI分析</router-link>
      </div>
      <div class="user-section">
        <div class="user-info">
          <span class="username">{{ userStore.userInfo?.username || '用户' }}</span>
        </div>
        <button @click="handleLogout" class="logout-btn">退出登录</button>
      </div>
    </nav>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { userStore } from '../store/user.js'
import { useRouter } from 'vue-router'

const router = useRouter()

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: 200px;
  background: #f4f8fb;
  display: flex;
  flex-direction: column;
  padding: 24px 0;
  box-shadow: 2px 0 8px #e3ecfa;
}
.nav-links {
  flex: 1;
}
.nav-link {
  padding: 16px 32px;
  color: #2a4d69;
  text-decoration: none;
  font-weight: 600;
  transition: background 0.2s, color 0.2s;
  display: block;
}
.nav-link.router-link-active {
  background: #e3ecfa;
  color: #1976d2;
}
.user-section {
  padding: 16px 24px;
  border-top: 1px solid #e3ecfa;
  margin-top: auto;
}
.user-info {
  margin-bottom: 12px;
}
.username {
  color: #2a4d69;
  font-weight: 600;
  font-size: 14px;
}
.logout-btn {
  width: 100%;
  padding: 8px 16px;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.logout-btn:hover {
  background: #ff3742;
}
.main-content {
  flex: 1;
  padding: 32px;
  overflow: auto;
}
</style>