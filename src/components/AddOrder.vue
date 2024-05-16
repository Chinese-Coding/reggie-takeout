<script setup lang="ts">
import {computed, ref} from "vue";
import axiosRequest from "../utils/axiosRequest.ts";
import router from "../router.ts";
import {showNotify} from "vant";
import '@/utils/base.ts';
import {onMounted} from "vue";

let address = ref({
  phone: '',
  consignee: '',
  detail: '',
  sex: '1',
  id: undefined
});
let finishTime = ref('');
let cartData = ref([]);
let note = ref('');

let goodsNum = computed(() => {
  let num = 0
  cartData.value.forEach(item => {
    num += item.number
  })
  if (num < 99)
    return num
  else
    return '99+'
})
let goodsPrice = computed(() => {
  let price = 0
  cartData.value.forEach(item => {
    price += (item.number * item.amount)
  })
  return price
})

onMounted(() => {
  getDefaultAddress();
  getCartData();
})

async function getCartData() {
  try {
    let r = await axiosRequest({
      url: '/shoppingCart/list',
      method: 'get',
    })

    if (r.code == 1)
      cartData.value = r.data
    else
      throw new Error(r.msg)

  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
}

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

async function getDefaultAddress() {
  try {
    let r = await axiosRequest({
      url: '/addressBook/default',
      method: 'get',
    })

    if (r.code == 1) {
      address.value = r.data
      getFinishTime();
    } else {
      throw new Error(r.msg)
    }
  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
}

function goBack() {
  router.go(-1)
}

function toAddressPage() {
  router.push('/address')
}

function imgPathConvert(path: string) {
  return '/download?name=' + path
}

async function goToPaySuccess() {
  const params = {
    remark: note.value,
    payMethod: 1,
    addressBookId: address.value.id
  }
  try {
    let r = await axiosRequest({
      url: '/order/submit',
      method: 'post',
      data: params
    })

    if (r.code == 1) {
      console.log('跳转成功')
      await router.push('/pay-success')

    } else
      throw new Error(r.msg)

  } catch (e: any) {
    console.log(e)
    showNotify({type: 'warning', message: e.message});
  }
}
</script>

<template>
  <div id="add_order" class="app">
    <div class="divHead">
      <div class="divTitle">
        <i class="el-icon-arrow-left" @click="goBack"></i>菩提阁
      </div>
    </div>
    <div class="divContent">
      <div class="divAddress">
        <div @click="toAddressPage">
          <div class="address">{{ address.detail }}</div>
          <div class="name">
            <span>{{ address.consignee }}{{ address.sex === '1' ? '先生' : '女士' }}</span>
            <span>{{ address.phone }}</span>
          </div>
          <i class="el-icon-arrow-right"></i>
        </div>
        <div class="divSplit"></div>
        <div class="divFinishTime">预计{{ finishTime }}送达</div>
      </div>
      <div class="order">
        <div class="title">订单明细</div>
        <div class="divSplit"></div>
        <div class="itemList">
          <div class="item" v-for="(item,index) in cartData" :key="index">
            <el-image :src="imgPathConvert(item.image)">
              <div slot="error" class="image-slot">
                <img src="../assets/images/noImg.png" alt=""/>
              </div>
            </el-image>
            <div class="desc">
              <div class="name">{{ item.name }}</div>
              <div class="numPrice">
                <span class="num">x{{ item.number }}</span>
                <div class="price">
                  <span class="spanMoney">￥</span>{{ item.amount }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="note">
        <div class="title">备注</div>
        <van-field v-model="note" rows="2" autosize type="textarea" maxlength="50" placeholder="请输入您需要备注的信息"
                   show-word-limit/>
      </div>
    </div>

    <div class="divCart">
      <div :class="{imgCartActive: cartData && cartData.length > 0, imgCart:!cartData || cartData.length<1}"></div>
      <div :class="{divGoodsNum:1===1, moreGoods:cartData && cartData.length > 99}"
           v-if="cartData && cartData.length > 0">{{ goodsNum }}
      </div>
      <div class="divNum">
        <span>￥</span>
        <span>{{ goodsPrice }}</span>
      </div>
      <div class="divPrice"></div>
      <div :class="{btnSubmitActive: cartData && cartData.length > 0, btnSubmit:!cartData || cartData.length<1}"
           @click="goToPaySuccess">去支付
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/styles/login.scss" as *;
@use "@/assets/styles/add-order.scss" as *;
</style>