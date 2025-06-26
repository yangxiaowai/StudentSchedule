import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { userStore } from './store/user.js'

// 引入Font Awesome
import '@fortawesome/fontawesome-free/css/all.css'

const app = createApp(App)

app.use(router)

// 初始化用户状态
userStore.init()

app.mount('#app')
