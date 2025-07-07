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
        target: 'https://localhost:8443', // HTTPS后端服务器地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
        secure: false, // 开发环境允许自签名证书
        configure: (proxy, options) => {
          // 忽略SSL证书验证（仅开发环境）
          proxy.on('error', (err, req, res) => {
            console.log('proxy error', err);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('Sending Request to the Target:', req.method, req.url);
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('Received Response from the Target:', proxyRes.statusCode, req.url);
          });
        }
      }
    }
  }
})
