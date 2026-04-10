import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { formatCurrency } from './utils/format'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.config.globalProperties.$formatCurrency = formatCurrency
app.config.globalProperties.$filters = { currency: formatCurrency }

app.mount('#app')
