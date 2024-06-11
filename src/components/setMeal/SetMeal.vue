<script lang="ts" setup>
import {getImage, initPage, page, pageSize, pageSizes, total} from "@/ts/common.ts";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router.ts";
import axiosRequest from "@/utils/axiosRequest.ts";
import {reloadHeader} from "@/ts/container.ts";
import {onMounted, ref} from "vue";
import {Search} from "@element-plus/icons-vue";

let input = ref('')
let tableData = ref([])
let selectedList = ref<string[]>([])

onMounted(() => {
  initPage(getSetMeals);
  reloadHeader('套餐管理', false);
  getSetMeals()
})

async function getSetMeals(pageValue: number | undefined = undefined) {
  if (pageValue != undefined)
    page.value = pageValue;

  let params = {
    page: page.value,
    pageSize: pageSize.value,
    name: input.value ? input.value : undefined
  }
  try {
    let r = await axiosRequest({
      url: '/setMeal/page',
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

function addOrUpdateSetMeal(option: string) {
  if (option == 'add')
    router.push('/setMeal/add')
  else
    router.push({path: '/setMeal/add', query: {id: option}})
}


/**
 * 删除处理函数
 * @param type
 * @param id
 */
function deleteSetMeal(type: string, id: string | null) {
  if (type == '批量' && id === null)
    if (selectedList.value.length === 0)
      return ElMessage.error('请选择删除对象')

  ElMessageBox.confirm('确认删除该套餐, 是否继续?', '确定删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(async () => {
    try {
      let r = await axiosRequest({
        url: '/setMeal',
        method: 'delete',
        params: {
          ids: type == '批量' ? selectedList.value.join(',') : id
        }
      })

      if (r.code == 1) {
        ElMessage.success('删除成功!')
        await getSetMeals(1)
      } else
        throw new Error(r.msg || '操作失败')

    } catch (e: any) {
      ElMessage.error('出错了: ' + e.message);
    }
  })
}

function updateSetMealStatus(row: any) {
  let params = {
    id: '',
    status: 0,
  }

  if (row === 1 || row === 0) { // 批量操作时, 把操作的选项填入
    if (selectedList.value.length == 0)
      return ElMessage.error('批量操作, 请先勾选操作菜品!')
    params.id = selectedList.value.join(',')
    params.status = row

  } else {
    params.id = row.id
    params.status = row.status == 0 ? 1 : 0
  }

  ElMessageBox.confirm('确认更改该菜品状态?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      let r = await axiosRequest({
        url: `/setMeal/status/${params.status}`,
        method: 'post',
        params: {ids: params.id}
      })

      if (r.code == 1) {
        ElMessage.success('菜品状态已经更改成功!')
        await getSetMeals(1)
      } else
        throw new Error(r.msg || '操作失败')
    } catch (e: any) {
      ElMessage.error('出错了: ' + e.message);
    }
  })
}

function handleSelectionChange(val: any) {
  let checkArr: string[] = []
  val.forEach((i: any) => {
    checkArr.push(i.id)
  })
  selectedList.value = checkArr
}
</script>


<template>
  <div class="dashboard-container" id="combo-app">
    <div class="container">
      <div class="tableBar">
        <el-input v-model="input" placeholder="请输入套餐名称"
                  style="width: 250px" clearable @keyup.enter.native="getSetMeals(1)">
          <template #prefix>
            <el-icon class="el-input__icon" style="cursor: pointer" @click="getSetMeals(1)">
              <Search/>
            </el-icon>
          </template>
        </el-input>

        <div class="tableLab">
          <span class="span-btn delete-button no-border-right" @click="deleteSetMeal('批量', null)">批量删除</span>
          <span class="span-btn update-button no-border-right" @click="updateSetMealStatus('1')">批量启售</span>
          <span class="span-btn delete-button no-border-right" @click="updateSetMealStatus('0')">批量停售</span>
          <el-button type="primary" size="large" @click="addOrUpdateSetMeal('add')">+ 新建套餐</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe class="tableBox" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="25"/>
        <el-table-column prop="name" label="套餐名称"/>
        <el-table-column prop="image" label="图片" align="center">
          <template #default="scope">
            <el-image style="width: auto; height: 40px; border:none;cursor: pointer;" :src="getImage(scope.row.image)"
                      :preview-src-list="[ `/api/download?name=${scope.row.image}` ]">
              <template #error>
                <img src="@/assets/images/noImg.png" style="width: auto; height: 40px; border:none;" alt="">
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="categoryName" label="套餐分类"/>
        <el-table-column prop="price" label="套餐价">
          <template #default="scope">
            <span>￥{{ scope.row.price / 100 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="售卖状态">
          <template #default="scope">
            <span>{{ scope.row.status == '0' ? '停售' : '启售' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="updateTime" label="最后操作时间"/>

        <el-table-column label="操作" width="160" align="center">
          <template #default="scope">
            <el-button type="text" size="small" class="update-button" @click="addOrUpdateSetMeal(scope.row.id)">
              修改
            </el-button>

            <el-button type="text" size="small" class="update-button" @click="updateSetMealStatus(scope.row)">
              {{ scope.row.status == '0' ? '启售' : '停售' }}
            </el-button>

            <el-button type="text" size="small" class="delete-button no-border-right"
                       @click="deleteSetMeal('单删', scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页部分 -->
      <el-pagination class="pagination" :page-sizes="pageSizes" v-model:page-size="pageSize" :total="total"
                     v-model:current-page="page" layout="total, sizes, prev, pager, next, jumper"/>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/assets/styles/common.scss" as *;
@use "@/assets/styles/button.scss" as *;
@use "@/assets/styles/table.scss" as *;

</style>
