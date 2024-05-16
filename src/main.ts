import {createApp} from 'vue'
import './style.css'
import App from './App.vue'
import router from "./router.ts";
import 'vant/lib/index.css';
import 'element-plus/dist/index.css'
import 'vant/es/notify/style'
import 'vant/es/toast/style'
import {Dialog} from "vant"

createApp(App).use(router).use(Dialog).mount('#app')
