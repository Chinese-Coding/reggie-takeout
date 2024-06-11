<script setup lang="ts">
import {ref} from "vue";
import axiosRequest from "../utils/axiosRequest.ts";
import router from "../router.ts";
import {showNotify} from "vant";
import '@/utils/base.ts';
import {onMounted} from "vue";

let order = ref([{
  orderItem: '',
  status: undefined,
  orderDetails: [{
    name: '',
    number: undefined
  }],
  amount: undefined,
  sumNum: 0
}])
let userPhone = ref('')
let userName = ref('')

onMounted(() => {
  userPhone.value = <string>sessionStorage.getItem('userPhone')
  getUserInfo()
  getLatestOrder()
})

async function getUserInfo() {
  try {
    let r = await axiosRequest({
      url: '/user/info',
      method: 'get'
    })

    if (r.code == 1) {
      userName.value = r.data.name
    } else
      throw new Error(r.msg)
  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
}

async function getLatestOrder() {
  const params = {
    page: 1,
    pageSize: 1
  }

  try {
    let r = await axiosRequest({
      url: '/order/userPage',
      method: 'get',
      params: {...params}
    })

    if (r.code == 1) {
      order.value = r.data.records
      if (order.value && order.value[0].orderDetails) {
        let number = 0
        order.value[0].orderDetails.forEach(item => {
          number += item.number
        })
        order.value[0].sumNum = number
      }
    } else
      throw new Error(r.msg)
  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
}

async function addOrderAgain() {
  try {
    let r = await axiosRequest({
      url: '/order/again',
      method: 'post',
      data: {id: order.value[0].id}
    })

    if (r.code == 1)
      await router.push('/index')
    else
      throw new Error(r.msg)
  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
}

async function toPageLogin() {
  try {
    let r = await axiosRequest({
      url: '/user/logout',
      method: 'post',
    })

    if (r.code == 1)
      await router.push('/login')
    else
      throw new Error(r.msg)
  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
}

function goBack() {
  router.push('/index')
}

function toAddressPage() {
  router.push('/address')
}

function toOrderPage() {
  router.push('/order')
}

function getStatus(status: number) {
  let str = ''
  switch (status) {
    case 1:
      str = '待付款'
      break;
    case 2:
      str = '正在派送'
      break;
    case 3:
      str = '已派送'
      break;
    case 4:
      str = '已完成'
      break;
    case 5:
      str = '已取消'
      break;

  }
  return str
}
</script>

<template>
  <div id="user" class="app">
    <div class="divHead">
      <div class="divTitle">
        <i @click="goBack">个人中心</i>
      </div>
      <div class="divUser">
        <img src="../assets/images/headPage.png" alt=""/>
        <div class="desc">
          <div class="divName">{{ userName }} <img src="../assets/images/women.png" alt=""/></div>
          <div class="divPhone">{{ userPhone }}</div>
        </div>
      </div>
    </div>
    <div class="divContent">
      <div class="divLinks">
        <div class="item" @click="toAddressPage">
          <img src="../assets/images/locations.png" alt=""/>
          <span>地址管理</span>
          <i class="el-icon-arrow-right"></i>
        </div>
        <div class="divSplit"></div>
        <div class="item" @click="toOrderPage">
          <img src="../assets/images/orders.png" alt=""/>
          <span>历史订单</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>
      <div class="divOrders" v-if="order[0]">
        <div class="title">最新订单</div>
        <div class="timeStatus">
          <span>{{ order[0].orderTime }}</span>
          <span>{{ getStatus(order[0].status) }}</span>
          <!-- <span>正在派送</span> -->
        </div>
        <div class="dishList">
          <div v-for="(item,index) in order[0].orderDetails" :key="index" class="item">
            <span>{{ item.name }}</span>
            <span>x{{ item.number }}</span>
          </div>
        </div>
        <div class="result">
          <span>共{{ order[0].sumNum }} 件商品,实付</span>
          <span class="price">￥{{ order[0].amount }}</span>
        </div>
        <div class="btn" v-if="order[0].status === 4">
          <div class="btnAgain" @click="addOrderAgain">再来一单</div>
        </div>
      </div>
      <div class="quitLogin" @click="toPageLogin">
        退出登录
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/styles/login.scss" as *;
@use "@/assets/styles/user.scss" as *;
</style>
