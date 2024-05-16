<script setup lang="ts">
import router from "../router.ts";
import '@/utils/base.ts';

function goBack() {
  router.go(-1)
}

function toPagePrev() {
  router.go(-1)
}
</script>

<template>
  <div id="no_wifi" class="app">
    <div class="divHead">
      <div class="divTitle">
        <i class="el-icon-arrow-left" @click="goBack"></i>菩提阁
      </div>
    </div>
    <div class="divContent">
      <img src="../assets/images/no_wifi.png" alt=""/>
      <div class="divDesc">网络连接异常</div>
      <div class="btnRefresh" @click="toPagePrev">点击刷新</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/styles/index.scss" as *;
@use "@/assets/styles/no-wifi.scss" as *;
</style>