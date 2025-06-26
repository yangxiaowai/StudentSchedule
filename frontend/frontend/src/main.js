import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'

// 引入Font Awesome
import '@fortawesome/fontawesome-free/css/all.css'

const app = createApp(App)
app.use(router)
app.mount('#app')

createApp(App).use(router).mount('#app')
