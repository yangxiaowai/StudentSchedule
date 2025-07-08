// API配置文件 - 统一管理所有API端点

// 基础配置
export const API_CONFIG = {
  BASE_URL: 'https://localhost:8443',
  TIMEOUT: 10000,
  RETRY_COUNT: 3
}

// API端点配置
export const API_ENDPOINTS = {
  // 用户相关
  USER: {
    BASE: '/api/user',
    LOGIN: '/api/user/login',
    REGISTER: '/api/user/register',
    LOGOUT: '/api/user/logout',
    PROFILE: '/api/user/profile',
    REFRESH_TOKEN: '/api/user/refresh',
    FORGOT_PASSWORD: '/api/user/forgot-password',
    RESET_PASSWORD: '/api/user/reset-password',
    VALIDATE_RESET_TOKEN: '/api/user/validate-reset-token',
    SEND_VERIFICATION_CODE: '/api/user/send-verification-code',
    VERIFY_CODE: '/api/user/verify-code'
  },
  
  // 任务相关
  TASK: {
    BASE: '/api/tasks',
    LIST: '/api/tasks',
    CREATE: '/api/tasks',
    UPDATE: (id) => `/api/tasks/${id}`,
    DELETE: (id) => `/api/tasks/${id}`,
    DETAIL: (id) => `/api/tasks/${id}`,
    PROGRESS: (id) => `/api/tasks/${id}/progress`
  },
  
  // 文件相关
  FILE: {
    BASE: '/api/files',
    UPLOAD: '/api/files/upload',
    LIST: '/api/files',
    DELETE: (fileName) => `/api/files/${encodeURIComponent(fileName)}`,
    DOWNLOAD: (fileName) => `/api/files/download/${encodeURIComponent(fileName)}`,
    PREVIEW: (fileName) => `/api/files/preview/${encodeURIComponent(fileName)}`,
    USER_FILES: '/api/files/user'
  },
  
  // 文件预览相关
  PREVIEW: {
    BASE: '/api/preview',
    BY_NAME: (fileName) => `/api/preview/${encodeURIComponent(fileName)}`,
    BY_ID: (id) => `/api/preview/file/${id}`
  },
  
  // AI搜索相关
  AI: {
    BASE: '/api/ai',
    SEARCH: '/api/ai/search'
  }
}

// 构建完整URL的工具函数
export const buildUrl = (endpoint) => {
  if (typeof endpoint === 'function') {
    throw new Error('Endpoint is a function, please call it with required parameters')
  }
  return `${API_CONFIG.BASE_URL}${endpoint}`
}

// 获取API端点的工具函数
export const getApiUrl = (category, action, ...params) => {
  const endpoint = API_ENDPOINTS[category]?.[action]
  if (!endpoint) {
    throw new Error(`API endpoint not found: ${category}.${action}`)
  }
  
  if (typeof endpoint === 'function') {
    return buildUrl(endpoint(...params))
  }
  
  return buildUrl(endpoint)
}

// 导出常用的完整URL
export const API_URLS = {
  // 用户
  USER_LOGIN: buildUrl(API_ENDPOINTS.USER.LOGIN),
  USER_REGISTER: buildUrl(API_ENDPOINTS.USER.REGISTER),
  USER_PROFILE: buildUrl(API_ENDPOINTS.USER.PROFILE),
  
  // 任务
  TASK_LIST: buildUrl(API_ENDPOINTS.TASK.LIST),
  TASK_CREATE: buildUrl(API_ENDPOINTS.TASK.CREATE),
  
  // 文件
  FILE_UPLOAD: buildUrl(API_ENDPOINTS.FILE.UPLOAD),
  FILE_LIST: buildUrl(API_ENDPOINTS.FILE.LIST),
  
  // AI搜索
  AI_SEARCH: buildUrl(API_ENDPOINTS.AI.SEARCH)
}