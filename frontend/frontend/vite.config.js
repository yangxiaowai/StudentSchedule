import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  optimizeDeps: {  // 注意：这个配置必须放在 defineConfig 内部
    include: ['chart.js', 'vue-chartjs']
  },
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
