import { reactive } from 'vue'
import { login as apiLogin, logout as apiLogout, getCurrentUser, getStoredUserInfo } from '../api/user.js'

// 用户状态管理
export const userStore = reactive({
  // 状态
  isLoggedIn: false,
  userInfo: null,
  loading: false,
  error: null,

  // 初始化用户状态
  init() {
    const storedUserInfo = getStoredUserInfo()
    const isLogin = localStorage.getItem('isLogin') === '1'
    const accessToken = localStorage.getItem('accessToken')
    
    if (isLogin && accessToken && storedUserInfo) {
      this.isLoggedIn = true
      this.userInfo = storedUserInfo
    }
  },

  // 登录
  async login(credentials) {
    this.loading = true
    this.error = null
    
    try {
      const result = await apiLogin(credentials)
      
      if (result.success) {
        this.isLoggedIn = true
        this.userInfo = result.data.userInfo
        
        // 保存登录信息到localStorage
        localStorage.setItem('isLogin', '1')
        localStorage.setItem('accessToken', result.data.accessToken)
        localStorage.setItem('refreshToken', result.data.refreshToken)
        localStorage.setItem('userInfo', JSON.stringify(result.data.userInfo))
        
        return { success: true }
      } else {
        this.error = result.message || '登录失败'
        return { success: false, message: this.error }
      }
    } catch (error) {
      this.error = '网络错误，请稍后重试'
      return { success: false, message: this.error }
    } finally {
      this.loading = false
    }
  },

  // 登出
  logout() {
    apiLogout()
    this.isLoggedIn = false
    this.userInfo = null
    this.error = null
    // 清除localStorage中的登录状态
    localStorage.removeItem('isLogin')
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  },

  // 刷新用户信息
  async refreshUserInfo() {
    if (!this.isLoggedIn) return
    
    try {
      const result = await getCurrentUser()
      if (result.success) {
        this.userInfo = result.data
        localStorage.setItem('userInfo', JSON.stringify(result.data))
      }
    } catch (error) {
      console.error('刷新用户信息失败:', error)
    }
  },

  // 清除错误
  clearError() {
    this.error = null
  }
})

// 初始化用户状态
userStore.init()