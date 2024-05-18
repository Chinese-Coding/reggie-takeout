<script lang="ts" setup>
import {goBack, reloadHeader} from "@/ts/container.ts";
import {onMounted, ref} from "vue";
import {ElMessage, FormInstance} from "element-plus";
import axiosRequest from "@/utils/axiosRequest.ts";
import {getURLParam} from "@/ts/common.ts";
import {Plus, Search} from "@element-plus/icons-vue";

interface Dish {
  dishId: string
  name: string
  price: string
  copies: number
}

let actionType = ref('')

let categoriesForSetMeal = ref([]) // 套餐分类集, 选择套餐对应的分类
let dishCategories = ref([]) // 菜品分类集

let dishList = ref<Dish[]>([]) // 保存选中的, 或者原先就有的菜品列表
let dialogVisible = ref(false)
let imageURL = ref('')
let input = ref('')
let dishesInOneCate = ref([]) // 同一分类下的菜品集合, cate -> category


let setMealForm = ref({
  name: '',
  categoryId: '',
  price: '',
  code: '',
  image: '',
  description: '',
  setMealDishes: [],
  status: 1,
  idType: '',
})

let keyInd = ref(0)

let setMealFormRef = ref<FormInstance>()

let rules = {
  name: [
    {required: true, message: '请输入套餐名称', trigger: 'blur'},
    {min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur'}
  ],
  categoryId: [
    {required: true, message: '请选择套餐分类', trigger: 'change'}
  ],
  price: {
    required: true,
    validator: (rules: any, value: any, callback: any) => {
      if (!value)
        callback(new Error('请填写菜品价格'))
      else {
        let reg = /^\d+(\.\d{0,2})?$/
        if (reg.test(value)) {
          if (value < 10000)
            callback()
          else
            callback(new Error('菜品价格小于10000'))
        } else
          callback(new Error('菜品价格格式只能为数字,且最多保留两位小数'))
      }
    },
    trigger: 'blur',
  }
}
onMounted(async () => {
  try {
    let r2 = await axiosRequest({
      url: '/category/list',
      method: 'get',
      params: {type: 2}
    })

    if (r2.code == 1) {
      categoriesForSetMeal.value = r2.data.map((obj: any) => ({
        id: obj.id,
        name: obj.name,
        idType: obj.id // 不知道为什么加这个属性, 但是加上之后修改套餐时, 套餐会有个默认值, 而不是空
      }))
    } else
      throw new Error(r2.msg)

    let r1 = await axiosRequest({
      url: '/category/list',
      method: 'get',
      params: {type: 1}
    })
    if (r1.code == 1) {
      dishCategories.value = r1.data.map((obj: any) => ({
        id: obj.id,
        name: obj.name,
      }))
      await getDishesByCategoryId(r1.data[0].id)
    } else
      throw new Error(r1.msg)

  } catch (e: any) {
    ElMessage.error(e.message)
  }

  let id = getURLParam('id')
  actionType.value = id ? 'edit' : 'add'
  if (id) {
    await init(id)
    reloadHeader('修改套餐', true, '/setMeal');
  } else
    reloadHeader('添加套餐', true, '/setMeal');
})

async function init(id: string) {
  try {
    let r = await axiosRequest({
      url: `/setMeal/${id}`,
      method: 'get',
    })
    if (r.code == 1) {
      setMealForm.value = r.data
      setMealForm.value.price = String(r.data.price / 100)
      // 修改套餐时, 显示套餐分类的初始值
      setMealForm.value.idType = r.data.categoryId
      imageURL.value = `/api/download?name=${r.data.image}`
      dishList.value = r.data.setMealDishes
    } else
      throw new Error(r.msg)
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}

async function getDishesByCategoryId(categoryId: string) {
  try {
    let r = await axiosRequest({
      url: '/dish/list',
      method: 'get',
      params: {categoryId}
    })

    if (r.code == 1) {
      if (r.data.length == 0) {
        dishesInOneCate.value = []
        return
      }
      let newArr = r.data;
      newArr.forEach((n: any) => {
        n.dishId = n.id
        n.copies = 1
        n.dishName = n.name
      })
      dishesInOneCate.value = newArr
    } else
      new Error(r.msg)
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}

/**
 * 打开菜品添加对话框
 */
function openAddDish() {
  dialogVisible.value = true
  // 搜索条件清空，菜品重新查询，菜品类别选第一个重新查询
  input.value = ''
  keyInd.value = 0
  getDishesByCategoryId(dishCategories.value[0].id)
}


function deleteDish(index: number) {
  dishList.value.splice(index, 1)
}

function finishAdd() {
  dialogVisible.value = false
}

function handleAvatarSuccess(response: any, file: any, fileList: any) {
  if (response.code === 1) {
    // FIXME: 有意思, 这个地方竟然还要设置成 `/api/download` 才能上传成功
    imageURL.value = `/api/download?name=${response.data}`
    console.log(imageURL.value)
    setMealForm.value.image = response.data
  }
}

function onChange(file: any) {
  if (file) {
    let suffix = file.name.split('.')[1]
    let size = file.size / 1024 / 1024 < 2
    if (['png', 'jpeg', 'jpg'].indexOf(suffix) < 0) {
      ElMessage.error('上传图片只支持 png、jpeg、jpg 格式！')
      setMealForm.value.upload.clearFiles()
      return false
    }
    if (!size) {
      ElMessage.error('上传文件大小不能超过 2MB!')
      return false
    }
    return file
  }
}

function submitForm(flag: boolean) {
  setMealFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      let prams = {...setMealForm.value}
      console.log(prams)
      prams.price = String(Number.parseInt(prams.price) * 100)
      prams.setMealDishes = dishList.value.map((obj) => ({
        dishId: obj.dishId,
        name: obj.name,
        price: obj.price,
        copies: obj.copies,
      }))

      prams.categoryId = setMealForm.value.idType

      if (prams.setMealDishes.length < 1) {
        ElMessage.error('请选择菜品！')
        return
      }
      if (!imageURL.value) {
        ElMessage.error('请上传套餐图片')
        return
      }


      if (actionType.value == 'add') {
        delete prams.id // 删除其中的 id 属性
        try {
          let r = await axiosRequest({
            url: '/setMeal',
            method: 'post',
            data: prams
          })
          if (r.code == 1) {
            ElMessage.success('套餐添加成功！')
            if (!flag) {
              goBack()
            } else {
              dishList.value = []
              setMealForm.value = {
                name: '',
                categoryId: '',
                price: '',
                code: '',
                image: '',
                description: '',
                setMealDishes: [],
                status: 1,
                idType: '',
              }
              imageURL.value = ''
            }
          } else
            throw new Error(r.msg)
        } catch (e: any) {
          ElMessage.error('请求出错了：' + e.message)
        }
      } else {
        delete prams.updateTime // 删除更新时间这个属性
        try {
          let r = await axiosRequest({
            url: '/setMeal',
            method: 'put',
            data: {...prams}
          })

          if (r.code == 1) {
            ElMessage.success('套餐修改成功！')
            goBack()
          } else
            throw new Error(r.msg)

        } catch (e: any) {
          ElMessage.error('请求出错了：' + e.message)
        }
      }
    } else
      return false

  })
}

