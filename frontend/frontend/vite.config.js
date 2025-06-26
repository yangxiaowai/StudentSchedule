import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 代理所有以/api开头的请求
      '/api': {
        target: 'http://localhost:8080', // 你的后端服务器地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
        secure: false
      }
    }
  }
})
