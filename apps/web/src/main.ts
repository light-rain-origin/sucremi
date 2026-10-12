import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/tokens.css'

import App from './App.vue'
import router from './router'
import { useSkinStore } from './stores/skin'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

/*
 * 挂载前恢复已保存的皮肤。默认皮肤直接由 :root 提供；
 * 选过其它皮肤的用户需要先取到对应样式表再落 data-skin，否则会先闪一下默认皮肤。
 * restore() 自身对存储异常与加载失败都是安全的，因此无论如何都会继续挂载。
 */
useSkinStore()
  .restore()
  .finally(() => app.mount('#app'))
