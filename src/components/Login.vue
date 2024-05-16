<script setup lang="ts">
import {reactive, ref} from "vue";
import axiosRequest from "../utils/axiosRequest.ts";
import router from "../router.ts";
import {Lock, User} from "@element-plus/icons-vue";
import {ElMessage, FormInstance, FormRules} from "element-plus";

let loginForm = reactive({
  username: 'admin',
  password: '123456'
})
let loading = ref(false)
let loginFormRef = ref<FormInstance>(null)
const validateUsername = (rule: any, value: any, callback: any) => {
  if (!value)
    return callback(new Error('请输入用户名'))
  else
    callback()
}
const validatePassword = (rule: any, value: any, callback: any) => {
  if (!value)
    return callback(new Error('请输入密码'))
  if (value.length < 6)
    callback(new Error('密码必须在6位以上'))
  else
    callback()
}
const rules = reactive<FormRules<typeof loginForm>>(
    {
      username: [{validator: validateUsername, trigger: 'blur'}],
      password: [{validator: validatePassword, trigger: 'blur'}]
    }
)

async function login() {
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        let r = await axiosRequest({
          url: '/employee/login',
          method: 'post',
          data: loginForm
        })

        if (r.code == 1) { // 1 表示登录成功
          localStorage.setItem('userInfo', JSON.stringify(r.data))
          ElMessage.success('登录成功')
          await router.push('/employee')
        } else
          throw new Error(r.msg)

      } catch (e: any) {
        ElMessage.error('出错了: ' + e.message)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<template>
  <div class="login">
    <div class="login-box">
      <img src="../assets/images/login/login-l.png" alt="">

      <div class="login-form">
        <el-form ref="loginFormRef" :model="loginForm" :rules="rules">
          <div class="login-form-title">
            <img src="../assets/images/login/logo.png" style="width:139px; height:42px;" alt=""/>
          </div>
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" auto-complete="off" text placeholder="账号" :prefix-icon="User"/>
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock"
                      @keyup.enter.native="login"/>
          </el-form-item>

          <el-form-item>
            <el-button :loading="loading" class="login-button" size="default"
                       type="primary" @click.native.prevent="login">
              <span v-if="!loading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #333;

  .login-box {
    width: 1000px;
    height: 474px;
    border-radius: 8px;
    display: flex;

    img {
      width: 60%;
      height: auto;
    }
  }

  .login-form {
    background: #ffffff;
    width: 40%;
    border-radius: 0 8px 8px 0;
    display: flex;
    justify-content: center;
    align-items: center;

    // 根据原先项目人工调整的样式, 也就是从 element-UI 到 element-plus 样式的调整
    // 不可能做到完全一直, 只能尽量模仿 (因为原项目在 element-UI 提供的 CSS 上进行了自定义)
    :deep(.el-form) {
      width: 214px;
      height: 307px;
    }

    :deep(.el-form-item) {
      margin-bottom: 30px;
    }

    :deep(.el-input__wrapper) {
      // 去除边框
      border: none !important;
      box-shadow: none !important;
      border-bottom: 1px solid #e9e9e8 !important; // 边框底部的横线
    }

    :deep(.el-input__prefix) {
      left: 0 !important;
      margin-left: -15px;
    }

    // 调整图标大小
    :deep(.el-input__icon) {
      height: 40px;
      width: 20px;
      margin-left: -2px;
      font-size: 25px;
    }

    .login-form-title {
      height: 36px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: 40px;

      & .title-label {
        font-weight: 500;
        font-size: 20px;
        color: #333333;
        margin-left: 10px;
      }
    }

    .login-button {
      width: 100%;
      border-radius: 17px;
      padding: 11px 20px !important;
      margin-top: 10px;
      font-weight: 500;
      font-size: 14px;
      border: 0;
      background-color: #ffc200;
      color: #000000;

      &:focus,
      &:hover {
        background-color: #ffc200;
        color: #ffffff;
      }
    }
  }
}
</style>