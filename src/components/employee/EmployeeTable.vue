<script lang="ts" setup>
import {goBack, reloadHeader} from "@/ts/container.ts";
import {getURLParam} from "@/ts/common.ts";
import axiosRequest from "@/utils/axiosRequest.ts";
import {ElMessage, FormInstance} from "element-plus";
import {ref, onMounted} from "vue";

interface EmployeeForm {
  username: string;
  name: string;
  sex: string;
  idNumber: string;
  phone: string;
}

let employeeFormRef = ref<FormInstance>(null)
let employeeForm = ref<EmployeeForm>({
  username: '',
  name: '',
  sex: '',
  idNumber: '',
  phone: ''
});
let id = '';
let actionType = '';
let rules = {
  username: [
    {required: true, message: '请输入账号', trigger: 'blur'},
    {min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur'},
  ],
  name: [
    {required: true, message: '请输入员工姓名', trigger: 'blur'},
    {min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur'},
  ],
  sex: [
    {required: true, message: '请选择性别', trigger: 'blur'},
  ],
  phone: [
    {required: true, message: '请输入手机号', trigger: 'blur'},
    {min: 11, max: 11, message: '长度在 11 个字符', trigger: 'blur'},
    {pattern: /^1[3|4|5|7|8][0-9]\d{8}$/, message: '手机号格式不正确', trigger: 'blur'}
  ],
  idNumber: [
    {required: true, message: '请输入身份证号', trigger: 'blur'},
    {pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证号码不正确', trigger: 'blur'}
  ]
};

async function getEmpById() {
  try {
    let r = await axiosRequest({
      url: `/employee/${id}`,
      method: 'get'
    })

    if (r.code == 1) {
      employeeForm.value = {
        username: r.data.username,
        name: r.data.name,
        sex: r.data.sex == 'male' ? '男' : '女',
        idNumber: r.data.idNumber,
        phone: r.data.phone
      }
    } else
      throw new Error(r.msg || '操作失败');

  } catch (e: any) {
    ElMessage.error('出错了: ' + e.message)
  }
}

function submitForm(_continue: boolean) {
  employeeFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      let params = {
        ...employeeForm.value,
        sex: employeeForm.value.sex == '女' ? 'female' : 'male'
      }

      if (actionType == 'add') {
        try {
          let r = await axiosRequest({
            url: '/employee',
            method: 'post',
            data: params
          })

          if (r.code == 1) {
            ElMessage.success('员工添加成功!')
            if (_continue) {
              employeeForm.value = {
                username: '',
                name: '',
                sex: '男',
                idNumber: '',
                phone: '',
              }
            } else
              goBack();

          } else
            throw new Error(r.msg || '操作失败')

        } catch (e: any) {
          ElMessage.error('请求出错了：' + e.message)
        }
      } else {
        params.id = id;
        try {
          let r = await axiosRequest({
            url: '/employee',
            method: 'put',
            data: params
          })

          if (r.code == 1) {
            ElMessage.success('员工信息修改成功!');
            goBack();
          } else
            throw new Error(r.msg || '操作失败')

        } catch (e: any) {
          ElMessage.error('请求出错了：' + e.message)
        }
      }
    } else {
      console.log('error submit!!')
      return false
    }
  });
}

onMounted(() => {
  id = getURLParam('id');
  if (id) {
    actionType = 'edit';
    reloadHeader('修改员工', true, '/employee');
    getEmpById(); // 先加载界面, 最后获取用户信息
  } else {
    actionType = 'add';
    reloadHeader('添加员工', true, '/employee');
  }
});
</script>

<template>
  <div class="addBrand-container">
    <div class="container">
      <el-form ref="employeeFormRef" :model="employeeForm" :rules="rules"
               :inline="false" label-width="180px" class="demo-ruleForm">
        <el-form-item label="账号:" prop="username">
          <el-input v-model="employeeForm.username" placeholder="请输入账号" maxlength="20"/>
        </el-form-item>

        <el-form-item label="员工姓名:" prop="name">
          <el-input v-model="employeeForm.name" placeholder="请输入员工姓名" maxlength="20"/>
        </el-form-item>

        <el-form-item label="手机号:" prop="phone">
          <el-input v-model="employeeForm.phone" placeholder="请输入手机号" maxlength="20"/>
        </el-form-item>

        <el-form-item label="性别:" prop="sex">
          <el-radio-group v-model="employeeForm.sex">
            <el-radio label="男"></el-radio>
            <el-radio label="女"></el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="身份证号:" prop="idNumber">
          <el-input v-model="employeeForm.idNumber" placeholder="请输入身份证号" maxlength="20"/>
        </el-form-item>

        <div class="subBox address">
          <el-form-item>
            <el-button @click="goBack()" size="large">取消</el-button>

            <el-button type="primary" @click="submitForm(false)" size="large">保存</el-button>

            <el-button v-if="actionType == 'add'" type="primary" class="continue" @click="submitForm(true)"
                       size="large">
              保存并继续添加
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/assets/styles/common.scss" as *;
@use "@/assets/styles/button.scss" as *;
@use "@/assets/styles/table.scss" as *;

</style>