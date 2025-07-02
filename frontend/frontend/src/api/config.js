import axios from 'axios'

// 设置基础URL
axios.defaults.baseURL = 'http://localhost:8080'

// 添加请求拦截器
axios.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加CORS相关头
    config.headers['Content-Type'] = 'application/json'
    config.headers['Access-Control-Allow-Origin'] = '*'
    
    console.log('发送请求:', config.url, config.data)
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 添加响应拦截器
axios.interceptors.response.use(
  response => {
    console.log('收到响应:', response.config.url, response.data)
    return response
  },
  error => {
    console.error('API请求错误:', error)
    if (error.response) {
      console.error('错误状态码:', error.response.status)
      console.error('错误数据:', error.response.data)
    } else if (error.request) {
      console.error('未收到响应:', error.request)
    } else {
      console.error('请求配置错误:', error.message)
    }
    return Promise.reject(error)
  }
)

export default axios