function handleClose() {
  dialogVisible.value = false
}

/**
 * 通过菜品名称查询菜品
 * @param name
 */
async function getDishByName(name: string) {
  try {
    let r = await axiosRequest({
      url: '/dish/list',
      method: 'get',
      params: {name}
    })

    if (r.code == 1) {
      let newArr = r.data
      newArr.forEach((n) => {
        n.dishId = n.id
        n.dishName = n.name
      })
      dishesInOneCate.value = newArr
    } else throw new Error(r.msg)
  } catch (e: any) {
    ElMessage.error('请求出错了：' + e.message)
  }
}

function changeCategory(index: number, id: string) {
  keyInd.value = index
  getDishesByCategoryId(id)
}


</script>

<template>
  <div class="addBrand-container" id="combo-add-app">
    <div class="container">
      <el-form ref="setMealFormRef" :model="setMealForm" :rules="rules" :inline="true" label-width="180px"
               class="demo-ruleForm">
        <div>
          <el-form-item label="套餐名称:" prop="name">
            <el-input v-model="setMealForm.name" placeholder="请填写套餐名称" maxlength="20"/>
          </el-form-item>

          <el-form-item label="套餐分类:" prop="idType">
            <el-select v-model="setMealForm.idType" placeholder="请选择套餐分类" @change="$forceUpdate()">
              <el-option v-for="(item, index) in categoriesForSetMeal" :key="index" :label="item.name"
                         :value="item.id"/>
            </el-select>
          </el-form-item>
        </div>

        <div>
          <el-form-item label="套餐价格:" prop="price">
            <el-input v-model="setMealForm.price" placeholder="请设置套餐价格"/>
          </el-form-item>
        </div>

        <div>
          <el-form-item label="套餐菜品:" class="setMealFood">
            <el-form-item>
              <div class="addDish">
                <span v-if="dishList.length == 0" class="addBut" @click="openAddDish"> + 添加菜品</span>

                <div v-if="dishList.length != 0" class="content">
                  <div class="addBut" style="margin-bottom: 20px" @click="openAddDish">+ 添加菜品</div>

                  <div class="table">
                    <el-table :data="dishList" style="width: 100%">
                      <el-table-column prop="name" label="名称" width="180" align="center"/>
                      <el-table-column prop="price" label="原价" width="100">
                        <template #default="scope">
                          {{ Number(scope.row.price) / 100 }}
                        </template>
                      </el-table-column>

                      <el-table-column prop="address" label="份数" align="center" width="180">
                        <template #default="scope">
                          <el-input-number v-model="scope.row.copies" size="small" :min="1" :max="99" label="描述文字"/>
                        </template>
                      </el-table-column>

                      <el-table-column prop="address" label="操作" width="180px;" align="center">
                        <template #default="scope">
                          <el-button type="text" size="small" @click="deleteDish(scope.$index)"> 删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-form-item>
        </div>

        <div>
          <el-form-item label="套餐图片:" class="uploadImg">
            <el-upload class="avatar-uploader" action="/api/upload" :show-file-list="false"
                       :on-success="handleAvatarSuccess" :on-change="onChange" ref="upload">
              <img v-if="imageURL" :src="imageURL" class="avatar" alt=""/>
              <el-icon v-else class="avatar-uploader-icon">
                <Plus/>
              </el-icon>
            </el-upload>
          </el-form-item>
        </div>

        <div class="address">
          <el-form-item label="套餐描述:">
            <el-input v-model="setMealForm.description" type="textarea" :rows="3" placeholder="套餐描述，最长200字"
                      maxlength="200"/>
          </el-form-item>
        </div>

        <div class="subBox address">
          <el-form-item>
            <el-button size="large" @click="goBack()"> 取消</el-button>
            <el-button size="large" type="primary" @click="submitForm( false)"> 保存</el-button>
            <el-button v-if="actionType == 'add'" size="large" type="primary" class="continue"
                       @click="submitForm(true)">
              保存并继续添加套餐
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>

    <el-dialog title="添加菜品" class="addDishList" v-model="dialogVisible"
               width="60%" :before-close="handleClose">
      <el-input v-model="input" class="searchDish" placeholder="请输入菜品名称进行搜索" style="width: 250px"
                size="small" clearable>
        <template #prefix>
          <el-icon class="el-input__icon" style="cursor: pointer" @click="getDishByName(input)">
            <Search/>
          </el-icon>
        </template>
      </el-input>

      <div class="addDishCon">
        <div class="leftCont">
          <div v-show="input.trim() == ''" class="tabBut">
              <span v-for="(item, index) in dishCategories" :key="index" :class="{act:index == keyInd}"
                    @click="changeCategory(index, item.id)">{{ item.name }}</span>
          </div>

          <div class="tabList">
            <div class="table">
              <div v-if="dishesInOneCate.length == 0" style="padding-left:10px">暂无菜品!</div>

              <el-checkbox-group v-if="dishesInOneCate.length > 0" v-model="dishList">
                <div v-for="(item, index) in dishesInOneCate" :key="index" class="items">
                  <el-checkbox :key="index" :label="item">
                    <div class="item">
                      <span style="flex: 3;text-align: left">{{ item.dishName }}</span>
                      <span>{{ item.status == 0 ? '停售' : '在售' }}</span>
                      <span>{{ Number(item.price) / 100 }}</span>
                    </div>
                  </el-checkbox>
                </div>
              </el-checkbox-group>
            </div>
          </div>
        </div>

        <div class="ritCont">
          <div class="tit">
            已选菜品({{ dishList.length }})
          </div>

          <div class="items">
            <div v-for="(item, index) in dishList" :key="id" class="item">
              <span>{{ item.dishName }}</span>
              <span class="price">￥ {{ Number(item.price) / 100 }} </span>
              <span class="del" @click="deleteDish(index)">
                  <img src="@/assets/images/icons/btn_clean@2x.png" alt="">
                </span>
            </div>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
          <el-button size="large" @click="handleClose">取 消</el-button>
          <el-button size="large" type="primary" @click="finishAdd">确 定</el-button>
        </span>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/styles/common.scss" as *;
