<script setup lang="ts">
import {ref} from "vue";
import axiosRequest from "../utils/axiosRequest.ts";
import router from "../router.ts";
import {showNotify} from "vant";
import '@/utils/base.ts';
import {onMounted} from "vue";

let addressList = ref([]);

function goBack() {
  router.go(-1)
}

function toAddressEditPage(item: any) {
  router.push({path: '/address-edit', query: {id: item.id}})
}

  function toAddressCreatePage() {
    router.push('/address-edit')
  }

  onMounted(() => {
    getAddressList()
  })

  async function getAddressList() {
    try {
      let r = await axiosRequest({
        url: '/addressBook/list',
        method: 'get',
      })

      if (r.code == 1)
        addressList.value = r.data
      else
        throw new Error(r.msg)

    } catch (e: any) {
      showNotify({type: 'warning', message: e.message});
    }
  }

  async function setDefaultAddress(item: any) {
    if (item.id) {
      try {
        let r = await axiosRequest({
          url: '/addressBook/default',
          method: 'put',
          data: {id: item.id}
        })

        if (r.code == 1)
          await getAddressList()
        else
          throw new Error(r.msg)

      } catch (e: any) {
        showNotify({type: 'warning', message: e.message});
      }
    }
  }

  function itemClick(item: any) {
    const url = document.referrer
    //表示是从订单页面跳转过来的
    if (url.includes('order')) {
      setDefaultAddress(item)
      router.go(-1)
    }
  }
</script>

<template>
  <div id="address" class="app">
    <div class="divHead">
      <div class="divTitle">
        <i class="el-icon-arrow-left" @click="goBack"></i>地址管理
      </div>
    </div>
    <div class="divContent">
      <div class="divItem" v-for="(item,index) in addressList" :key="index" @click.capture="itemClick(item)">
        <div class="divAddress">
          <span
              :class="{spanCompany:item.label === '公司',spanHome:item.label === '家',spanSchool:item.label === '学校'}">{{
              item.label
            }}</span>
          {{ item.detail }}
        </div>
        <div class="divUserPhone">
          <span>{{ item.consignee }}</span>
          <span>{{ item.sex === '0' ? '女士' : '先生' }}</span>
          <span>{{ item.phone }}</span>
        </div>
        <img src="../assets/images/edit.png" @click.stop.prevent="toAddressEditPage(item)"/>
        <div class="divSplit"></div>
        <div class="divDefault">
<!--          <img src="../assets/images/checked_true.png" v-if="item.isDefault === 1">-->
          <img src="../assets/images/checked_true.png" v-if="item.defaultAddress === true">
          <img src="../assets/images/checked_false.png" @click.stop.prevent="setDefaultAddress(item)" v-else>设为默认地址
        </div>
      </div>
    </div>
    <div class="divBottom" @click="toAddressCreatePage">+ 添加收货地址</div>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/styles/login.scss" as *;
@use "@/assets/styles/address.scss" as *;
</style>