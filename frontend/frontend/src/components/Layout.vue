<template>
  <div class="layout">
    <div class="video-bg">
      <video autoplay muted loop playsinline>
        <source src="/starfield.mp4" type="video/mp4" />
      </video>
      <div class="video-bg-overlay"></div>
    </div>
    <!-- 顶部透明导航栏 -->
    <header class="top-navbar">
      <div class="logo-area">
        <!-- 简约LOGO，可用SVG或文字 -->
        <div class="logo">
          <svg width="38" height="38" viewBox="0 0 38 38">
            <circle cx="19" cy="19" r="18" fill="url(#grad)" />
            <text x="50%" y="56%" text-anchor="middle" fill="#fff" font-size="18" font-family="Urbanist, Segoe UI, Arial" font-weight="bold" dy=".3em">SSA</text>
            <defs>
              <linearGradient id="grad" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#38cfd9"/>
                <stop offset="100%" stop-color="#1976d2"/>
              </linearGradient>
            </defs>
          </svg>
          <span class="logo-text">StudySys</span>
        </div>
      </div>
      <nav class="top-nav-links">
        <router-link to="/task-manager" class="top-nav-link" active-class="top-nav-link-active">Tasks</router-link>
        <router-link to="/history" class="top-nav-link" active-class="top-nav-link-active">History</router-link>
        <router-link to="/data-integration" class="top-nav-link" active-class="top-nav-link-active">Resources</router-link>
        <router-link to="/plan-manager" class="top-nav-link" active-class="top-nav-link-active">Plans</router-link>
        <router-link to="/ai-analysis" class="top-nav-link" active-class="top-nav-link-active">AI Analysis</router-link>
      </nav>
      <div class="user-mini">
        <span class="username">{{ userStore.userInfo?.username || 'User' }}</span>
        <button @click="handleLogout" class="logout-btn-mini">Logout</button>
      </div>
    </header>
    <!-- 主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
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
  min-height: 100vh;
  background: transparent; /* 改为完全透明，显示视频背景 */
}
.video-bg {
  position: fixed;
  top: 0; left: 0; width: 100vw; height: 100vh;
  z-index: 0; /* 保持在背景层 */
}
.video-bg video {
  min-width: 100vw;
  min-height: 100vh;
  object-fit: cover;
  width: 100vw;
  height: 100vh;
  position: absolute;
  top: 0; left: 0;
  z-index: 0;
}
.video-bg-overlay {
  position: absolute;
  top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,32,0.25); /* 减轻遮罩，让背景更明显 */
  z-index: 1;
  pointer-events: none;
}
.top-navbar {
  position: fixed;
  top: 0; left: 0; right: 0;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255,255,255,0.75); /* 保持导航栏半透明 */
  box-shadow: 0 2px 16px rgba(120,180,255,0.15);
  backdrop-filter: blur(12px);
  z-index: 1000;
  padding: 0 36px;
}
.logo-area {
  display: flex;
  align-items: center;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo-text {
  font-family: 'Urbanist', 'Segoe UI', Arial, sans-serif;
  font-weight: 800;
  font-size: 1.35rem;
  letter-spacing: 2px;
  color: #1976d2;
  text-shadow: 0 2px 8px #e3f0ff;
}
.top-nav-links {
  display: flex;
  gap: 32px;
}
.top-nav-link {
  color: #1976d2;
  font-family: 'Urbanist', 'Segoe UI', Arial, sans-serif;
  font-weight: 600;
  font-size: 1.08rem;
  text-decoration: none;
  padding: 8px 0;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
  letter-spacing: 1px;
}
.top-nav-link:hover,
.top-nav-link-active,
.top-nav-link.router-link-active {
  color: #38cfd9;
  border-bottom: 2.5px solid #38cfd9;
}
.user-mini {
  display: flex;
  align-items: center;
  gap: 16px;
}
.username {
  color: #1976d2;
  font-weight: 700;
  font-size: 1rem;
  letter-spacing: 1px;
}
.logout-btn-mini {
  background: linear-gradient(90deg, #ff6b81 60%, #ff4757 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  font-size: 1rem;
  padding: 6px 18px;
  transition: background 0.2s, box-shadow 0.2s, transform 0.15s;
  box-shadow: 0 2px 8px #ffd6db;
}
.logout-btn-mini:hover {
  background: linear-gradient(90deg, #ff4757 60%, #ff6b81 100%);
  box-shadow: 0 4px 16px #ffd6db;
  transform: scale(1.04);
}
.main-content {
  margin-top: 64px;
  padding: 36px 40px;
  min-height: calc(100vh - 64px);
  background: transparent; /* 完全透明，显示背景 */
  position: relative;
  z-index: 10;
}
.content-wrapper {
  background: rgba(255, 255, 255, 0.45); /* 从0.65进一步降低到0.45 */
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(4px); /* 从6px降低到4px，进一步减少模糊 */
  border: 1px solid rgba(255, 255, 255, 0.25); /* 降低边框透明度 */
  min-height: calc(100vh - 160px);
  position: relative;
}
.content-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 16px;
  padding: 1px;
  background: linear-gradient(135deg, rgba(56, 207, 217, 0.15), rgba(25, 118, 210, 0.15)); /* 进一步降低边框渐变透明度 */
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: exclude;
  pointer-events: none;
}
@media (max-width: 900px) {
  .top-navbar { padding: 0 10px; }
  .top-nav-links { gap: 14px; }
  .main-content { padding: 18px 4vw; }
  .content-wrapper { 
    padding: 16px;
    background: rgba(255, 255, 255, 0.55); /* 移动端从0.7降低到0.55 */
  }
}
@media (max-width: 600px) {
  .top-navbar { flex-direction: column; height: auto; padding: 8px 2vw; gap: 8px;}
  .main-content { padding: 8px 2vw; margin-top: 80px; }
  .logo-text { font-size: 1rem; }
  .content-wrapper { 
    padding: 12px;
    background: rgba(255, 255, 255, 0.6); /* 小屏幕从0.75降低到0.6 */
  }
}
</style>
