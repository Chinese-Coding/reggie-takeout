<script setup lang="ts">
import {reloadHeader} from "@/ts/container.ts";
import axiosRequest from "@/utils/axiosRequest.ts";
import router from "@/router.ts";
import {ElMessage, ElMessageBox} from "element-plus";
import {total, initPage, page, pageSize, pageSizes} from "@/ts/common.ts";
import {onMounted, ref} from "vue";
import {Search} from "@element-plus/icons-vue";

interface Employee {
  id: number,
  username: string,
  name: string,
  phone: string,
  sex: string,
  status: string,
}

let input = ref('');
let tableData = ref<Employee[]>([]);
let userInfo = ref('');

onMounted(() => {
  initPage(getEmployees);
  reloadHeader('员工管理', false);
  getEmployees();
  // 获取当前登录员工的账号, 并赋值给模型数据 user
  if (localStorage.getItem('userInfo') != null)
    userInfo.value = JSON.parse(localStorage.getItem('userInfo')).username
})

/**
 * 初始化函数, 获取员工列表
 * */
async function getEmployees(pageValue: number | undefined = undefined) {
  if (pageValue != undefined)
    page.value = pageValue;

  try {
    let r = await axiosRequest({
      url: '/employee/page',
      method: 'get',
      params: {
        page: page.value,
        pageSize: pageSize.value,
        name: input.value ? input.value : undefined
      }
    })

    if (r.code == 1) {
      tableData.value = r.data.records.map((item: any) => {
        return {
          id: item.id,
          username: item.username,
          name: item.name,
          phone: item.phone,
          sex: item.sex,
          status: item.status
        }
      }) || []
      total.value = r.data.total
    } else
      new Error(r.msg)
  } catch (err: any) {
    ElMessage.error('出错了: ' + err.message)
  }
}

/**
 * 新增或修改员工信息
 * @param option 用来指代操作, 或传递需要修改员工的 id
 * */
function addOrUpdateEmp(option: string) {
  if (option === 'add')
    router.push({path: '/employee/add'});
  else
    router.push({path: '/employee/add', query: {id: option}});
}

/**
 * 员工状态修改
 * */
function updateEmpStatus(row: any) {
  ElMessageBox.confirm('确认调整该账号的状态?', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      let r = await axiosRequest({
        url: '/employee',
        method: 'put',
        data: {
          id: row.id,
          status: row.status == '0' ? '1' : '0'
        }
      })
      if (r.code == 1) {
        ElMessage.success('账号状态更改成功！')
        await getEmployees(1);
      } else
        throw new Error(r.msg)
    } catch (err: any) {
      ElMessage.error('出错了: ' + +err.message)
    }
  })
}
</script>

<template>
  <div class="dashboard-container" id="member-app">
    <div class="container">
      <div class="tableBar">
        <!-- 搜索框 -->
        <el-input v-model="input" placeholder="请输入员工姓名" style="width: 250px" size="large"
                  clearable @keyup.enter.native="getEmployees(1)">
          <template #prefix>
            <el-icon class="el-input__icon" style="cursor: pointer" @click="getEmployees(1)">
              <Search/>
            </el-icon>
          </template>
        </el-input>

        <!-- 通过 为 button 添加 `size="large"` 这个参数, 终于做到了让央视看起来与原项目保持一致 -->
        <!-- 不知道原项目是怎么实现的, 难道是在 ElementUI 中 primary 类型的按钮大小默认就是 "large"? -->
        <el-button type="primary" size="large" @click="addOrUpdateEmp('add')">
          + 添加员工
        </el-button>
      </div>
      <el-table :data="tableData" stripe class="tableBox">
        <el-table-column prop="name" label="员工姓名"/>
        <el-table-column prop="username" label="账号"/>
        <el-table-column prop="phone" label="手机号"/>
        <el-table-column label="账号状态">
          <template #default="scope">
            {{ String(scope.row.status) === '0' ? '已禁用' : '正常' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="scope">
            <el-button text size="small" class="update-button" @click="addOrUpdateEmp(scope.row.id)">
              编辑
            </el-button>

            <!-- 对于管理员而言应该取消 启用或禁用按钮 -->
            <el-button text size="small" class="delete-button no-border-right"
                       @click="updateEmpStatus(scope.row)" v-if="userInfo=== 'admin'">
              {{ scope.row.status == '1' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页部分 -->
      <el-pagination class="pagination" :page-sizes="pageSizes"
                     v-model:page-size="pageSize" v-model:current-page="page"
                     layout="total, sizes, prev, pager, next, jumper" :total="total"
      />
    </div>
  </div>
</template>
<style scoped lang="scss">
@use "@/assets/styles/common.scss" as *;
@use "@/assets/styles/button.scss" as *;
@use "@/assets/styles/table.scss" as *;

</style>