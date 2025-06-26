// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  optimizeDeps: {  // 注意：这个配置必须放在 defineConfig 内部
    include: ['chart.js', 'vue-chartjs']
  }
})