// HTTP请求工具类 - 统一的请求处理
import { API_CONFIG } from '../config/api.js'

// 请求状态枚举
export const REQUEST_STATUS = {
  PENDING: 'pending',
  SUCCESS: 'success',
  ERROR: 'error',
  TIMEOUT: 'timeout'
}

// 错误类型枚举
export const ERROR_TYPES = {
  NETWORK: 'network',
  TIMEOUT: 'timeout',
  AUTH: 'auth',
  SERVER: 'server',
  VALIDATION: 'validation'
}

// HTTP状态码映射
const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500
}

class HttpClient {
  constructor() {
    this.requestInterceptors = []
    this.responseInterceptors = []
    this.errorHandlers = new Map()
  }

  // 添加请求拦截器
  addRequestInterceptor(interceptor) {
    this.requestInterceptors.push(interceptor)
  }

  // 添加响应拦截器
  addResponseInterceptor(interceptor) {
    this.responseInterceptors.push(interceptor)
  }

  // 添加错误处理器
  addErrorHandler(errorType, handler) {
    this.errorHandlers.set(errorType, handler)
  }

  // 获取认证头
  getAuthHeaders() {
    const token = localStorage.getItem('accessToken')
    return token ? { Authorization: `Bearer ${token}` } : {}
  }

  // 处理请求拦截器
  async processRequestInterceptors(config) {
    let processedConfig = { ...config }
    
    for (const interceptor of this.requestInterceptors) {
      try {
        processedConfig = await interceptor(processedConfig)
      } catch (error) {
        console.error('Request interceptor error:', error)
      }
    }
    
    return processedConfig
  }

  // 处理响应拦截器
  async processResponseInterceptors(response) {
    let processedResponse = response
    
    for (const interceptor of this.responseInterceptors) {
      try {
        processedResponse = await interceptor(processedResponse)
      } catch (error) {
        console.error('Response interceptor error:', error)
      }
    }
    
    return processedResponse
  }

  // 处理错误
  handleError(error, errorType) {
    const handler = this.errorHandlers.get(errorType)
    if (handler) {
      return handler(error)
    }
    
    // 默认错误处理
    console.error(`${errorType} error:`, error)
    return {
      success: false,
      error: errorType,
      message: this.getErrorMessage(error, errorType),
      data: null
    }
  }

  // 获取错误消息
  getErrorMessage(error, errorType) {
    switch (errorType) {
      case ERROR_TYPES.NETWORK:
        return '网络连接失败，请检查网络设置'
      case ERROR_TYPES.TIMEOUT:
        return '请求超时，请稍后重试'
      case ERROR_TYPES.AUTH:
        return '认证失败，请重新登录'
      case ERROR_TYPES.SERVER:
        return '服务器错误，请稍后重试'
      case ERROR_TYPES.VALIDATION:
        return '数据验证失败'
      default:
        return error.message || '未知错误'
    }
  }

  // 延迟函数
  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }

  // 带重试的请求
  async requestWithRetry(url, options, retryCount = API_CONFIG.RETRY_COUNT) {
    for (let i = 0; i <= retryCount; i++) {
      try {
        const response = await this.makeRequest(url, options)
        return response
      } catch (error) {
        if (i === retryCount) {
          throw error
        }
        
        // 指数退避重试
        const delayMs = Math.pow(2, i) * 1000
        console.warn(`Request failed, retrying in ${delayMs}ms... (${i + 1}/${retryCount})`, error)
        await this.delay(delayMs)
      }
    }
  }

  // 核心请求方法
  async makeRequest(url, options) {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), API_CONFIG.TIMEOUT)

    try {
      // 构建请求配置
      const config = {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...this.getAuthHeaders()
        },
        signal: controller.signal,
        ...options
      }

      // 处理请求拦截器
      const processedConfig = await this.processRequestInterceptors(config)

      // 发送请求
      const response = await fetch(url, processedConfig)
      clearTimeout(timeoutId)

      // 检查响应状态
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      // 解析响应数据
      const data = await response.json()
      
      // 处理响应拦截器
      const processedResponse = await this.processResponseInterceptors({
        data,
        status: response.status,
        headers: response.headers,
        config: processedConfig
      })

      return {
        success: true,
        data: processedResponse.data,
        status: processedResponse.status,
        message: 'Request successful'
      }

    } catch (error) {
      clearTimeout(timeoutId)
      
      if (error.name === 'AbortError') {
        return this.handleError(error, ERROR_TYPES.TIMEOUT)
      }
      
      if (!navigator.onLine) {
        return this.handleError(error, ERROR_TYPES.NETWORK)
      }
      
      if (error.message.includes('401')) {
        return this.handleError(error, ERROR_TYPES.AUTH)
      }
      
      if (error.message.includes('5')) {
        return this.handleError(error, ERROR_TYPES.SERVER)
      }
      
      return this.handleError(error, ERROR_TYPES.NETWORK)
    }
  }

  // GET请求
  async get(url, params = {}) {
    const queryString = new URLSearchParams(params).toString()
    const fullUrl = queryString ? `${url}?${queryString}` : url
    
    return this.requestWithRetry(fullUrl, { method: 'GET' })
  }

  // POST请求
  async post(url, data = {}) {
    return this.requestWithRetry(url, {
      method: 'POST',
      body: JSON.stringify(data)
    })
  }

  // PUT请求
  async put(url, data = {}) {
    return this.requestWithRetry(url, {
      method: 'PUT',
      body: JSON.stringify(data)
    })
  }

  // DELETE请求
  async delete(url) {
    return this.requestWithRetry(url, { method: 'DELETE' })
  }

  // 文件上传
  async upload(url, formData) {
    return this.requestWithRetry(url, {
      method: 'POST',
      body: formData,
      headers: {
        // 不设置Content-Type，让浏览器自动设置multipart/form-data
        ...this.getAuthHeaders()
      }
    })
  }
}

// 创建默认实例
const httpClient = new HttpClient()

// 添加默认的请求拦截器
httpClient.addRequestInterceptor((config) => {
  console.log('发送请求:', config.method, config.url || 'Unknown URL')
  return config
})

// 添加默认的响应拦截器
httpClient.addResponseInterceptor((response) => {
  console.log('收到响应:', response.status, response.data)
  return response
})

// 添加认证错误处理器
httpClient.addErrorHandler(ERROR_TYPES.AUTH, (error) => {
  // 清除本地存储的认证信息
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('isLogin')
  
  // 跳转到登录页
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
  
  return {
    success: false,
    error: ERROR_TYPES.AUTH,
    message: '登录已过期，请重新登录',
    data: null
  }
})

export default httpClient
export { HttpClient }