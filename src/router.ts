// 第一步: 引入 createRouter
import {createRouter, createWebHistory} from 'vue-router'


// 第二步: 创建路由器
const router = createRouter({
    history: createWebHistory(), // 告诉路由器, 使用 history 模式
    routes: [ // 告诉路由器, 要呈现的组件
        {
            path: '/login',
            component: () => import('./components/Login.vue') // 动态导入 (懒加载), 提高性能
        },
        {
            path: '/backend',
            component: () => import('./components/Container.vue'),
            redirect: '/employee',
            children: [
                {
                    path: '/employee',
                    component: () => import('./components/employee/Employee.vue')
                },
                {
                    path: '/employee/add', // 修改和增加员工共用一个界面
                    component: () => import('./components/employee/EmployeeTable.vue')
                },
                {
                    path: '/category',
                    component: () => import('./components/Category.vue')
                },
                {
                    path: '/dish',
                    component: () => import('./components/dish/Dish.vue')
                },
                {
                    path: '/dish/add',
                    component: () => import('./components/dish/DishTable.vue')
                },
                {
                    path: '/setMeal',
                    component: () => import('./components/setMeal/SetMeal.vue')
                },
                {
                    path: '/setMeal/add',
                    component: () => import('./components/setMeal/SetMealTable.vue')
                }
            ]
        }
    ]
})

export default router // 将 router 暴露出去