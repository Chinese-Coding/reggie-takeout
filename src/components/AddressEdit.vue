<script setup lang="ts">
import {ref} from "vue";
import axiosRequest from "../utils/axiosRequest.ts";
import router from "../router.ts";
import {showNotify, showConfirmDialog} from "vant";
import '@/utils/base.ts';
import {onMounted} from "vue";
import {parseUrl} from "@/utils/utils.ts";

let title = ref('新增收货地址')
let form = ref({
  consignee: '',
  phone: undefined,
  sex: '1',
  detail: '',
  label: '公司'
})
let labelList = ref([
  '无', '公司', '家', '学校'
])
let id = ref(undefined)
let activeIndex = ref(0)

onMounted(async () => {
  let params = parseUrl(window.location.search)
  id.value = params.id
  if (params.id) {
    title.value = '编辑收货地址'
    try {
      let r = await axiosRequest({
        url: `/addressBook/${params.id}`,
        method: 'get',
      })

      if (r.code == 1)
        form.value = r.data
      else
        throw new Error(r.msg)

    } catch (e: any) {
      showNotify({type: 'warning', message: e.message});
    }
  }
})

async function saveAddress() {
  let formValue = form.value
  if (!formValue.consignee) {
    showNotify({type: 'warning', message: '请输入联系人'});
    return
  }
  if (!formValue.phone) {
    showNotify({type: 'warning', message: '请输入手机号'});
    return
  }
  if (!formValue.detail) {
    showNotify({type: 'warning', message: '请输入收货地址'});
    return
  }
  const reg = /^1[3|4|5|7|8][0-9]{9}$/
  if (!reg.test(formValue.phone)) {
    showNotify({type: 'warning', message: '手机号码不合法'});
    return
  }

  let method: string
  if (id.value)
    method = 'put'
  else
    method = 'post'

  try {
    let r = await axiosRequest({
      url: '/addressBook',
      method: method,
      data: form.value
    })

    if (r.code == 1)
      await router.push('/address')
    else
      throw new Error(r.msg)

  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
}

function goBack() {
  router.go(-1)
}

function deleteAddress() {
  showConfirmDialog({
    title: '确认删除',
    message: '确认要删除当前地址吗？',
  }).then(async () => {

    let r = await axiosRequest({
      url: '/addressBook',
      method: 'delete',
    })
    if (r.code == 1)
      await router.push('/address')
    else
      throw new Error(r.msg)
  }).catch(() => {
    showNotify({type: 'warning', message: e.message});
  });
}

</script>

<template>
  <div id="address_edit" class="app">
    <div class="divHead">
      <div class="divTitle">
        <i class="el-icon-arrow-left" @click="goBack"></i>{{ title }}
      </div>
    </div>
    <div class="divContent">
      <div class="divItem">
        <span>联系人：</span>
        <el-input placeholder=" 请填写收货人的姓名" v-model="form.consignee" maxlength='10' class="inputUser"/>
        <span class="spanChecked" @click="form.sex = '1'">
                    <i :class="{iActive:form.sex === '1'}"/>
                    先生
                   </span>
        <span class="spanChecked" @click="form.sex = '0'">
                    <i :class="{iActive:form.sex === '0'}"/>
                    女士
                </span>
      </div>
      <div class="divItem">
        <span>手机号：</span>
        <el-input placeholder=" 请填写收货人手机号码" v-model="form.phone" maxlength='20'
                  style="width: calc(100% - 80rem);"/>
      </div>
      <div class="divItem">
        <span>收货地址：</span>
        <el-input placeholder=" 请输入收货地址" v-model="form.detail" maxlength='140'/>
      </div>
      <div class="divItem ">
        <span>标签：</span>
        <span v-for="(item,index) in labelList" :key="index" @click="form.label = item;activeIndex = index"
              :class="{spanItem:true,spanActiveSchool:activeIndex === index}">{{ item }}</span>
      </div>
      <div class="divSave" @click="saveAddress">保存地址</div>
      <div class="divDelete" @click="deleteAddress" v-if="id">删除地址</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/styles/login.scss" as *;
@use "@/assets/styles/address-edit.scss" as *;
</style>