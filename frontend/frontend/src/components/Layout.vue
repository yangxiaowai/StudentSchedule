<template>
  <div class="layout">
    <nav class="sidebar">
      <div class="nav-links">
        <router-link to="/task-manager" class="nav-link" active-class="nav-link-active">
          <span class="icon">📋</span> 学习任务管理
        </router-link>
        <router-link to="/history" class="nav-link" active-class="nav-link-active">
          <span class="icon">📖</span> 历史学习记录
        </router-link>
        <router-link to="/data-integration" class="nav-link" active-class="nav-link-active">
          <span class="icon">🔍</span> 资料检索库
        </router-link>
        <router-link to="/plan-manager" class="nav-link" active-class="nav-link-active">
          <span class="icon">🗂️</span> 学习计划管理
        </router-link>
        <router-link to="/ai-analysis" class="nav-link" active-class="nav-link-active">
          <span class="icon">🤖</span> AI分析
        </router-link>
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
  background: #f4f8fb;
}
.sidebar {
  width: 220px;
  background: #fff;
  display: flex;
  flex-direction: column;
  padding: 0;
  box-shadow: 2px 0 16px #e3ecfa;
  border-radius: 0 18px 18px 0;
}
.nav-links {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 36px 0 0 0;
}
.nav-link {
  display: flex;
  align-items: center;
  padding: 13px 24px 13px 20px;
  margin: 0 12px;
  border-radius: 10px;
  color: #2a4d69;
  font-weight: 600;
  font-size: 16px;
  text-decoration: none;
  transition: 
    background 0.2s, 
    color 0.2s, 
    box-shadow 0.2s,
    transform 0.15s;
  position: relative;
  overflow: hidden;
}
.nav-link .icon {
  margin-right: 14px;
  font-size: 20px;
  transition: transform 0.2s;
}
.nav-link:hover {
  background: #e3ecfa;
  color: #1976d2;
  box-shadow: 0 2px 12px #e3ecfa;
  transform: translateX(4px) scale(1.03);
}
.nav-link-active,
.nav-link.router-link-active {
  background: linear-gradient(90deg, #e3ecfa 60%, #f4f8fb 100%);
  color: #1976d2;
  box-shadow: 0 2px 16px #e3ecfa;
}
.nav-link-active::before,
.nav-link.router-link-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 4px;
  border-radius: 4px;
  background: #1976d2;
}
.user-section {
  padding: 22px 24px 26px 24px;
  border-top: 1px solid #e3ecfa;
  margin-top: auto;
  background: #fff;
  border-radius: 0 0 18px 0;
  box-shadow: 0 -2px 8px #e3ecfa;
}
.user-info {
  margin-bottom: 14px;
  text-align: center;
}
.username {
  color: #2a4d69;
  font-weight: 700;
  font-size: 15px;
  letter-spacing: 1px;
}
.logout-btn {
  width: 100%;
  padding: 10px 0;
  background: linear-gradient(90deg, #ff6b81 60%, #ff4757 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  font-size: 15px;
  transition: background 0.2s, box-shadow 0.2s, transform 0.15s;
  box-shadow: 0 2px 8px #ffd6db;
}
.logout-btn:hover {
  background: linear-gradient(90deg, #ff4757 60%, #ff6b81 100%);
  box-shadow: 0 4px 16px #ffd6db;
  transform: scale(1.04);
}
.main-content {
  flex: 1;
  padding: 36px 40px;
  overflow: auto;
  background: #f4f8fb;
  border-radius: 18px 0 0 18px;
}
</style>
