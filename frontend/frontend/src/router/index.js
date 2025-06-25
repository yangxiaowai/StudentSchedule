import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'
import TaskManager from '../components/TaskManager.vue'
import History from '../components/History.vue'
import DataIntegration from '../components/DataIntegration.vue'
import PlanManager from '../components/PlanManager.vue'
import AIAnalysis from '../components/AIAnalysis.vue'
import Login from '../pages/Login.vue'
import Register from '../pages/Register.vue'
import ForgotPassword from '../pages/ForgotPassword.vue'
import ResetPassword from '../pages/ResetPassword.vue'

const routes = [
  { path: '/login', component: Login },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: ResetPassword
  },
  {
    path: '/',
    component: Layout,
    children: [
      { path: '', redirect: '/data-integration' },
      { path: 'task-manager', component: TaskManager },
      { path: 'history', component: History },
      { path: 'data-integration', component: DataIntegration },
      { path: 'plan-manager', component: PlanManager },
      { path: 'ai-analysis', component: AIAnalysis }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 检查token有效性
const isTokenValid = () => {
  const token = localStorage.getItem('accessToken')
  const isLogin = localStorage.getItem('isLogin') === '1'
  
  if (!token || !isLogin) {
    return false
  }
  
  try {
    // 简单检查token格式（JWT通常有三个部分用.分隔）
    const parts = token.split('.')
    if (parts.length !== 3) {
      return false
    }
    
    // 解析token payload检查过期时间
    const payload = JSON.parse(atob(parts[1]))
    const currentTime = Math.floor(Date.now() / 1000)
    
    // 如果token已过期
    if (payload.exp && payload.exp < currentTime) {
      // 清除过期的登录状态
      localStorage.removeItem('isLogin')
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      return false
    }
    
    return true
  } catch (error) {
    console.error('Token验证失败:', error)
    // 如果token格式错误，清除登录状态
    localStorage.removeItem('isLogin')
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    return false
  }
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const isLogin = isTokenValid()
  
  console.log('路由守卫 - 从:', from.path, '到:', to.path)
  console.log('路由守卫 - 登录状态:', isLogin)
  
  // 如果用户已登录且访问登录相关页面，重定向到主页
  if (isLogin && ['/login', '/register', '/forgot-password'].includes(to.path)) {
    console.log('路由守卫 - 已登录用户访问登录页，重定向到/data-integration')
    next('/data-integration')
    return
  }
  
  // 如果用户未登录且访问需要登录的页面，重定向到登录页
  if (!isLogin && !['/login', '/register', '/forgot-password', '/reset-password'].includes(to.path)) {
    console.log('路由守卫 - 未登录用户访问受保护页面，重定向到/login')
    next('/login')
    return
  }
  
  console.log('路由守卫 - 允许访问:', to.path)
  next()
})

export default router