@use "@/assets/styles/button.scss" as *;
@use "@/assets/styles/table.scss" as *;


:deep(.el-dialog) {
  width: 60%;
}

// 设置上传图片方框样式
:deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);

  &:hover {
    border-color: #ffc200;
  }
}

:deep(.el-icon.avatar-uploader-icon) {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.avatar-uploader {
  .avatar {
    width: 178px;
    height: 178px;
    display: block;
  }
}

// 输入框和选择框样式
:deep(.el-input) {
  width: 293px;

  .el-input__wrapper {
    height: 40px;
    line-height: 40px;
  }
}

:deep(.el-select) {
  .el-select__wrapper {
    height: 40px;
    line-height: 40px;
  }

  width: 293px;
}

.addDish {
  .addBut {
    background: #ffc200;
    display: inline-block;
    padding: 0 20px;
    line-height: 40px;
    cursor: pointer;
    border-radius: 4px;
    color: #333333;
    font-weight: 500;

    .content {
      background: #fafafb;
      padding: 20px;
      border: solid 1px #ccc;
      border-radius: 3px;
    }
  }

  .table {
    border: solid 1px #EBEEF5;
    border-radius: 3px;

    th {
      padding: 5px 0;
    }

    td {
      padding: 7px 0;
    }
  }
}

.addDishList {
  .searchDish {
    position: absolute;
    top: 10px;
    right: 20px;
  }
}

.addDishCon {
  padding: 0 20px;
  display: flex;
  line-height: 40px;

  .leftCont {
    display: flex;
    border-right: solid 2px #E4E7ED;
    width: 60%;
    padding: 15px;

    .act {
      border-color: #FFC200 !important;
      color: #FFC200 !important;
    }

    .tabBut {
      width: 110px;

      span {
        display: block;
        text-align: center;
        border-right: solid 2px #f4f4f4;
        cursor: pointer;
      }
    }

    .tabList {
      flex: 1;
      padding: 15px;

      .table {
        border: 1px solid #f4f4f4;

        .items {
          border-bottom: solid 1px #f4f4f4;
          padding: 0 10px;
          display: flex;

          .item {
            display: flex;
            padding-right: 20px;

            span {
              display: inline-block;
              text-align: center;
              flex: 1;
            }
          }
        }
      }


    }
  }

  .ritCont {
    width: 40%;
    padding: 0 15px;

    .item {
      box-shadow: 0 1px 4px 3px rgba(0, 0, 0, 0.03);
      display: flex;
      text-align: center;
      padding: 0 10px;
      margin-bottom: 20px;
      border-radius: 6px;
      color: #818693;

      span:first-child {
        text-align: left;
        color: #20232A;
      }

      .price {
        display: inline-block;
        flex: 1;
      }

      .del {
        cursor: pointer;

        img {
          position: relative;
          top: 5px;
          width: 20px;
        }
      }
    }
  }
}
</style>