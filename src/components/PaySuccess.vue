<script setup lang="ts">
import {ref, onMounted} from "vue";
import router from "../router.ts";
import '@/utils/base.ts';

let finishTime = ref('')

onMounted(() => {
  getFinishTime()
})

function getFinishTime() {
  let now = new Date()
  let hour = now.getHours() + 1
  let minute = now.getMinutes()
  if (hour.toString().length < 2)
    hour = '0' + hour

  if (minute.toString().length < 2)
    minute = '0' + minute
  finishTime.value = hour + ':' + minute
}

function goBack() {
  router.push('/index')
}

function toOrderPage() {
  router.push('/order')
}
function toMainPage() {
  router.push('/index')
}
</script>

<template>
  <div id="pay_success" class="app">
    <div class="divHead">
      <div class="divTitle">
        <i class="el-icon-arrow-left" @click="goBack"></i>
        菩提阁
        <img src="../assets/images/noImg.png" @click="toMainPage" alt=""/>
      </div>
    </div>
    <div class="divContent">
      <img src="../assets/images/success.png" alt=""/>
      <div class="divSuccess">下单成功</div>
      <div class="divDesc">预计{{ finishTime }}到达</div>
      <div class="divDesc1">后厨正在加紧制作中，请耐心等待~</div>
      <div class="btnView" @click="toOrderPage">查看订单</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/styles/pay-success.scss" as *;
@use "@/assets/styles/index.scss" as *;
</style>