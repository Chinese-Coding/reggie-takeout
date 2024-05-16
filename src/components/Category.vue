<script lang="ts" setup>
import {ref, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from "element-plus";
import {initPage, page, pageSize, pageSizes, total} from "@/ts/common.ts";
import axiosRequest from "@/utils/axiosRequest.ts";
import {reloadHeader} from "@/ts/container.ts";


let actionType = ref('');
let tableData = ref([]);
let type = ref('');
let categoryData = ref({
  title: '添加菜品分类',
  dialogVisible: false, // 对话窗可见性
  // 以上两个属性为前端特有属性
  categoryId: '',
  name: '',
  sort: 0
});


// 刚刚进入页面的时候初始化一下
onMounted(() => {
  initPage(getCategories);
  reloadHeader('分类管理', false);
  getCategories();
})

async function getCategories(pageValue: number | undefined = undefined) {
  if (pageValue != undefined)
    page.value = pageValue;
  try {
    let r = await axiosRequest({
      url: '/category/page',
      method: 'get',
      params: {
        page: page.value,
        pageSize: pageSize.value
      }
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


function addCategory(st: string) {
  if (st == 'class') {
    categoryData.value.title = '新增菜品分类'
    type.value = '1'
  } else if (st == 'meal') {
    categoryData.value.title = '新增套餐分类'
    type.value = '2'
  }
  actionType.value = 'add'
  categoryData.value.name = ''
  categoryData.value.sort = 0
  categoryData.value.dialogVisible = true
}

function updateCategory(dat: any) {
  actionType.value = 'update'
  categoryData.value.title = '修改分类'
  categoryData.value.categoryId = dat.id
  categoryData.value.name = dat.name
  categoryData.value.sort = dat.sort
  categoryData.value.dialogVisible = true
}

// 关闭弹窗
function handleClose() {
  categoryData.value.dialogVisible = false
}

/**
 * 删除分类
 * @param id
 */
function deleteCat(id: any) {
  ElMessageBox.confirm('此操作将永久删除该文件, 是否继续?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      let r = await axiosRequest({
        url: '/category',
        method: 'delete',
        params: {
          id: id
        }
      })

      if (r.code == 1) {
        ElMessage.success('删除成功！')
        await getCategories(1)
      } else
        throw new Error(r.msg || '操作失败')
    } catch (e: any) {
      ElMessage.error('出错了：' + e)
    }
  })
}


// 数据提交
async function submitForm(_continue: boolean) {
  let classDataValue = categoryData.value;
  if (actionType.value == 'add') {
    try {
      let r = await axiosRequest({
        url: '/category',
        method: 'post',
        data: {
          name: classDataValue.name,
          type: type.value,
          sort: classDataValue.sort
        }
      })
      if (r.code == 1) {
        ElMessage.success('分类添加成功!')
        if (!_continue)
          categoryData.value.dialogVisible = false
        else {
          categoryData.value.name = ''
          categoryData.value.sort = 0
        }
        await getCategories(1)
      } else
        throw new Error(r.msg || '操作失败')
    } catch (e: any) {
      ElMessage.error('出错了：' + e.message)
    }

  } else if (actionType.value == 'update') {
    try {
      let r = await axiosRequest({
        url: '/category',
        method: 'put',
        data: {
          id: categoryData.value.categoryId,
          name: categoryData.value.name,
          sort: categoryData.value.sort
        }
      })

      if (r.code == 1) {
        ElMessage.success('分类修改成功!')
        categoryData.value.dialogVisible = false
        await getCategories(1)
      } else
        throw new Error(r.msg || '操作失败')
    } catch (e: any) {
      ElMessage.error('出错了：' + e.message)
    }
  }
}
</script>

<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar" style="display: inline-block">
        <el-button type="primary" class="continue" size="large" @click="addCategory('class')">
          + 新增菜品分类
        </el-button>

        <el-button type="primary" size="large" @click="addCategory('meal')">
          + 新增套餐分类
        </el-button>
      </div>

      <el-table :data="tableData" stripe class="tableBox">
        <el-table-column prop="name" label="分类名称"/>
        <el-table-column prop="type" label="分类类型">
          <template #default="scope">
            <span>{{ scope.row.type == '1' ? '菜品分类' : '套餐分类' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="updateTime" label="操作时间">
          <template #default="scope">{{ scope.row.updateTime }}</template>
        </el-table-column>

        <el-table-column prop="sort" label="排序"/>

        <el-table-column label="操作" width="160" align="center">
          <template #default="scope">
            <el-button text size="small" class="update-button" @click="updateCategory(scope.row)">
              修改
            </el-button>

            <el-button text size="small" class="delete-button no-border-right"
                       @click="deleteCat(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" :page-sizes="pageSizes" v-model:page-size="pageSize" v-model:current-page="page"
                     layout="total, sizes, prev, pager, next, jumper" :total="total"/>
    </div>

    <!-- 这个地方遮盖, 范围有问题, 我不知道怎么解决 :modal-append-to-body="false" -->
    <!-- 放弃修改了, 看起来不一样就不一样吧 -->

    <el-dialog :title="categoryData.title" v-model="categoryData.dialogVisible"
               width="30%" :before-close="handleClose" :center="true" :modal-append-to-body="false"
               :append-to-body="false">
      <el-form label-width="100px">
        <el-form-item label="分类名称：">
          <el-input v-model="categoryData.name" text placeholder="请输入分类名称" minlength="1" maxlength="14"/>
        </el-form-item>
        <el-form-item label="排序：">
          <el-input v-model="categoryData.sort" type="number" placeholder="请输入排序"/>
        </el-form-item>
      </el-form>
      <el-footer>
        <el-button size="large" @click="categoryData.dialogVisible = false">
          取 消
        </el-button>

        <el-button type="primary" size="large" @click="submitForm(false)">
          确 定
        </el-button>

        <el-button v-if="actionType != 'update'" type="primary" size="large"
                   class="continue" @click="submitForm(true)">
          保存并继续添加
        </el-button>
      </el-footer>

    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/styles/common.scss" as *;
@use "@/assets/styles/button.scss" as *;
@use "@/assets/styles/table.scss" as *;
@use "@/assets/styles/dialog.scss" as *;

</style>