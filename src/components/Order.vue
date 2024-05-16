<script setup lang="ts">
import {ref} from "vue";
import axiosRequest from "../utils/axiosRequest.ts";
import router from "../router.ts";
import {showNotify} from "vant";
import '@/utils/base.ts';
import {onMounted} from "vue";

let paging = ref({
  page: 1,
  pageSize: 5
})
let orderList = ref([])
let loading = ref(false)
let finished = ref(false)


function goBack() {
  let url = document.referrer
  if (url.includes('success'))
    router.push('/index')
  else
    router.go(-1)
}

async function getList() {
  if (finished.value) {
    loading.value = false;
    return
  }
  try {
    let r = await axiosRequest({
      url: '/order/userPage',
      method: 'get',
      params: paging.value
    })

    if (r.code == 1) {
      let records = r.data.records
      if (records && Array.isArray(records)) {
        // 把每一次订单的详细信息中点餐数量统计出来
        records.forEach(item => {
          let number = 0
          item.orderDetails.forEach((ele: any) => {
            number += ele.number
          })
          item.sumNum = number
        })
        loading.value = false;
        if (paging.value.page >= r.data.pages)
          finished.value = true;
        paging.value.page = paging.value.page + 1
      }
      orderList.value.push(...records)
    } else
      throw new Error(r.msg)

  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
}

onMounted(() => {
  getList()
})

async function addOrderAgain(order: any) {
  try {
    let r = await axiosRequest({
      url: '/order/again',
      method: 'post',
      params: {id: order.id}
    })

    if (r.code == 1)
      await router.push('/index')
    else
      throw new Error(r.msg)

  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
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
  <div id="order" class="app">
    <div class="divHead">
      <div class="divTitle">
        <i class="el-icon-arrow-left" @click="goBack"></i>菩提阁
      </div>
    </div>
    <div class="divBody" v-if="orderList.length > 0">
      <van-list v-model="loading" :finished="finished" finished-text="没有更多了" @load="getList">
        <van-cell v-for="(order, index) in orderList" :key="index" class="item">
          <div class="timeStatus">
            <span>{{ order.orderTime }}</span>
            <span>{{ getStatus(order.status) }}</span>
          </div>

          <div class="dishList">
            <div v-for="(item, index) in order.orderDetails" :key="index" class="item">
              <span>{{ item.name }}</span>
              <span>x{{ item.number }}</span>
            </div>
          </div>
          <div class="result">
            <span>共{{ order.sumNum }} 件商品,实付</span><span class="price">￥{{ order.amount }}</span>
          </div>
          <div class="btn" v-if="order.status === 4">
            <div class="btnAgain" @click="addOrderAgain(order)">再来一单</div>
          </div>
        </van-cell>
      </van-list>
    </div>
    <div class="divNoData" v-else>
      <div class="divContainer">
        <img src="../assets/images/no_order.png" alt=""/>
        <div>暂无订单</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/styles/login.scss" as *;
@use "@/assets/styles/order.scss" as *;
</style>