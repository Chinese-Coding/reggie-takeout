<script setup lang="ts">
import axiosRequest from "@/utils/axiosRequest.ts";
import router from "@/router.ts";
import {UserFilled, Menu, Dish, Present, Tickets} from "@element-plus/icons-vue";
import {goBack, goBackFlag, headTitle, reloadHeader} from "@/ts/container.ts";
import {onMounted, ref} from "vue";

interface Menu {
  id: string,
  name: string,
  url: string,
  icon: any
}

let activePage = ref(''); // 记录侧边栏当前激活的页面
let username = ref(''); // 也许这样写并不对
let menuList = [
  {
    id: '1',
    name: '员工管理',
    url: '/employee',
    icon: UserFilled
  },
  {
    id: '2',
    name: '分类管理',
    url: '/category',
    icon: Menu
  },
  {
    id: '3',
    name: '菜品管理',
    url: '/dish',
    icon: Dish
  },
  {
    id: '4',
    name: '套餐管理',
    url: '/setMeal',
    icon: Present
  },
  {
    id: '5',
    name: '订单明细',
    url: 'page/order/list.html',
    icon: Tickets
  }
];
let loading = ref(true);
let timer = ref(null);

onMounted(() => {
  let userInfoValue = window.localStorage.getItem('userInfo');
  if (userInfoValue)
    username.value = JSON.parse(userInfoValue).name;
  let activePageValue = window.localStorage.getItem('activePage');
  activePage.value = activePageValue || 1;

  timer.value = setTimeout(() => {
    loading.value = false
  }, 100)
});

/**
 * 登出函数, 清除缓存中的信息
 */
function logout() {
  let r = axiosRequest({
    'url': '/employee/logout',
    'method': 'post',
  })
  if (r.code === 1) {
    localStorage.removeItem('userInfo')
    localStorage.removeItem('activePage')
    window.location.href = '/login'
  }
}

/**
 * 页面跳转函数
 * @param menu
 */
function menuHandler(menu: Menu) {
  router.push(menu.url);
  activePage.value = menu.id;
  window.localStorage.setItem('activePage', menu.id.toString());
}
</script>

<template>
  <div class="app-main">
    <el-container class="app-wrapper">
      <el-aside class="sidebar-container">
        <!-- 侧边栏顶部图片 -->
        <div class="logo">
          <img src="../assets/images/login/login-logo.png" alt="" style="width: 117px; height: 32px"/>
        </div>

        <el-scrollbar wrap-class="scrollbar-wrapper">
          <!-- 侧边栏选项 -->
          <el-menu :default-active="activePage" :unique-opened="false" :collapse-transition="false"
                   background-color="#343744" text-color="#bfcbd9" active-text-color="#f4f4f5">
            <div v-for="item in menuList" :key="item.id">
              <el-menu-item :index="item.id" @click="menuHandler(item)">
                <el-icon>
                  <component :is="item.icon"/>
                </el-icon>
                <span slot="title">{{ item.name }}</span>
              </el-menu-item>
            </div>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <el-container class="main-container">
        <el-header class="navbar">
          <div class="head-label">
            <span v-if="goBackFlag" class="goBack" @click="goBack()">
                <img src="../assets/images/icons/btn_back@2x.png" alt=""/> 返回
            </span>
            <span>{{ headTitle }}</span>
          </div>

          <div class="right-menu">
            <div class="avatar-wrapper">{{ username }}</div>
            <img src="../assets/images/icons/btn_close@2x.png" class="outLogin" alt="退出" @click="logout"/>
          </div>
        </el-header>

        <el-main>
          <div class="divTmp" v-show="loading"></div>
          <router-view :key="$route.fullPath"/>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped lang="scss">
@use "../assets/styles/menu.scss";

.app-main {
  height: 100%;

  .divTmp {
    width: 100%;
    height: 100%;
  }
}

.app-wrapper {
  position: relative;
  height: 100%;
  width: 100%;
  overflow-y: hidden;
  min-width: 1366px;
  overflow-x: auto;
  display: flex;
}

// 侧边栏样式
.sidebar-container {
  transition: width 0.28s;
  width: 190px !important;
  height: 100%;
  z-index: 1001;
  overflow: hidden;

  .scrollbar-wrapper {
    overflow-x: hidden !important;
  }
  :deep(.el-scrollbar__view) {
    height: 100%;
  }
}

.main-container {
  height: 100%;
  background: #f3f4f7;
  position: relative;
  width: calc(100% - 190px);
}

.navbar {
  height: 64px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .right-menu {
    float: right;
    display: flex;
    margin-right: 34px;
    height: 100%;
    line-height: 64px;
    color: #333333;
    font-size: 14px;

    img {
      margin-top: 20px;
      margin-left: 10px;
      width: 25px;
      height: 25px;
    }

    .outLogin {
      cursor: pointer;
    }

    &:focus {
      outline: none;
    }
  }

  .head-label {
    color: #333333;
    height: 64px;
    font-size: 16px;
    width: 300px;
    padding-left: 22px;
    line-height: 64px;
    font-weight: 700;
    opacity: 0;
    float: left;
    animation: opacity 500ms ease-out 800ms forwards;

    .goBack {
      border-right: solid 1px #d8dde3;
      padding-right: 14px;
      margin-right: 14px;
      font-size: 16px;
      color: #333333;
      cursor: pointer;
      font-weight: 400;

      img {
        position: relative;
        top: 24px;
        margin-right: 5px;
        width: 18px;
        height: 18px;
        float: left;
      }
    }
  }
}

// 这段代码是 CSS 中的一个动画关键帧定义, 我也不知道有什么用
@keyframes opacity {
  0% {
    opacity: 0;
    left: 80px;
  }
  100% {
    opacity: 1;
    left: 0;
  }
}

.logo {
  text-align: center;
  background-color: rgb(52, 55, 68);
  padding: 46px 37px 67px 36px;
}

.img {
  display: inline-block;
}
</style>