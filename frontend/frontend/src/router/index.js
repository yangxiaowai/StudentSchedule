import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'
import TaskManager from '../components/TaskManager.vue'
import History from '../components/History.vue'
import DataIntegration from '../components/DataIntegration.vue'
import PlanManager from '../components/PlanManager.vue'
import AIAnalysis from '../components/AIAnalysis.vue'
import Login from '../components/Login.vue'

const routes = [
  { path: '/login', component: Login },
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

// 路由守卫
router.beforeEach((to, from, next) => {
  const isLogin = localStorage.getItem('isLogin') === '1'
  if (to.path !== '/login' && !isLogin) {
    next('/login')
  } else if (to.path === '/login' && isLogin) {
    next('/data-integration')
  } else {
    next()
  }
})

export default router
