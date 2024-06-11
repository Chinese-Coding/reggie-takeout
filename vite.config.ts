import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import {ElementPlusResolver} from "unplugin-vue-components/resolvers";
import {VantResolver} from "@vant/auto-import-resolver";
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {fileURLToPath} from 'url';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        AutoImport({
            imports: ['vue'],
            resolvers: [ElementPlusResolver(), VantResolver()],
        }),
        Components({
            resolvers: [ElementPlusResolver({importStyle: "sass"}), VantResolver()],
        }),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    // 配置代理, 解决跨域问题, 配置后, 不必再在脚本内部写 `http://localhost:8080`, 不过需要在具体的请求链接之前加上 `/api`
    server: {
        host: '0.0.0.0',
        port: 5174,
        proxy: {
            '/api': {
                // target: 'http://123.249.11.155:8080', // 目标请求地址
                target: 'http://localhost:8080', // 目标请求地址
                changeOrigin: true, //允许跨域
                rewrite: (path) => path.replace(/^\/api/, '') // 去除请求中的 `/api`
            }
        }
    }
})
