/**
 * 用户相关API模块
 * 提供用户认证、注册、密码管理等功能的前端接口
 *
 * 主要功能：
 * - 用户登录和注册
 * - JWT令牌管理和自动刷新
 * - 密码重置和找回
 * - 邮箱验证码功能
 * - 用户信息管理
 * - 本地存储管理
 *
 * 特性：
 * - 自动处理令牌过期和刷新
 * - 统一的错误处理机制
 * - 标准化的响应格式
 * - 安全的本地存储管理
 *
 * @author 系统
 * @version 1.0
 * @since 2024
 */

// API基础URL
const API_BASE_URL = '/api/user'

/**
 * 通用HTTP请求函数
 * 封装了fetch API，提供统一的请求处理、错误处理和令牌管理
 *
 * @param {string} url - 请求URL
 * @param {Object} options - 请求配置选项
 * @returns {Promise<Object>} 响应数据对象
 *
 * 功能特性：
 * - 自动添加Authorization头部
 * - 自动处理令牌过期和刷新
 * - 统一的错误处理
 * - 自动重试机制
 */
const request = async (url, options = {}) => {
  // 从本地存储获取访问令牌
  const token = localStorage.getItem('accessToken')
  
  // 构建请求配置，包含默认头部和用户自定义选项
  const config = {
    headers: {
      'Content-Type': 'application/json',
      // 如果存在令牌，则添加Authorization头部
      ...(token && { Authorization: `Bearer ${token}` })
    },
    ...options
  }
  
  try {
    // 发送HTTP请求
    const response = await fetch(url, config)
    const data = await response.json()
    
    // 检查是否因令牌过期返回401状态码
    if (response.status === 401 && token) {
      // 尝试使用刷新令牌获取新的访问令牌
      const refreshResult = await refreshToken()
      if (refreshResult.success) {
        // 刷新成功，使用新令牌重新发送原请求
        config.headers.Authorization = `Bearer ${localStorage.getItem('accessToken')}`
        const retryResponse = await fetch(url, config)
        return await retryResponse.json()
      } else {
        // 刷新失败，清除本地数据并跳转到登录页
        logout()
        window.location.href = '/login'
        return { code: 401, message: '登录已过期，请重新登录', data: null }
      }
    }
    
    // 返回响应数据
    return data
  } catch (error) {
    // 捕获网络错误或其他异常
    console.error('Request failed:', error)
    return { code: 500, message: '网络请求失败', data: null }
  }
}

/**
 * 用户注册函数
 * 向后端发送用户注册信息，创建新用户账户
 *
 * @param {Object} userData - 用户注册数据
 * @param {string} userData.username - 用户名
 * @param {string} userData.password - 密码
 * @param {string} userData.confirmPassword - 确认密码
 * @param {string} userData.email - 邮箱地址
 * @returns {Promise<Object>} 注册结果对象
 * @returns {boolean} returns.success - 注册是否成功
 * @returns {string} returns.message - 响应消息
 * @returns {Object|null} returns.data - 响应数据
 *
 * 功能说明：
 * - 验证用户输入的注册信息
 * - 检查用户名和邮箱的唯一性
 * - 创建新用户账户
 * - 返回标准化的响应格式
 */
