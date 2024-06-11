<script setup lang="ts">
import {ref} from "vue";
import axiosRequest from "../utils/axiosRequest.ts";
import router from "../router.ts";
import {showNotify} from "vant";
import '@/utils/base.ts';

let form = ref({
  phone: '',
  code: ''
})
let msgFlag = ref(false)
let loading = ref(false)

function getCode() {
  form.value.code = ''
  let regex = /^(13[0-9]{9})|(15[0-9]{9})|(17[0-9]{9})|(18[0-9]{9})|(19[0-9]{9})$/;
  if (regex.test(form.value.phone)) {
    msgFlag.value = false
    axiosRequest({
      url: '/user/sendMsg',
      method: 'post',
      data: {
        phone: form.value.phone
      }
    })
  } else
    msgFlag.value = true
}

async function btnLogin() {
  if (form.value.phone && form.value.code) {
    loading.value = true
    try {
      let r = await axiosRequest({
        url: '/user/login',
        method: 'post',
        data: form.value
      })

      if (r.code == 1) {
        sessionStorage.setItem("userPhone", form.value.phone)
        loading.value = false
        await router.push('/index')
      } else
        throw new Error(r.msg)

    } catch (e: any) {
      showNotify({type: 'warning', message: e.message});
    }
  } else
    showNotify({type: 'warning', message: '请输入手机号码'});

}
</script>

<template>
  <div id="login" v-loading="loading">
    <div class="divHead">登录</div>
    <div class="divContainer">
      <el-input placeholder=" 请输入手机号码" v-model="form.phone" maxlength='20'/>
      <div class="divSplit"/>
      <el-input placeholder=" 请输入验证码" v-model="form.code" maxlength='20'/>
      <span @click='getCode'>获取验证码</span>
    </div>
    <div class="divMsg" v-if="msgFlag">手机号输入不正确，请重新输入</div>
    <el-button type="primary" :class="{btnSubmit:1===1,btnNoPhone:!form.phone,btnPhone:form.phone}" @click="btnLogin">
      登录
    </el-button>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/styles/login.scss" as *;
@use "@/assets/styles/index.scss" as *;
</style>
