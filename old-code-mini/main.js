import { createSSRApp } from 'vue'
import App from './App'
import store from './store'
import { install } from './plugins'
import './permission'

export function createApp() {
  const app = createSSRApp(App)
  app.use(store)
  install(app)
  return {
    app
  }
}
