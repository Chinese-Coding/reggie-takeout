// 第一步: 引入 createRouter
import {createRouter, createWebHistory} from 'vue-router'


// 第二步: 创建路由器
const router = createRouter({
    history: createWebHistory(), // 告诉路由器, 使用 history 模式
    routes: [ // 告诉路由器, 要呈现的组件
        {
            path: '/index',
            component: () => import('./components/Main.vue')
        },
        {
            path: '/login',
            component: () => import('./components/Login.vue') // 动态导入 (懒加载), 提高性能
        },
        {
            path: '/addOrder',
            component: () => import('./components/AddOrder.vue')
        },
        {
            path: '/pay-success',
            component: () => import('./components/PaySuccess.vue')
        },
        {
            path: '/address',
            component: () => import('./components/Address.vue')
        },
        {
            path: '/address-edit',
            component: () => import('./components/AddressEdit.vue')
        },
        {
            path: '/order',
            component: () => import('./components/Order.vue')
        },
        {
            path: '/user',
            component: () => import('./components/User.vue')
        }

    ]
})

export default router // 将 router 暴露出去