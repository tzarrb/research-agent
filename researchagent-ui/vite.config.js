import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  devServer: {
    client: {
      // 将浏览器控制台日志输出到 Node 终端（支持 'log' | 'info' | 'warn' | 'error' | 'none'）
      logging: 'verbose', // 输出所有日志（包括 console.log）
    }
  },
})
