<template>
  <div class="layout">
    <nav class="sidebar">
      <router-link to="/task-manager" class="nav-link">学习任务管理</router-link>
      <router-link to="/history" class="nav-link">历史学习记录</router-link>
      <router-link to="/data-integration" class="nav-link">数据整合</router-link>
      <router-link to="/plan-manager" class="nav-link">学习计划管理</router-link>
      <router-link to="/ai-analysis" class="nav-link">AI分析</router-link>
    </nav>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
// 这里可以写逻辑
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
.nav-link {
  padding: 16px 32px;
  color: #2a4d69;
  text-decoration: none;
  font-weight: 600;
  transition: background 0.2s, color 0.2s;
}
.nav-link.router-link-active {
  background: #e3ecfa;
  color: #1976d2;
}
.main-content {
  flex: 1;
  padding: 32px;
  overflow: auto;
}
</style>