export const register = async (userData) => {
  const result = await request(`${API_BASE_URL}/register`, {
    method: 'POST',
    body: JSON.stringify(userData)
  })
  
  // 将后端的Result格式转换为前端期望的标准格式
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

/**
 * 用户登录函数
 * 验证用户凭据并获取访问令牌
 *
 * @param {Object} credentials - 登录凭据
 * @param {string} credentials.username - 用户名
 * @param {string} credentials.password - 密码
 * @returns {Promise<Object>} 登录结果对象
 * @returns {boolean} returns.success - 登录是否成功
 * @returns {string} returns.message - 响应消息
 * @returns {Object|null} returns.data - 登录数据（包含令牌和用户信息）
 *
 * 功能说明：
 * - 验证用户名和密码
 * - 获取访问令牌和刷新令牌
 * - 自动保存令牌和用户信息到本地存储
 * - 设置登录状态标识
 *
 * 本地存储项：
 * - accessToken: 访问令牌
 * - refreshToken: 刷新令牌
 * - userInfo: 用户信息JSON字符串
 * - isLogin: 登录状态标识
 */
export const login = async (credentials) => {
  const result = await request(`${API_BASE_URL}/login`, {
    method: 'POST',
    body: JSON.stringify(credentials)
  })
  
  // 将后端的Result格式转换为前端期望的标准格式
  const response = {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
  
  // 登录成功时保存令牌和用户信息到本地存储
  if (response.success && response.data) {
    localStorage.setItem('accessToken', response.data.accessToken)
    localStorage.setItem('refreshToken', response.data.refreshToken)
    localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
    localStorage.setItem('isLogin', '1')
  }
  
  return response
}

/**
 * 刷新访问令牌函数
 * 使用存储的刷新令牌获取新的访问令牌
 *
 * @returns {Promise<Object>} 刷新结果对象
 * @returns {boolean} returns.success - 刷新是否成功
 * @returns {string} returns.message - 响应消息
 * @returns {Object|null} returns.data - 刷新数据（包含新的访问令牌）
 *
 * 功能说明：
 * - 从本地存储获取刷新令牌
 * - 向后端发送刷新请求
 * - 更新本地存储中的访问令牌和用户信息
 * - 保持用户登录状态的连续性
 *
 * 使用场景：
 * - 访问令牌过期时自动调用
 * - 用户主动刷新令牌
 * - 应用启动时验证令牌有效性
 *
 * 注意事项：
 * - 如果没有刷新令牌，直接返回失败
 * - 刷新成功后会更新本地存储
 * - 刷新令牌本身不会更新，继续使用原有的
 */
export const refreshToken = async () => {
  // 从本地存储获取刷新令牌
  const refreshTokenValue = localStorage.getItem('refreshToken')
  if (!refreshTokenValue) {
    return { success: false, message: '无刷新令牌' }
  }
  
  // 向后端发送刷新令牌请求
  const result = await request(`${API_BASE_URL}/refresh`, {
    method: 'POST',
    body: JSON.stringify({ refreshToken: refreshTokenValue })
  })
  
  // 将后端的Result格式转换为前端期望的标准格式
  const response = {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
  
  // 刷新成功时更新本地存储中的访问令牌和用户信息
  if (response.success && response.data) {
    localStorage.setItem('accessToken', response.data.accessToken)
    localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
  }
  
  return response
}

/**
 * 获取当前用户信息函数
 * 从后端获取当前登录用户的详细信息
 *
 * @returns {Promise<Object>} 用户信息结果对象
 * @returns {boolean} returns.success - 获取是否成功
 * @returns {string} returns.message - 响应消息
 * @returns {Object|null} returns.data - 用户信息数据
 *
 * 功能说明：
 * - 使用当前访问令牌获取用户信息
 * - 返回用户的详细资料
 * - 可用于验证用户身份和权限
 *
 * 使用场景：
 * - 用户个人资料页面
 * - 权限验证
 * - 用户信息更新后的重新获取
 * - 应用初始化时获取用户状态
 *
 * 注意事项：
 * - 需要有效的访问令牌
 * - 如果令牌过期会自动触发刷新机制
 */
export const getCurrentUser = async () => {
  const result = await request(`${API_BASE_URL}/profile`)
  
  // 将后端的Result格式转换为前端期望的标准格式
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

/**
 * 发送密码重置邮件函数
 * 向指定邮箱发送包含密码重置链接的邮件
 *
 * @param {string} email - 用户邮箱地址
 * @returns {Promise<Object>} 发送结果对象
 * @returns {boolean} returns.success - 发送是否成功
 * @returns {string} returns.message - 响应消息
 * @returns {Object|null} returns.data - 响应数据
 *
 * 功能说明：
 * - 验证邮箱地址是否存在于系统中
 * - 生成密码重置令牌
 * - 发送包含重置链接的邮件
 * - 令牌有效期通常为30分钟
 *
 * 使用场景：
 * - 用户忘记密码时
 * - 密码找回流程的第一步
 * - 安全的密码重置机制
 *
 * 安全考虑：
 * - 不会泄露用户是否存在的信息
 * - 重置令牌有时间限制
 * - 一次性使用令牌
 */
export const forgotPassword = async (email) => {
  const result = await request(`${API_BASE_URL}/forgot-password`, {
    method: 'POST',
    body: JSON.stringify({ email })
  })
  
  // 将后端的Result格式转换为前端期望的标准格式
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

/**
 * 重置密码函数
 * 使用有效的重置令牌更新用户密码
 *
 * @param {string} token - 密码重置令牌
 * @param {string} newPassword - 新密码
 * @returns {Promise<Object>} 重置结果对象
 * @returns {boolean} returns.success - 重置是否成功
 * @returns {string} returns.message - 响应消息
 * @returns {Object|null} returns.data - 响应数据
 *
 * 功能说明：
 * - 验证重置令牌的有效性
 * - 检查令牌是否过期
 * - 更新用户密码
 * - 使令牌失效（一次性使用）
 *
 * 使用场景：
 * - 用户点击邮件中的重置链接后
 * - 密码找回流程的最后一步
 * - 通过令牌验证的安全密码更新
 *
 * 安全考虑：
 * - 令牌验证确保操作合法性
 * - 密码会进行加密存储
 * - 重置后令牌立即失效
 */
export const resetPassword = async (token, newPassword) => {
  const result = await request(`${API_BASE_URL}/reset-password`, {
    method: 'POST',
    body: JSON.stringify({ token, newPassword })
  })
  
  // 将后端的Result格式转换为前端期望的标准格式
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

/**
 * 验证密码重置令牌函数
 * 检查密码重置令牌是否有效且未过期
 *
 * @param {string} token - 密码重置令牌
 * @returns {Promise<Object>} 验证结果对象
 * @returns {boolean} returns.success - 令牌是否有效
 * @returns {string} returns.message - 响应消息
 * @returns {Object|null} returns.data - 验证数据
 *
 * 功能说明：
 * - 验证令牌格式的正确性
 * - 检查令牌是否已过期
 * - 确认令牌是否已被使用
 * - 验证令牌对应的用户是否存在
 *
 * 使用场景：
 * - 用户访问密码重置页面时
 * - 在显示重置表单前验证链接有效性
 * - 提供用户友好的错误提示
 *
 * 返回情况：
 * - 成功：令牌有效，可以进行密码重置
 * - 失败：令牌无效、过期或已使用
 */
export const validateResetToken = async (token) => {
  const result = await request(`${API_BASE_URL}/validate-reset-token?token=${encodeURIComponent(token)}`)
  
  // 将后端的Result格式转换为前端期望的标准格式
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

/**
 * 用户登出函数
 * 清除本地存储中的所有用户相关数据
 *
 * 功能说明：
 * - 清除访问令牌
 * - 清除刷新令牌
 * - 清除用户信息
 * - 清除登录状态标识
 *
 * 使用场景：
 * - 用户主动退出登录
 * - 令牌刷新失败时强制登出
 * - 安全退出清理敏感数据
 *
 * 注意事项：
 * - 仅清理本地数据，不向后端发送请求
 * - 调用后用户需要重新登录
 * - 建议在清理后跳转到登录页面
 */
export const logout = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('isLogin')
}

/**
 * 检查用户登录状态函数
 * 验证用户是否处于登录状态
 *
 * @returns {boolean} 用户是否已登录
 *
 * 功能说明：
 * - 检查登录状态标识
 * - 验证访问令牌是否存在
 * - 双重验证确保登录状态的准确性
 *
 * 使用场景：
 * - 路由守卫中验证用户权限
 * - 条件渲染登录/登出按钮
 * - 应用初始化时检查用户状态
 * - 页面访问权限控制
 *
 * 注意事项：
 * - 仅检查本地存储，不验证令牌有效性
 * - 实际API调用时可能仍需处理令牌过期
 */
export const isLoggedIn = () => {
  return localStorage.getItem('isLogin') === '1' && localStorage.getItem('accessToken')
}

/**
 * 获取存储的用户信息函数
 * 从本地存储中获取用户信息对象
 *
 * @returns {Object|null} 用户信息对象，如果不存在则返回null
 *
 * 功能说明：
 * - 从本地存储读取用户信息JSON字符串
 * - 解析JSON并返回用户信息对象
 * - 处理数据不存在或格式错误的情况
 *
 * 使用场景：
 * - 显示用户头像和基本信息
 * - 用户个人资料页面数据填充
 * - 权限判断和个性化设置
 * - 避免重复请求用户信息
 *
 * 返回数据结构：
 * - id: 用户ID
 * - username: 用户名
 * - email: 邮箱地址
 * - 其他用户相关信息
 */
export const getStoredUserInfo = () => {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 发送邮箱验证码函数
 * 向指定邮箱发送数字验证码
 *
 * @param {string} email - 目标邮箱地址
 * @returns {Promise<Object>} 发送结果对象
 * @returns {boolean} returns.success - 发送是否成功
 * @returns {string} returns.message - 响应消息
 * @returns {Object|null} returns.data - 响应数据
 *
 * 功能说明：
 * - 生成随机数字验证码
 * - 将验证码发送到指定邮箱
 * - 在服务端缓存验证码（通常5-10分钟有效期）
 * - 支持重复发送（有频率限制）
 *
 * 使用场景：
 * - 邮箱验证流程
 * - 基于验证码的密码重置
 * - 敏感操作的二次验证
 * - 用户邮箱绑定验证
 *
 * 安全考虑：
 * - 验证码有时间限制
 * - 发送频率限制防止滥用
 * - 验证码一次性使用
 */
export const sendVerificationCode = async (email) => {
  const result = await request(`${API_BASE_URL}/send-verification-code`, {
    method: 'POST',
    body: JSON.stringify({ email })
  })
  
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

/**
 * 验证邮箱验证码函数
 * 验证用户输入的验证码是否正确
 *
 * @param {string} email - 邮箱地址
 * @param {string} code - 验证码
 * @returns {Promise<Object>} 验证结果对象
 * @returns {boolean} returns.success - 验证是否成功
 * @returns {string} returns.message - 响应消息
 * @returns {Object|null} returns.data - 验证数据
 *
 * 功能说明：
 * - 验证邮箱和验证码的匹配性
 * - 检查验证码是否过期
 * - 验证成功后立即清除验证码
 * - 防止验证码重复使用
 *
 * 使用场景：
 * - 邮箱验证流程的确认步骤
 * - 密码重置前的身份验证
 * - 敏感操作的安全确认
 *
 * 验证规则：
 * - 验证码必须与邮箱匹配
 * - 验证码必须在有效期内
 * - 验证码只能使用一次
 */
export const verifyCode = async (email, code) => {
  const result = await request(`${API_BASE_URL}/verify-code`, {
    method: 'POST',
    body: JSON.stringify({ email, code })
  })
  
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}

/**
 * 基于验证码重置密码函数
 * 使用邮箱验证码直接重置用户密码
 *
 * @param {string} email - 用户邮箱地址
 * @param {string} code - 邮箱验证码
 * @param {string} newPassword - 新密码
 * @returns {Promise<Object>} 重置结果对象
 * @returns {boolean} returns.success - 重置是否成功
 * @returns {string} returns.message - 响应消息
 * @returns {Object|null} returns.data - 响应数据
 *
 * 功能说明：
 * - 验证邮箱验证码的有效性
 * - 确认用户身份后重置密码
 * - 对新密码进行加密存储
 * - 清除使用过的验证码
 *
 * 使用场景：
 * - 替代令牌方式的密码重置
 * - 更简便的密码找回流程
 * - 移动端友好的重置方式
 *
 * 与令牌重置的区别：
 * - 不需要点击邮件链接
 * - 直接输入验证码即可
 * - 流程更简化
 * - 安全性同样可靠
 *
 * 安全考虑：
 * - 验证码验证确保用户身份
 * - 密码加密存储
 * - 验证码使用后立即失效
 */
export const resetPasswordByCode = async (email, code, newPassword) => {
  const result = await request(`${API_BASE_URL}/reset-password-by-code`, {
    method: 'POST',
    body: JSON.stringify({ email, code, newPassword })
  })
  
  return {
    success: result.code === 200,
    message: result.message,
    data: result.data
  }
}