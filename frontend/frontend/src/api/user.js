// 用户相关API
const API_BASE_URL = 'http://localhost:8080/api/user'

// 通用请求函数
const request = async (url, options = {}) => {
  const token = localStorage.getItem('accessToken')
  
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` })
    },
    ...options
  }
  
  try {
    const response = await fetch(url, config)
    const data = await response.json()
    
    // 如果token过期，尝试刷新
    if (response.status === 401 && token) {
      const refreshResult = await refreshToken()
      if (refreshResult.success) {
        // 重新发送原请求
        config.headers.Authorization = `Bearer ${localStorage.getItem('accessToken')}`
        const retryResponse = await fetch(url, config)
        return await retryResponse.json()
      } else {
        // 刷新失败，跳转到登录页
        logout()
        window.location.href = '/login'
        return { code: 401, message: '登录已过期，请重新登录', data: null }
      }
    }
    
    return data
  } catch (error) {
    console.error('Request failed:', error)
    return { code: 500, message: '网络请求失败', data: null }
  }
}

// 用户注册
export const register = async (userData) => {
  const result = await request(`${API_BASE_URL}/register`, {
    method: 'POST',
    body: JSON.stringify(userData)
  })
  
  // 将后端的Result格式转换为前端期望的格式
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

// 用户登录
export const login = async (credentials) => {
  const result = await request(`${API_BASE_URL}/login`, {
    method: 'POST',
    body: JSON.stringify(credentials)
  })
  
  // 将后端的Result格式转换为前端期望的格式
  const response = {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
  
  if (response.success && response.data) {
    // 保存token和用户信息
    localStorage.setItem('accessToken', response.data.accessToken)
    localStorage.setItem('refreshToken', response.data.refreshToken)
    localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
    localStorage.setItem('isLogin', '1')
  }
  
  return response
}

// 刷新Token
export const refreshToken = async () => {
  const refreshTokenValue = localStorage.getItem('refreshToken')
  if (!refreshTokenValue) {
    return { success: false, message: '无刷新令牌' }
  }
  
  const result = await request(`${API_BASE_URL}/refresh`, {
    method: 'POST',
    body: JSON.stringify({ refreshToken: refreshTokenValue })
  })
  
  // 将后端的Result格式转换为前端期望的格式
  const response = {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
  
  if (response.success && response.data) {
    localStorage.setItem('accessToken', response.data.accessToken)
    localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
  }
  
  return response
}

// 获取当前用户信息
export const getCurrentUser = async () => {
  const result = await request(`${API_BASE_URL}/profile`)
  
  // 将后端的Result格式转换为前端期望的格式
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

// 发送密码重置邮件
export const forgotPassword = async (email) => {
  const result = await request(`${API_BASE_URL}/forgot-password`, {
    method: 'POST',
    body: JSON.stringify({ email })
  })
  
  // 将后端的Result格式转换为前端期望的格式
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

// 重置密码
export const resetPassword = async (token, newPassword) => {
  const result = await request(`${API_BASE_URL}/reset-password`, {
    method: 'POST',
    body: JSON.stringify({ token, newPassword })
  })
  
  // 将后端的Result格式转换为前端期望的格式
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

// 验证密码重置令牌
export const validateResetToken = async (token) => {
  const result = await request(`${API_BASE_URL}/validate-reset-token?token=${encodeURIComponent(token)}`)
  
  // 将后端的Result格式转换为前端期望的格式
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

// 用户登出
export const logout = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('isLogin')
}

// 检查登录状态
export const isLoggedIn = () => {
  return localStorage.getItem('isLogin') === '1' && localStorage.getItem('accessToken')
}

// 获取存储的用户信息
export const getStoredUserInfo = () => {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo) : null
}