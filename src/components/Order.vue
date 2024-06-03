<script setup lang="ts">
import {initPage, page, pageSize, pageSizes, total} from "@/ts/common.ts";
import {onMounted, ref} from "vue";
import axiosRequest from "@/utils/axiosRequest.ts";
import {ElMessage, ElMessageBox} from "element-plus";
import {reloadHeader} from "@/ts/container.ts";

let loading = ref(false);
let input = ref('');
let beginTime = ref('');
let endTime = ref('');
let tableData = ref([]);
let orderTime = ref('');
let diaForm = ref({})
let dialogVisible = ref(false)

onMounted(() => {
  initPage(getOrderDetailPage)
  reloadHeader('订单管理', false);
  getOrderDetailPage();
})

async function getOrderDetailPage() {
  let params = {
    page: page.value,
    pageSize: pageSize.value,
    number: input.value || undefined,
    beginTime: beginTime.value || undefined,
    endTime: endTime.value || undefined,
  }

  try {
    let r = await axiosRequest({
      url: '/order/page',
      method: 'get',
      params: params
    })

    if (r.code == 1) {
      tableData.value = r.data.records || []
      total.value = r.data.total
    } else
      throw new Error(r.msg || '操作失败')
  } catch (e: any) {
    ElMessage.error('出错了: ' + e.message);
  }
}

function getOrderType(row: any) {
  let str = ''
  switch (row.status) {
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

function goDetail(row: any) {
  diaForm.value = {}
  dialogVisible.value = true
  diaForm.value = {...row}
}

function cancelOrDeliveryOrComplete(status: number, id: string) {
  ElMessageBox.confirm('确认更改该订单状态?', '提示', {
    'confirmButtonText': '确定',
    'cancelButtonText': '取消',
    'type': 'warning'
  }).then(async () => {
        try {
          let r = await axiosRequest({
            url: '/order',
            method: 'put',
            params: {
              id: id,
              status: status
            }
          })

          if (r.code == 1) {
            ElMessage.success(status == 3 ? '订单已派送' : '订单已完成')
            await getOrderDetailPage()
          } else
            throw new Error(r.msg || '操作失败')

        } catch (e: any) {
          ElMessage.error('出错了: ' + e.message);
        }
      }
  )
}

function handleClose() {
  dialogVisible.value = false
}
</script>

<template>
  <div class="dashboard-container" id="order-app" v-loading="loading">
    <div class="container">
      <!-- 搜索项 -->
      <div class="tableBar">
        <el-input v-model="input" placeholder="请输入订单号" style="width: 250px">
          <i slot="prefix" class="el-input__icon el-icon-search" style="cursor: pointer" @click="initPage"></i>
        </el-input>

        <el-date-picker v-model="orderTime" clearable value-format="yyyy-MM-dd HH:mm:ss" type="datetimerange"
                        placeholder="选择日期" range-separator="至" start-placeholder="开始日期"
                        end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']"
                        style="width: 400px;margin-left: 20px;"/>
        <el-button type="primary" class="search-btn" @click="getOrderDetailPage">查询</el-button>
      </div>

      <el-table :data="tableData" stripe class="tableBox">
        <el-table-column prop="number" label="订单号" min-width="110"/>

        <el-table-column prop="订单状态" label="订单状态">
          <template #default="scope">
            <span>{{ getOrderType(scope.row) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="userName" label="用户"/>
        <el-table-column prop="phone" label="手机号"/>
        <el-table-column prop="address" label="地址" show-overflow-tooltip/>
        <el-table-column prop="orderTime" label="下单时间" min-width="100"/>
        <el-table-column prop="amount" label="实收金额">
          <template #default="scope">
            <span>￥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="btn" label="操作">
          <template #default="scope">
            <el-button type="text" @click="goDetail(scope.row)" class="blueBug">
              查看
            </el-button>
            <el-divider v-if="scope.row.status === 2" direction="vertical"/>

            <el-button v-if="scope.row.status === 2" type="text" @click="cancelOrDeliveryOrComplete(3, scope.row.id)"
                       class="blueBug">
              派送
            </el-button>

            <el-divider v-if="scope.row.status === 3" direction="vertical"/>
            <el-button v-if="scope.row.status === 3" type="text" @click="cancelOrDeliveryOrComplete(4, scope.row.id)"
                       class="blueBug">
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页部分 -->
      <el-pagination class="pagination" :page-sizes="pageSizes" v-model:page-size="pageSize" :total="total"
                     v-model:current-page="page" layout="total, sizes, prev, pager, next, jumper"/>
    </div>

    <!-- 查看弹框部分 -->
    <el-dialog title="订单信息" v-model="dialogVisible" width="30%" :before-close="handleClose">
      <div class="info-box">
        <div class="item-box">
          <span class="label">订单号：</span>
          <span class="des">{{ diaForm.number }}</span>
        </div>
        <div class="item-box">
          <span class="label">订单状态：</span>
          <span class="des">{{ getOrderType(diaForm) }}</span>
        </div>
        <div class="item-box">
          <span class="label">收货人：</span>
          <span class="des">{{ diaForm.consignee }}</span>
        </div>
        <div class="item-box">
          <span class="label">联系电话：</span>
          <span class="des">{{ diaForm.phone }}</span>
        </div>
        <div class="item-box">
          <span class="label">地址：</span>
          <span class="des">{{ diaForm.address }}</span>
        </div>
        <div class="item-box">
          <span class="label">支付金额：</span>
          <span class="des">{{ diaForm.amount }}</span>
        </div>
        <div class="item-box">
          <span class="label">下单时间：</span>
          <span class="des">{{ diaForm.orderTime }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use "@/assets/styles/common.scss" as *;
@use "@/assets/styles/button.scss" as *;
@use "@/assets/styles/table.scss" as *;
@use "@/assets/styles/dialog.scss" as *;

.search-btn {
  margin-left: 20px;
}

.tableBar {
  justify-content: flex-start !important;
}

.info-box {
  margin: -15px -44px 20px;
}

.info-box .item-box {
  display: flex;
  height: 20px;
  line-height: 20px;
  font-size: 14px;
  font-weight: 400;
  color: #666666;
  text-align: left;
  margin-bottom: 14px;
}

.info-box .item-box:last-child {
  margin-bottom: 0;
}

.info-box .item-box .label {
  width: 96px;
}

.info-box .item-box .des {
  flex: 1;
  color: #333333;
}
</style>
