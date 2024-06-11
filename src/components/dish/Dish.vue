<script lang="ts" setup>
import {ElMessage, ElMessageBox} from "element-plus";
import {getImage, initPage, page, pageSize, pageSizes, total} from "@/ts/common.ts";
import {onMounted, ref} from "vue";
import axiosRequest from "@/utils/axiosRequest.ts";
import router from "@/router.ts";
import {Search} from "@element-plus/icons-vue";
import {reloadHeader} from "@/ts/container.ts";

let input = ref('');
let tableData = ref([]);
let selectedList = ref<string[]>([]);

onMounted(() => {
  initPage(getDishes);
  reloadHeader('菜品管理', false);
  getDishes()
})

async function getDishes(pageValue: number | undefined = undefined) {
  if (pageValue != undefined)
    page.value = pageValue;

  let params = {
    page: page.value,
    pageSize: pageSize.value,
    name: input.value ? input.value : undefined
  }

  try {
    let r = await axiosRequest({
      url: '/dish/page',
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


/**
 * 删除处理函数
 * @param type
 * @param id
 */
function deleteDish(type: string, id: string | null) {
  if (type == '批量' && id === null)
    if (selectedList.value.length === 0)
      return ElMessage.error('请选择删除对象')

  ElMessageBox.confirm('确认删除该菜品, 是否继续?', '确定删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(async () => {
    try {
      let r = await axiosRequest({
        url: '/dish',
        method: 'delete',
        params: {
          ids: type == '批量' ? selectedList.value.join(',') : id
        }
      })

      if (r.code == 1) {
        ElMessage.success('删除成功!')
        await getDishes(1)
      } else
        throw new Error(r.msg || '操作失败')

    } catch (e: any) {
      ElMessage.error('出错了: ' + e.message);
    }
  })
}

function updateDishStatus(row: number | any) {
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
        url: `/dish/status/${params.status}`,
        method: 'post',
        params: {ids: params.id}
      })

      if (r.code == 1) {
        ElMessage.success('菜品状态已经更改成功!')
        await getDishes(1)
      } else
        throw new Error(r.msg || '操作失败')
    } catch (e: any) {
      ElMessage.error('出错了: ' + e.message);
    }
  })
}

function addOrUpdateDish(option: string) {
  if (option == 'add')
    router.push('/dish/add')
  else
    router.push({path: '/dish/add', query: {id: option}})
}

function handleSelectionChange(val: any) {
  console.log('1: ' + val)
  let checkArr: string[] = []
  val.forEach((i: any) => {
    checkArr.push(i.id)
  })
  console.log(checkArr)
  selectedList.value = checkArr
}
</script>

<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <el-input v-model="input" placeholder="请输入菜品名称" style="width: 250px" clearable
                  @keyup.enter.native="getDishes(1)">
          <template #prefix>
            <el-icon class="el-input__icon" style="cursor: pointer" @click="getDishes(1)">
              <Search/>
            </el-icon>
          </template>
        </el-input>

        <div class="tableLab">
          <span class="span-btn delete-button no-border-right" @click="deleteDish('批量', null)">批量删除</span>
          <span class="span-btn update-button no-border-right" @click="updateDishStatus(1)">批量启售</span>
          <span class="span-btn delete-button no-border-right" @click="updateDishStatus(0)">批量停售</span>
          <el-button type="primary" size="large" @click="addOrUpdateDish('add')">+ 新建菜品</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe class="tableBox" @selection-change="handleSelectionChange">
        <!-- 多选框, 导入失效, 暂时不知道为什么 TODO-->
        <el-table-column type="selection" width="25"/>

        <el-table-column prop="name" label="菜品名称"/>

        <el-table-column prop="image" label="图片" align="center">
          <template #default="scope">
            <el-image style="width: auto; height: 40px; border:none;cursor: pointer;"
                      :src="getImage(scope.row.image)"
                      :preview-src-list="[ `/download?name=${scope.row.image}` ]">
              <template #error>
                <img src="@/assets/images/noImg.png" style="width: auto; height: 40px; border:none;" alt="">
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="categoryName" label="菜品分类"/>

        <el-table-column label="售价">
          <template #default="scope">
            <span style="margin-right: 10px;">￥{{ scope.row.price / 100 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="售卖状态">
          <template #default="scope">
            <span style="margin-right: 10px;">{{ scope.row.status == 0 ? '停售' : '启售' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="updateTime" label="最后操作时间"/>

        <el-table-column label="操作" width="185" align="center" show-overflow-tooltip>
          <template #default="scope">
            <el-button text size="small" class="update-button" @click="addOrUpdateDish(scope.row.id)">
              修改
            </el-button>

            <el-button text size="small" class="update-button" @click="updateDishStatus(scope.row)">
              {{ scope.row.status == 0 ? '启售' : '停售' }}
            </el-button>

            <el-button text size="small" class="delete-button no-border-right"
                       @click="deleteDish('单删', scope.row.id)">
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
